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
| 생성형 업무 Reference Instance | `cpf-member` | `com.cpf.member` | MBR | Generator Golden Contract를 검증하는 최소 생성형 업무 Domain Instance |
| 교육·참조 | `cpf-reference` | `com.cpf.reference` | REF | CPF 기능 EDU와 선택형 참조 구현 |

`cpf-member`의 MBR은 고정 업무기능 표준이 아니라 **Generator 산출물과 동일해야 하는 Golden Reference Instance**다.
향후 `cpf-account`, `cpf-payment`, `cpf-insurance`, `cpf-lng`, `cpf-ing` 등 고객 업무 Domain은 공식 고정 목록이 아니며
사용자는 임의의 `DomainName + 3자리 SystemCode`로 동일한 표준 구조를 생성할 수 있어야 한다.
기본 생성형 Domain의 Source/DB/Test 구조는 Metadata를 제외하면 동일하고, DB 사용 시 중앙 Vendor Domain Template로
`${TablePrefix}_sample_item` CRUD/Search/Paging/Validation 예제가 실제 DB까지 연결되어야 한다.
신규 Domain 추가 때문에 CPF 본체의 `if/switch`, 고정 Domain 배열 또는 Vendor별 수작업 SQL 복제가 늘어나면 Generator 설계 실패다.

`cpf-external`/EXS는 영구 고정 업무 Domain으로 제공하지 않는다. 기존 Repository에 남은 대외연계 기능은
범용 기술 Contract/SPI, 고객 공통 정책, Generated Domain Adapter 또는 `cpf-reference` EDU 중 실제 책임에 맞는 Owner로
이관한 뒤 고정 Module/SystemCode/DB를 제거한다. 기관별 업무 Adapter 때문에 중앙 Generator에 EXS를 하드코딩하지 않는다.

구 명칭 `PFW`, `XYZ`는 승인된 호환성 이력 외에는 Active Source, Package, Config, SQL, Seed, Route, Queue, Topic, Cache, Log, Guide와 Evidence에 남기지 않는다.

## 5. 아키텍처와 의존성 원칙

### 5.1 Ownership

- 기술 공통: `cpf-core`
- 고객사 업무 공통: `cpf-common`
- 플랫폼 운영·보안·감사: `cpf-admin`
- 고객 업무 관리: `cpf-biz-admin`
- Batch·Scheduler·Agent·Runner·Worker·Center-Cut: `cpf-batch`
- 대외연계 기술 Contract/SPI: `cpf-core`; 고객 공통 연계정책: `cpf-common`; 기관/업무 Adapter: 해당 Generated Domain 또는 고객 확장 Module
- 업무 기능과 원장: 해당 업무 도메인

파일이나 Class가 존재해도 실제 Consumer, 오류 처리, 복구, 운영 기능과 Runtime Evidence가 없으면 완료가 아니다.

### 5.2 의존성 방향

```text
Business Domain → cpf-common → cpf-core
cpf-gateway → cpf-core
cpf-batch → cpf-core + Business Public Contract
cpf-admin → Operations Command/Query Contract
cpf-biz-admin → Business Public Contract
Generated Domain/Customer Adapter → cpf-common → cpf-core External SPI
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

초기 설치 시 `cmnDB` Schema는 생성한다. 고객 업무를 추정해 Table을 선점하지 않는 것이 기본 원칙이며,
**검증용 Sample Table 1개 + Final Target에서 명시적으로 CPF 제품 기능으로 확정된 최소 공통 Table**만 제공한다.
현재 정식 제품 Table은 `CMN-CALENDAR`의 `cmn_business_calendar_day`이며, 그 외 고객 업무 Table은 기본 설치에 추가하지 않는다.

검증용 Sample Table 한 개로 다음을 검증한다.

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

`cpf-common`은 DB-less Library/Extension으로도 사용할 수 있어야 한다. DB-less 모드의 Calendar는 주말 기반 조회만 제공하고
운영 Override 변경은 fail-closed한다. CPF 제품 설치에서는 CMN canonical Calendar Store를 사용한다.

## 8. 데이터, SQL과 Migration 원칙

### 8.1 Physical Ownership

모든 Schema, Table, View, Procedure, Trigger, Index, Constraint와 Seed는 하나의 Owner와 실제 Consumer를 가진다.

권장 Schema:

```text
cpfDB, cmnDB, admDB, bzaDB, batDB, refDB
+ Generator Manifest로 선언된 업무 Domain Schema(예: mbrDB, lngDB, ingDB)
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

### 8.3 Query Ownership

업무/관리 Module의 Vendor SQL은 Java String literal에 두지 않고 Owner가 식별 가능한 Query ID와 `cpf-tools/db/vendor/<vendor>/runtime/<owner>` Resource로 관리한다. 업무/관리/Generated Domain은 `com.cpf.core.common.*` 내부 구현을 직접 참조하지 않고 Public API/SPI를 사용한다. Query Resource는 Owner, Consumer, Parameter/Result Contract와 Vendor별 검증 근거를 가져야 한다.

