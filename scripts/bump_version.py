from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import re
import sys
from dataclasses import dataclass
from pathlib import Path


SEMVER_PATTERN = re.compile(
    r"^(?P<major>\d+)\.(?P<minor>\d+)\.(?P<patch>\d+)(?P<suffix>[-.][A-Za-z0-9.-]+)?$"
)


@dataclass
class VersionInfo:
    gradle_content: str
    version_code: int
    version_name: str


@dataclass
class ChannelState:
    channel: str
    min_supported_version: str
    min_supported_version_code: int
    mandatory: bool
    notes: str | None


@dataclass
class ReleasePlan:
    current_version_code: int
    new_version_code: int
    current_version_name: str
    new_version_name: str
    channel_state_before: ChannelState | None
    channel_state_after: ChannelState | None
    changelog_lines: list[str]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Bump della versione Android con supporto interattivo e non-interattivo. "
            "Aggiorna build.gradle.kts, update-config.json e CHANGELOG.md."
        )
    )
    parser.add_argument(
        "--file",
        type=Path,
        default=Path("androidApp/build.gradle.kts"),
        help="Percorso del build.gradle.kts da aggiornare.",
    )
    parser.add_argument(
        "--update-config",
        type=Path,
        default=Path("update-config.json"),
        help="Percorso di update-config.json.",
    )
    parser.add_argument(
        "--changelog",
        type=Path,
        default=Path("CHANGELOG.md"),
        help="Percorso di CHANGELOG.md.",
    )
    parser.add_argument(
        "--version-code",
        type=int,
        help="Nuovo versionCode.",
    )
    parser.add_argument(
        "--version-name",
        help="Nuovo versionName.",
    )
    parser.add_argument(
        "--bump",
        choices=("patch", "minor", "major", "none"),
        default="patch",
        help=(
            "Strategia per proporre automaticamente il versionName quando "
            "--version-name non viene specificato."
        ),
    )
    parser.add_argument(
        "--channel",
        choices=("stable", "beta"),
        help="Canale da aggiornare in update-config.json. Di default viene derivato dal versionName.",
    )
    parser.add_argument(
        "--mandatory",
        choices=("true", "false"),
        help="Override del flag mandatory nel canale selezionato.",
    )
    parser.add_argument(
        "--sync-min-supported",
        action="store_true",
        help="Allinea minSupportedVersion e minSupportedVersionCode al nuovo rilascio.",
    )
    parser.add_argument(
        "--min-supported-version",
        help="Override esplicito di minSupportedVersion.",
    )
    parser.add_argument(
        "--min-supported-version-code",
        type=int,
        help="Override esplicito di minSupportedVersionCode.",
    )
    parser.add_argument(
        "--note",
        action="append",
        default=[],
        help="Riga di changelog/note. Puoi ripetere il flag piu' volte.",
    )
    parser.add_argument(
        "--notes-file",
        type=Path,
        help="File testo contenente le note, una per riga.",
    )
    parser.add_argument(
        "--skip-notes",
        action="store_true",
        help="Non aggiornare note e changelog.",
    )
    parser.add_argument(
        "--yes",
        "-y",
        action="store_true",
        help="Applica senza chiedere conferma finale.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Mostra solo il riepilogo senza scrivere i file.",
    )
    return parser.parse_args()


def read_version_info(path: Path) -> VersionInfo:
    if not path.exists():
        raise FileNotFoundError(f"File non trovato: {path}")

    content = path.read_text(encoding="utf-8")
    code_match = re.search(r"(\bversionCode\s*=\s*)(\d+)", content)
    name_match = re.search(r'(\bversionName\s*=\s*)"(.*?)"', content)
    if code_match is None or name_match is None:
        raise ValueError(f"Impossibile leggere versionCode/versionName da {path}.")

    return VersionInfo(
        gradle_content=content,
        version_code=int(code_match.group(2)),
        version_name=name_match.group(2),
    )


def suggest_version_name(current_version: str, bump_kind: str) -> str:
    if bump_kind == "none":
        return current_version

    match = SEMVER_PATTERN.match(current_version)
    if match is not None:
        major = int(match.group("major"))
        minor = int(match.group("minor"))
        patch = int(match.group("patch"))
        suffix = match.group("suffix") or ""

        if bump_kind == "patch":
            patch += 1
        elif bump_kind == "minor":
            minor += 1
            patch = 0
        elif bump_kind == "major":
            major += 1
            minor = 0
            patch = 0

        # Keep version segments single-digit for this release scheme:
        # 1.6.9 -> 1.7.0 and 1.9.9 -> 2.0.0.
        if patch > 9:
            patch = 0
            minor += 1
        if minor > 9:
            minor = 0
            major += 1

        return f"{major}.{minor}.{patch}{suffix}"

    if bump_kind != "patch":
        return current_version

    matches = list(re.finditer(r"\d+", current_version))
    if not matches:
        return current_version

    last_match = matches[-1]
    next_value = int(last_match.group(0)) + 1
    return (
        current_version[: last_match.start()]
        + str(next_value)
        + current_version[last_match.end() :]
    )


