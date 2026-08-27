# CPF Current Work Request — C 개발/QA 관리_1_8

## 1. Current Source Authority

- 기준 입력: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_121653.zip`
- 입력 ZIP SHA-256: `59297cc5afa5d8b1eb217332dfa3a205ceb5bbe24e6f20eef92b1b01c3d247f8`
- 입력 ZIP Product Source Identity: `7d25511f04c49952709489499ed637661649fe8673983302f088efc97f5c8304`
- 사용자 Full Runtime execution Source Identity: `b9aac7877adce6b7cce5a0ae556fcaa0b9f775a1f11c8268cebd9533244c9c09` / `8,435` files
- 현재 DevGPT 보정 Product Source Identity SHA-256: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af`
- 현재 Product Source: `8,340` files / `40,742,487` bytes
- 상위 제품 Requirement 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Product Requirement: **208개**
- Canonical Development/Closure Inventory: **164개** (기존 127행 유지 + 이번 Runtime/VSCode/Harness 신규 세부 37행)
- 과거 `127/127 개발 완료`, 과거 Runtime PASS/CLOSED/Evidence는 현재 Source PASS 근거로 자동 승계하지 않는다.
- Git/HEAD는 Source 정본이 아니다. 현재 Source Authority는 사용자 Local Working Tree ZIP과 Git-independent source-state SHA-256이다.

## 2. 2026-08-27 사용자 Full Runtime 재기준화

사용자 실행 결과는 **PASS=129 / FAIL=18 / SKIP_ENV=1 / NOT_EXECUTED=7 / ExitCode=1**이다. 실행 Source와 현재 입력/보정 Source가 다르므로 FAIL은 최신 Source에서 재현 여부를 확인해 수정했고 PASS도 변경 영향 범위만 재검증한다.

주요 재개방 Root Cause:

1. Java/JDT: ADM Comparator, Policy Store generic inference, Batch Map import, Logging Map.Entry, HTTP Header sanitizer, fallback comparator, LoggingAspect constructor/classpath drift.
2. VSCode: 최신 dump **50 diagnostics / 30 resources = Error 18 / Warning 29 / 기타 3**.
3. Security Context: hashed request-scope Source는 존재하나 verifier가 과거 변수명을 고정한 False Red를 semantic 검증으로 교체.
4. Execution Scope: repository 밖 Temp Evidence에 `relative_to(root)`를 강제하던 결함을 external content-addressed identity로 보정.
5. DB: SQLPlus secret transport stale assertion, Oracle seed logical-vs-physical statement False Red, Oracle Spring Batch sequence namespace 검증 결함.
6. Batch: 최신 입력 ZIP에서 5-role Windows/Linux run/stop Shell 20개가 누락되어 재구현하고 Source-state 보존 회귀 추가.
7. Frontend: Node heap 750MB에서 OOM; 제품 1GB ceiling 안의 1000MB frontend budget과 Node `>=22.18 <25`, npm `10.9.2` exact fail-closed 계약으로 보강.
8. Full Runtime Harness: VSCode 0/0, Performance load/soak, Actual Open Git Fresh Release, Fresh Replay가 누락된 채 PASS할 수 있던 False Green을 차단.
9. Managed State: 실패 시 added/removed/changed exact path와 before/after SHA를 Evidence로 기록하도록 보강.
10. UTF-8: PowerShell/Python/child output orchestration을 UTF-8로 고정하여 기존 mojibake 재발 방지.

## 3. 현재 DevGPT 검증 결과

- NXT3 Final All: **23/23 PASS / failed=0 / unverified=0**.
- Current Final verifier: **PASS**.
- NXT3 Hygiene/Garbage: **PASS / protected delete 0 / directory delete 0 / ephemeral cache 0**.
- DB Python regression: **228 passed / 2 environment skipped**.
- Testing Tools 90 test files 분할 전수: **404 passed / 22 environment skipped / 2 subtests passed / FAIL 0**.
- Generator: **47 passed / 10 environment skipped / 6 subtests passed / FAIL 0**.
- Release/Open Git contract: **53 passed / FAIL 0**.
- Runtime Tools: **72 passed / 2 environment skipped / 7 subtests passed / FAIL 0**.
- Security Context semantic verifier: **PASS**.
- VSCode source-regression: **3 passed**; 과거 Warning/Error 생성 패턴 재도입을 fail-closed.
- Spring Java Hygiene: **2,449 main Java / PASS**.
- Execution Scope exhaustive: **PASS**.
- Batch Standalone/Profile: **5 roles / 20 shells / 15 profiles / 35 checks PASS**.
- 현재 환경 Java21에서 Gradle9.1 distribution cache가 없고 외부망이 차단되어 Java25 Root Gradle compile/build는 `BLOCKED_EXTERNAL`이다. 이를 PASS로 기록하지 않는다.

## 4. Work Package 상태

