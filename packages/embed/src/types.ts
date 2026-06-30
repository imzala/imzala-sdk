export type EmbedErrorCode = 'TOKEN_EXPIRED' | 'TOKEN_USED' | 'ORIGIN_DENIED' | 'NETWORK' | 'UNKNOWN';

export interface EmbedOptions {
  container?: HTMLElement | null;
  baseUrl?: string;        // default https://e.imzala.org
  locale?: 'tr' | 'en';
  autoResize?: boolean;    // default true
  onReady?: (p: any) => void;
  onComplete?: (p: { demandId: string; partyId: string; signedAt?: string }) => void;
  onDecline?: (p: { reason?: string }) => void;
  onCancel?: () => void;
  onTimeout?: (p: { code: 'TOKEN_EXPIRED' | 'SESSION_TIMEOUT' }) => void;
  onError?: (p: { code: EmbedErrorCode; message?: string }) => void;
  onResize?: (p: { height: number }) => void;
  onFieldSigned?: (p: { fieldId?: string; fieldType?: string }) => void;
  onFieldUnsigned?: (p: { fieldId?: string; fieldType?: string }) => void;
}
