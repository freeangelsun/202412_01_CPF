# CPF 대형 작업 요청서 — 파일 로그/CBW/BAT/Trace Boost runtime closure + 온보딩 생성기 apply-mode

## 0. 최상위 기준

작업 시작 전 repo 루트의 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 확인한다.

이번 작업은 문서 정리가 아니라 기능 개발과 closure가 목적이다.

중간 문서는 최소만 갱신한다.

* `CPF_STABILIZATION_REPORT.md`
* 기능 구현 매트릭스
* sanitized evidence
* 필요한 README 최소 보강

HTML/PDF/DOCX 정본화, 문장 다듬기, 목차 재정리는 후순위다.

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
CPF_NEW_REQUEST.md 수정 금지
CPF_FINAL_TARGET_REQUIREMENTS.md 수정 금지
문서 포맷 작업만으로 기능 완료 처리 금지
```

## 2. 컨펌 대기 금지 / 자율 진행 기준

이번 작업 중 사용자에게 “계속 진행해도 되냐”, “A/B 중 선택해달라”라고 묻고 멈추지 않는다.

Codex는 아래 원칙으로 자율 진행한다.

```text
비파괴 작업은 질문하지 말고 진행한다.
컴파일 오류, 테스트 오류, smoke 오류는 직접 원인 분석 후 수정한다.
요건이 애매하면 CPF_FINAL_TARGET_REQUIREMENTS.md 기준으로 합리적 기본값을 선택한다.
요건이 CPF 구조와 충돌하면 구현을 중단하지 말고 대체 설계안을 적용하거나 부분 구현으로 봉인한다.
환경 문제는 기능 개발을 멈추는 이유가 아니다.
환경 문제는 미검증으로 봉인하고 source-level/test/script/evidence 작업은 계속 진행한다.
```

단, 아래는 금지한다.

```text
Git commit/push/branch 생성
운영 DB 또는 사용자 데이터 삭제
실제 외부기관 발송/호출
민감정보 원문 기록
```

## 3. 이번 작업의 큰 목표

이번 작업은 작은 보완이 아니다. 아래 6개 축을 한 번에 진행한다.

```text
1. 현재 master 소스 정합성 실패 의심 지점 즉시 해소
2. CBW/CpfWebClient integration 파일 로그 hook 실제 구현
3. PFW 파일 로그 runtime grep closure
4. ADM Trace Boost runtime closure
5. BAT batch 파일 로그 / Batch Trace Boost runtime closure
6. create-domain 생성기 apply-mode / PFW·ADM seed 후보 고도화
```

## 4. 필수 완료 범위 A — 소스 정합성 즉시 보정

### 4.1 BAT 생성자 불일치 해소

현재 `CpfBatchAutoConfiguration`은 `CpfBatchRuntimeListener`를 3개 인자로 생성하는데, listener는 1개 인자 생성자로 보인다.

필수:

```text
CpfBatchRuntimeListener 생성자와 CpfBatchAutoConfiguration 주입 구조를 일치시킨다.
CpfBatchRuntimeListener가 CpfBatchFileLogWriter를 실제로 사용하게 한다.
Job before/after, Step before/after 시점에 batch 파일 로그를 남긴다.
LogPolicyResolver 또는 trace boost policy context가 아직 완성 전이면 nullable/object provider 방식으로 안전하게 처리한다.
```

완료 기준:

```text
:pfw:compileJava 성공
:bat:compileJava 성공
전체 gradlew test 성공
CpfBatchRuntimeListener에서 CpfBatchFileLogWriter.writeBatch 호출 확인
```

### 4.2 `CpfFileLogWriter` 컴파일/마스킹 정합성 점검

필수:

```text
CpfFileLogWriter가 실제 Java 문법상 정상 컴파일되는지 재확인한다.
파일 로그 실패 시 System.err 출력 문자열이 깨지지 않았는지 확인한다.
writeEvent 경로도 민감정보 마스킹 정책을 적용한다.
Batch failureMessageMasked가 실제로 SensitiveDataMasker를 거치도록 보강한다.
```

완료 기준:

```text
파일 로그 writer compile 성공
writeTransaction/writeIntegration/writeEvent 모두 민감정보 원문 차단
파일 로그 실패가 업무 실패로 전파되지 않음
파일 로그 실패 자체는 pfw error/application log에 남음
```

## 5. 필수 완료 범위 B — CBW/CpfWebClient integration 파일 로그 hook

CBW/CpfWebClient/CpfRestClient 같은 공통 outbound wrapper 로그 기준은 아래다.

```text
호출 결과 로그 = 호출한 주제영역 로그
wrapper 내부 정책/전파/마스킹 로그 = PFW 로그
```

예:

```text
ACC가 EXS 호출 → logs/acc/cpf-acc-integration.log
MBR이 EXS 호출 → logs/mbr/cpf-mbr-integration.log
EXS가 외부기관 호출 → logs/exs/cpf-exs-integration.log
PFW wrapper 내부 정책 적용 → logs/pfw/cpf-pfw-application.log 또는 cpf-pfw-transaction.log
```

필수 구현:

```text
CpfWebClient 또는 CpfWebClientConfig에 integration file log filter/hook 구현
요청 시작, 응답 성공, 응답 실패, timeout, exception을 기록
sourceModuleCode는 TransactionContext/module context 기준으로 호출 주체 모듈을 사용
targetModuleCode는 serviceId 또는 endpoint registry 기준으로 추정
apiPath, httpMethod, httpStatus, responseCode, durationMs, failureCode, failureMessageMasked 기록
remote instance response header가 있으면 remoteServerId/remoteInstanceId/remoteHostName 기록
Authorization/Bearer/X-Api-Key/Cookie/Set-Cookie 원문 기록 금지
```

필수 이벤트:

```text
OUTBOUND_REQUEST
OUTBOUND_RESPONSE
OUTBOUND_RESPONSE_ERROR
OUTBOUND_TIMEOUT
OUTBOUND_EXCEPTION
```

완료 기준:

```text
ACC → EXS 성공 호출 시 cpf-acc-integration.log에 기록
ACC → EXS 실패 호출 시 cpf-acc-integration.log에 오류 요약 기록
ACC WAS에 cpf-exs-* 로그가 생성되지 않음
PFW wrapper 내부 로그는 cpf-pfw-*에만 기록
동일 transactionGlobalId로 DB segment와 파일 로그 grep 가능
```

## 6. 필수 완료 범위 C — EXS integration 파일 로그 hook 실제 연결

EXS 원장 DB 저장과 별도로 파일 로그도 남아야 한다.

필수:

```text
EXS 수신/송신 처리에서 cpf-exs-integration.log 기록
EXS REST 송수신 원장 저장 시 동일 transactionGlobalId/segmentId로 파일 로그 기록
EXS 실패/timeout 응답 시 failureCode/failureMessageMasked 기록
institutionCode, endpointCode, externalTransactionId, retryCount, timeoutMs 기록
```

완료 기준:

```text
EXS 처리 로그는 logs/exs/cpf-exs-integration.log에 남음
ACC 쪽 outbound 오류 요약과 EXS 쪽 내부 처리 로그가 transactionGlobalId로 연결됨
EXS 내부 stacktrace를 ACC 로그에 복제하지 않음
```

## 7. 필수 완료 범위 D — 파일 로그 runtime grep closure

이번 작업에서 `file-log-standard`를 반드시 닫는다.

필수 선행:

```text
로컬 서비스 기동 안정화 스크립트 보강
ACC/MBR/ADM/EXS 포트 확인
서비스 기동 실패 시 포트/프로세스/로그 원인 분류
```

필수 실행:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime
```

