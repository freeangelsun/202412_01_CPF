# CPF 최종 목표 요구사항 정본

## 1. 문서 목적과 정본성

이 문서는 **Core Platform Framework(CPF)**의 최상위 제품 목표, 장기 아키텍처, Module Ownership, 확장 모델, 완료 판정과 Requirement Catalog를 정의하는 정본이다.

이 문서는 작업 일지나 날짜별 Evidence를 쌓는 문서가 아니다. 구현 상태와 실행 근거는 Gap Matrix, Review 문서, Generated Matrix와 Evidence에서 관리하되 모든 판단은 이 문서의 제품 목표를 기준으로 한다.

## 2. 제품 정의

CPF는 금융권을 포함한 다양한 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화할 수 있는 **Business Platform 품질의 Framework**다.

CPF는 단순 공통 Library, Sample 또는 개발 예제가 아니다. 다음을 하나의 제품 상태로 제공해야 한다.

- MSA와 Modular Monolith
- 동일 JVM Local Call과 분리 WAS Remote Call
- 다중 인스턴스, 부분 실패, 재시도, 복구와 분산 추적
- 금융권 수준의 인증, 권한, 감사, 마스킹과 승인
- 멱등성, 비동기, 재처리, 보상과 결과 불명 복구
- 외부 시스템, 전문, 파일, 첨부, 압축과 메시징
- Batch, Scheduler, Agent, Runner, Worker와 Center-Cut
- 운영 조회, 제어, 승인, 감사, 통계와 알림
- 신규 업무 도메인을 표준 구조로 확장하는 Generator
- EDU, OpenAPI, JavaDoc, Test Kit와 Reference
- 설치, Migration, Upgrade, Rollback, 배포, Backup, Restore와 DR
- Source, SQL, API, Test, Guide와 Evidence의 양방향 정합성

## 3. 지원 Topology

- Embedded JAR
- External WAS WAR
- Modular Monolith
- 독립 Microservice
- 동일 JVM Local Facade
- 분리 WAS Remote Facade
- ADM/BZA 독립 Static Web Artifact와 Web Server
- Agent/Runner/Worker 독립 Process
- Multi-instance
- Rolling, Canary, Blue-Green와 DR

특정 배포 방식이 Public Contract를 바꾸면 안 된다. Local과 Remote는 동일한 업무 계약, 표준 Header, 오류, 권한과 추적 기준을 사용한다.

## 4. 공식 명칭, Module과 식별 표준

- 정식 명칭: **Core Platform Framework**
- Business Platform: 제품 품질과 지향 수준
- 공식 Module: `cpf-` 접두사
- Java Root Package: `com.cpf.<domain>`
- SystemCode: 내부 식별용 3자리 대문자
- 읽을 수 있는 DomainName과 SystemCode는 서로 다른 개념

| 역할 | Module | Java Root Package | SystemCode | Owner 책임 |
|---|---|---|---:|---|
| 기술 공통 Framework | `cpf-core` | `com.cpf.core` | CPF | Contract, SPI, Technical Runtime |
| 고객 업무 공통 | `cpf-common` | `com.cpf.common` | CMN | 고객 공통 정책과 Core SPI 확장 |
| 플랫폼 관리자 | `cpf-admin` | `com.cpf.admin` | ADM | 플랫폼 운영 Control Plane |
| 고객 업무 관리자 | `cpf-biz-admin` | `com.cpf.bizadmin` | BZA | 고객 업무 관리, 승인과 선택형 Sample |
| Batch 실행 기반 | `cpf-batch` | `com.cpf.batch` | BAT | Batch, Scheduler, Agent, Runner, Worker, Center-Cut |
| Gateway Runtime | `cpf-gateway` | `com.cpf.gateway` | GWY | 외부 진입, Routing과 정책 집행 |
| 회원 Reference | `cpf-member` | `com.cpf.member` | MBR | 최소 업무 Reference |
| 계좌 Reference | `cpf-account` | `com.cpf.account` | ACC | Generator Lifecycle Reference |
| 교육·참조 | `cpf-reference` | `com.cpf.reference` | REF | EDU와 참조 구현 |
| 대외연계 | `cpf-external` | `com.cpf.external` | EXS | 기관별 Endpoint, Adapter, Mapping과 복구 |

