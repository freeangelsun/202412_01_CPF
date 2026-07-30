# CPF AI 개발·QA·세션 연속성 표준

## 1. 목적

이 표준은 ChatGPT, Codex 및 후속 AI 세션이 CPF를 검토·개발할 때 같은 완료 기준을 사용하도록 한다. 대화 기억이 아닌 Repository 문서를 정본으로 사용한다.

## 2. 역할은 경향이지 고정 분리 아님

사용자는 가능하면 ADM·BZA UI, EDU와 주요 기능의 실제 사용성을 중심으로 검토한다. 이는 역할 제한이 아니며 필요하면 Architecture·DB·Runtime·QA에도 관여한다. AI는 기술적 완결성과 요구사항 품질을 더 많이 책임지되 사용자 판단 영역을 임의로 배제하지 않는다.

## 3. 범위 통제

현재 Release 범위에 추가 가능한 것은 다음뿐이다.

- 정본 Requirement 충족에 필수
- 실제 기능이 동작하지 않는 결함
- 보안·권한·데이터 무결성 문제
- Build·배포·Runtime·복구 차단 문제
- 회귀 방지를 위한 필수 Test·Gate
- 사용자가 명시적으로 승인한 ADM 사용성·EDU 개발자 Sample 범위

검수자의 취향, 미래 확장, 편의성 제안은 결함으로 위장하지 않고 Post-GA 또는 별도 제안으로 분리한다.

## 4. 완료 판정

다음 존재만으로 완료가 아니다.

- Interface, DTO, Record, Enum
- Controller, Client, Adapter
- SQL Table, Migration 파일
- 화면, Button, Menu
- Test Class
- Static Anchor 문자열
- Compile 성공

완료는 다음 수직 흐름이 실제로 연결되고 필수 Evidence가 있을 때만 가능하다.

`사용자/호출자 → 인증·권한 → API/Port → Owner → DB/State → Runtime Consumer → 실패·복구 → 원장·감사 → UI/호출 결과`

## 5. 상태 정의

- `미구현`: Product Source 없음
- `부분 구현`: 일부 Layer나 일부 Topology만 구현
- `미검증`: 구현 주장 가능하나 필수 실행 Evidence 없음
- `실패`: 실행했으나 Acceptance 불충족
- `재확인 필요`: SHA·환경·문서·Evidence 불일치
- `완료`: 최신 exact SHA에서 모든 필수 Acceptance와 Evidence 통과

`개발 완료·미검증`은 최종 완료 수에 포함하지 않는다.

## 6. 검수 원본 불변성

요청서, 결함 원장, Requirement Matrix, Scenario Matrix와 무결성 Manifest는 검수 원본이다. 개발 AI는 원본의 결함 행 삭제, 심각도 하향, 완료 상태 변경, Acceptance 완화, Scenario 제거를 하지 않는다.

결과는 다음 별도 파일에 기록한다.

- Pre-development Review
- Development Completion Report
- Result Matrix
- Evidence JSON
- Unresolved/Risk Register
- Codex Review Report

## 7. README·Guide 제외 정책

README와 Guide는 별도 AI 모델이 병행 작업한다. 개발·QA·Codex는 다음을 원칙적으로 수정하거나 대규모 검수하지 않는다.

- 모든 `README*`
- `cpf-docs/guides/**`
- `cpf-tools/README.md`

기능 개발에 반드시 필요한 최소 변경은 허용하되 Completion Report에 이유를 기록한다. README·Guide 차이는 Product Source 결함 수에 포함하지 않으며, README·Guide 변경을 Runtime 완료 Evidence로 사용하지 않는다. 이 정책은 모든 QA 작업 요청과 Codex 인수인계에 반복 명시한다.

## 8. ADM UI 품질 기준

ADM은 단순 CRUD 화면이 아니라 운영 Control Plane이다.

- Capability-aware Menu와 Deep Link
- Global Search, Breadcrumb, 즐겨찾기/최근 메뉴
- Saved Filter, Column 설정, Server-side Paging/Sort
- 상태·마지막 갱신·Source Instance·Stale 표시
- Detail/Timeline/Diff/관련 거래 연결
- Dry-run/Simulation/영향도 Preview
- 위험조치 Reason·Approval·Typed Confirmation
- Async Operation 진행, SSE와 Polling Fallback
- Retry/Revalidate/Cancel/Reconcile
- Bulk Action과 부분 성공 결과
- 접근성, Keyboard, 오류 Focus
- API Permission과 직접 URL 차단

## 9. EDU 품질 기준

EDU는 `cpf-reference`의 실행 가능한 개발자 Sample이다.

- CPF Public API를 실제 사용
- 제품 Owner와 Sample Owner 분리
- 운영 Profile 기본 비활성
- Secret·개인정보·실제 Credential 미포함
- Happy Path와 Failure/Recovery Path 모두 제공
- Local/Remote Parity
- MariaDB/PostgreSQL/Oracle에서 가능한 Sample DB 검증
- OpenAPI·JavaDoc·Test Kit와 연결
- Sample Source와 Test가 서로 대응
- 미설치 Capability는 성공으로 위장하지 않음

## 10. Codex Credit 효율화

Codex는 매 개발 차수마다 전체 Repository를 다시 읽지 않는다.

- 2~4개 수직 Slice 또는 의미 있는 개발 묶음이 모이면 검수
- P0 보안·데이터 손상·Migration은 즉시 검수 가능
- Base SHA, Head SHA, 변경 파일, Root Cause, 영향 Module, 실행 명령을 미리 제공
- 1차: 변경 Diff와 직접 Consumer 중심
- 2차: Root Cause Repository 검색
- 3차: 영향 Test와 Gate
- Full Repository 재검수는 Release Candidate 또는 구조 변경 때만 수행
- README·Guide는 제외
- 이미 검증한 불변 영역은 Evidence Hash가 유지되면 재검수 생략

## 11. 세션 인수인계 필수 항목

모든 개발·QA 세션은 다음을 인수인계한다.

- 시작/종료 SHA
- 현재 수직 Slice
- 닫힌 결함과 Evidence
- 재개방 결함과 Root Cause
- 미검증 환경
- 다음 실행 명령
- README·Guide 제외 정책
- 사용자의 ADM·BZA UI·EDU 중점 선호
- Commit/Push 여부
- 다음 세션이 원본 요청서를 임의 변경하면 안 된다는 경고
