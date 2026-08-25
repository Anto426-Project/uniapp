from __future__ import annotations

import argparse
import json
from datetime import datetime, timezone
from pathlib import Path
from urllib.parse import quote


AUTO_KEYS = (
    "latestVersion",
    "latestVersionCode",
    "downloadUrl",
    "downloadUrlsByAbi",
    "publishedAt",
    "releaseChannel",
    "buildCommit",
)
PREFERRED_ORDER = (
    "latestVersion",
    "latestVersionCode",
    "minSupportedVersion",
    "minSupportedVersionCode",
    "mandatory",
    "releaseChannel",
    "downloadUrl",
    "downloadUrlsByAbi",
    "notes",
    "publishedAt",
    "buildCommit",
    "appEnabled",
)
TRACK_NAME = "release"
CHANNELS_KEY = "channels"
MANUAL_SHARED_KEYS = {"shared", "default", CHANNELS_KEY, "channel", "stable", "beta"}


def load_json_object(path: Path | None) -> dict:
    if path is None or not path.exists():
        return {}

    with path.open("r", encoding="utf-8") as handle:
        data = json.load(handle)

    if not isinstance(data, dict):
        raise ValueError(f"{path} must contain a JSON object.")

    return data


def normalize_abi_key(raw: str | None) -> str | None:
    if raw is None:
        return None

    normalized = raw.strip().lower()
    if not normalized:
        return None

    aliases = {
        "arm64-v8a": "arm64-v8a",
        "arm64": "arm64-v8a",
        "armv8": "arm64-v8a",
        "armv8-a": "arm64-v8a",
        "aarch64": "arm64-v8a",
        "armeabi-v7a": "armeabi-v7a",
        "armeabi": "armeabi-v7a",
        "armv7": "armeabi-v7a",
        "armv7-a": "armeabi-v7a",
        "universal": "universal",
        "all": "universal",
    }
    return aliases.get(normalized, normalized)


def resolve_output_abi(element: dict) -> str | None:
    filters = element.get("filters")
    if isinstance(filters, list):
        for filter_item in filters:
            if not isinstance(filter_item, dict):
                continue
            filter_type = str(filter_item.get("filterType", "")).strip().upper()
            if filter_type != "ABI":
                continue
            return normalize_abi_key(str(filter_item.get("value", "")).strip())

    element_type = str(element.get("type", "")).strip().upper()
    if element_type == "UNIVERSAL":
        return "universal"

    return None


def resolve_apk_metadata(path: Path) -> tuple[str, int, str, dict[str, str]]:
    payload = load_json_object(path)
    elements = payload.get("elements")
    if not isinstance(elements, list) or not elements:
        raise ValueError(f"{path} does not contain APK output metadata.")

    version_name: str | None = None
    version_code: int | None = None
    default_output_file: str | None = None
    output_files_by_abi: dict[str, str] = {}

    for element in elements:
        if not isinstance(element, dict):
            continue

        output_file = str(element.get("outputFile", "")).strip()
        candidate_version_name = str(element.get("versionName", "")).strip()
        candidate_version_code_raw = element.get("versionCode")
        if not candidate_version_name or not output_file or candidate_version_code_raw is None:
            continue

        try:
            candidate_version_code = int(candidate_version_code_raw)
        except (TypeError, ValueError) as error:
            raise ValueError(
                f"{path} has an invalid versionCode: {candidate_version_code_raw!r}"
            ) from error

        if candidate_version_code <= 0:
            raise ValueError(f"{path} has a non-positive versionCode: {candidate_version_code}")

        version_name = version_name or candidate_version_name
        version_code = version_code or candidate_version_code

        abi_key = resolve_output_abi(element)
        if abi_key and abi_key not in output_files_by_abi:
            output_files_by_abi[abi_key] = output_file

        if default_output_file is None and abi_key == "universal":
            default_output_file = output_file
        elif default_output_file is None:
            default_output_file = output_file

    if version_name is None or version_code is None or default_output_file is None:
        raise ValueError(f"{path} is missing versionName, versionCode, or outputFile.")

    return version_name, version_code, default_output_file, output_files_by_abi


def release_artifact_dir(release_channel: str) -> str:
    return "stable" if release_channel == "stable" else "beta"


def version_already_contains_channel(version_name: str, release_channel: str) -> bool:
    normalized_version = version_name.strip().lower()
    if release_channel == "beta":
        return "beta" in normalized_version
    return True


