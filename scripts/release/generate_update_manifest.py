#!/usr/bin/env python3
"""Create one Android update manifest from verified APKs and release policy."""
from __future__ import annotations

import argparse
from datetime import datetime, timezone
from pathlib import Path
from release_metadata import asset_url, load_json, published_releases, read_apks, write_json


def generate(metadata: Path, config: dict, existing: dict, repo: str, commit: str,
             published_at: str | None = None) -> dict:
    build = read_apks(metadata)
    code = build["versionCode"]
    for previous in published_releases(existing):
        old_code = previous.get("latestVersionCode", 0)
        if type(old_code) is int and old_code > code:
            raise ValueError("Refusing to replace a newer published build")
        if old_code == code and previous.get("buildCommit") not in (None, "", commit):
            raise ValueError("Increment versionCode before publishing a different commit")
    allowed = {"minSupportedVersion", "minSupportedVersionCode", "mandatory", "notes",
               "appEnabled", "description"}
    unknown = config.keys() - allowed
    if unknown:
        raise ValueError(f"Unsupported release policy keys: {sorted(unknown)}")
    minimum = config.get("minSupportedVersionCode", 1)
    if type(minimum) is not int or not 0 < minimum <= code:
        raise ValueError("Minimum supported build must be positive and cannot exceed this release")
    for key in ("mandatory", "appEnabled"):
        if key in config and type(config[key]) is not bool:
            raise ValueError(f"{key} must be a boolean")
    urls = {abi: asset_url(repo, build["tag"], name) for abi, name in build["files"].items()}
    result = dict(config)
    result.update(latestVersion=build["versionName"], latestVersionCode=code,
                  downloadUrl=urls["arm64-v8a"], downloadUrlsByAbi=urls,
                  sha256ByAbi={abi: build["sha256"][name] for abi, name in build["files"].items()},
                  publishedAt=published_at or datetime.now(timezone.utc).isoformat(), buildCommit=commit)
    # iOS has an independent version/build sequence and App Store destination.
    if isinstance(existing.get("platforms"), dict):
        result["platforms"] = existing["platforms"]
    return result


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--metadata", required=True, type=Path)
    parser.add_argument("--manual-config", required=True, type=Path)
    parser.add_argument("--existing", type=Path)
    parser.add_argument("--deploy-repo", required=True)
    parser.add_argument("--build-commit", required=True)
    parser.add_argument("--published-at")
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    write_json(args.output, generate(args.metadata, load_json(args.manual_config),
                                    load_json(args.existing), args.deploy_repo,
                                    args.build_commit, args.published_at))


if __name__ == "__main__":
    main()
