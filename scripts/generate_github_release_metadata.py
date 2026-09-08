#!/usr/bin/env python3
"""Create GitHub release notes from the same manifest used by the app."""
import argparse
from pathlib import Path
from release_metadata import load_json, read_apks


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--metadata", required=True, type=Path)
    parser.add_argument("--update-manifest", required=True, type=Path)
    parser.add_argument("--source-repo", required=True)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--github-env-output", required=True, type=Path)
    args = parser.parse_args()
    build = read_apks(args.metadata)
    manifest = load_json(args.update_manifest)
    notes = [f"# UniApp {build['versionName']}", "", f"Build: {build['versionCode']}", "",
             manifest.get("notes", ""), "", "## Download Android", ""]
    notes += [f"- [{abi}]({url})" for abi, url in manifest["downloadUrlsByAbi"].items()]
    notes += ["", "## Verifica dei file", "", "SHA-256:", "", "```text"]
    notes += [f"{digest}  {name}" for name, digest in build["sha256"].items()]
    notes += ["```", "", f"[Codice sorgente](https://github.com/{args.source_repo}/commit/{manifest['buildCommit']})", ""]
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text("\n".join(notes), encoding="utf-8")
    with args.github_env_output.open("a", encoding="utf-8") as handle:
        handle.write(f"RELEASE_TAG_NAME={build['tag']}\nRELEASE_TITLE=UniApp {build['versionName']} (build {build['versionCode']})\n")


if __name__ == "__main__":
    main()
