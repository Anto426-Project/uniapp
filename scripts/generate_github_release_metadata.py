from __future__ import annotations

import argparse
import json
from pathlib import Path

TRACK_NAME = "release"


def load_json_object(path: Path) -> dict:
    with path.open("r", encoding="utf-8") as handle:
        data = json.load(handle)

    if not isinstance(data, dict):
        raise ValueError(f"{path} must contain a JSON object.")

    return data


def resolve_apk_metadata(path: Path) -> dict:
    payload = load_json_object(path)
    elements = payload.get("elements")
    if not isinstance(elements, list) or not elements or not isinstance(elements[0], dict):
        raise ValueError(f"{path} does not contain valid APK metadata.")

    first = elements[0]
    output_file = str(first.get("outputFile", "")).strip()
    version_name = str(first.get("versionName", "")).strip()
    version_code_raw = first.get("versionCode")
    if not output_file or not version_name or version_code_raw is None:
        raise ValueError(f"{path} is missing outputFile, versionName, or versionCode.")

    try:
        version_code = int(version_code_raw)
    except (TypeError, ValueError) as error:
        raise ValueError(f"{path} has an invalid versionCode: {version_code_raw!r}") from error

    return {
        "outputFile": output_file,
        "versionName": version_name,
        "versionCode": version_code,
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


def resolve_scoped_manifest(root: dict, release_channel: str) -> dict:
    channels_root = root.get("channels") or root.get("channel")
    if not isinstance(channels_root, dict):
        return root

    channel_node = channels_root.get(release_channel) or channels_root.get("stable")
    if not isinstance(channel_node, dict):
        return root

    track_node = channel_node.get(TRACK_NAME)
    if isinstance(track_node, dict):
        return track_node

    return channel_node


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


def build_release_title(version_name: str, version_code: int, release_channel: str) -> str:
    title = f"UniApp {version_name} ({version_code})"
    if release_channel != "stable" and not version_already_contains_channel(
        version_name, release_channel
    ):
        return f"{title} {release_channel}"
    return title


def render_release_notes(
    manifest_root: dict,
    metadata: dict,
    deploy_repo: str,
    deploy_branch: str,
    source_repo: str,
    github_sha: str,
    release_channel: str,
) -> str:
    manifest = resolve_scoped_manifest(manifest_root, release_channel)
    notes = str(manifest.get("notes", "")).strip()
    published_at = str(manifest.get("publishedAt", "-")).strip() or "-"
    download_url = str(manifest.get("downloadUrl", "")).strip()
    output_file = metadata["outputFile"]
    version_name = metadata["versionName"]
    version_code = metadata["versionCode"]

    lines = [
        f"Automated Android release for `{github_sha[:7]}`.",
        "",
        f"- Version: `{version_name}`",
        f"- Version code: `{version_code}`",
        f"- Channel: `{release_channel}`",
        f"- Published at: `{published_at}`",
        f"- APK: `{output_file}`",
        f"- Deploy manifest: https://github.com/{deploy_repo}/blob/{deploy_branch}/update.json",
        f"- Source repository: https://github.com/{source_repo}",
    ]

    if download_url:
        lines.append(f"- Direct download: {download_url}")

    lines.extend(
        [
            "",
            "## Release Notes",
            "",
            notes or "No release notes were provided for this build.",
            "",
        ]
    )

    return "\n".join(lines)


def append_github_env(path: Path, values: dict[str, str]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("a", encoding="utf-8", newline="\n") as handle:
        for key, value in values.items():
            handle.write(f"{key}={value}\n")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate GitHub release metadata.")
    parser.add_argument("--update-manifest", required=True, type=Path)
    parser.add_argument("--metadata", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--github-env-output", required=True, type=Path)
    parser.add_argument("--source-repo", required=True)
    parser.add_argument("--deploy-repo", required=True)
    parser.add_argument("--deploy-branch", required=True)
    parser.add_argument("--github-sha", required=True)
    parser.add_argument("--release-channel")
    parser.add_argument("--asset-path-prefix", default="artifacts/release")
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    manifest = load_json_object(args.update_manifest)
    metadata = resolve_apk_metadata(args.metadata)
    release_channel = infer_release_channel(
        version_name=metadata["versionName"],
        explicit_channel=normalize_release_channel(
            args.release_channel
        ),
    )
    notes = render_release_notes(
        manifest_root=manifest,
        metadata=metadata,
        deploy_repo=args.deploy_repo,
        deploy_branch=args.deploy_branch,
        source_repo=args.source_repo,
        github_sha=args.github_sha,
        release_channel=release_channel,
    )

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="\n") as handle:
        handle.write(notes)

    append_github_env(
        args.github_env_output,
        {
            "RELEASE_TAG_NAME": build_release_tag(
                metadata["versionName"],
                metadata["versionCode"],
                release_channel,
            ),
            "RELEASE_TITLE": build_release_title(
                metadata["versionName"],
                metadata["versionCode"],
                release_channel,
            ),
            "RELEASE_CHANNEL": release_channel,
            "RELEASE_IS_PRERELEASE": "true"
            if release_channel != "stable"
            else "false",
            "RELEASE_ASSET_PATH": str(
                Path(args.asset_path_prefix) / metadata["outputFile"]
            ).replace("\\", "/"),
        },
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
