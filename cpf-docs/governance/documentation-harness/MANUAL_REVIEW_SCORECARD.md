# CPF Documentation Manual Review Scorecard — v2.10.0

자동 Validator는 필요조건일 뿐이다. 최종 품질은 실제 Render를 사람이 두 번 보고 아래 Scorecard로 판정한다.

## 점수

- **5 — 상용 완성도:** 처음 보는 독자가 거의 멈추지 않고 스캔/이해/실행 가능. 계층·여백·표·Figure·내용 선택이 자연스럽고 눈에 거슬리는 부분 없음.
- **4 — 상용 허용:** 경미한 개선 여지는 있으나 읽기/판단/실행을 방해하지 않고 Hard Fail 없음.
- **3 — 재작업 필요:** 답답함, 과밀, 불필요한 개행, 균형 불량, 정보 위치 혼란 중 하나가 눈에 띔. **FAIL.**
- **2/1 — 심각:** 구조/의미/접근성/시각 오류. **FAIL.**

모든 적용 Dimension은 4 이상, 평균 4.6 이상이어야 한다. Critical Finding은 점수와 무관하게 1건이라도 FAIL이다.

## Scan pass — 전페이지

축소 Contact Sheet에서 다음을 본다.

- H1/H2/H3 계층이 즉시 보이는가
- 대메뉴 전 충분한 여백이 있는가
- 페이지마다 시선 진입점이 있는가
- 표가 문서 전체를 덮어 답답해 보이지 않는가
- 빈/고립/표 꼬리 페이지가 없는가
- Figure/Callout/본문이 단조롭게 반복되지 않는가
- README가 브로셔형 제품 간판으로 보이는가

## Detail pass — 각 페이지 100%, 고위험 200%

- 표 Header/짧은 Token 자동개행 0
- 비대칭 균등폭/폭 역전 0
- Paragraph/Table/Figure/Callout 전환 간격 적절
- Figure Text/Connector/Boundary 충돌 0
- Crop/깨진 Glyph/저대비/잘못된 Link 0
- Figure 설명이 해당 Figure에 명확히 귀속
- README 의미 Figure의 Alt Text + 한글 Companion 존재

## Reader pass

Persona 역할로 문서를 처음부터 사용해 본다.

- 무엇을 선택해야 하는가
- 무엇을 입력/설정해야 하는가
- 정상 결과는 무엇인가
- 실패/UNKNOWN이면 어떻게 판단하는가
- Retry/복구/Reconcile/재처리는 어떻게 구분하는가
- 무엇을 확인하면 완료인가
- 더 정확한 API/Source/Specification은 어디인가

하나라도 문서 밖에서 추측해야 하면 Reader Task Complete가 아니다.

## README 전용

- 첫 화면에서 CPF 정체성과 제품 인상이 살아 있는가
- 장점을 숨기지 않되 홍보형 장점 장으로 분리하지 않았는가
- 전체가 표/장문 Reference Sheet처럼 보이지 않는가
- Visual이 의미 있고 서로 다른 Grammar를 사용하는가
- AI/검색/텍스트 독자가 Figure 설명만으로 핵심 의미를 이해할 수 있는가


## Harness 2.10.0 Visual Quality Uplift

- 자동 Validator PASS보다 실제 사용자/육안 Finding을 우선한다. 표 Header 2줄, 답답한 문단 호흡, 저대비 Header, 저밀도 마지막 페이지가 보이면 자동 PASS라도 FAIL이다.
- 승인되거나 잘 된 현행본은 PATCH_FIRST로 보존하며 Finding 영향 밖의 구조·내용·Visual을 전면 재작성하지 않는다.
- README는 900/1200/1440px에서 대메뉴 전환 호흡과 Header 단일 행을 확인한다. 폭이 부족한 Markdown 표는 유지하지 않고 독자 흐름에 맞는 prose/list/card로 바꾼다.
- 진한 DOCX 표 Header는 텍스트 대비 4.5:1 이상이며 AUTO/검정 글자를 허용하지 않는다.
- 마지막 페이지의 제목+한 문장, Source-only, 표 꼬리 1~2행, 의미 없는 대형 공백은 금지한다. 페이지 수를 줄이기 위해 전체 Font/Margin/Line spacing을 축소하지 않고 내용·국소 배치를 보정한다.
- README Visual은 원본과 900/1200/1440px 삽입 Surface에서 text safe area·crop·boundary intrusion 0을 확인한다.
- Windows VS Code built-in Markdown Preview Runtime은 가능한 Windows 환경에서 별도 실행한다. 실행하지 못한 경우 미검증으로 기록한다.
## Reader Trace / Visual Comfort Evidence

대표 Reader Task 최소 3개를 `시작 위치 → 선택 → 행동 → 결과 확인`으로 추적한다. `reader_actionability`, `selection_to_action`, `working_example_fit`, `failure_recovery_closure`, `visual_comfort`, `information_hierarchy`, `flat_list_density`, `heavy_block_rhythm` 각각에 page/section과 관찰 근거를 남긴다. 평균 4.6 이상이어도 동일 점수를 일괄 입력한 기록은 Evidence가 없으면 승인하지 않는다.

