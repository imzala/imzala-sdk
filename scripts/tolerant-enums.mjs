/**
 * Tolerant reader for growing enums.
 *
 * openapi-generator emits closed enums for string properties that declare
 * `enum`. Java, C#, Python and PHP models then throw when the server returns
 * a value the client was generated without (a new `item_type`, `status`, ...),
 * so any additive server change breaks every older SDK at deserialization.
 * This transform runs on the generator input only (the vendored spec is
 * untouched): `enum` is dropped from the listed string properties and the
 * known values are appended to the description.
 */

const SCHEMA_REF = '#/components/schemas/';
const CHILD_KEYS = ['properties', 'patternProperties'];
const SINGLE_KEYS = ['items', 'additionalProperties', 'not'];
const LIST_KEYS = ['allOf', 'oneOf', 'anyOf'];

function refName(node) {
  return node && typeof node.$ref === 'string' && node.$ref.startsWith(SCHEMA_REF)
    ? node.$ref.slice(SCHEMA_REF.length)
    : null;
}

function children(node) {
  const out = [];
  if (!node || typeof node !== 'object') return out;
  for (const k of CHILD_KEYS) if (node[k] && typeof node[k] === 'object') out.push(...Object.values(node[k]));
  for (const k of SINGLE_KEYS) if (node[k] && typeof node[k] === 'object') out.push(node[k]);
  for (const k of LIST_KEYS) if (Array.isArray(node[k])) out.push(...node[k]);
  return out;
}

function relax(node, stats) {
  if (!node || typeof node !== 'object' || refName(node)) return;
  if (Array.isArray(node.enum) && (node.type === 'string' || node.type === undefined)) {
    const values = node.enum.filter((v) => typeof v === 'string');
    if (values.length === node.enum.length && values.length > 0) {
      const note = `Bilinen değerler: ${values.join(', ')}. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın.`;
      node.description = node.description ? `${String(node.description).trimEnd()}\n\n${note}` : note;
      delete node.enum;
      stats.count += 1;
    }
  }
  for (const c of children(node)) relax(c, stats);
}

/**
 * Enum properties that grow with the API and are opened up for every language
 * ("Schema.property"). PageItem is shared by the items upsert request and the
 * items responses; the server still validates the value on write
 * (INVALID_ITEM_TYPE lists the allowed types), so opening it only drops the
 * client-side check. Other closed enums keep their generated enum types
 * (changing them renames public generated types).
 */
export const TOLERANT_TARGETS = ['PageItem.item_type', 'TemplateVariable.item_type'];

/** Mutates `doc` (an OpenAPI 3.0 object). Returns { count } of relaxed enums. */
export function relaxEnums(doc, targets = TOLERANT_TARGETS) {
  const stats = { count: 0 };
  for (const target of targets) {
    const [schemaName, prop] = target.split('.');
    const node = doc.components?.schemas?.[schemaName]?.properties?.[prop];
    if (!node) throw new Error(`tolerant-enums: ${target} not found in spec`);
    relax(node, stats);
  }
  return stats;
}
