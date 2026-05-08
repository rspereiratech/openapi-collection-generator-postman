package com.github.rspereiratech.openapi.collection.generator.postman.grouper;

import com.github.rspereiratech.openapi.collection.generator.postman.builder.ItemBuilder;
import com.github.rspereiratech.openapi.collection.generator.core.callback.CallbackProcessor;
import com.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of {@link OperationGrouper} that groups operations by their first OpenAPI tag.
 * Operations without tags are placed in the "default" group. Callback operations are collected
 * into a separate "Callbacks" group.
 */
public class TagOperationGrouper implements OperationGrouper {

    /**
     * Builder used to create a Postman item for each operation.
     */
    private final ItemBuilder itemBuilder;

    /**
     * Processor used to extract callback paths from operations.
     */
    private final CallbackProcessor callbackProcessor;

    /**
     * Creates a new tag-based operation grouper.
     *
     * @param ib the item builder used to create Postman items from operations
     * @param cp the callback processor for extracting callback paths
     */
    public TagOperationGrouper(ItemBuilder ib, CallbackProcessor cp) {
        this.itemBuilder = ib;
        this.callbackProcessor = cp;
    }

    @Override
    public Map<String, List<PostmanItem>> group(OpenAPI openApi, String baseUrl) {
        Map<String, List<PostmanItem>> result = new LinkedHashMap<>();
        openApi.getPaths().forEach((path, pi) -> pi.readOperationsMap().forEach((method, op) -> {
            result.computeIfAbsent(resolveTag(op), k -> new ArrayList<>())
                    .add(itemBuilder.build(path, method.name(), op, baseUrl, openApi));
            if (op.getCallbacks() != null && !op.getCallbacks().isEmpty()) {
                String cbName = Optional.ofNullable(op.getSummary())
                        .orElse(method.name() + " " + path);
                callbackProcessor.extractCallbackPaths(op.getCallbacks(), cbName, openApi)
                        .forEach((cbPath, cbPi) -> cbPi.readOperationsMap()
                                .forEach((cbMethod, cbOp) ->
                                        result.computeIfAbsent("Callbacks", k -> new ArrayList<>())
                                                .add(itemBuilder.build(cbPath, cbMethod.name(),
                                                        cbOp, baseUrl, openApi))));
            }
        }));
        return result;
    }

    /**
     * Resolves the group tag for an operation, defaulting to "default" if no tags are present.
     *
     * @param op the OpenAPI operation
     * @return the first tag name, or "default"
     */
    private String resolveTag(Operation op) {
        return Optional.ofNullable(op.getTags())
                .filter(t -> !t.isEmpty())
                .map(t -> t.get(0))
                .orElse("default");
    }
}
