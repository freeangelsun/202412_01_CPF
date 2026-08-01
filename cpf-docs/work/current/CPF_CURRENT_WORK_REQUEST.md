# CPF 현재 통합 개발 요청

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 최초 작업 시작 SHA: `23babb9140b90e501d6ac715e7b77f55b66198a5`
- 최종 재기준 master SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 통합 범위: `CPF-SELF-DEV-001~030`, QA36 Active Gap 85건, Canonical 162건, 기존 상세 QA와 Scenario Continuity

문서나 파일 존재만으로 완료 처리하지 않는다. 실제 Owner Source, Public API/SPI, Consumer, SQL, Test, Runtime, 실패·복구 경로와 exact-SHA Evidence가 연결돼야 한다.

## 2. 이번 개발 범위

1. ADM/BZA Route·Menu·Component·Permission·OpenAPI·Generated Consumer 전수 Closure
2. Batch/Online/Gateway/Notification·Incident 운영 Workbench와 위험조치 계약
3. Audit fail-closed, Operator Trust, Calendar CAS, Batch DB 오류·Ghost Lock 안전성
4. Oracle·PostgreSQL·MariaDB Canonical Schema와 Migration·Rollback·Runtime Query parity
5. Generator Golden Template·Lifecycle·EDU Coverage·Profile Isolation
6. Network/SSRF·Download·Archive·Actor 위조 Negative Boundary
7. Requirement→Source/API/SQL/Test/Evidence 역추적과 Read-only Final Verification Plan

## 3. 완료 판정

- `development_status`와 `verification_status`를 분리한다.
- Source 구현·Consumer·Test가 완결된 항목만 개발 완료로 판정한다.
- 실행하지 못한 Java 25, Frontend clean build, 3DB Runtime, Kafka/Redis, 3 Browser, Multi-instance/Fault 검증은 `미검증`으로 남기고 환경·명령·종료 코드를 Evidence에 기록한다.
- `부분 구현`과 `미구현`을 문서 변경으로 숨기지 않는다.
- 사용자 승인 없이 Commit, Push, Branch, Tag, PR, Reset, Restore, Stash, Clean 또는 추적 파일 삭제를 수행하지 않는다.

## 4. 결과물

프로젝트 Root 상대경로의 단일 ZIP Overlay에 Source·SQL·Test·Config·Script·Review·Matrix·Evidence·Manifest·독립 검수 요청서를 포함한다.
