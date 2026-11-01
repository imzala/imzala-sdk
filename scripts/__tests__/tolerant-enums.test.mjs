import { readFileSync } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import yaml from 'js-yaml';
import { describe, expect, it } from 'vitest';
import { ALSO_OPEN, classifyEnums, relaxEnums } from '../tolerant-enums.mjs';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..', '..');
const load = (file) => yaml.load(readFileSync(path.join(repoRoot, 'spec', file), 'utf8'));

const doc = () => ({
  paths: {
    '/things': {
      get: {
        parameters: [{ name: 'state', in: 'query', schema: { type: 'string', enum: ['open', 'closed'] } }],
        responses: {
          200: { content: { 'application/json': { schema: { $ref: '#/components/schemas/Thing' } } } },
          429: { $ref: '#/components/responses/Limited' },
        },
      },
      post: {
        requestBody: { content: { 'application/json': { schema: { $ref: '#/components/schemas/ThingInput' } } } },
        responses: { 201: { content: { 'application/json': { schema: { type: 'object', properties: { code: { type: 'string', enum: ['A'] } } } } } } },
      },
    },
  },
  components: {
    responses: {
      Limited: { content: { 'application/json': { schema: { type: 'object', properties: { code: { type: 'string', enum: ['RATE_LIMITED'] } } } } } },
    },
    schemas: {
      Thing: { properties: { status: { type: 'string', description: 'Durum.', enum: ['NEW', 'DONE'] }, locale: { $ref: '#/components/schemas/Locale' } } },
      ThingInput: { properties: { mode: { type: 'string', enum: ['x', 'y'] }, locale: { $ref: '#/components/schemas/Locale' }, items: { type: 'array', items: { $ref: '#/components/schemas/PageItem' } } } },
      Locale: { type: 'string', nullable: true, enum: ['tr', 'en', null] },
      Hook: { properties: { type: { type: 'string', enum: ['thing.created'] }, ok: { type: 'boolean', enum: [false] } } },
      PageItem: { properties: { item_type: { type: 'string', enum: ['signature'] } } },
    },
  },
});

describe('relaxEnums', () => {
  it('opens response, shared and unreferenced (webhook) enums, keeps request-only ones closed', () => {
    const d = doc();
    const r = relaxEnums(d, []);
    expect(r.opened).toEqual([
      'Hook.type',
      'Locale',
      'POST /things 201 application/json.code',
      'responses.Limited application/json.code',
      'Thing.status',
    ].sort((a, b) => a.localeCompare(b)));
    expect(r.requestOnly).toEqual(['GET /things param state', 'PageItem.item_type', 'ThingInput.mode']);
    expect(r.nonString).toEqual(['Hook.ok']);
    expect(r.count).toBe(5);

    const status = d.components.schemas.Thing.properties.status;
    expect(status.enum).toBeUndefined();
    expect(status.description).toContain('Durum.');
    expect(status.description).toContain('Bilinen değerler: NEW, DONE.');
    expect(status['x-known-values']).toEqual(['NEW', 'DONE']);
    const locale = d.components.schemas.Locale;
    expect(locale.enum).toBeUndefined();
    expect(locale.nullable).toBe(true);
    expect(locale['x-known-values']).toEqual(['tr', 'en']);
    expect(d.components.schemas.ThingInput.properties.mode.enum).toEqual(['x', 'y']);
    expect(d.components.schemas.Hook.properties.ok.enum).toEqual([false]);
  });

  it('opens an ALSO_OPEN request-only enum and fails loudly when it disappears', () => {
    const d = doc();
    expect(relaxEnums(d, ['PageItem.item_type']).opened).toContain('PageItem.item_type');
    expect(d.components.schemas.PageItem.properties.item_type.enum).toBeUndefined();
    expect(() => relaxEnums(doc(), ['PageItem.nope'])).toThrow(/not found/);
  });
});

describe('generator input (spec/openapi.v1.3.0.yaml)', () => {
  const generated = load('openapi.v1.3.0.yaml');

  it('has no closed string enum left outside request-only locations', () => {
    // ALSO_OPEN is already applied here, so classify without it.
    const c = classifyEnums(generated, []);
    expect(c.open.map((e) => e.loc)).toEqual([]);
    expect(c.nonString.every((e) => e.values.every((v) => typeof v !== 'string'))).toBe(true);
  });

  it('opened the representative response enums the SDK tests deserialize', () => {
    const s = generated.components.schemas;
    for (const node of [
      s.DemandStatus.properties.status,
      s.DocumentPartyDecision.properties.decision,
      s.DemandDocumentStatus.properties.doc_kind,
      s.EnvelopeDocument.properties.doc_kind,
      s.WebhookEnvelope.properties.type,
      s.StandardError.properties.error.properties.code,
      s.SignerLocale,
      s.PageItem.properties.item_type,
      s.TemplateVariable.properties.item_type,
    ]) {
      expect(node.enum).toBeUndefined();
      expect(node['x-known-values'].length).toBeGreaterThan(0);
    }
  });

  it('keeps exactly the reviewed request-only enums closed', () => {
    // A new request-only enum lands here and needs a decision: keep it
    // closed (request typing) or open it (the server may echo it back).
    expect(classifyEnums(generated, []).requestOnly.map((e) => e.loc)).toEqual([
      'ContractTermInput.renewal_type',
      'ContractTermInput.term_start_mode',
      'CreateDemandRequest.allowed_signature_variants.items',
      'CreateDemandRequest.document_collection_timing',
      'CreateDemandRequest.renewal_type',
      'CreateDemandRequest.term_start_mode',
      'GET /api/v1/demands param archived',
      'GET /api/v1/demands param status',
      'GET /api/v1/demands/{demandId}/documents param view',
      'PATCH /api/v1/demands/{demandId}/documents/{docId} request application/json.doc_kind',
      'POST /api/v1/demands/{demandId}/dispatch request application/json.send_invitations.oneOf[1]',
      'POST /api/v1/demands/{demandId}/documents request application/json.doc_kind',
      'POST /api/v1/demands/{demandId}/documents/upload request multipart/form-data.doc_kind',
      'POST /api/v1/demands/{demandId}/documents/upload request multipart/form-data.is_required',
      'POST /api/v1/demands/bulk request application/json.options.allowed_signature_variants.items',
      'POST /api/v1/demands/bulk request application/json.options.document_collection_timing',
      'POST /api/v1/demands/bulk request application/json.options.qes_pades_level',
      'POST /api/v1/demands/upload request multipart/form-data.force',
      'POST /api/v1/demands/upload request multipart/form-data.on_anchor_miss',
      'POST /api/v1/demands/upload request multipart/form-data.send_invitations',
      'POST /api/v1/field-templates/{id}/preview-layout request multipart/form-data.on_anchor_miss',
      'ReminderSettings.channels.items',
      'TriggerReminderRequest.channels.items',
    ]);
    expect(ALSO_OPEN).toEqual(['PageItem.item_type']);
  });
});
