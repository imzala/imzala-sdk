"""Çok belgeli zarf: KVKK aydınlatma + açık rıza + ön bilgilendirme ile sözleşme toplama akışı.
Kapsam: demands:write (taslak, belgeler, gönderim) + demands:read (liste, PDF).
Belge uçları kredi harcamaz; tahsilat oluşturma ve gönderim adımlarında yapılır. Yazma adımları yorumda."""

import os
import sys
import uuid
from pathlib import Path

from imzala import FileInput, Imzala, ImzalaError

base_url = os.environ.get("IMZALA_BASE_URL")
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    **({"base_url": base_url} if base_url else {}),
)

# 1) Davet göndermeden taslak sözleşme oluştur. dispatch_notifications: False ile
#    davetler oluşturma anında ÇIKMAZ; her şey hazır olunca 6. adımdaki dispatch
#    ile tek seferde gönderilir. Bu alan dispatch ucunun belgelerinde anlatılır;
#    gövde sözlük olarak olduğu gibi gönderilir. Kişiler kurgusaldır.
#
# taslak = imzala.demands.create(
#     {
#         "template_id": os.environ["IMZALA_TEMPLATE_ID"],
#         "title": "Üyelik sözleşmesi ve KVKK belgeleri",
#         "dispatch_notifications": False,
#         "party_mapping": [{"template_party_id": "<şablon detayından rol id>", "first_name": "Ayşe", "last_name": "Yılmaz", "email": "ayse@example.com", "phone": "+905551112233"}],
#     },
#     idempotency_key=str(uuid.uuid4()),
# )
# zarf_id = str(taslak.id)
# party_id = str(taslak.signing_urls[0].party_id)

# 2) Ana sözleşmeyi dosya olarak yükle (doc_kind CONTRACT). Belge başına TEK dosya,
#    her belge için ayrı çağrı. idempotency_key ZORUNLUDUR ve gövde alanı olarak
#    gider: aynı anahtarla tekrar yükleme yeni belge yaratmaz, önceki belgeyi döndürür.
#
# sozlesme = imzala.demands.documents.upload(
#     zarf_id,
#     file=FileInput(content=Path("sozlesme.pdf").read_bytes(), filename="sozlesme.pdf", content_type="application/pdf"),
#     title="Üyelik Sözleşmesi",
#     doc_kind="CONTRACT",
#     idempotency_key=str(uuid.uuid4()),
# )
# print(f"Belge: {sozlesme.document.id} · {sozlesme.document.title}")

# 3) KVKK belgelerini ekle (dosyasız, yalnız metadata).
#    KVKK_CONSENT için is_required FALSE olmak zorundadır: açık rıza reddedilebilir
#    olmalıdır (aksi halde 400 CONSENT_CANNOT_BE_REQUIRED).
#    PREINFO için is_required TRUE olmak zorundadır (aksi halde 400 PREINFO_MUST_BE_REQUIRED).
#
# aydinlatma = imzala.demands.documents.create(
#     zarf_id, {"title": "KVKK Aydınlatma Metni", "doc_kind": "KVKK_NOTICE", "is_required": True, "signature_required": False}
# )
# riza = imzala.demands.documents.create(
#     zarf_id, {"title": "KVKK Açık Rıza Beyanı", "doc_kind": "KVKK_CONSENT", "is_required": False}
# )
# on_bilgi = imzala.demands.documents.create(
#     zarf_id, {"title": "Ön Bilgilendirme Formu", "doc_kind": "PREINFO", "is_required": True}
# )

# 4) Hangi belgeyi kim görecek: belge başına taraf kümesi (küme olduğu gibi değişir).
#
# for belge in (sozlesme, aydinlatma, riza, on_bilgi):
#     imzala.demands.documents.set_assignments(zarf_id, str(belge.document.id), [party_id])

# 5) Sıralama: aydınlatma ve ön bilgilendirme önce, sözleşme sonra, rıza en sonda.
#    Liste zarfın TÜM belgelerini içermelidir (ORDER_SET_MISMATCH).
#
# imzala.demands.documents.reorder(
#     zarf_id, [str(aydinlatma.document.id), str(on_bilgi.document.id), str(sozlesme.document.id), str(riza.document.id)]
# )

# 6) Gönder. Davetler burada çıkar; kredi mutabakatı burada yapılır (aynı sözleşme
#    için ikinci kez düşmez). Bu uçta idempotency anahtarı yoktur; SDK hiçbir
#    koşulda yinelemez. Zaten gönderilmiş bir sözleşmeye tekrar çağrı kredi
#    düşürmez (dispatched: False) ama davetleri yeniden yollar.
#
# gonderim = imzala.demands.dispatch(zarf_id, send_invitations=True)
# print(f"Gönderildi: {gonderim.dispatched} · durum: {gonderim.status} · düşen kredi: {gonderim.credits.charged if gonderim.credits else '?'}")

# 7) Belgeleri listele (salt okuma). Çok belgeli zarf hesabınız için henüz açık
#    değilse tüm belge uçları 409 ENVELOPE_MULTI_DOC_DISABLED döner.
demand_id = os.environ.get("IMZALA_DEMAND_ID")
if not demand_id:
    demands = imzala.demands.list(limit=1, sort="createdAt:desc")
    demand_id = demands.demands[0].id if demands.demands else None
if not demand_id:
    print("Sözleşme yok. IMZALA_DEMAND_ID verin ya da önce bir sözleşme oluşturun.")
    sys.exit(0)
demand_id = str(demand_id)

try:
    belgeler = imzala.demands.documents.list(demand_id, view="wizard")
except ImzalaError as err:
    if err.code == "ENVELOPE_MULTI_DOC_DISABLED":
        print(f"Çok belgeli zarf bu hesap için kapalı: {err.code_description}")
        sys.exit(0)
    raise
print(f"Sözleşme {demand_id}: {len(belgeler.documents or [])} belge")
for belge in belgeler.documents or []:
    taraf = len(belge.assigned_party_ids or [])
    print(f"  {belge.order}. {belge.title} · {belge.doc_kind} · zorunlu: {belge.is_required} · imza: {belge.signature_required} · taraf: {taraf}")

# 8) Tamamlanınca belge-özgü imzalı PDF (tüm zarfın birleşik PDF'i için get_pdf).
demand = imzala.demands.get(demand_id)
ilk_belge = belgeler.documents[0] if belgeler.documents else None
if demand.status == "COMPLETED" and ilk_belge:
    pdf = imzala.demands.get_document_pdf(demand_id, str(ilk_belge.id))
    dosya = Path(f"belge-{ilk_belge.id}.pdf")
    dosya.write_bytes(pdf)
    print(f"İmzalı belge PDF'i kaydedildi: {dosya} ({len(pdf)} bayt)")
else:
    print(f"Sözleşme durumu {demand.status}; belge PDF'i yalnız COMPLETED durumunda indirilir.")
