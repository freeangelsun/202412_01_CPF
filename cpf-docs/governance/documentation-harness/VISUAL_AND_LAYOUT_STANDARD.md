# CPF 산출물 시각·레이아웃 표준 v1.2

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
- 균등폭 금지. 내용 의미와 예상 길이로 비율을 지정한다.
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
