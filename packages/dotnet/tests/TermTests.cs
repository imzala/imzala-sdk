using System.Net;
using System.Text;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// Contract term tracking and archive helpers, checked on the wire against a
/// local HTTP server: path slots, the partial-update body (only set properties
/// and explicitly cleared fields are sent), the archive filter on list, and
/// that none of these writes is retried. Mirrors
/// packages/node/src/__tests__/term.test.ts.
/// </summary>
public class TermTests : IDisposable
{
    private static readonly Guid Demand = Guid.Parse("11111111-1111-4111-8111-111111111111");

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

    private static Reply Ok(object data) => new(200, new { success = true, data });

    private static Reply Error(int status, string code) => new(status, new { success = false, error = "x", code });

    // Every ContractTerm key is required in the response; unknown values are null.
    private static object Term() => new
    {
        start_mode = "ON_COMPLETION",
        start_date = (string?)null,
        duration_months = 12,
        fixed_end_date = (string?)null,
        end_date = (string?)null,
        end_date_signed = (string?)null,
        renewal_type = "AUTO_RENEW",
        renewal_period_months = 12,
        notice_days = 30,
        notice_deadline = (string?)null,
        reminder_offsets = new[] { 30, 7 },
        notify_counterparty = false,
        state = "UNTRACKED",
        days_left = (int?)null,
        renewal_stopped_at = (string?)null,
    };

    [Fact]
    public async Task UpdateTerm_sends_only_set_properties_and_cleared_fields()
    {
        var srv = Server(Ok(new { term = Term() }));
        var update = new ContractTermUpdate
        {
            TermStartMode = ContractTermInput.TermStartModeEnum.ONCOMPLETION,
            TermDurationMonths = 12,
            RenewalType = ContractTermInput.RenewalTypeEnum.AUTORENEW,
        };
        update.Clear.Add("notice_days");
        var result = await Client(srv).Demands.UpdateTermAsync(Demand, update);
        Assert.Equal(ContractTerm.RenewalTypeEnum.AUTORENEW, result.Term.RenewalType);
        var call = srv.Calls[0];
        Assert.Equal("PATCH", call.Method);
        Assert.Equal($"/api/v1/demands/{Demand}/term", call.Uri);
        var body = JObject.Parse(call.Body);
        Assert.Equal("ON_COMPLETION", (string?)body["term_start_mode"]);
        Assert.Equal(12, (int?)body["term_duration_months"]);
        Assert.Equal("AUTO_RENEW", (string?)body["renewal_type"]);
        Assert.Equal(JTokenType.Null, body["notice_days"]!.Type);
        // notify_counterparty and every other unset field stay off the wire.
        Assert.Equal(4, body.Count);
    }

    [Fact]
    public async Task UpdateTerm_writes_dates_as_iso_and_notify_counterparty_only_when_set()
    {
        var srv = Server(Ok(new { term = Term() }), Ok(new { term = Term() }));
        await Client(srv).Demands.UpdateTermAsync(Demand, new ContractTermUpdate { TermFixedEndDate = new DateOnly(2027, 1, 31) });
        await Client(srv).Demands.UpdateTermAsync(Demand, new ContractTermUpdate { NotifyCounterparty = false });
        Assert.Equal(@"{""term_fixed_end_date"":""2027-01-31""}", srv.Calls[0].Body);
        Assert.Equal(@"{""notify_counterparty"":false}", srv.Calls[1].Body);
    }

    [Theory]
    [InlineData("notify_counterparty")]
    [InlineData("unknown_field")]
    public async Task UpdateTerm_rejects_fields_that_cannot_be_cleared(string name)
    {
        var srv = Server(Ok(new { term = Term() }));
        var update = new ContractTermUpdate();
        update.Clear.Add(name);
        await Assert.ThrowsAsync<ArgumentException>(() => Client(srv).Demands.UpdateTermAsync(Demand, update));
        Assert.Empty(srv.Calls);
    }

    [Fact]
    public async Task UpdateTerm_rejects_an_empty_update_and_a_field_both_set_and_cleared()
    {
        var srv = Server(Ok(new { term = Term() }));
        await Assert.ThrowsAsync<ArgumentException>(() => Client(srv).Demands.UpdateTermAsync(Demand, new ContractTermUpdate()));
        var both = new ContractTermUpdate { NoticeDays = 30 };
        both.Clear.Add("notice_days");
        await Assert.ThrowsAsync<ArgumentException>(() => Client(srv).Demands.UpdateTermAsync(Demand, both));
        Assert.Empty(srv.Calls);
    }

