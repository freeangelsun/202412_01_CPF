# CPF Documentation 최종 Gate Checklist — Harness v2.9.0

**한 항목이라도 미실행·FAIL·BLOCKED·UNKNOWN이면 최종 PASS 불가.** 마지막 판정은 `validate_final_acceptance`가 수행한다.

## 총 길이·Coverage

- [ ] README/DOCX/PDF 총 페이지/용량/문자/단어/Section/Figure Hard Cap 0
- [ ] 길이를 줄이기 위한 Source-backed Coverage 삭제 0
- [ ] 과밀은 삭제가 아니라 구조 분리/가로형/Navigation으로 해결
- [ ] README Hero/dark brochure/visual narrative 유지
- [ ] README 모든 의미 Figure Alt Text + 바로 아래 1~2문장 한글 설명
- [ ] 모든 공식 문서 Profile Persona/Entry/Exit/Reader Task 완결
- [ ] 전페이지 Scan pass Evidence 존재
- [ ] 전페이지 Detail pass Evidence 존재

## A. Harness
- [ ] canonical `documentation-harness/` 현행본 하나만 존재
- [ ] version/history/backup/session/CHANGELOG 가비지 0
- [ ] Harness self-validator PASS
- [ ] Negative Fixture 전건 실제 거부 PASS
- [ ] HARNESS_LOCK/PACKAGE_MANIFEST Hash 일치

## B. README
- [ ] 제품 H1 정확히 1개
- [ ] H2 최소 5개 권장, 상한 없음, H4 이상 0
- [ ] 목차 0
- [ ] `무엇이 달라지는가/핵심 장점/왜 좋은가/이 구조의 장점/핵심 해석/기반 기술/차별점/효익` 장점 전용 Heading 0
- [ ] 강점이 최소 7개 Capability Group, 4개 이상 기능 Section에 자연스럽게 분산
- [ ] 긴 문단 3개 연속 0
- [ ] 표 연속 0
- [ ] 설명 없는 대형 Figure 연속 0
- [ ] 상세 Reference 과밀 0
- [ ] PDF-only 사용자 Navigation

## C. Reader Task
- [ ] 12개 Artifact별 대상 Persona 확인
- [ ] 목적/질문 PASS
- [ ] 선택/사용 시점 PASS
- [ ] 입력·옵션·선행조건 PASS
- [ ] 정상 흐름/절차 PASS
- [ ] 실패·경계·UNKNOWN PASS
- [ ] 복구·Reconcile·다음 행동 PASS
- [ ] 결과 검증 PASS
- [ ] Source/Sample/상세 Reference PASS
- [ ] Keyword 존재만으로 PASS한 Artifact 0

## D. Vertical Rhythm
- [ ] Body line spacing >=1.25
- [ ] Body after >=7.5pt
- [ ] H1 before/after >=52/11pt
- [ ] H2 before/after >=28/7pt
- [ ] H3 before/after >=18/5.5pt
- [ ] 의미 Block 전환 호흡 정상
- [ ] 빈 문단 Layout 0
- [ ] 전페이지 답답함/과밀 0

## E. Table
- [ ] 비Tabular/Layout/Reader Metadata Table 0
- [ ] 비대칭 균등폭 0
- [ ] Semantic Width Inversion 0
- [ ] Header Wrap 0
- [ ] Short Token Wrap 0
- [ ] 반복 4줄 초과 Cell 0
- [ ] 긴 설명 Column 폭 부족 0
- [ ] 필요 시 Landscape 적용
- [ ] 실제 Render Width/Wrap Evidence 존재

## F. Figure/Connector
- [ ] Node/Text/Connector 상세 Geometry Manifest
- [ ] target/source interior penetration 0px
- [ ] arrowhead body inside target 0px
- [ ] unrelated Node/Text/Label crossing 0
- [ ] empty-space termination 0
- [ ] Canvas/Frame overflow 0
- [ ] README/DOCX/PDF Embedded Render PASS

## G. DOCX/PDF/접근성
- [ ] DOCX 11/11 구조 Validator PASS
- [ ] DOCX 전페이지 Render PASS
- [ ] PDF 11/11 Fresh Export
- [ ] PDF 전페이지 PDFium + Poppler PASS
- [ ] Accessibility High/Medium/Low = 0/0/0
- [ ] Font/Glyph/Link/Crop 오류 0
- [ ] 고립 마지막 Page/Table Fragment 0

## H. 회귀/패키지
- [ ] 기존 승인 영역 PATCH_FIRST 보존
- [ ] Before/After Regression PASS
- [ ] 사용자 Finding마다 Harness Rule + Fixture + Validator 존재
- [ ] Delete Manifest exact/root-relative/wildcard 0
- [ ] Fresh Clean Replay byte diff 0
- [ ] ZIP integrity/SHA-256/NFC PASS

## I. Final Acceptance
- [ ] 모든 required Gate 정확히 PASS
- [ ] Manual/Hybrid Gate Evidence 누락 0
- [ ] Artifact Review 승인 누락 0
- [ ] unresolved Critical Finding 0
- [ ] unresolved Finding 0
- [ ] `validate_final_acceptance` PASS
- [ ] `AUTOMATED_PASS_ONLY/NOT_EXECUTED/BLOCKED/UNKNOWN/PARTIAL/WAIVED` 상태 0


## v2.9.0 강제 보강

- README와 모든 공식 DOCX/PDF에는 총 파일 크기·페이지·문자·단어·Section/Figure 수 상한을 두지 않는다.
- 국소 Density/Paragraph/Table Threshold는 **재구성 Trigger**이며 정보 삭제 근거가 아니다.
- 길이 때문에 Source-backed Coverage 또는 Reader Task를 줄이면 FAIL한다.
- README는 브로셔형 Hero/시각 Story를 유지하고 모든 의미 Figure에 Alt Text + 바로 아래 간략 한글 설명을 제공한다.
- 작성자는 `DOCUMENT_DESIGN_PLAYBOOK.md`, `INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md`, `README_BROCHURE_AND_AI_TEXT_STANDARD.md`, `AUTHORING_EXECUTION_PROTOCOL.md`를 따라야 한다.
- 최종 시각검수는 전페이지 Scan pass + Detail pass 두 번을 모두 수행하고 Evidence를 남긴다.
