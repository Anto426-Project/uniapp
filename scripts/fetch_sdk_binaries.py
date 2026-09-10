#!/usr/bin/env python3
"""Resolve SDK releases once, verify them, and install their Maven binaries atomically."""
from __future__ import annotations
import argparse
import hashlib
import io
import json
import os
from pathlib import Path, PurePosixPath
import re
import shutil
import tempfile
from urllib.error import HTTPError
from urllib.parse import urlsplit
from urllib.request import HTTPRedirectHandler, Request, build_opener
import zipfile

ROOT = Path(__file__).resolve().parents[1]


class GitHubRedirectHandler(HTTPRedirectHandler):
    """Keep credentials on GitHub's API, never on redirected asset storage."""

    def redirect_request(self, req, fp, code, msg, headers, newurl):
        redirected = super().redirect_request(req, fp, code, msg, headers, newurl)
        if redirected is not None and urlsplit(newurl)[:2] != urlsplit(req.full_url)[:2]:
            redirected.remove_header('Authorization')
        return redirected


def download(url: str, *, api: bool = False) -> bytes:
    headers = {'User-Agent': 'UniApp-SDK-resolver', 'Accept': 'application/vnd.github+json' if api else 'application/octet-stream'}
    token = os.environ.get('GITHUB_TOKEN') or os.environ.get('GH_TOKEN')
    if urlsplit(url).scheme == 'https' and urlsplit(url).netloc == 'api.github.com' and token:
        headers['Authorization'] = 'Bearer ' + token
    with build_opener(GitHubRedirectHandler()).open(Request(url, headers=headers), timeout=120) as response:
        data = response.read(256 * 1024 * 1024 + 1)
    if len(data) > 256 * 1024 * 1024:
        raise ValueError('SDK archive exceeds the size limit')
    return data


def validate_archive(path: Path, expected: dict, target: Path) -> dict:
    with zipfile.ZipFile(path) as archive:
        entries = archive.infolist()
        if len(entries) > 10000 or sum(item.file_size for item in entries) > 512 * 1024 * 1024:
            raise ValueError('SDK archive exceeds extraction limits')
        names = [item.filename for item in entries]
        if len(names) != len(set(names)):
            raise ValueError('Duplicate SDK archive entry')
        info = json.loads(archive.read('sdk-info.json'))
        if info['sourceRepository'] != expected['repository'] or info['coordinate'] != expected['coordinate']:
            raise ValueError('SDK identity does not match the requested module')
        if not re.fullmatch(r'[a-f0-9]{40}', info['sourceSha']) or not re.fullmatch(r'[0-9A-Za-z.-]+', info['version']):
            raise ValueError('Invalid SDK version or source revision')
        files = info['files']
        if set(names) != {'sdk-info.json', *('maven/' + name for name in files)}:
            raise ValueError('SDK archive contents do not match its manifest')
        for name, checksum in files.items():
            relative = PurePosixPath(name)
            if relative.is_absolute() or '..' in relative.parts or '\\' in name or not relative.parts:
                raise ValueError('Unsafe SDK archive path')
            data = archive.read('maven/' + name)
            if hashlib.sha256(data).hexdigest() != checksum:
                raise ValueError('SDK file checksum mismatch')
            destination = target.joinpath(*relative.parts)
            if destination.exists() and destination.read_bytes() != data:
                raise ValueError('Conflicting SDK Maven coordinates')
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(data)
        return info


