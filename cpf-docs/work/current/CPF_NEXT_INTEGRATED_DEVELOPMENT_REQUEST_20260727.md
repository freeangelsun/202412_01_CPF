# CPF 다음 통합 개발 요청 — 2026-07-27

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 현재 작업 시작 SHA: `702bf83580b9c4db2dbba6482ece233e00842f1b` (`20260727_03`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 입력: `CPF 차기 통합 QA 요구사항` (`9e4edaef...` 기준 작성)
- 실제 상태 정본: Source/API/SQL/Test/Runtime/Evidence
- 상세 QA 입력 보존: `cpf-docs/work/review/CPF_QA_INPUT_20260727_04.md`

QA 기준 SHA 이후 `20260727_03` Gate/Tool 문서가 추가 Push되었으므로 모든 요구는 `702bf835...`에서 다시 계산한다.

## 2. 작업 원칙

1. 작업 전에 이전 Codex/ChatGPT 구현을 품질·Architecture·Ownership 관점에서 먼저 리뷰한다.
2. 기존 성공 구현은 보호하고, 잘못된 구조만 올바른 Owner로 이관한다.
3. 하나의 Change Set을 Source/Test/Guide/Gate까지 가능한 범위에서 닫고 다음 축으로 넘어간다.
4. 이후 변경의 영향권에 들어온 과거 PASS는 `재검증 필요`로 다시 연다.
5. 직접 영향 없는 고비용 Evidence를 습관적으로 전부 반복하지 않는다.
6. 실행하지 않은 검증은 `미검증`이다.
7. 사용자 승인 없이 Commit/Push/Branch/Tag/Release를 생성하지 않는다.

## 3. 작업 전 리뷰 결론

### Codex `20260727_01`

대체로 올바른 방향:

- BAT Runtime 역할 분리
- Lease/Fencing
- ADM MBR Ownership 제거
- REF MBR 중립화
- Query Contract/Runtime SQL Template
- Build Tooling `cpf-tools/build` 이동

남은 위험:

- Generated Domain Golden parity
- BAT EDU parity
- Gateway Fault/Multi-instance
- 대규모 변경 이후 최종 regression

### ChatGPT `20260727_02`

방향은 적절했으나 QA를 반영해 다음을 재보강한다.

- Artifact 공개 전 Quality 기준
- Local auto-sync side effect
- partial/mixed mutable artifact
- exact POM/BOM/Plugin Marker/Hash
- REMOTE Local fallback 금지
- Server/Offline 공급 모델

상세 리뷰:
`cpf-docs/work/review/CPF_CHATGPT_PRE_IMPLEMENTATION_REVIEW_20260727_04.md`

## 4. 통합 우선순위

### CHANGE-SET-A — Stack / Artifact / Baseline Safety

상태: **ChatGPT Source 보강 진행 / Runtime 미검증**

관련 QA:

- `QA-STACK-001~003`
- `QA-ART-001~010`
- `QA-BASE-001~003`

목표:

- 현재 지원 범위 밖 Stack을 숨기지 않고 `TRANSITION` 관리
- Version Single Source
- `LOCAL_DEV / REMOTE / OFFLINE` 배타 공급
- REMOTE Local fallback 금지
- Local auto-sync 기본 off
- Aggregate Quality → isolated staging → artifact verify → manifest barrier promotion
- Remote `cpfInternal` 전용 publish
- Offline Maven Bundle
- 최신 SHA 문서 재기준화

Spring Boot 4 실제 Migration은 별도 Stack Migration Change Set으로 수행한다.
현재 Candidate는 4.1.0이며 External WAS/Servlet 6.1/Spring Batch/MyBatis/Flyway/Generated Domain compatibility를 먼저 검증한다.

### CHANGE-SET-B — ADM/BZA Data Safety

관련 QA:

- `QA-ADM-DB-001~005`
- `QA-PII-001~008`
- `QA-BZA-STATUS-001~005`
- `QA-DB59/60`
- `QA-SQL-001~004`

목표:

- ADM Identity/Profile/Role 생성 Transaction 원자성
- Product DB 오류 fail-closed, Memory fallback 명시 Demo/Test 전용
- 연락처 Masked API/UI, raw 권한/사유/Audit
- Audit/Log/Trace/Evidence PII redaction
- Blank → NULL
- Employment Status/Account Status 분리
- V59/V60 upgrade/rollback/reapply/fresh
- BZA inline SQL/Vendor SQL 및 Core Internal import 정리

### CHANGE-SET-C — Generated Domain Golden

관련 QA: `QA-GEN-001~010`

목표:

- MBR/ACC/신규 Domain normalized parity
- Root fixed include 정책 제거
- Package/Class/Port 기반 SystemCode 추론 제거
- 임시 Domain 2개 create/export/build/package/runtime/remove/regenerate
- Local/Remote/Offline Artifact Mode 동일 계약
- Generator/DB/Install 동적 발견

### CHANGE-SET-D — BAT Runtime / EDU / Job Pack

관련 QA: `QA-BAT-001~007`

목표:

- 삭제 Legacy File 전체 disposition
- Runtime 기능 parity
- REF EDU 실행 parity
- Generated Domain Job Pack 표준
- Restart/Rerun/Checkpoint/Idempotency/Unknown Result
- Scheduler/Worker/Center-Cut 실제 Multi-instance

Batch Scheduler lifecycle 정본:
`cpf-docs/guides/CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md`

### CHANGE-SET-E — Gateway / Operations

관련 QA: `QA-GWY-001~009`, UI/ADM/BZA/OBS/LIFE

목표:

- target-down failover
- timeout/retry budget
- non-idempotent UNKNOWN_RESULT/reconciliation
- O/S/B 경계
- Header trust
- 2 Gateway drift/rejoin
- ADM/BZA Browser/Accessibility
- transactionId timeline
- Install/Upgrade/Rollback/Deployment Cell

## 5. Artifact 공급 최종 방향

### LOCAL_DEV

```text
같은 Repo Domain
→ Project Dependency로 Core/Common 변경 자동 반영

독립 Domain
→ 검증 Shared Local Maven Repository
→ current HEAD PROMOTED manifest가 있으면 재사용
→ 없거나 stale이면 verified publish
```

### REMOTE

```text
Jenkins Platform Build
→ 검증된 Version
→ Nexus/Artifactory cpfInternal
→ 업무 Domain이 고정 Version 소비
```

Local fallback 금지.

### OFFLINE

```text
Verified CPF Maven Set
→ Version/SourceSHA/Hash/Manifest Offline Bundle
→ 폐쇄망 서버에서 OFFLINE Repository로 사용
→ Domain bootJar/bootWar에 자동 포함
```

개별 JAR 수동 복사는 정상 절차가 아니다.

상세:
`cpf-docs/guides/CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md`

## 6. Gate / Tool / Manual

정본:
`cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`

향후 전체 Gate를 다음으로 분류한다.

- `DEV_ONLY`
- `CI_RELEASE`
- `PRODUCT_ADMIN_TOOL`

대표 Entry 목표:

- `QUICK`
- `VERIFY`
- `FULL`

중복/Legacy/Caller 0/일회성 Gate는 Requirement coverage를 확인 후 통합·삭제한다.
개발/CI Gate는 Runtime 제품 배포물에 포함하지 않는다.

공식 Tool은 옵션/Default/환경변수/입출력/Side Effect/실패/복구/예제를 문서화한다.

## 7. 보호할 기존 성공 기능

- BAT 158 Query Pack과 기존 MariaDB PREPARE Evidence
- V58 lifecycle Evidence
- BAT Lease/Fencing 구조
- ADM MBR 직접 결합 제거
- REF MBR 중립화
- ADM Identity/Profile 연락처 Ownership
- V59/V60 Source/Migration/Rollback

단 이후 변경 영향이 생기면 해당 PASS는 다시 연다.

## 8. 현재 재검증 필요

CHANGE-SET-A 영향:

- Java25 전체 compile/test
- Included BOM/Convention Plugin
- Generated standalone Domain
- bootJar/bootWar
- Local Artifact publish/promotion
- Generator create/export/package

즉시 전수 반복하지 않는 것:

- BAT 158 SQL PREPARE
- V58 SQL lifecycle

최종 aggregate/historical migration에서 영향이 생기면 다시 연다.

## 9. Codex 투입 시점

Codex는 지금 바로 투입하지 않는다.
ChatGPT가 위 Change Set을 몇 차례 더 수행한 후 최신 master의 누적 Diff를 기준으로 다음 문서를 재생성한다.

- Implementation Report
- Change Impact Ledger
- Codex Review Checklist
- Handover
- Continuity
- Evidence Index
- Unverified Scenario List

Codex는 ChatGPT 보고를 완료 근거로 사용하지 않고 최신 Git/Consumer/DB/Generator/UI/Runtime을 독립 검증한다.

## 10. 완료 금지

다음 상태에서는 완제품으로 완료 처리하지 않는다.

- 공식 지원 가능한 Stack 미확정
- Partial/Mutable Artifact 혼합 위험 미해소
- ADM Partial DB Write/fail-open
- PII 원문 목록/Audit
- V59/V60 lifecycle 미검증
- Generated Domain 특별취급
- BAT 삭제 기능 미대체
- Gateway Fault 미검증
- Browser/Multi-instance 미검증
- 최신 SHA Evidence 부재
