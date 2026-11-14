import { afterEach, describe, expect, it, vi } from 'vitest';
import { DemandsApi } from '../../generated/api';
import { Imzala, describeErrorCode, isKnownErrorCode } from '../index';
import type { ContractNumber, DemandStatus } from '../index';

// API 1.17.0: contract_numbers on demand detail/list, new 409 codes (1.16.0 + 1.17.0).

afterEach(() => {
  vi.restoreAllMocks();
});

const NUMBERS: ContractNumber[] = [
  { sequence_id: '2f8b6a41-7c3d-4e95-a0b2-5d1e9c7f3a68', value: 'ABC-2026-042' },
];

describe('contract_numbers', () => {
  it('demands.get() carries contract_numbers typed as ContractNumber[]', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsIdGet').mockResolvedValue({
      data: { success: true, data: { id: 'd1', status: 'PENDING', contract_numbers: NUMBERS } },
      status: 200,
    } as any);

    const status: DemandStatus = await new Imzala({ apiKey: 'imz_test' }).demands.get('d1');
    const numbers: ContractNumber[] | undefined = status.contract_numbers;
    expect(numbers?.map((n) => n.value)).toEqual(['ABC-2026-042']);
  });

  it('demands.list() rows carry contract_numbers; absent field stays undefined', async () => {
    vi.spyOn(DemandsApi.prototype, 'apiV1DemandsGet').mockResolvedValue({
      data: {
        success: true,
        data: { demands: [{ id: 'd1', contract_numbers: NUMBERS }, { id: 'd2' }], total: 2, page: 1, limit: 20 },
      },
      status: 200,
    } as any);

    const list = await new Imzala({ apiKey: 'imz_test' }).demands.list();
    expect(list.demands?.[0].contract_numbers?.[0].value).toBe('ABC-2026-042');
    expect(list.demands?.[1].contract_numbers).toBeUndefined();
  });
});

describe('new error codes', () => {
  it.each(['PARTY_PHONE_MISSING', 'CONTRACT_NUMBER_SEQUENCE_COLLISION'])('%s is catalogued with a description', (code) => {
    expect(isKnownErrorCode(code)).toBe(true);
    expect(describeErrorCode(code)).toMatch(/\S/);
  });
});
