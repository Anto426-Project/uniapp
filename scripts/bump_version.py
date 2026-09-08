#!/usr/bin/env python3
"""Prepare one Android release; never build or publish it automatically."""
from __future__ import annotations
import argparse
from datetime import date
from pathlib import Path
import re
from release_metadata import load_json, write_json


def plan_version(current_name: str, current_code: int, bump: str,
                 name: str | None = None, code: int | None = None) -> tuple[str, int]:
    if not re.fullmatch(r'\d+\.\d+\.\d+', current_name):
        raise ValueError('Current version must be major.minor.patch')
    parts = [int(part) for part in current_name.split('.')]
    if bump != 'none':
        index = ['major', 'minor', 'patch'].index(bump)
        parts[index] += 1
        for trailing in range(index + 1, 3):
            parts[trailing] = 0
    next_name = name or '.'.join(map(str, parts))
    next_code = code if code is not None else current_code + 1
    if not re.fullmatch(r'\d+\.\d+\.\d+', next_name):
        raise ValueError('New version must be major.minor.patch without a suffix')
    if next_code <= current_code:
        raise ValueError('versionCode must increase')
    return next_name, next_code


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--file', type=Path, default=Path('androidApp/build.gradle.kts'))
    parser.add_argument('--update-config', type=Path, default=Path('update-config.json'))
    parser.add_argument('--changelog', type=Path, default=Path('CHANGELOG.md'))
    parser.add_argument('--version-name')
    parser.add_argument('--version-code', type=int)
    parser.add_argument('--bump', choices=['patch', 'minor', 'major', 'none'], default='patch')
    parser.add_argument('--mandatory', choices=['true', 'false'])
    parser.add_argument('--min-supported-version')
    parser.add_argument('--min-supported-version-code', type=int)
    parser.add_argument('--sync-min-supported', action='store_true')
    parser.add_argument('--note', action='append', default=[])
    parser.add_argument('--notes-file', type=Path)
    parser.add_argument('--skip-notes', action='store_true')
    parser.add_argument('--dry-run', action='store_true')
    parser.add_argument('--yes', '-y', action='store_true')
    args = parser.parse_args()
    source = args.file.read_text(encoding='utf-8')
    old_name = re.search(r'\bversionName\s*=\s*"([^"]+)"', source).group(1)
    old_code = int(re.search(r'\bversionCode\s*=\s*(\d+)', source).group(1))
    name, code = plan_version(old_name, old_code, args.bump, args.version_name, args.version_code)
    policy = load_json(args.update_config)
    if any(key in policy for key in ('channels', 'shared', 'stable', 'beta')):
        raise ValueError('Use the flat, single-release update-config.json schema')
    if args.mandatory is not None:
        policy['mandatory'] = args.mandatory == 'true'
    if args.sync_min_supported:
        policy.update(minSupportedVersion=name, minSupportedVersionCode=code)
    if args.min_supported_version:
        policy['minSupportedVersion'] = args.min_supported_version
    if args.min_supported_version_code is not None:
        policy['minSupportedVersionCode'] = args.min_supported_version_code
    minimum = policy.get('minSupportedVersionCode', 1)
    if type(minimum) is not int or not 0 < minimum <= code:
        raise ValueError('Invalid minimum supported build')
    notes = args.note + (args.notes_file.read_text(encoding='utf-8').splitlines() if args.notes_file else [])
    notes = [line.strip().removeprefix('- ').strip() for line in notes if line.strip()]
    if args.skip_notes:
        notes = []
    if notes:
        policy['notes'] = '\n'.join('- ' + line for line in notes)
    print(f'UniApp {old_name} ({old_code}) -> {name} ({code})')
    print(f'Minimum build: {minimum}; mandatory: {policy.get("mandatory", False)}')
    if args.dry_run:
        return
    if not args.yes and input('Apply these changes? [y/N] ').strip().lower() != 'y':
        return
    updated = re.sub(r'(\bversionName\s*=\s*)"[^"]+"', lambda m: m[1] + f'"{name}"', source, count=1)
    updated = re.sub(r'(\bversionCode\s*=\s*)\d+', lambda m: m[1] + str(code), updated, count=1)
    # Validate and prepare all outputs before touching the files.
    old_changelog = args.changelog.read_text(encoding='utf-8') if args.changelog.exists() else ''
    entry = f'## [{name}] - {date.today().isoformat()} (build {code})\n\n' + '\n'.join('- ' + line for line in notes) + '\n\n'
    args.file.write_text(updated, encoding='utf-8')
    write_json(args.update_config, policy)
    if notes:
        args.changelog.write_text(entry + old_changelog, encoding='utf-8')


if __name__ == '__main__':
    main()
