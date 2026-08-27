# TEST AND EVIDENCE

## Source Identity
- Source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_125420(1).zip`
- SHA-256: `96587736A2BDCA1CE11896982E8DE5A7432FAF0CDC560125528FE4A236A3ECF9`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`
- Working Source authority: user-provided final Source ZIP
- Harness: `v2.5.0` current-only / `PATCH_FIRST`
- Verified at KST: `2026-08-27T17:14:24+09:00`

## Actual executed gates
- Source ZIP completeness: **PASS** - FILES=10302, DOCX=11, PDF=11, Product Visual PNG=8, Harness=57, `cpf-tools/build`=168.
- Harness Python validator: **PASS** - VERSION=2.5.0, ARTIFACTS=12, COVERAGE_ITEMS=57, PROFILES=12, TABLE_PRESETS=23, FIGURE_PRESETS=23.
- Quality negative fixture validator: **PASS** - 23/23 expected failures detected.
- README validator: **PASS** - visual references=8, user DOCX link=0, production provenance exposure=0.
- Visual geometry validator: **PASS** - assets=8; connector source/target IDs, boundary termination, target/source intrusion=0, route/text collision=0.
- DOCX structural validator: **PASS** - 11/11; opening reader/purpose metadata table=0, user-facing Harness/Source provenance=0, single-row layout table=0, heading level skip=0, repeating table headers enforced.
- DOCX fresh render: **PASS** - 11 documents, 53 pages; every page reviewed after final patch.
- DOCX accessibility audit: **PASS** - 11/11, issues=0.
- PDF fresh export from final DOCX: **PASS** - 11 PDFs, 53 pages.
- PDF preflight: **PASS** - 11/11, warnings=0, scanned=false, encrypted=false.
- PDF font embedding: **PASS** - non-embedded font=0 across 11 PDFs.
- PDF renderer verification: **PASS** - PDFium and Poppler rendered all 53 pages. PDFium vs Poppler numeric page-order SSIM average=0.9677, minimum=0.9407; no missing page/glyph/layout break found in manual review.
- Public API/source semantic check: **PASS** - Framework Developer Guide referenced 34 `Cpf*` symbols; missing source symbol=0.
- Documentation path gate: **PASS** - overlay generated/modified file absolute path >150 chars=0 for both supported home/company repository roots.
- Stale/current-only gate: **PASS** - canonical Harness only; versioned `_old/_backup/_history/_session` paths are exact Delete Manifest candidates and are not packaged as current artifacts.

## High-intensity manual visual QA
- Existing artifacts were **not rebuilt from scratch**. Good existing structure/content was preserved and only failed patterns were patched.
- Opening reader/purpose/basis key-value tables were removed. Reader purpose now uses a short lead/callout and Evidence-only provenance is separated from user-facing content.
- Tables are reserved for relational/comparative data. One-row layout tables, repeated-value columns, unjustified 50:50 widths, wrapped headers, decorative tables are hard-fail patterns.
- Major headings retain visible breathing room; H1 single-line preference and H2/H3 vertical rhythm are checked in final renders.
- Visual 8종 were re-reviewed at source and embedded-document scale. Connector/arrow shafts end on box boundaries; target/source box interior penetration, connector/text crossing, label/canvas overflow and crop are hard fail.
- Transaction visual has an explicit `RESULT STATE` hub; Gateway visual makes clear that internal Domain-to-Domain calls do not traverse Gateway.
- Trailing pages were reworked where table tails or one-sentence sections were isolated. The Specification closing page remains intentionally independent because it contains a substantive topology decision matrix and closing contract, not decorative filler.
- User navigation remains PDF-only; DOCX is packaged for editable archival use and is not exposed as a user navigation link.
- 찾아보기/TOC의 right-aligned dotted tab stop은 각 Section writable width로 재계산했다. Portrait 문서의 page number overflow를 제거했고, DOCX/PDF 최종 Render에서 page number가 오른쪽 안전영역 안에 표시됨을 확인했다. Harness에는 `tocTabStopOutsideWritableArea` Negative Fixture/Validator Gate를 추가했다.

## External quality reference used to strengthen Harness
- Microsoft Style Guide: scannable content, data-oriented tables, concise procedure headings/steps.
- GOV.UK: use tables only for data, proper Heading hierarchy/Table Header/contrast/accessibility checks.
- WCAG 2.2: normal text contrast 4.5:1 and meaningful graphical-object/non-text contrast 3:1.
- These references strengthen the CPF minimum quality floor; CPF user steering and Source contract remain authoritative.

## Not executed in this container
- Windows PowerShell validators: **미검증** because `pwsh`/Windows PowerShell is not installed in this Linux container. Equivalent Python validators were actually executed and passed. `VERIFY.ps1` is packaged for user-side Windows replay.

## Baseline issue intentionally kept separate
- The supplied Source may contain pre-existing long paths under `cpf-docs/work/evidence/codex/current/**`. They are outside this Documentation patch and were not deleted. Documentation path PASS applies to files generated/modified by this cycle.
