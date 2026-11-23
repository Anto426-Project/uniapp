import hashlib
import io
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
import unittest
from unittest.mock import patch
from urllib.error import HTTPError
from urllib.request import Request
import zipfile

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'sdk'))
import fetch_sdk_binaries as sdk


class SdkBinariesTest(unittest.TestCase):
    def setUp(self):
        sdk.local_github_token.cache_clear()
        self.addCleanup(sdk.local_github_token.cache_clear)

    def test_environment_tokens_take_priority_over_local_credentials(self):
        for environment, expected in [
            ({'GITHUB_TOKEN': 'workflow-token', 'GH_TOKEN': 'cli-token'}, 'workflow-token'),
            ({'GH_TOKEN': 'cli-token'}, 'cli-token'),
        ]:
            with self.subTest(environment=environment), patch.dict(os.environ, environment, clear=True), \
                    patch.object(sdk, 'local_github_token') as local:
                self.assertEqual(expected, sdk.github_token())
                local.assert_not_called()

    def test_local_github_cli_login_is_reused_and_cached(self):
        with patch.dict(os.environ, {}, clear=True), patch.object(sdk.subprocess, 'run') as run:
            run.return_value = subprocess.CompletedProcess([], 0, stdout='local-token\n')
            self.assertEqual('local-token', sdk.github_token())
            self.assertEqual('local-token', sdk.github_token())
            run.assert_called_once()
            self.assertEqual(['gh', 'auth', 'token', '--hostname', 'github.com'], run.call_args.args[0])
            self.assertEqual(sdk.ROOT, run.call_args.kwargs['cwd'])
            self.assertEqual(subprocess.PIPE, run.call_args.kwargs['stdout'])
            self.assertEqual(subprocess.DEVNULL, run.call_args.kwargs['stderr'])
            self.assertEqual(subprocess.DEVNULL, run.call_args.kwargs['stdin'])

    def test_git_credentials_are_used_when_github_cli_is_unavailable(self):
        for failure in (FileNotFoundError(), subprocess.TimeoutExpired(['gh'], 10),
                        subprocess.CompletedProcess([], 1, stdout='ignored-secret'),
                        subprocess.CompletedProcess([], 0, stdout='\n')):
            with self.subTest(failure=type(failure).__name__), patch.dict(os.environ, {}, clear=True), \
                    patch.object(sdk.subprocess, 'run') as run:
                sdk.local_github_token.cache_clear()
                run.side_effect = [failure, subprocess.CompletedProcess([], 0,
                    stdout='protocol=https\nhost=github.com\nusername=local-user\npassword=git=token\n')]
                self.assertEqual('git=token', sdk.github_token())
                self.assertEqual(['git', '-c', 'credential.interactive=false', 'credential', 'fill'], run.call_args.args[0])
                self.assertEqual('protocol=https\nhost=github.com\n\n', run.call_args.kwargs['input'])
                environment = run.call_args.kwargs['env']
                self.assertEqual('0', environment['GIT_TERMINAL_PROMPT'])
                self.assertEqual('Never', environment['GCM_INTERACTIVE'])
                self.assertEqual('', environment['GIT_ASKPASS'])
                self.assertEqual('', environment['SSH_ASKPASS'])
                self.assertEqual(10, run.call_args.kwargs['timeout'])

    def test_unavailable_local_credentials_allow_anonymous_downloads(self):
        for git_result in (FileNotFoundError(), OSError('helper failed'),
                           subprocess.TimeoutExpired(['git'], 10),
                           subprocess.CompletedProcess([], 1, stdout='password=ignored-secret'),
                           subprocess.CompletedProcess([], 0, stdout='username=user\n')):
            with self.subTest(result=type(git_result).__name__), patch.dict(os.environ, {}, clear=True), \
                    patch.object(sdk.subprocess, 'run', side_effect=[FileNotFoundError(), git_result]):
                sdk.local_github_token.cache_clear()
                self.assertEqual('', sdk.github_token())

    def test_external_downloads_do_not_read_local_credentials(self):
        with patch.object(sdk, 'github_token') as token, patch.object(sdk, 'build_opener') as opener:
            opener.return_value.open.return_value.__enter__.return_value.read.return_value = b'data'
            for url in ('https://release-assets.githubusercontent.com/file.zip',
                        'https://api.github.com.example.org/file.zip', 'http://api.github.com/file.zip'):
                sdk.download(url)
            token.assert_not_called()

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
                specs[module] = {'repository': f'owner/{module}', 'coordinate': f'com.example:{module}', 'version': '1.0.1'}
                info = {'sourceRepository': specs[module]['repository'], 'coordinate': specs[module]['coordinate'],
                        'version': '1.0.1', 'sourceSha': 'a' * 40, 'files': {filename: hashlib.sha256(payload).hexdigest()}}
                archives[module] = root / f'{module}.zip'
                with zipfile.ZipFile(archives[module], 'w') as archive:
                    archive.writestr('sdk-info.json', json.dumps(info))
                    archive.writestr('maven/' + filename, payload)
            (root / 'scripts/sdk').mkdir(parents=True)
            (root / 'scripts/sdk/sdk_binaries.json').write_text(json.dumps(specs))
            with patch.object(sys, 'argv', ['resolver']), patch.object(sdk, 'load_specs', return_value=specs), patch.object(sdk, 'resolve_release', side_effect=lambda name, _: archives[name]), \
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
        release = {'tag_name': 'v1.0.1', 'assets': [
            {'name': 'maven-repository.zip', 'id': 42},
            {'name': 'maven-repository.zip.sha256', 'id': 43},
        ]}
        responses = {
            api_root + '/releases/tags/v1.0.1': json.dumps(release).encode(),
            api_root + '/releases/assets/43': checksum.encode() + b'  maven-repository.zip',
            api_root + '/releases/assets/42': payload,
        }
        with tempfile.TemporaryDirectory() as directory, patch.object(sdk, 'ROOT', Path(directory)), \
                patch.object(sdk, 'download', side_effect=lambda url, **_: responses[url]) as download:
            spec = {'repository': 'owner/private-sdk', 'version': '1.0.1'}
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
                sdk.resolve_release('private-sdk', {'repository': 'owner/private', 'version': '1.0.1'})

    def test_archive_identity_paths_and_checksums_are_verified(self):
        spec = {'repository': 'owner/sdk', 'coordinate': 'com.example:sdk', 'version': '1.0.1'}
        for case in ('valid', 'wrong-repository', 'wrong-version', 'unsafe-path', 'bad-checksum'):
            with self.subTest(case=case), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                filename = '../outside' if case == 'unsafe-path' else 'com/example/sdk/1.0.1/sdk-1.0.1.pom'
                data = b'<project/>'
                info = {'sourceRepository': 'other/sdk' if case == 'wrong-repository' else spec['repository'],
                        'coordinate': spec['coordinate'], 'version': '1.0.2' if case == 'wrong-version' else '1.0.1', 'sourceSha': 'a' * 40,
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

    def test_license_manifest_is_verified_without_allowing_unlisted_or_unsafe_entries(self):
        spec = {'repository': 'owner/sdk', 'coordinate': 'com.example:sdk', 'version': '1.0.1'}
        for case in ('valid', 'bad-checksum', 'unlisted-file', 'unsafe-path', 'unexpected-path', 'aliased-path', 'invalid-manifest'):
            with self.subTest(case=case), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                pom_name = 'com/example/sdk/1.0.1/sdk-1.0.1.pom'
                data = b'<project/>'
                name = {'unsafe-path': 'licenses/../../outside', 'unexpected-path': 'script.py',
                        'aliased-path': 'licenses//dependency.txt'}.get(case, 'LICENSE')
                license_data = b'SDK license document'
                license_files = {name: '0' * 64 if case == 'bad-checksum' else hashlib.sha256(license_data).hexdigest()}
                info = {'sourceRepository': spec['repository'], 'coordinate': spec['coordinate'],
                        'version': spec['version'], 'sourceSha': 'a' * 40,
                        'files': {pom_name: hashlib.sha256(data).hexdigest()},
                        'licenseFiles': [] if case == 'invalid-manifest' else license_files}
                path = root / 'sdk.zip'
                with zipfile.ZipFile(path, 'w') as archive:
                    archive.writestr('sdk-info.json', json.dumps(info))
                    archive.writestr('maven/' + pom_name, data)
                    archive.writestr(name, license_data)
                    if case == 'unlisted-file':
                        archive.writestr('NOTICE', b'unlisted notice')
                if case == 'valid':
                    self.assertEqual(info, sdk.validate_archive(path, spec, root / 'maven'))
                    self.assertEqual(data, (root / 'maven' / pom_name).read_bytes())
                else:
                    with self.assertRaises(ValueError):
                        sdk.validate_archive(path, spec, root / 'maven')
                    self.assertFalse((root / 'outside').exists())

    def test_catalog_controls_versions_and_rejects_dynamic_selectors(self):
        with tempfile.TemporaryDirectory() as directory, patch.object(sdk, 'ROOT', Path(directory)):
            root = Path(directory)
            (root / 'scripts/sdk').mkdir(parents=True)
            (root / 'gradle').mkdir()
            (root / 'scripts/sdk/sdk_binaries.json').write_text(json.dumps({
                'sdk': {'repository': 'owner/sdk', 'coordinate': 'com.example:sdk'},
            }))
            for version in ('1.0.7', '2.3.4', 'latest.release', '1.+', '[1,2)'):
                (root / 'gradle/libs.versions.toml').write_text(
                    '[versions]\nsdk = "' + version + '"\n[libraries]\n'
                    'sdk = {module = "com.example:sdk", version.ref = "sdk"}\n'
                )
                with self.subTest(version=version):
                    if version in ('1.0.7', '2.3.4'):
                        self.assertEqual(version, sdk.load_specs()['sdk']['version'])
                    else:
                        with self.assertRaisesRegex(ValueError, 'explicit published SDK version'):
                            sdk.load_specs()
