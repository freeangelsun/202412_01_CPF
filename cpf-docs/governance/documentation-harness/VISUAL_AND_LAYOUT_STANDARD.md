# CPF 산출물 시각·레이아웃 표준 — Harness v2.10.0

## 1. 목표

읽는 사람이 문서의 계층과 다음 행동을 빠르게 찾게 한다. 페이지 수보다 스캔성, 세로 호흡, 줄개행 최소화, 실제 Render 품질을 우선한다.

## 2. 세로 호흡

- Body line spacing 1.25 이상, paragraph after 7.5pt 이상
- H1 before 52pt / after 11pt 이상
- H2 before 28pt / after 7pt 이상
- H3 before 18pt / after 5.5pt 이상
- 의미 Block 전환은 최소 14pt를 기본 기준으로 검토
- 대메뉴 시작 전 여백은 제목 아래 첫 내용보다 명확히 크게 보이게 한다.
- 빈 문단/Space/Tab으로 여백을 만들지 않는다.
- 수치상 PASS여도 전페이지 Render에서 답답하면 FAIL이다.

## 3. 문단과 정보 밀도

일반 문단은 약 3~7 visual lines를 기본 목표로 한다. 긴 문단이 3개 이상 연속되면 재구성한다. 복잡한 내용은 Heading, List, Callout, Figure를 의미에 맞게 선택한다. 표·그림을 단순 장식으로 추가하지 않는다.

## 4. Table

- 실제 행/열 관계가 있을 때만 사용한다.
- Header는 한 줄이다.
- 비대칭 표는 내용·역할 기반 비례폭이다.
- ID/상태/필수/기본값 열은 좁고 설명/선택/실패/복구 열은 넓어야 한다.
- 대칭 비교가 아니면 균등폭을 허용하지 않는다.
- Header Wrap 0, Short Token Wrap 0, Semantic Width Inversion 0, 반복 과도 개행 0이 필수다.
- 실제 Render에서 개행을 확인하고 필요하면 Landscape를 사용한다.

## 5. Figure Geometry와 Connector

Text는 Parent Node 내부 safe padding을 유지한다. Connector는 source/target/route를 기록하며 대상 Box 외곽 Boundary에서 끝난다.

- target/source interior penetration 0px
- arrowhead body inside target 0px
- unrelated Node/Text/Label crossing 0
- unlabeled empty-space termination 0
- Frame/Canvas overflow 0

중앙 Hub가 의미상 필요하면 실제 Hub/Label을 둔다. Geometry manifest만으로 의미 완결성을 대체하지 않는다.

## 6. Embedded Render

README Light/Dark, DOCX 삽입 크기, 최종 PDF를 모두 확인한다. Crop, Boundary intrusion, 저대비, Effective Text 과소, Caption ownership 오류가 하나라도 있으면 FAIL이다.

## 7. README 전용

제품 H1 하나와 H2 최소 5개 권장, 상한 없음의 간결한 구조를 기본으로 한다. 장점/효익/차별점 전용 Heading을 만들지 않고, 구조·개발·실패복구·Batch·운영 흐름을 설명하면서 강점이 자연스럽게 드러나게 한다. 한 화면에 긴 문단·표·대형 Figure를 몰아넣지 않는다.

## 8. 사람 검수

Contact Sheet는 탐색 도구일 뿐 승인 Evidence가 아니다. 전페이지를 실제 크기로 검수하며 Cover/TOC/Wide Table/Figure/Code-heavy/마지막 페이지는 고위험 Page로 별도 확인한다. 사용자가 보기 어렵다고 판단한 결과는 자동 PASS를 무효화한다.


## v2.10.0 강제 보강

- README와 모든 공식 DOCX/PDF에는 총 파일 크기·페이지·문자·단어·Section/Figure 수 상한을 두지 않는다.
- 국소 Density/Paragraph/Table Threshold는 **재구성 Trigger**이며 정보 삭제 근거가 아니다.
- 길이 때문에 Source-backed Coverage 또는 Reader Task를 줄이면 FAIL한다.
- README는 브로셔형 Hero/시각 Story를 유지하고 모든 의미 Figure에 Alt Text + 바로 아래 간략 한글 설명을 제공한다.
- 작성자는 `DOCUMENT_DESIGN_PLAYBOOK.md`, `INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md`, `README_BROCHURE_AND_AI_TEXT_STANDARD.md`, `AUTHORING_EXECUTION_PROTOCOL.md`를 따라야 한다.
- 최종 시각검수는 전페이지 Scan pass + Detail pass 두 번을 모두 수행하고 Evidence를 남긴다.


## Harness 2.10.0 Visual Quality Uplift

- 자동 Validator PASS보다 실제 사용자/육안 Finding을 우선한다. 표 Header 2줄, 답답한 문단 호흡, 저대비 Header, 저밀도 마지막 페이지가 보이면 자동 PASS라도 FAIL이다.
- 승인되거나 잘 된 현행본은 PATCH_FIRST로 보존하며 Finding 영향 밖의 구조·내용·Visual을 전면 재작성하지 않는다.
- README는 900/1200/1440px에서 대메뉴 전환 호흡과 Header 단일 행을 확인한다. 폭이 부족한 Markdown 표는 유지하지 않고 독자 흐름에 맞는 prose/list/card로 바꾼다.
- 진한 DOCX 표 Header는 텍스트 대비 4.5:1 이상이며 AUTO/검정 글자를 허용하지 않는다.
- 마지막 페이지의 제목+한 문장, Source-only, 표 꼬리 1~2행, 의미 없는 대형 공백은 금지한다. 페이지 수를 줄이기 위해 전체 Font/Margin/Line spacing을 축소하지 않고 내용·국소 배치를 보정한다.
- README Visual은 원본과 900/1200/1440px 삽입 Surface에서 text safe area·crop·boundary intrusion 0을 확인한다.
- Windows VS Code built-in Markdown Preview Runtime은 가능한 Windows 환경에서 별도 실행한다. 실행하지 못한 경우 미검증으로 기록한다.
