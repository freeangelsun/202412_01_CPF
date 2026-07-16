# CPF 증적 인덱스

생성 시각: 2026-07-16 기존 capability 정본 편입 갱신

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
| standard-execution-id | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 신규 10자리 O/S/B 계약과 migration 직접 검증 | 기존 16자리 evidence는 역사적 근거로만 유지 |
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
| sample-coverage | 재확인 필요 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json` | 신규 capability EDU/source/test/evidence 검증 | 기존 49/49는 이전 capability 범위의 역사적 근거 |
| generator-cleanup | 완료 | `specs/evidence/20260715_01/create-domain.sanitized.log` | report/matrix/evidence 정합성 | 생성기 스모크 성공 후 임시 source·verification 디렉터리가 제거됨 |
| evidence-sanitization | 완료 | `specs/evidence/20260715_01/log-management-standard.sanitized.json` | report/matrix/evidence 정합성 | 최종 근거 로그에 실행 메타데이터·secret 제거·본문 SHA-256을 적용함 |
| docx-openxml | 완료 | `specs/evidence/20260715_01/docx-standard.sanitized.json` | report/matrix/evidence 정합성 | 공식 DOCX 9개의 OpenXML 구조 검사를 통과함. Word 애플리케이션 실제 열기는 미검증 |
| readme-docs | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 신규 패턴·운영 기능 문서와 실제 구현 정합성 | 이전 문서 evidence는 신규 요구사항 이전 기준 |
| quality-gate | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 신규 gate 규칙과 최신 master 재실행 | 이전 로그는 신규 요구사항을 검사하지 않음 |
| request-protection | 재확인 필요 | `specs/evidence/20260715_01/cpf-new-request-protection.sanitized.json` | 변경된 CPF_NEW_REQUEST.md 새 baseline hash 검증 | 기존 hash는 의도적 요청서 변경으로 stale |
| report-matrix-consistency | 재확인 필요 | `specs/evidence/20260715_01/report-matrix-evidence-consistency.sanitized.json` | 신규 check ID 포함 report/matrix/evidence 재검증 | 기존 consistency evidence는 변경 전 기준 |
| channel-registry-policy | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | 통합 채널 마스터, 거래별 허용 채널, client/service binding, immutable snapshot, ADM UI와 runtime 차단 검증 필요 |
| transaction-test-console | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | O/S/B 테스트 콘솔, JUT, prod 강제 비활성, 권한·property·profile, 결과 포맷과 감사 검증 필요 |
| policy-package-promotion | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | 환경 독립 정책 파일 Export/Import, 원본 보관, diff, 사전 등록, source matching, rollback 검증 필요 |
| global-change-approval | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | ADM/BAM/BZA 전체 mutation inventory, 자동승인, 통합 결재함, 예약 적용, apply/rollback handler 검증 필요 |
| adm-bam-responsive-statistics | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | 전 화면 반응형, 거래별·채널별·성공/오류 통계, 교차 통계와 로그 drill-down 검증 필요 |
| log-raw-format | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | JSON/XML/text/fixed-length raw·formatted 조회, masking, 보안 원문 권한·감사·다운로드 검증 필요 |
| configuration-secret-lifecycle | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | 설정 catalog/version/drift/last-known-good와 secret·credential·certificate·key rotation 구현 및 runtime 검증 필요 |
| observability-alert-slo | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | metric·trace·health·SLI/SLO·alert·ack·runbook과 ADM/BAM 연계 검증 필요 |
| resource-protection | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | bulkhead·rate limit·quota·backpressure·retry budget·pool limit·overload 보호 검증 필요 |
| schema-versioning-migration | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | REST/event/file schema versioning, deprecation, compatibility, expand/contract, backfill·resume 검증 필요 |
| retention-privacy-dr | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | retention·archive·purge·privacy·backup·restore·RPO/RTO·DR 절차와 복구 검증 필요 |
| supply-chain-performance | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | SBOM·dependency/license/secret scan과 대표 경로 성능·용량 benchmark 필요 |
| full-capability-inventory | 재확인 필요 | `없음` | source/test/SQL/OpenAPI/UI/runtime/evidence 직접 검증 | PFW/CMN/업무/BAT/BZA/ADM/BAM/Gateway/DB/broker/file/UI 전체 owner·상태·source·test·evidence inventory 필요 |
| implemented-capability-target-traceability | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | 기존 source·ledger·sample을 정본 section과 양방향 연결하고 최신 상태를 재판정해야 함 |
| module-topology-authoritative | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | BZA 정식 명칭, ACC reference 유지, EXS 기능 inventory/이전, 기본·선택 실행 topology를 source·settings·deploy에서 재확인해야 함 |
| standard-execution-contract-migration | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | 신규 10자리 O/S/B 계약과 기존 16자리 source/DB/log/OpenAPI alias·migration을 전수 확인해야 함 |
| bza-iam-operational-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | password/auth/refresh family/reuse/bootstrap/session/조직·직원·권한·saved search를 최신 DB·browser에서 재검증해야 함 |
| ai-capability-target-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | PFW AI/embedding/vector port와 XYZ deterministic EDU는 과거 근거가 있으나 보안·관측·실 provider 상태를 최신 기준으로 재검증해야 함 |
| remote-log-attachment-download-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | 원격 로그·비동기 ZIP·일회성 token·첨부 storage·download audit의 실 mTLS/object storage/browser E2E가 필요함 |
| batch-dependency-ghost-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | dependency graph·trigger·cycle/orphan·ghost 다중신호·JobRepository·multi-worker runtime을 재검증해야 함 |
| generator-reference-domain-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | create-domain의 전체 산출과 generated ACC/LNG clean generation, startup, registry, SQL, ownership을 최신 기준으로 재검증해야 함 |
| cmn-telegram-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | CMN fixed-length layout/parser/formatter와 XYZ 실제 소비 sample의 charset·byte length·round-trip을 재검증해야 함 |
| ui-design-system-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | ADM/BZA 공통 design system, 환경 표시, route guard, column preference, browser history와 접근성을 실제 browser에서 확인해야 함 |
| evidence-governance-contract | 재확인 필요 | `없음` | 최신 source/test/SQL/OpenAPI/UI/runtime와 정본 양방향 trace 검증 | evidence sanitization, request baseline hash, DOCX OpenXML/Word 구분, source-to-target consistency gate를 최신 요구로 재실행해야 함 |
