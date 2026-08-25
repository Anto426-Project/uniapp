from __future__ import annotations

import argparse
import json
import os
import re
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
    if not output_file:
        raise ValueError(f"{path} is missing outputFile.")

    apk_path = path.parent / output_file
    if not apk_path.exists():
        raise ValueError(f"APK not found at {apk_path}.")

    return {
        "applicationId": str(payload.get("applicationId", "")).strip() or "com.anto426.uniapp",
        "versionCode": str(first.get("versionCode", "")).strip() or "-",
        "versionName": str(first.get("versionName", "")).strip() or "-",
        "minSdk": str(payload.get("minSdkVersionForDexing", "")).strip() or "-",
        "outputFile": output_file,
        "apkPath": apk_path,
    }


def human_size(size_bytes: int) -> str:
    size_mb = size_bytes / (1024 * 1024)
    return f"{size_mb:.1f} MB"


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


def release_artifact_dir(release_channel: str) -> str:
    return "stable" if release_channel == "stable" else "beta"


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


def resolve_channel_manifest(root: dict, release_channel: str) -> dict:
    channels_root = root.get("channels") or root.get("channel")
    if not isinstance(channels_root, dict):
        return root if release_channel == "stable" else {}

    channel_node = channels_root.get(release_channel)
    if not isinstance(channel_node, dict):
        return {}

    track_node = channel_node.get(TRACK_NAME)
    if isinstance(track_node, dict):
        return track_node

    return channel_node


def resolve_channel_download_url(root: dict, release_channel: str) -> str | None:
    manifest = resolve_channel_manifest(root, release_channel)
    download_url = str(manifest.get("downloadUrl", "")).strip()
    return download_url or None


def render_badge(message: str, color: str, url: str | None, alt: str) -> str:
    badge_url = (
        "https://img.shields.io/static/v1"
        f"?label=&message={message}&color={color}&style=for-the-badge"
    )
    image = f'<img alt="{alt}" src="{badge_url}">'
    if url:
        return f'<a href="{url}">{image}</a>'
    return image


def render_download_link(label: str, url: str | None) -> str:
    if url:
        return f"[{label}]({url})"
    return f"{label}: `NON TROVATO`"


def render_screenshots(screenshots_dir: Path, base_path: str = "./assets/screenshots") -> str:
    if not screenshots_dir.exists() or not screenshots_dir.is_dir():
        return ""

    extensions = {".png", ".jpg", ".jpeg", ".webp"}
    files = sorted([
        f for f in os.listdir(screenshots_dir)
        if os.path.isfile(os.path.join(screenshots_dir, f)) and Path(f).suffix.lower() in extensions
    ])

    if not files:
        return ""

    html = '## Screenshot\n\n<p align="center">\n'
    for f in files:
        # We assume the README will be in the root, and screenshots in assets/screenshots
        src = f"{base_path}/{f}"
        html += f'  <img src="{src}" width="200" style="margin: 10px;" alt="Screenshot">\n'
    html += "</p>\n"
    return html


