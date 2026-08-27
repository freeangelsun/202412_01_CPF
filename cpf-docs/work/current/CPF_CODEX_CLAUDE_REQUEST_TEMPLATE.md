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
