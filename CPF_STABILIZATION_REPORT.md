# CPF 최종 통합 안정화 보고서

## 1. 결론

이번 작업은 Java 25 단일 기준, PFW 기술 소유권, 안전한 ADM 초기 계정, 7개 실행 모듈 기동, OpenAPI 런타임, 표준 헤더 전파, 주제영역 생성기, DOCX 정본화와 품질 게이트를 중심으로 수행했다.

현재 저장소는 **로컬 소스·단위 테스트·패키징·비 DB 런타임 기준으로 안정화됐으나 전체 최종 완료 상태는 아니다.** MariaDB 검증 자격증명, 실 Redis/Kafka/RabbitMQ, 실제 파일전송 서버, ADM 로그인용 bootstrap 환경과 브라우저 검증이 제공되지 않아 해당 항목은 `미검증` 또는 `부분 구현`으로 남겼다.

## 2. 기준 정보

| 항목 | 값 |
|---|---|
| Repository root | `D:/WORK_CPF/202412_01_CPF` |
| Branch | `master` |
| 시작 HEAD | `d16cd7a40062a1e77bd8cd3c6f6f7125cdc0708d` |
| 요청서 | `CPF_NEW_REQUEST.md` |
| 요청서 SHA-256 | `BCCF457FABD14C9BE6D37563CC3A1E14AAA65525C9CD2C595CE23CEE3719A84C` |
| 요청서 git blob | `149c59dfddede5d1653817e603c1106f2e0f68ff` |
| 기준 evidence | `specs/evidence/20260714_02` |
| 최종 작업트리 manifest | `specs/evidence/20260714_02/final-worktree-manifest.sanitized.log` |
| Java/Gradle | Java 25 / Gradle 9.1.0 |
| 상태 정본 | `specs/기능_구현_매트릭스.json` |

요청서 원본과 시작 baseline은 수정하지 않았다. 요청서 내부에 작업 시작 전부터 존재한 깨진 문자열은 요청서 보호 원칙 때문에 교정 대상에서 제외했다.

## 3. 구현 결과

### Java 25

- 모든 Gradle 하위 프로젝트의 toolchain, source, target, `--release`를 25로 통일했다.
- 9개 모듈 클래스와 7개 실행 JAR의 class major 69를 검사하는 `checkJava25Standard`를 추가했다.
- `.vscode/settings.json`의 개인 PC JDK 절대 경로를 제거했다.
- 주제영역 생성기도 Java 25 모듈을 생성하고 class major 69까지 검사한다.

### 아키텍처와 소유권

- PFW는 broker, file transfer, transaction, logging, reliability 같은 기술 engine과 port를 소유한다.
- CMN의 기존 메시지 bridge와 파일교환 서비스는 PFW port를 호출하는 호환 facade로 축소했다.
- XYZ EDU 메시징·파일전송 예제는 PFW port를 직접 학습하도록 변경했다.
- CMN은 프로젝트 공통 규칙과 업무 공통 helper 역할을 유지한다.
- 타 주제영역 Repository/Mapper 직접 접근과 핵심 처리의 임의 Spring Event 사용을 정적 gate로 차단한다.

### ADM 초기 계정과 비밀번호

- 고정 관리자 ID·고정 평문 비밀번호 seed를 제거했다.
- bootstrap은 명시적 enable, 운영환경 승인, 환경변수 비밀번호를 요구한다.
- 첫 로그인 세션은 비밀번호 변경 전까지 본인 비밀번호 변경·로그아웃·정책 조회만 허용한다.
- 현재 비밀번호 확인, 새 비밀번호 확인, 정책 검사, 현재/이력 재사용 차단, optimistic update, 변경 이력, 감사 로그, 세션 전체 폐기를 구현했다.
- 관리자 reset도 비밀번호 이력 재사용을 차단한다.
- ADM UI는 강제 변경 상태에서 다른 메뉴를 잠그고 현재/신규/확인/사유를 받는다.

### PFW broker와 파일교환

- `CpfBrokerBridgePort`와 Kafka/RabbitMQ 선택 adapter를 PFW에 배치했다.
- 연결되지 않은 broker는 로컬 handler 또는 명시적 fallback 결과로 처리한다.
- `CpfFileExchangeGateway`는 검증, 임시 파일, checksum, 이동, 이력 record와 원격 명령 계획을 PFW 경계로 제공한다.
- 실 broker와 실 SFTP/FTP/FTPS/SCP/SSH 서버 통합은 외부 환경 미제공으로 미검증이다.

