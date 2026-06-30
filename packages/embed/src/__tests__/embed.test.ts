import { afterEach, describe, expect, it, vi } from 'vitest';
import { ImzalaEmbed } from '../index';

afterEach(() => {
  // clean up any modal divs or iframes added to body between tests
  document.body.innerHTML = '';
});

describe('origin guard', () => {
  it('ignores messages from wrong origin', () => {
    const onComplete = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onComplete });
    e.open('a'.repeat(64));
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://evil.com',
        data: { type: 'imzala-embed', version: '1.0', event: 'complete', payload: {} },
      }),
    );
    expect(onComplete).not.toHaveBeenCalled();
  });
});

describe('event dispatch', () => {
  it('fires onComplete for correct origin + namespace', () => {
    const onComplete = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onComplete });
    e.open('a'.repeat(64));
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://e.imzala.org',
        data: {
          type: 'imzala-embed',
          version: '1.0',
          event: 'complete',
          payload: { demandId: 'd', partyId: 'p' },
        },
      }),
    );
    expect(onComplete).toHaveBeenCalledWith({ demandId: 'd', partyId: 'p' });
  });

  it('ignores messages with wrong namespace (type)', () => {
    const onComplete = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onComplete });
    e.open('a'.repeat(64));
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://e.imzala.org',
        data: { type: 'other-widget', event: 'complete', payload: {} },
      }),
    );
    expect(onComplete).not.toHaveBeenCalled();
  });

  it('fires onDecline and closes on decline event', () => {
    const onDecline = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onDecline });
    e.open('a'.repeat(64));
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://e.imzala.org',
        data: { type: 'imzala-embed', version: '1.0', event: 'decline', payload: { reason: 'test' } },
      }),
    );
    expect(onDecline).toHaveBeenCalledWith({ reason: 'test' });
  });

  it('fires onReady', () => {
    const onReady = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onReady });
    e.open('a'.repeat(64));
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://e.imzala.org',
        data: { type: 'imzala-embed', version: '1.0', event: 'ready', payload: {} },
      }),
    );
    expect(onReady).toHaveBeenCalledWith({});
  });

  it('fires onError', () => {
    const onError = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onError });
    e.open('a'.repeat(64));
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://e.imzala.org',
        data: { type: 'imzala-embed', version: '1.0', event: 'error', payload: { code: 'TOKEN_EXPIRED' } },
      }),
    );
    expect(onError).toHaveBeenCalledWith({ code: 'TOKEN_EXPIRED' });
  });

  it('applies auto-resize when resize event arrives', () => {
    const onResize = vi.fn();
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', container, onResize });
    e.open('a'.repeat(64));
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://e.imzala.org',
        data: { type: 'imzala-embed', version: '1.0', event: 'resize', payload: { height: 800 } },
      }),
    );
    expect(onResize).toHaveBeenCalledWith({ height: 800 });
    expect(iframe.style.height).toBe('800px');
  });

  it('silently ignores unknown events', () => {
    const onComplete = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onComplete });
    e.open('a'.repeat(64));
    expect(() => {
      window.dispatchEvent(
        new MessageEvent('message', {
          origin: 'https://e.imzala.org',
          data: { type: 'imzala-embed', version: '2.0', event: 'future_event', payload: {} },
        }),
      );
    }).not.toThrow();
    expect(onComplete).not.toHaveBeenCalled();
  });
});

describe('close', () => {
  it('removes message listener after close', () => {
    const onComplete = vi.fn();
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', onComplete });
    e.open('a'.repeat(64));
    e.close();
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: 'https://e.imzala.org',
        data: { type: 'imzala-embed', version: '1.0', event: 'complete', payload: { demandId: 'd', partyId: 'p' } },
      }),
    );
    expect(onComplete).not.toHaveBeenCalled();
  });
});

describe('iframe mount', () => {
  it('appends iframe to provided container', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', container });
    e.open('tok123');
    expect(container.querySelector('iframe')).not.toBeNull();
  });

  it('creates modal and appends iframe when no container', () => {
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org' });
    e.open('tok123');
    expect(document.querySelector('[aria-modal="true"] iframe')).not.toBeNull();
  });

  it('encodes embed token in iframe src', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', container });
    e.open('tok/special');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.src).toContain('tok%2Fspecial');
  });

  it('appends locale param when specified', () => {
    const container = document.createElement('div');
    document.body.appendChild(container);
    const e = new ImzalaEmbed({ baseUrl: 'https://e.imzala.org', container, locale: 'tr' });
    e.open('abc');
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe.src).toContain('lang=tr');
  });
});
