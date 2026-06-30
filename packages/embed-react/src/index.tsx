'use client';
import { useEffect, useRef } from 'react';
import { ImzalaEmbed, type EmbedOptions } from '@imzala/embed';

export interface ImzalaSignProps extends Omit<EmbedOptions, 'container'> {
  token: string;
}

export function ImzalaSign({ token, ...opts }: ImzalaSignProps) {
  const ref = useRef<HTMLDivElement>(null);
  // Store latest callbacks in a ref to avoid stale closures in useEffect
  const cb = useRef(opts);
  cb.current = opts;

  useEffect(() => {
    if (!ref.current) return;
    const embed = new ImzalaEmbed({
      container: ref.current,
      baseUrl: cb.current.baseUrl,
      locale: cb.current.locale,
      autoResize: cb.current.autoResize,
      onComplete: (p) => cb.current.onComplete?.(p),
      onDecline: (p) => cb.current.onDecline?.(p),
      onCancel: () => cb.current.onCancel?.(),
      onTimeout: (p) => cb.current.onTimeout?.(p),
      onError: (p) => cb.current.onError?.(p),
      onReady: (p) => cb.current.onReady?.(p),
      onResize: (p) => cb.current.onResize?.(p),
      onFieldSigned: (p) => cb.current.onFieldSigned?.(p),
      onFieldUnsigned: (p) => cb.current.onFieldUnsigned?.(p),
    });
    embed.open(token);
    // StrictMode double-effect: cleanup closes, next effect re-opens
    return () => embed.close();
  }, [token]);

  return <div ref={ref} />;
}