### 런타임과 표준 헤더

- ACC, MBR, EXS, ADM, BAT, BIZADM, XYZ 7개 실행 모듈을 Java 25 JAR로 실제 기동했다.
- 7개 포트, PID, HTTP health/OpenAPI probe와 종료 후 포트 폐쇄를 확인했다.
- 표준 헤더 E2E에서 수신, 허용 확장 헤더, 금지 확장 헤더 차단, 하위 호출 전파를 확인했다.
- 거래 ID는 `yyyyMMddHHmmssSSS + module(3) + wasId(7) + sequence(7)` 규격으로 현재 일자를 사용한다.
- DB 로그 행과 ADM 조회는 DB 자격증명 미제공으로 미검증이다.

### 생성기와 문서

- `scripts/create-domain.ps1` 생성 결과를 임시 경로에서 test, bootJar, class major 69까지 검증했다.
- 생성 테스트에서 Java 25 동적 agent 의존을 제거하고 독립 test double을 사용했다.
- 공식 HTML 가이드 13개를 제거하고 9개 DOCX 정본으로 전환했다.
- README는 아키텍처, 기동, DB, 헤더, 로그, EDU, 생성기, 검증 진입점으로 재작성했다.

## 4. 전수 inventory

| 분류 | 현재 수량 | 근거 |
|---|---:|---|
| 전체 inventory asset | 3,201 | `specs/evidence/20260714_02/cpf-inventory.sanitized.json` |
| Gradle module | 9 | 동일 증적 |
| public port | 29 | 동일 증적 |
| controller | 61 | 동일 증적 |
| EDU source | 65 | 동일 증적 |
| Flyway migration | 27 | 동일 증적 |
| 샘플 capability 매핑 | 47/47 | `specs/evidence/20260714_02/sample-coverage-result.sanitized.json` |
| 실행 모듈 bootJar | 7 | `specs/evidence/20260714_02/java25-seven-bootjar.sanitized.log` |
| 공식 DOCX | 9 | `specs/evidence/20260714_02/docx-standard.sanitized.json` |
| 공식 HTML | 0 | 최종 DOCX gate |

## 5. 실행 검증 결과

### Gradle

| 검증 | 결과 | 증적 |
|---|---|---|
| `clean test` / 최종 quality gate | 130 suites, 290 tests, failures 0, errors 0, skipped 4 | `specs/evidence/20260714_02/quality-gate.sanitized.log` |
| 7개 `bootJar` | 성공 | `specs/evidence/20260714_02/java25-seven-bootjar.sanitized.log` |
| class major | 9개 module class와 7개 JAR 모두 69 | `specs/evidence/20260714_02/java25-standard.sanitized.json` |
| PFW 파일 로그 runtime | 성공 | `specs/evidence/20260714_02/pfw-file-log-runtime.sanitized.log` |

건너뛴 4건은 CMN MariaDB 채번 동시성, PFW 파일 로그 환경형 test, XYZ center-cut DB adapter, XYZ mapper DB slice다. PFW 파일 로그는 별도 runtime으로 통과했지만 JUnit 전체 결과의 skip 수치는 그대로 기록했다.

주제영역 생성기 smoke에서 생성된 임시 모듈의 test 1건은 별도 Gradle 실행과 `create-domain-result.sanitized.json`으로 검증했으며, 위 저장소 JUnit XML 합계에는 포함하지 않았다.

### 7개 애플리케이션과 OpenAPI

| 모듈 | 포트 | 상태 | OpenAPI paths/tags |
|---|---:|---|---:|
| ACC | 8080 | 완료 | 19 / 7 |
| MBR | 8081 | 완료 | 14 / 4 |
| EXS | 8092 | 완료 | 19 / 4 |
| ADM | 8090 | 완료 | 143 / 25 |
| BAT | 8093 | 완료 | 3 / 1 |
| BIZADM | 8091 | 완료 | 15 / 2 |
| XYZ | 8099 | 완료 | 61 / 17 |

증적은 `runtime-start-services-result.sanitized.json`, `runtime-status-result.sanitized.json`, `openapi-runtime-result.sanitized.json`, `runtime-stop-services-result.sanitized.json`에 있다.

