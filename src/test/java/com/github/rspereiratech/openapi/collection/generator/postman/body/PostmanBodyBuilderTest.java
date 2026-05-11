package com.github.rspereiratech.openapi.collection.generator.postman.body;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.github.rspereiratech.openapi.collection.generator.core.example.SchemaExampleGenerator;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanBody;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.parameters.RequestBody;

class PostmanBodyBuilderTest {

    private final SchemaExampleGenerator generator = mock(SchemaExampleGenerator.class);
    private final PostmanBodyBuilder builder = new PostmanBodyBuilder(generator);

    @Test
    void build_returnsNull_whenOperationHasNoRequestBody() {
        assertNull(builder.build(new Operation(), new OpenAPI()));
    }

    @Test
    void build_returnsNull_whenRequestBodyContentIsEmpty() {
        Operation op = new Operation().requestBody(new RequestBody().content(new Content()));
        assertNull(builder.build(op, new OpenAPI()));
    }

    @Test
    void build_prefersNamedExamples_whenPresent() {
        Map<String, Example> examples = new LinkedHashMap<>();
        examples.put("default", new Example().value(Map.of("id", 42)));
        MediaType mt = new MediaType().examples(examples)
                .example(Map.of("ignored", true))
                .schema(new ObjectSchema());
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", mt)));

        PostmanBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertEquals("raw", body.mode());
        assertNotNull(body.options());
        assertEquals("json", body.options().raw().language());
        assertTrue(body.raw().contains("\"id\" : 42"));
    }

    @Test
    void build_usesSingleExample_whenNamedExamplesAbsent() {
        MediaType mt = new MediaType().example(Map.of("name", "Rex"));
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", mt)));

        PostmanBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertEquals("raw", body.mode());
        assertTrue(body.raw().contains("\"name\" : \"Rex\""));
    }

    @Test
    void build_fallsBackToSchemaGenerator_whenNoExamplesProvided() {
        MediaType mt = new MediaType().schema(new ObjectSchema());
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", mt)));
        when(generator.generate(any(), any())).thenReturn(Map.of("foo", "bar"));

        PostmanBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertTrue(body.raw().contains("\"foo\" : \"bar\""));
    }

    @Test
    void build_skipsNamedExamples_whenValueIsNull() {
        Map<String, Example> examples = new LinkedHashMap<>();
        examples.put("default", new Example());
        MediaType mt = new MediaType().examples(examples).example(Map.of("fallback", "value"));
        Operation op = new Operation().requestBody(new RequestBody()
                .content(new Content().addMediaType("application/json", mt)));

        PostmanBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertTrue(body.raw().contains("\"fallback\" : \"value\""));
    }

    @Test
    void build_usesFirstMediaType_whenMultiplePresent() {
        MediaType json = new MediaType().example(Map.of("k", "json"));
        MediaType xml = new MediaType().example(Map.of("k", "xml"));
        Content content = new Content();
        content.addMediaType("application/json", json);
        content.addMediaType("application/xml", xml);
        Operation op = new Operation().requestBody(new RequestBody().content(content));

        PostmanBody body = builder.build(op, new OpenAPI());

        assertNotNull(body);
        assertTrue(body.raw().contains("\"k\" : \"json\""));
    }
}
