<?php

declare(strict_types=1);

namespace Imzala;

/**
 * Request payload failed validation: a 422 from the server, or a parameter
 * the generated client rejects before sending (e.g. a list limit outside the
 * documented range). In the latter case getStatusCode() is null.
 */
final class ImzalaValidationException extends ImzalaException
{
}
