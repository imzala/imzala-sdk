#!/usr/bin/env bash
# Generate server SDK clients from the downconverted OpenAPI 3.0.3 spec via
# openapi-generator-cli (Java tool, invoked through npx).
#
# Usage:
#   bash scripts/generate.sh              # generate all languages in LANGS
#   bash scripts/generate.sh typescript-axios   # generate only one language
#
# Prereqs: run `node scripts/downconvert.mjs` first — this script expects
# spec/openapi.v1.3.0.yaml to already exist.
#
# Only typescript-axios is wired up as GA today (packages/node). Other
# languages are listed here so a future task can flip them on one at a
# time without re-plumbing the script.

set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$REPO_ROOT"

SPEC="spec/openapi.v1.3.0.yaml"

if [ ! -f "$SPEC" ]; then
  echo "[generate] $SPEC not found — run 'node scripts/downconvert.mjs' first" >&2
  exit 1
fi

# lang:generator:output-dir:extra-additional-properties
LANG_TABLE=(
  "typescript-axios:typescript-axios:packages/node/generated:supportsES6=true,npmName=@imzala/server-sdk-node,useSingleRequestParameter=true"
  # Future langs — not run yet, kept here so the next task just uncomments:
  # "python:python:packages/python/generated:packageName=imzala_server_sdk"
  # "csharp:csharp:packages/csharp/generated:packageName=Imzala.ServerSdk"
  # "php:php:packages/php/generated:invokerPackage=Imzala\\\\ServerSdk"
  # "java:java:packages/java/generated:invokerPackage=org.imzala.serversdk,artifactId=imzala-server-sdk"
)

RUN_ONLY="${1:-}"

for entry in "${LANG_TABLE[@]}"; do
  IFS=':' read -r key generator outdir extra_props <<< "$entry"

  if [ -n "$RUN_ONLY" ] && [ "$RUN_ONLY" != "$key" ]; then
    continue
  fi

  echo "[generate] === $key ($generator) -> $outdir ==="
  rm -rf "$outdir"
  mkdir -p "$outdir"

  npx --yes @openapitools/openapi-generator-cli generate \
    -i "$SPEC" \
    -g "$generator" \
    -o "$outdir" \
    --additional-properties="$extra_props"

  echo "[generate] === $key done ==="
done

echo "[generate] all requested targets complete"
