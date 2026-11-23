#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
WEBSITE_DIR="${ROOT_DIR}/website"
DEFAULT_UPSTREAM_DIR="${ROOT_DIR}/../uniapp-upstream"

SYNC_UPSTREAM=false
BASE_PATH=""
UPSTREAM_DIR="${DEFAULT_UPSTREAM_DIR}"

print_help() {
  echo "Uso: $0 [opzioni]"
  echo ""
  echo "Opzioni:"
  echo "  --sync-upstream       Copia il build statico generato nella cartella docs/ di uniapp-upstream"
  echo "  --upstream-dir PATH   Percorso personalizzato del repository upstream (default: ../uniapp-upstream)"
  echo "  --base-path PATH      Base path per Next.js (default: /uniapp-upstream se --sync-upstream, altrimenti vuoto)"
  echo "  -h, --help            Mostra questo messaggio di aiuto"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --sync-upstream)
      SYNC_UPSTREAM=true
      shift
      ;;
    --upstream-dir)
      UPSTREAM_DIR="$2"
      shift 2
      ;;
    --base-path)
      BASE_PATH="$2"
      shift 2
      ;;
    -h|--help)
      print_help
      exit 0
      ;;
    *)
      echo "Opzione non riconosciuta: $1"
      print_help
      exit 1
      ;;
  esac
done

if [[ "${SYNC_UPSTREAM}" == "true" && -z "${BASE_PATH}" ]]; then
  BASE_PATH="/uniapp-upstream"
fi

echo "==> Compilazione sito statico Next.js in ${WEBSITE_DIR}..."
cd "${WEBSITE_DIR}"

if [[ ! -d "node_modules" ]]; then
  echo "--> Installazione dipendenze..."
  npm ci
fi

# Sincronizza il manifest più recente se esiste
if [[ -f "${UPSTREAM_DIR}/update.json" ]]; then
  echo "--> Sincronizzazione manifest da ${UPSTREAM_DIR}/update.json..."
  cp "${UPSTREAM_DIR}/update.json" "${WEBSITE_DIR}/public/update.json"
fi

echo "--> Generazione export statico (NEXT_PUBLIC_BASE_PATH='${BASE_PATH}')..."
rm -rf .next out
NEXT_PUBLIC_BASE_PATH="${BASE_PATH}" npm run build
NEXT_PUBLIC_BASE_PATH="${BASE_PATH}" npm run check:export

echo "✔ Build statico completato con successo in ${WEBSITE_DIR}/out"

if [[ "${SYNC_UPSTREAM}" == "true" ]]; then
  DOCS_DIR="${UPSTREAM_DIR}/docs"
  if [[ ! -d "${UPSTREAM_DIR}" ]]; then
    echo "❌ Errore: cartella upstream non trovata: ${UPSTREAM_DIR}"
    exit 1
  fi

  echo "==> Sincronizzazione in ${DOCS_DIR}..."
  mkdir -p "${DOCS_DIR}"
  rm -rf "${DOCS_DIR:?}"/*
  cp -a "${WEBSITE_DIR}/out/." "${DOCS_DIR}/"
  touch "${DOCS_DIR}/.nojekyll"
  touch "${UPSTREAM_DIR}/.nojekyll"

  echo "✔ Sincronizzazione completata in ${DOCS_DIR}!"
fi
