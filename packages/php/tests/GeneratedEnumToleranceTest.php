<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Model\PageItem;
use Imzala\Client\Model\TemplateVariable;
use Imzala\Client\ObjectSerializer;
use PHPUnit\Framework\Attributes\DataProvider;
use PHPUnit\Framework\TestCase;

// item_type grows with the API (contract_number in 1.14.5, stamp in 1.14.6).
// A value the client was generated without must not break deserialization.
final class GeneratedEnumToleranceTest extends TestCase
{
    /** @return array<string, array{string}> */
    public static function pageItemTypes(): array
    {
        return ['contract_number' => ['contract_number'], 'stamp' => ['stamp'], 'future_type' => ['future_type']];
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
        foreach (['stamp', 'future_type'] as $value) {
            $var = ObjectSerializer::deserialize(json_decode('{"slug":"a","item_type":"' . $value . '"}'), TemplateVariable::class);
            $this->assertSame($value, $var->getItemType());
        }
    }
}
