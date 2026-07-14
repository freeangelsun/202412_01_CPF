# CPF 안정화 작업 보고서

작성 시각: 2026-07-13 19:25 KST
입력 요청서: `CPF_NEW_REQUEST.md`
최종 목표: `CPF_FINAL_TARGET_REQUIREMENTS.md`
신규 증적: `specs/evidence/20260713_03`

## 1. 작업 기준

- 시작 commit: `255dd6ee752e6c8434eb4bd47c2524dca71cd196`
- branch: `master`
- 요청서 SHA-256: `5889860EE0C94F2DD3FE030DE5F9EDC82992AF740DB36998437A7F15D469341E`
- 요청서 git blob: `12665f46d50fc8177f6f2fe00f4f006259e0178b`
- 요청서 baseline: `specs/evidence/20260713_03/cpf-new-request.baseline.md`
- Java 21: 전체 test와 7개 실행 모듈 bootJar 검증 기준
- Java 25.0.3: 별도 호환성 재검증 기준
- 상태값: `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`

요청서는 작업 대상에서 제외했으며 시작 baseline 이후 재초기화하지 않았다. 종료 SHA-256과 byte-for-byte 비교가 모두 일치했고 요청서 보호 검사가 통과했다.

## 2. 수행 작업

### 2.1 로그 ownership과 절대 root

- PFW가 파일 로그 경로, 구조화 JSON Lines, 거래 로그, 보관·압축, DB fallback journal의 단일 owner가 되도록 `CpfLogPathPolicy`와 `CpfFileLogWriter`를 정리했다.
- local은 저장소 root를 탐색해 절대 `<repository>/logs`를 사용한다.
- dev/stg/prod는 외부 `CPF_LOG_ROOT`, `CPF_ENV`, `CPF_INSTANCE_ID`를 요구하고 상대경로·상위 경로 이동·기존 symbolic link 경로를 거부한다.
- 일반 실행 로그는 `${CPF_LOG_ROOT}/{environment}/{runtimeModuleCode}/{instanceId}` 아래에 둔다.
- 실행 프로세스에서 발생한 PFW/CMN 로그는 각각 `framework/pfw`, `common/cmn`에 귀속한다.
- 28개 배포 env, runtime 시작·상태 스크립트, 생성기 템플릿, 공통 logback을 같은 기준으로 갱신했다.

### 2.2 온라인 거래별 파일 로그

표준 경로:

```text
${CPF_LOG_ROOT}/{environment}/{runtimeModuleCode}/{instanceId}/transactions/{businessDate}/
{transactionId}_{businessDate}.log
```

- 파일 분리 키를 고정 업무 `transactionId + businessDate + instanceId`로 확정했다.
- `transactionGlobalId`는 JSON 레코드 연결 키이며 파일명으로 사용하지 않는다.
- 동일 transactionId와 서로 다른 globalId는 같은 파일에 append한다.
- context 누락은 `NO_TRANSACTION` 파일을 만들지 않고 anomaly 계수와 error 이벤트로 처리한다.
- 외부 연계 이벤트도 transaction context가 있으면 상위 거래 파일에 운영 판단 정보를 함께 남긴다.
- 파일명 토큰 검증, root 이탈 방지, 일자 고정, gzip archive, 보관기간과 전체 크기 상한을 적용했다.

실제 파일 생성 결과는 `specs/evidence/20260713_03/runtime-log-manifest.sanitized.json`에 있다. 두 ACC 인스턴스, PFW/CMN/application/error/integration, 거래 파일, DB fallback pending journal 등 8개 파일을 생성했고 JSON 파싱·필수 필드·마스킹 검사를 통과했다.

### 2.3 DB 거래 로그 rollback 독립성과 durable fallback

- `TransactionLogService`를 `pfwTransactionManager`의 `REQUIRES_NEW`로 분리했다.
- segment 저장은 별도 `TransactionSegmentPersistenceService` Spring bean으로 분리해 self-invocation proxy 우회를 막았다.
- DB 저장 실패 시 인스턴스별 `recovery/transaction-db/{pending,processing,poison}` journal에 원자적으로 기록한다.
- recovery event ID는 transactionGlobalId, segmentId, eventType, sequence의 결정적 SHA-256으로 생성한다.
- journal은 저장 전에 민감정보를 마스킹하고 중복 enqueue, 총 용량 상한, interrupted claim 복원, retry/backoff, poison 격리를 처리한다.
- scheduled/manual worker가 DB 복구 후 재적재하고 성공 시 journal을 삭제한다.
- `RECOVERY_EVENT_ID` unique 제약과 Flyway `V25__transaction_log_durable_recovery.sql`을 추가했다.
- ADM에 recovery 상태 조회와 감사 사유 필수 수동 실행 API를 추가하고 서버 버튼 권한을 연결했다.

