<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\ApiException;
use Imzala\ErrorCodes;
use Imzala\ErrorMapper;
use Imzala\Http;
use Imzala\ImzalaException;
use Imzala\ImzalaRateLimitException;
use Imzala\RateLimitInfo;
use PHPUnit\Framework\TestCase;

/**
 * Keeps the PHP error catalogue in step with the public spec and with the
 * Node SDK catalogue (packages/node/src/errorCodes.ts), and covers how codes,
 * Retry-After and RateLimit-* headers are read off an error response.
 */
final class ErrorCodesTest extends TestCase
{
    /** Same list as packages/node/src/__tests__/errorCodes.test.ts. */
    private const NOT_ERROR_CODES = [
        'ANCHOR_TEXT_NOT_FOUND' => 'field layout diagnostic code inside a response body',
        'CREATE_FAILED' => 'per-row result code in the bulk 200 response',
        'DISPATCH_FAILED' => 'per-party invitation result code in a 200 response',
        'DISPATCH_SKIPPED' => 'invitation result code in a 200 response',
        'DRAFT_UNDISPATCHED' => 'demand status value returned alongside an error, not a code itself',
        'RECIPIENT_QUOTA_EXCEEDED' => 'per-party invitation result code in a 200 response',
        'ON_ANCHOR_MISS_NOT_RELAXED' => 'warning in a 200 response, not an error',
        'DEAD_LETTER' => 'webhook delivery status',
        'DEMAND_ID' => 'shell variable in a curl example',
        'ENVELOPE_DECISION_ENFORCE' => 'server feature flag name',
        'FIELD_LAYOUT' => 'template kind value',
        'FILLABLE_TYPES' => 'server constant name in prose',
        'IMZALA_WEBHOOK_SECRET' => 'environment variable in a code sample',
        'KVKK_CONSENT' => 'doc_kind value',
        'KVKK_NOTICE' => 'doc_kind value',
        'PRICE_LIST' => 'doc_kind value',
        'WEBHOOK_TIMEOUT_MS' => 'server environment variable',
        'COMMENT_ADDED' => 'timeline event_type value',
        'FIELDS_FILLED' => 'timeline event_type value',
        'MOBILE_SIGNATURE_CAPTURED' => 'timeline event_type value',
        'OTP_LOCKED' => 'timeline event_type value',
        'OTP_SENT' => 'timeline event_type value',
        'OTP_VERIFIED' => 'timeline event_type value',
        'FROM_SAVED' => 'stamp source value',
        'FILLER_PROVIDES' => 'stamp source value',
        'STAMP_ITEM_ID' => 'shell variable in a curl example',
        'FIXED_DATE' => 'term_start_mode value',
        'ON_FIRST_SIGNATURE' => 'term_start_mode value',
        'ON_COMPLETION' => 'term_start_mode value',
        'AUTO_RENEW' => 'renewal_type value',
        'FIXED_TERM' => 'renewal_type value',
    ];

    /**
     * Prose also contains single upper-case words (API, PDF, KVKK), so the
     * spec-to-catalogue direction only considers underscore tokens.
     */
    private const CODE_TOKEN = '/\b[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)+\b/';

    private static function repoRoot(): string
    {
        return dirname(__DIR__, 3);
    }

    private static function specText(): string
    {
        $text = file_get_contents(self::repoRoot() . '/spec/openapi.v1.yaml');
        self::assertIsString($text);
        return $text;
    }

    private static function inSpec(string $token): bool
    {
        return preg_match('/\b' . preg_quote($token, '/') . '\b/', self::specText()) === 1;
    }

    /** @return array<string, string> */
    private static function nodeCatalogue(): array
    {
        $src = file_get_contents(self::repoRoot() . '/packages/node/src/errorCodes.ts');
        self::assertIsString($src);
        $body = explode('} as const;', explode('export const IMZALA_ERROR_CODES = {', $src, 2)[1], 2)[0];
        preg_match_all("/^\\s*([A-Z][A-Z0-9_]*):\\s*'([^']*)',/m", $body, $m, PREG_SET_ORDER);
        self::assertNotEmpty($m, 'could not read any entry from errorCodes.ts');
        $out = [];
        foreach ($m as $entry) {
            $out[$entry[1]] = $entry[2];
        }
        return $out;
    }

