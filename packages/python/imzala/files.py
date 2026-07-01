"""Ergonomic in-memory file-upload helpers for multipart endpoints
(`demands.upload_document`, `timestamps.create`).

The generated client's multipart file params accept `bytes`, a filesystem
path (`str`), or a `(filename, bytes)` tuple — see
`imzala_client.api_client.ApiClient.files_parameters`. A bare filesystem
path is inconvenient for servers holding upload bytes in memory (e.g. from
a Flask/FastAPI upload, `open(...).read()`, or an upstream fetch), so the
facade normalizes everything to the `(filename, bytes)` tuple form.
"""

from __future__ import annotations

from dataclasses import dataclass
from typing import Optional, Tuple, Union

__all__ = ["FileInput", "UploadPartyInput", "to_multipart_tuple"]

FileBytes = Union[bytes, bytearray]


@dataclass
class FileInput:
    """A file to upload, held in memory.

    Attributes:
        content: raw file bytes.
        filename: original filename, including extension (e.g.
            `"sozlesme.pdf"`). Required — the server infers processing
            (PDF vs image vs office doc) from the extension.
        content_type: MIME type. Currently informational only: the
            generated client infers the multipart Content-Type from
            `filename`'s extension (`mimetypes.guess_type`) — the same
            best-effort behavior the server falls back to when a client
            omits it — so this field isn't threaded through separately
            today.
    """

    content: FileBytes
    filename: str
    content_type: Optional[str] = None


def to_multipart_tuple(file_input: FileInput) -> Tuple[str, bytes]:
    """Builds the `(filename, bytes)` tuple the generated client's
    multipart file params expect."""
    return (file_input.filename, bytes(file_input.content))


@dataclass
class UploadPartyInput:
    """A signing party for `demands.upload_document`."""

    first_name: str
    last_name: str
    email: Optional[str] = None  # Email or phone (or both) required per party.
    phone: Optional[str] = None  # E.164 format, e.g. "+905551234567".
