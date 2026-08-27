# CPF Developer GPT Test & Evidence — 2026-08-27 — C 개발/QA 관리_1_8

## Source Identity

- Input ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_121653.zip`
- Input ZIP SHA-256: `59297cc5afa5d8b1eb217332dfa3a205ceb5bbe24e6f20eef92b1b01c3d247f8`
- Input Product Source Identity: `7d25511f04c49952709489499ed637661649fe8673983302f088efc97f5c8304`
- User Full Runtime execution Source Identity: `b9aac7877adce6b7cce5a0ae556fcaa0b9f775a1f11c8268cebd9533244c9c09` / 8,435 files
- Current DevGPT Product Source Identity: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af`
- Current Product Source: `8,340` files / `40,742,487` bytes
- Canonical Product Requirement: `208`
- Developer Closure Inventory: `169` rows
- Physical Runtime가 끝나지 않아 Overall은 `미검증`이다.

## DevGPT 실행환경

- Linux x86_64
- OpenJDK 21.0.11
- Python 3.13.5
- Node 22.16.0 / npm 10.9.2
- Gradle 9.1 distribution cache 없음, 외부망 차단 → Java25/Gradle 물리 Build는 `BLOCKED_EXTERNAL`

## 실제 실행 결과

| 검증 | 실제 결과 | 판정 |
|---|---:|---|
| User Windows Full Runtime (입력 Evidence) | PASS 129 / FAIL 18 / SKIP_ENV 1 / NOT_EXECUTED 7 / RC 1 | FAIL — current Source 자동승계 금지 |
| DB Python full regression | 228 passed / 2 environment skipped | PASS_WITH_ENV_SKIPS |
| Testing Tools 90 files split | 404 passed / 22 environment skipped / 2 subtests passed | PASS_WITH_ENV_SKIPS |
| Generator verification | 47 passed / 10 environment skipped / 6 subtests passed | PASS_WITH_ENV_SKIPS |
| Release/Open Git tests | 53 passed | PASS |
| Runtime Tools tests | 72 passed / 2 environment skipped / 7 subtests passed | PASS_WITH_ENV_SKIPS |
| NXT3 23-gate coverage | runner first 18 PASS before executor timeout + remaining 5 individually PASS | PASS_COVERAGE / MONOLITHIC_NOT_OBSERVED |
| Current Final verifier | failures 0 | PASS |
| NXT3 Hygiene | protected delete 0 / directory delete 0 | PASS |
| NXT3 Garbage | decision 1129 / delete 301 / ephemeral cache 0 / failures 0 | PASS |
| Security Context semantic verifier | failures 0 | PASS |
| Execution Scope exhaustive | status PASS | PASS |
| Batch Standalone/Profile | roles 5 / shells 20 / profiles 15 / checks 35 | PASS_STATIC_ONLY |
| Spring Java Hygiene | 2,449 main Java / failures 0 | PASS |
| VSCode source-regression gate | 3 passed; 2026-08-27 known diagnostics reintroduction fail-closed | PASS |
| VSCode verifier negative/positive | actual 50/18E/29W dump fails; zero E/W fixture passes | PASS |
| Approved Delete Manifest isolated replay | selected 312 / deleted 6 / already absent 306 / replacement missing 0 / unsafe 0 | PASS |
| Gradle Java25 Root Build | Gradle9.1 distribution unavailable in DevGPT environment | BLOCKED_EXTERNAL |
| Fresh VSCode Error/Warning | Windows VSCode unavailable | NOT_EXECUTED |
| DB3 Physical lifecycle | Docker/vendor runtime unavailable here | NOT_EXECUTED |
| One-WAS/real logging/Runtime OpenAPI/Browser/Performance | Windows/Docker/Browser environment unavailable here | NOT_EXECUTED |
| Actual Open Git Fresh Binary Release | Java25 publication environment unavailable here | NOT_EXECUTED |
| Fresh Replay | Final physical first pass 미완료 | NOT_EXECUTED |

