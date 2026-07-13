# CPF 증적 인덱스

작성 시각: 2026-07-13 09:25 KST

기준 증적 디렉터리: `specs/evidence/20260710_01`

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | report/matrix/evidence 정합성 | 기존 EDU mapper DB slice 증적 유지 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_02/mariadb-full-install-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 MariaDB full install 증적 유지, 이번 작업 재실행 없음 |
| adm-runtime | 완료 | `specs/evidence/20260707_02/adm-runtime-smoke-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM runtime smoke 증적 유지 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_02/adm-permission-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 권한 runtime smoke 증적 유지 |
| openapi-runtime | 완료 | `specs/evidence/20260707_02/openapi-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 OpenAPI runtime smoke 증적 유지 |
| adm-browser-click | 완료 | `specs/evidence/20260707_02/adm-ui-browser-smoke-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM browser smoke 증적 유지 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_02/standard-header-e2e-result.sanitized.json` | report/matrix/evidence 정합성 | scripts/smoke-standard-header-e2e.ps1 기준 기존 증적 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 복합 거래 trace 증적 유지 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 segment log 증적 유지 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 목록 증적 유지 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM timeline 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260708_05/cmn-test.log` | report/matrix/evidence 정합성 | 기존 CMN fixed-length 테스트 증적 유지 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 composite runtime smoke 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 Redis/Kafka/MQ broker runtime 미실행 |
| broker-real-integration | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 broker adapter와 장애 fallback runtime 미실행 |
| file-log-standard | 완료 | `specs/evidence/20260707_02/file-log-standard-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 파일 로그 표준 증적 유지 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_02/trace-boost-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 trace boost runtime 증적 유지 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_02/bat-trace-boost-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 BAT trace boost 증적 유지 |
| runtime-start-services | 완료 | `specs/evidence/20260707_02/runtime-start-services-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime start services 증적 유지 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_02/packaged-runtime-resource-check.sanitized.json` | report/matrix/evidence 정합성 | 기존 packaged resource 증적 유지 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime status 증적 유지 |
| runtime-closure | 완료 | `specs/evidence/20260707_02/runtime-closure-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime closure 증적 유지 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM operation console runtime 증적 유지 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_02/adm-log-policy-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM log policy UI static 증적 유지 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_02/bat-log-bean-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 BAT log bean runtime 증적 유지 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260708_05/exs-test.log` | report/matrix/evidence 정합성 | 기존 EXS timeout/retry 테스트 증적 유지 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260708_05/cmn-test.log` | report/matrix/evidence 정합성 | 기존 CMN advanced fixed-length 테스트 증적 유지 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_02/create-domain-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 create-domain smoke 증적 유지 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260709_03/servicecall-contract-test.sanitized.json`, `specs/evidence/20260710_01/pfw-test.log` | report/matrix/evidence 정합성 | contract 유지, 다중 HTTP runtime 미검증 |
| adm-service-registry-runtime | 완료 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM service registry runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | CMN 기술 실행 후보는 PFW migration 추적 필요 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260709_03/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | Spring Event usage scan 통과 증적 유지 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260709_03/broker-contract-test.sanitized.json`, `specs/evidence/20260710_01/pfw-test.log` | report/matrix/evidence 정합성 | persistent adapter와 SQL 추가, 실 broker runtime 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260709_03/filetransfer-contract-test.sanitized.json`, `specs/evidence/20260710_01/pfw-test.log` | report/matrix/evidence 정합성 | history/duplicate JDBC adapter 추가, 외부 protocol runtime 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | report/matrix/evidence 정합성 | Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | ADM runtime API 연결 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_03/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 profile loading 증적 유지 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | report/matrix/evidence 정합성 | 기존 packaged dependencies 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json` | report/matrix/evidence 정합성 | dry-run 계획 검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 garbage scan 증적 유지 |
| empty-directory-scan | 완료 | `specs/evidence/20260708_05/empty-directory-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 empty directory scan 증적 유지 |
| deploy-env-standard | 완료 | `specs/evidence/20260708_05/deploy-env-acc-dev.sanitized.json` | report/matrix/evidence 정합성 | 기존 deploy env 증적 유지 |
| deploy-inventory-standard | 완료 | `specs/evidence/20260708_05/deploy-inventory-acc-dev.sanitized.json` | report/matrix/evidence 정합성 | 기존 deploy inventory 증적 유지 |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260708_05/gradle-remote-deploy-task-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 Gradle deploy task scan 증적 유지 |
| datasource-mode-standard | 완료 | `specs/evidence/20260708_05/datasource-mode-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 datasource mode scan 증적 유지 |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260708_05/local-port-duplicate-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 local port duplicate scan 증적 유지 |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260708_05/edu-module-deploy-alias-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 EDU deploy alias scan 증적 유지 |
| bat-edu-package | 완료 | `specs/evidence/20260708_05/bat-test.log` | report/matrix/evidence 정합성 | 기존 BAT EDU sample/test 증적 유지 |
| bat-job-log-policy | 완료 | `specs/evidence/20260708_05/bat-test.log` | report/matrix/evidence 정합성 | 기존 BAT job/log policy 테스트 증적 유지 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260710_01/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | 이번 작업 신규 PFW persistence sample coverage 반영 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260710_01/sample-placeholder-scan.sanitized.json` | report/matrix/evidence 정합성 | 이번 작업 sample placeholder scan 통과 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260710_01/evidence-path-existence-check.sanitized.json` | report/matrix/evidence 정합성 | evidence path gate 결과 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260710_01/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | report/matrix/evidence consistency gate 결과 |
| quality-gate | 완료 | `specs/evidence/20260710_01/quality-gate.log` | report/matrix/evidence 정합성 | 이번 작업 qualityGate 실행 로그 |
| check-html-docs | 완료 | `specs/evidence/20260710_01/check-html-docs.log` | report/matrix/evidence 정합성 | HTML docs gate 실행 로그 |
| check-feature-evidence | 완료 | `specs/evidence/20260710_01/check-feature-evidence.log` | report/matrix/evidence 정합성 | feature evidence gate 실행 로그 |
| check-utf8 | 완료 | `specs/evidence/20260710_01/check-utf8-mojibake.log` | report/matrix/evidence 정합성 | UTF-8 및 mojibake gate 실행 로그 |
