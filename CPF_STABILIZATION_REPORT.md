# CPF 안정화 작업 리포트

작성일: 2026-07-07

## 수행 작업

- `CPF_FINAL_TARGET_REQUIREMENTS.md` 상단에 요청서 작성용 목표 인덱스를 추가했습니다. 기존 상세 본문은 삭제, 축약, 덮어쓰기 하지 않았습니다.
- PFW 서비스 호출 엔진 1차 축을 추가했습니다. 레지스트리 조회, endpoint/instance 선택, routing policy 해석, health-aware instance 선택, 호출 이력 기록, Remote Facade Proxy 지원 클래스를 포함합니다.
- ADM 서비스 레지스트리 조회 API를 추가했습니다. 서비스, endpoint, instance, health, routing policy, circuit state, call history를 조회합니다.
- `pfw_service*` 레지스트리 테이블, seed, smoke SQL, Flyway V21 migration, all-install 합본을 동기화했습니다.
- README를 짧은 진입점 형태로 재작성하고 최종 목표, 요청서, 리포트, gap matrix, evidence index, 기능 구현 매트릭스 링크를 정리했습니다.
- 새 smoke/evidence를 추가했습니다. PFW 서비스 레지스트리 소스/SQL 계약은 실행했고, ADM 실제 API 호출은 `-RunRuntime` 미지정으로 미검증 처리했습니다.

## 검증 상태

| check id | 상태 | 증적 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 완료 | `specs/evidence/20260707_01/edu-mapper-db-slice.log` | 이전 실DB mapper slice 검증 증적 |
| mariadb-full-install | 완료 | `specs/evidence/20260707_01/mariadb-full-install-result.sanitized.json` | 이전 MariaDB full install 검증 증적 |
| adm-runtime | 완료 | `specs/evidence/20260707_01/adm-runtime-smoke-result.sanitized.json` | 이전 ADM runtime smoke 증적 |
| adm-permission-runtime | 완료 | `specs/evidence/20260707_01/adm-permission-runtime-result.sanitized.json` | 이전 ADM 권한 runtime smoke 증적 |
| openapi-runtime | 완료 | `specs/evidence/20260707_01/openapi-runtime-result.sanitized.json` | 이전 OpenAPI runtime smoke 증적 |
| adm-browser-click | 미검증 | `specs/evidence/20260707_01/adm-ui-browser-smoke-result.sanitized.json` | 브라우저 클릭 실검증은 이전 증적에서도 SKIPPED |
| standard-header-e2e | 완료 | `specs/evidence/20260707_01/standard-header-e2e-result.sanitized.json` | 이전 표준 헤더 E2E 증적 |
| complex-transaction-trace | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | 이전 복합 거래 trace runtime 증적 |
| transaction-segment-log | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json` | 이전 segment log runtime 증적 |
| adm-transaction-group-list | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | 이전 ADM 운영 콘솔 smoke 증적 |
| adm-transaction-timeline | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json` | 이전 ADM 거래 그룹 timeline 증적 |
| cmn-fixed-length-engine | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | 이전 CMN fixed-length 검증 증적 |
| composite-runtime-smoke | 완료 | `specs/evidence/20260707_01/composite-transaction-runtime-result.sanitized.json`, `specs/evidence/20260707_01/composite-transaction-failure-runtime-result.sanitized.json` | 성공/실패 복합 거래 smoke 증적 |
| adm-transaction-group-runtime | 완료 | `specs/evidence/20260707_01/adm-transaction-group-runtime-result.sanitized.json`, `specs/evidence/20260707_01/adm-transaction-group-failure-runtime-result.sanitized.json` | 성공/실패 ADM 거래 그룹 smoke 증적 |
| redis-kafka-mq-broker | 미검증 | 없음 | 실제 broker 검증은 수행하지 않음 |
| broker-real-integration | 미검증 | 없음 | broker 장애/fallback/DLQ 검증은 수행하지 않음 |
| file-log-standard | 완료 | `specs/evidence/20260707_01/file-log-standard-result.sanitized.json` | 이전 파일 로그 smoke 증적 |
| trace-boost-runtime | 완료 | `specs/evidence/20260707_01/trace-boost-runtime-result.sanitized.json` | 이전 Trace Boost runtime 증적 |
| bat-trace-boost-runtime | 완료 | `specs/evidence/20260707_01/bat-trace-boost-runtime-result.sanitized.json` | 이전 BAT Trace Boost runtime 증적 |
| runtime-start-services | 완료 | `specs/evidence/20260707_01/runtime-start-services-result.sanitized.json` | 이전 runtime start 증적 |
| packaged-runtime-resources | 완료 | `specs/evidence/20260707_01/packaged-runtime-resource-check.sanitized.json` | 이전 bootJar resource 검증 증적 |
| runtime-status-diagnostics | 완료 | `specs/evidence/20260707_01/runtime-status-result.sanitized.json`, `specs/evidence/20260707_01/runtime-diagnostics-result.sanitized.json` | 이전 runtime status/diagnostics 증적 |
| runtime-closure | 완료 | `specs/evidence/20260707_01/runtime-closure-result.sanitized.json` | 이전 runtime closure 증적 |
| adm-operation-console-runtime | 완료 | `specs/evidence/20260707_01/adm-operation-console-runtime-result.sanitized.json` | 이전 ADM 운영 콘솔 smoke 증적 |
| adm-log-policy-ui-static | 완료 | `specs/evidence/20260707_01/adm-log-policy-ui-static-result.sanitized.json` | 이전 ADM log policy UI marker 증적 |
| bat-log-bean-runtime | 완료 | `specs/evidence/20260707_01/bat-log-bean-runtime-result.sanitized.json` | 이전 BAT logging bean runtime 증적 |
| exs-timeout-retry-runtime | 완료 | `specs/evidence/20260707_01/exs-timeout-retry-runtime-result.sanitized.json` | 이전 EXS timeout/retry 증적 |
| cmn-fixed-length-advanced | 완료 | `specs/evidence/20260707_01/cmn-fixed-length-advanced-result.sanitized.json` | 이전 CMN advanced fixed-length 증적 |
| create-domain-smoke | 완료 | `specs/evidence/20260707_01/create-domain-result.sanitized.json` | 이전 create-domain smoke 증적 |
| pfw-service-call-engine | 부분 구현 | `specs/evidence/20260707_02/service-registry-runtime-result.sanitized.json`, `specs/evidence/20260707_02/pfw-adm-test.log` | 소스/SQL 계약과 PFW/ADM 테스트 통과. 실제 원격 HTTP 호출/서킷 runtime은 후속 |
| adm-service-registry-runtime | 미검증 | `specs/evidence/20260707_02/adm-service-registry-runtime-result.sanitized.json` | `-RunRuntime` 미지정으로 실제 ADM API 호출은 수행하지 않음 |
| runtime-smoke-summary | 완료 | `specs/evidence/20260707_01/runtime-smoke-summary.sanitized.json` | 이전 runtime smoke summary 증적 |
| check-report-matrix-evidence-consistency | 완료 | `specs/evidence/20260707_02/quality-gate.log` | qualityGate 안에서 report/matrix/evidence 상태값 정합성 실행 |
| quality-gate | 완료 | `specs/evidence/20260707_02/quality-gate.log` | 이번 작업 최종 단계에서 실행 |
| check-html-docs | 완료 | `specs/evidence/20260707_02/check-html-docs.log` | 이번 작업 최종 단계에서 실행 |
| check-feature-evidence | 완료 | `specs/evidence/20260707_02/check-feature-evidence.log` | 이번 작업 최종 단계에서 실행 |
| check-utf8 | 완료 | `specs/evidence/20260707_02/check-utf8-mojibake.log` | 이번 작업 최종 단계에서 실행 |

