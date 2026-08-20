# CPF 문서 정본 Index

> 원칙: **현재 역할별 문서 하나만 유지한다.** 과거 Steering/ADR/Session/Checkpoint/Revision 문서는 Current Requirement에 흡수한 뒤 Repository에서 제거한다.

## 1. 개발/QA 정본

1. `CPF_FINAL_TARGET_REQUIREMENTS.md` — 최상위 제품 Target, Architecture, 205 Current Requirement
2. `CPF_CANONICAL_PATH_AND_ROLE_MAP.md` — 경로/Owner Navigation 전용
3. `../work/current/CPF_CURRENT_WORK_REQUEST.md` — 현재 개발 요청
4. `../work/REQUIREMENT_STATUS.csv` — 현재 상태
5. `../deliverables/TEST_AND_EVIDENCE.md` — 현재 Source 실행 Evidence
6. `../deliverables/OPEN_ISSUES.md` — 현재 미해결 문제

같은 정책을 Starter Policy, Generated Domain Policy, ADR, Steering 문서에 중복 정본으로 유지하지 않는다. 상세 기술 정책은 `CPF_FINAL_TARGET_REQUIREMENTS.md`의 Owner 절을 확장한다.

## 2. 사용자 공식 문서

README와 Developer/Batch/Operator/Gateway/Specification 등 공식 사용자 문서는 제품 사용법과 선택 기준을 설명한다. 이들은 Target을 변경하지 않으며 Target의 Public API/Config/Operation/DB 계약을 소비한다.

개발자 문서는 교과서가 아니라 **무엇을 만들 때 어떤 CPF 기능/명령/옵션을 선택하면 되는지 빠르게 판단**할 수 있게 구성한다. 상세 설명은 오해 가능성이 큰 기능에 집중한다.

## 3. Evidence 규칙

과거 Source SHA의 PASS를 Current Evidence로 승계하지 않는다. Current Evidence는 실제 검증한 Source identity, 명령, 환경, ExitCode, 결과를 포함한다.

## 4. 삭제 규칙

- `_REV`, `_SESSION`, `_FINAL_FINAL`, 날짜별 복제, Checkpoint, Steering History, 완료보고 복제본을 Current Surface에 남기지 않는다.
- 정책을 대체한 ADR/Starter/Generated Domain 별도 정본은 Final Target에 흡수 후 Delete Manifest로 제거한다.
- released DB migration처럼 제품 동작상 immutable history가 필요한 자산은 문서 History 삭제 규칙의 대상이 아니다.
