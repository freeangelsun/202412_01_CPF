# CPF QA33 Push 후 최종 재검수 리뷰

## 1. 검수 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 최신 확인 SHA: `da491b3f5210e36efc63a7a627ad07c9481fac63`
- 최신 Commit Message: `20260731_09`
- 직전 Commit: `e263526c1a15390206eeeeadf984a3ceb7145ecf` (`.gitignore` 정리)
- QA33 개발 Overlay 작성 기준 SHA: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`
- 검수 범위: 최신 Push 반영 여부, Post-Push exact-SHA 정합성, Release Gate 실행 가능성, Frontend Generated Client/Lock/Bundle 계약, Result/Evidence 계약, Current/Handover 정본 상태
- README와 Guide 내용은 이번 검수 범위에서 제외했다.

## 2. 총평

대규모 QA33 Source 반영 자체는 최신 `master`에 적용되었다. Build Tooling, Batch/Kafka/Scheduler, Gateway, BFF Security, Deployment/Agent, Archive, 3개 DB Vendor, ADM/BZA Frontend, Gate와 Evidence 관련 변경이 실제 Commit에 포함되어 있다.

그러나 Push 후 exact-SHA 재검증 관점에서는 **QA33 완료 판정이 불가능**하다. 단순히 Java 25, DB, Kafka, Browser 환경만 연결하면 끝나는 상태가 아니라, 먼저 아래 P0 구조 결함을 수정해야 한다.

1. Evidence·Manifest·Current·Handover·Frontend Marker가 최신 HEAD가 아닌 Overlay 기준 SHA를 계속 가리킨다.
2. Release Gate가 Evidence를 생성하면서 동시에 Clean Working Tree를 요구하여 정상 실행에서도 자기 자신 때문에 실패하는 순서 충돌이 있다.
3. Frontend Source SHA를 Git에 추적되는 파일 안에 현재 Commit SHA로 기록하려는 계약은 Commit SHA 자기참조 때문에 지속적으로 만족시킬 수 없다.
4. 기본 Generated Client 검증은 stale SHA를 PASS시키고, Repository Frontend Gate는 반대로 현재 Marker Schema와 맞지 않아 실제 적용 저장소에서 실패한다.
5. Result Matrix의 다수 행이 Release Validator 계약과 호환되지 않는 하나의 Development Evidence를 공동 참조한다.
6. Orval/TanStack Query 이관으로 완료 표시된 일부 항목은 실제 Source가 그 완료 조건을 충족하지 않는다.

따라서 현재 공식 판정은 다음과 같다.

- Source 반영: **부분 구현**
- Post-Push Development Gate: **재확인 필요**
- Release Gate: **실패 예상 / 구조 수정 선행 필요**
- Runtime·3DB·Kafka·3 Browser·Multi-instance·Supply-chain: **미검증**
- GA: **미완료**

## 3. 정상 반영 확인 사항

- 최신 대규모 Commit은 301개 파일을 변경했고, 직전 `.gitignore` Commit까지 포함해 QA33 Overlay가 `master`에 반영되었다.
- ADM/BZA `package-lock.json`은 각각 792 Package 후보를 포함한다.
- ADM/BZA Marker 내부 OpenAPI/Generated File Hash 자체 일관성 검사는 통과한다.
- QA33 Development Evidence에는 실행하지 않은 Java 25, clean npm, Playwright, 3DB, Kafka, Multi-instance 검증을 성공으로 기록하지 않았다.
- README·Guide 파일을 QA33 Source Overlay에 포함하지 않는 정책은 유지되었다.

## 4. P0 결함

### P0-01. Post-Push exact-SHA 정본 불일치

최신 HEAD는 `da491b3f...`이나 아래 정본은 계속 `c1f273f...`를 기준으로 한다.

- `cpf-docs/evidence/current/qa33-development/CPF_20260731_QA33_FINAL_SOURCE_VALIDATION.sanitized.json`
- `cpf-docs/work/review/CPF_20260731_QA33_FINAL_STATUS.json`
- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/handover/CPF_20260731_QA33_DEVELOPMENT_HANDOVER.md`
- `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
- `cpf-docs/work/manifest/CPF_20260731_QA33_COMPLETION_OVERLAY_MANIFEST.json`
- ADM/BZA `openapi/cpf-openapi.json`
- ADM/BZA `src/generated/.cpf-openapi-source.json`
- ADM/BZA `src/generated/source-sha.json`

`CPF_CODEX_CONTINUITY_STATE.md`는 사용자 Commit/Push가 수행되지 않았다고 기록하고 있어 현재 Git 상태와도 다르다.

**판정:** `QA33-REQ-001` 완료 유지 불가. 최소 `부분 구현`으로 재분류해야 한다.

### P0-02. Release Gate의 Self-Dirty 순서 충돌

`verify-cpf-qa33-all.ps1`은 Runtime Gate 전에 Source/Batch/DB/Repository/Frontend Evidence JSON을 `cpf-docs/evidence/current`에 생성한다. `.gitignore`는 `*.sanitized.json`을 명시적으로 Git 추적 대상으로 허용한다.

그 다음 `verify-cpf-qa33-runtime.ps1`은 시작 시 Clean Working Tree를 강제한다. 앞 단계에서 생성·변경한 Evidence 때문에 정상 흐름에서도 Dirty Tree가 될 수 있다.

Runtime Gate는 종료 시 timestamp가 포함된 `qa33-runtime-evidence.sanitized.json`을 기록한다. 이후 `verify-cpf-qa33-result-coverage-v2.py --release`가 다시 Clean Working Tree를 요구하므로, Runtime Evidence가 생성된 정상 실행도 실패할 수 있다.

**필수 수정:**

- 실행 중 Evidence는 Git 비추적 staging 디렉터리에 생성한다.
- Source Clean 검사는 Evidence 생성 전에 한 번 수행한다.
- Runtime 및 Result 검증을 모두 통과한 뒤 최종 Evidence를 정본 경로로 승격한다.
- Source 변경 여부는 Evidence 경로를 제외한 Source Tree Hash 또는 최초 HEAD와 최종 HEAD로 검증한다.
- `-Release`와 `-SkipExternalTools` 동시 사용은 금지한다.

### P0-03. Frontend Git SHA 자기참조 계약

현재 `prepare-cpf-qa33-frontend.ps1`은 실행 시작 HEAD를 `source-sha.json`과 Generated Marker에 기록한다. 이 파일들을 Commit하면 Commit SHA가 새로 바뀌므로 기록된 SHA는 즉시 이전 Commit이 된다. 다시 최신 SHA를 기록하면 파일이 바뀌어 또 다른 Commit SHA가 만들어진다.

즉, Git에 추적되는 파일이 자신을 포함한 현재 Commit SHA와 같아야 한다는 계약은 일반적인 Commit 절차로 지속 충족할 수 없다.

**필수 재설계:**

- Git 추적 Generated Marker에는 OpenAPI Hash, Generator Config Hash, Generated File Hash만 기록한다.
- exact Git Source SHA는 Commit 후 CI/검증 단계에서 생성하는 외부 Evidence 또는 최종 Artifact Manifest에 기록한다.
- Release Gate는 `HEAD + package-lock hash + generated client hash + browser bundle hash + browser result hash`를 하나의 Post-Commit Evidence로 묶어 검증한다.
- Source Tree 내부 Marker와 Post-Commit Evidence의 역할을 분리한다.

### P0-04. Generated Client Gate의 False Green과 Schema 충돌

실제 교차 실행 결과:

- 기본 실행: `node scripts/verify-generated-client.mjs` → stale `c1f273f...` Marker를 PASS
- 최신 HEAD 강제: `CPF_SOURCE_SHA=da491b3f... node scripts/verify-generated-client.mjs` → SHA 불일치로 FAIL

`npm run verify`는 `CPF_SOURCE_SHA`를 강제하지 않으므로 기본 검증은 stale Marker를 놓친다.

반대로 `verify-cpf-qa33-frontend-closure.py`의 non-overlay 경로는 `src/generated/source-sha.json`에 `openApiSha256`, `generatedClientSha256`, `sanitized`가 있다고 가정한다. 현재 Commit의 `source-sha.json`에는 `sourceSha`만 있어 Post-Push Development Gate에서도 실패한다.

**판정:** Frontend Gate는 한쪽은 false green, 다른 쪽은 fail-always 성격을 동시에 가진다.

### P0-05. Result Matrix와 Release Evidence 계약 불일치

`CPF_20260731_QA33_RESULT_MATRIX.csv` 552행 중 377행이 하나의 Development Evidence를 참조하며, 151행은 `verification_status=완료`로 표시되어 있다.

Release Validator는 Evidence에 아래를 요구한다.

- `sourceSha == HEAD`
- `exitCode == 0`
- `command` 또는 `commands`
- 비어 있지 않은 `artifacts` 배열과 각 Artifact SHA-256
- `sanitized == true`

현재 Development Evidence는 다음 상태다.

- `sourceSha`: 이전 SHA
- top-level `exitCode`: 없음
- top-level `command/commands`: 없음
- `artifacts`: 배열이 아니라 객체

따라서 현재 Matrix의 완료 151행은 Release Validator로 완료 증명될 수 없다.

**판정:** `QA33-REQ-002`, `QA33-REQ-004` 및 관련 Evidence/Scenario 완료 판정을 재검토해야 한다.

### P0-06. Orval/TanStack Query 완료 판정 과대

현재 Commit의 ADM Generated Client는 인증 Operation 2개, BZA는 인증 Operation 4개만 포함한다. Marker의 Generator는 실제 Orval 결과가 아니라 `scripts/generate-checked-client.mjs` Bootstrap Generator다.

ADM 핵심 기능은 여전히 `admMutation(...)` 같은 imperative URL Wrapper를 사용한다. 변경 Frontend Source에서 `useQuery`, `useMutation`, `QueryClient` 실제 Consumer는 확인되지 않았다. BZA Session도 Generated Function을 직접 호출하며 TanStack Query Hook으로 이관되지 않았다.

따라서 아래 Requirement의 `development_status=완료`는 유지하기 어렵다.

- `QA33-REQ-026`: OpenAPI exact SHA Client 생성 및 diff 0
- `QA33-REQ-027`: BZA exact-SHA Client 및 Consumer 이관
- `QA33-REQ-028`: Orval Mutator 외 raw API 제거 및 Query/Mutation Hook 이관
- `QA33-REQ-029`: 일반 API Orval/TanStack Query 이관

기존 부분 구현 3건 외에 최소 위 항목과 `QA33-REQ-001`, Evidence 관련 항목을 추가 재분류해야 한다. 전체 135/138 완료 수치는 재산정 전까지 정본 완료율로 사용하지 않는다.

## 5. P1 보완 사항

### P1-01. BZA Session 복구 오류 구분

`restoreBzaSession()`은 모든 오류를 catch하여 Session을 지운다. 401/403과 5xx·Network Timeout·Backend 장애를 분리하지 못해 운영 장애가 단순 미로그인으로 보일 수 있다.

- 401/419: Session 만료 처리
- 403: 권한 오류 처리
- 5xx/Timeout/Network: Session을 성급히 폐기하지 않고 장애 상태 표시 및 재시도 정책 적용

### P1-02. `.gitignore` Build Output 확장성

현재 Build Output ignore가 Module allowlist 방식이다. 신규 공식 Module 또는 Generator 산출 Module이 추가되면 `build/`가 Git에 노출될 수 있다.

`cpf-tools/build` Source Owner 예외를 유지하면서도 일반 Module의 중첩 `build/`를 자동 차단하는 재발 방지 Gate가 필요하다.

### P1-03. Runtime Gate 플랫폼 종속

PowerShell Script가 `gradlew.bat`, `npm.cmd`, `npx.cmd`를 직접 사용해 Windows에 종속된다. Windows 정본 실행을 유지하더라도 Linux CI 검증 경로 또는 OS별 실행기 선택이 필요하다.

### P1-04. Current 문서 정리

`cpf-docs/work/current`에 약 32개 문서가 남아 있고 QA29·QA31·QA32의 과거 Request/Report가 함께 존재한다. Current에는 현재 수행할 정본만 남기고 과거 문서는 `history` 또는 `review/history`로 이관해야 한다.

## 6. 필수 다음 작업 순서

1. 최신 SHA `da491b3f...`에서 Working Tree와 정본 Pointer를 확정한다.
2. Release Gate Self-Dirty와 Evidence Staging 구조를 먼저 수정한다.
3. Frontend Source SHA 자기참조를 제거하고 Post-Commit Artifact Evidence 계약으로 재설계한다.
4. Frontend Closure/Generated Client Validator의 Schema를 하나로 통일하고 Negative Fixture를 추가한다.
5. 실제 Orval Client를 생성하고 ADM/BZA 일반 API Consumer를 TanStack Query/Mutation으로 이관한다.
6. Result/Unresolved Matrix를 재분류하고 Evidence Index를 Release Validator 계약과 일치시킨다.
7. 승인 Registry에서 ADM/BZA clean `npm ci`, lint, typecheck, unit, build, Chromium/Firefox/WebKit을 실행한다.
8. Java 25 전체 Gradle Compile/Test와 QA32 Regression을 실행하고 발견 결함을 수정한다.
9. Oracle/PostgreSQL/MariaDB Migration→Rollback→Reapply와 Drift를 실행 검증한다.
10. Kafka·Gateway·Batch·Scheduler·Deployment·Agent Multi-instance 장애·복구를 실행한다.
11. Supply-chain 전체 검증을 Skip 없이 수행한다.
12. 모든 Evidence를 최신 exact SHA와 Artifact Hash로 재생성한 뒤 Matrix를 완료로 갱신한다.
13. Current/Handover/Continuity/Next Request를 최신 상태로 정리하고 Repository Hygiene를 통과시킨다.

## 7. 완료 판정 금지 조건

아래 중 하나라도 남으면 QA33 또는 GA 완료로 기록하지 않는다.

- 최신 HEAD와 Evidence/Manifest/Marker의 SHA 역할이 불명확함
- Release Gate가 자기 생성 Evidence 때문에 Dirty Tree로 실패함
- `-Release -SkipExternalTools`가 허용됨
- Bootstrap Generated Client를 Orval 완료로 기록함
- URL Wrapper를 TanStack Query/Mutation Hook 완료로 기록함
- 하나의 Development Evidence를 다수 Requirement/Scenario Runtime 완료 근거로 재사용함
- 3DB/Kafka/Browser/Multi-instance/Supply-chain을 실행하지 않음
- 실행하지 않은 Test를 PASS로 기록함
- Current에 superseded Request가 계속 누적됨

## 8. 최종 리뷰 판정

- 최신 Push 반영: **확인**
- Overlay 정적 반영 품질: **상당 부분 개선됨**
- Post-Push exact-SHA 정합성: **실패**
- Development Gate 실제 저장소 실행 가능성: **재확인 필요**
- Release Gate 실행 가능성: **구조 결함으로 선행 수정 필요**
- 다음 작업: **필수**
