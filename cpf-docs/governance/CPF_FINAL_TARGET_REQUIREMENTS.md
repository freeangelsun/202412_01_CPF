# CPF 최종 목표 요구사항 정본

> Canonical path: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
> Revision date: `2026-08-09`
> Previous reviewed blob SHA: `f5650f502fc11d87afb92f775588c710a02373d4`
> Currentization source master SHA: `4c4248a12e699c07f9f5fb11fbb33b97ca04077d` (`07_16`)
> Canonical Requirement Count: **186개**
> Legacy Alias: **8개** — 완료율 중복 집계 금지

### Core Kernel / Root Layout 강제 정책

Core는 기능 수를 줄이기 위한 빈 껍데기가 아니라 CPF 전체의 **헌법/Kernel**이다.
전역 Error/Result/Outcome, Transaction/Execution Context, transactionId/lineage,
UNKNOWN/Reconcile/Idempotency, 최소 Identity/Security/Tenant Context와 장기 안정적인 공통 Value/Contract를 보존한다.
특정 Owner/Optional Capability의 API/SPI/DTO/Operations는 Core에 두지 않는다.

Repository 물리 구조는 Root를 임의 확장하지 않는다.
Pure Foundation은 `cpf-starters/foundation/core`, 공식 범용 Test Support는
`cpf-tools/testing/cpf-testkit`에 둔다. 논리 Gradle project 이름은 호환성을 위해 유지할 수 있다.
새 Root 파일/Directory는 사용자 명시 승인과 Canonical Root Allowlist 변경 없이는 금지한다.

Core Closure Gate는 unknown class의 자동 KEEP를 금지하고,
Owner-specific/Optional API·SPI residue, Runtime/Operations residue, invalid logging operations,
old/new duplicate, stale reference, moved-source residue, empty migrated directory가 하나라도 남으면 FAIL한다.

## 1. 문서 목적과 정본성

이 문서는 **Core Platform Framework(CPF)**의 최상위 제품 목표, 장기 Architecture, Module Ownership, Public Contract, 운영·보안·배포 품질, 최종 완료 판정과 Requirement Catalog를 정의하는 최우선 정본이다.

이 문서는 작업 일지, 현재 진행률, 특정 QA 회차의 완료 보고 또는 날짜별 Evidence 저장소가 아니다. 구현 상태는 Current Request, Gap/Result Matrix, Review, Handover와 Evidence가 관리하지만, 모든 요구 도출·구현·검수·완료 판정은 이 문서에 종속된다.

하위 문서나 구현이 이 문서와 충돌하면 다음 순서로 처리한다.

1. 실제 최신 Git 구현과 실행 결과를 확인한다.
2. 이 문서의 제품 목표와 Architecture 원칙에 맞는 Owner를 결정한다.
3. 하위 문서·Source·SQL·API·Test·Generator·Guide·Evidence를 함께 이관한다.
4. 잘못된 Legacy와 중복 정본을 제거한다.
5. Requirement Continuity Ledger에 ID 이동·분해·통합 근거를 남긴다.

## 2. 규범 용어와 완료 상태

- **MUST / 필수**: GA 완료에 반드시 충족해야 한다.
- **MUST NOT / 금지**: 존재하면 Release 또는 완료 판정을 차단한다.
- **SHOULD / 권고**: 미적용 시 ADR과 대체 통제를 요구한다.
- **MAY / 선택**: 제품 정책에 따라 활성화할 수 있으나 선택하지 않은 Runtime을 강제 의존시키면 안 된다.

허용 상태는 다음뿐이다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

`개발 완료`와 `검증 완료`는 서로 다른 축이다. Source가 존재해도 실제 Consumer, 오류·복구, 다중 인스턴스, 보안·운영, 최신 exact-SHA Evidence가 없으면 전체 완료가 아니다.

## 3. 제품 정의와 최종 결과

CPF는 금융권을 포함한 다양한 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화할 수 있는 **Business Platform 품질의 상용 Framework**다. 단순 공통 Library, Sample, 예제 모음, 특정 프로젝트의 Base Code가 아니다.

최종 제품은 다음을 하나의 일관된 제품 구조로 제공해야 한다.

- MSA와 Modular Monolith
- 동일 JVM Local Call과 분리 WAS Remote Call
- Embedded JAR, External WAS WAR, 독립 Static Web Artifact와 독립 Worker Process
- Multi-instance, 부분 실패, retry, failover, restart, reconciliation과 DR
- 금융권 수준의 인증, 권한, 승인, 감사, masking, 개인정보와 credential 통제
- idempotency, async, outbox/inbox, DLQ, compensation, unknown-result recovery
- Gateway, 외부 REST/전문/파일, Kafka messaging
- Spring Batch, Scheduler, Agent, Runner, Worker와 Center-Cut
- ADM/BZA 운영 조회·제어·승인·감사·통계·incident
- 표준 Generator와 신규 업무 Domain lifecycle
- OpenAPI, JavaDoc, Test Kit, EDU와 실제 Reference Runtime
- install, migration, upgrade, rollback/forward recovery, backup/restore, deploy와 artifact trust
- Source, SQL, API, Test, Config, Frontend, Script, Guide와 Evidence의 양방향 일치

단기 구현 편의보다 장기 제품 구조, 확장성, 운영성, 보안성, 복구 가능성, 재현성과 상용 배포 가능성을 우선한다.

## 4. 지원 Topology와 동등성

공식 지원 Topology:

- Embedded Boot JAR
- External WAS WAR
- Modular Monolith
- 독립 Microservice
- 동일 JVM Local Facade
- 분리 WAS Remote Facade
- ADM/BZA 독립 Static Artifact + Web Server
- Gateway 독립 Runtime
- Agent/Runner/Worker 독립 Process
- Multi-instance와 Multi-zone
- Rolling, Canary, Blue-Green
- Backup/Restore와 DR Failover/Failback

Topology가 달라도 다음 계약은 동일해야 한다.

- 업무 요청·응답 DTO와 validation
- Standard/Extension Header
- transactionId, trace, segment와 attempt
- authentication/authorization와 service identity
- timeout budget, idempotency와 error taxonomy
- audit, masking와 observability
- version/compatibility와 failure/recovery semantics

Local 구현이 Remote보다 기능이 적거나, Remote 전환을 위해 업무 Source를 다시 작성하거나, 내부 호출이 필수적으로 Gateway를 경유하면 Architecture 실패다.

## 5. 공식 Module·식별·Ownership

| 역할 | Module | Java Root Package | SystemCode | 필수 Owner 책임 |
|---|---|---|---:|---|
| 기술 공통 Framework | `cpf-core` | `com.cpf.core` | CPF | CPF 전역 Kernel: topology-independent Contract/Semantics/Value와 최소 순수 Logic. 특정 Owner/Optional Capability API·SPI와 Runtime 구현은 소유하지 않음 |
| 고객 업무 공통 | `cpf-common` | `com.cpf.common` | CMN | 고객 공통 정책, Core SPI 확장, Calendar/Code/Message 등 명시된 고객 공통 |
| 플랫폼 관리자 | `cpf-admin` | `com.cpf.admin` | ADM | 플랫폼 운영 Control Plane, 플랫폼 위험조치 승인과 운영자 감사 |
| 고객 업무 관리자 | `cpf-biz-admin` | `com.cpf.bizadmin` | BZA | 고객 업무 관리, 조직·업무 결재, 선택형 Customization Sample |
| Batch 실행 기반 | `cpf-batch` | `com.cpf.batch` | BAT | Spring Batch, Scheduler, Center-Cut, Agent, Runner, Worker |
| Gateway Runtime | `cpf-gateway` | `com.cpf.gateway` | GWY | 외부 진입, trust boundary, route/load balance/resilience와 attempt ledger |
| Golden Generated Domain | `cpf-member` | `com.cpf.member` | MBR | Generator 산출물과 동일한 최소 업무 Domain Reference |
| 교육·참조 | `cpf-reference` | `com.cpf.reference` | REF | 제품 Public API의 실제 EDU·복구·운영 예제 |

영구 고정 `cpf-external`/EXS Module은 두지 않는다. 외부기관 Adapter는 범용 기술 Contract/SPI, 고객 공통 정책, Generated Domain 또는 고객 확장 Module 중 실제 Owner가 소유한다.

### 5.1 의존성 방향

```text
Generated/Business Domain → cpf-common → cpf-core
cpf-gateway → cpf-core Public Contract + 선택 Starter
cpf-batch → cpf-core Public Contract + Business Public Contract
cpf-admin → Operations Command/Query Contract
cpf-biz-admin → Business Public Contract
Customer Adapter/Plugin → cpf-common/core SPI
```

금지:

- `cpf-core`의 Common/Admin/Batch/업무 역방향 의존
- 선택 기능 Runtime(Kafka, Redis, OTel exporter 등)의 Core 강제 포함
- 업무 Domain 간 DB 직접 접근
- ADM/BZA의 Owner DB 직접 갱신
- 내부 호출의 Gateway 재경유
- 순환 의존과 Internal Package 직접 참조
- 실제 Product Consumer 없는 Interface/Adapter/Starter
- OSS와 Legacy의 Dual Primary
- Sample 또는 Generated Reference를 제품 원장으로 간주

### 5.2 Public API / SPI / Internal

```text
com.cpf.<owner>.api
com.cpf.<owner>.spi
com.cpf.<owner>.internal
```

- Public API는 semantic version과 compatibility 대상이다.
- SPI는 capability, lifecycle, failure, thread-safety와 version contract를 문서화한다.
- Internal은 외부 Module에서 compile되지 않도록 module metadata, package rule, ArchUnit와 publication gate로 차단한다.
- Public API에 선택 OSS 구현 type을 직접 노출하지 않는다.
- Public API/SPI와 중요 복구·동시성·보안 로직에는 한글 JavaDoc/주석을 제공한다.


### 5.3 Lightweight Core·Starter·Capability Profile

`cpf-starters/`는 CPF의 정식 Root 제품 영역이다. `cpf-core`는 Spring Boot 선택 Runtime을 직접 조립하는 범용 실행 모듈이 아니라, CPF 전역 Kernel로서 topology-independent Contract/Semantics/Value·표준 식별자·오류·문맥·최소 순수 Logic만 제공하는 초경량 Artifact여야 한다. Provider-neutral이라는 이유만으로 Core 소유가 정당화되지 않으며, 특정 Owner 또는 Optional Capability에만 필요한 API/SPI/DTO/Port도 해당 Owner/Capability가 소유한다.

선택 기술은 다음 계층으로 제공한다.

1. **Leaf Starter**: 하나의 기술 Capability와 AutoConfiguration을 소유한다.
2. **Generator Capability Profile**: 사용 사례를 승인된 Leaf Starter 목록으로 해석하고 Domain Manifest에 `resolvedStarters`와 버전을 고정한다.
3. **Aggregate Starter**: 안정성이 입증된 조합에 한해 전이 Dependency만 제공하며 고유 Bean·AutoConfiguration을 소유하지 않는다.
4. **Platform BOM**: 버전만 정렬하며 Capability 선택을 대신하지 않는다.

대표 Starter 하나가 의존 Starter를 자동 포함하는 것은 Gradle 전이 Dependency로 가능하다. 다만 기존 Domain의 묵시적 변경을 막기 위해 Generator Profile이 해석된 Leaf 목록과 Profile Version을 Manifest에 고정하는 방식을 우선한다.

다음을 금지한다.

- `all`, `full`, `everything` 형태의 Mega Starter
- Starter 선택만으로 업무·Admin·Batch·Gateway 고유 정책이 유입되는 구조
- 상호 배타 Provider의 무승인 동시 활성화
- 선택하지 않은 Starter의 JAR·Bean·SQL·Config·Secret 요구
- Core와 Starter에 동일 Primary AutoConfiguration·Adapter가 동시에 남는 구조
- Consumer 없는 Starter를 GA 완료로 처리하는 행위



### 5.2 제품 제공 영역과 EDU/Reference 경계

CPF가 제품으로 제공하는 Runtime/Application 자체와 CPF 도입 개발자가 직접 개발해야 하는 영역을 구분한다.

