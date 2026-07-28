# CPF Codex 전체 QA 검수·보완 개발 요청서 — 20260729

## 1. 수행 목적

이번 작업은 이번 ChatGPT 세션의 변경 파일만 검토하는 코드 리뷰가 아니다.

**직전 Codex 검수 이후 최신 master에 누적된 모든 변경**, 20260728 1차 QA Closure 구현, 20260729 2차 보완, 기존 QA 2,118건, 신규 통합 QA를 모두 포함한 최종 제품 검수·보완 개발이다.

검수 중 발견한 결함은 개수 제한 없이 직접 수정한다. 문서나 Matrix만 갱신하고 실제 결함을 남기지 않는다.

## 2. 기준과 정본

작업 시작 즉시 다음을 수행한다.

```powershell
git pull --ff-only
git status --short
git rev-parse HEAD
git log -5 --oneline
```

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 1차 Overlay 시작 기준: `84b4672e9e8b61ea067bb52b85b838a0b95e44b1`
- 1차 Push 확인 SHA: `9e5d1676a9ccba55fedf4dfb633a9e710f487a02`
- 실제 검수 기준: **사용자가 Stage 2 Overlay를 적용·Push한 최신 master SHA**
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

다음 자료를 우선 확인한다.

1. 최상위 목표·Architecture·Specification
2. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
3. 본 요청서
4. `cpf-tools/verification/20260729_02/CPF_FINAL_QA_SOURCE_MANIFEST_20260729.json`
5. `cpf-tools/verification/20260729_02/CPF_FINAL_QA_MASTER_LEDGER_20260729.csv`
6. `cpf-tools/verification/20260728_04/P0_REMEDIATION_LEDGER.csv`
7. `cpf-tools/verification/20260728_04/CPF_ADM_RUNTIME_CONTROL_SUPPLEMENT_20260728.md`
8. Source·SQL·API·Frontend·Generator·Test·Config·Script·Evidence

문서 완료 표시보다 최신 Git 구현과 실제 실행 결과를 우선한다.

## 3. 최종 QA 원장

병합 원장 작성 시점 기준:

- 기존 요구사항: 1,749건
- 기존 실행 시나리오: 369건
- 기존 합계: 2,118건
- 신규 고유 요구사항: 579건
- 신규 실행 시나리오: 18건
- 병합 요구사항: 2,328건
- 병합 실행 시나리오: 387건
- 병합 합계: **2,715건**

신규 QA 중 확정 중복 1건은 기존 Root Cause에 병합했고, 중복 후보 5건은 다음 파일에 보존했다.

`cpf-tools/verification/20260729_02/qa-source/CPF_ADDITIONAL_QA_DEDUP_TRACE_20260729.csv`

중복 후보는 문장 유사도가 아니라 다음 기준으로 재확정한다.

```text
Requirement Owner
→ Root Cause
→ Source Owner
→ 실제 Consumer
→ DB·Runtime·UI Owner
→ 정상·실패·복구 실행 경로
```

병합하더라도 원본 ID와 `merged_into` 추적을 삭제하지 않는다. 근거 없이 총수를 줄이지 않는다.

## 4. Stage 2에서 우선 확인할 실제 수정

### 4.1 Gradle 전역 구성 실패

사용자 실행에서 다음 오류가 발생했다.

```text
Could not get unknown property 'targetProject' for project ':cpf-common'
```

Root `build.gradle`의 `subprojects` Closure가 `targetProject`를 선언하지 않고 참조한 결함을 수정했다.

검수:

```powershell
.\gradlew.bat help --no-daemon --stacktrace
.\gradlew.bat :cpf-admin:test --tests "com.cpf.admin.opr.service.AdmNotificationOutboxServiceTest" --no-daemon --stacktrace
```

구성 단계에서 모든 Module이 정상 평가돼야 한다.

### 4.2 Notification Portable SQL Gate 오판

기존 Gate가 `new String[] {"delivery_id"}`를 `AdmNotificationService`에서 찾았지만 실제 Owner는 `AdmNotificationOutboxService`였다.

수정 후 Service와 Outbox를 분리 검증하며 MariaDB 전용 SQL과 Internal Package 참조를 둘 다 차단한다.

### 4.3 Durable Outbox Lease 만료

기존 구현은 `PROCESSING` Worker가 종료되면 Lease 만료 후에도 `READY/RETRY` 조회 대상이 아니어서 영구 고착됐다.

