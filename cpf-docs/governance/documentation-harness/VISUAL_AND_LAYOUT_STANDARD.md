# CPF 산출물 시각·레이아웃 표준 — Harness v2.3.0

## 1. 목적

시각 요소는 문서를 꾸미기 위해 넣지 않는다. 독자가 구조·선택·상태를 더 빨리 이해하고, 공식 제품 문서로서 정돈된 인상을 받을 때만 사용한다.

## 2. 정렬 Grid

- 본문 시작선, 표 제목, 표, 그림 제목, 그림 Caption은 같은 왼쪽 Grid를 사용한다.
- 1단 불릿은 4.5mm, 2단 불릿은 9mm에서 시작한다. 동일 Level의 들여쓰기가 달라지면 FAIL이다.
- 임의 Space/Tab/빈 문단으로 위치를 맞추지 않는다.

## 3. 표

- 모든 표는 `표 <장번호>-<순번>. 제목`과 55자 이하 목적문을 가진다.
- 일반 표는 4열 이하, Landscape에서도 5열 이하를 원칙으로 한다.
- 셀은 한 문장, 권장 45자 이내다. 70자를 넘거나 4줄을 넘으면 표가 아니라 본문/불릿으로 이동한다.
- 설명·역할·사용법·원인·조치는 좌측 정렬한다. 코드/숫자/짧은 상태 외 중앙 정렬을 금지한다.
- 균등폭을 일률적으로 금지하거나 강제하지 않는다. 대칭 비교는 균등폭을 허용하고, 내용 역할과 예상 길이가 다르면 비례 폭을 지정한다.
- 긴 API/Class가 폭 때문에 2줄로 깨지면 표 설계를 다시 한다.
- 표가 연속되면 표 사이 12pt 이상을 확보하고 각각 제목/목적을 둔다.

## 4. 그림

- README는 Hero와 CPF 전체 Architecture Map이 필수다.
- 같은 박스-화살표 Template을 반복하지 않는다. 동일 Layout은 전체에서 최대 2회다.
- Architecture Map, Split Compare, Recovery State, Batch Control/Execution, Lifecycle, Operations Trace, Capability Landscape, Decision Map 등 서로 다른 역할을 사용한다.
- 그림 안 문장 금지. Node는 1~3단어, Arrow Label은 1~2단어를 원칙으로 한다.
- 글자 겹침, Box Overflow, Crop, 깨진 Glyph는 0건이어야 한다.
- 그림이 없어도 이해가 같거나 더 빠르면 그림을 넣지 않는다.

## 5. 최종 Visual QA

- DOCX 11개를 최종 PDF로 만들고 전 페이지를 렌더한다.
- 전체 페이지를 빠르게 보되, 표가 넓은 페이지·그림 페이지·첫/마지막 페이지는 100% 크기로 본다. 그림 Label은 200%에서도 확인한다.
- Contact Sheet만 보고 PASS하지 않는다.
- 정렬 Grid, 들여쓰기, 표 제목, Column 폭, 자동개행, 그림 다양성, 페이지 밀도를 사람이 최종 확인한다.


## 6. 제목 계층과 여백

- H1은 `1.`, `2.` 형식, H2는 `1.1`, `1.2` 형식으로 번호를 표시한다. H3는 Profile이 허용할 때만 `1)`, `2)` 형식을 사용한다.
- H1 시작 전에는 앞 블록과 명확히 분리되는 충분한 상단 여백을 둔다. H2/H3는 그보다 작지만 계층이 눈에 보일 정도로 차등 여백을 둔다.
- H1/H2/H3/본문/표/그림을 같은 간격으로 기계적으로 붙이지 않는다. 빈 문단으로 여백을 만들지 않고 Style spacing을 사용한다.
- 큰 내용 블록이 바뀌는 지점은 한눈에 구분되어야 하며, 페이지를 과도하게 비우는 방식도 금지한다.

## 7. 그림과 설명의 조화

- 그림은 구조·흐름·비교를 먼저 보여주고, 바로 아래에 1~2문장·120자 이내의 간결한 한국어 설명을 둔다.
- 그림 안 Label은 배경과 최소 4.5:1 대비를 확보한다. 이해에 필요한 회색/보조 Label도 동일 기준을 적용한다.
- 저대비 Text, 작은 회색 글씨, 장문 문장을 그림 안에 숨기는 방식은 FAIL이다.
- Gateway 선택/미선택 비교 그림은 외부 경계를 비교하되 내부 Domain↔Domain 호출을 Gateway 경유로 그리지 않는다.

