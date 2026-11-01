/**
 * Tolerant reader for growing enums.
 *
 * openapi-generator emits closed enums for string properties that declare
 * `enum`. Java, C#, Python and PHP models then throw when the server returns
 * a value the client was generated without (a new `item_type`, `status`,
 * error `code`, webhook `type`, ...), so any additive server change breaks
 * every older SDK at deserialization. This transform runs on the generator
 * input only (the vendored spec is untouched): `enum` is dropped and the
 * known values are appended to the description.
 *
 * Rule (no hand-kept list): every string enum in the document is opened
 * unless it is request-only, i.e. reachable from a request body or a
 * parameter and NOT reachable from any response. Schemas reachable from no
 * operation at all (the webhook payloads; OpenAPI 3.0 has no `webhooks`
 * section) are server output and are opened too. Request-only enums stay
 * closed: the client never deserializes them, the server validates them on
 * write, and closing them keeps typed request builders (Java/C#/PHP enum
 * constants, TypeScript unions) unchanged for callers.
 *
 * Non-string enums (e.g. `success: enum [false]`) are left as they are.
 */

/**
 * Request-only enums that are opened anyway ("Schema.property"). Only one:
 * PageItem.item_type (items upsert body). Its response twin
 * (UpsertItemsResponse items) is already a plain string, so callers that
 * send back items they read get the same type; it was opened in the 1.2.0
 * line before this rule existed and its ItemTypeEnum removal is already in
 * the CHANGELOG. The server validates it on write (INVALID_ITEM_TYPE).
 */
export const ALSO_OPEN = ['PageItem.item_type'];

const SCHEMA_REF = '#/components/schemas/';
const CHILD_KEYS = ['properties', 'patternProperties'];
const SINGLE_KEYS = ['items', 'additionalProperties', 'not'];
const LIST_KEYS = ['allOf', 'oneOf', 'anyOf'];
const METHODS = ['get', 'put', 'post', 'delete', 'options', 'head', 'patch', 'trace'];