검증:

```text
ACC 성공 거래 후 logs/acc/cpf-acc-transaction.log 생성
ACC → EXS 실패 후 logs/acc/cpf-acc-integration.log 생성
EXS 처리 후 logs/exs/cpf-exs-integration.log 생성
PFW context 로그 logs/pfw/cpf-pfw-transaction.log 또는 application log 생성
transactionGlobalId grep 성공
failureCode=EXS_TIMEOUT grep 성공
serverId/instanceId/hostName/hostIp/port 존재
민감정보 원문 미노출
```

sanitized evidence:

```text
specs/evidence/20260706_02/file-log-standard-result.sanitized.json
specs/evidence/20260706_02/file-log-grep-summary.sanitized.log
```

만약 runtime 서비스 기동이 계속 실패하면 같은 방식으로 재시도만 반복하지 않는다.

반드시 아래를 기록한다.

```text
환경 문제인지 구현 문제인지 분류
기동 실패한 모듈
포트 상태
프로세스 상태
마지막 app log
대체 source-level 검증
다음 필요 조건
```

단, 기동 실패가 있어도 CBW hook, writer, listener, script, test 개발은 계속 진행한다.

## 8. 필수 완료 범위 E — ADM Trace Boost runtime closure

현재 Trace Boost는 API 1차 구현과 미검증 상태다. 이번 작업에서 runtime을 닫는다.

