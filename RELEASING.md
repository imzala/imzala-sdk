# Yayınlama (Releasing)

Bu monorepo 7 bağımsız paket barındırır, her biri kendi semver'ine ve kendi
git tag önekine sahiptir. `.github/workflows/` altındaki 5 workflow bunları
yayınlar (GitHub-hosted `ubuntu-latest` runner — self-hosted runner
KULLANILMAZ, bkz. `docs/superpowers/specs/2026-06-30-sdk-and-embed-master-design.md`
§12.3/§devops notu).

> ⚠️ **Yayın öncesi zorunlu gate — aşağıdaki "Yayın kapsamı kararı" bölümüne bakmadan
> ilk sürümü YAYINLAMA.** Bu SDK seti henüz avukat onayı almadı ve
> `embed.createSession()` prod'da canlı değil.

## Paket → registry → workflow → tag deseni

| Paket | Registry | Workflow | Tag deseni | Sürüm kaynağı |
|---|---|---|---|---|
| `@imzala/node` | npm | `npm-publish.yml` | `node-vX.Y.Z` | `packages/node/package.json` `version` |
| `@imzala/embed` | npm | `npm-publish.yml` | `embed-vX.Y.Z` | `packages/embed/package.json` `version` |
| `@imzala/embed-react` | npm | `npm-publish.yml` | `embed-react-vX.Y.Z` | `packages/embed-react/package.json` `version` |
| `imzala` (Python) | PyPI | `python-publish.yml` | `python-vX.Y.Z` | `packages/python/pyproject.toml` `[project].version` |
| `Imzala` (.NET) | NuGet | `dotnet-publish.yml` | `dotnet-vX.Y.Z` | `packages/dotnet/src/Imzala.csproj` `<Version>` |
| `org.imzala:imzala-java` | Maven Central | `java-publish.yml` | `java-vX.Y.Z` | `packages/java/pom.xml` `<version>` |
| `imzala/imzala-php` | Packagist | `php-publish.yml` | **`vX.Y.Z`** (bare — no `php-` prefix, see below) | the git tag itself |

