# CPF 증적 인덱스

생성 시각: 2026-07-20 16:20:57 +09:00

기준 증적 디렉터리: `specs/evidence/20260720_04`

| check id | 상태 | 증적 | 확인 기준 | 비고 |
|---|---|---|---|---|
| baseline-module-layout | 완료 | `specs/evidence/20260720_04/architecture-inventory.sanitized.json`, `specs/evidence/20260720_04/base-hierarchy.sanitized.json` | report/matrix/evidence 정합성 | PFW·CMN·MBR·ADM·BZA·BAT·ACC·XYZ·PFW Gateway의 모듈 구성과 공통 3단 Base 계층을 최신 source 기준으로 검증함 |
| bza-rename | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | BIZADM 소스·패키지·환경·DB 명칭을 BZA로 전환하고 legacy name gate를 통과함 |
| acc-exs-cleanup | 완료 | `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260716_02/repository-hygiene.sanitized.json` | report/matrix/evidence 정합성 | 삭제 기능 49건의 유지·대체·제거 판정을 기록하고 ACC reference, XYZ 외부 연계 EDU, MBR→ACC 계약을 복원했으며 가비지와 소유권 gate를 통과함 |
| architecture-ownership | 완료 | `specs/evidence/20260720_04/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260720_04/base-hierarchy.sanitized.json` | report/matrix/evidence 정합성 | 타 주제영역 Repository·Mapper·DB 직접 접근 금지, PFW 기술 AOP 단일 소유권과 PFW Base→주제영역 Base→기능 계층 검사를 통과함 |
| password-hashing | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | PFW PBKDF2 버전 hash, legacy verify·rehash와 ADM/BZA 사용 테스트를 통과함 |
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-refresh-rotation | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | refresh token hash 저장, 조건부 폐기, 회전과 동시 재사용 거부 테스트를 통과함 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | report/matrix/evidence 정합성 | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-permission-server | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 메뉴·행위 권한을 서버 필터에서 검사하고 로그인 이력 USER:READ 권한 테스트를 통과함 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260720_04/adm-operation-console-runtime-result.sanitized.json`, `specs/evidence/20260720_04/openapi-runtime-result.sanitized.json`, `specs/evidence/20260720_04/adm-forced-password-change.sanitized.json` | report/matrix/evidence 정합성 | ADM 인증·강제 비밀번호 변경·운영 콘솔 API와 OpenAPI 161개 경로를 실 DB 런타임에서 검증함. 브라우저 클릭은 로컬 브라우저 자동화 정책으로 미검증임 |
| adm-permission | 완료 | `specs/evidence/20260716_02/adm-permission-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 관리자 읽기 200, 조회 역할의 허용 조회 200과 금지 쓰기 403, 권한 변경 감사 사유와 before/after를 실제 ADM API에서 검증함 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | report/matrix/evidence 정합성 | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-local | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | allowlist root, 경로 이탈·symlink·확장자·크기 제한, SHA-256, 마스킹 preview와 다운로드 테스트를 통과함 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| remote-log-bundle-jobs | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 비동기 ZIP 작업 상태, 소유자 격리, 요청 한도, 부분 실패, 만료, 1회성 다운로드 token과 재발급을 구현하고 단위·ADM UI 정적 테스트를 통과함 |
| attachment-storage | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/attachment-edu-runtime.sanitized.log` | report/matrix/evidence 정합성 | PFW 첨부 저장 port와 로컬 adapter의 경로·symlink·확장자·크기·content type·SHA-256 검증, XYZ 기준 업무 샘플의 단위 테스트와 저장·재조회 HTTP 런타임을 통과함 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-id | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | report/matrix/evidence 정합성 | O/S/B 10자리 값 객체·annotation·중복 gate·327개 alias와 V28→V32 실 MariaDB 전환을 검증함 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | PFW 공통 pfwJdbcTemplate 소유권과 O/S/B 실행 메타 영속화를 검증하고 Gateway가 OACCQY0001로 ACC를 실제 호출함. 전체 모듈 catalog·ADM 정합성 E2E는 남음 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json`, `specs/evidence/20260716_02/transaction-meta-runtime-smoke-result.sanitized.json` | report/matrix/evidence 정합성 | 34자리 거래 ID, O 실행 ID와 transactionGlobalId 조회를 확인하고 Gateway→ACC에서 실행 ID·route·Gateway instance 헤더 전파를 실검증함. 다중 인스턴스 timeline은 남음 |
| batch-standard | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | Spring Batch JobRepository를 PFW DB에 고정하고 BAT 실제 Job 완료·step 조회·restart·rerun과 종료 정리를 실검증함 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 완료 | `specs/evidence/20260720_04/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | BAT를 MariaDB PFW JobRepository로 기동해 온디맨드 접수, 완료, step, restart, rerun과 프로세스 정리를 실검증함 |
| domain-generator | 완료 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/full-test-release.sanitized.log` | report/matrix/evidence 정합성 | 최소 옵션 생성 결과의 독립 DataSource, PFW Base→도메인 Base→기능 계층, null·정렬 방어, test·bootJar·bootWar·Java 25 산출물을 검증함 |
| xyz-reference-samples | 완료 | `specs/evidence/20260720_04/sample-coverage-result.sanitized.json`, `specs/evidence/20260720_04/architecture-inventory.sanitized.json` | report/matrix/evidence 정합성 | XYZ를 비운영 기본 reference business domain으로 정리하고 50개 공식 샘플의 source·test·문서 coverage를 검증함 |
| bat-edu | 완료 | `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | tasklet·chunk·retry·restart·idempotency·center-cut·온디맨드 EDU를 유지하고 실제 PFW JobRepository 실행까지 검증함 |
| ai-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/ai-edu-runtime.sanitized.json` | report/matrix/evidence 정합성 | PFW provider·embedding·vector port와 XYZ deterministic 구조화 출력·streaming·tool·RAG·fallback·token·사람 승인 테스트 및 표준 헤더를 포함한 HTTP 200 런타임 검증을 완료함. 실 provider는 외부 자격정보 항목으로 미검증 |
| standard-header | 완료 | `specs/evidence/20260720_04/standard-header-e2e-result.sanitized.json` | report/matrix/evidence 정합성 | XYZ 실제 HTTP 호출에서 표준 헤더 수신·하위 호출 전파·DB 거래 로그 연결을 E2E로 검증함 |
| service-call-engine | 완료 | `specs/evidence/20260720_04/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260720_04/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | typed 기본 API와 Gateway→ACC 표준 실행 ID 라우팅, 선택 인스턴스 헤더, 실제 HTTP 200 응답을 검증함 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | report/matrix/evidence 정합성 | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | report/matrix/evidence 정합성 | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| mariadb-full-install | 완료 | `specs/evidence/20260720_04/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260720_04/mariadb-full-install.sanitized.log` | report/matrix/evidence 정합성 | 실 MariaDB에서 전체 설치·smoke·seed 재실행과 FK·index·app/migration 권한 38개 검사를 통과함 |
| flyway-static | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json` | report/matrix/evidence 정합성 | 기존 V28 checksum을 보존하고 V32 증분 migration을 격리 DB에 실제 적용해 327개 alias와 구형 fixture 전환을 검증함 |
| sql-all-install | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/mariadb-full-install.sanitized.log` | report/matrix/evidence 정합성 | split SQL에서 SOURCE 없는 합본과 V1 baseline을 재생성하고 실 MariaDB 설치·재실행을 검증함 |
| runtime-baseline | 완료 | `specs/evidence/20260720_04/runtime-start-services-result.sanitized.json`, `specs/evidence/20260720_04/runtime-stop-services-result.sanitized.json` | report/matrix/evidence 정합성 | MBR·ADM·BZA·XYZ·ACC·Gateway·BAT 7개 최신 bootJar를 동시에 기동해 health와 프로세스 생존을 확인한 뒤 기록 PID만 정상 종료함 |
| openapi-runtime | 완료 | `specs/evidence/20260720_04/openapi-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | ADM·MBR·XYZ·BZA·BAT·ACC·Gateway의 OpenAPI 3.1 필수 tag/path를 실제 기동 상태에서 모두 검증함 |
| browser-public-http | 부분 구현 | `specs/evidence/20260720_04/adm-ui-browser-smoke-result.sanitized.json`, `specs/evidence/20260720_04/bza-ui-static-result.sanitized.json`, `specs/evidence/20260720_04/openapi-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | ADM·BZA UI 정적 계약과 실행 앱 HTTP는 검증했으나 시스템 브라우저 자동화 파이프 차단 및 전용 Chromium 인증서 체인 오류로 실제 렌더링 클릭은 미검증임 |
| browser-auth-e2e | 부분 구현 | `specs/evidence/20260720_04/adm-forced-password-change.sanitized.json`, `specs/evidence/20260720_04/adm-operation-console-runtime-result.sanitized.json`, `specs/evidence/20260720_04/adm-ui-browser-smoke-result.sanitized.json` | report/matrix/evidence 정합성 | ADM 로그인·강제 비밀번호 변경·인증 운영 API는 실검증했으나 인증 후 브라우저 메뉴 클릭은 환경 제약으로 미검증임 |
| multi-instance-runtime | 미검증 | `없음` | report/matrix/evidence 정합성 | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| security-static | 완료 | `specs/evidence/20260716_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 평문 secret·보안 seed·민감 헤더 우회와 PFW 보안 정적 gate를 최신 source에서 통과함 |
| bza-session-storage | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | BZA access·refresh token을 sessionStorage로 제한하고 localStorage 사용을 gate에서 차단함 |
| bza-login-history-auth | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 로그인 이력 조회에 Bearer token과 USER:READ 서버 권한을 강제하는 테스트를 통과함 |
| service-call-boundary | 완료 | `specs/evidence/20260720_04/architecture-ownership-scan.sanitized.json`, `specs/evidence/20260720_04/service-call-engine-runtime-success.sanitized.json` | report/matrix/evidence 정합성 | 타 주제영역 저장소 직접 참조 없이 PFW Service Call Engine과 typed client를 사용하는 경계를 정적·실 HTTP로 검증함 |
| spring-event-usage | 완료 | `specs/evidence/20260716_01/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | 핵심 동기 처리의 금지 Spring Event 사용 0건을 확인함 |
| profile-loading | 완료 | `specs/evidence/20260716_01/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | local/dev/stg/prod profile 로딩과 prod secret 기본값 금지 검사를 최신 모듈에서 통과함 |
| deploy-inventory | 완료 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | report/matrix/evidence 정합성 | ACC와 PFW Gateway를 local/dev/stg/prod env·inventory·runtime harness·remote deploy dry-run에 연결하고 local 선택 기동을 실검증함 |
| sql-standard | 완료 | `specs/evidence/20260720_04/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260720_04/mariadb-full-install.sanitized.log`, `specs/evidence/20260720_04/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 실 MariaDB 전체 설치·smoke·seed 재실행·FK·index·계정 권한 38개 검사와 SQL 정적 gate를 최신 source에서 통과함 |
| utf8-mojibake | 완료 | `specs/evidence/20260720_04/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | UTF-8, PowerShell UTF-8 BOM·CRLF, mojibake 검사를 전체 source와 최신 증적에 대해 통과함 |
| ui-static | 완료 | `specs/evidence/20260716_02/adm-log-policy-ui-static-result.sanitized.json`, `specs/evidence/20260716_02/bza-ui-static-result.sanitized.json`, `specs/evidence/20260716_02/adm-ui-model-consistency.sanitized.json` | report/matrix/evidence 정합성 | ADM/BZA 정적 UI·JavaScript와 ADM HTML/JS model 정합성 gate를 통과함. 실제 인증 후 전 화면 browser E2E는 별도 추적함 |
| sample-coverage | 완료 | `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/sample-coverage-matrix.md` | report/matrix/evidence 정합성 | XYZ 외부 연계와 BAT 온디맨드를 포함한 공식 EDU source·test·matrix 정합성 gate를 통과함 |
| generator-cleanup | 완료 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/repository-hygiene.sanitized.json` | report/matrix/evidence 정합성 | 생성 결과를 임시 검증 영역에 격리하고 기본 patch/profile/deploy 잔재를 만들지 않으며 저장소 가비지 gate를 통과함 |
| evidence-sanitization | 완료 | `specs/evidence/20260716_02/log-management-standard.sanitized.json`, `specs/evidence/20260716_02/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260716_02/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 최신 정본 증적을 sanitized 파일로 제한하고 실행 메타데이터·secret 제거·본문 SHA-256 규격을 gate에서 검증함 |
| docx-openxml | 완료 | `specs/evidence/20260716_01/docx-standard.sanitized.json` | report/matrix/evidence 정합성 | 공식 DOCX 9종의 OpenXML package 무결성을 확인함. 이번 구조 변경 freshness와 Word 실제 열기는 최종 정본화 단계로 보류 |
| readme-docs | 부분 구현 | `README.md`, `specs/sample-coverage-matrix.md` | report/matrix/evidence 정합성 | README를 ACC reference·Gateway·O/S/B·S형 공유 API 실제 구현에 맞췄고 샘플 매트릭스를 갱신함. DOCX 9종은 요청에 따라 최종 정본화 단계로 보류 |
| quality-gate | 완료 | `specs/evidence/20260720_04/quality-gate.sanitized.log`, `specs/evidence/20260720_04/full-test-release.sanitized.log` | report/matrix/evidence 정합성 | 최종 qualityGate 증적은 90 tasks(38 executed, 52 up-to-date)로 성공했고, 클린 실행에서 집계한 전체 156 suites, 359 tests는 failures 0, errors 0, skipped 4임 |
| request-protection | 완료 | `specs/evidence/20260720_04/cpf-current-work-request-protection.sanitized.json` | report/matrix/evidence 정합성 | CPF_CURRENT_WORK_REQUEST.md SHA-256가 작업 baseline과 일치하며 요청 입력이 변경되지 않았음을 최신 gate에서 검증함 |
| report-matrix-consistency | 완료 | `specs/evidence/20260716_02/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | 기능 ledger의 report·matrix·GAP·evidence index 상태와 증적 파일 존재·민감정보 검사를 동일 정본으로 검증함 |
| acc-reference-domain | 부분 구현 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | ACC 생성기 reference, 대표 CRUD, SQL/Flyway, 배포 설정과 local/remote Facade를 유지하고 Gateway→ACC reference 조회를 실검증함. external Tomcat/JNDI parity는 미검증 |
| shared-api-boundary | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | report/matrix/evidence 정합성 | S형 ID 일치, 허용 호출 서비스, 호출 인스턴스, 외부 Gateway 우회 차단과 fail-closed 운영 확장 경계를 구현·단위 검증함. mTLS adapter runtime은 미검증 |
| pfw-gateway-runtime | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | PFW 선택형 Gateway의 route snapshot·권한·채널 정책과 실제 ACC target proxy 200, 실행 ID·route·instance 헤더를 실검증함. streaming·cancellation·다중 인스턴스는 미검증 |
| batch-on-demand | 완료 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json`, `specs/sample-coverage-matrix.md` | report/matrix/evidence 정합성 | 온디맨드 접수·멱등·비동기 worker·상태·step·stop·restart·rerun API/SQL/EDU를 구현하고 MariaDB JobRepository에서 실검증함 |
| channel-registry-policy | 완료 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | DB-first 채널 master·거래 허용 정책·identity binding·불변 snapshot·export/import를 구현하고 SQL, ADM API/UI, Gateway 실제 호출로 검증함 |
| transaction-test-console | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | O/S/B 테스트 콘솔, 운영 강제 비활성, 권한·감사·결과 포맷 runtime 검증이 남음 |
| policy-package-promotion | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | 환경 독립 정책 export/import, diff, 승인, rollback runtime 검증이 남음 |
| global-change-approval | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | ADM/BZA 전체 mutation 승인·예약 적용·rollback handler 전수 검증이 남음 |
| adm-bam-responsive-statistics | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | ADM/BZA 반응형 화면, 거래·채널 통계와 drill-down browser 검증이 남음 |
| log-raw-format | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | JSON/XML/text/fixed-length 원문·포맷·마스킹·원문 권한·다운로드 감사 browser/DB 검증이 남음 |
| configuration-secret-lifecycle | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | 설정 버전·drift·last-known-good와 secret/certificate/key rotation 외부 adapter 검증이 남음 |
| observability-alert-slo | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | metric·trace·health·SLI/SLO·alert·ack·runbook의 운영 연계 검증이 남음 |
| resource-protection | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | bulkhead·rate limit·quota·backpressure·retry budget·pool 제한 검증이 남음 |
| schema-versioning-migration | 부분 구현 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json` | report/matrix/evidence 정합성 | DB expand/migrate 기반 한 사례는 실검증했으나 REST/event/file schema 호환과 장기 backfill/resume 표준은 재확인 필요 |
| retention-privacy-dr | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | retention·archive·purge·privacy·backup/restore·RPO/RTO·DR 실복구 검증이 남음 |
| supply-chain-performance | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | SBOM·dependency/license/secret scan과 대표 경로 성능·용량 benchmark 재검증이 남음 |
| full-capability-inventory | 부분 구현 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/feature-evidence-result.sanitized.json` | report/matrix/evidence 정합성 | 9개 모듈 1,078개 파일 ownership과 ACC/EXS capability를 inventory화했으나 최종 목표 전체 요구사항의 source 양방향 추적은 계속 필요 |
| module-topology-authoritative | 완료 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/architecture-inventory.sanitized.json` | report/matrix/evidence 정합성 | BZA 정식 업무 백오피스, ACC generator reference, BAT·Gateway 선택 실행, EXS 비runtime 대체 구조를 settings/빌드에서 확인함 |
| standard-execution-contract-migration | 완료 | `specs/evidence/20260716_01/standard-execution-v32-upgrade.sanitized.json`, `specs/evidence/20260716_01/standard-execution-id-migration-apply.sanitized.json` | report/matrix/evidence 정합성 | O/S/B 10자리 단일 기록과 구형 ID alias 조회 호환 migration을 실제 DB에서 검증함 |
| generator-reference-domain-contract | 부분 구현 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 생성 모듈의 test·bootJar·bootWar와 ACC reference HTTP 호출을 검증함. 외부 Tomcat/JNDI 실배포는 미검증임 |
| batch-dependency-ghost-contract | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 기존 dependency/ghost 구현은 유지되며 이번에는 온디맨드 restart/rerun을 보강함. 다중 worker JobRepository runtime은 미검증 |
| cmn-telegram-contract | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | CMN 전문 layout/parser/formatter와 XYZ charset·byte length·round-trip 최신 runtime 재검증이 남음 |
| ui-design-system-contract | 재확인 필요 | `없음` | report/matrix/evidence 정합성 | ADM/BZA 실제 browser 렌더링·접근성·반응형·history 검증이 남음 |
| evidence-governance-contract | 완료 | `specs/evidence/20260720_04/cpf-current-work-request-protection.sanitized.json`, `specs/evidence/20260720_04/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260720_04/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 시작 SHA·활성 요청서 hash 보호, current sanitized evidence 경로·고유 ID·본문 hash·secret scan을 최신 gate에서 검증함 |
| package-structure-standard | 완료 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | ACC·XYZ·BAT를 포함한 9개 모듈의 package/class ownership을 inventory화하고 주제영역 저장소 직접 접근과 package 규칙 gate를 통과함 |
| repository-garbage-cleanup | 완료 | `specs/evidence/20260716_02/repository-hygiene.sanitized.json`, `specs/evidence/20260716_02/deleted-files.sanitized.json`, `specs/evidence/20260716_02/empty-directory-scan.sanitized.json` | report/matrix/evidence 정합성 | patch-candidates·생성 결과·중복 candidate·빈 디렉터리를 정리하고 금지 산출물 재유입 gate를 통과함 |
| readme-product-document | 완료 | `README.md`, `acc/README.md` | report/matrix/evidence 정합성 | README를 작업 일지 없이 제품 가치·아키텍처·모듈·채널·Gateway·거래·오류·신뢰성·배치·운영·개발·EDU·기술 사양 문서로 정리하고 ACC README를 모듈 진입점으로 제한함 |
| acc-generator-output-cleanup | 완료 | `specs/evidence/20260720_04/create-domain-result.sanitized.json`, `specs/evidence/20260720_04/repository-hygiene.sanitized.json` | report/matrix/evidence 정합성 | ACC를 generator conformance reference로 유지하고 patch-candidates·중복 deploy/SQL·임시 생성 산출물 재유입을 차단함 |
| xyz-reference-package-standard | 완료 | `specs/evidence/20260720_04/architecture-inventory.sanitized.json`, `specs/evidence/20260720_04/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | cpf.xyz.<capability> feature-first 패키지와 /api/xyz/reference canonical API를 적용하고 기존 /xyz/edu 호환 alias를 분리 검증함 |
| bat-job-package-standard | 완료 | `specs/evidence/20260716_02/architecture-inventory.sanitized.json`, `specs/evidence/20260716_02/sample-coverage-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | BAT actual job을 JobDefinition별 vertical slice로, EDU를 학습 유형별 package로 정리하고 온디맨드 JobRepository runtime을 검증함 |
| base-hierarchy | 완료 | `specs/evidence/20260720_04/base-hierarchy.sanitized.json` | report/matrix/evidence 정합성 | 115개 Controller·Service가 PFW Base/Contract→주제영역 Base/Contract→기능 구현의 3단 계층을 따르는지 검사함 |
| platform-version-artifacts | 완료 | `specs/evidence/20260720_04/release-metadata-result.sanitized.json`, `specs/evidence/20260720_04/full-test-release.sanitized.log` | report/matrix/evidence 정합성 | 1.0.0-SNAPSHOT 정본, cpf-* 산출물 36개, sources·Javadoc JAR 각 9개, checksum 36개와 SBOM-lite·provenance 생성을 검증함 |
| logging-ownership | 완료 | `specs/evidence/20260720_04/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | 기술 거래 로깅 Aspect는 PFW 한 곳만 소유하고 업무 모듈의 중복 기술 Aspect가 없음을 검사함 |
| document-links | 완료 | `specs/evidence/20260720_04/document-link-check.sanitized.json` | report/matrix/evidence 정합성 | 추적 중인 README·Markdown·HTML 14개에서 로컬 파일 링크 9개를 해석하고 broken link 0건을 확인함 |

## 2026-07-20 정본 133개 도메인 Evidence 보정

기준 SHA: `42c0fda82e0f3061e839f69cad25bbfa9df2aa0f`

기존 evidence 범위 제한:

- `create-domain-result.sanitized.json`: PYM이며 ACC lifecycle 근거 아님
- `remove-domain-smoke.sanitized.json`: LNG/TMP이며 ACC 제거 근거 아님
- `base-hierarchy.sanitized.json`: Controller·Service 중심
- `log-management-standard.sanitized.json`: 설정·gitignore 중심
- `adm-ui-browser-smoke-result.sanitized.json`: static pass이며 browser 완료 아님
- `architecture-inventory.sanitized.json`: 경로·owner 신호이며 기능 완결성 아님
- `garbage-file-scan.sanitized.json`: stale가 남음

신규 필수 evidence:

- 133-domain requirement inventory
- repository implementation inventory
- 양방향 traceability
- ACC lifecycle·generator capability matrix
- full layer taxonomy
- PFW extension API inventory
- external integration failure/recovery
- SQL canonical source/vendor portability
- actual file/DB log parity
- 2 Batch Worker crash/lease/takeover
- ADM/BZA frontend build/browser E2E
- EDU completeness
- semantic garbage
- final document consistency

모든 evidence는 basis SHA, command, profile, DB, process/instance,
start/end, requirement ID, result, sanitization, body hash와 stale 여부를 포함한다.
