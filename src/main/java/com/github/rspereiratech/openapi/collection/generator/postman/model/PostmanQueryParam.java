package com.github.rspereiratech.openapi.collection.generator.postman.model;

/**
 * Represents a query parameter in a Postman request URL.
 *
 * @param key         the parameter name
 * @param value       the parameter value
 * @param description a human-readable description of the parameter
 */
public record PostmanQueryParam(String key, String value, String description) {}
