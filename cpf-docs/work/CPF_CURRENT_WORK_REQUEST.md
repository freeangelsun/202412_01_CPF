# CPF Current Development Request / Developer GPT Execution Contract
## Core Slimming · Unified Utility · Modern Starter Portfolio · Repository Hygiene · QA 전수 교차검수

> Current canonical path: `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
>
> 문서 성격: **신규 개발 작업의 정식 Requirement / Acceptance + Developer GPT 실행 계약**
>
> 이 문서는 임시 Steering이 아니다. 다음 Developer GPT 작업은 이 문서를 독립 실행 가능한 신규 작업 요청으로 사용한다.
>
> Repository: `https://github.com/freeangelsun/202412_01_CPF`
> Branch: `master`
> Currentization basis master: `b2da6bd720d1a8506db6bddf5d2e35feb9dca964` (`07_15`)
> 직전 Developer 실행 기준 SHA: `9f16468cccae71523f65f0aefcd94322788c4dd0`
> 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
>
> **중요:** 실제 개발 시작 시 latest `origin/master`, exact SHA, HEAD, Working Tree를 다시 확인한다. 위 SHA는 이 요구사항 작성 시점의 조사 기준일 뿐이며, 실제 실행 기준은 작업 시작 시점의 최신 `master`다.

---

## 1. 목적

직전 세션에서 XA/JTA, TCC, Inbox/Dedup, Saga, JPA, CRUD/Search, AI, OIDC, KMS/HSM 경계, Digital Signature, Tamper-evident Audit, SOAP, Generator/DB3 보강이 실제 `master`에 반영되었다.

그러나 다음 단계는 이전 작업을 단순 연장하는 것이 아니다.

이번 신규 작업은 CPF를 **Lightweight Core + 명확한 Capability Ownership + 실제로 쓰기 쉬운 Starter + Modern Enterprise Operations** 구조로 재정렬하고, 기존에 구현된 기능도 새 Architecture 기준에서 다시 검수한다.

최종 목표는 다음과 같다.

1. `cpf-core`를 topology-independent 계약과 최소 의미론 중심으로 Slimming한다.
2. Core 안에 남아 있는 Spring/Servlet/OTel/Provider/편의 구현을 올바른 Owner로 이동한다.
3. 공통 Utility는 Core 창고가 아니라 명확한 Foundation/Utility 개발경험으로 제공한다.
4. Transaction ID는 Core 의미론/계약과 실제 생성·채널 구현을 분리한다.
5. Runtime Health / Instance Operations를 독립 운영 Capability로 완성한다.
6. Spring Data JPA, CRUD/Paging/Search를 JDBC/MyBatis와 동일한 CPF 개발경험으로 정착시킨다.
7. Distributed Session, S3-compatible Object Storage, Event Schema Governance, GraphQL Optional 등 실사용 가치가 높은 Modern Capability를 보강한다.
8. 모든 신규/이동 기능을 Generator, Generated Domain, OpenAPI, ADM/BZA, EDU, Test와 실제 Consumer까지 연결한다.
9. 이동으로 발생한 old package, duplicate source, stale config/test/doc, empty directory, session artifact 등 Garbage를 최종 정리한다.
10. Developer 자체검수 후 QA A/B가 **동일 전체 범위를 각각 100% 전수검수하고 상호 교차검증**할 수 있는 Evidence와 인계자료를 완성한다.

---

## 2. 현재 기준선에서 직접 확인된 사항

아래는 작성 시점 `master=b2da6bd720d1a8506db6bddf5d2e35feb9dca964`에서 직접 확인한 기준선이다.

### 2.1 이미 반영된 주요 기능

다음은 신규로 다시 만드는 대상이 아니라 **직접 재검수·보강 대상**이다.

- `cpf-starters/data/persistence-jpa`
- `cpf-starters/data/transaction-jta`
- `cpf-starters/integration/ai`
- `cpf-starters/integration/soap`
- `cpf-starters/security/audit-jdbc`
- `cpf-starters/security/oidc-login`
- `cpf-starters/security/resource-server` 보강
- `cpf-starters/security/secret` KMS/HSM/Signature/Audit 보강
- DB3 XA/Inbox/Core Transaction Security SQL
- XA Crash Recovery Harness
- Generator persistence 선택 보강
- Core Transaction Strategy / TCC / XA Public Contract
- Core Persistence CRUD/Search/Bulk/Lock Contract

### 2.2 다음 작업에서 반드시 재정리할 구조

`cpf-core/build.gradle`에는 현재 다음 compile boundary가 남아 있다.

- Spring WebFlux
- Spring Boot RestClient
- Spring Batch
- Jakarta Servlet
- Spring Web
- Spring WebMVC
- OpenTelemetry API

`compileOnly`라는 이유만으로 Core Ownership을 정당화해서는 안 된다. Public Contract에 정말 필요한 최소 타입인지 전수 검토하고, Runtime/Adapter 성격이면 Core 밖으로 이동한다.

### 2.3 Core Utility 현황

현재 `cpf-core/src/main/java/com/cpf/core/api/util`에는 다음이 존재한다.

- `CpfAttributes`
- `CpfClock`
- `CpfDates`
- `CpfDecimals`
- `CpfFiles`
- `CpfHashes`
- `CpfHeaders`
- `CpfIds`
- `CpfJson`
- `CpfLists`
- `CpfMaps`
- `CpfNumbers`
- `CpfPages`
- `CpfStrings`
- `CpfTimes`
- `CpfValidation`
- `CpfValues`

이 목록은 모두 KEEP 대상이 아니다. 각 항목을 `KEEP_CORE / MOVE_FOUNDATION / MOVE_CAPABILITY / ABSORB / REMOVE_CANDIDATE`로 분류한다.

### 2.4 문서 Currentization 기준

본 Overlay에서 최상위 정본·Starter Architecture·Architecture/Developer/EDU/Security/Recovery Guide와
Current Work/Matrix/QA 표준을 `b2da6bd720d1a8506db6bddf5d2e35feb9dca964` 기준으로 먼저 현행화했다.

최상위 Canonical Requirement Count는 **186개**다.

- 기존 180개는 약화하지 않는다.
- 신규 Canonical은 `FOUNDATION-UTILITY`, `SEC-SESSION-DIST`, `FILE-OBJECT-STORAGE`, `EVENT-SCHEMA`, `API-GRAPHQL`, `API-REALTIME` 6개다.
- Core Slimming, transactionId ownership, Health, Lock, Testkit, Documentation Governance, QA Cross Review는 기존 Canonical ID를 강화하여 중복 ID를 만들지 않았다.
- Developer가 실제 Source를 변경한 뒤 최종 successor SHA와 물리 구현에 맞춰 같은 문서를 다시 currentize한다.

### 2.5 현재 Starter Tree에서 신규 검토가 필요한 영역

작성 시점 Starter tree에서 별도 Capability로 확인되지 않은 영역:

- Runtime Health 전용 Starter
- `session-valkey`
- S3-compatible Object Storage
- GraphQL Optional