    // --- catalogue <-> spec ---

    public function testEveryCataloguedCodeAppearsInThePublicSpec(): void
    {
        $missing = array_values(array_filter(array_keys(ErrorCodes::CODES), static fn ($c) => !self::inSpec($c)));
        $this->assertSame([], $missing);
    }

    public function testEveryCodeLikeTokenInTheSpecIsCataloguedOrExcluded(): void
    {
        preg_match_all(self::CODE_TOKEN, self::specText(), $m);
        $tokens = array_unique($m[0]);
        $unclassified = array_values(array_filter(
            $tokens,
            static fn ($t) => !ErrorCodes::isKnown($t) && !array_key_exists($t, self::NOT_ERROR_CODES)
        ));
        $this->assertSame([], $unclassified);
    }

    public function testTheExclusionListHasNoStaleOrOverlappingEntries(): void
    {
        foreach (array_keys(self::NOT_ERROR_CODES) as $token) {
            $this->assertTrue(self::inSpec($token), $token);
            $this->assertFalse(ErrorCodes::isKnown($token), $token);
        }
    }

    public function testEveryDescriptionIsANonEmptySingleLineWithoutAnEmDash(): void
    {
        foreach (ErrorCodes::CODES as $code => $text) {
            $this->assertMatchesRegularExpression('/\S/', $text, $code);
            $this->assertStringNotContainsString("\n", $text, $code);
            $this->assertStringNotContainsString("\u{2014}", $text, $code);
        }
    }

    public function testUnknownOrMissingCodesReturnNullInsteadOfThrowing(): void
    {
        $this->assertNull(ErrorCodes::describe('NEVER_A_REAL_CODE'));
        $this->assertNull(ErrorCodes::describe(null));
        $this->assertFalse(ErrorCodes::isKnown(null));
        $this->assertFalse(ErrorCodes::isKnown('__construct'));
        $this->assertSame(ErrorCodes::CODES['TEMPLATE_IN_USE'], ErrorCodes::describe('TEMPLATE_IN_USE'));
    }

    // --- catalogue <-> Node SDK ---

    public function testSameCodeSetAsTheNodeCatalogue(): void
    {
        $node = array_keys(self::nodeCatalogue());
        $php = array_keys(ErrorCodes::CODES);
        sort($node);
        sort($php);
        $this->assertSame($node, $php);
        $this->assertCount(98, $php);
    }

    public function testSameDescriptionsAsTheNodeCatalogue(): void
    {
        $node = self::nodeCatalogue();
        $drifted = [];
        foreach (ErrorCodes::CODES as $code => $text) {
            if (($node[$code] ?? null) !== $text) {
                $drifted[] = $code;
            }
        }
        $this->assertSame([], $drifted);
    }

    // --- error body shapes ---

    private static function apiException(int $status, array $body, array $headers = []): ApiException
    {
        return new ApiException("[{$status}] error", $status, $headers, (string) json_encode($body, JSON_UNESCAPED_UNICODE));
    }

    public function testErrorCodeWithMessageReadsTheCodeFromError(): void
    {
        $e = ErrorMapper::fromException(self::apiException(400, [
            'success' => false, 'error' => 'PAGE_ID_REQUIRED', 'message' => 'Each item must have an integer page_id',
        ]));
        $this->assertSame('PAGE_ID_REQUIRED', $e->getErrorCode());
        $this->assertSame(ErrorCodes::CODES['PAGE_ID_REQUIRED'], $e->getCodeDescription());
    }

    public function testHumanTextWithCodeReadsTheCodeNeverTheText(): void
    {
        $e = ErrorMapper::fromException(self::apiException(400, [
            'success' => false, 'error' => 'Geçersiz sayfa numarası (page >= 1 olmalı)', 'code' => 'INVALID_PAGE',
        ]));
        $this->assertSame('INVALID_PAGE', $e->getErrorCode());
        $this->assertSame('Geçersiz sayfa numarası (page >= 1 olmalı)', $e->getMessage());
        $this->assertSame(ErrorCodes::CODES['INVALID_PAGE'], $e->getCodeDescription());
    }

