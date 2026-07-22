# CPF 차기 Codex 전체 작업 요청서

## 0. 요청 성격

이 요청서는 이전 대화나 Codex 세션을 기억하지 않아도 독립적으로 수행할 수 있는 전체 요청서다.

기준:

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 시작 기준 Commit: `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`
- 직전 WIP Commit: `7251bd996a99ec61d9ea83559578ead0047d5f47`
- 최상위 목표 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검수 패키지: 본 요청서와 동봉된 Audit·Architecture·Guide 제안
- 실행환경: 회사 PC 또는 집 PC의 신규 Clone
- DB 상태: CPF Schema·Table이 없는 빈 MariaDB를 목표로 함

현재 Commit은 최종 완료본이 아니라 대규모 구조·DB 표준화 중간 WIP다. 기존 성공 주장을 승계하지 말고 최신 Source·SQL·Runtime 기준으로 다시 판정한다.


## 0.1 사용자 확인 현재 DB 상태와 필수 재구축 계약

사용자가 2026-07-22 다음 상태를 확인했다.

- 회사 PC의 MariaDB에서는 CPF가 소유하던 Schema와 Table을 이미 삭제했다.
- 집 PC의 MariaDB는 신규·초기화 환경이며 CPF Schema와 Table이 존재하지 않는다.
- 어느 PC에서 작업하더라도 기존 CPF DB Object, 초기 데이터, Flyway 적용 상태 또는 로컬 잔존 데이터에 의존할 수 없다.

따라서 이번 작업은 기존 DB를 보정하는 수준이 아니라 **완전히 빈 MariaDB에서 CPF 제품 DB를 정본으로 재구축하는 작업**이다. 첫 설치는 Reset Script를 선행 조건으로 삼지 말고 Empty Install만으로 성공해야 한다. Reset Script는 이후 개발·테스트 환경의 반복 재현을 위한 별도 제품 산출물로 제공한다.

Codex는 기존 Dump나 현재의 잘못된 SQL을 그대로 복원하지 말고, 최종 Module Ownership·DomainName·3자리 SystemCode·물리 DB 명명 표준을 먼저 확정한 후 다음 전체를 Source와 함께 다시 정비해야 한다.

1. CPF 소유 Schema의 정확한 Allowlist와 Owner
2. Schema명, Table명, Column명, PK·FK·UK·Check·Index·Constraint명
3. 필요한 View·Procedure·Function·Trigger의 존재 이유와 Owner
4. Service User, 최소 권한 Grant, Secret 외부화와 Bootstrap 절차
5. Install·Reset·Provision·Seed·Verify·Migration·Rollback 책임 분리
6. Framework 필수 Meta 초기 데이터와 Version
7. ADM/BZA Menu·Route·Action·Role·Permission·API Permission 초기 데이터
8. Service Registry, Response/Error Code, Log·Masking·Retention Policy 초기 데이터
9. Batch·Scheduler·Agent·Worker·Center-Cut Type·Policy·Catalog 초기 데이터
10. External·Channel·Endpoint Template 중 제품 기본값과 고객 설정의 분리
11. EDU·Reference·Sample·Test Data를 운영 Product Seed와 분리하고 Profile로 통제
12. DataSource, YAML, 환경변수, Mapper, Repository, Entity, DTO, Service, API, Admin UI, Generator Template, Test, Smoke, OpenAPI, JavaDoc, Guide와 Evidence의 물리명 정합성

특히 다음 정책은 선택사항이 아니다.

- `cmnDB`는 생성하되 기본 Table은 DB 연결·Migration·CRUD·검색·Offset·Slice·Keyset·Validation·Transaction을 검증하는 최소 Sample Table 1개만 둔다.
- `cmn_sequence*`는 Common 기본 제품에서 제거한다. 업무 채번은 Framework Core/Common 기능이 아니며 BZA의 선택형 Customization Sample로 제공한다.
- 고정길이 전문의 범용 Contract·Layout·Field·Parser·Writer·Validation·Masking SPI는 `cpf-core`가 소유한다. 기관별 Layout·Mapping·Endpoint·Adapter는 `cpf-external`이 소유한다.
- Batch·Scheduler·Agent·Worker·Center-Cut의 Runtime State는 `cpf-batch`가 소유하며 Physical Schema와 Prefix도 Owner와 일치시킨다.
- 초기 Meta Data에는 실제 비밀번호·Token·개인정보를 넣지 않는다. 관리자 Bootstrap은 환경 Secret 또는 1회성 Credential과 강제 변경 절차를 사용한다.
- 운영 Product Seed와 EDU/Test Data를 한 파일이나 동일 Profile로 섞지 않는다.
- 삭제된 DB라는 이유로 과거 Flyway Migration을 임의 수정하거나 Checksum을 맞춰 쓰지 않는다. 출시·적용 이력 여부를 확인하여 불변 Migration 유지 또는 승인된 단일 Re-baseline 중 하나를 명시적으로 결정한다.
- 새 Schema와 Table이 생성됐다는 사실만으로 완료 처리하지 않는다. Empty Install, Seed 검증, 전체 Module 기동, API·Batch·Center-Cut·ADM/BZA Runtime, 재설치, Upgrade, Rollback Evidence까지 있어야 한다.

