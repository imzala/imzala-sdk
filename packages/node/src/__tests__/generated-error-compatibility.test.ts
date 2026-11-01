import { describe, expect, it } from 'vitest';
import type { ApiV1DemandsPost400Response, DocumentSelectionErrorDetails } from '../../generated/api';

// These assignments are also compiled by the package's actual tsc check.
// Keep the published selection-only projection's optional property ABI.
// Since 1.2.0 `code` is an open string (response enums are open), so the
// former ApiV1DemandsPost400ResponseCodeEnum constant is gone; compare values.
const empty: ApiV1DemandsPost400Response = {};
const selection: ApiV1DemandsPost400Response = { code: 'INVALID_DOCUMENT_SELECTION' };
const optional: ApiV1DemandsPost400Response = { success: false, error: 'selection', code: 'INVALID_DOCUMENT_SELECTION', field: null, details: { reason: 'shape' } };
const details: DocumentSelectionErrorDetails | undefined = optional.details;

describe('published generated single400 selection projection (Task4D SDK-I1)', () => {
  it('retains the optional fields and accepts any code string', () => {
    const future: ApiV1DemandsPost400Response = { code: 'FUTURE_VALUE' };
    expect(future.code).toBe('FUTURE_VALUE');
    expect(empty).toEqual({});
    expect(selection).toEqual({ code: 'INVALID_DOCUMENT_SELECTION' });
    expect(optional.field).toBeNull();
    expect(details?.reason).toBe('shape');
  });
});
