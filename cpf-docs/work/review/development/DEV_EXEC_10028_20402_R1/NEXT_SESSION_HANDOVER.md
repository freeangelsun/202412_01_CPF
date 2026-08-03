# NEXT SESSION HANDOVER

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 최신 Push SHA: `c4a1a725f2973a9f5c8864ac53729357fb04cf75`
- 현재 개발 Batch: `DEV_EXEC_10028_20402_R1`
- 현재 개발 범위: 논리 `10,028~20,402`
- 현재 상태: 개발GPT `미완료`, QA `검수 전`

## 머지된 기존 QA 결과

- QA 검수 범위: `1~10,027`
- QA 결과: `미통과`
- QA Finding: `25건`
- CRITICAL `8`, HIGH `15`, MEDIUM `2`
- QA 원본: `cpf-docs/work/qa/qa-dev-r1-20260803-r2/`

## 다음 개발GPT 작업 지시

1. 논리 실행순서 `20,001~40,000`을 정본 Requirement·Scenario·Work Package에 따라 검수하고 개발한다.
2. Requirement별 실제 Source·SQL·API·Test·Config·Frontend·Script·Migration을 확인한다.
3. 실제 Consumer, 전체 호출 경로, Owner Module·Package, Public API·SPI·Internal 경계를 확인한다.
4. 정상·오류·경계·부분 실패·UNKNOWN·재시도·복구·Reconcile을 검증한다.
5. QA Finding 25건을 최신 master에서 재검수한다.
6. 요건 미충족 또는 결함이 확인된 항목은 수정 개발한다.
7. 수정 후 회귀 Test와 실행 Evidence를 제출한다.
8. 환경이 필요한 검증은 명령·기대 결과·실패 기준·필요 Evidence와 함께 이관한다.
9. 개발GPT는 자기 수행·자체검수 컬럼만 갱신한다.
10. QA·Codex 컬럼과 QA 원본 판정은 변경하지 않는다.
11. QA 통과 전 전체 완료 처리하지 않는다.

## 병행 QA

QA는 최신 Push SHA `c4a1a725f2973a9f5c8864ac53729357fb04cf75`를 기준으로 논리 실행순서 `10,028~20,402` 개발 결과를 별도로 검수한다.

## 금지

- 검수 전에 정상 또는 결함으로 단정
- 요건과 무관한 임의 변경
- QA Finding 삭제·약화
- 미실행 검증 PASS 처리
- 사용자 승인 없는 삭제·Commit·Push