## 8. PDF 한글 호환성

- PDF의 한글 Font는 최종 PDF에 실제 임베딩되어야 한다. Font substitution에 의존하면 FAIL이다.
- 최소 2개의 독립 PDF 렌더러에서 전 페이지를 렌더하고 한글 Glyph를 확인한다. 네모(□), tofu, replacement glyph, 깨진 자모가 1건이라도 있으면 FAIL이다.
- PDF가 단순히 열리는지만 확인해서 PASS하지 않는다.


## 9. Figure Geometry와 시각적 균형

- Figure의 Text Bounding Box는 Node/Container 경계를 침범하지 않는다. 내부 여백은 최소 18px, 권장 24px이다.
- Label 간 최소 간격은 20px, Node 간 최소 간격은 24px, Label과 Connector 간 여유는 최소 12px이다.
- Group/Container 제목은 최소 44px의 별도 Title Band에 둔다. `DATA / EXTERNAL` 같은 그룹 제목과 `Oracle`, `PostgreSQL`, `MariaDB` 같은 내부 Label을 같은 공간에 겹쳐 놓지 않는다.
- 병렬 Label은 동일 baseline, 동일 font size, 일관된 정렬축과 간격을 사용한다.
- Connector는 Text/Label을 가로지르지 않으며 Endpoint 외에는 Node 내부를 관통하지 않는다.
- 글자가 Box에 맞지 않으면 Font를 억지로 줄이지 말고 문구·Box·Layout을 다시 설계한다.
- 모든 Figure는 좌우·상하 정보량과 whitespace의 시각적 무게가 균형적이어야 한다. 한쪽 과밀·반대쪽 과공백은 재배치한다.

## 10. 페이지 전체 Balance

- README, DOCX, PDF 모든 페이지에서 제목·본문·불릿·표·그림·Callout의 시각적 무게와 여백을 함께 본다.
- 페이지 한쪽 또는 한 모서리에 내용이 몰리고 다른 영역이 의도 없이 비는 Layout은 FAIL이다.
- Pagination 때문에 작은 내용 블록 하나만 고립되거나 큰 dead space가 생기면 Page Break/Keep-with-next/Object Anchor를 재설계한다. 빈 문단으로 억지 균형을 맞추지 않는다.
- 같은 문서 안에서도 정보량이 다른 페이지를 기계적으로 같은 밀도로 채우지 않는다. 중요한 정보 위계와 읽는 흐름에 맞게 균형을 잡는다.
- 최종 판정은 전페이지 실제 Render와 모든 Figure 100%/200% 확인으로 한다.

## 11. 복잡한 내용의 표현 선택

- 거래 호출, Transaction, UNKNOWN/Reconcile, Batch 복구, Gateway 선택, 운영 판단처럼 관계와 선택이 있는 내용은 장문 Text만 연속 배치하지 않는다.
- 독자가 비교해야 하면 Decision Table/Split Compare, 흐름이면 Flow/Sequence, 상태 전이면 State/Recovery Map, 구조면 Architecture를 사용한다.
- 시각화 바로 아래에는 1~2문장의 짧은 한국어 설명을 두고, 그림의 Label을 다시 장문으로 반복하지 않는다.
- 페이지 안에서 동일 크기·동일 밀도의 Text Block이 3개 이상 연속되어 핵심이 묻히면 재구성한다.

## 12. 경로/목차/가비지 Gate

- 사용자 Windows Root를 포함한 최종 절대경로 150자 초과는 Hard Fail이다. ZIP entry와 Evidence도 동일하다.
- 고정/자동 목차는 실제 H1/H2 항목이 보이는 상태여야 한다. 빈 TOC 페이지 또는 목차 마지막 1~3항목만 고립된 페이지는 재배치한다.
- 현행 Harness 한 세트 외 과거 Harness/백업/해제본/세션 파일을 Repository에 남기지 않는다.


## 13. 부제목 Content Rail과 세로 리듬

