<?php

declare(strict_types=1);

namespace Imzala;

use Generator;
use Imzala\Client\Api\ContactsApi;
use Imzala\Client\Model\ApiV1ContactsGet200ResponseData;
use Imzala\Client\Model\ApiV1ContactsPostRequest;
use Imzala\Client\Model\ContactSummary;

/** {@code $imzala->contacts()}, backed by the vendored generated {@see ContactsApi}. */
final class ContactsResource
{
    public function __construct(
        private readonly ContactsApi $api,
        private readonly RetryConfig $retryConfig,
    ) {
    }

    /**
     * Lists contacts in your workspace (one page). GET, safe to auto-retry.
     *
     * @param int|null $limit 10 to 100, default 25
     */
    public function list(
        ?string $q = null,
        ?int $page = null,
        ?int $limit = null,
        ?string $sort = null,
        ?string $companyId = null,
        ?bool $archived = null,
    ): ApiV1ContactsGet200ResponseData {
        return Http::unwrapRetryableGet(
            fn () => $this->api->apiV1ContactsGetWithHttpInfo(
                page: $page,
                limit: $limit,
                q: $q,
                sort: $sort,
                company_id: $companyId,
                archived: $archived,
            ),
            $this->retryConfig,
        );
    }

    /**
     * Walks every page of contacts, yielding one contact at a time, with the
     * same filters on every page.
     *
     * @param int|null $limit 10 to 100, default 25
     * @return Generator<int, ContactSummary>
     */
    public function listAll(
        ?string $q = null,
        ?int $page = null,
        ?int $limit = null,
        ?string $sort = null,
        ?string $companyId = null,
        ?bool $archived = null,
    ): Generator {
        $currentPage = $page ?? 1;
        $yielded = 0;

        for (;;) {
            $result = $this->list($q, $currentPage, $limit, $sort, $companyId, $archived);
            $contacts = $result->getContacts() ?? [];

            foreach ($contacts as $contact) {
                yield $contact;
            }
            $yielded += count($contacts);

            if (count($contacts) === 0) {
                break;
            }

            $total = $result->getTotal();
            if ($total !== null && $yielded >= $total) {
                break;
            }

            $effectiveLimit = $result->getLimit() ?? $limit;
            if ($effectiveLimit !== null && count($contacts) < $effectiveLimit) {
                break;
            }

            $currentPage = ($result->getPage() ?? $currentPage) + 1;
        }
    }

    /**
     * Adds a contact. An active contact with the same e-mail or phone throws
     * {@code CONTACT_DUPLICATE}. This endpoint has no idempotency key, so it
     * is never retried. POST.
     *
     * @param ApiV1ContactsPostRequest|array<string, mixed> $body a generated
     *     request instance, or a plain associative array with the same keys
     */
    public function create(ApiV1ContactsPostRequest|array $body): ContactSummary
    {
        $request = $body instanceof ApiV1ContactsPostRequest ? $body : new ApiV1ContactsPostRequest($body);
        return Http::unwrap(fn () => $this->api->apiV1ContactsPostWithHttpInfo(api_v1_contacts_post_request: $request));
    }
}