- `cpf-admin`의 ADM은 플랫폼 운영 Control Plane **제품**이다. CPF 도입 개발자가 ADM 자체를 다시 개발하는 교육 대상이 아니다.
- `cpf-biz-admin`의 BZA도 고객 업무 관리 제품/확장 Surface이며, 제품 본체의 내부 기능을 EDU에 복제하지 않는다.
- `cpf-reference`/EDU는 CPF 도입 개발자가 실제로 사용해야 하는 **Public API, Public SPI, 공식 Extension Point, Integration Contract, Generator 산출물 사용법**을 실행 가능한 예제로 교육하는 영역이다.
- ADM/BZA/Gateway/Batch 내부 구현을 이름만 바꾼 Generic Handler/JDBC 예제로 EDU에 중복 구현하지 않는다.
- ADM/BZA 제품 기능의 완전성은 해당 Product Source/API/Frontend/SQL/Test/Runtime/Manual에서 검증한다.
- ADM/BZA와 관련된 EDU는 외부 Consumer가 실제로 구현·호출하는 공식 Public Extension/Integration 시나리오일 때만 유지한다.
- EDU 수량은 그 자체가 목표가 아니다. Canonical EDU Catalog의 수량은 Public Consumer 교육 필요성과 Architecture Ownership에 의해 결정한다.
- 기존 EDU ID를 축소·통합·재분류해야 할 경우 QA가 Source/Consumer/Generator/Manual/Test 영향도를 검토하고 정본 Requirement를 먼저 갱신한다. 개발GPT가 QA 원장을 임의 삭제하거나 완료 처리하지 않는다.

## 6. 모든 Requirement에 적용되는 공통 완료 축

각 Requirement는 적용 가능한 항목을 모두 충족해야 한다. `N/A`는 이유와 검수 승인이 있어야 한다.

| 완료 축 | 필수 기준 |
|---|---|
| Ownership | 단일 Owner Module, Public API/SPI/Internal 경계, 역방향·순환 의존 없음 |
| Consumer | 실제 Product Consumer, Bean/Route/SQL/Frontend/Script 연결, Dead abstraction 없음 |
| 정상 기능 | 대표 정상 흐름과 실제 Runtime 결과 |
| 오류·경계 | invalid input, 권한, timeout, conflict, empty, oversize, dependency failure |
| 동시성 | race, optimistic/distributed lock, idempotency, duplicate와 multi-thread |
| Multi-instance | lease, fencing, rebalance, failover, stale writer와 shared state |
| 결과 불명 | side effect 전후·DB commit 전후·ACK/response loss 분류와 reconciliation |
| 복구 | retry, restart, reprocess, compensation, rollback/forward recovery와 manual recovery |
| Security | authN/authZ, trust boundary, secret/PII masking, negative corpus와 secure default |
| Audit/Operations | 조회, status, control, reason, approval, immutable audit, metric/alert/runbook |
| Resource | memory/disk/thread/connection/queue/time budget, bounded streaming, cleanup |
| Data/DB | schema/query owner, 3 Vendor 또는 DB-less 근거, migration/rollback/drift |
| Compatibility | Local/Remote, mixed version, API/message/file/DB/config compatibility |
| Test | unit, contract, integration, runtime/browser/broker/fault 중 적용 항목 |
| Documentation | OpenAPI, JavaDoc, developer/operation/install/recovery guide |
| Evidence | exact Source SHA, command, environment, time, exit code, report/artifact hash, sanitization |
| Hygiene | Legacy/Dead Code/Stale Evidence/임시 산출물/Secret 제거와 회귀 방지 |

## 7. 기술 정본과 OSS Primary 정책

기술 Stack의 exact version은 `gradle/cpf-stack.properties`, Wrapper, BOM과 Lockfile을 단일 정본으로 한다. 이 Revision의 목표 baseline은 Java 25 LTS, Gradle 9.x, Spring Boot 4.1 계열, Spring Cloud 2025.1 계열, Spring Batch 6 계열이며, 공식 지원 Matrix 밖 조합은 `TRANSITION`으로 관리하고 GA를 차단한다.

승인된 Primary 방향:

| 영역 | Primary 방향 | 제품 경계 |
|---|---|---|
| Gateway | Spring Cloud Gateway Server Web MVC | CPF는 route/trust/audit/ledger 정책 확장 |
| Batch | Spring Batch | Job/Step/Repository/ExecutionContext/Restart 정본 |
| Scheduler | db-scheduler 기본, 고급 Adapter 선택 | Trigger 소유권과 Batch 실행 경계 명확화 |
| Messaging | Kafka | in-memory는 unit/local test Adapter만 |
| Resilience | Spring Cloud CircuitBreaker + Resilience4j | operation 정책·timeout budget 정본 |
| Observability | Micrometer Observation + OpenTelemetry | SDK/exporter는 Starter가 소유 |
| Cache | Caffeine local + 선택형 distributed provider | `cpf-common`/Core에 선택 Runtime 강제 금지 |
| Feature Flag | OpenFeature + CPF Provider | evaluation/audit/secure override |
| Session/BFF | Spring Security + Spring Session JDBC | Browser credential 저장 금지 |
| Frontend | Vue 3, Router, Pinia, TanStack Query/Table, Zod, Orval, Element Plus | 실제 Consumer 이관과 lock/generated drift 검증 |
| Migration | Flyway OSS Core | 기존 자체 migration Primary 제거 |
| Supply Chain | CycloneDX, ORT, Syft, Grype | 동일 final artifact와 exact SHA 검사 |

Dependency나 파일만 추가하고 실제 Consumer가 Legacy를 사용하면 전환 완료가 아니다.

## 8. 거래·신뢰·오류 표준

- 거래 실행 인스턴스 ID는 `transactionId` 하나다.
- 기본 생성 규격은 `yyyyMMddHHmmssSSS(17)+SystemCode(3)+wasId(7)+sequence(7)`의 34자리다.
- **정식 거래 기동 Channel 또는 최초 기동 System은 CPF 규격의 transactionId를 최초 1회 생성할 수 있다.**
- 이후 Local/Remote/REST/SOAP/Gateway/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile 등 동일 거래의 모든 참여 구간은 **동일 transactionId를 End-to-End로 승계·보존**하며, System hop이나 재시도마다 새 transactionId를 만들지 않는다.
- 하위 호출·병렬 호출·재시도는 `segmentId`, `parentSegmentId`, `attempt`, `traceId`, `spanId` 등 세부 실행 식별자로 구분한다.
- 정식 거래 기동 Channel/System이 생성한 transactionId와 비신뢰 Client가 임의 주입·변조·재사용한 transactionId를 구분한다.
- transactionId의 신뢰 여부를 Header 존재나 형식 적합성만으로 판단하지 않고 인증된 Channel/System identity, 호출 경로와 trust policy를 함께 검증한다.
- Client가 보낸 내부 Header, principal, environment, instance ID를 무조건 신뢰하지 않는다.
- 오류는 code, message, field/offset, retryability, failure stage, unknown-result 여부와 operator guidance를 가져야 한다.
- pre-execution failure, side-effect confirmed failure, success, stopped, retryable failure와 unknown result를 구분한다.
- 결과 불명은 자동 성공이나 무조건 재시도로 닫지 않는다.


### 8.1 거래 추적·파일로그·DB로그 표준

프레임워크의 거래·대외연계·Batch·Scheduler·Center-Cut·Gateway·비동기 실행은 장애 분석과 운영 추적을 위해 동일한 식별 체계를 사용한다.

#### 거래 계보

- 최상위 거래의 `transactionId`는 호출 체인 전체에서 유지한다.
- 거래가 다른 거래, Remote Service, Gateway, Message, File, Batch 또는 비동기 작업을 호출해도 원 `transactionId`를 잃지 않는다.
- 하위 호출은 `segmentId`, `parentSegmentId`, `attempt`, `traceId`, `spanId`로 계층과 재시도를 구분한다.
- Batch 전환 시 `jobId/jobInstanceId/jobExecutionId/stepExecutionId/partitionId/itemId/agentId/workerId`와 원 `transactionId/requestId`를 연결한다.
- 외부 연계는 destination/service/operation/requestId/attempt/timeout/result/error를 거래 계보에 연결한다.
- 정식 거래 기동 Channel/System이 생성하거나 신뢰된 내부 호출 체인에서 승계된 transactionId는 End-to-End로 유지한다.
- 비신뢰 Client의 내부 transaction/instance/security context 주입·변조·replay는 trust boundary에서 차단하거나 정책에 따라 별도 신규 거래로 격리하며, 이 경우 외부 correlation 정보는 내부 transactionId와 분리해 보존할 수 있다.

#### 표준 로그 필드

로그 종류에 따라 적용 가능한 범위에서 최소 다음을 구조화한다.

`timestamp`, `level`, `systemCode`, `environment`, `instanceId/wasId`, `transactionId`, `traceId`, `spanId`,
`segmentId`, `parentSegmentId`, `attempt`, `requestId/idempotencyKey`, `actor/tenant/channel`,
`jobId/jobInstanceId/jobExecutionId/stepExecutionId/partitionId/itemId/agentId/workerId`,
`operation/endpoint/remoteSystem`, `result/status`, `errorCode`, `failureStage`, `retryable`,
`unknownResult`, `elapsedMs`, `message/file identifiers`.

민감 Payload, Credential, Token, Session, Private Key, 주민번호/계좌 등 PII는 원문 기록하지 않고 표준 masking/redaction 정책을 적용한다.

#### 파일 로그

- 제품 표준 경로·파일명·encoding·event format·rotation·compression·retention·권한을 정의한다.
- 다중 인스턴스에서 파일 충돌이나 교차 기록이 없도록 system/date/instance 식별이 가능해야 한다.
- 비동기 File Writer는 bounded queue, backpressure/fallback, shutdown drain, disk-full/write-failure, process-kill, terminal-loss 탐지와 alert를 제공한다.
- 로그 저장 실패가 원 업무 Transaction을 불필요하게 Rollback시키지 않되, 법적/보안 감사처럼 fail-closed가 필요한 로그는 정책을 구분한다.
- local spool/replay를 사용하는 경우 순서, 중복 제거, checksum, retry, poison record/quarantine와 유실 탐지를 제공한다.

#### DB 거래·운영 로그

- transaction/segment/attempt/batch execution/remote call 상태를 조회할 수 있는 Canonical Schema와 Index를 제공한다.
- `transactionId` 단일 조건으로 대량 데이터에서도 효율적으로 전체 Timeline을 조회할 수 있어야 한다.
- append/duplicate/idempotency, retention/partition/archive/purge, DB 장애와 재전송, 부분 기록을 검증한다.
- Audit DB Log는 append-only/tamper-evident 요구를 별도로 만족한다.
- File Log와 DB Timeline이 동일 거래를 가리키되 민감 Payload를 중복 저장하지 않는다.

#### ADM 통합 거래 조회

ADM은 운영자가 **transactionId 하나로** 해당 거래의 전체 호출 계보를 조회할 수 있어야 한다.

최소 조회 범위:

- 최초 요청과 종료 결과
- Local/Remote 하위 Transaction Segment
- 외부 REST/전문/File/Gateway 호출과 attempt
- Message producer/consumer, retry, DLQ
- Batch/Center-Cut/Scheduler로 이어진 job/execution/step/partition/worker
- instance/was/agent/server identity
- 오류 code/failure stage/UNKNOWN/reconcile 결과
- 관련 File Log/Remote Log/Trace/Audit의 안전한 연결
- 시간순 Timeline, 계층 Tree, 검색/Paging/Detail
- 데이터 누락·지연·수집 불가 시 명시적 partial/stale 경고

원문 민감 로그 조회·다운로드는 별도 권한, 사유, 승인, masking, 감사와 만료 정책을 적용한다.

## 9. 데이터·SQL·Migration 정본

공식 지원 DB Vendor는 MariaDB, PostgreSQL, Oracle 3종이다. MySQL/MSSQL은 지원 선택값에서 제거한다.

권장 Schema:

```text
cpfDB, cmnDB, admDB, bzaDB, batDB, refDB
+ Generator Manifest가 선언한 Domain Schema
```

- 모든 DB Artifact는 `cpf-tools/db/vendor/<vendor>` Owner 경계에서 동일 구조로 관리한다.
- Canonical Schema/Metadata에서 Vendor-native install/seed/migration/rollback/runtime query를 생성·동기화한다.
- 특정 Vendor SQL의 복사·치환만으로 완료 처리하지 않는다.
- Index/FK가 없는 Column을 참조하면 DB 실행 전 생성 Gate에서 실패해야 한다.
- Flyway 적용 Migration은 불변이며 신규 변경은 새 Version으로 제공한다.
- Empty Install과 Upgrade 최종상태는 schema manifest로 동등해야 한다.
- 기존 Schema가 다르면 조용히 skip하지 않고 drift 또는 migration 문제로 실패한다.
- destructive rollback은 데이터 보존/backup/승인/대체 recovery가 명시되어야 한다.
- 업무/관리 SQL은 Java literal이 아닌 Owner Query ID와 Vendor Resource로 관리한다.
- DB 변경은 Generator domain-template, Generated Domain, checksum, install/upgrade/rollback까지 한 작업 단위로 검토한다.


