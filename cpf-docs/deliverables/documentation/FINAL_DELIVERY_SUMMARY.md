# CPF Documentation Final Delivery Summary

- Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260827_125420(1).zip`
- Source SHA-256: `96587736A2BDCA1CE11896982E8DE5A7432FAF0CDC560125528FE4A236A3ECF9`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`
- Harness: `v2.5.0`, current-only, `PATCH_FIRST`
- Product artifacts: README 1 + DOCX 11 + PDF 11
- Product Visual: 8 PNG + geometry manifest
- Final DOCX/PDF pages: 53/53
- Harness/README/Visual/DOCX/Fixture Python gates: PASS
- DOCX Accessibility: 11/11 issues=0
- PDF Fresh Export/Preflight/Font: 11/11 PASS / warnings=0 / non-embedded font=0
- PDFium + Poppler: 53 pages rendered in both engines; no missing page/glyph/layout break in final manual QA
- User navigation: PDF-only, DOCX link=0
- Current-only/Stale cleanup: exact Delete Manifest packaged
- Windows PowerShell replay: 미검증 in Linux container; `VERIFY.ps1` packaged
