# CPF 통합 안정화 보고서

## 기준

- 작업일: `2026-07-16`
- 기준 branch: `master`
- 시작 commit: `0788fee1eb329f9ba971660d5958a309f544fcbf`
- 요청서: `CPF_NEW_REQUEST.md`
- 요청서 SHA-256: `fc44647df0ab15c6e4edecde68ef2703133da3853a7bb1289e7cae2f40fceaaa`
- 최종 목표 SHA-256: `28564a5ae4553c4c7509fb61d7e1b0d02eb679b524e7d0e6e7c3bb1ff17dc774`
- commit/push: 수행하지 않음

이번 작업은 최신 요청서의 O/S/B 표준 실행 ID, ACC 생성기 reference domain, PFW Gateway, BAT 온디맨드, S형 내부 공유 API, 배포·SQL·증적 기준을 구현하고 검증한 마일스톤입니다. 장기 최종 목표 전체를 완료한 것으로 판정하지 않습니다.

## 수행 작업

### 표준 실행과 PFW 코어

- O/S/B 실행 ID를 `[유형 1][주제영역 3][기능 2][순번 4]` 10자리 규격으로 통일했습니다.
- 구형 실행 ID 327건을 `pfw_standard_execution_alias`로 분리하고 Flyway V32 업그레이드를 추가했습니다.
- `pfwJdbcTemplate` 소유권을 PFW 코어로 이동해 모든 실행 모듈이 동일한 표준 실행 카탈로그 저장 경계를 사용하게 했습니다.
- DB 저장 실패를 조용히 삼키지 않고 메모리 fallback과 운영 경고 로그를 함께 남기도록 보강했습니다.
- S형 공유 API의 실행 ID 일치, 허용 호출자, 내부 신원, 공개 Gateway 우회 차단 기준을 적용했습니다.

### ACC reference domain과 생성기

- `acc`를 선택형 생성기 검증 reference domain으로 복원하고 build, profile, 배포 inventory, SQL/Flyway, OpenAPI를 연결했습니다.
- ACC 전용 DataSource, JdbcTemplate, 트랜잭션 관리자, MyBatis factory/template을 명시해 다중 DataSource 선택 모호성을 제거했습니다.
- 대표 계정 CRUD에 validation, 검색·정렬 whitelist, 낙관적 버전, 논리 삭제, 감사 사유, before/after 이력을 적용했습니다.
- ACC Spring Batch `JobRepository`는 PFW DB를 사용하고 업무 Step은 ACC 트랜잭션 관리자를 사용하도록 분리했습니다.
- 생성기는 위 DataSource·MyBatis·BatchRepository 경계, 자동 Job 실행 차단, bootJar/bootWar 구성을 함께 생성합니다.

### Gateway·배치·공통 계약

- `pfw-gateway-runtime`을 PFW 소유 선택형 실행 모듈로 추가하고 route snapshot, 실행 ID route, 권한 port, 내부 헤더 재생성, health/OpenAPI를 연결했습니다.
- ACC와 Gateway를 local/dev/stg/prod 환경 파일, deploy inventory, runtime harness, Java 25·패키지 검사, remote deploy dry-run에 포함했습니다.
- BAT 온디맨드 202 접수, 멱등 저장, worker 실행, 상태·step 조회, stop·restart·rerun 계약과 Flyway V34를 추가했습니다.
- CMN facade contract와 MBR→ACC remote proxy, XYZ 외부 연계 EDU를 보강했습니다.

### SQL·증적·문서

- 분할 SQL, 단일 설치 SQL 2종, smoke, seed, Flyway V1/V32/V33/V34를 동기화했습니다.
- ACC app 계정은 DML만, migration 계정은 DDL을 수행하도록 권한을 분리했습니다.
- README를 현재 모듈 구조와 PFW/CMN 소유권, O/S/B, 선택형 ACC/Gateway, 생성 배치 DB 경계에 맞췄습니다.
- 원시 로그는 정본 증적에서 제거하고 비밀정보가 없는 정제 JSON·로그만 유지했습니다.
- 요청 지시에 따라 DOCX 9종은 이번 구조 안정화 단계에서 재생성하지 않았습니다.