## 이번에 보정한 주요 Root Cause / False Green

- 최신 VSCode 50 diagnostics를 18 Error/29 Warning/3 기타로 재기준화하고 ADM Comparator/generic/import/API/constructor drift를 보정.
- Security request-scope verifier가 `session` 변수명 literal을 강제하던 False Red를 hashed-session semantic contract로 교체.
- Execution Scope external Temp Evidence `relative_to(root)` 실패를 content-addressed external identity로 보정.
- Oracle SQLPlus failure diagnostic/secret transport 강화와 stale literal Test를 의미 기반으로 currentize.
- Oracle seed logical mutation과 row-wise MERGE physical expansion을 동일 statement count로 비교하던 False Red 제거.
- Oracle Spring Batch verify를 전체 `user_sequences`가 아니라 관리 `BAT_SB_` namespace 기준으로 currentize.
- 최신 입력 ZIP에서 누락된 Batch 5-role Windows/Linux run/stop Shell 20개를 복원하고 Source-state 보존 회귀를 추가했으며, Root `.gitignore`의 전역 `bin/` 규칙에서 `cpf-batch/*/bin/**`를 제품 Source로 명시적 예외 처리해 Git 추적 누락까지 보정.
- Frontend OOM의 750MB ceiling을 제품 1GB 상한 안의 1000MB로 조정하고 Node/npm exact toolchain을 fail-closed로 검증.
- Required Full Runtime에 Fresh VSCode 0/0, mandatory Performance load/soak, Actual Open Git Fresh Release, mandatory stage completeness, exact managed diff, 동일 Source Fresh Replay를 추가.
- Full Runtime PowerShell/Python child I/O를 UTF-8로 고정하여 mojibake 재발 방지.

## 통합 Runner Timeout 처리

장시간 pytest/NXT3 monolithic 호출이 assistant 실행 제한에 걸린 경우 해당 호출 자체를 PASS로 기록하지 않았다. 동일 파일/Stage를 독립 묶음으로 다시 실행했다. 현재 Source에서는 Runner가 18개 연속 PASS 후 외부 executor timeout에 걸렸고, 남은 5개 Gate를 개별 실행해 모두 PASS했다. 따라서 23개 Gate coverage는 PASS지만 현재 Source의 monolithic runner completion 자체를 PASS로 기록하지 않는다.

## BLOCKED_EXTERNAL / 필수 재실행

1. Windows Java25/Gradle9.1 clean Root Build/Test/Publication/SBOM + Generated Domains.
2. Fresh VSCode Gradle/JDT Problems Error 0 / Warning 0.
3. DB3 Physical Fresh/Upgrade/Rollback/Reapply/Fault/Cleanup.
4. Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile.
5. One-WAS + File/DB/Transaction/Segment/Timeline correlation.
6. Runtime OpenAPI + frontend build/test + Browser E2E/a11y.
7. signed Performance Live + required load/soak.
8. Actual Open Git Fresh Binary Release/Golden Path.
9. Source/Managed drift 0 + 동일 Source Fresh Replay.

미실행 결과는 PASS로 기록하지 않는다.

## Fresh Baseline + Overlay 재검증

원본 `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_121653.zip`에 이 세션 Overlay를 다시 적용한 독립 Snapshot에서 NXT3 23/23, DB 228/2 env skip, Testing Tools 403/22 env skip/2 subtests, Generator 47/10 env skip/6 subtests, Release 53, Runtime Tools 72/2 env skip/7 subtests를 재현했다. 이후 VSCode Warning Source 보강과 회귀 Test 1건이 추가되어 현재 Source의 Testing Tools 집계는 404 passed이며 해당 변경은 Targeted 3/3 및 Current Final/Hygiene/Garbage/Java Hygiene로 재검증했다. 최종 Overlay는 이 최신 Source에서 다시 Fresh Apply/Hash 검증한다.
