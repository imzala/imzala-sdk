using ImzalaApiClient.Model;
using Xunit;

namespace Imzala.Tests;

public class GeneratedErrorCompatibilityTests
{
    [Fact]
    public void RetainsCodedBranchConstructorAndGetters()
    {
        var coded = new CodedError(success: false, error: "Synthetic validation", code: "FUTURE_COLLECTION_ERROR");
        var wrapper = new ApiV1DemandsPost400Response(coded);
        Assert.Same(coded, wrapper.ActualInstance);
        Assert.Same(coded, wrapper.GetCodedError());
        Assert.Contains("FUTURE_COLLECTION_ERROR", wrapper.ToJson());
    }

    [Fact]
    public void RetainsSelectionBranchConstructorAndGetters()
    {
        var selection = new DocumentSelectionError(success: false, error: "Synthetic selection", code: DocumentSelectionError.CodeEnum.INVALIDDOCUMENTSELECTION);
        var wrapper = new ApiV1DemandsPost400Response(selection);
        Assert.Same(selection, wrapper.ActualInstance);
        Assert.Same(selection, wrapper.GetDocumentSelectionError());
        Assert.Contains("INVALID_DOCUMENT_SELECTION", wrapper.ToJson());
    }
}
