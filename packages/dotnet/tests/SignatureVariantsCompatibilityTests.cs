using ImzalaApiClient.Model;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using Xunit;

namespace ImzalaSdk.Tests;

public class SignatureVariantsCompatibilityTests
{
    [Fact]
    public void Existing_positional_constructors_and_binary_signatures_are_preserved()
    {
        var single = new CreateDemandRequest(Guid.NewGuid(), "title", null, null, false,
            new List<PartyMappingInput>(), null, null, false, false, false, true);
        var bulk = new ApiV1DemandsBulkPostRequestOptions(false, false, null, "tr", false, null, false, true, false);
        Assert.False(single.DispatchNotifications);
        Assert.True(bulk.RequireIdPhoto);
        Assert.Contains(typeof(CreateDemandRequest).GetConstructors(), c => c.GetParameters().Length == 19);
        Assert.Contains(typeof(ApiV1DemandsBulkPostRequestOptions).GetConstructors(), c => c.GetParameters().Length == 9);
        single.AllowedSignatureVariants = new() { CreateDemandRequest.AllowedSignatureVariantsEnum.Type, CreateDemandRequest.AllowedSignatureVariantsEnum.Draw };
        bulk.AllowedSignatureVariants = new() { ApiV1DemandsBulkPostRequestOptions.AllowedSignatureVariantsEnum.Upload, ApiV1DemandsBulkPostRequestOptions.AllowedSignatureVariantsEnum.Draw };
        Assert.Equal(new[] { "type", "draw" }, JObject.Parse(JsonConvert.SerializeObject(single))["allowed_signature_variants"]!.Values<string>());
        Assert.Equal(new[] { "upload", "draw" }, JObject.Parse(JsonConvert.SerializeObject(bulk))["allowed_signature_variants"]!.Values<string>());
        var roundTrip = JsonConvert.DeserializeObject<CreateDemandRequest>(JsonConvert.SerializeObject(single));
        Assert.Equal(single.AllowedSignatureVariants, roundTrip!.AllowedSignatureVariants);
    }
}