DB 접속이나 실행 권한이 없는 영역은 임의로 성공 처리하지 않는다. 정본 Script와 안전한 실행 명령·확인 SQL을 작성하고 상태를 `미검증` 또는 `재확인 필요`로 남긴 뒤, 접속 가능한 환경에서 실제 실행해 마무리한다.

## 1. 절대 가드레일

1. 사용자 명시 승인 전 Commit, Push, Branch 생성 금지.
2. 작업 시작 시 `reset`, `revert`, `checkout -- .`, `clean -fd` 등으로 현재 변경을 폐기하지 않는다.
3. 기존 Applied Flyway Migration을 근거 없이 변경하지 않는다.
4. 실행하지 않은 검증을 성공으로 기록하지 않는다.
5. Static Scan·Swagger 노출·파일 존재만으로 완료 처리하지 않는다.
6. Secret, Password, Token, Private Key, 개인정보 원문을 Source·Log·Evidence에 남기지 않는다.
7. 다른 Application Schema를 Pattern·Wildcard로 삭제하지 않는다.
8. 업무 Module 간 DB 직접 접근 금지.
9. `cpf-core`에서 업무 Module 또는 `cpf-common`을 참조하지 않는다.
10. Sample·EDU를 제품 Runtime 구현으로 대체하지 않는다.
11. 구현하지 않은 요구를 조용히 누락하지 않는다.
12. 반대하거나 더 나은 대안을 선택하면 근거와 Migration 영향을 보고한다.
13. 문서량으로 구현을 대신하지 않는다.
14. 최종 상태값은 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.

## 2. 작업 시작 시 Baseline 고정

가장 먼저 다음을 실행하고 결과를 Evidence에 남긴다.

```powershell
git status --short --branch
git rev-parse HEAD
git rev-parse origin/master
git log -5 --oneline --decorate
git diff --check
java -version
node --version
npm --version
$PSVersionTable
mariadb --version
```

필수:

- HEAD가 `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`인지 확인
- 다른 Commit이면 최신 master Commit을 기준선으로 다시 기록
- Worktree Dirty 여부
- OS, JDK, Node, npm, PowerShell, MariaDB Version
- 시작시각
- DB Host·Port는 마스킹
- Secret은 기록하지 않음

### PowerShell

- Windows에서는 `pwsh` 7을 우선 사용
- Gradle Task와 Script에서 `powershell`을 Hard Coding하지 않음
- 공통 Shell Resolver를 제공
- PowerShell 5.1 fallback은 명시적 호환 시험일 때만 사용
- UTF-8·한글·BOM 회귀 테스트 제공

## 3. 최신 WIP 손상 검수

광범위한 Rename이 있었으므로 구현 전에 전수 검수한다.

### 3.1 Hash·Lockfile·Binary

- `package-lock.json` Integrity
- Checksum 파일
- Flyway SHA-256
- Binary DOCX
- Compressed Artifact
- 인증서·Key 파일
- Base64·Hash 문자열
- JSON/YAML 값 안의 우연한 `pfw`, `xyz` 치환 손상

`PFW -> CPF`, `XYZ -> REF` 같은 추가 Repository 전체 문자열 치환은 금지한다.

### 3.2 Java·Spring

- Package와 Directory 일치
- Import
- Component Scan
- AutoConfiguration Imports
- Bean Name
- `@ConfigurationProperties`
- AOP Pointcut
- Reflection Class Name
- Serialization Type ID
- MyBatis Namespace·Mapper Location
- JNDI Name
- Test Package
- Java Service Loader
- Spring Factories
- OpenAPI operationId

### 3.3 Frontend

- Route
- API Base URL
- TypeScript Import
- Store
- Mock
- Menu Code
- Permission Code
- package-lock Integrity
- Vite Base Path
- Production Environment

### 3.4 SQL·Script

- Schema·Table·Column
- PK·FK·UK·Index·Constraint
- View·Procedure·Trigger
- Seed
- User·Grant
- Environment Variable
- Deploy Inventory
- Shell Script Path
- Gradle Project Path
- Queue·Topic·Cache Namespace
- Log Path

### 완료 조건

Rename 전수 검수 결과를 다음으로 제출한다.

```text
old identifier
new identifier
owner
consumer
migration
compatibility
status
evidence
```

## 4. 공식 제품 구조 확정

### 4.1 공식 Module

| Module | Code | 책임 |
|---|---:|---|
| cpf-core | CPF | 기술 공통 API·SPI·Runtime |
| cpf-common | CMN | 고객사 업무 공통 Extension |
| cpf-admin | ADM | 플랫폼 운영 |
| cpf-biz-admin | BZA | 고객 업무 운영 |
| cpf-batch | BAT | Batch·Scheduler·Agent·Runner·Worker·Center-Cut |
| cpf-gateway | GWY | 외부 진입·라우팅·보안 정책 |
| cpf-member | MBR | 생성형 업무 Reference |
| cpf-account | ACC | 생성형 업무 Reference |
| cpf-reference | REF | EDU·참조 |
| cpf-external | EXS | 기관별 대외연계 |

### 4.2 공식 Package

