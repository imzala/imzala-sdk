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
# typescript-axios (packages/node), python (packages/python), csharp
# (packages/dotnet), java (packages/java), and php (packages/php) are
# wired up as GA today. Other languages are listed here so a future task
# can flip them on one at a time without re-plumbing the script.

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
  "python:python:packages/python/generated:packageName=imzala_client"
  # csharp: httpclient library (Configuration+Api(configuration) ctor
  # pattern closest to axios/urllib3 — NOT the generichost default, which
  # requires ASP.NET Core DI). packageName is deliberately NOT
  # "Imzala.*" — the hand-written facade package (packages/dotnet/src)
  # ships a class literally named `Imzala`; if the vendored generated
  # code's own namespace also started with "Imzala.", the bare
  # `new Imzala(...)` call the task requires would fail to compile
  # (CS0118 "Imzala is a namespace but is used as a type") because C#
  # resolves any namespace segment reachable in the compilation ahead of
  # a same-named type brought into scope by `using` — confirmed
  # empirically while building this vertical. "ImzalaApiClient" avoids
  # the collision entirely.
  "csharp:csharp:packages/dotnet/generated:packageName=ImzalaApiClient,targetFramework=net8.0,library=httpclient,netCoreProjectFile=true"
  # java: native library (java.net.http, no extra HTTP dependency — Java
  # 11+ ships java.net.http.HttpClient in the JDK itself). invokerPackage
  # is deliberately NOT "org.imzala.*" bare — the hand-written facade
  # (packages/java/src) ships a class literally named `Imzala` in package
  # `org.imzala`; if the vendored generated code's own package were also
  # `org.imzala` (or a prefix collision like `org.imzala.client`), a
  # generated class could shadow/collide with the facade's `Imzala` type
  # or its resource classes on import. "org.imzala.client.generated" is a
  # distinct sub-package, same avoidance strategy as B3 (csharp)'s
  # "ImzalaApiClient" packageName. apiPackage/modelPackage are set
  # explicitly too — the Java generator does NOT cascade invokerPackage
  # into them (confirmed empirically: with invokerPackage alone, Api/Model
  # classes land in the tool's own default org.openapitools.client.{api,model}
  # while only ApiClient/Configuration/etc. follow invokerPackage — a
  # split-package footgun for a package meant to ship as org.imzala:*).
  "java:java:packages/java/generated:library=native,groupId=org.imzala,artifactId=imzala-client-generated,invokerPackage=org.imzala.client.generated,apiPackage=org.imzala.client.generated.api,modelPackage=org.imzala.client.generated.model"
  # php: guzzle library (the generator's only GA HTTP option besides the
  # beta psr-18 template). invokerPackage is deliberately NOT bare
  # "Imzala" — the hand-written facade (packages/php/src) ships a class
  # literally named `ImzalaClient` in namespace `Imzala`; "Imzala\\Client"
  # is a *sibling* sub-namespace (Imzala\Client\Api, Imzala\Client\Model,
  # ...), same avoidance strategy as B3 (csharp)'s "ImzalaApiClient" and
  # B5 (java)'s "org.imzala.client.generated" — never a namespace segment
  # the facade's own unqualified `Imzala\ImzalaClient` could collide with.
  # composerPackageName is deliberately NOT "imzala/imzala-php" — that's
  # the hand-written facade's own Packagist name (packages/php/composer.json);
  # this vendored/bundled generated code is never installed as a separate
  # Composer package (same "compile the generated sources directly into
  # the facade's own package" strategy as Java's build-helper-maven-plugin
  # add-source and C#'s <Compile Include>), so its composer.json is
  # present but effectively inert — only its *namespace* (PSR-4 prefix)
  # matters, and that comes from invokerPackage above, not this property.
  #
  # Backslash-escaping gotcha (found empirically — worth flagging for any
  # future PHP-generator re-run): passing invokerPackage as a *single*
  # literal backslash (`Imzala\Client`, i.e. 2 backslash chars in this
  # bash double-quoted array-literal source, `\\`) silently produces the
  # WRONG result — `namespace ImzalaClient\Api;` (segments concatenated,
  # inner backslash swallowed) — because openapi-generator-cli's own
  # --additional-properties value parsing treats a lone backslash as an
  # escape character itself, one layer *above* bash's own double-quote
  # escaping. Getting one literal namespace-separator backslash through
  # to the generator therefore requires FOUR backslash characters here
  # (bash collapses `\\\\` -> `\\`, i.e. two literal chars, which the
  # generator's own parser then collapses `\\` -> `\`, i.e. one literal
  # char) — confirmed by generating to a scratch dir and grepping the
  # emitted `namespace` line for both spellings.
  "php:php:packages/php/generated:invokerPackage=Imzala\\\\Client,composerPackageName=imzala/imzala-client-generated"
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