    public function testCodeWinsOverACodeShapedErrorString(): void
    {
        $e = ErrorMapper::fromResponse(['success' => false, 'error' => 'TOO_MANY_REQUESTS', 'code' => 'RECIPIENT_RESEND_LIMIT'], 429, []);
        $this->assertSame('RECIPIENT_RESEND_LIMIT', $e->getErrorCode());
    }

    public function testNestedErrorObjectReadsTheNestedCode(): void
    {
        $e = ErrorMapper::fromResponse((object) [
            'success' => false, 'error' => (object) ['code' => 'DEMAND_NOT_DISPATCHABLE', 'message' => 'x'],
        ], 409, []);
        $this->assertSame('DEMAND_NOT_DISPATCHABLE', $e->getErrorCode());
    }

    public function testASingleWordCodeInErrorIsStillACode(): void
    {
        $e = ErrorMapper::fromResponse(['error' => 'UNAUTHORIZED'], 401, []);
        $this->assertSame('UNAUTHORIZED', $e->getErrorCode());
    }

    public function testAPlainHumanReadableErrorStringIsNotReportedAsACode(): void
    {
        $e = ErrorMapper::fromException(self::apiException(404, ['success' => false, 'error' => 'Sözleşme bulunamadı']));
        $this->assertNull($e->getErrorCode());
        $this->assertSame('Sözleşme bulunamadı', $e->getMessage());
        $this->assertNull($e->getCodeDescription());
    }

    public function testShortOrLowercaseErrorStringsAreNotCodes(): void
    {
        $this->assertNull(ErrorMapper::fromResponse(['error' => 'OK'], 400, [])->getErrorCode());
        $this->assertNull(ErrorMapper::fromResponse(['error' => 'not_found'], 400, [])->getErrorCode());
        $this->assertNull(ErrorMapper::fromResponse(['error' => 'BAD CODE'], 400, [])->getErrorCode());
    }

    public function testUnknownCodeIsKeptWithoutADescription(): void
    {
        $e = ErrorMapper::fromResponse(['success' => false, 'code' => 'SOME_FUTURE_CODE'], 409, []);
        $this->assertSame('SOME_FUTURE_CODE', $e->getErrorCode());
        $this->assertNull($e->getCodeDescription());
    }

    public function testConstructingAnExceptionDirectlyFillsCodeDescription(): void
    {
        $this->assertSame(
            ErrorCodes::CODES['TEMPLATE_IN_USE'],
            (new ImzalaException('x', 409, null, 'TEMPLATE_IN_USE'))->getCodeDescription()
        );
        $this->assertNull((new ImzalaException('x'))->getCodeDescription());
    }

    public function testSuccessFalseOnA2xxCarriesTheCodeAndMessage(): void
    {
        try {
            Http::unwrap(static fn () => [
                ['success' => false, 'error' => 'Geçersiz istek', 'code' => 'VALIDATION_FAIL'],
                200,
                [],
            ]);
            $this->fail('expected ImzalaException');
        } catch (ImzalaException $e) {
            $this->assertSame('VALIDATION_FAIL', $e->getErrorCode());
            $this->assertSame('Geçersiz istek', $e->getMessage());
            $this->assertSame(ErrorCodes::CODES['VALIDATION_FAIL'], $e->getCodeDescription());
            $this->assertSame(200, $e->getStatusCode());
        }
    }

    // --- rate limit ---

    public function testAttachesTheDescriptionAndReadsRetryAfterFromTheHeader(): void
    {
        $e = ErrorMapper::fromException(self::apiException(
            429,
            ['success' => false, 'error' => 'Çok fazla istek', 'code' => 'RATE_LIMIT_EXCEEDED'],
            ['retry-after' => ['30']]
        ));
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertSame('RATE_LIMIT_EXCEEDED', $e->getErrorCode());
        $this->assertSame(ErrorCodes::CODES['RATE_LIMIT_EXCEEDED'], $e->getCodeDescription());
        $this->assertSame(30.0, $e->getRetryAfter());
    }

