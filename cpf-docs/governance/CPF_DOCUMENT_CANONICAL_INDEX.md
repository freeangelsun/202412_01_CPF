# CPF 문서 정본 Index

- 중앙 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- 적용 후 운영 기준 SHA: 이 Overlay가 반영된 successor `master` exact SHA
- 목적: 모든 개발GPT·QA·Codex·중앙 관리자가 동일한 프로젝트 정본과 현재 작업 진입점을 사용하게 한다.

## 1. 정본 우선순위

1. `CPF_FINAL_TARGET_REQUIREMENTS.md` — 최상위 제품 목표·Canonical Requirement **169**
2. `CPF_REQUIREMENT_CONTINUITY_LEDGER.md` — ID·Alias·Count 연속성
3. `CPF_REPOSITORY_SURFACE_INDEX.md` — Root·Module Ownership
4. `CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`
5. `CPF_GENERATED_DOMAIN_LIFECYCLE_POLICY.md`
6. `CPF_VERIFICATION_GATE_LIFECYCLE_POLICY.md`
7. `CPF_DOCUMENT_CONTROL_POLICY.md`
8. `../work/v9i/final-control/REVIEW_INDEX.md` — 현재 Final Cycle 중앙 통합 진입점
9. `../work/v9i/final-dev-request/CPF_DEVGPT_FINAL_SOURCE_COMPLETION.md` — 현재 Product Source 최종 개발지침
10. `../work/v9i/qa/final-a/**`, `../work/v9i/qa/final-b/**` — 현재 독립 QA 원본 Evidence
11. 실제 Source·SQL·API·Test·Config·Frontend·Generator·Script와 current exact-SHA Evidence

하위 문서가 상위 정본과 충돌하면 상위 정본을 따른다.
QA Requirement가 구체적 Acceptance를 추가한 경우 최상위 목표를 약화하지 않는 범위에서 QA Requirement를 적용한다.

## 2. 역할별 정본 Owner

| 역할 | Owner | 정본 |
|---|---|---|
| 프로젝트 목표·Canonical Requirement | 중앙 관리자 | `CPF_FINAL_TARGET_REQUIREMENTS.md` |
| Requirement ID/Count/Alias | 중앙 관리자 | `CPF_REQUIREMENT_CONTINUITY_LEDGER.md` |
| Repository/Module Ownership | 중앙 관리자 | `CPF_REPOSITORY_SURFACE_INDEX.md` |
| 프로젝트 문서·Current/History 통제 | 중앙 관리자 | `CPF_DOCUMENT_CONTROL_POLICY.md` |
| 현재 Final 중앙 Merge | 중앙 관리자 | `../work/v9i/final-control/**` |
| Product Source 구현 결과 | 개발GPT | `../work/v9i/dev-final/**` 또는 중앙이 지정한 개발 결과 경로 |
| QA A 원본 | QA A | `../work/v9i/qa/final-a/**` |
| QA B 원본 | QA B | `../work/v9i/qa/final-b/**` |
| README·Guide·고객 PDF/DOCX 산출물 | 별도 Documentation Finalization | 해당 고객 문서 경로 |

개발GPT·QA는 중앙 프로젝트 정본을 자기 판단으로 임의 수정하지 않는다.
정본 모호성·충돌은 중앙 관리자에게 Alignment Finding으로 올리고, 중앙 관리자가 전체 영향도를 대조해 현행화한다.

## 3. 현재 Final Cycle 진입점

- 중앙 상태/판정: `cpf-docs/work/v9i/final-control/REVIEW_INDEX.md`
- 중앙 Action: `cpf-docs/work/v9i/final-control/CENTRAL_FINAL_ACTIONS.csv`
- 개발 실행지침: `cpf-docs/work/v9i/final-dev-request/CPF_DEVGPT_FINAL_SOURCE_COMPLETION.md`
- QA A: `cpf-docs/work/v9i/qa/final-a/`
- QA B: `cpf-docs/work/v9i/qa/final-b/`

과거 `CPF_CURRENT_WORK_REQUEST.md`, QA38/QA39, V7/V9 Control, 날짜별 Session/Review 문서는 현재 진입점이 아니다.
결론이 현재 정본에 흡수된 과거 파일은 Git History로 보존하고 Working Tree에서는 삭제한다.

## 4. 고객 문서 분리

다음은 중앙 Project Control 현행화 대상에서 제외하고 별도 Documentation Finalization이 관리한다.

- `README.md`
- `cpf-docs/guides/**`
- `cpf-docs/deliverables/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

Project Source 변경으로 고객 문서 영향이 생기면 개발GPT는 Impact만 보고하고 고객 문서를 직접 수정하지 않는다.

## 5. 금지

- QA 번호·날짜·세션 번호를 붙여 같은 역할의 Current 문서를 계속 복제
- 과거 QA/개발 결과를 현재 exact SHA PASS로 승계
- Canonical 169와 대량 분해 Work Item/Scenario count를 같은 완료율 분모로 혼용
- 중앙 정본보다 과거 `current/`, `review/`, `handover/`, `codex/qaXX/` 문서를 우선 참조
