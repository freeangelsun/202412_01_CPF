# CPF 안정화 작업 리포트

## 1. 작업 요약

이번 작업은 `CPF_NEW_REQUEST.md`와 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 읽어, 복합 거래 trace, ADM 거래 그룹 조회, EXS 송수신 원장 저장, CMN fixed-length 사전 DB, MariaDB full install smoke, sanitized evidence 산출, OpenAPI/runtime smoke를 검증 가능한 상태로 보강했다.

요청 파일은 수정하지 않았다. `CPF_NEW_REQUEST.md`와 `CPF_FINAL_TARGET_REQUIREMENTS.md`는 확인용으로만 읽었고, 기존 사용자 변경 상태는 보존했다.

## 2. 주요 반영 내용

- EXS datasource 기본값을 local runtime 기준 활성화로 변경해 `exsTransactionManager` 미생성으로 인한 503 실패를 수정했다.
- EXS REST 송수신 성공/실패 경로가 `exs_transaction_log`, `exs_message_log`에 실제 원장 로그를 남기도록 보강했다.
- ADM 거래 그룹 조회가 `pfw_transaction_segment.external_*` 추정값보다 EXS 원장 로그를 우선 조회하도록 정리했다.
- `X-User-Id`, `X-Customer-No`, `X-Member-No`, `X-Operator-Id`, `X-Client-App-Id`, `X-Caller-Service`를 거래 segment 검색/조회 관점에서 분리했다.
- ACC 복합 거래 교육 API에 EXS 실패 경로를 추가하고, 실패 응답에 `failureCode`, `failureMessageMasked`, `failedSegmentId`, `failedModuleCode`를 남겼다.
- CMN fixed-length 사전용 `cmn_fixed_length_layout`, `cmn_fixed_length_group`, `cmn_fixed_length_field`, `cmn_fixed_length_masking_policy` SQL과 seed/smoke를 추가했다.
- `scripts/smoke-composite-transaction-failure-runtime.ps1`, `scripts/smoke-adm-transaction-group-failure-runtime.ps1`, `scripts/export-sanitized-evidence.ps1`를 추가했다.
- ADM 거래 그룹 smoke의 ADM 인증 누락 문제를 보강했다.
- sanitized evidence를 `specs/evidence/20260703_04` 아래에 생성했다.

## 3. 필수 검증 상태

| check id | 상태 | evidence | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `:xyz:test --tests cpf.xyz.edu.repository.XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql` | `CPF_XYZ_EDU_MAPPER_DB_USERNAME`와 호환용 `CPF_XYZ_EDU_MAPPER_DB_USER` 기준을 확인했다. |
| mariadb-full-install | 완료 | `scripts/smoke-mariadb-full-install.ps1`, `specs/evidence/20260703_04/mariadb-full-install-result.sanitized.json` | `00_all_install_and_smoke.sql`, FK/index/seed/idempotent, CMN fixed-length, EXS 원장 컬럼을 확인했다. |
| adm-runtime | 완료 | `scripts/smoke-adm-runtime.ps1 -BuildBeforeRun`, `build/runtime-smoke/adm-runtime-smoke-result.json` | ADM 단독 runtime smoke는 완료했다. OpenAPI는 별도 runtime smoke로 재검증했다. |
| adm-permission-runtime | 완료 | `scripts/smoke-adm-permission-runtime.ps1`, `build/runtime-smoke/adm-permission-runtime-result.json` | ADM 권한 runtime smoke를 재실행했다. |
| openapi-runtime | 완료 | `scripts/smoke-openapi.ps1`, `specs/evidence/20260703_04/openapi-runtime-result.sanitized.json` | 이번 기동 범위인 ADM/ACC/MBR/EXS 기준으로 `/v3/api-docs`를 검증했다. XYZ/BIZADM은 이번 runtime 기동 대상이 아니었다. |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick`, `specs/evidence/20260703_04/adm-ui-browser-smoke-result.sanitized.json` | Node/npm/npx/Playwright/브라우저 드라이버가 PATH에 없어 실제 클릭 검증은 스킵되었다. 정적 UI marker 검사는 통과했다. |
| standard-header-e2e | 완료 | `scripts/smoke-standard-header-e2e.ps1`, `specs/evidence/20260703_04/standard-header-e2e-result.sanitized.json` | 표준/확장 헤더 전파, 차단 헤더 거부, DB 로그 detail, 민감 헤더 미원문 저장을 확인했다. |
| complex-transaction-trace | 완료 | `scripts/smoke-composite-transaction-runtime.ps1`, `scripts/smoke-composite-transaction-failure-runtime.ps1` | ACC, MBR, EXS 성공/실패 복합 거래 trace를 runtime으로 확인했다. |
| transaction-segment-log | 완료 | `pfw_transaction_segment`, `TransactionSegmentMapper.xml`, `specs/evidence/20260703_04/adm-transaction-group-runtime-result.sanitized.json` | segment row, module flow, header/external log 연결을 ADM API로 확인했다. |
| adm-transaction-group-list | 완료 | `GET /adm/api/transaction-groups`, `specs/evidence/20260703_04/adm-transaction-group-runtime-result.sanitized.json` | transactionGlobalId 기반 그룹 목록 조회를 확인했다. |
| adm-transaction-timeline | 완료 | `GET /adm/api/transaction-groups/{transactionGlobalId}/timeline`, `specs/evidence/20260703_04/adm-transaction-group-runtime-result.sanitized.json` | segment, timeline, headers, external logs 조회를 확인했다. |
| cmn-fixed-length-engine | 완료 | `FixedLengthLayoutRegistry`, `FixedLengthMessageParserFormatterTest`, `specs/sql/20_cmn_schema.sql` | 기존 엔진 테스트와 신규 DB 사전/seed/smoke를 함께 확인했다. ADM 관리 UI는 다음 단계다. |
| composite-runtime-smoke | 완료 | `specs/evidence/20260703_04/composite-transaction-runtime-result.sanitized.json` | 성공 복합 거래 smoke가 통과했다. |
| adm-transaction-group-runtime | 완료 | `scripts/smoke-adm-transaction-group-runtime.ps1`, `scripts/smoke-adm-transaction-group-failure-runtime.ps1` | 성공/실패 거래 모두 ADM 목록/상세/timeline/external logs 조회를 통과했다. |
| redis-kafka-mq-broker | 미검증 | DB fallback 코드 경로 | 실제 Redis/Kafka/MQ broker는 이번 작업에서 띄우지 않았다. |
| broker-real-integration | 미검증 | 없음 | 실제 broker 장애/fallback 시나리오는 다음 작업 범위다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 최종 리포트 작성 후 실행했다. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 리포트와 기능 매트릭스 check id 상태 일치를 확인했다. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | 요청된 코드/SQL/문서/evidence marker를 확인했다. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | BOM 없는 UTF-8 evidence 재생성 후 통과했다. |

## 4. 실행 검증 결과

- `.\gradlew.bat :exs:compileJava :adm:compileJava :acc:compileJava --offline --no-daemon --console=plain`: 통과
- `.\gradlew.bat test --offline --no-daemon --console=plain`: 통과
- `.\gradlew.bat :xyz:test --offline --no-daemon --console=plain --tests cpf.xyz.edu.repository.XyzQueryEducationMapperSliceTest --rerun-tasks`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all-install-sql.ps1`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-mariadb-full-install.ps1 -RequireRun`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-standard-header-e2e.ps1 -RequireRuntime`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-composite-transaction-runtime.ps1 -RequireRuntime`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-composite-transaction-failure-runtime.ps1 -RequireRuntime`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-transaction-group-runtime.ps1 -RequireRuntime`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-transaction-group-failure-runtime.ps1 -RequireRuntime`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1 -RequireRuntime -SkipXyz -SkipBizAdm`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-ui.ps1 -BrowserClick -RequireBrowserClick`: 미검증, 브라우저 자동화 도구 부재로 스킵

