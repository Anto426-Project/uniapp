import hashlib
import json
from pathlib import Path
import sys
import tempfile
import unittest

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
from build_desktop_sdks import SDKS
from desktop_sdk_metadata import verify


class DesktopSdkMetadataTest(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.directory = Path(self.temporary.name)
        self.versions = {alias: "1.0.0-desktop.1" for alias, _ in SDKS.values()}
        self.coordinates = {name: f"example:{name}" for name in SDKS}
        self.data = {"modules": [], "sha256": {}}
        for name in SDKS:
            version = "1.0.0-desktop.1"
            self.data["modules"].append({"name": name, "version": version,
                "revision": "a" * 40, "sourceSha256": "b" * 64, "hasLocalChanges": True})
            for suffix, extensions in [("", ("module", "pom")), ("-desktop", ("module", "pom", "jar"))]:
                artifact = name + suffix
                for extension in extensions:
                    relative = f"maven/example/{artifact}/{version}/{artifact}-{version}.{extension}"
                    path = self.directory / relative
                    path.parent.mkdir(parents=True, exist_ok=True)
                    path.write_bytes(b"verified fixture")
                    self.data["sha256"][relative] = hashlib.sha256(path.read_bytes()).hexdigest()
        self.save()

    def save(self):
        (self.directory / "desktop-resolved.json").write_text(json.dumps(self.data))

    def test_valid_manifest_preserves_source_provenance(self):
        self.assertEqual(self.data["modules"], verify(self.directory, self.versions, self.coordinates))

    def test_changed_binary_is_rejected(self):
        (self.directory / next(iter(self.data["sha256"]))).write_bytes(b"tampered")
        with self.assertRaisesRegex(ValueError, "verification failed"):
            verify(self.directory, self.versions, self.coordinates)

    def test_wrong_catalog_version_is_rejected(self):
        self.versions[SDKS["uni-sdk"][0]] = "1.0.99-desktop.1"
        with self.assertRaisesRegex(ValueError, "catalog"):
            verify(self.directory, self.versions, self.coordinates)

    def test_incomplete_binary_checksums_are_rejected(self):
        key = next(name for name in self.data["sha256"] if name.endswith(".jar"))
        del self.data["sha256"][key]
        self.save()
        with self.assertRaisesRegex(ValueError, "Missing required"):
            verify(self.directory, self.versions, self.coordinates)

    def test_checksum_path_cannot_escape_the_binary_directory(self):
        self.data["sha256"]["../outside.jar"] = "a" * 64
        self.save()
        with self.assertRaisesRegex(ValueError, "verification failed"):
            verify(self.directory, self.versions, self.coordinates)
