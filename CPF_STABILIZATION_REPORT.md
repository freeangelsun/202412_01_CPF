# CPF 안정화 리포트

작성 시각: 2026-07-08 18:00:02 +09:00

## 수행 요약

- 레거시 profile 파일과 직접 배포 ps1/sh를 제거하고 Gradle 배포 검증 태스크로 표준화했습니다.
- `deploy/env`와 `deploy/inventory`에 local/dev/stg/prod, URL/JNDI datasource mode, runtimeMode 기준을 반영했습니다.
- BAT EDU 샘플과 Job 실행 단위 로그 경로 정책을 추가하고 테스트했습니다.
- README, Gap Matrix, Evidence Index, 기능 구현 매트릭스를 현재 기준으로 맞췄습니다.

## 검증 상태

| check id | 상태 | 증적 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs\evidence\20260707_01\edu-mapper-db-slice.log` | 기존 검증 증적 유지 |
| mariadb-full-install | 완료 | `specs\evidence\20260707_02\mariadb-full-install-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-runtime | 완료 | `specs\evidence\20260707_02\adm-runtime-smoke-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-permission-runtime | 완료 | `specs\evidence\20260707_02\adm-permission-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| openapi-runtime | 완료 | `specs\evidence\20260707_02\openapi-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-browser-click | 미검증 | `specs\evidence\20260707_02\adm-ui-browser-smoke-result.sanitized.json` | 실제 브라우저 클릭 증적 없음 |
| standard-header-e2e | 완료 | `specs\evidence\20260707_02\standard-header-e2e-result.sanitized.json` | 기존 검증 증적 유지 |
| complex-transaction-trace | 완료 | `specs\evidence\20260707_02\composite-transaction-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| transaction-segment-log | 완료 | `specs\evidence\20260707_02\composite-transaction-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-transaction-group-list | 완료 | `specs\evidence\20260707_02\adm-operation-console-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-transaction-timeline | 완료 | `specs\evidence\20260707_02\adm-transaction-group-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs\evidence\20260707_02\cmn-fixed-length-advanced-result.sanitized.json` | 기존 검증 증적 유지 |
| composite-runtime-smoke | 완료 | `specs\evidence\20260707_02\composite-transaction-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs\evidence\20260707_02\adm-transaction-group-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | 실제 broker 미실행 |
| broker-real-integration | 미검증 | 없음 | Redis/Kafka/MQ 실연동 후속 |
| file-log-standard | 완료 | `specs\evidence\20260707_02\file-log-standard-result.sanitized.json` | 기존 검증 증적 유지 |
| trace-boost-runtime | 완료 | `specs\evidence\20260707_02\trace-boost-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| bat-trace-boost-runtime | 완료 | `specs\evidence\20260707_02\bat-trace-boost-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| runtime-start-services | 완료 | `specs\evidence\20260707_02\runtime-start-services-result.sanitized.json` | 기존 검증 증적 유지 |
| packaged-runtime-resources | 완료 | `specs\evidence\20260707_02\packaged-runtime-resource-check.sanitized.json` | 기존 검증 증적 유지 |
| runtime-status-diagnostics | 완료 | `specs\evidence\20260707_02\runtime-status-result.sanitized.json` | 기존 검증 증적 유지 |
| runtime-closure | 완료 | `specs\evidence\20260707_02\runtime-closure-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-operation-console-runtime | 완료 | `specs\evidence\20260707_02\adm-operation-console-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| adm-log-policy-ui-static | 완료 | `specs\evidence\20260707_02\adm-log-policy-ui-static-result.sanitized.json` | 기존 검증 증적 유지 |
| bat-log-bean-runtime | 완료 | `specs\evidence\20260707_02\bat-log-bean-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| exs-timeout-retry-runtime | 완료 | `specs\evidence\20260707_02\exs-timeout-retry-runtime-result.sanitized.json` | 기존 검증 증적 유지 |
| cmn-fixed-length-advanced | 완료 | `specs\evidence\20260707_02\cmn-fixed-length-advanced-result.sanitized.json` | 기존 검증 증적 유지 |
| create-domain-smoke | 완료 | `specs\evidence\20260707_02\create-domain-result.sanitized.json` | 기존 검증 증적 유지 |
| pfw-service-call-engine | 부분 구현 | `specs\evidence\20260708_02\service-call-engine-runtime-success.sanitized.json`, `specs\evidence\20260708_03\pfw-service-call-engine-test.log` | 다중 서비스 실제 HTTP runtime은 후속 |
| adm-service-registry-runtime | 미검증 | `specs\evidence\20260708_03\adm-service-registry-api-test.log`, `specs\evidence\20260708_02\adm-service-registry-ui-static-smoke.sanitized.json` | 실제 ADM RunRuntime 호출은 미수행 |
| architecture-ownership-scan | 재확인 필요 | `specs\evidence\20260708_02\architecture-ownership-scan.sanitized.json` | CMN PFW port migration 후보를 warning으로 분류 |
| spring-event-usage-scan | 완료 | `specs\evidence\20260708_02\spring-event-usage-scan.sanitized.json` | Spring Event 핵심 거래 흐름 남용 금지 |
| pfw-broker-capability | 부분 구현 | `specs\evidence\20260708_03\pfw-capability-contract-test.log` | 실제 broker runtime은 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs\evidence\20260708_03\pfw-capability-contract-test.log` | 실제 SFTP/FTP/SSH runtime은 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs\evidence\20260708_03\pfw-capability-contract-test.log` | 실제 Vault/KMS/HSM 연동은 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs\evidence\20260708_03\pfw-capability-contract-test.log` | 다중 instance runtime은 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs\evidence\20260708_03\pfw-capability-contract-test.log` | ADM 실화면 연결은 후속 |
| profile-loading-standard | 완료 | `specs\evidence\20260708_03\profile-loading-result.sanitized.json` | PFW/CMN/업무 profile import 검사 |
| packaged-dependencies-check | 완료 | `specs\evidence\20260708_03\packaged-dependencies-acc.sanitized.json` | ACC bootJar 기준 PFW/CMN 포함 점검 |
| deploy-dry-run-standard | 부분 구현 | `specs\evidence\20260708_03\remote-deploy-dry-run-acc-dev.sanitized.json` | Gradle dry-run 기준. 실제 원격 배포는 미검증 |
| garbage-file-cleanup | 완료 | `specs\evidence\20260708_03\garbage-file-scan.sanitized.json`, `specs\evidence\20260708_03\deleted-files.sanitized.json` | 레거시 profile/deploy script 삭제와 잔존 파일 검사 |
| deploy-env-standard | 완료 | `specs\evidence\20260708_03\deploy-env-acc-dev.sanitized.json` | local/dev/stg/prod env 필수 key, 빈 값, datasource mode 검사 |
| deploy-inventory-standard | 완료 | `specs\evidence\20260708_03\deploy-inventory-acc-dev.sanitized.json` | runtimeMode, 승인, rollback 필드 검사 |
| gradle-deploy-task-standard | 완료 | `specs\evidence\20260708_03\quality-gate.log` | checkDeployEnv/checkDeployInventory/checkPackagedDependencies/remoteDeployDryRun 표준화 |
| datasource-mode-standard | 완료 | `specs\evidence\20260708_03\garbage-file-scan.sanitized.json` | URL/JNDI datasource mode 전환 key 검사 |
| bat-edu-package | 완료 | `specs\evidence\20260708_03\bat-test.log` | BAT EDU 패키지와 학습용 tasklet 샘플 테스트 |
| bat-job-log-policy | 완료 | `specs\evidence\20260708_03\bat-test.log` | jobExecutionId/centerCutExecutionId 단위 로그 경로 정책 테스트 |
| create-domain-profile-template | 완료 | `specs\evidence\20260708_03\garbage-file-scan.sanitized.json` | 신규 도메인 생성 후보에 profile/env/inventory 템플릿 포함 |
| runtime-smoke-summary | 완료 | `specs\evidence\20260707_02\runtime-smoke-summary.sanitized.json` | 기존 검증 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs\evidence\20260708_03\quality-gate.log` | report/matrix/evidence 정합성 gate |
| quality-gate | 완료 | `specs\evidence\20260708_03\quality-gate.log` | 이번 작업 최종 gate |
| check-html-docs | 완료 | `specs\evidence\20260708_03\check-html-docs.log` | HTML 문서 구조 검사 |
| check-feature-evidence | 완료 | `specs\evidence\20260708_03\check-feature-evidence.log` | 기능 증적 검사 |
| check-utf8 | 완료 | `specs\evidence\20260708_03\check-utf8-mojibake.log` | UTF-8/mojibake 검사 |

## 미검증/보류

- 실제 원격 서버 stop/start/health/rollback 배포는 이번 작업에서 수행하지 않았고 `remoteDeployDryRun` 증적으로만 확인했습니다.
- 실제 Redis/Kafka/MQ broker, SFTP/FTP, Vault/KMS/HSM runtime 검증은 외부 환경이 필요해 미검증 또는 부분 구현으로 유지했습니다.
- 실제 브라우저 클릭 기반 ADM UI 검증과 실제 MariaDB 재설치 검증은 이번 실행 범위에서 제외했습니다.
- 실제 MariaDB 전체 설치 검증은 `scripts/smoke-mariadb-full-install.ps1`로 수행해야 하며, 이번 작업에서는 재실행하지 않았습니다.
- 표준 헤더 E2E runtime 검증은 `scripts/smoke-standard-header-e2e.ps1`로 수행해야 하며, 이번 작업에서는 재실행하지 않았습니다.
- `qualityGate` 중 ACC 테스트 컨텍스트에서 `cpf_pfw_app` DB 접속 거부 WARN이 출력됐지만 테스트 실패로 이어지지는 않았습니다. 실제 DB 계정 권한 검증은 별도 MariaDB smoke에서 확인해야 합니다.
- EDU mapper DB fixture 기준 파일은 `xyz_edu_query_fixture.sql`이며, 환경 변수 호환성 확인 항목은 `CPF_XYZ_EDU_MAPPER_DB_USER`와 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`입니다.

## 항상 지켜야 할 기준 확인

- 문서와 소스, SQL, Swagger, EDU 샘플은 같은 check id와 증적 기준으로 연결합니다.
- README는 진입점으로 유지하고 상세 내용은 `specs/*.html` 가이드로 연결합니다.
- 기능 변경 시 README, 개발/관리자/구성/SQL 가이드, EDU 샘플, 매트릭스, 증적 인덱스를 함께 현행화합니다.
- 실행하지 않은 검증은 성공으로 보고하지 않습니다.