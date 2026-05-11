package io.github.rspereiratech.openapi.collection.generator.postman.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.HttpQueryParam;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.SecurityInjection;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanUrl;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.parameters.Parameter;

class PostmanUrlBuilderTest {

    private final SecurityApplier securityApplier = mock(SecurityApplier.class);
    private final PostmanUrlBuilder builder = new PostmanUrlBuilder(securityApplier);

    @Test
    void build_buildsBaseUrl_withNoParameters() {
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        PostmanUrl url = builder.build("/pets", new Operation(), "http://example.com", new OpenAPI());

        assertEquals("{{baseUrl}}/pets", url.raw());
        assertEquals(List.of("{{baseUrl}}"), url.host());
        assertEquals(List.of("pets"), url.path());
        assertTrue(url.query().isEmpty());
    }

    @Test
    void build_replacesPathParametersWithColonSyntax() {
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        PostmanUrl url = builder.build("/pets/{id}/owners/{ownerId}", new Operation(),
                "http://example.com", new OpenAPI());

        assertEquals("{{baseUrl}}/pets/:id/owners/:ownerId", url.raw());
        assertEquals(List.of("pets", ":id", "owners", ":ownerId"), url.path());
    }

    @Test
    void build_includesQueryParametersFromOperation() {
        Operation op = new Operation()
                .addParametersItem(new Parameter().name("limit").in("query").description("max"))
                .addParametersItem(new Parameter().name("X-Trace").in("header"));
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        PostmanUrl url = builder.build("/pets", op, "http://example.com", new OpenAPI());

        assertEquals(1, url.query().size());
        assertEquals("limit", url.query().get(0).key());
        assertEquals("", url.query().get(0).value());
        assertEquals("max", url.query().get(0).description());
        assertTrue(url.raw().contains("?limit="));
    }

    @Test
    void build_defaultsQueryParamDescriptionToEmpty_whenAbsent() {
        Operation op = new Operation()
                .addParametersItem(new Parameter().name("page").in("query"));
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        PostmanUrl url = builder.build("/pets", op, "http://example.com", new OpenAPI());

        assertEquals("", url.query().get(0).description());
    }

    @Test
    void build_appendsSecurityInjectedQueryParameters() {
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection(
                List.of(),
                List.of(new HttpQueryParam("api_key", "{{apiKey}}")),
                List.of()));

        PostmanUrl url = builder.build("/pets", new Operation(),
                "http://example.com", new OpenAPI());

        assertEquals(1, url.query().size());
        assertEquals("api_key", url.query().get(0).key());
        assertEquals("{{apiKey}}", url.query().get(0).value());
        assertEquals("security", url.query().get(0).description());
        assertTrue(url.raw().contains("?api_key={{apiKey}}"));
    }

    @Test
    void build_combinesOperationAndSecurityQueryParams() {
        Operation op = new Operation().addParametersItem(
                new Parameter().name("limit").in("query"));
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection(
                List.of(),
                List.of(new HttpQueryParam("api_key", "{{apiKey}}")),
                List.of()));

        PostmanUrl url = builder.build("/pets", op, "http://example.com", new OpenAPI());

        assertEquals(2, url.query().size());
        assertEquals("limit", url.query().get(0).key());
        assertEquals("api_key", url.query().get(1).key());
        assertTrue(url.raw().contains("?limit=&api_key={{apiKey}}"));
    }

    @Test
    void build_filtersBlankPathSegments() {
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        PostmanUrl url = builder.build("/", new Operation(),
                "http://example.com", new OpenAPI());

        assertTrue(url.path().isEmpty());
        assertEquals("{{baseUrl}}/", url.raw());
    }
}
