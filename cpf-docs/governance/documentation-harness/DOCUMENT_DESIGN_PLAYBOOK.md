# CPF Document Design Playbook — v2.8.0

이 문서는 작성자가 누구든 같은 수준의 시각 품질을 내도록 **작성 순서와 화면 판정 기준**을 고정한다. 총 페이지/용량을 줄이는 것은 목표가 아니다. 필요한 정보를 모두 제공하면서 읽기 쉽게 구조화하는 것이 목표다.

## 1. 먼저 독자의 화면을 설계한다

1. Profile에서 Persona, Entry Condition, Exit Condition을 읽는다.
2. 독자가 이 문서를 열었을 때의 질문을 3~8개 작업 단위로 적는다.
3. 각 작업에 `결정 → 입력/옵션 → 정상 흐름 → 실패/경계 → 복구 → 검증 → 상세 Reference` 중 필요한 Dimension을 배치한다.
4. 빠진 Dimension이 있으면 본문 작성 전에 Coverage Gap으로 기록한다.
5. 파일/Module 목록을 목차의 출발점으로 삼지 않는다.

## 2. 페이지 구성

- H1/대메뉴 전에는 가장 큰 여백, H2/H3 전에는 다음으로 큰 여백, 제목 아래 첫 내용은 더 가깝게 둔다.
- 빈 문단으로 여백을 만들지 않는다. Style/Token으로 제어한다.
- 한 페이지에 제목·본문·표·Figure를 모두 꽉 채우지 않는다. 시선이 쉬는 공간과 첫 스캔 진입점을 만든다.
- 반대로 제목+한 문장, 표 꼬리 1행만 남는 고립 페이지도 허용하지 않는다.
- 동일한 회색 박스/표/Callout을 연속 반복해 문서 전체가 한 덩어리처럼 보이게 하지 않는다.
- 문단이 길면 내용을 삭제하지 말고 의미 단위로 나눈다. Microsoft의 스캔 가이드처럼 짧은 문단과 충분한 문단 간 여백을 사용한다.

## 3. 표

- 실제 행×열 관계가 있을 때만 사용한다. 레이아웃, 독자/목적, 단일 메시지, 순차 절차를 표로 만들지 않는다.
- Column 폭은 `Header 최소폭 + short token 보호 + 셀 실제 내용량 + Column 역할 중요도`로 계산한다.
- 상태/ID/필수/기본값은 좁게, 설명/선택/실패·복구는 넓게 둔다.
- 비대칭 데이터의 균등폭과 고정 50:50은 FAIL이다. 대칭 비교만 예외다.
- Header와 짧은 API/Class/Path/상태 Token은 한 줄 우선이다. 줄이 꺾이면 폭 재배분 → Landscape → Table 분할 순서로 해결한다. Font를 먼저 줄이지 않는다.
- Word에서는 Fixed-width Table보다 Window/Contents 기반 유연 폭을 우선하고, 실제 Render에서 검증한다.

## 4. Figure와 도식

- 의미에 따라 Layer, Lane, State Map, Lifecycle, Timeline, Split Boundary, Mosaic 등을 선택한다. `둥근 네모+화살표`를 기본 문법으로 반복하지 않는다.
- Connector는 Node 내부로 들어가지 않고 경계에서 종료한다. Text/Label/다른 Node를 통과하지 않는다.
- Figure 자체뿐 아니라 README/DOCX/PDF 삽입 크기에서 Crop, Safe Area, Effective Text Size, Contrast를 확인한다.
- 이미지 안에 긴 설명을 넣지 않는다. 핵심 Label만 두고 상세 의미는 이미지 밖 본문으로 전달한다.

## 5. 색·타이포그래피

- 의미 없는 배경색/장식색을 사용하지 않는다. 색은 구조·상태·Category를 구분할 때만 사용한다.
- 일반 Text 대비는 4.5:1 이상, 큰 Text/의미 Graphic은 3:1 이상을 Hard Gate로 유지한다.
- Heading은 Font size뿐 아니라 위·아래 여백으로 계층을 만든다. 같은 Level은 같은 Rhythm을 사용한다.
- 정보가 많다고 Font/Margin/Spacing을 줄이지 않는다. 페이지가 늘어나는 것을 허용한다.

## 6. 고강도 시각검수

각 DOCX/PDF는 두 번 본다.

- **Scan pass:** 축소 Contact Sheet/전페이지에서 계층, 밀도, 리듬, 고립 페이지, 반복 Pattern을 찾는다.
- **Detail pass:** 각 페이지 100% 이상에서 Header wrap, Cell wrap, Alignment, Figure crop, Connector, Link, Glyph를 확인한다. 고위험 Figure/Table은 200%까지 본다.

Contact Sheet만 보고 PASS할 수 없다. 두 Pass의 Evidence가 모두 있어야 `FULL_PAGE_FRESH_EYES_REVIEW_PASS`가 PASS다.
