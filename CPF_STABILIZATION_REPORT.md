# CPF 안정화 작업 리포트

## 기준 정보

- 요청서: `CPF_NEW_REQUEST.md`
- 최종 목표 참고: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 시작 branch: `master`
- 시작 HEAD: `004cc441ccbc4e358ab8bcde7dcd52690477dab4`
- 시작 origin/master: `004cc441ccbc4e358ab8bcde7dcd52690477dab4`
- 시작 상태: `CPF_NEW_REQUEST.md`, `CPF_STABILIZATION_REPORT.md` 변경 상태에서 작업 시작
- 금지 준수: commit, push, branch 생성, 별도 변경파일 목록 산출물 생성 없음

## 수행 작업

- PFW 복합 거래 segment 표준을 추가했습니다.
  - `TransactionSegmentService`, `TransactionSegmentContext`, `TransactionSegmentScope`, `TransactionSegmentRecord`
  - `TransactionSegmentRole`, `TransactionSegmentDirection`, `TransactionSegmentStatus`
  - `TransactionSegmentMapper`, `TransactionSegmentMapper.xml`
- 표준 헤더에 복합 거래 구간 전파 헤더를 추가했습니다.
  - `X-Root-Transaction-Id`
  - `X-Transaction-Segment-Id`
  - `X-Parent-Transaction-Segment-Id`
  - `X-Transaction-Call-Depth`
- SQL 기준을 추가했습니다.
  - `pfw_transaction_segment`
  - `V19__transaction_segment_trace.sql`
  - `99_smoke_check.sql` table smoke 항목
- 복합 거래 교육 샘플을 추가했습니다.
  - `POST /acc/edu/composite/member-then-external`
  - `POST /acc/edu/composite/member-calls-external`
  - `GET /mbr/edu/composite/member-profile`
  - `GET /mbr/edu/composite/member-calls-external`
  - `POST /api/exs/edu/external-transfer`
  - `GET /xyz/edu/transactions/composite-sample`
- ADM 거래 그룹 조회 API를 추가했습니다.
  - `GET /adm/api/transaction-groups`
  - `GET /adm/api/transaction-groups/{transactionGlobalId}`
  - `GET /adm/api/transaction-groups/{transactionGlobalId}/segments`
  - `GET /adm/api/transaction-groups/{transactionGlobalId}/timeline`
  - `GET /adm/api/transaction-groups/{transactionGlobalId}/headers`
  - `GET /adm/api/transaction-groups/{transactionGlobalId}/external-logs`
- Runtime smoke 스크립트를 추가했습니다.
  - `scripts/smoke-composite-transaction-runtime.ps1`
  - `scripts/smoke-adm-transaction-group-runtime.ps1`
- CMN 고정길이 전문 skeleton을 보강했습니다.
  - `FixedLengthLayoutRegistry`
  - `FixedLengthGroupSpec`
  - `FixedLengthMessageError`
  - `FixedLengthMessageException`
  - `FixedLengthTypeConverter`
- 문서와 gate를 현행화했습니다.
  - `README.md`
  - `specs/index.html`
  - `specs/트랜잭션_가이드.html`
  - `specs/표준_헤더_가이드.html`
  - `specs/관리자_가이드.html`
  - `specs/개발_가이드.html`
  - `specs/SQL_가이드.html`
  - `specs/기능_구현_매트릭스.html`
  - `scripts/check-html-docs.ps1`
  - `scripts/check-feature-evidence.ps1`

## 기능 상태

| 항목 | 상태 | 근거 | 남은 확인 |
| --- | --- | --- | --- |
| PFW 복합 거래 segment 표준 | 완료 | `TransactionSegmentService`, `TransactionSegmentMapper.xml`, `CpfHeaderPropagator` | 운영 부하와 장기 보존 정책 검증 |
| `pfw_transaction_segment` SQL | 완료 | `specs/sql/10_pfw_schema.sql`, `V19__transaction_segment_trace.sql`, `99_smoke_check.sql` | 실제 MariaDB 전체 재설치 검증 |
| ACC/MBR/EXS 복합 거래 샘플 | 부분 구현 | Controller/Service/API 추가 및 모듈 테스트 통과 | runtime 서비스 기동 smoke |
| ADM 거래 그룹 API | 부분 구현 | `AdmTransactionGroupController`, `AdmTransactionGroupService` | runtime DB 데이터 조회와 브라우저 UX |
| CMN fixed-length 엔진 skeleton | 완료 | `FixedLengthLayoutRegistry`, `FixedLengthTypeConverter`, `FixedLengthMessageParserFormatterTest` | 반복부 고급 포맷과 전문 사전 DB화 |
| 문서/품질 gate 연결 | 완료 | README, 가이드, 기능 매트릭스, feature evidence 검사 갱신 | 최종 정본화 디자인 |

## 검증 상태

