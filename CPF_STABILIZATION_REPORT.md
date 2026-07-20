# CPF 통합 안정화 보고서

## 1. 기준 정보

| 항목 | 값 |
|---|---|
| 작업일 | 2026-07-20 |
| 시작 branch | `master` |
| 시작 HEAD | `4b1d60f784c4567a0a3cba7c74ac60c880ab9f99` |
| 시작 origin/master | `4b1d60f784c4567a0a3cba7c74ac60c880ab9f99` |
| 활성 요청서 | `CPF_CURRENT_WORK_REQUEST.md` |
| 최상위 정본 | `CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 요청서 SHA-256 | `BA80F93109243AED0BD5C38BDB760A0C4A9A1E5D2AEB52A6BAD8A352FB10041C` |
| 정본 SHA-256 | `959389ABA24E1A7E25CF9A0F11F232B65DA7864A1BA5EC1E4B3979E82B050269` |

이 보고서는 실행한 검증만 완료로 기록한다. 이 문서를 포함하는 최종 commit의 원격 SHA는 push 후 대화 리포트에서 별도로 제시한다.

## 2. 이번 회차 핵심 결과

- PFW Base/Contract에서 주제영역 Base/Contract를 거쳐 기능 구현으로 이어지는 3단 구조를 Controller·Service 115개에 적용하고 자동 검사를 추가했다.
- PFW 기술 로깅 Aspect 단일 소유권을 확립하고 MBR의 중복 기술 Aspect를 제거했다.
- XYZ를 운영 기본 배포 대상이 아닌 reference business domain으로 정리했다. canonical API는 `/api/xyz/reference/**`, 기존 `/xyz/edu/**`는 호환 alias다.
- O/S/B 10자리 표준 실행 ID와 typed service-call 기본 API를 코드·생성기·샘플·테스트에 맞췄다.
- 도메인 생성기의 기본 출력을 최소화하고 null 검증, 정렬 whitelist, fake port 테스트, Base 계층, `cpf-*` 아카이브 규칙을 적용했다.
- 제품 버전 정본을 `1.0.0-SNAPSHOT`으로 통일하고 sources/Javadoc JAR, checksum, SBOM-lite, provenance 기반을 추가했다.
- README를 PFW core, CMN project common, ADM control plane, BAT worker, BZA/ACC/XYZ 업무 영역 기준으로 다시 작성했다.
- 과거 요청서와 완료된 apply note를 삭제하고 활성 요청서를 `CPF_CURRENT_WORK_REQUEST.md` 하나로 정리했다.

## 3. 실제 검증 결과

| 검증 | 결과 | 증적 |
|---|---|---|
| fetch/pull 및 시작 SHA | 통과 | 이 보고서 기준 정보 |
| 전체 품질 게이트 | 통과, 최종 증적 90 tasks / 38 executed / 52 up-to-date | `specs/evidence/20260720_04/quality-gate.sanitized.log` |
| 문서 내부 링크 | 통과, 14 documents / 9 local links / broken 0 | `specs/evidence/20260720_04/document-link-check.sanitized.json` |
| 전체 단위 테스트 | 통과, 156 suites / 359 tests / skipped 4 / failures 0 / errors 0 | `specs/evidence/20260720_04/full-test-release.sanitized.log` |
| 전체 JavaDoc | 통과 | `specs/evidence/20260720_04/full-test-release.sanitized.log` |
| 릴리스 산출물 | 통과, 36 artifacts / 36 checksums / sources 9 / Javadoc 9 | `specs/evidence/20260720_04/release-metadata-result.sanitized.json` |
| Base 3단 계층 | 통과, 115 classes | `specs/evidence/20260720_04/base-hierarchy.sanitized.json` |
| 아키텍처 소유권 | 통과 | `specs/evidence/20260720_04/architecture-ownership-scan.sanitized.json` |
| 도메인 생성기 | 통과, test/JAR/WAR/Java 25 | `specs/evidence/20260720_04/create-domain-result.sanitized.json` |
| 샘플 coverage | 통과, 50/50 | `specs/evidence/20260720_04/sample-coverage-result.sanitized.json` |
| MariaDB 전체 설치 | 통과, install/smoke/seed 재실행/FK/index/권한 38개 | `specs/evidence/20260720_04/mariadb-full-install-result.sanitized.json` |
| 7개 앱 동시 기동/종료 | 통과, MBR/ADM/BZA/XYZ/ACC/Gateway/BAT | `specs/evidence/20260720_04/runtime-start-services-result.sanitized.json`, `specs/evidence/20260720_04/runtime-stop-services-result.sanitized.json` |
| Gateway→ACC + BAT | 통과, HTTP 200/JobRepository/step/restart/rerun | `specs/evidence/20260720_04/gateway-bat-runtime-result.sanitized.json` |
| OpenAPI runtime | 통과, 7 services / 365 paths | `specs/evidence/20260720_04/openapi-runtime-result.sanitized.json` |
| 표준 헤더 E2E | 통과, 수신/전파/DB 로그 연결 | `specs/evidence/20260720_04/standard-header-e2e-result.sanitized.json` |
| Service Call runtime | 통과, typed contract와 Gateway→ACC | `specs/evidence/20260720_04/service-call-engine-runtime-success.sanitized.json` |
| ADM 인증/운영 API | 통과, 강제 비밀번호 변경과 운영 콘솔 | `specs/evidence/20260720_04/adm-forced-password-change.sanitized.json`, `specs/evidence/20260720_04/adm-operation-console-runtime-result.sanitized.json` |
| ADM/BZA UI 정적 검사 | 통과 | `specs/evidence/20260720_04/adm-ui-browser-smoke-result.sanitized.json`, `specs/evidence/20260720_04/bza-ui-static-result.sanitized.json` |
| ADM 브라우저 클릭 | 미검증 | 시스템 브라우저 자동화 파이프 종료, 전용 Chromium 다운로드의 로컬 인증서 체인 오류 |
| Redis/Kafka/RabbitMQ 실 broker | 미검증 | Docker 및 별도 broker 미제공 |
| Vault/KMS·SFTP/FTPS/SCP 실연동 | 미검증 | 외부 서버와 자격정보 미제공 |
| 외부 Tomcat/JNDI 배포 | 미검증 | 외부 WAS 미제공 |

## 4. 발견 후 보완한 결함

1. 새 `cpf-*` 아카이브명과 패키징·런타임 탐색기의 예전 파일 패턴 불일치를 수정했다.
2. ACC OpenAPI smoke의 폐기된 `/api/v1/acc` 기준을 현재 `/api/v1/acc/reference` 계약으로 맞췄다.
3. 이름만 runtime이던 Service Call smoke를 실제 Gateway→ACC HTTP 검사로 구현했다.
4. ADM 브라우저 smoke의 Windows `npm.ps1` 실행 정책 및 임시 npx 잠금 문제를 격리 runner 방식으로 보완했다.
5. MBR 원격 프록시 테스트를 typed `CpfStandardExecutionId` API 계약으로 갱신했다.
6. MBR `BaseResponse`가 Lombok 생성 Builder 타입을 직접 참조해 JavaDoc이 실패하던 문제를 제거했다.
7. ADM 임시 검증 운영자는 강제 비밀번호 변경 정책까지 확인한 뒤 관련 세션·이력·계정 데이터를 삭제했다.
8. 활성 요청서 보호 baseline을 `CPF_CURRENT_WORK_REQUEST.md` 기준으로 초기화하고 품질 게이트에서 변경 여부를 검사하도록 정리했다.
9. 운영 배포에서 제외한 XYZ를 `prod` 환경 파일 필수 검사에서도 제외해 배포 정책과 검증 도구를 일치시켰다.
10. Java 25 검사가 sources/Javadoc JAR를 실행 bootJar로 오인하지 않도록 classifier 필터를 보강했다.
11. XYZ reference 카탈로그의 비정상 장문 라인을 분리해 Java 포맷 검사를 통과시켰다.
12. 최신 정제 로그에 실행 메타데이터, 테스트 수치, secret scan과 본문 SHA-256을 부여했다.

## 5. 정리한 과거 파일

- `CPF_NEW_REQUEST.md`
- `CPF_CANONICAL_PREFLIGHT_APPLY_NOTE_20260716.md`
- `CPF_FULL_REQUEST_ALIGNMENT_APPLY_NOTE_20260716.md`
- `CPF_REQUIREMENT_UPDATE_APPLY_NOTE_20260716.md`
- `CPF_TARGET_COMPLETENESS_APPLY_NOTE_20260716.md`
- `deploy/env/prod-xyz.env`
- MBR 중복 `LoggingAspect.java`

추가로 Gradle build 산출물 10개 모듈, 현재 회차 원본 로그·상태 파일과 루트 산출물 40개, 과거 비정제 증적 164개, 모듈 `bin` 6개, 빈 디렉터리 28개를 정리했다. 정제 증적과 Git 추적 이력은 유지했다.

장기 정본, 진행 가이드, 안정화 보고서, GAP, 증적 인덱스, 기능 ledger와 현재 활성 요청서는 유지한다.

## 6. 남은 GAP

최신 ledger는 총 98개이며 완료 57개, 부분 구현 25개, 재확인 필요 12개, 미검증 4개, 미구현·실패 0개다.

- 실제 브라우저 클릭은 코드 결함이 아니라 현재 PC의 브라우저 자동화 정책과 인증서 체인 때문에 미검증이다.
- 실 broker, Vault/KMS, 외부 파일 서버, 외부 Tomcat/JNDI, 2개 이상 실제 WAS 인스턴스 장애 전환은 해당 환경이 준비돼야 검증할 수 있다.
- Gradle 9.1은 일부 plugin의 Gradle 10 비호환 deprecation을 경고한다. 현재 빌드 실패는 아니며 plugin 업그레이드 회차에서 제거해야 한다.
- Java 25에서 Byte Buddy/Netty가 `sun.misc.Unsafe` 제거 예정 경고를 출력한다. 현 버전 테스트는 통과했지만 상위 의존성 릴리스 추적이 필요하다.

## 7. 기능 검증 Ledger

아래 표는 `specs/기능_구현_매트릭스.json`에서 동기화한다.

<!-- CPF_LEDGER_BEGIN -->
| check id | 상태 | 핵심 증적 | 판정 |
|---|---|---|---|
| baseline-module-layout | 완료 | `specs/evidence/20260720_04/architecture-inventory.sanitized.json`, `specs/evidence/20260720_04/base-hierarchy.sanitized.json` | PFW·CMN·MBR·ADM·BZA·BAT·ACC·XYZ·PFW Gateway의 모듈 구성과 공통 3단 Base 계층을 최신 source 기준으로 검증함 |
| bza-rename | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BIZADM 소스·패키지·환경·DB 명칭을 BZA로 전환하고 legacy name gate를 통과함 |
| acc-exs-cleanup | 완료 | `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260716_02/repository-hygiene.sanitized.json` | 삭제 기능 49건의 유지·대체·제거 판정을 기록하고 ACC reference, XYZ 외부 연계 EDU, MBR→ACC 계약을 복원했으며 가비지와 소유권 gate를 통과함 |
| architecture-ownership | 완료 | `specs/evidence/20260720_04/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260720_04/base-hierarchy.sanitized.json` | 타 주제영역 Repository·Mapper·DB 직접 접근 금지, PFW 기술 AOP 단일 소유권과 PFW Base→주제영역 Base→기능 계층 검사를 통과함 |
| password-hashing | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW PBKDF2 버전 hash, legacy verify·rehash와 ADM/BZA 사용 테스트를 통과함 |
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-refresh-rotation | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | refresh token hash 저장, 조건부 폐기, 회전과 동시 재사용 거부 테스트를 통과함 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-permission-server | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | 메뉴·행위 권한을 서버 필터에서 검사하고 로그인 이력 USER:READ 권한 테스트를 통과함 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260720_04/adm-operation-console-runtime-result.sanitized.json`, `specs/evidence/20260720_04/openapi-runtime-result.sanitized.json`, `specs/evidence/20260720_04/adm-forced-password-change.sanitized.json` | ADM 인증·강제 비밀번호 변경·운영 콘솔 API와 OpenAPI 161개 경로를 실 DB 런타임에서 검증함. 브라우저 클릭은 로컬 브라우저 자동화 정책으로 미검증임 |
| adm-permission | 완료 | `specs/evidence/20260716_02/adm-permission-runtime-result.sanitized.json` | 관리자 읽기 200, 조회 역할의 허용 조회 200과 금지 쓰기 403, 권한 변경 감사 사유와 before/after를 실제 ADM API에서 검증함 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-local | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | allowlist root, 경로 이탈·symlink·확장자·크기 제한, SHA-256, 마스킹 preview와 다운로드 테스트를 통과함 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| remote-log-bundle-jobs | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 비동기 ZIP 작업 상태, 소유자 격리, 요청 한도, 부분 실패, 만료, 1회성 다운로드 token과 재발급을 구현하고 단위·ADM UI 정적 테스트를 통과함 |
| attachment-storage | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/attachment-edu-runtime.sanitized.log` | PFW 첨부 저장 port와 로컬 adapter의 경로·symlink·확장자·크기·content type·SHA-256 검증, XYZ 기준 업무 샘플의 단위 테스트와 저장·재조회 HTTP 런타임을 통과함 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-id | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | O/S/B 10자리 값 객체·annotation·중복 gate·327개 alias와 V28→V32 실 MariaDB 전환을 검증함 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | PFW 공통 pfwJdbcTemplate 소유권과 O/S/B 실행 메타 영속화를 검증하고 Gateway가 OACCQY0001로 ACC를 실제 호출함. 전체 모듈 catalog·ADM 정합성 E2E는 남음 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json`, `specs/evidence/20260716_02/transaction-meta-runtime-smoke-result.sanitized.json` | 34자리 거래 ID, O 실행 ID와 transactionGlobalId 조회를 확인하고 Gateway→ACC에서 실행 ID·route·Gateway instance 헤더 전파를 실검증함. 다중 인스턴스 timeline은 남음 |
| batch-standard | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | Spring Batch JobRepository를 PFW DB에 고정하고 BAT 실제 Job 완료·step 조회·restart·rerun과 종료 정리를 실검증함 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 완료 | `specs/evidence/20260720_04/gateway-bat-runtime-result.sanitized.json` | BAT를 MariaDB PFW JobRepository로 기동해 온디맨드 접수, 완료, step, restart, rerun과 프로세스 정리를 실검증함 |
| domain-generator | 완료 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/full-test-release.sanitized.log` | 최소 옵션 생성 결과의 독립 DataSource, PFW Base→도메인 Base→기능 계층, null·정렬 방어, test·bootJar·bootWar·Java 25 산출물을 검증함 |
| xyz-reference-samples | 완료 | `specs/evidence/20260720_04/sample-coverage-result.sanitized.json`, `specs/evidence/20260720_04/architecture-inventory.sanitized.json` | XYZ를 비운영 기본 reference business domain으로 정리하고 50개 공식 샘플의 source·test·문서 coverage를 검증함 |
| bat-edu | 완료 | `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | tasklet·chunk·retry·restart·idempotency·center-cut·온디맨드 EDU를 유지하고 실제 PFW JobRepository 실행까지 검증함 |
| ai-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/ai-edu-runtime.sanitized.json` | PFW provider·embedding·vector port와 XYZ deterministic 구조화 출력·streaming·tool·RAG·fallback·token·사람 승인 테스트 및 표준 헤더를 포함한 HTTP 200 런타임 검증을 완료함. 실 provider는 외부 자격정보 항목으로 미검증 |
| standard-header | 완료 | `specs/evidence/20260720_04/standard-header-e2e-result.sanitized.json` | XYZ 실제 HTTP 호출에서 표준 헤더 수신·하위 호출 전파·DB 거래 로그 연결을 E2E로 검증함 |
| service-call-engine | 완료 | `specs/evidence/20260720_04/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260720_04/gateway-bat-runtime-result.sanitized.json` | typed 기본 API와 Gateway→ACC 표준 실행 ID 라우팅, 선택 인스턴스 헤더, 실제 HTTP 200 응답을 검증함 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| mariadb-full-install | 완료 | `specs/evidence/20260720_04/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260720_04/mariadb-full-install.sanitized.log` | 실 MariaDB에서 전체 설치·smoke·seed 재실행과 FK·index·app/migration 권한 38개 검사를 통과함 |
| flyway-static | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json` | 기존 V28 checksum을 보존하고 V32 증분 migration을 격리 DB에 실제 적용해 327개 alias와 구형 fixture 전환을 검증함 |
| sql-all-install | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/mariadb-full-install.sanitized.log` | split SQL에서 SOURCE 없는 합본과 V1 baseline을 재생성하고 실 MariaDB 설치·재실행을 검증함 |
| runtime-baseline | 완료 | `specs/evidence/20260720_04/runtime-start-services-result.sanitized.json`, `specs/evidence/20260720_04/runtime-stop-services-result.sanitized.json` | MBR·ADM·BZA·XYZ·ACC·Gateway·BAT 7개 최신 bootJar를 동시에 기동해 health와 프로세스 생존을 확인한 뒤 기록 PID만 정상 종료함 |
| openapi-runtime | 완료 | `specs/evidence/20260720_04/openapi-runtime-result.sanitized.json` | ADM·MBR·XYZ·BZA·BAT·ACC·Gateway의 OpenAPI 3.1 필수 tag/path를 실제 기동 상태에서 모두 검증함 |
| browser-public-http | 부분 구현 | `specs/evidence/20260720_04/adm-ui-browser-smoke-result.sanitized.json`, `specs/evidence/20260720_04/bza-ui-static-result.sanitized.json`, `specs/evidence/20260720_04/openapi-runtime-result.sanitized.json` | ADM·BZA UI 정적 계약과 실행 앱 HTTP는 검증했으나 시스템 브라우저 자동화 파이프 차단 및 전용 Chromium 인증서 체인 오류로 실제 렌더링 클릭은 미검증임 |
| browser-auth-e2e | 부분 구현 | `specs/evidence/20260720_04/adm-forced-password-change.sanitized.json`, `specs/evidence/20260720_04/adm-operation-console-runtime-result.sanitized.json`, `specs/evidence/20260720_04/adm-ui-browser-smoke-result.sanitized.json` | ADM 로그인·강제 비밀번호 변경·인증 운영 API는 실검증했으나 인증 후 브라우저 메뉴 클릭은 환경 제약으로 미검증임 |
| multi-instance-runtime | 미검증 | `없음` | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| security-static | 완료 | `specs/evidence/20260716_01/quality-gate.sanitized.log` | 평문 secret·보안 seed·민감 헤더 우회와 PFW 보안 정적 gate를 최신 source에서 통과함 |
| bza-session-storage | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | BZA access·refresh token을 sessionStorage로 제한하고 localStorage 사용을 gate에서 차단함 |
| bza-login-history-auth | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인 이력 조회에 Bearer token과 USER:READ 서버 권한을 강제하는 테스트를 통과함 |
| service-call-boundary | 완료 | `specs/evidence/20260720_04/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260720_04/service-call-engine-runtime-success.sanitized.json` | 타 주제영역 저장소 직접 참조 없이 PFW Service Call Engine과 typed client를 사용하는 경계를 정적·실 HTTP로 검증함 |
| spring-event-usage | 완료 | `specs/evidence/20260716_01/spring-event-usage-scan.sanitized.json` | 핵심 동기 처리의 금지 Spring Event 사용 0건을 확인함 |
| profile-loading | 완료 | `specs/evidence/20260716_01/profile-loading-result.sanitized.json` | local/dev/stg/prod profile 로딩과 prod secret 기본값 금지 검사를 최신 모듈에서 통과함 |
| deploy-inventory | 완료 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC와 PFW Gateway를 local/dev/stg/prod env·inventory·runtime harness·remote deploy dry-run에 연결하고 local 선택 기동을 실검증함 |
| sql-standard | 완료 | `specs/evidence/20260720_04/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260720_04/mariadb-full-install.sanitized.log`, `specs/evidence/20260720_04/quality-gate.sanitized.log` | 실 MariaDB 전체 설치·smoke·seed 재실행·FK·index·계정 권한 38개 검사와 SQL 정적 gate를 최신 source에서 통과함 |
| utf8-mojibake | 완료 | `specs/evidence/20260720_04/quality-gate.sanitized.log` | UTF-8, PowerShell UTF-8 BOM·CRLF, mojibake 검사를 전체 source와 최신 증적에 대해 통과함 |
| ui-static | 완료 | `specs/evidence/20260716_02/adm-log-policy-ui-static-result.sanitized.json`, `specs/evidence/20260716_02/bza-ui-static-result.sanitized.json`, `specs/evidence/20260716_02/adm-ui-model-consistency.sanitized.json` | ADM/BZA 정적 UI·JavaScript와 ADM HTML/JS model 정합성 gate를 통과함. 실제 인증 후 전 화면 browser E2E는 별도 추적함 |
| sample-coverage | 완료 | `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/sample-coverage-matrix.md` | XYZ 외부 연계와 BAT 온디맨드를 포함한 공식 EDU source·test·matrix 정합성 gate를 통과함 |
| generator-cleanup | 완료 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/repository-hygiene.sanitized.json` | 생성 결과를 임시 검증 영역에 격리하고 기본 patch/profile/deploy 잔재를 만들지 않으며 저장소 가비지 gate를 통과함 |
| evidence-sanitization | 완료 | `specs/evidence/20260716_02/log-management-standard.sanitized.json`, `specs/evidence/20260716_02/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260716_02/quality-gate.sanitized.log` | 최신 정본 증적을 sanitized 파일로 제한하고 실행 메타데이터·secret 제거·본문 SHA-256 규격을 gate에서 검증함 |
| docx-openxml | 완료 | `specs/evidence/20260716_01/docx-standard.sanitized.json` | 공식 DOCX 9종의 OpenXML package 무결성을 확인함. 이번 구조 변경 freshness와 Word 실제 열기는 최종 정본화 단계로 보류 |
| readme-docs | 부분 구현 | `README.md`, `specs/sample-coverage-matrix.md` | README를 ACC reference·Gateway·O/S/B·S형 공유 API 실제 구현에 맞췄고 샘플 매트릭스를 갱신함. DOCX 9종은 요청에 따라 최종 정본화 단계로 보류 |
| quality-gate | 완료 | `specs/evidence/20260720_04/quality-gate.sanitized.log`, `specs/evidence/20260720_04/full-test-release.sanitized.log` | 최종 qualityGate 증적은 90 tasks(38 executed, 52 up-to-date)로 성공했고, 클린 실행에서 집계한 전체 156 suites, 359 tests는 failures 0, errors 0, skipped 4임 |
| request-protection | 완료 | `specs/evidence/20260720_04/cpf-current-work-request-protection.sanitized.json` | CPF_CURRENT_WORK_REQUEST.md SHA-256가 작업 baseline과 일치하며 요청 입력이 변경되지 않았음을 최신 gate에서 검증함 |
| report-matrix-consistency | 완료 | `specs/evidence/20260716_02/report-matrix-evidence-consistency.sanitized.json` | 기능 ledger의 report·matrix·GAP·evidence index 상태와 증적 파일 존재·민감정보 검사를 동일 정본으로 검증함 |
| acc-reference-domain | 부분 구현 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | ACC 생성기 reference, 대표 CRUD, SQL/Flyway, 배포 설정과 local/remote Facade를 유지하고 Gateway→ACC reference 조회를 실검증함. external Tomcat/JNDI parity는 미검증 |
| shared-api-boundary | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | S형 ID 일치, 허용 호출 서비스, 호출 인스턴스, 외부 Gateway 우회 차단과 fail-closed 운영 확장 경계를 구현·단위 검증함. mTLS adapter runtime은 미검증 |
| pfw-gateway-runtime | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | PFW 선택형 Gateway의 route snapshot·권한·채널 정책과 실제 ACC target proxy 200, 실행 ID·route·instance 헤더를 실검증함. streaming·cancellation·다중 인스턴스는 미검증 |
| batch-on-demand | 완료 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json`, `specs/sample-coverage-matrix.md` | 온디맨드 접수·멱등·비동기 worker·상태·step·stop·restart·rerun API/SQL/EDU를 구현하고 MariaDB JobRepository에서 실검증함 |
| channel-registry-policy | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | DB-first 채널 master·거래 허용 정책·identity binding·불변 snapshot·export/import를 구현하고 SQL, ADM API/UI, Gateway 실제 호출로 검증함 |
| transaction-test-console | 재확인 필요 | `없음` | O/S/B 테스트 콘솔, 운영 강제 비활성, 권한·감사·결과 포맷 runtime 검증이 남음 |
| policy-package-promotion | 재확인 필요 | `없음` | 환경 독립 정책 export/import, diff, 승인, rollback runtime 검증이 남음 |
| global-change-approval | 재확인 필요 | `없음` | ADM/BZA 전체 mutation 승인·예약 적용·rollback handler 전수 검증이 남음 |
| adm-bam-responsive-statistics | 재확인 필요 | `없음` | ADM/BZA 반응형 화면, 거래·채널 통계와 drill-down browser 검증이 남음 |
| log-raw-format | 재확인 필요 | `없음` | JSON/XML/text/fixed-length 원문·포맷·마스킹·원문 권한·다운로드 감사 browser/DB 검증이 남음 |
| configuration-secret-lifecycle | 재확인 필요 | `없음` | 설정 버전·drift·last-known-good와 secret/certificate/key rotation 외부 adapter 검증이 남음 |
| observability-alert-slo | 재확인 필요 | `없음` | metric·trace·health·SLI/SLO·alert·ack·runbook의 운영 연계 검증이 남음 |
| resource-protection | 재확인 필요 | `없음` | bulkhead·rate limit·quota·backpressure·retry budget·pool 제한 검증이 남음 |
| schema-versioning-migration | 부분 구현 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json` | DB expand/migrate 기반 한 사례는 실검증했으나 REST/event/file schema 호환과 장기 backfill/resume 표준은 재확인 필요 |
| retention-privacy-dr | 재확인 필요 | `없음` | retention·archive·purge·privacy·backup/restore·RPO/RTO·DR 실복구 검증이 남음 |
| supply-chain-performance | 재확인 필요 | `없음` | SBOM·dependency/license/secret scan과 대표 경로 성능·용량 benchmark 재검증이 남음 |
| full-capability-inventory | 부분 구현 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/feature-evidence-result.sanitized.json` | 9개 모듈 1,078개 파일 ownership과 ACC/EXS capability를 inventory화했으나 최종 목표 전체 요구사항의 source 양방향 추적은 계속 필요 |
| module-topology-authoritative | 완료 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/architecture-inventory.sanitized.json` | BZA 정식 업무 백오피스, ACC generator reference, BAT·Gateway 선택 실행, EXS 비runtime 대체 구조를 settings/빌드에서 확인함 |
| standard-execution-contract-migration | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/standard-execution-id-migration-apply.sanitized.json` | O/S/B 10자리 단일 기록과 구형 ID alias 조회 호환 migration을 실제 DB에서 검증함 |
| generator-reference-domain-contract | 부분 구현 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/gateway-bat-runtime-result.sanitized.json` | 생성 모듈의 test·bootJar·bootWar와 ACC reference HTTP 호출을 검증함. 외부 Tomcat/JNDI 실배포는 미검증임 |
| batch-dependency-ghost-contract | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 dependency/ghost 구현은 유지되며 이번에는 온디맨드 restart/rerun을 보강함. 다중 worker JobRepository runtime은 미검증 |
| cmn-telegram-contract | 재확인 필요 | `없음` | CMN 전문 layout/parser/formatter와 XYZ charset·byte length·round-trip 최신 runtime 재검증이 남음 |
| ui-design-system-contract | 재확인 필요 | `없음` | ADM/BZA 실제 browser 렌더링·접근성·반응형·history 검증이 남음 |
| evidence-governance-contract | 완료 | `specs/evidence/20260720_04/cpf-current-work-request-protection.sanitized.json`, `specs/evidence/20260720_04/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260720_04/quality-gate.sanitized.log` | 시작 SHA·활성 요청서 hash 보호, current sanitized evidence 경로·고유 ID·본문 hash·secret scan을 최신 gate에서 검증함 |
| package-structure-standard | 완료 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json` | ACC·XYZ·BAT를 포함한 9개 모듈의 package/class ownership을 inventory화하고 주제영역 저장소 직접 접근과 package 규칙 gate를 통과함 |
| repository-garbage-cleanup | 완료 | `specs/evidence/20260716_02/repository-hygiene.sanitized.json`, `specs/evidence/20260716_02/deleted-files.sanitized.json`, `specs/evidence/20260716_02/empty-directory-scan.sanitized.json` | patch-candidates·생성 결과·중복 candidate·빈 디렉터리를 정리하고 금지 산출물 재유입 gate를 통과함 |
| readme-product-document | 완료 | `README.md`, `acc/README.md` | README를 작업 일지 없이 제품 가치·아키텍처·모듈·채널·Gateway·거래·오류·신뢰성·배치·운영·개발·EDU·기술 사양 문서로 정리하고 ACC README를 모듈 진입점으로 제한함 |
| acc-generator-output-cleanup | 완료 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/repository-hygiene.sanitized.json` | ACC를 generator conformance reference로 유지하고 patch-candidates·중복 deploy/SQL·임시 생성 산출물 재유입을 차단함 |
| xyz-reference-package-standard | 완료 | `specs/evidence/20260720_04/architecture-inventory.sanitized.json`, `specs/evidence/20260720_04/sample-coverage-result.sanitized.json` | cpf.xyz.<capability> feature-first 패키지와 /api/xyz/reference canonical API를 적용하고 기존 /xyz/edu 호환 alias를 분리 검증함 |
| bat-job-package-standard | 완료 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | BAT actual job을 JobDefinition별 vertical slice로, EDU를 학습 유형별 package로 정리하고 온디맨드 JobRepository runtime을 검증함 |
| base-hierarchy | 완료 | `specs/evidence/20260720_04/base-hierarchy.sanitized.json` | 115개 Controller·Service가 PFW Base/Contract→주제영역 Base/Contract→기능 구현의 3단 계층을 따르는지 검사함 |
| platform-version-artifacts | 완료 | `specs/evidence/20260720_04/release-metadata-result.sanitized.json`, `specs/evidence/20260720_04/full-test-release.sanitized.log` | 1.0.0-SNAPSHOT 정본, cpf-* 산출물 36개, sources·Javadoc JAR 각 9개, checksum 36개와 SBOM-lite·provenance 생성을 검증함 |
| logging-ownership | 완료 | `specs/evidence/20260720_04/architecture-ownership-scan.sanitized.json` | 기술 거래 로깅 Aspect는 PFW 한 곳만 소유하고 업무 모듈의 중복 기술 Aspect가 없음을 검사함 |
| document-links | 완료 | `specs/evidence/20260720_04/document-link-check.sanitized.json` | 추적 중인 README·Markdown·HTML 14개에서 로컬 파일 링크 9개를 해석하고 broken link 0건을 확인함 |
<!-- CPF_LEDGER_END -->
