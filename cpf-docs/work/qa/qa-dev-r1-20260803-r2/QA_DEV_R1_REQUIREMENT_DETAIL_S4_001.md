# QA Requirement Detail — CPF-SELF-DEV-S4-001

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: 분할 원장 Git 정본 byte/hash 정합성 및 fail-closed 검증
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv`
2. `cpf-docs/work/current/CPF_SCENARIO_MASTER.csv`
3. `cpf-docs/work/current/CPF_EXECUTION_SEQUENCE.csv`
4. `cpf-tools/scripts/verify-cpf-split-master-dataset.py`
5. `cpf-tools/scripts/verify-cpf-split-master-dataset.ps1`
6. `cpf-tools/scripts/tests/test_verify_cpf_split_master_dataset.py`
7. `cpf-docs/work/evidence/20260803/session4/P00_SPLIT_MASTER_VALIDATION_R2.json`
8. `cpf-docs/work/manifest/CPF_REQUIREMENT_VALIDATION_RESULT.json`
9. `cpf-docs/work/manifest/CPF_FILES.sha256`
10. `cpf-docs/work/development/p00-p05-session4/REQUIREMENT_STATUS.csv`

## 확인 결과

- Index 선언 합계:
  - Requirement: 30,558 / 19 Part
  - Scenario: 40,763 / 7 Part
  - Execution: 30,558 / 2 Part
- 선언상 Requirement/Scenario Part ID 구간은 연속한다.
- Scope 선언:
  - logical row 1~10,027
  - first execution `00-00010066`
  - last execution `05-00010919`
  - first Requirement `CPF-FR-010066`
  - last Requirement `CPF-FR-010919`
  - linked Scenario 12,277
  - Work Package 133
- 위 값은 Index/Evidence 선언 확인이며 28개 대용량 Part의 독립 byte 검증 완료를 의미하지 않는다.

## 미통과 근거

1. Python Gate가 `VERIFIED_AGAINST_SHA=a6856e...`를 상수로 사용한다.
2. PowerShell Wrapper는 exact HEAD와 Clean Working Tree를 확인하지 않는다.
3. Requirement/Scenario ID가 정규식에 맞지 않으면 continuity 검사를 실패시키지 않고 생략한다.
4. Execution `execution_order` 형식·중복·역전·연속성을 검사하지 않는다.
5. `build_scope`는 검증된 정렬이 아닌 현재 파일 행 순서의 앞 N행을 신뢰한다.
6. 상태 Schema가 현행 단일 원장 필수 컬럼과 일치하지 않는다.
7. Test는 malformed canonical ID, execution 중복·역전·누락, exact SHA, dirty tree를 검증하지 않는다.
8. 제출 Evidence는 Push 전 SHA `d2adc89f...`를 기록한다.
9. QA 통과 전 상태 원장에 development/verification `완료`를 기록했다.

## 대용량 Part 미검증 사유

현재 GitHub Connector는 약 7~8MB Part 파일의 metadata/blob SHA는 반환하지만 본문은 생략한다.
따라서 QA가 실제 bytes를 내려받아 독립 Hash·row continuity를 실행하지 못했다.
이는 개발 완료 사유가 아니며 Codex의 전체 Checkout에서 재검증해야 한다.

## 재개발 요청

- `--expected-sha` 필수화
- `git rev-parse HEAD`, Clean Tree 검증
- malformed Requirement/Scenario ID 즉시 실패
- execution_order canonical parser와 중복·누락·역전 검증
- Work Package 경계 검증
- 현행 원장 전체 상태 Schema 검증
- 위 결함별 Negative Test 추가
- 최신 후보 SHA에서 28개 Part 전체 재실행

## 성공 기대 결과

- 28개 Part의 실제 byte SHA, size, header, row count, first/last ID 일치
- Requirement/Scenario/Execution 논리 연속성 확인
- Scope 1~10,027 및 마지막 Work Package 경계 확인
- Evidence exact SHA와 HEAD 일치
- QA 전 전체 완료 상태 0건
