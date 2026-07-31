# CPF QA33 Push 후 최종 검증·결함수리 요청서

## 1. 작업 목적

최신 `master`에 반영된 QA33 Source를 실제 Post-Push exact SHA 기준으로 재검증하고, 현재 확인된 Release Gate·Frontend SHA·Evidence·Consumer 이관 결함을 수정한 뒤 Java 25, ADM/BZA, 3DB, Kafka, Gateway, Batch, Scheduler, Deployment, Agent, Supply-chain을 실행 검증한다.

이 요청은 단순 검수 요청이 아니다. 검증 중 발견되는 Source·SQL·Test·Script·Config·Matrix·Evidence 결함은 Owner Module에서 함께 수정하고, 실행하지 않은 항목을 완료로 기록하지 않는다.

## 2. 시작 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 요청서 작성 시 최신 SHA: `da491b3f5210e36efc63a7a627ad07c9481fac63`
- 최신 Commit Message: `20260731_09`
- 상세 리뷰: `cpf-docs/work/review/CPF_20260731_QA33_POST_PUSH_FINAL_REVIEW.md`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

작업 시작 시 반드시 실제 `HEAD`, `origin/master`, Working Tree를 다시 확인하고 요청서 SHA와 다르면 최신 Commit Diff를 먼저 검토한다.

```powershell
git fetch origin master; git rev-parse HEAD; git rev-parse origin/master; git status --porcelain=v1
```

사용자 승인 없이 Commit, Push, Branch, Tag, PR, Release를 생성하지 않는다.

## 3. 필수 읽기 순서

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
3. `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`
4. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
5. `cpf-docs/work/current/CPF_20260731_QA33_PACKAGE_INDEX.md`
6. `cpf-docs/work/current/CPF_20260731_QA33_DEVELOPMENT_AND_VERIFICATION_REQUEST.md`
7. `cpf-docs/work/review/CPF_20260731_QA33_POST_PUSH_FINAL_REVIEW.md`
8. `cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv`
9. `cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv`
10. `cpf-docs/quality/CPF_20260731_QA33_RESULT_MATRIX.csv`
11. `cpf-docs/quality/CPF_20260731_QA33_UNRESOLVED_REGISTER.csv`
12. `cpf-docs/evidence/current/qa33-development/CPF_20260731_QA33_FINAL_SOURCE_VALIDATION.sanitized.json`

## 4. P0-1 — Post-Push Baseline·정본 재설정

다음 자료가 Overlay 기준 SHA `c1f273f...`에 머물러 있다. 최신 Push 검증 결과로 자동 치환하지 말고, 먼저 역할을 구분한다.

- 과거 Overlay Evidence/Manifest: 당시 기록으로 `history` 보존
- 현재 실행 정본: 최신 HEAD 기반으로 신규 생성
- Git 추적 Source Marker: Commit SHA 자기참조 제거
- Post-Commit Evidence: 최신 HEAD와 Artifact Hash 기록

필수 수정 대상:

- `CPF_CURRENT_WORK_REQUEST.md`
- `CPF_NEXT_WORK_REQUEST.md`
- QA33 Current Handover
- `CPF_CODEX_CONTINUITY_STATE.md`
- QA33 Final Status
- Result/Unresolved Matrix
- Evidence Index 및 Post-Push Evidence

## 5. P0-2 — Release Gate Self-Dirty 수정

현재 `verify-cpf-qa33-all.ps1`, `verify-cpf-qa33-runtime.ps1`, `verify-cpf-qa33-result-coverage-v2.py`의 순서를 아래 원칙으로 재구성한다.

1. 최초 HEAD와 Clean Source Tree를 검증한다.
2. 실행 중 Report/Evidence는 Git 비추적 staging 경로에 생성한다.
3. Source Integrity, QA32 Regression, Java/Frontend/DB/Kafka/Multi-instance/Supply-chain을 수행한다.
4. 최종 HEAD가 최초 HEAD와 같은지 검증한다.
5. Source 경로가 변경되지 않았는지 검증한다.
6. Requirement/Scenario Evidence Index를 staging 결과로 검증한다.
7. 모든 검증 완료 후 sanitized 정본 Evidence를 `cpf-docs/evidence/current`로 승격한다.

필수 Negative Test:

