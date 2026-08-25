# CPF Developer GPT 다음 세션 상세 인수인계 — 2026-08-25

## 1. Source Identity

- Baseline: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260824_203050.zip`
- Baseline SHA-256: `0eba1e95d1552342a128984930b0f0f533787caad209f8b9e7f04ddcacf7caf1`
- Current Product Source SHA-256: `7c7b806d4284a5a655731cff60b3cce214cdcec9f73ce489b9f3f96bf9bac809`
- Current Source files: `8434`
- Current Source bytes: `49517641`
- Overlay SHA-256: 최종 ZIP 생성 후 사용자 응답에서 전달(자기참조 해시 방지로 문서 내부 미기록).
- Overlay filename: `CPF_DEVELOPER_GPT_OVERLAY_RUNTIME_PENDING_20260825_102512.zip`

## 2. 절대 보호 — Codex

Developer GPT는 `cpf-docs/work/current/CODEX_*`, Codex 전용 Work Package, `cpf-docs/work/evidence/codex/current/**`를 읽기 전용으로 유지했다. Baseline 대비 **1526 files changed=0 / missing=0**. Developer GPT 완료를 Codex 원장에 대신 CLOSED/PASS 처리하지 않는다.

## 3. 상태

- 개발완료: **완료**
- 정적검증완료: **PASS**
- 런타임검증완료: **미실행 / VERIFICATION_PENDING**
- Codex검증완료: **PENDING**
- 전체 Closure: **VERIFICATION_PENDING**

## 4. 실제 변경

### DB3
- `referenceFixture productionDefault=false`를 production source/install에서 분리.
- Oracle/PostgreSQL/MariaDB semantic/token/lifecycle/manifest/development/schema governance PASS.
- V001~V137/R001~R137 immutable: 629 checked, changed=0.
- 신규 V138/R138: CEC Center-Cut Runner role currentization.
- 신규 V139/R139: MBW Approval Execution lifecycle.

### BAT/CEC
- 일반 Batch `BAT/BAT`.
- Center-Cut Runner `CEC/CEC/CENTER_CUT_RUNNER`; Registry 독립 Runtime Instance 계약.
- 기능명 `CENTER_CUT`은 capability/launch mode로 유지하고 Runtime Role과 분리.
- Deploy inventory/topology runtimeRole도 `CENTER_CUT_RUNNER`로 currentize.

### Batch Job/ADM
- `FILE_WATCH` 실제 Worker Consumer 연결; 승인 PATH_ALIAS와 상대경로/안정화/marker/size/SHA 계약.
- `CENTER_CUT:<jobId>` 실제 Control Plane launch Consumer 연결; 활성 Job validation.
- ADM Batch 등록 화면에 실행유형 선택과 FILE_WATCH/CENTER_CUT 전용 입력 추가.
- 재발방지 verifier PASS.

### Approval
- MBW 업무 판단문서/Before-After/History Read, versionNo/payloadHash 결정 재검증.
- 승인과 실제 적용결과를 MBW_APPROVAL_EXECUTION으로 분리.
- UNKNOWN Reconcile은 mutation 재호출 금지/read-only owner-state 대사.
- Approval ID 직접 결정 UX 제거.

### Open Git
- 사용자 Steering 원문을 `CPF_OPEN_GIT_FRESH_RELEASE_REQUIREMENT.md`로 보존.
- canonical CLI dispatcher/wrapper 보완.
- pytest 17/17 PASS.
- Fresh Framework Java25 publication/external clean consumer는 Runtime pending.

### Hygiene/Identity
- Gradle/JVM disposable state를 Repository 밖 managed-work root로 이동.
- Root 외 nested `cpf-docs` 및 cache/build garbage는 Delete Manifest 114 directory 대상.
- Clean Source simulation PASS.
- Source Identity가 runtime log/cache에 흔들리지 않도록 generated scope 보정.

## 5. 실제 정적 PASS

상세 명령/결과는 `cpf-docs/deliverables/TEST_AND_EVIDENCE.md`. 주요 결과: Java syntax 2956/0 errors, DB3 core gates PASS, Backoffice OpenAPI 96/96, ADM 337/337, Open Git 17/17, Batch executor PASS, Approval/Logging/Unknown PASS.

## 6. 다음 세션의 첫 작업

**다시 Source 개발부터 시작하지 않는다.** 사용자가 Overlay+Delete Manifest를 적용한 Working Tree에서 Full Runtime 결과 로그부터 확인한다.

1. `cpf-source-state.py` SHA가 `7c7b806d4284a5a655731cff60b3cce214cdcec9f73ce489b9f3f96bf9bac809`와 일치하는지 확인.
2. `run-cpf-required-full-runtime-validation.ps1` 전체 로그 확인.
3. 실패가 있으면 첫 오류만 고치지 말고 전체 실패를 Root Cause별로 묶어 Source/Test/Generator/DB3를 일괄 보정.
4. 수정 후 부분 Stage가 아니라 같은 Full Runtime 전체를 재실행.
5. Oracle/PostgreSQL/MariaDB 모두 전체 객체 Fresh 초기화부터 실제 거래/Upgrade/Rollback-Recovery/Reapply가 PASS해야 DB Runtime PASS.
6. Batch는 실제 FILE_WATCH, CENTER_CUT, Worker Kill/Lease/Fencing/UNKNOWN/Reconcile/Recovery까지 확인.
7. Approval은 실제 Owner 적용/FAILED/UNKNOWN/Reconcile/History 재조회 확인.
8. Browser/OpenAPI/Open Git external clean consumer까지 실행.
9. ExitCode 0, SKIP/NOT_EXECUTED/UNVERIFIED 0 이후 Developer GPT 원장의 `런타임검증완료`를 PASS로 변경.
10. 그 다음 Codex에 최신 Source와 Developer GPT Evidence를 주고 Codex 원장은 그대로 둔 채 독립 재검수.

## 7. 보호 Path 200자 예외

현재 200자 초과 47개는 모두 Codex 보호 Evidence 아래의 기존 파일이다. Developer GPT는 사용자 지시상 Codex Evidence를 rename/delete하지 않았다. Codex 재개 시 자체 Evidence currentization 대상으로 남긴다.

## 8. 로컬 전체 Runtime 명령

```powershell
pwsh -NoProfile -File .\cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1 -RepoRoot (Get-Location).Path -DockerRoot 'C:\dev\Docker' -DockerSecretFile 'C:\dev\Docker\Secrets\cpf-runtime.env' -OutputRoot "$HOME\Downloads"
```

정상 기대: ExitCode 0 / Final PASS / SKIP·NOT_EXECUTED·UNVERIFIED 0.

## 9. Fresh Overlay Replay

최종 Overlay+Delete Manifest를 baseline fresh copy에 적용한 뒤 Clean/Java syntax/Batch Executor/Approval/Batch UNKNOWN/DB3 Semantic/Runtime Role/Backoffice/OpenAPI Gate를 재실행했고 모두 PASS했다. Replay Source SHA-256은 `7c7b806d4284a5a655731cff60b3cce214cdcec9f73ce489b9f3f96bf9bac809`로 작업본과 동일하다.
