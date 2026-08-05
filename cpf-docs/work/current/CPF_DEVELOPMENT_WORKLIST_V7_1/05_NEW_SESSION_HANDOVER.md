# CPF V7.1 신규 개발 세션 인수인계

## 1. 프로젝트와 정본

- 정식 명칭: **Core Platform Framework**
- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- V7.1 상세 개발 Backlog: `cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1/`
- 마지막 확인 `master` SHA: `f3814ccfb80a39be80772521826b671d692955e7`
- Commit 메시지: `04-05`
- 이전 Commit: `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` (`04-04`)
- QA 혼합 Patch 비교 시작점: `f97655c1299936a1101bc3ec10239265ec3b502e` (`04-03`)
- 확인 시각 기준: 2026-08-05, Asia/Seoul

새 세션은 위 SHA를 자동 승계하지 않는다. 작업 시작 시 최신 `origin/master`, exact SHA와 Working Tree를 다시 확인한다.

## 2. 04-05 Push 확인 결과

`f3814ccfb80a39be80772521826b671d692955e7`는 `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06`보다 1 Commit 앞서 있으며 뒤처짐은 없다.

Push된 경로:

```text
cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1/
```

확인된 구성:

```text
Git 변경 파일: 53개
Canonical Requirement: 169개
실제 실행 Work Package: 775개
WORK_ITEM_INDEX 행: 775개
분할 Ledger 행: 775개
분할 Ledger 컬럼: 21개
상세 Work Package Card: 775개
최대 파일: 276,171 bytes
400KB 초과 파일: 0개
10MB 초과 파일: 0개
```

Push는 Repository Root 기준 경로에 정상 배치됐다.

단, 이 Commit에는 GitHub Combined Status와 연결된 Workflow Run이 확인되지 않았다. 따라서 **문서 Push 성공만 확인된 상태이며 Build·Runtime·QA 통과를 의미하지 않는다.**

## 3. Push 후 발견한 문서 정합성 결함과 보정

V7.1에서는 대형 `WORK_ITEM_LEDGER.csv`를 제거하고 다음 구조로 바꿨다.

```text
WORK_ITEM_INDEX.csv
→ markdown_file
→ ledger_part
→ ledgers/*_WORK_ITEMS.csv
```

그런데 Push된 다음 문서에 이전 파일명이 남아 있었다.

```text
01_COMMON_ENGINEERING_GATES.md
99_EXPERT_REVIEW.md
```

보정 내용:

```text
WORK_ITEM_LEDGER.csv 참조
→ WORK_ITEM_INDEX.csv + ledger_part + GATE_APPLICABILITY_MATRIX.csv 참조

V7 최대 파일 1.1MB 표기
→ V7.1 최대 파일 WORK_ITEM_INDEX.csv 276,171 bytes로 정정

V7 기준 배정 설명
→ V7.1 Index→상세 Part→분할 Ledger 읽기 순서로 정정
```

신규 세션은 이 인수인계와 함께 제공된 문서 정합성 보정 Overlay가 Git에 반영됐는지 먼저 확인한다. 반영 전이라면 위 두 문서의 오래된 `WORK_ITEM_LEDGER.csv` 문구를 작업 정본으로 사용하지 않는다.

## 4. V7.1 읽기 순서

전체 775개 Work Package와 모든 상세 파일을 한 번에 Context에 넣지 않는다.

```text
1. 00_APPLICATION_POLICY.md
2. 01_COMMON_ENGINEERING_GATES.md
3. 03_BASELINE_STABILIZATION.md
4. WORK_ITEM_INDEX.csv
5. 선택 Work Item의 markdown_file
6. 선택 Work Item의 ledger_part
7. GATE_APPLICABILITY_MATRIX.csv의 적용 Gate
8. STANDARD_CROSSWALK.csv의 관련 표준
9. 현재 CPF-FR Requirement와 CPF-SC Scenario 원장
10. 최신 실제 Source·SQL·API·Test·Config·Frontend·Script
```

`WORK_ITEM_INDEX.csv`는 탐색용이다. 상태 전체 컬럼은 `ledgers/*_WORK_ITEMS.csv`에서 관리한다.

## 5. 작업 범위 선정

개발 세션 수는 고정하지 않는다.

첫 작업 전에 전체 담당 범위를 분석하고, 같은 State Owner·Source·Consumer·호출 경로·Test Harness를 공유하는 Work Package를 Connected Functional Slice로 묶는다.

기본 배정 크기:

```text
3~15 Work Package
```

단, 하나의 실제 호출 경로를 숫자 때문에 중간 절단하지 않는다.

선정 순서:

