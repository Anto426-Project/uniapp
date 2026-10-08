import os
from pathlib import Path
import subprocess
import tempfile
import unittest


SCRIPT = Path(__file__).resolve().parents[1] / 'publish_android.sh'


@unittest.skipIf(os.name == 'nt', 'The Android publisher runs on a POSIX runner')
class PublishAndroidTest(unittest.TestCase):
    def test_website_publication_keeps_the_platform_release_index(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            deploy = root / 'deploy-repo'
            remote = root / 'remote.git'
            env = {**os.environ, 'GIT_AUTHOR_NAME': 'Test', 'GIT_AUTHOR_EMAIL': 'test@example.invalid',
                   'GIT_COMMITTER_NAME': 'Test', 'GIT_COMMITTER_EMAIL': 'test@example.invalid',
                   'GIT_CONFIG_GLOBAL': os.devnull, 'GIT_CONFIG_NOSYSTEM': '1', 'DEPLOY_BRANCH': 'main'}
            def run(*args, cwd=root):
                return subprocess.run(args, cwd=cwd, env=env, check=True, capture_output=True, text=True)
            run('git', 'init', '--bare', '--initial-branch=main', str(remote))
            run('git', 'clone', str(remote), str(deploy))
            (deploy / 'docs').mkdir()
            (deploy / 'release').mkdir()
            (deploy / 'docs/index.html').write_text('old site')
            (deploy / 'update.json').write_text('{"latestVersionCode":1168}')
            records = '[{"tag":"android"},{"tag":"ios"},{"tag":"desktop"}]\n'
            (deploy / 'release/builds.json').write_text(records)
            (deploy / 'docs/builds.json').write_text(records)
            platforms = '{"schema":1,"platforms":{"windows":{"version":"2.0.15"}}}\n'
            (deploy / 'release/platforms.json').write_text(platforms)
            run('git', 'add', '.', cwd=deploy)
            run('git', 'commit', '-m', 'Existing releases', cwd=deploy)
            run('git', 'push', 'origin', 'main', cwd=deploy)
            (root / 'website/out').mkdir(parents=True)
            (root / 'website/out/index.html').write_text('new website')
            run('bash', str(SCRIPT.parent / 'publish_website.sh'))
            self.assertEqual('new website', run('git', '--git-dir=' + str(remote), 'show', 'main:docs/index.html').stdout)
            self.assertEqual(records, run('git', '--git-dir=' + str(remote), 'show', 'main:docs/builds.json').stdout)
            self.assertEqual(platforms, run('git', '--git-dir=' + str(remote), 'show', 'main:docs/platforms.json').stdout)

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
