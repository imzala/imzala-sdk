"""Şablondan sözleşme oluşturma, davet linklerini alma ve webhook imzasını doğrulama.
Kapsam: templates:read + demands:read; sözleşme oluşturma için demands:write.
Sözleşme oluşturma kredi harcar (yorumda). Diğer adımlar kredi harcamaz."""

import os
import sys
import uuid

from imzala import Imzala, verify_webhook

base_url = os.environ.get("IMZALA_BASE_URL")
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    **({"base_url": base_url} if base_url else {}),  # boşsa SDK varsayılanı (api-prd.imzala.org)
)

# 1) Hesap ve kalan kredi. Bu uç kapsam istemez.
me = imzala.me()
print(f"Hesap: {me.email} · kalan kredi: {me.credits.remaining if me.credits else '?'}")

# 2) Şablonu seç: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
template_id = os.environ.get("IMZALA_TEMPLATE_ID")
if not template_id:
    templates = imzala.templates.list(limit=10)
    template_id = templates.templates[0].id if templates.templates else None
if not template_id:
    print("Aktif şablon yok. Panelden bir şablon oluşturun.")
    sys.exit(0)

# 3) Şablonun rollerini ve değişkenlerini oku. party_mapping bu rollere göre
#    kurulur: her rol için template_party_id zorunludur.
detail = imzala.templates.get(str(template_id))
print(f"Şablon: {detail.name} · {detail.pages_count} sayfa")
for party in detail.parties or []:
    optional = "" if party.is_required else " · isteğe bağlı"
    print(f"  Rol {party.order}: {party.label or '(adsız)'} · {party.id}{optional}")
for variable in detail.variables or []:
    print(f"  Değişken: {variable.slug} ({variable.item_type})")

# 4) Sözleşmeyi oluştur. Kredi harcar; açmadan önce test ortamını kullanın.
#    idempotency_key: ağ koptuğunda aynı anahtarla tekrar denemek ikinci bir
#    sözleşme yaratmaz; SDK bir 429 sonrasında anahtarlı isteği bir kez yineler.
#    Kişiler kurgusaldır; her rol için template_party_id şablon detayından gelir.
#
# kisiler = [
#     {"first_name": "Ayşe", "last_name": "Yılmaz", "email": "ayse@example.com", "phone": "+905551112233"},
#     {"first_name": "Mehmet", "last_name": "Yılmaz", "email": "mehmet@example.com", "phone": "+905551112244"},
# ]
# demand = imzala.demands.create(
#     {
#         "template_id": str(detail.id),
#         "title": "Hizmet Sözleşmesi",
#         "expiry_date": "2026-12-31",
#         "party_mapping": [
#             {"template_party_id": str(party.id), **kisiler[i % len(kisiler)]}
#             for i, party in enumerate(detail.parties or [])
#         ],
#     },
#     idempotency_key=str(uuid.uuid4()),
# )
# print(f"Sözleşme: {demand.id} · durum: {demand.status}")
# for party in demand.signing_urls or []:
#     print(f"  {party.first_name} {party.last_name} → {party.signing_url}")

# 5) Son sözleşmelerin durumu (yalnız sayımlar, kişisel veri içermez).
demands = imzala.demands.list(limit=5, sort="createdAt:desc")
for d in demands.demands or []:
    print(f"{d.id} · {d.status} · {d.parties_signed}/{d.parties_total} imza")

# 6) Webhook imzası. Gövde HAM olarak okunmalı: JSON'u ayrıştırıp yeniden
#    serileştirmek baytları değiştirir ve imza tutmaz (Flask'ta request.get_data(),
#    Django'da request.body ham gövdeyi verir).
secret = os.environ.get("IMZALA_WEBHOOK_SECRET")
signature = os.environ.get("IMZALA_WEBHOOK_SIGNATURE")
if secret and signature:
    raw_body = b'{"event":"demand.completed","demand_id":"3f2a1c88-4a1e-4c1b-9a3a-9d2f7c1e5b40"}'
    print("Webhook imzası geçerli mi:", verify_webhook(secret, raw_body, signature))
else:
    print("Webhook doğrulaması atlandı: IMZALA_WEBHOOK_SECRET ve IMZALA_WEBHOOK_SIGNATURE tanımlı değil.")