Her workflow ayrıca `workflow_dispatch` ile manuel de tetiklenebilir
(`dry_run` input'u — npm/PyPI/NuGet/Maven için varsayılan `true`: build/test/pack
çalışır, registry'ye hiçbir şey yazılmaz).

### 🔴 PHP/Packagist tag deseni neden farklı

Composer, `composer.json`'da bir `version` alanı yoksa (bu repo'da yok —
Composer'ın önerdiği kurulum budur) sürümü doğrudan **git tag'in kendisinden**
okur. Composer'ın sürüm regex'i `v?<rakam>...` ile başlar; `php-v1.0.0` gibi
önekli bir tag bu deseni eşlemez ve Composer bunu düzgün bir `1.0.0` stabil
sürümü olarak değil, `dev-php-v1.0.0` gibi dengesiz bir "branch" sürümü
olarak görür — `composer require imzala/imzala-php` (varsayılan olarak en
yüksek *stabil* sürümü çözer) bu sürümü hiç görmez.

Bu yüzden **önek taşımayan `vX.Y.Z` tag'leri bu repoda sadece PHP/Packagist
sürümleri için ayrılmıştır.** Diğer 6 paket hep `<pkg>-vX.Y.Z` önekli tag
kullandığından (`node-v1.0.0`, `embed-v1.0.0`, ...), bare `v*` ile asla
çakışmaz (GitHub Actions tag filtresi tam-önek eşler, `node-v1.0.0` asla
`v*` deseniyle eşleşmez).

## Sürüm çıkarma adımları (dil bazlı)

Her seferinde: (1) ilgili paketin sürümünü manifest'te bump'la, (2) commit,
(3) o pakete özel tag at, (4) tag'i push et → workflow otomatik tetiklenir.

```bash
# Örnek: @imzala/node 0.1.0 → 0.2.0
cd packages/node && npm version 0.2.0 --no-git-tag-version && cd ../..
git add packages/node/package.json
git commit -m "chore(node): release 0.2.0"
git tag node-v0.2.0
git push origin main node-v0.2.0
```

- **npm paketleri** (`node`, `embed`, `embed-react`): `package.json`
  `version` alanını elle veya `npm version <x.y.z> --no-git-tag-version -w
  @imzala/<pkg>` ile bump'la → commit → `git tag <pkg>-vX.Y.Z` → push.
- **Python**: `packages/python/pyproject.toml` `[project].version`'ı
  bump'la → commit → `git tag python-vX.Y.Z` → push.
- **.NET**: `packages/dotnet/src/Imzala.csproj` `<Version>`'ı bump'la →
  commit → `git tag dotnet-vX.Y.Z` → push.
- **Java**: `packages/java/pom.xml` kök `<version>`'ı bump'la → commit →
  `git tag java-vX.Y.Z` → push.
- **PHP**: manifest'te bump'lanacak bir şey YOK (Composer sürümü tag'den
  okur) — sadece `git tag vX.Y.Z` (bare, önek yok) → push. (Bkz. yukarıdaki
  "PHP/Packagist tag deseni" notu.)

Her workflow, tag ile tetiklendiğinde tag'in beklediği sürümü manifest'teki
gerçek sürümle karşılaştıran bir **version guard** çalıştırır (mismatch
varsa build fail eder, yanlış sürüm asla yayınlanmaz) — PHP hariç (orada
karşılaştırılacak ayrı bir manifest sürümü yok).

## Her registry'nin auth mekanizması

| Registry | Mekanizma | Secret(ler) |
|---|---|---|
| npm | Granular access token + **provenance** (OIDC attestation, token'ın YERİNE değil, YANINDA) | `NPM_TOKEN` |
| PyPI | **Trusted Publishing (OIDC)** — token YOK | *(yok — sadece `id-token: write` izni)* |
| NuGet | **Trusted Publishing (OIDC)** — `NuGet/login@v1`, saklı token YOK | *(yok — `id-token: write` + repo variable `NUGET_USER`)* |
| Packagist | Yok (VCS-driven); opsiyonel re-index ping | `PACKAGIST_USERNAME`, `PACKAGIST_TOKEN` |
| Maven Central | Sonatype Central Portal user token + GPG imza | `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`, `MAVEN_GPG_PRIVATE_KEY`, `MAVEN_GPG_PASSPHRASE` |

**Not (verify):** npm 2025 itibariyle tam "Trusted Publisher" (token'sız,
saf OIDC) desteğini de sunmaya başladı — bu workflow'lar şimdilik daha
köklü/garanti mekanizma olan `NPM_TOKEN` + `--provenance` kombinasyonunu
kullanıyor (görev talimatında da bu istendi). npm'in token'sız trusted
publishing'i npmjs.com'daki paket ayarlarından **doğrulanıp** iyileştirme
olarak sonra devreye alınabilir — şu an "guess" etmiyoruz, mevcut kod
garanti çalışan yolu izliyor.

## Çağdaş — tek seferlik kayıt/kurulum aksiyonları (prod publish öncesi ZORUNLU)

Aşağıdakilerin hiçbiri kod ile yapılamaz — hepsi insan tarafından ilgili
registry/GitHub arayüzünde tek seferlik yapılır:

1. **🔴 npm** — npmjs.com'da `imzala` organizasyonu (scope `@imzala`)
   oluştur (yoksa). Organizasyon altında granular access token üret
   (`Automation` tipi, sadece `@imzala/*` paketlerine publish yetkili) →
   GitHub repo **Environment** `npm-registry` → Secret `NPM_TOKEN`.
2. **🔴 PyPI** — pypi.org'da proje adı `imzala` için bir **Trusted
   Publisher** kaydı oluştur (Publishing → Add a new publisher): owner
   `imzala`, repository `imzala-sdk`, workflow dosyası
   `python-publish.yml`, environment adı `pypi`. Proje henüz yoksa PyPI
   "pending publisher" mekanizmasıyla ilk publish'ten ÖNCE de
   kaydedilebilir. GitHub repo **Environment** adı workflow'daki
   `environment: pypi` ile birebir eşleşmeli. Token GEREKMEZ.
3. **🔴 NuGet — Trusted Publishing (OIDC, API key YOK).** nuget.org →
   hesap → **Trusted Publishing** → policy ekle: GitHub owner `imzala`,
   repo `imzala-sdk`, workflow `dotnet-publish.yml`, environment `nuget`.
   GitHub repo **Environment** `nuget` (workflow'daki `environment: nuget`
   ile birebir) + repo **Variable** (secret değil) `NUGET_USER` = nuget.org
   kullanıcı adın. Saklı `NUGET_API_KEY` GEREKMEZ; `NuGet/login@v1` her
   çalışmada kısa-ömürlü key üretir.
4. **🔴 Packagist** — packagist.org'da hesap ile `github.com/imzala/imzala-sdk`
   reposunu **Submit** et. ⚠️ **Doğrula:** Packagist'in standart GitHub
   entegrasyonu `composer.json`'ı repo KÖKÜNDE bekler; bu repoda
   `packages/php/composer.json` bir ALT dizinde. Bu ya doğrudan
   desteklenmiyor olabilir. İki seçenek:
   (a) packagist.org'a submit ederken/ayarlarda alt dizin
   composer.json'ı gerçekten okuyup okumadığını doğrula, OKUMUYORSA
   (b) `packages/php/` alt ağacını her `v*` tag'inde ayrı bir
   `imzala/imzala-php` GitHub reposuna aynalayan bir "repo splitter" adımı
   ekle (örn. `symplify/monorepo-split-github-action`) ve Packagist'i O
   temiz repoya bağla. Bu B6 kapsamının dışında bırakıldı (yeni bir repo
   açma kararı gerektirir) — Çağdaş'ın kararı bekleniyor.
   Packagist hesabını GitHub'a bağlarsan webhook otomatik kurulur (tag
   push → anında re-index); bağlamazsan `.github/workflows/php-publish.yml`'daki
   opsiyonel curl adımı için `PACKAGIST_USERNAME` + `PACKAGIST_TOKEN`
   (packagist.org → hesap ayarları → API Token) GitHub repo **Environment**
   `packagist`'e eklenebilir — hiçbiri yoksa adım sessizce atlanır (workflow
   kırılmaz).
