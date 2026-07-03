# CPF 안정화 작업 리포트

작성 기준: 2026-07-03  
보고 원칙: 실행하지 않은 검증은 성공으로 기록하지 않고, 민감정보 원문은 리포트에 남기지 않습니다.

## 1. 작업 요약

- ACC, MBR, EXS 복합 거래 성공 경로를 runtime smoke로 확인하고 동일 `transactionGlobalId` 기준 segment, header, external log가 ADM에서 조회되도록 보강했습니다.
- ADM 거래 그룹 화면에 목록, 검색 조건, 정렬, 페이징, 상세 탭, segment timeline, 표준 헤더, 확장 헤더, external logs 조회 흐름을 연결했습니다.
- `pfw_transaction_segment` 설치 SQL, smoke SQL, Flyway 기준 구조를 MariaDB full install smoke 검증 범위에 포함했습니다.
- CMN 고정길이 전문 엔진에 반복부 group parse/format, 필드 오류, type converter, masking 결과 구조를 보강하고 단위 테스트를 추가했습니다.
- EXS datasource 비활성 기본 구조를 유지하되 runtime smoke에서는 명시적으로 활성화해야 함을 코드 주석과 smoke 결과로 분리했습니다.
- 루트 안정화 리포트는 HTML이 아니라 `CPF_STABILIZATION_REPORT.md`만 유지하도록 정리했습니다. 별도 변경파일 목록 산출물은 만들지 않았습니다.

## 2. 주요 변경 영역

- `scripts/smoke-composite-transaction-runtime.ps1`: 표준 CPF 헤더를 포함하고 복합 거래 segment 수, module flow, 민감정보 원문 미노출을 확인하도록 보강했습니다.
- `scripts/smoke-adm-transaction-group-runtime.ps1`: ADM 거래 그룹 목록, 상세, segments, timeline, headers, external logs, DB segment row 검증을 추가했습니다.
- `scripts/smoke-adm-ui.ps1`: ADM 거래 그룹 화면과 API 연결 marker, 확장 헤더, 민감 헤더 표시 기준을 정적 smoke에 포함했습니다.
- `scripts/smoke-mariadb-full-install.ps1`: `pfw_transaction_segment` 필수 컬럼과 index 검증을 추가했습니다.
- `adm/src/main/java/cpf/adm/opr/service/AdmTransactionGroupService.java`: 거래 그룹 검색 조건을 segment, module, failure, API path, 표준/확장 헤더 기준으로 확장했습니다.
- `adm/src/main/resources/static/adm/adm.js`, `adm/src/main/resources/static/adm/index.html`: ADM 거래 그룹 운영 화면을 추가했습니다.
- `cmn/src/main/java/cpf/cmn/message/fixedlength/*`: 고정길이 전문 반복부와 오류 결과 구조를 보강했습니다.
- `cmn/src/test/java/cpf/cmn/message/fixedlength/FixedLengthMessageParserFormatterTest.java`: 반복부, type 검증, 필드 오류 테스트를 추가했습니다.
- `build.gradle`: `runLocalServices`가 필요한 서비스만 선택 기동할 수 있도록 `cpfRunServices` 옵션을 반영했습니다.
- `specs/기능_구현_매트릭스.html`: 깨진 문구를 정리하고 구현 상태와 검증 상태를 실제 evidence 기준으로 재작성했습니다.

## 3. Runtime Smoke 결과

- `scripts/smoke-standard-header-e2e.ps1`: 통과. 결과 파일은 `build/runtime-smoke/standard-header-e2e-result.json`입니다.
- `scripts/smoke-composite-transaction-runtime.ps1`: 통과. 결과 파일은 `build/runtime-smoke/composite-transaction-runtime-result.json`입니다.
- `scripts/smoke-adm-transaction-group-runtime.ps1`: 통과. 결과 파일은 `build/runtime-smoke/adm-transaction-group-runtime-result.json`입니다.
- 서비스 기동 로그 근거는 `build/runtime-smoke/run-local-services-composite-rerun.job.log`입니다.
- 검증 중 `Authorization`, `Bearer`, API key, password, secret 계열 원문이 결과 payload에 노출되지 않는지 확인했습니다.

## 4. SQL/Flyway/DB 결과

- `scripts/smoke-mariadb-full-install.ps1` 실행 결과는 `build/sql-smoke/mariadb-full-install-result.json`입니다.
- `00_all_install_and_smoke.sql`, `99_smoke_check.sql`, `50_framework_seed_data.sql` 실행과 seed 재실행 기준을 확인했습니다.
- `pfw_transaction_segment` 테이블 존재, 필수 컬럼, index 존재를 smoke 결과에 포함했습니다.
- SQL 합본은 `scripts/build-all-install-sql.ps1`로 재생성 검증합니다.
- SQL 표준은 `scripts/check-sql-standard.ps1`로 검증합니다.

## 5. 표준 헤더 규격과 전달 상태

온라인 거래에서 현재 다루는 주요 헤더는 아래 기준입니다.

- 거래/추적: `X-Transaction-Id`, `X-Trace-Id`, `X-Span-Id`, `X-Parent-Span-Id`, `X-Transaction-Segment-Id`
- 채널/호출자: `X-Original-Channel-Code`, `X-Channel-Code`, `X-Client-App-Id`, `X-Client-Version`, `X-Caller-Service`
- 사용자 식별: `X-User-Id`, `X-Customer-No`, `X-Member-No`, `X-Operator-Id`
- 외부 연계: `X-Institution-Code`, `X-External-Transaction-Id`, `X-External-Request-Id`
- 확장 헤더: `X-Cpf-Ext-*`
- 민감 헤더: `Authorization`, `X-Api-Key`, token, secret, password, credential, signature 계열은 원문 저장/노출 금지 대상입니다.

