package io.github.rspereiratech.openapi.collection.generator.postman.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.github.rspereiratech.openapi.collection.generator.core.config.GenerationConfig;
import io.github.rspereiratech.openapi.collection.generator.core.generator.AdditionalFile;
import io.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerationException;
import io.github.rspereiratech.openapi.collection.generator.core.generator.CollectionGenerator;
import io.github.rspereiratech.openapi.collection.generator.postman.grouper.OperationGrouper;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanCollection;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanInfo;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanVariable;
import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.github.rspereiratech.openapi.collection.generator.core.serializer.CollectionSerializer;
import io.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironment;
import io.github.rspereiratech.openapi.collection.generator.core.server.ServerEnvironmentGenerator;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Generates a Postman Collection JSON file and optional environment files from an OpenAPI specification.
 * Implements {@link CollectionGenerator} to integrate with the plugin's generation pipeline.
 */
public class PostmanCollectionGenerator implements CollectionGenerator {

    /**
     * Postman Collection v2.1.0 schema URL.
     */
    private static final String SCHEMA =
            "https://schema.getpostman.com/json/collection/v2.1.0/collection.json";

    /**
     * Grouper that organizes operations into tag-based folders.
     */
    private final OperationGrouper grouper;

    /**
     * Serializer used to convert the collection model to JSON.
     */
    private final CollectionSerializer serializer;

    /**
     * Applier used to resolve security schemes and inject variables.
     */
    private final SecurityApplier securityApplier;

    /**
     * Generator that produces server environment definitions from the OpenAPI spec.
     */
    private final ServerEnvironmentGenerator serverEnvGenerator;

    /**
     * Jackson mapper configured for pretty-printed environment JSON output.
     */
    private final ObjectMapper envMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Creates a new Postman collection generator.
     *
     * @param grouper          the operation grouper for organizing items into folders
     * @param serializer       the serializer for converting the collection model to JSON
     * @param securityApplier  the security applier for injecting authentication variables
     * @param serverEnvGen     the server environment generator for resolving base URLs
     */
    public PostmanCollectionGenerator(OperationGrouper grouper, CollectionSerializer serializer,
                                      SecurityApplier securityApplier,
                                      ServerEnvironmentGenerator serverEnvGen) {
        this.grouper = grouper;
        this.serializer = serializer;
        this.securityApplier = securityApplier;
        this.serverEnvGenerator = serverEnvGen;
    }

    @Override
    public String generate(OpenAPI openApi, GenerationConfig config) throws CollectionGenerationException {
        try {
            String name = resolveName(openApi, config);
            String baseUrl = serverEnvGenerator.generate(openApi, name).stream()
                    .findFirst().map(ServerEnvironment::baseUrl).orElse("{{baseUrl}}");
            var info = new PostmanInfo(name,
                    Optional.ofNullable(openApi.getInfo().getDescription()).orElse(""), SCHEMA);
            var vars = buildVariables(baseUrl, openApi);
            var folders = grouper.group(openApi, baseUrl).entrySet().stream()
                    .map(e -> PostmanItem.folder(e.getKey(), e.getValue()))
                    .collect(Collectors.toList());
            return serializer.serialize(new PostmanCollection(info, folders, vars));
        } catch (Exception e) {
            throw new CollectionGenerationException("Postman generation failed", e);
        }
    }

    @Override
    public List<AdditionalFile> generateAdditionalFiles(OpenAPI openApi, GenerationConfig config)
            throws CollectionGenerationException {
        try {
            String name = resolveName(openApi, config);
            List<AdditionalFile> files = new ArrayList<>();
            for (var env : serverEnvGenerator.generate(openApi, name)) {
                List<Map<String, Object>> values = new ArrayList<>();
                values.add(Map.of("key", "baseUrl", "value", env.baseUrl(),
                        "enabled", true, "type", "default"));
                securityApplier.applyGlobal(openApi).variables().forEach(v ->
                        values.add(Map.of("key", v.name(), "value", v.placeholder(),
                                "enabled", true, "type", "secret")));
                files.add(new AdditionalFile(env.fileName(), envMapper.writeValueAsString(
                        Map.of("name", env.name(), "values", values,
                                "_postman_variable_scope", "environment"))));
            }
            return files;
        } catch (Exception e) {
            throw new CollectionGenerationException("Postman environment generation failed", e);
        }
    }

    /**
     * Builds collection-level variables including the base URL and security-related variables.
     *
     * @param baseUrl the resolved base URL
     * @param openApi the full OpenAPI specification
     * @return an unmodifiable list of collection variables
     */
    private List<PostmanVariable> buildVariables(String baseUrl, OpenAPI openApi) {
        List<PostmanVariable> vars = new ArrayList<>();
        vars.add(new PostmanVariable("baseUrl", baseUrl, "string"));

        securityApplier.applyGlobal(openApi).variables().stream()
                .map(v -> new PostmanVariable(v.name(), v.placeholder(), "string"))
                .forEach(vars::add);

        return Collections.unmodifiableList(vars);
    }

    /**
     * Resolves the collection name from the plugin config or falls back to the API title.
     *
     * @param api the OpenAPI specification
     * @param cfg the plugin configuration
     * @return the resolved collection name
     */
    private String resolveName(OpenAPI api, GenerationConfig cfg) {
        return Optional.ofNullable(cfg.collectionName()).orElse(api.getInfo().getTitle());
    }
}