구 명칭 `PFW`, `XYZ`는 승인된 호환성 이력 외에는 Active Source, Package, Config, SQL, Seed, Route, Queue, Topic, Cache, Log, Guide와 Evidence에 남기지 않는다.

## 5. 아키텍처와 의존성 원칙

### 5.1 Ownership

- 기술 공통: `cpf-core`
- 고객사 업무 공통: `cpf-common`
- 플랫폼 운영·보안·감사: `cpf-admin`
- 고객 업무 관리: `cpf-biz-admin`
- Batch·Scheduler·Agent·Runner·Worker·Center-Cut: `cpf-batch`
- 대외 기관별 연계: `cpf-external`
- 업무 기능과 원장: 해당 업무 도메인

파일이나 Class가 존재해도 실제 Consumer, 오류 처리, 복구, 운영 기능과 Runtime Evidence가 없으면 완료가 아니다.

### 5.2 의존성 방향

```text
Business Domain → cpf-common → cpf-core
cpf-gateway → cpf-core
cpf-batch → cpf-core + Business Public Contract
cpf-admin → Operations Command/Query Contract
cpf-biz-admin → Business Public Contract
cpf-external → cpf-core + Institution Adapter
```

다음을 금지한다.

- `cpf-core`의 업무·Common·Admin·Batch 역방향 의존
- 업무 도메인 간 DB 직접 접근
- Admin의 업무 원장 직접 갱신
- 내부 호출의 Gateway 재경유
- 순환 의존
- 실제 Consumer 없는 추상화
- Sample을 제품 Runtime으로 간주하는 구조

### 5.3 API 경계

- Public API: 고객 개발자가 안정적으로 사용하는 계약
- SPI: 고객·업무·Adapter가 확장하는 계약
- Internal: 제품 내부 구현이며 호환성 보장 대상이 아님

Public API, SPI와 Internal 경계는 Package, Module Metadata, JavaDoc, ArchUnit와 Build Gate로 강제한다.

## 6. cpf-core 목표

### 6.1 Runtime Foundation

- Standard/Extension Header
- Transaction, Trace, Segment와 Context Propagation
- Standard Error, Message와 Validation
- Local/Remote Service Call
- Timeout Budget, Retry, Circuit Breaker, Bulkhead와 Backpressure
- Idempotency, State Machine, Optimistic/Distributed Lock
- Unknown Result, Reconciliation와 Compensation Hook
- File/Attachment/Compression Technical Contract
- Broker Envelope, Outbox/Inbox와 DLQ Contract
- Fixed-Length Message Engine
- Collection, List, Page, Slice와 Cursor
- Security Context, Masking와 Audit Hook
- File/DB Log, Metrics와 Trace Boost
- Progressive Configuration와 Public Test Kit

### 6.2 Public API 구조

```text
com.cpf.core.api
com.cpf.core.spi
com.cpf.core.internal
```

공개 API는 IDE에서 발견 가능하고 최소 입력으로 안전하게 사용할 수 있어야 한다. 거대한 `Utils` Class와 의미 없는 JDK Wrapper는 금지한다.

권장 API 묶음:

- `CpfDates`, `CpfTimes`, `CpfClock`
- `CpfStrings`, `CpfNumbers`, `CpfDecimals`
- `CpfLists`, `CpfMaps`, `CpfAttributes`
- `CpfIds`, `CpfHashes`, `CpfFiles`
- `CpfValidation`, `CpfPages`, `CpfHeaders`

### 6.3 설정 우선순위

```text
CPF safe default
→ customer property
→ profile
→ operation override
→ per-call override
```

