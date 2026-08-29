# CPF 분할 Master 원장 사용 지침

## 1. 활성 정본 경로

다음 메인 파일은 삭제하지 않고 분할 인덱스로 유지한다.

- `cpf-docs/governance/development-harness/current/CPF_REQUIREMENT_MASTER.csv`
- `cpf-docs/governance/development-harness/current/CPF_SCENARIO_MASTER.csv`
- `cpf-docs/governance/development-harness/current/CPF_EXECUTION_SEQUENCE.csv`

각 메인 CSV는 Part 위치·순서·건수·ID 범위·SHA-256을 설명하는 **논리 Master 인덱스**다.

## 2. 실제 데이터 위치

- Requirement: `cpf-docs/governance/development-harness/current/CPF_REQUIREMENT_MASTER.parts/*.csv`
- Scenario: `cpf-docs/governance/development-harness/current/CPF_SCENARIO_MASTER.parts/*.csv`
- Execution Sequence: `cpf-docs/governance/development-harness/current/CPF_EXECUTION_SEQUENCE.parts/*.csv`

각 Part는 원본과 동일한 Header를 가지며 **8,000,000 bytes 미만**이다.

## 3. 읽기 규칙

1. 메인 인덱스를 먼저 연다.
2. `part_sequence` 오름차순으로 모든 `part_path`를 읽는다.
3. 모든 Part Header와 `logical_header_sha256`을 확인한다.
4. 첫 Part의 Header만 유지하고 이후 Part Header는 제외한다.
5. 각 Part의 건수·ID 범위·SHA-256을 확인한다.
6. 논리 합계가 다음과 일치해야 한다.
   - Requirement: `30,605`건
   - Scenario: `40,806`건
   - Execution Sequence: `30,605`건
7. 메인 인덱스 행은 Requirement·Scenario·실행순서 데이터 행으로 계산하지 않는다.

## 4. 작업 범위 해석

논리적으로 결합한 `CPF_EXECUTION_SEQUENCE.csv`의 `execution_order`가 작업 순서 정본이다.
Part 번호·파일 경계는 Phase나 Work Package 경계가 아니다.

## 5. 갱신 규칙

데이터 변경 시 해당 Part와 메인 인덱스의 건수·ID 범위·크기·SHA-256을 함께 갱신한다.
`CPF_REQUIREMENT_VALIDATION_RESULT.json`, `CPF_FILES.sha256`, `CPF_PACKAGE_MANIFEST.json`도 다시 생성한다.

## 6. 금지 사항

- 메인 인덱스를 빈 원장으로 오인하지 않는다.
- 일부 Part만 읽고 전체 완료 처리하지 않는다.
- 단일 대용량 통합 CSV를 Git에 다시 추가하지 않는다.

현재 분할 파일은 논리 Master의 물리 저장 형식이며, 날짜·패키지명은 정본 식별자로 사용하지 않는다.