def build_release_tag(version_name: str, version_code: int, release_channel: str) -> str:
    tag_name = f"v{version_name}+{version_code}"
    if release_channel != "stable" and not version_already_contains_channel(
        version_name, release_channel
    ):
        return f"{tag_name}-{release_channel}"
    return tag_name


def build_lfs_raw_download_url(
    repo: str,
    branch: str,
    release_channel: str,
    output_file: str,
) -> str:
    artifact_dir = release_artifact_dir(release_channel)
    return f"https://github.com/{repo}/raw/{branch}/src/release/{artifact_dir}/{output_file}"


def build_release_asset_download_url(
    repo: str,
    version_name: str,
    version_code: int,
    release_channel: str,
    output_file: str,
) -> str:
    tag_name = build_release_tag(version_name, version_code, release_channel)
    return (
        f"https://github.com/{repo}/releases/download/"
        f"{quote(tag_name, safe='')}/{quote(output_file, safe='')}"
    )


def build_download_url(
    repo: str,
    branch: str,
    release_channel: str,
    output_file: str,
    version_name: str,
    version_code: int,
    use_lfs_raw_downloads: bool = False,
) -> str:
    if use_lfs_raw_downloads:
        return build_lfs_raw_download_url(repo, branch, release_channel, output_file)
    return build_release_asset_download_url(
        repo=repo,
        version_name=version_name,
        version_code=version_code,
        release_channel=release_channel,
        output_file=output_file,
    )


def build_download_urls_by_abi(
    repo: str,
    branch: str,
    release_channel: str,
    version_name: str,
    version_code: int,
    output_files_by_abi: dict[str, str],
    use_lfs_raw_downloads: bool = False,
) -> dict[str, str]:
    return {
        abi: build_download_url(
            repo=repo,
            branch=branch,
            release_channel=release_channel,
            version_name=version_name,
            version_code=version_code,
            output_file=output_file,
            use_lfs_raw_downloads=use_lfs_raw_downloads,
        )
        for abi, output_file in output_files_by_abi.items()
    }


def normalize_release_channel(raw: str | None) -> str | None:
    if raw is None:
        return None

    normalized = raw.strip().lower()
    if not normalized:
        return None

    if normalized not in {"stable", "beta"}:
        raise ValueError(f"Unsupported release channel: {raw}")

    return normalized


def infer_release_channel(version_name: str, explicit_channel: str | None) -> str:
    if explicit_channel is not None:
        return explicit_channel

    normalized_version = version_name.strip().lower()
    if "beta" in normalized_version or "-b" in normalized_version:
        return "beta"
    return "stable"


def compose_manifest(
    existing: dict,
    manual: dict,
    version_name: str,
    version_code: int,
    download_url: str,
    download_urls_by_abi: dict[str, str],
    published_at: str,
    release_channel: str,
    build_commit: str | None,
) -> dict:
    combined = dict(existing)
    combined.update(manual)

    manifest: dict = {}
    for key in PREFERRED_ORDER:
        if key == "latestVersion":
            manifest[key] = version_name
        elif key == "latestVersionCode":
            manifest[key] = version_code
        elif key == "downloadUrl":
            manifest[key] = download_url
        elif key == "downloadUrlsByAbi":
            if download_urls_by_abi:
                manifest[key] = download_urls_by_abi
            elif key in combined:
                manifest[key] = combined[key]
        elif key == "publishedAt":
            manifest[key] = published_at
        elif key == "releaseChannel":
            manifest[key] = release_channel
        elif key == "buildCommit":
            if build_commit:
                manifest[key] = build_commit
            elif key in combined:
                manifest[key] = combined[key]
        elif key in combined:
            manifest[key] = combined[key]

    for key, value in combined.items():
        if key not in manifest and key not in AUTO_KEYS:
            manifest[key] = value

    return manifest


def legacy_manifest_from_root(root: dict) -> dict:
    return {
        key: value
        for key, value in root.items()
        if key in PREFERRED_ORDER or key in AUTO_KEYS
    }


def extract_channels_root(root: dict) -> dict[str, dict]:
    channels_root = root.get(CHANNELS_KEY) or root.get("channel")
    if isinstance(channels_root, dict):
        extracted: dict[str, dict] = {}
        for channel_name, channel_value in channels_root.items():
            if isinstance(channel_value, dict):
                extracted[channel_name] = dict(channel_value)
        return extracted

    legacy_manifest = legacy_manifest_from_root(root)
    if legacy_manifest:
        return {"stable": {TRACK_NAME: legacy_manifest}}

    return {}


