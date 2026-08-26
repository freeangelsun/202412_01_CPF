# CPF Documentation v1.2.5 TEST AND EVIDENCE

- 작성일: 2026-08-26
- Target master: `9893c03f43dde6ff0198e5ff71fc564747e08845`
- Harness: `v1.2.5`

## 1. Harness
- `validate_harness.py`: PASS
- ARTIFACTS=12 / COVERAGE_ITEMS=57 / PROFILES=12 / TABLE_PRESETS=21 / FIGURE_PRESETS=23

## 2. DOCX
- 공식 DOCX 11개 생성/현행화
- 최종 Accessibility Audit: 11/11 High=0, Medium=0, Low=0
- 최종 DOCX 렌더 후 목차/Footer/Figure/Table 경계 재검수 완료

## 3. PDF
- 공식 PDF 11개 Fresh 생성
- 총 페이지: 131
- Openable / Not Encrypted / Not Scan-only
- `pdffonts`: 미임베딩 Font 0
- PDF text QA: blank=0, low-density isolated=0, broken/replacement glyph=0, outside-page text=0
- Poppler + PDFium: 11/11 문서에서 원본 PDF 페이지 수와 두 renderer의 렌더 페이지 수 일치

## 4. Source-backed cross-check
- DOCX Source 길찾기 추출: 75건
- 제공된 Source snapshot 기준 누락: 0건
- README: 내부 Domain↔Domain Gateway 미경유, Gateway optionality, UNKNOWN/Reconcile, Idempotency, Saga/TCC/XA, Batch Lease/Fencing, DB3, Bootstrap/Build/Test/Runtime, 역할별 문서 진입점 확인

## 5. 제한/구분
- Packaging 대상 master SHA는 GitHub에서 `9893c03f43dde6ff0198e5ff71fc564747e08845`로 재확인했다.
- 로컬 Clean Replay 기반 Source ZIP은 현재 master보다 이전 snapshot이다. 따라서 그 Replay는 Overlay/Delete/Hash/Harness/Path 재현성 검증이며, 최신 master 전체 Source Runtime PASS로 기록하지 않는다.
- Commit/Push는 수행하지 않았다.

## 6. Clean Replay
- 제공된 Source ZIP fresh extract 후 exact Delete Manifest 적용 → Overlay 적용 → Harness/README/Documentation verifier PASS.
- Overlay byte mismatch: 0
- Legacy `assets/readme|manual|manuals|guides` files: 0
- 이전 product-doc SVG 8종: 0
- 이번 Overlay 생성 파일 최대 Windows 절대경로 길이: 120자 (150자 초과 0)

## 7. ZIP Replay
- UTF-8 ZIP open/CRC test: PASS
- ZIP payload ↔ overlay byte mismatch: 0
- ZIP payload를 fresh source snapshot에 delete→apply 후 Harness/README/Documentation verifier PASS.
- Final replay legacy Documentation assets: 0
- Final replay old product visuals: 0
