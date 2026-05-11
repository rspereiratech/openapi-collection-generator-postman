# Extension Points

The plugin is designed to be replaceable piece by piece. Every collaborator the top-level generator depends on is an interface; the bundled implementations are sensible defaults, not requirements.

## Plugin-local interfaces

These live inside this module and are wired into `PostmanCollectionGenerator`.

| Interface                                                                                                                              | Default impl                | When to override                                                       |
| -------------------------------------------------------------------------------------------------------------------------------------- | --------------------------- | ---------------------------------------------------------------------- |
| [`OperationGrouper`](../src/main/java/io/github/rspereiratech/openapi/collection/generator/postman/grouper/OperationGrouper.java)     | `TagOperationGrouper`       | Group operations by something other than the first tag (e.g. by path prefix, by version). |
| [`ItemBuilder`](../src/main/java/io/github/rspereiratech/openapi/collection/generator/postman/builder/ItemBuilder.java)               | `PostmanItemBuilder`        | Customize naming, description, or assembly of a single request item.   |
| [`UrlBuilder`](../src/main/java/io/github/rspereiratech/openapi/collection/generator/postman/url/UrlBuilder.java)                     | `PostmanUrlBuilder`         | Change how URLs/path-variable syntax/query params are emitted.         |
| [`HeaderBuilder`](../src/main/java/io/github/rspereiratech/openapi/collection/generator/postman/header/HeaderBuilder.java)            | `PostmanHeaderBuilder`      | Add fixed headers, drop ones, or change `Content-Type` resolution.     |
| [`BodyBuilder`](../src/main/java/io/github/rspereiratech/openapi/collection/generator/postman/body/BodyBuilder.java)                  | `PostmanBodyBuilder`        | Emit non-JSON bodies, switch from `raw` to `formdata`/`urlencoded`, etc. |

### Example: replace the grouper

```java
class PathPrefixGrouper implements OperationGrouper {
    @Override
    public Map<String, List<PostmanItem>> group(OpenAPI openApi, String baseUrl) {
        // group by /v1, /v2, ...
    }
}

new PostmanCollectionGenerator(
    new PathPrefixGrouper(),  // <-- swapped in
    serializer,
    securityApplier,
    serverEnvironmentGenerator
);
```

## Core collaborators (passed in from `openapi-collection-generator-core`)

| Interface                       | What it controls                                                 |
| ------------------------------- | ---------------------------------------------------------------- |
| `CollectionSerializer`          | How the collection model is serialized to JSON (formatting, mappers). |
| `SecurityApplier`               | Resolves OpenAPI security schemes to headers, query params, and variables. |
| `ServerEnvironmentGenerator`    | Maps `OpenAPI.servers` to per-environment files and base URLs.   |
| `SchemaExampleGenerator`        | Builds example payloads from JSON schemas when no example exists. |
| `CallbackProcessor`             | Resolves OpenAPI callback path objects.                          |
| `DeprecationMarker`             | Generic SPI for marking deprecated items (Postman impl: `PostmanDeprecationMarker`). |
| `ExtensionProcessorChain`       | Runs vendor extension (`x-*`) processors over each operation.    |
| `LinkDescriptionEnricher`       | Appends OpenAPI response link metadata to item descriptions.     |

Override these in the host application (the module wiring `PostmanCollectionGenerator`) to control behavior across all generators in the project, not just Postman.

## Adding a vendor extension processor

To handle a custom `x-*` extension on operations, register a processor in the `ExtensionProcessorChain` you pass to `PostmanItemBuilder`. The chain receives an `ExtensionContext` (path, method, name, description, operation) and returns an `ExtensionResult` that may:

- override the item name (`nameOverride`)
- append text to the item description (`descriptionAppend`)

This is the cleanest way to surface things like custom labels, badges, or notes without subclassing the builders.

## Replacing the deprecation marker

`PostmanDeprecationMarker` prefixes names with `[DEPRECATED] ` and prepends a ⚠️ paragraph to descriptions. To customize, implement `DeprecationMarker` (from core) and pass it into `PostmanItemBuilder`:

```java
DeprecationMarker custom = new DeprecationMarker() {
    @Override public String markName(String n, boolean d) { ... }
    @Override public String markDescription(String d, boolean dep) { ... }
};
```
