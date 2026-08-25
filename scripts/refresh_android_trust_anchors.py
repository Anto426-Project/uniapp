from __future__ import annotations

import argparse
import base64
import hashlib
import json
import re
import ssl
import subprocess
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

DEFAULT_HOSTS = (
    "app.unimol.it",
    "unimol.esse3.cineca.it",
    "sync.faufas.it",
    "trasporti.unimol.it",
    "learn.unimol.it",
    "api.github.com",
    "github.com",
    "raw.githubusercontent.com",
    "avatars.githubusercontent.com",
    "objects.githubusercontent.com",
    "github-releases.githubusercontent.com",
    "codeload.github.com",
    "tile.openstreetmap.org",
    "router.project-osrm.org",
    "uniaappauthorization.antobot.info",
)
DEFAULT_OUTPUT = Path("trusted_ca_bundle.pem")
DEFAULT_METADATA_OUTPUT = Path("trusted_ca_bundle_meta.json")
DEFAULT_MANIFEST_OUTPUT = Path("trusted_ca_bundle_manifest.json")
DEFAULT_VALID_DAYS = 14
SIGNATURE_ALGORITHM = "SHA384withECDSA"
PEM_CERT_PATTERN = re.compile(
    r"-----BEGIN CERTIFICATE-----\s+.*?-----END CERTIFICATE-----",
    flags=re.DOTALL,
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Fetch TLS certificate chains for known API hosts, write a PEM bundle, "
            "and optionally sign a manifest for runtime Android loading."
        )
    )
    parser.add_argument(
        "--host",
        action="append",
        dest="hosts",
        help="Override host list. Can be provided multiple times.",
    )
    parser.add_argument("--port", type=int, default=443)
    parser.add_argument("--timeout", type=int, default=20)
    parser.add_argument("--output", type=Path, default=DEFAULT_OUTPUT)
    parser.add_argument("--metadata-output", type=Path, default=DEFAULT_METADATA_OUTPUT)
    parser.add_argument("--manifest-output", type=Path)
    parser.add_argument("--private-key-file", type=Path)
    parser.add_argument("--public-key-output", type=Path)
    parser.add_argument("--valid-days", type=int, default=DEFAULT_VALID_DAYS)
    parser.add_argument("--key-id", help="Stable key identifier written to the manifest.")
    parser.add_argument(
        "--reuse-existing-on-fetch-failure",
        action="store_true",
        help="Reuse an existing bundle for a host only when a fresh fetch fails.",
    )
    return parser.parse_args()


def fetch_host_certificates(host: str, port: int, timeout_seconds: int) -> list[str]:
    command = [
        "openssl",
        "s_client",
        "-showcerts",
        "-servername",
        host,
        "-connect",
        f"{host}:{port}",
    ]
    try:
        result = subprocess.run(
            command,
            input="",
            text=True,
            capture_output=True,
            timeout=timeout_seconds,
            check=False,
        )
    except FileNotFoundError as error:
        raise RuntimeError(
            "OpenSSL executable not found. Install OpenSSL and ensure it is in PATH."
        ) from error

    payload = f"{result.stdout}\n{result.stderr}"
    certificates = PEM_CERT_PATTERN.findall(payload)
    if not certificates:
        raise RuntimeError(
            f"No certificates extracted for host {host}. OpenSSL exit code: {result.returncode}"
        )

    return certificates


def normalize_certificate_pem(raw_pem: str) -> tuple[str, str]:
    der = ssl.PEM_cert_to_DER_cert(raw_pem)
    fingerprint = hashlib.sha256(der).hexdigest()
    normalized_pem = ssl.DER_cert_to_PEM_cert(der).strip() + "\n"
    return fingerprint, normalized_pem


