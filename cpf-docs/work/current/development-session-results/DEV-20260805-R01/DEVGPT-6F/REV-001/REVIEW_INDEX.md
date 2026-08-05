# DEVGPT-6F Development Review Index

Baseline: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`

## Scope

- `scope/WORK_ITEM_SCOPE.csv` — 224 exact Work Item IDs
- `scope/CANONICAL_REQUIREMENT_SCOPE.csv` — 58 Canonical IDs
- `scope/CPF_FR_SCOPE.csv` — 5,658 exact Requirement IDs
- `scope/CPF_SC_SCOPE.csv` — 7,878 exact Scenario IDs
- `scope/ENGINEERING_GATE_SCOPE.csv` — 21 Gate IDs
- `scope/EXCLUDED_WORK_ITEM_OWNER_VALIDATION.csv` — 601 excluded IDs and owner basis

## Individual review

- `review/WORK_ITEM_DEVELOPMENT_REVIEW.csv` — 224 rows
- `review/REQUIREMENT_DEVELOPMENT_REVIEW.csv` — 5,658 rows
- `review/SCENARIO_DEVELOPMENT_REVIEW.csv` — 7,878 rows
- `review/ENGINEERING_GATE_RESULT.csv` — 21 rows
- `REQUIREMENT_STATUS.csv` — DevGPT-owned status projection

## Current truthful status

- Work Item: {'부분 구현': 66, '재확인 필요': 146, '미검증': 12}
- Requirement: {'부분 구현': 1331, '재확인 필요': 4083, '미검증': 244}
- Scenario: {'부분 구현': 1798, '재확인 필요': 5792, '미검증': 288}
- Unreviewed / missing evidence field / missing consumer field: 0 / 0 / 0
- Unimplemented Work Item: 0
- Final PASS auto-promotion: 0

`재확인 필요`, `부분 구현`, `미검증` are not treated as CPF final completion.
