# CPF 안정화 작업 보고서

작성일: 2026-07-14
입력 요청서: `CPF_NEW_REQUEST.md`
최종 목표: `CPF_FINAL_TARGET_REQUIREMENTS.md`
상태 기준: 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요

## 1. 시작 commit과 종료 worktree

| 항목 | 결과 |
|---|---|
| 시작 HEAD | `fb6b17850aa915c8ff0db034833e2fe1343fc322` |
| 종료 HEAD | `fb6b17850aa915c8ff0db034833e2fe1343fc322` |
| branch | `master` |
| Git commit/push/branch 생성 | 수행하지 않음 |
| 기준 evidence | `specs/evidence/20260714_01` |
| 요청 전 사용자 변경 | `CPF_NEW_REQUEST.md`, `CPF_STABILIZATION_REPORT.md` |

종료 worktree는 요청된 구현·삭제·문서·증적 변경을 포함한 미커밋 상태다. 전체 목록은 20절의 최종 manifest 증적에 기록한다.
작업 중 사용자가 `.vscode/settings.json`을 Java 25로, Gradle wrapper를 9.1.0으로 갱신한 변경을 감지해 보존했고, 이후 검증은 이 최종 도구 체인에 맞춰 다시 수행했다.

## 2. 요청서 보호

| 검증 | 상태 | 결과 |
|---|---|---|
| SHA-256 | 완료 | `E241D448C026ACD564D7970FAC1AF004E9E779BC9186FA135C849CBD6F01A8D9` |
| git blob | 완료 | `beb533ed23529f695e4cc48c9dad5001010ef4d0` |
| baseline copy | 완료 | `specs/evidence/20260714_01/cpf-new-request.baseline.md` |
| baseline metadata | 완료 | `specs/evidence/20260714_01/request-baseline.sanitized.json` |
| 종료 byte 비교 | 완료 | SHA-256과 git blob이 시작값과 동일 |

요청서는 읽기 전용 입력으로 유지했고 baseline을 재생성하거나 요청서 본문을 수정하지 않았다.

## 3. 직접 변경한 source, test, SQL, config, script

- PFW 공개 경계: recovery query/command, transaction timeline query, service registry query, reliability operation 포트와 facade를 추가했다.
- durable recovery: transaction fallback의 eligible ordering·lease·stale reclaim·poison·capacity를 보강하고 segment fallback/store/worker를 추가했다.
- Service Call Engine: attempt별 segment, selected instance, retry/failover/circuit/unknown/reconciliation 결과를 확장했다.
- reference flow: CMN typed contract를 기준으로 MBR → ACC → EXS Local Facade/Remote Proxy 구조를 추가했다.
- idempotency: 만료된 PROCESSING 레코드를 원자적으로 재시작하도록 port와 JDBC/in-memory adapter를 수정했다.
- broker: claim lease, retry/backoff/max attempts, DLQ 이동, inbox 중복 제거, 상태 변경이 아닌 actual replay를 구현했다.
- BAT: JobInstance 논리 파일에 DB lease 기반 single writer와 충돌 시 fragment degraded mode를 적용했다.
- FileTransfer: SFTP/FTP/FTPS/SCP/SSH 계약을 검증하는 결정적 reference adapter를 추가했다.
- 거래 헤더: `@CpfTransaction` API에만 업무 필수 헤더를 강제하고 모든 Controller에서 민감 확장 헤더 우회 전파를 차단했다.
- MyBatis: CMN/ACC의 광범위 package scan을 `@Mapper` 인터페이스로 제한해 공개 계약의 가짜 Mapper 등록을 방지했다.
- 파일 로그: OS 수정시각 대신 파일명의 업무일자를 기준으로 보존·압축하고 지연 기록 시 gzip을 복원하도록 수정했다.
- OpenAPI: 공개 mapping의 명시 operationId, 공통 오류 schema/response, API 유형별 header policy를 보강했다.
- EDU: 범용 샘플을 XYZ/BAT로 집중하고 PFW/CMN/ACC/MBR/EXS/ADM/BIZADM의 중복 EducationSample을 제거했다.
- SQL: V26 trace recovery/timeline과 V27 broker claim/retry/replay를 추가하고 split SQL, V1, 단일 설치 파일, smoke를 동기화했다.
- 설정/정리: Docker MariaDB root password 기본값을 제거하고 stale `info/db.info`를 삭제했다.
- 도구 체인: Gradle 9.1.0과 Java 25를 지원하도록 Lombok 1.18.46, Byte Buddy/agent 1.18.7을 적용했다.
- 배포 호환: Java 25에서 빌드해도 Java 21에서 실행 가능한 산출물을 만들도록 모든 Java 컴파일을 `--release 21`, UTF-8로 고정했다.
- Gradle 10 대비: `remoteDeployDryRun`의 실행 시점 `Task.project` 접근을 구성 시점 task provider로 변경했고 `--warning-mode all` 재실행에서 deprecation을 제거했다.

