package com.github.rspereiratech.openapi.collection.generator.postman.url;

import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanQueryParam;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanUrl;
import com.github.rspereiratech.openapi.collection.generator.core.security.applier.SecurityApplier;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Default implementation of {@link UrlBuilder} that constructs a {@link PostmanUrl}
 * from OpenAPI path parameters, query parameters, and security-injected query parameters.
 */
public class PostmanUrlBuilder implements UrlBuilder {

    /**
     * Applier used to inject security-related query parameters into the URL.
     */
    private final SecurityApplier securityApplier;

    /**
     * Creates a new URL builder with the given security applier.
     *
     * @param sec the security applier used to inject authentication query parameters
     */
    public PostmanUrlBuilder(SecurityApplier sec) {
        this.securityApplier = sec;
    }

    @Override
    public PostmanUrl build(String path, Operation op, String baseUrl, OpenAPI openApi) {
        var inj = securityApplier.apply(op, openApi);
        List<PostmanQueryParam> qp = Stream.concat(
                Optional.ofNullable(op.getParameters()).orElse(List.of()).stream()
                        .filter(p -> "query".equals(p.getIn()))
                        .map(p -> new PostmanQueryParam(p.getName(), "",
                                Optional.ofNullable(p.getDescription()).orElse(""))),
                inj.queryParams().stream()
                        .map(q -> new PostmanQueryParam(q.name(), q.value(), "security"))
        ).collect(Collectors.toList());
        List<String> segs = Arrays.stream(path.split("/"))
                .filter(s -> !s.isBlank())
                .map(s -> s.startsWith("{") ? ":" + s.substring(1, s.length() - 1) : s)
                .collect(Collectors.toList());
        String raw = "{{baseUrl}}" + path.replace("{", ":").replace("}", "");
        if (!qp.isEmpty()) {
            raw += "?" + qp.stream()
                    .map(q -> q.key() + "=" + q.value())
                    .collect(Collectors.joining("&"));
        }
        return new PostmanUrl(raw, List.of("{{baseUrl}}"), segs, qp);
    }
}
