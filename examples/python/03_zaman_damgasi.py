"""Bir dosyanın belirli bir anda var olduğunu ve o andan beri değişmediğini kanıtlayan
zaman damgası hizmeti: damga al, damgaları listele, tek kaydı getir.
Kapsam: timestamps. Damga almak kredi harcar (yorumda); liste ve getirme harcamaz.

Damga dijital imza DEĞİLDİR: dosyayı kimin yazdığını ya da kimin sahiplendiğini
kanıtlamaz; yalnız var olma ve değişmezlik kanıtıdır. owner alanları bilgilendirme
amaçlıdır ve API tarafından doğrulanmaz."""

import os
import sys
import uuid
from pathlib import Path

from imzala import Imzala

base_url = os.environ.get("IMZALA_BASE_URL")
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    **({"base_url": base_url} if base_url else {}),
)

# 1) Damga al. Kredi harcar (yorumda). idempotency_key, ağ koptuğunda ikinci kez
#    kredi harcamayı önler: beş dakika içinde aynı anahtar aynı kaydı döndürür.
pdf_path = Path(os.environ.get("IMZALA_PDF_PATH") or Path(__file__).with_name("ornek-sozlesme.pdf"))
if pdf_path.exists():
    print(f"Damgalanacak dosya hazır: {pdf_path} (damga alma adımı yorumda).")
#
# kayit = imzala.timestamps.create(
#     content=pdf_path.read_bytes(),
#     filename="ornek-sozlesme.pdf",
#     content_type="application/pdf",
#     idempotency_key=str(uuid.uuid4()),
#     description="Taslak sürüm",
#     owner_first_name="Ayşe",
#     owner_last_name="Yılmaz",
# )
# print(f"Damga: {kayit.id} · {kayit.timestamp_time} · {kayit.tsa_authority}")
# print(f"  SHA-256: {kayit.file_sha256} · harcanan kredi: {kayit.credits_used}")

# 2) Damgaları listele (varsayılan sıralama yeni → eski).
liste = imzala.timestamps.list(limit=10)
print(f"Toplam damga: {liste.total or 0}")
for kayit in liste.timestamps or []:
    print(f"{kayit.id} · {kayit.original_file_name} · {kayit.timestamp_date} · {kayit.status}")

# 3) Tek kaydın ayrıntısı: IMZALA_TIMESTAMP_ID verilmişse o, yoksa listedeki ilk kayıt.
timestamp_id = os.environ.get("IMZALA_TIMESTAMP_ID") or (liste.timestamps[0].id if liste.timestamps else None)
if not timestamp_id:
    print("Henüz damga kaydı yok.")
    sys.exit(0)
detay = imzala.timestamps.get(str(timestamp_id))
print(f"Kayıt {detay.id}: {detay.original_file_name} ({detay.original_file_size} bayt)")
print(f"  Açıklama: {detay.description or '-'} · durum: {detay.status}")
print(f"  Damga dosyası: {detay.timestamp_file_url}")
