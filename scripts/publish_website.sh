#!/usr/bin/env bash
set -euo pipefail
: "${DEPLOY_BRANCH:?}"
test -s website/out/index.html
test -s deploy-repo/update.json
# Refresh the checkout before copying, so a newer app manifest is never rolled back.
git -C deploy-repo pull --ff-only origin "$DEPLOY_BRANCH"
mkdir -p deploy-repo/docs
# Keep the website publication independent from optional runner packages such as rsync.
find deploy-repo/docs -mindepth 1 -maxdepth 1 -exec rm -rf -- {} +
cp -a website/out/. deploy-repo/docs/
cp deploy-repo/update.json deploy-repo/docs/update.json
if [[ -f deploy-repo/release/builds.json ]]; then
  cp deploy-repo/release/builds.json deploy-repo/docs/builds.json
fi
touch deploy-repo/docs/.nojekyll
cd deploy-repo
git add docs
if ! git diff --cached --quiet; then
  git config user.name 'github-actions[bot]'
  git config user.email '41898282+github-actions[bot]@users.noreply.github.com'
  git commit -m 'chore: publish UniApp website'
  pushed=false
  for attempt in 1 2 3 4 5; do
    if git push origin "$DEPLOY_BRANCH"; then pushed=true; break; fi
    git pull --rebase origin "$DEPLOY_BRANCH"
  done
  [[ "$pushed" == true ]]
fi
