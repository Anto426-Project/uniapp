import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
import unittest


PROJECT = Path(__file__).resolve().parents[2] / 'iosApp/iosApp.xcodeproj/project.pbxproj'


class XcodeBuildPhaseTest(unittest.TestCase):
    def test_framework_build_handles_absent_and_enabled_ide_override(self):
        match = re.search(r'shellScript = ("(?:[^"\\]|\\.)*");', PROJECT.read_text())
        self.assertIsNotNone(match)
        script = json.loads(match.group(1))
        for override in (None, 'NO', 'YES'):
            with self.subTest(override=override), tempfile.TemporaryDirectory() as directory:
                root = Path(directory)
                (root / 'iosApp').mkdir()
                gradlew = root / 'gradlew'
                gradlew.write_text('#!/bin/sh\nprintf "%s\\n" "$@" > invocation.txt\n')
                gradlew.chmod(0o755)
                env = {**os.environ, 'SRCROOT': str(root / 'iosApp')}
                env.pop('OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED', None)
                if override is not None:
                    env['OVERRIDE_KOTLIN_BUILD_IDE_SUPPORTED'] = override
                result = subprocess.run(['/bin/sh', '-c', script], env=env, text=True, capture_output=True)
                self.assertEqual(0, result.returncode, result.stderr)
                invocation = root / 'invocation.txt'
                if override == 'YES':
                    self.assertFalse(invocation.exists())
                else:
                    self.assertIn(':composeApp:embedAndSignAppleFrameworkForXcode', invocation.read_text())
