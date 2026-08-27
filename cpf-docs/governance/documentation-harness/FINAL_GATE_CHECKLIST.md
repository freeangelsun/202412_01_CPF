# CPF Documentation 최종 Gate Checklist — Harness v2.4.0

모든 항목이 실제 PASS여야 한다. 미실행/예정/READY는 PASS가 아니다.

## A. Harness
- [ ] 현행 Harness 한 세트만 존재
- [ ] 과거 version/history/backup/session/CHANGELOG 가비지 0
- [ ] Harness self-validator PASS
- [ ] quality-fixtures negative test PASS
- [ ] HARNESS_LOCK/PACKAGE_MANIFEST Hash 일치

## B. 독자/내용
- [ ] 첫 화면 1~2문장 Lead로 독자·목적·완료 결과 식별 가능
- [ ] Reader metadata 2열 Table 0
- [ ] 사용자 본문 Harness version/Source SHA/Build baseline 0
- [ ] Source-backed API/Option/Default 검증
- [ ] stale terminology/Architecture 왜곡 0

## C. Table
- [ ] 비Tabular Content를 Table로 표현한 사례 0
- [ ] Layout/Reader Metadata/단일 Key-Value Table 0
- [ ] Header Wrap 0
- [ ] 반복 의미 없는 Column 0
- [ ] 부당한 균등폭/50:50 0
- [ ] Merge/Split/Nested/Blank structural cell 0
- [ ] Data Table Header 지정
- [ ] Text Contrast 기준 PASS

## D. Heading/Layout
- [ ] H1/H2/H3 hierarchy 정상
- [ ] H1 orphan wrap 0
- [ ] H1 전 breathing room 충분
- [ ] H2/H3와 첫 Content grouping 정상
- [ ] 빈 문단으로 만든 Layout 0
- [ ] 페이지 과밀/과공백/고립 Block 0

## E. Figure/Connector
- [ ] 의미 Node/Text/Connector 단위 Geometry Manifest
- [ ] Coarse group-only manifest PASS 0
- [ ] Connector source/target/route/arrowhead 기록
- [ ] target boundary termination PASS
- [ ] target interior penetration 0px
- [ ] arrowhead body inside target 0px
- [ ] unrelated Node/Text/Label crossing 0
- [ ] empty-space termination 0
- [ ] Text/Node/Canvas/Frame overflow 0
- [ ] 의미 Graphical Object Contrast 3:1 이상
- [ ] color-only meaning 0
- [ ] semantic incomplete visual 0

## F. Embedded Render
- [ ] README Light/Dark actual view
- [ ] DOCX 전페이지 Render
- [ ] PDF Fresh Export 전페이지 Render
- [ ] Embedded crop/boundary intrusion 0
- [ ] Embedded effective text 10.5pt 미만 0
- [ ] 주요 Figure 10초 이해 PASS

## G. 접근성/PDF
- [ ] DOCX Accessibility High/Medium/Low = 0/0/0
- [ ] Alt Text/Heading/Data Table Header 정상
- [ ] PDF Font embedded
- [ ] PDFium + Poppler Glyph/Page parity
- [ ] broken link/glyph 0

## H. 패키징
- [ ] README 포함
- [ ] 공식 DOCX 11/11
- [ ] 공식 PDF 11/11
- [ ] Visual/Harness/Assets 포함
- [ ] cpf-tools/build 강제 포함
- [ ] 한글 경로 UTF-8/NFC + Windows Expand-Archive round-trip PASS
- [ ] Delete Manifest exact/root-relative/wildcard 0
- [ ] Fresh Clean Replay byte diff 0
- [ ] ZIP integrity + SHA-256 PASS
