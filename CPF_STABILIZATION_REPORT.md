# CPF 안정화 작업 리포트

## 1. 작업 요약

이번 작업은 `CPF_NEW_REQUEST.md`의 Runtime Closure Harness, 파일 로그, Trace Boost, BAT 진단, ADM 운영 콘솔, EXS/CMN 연계 고도화 범위를 기준으로 진행했다. `CPF_FINAL_TARGET_REQUIREMENTS.md`는 최종 목표 기준으로 참고했으며, 요청서와 최종 목표 파일은 작업 대상에서 제외했다.

핵심 산출물은 다음과 같다.

- Runtime 통합 기동, 상태, 진단, 종료, closure smoke 스크립트 추가
- ADM Trace Boost 운영 UI 정적 marker와 API 연결부 보강
- BAT logging diagnostic API와 테스트 보강
- EXS timeout/retry runtime smoke 초안 추가
- CMN fixed-length 고도화 smoke와 직접 Gradle 검증 증적 정리
- sanitized evidence export 보강: 민감정보 마스킹과 깨진 대체문자 제거
- `specs/기능_구현_매트릭스.html` 재작성
- 이 리포트는 Markdown 기준으로 작성했고, `CPF_STABILIZATION_CHANGED_FILES.txt`는 생성하지 않았다.

## 2. 자율 진행 판단

- runtime이 닫혀 있는 경우 단순 재시도만 반복하지 않고, start/status/diagnostics/closure harness와 failure classification evidence를 남기도록 정리했다.
- ACC runtime 실패는 source의 logback 경로는 `logs` 기준으로 고쳤지만, 기존 bootJar에는 오래된 `D:/logs` 설정이 남아 있음을 evidence에 남겼다. 성공으로 표시하지 않았다.
- browser click, real broker, MariaDB full install, 기존 E2E smoke처럼 이번 작업에서 재실행하지 않은 검증은 완료로 표시하지 않았다.
- `CPF_STABILIZATION_REPORT.html`은 유지하지 않고 Markdown 리포트 기준으로 정리했다.

## 3. Runtime Harness 결과

| 항목 | 상태 | 근거 | 비고 |
|---|---|---|---|
| runtime start | 실패 | `specs/evidence/20260706_03/runtime-start-services-result.sanitized.json` | `-BuildBeforeRun` 포함 재시도 결과 ACC는 `D:/logs` 권한 오류가 남았고 전체 start 상태는 실패 |
| runtime stop | 완료 | `specs/evidence/20260706_03/runtime-stop-services-result.sanitized.json` | harness가 기록한 PID만 대상으로 정리 완료 |
| runtime status | 실패 | `specs/evidence/20260706_03/runtime-status-result.sanitized.json` | ACC 8080, MBR 8081, ADM 8090, EXS 8092, BAT 8093이 최종적으로 닫힌 상태 |
| runtime diagnostics | 실패 | `specs/evidence/20260706_03/runtime-diagnostics-result.sanitized.json` | process, port, health, log tail 수집 구조는 동작 |
| runtime closure | 실패 | `specs/evidence/20260706_03/runtime-closure-result.sanitized.json` | 하위 smoke 실패와 미검증을 집계해 실패 처리 |

## 4. 파일 로그 결과

`scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime`는 실행됐고 실패했다. 실패 사유는 ACC runtime port가 열리지 않은 것이다.

source-level로는 파일 로그 writer와 listener hook이 존재하며, ACC `logback-local.xml`, `logback-test.xml`, `logback-dev.xml`의 기본 경로를 `logs` 기준으로 보정했다. 다만 기존 bootJar를 그대로 사용한 runtime evidence에는 오래된 `D:/logs` 경로가 남아 있으므로, 다음 검증은 bootJar rebuild를 포함해야 한다.

## 5. Trace Boost 결과

ADM Trace Boost UI에는 생성, 상태 조회, 이력 조회, 해제 버튼과 입력 항목을 추가했다. `scripts/smoke-adm-log-policy-ui-static.ps1`는 정적 marker 기준으로 통과했다.

