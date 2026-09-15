"""Toplu sözleşme oluşturma (10'arlı parçalar), Idempotency-Key ve istek limiti (429) davranışı.
Kapsam: templates:read + demands:write. Oluşturma adımları kredi harcar (yorumda).
Limit gösterimi yalnız salt-okuma isteği (şablon listesi) kullanır, kredi harcamaz."""

import os
import sys
import uuid
from concurrent.futures import ThreadPoolExecutor

from imzala import Imzala, ImzalaRateLimitError, describe_error_code

base_url = os.environ.get("IMZALA_BASE_URL")
imzala = Imzala(
    api_key=os.environ["IMZALA_API_KEY"],
    **({"base_url": base_url} if base_url else {}),
    # GET isteklerinin 429 sonrası otomatik yinelenmesi kapatıldı: 4. adımda
    # 429'u kendimiz görmek istiyoruz. Üretimde varsayılanı (2) bırakın.
    max_retries=0,
)

# 1) Şablon: IMZALA_TEMPLATE_ID verilmişse o, yoksa listedeki ilk şablon.
template_id = os.environ.get("IMZALA_TEMPLATE_ID")
if not template_id:
    templates = imzala.templates.list(limit=1)
    template_id = templates.templates[0].id if templates.templates else None
if not template_id:
    print("Aktif şablon yok. Panelden bir şablon oluşturun.")
    sys.exit(0)
detail = imzala.templates.get(str(template_id))
rol_id = str(detail.parties[0].id) if detail.parties else None
print(f"Şablon: {detail.name} · ilk rol: {rol_id}")

# 2) Tek sözleşmede tekrar koruması. create bir Idempotency-Key kabul eder:
#    aynı anahtar + aynı gövde → aynı sözleşme döner, ikinci kayıt oluşmaz.
#    Aynı anahtar + farklı gövde → 409 IDEMPOTENCY_KEY_REUSED.
#    Anahtarlı bir istek 429 alırsa SDK Retry-After kadar bekleyip bir kez yineler;
#    anahtarsız istek hiç yinelenmez (yineleme ikinci sözleşme yaratırdı).
#
# anahtar = str(uuid.uuid4())  # ör. kendi sipariş numaranız da olabilir
# demand = imzala.demands.create(
#     {
#         "template_id": str(detail.id),
#         "party_mapping": [{"template_party_id": rol_id, "first_name": "Ayşe", "last_name": "Yılmaz", "email": "ayse@example.com"}],
#     },
#     idempotency_key=anahtar,
# )
# print(f"Sözleşme: {demand.id}")

# 3) Toplu oluşturma: tek istekte en fazla 10 satır (BULK_MAX_10). Daha uzun
#    listeyi 10'arlı parçalara bölün. Her satır bağımsız bir sözleşmedir; kısmi
#    başarı 200 ile döner, satır durumunu results[i].status'tan okuyun.
#    Bu uçta Idempotency-Key YOKTUR; SDK toplu isteği 429 dahil hiçbir koşulda
#    yinelemez, çünkü sunucu anahtarı tanımadığından yineleme aynı sözleşmeleri
#    ikinci kez yaratır ve krediyi iki kez düşerdi. Parça başarısız olursa
#    demands.list ile durumu doğrulayıp yalnız eksik satırları yeniden gönderin.
#
# alicilar = [
#     {"template_party_id": rol_id, "first_name": "Ayşe", "last_name": "Yılmaz", "email": "ayse@example.com"},
#     {"template_party_id": rol_id, "first_name": "Mehmet", "last_name": "Yılmaz", "email": "mehmet@example.com"},
# ]
# for i in range(0, len(alicilar), 10):
#     parca = alicilar[i : i + 10]
#     sonuc = imzala.demands.create_bulk(
#         {
#             "template_id": str(detail.id),
#             "options": {"dispatch_notifications": False},  # önce taslak oluştur, gözden geçir, sonra gönder
#             "rows": [{"party_mapping": [alici]} for alici in parca],
#         }
#     )
#     print(f"Parça {i // 10 + 1}: oluşan {sonuc.created}, başarısız {sonuc.failed}")
#     for satir in sonuc.results or []:
#         print(f"  satır {satir.row_index}: {satir.status} {satir.demand_id or satir.error or ''}")

# 4) İstek limiti. Anahtar başına dakikada 60 istek (anahtar bazında düşürülebilir).
#    Aşımda 429 + RATE_LIMIT_EXCEEDED döner.
#
#    NOT: limiti ardışık isteklerle göremezsiniz: her istek yaklaşık bir saniye
#    sürdüğü için 65 ardışık çağrının hepsi 200 döner (60 saniyelik pencere
#    aradan sıfırlanır). Limit ancak eşzamanlı bir patlamayla görünür.
try:
    with ThreadPoolExecutor(max_workers=70) as havuz:
        for sonuc in havuz.map(lambda _: imzala.templates.list(limit=1), range(70)):
            pass
    print("Limit tetiklenmedi.")
except ImzalaRateLimitError as err:
    print(f"Limit aşıldı: {describe_error_code(err.code) or err.code}")
    print(f"  {err.retry_after} saniye sonra tekrar deneyin.")
    info = err.rate_limit
    if info:
        print(f"  Politika: {info.policy} · limit: {info.limit} · kalan: {info.remaining}")
