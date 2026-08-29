# CPF TEST AND EVIDENCE — Current

## 1. Source Identity

- Baseline Source ZIP SHA-256: `e24d2596fc404c6761725cf5b5a4a618038dae4f1e177d4171296f6204d20802`
- Baseline Product Source Identity: `b33471236f57a30eba48c9cc582789ee33f81cd8b67194a9e710b06877b4d68e` / 8,409 source files
- DevGPT Handoff Product Source Identity: `398ebf1ee0d80f9ffc2bf80e9ad8b0e6834cea8ab0a84e2e5b131aa64672c717` / 8,320 source files
- Codex Continuation Current Product Source Identity: `fddfbcabe6f7bab187bd957178b5e9a06791886625c29e7362c4e0a4e38aea91` / 8,362 source files / 45,383,854 bytes
- Identity policy: `GIT_INDEPENDENT_CANONICAL_PATH_SIZE_SHA256_LINES`
- Git provenance: `master` / HEAD=`5f2289d4171d5a42ce579d7c98ebf3e1bef19828` / origin/master exact match / 시작 Working Tree clean / Git write 0.
- Identity correction: canonical Batch `bin/`의 Shell은 포함하되 `*.class`, root `logs/`, `.github/modernize/` generated workspace는 Source/Managed identity에서 제외한다.

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
| Open Git/Public Release pytest | **48 PASS / 0 FAIL after Codex fix** | exact HEAD 최초 재현은 21 PASS 뒤 1 FAIL; Windows `javac.exe` fault-injection suffix 결함 보완 후 isolated full regression PASS |
| Java/VS Code Problems impact | **Java 3,066 errors 0; known regression 5/5; CLI `-Werror` PASS** | 변경 Python 19개 compile PASS, PowerShell 10개 parse PASS, 신규 compiler/deprecation warning 0 |
| `cpfVerifyFast --warning-mode all` | **25/25 tasks PASS** | ADM/Backoffice/Batch/Starter/DB3/Generated boundary 영향도 PASS |
| Generated disposable mutation | **create→build→remove→restore→build PASS** | `codexscratch` actual Source removal 및 hash parity difference 0; 일반 작업공간 삭제는 계속 fail-closed |
| DB unit/contract | **142 PASS / 2 SKIP / 0 FAIL** | 22개 파일을 3개 묶음으로 분리 실행 |
| DB verification | **87/87 PASS** | DB lifecycle/verifier contract |
| Runtime tools | **72 PASS / 2 SKIP / 0 FAIL** | 환경/선택형 SKIP은 Physical PASS로 승격하지 않음 |
| Generator replay subset | **20 PASS / 9 SKIP / 0 FAIL** | 첫 7개 파일 replay 결과; 장기 setup/sync는 sandbox timeout. 기존 동일 Source targeted/mutation 결과는 별도 참고 |

## 3. Fresh Replay

- Baseline 전체 Tree + Overlay 적용 후 Delete Manifest 실행: **PASS**
- Delete Manifest: **2,038행**, 모두 `approved=true / user_approved=true / precondition=SATISFIED / lifecycle=PENDING_USER_EXECUTION`
- Fresh Replay에서 실제 삭제된 baseline 파일: **1,725건** / already missing 313건
- Replay Product Source Identity: `398ebf1ee0d80f9ffc2bf80e9ad8b0e6834cea8ab0a84e2e5b131aa64672c717` — **handoff source exact match; Codex current source로 승계하지 않음**
- Replay ↔ Current full file comparison: **8,832 files / missing 0 / extra 0 / changed 0**
- Replay Current/Hygiene/Garbage/UTF-8/DB4/CLI/Requirement Gate: **PASS**
- Replay Open Git/Public Release: **48/48 PASS**
- Replay DB: **142 PASS / 2 SKIP + 87 PASS / FAIL 0**
- Replay Runtime Tools: **72 PASS / 2 SKIP / FAIL 0**

Handoff package replay는 해당 handoff source에서는 **PASS**였다. Codex continuation current source의 Fresh Replay는 아직 `NOT_EXECUTED`이며 최종 PASS로 승격하지 않는다.

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

- Codex current host: Windows x64, Python 3.13.14, pytest 9.1.1, Java 25.0.3.
- Java25: available and used by the Open Git CLI build regression.
- Docker/DB3/Browser/Performance physical prerequisites: 별도 preflight 및 실행 전이며 PASS로 기록하지 않음.
- 사용자 Windows Java25 환경용 runner: `cpf-docs/work/current/CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS.md`

## 7. 판정

**DevGPT Source/Static Closure는 완료했다. 전체 QA/Physical Completion은 미검증이다.** 미실행 Runtime을 PASS로 기록하지 않는다.

## 8. Codex Continuation P0-1

- Initial isolated reproduction: `21 passed / 1 failed`, exact failing ID `test_cross_platform_cli_build_requires_java25`.
- Root Cause: Windows command path가 `javac.exe`인데 test fault injection이 `endswith("javac")`만 인식하여 Java 21 negative를 주입하지 못했다.
- Latent closure: Open Git release test와 독립 cross-platform CLI contract의 동일 패턴 6곳을 OS-neutral executable matching으로 보완했다.
- Targeted regression: `6/6 PASS`.
- Open Git/Public full regression: `48/48 PASS`, mandatory skip 0, cleanup exception 0, exit code 0.
- Source-state impact regression: combined `59/59 PASS`.
- Detailed Codex evidence: `cpf-docs/work/current/CODEX_TEST_AND_EVIDENCE.md`.
- P0-1 status: `CLOSED`; V2 overall status: `IN_PROGRESS`.

## 9. Codex Continuation P0-2 Partial Closure

- Unified CLI active consumer routing, Java25 warning-free CLI build, product-isolated BOM publication, and approved disposable Generated Domain lifecycle findings are `CLOSED` on the Current Product Source Identity.
- Zero/one/many root projection mutation, Backoffice-absent mutation, Linux physical CLI, and Open Git mutation residue checks remain `IN_PROGRESS`/`NOT_EXECUTED` and are not inherited from this result.
