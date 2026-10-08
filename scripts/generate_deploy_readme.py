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
    content += ["", "Android, iOS, Windows, Linux e macOS hanno pubblicazioni indipendenti nelle GitHub Releases. Linux comprende Debian/Ubuntu e Arch Linux. Le IPA non firmate richiedono firma/provisioning.", "",
                "[Tutte le piattaforme e varianti](https://github.com/Anto426-Project/uniapp-upstream/releases)", "", "## Note di rilascio", "", release.get("notes", ""), "", "## Contenuto del repository", "",
                "- `update.json`: un solo rilascio Android, con versioni, requisiti e download per architettura.",
                "- `docs/`: sito statico di distribuzione, aggiornabile anche con correzioni indipendenti dagli APK.",
                "- `release/`: metadati della build pubblicata.", "",
                "- `release/platforms.json`: ultimi download verificati per piattaforma e variante Linux.", "",
                f"[Codice e segnalazioni](https://github.com/{args.source_repo})", ""]
    args.output.write_text("\n".join(content), encoding="utf-8")


if __name__ == "__main__":
    main()
