# CPF 현재 자체 개발 요청

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Baseline SHA: `95e592c05fc457301efdb13ee50e0d7453325806`
- 최상위 목표: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

## 실행 정본

1. `cpf-docs/work/review/CPF_SELF_DEVELOPMENT_SOURCE_REVIEW.md`
2. `cpf-docs/work/current/CPF_SELF_DEVELOPMENT_REQUIREMENTS.md`
3. `cpf-docs/quality/CPF_SELF_DEVELOPMENT_REQUIREMENT_MATRIX.csv`
4. `cpf-docs/work/current/CPF_SELF_DEVELOPMENT_EXECUTION_PROMPT.md`

## 작업 범위

이 작업은 개발 주체의 자체 검토 결과만 구현한다.

외부 검수 Requirement·Defect 문서를 생성·수정·재분류하지 않는다.
별도 검수 목록이 제공되기 전에는 추정 Requirement를 만들지 않는다.

자체 개발 Requirement 30건을 선행 의존성 순서로 실제 Source에 구현한다.
각 항목은 Source·API·Consumer·Permission·Audit·정상/오류/복구 Test·Evidence를 하나의 완료 단위로 닫는다.