필수:

```text
/adm/api/log-policies/trace-boost 정책 생성
/runtime-state 조회
/history 조회
disable 또는 rollback
TTL 만료 또는 수동 해제
운영자 사유 필수
정책 변경 audit 저장
root logger 전체 변경 금지
특정 transactionGlobalId 또는 businessTransactionId만 DEBUG/TRACE 적용
파일 로그에 traceBoostPolicyId 기록
민감정보 원문 미노출
```

필수 실행:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime
```

완료 기준:

```text
traceBoost 정책 생성 성공
runtime-state에 현재 정책 표시
history/audit 조회 가능
대상 거래 파일 로그에 traceBoostPolicyId 존재
대상 외 거래는 기본 로그 레벨 유지
root logger level 변경 없음
cleanup 성공
sanitized evidence 생성
```

만약 ADM runtime이 안 뜨면:

```text
환경 문제 / 구현 문제 / 권한 문제 / DB 문제로 분류
ADM health/OpenAPI/auth/login 중 어디서 실패했는지 기록
API source/test는 닫고 runtime만 미검증으로 봉인
같은 방식으로 무한 재시도 금지
```

## 9. 필수 완료 범위 F — BAT batch 파일 로그 / Batch Trace Boost runtime closure

현재 BAT는 writer는 있으나 listener 연결과 runtime 파일 로그가 닫히지 않았다.

필수:

```text
CpfBatchRuntimeListener에서 CpfBatchFileLogWriter 호출
Job before/after 로그
Step before/after 로그
jobName/jobExecutionId/stepName/stepExecutionId/runId/rerunId 기록
workerInstanceId/serverId/instanceId 기록
traceBoostPolicyId/logLevelApplied 기록
Batch failureMessageMasked 마스킹
```

Batch Trace Boost 필수:

```text
특정 jobExecutionId만 DEBUG/TRACE
특정 stepExecutionId만 DEBUG/TRACE
특정 rerunId만 DEBUG/TRACE
실행 종료 시 자동 원복 후보
운영자 사유/감사 후보
다른 job은 기본 level 유지
```

필수 실행:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1 -RunBatRuntime -RequireRuntime
```

완료 기준:

```text
logs/bat/cpf-bat-batch.log 생성
jobName 포함
jobExecutionId 포함
stepExecutionId 포함
traceBoostPolicyId 또는 logLevelApplied 포함
BAT runtime smoke 성공
sanitized evidence 생성
```

만약 BAT runtime이 환경 문제로 실패하면:

```text
bootJar/compile/test/source-level은 닫는다.
runtime만 미검증으로 봉인한다.
BAT listener 연결 자체가 실패하면 구현 문제로 보고 즉시 수정한다.
```

## 10. 필수 완료 범위 G — create-domain 생성기 apply-mode 1차

현재 create-domain은 `build/domain-generator/{module}` 후보 생성까지 완료됐다. 이번 작업에서는 실제 적용 후보 수준을 높인다.

필수:

