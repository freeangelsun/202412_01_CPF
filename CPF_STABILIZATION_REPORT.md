# CPF 안정화 작업 리포트

작성 시각: 2026-07-10 10:00:22 +09:00

## 작업 요약

- PFW Service Call 고급 EDU 샘플을 추가해 selectedInstanceId, call history, registry, direct/LB mode, circuit failure 계약을 테스트했습니다.
- PFW Broker 고급 EDU 샘플을 추가해 outbox, inbox, DLQ, replay, idempotency 흐름을 테스트했습니다.
- PFW FileTransfer 고급 EDU 샘플을 추가해 LOCAL adapter, checksum, duplicateKey, history, unknown result, 외부 프로토콜 plan을 테스트했습니다.
- PFW Security crypto EDU 샘플을 추가해 credential reference, key/cert snapshot, signature, encryption/decryption 계약을 테스트했습니다.
- PFW Runtime/Archive 고급 EDU 샘플을 추가해 worker control, heartbeat, ghost candidate, archive guard, corrupted handling, history metadata를 테스트했습니다.
- sample-coverage-matrix의 PFW catalog-only 항목을 실제 source/test/evidence 경로로 전환했습니다.

## 구현 소스

- `pfw/src/main/java/cpf/pfw/common/servicecall/PfwServiceCallAdvancedEducationSample.java`
- `pfw/src/main/java/cpf/pfw/common/broker/PfwBrokerReliabilityEducationSample.java`
- `pfw/src/main/java/cpf/pfw/common/filetransfer/PfwFileTransferAdvancedEducationSample.java`
- `pfw/src/main/java/cpf/pfw/common/security/PfwSecurityCryptoEducationSample.java`
- `pfw/src/main/java/cpf/pfw/common/runtime/PfwRuntimeWorkerEducationSample.java`
- `pfw/src/main/java/cpf/pfw/common/archive/PfwArchiveAdvancedEducationSample.java`
- `pfw/src/main/java/cpf/pfw/common/archive/PfwArchiveHistoryEducationSample.java`

## 테스트 소스

- `pfw/src/test/java/cpf/pfw/common/servicecall/PfwServiceCallAdvancedEducationSampleTest.java`
- `pfw/src/test/java/cpf/pfw/common/broker/PfwBrokerReliabilityEducationSampleTest.java`
- `pfw/src/test/java/cpf/pfw/common/filetransfer/PfwFileTransferAdvancedEducationSampleTest.java`
- `pfw/src/test/java/cpf/pfw/common/security/PfwSecurityCryptoEducationSampleTest.java`
- `pfw/src/test/java/cpf/pfw/common/runtime/PfwRuntimeWorkerEducationSampleTest.java`
- `pfw/src/test/java/cpf/pfw/common/archive/PfwArchiveAdvancedEducationSampleTest.java`
- `pfw/src/test/java/cpf/pfw/common/archive/PfwArchiveHistoryEducationSampleTest.java`