수정 정책:

- 만료 `PROCESSING`은 자동 재발송하지 않는다.
- Provider 성공 후 DB 결과 반영 전에 Worker가 죽었을 수 있으므로 `UNKNOWN_RESULT`로 격리한다.
- `LEASE_EXPIRED_UNKNOWN_RESULT`를 기록한다.
- 운영자가 Provider 이력과 operationId를 대조한 뒤 Retry 또는 Cancel한다.

### 4.4 운영 Retry·Cancel CAS

Retry·Cancel API에 `expectedVersion`을 추가했다.

- 상태와 Version을 함께 비교한다.
- 충돌은 HTTP 409로 반환한다.
- 인증된 `adm.operatorId`만 사용한다.
- 요청의 `requestUser`가 인증 사용자와 다르면 403이다.
- 사유와 Audit를 보존한다.

### 4.5 Provider Attempt 불변 이력

재시도 성공 후 최초 실패 Attempt가 부모 발송 행에 덮어써지는 결함을 보완했다.

- 신규 Table: `cpf_notification_delivery_attempt`
- Migration: V68
- Rollback: R68
- Vendor: MariaDB·PostgreSQL·Oracle
- Claim 시 Attempt 생성
- 성공·실패·Timeout·UNKNOWN_RESULT 확정
- Lease 만료 시 미완료 Attempt도 UNKNOWN_RESULT
- Provider 메시지 민감정보 제거
- ADM API와 화면에서 Attempt Timeline 조회

확인할 핵심:

- Parent 결과 확정과 Attempt 결과 확정이 동일 DB Transaction에서 원자적으로 처리되는지
- Provider 호출은 DB Transaction 밖인지
- Parent CAS 실패 시 Attempt 결과가 거짓 완료되지 않는지
- Worker 재시작 후 Attempt 이력이 남는지
- 동일 Attempt 번호 중복 생성이 PK로 차단되는지
- Retry 후 기존 Attempt가 갱신·삭제되지 않는지

### 4.6 ADM Notification 운영 화면

Raw JSON `<pre>` 대신 다음을 제공한다.

- Durable Outbox 발송 이력 Table
- operationId·requestHash
- 상태·시도 횟수·다음 Retry
- Lease Owner·Lease Until
- lastErrorCode·Version
- Retry·Cancel 가능 상태 제어
- expectedVersion
- Provider Attempt Timeline

Frontend Button 숨김만 확인하지 말고 직접 API 호출 401·403·409도 검증한다.

## 5. 전체 검수 범위

P0 18건과 Runtime Control 14개 Capability는 우선순위이며 전체가 아니다.

다음을 전수 수행한다.

1. 병합 QA 2,715건 전수 재판정
2. 1차 구현 묶음 회귀 검수
3. ADM Runtime Control 14개 Capability의 실제 Runtime Consumer·ACK·Drift·Rollback
4. Module Ownership과 Public API·SPI·Internal 경계
5. ACC·MBR·EXS와 임시 Generated Domain normalized parity
6. 업무 기능별 Package 응집도와 Local·Remote parity
7. ADM/BZA Menu·Route·Page·Button·Permission·API parity
8. Tree·Upload·Download·Remote Operation Result·Failure UX
9. Dead Code·Legacy·Stale Evidence·Repository Garbage 제거
10. 3개 공식 DB Install·Migration·Upgrade·Rollback·Runtime Query
11. Gateway·Service Call·Cache·Notification·Batch·외부연계 다중 인스턴스·부분 실패·복구
12. Source·SQL·API·Test·Guide·Evidence 양방향 추적

## 6. 자동 Gate와 Matrix

우선 실행:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-notification-portable-sql.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-integrated-architecture-ui-hygiene.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
```

Matrix 생성:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\export-full-qa-closure-matrices.ps1
```

산출물:

- `MODULE_PACKAGE_MATRIX.csv`
- `GENERATED_DOMAIN_PARITY_MATRIX.csv`
- `MENU_UI_MATRIX.csv`
- `GARBAGE_REMOVAL_MATRIX.csv`
- `MATRIX_MANIFEST.json`

Matrix는 후보 탐지 결과다. 파일·Class·메뉴 존재만으로 완료 전환하지 않는다.

## 7. 필수 실행 순서

