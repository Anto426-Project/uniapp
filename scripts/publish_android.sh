#!/usr/bin/env bash
# The checked-out distribution repository already has scoped checkout credentials.
set -euo pipefail
: "${DEPLOY_REPO:?}" "${DEPLOY_BRANCH:?}" "${RELEASE_TAG_NAME:?}" "${RELEASE_TITLE:?}" "${SOURCE_SHA:?}"
mapfile -t release_assets < <(find incoming/release -maxdepth 1 -type f -name '*.apk' | sort)
((${#release_assets[@]} > 0))
# Publish files before advertising their URLs. A failed upload leaves the current manifest intact.
if gh release view "$RELEASE_TAG_NAME" --repo "$DEPLOY_REPO" >/dev/null 2>&1; then
  gh release upload "$RELEASE_TAG_NAME" "${release_assets[@]}" --clobber --repo "$DEPLOY_REPO"
  gh release edit "$RELEASE_TAG_NAME" --repo "$DEPLOY_REPO" --title "$RELEASE_TITLE" \
    --notes-file incoming/release-notes.md --prerelease=false --latest
else
  gh release create "$RELEASE_TAG_NAME" "${release_assets[@]}" --repo "$DEPLOY_REPO" \
    --target "$DEPLOY_BRANCH" --title "$RELEASE_TITLE" --notes-file incoming/release-notes.md --latest
fi
# Publish app metadata independently of the website build.
mkdir -p deploy-repo/release
cp incoming/update.json deploy-repo/update.json
cp incoming/README.md deploy-repo/README.md
cp incoming/release/output-metadata.json deploy-repo/release/output-metadata.json
cp incoming/context.json deploy-repo/release/context.json
# The existing site reads this manifest immediately; its static build has a separate workflow.
if [[ -d deploy-repo/docs ]]; then cp incoming/update.json deploy-repo/docs/update.json; fi
touch deploy-repo/.nojekyll
cd deploy-repo
git add -A
if ! git diff --cached --quiet; then
  git config user.name 'github-actions[bot]'
  git config user.email '41898282+github-actions[bot]@users.noreply.github.com'
  git commit -m "chore: publish UniApp from ${SOURCE_SHA:0:7}"
  git push origin "$DEPLOY_BRANCH"
fi