```text
-DryRun 유지
-Apply 또는 -GeneratePatch mode 추가
기존 모듈 충돌 방지
settings.gradle patch 후보 생성
build.gradle/module build 후보 생성
application-{module}.yml 생성
Controller/Service/Facade/Repository/Mapper/DTO/Validation 생성
Mapper XML 생성
sample CRUD/search/sort/paging/validation 강화
SQL split/Flyway/all_install/smoke_check patch 후보 생성
PFW module registry seed 후보 생성
ADM menu/API/button permission seed 후보 생성
로그 설정 cpf-{moduleCode}-{logType}.log 후보 생성
테스트/smoke 초안 생성
```

중요:

```text
무조건 실제 settings.gradle이나 all_install을 직접 수정하지 않아도 된다.
안전하지 않으면 generated patch 파일로 남긴다.
단, patch 경로와 적용 순서가 명확해야 한다.
```

필수 smoke:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-create-domain.ps1
```

보강 smoke:

```text
임시 모듈을 generated patch 기준으로 별도 build directory에서 compile 가능한지 확인
```

완료 기준:

```text
DryRun 성공
Generate 성공
Patch 후보 생성
PFW/ADM/SQL/log/test/smoke 후보 포함
생성된 후보가 기존 모듈 통째 복사 문자열 치환이 아님
생성 결과 evidence 저장
```

## 11. 필수 완료 범위 H — evidence/report/matrix 정합성 gate 강화

이번 작업에서 가장 중요한 반복 방지 장치다.

필수:

```text
check-feature-evidence.ps1가 report/matrix/evidence/runtime-summary 상태 불일치 탐지
CPF_NEW_REQUEST.md가 변경됐으면 보고서에 '요청서 변경됨'으로 기록
'요청서 미수정'이라고 보고했는데 commit에 변경이 있으면 실패로 기록
manifest에 있는 evidence 파일이 실제 repo에 없으면 실패
runtime summary exitCode 1이면 관련 항목 완료 처리 금지
```

필수 체크:

```text
file-log-grep-summary.sanitized.log manifest 존재 여부와 실제 파일 존재 여부 일치
trace-boost runtime 미검증이면 matrix도 미검증
bat trace boost 부분 구현이면 matrix도 부분 구현
```

완료 기준:

```text
check-feature-evidence.ps1 성공
check-html-docs.ps1 성공
runtime-smoke-summary와 matrix 상태 일치
CPF_STABILIZATION_REPORT.md와 matrix 상태 일치
```

## 12. 보강 범위

가능하면 아래도 같이 진행한다.

```text
ADM 로그 정책 UI 1차
ADM module onboarding status API 1차
CPF logging health API 1차
파일 로그 rolling/retention 설정 초안
파일 로그 경로 권한 오류 진단
prod profile logging guardrail startup validation
XYZ/BIZADM OpenAPI runtime smoke 범위 확장
```

못 닫으면 부분 구현/미검증으로 남긴다.

## 13. 착수 범위

가능하면 착수한다.

```text
Redis/Kafka/MQ real broker smoke 초안
broker unavailable fallback 정책
다중 인스턴스 ACC/EXS 처리 인스턴스 smoke 초안
event/outbox 후보
retention/purge policy seed 후보
```

실 broker가 없으면 mock/embedded 결과를 real broker 완료로 쓰지 않는다.

## 14. 후순위 / 제외 범위

이번 작업에서 완료 강제하지 않는다.

```text
최종 문서 정본화
HTML 디자인 전면 정리
실기관 EXS 호출
전체 Redis/Kafka/MQ 완료
다중 인스턴스 실환경 전체 검증
대규모 성능 benchmark
```

## 15. 필수 검증 명령

compile/test:

```text
.\gradlew.bat :pfw:compileJava :adm:compileJava :exs:compileJava :bat:compileJava --offline --no-daemon --console=plain --rerun-tasks
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

static checks:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

