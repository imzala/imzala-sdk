import { describe, expect, it } from 'vitest';
import { ApiV1DemandsPost400ResponseCodeEnum } from '../../generated/api';
import type { ApiV1DemandsPost400Response, DocumentSelectionErrorDetails } from '../../generated/api';

// These assignments are also compiled by the package's actual tsc check.
// Keep the published selection-only projection's optional property ABI.
const empty: ApiV1DemandsPost400Response = {};
const selection: ApiV1DemandsPost400Response = { code: ApiV1DemandsPost400ResponseCodeEnum.InvalidDocumentSelection };
const optional: ApiV1DemandsPost400Response = { success: false, error: 'selection', code: ApiV1DemandsPost400ResponseCodeEnum.InvalidDocumentSelection, field: null, details: { reason: 'shape' } };
const details: DocumentSelectionErrorDetails | undefined = optional.details;

describe('published generated single400 selection projection (Task4D SDK-I1)', () => {
  it('retains the old enum member and optional fields', () => {
    expect(ApiV1DemandsPost400ResponseCodeEnum).toEqual({ InvalidDocumentSelection: 'INVALID_DOCUMENT_SELECTION' });
    expect(empty).toEqual({});
    expect(selection).toEqual({ code: 'INVALID_DOCUMENT_SELECTION' });
    expect(optional.field).toBeNull();
    expect(details?.reason).toBe('shape');
  });
});