| check id | 상태 | 증거 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 미검증 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql`, `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, `CPF_XYZ_EDU_MAPPER_DB_USER` | 이번 작업에서는 DB slice 환경변수를 지정해 실행하지 않았습니다. |
| mariadb-full-install | 미검증 | `scripts/smoke-mariadb-full-install.ps1` | 전체 DB 재설치 검증은 실행하지 않았습니다. |
| adm-runtime | 미검증 | `scripts/smoke-adm-runtime.ps1` | ADM 앱 기동 runtime smoke는 이번 작업에서 실행하지 않았습니다. |
| adm-permission-runtime | 미검증 | `scripts/smoke-adm-permission-runtime.ps1` | 권한 runtime smoke는 이번 작업에서 실행하지 않았습니다. |
| openapi-runtime | 미검증 | `scripts/smoke-openapi.ps1` | 앱 기동 후 OpenAPI JSON 검증은 실행하지 않았습니다. |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | 브라우저 클릭 자동화는 실행하지 않았습니다. |
| standard-header-e2e | 미검증 | `scripts/smoke-standard-header-e2e.ps1` | 이번 작업에서는 실행하지 않았습니다. |
| complex-transaction-trace | 완료 | `AccCompositeTransactionService`, `MbrCompositeTransactionService`, `ExsCompositeEducationController` | 컴파일과 모듈 테스트 기준으로 확인했습니다. |
| transaction-segment-log | 완료 | `pfw_transaction_segment`, `TransactionSegmentMapper.xml` | SQL과 MyBatis 저장 경로를 추가했습니다. |
| adm-transaction-group-list | 부분 구현 | `GET /adm/api/transaction-groups` | API는 추가됐고 runtime DB 조회 검증은 남아 있습니다. |
| adm-transaction-timeline | 부분 구현 | `GET /adm/api/transaction-groups/{transactionGlobalId}/timeline` | timeline API는 추가됐고 브라우저 UX 검증은 남아 있습니다. |
| cmn-fixed-length-engine | 완료 | `FixedLengthLayoutRegistry`, `FixedLengthTypeConverter`, `FixedLengthMessageParserFormatterTest` | CMN 단위 테스트를 통과했습니다. |
| composite-runtime-smoke | 미검증 | `scripts/smoke-composite-transaction-runtime.ps1` | ACC/MBR/EXS 로컬 서비스 기동 후 실행해야 합니다. |
| adm-transaction-group-runtime | 미검증 | `scripts/smoke-adm-transaction-group-runtime.ps1` | ACC와 ADM runtime 기동 후 실행해야 합니다. |
| redis-kafka-mq-broker | 미검증 | Redis/Kafka/MQ broker | 외부 broker 실연동 검증은 이번 작업 범위가 아닙니다. |
| broker-real-integration | 미검증 | Redis/Kafka 실 broker | 실 broker 장애 시나리오 테스트는 실행하지 않았습니다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 리포트 작성 후 실행 결과와 맞춥니다. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 리포트와 기능 매트릭스 check id 상태 일치 기준입니다. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | 복합 거래, ADM 거래 그룹, CMN fixed-length 증거를 포함합니다. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | 리포트 작성 후 실행 결과와 맞춥니다. |

## 실행한 검증

- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-all-install-sql.ps1`
- `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain`
- `.\gradlew.bat :cmn:test --offline --no-daemon --console=plain`
- `.\gradlew.bat :acc:test --offline --no-daemon --console=plain`
- `.\gradlew.bat :mbr:test --offline --no-daemon --console=plain`
- `.\gradlew.bat :exs:test --offline --no-daemon --console=plain`
- `.\gradlew.bat :adm:test --offline --no-daemon --console=plain`
- `.\gradlew.bat :xyz:test --offline --no-daemon --console=plain`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake`
- `.\gradlew.bat qualityGate --offline --no-daemon --console=plain`
- Runtime smoke 사전 포트 확인: ACC 8080, MBR 8081, ADM 8090, EXS 8092 모두 TCP 연결 실패

## 미검증 또는 보류

- `scripts/smoke-composite-transaction-runtime.ps1`: ACC/MBR/EXS runtime 서비스 포트가 닫혀 있어 실행하지 않았습니다.
- `scripts/smoke-adm-transaction-group-runtime.ps1`: ACC/ADM runtime 서비스 포트가 닫혀 있어 실행하지 않았습니다.
- `scripts/smoke-mariadb-full-install.ps1`: 전체 DB 재설치/초기화 위험이 있어 이번 작업에서 실행하지 않았습니다.
- `scripts/smoke-standard-header-e2e.ps1`: 이번 요청의 필수 runtime 실행 범위에서 제외했습니다.
- Redis/Kafka/MQ broker 실연동: 외부 broker 환경이 필요해 미검증입니다.

## 다음 보강 후보

1. ACC/MBR/EXS/ADM 로컬 서비스를 기동한 뒤 복합 거래 runtime smoke와 ADM 거래 그룹 runtime smoke를 실행합니다.
2. ADM 화면에 거래 그룹 timeline tree, segment 상세, header snapshot, external logs 탭을 연결하고 브라우저 클릭 검증을 추가합니다.
3. 실제 MariaDB에서 `00_all_install_and_smoke.sql`을 실행해 FK, index, seed idempotent, `pfw_transaction_segment` 생성 여부를 확인합니다.
4. CMN fixed-length 반복부, 전문 사전 DB화, 필드별 오류코드 메시지 연동을 확장합니다.
5. broker 실연동 환경에서 Redis/Kafka/MQ 이벤트 전파와 DB fallback 장애 시나리오를 검증합니다.

## 항상 지켜야 할 기준 점검

- 문서와 소스, SQL, Swagger, EDU 샘플은 같은 명칭과 경로 기준으로 맞췄습니다.
- README는 짧은 진입점으로 유지하고 상세 내용은 `specs` 가이드에 반영했습니다.
- 신규 주석과 SQL COMMENT는 한글 기준으로 작성했습니다.
- 실행하지 않은 검증은 완료로 보고하지 않고 미검증으로 분리했습니다.