## 실행 검증

| 검증 | 결과 | 핵심 확인 |
|---|---|---|
| ACC/Gateway 집중 build·test | 완료 | ACC test·bootJar, Gateway test·bootJar 성공 |
| PFW/ACC/ADM/BAT 회귀 | 완료 | PFW 카탈로그·DataSource 테스트, ACC 전체 테스트, ADM/BAT 컴파일 성공 |
| 생성기 smoke | 완료 | 임시 PYM 생성, test, bootJar, bootWar, Java 25 검증 후 정리 |
| MariaDB 전체 설치 | 완료 | all-install·smoke, seed 재실행, FK/index, 권한 분리 성공 |
| ACC/Gateway 실기동 | 완료 | ACC `8082`, Gateway `8070`, health HTTP 200, 종료 정리 성공 |
| OpenAPI runtime | 완료 범위 | ACC 4 paths/3 tags, Gateway 2 paths/1 tag 확인 |
| ACC 실DB CRUD | 완료 | create/read/update/delete, 논리 삭제 1건, 감사 행위 3건 |
| 거래·카탈로그 저장 | 완료 범위 | ACC 거래 로그 4건, O/S/B 표준 실행 카탈로그 8건 영속 등록 |
| 배치 메타 소유권 | 완료 | `accDB.BATCH_*` 0개, `pfwDB.BATCH_*` 9개 |
| 전체 Gradle test | 완료 | 152 suites, 342 tests, failures 0, errors 0, skipped 4 |
| 전체 qualityGate | 완료 | 82 tasks, Java 25 major 69, SQL·UTF-8·mojibake·배포·증적 gate 성공 |

주요 정제 증적:

- `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json`
- `specs/evidence/20260716_01/mariadb-full-install.sanitized.json`
- `specs/evidence/20260716_01/create-domain-result.sanitized.json`
- `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`

## 작업 중 발견과 조치

| 발견 | 원인 | 조치 |
|---|---|---|
| ACC 기동 시 JdbcTemplate 누락 | 다중 DataSource에 ACC 전용 JDBC bean 없음 | ACC 전용 JdbcTemplate과 qualifier 추가 |
| ACC MyBatis template 모호성 | PFW·CMN template과 생성 Repository 주입 충돌 | ACC factory/template 및 Repository qualifier 추가 |
| ACC가 `accDB.BATCH_*` 조회 | Spring Batch가 기본 primary DataSource를 메타 DB로 선택 | PFW BatchRepository 구성 및 자동 Job 실행 차단 |
| Gateway health DOWN | IN_MEMORY 모드에서도 Rabbit health가 활성화 | broker 기본 모드에서 Rabbit health 비활성화 |
| 표준 실행 카탈로그 DB 0건 | `pfwJdbcTemplate`이 ADM/BAT에만 존재 | PFW 코어로 bean 소유권 이동, ACC 8건 실등록 검증 |
| CLI 경고가 검증 예외로 승격 | MariaDB SSL 경고가 PowerShell native error로 처리 | 경고와 SQL 결과를 분리해 DB 판정 재실행 |

최종 미해결 코드 실패는 없습니다. 전체 테스트와 품질 게이트 결과는 위 표와 정제 증적에 반영했습니다.

## 남은 리스크

- Gateway의 실제 MBR/ACC 대상 proxy, timeout, streaming, cancellation, 다중 인스턴스 route E2E는 미검증입니다.
- external Tomcat/JNDI에서 ACC와 Gateway WAR 기동 parity는 미검증입니다.
- BAT 실제 Job 실행, checkpoint/restart, 다중 worker JobRepository 시나리오는 미검증입니다.
- MBR·ADM·BZA·XYZ·BAT를 포함한 최신 전체 서비스 동시 기동은 이번 마지막 변경 이후 재실행하지 않았습니다.
- 운영 mTLS/service token, Redis/Kafka/RabbitMQ, 파일 서버/object storage, Vault/KMS, 장시간 부하·보안·DR은 외부 환경 검증이 필요합니다.
- 인증 후 ADM/BZA browser E2E와 DOCX 최종 정본화는 후속 검증 범위입니다.

