package io.github.rspereiratech.openapi.collection.generator.postman.model;

/**
 * Represents the body of a Postman request.
 *
 * @param mode    the body mode (e.g. "raw")
 * @param raw     the raw body content string
 * @param options body options such as language settings
 */
public record PostmanBody(String mode, String raw, PostmanBodyOptions options) {}
