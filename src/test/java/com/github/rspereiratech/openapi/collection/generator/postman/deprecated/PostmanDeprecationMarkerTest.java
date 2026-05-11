package com.github.rspereiratech.openapi.collection.generator.postman.deprecated;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PostmanDeprecationMarkerTest {

    private final PostmanDeprecationMarker marker = new PostmanDeprecationMarker();

    @Test
    void markName_returnsOriginal_whenNotDeprecated() {
        assertEquals("Get Pet", marker.markName("Get Pet", false));
    }

    @Test
    void markName_prefixesDeprecated_whenDeprecated() {
        assertEquals("[DEPRECATED] Get Pet", marker.markName("Get Pet", true));
    }

    @Test
    void markDescription_returnsOriginal_whenNotDeprecated() {
        assertEquals("Returns a pet", marker.markDescription("Returns a pet", false));
    }

    @Test
    void markDescription_returnsWarningOnly_whenBlankAndDeprecated() {
        assertEquals("⚠️ **This operation is deprecated.**", marker.markDescription("", true));
    }

    @Test
    void markDescription_returnsWarningOnly_whenWhitespaceAndDeprecated() {
        assertEquals("⚠️ **This operation is deprecated.**", marker.markDescription("   ", true));
    }

    @Test
    void markDescription_prependsWarning_whenDeprecatedAndDescriptionPresent() {
        String result = marker.markDescription("Returns a pet", true);
        assertEquals("⚠️ **This operation is deprecated.**\n\nReturns a pet", result);
    }
}
