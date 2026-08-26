# CPF Documentation Test and Evidence

- Source: `master 054d894b47f4be8323439dc6f9e58b7d8b60fe54`
- Harness: `v2.1.0`
- 공식 제품 산출물: README 1 + DOCX 11 + PDF 11 = 23
- PDF 총 페이지: 108
- 상태: Documentation 자체검수 PASS. 사용자 Golden Baseline 승인 전이므로 Lifecycle은 `GOLDEN_BASELINE_CANDIDATE`다.

## 1. Source / Semantic 재검증

- 최종 패키징 직전 GitHub `master`를 다시 조회했고 `054d894b47f4be8323439dc6f9e58b7d8b60fe54`가 최신임을 확인했다.
- 최신 Batch 계약의 실행 Topology는 `LOCAL / PARALLEL_STEPS / LOCAL_PARTITION`으로 현행화했다.
- Batch 전용 Kafka/Broker Remote Execution, `REMOTE_PARTITION/CHUNK/STEP`을 제품 기능처럼 설명하지 않는다.
- 내부 Domain 간 호출은 Gateway를 경유하지 않는다. Gateway는 선택형 외부 Entry/Policy Boundary다.
- 공식 DB Vendor는 Oracle/PostgreSQL/MariaDB만 사용한다.
- 11개 DOCX 내부 Source identity와 Footer는 `054d894b47f4` 기준으로 일치하며 구 Source SHA 잔존 0건을 확인했다.

## 2. DOCX Render / Accessibility

- 최종 Overlay의 11개 DOCX 자체를 `render_docx.py --emit_pdf`로 다시 Fresh Render했다.
- Render page count: 14 + 10 + 9 + 8 + 9 + 13 + 10 + 8 + 9 + 6 + 12 = 108 pages.
- Fresh Render 108페이지가 직전 전페이지 수동 Visual QA 대상과 108/108 pixel-identical임을 확인했다.
- Accessibility audit: 11/11 문서 `High=0`, `Medium=0`, `Low=0`.
- 전페이지 수동 검수에서 clipping, overlap, broken glyph, 표 잘림, Figure/Caption 귀속 오류를 배포 차단 결함으로 발견하지 않았다.

## 3. PDF Fresh Export / Preflight / Renderer Parity

- 최종 PDF 11개는 위 최종 DOCX Fresh Render에서 다시 생성한 PDF다.
- Preflight: 11/11 `ok_open=true`, `encrypted=false`, `likely_scanned=false`, `xfa_present=false`, warning 0.
- `pdffonts` 기준 11/11 사용 Font embedding 누락 0건.
- PDFium / Poppler 전페이지 Render count 일치: 108 / 108.
- 두 Renderer의 동일 페이지를 숫자 기준으로 매칭해 전페이지 출력 존재를 확인했고, PDFium Contact Sheet를 다시 수동 검수했다.

## 4. README / Visual / Link QA

- Hero 포함 최종 Visual 8개, Markdown Content Visual 7개로 Harness Gate를 만족한다.
- README dark surface와 주요 Visual을 밝은 surface로 분리했다.
- 동일 `둥근 네모 + 화살표` 반복 대신 Architecture plane, route/lane, state model, control/execution, optional boundary, timeline, capability landscape를 사용한다.
- Architecture DB3 label overlap과 Capability 한글 glyph 깨짐은 최종 보정 후 재검수했다.
- Figure 바로 아래 해석/설명을 같은 블록으로 묶어 설명 귀속을 분명히 했다.
- `[PDF]` label은 `.pdf`, `[DOCX]` label은 `.docx`를 가리키며 Overlay 내부 실제 Target 존재를 Validator로 확인했다.

## 5. Harness / Package QA

- Harness Python Validator PASS: `VERSION=2.1.0`, `ARTIFACTS=12`, `COVERAGE_ITEMS=57`, `PROFILES=12`, `TABLE_PRESETS=21`, `FIGURE_PRESETS=23`.
- README Validator PASS.
- Harness Working Tree에는 과거 v1.x/v2.0.0 Harness version reference 0건이다.
- Root-relative Overlay ZIP을 Fresh Temp에 다시 해제해 Package Manifest, SHA256SUMS, required 23 artifacts, README links, 150-char absolute-path Gate를 재검증한다.

## 6. 이번 Documentation Evidence에서 새로 실행했다고 주장하지 않는 범위

CPF 제품 전체 Build/Test, Oracle/PostgreSQL/MariaDB 실제 Runtime lifecycle, 업무 Runtime E2E는 이번 Documentation 작업에서 새로 실행한 것으로 주장하지 않는다. 해당 제품 Runtime 판정은 최신 개발/QA Evidence의 역할이다.
