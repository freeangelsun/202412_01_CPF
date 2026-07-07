# CPF 대형 작업 요청서 3/6 — bootJar/runtime 기동 폐쇄 + 파일로그/TraceBoost/BAT runtime 완료 + ADM/BAT/EXS 기능 진도

## 0. 최상위 기준

작업 시작 전 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 확인한다.

이번 작업은 CPF 전체 완료를 향한 6개 대형 요청 중 3번째 작업이다.
목표를 운영형 v1로 축소하지 않는다.
문서 정본화는 이번 작업 범위에서 제외한다.

중간 문서는 아래 최소 항목만 갱신한다.

```text
CPF_STABILIZATION_REPORT.md
specs/기능_구현_매트릭스.html
specs/evidence/{이번작업일자}/ sanitized evidence
필요한 README 최소 보강
```

가이드 문장 다듬기, HTML 디자인 정리, PDF/DOCX 생성, 최종 Runbook 정본화는 하지 않는다.

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

`CPF_NEW_REQUEST.md`는 원칙적으로 수정하지 않는다.
수정이 불가피하면 완료 보고에 반드시 사유, 범위, 사용자 기존 변경과 충돌 여부를 기록한다.

## 2. 컨펌 대기 금지 / 자율 진행 기준

작업 중 사용자에게 “계속 진행해도 되냐”, “A/B 중 선택해달라”고 묻고 멈추지 않는다.

Codex는 아래 원칙으로 자율 진행한다.