```text
com.cpf.core
com.cpf.common
com.cpf.admin
com.cpf.bizadmin
com.cpf.batch
com.cpf.gateway
com.cpf.member
com.cpf.account
com.cpf.reference
com.cpf.external
```

### 4.3 의존성

- 업무 -> common -> core
- gateway -> core
- batch -> core + 업무 Public Contract
- admin -> 운영 Contract
- biz-admin -> 업무 Contract
- core -> 업무/common/admin/batch 금지
- 업무 -> core.internal 금지
- 순환 금지

ArchUnit와 Gradle Dependency Gate로 강제한다.

## 5. Repository Root와 문서 정리

### 목표 Root

- README
- 최상위 목표 정본
- Build Wrapper·Settings·Config
- 공식 Module
- 공식 Tool·Deploy·Docs 디렉터리
- 최소 제품 파일

### 이동 대상

다음 역할의 문서를 Root에 두지 않는다.

- Current Work Request
- Gap Matrix
- Stabilization Report
- Evidence Index
- Review Progress
- 날짜별 Report
- 중단 메모

`cpf-docs/work`, `cpf-docs/review`, `cpf-docs/evidence` 등 역할별 정본 위치로 통합한다.

### DOCX

현재 `specs/`의 DOCX 9개는 최신 Source와 불일치 가능성을 검수한다.

- 현재 단계에서 최종 DOCX를 Quality Gate 필수 정본으로 강제하지 않음
- Markdown을 구현 중 정본으로 사용
- 기능·API·SQL·UI가 안정된 마지막 제품화 단계에서 DOCX/PDF 재생성
- Stale DOCX는 Archive 또는 제거
- Root/Specs에 날짜별 중복본 금지

## 6. DB Ownership과 Physical Schema 재설계

### 6.1 현재 Schema Inventory

현재 설치 SQL은 다음을 생성한다.

```text
cpfDB
cmnDB
admDB
refDB
exsDB
mbrDB
bzaDB
accDB
```

`batDB`가 없고 Batch Table이 `cpfDB`에 혼재한다.

### 6.2 목표 Schema

최종 Schema 목록은 실제 Owner와 Consumer 전수 검수 후 확정한다. 권장 목표:

```text
cpfDB   # core runtime
cmnDB   # common minimal sample
admDB   # platform admin
bzaDB   # business admin
batDB   # batch/agent/runner/worker/center-cut
mbrDB   # minimal generated reference
accDB   # minimal generated reference
refDB   # EDU/reference
exsDB   # external domain
```

Gateway는 원칙적으로 Stateless이며 전용 DB가 꼭 필요한지 검토한다. 필요하면 명확한 Runtime State와 Owner가 있을 때만 추가한다.

### 6.3 Table Owner Matrix

모든 Table에 다음을 작성한다.

- physical schema
- table name
- owner module
- source repository/mapper
- service
- API
- UI
- actual consumer
- retention
- PII classification
- audit
- migration
- rollback
- runtime evidence

Owner·Consumer가 없는 Table은 유지하지 않는다.

## 7. `cmnDB` 최소화

### 7.1 최종 설치 목표

`cmnDB`에는 기본적으로 Sample Table 1개만 둔다.

권장 예:

```text
cmn_sample_item
```

필수 기능:

- ID·Unique Key
- Name·Status
- Searchable Field
- Stable Sort
- createdAt/by
- updatedAt/by
- version
- CRUD
- Validation
- Duplicate
- Optimistic Lock
- Transaction Rollback
- List
- Offset Page
- Slice
- Keyset/Cursor

### 7.2 현재 제거·이동 대상

```text
cmn_sequence
cmn_sequence_issue_log
cmn_notification_log
cmn_business_log
cmn_edu_query_item
cmn_fixed_length_layout
cmn_fixed_length_group
cmn_fixed_length_field
cmn_fixed_length_masking_policy
```

이름만 보고 무조건 Drop하지 않는다.

- Source·Mapper·Repository·Service·API·UI Consumer 확인
- 실제 DB가 있으면 Count·FK·View·Procedure·최근 적재 확인
- 빈 DB 환경이면 신규 설치 정본에서 제외
- 기존 데이터가 있으면 Migration 후 Contract 단계 삭제
- DB 확인 불가 시 `미검증` 또는 `재확인 필요`

## 8. 업무 채번 정책

### 8.1 Core/Common에서 제거

- `cmn_sequence*`
- Sequence Service
- Mapper
- Seed
- Admin API
- Guide의 Framework 기본 채번 표현

거래 ID와 UUID/ULID 같은 기술 ID는 Core 유지.

### 8.2 BZA 선택형 Customization Sample

개발·EDU 또는 선택 설치로 제공한다.

기능:

- Rule CRUD
- Prefix/Suffix
- Date Pattern
- Padding
- Start/Increment
- Reset Policy
- Test Issue
- Current State
- Issue History
- Permission
- Approval
- Audit
- Concurrent Issue Reference Test
- Max Value
- Gap Policy
- Rollback/Re-use Policy
- Customer Extension Point

Table명과 Package에 `sample`임을 명확히 한다. 운영 기본 Menu로 자동 활성화하지 않는다.

