# TEST AND EVIDENCE

## Source Identity
- Source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_205036.zip`
- SHA-256: `A5B7844665F4AC3BDAEC601389B306CEBD6F0407AD1C07930C40170611DB7A07`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`
- Working Source authority: user-provided final Source ZIP
- Harness: `v2.3.0` current-only
- Verified at KST: `2026-08-27T04:11:47+09:00`

## Actual executed gates
- Harness Python validator: **PASS** — VERSION=2.3.0, ARTIFACTS=12, COVERAGE_ITEMS=57, PROFILES=12, TABLE_PRESETS=23, FIGURE_PRESETS=23.
- README Python validator: **PASS** — VISUALS=8, user DOCX link=0.
- Visual geometry validator: **PASS** — ASSETS=8.
- DOCX final render: **PASS** — 11 documents, 71 pages total; all document contact sheets reviewed and changed pages re-opened at full render.
- DOCX accessibility: **PASS** — 11/11, High=0, Medium=0, Low=0.
- PDF fresh export from final DOCX: **PASS** — 11 PDFs, 71 pages.
- PDFium render: **PASS** — 71 pages.
- Poppler render: **PASS** — 71 pages; page counts match PDFium for all 11 PDFs.
- PDF preflight: **PASS** — warnings 0 for 11/11 PDFs.
- PDF font embedding: **PASS** — non-embedded font 0.
- Public API/source semantic check: **PASS** — 36 referenced `Cpf*` symbols, missing 0 in supplied Source.
- Stale user-document terms: **PASS** — old Harness 2.2 baseline, old source digest, `cpf-reference`, `cpf-biz-admin`, generic Figure labels in product artifacts = 0.
- Documentation artifact path gate: **PASS** — generated/modified README, Harness, Visual, DOCX/PDF and delivery metadata are <=150 characters for both supported home/company repository roots.
- Fresh Source Clean Replay: **PASS** — source ZIP fresh extract + exact Delete Manifest (0 entries) + 109-file overlay; overlay byte diff=0.
- Clean Replay package integrity: **PASS** — `PACKAGE_MANIFEST.json` and `SHA256SUMS.txt` hashes verified from the replayed tree.

## Manual visual QA focus
- Tables are used only for genuinely tabular/comparative data. TOC/navigation, chapter reason, Golden Path and simple sequences are not forced into fake tables.
- Table headers remain one visual line in reviewed final renders. Equal-width columns are used only where semantically symmetric; other tables use content/role-weighted widths.
- H1 major-section spacing and title single-line preference applied; section transitions use visible breathing room without blank-paragraph padding.
- Transaction visual has an explicit `RESULT STATE` center hub; no empty implied junction.
- Invocation/System6, Architecture DB3 band, Figure titles/notes stay inside safe margins.
- Batch/Gateway sparse tail pages were improved with task-relevant pre-execution/recovery check tables instead of decorative filler.
- User navigation exposes PDF only; DOCX files remain packaged editable artifacts and are not user-facing links.

## Baseline issue kept separate from Documentation result
- The supplied Source already contains long paths under `cpf-docs/work/evidence/codex/current/**`. A repository-wide absolute-path scan therefore is **not claimed as PASS**.
- These existing evidence files are outside this Documentation patch and were not modified/deleted. The delivery verifier intentionally gates only files generated or modified by this Documentation cycle.

## Not executed in this container
- Windows PowerShell validators were **not executed** because `pwsh`/Windows PowerShell is unavailable in this Linux container. They are packaged in `VERIFY.ps1` and Harness validators for local/company replay. This is recorded as `미검증`, not PASS.
