<?php

declare(strict_types=1);

namespace Imzala\Tests;

use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Model\ApiV1TemplatesGet200Response;
use Imzala\Client\Model\ApiV1TemplatesGet200ResponseData;
use Imzala\Client\Model\TemplateSummary;
use Imzala\RetryConfig;
use Imzala\TemplatesResource;
use PHPUnit\Framework\TestCase;

/** {@code templates()->listAll()} — mirrors {@code pagination.test.ts} (Node). */
final class PaginationTest extends TestCase
{
    /**
     * @param TemplateSummary[] $templates
     * @return array{0: ApiV1TemplatesGet200Response, 1: int, 2: array<string, string[]>}
     */
    private static function page(array $templates, int $total, int $pageNum, int $limit): array
    {
        $data = new ApiV1TemplatesGet200ResponseData([
            'templates' => $templates,
            'total' => $total,
            'page' => $pageNum,
            'limit' => $limit,
        ]);
        $envelope = new ApiV1TemplatesGet200Response(['success' => true, 'data' => $data]);

        return [$envelope, 200, []];
    }

    public function testWalksTwoFullPagesThenAShortPageYieldingEveryItemAndStoppingWithNoExtraRequest(): void
    {
        $pages = [
            self::page([new TemplateSummary(['id' => 't1']), new TemplateSummary(['id' => 't2'])], 5, 1, 2),
            self::page([new TemplateSummary(['id' => 't3']), new TemplateSummary(['id' => 't4'])], 5, 2, 2),
            // short page (1 item < limit 2) -> stop
            self::page([new TemplateSummary(['id' => 't5'])], 5, 3, 2),
        ];

        $calls = [];
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->exactly(3))
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->willReturnCallback(function ($page, $limit) use (&$calls, $pages) {
                $calls[] = [$page, $limit];
                return $pages[count($calls) - 1];
            });

        $resource = new TemplatesResource($api, new RetryConfig(0, 0));

        $ids = [];
        foreach ($resource->listAll(null, 2) as $template) {
            $ids[] = $template->getId();
        }

        $this->assertSame(['t1', 't2', 't3', 't4', 't5'], $ids);
        $this->assertSame([[1, 2], [2, 2], [3, 2]], $calls);
    }

    public function testStopsAsSoonAsTotalIsReachedEvenWhenLastPageIsExactlyFullNoInfiniteLoop(): void
    {
        $pages = [
            self::page([new TemplateSummary(['id' => 't1']), new TemplateSummary(['id' => 't2'])], 4, 1, 2),
            self::page([new TemplateSummary(['id' => 't3']), new TemplateSummary(['id' => 't4'])], 4, 2, 2),
        ];
        // If the total-reached check didn't exist, a 3rd call would happen
        // here (the 2nd page was exactly `limit` items, not "short") and
        // the mock's exactly(2) expectation would fail — catching a
        // regression instead of spinning forever.

        $callIndex = 0;
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->exactly(2))
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->willReturnCallback(function () use (&$callIndex, $pages) {
                return $pages[$callIndex++];
            });

        $resource = new TemplatesResource($api, new RetryConfig(0, 0));

        $ids = [];
        foreach ($resource->listAll(null, 2) as $template) {
            $ids[] = $template->getId();
        }

        $this->assertSame(['t1', 't2', 't3', 't4'], $ids);
    }

    public function testStopsImmediatelyOnAnEmptyFirstPageNoInfiniteLoop(): void
    {
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->willReturn(self::page([], 0, 1, 20));

        $resource = new TemplatesResource($api, new RetryConfig(0, 0));

        $ids = [];
        foreach ($resource->listAll() as $template) {
            $ids[] = $template->getId();
        }

        $this->assertSame([], $ids);
    }

    public function testListSinglePageIsUnaffectedStillReturnsOnePageNotAnIterator(): void
    {
        $api = $this->createMock(TemplatesApi::class);
        $api->expects($this->once())
            ->method('apiV1TemplatesGetWithHttpInfo')
            ->willReturn(self::page([new TemplateSummary(['id' => 't1']), new TemplateSummary(['id' => 't2'])], 5, 1, 2));

        $resource = new TemplatesResource($api, new RetryConfig(0, 0));
        $result = $resource->list(1, 2);

        $this->assertCount(2, $result->getTemplates());
        $this->assertSame(5, $result->getTotal());
    }
}
