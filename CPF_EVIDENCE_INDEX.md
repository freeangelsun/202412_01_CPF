# CPF 증적 인덱스

작성 시각: 2026-07-13 19:20 KST

기준 증적 디렉터리: `specs/evidence/20260713_03`

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | report/matrix/evidence 정합성 | 기존 EDU mapper DB slice 증적 유지 |
| mariadb-full-install | 미검증 | `specs/evidence/20260713_03/mariadb-preflight.sanitized.json` | report/matrix/evidence 정합성 | MariaDB 서비스와 CLI는 확인했으나 현재 프로세스에 인증정보가 없어 설치 SQL·Flyway 미실행 |
| adm-runtime | 부분 구현 | `specs/evidence/20260713_03/java21-full-test.sanitized.json`, `specs/evidence/20260713_03/java21-all-bootjar.sanitized.json` | report/matrix/evidence 정합성 | 테스트와 bootJar는 통과했으나 변경 API를 포함한 앱 runtime 미실행 |
| adm-permission-runtime | 부분 구현 | `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | 권한 매핑 테스트는 통과했으나 권한별 200/403 runtime 미실행 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260713_03/openapi-source-coverage.sanitized.json` | report/matrix/evidence 정합성 | operationId 소스 검사는 통과, 현재 앱 `/v3/api-docs` 미실행 |
| adm-browser-click | 미검증 | `specs/evidence/20260713_02/adm-browser-click.sanitized.log` | report/matrix/evidence 정합성 | ADM 앱·DB를 기동한 브라우저 클릭 미실행 |
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
| file-log-standard | 부분 구현 | `specs/evidence/20260713_03/log-management-standard.sanitized.json`, `specs/evidence/20260713_03/runtime-log-manifest.sanitized.json`, `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | 실제 파일·두 인스턴스·거래별 JSON Lines 통과, 전체 앱과 공유 storage runtime 미검증 |
| trace-boost-runtime | 미검증 | 없음 | report/matrix/evidence 정합성 | 절대 로그 root로 변경한 runtime 스크립트 미실행 |
| bat-trace-boost-runtime | 미검증 | 없음 | report/matrix/evidence 정합성 | 환경 포함 BAT 로그 경로로 변경한 runtime 미실행 |
| runtime-start-services | 미검증 | 없음 | report/matrix/evidence 정합성 | 식별자 주입 정적 검사는 통과했으나 전체 서비스 기동 미실행 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260713_03/packaged-runtime-resource-check.sanitized.json` | report/matrix/evidence 정합성 | 7개 실행 모듈 bootJar 내부 리소스 검사 통과 |
| runtime-status-diagnostics | 미검증 | 없음 | report/matrix/evidence 정합성 | 변경된 인스턴스 경로 기준 status 명령 미실행 |
| runtime-closure | 미검증 | 없음 | report/matrix/evidence 정합성 | 변경된 공통 스크립트 기준 종료·정리 smoke 미실행 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM operation console runtime 증적 유지 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_02/adm-log-policy-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM log policy UI static 증적 유지 |
| bat-log-bean-runtime | 미검증 | 없음 | report/matrix/evidence 정합성 | PFW 단일 경로 정책으로 통합한 BAT bean 앱 runtime 미실행 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260708_05/exs-test.log` | report/matrix/evidence 정합성 | 기존 EXS timeout/retry 테스트 증적 유지 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260708_05/cmn-test.log` | report/matrix/evidence 정합성 | 기존 CMN advanced fixed-length 테스트 증적 유지 |
| create-domain-smoke | 완료 | `specs/evidence/20260713_02/create-domain-smoke.sanitized.log` | report/matrix/evidence 정합성 | 최신 로그 템플릿을 포함한 LNG 생성·test·bootJar 실제 통과 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | 현재 PFW 테스트 통과, 다중 HTTP runtime 미검증 |
| adm-service-registry-runtime | 완료 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM service registry runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260713_03/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | 오류 0건, CMN 기술 실행 후보 2건은 PFW migration 추적 필요 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260713_03/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | 허용 5건, 검토·금지 0건으로 통과 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | worker 테스트 통과, 실 broker·JDBC 재기동 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | LOCAL 보안·checksum 테스트 통과, 외부 protocol·JDBC 재기동 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | report/matrix/evidence 정합성 | Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | recovery API와 권한 테스트 통과, 실제 DB runtime·브라우저 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260713_03/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | local/dev/stg/prod profile 정적 계약 통과 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | report/matrix/evidence 정합성 | 기존 packaged dependencies 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json` | report/matrix/evidence 정합성 | dry-run 계획 검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260713_03/garbage-file-scan.sanitized.json`, `specs/evidence/20260713_03/legacy-log-cleanup-after.sanitized.json`, `specs/evidence/20260713_03/final-generated-log-cleanup.sanitized.json` | report/matrix/evidence 정합성 | 기존 비표준 로그 823개와 검증 생성 raw 로그 28개 삭제 후 상태 확인 |
| empty-directory-scan | 완료 | `specs/evidence/20260713_03/empty-directory-scan.sanitized.json` | report/matrix/evidence 정합성 | 지정 경로 빈 디렉터리 0건 |
| deploy-env-standard | 완료 | `specs/evidence/20260708_05/deploy-env-acc-dev.sanitized.json` | report/matrix/evidence 정합성 | 기존 deploy env 증적 유지 |
| deploy-inventory-standard | 완료 | `specs/evidence/20260708_05/deploy-inventory-acc-dev.sanitized.json` | report/matrix/evidence 정합성 | 기존 deploy inventory 증적 유지 |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260708_05/gradle-remote-deploy-task-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 Gradle deploy task scan 증적 유지 |
| datasource-mode-standard | 완료 | `specs/evidence/20260708_05/datasource-mode-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 datasource mode scan 증적 유지 |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260708_05/local-port-duplicate-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 local port duplicate scan 증적 유지 |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260708_05/edu-module-deploy-alias-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 EDU deploy alias scan 증적 유지 |
| bat-edu-package | 완료 | `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | BAT EDU와 전체 모듈 테스트 통과 |
| bat-job-log-policy | 완료 | `specs/evidence/20260713_03/java21-full-test.sanitized.json` | report/matrix/evidence 정합성 | 환경·JobInstance·restart·업무일자 정책 테스트 통과 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260713_03/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | PFW recovery와 XYZ/BAT 로그 EDU 포함 coverage 통과 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260713_03/sample-placeholder-scan.sanitized.json` | report/matrix/evidence 정합성 | sample placeholder scan 통과 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260713_03/evidence-path-existence-check.sanitized.json` | report/matrix/evidence 정합성 | 증적 존재·정제 확장자·현재 untracked commit 대상 포함 검사 통과 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260713_03/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | 61개 check ID 상태 일치 검사 통과 |
| quality-gate | 완료 | `specs/evidence/20260713_03/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | Java 21 BUILD SUCCESSFUL, 66 tasks, JUnit 308건·실패·오류 0 |
| check-html-docs | 완료 | `specs/evidence/20260713_03/static-quality-gates.sanitized.log` | report/matrix/evidence 정합성 | HTML 구조와 상태표 검사 통과 |
| check-feature-evidence | 완료 | `specs/evidence/20260713_03/static-quality-gates.sanitized.log` | report/matrix/evidence 정합성 | 필수 source·SQL·문서·샘플 검사 통과 |
| check-utf8 | 완료 | `specs/evidence/20260713_03/static-quality-gates.sanitized.log` | report/matrix/evidence 정합성 | UTF-8 및 mojibake gate 통과 |

## 추가 작업 증적

| 증적 | 상태 | 내용 |
| --- | --- | --- |
| `specs/evidence/20260713_03/runtime-log-manifest.sanitized.json` | 완료 | 실제 로그 파일 8개, 두 인스턴스, 거래별 JSON Lines·필수 필드·마스킹 검사 |
| `specs/evidence/20260713_03/java21-full-test.sanitized.json` | 완료 | 166 suites, 308 tests, 실패·오류 0, 건너뜀 4 |
| `specs/evidence/20260713_03/java21-all-bootjar.sanitized.json` | 완료 | 실행 모듈 7개 bootJar와 SHA-256 |
| `specs/evidence/20260713_03/java25-pfw-test.sanitized.json` | 실패 | Java 25.0.3에서 Gradle 8.11.1 Test task 생성 단계 `Type T not present` |
| `specs/evidence/20260713_03/mariadb-preflight.sanitized.json` | 미검증 | 서비스·CLI는 확인, 인증정보 미주입으로 SQL·Flyway 미실행 |
| `specs/evidence/20260713_03/legacy-log-inventory-before-cleanup.sanitized.json` | 완료 | 기존 비표준 로그 823개, 약 162 MiB 정리 전 목록 |
| `specs/evidence/20260713_03/legacy-log-cleanup-after.sanitized.json` | 완료 | `logs`, `acc/logs`, `mbr/logs` 정리 후 상태 |
| `specs/evidence/20260713_03/final-generated-log-cleanup.sanitized.json` | 완료 | qualityGate 생성 raw 로그 28개, 3,570,935 bytes 삭제 후 로그 디렉터리 0건 |
