using ImzalaApiClient.Model;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace ImzalaSdk;

/// <summary>
/// Wire body for <c>PATCH /api/v1/demands/{id}/items/{itemId}/stamp</c>.
///
/// The generated <see cref="StampData"/> marks every field
/// <c>EmitDefaultValue = true</c>, so serialising it as-is sends all twelve
/// fields, unset ones as <c>null</c>. On this endpoint <c>null</c> means
/// "remove this field", so a one-field update would wipe the rest of the
/// stamp. This wrapper sends only the fields that are set (non-null); an empty
/// string still reaches the server and removes that field.
/// </summary>
[JsonConverter(typeof(StampPatchBodyConverter))]
internal sealed class StampPatchBody : PatchStampItemRequest
{
    public StampPatchBody(PatchStampItemRequest source)
        : base(source.StampData ?? throw new ArgumentNullException(nameof(source), "StampData is required."), source.DocumentId)
    {
    }
}

internal sealed class StampPatchBodyConverter : JsonConverter
{
    public override bool CanRead => false;

    public override bool CanConvert(Type objectType) => objectType == typeof(StampPatchBody);

    public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer)
    {
        var body = (StampPatchBody)value!;
        var stamp = JObject.FromObject(body.StampData, JsonSerializer.CreateDefault());
        foreach (var unset in stamp.Properties().Where(p => p.Value.Type == JTokenType.Null).ToList())
        {
            unset.Remove();
        }
        var json = new JObject { ["stamp_data"] = stamp };
        if (body.DocumentId != null)
        {
            json["document_id"] = body.DocumentId;
        }
        json.WriteTo(writer);
    }

    public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer) =>
        throw new NotSupportedException();
}
