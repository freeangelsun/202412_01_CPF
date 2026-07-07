# Codex 요청서 02 — 목표파일 요청 인덱스 보강, Evidence/Matrix 정합성 복구, PFW Service Call Engine 대형 착수

## 0. 상위 기준

이번 작업은 repo root의 아래 두 파일을 상위 기준으로 수행한다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
```

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 이미 대용량 최종 목표파일로 master에 반영된 상태로 본다.
이번 작업에서 `CPF_FINAL_TARGET_REQUIREMENTS.md`의 상세 본문을 삭제, 축약, 재생성, 덮어쓰기 하지 않는다.

`CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`는 검수, 프로젝트 진행, 완료 판정, 반복 실패 방지, Codex 요청서 작성 기준이다.

이번 요청의 핵심은 아래다.

```text
1. 대용량 목표파일을 앞으로 요청서 작성에 안정적으로 활용하기 위한 요청서 작성용 인덱스 보강
2. CPF_EVIDENCE_INDEX.md / CPF_STABILIZATION_REPORT.md / CPF_GAP_MATRIX.md / 기능 매트릭스 정합성 복구
3. check-report-matrix-evidence-consistency.ps1와 qualityGate 신뢰 회복
4. README 진입점 보강
5. PFW Service Call Engine / Service Registry / Remote Facade Proxy / ADM 관제 대형 착수
```

이번 요청은 CPF 전체 최종 목표를 한 번에 완료하라는 뜻이 아니다.
하지만 대형 마일스톤 단위로 가능한 만큼 많이 진행한다.

---

## 1. Codex 크레딧 절약 / 작업 효율 기준

이번 작업은 크게 진행하되, 불필요한 repo 전체 탐색, 같은 실패 반복, 장문 리포트 재작성은 피한다.

```text
1. 목표 달성을 위해 필요한 소스, 설정, SQL, 테스트, smoke script, evidence 연결 수정은 Codex 판단으로 수행한다.
2. 목표와 무관한 문서 정본화, 대규모 스타일 변경, 불필요한 리팩토링은 하지 않는다.
3. CPF_FINAL_TARGET_REQUIREMENTS.md는 대용량 파일이므로 전체를 장문 출력하거나 요약하지 않는다.
4. 목표파일은 필요한 키워드, 제목, REQ-ID prefix, 섹션 단위로 검색한다.
5. 동일 실패는 같은 방식으로 반복하지 않는다. 1회 재시도 후에도 실패하면 원인과 대안을 기록한다.
6. 전체 Gradle test/qualityGate는 중간에 반복 실행하지 말고 targeted check 후 마지막에 수행한다.
7. 결과 리포트는 CPF_STABILIZATION_REPORT.md, CPF_GAP_MATRIX.md, CPF_EVIDENCE_INDEX.md 중심으로 최소 갱신한다.
8. 실행하지 않은 검증은 완료로 기록하지 않는다.
```

---

## 2. 필수 제한

아래는 반드시 지킨다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
Authorization/Bearer/X-Api-Key/password/secret/credential/signature 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
별도 변경파일 목록만을 위한 산출물 생성 금지
문서량 채우기식 문서화 금지
PDF 생성 금지
CPF_NEW_REQUEST.md 임의 수정 금지
```

기존 `specs/기능_구현_매트릭스.html`은 repo 표준 및 check script와 연결되어 있으므로, 정합성 복구를 위한 최소 수정은 허용한다.

---

## 3. 필수 완료 범위 A — 대용량 목표파일 요청서 작성용 인덱스 보강

### 3.1 목적

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 대용량 최종 목표파일이다. ChatGPT와 Codex가 매번 전체 파일을 통째로 읽지 못하더라도, 요청서와 작업 플랜이 목표파일 기준에서 흔들리지 않도록 파일 맨 앞에 **요청서 작성용 목표 인덱스**를 추가한다.

상세 목표 본문은 삭제하거나 축약하지 않는다.
인덱스는 상세 본문을 대체하는 문서가 아니라, 상세 본문을 찾기 위한 navigation이다.

### 3.2 추가 위치

`CPF_FINAL_TARGET_REQUIREMENTS.md` 최상단 또는 기존 목차 바로 아래에 아래 섹션을 추가한다.

