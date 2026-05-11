package com.github.rspereiratech.openapi.collection.generator.postman.generator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.github.rspereiratech.openapi.collection.generator.core.config.GenerationConfig;
import com.github.rspereiratech.openapi.collection.generator.core.generator.AdditionalFile;
import com.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerationException;
import com.github.rspereiratech.openapi.collection.generator.core.model.CollectionFormat;
import com.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import com.github.rspereiratech.openapi.collection.generator.core.security.model.EnvironmentVariable;
import com.github.rspereiratech.openapi.collection.generator.core.security.model.SecurityInjection;
import com.github.rspereiratech.openapi.collection.generator.core.serializer.CollectionSerializer;
import com.github.rspereiratech.openapi.collection.generator.core.serializer.SerializationException;
import com.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironment;
import com.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironmentGenerator;
import com.github.rspereiratech.openapi.collection.generator.postman.grouper.OperationGrouper;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanCollection;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanRequest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

class PostmanCollectionGeneratorTest {

    private final OperationGrouper grouper = mock(OperationGrouper.class);
    private final CollectionSerializer serializer = mock(CollectionSerializer.class);
    private final SecurityApplier securityApplier = mock(SecurityApplier.class);
    private final ServerEnvironmentGenerator serverEnvGen = mock(ServerEnvironmentGenerator.class);

    private final PostmanCollectionGenerator generator = new PostmanCollectionGenerator(
            grouper, serializer, securityApplier, serverEnvGen);

    private GenerationConfig config(String name) {
        return new GenerationConfig(new File("/tmp"), CollectionFormat.POSTMAN, name);
    }

    private OpenAPI api(String title, String description) {
        return new OpenAPI().info(new Info().title(title).description(description));
    }

    @Test
    void generate_buildsCollectionWithInfoAndBaseUrlVariable() throws Exception {
        OpenAPI api = api("Pet API", "An API");
        when(serverEnvGen.generate(any(), any()))
                .thenReturn(List.of(new ServerEnvironment("Prod", "http://prod", "prod.json")));
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(grouper.group(any(), any())).thenReturn(Map.of(
                "pets", List.of(PostmanItem.request("a", new PostmanRequest("GET", List.of(), null, null, "")))));
        when(serializer.serialize(any())).thenReturn("{}");

        String result = generator.generate(api, config("My Collection"));

        assertEquals("{}", result);
        ArgumentCaptor<PostmanCollection> captor = ArgumentCaptor.forClass(PostmanCollection.class);
        org.mockito.Mockito.verify(serializer).serialize(captor.capture());
        PostmanCollection coll = captor.getValue();
        assertEquals("My Collection", coll.info().name());
        assertEquals("An API", coll.info().description());
        assertEquals(1, coll.variable().size());
        assertEquals("baseUrl", coll.variable().get(0).key());
        assertEquals("http://prod", coll.variable().get(0).value());
    }

    @Test
    void generate_fallsBackToApiTitle_whenCollectionNameNull() throws Exception {
        OpenAPI api = api("API Title", "desc");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of());
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(grouper.group(any(), any())).thenReturn(Map.of());
        when(serializer.serialize(any())).thenReturn("{}");

        generator.generate(api, config(null));

