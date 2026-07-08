import { describe, it, expect, vi, afterEach } from 'vitest';
import { render } from '@testing-library/react';
import { ImzalaSign } from '../index';

// Gömülü imza iframe'inin varsayılan origin'i (baseUrl verilmezse).
const EMBED_ORIGIN = 'https://e.imzala.org';

/**
 * İmzala embed iframe'inden gelen bir postMessage olayını simüle eder.
 * Gerçek widget yalnızca doğru origin + doğru source + doğru namespace
 * ('imzala-embed') olan mesajları işler; testler bu koşulları taklit eder.
 */
function postFromIframe(
  iframe: HTMLIFrameElement,
  event: string,
  payload?: unknown,
  opts: { origin?: string; source?: MessageEventSource | null } = {},
) {
  window.dispatchEvent(
    new MessageEvent('message', {
      origin: opts.origin ?? EMBED_ORIGIN,
      source: 'source' in opts ? opts.source ?? null : iframe.contentWindow,
      data: { type: 'imzala-embed', event, payload },
    }),
  );
}

function getIframe(root: ParentNode): HTMLIFrameElement | null {
  return root.querySelector('iframe');
}

afterEach(() => {
  // Testlerin dokümanda bıraktığı artık iframe/modal olmadığından emin ol
  // (@testing-library otomatik cleanup RTL container'ını kaldırır; elle
  // eklenen yabancı iframe'leri ilgili test kendi içinde temizler).
  vi.clearAllMocks();
});

describe('ImzalaSign: mount', () => {
  it('bir container render eder ve iframe mount eder', () => {
    const { container } = render(<ImzalaSign token={'a'.repeat(64)} />);
    expect(getIframe(container)).toBeTruthy();
  });

  it('container verildiğinde tam-ekran modal (aria-modal) açmaz', () => {
    const { container } = render(<ImzalaSign token={'a'.repeat(64)} />);
    expect(getIframe(container)).toBeTruthy();
    expect(document.querySelector('[aria-modal]')).toBeNull();
  });

  it('baseUrl origin\'ini iframe src\'ine geçirir', () => {
    const { container } = render(
      <ImzalaSign token={'b'.repeat(64)} baseUrl="https://custom.example.org" />,
    );
    const iframe = getIframe(container)!;
    expect(iframe).toBeTruthy();
    expect(iframe.src).toContain('custom.example.org');
  });

  it('locale\'i lang query parametresi olarak iframe src\'ine ekler', () => {
    const { container } = render(<ImzalaSign token={'c'.repeat(64)} locale="tr" />);
    expect(getIframe(container)!.src).toContain('lang=tr');
  });

  it('token\'i iframe src\'ine (encode edilmiş) yerleştirir', () => {
    const token = 'e'.repeat(64);
    const { container } = render(<ImzalaSign token={token} />);
    expect(getIframe(container)!.src).toContain(`token=${token}`);
  });
});