def render_readme(
    manifest_root: dict,
    metadata: dict,
    source_repo: str,
    deploy_repo: str,
    icon_path: str,
    release_channel: str,
    screenshots_dir: Path | None = None,
    virustotal_url: str | None = None,
) -> str:
    manifest = resolve_scoped_manifest(manifest_root, release_channel)
    release_channel_label = "beta" if release_channel == "beta" else "stabile"
    latest_version = str(manifest.get("latestVersion", metadata["versionName"]))
    description = str(manifest.get("description", "")).strip()
    published_at = str(manifest.get("publishedAt", "-"))
    min_supported = str(manifest.get("minSupportedVersion", "-"))
    mandatory = str(manifest.get("mandatory", False)).lower()
    app_enabled = str(manifest.get("appEnabled", True)).lower()
    notes = str(manifest.get("notes", "")).strip()
    download_url = str(manifest.get("downloadUrl", "")).strip()
    stable_download_url = resolve_channel_download_url(manifest_root, "stable")
    beta_download_url = resolve_channel_download_url(manifest_root, "beta")
    apk_size = human_size(metadata["apkPath"].stat().st_size)
    output_file = metadata["outputFile"]
    artifact_dir = release_artifact_dir(release_channel)
    metadata_path = f"./src/release/{artifact_dir}/output-metadata.json"
    stable_badge = render_badge(
        "STABILE%20APK" if stable_download_url else "STABILE%20NON%20TROVATO",
        "1f6f5f" if stable_download_url else "6b7280",
        stable_download_url,
        "APK stabile" if stable_download_url else "APK stabile non trovato",
    )
    beta_badge = render_badge(
        "BETA%20APK" if beta_download_url else "BETA%20NON%20TROVATO",
        "d97706" if beta_download_url else "6b7280",
        beta_download_url,
        "APK beta" if beta_download_url else "APK beta non trovato",
    )

    screenshots_section = ""
    if screenshots_dir:
        screenshots_section = render_screenshots(screenshots_dir)

    beta_flag = ""
    if release_channel == "beta":
        beta_flag = """
<p align="center">
  <img alt="Canale beta" src="https://img.shields.io/static/v1?label=&message=CANALE%20BETA&color=d97706&style=for-the-badge">
</p>
"""

    description_section = ""
    if description:
        description_section = f"""
## Descrizione
{description}
"""
    
    virustotal_section = ""
    if virustotal_url:
        # Sanitize and extract a sensible URL if the provided value contains
        # extra quotes, whitespace or surrounding text. Wrap in angle
        # brackets to make Markdown links robust against special chars.
        url = str(virustotal_url).strip()
        url = url.strip('"').strip("'")
        if not url.startswith("http"):
            m = re.search(r"https?://[^\s\'\"\)]+", url)
            if m:
                url = m.group(0)

        safe_url = f"<{url}>" if url else url

        virustotal_section = f"""
## Security Scan
Quest'app e' stata scansionata per potenziali minacce.
- [Report Analisi VirusTotal]({safe_url})

[![VirusTotal Scan Result](https://img.shields.io/badge/VirusTotal-Scan_Report-blue)]({safe_url})
"""

    return f"""<p align="center">
  <img src="{icon_path}" alt="UniApp icon" width="120" height="120">
</p>

<h1 align="center">UniApp Upstream</h1>

{beta_flag}

<p align="center">
  Repository di distribuzione per il canale release Android di <strong>UniApp</strong>.
  Qui vengono pubblicati l'APK corrente, il manifest degli aggiornamenti usato dall'app e i metadati della release.
</p>

<p align="center">
  {stable_badge}
  {beta_badge}
  <a href="./update.json"><img alt="Manifest JSON" src="https://img.shields.io/static/v1?label=&message=MANIFEST%20JSON&color=cb5a2e&style=for-the-badge"></a>
</p>

{description_section}

{virustotal_section}

{screenshots_section}

## Panoramica

Questo e' il repository di deploy delle build Android di UniApp.
E' pensato per restare semplice, stabile e leggibile anche da script:

- `src/release/stable/` contiene APK e metadati stabili
- `src/release/beta/` contiene APK e metadati beta
- `update.json` espone i manifest separati per canale sotto `channels`
- `README.md` riassume la release pubblica corrente

## Release Corrente

| Campo | Valore |
| --- | --- |
| App | UniApp |
| Repository | `{deploy_repo}` |
| Versione corrente | `{latest_version}` |
| Canale release | `{release_channel_label}` |
| Version code | `{metadata["versionCode"]}` |
| Pubblicata il | `{published_at}` |
| Versione minima supportata | `{min_supported}` |
| Aggiornamento obbligatorio | `{mandatory}` |
| App abilitata | `{app_enabled}` |
| Package name | `{metadata["applicationId"]}` |
| Min SDK | `{metadata["minSdk"]}` |
| File APK | `src/release/{artifact_dir}/{output_file}` |
| Dimensione APK | `{apk_size}` |

## Link Rapidi

- {render_download_link("Scarica APK corrente", download_url or None)}
- [Metadati del canale corrente]({metadata_path})
- [Percorso APK del canale corrente](./src/release/{artifact_dir}/{output_file})
- {render_download_link("APK stabile", stable_download_url)}
- {render_download_link("APK beta", beta_download_url)}
- [Apri il manifest aggiornamenti](./update.json)

## Note Di Rilascio

{notes if notes else "Nessuna nota di rilascio disponibile per questa build."}

## Struttura Repository

```text
assets/
  uniapp-icon.webp
  screenshots/
    ...
src/
  release/
    stable/
      ...
    beta/
      ...
update.json
README.md
```

## Feed Aggiornamenti

L'app legge `update.json` per capire se esiste una build piu' recente.
I campi principali sono:

- `channels.stable.release`
- `channels.beta.release`
- `latestVersion`
- `downloadUrl`
- `publishedAt`
- `buildCommit`

`downloadUrl` punta attualmente a:

`{download_url}`

## Flusso Di Pubblicazione

Questo repository viene aggiornato automaticamente dal workflow GitHub Actions del repository principale di UniApp.
Ogni pubblicazione aggiorna:

1. l'APK sotto `src/release/stable/` oppure `src/release/beta/`
2. il relativo `output-metadata.json`
3. `update.json`
4. questo `README.md`

## Note

- Questo repository e' un endpoint di release, non il repository principale di sviluppo.
- I file pubblicati possono cambiare a ogni nuova release.
"""


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate deploy repository README.md.")
    parser.add_argument("--update-manifest", required=True, type=Path)
    parser.add_argument("--metadata", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--source-repo", required=True)
    parser.add_argument("--deploy-repo", required=True)
    parser.add_argument("--icon-path", default="./assets/uniapp-icon.webp")
    parser.add_argument("--screenshots-dir", type=Path)
    parser.add_argument("--release-channel")
    parser.add_argument("--virustotal-url")
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
    readme = render_readme(
        manifest_root=manifest,
        metadata=metadata,
        source_repo=args.source_repo,
        deploy_repo=args.deploy_repo,
        icon_path=args.icon_path,
        release_channel=release_channel,
        screenshots_dir=args.screenshots_dir,
        virustotal_url=args.virustotal_url,
    )

    args.output.parent.mkdir(parents=True, exist_ok=True)
    with args.output.open("w", encoding="utf-8", newline="\n") as handle:
        handle.write(readme)

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
