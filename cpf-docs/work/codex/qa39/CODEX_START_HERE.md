# CPF QA39 Codex Start Here — Final R3

## Baseline

`9a9634eb1f28071d47c205cc35227b6d013a4536`

## Fast review order

1. `CPF_QA39_FINAL_DEVELOPMENT_REQUIREMENTS.md`의 우선순위와 최종 유지/삭제 결정
2. `CPF_QA39_DEVELOPER_IMPLEMENTATION_REPORT.md`와 `CPF_QA39_DEVELOPER_SELF_REVIEW.md` 존재·완결성
3. `CPF_QA39_DELETE_WORK_ITEMS.csv`와 실제 삭제/잔여참조 결과
4. `CPF_QA39_STARTER_VALUE_CATALOG.csv`와 6 Profile+7 Group Public Catalog
5. `CHANGE_MANIFEST.csv`에서 변경 파일·Requirement·영향도
6. `REQUIREMENT_STATUS.csv`와 actual Evidence
7. `TEST_AND_EVIDENCE.md`의 명령·환경·시간·Exit·미실행
8. 핵심 Source/Consumer/Dependency/Runtime 독립 표본검증
9. `OPEN_ISSUES.md`와 Package Hash

Developer Report는 QA 승인 근거 자체가 아니라 반복 탐색을 줄이는 검수 인덱스다. 보고와 Source/Evidence가 다르면 Source/Evidence를 기준으로 실패 처리한다.

## 정본 및 정리 확인

1. Repository Root에 QA 산출물이 추가되지 않았는지 확인한다.
2. Current Request·자체요건·Handover·Continuity의 중복 정본이 남지 않았는지 확인한다.
3. QA 문서 정리와 제품 Source 삭제가 서로 다른 Manifest로 관리되는지 확인한다.
