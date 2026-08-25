# CPF Developer GPT Test and Evidence — 2026-08-25

## Source Identity

- Baseline ZIP SHA-256: `0eba1e95d1552342a128984930b0f0f533787caad209f8b9e7f04ddcacf7caf1`
- Current Product Source SHA-256: `7c7b806d4284a5a655731cff60b3cce214cdcec9f73ce489b9f3f96bf9bac809`
- Product Source files: `8434`
- Product Source bytes: `49517641`

## 실제 실행 PASS

- `verify-cpf-clean-source-tree.py`: PASS, files=10215, garbage=0, emptyDirs=0 (Delete Manifest 시뮬레이션 적용 작업본).
- `verify-cpf-java-source-syntax.py`: PASS, files=2956, errors=0.
- `verify-cpf-batch-executor-registration-contract.py`: PASS, checks=8, failures=0.
- `verify-cpf-approval-state-machine.py`: PASS.
- `verify-cpf-integrated-logging-closure.py`: PASS.
- `verify-cpf-batch-fail-closed.py`: PASS.
- `verify-cpf-batch-unknown-reconciliation.py`: PASS, Oracle/PostgreSQL/MariaDB semantic parity=true.
- `verify-cpf-db-vendor-semantic-parity.py`: PASS, canonical tables=236.
- `verify-cpf-db-vendor-static-token-parity.py`: PASS.
- `verify-cpf-db-lifecycle-contract.py`: PASS, vendors=3, stages=9.
- `verify-cpf-db-vendor-manifest.py`: PASS, checked paths=51.
- `verify-cpf-db-development-contract.py`: PASS.
- `verify-cpf-db-schema-governance.py`: PASS, tables=236, FKs=155; performance-review warning candidates=35.
- `sync_bat_runtime_roles.py`: PASS, current roles=`CONTROL_PLANE/SCHEDULER/WORKER/CENTER_CUT_RUNNER/AGENT`; V116 historical immutable, V138 currentization.
- Backoffice OpenAPI source validation: PASS, operations=96.
- ADM OpenAPI source validation: PASS, operations=337.
- OpenAPI/controller exact coverage: Backoffice 96/96 PASS, ADM 337/337 PASS.
- `verify-cpf-backoffice-route-contract.py`: PASS.
- `verify-cpf-backoffice-classification.py`: PASS.
- `verify-cpf-frontend-consumer-closure.py`: PASS.
- Open Git pytest: 17/17 PASS.
- targeted Approval/Batch approval/runtime harness pytest: 6/6 PASS.
- Immutable historical migration compare V001~V137/R001~R137: 629 files checked, changed=0, missing=0.
- Codex protected files: 1526 checked, changed=0, missing=0.

## 보정 중 발견 후 닫은 Finding

1. `referenceFixture` 4개 object가 production DB projection에 혼입: Canonical renderer mapping 수정 후 DB3 semantic parity PASS.
2. Center-Cut Runtime Identity가 BAT/CENTER_CUT으로 남음: CEC/CEC/CENTER_CUT_RUNNER + V138/R138 append-only로 현행화.
3. Runtime-role contract/verifier가 V116 historical role을 current로 오인: historical/current 분리 후 PASS.
4. `FILE_WATCH` enum/UI는 있으나 Worker Consumer가 없음: `ApprovedFileExecutor.awaitReady()`로 연결.
5. CENTER_CUT Batch Job consumer 부재: Control Plane `CpfCenterCutOperations.launch()` 연결.
6. Batch UNKNOWN verifier가 대문자 physical SQL을 소문자 literal로 비교해 False Red: case-insensitive semantic check로 수정.
7. MBW Approval의 업무 판단문서/History/Snapshot 결정/실행결과 계약 부족: 판단문서/Before-After/History/version+hash decision/MBW_APPROVAL_EXECUTION V139/R139을 보완.
8. 테스트/Gradle disposable state가 module 하위 `cpf-docs`를 생성: 외부 managed-work root로 변경, nested `cpf-docs`는 Delete Manifest 대상.
9. Source Identity에 runtime log/cache가 섞일 수 있음: generated/cache scope를 source identity에서 제외.

## 미실행 — PASS 아님

현재 assistant 실행환경은 Java 21이며 Java25/Gradle 9.1 cached distribution/Docker live DB3 환경이 없다. 따라서 아래는 `VERIFICATION_PENDING`이다.

- Java 25 전체 Gradle clean/build/test/publication.
- Oracle/PostgreSQL/MariaDB 실제 전체 객체 Fresh 초기화 → Seed → V138/V139 실제 거래 → Upgrade → Rollback/Recovery → Reapply → 기존 데이터 보존/Schema parity.
- BAT/CEC 실제 Process: Registry, FILE_WATCH 실제 파일, CENTER_CUT 실제 Domain Invocation, Worker Kill/Lease/Fencing/UNKNOWN/Reconcile/Recovery.
- ADM/Backoffice Browser E2E 및 실제 Approval Owner 적용/FAILED/UNKNOWN/Reconcile.
- Open Git Fresh binary publication + Fresh Generated Domain + EDU/Backoffice/UI + external clean consumer acceptance.

## 필수 로컬 검증

`cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1`을 프로젝트 Root에서 Java25 + Docker 환경으로 실행한다. ExitCode 0이고 Final PASS이며 SKIP/NOT_EXECUTED/UNVERIFIED가 0이 아니면 Runtime PASS로 기록하지 않는다.
## Fresh Baseline Overlay Replay

Baseline fresh copy에 최종 Overlay를 실제 압축해제하고 `DELETE_MANIFEST.csv` 승인경로 114개를 적용한 뒤 재검증했다.

- Clean Source: PASS, garbage=0, emptyDirs=0.
- Java Source Syntax: PASS, 2,956 files, errors=0.
- Batch Executor Registration: PASS, 8 checks.
- Approval State Machine: PASS.
- Batch UNKNOWN DB3: PASS, 3 Vendor semantic parity=true.
- DB3 Semantic Parity: PASS.
- BAT Runtime Role Current Contract: PASS.
- Backoffice Boundary/Frontend Consumer: PASS.
- Backoffice OpenAPI: 96/96 PASS.
- ADM OpenAPI: 337/337 PASS.
- Replay Product Source SHA-256: `7c7b806d4284a5a655731cff60b3cce214cdcec9f73ce489b9f3f96bf9bac809` — 작업본과 동일.

이 Replay는 Source/정적/패키지 적용성을 검증한 것이며 Java25/Docker 물리 Runtime PASS를 의미하지 않는다.

