# CPF 안정화 작업 보고서

작성 시각: 2026-07-13 09:25 KST

## 1. 기준과 원칙

- 입력 요청서: `CPF_NEW_REQUEST.md`
- 최종 목표: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 요청서 보호: `CPF_NEW_REQUEST.md`는 읽기 전용 입력으로만 사용했다. 작업 시작 전부터 git 기준 변경 상태였으며, Codex 작업 대상으로 수정하지 않았다.
- 완료 판정: source만 있으면 `부분 구현`, 외부 runtime이 필요하지만 이번에 실행하지 못한 항목은 `미검증`, 실행 실패 또는 호환성 의심은 `실패` 또는 `재확인 필요`로 남겼다.
- 현재 작업 증적 기본 디렉터리: `specs/evidence/20260710_01`

## 2. 이번 작업 완료

- PFW idempotency 공통 모델, port, JDBC reference repository를 추가했다.
- PFW broker outbox/inbox/DLQ/replay/idempotency JDBC reference repository를 추가했다.
- PFW file transfer history/duplicate prevention JDBC reference repository를 추가했다.
- PFW unknown result/reconciliation 공통 모델, port, JDBC reference repository를 추가했다.
- 신규 persistence 테이블을 `specs/sql/10_pfw_schema.sql`, Flyway `V22__pfw_reliability_persistence.sql`, `00_all_install.sql`, `00_all_install_and_smoke.sql`, `V1__cpf_baseline_install.sql`에 동기화했다.
- `CPF_NEW_REQUEST.md` 변경 감지 스크립트와 Gradle `checkCpfRequestProtection` gate를 추가했다.
- PFW 신규 persistence adapter 테스트를 실행해 `:pfw:test` 통과 증적을 생성했다.
- sample coverage matrix에 broker/file/idempotency/reconciliation 실제 구현 샘플 행을 추가하고 검사 증적을 갱신했다.

## 3. 상태 판정표

