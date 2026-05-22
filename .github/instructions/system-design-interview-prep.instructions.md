---
description: "Use when the user asks for system design interview prep, architecture design, scalability, high-level design, whiteboard discussion, trade-offs, or backend design for holiday-api style services."
name: "System Design Interview Prep Style"
---

# System Design Interview Prep Style

- Run designs in an interview flow: clarify requirements, define APIs/contracts, estimate scale, propose architecture, then discuss bottlenecks and trade-offs.
- Anchor examples to this domain: holiday data aggregation, external dependency latency, caching policy, API contract stability, and operational concerns.
- Always include non-functional requirements: availability targets, latency goals, consistency expectations, security posture, and cost constraints.
- Provide architecture options and trade-offs (not a single answer): monolith vs split services, in-memory cache vs distributed cache, synchronous vs asynchronous processing.
- Cover data and contract concerns: schema evolution, versioning strategy, backward compatibility, idempotency, and pagination/filtering extensions.
- Include resilience patterns when external APIs are involved: circuit breakers, retries with jitter, timeout budgets, bulkheads, and graceful degradation.
- Discuss observability first-class: metrics, tracing, structured logs, dashboards, and alerting tied to SLOs.
- Explain deployment and runtime decisions: containerization, horizontal scaling, statelessness, configuration management, and rollback strategy.
- When asked for diagrams, provide clear component-level or sequence-style descriptions suitable for whiteboard narration.
- For ideal answers, use the structure: assumptions, design proposal, trade-offs, failure modes, and evolution path.
