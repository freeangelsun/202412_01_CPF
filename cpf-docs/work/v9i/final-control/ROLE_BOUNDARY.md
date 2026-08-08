# CPF Final Role Boundary

- 중앙 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)

## 중앙 관리자

README·Guide·고객 PDF/DOCX 산출물을 제외한 프로젝트 관련 정본을 관리한다.

- 프로젝트 최상위 목표
- Canonical Requirement 169 / Alias / Count
- Governance
- Architecture/Specification 제품 계약
- Module Ownership
- Current Control / Handover / Continuity
- QA A/B Merge 및 충돌 판정
- 개발/QA 역할 경계
- Project Control Garbage/Delete Manifest
- 정본과 Source 사이의 해석 충돌 최종 판정

문서가 서로 다르면 중앙 관리자가 전체 영향을 대조해 즉시 현행화한다.

## Product Developer GPT

수정 가능:
- Product Source/SQL/API/SPI/Test/Config/Frontend/OpenAPI/Generated Client/Generator/Runtime Gate
- 중앙이 지정한 개발 결과 경로(권장 `cpf-docs/work/v9i/dev-final/**`)
- `PROJECT_DOCUMENT_ALIGNMENT_REQUEST.csv`
- `DOCUMENT_IMPACT.csv`

수정 금지:
- `cpf-docs/governance/**`
- `cpf-docs/work/v9i/final-control/**`
- `cpf-docs/work/v9i/qa/final-a/**`
- `cpf-docs/work/v9i/qa/final-b/**`
- 중앙 Architecture/Specification 정본
- 고객 문서 별도 Owner 경로

정본 모호성·충돌을 발견하면 임의 수정/완화하지 않고 Alignment Request로 중앙에 보고한다.

## QA A / QA B

둘 다 동일 CPF 전체 범위를 독립 전수검수한다.
개발GPT 또는 상대 QA의 PASS를 승계하지 않는다.
중앙 정본 자체의 모순은 Finding으로 보고하되 중앙 정본을 임의 변경하지 않는다.

## Documentation Finalization

별도 작업:
- `README.md`
- `cpf-docs/guides/**`
- `cpf-docs/deliverables/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

Product Source를 수정하지 않는다.

## Git/삭제

Commit/Push는 사용자만 수행한다.
삭제는 중앙 exact-path Manifest와 사용자 실행 명령으로만 수행한다.
