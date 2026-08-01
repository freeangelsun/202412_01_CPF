# CPF QA37 Codex 독립 검수 요청서 — 비용 절약형 단일 통합 실행

## 1. 검수 기준과 절대 경계

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Overlay Source Baseline: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`
- 실제 검수 기준: 사용자가 Overlay와 승인된 Delete Manifest를 적용·Commit·Push한 뒤의 `origin/master` exact SHA
- Codex 역할: 독립 검수자. Stage별 최초 Source Defect가 확인된 경우에만 영향 범위 안에서 최소 수정한다.
- Git 쓰기: 사용자의 별도 승인 없이는 Commit·Push·Branch·Tag·PR·Reset·Restore·Stash·Clean 금지

### EDU Architecture 불변 조건

1. EDU Owner Module은 `cpf-reference` 하나다.
2. EDU DB Owner는 중앙 Vendor Pack의 `refDB`다.
3. ACC·MBR·EXS 등 생성형 도메인 Source·API·DB·Job을 절대 참조하지 않는다.
4. 제품 `cpf-biz-admin`은 EDU 실행 의존성이 아니다. `EDU-BZA-*`는 정본 추적 ID이며 구현은 제거 가능한 REF Backoffice Pack이다.
5. Batch는 `com.cpf.reference.batch` 아래 기능별 Package와 `CPF_REF_BAT_*` SQL Pack으로 격리한다.
6. Operations·Backoffice·Gateway Simulator는 `com.cpf.reference.optional.*` 아래 선택 Pack이다.
7. Query·Schema 변경은 Canonical refDB Source → Oracle/PostgreSQL/MariaDB → Install → Upgrade → Rollback → Runtime Query → Verify → Checksum → Generator 제외 계약까지 같은 변경 단위로 검수한다.
8. README와 README 연결 Guide·Manual은 별도 산출물 Stream이므로 Source 불일치만 기록하고 임의 수정하지 않는다.

이 요청서는 전체 완료를 미리 선언하지 않는다. `cpf-docs/evidence/qa37/QA37_COMPLETION_STATE.json`의 모든 완료 조건이 exact result SHA에서 `완료/exitCode=0`이어야만 전체 완료다.

## 2. 먼저 읽을 정본

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
3. `cpf-docs/work/current/CPF_20260801_QA37_VERIFICATION_READINESS_REPORT.md`
4. `cpf-docs/work/current/CPF_20260801_QA37_SOURCE_READINESS_REPORT.md`
5. `cpf-docs/work/current/CPF_20260801_QA37_EDU_SOURCE_CLOSURE_AND_RECOVERY_REQUEST.md`
6. `cpf-reference/src/main/resources/edu/PACKAGE_INDEX.md`
7. `cpf-docs/quality/CPF_20260801_QA37_REQUIREMENT_RESULT_MATRIX.csv`
8. `cpf-docs/quality/CPF_20260801_QA37_MANUAL_EDU_135_COVERAGE_MATRIX.csv`
9. `cpf-docs/quality/CPF_20260801_QA37_EDU32_SOURCE_MAPPING.csv`
10. `cpf-docs/quality/CPF_20260801_QA37_ENVIRONMENT_VERIFICATION_MATRIX.csv`
11. `cpf-docs/work/manifest/CPF_20260801_QA37_DELETE_MANIFEST.txt`
12. 이 문서

## 3. Stage 0 — 선행 차단 Gate 한 번

Stage 0이 실패하면 Java 전체 Build, npm, DB, Runtime, Browser, Supply-chain을 시작하지 않는다. 공통 원인만 수정하고 Stage 0을 한 번 재실행한다.

```powershell
$root=(Get-Location).Path; git fetch origin master; $head=(git rev-parse HEAD).Trim(); $remote=(git rev-parse origin/master).Trim(); if($head -ne $remote){throw "HEAD/origin mismatch $head/$remote"}; if(git status --porcelain=v1 --untracked-files=all){throw 'Working Tree must be clean'}; git diff --check; python .\cpf-tools\scriptserify-cpf-qa37-source-closure.py --root .
```

Stage 0 한 번으로 확인할 항목:

- Root Build·Included Build Source·Java 25 계약
- 완료 오판 방지 Truth Gate
- 기능 중심 Package Layout: `online`, `batch`, `platform`, `optional/operations`, `optional/backoffice`, `optional/gateway`
- 숫자 ID Package·경로 0건
- EDU135 Source·Resource·5종 Test·실제 Consumer Binding
- `cpf-reference` 단일 Owner와 `refDB` Ownership
- 생성형 도메인·제품 BZA 실행 의존 0건
- Batch/Operations/Backoffice/Gateway 제거 경계와 역의존 0건
- Core 7 Table V93/U93와 Optional Batch 3 Table V94/U94의 3 Vendor 정적 Parity
- `CPF_EDU_*`, `CPF_REF_BAT_*`의 Generator Golden Template 유입 0건
- EDU32 실제 Source/Test/Public Contract Glob
- Frontend OpenAPI Lifecycle 정적 계약
- JSON·CSV·Secret·Repository Hygiene
- Delete Manifest·stale 문서 Review·Manifest 정합

### Stage 0 즉시 중단 조건

- EDU32가 Overlay Contract Mode로만 PASS하고 merged-root Mode가 실행되지 않음
- 135개 중 Consumer Binding 또는 등록된 Concrete Adapter가 없는 ID 존재
- 공통 Ledger만 호출하고 실제 JDBC·HTTP·File·Process·Outbox·Spring Batch·REF Gateway Consumer가 없는 ID 존재
- 생성형 도메인 또는 제품 BZA에 EDU가 연결됨
- Batch Off에서 필수 Online/Platform Source가 Batch Package나 `CPF_REF_BAT_*`를 참조함
- Matrix·보고서에 `미검증`이 남았는데 전체 완료를 선언함
- Delete Manifest가 정본·README·Guide·Manual을 포함함

## 4. Stage 1 — Java 25 Fresh Lifecycle 한 번

별도 Empty `GRADLE_USER_HOME`을 사용한다. `tasks`나 동일 전체 Build를 먼저 반복하지 않는다.

```powershell
$env:GRADLE_USER_HOME=Join-Path $env:TEMP ("cpf-gradle-qa37-"+[guid]::NewGuid()); .\gradlew.bat --no-daemon --stacktrace clean qa37JavaLifecycle
```

확인:

- Root Plugin/Convention/BOM Resolve
- Java 25 Toolchain
- 전체 Module Test·Assemble·isolated staging Publication
- `cpf-reference` Consumer Adapter·REF Counterparty·JDBC Store·30개 Spring Batch Job Compile
- Build 후 Working Tree Self-dirty 0건

실패 시 최초 Root Cause Module만 수정하고, 영향 Module Test 후 전체 Lifecycle은 마지막에 한 번만 재실행한다.

### Stage 1.1 — 선택 Pack 제거 Compile Matrix 한 번

```powershell
pwsh -NoProfile -File .\cpf-tools\scriptserify-cpf-reference-feature-removal.ps1 -Root .
```

검증 변형:

- Batch Off: `com.cpf.reference.batch`, Batch Job Resource, V94/U94, `CPF_REF_BAT_*` 미적용
- Operations Off
- Backoffice Off
- Gateway Simulator Off
- Core Only

각 변형은 별도 Build Directory를 사용해 이전 Class 잔존으로 PASS하지 못하게 한다.

## 5. Stage 2 — Frontend 회귀 검증 각 한 번

Node `22.18.x`, npm `10.9.2`를 사용한다. ADM/BZA는 EDU 의존성이 아니라 기존 제품 회귀 보호 대상이다.

```powershell
Push-Location .\cpf-adminrontend; npm ci; npm run verify; Pop-Location; Push-Location .\cpf-biz-adminrontend; npm ci; npm run verify; Pop-Location
```

각 `verify` 내부 순서만 사용하고 하위 Script를 별도로 반복하지 않는다.

`Source OpenAPI → Client 생성 → Generated Contract → Consumer → lint → typecheck → unit → production build`

## 6. Stage 3 — DB 3종 Lifecycle 각 Vendor 한 번

Oracle → PostgreSQL → MariaDB 순서로 한 Vendor씩 실행한다. 각 Vendor는 Fresh Volume에서 수행 후 제거한다.

### 3-A. Batch On

`Fresh Install → V93 Core Upgrade → V94 Batch Upgrade → Verify/Runtime Query → U94 Batch Rollback → U93 Core Rollback → V93/V94 Reapply → Verify`

필수 Table 10개:

- Core V93 7개: `CPF_EDU_OPERATION`, `CPF_EDU_TARGET`, `CPF_EDU_AUDIT`, `CPF_EDU_OUTBOX`, `CPF_EDU_BUSINESS_RECORD`, `CPF_EDU_LEASE`, `CPF_EDU_COUNTERPARTY_REQUEST`
- Optional Batch V94 3개: `CPF_REF_BAT_JOB_EXECUTION`, `CPF_REF_BAT_CHECKPOINT`, `CPF_REF_BAT_TARGET_RESULT`

### 3-B. Batch Off

`Fresh Install(Core only) → V93 Verify/Runtime Query → U93 Rollback → V93 Reapply`

- `CPF_REF_BAT_*` Table·Seed·Runtime Query가 없어야 한다.
- Online/Platform EDU는 정상 동작해야 한다.

각 Vendor에서 확인:

- Column·Key·Index·상태 의미 동일
- Checksum 일치와 Self-update 금지
- V94/U94 순서와 FK Drop 순서
- Same Idempotency Key/Same Hash Replay와 Different Hash Conflict
- Generated Domain은 기존 2개 Golden Table만 생성
- `CPF_EDU_*`, `CPF_REF_BAT_*` 생성 결과 0개

## 7. Stage 4 — Runtime 기능군별 한 번

필요한 Container만 기동하고 장애 주입 후 반드시 제거한다.

1. Online/JDBC: CRUD·Query·범위 권한·Optimistic Lock·Idempotency·Commit 전후 장애
2. REF Counterparty HTTP: 전송 전후 장애·202 UNKNOWN_RESULT·응답 유실·Retry·Reconcile
3. Messaging/Outbox: Publish·Inbox·DLQ·중복·재처리
4. File: 안전 경로·Checksum·부분 파일·Resume·암호화 경계
5. Batch On: 30개 Job/Step, `CPF_REF_BAT_*` Checkpoint, Restart·Partition·Remote Worker·Reconcile
6. Batch Off: Batch Bean·Route·SQL 없이 Online/Platform 기동
7. Optional Operations/Backoffice/Gateway: 각 On/Off와 REF DB/Simulator 동작
8. Multi-instance: Lease/Fencing·Process Kill·Unknown Result·Recovery
9. OTel: Log·Metric·Trace·Audit Correlation·민감정보 Masking

## 8. Stage 5 — Browser 한 번

Backend/Frontend Runtime 이후 ADM/BZA 기존 핵심 Route를 Chromium·Firefox·WebKit에서 한 번씩 검증한다. EDU는 제품 BZA에 의존하지 않으며, optional REF Backoffice/Gateway의 운영 링크는 미설치 시 숨김·404 계약을 검증한다. 401·403·404·409·429·500·503와 위험 조치 확인을 포함한다.

## 9. Stage 6 — Supply-chain 한 번

동일 Artifact를 대상으로 SBOM, Trivy, ORT/License, Secret, Artifact Hash를 한 번 실행한다.

## 10. Stage 7 — exact result SHA Evidence와 최종 판정

- `HEAD == origin/master == resultSha`
- Clean Working Tree
- 명령·환경·Tool Version·시작/종료·Exit Code
- Requirement·Scenario 연결
- Artifact SHA-256
- 민감정보 Sanitization

`QA37_COMPLETION_STATE.json`은 Evidence가 실제 존재할 때만 갱신한다. 실행하지 않은 검증을 `완료`로 바꾸지 않는다.

## 11. Source Defect와 Environment Blocker

### Source Defect

- Compile/Test 실패
- Consumer Binding/Bean/Adapter 누락
- 실제 Entry Point와 Binding 불일치
- DB 3종 의미 Drift 또는 V93/V94 Lifecycle 누락
- Generated Domain·제품 BZA EDU 결합
- Optional Pack 역방향 의존
- False Green Gate
- Runtime 상태·복구 계약 불일치

### Environment Blocker

- Java 25·Node 22.18·Docker·Browser·DB Image 부재
- Network·Registry·보안 정책 차단
- 자격정보·License Tool 부재

Blocker는 정확한 명령·오류·Exit Code를 남기며 성공으로 승격하지 않는다.

## 12. 최소 재검증 단위

- 문서·Matrix·Evidence 문구: Stage 0의 Truth/Evidence Contract만
- Package 이동·Binding: Stage 0 → `:cpf-reference:test`
- Batch Source: Stage 0 → Batch On/Off Compile → Batch Runtime Family
- Core DB Query: Stage 0 DB Parity → 3 Vendor V93 Lifecycle
- Batch DB Query: Stage 0 DB Parity → 3 Vendor V94/U94 + Batch Off
- Frontend: 영향 Frontend `npm ci && npm run verify` 한 번
- Runtime Adapter: 영향 기능군만
- 모든 수정 종료 후 exact SHA와 필요한 상위 Stage만 한 번 재확인

Codex는 기능을 임의 재설계하거나 EDU를 생성형 도메인·제품 BZA와 다시 연결하지 않는다.
