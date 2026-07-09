# CPF 안정화 작업 리포트

작성 시각: 2026-07-09 12:53:55 +09:00
기준 요청서: `CPF_NEW_REQUEST.md`
기준 최종 목표: `CPF_FINAL_TARGET_REQUIREMENTS.md`
기준 증적 디렉터리: `specs/evidence/20260708_05`

## 이번 작업 요약

- PFW archive/compression capability를 추가하고 ZIP/GZIP/checksum/zip-slip 테스트를 작성했다.
- BAT 24개, XYZ 21개, CMN 9개, EXS 10개, PFW 8개, ADM/BIZADM 9개, 총 81개 EducationSample 계열을 실제 class/test로 보강했다.
- `specs/sample-coverage-matrix.md`를 228개 sampleId 기준으로 갱신하고 CoverageCatalog 완료 근거를 금지했다.
- 외부 Kafka/MQ/SFTP/SSH/Vault/WAS runtime은 완료로 기록하지 않고 `미검증` 또는 `부분 구현`으로 분리했다.

## 기존 검증 기준 유지

- EDU Mapper DB slice fixture는 `xyz_edu_query_fixture.sql`을 기준으로 유지한다.
- EDU Mapper DB 환경변수는 신규 기준 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`과 기존 호환 `CPF_XYZ_EDU_MAPPER_DB_USER`를 함께 문서화한다.
- MariaDB 전체 설치 smoke는 `scripts/smoke-mariadb-full-install.ps1`로 수행한다.
- 표준 헤더 E2E smoke는 `scripts/smoke-standard-header-e2e.ps1`로 수행한다.

## 검증 상태

| check id | 상태 | 증적 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | 기존 DB slice 검증 증적 유지 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_02/mariadb-full-install-result.sanitized.json` | MariaDB 전체 설치 검증 기존 증적 유지 |
| adm-runtime | 완료 | `specs/evidence/20260707_02/adm-runtime-smoke-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_02/adm-permission-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| openapi-runtime | 완료 | `specs/evidence/20260707_02/openapi-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| adm-browser-click | 미검증 | 없음 | 이번 작업 범위에서 브라우저 실제 클릭은 실행하지 않았다. |
| standard-header-e2e | 완료 | `specs/evidence/20260707_02/standard-header-e2e-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| transaction-segment-log | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260708_05/cmn-test.log` | CMN fixed-length EDU 샘플 테스트 통과 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| redis-kafka-mq-broker | 미검증 | 없음 | Kafka/MQ 실 broker runtime은 이번 작업에서 실행하지 않았다. |
| broker-real-integration | 미검증 | 없음 | Kafka/MQ 실 broker runtime은 이번 작업에서 실행하지 않았다. |
| file-log-standard | 완료 | `specs/evidence/20260707_02/file-log-standard-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_02/trace-boost-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_02/bat-trace-boost-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| runtime-start-services | 완료 | `specs/evidence/20260707_02/runtime-start-services-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_02/packaged-runtime-resource-check.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_02/runtime-status-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| runtime-closure | 완료 | `specs/evidence/20260707_02/runtime-closure-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_02/adm-log-policy-ui-static-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_02/bat-log-bean-runtime-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260708_05/exs-test.log` | EXS retry/unknown/reconciliation source/contract 테스트 통과 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260708_05/cmn-test.log` | CMN fixed-length EDU 샘플 테스트 통과 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_02/create-domain-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260708_05/pfw-test.log` | PFW Service Call 샘플 contract 테스트 통과, 다중 HTTP runtime은 미실행 |
| adm-service-registry-runtime | 미검증 | 없음 | ADM 서비스 레지스트리 runtime 호출은 이번 작업에서 실행하지 않았다. |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260708_02/architecture-ownership-scan.sanitized.json` | ownership 경계는 유지하되 기존 경고는 후속 재확인 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260708_02/spring-event-usage-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260708_05/pfw-test.log` | PFW broker port/envelope source contract 검증, 실 broker runtime 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260708_05/pfw-test.log` | PFW file transfer request/result source contract 검증, SFTP/SSH runtime 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260708_05/pfw-test.log` | secret 원문 미노출 reference contract 검증, Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260708_05/pfw-test.log` | runtime 상태 DTO/source smoke 검증, 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260708_05/pfw-test.log` | runtime 상태 DTO/source smoke 검증, 다중 instance runtime 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_03/profile-loading-result.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json`, `specs/evidence/20260708_05/remote-deploy-dry-run-acc-dev.sanitized.json` | remote real deploy가 아니라 dry-run 계획 검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json`, `specs/evidence/20260708_05/deleted-files.sanitized.json`, `specs/evidence/20260708_05/retained-review-required-files.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| empty-directory-scan | 완료 | `specs/evidence/20260708_05/empty-directory-scan.sanitized.json`, `specs/evidence/20260708_05/deleted-empty-directories.sanitized.json`, `specs/evidence/20260708_05/retained-empty-directories.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| deploy-env-standard | 완료 | `specs/evidence/20260708_05/deploy-env-acc-dev.sanitized.json`, `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| deploy-inventory-standard | 완료 | `specs/evidence/20260708_05/deploy-inventory-acc-dev.sanitized.json`, `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260708_05/gradle-remote-deploy-task-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| datasource-mode-standard | 완료 | `specs/evidence/20260708_05/datasource-mode-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260708_05/local-port-duplicate-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260708_05/edu-module-deploy-alias-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| bat-edu-package | 완료 | `specs/evidence/20260708_05/bat-test.log`, `specs/evidence/20260708_05/bat-edu-actual-sample-scan.sanitized.json` | BAT 실제 EducationSample/Test 15개 이상 검증 |
| bat-job-log-policy | 완료 | `specs/evidence/20260708_05/bat-test.log` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| sample-coverage-matrix | 완료 | `specs/evidence/20260708_05/sample-coverage-result.sanitized.json`, `specs/evidence/20260708_05/sample-actual-implementation-scan.sanitized.json` | 228개 sampleId와 실제 sample/test 경로 검증 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260708_05/sample-placeholder-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| evidence-path-existence-check | 완료 | `specs/evidence/20260708_05/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260708_05/report-evidence-log-existence.sanitized.json`, `specs/evidence/20260708_05/evidence-index-consistency.sanitized.json` | 리포트/인덱스/매트릭스가 참조하는 증적 파일 존재를 확인한다. |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260708_05/report-matrix-evidence-consistency.sanitized.json` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| quality-gate | 완료 | `specs/evidence/20260708_05/quality-gate.log` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| check-html-docs | 완료 | `specs/evidence/20260708_05/check-html-docs.log` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| check-feature-evidence | 완료 | `specs/evidence/20260708_05/check-feature-evidence.log` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |
| check-utf8 | 완료 | `specs/evidence/20260708_05/check-utf8-mojibake.log` | 이번 작업 증적 또는 기존 유지 증적으로 확인한다. |

## 남은 리스크

- Kafka/MQ/SFTP/SSH/Vault/KMS/외부 WAS/JNDI는 실제 외부 runtime이 없어 이번 작업에서는 미검증이다.
- PFW TAR archive는 외부 라이브러리를 무리하게 추가하지 않고 후보/부분 구현으로 남겼다.
- 일부 PFW service registry/call history 고급 sampleId는 CoverageCatalog 추적 상태로 남아 후속 실제 샘플 전환 대상이다.
