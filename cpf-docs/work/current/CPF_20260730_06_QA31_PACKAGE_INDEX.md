# CPF QA31 개발·검수 요청 패키지

- 작성 시각: `2026-07-30T19:20:00+09:00`
- 검토 기준 Repository: `freeangelsun/202412_01_CPF`
- 검토 기준 Branch: `master`
- 검토 기준 SHA: `693cc77bde4c830b78ca1408dec7e34ef84cd11d`
- 적용 방식: 이 ZIP을 **CPF 프로젝트 Root에서 압축 해제**
- Root 직접 생성 파일: **없음**
- 허용 최상위 경로: `cpf-docs/**`, `cpf-tools/**`

## 패키지 목적

이 패키지는 QA30 개발 완료 주장에 대한 독립 검토 결과와 QA31 개발·자체검토·후속 QA·Codex 묶음 검수 체계를 Repository에 남긴다.

이 문서를 남기는 이유는 다음과 같다.

1. 대화 세션이 바뀌어도 동일 기준으로 검토하기 위함
2. Interface·DTO·Table·화면 존재만으로 완료 처리하는 False Completion을 막기 위함
3. 개발 AI가 요청 결함을 임의로 삭제·완화·완료 표시하지 못하게 하기 위함
4. 사용자가 Push한 exact SHA를 다음 QA 세션과 Codex가 다시 독립 검증하기 위함
5. 반복 검수 비용과 Codex Credit을 줄이면서도 Root Cause와 영향도를 놓치지 않기 위함

## 문서 사용 순서

1. `CPF_20260730_06_QA31_DEVELOPER_PROMPT.md`
2. `CPF_20260730_06_QA31_DEVELOPMENT_REMEDIATION_REQUEST.md`
3. `CPF_20260730_QA31_DEFECT_REGISTER.csv`
4. `CPF_20260730_QA31_REQUIREMENT_MATRIX.csv`
5. `CPF_20260730_QA31_SCENARIO_MATRIX.csv`
6. `CPF_AI_DEVELOPMENT_QA_CONTINUITY_STANDARD.md`
7. 개발 완료 후 `CPF_20260730_06_QA31_CODEX_BATCH_REVIEW_REQUEST.md`

## 불변 파일

다음 파일은 검수 기준 원본이다. 개발 AI와 Codex는 내용을 임의 수정하지 않는다.

- `cpf-docs/work/current/CPF_20260730_06_QA31_DEVELOPMENT_REMEDIATION_REQUEST.md`
- `cpf-docs/quality/CPF_20260730_QA31_DEFECT_REGISTER.csv`
- `cpf-docs/quality/CPF_20260730_QA31_REQUIREMENT_MATRIX.csv`
- `cpf-docs/quality/CPF_20260730_QA31_SCENARIO_MATRIX.csv`
- `cpf-docs/governance/CPF_AI_DEVELOPMENT_QA_CONTINUITY_STANDARD.md`
- `cpf-docs/quality/CPF_20260730_QA31_REQUEST_INTEGRITY.json`

개발 결과는 원본 수정이 아니라 별도 Completion Report, Evidence, Result Matrix로 기록한다.

## README·Guide 병행 작업 경계

README와 Guide는 별도 AI 모델이 프로젝트 분석 후 병행 작업 중이며 수시로 변경된다.

개발·QA·Codex는 다음을 원칙적으로 수정·재작성·대규모 검수하지 않는다.

- Repository의 모든 `README*`
- `cpf-docs/guides/**`
- `cpf-tools/README.md`
- 사용 설명 위주의 문서

기능 개발 때문에 반드시 필요한 최소 참조 수정만 허용하며, 그 경우 변경 이유와 영향 범위를 Completion Report에 별도 기록한다. README·Guide의 내용 차이만으로 Product Source 결함을 만들지 않는다. 반대로 README·Guide 병행 변경을 Product Source 완료 Evidence로 사용하지 않는다.
