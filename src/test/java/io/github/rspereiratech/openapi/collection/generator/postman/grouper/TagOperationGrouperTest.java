package io.github.rspereiratech.openapi.collection.generator.postman.grouper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.github.rspereiratech.openapi.collection.generator.core.callback.CallbackProcessor;
import io.github.rspereiratech.openapi.collection.generator.postman.builder.ItemBuilder;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanItem;
import io.github.rspereiratech.openapi.collection.generator.postman.model.PostmanRequest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.callbacks.Callback;

class TagOperationGrouperTest {

    private final ItemBuilder itemBuilder = mock(ItemBuilder.class);
    private final CallbackProcessor callbackProcessor = mock(CallbackProcessor.class);
    private final TagOperationGrouper grouper = new TagOperationGrouper(itemBuilder, callbackProcessor);

    private PostmanItem stubItem(String name) {
        return PostmanItem.request(name, new PostmanRequest("GET", List.of(), null, null, ""));
    }

    @Test
    void group_groupsOperationsByFirstTag() {
        Operation op = new Operation().addTagsItem("pets").addTagsItem("misc");
        OpenAPI api = new OpenAPI().paths(new Paths().addPathItem("/pets", new PathItem().get(op)));
        when(itemBuilder.build(eq("/pets"), eq("GET"), any(), anyString(), any()))
                .thenReturn(stubItem("get pets"));

        Map<String, List<PostmanItem>> result = grouper.group(api, "http://example.com");

        assertEquals(1, result.size());
        assertTrue(result.containsKey("pets"));
        assertEquals(1, result.get("pets").size());
        assertEquals("get pets", result.get("pets").get(0).name());
    }

    @Test
    void group_usesDefaultGroup_whenOperationHasNoTags() {
        Operation op = new Operation();
        OpenAPI api = new OpenAPI().paths(new Paths().addPathItem("/health", new PathItem().get(op)));
        when(itemBuilder.build(any(), any(), any(), any(), any())).thenReturn(stubItem("health"));

        Map<String, List<PostmanItem>> result = grouper.group(api, "http://example.com");

        assertTrue(result.containsKey("default"));
        assertEquals(1, result.get("default").size());
    }

    @Test
    void group_usesDefaultGroup_whenTagsAreEmpty() {
        Operation op = new Operation().tags(List.of());
        OpenAPI api = new OpenAPI().paths(new Paths().addPathItem("/health", new PathItem().get(op)));
        when(itemBuilder.build(any(), any(), any(), any(), any())).thenReturn(stubItem("health"));

        Map<String, List<PostmanItem>> result = grouper.group(api, "http://example.com");

        assertTrue(result.containsKey("default"));
    }

    @Test
    void group_collectsMultipleOperationsUnderSameTag() {
        Operation get = new Operation().addTagsItem("pets");
        Operation post = new Operation().addTagsItem("pets");
        OpenAPI api = new OpenAPI().paths(new Paths()
                .addPathItem("/pets", new PathItem().get(get).post(post)));
        when(itemBuilder.build(any(), any(), any(), any(), any()))
                .thenReturn(stubItem("a"))
                .thenReturn(stubItem("b"));

        Map<String, List<PostmanItem>> result = grouper.group(api, "http://example.com");

        assertEquals(1, result.size());
        assertEquals(2, result.get("pets").size());
    }

    @Test
    void group_addsCallbacksGroup_whenCallbacksPresent() {
        Callback callback = new Callback();
        Operation op = new Operation().addTagsItem("pets").summary("My op");
        op.callbacks(Map.of("cb", callback));
        OpenAPI api = new OpenAPI().paths(new Paths().addPathItem("/pets", new PathItem().get(op)));

        PathItem cbItem = new PathItem().get(new Operation());
        when(callbackProcessor.extractCallbackPaths(any(), eq("My op"), any()))
                .thenReturn(Map.of("/cb-path", cbItem));
        when(itemBuilder.build(eq("/pets"), any(), any(), any(), any()))
                .thenReturn(stubItem("pets-item"));
        when(itemBuilder.build(eq("/cb-path"), any(), any(), any(), any()))
                .thenReturn(stubItem("cb-item"));

        Map<String, List<PostmanItem>> result = grouper.group(api, "http://example.com");

        assertTrue(result.containsKey("pets"));
        assertNotNull(result.get("Callbacks"));
        assertEquals(1, result.get("Callbacks").size());
        assertEquals("cb-item", result.get("Callbacks").get(0).name());
    }

    @Test
    void group_callbackNameFallsBackToMethodAndPath_whenSummaryNull() {
        Callback callback = new Callback();
        Operation op = new Operation().callbacks(Map.of("cb", callback));
        OpenAPI api = new OpenAPI().paths(new Paths().addPathItem("/x", new PathItem().get(op)));

        when(callbackProcessor.extractCallbackPaths(any(), eq("GET /x"), any()))
                .thenReturn(Map.of());
        when(itemBuilder.build(any(), any(), any(), any(), any())).thenReturn(stubItem("x"));

        grouper.group(api, "http://example.com");

        verify(callbackProcessor).extractCallbackPaths(any(), eq("GET /x"), any());
    }

    @Test
    void group_doesNotInvokeCallbackProcessor_whenCallbacksEmpty() {
        Operation op = new Operation().addTagsItem("pets");
        op.callbacks(Map.of());
        OpenAPI api = new OpenAPI().paths(new Paths().addPathItem("/pets", new PathItem().get(op)));
        when(itemBuilder.build(any(), any(), any(), any(), any())).thenReturn(stubItem("x"));

        grouper.group(api, "http://example.com");

        verify(callbackProcessor, never()).extractCallbackPaths(any(), any(), any());
    }

    @Test
    void group_buildsItemUsingPassedBaseUrl() {
        Operation op = new Operation().addTagsItem("pets");
        OpenAPI api = new OpenAPI().paths(new Paths().addPathItem("/pets", new PathItem().get(op)));
        when(itemBuilder.build(any(), any(), any(), any(), any())).thenReturn(stubItem("x"));

        grouper.group(api, "http://my-base");

        verify(itemBuilder, times(1)).build(eq("/pets"), eq("GET"), eq(op), eq("http://my-base"), eq(api));
    }
}
