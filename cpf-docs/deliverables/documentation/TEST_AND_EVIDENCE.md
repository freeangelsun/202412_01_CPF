# TEST AND EVIDENCE — Harness 2.9.0

## Executed

- `validate_harness.py` — PASS / 64 negative fixtures enforced
- `validate_quality_fixtures.py` — PASS 64/64
- `validate_readme.py` — PASS, H1 1 / H2 12 / Visual 9 / AI text companion PASS / Value groups 8 / Value sections 11
- `validate_docx_artifacts.py` — PASS 11/11
- `validate_reader_task_coverage.py` — PASS 12/12
- `validate_visual_assets.py` — PASS 9/9
- Source ZIP validation — PASS / 10,363 files / CPF DOCX 11 / CPF PDF 11 / product visual PNG 8 / Harness 79 / cpf-tools/build 168
- Canonical DOCX render (`render_docx.py`) — 11 documents / 101 pages
- Canonical DOCX render vs latest full visual-review render — pixel-different pages **0/101**
- DOCX accessibility — 11/11 / findings 0
- PDF preflight — 11/11 / warnings 0
- Poppler — 101 pages
- PDFium — 101 pages
- README dark browser surfaces — 900/1200/1440px re-rendered after final Hero companion correction and visually reviewed
- Final archive fresh-extraction replay — **PASS** (performed after frozen Manifest/SHA generation; final ZIP SHA-256 is reported outside the archive)

## Not executed because environment unavailable

- Native Windows PowerShell execution of packaged wrappers
- Native Windows VS Code built-in Markdown Preview WebView

These are recorded as `미검증`, not PASS.
