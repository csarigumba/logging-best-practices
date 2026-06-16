# Logging Best Practices — Spring Boot Demo

A bare-minimum Spring Boot application demonstrating **12 logging best practices**.
Code-demonstrable practices are wired into the running app; ops/infra practices
are documented here with minimal local demonstrations.

- **Spring Boot 4.1.0** · **Java 21** · **Maven** (wrapper included)
- No third-party logging libraries — Logback (Spring Boot default) + Spring's
  **native structured logging** (`StructuredLogEncoder`, `logstash` format).

## Run it

```bash
# Dev (default): human-readable console logs
./mvnw spring-boot:run

# Prod: structured JSON to console + rolling JSON file under logs/
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

App listens on `http://localhost:8080`.

## The 12 practices → where they live

| # | Practice | Where |
|---|----------|-------|
| 1 | Clear logging strategy | Intentional, contextful log statements in `DemoService`; this README |
| 2 | Log levels + dynamic verbosity | INFO/WARN/ERROR in `DemoService`; runtime level change via `/actuator/loggers` (no redeploy) |
| 3 | Structured logging | `logback-spring.xml` prod block → `StructuredLogEncoder` `logstash` JSON |
| 4 | Context in entries | `web/MdcFilter` adds `requestId` + `path` (and `userId`) to MDC; appears in every line |
| 5 | Log sampling | `web/Sampler` — always keep errors, sample successful requests at `logging.sample-rate` |
| 6 | Canonical log lines | `web/CanonicalLogFilter` — one summary line per request (method/path/status/duration) |
| 7 | Centralized aggregation | *Documented below* — JSON output is aggregator-ready |
| 8 | Retention policies | `logback-spring.xml` rolling file: `maxHistory=7`, `totalSizeCap=500MB`; table below |
| 9 | Log security | *Documented below* — encrypt in transit/at rest, tiered access |
| 10 | What not to log | `domain/SafeUser` (id-only `toString`) — passwords/PII never logged |
| 11 | Performance | `logback-spring.xml` `AsyncAppender`; notes below |
| 12 | Logs vs metrics | Micrometer counters in `DemoService`; `/actuator/metrics` |

## Try it

```bash
# Business event (INFO) + metric increment
curl -s localhost:8080/api/orders/7

# Login — note the password is NEVER logged; user logged as SafeUser[id=...]
curl -s -X POST localhost:8080/api/login \
  -H 'Content-Type: application/json' -d '{"username":"alice","password":"hunter2"}'

# Random INFO / WARN(slow) / ERROR(with stack trace) — run a few times
curl -s localhost:8080/api/flaky

# Practice 12 — read the metric
curl -s localhost:8080/actuator/metrics/demo.orders.retrieved

# Practice 2 — raise log verbosity AT RUNTIME, no redeploy
curl -s -X POST localhost:8080/actuator/loggers/com.example.logging \
  -H 'Content-Type: application/json' -d '{"configuredLevel":"DEBUG"}'
# ...and dial it back:
curl -s -X POST localhost:8080/actuator/loggers/com.example.logging \
  -H 'Content-Type: application/json' -d '{"configuredLevel":"INFO"}'
```

In the **dev** profile each line shows `[req=<requestId>]`. In **prod** every line
is a JSON object including `requestId`, `path`, and (after login) `userId`.

## Documented, not built (ops/infra practices)

These can't honestly live inside a single demo app — here's how you'd do them in
production.

**7 — Centralized aggregation.** The prod profile emits one JSON object per line,
which is exactly what aggregators ingest. Ship it by pointing a collector at the
console stream or `logs/app.log`: Filebeat/Fluent Bit → Elasticsearch/Kibana,
Promtail → Loki/Grafana, or a Datadog/OpenTelemetry agent. Centralize from day
one; correlating across services after the fact is painful.

**8 — Retention.** The rolling file appender is the local stand-in (7 days, capped
at 500MB). In production set per-type retention at the aggregator/storage tier:

| Log type | Retention |
|----------|-----------|
| Debug | 7 days |
| Error | 90 days |
| Security / audit | 1 year+ (often legally required) |

**9 — Security.** Encrypt logs in transit (TLS to the collector) and at rest
(disk/bucket encryption). Enforce tiered access — app logs for developers,
sensitive system logs for senior engineers, full access for the security team —
and enable audit logging on the log manager itself.

**11 — Performance.** Three mitigations are shown or noted: (a) `AsyncAppender`
keeps log I/O off the request thread; (b) an efficient encoder/library (Logback +
native structured logging here) keeps overhead low; (c) `Sampler` cuts volume on
hot paths. In production also write logs to a separate disk partition and load-test
early to catch logging bottlenecks before they hit prod.

**Beyond canonical lines — distributed tracing.** Canonical log lines (practice 6)
summarize one request in one service. The next step is distributed tracing
(OpenTelemetry / Micrometer Tracing) to follow a request across services as linked
spans — drop in a tracing bridge to enable it.

## Project layout

```
src/main/java/com/example/logging/
  LoggingBestPracticesApplication.java
  web/MdcFilter.java          web/CanonicalLogFilter.java   web/Sampler.java
  api/DemoController.java
  domain/DemoService.java     domain/SafeUser.java
src/main/resources/
  application.yaml            logback-spring.xml
src/test/java/com/example/logging/   # smoke tests for MDC, canonical line, PII, sampling
```

## Test

```bash
./mvnw test
```
