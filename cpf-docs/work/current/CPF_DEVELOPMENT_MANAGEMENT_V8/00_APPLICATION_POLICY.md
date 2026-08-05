# CPF V8 상세 개발 작업 목록 적용 정책

## 목적

이 디렉터리는 CPF 제품 Requirement를 실제 Source·SQL·API·Test·Config·Frontend·Script로 구현하기 위한 상세 실행 Backlog와 **개발 GPT 전용 관리계층**이다.

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Package 생성 기준: `faedf43a7baffdad456bf40f8e46d622db9cfc76` (`04_06`)
- 마지막 Push 확인 기준: `be03808e0bed7aa02c2978c5aa73b315359bbead` (`04_07`)
- QA 혼합 제품 Patch 비교 시작점: `f97655c1299936a1101bc3ec10239265ec3b502e` (`04-03`)
- Canonical Requirement: 169개
- 실행 Work Package: 775개
- Baseline Stabilization: 28개
- Requirement Gap 관리항목: 24개
- 개발 관리 대상: 827개
- 세션 수: 고정하지 않음

새 세션은 위 SHA를 자동 승계하지 않는다. 시작 시 최신 `origin/master`, exact SHA와 Working Tree를 다시 확인한다.

## Repository 최상위 불변 규칙

**Repository 최상위에는 신규 파일이나 신규 디렉터리를 만들지 않는다.**

- 문서·원장·Evidence: 승인된 `cpf-docs/**` 하위
- 실행 Script·검증기: 승인된 `cpf-tools/**` 하위
- 제품 Source·Test·Config: 공식 Owner Module 하위
- Session 결과: `cpf-docs/work/current/development-session-results/**`
- Root Overlay ZIP은 Repository Root에서 해제하되 ZIP 내부 첫 경로는 기존 승인 디렉터리여야 한다.
- 루트 README, 임시 Manifest, 임시 Evidence, 작업용 디렉터리를 추가하지 않는다.
- 신규 최상위 항목이 필요해 보이면 직접 생성하지 않고 사용자 승인 대상으로 올린다.

## 강제 우선순위

1. CPF Final Target와 승인 Architecture·ADR·Specification
2. 공식 Module·Package·DB/State Ownership
3. Public API·SPI·Internal 계약
4. Acceptance Criteria와 Scenario
5. 보안·권한·감사·마스킹·DB Vendor·Migration·Rollback·Evidence 규격
6. 공통 Engineering Gate
7. 이 Backlog의 필수 개발 결과
8. 이 Backlog의 비강제 구현 제안

구현 제안이 1~7과 충돌하면 제안을 적용하지 않는다.

## 개발 요청 자동 축소

개발 GPT는 `DEVELOPMENT_ITEM_STATE.csv`의 `개발GPT_작업대상상태`가 다음인 항목만 작업한다.

```text
작업 대상
재개발 대상
재검수 대상
```

다음 항목은 현재 요청에서 제외하고 다시 수정·검수하지 않는다.

```text
완료 스킵
해당 없음 스킵
소유권 검토
외부환경 차단
```

단, QA Reopen Feed, 후속 변경 영향 무효화, Evidence 무효화 또는 완료 기준 SHA 불일치가 확인되면 관련 항목을 다시 연다.

`완료 스킵`은 개발 GPT 반복 요청에서 제외된다는 뜻이며 QA 최종 통과나 CPF 전체 완료를 의미하지 않는다.

## 덮어쓰기·유실 방지

- Campaign 요청 경로는 불변이다. 같은 Campaign ID와 Revision 경로가 이미 존재하면 생성기를 실패시킨다.
- Session 결과는 `campaign/session/revision` 고유 경로에만 추가한다.
- 다른 Session 결과, 과거 Campaign, 중앙 원장, Canonical 문서를 덮어쓰지 않는다.
- 기존 제품 파일을 수정하기 전에 Campaign Baseline과 현재 HEAD, 대상 파일의 사전 Hash를 기록한다.
- Baseline 이후 다른 Commit이 같은 대상 파일을 변경했으면 수정·Overlay 적용을 중단하고 `CROSS_SESSION_CHANGE_REQUEST`로 Integration Owner에게 넘긴다.
- Generated Output은 직접 수정하지 않고 정본 Generator·Template·OpenAPI·DB Canonical을 수정해 재생성한다.
- 중앙 State 원장은 Session이 직접 수정하지 않는다. Session Result를 관리 병합기가 반영한다.

## Session 산출물 보존과 정리

각 개발 GPT 세션은 다음을 제출한다.

```text
SESSION_ARTIFACT_MANIFEST.csv
SESSION_CLEANUP_COMMAND.ps1
DEVELOPMENT_SESSION_RESULT.csv
DEVELOPMENT_REQUIREMENT_RESULT.csv
DEVELOPMENT_SCENARIO_RESULT.csv
TEST_AND_EVIDENCE.md
```

`SESSION_ARTIFACT_MANIFEST.csv`는 각 파일을 다음으로 분류한다.

```text
PRODUCT_REQUIRED
EVIDENCE_RETAINED
SESSION_TEMPORARY
GENERATED_REGENERABLE
REJECTED_ARTIFACT
```

세션 종료 인수인계에는 `SESSION_TEMPORARY`, `GENERATED_REGENERABLE`, `REJECTED_ARTIFACT` 중 승인된 항목만 exact path로 삭제하는 PowerShell 한 줄 명령을 제공한다.

제품 Source·SQL·API·Test·Config·Frontend·Script와 통합 승인된 Evidence는 자동 삭제 명령에 포함하지 않는다. 해당 제품 변경을 되돌려야 하면 별도 Reverse Change Manifest와 사용자 승인이 필요하다.

## 완료 원칙

- Work Package 하나를 완료해도 Canonical 전체가 자동 완료되지 않는다.
- Canonical에 연결된 모든 Work Package, CPF-FR, CPF-SC와 적용 Gate가 판정돼야 한다.
- 직접검증을 실제로 먼저 시도하고 실패 단계·오류를 기록한 뒤 대체검증한다.
- 환경이 부족해도 가능한 Source·Test·Harness와 대체검증을 수행한다.
- QA 최신 통합 Git 통과 전 최종 완료가 아니다.
- 사용자 승인 없이 Commit·Push·Branch·Tag·PR·Release·Reset·Restore·Stash·삭제를 수행하지 않는다.

## AI 권장 읽기 순서

```text
1. 05_NEW_SESSION_HANDOVER.md
2. 00_APPLICATION_POLICY.md
3. 01_COMMON_ENGINEERING_GATES.md
4. DEVELOPMENT_ITEM_STATE.csv에서 현재 작업 대상만 필터
5. DEVELOPMENT_ITEM_INDEX.csv에서 상세 파일·Owner·Dependency 확인
6. 선택 항목의 markdown_file과 ledger_part
7. 생성된 Requirement·Scenario Map
8. 실제 Source·SQL·API·Test·Config·Frontend·Script
9. Evidence와 기준 SHA
```
