# CPF Build-vs-Buy Product Decision Matrix

> Canonical path: `cpf-docs/architecture/CPF_BUILD_VS_BUY_MATRIX.md`  
> Initial QA32 decision set retained: `OSS-MIG-001`~`OSS-MIG-023`  
> Product synchronization: `2026-07-31`  
> Review baseline: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`

## 1. 목적

이 문서는 CPF가 직접 소유할 제품 책임과 검증된 OSS가 소유할 범용 Engine 책임을 장기적으로 구분한다.

`OSS-MIG-*`는 Architecture Decision/Migration 추적 ID이며 Canonical Product Requirement 162개와 별도다.

## 2. 결정 상태

| 상태 | 의미 |
|---|---|
| `ADOPT_NOW` | 실제 Primary Consumer 이관과 Legacy 제거가 필수 |
| `ADOPT_SCOPED` | 명시된 제한 범위에만 적용 |
| `OPTIONAL_ADAPTER` | 선택 기능; 기본 dependency/server bundle 금지 |
| `CONDITIONAL` | 실제 Requirement·Consumer·ADR가 있을 때만 |
| `ADOPT_ARCHITECTURE` | 공개 SPI와 고객 관리 Service 연결 구조 채택 |
| `DO_NOT_ADOPT_UNLESS_ADR` | 별도 복잡성 Threshold와 승인 전 금지 |
| `EXCLUDED_CURRENT_SCOPE` | Module/Artifact/Dependency 생성 금지 |

## 3. Product Matrix

| Decision ID | 영역 | 결정 | Primary OSS | Canonical Requirement | CPF 소유 | 제거·금지 대상 |
|---|---|---|---|---|---|---|
| `OSS-MIG-001` | ADM/BZA UI Widget | ADOPT_NOW | Element Plus + TanStack Table | ADM-UX, BZA-BUSINESS, TEST-BROWSER | design token, permission/action, masking, audit UX | custom generic table/pager/dialog/menu/form |
| `OSS-MIG-002` | Frontend Routing | ADOPT_NOW | Vue Router | ADM-UX, BZA-BUSINESS, API-CONTRACT | business route ID, menu/permission metadata | hashchange/location.hash custom router |
| `OSS-MIG-003` | Frontend Client State | ADOPT_NOW | Pinia | ADM-UX, BZA-BUSINESS | persistence allow/deny, security classification | large mixin/global mutable state |
| `OSS-MIG-004` | Frontend Server State | ADOPT_NOW | TanStack Vue Query | ADM-UX, API-CONTRACT | query key, retry/idempotency/error contract | manual cache/refetch/loading |
| `OSS-MIG-005` | Frontend Form | ADOPT_NOW | Zod + Element Plus Form | CPF-VALID, ADM-UX, BZA-BUSINESS | server validation source, reason, null/empty | manual generic coercion |
| `OSS-MIG-006` | Frontend API Client | ADOPT_NOW | Orval | API-CONTRACT, DEVEX-CODEGEN, TEST-CONTRACT | OpenAPI, header, CSRF, error mutator | raw fetch/manual endpoint strings |
| `OSS-MIG-007` | Browser Security | ADOPT_NOW | Spring Security + Spring Session JDBC | ADM-AUTH, SEC-AUTHN, SEC-AUTHZ, SEC-APP | role/data scope, revocation, audit | browser token persistence/duplicate auth filter |
| `OSS-MIG-008` | Gateway Data Plane | ADOPT_NOW | SCG Server Web MVC + Tomcat | GWY-ENTRY, GWY-ROUTING, GWY-TRUST, GWY-RESILIENCE | route/approval/trust/attempt ledger | custom HTTP proxy primary/WebFlux plans |
| `OSS-MIG-009` | Messaging | ADOPT_NOW | Apache Kafka | EVENT-CORE, EVENT-BROKER, EVENT-DLQ | envelope/idempotency/DLT/attempt/recovery | AMQP primary/core broker dependency |
| `OSS-MIG-010` | Messaging Unit Adapter | ADOPT_NOW | CPF in-memory test adapter | CORE-TESTKIT, TEST-UNIT | minimal deterministic contract | full Kafka emulation |
| `OSS-MIG-011` | Resilience | ADOPT_NOW | Spring Cloud CircuitBreaker + Resilience4j | CPF-RESILIENCE, CPF-DEADLINE, CPF-IDEMP | retry eligibility/deadline/unknown/ledger | custom retry/circuit loops |
| `OSS-MIG-012` | Batch Engine | ADOPT_NOW | Spring Batch 6 | BAT-CORE, BAT-JOB, BAT-ITEM, CENTER-CORE | definition/approval/topology/fencing/reconciliation | custom Job/Step/repository/checkpoint/dispatcher |
| `OSS-MIG-013` | Persistent Scheduler | ADOPT_SCOPED | db-scheduler; Quartz optional ADR | CPF-SCHED, BAT-JOB | schedule approval/window/audit/outbox | duplicated persistent cron/cluster |
| `OSS-MIG-014` | DB Migration | ADOPT_NOW | Flyway OSS Core | DB-MIGRATION, DB-ROLLBACK, DB-MULTI-VENDOR | Vendor SQL/backup/rollback/evidence | duplicated history/order engine |
| `OSS-MIG-015` | Observability | ADOPT_NOW | Micrometer Observation + OTel OTLP | CPF-TRACE, OPS-METRIC, OPS-SLO | CPF IDs/attribute/masking/attempt | OTel type in public API/duplicate instrumentation |
| `OSS-MIG-016` | Local Cache | ADOPT_NOW | Caffeine | CMN-EXTENSION, OPS-CONFIG | key/TTL/invalidation/failure policy | cache runtime in common public API |
| `OSS-MIG-017` | Distributed Cache | OPTIONAL_ADAPTER | Valkey-compatible provider | PROD-PACKAGE, OPS-CONFIG | provider/consistency/operation policy | Redis server bundle/default assumption |
| `OSS-MIG-018` | Feature Flag | CONDITIONAL | OpenFeature + CPF Provider | OPS-CONFIG, SEC-AUDIT | definition/approval/environment/audit | proprietary public client/skeleton |
| `OSS-MIG-019` | Secret | ADOPT_ARCHITECTURE | CPF SecretProvider SPI + customer service | SEC-SECRET, OPS-CONFIG | reference/version/rotation/audit/masking | plaintext/default secret/product vault server |
| `OSS-MIG-020` | License/SBOM/CVE | ADOPT_NOW | CycloneDX + ORT + Syft + Grype | RULE-SEC, RULE-QUALITY, REL-BUILD | policy/exception/notice/final artifact decision | manual-only/source-only scan |
| `OSS-MIG-021` | Browser E2E | ADOPT_NOW | Playwright | TEST-BROWSER, ADM-UX | role/workflow/fault/a11y/evidence | static-only UI completion |
| `OSS-MIG-022` | Approval Workflow | DO_NOT_ADOPT_UNLESS_ADR | Flowable OSS only after threshold | ADM-APPROVAL, BZA-APPROVAL | policy/actor/audit/business adapter | premature generic workflow engine |
| `OSS-MIG-023` | WebFlux Gateway | EXCLUDED_CURRENT_SCOPE | None | GWY-ENTRY, REL-BUILD | future load evidence/ADR only | WebFlux/Netty gateway module/artifact |

## 4. 공통 완료 증명

각 Decision은 다음을 모두 만족해야 한다.

1. Current Source/Consumer/Artifact Inventory
2. exact version/license/transitive dependency
3. Owner Module과 Public Contract
4. 실제 Product Consumer
5. 정상 기능 parity
6. 오류·보안·성능·부분 실패·복구 parity
7. multi-instance 또는 비적용 근거
8. 전체 Consumer 이관
9. Legacy Source/Bean/Route/Dependency/Artifact 제거
10. POM/BOM/Lock/npm lock
11. final Artifact와 SBOM/NOTICE/CVE
12. exact-SHA Runtime Evidence
13. 재유입 Negative Gate

## 5. Dual Primary 정책

Dual Primary는 Migration 기간에만 허용한다.

필수:

- Primary/Secondary 식별
- 상태·데이터 reconciliation
- traffic/consumer migration 단계
- rollback
- 종료 조건
- 제거 기한
- 운영 visibility

기한이나 종료 조건이 없는 Dual Primary는 `부분 구현`이다.

## 6. 주요 Architecture 경계

### Core/Common

- `cpf-core`: topology-independent 계약과 최소 기술 Runtime
- `cpf-common`: 고객 공통 정책
- Kafka/AMQP/WebFlux/OTel exporter/Redis 등 선택 Runtime을 public dependency로 강제하지 않음
- Starter/Adapter가 실제 Consumer를 소유

### Batch

Spring Batch가 전체 Job/Step lifecycle의 Primary다. CPF는 승인·정의·Topology·Artifact·Fencing·Unknown·ADM만 확장한다.

### Gateway

SCG Server Web MVC가 Data Plane Primary다. CPF는 Route/Trust/Approval/Ledger Control Plane을 소유한다.

### Frontend

Vue Router/Pinia/TanStack Query/Zod/Orval/Element Plus/TanStack Table이 실제 Feature Consumer에서 사용돼야 한다.

### Supply Chain

CycloneDX/ORT/Syft/Grype가 동일 final Artifact를 검사해야 한다.

## 7. Decision 변경 절차

- OSS 변경·Major Upgrade는 ADR와 license 재검토
- Primary 교체는 Consumer/Legacy/rollback Matrix 작성
- Decision ID는 조용히 삭제하지 않음
- Canonical Requirement 영향 Mapping 갱신
- Final Target과 충돌 시 Final Target이 우선하며 이 Matrix를 같은 작업에서 갱신
