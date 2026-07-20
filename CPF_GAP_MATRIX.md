# CPF GAP 매트릭스

생성 시각: 2026-07-20 10:07:52 +09:00

`완료`가 아닌 항목만 표시하며, 외부 환경 선행조건은 완료로 승격하지 않습니다.

| check id | 상태 | 현재 증적 | 남은 GAP |
|---|---|---|---|
| bza-auth | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 로그인·잠금·비밀번호 변경·access token은 구현·테스트 완료, bzaDB 실로그인은 미검증 |
| bza-bootstrap | 미검증 | `specs/evidence/20260715_01/mariadb-full-install.sanitized.log` | 명시적 enable·승인·환경변수 기반 구현은 있으나 DB bootstrap 런타임은 미실행 |
| bza-ui | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json` | 권한 기반 조회 메뉴와 사용자·역할·메뉴·권한 등록·수정 dialog를 연결했으나 인증 후 실제 browser E2E는 미검증 |
| bza-organization-employee | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 조직·직원 조회 및 감사 사유 필수 등록·수정 API와 테스트는 완료, DB 런타임은 미검증 |
| bza-approval | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 순차·병렬 결재선, 상태 전이, 낙관적 잠금, idempotency와 감사 테스트는 완료, DB E2E는 미검증 |
| bza-audit | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 업무 변경 before·after·reason 감사 적재와 조회를 구현했으나 실 DB 행은 미검증 |
| adm-framework-console | 부분 구현 | `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/openapi-runtime-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json` | ADM DB health, 운영 API, 권한 200/403, OpenAPI 160 paths, 인증 후 채널 화면을 실기동 검증함. BZA와 전체 화면 browser E2E는 별도 추적함 |
| adm-log-console | 부분 구현 | `specs/evidence/20260715_01/openapi-runtime.sanitized.log` | 거래·상세·감사·배치·운영 로그 API/UI는 제공하나 MariaDB 실데이터 조회는 미검증 |
| remote-log-multi-instance | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | registry·node client·service credential port, timeout·부분 실패, 라우팅 ID와 checksum ZIP 테스트는 완료했으나 실 mTLS HTTP adapter와 다중 서버 E2E는 미검증 |
| bza-operation-support | 부분 구현 | `specs/evidence/20260715_01/bza-ui-static-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | BZA 대시보드·알림·첨부·저장 검색·다운로드 감사·역할 비교·권한 시뮬레이션 API/UI와 테스트는 완료, 인증 후 DB browser E2E는 미검증 |
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260716_02/mariadb-full-install-result.sanitized.json`, `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | PFW 공통 pfwJdbcTemplate 소유권과 O/S/B 실행 메타 영속화를 검증하고 Gateway가 OACCQY0001로 ACC를 실제 호출함. 전체 모듈 catalog·ADM 정합성 E2E는 남음 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json`, `specs/evidence/20260716_02/transaction-meta-runtime-smoke-result.sanitized.json` | 34자리 거래 ID, O 실행 ID와 transactionGlobalId 조회를 확인하고 Gateway→ACC에서 실행 ID·route·Gateway instance 헤더 전파를 실검증함. 다중 인스턴스 timeline은 남음 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| standard-header | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | 호출 서비스/인스턴스 재생성과 S형 ingress 차단 단위 테스트를 통과함. 실제 MBR→ACC HTTP 전파는 미검증 |
| service-call-engine | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | MBR→ACC Remote Facade Proxy와 endpoint/실행 ID 계약 테스트를 추가함. 다중 인스턴스 runtime은 미검증 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| runtime-baseline | 부분 구현 | `specs/evidence/20260716_02/adm-runtime-smoke-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | ADM을 별도 실기동하고 ACC·PFW Gateway·BAT 3개 서비스를 함께 기동해 health·업무 호출·종료를 확인함. MBR·BZA·XYZ 포함 전체 동시 기동은 남음 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260716_02/openapi-runtime-result.sanitized.json` | ADM 최신 실행본의 OpenAPI 3.1, 160 paths, 28 tags와 채널·로그·배치 필수 API를 실검증함. 나머지 실행 모듈의 동일 최신 build 검증은 남음 |
| browser-public-http | 부분 구현 | `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser.sanitized.png` | 인증된 ADM 화면에서 채널 정책 메뉴를 실제 클릭하고 6개 행 렌더링과 console error 0건을 확인함. BZA 및 전체 ADM 화면은 남음 |
| browser-auth-e2e | 부분 구현 | `specs/evidence/20260716_02/adm-channel-ui-browser-result.sanitized.json`, `specs/evidence/20260716_02/adm-channel-ui-browser.sanitized.png` | ADM 인증 후 채널 정책 메뉴 click·render를 Edge headless에서 검증함. BZA 인증과 ADM 전체 메뉴·반응형 E2E는 남음 |
| multi-instance-runtime | 미검증 | `없음` | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| readme-docs | 부분 구현 | `README.md`, `specs/sample-coverage-matrix.md` | README를 ACC reference·Gateway·O/S/B·S형 공유 API 실제 구현에 맞췄고 샘플 매트릭스를 갱신함. DOCX 9종은 요청에 따라 최종 정본화 단계로 보류 |
| acc-reference-domain | 부분 구현 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | ACC 생성기 reference, 대표 CRUD, SQL/Flyway, 배포 설정과 local/remote Facade를 유지하고 Gateway→ACC reference 조회를 실검증함. external Tomcat/JNDI parity는 미검증 |
| shared-api-boundary | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | S형 ID 일치, 허용 호출 서비스, 호출 인스턴스, 외부 Gateway 우회 차단과 fail-closed 운영 확장 경계를 구현·단위 검증함. mTLS adapter runtime은 미검증 |
| pfw-gateway-runtime | 부분 구현 | `specs/evidence/20260716_02/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | PFW 선택형 Gateway의 route snapshot·권한·채널 정책과 실제 ACC target proxy 200, 실행 ID·route·instance 헤더를 실검증함. streaming·cancellation·다중 인스턴스는 미검증 |
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
| generator-reference-domain-contract | 부분 구현 | `specs/evidence/20260716_02/create-domain-result.sanitized.json`, `specs/evidence/20260716_02/remove-domain-smoke.sanitized.json`, `specs/evidence/20260716_02/gateway-bat-runtime-result.sanitized.json` | 생성기 순수 PYM 산출물의 test·bootJar·bootWar와 제거를 확인하고 ACC embedded reference 호출을 실검증함. external Tomcat/JNDI E2E는 미검증 |
| batch-dependency-ghost-contract | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 dependency/ghost 구현은 유지되며 이번에는 온디맨드 restart/rerun을 보강함. 다중 worker JobRepository runtime은 미검증 |
| cmn-telegram-contract | 재확인 필요 | `없음` | CMN 전문 layout/parser/formatter와 XYZ charset·byte length·round-trip 최신 runtime 재검증이 남음 |
| ui-design-system-contract | 재확인 필요 | `없음` | ADM/BZA 실제 browser 렌더링·접근성·반응형·history 검증이 남음 |
| evidence-governance-contract | 부분 구현 | `specs/evidence/20260716_02/work-start.sanitized.json`, `specs/evidence/20260716_02/cpf-new-request-protection.sanitized.json`, `specs/evidence/20260716_02/evidence-path-existence-check.sanitized.json`, `specs/evidence/20260716_02/quality-gate.sanitized.log` | 시작 SHA·요청 hash, 정제 evidence 경로와 파일 존재 검사를 유지함. DOCX freshness와 최종 배포 정본화는 요청서 기준 후속 단계로 보류 |
