<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\TemplatesApi;
use Imzala\Client\Model\ApiV1TemplatesGet200ResponseData;
use Imzala\Client\Model\TemplateDetail;
use Imzala\Client\Model\TemplateUsage;

/** {@code $imzala->templates()} — backed by the vendored generated {@see TemplatesApi}. */
final class TemplatesResource
{
    public function __construct(private readonly TemplatesApi $api)
    {
    }

    /** Lists your active templates. */
    public function list(?int $page = null, ?int $limit = null): ApiV1TemplatesGet200ResponseData
    {
        return Http::unwrap(fn () => $this->api->apiV1TemplatesGetWithHttpInfo($page, $limit));
    }

    /** Returns a template's parties + fillable variables. */
    public function get(string $id): TemplateDetail
    {
        return Http::unwrap(fn () => $this->api->apiV1TemplatesIdGetWithHttpInfo($id));
    }

    /** Returns a ready-to-use integration guide (endpoint, required headers, example curl+JSON) for a template. */
    public function usage(string $id): TemplateUsage
    {
        return Http::unwrap(fn () => $this->api->apiV1TemplatesIdUsageGetWithHttpInfo($id));
    }
}