- 선행 Evidence 파일이 변경되어도 Source Clean 검사와 혼동하지 않아야 한다.
- Source 파일 변경은 반드시 실패해야 한다.
- timestamp Evidence 생성만으로 Release가 실패해서는 안 된다.
- `-Release -SkipExternalTools`는 시작 즉시 실패해야 한다.
- Evidence 승격 도중 실패하면 이전 정본을 보존하고 부분 파일을 남기지 않아야 한다.

## 6. P0-3 — Frontend exact-SHA 계약 재설계

### 6.1 Git 추적 Marker

Git에 추적되는 Marker에는 다음만 둔다.

- OpenAPI Snapshot SHA-256
- Orval Config SHA-256
- Generator Version
- Generated File별 SHA-256 또는 Directory Digest
- Lockfile SHA-256

현재 Commit SHA를 Git 추적 Marker 안에 기록하지 않는다.

### 6.2 Post-Commit Frontend Evidence

Commit 후 검증 단계에서 다음을 하나의 Evidence로 기록한다.

- `sourceSha = git rev-parse HEAD`
- Node/npm exact version
- `package-lock.json` SHA-256
- OpenAPI SHA-256
- 실제 Orval Generated Client SHA-256
- Production `dist` SHA-256
- Chromium/Firefox/WebKit 결과와 Report SHA-256
- 명령, Registry/Profile, 시작·종료 시각, exitCode, sanitized

### 6.3 Validator 통일

- `verify-generated-client.mjs`
- `write-generated-marker.mjs`
- `prepare-cpf-qa33-frontend.ps1`
- `verify-cpf-qa33-frontend-closure.py`
- Result Coverage Release Validator

위 도구가 하나의 Marker/Evidence Schema를 사용하도록 통일한다.

필수 Negative Fixture:

- stale OpenAPI Hash
- stale Generated Client Hash
- 다른 Lockfile Hash
- 다른 Git HEAD Evidence
- Browser 하나 누락
- dist 변경
- custom bootstrap generator를 Orval로 위장

## 7. P0-4 — 실제 Orval·TanStack Query Consumer 이관

Bootstrap Generator는 최종 제품 Generated Client로 인정하지 않는다.

필수 작업:

- 승인 Registry clean install 후 실제 Orval 실행
- ADM/BZA OpenAPI Snapshot을 실제 Controller 계약과 동기화
- 생성 Client를 실제 제품 API 범위로 확장
- ADM 핵심 `admQuery/admMutation` URL Wrapper Consumer를 Generated Operation + TanStack Query/Mutation Hook으로 이관
- BZA 인증 Bootstrap 예외를 최소화하고 일반 API를 Query/Mutation Hook으로 이관
- Query Key, invalidation, retry, cancellation, stale time, optimistic update/rollback 기준 정의
- 401/403/409/5xx/timeout/network error를 구분
- BZA `restoreBzaSession()`이 401 외 장애를 단순 logout으로 숨기지 않도록 수정
- Legacy Wrapper와 실제 Consumer 없는 Dependency 제거

재분류 검토 대상:

- `QA33-REQ-026`
- `QA33-REQ-027`
- `QA33-REQ-028`
- `QA33-REQ-029`
- 관련 12개 Mandatory Scenario

## 8. P0-5 — Evidence·Matrix 계약 재구축

현재 하나의 Development Evidence를 다수 Requirement/Scenario Runtime 완료 근거로 사용하지 않는다.

다음 중 하나의 정본 구조를 선택하고 Gate와 일치시킨다.

- Requirement/Scenario별 Evidence 파일
- 하나의 Evidence Bundle + 각 Record별 Entry와 Artifact Mapping

각 완료 Entry 필수 필드:

- recordType, recordId
- sourceSha, resultSha 또는 Artifact Digest
- command/commands
- profile/environment
- startedAt/finishedAt
- exitCode
- artifacts 배열과 SHA-256
- sanitized
- requirement/scenario가 요구한 정상·오류·경계·부분 실패·동시성·복구 결과

`verification_status=완료`는 해당 Entry가 Release Validator를 실제 통과한 뒤에만 기록한다.

재분류 우선 검토:

- `QA33-REQ-001`
- `QA33-REQ-002`
- `QA33-REQ-004`
- `QA33-REQ-017`
- `QA33-REQ-018`
- `QA33-REQ-026`
- `QA33-REQ-027`
- `QA33-REQ-028`
- `QA33-REQ-029`
- `QA33-REQ-120`
- 관련 Scenario

