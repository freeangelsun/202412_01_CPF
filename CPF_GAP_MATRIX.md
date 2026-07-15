# CPF GAP 매트릭스

생성 시각: 2026-07-15 10:08:25 +09:00

`완료`가 아닌 항목만 표시하며, 외부 환경 선행조건은 완료로 승격하지 않습니다.

| check id | 상태 | 현재 증적 | 남은 GAP |
|---|---|---|---|
| edu-mapper-db-slice | 미검증 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | MariaDB EDU mapper slice가 현재 전체 테스트에서 환경 선행조건으로 건너뜀. fixture: xyz_edu_query_fixture.sql |
| mariadb-full-install | 미검증 | `specs/evidence/20260714_02/mariadb-full-install-result.sanitized.json` | CLI는 확인했으나 root·migration·app 비밀번호 환경변수가 없어 DB 접속과 설치 SQL 실행은 시도하지 않음. 재현: scripts/smoke-mariadb-full-install.ps1 |
| adm-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 프로세스·포트·OpenAPI는 확인했으나 DB 로그인과 운영 API 실데이터 조회는 미검증 |
| adm-permission-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 필터·서비스 권한 테스트는 통과했으나 DB 계정별 200/403 런타임은 미검증 |
| adm-browser-click | 미검증 | `없음` | ADM bootstrap 자격증명과 DB 연결이 없어 실제 로그인·브라우저 클릭은 미실행 |
| standard-header-e2e | 부분 구현 | `specs/evidence/20260714_02/standard-header-e2e-result.sanitized.json` | 수신·금지 헤더 차단·하위 호출 전파는 완료, DB 로그·ADM 조회는 자격증명 부재로 미검증. 재현: scripts/smoke-standard-header-e2e.ps1 |
| complex-transaction-trace | 부분 구현 | `specs/evidence/20260714_02/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 복합거래 소스·단위 테스트는 통과했으나 현재 런타임은 DB 인증 실패로 HTTP 500을 반환함 |
| transaction-segment-log | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | segment·timeline·fallback 단위 테스트는 통과했으나 MariaDB 행과 ADM tree 연계는 미검증 |
| adm-transaction-group-list | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 기동은 완료했으나 DB 기반 거래 그룹 목록 실데이터 조회는 미검증 |
| adm-transaction-timeline | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | timeline 소스·테스트는 확인했으나 ADM DB 런타임은 미검증 |
| composite-runtime-smoke | 부분 구현 | `specs/evidence/20260714_02/composite-transaction-runtime-result.sanitized.json` | 실제 호출은 수행했으나 DB 인증 실패로 완료 조건을 검증하지 못함 |
| adm-transaction-group-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 애플리케이션은 기동했으나 DB 기반 거래 그룹 API 시나리오는 미검증 |
| redis-kafka-mq-broker | 미검증 | `없음` | 실 Redis·Kafka·RabbitMQ broker가 제공되지 않아 미실행 |
| broker-real-integration | 미검증 | `없음` | 실 broker 장애·fallback·재처리 통합 시나리오는 미실행 |
| trace-boost-runtime | 미검증 | `없음` | 운영 DB·권한을 사용하는 trace boost 실런타임은 미실행 |
| bat-trace-boost-runtime | 미검증 | `없음` | BAT trace boost 실런타임은 미실행 |
| runtime-closure | 미검증 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-stop-services-result.sanitized.json` | 기동·상태·종료는 확인했으나 DB 의존 개별 runtime smoke 전체 묶음은 완료하지 못함 |
| adm-operation-console-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | ADM 프로세스와 UI 소스는 확인했으나 DB 기반 운영 콘솔 시나리오는 미검증 |
| bat-log-bean-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | BAT 로그 bean·listener 테스트는 통과했으나 JobRepository DB 런타임은 미검증 |
| exs-timeout-retry-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | timeout/retry 단위 테스트는 통과했으나 외부 테스트 서버 런타임은 미검증 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260714_02/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260714_02/service-call-engine-failover.sanitized.json`, `specs/evidence/20260714_02/service-call-engine-circuit-transition.sanitized.json`, `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 성공·retry·failover·circuit 계약과 단위 테스트는 통과, DB 복합거래 런타임은 미완료 |
| adm-service-registry-runtime | 부분 구현 | `specs/evidence/20260714_02/service-registry-runtime-result.sanitized.json`, `specs/evidence/20260714_02/adm-service-registry-ui-static-smoke.sanitized.json` | registry source/SQL과 ADM UI 계약은 통과했으나 DB 실데이터 runtime은 미검증 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | PFW broker port·adapter와 outbox/inbox/DLQ 단위 테스트 통과, 실 broker는 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | PFW 파일전송 engine·gateway 테스트 통과, 실 SFTP/FTP/SCP 서버는 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | 보안·credential 계약 테스트 통과, 실 Vault/KMS는 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | worker·lock·재처리 계약 테스트 통과, 다중 인스턴스는 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | ADM 공개 port와 상태 조회 테스트 통과, DB 운영화면 실데이터는 미검증 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260714_02/runtime-config-inventory.sanitized.json` | 배포 명세·dry-run 소스 계약은 확인했으나 원격 대상 실행은 미검증 |
| bat-edu-package | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | BAT EDU 소스와 테스트는 포함됐으나 DB 기반 center-cut 테스트 1건이 건너뜀 |
| bat-job-log-policy | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | JobInstance 로그 경로·lease 테스트는 통과, 공유 스토리지 다중 프로세스는 미검증 |
| runtime-smoke-summary | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | 7개 기동과 OpenAPI는 완료했으나 DB·broker·browser 포함 전체 runtime bundle은 미완료 |
