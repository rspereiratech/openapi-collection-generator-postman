package com.github.rspereiratech.openapi.collection.generator.postman.builder;

import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Builds a {@link PostmanItem} representing a single API operation.
 */
public interface ItemBuilder {

    /**
     * Builds a Postman request item from an OpenAPI operation.
     *
     * @param path       the API path (e.g. "/pets/{id}")
     * @param httpMethod the HTTP method (e.g. "GET", "POST")
     * @param operation  the OpenAPI operation definition
     * @param baseUrl    the base URL for requests
     * @param openApi    the full OpenAPI specification
     * @return a {@link PostmanItem} representing the operation
     */
    PostmanItem build(String path, String httpMethod, Operation operation, String baseUrl, OpenAPI openApi);
}