전체 완료/부분 구현 수치는 재산정한다. 기존 135/138 수치를 그대로 승계하지 않는다.

## 9. P0-6 — 실제 실행 검증

### 9.1 Frontend

ADM/BZA 각각:

```powershell
npm ci --no-audit --no-fund
npm run generate:api
npm run lint
npm run typecheck
npm run test
npm run build
npx --no-install playwright test --project=chromium
npx --no-install playwright test --project=firefox
npx --no-install playwright test --project=webkit
```

검증 후 Generated Source Drift, Lockfile Drift, Bundle Hash, Browser Report를 Evidence에 기록한다.

### 9.2 Java 25·Gradle

- 전체 Compile/Test
- Included Build Platform BOM/Convention Plugin
- Published POM/Resolved Graph
- Generated Domain Golden Template
- QA31/QA32 Regression
- Packaging/Install/Upgrade/Rollback 관련 Task

### 9.3 DB 3종

Oracle, PostgreSQL, MariaDB 각각:

- Fresh Install
- V86~V91 Migration
- selective rollback
- reapply
- checksum
- schema drift
- concurrent claim/idempotency/fencing
- unsupported vendor fail-closed

### 9.4 Kafka·Gateway·Batch·Scheduler·Deployment·Agent

- duplicate
- timeout
- response loss
- process kill
- retry
- unknown result reconcile
- latest fencing
- multi-manager/multi-worker
- outbox claim/recovery
- DLT/backpressure
- Agent command replay/revocation/atomic activate/rollback
- Gateway audit/ledger spool capacity·recovery

### 9.5 Supply-chain

`-SkipExternalTools` 없이 CycloneDX, ORT, Syft, Grype와 Final Artifact Hash를 실행한다.

## 10. P1 — Repository 정리

- `cpf-docs/work/current`에는 현재 정본만 남긴다.
- QA29·QA31·QA32 과거 Request/Report는 history로 이동한다.
- `.gitignore`는 `cpf-tools/build` Source를 보호하면서 신규 Module의 Gradle Output 재발을 차단한다.
- Windows 전용 `.bat`/`.cmd` 실행기는 OS별 Runner 선택으로 보완한다.
- Build, node_modules, dist, Playwright Report, logs, tmp, stale Evidence를 Hygiene Gate로 차단한다.

## 11. 최종 Gate 순서

```powershell
python cpf-tools/scripts/verify-cpf-qa33-request-integrity.py --root .
python cpf-tools/scripts/verify-cpf-final-target-document-consistency.py --root .
pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa33-all.ps1 -Root . -SkipExternalTools
pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa33-all.ps1 -Root . -Release
```

마지막 두 명령이 같은 exact HEAD에서 실제 통과해야 한다. Development Gate와 Release Gate 각각의 Evidence Hash를 기록한다.

## 12. 필수 산출물

- 변경 Source/SQL/Test/Script/Config
- 최신 QA33 Result Matrix
- 최신 Unresolved Register
- Requirement/Scenario Evidence Index
- Frontend Post-Commit Evidence
- Java/3DB/Kafka/Multi-instance/Supply-chain Evidence
- Post-Push Completion Report
- Independent Self Review
- Current Request/Next Request/Handover/Continuity
- Root-relative Overlay ZIP
- ZIP SHA-256, 파일 수, 삭제 수
- Commit/Push 수행 여부

## 13. 완료 처리 금지 조건

- Post-Push exact SHA가 아닌 Overlay SHA Evidence
- Release Gate Self-Dirty 미해결
- Git 추적 Marker에 현재 Commit SHA 자기참조
- stale Marker 기본 PASS
- 실제 Orval 대신 Bootstrap Generator
- TanStack Dependency만 있고 실제 Hook Consumer 없음
- 하나의 Development Evidence로 다수 Runtime 완료 처리
- clean npm/3 Browser/Java25/3DB/Kafka/Multi-instance/Supply-chain 미실행
- `-Release -SkipExternalTools` 허용
- Current에 과거 요청 누적
- 실행하지 않은 검증을 성공으로 기록

모든 항목이 실제 실행과 exact-SHA Evidence로 닫힌 뒤에만 `완료`로 판정한다.
