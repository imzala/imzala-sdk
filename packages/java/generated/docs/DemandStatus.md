

# DemandStatus


## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**id** | **UUID** |  |  [optional] |
|**title** | **String** |  |  [optional] |
|**externalRef** | **String** | Kendi sisteminizdeki kayıt numarası (ör. sipariş no). Benzersiz olması gerekmez; aynı değerle birden çok sözleşme listelenebilir. Kimlik numarası, e-posta veya telefon yazmayın.  En fazla 255 karakter. Baştaki ve sondaki boşluklar kırpılır; boş dize &#x60;null&#x60; sayılır. Metin olmayan değer ya da satır sonu, sekme, kontrol veya görünmez yön karakteri içeren değer &#x60;400 EXTERNAL_REF_INVALID&#x60;, 255 karakteri aşan değer &#x60;400 EXTERNAL_REF_TOO_LONG&#x60; döner; hata mesajı gönderdiğiniz değeri içermez. Eşleşme tam ve büyük/küçük harf duyarlıdır.  &#x60;idempotency_key&#x60; ile farkı: idempotency anahtarı her oluşturma isteği için benzersizdir ve aynı isteğin iki kez işlenmesini engeller; takip numarası ise tekrar edebilir ve tekrar korumasının yerine geçmez. Aynı &#x60;Idempotency-Key&#x60; ile farklı bir takip numarası gönderilirse istek farklı içerik sayılır (&#x60;409 IDEMPOTENCY_KEY_REUSED&#x60;). Kimlik doğrulama (KYC) uçlarındaki &#x60;external_ref&#x60; alanından farklı olarak benzersizlik kuralı yoktur.  Tanımlı değilse &#x60;null&#x60; döner. Sonradan &#x60;PATCH /api/v1/demands/{id}/external-ref&#x60; ile değiştirilebilir. İmzacılara gösterilmez.  |  [optional] |
|**status** | **String** | Bilinen değerler: DRAFT, PENDING, COMPLETED, EXPIRED, CANCELLED. Sunucu ileride yeni değer ekleyebilir; bilinmeyen değeri hata saymayın. |  [optional] |
|**createdAt** | **OffsetDateTime** |  |  [optional] |
|**completedAt** | **OffsetDateTime** |  |  [optional] |
|**parties** | [**List&lt;DemandStatusPartiesInner&gt;**](DemandStatusPartiesInner.md) |  |  [optional] |
|**stampItems** | [**List&lt;DemandStatusStampItemsInner&gt;**](DemandStatusStampItemsInner.md) | Sözleşmedeki kaşe alanları. &#x60;PATCH /api/v1/demands/{id}/items/{itemId}/stamp&#x60; için alan kimliği buradan alınır. Kaşe içeriği bu listede dönmez.  |  [optional] |
|**resultUrl** | **URI** | Sonuç sayfası açık rıza belgesini, sayfalarını ve indirme bağlantısını göstermez. |  [optional] |
|**pdfUrl** | **URI** | Sadece status&#x3D;COMPLETED iken dolu. Açık rıza belgesi bu herkese açık bağlantıdan verilmez (zarfın ilk belgesi açık rıza belgesiyse 404 döner); API anahtarıyla &#x60;GET /api/v1/demands/{id}/pdf&#x60; kullanın. |  [optional] |
|**documents** | [**List&lt;DemandDocumentStatus&gt;**](DemandDocumentStatus.md) | Zarftaki belgelerin her biri için ayrı durum, &#x60;order&#x60; sırasıyla. Zarf genelindeki &#x60;status&#x60; alanı değişmez; bu liste hangi belgenin ne zaman tamamlandığını, hangisinin beklediğini, mühür durumunu ve her tarafın belge başına kararını ayrıca gösterir. Belge kaydı bulunmayan eski sözleşmelerde boş dizi döner.  |  [optional] |
|**term** | [**ContractTerm**](ContractTerm.md) | Sözleşmede süre/yenileme takibi tanımlı değilse &#x60;null&#x60;. |  [optional] |
|**archivedAt** | **OffsetDateTime** |  |  [optional] |
|**contractNumbers** | [**List&lt;ContractNumber&gt;**](ContractNumber.md) | Sözleşmeye verilmiş sözleşme numaraları. Numara yoksa boş dizi.  |  [optional] |
