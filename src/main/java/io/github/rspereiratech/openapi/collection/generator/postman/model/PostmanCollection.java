package io.github.rspereiratech.openapi.collection.generator.postman.model;

import java.util.List;

/**
 * Represents a Postman Collection containing metadata, request items, and variables.
 *
 * @param info     collection metadata such as name, description, and schema version
 * @param item     top-level items (folders or requests) in the collection
 * @param variable collection-level variables available to all requests
 */
public record PostmanCollection(PostmanInfo info, List<PostmanItem> item, List<PostmanVariable> variable) {}
