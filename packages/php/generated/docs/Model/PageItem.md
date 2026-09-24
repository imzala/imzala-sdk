# PageItem

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**page_id** | **int** | AgreementPage.id (&#x60;/upload&#x60; response&#39;undaki &#x60;pages[].id&#x60;) |
**party_id** | **string** | &#x60;signature&#x60; ve doldurulabilir alanlar (&#x60;dynamic_text&#x60;, &#x60;cells&#x60;, &#x60;date&#x60;, &#x60;dropdown&#x60;, &#x60;checkbox&#x60;, &#x60;radio&#x60;) için **zorunlu** — alanı dolduracak/imzalayacak partinin id&#39;si (&#x60;signing_urls[].party_id&#x60;). &#x60;text&#x60; için null. &#x60;stamp&#x60; için yalnız &#x60;config.source: FILLER_PROVIDES&#x60; ise kaşeyi dolduracak taraftır (zorunlu kaşede şart); diğer kaynaklarda yok sayılır. | [optional]
**item_type** | **string** |  |
**position_x** | **float** | Sayfa genişliğine göre x koordinatı (sol&#x3D;0) |
**position_y** | **float** | Sayfa yüksekliğine göre y koordinatı (üst&#x3D;0) |
**width** | **float** |  |
**height** | **float** |  |
**is_required** | **bool** | İmza/alan zorunlu mu — tarafın bu alanı doldurmadan imzalayamadığı | [optional] [default to false]
**slug** | **string** | Alan tanımlayıcı (snake_case, 2-50 karakter). Doldurulabilir alanlar için **önerilir**. &#x60;dynamic_text&#x60;/&#x60;cells&#x60; gibi değişken alanlarda &#x60;config.defaultSource&#x60; ile system değişkenleri (&#x60;{{signer.full_name}}&#x60;, &#x60;{{signer.government_id}}&#x60; vb.) bağlanır. | [optional]
**label** | **string** | Kullanıcıya gösterilecek etiket | [optional]
**config** | **object** | Item type&#39;a özgü konfigürasyon: - &#x60;dynamic_text&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;cells&#x60;: &#x60;{ cellCount, defaultSource }&#x60; - &#x60;date&#x60;: &#x60;{ defaultSource, defaultValue }&#x60; - &#x60;dropdown&#x60;/&#x60;radio&#x60;: &#x60;{ options: [{label, value}], defaultValue }&#x60; - &#x60;checkbox&#x60;: &#x60;{ checkedByDefault }&#x60; - &#x60;stamp&#x60;: &#x60;{ source, stampData, isRequired }&#x60;: &#x60;source&#x60; &#x3D;   &#x60;INLINE&#x60; | &#x60;FROM_SAVED&#x60; | &#x60;FILLER_PROVIDES&#x60;; &#x60;stampData&#x60; &#x3D;   yapılandırılmış kaşe verisi (bkz. &#x60;StampData&#x60;) - &#x60;signature&#x60;: &#x60;{ requireFirstSignatureOtp: true }&#x60; (opsiyonel,   varsayılan kapalı). Açıksa imzacı, zarftaki İLK imzasını atarken   tarafın telefon numarasına gönderilen SMS kodunu doğrular;   girişteki telefon doğrulama adımı bu taraf için gösterilmez.   Bu taraf için sözleşme içeriği, imza bağlantısına sahip kişi   tarafından kod girilmeden görüntülenebilir. Sözleşmede telefon   doğrulaması da açıksa, imza içermeyen kararlar (ör. isteğe   bağlı bir belgeyi onaylamamak) ayrı bir SMS koduyla   doğrulanır; bu durumda ilk imzada yeniden kod istenir. Yalnız   imzacı tarafa atanmış imza alanında ve geçerli cep telefonu   numarası olan tarafta kullanılabilir. Nitelikli e-imza (NES),   onaycı ve kendi kendine imza akışlarında desteklenmez; bu   durumda gönderim ya da alan kaydı imza bağlantısı   üretilmeden &#x60;400 FIRST_SIGNATURE_OTP_UNSUPPORTED&#x60; ile   reddedilir. Taraf imzalamaya başladıktan sonra açılamaz. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
