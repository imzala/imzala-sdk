import { afterEach, describe, expect, it, vi } from 'vitest';
import { ImzalaEmbed } from '../index';

// Track instances so window 'message' listeners are torn down between tests
// (removeEventListener only fires via close()); jsdom's window is shared.
const instances: ImzalaEmbed[] = [];
function make(opts: ConstructorParameters<typeof ImzalaEmbed>[0]): ImzalaEmbed {
  const e = new ImzalaEmbed(opts);
  instances.push(e);
  return e;
}

afterEach(() => {
  while (instances.length) instances.pop()?.close();
  document.body.innerHTML = '';
});

// Force the strict source guard to pass: make iframe.contentWindow === window
// so a synthetic MessageEvent with source:window is accepted.
function mockContentWindow(e: ImzalaEmbed): HTMLIFrameElement {
  const iframe =
    ((e as any).iframe as HTMLIFrameElement) ??
    (document.querySelector('iframe') as HTMLIFrameElement);
  Object.defineProperty(iframe, 'contentWindow', { get: () => window, configurable: true });
  return iframe;
}

function post(origin: string, event: string, payload: unknown, withSource = true) {
  window.dispatchEvent(
    new MessageEvent('message', {
      origin,
      source: withSource ? window : undefined,
      data: { type: 'imzala-embed', version: '1.0', event, payload },
    }),
  );
}

const ORIGIN = 'https://e.imzala.org';

describe('cancel event', () => {
  it('fires onCancel but keeps the widget open', () => {
    const onCancel = vi.fn();
    const e = make({ baseUrl: ORIGIN, onCancel });
    e.open('tok');
    mockContentWindow(e);
    post(ORIGIN, 'cancel', undefined);
    expect(onCancel).toHaveBeenCalledTimes(1);
    // cancel does NOT auto-close: iframe still mounted
    expect(document.querySelector('iframe')).not.toBeNull();
  });
});

describe('timeout event', () => {
  it('fires onTimeout and auto-closes the widget', () => {
    const onTimeout = vi.fn();
    const e = make({ baseUrl: ORIGIN, onTimeout });
    e.open('tok');
    mockContentWindow(e);
    post(ORIGIN, 'timeout', { code: 'TOKEN_EXPIRED' });
    expect(onTimeout).toHaveBeenCalledWith({ code: 'TOKEN_EXPIRED' });
    // auto-close removes the iframe + modal
    expect(document.querySelector('iframe')).toBeNull();
    expect(document.querySelector('[aria-modal="true"]')).toBeNull();
  });
});

describe('auto-close side effects', () => {
  it('complete removes iframe and detaches the message listener', () => {
    const onComplete = vi.fn();
    const e = make({ baseUrl: ORIGIN, onComplete });
    e.open('tok');
    mockContentWindow(e);
    post(ORIGIN, 'complete', { demandId: 'd', partyId: 'p' });
    expect(onComplete).toHaveBeenCalledTimes(1);
    expect(document.querySelector('iframe')).toBeNull();
    // listener detached: a second complete must not re-fire
    post(ORIGIN, 'complete', { demandId: 'd', partyId: 'p' }, false);
    expect(onComplete).toHaveBeenCalledTimes(1);
  });

  it('decline removes iframe from the DOM', () => {
    const onDecline = vi.fn();
    const e = make({ baseUrl: ORIGIN, onDecline });
    e.open('tok');
    mockContentWindow(e);
    post(ORIGIN, 'decline', { reason: 'x' });
    expect(onDecline).toHaveBeenCalledTimes(1);
    expect(document.querySelector('iframe')).toBeNull();
  });

  it('error does NOT auto-close the widget', () => {
    const onError = vi.fn();
    const e = make({ baseUrl: ORIGIN, onError });
    e.open('tok');
    mockContentWindow(e);
    post(ORIGIN, 'error', { code: 'NETWORK' });
    expect(onError).toHaveBeenCalledWith({ code: 'NETWORK' });
    expect(document.querySelector('iframe')).not.toBeNull();
  });
});

