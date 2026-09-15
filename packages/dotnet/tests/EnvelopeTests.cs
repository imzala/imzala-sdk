using System.Net;
using System.Text;
using System.Text.RegularExpressions;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// Multi-document envelope endpoints (<c>Demands.Documents</c>) and
/// <c>Demands.DispatchAsync</c>. These tests run the real vendored generated
/// client and <see cref="HttpClient"/> against a local HTTP server, so they
/// check what actually goes over the wire: the path slots, the multipart field
/// that carries the idempotency key, and how a 409 replay body is read back.
/// Mirrors packages/node/src/__tests__/envelope.test.ts.
/// </summary>
public class EnvelopeTests : IDisposable
{
    private static readonly Guid Demand = Guid.Parse("11111111-1111-4111-8111-111111111111");
    private static readonly Guid Doc = Guid.Parse("22222222-2222-4222-8222-222222222222");
    private static readonly Guid DocB = Guid.Parse("33333333-3333-4333-8333-333333333333");
    private static readonly Guid Party = Guid.Parse("44444444-4444-4444-8444-444444444444");

    /// <summary>One recorded request. <see cref="Body"/> is the UTF-8 text (JSON bodies); <see cref="RawBody"/> keeps every byte as one char (multipart bodies).</summary>
    private sealed record Captured(string Method, string Uri, Dictionary<string, string> Headers, string Body, string RawBody);

    private sealed record Reply(int Status, object Body, Dictionary<string, string>? Headers = null);

    /// <summary>A real HTTP server that records every request and answers from the scripted replies (the last one repeats).</summary>
    private sealed class LocalServer : IDisposable
    {
        private readonly HttpListener _listener;
        private readonly IReadOnlyList<Reply> _replies;
        public readonly List<Captured> Calls = new();
        public readonly string BaseUrl;

        public LocalServer(IReadOnlyList<Reply> replies)
        {
            _replies = replies;
            var port = FreePort();
            BaseUrl = $"http://127.0.0.1:{port}";
            _listener = new HttpListener();
            _listener.Prefixes.Add(BaseUrl + "/");
            _listener.Start();
            _ = Task.Run(Serve);
        }

        private static int FreePort()
        {
            var socket = new System.Net.Sockets.TcpListener(IPAddress.Loopback, 0);
            socket.Start();
            var port = ((IPEndPoint)socket.LocalEndpoint).Port;
            socket.Stop();
            return port;
        }

        private async Task Serve()
        {
            while (_listener.IsListening)
            {
                HttpListenerContext ctx;
                try
                {
                    ctx = await _listener.GetContextAsync();
                }
                catch (Exception)
                {
                    return;
                }

                using var buffer = new MemoryStream();
                await ctx.Request.InputStream.CopyToAsync(buffer);
                var bytes = buffer.ToArray();
                var headers = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);
                foreach (var key in ctx.Request.Headers.AllKeys)
                {
                    if (key is not null) headers[key] = ctx.Request.Headers[key] ?? "";
                }
                Reply reply;
                lock (Calls)
                {
                    Calls.Add(new Captured(ctx.Request.HttpMethod, ctx.Request.RawUrl ?? "", headers, Encoding.UTF8.GetString(bytes), Encoding.Latin1.GetString(bytes)));
                    reply = _replies[Math.Min(Calls.Count - 1, _replies.Count - 1)];
                }
                var replyBytes = Encoding.UTF8.GetBytes(JsonConvert.SerializeObject(reply.Body));
                ctx.Response.StatusCode = reply.Status;
                ctx.Response.ContentType = "application/json";
                foreach (var (k, v) in reply.Headers ?? new Dictionary<string, string>())
                {
                    ctx.Response.Headers[k] = v;
                }
                ctx.Response.ContentLength64 = replyBytes.Length;
                await ctx.Response.OutputStream.WriteAsync(replyBytes);
                ctx.Response.Close();
            }
        }

