#!/usr/bin/env python3
"""
Synchronize and translate Compose Multiplatform string resources.

Source of truth:
- composeApp/src/commonMain/composeResources/values/strings.xml

Targets:
- composeApp/src/commonMain/composeResources/values-<lang>/strings.xml

By default this script:
1) Keeps existing target translations when present
2) Translates only missing/empty keys
3) Preserves placeholders and URLs
4) Keeps extra keys already present in target files
"""

from __future__ import annotations

import argparse
import re
import sys
import time
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Tuple
from xml.sax.saxutils import escape


DEFAULT_RESOURCES_ROOT = Path("composeApp/src/commonMain/composeResources")
DEFAULT_BASE_FILE = Path("values/strings.xml")
DEFAULT_SOURCE_LANG = "it"

FORMAT_TOKEN_RE = re.compile(r"%(?:\d+\$)?[-+#, 0]*\d*(?:\.\d+)?[a-zA-Z%]")
URL_TOKEN_RE = re.compile(r'https?://[^\s<>"]+')
HAS_LETTERS_RE = re.compile(r"[A-Za-zÀ-ÖØ-öø-ÿ]")


@dataclass
class SyncStats:
    language: str
    kept_existing: int = 0
    translated: int = 0
    copied_source: int = 0
    extras_kept: int = 0


class TranslationBackend:
    def translate(self, text: str, source_lang: str, target_lang: str) -> str:
        raise NotImplementedError


class PassthroughBackend(TranslationBackend):
    def translate(self, text: str, source_lang: str, target_lang: str) -> str:
        return text


class GoogleBackend(TranslationBackend):
    def __init__(self, sleep_ms: int = 0) -> None:
        try:
            from deep_translator import GoogleTranslator  # type: ignore
        except ImportError as exc:
            raise RuntimeError(
                "Missing dependency 'deep-translator'. Install it with: pip install deep-translator"
            ) from exc

        self._translator_cls = GoogleTranslator
        self._instances: Dict[Tuple[str, str], object] = {}
        self._cache: Dict[Tuple[str, str, str], str] = {}
        self._sleep_seconds = max(sleep_ms, 0) / 1000.0

    def _get_translator(self, source_lang: str, target_lang: str):
        key = (source_lang, target_lang)
        translator = self._instances.get(key)
        if translator is None:
            translator = self._translator_cls(source=source_lang, target=target_lang)
            self._instances[key] = translator
        return translator

    def translate(self, text: str, source_lang: str, target_lang: str) -> str:
        if target_lang == source_lang:
            return text

        cache_key = (source_lang, target_lang, text)
        cached = self._cache.get(cache_key)
        if cached is not None:
            return cached

        translator = self._get_translator(source_lang, target_lang)
        translated = translator.translate(text)
        if self._sleep_seconds > 0:
            time.sleep(self._sleep_seconds)

        if not isinstance(translated, str) or not translated.strip():
            translated = text

        self._cache[cache_key] = translated
        return translated


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Sync and translate composeResources string files."
    )
    parser.add_argument(
        "--resources-root",
        type=Path,
        default=DEFAULT_RESOURCES_ROOT,
        help="Root path containing values/ and values-<lang>/ folders.",
    )
    parser.add_argument(
        "--base-file",
        type=Path,
        default=DEFAULT_BASE_FILE,
        help="Base strings.xml path relative to --resources-root.",
    )
    parser.add_argument(
        "--source-lang",
        default=DEFAULT_SOURCE_LANG,
        help="Source language code used in base file.",
    )
    parser.add_argument(
        "--targets",
        default="",
        help=(
            "Comma-separated language codes (for values-<lang>). "
            "If omitted, all existing values-<lang> folders are processed."
        ),
    )
    parser.add_argument(
        "--provider",
        choices=("google", "none"),
        default="google",
        help="Translation backend. Use 'none' to only sync keys without translation.",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Retranslate even keys that already have a value in target files.",
    )
    parser.add_argument(
        "--drop-extra",
        action="store_true",
        help="Drop target keys not present in base file.",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would change without writing files.",
    )
    parser.add_argument(
        "--sleep-ms",
        type=int,
        default=0,
        help="Delay between API calls (useful to reduce rate limiting).",
    )
    parser.add_argument(
        "--verbose",
        action="store_true",
        help="Print per-key live progress while processing.",
    )
    parser.add_argument(
        "--progress-every",
        type=int,
        nargs="?",
        const=25,
        default=25,
        help=(
            "When not in --verbose mode, print a live progress line every N keys "
            "(set 0 to disable periodic progress)."
        ),
    )
    return parser.parse_args()


def load_strings(path: Path) -> List[Tuple[str, str]]:
    if not path.exists():
        return []

    tree = ET.parse(path)
    root = tree.getroot()
    if root.tag != "resources":
        raise ValueError(f"Unexpected root tag in {path}: {root.tag}")

    items: List[Tuple[str, str]] = []
    for elem in root.findall("string"):
        name = elem.get("name")
        if not name:
            continue
        value = elem.text or ""
        items.append((name, value))
    return items


def write_strings(path: Path, items: List[Tuple[str, str]]) -> None:
    lines: List[str] = ['<?xml version="1.0" encoding="UTF-8"?>', "<resources>"]
    for name, value in items:
        escaped_name = escape(name, {"\"": "&quot;"})
        escaped_value = escape_xml_value(value)
        lines.append(f'    <string name="{escaped_name}">{escaped_value}</string>')
    lines.append("</resources>")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def escape_xml_value(value: str) -> str:
    return escape(value, {"'": "&apos;", "\"": "&quot;"})