    [Fact]
    public async Task UpdateTerm_throws_TERM_INVALID_with_the_rejected_field()
    {
        var srv = Server(new Reply(400, new { success = false, error = "x", code = "TERM_INVALID", field = "term_fixed_end_date" }));
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.UpdateTermAsync(Demand,
            new ContractTermUpdate { TermFixedEndDate = new DateOnly(2027, 1, 31), TermDurationMonths = 12 }));
        Assert.Equal("TERM_INVALID", err.Code);
        Assert.Equal("term_fixed_end_date", (string?)JObject.Parse(err.Body!)["field"]);
        Assert.Single(srv.Calls);
    }

    [Fact]
    public void CreateDemandRequest_leaves_unset_term_fields_off_the_wire()
    {
        // A missing key lets the server take the template's term policy; a
        // null or false written for every unset field would override it.
        var plain = JObject.Parse(new CreateDemandRequest(templateId: Demand, partyMapping: new List<PartyMappingInput>()).ToJson());
        foreach (var key in new[] { "term_start_mode", "term_start_date", "term_duration_months", "term_fixed_end_date", "renewal_type", "renewal_period_months", "notice_days", "reminder_offsets", "notify_counterparty" })
        {
            Assert.False(plain.ContainsKey(key), key);
        }

        var set = JObject.Parse(new CreateDemandRequest(templateId: Demand, partyMapping: new List<PartyMappingInput>())
        {
            TermDurationMonths = 12,
            NotifyCounterparty = true,
        }.ToJson());
        Assert.Equal(12, (int?)set["term_duration_months"]);
        Assert.True((bool?)set["notify_counterparty"]);
    }

    [Fact]
    public async Task Archive_posts_to_the_archive_path()
    {
        var srv = Server(Ok(new { archived_at = "2026-09-28T09:00:00.000Z" }));
        var result = await Client(srv).Demands.ArchiveAsync(Demand);
        Assert.NotEqual(default, result.ArchivedAt);
        Assert.Equal("POST", srv.Calls[0].Method);
        Assert.Equal($"/api/v1/demands/{Demand}/archive", srv.Calls[0].Uri);
    }

    [Fact]
    public async Task Unarchive_posts_to_the_unarchive_path()
    {
        var srv = Server(Ok(new { archived_at = (string?)null }));
        await Client(srv).Demands.UnarchiveAsync(Demand);
        Assert.Equal("POST", srv.Calls[0].Method);
        Assert.Equal($"/api/v1/demands/{Demand}/unarchive", srv.Calls[0].Uri);
    }

    [Theory]
    [InlineData("DEMAND_NOT_ARCHIVABLE")]
    [InlineData("DEMAND_REJECTED_CANCEL_FIRST")]
    public async Task Archive_throws(string code)
    {
        var srv = Server(Error(409, code));
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.ArchiveAsync(Demand));
        Assert.Equal(code, err.Code);
        Assert.Single(srv.Calls);
    }

    [Fact]
    public async Task Delete_of_an_archived_demand_throws_DEMAND_ARCHIVED()
    {
        var srv = Server(Error(409, "DEMAND_ARCHIVED"));
        var err = await Assert.ThrowsAsync<ImzalaError>(() => Client(srv).Demands.DeleteAsync(Demand));
        Assert.Equal("DEMAND_ARCHIVED", err.Code);
    }

    [Fact]
    public async Task List_sends_the_archive_filter_only_through_the_new_overload()
    {
        var empty = Ok(new { demands = Array.Empty<object>(), total = 0, page = 1, limit = 20 });
        var srv = Server(empty, empty);
        await Client(srv).Demands.ListAsync(archived: DemandArchiveFilter.Exclude);
        await Client(srv).Demands.ListAsync();
        Assert.Contains("archived=exclude", srv.Calls[0].Uri);
        Assert.DoesNotContain("archived=", srv.Calls[1].Uri);
    }

    public static IEnumerable<object[]> Writes() => new[]
    {
        new object[] { "UpdateTerm" },
        new object[] { "Archive" },
        new object[] { "Unarchive" },
    };

    [Theory]
    [MemberData(nameof(Writes))]
    public async Task Rate_limited_writes_are_not_retried(string name)
    {
        var srv = Server(
            new Reply(429, new { success = false, error = "Çok fazla istek", code = "RATE_LIMIT_EXCEEDED" }, new Dictionary<string, string> { ["Retry-After"] = "0" }),
            Ok(new { }));
        var client = Client(srv);
        Func<Task> call = name switch
        {
            "UpdateTerm" => () => client.Demands.UpdateTermAsync(Demand, new ContractTermUpdate { NoticeDays = 30 }),
            "Archive" => () => client.Demands.ArchiveAsync(Demand),
            _ => () => client.Demands.UnarchiveAsync(Demand),
        };
        await Assert.ThrowsAsync<ImzalaRateLimitError>(call);
        Assert.Single(srv.Calls);
    }
}
