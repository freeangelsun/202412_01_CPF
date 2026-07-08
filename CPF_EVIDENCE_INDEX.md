# CPF 증적 인덱스

## 작성 기준

- `CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`의 check id와 상태를 동일하게 유지한다.
- 완료 항목은 실제 존재하는 증적 파일과 연결한다.
- 실행하지 않은 검증은 미검증 또는 부분 구현으로 기록한다.

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | Gradle 로그 | 기존 검증 증적 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_01/mariadb-full-install-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-runtime | 완료 | `specs/evidence/20260707_01/adm-runtime-smoke-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_01/adm-permission-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| openapi-runtime | 완료 | `specs/evidence/20260707_01/openapi-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-browser-click | 미검증 | `specs/evidence/20260707_01/adm-ui-browser-smoke-result.sanitized.json` | SKIPPED 사유 확인 | 실제 브라우저 클릭 미수행 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_01/standard-header-e2e-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260707_01/composite-transaction-failure-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json`, `specs/evidence/20260707_01/adm-transaction-group-failure-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| redis-kafka-mq-broker | 미검증 | 없음 | broker 미실행 | 실제 broker runtime 미수행 |
| broker-real-integration | 미검증 | 없음 | broker 미실행 | Redis/Kafka/MQ 실연동 후속 |
| file-log-standard | 완료 | `specs/evidence/20260707_01/file-log-standard-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_01/trace-boost-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_01/bat-trace-boost-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| runtime-start-services | 완료 | `specs/evidence/20260707_01/runtime-start-services-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_01/packaged-runtime-resource-check.sanitized.json` | JSON parse | 기존 검증 증적 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_01/runtime-status-result.sanitized.json`, `specs/evidence/20260707_01/runtime-diagnostics-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| runtime-closure | 완료 | `specs/evidence/20260707_01/runtime-closure-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_01/adm-log-policy-ui-static-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_01/bat-log-bean-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260707_01/exs-timeout-retry-runtime-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_01/create-domain-result.sanitized.json` | JSON parse | 기존 검증 증적 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260708_02/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260708_02/service-call-engine-circuit-transition.sanitized.json`, `specs/evidence/20260708_02/service-call-engine-failover.sanitized.json`, `specs/evidence/20260708_02/service-registry-health-runtime.sanitized.json`, `specs/evidence/20260708_02/pfw-service-call-engine-test.log`, `specs/evidence/20260708_02/forbidden-direct-call-scan.log` | JSON parse, Gradle 로그, scan 로그 | 실제 다중 서비스 HTTP runtime은 후속 |
| adm-service-registry-runtime | 미검증 | `specs/evidence/20260708_02/adm-service-registry-api-test.log`, `specs/evidence/20260708_02/adm-service-registry-ui-static-smoke.sanitized.json` | Gradle 로그, JSON parse | 실제 ADM `-RunRuntime` 호출은 미수행 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260708_02/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260708_02/check-architecture-ownership.log` | JSON parse, scan 로그 | failure 0, CMN PFW port migration 후보는 warning 분류 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260708_02/spring-event-usage-scan.sanitized.json`, `specs/evidence/20260708_02/check-spring-event-usage.log` | JSON parse, scan 로그 | Spring Event 핵심 거래 흐름 남용 금지 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260708_02/pfw-capability-contract-test.log` | Gradle 로그 | 실제 broker runtime은 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260708_02/pfw-capability-contract-test.log` | Gradle 로그 | 실제 SFTP/FTP/SSH runtime은 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260708_02/pfw-capability-contract-test.log` | Gradle 로그 | 실제 Vault/KMS/HSM 연동은 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260708_02/pfw-capability-contract-test.log` | Gradle 로그 | 다중 instance runtime은 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260708_02/pfw-capability-contract-test.log` | Gradle 로그 | ADM 실화면 연결은 후속 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_02/profile-loading-result.sanitized.json`, `specs/evidence/20260708_02/check-profile-loading.log` | JSON parse, script 로그 | local/dev/stg/prod profile import 검사 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_02/packaged-dependencies-acc.sanitized.json`, `specs/evidence/20260708_02/check-packaged-dependencies-acc.log` | JSON parse, script 로그 | ACC bootJar 기준 PFW/CMN 포함 점검 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_02/deploy-acc-dev-dry-run.sanitized.json`, `specs/evidence/20260708_02/deploy-acc-dev-dry-run.log` | JSON parse, script 로그 | 실제 원격 배포는 미검증 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_01/runtime-smoke-summary.sanitized.json` | JSON parse | 기존 검증 증적 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260708_02/quality-gate.log` | Gradle 로그 | qualityGate 안에서 정합성 검사 |
| quality-gate | 완료 | `specs/evidence/20260708_02/quality-gate.log` | Gradle 로그 | 이번 작업 최종 gate |
| check-html-docs | 완료 | `specs/evidence/20260708_02/check-html-docs.log` | script 로그 | HTML 문서 구조 검사 |
| check-feature-evidence | 완료 | `specs/evidence/20260708_02/check-feature-evidence.log` | script 로그 | 기능 증적 검사 |
| check-utf8 | 완료 | `specs/evidence/20260708_02/check-utf8-mojibake.log` | script 로그 | UTF-8/mojibake 검사 |

