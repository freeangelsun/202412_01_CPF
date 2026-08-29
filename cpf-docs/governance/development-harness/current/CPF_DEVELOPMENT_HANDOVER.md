# CPF Development Handover — Current

## 1. Authority

- Baseline ZIP SHA-256: `e24d2596fc404c6761725cf5b5a4a618038dae4f1e177d4171296f6204d20802`
- Baseline Product Source Identity: `b33471236f57a30eba48c9cc582789ee33f81cd8b67194a9e710b06877b4d68e` / 8,409 files
- Current Product Source Identity: `398ebf1ee0d80f9ffc2bf80e9ad8b0e6834cea8ab0a84e2e5b131aa64672c717` / 8,320 files
- Canonical Inventory: 190 rows
- Developer Requirement Ledger: 218 rows
- Delete Manifest: 2,038 rows
- Git provenance: supplied ZIP에는 `.git`이 없다. Local 적용 후 Git은 read-only root/branch/HEAD/status를 확인하고 reset/restore/clean하지 않는다.

## 2. DevGPT Source Closure

- Current Canonical 선행 현행화.
- Unified `cpf` Java CLI + `doctor` + PUBLIC/INTERNAL boundary.
- Developer Gradle grouping / Capability taxonomy / Generated·Optional Domain zero dependency.
- Open Git binary/source/current-only/Git-write-zero projection.
- Current Physical DB = cpfDB/mbwDB/mbrDB/exsDB, active retired target 0.
- referenceFixture/refDB Current path 제거, Current DB Canonical 231 tables.
- UTF-8 child-process boundary fail-closed.
- Transaction DB logging independent boundary Source.
- Current-only history cleanup: dated/checkpoint/RERUN/Handover/Completion 및 current-identity 불일치 Codex Evidence 제거.

## 3. 다음 검증

`CPF_FINAL_LOCAL_APPLY_RUNTIME_COMMANDS.md` 순서로 Final Overlay 적용 → Delete Manifest → low-cost Gate → Java25 Full Runtime → Fresh Replay를 실행한다.

Physical PASS 전에는 QA 전체 완료/Commit 가능 상태라고 표현하지 않는다. Codex는 current exact Source에서 독립 검증 Evidence를 새로 작성한다.

## 2026-08-29 C 개발/QA 관리_3_1 — Open Git Release Repair Continuation

- Source baseline ZIP SHA-256: `E6E343947AB4D829996107833AD20CD056D35BAC340013F58E0B2068C9694B30`.
- Current repair work items: `WP-R01.21`, `WP-R03.15`, `WP-R07.17`. Source는 보정됐으나 Java25 actual replay 전까지 모두 `VERIFICATION_PENDING`이다.
- 적용 직후 기존 실패 `cpf-release/`를 수동 재사용하지 말고 동일 Canonical `cpf release open-git build --profile binary`를 실행한다. Engine이 exact `<CPF_ROOT>/cpf-release`를 먼저 안전 삭제하고 Fresh 생성한다.
- Release가 Stage 05를 넘기면 Public Maven coordinate/POM/BOM verifier를 계속 관찰한다. `com.cpf.internal` publication/dependency, `cpf-batch-contract`, 누락 `com.cpf.runtime:*`, `cpf-batch-runtime` 미게시가 1건이라도 있으면 PASS 금지.
- Release 성공 후 VS Code `Java: Clean Java Language Server Workspace` 또는 Gradle project reload에 준하는 Fresh import를 수행하여 Problems의 Java Error/Warning 0을 확인한다. source-empty stable output은 Gradle User Home 아래 `cpf-ide-classpath/<root-id>/...`에 존재해야 하며 project `build/` cleanup에 종속되면 안 된다.
- Public binary는 Local 기존 JAR 복사가 아니라 현재 Source의 Fresh Gradle Build/Publication 산출물이어야 한다. Open Git Source push와 Maven Repository 배포는 별도 Gate로 취급한다.
- 최종 CLOSED 조건: Java25 Fresh Build/Test/Publication + Public verifier/consumer + Open Git 14/14 + VS Code Error=0 Warning=0 + Windows/Linux Generator + Leakage 0 + Evidence/Source Identity + Independent Reviewer/QA PASS.

