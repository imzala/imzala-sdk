<?php

declare(strict_types=1);

namespace Imzala;

use Generator;
use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Model\ApiV1TemplatesGet200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdDelete200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdPatch200ResponseData;
use Imzala\Client\Model\ApiV1TemplatesIdPatchRequest;
use Imzala\Client\Model\TemplateDetail;
use Imzala\Client\Model\TemplateSummary;
use Imzala\Client\Model\TemplateUsage;

/** {@code $imzala->templates()} — backed by the vendored generated {@see TemplatesApi}. */
final class TemplatesResource
{
    public function __construct(
        private readonly TemplatesApi $api,
        private readonly RetryConfig $retryConfig,
    ) {
    }

    /** Lists your active templates (one page). GET — safe to auto-retry. */
    public function list(?int $page = null, ?int $limit = null): ApiV1TemplatesGet200ResponseData
    {
        return Http::unwrapRetryableGet(
            fn () => $this->api->apiV1TemplatesGetWithHttpInfo($page, $limit),
            $this->retryConfig
        );
    }

    /** Returns a template's parties + fillable variables. GET — safe to auto-retry. */
    public function get(string $id): TemplateDetail
    {
        return Http::unwrapRetryableGet(fn () => $this->api->apiV1TemplatesIdGetWithHttpInfo($id), $this->retryConfig);
    }

    /** Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template. GET — safe to auto-retry. */
    public function usage(string $id): TemplateUsage
    {
        return Http::unwrapRetryableGet(fn () => $this->api->apiV1TemplatesIdUsageGetWithHttpInfo($id), $this->retryConfig);
    }

    /**
     * Walks every page of your active templates, transparently, yielding
     * one template at a time. Internally calls the existing (unchanged)
     * {@see self::list()} — itself now retry-wrapped — and increments the
     * page (from the response's own {@code page} field, not a
     * locally-tracked counter) until a page comes back short (fewer items
     * than the effective page size) or the response's {@code total} has
     * been reached — whichever happens first — so it always terminates
     * even against a misbehaving/empty result set.
     *
     * <pre>{@code
     * foreach ($imzala->templates()->listAll(limit: 50) as $template) {
     *     echo $template->getId() . ' ' . $template->getName() . "\n";
     * }
     * }</pre>
     *
     * @return Generator<int, TemplateSummary>
     */
    public function listAll(?int $page = null, ?int $limit = null): Generator
    {
        $requestedLimit = $limit;
        $currentPage = $page ?? 1;
        $yielded = 0;

        for (;;) {
            $result = $this->list($currentPage, $requestedLimit);
            $templates = $result->getTemplates() ?? [];

            foreach ($templates as $template) {
                yield $template;
            }
            $yielded += count($templates);

            if (count($templates) === 0) {
                break;
            }

            $total = $result->getTotal();
            if ($total !== null && $yielded >= $total) {
                break;
            }

            $effectiveLimit = $result->getLimit() ?? $requestedLimit;
            if ($effectiveLimit !== null && count($templates) < $effectiveLimit) {
                break;
            }

            $currentPage = ($result->getPage() ?? $currentPage) + 1;
        }
    }

    /**
     * Updates a template's metadata (name / description / category). The
     * page/field/party structure can't be changed via the API — edit that in
     * the dashboard. PATCH — never auto-retried.
     *
     * @param ApiV1TemplatesIdPatchRequest|array<string, mixed> $body a
     *     generated request instance, or a plain associative array with the
     *     same keys — e.g. {@code ['name' => 'Yeni Ad', 'category' => 'HR']}
     */
    public function update(string $id, ApiV1TemplatesIdPatchRequest|array $body): ApiV1TemplatesIdPatch200ResponseData
    {
        $request = $body instanceof ApiV1TemplatesIdPatchRequest ? $body : new ApiV1TemplatesIdPatchRequest($body);
        return Http::unwrap(fn () => $this->api->apiV1TemplatesIdPatchWithHttpInfo($id, $request));
    }

    /**
     * Deletes (soft-deletes) a template. Existing demands created from it are
     * unaffected. DELETE — never auto-retried.
     */
    public function delete(string $id): ApiV1TemplatesIdDelete200ResponseData
    {
        return Http::unwrap(fn () => $this->api->apiV1TemplatesIdDeleteWithHttpInfo($id));
    }
}
