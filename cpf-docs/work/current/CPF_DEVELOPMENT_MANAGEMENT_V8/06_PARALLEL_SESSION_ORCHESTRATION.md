# 병렬 개발 세션 운영

개발 세션 수는 고정하지 않는다. `generate_development_requests.py`가 Active Scope를 Dependency 순서로 정렬한 뒤 Owner Module 기준 Connected Slice로 동적 분할한다.

## 충돌 방지

- Governance·Requirement/Scenario 원장·V8 중앙 State는 Session 직접 수정 금지다.
- Public API/SPI, Root Build, DB Canonical, Generator, OpenAPI Source는 Integration Owner가 단독 반영한다.
- Generated Output은 직접 편집하지 않는다.
- 일반 Module Source는 배정된 Owner Session만 변경한다.
- 다른 Session 경계가 필요하면 `CROSS_SESSION_CHANGE_REQUEST_TEMPLATE.csv`를 제출한다.
- 세션은 `DEVELOPMENT_SESSION_RESULT_TEMPLATE.csv`와 Requirement/Scenario별 결과를 제출하고 중앙 Merge Script가 상태를 갱신한다.

## 순서

1. P0 Baseline Stabilization
2. Public Contract·Ownership·DB Canonical·Generator Source
3. 실제 Provider·Consumer·Runtime·Frontend
4. 오류·부분 실패·UNKNOWN·복구·Reconcile
5. Security·Audit·Masking·Approval
6. Runtime/DB Vendor/Multi-instance/Fresh clone 검증
7. Session 결과 병합 후 QA Handoff