## 4. 직접 실행한 명령

```text
.\gradlew.bat cleanTest test --no-daemon
.\gradlew.bat :acc:bootJar :adm:bootJar :bat:bootJar :bizadm:bootJar :exs:bootJar :mbr:bootJar :xyz:bootJar --no-daemon
.\gradlew.bat :pfw:test --tests <reliability focused groups> --no-daemon
.\gradlew.bat :xyz:cleanTest :xyz:test --tests '*EducationSampleTest' :bat:cleanTest :bat:test --tests '*EducationSampleTest' --no-daemon
.\gradlew.bat checkUtf8 checkMojibake checkLegacyName checkSqlStandard checkJavaFormat checkTransactionIdStandard checkSecuritySeedStandard checkSampleStandard checkCpfRequestProtection checkServiceCallBoundary checkArchitectureOwnership checkSpringEventUsage checkProfileLoading checkRuntimeConfigStandard checkLogManagementStandard checkOpenApiSourceCoverage checkSampleCoverage --no-daemon
$env:JAVA_HOME='D:\Java\jdk-21'; .\gradlew.bat cleanTest qualityGate --no-daemon
$env:JAVA_HOME='D:\Java\jdk-25.0.3.9-hotspot'; .\gradlew.bat cleanTest qualityGate --no-daemon
.\gradlew.bat :acc:bootJar :adm:bootJar :bat:bootJar :bizadm:bootJar :exs:bootJar :mbr:bootJar :xyz:bootJar --no-daemon
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-all-install-sql.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-final-worktree-manifest.ps1
```

환경 준비 후 재현 명령은 `scripts/smoke-mariadb-full-install.ps1`, `scripts/smoke-standard-header-e2e.ps1`이다. 이번 작업에서는 실행하지 않았으며 성공으로 보고하지 않았다.

## 5. 성공

- Java 21 + Gradle 9.1.0 전체 실행: 76 tasks, 275 tests, failures 0, errors 0, skipped 4.
- Java 25 + Gradle 9.1.0 전체 실행: 76 tasks, 275 tests, failures 0, errors 0, skipped 4.
- 두 JDK의 `cleanTest qualityGate`는 모두 `BUILD SUCCESSFUL`이지만 환경형 4건의 건너뜀 때문에 종합 상태는 미검증으로 기록했다.
- 신뢰성 핵심: 13 suites, 50 tests, failures/errors/skipped 0.
- XYZ/BAT EDU: 47 sample classes, 53 tests, failures/errors/skipped 0.
- Java 25에서 7개 실행 모듈 bootJar가 성공했고, 각 jar의 Application class major가 모두 65(Java 21)임을 확인했다.
- 정적 gate 17개 성공: UTF-8, mojibake, SQL, Java format, request protection, ownership, Spring Event, profile, log, OpenAPI, sample 포함.
- OpenAPI source: mappings 297, explicit operationId 319, missing/duplicate 0.
- ownership: failures 0, review candidates 2.

핵심 증적은 `specs/evidence/20260714_01/focused-core-tests-final.sanitized.log`, `specs/evidence/20260714_01/edu-sample-final.sanitized.log`, `specs/evidence/20260714_01/quality-gate-gradle910-java21-release21.sanitized.log`, `specs/evidence/20260714_01/quality-gate-gradle910-java25-release21.sanitized.log`, `specs/evidence/20260714_01/seven-bootjar-class-version.sanitized.log`이다.

## 6. 실패

- Java 25 초기 재현은 Gradle 8.11.1에서 `Type T not present`, Gradle 8.9에서 class major 69 미지원으로 실패했다.
- Gradle 9.1.0 전환 후에는 Lombok 1.18.32가 javac 25에서 실패했고, Lombok을 올린 다음에는 구형 Byte Buddy가 Java 25 mock instrumentation을 거부했다.
- Lombok 1.18.46과 Byte Buddy 1.18.7로 보강한 최종 Java 25 실행은 전체 qualityGate까지 통과했다. 초기 실패 증적은 원인 추적용으로 유지한다.
- 첫 `qualityGate`는 신규 manifest 증적의 표준 메타데이터 누락으로 실패했다. 생성기를 보강한 뒤 두 번째 실행은 통과했고 최초 실패는 표준 정제 증적으로 보존했다.
- 구현 중 발견한 ACC context와 파일 로그 일자 테스트 실패는 원인을 수정한 뒤 재실행하여 통과했다. 최종 판정에는 재실행 결과를 사용하고 원시 실행 로그는 작업 종료 시 정리했다.

