<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Model\DemandStatus;
use Imzala\Client\Model\PageItem;
use Imzala\Client\Model\StandardError;
use Imzala\Client\Model\TemplateVariable;
use Imzala\Client\Model\WebhookEnvelope;
use Imzala\Client\ObjectSerializer;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

// Response enums grow with the API (item_type gained contract_number in
// 1.14.5 and stamp in 1.14.6; status, decision, doc_kind, error codes and
// webhook types grow the same way). Since SDK 1.2.0 every enum the server can
// return is an open string: a value the client was generated without must not
// break deserialization or fail model validation.
final class GeneratedEnumToleranceTest extends TestCase
{
    private const DEMAND = '{"id":"00000000-0000-4000-8000-000000000001","title":"Ornek","status":"future_value",'
        . '"parties":[{"party_id":"00000000-0000-4000-8000-000000000002","locale":"future_value","signed_locale":"future_value"}],'
        . '"stamp_items":[{"item_id":1,"source":"future_value"}],'
        . '"documents":[{"id":"00000000-0000-4000-8000-000000000003","doc_kind":"future_value",'
        . '"progress":{"status":"future_value"},"sealing":{"status":"future_value"},'
        . '"decisions":[{"party_id":"00000000-0000-4000-8000-000000000002","decision":"future_value"}]}]}';

    /** @return array<string, array{string}> */
    public static function pageItemTypes(): array
    {
        return ['contract_number' => ['contract_number'], 'stamp' => ['stamp'], 'future_value' => ['future_value']];
    }

    #[DataProvider('pageItemTypes')]
    public function testPageItemKeepsItemType(string $value): void
    {
        $data = json_decode(
            '{"page_id":1,"item_type":"' . $value . '","position_x":0.1,"position_y":0.2,"width":0.3,"height":0.1}'
        );
        $item = ObjectSerializer::deserialize($data, PageItem::class);
        $this->assertSame($value, $item->getItemType());
    }

    public function testTemplateVariableKeepsItemType(): void
    {
        foreach (['stamp', 'future_value'] as $value) {
            $var = ObjectSerializer::deserialize(json_decode('{"slug":"a","item_type":"' . $value . '"}'), TemplateVariable::class);
            $this->assertSame($value, $var->getItemType());
        }
    }

    public function testDemandStatusKeepsUnknownValues(): void
    {
        /** @var DemandStatus $d */
        $d = ObjectSerializer::deserialize(json_decode(self::DEMAND), DemandStatus::class);
        $this->assertSame('future_value', $d->getStatus());
        $this->assertSame([], $d->listInvalidProperties());
        $this->assertSame('future_value', $d->getParties()[0]->getLocale());
        $this->assertSame('future_value', $d->getParties()[0]->getSignedLocale());
        $this->assertSame('future_value', $d->getStampItems()[0]->getSource());
        $doc = $d->getDocuments()[0];
        $this->assertSame('future_value', $doc->getDocKind());
        $this->assertSame([], $doc->listInvalidProperties());
        $this->assertSame('future_value', $doc->getProgress()->getStatus());
        $this->assertSame('future_value', $doc->getSealing()->getStatus());
        $this->assertSame('future_value', $doc->getDecisions()[0]->getDecision());
    }

    public function testWebhookTypeAndErrorCodeKeepUnknownValues(): void
    {
        /** @var WebhookEnvelope $event */
        $event = ObjectSerializer::deserialize(json_decode(
            '{"id":"evt_0123456789abcdef0123456789abcdef","type":"future_value","created_at":"2026-11-01T00:00:00Z","data":{}}'
        ), WebhookEnvelope::class);
        $this->assertSame('future_value', $event->getType());
        $this->assertSame([], $event->listInvalidProperties());
        /** @var StandardError $error */
        $error = ObjectSerializer::deserialize(json_decode(
            '{"success":false,"error":{"code":"future_value","message":"Ornek"}}'
        ), StandardError::class);
        $this->assertSame('future_value', $error->getError()->getCode());
    }
}