온라인 MBR/ACC/EXS가 BZA를 필수 호출하지 않는다.

## 9. 고정길이 전문 재소유

### 9.1 cpf-core

Public API·SPI:

- `CpfFixedLengthLayout`
- `CpfFixedLengthField`
- `CpfFixedLengthGroup`
- `CpfFixedLengthParser`
- `CpfFixedLengthWriter`
- `CpfFieldConverter`
- `CpfFieldValidator`
- `CpfMaskingPolicy`
- `CpfEncodingPolicy`
- `CpfFixedLengthException`

필수:

- Character Length와 Byte Length 구분
- UTF-8·EUC-KR 등 Encoding
- Padding
- Alignment
- Numeric/Date/Decimal
- Header/Body/Trailer
- Repeat Group
- Conditional Field
- Version
- Streaming
- Max Size
- Error Offset
- Field Path
- Masked Error
- Thread Safety

### 9.2 cpf-external

- 기관별 Layout
- Institution Mapping
- Endpoint
- Auth
- Request/Response Adapter
- Version Routing
- Institution Error
- Retry
- Result Query
- Unknown Result
- Reconciliation

### 9.3 DB 저장

동적 Layout 관리가 실제 제품 요구로 확인되면 Core-owned Table을 설계한다. 그렇지 않으면 코드·YAML·External Domain Config로 제공하고 불필요한 DB Table을 만들지 않는다.

### 9.4 ADM

조회·등록·Version·승인·감사 화면은 Owner API를 사용한다. DB 직접 수정 금지.

## 10. File·Attachment·Compression·Messaging Ownership

### cpf-core

- 범용 Contract·SPI
- Streaming
- Checksum
- Chunk
- Compression
- Zip Bomb 방지
- Attachment Metadata
- File Transfer Port
- Broker Envelope
- Correlation
- Outbox/Inbox/DLQ Contract
- Idempotency
- Masking
- Audit Hook

### cpf-external

- SFTP/기관별 Adapter
- 기관 Path Alias
- 기관 File Layout
- Institution Callback
- External Reconciliation

### cpf-common

고객사 공통 정책 Adapter만 둔다. Generic Transfer Engine과 Remote Command Engine을 소유하지 않는다.

현재 `fle`, `message`, `mqe`, `msg` Package는 Consumer·Owner 기준으로 통합·이동·삭제한다.

## 11. Batch·Scheduler·Agent·Worker Physical Ownership

### 11.1 cpf-batch 책임

- Job Definition
- Step
- Schedule
- Calendar
- Dependency
- Checkpoint
- Agent Registry
- Agent Pool
- Agent Capability
- Runner
- Worker
- Queue·Partition
- Claim·Lease
- Fencing
- Operation Audit
- Center-Cut
- File Watch·Process
- Shell Registry

### 11.2 Table

Batch State를 `cpfDB`에 혼합하지 않는다. `batDB`와 `bat_*` 정본을 권장한다.

Core 거래 로그가 Batch 실행 ID를 참조할 수는 있지만, Batch Table을 Core가 소유하지 않는다.

### 11.3 실행 유형

- JAVA_JOB
- SHELL_JOB
- FILE_WATCH
- FILE_PROCESS
- FILE_TRANSFER
- CENTER_CUT
- COMMAND_JOB
- COMPOSITE_JOB

각 Executor SPI는 다음을 제공한다.

- Claim·Lease
- Duplicate Prevention
- Parameter Snapshot
- Timeout
- Cancel
- Restart
- Attempt
- Checkpoint
- transactionGlobalId
- Standard Error
- Audit·Masking
- Unknown Result
- Agent Failure Recovery
- ADM Query·Control

## 12. CenterCutRunner 완결

### 12.1 공식 책임

`cpf-batch`는 Batch Agent와 구분되는 `CenterCutRunner` Runtime 책임을 제공한다.

- Job Claim
- Immutable Parameter 복원
- Policy Snapshot 복원
- Target Item 생성
- Worker 분배
- 집계
- Pause·Resume·Cancel
- Retry
- Unknown Result Recovery
- Multi-instance Duplicate Prevention
- Speed·Backpressure
- Deployment Neutral

작은 구성에서는 Batch Agent 내부, 큰 구성에서는 별도 Process로 배포 가능해야 한다.

### 12.2 Job Parameter

```text
request_payload           LONGTEXT 또는 JSON
request_payload_size      BIGINT
request_payload_hash      VARCHAR
parameter_schema_version  VARCHAR
content_type              VARCHAR
encoding                  VARCHAR
encrypted_yn              CHAR
masked_summary            TEXT
```

- 원본 Immutable
- 수정은 별도 Version·Audit
- Raw Payload Log 금지
- Size Limit
- Schema Validation
- Encryption
- JDBC Packet 검증

### 12.3 상태

```text
REGISTERED
WAITING_APPROVAL
SCHEDULED
QUEUED
PREPARING
READY
RUNNING
PAUSING
PAUSED
STOPPING
CANCELLED
PARTIAL_COMPLETED
COMPLETED
FAILED
RECOVERY_REQUIRED
UNKNOWN
COMPENSATING
COMPENSATED
```

### 12.4 Item·Attempt