```text
P0 Baseline 안정화
→ P0 Work Package
→ 선행 Dependency
→ P1
→ P2
```

각 Work Package마다 연결된 CPF-FR Requirement와 CPF-SC Scenario를 현재 논리 원장에서 전수 추출한다.

## 6. 중간 Checkpoint 후 자의적 종료 금지

다음은 작업 종료 사유가 아니다.

```text
단일 P0 결함 수정
일부 Test 성공
ZIP·Manifest·Hash 생성
중간 Checkpoint 작성
환경 또는 도구 일부 부재
작업 범위가 큼
Context가 길어짐
새 결함 발견
```

담당 Work Package, Requirement ID와 Scenario ID를 하나씩 모두 개발·검수하고, 다음 조건이 충족될 때까지 같은 작업 라인을 계속한다.

```text
담당 Work Package 개별 판정 100%
담당 CPF-FR Requirement 개별 판정 100%
담당 CPF-SC Scenario 개별 판정 100%
미검수 ID 0
Evidence 없는 판정 0
실제 Consumer 미확인 0 또는 근거 있는 해당 없음
수정 가능한 P0/P1 구현 완료
적용 Gate 전부 판정
```

전체 선분석 후 효율적으로 묶어 개발하는 것은 허용하고 권장한다. 하지만 묶음 결과를 관련 ID에 자동 복제하거나 대표 ID만 검사하면 안 된다.

물리적인 Context·도구 한계에 도달한 경우에만 Checkpoint를 남긴다.

Checkpoint 필수 내용:

```text
완료한 마지막 Work Item
완료한 마지막 Requirement ID
완료한 마지막 Scenario ID
정확한 다음 시작점
현재 변경 파일
실패 명령과 원인
다음 재실행 명령
미검수 Work Item·Requirement·Scenario 수
```

미검수 항목이 남은 Checkpoint를 `FINAL`, `Complete`, `완료`로 표시하지 않는다.

## 7. 필수 결과와 비강제 구현 제안

다음은 강제 기준이다.

```text
CPF Final Target
승인 Architecture·ADR·Specification
공식 Module·Package·DB/State Ownership
Public API·SPI·Internal 경계
Acceptance Criteria와 Scenario
보안·권한·감사·마스킹
DB Vendor·Migration·Rollback
Test·Runtime·Evidence 규격
공통 Engineering Gate
```

Work Package의 Class·Package 세부구조·Library·알고리즘·Test 도구·구현 순서는 비강제 제안이다.

제안과 다른 구현도 허용하지만 다음을 모두 만족해야 한다.

```text
정본과 표준 준수
실제 Product Consumer 연결
동등 이상의 오류·복구·보안·운영 품질
호환성·Migration·Rollback 영향 처리
동등 이상의 Test와 Evidence
대안 선택 근거 기록
```

## 8. DB·SQL·Query 변경 공통 강제 순서

모든 Work Package는 DB·SQL·Query 영향을 판정한다. 무관하면 실제 호출 경로에 근거한 `N/A`를 기록한다.

영향이 있으면 다음 연쇄를 전부 점검한다.

```text
Canonical Model·Query Contract
→ Generator·Template
→ Generated SQL·Mapper
→ Fresh Install·Bootstrap·Initialization
→ Mandatory Seed
→ Upgrade Migration·Data Transform
→ Rollback 또는 Forward Recovery
→ MariaDB·PostgreSQL·Oracle
→ Repository·Mapper·Service·Consumer
→ API·OpenAPI·Generated Client
→ Frontend·Batch·ADM/BZA
→ Sample·Golden Generated Domain·EDU
→ Test·Drift·Evidence
```

Vendor SQL이나 Generated SQL만 직접 수정하고 Generator·초기화·Migration·Rollback·다른 Vendor와 Consumer를 누락하면 완료가 아니다.

## 9. 직접검증 우선·대체검증 의무

검증 가능 여부를 추정해 실행을 생략하지 않는다.

```text
목표 환경 직접 실행
→ 실패 단계와 실제 오류 기록
→ 가능한 대체검증 수행
→ 확인한 Acceptance와 남은 실제 Runtime 차이 분리
```

직접검증 대상 예:

```text
Java 25·Gradle
MariaDB·PostgreSQL·Oracle
Kafka·JMS·IBM MQ·RabbitMQ
Browser E2E·접근성
다중 JVM·Process Kill
Fresh Clone·Publication·SBOM·Provenance
```

환경이 없어도 Source·Test·Harness 구현을 중단하지 않는다. Mock 성공이나 정적 문자열 확인만으로 실제 Runtime 완료 처리하지 않는다.

## 10. 현재 우선순위

