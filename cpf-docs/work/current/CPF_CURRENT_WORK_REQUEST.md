# CPF Current Work Request — Final Integrated Verification

기준 Source: master `2daef3b7d2f82745d42d9b19804dde4bcac60edb` (`20260727_05`) + `CPF_FINAL_COMPLETION_PACKAGE_20260728` 적용 상태

## 현재 목표

이번 Completion 패치에서 구현한 Source/SQL/Generator/Gateway/BAT/ADM/BZA/DB 3 Vendor 변경을 최신 적용 Commit 기준으로 실제 실행 검증하고 Evidence를 정본화한다. 새 기능 개발목록을 다시 누적하지 않는다.

## 반드시 실행할 검증

1. Java 25 + Gradle 9.1 + Spring Boot 4.1 전체 `clean test assemble` 및 `qualityGate`.
2. ADM/BZA frontend install/build/test 및 Browser E2E.
3. MariaDB/PostgreSQL/Oracle 각각 Fresh Install → Product Seed → Runtime Query → Upgrade/Migration → Rollback → Reapply → Verify.
4. ADM Session/Permission DB 장애 fail-closed, 상태/Role/비밀번호 변경 즉시 Session 무효화, Session revoke UNKNOWN_RESULT 재처리.
5. BZA 로그인 성공/실패 Transaction, operationId 결과불명 재시도, Refresh Token rotation, 상태/Role/Permission 변경 세션 무효화.
6. Raw PII 최소 Projection, 감사 선행, 사유 Sanitization, ADM/BZA Browser zeroization.
7. Gateway target A down → target B failover, 일반 4xx 무재시도, timeout/408/425/429/5xx retry budget, UNKNOWN_RESULT/Reconciliation.
8. BAT Scheduler/Worker/Center-Cut 다중 인스턴스 lease/fencing/takeover/restart/checkpoint/retry.
9. Generated Domain create → DB bootstrap → build/test/runtime → remove → regenerate normalized parity.
10. LOCAL_DEV/REMOTE/OFFLINE Artifact와 install/upgrade/rollback/recovery release flow.

## Evidence 규칙

실행하지 않은 항목은 성공으로 기록하지 않는다. 각 Evidence에는 기준 SHA, 명령, profile/environment, 시작·종료시각, Requirement/QA ID, 실제 결과, 민감정보 제거 여부를 포함한다.
