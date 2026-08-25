# CPF 공식 산출물 작성 하네스 v1.2.1

## 1. 목적과 변경 권한

이 하네스는 CPF 공식 README와 11개 DOCX/PDF의 내용·구조·시각 품질을 고정한다. 하네스는 **사용자가 하네스 수정을 명시적으로 요청한 경우에만** 수정한다. Source 변경, QA Finding, 작성자 선호는 수정 권한이 아니다.

## 2. 가장 중요한 작성 원칙

- 문서를 위한 문서를 만들지 않는다. 실제 선택·개발·판단·운영에 필요한 내용만 남긴다.
- README는 기능 목록이 아니라 CPF의 차별점과 실제 편의를 자연스럽게 보여주는 브로셔다.
- 개발자 가이드는 `하려는 일 → 기능/API → 옵션 → 선택 기준 → 짧은 예 → 주의/실패 → 확인` 순서다.
- 운영 문서는 `상황 → 확인/판단 → 조치 → 금지/승인 → 완료` 순서다.
- Specification/Standard는 정확한 Contract/규칙 Reference이며 장문 소개를 금지한다.
- 모든 DOCX/PDF는 목차가 필수다. README는 목차를 **두지 않는다**.
- 모든 대제목은 번호를 가진다. README H1은 `1.`, `2.` 형식을 제목 자체에 포함한다. DOCX는 자동 다단계 번호를 사용한다.

## 3. 공통 Layout

세로 문서는 상·하 9mm, 좌·우 10mm. 가로 문서는 4면 9mm. 본문·표 제목·표·그림 Caption은 같은 왼쪽 Grid에 정렬한다. H1은 이전 콘텐츠와 충분히 분리되도록 22pt 선행 간격을 사용하며 표/그림 직후에는 최소 18pt를 확보한다.

들여쓰기는 1단 4.5mm, 2단 9mm로 고정한다. 동일 Level의 불릿·번호가 다른 위치에서 시작하면 Visual FAIL이다. 3단 불릿은 원칙적으로 금지한다.

## 4. 표

표는 데이터 관계·비교·선택을 빠르게 할 때만 사용한다. 긴 설명을 표에 넣지 않는다.

- `표 <장번호>-<순번>. 제목` 필수
- 표 목적 1문장(55자 이하) 필수
- 일반 4열 이하, Landscape 5열 이하
- 한 Cell 한 문장, 권장 45자 이하, 70자/4줄 초과 시 본문 또는 불릿으로 이동
- Header만 중앙정렬. 설명·역할·사용법·경로·명령·원인·조치는 좌측정렬
- 균등폭 금지. 의미와 예상 길이에 따라 Column 비율 지정
- API/Class/Code가 열 폭 때문에 2줄 이상 깨지면 FAIL
- 연속 표는 12pt 이상 간격 + 각 표 제목/목적 필수

## 5. 그림과 시각화

그림은 많을수록 좋은 것이 아니다. 정보를 더 빨리 이해시키고 공식 제품 문서다운 완성도를 높일 때만 사용한다.

README는 Hero와 **실제 CPF 전체 Architecture Map**이 필수다. Architecture Map은 Business Domain, Framework/Starter/Common, Gateway Optional Boundary, Backoffice Boundary, Batch Control/Execution, DB3, Operations/Trace 영역을 한눈에 보여야 한다.

동일한 박스-화살표 Template 반복을 금지한다. Architecture Map, Split Compare, Recovery State, Batch Control/Execution, Lifecycle, Operations Trace, Capability Landscape, Decision Map 등 서로 다른 시각 역할을 사용한다. 동일 Layout은 전체에서 최대 2회다.

그림 내부 긴 문장, 작은 글자, 겹침, Crop, Box Overflow는 0건이어야 한다.

## 6. 내용 압축

H1 시작 설명은 2문장/180자 이내, H2는 기본 2문단 이하를 원칙으로 한다. 같은 내용을 다른 말로 반복하지 않는다. 표에 있는 내용을 아래 문단에서 다시 설명하지 않는다. 상세 계약은 Specification/Source로 연결한다.

문서별 Hard Page Max는 `content-density.json`에 고정한다. 넘으면 먼저 중복·장문·불필요 표를 줄인다. 그래도 넘으면 작성자가 임의 확대하지 않고 `HARNESS_CHANGE_REQUIRED`로 처리한다.

## 7. README 특수 규칙

- 목차 금지
- 번호형 H1 필수
- Hero 다음 실제 Architecture Map 필수
- 전체 Visual 5~8개, 동일 Layout 최대 2개
- 장점 전용 장 금지. 각 섹션 제목/설명에서 CPF 구조와 사용자 효익이 자연스럽게 드러나야 함
- License는 **`CPF는 Community & Evaluation License 안내를 기준으로 사용합니다.`** 의미의 지정 한 문장만 사용하고 부가 설명을 붙이지 않는다. Markdown 실제 문구는 `writing-style.json`의 exact sentence를 사용한다.

## 8. 완료 판정

최종 완료는 Semantic, Source Identity, Cross-document, Visual, Accessibility, PDF Preflight, Link, Unicode/NFC, Package Integrity가 모두 PASS일 때만 가능하다.

Visual PASS는 전페이지 렌더 후 사람이 표·그림·들여쓰기·페이지 밀도를 실제로 확인해야 한다. Contact Sheet만으로 PASS할 수 없다.


## Harness-First 지속 개선 규칙

사용자가 README/DOCX/PDF의 가독성·구조·내용 우선순위·Visual·PDF 품질을 직접 지적하면 산출물만 임시 보정하지 않는다. 먼저 Harness 공통 규칙 또는 대상 Profile을 현행화하고 Validator PASS 후 산출물을 다시 만든다.

- 대·중·소 메뉴는 번호와 차등 여백으로 구분한다.
- 개발자 가이드는 사용빈도가 높은 거래 패턴·API·옵션·선택·오류·복구 중심으로 유지하고 책처럼 늘리지 않는다.
- 내부 Domain 간 호출은 Gateway를 경유하지 않는다. Gateway는 외부 진입/정책 경계가 필요한 경우에만 선택한다.
- 의미 있는 Visual은 구조를 보여주고, 바로 아래 짧은 한국어 설명으로 의미를 고정한다.
- PDF는 한글 Font 임베딩과 복수 렌더러 Glyph 검증을 통과해야 한다.


## v1.2.1 시각 균형 원칙

- 모든 문서와 Figure는 내용의 정확성뿐 아니라 시각적 균형을 품질 Gate로 관리한다.
- Figure 글자 겹침·잘림·깨짐·낮은 대비·Connector 충돌·Group Title/Child Label 겹침은 0건이어야 한다.
- Node 내부 여백, Label/Node 간격, Connector clearance를 정량 기준으로 관리한다.
- 병렬 데이터 Label은 동일 baseline·크기·간격으로 배치하고, 그룹 제목과 별도 영역으로 분리한다.
- 모든 페이지에서 좌우/상하 정보 밀도·whitespace·강조의 무게를 확인한다. 한쪽 과밀/한쪽 과공백을 최종본으로 승인하지 않는다.
- 시각 결함을 발견하면 해당 그림 하나만 임시 수정하지 않고 공통 원인이면 Harness Token/Visual Rule을 먼저 보완하고 영향 문서를 재생성한다.
