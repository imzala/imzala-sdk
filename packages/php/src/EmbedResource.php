<?php

declare(strict_types=1);

namespace Imzala;

use Imzala\Client\Api\DemandsApi;
use Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPost200ResponseData;
use Imzala\Client\Model\ApiV1DemandsIdEmbedSessionPostRequest;

/** {@code $imzala->embed()} — backed by the vendored generated {@see DemandsApi} (embed-session lives on the demands route). */
final class EmbedResource
{
    public function __construct(private readonly DemandsApi $api)
    {
    }

    /**
     * Mints a short-lived, single-use embed signing token for a demand's
     * party. The returned {@code embed_url} is meant for an
     * {@code <iframe>}.
     *
     * <p>Signatures obtained this way are SES by default (AES if TC/biometric
     * verification ran) — this flow never produces QES.
     *
     * @param string $demandId the demand to mint a session for
     * @param string $partyId the party to mint an embed session for — from {@code signing_urls[].party_id} in the demand's create/get response
     */
    public function createSession(string $demandId, string $partyId): ApiV1DemandsIdEmbedSessionPost200ResponseData
    {
        $request = new ApiV1DemandsIdEmbedSessionPostRequest(['party_id' => $partyId]);
        return Http::unwrap(fn () => $this->api->apiV1DemandsIdEmbedSessionPostWithHttpInfo($demandId, $request));
    }
}
