# CPF 안정화 작업 보고서

작성 시각: 2026-07-13 16:36 KST

## 1. 기준

- 입력 요청서: `CPF_NEW_REQUEST.md`
- 최종 목표: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 신규 증적 디렉터리: `specs/evidence/20260713_02`
- 요청서 보호 기준: `scripts/check-cpf-request-protection.ps1 -InitializeBaseline`로 명시 초기화 후 `checkCpfRequestProtection`은 검사만 수행
- 완료 판정 원칙: source/test만 있으면 `부분 구현`, 외부 runtime 또는 DB 접속이 없으면 `미검증`, 기존 증적만 있고 최신 재실행이 없으면 비고에 명확히 표기

## 2. 이번 작업 반영

- `.gitignore`를 복구해 `logs/**`, `*/logs/**`, `*.log`, `*.log.*` 원본을 차단하고, 기존 raw 로그 967개는 로컬 파일을 보존한 채 Git 추적에서만 제거했다.
- `specs/evidence`는 정제된 `*.sanitized.log`, `*.sanitized.json`, 요청서 기준 해시만 추적하도록 분리했고, 증적 메타데이터·secret scan·본문 SHA-256 검증기를 추가했다.
- 실행 모듈 로그 root를 `${CPF_LOG_ROOT}/{moduleCode}`로 통일하고 `cpf-{moduleCode}-{logType}-{instanceId}.{yyyy-MM-dd}.log` 파일명, Asia/Seoul 일자, 보관기간, gzip 압축, 운영 fail-fast 정책을 공통화했다.
- BAT 로그를 `businessDate + jobName + jobInstanceId` 파일로 분리하고 restart 동일 파일, 다른 인스턴스 분리, 자정 통과 업무일자 고정, 거래·segment·execution 연결 필드를 구현했다.
- `NO_TRANSACTION` 파일 appender를 제거하고 온라인 거래 context 누락을 `CONTEXT_MISSING` 오류와 누적 진단 지표로 전환했다.
- ADM에 BAT JobInstance 로그 목록·상세 API/UI를 추가하고 경로 이탈·심볼릭 링크·과도한 레코드 조회를 차단했다.
- LOCAL FileTransfer DOWNLOAD의 허용 base path, 심볼릭 링크, checksum, temp/불완전 target 정리를 보강했다.
- 전체 Controller에 결정적 OpenAPI operationId 자동 생성기를 적용하고 59개 Controller, 349개 mapping, 명시 ID 중복 0건을 소스 게이트로 확인했다.
- XYZ에 application/transaction/error 로그, 민감정보 마스킹 방어, ADM BAT 로그 조회 EDU와 테스트를 추가했다.
- Spring Batch 연계 메타에 JobInstance·업무일자·재수행·로그 상대 경로·거래 segment 컬럼을 추가하고 V24 migration과 합본 SQL을 동기화했다.

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
| mariadb-full-install | 미검증 | `specs/evidence/20260713_02/mariadb-preflight.sanitized.log` | MariaDB CLI는 확인했지만 접속 secret이 현재 셸에 없어 DB 접속·설치 SQL·Flyway 실행을 수행하지 않음 |
| adm-runtime | 완료 | `specs/evidence/20260707_02/adm-runtime-smoke-result.sanitized.json` | 기존 ADM runtime smoke 증적 유지 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_02/adm-permission-runtime-result.sanitized.json` | 기존 ADM 권한 runtime smoke 증적 유지 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260713_02/openapi-source-coverage.sanitized.json`, `specs/evidence/20260713_02/openapi-runtime.sanitized.log` | 전체 mapping의 고유 operationId 소스 검사는 통과했으나 현재 코드의 `/v3/api-docs` 앱 기동 검증은 미실행 |
| adm-browser-click | 미검증 | `specs/evidence/20260713_02/adm-browser-click.sanitized.log` | ADM 앱과 DB를 기동한 실제 브라우저 클릭은 수행하지 않음 |
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
| file-log-standard | 부분 구현 | `specs/evidence/20260713_02/log-management-standard.sanitized.json`, `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | root·파일명·일자전환·압축·마스킹·비추적 단위/정적 검증은 완료, 전체 실행 모듈 앱 기동과 다중 인스턴스 runtime은 미검증 |
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
| create-domain-smoke | 완료 | `specs/evidence/20260713_02/create-domain-smoke.sanitized.log` | 최신 공통 로그 설정과 프로젝트 Java 목표를 포함한 LNG 생성·test·bootJar 실제 통과 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | 현재 PFW 테스트 통과, 다중 HTTP runtime 미검증 |
| adm-service-registry-runtime | 완료 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | 기존 ADM service registry runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | `specs/evidence/20260709_03/architecture-ownership-scan.sanitized.json` | CMN 기술 실행 후보는 PFW migration 추적 필요 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260709_03/spring-event-usage-scan.sanitized.json` | Spring Event usage scan 통과 증적 유지 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | publisher/consumer worker와 결정적 adapter 테스트 통과, 실 broker·JDBC 재기동 runtime 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | LOCAL streaming·허용 base·심볼릭 링크·checksum 실패 cleanup은 통과, 외부 protocol·JDBC 재기동 runtime 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260709_03/security-credential-contract-test.sanitized.json` | Vault/KMS runtime 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260709_03/runtime-worker-contract-test.sanitized.json` | 다중 instance runtime 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log`, `specs/evidence/20260713_02/adm-ui-js-syntax.sanitized.log`, `specs/evidence/20260713_02/adm-browser-click.sanitized.log` | Reliability/BAT 로그 API·UI·문법·서비스 테스트 통과, 실제 DB runtime과 브라우저 클릭 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260708_03/profile-loading-result.sanitized.json` | 기존 profile loading 증적 유지 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260708_05/packaged-dependencies-acc.sanitized.json` | 기존 packaged dependencies 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260708_05/remote-deploy-dry-run.sanitized.json` | dry-run 계획 검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260713_02/garbage-file-scan.sanitized.json` | 현재 저장소 garbage scan 통과 |
| empty-directory-scan | 완료 | `specs/evidence/20260713_02/empty-directory-scan.sanitized.json` | 현재 저장소 empty directory scan 통과 |
| deploy-env-standard | 완료 | `specs/evidence/20260708_05/deploy-env-acc-dev.sanitized.json` | 기존 deploy env 증적 유지 |
| deploy-inventory-standard | 완료 | `specs/evidence/20260708_05/deploy-inventory-acc-dev.sanitized.json` | 기존 deploy inventory 증적 유지 |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260708_05/gradle-remote-deploy-task-scan.sanitized.json` | 기존 Gradle deploy task scan 증적 유지 |
| datasource-mode-standard | 완료 | `specs/evidence/20260708_05/datasource-mode-scan.sanitized.json` | 기존 datasource mode scan 증적 유지 |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260708_05/local-port-duplicate-scan.sanitized.json` | 기존 local port duplicate scan 증적 유지 |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260708_05/edu-module-deploy-alias-scan.sanitized.json` | 기존 EDU deploy alias scan 증적 유지 |
| bat-edu-package | 완료 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | BAT EDU와 전체 BAT 테스트 통과 |
| bat-job-log-policy | 완료 | `specs/evidence/20260713_02/reliability-log-tests.sanitized.log` | JobInstance 분리·restart 동일 파일·업무일자·JSON Lines 테스트 통과 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260713_02/sample-coverage-result.sanitized.json` | 신규 XYZ/BAT 로그 EDU를 포함한 sample coverage 검사 통과 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260713_02/sample-placeholder-scan.sanitized.json` | sample placeholder scan 통과 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260713_02/evidence-path-existence-check.sanitized.json` | 현재 증적의 Git 추적·stale commit·ID·raw 로그 금지 검사 통과 |
| create-domain-profile-template | 완료 | `specs/evidence/20260708_05/garbage-file-scan.sanitized.json` | 기존 create-domain profile template 증적 유지 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_02/runtime-smoke-summary.sanitized.json` | 기존 runtime smoke summary 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260713_02/report-matrix-evidence-consistency.sanitized.json` | 61개 check ID의 보고서·HTML 매트릭스·증적 인덱스 상태 일치 검사 통과 |
| quality-gate | 완료 | `specs/evidence/20260713_02/quality-gate.sanitized.log` | Java 21 전체 qualityGate 재실행 통과, JUnit 301건·실패 0·오류 0·건너뜀 3 |
| check-html-docs | 완료 | `specs/evidence/20260713_02/static-quality-gates.sanitized.log` | 현재 HTML 문서 검사 통과 |
| check-feature-evidence | 완료 | `specs/evidence/20260713_02/static-quality-gates.sanitized.log` | 현재 기능 증적 검사 통과 |
| check-utf8 | 완료 | `specs/evidence/20260713_02/static-quality-gates.sanitized.log` | ps1 UTF-8 BOM 예외를 명시한 UTF-8 및 mojibake 검사 통과 |

## 4. 실행·미실행 구분

- 실행 완료: 요청서 보호, runtime raw 로그 Git 비추적, 정제 증적 규격, 로그 root·파일명·보관·압축 정적 검사, Java 21 전체 `qualityGate`, SQL 표준, HTML, sample coverage, OpenAPI 소스 coverage, ADM JavaScript 문법 검사
- 테스트 결과: 최종 전체 qualityGate 기준 JUnit 301건을 실행했으며 실패 0건, 오류 0건, 건너뜀 3건이다. 로그 집중 검증은 `pfw`, `bat`, `adm` 192건과 XYZ 신규 로그 EDU 3건을 별도로 재실행했다.
- 생성기 검증: 최신 템플릿으로 신규 LNG 모듈을 격리 생성해 `:lng:test`와 `:lng:bootJar`를 실제 통과시켰다.
- 실행 실패: Java 25 `:pfw:test`는 Gradle 8.11.1 구성 단계의 `Type T not present`로 실패했으며 `specs/evidence/20260713_02/java25-pfw-test.sanitized.log`에 기록했다.
- 미실행으로 유지: 실제 MariaDB 전체 설치/Flyway V22·V23·V24, Redis/Kafka/MQ, SFTP/FTP/SCP/SSH, Vault/KMS/HSM, 다중 instance runtime, ADM browser click, 현재 코드의 `/v3/api-docs` runtime JSON 검증

## 5. EDU와 fixture 기준

- EDU DB slice fixture는 `xyz_edu_query_fixture.sql` 기준을 유지한다.
- 환경변수 표기는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`과 호환용 `CPF_XYZ_EDU_MAPPER_DB_USER`를 함께 유지한다.
- XYZ/BAT reliability EDU는 실제 PFW 멱등·Broker worker·LOCAL 파일전송 엔진을 호출하도록 보강했다.
- `XyzFileLogEducationSample`은 PFW writer를 직접 호출해 application/transaction/error 분리와 마스킹 방어를 보여준다.
- `XyzAdmBatchLogQueryEducationSample`은 PFW JobInstance 경로 검증과 ADM 목록·상세 URL, `RELIABILITY_READ` 권한을 연결한다.
- PFW/CMN/EXS/ADM/BIZADM에 남아 있는 기존 범용 `EducationSample` 전체 이전은 이번 로그 마일스톤에서 완료하지 못했으므로 `재확인 필요`로 유지한다.
- ADM Reliability/BAT 로그 API와 정적 UI 문법은 확인했으며 실제 DB 조회와 브라우저 클릭은 `미검증`이다.