function resolve(doc, node) {
  if (!node || typeof node !== 'object' || typeof node.$ref !== 'string') return node;
  const parts = node.$ref.replace(/^#\//, '').split('/');
  let cur = doc;
  for (const p of parts) cur = cur?.[p];
  if (cur === undefined) throw new Error(`tolerant-enums: unresolved $ref ${node.$ref}`);
  return cur;
}

function schemaChildren(node) {
  const out = [];
  if (!node || typeof node !== 'object') return out;
  for (const k of CHILD_KEYS) {
    if (node[k] && typeof node[k] === 'object') for (const [name, v] of Object.entries(node[k])) out.push([`.${name}`, v]);
  }
  for (const k of SINGLE_KEYS) if (node[k] && typeof node[k] === 'object') out.push([`.${k}`, node[k]]);
  for (const k of LIST_KEYS) if (Array.isArray(node[k])) node[k].forEach((c, i) => out.push([`.${k}[${i}]`, c]));
  return out;
}

/** Every enum-bearing schema node with its physical location (refs not followed). */
function collectEnums(doc) {
  const found = new Map();
  const walk = (node, loc) => {
    if (!node || typeof node !== 'object' || node.$ref) return;
    if (Array.isArray(node.enum) && !found.has(node)) found.set(node, loc);
    for (const [suffix, c] of schemaChildren(node)) walk(c, loc + suffix);
  };
  const schemaOf = (mediaMap, loc) => {
    for (const [ct, mt] of Object.entries(mediaMap ?? {})) if (mt?.schema) walk(mt.schema, `${loc} ${ct}`);
  };
  for (const [name, s] of Object.entries(doc.components?.schemas ?? {})) walk(s, name);
  for (const [name, r] of Object.entries(doc.components?.responses ?? {})) schemaOf(r.content, `responses.${name}`);
  for (const [name, p] of Object.entries(doc.components?.parameters ?? {})) if (p.schema) walk(p.schema, `parameters.${name}`);
  for (const [path, item] of Object.entries(doc.paths ?? {})) {
    for (const p of item.parameters ?? []) if (p.schema) walk(p.schema, `${path} param ${p.name}`);
    for (const m of METHODS) {
      const op = item[m];
      if (!op) continue;
      const at = `${m.toUpperCase()} ${path}`;
      for (const p of op.parameters ?? []) if (p.schema) walk(p.schema, `${at} param ${p.name}`);
      schemaOf(op.requestBody?.content, `${at} request`);
      for (const [code, r] of Object.entries(op.responses ?? {})) if (!r.$ref) schemaOf(r.content, `${at} ${code}`);
    }
  }
  return found;
}

/** Set of schema nodes reachable from the given roots, following $refs. */
function reach(doc, roots) {
  const seen = new Set();
  const stack = [...roots];
  while (stack.length) {
    const raw = stack.pop();
    const node = resolve(doc, raw);
    if (!node || typeof node !== 'object' || seen.has(node)) continue;
    seen.add(node);
    for (const [, c] of schemaChildren(node)) stack.push(c);
  }
  return seen;
}

function roots(doc) {
  const req = [];
  const resp = [];
  const media = (content, into) => {
    for (const mt of Object.values(content ?? {})) if (mt?.schema) into.push(mt.schema);
  };
  for (const item of Object.values(doc.paths ?? {})) {
    const shared = item.parameters ?? [];
    for (const m of METHODS) {
      const op = item[m];
      if (!op) continue;
      for (const p of [...shared, ...(op.parameters ?? [])]) {
        const prm = resolve(doc, p);
        if (prm?.schema) req.push(prm.schema);
      }
      media(resolve(doc, op.requestBody)?.content, req);
      for (const r of Object.values(op.responses ?? {})) {
        const res = resolve(doc, r);
        media(res?.content, resp);
        for (const h of Object.values(res?.headers ?? {})) if (resolve(doc, h)?.schema) resp.push(resolve(doc, h).schema);
      }
    }
  }
  return { req, resp };
}

/**
 * Classifies every enum in `doc` without changing it.
 * Returns { open, requestOnly, nonString }: arrays of { loc, values } sorted by loc.
 * `open` is what relaxEnums() opens.
 */
export function classifyEnums(doc, alsoOpen = ALSO_OPEN) {
  const enums = collectEnums(doc);
  const r = roots(doc);
  const fromReq = reach(doc, r.req);
  const fromResp = reach(doc, r.resp);
  const out = { open: [], requestOnly: [], nonString: [] };
  const locs = new Set(enums.values());
  for (const loc of alsoOpen) if (!locs.has(loc)) throw new Error(`tolerant-enums: ${loc} not found in spec`);
  for (const [node, loc] of enums) {
    const values = node.enum.filter((v) => v !== null);
    const entry = { loc, values, node };
    if (values.length === 0 || !values.every((v) => typeof v === 'string') || (node.type && node.type !== 'string')) {
      out.nonString.push(entry);
    } else if (fromReq.has(node) && !fromResp.has(node) && !alsoOpen.includes(loc)) {
      out.requestOnly.push(entry);
    } else {
      out.open.push(entry);
    }
  }
  for (const k of Object.keys(out)) out[k].sort((a, b) => a.loc.localeCompare(b.loc));
  return out;
}

/**
 * Mutates `doc` (an OpenAPI 3.0 object): opens every non-request-only string
 * enum plus ALSO_OPEN. Returns { count, opened, requestOnly, nonString } (location lists).
 */
export function relaxEnums(doc, alsoOpen = ALSO_OPEN) {
  const c = classifyEnums(doc, alsoOpen);
  for (const { node, values } of c.open) {
    const note = `Bilinen değerler: ${values.join(', ')}. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın.`;
    node.description = node.description ? `${String(node.description).trimEnd()}\n\n${note}` : note;
    delete node.enum;
    if (!node.type) node.type = 'string';
    // openapi-generator merges structurally identical inline schemas and
    // ignores descriptions while comparing; without the enum two error
    // bodies that differed only in their `code` values would collapse into
    // one model and rename the other (e.g. ApiV1DemandsPost500Response into
    // ApiV1DemandsPost402Response). The extension keeps them distinct and
    // gives tooling the known values in machine-readable form.
    node['x-known-values'] = values;
  }
  const locs = (list) => list.map((e) => e.loc);
  return { count: c.open.length, opened: locs(c.open), requestOnly: locs(c.requestOnly), nonString: locs(c.nonString) };
}
