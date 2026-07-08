using ImzalaSdk;
using Xunit;

namespace ImzalaSdk.Tests;

/// <summary>
/// Uçtan uca (e2e) testler — GERÇEK İmzala API'sine karşı çalışır.
///
/// Varsayılan olarak ATLANIR. Çalıştırmak için ortam değişkenleri:
///   IMZALA_E2E=1
///   IMZALA_API_KEY=imz_...
///   IMZALA_BASE_URL=https://test-api.imzala.org   (opsiyonel; varsayılan prod)
///
///   IMZALA_E2E=1 IMZALA_API_KEY=imz_... IMZALA_BASE_URL=https://test-api.imzala.org \
///     dotnet test --filter "FullyQualifiedName~E2eTests"
///
/// Yalnızca SALT-OKUMA uçları çağrılır (kredi harcamaz, veri değiştirmez):
/// MeAsync / Templates.ListAsync / Demands.ListAsync / Demands.GetAsync /
/// GetTimelineAsync + geçersiz-id hata yolu. Böylece herhangi bir gerçek
/// hesaba karşı güvenle koşturulabilir. Mirrors the Node SDK's
/// <c>src/__tests__/e2e.test.ts</c> (B1).
/// </summary>
public class E2eTests
{
    private readonly Imzala _imzala;

    public E2eTests()
    {
        // Bu kurucu yalnızca EN AZ BİR test çalışacaksa (yani e2e etkinken)
        // çağrılır; tüm testler [E2eFact] ile atlandığında hiç çalışmaz. Bu
        // yüzden ApiKey burada güvenle non-null kabul edilir (Node'un ENABLED
        // guard'lı client kurulumunun karşılığı).
        _imzala = string.IsNullOrEmpty(E2eEnvironment.BaseUrl)
            ? new Imzala(E2eEnvironment.ApiKey!)
            : new Imzala(E2eEnvironment.ApiKey!, E2eEnvironment.BaseUrl!);
    }

    [E2eFact]
    public async Task MeAsync_returns_owner_and_credits()
    {
        var me = await _imzala.MeAsync();

        Assert.NotNull(me);
        // e-posta ya da id alanlarından biri dolu olmalı
        Assert.True(!string.IsNullOrEmpty(me.Email) || me.Id != Guid.Empty);
    }

    [E2eFact]
    public async Task TemplatesListAsync_unwraps_the_envelope()
    {
        var res = await _imzala.Templates.ListAsync(limit: 3);

        Assert.NotNull(res.Templates);
        Assert.True(res.Total >= 0);
    }

    [E2eFact]
    public async Task DemandsListAsync_is_counts_only_and_leaks_no_party_PII()
    {
        var res = await _imzala.Demands.ListAsync(limit: 3);

        Assert.NotNull(res.Demands);
        // counts-only liste ham e-posta içermemeli (PII maskeleme kanıtı)
        var json = res.ToJson();
        Assert.DoesNotMatch(@"(?i)@[a-z0-9.-]+\.[a-z]{2,}", json);
    }

    [E2eFact]
    public async Task DemandGetAsync_and_GetTimelineAsync_when_a_demand_exists()
    {
        var list = await _imzala.Demands.ListAsync(limit: 1);
        var first = (list.Demands ?? new()).FirstOrDefault();
        if (first is null)
        {
            return; // sözleşme yoksa atla (assertion yok, kredi harcamaz)
        }

        var demand = await _imzala.Demands.GetAsync(first.Id);
        Assert.Equal(first.Id, demand.Id);

        // detay maskeli: EmailMasked ham (maskesiz) e-posta olmamalı
        foreach (var p in demand.Parties ?? new())
        {
            Assert.DoesNotMatch("^[^*]+@", p.EmailMasked ?? string.Empty);
        }

        var timeline = await _imzala.Demands.GetTimelineAsync(first.Id);
        Assert.NotNull(timeline.Events);
    }

    [E2eFact]
    public async Task InvalidId_throws_a_typed_ImzalaError()
    {
        await Assert.ThrowsAnyAsync<ImzalaError>(
            () => _imzala.Demands.GetAsync(Guid.Empty));
    }
}

/// <summary>
/// e2e ortam değişkenlerini bir kez okuyup paylaşır. <c>IMZALA_E2E=1</c> +
/// <c>IMZALA_API_KEY</c> yoksa <see cref="Enabled"/> false olur ve tüm
/// <see cref="E2eFactAttribute"/> testleri atlanır.
/// </summary>
internal static class E2eEnvironment
{
    public static readonly string? ApiKey = Environment.GetEnvironmentVariable("IMZALA_API_KEY");
    public static readonly string? BaseUrl = Environment.GetEnvironmentVariable("IMZALA_BASE_URL");

    public static readonly bool Enabled =
        Environment.GetEnvironmentVariable("IMZALA_E2E") == "1" && !string.IsNullOrEmpty(ApiKey);
}

/// <summary>
/// <c>[Fact]</c> gibidir, ama e2e ortamı ayarlı değilse testi <b>temiz bir
/// şekilde ATLAR</b> (runner'da "Skipped" olarak görünür, "Passed" değil).
/// xUnit 2.x'te dinamik/koşullu atlama için standart yaklaşım: dış paket
/// gerektirmeden <see cref="FactAttribute"/>'ı türetip <see cref="FactAttribute.Skip"/>'i
/// kurucu içinde çalışma-zamanı koşuluna göre set etmek (Node'daki
/// <c>describe.skip</c> davranışının karşılığı).
/// </summary>
internal sealed class E2eFactAttribute : FactAttribute
{
    public E2eFactAttribute()
    {
        if (!E2eEnvironment.Enabled)
        {
            Skip = "e2e devre dışı — çalıştırmak için IMZALA_E2E=1 ve IMZALA_API_KEY ayarlayın.";
        }
    }
}