### 8.4 Migration

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

기관별 Layout, Mapping, Endpoint, Authentication, Adapter는 해당 고객/Generated Domain 또는 선택적 고객 확장 Module이 소유한다. `cpf-core`는 범용 External SPI, Retry/Timeout/Unknown/Reconciliation 기술 Primitive를 제공하고 `cpf-common`은 고객 공통 연계 정책을 확장할 수 있다.

DB 기반 Dynamic Layout이 제품 필수라면 canonical Table은 Core 소유 Schema에 두고 ADM이 Core API를 통해 관리한다. Admin UI는 데이터 Owner가 아니다.

### 9.2 File/Attachment

Core는 범용 File, Attachment, Chunk, Compression, Checksum, Path Alias와 Transfer Contract를 제공한다. 기관별 SFTP, File Naming, Ack/Nack와 Reconciliation은 해당 고객/Generated Domain의 External Adapter가 소유한다.

### 9.3 Messaging

- Standard Envelope
- Correlation과 transactionId
- Outbox/Inbox
- Idempotent Consumer
- DLQ, Replay와 Poison Isolation
- Ordering Policy
- Schema/Version Compatibility
- 운영 조회와 Audit

## 9.4 거래 식별자 표준

- CPF의 거래 실행 인스턴스 식별자는 **`transactionId` 하나**다. 과거 별도 Global 거래 ID 명칭은 제품 개념으로 유지하지 않는다.
- 외부/선행 거래가 유효한 transactionId를 제공하면 그대로 승계한다.
- 내부 Scheduler/Batch/Worker/Center-Cut/Agent 등에서 독립 거래가 시작되면 `cpf-core`가 신규 transactionId를 생성한다.
- 기본 생성 규격은 `yyyyMMddHHmmssSSS(17) + SystemCode(3) + wasId(7) + sequence(7)`의 34자리다.
- 동일 업무 흐름의 Local/Remote/Async/Retry/Batch 후속 처리는 같은 transactionId를 사용한다.
- 호출 세부 계층은 `segmentId`, `parentSegmentId`, call depth와 attempt로 표현한다.
- `standardExecutionId`는 `OXYZAA0001` 같은 실행 정의 ID이며 transactionId와 별개다.
- File Log, DB Log, ADM Timeline, Broker Envelope, Idempotency/Reconciliation, Batch/Center-Cut, OpenAPI/EDU가 같은 계약을 사용한다.
- DB/Source/API에서 과거 Global/root/parent/child 거래 식별자 명칭을 신규로 만들지 않는다. 고객/Release에 적용된 Historical Migration은 불변으로 추적하되, 외부 배포 전 pre-GA 개발 Migration은 잘못된 명칭을 영구 보존하지 않고 적용 이력·호환성 영향을 확인한 뒤 정본화할 수 있다.

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
- 동일 `transactionId` 승계 + `segmentId`/`parentSegmentId` 호출 계층
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


### 13.3 ADM 운영자 Directory와 위험조치 승인

ADM은 HR 원장을 소유하지 않지만 금융권 운영 승인과 Immutable Audit에 필요한 운영자 조직 문맥을 확보해야 한다.
`adm_operator`는 인증 Identity로 유지하고 조직·직급·직책·사번·외부 Directory Subject 등은 별도 Profile/Directory 경계에서 관리한다.
기본 DB Adapter를 제공할 수 있으나 LDAP/AD/IAM/HR 연계를 위한 확장 Port를 허용하며 실제 Password, Token, Private Key를 Profile에 저장하지 않는다.
운영자 연락처는 인증 Identity 컬럼이 아니라 `adm_operator_profile` 등 Directory/Profile 경계가 소유한다. 휴대폰은 `연락처(휴대폰)`, 사내/내선 전화는 `내부 전화번호`로 구분하고 둘 다 선택값/문자열로 저장하여 국가번호와 선행 0을 보존한다. 목록·다운로드·로그·Evidence에서는 개인정보 Masking/Audit 정책을 적용한다.

ADM 제품 Runtime은 영속 DB를 fail-closed로 사용한다. DB 오류를 메모리 저장소 성공으로 변환하지 않으며, MEMORY 저장소는 `local/test/demo/library` 등 명시적 비제품 Profile에서만 허용한다. 일반 운영자 생성은 `PENDING_ACTIVATION`, Role 미부여, 비밀번호 변경 필요를 기본으로 하고 Identity/Profile/Role Mapping은 하나의 Transaction 경계와 생성 `operationId` 멱등성으로 보호한다.

기본 운영자 API는 연락처 Masked Projection만 반환한다. Raw PII는 별도 권한·사유·`transactionId`·감사가 필요하고 조회 사유는 URL query string이 아니라 POST body로 전달하며 응답은 `Cache-Control: no-store`를 사용한다.

