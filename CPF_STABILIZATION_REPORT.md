# CPF 안정화 작업 리포트

## 기준 정보

- 요청서: `CPF_NEW_REQUEST.md`
- 최종 목표 기준: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 증적 디렉터리: `specs/evidence/20260706_02`
- 변경파일 목록 산출물: 생성하지 않음
- 커밋/푸시/브랜치: 수행하지 않음
- 민감정보 원문: 리포트와 증적에 기록하지 않음

## 수행 작업

- `CpfFileLogWriter`의 `writeEvent`/`writeIntegration` 경로에 키 기반 민감정보 마스킹을 추가했습니다.
- 파일 로그 실패 시 `System.err` 대신 표준 logger 경고로 남기고 업무 실패로 전파하지 않게 정리했습니다.
- `CpfWebClientConfig`에 `OUTBOUND_REQUEST`, `OUTBOUND_RESPONSE`, `OUTBOUND_RESPONSE_ERROR`, `OUTBOUND_TIMEOUT`, `OUTBOUND_EXCEPTION` integration 파일 로그 hook을 연결했습니다.
- `CpfRestClientInterceptor`에 CPF 헤더 전파와 integration 파일 로그 기록을 추가하고, `RestClient`/`RestTemplate` customizer를 통해 자동 연결되게 했습니다.
- `CpfBatchRuntimeListener`가 `CpfBatchFileLogWriter`를 주입받아 Job/Step 시작·종료 시점에 batch 파일 로그 writer를 호출하는지 테스트로 확인했습니다.
- `CpfBatchFileLogWriter`의 실패 메시지가 `SensitiveDataMasker`를 거치도록 보강했습니다.
- `scripts/create-domain.ps1`에 `-GeneratePatch`, `-Apply` 후보 생성 흐름을 추가하고, SQL/seed/smoke/Flyway/README 후보 산출물을 생성하도록 확장했습니다.
- runtime smoke가 장시간 멈추지 않도록 `scripts/runtime-diagnostics.ps1`, `scripts/smoke-file-log-standard-runtime.ps1`, `scripts/smoke-trace-boost-runtime.ps1`, `scripts/smoke-runtime-closure.ps1`의 진단/포트 확인을 bounded 방식으로 보강했습니다.
- `scripts/export-sanitized-evidence.ps1`와 `scripts/check-feature-evidence.ps1`를 `20260706_02` 증적 기준에 맞췄습니다.
- `specs/기능_구현_매트릭스.html`의 runtime 실패 상태를 실제 실행 결과와 맞췄습니다.

## 검증 요약