runtime 정책 생성과 적용은 ADM port가 닫혀 실패했다. `scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime` 결과는 실패이며, 완료로 표시하지 않는다.

## 6. BAT 결과

`BatHealthController`에 `/bat/api/diagnostics/logging` 진단 API를 추가했다. 해당 API는 `CpfBatchFileLogWriter`, `CpfBatchRuntimeListener` bean 존재 여부와 배치 파일 로그 설정 상태를 확인하기 위한 운영 진단용이다.

`BatHealthControllerTest`는 provider 주입 방식으로 보강했고, `:bat:test`와 전체 `test`에서 통과했다. 다만 BAT runtime port가 닫혀 `scripts/smoke-bat-log-bean-runtime.ps1 -RequireRuntime`와 batch Trace Boost runtime은 실패로 남겼다.

## 7. ADM 운영 콘솔 결과

Trace Boost 운영 UI의 정적 marker smoke는 완료했다. ADM batch relation, 거래 그룹, 외부 로그, 권한별 버튼 상태를 실제 runtime API로 확인하는 범위는 ADM port가 닫혀 미검증이다.

브라우저 click 검증은 이번 작업에서 수행하지 않았다. `scripts/smoke-adm-operation-console-runtime.ps1 -RequireRuntime`는 ADM port 미기동으로 실패했다. 따라서 ADM 운영 UX 완료가 아니라 정적 UI/API 연결 보강 단계로 기록한다.

## 8. EXS/CMN 결과

EXS timeout/retry runtime smoke 스크립트를 추가했고 `-RequireRuntime`로 실행했다. EXS port가 닫혀 runtime 검증은 실패로 기록한다.

CMN fixed-length 고도화는 source marker와 직접 Gradle 테스트로 확인했다. 중첩 PowerShell에서 Gradle wrapper가 sandbox/network 제한에 걸릴 수 있어, `scripts/smoke-cmn-fixed-length-advanced.ps1 -DirectGradleVerified` 방식으로 직접 실행 결과를 evidence에 반영했다.

## 9. 보강/차수 범위 결과

- sanitized evidence export는 `specs/evidence/20260706_03` 기준으로 정리했다.
- 외부 프로세스 로그에서 들어온 `U+FFFD` 대체문자는 sanitized evidence에서 `[encoding-replaced]`로 치환하도록 보강했다.
- Redis/Kafka/MQ real broker, browser click, MariaDB full install, OpenAPI runtime은 이번 작업에서 재실행하지 않았다.

## 10. 검증 상태표

