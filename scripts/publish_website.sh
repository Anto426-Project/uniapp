#!/usr/bin/env bash
set -euo pipefail
: "${DEPLOY_BRANCH:?}"
test -s website/out/index.html
test -s deploy-repo/update.json
# Refresh the checkout before copying, so a newer app manifest is never rolled back.
git -C deploy-repo pull --ff-only origin "$DEPLOY_BRANCH"
mkdir -p deploy-repo/docs
rsync -a --delete website/out/ deploy-repo/docs/
cp deploy-repo/update.json deploy-repo/docs/update.json
touch deploy-repo/docs/.nojekyll
cd deploy-repo
git add docs
if ! git diff --cached --quiet; then
  git config user.name 'github-actions[bot]'
  git config user.email '41898282+github-actions[bot]@users.noreply.github.com'
  git commit -m 'chore: publish UniApp website'
  git push origin "$DEPLOY_BRANCH"
fi
