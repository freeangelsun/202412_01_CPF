# CPF 안정화 작업 보고서

작성 시각: 2026-07-13 12:56 KST

## 1. 기준

- 입력 요청서: `CPF_NEW_REQUEST.md`
- 최종 목표: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 신규 증적 디렉터리: `specs/evidence/20260713_01`
- 요청서 보호 기준: `scripts/check-cpf-request-protection.ps1 -InitializeBaseline`로 명시 초기화 후 `checkCpfRequestProtection`은 검사만 수행
- 완료 판정 원칙: source/test만 있으면 `부분 구현`, 외부 runtime 또는 DB 접속이 없으면 `미검증`, 기존 증적만 있고 최신 재실행이 없으면 비고에 명확히 표기

## 2. 이번 작업 반영

- `CPF_NEW_REQUEST.md` 보호 구조를 baseline 자동 생성 방식에서 명시 초기화 방식으로 보강했다.
- Gradle에 `initializeCpfRequestProtection` task를 추가하고 `qualityGate`의 `checkCpfRequestProtection`은 check-only 모드로 유지했다.
- `scripts/smoke-mariadb-full-install.ps1`에 V22 PFW reliability 테이블, 공통 컬럼, index 검사를 추가했다.
- MariaDB smoke는 CLI를 찾았지만 비밀번호 환경변수가 없어 DB 접속을 시도하지 않았고 `미검증` 증적을 생성했다.
- PFW `CpfIdempotencyEngine`과 상태 전이, 저장 응답 replay, 실패·UNKNOWN 재시도, TTL 정리 경계를 추가했다.
- PFW Broker publisher/consumer worker와 결정적 adapter를 추가해 발행 성공·실패, inbox 중복, DLQ 이동을 검증했다.
- PFW LOCAL 파일전송 adapter와 실행 엔진을 추가해 streaming, temp rename, SHA-256, 경로 이탈 차단, 중복·이력·UNKNOWN 연결을 검증했다.
- XYZ/BAT 멱등·Broker·파일전송 EDU가 자체 자료구조 대신 PFW capability를 호출하도록 변경했다.
- ADM reliability 조회 API와 DLQ replay, 결과 미확정 수동 처리 API를 추가하고 서버 권한·감사 사유·before/after 기록을 연결했다.
- ADM Reliability 메뉴·조회/replay/수동확정 UI와 역할별 메뉴·버튼·API seed, V23 migration을 추가하고 `adm.js` 문법 검사를 통과했다.
- 신규 주제영역 생성기의 손상된 한글·SQL COMMENT를 복구하고 Application/profile/test 골격을 보강했으며, LNG 생성 결과의 test와 bootJar를 실제 통과시켰다.
- Java 25에서 Gradle 8.11.1 구성 단계가 실패함을 실제 확인했다. Java 25의 Gradle 실행 지원은 9.1.0 이상이 필요하므로 현재 조합은 `실패`로 기록한다.
- report/evidence/matrix/gap의 과대 완료 상태를 보정했다.

## 3. 상태 판정표

