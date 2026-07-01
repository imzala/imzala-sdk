"""imzala.org server-side SDK for Python.

    from imzala import Imzala

    client = Imzala(api_key="imz_...")
    demand = client.demands.create({...})
"""

from .client import DEFAULT_BASE_URL, DEFAULT_TIMEOUT_S, Imzala
from .errors import (
    ImzalaAuthError,
    ImzalaError,
    ImzalaRateLimitError,
    ImzalaValidationError,
)
from .files import FileInput, UploadPartyInput
from .webhook import verify_webhook

__version__ = "0.1.0"

__all__ = [
    "Imzala",
    "DEFAULT_BASE_URL",
    "DEFAULT_TIMEOUT_S",
    "ImzalaError",
    "ImzalaAuthError",
    "ImzalaRateLimitError",
    "ImzalaValidationError",
    "FileInput",
    "UploadPartyInput",
    "verify_webhook",
    "__version__",
]