- Job ID
- Item ID
- Sequence
- Target Key
- State
- Attempt ID
- Worker
- Claim
- Lease
- Fencing Token
- parent/child transactionGlobalId
- Start/End
- Result
- Error
- Failure Stage
- Retry
- Next Execution
- Compensation
- Optimistic Version

Attempt를 덮어쓰지 않고 이력으로 보존한다.

### 12.5 Parameter Equality

다음을 Hash와 Runtime Test로 증명한다.

```text
요청 Parameter
= DB 저장 원본
= Runner 복원
= Worker/Handler 전달
= 허용된 거래 Log Summary
```

## 13. Rate Limit·Concurrency·Error Policy

### 처리 방식

- Sync/Async
- Pre-generation/Streaming
- Sequential/Parallel
- One-by-one/Chunk/Page
- Key Ordered
- Partition Ordered
- Unordered

### 속도

- TPS/RPS
- Delay
- Global Cluster Limit
- Per Instance
- Institution/API/Handler
- Max Worker
- Max Inflight
- Backpressure
- Runtime Change
- Restart Restore

3 Worker에서 Global 100 TPS이면 총합 약 100이어야 한다.

### 오류

- Fail Fast
- Continue
- Retry then Stop
- Retry then Continue
- Skip
- Threshold Stop
- Pause for Approval
- Compensate

정책은 실행별 Immutable Snapshot으로 저장한다.

## 14. Primary·Secondary Agent

### Mode

- Automatic
- Primary Priority
- Fixed
- Distributed
- Zone Priority

### Definition

- Primary Agent/Pool
- Ordered Secondary Agent/Pool
- Capability
- OS·Java·Shell
- Security Zone
- Path Alias
- Slots
- Failover
- Failback
- Fixed Reason

### Failure Classification

- Disconnected
- Lease Expired
- Drain/Pause
- No Slot
- Capability Mismatch
- Path Unavailable
- Artifact Mismatch
- Pre-execution Failure
- Mid-execution Failure
- Unknown Result

Pre-execution은 안전 Failover 가능. Mid-execution은 Blind Replay 금지.

### Duplicate Prevention

```text
Claim + Lease + Fencing Token + Attempt ID + Idempotency Key
```

Recovered Primary의 Late Update를 Fencing으로 거부한다.

### File Failover

공유 Durable Storage를 Secondary가 볼 수 있을 때만 자동 Failover한다. Raw Absolute Path 대신 Path Alias를 사용한다.

## 15. 실패 건만 재처리

### Scope

- All Failed
- Error Code
- Failure Stage
- Handler
- Domain
- Key
- Time
- Attempt
- Unknown
- Recovery Required
- Selected Items

성공·처리중·보상완료는 제외한다.

### History

Original Row를 Reset하지 않는다.

- Original Job/Item/Attempt
- Reprocess Job/Item/Attempt
- Requester
- Approver
- Reason
- Parameter Hash
- Override Diff
- transactionGlobalId
- Agent/Runner/Worker
- Result

동시 Double-click은 하나의 Active Reprocess만 허용한다.

### Unknown Result

```text
Unknown 탐지
→ 외부 결과 조회
→ 성공/실패/여전히 Unknown
→ 실패 확정만 재처리
→ 판단 불가 시 승인/보상
```

## 16. General Batch 등록·운영

### Job Definition

- Name
- Owner
- SystemCode
- Handler
- Version
- Activation
- Schedule
- Calendar
- Holiday
- Window
- Misfire
- Dependency
- Step
- Chunk/Page
- Transaction
- Retry/Skip/Rollback
- Timeout
- Checkpoint
- Agent Pool
- Priority
- Parameter Schema
- Approval
- Notification

### Shell

- 승인된 Script Registry ID
- Version
- Hash
- Shell
- Work Dir Alias
- Argument Schema
- Environment Allowlist
- Account
- Timeout
- Exit Code
- Output Limit
- Masking
- Termination
- Approval

Admin에서 임의 Command/Path 입력 금지.

### File Watch/Process

- Path Alias
- Pattern
- Temp/Done Extension
- Stable Size
- Duplicate Key
- Poll/Event
- Timeout
- Single Claim
- Encoding
- Compression
- Encryption
- Format/Layout
- Header/Trailer
- Count/Amount
- Bad Record
- Archive
- Retention
- Hash/Dedup

## 17. ADM 운영 구조

### Menu

```text
플랫폼 운영
├─ 운영 대시보드
├─ 온라인 운영
├─ Batch 관리
├─ Center-Cut 관리
├─ 실행 인프라
├─ 정책 관리
└─ 감사·승인
```

반드시 제공:

- 거래 그룹·상세·Timeline
- Header·Segment·External Call
- Batch Job·Instance·Step·Checkpoint
- Center-Cut Job·Item·Attempt
- Agent·Runner·Worker·Lease
- Queue·Partition
- Failover
- Pause·Resume·Cancel
- Speed Change
- Failed-only Reprocess
- Unknown Decision
- Compensation
- Approval
- Audit

상태를 DB에서 직접 Update하지 않고 Domain Command API를 호출한다.

## 18. BZA 구조

### Owner