ADM 위험조치는 단순 RBAC 버튼 권한만으로 실행하지 않는다. 다음을 만족하는 독립 Approval Runtime을 `cpf-admin`이 소유한다.

- 승인 정책 Version과 시행일/만료일
- 요청자와 승인자의 분리, 자기승인 금지 기본값
- Operator/Role/Organization/Organization Manager 대상 해석
- 순차, 병렬, `ALL`, `ANY`, `N_OF_M` 결정 규칙
- 요청 Reason과 대상 명령의 불변 Hash/Snapshot
- 승인 만료, 취소, 반려, 재요청과 Idempotency
- Owner Command API 실행과 승인 실행 결과의 연결
- 실행 결과 불명 시 자동 성공 처리 금지 및 Recovery 상태
- Break-glass는 별도 권한, 긴급 사유, TTL, 사후 Review와 Immutable Audit 필수
- Service 차단/재개, Routing 강제 변경, Batch 강제 제어, Center-Cut 재처리, Unknown Result 수동 확정, Compensation, DLQ Replay, 대량 Download, Masking 해제, Runtime Config, Credential/Certificate Rotation 등 위험행위를 정책 대상으로 확장 가능

ADM은 다른 Owner DB를 직접 수정하거나 승인 완료 후 SQL을 직접 갱신하는 방식으로 제어하지 않는다. 승인 완료 후에도 실제 실행은 Owner의 Command API/Facade로 수행하고 Local/Remote Topology에서 동일 계약을 유지한다.

### 13.4 BZA 조직·직원·업무결재

BZA는 고객 업무 관리자와 업무 결재를 소유한다. `bza_admin_user` 인증 Identity와 직원/조직 Directory를 분리하고 다음 모델을 지원한다.

- 조직 Hierarchy와 조직 유형, 사용기간
- 직원, 사번, 조직 소속, 직급, 직책, 재직 상태
- 업무 이메일, `연락처(휴대폰)`, `내부 전화번호`를 서로 다른 선택 Profile 값으로 관리하고 전화번호는 숫자형으로 저장하지 않음
- 신규 직원 재직 상태의 Safe Default는 `EMPLOYED`; 기존 데이터는 Migration 정책 없이 임의 변환하지 않음
- 직원 재직 상태와 관리자 인증 계정 상태는 서로 다른 Catalog로 관리하며 `ACTIVE`를 직원 재직상태와 혼용하지 않음
- 일반 BZA 관리자 생성은 `PENDING_ACTIVATION`, Role 미부여, 비밀번호 변경 필요를 기본으로 하며 Role 유효성 확인 후에만 `ACTIVE` 전환
- BZA 기본 직원 목록/API는 이메일·휴대폰·내부전화 Masked Projection을 사용하고 Raw 조회는 별도 `PII_RAW` 권한·사유·Audit·`no-store`를 요구
- BZA Audit Snapshot은 PII를 Masking하고 Password/Secret/Token/Credential/Attachment 원문을 Redaction한 canonical snapshot으로 Hash Chain을 구성
- 겸직/복수 조직/파견/직무대행을 표현할 수 있는 유효기간 기반 Assignment
- 직원 한 명의 복수 Role을 지원하는 User-Role Mapping과 역할 유효기간
- 조직개편·인사변경 후에도 과거 결재가 변하지 않도록 결재 생성 시 조직·직급·직책·결재자 Snapshot 보존

BZA Approval은 최소 다음을 실제 Engine으로 지원한다. 컬럼이나 Enum만 존재하는 것은 구현으로 보지 않는다.

- 순차 결재와 병렬 결재
- 개인 승인, Role 승인, 부서/조직 합의, 조직 책임자 승인
- 부서 전원 합의(`ALL`), 한 명 이상 합의(`ANY`), N명 중 M명 합의(`N_OF_M`)
- 필수 부서와 선택 부서
- 승인, 합의, 검토의 Step 의미 구분
- 위임, 대결, 부재기간과 위임 유효기간
- 요청자 자기승인 방지, 승인자 자격 재검증과 Separation of Duties
- Approval Policy Version, 시행일, 종료일과 변경 Audit
- 정책 정의와 실제 Approval Instance 분리
- Instance 생성 시 참여자 해석 결과 Snapshot
- 낙관적 잠금, 동시 승인, 중복 결정 Idempotency
- 반려, 회수, 취소, 재상신, 만료와 Escalation
- 결재선/정책 Simulation API와 운영 UI
- 결재 이력, 업무 Audit, transactionId와 Attachment 연결

