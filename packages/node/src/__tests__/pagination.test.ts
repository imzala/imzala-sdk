import { afterEach, describe, expect, it, vi } from 'vitest';
import { TemplatesApi } from '../../generated/api';
import { Imzala } from '../index';

afterEach(() => {
  vi.restoreAllMocks();
});

function page(templates: Array<{ id: string }>, total: number, pageNum: number, limit: number) {
  return { data: { success: true, data: { templates, total, page: pageNum, limit } }, status: 200 } as any;
}

describe('templates.listAll() — pagination iterator', () => {
  it('walks 2 full pages then a short page, yielding every item and stopping (no extra request)', async () => {
    const spy = vi
      .spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet')
      .mockResolvedValueOnce(page([{ id: 't1' }, { id: 't2' }], 5, 1, 2))
      .mockResolvedValueOnce(page([{ id: 't3' }, { id: 't4' }], 5, 2, 2))
      .mockResolvedValueOnce(page([{ id: 't5' }], 5, 3, 2)); // short page (1 < limit 2) -> stop

    const imzala = new Imzala({ apiKey: 'imz_test' });

    const ids: string[] = [];
    for await (const template of imzala.templates.listAll({ limit: 2 })) {
      ids.push(template.id!);
    }

    expect(ids).toEqual(['t1', 't2', 't3', 't4', 't5']);
    expect(spy).toHaveBeenCalledTimes(3);
    expect(spy).toHaveBeenNthCalledWith(1, { page: 1, limit: 2 });
    expect(spy).toHaveBeenNthCalledWith(2, { page: 2, limit: 2 });
    expect(spy).toHaveBeenNthCalledWith(3, { page: 3, limit: 2 });
  });

  it('stops as soon as `total` is reached even when the last page is exactly full — no infinite loop', async () => {
    const spy = vi
      .spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet')
      .mockResolvedValueOnce(page([{ id: 't1' }, { id: 't2' }], 4, 1, 2))
      .mockResolvedValueOnce(page([{ id: 't3' }, { id: 't4' }], 4, 2, 2));
    // If the total-reached check didn't exist, a 3rd call would happen here
    // (the 2nd page was exactly `limit` items, not "short") and the mock
    // queue would be exhausted -> vitest throws, catching a regression.

    const imzala = new Imzala({ apiKey: 'imz_test' });

    const ids: string[] = [];
    for await (const template of imzala.templates.listAll({ limit: 2 })) {
      ids.push(template.id!);
    }

    expect(ids).toEqual(['t1', 't2', 't3', 't4']);
    expect(spy).toHaveBeenCalledTimes(2);
  });

  it('stops immediately on an empty first page (no items, no infinite loop)', async () => {
    const spy = vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet').mockResolvedValueOnce(page([], 0, 1, 20));

    const imzala = new Imzala({ apiKey: 'imz_test' });

    const ids: string[] = [];
    for await (const template of imzala.templates.listAll()) {
      ids.push(template.id!);
    }

    expect(ids).toEqual([]);
    expect(spy).toHaveBeenCalledTimes(1);
  });

  it('list() (single page) is unaffected — still returns one page, not an iterator', async () => {
    vi.spyOn(TemplatesApi.prototype, 'apiV1TemplatesGet').mockResolvedValueOnce(
      page([{ id: 't1' }, { id: 't2' }], 5, 1, 2),
    );

    const imzala = new Imzala({ apiKey: 'imz_test' });
    const result = await imzala.templates.list({ page: 1, limit: 2 });

    expect(result.templates).toEqual([{ id: 't1' }, { id: 't2' }]);
    expect(result.total).toBe(5);
  });
});
