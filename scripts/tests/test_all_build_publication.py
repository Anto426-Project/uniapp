import hashlib
import json
import os
from pathlib import Path
import shutil
import sys
import subprocess
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import collect_build_artifacts as collector
import publish_builds as publisher
import prepare_build_artifact as preparer


class AllBuildPublicationTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        self.incoming = self.root / 'incoming'
        self.incoming.mkdir()
        self.repository = 'owner/uniapp'
        self.revision = 'a' * 40

    def artifact(self, platform, signing='unsigned', operating_system=None):
        directory = self.incoming / (platform + '-' + (operating_system or signing))
        directory.mkdir()
        names = ['release/app-release.apk'] if platform == 'android' else [f'UniApp-{operating_system or platform}.zip']
        files = {}
        for name in names:
            path = directory / name
            path.parent.mkdir(exist_ok=True)
            path.write_bytes(name.encode())
            files[name] = hashlib.sha256(path.read_bytes()).hexdigest()
        context = dict(schema=1, sourceRepository=self.repository, sourceSha=self.revision, runId='123', runNumber='9',
                       platform=platform, versionName='2.0.14', versionCode=1009, signing=signing, files=files)
        if operating_system:
            context['os'] = operating_system
        (directory / 'context.json').write_text(json.dumps(context))
        return directory, context

    def collect(self):
        return collector.collect(self.incoming, self.root / 'plan', self.repository, self.revision, '123')

    def test_unsigned_android_has_one_release_apk_and_its_own_tag(self):
        self.artifact('android')
        plan = self.collect()
        self.assertEqual('android-v2.0.14+1009-unsigned', plan[0]['tag'])
        self.assertNotIn('app-debug.apk', [Path(name).name for name in plan[0]['assets']])
        self.assertIn('app-release.apk', [Path(name).name for name in plan[0]['assets']])

    def test_signed_android_retains_production_tag_and_publisher_layout(self):
        self.artifact('android', 'release-key')
        group = self.collect()[0]
        self.assertEqual('v2.0.14+1009', group['tag'])
        self.assertTrue((Path(group['notes']).parent / 'incoming/release/app-release.apk').is_file())

    def test_desktop_matrix_is_merged_into_one_release(self):
        for operating_system in ['linux', 'windows', 'macos']:
            self.artifact('desktop', operating_system=operating_system)
        group, = self.collect()
        self.assertEqual(3, len(group['inputs']))
        self.assertEqual(3, sum(Path(name).suffix == '.zip' for name in group['assets']))

    def test_legacy_signed_and_unsigned_android_publish_only_signed(self):
        self.artifact('android', 'release-key')
        self.artifact('android', 'unsigned')
        groups = self.collect()
        self.assertEqual({'v2.0.14+1009'}, {group['tag'] for group in groups})
        self.assertTrue(all(len(group['inputs']) == 1 for group in groups))

    def test_legacy_debug_apk_is_not_published(self):
        directory, context = self.artifact('android', 'release-key')
        (directory / 'debug').mkdir()
        (directory / 'debug/app-debug.apk').write_bytes(b'debug')
        context['files']['debug/app-debug.apk'] = hashlib.sha256(b'debug').hexdigest()
        (directory / 'context.json').write_text(json.dumps(context))
        group, = self.collect()
        self.assertNotIn('app-debug.apk', [Path(name).name for name in group['assets']])

    def test_platform_publish_cannot_collect_another_platform(self):
        self.artifact('android')
        with self.assertRaisesRegex(ValueError, 'selected platform'):
            collector.collect(self.incoming, self.root / 'plan', self.repository, self.revision, '123', 'ios')

    def test_multiple_platform_releases_are_rejected_before_network_calls(self):
        with patch.object(publisher, 'release_data') as remote:
            with self.assertRaisesRegex(ValueError, 'exactly one release'):
                publisher.publish([{'platform': 'android'}, {'platform': 'ios'}], 'owner/deploy',
                    self.root, self.root / 'policy', self.repository, self.revision, '123')
            remote.assert_not_called()

    def test_android_preparation_ignores_existing_debug_outputs(self):
        project = self.root / 'project'
        output = self.root / 'prepared'
        output.mkdir()
        for variant in ['release', 'debug']:
            directory = project / f'androidApp/build/outputs/apk/{variant}'
            directory.mkdir(parents=True)
            (directory / f'app-{variant}.apk').write_bytes(variant.encode())
            (directory / 'output-metadata.json').write_text(json.dumps({'elements': [
                {'versionName': '2.0.14', 'versionCode': 1009}]}))
        (project / '.sdk-binaries').mkdir()
        (project / '.sdk-binaries/resolved.properties').write_text('verified SDK versions')
        with patch.object(preparer, 'ROOT', project):
            context = preparer.android(output, 'unsigned')
        self.assertEqual(1009, context['versionCode'])
        self.assertTrue((output / 'release/app-release.apk').is_file())
        self.assertFalse((output / 'debug').exists())

    def test_desktop_guides_preserve_windows_and_unix_line_endings(self):
        for operating_system in ['linux', 'windows']:
            directory, context = self.artifact('desktop', operating_system=operating_system)
            guide = b'UniApp PC\r\n' if operating_system == 'windows' else b'UniApp PC\n'
            (directory / 'UniApp-PC.md').write_bytes(guide)
            context['files']['UniApp-PC.md'] = hashlib.sha256(guide).hexdigest()
            (directory / 'context.json').write_text(json.dumps(context))
        group, = self.collect()
        assets = {Path(name).name: Path(name) for name in group['assets']}
        self.assertEqual(b'UniApp PC\r\n', assets['UniApp-PC-windows.md'].read_bytes())
        self.assertEqual(b'UniApp PC\n', assets['UniApp-PC-linux.md'].read_bytes())

    def test_wrong_source_or_run_is_rejected(self):
        directory, context = self.artifact('ios')
        for key, value in [('sourceSha', 'b' * 40), ('runId', '124')]:
            wrong = dict(context, **{key: value})
            with self.assertRaisesRegex(ValueError, 'identity'):
                collector.validate(wrong, directory, self.repository, self.revision, '123')

    def test_single_artifact_extracted_directly_into_download_directory(self):
        directory, _ = self.artifact('ios')
        for path in directory.iterdir():
            shutil.move(str(path), self.incoming / path.name)
        directory.rmdir()
        group, = self.collect()
        self.assertEqual('ios-v2.0.14+1009-unsigned', group['tag'])
        self.assertIn('UniApp-ios.zip', [Path(name).name for name in group['assets']])

    def test_tampered_or_unlisted_files_are_rejected(self):
        directory, context = self.artifact('ios')
        (directory / 'UniApp-ios.zip').write_bytes(b'tampered')
        with self.assertRaisesRegex(ValueError, 'checksum'):
            collector.validate(context, directory, self.repository, self.revision, '123')
        (directory / 'extra.apk').write_bytes(b'extra')
        with self.assertRaisesRegex(ValueError, 'manifest'):
            collector.validate(context, directory, self.repository, self.revision, '123')

    def test_unsigned_packages_never_call_the_android_update_publisher(self):
        self.artifact('android')
        plan = self.collect()
        deploy = self.root / 'deploy'
        deploy.mkdir()
        with patch.object(publisher, 'release_data', return_value=None), patch.object(publisher, 'command') as command, \
                patch.object(publisher, 'publish_signed_android') as signed, patch.object(publisher.subprocess, 'run') as run:
            run.return_value.returncode = 0
            run.return_value.stdout = ''
            publisher.publish(plan, 'owner/deploy', deploy, self.root / 'policy', self.repository, self.revision, '123')
            signed.assert_not_called()
            args = next(call.args for call in command.call_args_list if call.args[:3] == ('gh', 'release', 'create'))
            self.assertIn('--prerelease', args)
            self.assertIn('--latest=false', args)
            self.assertFalse((deploy / 'update.json').exists())

    def test_concurrent_platform_publication_preserves_both_remote_records(self):
        remote = self.root / 'remote.git'
        deploy = self.root / 'deploy'
        competitor = self.root / 'competitor'
        real_run = subprocess.run
        def run(*args, cwd=self.root):
            return real_run(args, cwd=cwd, check=True, capture_output=True, text=True)
        with patch.dict(os.environ, {'GIT_CONFIG_GLOBAL': os.devnull, 'GIT_CONFIG_NOSYSTEM': '1'}):
            run('git', 'init', '--bare', '--initial-branch=main', str(remote))
            run('git', 'clone', str(remote), str(deploy))
            run('git', 'config', 'user.name', 'Test', cwd=deploy)
            run('git', 'config', 'user.email', 'test@example.invalid', cwd=deploy)
            for folder in ['release', 'docs']:
                (deploy / folder).mkdir()
                (deploy / folder / 'builds.json').write_text('[{"tag":"existing-desktop"}]\n')
            run('git', 'add', '.', cwd=deploy)
            run('git', 'commit', '-m', 'Initial index', cwd=deploy)
            run('git', 'push', 'origin', 'main', cwd=deploy)
            run('git', 'clone', str(remote), str(competitor))
            run('git', 'config', 'user.name', 'Other platform', cwd=competitor)
            run('git', 'config', 'user.email', 'other@example.invalid', cwd=competitor)
            interfered = False
            def race(args, **kwargs):
                nonlocal interfered
                if args == ['git', 'push', 'origin', 'main'] and kwargs.get('cwd') == deploy and not interfered:
                    interfered = True
                    for folder in ['release', 'docs']:
                        (competitor / folder / 'builds.json').write_text('[{"tag":"existing-desktop"},{"tag":"other-android"}]\n')
                    run('git', 'add', '.', cwd=competitor)
                    run('git', 'commit', '-m', 'Android index', cwd=competitor)
                    run('git', 'push', 'origin', 'main', cwd=competitor)
                if not kwargs.get('capture_output'):
                    kwargs.setdefault('stdout', subprocess.PIPE)
                    kwargs.setdefault('stderr', subprocess.PIPE)
                return real_run(args, **kwargs)
            group = dict(tag='new-ios', platform='ios', signing='unsigned', version='2.0.14')
            with patch.object(publisher.subprocess, 'run', side_effect=race):
                publisher.update_build_index([group], deploy, 'owner/deploy', self.revision, '123')
            records = json.loads(run('git', '--git-dir=' + str(remote), 'show', 'main:release/builds.json').stdout)
            self.assertTrue(interfered)
            self.assertEqual({'existing-desktop', 'other-android', 'new-ios'}, {record['tag'] for record in records})
            self.assertEqual((deploy / 'release/builds.json').read_text(), (deploy / 'docs/builds.json').read_text())
