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

- `../work/repository-consolidation/20260802/STARTER_PROFILE_AND_BUNDLE_DESIGN.md` — 다음 QA의 Profile·Bundle 설계 입력
- `../work/repository-consolidation/20260802/DOCUMENT_UPDATE_STATUS_AND_REMAINING_APPLY.md` — 정본·Guide 실제 반영 상태