```text
# 0. 요청서 작성용 목표 인덱스

이 섹션은 Codex와 ChatGPT가 대용량 목표파일을 기준으로 요청서와 검수 범위를 잡기 위한 navigation이다.
상세 목표 본문을 대체하지 않는다.
요청서 작성 시 이 인덱스의 도메인, 키워드, REQ-ID prefix, 검증 범위를 먼저 확인한 뒤 상세 본문을 검색한다.
```

### 3.3 필수 인덱스 도메인

아래 도메인별로 최소 항목을 만든다.

```text
PFW-SERVICE-CALL
PFW-SERVICE-REGISTRY
PFW-TRANSACTION-CONTEXT
PFW-LOGGING-TRACE-BOOST
ADM-TRANSACTION-MONITORING
ADM-SERVICE-REGISTRY-MONITORING
SAGA-COMPENSATION
OUTBOX-INBOX-IDEMPOTENCY
BAT-WORKER
BAT-CENTER-CUT
BAT-DOMAIN-CALL
EXS-INTEGRATION
CMN-COMMON-FUNCTION
SECURITY-RBAC-ABAC
MASKING-PRIVACY-SECRET
BROKER-EVENT
SQL-FLYWAY-INSTALL
RUNTIME-SMOKE-EVIDENCE
BROWSER-CLICK
RELEASE-DR-OPERATIONS
ARCHITECTURE-RULE-CHECK
```

각 도메인에는 아래 필드를 넣는다.

```text
도메인명
목적
주요 키워드
관련 REQ-ID prefix 후보
관련 모듈
필수 구현 파일군
필수 SQL/Flyway/all_install 확인 범위
필수 ADM/API/UI 확인 범위
필수 smoke/evidence 범위
완료 불인정 기준
대형 요청서 작성 시 포함 기준
```

### 3.4 예시 형식

```text
## 0.x PFW-SERVICE-CALL

목적:
- CPF Service Call Engine 기준으로 주제영역 간 호출을 표준화한다.
- 업무 코드는 URL 직접 조합, Controller 직접 호출, 타 주제영역 DB 직접 접근을 금지한다.

주요 키워드:
- CpfServiceCallEngine
- CpfWebClient
- CpfRestClient
- Local Facade
- Remote Facade Proxy
- service/endpoint/instance registry
- health-aware routing
- failover
- timeout
- retry
- circuit breaker
- selectedInstanceId

관련 REQ-ID prefix 후보:
- PFW-CALL-*
- PFW-REG-*
- PFW-ROUTE-*
- PFW-RESILIENCE-*
- ADM-SVC-*

필수 확인:
- source
- yml/properties
- split SQL
- Flyway
- 00_all_install.sql
- 00_all_install_and_smoke.sql
- 99_smoke_check.sql
- unit/integration test
- runtime smoke
- ADM API
- report/matrix/evidence

완료 불인정:
- HTTP wrapper만 있고 registry/routing/health/failover가 없음
- selectedInstanceId 로그 없음
- SQL/Flyway/all_install 누락
- runtime smoke 없음
```

### 3.5 완료 기준

```text
CPF_FINAL_TARGET_REQUIREMENTS.md 상세 본문은 유지된다.
요청서 작성용 목표 인덱스가 최상단에 추가된다.
PFW/ADM/BAT/EXS/CMN/Security/Broker/SQL/Runtime 도메인의 요청서 작성 기준이 들어간다.
인덱스는 상세 본문을 대체하지 않는다고 명시한다.
README에 목표파일 대용량 사용 방식이 짧게 안내된다.
```

### 3.6 완료 불인정

```text
상세 목표 본문 삭제
대용량 목표파일 재생성
요약 인덱스만 남기고 상세 체크포인트 제거
목표파일을 다시 작은 파일로 축소
요청서 작성용 인덱스 없이 기존 본문만 유지
```

---

## 4. 필수 완료 범위 B — Evidence index 경로 불일치 수정

### 4.1 현재 문제

`CPF_EVIDENCE_INDEX.md`는 일부 `.log` 파일을 완료 증적으로 참조한다.

예시:

```text
specs/evidence/20260707_01/quality-gate.log
specs/evidence/20260707_01/check-html-docs.log
specs/evidence/20260707_01/check-feature-evidence.log
specs/evidence/20260707_01/check-utf8-mojibake.log
specs/evidence/20260707_01/edu-mapper-db-slice.log
```