따라서 실제 최신 작업 시작 시 전체 tree를 다시 검색하여 이미 추가되었는지 먼저 확인하고, 없으면 본 Requirement대로 구현한다.

---

# PART A. Architecture 강제 요구

## NXT-ARCH-001 — Core Dependency Direction 불변 규칙

### Requirement

CPF Dependency Direction을 다음과 같이 강제한다.

```text
Pure Foundation / Core Contract
            ↑
Capability / Provider
            ↑
Starter / AutoConfiguration
            ↑
Application
```

다음은 허용하지 않는다.

- `cpf-core -> cpf-starters/*`
- `cpf-core -> Optional Provider implementation`
- `cpf-core -> Vendor Runtime implementation`
- `Foundation -> Starter`
- `Foundation -> Optional Provider`
- 순환 의존
- Runtime 기술을 Core Public API에 불필요하게 노출

### Acceptance

- Core → Starter dependency: `0`
- Core → Optional Provider implementation: `0`
- Foundation → Starter dependency: `0`
- Build graph cycle: `0`
- Internal package 외부 참조: `0`

### Evidence

- Gradle dependency graph
- settings/catalog validation
- Architecture Gate
- Negative fixture

---

## NXT-ARCH-002 — `cpf-core` Slimming 전수검수

### Requirement

`cpf-core`의 `api/common/config/internal/service/spi` 전체 Source를 직접 열어 Class 단위로 판정한다.

허용 판정:

- `KEEP_CORE`
- `MOVE_FOUNDATION`
- `MOVE_CAPABILITY`
- `MOVE_PROVIDER`
- `MOVE_STARTER`
- `ABSORB_EXISTING`
- `REMOVE_CANDIDATE`

다음 패턴은 집중 전수검색한다.

- `Default*`
- `*Adapter`
- `*Configuration`
- `*Filter`
- `*Repository`
- `*Provider`
- `*Client`
- `*Controller`
- `*Endpoint`
- `*HealthIndicator`
- `*Contributor`

### Core에 남을 수 있는 것

- topology-independent Public Contract
- Provider-independent SPI/Port
- Framework 핵심 Value Object
- Error/Transaction/Security/Context 의미론
- Provider-independent Policy
- 외부 Runtime 없이 동작하는 최소 순수 Logic

### Core에 남겨서는 안 되는 것

- Spring AutoConfiguration
- Servlet/Web Runtime Filter
- Spring MVC/WebFlux Runtime 구현
- OpenTelemetry Adapter/Configuration
- Actuator Runtime
- HealthIndicator/HealthContributor
- JDBC/JPA/MyBatis 구현
- Feature Flag 특정 Provider
- Cache/Messaging/File/Cloud 특정 Provider
- Optional Capability 구현
- 단순 편의 Utility 집합

### Acceptance

Core에 부적절한 Runtime 구현 `0`.

---

## NXT-ARCH-003 — compileOnly Ownership Gate

### Requirement

`compileOnly`는 Ownership 면죄부가 아니다.

Core build의 Spring Web, WebFlux, Servlet, Batch, OTel 등은 Public Contract를 표현하는 데 진짜 필요한지 확인한다.

- 불필요하면 Core에서 제거한다.
- Runtime 구현 때문에 필요하면 구현을 Owner Starter로 이동한다.
- Public API가 특정 Runtime 타입을 노출하는 경우 Contract 자체를 재설계한다.

### Acceptance

Core compile dependency 각각에 `why_core_required` 근거가 존재한다. 근거 없는 compile dependency `0`.

---

### Core Kernel 강제 Closure — 현재 재개발 기준

`cpf-core`의 최종 역할은 **CPF 전체가 반드시 알아야 하는 최소 Kernel / Contract / Semantics**다.
Core를 무용지물로 만드는 것이 목적이 아니라, 오히려 모든 Capability가 공유해야 하는
Error/Result/Outcome, Transaction/Execution Context, transactionId/lineage,
UNKNOWN/Reconcile/Idempotency semantics, 최소 Identity/Security/Tenant Context,
공통 Value와 정말 전역적인 기술중립 Contract/SPI를 명확하게 보존한다.

반대로 특정 기능을 사용하지 않는 Application이 알 필요가 없는 API/SPI/DTO/Port는
Provider-neutral 여부와 무관하게 Core Owner가 아니다.

강제 Ownership:
- Admin contract → `cpf-admin`
- Batch/CenterCut → `cpf-batch`
- Gateway → `cpf-gateway`
- AI → AI capability
- FixedLength → `cpf-starters/integration/fixedlength-core`
- File/Archive/Attachment/Object Storage/Tabular/FileTransfer → file capability
- Distributed Session → security/session capability
- Event Schema → messaging/schema-governance
- Health/Drain/Instance Registry → platform-operations/health
- Dynamic Log Level/Remote Log/Runtime Control → platform operations
- Structured/File/Async Logging runtime → observability/logging
- Provider implementation → 각 provider
- Spring Bean/Properties/Conditional Wiring/AutoConfiguration → Starter

**Provider-neutral interface이므로 Core에 둔다**는 판정은 금지한다.
최종 질문은 “이 Capability를 전혀 사용하지 않는 CPF Application도 이 계약을 반드시 알아야 하는가?”다.
NO이면 Core 밖을 우선한다.

07_18의 False Green 원인은 `verify_nxt_architecture.py`가 일부 기술 import/AutoConfiguration만 MOVE로
분류하고 나머지를 자동 `KEEP_CORE` 처리한 데 있다. 이제 미분류 Core Class는 자동 PASS하지 않고
`REVIEW_REQUIRED`로 실패해야 한다. `KEEP_CORE`는 명시적인 Kernel 의미와 Consumer 근거가 있어야 한다.

물리 Layout도 강제한다.
- 논리 Gradle project `:cpf-foundation`의 물리 Owner: `cpf-starters/foundation/core`
- 논리 Gradle project `:cpf-testkit`의 물리 Owner: `cpf-tools/testing/cpf-testkit`
- `cpf-starters/foundation/base`는 Spring Boot convenience/wiring Starter이고 Pure Foundation과 역할을 섞지 않는다.
- Repository Root의 `cpf-foundation/`, `cpf-testkit/`는 최종 구조에서 허용하지 않는다.
- 새로운 Repository Root 파일/Directory는 **사용자 명시 승인 + Canonical Root Allowlist 변경** 없이는 생성 금지다.

이동은 Source Copy로 끝나지 않는다. settings/Gradle dependency/BOM/publication/catalog/AutoConfiguration metadata,
Generator/Generated Domain/Sample/EDU/OpenAPI/Frontend/Test/JavaDoc/README/SQL/Config/Consumer를 함께 갱신하고,
old source·old package·duplicate interface/DTO·stale import/resource·empty directory를 0으로 만든다.

# PART B. Foundation / Unified Utility

## NXT-UTIL-001 — Unified CPF Utility 재설계

### Requirement

Core를 Utility 창고로 사용하지 않는다.

현재 `cpf-core/api/util` 전체를 아래 규칙으로 재분류한다.

### A. 단순 Wrapper

