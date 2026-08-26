# CPF Documentation Test and Evidence

## 1. Source Identity
- User-supplied final source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_171223.zip`
- SHA-256: `B47BCE7700700BF4186B997E38AB84192F2DB391E750A3781CD66F398824514D`
- Git exact SHA: `UNAVAILABLE_IN_SUPPLIED_ZIP`; `.git` 없는 전달 ZIP이므로 과거 master SHA를 현재 성공 Evidence로 승계하지 않음
- Harness: `v2.2.0`

## 2. Harness-first 재발 방지
- Reader-first / audience need / task-first composition을 모든 공식 Profile에 반영
- Framework/Batch Developer Guide: Public API → 옵션/기본값 → 선택 시점 → 실패/복구 → 최소 예 → 검증/Source Content Model 강제
- 정보 밀도가 높은 개발자 Matrix는 landscape 허용
- `그림 해석`/`그림 설명` visible label 금지
- User navigation PDF-only, DOCX link forbidden / packaging required
- Visual Geometry: safe-area, explicit no-overlap, min vertical gap, frame/object boundary를 PowerShell + Python executable gate로 검증
- README Host background와 CPF-owned dark brochure surface를 구분; GitHub host theme 자체는 README가 강제할 수 없음을 Harness에 명시하고 CPF-owned Hero/Visual은 dark brochure surface로 유지
- current-only Harness; CHANGELOG/old/backup/versioned history artifact 금지

## 3. Automated Harness/README/Visual Gate
- `validate_harness.py`: PASS — VERSION=2.2.0, ARTIFACTS=12, COVERAGE_ITEMS=57, PROFILES=12, TABLE_PRESETS=23, FIGURE_PRESETS=23
- `validate_readme.py`: PASS — VISUALS=8
- `validate_visual_assets.py`: PASS — ASSETS=8
- PowerShell counterparts are included; current Linux environment has no pwsh, execution status `미검증`

## 4. DOCX final render
- 11/11 final DOCX를 `render_docx.py --emit_pdf`로 Fresh Render
- Pages: 15 + 8 + 5 + 6 + 6 + 6 + 7 + 5 + 4 + 5 + 3 = 70
- 모든 final page contact-sheet/manual visual review: clipping/overlap/table break/broken glyph/figure boundary 배포 차단 결함 0
- Accessibility audit: 11/11 High=0, Medium=0, Low=0

## 5. PDF final export / cross-render
- 최종 PDF는 위 final DOCX에서 Fresh Export
- PDF preflight warning: 0 / 11
- PDFium pages: 70; Poppler pages: 70; document-by-document page count parity PASS
- `pdffonts`: embedded-font 누락 0
- extracted text replacement glyph/tofu check: 0
- PDF annotation DOCX links: 0

## 6. Semantic / navigation / stale QA
- README `[PDF]` links actual `.pdf` target 존재; user-facing `.docx` links 0
- generic `그림 해석`/`그림 설명` 제거
- 내부 Domain→Domain 호출을 Gateway로 우회시키지 않음
- 공식 DB Vendor: Oracle/PostgreSQL/MariaDB
- Source identity stale historical master SHA 제거; supplied ZIP digest로 currentize
- Harness history stale target: CHANGELOG + V1.2.5 apply/delete scripts exact Delete Manifest

## 7. 범위 밖
CPF 전체 제품 Build/Test와 Oracle/PostgreSQL/MariaDB 실제 Runtime lifecycle은 Documentation 작업에서 새로 실행했다고 주장하지 않는다. 제품 Runtime QA Evidence와 분리한다.