def load_existing_host_certificates(bundle_path: Path, metadata_path: Path) -> dict[str, list[str]]:
    if not bundle_path.exists() or not metadata_path.exists():
        return {}

    try:
        bundle_text = bundle_path.read_text(encoding="utf-8")
        metadata = json.loads(metadata_path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as error:
        print(
            f"warning: failed to load existing trust anchor files for fallback reuse: {error}",
            file=sys.stderr,
        )
        return {}

    pem_by_fingerprint: dict[str, str] = {}
    for raw_pem in PEM_CERT_PATTERN.findall(bundle_text):
        try:
            fingerprint, normalized_pem = normalize_certificate_pem(raw_pem)
        except ValueError:
            continue
        pem_by_fingerprint[fingerprint] = normalized_pem

    host_certificates: dict[str, list[str]] = {}
    certificate_entries = metadata.get("certificates", [])
    if not isinstance(certificate_entries, list):
        return host_certificates

    for entry in certificate_entries:
        if not isinstance(entry, dict):
            continue
        fingerprint = entry.get("sha256")
        if not isinstance(fingerprint, str):
            continue
        pem = pem_by_fingerprint.get(fingerprint)
        if pem is None:
            continue
        hosts = entry.get("hosts", [])
        if not isinstance(hosts, list):
            continue
        for host in hosts:
            if not isinstance(host, str):
                continue
            host_certificates.setdefault(host, []).append(pem)

    return host_certificates


def build_bundle(
    hosts: list[str],
    port: int,
    timeout_seconds: int,
    fallback_certificates: dict[str, list[str]] | None = None,
) -> tuple[str, dict]:
    pem_by_fingerprint: dict[str, str] = {}
    hosts_by_fingerprint: dict[str, set[str]] = {}
    reused_hosts: dict[str, str] = {}
    failures: dict[str, str] = {}
    fallback_certificates = fallback_certificates or {}

    for host in hosts:
        try:
            certificates = fetch_host_certificates(
                host=host,
                port=port,
                timeout_seconds=timeout_seconds,
            )
        except RuntimeError as error:
            certificates = fallback_certificates.get(host, [])
            if certificates:
                reused_hosts[host] = str(error)
                print(
                    (
                        f"warning: reused existing certificates for host {host} because "
                        f"fresh fetch failed: {error}"
                    ),
                    file=sys.stderr,
                )
            else:
                failures[host] = str(error)
                continue

        for cert in certificates:
            fingerprint, normalized_pem = normalize_certificate_pem(cert)
            pem_by_fingerprint[fingerprint] = normalized_pem
            hosts_by_fingerprint.setdefault(fingerprint, set()).add(host)

    if failures:
        details = "; ".join(f"{host}: {reason}" for host, reason in sorted(failures.items()))
        raise RuntimeError(f"Failed to fetch certificates for hosts without fallback data: {details}")

    ordered_fingerprints = sorted(pem_by_fingerprint.keys())
    bundle = "\n".join(pem_by_fingerprint[fingerprint].strip() for fingerprint in ordered_fingerprints)
    if bundle:
        bundle += "\n"

    metadata = {
        "generatedAt": datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z"),
        "hosts": hosts,
        "certificateCount": len(ordered_fingerprints),
        "certificates": [
            {
                "sha256": fingerprint,
                "hosts": sorted(hosts_by_fingerprint.get(fingerprint, set())),
            }
            for fingerprint in ordered_fingerprints
        ],
    }

    return bundle, metadata


def write_if_changed(path: Path, content: str) -> bool:
    existing = path.read_text(encoding="utf-8") if path.exists() else None
    if existing == content:
        return False
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(content, encoding="utf-8", newline="\n")
    return True


def sha256_hex(content: str) -> str:
    return hashlib.sha256(content.encode("utf-8")).hexdigest()


def utc_now_iso() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")


def canonical_manifest_payload(manifest: dict) -> str:
    hosts = manifest.get("hosts", [])
    if not isinstance(hosts, list):
        hosts = []

    lines = [
        "UniApp TLS certificate bundle manifest v1",
        f"schemaVersion={manifest['schemaVersion']}",
        f"bundlePath={manifest['bundlePath']}",
        f"metadataPath={manifest['metadataPath']}",
        f"bundleSha256={manifest['bundleSha256']}",
        f"metadataSha256={manifest['metadataSha256']}",
        f"certificateCount={manifest['certificateCount']}",
        f"generatedAt={manifest['generatedAt']}",
        f"expiresAt={manifest['expiresAt']}",
        f"keyId={manifest['keyId']}",
        f"signatureAlgorithm={manifest['signatureAlgorithm']}",
        f"hosts={','.join(str(host) for host in hosts)}",
    ]
    return "\n".join(lines) + "\n"


def run_openssl(command: list[str], input_bytes: bytes | None = None) -> bytes:
    try:
        result = subprocess.run(
            command,
            input=input_bytes,
            capture_output=True,
            check=False,
        )
    except FileNotFoundError as error:
        raise RuntimeError(
            "OpenSSL executable not found. Install OpenSSL and ensure it is in PATH."
        ) from error

    if result.returncode != 0:
        stderr = result.stderr.decode("utf-8", errors="replace").strip()
        raise RuntimeError(f"OpenSSL failed: {stderr or 'unknown error'}")

    return result.stdout


def export_public_key_pem(private_key_file: Path) -> str:
    payload = run_openssl(["openssl", "pkey", "-in", str(private_key_file), "-pubout"])
    return payload.decode("utf-8").replace("\r\n", "\n")


def sign_payload(payload: str, private_key_file: Path) -> str:
    try:
        # Attempt deterministic ECDSA (RFC 6979) to ensure stable signature bytes across runs
        signature = run_openssl(
            ["openssl", "dgst", "-sha384", "-sign", str(private_key_file), "-sigopt", "rfc6979:1"],
            input_bytes=payload.encode("utf-8"),
        )
    except Exception:
        # Fallback to standard ECDSA if the host OpenSSL version does not support rfc6979
        signature = run_openssl(
            ["openssl", "dgst", "-sha384", "-sign", str(private_key_file)],
            input_bytes=payload.encode("utf-8"),
        )
    return base64.b64encode(signature).decode("ascii")


def build_signed_manifest(
    *,
    bundle_text: str,
    metadata_text: str,
    metadata: dict,
    bundle_path: Path,
    metadata_path: Path,
    private_key_file: Path,
    valid_days: int,
    key_id: str | None,
    public_key_pem: str,
) -> dict:
    if valid_days <= 0:
        raise RuntimeError("--valid-days must be greater than zero.")

    generated_at = metadata.get("generatedAt")
    if not isinstance(generated_at, str) or not generated_at:
        generated_at = utc_now_iso()

    expires_at = (
        datetime.now(timezone.utc) + timedelta(days=valid_days)
    ).isoformat(timespec="seconds").replace("+00:00", "Z")
    resolved_key_id = key_id or hashlib.sha256(public_key_pem.encode("utf-8")).hexdigest()[:16]
    hosts = metadata.get("hosts", [])
    if not isinstance(hosts, list):
        hosts = []

    manifest = {
        "schemaVersion": 1,
        "bundlePath": bundle_path.name,
        "metadataPath": metadata_path.name,
        "bundleSha256": sha256_hex(bundle_text),
        "metadataSha256": sha256_hex(metadata_text),
        "certificateCount": int(metadata.get("certificateCount", 0)),
        "generatedAt": generated_at,
        "expiresAt": expires_at,
        "keyId": resolved_key_id,
        "signatureAlgorithm": SIGNATURE_ALGORITHM,
        "hosts": [str(host) for host in hosts],
    }
    manifest["signature"] = sign_payload(
        canonical_manifest_payload(manifest),
        private_key_file=private_key_file,
    )
    return manifest


def main() -> int:
    args = parse_args()
    hosts = args.hosts or list(DEFAULT_HOSTS)
    fallback_certificates = (
        load_existing_host_certificates(
            bundle_path=args.output,
            metadata_path=args.metadata_output,
        )
        if args.reuse_existing_on_fetch_failure
        else {}
    )

    bundle, metadata = build_bundle(
        hosts=hosts,
        port=args.port,
        timeout_seconds=args.timeout,
        fallback_certificates=fallback_certificates,
    )

    bundle_changed = write_if_changed(args.output, bundle)

    if args.metadata_output.exists() and not bundle_changed:
        metadata_text = args.metadata_output.read_text(encoding="utf-8")
        metadata_changed = False
    else:
        metadata["generatedAt"] = datetime.now(timezone.utc).isoformat(timespec="seconds").replace("+00:00", "Z")
        metadata_text = json.dumps(metadata, ensure_ascii=False, indent=2) + "\n"
        metadata_changed = write_if_changed(args.metadata_output, metadata_text)

    manifest_changed = False
    public_key_changed = False
    if args.manifest_output is not None:
        if args.private_key_file is None:
            raise RuntimeError("--private-key-file is required when writing a signed manifest.")

        public_key_pem = export_public_key_pem(args.private_key_file)
        metadata_for_manifest = json.loads(metadata_text)
        manifest = build_signed_manifest(
            bundle_text=bundle,
            metadata_text=metadata_text,
            metadata=metadata_for_manifest,
            bundle_path=args.output,
            metadata_path=args.metadata_output,
            private_key_file=args.private_key_file,
            valid_days=args.valid_days,
            key_id=args.key_id,
            public_key_pem=public_key_pem,
        )
        manifest_text = json.dumps(manifest, ensure_ascii=False, indent=2) + "\n"
        manifest_changed = write_if_changed(args.manifest_output, manifest_text)

        if args.public_key_output is not None:
            public_key_changed = write_if_changed(args.public_key_output, public_key_pem)

    any_changed = bundle_changed or metadata_changed or manifest_changed or public_key_changed

    print(
        json.dumps(
            {
                "bundlePath": str(args.output).replace("\\", "/"),
                "metadataPath": str(args.metadata_output).replace("\\", "/"),
                "manifestPath": (
                    str(args.manifest_output).replace("\\", "/")
                    if args.manifest_output is not None
                    else None
                ),
                "bundleChanged": bundle_changed,
                "metadataChanged": metadata_changed,
                "manifestChanged": manifest_changed,
                "publicKeyChanged": public_key_changed,
                "anyChanged": any_changed,
                "hostCount": len(hosts),
                "certificateCount": metadata["certificateCount"],
            }
        )
    )

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
