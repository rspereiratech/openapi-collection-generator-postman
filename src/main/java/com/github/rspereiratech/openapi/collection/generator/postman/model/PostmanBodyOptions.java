package com.github.rspereiratech.openapi.collection.generator.postman.model;

/**
 * Options for a Postman request body, currently wrapping raw body settings.
 *
 * @param raw the raw body configuration (e.g. language)
 */
public record PostmanBodyOptions(PostmanBodyRaw raw) {}