JDK/Spring보다 의미 있는 가치가 없는 Wrapper는 유지하지 않는다.

예:

- 단순 `trim`
- 단순 `isEmpty`
- 단순 Map getter
- 단순 숫자 parse

이 경우 `REMOVE_CANDIDATE` 또는 기존 API 흡수를 선택한다.

### B. CPF 고유 공통 Utility

아래처럼 CPF 정책 가치가 있는 순수 기능은 Foundation/Unified Utility로 이동한다.

- ID
- Clock
- Date/Time/Timezone
- Business Date
- Decimal
- 금융 Amount Precision
- Rounding Policy
- Validation
- Code/Enum Conversion
- Mapping
- Common Attribute
- deterministic test support

### C. Capability Owner가 명확한 Utility

- Header → Web/Header Capability
- Hash/Crypto → Security/Crypto
- File → File Capability
- Paging → Page/Persistence Contract
- Transaction ID → Transaction Contract + Foundation/Provider
- Health → Platform Operations
- Secret → Security/Secret

### Acceptance

- Core의 단순 Utility Wrapper: `0`
- Owner 없는 Utility: `0`
- Unified Utility 실제 Consumer 존재
- 단순 OSS/JDK Wrapper만 모은 새 Starter 금지

---

## NXT-UTIL-002 — Pure Foundation과 Application Convenience 분리

### Requirement

업무 개발자는 통합 Utility/기본 기능을 쉽게 쓸 수 있어야 한다. 그러나 Core가 Starter를 참조하면 안 된다.

필요 시 다음 역할을 물리적으로 구분한다.

```text
Pure Foundation
      ↑
Core / Capability
      ↑
Application Convenience Starter
      ↑
Application
```

기존 `cpf-starters/foundation/base`가 순수 Foundation인지 Spring Boot Convenience Starter인지 실제 역할을 확정한다. 한 Module이 두 역할을 섞고 있으면 분리한다.

### Pure Foundation 조건

- 순수 Java
- topology-independent
- Spring Runtime 비종속
- Vendor 비종속
- Starter 비종속
- Optional Provider 비종속

### Acceptance

Core가 Foundation을 사용할 필요가 있다면 순수 Foundation만 사용한다. Starter 역참조 `0`.

---

# PART C. Transaction ID / Context

## NXT-TXID-001 — Transaction ID Contract와 구현 분리

### Requirement

`CpfTransactionIdGenerator`와 Transaction Context 의미론은 Core Contract로 유지할 수 있다.

다음은 Core 밖으로 이동한다.

- UUID/ULID 등 실제 기본 생성 알고리즘
- Default Generator 구현
- Spring Bean/AutoConfiguration
- HTTP Servlet Filter
- Messaging/Channel Adapter
- transport-specific extraction/propagation 구현

Core Consumer는 구현 Class가 아니라 Contract에 의존한다.

### Acceptance

```text
Core Contract
   ↑
Foundation/Provider Implementation
   ↑
Starter Wiring
```

구조가 실제 Source/Gradle에 반영되어야 한다.

### E2E 보존

동일 transactionId가 다음 전체에서 유지되어야 한다.

- REST
- SOAP
- Local/Remote Domain Call
- Gateway
- Kafka
- RabbitMQ
- JMS
- IBM MQ
- Async
- Batch
- File
- Outbox/Inbox
- Saga
- TCC
- XA Recovery
- Reconcile
- Log/Audit
- ADM Timeline

Retry/Hop마다 새 transactionId 생성 금지.

---

# PART D. Core Runtime 구현 Ownership 이동

## NXT-OWN-001 — Observability 구현 이동

Core에 존재하는 OpenTelemetry Adapter/Aspect/Configuration 등 특정 Runtime 구현을 Observability/OTel Starter로 이동한다.

Core에는 Telemetry/Trace Context의 Provider-neutral Contract만 남긴다.

### Acceptance

Core의 OTel runtime implementation `0`.

---

## NXT-OWN-002 — Feature Flag Provider 이동

Property/OpenFeature Provider, Spring Configuration 등은 Feature Flag Capability가 소유한다.

Core에는 Provider-neutral evaluation contract만 남긴다.

### Acceptance

Core의 Feature Flag Provider implementation `0`.

---

## NXT-OWN-003 — Web/Security Filter 이동

Servlet Filter, Spring Security Filter, HTTP-specific transaction/context filter는 Web/Security Starter가 소유한다.

Core에는 Identity/Context/Header/Trust 의미론만 남긴다.

### Acceptance

Core의 Servlet runtime filter `0`.

---

## NXT-OWN-004 — Persistent Repository/Scanner 이동

Core 내부의 DB-backed repository/scanner/store가 존재하면 해당 JDBC/Persistence/Operations Provider로 이동한다.

Contract와 Value Object만 Core에 유지한다.

---

# PART E. Runtime Health / Instance Operations

## NXT-HEALTH-001 — Runtime Health Starter

### Requirement

전용 Platform Operations Health Capability를 정식 제공한다.

단순 Actuator Wrapper로 완료 처리하지 않는다.

최소:

- liveness
- readiness
- startup
- `UP`
- `DEGRADED`
- `DOWN`
- `OUT_OF_SERVICE`
- `UNKNOWN`
- dependency health
- version/build SHA
- start time/uptime
- active profile/capability
- draining
- maintenance

### Ownership

Core:
- 필요 최소 Health Contract/Status/Port

Health Starter:
- Spring Actuator
- HealthIndicator
- HealthContributor
- Probe Endpoint
- dependency check
- Runtime status implementation
- AutoConfiguration

### Acceptance

Actuator/Health Runtime implementation이 Core에 남지 않는다.

---

## NXT-HEALTH-002 — Dependency Health 안전성

활성 Capability만 Health에 참여한다.

대상 예:

- Oracle/PostgreSQL/MariaDB
- JDBC/MyBatis/JPA
- Valkey
- Kafka/RabbitMQ/JMS/IBM MQ
- External HTTP
- Object Storage
- Batch/File

Health 자체가 장애를 확대하지 않도록:

- short timeout
- concurrency limit
- resource isolation
- cache policy
- rate protection
- secret masking
- normalized failure reason

을 제공한다.

DB Down 상황에서 `Liveness UP / Readiness DOWN` 같은 구분을 지원한다.

---

## NXT-HEALTH-003 — Multi-instance Health / ADM

실제 흐름:

```text
Instance
 → Health/Heartbeat
 → Runtime/Registry
 → ADM
```

ADM에서 최소:

- system
- application
- instance
- version
- build SHA
- start time
- uptime
- last seen
- liveness/readiness
- degraded dependency
- draining
- maintenance
- overall state

검색/Paging/상세/권한/오류처리를 제공한다.

Public probe와 privileged diagnostic endpoint를 분리한다.

---

## NXT-OPS-001 — Graceful Drain

Lifecycle:

```text
RUNNING
 → DRAINING
 → READINESS DOWN
 → 신규 작업 차단
 → in-flight 완료/timeout
 → STOPPED
```