현재 repo에 실제 존재하는 evidence와 `CPF_EVIDENCE_INDEX.md`가 참조하는 경로를 전수 대조한다.

### 4.2 처리 방식

둘 중 하나로 정리한다.

#### 1안 — 실제 로그 파일을 evidence에 저장

최종 검증 명령 출력을 sanitized log로 저장한다.

```powershell
New-Item -ItemType Directory -Force specs/evidence/20260707_02 | Out-Null

.\gradlew.bat qualityGate --offline --no-daemon --console=plain *> specs/evidence/20260707_02/quality-gate.log

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1 *> specs/evidence/20260707_02/check-html-docs.log

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1 *> specs/evidence/20260707_02/check-feature-evidence.log

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake *> specs/evidence/20260707_02/check-utf8-mojibake.log
```

EDU mapper DB slice도 실제 실행 로그 또는 sanitized summary JSON으로 남긴다.

#### 2안 — repo에 로그를 올리지 않을 정책이면 index에서 제거

로그 파일을 repo에 포함하지 않을 정책이면 `CPF_EVIDENCE_INDEX.md`에서 완료 증적 경로로 쓰지 않는다.
이 경우 완료 근거는 repo에 실제 존재하는 sanitized JSON 또는 manifest만 사용한다.

### 4.3 완료 기준

```text
CPF_EVIDENCE_INDEX.md가 참조하는 모든 evidence 경로가 실제 repo에 존재한다.
존재하지 않는 파일을 완료 근거로 쓰지 않는다.
JSON evidence는 parse 가능하다.
log evidence를 쓰면 실제 파일이 존재한다.
real broker/browser click 미검증 항목은 없음/미검증으로 유지한다.
```

---

## 5. 필수 완료 범위 C — 기능 매트릭스와 consistency script 구조 정합화

### 5.1 현재 문제

`specs/기능_구현_매트릭스.html`은 확장자는 `.html`이지만 현재 내용은 Markdown 형식에 가깝다.
`check-report-matrix-evidence-consistency.ps1`가 어떤 구조를 파싱하는지와 matrix 파일 구조를 맞춘다.

### 5.2 처리 방식

둘 중 하나로 정리한다.

#### 권장 1안 — 실제 HTML table 구조로 복구

기능 매트릭스를 실제 HTML table로 만들고 check id를 명확히 둔다.

필수 예시:

```html
<tr data-check-id="mariadb-full-install">
  <td>mariadb-full-install</td>
  <td class="status" data-status="완료">완료</td>
  <td>specs/evidence/20260707_01/mariadb-full-install-result.sanitized.json</td>
  <td>MariaDB full install smoke 통과</td>
</tr>
```

#### 대안 2안 — script가 Markdown 구조도 파싱하도록 확장

Markdown 형태를 유지하려면 `check-report-matrix-evidence-consistency.ps1`가 현재 matrix 구조를 정확히 파싱하도록 수정한다.

단, `.html` 확장자에 Markdown 내용을 넣는 혼란은 해소한다. 가능하면 1안을 우선한다.

### 5.3 동기화 대상 check id

아래 check id는 반드시 `CPF_STABILIZATION_REPORT.md`, `CPF_EVIDENCE_INDEX.md`, `CPF_GAP_MATRIX.md`, `specs/기능_구현_매트릭스.html`에서 상태와 evidence가 일관되게 연결되어야 한다.

```text
edu-mapper-db-slice
mariadb-full-install
adm-runtime
adm-permission-runtime
openapi-runtime
adm-browser-click
standard-header-e2e
complex-transaction-trace
transaction-segment-log
adm-transaction-group-list
adm-transaction-timeline
cmn-fixed-length-engine
composite-runtime-smoke
adm-transaction-group-runtime
redis-kafka-mq-broker
broker-real-integration
file-log-standard
trace-boost-runtime
bat-trace-boost-runtime
runtime-start-services
packaged-runtime-resources
runtime-status-diagnostics
runtime-closure
adm-operation-console-runtime
adm-log-policy-ui-static
bat-log-bean-runtime
exs-timeout-retry-runtime
cmn-fixed-length-advanced
create-domain-smoke
runtime-smoke-summary
quality-gate
check-html-docs
check-feature-evidence
check-utf8
```

### 5.4 EDU mapper 상태 정리

현재 EDU mapper는 기능 상태와 검증 상태가 다르게 보일 수 있다.
아래처럼 분리해서 혼동을 제거한다.

