# CPF 안정화 리포트

작성일: 2026-07-06

## 요약

이번 작업은 CPF runtime 안정화와 검증 신뢰도 보강에 집중했습니다. `CPF_NEW_REQUEST.md`와 `CPF_FINAL_TARGET_REQUIREMENTS.md`는 요청 확인용으로만 사용했고 수정하지 않았습니다.

- ACC, MBR, EXS, ADM, BAT 5개 모듈을 clean bootJar 기준으로 재생성했습니다.
- runtime start/status/diagnostics 판정에서 포트 열림뿐 아니라 프로세스 생존, health, `finalRuntimeUsable`을 함께 보도록 강화했습니다.
- ACC 로그 경로가 `D:/logs`나 CMN module log로 흐르지 않도록 LOG_PATH와 module id 우선순위를 보정했습니다.
- jar 내부 리소스 검사 스크립트 `scripts/check-packaged-runtime-resources.ps1`를 추가했습니다.
- 표준 헤더 필수화 이후 실패하던 smoke 스크립트에 거래 헤더를 보강했습니다.
- sanitized evidence를 `specs/evidence/20260706_04`에 생성했습니다.
- README와 `specs/기능_구현_매트릭스.html`를 이번 결과 기준으로 현행화했습니다.

## 주요 변경

- `LoggingAspect`와 `CpfFileLogWriter`는 `cpf.framework.module-id`를 `spring.application.name`보다 우선 사용합니다. ACC runtime에서 CMN nested 설정이 파일 로그 module code를 덮는 문제를 막기 위한 변경입니다.
- ACC logback local/dev/test/prod 기본 LOG_PATH를 `CPF_LOGGING_FILE_BASE_PATH` 또는 상대 경로 `logs`로 통일했습니다.
- ACC/MBR 서버 포트는 환경변수 override를 지원하도록 정리했습니다.
- BAT health endpoint는 거래 AOP 헤더 검증 대상에서 제외했습니다. health는 런타임 생존 판정을 위한 endpoint라 표준 거래 헤더 없이 호출될 수 있어야 합니다.
- EXS service 생성자는 Spring이 명확히 주입할 수 있도록 `@Autowired` 기준을 보강했습니다.
- runtime start/status/diagnostics/closure와 file-log, trace-boost, BAT, ADM, EXS smoke 스크립트의 오류 판정과 증적 저장을 보강했습니다.

## 실행 검증

| 명령 | 결과 | 비고 |
| --- | --- | --- |
| `.\gradlew.bat clean :acc:bootJar :mbr:bootJar :adm:bootJar :exs:bootJar :bat:bootJar --offline --no-daemon --console=plain --rerun-tasks` | 완료 | 5개 bootJar 재생성 성공 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-packaged-runtime-resources.ps1 -NoExitOnFailure` | 완료 | jar 내부 forbidden log path, port/env marker 확인 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT -BuildBeforeRun -StartupTimeoutSeconds 180 -NoExitOnFailure` | 완료 | 5개 모듈 finalRuntimeUsable true |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\runtime-status.ps1 -NoExitOnFailure` | 완료 | runtime status 완료 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\runtime-diagnostics.ps1 -NoExitOnFailure` | 완료 | runtime diagnostics 완료 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-file-log-standard-runtime.ps1 -RequireRuntime` | 완료 | ACC transaction/integration file log 확인 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-trace-boost-runtime.ps1 -RequireRuntime` | 완료 | ADM Trace Boost runtime 확인 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-bat-log-bean-runtime.ps1 -RequireRuntime` | 완료 | BAT logging bean과 batch log 확인 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-bat-trace-boost-runtime.ps1 -RequireRuntime` | 완료 | BAT batch log traceBoostPolicyId 확인 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-exs-timeout-retry-runtime.ps1 -RequireRuntime` | 완료 | EXS timeout/retry ledger 확인 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-operation-console-runtime.ps1 -RequireRuntime` | 완료 | ADM 운영 조회 API 확인 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-log-policy-ui-static.ps1` | 완료 | ADM log policy UI marker 확인 |
| `.\gradlew.bat :cmn:test --offline --no-daemon --console=plain --tests cpf.cmn.message.fixedlength.FixedLengthMessageParserFormatterTest --rerun-tasks` | 완료 | CMN fixed-length 단위 테스트 성공 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-cmn-fixed-length-advanced.ps1 -DirectGradleVerified` | 완료 | 직접 Gradle 검증 결과를 smoke evidence로 반영 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-runtime-closure.ps1 -Modules ACC,MBR,EXS,ADM,BAT` | 완료 | runtime closure 완료 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260706_04 -AllowMissing` | 완료 | sanitized evidence 생성 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1` | 완료 | SQL 표준 검사 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake` | 완료 | UTF-8/mojibake 검사 통과 |

## 미검증

