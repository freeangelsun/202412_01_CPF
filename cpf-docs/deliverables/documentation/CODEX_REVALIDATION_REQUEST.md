# Codex Independent Revalidation Request

독립 검수 기준 Source는 `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_205036.zip`, SHA-256 `A5B7844665F4AC3BDAEC601389B306CEBD6F0407AD1C07930C40170611DB7A07`다. Harness v2.3.0을 먼저 읽고 README + 11 DOCX + 11 PDF를 독립적으로 검수한다.

특히 다음을 재확인한다.
- PATCH_FIRST 회귀 0
- 독자/역할/업무 니즈에 맞는 메뉴와 정보 구조
- 표 semantic fit, header 1줄, symmetric/content-aware column width 적합성
- H1 한 줄 우선과 major-section vertical rhythm
- Figure embedded boundary/crop/overlap/semantic completeness
- Transaction RESULT STATE center hub와 UNKNOWN recovery 의미
- Gateway가 내부 Domain-to-Domain 호출 경로가 아님
- User navigation PDF-only / DOCX user link 0
- Oracle/PostgreSQL/MariaDB 공식 DB3 의미
- Source-backed Public API/option consistency
- DOCX accessibility와 PDFium/Poppler glyph/font/page parity
- Documentation artifact path <=150과 supplied Source baseline의 기존 long-path debt가 구분되어 기록되는지