먼저 `03_BASELINE_STABILIZATION.md`의 28개 항목을 확인한다.

특히 다음은 P0 우선이다.

```text
OpenAPI·Generated Client 계약 불일치
Masking·Logging·Reconciliation 안전성
Runtime Apply·Rollback·Agent 복구
Broker·TCP·Outbox/DLQ 신뢰성
3 Vendor Seed·Migration·Oracle Secret
Java 25 실제 Toolchain
SLSA·CycloneDX와 Secret/Evidence Sanitization
Requirement·Scenario Owner·Coverage
Source·Generated·SQL·API Drift
```

`04_REQUIREMENT_GAP_PROMOTION_REVIEW.md`의 24개 Gap은 자동 Canonical Requirement가 아니다.

```text
PROMOTE_NEW_CANONICAL
MERGE_STRENGTHEN
REVIEW_FOR_CANONICAL_SPLIT
DEFER_OUT_OF_SCOPE
```

판정을 따라 기존 ID와 중복을 확인하고 `REQ-GAP` 절차로 처리한다.

## 11. Git 안전

사용자 승인 없이 다음을 하지 않는다.

```text
Commit
Push
Branch
Tag
PR
Release
Reset
Restore
Stash
삭제
Working Tree 정리
History 변경
```

다음 광범위 명령은 실행하거나 제안하지 않는다.

```text
git clean
git reset --hard
git restore .
```

삭제 후보는 `DELETE_MANIFEST.csv`에 exact path와 근거를 기록하고 사용자 승인 전 실제 삭제하지 않는다.

Commit SHA는 다음 시점에만 필수 기록한다.

```text
작업 시작 baseline
Checkpoint 또는 종료 시 baseline + git status + 변경 경로
사용자 승인 후 통합 Commit
QA 최종 검수 Commit
```

모든 명령마다 SHA나 Repository 전체 Hash를 반복 계산하지 않는다.

## 12. 역할과 상태

개발 GPT는 구현과 자체검수를 수행하고 자기 역할 상태만 갱신한다.

```text
개발 GPT
→ Codex 독립 검수·보완
→ QA 최신 Git 검수
→ QA 통과 시 Requirement 최종 완료
```

개발 GPT가 Codex 또는 QA 상태를 변경하면 안 된다.

개발 상태와 검증 상태를 분리한다.

```text
development_status:
완료 / 부분 구현 / 미구현 / 실패 / 재확인 필요

verification_status:
완료 / 미검증 / 실패 / 재확인 필요
```

실제 Runtime 검증이 남으면 전체 완료가 아니다.

## 13. 세션 시작 확인 명령

다음은 읽기 전용 확인 명령이다.

```bash
git fetch origin
git rev-parse origin/master
git rev-parse HEAD
git status --short
git log -1 --oneline origin/master
```

기대 기준은 현재 확인 시점에 다음과 같다.

```text
origin/master = f3814ccfb80a39be80772521826b671d692955e7
04-05
```

다른 SHA가 확인되면 최신 Git과 V7.1 파일 변경 내용을 먼저 다시 분석한다.

## 14. 제출 산출물

담당 범위에 맞게 다음을 Repository Root 기준 Root Overlay로 제출한다.

```text
REVIEW_INDEX.md
SESSION_SCOPE.csv
DEVELOPMENT_EXECUTION_PLAN.csv
FUNCTION_POINT_STATUS.csv
REQUIREMENT_DEVELOPMENT_REVIEW.csv
SCENARIO_DEVELOPMENT_REVIEW.csv
CHANGE_MANIFEST.csv
TEST_AND_EVIDENCE.md
OPEN_ISSUES.md
CROSS_SESSION_CHANGE_REQUEST.csv
DELETE_MANIFEST.csv
제품 Source·SQL·API·Test·Config·Frontend·Script
Evidence
```

상세 상태는 해당 `ledgers/*_WORK_ITEMS.csv`에서 갱신한다.

사용자 승인 전 Commit·Push는 하지 않는다.

## 15. 신규 세션의 첫 실행 목표

신규 세션은 보고서 작성부터 시작하지 않는다.

```text
최신 Git 확인
→ V7.1 Index·Gate·해당 Part 읽기
→ 담당 전체 Scope와 Dependency 분석
→ Connected Functional Slice 계획
→ 실제 구현
→ 직접검증·대체검증
→ Work Item·Requirement·Scenario 개별 판정
→ 담당 전체 완료 전까지 계속
```

최초 작업 결과는 계획 문서만이 아니라 최소 하나 이상의 실제 Connected Functional Slice 구현·Test·Evidence를 포함해야 한다.
