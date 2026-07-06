# CPF 대형 작업 요청서 2/6 — Runtime Closure Harness + ADM 운영 콘솔 + EXS/CMN 연계 고도화

## 0. 최상위 기준

작업 시작 전 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 확인한다.

이번 작업은 CPF 전체 완료를 향한 6개 대형 요청 중 2번째 작업이다.
목표를 운영형 v1로 축소하지 않는다.
문서 정본화는 이번 작업 범위에서 제외한다.

중간 문서는 아래 최소 항목만 갱신한다.

```text
CPF_STABILIZATION_REPORT.md
specs/기능_구현_매트릭스.html
specs/evidence/{이번작업일자}/ sanitized evidence
필요한 README 최소 보강
```

가이드 문장 정리, HTML 디자인 정리, PDF/DOCX 생성, 최종 Runbook 정본화는 하지 않는다.

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
CPF_FINAL_TARGET_REQUIREMENTS.md 수정 금지
문서 정본화 작업 금지
```

`CPF_NEW_REQUEST.md`는 작업 요청서 원본이므로 원칙적으로 수정하지 않는다.
수정이 불가피한 경우에는 완료 보고에 반드시 아래를 기록한다.

```text
수정 사유
수정 범위
사용자 기존 변경과 충돌 여부
왜 요청서 파일을 건드려야 했는지
```

## 2. 컨펌 대기 금지 / 자율 진행 기준

작업 중 사용자에게 “계속 진행해도 되냐”, “A/B 중 선택해달라”고 묻고 멈추지 않는다.

Codex는 아래 원칙으로 자율 진행한다.

```text
비파괴 작업은 질문하지 말고 진행한다.
컴파일 오류는 직접 분석해 수정한다.
테스트 실패는 직접 원인 분류 후 수정한다.
runtime 실패는 환경/구현/설계/범위/도구 한계로 분류한다.
환경 문제라도 source-level/test/script/evidence 개발은 멈추지 않는다.
요건이 CPF 구조와 충돌하면 대체 설계안을 적용하고 보고한다.
```

단, 아래는 금지한다.

```text
Git commit/push/branch 생성
운영 DB 또는 사용자 데이터 삭제
실제 외부기관 호출
민감정보 원문 기록
```

## 3. 이번 작업의 큰 목표

이번 작업은 단순히 실패 smoke를 다시 돌리는 작업이 아니다.
아래 5개 축을 한 번에 진행한다.

```text
1. Runtime Closure Harness 구축
2. 파일 로그 / Trace Boost / BAT runtime 실패 원인 제거
3. ADM 운영 콘솔 실사용 기능 확장
4. EXS/CMN REST/전문/timeout/retry 고도화
5. 전체 evidence/report/matrix 정합성 강화
```

## 4. 필수 완료 범위 A — Runtime Closure Harness 구축

현재 반복 문제의 핵심은 ACC/ADM/EXS/BAT 런타임이 안정적으로 떠 있지 않아 file log, Trace Boost, BAT trace boost가 계속 실패하는 것이다.

이번 작업에서는 smoke만 반복하지 말고, 서비스 기동/진단/종료/로그수집을 표준화한다.

### 4.1 통합 runtime start/stop/status 스크립트

아래 스크립트를 신규 또는 보강한다.

```text
scripts/runtime-start-services.ps1
scripts/runtime-stop-services.ps1
scripts/runtime-status.ps1
scripts/runtime-diagnostics.ps1
scripts/smoke-runtime-closure.ps1
```

필수 기능:

```text
ACC/MBR/ADM/EXS/BAT 선택 기동
각 모듈별 port 확인
각 모듈별 process 확인
각 모듈별 health/openapi 확인
각 모듈별 app log tail 수집
각 모듈별 cpf-{moduleCode}-{logType}.log 존재 확인
기동 실패 시 무한 대기 금지
timeout bounded 처리
종료 시 child process cleanup
```

기본 port 후보:

```text
ACC 8080
MBR 8081
EXS 8092
ADM 8090
BAT 8093
```

### 4.2 Runtime failure classification

runtime 실패 시 반드시 아래를 구분한다.

```text
환경 문제: port 미기동, DB 미기동, Java process 없음, 권한 문제
구현 문제: app boot 실패, bean 생성 실패, endpoint 500, 로그 파일 생성 실패
설계 문제: 모듈 책임/bean 위치/auto configuration 구조 문제
요청 범위 문제: 선행 기능 미완료
도구 한계: 브라우저/Playwright/실 broker 없음
```

완료 보고에 반드시 아래를 남긴다.

```text
실패 모듈
실패 port
process 상태
마지막 app log
마지막 cpf-* log
bean 생성 여부
대체 검증
다음 조건
```

### 4.3 Runtime closure evidence

신규 evidence:

```text
specs/evidence/20260706_03/runtime-start-services-result.sanitized.json
specs/evidence/20260706_03/runtime-status-result.sanitized.json
specs/evidence/20260706_03/runtime-diagnostics-result.sanitized.json
```

완료 기준:

```text
ACC/MBR/ADM/EXS/BAT 중 필수 대상 기동 시도 증거 존재
기동 성공/실패 상태 명확
실패 시 원인 분류 존재
무한 대기 없음
process cleanup 기준 존재
```

## 5. 필수 완료 범위 B — 파일 로그 runtime closure

현재 source-level hook은 들어왔지만 runtime file grep 증거가 없다.

필수:

```text
ACC → EXS 성공 호출 시 logs/acc/cpf-acc-integration.log 생성
ACC → EXS 실패 호출 시 logs/acc/cpf-acc-integration.log 생성
EXS 내부 처리 시 logs/exs/cpf-exs-integration.log 생성
온라인 거래 시 logs/acc/cpf-acc-transaction.log 생성
PFW 내부 context/policy 로그는 logs/pfw/cpf-pfw-*에만 생성
ACC WAS에 cpf-exs-* 파일 생성 금지
EXS WAS에 cpf-acc-* 파일 생성 금지
```

검증 필드:

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
apiPath
httpMethod
status
failureCode
durationMs
serverId
instanceId
hostName
hostIp
port
processId
profile
appVersion
buildVersion
```

