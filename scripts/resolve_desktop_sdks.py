#!/usr/bin/env python3
"""Compile pinned desktop SDK sources into a private, local Maven staging tree."""
import json
import os
from pathlib import Path
import re
import subprocess
import sys

ROOT = Path(__file__).resolve().parents[1]


def build_pinned_sdks(destination, specs):
    locks = json.loads((ROOT / 'scripts/desktop_sdk_sources.json').read_text())
    if locks.keys() != specs.keys():
        raise ValueError('Desktop SDK source lock does not match the dependency catalog')
    for name, lock in locks.items():
        if lock['version'] != specs[name]['version'] or not re.fullmatch(r'[a-f0-9]{40}', lock['revision']):
            raise ValueError(f'{name}: desktop SDK source lock does not match the catalog version')
    sources = ROOT / '.sdk-sources'
    sources.mkdir(exist_ok=True)
    environment = os.environ.copy()
    token = environment.get('GITHUB_TOKEN') or environment.get('GH_TOKEN')
    if token:
        environment['GH_TOKEN'] = token
    # The credential helper reads the token from the environment, never command arguments.
    git_command = ['git', '-c', 'credential.helper=', '-c', 'credential.helper=!gh auth git-credential']
    for name, lock in locks.items():
        checkout = sources / name
        if not checkout.exists():
            subprocess.run([*git_command, 'clone', '--filter=blob:none', '--no-checkout',
                            f'https://github.com/{specs[name]["repository"]}.git', str(checkout)],
                           env=environment, check=True)
        else:
            status = subprocess.check_output(['git', '-C', str(checkout), 'status', '--porcelain'], text=True)
            if status.strip():
                raise ValueError(f'{name}: cached SDK checkout has changes; preserve or remove them before rebuilding')
        subprocess.run([*git_command, '-C', str(checkout), 'fetch', 'origin', lock['revision']], env=environment, check=True)
        subprocess.run([*git_command, '-C', str(checkout), 'checkout', '--detach', lock['revision']], env=environment, check=True)
        revision = subprocess.check_output(['git', '-C', str(checkout), 'rev-parse', 'HEAD'], text=True).strip()
        if revision != lock['revision']:
            raise ValueError(f'{name}: incorrect desktop SDK source revision')
    subprocess.run([sys.executable, str(ROOT / 'scripts/build_desktop_sdks.py'),
                    '--sdk-root', str(sources), '--output', str(destination)], check=True)
