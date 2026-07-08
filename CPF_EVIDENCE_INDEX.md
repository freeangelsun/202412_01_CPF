# CPF 증적 인덱스

## 작성 기준

- `CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`의 check id와 상태를 동일하게 유지합니다.
- 완료 항목은 실제 존재하는 증적 파일을 연결합니다.
- 실행하지 않은 검증은 미검증으로 표시하고 완료로 집계하지 않습니다.

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | 로그 파일 존재 | 이전 검증 증적 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_01/mariadb-full-install-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-runtime | 완료 | `specs/evidence/20260707_01/adm-runtime-smoke-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_01/adm-permission-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| openapi-runtime | 완료 | `specs/evidence/20260707_01/openapi-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-browser-click | 미검증 | `specs/evidence/20260707_01/adm-ui-browser-smoke-result.sanitized.json` | SKIPPED 사유 확인 | 브라우저 클릭 실검증 미수행 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_01/standard-header-e2e-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260707_01/composite-transaction-failure-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json`, `specs/evidence/20260707_01/adm-transaction-group-failure-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| redis-kafka-mq-broker | 미검증 | 없음 | broker 미실행 | 완료로 집계하지 않음 |
| broker-real-integration | 미검증 | 없음 | broker 미실행 | 완료로 집계하지 않음 |
| file-log-standard | 완료 | `specs/evidence/20260707_01/file-log-standard-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_01/trace-boost-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_01/bat-trace-boost-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| runtime-start-services | 완료 | `specs/evidence/20260707_01/runtime-start-services-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_01/packaged-runtime-resource-check.sanitized.json` | JSON parse | 이전 검증 증적 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_01/runtime-status-result.sanitized.json`, `specs/evidence/20260707_01/runtime-diagnostics-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| runtime-closure | 완료 | `specs/evidence/20260707_01/runtime-closure-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_01/adm-log-policy-ui-static-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_01/bat-log-bean-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260707_01/exs-timeout-retry-runtime-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_01/create-domain-result.sanitized.json` | JSON parse | 이전 검증 증적 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260707_03/service-registry-runtime-result.sanitized.json`, `specs/evidence/20260707_03/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260707_03/service-registry-health-runtime.sanitized.json`, `specs/evidence/20260707_03/service-call-engine-circuit-transition.sanitized.json`, `specs/evidence/20260707_03/service-call-engine-failover.sanitized.json`, `specs/evidence/20260707_03/pfw-service-call-engine-test.log`, `specs/evidence/20260707_03/forbidden-direct-call-scan.log` | JSON parse, Gradle test log, boundary scan log | 엔진 경로와 source/unit smoke는 통과. 실제 다중 서비스 HTTP runtime은 후속 |
| adm-service-registry-runtime | 미검증 | `specs/evidence/20260707_03/adm-service-registry-api-test.log`, `specs/evidence/20260707_03/adm-service-registry-ui-static-smoke.sanitized.json`, `specs/evidence/20260707_03/adm-service-registry-runtime-result.sanitized.json` | Gradle test log, JSON parse | API 단위/정적 UI 통과. 실제 ADM 서버 API 호출은 미수행 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_01/runtime-smoke-summary.sanitized.json` | JSON parse | 이전 검증 증적 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260707_03/quality-gate.log` | Gradle 로그 | qualityGate 안에서 report/matrix/evidence 상태값 정합성 실행 |
| quality-gate | 완료 | `specs/evidence/20260707_03/quality-gate.log` | Gradle 로그 | 이번 작업 최종 단계 |
| check-html-docs | 완료 | `specs/evidence/20260707_03/check-html-docs.log` | 스크립트 로그 | 이번 작업 최종 단계 |
| check-feature-evidence | 완료 | `specs/evidence/20260707_03/check-feature-evidence.log` | 스크립트 로그 | 이번 작업 최종 단계 |
| check-utf8 | 완료 | `specs/evidence/20260707_03/check-utf8-mojibake.log` | 스크립트 로그 | 이번 작업 최종 단계 |