| 항목 | 상태 | 결과 |
| --- | --- | --- |
| `:pfw:test` | 완료 | 신규 파일 로그/RestClient/BAT listener 테스트 포함 통과 |
| `:pfw:compileJava :adm:compileJava :exs:compileJava :bat:compileJava` | 완료 | 주요 모듈 컴파일 통과 |
| `gradlew test` | 완료 | 전체 테스트 통과 |
| `scripts/smoke-create-domain.ps1` | 완료 | dry-run/generate/patch 후보 생성 통과 |
| `scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime` | 실패 | ACC runtime port 미기동 |
| `scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime` | 실패 | ADM runtime port 미기동 |
| `scripts/smoke-bat-trace-boost-runtime.ps1 -RunBatRuntime -RequireRuntime` | 실패 | BAT runtime은 완료, batch 파일 로그 증거 없음 |
| `scripts/sync-runtime-smoke-summary.ps1` | 실패 | 실패 runtime 항목 포함으로 exitCode 1 |
| `scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260706_02` | 완료 | sanitized evidence 생성 |
| `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 완료 | feature evidence, HTML docs, UTF-8/mojibake, SQL, Java format, 전체 테스트 gate 통과 |
| `git diff --check` | 완료 | whitespace 오류 없음. `CPF_NEW_REQUEST.md`의 CRLF/LF 경고는 사용자 요청서 파일 변경 상태로 남김 |

## Check ID 상태

| check id | 상태 | 근거 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql` | DB slice 검증은 기존 증적 기준 완료 상태입니다. 환경변수명은 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`을 표준으로 쓰고, 레거시 호환 확인용으로 `CPF_XYZ_EDU_MAPPER_DB_USER`도 문서화합니다. |
| mariadb-full-install | 완료 | `scripts/smoke-mariadb-full-install.ps1`, `mariadb-full-install-result.sanitized.json` | 기존 MariaDB 전체 설치 smoke 증적 기준 완료 상태입니다. |
| adm-runtime | 완료 | `scripts/smoke-adm-runtime.ps1` | 기존 ADM runtime smoke 증적 기준 완료 상태입니다. |
| adm-permission-runtime | 완료 | `scripts/smoke-adm-permission-runtime.ps1` | 기존 ADM 권한 runtime smoke 증적 기준 완료 상태입니다. |
| openapi-runtime | 완료 | `openapi-runtime-result.sanitized.json` | 기존 OpenAPI runtime 증적 기준 완료 상태입니다. |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | 브라우저 드라이버 기반 클릭 검증은 이번에 실행하지 않았습니다. |
| standard-header-e2e | 완료 | `scripts/smoke-standard-header-e2e.ps1`, `standard-header-e2e-result.sanitized.json` | 기존 표준 헤더 E2E 증적 기준 완료 상태입니다. |
| complex-transaction-trace | 완료 | `composite-transaction-runtime-result.sanitized.json` | 기존 복합 거래 trace 증적 기준 완료 상태입니다. |
| transaction-segment-log | 완료 | `pfw_transaction_segment` | 기존 segment 로그 증적 기준 완료 상태입니다. |
| adm-transaction-group-list | 완료 | `/adm/api/transaction-groups` | 기존 ADM 거래 그룹 목록 증적 기준 완료 상태입니다. |
| adm-transaction-timeline | 완료 | `/timeline`, `/headers`, `/external-logs` | 기존 ADM timeline 증적 기준 완료 상태입니다. |
| cmn-fixed-length-engine | 완료 | `FixedLengthLayoutRegistry`, `cmn_fixed_length_layout` | 기존 고정길이 전문 엔진 증적 기준 완료 상태입니다. |
| composite-runtime-smoke | 완료 | `scripts/smoke-composite-transaction-runtime.ps1` | 기존 복합 runtime smoke 증적 기준 완료 상태입니다. |
| adm-transaction-group-runtime | 완료 | `scripts/smoke-adm-transaction-group-runtime.ps1` | 기존 ADM 거래 그룹 runtime 증적 기준 완료 상태입니다. |
| redis-kafka-mq-broker | 미검증 | DB fallback | 실제 Redis/Kafka/MQ broker는 이번에 띄우지 않았습니다. |
| broker-real-integration | 미검증 | 없음 | 실제 broker 연동 검증은 별도 환경이 필요합니다. |
| file-log-standard | 실패 | `file-log-standard-result.sanitized.json` | `-RequireRuntime` 실행 결과 ACC runtime port가 열려 있지 않아 실패했습니다. source-level hook과 마스킹 테스트는 통과했습니다. |
| trace-boost-runtime | 실패 | `trace-boost-runtime-result.sanitized.json` | `-RequireRuntime` 실행 결과 ADM runtime port가 열려 있지 않아 실패했습니다. |
| bat-trace-boost-runtime | 실패 | `bat-trace-boost-runtime-result.sanitized.json` | BAT runtime smoke는 완료됐지만 `logs/bat/cpf-bat-batch.log` 증거가 없어 실패했습니다. |
| create-domain-smoke | 완료 | `scripts/smoke-create-domain.ps1`, `create-domain-result.sanitized.json` | dry-run, generate, patch 후보 생성 검증을 통과했습니다. |
| runtime-smoke-summary | 실패 | `scripts/sync-runtime-smoke-summary.ps1`, `runtime-smoke-summary.sanitized.json` | 실패 runtime 항목이 포함되어 exitCode 1을 반환했습니다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 리포트 작성 후 재실행했고 통과했습니다. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | qualityGate 안에서 리포트/매트릭스 상태 일치를 확인했습니다. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | qualityGate 안에서 신규 evidence marker를 확인했습니다. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | qualityGate 안에서 UTF-8/mojibake 검사를 통과했습니다. |

## 남은 리스크

- 파일 로그 runtime grep closure는 ACC/ADM runtime port가 열려 있지 않아 실패했습니다. 서비스 기동 환경을 안정화한 뒤 같은 스크립트를 다시 실행해야 합니다.
- BAT Trace Boost runtime은 BAT Job 실행 자체는 완료됐지만 batch 파일 로그가 생성되지 않았습니다. Job listener는 연결되어 있고 단위 테스트로 writer 호출은 확인했으므로, 다음 작업에서는 실제 BAT 부트 환경에서 `CpfBatchFileLogWriter` bean 생성 여부와 `cpf.logging.file.batch-enabled` 적용 상태를 추가 진단해야 합니다.
- `scripts/smoke-runtime-closure.ps1`는 기본 실행을 안전 요약 모드로 변경했습니다. 실제 서비스 기동까지 강제하려면 `-StartServices`를 명시해야 합니다.
- Redis/Kafka/MQ broker 실연동과 ADM 브라우저 클릭 검증은 이번 작업에서 실행하지 않았습니다.

## 다음 조치

- 로컬 서비스 장시간 기동을 안정화한 뒤 `scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime`와 `scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime`를 재실행합니다.
- BAT 부트 환경에서 `CpfBatchFileLogWriter` bean 존재 여부와 `logs/bat/cpf-bat-batch.log` 생성 경로를 runtime endpoint 또는 actuator bean 목록으로 확인합니다.
- `scripts/smoke-runtime-closure.ps1 -StartServices`를 별도 터미널에서 실행해 서비스 기동 closure를 다시 닫습니다.
