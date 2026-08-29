# PDF Openability Standard

## 목적

공식 PDF는 확장자만 `.pdf`인 파일이 아니라 일반 PDF Viewer와 독립 Parser/Renderer에서 실제로 열리고 렌더되어야 합니다.

## Hard Gate

모든 공식 PDF는 다음을 모두 만족해야 합니다.

- 파일 Header가 `%PDF-`로 시작
- EOF marker 존재
- 암호화되지 않음
- `pypdf`와 PyMuPDF가 모두 문서를 열고 동일한 Page Count를 반환
- 첫 페이지와 마지막 페이지를 PyMuPDF와 PDFium으로 실제 Raster render 가능
- `pdftoppm`(Poppler)이 사용 가능한 환경에서는 첫/마지막 페이지를 추가 Renderer로 실제 render
- `pdfinfo`가 사용 가능한 환경에서는 동일 Page Count 확인
- Git LFS pointer, HTML/Error body, 0-byte/비정상 축약 파일 금지

## VS Code 표시와 파일 유효성 구분

VS Code가 PDF Viewer가 아닌 Text Editor로 연결되어 있으면 정상 PDF도 `%PDF-1.x` Binary Text로 보일 수 있습니다. 이는 PDF 손상과 별개입니다. Harness는 Viewer 연동을 가정하지 않고 파일 자체 Openability를 복수 Parser/Renderer(pypdf, PyMuPDF, PDFium, 사용 가능 시 Poppler/pdfinfo)로 검증합니다. Windows에서는 기본 PDF Viewer/브라우저 또는 설치된 PDF Preview Editor로 여는 Runtime 확인을 별도 Evidence로 남깁니다.
