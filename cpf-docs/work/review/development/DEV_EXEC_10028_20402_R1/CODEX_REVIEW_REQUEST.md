# CODEX REVIEW REQUEST

## Review Candidate

- Latest master SHA: `c4a1a725f2973a9f5c8864ac53729357fb04cf75`
- Current Batch: `DEV_EXEC_10028_20402_R1`
- Development range: logical `10,028~20,402`
- QA merged range: `1~10,027`
- QA Finding: `25건`

## 다음 개발·검수 범위

- 논리 실행순서 `20,001~40,000`
- QA Finding 25건 최신 master 재검수
- 요건 미충족 또는 결함 확인 항목 수정 개발
- 수정 결과 독립 검수와 회귀검증

## 독립 검수 기준

1. Requirement와 실제 Source·Consumer·호출 경로 일치
2. 정상·오류·경계·부분 실패·UNKNOWN 처리
3. 보안·권한·감사·마스킹
4. DB Vendor·Migration·Rollback
5. Generator·Generated Domain·Sample·EDU
6. ADM/BZA·Frontend·OpenAPI
7. exact SHA와 Evidence 정합성
8. 개발GPT가 QA·Codex 컬럼을 변경하지 않았는지
9. QA Finding을 삭제·약화하지 않았는지

QA 통과 전 완료로 판정하지 않는다.
