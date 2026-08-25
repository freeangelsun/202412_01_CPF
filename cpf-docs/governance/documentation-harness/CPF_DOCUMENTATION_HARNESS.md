# CPF 공식 산출물 작성 하네스

## 0. 문서 상태

- Harness Version: `1.0.0`
- 생성 기준 Source: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260825_104217.zip`
- Source SHA-256: `014a313a55c1852ac6131103668b3fe53e31a390e23ae7105aed97158977bad9`
- 변경 권한: **사용자의 명시적 하네스 수정 요청만 허용**
- 자동 수정: **금지**
- 적용 범위: Root `README.md` + 공식 DOCX 11개 + 공식 PDF 11개

이 하네스는 CPF 공식 산출물의 **내용 구조, 설명 방법, 문장 형식, 목차, 표, 그림, 코드 예시, Source 근거, Visual QA, Accessibility, PDF Preflight, Packaging을 하나의 실행 규격으로 잠근다.** 산출물 작성자는 이 하네스의 빈칸을 채우는 역할을 하며 목차와 표준을 다시 설계하지 않는다.

## 1. 최상위 불변 규칙

1. 모든 공식 문서는 목차가 있어야 한다. README도 Hero 바로 아래에 보이는 수동 목차를 둔다.
2. H1/H2 이름과 순서는 `profiles/*.json`이 정본이다. 작성자가 추가·삭제·개명·순서 변경하지 않는다.
3. 새로운 Source 기능이 Profile에 없으면 작성자가 임의 장을 추가하지 않는다. `HARNESS_CHANGE_REQUIRED`로 기록하고 사용자의 하네스 변경 요청을 기다린다.
4. 하네스는 Source가 바뀌었다는 이유로 자동 변경하지 않는다.
5. Source의 API/CLI/Class/Property/Route/DB Object는 매 작업 세션 최신 Source에서 다시 확인한다. 하네스는 존재하지 않는 식별자를 만들어내는 근거가 아니다.
6. 과거 Documentation ZIP/Evidence/PASS는 현재 Source의 PASS로 승계하지 않는다.
7. 모든 문서 작성 완료 후 Semantic, Cross-document, Visual, Accessibility, PDF Preflight, Link, Unicode/NFC, Package Integrity를 다시 검사한 뒤 부족함이 없을 때만 완료다.
8. Contact Sheet만 보고 Visual PASS하지 않는다.
9. 코드성 짧은 값이 아니면 표 본문을 가운데 정렬하지 않는다.
10. 표를 열 개수로 균등 분할하지 않는다. 내용 길이와 역할에 따라 `table-presets.json` 비율을 사용하고 Word `tblGrid`와 실제 Cell Width를 일치시킨다.
11. 표/그림/글자/Caption의 잘림·겹침·깨짐은 허용 0건이다.
12. 별도 `LICENSE` 파일은 만들거나 찾거나 링크하지 않는다. 사용자 문서에는 필요한 경우 `Community & Evaluation License`만 간략히 설명한다.

## 2. 작업 세션 시작 순서

1. `HARNESS_LOCK.json` Hash 검증
2. 최신 Source ZIP 또는 Working Tree Identity와 SHA-256 확인
3. `CPF_FINAL_TARGET_REQUIREMENTS.md` 확인
4. 최신 사용자 Steering 확인
5. `scope.json`으로 공식 산출물 Scope 고정
6. 대상 `profiles/<DOCUMENT>.json` 로드
7. `product-coverage.json`과 실제 Source Inventory 비교
8. Module/Public API/SPI/CLI/Config/DB/OpenAPI/Consumer를 실제 Source에서 Inventory
9. Source와 Harness 불일치 분류
10. 불일치가 없을 때 작성 시작

Harness 변경이 필요한 불일치는 문서 작성자가 우회하지 않는다.

## 3. 페이지와 여백

- A4 Portrait: 상 `10mm`, 하 `10mm`, 좌 `11mm`, 우 `11mm`
- A4 Landscape: 상·하·좌·우 `10mm`
- Header distance `5.5mm`, Footer distance `6.5mm`
- 빈 문단으로 여백을 만들지 않는다. Style spacing만 사용한다.
- H1은 앞 `24pt`, 뒤 `9pt`. H2는 앞 `16pt`, 뒤 `7pt`. H3는 앞 `11pt`, 뒤 `5pt`.
- H1은 이전 표/그림과 최소 `18pt`의 시각적 분리를 확보한다.
- H1+첫 본문/표/그림을 놓을 남은 공간이 `55mm` 미만이면 다음 페이지에서 시작한다.
- 충분한 공간이 있는데 모든 H1을 무조건 새 페이지에서 시작하는 것은 금지한다.
- 빈 페이지, 제목만 남은 페이지, 표 Header/한 행만 떨어진 페이지는 FAIL이다.

## 4. 글꼴과 본문

- 본문: `Noto Sans KR`, `10.5pt`, 1.22배 줄간격, 좌측 정렬
- 제목: 같은 Family. 문서 제목 `26pt`, H1 `20pt`, H2 `15pt`, H3 `12.5pt`
- 표: Header `9.5pt`, 본문 `9.3pt`
- Code: `Consolas 9pt`
- Caption: `9pt`
- 모든 읽기 텍스트 최소 `9pt`. 그보다 작아져야 들어가는 표/그림은 재설계한다.
- 본문 가운데 정렬과 양쪽 맞춤은 금지한다.

## 5. 문장 작성 규격

- 사용자용 설명은 `합니다/됩니다` 체를 기본으로 한다.
- 기술표준서 규칙은 `필수/금지/권장/허용`으로만 판정한다.
- 누가 행동하는지 중요한 문장은 `CPF가`, `개발자는`, `운영자는`, `Worker가`처럼 Actor를 명시한다.
- 한 문장에 주요 생각 하나만 둔다. 한글 기준 약 90자를 넘으면 분리 검토하고 140자를 넘으면 반드시 재검토한다.
- 조건은 명령보다 먼저 쓴다. 예: “UNKNOWN 상태이면 재호출하기 전에 Reconcile 대상인지 확인합니다.”
- 한 문단은 2~4문장을 기본으로 하고 첫 문장에 해당 문단의 핵심을 둔다.
- 7개가 넘는 병렬 항목은 의미 그룹으로 묶거나 구조화된 표로 바꾼다.
- `혁신적`, `최고의`, `엔터프라이즈급`, `생산성 극대화`, `차세대`, `완벽한` 같은 모호한 홍보 표현을 사용하지 않는다.
- `기반 기술`, `핵심 해석`, `이 구조의 장점`, `왜 좋은가`, `개발자에게 좋은 점` 같은 어필 Label을 사용하지 않는다.

## 6. 장점을 설명하는 방법

장점 전용 장을 만들지 않는다. 각 기능·구조를 설명하는 과정에서 아래 순서로 자연스럽게 드러낸다.

1. 사용자가 마주치는 실제 상황 또는 선택
2. CPF가 제공하는 계약/자동화/경계
3. 개발자나 운영자가 직접 하지 않아도 되는 일 또는 새로 할 수 있게 되는 일

README의 H1 제목 자체도 기능 이름만 쓰지 않고 동작과 편의가 동시에 드러나게 한다. 예를 들어 `Domain Invocation` 대신 `업무 호출은 배포 위치가 바뀌어도 같은 계약을 유지합니다`처럼 작성한다.

## 7. 표 규격

- Header만 가운데 정렬한다.
- 본문은 좌측 정렬이 기본이다.
- 가운데 정렬 허용: 순번, Y/N, Boolean, 매우 짧은 상태, 매우 짧은 Code, 짧은 숫자.
- 설명, 역할, 이유, 명령, 경로, API 설명, 완료 기준, 장애 원인, 조치, Owner 설명은 가운데 정렬 금지다.
- 균등폭 금지. `table-presets.json`의 비율을 사용한다.
- 표의 `tblGrid`와 Cell Width를 같은 비율로 기록한다.
- 짧은 코드/옵션/상태가 열이 좁아 2줄 이상으로 자동 개행되면 FAIL이다.
- 해결 순서: 문구 단축 → 폭 재배분 → 부가 설명을 표 아래로 이동 → 의미별 표 분리 → Profile 허용 범위의 Landscape → 최후에 Font 9pt까지 조정.
- 데이터 표에서 Merge/Split/Nested Table은 금지한다.
- 2페이지 이상 표는 Header 반복을 켜고 행이 페이지 사이에서 쪼개지지 않도록 한다.
- 셀의 여러 짧은 값은 `·` 또는 `|`로 한 줄 표시한다. 4개 이상이거나 길면 `○` mini-list를 쓴다. 장식 목적 수동 줄바꿈은 금지한다.

## 8. 그림/이미지 규격

- 설명용 그림은 `figure-presets.json`에 정의된 유형만 사용한다.
- README 핵심 Visual은 SVG를 우선하고 Raster가 필요하면 최소 1600×900을 사용한다. Hero는 최소 1920×720.
- README Visual의 텍스트는 최소 28px, DOCX/PDF 그림 내부 텍스트는 최종 크기 기준 최소 10pt.
- Node Label 최대 3단어, Arrow Label 최대 2단어. 문장 전체를 그림 안에 넣지 않는다.
- Text Overflow, Box Overflow, Crop, Overlap, Broken Glyph는 각각 허용 0건.
- Informative Image에는 Alt Text가 필수다.
- 모든 핵심 이미지 바로 아래에 2~4문장의 설명을 둔다. 그림을 못 보는 사용자와 AI Review도 그 설명만으로 의미를 이해해야 한다.
- 설명은 그림 제목 반복이 아니라 흐름/경계 → CPF 자동 처리 → 사용자 편의를 순서대로 쓴다.

## 9. README 제작 규격

README는 제품 브로셔이면서 실제 첫 시작점이다. 별도의 “장점” 장을 만들지 않는다. 전체 README가 기능 설명과 함께 장점을 자연스럽게 보여준다.

- Hero + 보이는 목차 필수
- Hero 포함 핵심 Visual 최소 14개, 권장 17개, 최대 20개
- Dark Brochure Visual System을 일관되게 사용한다.
- GitHub에서 지원하지 않는 CSS로 전체 배경을 강제하지 않는다. 대신 Hero와 핵심 장표를 동일 Dark Design System으로 제작하고 Light/Dark Theme 모두에서 본문이 읽혀야 한다.
- 핵심 장표는 `profiles/README.json` 순서를 그대로 따른다.
- 긴 API Reference, 긴 Source Tree, QA 상태, 개발 진행상태, 내부 Release 용어 반복은 금지한다.
- Bootstrap/Domain 생성/Build/Test/Stop/Reset은 실제 Source CLI를 재확인해 간략히 설명한다.
- License는 마지막에 `Community & Evaluation License`만 간략히 설명하고 별도 License 파일 링크를 만들지 않는다.

README 고정 H1 순서는 `profiles/README.json`이 정본이다. 작성자는 장을 합치거나 새 장을 삽입하지 않는다.

## 10. 개발자 가이드 제작 규격

개발자 문서의 주요 기능 장은 항상 다음 순서다.

`언제 보는가 → 기능 선택표 → Public API/Annotation/Starter → 옵션 → 최소 예제 → Runtime 동작 → 오류/경계/복구 → Test/기대 결과 → 잘못된 사용 → Source/EDU`

- 장 첫 1페이지 안에 기능 선택표가 있어야 한다.
- API 이름만 나열하지 않는다. 개발자가 무엇을 고를지 설명한다.
- 옵션 표는 `옵션 | 타입 | 필수 여부 | 기본값 | 예시 | 설명` 순서 고정.
- 코드 예시는 L1~L3. L4는 Source/EDU 링크로 넘긴다.
- 기능 선택이 많은 장은 `profiles/FRAMEWORK_DEVELOPER_GUIDE.json`의 H2를 그대로 사용한다.

## 11. 운영 문서 제작 규격

운영 장은 항상 다음 순서다.

`증상/목적 → 사전 조건 → 확인 위치 → 확인 값 → 정상/비정상 판단 → 안전 조치 → 금지 조치 → Permission/Reason/Approval → Audit → 조치 결과 → 정상화 → Escalation`

메뉴/화면 필드 목록만 나열하는 방식은 금지한다. 운영자는 문서로 판단과 조치를 끝낼 수 있어야 한다.

## 12. Architecture 제작 규격

Architecture 장은 다음 순서다.

`책임 → Owner → Dependency → Boundary → 필수 그림 → 정상 흐름 → Failure Domain → Multi-instance/HA → 허용 → 금지 → 선택 기준 → Source Map`

그림 없이 Module 설명만 길게 쓰는 것은 FAIL이다. Architecture 그림은 장식이 아니라 Owner/Boundary/Direction을 설명해야 한다.

## 13. Specification 제작 규격

Specification은 소개서가 아니라 정확한 Contract Reference다.

`정의 → Owner/Boundary → Canonical Identifier → Type/Package → Input → Output → Config → 기본 동작 → 실패 계약 → Transaction/Concurrency/Idempotency → Consumer → Compatibility → Source → Acceptance`

Source에서 확인되지 않은 Identifier는 1건도 허용하지 않는다.

## 14. 기술표준 제작 규격

모든 표준 규칙은 다음 순서다.

`적용 범위 → Rule Summary → 필수 → 금지 → 권장 → 허용 → 올바른 예 → 잘못된 예 → 검사 → 예외 → Review Checklist`

`적절히`, `가능하면`, `상황에 따라`처럼 기준이 없는 문구를 규칙으로 사용하지 않는다.

## 15. DB 표준 제작 규격

모든 DB 주요 장은 다음 순서다.

`Canonical Rule → Oracle → PostgreSQL → MariaDB → Runtime Query 영향 → Migration → Upgrade → Rollback/Recovery → Performance/Lock → Generator Binding → Verification`

DDL 한 조각만 설명하고 DB 표준을 끝내지 않는다.

## 16. Product Coverage 강제

`product-coverage.json`의 모든 항목은 최소 하나의 Primary Document와 Secondary Document를 가진다. 각 작업 세션은 현재 Source에서 다음을 반드시 다시 Inventory한다.

- Module Owner / Public API / SPI / Internal
- Same JVM / Remote / Multi-instance / Domain Invocation
- System6 / operationId / instanceId
- Transaction / Remote Side Effect / Saga / TCC / XA
- Error / UNKNOWN / Reconcile / Idempotency
- Starter / Profile / Capability / Provider
- Generated Domain / Generator / Sync
- JDBC / MyBatis / JPA / Cache / Lock
- Oracle / PostgreSQL / MariaDB와 DB Lifecycle
- Gateway / External Integration / Fixed-Length / GraphQL / Realtime
- Messaging / File / Object Storage
- Security / Crypto / Approval / Audit
- Batch Runtime 전 영역
- ADM / OpenAPI / Frontend Consumer / Observability
- Config / Bootstrap / CLI / EDU / Build / Release
- cpf-common Code / Parameter / Message / Calendar / Template
- Data Quality / Archive / Tabular / Notification / AI Integration
- Feature Flag / Platform State / Runtime Control / Service Registry / Incident
- Crypto / Digital Signature / Masking / Sensitive Data Access

Coverage 누락이 있으면 문서를 임의 확장하지 않고 Harness Change Required인지 Source Gap인지 먼저 판정한다.

## 17. Visual QA 최종 Gate

문서를 만든 직후 다음 순서로 검수한다.

1. README 실제 Render
2. DOCX 11개를 PDF로 렌더
3. PDF 전 페이지 이미지 렌더
4. 전 페이지 빠른 육안검수
5. 첫 페이지/목차/그림/넓은 표/코드/페이지 경계/마지막 페이지를 원본 크기로 직접 검수
6. 고위험 그림은 200% 확대 검수
7. 발견 오류를 문서 전체 공통 원인으로 묶어 일괄 보정
8. DOCX 재생성
9. PDF 재생성
10. Visual 재검수

Hard Fail: 잘림 1건, 겹침 1건, 깨진 Glyph 1건, 표 균등폭 1건, 설명 셀 가운데 정렬 1건, 빈 페이지 1건, 제목 단독 페이지 1건, Broken Link/Image 1건.

## 18. Accessibility / PDF

- 논리적 Heading 순서를 사용하고 Heading Level을 건너뛰지 않는다.
- 표는 단순 구조, Header Row를 사용하고 장표용 Merge/Nested Table로 데이터를 표현하지 않는다.
- Informative Image에는 Alt Text를 작성한다.
- 본문은 좌측 정렬한다.
- 텍스트 대비 최소 4.5:1, 큰 텍스트와 의미 있는 Graphic은 최소 3:1을 지킨다.
- PDF 생성 후 Font/Glyph, Language, Reading Order, Table Header, Link, Warning/Error를 검사한다.
- 자동 Accessibility PASS가 Visual PASS를 대체하지 않는다.

## 19. 최종 완료

다음이 모두 PASS여야 한다.

`SEMANTIC_PASS + SOURCE_IDENTITY_PASS + CROSS_DOCUMENT_PASS + VISUAL_PASS + ACCESSIBILITY_PASS + PDF_PREFLIGHT_PASS + LINK_PASS + UNICODE_NFC_PASS + PACKAGE_INTEGRITY_PASS`

미실행은 PASS가 아니다. 일부만 PASS한 문서를 최종본이라고 부르지 않는다.

## 20. Harness 변경

하네스 파일은 사용자 요청에 의해서만 수정한다.

- Source가 바뀌어도 자동 변경 금지
- QA Finding만으로 자동 변경 금지
- 작성 AI의 선호로 자동 변경 금지
- “이 문서에는 이 제목이 더 좋아 보인다”는 이유로 변경 금지

사용자가 하네스 수정을 명시적으로 요청한 경우에만 Version을 올리고 `CHANGELOG.md`와 `HARNESS_LOCK.json`을 갱신한다.
