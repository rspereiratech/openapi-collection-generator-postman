package com.github.rspereiratech.openapi.collection.generator.postman.model;

import java.util.List;

/**
 * Represents a Postman item, which can be either a folder (containing nested items) or a single request.
 *
 * @param name        the display name of the item
 * @param description an optional description
 * @param item        nested items when this is a folder; {@code null} for requests
 * @param request     the request definition when this is a request; {@code null} for folders
 */
public record PostmanItem(String name, String description, List<PostmanItem> item, PostmanRequest request) {

    /**
     * Creates a folder item containing the given child items.
     *
     * @param name  the folder name
     * @param items the child items within this folder
     * @return a new folder {@link PostmanItem}
     */
    public static PostmanItem folder(String name, List<PostmanItem> items) {
        return new PostmanItem(name, null, items, null);
    }

    /**
     * Creates a request item wrapping the given request definition.
     *
     * @param name the request display name
     * @param req  the request definition
     * @return a new request {@link PostmanItem}
     */
    public static PostmanItem request(String name, PostmanRequest req) {
        return new PostmanItem(name, null, null, req);
    }
}
