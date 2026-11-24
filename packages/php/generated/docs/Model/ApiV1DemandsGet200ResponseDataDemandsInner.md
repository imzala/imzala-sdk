# ApiV1DemandsGet200ResponseDataDemandsInner

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **string** |  | [optional]
**title** | **string** |  | [optional]
**status** | **string** |  | [optional]
**created_at** | **\DateTime** |  | [optional]
**completed_at** | **\DateTime** |  | [optional]
**term_end_date** | **\DateTime** | Takip edilen bitiş tarihi; takip yoksa &#x60;null&#x60; (bkz. &#x60;GET /demands/{id}&#x60; → &#x60;term&#x60;). | [optional]
**archived_at** | **\DateTime** |  | [optional]
**parties_total** | **int** |  | [optional]
**parties_signed** | **int** |  | [optional]
**pdf_url** | **string** | COMPLETED ise imzalı PDF public URL&#39;i. Açık rıza belgesi bu herkese açık bağlantıdan verilmez (zarfın ilk belgesi açık rıza belgesiyse 404 döner); API anahtarıyla &#x60;GET /api/v1/demands/{id}/pdf&#x60; kullanın. | [optional]
**external_ref** | **string** | Kendi sisteminizdeki kayıt numarası (ör. sipariş no). Benzersiz olması gerekmez; aynı değerle birden çok sözleşme listelenebilir. Kimlik numarası, e-posta veya telefon yazmayın.  En fazla 255 karakter. Baştaki ve sondaki boşluklar kırpılır; boş dize &#x60;null&#x60; sayılır. Metin olmayan değer ya da satır sonu, sekme, kontrol veya görünmez yön karakteri içeren değer &#x60;400 EXTERNAL_REF_INVALID&#x60;, 255 karakteri aşan değer &#x60;400 EXTERNAL_REF_TOO_LONG&#x60; döner; hata mesajı gönderdiğiniz değeri içermez. Eşleşme tam ve büyük/küçük harf duyarlıdır.  &#x60;idempotency_key&#x60; ile farkı: idempotency anahtarı her oluşturma isteği için benzersizdir ve aynı isteğin iki kez işlenmesini engeller; takip numarası ise tekrar edebilir ve tekrar korumasının yerine geçmez. Aynı &#x60;Idempotency-Key&#x60; ile farklı bir takip numarası gönderilirse istek farklı içerik sayılır (&#x60;409 IDEMPOTENCY_KEY_REUSED&#x60;). Kimlik doğrulama (KYC) uçlarındaki &#x60;external_ref&#x60; alanından farklı olarak benzersizlik kuralı yoktur.  Tanımlı değilse &#x60;null&#x60; döner. Sonradan &#x60;PATCH /api/v1/demands/{id}/external-ref&#x60; ile değiştirilebilir. İmzacılara gösterilmez. | [optional]
**contract_numbers** | [**\Imzala\Client\Model\ContractNumber[]**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları. Numara yoksa boş dizi. | [optional]

[[Back to Model list]](../../README.md#models) [[Back to API list]](../../README.md#endpoints) [[Back to README]](../../README.md)
