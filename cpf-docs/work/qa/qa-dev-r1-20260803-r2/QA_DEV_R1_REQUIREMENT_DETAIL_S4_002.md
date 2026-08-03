# QA Requirement Detail — CPF-SELF-DEV-S4-002

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: 분할 논리 원장 기반 Traceability와 기존 함수 계약 보존
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-tools/scripts/verify-cpf-requirement-traceability.py`
2. 이전 SHA `d2adc89f...`의 동일 Script
3. `cpf-tools/scripts/tests/test_verify_cpf_requirement_traceability.py`
4. `cpf-docs/quality/CPF_20260801_INTEGRATED_RESULT_MATRIX.csv`
5. `cpf-docs/work/evidence/20260803/session4/P01_REQUIREMENT_TRACEABILITY_R2.json`
6. `cpf-docs/work/development/p00-p05-session4/EXECUTION_LEDGER.csv`
7. `cpf-docs/work/development/p00-p05-session4/REQUIREMENT_STATUS.csv`

## 확인 결과

- `read_csv`, `require_columns`, `unique_ids`, `check_statuses`,
  `check_result_matrix` 공개 함수는 남아 있다.
- 엄격한 `check_result_matrix` API는 Coverage·경로·SHA를 검사한다.
- 그러나 Main 실행은 엄격 API를 사용하지 않고 `check_optional_result_matrix`를 사용한다.
- 기본 Result Matrix가 없으면 rows/completed/verified 모두 0으로 반환하고 PASS한다.
- Matrix가 존재해도 Canonical 전체 또는 Scope 전체 Coverage를 강제하지 않는다.
- 제출 Evidence는:
  - `head=UNAVAILABLE`
  - `result_matrix.rows=0`
  - `completed=0`
  - `verified=0`
  - `status=PASS`

## 미통과 근거

1. 실제 Git HEAD를 확인하지 않은 Overlay 실행을 PASS로 기록했다.
2. 활성 단일 Requirement 원장이 없거나 비어도 PASS한다.
3. Scope 1~10,027의 Requirement별 Source/Consumer/Test/Evidence Coverage를 강제하지 않는다.
4. 이전 strict Main의 SELF 30 + Active Gap 85 Coverage 검증을 제거했지만 동등한 Canonical Coverage를 추가하지 않았다.
5. Test는 Optional Matrix 누락/0행/부분행 PASS를 막지 않는다.
6. Evidence에 `verifiedAgainstSha=d2adc89f...`가 별도 필드로 들어갔지만 실제 Gate 결과는 `head=UNAVAILABLE`이다.
7. 실행 원장에 실제 명령 대신 placeholder가 남아 있다.
8. 70건 회귀 로그가 Git 정본에서 조회되지 않아 공개 함수 호환성 PASS를 재현할 수 없다.

## 재개발 요청

- 활성 단일 Requirement 원장을 필수 입력으로 지정
- Scope 1~10,027 Coverage 강제
- completed 행 Source/Consumer/Test 실제 경로 확인
- verified 행 Evidence/exact SHA 확인
- `--expected-sha`와 `--require-clean` 필수화
- Optional sparse 보조 원장은 별도 모드·별도 이름으로 분리
- 누락·0행·부분행·Unknown ID·Stale SHA Negative Test 추가

## 성공 기대 결과

- 최신 후보 HEAD와 Evidence SHA 일치
- Scope Requirement 10,027건 모두 원장에 존재
- 각 행의 Source/Consumer/Test/Evidence 상태가 검증됨
- 원장 누락·0행은 fail-closed
