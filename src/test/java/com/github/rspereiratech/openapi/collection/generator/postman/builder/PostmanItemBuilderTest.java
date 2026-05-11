package com.github.rspereiratech.openapi.collection.generator.postman.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.github.rspereiratech.openapi.collection.generator.core.deprecated.DeprecationMarker;
import com.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionContext;
import com.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionProcessorChain;
import com.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionResult;
import com.github.rspereiratech.openapi.collection.generator.core.link.LinkDescriptionEnricher;
import com.github.rspereiratech.openapi.collection.generator.postman.body.BodyBuilder;
import com.github.rspereiratech.openapi.collection.generator.postman.header.HeaderBuilder;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanBody;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanBodyOptions;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanBodyRaw;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanUrl;
import com.github.rspereiratech.openapi.collection.generator.postman.url.UrlBuilder;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.links.Link;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;

class PostmanItemBuilderTest {

    private final UrlBuilder urlBuilder = mock(UrlBuilder.class);
    private final HeaderBuilder headerBuilder = mock(HeaderBuilder.class);
    private final BodyBuilder bodyBuilder = mock(BodyBuilder.class);
    private final DeprecationMarker deprecationMarker = mock(DeprecationMarker.class);
    private final ExtensionProcessorChain extensionChain = mock(ExtensionProcessorChain.class);
    private final LinkDescriptionEnricher linkEnricher = mock(LinkDescriptionEnricher.class);

    private final PostmanItemBuilder builder = new PostmanItemBuilder(
            urlBuilder, headerBuilder, bodyBuilder,
            deprecationMarker, extensionChain, linkEnricher);

    private final PostmanUrl url = new PostmanUrl("raw", List.of(), List.of(), List.of());
    private final PostmanBody body = new PostmanBody("raw", "{}",
            new PostmanBodyOptions(new PostmanBodyRaw("json")));

    private void stubCommon() {
        when(urlBuilder.build(any(), any(), any(), any())).thenReturn(url);
        when(headerBuilder.build(any(), any())).thenReturn(List.of());
        when(bodyBuilder.build(any(), any())).thenReturn(body);
        when(linkEnricher.enrich(any(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(deprecationMarker.markName(anyString(), anyBoolean()))
                .thenAnswer(inv -> inv.getArgument(0));
        when(deprecationMarker.markDescription(anyString(), anyBoolean()))
                .thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void build_usesSummaryAsName_whenPresent() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());
        Operation op = new Operation().summary("List pets").description("desc");

        PostmanItem item = builder.build("/pets", "GET", op, "http://example.com", new OpenAPI());

        assertEquals("List pets", item.name());
        assertNotNull(item.request());
        assertEquals("GET", item.request().method());
        assertSame(url, item.request().url());
        assertSame(body, item.request().body());
    }

    @Test
    void build_fallsBackToMethodAndPath_whenSummaryAbsent() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());

        PostmanItem item = builder.build("/pets", "POST", new Operation(), "http://example.com", new OpenAPI());

        assertEquals("POST /pets", item.name());
    }

    @Test
    void build_appliesNameOverrideFromExtension() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(new ExtensionResult("Custom Name", null));

        PostmanItem item = builder.build("/pets", "GET",
                new Operation().summary("Original"), "http://example.com", new OpenAPI());

        assertEquals("Custom Name", item.name());
    }

    @Test
    void build_appendsExtensionDescription() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(new ExtensionResult(null, "Extra info"));

        PostmanItem item = builder.build("/pets", "GET",
                new Operation().description("Base"), "http://example.com", new OpenAPI());

        assertEquals("Base\n\nExtra info", item.request().description());
    }

    @Test
    void build_appendsExtensionDescription_whenBaseEmpty() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(new ExtensionResult(null, "Extra info"));

        PostmanItem item = builder.build("/pets", "GET", new Operation(), "http://example.com", new OpenAPI());

        assertEquals("\n\nExtra info", item.request().description());
    }

    @Test
    void build_marksNameAndDescription_whenDeprecated() {
        when(urlBuilder.build(any(), any(), any(), any())).thenReturn(url);
        when(headerBuilder.build(any(), any())).thenReturn(List.of());
        when(bodyBuilder.build(any(), any())).thenReturn(body);
        when(linkEnricher.enrich(any(), any())).thenAnswer(inv -> inv.getArgument(0));
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());
        when(deprecationMarker.markName(eq("List pets"), eq(true))).thenReturn("[D] List pets");
        when(deprecationMarker.markDescription(eq("desc"), eq(true))).thenReturn("[D] desc");

        PostmanItem item = builder.build("/pets", "GET",
                new Operation().summary("List pets").description("desc").deprecated(true),
                "http://example.com", new OpenAPI());

        assertEquals("[D] List pets", item.name());
        assertEquals("[D] desc", item.request().description());
        verify(deprecationMarker).markName("List pets", true);
        verify(deprecationMarker).markDescription("desc", true);
    }

    @Test
    void build_doesNotMarkDeprecated_whenFalse() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());

        builder.build("/pets", "GET", new Operation().summary("x"),
                "http://example.com", new OpenAPI());

        verify(deprecationMarker).markName("x", false);
    }

    @Test
    void build_collectsLinksFromAllResponses() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());

        ApiResponse ok = new ApiResponse().link("getById", new Link().operationId("getById"));
        ApiResponse created = new ApiResponse().link("self", new Link().operationId("self"));
        Operation op = new Operation().responses(new ApiResponses()
                .addApiResponse("200", ok).addApiResponse("201", created));

        builder.build("/pets", "GET", op, "http://example.com", new OpenAPI());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Link>> captor = ArgumentCaptor.forClass(Map.class);
        verify(linkEnricher).enrich(any(), captor.capture());
        Map<String, Link> links = captor.getValue();
        assertEquals(2, links.size());
        assertNotNull(links.get("getById"));
        assertNotNull(links.get("self"));
    }

    @Test
    void build_passesEmptyMap_whenResponsesNull() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());

        builder.build("/pets", "GET", new Operation(), "http://example.com", new OpenAPI());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Link>> captor = ArgumentCaptor.forClass(Map.class);
        verify(linkEnricher).enrich(any(), captor.capture());
        assertEquals(Map.of(), captor.getValue());
    }

    @Test
    void build_passesContextToExtensionChain() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());
        Operation op = new Operation().summary("Sum").description("Desc");

        builder.build("/pets/{id}", "PUT", op, "http://example.com", new OpenAPI());

        ArgumentCaptor<ExtensionContext> captor = ArgumentCaptor.forClass(ExtensionContext.class);
        verify(extensionChain).process(captor.capture());
        ExtensionContext ctx = captor.getValue();
        assertEquals("/pets/{id}", ctx.path());
        assertEquals("PUT", ctx.httpMethod());
        assertEquals("Sum", ctx.currentName());
        assertEquals("Desc", ctx.currentDescription());
        assertSame(op, ctx.operation());
    }

    @Test
    void build_skipsResponseLinks_whenLinksFieldNull() {
        stubCommon();
        when(extensionChain.process(any())).thenReturn(ExtensionResult.noChange());
        Operation op = new Operation().responses(new ApiResponses().addApiResponse("200", new ApiResponse()));

        builder.build("/pets", "GET", op, "http://example.com", new OpenAPI());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Link>> captor = ArgumentCaptor.forClass(Map.class);
        verify(linkEnricher).enrich(any(), captor.capture());
        assertEquals(0, captor.getValue().size());
    }
}
