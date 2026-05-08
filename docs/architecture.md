# Architecture

The plugin follows a **builder + strategy** design: a top-level generator orchestrates a small set of focused builders that each translate a slice of the OpenAPI spec into a piece of the Postman Collection model.

## High-level flow

```
OpenAPI spec ──► PostmanCollectionGenerator
                     │
                     ├─ OperationGrouper        (paths × operations  → folders)
                     │     └─ ItemBuilder       (single operation    → PostmanItem)
                     │            ├─ UrlBuilder
                     │            ├─ HeaderBuilder
                     │            ├─ BodyBuilder
                     │            ├─ DeprecationMarker
                     │            ├─ ExtensionProcessorChain (x-* extensions)
                     │            └─ LinkDescriptionEnricher
                     │
                     ├─ SecurityApplier         (auth schemes → vars/headers/query params)
                     ├─ ServerEnvironmentGenerator (servers → env files + baseUrl)
                     └─ CollectionSerializer    (model → JSON)
```

The generator returns:

- a **Postman Collection JSON** (the main artifact), and
- one or more **Postman Environment JSON** files (one per OpenAPI server).

## Package layout

| Package        | Responsibility                                                                 |
| -------------- | ------------------------------------------------------------------------------ |
| `generator/`   | Top-level orchestration ([`PostmanCollectionGenerator`](../src/main/java/com/github/rspereiratech/openapi/collection/generator/postman/generator/PostmanCollectionGenerator.java)). |
| `grouper/`     | Groups operations into folders by tag (or `Callbacks`).                        |
| `builder/`     | Assembles a `PostmanItem` for a single operation.                              |
| `url/`         | Builds the request URL (path segments + query params + security injection).    |
| `header/`      | Builds request headers (declared headers + `Content-Type` + security headers). |
| `body/`        | Builds the request body from examples or schema-generated examples.            |
| `deprecated/`  | Marks deprecated operations in name and description.                           |
| `model/`       | Postman Collection v2.1.0 records (POJOs).                                     |

## Design principles

1. **Strategy interfaces, default implementations.** Each builder exposes an interface (`UrlBuilder`, `HeaderBuilder`, `BodyBuilder`, `ItemBuilder`, `OperationGrouper`) with a default implementation. Swap the implementation if you need different behavior. See [Extension Points](extension-points.md).

2. **Pluggable core collaborators.** Cross-cutting concerns (security, deprecation, extensions, links, server environments, schema examples) come from `openapi-collection-generator-core` and are passed in via constructor, so this module stays focused on the Postman-specific shape.

3. **Immutable model.** All Postman model types are `record`s. They are constructed once and serialized as-is.

4. **Constructor injection only.** Every collaborator is required through the constructor — no statics, no service locators, no reflection.