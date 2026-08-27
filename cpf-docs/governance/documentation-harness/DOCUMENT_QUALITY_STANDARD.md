# CPF 문서 품질 엔지니어링 표준 — Harness v2.4.0

## 1. 목적

이 표준은 README와 11개 DOCX/PDF를 “내용이 존재하는 문서”가 아니라 **처음 보는 사람이 빠르게 이해하고 실제 개발·운영·판단을 끝낼 수 있는 상용 Framework 문서**로 만들기 위한 공통 품질 Gate다. 자동 검사와 사람 검수의 역할을 분리하며 어느 한쪽만 통과해서 완료할 수 없다.

## 2. 첫 화면과 문서 도입부

1. 제목과 부제목 다음에는 1~2문장의 자연스러운 Lead를 둔다.
2. Lead는 `누가 읽는가 → 무엇을 하려는가 → 무엇까지 끝낼 수 있는가`를 설명한다.
3. `누가 보는가 / 이 문서로 끝낼 일 / 기준`을 2열 Key/Value Table로 만드는 것을 금지한다.
4. 단순 Meta 정보는 Table로 만들지 않는다.
5. `Harness v...`, `Source SHA...`, `Source snapshot...`, ZIP Hash, 제작 Baseline은 사용자 본문/표/Footer에 노출하지 않는다. Evidence/Manifest/Handover에서만 관리한다.
6. 첫 화면의 시각적 우선순위는 `문서명 → 독자 가치 → 첫 행동/탐색`이다. 제작자의 검증 정보가 독자의 첫 행동보다 앞에 오면 FAIL이다.

## 3. 정보 표현 선택

### 3.1 Table

Table은 최소 2개의 독립 Dimension과 의미 있는 2개 이상의 Data Row가 있을 때 우선 검토한다. 한 행의 Key/Value, 장을 보는 이유, Golden Path 한 문장, 목차, 단순 메뉴, 순차 절차를 Table로 표현하지 않는다.

- Layout용 Table: 금지
- Reader metadata Table: 금지
- Merge/Split/Nested Data Table: 금지
- Header Row 없는 Data Table: 금지
- 의미 없는 반복 Column: 금지
- Header 자동 2줄: 금지
- 고정 50:50: 대칭 비교가 아니면 금지
- 긴 설명을 Cell에 밀어 넣기: 금지

Table은 최종 데이터로 열 폭을 결정한다. 짧은 Code/Status/ID는 좁게, 설명/조건/복구는 넓게 둔다. Header 글씨를 작게 줄여 맞추지 않는다.

### 3.2 List와 Procedure

순서가 중요하면 Numbered List, 순서가 중요하지 않은 병렬 정보는 Bullet List를 사용한다. 같은 List 안에서는 문장 구조와 품사를 최대한 평행하게 유지한다.

### 3.3 Lead/Callout

단일 메시지, 장 목적, 주의, Golden Path는 Full-width Lead 또는 짧은 Callout으로 표현한다. 짧은 메시지를 억지 Table로 확장하지 않는다.

## 4. 제목과 Vertical Rhythm

- Heading은 계층을 건너뛰지 않는다.
- H1은 한 줄을 우선하고 마지막 한두 단어만 다음 줄로 떨어지는 Wrap은 FAIL이다.
- H1 시작 전 여백은 제목 아래 첫 내용보다 명확히 크다.
- H2/H3는 첫 본문과 한 덩어리로 읽히게 붙이고 다음 Heading 전 여백은 더 크게 둔다.
- 빈 문단/수동 줄바꿈으로 여백을 만들지 않는다. Style/Token으로만 제어한다.
- 문단·Table·Code·Callout·Figure 전환마다 의미 단위에 맞는 차등 여백을 사용한다.

## 5. Figure와 Connector

### 5.1 Geometry Manifest 최소 상세

Group 전체를 큰 Rectangle 하나로만 등록한 Manifest는 PASS 근거가 아니다. 이해에 필요한 Node, Text, Annotation, Junction, Connector를 개별 Bounding/Route 데이터로 등록한다.

### 5.2 Connector Boundary 규칙

1. Connector는 source와 target ID를 가진다.
2. Connector route point를 Manifest에 기록한다.
3. target tip은 target Box의 외곽 Boundary에 닿는다.
4. shaft와 arrowhead body는 target Node 내부를 침범하지 않는다.
5. target interior penetration 허용치는 0px이다.
6. Endpoint가 아닌 Node/Container/Text/Label을 관통하지 않는다.
7. 명시적 Label/Hub/Junction이 없는 빈 공간에서 끝나지 않는다.
8. Box 내부에서 Connector를 끊거나 화살표가 Text를 향해 찌르는 모양을 만들지 않는다.

### 5.3 Contrast와 의미

- 일반 Text: 4.5:1 이상
- 큰 Text: 3:1 이상
- 의미 있는 Node Border/Connector/Graphical Object: 3:1 이상
- 상태/Owner/경계를 색 하나로만 구분: 금지

### 5.4 Embedded Render

원본 PNG가 정상이어도 PASS가 아니다. README 실제 폭, DOCX 삽입 크기, 최종 PDF Export 크기에서 다음을 다시 검사한다.

- Crop 0
- Frame/Page Boundary 침범 0
- Effective Text 10.5pt 미만 0
- Connector/Label/Node 충돌 0
- 설명 귀속 모호 0
- 주요 Visual의 10초 이해 실패 0

## 6. 접근성

- Heading Style과 Heading hierarchy를 사용한다.
- Informative Figure는 Alt Text를 가진다.
- Data Table은 단순 Header 구조를 사용한다.
- 중요한 정보는 Header/Footer에만 두지 않는다.
- Body는 좌측 정렬하고 Full Justification을 금지한다.
- 색만으로 의미를 전달하지 않는다.
- 최종 DOCX Accessibility 결과 High/Medium/Low = 0/0/0을 목표 Gate로 사용한다.

## 7. Developer Guide

주요 기능은 독자가 첫 Scan에서 다음을 찾을 수 있어야 한다.

`하려는 일 → Public API/Annotation → 옵션/기본값 → 언제 선택 → 언제 피함 → 실패 → Retry/Idempotency/UNKNOWN/Reconcile → 최소 예 → 검증 → Source`

API/Option/기본값은 최신 Source에서 검증한다. Source에 없는 API를 문서 편의를 위해 만들지 않는다.

## 8. README

README는 제품 간판이며 내부 Evidence 문서가 아니다. Architecture/Value/Golden Path/Manual Navigation을 빠르게 제공하되 Harness version, Source SHA, 내부 제작 상태를 본문에 노출하지 않는다. Visual은 Dark Content Surface 안에서도 Light/Neutral Focal Canvas로 충분히 분리한다.

## 9. 재발 방지

사용자 지적 하나를 고칠 때 다음 4단계를 모두 완료한다.

1. 공통 원인을 Harness Rule/Token/Component/Profile에 반영
2. `quality-fixtures.json`에 의도적으로 실패하는 Negative Fixture 등록
3. Validator가 해당 Fixture를 FAIL로 판정하는지 실행
4. 영향받은 산출물을 수정하고 Before/After + Embedded Render로 회귀 검증

규칙 문구만 추가하고 Validator/Fixture가 없으면 재발 방지 완료가 아니다.

## 10. 최종 판정

자동 검사 PASS는 필요조건이지 충분조건이 아니다. 실제 화면에서 어색함·충돌·과밀·모호함이 발견되면 해당 산출물은 FAIL이며, 먼저 Harness가 그 유형을 잡도록 보완한 뒤 산출물을 다시 수정한다.