## 7. 미실행 또는 환경 부재

- MariaDB 인증정보 환경변수 없음: full install, Flyway upgrade, seed idempotency, DB recovery 미검증.
- 앱 runtime DB profile 없음: 7개 `/v3/api-docs`, ADM 권한별 200/403, 브라우저 클릭 미검증.
- Kafka/MQ, SFTP/FTP/SCP/SSH 실 서버, 공유 storage, Vault/KMS 없음: 실 adapter runtime 미검증.
- 전체 275개 테스트 중 CMN sequence DB, PFW runtime file evidence, XYZ center-cut DB, XYZ mapper DB 4건이 조건부 건너뜀이다.

## 8. 요청서와 다르게 적용한 대안

- CMN의 기존 파일교환·메시지 bridge는 즉시 삭제하면 호환 API가 깨지므로 이번에는 migration candidate로 유지했다. 새 기술 capability와 reference adapter 소유권은 PFW로 고정했다.
- 실제 원격 protocol 서버 대신 동일 정책 엔진을 호출하는 deterministic adapter로 path/checksum/temp/atomic/credential 계약을 검증했다.
- BAT 공유 storage가 없어 DB lease + local fragment harness로 single writer 충돌과 degraded mode를 검증했다.
- PFW DB가 없는 업무 앱도 기동하도록 timeline public facade는 optional `pfwJdbcTemplate`을 사용하고 unavailable 결과를 반환한다.

## 9. 대안의 사유, 장단점, 영향

- 호환 facade 유지의 장점은 기존 호출자 무중단이고 단점은 CMN 기술 구현 후보 2건이 남는 점이다.
- deterministic adapter는 재현성이 높지만 wire protocol·서버 권한·네트워크 장애를 검증하지 못한다.
- optional datasource facade는 앱 기동 독립성을 높이지만 실제 데이터 조회 완료 판정에는 DB runtime이 별도로 필요하다.
- DB lease/fragment 방식은 이벤트 손실을 막지만 실제 NFS/object storage의 atomicity와 성능은 후속 runtime 검증 대상이다.

## 10. 미처리 원인 분류

| 분류 | 항목 |
|---|---|
| 환경 | MariaDB credential, Kafka/MQ, 원격 파일 서버, 브라우저 기동 |
| 도구 경고 | Byte Buddy의 `sun.misc.Unsafe` 제거 예정 경고와 Java 25 native-access 경고 |
| 호환성 | CMN 기술 bridge 즉시 제거 보류 |
| runtime | multi-process MBR → ACC → EXS, DB timeline, ADM 통합 조회 |
| 운영 인프라 | shared storage, Vault/KMS, 다중 WAS |

## 11. 다음 실행 가능한 조치

1. 비추적 환경변수로 MariaDB 검증 계정을 주입하고 `00_all_install_and_smoke.sql` 2회 실행 및 Flyway V22→V27 upgrade를 수행한다.
2. 동일 DB profile로 7개 앱을 기동해 `/v3/api-docs`, MBR → ACC → EXS, ADM timeline/recovery를 검증한다.
3. Kafka 또는 MQ test container와 SFTP/FTP test server를 붙여 실제 adapter 장애·재시작·DLQ replay를 검증한다.
4. Byte Buddy Unsafe와 Gradle native-access 경고가 해소되는 후속 버전을 추적한다.
5. CMN migration candidate 두 클래스를 PFW adapter + CMN compatibility facade로 단계 이전한다.

## 12. EDU coverage

