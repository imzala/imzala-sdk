using System.Globalization;
using ImzalaApiClient.Model;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;

namespace ImzalaSdk;

/// <summary>
/// Partial update for <see cref="DemandsResource.UpdateTermAsync"/>
/// (<c>PATCH /api/v1/demands/{id}/term</c>). A property left <c>null</c> is
/// not sent, so the server keeps its current value. To clear a value on the
/// server, add its wire name (for example <c>"notice_days"</c>) to
/// <see cref="Clear"/>; it is then sent as <c>null</c>.
///
/// The generated <see cref="ContractTermInput"/> is not used directly because
/// it always writes every property: unset ones as <c>null</c> (which clears
/// them on this endpoint) and <c>notify_counterparty</c> as <c>false</c>.
/// </summary>
public sealed class ContractTermUpdate
{
    /// <summary>Wire names that can be cleared through <see cref="Clear"/>.</summary>
    public static readonly IReadOnlyCollection<string> ClearableFields = new[]
    {
        "term_start_mode", "term_start_date", "term_duration_months", "term_fixed_end_date",
        "renewal_type", "renewal_period_months", "notice_days", "reminder_offsets",
    };

    /// <summary><c>term_start_mode</c>: when tracking starts.</summary>
    public ContractTermInput.TermStartModeEnum? TermStartMode { get; set; }

    /// <summary><c>term_start_date</c>: used only with <c>FIXED_DATE</c>.</summary>
    public DateOnly? TermStartDate { get; set; }

    /// <summary><c>term_duration_months</c> (1 to 600). Cannot be combined with <see cref="TermFixedEndDate"/>.</summary>
    public int? TermDurationMonths { get; set; }

    /// <summary><c>term_fixed_end_date</c>. Cannot be combined with <see cref="TermDurationMonths"/>.</summary>
    public DateOnly? TermFixedEndDate { get; set; }

    /// <summary><c>renewal_type</c>.</summary>
    public ContractTermInput.RenewalTypeEnum? RenewalType { get; set; }

    /// <summary><c>renewal_period_months</c> (1 to 600).</summary>
    public int? RenewalPeriodMonths { get; set; }

    /// <summary><c>notice_days</c>: notice period in days before the end date (0 to 3650).</summary>
    public int? NoticeDays { get; set; }

    /// <summary><c>reminder_offsets</c>: days before the end date to send reminders (at most 5).</summary>
    public List<int>? ReminderOffsets { get; set; }

    /// <summary><c>notify_counterparty</c>: also remind the counterparty. <c>null</c> keeps the current value.</summary>
    public bool? NotifyCounterparty { get; set; }

    /// <summary>Wire names from <see cref="ClearableFields"/> to send as <c>null</c> (clears the value on the server).</summary>
    public ISet<string> Clear { get; } = new HashSet<string>(StringComparer.Ordinal);

    internal JObject ToWire()
    {
        var json = new JObject();
        void Put(string name, object? value, Func<object, JToken> convert)
        {
            if (value != null)
            {
                json[name] = convert(value);
            }
        }

        Put("term_start_mode", TermStartMode, v => JToken.FromObject(v));
        Put("term_start_date", TermStartDate, v => ((DateOnly)v).ToString("yyyy-MM-dd", CultureInfo.InvariantCulture));
        Put("term_duration_months", TermDurationMonths, v => (int)v);
        Put("term_fixed_end_date", TermFixedEndDate, v => ((DateOnly)v).ToString("yyyy-MM-dd", CultureInfo.InvariantCulture));
        Put("renewal_type", RenewalType, v => JToken.FromObject(v));
        Put("renewal_period_months", RenewalPeriodMonths, v => (int)v);
        Put("notice_days", NoticeDays, v => (int)v);
        Put("reminder_offsets", ReminderOffsets, v => new JArray(((List<int>)v).Cast<object>().ToArray()));
        Put("notify_counterparty", NotifyCounterparty, v => (bool)v);

        foreach (var name in Clear)
        {
            if (!ClearableFields.Contains(name))
            {
                throw new ArgumentException($"\"{name}\" cannot be cleared; use one of: {string.Join(", ", ClearableFields)}.", nameof(Clear));
            }
            if (json.ContainsKey(name))
            {
                throw new ArgumentException($"\"{name}\" is both set and listed in Clear.", nameof(Clear));
            }
            json[name] = JValue.CreateNull();
        }

        if (json.Count == 0)
        {
            throw new ArgumentException("Set at least one property or list a field in Clear.");
        }
        return json;
    }
}

/// <summary>Wire body for <c>PATCH /api/v1/demands/{id}/term</c>: writes exactly the keys of a <see cref="ContractTermUpdate"/>.</summary>
[JsonConverter(typeof(TermPatchBodyConverter))]
internal sealed class TermPatchBody : ContractTermInput
{
    public TermPatchBody(ContractTermUpdate update)
    {
        Wire = (update ?? throw new ArgumentNullException(nameof(update))).ToWire();
    }

    internal JObject Wire { get; }
}

internal sealed class TermPatchBodyConverter : JsonConverter
{
    public override bool CanRead => false;

    public override bool CanConvert(Type objectType) => objectType == typeof(TermPatchBody);

    public override void WriteJson(JsonWriter writer, object? value, JsonSerializer serializer) =>
        ((TermPatchBody)value!).Wire.WriteTo(writer);

    public override object ReadJson(JsonReader reader, Type objectType, object? existingValue, JsonSerializer serializer) =>
        throw new NotSupportedException();
}
