import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render } from '@testing-library/react';
import { ImzalaSign } from '../index';

describe('ImzalaSign', () => {
  it('renders a container and mounts iframe', () => {
    const { container } = render(<ImzalaSign token={'a'.repeat(64)} />);
    expect(container.querySelector('iframe')).toBeTruthy();
  });

  it('passes baseUrl to the embed', () => {
    const { container } = render(
      <ImzalaSign token={'b'.repeat(64)} baseUrl="https://custom.example.org" />,
    );
    const iframe = container.querySelector('iframe') as HTMLIFrameElement;
    expect(iframe).toBeTruthy();
    expect(iframe.src).toContain('custom.example.org');
  });

  it('calls onComplete callback when message is received', () => {
    const onComplete = vi.fn();
    render(<ImzalaSign token={'c'.repeat(64)} onComplete={onComplete} />);

    const iframe = document.querySelector('iframe') as HTMLIFrameElement;
    // Simulate iframe postMessage (from embed's origin)
    const origin = 'https://e.imzala.org';
    const payload = { demandId: 'd1', partyId: 'p1' };
    window.dispatchEvent(
      new MessageEvent('message', {
        origin,
        source: iframe.contentWindow,
        data: { type: 'imzala-embed', event: 'complete', payload },
      }),
    );

    expect(onComplete).toHaveBeenCalledWith(payload);
  });

  it('unmounts cleanly (no iframe after unmount)', () => {
    const { container, unmount } = render(<ImzalaSign token={'d'.repeat(64)} />);
    expect(container.querySelector('iframe')).toBeTruthy();
    unmount();
    expect(container.querySelector('iframe')).toBeNull();
  });
});