- README 포함 모든 공식 문서에서 H2/H3는 제목 Grid에 두고, 그 아래 본문·불릿·Callout·Figure 설명은 4.5mm 안쪽의 공통 Content Rail에 맞춘다.
- H2 아래 첫 내용 간격은 6pt 이하, H3는 5pt 이하를 기본 Gate로 하며 다음 부제목 전 여백은 이보다 크게 둔다.
- 제목과 첫 내용이 하나의 의미 블록으로 읽혀야 한다. 같은 시작선에 평평하게 놓여 계층이 약해지거나 제목-내용이 과도하게 떨어지면 재배치한다.
- 넓은 표/그림만 full-width 예외를 허용하며 이후 본문은 즉시 Content Rail로 복귀한다.
- Markdown README는 공백 4칸으로 들여써 code block을 만들지 않는다. bullet/number/callout/figure-caption grouping을 사용한다.

## 14. Marker 사용

- 핵심 포인트, 선택 기준, 주의사항, 복구 포인트, 체크포인트처럼 짧은 병렬 정보는 필요할 때 `•` 또는 번호 marker를 사용한다.
- 순서가 있으면 번호, 순서가 없으면 bullet을 사용한다.
- 모든 문장을 bullet로 만드는 장식성 남발은 금지한다. 하나의 설명은 문단으로 유지한다.

## 15. Figure와 설명의 귀속

- `그림 제목 → Figure → 1~2문장 설명`을 하나의 시각 블록으로 구성한다.
- 설명과 Figure 사이 간격은 6pt 이하, 다음 H1/H2와의 간격은 최소 14pt를 권장한다. 설명이 다음 섹션에 더 가까워 보이면 FAIL이다.
- 설명은 어느 Figure의 의미를 고정하는지 즉시 알아야 하며, Figure 밖 별도 장문 문단처럼 떠 있지 않아야 한다.

## 16. 현대적 Visual Grammar

- `Rounded Rectangle + Arrow`는 기본 Template이 아니다. 방향성이 실제로 있을 때만 Arrow를 사용한다.
- 전체 Architecture는 Layer/Plane/Ownership Zone, 호출/거래는 Lane/Route, 상태/복구는 Ring/Branch, Batch는 Control Plane/Execution Lane, DB3는 Lifecycle Spine/Vendor Band, Capability는 Mosaic/Cluster, Gateway는 Split Boundary를 우선 검토한다.
- README에서 5개 이상 Visual을 쓸 경우 최소 4개의 서로 다른 Visual Grammar를 사용하고 Rounded Rectangle+Arrow chain은 최대 1개만 허용한다.
- Canvas 외곽 safe margin 48px, Node 내부 24px(권장 32px), Label 간 24px, Node 간 28px, Label↔Connector 16px를 확보한다.
- README가 다크 배경이면 주요 Visual은 밝은 neutral/light-tint canvas 또는 밝은 focal panel로 분리한다. 다크 배경 위 다크 Visual이 묻히면 FAIL이다.

## 17. 링크 형식과 점진 개선

- 일반 사용자 Navigation은 `[PDF]`만 노출하고 실제 `.pdf` Target이 존재해야 한다. `.docx`는 편집·보관용 최종 ZIP에는 포함하지만 README/산출물목록/사용자 탐색 링크에는 노출하지 않는다.
- 산출물 개선은 USER_APPROVED 또는 VISUAL_QA_APPROVED 영역을 기준으로 `PATCH_FIRST`로 수행한다. 새 Finding과 영향 범위만 보정하고 관계없는 PASS 영역을 전면 재작성하지 않는다.
- 변경 전/후 렌더를 비교해 이전에 좋았던 Visual·간격·표·링크가 나빠진 회귀가 0건인지 확인한다.


## v2 Render Acceptance

- 전페이지 Render를 실제 페이지 크기로 확인한다. Contact Sheet는 탐색용이며 PASS 근거가 아니다.
- 각 변경 페이지는 `quality-acceptance.json`의 12개 Manual Visual Dimension을 1~5점으로 평가한다.
- 모든 항목 4 이상, 평균 4.4 이상, Critical Finding 0이어야 `HUMAN_VISUAL_PASS`다.
- 승인 기준선이 있으면 Before/After를 동일 배율로 비교한다. 한 항목이라도 시각적으로 퇴행하면 `REGRESSION_FAIL`.
- 새 Visual은 먼저 기존 Visual Patch 가능성을 검토한다. 교체할 때만 Visual Brief를 작성한다.
- 문서별 임의 spacing/indent/font는 금지하고 Component System과 Design Token만 사용한다.


