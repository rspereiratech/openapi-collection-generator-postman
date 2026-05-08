package com.github.rspereiratech.openapi.collection.generator.postman.builder;

import com.github.rspereiratech.openapi.collection.generator.postman.body.BodyBuilder;
import com.github.rspereiratech.openapi.collection.generator.core.deprecated.DeprecationMarker;
import com.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionContext;
import com.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionProcessorChain;
import com.github.rspereiratech.openapi.collection.generator.core.extension.ExtensionResult;
import com.github.rspereiratech.openapi.collection.generator.postman.header.HeaderBuilder;
import com.github.rspereiratech.openapi.collection.generator.core.link.LinkDescriptionEnricher;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanRequest;
import com.github.rspereiratech.openapi.collection.generator.postman.url.UrlBuilder;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.links.Link;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link ItemBuilder} that assembles a {@link PostmanItem}
 * by delegating URL, header, and body construction to dedicated builders, and applying
 * deprecation markers, extension processing, and link enrichment.
 */
public class PostmanItemBuilder implements ItemBuilder {

    /**
     * Builder for the request URL including path segments and query parameters.
     */
    private final UrlBuilder url;

    /**
     * Builder for the request headers.
     */
    private final HeaderBuilder header;

    /**
     * Builder for the request body.
     */
    private final BodyBuilder body;

    /**
     * Marker used to flag deprecated operations in the item name and description.
     */
    private final DeprecationMarker depr;

    /**
     * Chain that processes vendor extensions ({@code x-*}) on each operation.
     */
    private final ExtensionProcessorChain extChain;

    /**
     * Enricher that appends OpenAPI link information to the item description.
     */
    private final LinkDescriptionEnricher linkEnricher;

    /**
     * Creates a new builder with the given collaborators.
     *
     * @param url          the URL builder
     * @param header       the header builder
     * @param body         the body builder
     * @param depr         the deprecation marker
     * @param extChain     the extension processor chain
     * @param linkEnricher the link description enricher
     */
    public PostmanItemBuilder(UrlBuilder url, HeaderBuilder header, BodyBuilder body,
                              DeprecationMarker depr, ExtensionProcessorChain extChain,
                              LinkDescriptionEnricher linkEnricher) {
        this.url = url;
        this.header = header;
        this.body = body;
        this.depr = depr;
        this.extChain = extChain;
        this.linkEnricher = linkEnricher;
    }

    @Override
    public PostmanItem build(String path, String method, Operation op, String baseUrl, OpenAPI openApi) {
        boolean deprecated = Boolean.TRUE.equals(op.getDeprecated());
        var ext = extChain.process(new ExtensionContext(path, method, resolveName(op, method, path), resolveDescription(op), op));

        String name = depr.markName(applyNameOverride(ext, op, method, path), deprecated);
        String desc = depr.markDescription(enrichDescription(ext, op), deprecated);

        PostmanRequest request = new PostmanRequest(method, header.build(op, openApi),
                url.build(path, op, baseUrl, openApi), body.build(op, openApi), desc);
        return PostmanItem.request(name, request);
    }

    /**
     * Resolves the item name from the operation summary, falling back to the HTTP method and path.
     *
     * @param op     the OpenAPI operation
     * @param method the HTTP method
     * @param path   the request path
     * @return the resolved name
     */
    private String resolveName(Operation op, String method, String path) {
        return op.getSummary() != null ? op.getSummary() : method + " " + path;
    }

    /**
     * Resolves the item description from the operation, falling back to an empty string.
     *
     * @param op the OpenAPI operation
     * @return the resolved description
     */
    private String resolveDescription(Operation op) {
        return op.getDescription() != null ? op.getDescription() : "";
    }

    /**
     * Returns the extension name override if present, otherwise falls back to the resolved name.
     *
     * @param ext    the extension processing result
     * @param op     the OpenAPI operation
     * @param method the HTTP method
     * @param path   the request path
     * @return the final item name
     */
    private String applyNameOverride(ExtensionResult ext, Operation op, String method, String path) {
        return ext.nameOverride() != null ? ext.nameOverride() : resolveName(op, method, path);
    }

    /**
     * Builds the final description by appending extension text and enriching with response links.
     *
     * @param ext the extension processing result
     * @param op  the OpenAPI operation
     * @return the enriched description
     */
    private String enrichDescription(ExtensionResult ext, Operation op) {
        String desc = resolveDescription(op);
        if (ext.descriptionAppend() != null) {
            desc = desc + "\n\n" + ext.descriptionAppend();
        }
        return linkEnricher.enrich(desc, collectLinks(op));
    }

    /**
     * Collects all links from the operation's responses.
     *
     * @param op the OpenAPI operation
     * @return a map of link names to link definitions
     */
    private Map<String, Link> collectLinks(Operation op) {
        if (op.getResponses() == null) {
            return Map.of();
        }

        return op.getResponses().values().stream()
                .filter(r -> r.getLinks() != null)
                .flatMap(r -> r.getLinks().entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new));
    }
}
