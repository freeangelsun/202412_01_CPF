# CPF QA32 OSS-first 전면 교체 및 전 저장소 결함 수정 개발 요청

## 1. 작업 목표

이번 개발은 기존 기능에 OSS 의존성을 몇 개 추가하는 작업이 아니다.

1. 검증된 permissive OSS를 범용 기술의 **유일한 Primary Path**로 전환한다.
2. CPF는 제품 고유의 정책, 계약, 승인, 버전, 감사, Evidence, Control Plane만 소유한다.
3. QA31과 이후 타 AI 감사에서 확인한 공통 결함 패턴을 특정 파일에 한정하지 않고 **저장소 전체 개발 방식**으로 수정한다.
4. 모든 변경은 최신 exact SHA에서 실제 Runtime·Failure·Recovery Evidence를 갖춰야 한다.
5. 부분 구현을 남긴 채 `완료`라고 기록하는 것을 금지한다.

## 2. 기준 상태

- Source review baseline: `3249581e8f01dcb546bc6601c31aee525f564d21`
- 최신 Commit message: `20260730_11`
- QA31 결과 보고서가 선언한 상태:
  - Requirement + Scenario 165건: 완료 0, 부분 구현 73, 미검증 92
  - Defect 23건: 완료 0, 부분 구현 20, 재확인 필요 3
  - Java 25 전체 Gradle, Frontend 전체 검증, 3DB, Runtime, Browser: 미실행
- 최신 Commit의 GitHub Combined Status는 패키지 작성 시점에 비어 있었다.
- QA31 결과 문서는 이전 SHA와 WORKTREE 상태를 유지하므로 최신 Push의 완료 Evidence로 사용할 수 없다.

## 3. 최상위 OSS 전환 결정

### 3.1 즉시 Primary Path로 전환

- UI Widget: **Element Plus**
- 고급 Table: **TanStack Table**
- Routing: **Vue Router**
- Client State: **Pinia**
- Server State: **TanStack Vue Query**
- Form Validation: **Zod + Element Plus Form**
- ADM/BZA API Client: **Orval**
- Browser Security: **BFF + Spring Security + Spring Session JDBC**
- Gateway: **Spring Cloud Gateway Server Web MVC + Embedded Tomcat**
- Messaging: **Apache Kafka**
- Unit Test Messaging: **CPF In-memory Test Adapter**
- Kafka Integration: **Testcontainers Kafka**
- Resilience: **Spring Cloud CircuitBreaker + Resilience4j**
- DB Migration: **Flyway OSS Core**
- Observability: **Micrometer Observation + OpenTelemetry OTLP**
- Local Cache: **Caffeine**
- Compliance: **CycloneDX + ORT + Syft + Grype**
- Browser E2E: **Playwright**

### 3.2 제한 범위 또는 선택 Adapter

- Spring Batch: Job/Step/JobRepository/ExecutionContext/Restart 범위
- db-scheduler: 기본 persistent Trigger
- Quartz: 고급 Calendar/JTA 요구가 ADR로 입증된 경우만
- Valkey: 분산 Cache 선택 Adapter; Server binary를 제품에 번들하지 않음
- OpenFeature: Feature Flag 요구가 실제 범위일 때 CPF Provider와 함께
- SecretProvider SPI: 외부 고객 관리 Secret Service 연결

### 3.3 현재 개발 범위에서 제외

- `cpf-gateway-webflux.jar`
- WebFlux/Netty Gateway Data Plane
- Envoy Data Plane
- Kafka와 병행되는 Artemis/RabbitMQ Primary
- Flowable: 복잡한 사람 중심 Workflow 필요성 ADR 없이는 도입 금지
- Redis Server 기본 번들
- HashiCorp Vault Server 기본 번들
- Flyway Teams/Enterprise
- PrimeVue 최신 및 유료 PrimeVue 자산

## 4. OSS 전환의 강제 완료 순서

각 Change ID는 아래 10단계가 모두 끝나기 전 완료가 아니다.

