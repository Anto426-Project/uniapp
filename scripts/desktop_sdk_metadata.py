#!/usr/bin/env python3
"""Verify the desktop SDK binary manifest and emit bundled module metadata."""
import hashlib
import json
from pathlib import Path
import re
import tomllib
from build_desktop_sdks import SDKS
ROOT = Path(__file__).resolve().parents[1]


def verify(directory, versions, coordinates):
    manifest = directory / "desktop-resolved.json"
    if not manifest.is_file():
        raise ValueError("Desktop SDK binaries are missing. Run: python3 scripts/build_desktop_sdks.py")
    data = json.loads(manifest.read_text())
    modules = data["modules"]
    if {module["name"] for module in modules} != set(SDKS) or len(modules) != len(SDKS):
        raise ValueError("Invalid desktop SDK module manifest")
    checksums = data["sha256"]
    for module in modules:
        version = module["version"]
        if (version != versions[SDKS[module["name"]][0]] or
                not re.fullmatch(r"[0-9a-f]{40}", module["revision"]) or
                not re.fullmatch(r"[0-9a-f]{64}", module["sourceSha256"]) or
                not isinstance(module["hasLocalChanges"], bool)):
            raise ValueError("Desktop SDK binaries do not match the catalog; rebuild them")
        group, artifact = coordinates[module["name"]].split(":")
        for suffix, extensions in [("", ("module", "pom")), ("-desktop", ("module", "pom", "jar"))]:
            name = artifact + suffix
            for extension in extensions:
                expected = f"maven/{group.replace('.', '/')}/{name}/{version}/{name}-{version}.{extension}"
                if expected not in checksums:
                    raise ValueError(f"Missing required desktop SDK checksum: {expected}")
    for name, expected in checksums.items():
        path = (directory / name).resolve()
        if (not path.is_relative_to(directory.resolve()) or not path.is_file() or
                hashlib.sha256(path.read_bytes()).hexdigest() != expected):
            raise ValueError(f"Desktop SDK binary verification failed: {name}")
    return modules


def main():
    versions = tomllib.loads((ROOT / "gradle/libs.versions.toml").read_text())["versions"]
    config = json.loads((ROOT / "scripts/sdk_binaries.json").read_text())
    modules = verify(ROOT / ".sdk-binaries", versions, {key: value["coordinate"] for key, value in config.items()})
    entries = ",\n".join("AppModuleInfo(%s, %s, %s, %s)" % (
        json.dumps(module["name"]), json.dumps(module["version"]), json.dumps(module["revision"]),
        str(module["hasLocalChanges"]).lower()) for module in modules)
    print("package com.anto426.uniapp.app.info\nimport com.anto426.unisdk.platform.AppModuleInfo\ninternal object DesktopBuildMetadata { val modules = listOf(" + entries + ") }")


if __name__ == "__main__":
    try:
        main()
    except (ValueError, KeyError) as error:
        raise SystemExit(str(error)) from error
