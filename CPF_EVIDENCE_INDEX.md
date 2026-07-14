# CPF 증적 인덱스

작성 시각: 2026-07-14 13:02 KST

기준 증적 디렉터리: `specs/evidence/20260714_01`

| check id | 상태 | 증적 | 확인 기준 | 비고 |
| --- | --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | report/matrix/evidence 정합성 | 기존 EDU mapper DB slice 증적 유지 |
| mariadb-full-install | 미검증 | `specs/evidence/20260714_01/mariadb-preflight.sanitized.log`, `specs/evidence/20260714_01/mariadb-full-install.sanitized.log` | report/matrix/evidence 정합성 | 서비스·CLI·포트는 확인했으나 인증정보 환경변수가 없어 설치 SQL·Flyway 미실행 |
| adm-runtime | 부분 구현 | `specs/evidence/20260714_01/java21-full-test-final.sanitized.log`, `specs/evidence/20260714_01/java21-seven-bootjar-final.sanitized.log` | report/matrix/evidence 정합성 | 소스·테스트·bootJar는 확인했으나 DB 연결 앱 runtime 미실행 |
| adm-permission-runtime | 부분 구현 | `specs/evidence/20260714_01/java21-full-test-final.sanitized.log` | report/matrix/evidence 정합성 | 서버 권한 테스트는 실행됐으나 권한별 200/403 runtime 미실행 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260714_01/openapi-source-coverage.sanitized.json` | report/matrix/evidence 정합성 | mapping 297개·명시 operationId 319개·중복 0건, `/v3/api-docs` runtime 미실행 |
| adm-browser-click | 미검증 | `specs/evidence/20260713_02/adm-browser-click.sanitized.log` | report/matrix/evidence 정합성 | ADM 앱·DB를 기동한 브라우저 클릭 미실행 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_02/standard-header-e2e-result.sanitized.json` | report/matrix/evidence 정합성 | scripts/smoke-standard-header-e2e.ps1 기준 기존 증적 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 복합 거래 trace 증적 유지 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | report/matrix/evidence 정합성 | 기존 runtime과 신규 durable segment fallback·recovery 계약 확인 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 목록 증적 유지 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM timeline 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260708_05/cmn-test.log` | report/matrix/evidence 정합성 | 기존 CMN fixed-length 테스트 증적 유지 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 composite runtime smoke 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM 거래 그룹 runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 Redis/Kafka/MQ broker runtime 미실행 |
| broker-real-integration | 미검증 | 없음 | report/matrix/evidence 정합성 | 실 broker adapter와 장애 fallback runtime 미실행 |
| file-log-standard | 부분 구현 | `specs/evidence/20260713_03/runtime-log-manifest.sanitized.json`, `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | report/matrix/evidence 정합성 | 업무일자 압축·지연 기록 복원과 기존 JSON Lines runtime 확인, 전체 다중 서버 runtime 미검증 |
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
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | report/matrix/evidence 정합성 | attempt segment·retry·failover·unknown 계약 테스트 통과, 다중 HTTP runtime 미검증 |
| adm-service-registry-runtime | 완료 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 기존 ADM service registry runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260714_01/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | 위반 0건, CMN 기술 실행 호환 후보 2건은 PFW migration 추적 필요 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260714_01/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | 허용 5건, 검토·금지 0건으로 통과 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | report/matrix/evidence 정합성 | claim lease·backoff·DLQ·actual replay 테스트 통과, 실 broker·DB 재기동 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | report/matrix/evidence 정합성 | 결정적 원격 protocol reference adapter 계약 통과, 실 protocol 서버 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | report/matrix/evidence 정합성 | Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | report/matrix/evidence 정합성 | 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | report/matrix/evidence 정합성 | PFW 공개 recovery·timeline facade와 ADM 경계 테스트 통과, DB runtime·브라우저 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260714_01/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | local/dev/stg/prod profile 정적 계약 통과 |
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
| bat-edu-package | 완료 | `specs/evidence/20260714_01/edu-sample-final.sanitized.log` | report/matrix/evidence 정합성 | XYZ/BAT EDU 47개 클래스·53개 테스트·건너뜀 0건 |
| bat-job-log-policy | 완료 | `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | report/matrix/evidence 정합성 | JobInstance 파일·DB lease·stale takeover·fragment degraded mode 테스트 통과 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260714_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260714_01/edu-sample-final.sanitized.log` | report/matrix/evidence 정합성 | XYZ/BAT 소유권 기준 47개 샘플·테스트·증적 경로 일치 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260713_03/sample-placeholder-scan.sanitized.json` | report/matrix/evidence 정합성 | sample placeholder scan 통과 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260714_01/evidence-path-existence-check.sanitized.json` | report/matrix/evidence 정합성 | 증적 존재·정제 확장자·현재 untracked manifest 검사 대상 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | report/matrix/evidence 정합성 | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | report/matrix/evidence 정합성 | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260714_01/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | 61개 check ID 상태 일치 검사 대상 |
| quality-gate | 미검증 | `specs/evidence/20260714_01/quality-gate-gradle910-java21-release21.sanitized.log`, `specs/evidence/20260714_01/quality-gate-gradle910-java25-release21.sanitized.log` | report/matrix/evidence 정합성 | Java 21/25 모두 76 tasks, JUnit 275건·실패·오류 0·건너뜀 4이므로 전체 완료 선언 보류 |
| check-html-docs | 완료 | `specs/evidence/20260714_01/static-gates-final.sanitized.log` | report/matrix/evidence 정합성 | HTML 구조와 상태표를 최종 qualityGate에서 재검증 |
| check-feature-evidence | 완료 | `specs/evidence/20260714_01/static-gates-final.sanitized.log` | report/matrix/evidence 정합성 | 필수 source·SQL·문서·샘플을 최종 qualityGate에서 재검증 |
| check-utf8 | 완료 | `specs/evidence/20260714_01/static-gates-final.sanitized.log` | report/matrix/evidence 정합성 | UTF-8 및 mojibake gate 통과 |