ADM Approval과 BZA Approval은 정책·DB·Service를 공유하지 않는다. 공통화가 필요하면 상태/결정 같은 작은 기술 Primitive만 `cpf-core`에 둘 수 있으며 업무 Approval Engine을 Core로 올리지 않는다.

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
create → optional DB bootstrap → CRUD/Search/Paging/Validation/Commit/Rollback 검증
→ build/test/runtime → remove → regenerate → normalized parity verification
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
- 생성형 기본 DB는 중앙 `cpf-tools/db/vendor/<vendor>/domain-template`만 정본으로 사용하고 Generator 내부에 DDL을 복제하지 않는다.
- DB Capability 사용 시 `${TablePrefix}_sample_item` 한 개의 Golden Sample Table과 CRUD/Search/Paging/Validation/Idempotency/Optimistic Lock/Commit/Rollback Example을 생성한다.
- Generator는 선택적으로 DB bootstrap을 orchestration할 수 있으나 공식 `initialize-domain-database.ps1`를 호출해야 한다.
- MBR 등 Reference Instance와 임의 LNG/ING/TST 생성 결과를 이름 Normalize 후 Source/DB/Test 구조 parity로 비교하는 Quality Gate를 둔다.
- 독립 Generated Domain/독립 WAS는 CPF Public Artifact를 임의 Source/JAR 복사로 포함하지 않고 CPF BOM + Convention Plugin + Versioned Maven Artifact 계약으로 소비한다.
- CPF Artifact 공급은 `LOCAL_DEV` / `REMOTE` / `OFFLINE` 세 모드를 같은 BOM/Version 계약으로 지원한다. `LOCAL_DEV`는 검증된 Shared Local Maven Repository, `REMOTE`는 Nexus/Artifactory 등 승인 Registry, `OFFLINE`은 Manifest/Checksum을 포함한 Versioned Offline Maven Bundle을 사용한다.
- CI/STG/PROD의 `REMOTE` 모드는 Remote Artifact가 없을 때 개발자 Local Repository로 fallback하지 않고 fail-closed한다. Shared Local Repository 자동 갱신은 개발 편의용 opt-in이며, Quality Gate를 통과한 staging artifact를 검증한 뒤 승격한다.
- Generated `bootJar`/`bootWar`는 필요한 `cpf-core`, `cpf-common` 및 선택 Capability의 Public Contract JAR가 실제 패키지 내부에 포함됐는지 Gate로 검증한다.

CPF의 공식 지원 DB Vendor는 MariaDB, PostgreSQL, Oracle 3종으로 한정한다. MySQL과 Microsoft SQL Server(MSSQL)는 제품 지원 범위와 Generator/Tool 선택값에서 제거한다. 세 공식 Vendor 모두 Vendor-native Script, Dialect, Type, Paging, Lock, Canonical Schema/Seed, Migration, Upgrade/Rollback, Runtime Query와 Runtime Evidence가 있을 때만 Release 검증 완료로 표기한다.

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
- transactionId, segment와 timeline
- Metrics와 SLI/SLO
- Instance, Host와 Container
- Agent, Runner와 Worker
- Failure Stage
- Dynamic Log Level과 Trace Boost
- Alert와 Incident Link
- Retention, Masking과 Secure Evidence


### 18.1 DB Vendor source ownership 정본

Platform DB의 canonical source, domain-template, install/seed/migration/rollback/verify는 모두 `cpf-tools/db/vendor/<vendor>` Owner 경계 안에서 동일하게 관리한다. 특정 Vendor만 별도 `cpf-tools/db/source/<vendor>` 경로를 갖지 않는다. 공식 3 Vendor의 산출물은 Canonical Schema/Seed에서 Vendor-native 형태로 생성·동기화한다. 다른 Vendor SQL의 단순 복사·치환을 완료 근거로 사용하지 않으며, lifecycle/readiness 또는 Runtime Evidence가 부족하면 Release Gate에서 fail-closed한다.

DB/SQL/Metadata 변경 후 `sync-database-artifacts.ps1`은 각 하위 gate의 실제 process exit code를 판정해야 하며 과거 native `$LASTEXITCODE` 잔존값을 성공/실패로 오판해서는 안 된다.

### 18.2 Tooling/Deploy/Frontend Repository 정책

- `cpf-tools/README.md`와 `cpf-docs/guides/CPF_TOOLS_GUIDE.md`를 공식 Tooling 진입점으로 유지한다.
- Docker Compose, 환경별 배포 파일은 Root에 두지 않고 `deploy/` 아래에 둔다.
- `logs/`, 임시 ZIP, patch staging, build output은 Root 정식 파일이 아니다.
- ADM/BZA는 Vue 3 + TypeScript + Vite를 사용하고 App Shell, feature package, route registry, state/API boundary를 분리한다. 대형 단일 `App.vue` 또는 단일 `console.ts`에 운영 기능을 계속 누적하지 않는다.
- 화면 자산은 제품 Artifact에 self-contained 되어야 하며 외부 CDN/원격 CSS/font/icon을 Runtime 의존성으로 사용하지 않는다.
- route-level lazy import 또는 동등한 code splitting을 적용하고 Browser E2E 없이 완료 처리하지 않는다.

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
- Java/Gradle/Spring Boot/Spring Framework 등 핵심 Build Runtime은 공식 지원 Matrix 안의 조합만 GA Release로 허용하며, 지원 범위 밖 조합은 명시적 `TRANSITION` 상태로 관리하고 Commercial Release Gate를 차단한다.
- CPF Public Artifact의 `LOCAL_DEV`/`REMOTE`/`OFFLINE` Repository Federation, Version/BOM/Plugin Marker/Hash/Manifest 정합성
- 독립 배포 Artifact는 Runtime에 필요한 CPF Public Dependency를 self-contained packaging하거나 제품이 명시한 Shared Runtime 방식으로 결정적으로 해석해야 하며, 개발자 수동 JAR 복사를 정상 배포 절차로 사용하지 않음
- ADM/BZA Static Artifact
- Docker/Kubernetes는 실제 검증 후 지원 표기
- Rolling, Canary와 Blue-Green
- API, DB, Config, Message, File와 전문 호환성

