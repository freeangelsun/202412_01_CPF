# CPF TEST AND EVIDENCE — Current

## 1. Source Identity

- Baseline Source ZIP SHA-256: `e24d2596fc404c6761725cf5b5a4a618038dae4f1e177d4171296f6204d20802`
- Baseline Product Source Identity: `b33471236f57a30eba48c9cc582789ee33f81cd8b67194a9e710b06877b4d68e` / 8,409 source files
- Current Product Source Identity: `398ebf1ee0d80f9ffc2bf80e9ad8b0e6834cea8ab0a84e2e5b131aa64672c717` / 8,320 source files
- Identity policy: `GIT_INDEPENDENT_CANONICAL_PATH_SIZE_SHA256_LINES`
- Git provenance: supplied ZIP에는 `.git`이 없으므로 local overlay 적용 후 read-only Git root/branch/HEAD/status로 확인한다.

## 2. Current exact-source Static / Contract 결과

| Gate/Test | Result | Interpretation |
|---|---|---|
| `verify-cpf-current-final.py` | PASS | Current Canonical / EDU / operation / delete governance |
| `verify_nxt3_hygiene.py` | PASS | Delete Manifest, protected path, current hygiene |
| `verify_nxt3_repository_garbage.py` | PASS | Current-only garbage decision integrity |
| UTF-8 Runtime Boundary | PASS | ProcessStartInfo 27 / redirected 27 / Start-Process 10 / mojibake source 0 |
| Physical DB Consolidation | PASS | Current Physical DB = cpfDB/mbwDB/mbrDB/exsDB; retired active target 0 |
| Unified CLI | PASS | Public `bootstrap/build/doctor/domain-new/domain-sync/help/reset/run/status/stop/test/version`; internal namespace 분리 |
| Requirement Progress / Projection | PASS 218/218 | Developer Requirement ledger와 canonical projection 일치 |
| Open Git/Public Release pytest | **48 PASS / 0 FAIL** | replay 및 current source contract regression |
| DB unit/contract | **142 PASS / 2 SKIP / 0 FAIL** | 22개 파일을 3개 묶음으로 분리 실행 |
| DB verification | **87/87 PASS** | DB lifecycle/verifier contract |
| Runtime tools | **72 PASS / 2 SKIP / 0 FAIL** | 환경/선택형 SKIP은 Physical PASS로 승격하지 않음 |
| Generator replay subset | **20 PASS / 9 SKIP / 0 FAIL** | 첫 7개 파일 replay 결과; 장기 setup/sync는 sandbox timeout. 기존 동일 Source targeted/mutation 결과는 별도 참고 |

## 3. Fresh Replay

- Baseline 전체 Tree + Overlay 적용 후 Delete Manifest 실행: **PASS**
- Delete Manifest: **2,038행**, 모두 `approved=true / user_approved=true / precondition=SATISFIED / lifecycle=PENDING_USER_EXECUTION`
- Fresh Replay에서 실제 삭제된 baseline 파일: **1,725건** / already missing 313건
- Replay Product Source Identity: `398ebf1ee0d80f9ffc2bf80e9ad8b0e6834cea8ab0a84e2e5b131aa64672c717` — **exact match**
- Replay ↔ Current full file comparison: **8,832 files / missing 0 / extra 0 / changed 0**
- Replay Current/Hygiene/Garbage/UTF-8/DB4/CLI/Requirement Gate: **PASS**
- Replay Open Git/Public Release: **48/48 PASS**
- Replay DB: **142 PASS / 2 SKIP + 87 PASS / FAIL 0**
- Replay Runtime Tools: **72 PASS / 2 SKIP / FAIL 0**

Final package replay는 **PASS**이며 Product Source Identity와 full tree가 current 작업본에 exact match했다.

## 4. Current-only / Legacy Zero

- `referenceFixture/refDB` Current schema/seed/provision/verify/runtime 경로 제거.
- Current DB Canonical 231 tables.
- `admDB/batDB/bzaDB/cmnDB/refDB` active runtime target 0.
- 날짜/세션/checkpoint/RERUN/Handover/Completion stale current 자료 제거.
- Codex current evidence 1,581개를 검사한 결과 Current Product Source Identity match 0건 → 사용자 승인 Current-only cleanup으로 제거. Codex 상태 컬럼 자체는 수정하지 않음.
- 일반 `backup` 기능 테스트 4개는 현행 Backup 기능 계약이므로 Legacy filename으로 오판하지 않고 유지.

## 5. Physical Acceptance — 성공 처리하지 않은 항목

- Java 25.0.3 Root Gradle build/test/publication/SBOM
- Fresh VS Code Java25/Gradle Import Error 0 / Warning 0
- Oracle/PostgreSQL/MariaDB actual DB3 Fresh→Seed→Runtime→Upgrade→Rollback/Recovery→Reapply→Fresh Replay
- Windows/Linux Unified CLI actual lifecycle / UTF-8 / path / prerequisite negative
- Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile
- One-WAS actual transaction + rollback-surviving DB logging + file/db/segment/timeline correlation
- ADM/Backoffice Runtime OpenAPI + Frontend/Browser E2E/a11y/error states
- signed Performance live/load/soak
- Actual `cpf-release/` Fresh Binary/Source Release + public CLI lifecycle + leakage 0
- Full Runtime `FAIL=0 / mandatory SKIP_ENV=0 / NOT_EXECUTED=0 / unresolved UNKNOWN=0`
- Source/Managed drift 0 + Physical Same Source Fresh Replay
- Codex current-source independent verification

## 6. Environment

- Assistant sandbox: Java 21, Python 3.13, Node 22.16
- Java25: unavailable
- Docker: unavailable
- PowerShell/pwsh: unavailable
- 사용자 Windows Java25 환경용 runner: `cpf-docs/work/current/CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS.md`

## 7. 판정

**DevGPT Source/Static Closure는 완료했다. 전체 QA/Physical Completion은 미검증이다.** 미실행 Runtime을 PASS로 기록하지 않는다.
