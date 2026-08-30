# CPF 상세 개발·검수 리뷰 출력 표준

리뷰는 "완료 몇 건" 요약으로 끝내지 않는다. 리뷰 대상 집합과 현재 상태의 **유일한 출발점은 `current/CURRENT_WORK_ITEM_REGISTRY.csv`**다. 각 Work Item에서 필요할 때 `ROLE_EXECUTION_LEDGER.csv`, `TEST_EXECUTION_LEDGER.csv`, Canonical Requirement Registry, session Evidence로 Drill-down하여 **세부항목별** 아래 필드를 출력한다. 파생 Status/Ledger에서 별도 작업목록을 재구성하거나 Registry에 없는 항목을 완료 대상으로 추가하지 않는다.

1. Work Item ID / Requirement ID / Priority / Work Package
2. 원 Requirement와 Acceptance Criteria
3. Root Cause / 변경 목적
4. 변경 전 영향 Source·Consumer·DB·Config·API·Frontend·Generator·Runtime
5. 실제 변경 파일과 동작 방식
6. 변경 중 추가 발견/잠복 결함과 병합 여부
7. 변경 후 영향도 재검증
8. Targeted Test / Regression / Security / DB3 / Runtime / Fresh Replay — prerequisite source와 required/actual 환경 판정 포함
9. DevGPT 수행·상태·완료/미완료 사유·Evidence
10. Independent Reviewer(Codex/Claude) 수행·상태·완료/미완료 사유·Evidence
11. QA 수행·상태·완료/미완료 사유·Evidence
12. 현재 전체 상태와 다음 조치/재실행 조건

누락된 항목은 `미확인`으로 명시하고 완료로 추정하지 않는다. `validators/generate_detailed_review.py`가 원장 전체를 기준으로 리뷰 뼈대를 생성하며 일부 행만 임의 생략하지 않는다.

## 개별 Work Item 근거 강제

리뷰 파일 하나에 전체 Work Item을 담을 수는 있지만 **각 Work Item은 독립 Section/Evidence Block**이어야 한다. `WP-001~WP-050 동일`, `나머지 PASS`, `기존 실패이므로 SKIP` 같은 일괄 표현은 허용하지 않는다.

같은 Test/Runtime 실행이 여러 Work Item을 검증했으면 로그 자체는 공유할 수 있다. 그러나 각 Work Item에서 어떤 assertion, transactionId, DB query, file log row, runtime event가 해당 Acceptance를 만족하는지 별도로 설명한다. 이 매핑이 없으면 그 Work Item의 PASS 근거가 아니다.

최종 전체 리뷰에서는 sessionKey와 Merge 상태도 함께 출력해 해당 Evidence가 Current Registry에 실제 Merge됐는지 확인한다.

