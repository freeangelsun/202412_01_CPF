# CPF 대형 작업 요청서 — 파일 로그 표준, Trace Boost, BAT 로그, 온보딩 생성기, 검증 충돌 봉인

## 0. 최상위 기준

작업 시작 전에 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 확인한다.

이번 작업은 기능 개발을 우선한다.
문서 포맷 정리, HTML 정본화, 가이드 문장 개선은 후순위다.

문서는 아래 최소 정보만 갱신한다.

* `CPF_STABILIZATION_REPORT.md`
* 기능 구현 매트릭스
* 필요한 README 최소 보강
* sanitized evidence 경로
* 미검증/실패 사유
* 다음 필요 조건

## 1. 필수 제한

아래는 반드시 지킨다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
Authorization/Bearer/X-Api-Key/password/secret/credential/signature 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 정리만으로 기능 완료 처리 금지
CPF_NEW_REQUEST.md 수정 금지
CPF_FINAL_TARGET_REQUIREMENTS.md 수정 금지
```

## 2. 컨펌 대기 금지 / 자율 진행 기준

이번 작업에서는 사용자에게 “계속 진행해도 되냐”, “A/B 중 선택해달라”는 식으로 멈추지 않는다.

Codex는 아래 기준으로 자율 진행한다.

```text
비파괴 작업은 질문하지 말고 진행한다.
기능 구현, 테스트, smoke, SQL 정합성 확인, evidence 생성, 리포트 갱신은 자율 진행한다.
요건이 애매하면 CPF_FINAL_TARGET_REQUIREMENTS.md 기준으로 합리적 기본값을 선택하고 완료 보고에 선택 사유를 기록한다.
요건이 CPF 구조와 충돌하면 무리하게 구현하지 말고 요건 변경안을 제시한다.
실패하거나 미검증이면 질문으로 멈추지 말고 원인 분류와 대안을 기록한다.
```

단, 아래는 여전히 금지한다.

```text
Git commit/push/branch 생성
운영 DB 또는 사용자 데이터 삭제
민감정보 원문 기록
요청서/최종목표 파일 임의 수정
외부 시스템에 실제 발송/실제 기관 호출
```

## 3. 실패/미검증 처리 규칙

동일 항목을 같은 방식으로 반복 실패시키지 않는다.

실패 또는 미검증 항목은 반드시 아래 중 하나로 분류한다.

```text
환경 문제
구현 문제
설계 문제
요청 범위 문제
도구 한계
```

각 항목에는 반드시 아래를 기록한다.

```text
안 되는 이유
이번 작업에서 취한 대체 검증
다음에 필요한 조건
요건 변경이 필요한지 여부
이번 작업에서 계속 진행한 후속 기능
```

예:

```text
ADM browser click:
- 분류: 도구 한계
- 사유: Node/npm/npx/Playwright/browser driver 없음
- 대체 검증: UI marker + API runtime + static smoke + self-diagnosis evidence
- 상태: 미검증
- 다음 조건: Node/npm/npx/Playwright 또는 browser driver 설치
- 기능 개발은 계속 진행
```

## 4. 이번 작업의 큰 목표

이번 작업은 아래 5개 축을 크게 진행한다.

```text
1. 직전 검증 충돌 봉인
2. PFW 파일 로그 표준 구현
3. ADM 동적 로그 레벨 / Trace Boost 온라인 거래 1차 구현
4. BAT 배치 로그 / 배치 Trace Boost 1차 구현
5. 신규 주제영역 온보딩 생성기 1차 구현
```

이번 작업은 단순 보완 작업이 아니다.
기능 개발 진도가 나야 한다.

## 5. 필수 완료 범위 A — 직전 검증 충돌 봉인

### 5.1 standard-header-e2e summary 충돌 정리

현재 상세 evidence는 완료로 보이나 runtime summary에는 `standard-header-e2e exitCode=1`이 남아 있다.

필수:

```text
standard-header-e2e를 재실행하거나 summary 생성 로직을 점검한다.
상세 result와 summary가 같은 상태를 가리키게 한다.
실패였으면 실패로 기록한다.
성공이면 exitCode 0으로 재생성한다.
report / matrix / runtime summary / evidence 상태값을 일치시킨다.
```

완료 기준:

```text
standard-header-e2e-result.sanitized.json
runtime-smoke-summary.sanitized.json
CPF_STABILIZATION_REPORT.md
기능 매트릭스
```

위 4개가 같은 상태를 보여야 한다.

### 5.2 evidence consistency gate 보강

필수:

```text
check-feature-evidence.ps1 또는 별도 check-report-status-consistency.ps1에서
report/matrix/evidence/runtime-summary 간 상태 충돌을 잡는다.
```

잡아야 할 예:

```text
상세 evidence status 완료인데 summary exitCode 1
report는 완료인데 evidence missing
matrix는 완료인데 runtime summary 실패
browser click 미검증인데 완료 표기
```

## 6. 필수 완료 범위 B — PFW 파일 로그 표준

DB 로그뿐 아니라 파일 로그도 CPF 핵심 완료 기준이다.

### 6.1 파일 로그명 표준

아래 패턴을 구현한다.

```text
cpf-{moduleCode}-{logType}.log
```

필수 logType:

```text
application
transaction
integration
audit
error
batch
```

예:

```text
logs/pfw/cpf-pfw-application.log
logs/pfw/cpf-pfw-transaction.log
logs/pfw/cpf-pfw-error.log