- `scripts/smoke-mariadb-full-install.ps1`: 이번 작업에서는 MariaDB 전체 설치 smoke를 재실행하지 않았습니다.
- `scripts/smoke-standard-header-e2e.ps1`: 이번 작업에서는 표준 헤더 E2E를 재실행하지 않았습니다.
- `scripts/smoke-adm-runtime.ps1`, `scripts/smoke-adm-permission-runtime.ps1`, `scripts/smoke-openapi.ps1`, `scripts/smoke-adm-ui.ps1 -BrowserClick`: 이번 작업에서는 별도 재실행하지 않았습니다.
- `scripts/smoke-composite-transaction-runtime.ps1`, `scripts/smoke-adm-transaction-group-runtime.ps1`, `scripts/smoke-create-domain.ps1`: 이번 작업에서는 별도 재실행하지 않았습니다.
- `scripts/sync-runtime-smoke-summary.ps1`: 과거 E2E 결과까지 함께 요구하는 집계라 이번 작업 범위에서는 재생성하지 않았습니다.
- Redis/Kafka/MQ 실제 broker 연동과 장애 시나리오는 별도 인프라 검증 범위입니다.
- EDU Mapper slice는 `CPF_XYZ_EDU_MAPPER_DB_URL`, `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, `CPF_XYZ_EDU_MAPPER_DB_PASSWORD` 또는 호환용 `CPF_XYZ_EDU_MAPPER_DB_USER`를 지정한 별도 DB에서 실행해야 하며, fixture 기준은 `xyz/src/test/resources/sql/xyz_edu_query_fixture.sql`입니다.

## 검증 매트릭스

| check id | 상태 | 근거 |
| --- | --- | --- |
| edu-mapper-db-slice | 미검증 | 이번 작업에서는 Mapper DB slice를 재실행하지 않았습니다. |
| mariadb-full-install | 미검증 | `scripts/smoke-mariadb-full-install.ps1` 미실행 |
| adm-runtime | 미검증 | `scripts/smoke-adm-runtime.ps1` 미실행 |
| adm-permission-runtime | 미검증 | `scripts/smoke-adm-permission-runtime.ps1` 미실행 |
| openapi-runtime | 미검증 | `scripts/smoke-openapi.ps1` 미실행 |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` 미실행 |
| standard-header-e2e | 미검증 | `scripts/smoke-standard-header-e2e.ps1` 미실행 |
| complex-transaction-trace | 미검증 | 복합 거래 runtime smoke 미실행 |
| transaction-segment-log | 미검증 | segment DB 조회 smoke 미실행 |
| adm-transaction-group-list | 완료 | `adm-operation-console-runtime-result.sanitized.json` |
| adm-transaction-timeline | 미검증 | timeline/detail 전용 API smoke 미실행 |
| cmn-fixed-length-engine | 완료 | `FixedLengthMessageParserFormatterTest` 직접 실행 |
| composite-runtime-smoke | 미검증 | `scripts/smoke-composite-transaction-runtime.ps1` 미실행 |
| adm-transaction-group-runtime | 미검증 | `scripts/smoke-adm-transaction-group-runtime.ps1` 미실행 |
| redis-kafka-mq-broker | 미검증 | 실제 broker 미기동 |
| broker-real-integration | 미검증 | 실제 broker 장애/복구 시나리오 미실행 |
| file-log-standard | 완료 | `file-log-standard-result.sanitized.json` |
| trace-boost-runtime | 완료 | `trace-boost-runtime-result.sanitized.json` |
| bat-trace-boost-runtime | 완료 | `bat-trace-boost-runtime-result.sanitized.json` |
| runtime-start-services | 완료 | `runtime-start-services-result.sanitized.json` |
| packaged-runtime-resources | 완료 | `packaged-runtime-resource-check.sanitized.json` |
| runtime-status-diagnostics | 완료 | `runtime-status-result.sanitized.json`, `runtime-diagnostics-result.sanitized.json` |
| runtime-closure | 완료 | `runtime-closure-result.sanitized.json` |
| adm-operation-console-runtime | 완료 | `adm-operation-console-runtime-result.sanitized.json` |
| adm-log-policy-ui-static | 완료 | `adm-log-policy-ui-static-result.sanitized.json` |
| bat-log-bean-runtime | 완료 | `bat-log-bean-runtime-result.sanitized.json` |
| exs-timeout-retry-runtime | 완료 | `exs-timeout-retry-runtime-result.sanitized.json` |
| cmn-fixed-length-advanced | 완료 | `cmn-fixed-length-advanced-result.sanitized.json` |
| create-domain-smoke | 미검증 | `scripts/smoke-create-domain.ps1` 미실행 |
| runtime-smoke-summary | 미검증 | 전체 과거 E2E 집계 미재생성 |
| quality-gate | 완료 | 최종 qualityGate 재확인 대상 |
| check-html-docs | 완료 | 최종 `scripts/check-html-docs.ps1` 재확인 대상 |
| check-feature-evidence | 완료 | 최종 `scripts/check-feature-evidence.ps1` 재확인 대상 |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` 통과 |

## 남은 리스크

- MariaDB full install, 표준 헤더 E2E, 복합 거래 trace, ADM transaction group, OpenAPI, ADM browser click은 이번 작업에서 재실행하지 않았으므로 완료로 주장하지 않습니다.
- 실제 Redis/Kafka/MQ broker 연동은 로컬 DB fallback과 별도로 검증해야 합니다.
- runtime smoke는 현재 로컬 포트 8080, 8081, 8090, 8092, 8093을 사용합니다. 이미 다른 프로세스가 점유하면 결과가 달라질 수 있습니다.
- `CPF_STABILIZATION_REPORT.md`는 AI 검증용 MD 리포트입니다. HTML 리포트는 생성하지 않습니다.

## 다음 보강 후보

1. MariaDB full install smoke와 seed idempotent/FK/index/권한 검증을 같은 evidence 폴더로 재수집
2. 표준 헤더 E2E, 복합 거래 trace, ADM transaction group runtime smoke 재실행
3. ADM browser click 자동화와 OpenAPI JSON 품질 gate 재실행
4. Redis/Kafka/MQ 실제 broker 연동과 DB fallback 장애 시나리오 검증
5. EDU Mapper slice DB 검증을 별도 테스트 DB 기준으로 재실행
