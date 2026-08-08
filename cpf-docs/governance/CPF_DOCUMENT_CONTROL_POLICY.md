# CPF 문서·디렉터리 통제 정책

- 중앙 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- 목적: CPF 프로젝트 정본을 하나의 일관된 구조로 유지하고, 완료된 세션·QA·Checkpoint·중복 Request가 Working Tree에 누적되어 다음 작업자를 혼란시키지 않게 한다.

## 1. 중앙 관리 Owner

README·공식 Guide·고객 PDF/DOCX 산출물을 제외한 **프로젝트 목표·Requirement·Architecture/Specification 계약·Current Control·Handover/Continuity 기준은 중앙 관리자**가 현행화한다.

개발GPT는 Product Source와 자기 개발 결과/Evidence를 수정한다.
QA A/B는 독립 검수 결과와 QA Evidence를 수정한다.
개발GPT·QA는 중앙 Project Canonical을 자기 판단으로 임의 변경하지 않는다.

## 2. 현재 활성 정본

| 역할 | 정본 |
|---|---|
| 최상위 제품 목표 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` |
| Requirement 연속성 | `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md` |
| Root·Module Ownership | `cpf-docs/governance/CPF_REPOSITORY_SURFACE_INDEX.md` |
| 문서/정본 Index | `cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md` |
| 현재 Final 중앙 진입점 | `cpf-docs/work/v9i/final-control/REVIEW_INDEX.md` |
| 현재 중앙 Action | `cpf-docs/work/v9i/final-control/CENTRAL_FINAL_ACTIONS.csv` |
| 현재 Product 개발지침 | `cpf-docs/work/v9i/final-dev-request/CPF_DEVGPT_FINAL_SOURCE_COMPLETION.md` |
| Final QA A 원본 | `cpf-docs/work/v9i/qa/final-a/**` |
| Final QA B 원본 | `cpf-docs/work/v9i/qa/final-b/**` |

`cpf-docs/work/current/**`, `cpf-docs/work/review/**`, `cpf-docs/work/handover/**`,
`cpf-docs/work/codex/qaXX/**`라는 위치명만으로 정본성이 생기지 않는다.
현재 정본 Index에 명시되지 않은 과거 작업 파일은 History/Evidence 또는 삭제 후보다.

## 3. Canonical 169와 분해 Dataset 구분

- 프로젝트 완료율 분모: Canonical Requirement **169**
- Legacy Alias: 8, 중복 집계 금지
- Work Item/Scenario/대량 분해 Dataset: 실행·추적용 파생 데이터이며 Canonical 분모가 아니다.
- 93 Requirement 원장, 56 기존 Finding, 31 중앙 신규 Action도 Project 완료율 분모를 대체하지 않는다.

## 4. 파일 생성 제한

- 같은 역할의 파일이 있으면 새 Current 문서를 만들지 않고 정본을 갱신한다.
- 날짜/세션/QA 번호/R1/R2/FINAL을 붙인 Current 복제를 금지한다.
- 완료 회차의 상세 경과는 Git History로 보존한다.
- 신규 파일은 독립 역할·Owner·Consumer·폐기 조건이 명확할 때만 만든다.
- `final-control`은 Final Cycle 동안 안정 경로로 유지하고, 후속 Cycle에서도 새 디렉터리 복제보다 기존 정본 현행화를 우선한다.

## 5. 보호 및 별도 문서 Owner 경로

중앙 Project Control 정리와 Product Developer가 직접 수정하지 않는다.

```text
README.md
cpf-docs/guides/**
cpf-docs/deliverables/**
cpf-docs/assets/manuals/**
cpf-docs/assets/readme/**
cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

README·Guide·PDF/DOCX 고객 산출물은 별도 Documentation Finalization 역할이 관리한다.
Docker 환경 보호 경로는 환경 Owner 승인 없이 삭제·이동하지 않는다.

## 6. 삭제 및 Garbage 통제

- 현재 정본에 유효 결론이 흡수된 과거 Session/QA/Checkpoint/중복 Control 파일은 Working Tree에서 제거한다.
- Git History가 과거 기록을 보존하므로 활성 Repository에 동일 내용의 과거 문서를 무기한 중복 보존하지 않는다.
- 삭제는 **Root 상대 exact file path Manifest**로만 한다.
- 파일 삭제 후 비게 되는 디렉터리는 **exact empty-directory Manifest**로만 제거한다.
- Wildcard, `git clean`, `git reset --hard`, `git restore .`, directory recursive deletion을 금지한다.
- 보호 경로가 Manifest에 들어가면 fail-closed한다.
- 삭제 전/후 Broken Link와 Current Canonical 참조를 검사한다.
- Commit/Push는 사용자만 수행한다.

## 7. Currentization 의무

중앙 관리자는 매 Final/QA Merge/Architecture 결정 시 다음을 즉시 확인한다.

1. Final Target Count/문구
2. Continuity Ledger Count/Alias
3. Canonical Path/Document Index
4. Current Control/Role Boundary
5. Developer/QA instruction
6. Stale exact SHA
7. 폐기된 QA/세션 진입점
8. 불필요한 중복 파일·빈 폴더

모순을 발견하면 다음 작업자에게 떠넘기지 않고 중앙 Project Control Overlay로 현행화한다.
