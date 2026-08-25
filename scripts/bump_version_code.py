from __future__ import annotations

import argparse
import datetime
import json
import re
from pathlib import Path


DEFAULT_CHANGELOG_MESSAGE = "Aggiornamento automatico certificati TLS."


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Increment Android versionCode by one and update update-config.json "
            "plus CHANGELOG.md."
        )
    )
    parser.add_argument(
        "--file",
        type=Path,
        default=Path("androidApp/build.gradle.kts"),
        help="Path to build.gradle.kts containing versionCode.",
    )
    parser.add_argument(
        "--update-config",
        type=Path,
        default=Path("update-config.json"),
        help="Path to update-config.json.",
    )
    parser.add_argument(
        "--changelog",
        type=Path,
        default=Path("CHANGELOG.md"),
        help="Path to CHANGELOG.md.",
    )
    parser.add_argument(
        "--message",
        default=DEFAULT_CHANGELOG_MESSAGE,
        help="Single-line changelog/update note message.",
    )
    return parser.parse_args()


def read_version_info(path: Path) -> tuple[str, int, str]:
    content = path.read_text(encoding="utf-8")
    code_match = re.search(r"(\bversionCode\s*=\s*)(\d+)", content)
    name_match = re.search(r'(\bversionName\s*=\s*)"(.*?)"', content)
    if code_match is None:
        raise ValueError(f"Could not find versionCode assignment in {path}.")
    if name_match is None:
        raise ValueError(f"Could not find versionName assignment in {path}.")
    return content, int(code_match.group(2)), name_match.group(2)


def write_bumped_version_code(path: Path, content: str, next_code: int) -> None:
    updated = re.sub(
        r"(\bversionCode\s*=\s*)\d+",
        rf"\g<1>{next_code}",
        content,
        count=1,
    )
    path.write_text(updated, encoding="utf-8", newline="\n")


def resolve_release_channel(version_name: str) -> str:
    normalized = version_name.lower()
    return "beta" if ("beta" in normalized or "alpha" in normalized) else "stable"


def update_update_config(
    path: Path,
    channel: str,
    previous_code: int,
    message: str,
) -> bool:
    if not path.exists():
        return False

    payload = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(payload, dict):
        raise ValueError(f"{path} must contain a JSON object.")

    channel_node = payload.get(channel)
    if not isinstance(channel_node, dict):
        payload[channel] = {}
        channel_node = payload[channel]

    current_min_code = channel_node.get("minSupportedVersionCode")
    if not isinstance(current_min_code, int) or current_min_code <= 0:
        channel_node["minSupportedVersionCode"] = previous_code

    today_label = datetime.datetime.now().strftime("%d %b %Y")
    channel_node["notes"] = f"Changelog {today_label}:\n- {message}"

    serialized = json.dumps(payload, ensure_ascii=False, indent=2) + "\n"
    path.write_text(serialized, encoding="utf-8", newline="\n")
    return True


def update_changelog(path: Path, next_code: int, message: str) -> bool:
    today_iso = datetime.datetime.now().strftime("%Y-%m-%d")
    entry_header = f"## [{next_code}] - {today_iso}"
    entry_body = f"{entry_header}\n- {message}\n\n"

    existing = path.read_text(encoding="utf-8") if path.exists() else ""
    if entry_header in existing:
        return False

    path.write_text(entry_body + existing, encoding="utf-8", newline="\n")
    return True


def main() -> int:
    args = parse_args()
    message = args.message.strip()
    if not message:
        raise ValueError("Message cannot be blank.")

    content, previous_code, version_name = read_version_info(args.file)
    next_code = previous_code + 1
    write_bumped_version_code(
        path=args.file,
        content=content,
        next_code=next_code,
    )

    channel = resolve_release_channel(version_name)
    config_updated = update_update_config(
        path=args.update_config,
        channel=channel,
        previous_code=previous_code,
        message=message,
    )
    changelog_updated = update_changelog(
        path=args.changelog,
        next_code=next_code,
        message=message,
    )

    print(
        json.dumps(
            {
                "versionCodeFrom": previous_code,
                "versionCodeTo": next_code,
                "channel": channel,
                "updateConfigUpdated": config_updated,
                "changelogUpdated": changelog_updated,
            }
        )
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
