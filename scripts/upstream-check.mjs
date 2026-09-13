#!/usr/bin/env node
/**
 * upstream-check.mjs: consumer-side lock for the vendored OpenAPI spec.
 *
 * spec/openapi.v1.yaml is a verbatim copy of the backend's published public
 * artefact (imzala/imzala-service, openapi/openapi.v1.public.yaml). The
 * backend also publishes a version lock (openapi/openapi.v1.lock.json,
 * shape { version, sha256, previous[] }). spec/.upstream.json records which
 * backend version this repo was synced against.
 *
 * Two independent checks, each with its own failure reason:
 *
 *   1. Local integrity (no network): hash of spec/openapi.v1.yaml must equal
 *      .upstream.json `public_sha256`, and its info.version must equal
 *      .upstream.json `version`. A mismatch means someone edited the vendored
 *      spec by hand instead of re-syncing it.
 *
 *   2. Upstream alignment (network, or a local copy of the backend lock):
 *      .upstream.json `version` + `upstream_ssot_sha256` must equal the
 *      backend lock's `version` + `sha256`. A mismatch means the backend has
 *      moved on and this repo's copy is stale.
 *
 * Why two hashes: the backend lock hashes its full source-of-truth spec, while
 * this repo only receives the public derivative (internal operations removed).
 * The two bodies differ by construction, so a single hash could never match
 * both. `upstream_ssot_sha256` follows the backend lock; `public_sha256`
 * protects the copy we actually ship.
 *
 * Hash method is identical to the backend's (scripts/openapi-version-lock-check.mjs):
 * parse YAML, drop info.version, sort keys recursively, JSON.stringify, SHA-256.
 *
 * Upstream branch is configurable. Default comes from .upstream.json
 * `source.branch` (currently "test": the lock and public artefact are
 * published there first and promoted to "main" later). Override with
 * IMZALA_UPSTREAM_BRANCH or `--branch <name>`. Once the artefacts are on
 * "main", switch `source.branch` in .upstream.json and nothing else changes.
 *
 * The backend repository is private: set IMZALA_UPSTREAM_TOKEN (or
 * GITHUB_TOKEN) with read access. A missing token is an error, not a pass.
 * Any failure (network, malformed data, mismatch) exits 1. Unreachable never
 * means aligned.
 *
 * Usage:
 *   node scripts/upstream-check.mjs
 *   node scripts/upstream-check.mjs --branch main
 *   node scripts/upstream-check.mjs --remote ./path/to/openapi.v1.lock.json   # local copy, no network
 *   node scripts/upstream-check.mjs --local spec/.upstream.json --spec spec/openapi.v1.yaml
 */
import { createHash } from 'node:crypto';
import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import yaml from 'js-yaml';

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const repoRoot = path.resolve(__dirname, '..');

const DEFAULT_LOCAL = path.join(repoRoot, 'spec', '.upstream.json');
const DEFAULT_SPEC = path.join(repoRoot, 'spec', 'openapi.v1.yaml');
const DEFAULT_REPO = 'imzala/imzala-service';
const DEFAULT_LOCK_PATH = 'openapi/openapi.v1.lock.json';
const DEFAULT_BRANCH = 'test';

const SHA256_RE = /^[0-9a-f]{64}$/;

function isRecord(v) {
  return Boolean(v) && typeof v === 'object' && !Array.isArray(v);
}

function canonicalize(node) {
  if (Array.isArray(node)) return node.map(canonicalize);
  if (node && typeof node === 'object') {
    const out = {};
    for (const key of Object.keys(node).sort()) out[key] = canonicalize(node[key]);
    return out;
  }
  return node;
}

/** SHA-256 of the spec body with info.version excluded (same method as the backend lock). */
export function hashSpecBody(doc) {
  const clone = JSON.parse(JSON.stringify(doc));
  if (clone.info) delete clone.info.version;
  return createHash('sha256').update(JSON.stringify(canonicalize(clone)), 'utf8').digest('hex');
}

