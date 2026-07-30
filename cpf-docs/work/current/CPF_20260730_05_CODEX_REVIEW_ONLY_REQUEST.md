# CPF 20260730_05 Codex Review-Only Request

## 역할

Codex는 신규 설계나 개발 담당이 아니라 사용자가 Push한 exact SHA의 독립 검수자다. 사용자 승인 없이 Source 수정, Commit, Push, Branch, Tag, PR을 수행하지 않는다.

## 시작 조건

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 시작 시 `origin/master` SHA를 기록한다.
- 개발 Overlay 기준 SHA `0c502b917cd2185cf1ff097c5beac3e5aefb00ac`와 사용자 Push SHA를 구분한다.
- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`와 최신 Handover를 먼저 읽는다.

## 최소 크레딧 검수 순서

1. `git diff --check`, Repository Hygiene, 구형 Gateway 모델 3개 제거 확인
2. `verify-qa30-completion.py` Static Gate
3. 변경 Module Compile/Test와 Frontend Verify
4. 실제 3DB Lifecycle
5. Gateway/Batch Runtime·Redis·Multi-instance·Browser E2E
6. Matrix 926개와 결함 56개(48+8) 양방향 추적
7. Strict Full Completion Gate

이미 PASS Evidence가 있는 Changed-source Harness를 무의미하게 반복하지 말고, exact-SHA·실제 환경에서만 확인 가능한 항목을 우선한다.

## 실패 보고 형식

Requirement/Scenario/Defect ID, Source 위치, 실행 명령, Expected/Actual, Exit Code, Evidence 경로를 한 건으로 묶는다. 실행하지 않은 항목은 PASS가 아니라 `미검증`으로 기록한다.