Override는 허용 범위, 권한, 버전, Audit와 Rollback을 가져야 한다.

## 7. cpf-common과 cmnDB 정책

`cpf-common`은 고객사 업무 공통 Extension Layer다. 고객 Header·User Context, 고객 Validation, Error Mapping, Web Client Profile, Masking·Audit 정책, 공통 Facade와 Core SPI 구현을 제공한다.

`cpf-common`은 기술 Engine이나 추정성 고객 업무를 선점하지 않는다.

### 7.1 cmnDB

초기 설치 시 `cmnDB` Schema는 생성하되, 기본 제품은 **최소 검증용 Sample Table 1개만** 제공한다.

그 한 Table로 다음을 검증한다.

- Connection과 Migration
- CRUD
- Search와 Sort Allowlist
- Offset Page, Slice와 Keyset/Cursor
- Validation과 Duplicate Key
- Optimistic Version Collision
- Transaction Commit과 Rollback

다음은 기본 `cmnDB`에 두지 않는다.

- `cmn_sequence*`
- `cmn_fixed_length_*`
- 통합 Business Log
- Notification Log
- 다수 EDU 전용 Table
- 실제 고객 요구 없이 추정한 업무 Table

`cpf-common`은 기본적으로 DB-less Library/Extension으로도 사용할 수 있어야 하며, Sample DB 기능은 명시적 Profile 또는 선택 설치로 활성화한다.

## 8. 데이터, SQL과 Migration 원칙

### 8.1 Physical Ownership

모든 Schema, Table, View, Procedure, Trigger, Index, Constraint와 Seed는 하나의 Owner와 실제 Consumer를 가진다.

권장 Schema:

```text
cpfDB, cmnDB, admDB, bzaDB, batDB,
mbrDB, accDB, refDB, exsDB
```

물리 이름은 Owner의 SystemCode와 일치해야 한다. 예외는 Architecture Decision과 Migration 근거가 있어야 한다.

### 8.2 설치와 데이터 분리

- Schema/User Provision
- Product Table/Constraint/Index
- Product Mandatory Meta Seed
- Optional Sample/EDU/Test Seed
- Verify/Smoke
- Reset
- Upgrade Migration
- Rollback/Recovery

위 책임을 하나의 파괴적 `all install` Script에 섞지 않는다.

Product Meta에는 System/Module Registry, 표준 Error/Response Code, Menu/Permission, Runtime Type Catalog, Masking/Retention Policy 등 실제 실행에 필요한 최소 데이터만 둔다. Password, Token, Private Key와 개인정보 원문은 Seed에 넣지 않는다.

### 8.3 Migration

- 적용된 Flyway Migration은 불변
- 신규 변경은 새 Version Migration으로 제공
- Upgrade와 Rollback 또는 Forward Recovery 제공
- Expand-Migrate-Contract 사용
- Backup, Restore와 Schema Version 검증
- 신규 설치 SQL과 Upgrade 최종 상태 일치

정식 적용 이력이 전혀 없고 모든 개발 DB를 폐기하는 Pre-release Re-baseline은 사용자 승인, 근거, Checksum 폐기와 Empty Install Evidence를 한 번에 갖춰야 한다.

## 9. 고정길이 전문, File과 대외연계

### 9.1 Fixed-Length Owner

`cpf-core`가 범용 기술 Engine을 소유한다.

- Layout, Field, Group과 Version Contract
- Byte Length와 Encoding
- Padding, Alignment와 Required Validation
- Header, Body, Trailer와 Repeated Group
- Numeric, Date, Amount Converter
- Parser와 Writer
- Error Field, Byte Offset와 Original Position
- Masking과 Secure Diagnostic
- Large Message Streaming
- Codec, Converter, Validator SPI

`cpf-external`은 기관별 Layout, Mapping, Endpoint, Authentication, Adapter, Retry, Unknown Result와 Reconciliation을 소유한다.