단위 테스트에서는 journal 생성·마스킹·중복 방지·복구 후 cleanup·poison 격리를 확인했다. 실제 MariaDB 장애·복구 재적재와 업무 rollback 후 로그 유지 runtime은 인증정보가 없어 `미검증`이다.

### 2.4 BAT 로그

- BAT 중복 경로 정책 파일을 삭제하고 PFW 단일 경로 정책을 사용한다.
- JobInstance 논리 경로에 environment를 포함하고 DB operation metadata에도 같은 상대경로를 저장한다.
- BAT health, ADM 조회, EDU 샘플과 테스트를 동기화했다.
- 단일 JVM 정책 테스트는 통과했지만 공유 storage와 lease를 이용한 cross-instance same-file runtime은 `미검증`이다.

### 2.5 SQL·설정·문서·EDU

- `specs/sql/10_pfw_schema.sql`, V1 baseline, V25, `00_all_install.sql`, `00_all_install_and_smoke.sql`, ADM 권한 seed를 동기화했다.
- README, 개발 가이드, 관리자 가이드, 프레임워크 구성 가이드, SQL 가이드, 운영 매뉴얼을 로그 root와 recovery 기준으로 갱신했다.
- XYZ 파일 로그 EDU가 실제 PFW 거래 저장 경로를 호출하도록 업무 transactionId와 transactionGlobalId를 분리했다.
- PFW DB 장애 recovery EDU와 테스트를 추가하고 sample coverage matrix에 연결했다.
- EDU mapper DB fixture 기준은 `xyz_edu_query_fixture.sql`이며 인증 변수는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, 레거시 호환 변수는 `CPF_XYZ_EDU_MAPPER_DB_USER`다.
- MariaDB 재현 진입점은 `scripts/smoke-mariadb-full-install.ps1`, 표준 헤더 E2E 진입점은 `scripts/smoke-standard-header-e2e.ps1`이다.

### 2.6 미사용 파일·로그 cleanup

- 작업 전 `logs`, `acc/logs`, `mbr/logs`에서 823개 파일, 169,963,817 bytes를 확인하고 workspace 내부 대상만 삭제했다.
- BAT의 미사용 중복 `BatJobLogPathPolicy` source/test를 삭제했다.
- 추적 중인 모듈별 logback은 없고 공통 `pfw/.../cpf-logback-spring.xml` 하나만 유지한다.
- backup/temp/candidate 이름의 추적 파일, 삭제 정책 대상 stale 파일, 빈 지정 디렉터리, raw Git 로그를 검사했다.
- 실제 로그 생성 증적을 만든 후 qualityGate가 생성한 raw runtime 로그 28개, 3,570,935 bytes를 삭제했다.
- 최종 탐색 결과 `.git/logs`를 제외한 `logs`와 `test-runtime-logs` 디렉터리는 0건이며 정제 manifest만 유지한다.
- 최종 cleanup 근거: `specs/evidence/20260713_03/final-generated-log-cleanup.sanitized.json`

## 3. 직접 실행 검증