ADM 거래 그룹 상세에서는 data 영역과 header 영역을 분리합니다. 표준 헤더, 확장 헤더, external logs를 별도 탭과 API로 조회하며, 민감정보는 마스킹 또는 차단 기준을 따릅니다.

## 6. 검증 상태 매트릭스

| check id | 상태 | 근거 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 미검증 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql` | `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, `CPF_XYZ_EDU_MAPPER_DB_USER` 기반 DB slice 테스트는 이번 작업에서 실행하지 않았습니다. |
| mariadb-full-install | 완료 | `scripts/smoke-mariadb-full-install.ps1`, `build/sql-smoke/mariadb-full-install-result.json` | MariaDB full install smoke를 실행했습니다. |
| adm-runtime | 미검증 | `scripts/smoke-adm-runtime.ps1` | ADM 단독 runtime smoke는 이번 작업에서 실행하지 않았습니다. |
| adm-permission-runtime | 미검증 | `scripts/smoke-adm-permission-runtime.ps1` | 권한 runtime smoke는 이번 작업에서 실행하지 않았습니다. |
| openapi-runtime | 미검증 | `scripts/smoke-openapi.ps1` | 앱 기동 후 OpenAPI JSON 검증은 실행하지 않았습니다. |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | 브라우저 클릭 자동화는 실행하지 않았습니다. |
| standard-header-e2e | 완료 | `scripts/smoke-standard-header-e2e.ps1`, `build/runtime-smoke/standard-header-e2e-result.json` | 표준/확장 헤더 전파와 민감 헤더 차단을 확인했습니다. |
| complex-transaction-trace | 완료 | `build/runtime-smoke/composite-transaction-runtime-result.json` | ACC, MBR, EXS 복합 거래 성공 경로를 확인했습니다. |
| transaction-segment-log | 완료 | `pfw_transaction_segment`, `build/runtime-smoke/adm-transaction-group-runtime-result.json` | DB segment row와 ADM 조회를 확인했습니다. |
| adm-transaction-group-list | 완료 | `GET /adm/api/transaction-groups`, `build/runtime-smoke/adm-transaction-group-runtime-result.json` | ADM 거래 그룹 목록 조회를 runtime으로 확인했습니다. |
| adm-transaction-timeline | 완료 | `GET /adm/api/transaction-groups/{transactionGlobalId}/timeline`, `build/runtime-smoke/adm-transaction-group-runtime-result.json` | timeline, headers, external logs 조회를 확인했습니다. |
| cmn-fixed-length-engine | 완료 | `FixedLengthMessageParserFormatterTest` | 반복부 group, 필드 오류, type converter 테스트를 통과했습니다. |
| composite-runtime-smoke | 완료 | `scripts/smoke-composite-transaction-runtime.ps1` | 로컬 서비스 기동 후 복합 거래 smoke를 통과했습니다. |
| adm-transaction-group-runtime | 완료 | `scripts/smoke-adm-transaction-group-runtime.ps1` | 목록, 상세, segment, timeline, headers, external logs를 확인했습니다. |
| redis-kafka-mq-broker | 미검증 | Redis/Kafka/MQ broker | 실 broker 연동 검증은 이번 작업에서 실행하지 않았습니다. |
| broker-real-integration | 미검증 | Redis/Kafka 실 broker | 실 broker 장애 시나리오는 실행하지 않았습니다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 최종 작성 후 실행했고 통과했습니다. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 리포트와 기능 매트릭스 check id 상태 일치를 확인합니다. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | 필수 파일, API, smoke, 문서 증거를 확인합니다. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | 최종 작성 후 실행했고 통과했습니다. |

## 7. 미검증 및 보류 항목

- `edu-mapper-db-slice`: 별도 DB slice 환경변수 기반 테스트는 이번 작업에서 실행하지 않았습니다.
- `adm-runtime`: ADM 단독 runtime smoke는 복합 거래 smoke와 분리되어 있어 이번 작업에서는 실행하지 않았습니다.
- `adm-permission-runtime`: 권한 runtime smoke는 실행하지 않았습니다.
- `openapi-runtime`: 앱 기동 후 `/v3/api-docs` 검증은 실행하지 않았습니다.
- `adm-browser-click`: 정적 UI marker smoke는 수행하지만 실제 브라우저 클릭 자동화는 실행하지 않았습니다.
- `redis-kafka-mq-broker`, `broker-real-integration`: 실 broker 환경 연동 및 장애 시나리오는 실행하지 않았습니다.

## 8. 다음 보강 후보

1. ADM 브라우저 클릭 smoke를 실행해 거래 그룹 메뉴 진입, 검색, 상세 탭 전환까지 실제 화면 기준으로 확인합니다.
2. OpenAPI runtime smoke를 실행하고 `/v3/api-docs` JSON 품질 gate를 더 강하게 연결합니다.
3. 실패 복합 거래 샘플을 추가해 실패 segment, failureCode, failureMessageMasked, external timeout 경로를 ADM에서 확인합니다.
4. CMN 고정길이 전문 layout/field/group 사전을 DB화하고 ADM 조회 기능으로 확장합니다.
5. Redis/Kafka/MQ 실 broker 연동 테스트와 DB fallback 장애 시나리오를 분리 검증합니다.
