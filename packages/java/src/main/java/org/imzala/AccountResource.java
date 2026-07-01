package org.imzala;

import org.imzala.client.generated.api.AccountApi;
import org.imzala.client.generated.model.ApiV1MeGet200ResponseData;

/** Backs {@code imzala.me()} — the vendored generated {@code AccountApi} under the hood. */
final class AccountResource {

  private final AccountApi api;

  AccountResource(AccountApi api) {
    this.api = api;
  }

  /** Returns the calling API key's owner info (id, email, name, workspace, remaining credits). Requires the {@code timestamps} scope. */
  ApiV1MeGet200ResponseData me() {
    return Http.unwrap(
        api::apiV1MeGet,
        r -> Boolean.TRUE.equals(r.getSuccess()),
        r -> r.getData());
  }
}