runtime:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-standard-header-e2e.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-composite-transaction-failure-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-transaction-group-failure-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-file-log-standard-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-trace-boost-runtime.ps1 -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-bat-trace-boost-runtime.ps1 -RunBatRuntime -RequireRuntime
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-create-domain.ps1
```

evidence:

```text
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/sync-runtime-smoke-summary.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/export-sanitized-evidence.ps1 -EvidenceDir specs/evidence/20260706_02
```

## 16. 완료 기준

이번 작업 완료는 아래를 만족해야 한다.

```text
BAT compile 생성자 불일치 해소
CpfBatchRuntimeListener가 batch file writer 실제 호출
CpfWebClient/CBW integration file log hook 구현
ACC → EXS 호출 결과가 cpf-acc-integration.log에 기록
EXS 내부 처리 결과가 cpf-exs-integration.log에 기록
ACC WAS에 cpf-exs-* 로그가 생성되지 않음
transactionGlobalId 기준 DB 로그와 파일 로그 grep 연결
file-log-standard runtime smoke 완료
Trace Boost runtime smoke 완료
BAT trace boost runtime smoke 완료 또는 환경 문제 명확 분리
create-domain apply/patch mode 1차 구현
evidence manifest 파일과 실제 파일 경로 일치
runtime summary exitCode와 matrix/report 상태 일치
민감정보 원문 미노출
```

## 17. 완료 불인정 기준

아래는 완료로 인정하지 않는다.

```text
compileJava 실패 또는 생성자 시그니처 불일치
WebClient hook 없이 파일 로그 완료 처리
DB 로그만 있고 파일 로그 grep 없음
file-log-standard 미검증인데 완료 표기
Trace Boost API만 있고 runtime 적용 없음
root logger 전체 변경으로 Trace Boost 구현
TTL/감사/사유 없음
BAT writer만 있고 listener 호출 없음
BAT runtime 파일 로그 없음
create-domain이 후보 생성만 하고 전체 온보딩 완료로 표기
manifest에는 evidence가 있는데 repo에 실제 파일 없음
CPF_NEW_REQUEST.md 변경됐는데 미수정으로 보고
runtime summary 실패인데 관련 항목 완료 표기
```

## 18. 완료 보고 양식

완료 보고는 아래 형식으로 작성한다.

```text
1. 작업 요약

2. 자율 진행 판단
   - 사용자가 컨펌하지 않아도 진행한 항목
   - 선택한 기본값
   - 요건 변경 또는 대체 설계

3. 소스 정합성 보정 결과
   - BAT 생성자/Listener
   - CpfFileLogWriter
   - CpfWebClient hook

4. 파일 로그 runtime closure 결과
   - 생성 파일
   - grep evidence
   - transactionGlobalId
   - 민감정보 검사

5. Trace Boost runtime 결과

6. BAT trace boost 결과

7. create-domain 결과

8. evidence/report/matrix consistency 결과

9. 미검증 항목
   - 환경 문제 / 구현 문제 / 설계 문제 / 요청 범위 문제 / 도구 한계 분류
   - 사유
   - 대체 검증
   - 다음 필요 조건

10. 실패 항목
    - 실패 명령
    - 원인
    - 수정 여부
    - 대안

11. Closed

12. Closed with Limitation

13. Still Open

14. Blocked

15. Do Not Rework Without Failure Evidence
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

## 19. 다음 작업 분기

이번 작업 후 분기는 아래 기준으로 판단한다.

```text
파일 로그/Trace Boost/BAT runtime이 닫히면:
  → ADM 로그 정책 UI, onboarding status UI, EXS timeout/retry/circuit breaker로 이동

파일 로그 runtime만 계속 미검증이면:
  → 서비스 기동 안정화와 로그 경로/권한 진단을 먼저 닫고 다른 기능은 계속 진행

BAT runtime이 환경 문제면:
  → BAT source/test/bootJar는 닫고 다중 worker는 후순위로 이동

create-domain patch mode가 닫히면:
  → 실제 LON 같은 신규 주제영역 샘플 생성 runtime smoke로 이동

evidence consistency가 계속 실패하면:
  → 새 기능보다 closure gate 보정 우선
```