### 실패 후 수정과 재검증

| 최초 실패 | 원인 | 조치 | 최종 결과 |
|---|---|---|---|
| 생성 모듈 smoke | Windows `.bat` 실행 권한과 Java 25 Mockito agent | wrapper JAR 직접 실행, test double 전환 | test·bootJar·major 69 완료 |
| BIZADM 기동 | `wasId` 7자 규격 위반 | `bizAP01`로 표준화 | 기동 완료 |
| XYZ 기동 | center-cut 기본 bean 부재 | PFW conditional auto-configuration 추가 | 기동 완료 |
| BAT OpenAPI tag 판독 | PowerShell 5.1 응답 인코딩 | 안정적인 ASCII tag와 한글 description 분리 | OpenAPI 완료 |
| BAT runtime status | DB DOWN 진단으로 응답이 3초 초과 | probe timeout을 10초로 조정 | 7개 status 완료 |
| 표준 헤더 mock capture | 백그라운드 job 상대 경로 해석 | 결과 경로 절대화 | 하위 수신 헤더 완료 |
| 첫 quality gate | 매트릭스의 fixture·재현 명령 3개 누락 | 상태 정본에 정확한 파일명과 명령 추가 | 두 번째 quality gate 완료 |
| 복합거래 smoke | `cpf_pfw_app` DB 인증 실패 | 임의 비밀번호 사용 금지, 실패 증적 보존 | 미검증 |

## 6. MariaDB 검증

`scripts/smoke-mariadb-full-install.ps1`을 실행했다. MariaDB CLI는 발견됐지만 `CPF_DB_ROOT_PASSWORD`, `CPF_DB_MIGRATION_PASSWORD`, `CPF_DB_APP_PASSWORD`가 없어 접속 자체를 시도하지 않았다.

| 검증 | 상태 | 설명 |
|---|---|---|
| CLI 발견 | 완료 | 로컬 MariaDB client 확인 |
| 인증정보 선행조건 | 미검증 | 세 비밀번호 환경변수 미제공 |
| `00_all_install_and_smoke.sql` | 미검증 | DB 접속 미시도 |
| FK/index/seed 재실행 | 미검증 | DB 접속 미시도 |
| migration/app 권한 분리 | 미검증 | DB 접속 미시도 |
| Flyway upgrade | 미검증 | DB 접속 미시도 |

재현 명령은 비밀번호 값을 셸 기록이나 문서에 남기지 않은 환경에서 다음과 같다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1 -RequireRun
```

EDU mapper DB slice는 `xyz_edu_query_fixture.sql`을 사용한다. 표준 변수는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`이며 기존 `CPF_XYZ_EDU_MAPPER_DB_USER`는 호환 입력으로만 유지한다.

## 7. 표준 헤더 검증