1. 현재 구현과 모든 Consumer Inventory
2. License·Version·전이 의존성 검증
3. ADR 및 Target Module/Owner 확정
4. OSS Adapter/Wrapper와 실제 수직 Slice 구현
5. 기존/신규 동일 Scenario Functional Parity
6. Performance·Security·Failure·Recovery Parity
7. 모든 Consumer 이관
8. Legacy Source·Bean·Route·Dependency·Artifact 제거
9. Generated Source·Published POM·SBOM·Final Artifact 재검증
10. 최신 exact SHA Evidence와 Result Matrix 완료

Dual-run은 데이터 이관 또는 무중단 전환에 필요한 기간에만 허용하며 Change ID, 종료일, owner, reconciliation과 rollback을 가져야 한다. 최종 Release에는 하나의 Primary Path만 남긴다.

## 5. 핵심 구현 지시

### 5.1 Core/Common 모듈 경계

현재 `cpf-core`와 `cpf-common`의 `api` 의존성을 기능별 모듈로 분리한다.

예시 목표:

```text
cpf-core-api
cpf-core-spi
cpf-core-autoconfigure
cpf-starter-web
cpf-starter-security
cpf-message-api
cpf-starter-kafka
cpf-message-testkit
cpf-cache-api
cpf-starter-cache-caffeine
cpf-starter-cache-valkey
cpf-starter-excel
cpf-starter-mybatis
cpf-starter-observability
```

이름은 Source 구조 검토 후 조정 가능하지만 다음 결과는 필수다.

- 기술 구현 의존성이 Public API에 전파되지 않는다.
- 기능 미사용 Consumer가 Kafka/AMQP/WebFlux/Redis/POI/OpenAPI/MyBatis를 받지 않는다.
- Published POM과 Gradle module metadata가 Source 의도를 반영한다.
- MyBatis는 Boot 4 호환 4.x 한 버전만 사용한다.
- BOM, lockfile, SBOM에 동일 버전이 나타난다.

### 5.2 ADM/BZA Frontend

- Element Plus/TanStack Table Wrapper를 만든 뒤 모든 화면을 실제 이관한다.
- Vue Router가 Deep Link/Guard/Forbidden/Not Found를 소유한다.
- Pinia에는 client/UI state만 두고 서버 응답은 TanStack Query로 관리한다.
- Zod는 frontend 검증을 담당하지만 Backend 검증이 정본이다.
- Orval이 OpenAPI에서 API Client/Query hook을 생성한다.
- raw `fetch`와 endpoint 문자열은 공통 mutator 등 좁은 allowlist 외 금지한다.
- 대형 mixin·custom generic table/pager/dialog/hash router를 Consumer 이관 후 제거한다.
- Browser Storage에는 민감 Token을 저장하지 않는다.

### 5.3 BFF·Session

- ADM/BZA Backend가 각 Frontend의 BFF 역할을 한다.
- Server-side Session은 Spring Session JDBC가 기본이다.
- Cookie: Secure, HttpOnly, SameSite, 명시 Path/Name, 적절한 Max-Age.
- CSRF, Session Fixation, Force Logout, Idle/Absolute Timeout, Concurrent Session, Role 변경 재검증을 구현한다.
- Gateway 이후 내부 통신은 Browser Session Cookie를 그대로 전달하지 않는다.

### 5.4 Gateway

- Spring Cloud Gateway Server Web MVC를 실제 요청 처리 Primary로 사용한다.
- CPF Control Plane의 Route/Binding/Server Group/Approval/Version/Checksum/ACK/NACK/Safety/Ledger는 유지한다.
- CPF 정책을 SCG Predicate/Filter/Route에 매핑하는 Adapter를 구현한다.
- `cpf-gateway.jar` 하나만 생성하는 실행형 BootJar로 고정한다.
- Embedded Tomcat을 포함한다.
- `bootWar`와 WebFlux/Netty Gateway Artifact는 제거한다.
- 현재 자체 HTTP Forwarding 구현은 모든 Consumer 이관 후 제거한다.
- 성능 확장은 Web MVC Instance Scale-out을 우선한다.

### 5.5 Kafka

