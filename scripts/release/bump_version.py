#!/usr/bin/env python3
"""Prepare one Android release; never build or publish it automatically.

NOTE: versionCode is managed automatically by the CI runner (GITHUB_RUN_NUMBER).
This script only updates versionName in build.gradle.kts and release policy in
update-config.json. The versionCode in the compiled APK will always match the
GitHub Actions run number of the build workflow.
"""
from __future__ import annotations
import argparse
from datetime import date
from pathlib import Path
import re
from release_metadata import load_json, write_json


def plan_version(current_name: str, bump: str,
                 name: str | None = None) -> str:
    if not re.fullmatch(r'\d+\.\d+\.\d+', current_name):
        raise ValueError('Current version must be major.minor.patch')
    parts = [int(part) for part in current_name.split('.')]
    if bump != 'none':
        index = ['major', 'minor', 'patch'].index(bump)
        parts[index] += 1
        for trailing in range(index + 1, 3):
            parts[trailing] = 0
    next_name = name or '.'.join(map(str, parts))
    if not re.fullmatch(r'\d+\.\d+\.\d+', next_name):
        raise ValueError('New version must be major.minor.patch without a suffix')
    return next_name


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--file', type=Path, default=Path('androidApp/build.gradle.kts'))
    parser.add_argument('--ios-config', type=Path, default=Path('iosApp/Configuration/Config.xcconfig'))
    parser.add_argument('--update-config', type=Path, default=Path('update-config.json'))
    parser.add_argument('--changelog', type=Path, default=Path('CHANGELOG.md'))
    parser.add_argument('--version-name')
    parser.add_argument('--bump', choices=['patch', 'minor', 'major', 'none'], default='patch')
    parser.add_argument('--mandatory', choices=['true', 'false'])
    parser.add_argument('--min-supported-version')
    parser.add_argument('--min-supported-version-code', type=int)
    parser.add_argument('--sync-min-supported', action='store_true',
                        help='Set minSupportedVersionCode from update-config.json manually '
                             '(cannot use run number here; set an explicit --min-supported-version-code)')
    parser.add_argument('--note', action='append', default=[])
    parser.add_argument('--notes-file', type=Path)
    parser.add_argument('--skip-notes', action='store_true')
    parser.add_argument('--dry-run', action='store_true')
    parser.add_argument('--yes', '-y', action='store_true')
    args = parser.parse_args()
    source = args.file.read_text(encoding='utf-8')
    old_name = re.search(r'\bversionName\s*=\s*"([^"]+)"', source).group(1)
    name = plan_version(old_name, args.bump, args.version_name)
    policy = load_json(args.update_config)
    if any(key in policy for key in ('channels', 'shared', 'stable', 'beta')):
        raise ValueError('Use the flat, single-release update-config.json schema')
    if args.mandatory is not None:
        policy['mandatory'] = args.mandatory == 'true'
    if args.min_supported_version:
        policy['minSupportedVersion'] = args.min_supported_version
    if args.min_supported_version_code is not None:
        policy['minSupportedVersionCode'] = args.min_supported_version_code
    minimum = policy.get('minSupportedVersionCode', 1)
    if type(minimum) is not int or minimum <= 0:
        raise ValueError('minSupportedVersionCode must be a positive integer')
    notes = args.note + (args.notes_file.read_text(encoding='utf-8').splitlines() if args.notes_file else [])
    notes = [line.strip().removeprefix('- ').strip() for line in notes if line.strip()]
    if args.skip_notes:
        notes = []
    if notes:
        policy['notes'] = '\n'.join('- ' + line for line in notes)
    print(f'UniApp {old_name} -> {name}  (versionCode will be set by GITHUB_RUN_NUMBER at build time)')
    print(f'Minimum supported versionCode: {minimum}; mandatory: {policy.get("mandatory", False)}')
    if args.dry_run:
        return
    if not args.yes and input('Apply these changes? [y/N] ').strip().lower() != 'y':
        return
    updated = re.sub(r'(\bversionName\s*=\s*)"[^"]+"', lambda m: m[1] + f'"{name}"', source, count=1)
    # Validate and prepare all outputs before touching the files.
    old_changelog = args.changelog.read_text(encoding='utf-8') if args.changelog.exists() else ''
    entry = f'## [{name}] - {date.today().isoformat()} (build set by CI)\n\n' + '\n'.join('- ' + line for line in notes) + '\n\n'
    args.file.write_text(updated, encoding='utf-8')
    if args.ios_config.exists():
        ios_source = args.ios_config.read_text(encoding='utf-8')
        ios_updated = re.sub(r'(\bMARKETING_VERSION\s*=\s*)[^\r\n]+', lambda m: m[1] + f'{name}', ios_source)
        args.ios_config.write_text(ios_updated, encoding='utf-8')
    write_json(args.update_config, policy)
    if notes:
        args.changelog.write_text(entry + old_changelog, encoding='utf-8')


if __name__ == '__main__':
    main()
