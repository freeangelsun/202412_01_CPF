# CPF Stabilization Report

## 기준 정보

- 요청 기준: `CPF_NEW_REQUEST.md`의 PFW Service Call Engine Runtime, Service Registry, ADM 관제, Evidence Gate 보강 요청
- 최종 목표 참조: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 요청 제외: `CPF_NEW_REQUEST.md` 직접 수정, commit/push/branch 생성, PDF 생성, 별도 변경파일 목록 생성
- 증적 디렉터리: `specs/evidence/20260707_03`

## 수행 작업

- PFW Service Call Engine을 target-aware runtime 경로로 보강했습니다.
- `CpfWebClient`가 registry 기반 engine 호출을 우선 사용하고, registry 미가용 시 설정 endpoint fallback을 제한적으로 사용하도록 정리했습니다.
- retry, failover, selected instance 전달, call history 기록, health 상태 기록, circuit OPEN 차단, HALF_OPEN 전이, CLOSED 복구 경로를 보강했습니다.
- ACC/MBR의 기존 `cpfWebClient.service(...)` 직접 사용을 service-call engine 경유 API로 변경했습니다.
- Remote Facade Proxy support에 target-aware 호출 메서드를 추가했습니다.
- ADM Service Registry 정적 UI 패널과 초기 로딩/수동 조회 메서드를 추가했습니다.
- service-call boundary scan을 추가하고 `qualityGate`에 연결했습니다.
- service-call runtime/health/circuit/failover 및 ADM service-registry UI static smoke 스크립트를 추가했습니다.
- README의 smoke 명령 목록에 신규 service-call/service-registry 검증 명령을 추가했습니다.

## 검증 결과

| check id | 상태 | 증적 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | 이전 검증 증적 유지 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_01/mariadb-full-install-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-runtime | 완료 | `specs/evidence/20260707_01/adm-runtime-smoke-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_01/adm-permission-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| openapi-runtime | 완료 | `specs/evidence/20260707_01/openapi-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-browser-click | 미검증 | `specs/evidence/20260707_01/adm-ui-browser-smoke-result.sanitized.json` | 브라우저 클릭 실검증 미수행 |
| standard-header-e2e | 완료 | `specs/evidence/20260707_01/standard-header-e2e-result.sanitized.json` | 이전 검증 증적 유지 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | 이전 검증 증적 유지 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260707_01/composite-transaction-failure-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json`, `specs/evidence/20260707_01/adm-transaction-group-failure-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| redis-kafka-mq-broker | 미검증 | 없음 | 실제 broker 실검증 미수행 |
| broker-real-integration | 미검증 | 없음 | 실제 broker 실검증 미수행 |
| file-log-standard | 완료 | `specs/evidence/20260707_01/file-log-standard-result.sanitized.json` | 이전 검증 증적 유지 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_01/trace-boost-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_01/bat-trace-boost-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| runtime-start-services | 완료 | `specs/evidence/20260707_01/runtime-start-services-result.sanitized.json` | 이전 검증 증적 유지 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_01/packaged-runtime-resource-check.sanitized.json` | 이전 검증 증적 유지 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_01/runtime-status-result.sanitized.json`, `specs/evidence/20260707_01/runtime-diagnostics-result.sanitized.json` | 이전 검증 증적 유지 |
| runtime-closure | 완료 | `specs/evidence/20260707_01/runtime-closure-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_01/adm-log-policy-ui-static-result.sanitized.json` | 이전 검증 증적 유지 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_01/bat-log-bean-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260707_01/exs-timeout-retry-runtime-result.sanitized.json` | 이전 검증 증적 유지 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | 이전 검증 증적 유지 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_01/create-domain-result.sanitized.json` | 이전 검증 증적 유지 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260707_03/service-registry-runtime-result.sanitized.json`, `specs/evidence/20260707_03/service-call-engine-runtime-success.sanitized.json`, `specs/evidence/20260707_03/service-registry-health-runtime.sanitized.json`, `specs/evidence/20260707_03/service-call-engine-circuit-transition.sanitized.json`, `specs/evidence/20260707_03/service-call-engine-failover.sanitized.json`, `specs/evidence/20260707_03/pfw-service-call-engine-test.log`, `specs/evidence/20260707_03/forbidden-direct-call-scan.log` | source/unit smoke 통과. 실제 다중 서비스 HTTP runtime 미검증 |
| adm-service-registry-runtime | 미검증 | `specs/evidence/20260707_03/adm-service-registry-api-test.log`, `specs/evidence/20260707_03/adm-service-registry-ui-static-smoke.sanitized.json`, `specs/evidence/20260707_03/adm-service-registry-runtime-result.sanitized.json` | API 단위/정적 UI 통과. 실제 ADM 서버 API 호출 미수행 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_01/runtime-smoke-summary.sanitized.json` | 이전 검증 증적 유지 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260707_03/quality-gate.log` | 최종 qualityGate에서 확인 |
| quality-gate | 완료 | `specs/evidence/20260707_03/quality-gate.log` | 최종 qualityGate에서 확인 |
| check-html-docs | 완료 | `specs/evidence/20260707_03/check-html-docs.log` | 개별 실행 후 qualityGate 포함 |
| check-feature-evidence | 완료 | `specs/evidence/20260707_03/check-feature-evidence.log` | 개별 실행 후 qualityGate 포함 |
| check-utf8 | 완료 | `specs/evidence/20260707_03/check-utf8-mojibake.log` | 개별 실행 후 qualityGate 포함 |

## 직접 실행한 명령

```powershell
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain --tests cpf.pfw.common.servicecall.*
.\gradlew.bat :adm:test --offline --no-daemon --console=plain --tests cpf.adm.opr.controller.AdmServiceRegistryControllerTest
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-runtime.ps1 -ResultDir specs/evidence/20260707_03
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-registry-health-runtime.ps1 -ResultDir specs/evidence/20260707_03
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-circuit-runtime.ps1 -ResultDir specs/evidence/20260707_03
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-service-call-engine-failover-runtime.ps1 -ResultDir specs/evidence/20260707_03
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-service-registry-ui-static.ps1 -ResultDir specs/evidence/20260707_03
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-service-call-boundary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

