package io.github.rspereiratech.openapi.collection.generator.postman.body;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.rspereiratech.openapi.collection.generator.core.example.SchemaExampleGenerator;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanBody;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanBodyOptions;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanBodyRaw;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.MediaType;

import java.util.Optional;

/**
 * Default implementation of {@link BodyBuilder} that generates a raw JSON Postman body
 * from OpenAPI request body examples or schema-generated examples.
 */
public class PostmanBodyBuilder implements BodyBuilder {

    /**
     * Generator used to produce example values from OpenAPI schemas.
     */
    private final SchemaExampleGenerator gen;

    /**
     * Jackson mapper used to pretty-print request body examples.
     */
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Creates a new body builder with the given schema example generator.
     *
     * @param gen the generator used to produce example values from schemas
     */
    public PostmanBodyBuilder(SchemaExampleGenerator gen) {
        this.gen = gen;
    }

    @Override
    public PostmanBody build(Operation op, OpenAPI openApi) {
        return Optional.ofNullable(op.getRequestBody())
                .map(rb -> rb.getContent())
                .filter(c -> !c.isEmpty())
                .map(c -> buildBody(c.get(c.keySet().iterator().next()), openApi))
                .orElse(null);
    }

    /**
     * Builds a raw JSON body from the given media type, falling back to an empty JSON object on error.
     *
     * @param mt      the media type containing examples or schema
     * @param openApi the full OpenAPI specification
     * @return a {@link PostmanBody} with mode "raw" and JSON content
     */
    private PostmanBody buildBody(MediaType mt, OpenAPI openApi) {
        try {
            return new PostmanBody("raw", resolveRaw(mt, openApi),
                    new PostmanBodyOptions(new PostmanBodyRaw("json")));
        } catch (JsonProcessingException e) {
            return new PostmanBody("raw", "{}",
                    new PostmanBodyOptions(new PostmanBodyRaw("json")));
        }
    }

    /**
     * Resolves the raw body string from examples or schema generation.
     *
     * @param mt      the media type to extract examples from
     * @param openApi the full OpenAPI specification
     * @return a pretty-printed JSON string
     * @throws JsonProcessingException if JSON serialization fails
     */
    private String resolveRaw(MediaType mt, OpenAPI openApi) throws JsonProcessingException {
        if (mt.getExamples() != null && !mt.getExamples().isEmpty()) {
            var v = mt.getExamples().values().iterator().next().getValue();
            if (v != null) return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(v);
        }
        if (mt.getExample() != null) {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mt.getExample());
        }
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(gen.generate(mt.getSchema(), openApi));
    }
}
