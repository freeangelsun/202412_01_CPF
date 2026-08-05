# CPF 공통 Engineering Gate

모든 Work Package는 `WORK_ITEM_INDEX.csv`에서 대상 Work Item을 찾고, 해당 `ledger_part`의 `ledgers/*_WORK_ITEMS.csv` 및 `GATE_APPLICABILITY_MATRIX.csv`에 지정된 Gate를 판정한다. 적용되지 않는 Gate는 실제 호출 경로와 변경 영향에 근거한 `N/A`가 필요하다.

## GATE-01-OWNERSHIP — Module·Package·State Ownership

### 필수 기준

Single owner module/package/state owner; Public API/SPI/Internal boundaries; no reverse/cyclic dependency, owner DB direct access, dual primary or consumerless abstraction.

### 직접검증

Build graph, ArchUnit/module metadata, published consumer compile, package/dependency scan.

### 환경 제약 시 대체검증

Source graph and bytecode/package scan plus independent consumer compile.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-02-CONSUMER — Actual Product Consumer and Call Path

### 필수 기준

Default implementation, configuration, route/bean/SQL/script/frontend connection and complete producer→consumer→state→response path.

### 직접검증

Runtime startup and end-to-end consumer invocation.

### 환경 제약 시 대체검증

Module compile, bean/route/query contract harness and static call-path inventory.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-03-HTTP-API — HTTP API·OpenAPI·Problem Details

### 필수 기준

Pinned supported OAS profile, validation, AuthN/AuthZ, idempotency, versioning, RFC 9457 errors, generated client parity, reference-cycle and Markdown sanitization.

### 직접검증

OpenAPI validation, clean regeneration, backend/consumer contract and browser/API runtime.

### 환경 제약 시 대체검증

Schema/parser validation, standalone generated-client compile and mock-server contract.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-04-EVENT-API — Event·Message Contract

### 필수 기준

Versioned envelope, schema compatibility, destination/channel/operation, correlation, idempotency, ordering, TTL, security and provider semantics; AsyncAPI evaluation.

### 직접검증

AsyncAPI/schema validation and actual broker/provider contract matrix.

### 환경 제약 시 대체검증

Schema compatibility harness, embedded/fake provider and invocation capture.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-05-DB-QUERY — DB·SQL·Query Full Lifecycle

### 필수 기준

Canonical model/query contract→generator/template→generated SQL/mapper→fresh install/seed→migration/rollback/reapply→MariaDB/PostgreSQL/Oracle→repository/API/frontend/batch→drift/evidence.

### 직접검증

Three-vendor lifecycle and runtime consumer query/command.

### 환경 제약 시 대체검증

Dialect parser, migration simulator, metadata comparator, double-run seed and repository mapping harness.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-06-STATE-IDEMP — State·Idempotency·Concurrency

### 필수 기준

Allowed transitions, optimistic version, canonical request hash, duplicate/race semantics, terminal states and durable ownership.

### 직접검증

Concurrent runtime tests using shared durable state.

### 환경 제약 시 대체검증

Barrier/virtual-clock state-machine and persistence harness.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-07-MULTI-INSTANCE — Multi-instance·Lease·Fencing

### 필수 기준

Lease/TTL, fencing/owner epoch, heartbeat, stale-writer block, claim/reclaim, failover and process-kill recovery.

### 직접검증

Two or more JVM/process instances with shared state and kill/restart.

### 환경 제약 시 대체검증

Child-process/loopback competition and stale-token harness.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-08-UNKNOWN-RECOVERY — Partial Failure·UNKNOWN·Recovery

### 필수 기준

Classify failure before/after side effect, persist UNKNOWN, prohibit blind retry/success, reconcile, compensate, reprocess and manual resolution.

### 직접검증

Fault injection before/after commit/send/ACK/response with restart.

### 환경 제약 시 대체검증

Deterministic state-machine and crash-point persistence harness.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-09-SECURITY — Threat Model·AuthN·AuthZ·Secure Default

### 필수 기준

Trust boundary, abuse cases, server-side object/property/function authorization, input limits, fail-closed defaults and OWASP negative corpus.

### 직접검증

Security integration tests and intentional attack fixtures.

### 환경 제약 시 대체검증

Contract/security unit tests, static analysis and mock identity providers.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-10-CRYPTO-PRIVACY — Secret·Crypto·Certificate·Privacy

### 필수 기준

Secret provider, rotation/revocation, algorithm/key policy, PII classification, masking, raw access, encryption/tokenization impact and zero plaintext in logs/evidence.

### 직접검증

Vault/certificate/rotation runtime, leak scan and cryptographic inventory.

### 환경 제약 시 대체검증

