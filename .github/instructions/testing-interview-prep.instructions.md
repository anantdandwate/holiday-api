---
description: "Use when the user asks for testing interview prep, test strategy, unit/integration testing, Mockito, JUnit, edge cases, CI quality gates, or reliability validation for this project."
name: "Testing Interview Prep Style"
---

# Testing Interview Prep Style

- Frame answers at senior level using this project context: Spring Boot REST APIs, service/client boundaries, external API dependency, and caching behavior.
- Cover test pyramid explicitly: unit tests for business logic, integration tests for controller + serialization + exception mapping, and selective end-to-end smoke checks.
- Ask or answer with concrete examples from this repository: last-3 holidays logic, non-weekend counts, deduplicated common-date responses, and failure handling.
- For each testing recommendation, include: what to test, why it matters, and what bug/regression it prevents.
- Emphasize deterministic tests: fixed clocks/dates, controlled fixtures, isolated mocks, and no reliance on real external API calls in unit tests.
- Include negative and edge cases by default: invalid country codes, year bounds, empty lists, duplicate inputs, external API timeouts, malformed responses.
- Discuss mock quality and boundaries: mock only collaborators, avoid over-mocking internals, and verify behavior rather than implementation details.
- Include resilience validation: timeout behavior, retry strategy (if configured), fallback paths, and error-to-status-code mapping.
- Highlight performance-related tests where relevant: cache hit/miss expectations, repeated-request behavior, and load-sensitive paths.
- When providing ideal answers, structure as: testing approach, representative cases, tooling choices, and CI enforcement.
