# Security Policy

## Supported versions

This project is currently in active early development. Only the latest released version on `master` receives security fixes.

| Version          | Supported |
| ---------------- | --------- |
| `1.0.0-SNAPSHOT` (latest) | ✅ |
| Older snapshots  | ❌ |

## Reporting a vulnerability

**Please do not open a public GitHub issue for security vulnerabilities.**

If you believe you have found a security issue in this project — for example, an input that causes the generator to produce a Postman collection containing leaked secrets, a code path that allows remote code execution via a crafted OpenAPI spec, or a dependency advisory that affects this module — report it privately.

### How to report

Send a private report to **rspereiratech@gmail.com** with:

- A description of the issue and its impact.
- Steps to reproduce, ideally with a minimal OpenAPI spec that triggers the problem.
- The version / commit hash you tested against.
- Any suggested fix or mitigation, if you have one.

You may also use [GitHub's private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing-information-about-vulnerabilities/privately-reporting-a-security-vulnerability) on the repository if available.

### What to expect

- **Acknowledgement:** within 5 business days.
- **Initial assessment:** within 14 days, including whether the report is accepted as a vulnerability and an estimated severity.
- **Fix timeline:** depends on severity and complexity. Critical issues are prioritized; you'll be kept informed.
- **Disclosure:** coordinated. We'll publish an advisory and credit the reporter (unless you prefer to remain anonymous) once a fix is available.

## Scope

In scope:

- Code in this repository.
- Generated artifacts (Postman collection / environment files) when produced from a well-formed OpenAPI spec.

Out of scope:

- Vulnerabilities in transitive dependencies that don't affect this project's usage of them — please report those upstream.
- Issues that require an attacker to already have write access to the OpenAPI spec being processed (the spec is treated as trusted input).
- Misconfiguration of Postman itself or of the user's environment.

## Handling secrets in generated output

The plugin emits Postman variables for security schemes (API keys, bearer tokens, etc.) using **placeholders** rather than real values. Real secrets should be filled in by the user inside Postman, in environment files marked as `secret`. **Do not commit Postman environment files containing real secrets to source control.**

If you discover a case where the generator inadvertently writes a real secret into the collection or environment file, treat it as a security bug and report it via the process above.