function isValidLocal(local) {
  return (
    isRecord(local) &&
    typeof local.version === 'string' &&
    local.version.length > 0 &&
    SHA256_RE.test(String(local.upstream_ssot_sha256)) &&
    SHA256_RE.test(String(local.public_sha256))
  );
}

const MALFORMED_LOCAL = {
  ok: false,
  reason: 'MALFORMED_LOCAL',
  message:
    'spec/.upstream.json could not be read or is missing one of ' +
    '{ version, upstream_ssot_sha256, public_sha256 }.',
};

/** Check 1: the vendored spec matches what .upstream.json says was copied. Pure, no I/O. */
export function verifyLocalSpec(doc, local) {
  if (!isValidLocal(local)) return MALFORMED_LOCAL;
  if (!isRecord(doc) || !isRecord(doc.info)) {
    return {
      ok: false,
      reason: 'MALFORMED_SPEC',
      message: 'spec/openapi.v1.yaml could not be parsed or has no info block.',
    };
  }
  const declared = doc.info.version;
  if (declared !== local.version) {
    return {
      ok: false,
      reason: 'LOCAL_VERSION_MISMATCH',
      message:
        `spec/openapi.v1.yaml declares info.version ${declared} but spec/.upstream.json records ` +
        `${local.version}. The two move together; re-sync the spec instead of editing either by hand.`,
    };
  }
  const actual = hashSpecBody(doc);
  if (actual !== local.public_sha256) {
    return {
      ok: false,
      reason: 'LOCAL_SPEC_MODIFIED',
      message:
        `spec/openapi.v1.yaml was modified after it was synced (body hash ${actual.slice(0, 12)} ` +
        `differs from recorded public_sha256 ${local.public_sha256.slice(0, 12)}). ` +
        'The vendored spec is not hand-edited; copy the backend public artefact again and refresh spec/.upstream.json.',
    };
  }
  return { ok: true, message: `Local spec intact: ${declared} (${actual.slice(0, 12)})` };
}

/** Check 2: .upstream.json agrees with the backend lock. Pure, no I/O. */
export function compareUpstream(local, remote) {
  if (!isValidLocal(local)) return MALFORMED_LOCAL;
  if (!isRecord(remote) || typeof remote.version !== 'string' || !SHA256_RE.test(String(remote.sha256))) {
    return {
      ok: false,
      reason: 'MALFORMED_REMOTE',
      message: 'Backend lock is not shaped like { version, sha256 }.',
    };
  }
  if (local.version !== remote.version) {
    return {
      ok: false,
      reason: 'VERSION_MISMATCH',
      message:
        `API version out of date: this repo is synced to ${local.version}, backend publishes ${remote.version}. ` +
        'Copy openapi/openapi.v1.public.yaml to spec/openapi.v1.yaml, refresh spec/.upstream.json, ' +
        'then run scripts/downconvert.mjs and scripts/generate.sh.',
    };
  }
  if (local.upstream_ssot_sha256 !== remote.sha256) {
    return {
      ok: false,
      reason: 'HASH_MISMATCH',
      message:
        `Same version (${local.version}) but the backend lock hash changed ` +
        `(${remote.sha256.slice(0, 12)} vs recorded ${local.upstream_ssot_sha256.slice(0, 12)}). ` +
        'spec/openapi.v1.yaml is no longer the body the backend publishes under this version; ' +
        'copy the public artefact again and refresh spec/.upstream.json.',
    };
  }
  return { ok: true, message: `Aligned with backend: API ${local.version} (${local.upstream_ssot_sha256.slice(0, 12)})` };
}

/** Branch precedence: --branch flag > IMZALA_UPSTREAM_BRANCH env > .upstream.json source.branch > "test". */
export function resolveUpstreamBranch({ argv = [], env = {}, local = null } = {}) {
  const i = argv.indexOf('--branch');
  if (i !== -1 && argv[i + 1]) return argv[i + 1];
  if (env.IMZALA_UPSTREAM_BRANCH) return env.IMZALA_UPSTREAM_BRANCH;
  if (isRecord(local) && isRecord(local.source) && typeof local.source.branch === 'string' && local.source.branch) {
    return local.source.branch;
  }
  return DEFAULT_BRANCH;
}

