# CPF Build-vs-Buy Matrix — QA32

- 기준일: 2026-07-31
- 적용 기준 Source: `4f675c7f89998cdbba7202e6c83320a0a4421a1f`
- Amendment: `CPF_20260731_QA32_OSS_PRIMARY_ENGINE_STEERING.md`

이 문서는 `CPF_20260730_QA32_OSS_MIGRATION_MATRIX.csv`의 사람이 읽는 요약이다.

| Change ID | 영역 | 결정 | Primary OSS | CPF가 소유 | 제거 대상 |
|---|---|---|---|---|---|
| `OSS-MIG-001` | ADM/BZA UI Widgets | ADOPT_NOW | Element Plus + TanStack Table | Design tokens, permission/action policy, PII masking, audit UX, feature composition | custom generic table/pager/dialog/menu/form implementations after consumer parity |
| `OSS-MIG-002` | Frontend Routing | ADOPT_NOW | Vue Router | menu/permission metadata and business route IDs | hashchange/location.hash/history custom primary routing |
| `OSS-MIG-003` | Frontend Client State | ADOPT_NOW | Pinia | state persistence allow/deny and security classification | large cross-feature mixins/global mutable stores |
| `OSS-MIG-004` | Frontend Server State | ADOPT_NOW | TanStack Vue Query | query keys, error contract, idempotent retry policy | manual cache/refetch/loading logic |
| `OSS-MIG-005` | Frontend Form Validation | ADOPT_NOW | Zod + Element Plus Form | backend validation source of truth, audit reason, null/empty policy | generic manual coercion for important workflows |
| `OSS-MIG-006` | Frontend API Client | ADOPT_NOW | Orval | OpenAPI, headers, CSRF, standard error, operationId | raw fetch and manual endpoint strings except narrow approved mutator |
| `OSS-MIG-007` | Browser Security | ADOPT_NOW | Spring Security + Spring Session JDBC | Role/Menu/Button/API/Data Scope, session audit/forced termination | browser readable access/refresh token persistence and duplicate auth filters |
| `OSS-MIG-008` | Gateway Data Plane | ADOPT_NOW | Spring Cloud Gateway Server Web MVC + Embedded Tomcat | route/binding/server group/approval/version/ACK/NACK/safety/ledger | custom HTTP forwarding Primary path, bootWar, WebFlux artifact plans |
| `OSS-MIG-009` | Messaging | ADOPT_NOW | Apache Kafka | message contract, schema/version, idempotency, DLT, attempt ledger | AMQP primary and broker dependencies from core API |
| `OSS-MIG-010` | Messaging Unit Test | ADOPT_NOW | CPF In-memory Test Adapter | simple deterministic contract adapter | none; must not emulate full Kafka |
| `OSS-MIG-011` | Resilience | ADOPT_NOW | Spring Cloud CircuitBreaker + Resilience4j | retry eligibility, request hash, unknown result, policy approval, attempt ledger | custom retry/circuit loops and duplicated layers |
| `OSS-MIG-012` | Batch Primary Execution Engine | ADOPT_NOW | Spring Batch | definition/version/approval/topology/agent security/unknown/fencing/audit/ADM | custom Job/Step execution engine, duplicate metadata owner, restart/checkpoint/partition/center-cut dispatcher and worker completion aggregation after parity |
| `OSS-MIG-013` | Persistent Scheduler Trigger | ADOPT_SCOPED | db-scheduler; Quartz only advanced optional adapter | schedule approval/version/windows/audit; Spring Batch Job linkage | duplicated persistent cron/cluster logic and scheduler-owned Job execution state |
| `OSS-MIG-014` | DB Migration | ADOPT_NOW | Flyway OSS Core | vendor SQL, backup/restore, rollback scripts, evidence | duplicated history/order execution logic after parity |
| `OSS-MIG-015` | Observability | ADOPT_NOW | Micrometer Observation + OpenTelemetry OTLP | CPF execution IDs, attribute policy, masking, attempt semantics | OTel SDK types from public API and duplicate instrumentation |
| `OSS-MIG-016` | Local Cache | ADOPT_NOW | Caffeine | key/TTL/invalidation/fail policy | cache dependency from common API |
| `OSS-MIG-017` | Distributed Cache | OPTIONAL_ADAPTER | Valkey-compatible provider | provider contract and consistency policy | Redis server bundling/default assumptions |
| `OSS-MIG-018` | Feature Flags | CONDITIONAL | OpenFeature SDK + CPF Provider | definitions/approval/environment/audit | CPF proprietary public client if present |
| `OSS-MIG-019` | Secret Management | ADOPT_ARCHITECTURE | CPF SecretProvider SPI + customer-managed secret services | reference, version, rotation status, approval, masking | plaintext/default secret and product secret-store aspirations |
| `OSS-MIG-020` | License/SBOM | ADOPT_NOW | CycloneDX + ORT + Syft + Grype | allow/deny/exception/NOTICE/release decision | manual-only license review and source-only SBOM |
| `OSS-MIG-021` | Browser E2E | ADOPT_NOW | Playwright + vue accessibility lint | role/workflow/test data/PII evidence rules | static-only UI completion |
| `OSS-MIG-022` | Approval Workflow | DO_NOT_ADOPT_UNLESS_ADR | Flowable OSS only if complexity threshold is met | approval policy/actor/audit/business object adapters | none by default |
| `OSS-MIG-023` | WebFlux Gateway | EXCLUDED_CURRENT_SCOPE | None | future performance ADR only | any premature webflux/netty gateway artifact |

## 판단 원칙

- `ADOPT_NOW`: QA32에서 해당 책임 범위의 실제 Product Primary Engine으로 전환하고 Consumer 전수 이관과 Legacy 제거를 완료한다.
- `ADOPT_SCOPED`: 제품 책임을 분할한 뒤 OSS에 위임한 범위에서는 OSS 표준 기능을 Primary Path로 사용하고, CPF 고유 Control Plane만 유지한다.
- `OPTIONAL_ADAPTER`: 제품 기본 의존성이나 Server Bundle로 만들지 않는다.
- `CONDITIONAL`: 실제 Product Requirement와 ADR 없이는 구현하지 않는다.
- `EXCLUDED_CURRENT_SCOPE`: Build Module·Artifact·Dependency를 생성하지 않는다.

`ADOPT_NOW` 또는 `ADOPT_SCOPED`는 Dependency만 추가하거나 OSS 위에 동일 기능의 자체 엔진을 다시 만드는 것을 허용하지 않는다.

## 완료 판정

각 행은 `dependency added`가 아니라 `consumer migrated + OSS-native runtime used + legacy removed + failure/recovery evidence`일 때만 완료다. 일부 Consumer, Sample, Wrapper, Skeleton 또는 Dual Primary 상태는 `PARTIAL`이다.
