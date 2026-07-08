# imzala

[![PyPI](https://img.shields.io/pypi/v/imzala.svg)](https://pypi.org/project/imzala/)
[![Python](https://img.shields.io/pypi/pyversions/imzala.svg)](https://pypi.org/project/imzala/)
[![PyPI downloads](https://img.shields.io/pypi/dm/imzala.svg)](https://pypi.org/project/imzala/)

İmzala dijital imza platformunun resmi **Python** SDK'sı. Sözleşme oluşturma, imza takibi, imzalı PDF ve sertifika indirme, denetim izi, şablon yönetimi ve zaman damgası işlemlerini tek bir tip-güvenli istemciyle yapın.

```bash
pip install imzala
```

> **Sunucu-taraflı paket.** API anahtarınız hesabınızın tamamına erişir; tarayıcıya veya mobil uygulamaya gömmeyin. Tarayıcıda imza almak için [`@imzala/embed`](../embed) kullanın. Ayrıntı: [Sunucu-taraflı](#sunucu-taraflı) bölümü.

## İçindekiler

- [Gereksinimler](#gereksinimler)
- [Hızlı başlangıç](#hızlı-başlangıç)
- [Yapılandırma](#yapılandırma)
- [API referansı](#api-referansı)
  - [Sözleşmeler (demands)](#sözleşmeler-demands)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sunucu-taraflı](#sunucu-taraflı)
- [İmza sınıfı](#imza-sınıfı)

## Gereksinimler

- Python **3.9+**
- `imz_` ile başlayan bir API anahtarı: Panel, Geliştirici, API Anahtarları

## Hızlı başlangıç

```python
import os
from imzala import Imzala

imzala = Imzala(api_key=os.environ["IMZALA_API_KEY"])

# 1) Şablonları listele, birini seç
templates = imzala.templates.list()
template = templates.templates[0]

# 2) Şablondan sözleşme oluştur (imza daveti otomatik gider)
demand = imzala.demands.create({
    "template_id": template.id,
    "party_mapping": [
        {
            "template_party_id": template.parties[0].id,
            "first_name": "Ahmet",
            "last_name": "Yılmaz",
            "email": "ahmet@example.com",
            "phone": "+905301112233",
        },
    ],
})
for u in demand.signing_urls or []:
    print(u.signing_url)  # her taraf için imzalama linki

# 3) Durumu takip et
status = imzala.demands.get(demand.id)
signed = sum(1 for p in (status.parties or []) if p.signed)
print(f"{signed} taraf imzaladı")

# 4) Tamamlanınca imzalı PDF'i indir
if status.status == "COMPLETED":
    pdf = imzala.demands.get_pdf(demand.id)  # bytes
    with open("sozlesme.pdf", "wb") as fh:
        fh.write(pdf)
```

## Yapılandırma

```python
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    base_url="https://api-prd.imzala.org",  # varsayılan; test için test-api.imzala.org
    timeout=30.0,          # istek başına zaman aşımı, saniye (varsayılan 30.0)
    max_retries=2,         # güvenli GET'ler için (varsayılan 2, 0 = kapalı)
    retry_base_delay=0.3,  # backoff temel gecikmesi, saniye (varsayılan 0.3)
)
```

| Seçenek | Tip | Varsayılan | Açıklama |
|---|---|---|---|
| `api_key` | `str` | zorunlu | `imz_<64 hex>` |
| `base_url` | `str` | `https://api-prd.imzala.org` | Test: `https://test-api.imzala.org` |
| `timeout` | `float` | `30.0` | İstek başına zaman aşımı (saniye) |
| `max_retries` | `int` | `2` | Yalnızca idempotent GET'ler; `0` kapatır |
| `retry_base_delay` | `float` | `0.3` | Exponential backoff + jitter (saniye) |

## API referansı

Her metod `{success, data}` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaError` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)).

Dönen değerler tiplenmiş modellerdir; öznitelikle okuyun (`me.email`, `status.parties[0].signed`). Düz `dict` isterseniz `.to_dict()` (veya pydantic `.model_dump()`) kullanın.

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands.create(body)` | Şablondan sözleşme oluştur + imza daveti gönder | Hayır (POST) |
| `demands.upload_document(files=[...], parties=[...], order=None, title=None, description=None)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | Hayır |
| `demands.list(status=None, q=None, from_=None, to=None, template_id=None, page=None, limit=None, sort=None)` | Sözleşme listesi (counts-only, taraf PII'siz) | Evet (GET) |
| `demands.get(demand_id)` | Sözleşme detayı + taraf imza durumu (maskeli) | Evet (GET) |
| `demands.get_pdf(demand_id)` | İmzalı sözleşme PDF'i, `bytes` döner | Evet (GET) |
| `demands.get_certificate(demand_id, lang=None)` | Tamamlanma sertifikası (PAdES B-T), `bytes` döner | Evet (GET) |
| `demands.get_timeline(demand_id)` | İmza denetim izi (maskeli olaylar) | Evet (GET) |
| `demands.cancel(demand_id, body=None)` | Bekleyen sözleşmeyi iptal et | Hayır (POST) |
| `demands.resend_party(demand_id, party_id)` | Tekil tarafa daveti tekrar gönder | Hayır (POST) |
| `demands.delete(demand_id)` | Tamamlanmamış sözleşmeyi sil | Hayır (DELETE) |
| `demands.add_items(demand_id, body)` | Sayfa alanlarını (imza/form) yerleştir | Hayır (POST) |
| `demands.send_reminder(demand_id, body=None)` | İmzalamamış taraflara hatırlatma | Hayır (POST) |

```python
# Filtreli liste (from_ sondaki alt çizgiyle: `from` bir Python anahtar kelimesi)
listing = imzala.demands.list(status="PENDING", limit=20, sort="createdAt:desc")
for d in listing.demands or []:
    print(d.id, d.status, f"{d.parties_signed}/{d.parties_total}")

# İptal
imzala.demands.cancel(demand_id, {"reason": "Anlaşma değişti"})

# Denetim izi
timeline = imzala.demands.get_timeline(demand_id)
for e in timeline.events or []:
    print(e.created_at, e.event_type, e.ip_masked)
```

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates.list(page=None, limit=None)` | Aktif şablonlar (tek sayfa) | Evet (GET) |
| `templates.list_all(page=None, limit=None)` | Tüm şablonları gezen iterator | Evet (GET) |
| `templates.get(template_id)` | Şablon detayı + taraflar + doldurulabilir alanlar | Evet (GET) |
| `templates.usage(template_id)` | API kullanım kılavuzu (curl + JSON örneği) | Evet (GET) |
| `templates.update(template_id, body)` | Şablon metadata güncelle (`name` / `description` / `category`) | Hayır (PATCH) |
| `templates.delete(template_id)` | Şablon sil (soft-delete) | Hayır (DELETE) |

### Gömülü imza (embed)

```python
session = imzala.embed.create_session(demand_id, party_id=party_id)
# session.embed_url -> bir <iframe>'e gömün (bkz. @imzala/embed)
```

Gömülü imza yalnızca dijital imza (SES/AES) üretir, QES değil. Tarayıcı tarafı için [`@imzala/embed`](../embed) / [`@imzala/embed-react`](../embed-react).

### Zaman damgası (timestamps)

```python
with open("belge.pdf", "rb") as fh:
    content = fh.read()

ts = imzala.timestamps.create(
    content=content,               # bytes
    filename="belge.pdf",
    idempotency_key="unique-key",  # tekrarları güvenli yapar (5dk pencere)
)
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir). `description`, `owner_first_name`, `owner_last_name` opsiyoneldir.

### Hesap (me)

```python
me = imzala.me()
print(me.email, me.credits)  # id, e-posta, workspace, kalan kredi
```

## İmzalı PDF ve sertifika (binary)

`get_pdf` ve `get_certificate` ham baytları `bytes` olarak döndürür (JSON değil). Diske yazın ya da stream'leyin:

```python
pdf = imzala.demands.get_pdf(demand_id)
with open("sozlesme.pdf", "wb") as fh:
    fh.write(pdf)

cert = imzala.demands.get_certificate(demand_id, lang="tr")
with open("sertifika.pdf", "wb") as fh:
    fh.write(cert)
```

## Otomatik yeniden deneme

Yalnızca **GET (okuma)** metodları, yani `list` / `get` / `get_pdf` / `get_certificate` / `get_timeline`, `templates.*` okumaları ve `me()`, 429 (`Retry-After` header'ına uyarak) veya 5xx aldığında jitter'lı exponential backoff ile yeniden denenir.

```python
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    max_retries=2,          # varsayılan 2 deneme; 0 = kapalı
    retry_base_delay=0.3,   # saniye cinsinden taban gecikme
)
```

**POST/PATCH/DELETE (yazma) metodları ASLA yeniden denenmez** ve bu yapılandırılamaz: tekrarlanan bir `demands.create` mükerrer sözleşme oluşturur, tekrarlanan bir `send_reminder` çift SMS/e-posta gönderir. Yeniden deneme sarmalayıcısı yapısal olarak yalnızca GET çağrılarına uygulanır; yazma metodlarının bu yola girme imkanı yoktur.

## Sayfalama

`templates.list()` tek sayfa döner; tüm aktif şablonlarınızı tek tek dolaşmak için `list_all()` kullanın:

```python
for template in imzala.templates.list_all(limit=50):
    print(template.id, template.name)
```

Bir sayfa `limit`'ten az öğe döndürünce ya da yanıttaki `total`'a ulaşınca durur (sonsuz döngü yok).

## Webhook doğrulama

```python
from flask import Flask, request, abort
from imzala import verify_webhook
import os

app = Flask(__name__)

@app.post("/webhooks/imzala")
def imzala_webhook():
    raw_body = request.get_data()  # ham body, JSON parse ETMEDEN
    valid = verify_webhook(
        os.environ["IMZALA_WEBHOOK_SECRET"],           # whsec_...
        raw_body,                                       # bytes
        request.headers.get("X-Imzala-Signature-256"),  # "sha256=<hex>"
    )
    if not valid:
        abort(401)

    event = request.get_json()
    # event["type"]: demand.created / demand.completed / demand.expired
    #                party.signed / party.viewed / party.rejected
    return "", 200
```

`verify_webhook` asla exception fırlatmaz; geçersiz/eksik imzada `False` döner. Body'yi parse edip yeniden serialize etmeyin (imza byte-byte karşılaştırılır). Ham body okuma: Flask `request.get_data()`, FastAPI/Starlette `await request.body()`, Django `request.body`.

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
    imzala.demands.get(demand_id)
except ImzalaRateLimitError as err:
    print(f"Rate limit: {err.retry_after} sn sonra tekrar dene")
except ImzalaAuthError:
    print("API anahtarı geçersiz veya yetkisiz")
except ImzalaValidationError as err:
    print("İstek doğrulanamadı:", err.body)
except ImzalaError as err:
    print("İmzala API hatası:", err.status_code, str(err))
```

Tüm hatalar `ImzalaError`'dan türer (ortak alanlar: `status_code`, `body`, `code`). 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError` (`retry_after` saniye), 422 → `ImzalaValidationError`.

## Sunucu-taraflı

⚠️ Bu paket **yalnızca sunucuda** kullanılır. API anahtarınızı tarayıcı JavaScript'ine veya mobil uygulamaya asla gömmeyin: sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. Tarayıcıda imza almak için [`@imzala/embed`](../embed) veya [`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da dijital imza (SES/AES) üretir.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnek: [`examples/python/lifecycle.py`](../../examples/python/lifecycle.py)
- [Monorepo README](../../README.md) · [RELEASING.md](../../RELEASING.md): sürüm yayınlama, kapsam kararları
