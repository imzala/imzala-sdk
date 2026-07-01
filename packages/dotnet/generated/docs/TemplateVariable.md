# ImzalaApiClient.Model.TemplateVariable

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Slug** | **string** |  | [optional] 
**Label** | **string** |  | [optional] 
**ItemType** | **string** |  | [optional] 
**IsRequired** | **bool** |  | [optional] 
**DefaultSource** | **string** | Doluysa item değeri otomatik olarak bu kaynaktan dolar (örn. signer.full_name → party_mapping&#39;teki ad+soyad). variables payload&#39;ında override edilebilir.  **Kullanılabilir sistem değişkenleri:**  İmzalayan (party-bağlı, render anında çözülür): - &#x60;signer.first_name&#x60;, &#x60;signer.last_name&#x60;, &#x60;signer.full_name&#x60; - &#x60;signer.email&#x60;, &#x60;signer.phone&#x60;, &#x60;signer.government_id&#x60; - &#x60;signer.birth_date&#x60; — İmzalayanın doğum tarihi (gg.aa.yyyy).   Source: &#x60;party_mapping[i].birth_date&#x60; API alanı. - &#x60;signer.sign_date&#x60; — İmzalayanın imza tarihi (gg.aa.yyyy);   imzalanmadıysa boş. Source: server-computed   (&#x60;DemandContractParty.sign_timestamp&#x60;), API üzerinden   settable DEĞİL.  Sözleşme: &#x60;contract.title&#x60;, &#x60;contract.created_date&#x60;, &#x60;contract.expiry_date&#x60;, &#x60;contract.id&#x60;  Gönderen: &#x60;sender.full_name&#x60;, &#x60;sender.email&#x60;, &#x60;sender.company_name&#x60;  Tarih: &#x60;current.date&#x60;, &#x60;current.datetime&#x60;  **Precedence — TC alanı ve diğer slug çakışmaları:** Eğer &#x60;party_mapping[i].government_id&#x60; ile &#x60;party_mapping[i].variables.tc_kimlik&#x60; (veya başka slug-eşleşmeli variable) aynı anda gönderilirse, **&#x60;variables.&lt;slug&gt;&#x60; öncelikli** olur. Bunun nedeni &#x60;applyPartyAwareVariables&#x60; slug yazımını önce uygular; system variable autofill (&#x60;signer.government_id&#x60; defaultSource&#39;u) sonra çalışır ve dolu alanları atlar.  | [optional] 
**TemplatePartyId** | **string** | Bu field&#39;in sahibi olan template parti id&#39;si. NULL ise root scope (party_mapping dışında) — kök variables&#39;a göndermeniz gerekir. Doluysa party_mapping[i].variables&#39;ta i bu id ile eşleşen partinin altına göndermeniz gerekir.  | [optional] 

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)