실행 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1 -SkipLogLookup
```

| 구간 | 상태 | 결과 |
|---|---|---|
| inbound 필수/권장 헤더 | 완료 | ACC 실호출 200 |
| `X-Cpf-Ext-*` 허용 헤더 | 완료 | downstream 전파 확인 |
| token/api-key 확장 헤더 | 완료 | 400 거부 확인 |
| 거래 ID 업무일자 | 완료 | 실행일 `20260714`과 prefix 일치 |
| DB log/detail | 미검증 | DB 자격증명 없음 |
| ADM header/data 분리 조회 | 미검증 | ADM 로그인·DB 선행조건 없음 |

## 8. 공식 문서

| 문서 | 경로 |
|---|---|
| 프레임워크 소개/아키텍처 | `specs/CPF_프레임워크_소개_및_아키텍처.docx` |
| 개발자 가이드 | `specs/CPF_개발자_가이드.docx` |
| 운영자 ADM 가이드 | `specs/CPF_운영자_ADM_가이드.docx` |
| 설치/DB/SQL/Flyway | `specs/CPF_설치_DB_SQL_Flyway_가이드.docx` |
| 배치/센터컷/스케줄러 | `specs/CPF_배치_센터컷_스케줄러_가이드.docx` |
| 외부연계/파일전송/전문 | `specs/CPF_외부연계_파일전송_전문_가이드.docx` |
| EDU 카탈로그/실습 | `specs/CPF_EDU_샘플_카탈로그_및_실습가이드.docx` |
| 기능 구현 검증 매트릭스 | `specs/CPF_기능_구현_검증_매트릭스.docx` |
| 전체 테스트 검증 리포트 | `specs/CPF_전체_테스트_검증_리포트.docx` |

## 9. 기능별 상태 ledger

아래 표는 `specs/기능_구현_매트릭스.json`과 `CPF_EVIDENCE_INDEX.md`에서 동일 상태로 관리한다.

<!-- CPF_LEDGER_BEGIN -->
| check id | 상태 | 핵심 증적 | 판정 |
|---|---|---|---|
| edu-mapper-db-slice | 미검증 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | MariaDB EDU mapper slice가 현재 전체 테스트에서 환경 선행조건으로 건너뜀. fixture: xyz_edu_query_fixture.sql |
| mariadb-full-install | 미검증 | `specs/evidence/20260714_02/mariadb-full-install-result.sanitized.json` | CLI는 확인했으나 root·migration·app 비밀번호 환경변수가 없어 DB 접속과 설치 SQL 실행은 시도하지 않음. 재현: scripts/smoke-mariadb-full-install.ps1 |
| adm-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 프로세스·포트·OpenAPI는 확인했으나 DB 로그인과 운영 API 실데이터 조회는 미검증 |
| adm-permission-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 필터·서비스 권한 테스트는 통과했으나 DB 계정별 200/403 런타임은 미검증 |
| openapi-runtime | 완료 | `specs/evidence/20260714_02/openapi-runtime-result.sanitized.json`, `specs/evidence/20260714_02/openapi-source-coverage.sanitized.json` | 7개 실행 모듈의 OpenAPI JSON을 실제 기동 상태에서 확인함 |
| adm-browser-click | 미검증 | `없음` | ADM bootstrap 자격증명과 DB 연결이 없어 실제 로그인·브라우저 클릭은 미실행 |
| standard-header-e2e | 부분 구현 | `specs/evidence/20260714_02/standard-header-e2e-result.sanitized.json` | 수신·금지 헤더 차단·하위 호출 전파는 완료, DB 로그·ADM 조회는 자격증명 부재로 미검증. 재현: scripts/smoke-standard-header-e2e.ps1 |
| complex-transaction-trace | 부분 구현 | `specs/evidence/20260714_02/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 복합거래 소스·단위 테스트는 통과했으나 현재 런타임은 DB 인증 실패로 HTTP 500을 반환함 |
| transaction-segment-log | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | segment·timeline·fallback 단위 테스트는 통과했으나 MariaDB 행과 ADM tree 연계는 미검증 |
| adm-transaction-group-list | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 기동은 완료했으나 DB 기반 거래 그룹 목록 실데이터 조회는 미검증 |
| adm-transaction-timeline | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | timeline 소스·테스트는 확인했으나 ADM DB 런타임은 미검증 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 고정길이 전문 parser·formatter 테스트가 Java 25 전체 테스트에서 통과함 |
| composite-runtime-smoke | 부분 구현 | `specs/evidence/20260714_02/composite-transaction-runtime-result.sanitized.json` | 실제 호출은 수행했으나 DB 인증 실패로 완료 조건을 검증하지 못함 |
| adm-transaction-group-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 애플리케이션은 기동했으나 DB 기반 거래 그룹 API 시나리오는 미검증 |
| redis-kafka-mq-broker | 미검증 | `없음` | 실 Redis·Kafka·RabbitMQ broker가 제공되지 않아 미실행 |
| broker-real-integration | 미검증 | `없음` | 실 broker 장애·fallback·재처리 통합 시나리오는 미실행 |
| file-log-standard | 완료 | `specs/evidence/20260714_02/pfw-file-log-runtime.sanitized.log`, `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json` | PFW 파일 로그 런타임과 7개 모듈 로그 경로 생성을 확인함 |
| trace-boost-runtime | 미검증 | `없음` | 운영 DB·권한을 사용하는 trace boost 실런타임은 미실행 |
| bat-trace-boost-runtime | 미검증 | `없음` | BAT trace boost 실런타임은 미실행 |
| runtime-start-services | 완료 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ACC·MBR·EXS·ADM·BAT·BIZADM·XYZ 7개 프로세스가 포트와 HTTP 프로브를 통과함 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260714_02/packaged-runtime-resource-check.sanitized.json` | 7개 bootJar의 표준 설정·로그 리소스 패키징 검사를 통과함 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260714_02/runtime-status-result.sanitized.json`, `specs/evidence/20260714_02/runtime-stop-services-result.sanitized.json` | 7개 모듈 상태 완료 후 종료와 포트 폐쇄를 확인함 |
| runtime-closure | 미검증 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-stop-services-result.sanitized.json` | 기동·상태·종료는 확인했으나 DB 의존 개별 runtime smoke 전체 묶음은 완료하지 못함 |
| adm-operation-console-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 프로세스와 UI 소스는 확인했으나 DB 기반 운영 콘솔 시나리오는 미검증 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260714_02/adm-log-policy-ui-static-result.sanitized.json` | 로그 정책·배치·센터컷·실행 사유 UI/API marker 정적 계약을 통과함 |
| bat-log-bean-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | BAT 로그 bean·listener 테스트는 통과했으나 JobRepository DB 런타임은 미검증 |
| exs-timeout-retry-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | timeout/retry 단위 테스트는 통과했으나 외부 테스트 서버 런타임은 미검증 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 반복 그룹·인코딩·길이 검증을 포함한 전문 테스트가 통과함 |
| create-domain-smoke | 완료 | `specs/evidence/20260714_02/create-domain-result.sanitized.json` | 임시 주제영역 생성 후 test·bootJar·Java class major 69를 확인함 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260714_02/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260714_02/service-call-engine-failover.sanitized.json`, `specs/evidence/20260714_02/service-call-engine-circuit-transition.sanitized.json`, `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 성공·retry·failover·circuit 계약과 단위 테스트는 통과, DB 복합거래 런타임은 미완료 |
| adm-service-registry-runtime | 부분 구현 | `specs/evidence/20260714_02/service-registry-runtime-result.sanitized.json`, `specs/evidence/20260714_02/adm-service-registry-ui-static-smoke.sanitized.json` | registry source/SQL과 ADM UI 계약은 통과했으나 DB 실데이터 runtime은 미검증 |
| architecture-ownership-scan | 완료 | `specs/evidence/20260714_02/architecture-ownership-scan.sanitized.json` | PFW 기술 소유권과 CMN 호환 facade 경계 검사에서 실패·경고 0건 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260714_02/spring-event-usage-scan.sanitized.json` | 핵심 흐름의 금지 Spring Event 사용 0건 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | PFW broker port·adapter와 outbox/inbox/DLQ 단위 테스트 통과, 실 broker는 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | PFW 파일전송 engine·gateway 테스트 통과, 실 SFTP/FTP/SCP 서버는 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 보안·credential 계약 테스트 통과, 실 Vault/KMS는 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | worker·lock·재처리 계약 테스트 통과, 다중 인스턴스는 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | ADM 공개 port와 상태 조회 테스트 통과, DB 운영화면 실데이터는 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260714_02/profile-loading-result.sanitized.json` | local/dev/stg/prod profile 정적 계약을 통과함 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260714_02/packaged-runtime-resource-check.sanitized.json` | 7개 bootJar 의존성과 공통 리소스 포함을 확인함 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260714_02/runtime-config-inventory.sanitized.json` | 배포 명세·dry-run 소스 계약은 확인했으나 원격 대상 실행은 미검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260714_02/garbage-file-scan.sanitized.json`, `specs/evidence/20260714_02/deleted-files.sanitized.json` | 추적 대상 가비지와 삭제 목록 검사를 통과함 |
| empty-directory-scan | 완료 | `specs/evidence/20260714_02/empty-directory-scan.sanitized.json` | 관리 대상 빈 디렉터리 검사를 통과함 |
| deploy-env-standard | 완료 | `specs/evidence/20260714_02/runtime-config-inventory.sanitized.json` | 배포 환경 파일 정적 표준 검사를 통과함 |
| deploy-inventory-standard | 완료 | `specs/evidence/20260714_02/runtime-config-inventory.sanitized.json` | dev/stg/prod inventory 정적 표준 검사를 통과함 |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260714_02/gradle-remote-deploy-task-scan.sanitized.json` | Gradle 배포 task 표준 검사를 통과함 |
| datasource-mode-standard | 완료 | `specs/evidence/20260714_02/datasource-mode-scan.sanitized.json` | datasource mode 정적 검사를 통과함 |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260714_02/local-port-duplicate-scan.sanitized.json` | 로컬 기본 포트 중복 0건 |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260714_02/edu-module-deploy-alias-scan.sanitized.json` | EDU 모듈 배포 alias 위반 0건 |
| bat-edu-package | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | BAT EDU 소스와 테스트는 포함됐으나 DB 기반 center-cut 테스트 1건이 건너뜀 |
| bat-job-log-policy | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | JobInstance 로그 경로·lease 테스트는 통과, 공유 스토리지 다중 프로세스는 미검증 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260714_02/sample-coverage-result.sanitized.json` | 공개 capability 대비 EDU 샘플 47/47 매핑을 확인함 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260714_02/sample-coverage-result.sanitized.json` | 샘플 표준·placeholder 검사를 통과함 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260714_02/evidence-path-existence-check.sanitized.json` | 최종 문서가 참조하는 증적 경로 검사에서 누락과 실패 0건을 확인함 |
| create-domain-profile-template | 완료 | `specs/evidence/20260714_02/create-domain-result.sanitized.json` | 생성 모듈의 profile·test·bootJar 템플릿을 실제 생성 결과로 확인함 |
| runtime-smoke-summary | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | 7개 기동과 OpenAPI는 완료했으나 DB·broker·browser 포함 전체 runtime bundle은 미완료 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260714_02/report-matrix-evidence-consistency.sanitized.json` | 61개 check ID의 report·matrix·evidence 상태 일치 검사 통과 |
| quality-gate | 완료 | `specs/evidence/20260714_02/quality-gate.sanitized.log` | Java 25 기준 79개 Gradle 작업과 저장소 JUnit 290개 테스트를 포함한 qualityGate 통과, failures 0, errors 0, skipped 4. 생성 모듈 smoke 테스트는 별도 증적으로 관리함 |
| check-docx-standard | 완료 | `specs/evidence/20260714_02/docx-standard.sanitized.json`, `specs/evidence/20260714_02/docx-word-open.sanitized.json` | 공식 DOCX 9개의 OpenXML 구조와 실제 Word 열기를 모두 확인함 |
| check-feature-evidence | 완료 | `specs/evidence/20260714_02/check-feature-evidence.sanitized.log` | 필수 source·SQL·EDU·문서·재현 명령 증적 gate 통과 |
| check-utf8 | 완료 | `specs/evidence/20260714_02/check-utf8.sanitized.log` | 저장소 텍스트 UTF-8과 mojibake 검사 통과 |
<!-- CPF_LEDGER_END -->

