# CPF 문서 정본 Index

> 원칙: **현재 역할별 문서 하나만 유지한다.** 과거 Steering/ADR/Session/Checkpoint/Revision 문서는 Current Requirement에 의미를 흡수한 뒤 Repository Current Surface에서 제거한다.

## 1. 최상위 개발/QA 정본

1. `CPF_FINAL_TARGET_REQUIREMENTS.md` — 최상위 제품 Target, Architecture, 205 Current Requirement
2. `CPF_CANONICAL_PATH_AND_ROLE_MAP.md` — 경로/Owner Navigation 전용
3. `../work/current/CPF_CURRENT_WORK_REQUEST.md` — 현재 개발 요청과 우선순위
4. `../work/REQUIREMENT_STATUS.csv` — 개발 GPT 역할 상태/검증 상태 원장. QA/Codex 소유 컬럼은 각 역할 권한을 따른다.
5. `../deliverables/TEST_AND_EVIDENCE.md` — **현재 Source에서 실제 실행한 검증 Evidence**
6. `../deliverables/OPEN_ISSUES.md` — 현재 미해결 Source/Runtime/QA Gap
7. `../work/current/CPF_DEVELOPMENT_HANDOVER.md` — 현재 세션/PC 인수인계. 정본 정책을 복제하지 않고 Source identity, 실제 실패/성공, 다음 실행 조건만 관리한다.

## 2. Current Deliverable 역할

`cpf-docs/deliverables/`는 현재 패키지/검증 산출물의 단일 위치다. 다음 역할의 동일 파일을 `cpf-docs/work/`에 중복 유지하지 않는다.

- `TEST_AND_EVIDENCE.md` — 실행 Evidence
- `OPEN_ISSUES.md` — 미해결 Gap
- `QA_REWORK_REQUEST.md` — QA 재개발/재검수 요청
- `CHANGE_MANIFEST.csv` — 현재 전달 패키지 변경 목록
- `PACKAGE_MANIFEST.json` — 현재 전달 패키지 식별/구성
- `DELETE_MANIFEST.csv` — Root-relative 삭제 Audit/승인 목록

`cpf-docs/work/`는 Requirement 상태, 개발 중 추적 자료, 현재 실행 원장을 위한 공간이며 위 Deliverable 역할을 복제하지 않는다.

## 3. Steering / Review / Handover 규칙

- Steering에서 구현 의미를 바꾸는 내용은 `CPF_FINAL_TARGET_REQUIREMENTS.md`의 해당 Owner 절/Requirement로 먼저 흡수한다.
- 흡수 완료 후 별도 `STEERING_*`, 과거 ADR, Session/Checkpoint/완료 Review가 같은 정책의 두 번째 정본으로 남지 않게 한다.
- Review/Handover가 필요하면 현재 Source identity, 실행 결과, 미검증 조건, 다음 실행 명령을 기록하되 Architecture/Requirement를 다시 정의하지 않는다.
- 같은 이름의 `CURRENT_WORK_REQUEST`, `TEST_AND_EVIDENCE`, `OPEN_ISSUES`, `CHANGE_MANIFEST`, `PACKAGE_MANIFEST`를 둘 이상의 경로에 유지하지 않는다.

## 4. 사용자 공식 문서

README와 Developer/Batch/Operator/Gateway/Specification 등 공식 사용자 문서는 제품 사용법과 선택 기준을 설명한다. 이들은 Target을 변경하지 않으며 Target의 Public API/Config/Operation/DB 계약을 소비한다.

개발자 문서는 교과서가 아니라 **무엇을 만들 때 어떤 CPF 기능/명령/옵션을 선택하면 되는지 빠르게 판단**할 수 있게 구성한다. 상세 설명은 오해 가능성이 큰 기능에 집중한다.

## 5. Evidence 규칙

과거 Source SHA의 PASS를 Current Evidence로 승계하지 않는다. Current Evidence는 실제 검증한 Source identity, 명령, 환경, ExitCode, 결과를 포함한다. 부분 PASS와 전체 PASS를 구분하고 미실행 Runtime은 `미검증`으로 유지한다.

## 6. 삭제 규칙

- `_REV`, `_SESSION`, `_FINAL_FINAL`, 날짜별 복제, Checkpoint, Steering History, 완료보고 복제본을 Current Surface에 남기지 않는다.
- 정책을 대체한 ADR/Starter/Generated Domain 별도 정본은 Final Target에 의미를 흡수한 뒤 Delete Manifest로 제거한다.
- 삭제 전 링크/Verifier/다음 세션 진입점이 삭제 문서를 요구하지 않는지 확인한다.
- released DB migration처럼 제품 동작상 immutable history가 필요한 자산은 문서 History 삭제 규칙의 대상이 아니다.

## Documentation Harness 정본

CPF 공식 사용자 산출물의 생성·현행화·시각 QA·Packaging 기준은 다음 디렉터리를 단일 정본으로 사용한다.

- `cpf-docs/governance/documentation-harness/` — **CPF Documentation Harness v1.1.3**

Harness는 **사용자의 명시 요청에 의해서만 수정**한다. Source 변경, QA Finding, 작성자 판단은 Harness 자동 수정 권한이 아니다. 새 Source와 Harness가 충돌하면 `HARNESS_CHANGE_REQUIRED` 또는 `HARNESS_SOURCE_CONFLICT`로 기록하고 사용자 승인 없이 목차·규격·Coverage를 바꾸지 않는다.

과거 분산 Documentation 작성 지침은 Harness의 `DELETE_MANIFEST.txt`에 기록된 exact path를 기준으로 제거하며, 제거 이후 이 Index에서는 Harness만 산출물 작성 기준으로 사용한다.