## 기준 준수

- 문서·소스·SQL·OpenAPI·EDU 상태를 같은 ledger 기준으로 동기화했습니다.
- README는 제품 진입점으로 유지하고 상세 상태는 보고서와 매트릭스로 분리했습니다.
- 신규 주석과 설명은 한글로 작성했습니다.
- 실행하지 않은 외부 환경 검증은 완료로 기록하지 않았습니다.
- `CPF_NEW_REQUEST.md`는 읽기 전용 요청 기준으로 사용했으며 수정하지 않았습니다.
- 비밀번호·JWT·서비스 secret 원문을 소스·문서·증적에 저장하지 않았습니다.

## 기능별 판정

아래 표는 `specs/기능_구현_매트릭스.json`에서 자동 생성합니다.

<!-- CPF_LEDGER_BEGIN -->
| check id | 상태 | 핵심 증적 | 판정 |
|---|---|---|---|
| baseline-module-layout | 완료 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | PFW·CMN·MBR·ADM·BZA·XYZ 기본 구성과 선택형 BAT·ACC·PFW Gateway 모듈을 settings 및 빌드 기준으로 확인함 |
| bza-rename | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BIZADM 소스·패키지·환경·DB 명칭을 BZA로 전환하고 legacy name gate를 통과함 |
| acc-exs-cleanup | 부분 구현 | `specs/evidence/20260716_01/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | 삭제 기능 inventory와 ACC reference·XYZ 외부 연계 EDU·MBR→ACC 계약을 복원했고 ACC embedded HTTP/DB CRUD를 실검증함. 49개 전체 기능 runtime E2E는 남음 |
| architecture-ownership | 완료 | `specs/evidence/20260716_01/architecture-ownership-scan.sanitized.json` | 최신 모듈 구조에서 타 주제영역 Repository·Mapper·DB 직접 접근 금지와 PFW/CMN 소유권 검사를 통과함 |
| password-hashing | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW PBKDF2 버전 hash, legacy verify·rehash와 ADM/BZA 사용 테스트를 통과함 |
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-refresh-rotation | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | refresh token hash 저장, 조건부 폐기, 회전과 동시 재사용 거부 테스트를 통과함 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-permission-server | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | 메뉴·행위 권한을 서버 필터에서 검사하고 로그인 이력 USER:READ 권한 테스트를 통과함 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | ADM 154개 OpenAPI path와 UI를 기동 확인, DB 기반 운영 데이터·인증 후 화면은 미검증 |
| adm-permission | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 메뉴·버튼·API 권한 서비스 테스트는 통과했으나 계정별 200/403 런타임은 미검증 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-local | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | allowlist root, 경로 이탈·symlink·확장자·크기 제한, SHA-256, 마스킹 preview와 다운로드 테스트를 통과함 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| remote-log-bundle-jobs | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 비동기 ZIP 작업 상태, 소유자 격리, 요청 한도, 부분 실패, 만료, 1회성 다운로드 token과 재발급을 구현하고 단위·ADM UI 정적 테스트를 통과함 |
| attachment-storage | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/attachment-edu-runtime.sanitized.log` | PFW 첨부 저장 port와 로컬 adapter의 경로·symlink·확장자·크기·content type·SHA-256 검증, XYZ EDU 단위 테스트와 저장·재조회 HTTP 런타임을 통과함 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-id | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | O/S/B 10자리 값 객체·annotation·중복 gate·327개 alias와 V28→V32 실 MariaDB 전환을 검증함 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260716_01/mariadb-full-install.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | PFW 공통 pfwJdbcTemplate 소유권을 정리하고 ACC 시작 시 O/S/B 실행 메타 8건의 pfwDB 영속 등록을 실검증함. 전체 모듈 catalog·route·ADM 정합성 E2E는 남음 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC CRUD 호출 4건이 34자리 거래 ID와 O 실행 ID로 pfw_transaction_log에 적재됨을 확인함. Gateway→target 다구간 전파 E2E는 남음 |
| batch-standard | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC 배치 JobRepository를 PFW DB에 고정하고 accDB BATCH_* 0개, pfwDB BATCH_* 9개를 실검증함. 실제 Job 실행·restart는 BAT runtime에서 남음 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 미검증 | `없음` | BAT test는 통과했으나 선택 runtime 기동과 MariaDB JobRepository 실행은 아직 수행하지 않음 |
| domain-generator | 완료 | `specs/evidence/20260716_01/create-domain-result.sanitized.json` | 최신 생성기로 임시 PYM 모듈을 순수 생성해 전용 DataSource/MyBatis, PFW BatchRepository, 자동 Job 실행 차단, test·bootJar·bootWar·Java 25를 검증하고 정리함 |
| xyz-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json` | XYZ/BAT 공개 capability 대비 EDU 샘플 카탈로그 49/49 매핑을 통과함 |
| bat-edu | 부분 구현 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | tasklet·chunk·retry·restart·idempotency·center-cut 샘플은 있으나 실 JobRepository 검증은 미실행 |
| ai-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/ai-edu-runtime.sanitized.json` | PFW provider·embedding·vector port와 XYZ deterministic 구조화 출력·streaming·tool·RAG·fallback·token·사람 승인 테스트 및 표준 헤더를 포함한 HTTP 200 런타임 검증을 완료함. 실 provider는 외부 자격정보 항목으로 미검증 |
| standard-header | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | 호출 서비스/인스턴스 재생성과 S형 ingress 차단 단위 테스트를 통과함. 실제 MBR→ACC HTTP 전파는 미검증 |
| service-call-engine | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | MBR→ACC Remote Facade Proxy와 endpoint/실행 ID 계약 테스트를 추가함. 다중 인스턴스 runtime은 미검증 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| mariadb-full-install | 완료 | `specs/evidence/20260716_01/mariadb-full-install.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | 실 MariaDB에서 전체 설치·smoke·seed 재실행·FK/index와 ACC app/migration 권한 분리, ACC CRUD·감사·로그·배치 메타 소유권을 검증함 |
| flyway-static | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json` | 기존 V28 checksum을 보존하고 V32 증분 migration을 격리 DB에 실제 적용해 327개 alias와 구형 fixture 전환을 검증함 |
| sql-all-install | 완료 | `specs/evidence/20260716_01/mariadb-full-install.sanitized.json` | split SQL에서 SOURCE 없는 합본과 V1 baseline을 재생성하고 실 MariaDB 설치·재실행을 검증함 |
| runtime-baseline | 부분 구현 | `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | 신규 ACC와 PFW Gateway embedded bootJar를 동시에 기동해 health 200과 종료 정리를 검증함. MBR·ADM·BZA·XYZ·BAT를 포함한 최신 전체 묶음 재기동은 남음 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC OpenAPI 4 paths/3 tags와 Gateway 2 paths/1 tag를 실제 /v3/api-docs에서 검증함. 전체 실행 모듈을 동일 최신 build로 재검증하는 단계는 남음 |
| browser-public-http | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log` | ADM·BZA HTML HTTP 200은 확인했으나 내장 browser가 없어 실제 렌더링·console 검증은 미실행 |
| browser-auth-e2e | 미검증 | `없음` | DB·bootstrap 인증정보와 browser 연결이 없어 로그인 이후 E2E를 실행하지 않음 |
| multi-instance-runtime | 미검증 | `없음` | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| security-static | 완료 | `specs/evidence/20260716_01/quality-gate.sanitized.log` | 평문 secret·보안 seed·민감 헤더 우회와 PFW 보안 정적 gate를 최신 source에서 통과함 |
| bza-session-storage | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | BZA access·refresh token을 sessionStorage로 제한하고 localStorage 사용을 gate에서 차단함 |
| bza-login-history-auth | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인 이력 조회에 Bearer token과 USER:READ 서버 권한을 강제하는 테스트를 통과함 |
| service-call-boundary | 완료 | `specs/evidence/20260716_01/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260716_01/quality-gate.sanitized.log` | MBR→ACC가 CMN Facade Contract와 PFW Service Call Engine을 사용하며 타 주제영역 저장소 직접 참조 gate를 통과함 |
| spring-event-usage | 완료 | `specs/evidence/20260716_01/spring-event-usage-scan.sanitized.json` | 핵심 동기 처리의 금지 Spring Event 사용 0건을 확인함 |
| profile-loading | 완료 | `specs/evidence/20260716_01/profile-loading-result.sanitized.json` | local/dev/stg/prod profile 로딩과 prod secret 기본값 금지 검사를 최신 모듈에서 통과함 |
| deploy-inventory | 완료 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC와 PFW Gateway를 local/dev/stg/prod env·inventory·runtime harness·remote deploy dry-run에 연결하고 local 선택 기동을 실검증함 |
| sql-standard | 완료 | `specs/evidence/20260716_01/mariadb-full-install.sanitized.json`, `specs/evidence/20260716_01/quality-gate.sanitized.log` | prefix·공통 컬럼·COMMENT·FK·index·seed·합본 정적 gate와 실 MariaDB 설치를 통과함 |
| utf8-mojibake | 완료 | `specs/evidence/20260716_01/quality-gate.sanitized.log` | UTF-8, PowerShell BOM/CRLF와 mojibake 검사를 최신 변경 파일 포함 전체 gate에서 통과함 |
| ui-static | 완료 | `specs/evidence/20260716_01/adm-log-policy-ui-static-result.sanitized.json`, `specs/evidence/20260716_01/bza-ui-static-result.sanitized.json` | ADM/BZA 정적 UI·JavaScript gate를 통과함. 실제 인증 후 browser E2E는 별도 미검증 |
| sample-coverage | 완료 | `specs/evidence/20260716_01/sample-coverage-result.sanitized.json`, `specs/sample-coverage-matrix.md` | XYZ 외부 연계와 BAT 온디맨드를 포함한 EducationSample 51건의 source·test·matrix 정합성 gate를 통과함 |
| generator-cleanup | 완료 | `specs/evidence/20260715_01/create-domain.sanitized.log` | 생성기 스모크 성공 후 임시 source·verification 디렉터리가 제거됨 |
| evidence-sanitization | 완료 | `specs/evidence/20260716_01/log-management-standard.sanitized.json`, `specs/evidence/20260716_01/quality-gate.sanitized.log` | 최신 정본 증적을 sanitized 파일로 제한하고 실행 메타데이터·secret 제거·본문 SHA-256 규격을 gate에서 검증함 |
| docx-openxml | 완료 | `specs/evidence/20260716_01/docx-standard.sanitized.json` | 공식 DOCX 9종의 OpenXML package 무결성을 확인함. 이번 구조 변경 freshness와 Word 실제 열기는 최종 정본화 단계로 보류 |
| readme-docs | 부분 구현 | `README.md`, `specs/sample-coverage-matrix.md` | README를 ACC reference·Gateway·O/S/B·S형 공유 API 실제 구현에 맞췄고 샘플 매트릭스를 갱신함. DOCX 9종은 요청에 따라 최종 정본화 단계로 보류 |
| quality-gate | 완료 | `specs/evidence/20260716_01/quality-gate.sanitized.log`, `specs/evidence/20260716_01/full-test.sanitized.log` | 최종 qualityGate 82 tasks가 성공했고 전체 152 suites, 342 tests, failures 0, errors 0, skipped 4를 정제 증적으로 기록함 |
| request-protection | 완료 | `specs/evidence/20260716_01/cpf-new-request-protection.sanitized.json` | 요청서 SHA-256가 작업 시작 baseline과 일치하며 요청서를 수정하지 않음 |
| report-matrix-consistency | 완료 | `specs/evidence/20260716_01/report-matrix-evidence-consistency.sanitized.json` | 88개 check ID의 report·기능 matrix·GAP·evidence index 상태와 evidence 파일 존재·민감정보 검사를 통과함 |
| acc-reference-domain | 부분 구현 | `specs/evidence/20260716_01/acc-pure-generated-inventory.sanitized.json`, `specs/evidence/20260716_01/create-domain-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC 생성기 reference domain, 대표 CRUD, SQL/Flyway, 배포 설정, local/remote Facade를 유지하고 embedded HTTP/DB CRUD·감사·OpenAPI를 실검증함. external Tomcat/JNDI parity는 미검증 |
| shared-api-boundary | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | S형 ID 일치, 허용 호출 서비스, 호출 인스턴스, 외부 Gateway 우회 차단과 fail-closed 운영 확장 경계를 구현·단위 검증함. mTLS adapter runtime은 미검증 |
| pfw-gateway-runtime | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | PFW 선택형 Gateway core/runtime, route snapshot, 권한 port와 proxy 단위 테스트를 구현하고 embedded health·OpenAPI를 실검증함. 실제 MBR/ACC target proxy·streaming·cancellation runtime은 미검증 |
| batch-on-demand | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/sample-coverage-matrix.md` | 온디맨드 접수·멱등·비동기 worker·상태·step·stop·restart·rerun API/SQL/EDU 테스트를 구현함. MariaDB JobRepository runtime은 미검증 |
| channel-registry-policy | 재확인 필요 | `없음` | 통합 채널 master, 거래별 허용 채널, client/service identity binding과 immutable snapshot 전수 검증이 남음 |
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
| full-capability-inventory | 부분 구현 | `specs/evidence/20260716_01/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | 이번 마일스톤 ACC/EXS/O-S-B/Gateway/BAT 범위 inventory는 완료했으나 25MB 최종 목표 전체 capability의 source 양방향 추적은 계속 필요 |
| module-topology-authoritative | 완료 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | BZA 정식 업무 백오피스, ACC generator reference, BAT·Gateway 선택 실행, EXS 비runtime 대체 구조를 settings/빌드에서 확인함 |
| standard-execution-contract-migration | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/standard-execution-id-migration-apply.sanitized.json` | O/S/B 10자리 단일 기록과 구형 ID alias 조회 호환 migration을 실제 DB에서 검증함 |
| generator-reference-domain-contract | 부분 구현 | `specs/evidence/20260716_01/acc-pure-generated-inventory.sanitized.json`, `specs/evidence/20260716_01/create-domain-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | 생성기 순수 산출물과 독립 PYM smoke, generated ACC embedded startup·CRUD·registry를 확인함. external Tomcat/JNDI E2E는 미검증 |
| batch-dependency-ghost-contract | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 dependency/ghost 구현은 유지되며 이번에는 온디맨드 restart/rerun을 보강함. 다중 worker JobRepository runtime은 미검증 |
| cmn-telegram-contract | 재확인 필요 | `없음` | CMN 전문 layout/parser/formatter와 XYZ charset·byte length·round-trip 최신 runtime 재검증이 남음 |
| ui-design-system-contract | 재확인 필요 | `없음` | ADM/BZA 실제 browser 렌더링·접근성·반응형·history 검증이 남음 |
| evidence-governance-contract | 부분 구현 | `specs/evidence/20260716_01/work-start.sanitized.json`, `specs/evidence/20260716_01/cpf-new-request-protection.sanitized.json`, `specs/evidence/20260716_01/quality-gate.sanitized.log` | 시작 SHA·정본/요청 hash, 정제 evidence 경로와 최신 qualityGate를 유지함. DOCX freshness와 최종 worktree manifest는 최종 배포 정본화 단계로 보류 |
<!-- CPF_LEDGER_END -->