def resolve_release_channel(version_name: str, explicit_channel: str | None = None) -> str:
    if explicit_channel:
        return explicit_channel
    normalized = version_name.lower()
    return "beta" if ("beta" in normalized or "alpha" in normalized) else "stable"


def load_update_config(path: Path) -> dict | None:
    if not path.exists():
        return None
    payload = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(payload, dict):
        raise ValueError(f"{path} deve contenere un oggetto JSON.")
    return payload


def build_channel_state(payload: dict, channel: str, fallback_version: str, fallback_code: int) -> ChannelState:
    channel_node = payload.get(channel)
    if not isinstance(channel_node, dict):
        channel_node = {}

    min_supported_version = channel_node.get("minSupportedVersion")
    if not isinstance(min_supported_version, str) or not min_supported_version.strip():
        min_supported_version = fallback_version

    min_supported_version_code = channel_node.get("minSupportedVersionCode")
    if not isinstance(min_supported_version_code, int) or min_supported_version_code <= 0:
        min_supported_version_code = fallback_code

    mandatory = bool(channel_node.get("mandatory", False))
    notes = channel_node.get("notes")
    if not isinstance(notes, str) or not notes.strip():
        notes = None

    return ChannelState(
        channel=channel,
        min_supported_version=min_supported_version,
        min_supported_version_code=min_supported_version_code,
        mandatory=mandatory,
        notes=notes,
    )


def prompt_text(message: str, default: str) -> str:
    while True:
        raw = input(f"{message} [premi Invio per confermare {default}]: ").strip()
        if raw:
            return raw
        if default:
            return default
        print("Inserisci un valore valido.")


def prompt_int(message: str, default: int) -> int:
    while True:
        raw = input(f"{message} [premi Invio per confermare {default}]: ").strip()
        if not raw:
            return default
        try:
            value = int(raw)
        except ValueError:
            print("Valore non valido, inserisci un numero intero.")
            continue
        if value <= 0:
            print("Il valore deve essere maggiore di zero.")
            continue
        return value


def prompt_bool(message: str, default: bool) -> bool:
    suffix = "S/n" if default else "s/N"
    while True:
        raw = input(f"{message} [{suffix}]: ").strip().lower()
        if not raw:
            return default
        if raw in {"s", "si", "y", "yes"}:
            return True
        if raw in {"n", "no"}:
            return False
        print("Risposta non valida. Usa S oppure N.")


def prompt_choice(message: str, options: dict[str, str], default: str) -> str:
    while True:
        print(message)
        for key, description in options.items():
            default_label = " (default)" if key == default else ""
            print(f"  {key}) {description}{default_label}")
        choice = input("Scelta: ").strip()
        if not choice:
            return default
        if choice in options:
            return choice
        print("Scelta non valida.")


def normalize_note_line(line: str) -> str:
    normalized = line.strip()
    normalized = re.sub(r"^[\-\*\u2022]+\s*", "", normalized)
    return normalized.strip()


def load_note_lines_from_file(path: Path) -> list[str]:
    if not path.exists():
        raise FileNotFoundError(f"File note non trovato: {path}")
    lines: list[str] = []
    for raw_line in path.read_text(encoding="utf-8").splitlines():
        normalized = normalize_note_line(raw_line)
        if normalized:
            lines.append(normalized)
    return lines


def collect_changelog_lines(args: argparse.Namespace, interactive: bool) -> list[str]:
    cli_lines = [normalize_note_line(line) for line in args.note if normalize_note_line(line)]
    if args.notes_file is not None:
        cli_lines.extend(load_note_lines_from_file(args.notes_file))
    if cli_lines:
        return cli_lines
    if args.skip_notes or not interactive:
        return []
    if not prompt_bool("Vuoi aggiornare anche note e changelog?", default=False):
        return []

    print(
        "\nInserisci le novita' per il changelog. "
        "Premi Invio su riga vuota oppure scrivi FINE per terminare."
    )
    lines: list[str] = []
    while True:
        try:
            raw_line = input("- ")
        except KeyboardInterrupt:
            print("\nInserimento note interrotto: uso le note raccolte finora.")
            break
        normalized = normalize_note_line(raw_line)
        if not normalized or normalized.upper() == "FINE":
            break
        lines.append(normalized)
    return lines


def parse_mandatory_override(raw_value: str | None) -> bool | None:
    if raw_value is None:
        return None
    return raw_value.lower() == "true"


