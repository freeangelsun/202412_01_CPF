# CPF Documentation Harness v2.12.0

## 1. 목적

CPF 공식 README와 11개 DOCX/PDF의 내용·구조·시각·접근성·패키징 품질을 하나의 실행 가능한 기준으로 고정한다. 사용자 지적은 산출물만 임시 수정하지 않고 Harness에 먼저 반영해 동일 결함의 재발을 막는다.

## 2. 변경 권한과 현행본

- 사용자가 Harness 수정/보완을 명시적으로 요청한 경우에만 Harness를 변경한다.
- Source 변경이나 작성자 취향만으로 Harness를 임의 변경하지 않는다.
- Repository에는 `cpf-docs/governance/documentation-harness/` 현행본 하나만 유지한다.
- version/history/backup/session/snapshot 복제본과 CHANGELOG 누적본을 제품 Source에 남기지 않는다.
- Harness 수정 시 `HARNESS_LOCK.json`, `PACKAGE_MANIFEST.json`, Delete Manifest를 다시 생성한다.

## 3. Harness-first 재발 방지

사용자 지적이 발생하면 `Finding → 공통 원인 → Harness Rule/Token/Component → Negative Fixture → Validator → Artifact Patch → Embedded Render Regression` 순서로 처리한다.

규칙 문구만 추가하고 Fixture/Validator가 동일 실패를 잡지 못하면 재발 방지 완료가 아니다. 자동 Validator가 PASS해도 사용자가 실제 화면에서 결함을 발견하면 해당 Artifact는 FAIL이다.

## 4. 독자 중심 문서 구조

- 모든 문서는 누가 왜 여는지, 무엇을 결정/개발/운영하고 무엇을 끝낼지 먼저 설계한다.
- 문서 시작부는 제목/부제목 다음 1~2문장 Lead를 사용한다.
- `누가 보는가 / 이 문서로 끝낼 일 / 기준` 같은 Key/Value 정보를 2열 Table로 만들지 않는다.
- Harness version, Source ZIP/SHA, build/evidence baseline은 사용자 README/DOCX/PDF 본문에 노출하지 않는다.
- Developer Guide는 `하려는 일 → Public API → 옵션/기본값 → 선택 → 실패/복구 → 최소 예 → 검증 → Source`를 빠르게 찾을 수 있어야 한다.
- 운영 문서는 `상황 → 확인/판단 → 조치 → 승인/금지 → 완료` 흐름을 사용한다.
- Specification/Standard는 장문 소개보다 Contract/규칙 Reference에 집중한다.

## 5. Table

Table은 행·열의 교차점이 실제 의미 있는 데이터일 때만 사용한다. Layout, 단일 메시지, 단순 Key/Value, Reader metadata, 목차, 순차 절차는 Table 금지다.

- Header 1줄
- 단순 Header 구조
- Merge/Split/Nested 금지
- 빈 구조 Row/Column 금지
- 반복 의미 없는 Column 금지
- 대칭 비교가 아니면 고정 50:50 금지
- 실제 Content/Role 기반 Column 폭
- Body 좌측정렬 기본
- 일반 4열, Landscape 5열 이내
- 긴 설명은 본문/불릿으로 이동

## 6. Heading과 Vertical Rhythm

DOCX/PDF의 H1/H2/H3는 문서 Profile의 번호/계층을 유지한다. README는 제품 H1 하나와 짧은 H2 구조를 사용하며 번호형 Heading을 강제하지 않는다. H1은 한 줄을 우선하고 마지막 1~2단어 고립 Wrap을 허용하지 않는다. H1 시작 전 여백은 제목 아래 첫 Content보다 명확히 크며, H2/H3는 첫 Content와 가깝게 묶는다. 빈 문단으로 여백을 만들지 않는다.

## 7. Figure와 Connector

Figure는 구조·흐름·상태·선택을 Text/Table보다 빠르게 이해시킬 때만 사용한다.

