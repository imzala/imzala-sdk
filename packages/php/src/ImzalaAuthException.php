<?php

declare(strict_types=1);

namespace Imzala;

/** Missing/invalid API key (401) or disabled key / insufficient scope (403). */
final class ImzalaAuthException extends ImzalaException
{
}