## 5. Sanitized Evidence

- `specs/evidence/20260703_04/standard-header-e2e-result.sanitized.json`
- `specs/evidence/20260703_04/composite-transaction-runtime-result.sanitized.json`
- `specs/evidence/20260703_04/composite-transaction-failure-runtime-result.sanitized.json`
- `specs/evidence/20260703_04/adm-transaction-group-runtime-result.sanitized.json`
- `specs/evidence/20260703_04/adm-transaction-group-failure-runtime-result.sanitized.json`
- `specs/evidence/20260703_04/openapi-runtime-result.sanitized.json`
- `specs/evidence/20260703_04/adm-ui-browser-smoke-result.sanitized.json`
- `specs/evidence/20260703_04/mariadb-full-install-result.sanitized.json`
- `specs/evidence/20260703_04/run-local-services-composite-rerun.sanitized.log`
- `specs/evidence/20260703_04/runtime-smoke-summary.sanitized.json`
- `specs/evidence/20260703_04/evidence-manifest.sanitized.json`

## 6. 남은 리스크와 다음 보강 후보

- ADM browser click은 로컬에 Node/npm/npx/Playwright 또는 브라우저 드라이버가 없어 미검증이다. 도구 설치 후 같은 smoke를 재실행해야 한다.
- OpenAPI runtime은 이번 기동 범위 ADM/ACC/MBR/EXS 기준으로 통과했다. XYZ/BIZADM까지 포함한 전체 서비스 OpenAPI 검증은 별도 기동 범위에서 재확인해야 한다.
- Redis/Kafka/MQ real broker 연동과 broker 장애 시 DB fallback 시나리오는 아직 미검증이다.
- CMN fixed-length DB 사전은 SQL/seed/smoke까지 들어갔지만 ADM 관리 UI와 전문 원문 권한 감사는 다음 단계다.
- EXS 원장 로그는 REST 교육 경로 기준으로 확인했다. 실제 외부기관 adapter, timeout/retry/circuit breaker 운영 시나리오는 다음 단계다.

## 7. 항상 지킨 기준 확인

- 문서, SQL, smoke, evidence, 기능 매트릭스를 같은 check id 기준으로 맞췄다.
- 실행하지 않은 검증은 성공으로 쓰지 않았다.
- 요청 파일은 수정하지 않았다.
- 민감정보 원문은 리포트와 sanitized evidence에 남기지 않았다.
- `CPF_STABILIZATION_CHANGED_FILES.txt`는 생성하지 않았다.