        public void Dispose()
        {
            try { _listener.Stop(); _listener.Close(); } catch (Exception) { }
        }
    }

    private readonly List<LocalServer> _servers = new();

    public void Dispose()
    {
        foreach (var s in _servers) s.Dispose();
        _servers.Clear();
    }

    private LocalServer Server(params Reply[] replies)
    {
        var s = new LocalServer(replies);
        _servers.Add(s);
        return s;
    }

    private static Imzala Client(LocalServer s) => new("imz_test", s.BaseUrl, timeoutMs: 5_000, maxRetries: 2, retryBaseDelayMs: 1);

    private static FileInput Pdf() => new()
    {
        Content = Encoding.ASCII.GetBytes("%PDF-1.7 test"),
        FileName = "kira.pdf",
        ContentType = "application/pdf",
    };

    private static object DocBody() => new { id = Doc, order = 1, title = "Kira sözleşmesi", doc_kind = "CONTRACT" };

    private static Reply Ok(object data) => new(200, new { success = true, data });

    private static Reply RateLimited(string retryAfter = "0") => new(
        429,
        new { success = false, error = "Çok fazla istek", code = "RATE_LIMIT_EXCEEDED" },
        new Dictionary<string, string> { ["Retry-After"] = retryAfter });

    private static Reply Disabled() => new(409, new { success = false, error = "Kapalı", code = "ENVELOPE_MULTI_DOC_DISABLED" });

    private static Reply Conflict(string code, bool withDocument = false) => withDocument
        ? new(409, new { success = false, error = "x", code, data = new { document = DocBody() } })
        : new(409, new { success = false, error = "x", code });

    private static JObject Json(string body) => JObject.Parse(body);

    /// <summary>Value of one multipart form field in a captured request body (.NET writes <c>name=field</c> without quotes; quoted is accepted too).</summary>
    private static string? FormField(string body, string name)
    {
        var m = Regex.Match(body, "name=\"?" + Regex.Escape(name) + "\"?\\r\\n(?:[^\\r\\n]+\\r\\n)*\\r\\n([\\s\\S]*?)\\r\\n--");
        return m.Success ? m.Groups[1].Value : null;
    }

    private static UploadEnvelopeDocumentParams Upload(string? key, string title = "T") => new()
    {
        File = Pdf(),
        Title = title,
        IdempotencyKey = key!,
    };

    // --- path slots and bodies on the wire -----------------------------------

    [Fact]
    public async Task List_sends_demandId_in_the_path_and_view_as_a_query_parameter()
    {
        var srv = Server(Ok(new { documents = new[] { DocBody() } }));
        var result = await Client(srv).Demands.Documents.ListAsync(Demand, view: "wizard");
        Assert.Equal(Doc, result.Documents[0].Id);
        Assert.Equal("Kira sözleşmesi", result.Documents[0].Title);
        Assert.Single(srv.Calls);
        Assert.Equal("GET", srv.Calls[0].Method);
        Assert.Equal($"/api/v1/demands/{Demand}/documents?view=wizard", srv.Calls[0].Uri);
    }

    [Fact]
    public async Task List_without_view_sends_no_query_string()
    {
        var srv = Server(Ok(new { documents = Array.Empty<object>() }));
        await Client(srv).Demands.Documents.ListAsync(Demand);
        Assert.Equal($"/api/v1/demands/{Demand}/documents", srv.Calls[0].Uri);
    }

    [Fact]
    public async Task Create_posts_the_json_body()
    {
        var srv = Server(new Reply(201, new { success = true, data = new { document = DocBody() } }));
        var result = await Client(srv).Demands.Documents.CreateAsync(Demand, new ApiV1DemandsDemandIdDocumentsPostRequest(
            title: "KVKK aydınlatma",
            docKind: ApiV1DemandsDemandIdDocumentsPostRequest.DocKindEnum.KVKKNOTICE,
            isRequired: false,
            signatureRequired: false));
        Assert.Equal(Doc, result.Document.Id);
        var call = srv.Calls[0];
        Assert.Equal("POST", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/documents", call.Uri);
        var sent = Json(call.Body);
        Assert.Equal("KVKK aydınlatma", (string?)sent["title"]);
        Assert.Equal("KVKK_NOTICE", (string?)sent["doc_kind"]);
        Assert.False((bool?)sent["is_required"]);
        Assert.False((bool?)sent["signature_required"]);
    }

    [Fact]
    public async Task Update_keeps_demandId_and_docId_in_their_own_slots()
    {
        var srv = Server(Ok(new { document = DocBody() }));
        var result = await Client(srv).Demands.Documents.UpdateAsync(Demand, Doc,
            new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest(title: "Yeni başlık"));
        Assert.Equal(Doc, result.Document.Id);
        var call = srv.Calls[0];
        Assert.Equal("PATCH", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/documents/{Doc}", call.Uri);
        var body = Json(call.Body);
        Assert.Equal("Yeni başlık", (string?)body["title"]);
        // A title-only update must not carry false for the booleans: the
        // server reads null (or absence) as "unchanged".
        Assert.NotEqual(false, (bool?)body["is_required"]);
        Assert.NotEqual(false, (bool?)body["signature_required"]);
    }

    [Fact]
    public async Task Delete_keeps_demandId_and_docId_in_their_own_slots()
    {
        var srv = Server(Ok(new { id = Doc, deleted = true }));
        var result = await Client(srv).Demands.Documents.DeleteAsync(Demand, Doc);
        Assert.Equal(Doc, result.Id);
        Assert.True(result.Deleted);
        var call = srv.Calls[0];
        Assert.Equal("DELETE", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/documents/{Doc}", call.Uri);
    }

    [Fact]
    public async Task Reorder_sends_document_ids_in_the_given_order()
    {
        var srv = Server(Ok(new { documents = new[] { DocBody() } }));
        var result = await Client(srv).Demands.Documents.ReorderAsync(Demand, new[] { DocB, Doc });
        Assert.Single(result.Documents);
        var call = srv.Calls[0];
        Assert.Equal("PUT", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/documents/order", call.Uri);
        Assert.Equal(new[] { DocB.ToString(), Doc.ToString() }, Json(call.Body)["document_ids"]!.Select(t => (string?)t).ToArray());
    }

    [Fact]
    public async Task SetAssignments_keeps_demandId_and_docId_apart_and_sends_party_ids()
    {
        var srv = Server(Ok(new { document = DocBody() }));
        await Client(srv).Demands.Documents.SetAssignmentsAsync(Demand, Doc, new[] { Party });
        var call = srv.Calls[0];
        Assert.Equal("PUT", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/documents/{Doc}/assignments", call.Uri);
        Assert.Equal(new[] { Party.ToString() }, Json(call.Body)["party_ids"]!.Select(t => (string?)t).ToArray());
    }

    [Fact]
    public async Task Dispatch_sends_no_send_invitations_field_by_default()
    {
        var srv = Server(Ok(new { demand_id = Demand, status = "PENDING", dispatched = true }));
        var result = await Client(srv).Demands.DispatchAsync(Demand);
        Assert.Equal(Demand, result.DemandId);
        Assert.True(result.Dispatched);
        var call = srv.Calls[0];
        Assert.Equal("POST", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/dispatch", call.Uri);
        var sent = call.Body.Length == 0 ? new JObject() : Json(call.Body);
        Assert.Null(sent["send_invitations"]);
    }

    [Fact]
    public async Task Dispatch_forwards_a_boolean_or_a_string_sendInvitations_value_unchanged()
    {
        var srv = Server(Ok(new { dispatched = true }));
        await Client(srv).Demands.DispatchAsync(Demand, sendInvitations: false);
        await Client(srv).Demands.DispatchAsync(Demand, sendInvitations: "email");
        await Client(srv).Demands.DispatchAsync(Demand, sendInvitations: true);
        Assert.Equal(JTokenType.Boolean, Json(srv.Calls[0].Body)["send_invitations"]!.Type);
        Assert.False((bool)Json(srv.Calls[0].Body)["send_invitations"]!);
        Assert.Equal("email", (string?)Json(srv.Calls[1].Body)["send_invitations"]);
        Assert.True((bool)Json(srv.Calls[2].Body)["send_invitations"]!);
    }

    // --- upload ----------------------------------------------------------------

    [Fact]
    public async Task Upload_sends_the_idempotency_key_as_the_idempotency_key_body_field_not_as_a_header()
    {
        var srv = Server(Ok(new { document = DocBody() }));
        var result = await Client(srv).Demands.Documents.UploadAsync(Demand, new UploadEnvelopeDocumentParams
        {
            File = Pdf(),
            Title = "Kira sozlesmesi",
            IdempotencyKey = "siparis-42",
            DocKind = "CONTRACT",
            IsRequired = false,
        });
        Assert.Equal(Doc, result.Document.Id);
        var call = srv.Calls[0];
        Assert.Equal("POST", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/documents/upload", call.Uri);
        Assert.False(call.Headers.ContainsKey("Idempotency-Key"));
        Assert.StartsWith("multipart/form-data", call.Headers["Content-Type"]);
        Assert.Equal("siparis-42", FormField(call.RawBody, "idempotency_key"));
        Assert.Equal("Kira sozlesmesi", FormField(call.RawBody, "title"));
        Assert.Equal("CONTRACT", FormField(call.RawBody, "doc_kind"));
        Assert.Equal("false", FormField(call.RawBody, "is_required"));
        Assert.Matches("filename=\"?kira.pdf\"?", call.RawBody);
        Assert.Contains("%PDF-1.7 test", call.RawBody);
    }

    [Fact]
    public async Task Upload_omits_doc_kind_and_is_required_when_not_given()
    {
        var srv = Server(Ok(new { document = DocBody() }));
        await Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1"));
        Assert.Null(FormField(srv.Calls[0].RawBody, "doc_kind"));
        Assert.Null(FormField(srv.Calls[0].RawBody, "is_required"));
    }

    [Theory]
    [InlineData(null)]
    [InlineData("")]
    [InlineData("sipariş-1")]
    [InlineData("a\r\nX-Evil: 1")]
    public async Task Upload_rejects_a_bad_idempotency_key_locally_without_sending_anything(string? key)
    {
        var srv = Server(Ok(new { document = DocBody() }));
        var err = await Assert.ThrowsAsync<ImzalaValidationError>(() => Client(srv).Demands.Documents.UploadAsync(Demand, Upload(key)));
        Assert.Null(err.StatusCode);
        Assert.Empty(srv.Calls);
    }

    [Fact]
    public async Task Upload_retries_once_after_a_429_and_sends_the_file_again()
    {
        var srv = Server(RateLimited(), Ok(new { document = DocBody() }));
        var result = await Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1"));
        Assert.Equal(Doc, result.Document.Id);
        Assert.Equal(2, srv.Calls.Count);
        Assert.Contains("%PDF-1.7 test", srv.Calls[1].RawBody);
        Assert.Equal("k-1", FormField(srv.Calls[1].RawBody, "idempotency_key"));
    }

    [Fact]
    public async Task Upload_does_not_retry_when_RetryAfter_exceeds_the_cap()
    {
        var srv = Server(RateLimited("61"));
        var err = await Assert.ThrowsAsync<ImzalaRateLimitError>(() => Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1")));
        Assert.Equal(429, err.StatusCode);
        Assert.Single(srv.Calls);
    }

    [Fact]
    public async Task Upload_returns_the_earlier_document_on_a_409_IDEMPOTENT_REPLAY_instead_of_throwing()
    {
        var srv = Server(Conflict("IDEMPOTENT_REPLAY", withDocument: true));
        var result = await Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1"));
        Assert.NotNull(result.Document);
        Assert.Equal(Doc, result.Document.Id);
        Assert.Equal("Kira sözleşmesi", result.Document.Title);
        Assert.Single(srv.Calls);
    }

    [Fact]
    public async Task Upload_treats_a_replay_that_follows_a_429_retry_as_success_too()
    {
        var srv = Server(RateLimited(), Conflict("IDEMPOTENT_REPLAY", withDocument: true));
        var result = await Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1"));
        Assert.Equal(Doc, result.Document.Id);
        Assert.Equal(2, srv.Calls.Count);
    }

    [Fact]
    public async Task Upload_throws_a_409_replay_without_a_document()
    {
        var srv = Server(Conflict("IDEMPOTENT_REPLAY"));
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1")));
        Assert.Equal("IDEMPOTENT_REPLAY", err.Code);
    }

    [Fact]
    public async Task Upload_throws_any_other_409_code_for_example_SIGNING_ALREADY_STARTED()
    {
        var srv = Server(Conflict("SIGNING_ALREADY_STARTED", withDocument: true));
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1")));
        Assert.Equal("SIGNING_ALREADY_STARTED", err.Code);
        Assert.Equal(409, err.StatusCode);
    }

    [Fact]
    public async Task Upload_throws_ENVELOPE_MULTI_DOC_DISABLED()
    {
        var srv = Server(Disabled());
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.Documents.UploadAsync(Demand, Upload("k-1")));
        Assert.Equal("ENVELOPE_MULTI_DOC_DISABLED", err.Code);
    }

    // --- feature switched off --------------------------------------------------

    [Fact]
    public async Task List_throws_ENVELOPE_MULTI_DOC_DISABLED_and_does_not_return_an_empty_list()
    {
        var srv = Server(Disabled());
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.Documents.ListAsync(Demand));
        Assert.Equal("ENVELOPE_MULTI_DOC_DISABLED", err.Code);
        Assert.Equal(409, err.StatusCode);
        Assert.Single(srv.Calls);
    }

    private static Func<Imzala, Task> UnkeyedWrite(string name) => name switch
    {
        "create" => c => c.Demands.Documents.CreateAsync(Demand, new ApiV1DemandsDemandIdDocumentsPostRequest(title: "T")),
        "update" => c => c.Demands.Documents.UpdateAsync(Demand, Doc, new ApiV1DemandsDemandIdDocumentsDocIdPatchRequest(title: "T")),
        "delete" => c => c.Demands.Documents.DeleteAsync(Demand, Doc),
        "reorder" => c => c.Demands.Documents.ReorderAsync(Demand, new[] { Doc }),
        "setAssignments" => c => c.Demands.Documents.SetAssignmentsAsync(Demand, Doc, new[] { Party }),
        "dispatch" => c => c.Demands.DispatchAsync(Demand),
        _ => throw new ArgumentOutOfRangeException(nameof(name)),
    };

    [Theory]
    [InlineData("create")]
    [InlineData("update")]
    [InlineData("delete")]
    [InlineData("reorder")]
    [InlineData("setAssignments")]
    public async Task Document_writes_throw_ENVELOPE_MULTI_DOC_DISABLED(string name)
    {
        var srv = Server(Disabled());
        var err = await Assert.ThrowsAsync<ImzalaError>(() => UnkeyedWrite(name)(Client(srv)));
        Assert.Equal("ENVELOPE_MULTI_DOC_DISABLED", err.Code);
    }

    // --- writes without an idempotency key are never retried ------------------

    [Theory]
    [InlineData("create")]
    [InlineData("update")]
    [InlineData("delete")]
    [InlineData("reorder")]
    [InlineData("setAssignments")]
    [InlineData("dispatch")]
    public async Task A_429_is_thrown_after_exactly_one_request(string name)
    {
        var srv = Server(RateLimited(), Ok(new { }));
        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => UnkeyedWrite(name)(Client(srv)));
        Assert.Single(srv.Calls);
    }

    [Fact]
    public async Task List_is_retried_after_a_429()
    {
        var srv = Server(RateLimited(), Ok(new { documents = Array.Empty<object>() }));
        var result = await Client(srv).Demands.Documents.ListAsync(Demand);
        Assert.Empty(result.Documents);
        Assert.Equal(2, srv.Calls.Count);
    }

    // --- dispatch errors -------------------------------------------------------

    [Theory]
    [InlineData("DISPATCH_NO_PARTIES")]
    [InlineData("DISPATCH_TOO_MANY")]
    [InlineData("QES_NOT_SUPPORTED_MULTI_DOCUMENT")]
    public async Task Dispatch_throws_the_server_code(string code)
    {
        var srv = Server(Conflict(code));
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.DispatchAsync(Demand));
        Assert.Equal(code, err.Code);
        Assert.Equal(409, err.StatusCode);
        Assert.Single(srv.Calls);
    }

    // POST /api/v1/timestamps also declares a JSON (base64) body. The C#
    // generator preferred application/json for such operations and sent an
    // empty body; generate.sh narrows the list to multipart. Real client here.
    [Fact]
    public async Task Timestamps_create_sends_the_file_as_multipart()
    {
        var srv = Server(Ok(new { id = Guid.NewGuid(), file_sha256 = "abc" }));
        await Client(srv).Timestamps.CreateAsync(new CreateTimestampParams
        {
            Content = Encoding.ASCII.GetBytes("%PDF-1.7 test"),
            FileName = "eser.pdf",
            ContentType = "application/pdf",
            IdempotencyKey = "damga-1",
        });
        var call = srv.Calls[0];
        Assert.Equal("/api/v1/timestamps", call.Uri);
        Assert.StartsWith("multipart/form-data", call.Headers["Content-Type"]);
        Assert.Matches("name=\"?file\"?", call.RawBody);
        Assert.Matches("filename=\"?eser.pdf\"?", call.RawBody);
        Assert.Contains("%PDF-1.7 test", call.RawBody);
        Assert.Equal("damga-1", call.Headers["Idempotency-Key"]);
    }
}