logs/acc/cpf-acc-application.log
logs/acc/cpf-acc-transaction.log
logs/acc/cpf-acc-integration.log
logs/acc/cpf-acc-error.log

logs/exs/cpf-exs-application.log
logs/exs/cpf-exs-transaction.log
logs/exs/cpf-exs-integration.log
logs/exs/cpf-exs-error.log

logs/bat/cpf-bat-batch.log
logs/bat/cpf-bat-error.log
```

금지:

```text
cpf-external.log 같은 EXS 특수 파일명
모든 모듈 로그를 cpf-application.log 하나에 섞기
ACC WAS에 cpf-exs-* 로그 파일 생성
EXS WAS에 cpf-acc-* 로그 파일 생성
```

### 6.2 파일 로그 필수 필드

모든 구조화 파일 로그에는 아래 공통 필드가 있어야 한다.

```text
timestamp
level
logType
eventType
moduleCode
sourceModuleCode
targetModuleCode
transactionGlobalId
transactionSegmentId
parentSegmentId
transactionRole
direction
apiPath
httpMethod
status
durationMs
failureCode
failureMessageMasked
serverId
instanceId
hostName
hostIp
port
processId
containerId
podName
profile
appVersion
buildVersion
```

integration 로그에는 추가로 아래를 포함한다.

```text
integrationType
protocolType
institutionCode
endpointCode
externalTransactionId
httpStatus
responseCode
retryCount
timeoutMs
timeoutYn
requestHeaderMasked
responseHeaderMasked
requestPayloadMasked
responsePayloadMasked
```

### 6.3 ACC → EXS 오류 응답 요약 로그

ACC가 EXS를 호출하고 EXS가 오류 응답을 보내면, ACC 쪽 로그에도 오류 응답 요약이 남아야 한다.

ACC WAS에는 `cpf-pfw-*`, `cpf-acc-*` 로그만 존재해야 한다.
EXS 내부 stacktrace를 ACC 로그에 복제하지 않는다.

ACC `cpf-acc-integration.log` 필수 항목:

```text
sourceModuleCode=ACC
targetModuleCode=EXS
direction=OUTBOUND
apiPath
httpMethod
httpStatus
responseCode
durationMs
retryCount
timeoutYn
status
failureCode
failureMessageMasked
responsePayloadMasked summary
remoteModuleCode
remoteServerId
remoteInstanceId
remoteHostName
remotePort
remoteTransactionSegmentId 후보
```

EXS 내부 원인은 같은 `transactionGlobalId`로 EXS WAS의 `cpf-exs-*` 로그에서 확인한다.

### 6.4 이중화/다중 인스턴스 식별자

파일 로그와 DB segment에는 아래 식별자가 있어야 한다.

```text
serverId
instanceId
hostName
hostIp
port
processId
containerId
podName
profile
appVersion
buildVersion
startupTime
servingRole
```

EXS가 응답할 때는 가능하면 아래 응답 헤더를 제공한다.

```text
X-Cpf-Module-Code
X-Cpf-Server-Id
X-Cpf-Instance-Id
X-Cpf-Host-Name
X-Cpf-App-Version
X-Transaction-Id
X-Transaction-Segment-Id
```

ACC는 응답 헤더를 받아 자신의 outbound log에 remote instance 정보를 기록한다.

### 6.5 환경파일 설정

DB 로그와 파일 로그 저장 여부는 profile별 환경파일에서 제어 가능해야 한다.

필수 설정 후보:

```yaml
cpf:
  logging:
    db:
      enabled: true
      transaction-enabled: true
      audit-enabled: true
      error-enabled: true
      integration-enabled: true
      batch-enabled: true
    file:
      enabled: true
      base-path: ./logs
      json-line-enabled: true
      application-enabled: true
      transaction-enabled: true
      integration-enabled: true
      audit-enabled: true
      error-enabled: true
      batch-enabled: true
      file-pattern: "cpf-{moduleCode}-{logType}.log"
    masking:
      enabled: true
      raw-sensitive-log-enabled: false
    dynamic-level:
      enabled: true
      trace-boost-enabled: true
      max-ttl-minutes: 30
      require-operator-reason: true
