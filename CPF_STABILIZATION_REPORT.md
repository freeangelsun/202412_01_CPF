# CPF 안정화 작업 리포트

## 기준 정보

- 기준 요청서: `CPF_NEW_REQUEST.md`
- 최종 목표 기준: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 리포트 형식: Markdown 정본
- 원칙: 실행하지 않은 검증은 완료로 보고하지 않음

## 수행 작업

1. PFW 구조화 파일 로그 표준 1차 구현
   - `CpfFileLogWriter`를 추가해 `logs/{module}/cpf-{module}-{logType}.log` 규격의 JSONL 파일 로그를 기록하도록 했습니다.
   - 온라인 거래 로그는 `TransactionLogEvent`를 구독하는 `TransactionFileLogListener`에서 DB 저장 여부와 별개로 파일 로그를 남길 수 있게 했습니다.
   - WebClient 외부 호출은 `integrationFileLogFilter`로 integration 파일 로그를 남기도록 했습니다.
   - EXS 수신 로그 저장 흐름에도 integration 파일 로그 hook을 연결했습니다.

2. ADM Trace Boost 1차 구현
   - `/adm/api/log-policies/trace-boost` 정책 생성 API를 추가했습니다.
   - `/adm/api/log-policies/{policyId}/disable`, `/runtime-state`, `/history` 조회 API를 추가했습니다.
   - 기존 `pfw_log_policy_override`를 활용해 TTL 기반 온라인 거래 Trace Boost의 첫 기준을 만들었습니다.

3. BAT 파일 로그와 배치 Trace Boost 1차 연결
   - `CpfBatchFileLogWriter`를 추가했습니다.
   - `CpfBatchRuntimeListener`가 Job/Step before/after 시점에 batch 파일 로그를 남길 수 있게 했습니다.
   - Batch auto configuration에서 batch 파일 로그 writer를 주입하도록 했습니다.

4. create-domain 1차 생성기
   - `scripts/create-domain.ps1`를 추가했습니다.
   - 신규 주제영역 후보를 `build/domain-generator/{module}` 아래 생성하고, 기존 모듈/패키지/테이블 prefix 충돌을 먼저 점검하게 했습니다.
   - `scripts/smoke-create-domain.ps1`로 dry-run과 후보 파일 생성을 검증했습니다.

5. 검증/evidence 체계 보강
   - `scripts/smoke-file-log-standard-runtime.ps1`, `scripts/smoke-trace-boost-runtime.ps1`, `scripts/smoke-bat-trace-boost-runtime.ps1`를 추가했습니다.
   - `scripts/sync-runtime-smoke-summary.ps1`를 추가해 runtime smoke 결과를 한 파일로 모을 수 있게 했습니다.
   - `scripts/export-sanitized-evidence.ps1`의 기본 evidence 경로를 `specs/evidence/20260703_05`로 갱신하고 신규 결과 파일을 sanitize 대상에 포함했습니다.
   - `scripts/check-feature-evidence.ps1`와 `scripts/check-html-docs.ps1`가 이번 신규 기능의 파일/문서/evidence를 확인하도록 보강했습니다.

