# CPF Codex / Claude 작업 요청 공통 Template

## LONG-TURN MODE — 최상위 비협상

현재 Work Package를 끝내기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 않는다. 계획·중간보고는 중단점이 아니며 같은 turn에서 즉시 작업을 계속한다. 서버 측 사용량 제한을 우회하려는 지침이 아니다.

## 실행 순서

1. 현재 에이전트가 이미 수행 중인 작업이 있으면 신규 업무보다 먼저 완전히 종결한다.
2. 현재 작업 종결 후 남은 크레딧으로 아래 필수 독립검증 중 필요한 항목만 우선한다.
3. 기존 미완료 전체/Repository 전체 전수 재검수는 하지 않는다.
4. 같은 Root Cause/Build/DB/Runtime은 공통 실행으로 묶는다.
5. 크레딧이 줄어들면 신규 WP를 열지 말고 진행 중 WP를 Source/Test/Runtime/Evidence/문서까지 완결한다.
6. ADM/Backoffice/Frontend/Browser는 가장 마지막이다.

## 기본 필수 독립검증 우선순위

- Java25 Build/Compile/Dependency
- Logging 실제 File↔DB↔Transaction/Timeline
- DB3 Physical Runtime
- Batch 5-role/2-worker/kill/takeover/fencing/UNKNOWN/reconcile
- Generator/Generated Domain idempotency
- Performance signed source identity
- Open Git Actual Fresh Release 핵심 Golden Path
- ADM/Frontend 최후순위

## 병행 세션

Git/HEAD/전체 Local Working Tree/전체 Source Identity를 작업 Gate로 사용하지 않는다. 다른 세션 변경을 조사·복구·초기화하지 않고 자신의 할당 범위만 처리한다.


## Source 수정 시 필수 Zero-Diagnostic Closure

Source를 한 줄이라도 수정하면 영향 Domain/Module/Consumer를 확정하고 Java 25 + Gradle Fresh import/reload 후 **영향 범위 전체 VS Code Problems Error=0 / Warning=0**을 새 Evidence로 남긴다. 오류/경고가 생기면 현재 WP에서 즉시 Root Cause와 동일 원인 전체를 수정하고 Source/Consumer/Test/Generator/Config/Runtime까지 재검증한다. suppression/waiver/expected 변경/검사 제외로 숨기지 않는다. 추가 Source 수정은 이전 Problems PASS를 무효화한다. 실행 환경 부족은 `BLOCKED_EXTERNAL`/`VERIFICATION_PENDING`이며 PASS/CLOSED가 아니다.

## Test / Runtime 기록

선택한 검증 범위의 강도를 낮추지 않는다. `ROLE_EXECUTION_LEDGER.csv`와 `TEST_EXECUTION_LEDGER.csv`에 수행여부, 실제 명령, 환경, 시작/종료, ExitCode, 관찰 결과, Evidence/SHA, 완료사유 또는 미완료사유를 기록한다. Test/Runtime 미실행을 Source 완료로 덮지 않는다.

## Windows / Linux

Standalone/CLI/운영 Runtime 영향이 있으면 Windows PowerShell과 Linux shell 경로를 모두 구현·검증하고 의미적 parity를 확인한다. 한쪽 미구현/미검증은 완료가 아니다.