describe('ImzalaSign: olay/callback köprüsü', () => {
  it('onComplete callback\'ini payload ile çağırır', () => {
    const onComplete = vi.fn();
    const { container } = render(
      <ImzalaSign token={'c'.repeat(64)} onComplete={onComplete} />,
    );
    const payload = { demandId: 'd1', partyId: 'p1', signedAt: '2026-07-08T10:00:00Z' };
    postFromIframe(getIframe(container)!, 'complete', payload);
    expect(onComplete).toHaveBeenCalledWith(payload);
  });

  it('onDecline callback\'ini reason payload\'u ile çağırır', () => {
    const onDecline = vi.fn();
    const { container } = render(
      <ImzalaSign token={'d'.repeat(64)} onDecline={onDecline} />,
    );
    postFromIframe(getIframe(container)!, 'decline', { reason: 'Şartları kabul etmiyorum' });
    expect(onDecline).toHaveBeenCalledWith({ reason: 'Şartları kabul etmiyorum' });
  });

  it('onReady callback\'ini iframe hazır olduğunda çağırır', () => {
    const onReady = vi.fn();
    const { container } = render(
      <ImzalaSign token={'r'.repeat(64)} onReady={onReady} />,
    );
    postFromIframe(getIframe(container)!, 'ready', { version: '1.0' });
    expect(onReady).toHaveBeenCalledWith({ version: '1.0' });
  });

  it('onError callback\'ini hata payload\'u ile çağırır', () => {
    const onError = vi.fn();
    const { container } = render(
      <ImzalaSign token={'x'.repeat(64)} onError={onError} />,
    );
    postFromIframe(getIframe(container)!, 'error', { code: 'TOKEN_USED', message: 'kullanılmış' });
    expect(onError).toHaveBeenCalledWith({ code: 'TOKEN_USED', message: 'kullanılmış' });
  });

  it('onCancel callback\'ini argümansız çağırır', () => {
    const onCancel = vi.fn();
    const { container } = render(
      <ImzalaSign token={'k'.repeat(64)} onCancel={onCancel} />,
    );
    postFromIframe(getIframe(container)!, 'cancel');
    expect(onCancel).toHaveBeenCalledTimes(1);
    expect(onCancel).toHaveBeenCalledWith();
  });

  it('onTimeout callback\'ini süre dolduğunda çağırır', () => {
    const onTimeout = vi.fn();
    const { container } = render(
      <ImzalaSign token={'t'.repeat(64)} onTimeout={onTimeout} />,
    );
    postFromIframe(getIframe(container)!, 'timeout', { code: 'TOKEN_EXPIRED' });
    expect(onTimeout).toHaveBeenCalledWith({ code: 'TOKEN_EXPIRED' });
  });

  it('onResize callback\'ini yeni yükseklikle çağırır', () => {
    const onResize = vi.fn();
    const { container } = render(
      <ImzalaSign token={'z'.repeat(64)} onResize={onResize} />,
    );
    postFromIframe(getIframe(container)!, 'resize', { height: 720 });
    expect(onResize).toHaveBeenCalledWith({ height: 720 });
  });

  it('onFieldSigned / onFieldUnsigned callback\'lerini alan olaylarında çağırır', () => {
    const onFieldSigned = vi.fn();
    const onFieldUnsigned = vi.fn();
    const { container } = render(
      <ImzalaSign
        token={'f'.repeat(64)}
        onFieldSigned={onFieldSigned}
        onFieldUnsigned={onFieldUnsigned}
      />,
    );
    const iframe = getIframe(container)!;
    postFromIframe(iframe, 'field_signed', { fieldId: 'sig-1', fieldType: 'signature' });
    postFromIframe(iframe, 'field_unsigned', { fieldId: 'sig-1', fieldType: 'signature' });
    expect(onFieldSigned).toHaveBeenCalledWith({ fieldId: 'sig-1', fieldType: 'signature' });
    expect(onFieldUnsigned).toHaveBeenCalledWith({ fieldId: 'sig-1', fieldType: 'signature' });
  });
});

describe('ImzalaSign: terminal olaylarda otomatik kapanma', () => {
  it('complete olayından sonra iframe\'i kaldırır', () => {
    const { container } = render(<ImzalaSign token={'c'.repeat(64)} />);
    postFromIframe(getIframe(container)!, 'complete', { demandId: 'd1', partyId: 'p1' });
    expect(getIframe(container)).toBeNull();
  });

  it('decline olayından sonra iframe\'i kaldırır', () => {
    const { container } = render(<ImzalaSign token={'d'.repeat(64)} />);
    postFromIframe(getIframe(container)!, 'decline', { reason: 'ret' });
    expect(getIframe(container)).toBeNull();
  });

  it('resize (terminal olmayan) olaydan sonra iframe açık kalır', () => {
    const { container } = render(<ImzalaSign token={'z'.repeat(64)} />);
    postFromIframe(getIframe(container)!, 'resize', { height: 500 });
    expect(getIframe(container)).toBeTruthy();
  });
});

