# CPF Documentation Anti-Pattern Catalog — v2.9.0

Harness 작성자가 반복 실수를 즉시 식별하도록 **실패 모양 → 올바른 방향**을 정리한다. 아래 항목은 하나라도 남으면 해당 Gate를 PASS할 수 없다.

## README

- **FAIL:** `CPF를 적용하면 무엇이 달라지는가`, `핵심 장점`, `왜 좋은가` 같은 홍보형 장점 Section.
  - **대신:** 구조·개발·실패복구·Batch·운영 설명 문장 안에서 실제 효과를 자연스럽게 연결.
- **FAIL:** Hero 없이 장문 본문/표로 시작.
  - **대신:** 제품명 + 한 줄 정의 + 짧은 설명 + Hero Visual의 브로셔 첫 화면.
- **FAIL:** 표·표·긴 문단·긴 문단이 연속되어 첫 스캔 지점이 없음.
  - **대신:** 짧은 문단, Bullet, Figure, 짧은 설명을 의미에 따라 교차 배치.
- **FAIL:** Figure 아래 한글 설명 없음, Alt Text가 비어 있음.
  - **대신:** 의미 있는 Alt Text + Figure 바로 아래 1~2문장 자연스러운 한글 설명.
- **FAIL:** README가 길어진다는 이유로 CPF 기능/효과를 빼버림.
  - **대신:** Section을 추가하고 Navigation/시각 리듬을 강화. 총 길이 상한 없음.

## 표

- **FAIL:** 3열/4열이라는 이유만으로 균등폭.
  - **대신:** 상태/ID/기본값은 좁게, 설명/선택/실패복구는 넓게. 실제 내용량+역할 기반.
- **FAIL:** Header 또는 `transactionId`, API, Path 같은 짧은 Token이 폭 부족으로 2줄.
  - **대신:** 폭 재배분 → Landscape → 의미 단위 Table 분할. Font 축소는 마지막 수단이며 가독성 저하 시 FAIL.
- **FAIL:** `누가 보는가 / 기준` 같은 도입부 Meta Table.
  - **대신:** 제목 아래 자연스러운 1~2문장 Lead.
- **FAIL:** 단일 메시지를 1행 Table로 포장.
  - **대신:** 본문, Bullet, Callout, Code 중 의미에 맞는 Component.

## 세로 여백과 페이지

- **FAIL:** H1/H2/H3, 본문, 표, Figure가 붙어 한 덩어리로 보임.
  - **대신:** 제목 위 여백 > 제목 아래 첫 내용 여백. 의미 Block 전환마다 차등 호흡.
- **FAIL:** 페이지 수를 줄이려고 Margin/Font/Line spacing을 축소.
  - **대신:** 페이지 증가 허용. 읽기 쉬운 여백을 유지.
- **FAIL:** 마지막 페이지에 제목+한 문장, 표 꼬리 1행만 남음.
  - **대신:** Keep-with-next/재배치/문단 분배로 의미 블록을 온전하게 유지.

## Figure

- **FAIL:** Arrow가 Box 안쪽을 침범하거나 Label/Text 위를 지나감.
  - **대신:** Connector는 Source/Target Boundary에서 시작·종료하고 unrelated Node/Text를 통과하지 않음.
- **FAIL:** 모든 그림이 같은 둥근 네모+화살표.
  - **대신:** Layer/Lane/State/Lifecycle/Timeline/Split/Mosaic 등 개념에 맞는 Grammar.
- **FAIL:** 원본 PNG만 예쁘면 PASS.
  - **대신:** README/DOCX/PDF 실제 삽입 크기에서 Crop/Contrast/Safe Area/Text Size를 다시 확인.

## 내용과 독자

- **FAIL:** 기능 이름은 많지만 독자가 실제로 어떻게 선택·실패대응·검증하는지 없음.
  - **대신:** Profile의 Reader Task Dimension을 실제로 끝낼 수 있게 작성.
- **FAIL:** Specification/How-to/Explanation을 한 Section에 무분별하게 섞어 과밀.
  - **대신:** 문서 Intent에 맞게 우선순위를 두고 필요한 Reference로 연결.
- **FAIL:** Harness/Source SHA/Build baseline을 사용자 본문에 표시.
  - **대신:** Evidence/Manifest에만 기록.

## AP-DELIVERY-WINDOWS-ROOT-PREFIX

- **금지:** Windows root containment 검사에서 separator를 문자열 literal로 중복 조립하여 정상 Root 상대경로를 `OUTSIDE ROOT`로 오판하는 구현.
- **필수:** `DirectorySeparatorChar`로 root prefix와 상대경로 separator를 구성하고 `GetFullPath` 결과를 root boundary와 비교한다.
- **Gate:** canonical `APPLY.ps1`/`DELETE_ONLY.ps1`에 `TrimEnd('` separator literal이 있거나 `DirectorySeparatorChar`/`$rootPrefix` 경계 검사가 없으면 Harness FAIL.



## Harness 2.9.0 Visual Quality Uplift

- 자동 Validator PASS보다 실제 사용자/육안 Finding을 우선한다. 표 Header 2줄, 답답한 문단 호흡, 저대비 Header, 저밀도 마지막 페이지가 보이면 자동 PASS라도 FAIL이다.
- 승인되거나 잘 된 현행본은 PATCH_FIRST로 보존하며 Finding 영향 밖의 구조·내용·Visual을 전면 재작성하지 않는다.
- README는 900/1200/1440px에서 대메뉴 전환 호흡과 Header 단일 행을 확인한다. 폭이 부족한 Markdown 표는 유지하지 않고 독자 흐름에 맞는 prose/list/card로 바꾼다.
- 진한 DOCX 표 Header는 텍스트 대비 4.5:1 이상이며 AUTO/검정 글자를 허용하지 않는다.
- 마지막 페이지의 제목+한 문장, Source-only, 표 꼬리 1~2행, 의미 없는 대형 공백은 금지한다. 페이지 수를 줄이기 위해 전체 Font/Margin/Line spacing을 축소하지 않고 내용·국소 배치를 보정한다.
- README Visual은 원본과 900/1200/1440px 삽입 Surface에서 text safe area·crop·boundary intrusion 0을 확인한다.
- Windows VS Code built-in Markdown Preview Runtime은 가능한 Windows 환경에서 별도 실행한다. 실행하지 못한 경우 미검증으로 기록한다.