### 9.1 Generator-first Fresh Database Lifecycle

모든 DB 변경은 다음 순서로 수행한다.

```text
Requirement/Data Model
→ Canonical Schema·Metadata·Runtime Query Contract
→ Generator·Golden Template
→ Oracle/PostgreSQL/MariaDB Vendor Source
→ Install·Migration·Rollback·Runtime Pack
→ Java Consumer·Test
→ Fresh Runtime Evidence
```

Vendor SQL이나 Historical Migration을 먼저 수동 수정해 정본을 역전시키면 안 된다.

Codex·QA의 DB 검증은 기존 사용자 DB를 재사용하지 않고, 각 Vendor별 전용 QA Database/Schema가 CPF Object 0건인 초기 상태임을 확인한 뒤 시작한다. 공식 Reset/Provision 경로가 없으면 수동 SQL로 우회하지 말고 그 경로를 Source Defect로 구현한다.

각 Vendor는 단독으로 다음 Lifecycle을 통과해야 한다.

- Fresh Provision·Install·Mandatory Metadata/Seed
- Generator로 만든 임의 Domain Bootstrap
- Upgrade·Runtime Query·Schema Drift
- Rollback·Reapply·Idempotent Reapply
- Different-hash Conflict·Partial Failure·Restart
- Optional Pack On/Off
- Cleanup 후 CPF Object 0건 또는 승인된 보존 상태
- exact-SHA Evidence


## 10. File·Attachment·Archive·전문

- create/extract/upload/download/transfer 전 경로를 bounded streaming으로 처리한다.
- 대용량 payload를 `byte[]`, `readAllBytes`, 전체 DOM/문자열로 적재하지 않는다.
- 임시 파일→fsync/checksum→atomic publish를 사용하고 실패 시 partial target을 제거한다.
- path alias, canonical path, symlink/hardlink/device/FIFO, zip slip, duplicate canonical entry, 압축률·entry/total budget을 통제한다.
- client cancellation, timeout, disk full, process kill과 restart cleanup을 검증한다.
- 고정길이 전문은 byte length·encoding·padding·group·version·field offset·masking·streaming을 지원한다.
- 기관별 Layout/Endpoint/Auth는 고객 Adapter가 소유한다.

## 11. Gateway·외부연계·Event

Gateway:

- Control Plane과 Data Plane을 분리한다.
- Route snapshot은 atomic refresh하고 stale/invalid snapshot을 fail-closed한다.
- trusted header allowlist와 proxy chain을 적용하고 내부 Header spoof를 차단한다.
- SSRF 방지를 위해 scheme/host/port/CIDR/service allowlist, URI canonicalization, redirect와 DNS 정책을 적용한다.
- one-shot/streaming body는 안전한 replay 조건이 없으면 retry하지 않는다.
- 실제 async/stream 종료와 client disconnect 시점에 ledger를 닫는다.
- connect/send/response/read failure를 분류한다.
- audit/ledger 저장 실패가 원 업무를 불필요하게 오염시키지 않도록 transaction 경계를 분리한다.

Kafka/Event:

- stable message ID, schema version, key/partition/order, TTL, size/depth, producer/environment binding을 제공한다.
- at-least-once + idempotent consumer를 기본으로 한다.
- ACK/transaction/consumer commit과 업무 side effect 경계를 명시한다.
- retry topic, DLT, poison isolation, replay approval와 audit를 제공한다.
- 다중 Manager/Worker에서 reply/correlation이 인스턴스 로컬 queue에 의존하지 않게 한다.
- rebalance, broker outage, duplicate, late reply, process kill과 response loss를 검증한다.


### 11.1 Messaging Provider·JMS·MQ·TCP 지원

CPF Event 계약은 특정 Broker Client에 종속되지 않는 Envelope·Idempotency·Ordering·Retry·DLQ·Unknown-result 계약을 `cpf-core` Public API/SPI로 제공한다. 실제 Provider Runtime은 Starter가 소유한다.

공식 구현 대상은 다음과 같다.

- Kafka: `cpf-starter-messaging-kafka`
- JMS 3.x 공통 Runtime: `cpf-starter-messaging-jms`
- IBM MQ Provider: `cpf-starter-messaging-ibm-mq` — JMS Starter를 기반으로 TLS, Queue Manager, Channel, CCDT/Endpoint, Connection Recovery와 운영 상태를 제공한다.
- RabbitMQ/AMQP Provider: `cpf-starter-messaging-rabbitmq`
- 영속 연결형 TCP 전문: `cpf-starter-integration-tcp`

`JMS`는 API/Runtime 추상화이고 `IBM MQ`는 Provider이므로 하나로 뭉개지 않는다. RabbitMQ는 AMQP Provider로 별도 Lifecycle을 가진다. 각 Provider는 같은 CPF Envelope와 오류 분류를 사용하되 ACK·Transaction·Ordering·Redelivery 의미 차이를 숨기지 않는다.

TCP Starter는 연결 수명주기, framing, encoding, heartbeat, reconnect, backoff, half-open 탐지, bounded queue, backpressure, request-response correlation, 전송 후 응답 유실, duplicate/reconciliation, TLS와 credential rotation을 제공해야 한다.

사용자 입력에서 확인된 `TPC` 표기는 별도 요구를 버리지 않기 위한 검색 Alias로 보존하고, 후속 확인 전까지 `EXS-TCP`에 연결한다.


## 12. Batch·Scheduler·Center-Cut·Agent

- Spring Batch가 Primary Engine이며 자체 Job/Step/Execution Repository를 중복 구현하지 않는다.
- CPF 승인·idempotency·fencing·unknown-result 원장과 Spring Batch JobInstance/JobExecution/StepExecution ID를 연결한다.
- idempotency key 재사용 시 canonical request hash와 scope가 다르면 conflict로 거부한다.
- fencing은 실행 행에 저장된 token이 아니라 최신 owner/lease epoch를 검증한다.
- reserve→start→bind 사이 response loss와 고아 상태를 reconciliation한다.
- STOPPED/RETRYABLE_FAILURE/FAILED/UNKNOWN_RESULT가 Spring Batch 상태와 운영 UI에서 정확히 일치해야 한다.
- ExecutionContext에 Secret, 전체 stdout/stderr, 대용량 payload를 저장하지 않는다.
- Remote Partition/Chunk/Step은 Kafka transport와 stable correlation, DLT, backpressure를 사용한다.
- Product Profile에서 Remote topology가 in-memory channel로 조용히 fallback하면 안 된다.
- Scheduler trigger claim과 Job start 사이를 outbox/state machine으로 복구 가능하게 한다.
- Center-Cut은 immutable parameter, item claim/lease/fencing, global rate, failed-only reprocess와 unknown reconciliation을 제공한다.
- Agent는 승인 Script/Artifact만 실행하고 process tree, output budget, timeout, drain, takeover, artifact trust를 제공한다.

## 13. ADM·BZA·Frontend·BFF

ADM은 플랫폼 운영 Control Plane이며 Owner DB를 직접 수정하지 않는다. 위험조치는 Owner Command API로 수행한다.

BZA는 고객 업무 관리와 업무 결재를 소유하며 플랫폼 Runtime을 직접 제어하지 않는다.

공통 Frontend 기준:

- ADM/BZA 독립 Vue 3 + TypeScript + Vite Application/Artifact
- feature folder, route registry, Pinia state, TanStack Query API boundary
- Orval exact-SHA generated client와 drift gate
- package.json/package-lock exact 일치와 clean `npm ci`
- Element Plus/TanStack Table/Zod를 실제 화면 Consumer에 적용
- raw `fetch`는 단일 승인 mutator/auth bootstrap 경계 외 금지
- search/paging/sort/detail/status/loading/empty/error/retry UX
- deep link, 403/404, session expiry, browser history
- responsive, keyboard, accessibility와 Chromium/Firefox/WebKit E2E
- 외부 Runtime CDN/font/script 의존 금지

BFF/Session 기준:

- Browser Local/Session Storage, URL, DOM, response body, console/log에 Access Token, Refresh Token, Session ID를 노출하지 않는다.
- 인증 응답 형태가 Map/DTO/record 중 무엇이든 credential stripping은 fail-closed한다.
- JDBC Session의 credential 저장은 최소화·암호화/참조화하고 DB dump/운영화면에 원문을 노출하지 않는다.
- Session fixation 보호, rotation, timeout, concurrency, 권한회수, 강제 logout을 제공한다.
- Spring Security 표준 CSRF와 route inventory 기반 보호를 사용하고 mutation 전체를 검증한다.
- Session Store readiness는 연결뿐 아니라 schema/index/create-read-delete를 검증한다.
- 제품 Profile에서 DB 오류를 Memory Session/Repository 성공으로 대체하지 않는다.

## 14. Security·Privacy·Audit

- 관리자 MFA, IP/Network policy, Session policy와 service mTLS/OIDC/OAuth/JWT/API key를 지원한다.
- credential/secret/certificate는 외부화하고 keyId 기반 trust, rotation, expiry, revocation을 제공한다.
- PII는 분류·최소수집·masking·raw 승인·retention/deletion을 제공한다.
- 위험조치는 requester/approver 분리, 자기승인 금지, ALL/ANY/N_OF_M, expiry와 immutable command hash를 제공한다.
- Break-glass는 별도 권한, TTL, 긴급사유, 사후 Review와 immutable audit가 필수다.
- Audit는 append-only/tamper-evident하고 before/after snapshot은 credential/PII를 redaction한다.
- XSS, CSRF, SSRF, injection, path traversal, deserialization, upload/archive bomb, unsafe process 실행을 negative corpus로 검증한다.
- Evidence와 로그도 제품 보안 경계이며 Secret/Token/Session/Private Key 원문을 저장하지 않는다.

## 15. 운영·Observability·Reliability

주요 실행 흐름은 system/domain/instance/transaction/segment/attempt/job/execution/item/agent 식별자를 연결한다.

필수:

- metrics, logs, traces, transaction timeline
- bounded cardinality와 masking
- SLI/SLO, error budget와 burn-rate
- alert dedup/group/routing/escalation
- incident, runbook, recovery action와 postmortem
- topology/service catalog
- maintenance/drain/quiesce
- runtime config version/approval/rollback
- desired/actual drift
- capacity trend/load limit
- backup/restore와 DR drill

운영 기능 자체의 장애가 원 업무를 불필요하게 오염시키지 않도록 보안 결정과 관측 기록의 transaction 경계를 분리한다.

## 16. Generator·Developer Experience·EDU

Generator 입력:

- DomainName
- 3자리 SystemCode
- Module/Package
- DB Vendor
- Capability

필수 lifecycle:

```text
create → optional DB bootstrap → build/test/runtime
→ CRUD/Search/Paging/Validation/Commit/Rollback
→ remove → regenerate → normalized parity
```

- Module/Package/SystemCode/Config/Route/Menu/SQL/DB 충돌을 사전 검증한다.
- 하나의 표준 Template을 사용하고 특정 Domain 예외 `if/switch`를 늘리지 않는다.
- 사용자 소유 영역을 덮어쓰지 않는다.
- Generator-owned 영역은 checksum과 deterministic output으로 관리한다.
- 중앙 `domain-template`만 DB 정본으로 사용한다.
- `cpf-member`와 임의 생성 Domain을 이름 normalize 후 parity 비교한다.
- Generated Domain은 CPF BOM + Convention Plugin + Versioned Maven Artifact를 사용하고 Source/JAR 수동 복사를 금지한다.
- EDU와 Sample은 실제 제품 Header/API/DB/Event/Batch/Security 계약을 사용하고 정상뿐 아니라 오류·복구·권한·운영을 교육한다.


### 16.1 EDU Architecture 판정 기준

EDU는 Product 완성도를 대신하는 우회 구현이 아니다.

- 제품 ADM/BZA/Gateway/Batch 자체의 CRUD·운영·승인·Incident·Topology·Log/Trace·Session 기능은 제품 Module에서 완성한다.
- EDU는 도입 개발자가 직접 작성해야 하는 Consumer/Extension/Integration 개발 예제에 집중한다.
- EDU ID별로 `교육 대상 사용자`, `공개 계약`, `실제 Consumer`, `왜 EDU가 필요한지`를 설명할 수 없으면 Architecture 재분류 대상으로 본다.
- 기존 `EDU-ADM-*`를 포함한 EDU 항목은 숫자를 유지하기 위해 Product 기능을 복제하지 않는다.
- QA는 각 항목을 `유지`, `통합`, `Product 귀속`, `공식 Extension Sample`, `삭제 후보`로 판정하고 영향도를 보고한다.