## v2.3.0 독자 니즈·실행형 매뉴얼 강제 규칙

- 모든 산출물은 `누가 → 왜 연다 → 무엇을 결정/개발/운영한다 → 완료 기준`을 먼저 정의한다. 파일/기능 분류를 위한 메뉴는 금지한다.
- Framework/Batch Developer Guide의 주요 기능 장은 `Task Summary → Public API → Option/기본값 → 선택 기준 → 실패·복구 → 최소 예 → 검증 → Source`를 첫 스캔에서 찾을 수 있어야 한다. 설명문만 있는 장은 FAIL이다.
- API/옵션/선택표가 핵심이고 Portrait에서 token 개행·과밀이 발생하면 Landscape를 사용한다. 문서 방향보다 독자의 스캔 속도를 우선한다.
- 일반 사용자 Navigation은 PDF만 제공한다. DOCX는 최종 ZIP에 포함하지만 README/산출물목록의 바로 열기 링크로 노출하지 않는다.
- Figure 설명 앞에 `그림 해석`, `그림 설명` 같은 중간 라벨을 두지 않는다. Figure 바로 아래 1~2문장으로 의미를 자연스럽게 고정한다.
- 모든 Product Figure는 geometry manifest를 가진다. 실제 canvas/frame/node/text/annotation bounding box가 safe area를 침범하거나 서로 겹치면 자동 Gate에서 FAIL해야 한다.
- Markdown Viewer의 Host Background는 README가 제어할 수 없다. 대신 CPF가 소유하는 Hero/주요 Content Surface는 Dark Brochure로 분명히 만들고 그 위 주요 Visual은 light/neutral canvas로 분리한다.


## v2.3.0 표현 선택·표·제목·여백 추가 규칙

- **표로 표현해야 하는 것을 표로 표현한다.** 행과 열의 교차점이 실제 비교·선택·상태·규격 정보를 가질 때만 표를 사용한다. 목차·찾아보기·단순 메뉴·순차 절차·한 문장 설명·`이 장을 보는 이유`·Golden Path 단일 메시지는 표로 만들지 않는다.
- 표 Column 폭은 일률 균등/불균등으로 고정하지 않는다. **내용의 역할·실제 길이·비교 대칭성**으로 결정한다. Vendor/Topology처럼 동일 축의 대칭 비교는 균등폭을 허용하고, 짧은 ID/상태와 긴 설명이 섞이면 설명 Column을 넓힌다.
- 고정 `50:50`은 대칭 비교가 아닌 한 금지한다. 짧은 Label 하나 때문에 페이지 절반을 비우지 않는다.
- **모든 표 Header는 한 줄 필수**다. Wrap 시 `문구 축약 → Column 재배분 → 표 폭/여백 조정 → Landscape` 순으로 해결한다. 과도한 Font 축소로 맞추지 않는다.
- 대제목/H1은 가능한 한 한 줄에 넣는다. 마지막 1~2단어가 다음 줄로 고립되는 자동 개행은 Visual FAIL이다.
- 대메뉴/H1 시작 전에는 일반 단락 전환보다 **한 줄 안팎의 추가 호흡 공간**을 둔다. H2/H3·표·Callout·코드·Figure 등 의미 단위가 바뀔 때도 붙어 보이지 않게 차등 간격을 둔다. 제목 아래 첫 내용은 전환 간격보다 작게 유지한다.
- Figure는 원본 PNG geometry뿐 아니라 README/DOCX/PDF에 삽입된 최종 크기에서 제목·Node·Stroke·Label이 경계를 침범하지 않는지 확인한다.
- Connector가 중앙 Hub/Junction을 암시하는 Figure는 중앙 의미 Node/Label 또는 완결된 연결 구조를 가져야 한다. 빈 중앙 때문에 누락된 그림처럼 보이면 재설계한다.
- 최종 Manual Gate는 `내용에 맞는 Component를 골랐는가`, `표 Column 비율이 실제 데이터에 맞는가`, `페이지 전환에 숨 쉴 공간이 있는가`까지 평가한다.
