import hashlib
import json
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import collect_build_artifacts as collector
import publish_builds as publisher


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
        names = ['release/app-release.apk', 'debug/app-debug.apk'] if platform == 'android' else [f'UniApp-{operating_system or platform}.zip']
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

    def test_unsigned_android_includes_both_variants_and_has_its_own_tag(self):
        self.artifact('android')
        plan = self.collect()
        self.assertEqual('android-v2.0.14+1009-unsigned', plan[0]['tag'])
        self.assertIn('app-debug.apk', [Path(name).name for name in plan[0]['assets']])
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

    def test_signed_and_unsigned_android_from_same_run_are_both_published(self):
        self.artifact('android', 'release-key')
        self.artifact('android', 'unsigned')
        groups = self.collect()
        self.assertEqual({'v2.0.14+1009', 'android-v2.0.14+1009-unsigned'}, {group['tag'] for group in groups})
        self.assertTrue(all(len(group['inputs']) == 1 for group in groups))

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
            publisher.publish(plan, 'owner/deploy', deploy, self.root / 'policy', self.repository, self.revision, '123')
            signed.assert_not_called()
            args = next(call.args for call in command.call_args_list if call.args[:3] == ('gh', 'release', 'create'))
            self.assertIn('--prerelease', args)
            self.assertIn('--latest=false', args)
            self.assertFalse((deploy / 'update.json').exists())