회사 PC와 집 PC의 CPF DB가 비어 있어도 동일한 정본 Script로 Schema, Table, Constraint, Index와 Product Meta를 재생성할 수 있어야 한다.

## 20. Repository와 문서 정본화

Root에는 제품 식별, Build와 실행에 필요한 최소 파일과 공식 Module만 둔다. **Root의 문서 파일은 `README.md`만 허용한다.**

- Root `README.md`: 유일한 제품 README
- `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`: 작업/검수 기간의 최상위 목표 정본. 최종 제품 정본화 완료 후 영구 Architecture/Specification에 흡수하고 작업 정본은 제거 대상
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


## 21.1 Requirement ID 연속성과 삭제 금지

Requirement ID는 세션, PC, Codex 계정, Architecture Rename과 무관한 장기 추적 Key다.
한 번 정본에 등록된 Requirement는 조용히 삭제하거나 새로운 이름으로 다시 집계하지 않는다.

- 통합되면 기존 ID를 `superseded-by` 관계로 Continuity Ledger에 남긴다.
- 분해되면 기존 ID에서 신규 ID들로 `split-into` 관계를 남긴다.
- Owner가 바뀌어도 기존 요구 의미를 잃지 않고 Migration 사유를 기록한다.
- 같은 요구를 Legacy ID와 신규 ID 양쪽에서 완료율에 중복 집계하지 않는다.
- Requirement 삭제는 오직 제품 정책에서 요구 자체를 폐기한 경우에만 가능하며 폐기 이유, 영향, 대체 요구와 승인 근거가 있어야 한다.
- 최신 정본의 Count가 이전 정본보다 감소하면 반드시 Continuity Ledger의 Mapping과 폐기 근거로 설명 가능해야 한다.

`a63380e6c736fa9c5ae7e425d0e301d21ef3b848` 정본의 133개 Catalog와 `22b1874e67547372b51a4bcd21f47aea6fcb5c25` 정본의 126개 Catalog를 재조정한 결과, 의미가 실제로 남아 있는 34개 Requirement를 복구한다. 8개 Legacy ID는 새로운 Owner/세분화된 ID로 명시적으로 Mapping하며 중복 집계하지 않는다. 이번 전수검수에서 독립 검증축이 필요하다고 확인한 `ADM-APPROVAL`, `BZA-ORG` 2개를 신규 정본화하여 현재 Canonical Requirement Count는 **162개**다.

## 21.2 복구된 독립 Requirement의 의미

133→126 축소 과정에서 추적 ID가 사라졌지만 현재 제품 목표에서 여전히 독립적으로 검증해야 하는 34개 Requirement를 다음 의미로 복구한다. 이 정의는 이름만 되살리는 것이 아니라 다음 요청과 Gap/검수의 최소 범위다.

