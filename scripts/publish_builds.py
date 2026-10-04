#!/usr/bin/env python3
"""Publish all verified build variants, retaining the signed Android update channel."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import sys
import tempfile
from urllib.parse import quote

ROOT = Path(__file__).resolve().parents[1]


def command(*args, cwd=None, env=None):
    subprocess.run([str(arg) for arg in args], cwd=cwd, env=env, check=True)


def release_data(repository, tag):
    result = subprocess.run(['gh', 'api', f'repos/{repository}/releases/tags/{quote(tag, safe="")}'], text=True, capture_output=True)
    if result.returncode == 0:
        return json.loads(result.stdout)
    if '404' in result.stderr:
        return None
    raise RuntimeError(result.stderr.strip())


def verify_existing_assets(repository, release, assets):
    existing = {asset['name']: asset for asset in release['assets']}
    missing = []
    for name in assets:
        path = Path(name)
        digest = hashlib.sha256(path.read_bytes()).hexdigest()
        if path.name not in existing:
            missing.append(name)
            continue
        remote = existing[path.name]
        if remote.get('digest') == 'sha256:' + digest:
            continue
        with tempfile.TemporaryDirectory() as temporary:
            target = Path(temporary) / path.name
            with target.open('wb') as handle:
                subprocess.run(['gh', 'api', '-H', 'Accept: application/octet-stream',
                    f'repos/{repository}/releases/assets/{remote["id"]}'], stdout=handle, check=True)
            if hashlib.sha256(target.read_bytes()).hexdigest() != digest:
                raise ValueError(f'Existing immutable release asset differs: {path.name}')
    return missing


def publish_signed_android(group, deploy_repo, deploy_directory, policy, source_repo, revision):
    incoming = Path(group['notes']).parent / 'incoming'
    metadata = incoming / 'release/output-metadata.json'
    command(sys.executable, ROOT / 'scripts/generate_update_manifest.py', '--metadata', metadata,
        '--manual-config', policy, '--existing', deploy_directory / 'update.json', '--deploy-repo', deploy_repo,
        '--build-commit', revision, '--output', incoming / 'update.json')
    env_file = incoming / 'release.env'
    command(sys.executable, ROOT / 'scripts/generate_github_release_metadata.py', '--metadata', metadata,
        '--update-manifest', incoming / 'update.json', '--source-repo', source_repo,
        '--output', incoming / 'release-notes.md', '--github-env-output', env_file)
    with (incoming / 'release-notes.md').open('a') as handle:
        handle.write('\nInclusi anche gli APK debug, firmati con una chiave di sviluppo.\n')
    command(sys.executable, ROOT / 'scripts/generate_deploy_readme.py', '--update-manifest', incoming / 'update.json',
        '--source-repo', source_repo, '--output', incoming / 'README.md')
    environment = os.environ.copy()
    environment.update(DEPLOY_REPO=deploy_repo, DEPLOY_BRANCH='main', SOURCE_SHA=revision)
    environment.update(line.split('=', 1) for line in env_file.read_text().splitlines() if line)
    # The existing publisher expects deploy-repo and incoming in its working directory.
    working = incoming.parent
    link = working / 'deploy-repo'
    if not link.exists():
        link.symlink_to(deploy_directory.resolve(), target_is_directory=True)
    command('bash', ROOT / 'scripts/publish_android.sh', cwd=working, env=environment)


def publish(plan, repository, deploy_directory, policy, source_repo, revision, run_id):
    for group in plan:
        remote = release_data(repository, group['tag'])
        missing = verify_existing_assets(repository, remote, group['assets']) if remote else group['assets']
        if group['platform'] == 'android' and group['signing'] == 'release-key':
            publish_signed_android(group, repository, deploy_directory, policy, source_repo, revision)
            # The signed publisher already supplied release/debug APKs; upload other verified assets.
            remote = release_data(repository, group['tag'])
            missing = verify_existing_assets(repository, remote, group['assets'])
        elif remote is None:
            command('gh', 'release', 'create', group['tag'], *group['assets'], '--repo', repository,
                '--target', 'main', '--title', f'UniApp {group["version"]} — {group["platform"]}',
                '--notes-file', group['notes'], '--prerelease', '--latest=false')
            missing = []
        if missing:
            command('gh', 'release', 'upload', group['tag'], *missing, '--repo', repository)
        print(f'Published: https://github.com/{repository}/releases/tag/{quote(group["tag"], safe="+")}')
    index = deploy_directory / 'release/builds.json'
    records = json.loads(index.read_text()) if index.is_file() else []
    for group in plan:
        item = dict(tag=group['tag'], platform=group['platform'], signing=group['signing'], version=group['version'],
                    sourceSha=revision, runId=run_id, url=f'https://github.com/{repository}/releases/tag/{quote(group["tag"], safe="+")}')
        records = [old for old in records if old['tag'] != group['tag']] + [item]
    index.parent.mkdir(exist_ok=True)
    index.write_text(json.dumps(records, indent=2) + '\n')
    paths = ['release/builds.json']
    if (deploy_directory / 'docs').is_dir():
        (deploy_directory / 'docs/builds.json').write_text(index.read_text())
        paths.append('docs/builds.json')
    command('git', 'add', '--', *paths, cwd=deploy_directory)
    if subprocess.run(['git', 'diff', '--cached', '--quiet'], cwd=deploy_directory).returncode != 0:
        command('git', 'config', 'user.name', 'github-actions[bot]', cwd=deploy_directory)
        command('git', 'config', 'user.email', '41898282+github-actions[bot]@users.noreply.github.com', cwd=deploy_directory)
        command('git', 'commit', '-m', f'chore: publish UniApp build {run_id}', cwd=deploy_directory)
        command('git', 'push', 'origin', 'main', cwd=deploy_directory)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--plan', type=Path, required=True)
    parser.add_argument('--repository', required=True)
    parser.add_argument('--deploy-directory', type=Path, required=True)
    parser.add_argument('--policy', type=Path, required=True)
    parser.add_argument('--source-repo', required=True)
    parser.add_argument('--revision', required=True)
    parser.add_argument('--run-id', required=True)
    args = parser.parse_args()
    publish(json.loads(args.plan.read_text()), args.repository, args.deploy_directory.resolve(), args.policy.resolve(),
            args.source_repo, args.revision, args.run_id)


if __name__ == '__main__':
    main()