def build_release_plan(
    args: argparse.Namespace,
    version_info: VersionInfo,
    config_payload: dict | None,
) -> ReleasePlan:
    interactive = not args.yes
    suggested_version_code = version_info.version_code + 1
    suggested_version_name = suggest_version_name(version_info.version_name, args.bump)

    if args.version_code is not None:
        new_version_code = args.version_code
    elif interactive:
        new_version_code = prompt_int("Inserisci il nuovo versionCode", suggested_version_code)
    else:
        new_version_code = suggested_version_code

    if args.version_name:
        new_version_name = args.version_name.strip()
    elif interactive:
        new_version_name = prompt_text(
            f"Inserisci la nuova versionName (attuale: {version_info.version_name})",
            suggested_version_name,
        )
    else:
        new_version_name = suggested_version_name

    if not new_version_name:
        raise ValueError("La versionName non puo' essere vuota.")

    channel = resolve_release_channel(new_version_name, args.channel)
    channel_state_before: ChannelState | None = None
    channel_state_after: ChannelState | None = None

    if config_payload is not None:
        channel_state_before = build_channel_state(
            payload=config_payload,
            channel=channel,
            fallback_version=version_info.version_name,
            fallback_code=version_info.version_code,
        )

        mandatory_override = parse_mandatory_override(args.mandatory)
        resulting_mandatory = (
            channel_state_before.mandatory
            if mandatory_override is None
            else mandatory_override
        )

        if args.sync_min_supported:
            resulting_min_version = args.min_supported_version or new_version_name
            resulting_min_code = args.min_supported_version_code or new_version_code
        else:
            resulting_min_version = (
                args.min_supported_version or channel_state_before.min_supported_version
            )
            resulting_min_code = (
                args.min_supported_version_code
                or channel_state_before.min_supported_version_code
            )

        if interactive:
            resulting_mandatory = prompt_bool(
                f"Impostare l'aggiornamento come mandatory per il canale {channel}?",
                default=resulting_mandatory,
            )

            min_supported_choice = prompt_choice(
                f"\nGestione minSupported per il canale {channel}:",
                options={
                    "1": "Lascia invariato",
                    "2": "Allinea al nuovo rilascio",
                    "3": "Inserisci valori custom",
                },
                default="1",
            )
            if min_supported_choice == "2":
                resulting_min_version = prompt_text(
                    "minSupportedVersion",
                    new_version_name,
                )
                resulting_min_code = prompt_int(
                    "minSupportedVersionCode",
                    new_version_code,
                )
            elif min_supported_choice == "3":
                resulting_min_version = prompt_text(
                    "minSupportedVersion",
                    channel_state_before.min_supported_version,
                )
                resulting_min_code = prompt_int(
                    "minSupportedVersionCode",
                    channel_state_before.min_supported_version_code,
                )

        channel_state_after = ChannelState(
            channel=channel,
            min_supported_version=resulting_min_version,
            min_supported_version_code=resulting_min_code,
            mandatory=resulting_mandatory,
            notes=channel_state_before.notes,
        )

    changelog_lines = collect_changelog_lines(args, interactive=interactive)

    return ReleasePlan(
        current_version_code=version_info.version_code,
        new_version_code=new_version_code,
        current_version_name=version_info.version_name,
        new_version_name=new_version_name,
        channel_state_before=channel_state_before,
        channel_state_after=channel_state_after,
        changelog_lines=changelog_lines,
    )


def format_update_notes(lines: list[str], now: dt.datetime) -> str:
    heading = now.strftime("%d %b %Y")
    bullets = "\n".join(f"- {line}" for line in lines)
    return f"Changelog {heading}:\n{bullets}"


def build_updated_gradle(content: str, new_version_code: int, new_version_name: str) -> str:
    updated = re.sub(
        r"(\bversionCode\s*=\s*)\d+",
        rf"\g<1>{new_version_code}",
        content,
        count=1,
    )
    updated = re.sub(
        r'(\bversionName\s*=\s*)"(.*?)"',
        rf'\g<1>"{new_version_name}"',
        updated,
        count=1,
    )
    return updated


def build_updated_config(
    payload: dict,
    channel_state: ChannelState | None,
    changelog_lines: list[str],
    now: dt.datetime,
) -> str:
    updated_payload = json.loads(json.dumps(payload))
    if channel_state is not None:
        channel_node = updated_payload.get(channel_state.channel)
        if not isinstance(channel_node, dict):
            updated_payload[channel_state.channel] = {}
            channel_node = updated_payload[channel_state.channel]

        channel_node["minSupportedVersion"] = channel_state.min_supported_version
        channel_node["minSupportedVersionCode"] = channel_state.min_supported_version_code
        channel_node["mandatory"] = channel_state.mandatory
        if changelog_lines:
            channel_node["notes"] = format_update_notes(changelog_lines, now)

    return json.dumps(updated_payload, ensure_ascii=False, indent=2) + "\n"


