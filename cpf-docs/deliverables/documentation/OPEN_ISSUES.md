# OPEN ISSUES

- Windows PowerShell-only replay: **미검증** in this Linux container because `pwsh`/Windows PowerShell is unavailable. Re-run `cpf-docs\deliverables\documentation\VERIFY.ps1` after applying the overlay on Windows.
- Documentation/Harness/README/Visual/official DOCX/PDF files generated or modified in this cycle: **absolute path >150 = 0** for both supported home/company repository roots.
- Supplied Source baseline contains pre-existing long paths under `cpf-docs/work/evidence/codex/current/**` that exceed 150 characters when combined with the known home/company roots. These files were **not created, changed, deleted, or claimed as PASS by this Documentation patch**. They are retained because they are outside the Documentation artifact change set and evidence deletion requires explicit approval.
- No known product-document visual/layout/content defect remains from the findings addressed in this cycle.
