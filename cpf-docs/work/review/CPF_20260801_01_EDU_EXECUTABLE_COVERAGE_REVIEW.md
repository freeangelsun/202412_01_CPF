# CPF EDU 실행 가능 Coverage 개발 리뷰

## 기준

- 최초 기준 SHA: `23babb9140b90e501d6ac715e7b77f55b66198a5`
- 최종 재기준 master SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 정본 Feature: `EDU-001`~`EDU-032`
- 정본 Requirement: QA36 Canonical 162건
- Owner: 실행 예제는 `cpf-reference`, 생성·DB·Release 검증은 `cpf-tools`, 운영 관측은 `cpf-admin`

## 실제 결함

기존 EDU Matrix는 32개 Feature와 Canonical 162개 Requirement의 행은 존재했지만 Public API/SPI, Reference Source, 정상·오류·복구 Scenario, ADM 관측 경로, 실행 명령과 exact-SHA Evidence가 `미수집`이었다. 이 상태에서는 문서 행 존재만으로 Coverage가 있는 것처럼 보일 수 있고, Internal Package를 직접 참조하는 예제나 실행되지 않은 Runtime Scenario도 차단하지 못한다.

## 구현

1. `cpf-tools/governance/cpf-edu-executable-catalog.json`
   - 32개 Feature를 연속 ID로 고정한다.
   - Owner Module, Source/Test/Public Contract glob, Scenario 축, ADM Deep Link, 고유 Runtime 명령을 선언한다.
2. `CPF_20260801_QA36_EDU_EXECUTABLE_COVERAGE_MATRIX.csv`
   - Canonical 162개 Requirement를 하나 이상의 EDU Feature에 연결한다.
   - Public Contract·Reference Source·Scenario·관측 경로·명령·Evidence 경로를 비어 있지 않게 유지한다.
3. `verify-cpf-edu-executable-coverage.py`
   - 누락·중복·Placeholder·미등록 Feature·Internal Import·Source/Test 미해결을 fail-closed로 차단한다.
   - 개발 모드와 Release 모드를 분리한다.
   - Release 모드는 162건 각각의 `완료` Runtime 상태와 exact 40자리 Source SHA, exit code 0, Sanitized Evidence를 요구한다.

## 완료와 미검증 구분

- EDU Catalog 및 Canonical 162 추적 계약 개발: `완료`
- Local Negative/Contract Test 7건: `완료`
- 전체 Repository의 실제 Source/Test glob 해석: `미검증`
- Java 25·3DB·Kafka·Agent·Gateway·Browser Runtime Scenario: `미검증`
- Final Push SHA별 162개 Evidence: `미검증`

실행하지 않은 Runtime 검증을 성공으로 승계하지 않으며, Release Gate는 Evidence가 준비되기 전까지 실패한다.
