<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\ImzalaClient;
use PHPUnit\Framework\TestCase;

final class WebhookTest extends TestCase
{
    private const SECRET = 'whsec_' . '0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcd';

    public function testValidSignaturePasses(): void
    {
        $rawBody = '{"id":"evt_abc123","type":"demand.completed","data":{"demand_id":"d1"}}';
        $signature = 'sha256=' . hash_hmac('sha256', $rawBody, self::SECRET);

        $this->assertTrue(ImzalaClient::verifyWebhook(self::SECRET, $rawBody, $signature));
    }

    public function testWrongSecretFails(): void
    {
        $rawBody = '{"id":"evt_abc123"}';
        $signature = 'sha256=' . hash_hmac('sha256', $rawBody, self::SECRET);

        $this->assertFalse(ImzalaClient::verifyWebhook('whsec_different_secret', $rawBody, $signature));
    }

    public function testTamperedBodyFails(): void
    {
        $rawBody = '{"id":"evt_abc123"}';
        $signature = 'sha256=' . hash_hmac('sha256', $rawBody, self::SECRET);

        $this->assertFalse(ImzalaClient::verifyWebhook(self::SECRET, $rawBody . 'tampered', $signature));
    }

    public function testMissingSha256PrefixFails(): void
    {
        $rawBody = '{"id":"evt_abc123"}';
        $hex = hash_hmac('sha256', $rawBody, self::SECRET);

        $this->assertFalse(ImzalaClient::verifyWebhook(self::SECRET, $rawBody, $hex));
    }

    public function testGarbageSameLengthHeaderFails(): void
    {
        $rawBody = '{"id":"evt_abc123"}';
        $valid = 'sha256=' . hash_hmac('sha256', $rawBody, self::SECRET);
        $garbage = 'sha256=' . str_repeat('0', strlen($valid) - 7);

        $this->assertFalse(ImzalaClient::verifyWebhook(self::SECRET, $rawBody, $garbage));
    }

    public function testMissingHeaderFails(): void
    {
        $this->assertFalse(ImzalaClient::verifyWebhook(self::SECRET, 'body', null));
    }

    public function testEmptyHeaderFails(): void
    {
        $this->assertFalse(ImzalaClient::verifyWebhook(self::SECRET, 'body', ''));
    }

    public function testEmptySecretFails(): void
    {
        $rawBody = 'body';
        // Reuse a real signature (computed with a real secret) as an
        // arbitrary header value — verifyWebhook's own empty-secret guard
        // must short-circuit to false before ever touching hash_hmac()
        // with an empty key.
        $signature = 'sha256=' . hash_hmac('sha256', $rawBody, self::SECRET);

        $this->assertFalse(ImzalaClient::verifyWebhook('', $rawBody, $signature));
    }

    public function testEmptyBodyStillVerifiesCorrectly(): void
    {
        $rawBody = '';
        $signature = 'sha256=' . hash_hmac('sha256', $rawBody, self::SECRET);

        $this->assertTrue(ImzalaClient::verifyWebhook(self::SECRET, $rawBody, $signature));
    }

    public function testUnicodeBodyIsHashedAsUtf8Bytes(): void
    {
        $rawBody = '{"title":"Kira Sözleşmesi — İşyeri"}';
        $signature = 'sha256=' . hash_hmac('sha256', $rawBody, self::SECRET);

        $this->assertTrue(ImzalaClient::verifyWebhook(self::SECRET, $rawBody, $signature));
    }
}
