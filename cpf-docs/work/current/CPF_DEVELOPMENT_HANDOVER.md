# CPF Development Handover — C 개발/QA 관리_1_8 — 2026-08-27

## 현재 상태

- Input ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_121653.zip`
- Input ZIP SHA-256: `59297cc5afa5d8b1eb217332dfa3a205ceb5bbe24e6f20eef92b1b01c3d247f8`
- Input Product Source Identity: `7d25511f04c49952709489499ed637661649fe8673983302f088efc97f5c8304`
- User Runtime Source Identity: `b9aac7877adce6b7cce5a0ae556fcaa0b9f775a1f11c8268cebd9533244c9c09` / 8,435 files
- Current DevGPT Product Source Identity: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af`
- Product Source: `8,340` files / `40,742,487` bytes
- Canonical Requirements: `208`
- Developer Closure Inventory: **169행**
- DevGPT Static/Contract/NXT3: 현재 재현 FAIL 0
- Physical Verification: **미완료**
- QA 최종 완료: **아님**

## 이번 세션 핵심 변경

- Runtime 전체 18 FAIL/1 SKIP/7 NOT_EXECUTED와 최신 VSCode 50 diagnostics를 과거 완료상태보다 우선하여 재개방.
- Java/JDT Comparator/generic/import/API/constructor root cause 보정.
- Security Context literal verifier False Red 및 Execution Scope external Temp path 결함 보정.
- DB Oracle SQLPlus/Seed/Spring Batch sequence Closure 보정.
- 입력 ZIP에서 누락된 Batch 5-role Shell 20개 재구현 + Source-state regression.
- Frontend 1GB ceiling 내 1000MB budget + Node/npm toolchain fail-closed.
- Required Full Runtime의 VSCode 0/0, Performance load/soak, Actual Open Git Fresh Release, mandatory stage completeness, managed diff, Fresh Replay 누락 보정.
- transient generated Evidence/cache/pyc를 최종 Source에서 제거.
- VSCode 과거 29 Warning의 method-reference/nullness/unused/suppression/deprecation 패턴을 Source에서 추가 보정하고 재발방지 Test를 추가.
- stale `developer-rework` Evidence 6건을 Current-only 정본에서 제거할 수 있도록 승인 Delete Manifest에 추가하고 isolated delete replay를 PASS.

## 현재 검증

- NXT3 23/23 PASS.
- DB 228 passed / 2 env skipped.
- Testing Tools 404 passed / 22 env skipped / 2 subtests.
- Generator 47 passed / 10 env skipped / 6 subtests.
- Release 53 passed.
- Runtime Tools 72 passed / 2 env skipped / 7 subtests.
- Current Final/Hygiene/Garbage/Security Context/Execution Scope PASS.
- Java25/Windows/Docker/Browser physical acceptance는 BLOCKED_EXTERNAL/NOT_EXECUTED이며 PASS가 아니다.

## 다음 순서

1. 이번 Overlay를 사용자 최신 Local Working Tree에 적용하고 Overlay 자체 hash/target만 검증한다.
2. Windows Java25에서 강화된 `run-cpf-required-full-runtime-validation.ps1`을 실행한다.
3. FAIL/SKIP_ENV/NOT_EXECUTED/UNKNOWN/Source·Managed drift를 Root Cause별로 재개방한다.
4. Full Runtime가 PASS하면 동일 Source Fresh Replay까지 실행한다.
5. Codex는 `CODEX_NEXT_WORK_INSTRUCTION_20260827.md`로 **기존 Active WP부터 종결**하고 이번 DevGPT 고위험 변경만 독립검증한다.
6. Codex가 Source를 수정하면 이전 Runtime PASS를 승계하지 말고 다시 Final Runtime/Fresh Replay한다.
7. mandatory PASS 후에만 QA 재검수 요청한다.

## LONG-TURN / 병행 Local Working Tree

- Codex/Claude는 현재 하던 작업을 먼저 Source/Consumer/Test/Runtime/Evidence/문서까지 완결한다.
- 계획/중간보고는 중단점이 아니며 같은 turn에서 계속 실행한다.
- 기존 미완료 전체/Repository 전체 전수 재검수 금지.
- 같은 Root Cause/Build/DB/Batch Runtime을 묶어서 재작업을 최소화한다.
- Build/Logging/DB3/Batch/Generator/Performance/Open Git 우선, ADM/Frontend/Browser 최후순위.
- Git/HEAD/전체 Local Working Tree를 차단 Gate로 쓰지 않고 다른 세션 변경을 건드리지 않는다.
- 사용자 승인 없이 Git 쓰기/삭제/history 변경을 하지 않는다.


## 최종 전달 인덱스

- 전체 완료 리뷰: `CPF_DEVELOPMENT_COMPLETION_REVIEW_20260827.md`
- Local 적용/삭제/검증/Full Runtime: `CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS_20260827.md`
- 다음 DevGPT 세션 상세 인수인계: `CPF_NEXT_SESSION_HANDOVER_C_DEV_QA_1_8_20260827.md`
- LONG-TURN 공통 인수인계: `CPF_NEXT_SESSION_HANDOVER_LONG_TURN_20260827.md`
- Codex 현재 turn 연속 지침: `CODEX_NEXT_WORK_INSTRUCTION_20260827.md`
- Codex 결과 Trace: `CODEX_RESULT_TO_NEXT_WORK_TRACE_20260827.md`
- 전체 전달 파일 목록/실행 순서: `CPF_FINAL_DELIVERY_INDEX_20260827.md`
