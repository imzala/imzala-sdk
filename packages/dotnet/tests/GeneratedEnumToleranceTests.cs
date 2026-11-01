using ImzalaApiClient.Model;
using Xunit;

namespace Imzala.Tests;

// item_type grows with the API (contract_number in 1.14.5, stamp in 1.14.6).
// A value the client was generated without must not break deserialization.
public class GeneratedEnumToleranceTests
{
    [Theory]
    [InlineData("contract_number")]
    [InlineData("stamp")]
    [InlineData("future_type")]
    public void PageItemKeepsItemType(string value)
    {
        var body = "{\"page_id\":1,\"item_type\":\"" + value
            + "\",\"position_x\":0.1,\"position_y\":0.2,\"width\":0.3,\"height\":0.1}";
        Assert.Equal(value, Newtonsoft.Json.JsonConvert.DeserializeObject<PageItem>(body)!.ItemType);
    }

    [Theory]
    [InlineData("stamp")]
    [InlineData("future_type")]
    public void TemplateVariableKeepsItemType(string value)
    {
        var body = "{\"slug\":\"a\",\"item_type\":\"" + value + "\"}";
        Assert.Equal(value, Newtonsoft.Json.JsonConvert.DeserializeObject<TemplateVariable>(body)!.ItemType);
    }
}
