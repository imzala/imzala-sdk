<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\ImzalaClient;
use Imzala\ImzalaException;
use PHPUnit\Framework\TestCase;

/**
 * Uçtan uca (e2e) testler, GERÇEK İmzala API'sine karşı çalışır.
 *
 * Varsayılan olarak ATLANIR (setUp'ta markTestSkipped). Çalıştırmak için
 * ortam değişkenleri:
 *   IMZALA_E2E=1
 *   IMZALA_API_KEY=imz_...
 *   IMZALA_BASE_URL=https://test-api.imzala.org   (opsiyonel; varsayılan prod)
 *
 *   IMZALA_E2E=1 IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
 *     vendor/bin/phpunit tests/E2eTest.php
 *
 * Yalnızca SALT-OKUMA uçları çağrılır (kredi harcamaz, veri değiştirmez):
 * me / templates.list / demands.list / demands.get / getTimeline + geçersiz
 * id. Böylece herhangi bir gerçek hesaba karşı güvenle koşturulabilir.
 *
 * Node muadili: packages/node/src/__tests__/e2e.test.ts.
 */
final class E2eTest extends TestCase
{
    private ImzalaClient $imzala;

    protected function setUp(): void
    {
        // Ortam bayrağı yoksa her test temizce ATLANIR (markTestSkipped),
        // başarısız olmaz; apiKey'siz client kurmayız (constructor throw eder).
        if (getenv('IMZALA_E2E') !== '1' || (getenv('IMZALA_API_KEY') ?: '') === '') {
            $this->markTestSkipped(
                'e2e devre dışı. IMZALA_E2E=1 ve IMZALA_API_KEY=imz_... ile çalıştırın.'
            );
        }

        $apiKey = (string) getenv('IMZALA_API_KEY');
        $baseUrl = getenv('IMZALA_BASE_URL') ?: '';

        // baseUrl boş ise SDK varsayılanını (api-prd) kullan.
        $this->imzala = $baseUrl !== ''
            ? new ImzalaClient($apiKey, $baseUrl)
            : new ImzalaClient($apiKey);
    }

    public function testMeReturnsOwnerAndCredits(): void
    {
        $me = $this->imzala->me();
        // e-posta ya da id alanlarından biri dolu olmalı
        $this->assertNotEmpty($me->getEmail() ?? $me->getId());
    }

    public function testTemplatesListUnwrapsEnvelope(): void
    {
        $res = $this->imzala->templates()->list(limit: 3);
        $this->assertIsArray($res->getTemplates() ?? []);
        $this->assertIsInt($res->getTotal());
    }

    public function testDemandsListIsCountsOnlyAndLeaksNoPartyPii(): void
    {
        $res = $this->imzala->demands()->list(limit: 3);
        $this->assertIsArray($res->getDemands() ?? []);

        // counts-only liste ham e-posta içermemeli (yalnızca sayaçlar döner)
        $serialized = (string) json_encode($res, JSON_UNESCAPED_UNICODE);
        $this->assertDoesNotMatchRegularExpression('/@[a-z0-9.-]+\.[a-z]{2,}/i', $serialized);
    }

    public function testDemandGetAndTimelineWhenADemandExists(): void
    {
        $list = $this->imzala->demands()->list(limit: 1);
        $demands = $list->getDemands() ?? [];
        if (count($demands) === 0) {
            $this->markTestSkipped('Hesapta sözleşme yok, get/getTimeline atlandı.');
        }

        $firstId = (string) $demands[0]->getId();

        $demand = $this->imzala->demands()->get($firstId);
        $this->assertSame($firstId, $demand->getId());

        // detay maskeli: taraf ham e-postası yok, yalnızca email_masked var
        foreach ($demand->getParties() ?? [] as $party) {
            $masked = (string) ($party->getEmailMasked() ?? '');
            // '^[^*]+@' → maskesiz (ör. ada@...) eşleşir, maskeli (ör. a***@...) eşleşmez
            $this->assertDoesNotMatchRegularExpression('/^[^*]+@/', $masked);
        }

        $timeline = $this->imzala->demands()->getTimeline($firstId);
        $this->assertIsArray($timeline->getEvents() ?? []);
    }

    public function testInvalidIdThrowsTypedImzalaException(): void
    {
        $this->expectException(ImzalaException::class);
        $this->imzala->demands()->get('00000000-0000-0000-0000-000000000000');
    }
}