```

운영 guardrail:

```text
prod에서 audit/error/transaction/masking을 임의로 비활성화하지 못하게 제한한다.
비활성화가 필요한 경우 명시적 경고, 사유, 감사 또는 fail-fast를 제공한다.
```

### 6.6 파일 로그 smoke

신규 smoke를 추가한다.

```text
scripts/smoke-file-log-standard-runtime.ps1
```

검증:

```text
ACC 성공 거래 후 cpf-acc-transaction.log grep
ACC → EXS 실패 후 cpf-acc-integration.log grep
EXS 처리 후 cpf-exs-integration.log grep
PFW context 로그 cpf-pfw-transaction.log grep
transactionGlobalId로 각 파일 로그 검색 가능
failureCode=EXS_TIMEOUT 검색 가능
serverId/instanceId 존재
민감정보 원문 미노출
```

sanitized evidence:

```text
specs/evidence/20260703_05/file-log-standard-result.sanitized.json
specs/evidence/20260703_05/file-log-grep-summary.sanitized.log
```

## 7. 필수 완료 범위 C — ADM 동적 로그 레벨 / Trace Boost 온라인 거래

운영 중 전체 WAS 로그 레벨을 올리는 방식은 금지한다.
특정 거래 또는 조건에 맞는 거래만 DEBUG/TRACE 상세 로그가 활성화되어야 한다.

### 7.1 DB 테이블 후보

```text
pfw_dynamic_log_level_rule
pfw_dynamic_log_level_history
pfw_trace_boost_policy
pfw_trace_boost_runtime_state
```

필수 컬럼 후보:

```text
policyId
policyName
targetType
targetValueMasked
conditionJson
moduleCode
serverId
instanceId
loggerName
level
enabledYn
startedAt
expiresAt
ttlSeconds
createdBy
createdReason
approvedBy
status
rollbackAt
rollbackReason
createdAt
updatedAt
```

### 7.2 ADM API

후보 API:

```text
GET    /adm/api/log-policies
POST   /adm/api/log-policies/trace-boost
POST   /adm/api/log-policies/{policyId}/disable
GET    /adm/api/log-policies/runtime-state
GET    /adm/api/log-policies/history
```

지원 조건:

```text
transactionGlobalId
transactionSegmentId
moduleCode
serverId
instanceId
apiPath
status
failureCode
durationMsFrom
institutionCode
endpointCode
```

### 7.3 동작 기준

필수:

```text
전체 root logger 변경 금지
특정 transactionGlobalId TRACE boost
apiPath/status/failureCode/duration 조건 TRACE boost
TTL 자동 원복
수동 원복
운영자 사유 필수
정책 변경 감사 로그
파일 로그에 traceBoostPolicyId 기록
DEBUG/TRACE에서도 민감정보 원문 금지
```

### 7.4 Smoke

신규 smoke:

```text
scripts/smoke-trace-boost-runtime.ps1
```

검증:

```text
정책 생성
특정 transactionGlobalId만 TRACE 상세 로그 증가
다른 거래는 기본 INFO 유지
traceBoostPolicyId가 파일 로그에 기록
DB history 저장
TTL 만료 후 자동 원복
수동 원복 API 동작
root logger level이 변경되지 않음
민감정보 원문 미노출
```

sanitized evidence:

```text
specs/evidence/20260703_05/trace-boost-runtime-result.sanitized.json
```

## 8. 필수 완료 범위 D — BAT 배치 로그 / 배치 Trace Boost 1차

BAT에도 온라인 거래와 같은 로그 운영성이 필요하다.

### 8.1 BAT 파일 로그

필수 파일:

```text
logs/bat/cpf-bat-application.log
logs/bat/cpf-bat-batch.log
logs/bat/cpf-bat-transaction.log
logs/bat/cpf-bat-error.log
```

필수 필드:

```text
jobName
jobExecutionId
stepName
stepExecutionId
runId
rerunId
workerInstanceId
serverId
instanceId
itemId
chunkNo
partitionNo
transactionGlobalId
traceBoostPolicyId
logLevelApplied
status
durationMs
failureCode
failureMessageMasked
```

### 8.2 배치 Trace Boost

필수:

```text
특정 jobExecutionId만 DEBUG/TRACE
특정 stepExecutionId만 DEBUG/TRACE
특정 rerunId만 DEBUG/TRACE
특정 workerInstanceId/serverId/instanceId만 DEBUG/TRACE
배치 재수행 시 해당 실행만 로그 레벨 상향
실행 종료 시 자동 원복
운영자 사유 필수
감사 로그
파일 로그 traceBoostPolicyId
```

### 8.3 Smoke

신규 smoke:

```text
scripts/smoke-bat-trace-boost-runtime.ps1
```

검증:

```text
BAT worker 기동
job 실행
step 실행
특정 rerun/jobExecutionId trace boost 적용
해당 실행 파일 로그만 DEBUG/TRACE 상세 증가
다른 job 실행은 기본 level 유지
실행 종료 후 자동 원복
ADM BAT 조회 또는 API 조회
```

환경 문제로 BAT runtime이 어렵다면:

```text
bootJar/test/DB/API/source-level은 닫고
runtime만 미검증으로 봉인한다.
같은 방식으로 반복 실패하지 않는다.
```

## 9. 필수 완료 범위 E — 신규 주제영역 온보딩 생성기 1차

신규 주제영역을 기존 모듈 복사 후 수동 수정하는 방식으로 만들지 않는다.

### 9.1 생성 방식

ADM에서 소스를 직접 생성하지 않는다.

로컬 개발 워크스페이스에서 관리자급 개발자가 실행하는 CLI 또는 Gradle task로 구현한다.

후보:

```text
scripts/create-domain.ps1
또는
./gradlew createDomain
```

실행 예:

```powershell
powershell -File scripts/create-domain.ps1 `
  -ModuleCode LON `
  -ModuleName loan `
  -DisplayName "대출" `
  -BasePackage cpf.lon `
  -Port 8095 `
  -TablePrefix lon
