# QA 문서 분리 및 다음 개발요건 머지 정책

## 1. QA 문서 소유권

이 디렉터리의 모든 문서는 QA 전용 신규 산출물이다.

- 개발 GPT 산출물과 같은 경로를 사용하지 않는다.
- 개발 GPT의 REVIEW_INDEX.md, REQUIREMENT_STATUS.csv, TEST_AND_EVIDENCE.md,
  OPEN_ISSUES.md, CHANGE_MANIFEST.csv, PACKAGE_MANIFEST.json을 덮어쓰지 않는다.
- 개발 GPT와 Codex는 QA 원본 문서의 판정·내용·상태를 수정하지 않는다.
- QA 재검수 시 새 QA 회차 디렉터리와 새 파일을 생성한다.

## 2. 다음 개발요건 생성 시 머지 방식

다음 개발요건을 작성할 때 QA 원본을 직접 수정하지 않는다.

다음 절차를 사용한다.

1. QA Finding ID와 Requirement ID를 읽는다.
2. 다음 개발 Batch의 신규 개발요청서에 별도 행으로 복제·연결한다.
3. 개발 대상 실행순서와 QA 재개발 항목을 하나의 신규 개발 Scope에서 관리한다.
4. QA 원본 경로를 source_basis와 qa_source_path로 참조한다.
5. 개발 GPT는 자신의 수행·자체검수 컬럼만 기록한다.
6. QA 원본의 QA 결과와 미통과 사유는 그대로 보존한다.
7. Codex 보완 후 QA는 새 QA 회차에서 다시 판정한다.

## 3. 금지

- QA 파일을 개발 파일명으로 변경
- QA 파일을 개발 디렉터리에 덮어쓰기
- 개발 GPT가 QA 결과를 통과로 변경
- 기존 QA Finding 삭제 또는 축약
- QA 원본과 개발 결과를 같은 파일에서 역할 구분 없이 혼합
- 사용자 머지 지시 전 활성 단일 Requirement 원장에 직접 반영

## 4. 다음 통합 개발 범위

다음 개발요건 생성 시 다음을 신규 문서로 통합한다.

- execution_order 10,028~30,027 기본 20,000건
- Work Package 경계 연장분
- QA-DEV-R1 Finding 25건
- CPF-SELF-DEV-S4-001~009 재개발
- 기존 execution_order 1~10,027 회귀검증

통합 문서는 개발 Batch의 신규 경로에 생성하고, 이 QA 디렉터리는 읽기 전용 Source Basis로 유지한다.
