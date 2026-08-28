# CPF 다음 세션 인수인계 — C 개발/QA 관리_2_1 CHECKPOINT — 2026-08-29

## 1. 시작 원칙

이 문서는 **완료본이 아니라 중간 체크포인트**다. 다음 세션은 처음부터 다시 개발하지 말고 현재 미완결 WP부터 continuation한다. 이전 DevGPT의 일괄 완료판정을 자동 승계하지 않고 Canonical Inventory 188행을 실제 Source/Consumer/Test/Runtime/Evidence와 대조한다.

- Checkpoint Source Identity: `1ebf45cd79a528d462c150a73686505a3f2daa7ad1074e5c3bc42a387eefa032` / 8390 files
- Baseline Source Identity: `e5a652fd0d3d85b764196d066a690065224048c72efcba034e97395a9382cca4` / 8378 files
- Inventory: 188행
- Developer ledger: 216행
- 현재 상태: Developer `{'완료': 169, '미완료': 19}`, Verification `{'완료': 7, '미검증': 181}`, Overall `{'완료': 7, '미검증': 168, '재확인 필요': 10, '실패': 3}`

## 2. 다음 세션 첫 순서

1. 사용자가 적용한 Local Working Tree에서 `cpf-source-state.py --scope source`로 Identity를 다시 계산한다.
2. `CPF_FINAL_TARGET_REQUIREMENTS.md`, `CPF_CURRENT_WORK_REQUEST.md`, `CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv`, `CPF_OPEN_GIT_FRESH_RELEASE_REQUIREMENT.md`, `CPF_OPEN_GIT_RELEASE_WORK_PACKAGE.md`를 먼저 대조한다.
3. Checkpoint Identity와 Local Identity가 다르면 차이를 분류하고 현재 Local Source를 authority로 사용한다. reset/restore/clean 금지.
4. **Open Git 최신 Steering currentization부터 완결**한다. 특히 Release Engine/정본/Test에서 과거 `READY_TO_COMMIT`/자동 index staging 개념이 남지 않았는지 확인한다. 사용자 Git 명령을 파일로 생성하는 것은 허용하지만 Tool이 실행하면 FAIL이다.
5. Unified CLI exactly-one owner와 PUBLIC/INTERNAL capability를 재검증한다.
6. `cpf-release/`는 Private master에 절대 포함하지 않고 Open Git staging 전용으로 검증한다.
7. Source-changing 작업을 모두 끝낸 뒤 Java25/Gradle/DB3/Batch/One-WAS/Frontend/Open Git Physical Runtime을 한 번에 최대강도로 실행한다.
8. 동일 Source Fresh Replay 후 188/188 Closure Review를 수행한다.

## 3. Open Git 최종 정본 — 반드시 승계

- Open Git은 Open Source Repository가 아니라 고객 개발·실행 Distribution Repository다.
- Canonical staging root는 `<CPF_PROJECT_ROOT>/cpf-release/`.
- 기본 profile은 `binary`; Framework 구현 Source와 `sources.jar`는 미포함.
- Customer development Source(Member/External/MBW/Sample/EDU/Frontend/SQL/Test)는 실제 Source Tree로 제공.
- `source` profile은 명시적 Optional이며 Canonical Public Source Allowlist만 projection.
- Unified CLI는 `cpf` 단 하나. Java CLI가 구현 Owner, `.ps1/.cmd/.sh`는 Thin Wrapper.
- PUBLIC commands 최소 `bootstrap/domain-new/domain-sync/build/test/run/stop/reset/status`.
- INTERNAL namespace는 동일 CLI architecture를 사용하되 Public Release capability에 물리적으로 노출하지 않는다.
- Release Tool은 Generated/Verified까지만 수행. 사용자 승인 전 `git add/commit/push` 및 기타 Git write 0.
- `cpf-release/` 자체는 Private CPF master Git/Source Identity에서 제외하며 Private master Commit/Push 대상이 아니다.
- 모든 Release Gate PASS 후 사용자가 **Open Git repository에서만** 직접 Commit/Push한다.

## 4. P0 지속 항목

- P0 UTF-8: Child→Parent→Tee→File/JSON/CSV/Evidence 전 구간 mojibake 0.
- P0 Physical DB: Product Runtime DB exact `cpfDB/mbwDB/mbrDB/exsDB`; active `cmnDB/admDB/batDB/refDB/bzaDB=0`.
- P0 Transaction DB Logging: 업무 rollback과 독립된 로그 Transaction, 성공/업무오류/예외 요청·응답 결과 추적, masking/fallback.
- P0 VSCode: Fresh Java25/Gradle Import Error 0 / Warning 0.
- P0 Unified CLI/Open Git: exactly-one CLI, Binary/Source profile, Public/Internal projection, Fresh lifecycle.

## 5. Codex Credit Continuation

Codex가 5시간/주간 크레딧 소진으로 강제중단됐으면 기존 작업을 처음부터 다시 시키지 않는다. 마지막 상태를 `CLOSED / SOURCE_FIXED / VERIFICATION_PENDING / IN_PROGRESS / NOT_EXECUTED`로 분리하고 **미완결 WP부터 continuation**한다. 동일 Source에서 CLOSED + 실제 PASS Evidence가 있는 항목은 반복하지 않되 Source 변경 영향은 재검증한다. 크레딧이 적으면 신규 WP를 열지 말고 현재 WP를 완결한다.

## 6. Garbage / Current-only

- Delete Manifest Ready: 349건.
- 과거/중간/중복 정본·스크립트는 replacement와 consumer 0 확인 후 삭제한다.
- 보호 경로: `cpf-docs/deliverables/**`, `cpf-docs/guides/**`, `cpf-docs/environment/docker/**`, `cpf-tools/environment/docker-development-test/**`.
- Released migration byte/checksum/provenance는 가비지라는 이유로 삭제/수정하지 않는다.
- 현행 Canonical은 역할별 한 본만 남긴다.

## 7. 종료 조건

다음이 동시에 0/PASS일 때만 최종 완료 ZIP을 만든다.

`FAIL=0`, mandatory `SKIP_ENV=0`, mandatory `NOT_EXECUTED=0`, unresolved `UNKNOWN=0`, VSCode Error/Warning=0, mojibake=0, active legacy DB=0, Source drift=0, Managed drift=0, Actual Open Git Fresh Release PASS, Windows/Linux CLI lifecycle PASS, Same Source Fresh Replay PASS, Inventory 188/188 PASS.
