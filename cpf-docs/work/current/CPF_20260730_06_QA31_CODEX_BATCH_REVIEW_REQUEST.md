# CPF QA31 Codex 묶음 검수 요청서

## 목적

Codex Credit을 낭비하지 않으면서 ChatGPT 개발 묶음의 실제 완결성, Root Cause 제거와 회귀 영향을 독립 검수한다.

## 검수 시작 조건

다음 중 하나일 때 검수를 시작한다.

- 2~4개 수직 Slice가 완료돼 하나의 의미 있는 변경 묶음이 형성됨
- P0 보안·승인·원장·데이터 무결성·Migration 변경
- 사용자 Push 후 exact SHA가 생성됨
- Release Candidate Full Gate 전

한 파일 수정이나 사소한 UI 문구 변경마다 전체 Repository를 재검수하지 않는다.

## 입력 필수값

개발자가 `CPF_20260730_QA31_CODEX_REVIEW_READY.md`에 다음을 채운 뒤 요청한다.

- Repository/Branch
- Base SHA
- Head SHA
- 변경 Commit 범위
- 관련 QA31 Defect/Requirement/Scenario ID
- Root Cause
- 변경 파일 목록
- 직접 Consumer와 영향 Module
- API/DB/Migration/Runtime/UI 영향
- 실행한 명령과 Evidence
- 실행하지 못한 환경
- 알려진 미해결 항목
- README·Guide 제외 확인

## Credit 절약 검수 단계

### 1단계: Diff 중심

- Base..Head 변경만 읽기
- 수정된 Public Contract와 직접 Consumer 확인
- SQL/Migration 변경 시 Source·Rollback·Canonical 연결 확인
- 완료 주장과 Evidence 경로 확인

### 2단계: Root Cause 검색

다음 Pattern만 Repository 전체 검색한다.

- 구형·신형 모델 병존
- Interface 미사용
- Test-only Consumer
- 빈 목록/기본값/현재시각/SUCCESS False Default
- Owner DB 직접 접근
- Approval 우회
- Raw Map, 하드코딩 URL·Code
- Timeout/Unknown 평탄화
- 신규 Table의 Producer/Consumer 부재

### 3단계: 영향 Test

- 변경 Module Compile/Test
- Contract/Architecture Test
- Slice Integration Test
- DB Vendor 변경 시 대상 Vendor Lifecycle
- ADM/BZA/EDU Frontend 변경 시 Browser E2E
- P0일 때 Failure Injection

### 4단계: Full Gate

여러 개발 묶음이 모였거나 Release Candidate일 때만 Java25 전체, 3DB, Redis, Multi-instance, Browser를 실행한다.

## README·Guide 경계

README와 Guide는 별도 AI 모델이 병행 작업한다. Codex는 모든 `README*`, `cpf-docs/guides/**`, `cpf-tools/README.md`를 원칙적으로 검수·수정 범위에서 제외한다.

예외는 다음뿐이다.

- Source 변경으로 링크·명령이 즉시 위험해진 경우
- Build가 README/Guide 파일 자체를 입력으로 사용해 실패하는 경우
- 사용자가 명시적으로 문서 검수를 요청한 경우

README·Guide 차이만으로 QA 결함을 추가하지 말고, README·Guide 변경만으로 Source 완료를 인정하지 말라.

## Codex 판정 형식

1. Blocker
2. Major
3. Minor
4. Evidence 부족
5. Post-GA 제안
6. 폐쇄 확인
7. 재검증 명령
8. exact SHA
9. Credit 절약을 위해 재검수 생략 가능한 불변 영역

각 결함은 관련 QA31 ID, Source 경로, 재현 흐름, 영향도, 최소 수정 범위와 완료 조건을 포함한다.

## 금지

- 요청 Matrix Acceptance 완화
- 미실행 항목 PASS 처리
- README·Guide 병행 변경을 Product 결함으로 확대
- 미래 확장을 현재 P0로 확대
- 코드 존재만으로 결함 폐쇄
- 사용자의 승인 없이 Commit/Push/PR 생성
