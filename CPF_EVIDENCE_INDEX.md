# CPF 증적 인덱스

생성 시각: 2026-07-15 10:08:24 +09:00

기준 증적 디렉터리: `specs/evidence/20260714_02`

| check id | 상태 | 증적 | 확인 기준 | 비고 |
|---|---|---|---|---|
| edu-mapper-db-slice | 미검증 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | MariaDB EDU mapper slice가 현재 전체 테스트에서 환경 선행조건으로 건너뜀. fixture: xyz_edu_query_fixture.sql |
| mariadb-full-install | 미검증 | `specs/evidence/20260714_02/mariadb-full-install-result.sanitized.json` | report/matrix/evidence 정합성 | CLI는 확인했으나 root·migration·app 비밀번호 환경변수가 없어 DB 접속과 설치 SQL 실행은 시도하지 않음. 재현: scripts/smoke-mariadb-full-install.ps1 |
| adm-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | ADM 프로세스·포트·OpenAPI는 확인했으나 DB 로그인과 운영 API 실데이터 조회는 미검증 |
| adm-permission-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | 필터·서비스 권한 테스트는 통과했으나 DB 계정별 200/403 런타임은 미검증 |
| openapi-runtime | 완료 | `specs/evidence/20260714_02/openapi-runtime-result.sanitized.json`, `specs/evidence/20260714_02/openapi-source-coverage.sanitized.json` | report/matrix/evidence 정합성 | 7개 실행 모듈의 OpenAPI JSON을 실제 기동 상태에서 확인함 |
| adm-browser-click | 미검증 | `없음` | report/matrix/evidence 정합성 | ADM bootstrap 자격증명과 DB 연결이 없어 실제 로그인·브라우저 클릭은 미실행 |
| standard-header-e2e | 부분 구현 | `specs/evidence/20260714_02/standard-header-e2e-result.sanitized.json` | report/matrix/evidence 정합성 | 수신·금지 헤더 차단·하위 호출 전파는 완료, DB 로그·ADM 조회는 자격증명 부재로 미검증. 재현: scripts/smoke-standard-header-e2e.ps1 |
| complex-transaction-trace | 부분 구현 | `specs/evidence/20260714_02/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | 복합거래 소스·단위 테스트는 통과했으나 현재 런타임은 DB 인증 실패로 HTTP 500을 반환함 |
| transaction-segment-log | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | segment·timeline·fallback 단위 테스트는 통과했으나 MariaDB 행과 ADM tree 연계는 미검증 |
| adm-transaction-group-list | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | ADM 기동은 완료했으나 DB 기반 거래 그룹 목록 실데이터 조회는 미검증 |
| adm-transaction-timeline | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | timeline 소스·테스트는 확인했으나 ADM DB 런타임은 미검증 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | 고정길이 전문 parser·formatter 테스트가 Java 25 전체 테스트에서 통과함 |
| composite-runtime-smoke | 부분 구현 | `specs/evidence/20260714_02/composite-transaction-runtime-result.sanitized.json` | report/matrix/evidence 정합성 | 실제 호출은 수행했으나 DB 인증 실패로 완료 조건을 검증하지 못함 |
| adm-transaction-group-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | ADM 애플리케이션은 기동했으나 DB 기반 거래 그룹 API 시나리오는 미검증 |
| redis-kafka-mq-broker | 미검증 | `없음` | report/matrix/evidence 정합성 | 실 Redis·Kafka·RabbitMQ broker가 제공되지 않아 미실행 |
| broker-real-integration | 미검증 | `없음` | report/matrix/evidence 정합성 | 실 broker 장애·fallback·재처리 통합 시나리오는 미실행 |
| file-log-standard | 완료 | `specs/evidence/20260714_02/pfw-file-log-runtime.sanitized.log`, `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json` | report/matrix/evidence 정합성 | PFW 파일 로그 런타임과 7개 모듈 로그 경로 생성을 확인함 |
| trace-boost-runtime | 미검증 | `없음` | report/matrix/evidence 정합성 | 운영 DB·권한을 사용하는 trace boost 실런타임은 미실행 |
| bat-trace-boost-runtime | 미검증 | `없음` | report/matrix/evidence 정합성 | BAT trace boost 실런타임은 미실행 |
| runtime-start-services | 완료 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | ACC·MBR·EXS·ADM·BAT·BIZADM·XYZ 7개 프로세스가 포트와 HTTP 프로브를 통과함 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260714_02/packaged-runtime-resource-check.sanitized.json` | report/matrix/evidence 정합성 | 7개 bootJar의 표준 설정·로그 리소스 패키징 검사를 통과함 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260714_02/runtime-status-result.sanitized.json`, `specs/evidence/20260714_02/runtime-stop-services-result.sanitized.json` | report/matrix/evidence 정합성 | 7개 모듈 상태 완료 후 종료와 포트 폐쇄를 확인함 |
| runtime-closure | 미검증 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-stop-services-result.sanitized.json` | report/matrix/evidence 정합성 | 기동·상태·종료는 확인했으나 DB 의존 개별 runtime smoke 전체 묶음은 완료하지 못함 |
| adm-operation-console-runtime | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | ADM 프로세스와 UI 소스는 확인했으나 DB 기반 운영 콘솔 시나리오는 미검증 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260714_02/adm-log-policy-ui-static-result.sanitized.json` | report/matrix/evidence 정합성 | 로그 정책·배치·센터컷·실행 사유 UI/API marker 정적 계약을 통과함 |
| bat-log-bean-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | BAT 로그 bean·listener 테스트는 통과했으나 JobRepository DB 런타임은 미검증 |
| exs-timeout-retry-runtime | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | timeout/retry 단위 테스트는 통과했으나 외부 테스트 서버 런타임은 미검증 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | 반복 그룹·인코딩·길이 검증을 포함한 전문 테스트가 통과함 |
| create-domain-smoke | 완료 | `specs/evidence/20260714_02/create-domain-result.sanitized.json` | report/matrix/evidence 정합성 | 임시 주제영역 생성 후 test·bootJar·Java class major 69를 확인함 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260714_02/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260714_02/service-call-engine-failover.sanitized.json`, `specs/evidence/20260714_02/service-call-engine-circuit-transition.sanitized.json`, `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | 성공·retry·failover·circuit 계약과 단위 테스트는 통과, DB 복합거래 런타임은 미완료 |
| adm-service-registry-runtime | 부분 구현 | `specs/evidence/20260714_02/service-registry-runtime-result.sanitized.json`, `specs/evidence/20260714_02/adm-service-registry-ui-static-smoke.sanitized.json` | report/matrix/evidence 정합성 | registry source/SQL과 ADM UI 계약은 통과했으나 DB 실데이터 runtime은 미검증 |
| architecture-ownership-scan | 완료 | `specs/evidence/20260714_02/architecture-ownership-scan.sanitized.json` | report/matrix/evidence 정합성 | PFW 기술 소유권과 CMN 호환 facade 경계 검사에서 실패·경고 0건 |
| spring-event-usage-scan | 완료 | `specs/evidence/20260714_02/spring-event-usage-scan.sanitized.json` | report/matrix/evidence 정합성 | 핵심 흐름의 금지 Spring Event 사용 0건 |
| pfw-broker-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | PFW broker port·adapter와 outbox/inbox/DLQ 단위 테스트 통과, 실 broker는 미검증 |
| pfw-file-transfer-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | PFW 파일전송 engine·gateway 테스트 통과, 실 SFTP/FTP/SCP 서버는 미검증 |
| pfw-security-credential-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | 보안·credential 계약 테스트 통과, 실 Vault/KMS는 미검증 |
| pfw-runtime-control-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | worker·lock·재처리 계약 테스트 통과, 다중 인스턴스는 미검증 |
| pfw-admin-status-capability | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | ADM 공개 port와 상태 조회 테스트 통과, DB 운영화면 실데이터는 미검증 |
| profile-loading-standard | 완료 | `specs/evidence/20260714_02/profile-loading-result.sanitized.json` | report/matrix/evidence 정합성 | local/dev/stg/prod profile 정적 계약을 통과함 |
| packaged-dependencies-check | 완료 | `specs/evidence/20260714_02/packaged-runtime-resource-check.sanitized.json` | report/matrix/evidence 정합성 | 7개 bootJar 의존성과 공통 리소스 포함을 확인함 |
| deploy-dry-run-standard | 부분 구현 | `specs/evidence/20260714_02/runtime-config-inventory.sanitized.json` | report/matrix/evidence 정합성 | 배포 명세·dry-run 소스 계약은 확인했으나 원격 대상 실행은 미검증 |
| garbage-file-cleanup | 완료 | `specs/evidence/20260714_02/garbage-file-scan.sanitized.json`, `specs/evidence/20260714_02/deleted-files.sanitized.json` | report/matrix/evidence 정합성 | 추적 대상 가비지와 삭제 목록 검사를 통과함 |
| empty-directory-scan | 완료 | `specs/evidence/20260714_02/empty-directory-scan.sanitized.json` | report/matrix/evidence 정합성 | 관리 대상 빈 디렉터리 검사를 통과함 |
| deploy-env-standard | 완료 | `specs/evidence/20260714_02/runtime-config-inventory.sanitized.json` | report/matrix/evidence 정합성 | 배포 환경 파일 정적 표준 검사를 통과함 |
| deploy-inventory-standard | 완료 | `specs/evidence/20260714_02/runtime-config-inventory.sanitized.json` | report/matrix/evidence 정합성 | dev/stg/prod inventory 정적 표준 검사를 통과함 |
| gradle-deploy-task-standard | 완료 | `specs/evidence/20260714_02/gradle-remote-deploy-task-scan.sanitized.json` | report/matrix/evidence 정합성 | Gradle 배포 task 표준 검사를 통과함 |
| datasource-mode-standard | 완료 | `specs/evidence/20260714_02/datasource-mode-scan.sanitized.json` | report/matrix/evidence 정합성 | datasource mode 정적 검사를 통과함 |
| local-port-duplicate-scan | 완료 | `specs/evidence/20260714_02/local-port-duplicate-scan.sanitized.json` | report/matrix/evidence 정합성 | 로컬 기본 포트 중복 0건 |
| edu-module-deploy-alias-scan | 완료 | `specs/evidence/20260714_02/edu-module-deploy-alias-scan.sanitized.json` | report/matrix/evidence 정합성 | EDU 모듈 배포 alias 위반 0건 |
| bat-edu-package | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | BAT EDU 소스와 테스트는 포함됐으나 DB 기반 center-cut 테스트 1건이 건너뜀 |
| bat-job-log-policy | 부분 구현 | `specs/evidence/20260714_02/java25-full-test.sanitized.log` | report/matrix/evidence 정합성 | JobInstance 로그 경로·lease 테스트는 통과, 공유 스토리지 다중 프로세스는 미검증 |
| sample-coverage-matrix | 완료 | `specs/evidence/20260714_02/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | 공개 capability 대비 EDU 샘플 47/47 매핑을 확인함 |
| sample-placeholder-scan | 완료 | `specs/evidence/20260714_02/sample-coverage-result.sanitized.json` | report/matrix/evidence 정합성 | 샘플 표준·placeholder 검사를 통과함 |
| evidence-path-existence-check | 완료 | `specs/evidence/20260714_02/evidence-path-existence-check.sanitized.json` | report/matrix/evidence 정합성 | 최종 문서가 참조하는 증적 경로 검사에서 누락과 실패 0건을 확인함 |
| create-domain-profile-template | 완료 | `specs/evidence/20260714_02/create-domain-result.sanitized.json` | report/matrix/evidence 정합성 | 생성 모듈의 profile·test·bootJar 템플릿을 실제 생성 결과로 확인함 |
| runtime-smoke-summary | 부분 구현 | `specs/evidence/20260714_02/runtime-start-services-result.sanitized.json`, `specs/evidence/20260714_02/runtime-status-result.sanitized.json` | report/matrix/evidence 정합성 | 7개 기동과 OpenAPI는 완료했으나 DB·broker·browser 포함 전체 runtime bundle은 미완료 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260714_02/report-matrix-evidence-consistency.sanitized.json` | report/matrix/evidence 정합성 | 61개 check ID의 report·matrix·evidence 상태 일치 검사 통과 |
| quality-gate | 완료 | `specs/evidence/20260714_02/quality-gate.sanitized.log` | report/matrix/evidence 정합성 | Java 25 기준 79개 Gradle 작업과 저장소 JUnit 290개 테스트를 포함한 qualityGate 통과, failures 0, errors 0, skipped 4. 생성 모듈 smoke 테스트는 별도 증적으로 관리함 |
| check-docx-standard | 완료 | `specs/evidence/20260714_02/docx-standard.sanitized.json`, `specs/evidence/20260714_02/docx-word-open.sanitized.json` | report/matrix/evidence 정합성 | 공식 DOCX 9개의 OpenXML 구조와 실제 Word 열기를 모두 확인함 |
| check-feature-evidence | 완료 | `specs/evidence/20260714_02/check-feature-evidence.sanitized.log` | report/matrix/evidence 정합성 | 필수 source·SQL·EDU·문서·재현 명령 증적 gate 통과 |
| check-utf8 | 완료 | `specs/evidence/20260714_02/check-utf8.sanitized.log` | report/matrix/evidence 정합성 | 저장소 텍스트 UTF-8과 mojibake 검사 통과 |
