# CPF CURRENT WORK REQUEST — 20260729_04 FINAL REVIEW

## 현재 목표

`b8941577b99535ff3e64a4fad99b74bafa544227` 기준 Source 개발 Overlay를 최신 `master`에 적용한 뒤 exact-SHA 통합 검수를 수행한다. 신규 기능 개발을 Codex에게 넘기지 않는다.

## 검수 대상

1. Local Runtime 물리 이관과 Root 중복 제거 후 Java 25·Gradle 9.1 Clean Build/Test/Assemble/Quality Gate
2. ADM/BZA Frontend Test·Production Build·Browser E2E
3. Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Rollback/Recovery
4. Redis Topology·장애·복구·다중 Instance
5. File Job 대용량·보안·중단복구·Lease/Fencing
6. Runtime Control 부분 실패·Unknown·Rollback·Reconcile
7. Generator 임의 Domain Lifecycle과 CREATE/UPDATE/DELETE 멱등 원장 재호출·Request Hash 충돌·3 Vendor Rollback
8. QA Scenario 387개 exact-SHA Evidence

## 실패 처리

Codex는 Source를 수정하지 않는다. 실패한 Scenario ID, 명령, 환경, Expected/Actual, 최초 실패 지점, Sanitized Evidence를 ChatGPT 개발 세션에 반환한다. Source 결함은 ChatGPT가 수정한다.

## 완료 금지

실행하지 않은 검증, 과거 SHA Evidence, 빈 PASS 파일, 일부 Vendor/일부 Role/일부 Instance 결과로 전체 완료 처리하지 않는다.
