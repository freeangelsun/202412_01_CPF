# CPF 20260801_03 자체 개발 보정 요청서

## 기준

- 기준 Branch: `master`
- 시작 SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- 최우선 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`의 실제 최신 경로를 시작 시 재확인
- 통합 Backlog: `cpf-docs/quality/CPF_20260801_03_SELF_DEVELOPMENT_BACKLOG.csv`
- 사전 검수: `cpf-docs/work/review/CPF_20260801_03_POST_PUSH_INDEPENDENT_REVIEW.md`

## 작업 목표

현재 Checkpoint의 P0 Build·Frontend·Evidence 결함을 먼저 완결하고, 그 뒤 EDU·DB·Runtime 미검증을 실제 Source/Test/Evidence로 닫는다. README와 README 연결 Manual/Guide는 이번 개발 작업에서 수정하지 않는다.

## 필수 작업 순서

1. 최신 `master`, `origin/master`, Working Tree를 확인한다.
2. Root `build.gradle`을 Parent SHA `19dd72b5978f2a3c630943c0fff05bee2d2fed34` 기준으로 복구하고 이번 작업의 의도된 Root 변경만 재적용한다.
3. 삭제된 Gradle Plugin/BOM의 실제 Consumer와 Artifact 계약을 추적해 복구 또는 정식 제거한다.
4. Java 25에서 Root Project Configuration과 저비용 Gate를 먼저 통과시킨다.
5. ADM/BZA OpenAPI Source Gate와 Backend Runtime Release Gate를 분리한다.
6. Generated Client 생성 순서, `.npmrc`, Package/Lockfile, CI를 하나의 동일 계약으로 맞춘다.
7. Clean Checkout에서 ADM/BZA npm ci, generate, consumer, lint, typecheck, unit, build를 수행한다.
8. 격리된 Runner에서 Playwright Chromium·Firefox·WebKit을 수행한다. 사용자 Windows Smart App Control 환경에 검증을 전가하지 않는다.
9. EDU 32 Feature를 기존 `cpf-reference` Source/Test에 실제로 해석한다. 누락된 Feature만 구현하고 기존 예제를 중복 작성하지 않는다.
10. Oracle/PostgreSQL/MariaDB V92와 전체 Canonical Lifecycle을 실제 DB에서 검증한다.
11. Kafka·Redis·Batch·Gateway·Agent, 다중 인스턴스, 부분 실패와 Recovery를 검증한다.
12. 최종 SHA에서 Matrix, Evidence, Completion Report, Manifest, SHA-256을 재생성한다.
13. 사후 리뷰와 Codex 독립 검수 요청서를 작성하고 Root Overlay ZIP을 만든다.

## 완료 금지 조건

- Root `build.gradle`이 Module 전용 Build 내용인 상태
- Source OpenAPI를 Release 성공으로 처리
- Generated Client 없이 Typecheck/Build를 완료 처리
- `legacy-peer-deps` 우회 이유와 동일 CI 조건이 없는 상태
- Parent SHA Evidence를 현재 SHA Evidence로 승계
- EDU Matrix 행만 존재하고 실제 Source/Test Glob가 해석되지 않은 상태
- 실행하지 않은 Java, Frontend, DB, Runtime, Browser 검증을 성공으로 기록
- README·Guide를 Source 완료 증거로 사용

## 완료 조건

Backlog 13건 각각을 `완료`, `미검증`, `실패`, `재확인 필요` 중 하나로 사실대로 기록한다. 개발 목표는 P0 Source 결함을 모두 `완료`로 닫는 것이며, 환경 검증은 실제 실행 Evidence가 없으면 `미검증`으로 남긴다.
