# CPF C 개발/QA 관리_2_1 — 중간 체크포인트 상태 — 2026-08-29

## 1. 판정

- 상태: **CHECKPOINT / INCOMPLETE — 최종 완료본 아님**
- 목적: 현재 개발 변경을 손실 없이 다음 세션으로 승계하고, 로컬 Windows Physical Runtime 및 남은 Open Git/Final Closure를 이어서 수행하기 위한 중간 보존본
- 전체 완료/QA 통과 선언: **금지**
- 현재 Canonical Inventory: **188행**
- Developer Status: `{'완료': 169, '미완료': 19}`
- Verification Status: `{'완료': 7, '미검증': 181}`
- Overall Status: `{'완료': 7, '미검증': 168, '재확인 필요': 10, '실패': 3}`
- Developer Requirement Ledger: **216행**

## 2. Source Identity

- 최신 병행 Local Source baseline ZIP 기준 Identity: `e5a652fd0d3d85b764196d066a690065224048c72efcba034e97395a9382cca4` / 8378 files
- 현재 Checkpoint Product Source Identity: `1ebf45cd79a528d462c150a73686505a3f2daa7ad1074e5c3bc42a387eefa032` / 8390 files / 43820019 bytes
- Identity policy: `GIT_INDEPENDENT_CANONICAL_PATH_SIZE_SHA256_LINES`
- Git HEAD/Branch: 이 ChatGPT 작업복사본은 `.git`이 없는 전달 ZIP 기반이므로 **UNAVAILABLE**. 로컬 적용 후 read-only Git 명령으로 확인한다.

## 3. 이번 체크포인트에 포함된 핵심 개발

- VS Code/JDT nullness warning 계열 Source 보정 및 동일 Root Cause 회귀
- Runtime UTF-8 child-process boundary 보정 및 fail-closed verifier/test
- 거래 DB Logging의 독립 Transaction/오류 결과 보존 관련 Source/Verifier/Requirement currentization
- Physical DB canonical 정책: Product Runtime `cpfDB/mbwDB/mbrDB/exsDB`, legacy active `cmnDB/admDB/batDB/refDB/bzaDB=0` 방향의 Source/Verifier currentization
- DB/Generator/Runtime/Open Git 관련 stale verifier 및 False-Green/False-Red 보정
- Unified `cpf` Java CLI exactly-one owner 방향과 Windows/Linux Thin Wrapper, Command/Entrypoint Catalog
- Open Git Binary/Source profile, Public CLI projection, Framework Source/sources.jar 기본 비공개 정책
- `cpf-release/`를 Open Git 전달 전용 Current-only staging으로 두고 Private CPF master Git/Source Identity에서 제외하는 정본/Source 방향
- Release Tool 자동 Git add/commit/push 금지 및 VERIFIED 이후 **사용자 Open Git 직접 반영 명령만 생성**하는 방향
- 보호 Docker 개발테스트 설치 스크립트 3종 복구
- Current-only 삭제 대상은 Root-relative `DELETE_MANIFEST.csv`로 관리

## 4. 현재 검증 해석

이 세션 중 Targeted/Canonical/DB/Generator/Unified CLI/Open Git 계약 Gate에서 다수 PASS를 확인했으나, 이후에도 Open Git/정본 Source가 변경되었다. 따라서 과거 PASS를 현재 Identity의 최종 Physical PASS로 자동 승계하지 않는다.

현재 Checkpoint의 완료조건은 **Source 보존**이지 전체 Requirement Closure가 아니다. 특히 아래는 다음 세션에서 현재 Source Identity 기준으로 다시 닫아야 한다.

- Open Git 최신 Git-boundary Steering 반영 후 targeted/full regression
- Actual `cpf-release/` Fresh Binary/Source profile 생성 및 Framework Source leakage 0
- Java 25 / Gradle 9.1 Root Build/Test/Publication/SBOM
- Unified CLI PUBLIC/INTERNAL capability 실제 Java25 JAR 및 Windows/Linux lifecycle
- Fresh VS Code Java25/Gradle Import Error 0 / Warning 0
- DB3 Oracle/PostgreSQL/MariaDB Physical Full Lifecycle
- Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile
- One-WAS actual transaction + DB/File/Segment/Timeline logging correlation
- ADM/Backoffice Runtime OpenAPI
- Frontend lint/typecheck/test/build + Browser E2E/a11y/error statuses
- signed Performance live/load/soak
- Full Runtime `FAIL=0 / mandatory SKIP_ENV=0 / NOT_EXECUTED=0 / UNKNOWN=0`
- Source drift=0 / Managed drift=0 / mojibake=0 / legacy active DB=0
- Same Source Fresh Replay
- Inventory 188/188 Closure Review + Codex continuation/independent verification

## 5. Git / Open Git Boundary

- `cpf-release/`는 **Open Git 전달 전용 local-generated staging**이다.
- `cpf-release/` 결과물은 CPF Private/master의 Commit/Push 대상이 아니다.
- CPF master에는 Release Generator/CLI/Policy/Test/정본 Source만 사용자가 검토 후 반영한다.
- Open Git Release Gate가 전부 PASS한 후에만 사용자가 Open Git repository에서 직접 `git add/commit/push`한다.
- Tool/CLI/DevGPT/Codex는 사용자 승인 없이 Git write를 수행하지 않는다.

## 6. Delete Manifest

- 전체 행: 357
- 현재 `approved=true + user_approved=true + precondition=SATISFIED + lifecycle=PENDING_USER_EXECUTION`: **349건**
- 보호 경로는 삭제 명령에서 fail-closed한다.
- Replacement가 정의된 경우 Replacement 존재를 확인한 후 삭제한다.
- 삭제 후 low-cost/current-final/hygiene 및 Git status를 다시 확인한다.

## 7. Checkpoint Overlay 재현성 검증

- 최신 baseline에 Checkpoint Overlay 적용 후 Delete Manifest를 실행하는 fresh snapshot 검증: **PASS**
- 적용 후 Source Identity가 Checkpoint 작업본과 exact match: **PASS**
- 저비용 Current Final/Hygiene/Garbage/UTF-8/Physical DB/Unified CLI: **PASS**
- 따라서 이 ZIP은 다음 세션/로컬 작업을 이어가기 위한 **재현 가능한 CHECKPOINT**다. 단, 전체 Runtime 미완료이므로 FINAL이 아니다.
