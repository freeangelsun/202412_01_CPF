# CPF GAP 매트릭스

생성 시각: 2026-07-16 16:53:33 +09:00

`완료`가 아닌 항목만 표시하며, 외부 환경 선행조건은 완료로 승격하지 않습니다.

| check id | 상태 | 현재 증적 | 남은 GAP |
|---|---|---|---|
| acc-exs-cleanup | 부분 구현 | `specs/evidence/20260716_01/acc-exs-capability-inventory.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | 삭제 기능 inventory와 ACC reference·XYZ 외부 연계 EDU·MBR→ACC 계약을 복원했고 ACC embedded HTTP/DB CRUD를 실검증함. 49개 전체 기능 runtime E2E는 남음 |
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
| standard-execution-catalog | 부분 구현 | `specs/evidence/20260716_01/mariadb-full-install.sanitized.json`, `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | PFW 공통 pfwJdbcTemplate 소유권을 정리하고 ACC 시작 시 O/S/B 실행 메타 8건의 pfwDB 영속 등록을 실검증함. 전체 모듈 catalog·route·ADM 정합성 E2E는 남음 |
| execution-log-propagation | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC CRUD 호출 4건이 34자리 거래 ID와 O 실행 ID로 pfw_transaction_log에 적재됨을 확인함. Gateway→target 다구간 전파 E2E는 남음 |
| batch-standard | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC 배치 JobRepository를 PFW DB에 고정하고 accDB BATCH_* 0개, pfwDB BATCH_* 9개를 실검증함. 실제 Job 실행·restart는 BAT runtime에서 남음 |
| scheduler-dependency | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 영업일·허용시간·시뮬레이션·선후행·trigger·실행대상 API/UI는 구현, DB 실행 시나리오는 미검증 |
| batch-ghost | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | heartbeat 기반 ghost 후보·조치·운영 로그를 구현, 다중 worker 오탐 검증은 미실행 |
| bat-runtime | 미검증 | `없음` | BAT test는 통과했으나 선택 runtime 기동과 MariaDB JobRepository 실행은 아직 수행하지 않음 |
| bat-edu | 부분 구현 | `specs/evidence/20260715_01/sample-coverage-result.sanitized.json`, `specs/evidence/20260715_01/quality-gate.sanitized.log` | tasklet·chunk·retry·restart·idempotency·center-cut 샘플은 있으나 실 JobRepository 검증은 미실행 |
| standard-header | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | 호출 서비스/인스턴스 재생성과 S형 ingress 차단 단위 테스트를 통과함. 실제 MBR→ACC HTTP 전파는 미검증 |
| service-call-engine | 부분 구현 | `specs/evidence/20260716_01/feature-evidence-result.sanitized.json` | MBR→ACC Remote Facade Proxy와 endpoint/실행 ID 계약 테스트를 추가함. 다중 인스턴스 runtime은 미검증 |
| broker-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | PFW broker port, outbox·inbox·DLQ와 adapter 테스트는 통과, 실 broker는 미검증 |
| broker-real-integration | 미검증 | `없음` | Redis·Kafka·RabbitMQ 서버가 제공되지 않아 실 장애·fallback·replay를 실행하지 않음 |
| file-transfer-capability | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 파일 검증·checksum·임시파일·이동·이력·원격 명령 계획 테스트는 통과함 |
| file-server-real-integration | 미검증 | `없음` | SFTP·FTP·FTPS·SCP·SSH 실 서버가 없어 전송 runtime은 실행하지 않음 |
| runtime-baseline | 부분 구현 | `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | 신규 ACC와 PFW Gateway embedded bootJar를 동시에 기동해 health 200과 종료 정리를 검증함. MBR·ADM·BZA·XYZ·BAT를 포함한 최신 전체 묶음 재기동은 남음 |
| openapi-runtime | 부분 구현 | `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | ACC OpenAPI 4 paths/3 tags와 Gateway 2 paths/1 tag를 실제 /v3/api-docs에서 검증함. 전체 실행 모듈을 동일 최신 build로 재검증하는 단계는 남음 |
| browser-public-http | 부분 구현 | `specs/evidence/20260715_01/runtime-start-services.sanitized.log` | ADM·BZA HTML HTTP 200은 확인했으나 내장 browser가 없어 실제 렌더링·console 검증은 미실행 |
| browser-auth-e2e | 미검증 | `없음` | DB·bootstrap 인증정보와 browser 연결이 없어 로그인 이후 E2E를 실행하지 않음 |
| multi-instance-runtime | 미검증 | `없음` | 2개 instance registry·failover·lease·worker claim·graceful shutdown 환경을 실행하지 않음 |
| readme-docs | 부분 구현 | `README.md`, `specs/sample-coverage-matrix.md` | README를 ACC reference·Gateway·O/S/B·S형 공유 API 실제 구현에 맞췄고 샘플 매트릭스를 갱신함. DOCX 9종은 요청에 따라 최종 정본화 단계로 보류 |
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
| generator-reference-domain-contract | 부분 구현 | `specs/evidence/20260716_01/acc-pure-generated-inventory.sanitized.json`, `specs/evidence/20260716_01/create-domain-result.sanitized.json`, `specs/evidence/20260716_01/acc-gateway-runtime.sanitized.json` | 생성기 순수 산출물과 독립 PYM smoke, generated ACC embedded startup·CRUD·registry를 확인함. external Tomcat/JNDI E2E는 미검증 |
| batch-dependency-ghost-contract | 부분 구현 | `specs/evidence/20260715_01/quality-gate.sanitized.log` | 기존 dependency/ghost 구현은 유지되며 이번에는 온디맨드 restart/rerun을 보강함. 다중 worker JobRepository runtime은 미검증 |
| cmn-telegram-contract | 재확인 필요 | `없음` | CMN 전문 layout/parser/formatter와 XYZ charset·byte length·round-trip 최신 runtime 재검증이 남음 |
| ui-design-system-contract | 재확인 필요 | `없음` | ADM/BZA 실제 browser 렌더링·접근성·반응형·history 검증이 남음 |
| evidence-governance-contract | 부분 구현 | `specs/evidence/20260716_01/work-start.sanitized.json`, `specs/evidence/20260716_01/cpf-new-request-protection.sanitized.json`, `specs/evidence/20260716_01/quality-gate.sanitized.log` | 시작 SHA·정본/요청 hash, 정제 evidence 경로와 최신 qualityGate를 유지함. DOCX freshness와 최종 worktree manifest는 최종 배포 정본화 단계로 보류 |