export function buildRemoteUrl({ repo = DEFAULT_REPO, branch = DEFAULT_BRANCH, lockPath = DEFAULT_LOCK_PATH } = {}) {
  return `https://api.github.com/repos/${repo}/contents/${lockPath}?ref=${encodeURIComponent(branch)}`;
}

/**
 * Reads the backend lock. `target` is either an http(s) URL (private repo, token
 * required) or a local file path (no network; useful for offline verification).
 */
export async function fetchBackendLock(target, { fetchImpl = globalThis.fetch, token = '' } = {}) {
  if (!/^https?:\/\//.test(target)) {
    return JSON.parse(readFileSync(target, 'utf8'));
  }
  if (!token) {
    throw new Error(
      'No token for the backend repository. Set IMZALA_UPSTREAM_TOKEN (or GITHUB_TOKEN) with read access, ' +
      'or pass --remote <local copy of openapi.v1.lock.json>.',
    );
  }
  const res = await fetchImpl(target, {
    headers: {
      accept: 'application/vnd.github.raw+json',
      authorization: `Bearer ${token}`,
      'user-agent': 'imzala-sdk upstream-check',
    },
  });
  if (!res || res.ok !== true) {
    throw new Error(`Backend lock could not be downloaded (${target}): HTTP ${res ? res.status : 'no response'}`);
  }
  return JSON.parse(await res.text());
}

function readJson(file) {
  try {
    return JSON.parse(readFileSync(file, 'utf8'));
  } catch {
    return null;
  }
}

function readYaml(file) {
  try {
    return yaml.load(readFileSync(file, 'utf8'));
  } catch {
    return null;
  }
}

function parseArgs(argv) {
  const out = { local: DEFAULT_LOCAL, spec: DEFAULT_SPEC, remote: null };
  for (let i = 0; i < argv.length; i += 1) {
    if (argv[i] === '--local') out.local = path.resolve(argv[i + 1]);
    if (argv[i] === '--spec') out.spec = path.resolve(argv[i + 1]);
    if (argv[i] === '--remote') out.remote = /^https?:\/\//.test(argv[i + 1]) ? argv[i + 1] : path.resolve(argv[i + 1]);
  }
  return out;
}

/** CLI entry: returns 0 when both checks pass, 1 otherwise. */
export async function main(argv = process.argv.slice(2), { env = process.env, log = console.log, error = console.error } = {}) {
  const args = parseArgs(argv);
  const local = readJson(args.local);
  const doc = readYaml(args.spec);

  const localResult = verifyLocalSpec(doc, local);
  if (!localResult.ok) {
    error(`[upstream-check] RED (${localResult.reason}): ${localResult.message}`);
    return 1;
  }
  log(`[upstream-check] ${localResult.message}`);

  const branch = resolveUpstreamBranch({ argv, env, local });
  const repo = (isRecord(local.source) && local.source.repo) || DEFAULT_REPO;
  const lockPath = (isRecord(local.source) && local.source.lock) || DEFAULT_LOCK_PATH;
  const target = args.remote || buildRemoteUrl({ repo, branch, lockPath });
  const token = env.IMZALA_UPSTREAM_TOKEN || env.GITHUB_TOKEN || '';

  let remote;
  try {
    remote = await fetchBackendLock(target, { token });
  } catch (err) {
    error(`[upstream-check] RED (FETCH_FAILED): ${err.message}`);
    error('[upstream-check] Unreachable does not mean aligned; the gate stays closed.');
    return 1;
  }

  const result = compareUpstream(local, remote);
  if (!result.ok) {
    error(`[upstream-check] RED (${result.reason}): ${result.message}`);
    return 1;
  }
  log(`[upstream-check] GREEN: ${result.message} [branch ${branch}]`);
  return 0;
}

if (process.argv[1] && path.resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  main().then((code) => process.exit(code));
}