DB 기반 Dynamic Layout이 제품 필수라면 canonical Table은 Core 소유 Schema에 두고 ADM이 Core API를 통해 관리한다. Admin UI는 데이터 Owner가 아니다.

### 9.2 File/Attachment

Core는 범용 File, Attachment, Chunk, Compression, Checksum, Path Alias와 Transfer Contract를 제공한다. 기관별 SFTP, File Naming, Ack/Nack와 Reconciliation은 External Adapter가 소유한다.

### 9.3 Messaging

- Standard Envelope
- Correlation과 transactionGlobalId
- Outbox/Inbox
- Idempotent Consumer
- DLQ, Replay와 Poison Isolation
- Ordering Policy
- Schema/Version Compatibility
- 운영 조회와 Audit

## 10. 업무 채번 정책

업무 번호 채번은 `cpf-core` 또는 `cpf-common`의 필수 Framework 기능이 아니다.

- `cmn_sequence`, `cmn_sequence_issue_log`와 관련 기본 Runtime 제거
- UUID/ULID, transaction ID, trace ID, idempotency key 같은 기술 ID는 Core가 제공 가능
- `cpf-biz-admin`은 규칙 관리, 현재값, 시험 발급, 이력, 승인과 Audit을 보여주는 **선택형 Customization Sample**을 제공
- 실제 Runtime Owner는 고객 `cpf-common` 확장, 해당 업무 도메인 또는 별도 고객 중앙 채번 Service
- 온라인 업무가 BZA 관리자 Application 가용성에 필수 의존하는 구조 금지

BZA Sample은 운영 기본 설치에서 자동 활성화하지 않으며 Dev/EDU 또는 선택 Migration으로 제공한다.

## 11. Gateway 목표

- External Entry와 Authentication Policy
- Routing, Discovery, Load Balance와 Direct Instance
- Health, Timeout, Circuit Breaker와 Failover
- Header Trust Boundary와 재작성
- Rate Limit, Manual Block와 Maintenance
- Registry, Metrics와 Audit

Gateway는 내부 업무 호출의 Mandatory Hop이 아니다.

## 12. Batch, Center-Cut와 Agent 목표

### 12.1 General Batch

- Job, Step, Parameter와 Dependency
- Schedule, Calendar와 Misfire
- Checkpoint, Restart와 Partial Retry
- Java, approved Shell, File Watch, File Process와 File Transfer Executor
- Timeout, Retry, Skip, Threshold, Pause와 Rollback
- Agent Pool, Capability, Zone와 Path Alias

Shell Executor는 승인된 Script Catalog만 실행하며 임의 Command 실행을 금지한다.

### 12.2 Center-Cut

`cpf-batch`는 Batch Agent와 구분되는 `CenterCutRunner` Runtime 책임을 제공한다.

```text
request → validate → create job
→ persist immutable parameter/execution policy
→ target generation → item claim
→ worker dispatch → business transaction
→ result/error/attempt
→ aggregate → ADM timeline/recovery
```

필수:

- Job, Item, Attempt
- parent/child transactionGlobalId
- Immutable Parameter Snapshot, hash, schema, encryption과 masking
- Chunked/Streaming Target Generation과 Resume
- Claim, Lease, Fencing과 Multi-instance Duplicate Prevention
- Global TPS/RPS, Backpressure와 Concurrency
- Pause, Resume, Cancel과 Drain
- Failed-only Reprocess
- Unknown Result 확인 후 재처리
- Compensation와 Manual Recovery
- Runner를 Agent 내장 또는 별도 Process로 배포 가능

### 12.3 Agent/Worker

- Registry와 Capability
- Primary/Secondary, Failover와 Failback
- Lease와 Fencing Token
- Drain과 Takeover
- Pre-execution Failure와 Mid-execution Unknown 구분
- Shared Durable Storage가 없는 File Job의 Blind Failover 금지

## 13. ADM과 BZA 목표

