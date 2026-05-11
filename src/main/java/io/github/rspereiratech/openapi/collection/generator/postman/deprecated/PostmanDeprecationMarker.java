package io.github.rspereiratech.openapi.collection.generator.postman.deprecated;

import io.github.rspereiratech.openapi.collection.generator.core.deprecated.DeprecationMarker;

/**
 * Postman-specific implementation of {@link DeprecationMarker} that prefixes deprecated
 * operation names with "[DEPRECATED]" and prepends a warning to their descriptions.
 */
public class PostmanDeprecationMarker implements DeprecationMarker {

    @Override
    public String markName(String n, boolean d) {
        return d ? "[DEPRECATED] " + n : n;
    }

    @Override
    public String markDescription(String desc, boolean d) {
        if (!d) return desc;
        String w = "\u26a0\ufe0f **This operation is deprecated.**";
        return desc.isBlank() ? w : w + "\n\n" + desc;
    }
}
