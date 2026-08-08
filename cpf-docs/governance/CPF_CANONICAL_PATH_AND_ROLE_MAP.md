# CPF Canonical Path and Role Map

- 중앙 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- 목적: 어떤 세션·PC·AI에서도 같은 정본을 찾고 과거 QA/개발 경로를 Current로 복원하지 않게 한다.

## 1. Canonical 위치

| 역할 | 정본 위치 | Owner |
|---|---|---|
| 최상위 제품 목표·Canonical 169 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` | 중앙 관리자 |
| Requirement 연속성·Alias | `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md` | 중앙 관리자 |
| Repository/Module Ownership | `cpf-docs/governance/CPF_REPOSITORY_SURFACE_INDEX.md` | 중앙 관리자 |
| 문서 정본 Index | `cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md` | 중앙 관리자 |
| 문서 통제 정책 | `cpf-docs/governance/CPF_DOCUMENT_CONTROL_POLICY.md` | 중앙 관리자 |
| 현재 Final Cycle 중앙 진입점 | `cpf-docs/work/v9i/final-control/REVIEW_INDEX.md` | 중앙 관리자 |
| 현재 중앙 Finding/Action | `cpf-docs/work/v9i/final-control/CENTRAL_FINAL_ACTIONS.csv` | 중앙 관리자 |
| 현재 Product 개발지침 | `cpf-docs/work/v9i/final-dev-request/CPF_DEVGPT_FINAL_SOURCE_COMPLETION.md` | 중앙 관리자 |
| Final QA A | `cpf-docs/work/v9i/qa/final-a/**` | QA A |
| Final QA B | `cpf-docs/work/v9i/qa/final-b/**` | QA B |
| Product 개발 결과 | `cpf-docs/work/v9i/dev-final/**` 또는 중앙 지정 stable result path | 개발GPT |
| Architecture/Specification | `cpf-docs/architecture/**` 및 중앙 지정 Specification 계약 | 중앙 관리자 |
| Evidence Index | `cpf-docs/evidence/**` 및 current exact-SHA Evidence | 역할별 Owner |
| Product Tool/Script | `cpf-tools/**` | Product Owner |
| DB Source SSOT | `cpf-tools/db/vendor/<vendor>/source/**` | DB/Tools |
| README·Guide·PDF/DOCX | 해당 고객 문서 경로 | Documentation Finalization |

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 WIP 임시 문서가 아니라 **최상위 영구 제품 정본**이다.
다른 영구 Specification에 내용을 반영하더라도 사용자의 명시적 정본 변경 결정 없이 제거하지 않는다.

## 2. 현재 사용하지 않는 과거 진입점

다음 유형은 현재 정본이 아니다.

- `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- QA38/QA39 전용 Request/Report/Backlog
- `cpf-docs/work/current/CPF_DEVELOPMENT_WORKLIST_V7_1/**`
- `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/**`
- `cpf-docs/work/codex/qa38/**`
- 날짜별 과거 Session Handover/Review

유효 결론은 Canonical/Final Control에 흡수하고, 과거 파일은 Git History로 보존한 뒤 exact Manifest로 제거한다.

## 3. 역할 경계

### 중앙 관리자
프로젝트 목표, Requirement, Architecture 계약, Module Ownership, 문서 정본, QA A/B Merge, Current Control, 역할 경계와 충돌 판정을 관리한다.

### Product Developer GPT
정본에 따라 Source/SQL/API/SPI/Test/Config/Frontend/Generator/Runtime Gate를 구현하고 자기 결과/Evidence만 기록한다.
정본 모호성은 Alignment Request로 보고하고 중앙 정본을 임의 수정하지 않는다.

### QA A / QA B
동일 전체 범위를 독립 전수검수한다.
상대 QA나 개발GPT의 PASS를 승계하지 않고, 중앙 정본의 모순도 Finding으로 올린다.

### Documentation Finalization
`README.md`, 공식 Guide, 고객 PDF/DOCX 산출물의 내용·한글화·시각 편집을 담당한다.
Product Source를 수정하지 않는다.

## 4. Root/Module Ownership

- `cpf-core`: topology-independent 기술 핵심 계약
- `cpf-common`: 고객 업무 공통
- `cpf-admin`: 플랫폼 운영·관리 Product
- `cpf-biz-admin`: 고객 업무 관리자 Product
- `cpf-batch`: Batch·Worker·Scheduler·Center-Cut Runtime
- `cpf-gateway`: Gateway Runtime
- `cpf-reference`: Adopter Public API/SPI/Extension/EDU Example
- `cpf-member`: Generator Golden Generated Domain
- `cpf-starters`: 선택 Provider/AutoConfiguration/Library Artifact
- `cpf-tools`: Build/Generator/DB/Verification/Supply-chain

역방향·순환 의존, 외부 Module의 Internal Package 참조, Public BOM의 Internal Leaf 노출을 금지한다.

## 5. 시작 규칙

새 Developer/QA/Codex/중앙 세션은 과거 Handover를 먼저 찾지 않는다.
항상 `CPF_DOCUMENT_CANONICAL_INDEX.md` → Final Target → `work/v9i/final-control/REVIEW_INDEX.md` 순서로 시작하고 최신 `origin/master` exact SHA를 확인한다.