    public function testAnHttpDateRetryAfterIsConvertedToSeconds(): void
    {
        $at = gmdate('D, d M Y H:i:s', time() + 30) . ' GMT';
        $e = ErrorMapper::fromException(self::apiException(429, ['success' => false, 'code' => 'RATE_LIMIT_EXCEEDED'], ['Retry-After' => [$at]]));
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertGreaterThanOrEqual(28.0, $e->getRetryAfter());
        $this->assertLessThanOrEqual(30.0, $e->getRetryAfter());
    }

    public function testAPastHttpDateRetryAfterIsZeroNotNegative(): void
    {
        $at = gmdate('D, d M Y H:i:s', time() - 120) . ' GMT';
        $e = ErrorMapper::fromResponse(['success' => false], 429, ['Retry-After' => [$at]]);
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertSame(0.0, $e->getRetryAfter());
    }

    public function testAnUnparseableRetryAfterIsNull(): void
    {
        $e = ErrorMapper::fromResponse(['success' => false], 429, ['Retry-After' => ['yakında']]);
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertNull($e->getRetryAfter());
    }

    public function testReadsRetryAfterSecondsFromTheBodyWhenThereIsNoHeader(): void
    {
        $e = ErrorMapper::fromException(self::apiException(
            429,
            [
                'success' => false,
                'error' => 'Çok fazla istek gönderildi. Lütfen bir dakika bekleyin.',
                'code' => 'RATE_LIMIT_EXCEEDED',
                'retry_after_seconds' => 60,
            ],
            ['ratelimit-limit' => ['60'], 'ratelimit-policy' => ['60;w=60']]
        ));
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertSame('RATE_LIMIT_EXCEEDED', $e->getErrorCode());
        $this->assertSame(60.0, $e->getRetryAfter());
    }

    public function testReadsTheStandardRateLimitHeadersAndIgnoresXRateLimit(): void
    {
        $e = ErrorMapper::fromException(self::apiException(
            429,
            ['success' => false, 'code' => 'RATE_LIMIT_EXCEEDED', 'retry_after_seconds' => 60],
            [
                'RateLimit-Limit' => ['5'],
                'RateLimit-Remaining' => ['0'],
                'RateLimit-Reset' => ['42'],
                'RateLimit-Policy' => ['5;w=60'],
                'X-RateLimit-Limit' => ['999'],
            ]
        ));
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $info = $e->getRateLimit();
        $this->assertInstanceOf(RateLimitInfo::class, $info);
        $this->assertSame(5, $info->limit);
        $this->assertSame(0, $info->remaining);
        $this->assertSame(42, $info->reset);
        $this->assertSame('5;w=60', $info->policy);
    }

    public function testHeaderNamesAreReadCaseInsensitively(): void
    {
        $e = ErrorMapper::fromResponse(['success' => false], 429, ['ratelimit-limit' => ['60'], 'RETRY-AFTER' => ['7']]);
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertSame(60, $e->getRateLimit()?->limit);
        $this->assertNull($e->getRateLimit()?->remaining);
        $this->assertNull($e->getRateLimit()?->policy);
        $this->assertSame(7.0, $e->getRetryAfter());
    }

    public function testOnlyXRateLimitHeadersLeaveRateLimitNull(): void
    {
        $e = ErrorMapper::fromResponse(['success' => false], 429, ['X-RateLimit-Limit' => ['60'], 'X-RateLimit-Remaining' => ['0']]);
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertNull($e->getRateLimit());
    }

    public function testLeavesRateLimitNullWhenTheServerSentNoRateLimitHeaders(): void
    {
        $e = ErrorMapper::fromResponse(['success' => false, 'error' => ['code' => 'RATE_LIMITED']], 429, null);
        $this->assertInstanceOf(ImzalaRateLimitException::class, $e);
        $this->assertNull($e->getRateLimit());
        $this->assertSame('RATE_LIMITED', $e->getErrorCode());
    }
}