6. 문서 정리
   - `README.md`를 짧은 진입점으로 다시 정리했습니다.
   - `specs/기능_구현_매트릭스.html`를 신규 구현/검증 ID가 보이도록 갱신했습니다.
   - EDU mapper DB slice 기준은 표준 환경변수 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`와 기존 호환 환경변수 `CPF_XYZ_EDU_MAPPER_DB_USER`를 함께 문서에 남겼습니다.

## 검증 상태

| check id | 상태 | 증거/명령 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql` | 기존 증거 기준 |
| mariadb-full-install | 완료 | `scripts/smoke-mariadb-full-install.ps1` | 기존 sanitized evidence 기준 |
| adm-runtime | 완료 | `scripts/smoke-adm-runtime.ps1` | 기존 runtime smoke 기준 |
| adm-permission-runtime | 완료 | `scripts/smoke-adm-permission-runtime.ps1` | 기존 runtime smoke 기준 |
| openapi-runtime | 완료 | `scripts/smoke-openapi.ps1` | 기존 runtime smoke 기준 |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | 이번 세션에서 브라우저 클릭 검증 미실행 |
| standard-header-e2e | 완료 | `scripts/smoke-standard-header-e2e.ps1` | 기존 sanitized evidence 기준 |
| complex-transaction-trace | 완료 | `composite-transaction-runtime-result.sanitized.json` | 기존 sanitized evidence 기준 |
| transaction-segment-log | 완료 | `pfw_transaction_segment` | 기존 runtime smoke 기준 |
| adm-transaction-group-list | 완료 | `/adm/api/transaction-groups` | 기존 runtime smoke 기준 |
| adm-transaction-timeline | 완료 | `/timeline`, `/headers`, `/external-logs` | 기존 runtime smoke 기준 |
| cmn-fixed-length-engine | 완료 | `FixedLengthLayoutRegistry` | 기존 테스트/SQL 기준 |
| composite-runtime-smoke | 완료 | `scripts/smoke-composite-transaction-runtime.ps1` | 기존 runtime smoke 기준 |
| adm-transaction-group-runtime | 완료 | `scripts/smoke-adm-transaction-group-runtime.ps1` | 기존 runtime smoke 기준 |
| redis-kafka-mq-broker | 미검증 | DB fallback | 이번 세션에서 실제 broker 미기동 |
| broker-real-integration | 미검증 | 없음 | Redis/Kafka/MQ 실연동 미검증 |
| file-log-standard | 미검증 | `scripts/smoke-file-log-standard-runtime.ps1` | 로컬 서비스 기동 유지 실패로 runtime grep 미검증 |
| trace-boost-runtime | 미검증 | `scripts/smoke-trace-boost-runtime.ps1` | ADM runtime 필요 |
| bat-trace-boost-runtime | 부분 구현 | `scripts/smoke-bat-trace-boost-runtime.ps1` | 기존 BAT smoke 결과는 확인했으나 batch 파일 로그 증거가 아직 닫히지 않음 |
| create-domain-smoke | 완료 | `scripts/smoke-create-domain.ps1` | dry-run과 후보 파일 생성 통과 |
| runtime-smoke-summary | 실패 | `scripts/sync-runtime-smoke-summary.ps1`, `runtime-smoke-summary.sanitized.json` | summary는 생성됐지만 미검증/부분 구현 항목이 포함되어 exitCode 1 반환 |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | Gradle 품질 게이트 통과 |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 매트릭스와 리포트 check id 상태 일치 확인 |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | 신규 파일 로그, Trace Boost, create-domain evidence marker 확인 |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | UTF-8과 mojibake 검사 통과 |

## 실행 검증 결과

- `.\gradlew.bat :pfw:compileJava :adm:compileJava :exs:compileJava :bat:compileJava --offline --no-daemon --console=plain --rerun-tasks`: 완료
- `.\gradlew.bat test --offline --no-daemon --console=plain`: 완료
- `.\gradlew.bat qualityGate --offline --no-daemon --console=plain`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-utf8.ps1 -CheckMojibake`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-html-docs.ps1`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-feature-evidence.ps1`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-sql-standard.ps1`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-create-domain.ps1`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-file-log-standard-runtime.ps1`: 미검증
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-trace-boost-runtime.ps1`: 미검증
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-trace-boost-runtime.ps1`: 부분 구현
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\sync-runtime-smoke-summary.ps1`: 실패
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\export-sanitized-evidence.ps1 -EvidenceDir specs\evidence\20260703_05`: 완료

## Sanitized Evidence

- `specs/evidence/20260703_05/file-log-standard-result.sanitized.json`
- `specs/evidence/20260703_05/file-log-grep-summary.sanitized.log`
- `specs/evidence/20260703_05/trace-boost-runtime-result.sanitized.json`
- `specs/evidence/20260703_05/bat-trace-boost-runtime-result.sanitized.json`
- `specs/evidence/20260703_05/create-domain-result.sanitized.json`
- `specs/evidence/20260703_05/runtime-smoke-summary.sanitized.json`
- `specs/evidence/20260703_05/evidence-manifest.sanitized.json`

## 현재 리스크

- 신규 파일 로그 writer와 Trace Boost API는 컴파일 검증 대상이지만, 로컬 서비스가 백그라운드에서 안정적으로 유지되지 않아 이번 세션의 transactionGlobalId 기준 파일 로그 grep 증거는 아직 미검증입니다.
- BAT batch 로그도 listener 연결은 반영했지만, 이번 smoke에서는 기존 BAT runtime 결과만 참조했고 `logs/bat/cpf-bat-batch.log` 생성까지 닫히지 않아 부분 구현입니다.
- 파일 로그 rolling, 보존 기간, 디스크 권한 오류 진단, ADM 진단 화면 노출은 다음 단계 고도화 대상입니다.

## 다음 보강 후보

1. 로컬 서비스 기동 방식을 안정화한 뒤 `smoke-file-log-standard-runtime.ps1`, `smoke-trace-boost-runtime.ps1`, `smoke-bat-trace-boost-runtime.ps1`를 runtime 증거로 닫기.
2. 파일 로그 rolling appender, total size cap, 보존 정책, 권한 오류 진단을 ADM observability에 연결하기.
3. Trace Boost를 온라인 거래뿐 아니라 BAT job/step 조건과 ADM 화면 조작까지 확장하기.
4. create-domain 후보 산출물을 실제 신규 모듈 적용 절차와 Gradle task로 확장하기.
5. Redis/Kafka/MQ 실 broker 우선 전파와 DB fallback 장애 시나리오를 runtime smoke로 검증하기.
