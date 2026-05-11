package io.github.rspereiratech.openapi.collection.generator.postman.header;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.HttpHeader;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.HttpQueryParam;
import io.github.rspereiratech.openapi.collection.generator.core.security.model.SecurityInjection;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanHeader;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;

class PostmanHeaderBuilderTest {

    private final SecurityApplier securityApplier = mock(SecurityApplier.class);
    private final PostmanHeaderBuilder builder = new PostmanHeaderBuilder(securityApplier);

    @Test
    void build_returnsEmptyList_whenOperationHasNothing() {
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        List<PostmanHeader> headers = builder.build(new Operation(), new OpenAPI());

        assertTrue(headers.isEmpty());
    }

    @Test
    void build_includesHeaderParameters() {
        Operation op = new Operation()
                .addParametersItem(new Parameter().name("X-Trace").in("header"))
                .addParametersItem(new Parameter().name("ignored").in("query"));
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        List<PostmanHeader> headers = builder.build(op, new OpenAPI());

        assertEquals(1, headers.size());
        assertEquals("X-Trace", headers.get(0).key());
        assertEquals("", headers.get(0).value());
    }

    @Test
    void build_addsContentTypeFromRequestBody() {
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", new MediaType())));
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        List<PostmanHeader> headers = builder.build(op, new OpenAPI());

        assertEquals(1, headers.size());
        assertEquals("Content-Type", headers.get(0).key());
        assertEquals("application/json", headers.get(0).value());
    }

    @Test
    void build_skipsContentType_whenRequestBodyContentIsEmpty() {
        Operation op = new Operation().requestBody(new RequestBody().content(new Content()));
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        List<PostmanHeader> headers = builder.build(op, new OpenAPI());

        assertTrue(headers.isEmpty());
    }

    @Test
    void build_appendsSecurityHeaders() {
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection(
                List.of(new HttpHeader("Authorization", "Bearer {{token}}")),
                List.of(new HttpQueryParam("apiKey", "{{key}}")),
                List.of()));

        List<PostmanHeader> headers = builder.build(new Operation(), new OpenAPI());

        assertEquals(1, headers.size());
        assertEquals("Authorization", headers.get(0).key());
        assertEquals("Bearer {{token}}", headers.get(0).value());
    }

    @Test
    void build_returnsUnmodifiableList() {
        when(securityApplier.apply(any(), any())).thenReturn(new SecurityInjection());

        List<PostmanHeader> headers = builder.build(new Operation(), new OpenAPI());

        assertThrows(UnsupportedOperationException.class,
                () -> headers.add(new PostmanHeader("X", "Y")));
    }
}
