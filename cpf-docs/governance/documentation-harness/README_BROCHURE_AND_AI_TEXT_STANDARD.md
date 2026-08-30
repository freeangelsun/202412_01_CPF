# README Brochure & AI Text Standard — v2.15.4

README는 단순 Reference 문서가 아니라 **CPF의 첫 인상을 만드는 다크 브로셔형 제품 간판**이다. 동시에 이미지에 의존하지 않고 AI·검색·텍스트 독자가 내용을 이해할 수 있어야 한다.

## 1. 브로셔형 구조

- README 목차는 두지 않는다.
- 최상단은 CPF 제품명, 한 줄 정의, 짧은 보조 설명, Hero Visual로 시작한다.
- 첫 화면에서 `무엇인가 / 어디에 쓰는가 / 어떤 범위를 표준화하는가`가 파악돼야 한다.
- 이후 `전체 구조 → 개발 흐름 → 거래/실패/복구 → Batch/연계/운영 → 사용 시작 → 상세 문서`가 독자 흐름에 맞게 이어진다. 필요한 Coverage가 많으면 H2를 추가할 수 있으며 **H2 상한은 없다.**
- `핵심 장점`, `왜 좋은가`, `CPF를 적용하면 무엇이 달라지는가` 같은 장점 전용 Section을 만들지 않는다. 기능과 동작을 설명하면서 결과가 자연스럽게 보이게 한다.
- 전체 README의 파일 크기·문자수·줄수·H2 수·Figure 수 상한은 없다. 필요한 Source-backed Coverage를 길이 때문에 삭제하지 않는다.

## 2. 시각 리듬

- CPF가 소유하는 Hero/주요 Panel은 다크 브로셔 Surface를 유지한다. Viewer 자체 Theme는 제어한다고 주장하지 않는다.
- 다크 Surface 위 핵심 Figure는 light/neutral canvas 또는 충분히 밝은 focal surface로 분리한다. dark-on-dark는 FAIL이다.
- `긴 본문 → 긴 본문 → 표 → 표`처럼 단조로운 흐름을 피한다. 짧은 문장, Bullet, Figure, 짧은 설명을 의미에 따라 교차 배치한다.
- 표는 비교가 꼭 필요한 곳만 사용한다. README를 Matrix/Reference Sheet처럼 만들지 않는다.
- Visual은 총개수 상한이 없지만 의미 없는 장식/중복 Figure는 추가하지 않는다. 5개 이상 Figure가 있으면 최소 4개의 서로 다른 시각 문법을 사용하는 기존 기준을 유지한다.

## 3. AI/텍스트 대응 — 모든 의미 Figure 필수

각 의미 Figure는 세 층의 설명을 가진다.

1. **Alt Text:** 비어 있지 않고 파일명이 아닌 의미 설명.
2. **Figure 내부:** 핵심 Node/관계만 짧게 표시. 긴 문장 금지.
3. **Visible Korean Companion:** Figure 바로 아래 1~2문장의 간략 한글 설명.

Visible Korean Companion은 그림을 못 보더라도 다음을 이해할 수 있어야 한다.

- 무엇을 보여주는가
- 핵심 관계/흐름이 무엇인가
- 독자가 무엇을 기억해야 하는가

`그림 설명`, `그림 해석` 같은 라벨을 붙이지 않고 자연스러운 문장으로 이어 쓴다. Figure와 다음 Heading 사이에서 **Figure에 더 가깝게** 배치한다.

## 4. 장점 표현

CPF의 강점은 숨기지 않는다. 다만 광고성 장으로 따로 모으지 않는다.

예: Same JVM/Remote 호출 계약을 설명하는 문장에서 배포 형태가 바뀌어도 업무 호출 코드를 다시 만드는 범위를 줄인다는 결과를 자연스럽게 연결한다. UNKNOWN/Reconcile을 설명하면서 결과 불명 상태를 성공/실패로 오판하지 않고 복구 가능하게 남긴다는 효과를 연결한다.

Source로 확인 가능한 가치가 충분한데 README에서 이를 지나치게 약하게 표현하면 `README_NATURAL_VALUE_PASS` 실패다.

## 5. 최종 FAIL 조건

