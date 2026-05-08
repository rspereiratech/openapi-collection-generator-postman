# OpenAPI Collection Generator - Postman

Postman collection generator plugin for the [openapi-collection-generator](https://github.com/rspereiratech) project. It converts an OpenAPI 3 specification into a ready-to-import **Postman Collection v2.1.0**, plus one Postman **environment** file per declared server.

## Features

- Generates Postman Collection v2.1.0 JSON from any OpenAPI 3 specification.
- Groups requests into folders by OpenAPI **tag**.
- Builds full request models: URL, path/query parameters, headers, and JSON request bodies.
- Resolves server URLs and emits one Postman **environment** file per server.
- Applies global security schemes as collection/environment variables (with secret placeholders).
- Marks deprecated operations.
- Pluggable architecture: implements the `CollectionGenerator` SPI from `openapi-collection-generator-core`.

## Requirements

- Java 17+
- Maven 3.8+
- The parent project [`openapi-collection-generator-parent`](https://github.com/rspereiratech) and the `openapi-collection-generator-core` module on your build path.

## Build

```bash
mvn clean install
```

## Usage

This module is a **plugin** consumed by the core generator. Add it as a dependency alongside the core:

```xml
<dependency>
    <groupId>com.github.rspereiratech</groupId>
    <artifactId>openapi-collection-generator-postman</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Then invoke the core pipeline. The entry point is `PostmanCollectionGenerator`, which implements `CollectionGenerator` and is wired through the plugin's generation pipeline. It produces:

- `<collection-name>.postman_collection.json` — the collection itself.
- `<server-name>.postman_environment.json` — one per server defined in the OpenAPI spec.

### Programmatic example

```java
PostmanCollectionGenerator generator = new PostmanCollectionGenerator(
        operationGrouper,
        collectionSerializer,
        securityApplier,
        serverEnvironmentGenerator
);

String collectionJson = generator.generate(openApi, generationConfig);
List<AdditionalFile> environments = generator.generateAdditionalFiles(openApi, generationConfig);
```

## Project structure

```
src/main/java/com/github/rspereiratech/openapi/collection/generator/postman/
├── body/         # Request body builders
├── builder/      # Postman item builders (request → item)
├── deprecated/   # Deprecation marker
├── generator/    # PostmanCollectionGenerator entry point
├── grouper/      # Tag-based operation grouping
├── header/       # Header builders
├── model/        # Postman v2.1.0 data model (POJOs)
└── url/          # URL builders (path + query params)
```

## Output

- **Collection**: Postman Collection v2.1.0 JSON, ready to import via *File → Import* in Postman.
- **Environment(s)**: one per server, with `baseUrl` plus any required security variables (API keys, tokens, etc.) as secret placeholders.

## License

See the parent project for licensing information.