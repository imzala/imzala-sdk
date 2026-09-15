using ImzalaSdk;

namespace ImzalaOrnekleri;

internal interface ISenaryo
{
    string Ad { get; }

    /// <summary>true ise istemci GET isteklerini 429 sonrası yinelemeden kurulur.</summary>
    bool YinelemeKapali => false;

    Task CalistirAsync(Imzala imzala);
}

internal static class Ortam
{
    public static string? Oku(string ad)
    {
        var deger = Environment.GetEnvironmentVariable(ad);
        return string.IsNullOrEmpty(deger) ? null : deger;
    }
}