def extract_existing_channel_manifest(root: dict, release_channel: str) -> dict:
    channels_root = extract_channels_root(root)
    channel_node = channels_root.get(release_channel)
    if not isinstance(channel_node, dict):
        return {}

    track_node = channel_node.get(TRACK_NAME)
    if isinstance(track_node, dict):
        return dict(track_node)

    return dict(channel_node)


def compose_root_manifest(
    existing: dict,
    scoped_manifest: dict,
    release_channel: str,
) -> dict:
    channels_root = extract_channels_root(existing)
    channel_node = dict(channels_root.get(release_channel) or {})
    channel_node[TRACK_NAME] = scoped_manifest
    channels_root[release_channel] = channel_node

    root = {
        key: value
        for key, value in existing.items()
        if key not in PREFERRED_ORDER and key not in AUTO_KEYS and key not in {CHANNELS_KEY, "channel"}
    }
    root[CHANNELS_KEY] = channels_root
    return root


def extract_manual_shared_config(root: dict) -> dict:
    shared: dict = {}

    top_level_shared = {
        key: value for key, value in root.items() if key not in MANUAL_SHARED_KEYS
    }
    shared.update(top_level_shared)

    for key in ("shared", "default"):
        node = root.get(key)
        if isinstance(node, dict):
            shared.update(node)

    return shared


def extract_manual_channel_config(root: dict, release_channel: str) -> dict:
    scoped: dict = {}

    top_level_channel = root.get(release_channel)
    if isinstance(top_level_channel, dict):
        track_node = top_level_channel.get(TRACK_NAME)
        if isinstance(track_node, dict):
            scoped.update(track_node)
        else:
            scoped.update(top_level_channel)

    channels_root = root.get(CHANNELS_KEY) or root.get("channel")
    if isinstance(channels_root, dict):
        channel_node = channels_root.get(release_channel)
        if isinstance(channel_node, dict):
            track_node = channel_node.get(TRACK_NAME)
            if isinstance(track_node, dict):
                scoped.update(track_node)
            else:
                scoped.update(channel_node)

    return scoped


def resolve_manual_config(root: dict, release_channel: str) -> dict:
    resolved = extract_manual_shared_config(root)
    resolved.update(extract_manual_channel_config(root, release_channel))
    return resolved


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Generate update.json from build metadata and manual config."
    )
    parser.add_argument("--metadata", required=True, type=Path)
    parser.add_argument("--manual-config", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--deploy-repo", required=True)
    parser.add_argument("--deploy-branch", required=True)
    parser.add_argument("--existing", type=Path)
    parser.add_argument("--published-at")
    parser.add_argument("--release-channel")
    parser.add_argument("--build-commit")
    parser.add_argument(
        "--use-lfs-raw-downloads",
        action="store_true",
        help=(
            "Generate github.com/.../raw/... URLs. By default update downloads point "
            "to GitHub Release assets to avoid slow and flaky LFS raw downloads."
        ),
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    version_name, version_code, output_file, output_files_by_abi = resolve_apk_metadata(
        args.metadata
    )
    manual_root = load_json_object(args.manual_config)
    existing = load_json_object(args.existing)
    published_at = args.published_at or datetime.now(timezone.utc).date().isoformat()
    release_channel = infer_release_channel(
        version_name=version_name,
        explicit_channel=normalize_release_channel(args.release_channel),
    )
    download_url = build_download_url(
        repo=args.deploy_repo,
        branch=args.deploy_branch,
        release_channel=release_channel,
        version_name=version_name,
        version_code=version_code,
        output_file=output_file,
        use_lfs_raw_downloads=args.use_lfs_raw_downloads,
    )
    download_urls_by_abi = build_download_urls_by_abi(
        repo=args.deploy_repo,
        branch=args.deploy_branch,
        release_channel=release_channel,
        version_name=version_name,
        version_code=version_code,
        output_files_by_abi=output_files_by_abi,
        use_lfs_raw_downloads=args.use_lfs_raw_downloads,
    )
    build_commit = args.build_commit.strip() if args.build_commit else None
    manual = resolve_manual_config(manual_root, release_channel)

    manifest = compose_manifest(
        existing=extract_existing_channel_manifest(existing, release_channel),
        manual=manual,
        version_name=version_name,
        version_code=version_code,
        download_url=download_url,
        download_urls_by_abi=download_urls_by_abi,
        published_at=published_at,
        release_channel=release_channel,
        build_commit=build_commit,
    )
    root_manifest = compose_root_manifest(
        existing=existing,
        scoped_manifest=manifest,
        release_channel=release_channel,
    )

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="\n") as handle:
        json.dump(root_manifest, handle, ensure_ascii=False, indent=2)
        handle.write("\n")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
