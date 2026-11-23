#!/usr/bin/env python3
"""Run checked-in release tests reliably, independently of the caller's directory."""
from pathlib import Path
import sys
import unittest

CHECKS_DIR = Path(__file__).resolve().parent
if str(CHECKS_DIR) not in sys.path:
    sys.path.insert(0, str(CHECKS_DIR))

from check_app_strings import validate
from check_motion_ownership import validate as validate_motion


def main() -> int:
    scripts_dir = CHECKS_DIR.parent
    directory = scripts_dir / 'tests'
    if not directory.is_dir():
        print('Release tests are missing from this checkout: scripts/tests', file=sys.stderr)
        return 1
    tests = unittest.defaultTestLoader.discover(str(directory), pattern='test_*.py')
    if tests.countTestCases() == 0:
        print('No release tests discovered', file=sys.stderr)
        return 1
    result = unittest.TextTestRunner(verbosity=2).run(tests)
    errors = validate() + validate_motion()
    for error in errors:
        print(error, file=sys.stderr)
    return 0 if result.wasSuccessful() and not errors else 1


if __name__ == '__main__':
    raise SystemExit(main())
