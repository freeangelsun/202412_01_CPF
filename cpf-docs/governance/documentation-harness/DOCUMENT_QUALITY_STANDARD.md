# CPF 문서 품질 엔지니어링 표준 — Harness v2.10.0

## 1. 최종 품질 정의

CPF 공식 문서는 내용이 존재하는 것만으로 완료되지 않는다. 대상 독자가 빠르게 훑어 구조를 이해하고, 필요한 선택·실행·실패 대응·복구·검증까지 끝낼 수 있어야 한다. 자동 검사는 필요조건이며 전페이지 Render와 사람 검수를 대체하지 않는다.

최종 PASS는 `validators/validate_final_acceptance.py` 또는 `.ps1`이 모든 필수 Gate를 확인한 경우에만 가능하다. `AUTOMATED_PASS_ONLY`, `NOT_EXECUTED`, `BLOCKED`, `UNKNOWN`, `SKIPPED`, `PARTIAL`, `WAIVED`는 PASS가 아니다.

## 2. Reader Task 완결성

Keyword가 있다는 이유로 Coverage PASS를 주지 않는다. 문서별 Persona는 적용 가능한 범위에서 다음을 완료할 수 있어야 한다.

1. 이 문서를 왜 여는지와 해결할 질문
2. 언제 사용하고 무엇을 선택하는지
3. 필요한 입력·옵션·기본값·선행조건
4. 정상 흐름 또는 실행 절차
5. 오류·경계·부분 실패·UNKNOWN
6. 복구·Reconcile·Compensation 또는 다음 행동
7. 결과·상태·로그·DB·운영 화면의 검증 방법
8. Source·Sample·상세 Reference로 이동할 경로

필요한 Dimension이 하나라도 빠지면 `READER_TASK_COMPLETENESS_PASS`는 FAIL이다.

## 3. README

README는 Repository의 제품 간판이자 빠른 탐색 화면이다. 상세 Reference를 복제하지 않는다.

- 제품 H1은 하나만 사용한다.
- H2는 기본 필요 Coverage 기반, 상한 없음에서 독자의 질문·작업 중심으로 구성한다.
- 목차는 만들지 않는다.
- `CPF를 적용하면 무엇이 달라지는가`, `핵심 장점`, `왜 좋은가`, `이 구조의 장점`, `핵심 해석`, `기반 기술`, `차별점`, `효익`, `좋아지는 점`, `편해지는 점` 같은 장점 전용 Heading/Section을 금지한다.
- CPF의 강점은 제품 정의, 전체 구조, 개발 흐름, 거래·호출·실패/복구, Batch·연계·운영 설명 속에서 **구조와 동작의 결과로 자연스럽게** 드러내고 Source-backed 사실로 뒷받침한다.
- 긴 문단 3개 연속, 표 연속, 설명 없는 대형 Figure 연속, 한 Section 과밀을 금지한다.
- 일반 문단은 3~7 visual lines를 목표로 하며 420자 또는 5문장을 넘기지 않는다.
- 긴 API/옵션/실패 Reference는 PDF Guide/Specification으로 이동한다.
- 사용자 Navigation은 PDF만 노출하고 DOCX는 전달 패키지에만 포함한다.

## 4. 문서 도입부

제목·부제목 다음에는 자연스러운 1~2문장 Lead를 둔다. `누가 보는가 / 이 문서로 끝낼 일 / 기준` 같은 Reader Metadata를 2열 Key/Value Table로 만들지 않는다. Harness version, Source SHA, ZIP Hash, 제작 Baseline은 사용자 본문·표·Footer에서 금지하고 Evidence에서만 관리한다.

## 5. Vertical Rhythm

페이지 수를 줄이기 위해 여백을 축소하지 않는다.

- Body line spacing: 1.25 이상
- Body space after: 7.5pt 이상
- H1 before/after: 52pt / 11pt 이상
- H2 before/after: 28pt / 7pt 이상
- H3 before/after: 18pt / 5.5pt 이상
- 의미 Block 전환: 14pt 이상을 기본으로 검토

제목 아래 첫 내용은 다음 소제목 시작 전보다 가깝게 묶는다. 빈 문단으로 여백을 만들지 않는다. 수치가 맞아도 실제 Render가 답답하면 FAIL이다.

