# CPF Documentation Harness Change Log

## v2.1.0 — Rejected Baseline Initial Rebuild Lifecycle

- 사용자가 기존 공식 산출물 전체를 품질 기준 미달로 판정한 경우 `BASELINE_REJECTED`로 분리한다.
- `BASELINE_REJECTED` + 사용자 명시 요청일 때만 최초 1회 `INITIAL_FRESH_REBUILD`를 허용한다.
- 최초 재구축은 `GOLDEN_BASELINE_CANDIDATE`로 검수하며, `USER_APPROVED` 또는 `VISUAL_QA_APPROVED` 이후 `PATCH_ONLY`로 전환한다.
- 승인 이후에는 `PATCH_FIRST / PRESERVE_APPROVED / NO_FRESH_REWRITE_BY_DEFAULT`를 강제한다.
- H3 번호 표현은 `1)` 형식으로 정정한다.
- Source baseline은 최종 산출물 재구축 기준 master `054d894b47f4be8323439dc6f9e58b7d8b60fe54`로 현행화한다.
- 기존 v2 실행형 Design System, Render Acceptance, Golden Reference, Regression Gate, Content Rail, Visual Grammar, PDF/DOCX Link Gate를 유지한다.
