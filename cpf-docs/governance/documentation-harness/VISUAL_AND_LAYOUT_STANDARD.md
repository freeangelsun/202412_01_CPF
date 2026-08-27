# CPF 산출물 시각·레이아웃 표준 — Harness v2.5.0

## 1. 기본 원칙

시각 요소는 꾸밈이 아니라 독자의 이해 시간을 줄이는 도구다. 자동 Geometry PASS보다 실제 README/DOCX/PDF에서의 읽기 편의와 완결성을 우선한다.

## 2. Grid와 여백

- 본문, Table Title, Table, Figure Title/Explanation은 정의된 Rail에 정렬한다.
- H1 시작 전에는 이전 Block과 충분한 breathing room을 둔다.
- H2/H3 아래 첫 Content는 가깝게, 다음 Heading 전 간격은 더 크게 둔다.
- 빈 문단·Space·Tab으로 여백을 만들지 않는다.
- Page 한쪽 과밀/반대쪽 과공백은 FAIL이다.

## 3. 도입부

- 제목/부제목 뒤 1~2문장 Lead로 독자·목적·완료 결과를 설명한다.
- `누가 보는가`, `기준` 같은 제작형 Meta Label을 2열 Table로 배치하지 않는다.
- Harness version/Source SHA/Build baseline은 사용자 화면에 노출하지 않는다.

## 4. Table

- 실제 Data 관계가 있을 때만 사용한다.
- Header는 한 줄, Body는 좌측정렬 기본이다.
- Column 폭은 Content 역할/길이로 배분하고 대칭 비교만 균등폭을 허용한다.
- Merge/Split/Nested/Blank Structural Cell은 금지한다.
- Header/Body Text Contrast 4.5:1 이상을 확보한다.
- Layout Table, Reader Metadata Table, 단일 Key/Value Table은 금지한다.

## 5. Figure Geometry

### 5.1 Node/Text

Text는 Parent Node 내부 Padding을 확보하고 Canvas/Node/Frame 경계를 침범하지 않는다. Group Title은 Child Label과 별도 Band에 둔다.

### 5.2 Connector

- Geometry Manifest에 source ID, target ID, route points, target boundary point, arrowhead 정보를 기록한다.
- target tip은 Box Boundary에 닿고 Node interior로 들어가지 않는다.
- target interior penetration = 0px
- arrowhead body inside target = 0px
- unrelated Node interior crossing = 0
- Text/Label crossing = 0
- unlabeled empty-space termination = 0
- Connector가 중앙 Hub를 암시하면 실제 Label/Hub/Junction을 둔다.

### 5.3 Contrast

일반 Text 4.5:1, 큰 Text 3:1, 의미 있는 Border/Connector/Graphical Object 3:1 이상이다. 색 하나만으로 의미를 구분하지 않는다.

## 6. Embedded Render

원본 Asset PASS만으로 완료하지 않는다. README 실제 Viewer 크기, DOCX 실제 삽입 크기, PDF Fresh Export에서 다음을 확인한다.

- Crop/Boundary intrusion 0
- Effective minimum text 10.5pt 이상
- Connector/Label/Node collision 0
- Contrast 기준 유지
- Caption/Explanation ownership 명확
- 100%와 고위험 Label 200% 확인
- 주요 Figure 10초 이해 테스트 PASS

## 7. 시각 문법

모든 그림을 둥근 Box+Arrow Chain으로 반복하지 않는다. Architecture는 Layer/Zone, 호출은 Lane/Route, Recovery는 State/Branch/Ring, Batch는 Control/Execution Plane, DB3는 Lifecycle/Vendor Band, Operations는 Timeline/Trace처럼 의미에 맞는 문법을 선택한다.

## 8. 최종 사람 검수

Contact Sheet만으로 PASS하지 않는다. 전페이지를 보고 Cover/TOC/Figure/Wide Table/Code-heavy/마지막 Page는 100% 이상에서 검수한다. 사용자가 실제 화면에서 어색함을 발견하면 자동 PASS를 무효화하고 Harness 재발 방지부터 보완한다.