```text
비파괴 작업은 질문하지 말고 진행한다.
컴파일 오류는 직접 분석해 수정한다.
테스트 실패는 직접 원인 분류 후 수정한다.
runtime 실패는 환경/구현/설계/범위/도구 한계로 분류한다.
동일 실패를 같은 방식으로 반복하지 않는다.
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

이번 작업은 runtime smoke를 다시 돌리는 작업이 아니다.
아래 6개를 반드시 함께 수행한다.

```text
1. ACC/MBR/ADM/EXS/BAT bootJar rebuild와 packaged resource 검증
2. runtime harness 판정 오류 수정
3. ACC D:/logs 권한 오류 제거
4. 파일 로그/Trace Boost/BAT runtime closure 완료
5. ADM 운영 콘솔 runtime 기능 검증
6. EXS timeout/retry와 BAT/Center-Cut 기능 진도
```

## 4. 필수 완료 범위 A — bootJar rebuild / packaged resource 검증

현재 runtime evidence에서 ACC가 `D:/logs` 경로로 기동 실패했다.
source logback은 `logs` 기준으로 보정됐지만, runtime jar에는 오래된 설정이 남아 있을 수 있다.

### 4.1 bootJar clean rebuild

필수 실행:

```powershell
.\gradlew.bat clean :acc:bootJar :mbr:bootJar :adm:bootJar :exs:bootJar :bat:bootJar --offline --no-daemon --console=plain --rerun-tasks
```

완료 기준:

```text
acc bootJar 성공
mbr bootJar 성공
adm bootJar 성공
exs bootJar 성공
bat bootJar 성공
```

### 4.2 packaged resource 검사

신규 또는 보강 스크립트:

```text
scripts/check-packaged-runtime-resources.ps1
```

검사 대상:

```text
acc/build/libs/*.jar
mbr/build/libs/*.jar
adm/build/libs/*.jar
exs/build/libs/*.jar
bat/build/libs/*.jar
```

필수 검사:

```text
jar 안에 D:/logs, D:\logs, C:/logs, C:\logs 하드코딩이 없어야 한다.
jar 안에 cpf.logging.file.base-path 기본값이 logs 기준이어야 한다.
jar 안의 logback-local/test/dev/prod 설정이 실제 source와 일치해야 한다.
application-local/test/dev/prod yml의 server.port와 logging path가 모듈별 기준과 맞아야 한다.
```

evidence:

```text
specs/evidence/20260706_04/packaged-runtime-resource-check.sanitized.json
```

완료 불인정:

```text
source만 고치고 jar 재빌드 안 함
jar 안에 D:/logs가 남아 있음
bootJar 실패인데 기존 jar로 runtime 완료 처리
```

## 5. 필수 완료 범위 B — runtime harness 판정 로직 수정

현재 start evidence에서 bootJarBuild는 FAILED인데 모듈 status가 완료로 찍힌다.
이 상태는 잘못된 판정이다.

필수 수정:

```text
bootJarBuild.status=FAILED이면 해당 모듈 runtime start는 완료로 기록하지 않는다.
기존 jar를 fallback으로 사용했다면 status는 재확인 필요 또는 부분 구현으로 기록한다.
startup 직후 port가 열렸더라도 최종 status에서 processAlive=false이면 완료로 보지 않는다.
start/status/diagnostics/closure가 같은 결론을 내도록 정합성을 맞춘다.
```

필수 추가 필드:

```text
bootJarBuildRequired
bootJarBuildStatus
jarFallbackUsed
processStarted
portOpened
processStillAliveAfterProbe
healthCheckPassed
finalRuntimeUsable
failureClassification
failureRootCause
```

완료 기준:

```text
runtime-start-services-result
runtime-status-result
runtime-diagnostics-result
runtime-closure-result
runtime-smoke-summary
```

위 5개가 같은 상태를 가리켜야 한다.

## 6. 필수 완료 범위 C — ACC D:/logs 오류 제거

필수:

```text
ACC logback-local/test/dev/prod에서 D:/logs, D:\logs 제거
ACC application yml의 logging path와 cpf.logging.file.base-path 정합성 확인
runtime-start-services.ps1에서 CPF_LOGGING_FILE_BASE_PATH=logs override가 실제 jar에 반영되는지 확인
logback에서 LOG_PATH 기본값이 logs 기준인지 확인
transaction sifting appender가 null file path를 만들지 않도록 방어
NO_TRANSACTION 로그도 logs/acc/transactions 하위에 생성되도록 보정
```

추가 검증:

```text
D:/logs 문자열 전체 검색
jar 내부 D:/logs 검색
runtime stdout/stderr D:/logs 검색
```

완료 기준:

```text
ACC runtime start 성공
D:/logs 오류 재발 없음
logs/acc 하위에 로그 생성
```

## 7. 필수 완료 범위 D — 전체 runtime 기동 성공

필수 실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT -BuildBeforeRun -StartupTimeoutSeconds 120 -NoExitOnFailure
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-status.ps1 -Modules ACC,MBR,EXS,ADM,BAT -NoExitOnFailure
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-diagnostics.ps1 -Modules ACC,MBR,EXS,ADM,BAT -NoExitOnFailure
```

완료 기준:

```text
ACC 8080 listening=true
MBR 8081 listening=true
EXS 8092 listening=true
ADM 8090 listening=true
BAT 8093 listening=true
processAlive=true
health/openapi 또는 health endpoint 확인
```

만약 특정 모듈이 실패하면:

```text
기동 실패 원인 분류
마지막 stdout/stderr tail
마지막 app log tail
bean 생성 실패 여부
port 충돌 여부
DB 연결 실패 여부
log path 실패 여부
```

를 남긴다.

단, 이번 작업에서는 runtime 실패를 단순 환경 문제로만 넘기지 말고, **구현 문제면 반드시 수정**한다.

## 8. 필수 완료 범위 E — 파일 로그 runtime closure

필수 실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime
```

필수 검증:

```text
ACC 성공 거래 후 logs/acc/cpf-acc-transaction.log 생성
ACC → EXS 성공 호출 후 logs/acc/cpf-acc-integration.log 생성
ACC → EXS 실패 호출 후 logs/acc/cpf-acc-integration.log 생성
EXS 내부 처리 후 logs/exs/cpf-exs-integration.log 생성
PFW 내부 context/policy 로그는 logs/pfw/cpf-pfw-* 또는 모듈별 pfw log 기준으로 확인
BAT 실행 후 logs/bat/cpf-bat-batch.log 생성
transactionGlobalId grep 성공
failureCode=EXS_TIMEOUT grep 성공
serverId/instanceId/hostName/hostIp/port/processId 존재
Authorization/Bearer/X-Api-Key/password/secret/credential/signature 원문 미노출
```

금지:

```text
기존 acc-2026-07-02.log, pfw-2026-07-02.log 같은 과거 로그로 완료 처리
source hook만 보고 완료 처리
```

evidence:

```text
specs/evidence/20260706_04/file-log-standard-result.sanitized.json
specs/evidence/20260706_04/file-log-grep-summary.sanitized.log
```

## 9. 필수 완료 범위 F — Trace Boost runtime closure

필수 실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime
```

필수 검증:

```text
ADM login 또는 운영자 인증 우회 smoke 기준 명확화
Trace Boost 정책 생성
runtime-state 조회
history 조회
disable/rollback
TTL 만료 또는 수동 해제
운영자 사유 필수
정책 변경 audit 저장
root logger 전체 변경 금지
대상 transactionGlobalId/apiPath/failureCode 조건에만 DEBUG/TRACE 적용
파일 로그에 traceBoostPolicyId 기록
대상 외 거래는 기본 로그 레벨 유지
민감정보 원문 미노출
```

완료 불인정:

```text
ADM port 미기동
API source만 있음
UI static marker만 있음
root logger 전체 DEBUG 변경
traceBoostPolicyId grep 없음
```

## 10. 필수 완료 범위 G — BAT batch log / BAT Trace Boost runtime closure

필수 실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-log-bean-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1 -RunBatRuntime -RequireRuntime
```

필수 검증:

```text
/bat/api/diagnostics/logging 응답 성공
CpfBatchFileLogWriter bean=true
CpfBatchRuntimeListener bean=true
jobListenerWiring 확인
smoke job 실행 성공
logs/bat/cpf-bat-batch.log 생성
jobName/jobExecutionId/stepName/stepExecutionId 기록
runId/rerunId 후보 기록
serverId/instanceId/workerInstanceId 기록
traceBoostPolicyId 또는 logLevelApplied 기록
failureMessageMasked 마스킹
```

완료 불인정:

```text
BAT port 미기동
bean diagnostic API만 있고 실제 batch log 없음
listener source만 있고 job 실행 로그 없음
```

## 11. 필수 완료 범위 H — ADM 운영 콘솔 runtime 기능 검증

필수 실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-operation-console-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-log-policy-ui-static.ps1
```

필수 검증:

```text
ADM 거래 그룹 목록/상세/timeline 조회
ADM EXS external logs 조회
ADM Log Policy 조회
ADM Trace Boost 생성/상태/이력/해제 API
ADM BAT/Center-Cut 조회 후보
권한별 버튼/메뉴/API 허용/차단 후보
마스킹 필드 원문 미노출
```

browser click은 도구가 없으면 미검증으로 남긴다.
단, API runtime + static marker + permission smoke는 최대한 닫는다.

## 12. 필수 완료 범위 I — EXS timeout/retry runtime closure

필수 실행:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-exs-timeout-retry-runtime.ps1 -RequireRuntime
```

필수 검증:

```text
기관/endpoint timeoutMs 조회
retryCount 조회
EXS_TIMEOUT 실패 호출
exs_transaction_log 또는 exs_message_log에 timeout/retry/failure 기록
responseCode → CPF error code 매핑 후보
ADM EXS 송수신 조회에서 timeout/retry/failure 확인
logs/exs/cpf-exs-integration.log에 timeout/retry 기록
```

실기관 호출은 금지한다. 내부 smoke/mock endpoint 기준으로 검증한다.

## 13. 보강 범위 — BAT/Center-Cut 운영 기능 진도

runtime이 닫힌 뒤 가능하면 아래도 진행한다.

```text
BAT worker heartbeat 조회
ghost candidate 조회
lock/stale lock 조회
stop/rerun/force run API 보강
operator action audit
center-cut parent/child/item/result runtime smoke
ADM BAT/Center-Cut 관제 API/UI 보강
```

닫지 못하면 부분 구현/미검증으로 남긴다.

## 14. 보강 범위 — EXS/CMN 전문 고도화 추가

가능하면 아래도 진행한다.

```text
CMN fixed-length DB 사전과 parser/formatter 연결 강화
전문 송수신 fixture 추가
전문 오류 코드 매핑
EXS 전문 송수신 로그 후보
ADM 전문 송수신 조회 후보
```

## 15. 착수 범위

가능하면 착수한다.

```text
Redis/Kafka/MQ real broker smoke 초안
broker unavailable fallback 정책
다중 인스턴스 ACC/EXS 처리 인스턴스 smoke 초안
event/outbox 후보
retention/purge policy seed 후보
```

실 broker가 없으면 완료로 쓰지 않는다.

## 16. 후순위 / 제외 범위

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

## 17. 필수 검증 명령

compile/test:

```powershell
.\gradlew.bat clean :acc:bootJar :mbr:bootJar :adm:bootJar :exs:bootJar :bat:bootJar --offline --no-daemon --console=plain --rerun-tasks
.\gradlew.bat :pfw:compileJava :adm:compileJava :exs:compileJava :bat:compileJava --offline --no-daemon --console=plain --rerun-tasks
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

static checks:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-packaged-runtime-resources.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

runtime:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-start-services.ps1 -Modules ACC,MBR,EXS,ADM,BAT -BuildBeforeRun -StartupTimeoutSeconds 120 -NoExitOnFailure
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-status.ps1 -Modules ACC,MBR,EXS,ADM,BAT -NoExitOnFailure
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/runtime-diagnostics.ps1 -Modules ACC,MBR,EXS,ADM,BAT -NoExitOnFailure
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-log-bean-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1 -RunBatRuntime -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-operation-console-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-exs-timeout-retry-runtime.ps1 -RequireRuntime
```

summary/evidence:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/sync-runtime-smoke-summary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260706_04
```

## 18. 완료 기준

이번 작업은 아래를 만족해야 완료로 본다.

```text
bootJar clean rebuild 성공
packaged jar 안 D:/logs 제거 확인
runtime harness 판정 로직 정정
ACC/MBR/ADM/EXS/BAT port listening=true
processAlive=true
file-log-standard runtime 완료
Trace Boost runtime 완료
BAT log bean runtime 완료
BAT trace boost runtime 완료
ADM operation console runtime 완료 또는 browser click만 미검증으로 분리
EXS timeout/retry runtime 완료
runtime-smoke-summary exitCode 0
report/matrix/evidence 상태 일치
민감정보 원문 미노출
```

## 19. 완료 불인정 기준

아래는 완료로 인정하지 않는다.

```text
bootJarBuild 실패인데 runtime start 완료 표기
기존 jar fallback을 완료로 표기
최종 port closed인데 완료 표기
D:/logs 오류 재발
파일 로그 grep 없음
Trace Boost API만 있고 runtime 적용 없음
BAT diagnostic API만 있고 cpf-bat-batch.log 없음
ADM port 미기동인데 운영 콘솔 runtime 완료 표기
EXS port 미기동인데 timeout/retry runtime 완료 표기
runtime summary exitCode 1인데 관련 항목 완료 표기
```

## 20. 완료 보고 양식

완료 보고는 아래 형식으로 작성한다.

```text
1. 작업 요약

2. 자율 진행 판단
   - 사용자 컨펌 없이 진행한 항목
   - 선택한 기본값
   - 요건 변경/대체 설계

3. bootJar / packaged resource 결과
   - clean bootJar 결과
   - jar 내부 D:/logs 검사
   - logback/yml packaged resource 검사

4. runtime harness 정정 결과
   - start/status/diagnostics/closure 정합성
   - 모듈별 port/process/health

5. 파일 로그 runtime 결과
   - 생성 파일
   - transactionGlobalId grep
   - 민감정보 검사
   - 모듈별 로그 분리 확인

6. Trace Boost runtime 결과

7. BAT batch log / Trace Boost 결과

8. ADM operation console runtime 결과

9. EXS timeout/retry runtime 결과

10. 보강/착수 범위 결과

11. 미검증 항목
    - 환경 문제 / 구현 문제 / 설계 문제 / 요청 범위 문제 / 도구 한계
    - 사유
    - 대체 검증
    - 다음 조건

12. 실패 항목
    - 실패 명령
    - 원인
    - 수정 여부
    - 대안

13. Closed

14. Closed with Limitation

15. Still Open

16. Blocked

17. Do Not Rework Without Failure Evidence
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

## 21. 다음 작업 분기

이번 작업 후 분기는 아래 기준으로 판단한다.

```text
runtime closure가 닫히면:
  → 대형 요청 4/6: BAT/Center-Cut 운영 완성 + ADM BAT 관제 + EDU/XYZ/BIZADM 샘플 완성

runtime closure가 또 실패하면:
  → 기능 확장을 멈추지 않되, 실패 원인을 구현/환경으로 분리하고 구현 문제는 즉시 수정

D:/logs 문제가 계속 남으면:
  → logging bootstrap/logback packaged resource를 최우선 수정

BAT runtime만 남으면:
  → BAT auto configuration, listener registration, job wiring, log base path를 최우선 수정
```