## 6. 남은 핵심 리스크

- MariaDB 접속 secret이 현재 셸에 없어 `00_all_install_and_smoke.sql`, Flyway V22·V23·V24, PK/UK/index 실검증을 실행하지 못했다.
- Java 25 실행을 완료하려면 Gradle 9.1 이상과 Spring Boot/plugin 조합의 호환성 업그레이드가 선행돼야 한다.
- Broker JDBC claim은 다중 worker 경쟁과 재기동을 실제 MariaDB에서 검증하지 않았다.
- 전체 Controller의 고유 `operationId`는 자동 보장하지만 표준 오류 response, 필수 헤더, request/response schema의 runtime JSON 검증은 아직 미완이다.
- ADM Reliability와 BAT 로그 조회는 source/test/UI 문법까지만 확인했으며 실제 DB·브라우저·다운로드 감사 runtime은 미검증이다.
- 기존 PFW/CMN/EXS/ADM/BIZADM 범용 EDU 소유권 정리는 별도 호환성 분석과 함께 진행해야 한다.

## 7. 증적과 다음 우선순위

- 시작 commit, 요청서 hash, raw 로그 추적 제거 967건, 그 외 변경 경로는 `specs/evidence/20260713_02/worktree-diff.sanitized.log`에 기록했다.
- whitespace 검사는 `specs/evidence/20260713_02/git-diff-check.sanitized.log` 기준으로 통과했다.
- 다음 우선순위는 접속정보를 비추적 환경변수로 주입한 MariaDB V22·V23·V24/all-install 재실행, ADM 브라우저·OpenAPI runtime, 실 broker·외부 파일전송·다중 인스턴스 검증이다.
- Java 25는 Spring Boot와 Gradle의 공식 호환 조합을 함께 올리는 별도 업그레이드 마일스톤으로 진행한다.