- 고객 업무 관리자
- 업무 Menu·Permission
- 업무 승인
- 업무 조회
- 고객 Custom Extension
- 선택형 Reference Sample

### 금지

- CPF Runtime 내부 제어
- 다른 업무 Domain 원장 소유
- `bza_customer/product/order`를 기본 제품 업무 원장으로 제공
- 온라인 거래가 BZA에 필수 의존

### 채번 Sample

제10장의 정책으로 선택형 제공한다.

## 19. ADM/BZA Vue 구조와 독립 배포

### Source

기능별:

```text
features/<feature>/
├─ pages/
├─ components/
├─ dialogs/
├─ api/
├─ model/
├─ composables/
└─ routes/
```

- 목록 Page 1 File
- 상세 Page 1 File
- 복잡한 Form 분리
- 의미 있는 Dialog 분리
- Giant Vue File 금지
- Shared Dumping 금지
- ADM/BZA API·Route·Permission 분리
- Lazy Loading
- Duplicate Type/API 제거

### Build

```text
ADM Vue → adm static artifact
BZA Vue → bza static artifact
Java API → independent JAR/WAR
```

### Web Runtime

- Nginx/Apache
- `/adm`, `/bza` Base Path
- SPA Fallback
- Deep Link
- Static Cache Hash
- Cache-Control
- Compression
- MIME
- Security Header
- CSP
- Source Map Policy
- External API Base
- No Hard-coded dev/prod URL
- UI/API Version Compatibility
- Independent Rollback

### Browser E2E

- Login
- Role Menu
- Direct URL Block
- Search·Detail
- Approval
- Control
- Timeout
- Session Expiry
- Paging
- Error
- Refresh/Deep Link
- Simultaneous ADM/BZA
- UI-only Redeploy
- API-only Redeploy
- Rollback

## 20. cpf-core Public API와 Developer Experience

### 20.1 Package

```text
com.cpf.core.api.*
com.cpf.core.spi.*
com.cpf.core.internal.*
```

Public API가 `common`이라는 거대 Package 아래 무질서하게 노출되지 않도록 정리한다.

### 20.2 Utility

- `CpfDates`
- `CpfTimes`
- `CpfStrings`
- `CpfNumbers`
- `CpfDecimals`
- `CpfLists`
- `CpfMaps`
- `CpfIds`
- `CpfFiles`
- `CpfHashes`
- `CpfValidation`
- `CpfPages`
- `CpfHeaders`
- `CpfAttributes`
- `CpfClock`

Wrapper는 Safe Default·CPF Standard·Validation·Error·Unicode/Byte Awareness가 있을 때만 제공한다.

### 20.3 Stateful Service

Static Utility 금지 대상:

- Web Client
- Auth/Token
- Encryption
- Masking Policy
- Audit Persistence
- Persistent Sequence
- Retry/Circuit Breaker
- File Transfer
- DB
- Business Calendar
- Clock
- ObjectMapper

### 20.4 Progressive Config

```text
CPF Safe Default
→ Customer Common Property
→ Named Profile
→ Operation/API Config
→ Call-site Override
```

Effective Value와 Source를 운영에서 조회 가능하게 한다.

### 20.5 Web Client SPI

- HeaderContributor
- AuthenticationProvider
- EndpointResolver
- RequestCustomizer
- Codec
- ResponseDecoder
- ErrorMapper
- RetryDecider
- IdempotencyKeyProvider
- UnknownResultResolver
- MaskingPolicy
- AuditContributor
- TraceContributor
- ClientObserver

## 21. Collection·Paging·Cursor 표준

### Types

- `ListResult<T>`
- `PageRequest`
- `PageResult<T>`
- `SliceResult<T>`
- `CursorRequest`
- `CursorResult<T>`
- `AsyncAcceptedResult`
- `OperationStatusResult`
- `BulkResult<T>`
- `ValidationResult`
- `CpfAttributes`

### List Limit

- Framework Default/Max/Hard Max
- Query는 Max+1 조회
- `CPF-COLLECTION-LIMIT-EXCEEDED`
- Request Collection Count·Length·Depth·Payload Limit
- Client `ignoreLimit=true` 금지
- 예외는 Server Profile·Permission·Reason·Audit
- 대량은 Cursor·Stream·Async Export·Batch

### Offset

- page/size/sort
- Max Page
- Deep Offset
- Count Timeout
- Sort Allowlist
- Stable Tie-breaker
- Null Ordering

### Cursor

- Compound Key
- Stable Tie-breaker
- Next/Previous
- Signed Cursor
- Version·Expiry
- Search/Sort Binding
- DB ID·PII 은닉
- Concurrent Change 안정성

### Search

- Field/Operator Allowlist
- Date Range Max
- IN Max
- Wildcard Restriction
- Raw SQL/Column 노출 금지

## 22. API·Error·Context 표준

### Response

- transactionGlobalId
- code
- message
- data
- meta

Streaming/File/Multipart/SSE/Async/Batch/Center-Cut은 별도 Contract.

### Error

- Code
- Message Parameters
- User Message
- Operator Message
- Retryable
- Unknown Result
- Compensation Required
- Field Errors
- Masked Detail
- Cause Chain ID

### Context