| check id | 상태 | 증적 | 판정 메모 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | 기존 EDU mapper DB slice 증적 유지 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_02/mariadb-full-install-result.sanitized.json` | 기존 MariaDB full install 증적 유지, 이번 작업에서 재실행하지 않음 |
| adm-runtime | 완료 | `specs/evidence/20260707_02/adm-runtime-smoke-result.sanitized.json` | 기존 ADM runtime smoke 증적 유지 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_02/adm-permission-runtime-result.sanitized.json` | 기존 ADM 권한 runtime smoke 증적 유지 |
| openapi-runtime | 완료 | `specs/evidence/20260707_02/openapi-runtime-result.sanitized.json` | 기존 OpenAPI runtime smoke 증적 유지 |
| adm-browser-click | 완료 | `specs/evidence/20260707_02/adm-ui-browser-smoke-result.sanitized.json` | 기존 ADM browser smoke 증적 유지, 이번 작업에서 브라우저 재실행하지 않음 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_02/standard-header-e2e-result.sanitized.json` | 기존 표준 헤더 E2E 증적 유지, scripts/smoke-standard-header-e2e.ps1 기준 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 기존 복합 거래 trace 증적 유지 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 기존 segment log 증적 유지 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | 기존 ADM 거래 그룹 목록 증적 유지 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | 기존 ADM timeline 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260708_05/cmn-test.log` | 기존 CMN fixed-length 테스트 증적 유지 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 기존 composite runtime smoke 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | 기존 ADM 거래 그룹 runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | 실 Redis/Kafka/MQ broker runtime은 이번 작업에서 실행하지 않음 |
| broker-real-integration | 미검증 | 없음 | 실 broker adapter와 장애 fallback runtime은 외부 환경 필요 |
| file-log-standard | 완료 | `specs/evidence/20260707_02/file-log-standard-result.sanitized.json` | 기존 파일 로그 표준 증적 유지 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_02/trace-boost-runtime-result.sanitized.json` | 기존 trace boost runtime 증적 유지 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_02/bat-trace-boost-runtime-result.sanitized.json` | 기존 BAT trace boost 증적 유지 |
| runtime-start-services | 완료 | `specs/evidence/20260707_02/runtime-start-services-result.sanitized.json` | 기존 runtime start services 증적 유지 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_02/packaged-runtime-resource-check.sanitized.json` | 기존 packaged resource 증적 유지 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_02/runtime-status-result.sanitized.json` | 기존 runtime status 증적 유지 |
| runtime-closure | 완료 | `specs/evidence/20260707_02/runtime-closure-result.sanitized.json` | 기존 runtime closure 증적 유지 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | 기존 ADM operation console runtime 증적 유지 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_02/adm-log-policy-ui-static-result.sanitized.json` | 기존 ADM log policy UI static 증적 유지 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_02/bat-log-bean-runtime-result.sanitized.json` | 기존 BAT log bean runtime 증적 유지 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260708_05/exs-test.log` | 기존 EXS timeout/retry 테스트 증적 유지 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260708_05/cmn-test.log` | 기존 CMN advanced fixed-length 테스트 증적 유지 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_02/create-domain-result.sanitized.json` | 기존 create-domain smoke 증적 유지 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260709_03/servicecall-contract-test.sanitized.json`, `specs/evidence/20260710_01/pfw-test.log` | Service Call contract는 유지, 이번 작업은 persistence capability 확장 중심이며 다중 HTTP runtime은 미검증 |
| adm-service-registry-runtime | 완료 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | 기존 ADM service registry runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json` | CMN 기술 실행 후보 2건은 PFW port migration 후보로 계속 추적 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260709_03/spring-event-usage-scan.sanitized.json` | Spring Event usage scan 통과 증적 유지 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260709_03/broker-contract-test.sanitized.json`, `specs/evidence/20260710_01/pfw-test.log` | 이번 작업에서 persistent reference adapter와 SQL을 추가했으나 실 Kafka/MQ runtime은 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260709_03/filetransfer-contract-test.sanitized.json`, `specs/evidence/20260710_01/pfw-test.log` | 이번 작업에서 history/duplicate JDBC adapter를 추가했으나 SFTP/FTP/SCP/SSH runtime은 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | signature/encryption/key reference는 테스트 통과, Vault/KMS runtime은 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | worker control/heartbeat/ghost 계약은 유지, 다중 instance runtime은 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | ADM 조회 후보 DTO 계약은 유지, 이번 작업에서 통합 runtime API는 추가하지 않음 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_03/profile-loading-result.sanitized.json` | 기존 profile loading 증적 유지 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | 기존 packaged dependencies 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json` | remote real deploy가 아니라 dry-run 계획 검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | 기존 garbage scan 증적 유지 |
| empty-directory-scan | 완료 | `specs/evidence/20260708_05/empty-directory-scan.sanitized.json` | 기존 empty directory scan 증적 유지 |
| deploy-env-standard | 완료 | `specs/evidence/20260708_05/deploy-env-acc-dev.sanitized.json` | 기존 deploy env 증적 유지 |
| deploy-inventory-standard | 완료 | `specs/evidence/20260708_05/deploy-inventory-acc-dev.sanitized.json` | 기존 deploy inventory 증적 유지 |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260708_05/gradle-remote-deploy-task-scan.sanitized.json` | 기존 Gradle deploy task scan 증적 유지 |
| datasource-mode-standard | 완료 | `specs/evidence/20260708_05/datasource-mode-scan.sanitized.json` | 기존 datasource mode scan 증적 유지 |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260708_05/local-port-duplicate-scan.sanitized.json` | 기존 local port duplicate scan 증적 유지 |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260708_05/edu-module-deploy-alias-scan.sanitized.json` | 기존 EDU deploy alias scan 증적 유지 |
| bat-edu-package | 완료 | `specs/evidence/20260708_05/bat-test.log` | 기존 BAT EDU sample/test 증적 유지 |
| bat-job-log-policy | 완료 | `specs/evidence/20260708_05/bat-test.log` | 기존 BAT job/log policy 테스트 증적 유지 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260710_01/sample-coverage-result.sanitized.json` | 이번 작업의 신규 PFW persistence sample coverage 반영 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260710_01/sample-placeholder-scan.sanitized.json` | 이번 작업 sample placeholder scan 통과 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260710_01/evidence-path-existence-check.sanitized.json` | report/matrix/evidence 경로 존재성 검사 대상 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260710_01/report-matrix-evidence-consistency.sanitized.json` | 보고서, evidence index, HTML matrix 정합성 검사 대상 |
| quality-gate | 완료 | `specs/evidence/20260710_01/quality-gate.log` | 이번 작업 qualityGate 실행 로그 대상 |
| check-html-docs | 완료 | `specs/evidence/20260710_01/check-html-docs.log` | HTML 문서 gate 실행 로그 대상 |
| check-feature-evidence | 완료 | `specs/evidence/20260710_01/check-feature-evidence.log` | feature evidence gate 실행 로그 대상 |
| check-utf8 | 완료 | `specs/evidence/20260710_01/check-utf8-mojibake.log` | UTF-8 및 mojibake 검사 로그 대상 |

## 4. SQL과 persistence 동기화

신규 persistence 테이블:

- `pfw_idempotency_record`
- `pfw_broker_outbox`
- `pfw_broker_inbox`
- `pfw_broker_dlq`
- `pfw_file_transfer_history`
- `pfw_unknown_result`

반영 위치:

- `specs/sql/10_pfw_schema.sql`
- `specs/sql/migration/flyway/V22__pfw_reliability_persistence.sql`
- `specs/sql/migration/flyway/V1__cpf_baseline_install.sql`
- `specs/sql/00_all_install.sql`
- `specs/sql/00_all_install_and_smoke.sql`
- `scripts/build-all-install-sql.ps1`

## 5. 요청서 보호와 EDU 기준

- `scripts/check-cpf-request-protection.ps1`을 추가해 작업 기준 hash를 증적으로 남기고 변경 여부를 감지한다.
- Gradle `qualityGate`에 `checkCpfRequestProtection`을 연결했다.
- `specs/sample-coverage-matrix.md`에 신규 실제 구현 샘플과 테스트 증거를 연결했다.
- EDU DB slice 관련 표준 fixture는 `xyz_edu_query_fixture.sql`을 유지한다.
- DB 환경변수 표기는 신규 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`과 호환용 `CPF_XYZ_EDU_MAPPER_DB_USER`를 함께 문서와 테스트에 유지한다.

