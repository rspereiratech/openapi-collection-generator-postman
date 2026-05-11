package io.github.rspereiratech.openapi.collection.generator.postman.model;

import java.util.List;

/**
 * Represents a Postman request URL, including the raw string, host segments, path segments, and query parameters.
 *
 * @param raw   the full raw URL string (may contain Postman variables)
 * @param host  the host segments (e.g. {@code ["{{baseUrl}}"]})
 * @param path  the path segments
 * @param query the query parameters
 */
public record PostmanUrl(String raw, List<String> host, List<String> path, List<PostmanQueryParam> query) {}