- transaction
- trace
- segment
- channel
- caller
- user
- domain
- SystemCode
- host
- instance
- agent
- runner
- worker
- locale
- timezone
- deadline
- parent-child

## 23. Generator 완결

### Input

- DomainName
- SystemCode
- Base Package
- DB Vendor
- Capability

### Collision

- Reserved Code
- Existing Module
- Package
- Config Prefix
- Route
- Port
- Schema/Table Prefix
- Queue/Topic
- Cache
- Menu
- Manifest

### Output

- `cpf-<domain>`
- `com.cpf.<domain>`
- Build
- Config
- SQL
- Migration
- Test
- EDU
- OpenAPI
- ADM/BZA Registration
- Deploy
- Manifest

### Lifecycle

```text
Create
→ Verify
→ DB Init
→ Build
→ Runtime
→ Safe Delete
→ Residual Scan
→ Regenerate
→ Hash/Semantic Parity
```

### Provided Domain

MBR·ACC·EXS는 최소 거래 검증 Table만 제공한다. 추정성 전체 업무 Schema 금지.

### Official Sample Migration

CPF Public API가 제공되면 모든 공식 Sample·Generator Template·EDU·JavaDoc·OpenAPI·Test가 그 API를 사용하게 한다. 직접 JDK 구현과 Custom Page DTO가 남으면 완료가 아니다.

## 24. Gateway

- External Entry
- Route
- Health
- Client-side Load Balance
- Timeout
- Circuit Breaker
- Failover
- Auth Policy
- Header Trust Boundary
- Unknown Result Policy
- Instance Registry
- Manual Block/Resume
- Audit
- Metrics

내부 업무 간 호출은 Gateway 재경유 금지.

## 25. External Runtime

- Endpoint Policy
- Institution
- Auth Profile
- Token Rotation
- Timeout
- Retry
- Circuit Breaker
- Idempotency
- Unknown Result
- Result Query
- Reconciliation
- Fixed Length
- File
- Callback
- Messaging
- Masking
- Audit
- Operations

정상·중복·충돌·Timeout·기관 처리 후 응답유실·재조회·수동확정·보상을 Runtime E2E로 검증한다.

## 26. Security·Audit

- AuthN/AuthZ
- RBAC
- API Permission
- Button Permission
- Direct URL/API Block
- MFA
- IP Allowlist
- Session
- Cookie Security
- CSRF
- CSP
- XSS
- SSRF
- Upload
- Download
- PII Classification
- Masking
- Dual Control
- Secret Externalization
- Rotation
- mTLS
- OAuth/JWT/API Key
- Audit Immutability
- Retention
- Secret Scan
- Dependency/SBOM/License

ADM/BZA 화면 숨김만으로 권한 완료 처리 금지.

## 27. SQL Install·Reset·Provision·Migration

### Directory

```text
specs/sql/
├─ install/
├─ reset/
├─ provision/
├─ migration/flyway/
├─ migration/rollback/
├─ seed/
└─ verify/
```

실제 위치는 Repository 역할과 기존 Script를 고려해 최적화할 수 있으나 책임은 분리한다.

### Install

- Empty DB
- Non-destructive
- Schema Create
- Table Create
- Index/Constraint
- Seed
- Idempotent
- No Drop

### Reset

- Dev/Test only
- Exact Allowlist
- Dry Run default
- Explicit Apply
- Target Server/Schema Display
- Other Schema Protection
- No Pattern/Wildcard
- Backup Warning
- Audit Log

### Provision

- Separate Migration/App/Read-only User
- Per Service Credential
- Minimum Grant
- Password External Input
- No Shared Password Assumption
- No Secret Evidence

### Upgrade

- Immutable Flyway
- Baseline Decision
- Checksum
- Expand/Contract
- Backfill
- Lock/Duration
- Multi-version Compatibility

### Rollback

- Precondition
- Data Loss Warning
- Backup Point
- Application Compatibility
- Forward-fix Decision
- Validation

## 28. Clean MariaDB 전수 검증

현재 회사 PC와 집 PC 모두 CPF 관점에서 빈 DB 상태다. 회사 PC의 기존 CPF Schema·Table은 사용자가 이미 삭제했고, 집 PC는 신규 MariaDB다. Codex는 기존 Object가 있다고 가정하지 말고 정본 Install·Provision·Seed Script로 모든 CPF Schema, Table, Constraint, Index, Meta 초기 데이터와 권한을 처음부터 생성한다.

첫 실행은 Reset 없이 Empty Install만으로 성공해야 한다. 반복 검증 시에만 별도의 안전한 Reset을 사용한다. Schema·Table 생성 후 Source DataSource, Mapper, Repository, API, ADM/BZA, Generator와 Smoke Query가 동일한 최종 물리명을 사용해야 한다.

### 순서

1. Reset Dry Run
2. Exact Allowlist 확인
3. User 승인 조건 확인
4. CPF Schema만 초기화
5. User Provision
6. Empty Install
7. Seed
8. Smoke
9. 전체 Backend Build
10. 전체 Module Startup
11. OpenAPI
12. Sample CRUD
13. Local/Remote
14. Gateway
15. Batch
16. Center-Cut
17. External
18. ADM/BZA
19. Stop
20. Reinstall
21. Upgrade
22. Rollback
23. Restore/Forward-fix

