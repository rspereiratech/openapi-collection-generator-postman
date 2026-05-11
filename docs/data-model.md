# Data Model

All Postman model types live in the [`model/`](../src/main/java/io/github/rspereiratech/openapi/collection/generator/postman/model) package and are immutable Java `record`s. They mirror the [Postman Collection v2.1.0](https://schema.getpostman.com/json/collection/v2.1.0/collection.json) schema closely enough for direct serialization via Jackson.

## Tree

```
PostmanCollection
├── info: PostmanInfo            { name, description, schema }
├── item: List<PostmanItem>      ── folders or requests (recursive)
└── variable: List<PostmanVariable>  { key, value, type }

PostmanItem
├── name: String
├── description: String?
├── item: List<PostmanItem>?     ── present only for folders
└── request: PostmanRequest?     ── present only for requests

PostmanRequest
├── method: String               ── GET, POST, ...
├── header: List<PostmanHeader>  { key, value }
├── url: PostmanUrl              { raw, host, path, query }
├── body: PostmanBody?           { mode, raw, options }
└── description: String

PostmanUrl
├── raw: String                  ── e.g. "{{baseUrl}}/users/:id?limit="
├── host: List<String>           ── e.g. ["{{baseUrl}}"]
├── path: List<String>           ── e.g. ["users", ":id"]
└── query: List<PostmanQueryParam>  { key, value, description }

PostmanBody
├── mode: String                 ── always "raw" in this plugin
├── raw: String                  ── pretty-printed JSON
└── options: PostmanBodyOptions  { raw: { language: "json" } }
```

## Folders vs. requests

`PostmanItem` is a tagged union expressed via two factory methods:

- `PostmanItem.folder(name, items)` — sets `item`, leaves `request` null.
- `PostmanItem.request(name, req)`  — sets `request`, leaves `item` null.

Jackson serializes the null fields as absent, matching the Postman schema.

## URL representation

Postman wants both the raw URL **and** a structured breakdown. The plugin produces both:

- `raw`: literal string with `{var}` rewritten as `:var` and `{{baseUrl}}` prefixed.
- `host`: a single-element list `["{{baseUrl}}"]`.
- `path`: each segment separately, with path variables converted to `:name` form.
- `query`: name/value pairs (values empty unless injected by security).

Example for `GET /users/{id}` with a `limit` query param and an injected `apiKey`:

```json
{
  "raw": "{{baseUrl}}/users/:id?limit=&apiKey={{apiKey}}",
  "host": ["{{baseUrl}}"],
  "path": ["users", ":id"],
  "query": [
    { "key": "limit",  "value": "",            "description": "" },
    { "key": "apiKey", "value": "{{apiKey}}",  "description": "security" }
  ]
}
```

## Body representation

The plugin only emits raw JSON bodies. The shape is:

```json
{
  "mode": "raw",
  "raw": "{\n  \"name\": \"sample\"\n}",
  "options": { "raw": { "language": "json" } }
}
```

The `raw` string is pretty-printed via Jackson and resolved (in order) from: named examples → single example → schema-generated example.

## Variables

Two kinds of variables are emitted:

| Where                | Source                                                | Type      |
| -------------------- | ----------------------------------------------------- | --------- |
| Collection variables | `baseUrl` + every security variable                   | `string`  |
| Environment values   | `baseUrl` (default) + each security variable          | `secret`  |
