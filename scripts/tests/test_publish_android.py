import os
from pathlib import Path
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / 'publish_android.sh'


class PublishAndroidTest(unittest.TestCase):
    def test_publish_app_without_website_output_preserves_existing_site(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            deployment = root / 'deploy-repo'
            remote = root / 'remote.git'
            env = {**os.environ, 'GIT_AUTHOR_NAME': 'Test', 'GIT_AUTHOR_EMAIL': 'test@example.invalid',
                   'GIT_COMMITTER_NAME': 'Test', 'GIT_COMMITTER_EMAIL': 'test@example.invalid',
                   'GIT_CONFIG_GLOBAL': os.devnull, 'GIT_CONFIG_NOSYSTEM': '1'}

            def run(*command, cwd=root):
                return subprocess.run(command, cwd=cwd, env=env, text=True, capture_output=True, check=True)

            run('git', 'init', '--bare', '--initial-branch=main', str(remote))
            run('git', 'clone', str(remote), str(deployment))
            (deployment / 'docs').mkdir()
            (deployment / 'docs/index.html').write_text('existing website')
            (deployment / 'docs/update.json').write_text('old manifest')
            run('git', 'add', '.', cwd=deployment)
            run('git', 'commit', '-m', 'Existing site', cwd=deployment)
            run('git', 'push', 'origin', 'main', cwd=deployment)

            incoming = root / 'incoming'
            (incoming / 'release').mkdir(parents=True)
            (incoming / 'release/app.apk').write_bytes(b'test APK')
            for filename in ('update.json', 'README.md', 'context.json', 'release-notes.md', 'release/output-metadata.json'):
                (incoming / filename).write_text(f'new {filename}')

            # GitHub is stubbed; the real script commits and pushes to a local bare repository.
            binaries = root / 'bin'
            binaries.mkdir()
            gh = binaries / 'gh'
            gh.write_text('#!/usr/bin/env bash\nif [[ "$2" == view ]]; then exit 1; fi\nexit 0\n')
            gh.chmod(0o755)
            env.update(PATH=str(binaries) + os.pathsep + env['PATH'], DEPLOY_REPO='owner/distribution',
                       DEPLOY_BRANCH='main', RELEASE_TAG_NAME='v2.0.3+203', RELEASE_TITLE='Test release', SOURCE_SHA='a' * 40)
            run('bash', str(SCRIPT))

            self.assertFalse((root / 'website').exists())
            self.assertEqual('existing website', run('git', '--git-dir=' + str(remote), 'show', 'main:docs/index.html').stdout)
            for filename in ('update.json', 'README.md', 'release/output-metadata.json', 'release/context.json', 'docs/update.json'):
                source = 'context.json' if filename == 'release/context.json' else 'update.json' if filename == 'docs/update.json' else filename
                self.assertEqual((incoming / source).read_text(), run('git', '--git-dir=' + str(remote), 'show', f'main:{filename}').stdout)
