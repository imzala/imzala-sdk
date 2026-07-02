# imzala (Python)

İmzala dijital imza platformu resmi Python SDK'sı.

> ⚠️ Geliştirme aşamasında, yayın öncesi avukat onayı bekliyor. Kapsam ve
> yayın kararları için monorepo kökündeki [RELEASING.md](../../RELEASING.md).

## Kurulum

```bash
pip install imzala
```

Python 3.9+ gerektirir.

## Hızlı başlangıç

```python
import os
from imzala import Imzala

client = Imzala(api_key=os.environ["IMZALA_API_KEY"])

# Aktif şablonlarını listele
templates = client.templates.list()

# Bir şablondan yeni sözleşme (demand) oluştur
demand = client.demands.create({
    "template_id": templates["templates"][0]["id"],
    "party_mapping": [
        {
            "template_party_id": templates["templates"][0]["parties"][0]["id"],
            "first_name": "Ahmet",
            "last_name": "Yılmaz",
            "email": "ahmet@example.com",
        },
    ],
})

print(demand["signing_urls"])  # her taraf için imzalama linki
```

`base_url` varsayılan olarak `https://api-prd.imzala.org`'dur; test ortamı
için `Imzala(api_key=..., base_url="https://test-api.imzala.org")`.

## Kaynaklar

- `client.templates.list(page=None, limit=None)` / `.get(template_id)` /
  `.usage(template_id)`
- `client.demands.create(body)` / `.get(demand_id)` /
  `.add_items(demand_id, body)` /
  `.upload_document(files=[...], parties=[...], order=None, title=None, description=None)` /
  `.send_reminder(demand_id, body=None)`
- `client.embed.create_session(demand_id, party_id=...)`: bkz. aşağıdaki
  gömülü imza notu
- `client.timestamps.create(content=..., filename=..., idempotency_key=None, ...)`
- `client.me()`: API anahtarının sahibi (id, e-posta, workspace, kalan kredi)

Dosya yükleyen metodlar `FileInput` (dataclass: `content: bytes, filename: str,
content_type: Optional[str]`) veya doğrudan `content=`/`filename=` keyword'lerini
kabul eder; Flask/FastAPI upload'larından gelen bytes'ı doğrudan geçebilirsiniz.

## Webhook doğrulama

```python
from flask import Flask, request, abort
from imzala import verify_webhook
import os

app = Flask(__name__)

@app.post("/webhooks/imzala")
def imzala_webhook():
    raw_body = request.get_data()  # ham body, JSON parse ETMEDEN önce
    valid = verify_webhook(
        os.environ["IMZALA_WEBHOOK_SECRET"],
        raw_body,
        request.headers.get("X-Imzala-Signature-256"),
    )
    if not valid:
        abort(401)

    event = request.get_json()
    # ... event["type"]'a göre işle
    return "", 200
```

`verify_webhook` asla exception fırlatmaz; geçersiz/eksik imzada `False`
döner. Body'yi parse edip yeniden serialize etmeyin (FastAPI için
`await request.body()`, Django için `request.body`).

## Hata yönetimi

```python
from imzala import (
    Imzala,
    ImzalaError,
    ImzalaAuthError,
    ImzalaRateLimitError,
    ImzalaValidationError,
)

try:
    client.demands.get(demand_id)
except ImzalaRateLimitError as err:
    print(f"Rate limit: {err.retry_after} sn sonra tekrar dene")
except ImzalaAuthError:
    print("API anahtarı geçersiz veya yetkisiz")
except ImzalaValidationError as err:
    print("İstek doğrulanamadı:", err.body)
except ImzalaError as err:
    print("İmzala API hatası:", err.status_code, str(err))
```

Tüm hatalar `ImzalaError`'dan türer (`status_code`, `body`, `code` alanları
ortak); 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError`
(`retry_after` saniye), 422 → `ImzalaValidationError`.

## ⚠️ Sunucu-taraflı

Bu paket **sadece sunucuda** kullanılır. API anahtarınızı tarayıcı
JavaScript'ine veya mobil uygulamaya asla gömmeyin: sızarsa hesabınızdaki
tüm sözleşme/şablon/zaman damgası işlemlerine erişim kazanılır. Tarayıcıda
imza almak için [`@imzala/embed`](../embed) veya
[`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı notu

İmzala dijital imza (SES) üretir; her imza zaman damgalıdır. Nitelikli/
güvenli elektronik imza (QES) DEĞİLDİR.

`client.embed.create_session(...)` şu an **yalnızca test ortamında** çalışır;
gömülü imza özelliği henüz avukat onaylı prod-canlı değildir. Kapsam kararı
[RELEASING.md](../../RELEASING.md)'de.

## Daha fazla

- [Monorepo README](../../README.md): tüm paketler + genel desen
- [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
