# TEST AND EVIDENCE — Documentation Harness 2.13.0

## Source identity

- Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260828_191735.zip`
- Source SHA-256: `34D692419F7701EBC58439B00F0A5111DBBE629BC8C25F46ED17DB875D4E3EA5`
- Exact Git SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`
- Source Currentization: preAuthoring/finalValidation **PASS**, fingerprint `7F400D8B4D525F1A5D4B6CE0DC6B6FE8BF0723798E0F97F63DDA8292DC86E9A4`, files 10,039
- Product non-document Source drift: changed 0 / missing 0 / added 0

## Executed gates

- Harness self validation: PASS — version 2.13.0
- Negative fixtures: **104/104 PASS**
- Source alignment: PASS — inventory sample refs 414 / public developer artifacts 26
- README: PASS — H1 1 / H2 12 / visuals 9
- Reader task coverage: PASS — 12 artifacts
- Readability/actionability: PASS
- DOCX structure: **11/11 PASS**
- Visual comfort and rendered-page composition: hard failure 0
- DOCX Accessibility: **11/11, findings 0**
- PDF Preflight: **11/11, warnings 0**
- PDFium: **11 PDFs / 117 pages PASS**
- Poppler: **11 PDFs / 117 pages PASS**
- README dark preview: **900/1200/1440 PASS**, 9 visuals each
- Fresh-eyes/manual review: 12/12 artifact manifests, 3 reader task traces each

## Environment-specific not executed

- Windows PowerShell wrapper runtime: **미검증** — Linux container has no Windows PowerShell runtime.
- Windows VS Code built-in Markdown Preview WebView: **미검증** — unavailable in Linux container.

These two environment-specific items are not recorded as PASS.

## Package replay

- Exact Delete Manifest + 237 managed overlay files applied to an independent copy of the supplied Source: **PASS**
- Fresh-replayed `verify_documentation_delivery.py`: **PASS**
- Documentation Harness 2.13.0 / 100 fixtures / 12 reader tasks / 11 DOCX / 11 PDF / Manifest / SHA: **PASS**
- Product non-documentation Source drift after replay: **0 changed / 0 missing / 0 added**
- Final ZIP extraction/replay is executed after metadata locking; ZIP SHA is intentionally external to the archive.
- PDF Openability: **11/11 PASS** — pypdf, PyMuPDF, PDFium, pdfinfo, Poppler에서 열기/렌더 확인. VS Code Text Editor에서 `%PDF-` 바이너리가 보이는 것은 Viewer 연결 문제이며 PDF 손상으로 판정하지 않습니다.

