import json
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from generate_update_manifest import generate
from release_metadata import read_apks


class ReleaseMetadataTest(unittest.TestCase):
    def setUp(self):
        self.directory = tempfile.TemporaryDirectory()
        self.root = Path(self.directory.name)
        self.addCleanup(self.directory.cleanup)
        self.metadata = self.root / 'output-metadata.json'
        self.build()

    def build(self, abis=('arm64-v8a',), filename='app-release.apk', version='2.0.1', code=201):
        with zipfile.ZipFile(self.root / 'app-release.apk', 'w') as apk:
            for abi in abis:
                apk.writestr(f'lib/{abi}/libexample.so', b'native test fixture')
        self.metadata.write_text(json.dumps({'elements': [{'outputFile': filename,
            'versionName': version, 'versionCode': code, 'filters': []}]}))

    def test_single_release_replaces_old_channels_and_preserves_ios_policy(self):
        existing = {'channels': {'stable': {'release': {'latestVersionCode': 10}},
            'beta': {'release': {'latestVersionCode': 199}}}, 'platforms': {'ios': {'latestVersionCode': 3}}}
        result = generate(self.metadata, {'mandatory': False}, existing, 'owner/repo', 'abc123', '2026-09-08')
        self.assertNotIn('channels', result)
        self.assertNotIn('beta', json.dumps(result))
        self.assertEqual({'ios': {'latestVersionCode': 3}}, result['platforms'])
        self.assertEqual(201, result['latestVersionCode'])
        self.assertIn('/v2.0.1%2B201/', result['downloadUrl'])
        self.assertEqual(64, len(result['sha256ByAbi']['universal']))

    def test_rejects_32_bit_and_all_x86_architectures(self):
        for abi in ('armeabi-v7a', 'x86', 'x86_64'):
            with self.subTest(abi=abi):
                self.build(abis=('arm64-v8a', abi))
                with self.assertRaisesRegex(ValueError, '64-bit libraries'):
                    read_apks(self.metadata)

    def test_rejects_missing_apk_files_and_path_traversal(self):
        for filename in ('missing.apk', '../app-release.apk'):
            self.build(filename=filename)
            with self.assertRaises(ValueError):
                read_apks(self.metadata)

    def test_rejects_version_suffix_and_invalid_build(self):
        for version, code in [('2.0.1-beta', 201), ('2.0.1', 0), ('2.0.1', True)]:
            self.build(version=version, code=code)
            with self.assertRaises(ValueError):
                read_apks(self.metadata)

    def test_rejects_downgrades_including_the_old_channel_schema(self):
        for existing in [{'latestVersionCode': 202}, {'channels': {'beta': {'release': {'latestVersionCode': 202}}}}]:
            with self.assertRaisesRegex(ValueError, 'newer published build'):
                generate(self.metadata, {}, existing, 'owner/repo', 'new-sha')

    def test_same_build_cannot_be_reused_for_different_code(self):
        existing = {'latestVersionCode': 201, 'buildCommit': 'old-sha'}
        with self.assertRaisesRegex(ValueError, 'Increment versionCode'):
            generate(self.metadata, {}, existing, 'owner/repo', 'new-sha')
        generate(self.metadata, {}, existing, 'owner/repo', 'old-sha')

    def test_rejects_invalid_minimum_build_and_multichannel_configuration(self):
        for policy in [{'minSupportedVersionCode': 202}, {'mandatory': 'false'}, {'beta': {}}]:
            with self.assertRaises(ValueError):
                generate(self.metadata, policy, {}, 'owner/repo', 'new-sha')


if __name__ == '__main__':
    unittest.main()
