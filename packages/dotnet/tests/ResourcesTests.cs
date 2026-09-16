using System.Text;
using ImzalaApiClient.Api;
using ImzalaApiClient.Client;
using ImzalaApiClient.Model;
using ImzalaSdk;
using Moq;
using Xunit;
using GeneratedApiException = ImzalaApiClient.Client.ApiException;
using GeneratedMultimap = ImzalaApiClient.Client.Multimap<string, string>;

namespace ImzalaSdk.Tests;

/// <summary>
/// Field templates, contacts, reports, timestamp listing, bulk create and
/// envelope document download. Every generated call is verified slot by
/// slot with distinct values: same-typed neighbours still compile when an
/// argument shifts, so only a per-slot assertion catches it.
/// </summary>
public class ResourcesTests
{
    private static readonly RetryConfig NoWaitRetry = new() { MaxRetries = 2, RetryBaseDelayMs = 1, Delay = (_, _) => Task.CompletedTask };

    // ---- field templates ----------------------------------------------------

    [Fact]
    public async Task FieldTemplates_ListAsync_forwards_page_and_limit_in_order()
    {
        var templates = new Mock<ITemplatesApi>();
        templates.Setup(a => a.ApiV1FieldTemplatesGetAsync(2, 50, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1FieldTemplatesGet200Response(true, new ApiV1FieldTemplatesGet200ResponseData(total: 7)));

        var result = await new FieldTemplatesResource(templates.Object, new Mock<IDemandsApi>().Object, NoWaitRetry).ListAsync(page: 2, limit: 50);

        Assert.Equal(7, result.Total);
        templates.Verify(a => a.ApiV1FieldTemplatesGetAsync(2, 50, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task FieldTemplates_ListAsync_defaults_to_server_paging()
    {
        var templates = new Mock<ITemplatesApi>();
        templates.Setup(a => a.ApiV1FieldTemplatesGetAsync(null, null, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1FieldTemplatesGet200Response(true, new ApiV1FieldTemplatesGet200ResponseData()));

        await new FieldTemplatesResource(templates.Object, new Mock<IDemandsApi>().Object, NoWaitRetry).ListAsync();

        templates.Verify(a => a.ApiV1FieldTemplatesGetAsync(null, null, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task FieldTemplates_GetAsync_is_retried_as_a_get()
    {
        var id = Guid.NewGuid();
        var templates = new Mock<ITemplatesApi>();
        templates.SetupSequence(a => a.ApiV1FieldTemplatesIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ThrowsAsync(new GeneratedApiException(503, "down", """{"success":false}""", new GeneratedMultimap()))
            .ReturnsAsync(new ApiV1FieldTemplatesIdGet200Response(true, new FieldTemplateDetail(id: id, name: "Kira")));

        var result = await new FieldTemplatesResource(templates.Object, new Mock<IDemandsApi>().Object, NoWaitRetry).GetAsync(id);

        Assert.Equal("Kira", result.Name);
        templates.Verify(a => a.ApiV1FieldTemplatesIdGetAsync(id, It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    [Fact]
    public async Task FieldTemplates_PreviewLayoutAsync_sends_the_id_one_pdf_and_on_anchor_miss_in_their_slots()
    {
        var id = Guid.NewGuid();
        var demands = new Mock<IDemandsApi>();
        Guid? capturedId = null;
        List<FileParameter>? capturedFiles = null;
        string? capturedOnAnchorMiss = null;
        demands.Setup(a => a.ApiV1FieldTemplatesIdPreviewLayoutPostAsync(It.IsAny<Guid>(), It.IsAny<List<FileParameter>>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .Callback<Guid, List<FileParameter>, string?, CancellationToken>((i, files, onAnchorMiss, _) =>
            {
                capturedId = i;
                capturedFiles = files;
                capturedOnAnchorMiss = onAnchorMiss;
            })
            .ReturnsAsync(new ApiV1FieldTemplatesIdPreviewLayoutPost200Response(true, new FieldLayoutPreview(templateId: id, resolvable: true)));

        var result = await new FieldTemplatesResource(new Mock<ITemplatesApi>().Object, demands.Object, NoWaitRetry).PreviewLayoutAsync(
            id,
            new[] { new FileInput { Content = "pdf"u8.ToArray(), FileName = "sozlesme.pdf", ContentType = "application/pdf" } },
            onAnchorMiss: "drop");

        Assert.True(result.Resolvable);
        Assert.Equal(id, capturedId);
        Assert.Single(capturedFiles!);
        Assert.Equal("sozlesme.pdf", capturedFiles![0].Name);
        Assert.Equal("drop", capturedOnAnchorMiss);
    }

    [Fact]
    public async Task FieldTemplates_PreviewLayoutAsync_without_on_anchor_miss_sends_null_and_is_not_retried()
    {
        var demands = new Mock<IDemandsApi>();
        demands.Setup(a => a.ApiV1FieldTemplatesIdPreviewLayoutPostAsync(It.IsAny<Guid>(), It.IsAny<List<FileParameter>>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new GeneratedApiException(429, "slow down", """{"success":false,"code":"RATE_LIMIT_EXCEEDED"}""", new GeneratedMultimap()));

        await Assert.ThrowsAsync<ImzalaRateLimitError>(() => new FieldTemplatesResource(new Mock<ITemplatesApi>().Object, demands.Object, NoWaitRetry)
            .PreviewLayoutAsync(Guid.NewGuid(), new[] { new FileInput { Content = "pdf"u8.ToArray(), FileName = "a.pdf" } }));

        demands.Verify(a => a.ApiV1FieldTemplatesIdPreviewLayoutPostAsync(It.IsAny<Guid>(), It.IsAny<List<FileParameter>>(), null, It.IsAny<CancellationToken>()), Times.Once);
    }

    // ---- contacts -------------------------------------------------------------

    [Fact]
    public async Task Contacts_ListAsync_forwards_every_filter_in_its_slot()
    {
        var companyId = Guid.NewGuid();
        var contacts = new Mock<IContactsApi>();
        contacts.Setup(a => a.ApiV1ContactsGetAsync(2, 30, "ayse", "-createdAt", companyId, true, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1ContactsGet200Response(true, new ApiV1ContactsGet200ResponseData(total: 1)));

        var result = await new ContactsResource(contacts.Object, NoWaitRetry)
            .ListAsync(q: "ayse", page: 2, limit: 30, sort: "-createdAt", companyId: companyId, archived: true);

        Assert.Equal(1, result.Total);
        contacts.Verify(a => a.ApiV1ContactsGetAsync(2, 30, "ayse", "-createdAt", companyId, true, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task Contacts_ListAsync_without_arguments_sends_nothing()
    {
        var contacts = new Mock<IContactsApi>();
        contacts.Setup(a => a.ApiV1ContactsGetAsync(null, null, null, null, null, null, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1ContactsGet200Response(true, new ApiV1ContactsGet200ResponseData()));

        await new ContactsResource(contacts.Object, NoWaitRetry).ListAsync();

        contacts.Verify(a => a.ApiV1ContactsGetAsync(null, null, null, null, null, null, It.IsAny<CancellationToken>()), Times.Once);
    }

    private static ApiV1ContactsGet200Response ContactsPage(int page, int limit, int total, int count) =>
        new(true, new ApiV1ContactsGet200ResponseData(
            contacts: Enumerable.Range(0, count).Select(_ => new ContactSummary(id: Guid.NewGuid(), firstName: "Ayşe")).ToList(),
            total: total,
            page: page,
            limit: limit));

    [Fact]
    public async Task Contacts_ListAllAsync_walks_pages_and_keeps_filters()
    {
        var companyId = Guid.NewGuid();
        var contacts = new Mock<IContactsApi>();
        contacts.Setup(a => a.ApiV1ContactsGetAsync(1, 10, "ayse", "email", companyId, false, It.IsAny<CancellationToken>())).ReturnsAsync(ContactsPage(1, 10, 15, 10));
        contacts.Setup(a => a.ApiV1ContactsGetAsync(2, 10, "ayse", "email", companyId, false, It.IsAny<CancellationToken>())).ReturnsAsync(ContactsPage(2, 10, 15, 5));

        var all = new List<ContactSummary>();
        await foreach (var c in new ContactsResource(contacts.Object, NoWaitRetry).ListAllAsync(q: "ayse", limit: 10, sort: "email", companyId: companyId, archived: false))
        {
            all.Add(c);
        }

        Assert.Equal(15, all.Count);
        contacts.Verify(a => a.ApiV1ContactsGetAsync(It.IsAny<int?>(), It.IsAny<int?>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<Guid?>(), It.IsAny<bool?>(), It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    [Fact]
    public async Task Contacts_ListAllAsync_stops_on_an_empty_page()
    {
        var contacts = new Mock<IContactsApi>();
        contacts.Setup(a => a.ApiV1ContactsGetAsync(1, null, null, null, null, null, It.IsAny<CancellationToken>())).ReturnsAsync(ContactsPage(1, 25, 0, 0));

        var all = new List<ContactSummary>();
        await foreach (var c in new ContactsResource(contacts.Object, NoWaitRetry).ListAllAsync())
        {
            all.Add(c);
        }

        Assert.Empty(all);
        contacts.Verify(a => a.ApiV1ContactsGetAsync(It.IsAny<int?>(), It.IsAny<int?>(), It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<Guid?>(), It.IsAny<bool?>(), It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task Contacts_CreateAsync_sends_the_body_and_unwraps()
    {
        var body = new ApiV1ContactsPostRequest(firstName: "Ayşe", lastName: "Yılmaz", email: "ayse@example.com", phone: "+905551112233");
        var contacts = new Mock<IContactsApi>();
        contacts.Setup(a => a.ApiV1ContactsPostAsync(body, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1ContactsPost201Response(true, new ContactSummary(id: Guid.NewGuid(), email: "ayse@example.com")));

        var result = await new ContactsResource(contacts.Object, NoWaitRetry).CreateAsync(body);

        Assert.Equal("ayse@example.com", result.Email);
        contacts.Verify(a => a.ApiV1ContactsPostAsync(body, It.IsAny<CancellationToken>()), Times.Once);
    }

    // ---- reports ----------------------------------------------------------------

    [Fact]
    public async Task Reports_GetAsync_unwraps()
    {
        var reports = new Mock<IReportsApi>();
        var data = new ApiV1ReportsGet200ResponseData();
        reports.Setup(a => a.ApiV1ReportsGetAsync(It.IsAny<CancellationToken>())).ReturnsAsync(new ApiV1ReportsGet200Response(true, data));

        Assert.Same(data, await new ReportsResource(reports.Object, NoWaitRetry).GetAsync());
    }

    // ---- timestamps -------------------------------------------------------------

    [Fact]
    public async Task Timestamps_ListAsync_forwards_every_filter_in_its_slot()
    {
        var from = new DateOnly(2026, 1, 1);
        var to = new DateOnly(2026, 2, 1);
        var timestamps = new Mock<ITimestampsApi>();
        timestamps.Setup(a => a.ApiV1TimestampsGetAsync(3, 40, "eser", "ACTIVE,VERIFIED", from, to, "-timestamp_date", It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TimestampsGet200Response(true, new ApiV1TimestampsGet200ResponseData(total: 2)));

        var result = await new TimestampsResource(timestamps.Object, NoWaitRetry)
            .ListAsync(q: "eser", status: "ACTIVE,VERIFIED", from: from, to: to, page: 3, limit: 40, sort: "-timestamp_date");

        Assert.Equal(2, result.Total);
        timestamps.Verify(a => a.ApiV1TimestampsGetAsync(3, 40, "eser", "ACTIVE,VERIFIED", from, to, "-timestamp_date", It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task Timestamps_GetAsync_unwraps()
    {
        var id = Guid.NewGuid();
        var timestamps = new Mock<ITimestampsApi>();
        timestamps.Setup(a => a.ApiV1TimestampsIdGetAsync(id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TimestampsIdGet200Response(true, new TimestampListItem(id: id, originalFileName: "eser.pdf")));

        var result = await new TimestampsResource(timestamps.Object, NoWaitRetry).GetAsync(id);

        Assert.Equal("eser.pdf", result.OriginalFileName);
    }

    // ---- demands ----------------------------------------------------------------

    private static DemandsResource Demands(Mock<IDemandsApi> api) => new(api.Object, new Mock<IRemindersApi>().Object, NoWaitRetry);

    [Fact]
    public async Task Demands_CreateBulkAsync_sends_the_body_and_no_workspace_header()
    {
        var body = new ApiV1DemandsBulkPostRequest(templateId: Guid.NewGuid(), rows: new List<ApiV1DemandsBulkPostRequestRowsInner>());
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsBulkPostAsync(body, null, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsBulkPost200Response(true, new ApiV1DemandsBulkPost200ResponseData(created: 3)));

        var result = await Demands(api).CreateBulkAsync(body);

        Assert.Equal(3, result.Created);
        api.Verify(a => a.ApiV1DemandsBulkPostAsync(body, null, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task Demands_GetDocumentPdfAsync_returns_bytes_and_forwards_both_ids_in_order()
    {
        var id = Guid.NewGuid();
        var documentId = Guid.NewGuid();
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsIdBelgeDocumentIdPdfGetAsync(id, documentId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new FileParameter("belge.pdf", "application/pdf", new MemoryStream("%PDF-1.7"u8.ToArray())));

        var bytes = await Demands(api).GetDocumentPdfAsync(id, documentId);

        Assert.Equal("%PDF-1.7", Encoding.ASCII.GetString(bytes));
        api.Verify(a => a.ApiV1DemandsIdBelgeDocumentIdPdfGetAsync(id, documentId, It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task Demands_GetDocumentPdfAsync_maps_a_404()
    {
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsIdBelgeDocumentIdPdfGetAsync(It.IsAny<Guid>(), It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new GeneratedApiException(404, "nope", """{"success":false,"code":"DEMAND_NOT_FOUND"}""", new GeneratedMultimap()));

        var err = await Assert.ThrowsAsync<ImzalaError>(() => Demands(api).GetDocumentPdfAsync(Guid.NewGuid(), Guid.NewGuid()));
        Assert.Equal(404, err.StatusCode);
        Assert.Equal("DEMAND_NOT_FOUND", err.Code);
    }

    private sealed class UploadCall
    {
        public List<FileParameter>? Files;
        public string? Parties;
        public string? IdempotencyKey;
        public string? Order;
        public string? Title;
        public string? Description;
        public Guid? FieldTemplateId;
        public string? Force;
        public string? SendInvitations;
        public string? OnAnchorMiss;
    }

    private static UploadCall CaptureUpload(Mock<IDemandsApi> api)
    {
        var call = new UploadCall();
        api.Setup(a => a.ApiV1DemandsUploadPostAsync(
                It.IsAny<List<FileParameter>>(), It.IsAny<string>(), It.IsAny<string?>(), It.IsAny<string?>(),
                It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<Guid?>(), It.IsAny<string?>(),
                It.IsAny<string?>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .Callback<List<FileParameter>, string, string?, string?, string?, string?, Guid?, string?, string?, string?, CancellationToken>(
                (files, parties, idempotencyKey, order, title, description, fieldTemplateId, force, sendInvitations, onAnchorMiss, _) =>
                {
                    call.Files = files;
                    call.Parties = parties;
                    call.IdempotencyKey = idempotencyKey;
                    call.Order = order;
                    call.Title = title;
                    call.Description = description;
                    call.FieldTemplateId = fieldTemplateId;
                    call.Force = force;
                    call.SendInvitations = sendInvitations;
                    call.OnAnchorMiss = onAnchorMiss;
                })
            .ReturnsAsync(new ApiV1DemandsUploadPost201Response(true, new CreatedDemandUpload(id: Guid.NewGuid())));
        return call;
    }

    [Fact]
    public async Task Demands_UploadDocumentAsync_puts_every_field_in_its_own_slot()
    {
        var fieldTemplateId = Guid.NewGuid();
        var templatePartyId = Guid.NewGuid();
        var api = new Mock<IDemandsApi>();
        var call = CaptureUpload(api);

        await Demands(api).UploadDocumentAsync(new UploadDemandParams
        {
            Files = new[] { new FileInput { Content = "pdf"u8.ToArray(), FileName = "sozlesme.pdf", ContentType = "application/pdf" } },
            Parties = new[] { new UploadPartyInput { FirstName = "Ayşe", LastName = "Yılmaz", Phone = "+905551112233", TemplatePartyId = templatePartyId } },
            Order = new[] { 0 },
            Title = "baslik-slotu",
            Description = "aciklama-slotu",
            IdempotencyKey = "anahtar-slotu",
            FieldTemplateId = fieldTemplateId,
            OnAnchorMiss = "drop",
            SendInvitations = "sms",
            Force = true,
        });

        Assert.Equal("sozlesme.pdf", Assert.Single(call.Files!).Name);
        // Compared after parsing: the encoder escapes '+' as \u002B, which is the same JSON string.
        using (var parties = System.Text.Json.JsonDocument.Parse(call.Parties!))
        {
            var party = Assert.Single(parties.RootElement.EnumerateArray().ToList());
            Assert.Equal("Ayşe", party.GetProperty("first_name").GetString());
            Assert.Equal("Yılmaz", party.GetProperty("last_name").GetString());
            Assert.Equal("+905551112233", party.GetProperty("phone").GetString());
            Assert.Equal(templatePartyId.ToString(), party.GetProperty("template_party_id").GetString());
            Assert.False(party.TryGetProperty("email", out _));
        }
        Assert.Equal("anahtar-slotu", call.IdempotencyKey);
        Assert.Equal("[0]", call.Order);
        Assert.Equal("baslik-slotu", call.Title);
        Assert.Equal("aciklama-slotu", call.Description);
        Assert.Equal(fieldTemplateId, call.FieldTemplateId);
        Assert.Equal("true", call.Force);
        Assert.Equal("sms", call.SendInvitations);
        Assert.Equal("drop", call.OnAnchorMiss);
    }

    [Fact]
    public async Task Demands_UploadDocumentAsync_leaves_optional_fields_unset_by_default()
    {
        var api = new Mock<IDemandsApi>();
        var call = CaptureUpload(api);

        await Demands(api).UploadDocumentAsync(new UploadDemandParams
        {
            Files = new[] { new FileInput { Content = "pdf"u8.ToArray(), FileName = "a.pdf" } },
            Parties = new[] { new UploadPartyInput { FirstName = "Ayşe", LastName = "Yılmaz", Email = "ayse@example.com" } },
            Force = false,
        });

        Assert.Equal("""[{"first_name":"Ayşe","last_name":"Yılmaz","email":"ayse@example.com"}]""", call.Parties);
        Assert.Null(call.IdempotencyKey);
        Assert.Null(call.Order);
        Assert.Null(call.Title);
        Assert.Null(call.Description);
        Assert.Null(call.FieldTemplateId);
        Assert.Null(call.Force);
        // Invitations stay off on this endpoint unless asked for.
        Assert.Null(call.SendInvitations);
        Assert.Null(call.OnAnchorMiss);
    }


    // ---- demands: template document selection -----------------------------------

    private static ApiV1DemandsPost201Response CreatedResponse() =>
        new(true, new CreatedDemand(id: Guid.NewGuid()));

    [Fact]
    public async Task Demands_CreateAsync_forwards_the_document_selection_untouched()
    {
        var includedId = Guid.NewGuid();
        var excludedId = Guid.NewGuid();
        var body = new CreateDemandRequest(
            templateId: Guid.NewGuid(),
            partyMapping: new List<PartyMappingInput>(),
            documents: new DocumentSelectionInput(include: new List<Guid> { includedId }, exclude: new List<Guid> { excludedId }));
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsPostAsync(body, null, It.IsAny<CancellationToken>())).ReturnsAsync(CreatedResponse());

        await Demands(api).CreateAsync(body);

        api.Verify(a => a.ApiV1DemandsPostAsync(body, null, It.IsAny<CancellationToken>()), Times.Once);
        Assert.Equal(new List<Guid> { includedId }, body.Documents.Include);
        Assert.Equal(new List<Guid> { excludedId }, body.Documents.Exclude);
        Assert.Contains(@"""documents""", body.ToJson());
        Assert.Contains(includedId.ToString(), body.ToJson());
    }

    [Fact]
    public async Task Demands_CreateAsync_without_a_selection_sends_the_same_payload_as_before()
    {
        var body = new CreateDemandRequest(templateId: Guid.NewGuid(), partyMapping: new List<PartyMappingInput>());
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsPostAsync(body, null, It.IsAny<CancellationToken>())).ReturnsAsync(CreatedResponse());

        await Demands(api).CreateAsync(body);

        api.Verify(a => a.ApiV1DemandsPostAsync(body, null, It.IsAny<CancellationToken>()), Times.Once);
        Assert.Null(body.Documents);
        Assert.DoesNotContain(@"""documents"": {", body.ToJson());
    }

    [Fact]
    public async Task Demands_CreateBulkAsync_carries_the_selection_per_row()
    {
        var excludedId = Guid.NewGuid();
        var body = new ApiV1DemandsBulkPostRequest(
            templateId: Guid.NewGuid(),
            rows: new List<ApiV1DemandsBulkPostRequestRowsInner>
            {
                new(
                    partyMapping: new List<ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner>(),
                    documents: new DocumentSelectionInput(exclude: new List<Guid> { excludedId })),
                new(partyMapping: new List<ApiV1DemandsBulkPostRequestRowsInnerPartyMappingInner>()),
            });
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsBulkPostAsync(body, null, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1DemandsBulkPost200Response(true, new ApiV1DemandsBulkPost200ResponseData(created: 1)));

        await Demands(api).CreateBulkAsync(body);

        api.Verify(a => a.ApiV1DemandsBulkPostAsync(body, null, It.IsAny<CancellationToken>()), Times.Once);
        Assert.Equal(new List<Guid> { excludedId }, body.Rows[0].Documents.Exclude);
        Assert.Null(body.Rows[1].Documents);
    }

    [Fact]
    public async Task Templates_GetAsync_returns_the_documents_the_ids_come_from()
    {
        var templateId = Guid.NewGuid();
        var documentId = Guid.NewGuid();
        var roleId = Guid.NewGuid();
        var detail = new TemplateDetail(
            id: templateId,
            documents: new List<TemplateDocumentSummary>
            {
                new(
                    id: documentId,
                    order: 1,
                    title: "Sözleşme",
                    docKind: TemplateDocumentSummary.DocKindEnum.CONTRACT,
                    isRequired: true,
                    signatureRequired: true,
                    defaultIncluded: true,
                    assignedTemplatePartyIds: new List<Guid> { roleId }),
            });
        var templates = new Mock<ITemplatesApi>();
        templates.Setup(a => a.ApiV1TemplatesIdGetAsync(templateId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new ApiV1TemplatesIdGet200Response(true, detail));

        var documents = (await new TemplatesResource(templates.Object, NoWaitRetry).GetAsync(templateId)).Documents;

        Assert.Equal(documentId, documents[0].Id);
        Assert.True(documents[0].DefaultIncluded);
        Assert.Equal(new List<Guid> { roleId }, documents[0].AssignedTemplatePartyIds);
    }

    [Fact]
    public async Task Demands_CreateAsync_maps_a_rejected_document_selection()
    {
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsPostAsync(It.IsAny<CreateDemandRequest>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new GeneratedApiException(
                400,
                "bad request",
                """{"success":false,"error":"Belge seçimi geçersiz","code":"INVALID_DOCUMENT_SELECTION","details":{"reason":"unknown_document"}}""",
                new GeneratedMultimap()));

        var body = new CreateDemandRequest(
            templateId: Guid.NewGuid(),
            partyMapping: new List<PartyMappingInput>(),
            documents: new DocumentSelectionInput(include: new List<Guid> { Guid.NewGuid() }));

        var err = await Assert.ThrowsAsync<ImzalaError>(() => Demands(api).CreateAsync(body));
        Assert.Equal(400, err.StatusCode);
        Assert.Equal("INVALID_DOCUMENT_SELECTION", err.Code);
        Assert.Contains("unknown_document", err.CodeDescription);
    }

    [Fact]
    public async Task Demands_CreateAsync_maps_a_party_left_without_documents()
    {
        var api = new Mock<IDemandsApi>();
        api.Setup(a => a.ApiV1DemandsPostAsync(It.IsAny<CreateDemandRequest>(), It.IsAny<string?>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new GeneratedApiException(
                409,
                "conflict",
                """{"success":false,"error":"Eşlenen bir tarafa imzalayacak belge düşmüyor","code":"PARTY_WITHOUT_DOCUMENTS"}""",
                new GeneratedMultimap()));

        var body = new CreateDemandRequest(
            templateId: Guid.NewGuid(),
            partyMapping: new List<PartyMappingInput>(),
            documents: new DocumentSelectionInput(exclude: new List<Guid> { Guid.NewGuid() }));

        var err = await Assert.ThrowsAsync<ImzalaError>(() => Demands(api).CreateAsync(body));
        Assert.Equal(409, err.StatusCode);
        Assert.Equal("PARTY_WITHOUT_DOCUMENTS", err.Code);
    }

    // ---- client surface ---------------------------------------------------------

    [Fact]
    public void Client_exposes_the_new_resources()
    {
        var imzala = new Imzala("imz_test");
        Assert.NotNull(imzala.FieldTemplates);
        Assert.NotNull(imzala.Contacts);
        Assert.NotNull(imzala.Reports);
    }
}
