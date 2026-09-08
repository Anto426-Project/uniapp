#!/usr/bin/env python3
"""Run checked-in release tests reliably, independently of the caller's directory."""
from pathlib import Path
import sys
import unittest
from check_app_strings import validate


def main() -> int:
    root = Path(__file__).resolve().parent
    directory = root / 'tests'
    if not directory.is_dir():
        print('Release tests are missing from this checkout: scripts/tests', file=sys.stderr)
        return 1
    tests = unittest.defaultTestLoader.discover(str(directory), pattern='test_*.py')
    if tests.countTestCases() == 0:
        print('No release tests discovered', file=sys.stderr)
        return 1
    result = unittest.TextTestRunner(verbosity=2).run(tests)
    errors = validate()
    for error in errors:
        print(error, file=sys.stderr)
    return 0 if result.wasSuccessful() and not errors else 1


if __name__ == '__main__':
    raise SystemExit(main())
