import hashlib
import io
import json
import os
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch
from urllib.error import HTTPError
from urllib.request import Request
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import fetch_sdk_binaries as sdk


class SdkBinariesTest(unittest.TestCase):
    def test_collision_does_not_replace_previously_installed_sdks(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(sdk, 'ROOT', Path(directory)):
            root = Path(directory)
            installed = root / '.sdk-binaries' / 'resolved.properties'
            installed.parent.mkdir()
            installed.write_text('previous installation')
            specs, archives = {}, {}
            for module in ('one', 'two'):
                classes, aar_data = io.BytesIO(), io.BytesIO()
                with zipfile.ZipFile(classes, 'w') as jar:
                    jar.writestr('a/a.class', b'bytecode')
                with zipfile.ZipFile(aar_data, 'w') as aar:
                    aar.writestr('classes.jar', classes.getvalue())
                payload = aar_data.getvalue()
                filename = f'com/example/{module}/1.0.1/{module}.aar'
                specs[module] = {'repository': f'owner/{module}', 'coordinate': f'com.example:{module}'}
                info = {'sourceRepository': specs[module]['repository'], 'coordinate': specs[module]['coordinate'],
                        'version': '1.0.1', 'sourceSha': 'a' * 40, 'files': {filename: hashlib.sha256(payload).hexdigest()}}
                archives[module] = root / f'{module}.zip'
                with zipfile.ZipFile(archives[module], 'w') as archive:
                    archive.writestr('sdk-info.json', json.dumps(info))
                    archive.writestr('maven/' + filename, payload)
            (root / 'scripts').mkdir()
            (root / 'scripts/sdk_binaries.json').write_text(json.dumps(specs))
            with patch.object(sys, 'argv', ['resolver']), patch.object(sdk, 'resolve_release', side_effect=lambda name, _: archives[name]), \
                    patch('builtins.print'):
                with self.assertRaisesRegex(ValueError, 'Duplicate Android class'):
                    sdk.main()
            self.assertEqual('previous installation', installed.read_text())
            self.assertEqual([], list(root.glob('.sdk-binaries-stage-*')))

    def test_independently_obfuscated_sdks_cannot_publish_colliding_classes(self):
        for duplicate in (False, True):
            with self.subTest(duplicate=duplicate), tempfile.TemporaryDirectory() as directory:
                for module in ('one', 'two'):
                    classes = io.BytesIO()
                    with zipfile.ZipFile(classes, 'w') as jar:
                        jar.writestr('a/a.class' if duplicate else f'com/example/{module}/a.class', b'bytecode')
                    with zipfile.ZipFile(Path(directory) / f'{module}.aar', 'w') as aar:
                        aar.writestr('classes.jar', classes.getvalue())
                if duplicate:
                    with self.assertRaisesRegex(ValueError, 'Duplicate Android class a/a.class'):
                        sdk.validate_android_class_names(Path(directory))
                else:
                    sdk.validate_android_class_names(Path(directory))

    def test_private_assets_use_authenticated_api_and_verified_cache(self):
        payload = b'verified archive'
        checksum = hashlib.sha256(payload).hexdigest()
        api_root = 'https://api.github.com/repos/owner/private-sdk'
        release = {'assets': [
            {'name': 'maven-repository.zip', 'id': 42},
            {'name': 'maven-repository.zip.sha256', 'id': 43},
        ]}
        responses = {
            api_root + '/releases/latest': json.dumps(release).encode(),
            api_root + '/releases/assets/43': checksum.encode() + b'  maven-repository.zip',
            api_root + '/releases/assets/42': payload,
        }
        with tempfile.TemporaryDirectory() as directory, patch.object(sdk, 'ROOT', Path(directory)), \
                patch.object(sdk, 'download', side_effect=lambda url, **_: responses[url]) as download:
            spec = {'repository': 'owner/private-sdk'}
            path = sdk.resolve_release('private-sdk', spec)
            self.assertEqual(payload, path.read_bytes())
            download.reset_mock()
            self.assertEqual(path, sdk.resolve_release('private-sdk', spec))
            self.assertNotIn(api_root + '/releases/assets/42', [call.args[0] for call in download.call_args_list])
            path.write_bytes(b'corrupt cache')
            self.assertEqual(payload, sdk.resolve_release('private-sdk', spec).read_bytes())

    def test_authentication_is_sent_only_to_github_api(self):
        with patch.dict(os.environ, {'GITHUB_TOKEN': 'test-token'}), patch.object(sdk, 'build_opener') as opener:
            opener.return_value.open.return_value.__enter__.return_value.read.return_value = b'data'
            for url, accept, authorized in [
                ('https://api.github.com/repos/owner/sdk/releases/latest', True, True),
                ('https://api.github.com/repos/owner/sdk/releases/assets/42', False, True),
                ('https://release-assets.githubusercontent.com/file.zip', False, False),
                ('https://api.github.com.example.org/file.zip', False, False),
                ('http://api.github.com/file.zip', False, False),
            ]:
                with self.subTest(url=url):
                    sdk.download(url, api=accept)
                    request = opener.return_value.open.call_args.args[0]
                    self.assertEqual('Bearer test-token' if authorized else None, request.get_header('Authorization'))
                    self.assertEqual('application/vnd.github+json' if accept else 'application/octet-stream', request.get_header('Accept'))

    def test_asset_redirect_does_not_forward_token_to_storage(self):
        request = Request('https://api.github.com/repos/owner/sdk/releases/assets/42',
                          headers={'Authorization': 'Bearer test-token'})
        handler = sdk.GitHubRedirectHandler()
        redirected = handler.redirect_request(request, None, 302, 'Found', {},
                                              'https://release-assets.githubusercontent.com/file.zip')
        self.assertIsNone(redirected.get_header('Authorization'))
        same_origin = handler.redirect_request(request, None, 301, 'Moved', {},
                                               'https://api.github.com/repositories/123/releases/assets/42')
        self.assertEqual('Bearer test-token', same_origin.get_header('Authorization'))

    def test_private_repository_failure_identifies_access_requirement(self):
        error = HTTPError('https://api.github.com/repos/owner/private/releases/latest', 404, 'Not Found', {}, io.BytesIO())
        with patch.object(sdk, 'download', side_effect=error):
            with self.assertRaisesRegex(RuntimeError, 'owner/private.*SDK_READ_TOKEN or DEPLOY_TOKEN'):
                sdk.resolve_release('private-sdk', {'repository': 'owner/private'})

    def test_archive_identity_paths_and_checksums_are_verified(self):
        spec = {'repository': 'owner/sdk', 'coordinate': 'com.example:sdk'}
        for case in ('valid', 'wrong-repository', 'unsafe-path', 'bad-checksum'):
            with self.subTest(case=case), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                filename = '../outside' if case == 'unsafe-path' else 'com/example/sdk/1.0.1/sdk-1.0.1.pom'
                data = b'<project/>'
                info = {'sourceRepository': 'other/sdk' if case == 'wrong-repository' else spec['repository'],
                        'coordinate': spec['coordinate'], 'version': '1.0.1', 'sourceSha': 'a' * 40,
                        'files': {filename: '0' * 64 if case == 'bad-checksum' else hashlib.sha256(data).hexdigest()}}
                path = root / 'sdk.zip'
                with zipfile.ZipFile(path, 'w') as archive:
                    archive.writestr('sdk-info.json', json.dumps(info))
                    archive.writestr('maven/' + filename, data)
                if case == 'valid':
                    self.assertEqual(info, sdk.validate_archive(path, spec, root / 'maven'))
                    self.assertEqual(data, (root / 'maven' / filename).read_bytes())
                else:
                    with self.assertRaises(ValueError):
                        sdk.validate_archive(path, spec, root / 'maven')
                    self.assertFalse((root / 'outside').exists())
