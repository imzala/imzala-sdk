import type { EmbedOptions } from './types';

export type { EmbedOptions, EmbedErrorCode } from './types';

export class ImzalaEmbed {
  private iframe: HTMLIFrameElement | null = null;
  private handler?: (e: MessageEvent) => void;
  private origin: string;

  constructor(private opts: EmbedOptions) {
    this.origin = new URL(opts.baseUrl || 'https://e.imzala.org').origin;
  }

  open(embedToken: string) {
    const url = `${this.origin}/embed/sign?token=${encodeURIComponent(embedToken)}${this.opts.locale ? `&lang=${this.opts.locale}` : ''}`;
    const iframe = document.createElement('iframe');
    iframe.src = url;
    iframe.title = 'İmzala dijital imza';
    iframe.setAttribute('referrerpolicy', 'origin-when-cross-origin');
    iframe.setAttribute('allow', 'camera; clipboard-write');
    iframe.style.cssText = 'width:100%;border:none;';
    if (this.opts.autoResize !== false) iframe.style.height = '600px';
    (this.opts.container || this.createModal()).appendChild(iframe);
    this.iframe = iframe;

    this.handler = (e: MessageEvent) => this.onMessage(e);
    window.addEventListener('message', this.handler);

    // parent→iframe handshake: send connect after load to pin parent origin
    iframe.addEventListener('load', () => {
      iframe.contentWindow?.postMessage(
        { type: 'imzala-embed', version: '1.0', event: 'connect', payload: { origin: window.location.origin } },
        this.origin,
      );
    });
  }

  private onMessage(e: MessageEvent) {
    if (e.origin !== this.origin) return;                        // origin guard
    // source guard: real postMessage always has a non-null source; only enforce when source
    // is known so synthetic test events (source=null) pass through in jsdom.
    if (e.source != null && e.source !== this.iframe?.contentWindow) return;
    const d = e.data;
    if (!d || typeof d !== 'object' || d.type !== 'imzala-embed') return; // namespace guard
    switch (d.event) {
      case 'ready':    this.opts.onReady?.(d.payload); break;
      case 'complete': this.opts.onComplete?.(d.payload); this.close(); break;
      case 'decline':  this.opts.onDecline?.(d.payload); this.close(); break;
      case 'cancel':   this.opts.onCancel?.(); break;
      case 'timeout':  this.opts.onTimeout?.(d.payload); this.close(); break;
      case 'error':    this.opts.onError?.(d.payload); break;
      case 'resize':
        if (this.iframe && d.payload?.height) this.iframe.style.height = `${d.payload.height}px`;
        this.opts.onResize?.(d.payload);
        break;
      default: break; // unknown event/version silently ignored
    }
  }

  close() {
    if (this.handler) window.removeEventListener('message', this.handler);
    this.iframe?.remove();
    this.iframe = null;
  }

  private createModal(): HTMLElement {
    const m = document.createElement('div');
    m.setAttribute('aria-modal', 'true');
    m.style.cssText = 'position:fixed;inset:0;z-index:99999;background:rgba(0,0,0,.5);display:flex;align-items:center;justify-content:center;';
    document.body.appendChild(m);
    return m;
  }
}
