package org.imzala;

import org.imzala.client.generated.api.DemandsApi;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPost200ResponseData;
import org.imzala.client.generated.model.ApiV1DemandsIdEmbedSessionPostRequest;

import java.util.UUID;

/** {@code imzala.embed()} — backed by the vendored generated {@code DemandsApi} (embed-session lives on the demands route). */
public final class EmbedResource {

  private final DemandsApi api;

  EmbedResource(DemandsApi api) {
    this.api = api;
  }

  /**
   * Mints a short-lived, single-use embed signing token for a demand's
   * party. The returned {@code embed_url} is meant for an
   * {@code <iframe>}.
   *
   * <p>Signatures obtained this way are electronic signatures within the
   * meaning of Turkish Law No. 5070 (Art. 3); they are not secure electronic
   * signatures and are not presented as advanced or qualified signatures
   * under EU law. Additional verification steps strengthen evidential value;
   * they do not change the signature level.
   *
   * @param demandId the demand to mint a session for
   * @param partyId the party to mint an embed session for — from {@code signing_urls[].party_id} in the demand's create/get response
   */
  public ApiV1DemandsIdEmbedSessionPost200ResponseData createSession(UUID demandId, UUID partyId) {
    ApiV1DemandsIdEmbedSessionPostRequest body = new ApiV1DemandsIdEmbedSessionPostRequest().partyId(partyId);
    return Http.unwrap(
        () -> api.apiV1DemandsIdEmbedSessionPost(demandId, body),
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }
}
