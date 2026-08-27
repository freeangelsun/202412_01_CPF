# CPF 다음 세션 상세 인수인계 — C 개발/QA 관리_1_8 — 2026-08-27

## 1. 현재 Source Authority

- 입력 Local Working Tree ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_121653.zip`
- 입력 ZIP SHA-256: `59297cc5afa5d8b1eb217332dfa3a205ceb5bbe24e6f20eef92b1b01c3d247f8`
- 입력 Product Source Identity: `7d25511f04c49952709489499ed637661649fe8673983302f088efc97f5c8304`
- 사용자 Full Runtime 수행 Source Identity: `b9aac7877adce6b7cce5a0ae556fcaa0b9f775a1f11c8268cebd9533244c9c09` / 8,435 files
- **현재 DevGPT Product Source Identity: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af` / 8,340 files**
- Runtime Source와 현재 Source가 다르므로 과거 Runtime PASS/CLOSED 자동승계 금지.
- Git은 Source Authority가 아니며 사용자 승인 없이 write/reset/restore/clean/history 변경 금지.

## 2. 이번 세션에서 재개방한 입력

- 사용자 Full Runtime: `PASS=129 / FAIL=18 / SKIP_ENV=1 / NOT_EXECUTED=7 / ExitCode=1`.
- VS Code Problems: `50 diagnostics / Error 18 / Warning 29 / 기타 3 / 30 resources`.
- 기존 `127/127 개발완료`를 Current PASS로 승계하지 않고 기존 WP-R00~R15 하위에 Runtime/VSCode/Harness Finding을 병합.
- Canonical Development/Closure Inventory는 **169행**으로 현행화.

## 3. 실제 Source/Verifier 보정

- Java/JDT: ADM Comparator, Policy Store generic inference, Batch Map import, Logging generic/Map.Entry, HTTP Header sanitizer 계약, fallback Comparator, LoggingAspect constructor/consumer drift, JDT nullness/unused/suppression/deprecation 패턴 보정.
- Security Context: 로컬 변수명 literal에 의존한 False Red를 semantic hashed-session/request-scope 검증으로 전환.
- Execution Scope: Repository 외 Temp Evidence를 `relative_to(root)`로 강제하던 결함을 external content identity 방식으로 보정.
- DB3: SQLPlus failure diagnostic/secret masking 의미, Oracle Seed logical mutation vs row-wise MERGE 의미, Spring Batch Oracle managed `BAT_SB_` sequence namespace 계약 보정.
- Batch: 5 role × Windows/Linux run/stop = 20 Shell 재구현. **Root `.gitignore`의 `bin/` 전역 규칙 예외를 추가하여 Git 추적 가능성까지 보정**하고 verifier/test로 재발방지.
- Frontend: CPF 1GB ceiling 안에서 frontend heap 1000MB, Node `>=22.18 <25`, npm `10.9.2` fail-closed.
- Final Runtime Harness: VS Code 0/0, Performance load/soak, Actual Open Git Fresh Release, mandatory SKIP/NOT_EXECUTED/UNKNOWN 차단, Managed exact diff, 동일 Source Fresh Replay를 mandatory로 currentize.
- UTF-8: PowerShell/child process 출력 경로 보정.
- Garbage: transient pytest/python/generated evidence/cache를 제품 Source에서 제거.

## 4. 현재까지 실제 검증

- NXT3 Final All: `23/23 PASS`, failed=0, unverified=0.
- DB Python: `228 passed / 2 environment skipped`.
- Testing Tools: fail 0; 분할 전수 실행 완료.
- Generator: `47 passed / 10 environment skipped / 6 subtests`.
- Release/Open Git contract: `53 passed`.
- Runtime Tools: `72 passed / 2 environment skipped / 7 subtests`.
- Batch Standalone/Profile: `5 roles / 20 shells / 15 profiles / 35 checks PASS`.
- Batch Shell Source/Git-ignore regression: PASS.
- Security Context semantic verifier: PASS.
- Execution Scope: PASS.
- Current Final/Hygiene/Garbage: PASS, ephemeral cache 0.
- VS Code verifier negative: 사용자 과거 dump `18E/29W`를 정확히 FAIL. positive 0E/0W PASS.
- Java25 Root Gradle: 현재 DevGPT 실행환경에 Gradle 9.1 distribution cache/외부망이 없어 `BLOCKED_EXTERNAL`; PASS로 기록하지 않음.