### 필수 초기 Meta Data 검증

제품 기본 Seed는 최소한 다음 범주를 Owner별로 분리하고, 실제 Consumer와 조회 API를 확인한다.

- SystemCode·Module·Domain Registry
- Schema Version과 Migration Baseline
- 표준 Response·Error Code와 Message
- ADM/BZA Menu·Route·Action·Role·Permission·API Permission
- Service·Instance·Endpoint Registry의 정적 Bootstrap 항목
- Log·Masking·Retention·Audit·Download 기본 Policy
- Batch Job Type, Executor Type, Agent Capability, Center-Cut Policy Catalog
- 운영 승인·위험 작업 권한의 기본 구조
- Channel·External Adapter Template 중 Secret이 아닌 제품 기본값

다음은 운영 Product Seed와 분리한다.

- EDU 실습 데이터
- Reference Sample 데이터
- Generator 검증 데이터
- Smoke·Integration Test Fixture
- BZA 업무 채번 Customization Sample

각 Seed는 반복 실행 시 중복·오염 없이 결과가 결정적이어야 하며, Code·Key 충돌·삭제·변경 정책과 Upgrade·Rollback을 검증한다.

### DB Evidence

- Schema List
- Table/Index/Constraint Count
- Owner Matrix
- Seed Count
- Charset/Collation/Timezone
- User Grants
- Row Count
- Migration History
- No Other Schema Damage
- Reinstall Result

## 29. 필수 Build·Runtime·Browser 검증

### Build

```text
clean compile
unit test
integration test
bootJar
bootWar
aggregate JavaDoc
frontend npm ci
lint
typecheck
unit
production build
```

### Runtime

- All Module Health
- OpenAPI HTTP 200
- Standard Header
- Transaction ID
- File/DB Log
- Local/Remote
- Gateway Failover
- Retry
- Timeout
- Unknown Result
- Broker
- File
- Batch
- Two Worker
- Center-Cut
- Agent Failover
- Recovery

### Browser

- ADM
- BZA
- Role
- Direct URL/API
- Search
- Detail
- Approval
- Batch
- Center-Cut
- Agent
- Reprocess
- Unknown
- Sequence Sample
- Session
- Deep Link
- Split Deploy

### Deployment

- Java WAS/JAR
- External WAS WAR
- ADM Web Server
- BZA Web Server
- Independent Version
- Rolling
- Rollback
- Config Externalization
- Artifact Checksum

## 30. Evidence 기준

각 Evidence:

- Requirement ID
- Baseline Commit
- Final Commit
- Command
- Working Directory
- OS
- Java
- Node/npm
- PowerShell
- DB
- Profile
- Start/End
- Exit Code
- Result
- Raw Log
- Sanitized
- Secret Review
- Source/API/SQL/Test Link
- Stale Flag

Static Inventory는 보조 Evidence로만 분류한다.

## 31. Guide와 README 현행화

구현 완료 시 다음을 실제 이름·Property·API·Screen·SQL에 맞춰 갱신한다.

- Architecture
- Developer
- Generator
- API
- Security
- Installation
- Deployment
- Operator
- Recovery
- Migration
- Batch/Center-Cut/Agent
- External/Fixed-Length/File
- ADM/BZA Frontend
- EDU
- README

README에는 진행률·Commit·Gap·검수결과를 넣지 않는다.

## 32. 관리 문서 갱신

다음은 실제 최신 상태로 갱신한다.

- Current Work Request
- Final Target Requirements
- Gap Matrix
- Review/Completion Guide
- Stabilization Report
- Evidence Index
- Feature Matrix
- Sample Coverage Matrix

동일 역할 문서를 추가 생성하지 않고 기존 정본 위치에 통합한다.

## 33. 완료 금지 조건

다음 중 하나라도 있으면 전체 완료 선언 금지.

- Dirty Worktree 원인 미확인
- Build 실패
- Frontend 실패
- 빈 DB 설치 미수행
- Migration Checksum 불명
- Runtime 미실행
- Browser 미실행
- PFW/XYZ 활성 잔존
- cmnDB 1 Sample 정책 미충족
- Fixed-Length Ownership 미충족
- Sequence Common 잔존
- Batch Table Owner 혼재
- CenterCutRunner E2E 없음
- Agent Failover/Fencing 없음
- Failed-only Reprocess 없음
- Unknown Result Blind Replay
- ADM/BZA 서버 권한 없음
- Guide와 Source 불일치
- Stale Evidence
- Secret 노출
- Root Garbage
- 실제 Deploy/Rollback 미검증

## 34. 최종 결과 보고

요구사항별 표:

```text
ID
상태
Source
API
SQL
Test
Runtime
UI
Evidence
Gap
다음 조치
```

별도 요약:

- 변경 파일
- 삭제 파일
- 이동 파일
- DB Object
- Migration
- 실행 명령
- 실패와 해결
- 미검증
- 사용자 수동 확인
- Commit/Push 미수행 확인

## 35. Git 종료 규칙

모든 작업과 검증이 끝나도 Commit·Push하지 않는다.  
사용자에게 상세 결과를 보고하고 승인을 기다린다.
