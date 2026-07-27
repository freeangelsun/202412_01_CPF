# CPF Codex 1차 전수 검증·오류수정·보완개발 요청서

> 권장 Repository 위치  
> `cpf-docs/work/current/CPF_CODEX_1ST_FULL_VALIDATION_AND_REPAIR_REQUEST_20260726.md`

---

# 1. 작업 목적

이번 작업은 **단순 검수, 문제 목록 작성, 정적 검색, 리포트 작성만 하는 작업이 아니다.**

지금까지 ChatGPT/GPT 및 이전 Codex 세션에서 누적 개발된 CPF 전체 구현을 최신 `master` 기준으로 다시 확인하고, 실제 개발환경에서 가능한 검증을 수행한다.

검수 중 다음이 발견되면 보고만 하지 말고 **표준·요건·Architecture를 기준으로 직접 수정·보완 개발한 뒤 동일 검증을 다시 실행하여 통과시킨다.**

- Build/Compile 오류
- Test 실패
- Runtime 오류
- SQL/Migration 오류
- Generator Drift
- API/Consumer 연결 누락
- Frontend Route/API/Permission 오류
- 부분 구현
- 미구현
- 잘못된 Module Ownership
- Public API/SPI/Internal 경계 위반
- 실제 Consumer 없는 Interface/SPI
- Legacy/신규 구현 중복
- Dead Code/Stale Code
- DB Canonical Source와 Vendor SQL 불일치
- Generated Domain과 Framework 표준 불일치
- 다중 인스턴스 Race
- Lock/Lease/Fencing 오류
- Idempotency 오류
- UNKNOWN_RESULT 오처리
- Retry/Recovery/Compensation 오류
- Security/AuthN/AuthZ/Audit/Masking 문제
- 위험한 운영조치의 승인/사유/Audit 누락
- Remote Agent/Deployment 오류
- Release/Artifact/Compatibility 오류
- Repository Root Hygiene 문제
- 문서/Evidence와 실제 Git 불일치
- 과거 QA에서 수정 완료라고 했으나 최신 Source에 남아 있는 결함
- CPF 최종 제품 목표상 필수지만 빠져 있는 상용 기능

이번 작업은 다음 사이클을 반복한다.

```text
최신 Git 확인
→ 최상위 Requirement/정본 확인
→ 실제 Source/SQL/API/Test/Frontend/Script 대조
→ 검증 실행
→ 오류/Gap 발견
→ 원인 분석
→ 올바른 Owner/표준 구조로 수정
→ Consumer/Generator/DB/Test/문서/Evidence 동기화
→ 재검증
→ PASS 확인
```

---

# 2. 작업 완료 원칙

이번 검수의 목표는 **“검수했다”가 아니라 “검수 가능한 범위를 통과시켰다”**이다.

작업 종료 시점에는 다음 조건을 만족시킨다.

1. 현재 PC/Repository 환경에서 실행 가능한 검증은 모두 실행한다.
2. 실행 가능한 항목을 `미검증`, `추후 확인` 상태로 남기지 않는다.
3. 검증 중 오류가 발생하면 가능한 범위에서 직접 수정한다.
4. 수정 후 동일 검증을 다시 수행하여 PASS를 확인한다.
5. 단순 File 존재/Static grep만으로 완료 판정하지 않는다.
6. Source와 Requirement가 어긋나면 Source를 제품 목표에 맞게 수정한다.
7. 기존 성공 기능을 회귀시키지 않는다.
8. 새 기능만 보고 기존 전체 기능을 무시하지 않는다.
9. 외부 Infra/제품이 실제 환경에 없어 실행할 수 없는 E2E만 예외적으로 실행 미검증이 허용된다.
10. 외부 Infra 부재 항목도 **Source/Config/Contract/Test-double 수준의 검수는 완료**해야 한다.
11. 실행하지 못한 외부 E2E는 정확한 환경조건·실행명령·예상결과를 Handover에 남긴다.
12. “시간 부족”, “Credit 부족”만을 이유로 확인 가능한 오류를 그대로 방치하지 않는다.

즉:

```text
검수 가능한 범위 = 검수 완료 + 오류 수정 + 재검증 PASS
외부 환경 부재 범위 = Source 검수 완료 + 실행 미검증 사유/후속 명령 명확화
```

---

# 3. Repository 기준

Repository:

`https://github.com/freeangelsun/202412_01_CPF`

Branch:

`master`

2026-07-26 요청서 작성 시 다시 확인한 최신 원격 기준:

```text
SHA     : e725ed3f1bc203e28ff6f06c62a69583358d3b6a
Commit  : 20260726_05
```

단, Codex 작업 시작 시 반드시 다시 확인한다.

```powershell
git fetch --prune origin
git status --short
git branch --show-current
git rev-parse HEAD
git rev-parse origin/master
git log -1 --oneline
git log origin/master -1 --oneline
```