def detect_target_languages(resources_root: Path) -> List[str]:
    langs: List[str] = []
    for child in resources_root.iterdir():
        if not child.is_dir():
            continue
        if child.name.startswith("values-"):
            lang = child.name[len("values-") :].strip()
            if lang:
                langs.append(lang)
    return sorted(set(langs))


def build_backend(provider: str, sleep_ms: int) -> TranslationBackend:
    if provider == "none":
        return PassthroughBackend()
    if provider == "google":
        return GoogleBackend(sleep_ms=sleep_ms)
    raise ValueError(f"Unsupported provider: {provider}")


def protect_tokens(text: str) -> Tuple[str, Dict[str, str]]:
    token_map: Dict[str, str] = {}
    counter = 0

    def _replace(match: re.Match[str]) -> str:
        nonlocal counter
        token = f"__UNIAPP_TKN_{counter}__"
        token_map[token] = match.group(0)
        counter += 1
        return token

    protected = URL_TOKEN_RE.sub(_replace, text)
    protected = FORMAT_TOKEN_RE.sub(_replace, protected)
    return protected, token_map


def restore_tokens(text: str, token_map: Dict[str, str]) -> str:
    restored = text
    for token, original in token_map.items():
        restored = restored.replace(token, original)
    return restored


def should_translate(value: str) -> bool:
    if not value.strip():
        return False
    return bool(HAS_LETTERS_RE.search(value))


def translate_value(
    value: str,
    backend: TranslationBackend,
    source_lang: str,
    target_lang: str,
) -> str:
    if target_lang == source_lang:
        return value
    if not should_translate(value):
        return value

    protected, token_map = protect_tokens(value)
    translated = backend.translate(protected, source_lang=source_lang, target_lang=target_lang)
    if not translated.strip():
        translated = protected
    restored = restore_tokens(translated, token_map)
    return restored


def sync_language(
    base_items: List[Tuple[str, str]],
    target_items: List[Tuple[str, str]],
    language: str,
    source_lang: str,
    backend: TranslationBackend,
    force: bool,
    keep_extras: bool,
    verbose: bool,
    progress_every: int,
) -> Tuple[List[Tuple[str, str]], SyncStats]:
    target_map = dict(target_items)
    base_map = dict(base_items)
    stats = SyncStats(language=language)
    total = len(base_items)

    result: List[Tuple[str, str]] = []
    for index, (key, source_value) in enumerate(base_items, start=1):
        action_label = ""
        existing = target_map.get(key)
        if existing is not None and existing.strip() and not force:
            result.append((key, existing))
            stats.kept_existing += 1
            action_label = "kept"
        else:
            if language == source_lang or isinstance(backend, PassthroughBackend):
                translated_value = source_value
                stats.copied_source += 1
                action_label = "copied"
            else:
                translated_value = translate_value(
                    source_value,
                    backend=backend,
                    source_lang=source_lang,
                    target_lang=language,
                )
                stats.translated += 1
                action_label = "translated"
            result.append((key, translated_value))

        if verbose:
            print(f"[{language}] {index}/{total} {action_label} {key}", flush=True)
        elif progress_every > 0 and (index % progress_every == 0 or index == total):
            print(
                f"[{language}] progress {index}/{total} "
                f"kept={stats.kept_existing} translated={stats.translated} copied={stats.copied_source}",
                flush=True,
            )

    if keep_extras:
        for key, value in target_items:
            if key not in base_map:
                result.append((key, value))
                stats.extras_kept += 1

    return result, stats


def main() -> int:
    args = parse_args()
    resources_root: Path = args.resources_root
    base_file = resources_root / args.base_file

    if not base_file.exists():
        print(f"Base file not found: {base_file}", file=sys.stderr)
        return 2

    if args.targets.strip():
        target_languages = [x.strip() for x in args.targets.split(",") if x.strip()]
    else:
        target_languages = detect_target_languages(resources_root)

    if not target_languages:
        print("No target languages found.", file=sys.stderr)
        return 2

    try:
        backend = build_backend(args.provider, args.sleep_ms)
    except Exception as exc:
        print(str(exc), file=sys.stderr)
        return 2

    base_items = load_strings(base_file)
    if not base_items:
        print(f"No <string> entries found in base file: {base_file}", file=sys.stderr)
        return 2

    print(f"Base: {base_file} ({len(base_items)} keys)", flush=True)

    for language in target_languages:
        target_file = resources_root / f"values-{language}" / "strings.xml"
        target_items = load_strings(target_file)
        language_start = time.perf_counter()

        print(
            f"[{language}] start | base_keys={len(base_items)} target_keys={len(target_items)}",
            flush=True,
        )

        synced_items, stats = sync_language(
            base_items=base_items,
            target_items=target_items,
            language=language,
            source_lang=args.source_lang,
            backend=backend,
            force=args.force,
            keep_extras=not args.drop_extra,
            verbose=args.verbose,
            progress_every=max(0, args.progress_every),
        )

        elapsed_seconds = time.perf_counter() - language_start

        if args.dry_run:
            action = "DRY-RUN"
        else:
            write_strings(target_file, synced_items)
            action = "UPDATED"

        print(
            f"{action} values-{language}/strings.xml | "
            f"kept={stats.kept_existing} translated={stats.translated} "
            f"copied={stats.copied_source} extras_kept={stats.extras_kept} "
            f"total_out={len(synced_items)} elapsed={elapsed_seconds:.1f}s",
            flush=True,
        )

    if args.dry_run:
        print("Dry-run completed. No files were written.", flush=True)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
