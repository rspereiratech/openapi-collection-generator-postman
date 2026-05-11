# Documentation

This folder contains the technical documentation for the **OpenAPI Collection Generator – Postman** plugin.

## Index

- [Architecture](architecture.md) — how the plugin is structured and how the pieces fit together.
- [Generation Pipeline](generation-pipeline.md) — step-by-step description of how an OpenAPI spec becomes a Postman collection.
- [Configuration](configuration.md) — configuration options and how they affect the output.
- [Data Model](data-model.md) — Postman v2.1.0 records used internally.
- [Extension Points](extension-points.md) — interfaces you can implement to customize behavior.
- [Output](output.md) — what the generator produces (collection + environments) and how to import it.
- [Publishing](publishing.md) — SNAPSHOT/release publishing to Sonatype Central, required secrets.

## Quick links

- Source root: [`src/main/java/com/github/rspereiratech/openapi/collection/generator/postman`](../src/main/java/com/github/rspereiratech/openapi/collection/generator/postman)
- Entry point: [`PostmanCollectionGenerator`](../src/main/java/com/github/rspereiratech/openapi/collection/generator/postman/generator/PostmanCollectionGenerator.java)
- Postman Collection schema: [v2.1.0](https://schema.getpostman.com/json/collection/v2.1.0/collection.json)