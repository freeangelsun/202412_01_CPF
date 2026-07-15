# CPF 증적 인덱스

생성 시각: 2026-07-15 16:09:47 +09:00

기준 증적 디렉터리: `specs/evidence/20260715_01`

| check id | 상태 | 증적 | 확인 기준 | 비고 |
|---|---|---|---|---|
| baseline-module-layout | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 기본 배포 pfw·cmn·mbr·adm·bza·xyz와 선택 bat 구성으로 settings·배포 설정을 통일함 |
| bza-rename | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | BIZADM 소스·패키지·환경·DB 명칭을 BZA로 전환하고 legacy name gate를 통과함 |
| acc-exs-cleanup | 완료 | `specs/evidence/20260715_01/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | ACC·EXS 모듈과 직접 참조를 baseline에서 제거함 |
| architecture-ownership | 완료 | `specs/evidence/20260715_01/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | PFW 기술 코어, CMN 프로젝트 공통, ADM 프레임워크 운영, BZA 업무 운영 소유권 검사를 통과함 |
| password-hashing | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | PFW PBKDF2 버전 hash, legacy verify·rehash와 ADM/BZA 사용 테스트를 통과함 |
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-refresh-rotation | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | refresh token hash 저장, 조건부 폐기, 회전과 동시 재사용 거부 테스트를 통과함 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | report/matrix/evidence 정합성 | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-permission-server | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 메뉴·행위 권한을 서버 필터에서 검사하고 로그인 이력 USER:READ 권한 테스트를 통과함 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | report/matrix/evidence 정합성 | ADM 154개 OpenAPI path와 UI를 기동 확인, DB 기반 운영 데이터·인증 후 화면은 미검증 |
| adm-permission | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 메뉴·버튼·API 권한 서비스 테스트는 통과했으나 계정별 200/403 런타임은 미검증 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | report/matrix/evidence 정합성 | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-local | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | allowlist root, 경로 이탈·symlink·확장자·크기 제한, SHA-256, 마스킹 preview와 다운로드 테스트를 통과함 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| remote-log-bundle-jobs | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 비동기 ZIP 작업 상태, 소유자 격리, 요청 한도, 부분 실패, 만료, 1회성 다운로드 token과 재발급을 구현하고 단위·ADM UI 정적 테스트를 통과함 |
| attachment-storage | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/attachment-edu-runtime.sanitized.log` | report/matrix/evidence 정합성 | PFW 첨부 저장 port와 로컬 adapter의 경로·symlink·확장자·크기·content type·SHA-256 검증, XYZ EDU 단위 테스트와 저장·재조회 HTTP 런타임을 통과함 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-id | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 온라인·배치 ID 322건의 16자 형식, 유형, 0000 금지와 전역 중복 검사를 통과함 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 시작 스캔과 메모리 fallback 기동은 확인, pfw_standard_execution 실 DB 등록은 미검증 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 표준 ID·transactionGlobalId의 로그 context 연계 테스트는 통과, 운영 DB·다중 instance는 미검증 |
| batch-standard | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | Spring Batch port, 실행·step·lock·운영 API와 EDU는 구현, JobRepository 실 DB는 미검증 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 미검증 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | BAT compile·test·bootJar는 통과했으나 선택 실행 모듈 runtime과 DB job 실행은 이번에 미실행 |
| domain-generator | 완료 | `specs/evidence/20260715_01/create-domain.sanitized.log` | report/matrix/evidence 정합성 | 온라인·배치·BZA 메뉴·서비스 레지스트리 후보 생성 후 test·bootJar·major 69를 통과함 |
| xyz-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | XYZ/BAT 공개 capability 대비 EDU 샘플 카탈로그 49/49 매핑을 통과함 |
| bat-edu | 부분 구현 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | tasklet·chunk·retry·restart·idempotency·center-cut 샘플은 있으나 실 JobRepository 검증은 미실행 |
| ai-edu | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log`, `specs/evidence/20260715_01/ai-edu-runtime.sanitized.json` | report/matrix/evidence 정합성 | PFW provider·embedding·vector port와 XYZ deterministic 구조화 출력·streaming·tool·RAG·fallback·token·사람 승인 테스트 및 표준 헤더를 포함한 HTTP 200 런타임 검증을 완료함. 실 provider는 외부 자격정보 항목으로 미검증 |
| standard-header | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 헤더 검증·전파·로그 context 테스트와 EDU는 완료, 이번 세션 DB 로그·하위 E2E는 미실행 |
| service-call-engine | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | timeout·retry·failover·circuit 단위 테스트는 통과, 다중 instance 실 runtime은 미검증 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | report/matrix/evidence 정합성 | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | report/matrix/evidence 정합성 | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| mariadb-full-install | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | report/matrix/evidence 정합성 | CLI·서비스는 확인했으나 인증 환경변수가 없어 접속과 SQL 실행을 시도하지 않음 |
| flyway-static | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | V1 baseline과 V28~V31 변경 파일, naming·순서 정적 검사를 통과함 |
| sql-all-install | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 분할 SQL에서 두 단일 실행 합본과 Flyway baseline을 재생성하고 mismatch 검사를 통과함 |
| runtime-baseline | 완료 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/runtime-status.sanitized.log`, `specs/evidence/20260715_01/runtime-stop-services.sanitized.log` | report/matrix/evidence 정합성 | MBR·ADM·BZA·XYZ 프로세스·포트·HTTP 200과 종료를 실제 확인함 |
| openapi-runtime | 완료 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | report/matrix/evidence 정합성 | ADM 154, MBR 11, BZA 35, XYZ 70개 path와 필수 tag/path를 검증함 |
| browser-public-http | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log` | report/matrix/evidence 정합성 | ADM·BZA HTML HTTP 200은 확인했으나 내장 browser가 없어 실제 렌더링·console 검증은 미실행 |
| browser-auth-e2e | 미검증 | `없음` | report/matrix/evidence 정합성 | DB·bootstrap 인증정보와 browser 연결이 없어 로그인 이후 E2E를 실행하지 않음 |
| multi-instance-runtime | 미검증 | `없음` | report/matrix/evidence 정합성 | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| security-static | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 평문·raw SHA·기본 JWT secret·seed 보안과 secret scan을 통과함 |
| bza-session-storage | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | BZA access·refresh token을 sessionStorage로 제한하고 localStorage 사용을 gate에서 차단함 |
| bza-login-history-auth | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 로그인 이력 조회에 Bearer token과 USER:READ 서버 권한을 강제하는 테스트를 통과함 |
| service-call-boundary | 완료 | `specs/evidence/20260715_01/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | 타 주제영역 Repository·Mapper·DB 직접 접근 금지 검사를 통과함 |
| spring-event-usage | 완료 | `specs/evidence/20260715_01/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | 핵심 처리의 금지 Spring Event 사용 검사를 통과함 |
| profile-loading | 완료 | `specs/evidence/20260715_01/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | local·dev·stg·prod profile과 secret 기본값 금지 정적 계약을 통과함 |
| deploy-inventory | 완료 | `specs/evidence/20260715_01/runtime-config-inventory.sanitized.json` | report/matrix/evidence 정합성 | BZA 기준 dev·stg·prod inventory와 환경 파일 정적 검사를 통과함 |
| sql-standard | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | prefix·공통 컬럼·COMMENT·FK·index·seed·합본 정적 검사를 통과함 |
| utf8-mojibake | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 텍스트 UTF-8, PowerShell BOM/CRLF, mojibake와 생성 JSON no BOM 검사를 통과함 |
| ui-static | 완료 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/adm-log-policy-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | ADM 메뉴/API marker와 BZA 권한·전체 route·Node JS 문법 정적 스모크를 통과함 |
| sample-coverage | 완료 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | 공개 capability 대비 EDU sample 49/49과 placeholder 검사를 통과함 |
| generator-cleanup | 완료 | `specs/evidence/20260715_01/create-domain.sanitized.log` | report/matrix/evidence 정합성 | 생성기 스모크 성공 후 임시 source·verification 디렉터리가 제거됨 |
| evidence-sanitization | 완료 | `specs/evidence/20260715_01/log-management-standard.sanitized.json` | report/matrix/evidence 정합성 | 최종 근거 로그에 실행 메타데이터·secret 제거·본문 SHA-256을 적용함 |
| docx-openxml | 완료 | `specs/evidence/20260715_01/docx-standard.sanitized.json` | report/matrix/evidence 정합성 | 공식 DOCX 9개의 OpenXML 구조 검사를 통과함. Word 애플리케이션 실제 열기는 미검증 |
| readme-docs | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | README를 현재 모듈·기동·DB·ID·생성기 진입점으로 재작성하고 상세 문서로 연결함 |
| quality-gate | 완료 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | 최종 qualityGate에서 compile·test·SQL·UTF-8·소유권·보안·UI·evidence 검사를 통과함 |
| request-protection | 완료 | `specs/evidence/20260715_01/cpf-new-request-protection.sanitized.json` | report/matrix/evidence 정합성 | CPF_NEW_REQUEST.md SHA-256와 git blob이 작업 시작 baseline과 일치함 |
| report-matrix-consistency | 완료 | `specs/evidence/20260715_01/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | 64개 check ID의 보고서·matrix·evidence index 상태 정합성 검사를 통과함 |