Fake provider/clock, argument/log capture, CBOM/schema checks.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-11-OPS-AUDIT — Operations·Approval·Audit

### 필수 기준

Search/status/control, reason, immutable command hash, approval/SoD/expiry/break-glass, owner command execution and tamper-evident audit.

### 직접검증

ADM/API runtime and authorization/audit tests.

### 환경 제약 시 대체검증

Controller/service contract and append-only audit state harness.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-12-OBSERVABILITY — Trace·Metric·Log·SLO

### 필수 기준

transactionId/trace/segment/attempt correlation, stable OTel semantic conventions by default, bounded cardinality, errors, SLI/SLO and telemetry migration policy.

### 직접검증

Collector/exporter integration and query/dashboard assertions.

### 환경 제약 시 대체검증

In-memory exporter, schema/cardinality checks and trace continuity harness.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-13-PERFORMANCE — Resource·Performance·Load·Soak

### 필수 기준

Memory/disk/thread/connection/queue/time budgets, backpressure, bounded streaming, representative load, soak/leak and regression threshold.

### 직접검증

Load/stress/soak runtime with representative data.

### 환경 제약 시 대체검증

Micro/contract benchmarks, resource-bound assertions and leak-oriented repeated harness.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-14-FRONTEND — Frontend·Generated Client·WCAG

### 필수 기준

Loading/empty/error, permission, generated client parity, keyboard/focus/label/target size, responsive and WCAG 2.2 AA.

### 직접검증

Typecheck/unit/production build and Chromium/Firefox/WebKit E2E/a11y.

### 환경 제약 시 대체검증

jsdom/mock server, standalone compiler and ARIA/keyboard/focus assertions.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-15-GENERATOR — Generator·Generated Domain·Sample

### 필수 기준

Canonical input, deterministic output, collision validation, user-owned area preservation, remove/regenerate parity, golden domain/sample/EDU real product contracts.

### 직접검증

Fresh-clone create→build→runtime→remove→regenerate.

### 환경 제약 시 대체검증

Golden normalized tree, template parser and offline generated consumer compile.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-16-COMPATIBILITY — Version·Migration·Rollback Compatibility

### 필수 기준

Semantic/API/message/file/DB/config compatibility, mixed version, deprecation, expand-migrate-contract, rollback/forward recovery and unsupported fail-closed.

### 직접검증

Mixed-version rolling and upgrade/rollback runtime.

### 환경 제약 시 대체검증

Compatibility matrix, schema diff and migration/rollback simulator.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-17-SUPPLY-CHAIN — Build·Dependency·SBOM·Provenance

### 필수 기준

Pinned wrapper/BOM/lock, SLSA 1.2 source/build controls, CycloneDX 1.7 final-artifact BOM, supplier due diligence, license/vulnerability/KEV response and artifact verification.

### 직접검증

Fresh clean build, provenance/SBOM generation, signature and consumer verification.

### 환경 제약 시 대체검증

Module build, POM/BOM/artifact structure, schema and attestation verification.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-18-TEST-EVIDENCE — Direct Test·Alternative Validation·Evidence

### 필수 기준

Target environment attempted first; commands, versions, exit codes, results, reports, hashes, sanitization, linked Requirement/Scenario and remaining runtime gap.

### 직접검증

Target runtime/browser/broker/DB/process execution.

### 환경 제약 시 대체검증

Maximum equivalent compile/contract/state/fault/parser validation with explicit equivalence and gaps.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-19-DOC-SUPPORT — Documentation·Runbook·Supportability

### 필수 기준

OpenAPI/JavaDoc/developer/operator/install/recovery documentation, executable runbook and sanitized support bundle aligned with source.

### 직접검증

Fresh user walkthrough and runbook/support-bundle execution.

### 환경 제약 시 대체검증

Link/path/example/command validation and sanitized fixture bundle.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-20-HYGIENE — Repository Hygiene·Legacy Retirement

### 필수 기준

No dual primary, dead code, orphan config, generated garbage, stale evidence, secret, external runtime CDN/font/script; deletion through approved manifest.

### 직접검증

Fresh clone/build and repository quality gates.

### 환경 제약 시 대체검증

Static inventory, consumer/reference scan and protected-path manifest validation.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
## GATE-21-TIME — Time·Clock·Timezone

### 필수 기준

UTC/business timezone, monotonic duration, clock skew, expiry/lease/deadline/audit timestamp semantics, test clock and NTP/clock health.

### 직접검증

Clock-skew and timezone runtime tests.

### 환경 제약 시 대체검증

Injectable clock/virtual time and serialization/ordering tests.

### 완료 차단

- 필수 기준 미충족
- 직접검증 미시도
- 가능한 대체검증 미수행
- 근거 없는 N/A
- Evidence 없는 판정

---
