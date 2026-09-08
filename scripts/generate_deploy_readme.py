#!/usr/bin/env python3
"""Generate the distribution repository overview from its published manifest."""
import argparse
from pathlib import Path
from release_metadata import load_json


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--update-manifest", required=True, type=Path)
    parser.add_argument("--source-repo", required=True)
    parser.add_argument("--output", required=True, type=Path)
    args = parser.parse_args()
    release = load_json(args.update_manifest)
    content = ["# UniApp — distribuzione", "",
               "UniApp è un progetto indipendente per accedere ai servizi universitari. Questo repository contiene il sito di distribuzione e il manifest degli aggiornamenti Android.", "",
               f"Versione **{release['latestVersion']}**, build **{release['latestVersionCode']}**.", "",
               "## Download Android", ""]
    content += [f"- [{abi}]({url})" for abi, url in release["downloadUrlsByAbi"].items()]
    content += ["", "Gli APK e i relativi SHA-256 sono pubblicati nelle GitHub Releases. Su iOS la distribuzione agli utenti avviene tramite App Store.", "",
                "## Note di rilascio", "", release.get("notes", ""), "", "## Contenuto del repository", "",
                "- `update.json`: un solo rilascio Android, con versioni, requisiti e download per architettura.",
                "- `docs/`: sito statico generato dalla stessa revisione del codice compilato.",
                "- `release/`: metadati della build pubblicata.", "",
                f"[Codice e segnalazioni](https://github.com/{args.source_repo})", ""]
    args.output.write_text("\n".join(content), encoding="utf-8")


if __name__ == "__main__":
    main()
