# CPF GAP 매트릭스

생성 시각: 2026-07-16 요구사항 확장 갱신

`완료`가 아닌 항목만 표시하며, 외부 환경 선행조건은 완료로 승격하지 않습니다.

| check id | 상태 | 현재 증적 | 남은 GAP |
|---|---|---|---|
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | ADM 154개 OpenAPI path와 UI를 기동 확인, DB 기반 운영 데이터·인증 후 화면은 미검증 |
| adm-permission | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 메뉴·버튼·API 권한 서비스 테스트는 통과했으나 계정별 200/403 런타임은 미검증 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | 시작 스캔과 메모리 fallback 기동은 확인, pfw_standard_execution 실 DB 등록은 미검증 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 표준 ID·transactionGlobalId의 로그 context 연계 테스트는 통과, 운영 DB·다중 instance는 미검증 |
| batch-standard | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | Spring Batch port, 실행·step·lock·운영 API와 EDU는 구현, JobRepository 실 DB는 미검증 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 미검증 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BAT compile·test·bootJar는 통과했으나 선택 실행 모듈 runtime과 DB job 실행은 이번에 미실행 |
| bat-edu | 부분 구현 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | tasklet·chunk·retry·restart·idempotency·center-cut 샘플은 있으나 실 JobRepository 검증은 미실행 |
| standard-header | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 헤더 검증·전파·로그 context 테스트와 EDU는 완료, 이번 세션 DB 로그·하위 E2E는 미실행 |
| service-call-engine | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | timeout·retry·failover·circuit 단위 테스트는 통과, 다중 instance 실 runtime은 미검증 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| mariadb-full-install | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | CLI·서비스는 확인했으나 인증 환경변수가 없어 접속과 SQL 실행을 시도하지 않음 |
| browser-public-http | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log` | ADM·BZA HTML HTTP 200은 확인했으나 내장 browser가 없어 실제 렌더링·console 검증은 미실행 |
| browser-auth-e2e | 미검증 | `없음` | DB·bootstrap 인증정보와 browser 연결이 없어 로그인 이후 E2E를 실행하지 않음 |
| multi-instance-runtime | 미검증 | `없음` | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| standard-execution-id | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 16자리 ID 검증은 역사적 근거이며 신규 O/S/B 10자리 ID, alias, migration, OpenAPI·catalog·log 전환을 다시 구현·검증해야 함 |
| sample-coverage | 재확인 필요 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json` | 신규 channel·approval·promotion·횡단 capability의 실제 XYZ/BAT EDU, test, evidence 추가 후 matrix 재생성 필요 |
| readme-docs | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 신규 CPF 패턴과 운영 기능을 README·스펙·개발·운영 가이드에 실제 구현과 일치하게 반영 후 재검증 필요 |
| quality-gate | 재확인 필요 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 신규 전역 gate 규칙 추가 및 최신 source/test/SQL/UI/evidence 기준 재실행 필요 |
| request-protection | 재확인 필요 | `specs/evidence/20260715_01/cpf-new-request-protection.sanitized.json` | CPF_NEW_REQUEST.md 변경 후 새 baseline hash와 보호 evidence 생성 필요 |
| report-matrix-consistency | 재확인 필요 | `specs/evidence/20260715_01/report-matrix-evidence-consistency.sanitized.json` | 신규 check ID와 상태를 기능 matrix까지 반영한 뒤 consistency evidence 재생성 필요 |
| channel-registry-policy | 재확인 필요 | `CPF_NEW_REQUEST.md` 요구사항 정의, source/runtime 직접 검증 미수행 | 통합 채널 마스터, 거래별 허용 채널, client/service binding, immutable snapshot, ADM UI와 runtime 차단 검증 필요 |
| transaction-test-console | 재확인 필요 | `CPF_NEW_REQUEST.md` 요구사항 정의, source/runtime 직접 검증 미수행 | O/S/B 테스트 콘솔, JUT, prod 강제 비활성, 권한·property·profile, 결과 포맷과 감사 검증 필요 |
| policy-package-promotion | 재확인 필요 | `CPF_NEW_REQUEST.md` 요구사항 정의, source/runtime 직접 검증 미수행 | 환경 독립 정책 파일 Export/Import, 원본 보관, diff, 사전 등록, source matching, rollback 검증 필요 |
| global-change-approval | 재확인 필요 | `CPF_NEW_REQUEST.md` 요구사항 정의, source/runtime 직접 검증 미수행 | ADM/BAM/BZA 전체 mutation inventory, 자동승인, 통합 결재함, 예약 적용, apply/rollback handler 검증 필요 |
| adm-bam-responsive-statistics | 재확인 필요 | `CPF_NEW_REQUEST.md` 요구사항 정의, browser 직접 검증 미수행 | 전 화면 반응형, 거래별·채널별·성공/오류 통계, 교차 통계와 로그 drill-down 검증 필요 |
| log-raw-format | 재확인 필요 | `CPF_NEW_REQUEST.md` 요구사항 정의, MariaDB/browser 직접 검증 미수행 | JSON/XML/text/fixed-length raw·formatted 조회, masking, 보안 원문 권한·감사·다운로드 검증 필요 |
| configuration-secret-lifecycle | 재확인 필요 | `CPF_NEW_REQUEST.md` 전체 횡단 요구사항 정의 | 설정 catalog/version/drift/last-known-good와 secret·credential·certificate·key rotation 구현 및 runtime 검증 필요 |
| observability-alert-slo | 재확인 필요 | `CPF_NEW_REQUEST.md` 전체 횡단 요구사항 정의 | metric·trace·health·SLI/SLO·alert·ack·runbook과 ADM/BAM 연계 검증 필요 |
| resource-protection | 재확인 필요 | `CPF_NEW_REQUEST.md` 전체 횡단 요구사항 정의 | bulkhead·rate limit·quota·backpressure·retry budget·pool limit·overload 보호 검증 필요 |
| schema-versioning-migration | 재확인 필요 | `CPF_NEW_REQUEST.md` 전체 횡단 요구사항 정의 | REST/event/file schema versioning, deprecation, compatibility, expand/contract, backfill·resume 검증 필요 |
| retention-privacy-dr | 재확인 필요 | `CPF_NEW_REQUEST.md` 전체 횡단 요구사항 정의 | retention·archive·purge·privacy·backup·restore·RPO/RTO·DR 절차와 복구 검증 필요 |
| supply-chain-performance | 재확인 필요 | `CPF_NEW_REQUEST.md` 전체 횡단 요구사항 정의 | SBOM·dependency/license/secret scan과 대표 경로 성능·용량 benchmark 필요 |
| full-capability-inventory | 재확인 필요 | `CPF_NEW_REQUEST.md` 전체 프레임워크 전수 검토 요구사항 정의 | PFW/CMN/업무/BAT/BZA/ADM/BAM/Gateway/DB/broker/file/UI 전체 owner·상태·source·test·evidence inventory 필요 |