```

### 9.2 생성 대상

필수 생성:

```text
신규 Gradle module
settings.gradle 반영 후보 또는 patch
build.gradle
application-{module}.yml
Java package
Controller
Service
Facade
Repository/Mapper
DTO
Validation
Mapper XML
sample CRUD
search/sort whitelist
offset paging
keyset paging 후보
validation
unit test
mapper DB slice test 초안
runtime smoke 초안
README 초안
```

PFW/ADM 자동 대응 후보:

```text
PFW module registry seed
PFW log/header/transaction context 대상 등록 seed
cpf-{moduleCode}-{logType}.log 설정
ADM menu seed
ADM API permission seed
ADM button permission seed
ADM transaction group moduleCode 후보
ADM log policy moduleCode 후보
masking/audit policy seed 후보
common code seed 후보
```

SQL:

```text
split SQL 후보
Flyway migration 후보
00_all_install.sql 반영 후보
00_all_install_and_smoke.sql 반영 후보
99_smoke_check.sql 반영 후보
```

### 9.3 Dry-run / 안전장치

필수:

```text
-DryRun 지원
이미 존재하는 moduleCode면 중단
이미 존재하는 package/tablePrefix면 중단
생성 전 validation
생성 후 summary 출력
```

금지:

```text
기존 ACC 폴더 통째 복사 후 문자열 치환만 하는 방식
ADM 운영 서버가 Git 소스 직접 생성
Git commit/push 자동 수행
```

### 9.4 Smoke

신규 smoke:

```text
scripts/smoke-create-domain.ps1
```

검증:

```text
DryRun 성공
임시 moduleCode TMP 또는 LON 생성
생성 모듈 compile
생성 모듈 test
settings/build/yml/SQL seed 후보 생성 확인
PFW/ADM seed 후보 생성 확인
민감정보 원문 없음
```

이번 작업에서 전체 all_install 자동 수정이 위험하면:

```text
patch 후보 또는 generated SQL 후보로 남긴다.
상태는 부분 구현으로 기록한다.
단, 생성기 자체 compile/test는 닫는다.
```

## 10. 보강 범위

가능하면 아래도 진행한다.

```text
EXS timeout/retry/circuit breaker 운영 시나리오 1차
CMN fixed-length 사전 ADM 조회 API 1차
ADM log policy UI 1차
ADM onboarding status UI 1차
XYZ/BIZADM OpenAPI runtime smoke 범위 확장
```

보강 범위는 못 닫아도 완료로 포장하지 않는다.

## 11. 착수 범위

가능하면 아래를 착수한다.

```text
Redis/Kafka/MQ real broker smoke 초안
broker unavailable fallback 정책
다중 인스턴스 ACC/EXS 처리 인스턴스 smoke 초안
event/outbox 후보
retention/purge 정책 후보
```

실제 broker가 없으면 미검증으로 남긴다.
mock/embedded 성공을 real broker 완료로 기록하지 않는다.

## 12. 후순위 / 제외 범위

이번 작업에서 완료 강제하지 않는다.

```text
최종 문서 정본화
전체 HTML 디자인 정리
실기관 EXS 연계
전체 Redis/Kafka/MQ 완료
다중 인스턴스 실환경 전체 검증
대규모 성능 benchmark
```

## 13. 필수 검증 명령

가능한 범위에서 실행한다.

```text
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat qualityGate --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
```

runtime:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-failure-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-failure-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-create-domain.ps1
```

