#!/usr/bin/env python3
"""Validate app resource XML and Kotlin resource references before compiling."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def validate() -> list[str]:
    errors = []
    resource_root = ROOT / 'composeApp/src/commonMain/composeResources'
    base_path = resource_root / 'values/strings.xml'
    base_keys = set()
    for path in sorted(resource_root.glob('values*/strings.xml')):
        try:
            elements = ET.parse(path).getroot().findall('string')
        except ET.ParseError as error:
            errors.append(f'{path.relative_to(ROOT)}: {error}')
            continue
        keys = set()
        for element in elements:
            key = element.get('name', '')
            if not re.fullmatch(r'[a-z][a-z0-9_]*', key):
                errors.append(f'{path.relative_to(ROOT)}: invalid resource name {key!r}')
            if key in keys:
                errors.append(f'{path.relative_to(ROOT)}: duplicate resource {key}')
            keys.add(key)
        if path == base_path:
            base_keys = keys
    if not base_keys:
        errors.append('The base string catalogue is missing or empty')
    for path in (ROOT / 'composeApp/src').rglob('*.kt'):
        for key in set(re.findall(r'\bRes\.string\.([a-zA-Z0-9_]+)', path.read_text(encoding='utf-8'))):
            if key not in base_keys:
                errors.append(f'{path.relative_to(ROOT)}: missing string {key}')
    return errors


if __name__ == '__main__':
    errors = validate()
    if errors:
        print('\n'.join(errors), file=sys.stderr)
        sys.exit(1)
    print('App string resources and references: OK')