| WP | 기존+신규 세부 | 개발 상태 | 검증 상태 |
|---|---:|---|---|
| WP-R00 | 14 | 현행화 완료 | Current governance/static 완료, 최종 package/runtime identity 재확인 필요 |
| WP-R01 | 20 | Source 보정 완료 | Java25 Root Build/Publication 미검증 |
| WP-R02 | 11 | Source/JDT 보정 완료 | Fresh VSCode Error 0 / Warning 0 미검증 |
| WP-R03 | 14 | Harness 보강 완료 | 강화 Full Runtime/Fresh Replay 미검증 |
| WP-R04 | 13 | Generator Source/contract 완료 | Java25 generated build/runtime 미검증 |
| WP-R05 | 8 | Shell 20 재구현 포함 완료 | Windows/Linux 실제 역할기동 미검증 |
| WP-R06 | 7 | Profile 계약 완료 | role별 effective Runtime 미검증 |
| WP-R07 | 10 | Release 계약 완료 | Actual Fresh Binary Release/Golden Path 미검증 |
| WP-R08 | 8 | contract 완료 | full mutation Runtime 미검증 |
| WP-R09 | 6 | Security verifier/Source 보정 완료 | full auth/session Runtime 미검증 |
| WP-R10 | 11 | DB Source/Test 보정 완료 | DB3 physical lifecycle 미검증 |
| WP-R11 | 10 | Batch contract 완료 | Worker×2 kill/UNKNOWN/reconcile 미검증 |
| WP-R12 | 8 | Logging/OpenAPI contract 완료 | One-WAS/real logging/OpenAPI/Performance 미검증 |
| WP-R13 | 7 | Frontend resource/toolchain 보강 완료 | Windows Node/browser E2E 미검증 |
| WP-R14 | 7 | UTF-8/path/garbage/drift 보강 완료 | Windows native path/output/managed drift 재검증 필요 |
| WP-R15 | 10 | Final Gate Source 보강 완료 | Full Runtime/Fresh Replay/Codex 독립검수 미검증 |

`development_status`와 `verification_status`는 분리한다. Source 보정 완료는 제품 전체 완료가 아니다.

## 5. 다음 mandatory Physical Acceptance

1. Windows Java25 + Gradle9.1 Root clean build/test/publication/SBOM + Generated Domain build.
2. Fresh VSCode Gradle/JDT Import Problems **Error 0 / Warning 0**.
3. Oracle/PostgreSQL/MariaDB Fresh→Seed→Verify→Runtime→Upgrade→Rollback→Reapply→Fault→Cleanup.
4. Kafka-free Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile/Center-Cut.
5. One-WAS real transaction + File Log↔DB Log↔Transaction/Segment/Timeline correlation.
6. ADM/Backoffice Runtime OpenAPI + frontend lint/typecheck/test/build + Browser E2E/a11y/error statuses.
7. signed Performance Live + mandatory load/soak.
8. Actual Open Git Fresh Binary Release + fresh clone-equivalent Golden Path.
9. Source/Managed drift 0 및 동일 Source Fresh Replay.
10. 최종 기준: `FAIL=0 / mandatory SKIP_ENV=0 / NOT_EXECUTED=0 / unresolved UNKNOWN=0 / Source drift=0 / Managed drift=0 / ExitCode=0`.

## 6. Current-only / Garbage

- `CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv`와 이 파일이 Developer 현재 작업 정본이다.
- `CODEX_FINDING_CLOSURE.csv`의 Codex 상태를 DevGPT가 수정하지 않는다.
- 테스트 과정에서 생성된 transient generated Evidence/cache/pyc는 결과 Source에서 제거한다.
- 제품 Source 삭제는 사용자 승인 `DELETE_MANIFEST.csv`와 precondition을 통해서만 수행한다.
- 사용자 승인 없이 commit/push/reset/restore/clean/history 변경을 하지 않는다.

## 7. Codex Continuation

이번 DevGPT 변경 종료 후 `CODEX_NEXT_WORK_INSTRUCTION_20260827.md`와 `CODEX_RESULT_TO_NEXT_WORK_TRACE_20260827.md`를 사용한다. Codex는 기존 Active Finding을 먼저 종결하고 이번 DevGPT 변경 중 Build/Logging/DB3/Batch/Generator/Performance/Open Git 고위험 범위만 독립검증한다. 완료된 Repository 전체를 다시 전수검수하지 않는다.


## 최종 전달 패키지 구성

최종 전달물은 Source 수정만 포함하는 작은 Patch가 아니라 다음을 한 Overlay에 함께 포함한다.

- 변경 Source/SQL/Test/Verifier/Config와 Batch Shell 20개 + `.gitignore` trackability 보정
- Canonical Development/Closure Inventory 및 Requirement Status
- 개발 완료 리뷰와 Current Evidence
- Local 적용/삭제/저비용/Full Runtime 명령
- 다음 DevGPT 세션 상세 Handover + LONG-TURN Handover
- Codex 현재 turn 연속 작업지침 + 결과 Trace
- CHANGE/DELETE/PACKAGE Manifest + SHA256SUMS

패키지 적용 후 Windows Physical Final Runtime/Fresh Replay와 Codex independent verification이 PASS하기 전 Overall 완료로 선언하지 않는다.
