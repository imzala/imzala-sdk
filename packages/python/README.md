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
  - [Çok belgeli zarf (demands.documents, dispatch)](#çok-belgeli-zarf-demandsdocuments-dispatch)
  - [Şablonlar (templates)](#şablonlar-templates)
  - [Alan Şablonları (field_templates)](#alan-şablonları-field_templates)
  - [Kişiler (contacts)](#kişiler-contacts)
  - [Raporlar (reports)](#raporlar-reports)
  - [Gömülü imza (embed)](#gömülü-imza-embed)
  - [Zaman damgası (timestamps)](#zaman-damgası-timestamps)
  - [Hesap (me)](#hesap-me)
- [İmzalı PDF ve sertifika (binary)](#imzalı-pdf-ve-sertifika-binary)
- [Otomatik yeniden deneme](#otomatik-yeniden-deneme)
- [Sayfalama](#sayfalama)
- [Webhook doğrulama](#webhook-doğrulama)
- [Hata yönetimi](#hata-yönetimi)
- [Sık karşılaşılan hatalar](#sık-karşılaşılan-hatalar)
- [Sürüm uyumu](#sürüm-uyumu)
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
            "first_name": "Ayşe",
            "last_name": "Yılmaz",
            "email": "ayse@example.com",
            "phone": "+905551112233",
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
| `max_retries` | `int` | `2` | Yalnızca GET'ler; `0` kapatır (bkz. [Otomatik yeniden deneme](#otomatik-yeniden-deneme)) |
| `retry_base_delay` | `float` | `0.3` | Exponential backoff + jitter (saniye) |

API anahtarı ve `Idempotency-Key` değerleri yazdırılabilir ASCII olmalıdır; dosyadan okunan anahtarın sonundaki satır sonu gibi karakterler istek gönderilmeden `ImzalaValidationError` fırlatır (`status_code` `None`).

`X-Workspace-Id` başlığı için bir seçenek yoktur: organizasyon içinde üretilmiş anahtar kendi organizasyonuna bağlıdır, ayrıca başlık gerekmez. Kişisel anahtarla bir organizasyon adına çalışmak bu sürümde desteklenmez.

## API referansı

Her metod `{success, data}` zarfını açar ve `data`'yı döndürür; hata durumunda tipli bir `ImzalaError` fırlatır (bkz. [Hata yönetimi](#hata-yönetimi)).

Dönen değerler tiplenmiş modellerdir; öznitelikle okuyun (`me.email`, `status.parties[0].signed`). Düz `dict` isterseniz `.to_dict()` (veya pydantic `.model_dump()`) kullanın.

### Sözleşmeler (demands)

| Metod | Açıklama | Retry |
|---|---|---|
| `demands.create(body, idempotency_key=None)` | Şablondan sözleşme oluştur + imza daveti gönder (`dispatch_notifications: False` ile sessiz taslak) | Anahtar varsa 429'da 1 kez |
| `demands.create_bulk(body)` | Tek istekte en çok 10 sözleşme; her satır bağımsız, `failed` sayısını ve her satırın durumunu kontrol edin | Hayır (POST) |
| `demands.upload_document(files=[...], parties=[...], order=None, title=None, description=None, idempotency_key=None, field_template_id=None, on_anchor_miss=None, send_invitations=None, force=False)` | Şablonsuz, dosya yükleyerek sözleşme (1 PDF/DOC ya da 1-20 görsel) | Anahtar varsa 429'da 1 kez |
| `demands.list(status=None, q=None, from_=None, to=None, template_id=None, page=None, limit=None, sort=None)` | Sözleşme listesi (counts-only, taraf PII'siz); `sort` biçimi `alan:yön` (ör. `createdAt:desc`) | Evet (GET) |
| `demands.get(demand_id)` | Sözleşme detayı + taraf imza durumu (maskeli) | Evet (GET) |
| `demands.get_pdf(demand_id)` | İmzalı sözleşme PDF'i, `bytes` döner | Evet (GET) |
| `demands.get_document_pdf(demand_id, document_id)` | Çok belgeli zarfta tek belgenin imzalı PDF'i, `bytes` döner | Evet (GET) |
| `demands.get_certificate(demand_id, lang=None)` | Tamamlanma sertifikası (PAdES B-T), `bytes` döner | Evet (GET) |
| `demands.get_timeline(demand_id)` | İmza denetim izi (maskeli olaylar) | Evet (GET) |
| `demands.cancel(demand_id, body=None)` | Bekleyen sözleşmeyi iptal et | Hayır (POST) |
| `demands.resend_party(demand_id, party_id)` | Tekil tarafa daveti tekrar gönder | Hayır (POST) |
| `demands.delete(demand_id)` | Tamamlanmamış sözleşmeyi sil | Hayır (DELETE) |
| `demands.add_items(demand_id, body)` | Sayfa alanlarını (imza/form) yerleştir (`PAGE_ID_REQUIRED`, `INVALID_ITEM_TYPE`) | Hayır (POST) |
| `demands.send_reminder(demand_id, body=None)` | İmzalamamış taraflara hatırlatma (5 dk pencerede `RATE_LIMITED`, `{"force": True}` aşar) | Hayır (POST) |
| `demands.dispatch(demand_id, send_invitations=None)` | Sessiz hazırlanmış sözleşmeyi yayına al, davetleri gönder | Hayır (POST) |

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

# Tekrar-güvenli oluşturma: aynı anahtarla ikinci istek ikinci sözleşme üretmez
created = imzala.demands.create(body, idempotency_key=f"siparis-{order_id}")

# Alan Şablonu ile yükleme: her tarafta template_party_id zorunludur
uploaded = imzala.demands.upload_document(
    files=[FileInput(pdf_bytes, "sozlesme.pdf")],
    parties=[UploadPartyInput("Ayşe", "Yılmaz", "ayse@example.com", template_party_id=party_template_id)],
    field_template_id=field_template_id,
    on_anchor_miss="block",  # çapa bulunamazsa sözleşme oluşturulmaz, kredi düşmez
    idempotency_key=f"yukleme-{order_id}",
)
```

### Çok belgeli zarf (demands.documents, dispatch)

Bir sözleşme `dispatch_notifications: False` ile sessizce oluşturulur, belgeler eklenir, sonra tek çağrıyla yayına alınır. Belge uçları kimseye bildirim göndermez ve kredi düşmez; kredi yalnız `dispatch` anında düşer.

| Metod | Açıklama | Retry |
|---|---|---|
| `demands.documents.list(demand_id, view=None)` | Zarftaki belgeler (`view="wizard"` atama ve karar sayılarını da verir) | Evet (GET) |
| `demands.documents.create(demand_id, body)` | Mevcut bir belgeyi zarfa ekle | Hayır (POST) |
| `demands.documents.upload(demand_id, file=..., title=..., idempotency_key=..., doc_kind=None, is_required=None)` | Dosya yükleyerek belge ekle; `idempotency_key` zorunlu | 429'da 1 kez |
| `demands.documents.update(demand_id, doc_id, body)` | Başlık / zorunluluk bayrakları | Hayır (PATCH) |
| `demands.documents.delete(demand_id, doc_id)` | Belgeyi kaldır (son belge silinemez) | Hayır (DELETE) |
| `demands.documents.reorder(demand_id, document_ids)` | Belge sırası (kimlik kümesi birebir eşleşmeli) | Hayır (PUT) |
| `demands.documents.set_assignments(demand_id, doc_id, party_ids)` | Belgeyi imzalayacak taraflar | Hayır (PUT) |
| `demands.dispatch(demand_id, send_invitations=None)` | Zarfı yayına al | Hayır (POST) |

`upload` aynı `idempotency_key` ile tekrar çağrılırsa yeni belge oluşmaz; sunucunun 409 `IDEMPOTENT_REPLAY` yanıtı normal sonuç olarak (mevcut belge) döner. Özellik hesabınızda açık değilse her belge metodu `ENVELOPE_MULTI_DOC_DISABLED` fırlatır; `list` boş liste döndürmez.

### Şablonlar (templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `templates.list(page=None, limit=None)` | Aktif şablonlar (tek sayfa) | Evet (GET) |
| `templates.list_all(page=None, limit=None)` | Tüm şablonları gezen iterator | Evet (GET) |
| `templates.get(template_id)` | Şablon detayı + taraflar + doldurulabilir alanlar | Evet (GET) |
| `templates.usage(template_id)` | API kullanım kılavuzu (curl + JSON örneği) | Evet (GET) |
| `templates.update(template_id, body)` | Şablon metadata güncelle (`name` / `description` / `category`) | Hayır (PATCH) |
| `templates.delete(template_id)` | Şablonu sil; kayıt 30 gün saklanır, mevcut sözleşmeler etkilenmez. Aktif (taslak veya imza bekleyen) sözleşmesi olan şablon silinemez (`409 TEMPLATE_IN_USE`) | Hayır (DELETE) |

`templates.list()` Alan Şablonlarını listelemez ve `templates.get()` bir Alan Şablonu kimliğine `404` döner; onlar için aşağıdaki `field_templates` kaynağını kullanın.

### Alan Şablonları (field_templates)

| Metod | Açıklama | Retry |
|---|---|---|
| `field_templates.list(page=None, limit=None)` | Alan Şablonları (tek sayfa) | Evet (GET) |
| `field_templates.get(field_template_id)` | Alan Şablonu detayı | Evet (GET) |
| `field_templates.preview_layout(field_template_id, files=[...], on_anchor_miss=None)` | Yerleşimi tam bir PDF üzerinde kuru koşumla dener; sözleşme oluşturmaz, kredi harcamaz | Hayır (POST) |

```python
preview = imzala.field_templates.preview_layout(
    field_template_id,
    files=[FileInput(pdf_bytes, "sozlesme.pdf")],
    on_anchor_miss="drop",  # verilmezse sunucu "block" uygular
)
```

### Kişiler (contacts)

| Metod | Açıklama | Retry |
|---|---|---|
| `contacts.list(q=None, page=None, limit=None, sort=None, company_id=None, archived=None)` | Kişiler (tek sayfa); `limit` 10 ile 100 arası, `sort` biçimi `-createdAt` (`-` öneki azalan) | Evet (GET) |
| `contacts.list_all(...)` | Tüm kişileri gezen iterator | Evet (GET) |
| `contacts.create(body)` | Kişi oluştur (`CONTACT_DUPLICATE`: aynı e-posta veya telefon zaten var) | Hayır (POST) |

### Raporlar (reports)

```python
report = imzala.reports.get()  # sözleşme durumlarının toplu sayımı; parametre almaz
```

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

listing = imzala.timestamps.list(status="COMPLETED", sort="-createdAt")  # limit 10..100
one = imzala.timestamps.get(ts.id)
```

TÜBİTAK KAMU SM TSA ile RFC 3161 zaman damgası (var-olma + değişmezlik kanıtı; imza değildir). `description`, `owner_first_name`, `owner_last_name` opsiyoneldir. Bu uç anahtar başına dakikada 10 istekle sınırlıdır (`RATE_LIMIT_EXCEEDED`).

### Hesap (me)

```python
me = imzala.me()
print(me.email, me.credits)  # id, e-posta, workspace, kalan kredi
```

`me()` hiçbir kapsam (scope) istemez; geçerli her anahtarla çalışır.

## İmzalı PDF ve sertifika (binary)

`get_pdf`, `get_document_pdf` ve `get_certificate` ham baytları `bytes` olarak döndürür (JSON değil). Diske yazın ya da stream'leyin:

```python
pdf = imzala.demands.get_pdf(demand_id)
with open("sozlesme.pdf", "wb") as fh:
    fh.write(pdf)

cert = imzala.demands.get_certificate(demand_id, lang="tr")
with open("sertifika.pdf", "wb") as fh:
    fh.write(cert)
```

## Otomatik yeniden deneme

Üç kural vardır; hepsi SDK'nın içindedir. Taşıma katmanının (urllib3) kendi tekrarı kapalıdır (`retries=0`): önce urllib3 429 ve 503'te `Retry-After`'a uyup isteği, yazmalar dahil, SDK görmeden sessizce tekrarlayabiliyordu.

1. **Okumalar (GET):** `demands.list/get/get_timeline/get_pdf/get_document_pdf/get_certificate`, `templates.*` okumaları, `field_templates.list/get`, `contacts.list/list_all`, `timestamps.list/get`, `reports.get()`, `demands.documents.list` ve `me()` 429 veya 5xx aldığında en çok `max_retries` kez (varsayılan 2) jitter'lı exponential backoff ile yeniden denenir. `0` kapatır.
2. **`Idempotency-Key` ile gönderilen yazmalar:** `demands.create(body, idempotency_key=...)`, `demands.upload_document(idempotency_key=...)`, `timestamps.create(idempotency_key=...)` ve `demands.documents.upload(idempotency_key=...)` bir 429 sonrasında **tam bir kez** yeniden denenir; sunucu aynı anahtar için ikinci kayıt oluşturmaz. İkinci 429, 5xx ve diğer tüm hatalar doğrudan fırlatılır. Anahtar verilmezse tek denemedir. `max_retries` bu kuralı etkilemez.
3. **Diğer yazmalar hiç yeniden denenmez:** `create_bulk`, `contacts.create`, `dispatch`, `send_reminder`, `cancel`, `resend_party`, `delete`, `add_items`, `templates.update/delete`, `embed.create_session` ve zarf belgesi `create/update/delete/reorder/set_assignments`. Tekrarlanan bir `create_bulk` ikinci bir toplu iş, tekrarlanan bir `send_reminder` ikinci bir SMS/e-posta üretir.

```python
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    max_retries=2,          # varsayılan 2 deneme; 0 = kapalı
    retry_base_delay=0.3,   # saniye cinsinden taban gecikme
)
```

Bekleme süresi `Retry-After` başlığından okunur (saniye ya da HTTP tarihi); başlık yoksa backoff gecikmesi uygulanır. **Bekleme tavanı 60 saniyedir:** sunucu daha uzun bir süre isterse SDK beklemek yerine 429'u fırlatır (`ImzalaRateLimitError.retry_after` süreyi taşır). `NaN`, sonsuz ve negatif `Retry-After` değerleri yok sayılır.

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
    #                party.signed / party.viewed / party.rejected / kyc.completed / kyc.failed
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

Tüm hatalar `ImzalaError`'dan türer (ortak alanlar: `status_code`, `body`, `code`, `code_description`). 401/403 → `ImzalaAuthError`, 429 → `ImzalaRateLimitError` (`retry_after` saniye, `rate_limit`), 422 → `ImzalaValidationError`. İstek gönderilmeden yakalanan yerel doğrulama hataları (geçersiz API anahtarı veya `Idempotency-Key` karakteri) de `ImzalaValidationError`'dır; `status_code` `None`'dır.

- **`code`:** sunucunun makine-okunur kodu. `{"error": "metin", "code": "KOD"}` biçimindeki gövdelerde `code` alanından okunur; yalnız metin taşıyan gövdede `code` `None` kalır.
- **`code_description`:** kod için katalogdaki tek satırlık Türkçe açıklama. Katalog `IMZALA_ERROR_CODES` (73 kod) olarak dışa aktarılır; `describe_error_code(code)` ve `is_known_error_code(code)` ile sorgulanır. Katalogda olmayan bir kod açıklamasız fırlatılır, yani yeni bir sunucu sürümü eski SDK'yı kırmaz.
- **`ImzalaRateLimitError.rate_limit`:** standart `RateLimit-Limit` / `RateLimit-Remaining` / `RateLimit-Reset` / `RateLimit-Policy` başlıkları (`ImzalaRateLimitInfo`). Birden çok sınır 429 döndürür ve farklı kod taşır; bkz. aşağıdaki tablo.

```python
from imzala import IMZALA_ERROR_CODES, describe_error_code

describe_error_code("TEMPLATE_IN_USE")  # 'Şablonun aktif (taslak veya imza bekleyen) sözleşmesi olduğu için silinemez.'
len(IMZALA_ERROR_CODES)  # 73
```

## Sık karşılaşılan hatalar

| Durum | Kod | Ne yapmalı |
|---|---|---|
| 400 | `INVALID_PAGE` | `page` 1 veya daha büyük bir tam sayı olmalı |
| 400 | `INVALID_EXPIRY_DATE` | `expiry_date` geçerli bir takvim tarihi olmalı |
| 400 | `PAGE_ID_REQUIRED` | Alan yerleştirmede her öğede tam sayı `page_id` zorunlu |
| 400 | `INVALID_ITEM_TYPE` | Desteklenen alan tiplerinden birini kullanın (hata mesajı listeler) |
| 404 | (kodsuz) | Alan Şablonu kimliğiyle `templates.get()` çağrıldı; Alan Şablonları için `field_templates.get()` kullanın (iki kavram ayrıdır) |
| 409 | `TEMPLATE_IN_USE` | Şablonun taslak veya imza bekleyen sözleşmesi var; tamamlanınca silinebilir |
| 409 | `DEMAND_NOT_DISPATCHABLE` | Sözleşme tamamlanmış veya iptal edilmiş; tekrar gönderilemez |
| 409 | `ENVELOPE_MULTI_DOC_DISABLED` | Çok belgeli zarf bu hesapta açık değil; `demands.documents.*` bu kodu fırlatır |
| 409 | `IDEMPOTENCY_KEY_REUSED` | Aynı anahtar farklı gövdeyle kullanıldı; yeni sözleşme için yeni anahtar üretin |
| 409 | `IDEMPOTENCY_UNVERIFIABLE` | Anahtar daha önce sözleşme üretti ama isteğin aynı olduğu doğrulanamadı; gövdedeki `demand_id` ile durumu sorgulayın, yeni anahtarla körlemesine tekrarlamayın |
| 409 | `DUPLICATE_SUSPECTED` | Anahtarsız istek son 10 dakikada gönderilmiş aynı içerikle eşleşti; kasten tekrarlamak için `force` gönderin |
| 422 | `FIELD_LAYOUT_UNRESOLVED` | Alan yerleşimi belgeye uygulanamadı; sözleşme oluşturulmadı, kredi düşülmedi. `on_anchor_miss` seçin veya belgeyi düzeltin |
| 429 | `RATE_LIMIT_EXCEEDED` | Genel sınır: anahtar başına dakikada 60 istek (zaman damgası ucunda 10). `Retry-After` kadar bekleyin |
| 429 | `TOO_MANY_REQUESTS` | Uca özgü sınır: belge yüklemede dakikada 30, davet tekrarı ve zarf gönderiminde saatte 300 |
| 429 | `RECIPIENT_RESEND_LIMIT` | Aynı alıcıya saatte en çok 3, günde en çok 10 davet tekrarı |

Sözleşme, şablon ve Alan Şablonu listelerinde `limit` üst sınırı 100'dür; daha büyük bir değer hata vermez, sessizce 100'e kırpılır. Kişi ve zaman damgası listelerinde `limit` 10 ile 100 arasında olmalıdır.

## Sürüm uyumu

| imzala (PyPI) | Konuştuğu API | Durum |
|---|---|---|
| 1.0.0 | v1 (`1.8.13`) | Güncel |
| 0.x | v1 (`1.7.x`) | Bakım dışı; 1.0.0'a yükseltin |

İmzala dış API'si **v1**'dir ve geriye dönük uyumludur: yeni alanlar opsiyonel, yeni davranışlar
opt-in, varsayılan davranış eskisidir. Kırıcı bir değişiklik gerekirse yeni bir major API sürümü
yayımlanır; eski sürüm duyurudan sonra **12 ay** çalışmaya devam eder.

## Sunucu-taraflı

⚠️ Bu paket **yalnızca sunucuda** kullanılır. API anahtarınızı tarayıcı JavaScript'ine veya mobil uygulamaya asla gömmeyin: sızarsa hesabınızdaki tüm sözleşme/şablon/zaman damgası işlemlerine erişilir. Tarayıcıda imza almak için [`@imzala/embed`](../embed) veya [`@imzala/embed-react`](../embed-react) kullanın.

## İmza sınıfı

İmzala **dijital imza (SES)** üretir; her imza zaman damgalıdır. Nitelikli/güvenli elektronik imza (QES) DEĞİLDİR. Gömülü imza da dijital imza (SES/AES) üretir.

## Daha fazla

- Tam API referansı: [api-docs.imzala.org](https://api-docs.imzala.org)
- Kullanım kılavuzu: [imzala.org/docs/api-sozlesme-yasam-dongusu](https://imzala.org/docs/api-sozlesme-yasam-dongusu)
- Çalışan örnekler: [`examples/python`](../../examples/python) (altı senaryo)
- Değişiklik günlüğü: [CHANGELOG.md](./CHANGELOG.md)
- [Monorepo README](../../README.md)
