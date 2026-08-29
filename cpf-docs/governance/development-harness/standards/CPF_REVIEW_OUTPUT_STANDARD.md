# CPF 상세 개발·검수 리뷰 출력 표준

리뷰는 "완료 몇 건" 요약으로 끝내지 않는다. Harness의 `CURRENT_DEVELOPMENT_STATUS.csv`, `ROLE_EXECUTION_LEDGER.csv`, Canonical Requirement Registry를 결합해 **세부항목별** 아래 필드를 출력한다.

1. Work Item ID / Requirement ID / Priority / Work Package
2. 원 Requirement와 Acceptance Criteria
3. Root Cause / 변경 목적
4. 변경 전 영향 Source·Consumer·DB·Config·API·Frontend·Generator·Runtime
5. 실제 변경 파일과 동작 방식
6. 변경 중 추가 발견/잠복 결함과 병합 여부
7. 변경 후 영향도 재검증
8. Targeted Test / Regression / Security / DB3 / Runtime / Fresh Replay
9. DevGPT 수행·상태·완료/미완료 사유·Evidence
10. Independent Reviewer(Codex/Claude) 수행·상태·완료/미완료 사유·Evidence
11. QA 수행·상태·완료/미완료 사유·Evidence
12. 현재 전체 상태와 다음 조치/재실행 조건

누락된 항목은 `미확인`으로 명시하고 완료로 추정하지 않는다. `tools/generate_detailed_review.py`가 원장 전체를 기준으로 리뷰 뼈대를 생성하며 일부 행만 임의 생략하지 않는다.