## 직접 실행한 검증

- `.\gradlew.bat :pfw:test :adm:test --offline --no-daemon --console=plain`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\build-all-install-sql.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-service-registry-runtime.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-service-registry-runtime.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\export-sanitized-evidence.ps1 -EvidenceDir specs\evidence\20260707_02 -AllowMissing`
- `.\gradlew.bat :mbr:test --offline --no-daemon --console=plain --tests cpf.mbr.bse.controller.MbrControllerValidationTest`
- `.\gradlew.bat qualityGate --offline --no-daemon --console=plain`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-report-matrix-evidence-consistency.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-html-docs.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-feature-evidence.ps1`
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-utf8.ps1 -CheckMojibake`

## 기존 증적 연결 marker

- 기존 MariaDB full install 검증 명령: `scripts/smoke-mariadb-full-install.ps1`
- 기존 표준 헤더 E2E 검증 명령: `scripts/smoke-standard-header-e2e.ps1`
- EDU canonical fixture: `xyz_edu_query_fixture.sql`
- EDU mapper DB 환경변수: `CPF_XYZ_EDU_MAPPER_DB_USERNAME`
- EDU mapper legacy alias 환경변수: `CPF_XYZ_EDU_MAPPER_DB_USER`

## 미검증/남은 리스크

- ADM 서비스 레지스트리 실제 API 호출은 `scripts/smoke-adm-service-registry-runtime.ps1 -RunRuntime`로 별도 실행해야 합니다.
- 서비스 호출 엔진은 레지스트리와 호출 이력 중심의 1차 구현입니다. 실제 HTTP adapter 통합, retry/circuit half-open 전이, health scheduler는 후속 작업입니다.
- Redis/Kafka/MQ 실제 broker 연동은 이번 작업에서 실행하지 않았습니다.
- 브라우저 클릭 검증은 이번 작업에서 실행하지 않았습니다.
- `CPF_NEW_REQUEST.md`는 요청 확인 용도로만 사용했고 작업 대상에서 제외했습니다.

## 다음 보강 후보

1. `CpfWebClient`와 `CpfServiceCallEngine`을 실제 HTTP 호출 경로로 통합하고 timeout/retry/circuit 상태 전이를 runtime smoke로 검증합니다.
2. ADM 서비스 레지스트리 화면을 추가하고 서비스, endpoint, instance, health, circuit, call history를 운영자 UX로 조회합니다.
3. 서비스 health scheduler와 instance heartbeat 갱신 job을 추가합니다.
4. broker real integration과 browser click 검증을 독립 증적으로 추가합니다.

## 완료 확인 문장

이번 작업에서 실제 실행한 검증과 실행하지 않은 검증을 분리했습니다.
실행하지 않은 검증은 완료로 기록하지 않았습니다.
report/matrix/evidence 상태값 정합성을 실제 파일 기준으로 확인했습니다.
CPF_FINAL_TARGET_REQUIREMENTS.md 상세 본문은 삭제/축약/덮어쓰기 하지 않았습니다.
Git commit/push/branch 생성은 수행하지 않았습니다.