```text
기능 상태: EDU/XYZ mapper 교육 fixture 제공 여부
검증 상태: 실제 MariaDB DB slice 테스트 실행 여부
```

같은 check id의 검증 상태는 report/evidence와 일치시킨다.

### 5.5 완료 기준

```text
기능 매트릭스가 consistency script에서 실제 파싱된다.
report/matrix/evidence 상태값이 일치한다.
미검증 항목은 완료로 집계되지 않는다.
ADM browser click은 실제 browser click 전까지 미검증이다.
Redis/Kafka/MQ real broker는 실제 broker 실행 전까지 미검증이다.
EDU mapper 기능 상태와 검증 상태가 혼동되지 않는다.
```

---

## 6. 필수 완료 범위 D — consistency gate와 qualityGate 신뢰 회복

### 6.1 consistency script 강화

`scripts/check-report-matrix-evidence-consistency.ps1`는 아래를 검증해야 한다.

```text
CPF_STABILIZATION_REPORT.md의 check id와 상태
CPF_EVIDENCE_INDEX.md의 check id와 상태/evidence 경로
specs/기능_구현_매트릭스.html의 check id와 상태
CPF_GAP_MATRIX.md의 미검증/실패/후순위 항목
evidence 파일 존재 여부
JSON parse 가능 여부
완료 항목의 evidence 존재 여부
미검증 항목이 완료로 집계되지 않는지
browser click SKIPPED가 완료가 아닌지
real broker 미실행이 완료가 아닌지
```

### 6.2 실제 실행

수정 후 아래를 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

### 6.3 evidence

검증 결과는 아래 중 하나로 남긴다.

```text
specs/evidence/20260707_02/report-matrix-evidence-consistency-result.sanitized.json
specs/evidence/20260707_02/quality-gate.log
specs/evidence/20260707_02/evidence-manifest.sanitized.json
```

### 6.4 완료 기준

```text
check-report-matrix-evidence-consistency.ps1가 실제 matrix를 파싱한다.
불일치가 있으면 실패한다.
불일치가 없으면 성공한다.
qualityGate가 consistency gate를 포함한다.
qualityGate 성공 evidence가 실제 repo 파일로 존재한다.
```

---

## 7. 필수 완료 범위 E — README 진입점 보강

README는 짧게 유지하되 주요 문서 목록에 아래를 반드시 포함한다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
specs/기능_구현_매트릭스.html
```

README에는 역할을 짧게 명시한다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md = 무엇을 만들어야 하는가
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md = 어떻게 검수하고 완료로 볼 것인가
CPF_STABILIZATION_REPORT.md = 최근 작업 검증 결과
CPF_GAP_MATRIX.md = 남은 미구현/미검증/실패 항목
CPF_EVIDENCE_INDEX.md = 검증 증적 경로
specs/기능_구현_매트릭스.html = 기능/검증 상태 매트릭스
```

