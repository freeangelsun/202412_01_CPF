# CPF Documentation Harness CHANGELOG

## v2.0.0 — Executable Design System / Regression-safe Incremental Improvement

- 기본 동작을 `PATCH_FIRST`로 확정하고 Fresh rewrite를 예외로 전환.
- 보존 기준선을 `USER_APPROVED` / `VISUAL_QA_APPROVED`로 제한. `AUTOMATED_PASS_ONLY`는 품질 승인 아님.
- `component-system.json` 추가: H1/H2/본문/Bullet/Figure/Caption/Table/Link/Code 등 공식 Layout Component 정의.
- `quality-acceptance.json` 추가: Harness/Artifact/Render/Human Visual/Regression/Click-through/Replay Gate 분리.
- `GOLDEN_REFERENCE_STANDARD.md`, `golden-reference.json` 추가: 승인된 화면을 실제 회귀 기준으로 관리.
- `ARTIFACT_REVIEW.template.json` 추가: 변경 범위·보존 Block·Visual Score·Regression Evidence 관리.
- Profile을 `OUTCOME_LOCKED_LAYOUT_FLEXIBLE`로 변경. Coverage는 유지하되 정확한 H1/H2 수 채우기와 관계없는 전면 재배치를 금지.
- H2/H3와 첫 내용 간격을 더 compact하게 조정하고 Content Rail 규칙 유지.
- 자동 Validator PASS와 실제 품질 PASS를 분리하고 Manual Visual Score(각 >=4, 평균 >=4.4, Critical 0)를 필수화.
- 승인 영역의 관계없는 재생성, Visual 회귀, 사유 없는 Fresh rewrite를 Hard Fail로 추가.
- Visual 생성 전에 기존 Visual Patch 가능성을 먼저 검토하는 Art Direction Brief 규칙 추가.
- 회사 Windows 정본 검증은 PowerShell-only 경로 유지.
- 현행 Harness 하나만 유지하며 과거 Harness history/stale reference를 Working Tree에 남기지 않음.
