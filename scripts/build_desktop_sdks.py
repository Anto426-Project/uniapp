#!/usr/bin/env python3
"""Build independently versioned desktop Maven binaries; never installs composite builds."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import tomllib

ROOT = Path(__file__).resolve().parents[1]
SDKS = {
    "liquid-monet": ("desktop-liquid-monet-sdk", ":sdk:"),
    "uni-sdk": ("desktop-unisdk", ":"),
    "secure-storage-sdk": ("desktop-secure-storage-sdk", ":"),
    "firebase-connector-sdk": ("desktop-firebase-connector-sdk", ":"),
}

def git(repo, *args):
    return subprocess.check_output(["git", "-C", str(repo), *args], text=True).strip()

def source_digest(repo):
    names = subprocess.check_output(["git", "-C", str(repo), "ls-files", "-z", "--cached", "--others", "--exclude-standard"]).split(b"\0")
    result = hashlib.sha256()
    for name in sorted(set(names)):
        if not name:
            continue
        path = repo / os.fsdecode(name)
        if path.is_file():
            result.update(name + b"\0" + hashlib.sha256(path.read_bytes()).digest())
    return result.hexdigest()

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--sdk-root", type=Path, default=ROOT.parent)
    parser.add_argument("--output", type=Path, default=ROOT / ".sdk-binaries")
    args = parser.parse_args()
    versions = tomllib.loads((ROOT / "gradle/libs.versions.toml").read_text())["versions"]
    destination = args.output.resolve()
    destination.mkdir(parents=True, exist_ok=True)
    manifest = {"modules": [], "sha256": {}}
    for name, (alias, prefix) in SDKS.items():
        repo = args.sdk_root.resolve() / name
        if not (repo / "gradlew").is_file():
            raise SystemExit(f"Missing SDK checkout: {repo}")
        version = versions[alias]
        revision = git(repo, "rev-parse", "HEAD")
        digest = source_digest(repo)
        launcher = repo / ("gradlew.bat" if os.name == "nt" else "gradlew")
        subprocess.run([str(launcher), "-p", str(repo),
            f"{prefix}publishDesktopPublicationToStagingRepository",
            f"{prefix}publishKotlinMultiplatformPublicationToStagingRepository",
            f"-PsdkVersion={version}", "--no-configuration-cache", "--no-daemon", "--max-workers=2"], check=True)
        if digest != source_digest(repo):
            raise SystemExit(f"{name} sources changed during compilation; run again")
        staging = repo / "build/maven-repository"
        selected = [path for path in staging.rglob("*") if path.is_file() and path.parent.name == version]
        if not any(path.suffix == ".jar" and "desktop" in path.name for path in selected):
            raise SystemExit(f"Missing desktop binary for {name} {version}")
        for path in selected:
            relative = Path("maven") / path.relative_to(staging)
            target = destination / relative
            target.parent.mkdir(parents=True, exist_ok=True)
            with tempfile.NamedTemporaryFile(dir=target.parent, delete=False) as handle:
                temporary = Path(handle.name)
                handle.write(path.read_bytes())
            os.replace(temporary, target)
            manifest["sha256"][relative.as_posix()] = hashlib.sha256(target.read_bytes()).hexdigest()
        manifest["modules"].append({"name": name, "version": version, "revision": revision,
            "hasLocalChanges": bool(git(repo, "status", "--porcelain")), "sourceSha256": digest})
    temporary = destination / "desktop-resolved.json.tmp"
    temporary.write_text(json.dumps(manifest, indent=2) + "\n")
    os.replace(temporary, destination / "desktop-resolved.json")
    print("Desktop SDK binaries installed with exact versions, checksums and source provenance.")

if __name__ == "__main__":
    main()
