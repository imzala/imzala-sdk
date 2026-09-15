"""Shared test helpers for the facade tests."""

from unittest.mock import patch


def patch_api(api_class, method_name, **kwargs):
    """`patch.object` with `autospec=True`.

    The autospec copies the generated method's real signature, so a facade
    call that passes a keyword the generated client does not have (for
    example after a regeneration renames it) fails the test with a
    `TypeError` instead of passing silently against a permissive mock.
    """
    return patch.object(api_class, method_name, autospec=True, **kwargs)