- Kafka는 Product Messaging Primary다.
- Core API에 Kafka 구현 의존성을 노출하지 않는다.
- CPF Message Contract는 Header, schema/version, correlation, idempotency, attempt, DLT 의미를 소유한다.
- 기본 전달 의미는 `at-least-once + consumer idempotency`다.
- Unit Test는 단순 In-memory Adapter를 사용한다.
- Kafka 기능 완료 판정에는 Testcontainers Kafka Integration이 필수다.
- Partition, ordering, rebalance, duplicate, redelivery, DLT, Broker outage, offset recovery를 검증한다.
- AMQP/Rabbit/Artemis를 동시 Primary로 남기지 않는다.

### 5.6 Supply Chain

- CycloneDX: Gradle/npm resolved graph SBOM
- ORT: License, policy, NOTICE, source/compliance
- Syft: 최종 JAR/ZIP/Container/Filesystem 실제 구성
- Grype: 최종 Artifact vulnerability
- 네 결과를 상호 대조해 Source에 없지만 Final Artifact에 존재하는 Package, shaded/bundled dependency, Unknown License, 금지 License, CVE를 차단한다.

## 6. 기존 결함 전수 수정

`CPF_20260730_QA32_REQUIREMENT_MATRIX.csv`의 기존 44개 공통 요건은 발견 파일만 고치는 목록이 아니다.

- Java/Groovy/Kotlin
- Gradle와 Included Build
- PowerShell/Shell/Python
- Generator Template와 Generated Source
- SQL/Vendor Pack
- Runtime Configuration
- JAR/WAR/ZIP/SBOM/Manifest
- Local/Remote/Offline
- Windows/Linux
- Unit/Integration/Runtime/Browser

모두에 동일 패턴과 변형 패턴을 적용한다.

## 7. 필수 실행 Gate

### Build/Architecture

- `git diff --check`
- Java 25 전체 Gradle clean/test/check
- dependency convergence/lock/POM/BOM/SBOM
- public API leakage/jdeps/architecture tests
- generated domain create/recreate/export/standalone
- Local/Remote/FULL_OFFLINE
- Windows/Linux PowerShell

### Frontend

- clean npm cache 또는 격리 cache
- `npm ci`
- lint/typecheck/unit/production build
- Orval clean regeneration
- Playwright Chromium/Firefox/WebKit
- accessibility, role/route/action, server paging/sort/filter, large data, download, partial failure

### Runtime

- Gateway route/filter/scale-out/load/fault
- Kafka real integration
- Batch/Scheduler/Worker/Agent
- BFF multi-instance session
- process kill, network timeout, response loss, duplicate retry, lock contention, disk full
- security negative corpus

### Database

MariaDB/PostgreSQL/Oracle 각각:

- fresh install
- N-1 upgrade
- failed migration recovery
- checksum tamper
- backup/restore
- rollback script 또는 forward-fix 정책
- reinstall and schema parity

### Release

- exact SHA provenance
- CycloneDX/ORT/Syft/Grype
- license/NOTICE
- final artifact unpack
- reproducibility
- current SHA CI evidence

## 8. 완료 결과 파일

개발 종료 시 다음 파일을 새로 작성한다.

- `cpf-docs/work/review/CPF_20260730_QA32_PRE_DEVELOPMENT_REVIEW.md`
- `cpf-docs/work/review/CPF_20260730_QA32_DEVELOPMENT_COMPLETION_REPORT.md`
- `cpf-docs/quality/CPF_20260730_QA32_RESULT_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA32_UNRESOLVED_REGISTER.csv`
- `cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_RESULT.csv`
- `cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER_RESULT.md`
- `cpf-docs/work/current/CPF_20260730_QA32_CODEX_REVIEW_READY.md`
- `cpf-docs/evidence/current/**`

## 9. 사용자 전달

- 개발 결과는 프로젝트 Root 상대경로 ZIP으로 생성한다.
- 변경 Source·Build·SQL·Test·Evidence·Result·Manifest를 포함한다.
- `.git`, cache, `node_modules`, secret, private key, credential, 실제 DB dump는 제외한다.
- ZIP SHA-256과 파일 수를 출력한다.
- 사용자에게 실제 다운로드 가능한 링크를 제공한다.
- 사용자의 명시 승인 전 Commit·Push·Branch·Tag·PR을 만들지 않는다.
