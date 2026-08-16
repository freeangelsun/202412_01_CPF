# TEST AND EVIDENCE - Documentation Delivery 2026-08-16

## Baseline

- Source archive: `CPF_FULL_SOURCE_FOR_NEXT_QA(9).zip`
- SHA-256: `7334ce55d1529cd5910f7b67ed97375ebcce63c0324b9668eb7bab38ccd0756e`
- Git exact SHA: `미확인` - supplied local ZIP has no `.git` directory.

## Writing-standard integrity

Command:
`cd cpf-docs/documentation-standards && sha256sum -c IMMUTABLE_SHA256SUMS.txt`

Result: 8/8 OK.

## Source contract review

Actual Source and consumer/sample paths were reviewed before finalizing examples. Representative evidence:

- `cpf-starters/web/.../CpfController.java`, `CpfBaseController.java`
- `cpf-starters/base/runtime/.../CpfService.java`, `CpfBaseService.java`
- `cpf-starters/data/persistence/.../CpfTx.java`, `CpfBaseRepository.java`, `CpfBaseDao.java`
- `cpf-starters/data/.../CpfCachePort.java`, `CpfCacheAsideService.java`
- `cpf-starters/integration/.../CpfClient.java`, `CpfTimeout.java`, `CpfRetry.java`
- `cpf-starters/messaging/.../CpfMessageListener.java`
- `cpf-starters/security/.../CpfPermission.java`, `CpfApprovalRequired.java`
- `cpf-starters/platform-operations/.../CpfAudit.java`
- `cpf-member/online/.../SampleTransactionController.java`, `SampleTransactionService.java`, `SampleTransactionRepository.java`
- `cpf-batch/api/.../CpfBatchJob.java`, `api/annotation/CpfBatchJob.java`, `CpfBatchStep.java`
- `cpf-batch/api/.../BusinessJobProvider.java`, `JobPackManifest.java`, `BatchExecutionControlPort.java`
- `cpf-education/.../EducationBatchEducationConfig.java`
- Canonical starter catalog: `cpf-tools/generator/contracts/cpf-starter-catalog.json` (`publicProfiles`=5, `capabilityGroups`=9).

## DOCX final render

Renderer: `/home/oai/skills/docx/render_docx.py --emit_pdf`

- 02 Framework Developer Guide: 16 pages
- 03 Batch Developer Guide: 12 pages
- 04 Operator Manual: 9 pages
- 05 Batch Operations Guide: 8 pages
- 06 Gateway Guide: 8 pages
- 07 Specification: 12 pages
- Total: 65 pages

Visual review result: no clipping, overlap, missing glyph, broken table, or page-boundary defect found in final render.

Final visual correction cycle: the 06 Gateway guide exposed Korean fallback-glyph squares in `cpf-gateway-route-workbench` / `cpf-gateway-publish`. The SVG font stack and PNG assets were corrected, the DOCX embedded images were replaced, and all 8 DOCX pages plus the regenerated PDF were rendered and visually reviewed again. No fallback glyphs remain.

## Accessibility

`a11y_audit.py` final result:

- 02: high=0 / medium=0 / low=0
- 03: high=0 / medium=0 / low=0
- 04: high=0 / medium=0 / low=0
- 05: high=0 / medium=0 / low=0
- 06: high=0 / medium=0 / low=0
- 07: high=0 / medium=0 / low=0

Final fix applied before shipping: image alt text and first-row table-header metadata. Re-render after this fix had 0 visually changed pages.

## Navigation / table structure

- DOCX broken internal hyperlinks: 0 for all six documents.
- PDF broken internal hyperlinks: 0 for all six documents.
- Dedicated TOC bookmark: present in all six documents.
- Equal-width multi-column tables: 0.
- Cells longer than 220 characters: 0.

PDF internal links after final export:
- 02: 44
- 03: 40
- 04: 32
- 05: 34
- 06: 29
- 07: 34

## PDF verification

All 6 final PDFs were rendered again with `/home/oai/skills/pdfs/scripts/render_pdf.py` at 150 dpi and visually reviewed. Page counts matched the DOCX exports (65 total).

## README verification

- Relative links/images: checked against overlay paths.
- README images: all 8 regenerated; 7 body graphics use 1672x941, hero uses 1800x560.
- Manual image fixes shipped with the overlay: `cpf-command-lifecycle`, `cpf-gateway-route-workbench`, `cpf-gateway-publish` (source SVG/PNG as applicable).
- Architecture/Gateway/Batch/Starter/Lifecycle visuals reviewed as one consistent set.
- Final section: `## License` with requested Community & Evaluation License wording.

## Not executed / not claimed

- Git HEAD/Working Tree verification: not executable because the supplied local source ZIP contains no `.git` metadata.
- Application runtime/build verification is outside this documentation-only overlay QA; documentation content was instead checked against the supplied Source contracts and existing consumers/samples.
- No commit, push, branch, reset, restore, clean, stash, or deletion was performed.