| check id | 상태 | 근거 | 비고 |
|---|---|---|---|
| edu-mapper-db-slice | 미검증 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql` | 이번 작업에서는 DB slice 테스트를 재실행하지 않았다. 환경 변수는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME` 기준이며 기존 호환으로 `CPF_XYZ_EDU_MAPPER_DB_USER`도 유지한다. |
| mariadb-full-install | 미검증 | `scripts/smoke-mariadb-full-install.ps1` | 이번 작업에서는 재실행하지 않았다. |
| adm-runtime | 미검증 | `scripts/smoke-adm-runtime.ps1` | 이번 작업에서는 재실행하지 않았다. |
| adm-permission-runtime | 미검증 | `scripts/smoke-adm-permission-runtime.ps1` | 이번 작업에서는 재실행하지 않았다. |
| openapi-runtime | 미검증 | `scripts/smoke-openapi.ps1` | 이번 작업에서는 재실행하지 않았다. |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | 브라우저 click 검증은 수행하지 않았다. |
| standard-header-e2e | 미검증 | `scripts/smoke-standard-header-e2e.ps1` | 이번 작업에서는 재실행하지 않았다. |
| complex-transaction-trace | 미검증 | `composite-transaction-runtime-result.sanitized.json` | 기존 증적은 있으나 이번 작업에서는 재실행하지 않았다. |
| transaction-segment-log | 미검증 | `pfw_transaction_segment` | 이번 작업에서는 DB 조회 smoke를 재실행하지 않았다. |
| adm-transaction-group-list | 미검증 | `/adm/api/transaction-groups` | 이번 작업에서는 API runtime smoke를 재실행하지 않았다. |
| adm-transaction-timeline | 미검증 | `/timeline`, `/headers`, `/external-logs` | 이번 작업에서는 API runtime smoke를 재실행하지 않았다. |
| cmn-fixed-length-engine | 완료 | `FixedLengthMessageParserFormatterTest` | 직접 Gradle 테스트와 고도화 smoke evidence로 확인했다. |
| composite-runtime-smoke | 미검증 | `scripts/smoke-composite-transaction-runtime.ps1` | 이번 작업에서는 재실행하지 않았다. |
| adm-transaction-group-runtime | 미검증 | `scripts/smoke-adm-transaction-group-runtime.ps1` | 이번 작업에서는 재실행하지 않았다. |
| redis-kafka-mq-broker | 미검증 | DB fallback | real broker는 기동하지 않았다. |
| broker-real-integration | 미검증 | 없음 | Redis, Kafka, MQ 실연동은 별도 환경 검증 대상이다. |
| file-log-standard | 실패 | `scripts/smoke-file-log-standard-runtime.ps1`, `file-log-standard-result.sanitized.json` | ACC runtime port 미기동으로 실패했다. |
| trace-boost-runtime | 실패 | `scripts/smoke-trace-boost-runtime.ps1`, `trace-boost-runtime-result.sanitized.json` | ADM runtime port 미기동으로 실패했다. |
| bat-trace-boost-runtime | 실패 | `scripts/smoke-bat-trace-boost-runtime.ps1`, `bat-trace-boost-runtime-result.sanitized.json` | BAT batch 파일 로그 runtime 증거가 없어 실패했다. |
| runtime-start-services | 실패 | `runtime-start-services-result.sanitized.json`, `runtime-stop-services-result.sanitized.json` | `-BuildBeforeRun` 포함 재시도 결과 ACC는 `D:/logs` 권한 오류가 남았고 전체 start는 실패했다. stop은 기록 PID 기준으로 완료했다. |
| runtime-status-diagnostics | 실패 | `runtime-status-result.sanitized.json`, `runtime-diagnostics-result.sanitized.json` | status와 diagnostics는 생성됐지만 대상 port는 닫혀 있었다. |
| runtime-closure | 실패 | `runtime-closure-result.sanitized.json` | 하위 runtime smoke 실패와 미검증을 집계했다. |
| adm-operation-console-runtime | 실패 | `adm-operation-console-runtime-result.sanitized.json` | `-RequireRuntime` 실행 결과 ADM port가 닫혀 실패했다. |
| adm-log-policy-ui-static | 완료 | `adm-log-policy-ui-static-result.sanitized.json` | 정적 UI marker와 JS 연결을 확인했다. |
| bat-log-bean-runtime | 실패 | `bat-log-bean-runtime-result.sanitized.json` | `-RequireRuntime` 실행 결과 BAT port가 닫혀 실패했다. |
| exs-timeout-retry-runtime | 실패 | `exs-timeout-retry-runtime-result.sanitized.json` | `-RequireRuntime` 실행 결과 EXS port가 닫혀 실패했다. |
| cmn-fixed-length-advanced | 완료 | `cmn-fixed-length-advanced-result.sanitized.json` | source marker와 직접 Gradle 테스트를 확인했다. |
| create-domain-smoke | 미검증 | `scripts/smoke-create-domain.ps1` | 이번 작업에서는 재실행하지 않았다. |
| runtime-smoke-summary | 실패 | `scripts/sync-runtime-smoke-summary.ps1`, `runtime-smoke-summary.sanitized.json` | file log, Trace Boost, BAT trace boost 실패를 집계해 exitCode 1을 반환했다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | Gradle qualityGate가 통과했다. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 매트릭스와 리포트 check id 상태 일치를 확인했다. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | 신규 runtime harness와 sanitized evidence marker를 확인했다. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | UTF-8과 mojibake 검사가 통과했다. |

