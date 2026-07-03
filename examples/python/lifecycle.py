"""İmzala Python SDK, sözleşme yaşam döngüsü örneği (çalışır).

Çalıştırma:
    IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
        python examples/python/lifecycle.py

Salt-okuma işlemleri doğrudan çalışır. Veri değiştiren işlemler (create /
cancel / resend_party / delete) yorum satırıdır: gerçek kredi harcar / durum
değiştirir. Açmadan önce ne yaptığını okuyun.

İmzala sözleşmeleri varsayılan olarak dijital imza (SES) üretir; SDK imza
geçerliliği hakkında hukuki iddiada bulunmaz.
"""

from __future__ import annotations

import json
import os
import sys
from collections.abc import Mapping
from typing import Any

from imzala import Imzala, ImzalaError, verify_webhook


def field(obj: Any, name: str, default: Any = None) -> Any:
    """Yanıt gerçek runtime'da pydantic modeli, testte düz dict olabilir;
    ikisinden de aynı şekilde alan oku (SDK'nın kendi `_get_field` kalıbı)."""
    if isinstance(obj, Mapping):
        return obj.get(name, default)
    return getattr(obj, name, default)


def main() -> None:
    api_key = os.environ.get("IMZALA_API_KEY")
    if not api_key:
        print(
            "IMZALA_API_KEY gerekli (imz_...). Panel, Geliştirici, API Anahtarları.",
            file=sys.stderr,
        )
        sys.exit(1)

    # base_url'in gerçek bir varsayılanı var (api-prd.imzala.org). Env boşsa
    # anahtarı hiç geçme ki SDK varsayılanı kullansın; None geçmek host'u bozar.
    kwargs: dict[str, Any] = {"api_key": api_key}
    base_url = os.environ.get("IMZALA_BASE_URL")
    if base_url:
        kwargs["base_url"] = base_url  # test ortamı: https://test-api.imzala.org

    imzala = Imzala(**kwargs)

    # 1) Kimlik + kredi bakiyesi
    me = imzala.me()
    credits = field(me, "credits")
    if isinstance(credits, (Mapping, list)):
        credits_str = json.dumps(credits, ensure_ascii=False)
    else:
        credits_str = str(credits) if credits is not None else "?"
    print(f"\n👤 {field(me, 'email') or field(me, 'id')}, kredi: {credits_str}")

    # 2) Şablonlar (bir sayfa)
    templates = imzala.templates.list(limit=5)
    template_rows = field(templates, "templates") or []
    print(f"\n📄 Şablon sayısı: {field(templates, 'total') or len(template_rows)}")
    for t in template_rows:
        print(f"   - {field(t, 'id')}  {field(t, 'name')}")

    # 3) Sözleşme listesi (counts-only, taraf PII'si yok)
    listing = imzala.demands.list(limit=5, sort="createdAt:desc")
    demand_rows = field(listing, "demands") or []
    print(f"\n📋 Sözleşme sayısı: {field(listing, 'total') or 0}")
    for d in demand_rows:
        signed = field(d, "parties_signed")
        total = field(d, "parties_total")
        print(
            f"   - {field(d, 'id')}  [{field(d, 'status')}]  "
            f"{signed}/{total} imzalı  {field(d, 'title') or ''}"
        )

    if not demand_rows:
        print("\n(Sözleşme yok, create örneğini açın.)")
        return

    first = demand_rows[0]
    first_id = field(first, "id")

    # 4) Sözleşme detay (imzacı adı kısaltılmış, e-posta maskeli, KVKK)
    demand = imzala.demands.get(first_id)
    print(f"\n🔎 {field(demand, 'id')} taraflar:")
    for p in field(demand, "parties") or []:
        durum = "✅ imzaladı" if field(p, "signed") else "⏳ bekliyor"
        print(f"   - {field(p, 'name')}  {field(p, 'email_masked')}  {durum}")

    # 5) İmza denetim izi (maskeli, ip_masked, ham IP yok)
    timeline = imzala.demands.get_timeline(first_id)
    events = field(timeline, "events") or []
    print(f"\n🕒 Denetim izi: {len(events)} olay")
    for e in events[:5]:
        print(
            f"   - {field(e, 'created_at')}  {field(e, 'event_type')}  "
            f"{field(e, 'actor_label') or ''}  {field(e, 'ip_masked') or ''}"
        )

    # 6) Tamamlanmış sözleşmenin imzalı PDF'ini indir (binary, bytes)
    if field(first, "status") == "COMPLETED":
        pdf = imzala.demands.get_pdf(first_id)  # -> bytes
        out = f"demand-{first_id}.pdf"
        with open(out, "wb") as fh:
            fh.write(pdf)
        print(f"\n💾 İmzalı PDF kaydedildi: {out} ({len(pdf)} bayt)")

        # Tamamlanma sertifikası (PAdES B-T):
        # cert = imzala.demands.get_certificate(first_id, lang="tr")  # -> bytes
        # with open(f"demand-{first_id}-sertifika.pdf", "wb") as fh:
        #     fh.write(cert)

    # -- Veri değiştiren işlemler (bilerek yorumlu) --------------------------
    #
    # # Şablondan yeni sözleşme oluştur (1 kredi harcar):
    # created = imzala.demands.create({
    #     "template_id": "<template-id>",
    #     "party_mapping": [{
    #         "template_party_id": "<template-party-id>",
    #         "first_name": "Ada", "last_name": "Kalkan",
    #         "email": "ada@example.com", "phone": "+905304636743",
    #     }],
    #     "variables": {"adres": "Çankaya/Ankara", "tutar": "5.000 TL"},
    # })
    # print("İmza URL:", field(field(created, "signing_urls")[0], "signing_url"))
    #
    # # Bekleyen sözleşmeyi iptal et:
    # imzala.demands.cancel(first_id, {"reason": "Vazgeçildi"})
    #
    # # Tekil tarafa daveti tekrar gönder:
    # imzala.demands.resend_party(first_id, "<party-id>")
    #
    # # Tamamlanmamış sözleşmeyi sil:
    # imzala.demands.delete(first_id)
    #
    # # Şablon metadata güncelle / sil:
    # imzala.templates.update("<template-id>", {"name": "Yeni Ad"})
    # imzala.templates.delete("<template-id>")


# -- Webhook imza doğrulama (ayrı bir HTTP handler'da) -----------------------
#
# Flask örneği (ham gövde şart, JSON'u parse edip yeniden serialize etmeyin):
#   from flask import Flask, request, abort
#   app = Flask(__name__)
#
#   @app.post("/webhooks/imzala")
#   def imzala_webhook():
#       ok = verify_webhook(
#           os.environ["IMZALA_WEBHOOK_SECRET"],   # whsec_...
#           request.get_data(),                    # ham gövde (bytes)
#           request.headers.get("X-Imzala-Signature-256"),  # "sha256=<hex>"
#       )
#       if not ok:
#           abort(401)
#       event = json.loads(request.get_data())
#       # event["type"]: demand.created / demand.completed / ...
#       return "", 200


if __name__ == "__main__":
    try:
        main()
    except ImzalaError as err:
        print(f"\n❌ ImzalaError [{err.status_code}]: {err}", file=sys.stderr)
        sys.exit(1)
