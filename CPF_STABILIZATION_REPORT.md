# CPF 안정화 작업 리포트

작성 시각: 2026-07-08 18:35:38 +09:00

## 작업 요약

- EDU 별도 배포 alias 제거, Gradle 배포 검증 기본 증적 경로를 `specs/evidence/20260708_04`로 정리했다.
- XYZ, BAT, CMN, EXS, PFW, ADM, BIZADM 교육 샘플 카탈로그와 단위 테스트를 추가했다.
- `specs/sample-coverage-matrix.md`를 생성해 192개 필수 sampleId의 source/test/evidence/status를 분리했다.
- 가비지 파일, 빈 디렉터리, local 포트, datasource mode, EDU alias, evidence path 존재 검사를 품질 게이트에 연결했다.
- 실제 remote deploy, 외부 WAS/JNDI, Kafka/MQ/Redis/SFTP/Vault, 브라우저 클릭, 신규 빈 MariaDB full install은 이번 로컬 검증 범위 밖이므로 완료로 주장하지 않는다.

## 검증 결과

| 명령 | 상태 | 증적 |
| --- | --- | --- |
| `:xyz:test` | 완료 | `specs/evidence/20260708_04/xyz-test.log` |
| `:bat:test` | 완료 | `specs/evidence/20260708_04/bat-test.log` |
| `:pfw:test` | 완료 | `specs/evidence/20260708_04/pfw-test.log` |
| `:cmn:test` | 완료 | `specs/evidence/20260708_04/cmn-test.log` |
| `:exs:test` | 완료 | `specs/evidence/20260708_04/exs-test.log` |
| `:adm:test` | 완료 | `specs/evidence/20260708_04/adm-test.log` |
| `:bizadm:test` | 완료 | `specs/evidence/20260708_04/bizadm-test.log` |
| `checkDeployEnv` | 완료 | `specs/evidence/20260708_04/deploy-env-acc-dev.sanitized.json` |
| `checkDeployInventory` | 완료 | `specs/evidence/20260708_04/deploy-inventory-acc-dev.sanitized.json` |
| `checkPackagedDependencies` | 완료 | `specs/evidence/20260708_04/packaged-dependencies-acc.sanitized.json` |
| `remoteDeployDryRun` | 부분 구현 | `specs/evidence/20260708_04/remote-deploy-dry-run.sanitized.json` |
| `qualityGate` | 완료 | `specs/evidence/20260708_04/quality-gate.log` |

참고: `qualityGate` 로그에는 로컬 MariaDB의 `cpf_pfw_app` 계정 접근 거부 WARN이 남아 있으나, 해당 테스트 흐름은 fail-open/로컬 검증 기준으로 통과했다. 신규 빈 MariaDB full install 재검증은 이번 완료 범위에 포함하지 않았다.

## 체크 결과