대용량 목표파일 사용 방식도 한 줄로 넣는다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md는 대용량 최종 목표파일이므로 요청서 작성 시 최상단 요청서 작성용 목표 인덱스와 관련 REQ-ID/키워드 검색을 우선 사용한다.
```

---

## 8. 필수 완료 범위 F — PFW Service Call Engine / Service Registry 대형 착수

정합성 복구가 완료되면 바로 PFW Service Call Engine을 크게 착수한다.

### 8.1 목표파일 기준 검색

작업 전 `CPF_FINAL_TARGET_REQUIREMENTS.md`에서 아래 키워드를 로컬 기준으로 검색한다.

```text
Service Call Engine
CpfWebClient
CpfRestClient
Remote Facade Proxy
Local Facade
service registry
endpoint registry
instance registry
routing policy
health check
failover
timeout
retry
circuit breaker
selectedInstanceId
transactionGlobalId
segment propagation
```

검색 결과의 관련 섹션/REQ-ID prefix를 `CPF_STABILIZATION_REPORT.md`에 짧게 기록한다.

### 8.2 DB/SQL 착수

아래 테이블 후보를 설계/구현한다.

```text
pfw_service
pfw_service_endpoint
pfw_service_instance
pfw_service_health_status
pfw_service_routing_policy
pfw_service_circuit_state
pfw_service_call_history
```

필수 필드 후보:

```text
serviceCode
moduleCode
endpointCode
endpointMode
lbUrl
instanceId
instanceUrl
healthUrl
zone
region
weight
priority
enabledYn
maintenanceYn
status
lastHealthCheckAt
lastSuccessAt
lastFailureAt
failureCount
latencyMs
routingPolicy
timeoutMs
retryPolicyId
circuitPolicyId
selectedInstanceId
transactionGlobalId
transactionSegmentId
sourceModuleCode
targetModuleCode
```

SQL 반영 범위:

```text
split SQL
Flyway migration
00_all_install.sql
00_all_install_and_smoke.sql
99_smoke_check.sql
```

한 경로만 반영하면 완료로 기록하지 않는다.

### 8.3 Java 구성 착수

아래 클래스/컴포넌트 후보를 구현한다.

```text
CpfServiceCallEngine
CpfServiceRegistry
CpfEndpointRegistry
CpfServiceInstanceRegistry
CpfEndpointResolver
CpfRoutingPolicyResolver
CpfHealthAwareInstanceSelector
CpfRemoteFacadeProxySupport
CpfServiceCallLogWriter
CpfServiceHealthChecker
CpfServiceCallProperties
```

필수 기능:

```text
LB endpoint mode
direct instance mode
health-aware routing
round-robin 후보
priority/weight 후보
timeout
retry
failover
circuit breaker 상태 후보
selectedInstanceId logging
remote response header capture
transactionGlobalId propagation
transactionSegmentId propagation
source/target module logging
```

### 8.4 Local/Remote Facade 기준 착수

아래 기준을 명시적으로 구현/문서화/테스트한다.

```text
Facade는 같은 JVM 전용이 아니다.
같은 JVM에서는 Local Facade 사용.
MSA에서는 Facade Contract/Port를 Remote Facade Proxy가 구현.
Remote Facade Proxy는 CpfWebClient/CpfRestClient 또는 CpfServiceCallEngine을 통해 대상 API 호출.
업무 코드는 URL 직접 조합 금지.
업무 코드는 Controller 직접 호출 금지.
업무 코드는 타 주제영역 DB 직접 접근 금지.
```

가능하면 ACC → MBR 또는 ACC → EXS 호출 중 하나를 Service Call Engine 경유 예제로 보강한다.

### 8.5 설정

`application.yml` 또는 모듈별 yml/properties에 아래 설정 후보를 추가한다.

```yaml
cpf:
  service-call:
    enabled: true
    endpoint-mode: direct # direct | lb
    timeout-ms: 3000
    retry:
      enabled: true
      max-attempts: 2
    circuit-breaker:
      enabled: true
    health-check:
      enabled: true
    logging:
      selected-instance: true
      response-header-capture: true
