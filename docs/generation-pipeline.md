# Generation Pipeline

This document walks through what happens when [`PostmanCollectionGenerator.generate`](../src/main/java/com/github/rspereiratech/openapi/collection/generator/postman/generator/PostmanCollectionGenerator.java) is invoked.

## 1. Resolve the collection name

The collection name is taken from `GenerationConfig.collectionName()` if present, otherwise from `OpenAPI.info.title`.

## 2. Resolve the base URL

`ServerEnvironmentGenerator` is called to produce one `ServerEnvironment` per declared OpenAPI server. The first server's `baseUrl` is used as the default. If no server is declared, the placeholder `{{baseUrl}}` is used.

## 3. Build collection-level variables

A `baseUrl` variable is created first, followed by one variable per security requirement returned by `SecurityApplier.applyGlobal(openApi)` (e.g. `apiKey`, `bearerToken`). Their values are placeholders; users fill them in Postman.

## 4. Group operations into folders

`TagOperationGrouper` walks every path × method combination:

- Each operation goes into a folder named after its **first tag** (or `default` if untagged).
- If an operation defines OpenAPI **callbacks**, each callback path is materialized as an additional request in a separate folder named `Callbacks`.

## 5. Build a `PostmanItem` for each operation

For each operation, `PostmanItemBuilder` produces a request item by composing:

| Step | Component                  | Result                                                                   |
| ---- | -------------------------- | ------------------------------------------------------------------------ |
| 1    | name resolution            | `op.summary` if present, else `"{METHOD} {path}"`                        |
| 2    | description resolution     | `op.description` (or empty)                                              |
| 3    | extension processing       | `ExtensionProcessorChain` may override the name or append to description |
| 4    | deprecation marking        | `[DEPRECATED]` prefix + warning paragraph if `op.deprecated == true`     |
| 5    | URL build                  | `PostmanUrlBuilder` (see below)                                          |
| 6    | headers build              | `PostmanHeaderBuilder` (see below)                                       |
| 7    | body build                 | `PostmanBodyBuilder` (see below)                                         |
| 8    | link enrichment            | `LinkDescriptionEnricher` appends OpenAPI response links to description  |

### URL construction (`PostmanUrlBuilder`)

- Path segments: `/users/{id}` → `["users", ":id"]`. Postman uses `:name` for path variables.
- The raw URL is `{{baseUrl}}/users/:id`.
- Query parameters from `op.parameters` (with `in: query`) are added as empty-valued entries.
- `SecurityApplier.apply(op, openApi)` may inject extra query parameters (e.g. for an API-key-in-query scheme), tagged with description `"security"`.

### Header construction (`PostmanHeaderBuilder`)

Three sources, concatenated in order:

1. Declared header parameters (`op.parameters` with `in: header`) — empty values.
2. `Content-Type` derived from the first media type of the request body (if any).
3. Security headers injected by `SecurityApplier.apply(op, openApi)` (e.g. `Authorization`, `X-API-Key`).

### Body construction (`PostmanBodyBuilder`)

Always produces a `raw` JSON body (`mode: raw`, `language: json`):

1. If the first media type defines named **examples**, the first one is used.
2. Else if it defines a single **example**, that is used.
3. Else `SchemaExampleGenerator` synthesizes an example from the schema.
4. Pretty-printed via Jackson.

If JSON serialization fails, the body falls back to `{}`.

## 6. Assemble the collection

```java
new PostmanCollection(info, folders, vars)
```

…and serialize via `CollectionSerializer.serialize(...)`.

## 7. Generate environment files (`generateAdditionalFiles`)

For each `ServerEnvironment` produced in step 2, an environment file is emitted with:

- A `baseUrl` variable (default type).
- One `secret`-typed variable per security variable returned by `SecurityApplier.applyGlobal(openApi)`.
- Postman metadata (`_postman_variable_scope: environment`).

These files are returned alongside the collection so the host pipeline can write them next to the main artifact.