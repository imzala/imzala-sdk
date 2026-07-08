// İmzala React SDK gömülü imza örneği (@imzala/embed-react).
//
// Akış iki adımdır:
//   1) Sunucunuzda, o taraf için tek kullanımlık bir embed token üretin
//      (server SDK ile, örneğin @imzala/node). API anahtarı asla tarayıcıya inmez.
//   2) Token'i tarayıcıya (bu bileşene) verin; <ImzalaSign /> iframe'i açar
//      ve imza olaylarını callback prop'ları olarak yüzeye çıkarır.
//
// Bu dosya kopyala-yapıstir edilebilecek minimal bir React bileşenidir.
// Gerçek projede token'i asagidaki gibi kendi backend ucunuzdan cekersiniz.

import { useEffect, useState } from 'react';
import { ImzalaSign } from '@imzala/embed-react';

// Backend'ten embed token'i cek. Kendi ucunuz iceride su cagriyi yapar:
//   const { embed_token } = await imzala.embed.createSession(demandId, { partyId });
// ve token'i kimligi dogrulanmis bir response'ta doner.
async function fetchEmbedToken(demandId: string, partyId: string): Promise<string> {
  const res = await fetch('/api/imza/embed-token', {
    method: 'POST',
    headers: { 'content-type': 'application/json' },
    body: JSON.stringify({ demandId, partyId }),
  });
  if (!res.ok) throw new Error('Embed token alinamadi');
  const data = await res.json();
  return data.embed_token as string;
}

export default function App() {
  // Gerçekte bu id'ler sözleşme akisindan gelir. Yer tutucu degerler:
  const demandId = 'DEMAND_ID_YER_TUTUCU';
  const partyId = 'PARTY_ID_YER_TUTUCU';

  const [token, setToken] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [done, setDone] = useState(false);

  useEffect(() => {
    let cancelled = false;
    fetchEmbedToken(demandId, partyId)
      .then((t) => {
        if (!cancelled) setToken(t);
      })
      .catch((e) => {
        if (!cancelled) setError(String(e));
      });
    return () => {
      cancelled = true;
    };
  }, [demandId, partyId]);

  if (error) return <p>Hata: {error}</p>;
  if (done) return <p>Teşekkürler, imzanız alındı.</p>;
  if (!token) return <p>İmza yüzeyi hazırlanıyor…</p>;

  return (
    <div style={{ maxWidth: 720, margin: '0 auto' }}>
      <h1>Sözleşmeyi imzalayın</h1>

      <ImzalaSign
        token={token}
        locale="tr"
        // Iframe yüzeyi yüklendi.
        onReady={() => console.log('İmza yüzeyi hazır')}
        // Taraf imzayi tamamladi (widget otomatik kapanir).
        onComplete={({ demandId, partyId, signedAt }) => {
          console.log('İmzalandi:', demandId, partyId, signedAt);
          setDone(true);
        }}
        // Taraf sözlesmeyi reddetti.
        onDecline={({ reason }) => console.log('Reddedildi:', reason)}
        // Oturum veya token süresi doldu: yeni bir token üretip tekrar deneyin.
        onTimeout={({ code }) => setError(`Oturum süresi doldu: ${code}`)}
        // Hata kodlari: TOKEN_EXPIRED | TOKEN_USED | ORIGIN_DENIED | NETWORK | UNKNOWN
        onError={({ code, message }) => setError(`${code}: ${message ?? ''}`)}
      />
    </div>
  );
}

// İmza sinifi notu: gömülü imza dijital imza (SES/AES) üretir; her imza zaman
// damgalidir. Nitelikli veya güvenli elektronik imza (QES) degildir.
