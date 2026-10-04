#!/usr/bin/env python3
"""Stage the self-contained desktop app for native Arch Linux makepkg packaging."""
import argparse
import hashlib
import os
from pathlib import Path
import re
import shutil
import tarfile

ROOT = Path(__file__).resolve().parents[1]


def stage_package(output, release):
    if not re.fullmatch(r'[1-9][0-9]*', str(release)):
        raise ValueError('Arch Linux package release must be a positive integer')
    app = ROOT / 'desktopApp/build/compose/binaries/main/app/UniApp'
    for name in ['bin/UniApp', 'lib/UniApp.png', 'lib/runtime/release']:
        if not (app / name).is_file():
            raise ValueError(f'Missing desktop distribution file: {name}; run :desktopApp:createDistributable')
    output.mkdir(parents=True, exist_ok=False)
    archive = output / 'UniApp.tar.gz'
    with tarfile.open(archive, 'w:gz') as handle:
        handle.add(app, arcname='UniApp')
    desktop = output / 'uniapp.desktop'
    shutil.copy2(ROOT / 'packaging/archlinux/uniapp.desktop', desktop)
    template = (ROOT / 'packaging/archlinux/PKGBUILD.in').read_text()
    for placeholder, value in [('PKGREL', str(release)), ('APP_SHA256', hashlib.sha256(archive.read_bytes()).hexdigest()),
                               ('DESKTOP_SHA256', hashlib.sha256(desktop.read_bytes()).hexdigest())]:
        template = template.replace('@' + placeholder + '@', value)
    (output / 'PKGBUILD').write_text(template)
    return output


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--output', type=Path, default=ROOT / 'desktopApp/build/compose/binaries/main/archlinux')
    args = parser.parse_args()
    stage_package(args.output.resolve(), os.environ.get('GITHUB_RUN_NUMBER', '1'))


if __name__ == '__main__':
    main()
