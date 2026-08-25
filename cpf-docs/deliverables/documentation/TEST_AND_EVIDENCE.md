# CPF Documentation Test and Evidence

## Source identity
- master SHA: `8670f6c9b675e3d210576c843d826898c781f9f0`
- Source ZIP SHA-256: `9c716e16752972bc15ec9834071f44d721c60d893c929a55a51ed95f654ee11e`
- Harness: v1.1.3

## Executed gates
- Harness validator: PASS / artifacts 12 / coverage 57 / profiles 12 / tables 21 / figures 23
- README validator: PASS
- DOCX: 11/11 Profile H1/H2 and orientation checked
- Accessibility: 11/11 High 0 / Medium 0 / Low 0
- PDF: 11/11 openable / not encrypted / not scan-only
- Final PDF pages: 146
- Visual QA: blank or body-low-density 0 / out-of-page text 0 / broken glyph 0
- Semantic/source QA: 92 source references exist; identified cross-capability mis-mappings were corrected and rechecked
- README Dark responsive render: 1440 / 1024 / 480 widths checked with WeasyPrint; 8 SVG visuals separately rendered with Inkscape

## Environment note
Chromium headless CLI hung before screenshot completion in this container. The same local Dark HTML/CSS was rendered using WeasyPrint for three widths and the SVGs were verified via Inkscape. This is recorded rather than being reported as Chromium PASS.

## Clean replay
- Input ZIP fresh extraction: PASS
- Exact Delete Manifest application: PASS
- Overlay application: PASS
- Harness validator after replay: PASS
- README validator after replay: PASS
- Delivery checksum/official artifact verifier: PASS
- Official artifact files: 23 / missing 0
- Legacy documentation asset files after cleanup: 0
- Fresh product-docs visuals: 8
- Final ZIP second replay: executed after package assembly; result recorded in `FINAL_DELIVERY_SUMMARY.md`.
