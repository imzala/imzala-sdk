import { describe, expect, it } from 'vitest';
import type { DemandStatus, PageItem, StandardError, TemplateVariable, WebhookEnvelope } from '../../generated/api';

// Response enums grow with the API (item_type gained contract_number in
// 1.14.5 and stamp in 1.14.6; status, decision, doc_kind, error codes and
// webhook types grow the same way). Since SDK 1.2.0 every enum the server
// can return is an open string in the generated models: a value this client
// was generated without must still type-check and survive a JSON round trip.
const BODY = '{"page_id":1,"item_type":"future_value","position_x":0.1,"position_y":0.2,"width":0.3,"height":0.1}';

const DEMAND = JSON.stringify({
  id: '00000000-0000-4000-8000-000000000001',
  title: 'Ornek',
  status: 'future_value',
  parties: [{ party_id: '00000000-0000-4000-8000-000000000002', locale: 'future_value', signed_locale: 'future_value' }],
  stamp_items: [{ item_id: 1, source: 'future_value' }],
  documents: [{
    id: '00000000-0000-4000-8000-000000000003',
    doc_kind: 'future_value',
    progress: { status: 'future_value' },
    sealing: { status: 'future_value' },
    decisions: [{ party_id: '00000000-0000-4000-8000-000000000002', decision: 'future_value' }],
  }],
});

describe('generated models tolerate unknown enum values', () => {
  it.each(['contract_number', 'stamp', 'future_value'])('PageItem keeps %s', (value) => {
    const parsed = JSON.parse(BODY.replace('future_value', value)) as PageItem;
    const item: PageItem = { ...parsed, item_type: value };
    expect(item.item_type).toBe(value);
  });

  it.each(['stamp', 'future_value'])('TemplateVariable keeps %s', (value) => {
    const variable: TemplateVariable = { slug: 'a', item_type: value };
    expect((JSON.parse(JSON.stringify(variable)) as TemplateVariable).item_type).toBe(value);
  });

  it('DemandStatus keeps status, locale, source, doc_kind, progress, sealing and decision', () => {
    const d = JSON.parse(DEMAND) as DemandStatus;
    const status: string | undefined = d.status;
    expect(status).toBe('future_value');
    expect(d.parties?.[0].locale).toBe('future_value');
    expect(d.parties?.[0].signed_locale).toBe('future_value');
    expect(d.stamp_items?.[0].source).toBe('future_value');
    const doc = d.documents?.[0];
    expect(doc?.doc_kind).toBe('future_value');
    expect(doc?.progress?.status).toBe('future_value');
    expect(doc?.sealing?.status).toBe('future_value');
    expect(doc?.decisions?.[0].decision).toBe('future_value');
  });

  it('webhook type and error code accept unknown values', () => {
    const event: WebhookEnvelope = { id: 'evt_0123456789abcdef0123456789abcdef', type: 'future_value', created_at: '2026-11-01T00:00:00Z', data: {} };
    const error: StandardError = { success: false, error: { code: 'future_value', message: 'Ornek' } };
    expect(event.type).toBe('future_value');
    expect(error.error?.code).toBe('future_value');
  });
});
