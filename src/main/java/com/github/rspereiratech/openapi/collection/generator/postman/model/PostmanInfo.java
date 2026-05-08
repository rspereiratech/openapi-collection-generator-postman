package com.github.rspereiratech.openapi.collection.generator.postman.model;

/**
 * Represents the info block of a Postman Collection, holding its name, description, and schema URL.
 *
 * @param name        the collection name
 * @param description a human-readable description of the collection
 * @param schema      the Postman Collection schema URL (e.g. v2.1.0)
 */
public record PostmanInfo(String name, String description, String schema) {}