## 11. 실행한 검증

| 명령 | 결과 | 메모 |
|---|---|---|
| `.\gradlew.bat :pfw:compileJava :adm:compileJava :exs:compileJava :bat:compileJava --offline --no-daemon --console=plain --rerun-tasks` | 완료 | compile 통과 |
| `.\gradlew.bat test --offline --no-daemon --console=plain` | 완료 | 전체 test 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1` | 완료 | SQL standard check 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake` | 실패 후 수정 | sanitized evidence의 대체문자를 exporter에서 치환하도록 보강했다. 재실행 결과는 아래 최종 검증에 반영한다. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT -BuildBeforeRun -StartupTimeoutSeconds 45 -NoExitOnFailure` | 실패 | 최신 runtime start evidence 생성 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-status.ps1 -Modules ACC,MBR,EXS,ADM,BAT -NoExitOnFailure` | 실패 | 최신 runtime status evidence 생성 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-diagnostics.ps1 -Modules ACC,MBR,EXS,ADM,BAT -NoExitOnFailure` | 실패 | 최신 runtime diagnostics evidence 생성 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-stop-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT` | 완료 | harness 기록 PID cleanup evidence 생성 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/sync-runtime-smoke-summary.ps1` | 실패 | 실패 smoke 집계로 exitCode 1 반환 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1` | 완료 | HTML 문서와 report 상태 정합성 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1` | 완료 | feature evidence marker 통과 |
| `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 완료 | Gradle qualityGate 통과 |

## 12. 실패 항목

- runtime start/status/diagnostics/closure: port 미기동과 ACC log path 문제로 실패
- file-log-standard runtime: ACC port 미기동으로 실패
- trace-boost-runtime: ADM port 미기동으로 실패
- bat-log-bean-runtime: BAT port 미기동으로 실패
- bat-trace-boost-runtime: BAT batch 파일 로그 runtime 증거 부족으로 실패
- adm-operation-console-runtime: ADM port 미기동으로 실패
- exs-timeout-retry-runtime: EXS port 미기동으로 실패

## 13. Closed

- runtime harness 스크립트 골격
- runtime failure evidence 생성
- BAT logging diagnostic API와 단위 테스트
- ADM Trace Boost UI 정적 marker
- CMN fixed-length 고도화 source marker와 직접 테스트
- sanitized evidence export 보강
- 기능 매트릭스 재작성

## 14. Closed with Limitation

- ACC logback path는 source에서 상대경로로 고쳤지만, 기존 bootJar로 실행한 runtime evidence에는 오래된 `D:/logs` 설정이 남아 있다.
- runtime smoke는 실패 원인을 분류하는 수준까지 닫았고, 정상 거래 grep 완료 증적은 다음 runtime 기동 검증에서 확보해야 한다.

## 15. Still Open

- bootJar rebuild 포함 runtime closure 재검증
- ACC, MBR, ADM, EXS, BAT port 안정 기동
- 파일 로그 grep 증적 확보
- Trace Boost runtime 생성, 적용, 이력, 해제 검증
- BAT batch log runtime 생성 검증
- ADM browser click 검증
- Redis, Kafka, MQ real broker 검증

## 16. Blocked

현재 영구 차단 항목은 없다. 다만 runtime 완료 판단은 bootJar rebuild와 서비스 port 안정 기동 후에만 가능하다.

## 17. Do Not Rework Without Failure Evidence

- 기존 증적만 보고 runtime 완료로 올리지 않는다.
- source marker만으로 browser click, real broker, runtime E2E 완료를 표시하지 않는다.
- runtime port 미기동 상태에서 file log, Trace Boost, BAT batch log 완료로 표시하지 않는다.