## 추가 작업 증적

| 증적 | 상태 | 내용 |
| --- | --- | --- |
| `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log` | 완료 | 신뢰성 핵심 50건, 실패·오류·건너뜀 0 |
| `specs/evidence/20260714_01/edu-sample-final.sanitized.log` | 완료 | XYZ/BAT EDU 53건, 실패·오류·건너뜀 0 |
| `specs/evidence/20260714_01/java21-seven-bootjar-final.sanitized.log` | 완료 | 실행 모듈 7개 bootJar 성공 |
| `specs/evidence/20260714_01/java21-full-test-final.sanitized.log` | 미검증 | 전체 275건 중 환경형 4건 건너뜀 |
| `specs/evidence/20260714_01/quality-gate-final.sanitized.log` | 미검증 | 명령은 성공했으나 전체 275건 중 환경형 4건 건너뜀 |
| `specs/evidence/20260714_01/java25-gradle89-pfw-test-final.sanitized.log` | 실패 | 현재 wrapper 8.9가 Java 25 class file major version 69를 지원하지 않아 테스트 전 실패 |
| `specs/evidence/20260714_01/quality-gate-gradle910-java21-release21.sanitized.log` | 미검증 | Java 21, Gradle 9.1.0, release 21 전체 gate 성공, 환경형 4건 건너뜀 |
| `specs/evidence/20260714_01/quality-gate-gradle910-java25-release21.sanitized.log` | 미검증 | Java 25, Gradle 9.1.0, release 21 전체 gate 성공, 환경형 4건 건너뜀 |
| `specs/evidence/20260714_01/quality-gate-final-postfix-java25.sanitized.log` | 미검증 | 최종 문서·도구 체인 보강 후 Java 25 qualityGate 재실행 성공, 환경형 4건 건너뜀 |
| `specs/evidence/20260714_01/java25-gradle910-lombok-compat-failure.sanitized.log` | 실패 | Gradle 9.1.0 전환 후 구형 Lombok의 javac 25 비호환을 재현한 정제 증적 |
| `specs/evidence/20260714_01/java25-gradle910-bytebuddy-compat-failure.sanitized.log` | 실패 | Lombok 보강 후 구형 Byte Buddy의 Java 25 instrumentation 비호환을 재현한 정제 증적 |
| `specs/evidence/20260714_01/quality-gate-manifest-format-failure.sanitized.log` | 실패 | 최종 manifest 표준 메타데이터 누락을 탐지한 최초 qualityGate 실패 증적 |
| `specs/evidence/20260714_01/seven-bootjar-gradle910-java25-release21.sanitized.log` | 완료 | Java 25에서 실행 모듈 7개 bootJar 성공 |
| `specs/evidence/20260714_01/seven-bootjar-class-version.sanitized.log` | 완료 | 7개 bootJar의 Application class major 65(Java 21) 확인 |
| `specs/evidence/20260714_01/gradle9-remote-deploy-warning-check.sanitized.log` | 완료 | remoteDeployDryRun을 warning-mode all로 실행해 Task.project deprecation 제거 확인 |
| `specs/evidence/20260714_01/final-worktree-manifest.sanitized.log` | 완료 | 최종 변경·삭제·신규 경로와 SHA-256 manifest |
| `specs/evidence/20260713_03/runtime-log-manifest.sanitized.json` | 완료 | 실제 로그 파일 8개, 두 인스턴스, 거래별 JSON Lines·필수 필드·마스킹 검사 |
| `specs/evidence/20260713_03/java21-full-test.sanitized.json` | 완료 | 166 suites, 308 tests, 실패·오류 0, 건너뜀 4 |
| `specs/evidence/20260713_03/java21-all-bootjar.sanitized.json` | 완료 | 실행 모듈 7개 bootJar와 SHA-256 |
| `specs/evidence/20260713_03/java25-pfw-test.sanitized.json` | 실패 | Java 25.0.3에서 Gradle 8.11.1 Test task 생성 단계 `Type T not present` |
| `specs/evidence/20260713_03/mariadb-preflight.sanitized.json` | 미검증 | 서비스·CLI는 확인, 인증정보 미주입으로 SQL·Flyway 미실행 |
| `specs/evidence/20260713_03/legacy-log-inventory-before-cleanup.sanitized.json` | 완료 | 기존 비표준 로그 823개, 약 162 MiB 정리 전 목록 |
| `specs/evidence/20260713_03/legacy-log-cleanup-after.sanitized.json` | 완료 | `logs`, `acc/logs`, `mbr/logs` 정리 후 상태 |
| `specs/evidence/20260713_03/final-generated-log-cleanup.sanitized.json` | 완료 | qualityGate 생성 raw 로그 28개, 3,570,935 bytes 삭제 후 로그 디렉터리 0건 |