## 6. 미검증과 남은 리스크

- 실 Redis/Kafka/MQ broker runtime, SFTP/FTP/SCP/SSH, Vault/KMS/HSM, 다중 instance runtime은 이번 작업에서 실행하지 않았다.
- 신규 PFW JDBC adapter는 unit test와 SQL source sync까지 완료했지만 ADM 운영 API/UI 전체 연결은 다음 마일스톤이다.
- Service Call의 다중 HTTP runtime, remote facade proxy 실제 호출, 전체 Controller OpenAPI coverage 자동 검사는 다음 단계로 남는다.
- Java 25 호환성은 이전 증적 기준 `재확인 필요` 상태다. 현재 검증은 Java 21 기준으로 수행했다.

## 7. 실행 검증 요약

- `:pfw:test`: `specs/evidence/20260710_01/pfw-test.log`
- `scripts/smoke-mariadb-full-install.ps1`: 기존 MariaDB full install 증적은 유지했지만 이번 작업에서는 재실행하지 않았다.
- `scripts/check-sample-coverage.ps1`: `specs/evidence/20260710_01/sample-coverage-check.log`
- `scripts/check-cpf-request-protection.ps1`: `specs/evidence/20260710_01/cpf-new-request-protection.sanitized.json`
- `scripts/check-html-docs.ps1`: `specs/evidence/20260710_01/check-html-docs.log`
- `scripts/check-feature-evidence.ps1`: `specs/evidence/20260710_01/check-feature-evidence.log`
- `scripts/check-utf8.ps1 -CheckMojibake`: `specs/evidence/20260710_01/check-utf8-mojibake.log`
- `qualityGate`: `specs/evidence/20260710_01/quality-gate.log`

위 검증은 실행 완료했다. 다만 Redis/Kafka/MQ, SFTP/FTP/SCP/SSH, Vault/KMS/HSM, 다중 instance, Java 25 실호환성 검증은 이번 범위에서 실행하지 않았으므로 미검증 또는 재확인 필요 상태로 유지한다.
