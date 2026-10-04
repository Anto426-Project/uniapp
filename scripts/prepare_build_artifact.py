#!/usr/bin/env python3
"""Package every built variant with its exact source, run and file checksums."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import plistlib
import re
import shutil
import subprocess
import tarfile

ROOT = Path(__file__).resolve().parents[1]


def android(output, signing):
    metadata = ROOT / 'androidApp/build/outputs/apk/release/output-metadata.json'
    elements = json.loads(metadata.read_text())['elements']
    versions = {(item['versionName'], item['versionCode']) for item in elements}
    if len(versions) != 1:
        raise ValueError('Android release versions do not agree')
    version, build = versions.pop()
    for variant in ('release',):
        directory = ROOT / 'androidApp/build/outputs/apk' / variant
        apks = list(directory.glob('*.apk'))
        if not apks:
            raise ValueError(f'Missing Android {variant} APKs')
        target = output / variant
        target.mkdir()
        for path in [*apks, directory / 'output-metadata.json']:
            shutil.copy2(path, target / path.name)
    shutil.copy2(ROOT / '.sdk-binaries/resolved.properties', output / 'sdk-binaries.properties')
    return dict(platform='android', versionName=version, versionCode=build, signing=signing)


def ios(output):
    applications = list((ROOT / 'build/iosApp.xcarchive/Products/Applications').glob('*.app'))
    if len(applications) != 1:
        raise ValueError('Exactly one archived iOS app is required')
    info = plistlib.loads((applications[0] / 'Info.plist').read_bytes())
    version, build = str(info['CFBundleShortVersionString']), str(info['CFBundleVersion'])
    shutil.copy2(ROOT / 'build/iosApp-unsigned.ipa', output / f'UniApp-ios-{version}-{build}-unsigned.ipa')
    simulators = list((ROOT / 'build/ios-simulator/Build/Products').glob('*-iphonesimulator/*.app'))
    if len(simulators) != 1:
        raise ValueError('Exactly one simulator app is required')
    subprocess.run(['zip', '-qry', str((output / f'UniApp-ios-{version}-{build}-simulator.zip').resolve()), simulators[0].name],
                   cwd=simulators[0].parent, check=True)
    shutil.copy2(ROOT / '.sdk-binaries/resolved.properties', output / 'sdk-binaries.properties')
    return dict(platform='ios', versionName=version, versionCode=build, signing='unsigned')


def desktop(output, operating_system):
    base = ROOT / 'desktopApp/build/compose/binaries/main'
    version = '2.0.14-desktop.1'
    extension = {'linux': '.deb', 'windows': '.msi', 'macos': '.dmg'}[operating_system]
    installers = list(base.rglob('*' + extension))
    if not installers:
        raise ValueError(f'Missing {operating_system} installer')
    for path in installers:
        shutil.copy2(path, output / f'UniApp-{version}-{operating_system}{path.suffix}')
    app = base / 'app'
    if not app.is_dir() or not any(app.iterdir()):
        raise ValueError('Missing standalone desktop distribution')
    name = output / f'UniApp-{version}-{operating_system}-portable'
    if operating_system == 'windows':
        shutil.make_archive(str(name), 'zip', root_dir=app)
    else:
        with tarfile.open(str(name) + '.tar.gz', 'w:gz') as archive:
            for path in sorted(app.iterdir()):
                archive.add(path, arcname=path.name)
    shutil.copy2(ROOT / '.sdk-binaries/desktop-resolved.json', output / f'sdk-binaries-{operating_system}.json')
    shutil.copy2(ROOT / 'docs/desktop.md', output / 'UniApp-PC.md')
    return dict(platform='desktop', os=operating_system, versionName=version, versionCode=None, signing='unsigned')


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('platform', choices=['android', 'ios', 'desktop'])
    parser.add_argument('--output', type=Path, required=True)
    parser.add_argument('--os', choices=['linux', 'windows', 'macos'])
    parser.add_argument('--signing', choices=['unsigned', 'release-key'], default='unsigned')
    args = parser.parse_args()
    args.output.mkdir(parents=True, exist_ok=False)
    if args.platform == 'android':
        context = android(args.output, args.signing)
    elif args.platform == 'ios':
        context = ios(args.output)
    else:
        if not args.os:
            parser.error('desktop requires --os')
        context = desktop(args.output, args.os)
    context.update(schema=1, sourceRepository=os.environ['GITHUB_REPOSITORY'], sourceSha=os.environ['GITHUB_SHA'],
                   runId=os.environ['GITHUB_RUN_ID'], runNumber=os.environ['GITHUB_RUN_NUMBER'])
    if not re.fullmatch(r'[0-9a-f]{40}', context['sourceSha']):
        raise ValueError('Invalid source revision')
    context['files'] = {path.relative_to(args.output).as_posix(): hashlib.sha256(path.read_bytes()).hexdigest()
                        for path in sorted(args.output.rglob('*')) if path.is_file()}
    (args.output / 'context.json').write_text(json.dumps(context, indent=2) + '\n')


if __name__ == '__main__':
    main()