실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime
```

evidence:

```text
specs/evidence/20260706_03/file-log-standard-result.sanitized.json
specs/evidence/20260706_03/file-log-grep-summary.sanitized.log
```

완료 기준:

```text
file-log-standard status 완료
transactionGlobalId grep 성공
ACC integration log에 EXS outbound result 존재
EXS integration log에 EXS 내부 처리 result 존재
민감정보 원문 미노출
```

만약 runtime이 또 실패하면 단순 재시도만 하지 않는다.
반드시 runtime harness 기준으로 원인을 분류하고, 구현 문제면 이번 작업에서 수정한다.

## 6. 필수 완료 범위 C — Trace Boost runtime closure

현재 Trace Boost API는 source-level이고 runtime은 ADM port 미기동으로 실패했다.

필수:

```text
ADM runtime 기동
/adm/api/log-policies/trace-boost 정책 생성
/admlog runtime-state 조회
history 조회
disable 또는 rollback
TTL 만료 또는 수동 해제
운영자 사유 필수
정책 변경 audit 저장
root logger 전체 변경 금지
특정 transactionGlobalId 또는 apiPath/failureCode 조건만 DEBUG/TRACE 적용
파일 로그에 traceBoostPolicyId 기록
민감정보 원문 미노출
```

API 경로는 실제 구현에 맞게 사용하되, 완료 보고에 실제 호출 경로를 기록한다.

실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime
```

evidence:

```text
specs/evidence/20260706_03/trace-boost-runtime-result.sanitized.json
```

완료 기준:

```text
trace boost 정책 생성 완료
대상 거래 파일 로그에 traceBoostPolicyId 존재
대상 외 거래는 기본 log level 유지
root logger level 변경 없음
history/audit 조회 가능
cleanup 완료
```

## 7. 필수 완료 범위 D — BAT batch file log / BAT Trace Boost runtime closure

현재 BAT listener는 writer를 호출하는 구조가 들어왔지만 runtime에서 `logs/bat/cpf-bat-batch.log`가 생성되지 않았다.

이번 작업에서는 BAT 부트 환경의 bean/설정/log path 문제를 직접 닫는다.

필수 확인:

```text
BAT runtime에서 CpfBatchFileLogWriter bean 생성 여부
CpfBatchRuntimeListener bean 생성 여부
Batch job에 CpfBatchRuntimeListener 실제 연결 여부
cpf.logging.file.enabled=true 여부
cpf.logging.file.batch-enabled=true 여부
cpf.logging.file.base-path 실제 값
server.port=8093 또는 BAT port 실제 값
BAT process working directory
logs/bat 디렉터리 생성 권한
```

필수 구현/보강:

```text
BAT job/step 실행 시 cpf-bat-batch.log 생성
jobName 기록
jobExecutionId 기록
stepName 기록
stepExecutionId 기록
runId/rerunId 후보 기록
workerInstanceId/serverId/instanceId 기록
traceBoostPolicyId 또는 logLevelApplied 기록
failureMessageMasked 마스킹 적용
```