describe('double open guard', () => {
  it('replaces the previous iframe and does not leak the old listener', () => {
    const onComplete = vi.fn();
    const e = make({ baseUrl: ORIGIN, onComplete });
    e.open('first');
    e.open('second');
    // only one iframe / one modal remain after re-open
    expect(document.querySelectorAll('iframe').length).toBe(1);
    expect(document.querySelectorAll('[aria-modal="true"]').length).toBe(1);
    // the current (second) iframe carries the token
    const iframe = mockContentWindow(e);
    expect(iframe.src).toContain('token=second');
    // a single complete fires the callback exactly once (no leaked listener)
    post(ORIGIN, 'complete', { demandId: 'd', partyId: 'p' });
    expect(onComplete).toHaveBeenCalledTimes(1);
  });
});

describe('origin derivation', () => {
  it('defaults origin to https://e.imzala.org when baseUrl is omitted', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ container });
    e.open('tok');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.src.startsWith('https://e.imzala.org/embed/sign')).toBe(true);
  });

  it('accepts messages from a custom baseUrl origin', () => {
    const onReady = vi.fn();
    const custom = 'https://test-esign.imzala.org';
    const e = make({ baseUrl: custom, onReady });
    e.open('tok');
    mockContentWindow(e);
    post(custom, 'ready', {});
    expect(onReady).toHaveBeenCalledTimes(1);
  });

  it('rejects the default origin when a custom baseUrl is set', () => {
    const onReady = vi.fn();
    const e = make({ baseUrl: 'https://test-esign.imzala.org', onReady });
    e.open('tok');
    mockContentWindow(e);
    post('https://e.imzala.org', 'ready', {}); // wrong origin for this instance
    expect(onReady).not.toHaveBeenCalled();
  });

  it('normalizes a baseUrl with a path down to its origin', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: 'https://e.imzala.org/deep/path?x=1', container });
    e.open('tok');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.src.startsWith('https://e.imzala.org/embed/sign')).toBe(true);
    expect(iframe.src).not.toContain('/deep/path');
  });
});

describe('iframe url + attributes', () => {
  it('builds the /embed/sign?token= url', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: ORIGIN, container });
    e.open('abc123');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.src).toContain('/embed/sign?token=abc123');
  });

  it('omits the lang param when locale is not set', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: ORIGIN, container });
    e.open('abc');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.src).not.toContain('lang=');
  });

  it('sets accessible title and security/permission attributes', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: ORIGIN, container });
    e.open('tok');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.title).toBe('İmzala dijital imza');
    expect(iframe.getAttribute('allow')).toBe('camera; clipboard-write');
    expect(iframe.getAttribute('referrerpolicy')).toBe('origin-when-cross-origin');
  });
});

describe('initial height', () => {
  it('sets a default 600px height when autoResize is on', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: ORIGIN, container });
    e.open('tok');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.style.height).toBe('600px');
  });

  it('does not set an initial height when autoResize is false', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: ORIGIN, container, autoResize: false });
    e.open('tok');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.style.height).toBe('');
  });
});

describe('resize payload guard', () => {
  it('does not throw or change height when resize payload has no height', () => {
    const onResize = vi.fn();
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: ORIGIN, container, onResize });
    e.open('tok');
    const iframe = mockContentWindow(e);
    const before = iframe.style.height;
    expect(() => post(ORIGIN, 'resize', {})).not.toThrow();
    expect(onResize).toHaveBeenCalledWith({});
    expect(iframe.style.height).toBe(before);
  });
});

describe('connect handshake', () => {
  it('posts an imzala-embed connect message to the iframe on load', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = make({ baseUrl: ORIGIN, container });
    e.open('tok');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    const postMessage = vi.fn();
    Object.defineProperty(iframe, 'contentWindow', { get: () => ({ postMessage }), configurable: true });
    iframe.dispatchEvent(new Event('load'));
    expect(postMessage).toHaveBeenCalledTimes(1);
    const [msg, targetOrigin] = postMessage.mock.calls[0];
    expect(msg).toMatchObject({ type: 'imzala-embed', event: 'connect' });
    expect(msg.payload.origin).toBe(window.location.origin);
    expect(targetOrigin).toBe(ORIGIN); // pinned target origin, never '*'
  });
});

describe('close idempotency', () => {
  it('is safe to call close() before open()', () => {
    const e = make({ baseUrl: ORIGIN });
    expect(() => e.close()).not.toThrow();
  });

  it('is safe to call close() twice', () => {
    const e = make({ baseUrl: ORIGIN });
    e.open('tok');
    e.close();
    expect(() => e.close()).not.toThrow();
    expect(document.querySelector('iframe')).toBeNull();
  });
});