HTTP, Batch Worker, Message Consumer별 drain semantics를 구분한다.

기존 Runtime Control과 중복 구현하지 않는다.

---

# PART F. Persistence / JPA 보강

## NXT-JPA-001 — 기존 Spring Data JPA Starter 재검수

`cpf-starters/data/persistence-jpa`는 이미 존재하므로 재생성하지 않는다.

다음을 실제 Consumer까지 확인하고 부족하면 보완한다.

- Spring Data JPA
- Hibernate
- `JpaRepository`
- `PagingAndSortingRepository`
- `Pageable`
- Spring Data `Sort`
- `Specification`
- `@Query`
- `EntityManager`
- CPF Page/Sort Adapter
- native escape
- lock
- transaction/XA-JTA
- Audit/transactionId/Tenant/Security
- N+1 guard/observation
- slow query
- bulk
- DB3
- Generator
- EDU
- Golden/Generated Domain Consumer

### 0-footprint

JPA 미사용 Profile에는 Hibernate/JPA Bean/EntityManager/Config/side effect `0`.

---

## NXT-PERSIST-001 — CRUD/Paging/Search Provider Parity

다음 E2E를 JDBC/MyBatis/JPA 모두에서 검증한다.

```text
API
 → Controller
 → Service
 → Repository Contract
 → Provider
 → DB3
 → CPF Page/Result
 → API
 → OpenAPI
 → Generated Client
 → Real Consumer
```

기능:

- Create
- Read by ID
- Update
- Delete
- Exists
- Count
- Page
- Slice
- Cursor
- Sort
- Multi-sort
- Search
- Filter
- Dynamic Condition
- Bulk Insert/Update/Delete
- Optimistic Lock
- Pessimistic Lock
- Transaction
- Query timeout
- Audit
- transactionId
- Tenant/Security
- Slow Query
- SQL Injection defense
- Sort Field Allow-list
- Page Size limit
- Large Query protection

복잡 JOIN/Aggregation/Hint/Vendor SQL/Bulk 특수처리는 Domain Repository + Native Escape를 허용한다.

---

# PART G. Security / Session

## NXT-SEC-001 — OIDC/SSO 기존 구현 재검수

기존 `oidc-login`과 `resource-server`를 재사용한다.

최소:

- OAuth2 Resource Server
- JWT
- OAuth2 Client
- OIDC Login
- SSO
- Current User
- Current Tenant
- Role/Scope
- Token propagation
- Logout
- Session integration
- 401/403
- Audit
- transactionId
- Observability

특정 IdP Public API 종속 금지.

Keycloak/Entra ID/Okta 등은 표준 OIDC Provider 경계로 교체 가능해야 한다.

---

## NXT-SESSION-001 — Valkey Distributed Session

기존 JDBC Session은 유지한다.

Multi-instance용 Optional Valkey Session Provider를 제공한다.

최소:

- create/read/update/delete
- expiration
- renewal
- rotation
- session fixation defense
- concurrent session control
- forced logout
- logout propagation
- user/session index
- tenant isolation
- Audit
- Metrics
- multi-instance
- provider failure semantics

미사용 시 0-footprint.

---

# PART H. File / Object Storage

## NXT-STORE-001 — S3-compatible Object Storage

기존 Attachment/Archive/SFTP를 먼저 확인하고 중복 API를 만들지 않는다.

우선 기존 Attachment abstraction을 Provider 방식으로 확장한다. 불가능할 때만 별도 Object Storage Contract를 추가한다.

Public API는 AWS에 종속시키지 않는다.

최소:

- put/get/delete
- stream
- metadata
- checksum
- multipart
- range
- content-type
- presigned access
- expiry
- encryption
- Secret/KMS
- tenant isolation
- retry/timeout
- partial failure
- orphan reconcile
- retention/lifecycle
- malware scan hook

Provider는 S3-compatible boundary로 AWS S3/MinIO 등을 교체 가능하게 한다.

---

# PART I. Messaging Contract Governance

## NXT-EVENT-001 — Event Schema / Contract Governance

기존 Kafka/Rabbit/JMS/IBM MQ를 유지한다.

다음 Schema Contract를 Provider-neutral하게 제공한다.

- JSON Schema
- Avro
- Protobuf
- schema version
- backward compatibility
- forward compatibility
- breaking-change gate
- producer contract
- consumer contract
- generated model
- runtime validation
- schema id/content type
- CI gate
- Generator
- EDU

특정 Schema Registry Vendor를 Public API에 노출하지 않는다.

---

# PART J. GraphQL / Realtime

## NXT-GQL-001 — GraphQL Optional Capability

GraphQL은 Optional Capability로 구현한다.

REST/OpenAPI는 계속 기본 API다.

Spring for GraphQL 기반을 우선한다.

실제 활용:

- Browser BFF
- Mobile BFF
- Domain Query aggregation
- client-driven field selection
- ADM/BZA 복합조회

최소:

- Query
- Mutation
- Subscription은 실제 필요 시
- Schema/version
- CPF Error
- CPF Paging/Cursor
- Sort/Search
- Validation
- Authentication
- Authorization
- field authorization
- Tenant
- transactionId
- Audit
- Logging/Metrics/Trace
- timeout/rate limit
- query depth
- complexity limit
- request size
- N+1/DataLoader
- exception masking
- introspection production policy
- GraphiQL production policy
- Generator
- contract test
- EDU
- 실제 BFF Consumer
- native Spring GraphQL escape

Resolver에 업무 로직을 복제하지 않는다. REST와 Application/Service Layer를 재사용한다.

미사용 시 0-footprint.

---

## NXT-RT-001 — SSE / WebSocket Realtime

Server→Browser 단방향 상태는 SSE 우선.

양방향 필요 시 WebSocket.

사용 예:

- Batch progress
- Transaction timeline
- Runtime/Health state
- Notification
- Long-running operation

최소:

- auth/authz
- reconnect
- heartbeat
- duplicate control
- slow consumer
- backpressure
- rate limit
- multi-instance
- graceful shutdown
- fallback polling
- frontend typed consumer

독립 Starter가 필요 없으면 기존 Web/Operations Capability에 흡수한다.

---

# PART K. Lock / AI / Testkit / Execution

## NXT-LOCK-001 — Valkey Lock/Lease Provider

기존 `CpfDistributedLockPort`, JDBC lock, fencing semantics를 유지한다.

Valkey Provider를 추가할 경우 단순 SETNX Wrapper 금지.

필수:

- fencing token
- lease
- expiry
- owner identity
- stale owner block
- process kill
- network partition
- multi-instance
- retry/recovery
- idempotency

---

## NXT-AI-001 — 기존 AI Optional 재검수

기존 `cpf-starters/integration/ai`를 재사용한다.

Provider-neutral Public API를 유지하며:

- provider switch
- timeout/retry
- quota/rate
- resource/token limit
- masking
- Security/Audit
- Observability
- UNKNOWN/failure mapping
- 0-footprint

을 검증한다.

---

## NXT-TESTKIT-001 — 공식 CPF Testkit

