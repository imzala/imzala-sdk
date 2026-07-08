"""Uçtan uca (e2e) testler, GERÇEK İmzala API'sine karşı çalışır.

Varsayılan olarak ATLANIR. Çalıştırmak için ortam değişkenleri:
    IMZALA_E2E=1
    IMZALA_API_KEY=imz_...
    IMZALA_BASE_URL=https://test-api.imzala.org   (opsiyonel; varsayılan prod)

    IMZALA_E2E=1 IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
        python -m pytest tests/test_e2e.py

Yalnızca SALT-OKUMA uçları çağrılır (kredi harcamaz, veri değiştirmez):
me / templates.list / demands.list / demands.get / get_timeline, artı bir
geçersiz-id çağrısının tipli `ImzalaError` fırlattığının doğrulaması. Böylece
herhangi bir gerçek hesaba karşı güvenle koşturulabilir. Env değişkenleri yoksa
tüm test'ler pytest tarafından ATLANIR (skip), hata vermez.
"""

from __future__ import annotations

import json
import os
import re
from collections.abc import Mapping
from typing import Any

import pytest

from imzala import Imzala, ImzalaError

ENABLED = os.environ.get("IMZALA_E2E") == "1" and bool(os.environ.get("IMZALA_API_KEY"))

# Env yoksa modüldeki her test skip olur (client kurmayı hiç denemeyiz;
# apiKey'siz Imzala(...) ValueError fırlatır).
pytestmark = pytest.mark.skipif(
    not ENABLED,
    reason="e2e devre dışı: IMZALA_E2E=1 + IMZALA_API_KEY gerekir",
)

# Ham (maskesiz) e-posta yakalayan basit desen. Maskeli e-postalarda yerel
# kısım yıldızlı olur (ör. "ah***@e***.com"), dolayısıyla bu desen eşleşmez.
RAW_EMAIL_RE = re.compile(r"[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}")


def _field(obj: Any, name: str, default: Any = None) -> Any:
    """Yanıt gerçek runtime'da pydantic modeli, testte düz dict olabilir;
    ikisinden de aynı şekilde alan oku."""
    if isinstance(obj, Mapping):
        return obj.get(name, default)
    return getattr(obj, name, default)


def _to_serializable(obj: Any) -> Any:
    """Pydantic modelini (ya da dict/list'i) JSON'lanabilir düz yapıya çevir."""
    to_dict = getattr(obj, "to_dict", None)
    if callable(to_dict):
        return to_dict()
    return obj


def _dump(obj: Any) -> str:
    """PII taraması için tüm yanıtı JSON string'e çevir (UUID/tarih -> str)."""
    return json.dumps(_to_serializable(obj), default=str, ensure_ascii=False)


@pytest.fixture(scope="module")
def imzala() -> Imzala:
    # Sadece ENABLED iken (skip edilmemiş test'ler için) kurulur.
    kwargs: dict[str, Any] = {"api_key": os.environ["IMZALA_API_KEY"]}
    base_url = os.environ.get("IMZALA_BASE_URL")
    if base_url:
        kwargs["base_url"] = base_url  # aksi halde SDK varsayılanı: api-prd
    return Imzala(**kwargs)


def test_me_returns_owner_and_credits(imzala: Imzala) -> None:
    me = imzala.me()
    assert me is not None
    # e-posta ya da id alanlarından biri dolu olmalı
    assert _field(me, "email") or _field(me, "id")


def test_templates_list_unwraps_envelope(imzala: Imzala) -> None:
    res = imzala.templates.list(limit=3)
    assert isinstance(_field(res, "templates"), list)
    assert isinstance(_field(res, "total"), int)


def test_demands_list_is_counts_only_without_raw_pii(imzala: Imzala) -> None:
    res = imzala.demands.list(limit=3)
    assert isinstance(_field(res, "demands"), list)
    # counts-only liste ham e-posta/telefon içermemeli (yalnızca sayılar)
    assert not RAW_EMAIL_RE.search(_dump(res))


def test_demand_get_and_timeline_when_a_demand_exists(imzala: Imzala) -> None:
    listing = imzala.demands.list(limit=1)
    rows = _field(listing, "demands") or []
    if not rows:
        pytest.skip("hesapta sözleşme yok, detay/timeline atlandı")

    first_id = _field(rows[0], "id")
    demand = imzala.demands.get(first_id)
    assert _field(demand, "id") == first_id

    # detay maskeli olmalı: taraf e-postaları maskeli (ham e-posta yok)
    for party in _field(demand, "parties") or []:
        masked = _field(party, "email_masked") or ""
        assert not re.match(r"^[^*]+@", masked)  # yerel kısım maskesiz değil

    timeline = imzala.demands.get_timeline(first_id)
    assert isinstance(_field(timeline, "events"), list)


def test_invalid_demand_id_raises_typed_imzala_error(imzala: Imzala) -> None:
    with pytest.raises(ImzalaError):
        imzala.demands.get("00000000-0000-0000-0000-000000000000")
