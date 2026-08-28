# Cross Renderer Check

- Poppler (`pdftoppm`): 11 PDFs / 101 pages rendered successfully.
- PDFium: 11 PDFs / 101 pages rendered successfully.
- Page counts match document-by-document.
- `pdf_preflight.py`: 11/11 PDFs, warnings 0.
- Raster antialiasing differences between Poppler and PDFium are not treated as semantic defects; pass evidence is openability, equal page counts, no preflight warning, and the final-page visual review.

**PASS**
