# CPF Documentation Final Delivery Summary

- Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_205036.zip`
- Source SHA-256: `A5B7844665F4AC3BDAEC601389B306CEBD6F0407AD1C07930C40170611DB7A07`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`
- Harness: `v2.3.0`, current-only, `PATCH_FIRST`
- Product artifacts: README 1 + DOCX 11 + PDF 11
- Visual assets: 8 + geometry manifest
- Final DOCX/PDF pages: 71
- User navigation: PDF-only; DOCX is packaged as editable source but not exposed as a user link.
- Harness/README/Visual/Python delivery gates: PASS
- Fresh Source Clean Replay: PASS — 109 overlay files, byte diff 0, package hashes PASS
- DOCX A11y: 11/11 High=0, Medium=0, Low=0
- PDFium/Poppler: 11/11 page-count parity, 71 pages
- PDF preflight/font embedding: PASS
- Generated/modified Documentation artifact absolute path >150: 0 for supported home/company roots.
- Supplied Source baseline has pre-existing long Codex evidence paths outside this Documentation patch; they were not modified or deleted.
- Windows PowerShell replay: 미검증 in this Linux container; `VERIFY.ps1` is packaged for post-apply Windows verification.