| check id | 상태 | 요약 |
| --- | --- | --- |
| edu-mapper-db-slice | 완료 | 기존 검증 증적 유지 |
| mariadb-full-install | 완료 | 기존 검증 증적 유지 |
| adm-runtime | 완료 | 기존 검증 증적 유지 |
| adm-permission-runtime | 완료 | 기존 검증 증적 유지 |
| openapi-runtime | 완료 | 기존 검증 증적 유지 |
| adm-browser-click | 미검증 | 실제 브라우저 클릭 증적 없음 |
| standard-header-e2e | 완료 | 기존 검증 증적 유지 |
| complex-transaction-trace | 완료 | 기존 검증 증적 유지 |
| transaction-segment-log | 완료 | 기존 검증 증적 유지 |
| adm-transaction-group-list | 완료 | 기존 검증 증적 유지 |
| adm-transaction-timeline | 완료 | 기존 검증 증적 유지 |
| cmn-fixed-length-engine | 완료 | 기존 검증 증적 유지 |
| composite-runtime-smoke | 완료 | 기존 검증 증적 유지 |
| adm-transaction-group-runtime | 완료 | 기존 검증 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 실제 broker 미실행 |
| broker-real-integration | 미검증 | Redis/Kafka/MQ 실연동 후속 |
| file-log-standard | 완료 | 기존 검증 증적 유지 |
| trace-boost-runtime | 완료 | 기존 검증 증적 유지 |
| bat-trace-boost-runtime | 완료 | 기존 검증 증적 유지 |
| runtime-start-services | 완료 | 기존 검증 증적 유지 |
| packaged-runtime-resources | 완료 | 기존 검증 증적 유지 |
| runtime-status-diagnostics | 완료 | 기존 검증 증적 유지 |
| runtime-closure | 완료 | 기존 검증 증적 유지 |
| adm-operation-console-runtime | 완료 | 기존 검증 증적 유지 |
| adm-log-policy-ui-static | 완료 | 기존 검증 증적 유지 |
| bat-log-bean-runtime | 완료 | 기존 검증 증적 유지 |
| exs-timeout-retry-runtime | 완료 | 기존 검증 증적 유지 |
| cmn-fixed-length-advanced | 완료 | 기존 검증 증적 유지 |
| create-domain-smoke | 완료 | 기존 검증 증적 유지 |
| pfw-service-call-engine | 부분 구현 | 다중 서비스 실제 HTTP runtime은 후속 |
| adm-service-registry-runtime | 미검증 | 실제 ADM RunRuntime 호출은 미수행 |
| architecture-ownership-scan | 재확인 필요 | CMN PFW port migration 후보를 warning으로 분류 |
| spring-event-usage-scan | 완료 | Spring Event 핵심 거래 흐름 남용 금지 |
| pfw-broker-capability | 부분 구현 | 실제 broker runtime은 미검증 |
| pfw-file-transfer-capability | 부분 구현 | 실제 SFTP/FTP/SSH runtime은 미검증 |
| pfw-security-credential-capability | 부분 구현 | 실제 Vault/KMS/HSM 연동은 미검증 |
| pfw-runtime-control-capability | 부분 구현 | 다중 instance runtime은 미검증 |
| pfw-admin-status-capability | 부분 구현 | ADM 실화면 연결은 후속 |
| profile-loading-standard | 완료 | PFW/CMN/업무 profile import 검사 |
| packaged-dependencies-check | 완료 | ACC bootJar 기준 PFW/CMN 포함 여부를 검사한다. |
| deploy-dry-run-standard | 부분 구현 | remote real deploy가 아니라 Gradle dry-run 계획 검증이다. |
| garbage-file-cleanup | 완료 | 구버전 profile/deploy script 잔여와 삭제/보류 목록을 새 증적으로 검증한다. |
| empty-directory-scan | 완료 | 빈 디렉터리 스캔과 삭제 결과를 기록한다. |
| deploy-env-standard | 완료 | ACC dev 기준 env 검증과 전체 env 스캔을 함께 사용한다. |
| deploy-inventory-standard | 완료 | ACC dev 기준 inventory 검증과 전체 inventory 스캔을 함께 사용한다. |
| gradle-deploy-task-standard | 완료 | Gradle 배포 task와 허용 모듈 목록을 검사한다. |
| datasource-mode-standard | 완료 | URL/JNDI mode와 빈 env 값을 검사한다. |
| local-port-duplicate-scan | 완료 | local profile 포트 중복을 검사한다. |
| edu-module-deploy-alias-scan | 완료 | EDU 별도 deploy/module alias 금지를 검사한다. |
| bat-edu-package | 완료 | BAT 교육 카탈로그와 tasklet 샘플 테스트로 검증한다. |
| bat-job-log-policy | 완료 | jobExecutionId와 centerCutExecutionId 로그 경로 정책을 검증한다. |
| sample-coverage-matrix | 완료 | 192개 필수 sampleId와 source/test 경로를 검사한다. |
| sample-placeholder-scan | 완료 | placeholder-only 샘플 금지 패턴을 검사한다. |
| evidence-path-existence-check | 완료 | 보고서와 매트릭스의 evidence path 실제 존재를 검사한다. |
| create-domain-profile-template | 완료 | 신규 도메인 생성 후보에 profile/env/inventory 템플릿 포함 |
| runtime-smoke-summary | 완료 | 기존 검증 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | report, evidence index, feature matrix의 상태 일치를 검사한다. |
| quality-gate | 완료 | Gradle qualityGate 전체 실행 로그다. |
| check-html-docs | 완료 | HTML 문서 구조와 상태 marker를 검사한다. |
| check-feature-evidence | 완료 | 기능 증적 연결 규칙을 검사한다. |
| check-utf8 | 완료 | UTF-8과 mojibake 패턴을 검사한다. |

## 실행/검증 기준

- 실행한 검증만 완료로 기록하고, 외부 runtime이 필요한 항목은 미검증 또는 부분 구현으로 분리한다.
- 문서, 소스, SQL, Swagger, EDU 샘플은 변경 시 함께 현행화한다.
- README는 진입점으로 유지하고 상세 검증은 `CPF_EVIDENCE_INDEX.md`, `CPF_GAP_MATRIX.md`, 기능 매트릭스, 샘플 커버리지 매트릭스로 연결한다.
- EDU DB mapper 검증 기준은 `xyz_edu_query_fixture.sql`, `CPF_XYZ_EDU_MAPPER_DB_USER`, `CPF_XYZ_EDU_MAPPER_DB_USERNAME` 참조를 유지한다.
- MariaDB 전체 설치 검증 스크립트는 `scripts/smoke-mariadb-full-install.ps1`이며, 표준 헤더 E2E 검증 스크립트는 `scripts/smoke-standard-header-e2e.ps1`이다.

## 보류/다음 작업

- sample matrix에서 `부분 구현` 또는 `미검증`인 항목은 실제 기능 샘플과 runtime smoke를 추가하며 단계적으로 완료 전환한다.
- remoteDeploy real mode, external WAS/JNDI, real broker, SFTP/FTP/SSH, Vault/KMS/HSM은 실제 인프라 연결 후 별도 증적으로 검증한다.
- MariaDB 신규 빈 DB full install은 현재 요청 범위의 로컬 코드 검증과 분리해 실 DB 초기화 가능 시 재검증한다.
