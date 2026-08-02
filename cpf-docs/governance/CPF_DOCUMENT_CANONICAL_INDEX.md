# CPF 문서 정본 Index

## 읽기 순서

1. `CPF_FINAL_TARGET_REQUIREMENTS.md` — 최상위 제품 목표
2. `CPF_REPOSITORY_SURFACE_INDEX.md` — Root와 Module Ownership
3. `CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md` — Lightweight Core·Starter·Domain 선택 정책
4. `CPF_GENERATED_DOMAIN_LIFECYCLE_POLICY.md` — Generated Domain 생성·삭제·재생성
5. `CPF_VERIFICATION_GATE_LIFECYCLE_POLICY.md` — Gate 생성·이관·폐기
6. `CPF_DOCUMENT_CONTROL_POLICY.md` — 문서 역할·Current·History 통제
7. 현재 작업 Request·Requirement Matrix·Pre/Post Review
8. 실제 Source·SQL·API·Test·Config·Frontend·Script와 exact-SHA Evidence

## 역할별 정본

| 역할 | 정본 |
|---|---|
| 최종 제품 목표 | `CPF_FINAL_TARGET_REQUIREMENTS.md` |
| Root·Module Ownership | `CPF_REPOSITORY_SURFACE_INDEX.md` |
| Core 경량화·Starter Architecture | `CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md` |
| Generated Domain Lifecycle | `CPF_GENERATED_DOMAIN_LIFECYCLE_POLICY.md` |
| Gate Lifecycle | `CPF_VERIFICATION_GATE_LIFECYCLE_POLICY.md` |
| 문서 Lifecycle | `CPF_DOCUMENT_CONTROL_POLICY.md` |
| 역사·Decision | `history/*.csv` |

QA 번호·날짜가 붙은 작업 문서는 제품 정본이 아니다. 활성 Request가 끝나면 결론을 위 정본과 History Ledger에 반영하고 중복 Prompt·Checkpoint·Report는 삭제 후보로 전환한다.

## 현재 작업 보조 문서

- `../work/current/CPF_CURRENT_WORK_REQUEST.md` — 유일한 현재 진입점
- `../work/current/CPF_20260802_05_POST_QA37_INTEGRATED_DEVELOPMENT_REQUEST.md` — 통합 개발 요청
- `../work/codex/qa38/CODEX_START_HERE.md` — 다음 Codex 검수·보완 시작점
- `../work/review/20260802_05/` — 이번 작업의 사전·사후 리뷰와 Source 기반 판정

`../work/repository-consolidation/20260802/`는 이번 정본 갱신의 입력 History다. 활성 작업이 끝나면 결론을 Governance/State에 흡수하고 날짜별 중복 문서는 Delete Review 대상으로 전환한다.

<!-- CPF_QA38_CURRENTIZATION_START -->
## QA38 현행 개발·검수 정본
- Current: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- Detailed Request: `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- Requirement Matrix: `cpf-docs/quality/CPF_QA38_FINAL_REQUIREMENT_MATRIX.csv`
- Scenario Matrix: `cpf-docs/quality/CPF_QA38_FINAL_SCENARIO_MATRIX.csv`
- Starter Review: `cpf-docs/work/review/CPF_QA38_STARTER_INDEPENDENT_REVIEW.md`
- Codex Entry: `cpf-docs/work/codex/qa38/CODEX_START_HERE.md`
- History: `cpf-docs/work/history/CPF_QA37_TO_QA38_CONSOLIDATED_HISTORY.md`
과거 날짜별 문서는 활성 정본이 아니다.
<!-- CPF_QA38_CURRENTIZATION_END -->