## 고정 Gate 참고 기준

- EDU DB slice 표준 fixture: `xyz_edu_query_fixture.sql`
- EDU DB slice 환경변수 호환 키: `CPF_XYZ_EDU_MAPPER_DB_USER`
- EDU DB slice 환경변수 표준 키: `CPF_XYZ_EDU_MAPPER_DB_USERNAME`
- MariaDB 전체 설치 smoke 기준 스크립트: `scripts/smoke-mariadb-full-install.ps1`
- 표준 헤더 E2E smoke 기준 스크립트: `scripts/smoke-standard-header-e2e.ps1`

## 남은 리스크

- 실제 ACC/MBR/EXS/ADM 다중 서비스 기동 후 원격 HTTP runtime 호출은 아직 실행하지 않았습니다.
- ADM Service Registry 실제 서버 API 호출은 `RunRuntime` 없이 미검증 JSON으로 남겼습니다.
- 브라우저 클릭 기반 ADM 화면 검증은 수행하지 않았습니다.
- SQL 전체 MariaDB 재설치는 이번 작업에서 다시 수행하지 않았습니다.
- PowerShell 콘솔에서는 일부 UTF-8 문서/SQL 한글이 깨져 보일 수 있어, 검증은 UTF-8 파일 기준 스크립트로 확인해야 합니다.

## 다음 보강 후보

1. `runtime-start-services.ps1`로 ACC/MBR/EXS/ADM을 동시 기동한 뒤 service-call engine의 실제 HTTP 성공/실패/timeout/retry/failover/circuit/call-history를 검증합니다.
2. ADM Service Registry를 브라우저로 클릭 검증하고, 서비스별 health/circuit/call-history UX를 운영자 기준으로 다듬습니다.
3. `00_all_install_and_smoke.sql`을 MariaDB에 재실행해 service registry 테이블, seed idempotent, FK/index를 다시 확인합니다.
4. SQL/HTML/Java에 남은 과거 mojibake 표시를 전수 정리합니다.