## 검증 결과 매트릭스

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | report/matrix/evidence 정합성 | 기존 EDU mapper DB slice 증적 유지 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_02/mariadb-full-install-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 MariaDB full install 증적 유지 |
| adm-runtime | 완료 | `specs/evidence/20260707_02/adm-runtime-smoke-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM runtime smoke 증적 유지 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_02/adm-permission-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 권한 runtime smoke 증적 유지 |
| openapi-runtime | 완료 | `specs/evidence/20260707_02/openapi-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 OpenAPI runtime smoke 증적 유지 |
| adm-browser-click | 미검증 | 없음 | report/matrix/evidence 정합성 | 이번 작업에서 브라우저 클릭 검증은 실행하지 않았습니다. |
| standard-header-e2e | 완료 | `specs/evidence/20260707_02/standard-header-e2e-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 표준 헤더 E2E 증적 유지 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 복합 거래 trace 증적 유지 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 segment log 증적 유지 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 목록 증적 유지 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM timeline 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260708_05/cmn-test.log` | report/matrix/evidence 정합성 | 기존 CMN fixed-length 테스트 증적 유지 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 composite runtime smoke 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 Kafka/MQ/Redis broker runtime은 실행하지 않았습니다. |
| broker-real-integration | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 broker 연동 검증은 외부 runtime 항목으로 남겼습니다. |
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
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260709_03/servicecall-contract-test.sanitized.json` | report/matrix/evidence 정합성 | Service Call registry/direct/LB/history/selectedInstanceId는 테스트 통과, 다중 HTTP runtime은 미검증 |
| adm-service-registry-runtime | 미검증 | 없음 | report/matrix/evidence 정합성 | 이번 작업에서 ADM service registry runtime 호출은 실행하지 않았습니다. |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | 실패는 없으나 CMN 기술 구현 2건이 PFW port migration 후보로 남았습니다. |
| spring-event-usage-scan | 완료 | `specs/evidence/20260709_03/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | Spring Event usage scan 통과 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260709_03/broker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | outbox/inbox/DLQ/replay/idempotency는 테스트 통과, 실 broker runtime은 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260709_03/filetransfer-contract-test.sanitized.json` | report/matrix/evidence 정합성 | LOCAL adapter/history/unknown/protocol plan은 테스트 통과, SFTP/FTP/SCP/SSH runtime은 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | report/matrix/evidence 정합성 | signature/encryption/key reference는 테스트 통과, Vault/KMS runtime은 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | worker control/heartbeat/ghost/admin DTO는 테스트 통과, 다중 instance runtime은 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | ADM 조회 후보 DTO 계약은 테스트 통과, ADM runtime API 연결은 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_03/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 profile loading 증적 유지 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | report/matrix/evidence 정합성 | 기존 packaged dependencies 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json` | report/matrix/evidence 정합성 | remote real deploy가 아니라 dry-run 계획 검증 |
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
| sample-coverage-matrix | 완료 | `specs/evidence/20260709_03/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | PFW catalog-only rows를 실제 sample/test/evidence로 전환하고 gate 통과 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260709_03/sample-placeholder-scan.sanitized.json` | report/matrix/evidence 정합성 | sample placeholder scan 통과 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260709_03/evidence-path-existence-check.sanitized.json` | report/matrix/evidence 정합성 | 리포트/인덱스/매트릭스 evidence path 검사 대상 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260709_03/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | 리포트/인덱스/매트릭스 정합성 검사 대상 |
| quality-gate | 완료 | `specs/evidence/20260709_03/quality-gate.log` | report/matrix/evidence 정합성 | qualityGate 실행 로그로 확인 |
| check-html-docs | 완료 | `specs/evidence/20260709_03/check-html-docs.log` | report/matrix/evidence 정합성 | HTML 문서 gate 실행 로그 |
| check-feature-evidence | 완료 | `specs/evidence/20260709_03/check-feature-evidence.log` | report/matrix/evidence 정합성 | feature evidence gate 실행 로그 |
| check-utf8 | 완료 | `specs/evidence/20260709_03/check-utf8-mojibake.log` | report/matrix/evidence 정합성 | UTF-8 및 mojibake 검사 통과 |


## 자동 검증 연결 마커

- MariaDB full install smoke: `scripts/smoke-mariadb-full-install.ps1`
- 표준 헤더 E2E smoke: `scripts/smoke-standard-header-e2e.ps1`
- XYZ EDU canonical fixture: `xyz_edu_query_fixture.sql`
- XYZ EDU mapper DB username env: `CPF_XYZ_EDU_MAPPER_DB_USERNAME`
- XYZ EDU mapper legacy DB user env 호환 키: `CPF_XYZ_EDU_MAPPER_DB_USER`
## 남은 리스크

- Java 25 런타임은 감지됐지만 Gradle 8.11.1 test task 구성에서 `Type T not present`로 실패했습니다. PFW 기능 테스트는 Java 21 런타임에서 통과했습니다.
- Kafka/MQ, SFTP/FTP/SCP/SSH, Vault/KMS, 다중 WAS runtime은 실제 외부 runtime을 실행하지 않았으므로 미검증입니다.
- architecture ownership scan은 실패는 없지만 CMN 파일/메시지 기술 구현 2건이 PFW port migration 후보로 남았습니다.
- TAR archive는 Java 표준 라이브러리만으로 구현하지 않고 부분 구현 후보로 남겼습니다.
