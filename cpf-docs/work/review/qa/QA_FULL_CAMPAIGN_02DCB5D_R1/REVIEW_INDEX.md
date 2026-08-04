# CPF 전체 QA Campaign — QA_FULL_CAMPAIGN_02DCB5D_R1

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 exact SHA: `02dcb5d45646469f4950cf43c371706e00458616`
- 직전 SHA: `cb305fc5363263c9607e990ba640233c28668f01`
- 생성 시각: `2026-08-04T05:03:39+09:00`
- 역할: **QA GPT — 전체 검수개발 및 제한적 직접보완**
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

## 전체 논리 범위

- Requirement: **30,558**
- Scenario: **40,763**
- 논리 항목 합계: **71,321**
- Execution: **30,558**

## 현재 판정

- **제품 QA 통과: 아님**
- **확정 Finding: 17건**
- **P0: 13건**
- **P1: 4건**
- **QA 직접보완 Finding: 5건**
- **통합 대체 회귀: 52/52 PASS**
- **최신 HEAD Spring 중복 Mapping: 7개 재현**

현재 Repository 후보는 P0 결함이 실제 Source에서 재현되므로 통과할 수 없다. 특정 Finding 발견 후 전체 QA를 종료하는 방식 대신, 이 Overlay는 다음을 강제한다.

1. Canonical split master로 Requirement 30,558행과 Scenario 40,763행의 단일 Current 원장을 생성한다.
2. 모든 행에 개별 QA 판정·Source·Consumer·호출 경로·Evidence·exact SHA가 없으면 Campaign 완료를 거부한다.
3. Java25·Root Gradle·Frontend·3 Vendor DB·Browser·Multi-instance가 성공한 뒤 마지막에 71,321행 `product-pass` Gate를 실행한다.
4. Work Package 공통 Evidence나 Finding 일괄 확대만으로 개별 Requirement를 통과/미통과시키지 않는다.

## Current 단일 원장 정본 경로

- `cpf-docs/work/current/REQUIREMENT_STATUS.csv`
- `cpf-docs/work/current/SCENARIO_STATUS.csv`

현재 Git 후보에는 위 전체 Current 상태 원장이 확인되지 않았다. Overlay의 `build-cpf-full-qa-ledgers.py`를 clean checkout에서 1회 실행해 생성한 후, 동일 파일의 역할별 컬럼만 순차 갱신해야 한다.

## 안전

Commit, Push, Branch, Tag, PR, Reset, Restore, Stash, Clean, Repository 파일 삭제는 수행하지 않았다. 제품 Source 직접보완은 Root Overlay에만 존재하며 자동 통과가 아니다.