describe('ImzalaSign: güvenlik koruması (origin/source/namespace)', () => {
  it('yabancı bir origin\'den gelen mesajı yok sayar', () => {
    const onComplete = vi.fn();
    const { container } = render(
      <ImzalaSign token={'c'.repeat(64)} onComplete={onComplete} />,
    );
    postFromIframe(getIframe(container)!, 'complete', { demandId: 'd1', partyId: 'p1' }, {
      origin: 'https://evil.example.com',
    });
    expect(onComplete).not.toHaveBeenCalled();
  });

  it('yanlış bir source (başka bir frame) mesajını yok sayar', () => {
    const onComplete = vi.fn();
    const { container } = render(
      <ImzalaSign token={'c'.repeat(64)} onComplete={onComplete} />,
    );
    // Beklenen iframe dışındaki bir kaynaktan gelen mesaj reddedilmeli.
    const foreign = document.createElement('iframe');
    document.body.appendChild(foreign);
    try {
      postFromIframe(getIframe(container)!, 'complete', { demandId: 'd1', partyId: 'p1' }, {
        source: foreign.contentWindow,
      });
      expect(onComplete).not.toHaveBeenCalled();
    } finally {
      foreign.remove();
    }
  });

  it('yanlış namespace (type) taşıyan mesajı yok sayar', () => {
    const onComplete = vi.fn();
    const { container } = render(
      <ImzalaSign token={'c'.repeat(64)} onComplete={onComplete} />,
    );
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: EMBED_ORIGIN,
        source: getIframe(container)!.contentWindow,
        data: { type: 'some-other-widget', event: 'complete', payload: { demandId: 'd1' } },
      }),
    );
    expect(onComplete).not.toHaveBeenCalled();
  });
});

describe('ImzalaSign: yaşam döngüsü', () => {
  it('temiz unmount olur (unmount sonrası iframe kalmaz)', () => {
    const { container, unmount } = render(<ImzalaSign token={'d'.repeat(64)} />);
    expect(getIframe(container)).toBeTruthy();
    unmount();
    expect(getIframe(container)).toBeNull();
  });

  it('unmount sonrası gelen mesajlar callback tetiklemez (listener temizlenir)', () => {
    const onComplete = vi.fn();
    const { container, unmount } = render(
      <ImzalaSign token={'c'.repeat(64)} onComplete={onComplete} />,
    );
    const iframe = getIframe(container)!;
    unmount();
    // Kaldırılmış iframe'in contentWindow'u üzerinden mesaj göndermeyi dene.
    window.dispatchEvent(
      new MessageEvent('message', {
        origin: EMBED_ORIGIN,
        source: iframe.contentWindow,
        data: { type: 'imzala-embed', event: 'complete', payload: { demandId: 'd1' } },
      }),
    );
    expect(onComplete).not.toHaveBeenCalled();
  });

  it('yeni bir token ile re-render iframe\'i yeniden mount eder (eski kapatılır)', () => {
    const { container, rerender } = render(<ImzalaSign token={'a'.repeat(64)} />);
    const first = getIframe(container)!;
    expect(first.src).toContain('a'.repeat(64));

    rerender(<ImzalaSign token={'b'.repeat(64)} />);

    const iframes = container.querySelectorAll('iframe');
    expect(iframes).toHaveLength(1); // eski iframe kapandı, tek iframe kaldı
    const second = getIframe(container)!;
    expect(second.src).toContain('b'.repeat(64));
    expect(second.src).not.toContain('a'.repeat(64));
    expect(second).not.toBe(first); // gerçekten yeni bir iframe
  });

  it('aynı token ile re-render iframe\'i yeniden mount ETMEZ', () => {
    const { container, rerender } = render(
      <ImzalaSign token={'a'.repeat(64)} onComplete={vi.fn()} />,
    );
    const first = getIframe(container)!;
    rerender(<ImzalaSign token={'a'.repeat(64)} onComplete={vi.fn()} />);
    const second = getIframe(container)!;
    expect(second).toBe(first); // aynı iframe korunur (gereksiz remount yok)
  });

  it('yeni bir callback ile re-render en güncel callback\'i çağırır (stale closure koruması)', () => {
    const first = vi.fn();
    const second = vi.fn();
    const { container, rerender } = render(
      <ImzalaSign token={'a'.repeat(64)} onComplete={first} />,
    );
    // Aynı token, farklı callback: iframe remount edilmez ama güncel callback kullanılmalı.
    rerender(<ImzalaSign token={'a'.repeat(64)} onComplete={second} />);
    postFromIframe(getIframe(container)!, 'complete', { demandId: 'd1', partyId: 'p1' });
    expect(first).not.toHaveBeenCalled();
    expect(second).toHaveBeenCalledWith({ demandId: 'd1', partyId: 'p1' });
  });
});
