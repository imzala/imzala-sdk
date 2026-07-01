#!/usr/bin/env node
/**
 * Downconvert the vendored OpenAPI 3.1 SSOT spec to OpenAPI 3.0.3.
 *
 * openapi-generator-cli (Java tool used by scripts/generate.sh) only
 * reliably supports OpenAPI 3.0.x input, while the imzala backend SSOT
 * spec (spec/openapi.v1.yaml) is authored in 3.1.0. This script runs
 * @apiture/openapi-down-convert via npx and then sanity-checks the
 * result: output must declare `openapi: 3.0.x` and must not have lost
 * any top-level path.
 *
 * Usage: node scripts/downconvert.mjs
 */
import { execFileSync } from 'node:child_process';
import { readFileSync, existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import path from 'node:path';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(__dirname, '..');

const INPUT = path.join(repoRoot, 'spec', 'openapi.v1.yaml');
const OUTPUT = path.join(repoRoot, 'spec', 'openapi.v1.3.0.yaml');

function countPaths(yamlText) {
  // Top-level path keys are 2-space-indented lines starting with "/".
  const lines = yamlText.split('\n');
  let inPaths = false;
  let count = 0;
  for (const line of lines) {
    if (/^paths:\s*$/.test(line)) {
      inPaths = true;
      continue;
    }
    if (inPaths) {
      if (/^\S/.test(line)) {
        // back to top-level, paths block ended
        break;
      }
      if (/^  \S/.test(line) && line.trimStart().startsWith('/')) {
        count++;
      }
    }
  }
  return count;
}

function main() {
  if (!existsSync(INPUT)) {
    console.error(`[downconvert] input spec not found: ${INPUT}`);
    process.exit(1);
  }

  console.log(`[downconvert] input:  ${path.relative(repoRoot, INPUT)}`);
  console.log(`[downconvert] output: ${path.relative(repoRoot, OUTPUT)}`);

  execFileSync(
    'npx',
    [
      '--yes',
      '@apiture/openapi-down-convert',
      '-i', INPUT,
      '-o', OUTPUT,
    ],
    { stdio: 'inherit', cwd: repoRoot }
  );

  const srcText = readFileSync(INPUT, 'utf8');
  const outText = readFileSync(OUTPUT, 'utf8');

  const outVersionMatch = outText.match(/^openapi:\s*["']?(\S+?)["']?\s*$/m);
  const outVersion = outVersionMatch ? outVersionMatch[1] : null;

  if (!outVersion || !outVersion.startsWith('3.0')) {
    console.error(`[downconvert] FAIL: output openapi version is "${outVersion}", expected 3.0.x`);
    process.exit(1);
  }

  const srcPaths = countPaths(srcText);
  const outPaths = countPaths(outText);

  console.log(`[downconvert] output openapi version: ${outVersion}`);
  console.log(`[downconvert] source paths: ${srcPaths}, output paths: ${outPaths}`);

  if (srcPaths === 0) {
    console.error('[downconvert] FAIL: could not count any paths in source spec (parser bug?)');
    process.exit(1);
  }

  if (srcPaths !== outPaths) {
    console.error(`[downconvert] FAIL: path count mismatch (source=${srcPaths}, output=${outPaths}) — paths lost during downconvert`);
    process.exit(1);
  }

  console.log('[downconvert] OK — 3.0.x output, path count preserved');
}

main();
