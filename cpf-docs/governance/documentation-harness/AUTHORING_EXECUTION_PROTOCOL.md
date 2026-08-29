# Authoring Execution Protocol — v2.13.0

누가 작성해도 Harness를 같은 순서로 적용하도록 실행 절차를 고정한다.

1. **Source identity** — 최신 Source ZIP/SHA와 canonical requirement 확인. 과거 Evidence 승계 금지.
2. **Profile load** — 대상 Artifact Profile, Persona, Entry/Exit Condition, documentationIntent 확인.
3. **Coverage map** — Reader Task Dimension과 Source Capability를 매핑. 길이 때문에 누락 금지.
4. **Information architecture** — H1/H2 흐름, TOC/Navigation, Figure/Table 후보를 먼저 설계.
5. **Format selection** — 문장/List/Table/Callout/Figure 중 의미에 맞는 표현을 선택.
6. **Draft/Patch** — 승인본은 PATCH_FIRST. BASELINE_REJECTED일 때만 사용자 승인된 Fresh Rebuild.
7. **Automated gates** — Harness/README/DOCX/Visual/Accessibility/Link/Package 검사.
8. **Render** — DOCX 전페이지, PDF Fresh Export, PDFium/Poppler, README 실제 viewer 기준 확인.
9. **Scan pass** — 전페이지 Contact Sheet로 계층/리듬/밀도/고립/반복 패턴 검수.
10. **Detail pass** — 각 페이지 100%, 고위험 200%로 wrap/crop/connector/glyph/link 검수.
11. **Reader pass** — Persona 관점에서 실제 task를 처음부터 끝까지 따라가며 누락 여부 확인.
12. **Regression** — 승인 영역 Before/After 비교. 관계없는 회귀 0.
13. **Final acceptance** — 모든 required gate가 정확히 PASS이고 Manual Evidence가 존재해야만 최종 PASS.
14. **Clean replay** — exact Source에 Overlay/Delete Manifest를 적용해 byte/hash/stale/zip integrity 재검증.

`AUTOMATED_PASS_ONLY`, `NOT_EXECUTED`, `BLOCKED`, `UNKNOWN`, `PARTIAL`, `WAIVED`는 완료가 아니다.
