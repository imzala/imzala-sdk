import { describe, expect, it } from 'vitest';
import type { PageItem, TemplateVariable } from '../../generated/api';

// item_type grows with the API (contract_number in 1.14.5, stamp in 1.14.6).
// A value this client was generated without must still type-check and
// survive a JSON round trip; the generated types keep item_type an open string.
const BODY = '{"page_id":1,"item_type":"future_type","position_x":0.1,"position_y":0.2,"width":0.3,"height":0.1}';

describe('generated models tolerate unknown item_type values', () => {
  it.each(['contract_number', 'stamp', 'future_type'])('PageItem keeps %s', (value) => {
    const parsed = JSON.parse(BODY.replace('future_type', value)) as PageItem;
    const item: PageItem = { ...parsed };
    expect(item.item_type).toBe(value);
  });

  it.each(['stamp', 'future_type'])('TemplateVariable keeps %s', (value) => {
    const variable: TemplateVariable = JSON.parse(`{"slug":"a","item_type":"${value}"}`);
    expect(variable.item_type).toBe(value);
  });
});
