# CPF 전체 기능 Requirement 신규 원장 R3

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Baseline SHA: `4aea798c913787e86341809e2cef2b9495cbf7ba`
- 생성 시각: `2026-08-03T10:28:00+09:00`
- 기능 Requirement: **30,558건**
- 실행 Scenario: **40,763건**
- R3 추가 Requirement: **7,896건**
- 실행 Phase: **16단계**
- 초기 상태: 개발GPT/자체검수/Codex 모두 `미완료`, 전체 `재확인 필요/미검증`

## 목적

R3는 단순 목록 확장이 아니라 다음을 종료 조건으로 삼는다.

1. 최신 Repository의 모든 제품 표면을 Inventory로 추출한다.
2. 모든 Source·API·SQL·Config·Frontend·Script·Test·Doc·Evidence를 Requirement에 연결한다.
3. 모든 Requirement를 실제 Consumer·상태전이·실패·복구·권한·감사·실행 Evidence로 검증한다.
4. 선행 Gate가 통과하기 전에 후행 개발을 시작하지 않는다.
5. 최종 Commit에서 Inventory를 다시 생성해 새 미등록 기능·결함이 0건인지 검증한다.
6. Codex 독립 검수 후 QA가 최신 master 전체를 다시 검수하고 통과한 경우에만 종료한다.

## 반복 작업 방지 순서

`P00 정본/안전 → P01 Inventory/Trace → P02 Architecture → P03 공통 계약 → P04 DB → P05 Capability → P06 Generator → P07 Runtime → P08 ADM/BZA Backend → P09 Frontend → P10 Fault/Recovery → P11 Security/Performance/DR → P12 Release/Migration → P13 Fresh Clone → P14 Codex → P15 QA`

각 Phase는 `CPF-GATE-NN`을 통과해야 다음 Phase를 시작할 수 있다.

## 완료 판정

R3의 모든 Requirement와 Scenario가 QA 통과하고 `CPF_COVERAGE_CLOSURE_MATRIX.csv`의 미등록·불일치가 0건이면, **현재 정본과 지원 범위에서 CPF 상용 Framework 개발·검수 완료**로 판정할 수 있다. 새로운 제품 범위나 외부 표준 변경은 신규 Requirement로 관리한다.

## 분할 Master 원장 해석 규칙

`CPF_REQUIREMENT_MASTER.csv`와 `CPF_SCENARIO_MASTER.csv`는 삭제되지 않았으며 분할 Part 인덱스로 유지된다.
작업자는 두 메인 파일의 `part_path`를 `part_sequence` 순으로 모두 읽어 논리 원장을 구성해야 한다.
상세 규칙은 `CPF_SPLIT_MASTER_DATASET_GUIDE.md`를 따른다.
메인 인덱스 행을 Requirement/Scenario로 계산하거나 일부 Part만으로 완료 판정하는 것을 금지한다.

## R5 8MB 논리 원장 기준

Requirement·Scenario·Execution Sequence 메인 CSV는 삭제하지 않고 분할 인덱스로 유지한다.
실제 데이터는 각 8,000,000 bytes 미만 Part에 저장한다.
모든 Part를 `part_sequence` 순서로 결합해야 하며, 일부 Part만으로 작업 범위나 완료 상태를 판정하지 않는다.
