# Information Architecture & Reader Needs — v2.12.0

문서의 품질은 내용량보다 **독자가 필요한 답을 얼마나 빨리 찾고 끝낼 수 있는지**로 판정한다. CPF 문서는 Diátaxis의 사용자 요구 구분을 참고하되 CPF 문서 체계에 맞게 적용한다.

## 문서 유형

- README: Overview + Wayfinding. 제품을 이해하고 적절한 시작/상세 문서로 이동.
- Developer Guide: How-to 중심 + 필요한 Reference. 실제 구현/선택/실패/검증을 끝냄.
- Operator Guide/Manual: How-to/Runbook. 상태 확인→판단→안전 조치→복구→완료 확인.
- Specification/Standard: Reference 중심. 정확한 계약/옵션/제약/규칙을 빠르게 조회.
- Architecture Design: Explanation + Reference. Owner/Boundary/Topology/의존성의 이유와 제약을 이해.
- Deliverable Index: Navigation Reference. 공식 산출물의 범위/위치/용도를 찾음.

## Reader Task 완결성

각 Profile은 적용 가능한 범위에서 다음 Dimension을 검수한다.

1. 목적/언제 사용하는가
2. 선택 기준
3. 입력/옵션/기본값/필수값
4. 정상 흐름
5. 오류/경계/UNKNOWN
6. Retry/Idempotency/복구/Reconcile/Compensation
7. 보안/권한/감사
8. 결과 확인/검증
9. 관련 Source/API/Sample/상세 문서

Keyword가 한 번 등장하는 것만으로는 Coverage가 아니다. 독자가 실제로 판단·실행·복구·검증할 수 있어야 한다.

## 긴 문서 처리

문서가 길어질 수 있다. 총 페이지/용량 상한은 없다. 대신 긴 문서에는 실제 TOC, 의미 있는 Heading, 역할/업무 기반 Navigation을 제공한다. 내용이 많다는 이유로 정보를 삭제하지 않는다.

## 섹션 설계 질문

각 H1/H2를 작성하기 전에 다음 네 문장을 답한다.

- 누가 이 부분을 보는가?
- 지금 무엇을 하려는가?
- 여기서 무엇을 결정/실행하는가?
- 읽은 뒤 어떤 결과를 확인할 수 있어야 하는가?

답할 수 없으면 Section 자체를 재설계한다.
## Selection-to-Action

Reader Task는 선택 지점에서 끝나지 않는다. 선택 후 실제 설정·Consumer/작업·정상 결과·실패/복구·검증으로 이어져야 한다. API/키워드 존재는 pre-check일 뿐 Reader Task PASS Evidence가 아니다.