### 13.1 ADM

플랫폼 운영 Control Plane:

- Online Transaction과 Timeline
- Async, Unknown Result와 Reconciliation
- Batch, Center-Cut, Agent, Runner와 Worker
- Gateway, Registry와 Health
- Log, Trace, Metric와 Alert
- Security, Approval와 Immutable Audit
- Recovery, Compensation와 Incident

상태를 DB에서 직접 수정하지 않고 Owner Command API를 사용한다. 위험 조치는 권한, 승인, 사유와 감사가 필수다.

### 13.2 BZA

고객 업무 관리:

- 업무 Menu와 Permission
- 고객 업무 조회, Download와 Approval
- 고객 Extension
- 선택형 Reference Sample

BZA는 고객·상품·주문 같은 추정성 원장을 임의 소유하지 않으며 CPF Platform Runtime을 직접 제어하지 않는다.

## 14. Frontend 목표

- Vue 3, TypeScript와 Vite
- ADM/BZA 독립 Application과 Artifact
- Feature Folder 구조
- API Contract와 Server-side Authorization
- Accessibility와 Keyboard Navigation
- CSP, Cookie, CSRF와 Secure Header
- SPA Deep Link
- Production Build
- Web Server와 Java API/WAS 분리 배포
- 독립 Version, Compatibility와 Rollback
- Browser E2E

메뉴 숨김만으로 권한을 처리하지 않는다.

## 15. API 목표

- Standard Header와 Trust Boundary
- Standard Success/Error Envelope
- List, Page, Slice와 Signed Cursor
- Field/Operator/Sort Allowlist
- Async 202, Polling, Callback와 Event
- File, Multipart, Range와 Streaming
- SSE
- Idempotency와 Conflict Contract
- Versioning, Deprecation과 Compatibility
- OpenAPI operationId, Example, Error와 Permission
- JavaDoc와 Consumer Contract Test

## 16. Security와 Audit 목표

- AuthN, AuthZ, RBAC와 필요한 범위의 ABAC
- 관리자 MFA, IP Allowlist와 Session Policy
- mTLS, OAuth2/OIDC, JWT와 API Key
- Secret 외부화, Rotation과 Revocation
- PII Classification, Masking와 최소 수집
- Download Approval, Watermark, Expiry와 Audit
- Dual Control, Break-glass와 사후 Review
- Immutable Audit와 Tamper Detection
- Secure Coding, Dependency, SBOM과 Secret Scan

License/Edition 정책은 제품 안정화 후 별도 결정하며 완료되지 않은 License 문서를 GA 산출물처럼 제공하지 않는다.

## 17. Generator와 Developer Experience

Generator 입력:

- DomainName
- 3자리 SystemCode
- Module/Package
- DB Vendor
- Capability

필수 Lifecycle:

```text
create → verify → db-init → build/test/runtime
→ remove → regenerate → parity verification
```

필수 특성:

- 예약 Code와 기존 등록 충돌
- Module, Package, Config, Route, Menu와 SQL 충돌
- 선택 Capability만 생성
- Local/Remote Contract Parity
- 사용자 소유 File 보호
- 반복 생성 결정성
- 안전 삭제
- Manifest와 Ownership Metadata
- 공식 CPF Public API를 실제 사용하는 Sample

MariaDB는 필수 실검증 대상이다. PostgreSQL, Oracle과 SQL Server는 Vendor별 Script, Dialect, Type, Paging, Lock, Migration과 Runtime Evidence가 있을 때만 지원 완료로 표기한다.

## 18. Reliability와 Observability

### Reliability

- Multi-instance
- Idempotency
- Timeout Budget
- Retry와 Retry Storm 방지
- Circuit Breaker와 Bulkhead
- Backpressure
- Lease와 Fencing
- Checkpoint와 Durable Recovery
- Partial Failure
- Unknown Result와 Reconciliation
- Compensation
- Fault Injection과 Chaos Smoke

