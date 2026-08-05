# 개발 완성도 추적

`WORK_ITEM_SCOPE_SUMMARY.csv`는 항목별 다음 값을 제공한다.

- Primary/Supporting Requirement 수
- Primary/Supporting Scenario 수
- 현재 Active Requirement/Scenario 수
- Manual Review 잔량
- Session 배정
- Dependency와 Open Issue

정량 진척률은 행 수만으로 완료를 판정하지 않는다. Work Package 완료 후보는 실제 Source·Consumer·오류/경계/부분 실패·보안·운영·DB/Generator·Test/Evidence가 모두 닫힌 경우에만 등록한다.

QA는 최신 통합 Git에서 매회 신규 Universe를 구성한다. 개발 중간 회차는 변경·영향·재개방 범위를 우선 검수하고, 최종/Release QA는 전체 Requirement·Scenario·Gate를 다시 실행한다.