| Requirement | 복구 의미 |
|---|---|
| `CPF-ROLE` | 거래의 role/direction/source-target을 표준 Context·Log·Audit에 일관되게 전달 |
| `CPF-OPSDB` | CPF 운영 DB의 가용성, 공유/분리 topology, 장애 시 fail-open/fail-closed 경계 |
| `CPF-LOGFAIL` | DB/원격 로그 실패 시 업무 영향 정책, local spool, 재전송, 유실 탐지 |
| `CPF-SCHED` | 기술 Scheduler 계약, 중복 실행 방지, lease/cluster 실행과 운영 제어 |
| `CMN-CODE` | 고객 업무 공통 코드/참조데이터의 Version, Cache, 유효기간과 조회 계약 |
| `CMN-MSG` | 공통 메시지·다국어·오류 메시지 외부화와 Parameter 조립 |
| `CMN-CALENDAR` | 영업일/휴일/기관 Calendar와 기준일 계산 확장 |
| `CMN-TEMPLATE` | 알림/문서 Template의 Version, 변수 검증, Channel 확장 |
| `ADM-SERVICE` | Service/Endpoint/Instance/Health/Routing Control Plane과 Owner Command 경계 |
| `ADM-LOG` | 운영 Log 조회/정책/Trace Boost/동적 Level과 권한·감사 |
| `ADM-INCIDENT` | Alert→Incident→Runbook→조치→종결의 운영 흐름 |
| `ADM-UX` | 대량 운영화면의 검색, Paging, Download Guard, Accessibility, 오류 UX |
| `SEC-APP` | Secure coding, Input/Output validation, SSRF/Injection/Upload 등 Application Security 기본 통제 |
| `OPS-SELF` | 자동진단/자동복구 조건, 안전한 제한, 반복실패 차단과 감사 |
| `OPS-TOPOLOGY` | Service/Instance/Dependency/Owner 관계를 운영자가 추적 가능한 Topology/Service Catalog |
| `OPS-MAINT` | Maintenance/Drain/Quiesce와 신규 유입 차단, in-flight 종료 정책 |
| `DB-SQL` | SQL/MyBatis 표준, Naming, Query ownership, Vendor-neutral Java와 성능 기본 규칙 |
| `DB-PERF` | Index/Plan/Partition/Statistics/Slow Query/Capacity와 DB 성능 운영 |
| `DB-MULTI` | Multi Datasource, Read Replica, Read/Write routing, failover와 transaction consistency |
| `DATA-LINEAGE` | Source→처리→저장→외부전달의 Data Lineage, 품질/정합성 추적 |
| `DATA-RETENTION` | Retention/Purge/Legal Hold/Archive/개인정보 삭제와 운영 증적 |
| `API-LIMIT` | Rate Limit, Quota, Abuse Detection, Client/Channel별 정책과 429 계약 |
| `DEVEX-COMMENT` | Public API JavaDoc, 중요 복구/동시성 로직 주석, 설정 주석의 유지보수 표준 |
| `RULE-ARCH` | Module/Package/Dependency/Owner 위반의 자동 Architecture Gate |
| `RULE-SEC` | Secret/URL/취약 설정/민감정보 패턴과 보안 정책 자동 Gate |
| `RULE-QUALITY` | Static Analysis, Dependency/License, Dead Code와 품질 Gate |
| `PROD-EDITION` | 제품 Edition/License/Capability Packaging 정책 후보와 런타임 분리 원칙 |
| `PROD-MULTITENANT` | Multi-customer/tenant 격리 후보, Tenant Context/DB/권한/감사 경계 |
| `PROD-PLUGIN` | 고객/기관 Adapter와 Plugin의 SPI, Version/Compatibility/Isolation |
| `PROD-PACKAGE` | 금융/산업군별 선택 Capability Package와 설치/Upgrade 호환 모델 |
| `REQ-GAP` | 새 상용 필수요건을 중복 없이 Intake하고 Requirement ID로 정본화하는 절차 |
| `BAT-CALL-SYNC` | Batch/Worker에서 업무 Domain을 동기 호출할 때 Local/Remote parity와 Header/Timeout |
| `BAT-CALL-ASYNC` | Batch/Worker→Event/Outbox 비동기 호출, Retry/DLQ/Idempotency |
| `BAT-SHARED` | Batch가 온라인/공유 Facade를 재사용할 때 Owner/Transaction/Topology 경계 |

8개 Legacy Alias(`FACADE-LOCAL`, `FACADE-REMOTE`, `CMN-ID`, `CMN-FILE`, `CMN-FIXED`, `ADM-COMP`, `CENTER-ADV`, `API-GATEWAY`)는 Continuity Ledger의 `superseded-by/split-into` 관계로만 추적하고 Canonical Count에는 중복 포함하지 않는다.


## 21.3 이번 전수검수에서 신규 정본화한 Requirement

| Requirement | 신규 정본화 이유와 최소 범위 |
|---|---|
| `ADM-APPROVAL` | 플랫폼 위험조치 승인 Runtime은 일반 `SEC-APPROVAL` 통제만으로는 Owner/Command/Unknown/Break-glass 실행 책임을 추적하기 부족하다. `cpf-admin` Owner, 정책 Version, Operator/Role/Organization Target, ALL/ANY/N_OF_M, SoD, Owner Command, Unknown Recovery, Immutable Audit와 UI/API를 독립 검증한다. |
| `BZA-ORG` | BZA 업무결재가 실제 기업 조직을 해석하려면 조직 Hierarchy, 직원, Position, JobTitle, 다중 Role, 유효기간 Assignment, 겸직/파견/직무대행과 결재 Snapshot을 독립 제품 기능으로 추적해야 한다. 단순 `BZA-APPROVAL` 하위 컬럼 존재만으로 완료 처리하지 않는다. |

## 22. Requirement Catalog

현재 Canonical Requirement Count: **162개**. Legacy Alias 8개는 `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`에서 별도 추적하고 완료율에는 중복 집계하지 않는다.

### Architecture/Core

