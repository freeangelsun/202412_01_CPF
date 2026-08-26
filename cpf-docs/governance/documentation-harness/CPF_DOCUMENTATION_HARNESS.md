# CPF Documentation Harness v2.2.0

## 0. v2 핵심 철학 — 새로 만드는 Harness가 아니라 개선하는 Harness

1. 기본 동작은 **PATCH_FIRST**다.
2. 전체 Fresh Rewrite는 예외이며 반드시 사유와 영향 범위를 기록한다.
3. 보존 기준선은 `USER_APPROVED` 또는 `VISUAL_QA_APPROVED`다. `AUTOMATED_PASS_ONLY`는 품질 승인으로 간주하지 않는다.
4. 승인된 제목·본문·표·그림·Link·Layout은 Finding 영향이 없으면 재생성하지 않는다.
5. 한 항목을 고친 뒤 관계없는 승인 영역이 바뀌면 **REGRESSION_FAIL**이다.
6. 모든 문서는 `component-system.json` Component + `design-tokens.json` Token으로 구성한다.
7. `HARNESS_SCHEMA_PASS`와 `HUMAN_VISUAL_PASS`는 다른 Gate다. 자동 PASS만으로 완료를 선언하지 않는다.
8. Golden Reference가 있으면 승인본보다 낮은 품질을 허용하지 않는다.
9. Profile은 Coverage/Outcome을 잠그며 정확한 H1/H2 개수를 채우는 것이 목표가 아니다. 가독성을 위해 인접 내용을 병합할 수 있다.
10. 새 Finding은 Harness 원인을 먼저 보완한 뒤 **해당 영향 범위만** 다시 Patch한다.

---

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

세로 문서는 상·하 9mm, 좌·우 10mm. 가로 문서는 4면 9mm. 본문·표 제목·표·그림 Caption은 같은 왼쪽 Grid에 정렬한다. H1은 이전 콘텐츠와 충분히 분리한다. H2/H3는 첫 내용과 한 블록으로 보이도록 아래 여백을 작게 하고 다음 부제목 전 여백을 더 크게 둔다.

README 포함 모든 문서에서 H2/H3 아래 본문·불릿·Callout·Figure 설명은 제목 기준선보다 4.5mm 안쪽의 공통 Content Rail로 정렬한다. 1단 불릿은 그 Content Rail 안에서 hanging indent를 사용하고, 동일 Level이 다른 위치에서 시작하면 Visual FAIL이다. Markdown은 4-space code indent 대신 semantic list/callout으로 같은 계층을 표현한다.

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


## v2.2.0 시각 균형 원칙

- 모든 문서와 Figure는 내용의 정확성뿐 아니라 시각적 균형을 품질 Gate로 관리한다.
- Figure 글자 겹침·잘림·깨짐·낮은 대비·Connector 충돌·Group Title/Child Label 겹침은 0건이어야 한다.
- Node 내부 여백, Label/Node 간격, Connector clearance를 정량 기준으로 관리한다.
- 병렬 데이터 Label은 동일 baseline·크기·간격으로 배치하고, 그룹 제목과 별도 영역으로 분리한다.
- 모든 페이지에서 좌우/상하 정보 밀도·whitespace·강조의 무게를 확인한다. 한쪽 과밀/한쪽 과공백을 최종본으로 승인하지 않는다.
- 시각 결함을 발견하면 해당 그림 하나만 임시 수정하지 않고 공통 원인이면 Harness Token/Visual Rule을 먼저 보완하고 영향받은 Block/페이지/링크만 Patch하고 필요한 경우 해당 파일만 재export한다.


## v2.2.0 점진 개선·링크·Visual Grammar 원칙

