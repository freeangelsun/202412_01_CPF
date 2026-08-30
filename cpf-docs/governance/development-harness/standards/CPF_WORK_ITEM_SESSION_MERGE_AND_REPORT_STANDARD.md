# CPF Work Item · Session Report · Merge · Final Self Review Standard

> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `../CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

## 1. 목적

동시에 여러 AI/개발자가 작업해도 최종 상태가 덮어쓰기·누락·일괄 완료로 왜곡되지 않도록 **단일 Work Item Registry + 세션별 실행근거 + 자동 선행 Merge + QA Final Review** 흐름을 정의한다.

핵심은 다음 한 줄이다.

`CURRENT_WORK_ITEM_REGISTRY.csv(단일 작업대상 정본) ← 검증 Merge ← sessionKey별 Report/Evidence ← 실제 개발·검수 실행`.

세션 Report는 정본이 아니라 **검증 가능한 주장과 실행 근거**다. Current Registry에 Merge됐다고 해서 자동 PASS가 되는 것도 아니다.

## 2. sessionKey

개발·수정·독립검수·QA·Harness 통합 작업을 시작할 때마다 고유 `sessionKey`를 하나 생성한다.

권장 형식:

`<ROLE>-<UTC yyyyMMddTHHmmssZ>-<8자리 UUID>`

예: `CLAUDE-20260830T001530Z-8F31C2A4`

ROLE은 `DEVGPT`, `CLAUDE`, `CODEX`, `QA`, `HARNESS` 중 실제 역할을 사용한다. 같은 대화가 길게 이어져도 Source Identity가 바뀌거나 독립 실행 단위가 명확히 바뀌면 새 sessionKey를 발급한다. 임의 재사용하거나 다른 역할의 sessionKey를 복사하지 않는다.

## 3. 세션 Evidence 경로

각 역할은 자신의 경로만 사용한다.

`evidence/<role>/current/sessions/<sessionKey>/`

각 세션은 최소 다음을 가진다.

- `SESSION_REPORT.md` — 사람이 읽는 상세 개발·검수 리포트.
- `SESSION_MANIFEST.json` — sessionKey, role, started/ended, Source Identity, touched Work Item, Evidence 목록, merge 상태를 기계적으로 식별하기 위한 최소 메타데이터.
- 실제 로그/쿼리 결과/스크린샷 메타데이터/검증 결과 등 해당 세션 Evidence.

Evidence 파일명에 `final`, `latest`, `new`, `copy` 같은 의미 불명 이름을 사용하지 않고 Work Item/실행 목적이 드러나게 한다.

### 3.1 SESSION_MANIFEST.json 최소 Schema

`SESSION_MANIFEST.json`은 사람이 임의 문장으로 해석하지 않도록 최소 다음 필드를 구조화한다.

- `schemaVersion`
- `sessionKey` / `role`
- `startedAt` / `endedAt`
- `sourceIdentity` / `sourceBasis`
- `registrySha256AtStart`
- `reportPath` / `reportSha256`
- `workItems[]` — `workItemId`, 제안 상태, Evidence reference, Acceptance mapping. Acceptance mapping에는 `prerequisiteSource`, `requiredEnvironment`, `actualEnvironment`를 포함하며 `CLOSED/PASS` 제안 시 `finalSelfReview.complete=true`와 독립 Self Review 근거를 추가한다.
- `evidenceFiles[]` — path, sha256, purpose
- `gitWriteExecuted`
- `mergeStatus` — `UNMERGED/PARTIAL/MERGED/CONFLICT/REJECTED`
- `mergedBySessionKey` / `mergedAt` / `mergeTargetSourceIdentity`
- `pendingReasons[]` / `conflicts[]` / `rerunConditions[]`

작업 Agent가 자신의 Report 작성만으로 `MERGED`를 선언하지 않는다. `MERGED`는 후속 Merge writer가 실제 Current Registry 반영과 Evidence 검증을 끝낸 뒤 기록하는 상태다. `workItems[]`의 개수와 `SESSION_REPORT.md`의 Work Item 독립 Block 수가 일치하지 않으면 Merge 대상이 아니다.

## 4. SESSION_REPORT.md 필수 머리말

세션 Report에는 최소 다음이 있어야 한다.

- sessionKey
- 역할
- Source Identity 및 Source 기준(Git Working Tree/ZIP 등)
- 시작/종료 시각과 환경
- 이번 세션이 읽은 Current Registry 기준
- 시작 시 발견한 미Merge sessionKey
- 이번 세션에서 Merge한 sessionKey와 결과
- 이번 세션에서 실제 작업한 Work Item ID 전체
- Git write 수행 여부
- 새 Finding/Conflict
- 다음 재실행 조건

## 5. Work Item별 독립 Evidence Block

세션 Report에서 Work Item을 여러 개 묶어 `모두 동일`로 기록하지 않는다. **Work Item 한 건마다 독립 Block**을 작성한다.

각 Block 필수 필드:

1. Work Item ID / Requirement ID / Priority
2. 원 Requirement
3. Root Cause
4. Owner
5. 변경 전 영향범위
6. 실제 변경 Source
7. 실제 Consumer / 호출경로
8. Config / DB / Generator / API/OpenAPI / Frontend 영향
9. 정상 / 오류 / 경계 / 부분실패 / UNKNOWN
10. Retry / Recovery / Reconcile / Rollback 또는 N/A 근거
11. Security / Audit / Masking / Secret 영향
12. 수행 Test/Runtime 명령
13. 환경 — OS/host/tool versions뿐 아니라 **Current Source에서 읽은 prerequisite source, required 값, actual 값, MATCH/MISMATCH 판정**을 포함
14. started_at / ended_at
15. Exit Code
16. 실제 관찰 결과
17. Side Effect / Regression 결과
18. Evidence 경로와 SHA-256
19. Evidence Source Identity
20. 개발상태 / 검증상태 / Runtime상태 / 전체상태 제안
21. 완료 또는 미완료 사유
22. 재실행 조건

같은 실제 실행 로그가 여러 Work Item을 검증할 수는 있다. 그러나 각 Work Item Block에서 **그 공통 실행의 어떤 assertion/transaction/query/result가 해당 Work Item의 Acceptance를 증명하는지**를 별도로 연결해야 한다. 같은 로그 경로를 적었다는 이유만으로 여러 Work Item을 자동 PASS시키지 않는다.

## 6. 일괄 완료·일괄 SKIP 금지

다음 형태의 상태 변경을 금지한다.

- `WP-001~WP-050 CLOSED`
- `나머지 동일 PASS`
- `기존 실패라 전체 SKIP`
- `환경 문제로 Runtime 전체 제외`
- `Source 수정했으므로 관련 Finding 모두 완료`

각 Work Item마다 독립 Evidence Block이 없으면 해당 상태 변경은 Merge 대상이 아니다.

`SKIP`, `NOT_APPLICABLE`, `BLOCKED_EXTERNAL`, `NOT_EXECUTED`도 상태를 숨기는 수단이 아니다. 반드시 개별 Work Item의 이유, 실제 prerequisite, 재실행 명령/조건, 최종 완료에 미치는 영향을 기록한다.

## 7. 세션 시작 전 자동 Merge Preflight

**새 개발을 시작하기 전에** 작업자는 다음 절차를 수행해야 한다. 사용자가 매번 Merge를 지시할 필요가 없다.

1. `CPF_DEVELOPMENT_HARNESS.md`의 Current Merge Control State를 읽는다.
2. `evidence/*/current/sessions/*/SESSION_MANIFEST.json` 전체를 검색한다.
3. Current Merge Control State와 대조해 `UNMERGED`, `PARTIAL`, `CONFLICT`, 메타데이터 누락 세션을 찾는다.
4. 미Merge 세션이 있으면 새 기능 개발보다 먼저 해당 세션의 Report/Evidence를 읽고 Work Item별로 검증한다.
5. Merge 직전 `CURRENT_WORK_ITEM_REGISTRY.csv`의 SHA-256을 다시 계산해 Preflight에서 읽은 SHA와 비교한다. 중간에 다른 writer가 변경했으면 **쓰기 금지**하고 최신 Registry를 다시 읽어 Merge를 재계산한다.
6. 검증 가능한 사실만 `CURRENT_WORK_ITEM_REGISTRY.csv`와 관련 Role/Test/Evidence 상태에 Merge한다.
7. Merge 후 Registry SHA와 Source Identity를 기록하고 해당 session manifest의 merge 상태를 currentize한다.
8. 충돌이 있으면 마지막 작성자 우선으로 덮어쓰지 않고 `MERGE_CONFLICT`로 남긴 뒤 Root Cause를 해결한다.
9. Merge Control State를 갱신하고 Pending/Conflict가 해소됐는지 재검색한다.
10. Mandatory 미Merge/Conflict가 남아 있으면 신규 Work Item 개발을 시작하지 않는다. 허용되는 작업은 해당 Merge/Conflict 종결뿐이다.

이 Preflight는 Developer, Claude, Codex, QA 어느 역할이 시작하더라도 동일하다.

## 8. Merge Control State

Canonical `CPF_DEVELOPMENT_HARNESS.md`는 Merge Control 규칙을 소유하고, 가변 Current Merge Control State는 Source Identity 순환변경을 막기 위해 `current/CURRENT_MERGE_CONTROL_STATE.json` 한 파일에 유지한다.

필수 필드:

- `merge_protocol_version`
- `merge_baseline_source_identity`
- `last_merged_session_key`
- `merged_session_set_digest`
- `pending_session_keys`
- `conflict_session_keys`
- `last_merge_review_at`
- `last_merge_reviewer_session_key`

`last_merged_session_key`는 편의용 Watermark일 뿐 완전성 증명이 아니다. 병렬 세션과 순서 역전을 놓치지 않기 위해 **세션 전체 검색 + merged set digest + pending/conflict 목록**을 함께 사용한다.

세션 번호가 더 크다는 이유로 그 이전 세션이 모두 Merge됐다고 추정하지 않는다.

동시 수정 유실을 막기 위해 Merge writer는 **compare-before-write**를 적용한다. Merge 계산 시작 시 Registry SHA를 기록하고 실제 쓰기 직전에 다시 비교한다. SHA가 달라졌다면 자신의 계산 결과를 덮어쓰지 않고 최신 Registry에서 Preflight를 다시 수행한다. 파일 잠금이 없다는 이유로 마지막 writer 우선 정책을 사용하지 않는다.

## 9. Merge 판정 규칙

세션 Report의 상태는 주장이고 Current Harness 상태는 검증된 사실이다.

Merge 시 Work Item 한 건마다 다음을 확인한다.

- Registry에 동일 Requirement/WP가 존재하는가.
- Report가 Requirement 범위를 축소하지 않았는가.
- Root Cause가 기존과 같은가. 다르면 별도 Finding/Conflict인가.
- Source Identity가 현재 Source와 어떤 관계인가.
- 변경 Source와 Consumer가 실제로 존재하는가.
- Test/Runtime 명령과 Exit Code가 있는가.
- 실행 prerequisite의 required 값이 Current Source에서 파생됐고 actual 환경과 비교됐는가. 과거 세션의 숫자나 사용자 PC 값에 맞춘 expected 변경은 없는가.
- observed result가 Acceptance를 직접 증명하는가.
- Evidence 파일과 SHA가 일치하는가.
- Side Effect/Regression이 확인됐는가.
- 역할 권한을 넘어서 다른 역할의 PASS를 선언하지 않았는가.

Source Identity가 변경됐다면 과거 Evidence는 provenance로 Merge할 수 있지만 현재 PASS 근거로 자동 승계하지 않는다. 영향받는 Work Item은 필요한 상태로 재개방한다.

## 10. 병렬 세션과 충돌

같은 Work Item을 여러 세션이 작업했다면 시간순 덮어쓰기를 금지한다.

예:

`Claude A SOURCE_FIXED → Codex B Finding/FAIL → Claude C 재수정/PASS → Independent 재검증 → QA Acceptance`.

최종 상태는 Lifecycle과 현재 Source Identity 기준으로 계산한다.

다음은 자동 Merge 금지다.

- 서로 다른 Root Cause
- Architecture/Product Contract/Owner/Public API/SPI 해석 충돌
- 한쪽 PASS, 다른 쪽 FAIL
- 서로 다른 Source Identity를 같은 PASS로 취급
- Evidence 없는 대량 상태 변경
- Requirement 범위를 줄인 Report

이 경우 `MERGE_CONFLICT`로 남기고 충돌 해결을 현재 최우선 Work Item으로 취급한다.

## 11. 역할과 Merge

- Developer/Claude/Codex는 자신의 실제 개발·검수 사실과 Evidence를 Merge할 수 있지만 다른 역할의 PASS를 대신 만들 수 없다.
- 다음 세션 작업자는 역할과 관계없이 **이전 미Merge 세션 탐색·통합 책임**을 가진다.
- QA는 Final Acceptance 전에 Pending/Conflict session=0, Current Registry와 Session Evidence parity를 다시 검증한다.
- 동시에 두 세션이 `CURRENT_WORK_ITEM_REGISTRY.csv`를 수정하지 않는다. 동시 작업이 감지되면 각 세션은 자신의 Evidence 영역에만 기록하고 다음 Merge Preflight에서 순차 통합한다.

즉 중앙 담당자를 매번 수동 지정하지 않지만, **한 시점의 Current Registry Merge writer는 하나**다.

## 12. 세션 Handover 필수 항목

모든 세션 인수인계에는 다음을 반드시 포함한다.

- 현재 sessionKey
- Current Source Identity
- 현재 Merge Control State 전체
- 이번 세션이 Merge한 sessionKey
- 발견했지만 아직 Merge되지 않은 sessionKey
- MERGE_CONFLICT sessionKey와 이유
- 이번 세션 Work Item별 상태
- SOURCE_FIXED/VERIFICATION_PENDING/BLOCKED_EXTERNAL/NOT_EXECUTED 항목
- 마지막 실제 Test/Runtime 명령과 결과
- Evidence 경로
- 다음 세션이 가장 먼저 해야 할 Merge Preflight와 정확한 시작 Work Item

`다음 세션은 알아서 확인` 같은 모호한 Handover를 금지한다.

## 13. 최종 Self Review Hard Gate

최종 완료 직전에 전체 Mandatory Work Item을 **한 건씩** 다시 리뷰한다. 전체 개수를 한 문장으로 요약해 PASS시키지 않는다.

각 Work Item에 대해 다음 순서로 확인한다.

`원 Requirement → Root Cause → Owner → Source → Consumer/호출경로 → Config/DB/Generator/API/Frontend → 오류/복구 → Test → Runtime → Regression → Evidence/SHA → Source Identity → 역할별 상태 → 완료사유`.

한 건이라도 다음 상태면 Final PASS 금지:

- Evidence 없음/불일치
- 현재 Source Identity와 불일치
- mandatory Test/Runtime 미실행
- `READY/PLANNED/SKIP/NOT_EXECUTED/UNKNOWN/VERIFICATION_PENDING/BLOCKED_EXTERNAL`
- 미Merge Session 존재
- MERGE_CONFLICT 존재
- QA 미검증

Final Self Review 결과는 한 파일 안에서 Work Item별 독립 Section을 유지한다. `410건 모두 PASS` 같은 집계만으로 대체하지 않는다.

## 14. Current-only와 세션 Evidence 보존

세션 Report/Evidence는 현재 Registry의 완료 근거가 실제 참조하는 동안 보존한다. 그러나 다음은 Garbage 대상이다.

- 잘못 생성된/중복 sessionKey
- 현재 Registry와 연결되지 않는 임시 Report
- superseded Source Identity의 중간 debug 파일
- 동일 근거의 불필요한 copy/backup
- Merge가 거부된 뒤 더 이상 provenance 가치가 없는 임시 산출물

과거 Evidence를 별도 개발 정본으로 승격하지 않는다. 현재 작업상태를 파악하기 위해 세션 폴더를 사람이 조립해야 하는 구조도 금지한다. 현재 상태는 항상 `CURRENT_WORK_ITEM_REGISTRY.csv` 하나에서 시작하고, 상세 근거가 필요할 때만 session Evidence를 따라간다.
