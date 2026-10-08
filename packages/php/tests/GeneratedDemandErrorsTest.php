<?php

declare(strict_types=1);

namespace Imzala\Tests;

use GuzzleHttp\Client;
use GuzzleHttp\Handler\MockHandler;
use GuzzleHttp\HandlerStack;
use GuzzleHttp\Middleware;
use GuzzleHttp\Psr7\Response;
use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Api\RemindersApi;
use Imzala\DemandsResource;
use Imzala\ImzalaException;
use Imzala\RetryConfig;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

/** Real generated HTTP status switch/ObjectSerializer before facade Http::unwrap. */
final class GeneratedDemandErrorsTest extends TestCase
{
    public static function responses(): iterable
    {
        $cases = [
            'old-bulk' => [400, ['success' => false, 'error' => 'BULK_MAX_10'], 'BULK_MAX_10', 'BULK_MAX_10'],
            'old-selection' => [400, ['success' => false, 'error' => 'Selection rejected', 'code' => 'INVALID_DOCUMENT_SELECTION', 'message' => 'Synthetic selection message', 'field' => 'documents', 'details' => ['reason' => 'shape', 'document_ids' => ['synthetic-doc']]], 'INVALID_DOCUMENT_SELECTION', 'Synthetic selection message'],
            'new-timing' => [400, ['success' => false, 'error' => 'Timing rejected', 'code' => 'INVALID_DOCUMENT_COLLECTION_TIMING'], 'INVALID_DOCUMENT_COLLECTION_TIMING', 'Timing rejected'],
            'future400' => [400, ['success' => false, 'error' => 'Future collection rejection', 'code' => 'FUTURE_COLLECTION_ERROR'], 'FUTURE_COLLECTION_ERROR', 'Future collection rejection'],
            'disabled503' => [503, ['success' => false, 'error' => 'DOCUMENT_VERIFICATION_DISABLED', 'code' => 'DOCUMENT_VERIFICATION_DISABLED'], 'DOCUMENT_VERIFICATION_DISABLED', 'DOCUMENT_VERIFICATION_DISABLED'],
            'future503' => [503, ['success' => false, 'error' => 'Future unavailable policy', 'code' => 'FUTURE_COLLECTION_ERROR'], 'FUTURE_COLLECTION_ERROR', 'Future unavailable policy'],
        ];
        foreach (['DOCUMENT_TYPE_INVALID', 'VERIFICATION_POLICY_INVALID', 'VERIFICATION_INSTRUCTION_INVALID', 'VERIFICATION_CONTEXT_REQUIRED', 'VERIFICATION_POLICY_CONFLICT'] as $origin) {
            $cases['policy-' . $origin] = [400, ['success' => false, 'error' => $origin, 'code' => 'VERIFICATION_CONTEXT_REQUIRED'], 'VERIFICATION_CONTEXT_REQUIRED', $origin];
        }
        foreach (['single', 'bulk'] as $surface) {
            foreach ($cases as $name => $case) {
                yield $surface . '-' . $name => [$surface, ...$case];
            }
        }
    }

    #[DataProvider('responses')]
    public function testGeneratedTransportPreservesMapperEnvelope(string $surface, int $status, array $body, string $code, string $message): void
    {
        $history = [];
        $stack = HandlerStack::create(new MockHandler([new Response($status, ['Content-Type' => 'application/json'], json_encode($body, JSON_THROW_ON_ERROR))]));
        $stack->push(Middleware::history($history));
        $transport = new Client(['handler' => $stack]);
        $resource = new DemandsResource(new DemandsApi($transport), new RemindersApi($transport), new RetryConfig(0, 0));
        $party = [['template_party_id' => 'synthetic-party', 'first_name' => 'Synthetic', 'last_name' => 'Person', 'email' => 'synthetic@example.com']];
        try {
            if ($surface === 'single') {
                $resource->create(['template_id' => 'synthetic-template', 'party_mapping' => $party]);
            } else {
                $resource->createBulk(['template_id' => 'synthetic-template', 'rows' => [['party_mapping' => $party]]]);
            }
            $this->fail('Expected HTTP error');
        } catch (ImzalaException $e) {
            $this->assertSame(ImzalaException::class, $e::class);
            $this->assertSame($status, $e->getStatusCode());
            $this->assertSame($code, $e->getErrorCode());
            $this->assertSame($message, $e->getMessage());
            $decoded = json_decode($e->getBody(), true, flags: JSON_THROW_ON_ERROR);
            foreach ($body as $key => $value) {
                $this->assertSame($value, $decoded[$key] ?? null, $key);
            }
            if ($code === 'FUTURE_COLLECTION_ERROR') $this->assertNull($e->getCodeDescription());
        }
        $this->assertCount(1, $history);
        $this->assertSame('POST', $history[0]['request']->getMethod());
        $this->assertSame($surface === 'single' ? '/api/v1/demands' : '/api/v1/demands/bulk', $history[0]['request']->getUri()->getPath());
    }
}
