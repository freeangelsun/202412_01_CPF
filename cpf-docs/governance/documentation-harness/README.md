# CPF Documentation Harness v2.9.0

CPF 공식 README와 11개 DOCX/PDF를 일관된 상용 문서 품질로 만들기 위한 **실행형 Design/QA Harness**다.

## 최상위 원칙

- 사용자 지적은 산출물 한 곳만 고치지 않고 Harness 공통 규칙과 Negative Fixture/Validator에 먼저 반영한다.
- 기존 좋은 영역은 보존하는 `PATCH_FIRST`가 기본이다.
- 자동 PASS만으로 완료하지 않는다. 실제 README/DOCX/PDF Render와 독자 관점 검수가 필요하다.
- Table은 실제 행·열 데이터에만 사용한다. 문서 도입부의 독자/목적을 2열 Meta Table로 만들지 않는다.
- Harness version, Source SHA, build baseline은 사용자 문서 본문에 노출하지 않고 Evidence/Manifest에서만 관리한다.
- Figure Connector는 대상 Box 내부를 침범하지 않고 Boundary에서 끝난다.
- 원본 Figure뿐 아니라 README/DOCX/PDF 삽입 상태를 검증한다.
- Repository에는 현행 Harness 한 세트만 유지한다. 과거 version/history/backup/session 복제본을 남기지 않는다.

## Strict No-False-Green

- `validate_final_acceptance`가 모든 required Gate를 정확히 `PASS`로 확인하지 않으면 최종 PASS가 아니다.
- Manual/Hybrid Gate는 Evidence가 없으면 PASS할 수 없다.
- README의 장점 전용 Heading/Section, 과밀 문단, 연속 표/대형 Figure를 Hard Fail로 관리한다.
- DOCX Table은 비대칭 균등폭, Header/short-token wrap, semantic width inversion을 Hard Fail로 관리하고 최종 Render Evidence를 요구한다.
- H1/H2/H3/Body/Block 간 최소 세로 호흡을 수치화하고, 수치가 맞아도 실제 Render가 답답하면 Human Visual Gate에서 FAIL한다.
- Reader Task는 Keyword 존재가 아니라 선택·입력/옵션·정상 흐름·실패/복구·검증·Reference까지 실제 완결되는지 검수한다.

## 정본 파일

- `DOCUMENT_QUALITY_STANDARD.md` — 독자·Table·Heading·Figure·Accessibility·재발 방지 통합 품질 표준
- `DOCUMENT_DESIGN_PLAYBOOK.md` — 누구든 같은 화면 품질을 만들기 위한 페이지/표/Figure/검수 실행 규칙
- `INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md` — 문서 유형과 Persona/Reader Task 기반 정보 구조
- `README_BROCHURE_AND_AI_TEXT_STANDARD.md` — README 브로셔형 구성과 AI/텍스트 대응 한글 Figure 설명 규칙
- `AUTHORING_EXECUTION_PROTOCOL.md` — Source 확인부터 Clean Replay까지 고정 실행 순서
- `ANTI_PATTERN_CATALOG.md` — 실제 실패 모양과 올바른 대안의 재발방지 Catalog
- `MANUAL_REVIEW_SCORECARD.md` — Scan/Detail/Reader pass 수동 품질 판정표
- `CPF_DOCUMENTATION_HARNESS.md` — 전체 실행 원칙과 완료 조건
- `design-tokens.json` — 수치·Spacing·Geometry·Contrast 정본
- `component-system.json` — 공식 문서 Component
- `quality-acceptance.json` — 자동/수동/회귀/독자 품질 Stage
- `quality-fixtures.json` — 사용자 화면 결함의 재발을 막는 Negative Fixture
- `visual-qa.json` — Render/Visual Hard Fail
- `profiles/*.json` — 문서별 Coverage/Outcome
- `document-output-rules.json` — 파일/링크/Source ZIP/현행본 정책
- `REFERENCE_BASIS.md` — 외부 문서·접근성 참고 근거

## 작업 순서

1. 최신 Source와 공식 Artifact Inventory 확정
2. 사용자 Finding을 Harness Rule/Token/Component에 반영
3. Negative Fixture + Validator Assertion 추가
4. Harness self-validation PASS
5. Artifact를 Harness 기준으로 Patch
6. README/DOCX/PDF 실제 Render 전페이지 검증
7. Manual Visual + Reader Comprehension + Accessibility 검증
8. Before/After Regression + Click-through + Source-backed semantic 검증
9. Manifest/Hash/Delete Manifest 생성
10. Fresh Clean Replay 후 최종 ZIP 확정


## v2.9.0 강제 보강

- README와 모든 공식 DOCX/PDF에는 총 파일 크기·페이지·문자·단어·Section/Figure 수 상한을 두지 않는다.
- 국소 Density/Paragraph/Table Threshold는 **재구성 Trigger**이며 정보 삭제 근거가 아니다.
- 길이 때문에 Source-backed Coverage 또는 Reader Task를 줄이면 FAIL한다.
- README는 브로셔형 Hero/시각 Story를 유지하고 모든 의미 Figure에 Alt Text + 바로 아래 간략 한글 설명을 제공한다.
- 작성자는 `DOCUMENT_DESIGN_PLAYBOOK.md`, `INFORMATION_ARCHITECTURE_AND_READER_NEEDS.md`, `README_BROCHURE_AND_AI_TEXT_STANDARD.md`, `AUTHORING_EXECUTION_PROTOCOL.md`를 따라야 한다.
- 최종 시각검수는 전페이지 Scan pass + Detail pass 두 번을 모두 수행하고 Evidence를 남긴다.
