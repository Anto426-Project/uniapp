"""Shared validation for the single Android release and its APK artifacts."""
from __future__ import annotations

import hashlib
import json
import re
import zipfile
from pathlib import Path
from urllib.parse import quote

SUPPORTED_ABIS = {"arm64-v8a"}


def load_json(path: Path | None) -> dict:
    if path is None or not path.exists():
        return {}
    value = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(value, dict):
        raise ValueError(f"Expected a JSON object: {path}")
    return value


def write_json(path: Path, value: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def read_apks(metadata_path: Path) -> dict:
    metadata = load_json(metadata_path)
    elements = metadata.get("elements", [])
    if not elements:
        raise ValueError("No APK output metadata")
    versions = {(e.get("versionName"), e.get("versionCode")) for e in elements}
    if len(versions) != 1:
        raise ValueError("All APKs must have the same version")
    name, code = versions.pop()
    if not isinstance(name, str) or not re.fullmatch(r"\d+\.\d+\.\d+", name):
        raise ValueError("Release version must use major.minor.patch without a prerelease suffix")
    if type(code) is not int or code <= 0:
        raise ValueError("versionCode must be a positive integer")
    files = {}
    hashes = {}
    for element in elements:
        filename = element.get("outputFile", "")
        if not re.fullmatch(r"[A-Za-z0-9][A-Za-z0-9._-]*\.apk", filename):
            raise ValueError("Invalid APK filename")
        path = metadata_path.parent / filename
        if not path.is_file() or path.is_symlink():
            raise ValueError(f"Missing regular APK file: {filename}")
        abi_filters = [f for f in element.get("filters", []) if f.get("filterType") == "ABI"]
        abi = abi_filters[0]["value"] if abi_filters else "arm64-v8a"
        if abi not in SUPPORTED_ABIS:
            raise ValueError(f"Unsupported APK architecture: {abi}")
        with zipfile.ZipFile(path) as apk:
            native_abis = {entry.split('/')[1] for entry in apk.namelist()
                           if entry.startswith('lib/') and entry.endswith('.so')}
        if not native_abis or not native_abis <= SUPPORTED_ABIS:
            raise ValueError(f"APK must contain only supported 64-bit libraries: {filename}")
        if native_abis != {abi}:
            raise ValueError(f"APK native libraries do not match its ABI metadata: {filename}")
        if abi in files or filename in files.values():
            raise ValueError("Duplicate APK architecture or filename")
        files[abi] = filename
        with path.open("rb") as handle:
            hashes[filename] = hashlib.file_digest(handle, "sha256").hexdigest()
    if "arm64-v8a" not in files:
        raise ValueError("An ARM64 APK is required")
    return {"versionName": name, "versionCode": code, "files": files, "sha256": hashes,
            "tag": f"v{name}+{code}"}


def asset_url(repo: str, tag: str, filename: str) -> str:
    if not re.fullmatch(r"[A-Za-z0-9_.-]+/[A-Za-z0-9_.-]+", repo):
        raise ValueError("Invalid GitHub repository")
    return f"https://github.com/{repo}/releases/download/{quote(tag, safe='')}/{quote(filename, safe='')}"


def published_releases(manifest: dict) -> list[dict]:
    """Read the previous schema only to prevent a downgrade during migration."""
    result = [manifest]
    for channel in manifest.get("channels", {}).values():
        if isinstance(channel, dict):
            result.append(channel.get("release", channel))
    return result
