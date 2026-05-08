package com.github.rspereiratech.openapi.collection.generator.postman.model;

/**
 * Configuration for a raw Postman request body, specifying the content language.
 *
 * @param language the language of the raw body content (e.g. "json")
 */
public record PostmanBodyRaw(String language) {}
