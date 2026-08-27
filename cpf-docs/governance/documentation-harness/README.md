# CPF Documentation Harness v2.5.0

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

## 정본 파일

- `DOCUMENT_QUALITY_STANDARD.md` — 독자·Table·Heading·Figure·Accessibility·재발 방지 통합 품질 표준
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