## 6. Table 사용과 열 폭

Table은 두 개 이상의 Dimension과 의미 있는 Data Row가 있을 때만 사용한다. 목차, Reader Metadata, 단일 메시지, 순차 절차, 한 행 Key/Value, Layout 용도로 사용하지 않는다.

열 폭은 실제 데이터와 의미 역할을 기반으로 배분한다.

- ID·상태·필수·기본값·Code: 좁게
- 설명·용도·선택조건·실패·복구·주의: 넓게
- 비대칭 데이터의 균등폭: 금지
- 균등폭 허용: 대칭 비교 + 의미 역할 동등 + Content Demand 변동 12% 이하
- Header Wrap: 0
- Short Token Wrap: 0
- Semantic Width Inversion: 0
- 반복 4줄 초과 Cell: 0

먼저 Header를 짧게 하고, 좁은 열을 줄이고, 설명 열을 넓히고, 필요하면 Table을 분리하거나 Landscape로 전환한다. Font를 줄여 억지로 맞추지 않는다. OOXML 폭만으로 PASS하지 않고 최종 Render에서 실제 줄개행을 확인한다.

## 7. Figure와 Connector

Connector는 source/target ID와 route를 가진다. 화살표는 대상 Box 내부를 찌르지 않고 **외곽 Boundary에서 종료**한다.

- target interior penetration = 0px
- source interior penetration = 0px
- arrowhead body inside target = 0px
- unrelated Node crossing = 0
- Text/Label crossing = 0
- unlabeled empty-space termination = 0

원본 PNG만 확인하지 않는다. README, DOCX, PDF 삽입 Render에서 Crop·Boundary·Contrast·Effective Text·Connector 충돌을 다시 검사한다.

## 8. 정보 표현 선택

- 순차 작업: Numbered List
- 병렬 정보: Bullet List
- 단일 메시지·장 목적·주의: Lead/Callout
- 실제 비교·선택·상태·규격: Table
- 구조·흐름·상태 변화: 의미에 맞는 Figure

모든 내용을 표나 네모+화살표 그림으로 반복하지 않는다.

## 9. 접근성

Heading Style과 논리적 계층을 사용하고, Informative Figure는 Alt Text를 제공한다. Data Table은 단순 Header 구조를 사용한다. Body는 좌측 정렬하고 Full Justification을 금지한다. 일반 Text 4.5:1, 큰 Text와 의미 그래픽 3:1 이상 Contrast를 사용한다. 색 하나만으로 의미를 전달하지 않는다.

## 10. 사용자 Finding 재발 방지

사용자 Finding을 Artifact만 수정해서는 안 된다. 다음을 모두 수행해야 한다.

1. Harness 공통 Rule 또는 대상 Profile 보완
2. Negative Fixture 추가
3. Validator가 해당 Fixture를 실제 FAIL시키는지 실행
4. Artifact는 PATCH_FIRST로 영향 범위만 보정
5. Before/After와 Embedded Render 회귀 검수
6. Final Acceptance Manifest에 Evidence 연결

Fixture가 실제로 거부되지 않으면 Harness 자체가 FAIL이다.

## 11. Current-only

Repository에는 `cpf-docs/governance/documentation-harness/` 현행본 하나만 유지한다. version folder, `_old`, `_backup`, `_history`, `_session`, CHANGELOG/history snapshot을 남기지 않는다. 삭제는 `DELETE_MANIFEST.txt`의 exact Root-relative 경로만 허용한다.

## 12. 최종 판정

평균 점수가 높아도 Critical Finding 1건이면 FAIL이다. Contact Sheet만으로 전페이지 수동 검수를 대체하지 않는다. 모든 required Gate=PASS, Manual Evidence 존재, Critical Finding=0, Artifact Review 승인 상태가 충족된 경우에만 최종 완료다.


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
## Reader-Executable / Visual Comfort Hard Gate

`READABILITY_AND_ACTIONABILITY_STANDARD.md`를 적용한다. API/선택표/키워드 존재만으로 완료하지 않고 Selection-to-Action, 실제 Consumer/Working Example, 실패·복구, 검증까지 닫는다. 전체 문서는 긴 Flat List와 Heavy Block 적층을 피하고 의미 그룹과 여백으로 과밀하지 않게 구성한다.