```

모든 신규/수정 설정에는 한글 설명 주석을 철저히 작성한다.

### 8.6 ADM API 착수

아래 API 후보를 구현한다.

```text
GET /adm/api/service-registry/services
GET /adm/api/service-registry/endpoints
GET /adm/api/service-registry/instances
GET /adm/api/service-registry/health
GET /adm/api/service-registry/routing-policies
GET /adm/api/service-registry/circuit-states
GET /adm/api/service-registry/call-history
```

필수 응답 필드 후보:

```text
serviceCode
moduleCode
endpointCode
instanceId
endpointMode
status
healthStatus
selectedYn
lastHealthCheckAt
lastSuccessAt
lastFailureAt
failureCount
latencyMs
routingPolicy
circuitState
```

ADM browser UI는 이번 요청에서 완료 강제하지 않는다.
API와 runtime smoke를 우선한다.

### 8.7 Smoke/Test

가능한 범위에서 아래 smoke를 추가/실행한다.

```text
service registry seed smoke
service instance health smoke
direct instance routing smoke
LB endpoint mode config smoke
selectedInstanceId log smoke
failover fallback smoke
ADM service registry API smoke
```

스크립트 후보:

```text
scripts/smoke-service-registry-runtime.ps1
scripts/smoke-service-call-engine-runtime.ps1
scripts/smoke-adm-service-registry-runtime.ps1
```

---

## 9. 보강 범위 — 남은 미검증 항목 유지

아래는 이번 요청에서 완료로 올리지 않는다.

```text
ADM browser click
Redis/Kafka/MQ real broker
broker 장애/복구/DLQ/replay
2대/3대 multi-instance 실검증
DR/backup/restore
성능 benchmark
최종 PDF/HTML 정본화
전체 15,000개 이상 체크포인트 구현 완료
```

단, 목표에서 삭제하지 말고 `CPF_GAP_MATRIX.md`에 미검증 또는 후순위로 유지한다.

---

## 10. 필수 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-report-matrix-evidence-consistency.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake

.\gradlew.bat clean :pfw:bootJar :acc:bootJar :mbr:bootJar :adm:bootJar :exs:bootJar :bat:bootJar --offline --no-daemon --console=plain --rerun-tasks

.\gradlew.bat test --offline --no-daemon --console=plain

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

모듈명이 실제 repo와 다르면 실제 Gradle module 기준으로 조정한다.
전체 test가 너무 크면 관련 targeted test를 먼저 실행하고 마지막에 qualityGate를 수행한다.

---

## 11. 완료 리포트 기준

작업 종료 시 아래 파일을 최소 갱신한다.

```text
CPF_STABILIZATION_REPORT.md
CPF_GAP_MATRIX.md
CPF_EVIDENCE_INDEX.md
README.md
specs/기능_구현_매트릭스.html
```

리포트에는 아래를 기록한다.

```text
요청 범위
목표파일 요청서 작성용 인덱스 보강 여부
목표파일 관련 검색 키워드/섹션/REQ-ID prefix
report/matrix/evidence 정합성 결과
수정한 정합성 항목
PFW Service Call Engine 착수 범위
SQL/Flyway/all_install 반영 여부
실행 명령
실행 일시
profile
DB 종류
runtime smoke 여부
browser click 여부
real broker 검증 여부
evidence 경로
실패/미검증 사유
다음 조치
```

결과 리포트는 최소 갱신한다. 로그 전문을 장문으로 붙이지 않는다.

---

## 12. 완료 기준

이번 요청의 완료 기준은 아래다.

```text
CPF_FINAL_TARGET_REQUIREMENTS.md 상세 본문이 삭제/축약/덮어쓰기 되지 않음
CPF_FINAL_TARGET_REQUIREMENTS.md 상단에 요청서 작성용 목표 인덱스가 추가됨
CPF_EVIDENCE_INDEX.md가 존재하지 않는 파일을 완료 증적으로 참조하지 않음
기능 매트릭스와 consistency script 구조가 맞음
report/matrix/evidence 상태값이 일치함
qualityGate 성공 evidence가 실제 repo 파일로 존재함
README 주요 문서 목록에 기준 파일과 report/gap/evidence/matrix 파일이 포함됨
PFW Service Call Engine / Service Registry / Remote Facade Proxy가 착수됨
관련 SQL/Flyway/all_install/smoke가 가능한 범위에서 반영됨
ADM browser click과 real broker는 실제 실행 전까지 미검증으로 유지됨
실행하지 않은 검증을 완료로 기록하지 않음
Git commit/push/branch 생성 없음
```

---

## 13. 완료 불인정 기준

아래는 완료로 인정하지 않는다.

```text
목표파일 상세 본문 삭제 또는 축약
목표파일을 작은 파일로 다시 덮어쓰기
요청서 작성용 목표 인덱스 없이 완료 주장
없는 evidence 파일을 완료 근거로 참조
feature matrix를 script가 실제 파싱하지 못함
report는 완료인데 matrix/evidence는 미검증 또는 누락
qualityGate 성공 로그/summary가 repo에 없음
browser click SKIPPED를 완료로 기록
real broker 미실행을 완료로 기록
PFW Service Call Engine을 단순 HTTP wrapper 수준으로만 구현하고 완료 주장
SQL/Flyway/all_install 중 한 경로만 반영하고 완료 주장
CPF_NEW_REQUEST.md 임의 수정
Git commit/push/branch 생성
```

---

## 14. 완료 보고 마지막 문장

완료 보고 마지막에는 반드시 아래를 쓴다.

```text
이번 작업에서 실제 실행한 검증과 실행하지 않은 검증을 분리했습니다.
실행하지 않은 검증은 완료로 기록하지 않았습니다.
report/matrix/evidence 상태값 정합성을 실제 파일 기준으로 확인했습니다.
CPF_FINAL_TARGET_REQUIREMENTS.md 상세 본문은 삭제/축약/덮어쓰기 하지 않았습니다.
Git commit/push/branch 생성은 수행하지 않았습니다.
```