### 16.2 R6J EDU-ADM 중앙 Architecture 결정

R6J QA A/B 독립 검수 후 다음 원칙을 확정한다.

- `EDU-ADM-08`, `10`, `11`, `12`, `13`, `14`, `15`, `16`, `17`은 ADM Product 기능으로 귀속한다. ADM Product Source/API/Frontend/Test/Runtime/Manual에서 검증하고 generic REF EDU로 복제하지 않는다.
- `EDU-ADM-02`, `03`, `04`, `07`은 공식 Public Extension/Integration 계약을 사용하는 adopter-facing Sample로만 유지한다. 해당 Public 계약이 없으면 먼저 정식 Extension Point를 설계하거나 Product로 귀속한다.
- `EDU-ADM-01`, `05`, `06`, `09`는 독립 ADM EDU로 유지하지 않고 기존 Public Extension/Async/Recovery/Concurrency EDU와 통합한다.
- EDU 17개 또는 전체 135개라는 수량 자체를 완료 기준으로 사용하지 않는다.
- 전체 EDU Canonical Count는 다른 EDU의 Architecture/Consumer 적정성까지 검토하고 Merge/Product 귀속을 반영한 뒤 Catalog에서 재산정한다.
- 수량 보존을 위한 dummy handler, generic JDBC state-machine, Product mimic을 금지한다.
- 물리 Source 삭제는 Delete Manifest와 사용자 승인 절차를 따른다.

## 17. Build·Artifact·배포·Supply Chain

Artifact 공급 모드:

- `LOCAL_DEV`: 검증된 shared local Maven repository
- `REMOTE`: Nexus/Artifactory 등 승인 Registry
- `OFFLINE`: Manifest/Checksum이 있는 versioned offline Maven bundle

REMOTE/OFFLINE 실패 시 개발자 Local Repository로 fallback하지 않는다.

Build 필수:

- fresh clone, clean Gradle/npm cache
- settings/includeBuild/project path 전체 존재와 resolution
- Java 25 toolchain, Wrapper/BOM/Plugin/Lock 정합성
- Published POM/BOM/source/javadoc
- deterministic/reproducible artifact
- final JAR/WAR/static artifact dependency inclusion
- ADM/BZA package lock와 generated client
- unsupported stack fail-closed

Artifact/Deploy 필수:

- canonical manifest와 SHA-256
- environment/channel/service/version/release sequence binding
- keyId 기반 signature/trust/revocation
- local artifact state tamper protection
- install lock와 atomic activation
- health/service identity/build SHA 검증
- 실제 side effect가 발생한 instance만 selective rollback
- deployment request hash/idempotency와 unknown-result reconciliation
- power loss/process kill 후 이전 또는 새 version 중 하나로 복구

Supply-chain은 Source directory가 아니라 **각 최종 Release Artifact**를 검사한다.

- CycloneDX resolved graph
- ORT analyze + evaluate + report
- Syft final artifact SBOM
- Grype final artifact vulnerability
- Approved OSS lock와 PURL/name/version/hash 양방향 대조
- conditional license 승인과 THIRD_PARTY_NOTICES/source obligation
- 모든 도구의 source SHA, input artifact hash, config/tool binary hash 일치

## 18. 설치·Migration·Upgrade·Rollback·Compatibility

GA 지원 표기는 다음이 실제 실행됐을 때만 가능하다.

- Empty Install
- 최소권한 Service User Provision
- idempotent mandatory seed
- reinstall
- upgrade
- rollback 또는 forward recovery
- backup/restore
- mixed-version rolling compatibility
- JAR/WAR/static artifact
- 3 DB Vendor
- Local/Remote topology
- multi-instance
- signed deploy와 rollback
- API/DB/config/message/file/전문 compatibility

지원하지 않은 Docker/Kubernetes/Cloud/DB/Browser/OS는 문서 문자열만으로 지원 표기하지 않는다.

## 19. Repository·문서·Evidence 정본

Repository Root에는 제품 식별, Build, 실행에 필요한 최소 파일과 공식 Module만 둔다. Root 문서는 `README.md` 하나만 허용한다.

정본 역할:

- Final Target: 최상위 제품 목표와 Requirement Catalog
- Continuity Ledger: Requirement ID 영속성
- Architecture/ADR/Specification: 구조·계약·결정
- Guide: 개발·운영·설치·복구
- Current Request: 현재 작업
- Review/Handover: 독립 검수와 연속성
- Evidence: 직접 실행 근거
- Generated: 재생성 가능한 파생물
- Release: 실제 Release만

Evidence 최소 필드:

- exact source SHA와 clean tree
- 실행 명령
- profile/environment/topology
- tool/runtime version
- 시작·종료 시각
- requirement/scenario ID
- exit code와 실제 결과
- report/log/artifact SHA-256
- 민감정보 정제 여부
- 현재 Commit 유효성

파일 존재, 문자열 Marker 수, 정적 검색, Swagger 노출, 일부 Test, 과거 Commit Evidence, 작업자 보고는 단독 완료 근거가 아니다.

## 20. 최종 제품화 Gate

다음이 모두 최신 exact Commit과 재현 가능한 환경에서 확인돼야 GA 완료다.

1. 공식 Module/Package/SystemCode/DB Ownership과 dependency 방향
2. fresh clean settings evaluation, full build/test와 published artifact
3. Empty DB install, reinstall, upgrade, rollback/forward recovery, backup/restore
4. 주요 API와 Runtime E2E
5. Local/Remote parity와 mixed-version compatibility
6. Multi-instance, lease, fencing, rebalance, failover와 recovery
7. 실제 Kafka, 외부 failure, response loss와 unknown-result reconciliation
8. Spring Batch, Scheduler, Center-Cut, Agent/Runner/Worker
9. Gateway streaming/disconnect/retry/failover와 ledger
10. ADM/BZA Server Authorization, Production Build와 3 Browser E2E
11. Session/BFF, Security, Approval, Audit, Privacy와 Masking
12. Generator create→runtime→remove→regenerate lifecycle
13. Final Artifact signature, deploy, selective rollback와 supply-chain scan
14. EDU, OpenAPI, JavaDoc, 개발/운영/설치/복구 Guide
15. Requirement→Source/API/SQL/Test/Runtime/Evidence와 역방향 추적
16. Root Hygiene, No Legacy/Dual Primary/Dead Code/Stale Evidence/Secret

하나라도 `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`이면 전체 GA 완료가 아니다.

## 21. Requirement ID 연속성

- Requirement ID는 세션, PC, AI 계정, Architecture Rename과 무관한 영구 Key다.
- 통합은 `superseded-by`, 분해는 `split-into`, 폐기는 근거·영향·대체·승인을 Continuity Ledger에 기록한다.
- Owner 변경으로 ID 의미를 지우지 않는다.
- Legacy Alias와 Canonical ID를 완료율에 중복 집계하지 않는다.
- Canonical Count 감소는 Continuity Mapping으로 완전히 설명돼야 한다.
- 새 요구는 `REQ-GAP` 절차로 기존 ID와 중복을 먼저 검사한다.

현재 Canonical Requirement Count는 **186개**이며, 아래 Catalog가 각 ID의 최소 제품 의미와 완료 증명을 정의한다.

## 22. 상세 Requirement Catalog

### 22.1 Architecture/Core

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `ARCH-MISSION` | cpf-core / repository architecture | CPF를 샘플이나 공통 라이브러리가 아닌 금융권 포함 엔터프라이즈 업무시스템의 구축·운영·감사·확장·배포를 책임지는 상용 Business Platform Framework로 완성한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-MSA` | cpf-core / repository architecture | 동일 Public Contract로 Modular Monolith, 동일 JVM Local Call, 분리 WAS Remote Call, 독립 Microservice를 지원하며 topology 변경이 업무 계약을 바꾸지 않게 한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-BOUNDARY` | cpf-core / repository architecture | 기술 공통·고객 공통·플랫폼 운영·업무 관리·Batch·Gateway·Generated Domain의 Owner를 단일화하고 역방향·순환·DB 직접 접근을 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-LAYER` | cpf-core / repository architecture | Public API, 확장 SPI, Internal 구현을 Module·Package·Publication·JavaDoc·ArchUnit로 구분하고 외부 Consumer가 Internal Package를 참조하지 못하게 한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-API` | cpf-core / repository architecture | 고객 개발자가 최소 입력으로 안전하게 사용할 수 있는 발견 가능한 Public API를 제공하고 거대 Utils·의미 없는 Wrapper·선택 Runtime type 노출을 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-SPI` | cpf-core / repository architecture | 고객·Generated Domain·기관 Adapter가 구현할 안정된 SPI와 lifecycle, capability, version compatibility, failure contract를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-CONFIG` | cpf-core / repository architecture | safe default→customer property→profile→operation override→per-call override 순서와 허용범위·권한·버전·감사·rollback을 보장한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-TESTKIT` | cpf-core / repository architecture | Public Contract, Header, 오류, idempotency, Local/Remote parity, failure injection을 외부 Consumer가 재사용할 수 있는 Test Kit로 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-CALL` | cpf-core / repository architecture | 동일 JVM과 분리 WAS 호출에 동일한 Header·권한·timeout budget·오류·추적·idempotency를 적용하고 내부 호출의 Gateway 재경유를 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-REGISTRY` | cpf-core / repository architecture | Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ROUTING` | cpf-core / repository architecture | service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-HEALTH` | cpf-core / repository architecture | liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-HEADER` | cpf-core / repository architecture | 표준/확장 Header의 이름·형식·신뢰경계·생성자·전파·masking·최대크기·호환성을 정본화하고 spoofing을 차단한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-CONTEXT` | cpf-core / repository architecture | transaction, trace, segment, caller, principal, environment, channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-TXID` | cpf-core / repository architecture | 정식 거래 기동 Channel/System이 34자리 transactionId를 최초 생성할 수 있고 이후 Local/Remote/REST/SOAP/Gateway/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile/Log/ADM Timeline 전체가 같은 transactionId를 승계한다. 비신뢰 주체의 사칭·변조·replay는 인증된 Channel/System identity와 trust policy로 차단한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ROLE` | cpf-core / repository architecture | transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ERROR` | cpf-core / repository architecture | 표준 오류 코드·HTTP/Protocol mapping·retryability·unknown-result·field error·operator message를 버전 가능한 계약으로 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-VALID` | cpf-core / repository architecture | 입력·출력·설정·Header·파일·메시지·SQL parameter 검증과 오류 위치, allowlist, 크기·깊이·개수 상한을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-IDEMP` | cpf-core / repository architecture | canonical request hash, scope, TTL, result replay, conflict semantics와 concurrent race를 포함한 idempotency 원장을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-STATE` | cpf-core / repository architecture | 승인·비동기·배치·배포·복구 등 장기 거래의 허용 상태전이, 낙관적 잠금, terminal state, reconciliation을 명시한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOCK` | cpf-core / repository architecture | optimistic/distributed lock, lease, fencing token, owner epoch, expiry, takeover와 stale writer 차단을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-RESILIENCE` | cpf-core / repository architecture | timeout, retry, circuit breaker, bulkhead, rate/backpressure, retry storm 방지와 operation별 정책을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-DEADLINE` | cpf-core / repository architecture | 요청 전체 deadline budget을 하위 호출·DB·Broker·파일·process에 분배하고 초과 시 cancel·cleanup·unknown-result 규칙을 적용한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-SCHED` | cpf-core / repository architecture | 기술 Scheduler의 trigger, cluster claim, misfire, calendar, idempotency, pause/resume, 운영 제어 계약을 정의하고 Batch Scheduler와 Owner를 분리한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-OPSDB` | cpf-core / repository architecture | 운영 DB의 공유/분리 topology, schema ownership, 연결 장애 시 fail-open/fail-closed, backpressure, 복구와 readiness를 정의한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOGDB` | platform-operations/logging + persistence provider; core는 공통 transaction/error/context 의미만 | DB Log의 schema·index·retention·masking·비동기 적재·조회 성능·장애 격리와 ADM projection을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-FILELOG` | platform-operations/observability logging capability; core는 공통 context 의미만 | 환경·Domain·Instance·transactionId·execution 단위로 탐색 가능한 구조화 File Log, rotation, retention, secure permission과 수집 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOGFAIL` | platform-operations/observability logging capability | 로그 저장 실패가 업무를 오염시키지 않도록 정책별 fail-open/closed, local spool, 재전송, 중복 제거, 유실 탐지와 alert를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-TRACE` | core trace/context contract + platform-operations/observability provider | transactionId와 trace/span/segment/attempt를 연결하고 sampling, trace boost, baggage allowlist, cardinality·민감정보 통제를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-MASK` | core classification/redaction contract + security/masking capability | PII/Secret/Credential 분류, context-aware masking/redaction, raw 조회 승인, logging/evidence/download 정책과 테스트 corpus를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-FIXED` | integration/fixedlength-core contract + fixedlength starter/provider | 고정길이 전문 Layout/Field/Group/encoding/byte length/parser/writer/validator/version/streaming과 secure diagnostic engine을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-FILE` | file capability contracts + archive/attachment/transfer/object-storage providers | Path Alias, bounded streaming, checksum, atomic publish, symlink/path traversal 방지, cleanup, cancellation을 포함한 File/Attachment/Archive 기술 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-MESSAGE` | cpf-core / repository architecture | versioned broker envelope, correlation, idempotency key, schema, TTL, producer/environment binding, size limit와 serialization allowlist를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |

| `ARCH-STARTER` | product architecture + cpf-tools generator/build | `cpf-core`를 Spring Boot 없는 초경량 계약 Artifact로 유지하고 Leaf Starter·Capability Profile·Aggregate Starter·BOM의 역할, Provider 충돌, Consumer와 Footprint를 정본화한다. Starter는 단순 OSS Dependency Wrapper가 아니며 OSS 직접 적용보다 설정·API·오류처리·보안·감사·운영이 더 단순하고 안전해야 하고, 좋은 Default·Fail-Fast·세밀한 Override·Native API Escape Hatch를 제공한다. | non-Boot Core consumer, Starter removal compile, Profile resolution lock, Aggregate POM, BOM/publication, actual Consumer, startup/classpath/fault Evidence + OSS 직접 적용 대비 boilerplate/설정 감소와 misuse fail-fast 검증 |

### 22.37 Common/Data

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `CMN-EXTENSION` | cpf-common | 고객 Header·User Context·Validation·Error Mapping·Masking·Audit·Web Client 정책을 cpf-core SPI 위에서 확장하며 기술 Engine을 중복 소유하지 않는다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-SAMPLE-DB` | cpf-common | cmnDB의 단일 Golden Sample Table로 CRUD/Search/Paging/Validation/duplicate/optimistic lock/commit/rollback을 3 Vendor에서 검증한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-CODE` | cpf-common | 고객 공통 코드·참조데이터의 group/item/version/유효기간/cache/invalidation/조회·관리·audit 계약을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-MSG` | cpf-common | 다국어·오류·업무 메시지의 code, locale, parameter schema, fallback, cache, version과 관리 계약을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-CALENDAR` | cpf-common | 영업일·휴일·기관 calendar, 기준일 계산, DB-less fallback, override 승인과 Batch/업무 공통 소비 계약을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-TEMPLATE` | cpf-common | 알림·문서 Template의 version, variable schema, escaping, preview, channel extension, approval과 audit를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-OWNERSHIP` | cpf-tools DB + owning module | 모든 schema/table/view/index/FK/trigger/seed/query에 단일 Owner와 실제 Consumer를 부여하고 Admin/타 Domain의 직접 갱신을 금지한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-INSTALL` | cpf-tools DB + owning module | Schema/User provision, 최소권한, product table/index/constraint, mandatory seed, verify/smoke를 Vendor-native 정본으로 재현 가능하게 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-FRESH` | cpf-tools DB + owning module | Oracle·PostgreSQL·MariaDB 검증을 CPF Object 0건의 전용 초기 Database/Schema에서 시작하고 Canonical/Generator-first Fresh Install→Upgrade→Rollback→Reapply→Cleanup을 자동화한다. | Vendor별 pre-object-count 0, generated metadata/seed, runtime query, drift, rollback/reapply, different-hash, optional pack, post-cleanup exact-SHA Evidence |
| `DB-MIGRATION` | cpf-tools DB + owning module | 불변 version migration, expand-migrate-contract, checksum, drift fail-closed, restart, data transform와 신규설치 최종상태 parity를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-ROLLBACK` | cpf-tools DB + owning module | rollback/forward recovery 가능성 분류, 데이터 보존·backup checkpoint·승인·재적용·부분 실패 복구를 Vendor별로 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-BACKUP` | cpf-tools DB + owning module | schema/data/config/key metadata의 backup, encryption, retention, restore validation, PITR와 DR 연계 절차를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-MULTI-VENDOR` | cpf-tools DB + owning module | MariaDB·PostgreSQL·Oracle의 type/default/index/FK/paging/locking/error semantics와 install→upgrade→rollback→reapply 동등성을 보장한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-SQL` | cpf-tools DB + owning module | Query ID·Owner·parameter/result contract·sort/filter allowlist·Vendor SQL resource·MyBatis/JDBC 규칙과 Java literal SQL 금지를 적용한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-PERF` | cpf-tools DB + owning module | index/plan/statistics/partition/slow query/capacity/purge 성능 기준과 대표 데이터 규모의 regression gate를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-MULTI` | cpf-tools DB + owning module | multi datasource, read replica, read/write routing, transaction consistency, lag, failover/failback와 tenant/domain isolation을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DATA-LINEAGE` | cpf-tools DB + owning module | 입력 source→처리→저장→외부전달의 dataset/field lineage, quality rule, reconciliation, owner와 audit 연결을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DATA-RETENTION` | cpf-tools DB + owning module | retention, purge, archive, legal hold, 개인정보 삭제, backup 예외와 실행 증적을 데이터 유형별로 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |

### 22.57 Gateway/External/Event

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `GWY-ENTRY` | cpf-gateway | 외부 진입점의 TLS, listener, protocol, client identity, request limit, maintenance와 control/data plane 분리를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-ROUTING` | cpf-gateway | Spring Cloud Gateway 기반 route snapshot, service registry, path/query rewrite, load balancing, version/zone/weight routing과 atomic refresh를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-TRUST` | cpf-gateway | trusted proxy와 client header allowlist, internal header overwrite, forwarded chain, principal/context 생성과 SSRF target allowlist를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-RESILIENCE` | cpf-gateway | connect/send/response/read 단계별 timeout·retry·failover·circuit breaker·streaming completion·client disconnect·unknown-result ledger를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `API-LIMIT` | cpf-gateway + cpf-core contract | client/channel/API/tenant별 rate limit·quota·burst·abuse detection·distributed counter·429/Retry-After·운영 override를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-INST` | generated domain / customer adapter | 기관별 외부연계 Adapter를 Generated Domain/고객 확장 Owner로 생성·배포하며 중앙 cpf-external 고정 Module을 두지 않는다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-REST` | generated domain / customer adapter | 외부 REST 호출의 auth, timeout, retry, idempotency, schema, mapping, audit, mock/test contract를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-FIXED` | generated domain / customer adapter | 기관별 고정길이 Layout/Mapping/endpoint를 CORE-FIXED Engine 위에 versioned Adapter로 구현한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-SEC` | generated domain / customer adapter | 외부연계 mTLS/OAuth/API key/certificate/secret rotation, endpoint allowlist, payload masking과 non-repudiation을 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-FILE` | generated domain / customer adapter | SFTP/파일명/ack-nack/checksum/claim/transfer/reconciliation/retention을 고객 Adapter가 안전하게 소유한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-UNKNOWN` | generated domain / customer adapter | 외부 요청의 전송 전 실패·전송 후 응답 유실·상대 처리 불명 상태를 분류하고 자동 성공·무조건 재시도를 금지한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-RECON` | generated domain / customer adapter | 상대 조회·callback·file ack·수동 확인을 통한 reconciliation, compensation, reprocess, SLA와 운영 UI를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-CORE` | cpf-core contract + owning business adapter | Provider-neutral destination naming, versioned envelope, key/order, producer/consumer contract와 in-memory test adapter를 제공하며 Provider별 의미 차이를 명시한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-OUTBOX` | cpf-core contract + owning business adapter | 업무 데이터 변경과 Outbox INSERT를 동일 Local Transaction으로 묶고 stable event/message ID, claim/lease/fencing, retry, ordering, publish/confirm 상태, cleanup, broker ACK 유실·process kill 후 중복 발행과 UNKNOWN/Reconcile을 제공한다. Outbox는 일반 로그가 아니라 외부 전달이 완료될 때까지 생명주기를 관리하는 durable delivery state다. | 업무 DB+Outbox 동일 commit/rollback, 실제 Broker, publisher kill/restart, ACK loss, duplicate publish, multi-instance claim, timeout·unknown·reconcile Evidence |
| `EVENT-BROKER` | cpf-core contract + Starter Provider owner | Kafka/JMS/IBM MQ/AMQP의 ACK·transaction·redelivery·consumer concurrency·backpressure·schema·TTL·security·observability와 multi-instance correlation을 공통 계약으로 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-MQ` | cpf-core contract + Starter Provider owner | Queue 기반 Messaging의 destination, durable delivery, correlation, idempotency, expiry, priority, transaction, redelivery, DLQ와 운영 조회를 Provider-neutral 계약으로 제공한다. | Kafka/JMS/IBM MQ/RabbitMQ provider contract parity, actual broker, duplicate/ordering/outage/recovery/multi-instance Evidence |
| `EVENT-JMS` | cpf-starter-messaging-jms | Jakarta JMS 3.x ConnectionFactory, producer/consumer, transaction/session, selector, durable subscription, redelivery, exception listener와 readiness를 CPF Event 계약에 연결한다. Local JMS transaction과 XA-capable ConnectionFactory/XAResource 경로를 구분하며 XA/JTA 선택 시 CPF Transaction Strategy와 연결한다. | embedded/mock만이 아닌 실제 JMS provider matrix, local/XA transaction, redelivery/connection-loss/recovery Evidence |
| `EVENT-IBM-MQ` | cpf-starter-messaging-ibm-mq | JMS 공통 Starter 위의 Optional Provider로 IBM MQ Queue Manager·Channel·TLS·CCDT/endpoint·connection recovery·reason-code mapping·운영 상태를 제공한다. CPF 기본 Runtime에 IBM MQ 의존성을 강제하지 않으며 고객이 선택할 때만 로드되고 XA-capable JMS 구성이 필요한 경우 TX-XA-JTA 계약과 연결한다. | optional dependency/bean 0-footprint, IBM MQ compatible runtime, TLS/credential rotation, queue manager outage, reconnect, XA/in-doubt/duplicate/reconcile Evidence |
| `EVENT-AMQP` | cpf-starter-messaging-rabbitmq | RabbitMQ/AMQP exchange·queue·binding·publisher confirm·consumer ack/nack·redelivery·DLX·quorum/connection recovery를 CPF Event 계약에 연결한다. | actual RabbitMQ runtime, confirm/ack/nack/DLX, duplicate/order/outage/recovery Evidence |
| `EXS-TCP` | cpf-starter-integration-tcp + generated/customer adapter | 영속 TCP 연결의 framing·encoding·heartbeat·reconnect·backpressure·correlation·TLS·half-open·전송 후 결과 불명과 기관별 전문 Adapter 연결을 제공한다. | loopback/mock 및 실제 fault proxy, disconnect/half-open/timeout/response-loss/duplicate/reconcile/multi-instance Evidence |
| `EVENT-DLQ` | cpf-core contract + owning business adapter | retry topic, DLT, poison isolation, replay approval, payload masking, idempotent reprocess와 운영 추적을 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `SAGA-CORE` | cpf-core contract + owning business adapter | 장기 업무 흐름의 step, state, version, timeout, idempotency, event/call correlation과 durable orchestration/choreography 계약을 제공한다. 동일 transactionId lineage 아래 STARTED/RUNNING/COMPLETED/FAILED/COMPENSATING/COMPENSATED/UNKNOWN/MANUAL_REVIEW 상태를 구분하고 restart/multi-instance에서도 이어서 복구한다. | A→B→C(/D) 실제 Reference, 부분 성공, process kill, timeout, duplicate, compensation/retry/unknown/reconcile/multi-instance Evidence |
| `SAGA-COMP` | cpf-core contract + owning business adapter | 각 step의 compensation eligibility, reverse order, idempotency, partial compensation와 unknown-result 분리를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `SAGA-MANUAL` | cpf-core contract + owning business adapter | 자동 복구 불가 Saga의 승인된 수동 확정·보상·재실행·audit·operator guidance를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |

### 22.78 Core Transaction Strategy / Starter DX / AI

기존 `CPF-CALL`, `CPF-CONTEXT`, `CPF-TXID`, `EXS-*`, `EVENT-OUTBOX`, `EVENT-*`, `SAGA-*`, `ADM-TX`, `ADM-TIMELINE`, `SEC-*`를 대체하지 않는다. 아래 Requirement는 이 기능들을 하나의 상용 Transaction/Integration 모델로 연결하는 새 상위 Capability와 기존 정본에 없던 XA/JTA·TCC·AI·Developer Experience 공백만 추가한다.

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `TX-STRATEGY` | cpf-core contract + selected runtime owner | 업무 특성에 따라 `LOCAL`, `XA_JTA`, `OUTBOX`, `SAGA`, `TCC`를 명시적으로 선택·조합하는 정책, 안전한 Default, 상호배타/호환 규칙과 선택하지 않은 Runtime 0-footprint를 제공한다. 동일 거래에서 전략을 혼합해도 transactionId lineage와 오류/복구 모델이 유지되어야 한다. | strategy selection API/config, fail-fast conflict test, selected-only dependency/bean/config/SQL, 실제 Consumer와 혼합 전략 E2E Evidence |
| `TX-LOCAL` | cpf-core contract + data runtime owner | 단일 Resource의 local transaction을 가장 단순한 기본 개발경험으로 제공하고 commit/rollback, propagation, isolation, timeout, read-only, exception mapping, transactionId/log 연계를 표준화한다. XA Provider가 없어도 기본 업무가 정상 동작해야 한다. | 실제 Domain Consumer, commit/rollback/timeout/exception Test, 3 DB Vendor Runtime 또는 타당한 DB-less 근거 |
| `TX-XA-JTA` | cpf-core transaction contract + Optional JTA Provider owner | JTA/XA를 Optional 상용 Capability로 제공한다. Tomcat에서는 standalone Transaction Manager Adapter, JTA-capable WAS에서는 managed JTA Adapter를 지원하며 DB+DB, DB+JMS의 XAResource enlistment와 2PC prepare/commit/rollback/heuristic/in-doubt 상태를 제공한다. 특정 TM 구현을 `cpf-core`에 강제하지 않는다. | Tomcat-compatible standalone TM Reference, managed-JTA adapter contract, Oracle/PostgreSQL/MariaDB XADataSource, JMS XAConnectionFactory/XAResource, DB+DB·DB+JMS Consumer/Test/Runtime Evidence |
| `TX-XA-RECOVERY` | JTA Provider owner + operations | prepare 이후 process kill, TM/RM restart, commit 중 장애와 in-doubt transaction을 durable recovery log와 resource recovery scan으로 안전하게 해소하고 duplicate recovery·heuristic outcome을 구분한다. ADM에서 권한·사유·감사와 함께 조회/조치한다. | prepare-kill-restart, TM/RM restart, in-doubt scan, heuristic/manual review, multi-instance/fencing, ADM Timeline/Recovery Evidence |
| `TX-INBOX` | messaging reliability owner + business consumer | At-least-once 전달 환경에서 Inbox/Dedup을 공식 계약으로 제공하고 eventId/messageId, consumer identity, idempotency, concurrency, duplicate/partial processing, process kill/restart와 retention을 관리하여 업무 중복 Side Effect를 방지한다. | Outbox→Broker→Inbox 실제 Consumer, duplicate/redelivery/process-kill/multi-instance Test, dedup state/cleanup/reconcile Evidence |
| `TX-TCC` | cpf-core contract + owning business domain | Hold/Reservation형 업무를 위한 Optional `Try/Confirm/Cancel` 계약을 제공한다. Try/Confirm/Cancel idempotency, empty rollback, hanging, duplicate confirm/cancel, timeout, UNKNOWN, recovery를 지원하되 Framework가 업무 보상 의미를 임의 결정하지 않는다. | 잔액/한도/재고 등 Reference Consumer, Try→Confirm/Cancel, duplicate/empty rollback/hanging/process-kill/reconcile Evidence |
| `TX-E2E` | cpf-core + all integration/runtime owners | Domain Call, 외부 REST/SOAP/TCP/File, DB, JMS/Kafka/RabbitMQ, Batch, Outbox/Inbox, Saga/TCC/XA, Retry/UNKNOWN/Reconcile, Logging/Audit/Trace/ADM을 하나의 transaction lineage로 연결한다. 기능별 단독 PASS로 E2E 완료를 대신하지 않는다. | 동일 Reference Transaction의 Source→Consumer→Call Path→failure/recovery→Log/ADM Timeline, local/remote/multi-instance/process-kill Evidence |
| `TX-DX` | cpf-core + Starter owners | 업무 개발자가 transactionId/log/audit/metrics/retry/idempotency/recovery를 매번 수동 조립하지 않도록 typed API, 안전 Default, 최소 Config, Fail-Fast와 세밀한 Override를 제공하고 고급 사용자는 underlying transaction/provider native 기능에 접근할 수 있게 한다. | 실제 업무 Consumer 코드 비교, boilerplate 감소, configuration misuse negative test, native escape/conformance Evidence |
| `TX-EDU` | cpf-reference + generator | Local, XA DB+DB, XA DB+JMS, XA crash recovery, Outbox/Inbox, Saga compensation, TCC, 외부 timeout/UNKNOWN/Reconcile, Domain A→B→C, Batch 연계를 실행 가능한 Reference로 제공하고 동일 transactionId와 ADM Timeline을 검증한다. | executable EDU/Reference, Source+Test+Harness, 3 Vendor/actual broker where applicable, failure/process-kill/restart Evidence |
| `STARTER-DX` | all Starter owners + generator/build | Canonical Starter Catalog의 모든 활성 Starter가 OSS 직접 사용보다 편하고 안전한 개발경험을 제공해야 한다. 편의 API, AutoConfiguration, 최소 설정, safe default, Fail-Fast, CPF Error/Security/Audit/Masking/Observability/Transaction 연계, Provider 확장성, Native API Escape Hatch, 미사용 0-footprint와 실제 EDU Consumer를 갖춘다. Wrapper-only/consumer-less Starter는 완료가 아니다. | 활성 Starter 전수 DX matrix, OSS-direct 대비 사용 코드/Config 비교, actual Consumer, boot context, failure/timeout/retry/unknown, selected-only footprint, EDU Evidence |
| `AI-OPTIONAL` | Optional AI Starter/Capability owner | 특정 AI Provider에 종속되지 않는 Optional AI API/SPI를 제공하고 model/provider routing, timeout/retry/circuit breaker/fallback, sensitive data masking, token/usage/cost metering, audit/observability, transactionId, authorization/policy, 위험 작업 approval, provider failure/UNKNOWN을 제공한다. 자체 LLM·Vector DB·거대 Agent Framework를 제품 기본범위로 만들지 않는다. | 최소 2 Provider 또는 1 Provider+customer plugin conformance, sensitive-data negative test, timeout/fallback/unknown, usage/cost/audit, approval, actual Consumer/EDU Evidence |


### 22.79 Batch/Center-Cut

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `BAT-CORE` | cpf-batch | Spring Batch를 Job/Step/Repository/ExecutionContext/Restart의 단일 Primary Engine으로 사용하고 자체 중복 실행 Engine을 제거한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-JOB` | cpf-batch | immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-ITEM` | cpf-batch | reader/processor/writer, chunk/skip/retry/checkpoint, item idempotency, partition, restart와 대용량 memory bound를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-EXECUTOR` | cpf-batch | Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-AGENT` | cpf-batch | Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-CALL-SYNC` | cpf-batch | Batch/Worker의 업무 Domain 동기 호출에 Local/Remote parity, Header, deadline, idempotency, retry/unknown-result를 적용한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-CALL-ASYNC` | cpf-batch | Batch/Worker의 Event/Outbox 비동기 호출에 stable message ID, retry/DLT, consumer idempotency와 completion correlation을 적용한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `BAT-SHARED` | cpf-batch | Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-CORE` | cpf-batch | Center-Cut의 job/item/attempt/aggregate 상태모델, immutable policy와 Spring Batch/업무 transaction 경계를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-RUNNER` | cpf-batch | CenterCutRunner를 Agent 내장 또는 독립 Process로 배포하고 target generation→claim→dispatch→aggregate lifecycle을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-PARAM` | cpf-batch | 대량 작업 parameter snapshot, schema, canonical hash, encryption/masking, version과 replay 재현성을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-CLAIM` | cpf-batch | item claim, lease, fencing, chunk assignment, stale worker 차단, duplicate prevention과 restart를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-RATE` | cpf-batch | global/domain/target TPS·RPS, concurrency, backpressure, adaptive throttle, pause/drain과 multi-instance 일관성을 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-REPROCESS` | cpf-batch | failed-only·selected·range 재처리, approval, idempotency, prior result 보존, compensation와 결과 비교를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-UNKNOWN` | cpf-batch | item/attempt 결과 불명을 분류·대사하고 확인 전 무조건 재처리를 금지하며 수동 확정과 audit를 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |
| `CENTER-OPS` | cpf-batch | ADM에서 job/item/attempt/timeline/progress/error/reprocess/pause/cancel/drain을 권한·사유·승인과 함께 제공한다. | Spring Batch Repository/Execution ID, 2개 이상 Instance, Kafka/DB, process kill·restart·reconcile Evidence |

