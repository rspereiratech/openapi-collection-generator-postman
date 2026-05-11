package io.github.rspereiratech.openapi.collection.generator.postman.model;

/**
 * Represents a single HTTP header in a Postman request.
 *
 * @param key   the header name
 * @param value the header value
 */
public record PostmanHeader(String key, String value) {}
