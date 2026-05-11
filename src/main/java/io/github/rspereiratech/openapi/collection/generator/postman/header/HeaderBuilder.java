package io.github.rspereiratech.openapi.collection.generator.postman.header;

import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanHeader;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.List;

/**
 * Builds a list of {@link PostmanHeader} instances for an API operation.
 */
public interface HeaderBuilder {

    /**
     * Builds the request headers for the given OpenAPI operation.
     *
     * @param operation the OpenAPI operation
     * @param openApi   the full OpenAPI specification
     * @return an unmodifiable list of Postman headers
     */
    List<PostmanHeader> build(Operation operation, OpenAPI openApi);
}
