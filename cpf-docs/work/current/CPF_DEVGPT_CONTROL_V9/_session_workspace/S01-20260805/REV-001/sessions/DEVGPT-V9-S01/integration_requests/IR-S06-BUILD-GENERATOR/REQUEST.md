# IR-S06-BUILD-GENERATOR — Root Build·Generator·V9 Assignment 보정

- Parent request: `DEVGPT-V9-S01`
- Integration owner: `DEVGPT-V9-S06`
- Baseline SHA: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Status: `미완료 / 재확인 필요`

## Reproduced defect

- `build_full_assignment.py`가 Requirement Master의 `canonical_requirement_ids` 복수형을 canonical alias로 읽지 못한다.
- Scenario Master의 `linked_requirement_id`를 Requirement canonical로 역참조하지 못한다.
- alias를 로컬 보정한 뒤에도 전역 Canonical과 Work Item canonical 불일치 때문에 23,040건이 unresolved로 남는다.

## Required correction

1. plural canonical alias와 linked requirement fallback을 정식 스크립트/test에 추가한다.
2. 전역 169 Canonical / 775 Work Item / 30,558 CPF-FR / 40,763 CPF-SC mapping에서 unresolved, duplicate, orphan, unknown owner를 0으로 만든다.
3. Root Gradle Java 25 build/test/publication와 Generator/Sample/EDU/BOM parity를 수행한다.
4. S01 제안 Security Starter 변경을 Canonical Catalog와 publication metadata에 반영한다.
5. 명령, Exit Code, 실제 결과, exact SHA를 회신한다.

S01 범위 2,446/3,850은 독립 재구성으로 검산했으나, 전역 Assignment Gate는 S06 보정 전 미완료다.
