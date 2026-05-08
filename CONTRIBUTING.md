# Contributing

Thanks for your interest in contributing to **OpenAPI Collection Generator – Postman**! This document outlines how to propose changes, report issues, and get your work merged.

## Code of conduct

Be respectful, constructive, and assume good intent. Personal attacks, harassment, or discriminatory language are not tolerated.

## Ways to contribute

- **Report bugs.** Open an issue describing the problem, the OpenAPI input that triggers it (minimal repro if possible), the expected behavior, and what you actually saw.
- **Request features.** Open an issue describing the use case before opening a PR for non-trivial work — it saves time on both sides.
- **Improve documentation.** Typos, clarifications, examples in [`docs/`](docs/) — all welcome.
- **Submit code changes.** See below.

## Development setup

### Prerequisites

- Java 17+
- Maven 3.8+
- The parent project [`openapi-collection-generator-parent`](https://github.com/rspereiratech) and the `openapi-collection-generator-core` module on your build path.

### Build & test

```bash
mvn clean install
```

Tests live under `src/test/java`. Add tests for new behavior — pull requests without tests for non-trivial changes will likely be asked to add them.

## Project conventions

- **Java 17 features** (records, pattern matching, `var` for local types) are encouraged.
- **Constructor injection only.** No statics, no service locators.
- **Strategy pattern.** New behavior should be exposed via an interface in the relevant package, with a default implementation alongside it. See [docs/extension-points.md](docs/extension-points.md).
- **Immutable model types** — use `record`s.
- **Javadoc** on public types and methods. Keep it concise; describe *what* and *why*, not the obvious.
- **Code style.** Follow the existing style: 4-space indent, no wildcard imports, line length under ~120 chars.

## Pull request workflow

1. **Fork** the repository and create a topic branch from `master`:
   ```bash
   git checkout -b feature/short-name
   ```
2. Make focused commits. Prefer **small PRs** doing one thing well over large mixed-purpose ones.
3. Add or update tests for any code change.
4. Update documentation under [`docs/`](docs/) if behavior, configuration, or extension points change.
5. Make sure `mvn clean install` passes locally.
6. Open the PR against `master` and fill in:
   - **What** the change does.
   - **Why** it's needed (link issues if any).
   - **How** to verify it (reproduction steps, sample OpenAPI snippet, before/after output).
7. Be responsive to review feedback. PRs that go silent for a long time may be closed (you're welcome to reopen).

### Commit messages

Follow a short, imperative style:

```
Add support for multipart request bodies

- Emit `formdata` body mode when consumes contains multipart/form-data
- Cover schema-driven and example-driven cases
```

Keep the subject line under ~70 characters; use the body for context if needed.

## Reporting security issues

Please **do not** open public issues for security vulnerabilities. See [SECURITY.md](SECURITY.md) for the disclosure process.

## License

By contributing, you agree that your contributions will be licensed under the [MIT License](LICENSE) that covers this project.