작업 기준 SHA를 Evidence와 Handover에 기록한다.

---

# 4. Working Tree 보호

Working Tree가 Dirty이면 절대 임의 초기화하지 않는다.

금지:

```text
git reset --hard
git clean -fd
git checkout .
git restore .
```

먼저 확인:

```powershell
git status
git diff --stat
git diff --name-status
```

사용자/GPT의 미Commit 작업일 수 있으므로 반드시 보호한다.

Local이 원격보다 뒤처지고 Working Tree가 Clean일 때만 필요하면:

```powershell
git pull --ff-only origin master
```

를 사용한다.

---

# 5. Git 변경 권한

이번 작업에서 Codex는 필요하면 다음을 직접 수정할 수 있다.

- Java
- Gradle
- SQL
- Migration/Rollback
- Generator
- Test
- Frontend
- Config
- Script
- Deployment
- Guide
- Request
- Handover
- Review
- Evidence

그러나 사용자의 명시적 승인 없이는 다음을 하지 않는다.

- Commit
- Push
- Branch 생성
- Tag 생성
- Release 생성

최종 변경은 Working Tree에 남긴다.

---

# 6. 최상위 제품 목표

CPF 정식 명칭은:

**Core Platform Framework**

CPF는 단순 공통 Library나 Sample 프로젝트가 아니다.

금융권을 포함한 다양한 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화할 수 있는 **Business Platform 품질의 상용 Framework**가 목표다.

최상위 정본:

`cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

최종 목표는 최소 다음 범위를 포함한다.

- MSA + Modular Monolith
- Same-JVM / Separate-WAS Service Call
- Multi-instance / Failover / Retry / Recovery
- Distributed Trace
- Security / Authentication / Authorization
- Audit / Masking
- Idempotency
- Async / DLQ / Reprocessing
- Compensation
- UNKNOWN_RESULT Recovery
- External Integration
- Messaging
- File / Attachment / Protocol
- Batch
- Scheduler
- Worker
- Agent
- Center-Cut
- ADM/BZA Operation Control
- Approval / Audit
- Generated Domain
- EDU
- OpenAPI
- JavaDoc
- Test
- Install
- Migration
- Upgrade
- Rollback
- Deployment
- Release
- Evidence
- Repository Governance

단기 구현 편의보다 **장기 제품 구조·확장성·운영성·검증 가능성**을 우선한다.

---

# 7. 작업 시작 전에 반드시 읽을 정본

개별 Source부터 손대지 말고 먼저 다음을 확인한다.

최우선:

```text
cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md
```

그리고 실제 존재하는 최신 역할별 정본:

```text
cpf-docs/work/current/
cpf-docs/work/state/
cpf-docs/work/review/
cpf-docs/evidence/
cpf-docs/architecture/
cpf-docs/governance/
cpf-docs/guides/
specs/
```

특히 이름이 존재하면 다음을 반드시 확인한다.

```text
CPF_CURRENT_WORK_REQUEST.md
CPF_NEXT_WORK_REQUEST.md
CPF_INTEGRATED_VERIFICATION_PLAN.md
CPF_CODEX_FINAL_VALIDATION_AND_FIX_REQUEST.md
CPF_FINAL_COMPLETION_HANDOVER.md
CPF_FINAL_IMPLEMENTATION_REPORT.md
CPF_QA_CLOSURE_20260726.md
CPF_MASTER_FULL_DEFECT_AUDIT_20260726.md
CPF_GAP_MATRIX.md
CPF_STABILIZATION_REPORT.md
기능_구현_매트릭스.html
sample-coverage-matrix.md
```

파일명/위치가 변경됐으면 동일 역할의 최신 정본을 찾는다.

---

# 8. 문서보다 실제 Git이 우선

과거 GPT/Codex 문서의 다음 표현을 그대로 신뢰하지 않는다.

- 완료
- PASS
- 구현 완료
- QA Closure
- Final
- Evidence 존재

판정 기준은 실제 최신:

```text
Source
SQL
Migration
API
Test
Runtime
Config
Frontend
Generator
Deployment Script
Release Script
Evidence
```

이다.

문서의 기준 SHA가 최신 SHA와 다르면 Stale 문서다.

이번 작업 종료 전 관련 Current/Handover/Review/Evidence를 실제 최신 상태로 수정한다.

---

# 9. DB / SQL / Generator 최우선 강제 정책

**이 항목은 매우 중요하다.**

앞으로 CPF에서 DB Query, Schema, Metadata를 수정할 때는 반드시 **Generator와 Canonical Source/Metadata를 먼저 수정한 뒤 Vendor 산출물에 반영**한다.

## 9.1 DB 변경의 정식 순서

반드시 다음 흐름을 따른다.

```text
Requirement / Data Model
→ Canonical Schema / Metadata
→ Generator / Domain Template
→ Generated Domain 산출 기준
→ Vendor Source SQL
→ Migration
→ Install
→ Upgrade
→ Rollback
→ Seed
→ Verify
→ Test
→ Evidence
```

금지:

```text
MariaDB SQL만 직접 수정
→ Generator/Metadata는 기존 상태로 방치
```

금지:

```text
Migration만 수정
→ Fresh Install SQL은 과거 구조
```

금지:

```text
Vendor SQL 수정
→ Generated Domain Template은 다른 Schema
```

---

# 10. Generator/Metadata 선행 대상

다음이 변경되면 반드시 Generator/Canonical Metadata 영향을 먼저 확인한다.

- Table
- Column
- PK
- FK
- Index
- Unique
- Sequence/Identity
- Data Type
- Default
- Nullability
- Code/Status
- Metadata
- Query
- DDL
- DML
- Batch Metadata
- Domain Metadata
- Schema Version
- Migration Version
- Seed
- Search/Paging 대상 필드
- Audit 필드
- Optimistic Lock 필드
- Tenant 필드
- Transaction/Trace 필드

Generated Domain이 Framework와 다른 DB 규격을 사용하지 않게 한다.

---

# 11. Vendor DB 관리

DB 관련 변경 시 최소 다음을 함께 검토한다.

```text
cpf-tools/generator/
cpf-tools/db/vendor/
domain-template/
source/
migration/
install/
upgrade/
rollback/
seed/
verify/
pack metadata
Generated Domain DB artifacts
```

특정 Vendor SQL을 다른 Vendor로 단순 복사/문자치환하여 “지원 완료” 처리하지 않는다.

공식 지원 Vendor와 Candidate Vendor를 실제 구현/검증 기준으로 구분한다.

지원되지 않은 Vendor는 fail-closed가 맞다.

---

# 12. DB 실제 검증 환경

이번 작업은 **현재 PC의 Local MariaDB를 실제 사용**한다.

추가 DB 제품은 설치하지 않는다.

Repository Config/Profile/환경변수를 확인한 뒤 민감정보를 노출하지 않고 수행한다.

가능하면 실제 수행:

- Provision
- Fresh Empty Install
- Product Seed
- Verify
- Upgrade
- Migration
- Rollback
- Re-Upgrade
- Schema Drift
- FK/Index/Column 검증
- Canonical Source parity
- Generator DB 산출물 검증

실행하지 않은 DB 검증은 PASS로 기록하지 않는다.

DB Password/Secret은 콘솔 로그/Evidence/문서에 원문을 남기지 않는다.

---

# 13. 최신 GPT 개발분 전수 검수

최신 `20260726_05 / e725ed3...`에는 GPT가 대규모 Source를 추가/수정했다.

**GPT가 만들었다는 이유로 신뢰하지 않는다.**

다음 관점으로 전부 확인한다.

- Build 가능한가
- Compile 가능한가
- 실제 Consumer가 있는가
- Runtime에서 Bean이 연결되는가
- 중복 구현은 없는가
- 실제 DB와 SQL이 맞는가
- API가 호출되는가
- Frontend와 Backend가 연결되는가
- 권한이 있는가
- 오류 흐름이 안전한가
- Multi-instance에 안전한가
- 운영 제어가 실제 동작하는가
- Release/Deploy까지 연결되는가
- Generated Domain과 정합한가

---

# 14. Repository Hygiene 우선 점검

최신 Git Root에 GPT 작업용 산출물이 남아 있는지 확인한다.

예:

```text
CPF_APPLY_FIRST.md
CPF_DELETE_PATHS.txt
CPF_PACKAGE_SHA256SUMS.txt
```

제품 Root에 작업용 Patch/Apply/Checksum 파일을 남기지 않는다.

필요 시 다음과 같은 Owner 위치로 이동한다.

```text
cpf-docs/handover/
cpf-docs/evidence/
cpf-tools/scripts/config/
```

또 다음 Build Support Unit도 확인한다.

```text
cpf-tools/
  build/
    platform-bom/
    gradle-plugin/
