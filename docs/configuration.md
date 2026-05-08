# Configuration

The plugin itself is configuration-light: the heavy lifting is delegated to `openapi-collection-generator-core`, which exposes a `GenerationConfig` shared across all output formats. This document describes which fields the Postman plugin actually reads, and how OpenAPI input shapes the result.

## `GenerationConfig` fields used

| Field              | Used for                                                                 | Default if absent             |
| ------------------ | ------------------------------------------------------------------------ | ----------------------------- |
| `collectionName()` | Postman collection name (`info.name`) and base file name for env outputs | `OpenAPI.info.title`          |

Everything else (security, links, callbacks, extensions, schema examples) is configured through the **core** module's collaborators. Wire the desired implementations into the constructor of `PostmanCollectionGenerator`.

## Constructor wiring

```java
new PostmanCollectionGenerator(
    OperationGrouper grouper,            // e.g. TagOperationGrouper
    CollectionSerializer serializer,     // core: serializes the model to JSON
    SecurityApplier securityApplier,     // core: resolves auth schemes
    ServerEnvironmentGenerator serverEnv // core: turns OpenAPI servers into env files
);
```

The `OperationGrouper` (default: `TagOperationGrouper`) itself takes:

```java
new TagOperationGrouper(itemBuilder, callbackProcessor);
```

…and the `ItemBuilder` (default: `PostmanItemBuilder`) takes:

```java
new PostmanItemBuilder(
    new PostmanUrlBuilder(securityApplier),
    new PostmanHeaderBuilder(securityApplier),
    new PostmanBodyBuilder(schemaExampleGenerator),
    new PostmanDeprecationMarker(),
    extensionProcessorChain,
    linkDescriptionEnricher
);
```

## How OpenAPI shapes the output

| OpenAPI element                                 | Effect on the Postman collection                                                 |
| ----------------------------------------------- | -------------------------------------------------------------------------------- |
| `info.title`                                    | Default collection name.                                                         |
| `info.description`                              | Collection description.                                                          |
| `servers[]`                                     | Each server → one Postman environment file; first server's URL → `baseUrl`.      |
| `tags` on operations                            | Folder grouping (first tag wins; untagged ops go to `default`).                  |
| `summary` on operations                         | Item display name (fallback: `"{METHOD} {path}"`).                               |
| `description` on operations                     | Item description (enriched by extensions, links, deprecation marker).            |
| `deprecated: true` on operations                | Item name prefixed with `[DEPRECATED] `; description prefixed with a warning.    |
| `parameters` (`in: header`)                     | Empty-valued request headers.                                                    |
| `parameters` (`in: query`)                      | Empty-valued query params (with description).                                    |
| `parameters` (`in: path`)                       | `:variable` placeholders in the URL path.                                        |
| `requestBody.content` (first media type)        | `Content-Type` header + raw JSON body.                                           |
| `requestBody` examples / example / schema       | Source for the body content (in this order).                                     |
| `responses[*].links`                            | Appended to the item description by `LinkDescriptionEnricher`.                   |
| `callbacks`                                     | Materialized as additional requests in a `Callbacks` folder.                     |
| `security` (global) / `security` (operation)    | Generates auth headers/query params + collection variables.                      |
| `x-*` vendor extensions                         | Processed by `ExtensionProcessorChain`; may rename items or append to descriptions. |

## Security variable conventions

For each security scheme detected globally:

- A **collection variable** is created with a placeholder value.
- The same variable is added to each generated **environment file** as a `secret`.

Headers and query params for security are injected per-operation according to the operation's effective security requirements.

## Output file naming

| Artifact   | File name                                |
| ---------- | ---------------------------------------- |
| Collection | `<collectionName>.postman_collection.json` (host pipeline writes it) |
| Environment | `<server-name>.postman_environment.json` — one per declared server  |