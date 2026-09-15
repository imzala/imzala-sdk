"""Kendi PDF'inizi Alan Şablonuyla kuru koşumda deneme, sonra sözleşme olarak yükleme.
Kapsam: templates:read + demands:write (kuru koşum da bu kapsamı ister).
Kuru koşum kredi harcamaz ve hiçbir şey oluşturmaz; yükleme kredi harcar (yorumda)."""

import os
import sys
import uuid
from pathlib import Path

from imzala import FileInput, Imzala

base_url = os.environ.get("IMZALA_BASE_URL")
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    **({"base_url": base_url} if base_url else {}),
)

# 1) Alan Şablonunu seç. Alan Şablonu, sözleşme şablonundan ayrıdır: belgedeki
#    çapa metinlerine göre alanların nereye düşeceğini tarif eder.
field_template_id = os.environ.get("IMZALA_FIELD_TEMPLATE_ID")
if not field_template_id:
    listing = imzala.field_templates.list(limit=20)
    field_template_id = listing.field_templates[0].id if listing.field_templates else None
if not field_template_id:
    print("Tanımlı Alan Şablonu yok. Panelden oluşturabilirsiniz.")
    sys.exit(0)

# 2) Rolleri ve alan sayılarını oku. Yüklemede her rol için template_party_id
#    bu listeden verilir.
detail = imzala.field_templates.get(str(field_template_id))
print(f"Alan Şablonu: {detail.name} · toplam alan: {detail.total_field_count}")
for party in detail.parties or []:
    print(f"  Rol {party.order}: {party.label or '(adsız)'} · {party.field_count} alan · {party.id}")

# 3) KURU KOŞUM: yerleşimi belge üzerinde dene. Hiçbir şey oluşturmaz, kredi
#    harcamaz, dosyayı saklamaz. Çözülemeyen çapaları önceden görmenin en ucuz
#    yolu. Çözülemeyen belge de 200 döner: resolvable=False.
pdf_path = Path(os.environ.get("IMZALA_PDF_PATH") or Path(__file__).with_name("ornek-sozlesme.pdf"))
if not pdf_path.exists():
    print(f"PDF bulunamadı: {pdf_path}. IMZALA_PDF_PATH ile bir PDF verin.")
    sys.exit(0)
pdf = FileInput(content=pdf_path.read_bytes(), filename="ornek-sozlesme.pdf", content_type="application/pdf")

preview = imzala.field_templates.preview_layout(
    str(detail.id),
    files=[pdf],
    # on_anchor_miss="drop" yalnız şablonun ilgili alanları zaten drop ise uygulanır;
    # verilmezse block geçerlidir. İmza alanları hiçbir koşulda düşürülmez.
)
total = preview.placements_summary.total if preview.placements_summary else 0
print(f"Uygulanabilir: {'evet' if preview.resolvable else 'hayır'} · sayfa: {preview.page_count} · yerleşen alan: {total}")
for diagnostic in preview.diagnostics or []:
    print(f"  {diagnostic.severity} {diagnostic.code}: {diagnostic.message}")
for warning in preview.warnings or []:
    print(f"  Uyarı {warning.code}: {warning.message}")
if not preview.resolvable:
    print("Yerleşim çözülemedi. Belgedeki çapaları düzeltip yeniden deneyin; yükleme 422 ile reddedilir.")

# 4) Gerçek yükleme. Kredi harcar (yorumda). field_template_id verildiğinde alanlar
#    bu şablona göre yerleştirilir; her rol tam bir kez eşlenmelidir. Bu uçta
#    davetler varsayılan olarak KAPALIDIR; send_invitations="true" ile aynı
#    istekte gönderilir. idempotency_key ile tekrar deneme ikinci sözleşme yaratmaz.
#
# kisiler = [
#     {"first_name": "Ayşe", "last_name": "Yılmaz", "email": "ayse@example.com", "phone": "+905551112233"},
#     {"first_name": "Mehmet", "last_name": "Yılmaz", "email": "mehmet@example.com", "phone": "+905551112244"},
# ]
# demand = imzala.demands.upload_document(
#     files=[pdf],
#     title="Hizmet Sözleşmesi",
#     parties=[
#         {"template_party_id": str(party.id), **kisiler[i % len(kisiler)]}
#         for i, party in enumerate(detail.parties or [])
#     ],
#     field_template_id=str(detail.id),
#     idempotency_key=str(uuid.uuid4()),
#     send_invitations="true",
# )
# print(f"Sözleşme: {demand.id} · durum: {demand.status}")
# for party in demand.signing_urls or []:
#     print(f"  {party.first_name} {party.last_name} → {party.signing_url}")