- `ARCH-MISSION`, `ARCH-MSA`, `ARCH-BOUNDARY`, `ARCH-LAYER`
- `CORE-API`, `CORE-SPI`, `CORE-CONFIG`, `CORE-TESTKIT`
- `CPF-CALL`, `CPF-REGISTRY`, `CPF-ROUTING`, `CPF-HEALTH`
- `CPF-HEADER`, `CPF-CONTEXT`, `CPF-TXID`, `CPF-ROLE`, `CPF-ERROR`, `CPF-VALID`
- `CPF-IDEMP`, `CPF-STATE`, `CPF-LOCK`, `CPF-RESILIENCE`, `CPF-DEADLINE`, `CPF-SCHED`
- `CPF-OPSDB`, `CPF-LOGDB`, `CPF-FILELOG`, `CPF-LOGFAIL`, `CPF-TRACE`, `CPF-MASK`
- `CORE-FIXED`, `CORE-FILE`, `CORE-MESSAGE`

### Common/Data

- `CMN-EXTENSION`, `CMN-SAMPLE-DB`, `CMN-CODE`, `CMN-MSG`, `CMN-CALENDAR`, `CMN-TEMPLATE`
- `DB-OWNERSHIP`, `DB-INSTALL`, `DB-MIGRATION`, `DB-ROLLBACK`, `DB-BACKUP`
- `DB-MULTI-VENDOR`, `DB-SQL`, `DB-PERF`, `DB-MULTI`
- `DATA-LINEAGE`, `DATA-RETENTION`

### Gateway/External/Event

- `GWY-ENTRY`, `GWY-ROUTING`, `GWY-TRUST`, `GWY-RESILIENCE`, `API-LIMIT`
- `EXS-INST`, `EXS-REST`, `EXS-FIXED`, `EXS-SEC`, `EXS-FILE`, `EXS-UNKNOWN`, `EXS-RECON`
- `EVENT-CORE`, `EVENT-OUTBOX`, `EVENT-BROKER`, `EVENT-DLQ`
- `SAGA-CORE`, `SAGA-COMP`, `SAGA-MANUAL`

### Batch/Center-Cut

- `BAT-CORE`, `BAT-JOB`, `BAT-ITEM`, `BAT-EXECUTOR`, `BAT-AGENT`
- `BAT-CALL-SYNC`, `BAT-CALL-ASYNC`, `BAT-SHARED`
- `CENTER-CORE`, `CENTER-RUNNER`, `CENTER-PARAM`, `CENTER-CLAIM`
- `CENTER-RATE`, `CENTER-REPROCESS`, `CENTER-UNKNOWN`, `CENTER-OPS`

### Admin/Security/Operations

- `ADM-AUTH`, `ADM-RBAC`, `ADM-AUDIT`, `ADM-TX`, `ADM-TIMELINE`
- `ADM-SERVICE`, `ADM-LOG`, `ADM-BATCH`, `ADM-CENTER`, `ADM-AGENT`, `ADM-EXS`, `ADM-RECOVERY`, `ADM-INCIDENT`, `ADM-UX`, `ADM-APPROVAL`
- `BZA-BUSINESS`, `BZA-ORG`, `BZA-APPROVAL`, `BZA-SEQUENCE-SAMPLE`
- `SEC-AUTHN`, `SEC-AUTHZ`, `SEC-SECRET`, `SEC-CERT`, `SEC-PRIVACY`
- `SEC-DOWNLOAD`, `SEC-APP`, `SEC-APPROVAL`, `SEC-AUDIT`
- `OPS-METRIC`, `OPS-SLO`, `OPS-ALERT`, `OPS-INCIDENT`, `OPS-RUNBOOK`
- `OPS-SELF`, `OPS-TOPOLOGY`, `OPS-MAINT`, `OPS-CONFIG`, `OPS-DRIFT`, `OPS-CAPACITY`, `OPS-DR`

### Generator/EDU/API/Quality/Productization

- `DEVEX-QUICK`, `DEVEX-CODEGEN`, `DEVEX-COMMENT`, `ONBOARD-DOMAIN`
- `SAMPLE-ACC`, `SAMPLE-MBR`, `SAMPLE-REF`, `SAMPLE-BIZADM`, `SAMPLE-EDU`
- `API-CONTRACT`, `API-PAGING`, `API-ASYNC`, `API-FILE`
- `RULE-ARCH`, `RULE-SEC`, `RULE-QUALITY`
- `TEST-UNIT`, `TEST-CONTRACT`, `TEST-RUNTIME`, `TEST-BROWSER`, `TEST-BROKER`, `TEST-FAULT`, `TEST-EVIDENCE`
- `REL-BUILD`, `REL-DEPLOY`, `REL-MIG`, `REL-COMPAT`
- `DOC-GOV`, `DOC-PRODUCT`
- `PROD-EDITION`, `PROD-MULTITENANT`, `PROD-PLUGIN`, `PROD-PACKAGE`
- `REQ-GOV`, `REQ-REVIEW`, `REQ-CODEX`, `REQ-GAP`

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

## DB / Query / Metadata Generator 동기화 가드레일

