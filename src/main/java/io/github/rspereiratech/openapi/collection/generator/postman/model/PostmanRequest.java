package io.github.rspereiratech.openapi.collection.generator.postman.model;

import java.util.List;

/**
 * Represents a Postman HTTP request with its method, headers, URL, body, and description.
 *
 * @param method      the HTTP method (e.g. GET, POST)
 * @param header      the list of request headers
 * @param url         the request URL
 * @param body        the request body, or {@code null} if none
 * @param description a human-readable description of the request
 */
public record PostmanRequest(String method, List<PostmanHeader> header, PostmanUrl url, PostmanBody body,
                             String description) {}