### 7.1 저비용 Gate

```powershell
git diff --check
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
.\gradlew.bat help --no-daemon --stacktrace
```

실패하면 다음 단계로 넘어가기 전에 Root Cause를 수정한다.

### 7.2 Java 25·Gradle 9.1

```powershell
.\gradlew.bat clean test assemble qualityGate verifyCpfFinalSourceGates checkJava25Standard checkCpfStackSupport --no-daemon --stacktrace
```

### 7.3 Frontend

```powershell
.\gradlew.bat :cpf-admin:frontendVerify :cpf-biz-admin:frontendVerify --no-daemon --stacktrace
```

Browser에서 권한별 Route·Button·API 직접 호출·Tree·Upload·Download·오류 UX를 확인한다.

### 7.4 Generated Domain

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\generator\verify-domain-federation.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-generator-golden-path.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-generator-arbitrary-domain-parity.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\smoke-generated-domain-lifecycle.ps1
```

### 7.5 3개 DB Lifecycle

실제 Secret 없는 Profile 경로를 사용한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\invoke-cpf-final-closure.ps1 `
  -DatabaseProfilePath .\profiles\mariadb.json,.\profiles\postgresql.json,.\profiles\oracle.json `
  -RunGitHubGovernance
```

각 Vendor에서 확인:

- Fresh Install
- Historical Migration
- V67·V68 Upgrade
- R68·R67 Rollback
- Re-upgrade
- Generated Key
- Notification Outbox·Attempt
- Checksum·Schema Drift
- Runtime Query

### 7.6 다중 인스턴스·장애 복구

최소 다음을 실제 Process로 수행한다.

- Outbox Worker 2개 동시 Claim
- Provider 성공 후 결과 DB 반영 전 Process Kill
- Lease 만료 UNKNOWN_RESULT
- ACK 유실
- Offline Instance 복귀
- Retry·Cancel CAS 충돌
- Runtime Control 부분 성공·미응답·Rollback·Reconcile
- Cache Snapshot High-Water Replay
- Service Call Target Down·Timeout·Failover
- Batch Worker Crash·Lease·Fencing·Drain

## 8. Evidence

최신 master 정확한 SHA에서 다음을 남긴다.

- 기준 SHA
- 명령
- Profile·환경
- 시작·종료 시각
- Exit Code
- 실제 원본 Log
- DB 조회 결과
- Browser 결과
- Requirement·Scenario ID
- 민감정보 제거 여부
- 파일 SHA-256

한 줄 PASS, 과거 Commit, 다른 장비 결과를 승계하지 않는다.

## 9. 완료 판정과 산출물

허용 상태:

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

작업 종료 시 병합 원장 2,715건을 최신 Evidence로 갱신한다. `완료` 이외 항목이 남으면 전체 완료로 보고하지 않는다.

다음 산출물을 갱신한다.

- Source·SQL·Migration·Rollback·Frontend·Generator·Test
- 병합 QA 원장과 Dedup Trace
- 네 가지 QA Matrix
- Implementation Report
- Evidence Index
- Current Request
- Handover

## 10. 금지사항

- 18개 P0만 처리하고 전체 완료 선언
- Runtime Control 14개 이름만 확인
- 1차 구현 회귀 제외
- Mock·Simulator를 실제 Provider 성공으로 판정
- Source·API·Table·화면 존재만으로 완료
- 실패 Test를 삭제·완화하여 통과
- Migration checksum을 현재 파일에 맞춰 조용히 재생성
- Legacy를 Archive로 옮기고 제거 완료 처리
- Evidence 없는 상태 변경
- 사용자 승인 없는 Commit·Push·Branch·Tag·Release

## 신규 QA 계수 보정

- 신규 QA 원문 Bullet 590개 중 Section 15 판정 분류 4개와 Section 18 허용 상태 6개는 독립 Requirement가 아니므로 `EXCLUDED_METADATA`로 보존하고 병합 원장에서는 제외했다.
- 기존 Requirement와 확인된 중복 1개는 `MERGED` 처리했다.
- 따라서 신규 고유 Requirement는 579건, 신규 Scenario는 18건이며 최종 병합 원장은 Requirement 2,328건 + Scenario 387건 = 2,715건이다.
- 중복 후보 5건은 임의 삭제하지 않고 Codex가 Root Cause·실제 Consumer 기준으로 재확정한다.