### Observability

- File Log와 DB Log
- transactionGlobalId, segment와 timeline
- Metrics와 SLI/SLO
- Instance, Host와 Container
- Agent, Runner와 Worker
- Failure Stage
- Dynamic Log Level과 Trace Boost
- Alert와 Incident Link
- Retention, Masking과 Secure Evidence

## 19. 설치, 배포와 호환성

필수:

- Empty Install
- Allowlisted Reset
- Service User Provision과 최소 권한
- Idempotent Product Seed
- Reinstall
- Upgrade와 Rollback/Forward Recovery
- Backup, Restore와 DR
- JAR와 WAR
- ADM/BZA Static Artifact
- Docker/Kubernetes는 실제 검증 후 지원 표기
- Rolling, Canary와 Blue-Green
- API, DB, Config, Message, File와 전문 호환성

회사 PC와 집 PC의 CPF DB가 비어 있어도 동일한 정본 Script로 Schema, Table, Constraint, Index와 Product Meta를 재생성할 수 있어야 한다.

## 20. Repository와 문서 정본화

Root에는 제품 식별, Build와 실행에 필요한 최소 파일과 공식 Module만 둔다.

- Root `README.md`: 유일한 제품 README
- `CPF_FINAL_TARGET_REQUIREMENTS.md`: 최상위 목표 정본
- Architecture/Specification: 제품 구조와 계약
- Guide: 개발, 운영, 설치와 복구
- Work/Review: 요청, 검수와 진행
- Evidence: 실행 근거
- Generated: 재생성 가능한 파생 산출물
- Release: 실제 Release가 있을 때만 CHANGELOG/Release Note

기능과 구조가 안정되기 전에는 Markdown을 정본으로 사용한다. DOCX/PDF는 최종 제품화 단계에서 Source와 일치하도록 생성한다.

## 21. Evidence와 완료 판정

허용 상태값:

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

각 요구사항은 적용 가능한 범위에서 다음과 대조한다.

- Source와 계층 연결, 실제 Consumer
- Module, Package와 Ownership
- API Contract, 오류와 권한
- SQL, Migration, Install, Upgrade와 Rollback
- 정상, 오류, 경계와 부분 실패
- 멱등성, 동시성, Retry, Unknown과 Recovery
- Multi-instance, Lease와 Fencing
- Security, Audit와 Masking
- 운영 조회, 제어, 통계와 Alert
- EDU, OpenAPI와 JavaDoc
- Unit, Integration, Runtime와 Browser
- 최신 Commit/환경과 일치하는 Evidence
- 기존 성공 기능 회귀
- Dead Code, Stale Evidence와 Garbage 제거

파일·Class 존재, 정적 검색, Swagger 노출, 일부 Test, 과거 Evidence, Codex 보고와 문서의 완료 표시는 단독 완료 근거가 아니다.

## 22. Requirement Catalog

### Architecture/Core

- `ARCH-MISSION`, `ARCH-MSA`, `ARCH-BOUNDARY`, `ARCH-LAYER`
- `CORE-API`, `CORE-SPI`, `CORE-CONFIG`, `CORE-TESTKIT`
- `CPF-CALL`, `CPF-REGISTRY`, `CPF-ROUTING`, `CPF-HEALTH`
- `CPF-HEADER`, `CPF-CONTEXT`, `CPF-TXID`, `CPF-ERROR`, `CPF-VALID`
- `CPF-IDEMP`, `CPF-STATE`, `CPF-LOCK`, `CPF-RESILIENCE`, `CPF-DEADLINE`
- `CPF-LOGDB`, `CPF-FILELOG`, `CPF-TRACE`, `CPF-MASK`
- `CORE-FIXED`, `CORE-FILE`, `CORE-MESSAGE`

### Common/Data

- `CMN-EXTENSION`, `CMN-SAMPLE-DB`, `DB-OWNERSHIP`, `DB-INSTALL`
- `DB-MIGRATION`, `DB-ROLLBACK`, `DB-BACKUP`, `DB-MULTI-VENDOR`