        ArgumentCaptor<PostmanCollection> captor = ArgumentCaptor.forClass(PostmanCollection.class);
        org.mockito.Mockito.verify(serializer).serialize(captor.capture());
        assertEquals("API Title", captor.getValue().info().name());
    }

    @Test
    void generate_usesPlaceholderBaseUrl_whenNoServers() throws Exception {
        OpenAPI api = api("API", "desc");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of());
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(grouper.group(any(), any())).thenReturn(Map.of());
        when(serializer.serialize(any())).thenReturn("{}");

        generator.generate(api, config("X"));

        ArgumentCaptor<PostmanCollection> captor = ArgumentCaptor.forClass(PostmanCollection.class);
        org.mockito.Mockito.verify(serializer).serialize(captor.capture());
        assertEquals("{{baseUrl}}", captor.getValue().variable().get(0).value());
    }

    @Test
    void generate_defaultsEmptyDescription_whenInfoDescriptionNull() throws Exception {
        OpenAPI api = api("API", null);
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of());
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(grouper.group(any(), any())).thenReturn(Map.of());
        when(serializer.serialize(any())).thenReturn("{}");

        generator.generate(api, config("X"));

        ArgumentCaptor<PostmanCollection> captor = ArgumentCaptor.forClass(PostmanCollection.class);
        org.mockito.Mockito.verify(serializer).serialize(captor.capture());
        assertEquals("", captor.getValue().info().description());
    }

    @Test
    void generate_addsSecurityVariables() throws Exception {
        OpenAPI api = api("API", "d");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of());
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection(
                List.of(), List.of(),
                List.of(new EnvironmentVariable("token", "{{token}}"))));
        when(grouper.group(any(), any())).thenReturn(Map.of());
        when(serializer.serialize(any())).thenReturn("{}");

        generator.generate(api, config("X"));

        ArgumentCaptor<PostmanCollection> captor = ArgumentCaptor.forClass(PostmanCollection.class);
        org.mockito.Mockito.verify(serializer).serialize(captor.capture());
        PostmanCollection coll = captor.getValue();
        assertEquals(2, coll.variable().size());
        assertEquals("token", coll.variable().get(1).key());
        assertEquals("{{token}}", coll.variable().get(1).value());
        assertEquals("string", coll.variable().get(1).type());
    }

    @Test
    void generate_buildsFoldersFromGrouper() throws Exception {
        OpenAPI api = api("API", "d");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of());
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection());
        PostmanItem item = PostmanItem.request("op", new PostmanRequest("GET", List.of(), null, null, ""));
        when(grouper.group(any(), any())).thenReturn(Map.of("pets", List.of(item)));
        when(serializer.serialize(any())).thenReturn("{}");

        generator.generate(api, config("X"));

        ArgumentCaptor<PostmanCollection> captor = ArgumentCaptor.forClass(PostmanCollection.class);
        org.mockito.Mockito.verify(serializer).serialize(captor.capture());
        PostmanCollection coll = captor.getValue();
        assertEquals(1, coll.item().size());
        assertEquals("pets", coll.item().get(0).name());
        assertNotNull(coll.item().get(0).item());
        assertSame(item, coll.item().get(0).item().get(0));
    }

    @Test
    void generate_wrapsExceptions_inCollectionGenerationException() throws Exception {
        OpenAPI api = api("API", "d");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of());
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection());
        when(grouper.group(any(), any())).thenReturn(Map.of());
        when(serializer.serialize(any())).thenThrow(new SerializationException("boom", new RuntimeException()));

        CollectionGenerationException ex = assertThrows(CollectionGenerationException.class,
                () -> generator.generate(api, config("X")));
        assertTrue(ex.getMessage().contains("Postman generation failed"));
    }

    @Test
    void generateAdditionalFiles_producesEnvironmentFilePerServer() throws Exception {
        OpenAPI api = api("API", "d");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of(
                new ServerEnvironment("Prod", "http://prod", "prod.json"),
                new ServerEnvironment("Dev", "http://dev", "dev.json")));
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection());

        List<AdditionalFile> files = generator.generateAdditionalFiles(api, config("X"));

        assertEquals(2, files.size());
        assertEquals("prod.json", files.get(0).fileName());
        assertEquals("dev.json", files.get(1).fileName());
        assertTrue(files.get(0).content().contains("\"name\" : \"Prod\""));
        assertTrue(files.get(0).content().contains("\"baseUrl\""));
        assertTrue(files.get(0).content().contains("http://prod"));
        assertTrue(files.get(0).content().contains("_postman_variable_scope"));
    }

    @Test
    void generateAdditionalFiles_includesSecurityVariablesAsSecret() throws Exception {
        OpenAPI api = api("API", "d");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of(
                new ServerEnvironment("Prod", "http://prod", "prod.json")));
        when(securityApplier.applyGlobal(any())).thenReturn(new SecurityInjection(
                List.of(), List.of(),
                List.of(new EnvironmentVariable("apiKey", "{{apiKey}}"))));

        List<AdditionalFile> files = generator.generateAdditionalFiles(api, config("X"));

        assertEquals(1, files.size());
        String content = files.get(0).content();
        assertTrue(content.contains("\"apiKey\""));
        assertTrue(content.contains("\"secret\""));
    }

    @Test
    void generateAdditionalFiles_returnsEmptyList_whenNoServers() throws Exception {
        OpenAPI api = api("API", "d");
        when(serverEnvGen.generate(any(), any())).thenReturn(List.of());

        List<AdditionalFile> files = generator.generateAdditionalFiles(api, config("X"));

        assertTrue(files.isEmpty());
    }

    @Test
    void generateAdditionalFiles_wrapsExceptions() {
        OpenAPI api = api("API", "d");
        when(serverEnvGen.generate(any(), any())).thenThrow(new RuntimeException("nope"));

        CollectionGenerationException ex = assertThrows(CollectionGenerationException.class,
                () -> generator.generateAdditionalFiles(api, config("X")));
        assertTrue(ex.getMessage().contains("Postman environment generation failed"));
    }
}