| check id | 상태 | 증적 | 판정 메모 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | 기존 EDU mapper DB slice 증적 유지 |
| mariadb-full-install | 미검증 | `specs/evidence/20260713_01/mariadb-full-install-result.json` | MariaDB CLI는 확인했지만 `CPF_DB_ROOT_PASSWORD` 미제공으로 DB 접속 미수행 |
| adm-runtime | 완료 | `specs/evidence/20260707_02/adm-runtime-smoke-result.sanitized.json` | 기존 ADM runtime smoke 증적 유지 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_02/adm-permission-runtime-result.sanitized.json` | 기존 ADM 권한 runtime smoke 증적 유지 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260707_02/openapi-runtime-result.sanitized.json` | 기존 runtime 증적은 있으나 신규 reliability API와 전체 Controller coverage는 미완 |
| adm-browser-click | 미검증 | 없음 | 이번 작업에서 브라우저 클릭 재실행 없음 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_02/standard-header-e2e-result.sanitized.json` | `scripts/smoke-standard-header-e2e.ps1` 기존 증적 유지 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 기존 복합 거래 trace 증적 유지 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 기존 segment log 증적 유지 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_02/adm-operation-console-runtime-result.sanitized.json` | 기존 ADM 거래 그룹 목록 증적 유지 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | 기존 ADM timeline 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260708_05/cmn-test.log` | 기존 CMN fixed-length 테스트 증적 유지 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_02/composite-transaction-runtime-result.sanitized.json` | 기존 composite runtime smoke 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_02/adm-transaction-group-runtime-result.sanitized.json` | 기존 ADM 거래 그룹 runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | 실 Redis/Kafka/MQ broker runtime 미실행 |
| broker-real-integration | 미검증 | 없음 | 실 broker adapter와 장애 fallback runtime 미실행 |
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
| create-domain-smoke | 완료 | `specs/evidence/20260713_01/create-domain-result.json`, `specs/evidence/20260713_01/create-domain-compile.log` | LNG 생성·UTF-8 marker·필수 파일·test·bootJar 실제 통과 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260709_03/servicecall-contract-test.sanitized.json`, `specs/evidence/20260713_01/reliability-module-tests.log` | 기존 contract 유지, 다중 HTTP runtime 미검증 |
| adm-service-registry-runtime | 완료 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | 기존 ADM service registry runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json` | CMN 기술 실행 후보는 PFW migration 추적 필요 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260709_03/spring-event-usage-scan.sanitized.json` | Spring Event usage scan 통과 증적 유지 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260709_03/broker-contract-test.sanitized.json`, `specs/evidence/20260713_01/reliability-module-tests.log` | publisher/consumer worker와 결정적 adapter는 통과, 실 broker·JDBC 재기동 runtime 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260709_03/filetransfer-contract-test.sanitized.json`, `specs/evidence/20260713_01/reliability-module-tests.log` | LOCAL streaming adapter와 엔진은 통과, 외부 protocol·JDBC 재기동 runtime 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260713_01/adm-reliability-tests.log`, `specs/evidence/20260713_01/adm-ui-js-syntax.log` | reliability API/UI/seed 추가, 실제 DB runtime과 브라우저 클릭 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_03/profile-loading-result.sanitized.json` | 기존 profile loading 증적 유지 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | 기존 packaged dependencies 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json` | dry-run 계획 검증 |
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
| sample-coverage-matrix | 완료 | `specs/evidence/20260713_01/sample-coverage-result.sanitized.json` | 이번 작업 sample coverage 검사 대상 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260713_01/sample-placeholder-scan.sanitized.json` | sample placeholder scan 대상 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260713_01/evidence-path-existence-check.sanitized.json` | evidence path gate 대상 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260713_01/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence consistency gate 대상 |
| quality-gate | 완료 | `specs/evidence/20260713_01/quality-gate.log` | 전체 qualityGate 실제 통과 |
| check-html-docs | 완료 | `specs/evidence/20260713_01/check-html-docs.log` | HTML 문서 검사 실제 통과 |
| check-feature-evidence | 완료 | `specs/evidence/20260713_01/check-feature-evidence.log` | 기능 증적 검사 실제 통과 |
| check-utf8 | 완료 | `specs/evidence/20260713_01/check-utf8-mojibake.log` | UTF-8 및 mojibake 검사 실제 통과 |

## 4. 실행·미실행 구분

- 실행 완료: 요청서 보호 baseline 초기화/검사, MariaDB smoke 사전 환경 확인, Java 21 전체 `qualityGate`, HTML 문서, 기능 증적, UTF-8/mojibake, SQL 표준, report/matrix/evidence 정합성, sample coverage, ADM JavaScript 문법 검사
- 테스트 결과: 전체 모듈의 현재 JUnit XML 157개 기준 281건 수행, 실패 0건, 오류 0건, 건너뜀 3건
- 생성기 검증: 신규 LNG 모듈 생성 후 격리된 Gradle 구성에서 `:lng:test`와 `:lng:bootJar` 실제 통과
- 실행 실패: Java 25 `:pfw:test`는 Gradle 8.11.1의 Java 25 실행 비호환으로 구성 단계에서 실패했으며 `specs/evidence/20260713_01/java25-pfw-test.log`에 기록
- 미실행으로 유지: 실제 MariaDB schema/Flyway V22·V23, Redis/Kafka/MQ, SFTP/FTP/SCP/SSH, Vault/KMS/HSM, 다중 instance runtime, browser click

## 5. EDU와 fixture 기준

- EDU DB slice fixture는 `xyz_edu_query_fixture.sql` 기준을 유지한다.
- 환경변수 표기는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`과 호환용 `CPF_XYZ_EDU_MAPPER_DB_USER`를 함께 유지한다.
- XYZ/BAT reliability EDU는 실제 PFW 멱등·Broker worker·LOCAL 파일전송 엔진을 호출하도록 보강했다.
- PFW/CMN 내부의 기존 `EducationSample` 잔존 파일은 최신 소유권 원칙과 맞지 않아 후속 이전·정리가 필요하다.
- ADM reliability API와 정적 UI 문법은 확인했으며 실제 DB 조회와 브라우저 클릭은 `미검증`이다.

## 6. 남은 핵심 리스크

- MariaDB 접속 secret이 현재 셸에 없어 `00_all_install_and_smoke.sql`, Flyway V22·V23, PK/UK/index 실검증을 실행하지 못했다.
- Java 25 실행을 완료하려면 Gradle 9.1 이상과 Spring Boot/plugin 조합의 호환성 업그레이드가 선행돼야 한다.
- Broker JDBC claim은 다중 worker 경쟁과 재기동을 실제 MariaDB에서 검증하지 않았다.
- 전체 Controller의 명시적 OpenAPI `operationId`, 표준 오류 response, 필수 헤더 coverage gate는 아직 미완이다.
- ADM reliability 정적 화면과 seed 메뉴·버튼 권한, 실제 runtime 클릭 검증은 미완이다.