- 의미 Node/Text/Annotation/Connector를 개별 Geometry Manifest에 기록한다.
- Group Rectangle만 등록한 coarse manifest는 PASS 근거가 아니다.
- Connector는 source/target과 route를 가진다.
- target tip은 Box 외곽 Boundary에서 종료한다.
- shaft/arrowhead body의 target interior 침범은 0px이다.
- Endpoint가 아닌 Node/Text/Label 관통은 0건이다.
- Label/Connector 충돌 0건, Crop/Overflow 0건이다.
- 이해에 필요한 Graphical Object/Connector는 배경과 3:1 이상 대비를 가진다.
- 색만으로 의미를 전달하지 않는다.
- 원본 PNG만 보지 않고 README/DOCX/PDF 실제 삽입 크기에서 Effective Text, Crop, Boundary, Contrast를 다시 검사한다.

## 8. 접근성

일반 Text 대비 4.5:1, 큰 Text 3:1을 기준으로 한다. Heading Style/Hierarchy, Alt Text, 단순 Data Table Header, 의미 있는 Link Text, 좌측정렬 본문을 사용한다. 최종 DOCX Accessibility와 PDF Font/Glyph/Link를 실제 산출물에서 검증한다.

## 9. README

README는 제품 간판이다. Hero/Architecture/핵심 Value/개발 진입/문서 Navigation을 빠르게 제공하되 내부 Evidence Metadata는 노출하지 않는다. 사용자 문서 Navigation은 PDF만 제공하고 DOCX는 패키지에만 포함한다.

## 10. Source ZIP 완전성

다음 세션용 Source ZIP은 tracked + non-ignored untracked를 기본으로 하되 `README.md`, `cpf-docs/guides/**`, `cpf-docs/deliverables/**`, `cpf-docs/assets/**`, `cpf-docs/governance/documentation-harness/**`, `cpf-tools/build/**`를 강제 포함한다. 생성 후 DOCX 11, PDF 11, Visual, Harness, Assets, Tools Build 수량과 Unicode/NFC Round-trip을 검증한다.

## 11. 완료 Gate

Semantic, Source Identity, Reader Task Fit, Table Semantic Fit, Heading Rhythm, Figure Geometry, Connector Boundary, Embedded Figure, Contrast/Non-text, Accessibility, PDF Preflight, Link, Human Visual, Human Reader, Negative Fixture, Regression, Package Replay가 모두 PASS여야 한다. 미실행은 PASS가 아니다.


## Strict Final Acceptance

개별 Validator가 PASS해도 완료가 아니다. `validators/validate_final_acceptance.py` 또는 `.ps1`이 모든 required Gate=PASS, Manual Evidence 존재, Critical Finding=0, Artifact Review 승인 상태를 확인해야 최종 PASS다. `NOT_EXECUTED/BLOCKED/UNKNOWN/PARTIAL/WAIVED`는 PASS로 변환하지 않는다.


## v2.12.0 강제 보강

- README와 모든 공식 DOCX/PDF에는 총 파일 크기·페이지·문자·단어·Section/Figure 수 상한을 두지 않는다.
- 국소 Density/Paragraph/Table Threshold는 **재구성 Trigger**이며 정보 삭제 근거가 아니다.
- 길이 때문에 Source-backed Coverage 또는 Reader Task를 줄이면 FAIL한다.
- README는 브로셔형 Hero/시각 Story를 유지하고 모든 의미 Figure에 Alt Text + 바로 아래 간략 한글 설명을 제공한다.
- 작성자는 `DOCUMENT_DESIGN_PLAYBOOK.md`, `INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md`, `README_BROCHURE_AND_AI_TEXT_STANDARD.md`, `AUTHORING_EXECUTION_PROTOCOL.md`를 따라야 한다.
- 최종 시각검수는 전페이지 Scan pass + Detail pass 두 번을 모두 수행하고 Evidence를 남긴다.
## Reader-Executable 목적 재확인

이 Harness는 문서 파일의 존재나 Geometry만 검사하기 위한 체계가 아니다. 최신 Source와 정본을 바탕으로 대상 Persona가 `질문/목적 → 선택 → 설정/입력 → 실제 Consumer/절차 → 정상 결과 → 실패/UNKNOWN → 복구/다음 행동 → 검증 → Reference`를 끝낼 수 있는 공식 산출물을 만들고 검수하기 위한 생산 Harness다.

API/Annotation/Keyword/선택표만 존재하는 문서는 How-to 완료로 인정하지 않는다. 전체 문서는 정보량을 유지하되 긴 Flat List와 Heavy Block 적층을 의미 그룹·여백·Component 재구성으로 해소하고, 페이지 수를 줄이기 위해 Font/Margin/Line spacing을 축소하지 않는다.