```

`settings.gradle`의 includeBuild 경로와 실제 Directory가 일치해야 한다.

공식 Runtime Module과 Build Tooling을 구분한다.

Repository Root는 제품 식별/Build/실행에 필요한 최소 구조만 유지한다.

---

# 15. BAT Standalone Architecture

`cpf-batch`는 하나의 제품 Owner다.

하지만 Runtime Role은 독립 실행 Artifact여야 한다.

실행 Runtime:

```text
cpf-batch-control-server
cpf-batch-scheduler
cpf-batch-worker
cpf-center-cut-runner
cpf-batch-host-agent
```

Library:

```text
cpf-batch-contract
cpf-batch-runtime-common
cpf-batch-testkit
```

각 Runtime은 실제로 다음을 만족해야 한다.

- 독립 Gradle Project
- 독립 bootJar
- 독립 Main Class
- 독립 Profile
- 독립 Properties
- 독립 Port
- 독립 Health/Readiness
- 독립 Instance ID
- 독립 Log
- 독립 Shell
- 독립 PowerShell
- 독립 systemd
- 독립 Start/Stop/Restart
- Multi-instance 가능
- 특정 Runtime Artifact만 선택 배포 가능

하나의 Boot JAR에 Profile만 나눈 구조는 완료가 아니다.

---

# 16. Legacy `cpf-batch/src` 정리

최신 Git에서 `cpf-batch/src`가 존재하는지 확인한다.

바로 삭제하지 않는다.

먼저 기존 모든 기능을 분류한다.

```text
Legacy Source
→ Requirement
→ 새 Owner Module
→ 대체 Source
→ Consumer
→ Test
→ Runtime
```

특히:

- Batch Operations
- Job
- Execution
- Scheduler
- Worker
- Lock
- Ghost
- Retention
- Center-Cut
- Recovery
- EDU
- Test
- ADM 연동
- Audit

기존 성공 기능이 새 Standalone Module로 완전히 이관된 뒤 중복 Legacy만 제거한다.

Legacy와 신규 Bean/Controller/Repository가 동시에 활성화되어 중복 Runtime이 되지 않는지도 확인한다.

---

# 17. Build / Compile / Test

환경부터 기록한다.

```powershell
java -version
.\gradlew.bat --version
pwsh --version
node --version
npm --version
git --version
```

CPF Java 기준을 확인한다.

우선 저비용 Gate:

```powershell
.\gradlew.bat projects
.\gradlew.bat :cpf-batch:verifyStandaloneArtifacts --no-daemon
```

실패하면 즉시 원인 분석/수정.

관련 Module Test를 먼저 실행한 뒤 전체 검증으로 확대한다.

최종적으로 가능한 범위에서:

```powershell
.\gradlew.bat clean test assemble --no-daemon
```

Repository의 Quality Gate가 있으면 실행한다.

검증 Script 자체가 잘못되어 있으면 Script도 수정한다.

---

# 18. Credit / 반복 비용 관리

Codex Credit을 같은 전체 테스트 반복으로 낭비하지 않는다.

권장 순서:

```text
Static/Ownership/Dependency 검사
→ 관련 Module compile/test
→ 오류 수정
→ 관련 Integration
→ 여러 수정 묶기
→ 전체 Build
→ DB
→ Runtime/Multi-instance
→ Frontend
```

같은 명령이 실패하면 원인 분석 없이 반복 실행하지 않는다.

---

# 19. BAT Control Server

BAT Runtime Control Plane Owner로 실제 동작해야 한다.

검증:

- Runtime Registration
- Heartbeat
- Stale
- Unreachable
- Desired State
- Actual State
- Capabilities
- Command
- Command Idempotency
- Command CAS
- Approval handoff
- Deployment Plan
- Deployment Cell
- Instance Inventory
- Runtime Lifecycle
- Compatibility
- Reconcile
- Audit
- Evidence
- Retention

ADM이 batDB를 위험하게 직접 UPDATE해서는 안 된다.

ADM은 BAT Owner API를 사용한다.

---

# 20. Scheduler HA / Fencing

가능하면 Scheduler 2개 이상 실행한다.

검증:

1. Active Leader는 하나
2. Standby 존재
3. Lease 획득
4. Fencing Token
5. Leader Kill
6. Standby Takeover
7. 이전 Leader stale token 차단
8. 동일 Schedule 중복 Trigger 0
9. Business Calendar
10. Timezone
11. Available Window
12. Holiday Policy

단일 JVM에서만 안전한 구현이면 수정한다.

---

# 21. Worker

Worker 2개 이상 가능한 환경이면 실제 실행한다.

검증:

- Claim
- Lease
- Lease renew
- Fencing
- Heartbeat
- maxConcurrency
- Capability matching
- Version matching
- Queue depth
- Drain
- Resume
- Graceful shutdown
- Duplicate execution 방지

중요:

### 업무 실행 전 장애

안전한 경우 재할당 가능.

### 실제 업무 실행 후 장애

결과를 알 수 없으면 자동 재실행하지 않는다.

```text
UNKNOWN_RESULT
```

로 격리한다.

비멱등 업무의 자동 중복 실행을 허용하지 않는다.

명시적 Reconcile/Retry 경로를 제공한다.

---

# 22. Job Pack

업무 Batch Job을 BAT Runtime Source에 직접 하드코딩하지 않는다.

검증:

- Job Pack Manifest
- Job ID
- Job Name
- Parameter Definition
- Sensitive Parameter
- Identifying Parameter
- Platform Version Range
- Required Capability
- Checksum
- Signature
- Duplicate Job ID 차단
- Unsupported Version 차단
- Publish
- Worker Load
- Worker Execute
- Generated Domain Job Pack

Diagnostic/Test Job이 운영에서 기본 활성화되어서는 안 된다.

---

# 23. Center-Cut Runner

Center-Cut은 독립:

```text
cpf-center-cut-runner
```

Runtime이어야 한다.

검증:

- immutable Parameter Snapshot
- Target Provider
- Cursor
- Chunk
- Streaming
- Claim
- Lease
- Fencing
- Handler
- Result
- Multi-runner
- Global concurrency
- TPS
- Pause
- Resume
- Drain
- Cancel
- Failed-only retry
- UNKNOWN_RESULT
- Reconcile
- Compensation Hook

다중 Runner가 같은 Item을 동시에 처리하지 않아야 한다.

UNKNOWN_RESULT는 자동 Success/Retry로 바꾸지 않는다.

---

# 24. Host Agent

Host Agent는 원격 BAT Runtime 설치·운영을 위한 제한된 Agent다.

arbitrary shell API를 제공하면 안 된다.

검증:

- Approved Service Catalog
- Approved Command Catalog
- Artifact Coordinate
- Repository Origin 제한
- SHA-256
- Ed25519 Signature
- Artifact Size 제한
- Path Sandbox
- Install
- Upgrade
- Start
- Stop
- Restart
- Status
- Drain
- Resume
- Rollback
- Previous Version
- Log Collection
- mTLS
- Client Certificate
- Production fail-closed
- Secret Reference
- Audit
- Idempotency
- UNKNOWN_RESULT

사용자가 입력한 임의 Shell/Path/Executable을 그대로 실행하는 기능은 금지한다.

---

# 25. Linux / Windows Agent 운영

Linux:

- systemd unit
- EnvironmentFile
- permission
- process lifecycle
- log path
- graceful stop

Windows:

- START
- STOP
- RESTART
- STATUS
- PID 관리
- Service Artifact
- Log Root
- Config

Agent Java 코드의 Action과 PowerShell/Shell의 Action 이름이 실제 일치하는지 확인한다.

---

# 26. Remote Deployment

실제 Remote Host가 현재 없으면 무리하게 프로그램을 설치하지 않는다.

하지만 Source/Contract 기준으로 다음 Flow는 완성되어 있어야 한다.

```text
ADM
→ Approval
→ BAT Control Server
→ Deployment Plan
→ Compatibility
→ Host Agent
→ Artifact Pull
→ Checksum
→ Signature
→ Immutable Release
→ Drain
→ Stop
→ Install
→ Start
→ Readiness
→ Smoke
→ Resume
```

실패 시:

```text
Rollback
```

Transport 결과를 알 수 없으면:

```text
UNKNOWN_RESULT
```

를 유지한다.

---

# 27. Deployment Strategy

다음은 Enum만 존재한다고 완료가 아니다.

- Rolling
- Canary
- Blue/Green

실제 Deployment Engine에서 의미 있게 동작해야 한다.

검증:

- minHealthy
- maxUnavailable
- Deployment Lock
- Fencing
- Health
- Drain timeout
- Rollback
- Version
- Instance selection
- Scale-out
- Scale-in

Busy Runtime을 Scale-in에서 즉시 Kill하지 않는다.

기본:

```text
Drain
→ currentExecution = 0
→ Stop
```

---

# 28. ADM Batch Control Plane

화면 파일 존재만으로 완료 처리하지 않는다.

최소 기능:

- Overview
- Runtime Topology
- Execution
- Scheduler
- Worker Pool
- Center-Cut
- Host Agent
- Job Pack
- Deployment
- Rollback
- Recovery
- Lease/Ghost
- Alert
- Audit
- Evidence

각 기능:

- 실제 Route
- API
- Backend Consumer
- Permission
- Loading
- Empty
- Error
- Stale
- Partial
- UNKNOWN_RESULT
- Paging
- Search
- Detail
- Dangerous Action Confirm
- Responsive
- Accessibility

를 확인한다.

---

# 29. Approval / 위험조치

위험 BAT 운영조치는 기존 ADM Approval Engine을 사용한다.

검증:

- Requester
- Approver
- Requester != Approver
- reason
- target snapshot
- expected version
- expiration
- idempotency
- owner command
- audit
- result
- UNKNOWN_RESULT

개발운영자가 자기 승인해서 Deploy/Rollback하지 못해야 한다.

---

# 30. Domain Repository Federation

신규 업무 Domain을 CPF Root의 고정 Module로 계속 추가하지 않는다.

기본은 독립:

```text
cpf-domain-<domain>
```

Repository 구조다.

검증:

- Generator
- DomainName
- 3자리 SystemCode
- standalone settings.gradle
- Gradle Wrapper
- BOM
- Convention Plugin
- Published cpf-core/common
- dependency locking
- independent clean build
- DB Template
- optional Job Pack
- collision validation
- user-owned code 보호

Generated Domain 금지:

```text
project(':cpf-core')
project(':cpf-common')
project(':cpf-batch')
com.cpf.core.common.*
```

Public API/SPI Artifact를 소비해야 한다.

---

# 31. Generator 실제 생성 검증

충돌하지 않는 임시 Domain 2개 이상을 생성해 가능한 범위에서 검증한다.

검증:

- dry run
- generate
- standalone repository
- build
- test
- database template
- job pack
- same SystemCode collision
- module collision
- package collision
- DB collision
- regenerate
- remove
- user code protection

검증 후 테스트 Domain 찌꺼기를 Root에 남기지 않는다.

---

# 32. EXS

EXS를 고정 제품 Module로 복원하지 않는다.

외부연계 Ownership:

```text
범용 기술 → cpf-core
고객 업무 공통 정책 → cpf-common
기관/업무 Adapter → Generated Domain / Customer Extension
EDU → cpf-reference
```

Generator에 EXS 하드코딩을 넣지 않는다.

---

# 33. MBR / ACC / 기존 Domain

기존 Domain을 임의 삭제하지 않는다.

현재 Root에 있는 Generated/Reference Domain과 향후 독립 Repository 정책의 관계를 확인한다.

특히 MBR은 Golden Reference 역할 여부를 정본과 실제 Generator 기준으로 판단한다.

ACC 등 기존 업무 Domain의 Consumer/DB/Test를 분석하고 이관 정책을 명확히 한다.

---

# 34. Frontend

Node/npm이 이미 설치돼 있으면 실제 검증한다.

ADM:

```powershell
.\gradlew.bat :cpf-admin:frontendVerify --no-daemon
```

BZA:

```powershell
.\gradlew.bat :cpf-biz-admin:frontendVerify --no-daemon
```

또는 해당 package.json의 실제 Script를 사용한다.

확인:

- npm install/ci
- typecheck
- lint
- unit test
- build
- Route resolve
- API path
- Permission
- logout
- token refresh
- 400/401/403/409/500
- console error

Browser Driver/Playwright 등 별도 프로그램이 없으면 임의 설치하지 않는다.

Source/Test 가능한 범위까지 완료하고 Browser E2E 실행 조건을 Handover에 남긴다.

---

# 35. 이전 전체 성공 기능 회귀검수

이번 BAT/GPT 변경만 검수하지 않는다.

기존 R9~R14 및 QA 작업의 성공 기능도 최신 Source에서 회귀되지 않았는지 확인한다.

최소:

- Service Call Engine
- Local Adapter
- Remote Adapter
- Registry
- Gateway
- Standard Header
- transactionGlobalId
- Error
- Validation
- Logging
- DB/File Logging
- Outbox
- Inbox
- DLQ
- Async
- Idempotency
- Retry
- Recovery
- Compensation
- Security
- Authentication
- Authorization
- Audit
- Masking
- Cache
- Feature Flag
- Messaging
- File/Attachment
- External Integration
- ADM
- BZA
- Generator
- DB Lifecycle
- EDU
- OpenAPI
- JavaDoc
- Release
- Upgrade
- Rollback
- Repository Governance

과거 문서가 완료라고 해도 실제 Consumer/Test가 없으면 다시 판정한다.

---

# 36. Public API / SPI / Internal

각 구현에 대해 확인한다.

- 이 기능의 Owner Module은 어디인가
- Public API인가
- Extension SPI인가
- Internal 구현인가
- 실제 Consumer는 누구인가
- 외부 Module이 Internal Package를 직접 import하는가
- 역방향 의존이 있는가
- 순환 의존이 있는가
- 같은 계약이 중복 정의되어 있는가

특히 Generated Domain에서:

```text
com.cpf.core.common.*
```

Internal 구현 직접 참조를 금지한다.

---

# 37. Security / Audit / Masking

최신 Source 기준으로 다시 확인한다.

- Authentication
- Authorization
- Role
- Permission
- Tenant
- mTLS
- Secret
- Masking
- Audit
- Dangerous operation
- Reason
- Approval
- Result tracking
- Session
- CSRF/CORS
- Download authorization
- Sensitive logs

민감정보 원문을:

- Log
- Evidence
- ADM 화면
- Exception
- Download
- Script

에 노출하지 않는다.

---

# 38. UNKNOWN_RESULT

CPF 중요 표준이다.

다음 상황에서 성공으로 추정하지 않는다.

- Remote command transport timeout
- Worker crash after external side effect
- Center-Cut result uncertainty
- Agent response lost
- Deployment response unknown

UNKNOWN_RESULT는 반드시:

```text
조회
→ Reconcile
→ 필요 시 승인된 Retry / Compensation / Rollback
```

경로를 제공해야 한다.

---

# 39. Multi-instance / Concurrency

가능한 범위에서 실제 검증한다.

- Duplicate scheduler trigger
- Worker claim race
- Center-Cut item race
- Idempotency key
- Optimistic Lock
- Deployment lock
- Fencing
- Sequence/ID
- Cache invalidation
- Outbox claim
- DLQ reprocessing
- Runtime heartbeat
- stale detection

단일 인스턴스에서만 성공하는 것으로 완료 처리하지 않는다.

---

# 40. Release / Artifact / Supply Chain

가능한 범위에서 확인한다.

- BOM Publish
- Convention Plugin Publish
- cpf-core/common Publish
- BAT Contract/Testkit Publish
- 5 BAT Runtime Artifact
- Version
- SHA-256
- Signature
- SBOM
- Provenance
- Compatibility
- clean master requirement
- SNAPSHOT vs Release
- CVE/License Gate

외부 Scanner가 설치돼 있지 않으면 임의 설치하지 않고 Source/Gate 정의를 검수한다.

---

# 41. 추가 프로그램 설치 정책

이번 작업 때문에 현재 PC에 없는 외부 제품을 임의 설치하지 않는다.

예:

- Docker
- Kubernetes
- Broker
- Vault
- HSM
- Remote SSH Server
- External Tomcat
- Artifact Registry
- 추가 DB Vendor
- Browser Driver

환경에 없으면:

1. Source 검수
2. Config 검수
3. Contract 검수
4. Unit/Mock/Test-double 가능한 검증
5. 필요한 Test Source 보완
6. 실제 외부 E2E 실행만 미검증
7. 후속 검증 명령/환경조건 기록

한다.

---

# 42. 상태 판정

허용 상태:

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

그러나 이번 1차 검수 종료 시 **현재 환경에서 검증 가능한 항목은 `완료` 상태로 만드는 것이 목표**다.

`부분 구현`, `미구현`, `실패`가 나오면 현재 환경에서 수정 가능한 것은 직접 개발한다.

`미검증`은 실제 외부 환경 부재처럼 Codex가 현재 실행할 수 없는 경우에만 허용한다.

---

# 43. 완료로 인정하지 않는 근거

다음만으로 완료 처리하지 않는다.

- Class 존재
- Package 존재
- Interface 존재
- 일부 Unit Test
- Static grep
- Swagger
- Config 문자열
- Sample
- Dry-run
- Previous Evidence
- GPT 성공 보고
- Codex 과거 성공 보고
- 문서의 완료 체크

반드시 Requirement와 실제 구현/Consumer/Test/Runtime을 연결한다.

---

# 44. 양방향 추적

작업 중 다음 양방향 추적을 수행한다.

```text
Requirement
→ Source
→ API
→ SQL
→ Test
→ Runtime
→ Evidence
```

반대로:

```text
Implementation
→ Requirement
→ Owner
→ Consumer
→ Operation
→ Test
```

Owner/Consumer/Requirement가 없는 구현은 구조적 부채인지 확인한다.

---

# 45. Evidence

실제로 수행한 검증에는 최소 다음을 남긴다.

- 기준 SHA
- 실행 명령
- 시작 시각
- 종료 시각
- Java/Gradle/Profile
- DB/Environment
- 관련 Requirement
- Expected
- Actual
- PASS/FAIL
- 오류 원인
- 수정 내용
- 재실행 결과
- Log/DB 조회
- 민감정보 제거 여부
- Evidence가 현재 SHA에 유효한지

다른 장비/과거 SHA Evidence를 현재 PASS로 자동 승계하지 않는다.

---

# 46. 작업 중 문서/Handover 갱신

이 프로젝트는:

- 회사 PC
- 집 PC
- Codex
- GPT/ChatGPT

가 번갈아 작업한다.

따라서 작업 중 중요한 Architecture 결정이나 예상 외 결함을 발견하면 마지막까지 기억에만 의존하지 말고 정본 Handover/Decision Log에 반영한다.

작업 종료 시 반드시 다음 역할을 최신화한다.

```text
cpf-docs/work/current/
cpf-docs/work/state/
cpf-docs/work/review/
cpf-docs/evidence/
```

---

# 47. 작업 완료 리포트 필수 내용

작업 종료 리포트에는 최소 다음이 있어야 한다.

## 기준

- 시작 SHA
- 종료 시 HEAD
- origin/master
- Working Tree 상태

## 수정

- 수정 파일 목록
- 주요 변경
- 해결 Requirement
- Architecture 변경
- DB/Generator 변경
- Frontend 변경
- Deployment 변경

## 검증

- 실행한 명령
- PASS
- FAIL 발생 이력
- FAIL 원인
- 수정 후 재검증 결과

## 외부환경 미실행

- 어떤 Test를 못 했는가
- 왜 못 했는가
- Source 검수는 무엇을 했는가
- 필요한 환경
- 정확한 실행 명령
- Expected Result

## Remaining Gap

현재 환경에서 해결 가능한 문제를 단순 Remaining Gap으로 남기지 않는다.

실제 외부 환경 의존 또는 다음 대형 Milestone에 속하는 것만 남긴다.

---

# 48. Current Request 정리

`CPF_CURRENT_WORK_REQUEST.md`에는 현재 실제 남은 작업만 유지한다.

금지:

- 완료된 과거 작업 누적
- 오래된 SHA 유지
- 이미 수정한 Gap 반복
- 동일 Gap 이름만 바꾸어 반복

작업 완료 후 현재 Request가 비어야 할 정도로 이번 범위를 모두 처리했다면,
그 사실과 다음 통합검증/외부환경 검증만 명확하게 남긴다.

---

# 49. 다음 GPT 인수인계

작업 종료 후 GPT가 다시 이어서 전체 목표를 완성할 예정이다.

따라서 Handover에는 반드시 다음을 명확하게 작성한다.

1. 최신 기준 Commit
2. Codex 시작 상태
3. Codex가 실제 수정한 내용
4. 실행 검증 결과
5. 수정 후 PASS 여부
6. DB Generator/Schema 정본 변경
7. Vendor SQL 변경
8. BAT/Agent/Deployment 상태
9. ADM/BZA 상태
10. Domain Generator/Federation 상태
11. External 환경 때문에 실행하지 못한 검증
12. 다음 GPT가 바로 시작해야 할 정확한 위치
13. 다시 조사할 필요 없는 완료 항목
14. 주의해야 할 Architecture 결정
15. Commit/Push하지 않았음을 명시

다음 GPT가 다시 처음부터 분석하지 않도록 구체적으로 남긴다.

---

# 50. Credit 부족 시 종료 기준

Credit이 부족해 모든 고비용 E2E를 끝내지 못하더라도 갑자기 중단하지 않는다.

반드시:

1. 현재 발견한 오류는 가능한 범위까지 수정
2. 수정 중이던 파일을 일관된 상태로 정리
3. 실행한 검증 Evidence 저장
4. 실패한 명령과 원인 저장
5. 다음에 실행해야 할 정확한 명령 저장
6. Handover 최신화
7. Current Request 최신화

후 종료한다.

단순히:

```text
Credit 부족으로 중단
```

만 남기지 않는다.

---

# 51. 이번 작업의 최종 완료 목표

이번 Codex 작업 완료 시점에는:

### 현재 환경에서 검증할 수 있는 모든 항목

```text
오류 발견
→ 수정
→ 재검증
→ PASS
```

상태를 목표로 한다.

### 현재 환경에 실제 외부 Infra가 없어 실행할 수 없는 항목

```text
Source/Architecture/Config/Test 검수 완료
+ 실행 불가 이유 기록
+ 후속 실행 명령 기록
```

까지 완료한다.

가장 중요한 원칙:

> **오류를 찾는 것이 목적이 아니라 오류가 없는 상태로 만드는 것이 목적이다.**

> **부분 구현을 분류하는 것이 목적이 아니라 현재 구현 가능한 부분 구현을 완성하는 것이 목적이다.**

> **DB/SQL을 직접 고치는 것이 아니라 Generator·Canonical Schema/Metadata를 먼저 고치고 Vendor SQL/Migration/Install/Rollback을 동기화하는 것이 CPF의 표준이다.**

> **GPT/Codex 과거 보고를 신뢰하지 말고 최신 Git의 실제 Source와 Runtime을 검증한다.**

> **기존 성공 기능을 보호하면서 CPF 최종 제품 목표 전체의 완성도를 높인다.**

---

# 52. Codex 시작 지시

위 내용을 모두 기준으로 작업을 시작한다.

1. 최신 Git 상태부터 확정한다.
2. 최상위 Requirement와 최신 Handover/Current Request를 읽는다.
3. `20260726_05` GPT 대형 변경을 우선 검수한다.
4. Repository Hygiene/Build 구조부터 저비용 Gate를 실행한다.
5. Compile/Test 오류를 수정한다.
6. DB는 Generator/Canonical Schema/Metadata부터 확인한다.
7. MariaDB 실제 검증을 수행한다.
8. BAT Standalone/Multi-instance를 실행 검증한다.
9. ADM/BZA/Generator/Release를 검증한다.
10. 오류는 즉시 수정하고 재검증한다.
11. 기존 R9~R14 성공 기능도 회귀검수한다.
12. 작업 도중 중요한 결정은 Handover에 남긴다.
13. 종료 시 Review/Evidence/Current/Handover를 최신화한다.
14. Commit/Push/Branch는 하지 않는다.

**최종 보고만 작성하지 말고 실제 개발·수정·검증을 수행할 것.**