def build_changelog_entry(version_code: int, lines: list[str], now: dt.datetime) -> str:
    header = f"## [{version_code}] - {now.strftime('%Y-%m-%d')}"
    bullets = "\n".join(f"- {line}" for line in lines)
    return f"{header}\n{bullets}\n\n"


def build_updated_changelog(existing: str, version_code: int, lines: list[str], now: dt.datetime) -> str:
    if not lines:
        return existing

    entry = build_changelog_entry(version_code, lines, now)
    entry_pattern = re.compile(
        rf"(?ms)^## \[{re.escape(str(version_code))}\] - .*?(?=^## \[|\Z)"
    )
    if entry_pattern.search(existing):
        return entry_pattern.sub(entry, existing, count=1)
    return entry + existing


def print_summary(plan: ReleasePlan, config_exists: bool, changelog_path: Path) -> None:
    print("\n========================================")
    print("Riepilogo modifiche")
    print("========================================")
    print(f"- versionCode: {plan.current_version_code} -> {plan.new_version_code}")
    print(f"- versionName: {plan.current_version_name} -> {plan.new_version_name}")

    if config_exists and plan.channel_state_after is not None:
        before = plan.channel_state_before
        after = plan.channel_state_after
        print(f"- canale update-config: {after.channel}")
        if before is not None:
            print(f"- mandatory: {before.mandatory} -> {after.mandatory}")
            print(
                "- minSupportedVersion: "
                f"{before.min_supported_version} -> {after.min_supported_version}"
            )
            print(
                "- minSupportedVersionCode: "
                f"{before.min_supported_version_code} -> {after.min_supported_version_code}"
            )

    if plan.changelog_lines:
        print(f"- note/changelog: {len(plan.changelog_lines)} voce/i")
        for line in plan.changelog_lines:
            print(f"  - {line}")
    else:
        print("- note/changelog: invariati")

    if plan.changelog_lines:
        print(f"- CHANGELOG.md: verra' aggiornato in {changelog_path}")
    else:
        print("- CHANGELOG.md: nessuna modifica")


def write_text(path: Path, content: str) -> None:
    path.write_text(content, encoding="utf-8", newline="\n")


def apply_release_plan(
    args: argparse.Namespace,
    version_info: VersionInfo,
    config_payload: dict | None,
    plan: ReleasePlan,
) -> None:
    now = dt.datetime.now()
    updated_gradle = build_updated_gradle(
        content=version_info.gradle_content,
        new_version_code=plan.new_version_code,
        new_version_name=plan.new_version_name,
    )

    updated_config: str | None = None
    if config_payload is not None:
        updated_config = build_updated_config(
            payload=config_payload,
            channel_state=plan.channel_state_after,
            changelog_lines=plan.changelog_lines,
            now=now,
        )

    updated_changelog: str | None = None
    if plan.changelog_lines:
        existing_changelog = (
            args.changelog.read_text(encoding="utf-8")
            if args.changelog.exists()
            else ""
        )
        updated_changelog = build_updated_changelog(
            existing=existing_changelog,
            version_code=plan.new_version_code,
            lines=plan.changelog_lines,
            now=now,
        )

    print_summary(plan, config_exists=config_payload is not None, changelog_path=args.changelog)
    if args.dry_run:
        print("\nDry run completato: nessun file e' stato modificato.")
        return

    if not args.yes and not prompt_bool("Confermi le modifiche?", default=True):
        print("\nOperazione annullata. Nessun file modificato.")
        return

    write_text(args.file, updated_gradle)
    if updated_config is not None:
        write_text(args.update_config, updated_config)
    if updated_changelog is not None:
        write_text(args.changelog, updated_changelog)

    print("\n========================================")
    print("Fatto! Puoi verificare le modifiche con git diff.")
    print("========================================")


def main() -> int:
    args = parse_args()
    root_dir = Path(__file__).resolve().parent.parent
    os.chdir(root_dir)

    try:
        version_info = read_version_info(args.file)
        config_payload = load_update_config(args.update_config)

        print("========================================")
        print("       Bump App Version Script")
        print("========================================")
        print(f"\nVersion Code attuale: {version_info.version_code}")
        print(f"Version Name attuale: {version_info.version_name}")

        plan = build_release_plan(
            args=args,
            version_info=version_info,
            config_payload=config_payload,
        )
        apply_release_plan(
            args=args,
            version_info=version_info,
            config_payload=config_payload,
            plan=plan,
        )
        return 0
    except KeyboardInterrupt:
        print("\n\nOperazione annullata dall'utente. Nessun file modificato.")
        return 130
    except Exception as exc:
        print(f"\nErrore: {exc}", file=sys.stderr)
        return 1


if __name__ == "__main__":
    raise SystemExit(main())