- 소유권: 일반/실패/복합/외부연계 EDU는 XYZ, 배치/center-cut EDU는 BAT가 소유한다.
- PFW는 engine/port/reference adapter와 contract/unit test, CMN은 parser/formatter/converter/helper와 unit test만 소유한다.
- `specs/sample-coverage-matrix.md`는 47개 샘플 모두 source/test/evidence를 연결한다.
- mapper DB 교육 fixture는 `xyz_edu_query_fixture.sql`을 정본으로 사용한다.
- DB 계정 환경변수는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`을 표준으로 하고 기존 `CPF_XYZ_EDU_MAPPER_DB_USER`는 호환 입력으로만 인식한다.

## 13. OpenAPI runtime

- source 완료: tag/operation/operationId, 공통 `CpfErrorResponse`, 400/401/403/404/409/429/500/503, 업무 거래와 운영 API의 header policy를 보강했다.
- runtime 미검증: DB profile 없이 앱 7개를 기동하지 않아 `/v3/api-docs` JSON과 Swagger UI를 실행하지 않았다.
- 증적: `specs/evidence/20260714_01/openapi-source-coverage.sanitized.json`.

## 14. MSA와 Reliability runtime

- source/test: MBR → ACC → EXS typed contract, Local Facade, Remote Proxy, registry/health selection, attempt segment, retry/failover/circuit/unknown을 구현했다.
- durable: transaction/segment fallback의 lease·stale reclaim·poison·capacity와 actual broker replay를 검증했다.
- 미검증: 세 프로세스 HTTP 호출, 다중 인스턴스 failover, DB timeline과 ADM 상호 추적.

## 15. MariaDB

- 서비스 실행, client 12.3, TCP port를 preflight에서 확인했다.
- credential present 여부는 absent로만 기록했고 값은 수집하지 않았다.
- V26/V27, split SQL, V1 baseline, `00_all_install.sql`, `00_all_install_and_smoke.sql`, smoke SQL은 정적으로 동기화했다.
- 실제 DB 작업은 미검증이며 증적은 `specs/evidence/20260714_01/mariadb-preflight.sanitized.log`, `specs/evidence/20260714_01/mariadb-full-install.sanitized.log`이다.

## 16. ADM

- ADM은 PFW 내부 worker/table/JdbcTemplate 대신 public recovery/timeline/service-registry/reliability port만 호출한다.
- recovery status/run/poison retry, timeline/group query, DLQ replay의 서버 권한·감사 경계를 유지했다.
- source/unit/bootJar는 확인했으나 DB 연결 runtime, 권한별 HTTP status, 브라우저 UX는 미검증이다.

## 17. BAT, FileTransfer, Broker

- BAT: DB lease single writer, stale takeover, same JobInstance logical file, fragment degraded mode를 구현·테스트했다.
- FileTransfer: LOCAL 정책 엔진과 SFTP/FTP/FTPS/SCP/SSH deterministic reference adapter를 구현·테스트했다.
- Broker: outbox lease, retry/backoff/max attempts, DLQ, inbox duplicate prevention, actual replay를 구현·테스트했다.
- 실제 shared storage, wire protocol, Kafka/MQ runtime은 미검증이다.

## 18. cleanup

- root `info/db.info`는 runtime·문서 참조가 없는 stale 정보 파일로 확인해 삭제했다.
- `docker-compose.local.yml`의 MariaDB root password 기본값을 제거하고 필수 환경변수로 전환했다.
- 범용 EducationSample 중복 파일과 대응 중복 테스트를 삭제하고 XYZ/BAT 정본으로 통합했다.
- 검증 중간 원시 로그는 `build/evidence-raw`에서만 임시 생성하고, 작업 종료 시 삭제한다. 재현 가치가 있는 성공·실패 결과만 비밀정보 검사를 거친 정제 증적으로 `specs/evidence/20260714_01`에 보존한다.

## 19. report, gap, evidence, matrix 정합성

다음 표의 상태는 `CPF_EVIDENCE_INDEX.md`와 `specs/기능_구현_매트릭스.html`에 동일하게 유지한다.

| check id | 상태 | 비고 |
|---|---|---|
| edu-mapper-db-slice | 완료 | 기존 DB slice 증적 유지 |
| mariadb-full-install | 미검증 | credential absent |
| adm-runtime | 부분 구현 | source/test 완료, runtime 미검증 |
| adm-permission-runtime | 부분 구현 | server test 완료, HTTP runtime 미검증 |
| openapi-runtime | 부분 구현 | source gate 완료, runtime 미검증 |
| adm-browser-click | 미검증 | 앱/브라우저 미기동 |
| standard-header-e2e | 완료 | 기존 E2E 증적 유지 |
| complex-transaction-trace | 완료 | 기존 runtime 증적 유지 |
| transaction-segment-log | 완료 | 기존 runtime + 신규 fallback test |
| adm-transaction-group-list | 완료 | 기존 runtime 증적 유지 |
| adm-transaction-timeline | 완료 | 기존 runtime 증적 유지 |
| cmn-fixed-length-engine | 완료 | 기존 test 증적 유지 |
| composite-runtime-smoke | 완료 | 기존 runtime 증적 유지 |
| adm-transaction-group-runtime | 완료 | 기존 runtime 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 실 broker 없음 |
| broker-real-integration | 미검증 | 실 adapter runtime 없음 |
| file-log-standard | 부분 구현 | 신규 일자 정책 test, 전체 runtime 미검증 |
| trace-boost-runtime | 미검증 | 미실행 |
| bat-trace-boost-runtime | 미검증 | 미실행 |
| runtime-start-services | 미검증 | 미실행 |
| packaged-runtime-resources | 완료 | 기존 package 증적 + 신규 bootJar |
| runtime-status-diagnostics | 미검증 | 미실행 |
| runtime-closure | 미검증 | 미실행 |
| adm-operation-console-runtime | 완료 | 기존 runtime 증적 유지 |
| adm-log-policy-ui-static | 완료 | 기존 static 증적 유지 |
| bat-log-bean-runtime | 미검증 | 앱 미기동 |
| exs-timeout-retry-runtime | 완료 | 기존 test 증적 유지 |
| cmn-fixed-length-advanced | 완료 | 기존 test 증적 유지 |
| create-domain-smoke | 완료 | 기존 smoke 증적 유지 |
| pfw-service-call-engine | 부분 구현 | focused test 완료, multi-process 미검증 |
| adm-service-registry-runtime | 완료 | 기존 runtime 증적 유지 |
| architecture-ownership-scan | 재확인 필요 | 위반 0, migration candidate 2 |
| spring-event-usage-scan | 완료 | 허용 5, 검토/금지 0 |
| pfw-broker-capability | 부분 구현 | focused test 완료, real broker 미검증 |
| pfw-file-transfer-capability | 부분 구현 | reference adapter 완료, real server 미검증 |
| pfw-security-credential-capability | 부분 구현 | contract 존재, Vault/KMS 미검증 |
| pfw-runtime-control-capability | 부분 구현 | contract 존재, multi-instance 미검증 |
| pfw-admin-status-capability | 부분 구현 | public port 완료, DB/browser 미검증 |
| profile-loading-standard | 완료 | 정적 계약 통과 |
| packaged-dependencies-check | 완료 | 기존 증적 유지 |
| deploy-dry-run-standard | 부분 구현 | real deploy 제외 |
| garbage-file-cleanup | 완료 | 기존 정리 증적 유지 |
| empty-directory-scan | 완료 | 기존 증적 유지 |
| deploy-env-standard | 완료 | 기존 증적 유지 |
| deploy-inventory-standard | 완료 | 기존 증적 유지 |
| gradle-deploy-task-standard | 완료 | 기존 증적 유지 |
| datasource-mode-standard | 완료 | 기존 증적 유지 |
| local-port-duplicate-scan | 완료 | 기존 증적 유지 |
| edu-module-deploy-alias-scan | 완료 | 기존 증적 유지 |
| bat-edu-package | 완료 | 53 tests, skipped 0 |
| bat-job-log-policy | 완료 | lease/fragment test 통과 |
| sample-coverage-matrix | 완료 | 47 rows 일치 |
| sample-placeholder-scan | 완료 | 기존 증적 유지 |
| evidence-path-existence-check | 완료 | 최종 gate 대상 |
| create-domain-profile-template | 완료 | 기존 증적 유지 |
| runtime-smoke-summary | 완료 | 기존 runtime 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | 최종 gate 대상 |
| quality-gate | 미검증 | Java 21/25의 76-task gate 명령 성공, 각각 275 tests 중 환경형 4건 skipped |
| check-html-docs | 완료 | 최종 gate 대상 |
| check-feature-evidence | 완료 | 최종 gate 대상 |
| check-utf8 | 완료 | UTF-8/mojibake 통과 |

## 20. 사용자 commit 대상 전체 manifest

- 수정/신규/삭제/staged/untracked를 `git status --short`, `git diff --name-status`, `git diff --cached --name-status`, `git ls-files --others --exclude-standard`로 수집한다.
- staged 파일은 0건이며 Codex는 commit하지 않았다.
- 요청서 자체는 사용자 시작 변경으로 보존한다.
- 전체 `git diff --check`는 보호 대상 요청서의 기존 Markdown 줄바꿈 공백 2건 때문에 실패하고, 요청서를 제외한 검사는 통과했다. 요청서 hash 보호 원칙에 따라 해당 공백은 수정하지 않았다.
- 작업 중 외부 갱신된 Java 25 IDE 설정과 Gradle 9.1.0 wrapper는 보존하고 최종 검증 기준으로 사용했다.
- 전체 경로 목록과 hash는 최종 `specs/evidence/20260714_01/final-worktree-manifest.sanitized.log`에 기록한다.
- manifest에는 상태, 경로, SHA-256, 요청서 SHA-256·Git blob, 시작 commit, 최종 도구 체인을 기록하고 파일 자체는 `SELF_REFERENTIAL` 정책으로 표시한다.
- accidental/unrelated 변경은 최종 보호·diff 검사에서 별도 확인한다.