### Gateway/External/Event

- `GWY-ENTRY`, `GWY-ROUTING`, `GWY-TRUST`, `GWY-RESILIENCE`
- `EXS-INST`, `EXS-REST`, `EXS-FIXED`, `EXS-SEC`, `EXS-FILE`
- `EXS-UNKNOWN`, `EXS-RECON`
- `EVENT-CORE`, `EVENT-OUTBOX`, `EVENT-BROKER`, `EVENT-DLQ`
- `SAGA-CORE`, `SAGA-COMP`, `SAGA-MANUAL`

### Batch/Center-Cut

- `BAT-CORE`, `BAT-JOB`, `BAT-ITEM`, `BAT-EXECUTOR`, `BAT-AGENT`
- `CENTER-CORE`, `CENTER-RUNNER`, `CENTER-PARAM`, `CENTER-CLAIM`
- `CENTER-RATE`, `CENTER-REPROCESS`, `CENTER-UNKNOWN`, `CENTER-OPS`

### Admin/Security/Operations

- `ADM-AUTH`, `ADM-RBAC`, `ADM-AUDIT`, `ADM-TX`, `ADM-TIMELINE`
- `ADM-BATCH`, `ADM-CENTER`, `ADM-AGENT`, `ADM-EXS`, `ADM-RECOVERY`
- `BZA-BUSINESS`, `BZA-APPROVAL`, `BZA-SEQUENCE-SAMPLE`
- `SEC-AUTHN`, `SEC-AUTHZ`, `SEC-SECRET`, `SEC-CERT`, `SEC-PRIVACY`
- `SEC-DOWNLOAD`, `SEC-APPROVAL`, `SEC-AUDIT`
- `OPS-METRIC`, `OPS-SLO`, `OPS-ALERT`, `OPS-INCIDENT`, `OPS-RUNBOOK`
- `OPS-CONFIG`, `OPS-DRIFT`, `OPS-CAPACITY`, `OPS-DR`

### Generator/EDU/Productization

- `DEVEX-QUICK`, `DEVEX-CODEGEN`, `ONBOARD-DOMAIN`
- `SAMPLE-ACC`, `SAMPLE-MBR`, `SAMPLE-REF`, `SAMPLE-BIZADM`, `SAMPLE-EDU`
- `API-CONTRACT`, `API-PAGING`, `API-ASYNC`, `API-FILE`
- `TEST-UNIT`, `TEST-CONTRACT`, `TEST-RUNTIME`, `TEST-BROWSER`
- `TEST-BROKER`, `TEST-FAULT`, `TEST-EVIDENCE`
- `REL-BUILD`, `REL-DEPLOY`, `REL-MIG`, `REL-COMPAT`
- `DOC-GOV`, `DOC-PRODUCT`, `REQ-GOV`, `REQ-REVIEW`, `REQ-CODEX`

## 23. 최종 제품화 Gate

다음이 모두 최신 Commit과 재현 가능한 환경에서 확인돼야 GA 완료다.

- 공식 Module/Package/SystemCode와 DB Physical Ownership
- Clean Build와 Full Test
- Empty DB Install, Reinstall, Upgrade와 Rollback
- 전체 Runtime과 주요 API E2E
- Multi-instance, Worker, Lease, Fencing과 Recovery
- 실제 Broker, External Failure와 Unknown Result
- Batch, Center-Cut, Agent/Runner/Worker
- ADM/BZA Runtime, Production Build와 Browser E2E
- Security, Approval, Audit와 Masking
- EDU, OpenAPI, JavaDoc와 Generator Lifecycle
- 설치, 배포, 운영, Migration과 Recovery Guide
- Source, SQL, API, Test, Document와 Evidence 정합성
- Root Hygiene, No Stale Name, No Stale Evidence와 No Secret