| 검증 | 결과 | 근거 |
| --- | --- | --- |
| Java 21 전체 Gradle test | 완료 | 166 suites, 308 tests, 실패 0, 오류 0, 건너뜀 4. `specs/evidence/20260713_03/java21-full-test.sanitized.json` |
| Java 21 전체 실행 모듈 bootJar | 완료 | ACC/ADM/BAT/BIZADM/EXS/MBR/XYZ 7개. `specs/evidence/20260713_03/java21-all-bootjar.sanitized.json` |
| packaged runtime resource | 완료 | 7개 JAR 내부 로그·프로필 리소스 검사. `specs/evidence/20260713_03/packaged-runtime-resource-check.sanitized.json` |
| 실제 파일 로그 probe | 완료 | 파일 8개, JSON Lines·필수 필드·secret scan 통과. `specs/evidence/20260713_03/runtime-log-manifest.sanitized.json` |
| SQL naming/comment | 완료 | `scripts/check-sql-standard.ps1` 통과 |
| UTF-8/mojibake | 완료 | `scripts/check-utf8.ps1`와 `-CheckMojibake` 통과 |
| architecture ownership | 재확인 필요 | 오류 0, CMN migration 후보 2. `specs/evidence/20260713_03/architecture-ownership-scan.sanitized.json` |
| OpenAPI source coverage | 완료 | `specs/evidence/20260713_03/openapi-source-coverage.sanitized.json` |
| MariaDB full install/Flyway | 미검증 | 서비스와 CLI는 확인했으나 현재 process 인증정보 부재. `specs/evidence/20260713_03/mariadb-preflight.sanitized.json` |
| Java 25 PFW test | 실패 | Gradle Test task 생성 중 `Type T not present`. `specs/evidence/20260713_03/java25-pfw-test.sanitized.json` |
| ADM 앱·브라우저·권한별 runtime | 미검증 | MariaDB 인증과 앱 runtime 미실행 |
| 실 Redis/Kafka/MQ 및 외부 파일전송 | 미검증 | 외부 broker/server runtime 미실행 |

## 4. 기능 상태 정합성

아래 표는 `specs/기능_구현_매트릭스.html`과 `CPF_EVIDENCE_INDEX.md`의 check ID 상태와 일치해야 한다.

| check id | 상태 | 비고 |
| --- | --- | --- |
| edu-mapper-db-slice | 완료 | 기존 DB slice 증적 유지 |
| mariadb-full-install | 미검증 | 인증정보 미주입 |
| adm-runtime | 부분 구현 | test·bootJar 완료, 앱 runtime 미실행 |
| adm-permission-runtime | 부분 구현 | 서버 매핑 test 완료, 200/403 runtime 미실행 |
| openapi-runtime | 부분 구현 | source coverage 완료, JSON runtime 미실행 |
| adm-browser-click | 미검증 | 앱·DB 미기동 |
| standard-header-e2e | 완료 | 기존 증적 유지 |
| complex-transaction-trace | 완료 | 기존 증적 유지 |
| transaction-segment-log | 완료 | 기존 증적 유지 |
| adm-transaction-group-list | 완료 | 기존 증적 유지 |
| adm-transaction-timeline | 완료 | 기존 증적 유지 |
| cmn-fixed-length-engine | 완료 | 기존 증적 유지 |
| composite-runtime-smoke | 완료 | 기존 증적 유지 |
| adm-transaction-group-runtime | 완료 | 기존 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 실 broker 미실행 |
| broker-real-integration | 미검증 | 실 adapter 미실행 |
| file-log-standard | 부분 구현 | 파일 probe 완료, 전체 앱·공유 storage 미검증 |
| trace-boost-runtime | 미검증 | 변경 스크립트 runtime 미실행 |
| bat-trace-boost-runtime | 미검증 | 변경 스크립트 runtime 미실행 |
| runtime-start-services | 미검증 | 전체 서비스 기동 미실행 |
| packaged-runtime-resources | 완료 | 현재 7개 JAR 검사 통과 |
| runtime-status-diagnostics | 미검증 | 변경 스크립트 runtime 미실행 |
| runtime-closure | 미검증 | 변경 스크립트 runtime 미실행 |
| adm-operation-console-runtime | 완료 | 기존 증적 유지 |
| adm-log-policy-ui-static | 완료 | 기존 증적 유지 |
| bat-log-bean-runtime | 미검증 | 변경 bean 앱 runtime 미실행 |
| exs-timeout-retry-runtime | 완료 | 기존 증적 유지 |
| cmn-fixed-length-advanced | 완료 | 기존 증적 유지 |
| create-domain-smoke | 완료 | 기존 생성 smoke 증적 유지 |
| pfw-service-call-engine | 부분 구현 | test 완료, 다중 HTTP runtime 미검증 |
| adm-service-registry-runtime | 완료 | 기존 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | CMN 후보 2건 |
| spring-event-usage-scan | 완료 | 현재 검사 통과 |
| pfw-broker-capability | 부분 구현 | test 완료, 실 broker 미검증 |
| pfw-file-transfer-capability | 부분 구현 | LOCAL test 완료, 외부 protocol 미검증 |
| pfw-security-credential-capability | 부분 구현 | Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | recovery API test 완료, 앱 runtime 미검증 |
| profile-loading-standard | 완료 | 현재 검사 통과 |
| packaged-dependencies-check | 완료 | 기존 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | dry-run만 검증 |
| garbage-file-cleanup | 완료 | stale 후보·비표준 로그 정리 확인 |
| empty-directory-scan | 완료 | 지정 경로 0건 |
| deploy-env-standard | 완료 | 기존 증적 유지 |
| deploy-inventory-standard | 완료 | 기존 증적 유지 |
| gradle-deploy-task-standard | 완료 | 기존 증적 유지 |
| datasource-mode-standard | 완료 | 기존 증적 유지 |
| local-port-duplicate-scan | 완료 | 기존 증적 유지 |
| edu-module-deploy-alias-scan | 완료 | 기존 증적 유지 |
| bat-edu-package | 완료 | 현재 전체 test 통과 |
| bat-job-log-policy | 완료 | 현재 정책 test 통과 |
| sample-coverage-matrix | 완료 | 현재 검사 통과 |
| sample-placeholder-scan | 완료 | 현재 검사 통과 |
| evidence-path-existence-check | 완료 | 현재·기존 증적 경로와 commit 대상 포함 검사 통과 |
| create-domain-profile-template | 완료 | 기존 증적 유지 |
| runtime-smoke-summary | 완료 | 기존 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | 61개 check ID 상태 일치 검사 통과 |
| quality-gate | 완료 | Java 21 BUILD SUCCESSFUL. `specs/evidence/20260713_03/quality-gate.sanitized.log` |
| check-html-docs | 완료 | HTML 구조와 상태표 검사 통과 |
| check-feature-evidence | 완료 | 필수 source·SQL·문서·샘플 검사 통과 |
| check-utf8 | 완료 | UTF-8 및 mojibake gate 통과 |