MariaDB:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-all-install-sql.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-mariadb-full-install.ps1 -RequireRun
```

evidence:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260703_05
```

## 14. 완료 기준

이번 작업 완료는 아래를 만족해야 한다.

```text
standard-header-e2e summary 충돌 해소
report/matrix/evidence/runtime-summary 상태 일치
파일 로그 표준 구현
cpf-{moduleCode}-{logType}.log 생성
transactionGlobalId 기준 파일 로그 grep 가능
DB 로그와 파일 로그 연결
ACC → EXS 오류 응답 요약이 ACC integration log에 남음
EXS 내부 상세는 EXS 로그에서 확인
serverId/instanceId/hostName/port 기록
환경파일로 DB/file 로그 on/off 제어 가능
prod guardrail 존재
Trace Boost 온라인 거래 1차 구현
TTL 자동 원복
운영자 사유/감사 로그
root logger 변경 금지 검증
BAT batch log/trace boost 1차 구현
create-domain 1차 구현
sanitized evidence 생성
민감정보 원문 미노출
SQL/Flyway/all_install 정합성 유지
```

## 15. 완료 불인정 기준

아래는 완료로 인정하지 않는다.

```text
standard-header summary 충돌 방치
파일 로그 grep evidence 없음
DB 로그만 있고 파일 로그 없음
모든 로그가 하나의 application.log에 섞임
EXS 특수 cpf-external.log만 생성
ACC WAS에 cpf-exs-* 로그 생성
Trace Boost가 root logger 전체 DEBUG로 구현됨
TTL/자동 원복 없음
운영자 사유/감사 없음
DEBUG/TRACE에서 민감정보 원문 노출
BAT 재수행 trace boost 없음
create-domain이 기존 모듈 통째 복사 후 문자열 치환만 함
ADM이 운영 서버에서 Git 소스를 직접 생성
실행하지 않은 검증 완료 기록
mock/embedded broker를 real broker 완료로 기록
```

