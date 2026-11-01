import { describe, expect, it } from 'vitest';
import { relaxEnums } from '../tolerant-enums.mjs';

const doc = () => ({
  components: {
    schemas: {
      PageItem: { properties: { item_type: { type: 'string', description: 'Tür.', enum: ['signature', 'stamp'] }, other: { type: 'string', enum: ['a'] } } },
      TemplateVariable: { properties: { item_type: { type: 'string', enum: ['text'] } } },
    },
  },
});

describe('relaxEnums', () => {
  it('drops the enum on the targets, keeps values in the description, leaves other enums', () => {
    const d = doc();
    expect(relaxEnums(d, ['PageItem.item_type', 'TemplateVariable.item_type']).count).toBe(2);
    const t = d.components.schemas.PageItem.properties.item_type;
    expect(t.enum).toBeUndefined();
    expect(t.description).toContain('Tür.');
    expect(t.description).toContain('signature, stamp');
    expect(d.components.schemas.PageItem.properties.other.enum).toEqual(['a']);
  });

  it('fails loudly when a target disappears from the spec', () => {
    expect(() => relaxEnums(doc(), ['PageItem.nope'])).toThrow(/not found/);
  });
});
