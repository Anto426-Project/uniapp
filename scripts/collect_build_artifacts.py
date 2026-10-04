#!/usr/bin/env python3
"""Validate trusted workflow output and stage signed, unsigned and desktop releases."""
import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import re
import shutil


def validate(context, directory, repository, revision, run_id):
    if context.get('schema') != 1 or any(context.get(key) != expected for key, expected in
            [('sourceRepository', repository), ('sourceSha', revision), ('runId', run_id)]):
        raise ValueError('Artifact source/run identity does not match the selected build')
    if context.get('platform') not in ('android', 'ios', 'desktop') or context.get('signing') not in ('unsigned', 'release-key'):
        raise ValueError('Unsupported artifact platform/signing classification')
    if not re.fullmatch(r'\d+\.\d+\.\d+(?:-[A-Za-z0-9.-]+)?', context.get('versionName', '')) or not re.fullmatch(r'\d+', str(context.get('runNumber', ''))):
        raise ValueError('Invalid artifact version/build number')
    if context['platform'] == 'desktop' and context.get('os') not in ('linux', 'archlinux', 'windows', 'macos'):
        raise ValueError('Invalid desktop operating system')
    if context['platform'] != 'desktop' and not re.fullmatch(r'[1-9][0-9]*', str(context.get('versionCode', ''))):
        raise ValueError('Invalid artifact version code')
    if context['platform'] != 'android' and context['signing'] != 'unsigned':
        raise ValueError('Only the Android release pipeline may supply release-key signing')
    expected_files = set(context['files']) | {'context.json'}
    actual_files = {path.relative_to(directory).as_posix() for path in directory.rglob('*') if path.is_file()}
    if actual_files != expected_files or not context['files']:
        raise ValueError('Artifact contents do not match its file manifest')
    for name, checksum in context['files'].items():
        relative = PurePosixPath(name)
        path = (directory / name).resolve()
        if relative.is_absolute() or '..' in relative.parts or '\\' in name or not path.is_relative_to(directory.resolve()):
            raise ValueError('Unsafe artifact path')
        if hashlib.sha256(path.read_bytes()).hexdigest() != checksum:
            raise ValueError('Artifact checksum mismatch')
    if context['platform'] == 'android':
        if not any(name.startswith('release/') and name.endswith('.apk') for name in context['files']):
            raise ValueError('Android output must include a release APK')
    return context


def release_tag(context):
    version = context['versionName']
    if context['platform'] == 'android' and context['signing'] == 'release-key':
        return f'v{version}+{context["versionCode"]}'
    if context['platform'] == 'desktop':
        return f'desktop-v{version}+{context["runNumber"]}'
    return f'{context["platform"]}-v{version}+{context["versionCode"]}-unsigned'


def collect(incoming, output, repository, revision, run_id, platform=None):
    output.mkdir(parents=True, exist_ok=True)
    groups = {}
    # download-artifact extracts a single match directly into the destination.
    manifests = [incoming / 'context.json'] if (incoming / 'context.json').is_file() else sorted(incoming.glob('*/context.json'))
    verified = [(manifest, validate(json.loads(manifest.read_text()), manifest.parent, repository, revision, run_id))
                for manifest in manifests]
    platforms = {context['platform'] for _, context in verified}
    if len(platforms) > 1 or (platform and platforms != {platform}):
        raise ValueError('Build artifacts must belong to the selected platform only')
    if platforms == {'android'}:
        versions = {(context['versionName'], context['versionCode']) for _, context in verified}
        if len(versions) != 1:
            raise ValueError('Android artifacts disagree on the release version')
        # Historical runs may contain both variants. Publish only the signed release when present.
        signed = [item for item in verified if item[1]['signing'] == 'release-key']
        verified = signed or verified
        if len(verified) != 1:
            raise ValueError('Android publication requires exactly one release artifact')
    for manifest, context in verified:
        tag = release_tag(context)
        group = groups.setdefault(tag, {'tag': tag, 'platform': context['platform'], 'signing': context['signing'],
                                        'version': context['versionName'], 'inputs': [], 'assets': [], 'sourceSha': revision})
        if group['platform'] != context['platform'] or group['signing'] != context['signing']:
            raise ValueError('Conflicting release classifications')
        group['inputs'].append(str(manifest.parent.resolve()))
        directory = output / tag
        directory.mkdir(exist_ok=True)
        if context['platform'] == 'android' and context['signing'] == 'release-key':
            # Preserve the release/debug layout required by the production Android publisher.
            shutil.copytree(manifest.parent, directory / 'incoming', dirs_exist_ok=True)
        for name, checksum in context['files'].items():
            if (name.endswith('output-metadata.json') or Path(name).name.startswith('sdk-binaries') or
                    (context['platform'] == 'android' and name.startswith('debug/'))):
                continue
            asset_name = Path(name).name
            if context['platform'] == 'desktop' and asset_name == 'UniApp-PC.md':
                # Windows checkout may use CRLF; retain each verified guide byte-for-byte.
                asset_name = f'UniApp-PC-{context["os"]}.md'
            target = directory / asset_name
            if target.exists():
                if hashlib.sha256(target.read_bytes()).hexdigest() != checksum:
                    raise ValueError(f'Conflicting release asset names: {asset_name}')
            else:
                shutil.copy2(manifest.parent / name, target)
        shutil.copy2(manifest, directory / ('build-context-' + context.get('os', context['platform']) + '.json'))
    if not groups:
        raise ValueError('The selected run has no verified build artifacts to publish')
    if len(groups) != 1:
        raise ValueError('Publication requires exactly one version for the selected platform')
    for group in groups.values():
        directory = output / group['tag']
        assets = sorted(path for path in directory.iterdir() if path.is_file())
        sums = directory / 'SHA256SUMS.txt'
        sums.write_text(''.join(f'{hashlib.sha256(path.read_bytes()).hexdigest()}  {path.name}\n' for path in assets))
        group['assets'] = [str(path.resolve()) for path in [*assets, sums]]
        notes = directory / 'release-notes.md'
        notes.write_text(f'UniApp {group["version"]} — {group["platform"]}\n\n'
            f'Firma: {group["signing"]}.\n\n'
            + ('Gli APK release unsigned richiedono una firma prima dell’installazione.\n\n' if group['platform'] == 'android' and group['signing'] == 'unsigned' else '')
            + ('IPA iOS non firmata: richiede firma/provisioning per l’installazione su un dispositivo. Incluso anche il bundle per simulatore.\n\n' if group['platform'] == 'ios' else '')
            + ('Pacchetti desktop non firmati. Le guide UniApp-PC-<sistema>.md allegate descrivono installazione e funzioni disponibili, incluso Arch Linux.\n\n' if group['platform'] == 'desktop' else '')
            + f'Sorgenti: https://github.com/{repository}/commit/{revision}\n\nBuild: https://github.com/{repository}/actions/runs/{run_id}\n')
        group['notes'] = str(notes.resolve())
    (output / 'plan.json').write_text(json.dumps(list(groups.values()), indent=2) + '\n')
    return list(groups.values())


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--incoming', type=Path, required=True)
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--repository', required=True)
    parser.add_argument('--revision', required=True)
    parser.add_argument('--run-id', required=True)
    parser.add_argument('--platform', choices=['android', 'ios', 'desktop'], required=True)
    args = parser.parse_args()
    collect(args.incoming, args.output, args.repository, args.revision, args.run_id, args.platform)


if __name__ == '__main__':
    main()
