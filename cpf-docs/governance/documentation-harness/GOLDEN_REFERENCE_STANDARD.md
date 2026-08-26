# CPF Documentation Golden Reference Standard

- Harness: **v2.1.0**
- 목적: 규칙 문장만으로 품질을 판단하지 않고, 승인된 화면을 실제 회귀 기준으로 사용한다.

## 1. Golden Reference 원칙

Golden Reference는 자동 Validator PASS 결과가 아니라 **사용자 승인 또는 전페이지 Visual QA 승인**을 받은 페이지/블록이다.

- `USER_APPROVED`가 최우선이다.
- `VISUAL_QA_APPROVED`는 사용자가 별도 거절하지 않은 경우 보존 기준선으로 사용할 수 있다.
- `AUTOMATED_PASS_ONLY`는 Golden Reference가 될 수 없다.
- 승인되지 않은 예전 산출물을 “잘 된 예”로 자동 승계하지 않는다.

## 2. 최소 Reference 유형

다음 유형은 승인본이 생기면 Registry에 등록한다. 모든 유형을 억지로 새로 만들지 않는다.

- README Dark Page + Light Visual
- README 전체 Architecture
- 일반 DOCX H1/H2/본문/Bullet 페이지
- 개발자 Decision/API 페이지
- Figure + 짧은 한국어 설명 페이지
- Table + 본문 페이지
- 운영 판단→조치→검증 페이지
- Gateway 선택/미선택 Visual

## 3. 사용 방법

1. 현재 Artifact에서 `USER_APPROVED`/`VISUAL_QA_APPROVED` 영역을 찾는다.
2. 변경 대상의 동일 유형 Golden Reference를 불러온다.
3. 영향 범위만 Patch한다.
4. Before/After를 같은 배율로 비교한다.
5. 승인본보다 계층·여백·균형·가독성이 낮아지면 FAIL이다.
6. 새 결과가 더 좋아지고 승인되면 이전 Reference를 `SUPERSEDED`로 바꾼다.

## 4. 금지

- Golden Reference가 없다는 이유로 전체 문서를 Fresh redesign하지 않는다.
- 다른 문서의 스타일을 무작정 복제하지 않는다.
- 단순히 최신 파일이라는 이유로 Golden Reference로 등록하지 않는다.

## 최초 재구축 예외 Lifecycle

기존 공식 산출물이 사용자에 의해 `BASELINE_REJECTED`로 판정된 경우에만 사용자 명시 요청으로 최초 1회 `INITIAL_FRESH_REBUILD`를 수행할 수 있다. 결과는 `GOLDEN_BASELINE_CANDIDATE`이며, `USER_APPROVED` 또는 `VISUAL_QA_APPROVED` 이후에는 `PATCH_ONLY`로 전환한다. 승인 이후 관계없는 영역의 Fresh Rewrite는 Regression FAIL이다.
