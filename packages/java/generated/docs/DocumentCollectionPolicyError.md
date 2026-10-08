

# DocumentCollectionPolicyError

Seçili Belge Toplama şablonunun politikası geçersiz; error kaynak makine kodudur.

## Properties

| Name | Type | Description | Notes |
|------------ | ------------- | ------------- | -------------|
|**success** | **Boolean** |  |  |
|**code** | [**CodeEnum**](#CodeEnum) |  |  |
|**error** | [**ErrorEnum**](#ErrorEnum) |  |  |



## Enum: CodeEnum

| Name | Value |
|---- | -----|
| VERIFICATION_CONTEXT_REQUIRED | &quot;VERIFICATION_CONTEXT_REQUIRED&quot; |



## Enum: ErrorEnum

| Name | Value |
|---- | -----|
| DOCUMENT_TYPE_INVALID | &quot;DOCUMENT_TYPE_INVALID&quot; |
| VERIFICATION_POLICY_INVALID | &quot;VERIFICATION_POLICY_INVALID&quot; |
| VERIFICATION_INSTRUCTION_INVALID | &quot;VERIFICATION_INSTRUCTION_INVALID&quot; |
| VERIFICATION_CONTEXT_REQUIRED | &quot;VERIFICATION_CONTEXT_REQUIRED&quot; |
| VERIFICATION_POLICY_CONFLICT | &quot;VERIFICATION_POLICY_CONFLICT&quot; |



