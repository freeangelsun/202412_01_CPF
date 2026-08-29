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

## Source 수정 후 VS Code 0/0 및 개별 Evidence

- Source/Gradle/Catalog/Generator/Config/DB/OpenAPI/Frontend 변경 후 **Fresh Gradle Import + 전체 Domain/Module VS Code Problems**를 다시 수행한다.
- 완료 조건은 **Error 0 / Warning 0**이다. 하나라도 발생하면 같은 WP에서 즉시 원인 분석 → Source/Classpath/Dependency/Generated/Gradle 보정 → Fresh Import → 0/0 재확인까지 진행한다.
- VS Code 검증이 환경상 실행 불가하면 `BLOCKED_EXTERNAL`/`NOT_EXECUTED`로 남기고 환경, 실행 명령, 실제 오류, prerequisite, 재실행 조건, 기대 결과, Evidence를 해당 Requirement별로 기록한다.
- 각 Requirement/Finding은 `development_status`, `verification_status`, `runtime_status`, 변경 Source/Consumer, Test/Verifier 명령·실제 결과, blocker, 재실행 조건, Evidence를 개별 기록한다. 공통 실행 로그를 공유해도 Requirement→Evidence trace는 각각 남긴다.
- 미실행/Skip/Unknown/환경 차단을 PASS 또는 완료로 승격하지 않는다.


## 설계 핑퐁 금지 — Current 방향 유지

동일 Root Cause를 다른 세션이 임의 방식으로 되돌리거나 재설계해 핑퐁하지 않는다. 먼저 Current Source와 기존 Codex Evidence의 설계 방향을 확인하고 **상위 QA Requirement/Architecture와 충돌하지 않으면 그 방향을 유지**한다. 특히 VS Code classpath는 `assembly-only` Public Profile 유지, fake Source/Class 금지, Consumer dependency 우회 금지, **Source가 0개인 Java project의 Gradle configuration/Buildship model을 discovery-driven으로 정상화**하는 Current 방향을 유지한다. 변경이 불가피하면 기존 방식의 실패 근거, 영향범위, 대체 설계와 회귀를 같은 Finding에 기록한다.

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