필수 diagnostic endpoint 또는 script:

```text
scripts/smoke-bat-log-bean-runtime.ps1
```

후보 조회:

```text
/actuator/beans 사용 가능 시 bean 확인
불가하면 BAT internal diagnostic API 또는 app log marker로 확인
```

실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules BAT
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-log-bean-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1 -RunBatRuntime -RequireRuntime
```

evidence:

```text
specs/evidence/20260706_03/bat-log-bean-runtime-result.sanitized.json
specs/evidence/20260706_03/bat-trace-boost-runtime-result.sanitized.json
```

완료 기준:

```text
BAT runtime에서 cpf-bat-batch.log 생성
Job/Step event grep 성공
traceBoostPolicyId 또는 logLevelApplied 확인
BAT runtime smoke 완료
```

## 8. 필수 완료 범위 E — ADM 운영 콘솔 기능 확장

이번 작업은 runtime closure만 하지 않는다. ADM 운영 콘솔 기능 진도도 같이 낸다.

필수 API/UI 범위:

```text
ADM 거래 그룹 목록/상세/timeline 기존 유지
ADM EXS 송수신 원장 조회 화면/API 보강
ADM 전문 송수신 조회 후보
ADM 오류 상세 조회
ADM 감사 조회
ADM Log Policy / Trace Boost 정책 조회
ADM Trace Boost 생성/해제 UI 1차
ADM BAT/Center-Cut 조회 연결 후보
권한별 허용/차단 검증
마스킹 필드와 원문 금지 확인
```

browser click은 이번에도 도구가 없으면 무한 반복하지 않는다.

대체 검증:

```text
static UI marker smoke
API runtime smoke
screenshot 또는 HTML marker evidence
permission runtime smoke
```

상태는 정확히 남긴다.

```text
browser click 도구 없음: 미검증
API/static/permission 대체 검증 성공: 완료 또는 부분 구현
```

evidence:

```text
specs/evidence/20260706_03/adm-operation-console-runtime-result.sanitized.json
specs/evidence/20260706_03/adm-log-policy-ui-static-result.sanitized.json
```

## 9. 필수 완료 범위 F — EXS/CMN REST/전문/timeout/retry 고도화

이번 작업에서 EXS/CMN 축도 같이 진도 낸다.

### 9.1 EXS timeout/retry/circuit/fallback

필수:

```text
기관/endpoint별 timeout 설정
retry 설정
retryCount 기록
timeoutYn 기록
failureCode 매핑
responseCode → CPF error code 매핑
fallback 또는 재처리 후보 기록
ADM EXS 송수신 조회에서 retry/timeout/failure 확인
```

### 9.2 CMN fixed-length 전문 고도화

필수:

```text
layout/group/field DB 사전 유지
반복부 parse/format 보강
type converter 보강
validation error 보강
field masking policy 보강
전문 fixture 추가
EXS 송수신 로그와 transactionGlobalId 연결
```

### 9.3 전문 runtime 또는 source-level smoke

가능하면 runtime smoke를 실행한다.
외부 실기관이 없으면 mock endpoint 또는 내부 fixture 기반으로 닫고, 실기관은 미검증으로 남긴다.

evidence:

```text
specs/evidence/20260706_03/exs-timeout-retry-runtime-result.sanitized.json
specs/evidence/20260706_03/cmn-fixed-length-advanced-result.sanitized.json
```

## 10. 보강 범위

가능하면 아래도 진행한다.

```text
ADM onboarding status API 1차
create-domain 생성 결과를 ADM onboarding status와 연결
logging health API 1차
파일 로그 rolling/retention 설정 초안
prod profile logging guardrail startup validation
XYZ/BIZADM OpenAPI runtime smoke 범위 확장
```

보강 범위는 못 닫아도 완료로 포장하지 않는다.

## 11. 착수 범위

가능하면 착수한다.

```text
Redis/Kafka/MQ real broker smoke 초안
broker unavailable fallback 정책
다중 인스턴스 ACC/EXS 처리 인스턴스 smoke 초안
event/outbox 후보
retention/purge policy seed 후보
```

실 broker가 없으면 mock/embedded 성공을 real broker 완료로 쓰지 않는다.

## 12. 후순위 / 제외 범위

이번 작업에서 제외한다.

```text
최종 문서 정본화
HTML 디자인 정리
PDF/DOCX 생성
실기관 EXS 호출
전체 Redis/Kafka/MQ 완료
다중 인스턴스 실환경 전체 검증
대규모 성능 benchmark
최종 Runbook
```

## 13. 필수 검증 명령

compile/test:

```powershell
.\gradlew.bat :pfw:compileJava :adm:compileJava :exs:compileJava :bat:compileJava --offline --no-daemon --console=plain --rerun-tasks
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