기존 `CORE-TESTKIT` Requirement를 실제 제품 Test Support로 완성한다.

최소 fixture/harness:

- deterministic Clock
- deterministic ID
- transaction context
- authenticated user
- tenant
- HTTP
- DB
- Messaging
- Outbox/Inbox
- Saga/TCC
- Batch
- Object Storage
- Health
- GraphQL
- failure injection
- multi-instance
- process kill
- Generator smoke

Testcontainers는 사용 가능하지만 단순 Wrapper가 목적이 아니다.

---

## NXT-EXEC-001 — Java 25 Modern Execution

Virtual Thread 적용 가치가 있는 blocking I/O 경로를 검수한다.

지원 시 반드시 보존:

- transactionId
- Security Context
- MDC/trace
- transaction boundary
- deadline
- resource pool safety

Virtual Thread 전용 Starter를 무조건 만들지 않는다.

R2DBC/WebFlux reactive persistence는 실제 Consumer가 없으면 이번 작업에서 강제 추가하지 않는다.

gRPC도 실제 consumer requirement가 없으므로 이번 작업에서는 신규 Starter를 강제하지 않는다.

---

# PART L. Starter Developer Experience

## NXT-DX-001 — 모든 Starter DX 전수검수

각 Starter마다 다음을 직접 확인한다.

- 존재 이유
- actual consumer
- CPF API
- OSS 직접 사용 대비 장점
- Typed Properties
- Config Metadata
- Safe Default
- Fail-Fast
- Actionable Error
- Security
- Audit
- Masking
- transactionId
- Observability
- Timeout/Retry/Recovery
- Native Escape
- Generator
- EDU
- Test
- Optional removal / 0-footprint
- Provider 교체 가능성
- 중복 Owner 여부

단순 OSS Dependency + AutoConfig만 있는 Wrapper-only Starter는 FAIL.

---

# PART M. Generator / Generated Domain / Frontend

## NXT-GEN-001 — Generator 정합성

Framework Contract/Starter가 변경되면 Generator를 같은 사이클에서 수정한다.

특히 persistence profile:

- JDBC
- MyBatis
- JPA

선택 시 다음이 일관되게 생성되어야 한다.

- Repository
- Service
- Controller
- DTO
- Validation
- CRUD
- Paging
- Sort
- Search
- Test
- SQL/Migration
- OpenAPI
- Generated Client
- EDU Fixture

Health/Security/GraphQL/Object Storage/Utility 역시 새 프로젝트에서 실제 사용할 수 있어야 한다.

---

## NXT-GEN-002 — Golden Domain / Reference Consumer

`cpf-member`와 `cpf-reference`에서 새 Public API를 실제 소비한다.

Sample/Interface 존재만으로 완료 처리하지 않는다.

```text
Public API
 → Runtime implementation
 → Real Consumer
 → Test/Harness
 → Evidence
```

경로를 요구한다.

---

## NXT-FE-001 — ADM/BZA/OpenAPI/Generated Client 영향

Backend 계약 변경 시 반드시 확인:

- OpenAPI
- Generated Client
- ADM
- BZA
- 400/401/403/404/409/429/500/503
- search/paging/detail
- permission
- risky operation
- accessibility
- responsive
- realtime consumer

---

# PART N. Canonical / 작업 문서 Currentization

## NXT-DOC-001 — 최상위 Canonical Requirement 갱신

Developer는 구현 후 아래 정본을 실제 최신 successor SHA 기준으로 currentize한다.

필수:

