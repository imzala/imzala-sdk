namespace ImzalaSdk;

/// <summary>
/// Every imzala.org API response uses the same envelope: <c>{success: true,
/// data: {...}}</c> on success, or a non-2xx status with <c>{success: false,
/// error/message: ...}</c> on failure (surfaced by the vendored generated
/// client as a thrown <c>ImzalaApiClient.Client.ApiException</c>, not a
/// resolved-but-failed response).
///
/// <see cref="Unwrap{TResponse, TData}"/> awaits a generated-client call,
/// unwraps its <c>Data</c> property, and normalizes any failure (thrown
/// exception, or a <c>{success:false}</c> body on an otherwise-2xx response)
/// into a typed <see cref="ImzalaError"/> — see <c>Errors.cs</c>. Every
/// resource method in <c>src/Resources/*</c> routes through this. Mirrors
/// <c>http.ts</c>'s <c>unwrap&lt;T&gt;()</c> (TS) and <c>client.py</c>'s
/// <c>_unwrap()</c> (Python); C#'s nominal typing (each generated response is
/// its own concrete class, not a structural <c>{success,data}</c> shape) means
/// the caller passes explicit <c>Success</c>/<c>Data</c> selectors instead of
/// relying on duck typing.
/// </summary>
internal static class Http
{
    public static async Task<TData> Unwrap<TResponse, TData>(
        Task<TResponse> call,
        Func<TResponse, bool> success,
        Func<TResponse, TData> data)
    {
        TResponse response;
        try
        {
            response = await call.ConfigureAwait(false);
        }
        catch (Exception err)
        {
            throw ErrorMapper.Map(err);
        }

        if (response is null || !success(response))
        {
            throw new ImzalaError("imzala.org API request failed");
        }

        return data(response);
    }
}
