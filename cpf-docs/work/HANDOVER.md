# CPF Current Development / QA Handover

## 기준

- 입력 Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA(20260816-061343).zip`
- 입력 FullLocal ZIP: `CPF_LOCAL_VALIDATION_20260816_132902.zip`
- 결과 Content SHA-1: `6ce96c49fbfca3b26ab172187ac06fe279e09040`
- 결과 Content SHA-256: `91a58a0a50abbba56f75c5ba5f4aa5cf84965353a54cbb586bca38177ba09eea`
- 제품 Source 삭제: 0
- Git write/delete/history 작업: 없음

## 현재 판정

입력 FullLocal 135단계는 PASS 102 / FAIL 30 / SKIP_ENV 3이었다. 7개 공통 Root Cause를 Source/Verifier/Generator/Frontend/Test/Evidence에 재개발했다. 현재 Fresh Apply에서 QA-B3 완료 대상 22개는 다시 PASS했으며 008/010/011은 미완료다. 전체 완료는 아니다.

## 현재 Clean Fresh Apply 재검수

- Verification: 45/45 PASS
- Testing Tools: 80/80 test files PASS / FAIL 0
- Runtime Tools: 65 PASS / 2 SKIP / 7 subtests PASS
- Generator: 27 PASS / 10 SKIP / 6 subtests PASS
- DB Verification: 75/75 PASS
- NXT3: 22/22 individual gates PASS; Windows aggregate 재확인 필요
- Starter Catalog: 64 modules / public 24 / internal 40 PASS
- Public Function TOP100: 100 / Golden 20 PASS
- Batch Developer TOP50: 50/50 PASS
- ADM/BZA static: 321 / 96 OpenAPI operations PASS
- Source identity: SHA-1 `6ce96c49fbfca3b26ab172187ac06fe279e09040`, SHA-256 `91a58a0a50abbba56f75c5ba5f4aa5cf84965353a54cbb586bca38177ba09eea`

Java25/Docker/PowerShell7/Browser Live 항목은 성공 처리하지 않았으며 다음 FullLocal에서 닫는다. Fresh Apply / corruption negative / 보호경로 Hash 검증은 PASS했다.

## 개발자·도입사 REWORK

REWORK-01~10 Source/Contract/Guide/Gate 구현은 완료했다. REWORK-05 Generator runtime과 REWORK-06 FullLocal은 Windows Runtime 재검수가 남아 있다. Public Function TOP100(Golden20), Starter Quick Select, Batch TOP50, Fast/Targeted/FullLocal, Upgrade impact, Adoption Profile이 현재 Source에 포함된다.

## SPECIAL 20

`cpf-docs/work/current/CPF_SPECIAL_20_STATUS.csv`를 current backlog로 사용한다. SPECIAL-17은 현재 정적 검증 완료, 나머지 Runtime 연관 P0/P1은 FullLocal 결과로 닫는다.

## 다음 순서

1. Overlay 적용 후 `cpf-docs/work/current/CPF_NEXT_LOCAL_DEVELOPMENT_REQUIREMENTS.md`의 FullLocal 한 줄 실행.
2. 결과 ZIP의 PASS/FAIL/SKIP_ENV/NOT_EXECUTED 전부 집계.
3. FAIL을 Root Cause로 묶고 영향 SPECIAL/REWORK를 다시 연다.
4. Java25/Docker/Browser에서 재현 가능한 FAIL은 같은 Requirement ID로 PASS까지 재개발.
5. QA 최종 통과 전 전체 완료로 표현하지 않는다.