5. **🔴 Maven Central (Sonatype Central Portal)** — central.sonatype.com'da
   hesap oluştur, **`org.imzala` namespace'ini doğrula** (DNS TXT kaydı ile
   `imzala.org` alan adı sahipliğini kanıtlama — `org.imzala` groupId
   reverse-domain kuralına göre `imzala.org`'a bağlı). Central Portal'da
   kullanıcı token'ı üret (username/password çifti, hesap şifren DEĞİL) →
   GitHub repo **Environment** `maven-central` → Secret'ler
   `SONATYPE_USERNAME` + `SONATYPE_PASSWORD`. Ayrıca bir **GPG anahtar
   çifti** üret (`gpg --full-generate-key`), public key'i bir keyserver'a
   yükle (`gpg --keyserver keyserver.ubuntu.com --send-keys <KEYID>` —
   Central doğrulama sırasında public key'i keyserver'dan çeker), private
   key'i ASCII-armored (`gpg --export-secret-keys --armor <KEYID>`) olarak
   Secret `MAVEN_GPG_PRIVATE_KEY`'e, parolasını `MAVEN_GPG_PASSPHRASE`'e
   koy.
6. **GitHub Environments** — repo Settings → Environments'ta yukarıdaki 5
   environment'ı (`npm-registry`, `pypi`, `nuget`, `packagist`,
   `maven-central`) oluştur, her birine ilgili secret'ları ekle. (Secret'lar
   repo-level değil environment-level — bu, "required reviewers" gibi ek
   bir onay kapısı eklemeyi de mümkün kılar; public paket yayınlamak için
   önerilir.)