## 5. 반드시 남아 있는 Physical Acceptance

1. Windows Java25 Root clean build/test/publication/SBOM.
2. Fresh VS Code Java25/Gradle Import 후 Error 0 / Warning 0 JSON.
3. Oracle/PostgreSQL/MariaDB Fresh→Upgrade→Rollback→Reapply→Fault/UNKNOWN→Cleanup.
4. Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile.
5. One-WAS actual transaction.
6. File Log↔DB Log↔Transaction/Segment/Timeline correlation.
7. ADM/Backoffice Runtime OpenAPI.
8. Frontend lint/typecheck/test/build + Browser E2E/a11y/errors.
9. signed Performance Live + load/soak.
10. Actual Open Git Fresh Binary Release/Golden Path.
11. Source/Managed drift 0.
12. 동일 Source Fresh Replay.

하나라도 FAIL/SKIP_ENV/NOT_EXECUTED/unresolved UNKNOWN이면 Overall 완료 금지.

## 6. 다음 세션 시작 순서

1. `CPF_FINAL_DELIVERY_INDEX_20260827.md`를 먼저 읽는다.
2. `CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS_20260827.md`의 적용/저비용 검증을 수행한다.
3. Fresh VS Code Problems JSON을 만든 뒤 Required Full Runtime을 한 번 실행한다.
4. 실패는 `CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv`의 기존 Root Cause WP를 재개방한다.
5. Source가 바뀌면 Source Identity 재계산 후 과거 Runtime PASS 폐기.
6. Codex는 `CODEX_NEXT_WORK_INSTRUCTION_20260827.md`를 현재 Codex turn의 다음 입력으로 사용한다.
7. Codex가 Source를 수정하면 Final Runtime/Fresh Replay를 다시 실행한다.
8. mandatory PASS 이후에만 QA 재검수 요청.

## 7. 정본/결과물

- `CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv` — 단일 Developer Closure 원장.
- `REQUIREMENT_STATUS.csv` — 동일 행 상태 Mirror.
- `CPF_CURRENT_WORK_REQUEST.md` — 현재 개발 요청/상태.
- `CPF_DEVELOPMENT_COMPLETION_REVIEW_20260827.md` — 이번 개발 상세 완료 리뷰.
- `TEST_AND_EVIDENCE.md` + `cpf-docs/work/evidence/current/DEVGPT_C1_8_VALIDATION_EVIDENCE_20260827.log`.
- `OPEN_ISSUES.md` — 미검증/환경 Acceptance.
- `CHANGE_MANIFEST.csv`, `DELETE_MANIFEST.csv`, `PACKAGE_MANIFEST.json`, `SHA256SUMS.txt`.
- `CPF_DEVELOPMENT_HANDOVER.md` 및 본 파일.
- `CODEX_NEXT_WORK_INSTRUCTION_20260827.md`, `CODEX_RESULT_TO_NEXT_WORK_TRACE_20260827.md`.
- `CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS_20260827.md`.

## 8. Git/삭제 안전

- 사용자 승인 없는 commit/push/reset/restore/clean/stash/history 변경 금지.
- Batch Shell 20개는 이제 `.gitignore` 예외로 Git 추적 가능해야 하며, 실제 Local Git에서 ignore되면 Finding 재개방.
- 삭제는 `DELETE_MANIFEST.csv`에서 approved/user_approved/precondition/lifecycle 조건을 모두 만족하는 Root-relative 파일만 실행.
- 보호 경로/디렉터리 삭제/Root escape 금지.
