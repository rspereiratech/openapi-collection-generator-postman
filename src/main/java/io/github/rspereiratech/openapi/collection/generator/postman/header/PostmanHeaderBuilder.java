package io.github.rspereiratech.openapi.collection.generator.postman.header;

import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanHeader;
import io.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Default implementation of {@link HeaderBuilder} that builds Postman headers
 * from OpenAPI header parameters, request body content type, and security requirements.
 */
public class PostmanHeaderBuilder implements HeaderBuilder {

    /**
     * Applier used to inject security-related headers into the request.
     */
    private final SecurityApplier securityApplier;

    /**
     * Creates a new header builder with the given security applier.
     *
     * @param sec the security applier used to inject authentication headers
     */
    public PostmanHeaderBuilder(SecurityApplier sec) {
        this.securityApplier = sec;
    }

    @Override
    public List<PostmanHeader> build(Operation op, OpenAPI openApi) {
        List<PostmanHeader> h = new ArrayList<>();
        Optional.ofNullable(op.getParameters()).orElse(List.of()).stream()
                .filter(p -> "header".equals(p.getIn()))
                .map(p -> new PostmanHeader(p.getName(), ""))
                .forEach(h::add);

        Optional.ofNullable(op.getRequestBody())
                .map(rb -> rb.getContent())
                .filter(c -> !c.isEmpty())
                .map(c -> new PostmanHeader("Content-Type", c.keySet().iterator().next()))
                .ifPresent(h::add);

        securityApplier.apply(op, openApi).headers().stream()
                .map(s -> new PostmanHeader(s.name(), s.value()))
                .forEach(h::add);

        return Collections.unmodifiableList(h);
    }
}