## 16. 완료 보고 양식

완료 보고는 아래 형식으로 작성한다.

```text
1. 작업 요약

2. 컨펌 없이 자율 진행한 판단
   - 선택한 기본값
   - 선택 사유
   - 요건 변경/대체 설계가 있었는지

3. 필수 완료 범위 결과
   - 항목 / 상태 / evidence / 남은 확인

4. 검증 충돌 봉인 결과
   - standard-header-e2e
   - report/matrix/evidence/runtime-summary 정합성

5. 파일 로그 표준 결과
   - 생성 파일
   - grep evidence
   - 민감정보 확인
   - DB 로그 연결

6. Trace Boost 결과
   - 정책 DB/API/UI
   - TTL/원복
   - 파일 로그 traceBoostPolicyId
   - 감사 로그

7. BAT 로그/Trace Boost 결과

8. create-domain 결과

9. 보강/착수 범위 결과

10. 미검증 항목
    - 분류: 환경/구현/설계/범위/도구
    - 사유
    - 대체 검증
    - 다음 조건

11. 실패 항목
    - 실패 명령
    - 원인 분류
    - 수정 여부
    - 대안

12. Closed
    - 이번 작업에서 다시 요청하지 않아도 되는 항목

13. Closed with Limitation
    - 소스/SQL/test는 닫혔지만 환경 검증만 남은 항목

14. Still Open
    - 다음 작업에서 이어갈 항목

15. Blocked
    - 환경/도구/권한 문제

16. Do Not Rework Without Failure Evidence
    - 실패 증거 없으면 구조 변경하지 말 항목
```

상태값은 반드시 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

## 17. 다음 작업 분기 기준

이번 작업 후 다음 분기는 아래 기준으로 판단한다.

```text
파일 로그와 Trace Boost가 닫히면:
  → ADM 운영 UX, EXS timeout/retry, CMN 사전 관리 UI로 이동

Trace Boost가 온라인만 닫히고 BAT가 남으면:
  → BAT/Center-Cut 운영 기능을 다음 필수 범위로 유지

create-domain이 1차 성공하면:
  → 신규 생성 모듈 runtime smoke와 ADM onboarding status UI로 이동

파일 로그가 계속 미검증이면:
  → 다음 작업도 파일 로그 grep evidence를 최우선으로 유지

standard-header summary 충돌이 남으면:
  → 다른 완료 항목도 재확인 필요로 격하한다
```
