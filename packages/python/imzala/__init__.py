"""imzala.org server-side SDK for Python.

    from imzala import Imzala

    client = Imzala(api_key="imz_...")
    demand = client.demands.create({...})
"""

from .client import DEFAULT_BASE_URL, DEFAULT_TIMEOUT_S, Imzala
from .error_codes import IMZALA_ERROR_CODES, describe_error_code, is_known_error_code
from .errors import (
    ImzalaAuthError,
    ImzalaError,
    ImzalaRateLimitError,
    ImzalaRateLimitInfo,
    ImzalaValidationError,
)
from .files import FileInput, UploadPartyInput
from .webhook import verify_webhook

__version__ = "1.0.0"

__all__ = [
    "Imzala",
    "DEFAULT_BASE_URL",
    "DEFAULT_TIMEOUT_S",
    "ImzalaError",
    "ImzalaAuthError",
    "ImzalaRateLimitError",
    "ImzalaValidationError",
    "ImzalaRateLimitInfo",
    "IMZALA_ERROR_CODES",
    "describe_error_code",
    "is_known_error_code",
    "FileInput",
    "UploadPartyInput",
    "verify_webhook",
    "__version__",
]