### 22.98 Admin/Security/Operations

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `ADM-AUTH` | cpf-admin | 운영자 identity, password/MFA/OIDC, JDBC Session, session fixation·concurrency·revocation·force logout과 fail-closed product profile을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-RBAC` | cpf-admin | menu/button/API/command 권한, role mapping, 유효기간, organization context, server-side enforcement와 cache invalidation을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-AUDIT` | cpf-admin | 운영자·대상·before/after masked snapshot·reason·approval·result·transactionId의 immutable/tamper-evident audit를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-TX` | cpf-admin | 온라인/비동기/Batch/외부연계 transaction 검색, 표준 Header, payload masking, segment/attempt linkage와 상세 조회를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-TIMELINE` | cpf-admin | transactionId 기준 Local/Remote/Event/Batch/Gateway/File/Agent timeline을 순서·시각·instance·failure stage로 재구성한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-SERVICE` | cpf-admin | service/endpoint/instance/health/version/zone/routing/maintenance/draining을 조회·제어하되 Owner Command API를 사용한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-LOG` | cpf-admin | File/DB Log 조회, saved search, trace boost, dynamic log level, retention, download guard와 권한·audit를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-BATCH` | cpf-admin | Job definition/execution/step/checkpoint/restart/stop/recover와 승인·사유·Spring Batch ID 연계를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-CENTER` | cpf-admin | Center-Cut job/item/attempt/progress/reprocess/unknown/compensation 운영 기능을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-AGENT` | cpf-admin | Agent/Runner/Worker registry, capability, heartbeat, artifact, process, drain/takeover와 위험조치 승인을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-EXS` | cpf-admin | 외부기관 endpoint, health, credential/certificate status, request/response timeline, unknown/reconciliation을 기술 Owner API로 관제한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-RECOVERY` | cpf-admin | unknown-result, DLQ, Saga, deployment, file/batch 실패의 runbook, 승인된 recover/compensate/reconcile와 결과 추적을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-INCIDENT` | cpf-admin | alert→incident→severity/owner→runbook/action→postmortem/closure 흐름과 관련 transaction/evidence를 연결한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-UX` | cpf-admin | 대량 검색·paging·sort·filter·saved condition·status·empty/error/loading·responsive·keyboard·accessibility·safe download UX를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-APPROVAL` | cpf-admin | 플랫폼 위험조치의 versioned policy, ALL/ANY/N_OF_M, SoD, expiry, break-glass, immutable command hash와 owner-command execution을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-BUSINESS` | cpf-biz-admin | 고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-ORG` | cpf-biz-admin | 조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-APPROVAL` | cpf-biz-admin | 순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `BZA-SEQUENCE-SAMPLE` | cpf-biz-admin | 업무 채번을 선택형 Customization Sample로 제공하되 기본 Runtime 의존을 만들지 않고 규칙·시험·승인·audit를 교육한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `SEC-AUTHN` | cpf-core security contract + product owner | 사용자·운영자·service의 MFA/OIDC/OAuth2/JWT/API key/mTLS 인증, credential lifecycle, session/token replay 방어를 제공한다. Resource Server뿐 아니라 OIDC/OAuth2 Login 기반 SSO를 Keycloak·Microsoft Entra ID·Okta 등 외부 IdP와 연동하고 user/tenant/role/group/scope/claim을 CPF Security Context로 안전하게 매핑하며 login/logout/session/token 만료·갱신과 Frontend/BFF 연결을 제공한다. SAML2는 필요 시 Optional 확장으로 둔다. | 보안 Negative Corpus, credential/PII leak scan, issuer/audience/expiry/claim mapping, login/logout/session/refresh, IdP failure, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-AUTHZ` | cpf-core security contract + product owner | RBAC/ABAC, least privilege, server-side resource/action authorization, SoD, permission version과 즉시 회수를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-SECRET` | cpf-core security contract + product owner | Secret Provider SPI, 외부 Vault/file/env 및 KMS/HSM Provider integration, key version/rotation/revocation/provider health/failure-timeout, 필요 시 PKCS#11 연계, zeroization와 log/config/ADM/evidence의 key·secret 원문 금지를 제공한다. Local/JCE와 외부 KMS/HSM은 동일 계약을 따르되 Provider 고유 기능을 불필요하게 가두지 않는다. | 보안 Negative Corpus, credential/PII leak scan, KMS/HSM/provider failover·health, key version/rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-CERT` | cpf-core security contract + product owner | certificate/key trust store, issuance/import, expiry alert, rotation, revocation, mTLS identity와 keyId 기반 검증을 제공한다. 기존 Crypto/Secret을 재사용하여 범용 digital signature의 sign/verify, algorithm, keyId/keyVersion, certificate, signature metadata와 audit를 제공하고 Private Key 원문 노출을 금지한다. | 보안 Negative Corpus, credential/PII leak scan, sign/verify negative corpus, key/certificate rotation·revocation, 권한·audit와 침해경계 Evidence |
| `SEC-PRIVACY` | cpf-core security contract + product owner | PII catalog, 목적·최소수집·동의/법적근거, masking, raw access, retention/deletion, export와 audit를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-DOWNLOAD` | cpf-core security contract + product owner | 대량/민감 download의 권한·사유·승인·watermark·row/size limit·expiry·encryption·one-time link와 audit를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-APP` | cpf-core security contract + product owner | injection, SSRF, path traversal, upload/archive bomb, XSS/CSRF, deserialization, process execution, security header와 secure default를 통제한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-APPROVAL` | cpf-core security contract + product owner | 보안 위험행위의 dual control, 자기승인 금지, immutable target hash, expiry, break-glass, 사후 review를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-AUDIT` | cpf-core security contract + product owner | audit append-only/tamper detection, canonical payload, previousHash/currentHash chain, 선택적 digital signature, record 수정·삭제 탐지, concurrency/multi-instance 일관성, clock/identity, retention, search, export와 evidence integrity를 제공한다. Masking 후 canonicalization과 검증 순서를 명확히 하고 Audit 자체가 Secret/PII 원문 저장소가 되지 않게 한다. | hash-chain mutation/delete/reorder/concurrency/multi-instance 검증, signature verification, 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `OPS-METRIC` | cpf-admin control plane + runtime owner | transaction/service/instance/DB/Broker/Batch/Gateway/Agent의 bounded-cardinality metric과 dashboard/export를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-SLO` | cpf-admin control plane + runtime owner | availability, latency, error, freshness, backlog, recovery SLI/SLO와 error budget, burn-rate alert를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-ALERT` | cpf-admin control plane + runtime owner | dedup, grouping, inhibition, severity, routing, escalation, maintenance suppression와 acknowledgement를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-INCIDENT` | cpf-admin control plane + runtime owner | incident lifecycle, commander/owner, communication, timeline, evidence, action item와 problem linkage를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-RUNBOOK` | cpf-admin control plane + runtime owner | 탐지조건·영향·진단·안전조치·rollback·escalation·검증·종결 기준을 실행 가능한 runbook으로 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-SELF` | cpf-admin control plane + runtime owner | 자동진단/자동복구의 allowlist, rate/attempt limit, circuit stop, approval boundary, rollback와 immutable audit를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-TOPOLOGY` | cpf-admin control plane + runtime owner | service/instance/dependency/domain/owner/database/broker/endpoint 관계를 versioned topology/service catalog로 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-MAINT` | cpf-admin control plane + runtime owner | maintenance, admission block, drain/quiesce, in-flight deadline, health/routing 반영, resume와 audit를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-CONFIG` | cpf-admin control plane + runtime owner | runtime config catalog, schema, encryption, version, staged rollout, approval, dynamic apply, rollback와 drift detection을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-DRIFT` | cpf-admin control plane + runtime owner | Source/Artifact/Config/DB/Route/Permission/Runtime version의 desired-actual drift를 탐지·차단·복구한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-CAPACITY` | cpf-admin control plane + runtime owner | CPU/memory/thread/connection/queue/storage/DB/Broker 용량과 threshold, trend, forecast, load test 기준을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-DR` | cpf-admin control plane + runtime owner | RTO/RPO, multi-zone/site, backup/restore, failover/failback, data consistency, runbook와 정기 DR drill을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |

### 22.141 Generator/EDU/API/Quality/Productization

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `DEVEX-QUICK` | cpf-tools + public artifacts | 신규 개발자가 표준 환경에서 설치→생성→빌드→실행→테스트→디버깅을 문서대로 재현할 수 있는 quick start를 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-CODEGEN` | cpf-tools + public artifacts | OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-COMMENT` | cpf-tools + public artifacts | Public API/SPI, 주요 Service/Controller, 복구·동시성·보안 로직에 유지보수 가능한 한글 JavaDoc/주석과 설정 설명을 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `ONBOARD-DOMAIN` | cpf-tools + public artifacts | DomainName+SystemCode로 신규 업무 Domain을 충돌 검증 후 생성·DB bootstrap·build/runtime·remove/regenerate할 수 있게 한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-ACC` | cpf-reference / generated reference | 범용 계정/업무 흐름을 과도한 제품 원장 없이 Local/Remote·validation·transaction·error 사용법을 보여주는 선택형 Sample로 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-MBR` | cpf-reference / generated reference | cpf-member를 Generator output과 동일한 Golden Reference Instance로 유지하고 normalize parity gate를 통과시킨다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-REF` | cpf-reference / generated reference | cpf-reference에서 제품 Public API의 정상·오류·경계·복구·권한·운영 사용법을 실제 Runtime으로 교육한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-BIZADM` | cpf-reference / generated reference | BZA 선택형 업무관리/채번/결재 Customization Sample을 기본 활성화 없이 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-EDU` | cpf-reference / generated reference | 교육 시나리오가 장난감 계약을 만들지 않고 실제 Header/API/DB/Event/Batch/Security 표준을 사용한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `API-CONTRACT` | cpf-core API contract + endpoint owner | HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-PAGING` | cpf-core API contract + endpoint owner | offset page, slice, keyset/signed cursor, sort/filter allowlist, stable ordering, max size와 count 비용 정책을 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-ASYNC` | cpf-core API contract + endpoint owner | 202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-FILE` | cpf-core API contract + endpoint owner | multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `RULE-ARCH` | cpf-tools quality gates | Module/package/dependency/owner/internal API/DB access/dual primary/generated drift 위반을 자동 Architecture Gate로 차단한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `RULE-SEC` | cpf-tools quality gates | secret/credential/URL/TLS/security header/unsafe API/path/query/log/evidence pattern과 dependency vulnerability를 자동 Gate로 차단한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `RULE-QUALITY` | cpf-tools quality gates | compile/static analysis/duplication/dead code/dependency lock/license/SBOM/test coverage/marker-only 구현을 자동 Gate로 검증한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `TEST-UNIT` | repository-wide test ownership | 순수 로직·validation·state transition·error mapping·serialization·security utility를 deterministic unit test로 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-CONTRACT` | repository-wide test ownership | Public API/SPI, Local/Remote, OpenAPI, message schema, DB query, generated client와 published artifact compatibility를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-RUNTIME` | repository-wide test ownership | 실제 Java25/WAS/DB/Process 환경에서 startup, endpoint, transaction, shutdown, recovery와 resource leak를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-BROWSER` | repository-wide test ownership | ADM/BZA의 Chromium/Firefox/WebKit, route/deep link/session/permission/error/a11y/keyboard/responsive를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-BROKER` | repository-wide test ownership | 실제 Kafka·JMS/IBM MQ·RabbitMQ 지원 Matrix에서 ACK, transaction, duplicate, ordering, redelivery/rebalance, retry/DLQ, broker outage와 consumer crash를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-FAULT` | repository-wide test ownership | DB/network/broker/disk/process/time/response loss를 side-effect 전후에 주입해 idempotency·unknown·recovery·compensation을 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-EVIDENCE` | repository-wide test ownership | 모든 검증의 exact SHA, command, environment, time, exit code, report/artifact hash, sanitized 여부를 schema로 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `REL-BUILD` | cpf-tools release/deploy | fresh clone과 clean cache에서 Java25/Gradle build, LOCAL_DEV/REMOTE/OFFLINE resolution, lock/POM/BOM/source/javadoc/reproducible artifact를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-DEPLOY` | cpf-tools release/deploy | signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-MIG` | cpf-tools release/deploy | 제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-COMPAT` | cpf-tools release/deploy | semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `DOC-GOV` | cpf-docs + source owner | Final Target, ADR, Requirement Continuity, Current Request, Review, Handover의 역할·정본·폐기·변경 승인 규칙을 제공한다. | Source/API/SQL/Test와 문서의 링크·예제·명령을 현재 exact SHA에서 검증한 Evidence |
| `DOC-PRODUCT` | cpf-docs + source owner | 개발자·운영자·ADM/BZA·Gateway·Batch·설치·Migration·복구 Guide, OpenAPI, JavaDoc가 실제 Source와 일치하게 한다. | Source/API/SQL/Test와 문서의 링크·예제·명령을 현재 exact SHA에서 검증한 Evidence |
| `PROD-EDITION` | product governance | Edition/License/Capability packaging을 기술 Runtime과 분리하고 미결정 정책을 GA 완료처럼 노출하지 않는다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-MULTITENANT` | product governance | tenant context, data/config/permission/secret/quota/audit isolation과 tenant lifecycle을 선택 기능으로 설계한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-PLUGIN` | product governance | 고객/기관 Plugin·Adapter의 SPI, package, signature, compatibility, isolation, lifecycle, permission와 marketplace 후보 정책을 제공한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-PACKAGE` | product governance | 산업군·금융권별 capability package의 dependency, install/upgrade, config, license, compatibility와 support boundary를 제공한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `REQ-GOV` | requirement governance | Requirement ID, owner, priority, acceptance, status, continuity, traceability와 변경 승인 규칙을 영속 정본으로 관리한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-REVIEW` | requirement governance | 각 작업 전후 Requirement→구현과 구현→Requirement를 독립 검수하고 완료 보고와 실제 Git 차이를 기록한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-CODEX` | requirement governance | Codex/AI 요청서가 이전 대화 없이 실행 가능하도록 baseline, scope, architecture, acceptance, evidence, 금지조건과 output을 포함한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-GAP` | requirement governance | 새 상용 필수 요구를 기존 ID와 중복 검사해 intake하고 split/supersede/deprecate 관계와 count 변화를 기록한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |

## 22.42 Core Slimming / Modern Starter Portfolio Currentization

본 절은 기존 `ARCH-STARTER`, `CPF-TXID`, `CPF-HEALTH`, `CPF-LOCK`, `CORE-TESTKIT`,
`SEC-AUTHN`, `SEC-SECRET`, `SEC-CERT`, `SEC-AUDIT`, `EVENT-*`, `AI-OPTIONAL`,
`DOC-GOV`, `REQ-REVIEW`를 대체하지 않는다. 기존 Requirement의 의미를 유지하면서
2026-08-08 `07_16` Currentization 이후 제품 구조에서 새로 명시가 필요한 Capability와 Core Ownership 해석만 보강하고,
기존 Requirement에는 아래 강제 해석을 적용한다.

### 기존 Requirement 강제 해석

