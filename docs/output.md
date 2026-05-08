# Output

The plugin produces two kinds of files. Both are JSON and ready to import in Postman as-is.

## 1. The collection

A single JSON file conforming to [Postman Collection v2.1.0](https://schema.getpostman.com/json/collection/v2.1.0/collection.json).

### Top-level shape

```json
{
  "info": {
    "name": "My API",
    "description": "...",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "users",
      "item": [
        {
          "name": "List users",
          "request": {
            "method": "GET",
            "header": [],
            "url": {
              "raw": "{{baseUrl}}/users",
              "host": ["{{baseUrl}}"],
              "path": ["users"],
              "query": []
            },
            "description": "Returns all users."
          }
        }
      ]
    }
  ],
  "variable": [
    { "key": "baseUrl", "value": "https://api.example.com", "type": "string" }
  ]
}
```

### Folder rules

- One folder per first OpenAPI tag.
- A `default` folder for untagged operations.
- A `Callbacks` folder for any callback paths discovered on operations.

### Item rules

- Name = `op.summary`, fallback `"{METHOD} {path}"`. May be overridden by a vendor extension.
- Description = `op.description` + extension append + response links. Prefixed with a deprecation warning if applicable.
- Path variables `{name}` are written as `:name` in both the segmented `path` array and the `raw` URL.

## 2. Environment files

One file per OpenAPI server, named by `ServerEnvironmentGenerator`. Shape:

```json
{
  "name": "production",
  "values": [
    { "key": "baseUrl",  "value": "https://api.example.com", "enabled": true, "type": "default" },
    { "key": "apiKey",   "value": "<your-api-key>",          "enabled": true, "type": "secret"  }
  ],
  "_postman_variable_scope": "environment"
}
```

The `secret` type causes Postman to mask the value in the UI.

## Importing into Postman

1. Open Postman.
2. **File → Import** (or click *Import* in the sidebar).
3. Drop the `*.postman_collection.json` file in.
4. (Optional) **File → Import** the `*.postman_environment.json` file(s).
5. Select the environment from the top-right dropdown and fill in any secret values.

You should now see the collection with its tag-based folders, every operation as a request, and `{{baseUrl}}` resolved by the active environment.

## Notes

- **Empty values are intentional.** Headers and query params declared in the spec are emitted with empty `value`s so the user can fill them in. Security parameters are pre-filled with `{{varName}}` placeholders.
- **Bodies are always raw JSON.** The plugin does not currently emit `formdata`, `urlencoded`, or binary bodies.
- **Error fallback.** If body example generation fails for any reason, the body becomes `{}` rather than failing the whole collection.