- Hero/브로셔 첫 화면이 없음
- README가 표/장문 위주의 Reference Sheet처럼 보임
- 의미 Figure Alt Text 누락
- 의미 Figure 바로 아래 간략 한글 설명 누락
- Figure 설명이 다음 Section에 더 가까움
- 장점 전용 홍보 Heading/표 사용
- Coverage를 길이 때문에 삭제
- 다크 Surface와 Figure가 묻힘
- 동일 Visual Grammar 반복
## 제품 설명 충분성
Brochure는 얇게 쓰라는 뜻이 아니다. 전체 Architecture와 핵심 기능·개발·운영을 충분히 설명한 뒤 시각적으로 읽기 쉽게 구성한다. 설명을 줄여 간판처럼 보이게 만드는 것은 금지한다.
## 6. Rendered Brochure Hard Gate

README의 Markdown 구조 검사와 제품 설명 충분성 PASS만으로 브로셔 품질을 선언하지 않는다. 최종 README SHA에 대해 900/1200/1440px 전체 Render를 각각 생성하고 다음을 모두 수동 검수한다.

- 첫 Viewport에서 Hero, 제품 정의, 시각적 focal point가 보이고 긴 본문이 화면을 지배하지 않아야 한다.
- H2 전환마다 시각적 경계와 충분한 여백이 보여야 하며, 긴 본문이 화면 단위로 연속되는 Text Wall은 0건이어야 한다.
- 기존 승인 Visual은 파일 존재가 아니라 README에서 역할·위치·가독성이 유지되어야 한다. Hero/Architecture/Invocation/Transaction/Batch/Development/Capabilities/Gateway/Operations 핵심 Visual 역할이 사라지면 FAIL이다.
- Architecture 설명은 Figure 이름 나열이 아니라 Owner, 호출, Gateway 선택, Runtime/DB 경계를 읽은 뒤 독자가 구조를 설명할 수 있는 수준이어야 한다.
- Human Review Evidence는 현재 README SHA, 각 Render Screenshot SHA, width, Scan/Detail 결과를 기록한다. 하나라도 stale이면 FAIL이다.

`README_RENDERED_BROCHURE_REVIEW_PASS`가 PASS하지 않으면 자동 README Validator가 모두 PASS해도 Final Acceptance는 FAIL이다.


## 2.15.4 Large-product README completeness reinforcement

- CPF처럼 기능 범위가 큰 제품의 Root README를 얇은 개요로 축약하지 않는다. 제품 대표 문서에서 실제 Source로 확인되는 주요 Capability Family를 충분히 설명한다.
- 최소 Coverage는 Web/Application/Common, Data/Transaction/Cache/Lock/Session, Integration/Protocol/Resilience, Messaging/Async/Schema, File/Object Storage/Archive/Tabular, Security/Crypto/Secret/Identity, Config/Feature Flag/Health/Observability, AI/Realtime/GraphQL, Batch Runtime, DB3/Generator/Operations를 포함한다.
- 기능명만 나열하면 충족으로 인정하지 않는다. 독자가 무엇을 선택하는지, 실패 또는 운영 경계가 무엇인지, 어떤 결과를 얻는지 설명 흐름 안에서 확인할 수 있어야 한다.
- 정보량을 늘릴 때 장문 Text Wall로 바꾸는 것도 FAIL이다. 900/1200/1440 Rendered Brochure Review에서 Figure, 짧은 설명, 의미 그룹, 충분한 Section Boundary가 유지되어야 한다.
- 사용자 Finding으로 “대표 README가 제품 규모에 비해 내용이 부족하다”가 발생하면 이전 Completeness/Visual PASS는 즉시 무효화하고 현재 README SHA로 전체 Gate를 재실행한다.

## 7. Render Evidence Integrity

- 900/1200/1440 Screenshot은 `PIL.Image.verify()`에 준하는 전체 PNG 무결성 검사를 통과해야 한다. Width/Height Header 조회만으로 Rendered Brochure PASS를 선언할 수 없다.
- Screenshot SHA는 무결성 검사를 통과한 최종 바이트에 대해 기록한다. 손상 파일의 SHA가 Review JSON과 일치하더라도 FAIL이다.
- `README_PREVIEW.html`은 repository-relative Base/Asset 경로를 사용해 Package를 다른 PC/폴더에 적용한 뒤에도 동일 Visual을 재생성할 수 있어야 한다.