7. **Sonatype Central Portal ilk publish** — `autoPublish=false`
   (`packages/java/pom.xml` `release` profile) olduğundan, CI bundle'ı
   Central'a yükledikten sonra central.sonatype.com'da elle "Publish"
   tıklaman gerekir (ilk birkaç sürüm için önerilir — sürpriz yok
   doğrulandıktan sonra `autoPublish=true`'ya geçirilebilir).

## Yayın kapsamı kararı — `embed.createSession` (Çağdaş kararı bekliyor)

Her 5 server SDK'sının facade'i (`imzala.embed().createSession(...)` / dil
eşdeğerleri) `POST /api/v1/demands/{id}/embed-session` endpoint'ini
çağırıyor. Bu endpoint **şu an sadece TEST ortamında** var ve **gömülü imza
(embed) özelliği henüz avukat onaylı prod-canlı DEĞİL** (bkz. proxmox-imzala
CLAUDE.md → "SDK + Gömülü İmza" — backend esign Docker-gated, prod=avukat
onayı bekliyor; memory `project_sdk_embed_implementation.md`).

Yani SDK'ları OLDUĞU GİBİ ilk kez yayınlarsak, npm/PyPI/NuGet/Packagist/Maven
Central'daki genel kullanıcılar `embed.createSession()`'ı prod'a karşı
çağırdığında 404 alır (ya da endpoint hiç yok).

**İki seçenek — Çağdaş karar verecek:**

- **(a) İlk SDK yayınını embed prod-hazır olana kadar ERTELE.** En temiz
  seçenek; embed'in avukat onayı zaten prod-gate olduğundan SDK'nın da aynı
  gate'e tabi olması tutarlı.
- **(b) `v0.x` "beta" etiketiyle yayınla, embed'i README'de açıkça
  "preview/test-only" olarak işaretle.** SDK'nın geri kalanı (templates,
  demands, timestamps, webhook doğrulama) prod-hazırsa erken erişim
  sağlar; ama her paketin README'sinde + kod-içi docstring'lerinde
  `embed.createSession()` için "yalnızca test ortamında çalışır, prod'da
  henüz yok" uyarısı ZORUNLU olur.

Her iki durumda da: **SDK/README'deki imza-iddiası dili avukat gate'inden
geçmeden yayınlanamaz** — "SES" (Basit Elektronik İmza) ötesine geçen hiçbir
ifade ("nitelikli", "QES", "güvenli elektronik imza ile eşdeğer" vb.)
kullanılamaz; bkz. proxmox-imzala CLAUDE.md → imza sınıfı kuralı
(`project_current_signature_class` memory) ve "dijital imza" terminoloji
kuralı. Bu, master design §8'deki compliance gate'inin (hukuki doküman +
`imzala-legal-advisor`/`imzala-kvkk-advisor` ön-review + lisanslı avukat
onayı = prod gate) bir parçasıdır — B6 bunu YAPMAZ, sadece CI/release
altyapısını hazırlar.

## CI davranışı özeti

- Her workflow: checkout → dil toolchain kurulumu → **version guard**
  (tag'deki sürüm ↔ manifest sürümü eşleşmeli, PHP hariç) → testler →
  build/pack → (dry-run değilse) publish.
- `dry_run` girişi (yalnızca `workflow_dispatch`) varsayılan `true` —
  yanlışlıkla manuel tetiklenen bir run registry'ye asla yazmaz.
- Secret'lar GitHub **Environment**'lara bağlı (repo-level değil) —
  environment'a "required reviewers" eklenirse her public yayın insan
  onayından geçer.
- Hiçbir workflow secret değerini loglara yazmaz; GitHub Actions zaten
  `secrets.*` değerlerini log'da otomatik maskeler, ama yine de hiçbir
  `run:` adımı bir secret'i `echo`/`printf` ETMEZ.