- `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
- `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`

원칙:

1. 기존 Requirement와 중복되는 것은 새 ID를 남발하지 않고 기존 Requirement를 강화한다.
2. 실제 새로운 제품 Requirement만 신규 Canonical ID를 추가한다.
3. Canonical Requirement Count를 실제 행 수와 정확히 맞춘다.
4. 이전 currentization source SHA를 최신 successor로 갱신한다.
5. Alias/Supersede/Split/Merge 관계를 Continuity Ledger에 남긴다.
6. 삭제/통합된 Requirement를 조용히 소실시키지 않는다.

---

## NXT-DOC-002 — Architecture / Developer / Security / Operations Guide 갱신

최소:

- `cpf-docs/architecture/ARCHITECTURE_GUIDE.md`
- `cpf-docs/development/DEVELOPER_GUIDE.md`
- `cpf-docs/development/EDU_GUIDE.md`
- `cpf-docs/security/SECURITY_GUIDE.md`
- `cpf-docs/operations/RECOVERY_GUIDE.md`

에 다음을 실제 Source와 일치시키며 반영한다.

- Core Slimming
- Dependency Direction
- Unified Utility
- Transaction ID Ownership
- Health/Instance Operations
- JPA/JDBC/MyBatis
- OIDC/SSO
- Distributed Session
- Object Storage
- Event Schema
- GraphQL
- Realtime
- Testkit
- Generator
- Garbage cleanup/migration

---

## NXT-DOC-003 — Current Work / Matrix / Evidence Currentization

기존 Current-State 문서만 갱신한다.

필수:

- `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/CPF_REQUIREMENT_MATRIX.csv`
- `cpf-docs/work/CPF_SCENARIO_MATRIX.csv`
- `cpf-docs/work/CPF_STARTER_VALUE_CATALOG.csv`
- `cpf-docs/work/CPF_SOURCE_FINDINGS.csv`
- `cpf-docs/work/current/CPF_REQUIREMENT_CONTINUITY.csv`
- `cpf-docs/work/current/CPF_COVERAGE_CLOSURE_MATRIX.csv`
- `cpf-docs/work/current/CPF_REQUIREMENT_SOURCE_COVERAGE.csv`
- `cpf-docs/work/REQUIREMENT_STATUS.csv`
- `cpf-docs/work/REVIEW_INDEX.md`
- `cpf-docs/work/CPF_CHANGE_MANIFEST.csv`
- `cpf-docs/work/TEST_AND_EVIDENCE.md`
- `cpf-docs/work/OPEN_ISSUES.md`
- `cpf-docs/work/HANDOVER.md`
- `cpf-docs/work/CODEX_REVIEW_REQUEST.md`

새 날짜/REV/SESSION/FINAL_FINAL 문서 복제 금지.

---

# PART O. Repository Hygiene / Garbage Cleanup

## NXT-HYG-001 — Core 이동 후 Garbage 전수검사

Core Slimming은 많은 Source relocation을 발생시킬 수 있다.

따라서 이동마다 다음을 즉시 검사한다.

- old source
- old test
- old package
- stale import
- stale AutoConfiguration
- stale resources
- duplicate bean
- duplicate catalog entry
- duplicate publication
- stale generator template
- stale generated output
- stale docs
- stale SQL/config
- empty directory
- session workspace
- temporary CSV/TXT/log
- obsolete ZIP/hash/manifest
- ignored build artifact
- resurrected deprecated file

### Acceptance

- Duplicate relocation: `0`
- Stale import/reference: `0`
- Empty abandoned package: `0`
- Untracked garbage: `0`
- obsolete session artifacts: `0`

---

## NXT-HYG-002 — DELETE_MANIFEST 강제

사용자 승인 없이 실제 삭제하지 않는다.

삭제가 필요한 모든 파일은:

`cpf-docs/work/CPF_DELETE_MANIFEST.csv`

에 기록한다.

필수 컬럼:

- `path`
- `reason`
- `replacement_path`
- `owner`
- `requirement_id`
- `protected_check`
- `delete_status`

규칙:

- Repository Root 상대경로
- 파일 단위 exact path
- wildcard 금지
- directory 단위 삭제 금지
- protected path 차단
- replacement가 필요한 경우 replacement 존재 확인
- 중복 path `0`

보호 경로:

- `cpf-docs/deliverables/**`
- `cpf-docs/guides/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- `cpf-docs/environment/docker/**`
- `cpf-tools/environment/docker-development-test/**`

---

## NXT-HYG-003 — 사용자용 Garbage 정리 PowerShell 한 줄

Developer 최종 결과물에는 반드시:

`cpf-docs/work/CPF_DELETE_ONE_LINE.ps1.txt`

를 생성한다.

이 파일에는 **Repository Root에서 그대로 복사/실행 가능한 PowerShell 한 줄**만 둔다.

그 한 줄은 다음을 모두 수행해야 한다.

1. `git rev-parse --show-toplevel`로 Root 확인
2. `CPF_DELETE_MANIFEST.csv` 존재 확인
3. exact root-relative path만 처리
4. `..` traversal 차단
5. protected path 차단
6. replacement_path가 필요한 항목은 replacement 존재 확인
7. `Remove-Item -LiteralPath` 사용
8. 파일이 이미 없어도 실패하지 않음
9. wildcard 사용 금지
10. manifest에 없는 파일 삭제 금지
11. 삭제 결과 count 출력
12. 마지막에 `git status --short` 출력
13. `exit` 사용 금지
14. `git clean/reset/restore/stash` 사용 금지

빈 Directory 정리가 필요한 경우에도 manifest 삭제 완료 후 **비어 있는 Directory만** leaf부터 제거하며 보호경로와 Repository Root를 절대 건드리지 않는다.

### 중요

Core→Starter 이동 후 old/new Source가 동시에 남는 상태로 ZIP을 최종본이라 하지 않는다.

실제 삭제가 사용자 승인 때문에 아직 수행되지 않았다면:

- `development_status`: Source relocation 완료
- `verification_status`: cleanup pending
- `DELETE_MANIFEST`: 완료
- `CPF_DELETE_ONE_LINE.ps1.txt`: 완료

로 명확히 분리한다.

---

## NXT-HYG-004 — Windows Path Compatibility

새 Current 문서/Source/Package 경로는 짧게 유지한다.

- 날짜/Session 중첩 directory 금지
- `_workspace/.../REV.../sessions/...` 형태 금지
- 불필요한 깊은 Package 금지
- Overlay Root-relative max path를 검산한다.
- 목표: 새로 추가되는 경로는 가능한 한 160자 이하

---

# PART P. Verification / Evidence

## NXT-EVD-001 — Developer 자체검수

Developer는 구현 완료 전 독립 Self Review를 수행한다.

확인:

1. Core→Starter dependency 0
2. Core Optional Provider implementation 0
3. Core Runtime pollution 0
4. Core simple utility wrapper 0
5. Owner-less capability 0
6. Consumer-less API/SPI 0
7. Wrapper-only Starter 0
8. Transaction ID E2E gap 0
9. CRUD/Paging/Search parity gap 0
10. JPA/JDBC/MyBatis gap 0
11. Health/Instance Operations gap 0
12. OIDC/SSO/Session gap 0
13. Object Storage gap 0
14. Event Schema gap 0
15. GraphQL gap 0
16. Generator impact gap 0
17. OpenAPI/ADM/BZA gap 0
18. EDU/Reference gap 0
19. DB3 gap 0
20. Duplicate/garbage gap 0

실행하지 않은 Runtime Test는 PASS로 쓰지 않는다.

---

## NXT-EVD-002 — 이전 Runtime-only 항목 처리

직전 세션의 Runtime-only 10건을 PASS로 승계하지 않는다.

이번 Source 이동/보강 영향이 있으면 Harness/Config/Script를 갱신한다.

외부 환경이 필요한 항목은:

- 상태 `미검증`
- 환경
- 실행 명령
- 기대 결과
- 실패 기준
- 필요 Evidence

를 기록한다.

---

# PART Q. QA A/B 전수 교차검수 강제

## NXT-QA-001 — QA A 전체 전수검수

Developer 완료 후 QA A는 **전체 Scope 100%**를 독립 검수한다.

샘플링, 대표 ID, 상위 Requirement 하나로 하위 일괄 PASS 금지.

QA A 순서:

```text
Canonical
→ Architecture
→ Core/Foundation
→ Starter/Provider
→ Source
→ Consumer
→ Generator/Generated Domain
→ DB3
→ Transaction/Security
→ Health/Operations
→ ADM/BZA/OpenAPI
→ Test/Harness
→ Runtime/Evidence
→ Repository Hygiene
```

각 Requirement마다:

```text
Requirement ID
→ Source Path/Symbol
→ Consumer
→ Call Path
→ Failure/Boundary
→ Test/Harness
→ Execution/Evidence
→ Judgement
```

를 남긴다.

---

## NXT-QA-002 — QA B 전체 전수검수

QA B도 QA A와 **같은 전체 Scope를 100%** 독립 검수한다.

QA A의 PASS/Evidence를 판정 근거로 상속하지 않는다.

QA B 순서는 반대로 한다.

```text
Repository Hygiene
→ Runtime/Evidence
→ Test/Harness
→ ADM/BZA/OpenAPI
→ Health/Operations
→ Transaction/Security
→ DB3
→ Generator/Generated Domain
→ Consumer
→ Source
→ Starter/Provider
→ Core/Foundation
→ Architecture
→ Canonical
```

---

## NXT-QA-003 — QA A/B Cross Validation

A/B 완료 후 모든 Requirement ID를 교차대조한다.

반드시 산출:

- A 판정
- B 판정
- A evidence
- B evidence
- 일치/불일치
- 불일치 원인
- 재개발 필요 여부
- 재검수 필요 여부
- 최종 QA 판단 대기 상태

규칙:

- A PASS + B FAIL → 완료 금지
- A FAIL + B PASS → 완료 금지
- 한쪽 미검수 → 완료 금지
- 동일 generic evidence 반복 → 완료 금지
- Source 직접 open 없이 deep-review 완료 처리 금지
- Runtime 미실행 → 미검증
- Developer 보고만 근거로 PASS 금지

QA는 반드시 신규 추가 Requirement와 기존 Canonical Requirement를 함께 전수검수한다.

---

## NXT-QA-004 — QA가 다시 확인할 Framework Fundamentals

QA A/B 모두 Canonical 목록과 별도로 다음 Fundamentals Sweep을 수행한다.

### Web/API
Request/Response, Validation, Error, Paging, Sort/Search, Header, File, Idempotency, Rate Limit, OpenAPI, Generated Client.

### Persistence
CRUD, Paging/Cursor, JDBC, MyBatis, JPA, Transaction, Lock, DB3, Timeout, Multi-datasource.

### Security
Current User/Tenant/Role/Scope, OAuth2/JWT/OIDC/SSO, Secret/KMS/HSM, Masking, Audit.

### Utility
Date/Time, Decimal, ID, Serialization, Validation, Mapping. 단순 wrapper 여부.

### Reliability
Timeout, Retry, CB, Idempotency, UNKNOWN, Reconcile, Multi-instance, Process Kill.

### Integration
REST, SOAP, TCP, File, JMS, IBM MQ, Kafka, RabbitMQ, Batch.

### Operations
Health, Readiness, Liveness, Drain, Registry, Runtime Control, ADM.

### Observability
transactionId, Log, Audit, Metrics, Trace, Timeline.

### Developer Experience
Quick Start, minimal config, safe default, fail-fast, native escape, JavaDoc, EDU.

누락된 기본 기능은 QA 자체 Finding으로 등록한다.

---

# PART R. Canonical Requirement / QA 인계 산출물

## NXT-HO-001 — Developer 최종 산출물

Developer 완료 시 반드시 Root-relative Overlay에 포함:

- Source/SQL/API/Test/Config/Frontend/Script
- `REVIEW_INDEX.md`
- `REQUIREMENT_STATUS.csv`
- `CPF_CHANGE_MANIFEST.csv`
- `TEST_AND_EVIDENCE.md`
- `OPEN_ISSUES.md`
- `HANDOVER.md`
- `CODEX_REVIEW_REQUEST.md`
- `CPF_DELETE_MANIFEST.csv`
- `CPF_DELETE_ONE_LINE.ps1.txt`
- Apply one-line
- Verify one-line
- Package Manifest
- SHA-256
- Runtime-only matrix
- QA A/B 실행 요청서 또는 QA 인계 Section

현재 Canonical/Current 문서는 동일 경로를 갱신한다. 별도 Session/REV duplicate 문서를 만들지 않는다.

---

# PART S. 개발 진행률 보고

## NXT-PROG-001 — 실제 분모 기반 진행률

장시간 작업 중에는 작업을 멈추지 않고 진행률을 표시한다.

최소:

- 전체 통합 진행률
- Core Slimming: 완료/전체
- Utility/Foundation: 완료/전체
- Starter Portfolio: 완료/전체
- Consumer: 완료/전체
- Generator: 완료/전체
- Test/Harness: 완료/전체
- Documentation: 완료/전체
- Hygiene/Delete Manifest: 완료/전체
- 발견 Gap / 수정 완료 / 남음
- Runtime-only 수량
- 현재 작업 영역
- 직전 보고 이후 완료한 일
- 다음 작업

새 Gap 발견 시 전체 분모를 늘린다. 인위적 % 고정 금지.

진행 보고는 종료점이 아니다.

---

# PART T. 완료 조건

Developer는 다음 조건을 모두 만족할 때만 이번 신규 작업 개발 완료를 선언한다.

### Architecture

- Core → Starter dependency = 0
- Core → Optional Provider implementation = 0
- Foundation → Starter dependency = 0
- Core Spring Runtime pollution = 0
- Core OTel implementation = 0
- Core Health Runtime = 0
- Core Persistence implementation ownership gap = 0

### Utility

- Core simple wrapper = 0
- Owner-less Utility = 0
- Unified Utility actual consumer 존재

### Transaction

- Transaction ID Contract/Implementation 분리 완료
- transactionId E2E propagation gap = 0

### Persistence

- JDBC/MyBatis/JPA parity gap = 0
- CRUD/Paging/Search gap = 0
- DB3 gap = 0
- JPA 0-footprint gap = 0

### Operations

- Health/Readiness/Liveness gap = 0
- Multi-instance health/ADM gap = 0
- Graceful drain gap = 0

### Security

- OAuth2/JWT/OIDC/SSO gap = 0
- Distributed Session gap = 0
- Secret/KMS/HSM/Signature gap = 0

### Modern Capability

- Object Storage gap = 0
- Event Schema Governance gap = 0
- GraphQL Optional gap = 0
- Realtime gap = 0
- Lock/Lease gap = 0
- AI gap = 0
- Testkit gap = 0

### Product Integration

- Consumer-less API/SPI = 0
- Wrapper-only Starter = 0
- Generator impact gap = 0
- Golden/Generated Domain gap = 0
- OpenAPI/Generated Client gap = 0
- ADM/BZA gap = 0
- EDU/Reference gap = 0

### Documentation / Hygiene

- Canonical currentization 완료
- Requirement Count 정합성
- Continuity Ledger 정합성
- Matrix 정합성
- stale/duplicate docs = 0
- Duplicate relocation = 0
- Garbage candidate 누락 = 0
- Delete Manifest exact path 오류 = 0
- Protected delete = 0
- Windows path 문제 = 0

### Verification

외부 환경만 필요한 검증을 제외하고 Developer-remediable FAIL = `0`.

그때만:

`현행 요건상 개발GPT 추가 구현 없음 / Remaining=Runtime-only verification`

으로 기록할 수 있다.

---

# PART U. Developer 완료 후 QA 인계 원칙

이번 신규 개발이 끝나면 **추가 Developer 반복보다 QA A/B 전수 교차검수를 우선한다.**

순서:

```text
Developer 구현/자체검수
→ 최신 master successor 확인
→ QA A 100% 전수
→ QA B 100% 전수
→ A/B Cross Matrix
→ QA Finding이 있으면 동일 Requirement ID로 재개발/재검수
→ Runtime-only는 Codex/실환경 검증
→ QA 최종 판정
```

Developer 자체 완료는 QA PASS가 아니다.

QA A와 QA B 모두 통과하지 않은 Requirement는 전체 완료로 판단하지 않는다.

---

# PART V. Git / 삭제 안전

사용자 명시 승인 없이 다음을 실행하지 않는다.

- Commit
- Push
- Branch
- Tag
- PR
- Release
- Reset
- Restore
- Stash
- Clean
- 실제 File Delete
- History 변경

다음 명령은 금지:

- `git clean`
- `git reset --hard`
- `git restore .`

삭제는 exact `CPF_DELETE_MANIFEST.csv` + 사용자 승인 + 안전한 PowerShell 한 줄로만 수행한다.

---

## 최종 판정 문구

이 신규 작업의 목적은 기능 수를 늘리는 것이 아니다.

최종 CPF는 다음이어야 한다.

```text
Core
= 최소 계약과 의미론

Pure Foundation
= 정말 필요한 topology-independent 공통 구현

Capability / Provider
= 실제 기술 구현

Starter
= 고객 애플리케이션에서 쉽게 켜고 쓰는 개발자 경험

Application
= 필요한 Starter만 선택하여 조립
```

Core는 Starter를 모른다.

Starter는 Core Contract를 구현/조립한다.

Optional 기능은 미사용 시 0-footprint다.

신규 기능은 Consumer, Generator, Test, Operations, Documentation까지 연결된다.

Repository에는 현재 제품 정본과 필요한 Source만 남고, relocation garbage와 duplicate artifact를 남기지 않는다.

Developer 완료 후 QA A/B가 같은 전체 범위를 각각 직접 전수검수하고 교차대조할 수 있어야 한다.


---

# PART W. Tool / Gate / Script Currentization

## NXT-HYG-005 — Verification Tool Consumer 전수검수

`cpf-tools/**`, `.github/workflows/**`의 검증·Gate·Helper Script를 현재 제품 기준으로 전수 Inventory한다.

대상:

- `*.py`
- `*.ps1`
- `*.sh`
- Gradle verification helper
- 날짜/QA회차 이름의 verification directory
- `final-*`, `qa38`, `qa39`, `r6*` 등 과거 캠페인 도구
- 일회성 migration/currentization/overlay helper

각 파일은 반드시 아래 중 하나로 판정한다.

- `KEEP_CANONICAL_GATE`
- `MERGE_INTO_CANONICAL_GATE`
- `RENAME_CURRENT`
- `REMOVE_CANDIDATE`

Consumer 판정은 다음을 실제 검색한다.

- GitHub Workflow
- Gradle task
- `verify-full-product.ps1`
- 다른 script
- Runbook
- README/Developer Guide
- QA/Release process

파일 이름이 오래됐다는 이유만으로 제품 Gate를 삭제하지 않는다.
반대로 Consumer가 없고 현재 Canonical Gate가 같은 검증을 수행한다면 과거 캠페인 Script를 유지하지 않는다.

특히 작성 시점에 존재하는:

- `cpf-tools/verification/final_dev_campaign.py`
- `cpf-tools/verification/release_target_trust.py`
- `cpf-tools/verification/verify_integration_closure_contract.py`
- `cpf-tools/verification/verify_starter_catalog.py`
- 날짜형 `cpf-tools/verification/20260728_*`, `20260729_*`, `20260801_*`
- `qa38`, `qa39`, `final-dev`, `java21`

을 직접 Consumer 기준으로 판정한다.

현재 기준 Canonical 통합 검증 Entry는
`cpf-tools/scripts/verify-full-product.ps1`이며, 개별 Gate는 이 Script/CI에서 호출되거나
독립 Runtime Harness로 명확한 Consumer를 가져야 한다.

### Acceptance

- Consumer 없는 verification/helper = 0
- 동일 검증을 수행하는 중복 Gate = 0
- 날짜/QA 회차 전용 history tool이 Current product gate처럼 남는 건 = 0
- 실제 CI/Release Consumer가 있는 Gate 오삭제 = 0
- 제거 대상은 exact Delete Manifest에 기록
- `cpf-tools/build/**`가 ignore 누락으로 사라지지 않는지 확인

---

## NXT-HYG-006 — Development Document Consolidation

개발 관련 모든 문서를 최신 Source와 본 Request 기준으로 현행화한 뒤 문서 종류 자체를 최소화한다.

Narrative Current Owner는 다음 7개를 기본으로 한다.

1. `CPF_CURRENT_WORK_REQUEST.md`
2. `REQUIREMENT_STATUS.csv`
3. `REVIEW_INDEX.md`
4. `TEST_AND_EVIDENCE.md`
5. `HANDOVER.md`
6. `CPF_CHANGE_MANIFEST.csv`
7. `CPF_DELETE_MANIFEST.csv`

`CPF_REQUIREMENT_MATRIX.csv`, `CPF_SCENARIO_MATRIX.csv`, `current/**`의 대용량 Part는
Narrative 문서가 아니라 논리 Dataset으로 관리한다.

다음은 Current Owner에 내용이 흡수되면 제거한다.

- 세션별 Handover
- 세션별 Review Index
- 세션별 Test/Evidence
- 세션별 Open Issues
- 세션별 Codex Request
- REV/FINAL/Checkpoint 결과
- 과거 Package Manifest/Hash
- 과거 단발 Audit Matrix
- 동일 목적 Starter/Public Surface/Source Findings Snapshot
- `cpf-docs/work/v9i/**` 같은 누적 역사 Workspace

Git history가 과거를 보존한다.

문서 수를 줄이기 위해 내용을 축약하지 않는다. 남기는 문서는 Architecture 이유, Owner, Consumer,
오류/복구, Security, DB3, Generator, Runtime, QA Acceptance를 상세히 보존한다.

Developer/QA/Codex는 임의의 새 관리 문서 종류를 생성하지 않는다.

# P0 Unified Context / Standard Header / Mandatory Batch Closure

이 절은 신규 분모를 추가하는 별도 Requirement가 아니라
`NXT-TXID-001`, `NXT-ARCH-002/003`, `NXT-OWN-001/003`, `NXT-UTIL-001/002`, `NXT-HYG-001`, `NXT-TESTKIT-001`
Acceptance를 상세화한다.

## P0-CTX-01 Core Context 분해

현행 `common/logging/TransactionContext`, `TransactionHeader`, `api/logging/CpfTransactionContext`를 파일별 전수 판정한다.

목표:
- Core semantic Context
- HTTP Header Adapter
- Observability Adapter
- Owner-specific Context
를 분리한다.

Core의 `MDC`, `RequestContextHolder`, OTel Runtime, HTTP Header Runtime dependency = 0.

## P0-CTX-02 Header Policy

`CpfHeaderSpec` 또는 replacement는:
semantic owner / scope / trust / source / mutation / log/mask / max length / aliases / direction / compatibility를 표현한다.

blind propagation 금지:
- Idempotency-Key
- API Version
- Caller Service
- Raw Forwarded chain
- Authorization/API Key
- Legacy Trace duplicate outbound

## P0-CTX-03 Mandatory Batch

Core Context 변경과 동시에:
- cpf-batch/contract Batch Context
- execution-runtime Adapter
- Scheduler
- Job/Step
- Partition/Worker
- Center-Cut
- Restart
- Process Kill
- Multi-instance
- UNKNOWN/Reconcile
- Batch Testkit
- Batch Log/Audit/ADM
를 실제 Source/Consumer/Test로 연결한다.

한 항목이라도 개발 가능한데 미연결이면 NXT-TXID-001 미완료.

## P0-CTX-04 Mandatory Messaging/Async/Web/Gateway

실제 Repository에 존재하는 Runtime/Provider를 전수 연결한다.
Context capture/restore/clear와 trust/spoof/alias/per-hop policy를 검증한다.

## P0-CTX-05 Generator/EDU/ADM

Generated consumer compile, EDU 11개 핵심 시나리오, ADM Timeline을 동일 Context 모델로 연결한다.

## P0-CTX-06 Garbage Closure

old logging/header/context/batch/centercut Source/Test/Resource/Metadata를 replacement consumer 전환 후 실제 제거한다.
Delete Manifest만 작성하고 종료 금지.

## P0-ROOT-01 Permanent Root Freeze

사용자 승인 없이 Repository Root에 신규 file/directory/module 생성 금지.
Architecture Gate가 항상 검출해야 한다.
