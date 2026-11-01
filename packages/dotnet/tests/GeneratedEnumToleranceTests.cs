using ImzalaApiClient.Model;
using Newtonsoft.Json;
using Xunit;

namespace Imzala.Tests;

// Response enums grow with the API (item_type gained contract_number in
// 1.14.5 and stamp in 1.14.6; status, decision, doc_kind, error codes and
// webhook types grow the same way). Since SDK 1.2.0 every enum the server can
// return is an open string: a value the client was generated without must not
// break deserialization.
public class GeneratedEnumToleranceTests
{
    private const string Demand = "{\"id\":\"00000000-0000-4000-8000-000000000001\",\"title\":\"Ornek\","
        + "\"status\":\"future_value\",\"parties\":[{\"party_id\":\"00000000-0000-4000-8000-000000000002\","
        + "\"locale\":\"future_value\",\"signed_locale\":\"future_value\"}],"
        + "\"stamp_items\":[{\"item_id\":1,\"source\":\"future_value\"}],"
        + "\"documents\":[{\"id\":\"00000000-0000-4000-8000-000000000003\",\"doc_kind\":\"future_value\","
        + "\"progress\":{\"status\":\"future_value\"},\"sealing\":{\"status\":\"future_value\"},"
        + "\"decisions\":[{\"party_id\":\"00000000-0000-4000-8000-000000000002\",\"decision\":\"future_value\"}]}]}";

    [Theory]
    [InlineData("contract_number")]
    [InlineData("stamp")]
    [InlineData("future_value")]
    public void PageItemKeepsItemType(string value)
    {
        var body = "{\"page_id\":1,\"item_type\":\"" + value
            + "\",\"position_x\":0.1,\"position_y\":0.2,\"width\":0.3,\"height\":0.1}";
        Assert.Equal(value, JsonConvert.DeserializeObject<PageItem>(body)!.ItemType);
    }

    [Theory]
    [InlineData("stamp")]
    [InlineData("future_value")]
    public void TemplateVariableKeepsItemType(string value)
    {
        var body = "{\"slug\":\"a\",\"item_type\":\"" + value + "\"}";
        Assert.Equal(value, JsonConvert.DeserializeObject<TemplateVariable>(body)!.ItemType);
    }

    [Fact]
    public void DemandStatusKeepsUnknownValues()
    {
        var d = JsonConvert.DeserializeObject<DemandStatus>(Demand)!;
        Assert.Equal("future_value", d.Status);
        Assert.Equal("future_value", d.Parties[0].Locale);
        Assert.Equal("future_value", d.Parties[0].SignedLocale);
        Assert.Equal("future_value", d.StampItems[0].Source);
        var doc = d.Documents[0];
        Assert.Equal("future_value", doc.DocKind);
        Assert.Equal("future_value", doc.Progress.Status);
        Assert.Equal("future_value", doc.Sealing.Status);
        Assert.Equal("future_value", doc.Decisions[0].Decision);
    }

    [Fact]
    public void WebhookTypeAndErrorCodeKeepUnknownValues()
    {
        var evt = JsonConvert.DeserializeObject<WebhookEnvelope>("{\"id\":\"evt_0123456789abcdef0123456789abcdef\","
            + "\"type\":\"future_value\",\"created_at\":\"2026-11-01T00:00:00Z\",\"data\":{}}")!;
        Assert.Equal("future_value", evt.Type);
        var error = JsonConvert.DeserializeObject<StandardError>(
            "{\"success\":false,\"error\":{\"code\":\"future_value\",\"message\":\"Ornek\"}}")!;
        Assert.Equal("future_value", error.Error.Code);
    }
}