- 기본 작업 방식은 `PATCH_FIRST`다. USER_APPROVED 또는 VISUAL_QA_APPROVED 영역을 기준선으로 삼고 지적받은 영역과 실제 영향 범위만 보정한다. AUTOMATED_PASS_ONLY는 품질 승인으로 승계하지 않는다.
- 관계없는 PASS 페이지·문장·Visual은 유지한다. 전체 재작성/재디자인은 사용자의 명시 요청, 구조 손상, Canonical 전체 구조 변경 등 Harness가 허용한 경우만 수행한다.
- 변경 전/후를 비교해 좋아진 요소가 나빠지는 시각·내용 회귀가 0건인지 확인한다.
- H2/H3와 첫 내용은 가까이 묶고 하위 내용은 공통 Content Rail로 들여쓴다. 짧은 핵심/선택/주의/복구는 필요한 경우 marker를 사용한다.
- Figure 제목·이미지·설명은 하나의 블록으로 보여야 하며, 설명이 다음 섹션에 더 가까워 어느 그림 설명인지 모호하면 FAIL이다.
- README 다크 배경에서는 Architecture/Flow 등 주요 Visual을 밝은 neutral/light-tint canvas 또는 밝은 focal surface로 분리한다.
- 내용별 Visual Grammar를 선택한다. Architecture는 Layer/Zone, 호출은 Lane/Route, UNKNOWN은 State Ring/Branch, Batch는 Control/Execution Plane, DB3는 Lifecycle Spine+Vendor Band, Capability는 Mosaic/Cluster를 우선한다. 동일 Rounded Rectangle+Arrow 반복은 금지한다.
- 일반 사용자 Navigation에는 `[PDF]`만 제공하며 실제 `.pdf`를 직접 가리켜야 한다. `.docx` 링크가 README/산출물목록/사용자 탐색 Surface에 노출되면 Link FAIL이다.
- 회사 Windows 설치 검증은 PowerShell-only로 완료 가능해야 하며 Python은 필수 의존성이 아니다.

## 최초 재구축 예외 Lifecycle

기존 공식 산출물이 사용자에 의해 `BASELINE_REJECTED`로 판정된 경우에만 사용자 명시 요청으로 최초 1회 `INITIAL_FRESH_REBUILD`를 수행할 수 있다. 결과는 `GOLDEN_BASELINE_CANDIDATE`이며, `USER_APPROVED` 또는 `VISUAL_QA_APPROVED` 이후에는 `PATCH_ONLY`로 전환한다. 승인 이후 관계없는 영역의 Fresh Rewrite는 Regression FAIL이다.


## v2.2.0 독자 니즈·실행형 매뉴얼 강제 규칙

- 모든 산출물은 `누가 → 왜 연다 → 무엇을 결정/개발/운영한다 → 완료 기준`을 먼저 정의한다. 파일/기능 분류를 위한 메뉴는 금지한다.
- Framework/Batch Developer Guide의 주요 기능 장은 `Task Summary → Public API → Option/기본값 → 선택 기준 → 실패·복구 → 최소 예 → 검증 → Source`를 첫 스캔에서 찾을 수 있어야 한다. 설명문만 있는 장은 FAIL이다.
- API/옵션/선택표가 핵심이고 Portrait에서 token 개행·과밀이 발생하면 Landscape를 사용한다. 문서 방향보다 독자의 스캔 속도를 우선한다.
- 일반 사용자 Navigation은 PDF만 제공한다. DOCX는 최종 ZIP에 포함하지만 README/산출물목록의 바로 열기 링크로 노출하지 않는다.
- Figure 설명 앞에 `그림 해석`, `그림 설명` 같은 중간 라벨을 두지 않는다. Figure 바로 아래 1~2문장으로 의미를 자연스럽게 고정한다.
- 모든 Product Figure는 geometry manifest를 가진다. 실제 canvas/frame/node/text/annotation bounding box가 safe area를 침범하거나 서로 겹치면 자동 Gate에서 FAIL해야 한다.
- Markdown Viewer의 Host Background는 README가 제어할 수 없다. 대신 CPF가 소유하는 Hero/주요 Content Surface는 Dark Brochure로 분명히 만들고 그 위 주요 Visual은 light/neutral canvas로 분리한다.