## 10. 남은 리스크와 다음 조치

1. 보안 채널로 MariaDB root·migration·app 검증 변수를 주입한 뒤 full install, 재실행, FK/index/권한, Flyway, DB 의존 테스트 3건을 실행한다.
2. ADM bootstrap 환경과 DB를 준비해 최초 로그인, 강제 비밀번호 변경, 권한별 200/403, 운영 로그 header/data 탭을 브라우저로 검증한다.
3. Redis/Kafka/RabbitMQ와 SFTP/FTP/FTPS/SCP/SSH 테스트 서버에서 fallback, DLQ/replay, 대용량 전송을 실행한다.
4. 다중 프로세스·공유 스토리지에서 BAT lease, JobRepository, 로그 writer를 검증한다.
5. 위 외부환경 검증이 끝난 뒤 현재 `부분 구현`과 `미검증`만 상태 승격한다.

## 11. 항상 지켜야 할 기준 점검

| 기준 | 이번 반영 |
|---|---|
| 문서·소스·SQL·OpenAPI·EDU 일치 | inventory·feature·matrix gate로 검사 |
| README는 진입점, 상세는 공식 가이드 | 반영 |
| 변경 시 관련 가이드 동시 현행화 | DOCX 재생성과 ledger 동기화로 반영 |
| 신규 주석·SQL COMMENT 한글 | 정적 검사 대상 |
| EDU는 자세한 학습 주석과 실제 engine 호출 | sample standard와 coverage gate 대상 |
| 실행하지 않은 검증 성공 보고 금지 | DB·broker·browser·protocol을 미검증으로 유지 |
| 민감정보 원문 금지 | evidence sanitization과 seed gate 적용 |
| 요청서 무수정 | 시작 hash/blob과 최종 보호 gate로 확인 |