DB Schema, Column, Index, FK, Seed, Migration, Runtime Mapper SQL, Vendor SQL 또는 Generated Domain metadata를 변경하는 모든 작업은
`cpf-tools/scripts/sync-database-artifacts.ps1`을 실행하여 canonical source → install/seed Vendor Pack → generated schema manifest를 동기화해야 한다.

- 수작업으로 generated bundle 또는 Vendor Pack만 수정하는 것은 금지한다.
- Index/FK가 존재하지 않는 Column을 참조하면 생성 단계에서 실패해야 한다.
- `database-schema-manifest.json` drift가 있으면 완료 처리하지 않는다.
- Fresh Install은 기존 완전 Schema를 덮어쓰지 않으며, Column drift는 Migration 대상으로 실패한다.
- 모든 Generated Business Domain은 중앙 `domain-template`을 동일하게 사용한다.
- EXS는 고정 `cpf-external`/`exsDB`를 갖지 않는다. 필요한 경우 다른 업무 Domain과 동일하게 `DomainName=external`, `SystemCode=EXS`로 생성한다.
- 이후 Codex 요청서의 DB/SQL/metadata 변경 항목에는 위 generator 동기화와 drift gate를 반드시 완료 조건으로 포함한다.

## R10 작업·생성·검증 일관성 가드레일

다음은 CPF 개발 중 모든 요청서와 작업자가 공통으로 지켜야 하는 제품 정책이다.

- 작업 시작 전에 Final Target, Requirement Continuity, Current Work Request, Decision Log, Continuity State와 최신 Git Diff를 반드시 확인한다.
- EXS는 저장소 정식 Module이 아니라 Generated Domain 검증 대상이다. Baseline에는 `cpf-external`을 남기지 않고 통합 검증에서 `external/EXS` 생성 → verify → remove를 수행한다.
- DB Schema/SQL/Mapper/Metadata/Generated Domain Template 변경은 `sync-database-artifacts.ps1`과 Generated Domain artifact parity를 함께 통과해야 한다. 기존 Generated Domain의 generator-owned DB/MyBatis/SQL은 `sync-generated-domain-artifacts.ps1`로 checksum 보호 하에 동기화한다.
- 특정 Vendor만 수작업으로 맞추지 않는다. 지원 Vendor는 동일 계약으로 갱신하고, 미지원 Vendor는 coverage metadata에서 fail-closed한다.
- 검증은 개발 작업마다 같은 Runtime 시나리오를 반복하지 않고 통합 검증 계획에 누적한다. 최종 검증은 Build/Test/DB/Generator/Runtime/Browser/Multi-instance/Evidence를 한 번의 Runner로 실행할 수 있어야 한다.
- Framework 기술 편의 API와 자료구조는 `cpf-core` Public API가 소유한다. 거대한 Utils Class는 만들지 않고 목적별 `Cpf*` API와 Page/Slice/Cursor 계약을 제공한다. EDU/Generated Domain은 임의 DTO 대신 표준 계약을 우선 사용한다.
- 고객 업무공통 Calendar는 `cpf-common`이 소유하고 ADM은 관리 UI/API, BAT/Scheduler/업무 Domain은 동일 `CmnBusinessCalendar` 계약을 소비한다. BAT 전용 영업일 정본을 별도로 만들지 않는다.
- transactionId/표준 Header는 `cpf-core`가 생성·검증·전파한다. transactionId는 34자리 단일 정본이며 Local/Remote/Async/Batch 로그와 운영 Timeline이 같은 값을 사용한다.
- File Log는 Environment/Domain/Instance와 transactionId 단위 탐색이 가능해야 하고, DB Log는 ADM에서 Module/Instance/transactionId를 교차 조회할 수 있어야 한다. Batch는 Job/Execution/Worker/transactionId를 연결해 관제한다.
- 작업 중 생성된 `logs/`, build output, 임시 ZIP, patch staging, stale report와 사용하지 않는 package는 정식 Repository에 남기지 않는다.
- Source/SQL/API/Test/Guide/Evidence 중 적용 가능한 항목을 같은 작업에서 함께 닫는다. 구현 가능한 항목을 관성적으로 “추후 구현”으로 넘기지 않는다.
- 실행하지 않은 검증, 빈 Adapter, TODO만 있는 화면/Interface, 실제 Consumer가 없는 추상화를 완료라고 기록하는 가짜 구현·가짜 검증을 금지한다.
- Public API, 주요 Class/Method, 복구·동시성·보안 로직은 유지보수 가능한 한글 JavaDoc/주석을 제공한다. Controller는 OpenAPI `@Tag/@Operation`, 요청/응답 의미와 대표 Example을 갖춘다.
- 작업 종료 시 Current Request/Guide/상태/Handover/검증 계획을 최신화하고, 사용자 전달 패치는 CPF Root 상대경로를 보존한 ZIP + APPLY/VERIFY Script로 제공한다.
