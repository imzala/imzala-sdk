"""Adres defterini sayfalı gezme, kişi oluşturma ve sözleşme durum sayımlarını okuma.
Kapsam: contacts:read + contacts:write (kişi oluşturma) + demands:read (rapor).
Kredi harcamaz. Kişi oluşturma veri değiştirir (yorumda)."""

import os

from imzala import Imzala, ImzalaError

base_url = os.environ.get("IMZALA_BASE_URL")
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    **({"base_url": base_url} if base_url else {}),
)

# 1) Sayfalı gezinme: list_all sayfaları şeffaf biçimde dolaşır (limit 10..100).
sayac = 0
for kisi in imzala.contacts.list_all(limit=50):
    sayac += 1
    if sayac <= 5:
        print(f"{kisi.id} · {kisi.first_name} {kisi.last_name}")
print(f"Toplam kişi: {sayac}")

# 2) Arama: tek sayfa, metin filtresiyle.
arama = imzala.contacts.list(q="ayse", limit=10)
print(f'"ayse" için {arama.total or 0} sonuç')

# 3) Kişi oluştur. Aynı e-posta veya telefona sahip aktif bir kişi varsa 409
#    CONTACT_DUPLICATE döner. Bu uçta idempotency anahtarı yoktur; SDK isteği
#    hiçbir koşulda yinelemez. Kişisel veriyi buraya yazan, o veri için veri
#    sorumlusudur (aydınlatma yükümlülüğü size aittir).
#
# try:
#     yeni = imzala.contacts.create(
#         {
#             "first_name": "Ayşe",
#             "last_name": "Yılmaz",
#             "email": "ayse@example.com",
#             "phone": "+905551112233",
#         }
#     )
#     print(f"Kişi oluşturuldu: {yeni.id}")
# except ImzalaError as err:
#     if err.code == "CONTACT_DUPLICATE":
#         print(f"Kişi zaten var: {err.code_description}")
#     else:
#         raise

# 4) Rapor: sözleşme durumlarının toplu sayımı. Kişisel veri içermez.
rapor = imzala.reports.get()
c = rapor.contracts
print(f"Sözleşmeler: toplam {c.total} · bekleyen {c.pending} · tamamlanan {c.completed}")
print(f"  iptal {c.cancelled} · süresi dolan {c.expired} · bu ay oluşturulan {c.this_month}")