- `ARCH-STARTER`: `cpf-core -> cpf-starters/*` 의존은 0이어야 한다. Core는 CPF 전역 Kernel에 필요한 topology-independent Contract/Semantics/Value와 최소 순수 Logic만 소유한다. **Provider-neutral이라는 사실만으로 Core 소유를 허용하지 않는다.** Core Class는 (1) 대부분의 CPF Capability에 공통으로 필요하고, (2) Admin/Batch/Gateway/File/AI 등 특정 Owner 전용이 아니며, (3) Optional Capability를 사용하지 않아도 필요하고, (4) Runtime/Topology/Provider와 독립적이며, (5) 기술 교체 후에도 의미가 유지되고, (6) CPF 자체 Contract/Semantics/Value라는 조건을 충족해야 한다. 조건을 충족하지 못하는 API/SPI/DTO/Port는 해당 Capability/Owner Module이 소유한다. Spring AutoConfiguration, Servlet/Web Runtime, Logging Runtime, Dynamic Log Level/Remote Log 운영, OTel Adapter, Actuator Runtime, JDBC/JPA/MyBatis 구현, 특정 Provider와 일반 개발 편의 Utility를 Core에 적치하지 않는다. `compileOnly`도 Ownership 면죄부가 아니다.
- `CPF-TXID`: transactionId 의미·Context·Generator Contract는 Core에 둘 수 있으나 UUID/ULID/sequence 등 실제 기본 생성 구현, Spring wiring, Servlet/Message/Channel Adapter는 Foundation/Capability/Starter가 소유한다. 최초 신뢰 Entry에서 생성된 동일 transactionId는 Retry/Hop에서도 바꾸지 않는다.
- `CPF-HEALTH`: Core는 Health 의미·Port만 소유하고 Actuator, `HealthIndicator`, `HealthContributor`, Probe, Dependency Check와 Instance Runtime 구현은 Platform Operations Health Capability가 소유한다. Liveness/Readiness/Startup/Drain/DEGRADED/UNKNOWN과 Multi-instance ADM projection을 제공한다.
- `CPF-LOCK`: JDBC/Valkey 등 Provider 구현은 Core 밖에 둔다. 분산 Lock은 fencing token, lease, owner identity, stale-writer 차단, process kill/network partition/multi-instance recovery를 포함한다.
- `CORE-TESTKIT`: Runtime 제품 Module이 아니라 공식 Test Support로 제공하며 deterministic clock/id, transaction/security/tenant fixture, DB/Messaging/Batch/Health/Object Storage/GraphQL fixture, failure injection, multi-instance/process-kill harness를 지원한다.
- `DOC-GOV`: 개발·QA·Codex가 세션마다 `*_REV*`, `*_SESSION*`, 날짜별 `*_FINAL*`, Checkpoint, 중복 결과서·Matrix를 만들지 않는다. 동일 목적은 기존 Canonical/Current 파일을 직접 현행화하고 Git history가 과거 상태를 보존한다.
- `REQ-REVIEW`: QA A와 QA B는 같은 전체 Scope를 각각 100% 독립 전수검수한다. 한쪽 PASS/Evidence 승계, 대표 ID·샘플링 일괄 PASS, Source 직접 확인 없는 Deep Review를 금지하며 A/B 판정을 Requirement ID 단위로 Cross Validation한다.
- `ARCH-BOUNDARY/ARCH-LAYER`: 특정 Owner 전용 Contract는 그 Owner가 소유한다. `admin`, `batch`, `centercut`, `gateway` 등 전용 Command/Query/Operations/DTO/Status/Port를 단지 interface라는 이유로 `cpf-core`에 유지하지 않는다. 해당 Owner가 Core의 범용 Error/Transaction/Context/Security 계약을 소비하는 방향만 허용한다.
- `CPF-LOGDB/CPF-FILELOG/CPF-LOGFAIL/CPF-TRACE/CPF-MASK`: Core는 transaction/trace/context, 민감정보 분류·redaction 의미와 필요한 최소 contract만 보유할 수 있다. Structured/File/Async Logging, Logback/SLF4J 연계, Log Policy Runtime, Dynamic Log Level, Recovery Spool, Remote Log Artifact/Search/Bundle/Download/Node 운영은 Platform Operations/Observability 또는 Security/Masking Capability가 소유한다.
- `CORE-FIXED/CORE-FILE/AI-OPTIONAL`: FixedLength/File/AI처럼 선택 Capability의 Contract/API/SPI는 해당 capability owner가 소유한다. `fixedlength-core`, file capability contract, AI capability가 Core의 범용 contract를 소비하며, 선택 기능을 사용하지 않는 Application의 `cpf-core`에 해당 전용 API가 따라오지 않게 한다.

### 신규 Canonical Requirement

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `FOUNDATION-UTILITY` | pure foundation + foundation convenience starter | Core를 Utility 창고로 사용하지 않는다. `CpfClock/Dates/Decimals/Ids/Json/Lists/Maps/Numbers/Strings/Times/Validation/Values/Files/Hashes/Headers/Pages/Attributes` 등 현재 Core Utility를 전수 분류하여 JDK/Spring 단순 Wrapper는 제거 후보로 전환하고, CPF 고유 정책 가치가 있는 순수 기능만 topology-independent Foundation으로 이동한다. Header/Crypto/File/Paging/TransactionId처럼 Owner가 분명한 기능은 해당 Capability로 이동한다. 업무 개발자는 Application Convenience Starter/Profile을 통해 쉽게 사용하되 Core는 Starter를 참조하지 않는다. | Core Utility class-by-class ownership matrix, Core→Starter 0, simple-wrapper 0, actual consumer, deterministic test, native JDK/OSS escape, relocation duplicate 0 |
| `SEC-SESSION-DIST` | security/session provider | 기존 JDBC Session을 유지하면서 Multi-instance용 Valkey Distributed Session Provider를 Optional로 제공한다. expiration/renewal/rotation, fixation 방어, concurrent-session control, forced logout/logout propagation, user·tenant index, audit/metrics, provider failure와 0-footprint를 제공한다. | JDBC/Valkey provider parity, 2+ instance login/logout/revoke, provider outage/expiry/rotation test, security negative corpus, optional removal boot evidence |
| `FILE-OBJECT-STORAGE` | file/attachment + object-storage provider | Attachment/Archive/SFTP와 중복 Public API를 만들지 않고 S3-compatible Object Storage를 Provider-neutral하게 제공한다. streaming, multipart, checksum, range, metadata, presigned access, encryption/KMS, tenant isolation, timeout/retry, partial failure, orphan reconcile, retention/lifecycle와 malware-scan hook을 지원한다. | Attachment/Object Storage ownership trace, AWS S3 또는 MinIO reference provider, stream/multipart/failure/reconcile test, security/audit, 0-footprint, actual consumer |
| `EVENT-SCHEMA` | messaging contract governance + generator | Kafka/RabbitMQ/JMS/IBM MQ의 Broker 선택과 독립적인 Event Contract Governance를 제공한다. JSON Schema/Avro/Protobuf version, backward/forward compatibility, breaking-change gate, producer/consumer validation, generated model, schema id/content type와 provider-neutral registry boundary를 제공한다. | compatibility corpus, producer/consumer contract test, breaking-change CI gate, generated model, broker-independent reference, EDU |
| `API-GRAPHQL` | optional web/graphql starter + application service owner | REST/OpenAPI를 기본 API로 유지하면서 Browser/Mobile BFF와 복합 Domain Query를 위한 Optional GraphQL을 제공한다. Resolver는 Service/Application Layer를 재사용하고 Query/Mutation, 필요 시 Subscription, CPF Error/Paging/Cursor/Sort/Search, authN/authZ/field auth/tenant/transactionId/audit/trace, depth/complexity/size/rate-limit, N+1/DataLoader, introspection/GraphiQL prod policy와 Native Spring GraphQL escape를 제공한다. | real BFF consumer, schema/contract test, field-auth negative test, N+1 guard, query limit test, REST service reuse, 0-footprint |
| `API-REALTIME` | web/operations capability | Batch progress, Transaction Timeline, Runtime/Health State와 long-running operation을 위해 Server→Browser 단방향은 SSE를 우선하고 실제 양방향 요구에만 WebSocket을 사용한다. authN/authZ, reconnect/heartbeat, duplicate, slow-consumer/backpressure, rate limit, multi-instance fan-out, graceful shutdown, fallback polling과 typed frontend consumer를 제공한다. | SSE reference consumer, optional WebSocket consumer where justified, reconnect/duplicate/backpressure/multi-instance test, frontend typed consumer, fallback evidence |

### 신규 Capability 채택 경계

- Spring Data JPA는 이미 `cpf-starters/data/persistence-jpa`에 반영된 Optional Provider이므로 재생성하지 않고 `JpaRepository/Pageable/Sort/Specification/@Query/EntityManager`, CPF Paging Adapter, Lock, XA/JTA, DB3, Generator, EDU와 실제 Consumer를 재검수·보강한다.
- OAuth2/JWT/OIDC/SSO, KMS/HSM, Digital Signature, Tamper-evident Audit, AI Optional, XA/JTA/TCC/Inbox/Saga는 `07_15` Source를 기준으로 재검수하며 신규 중복 Starter를 만들지 않는다.
- gRPC는 실제 Product Consumer가 없는 한 이번 Canonical 기본 Portfolio에 추가하지 않는다. Protobuf는 `EVENT-SCHEMA`에서 사용할 수 있다.
- R2DBC/WebFlux persistence는 실제 채택 Requirement가 생길 때까지 강제하지 않는다.
- GraphQL, Distributed Session, Object Storage 등 Optional Capability는 미선택 Application에서 dependency/bean/config/sql/thread/endpoint/background runtime side effect가 0이어야 한다.

## 23. Legacy Alias Mapping

아래 ID는 검색과 과거 Evidence 연속성만 유지하고 Canonical 완료율에 포함하지 않는다.

| Legacy ID | 현재 Canonical 추적 대상 |
|---|---|
| `FACADE-LOCAL` | `ARCH-MSA + CPF-CALL` |
| `FACADE-REMOTE` | `ARCH-MSA + CPF-CALL` |
| `CMN-ID` | `CPF-TXID + BZA-SEQUENCE-SAMPLE/업무 Domain` |
| `CMN-FILE` | `CORE-FILE` |
| `CMN-FIXED` | `CORE-FIXED` |
| `ADM-COMP` | `ADM-RECOVERY` |
| `CENTER-ADV` | `CENTER-RUNNER + CENTER-PARAM + CENTER-CLAIM + CENTER-RATE + CENTER-REPROCESS + CENTER-UNKNOWN + CENTER-OPS` |
| `API-GATEWAY` | `GWY-ENTRY + GWY-ROUTING + GWY-TRUST + GWY-RESILIENCE + API-CONTRACT` |

## 24. 영구 완료 금지 조건

다음 상태에서는 어떤 Requirement도 `완료`로 처리하지 않는다.

- Dependency, Interface, DTO, Adapter, 화면, Table 또는 Script만 존재
- 실제 Product Consumer가 없음
- OSS와 Legacy가 동시에 Primary
- 일부 Module/화면/Vendor/Topology만 이관
- compile 또는 static Marker Gate만 통과
- package manifest와 lock/generated artifact 불일치
- Local에서만 동작하고 Remote/Multi-instance가 미검증
- 정상 예제만 있고 오류·권한·부분 실패·복구가 없음
- idempotency/fencing/unknown-result가 문자열이나 Column만 존재
- 위험 운영조치의 권한·사유·승인·감사가 없음
- DB/Generator/Vendor/Migration/Rollback 영향 누락
- final artifact가 아닌 Source directory만 SBOM/보안 검사
- 다른 Commit·장비·Artifact의 Evidence를 현재 결과로 사용
- 실행하지 않은 Test를 성공으로 기록
- 민감정보 원문이 Log, DB, Browser, Evidence 또는 운영화면에 존재
- 기존 성공 기능 회귀, Dead Code, Stale Evidence 또는 Repository garbage 잔존
- README/Guide/문서만 변경하고 실제 Source·Runtime이 불일치

## 25. 작업과 검수의 영구 원칙

- 작업 시작 전 Final Target, Continuity Ledger, Current Request, ADR, 최신 master와 실제 Git diff를 확인한다.
- 어떤 Requirement를 해결하는지, 실제 Owner와 Consumer가 누구인지 먼저 결정한다.
- MSA와 동일 JVM, 다중 인스턴스와 부분 실패, 보안·감사·운영·DB·Generator 영향을 함께 검토한다.
- 잘못된 구조를 영향도라는 이유로 무기한 보존하지 않는다. 대체 구현과 Consumer 이관 후 Legacy를 제거한다.
- 구현 가능한 Source·SQL·Test·Script·Guide·Evidence를 관성적으로 추후 작업으로 넘기지 않는다.
- 반복 비용이 큰 Runtime 검증은 통합 계획에 누적할 수 있으나 실행 전에는 `미검증`이다.
- 작업 종료 시 최신 Handover와 Requirement 상태를 갱신하되 README를 작업 일지로 사용하지 않는다.
- 사용자 승인 없이 Commit, Push, Branch, Tag와 PR을 생성하지 않는다.