def resolve_release(name: str, spec: dict) -> Path:
    api_root = f"https://api.github.com/repos/{spec['repository']}"
    try:
        release = json.loads(download(f"{api_root}/releases/latest", api=True))
    except HTTPError as error:
        if error.code in (401, 403, 404):
            raise RuntimeError(
                f"{name}: GitHub returned HTTP {error.code} for {spec['repository']}. "
                "Check that a published SDK release exists and the SDK_READ_TOKEN or DEPLOY_TOKEN "
                "used by this workflow has Contents: read access to every SDK repository. "
                "UniApp's GITHUB_TOKEN alone cannot read other private repositories."
            ) from None
        raise
    # Private releases require the asset API and authentication; their browser URLs return 404.
    assets = {item['name']: f"{api_root}/releases/assets/{int(item['id'])}" for item in release['assets']}
    if not {'maven-repository.zip', 'maven-repository.zip.sha256'} <= assets.keys():
        raise ValueError(f"{name}: the latest SDK release has no complete Maven binaries; run its SDK workflow first")
    checksum = download(assets['maven-repository.zip.sha256']).decode().split()[0]
    if not re.fullmatch(r'[a-f0-9]{64}', checksum):
        raise ValueError('Invalid SDK archive checksum')
    cache = ROOT / '.sdk-downloads' / (checksum + '.zip')
    if not cache.exists() or hashlib.sha256(cache.read_bytes()).hexdigest() != checksum:
        data = download(assets['maven-repository.zip'])
        if hashlib.sha256(data).hexdigest() != checksum:
            raise ValueError('Downloaded SDK checksum mismatch')
        cache.parent.mkdir(parents=True, exist_ok=True)
        cache.write_bytes(data)
    return cache


def validate_android_class_names(repository: Path) -> None:
    """Independent R8 passes must produce class names unique across all SDKs."""
    owners = {}
    for path in sorted(repository.rglob('*.aar')):
        with zipfile.ZipFile(path) as aar:
            for jar_name in aar.namelist():
                if jar_name != 'classes.jar' and not (jar_name.startswith('libs/') and jar_name.endswith('.jar')):
                    continue
                with zipfile.ZipFile(io.BytesIO(aar.read(jar_name))) as jar:
                    for name in jar.namelist():
                        if not name.endswith('.class') or name == 'module-info.class':
                            continue
                        owner = f'{path.name}!{jar_name}'
                        if name in owners:
                            raise ValueError(f'Duplicate Android class {name}: {owners[name]} and {owner}. '
                                             'Rebuild the SDKs with distinct R8 repackaging namespaces.')
                        owners[name] = owner


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--local', type=Path, action='append', default=[], help='Use locally validated SDK archives instead of GitHub releases')
    args = parser.parse_args()
    specs = json.loads((ROOT / 'scripts/sdk_binaries.json').read_text())
    local = {}
    for path in args.local:
        with zipfile.ZipFile(path) as archive:
            local[json.loads(archive.read('sdk-info.json'))['sourceRepository']] = path
    stage = Path(tempfile.mkdtemp(prefix='.sdk-binaries-stage-', dir=ROOT))
    try:
        properties = []
        for name, spec in specs.items():
            archive = local.get(spec['repository']) if args.local else resolve_release(name, spec)
            if archive is None:
                raise ValueError(f'Missing local SDK archive: {name}')
            info = validate_archive(archive, spec, stage / 'maven')
            properties += [f'{name}.version={info["version"]}', f'{name}.revision={info["sourceSha"]}', f'{name}.coordinate={info["coordinate"]}']
            print(f'{name}: {info["version"]} ({info["sourceSha"][:12]})')
        validate_android_class_names(stage / 'maven')
        (stage / 'resolved.properties').write_text('\n'.join(properties) + '\n')
        destination = ROOT / '.sdk-binaries'
        backup = ROOT / '.sdk-binaries-previous'
        if backup.exists():
            raise ValueError('An interrupted SDK installation needs review: .sdk-binaries-previous')
        if destination.exists():
            destination.rename(backup)
        try:
            stage.rename(destination)
        except BaseException:
            if backup.exists():
                backup.rename(destination)
            raise
        shutil.rmtree(backup, ignore_errors=True)
    finally:
        shutil.rmtree(stage, ignore_errors=True)


if __name__ == '__main__':
    main()
