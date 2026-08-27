# HANDOVER

- Canonical working Source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_205036.zip`
- Source SHA-256: `A5B7844665F4AC3BDAEC601389B306CEBD6F0407AD1C07930C40170611DB7A07`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`
- Harness: `cpf-docs/governance/documentation-harness/` v2.3.0 current-only
- Evolution rule: `PATCH_FIRST`; good sections/pages/visuals are preserved unless an actual finding or dependency requires change.
- Official artifacts: README + 11 DOCX + 11 PDF
- Visual assets: 8 + geometry manifest; geometry gate PASS
- DOCX/PDF final page count: 71
- PDFium/Poppler: 11/11 page-count parity
- A11y: 11/11 High/Medium/Low 0
- User navigation: PDF-only; DOCX packaged only
- Tables: only genuinely tabular/comparative data; equal widths only when semantically symmetric, otherwise content/role-weighted; header one-line hard gate.
- Titles/layout: H1 single-line preferred, major-section top breathing room, semantic vertical rhythm.
- Visuals: embedded safe-area/crop/overlap and semantic completeness are hard gates; Transaction has explicit RESULT STATE hub.
- Supplied Source has pre-existing long paths under `cpf-docs/work/evidence/codex/current/**`; this Documentation patch does not modify/delete them.
- Windows PowerShell verifier is packaged but was not executable in this Linux container.
- Cross-environment commands: auto-detect repository root with `git rev-parse --show-toplevel`; use `$HOME\Downloads`; do not hard-code home/company drive paths.