## 5. 남은 리스크와 다음 조치

1. MariaDB 인증정보를 비추적 환경변수로 주입한 뒤 빈 schema에서 full install, V22~V25 upgrade, seed 재실행, rollback 독립성, fallback 중복 재적재를 검증해야 한다.
2. ADM을 실제 기동해 recovery 상태·수동 실행 권한·감사 사유, transactionGlobalId timeline, `/v3/api-docs`, 브라우저 화면을 검증해야 한다.
3. 변경된 runtime start/status/closure와 trace boost smoke를 실제 다중 프로세스로 실행해야 한다.
4. 공유 storage와 lease 기반 BAT cross-instance same-file writer는 아직 구현·runtime 검증이 완료되지 않았다.
5. CMN의 `CmnFileExchangeService`, `CmnMessageBridgeService`는 호환성 때문에 남아 있으며 기술 실행부를 PFW port adapter로 이전할지 확정해야 한다.
6. Java 25는 현재 Gradle 8.11.1 Test task 생성 단계에서 실패한다. Java 21을 현행 검증 기준으로 유지하고 Gradle/Spring Boot 공식 지원 조합 업그레이드를 별도 호환성 작업으로 수행해야 한다.

## 6. 최종 manifest

최종 `git status --short`, `git diff --name-status`, `git diff --cached --name-status`, `git ls-files --others --exclude-standard`, `git diff --check` 결과를 `specs/evidence/20260713_03/changed-untracked-manifest.sanitized.json`에 기록했다. 요청서와 구현·리포트·증적 변경을 분류했으며 staged 파일은 0건이다. commit·push·branch 생성은 수행하지 않았다.

## 7. 항상 지켜야 할 기준 점검

- 문서와 소스, SQL, OpenAPI, EDU 샘플 정합성: 현재 변경 범위 반영 및 qualityGate 통과
- README는 짧은 진입점, 상세 내용은 가이드 연결: 준수
- 기능 변경 시 README·개발/관리자/프레임워크/SQL/운영 가이드·EDU 동시 현행화: 준수
- 신규 주석·설명·SQL COMMENT 한글: 준수
- 운영 코드는 기능 단위 주석, EDU는 학습 중심 상세 주석: 준수
- 실행하지 않은 검증을 완료로 보고하지 않음: 준수
- 민감정보를 source·log·evidence·report에 기록하지 않음: 준수
