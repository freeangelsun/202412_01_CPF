# CPF QA32 개발 인수인계

## 1. 작업 성격

QA32는 QA31 원본을 수정하지 않는 추가 개발 요청이다. OSS-first 전면 교체와 이전 공통 결함 원장을 하나의 실행 계획으로 묶는다.

## 2. 시작 전 확인

- `git remote -v`
- `git status --short`
- `git rev-parse HEAD`
- branch/tag/detached 상태
- Request Integrity Script
- Gradle/Java/Node/npm/PowerShell/Python/Container Runtime Version
- 개발 가능한 DB·Kafka·Browser 환경

환경이 없으면 해당 Scenario를 `NOT_EXECUTED`로 유지하고 완료 선언을 하지 않는다.

## 3. 권장 작업 순서

1. OSS Governance, Core/Common 분리, BOM/Lock/Repository 정책
2. ADM/BZA UI·Router·State·Orval·BFF Session
3. Gateway SCG Web MVC 단일 BootJar
4. Kafka Messaging과 실제 통합 Test
5. Supply Chain Gate
6. Resilience/Batch/Scheduler/Flyway/Observability/Cache
7. 44개 공통 Source/Build/Security/Operations 요구 전수 수정
8. Legacy 제거
9. 전체 Mandatory Scenario 실행
10. Result Matrix·Evidence·Result ZIP

## 4. 병렬 작업 규칙

병렬 개발은 허용하지만 다음을 공유 정본으로 사용한다.

- Requirement/Scenario/Defect ID
- Public API와 Module Owner
- Database Migration Version
- OSS Version Catalog/BOM
- OpenAPI Specification
- Error/State/Idempotency/Audit Contract

같은 파일을 여러 Agent가 동시에 수정하지 않도록 Workstream Owner를 기록한다.

## 5. 실패 처리

- 검증 실패 로그를 숨기지 않는다.
- 보안·무결성 실패는 자동 재생성이나 Retry로 덮지 않는다.
- 결과 불명 Side Effect는 `UNKNOWN_RESULT`와 Reconciliation으로 처리한다.
- 필수 환경 미보유를 구현 완료로 대체하지 않는다.
- 시간이 부족하면 완료된 항목과 미완료 항목을 정확히 분리한다.

## 6. Result ZIP

최종 결과 ZIP은 개발 시작 SHA 이후 실제 변경 파일과 필수 결과 문서를 Root 상대경로로 포함한다. 사용자의 명시 승인 전 GitHub Write는 수행하지 않는다.
