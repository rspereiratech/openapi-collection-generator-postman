package com.github.rspereiratech.openapi.collection.generator.postman.grouper;

import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.List;
import java.util.Map;

/**
 * Groups OpenAPI operations into named collections of {@link PostmanItem} instances.
 */
public interface OperationGrouper {

    /**
     * Groups all operations from the OpenAPI specification into named folders.
     *
     * @param openApi the full OpenAPI specification
     * @param baseUrl the base URL for requests
     * @return a map of group names to their corresponding Postman items
     */
    Map<String, List<PostmanItem>> group(OpenAPI openApi, String baseUrl);
}
