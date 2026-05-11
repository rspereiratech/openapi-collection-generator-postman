package io.github.rspereiratech.openapi.collection.generator.postman.url;

import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanUrl;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

/**
 * Builds a {@link PostmanUrl} from an OpenAPI operation and path.
 */
public interface UrlBuilder {

    /**
     * Builds a Postman URL for the given API path and operation.
     *
     * @param path      the API path (e.g. "/pets/{id}")
     * @param operation the OpenAPI operation
     * @param baseUrl   the base URL for requests
     * @param openApi   the full OpenAPI specification
     * @return a {@link PostmanUrl} representing the request URL
     */
    PostmanUrl build(String path, Operation operation, String baseUrl, OpenAPI openApi);
}
