# CPF Documentation Harness v2.3.0

CPF 공식 README/DOCX/PDF를 **점진적으로 개선**하기 위한 실행형 Design/QA Harness다.

## 최상위 원칙

- **항상 보완 수정(PATCH_FIRST)한다. Fresh rewrite는 예외다.**
- `USER_APPROVED` 또는 `VISUAL_QA_APPROVED` 영역은 Finding 영향이 없으면 그대로 보존한다.
- `AUTOMATED_PASS_ONLY`는 품질 승인 상태가 아니다.
- 문서는 `component-system.json`의 공식 Component와 `design-tokens.json`을 조립해 만든다.
- 자동 Validator와 실제 Render 품질 판정을 분리한다.
- 완료에는 `HUMAN_VISUAL_PASS`와 `REGRESSION_PASS`가 반드시 필요하다.
- 승인된 Golden Reference가 있으면 그보다 나빠지는 변경은 FAIL이다.
- README/DOCX/PDF에서 새 결함이 발견되면 해당 원인을 Harness에 먼저 반영하고 **영향 범위만** 다시 보정한다.

## 정본 파일 역할

- `harness.json` — 흐름·우선순위·완료 조건
- `design-tokens.json` — 숫자/spacing/grid 정본
- `component-system.json` — 실행 가능한 문서 블록 정본
- `quality-acceptance.json` — 자동/수동/회귀 승인 모델
- `golden-reference.json` + `GOLDEN_REFERENCE_STANDARD.md` — 승인 기준 화면 Registry
- `profiles/*.json` — 문서별 Coverage/Outcome; 정확한 페이지 박스 수를 채우는 규격이 아님
- `visual-qa.json` — Render/Visual Hard Fail + 수동 Score
- `document-output-rules.json` — 파일/Link/생성 정책

## 작업 순서

1. 현행 Harness와 Artifact 상태 확인
2. 승인 영역과 Reject/자동 PASS 영역 구분
3. 사용자 Finding을 Harness에 먼저 반영
4. Harness Validator PASS
5. Finding 영향 범위만 Patch
6. 실제 Render 전페이지 검증
7. Manual Visual Score
8. Before/After Regression 비교
9. PDF Click-through + 사용자 DOCX 링크 0
10. Clean Replay 후 완료

Repository에는 현행 Harness 한 세트만 유지한다. 과거 Harness 복제본·해제본·백업·stale rule은 남기지 않는다.

## 최초 재구축 예외 Lifecycle

기존 공식 산출물이 사용자에 의해 `BASELINE_REJECTED`로 판정된 경우에만 사용자 명시 요청으로 최초 1회 `INITIAL_FRESH_REBUILD`를 수행할 수 있다. 결과는 `GOLDEN_BASELINE_CANDIDATE`이며, `USER_APPROVED` 또는 `VISUAL_QA_APPROVED` 이후에는 `PATCH_ONLY`로 전환한다. 승인 이후 관계없는 영역의 Fresh Rewrite는 Regression FAIL이다.


## v2.3.0 핵심

- 내용 구조가 Component를 결정한다. 표는 실제 행·열 데이터에만 사용한다.
- 표 폭은 실제 내용/역할 기반이며, 대칭 비교는 균등폭을 허용한다.
- 표 Header는 1줄 필수, H1은 1줄 우선이다.
- 대메뉴 시작 전 한 줄 안팎의 추가 여백과 의미 단위별 Vertical Rhythm을 강제한다.
- 원본 Figure뿐 아니라 README/DOCX/PDF 삽입 후 경계와 의미 완결성을 확인한다.