static checks:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

runtime harness:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-status.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-diagnostics.ps1
```

runtime smoke:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-log-bean-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1 -RunBatRuntime -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-failure-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1 -RequireRuntime
```

EXS/CMN:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-exs-timeout-retry-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-cmn-fixed-length-advanced.ps1
```

summary/evidence:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/sync-runtime-smoke-summary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260706_03
```

## 14. 완료 기준

이번 작업은 아래를 만족해야 완료로 본다.

```text
runtime start/status/diagnostics harness 존재
ACC/MBR/EXS/ADM/BAT 기동 상태 확인 가능
file-log-standard runtime 완료 또는 구현 문제 수정 후 원인 봉인
Trace Boost runtime 완료 또는 구현 문제 수정 후 원인 봉인
BAT batch log runtime 완료
cpf-bat-batch.log 생성 증거 존재
CBW/WebClient/RestClient 로그가 호출 주체 moduleCode 로그에 남음
EXS 로그가 EXS 로그에 남음
ACC WAS에 cpf-exs-* 로그 생성 안 됨
ADM 운영 콘솔 API/UI 기능 확장
EXS timeout/retry/failureCode/responseCode 고도화
CMN fixed-length 반복부/type/validation/masking 보강
sanitized evidence 생성
report/matrix/evidence/runtime-summary 상태 일치
민감정보 원문 미노출
```

## 15. 완료 불인정 기준

아래는 완료로 인정하지 않는다.

```text
서비스 기동 실패를 단순히 같은 smoke 실패로만 남김
runtime failure 원인 분류 없음
ACC/ADM port 미기동 상태를 계속 반복
file-log source만 있고 grep evidence 없음
Trace Boost API만 있고 runtime 적용 없음
BAT writer만 있고 cpf-bat-batch.log 없음
root logger 전체 DEBUG로 Trace Boost 구현
mock/embedded broker를 real broker 완료로 기록
browser click 도구 없음인데 완료 표기
실행하지 않은 검증 완료 기록
CPF_NEW_REQUEST.md 변경 후 미수정 보고
```

## 16. 완료 보고 양식

완료 보고는 아래 형식으로 작성한다.

```text
1. 작업 요약

2. 자율 진행 판단
   - 사용자 컨펌 없이 진행한 항목
   - 선택한 기본값
   - 요건 변경/대체 설계

3. Runtime Harness 결과
   - start/status/diagnostics
   - 모듈별 port/process/health
   - 실패 원인 분류

4. 파일 로그 runtime 결과
   - 생성 파일
   - transactionGlobalId grep
   - 민감정보 검사
   - 모듈별 로그 분리 확인

5. Trace Boost runtime 결과

6. BAT batch log / Trace Boost 결과

7. ADM 운영 콘솔 결과

8. EXS/CMN 고도화 결과

9. 보강/착수 범위 결과

10. 미검증 항목
    - 환경 문제 / 구현 문제 / 설계 문제 / 요청 범위 문제 / 도구 한계
    - 사유
    - 대체 검증
    - 다음 조건

11. 실패 항목
    - 실패 명령
    - 원인
    - 수정 여부
    - 대안

12. Closed

13. Closed with Limitation

14. Still Open

15. Blocked

16. Do Not Rework Without Failure Evidence
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

## 17. 다음 작업 분기

이번 작업 후 분기는 아래 기준으로 판단한다.

```text
Runtime Harness + 파일로그 + Trace Boost + BAT runtime이 닫히면:
  → 대형 요청 3/6: BAT/Center-Cut/Worker 운영 완성 + ADM BAT 관제 고도화

Runtime Harness는 닫혔지만 일부 runtime이 환경 문제면:
  → 환경 문제는 봉인하고 EXS/CMN/BAT 기능 진도 계속 진행

파일 로그가 구현 문제로 실패하면:
  → 다음 작업보다 먼저 file log writer/context/log path를 수정

BAT log가 계속 실패하면:
  → BAT auto configuration, listener registration, job wiring, log base path를 최우선 수정

EXS/CMN 고도화가 닫히면:
  → 다음 작업에서 실 broker/multi instance 준비로 이동
```
