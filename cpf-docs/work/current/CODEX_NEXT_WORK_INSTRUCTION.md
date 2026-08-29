# Codex Next Work Instruction — Current

## LONG-TURN MODE — 최상위 비협상

현재 Work Package를 끝내기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 않는다. 계획·중간보고는 중단점이 아니며 같은 turn에서 즉시 작업을 계속한다. 현재 작업을 Source → Owner/Consumer → Test/Verifier → 필요한 Runtime → 오류/복구/회귀 → Evidence → Current Canonical까지 완전히 종결한 뒤 다음 WP를 연다.

Current Product Source Identity: `b162d358b4f127f7c4e3a816f89dcc8ab5fc2f66423ec7c37cbbefd6414fde9e`.

과거 Codex RERUN Evidence는 Current exact Source의 PASS로 승계하지 않는다. Current Source에서 `VERIFICATION_PENDING / IN_PROGRESS / NOT_EXECUTED` Physical WP를 우선순위대로 닫고, 이미 동일 Source·동일 영향범위의 유효 PASS Evidence가 있는 항목만 재사용한다.

## Source 수정 시 VS Code 0/0 강제

Source/Gradle/Catalog/Generator/Config/DB/OpenAPI/Frontend를 하나라도 수정하면 같은 WP 안에서 **Fresh Gradle Import 후 전체 Domain/Module VS Code Problems를 재검증**한다. 완료 조건은 **Error 0 / Warning 0**이다. 하나라도 남으면 즉시 실제 Source/Classpath/Dependency/Generated/Gradle 원인을 수정하고 다시 Fresh Import하여 0/0을 확인한다. 일부 Domain만 확인하거나 오류를 다음 WP로 이월하지 않는다.

환경 때문에 Fresh VS Code 검증을 실행할 수 없는 경우에만 `BLOCKED_EXTERNAL`/`NOT_EXECUTED`로 기록한다. 이때 Requirement별로 환경, 실제 실행 명령, 실제 오류, 부족 prerequisite, 재실행 명령·조건, 기대 결과(Error 0 / Warning 0), Evidence를 남기며 PASS로 올리지 않는다.

## Requirement별 Closure Evidence

각 Requirement/Finding마다 최소 `development_status / verification_status / runtime_status / 변경 Source·Consumer / Test·Verifier / 실제 결과 / 환경 blocker / 재실행 조건 / Evidence / Source Identity`를 개별 기록한다. 공통 Build/DB/Runtime을 공유해도 각 Requirement가 어느 Evidence로 검증되었는지 1:1 trace를 남긴다. `SKIP / NOT_EXECUTED / UNKNOWN / BLOCKED_EXTERNAL`은 완료가 아니다.


## 설계 핑퐁 금지 — Current 방향 유지

동일 Root Cause를 다른 세션이 임의 방식으로 되돌리거나 재설계해 핑퐁하지 않는다. 먼저 Current Source와 기존 Codex Evidence의 설계 방향을 확인하고 **상위 QA Requirement/Architecture와 충돌하지 않으면 그 방향을 유지**한다. 특히 VS Code classpath는 `assembly-only` Public Profile 유지, fake Source/Class 금지, Consumer dependency 우회 금지, **Source가 0개인 Java project의 Gradle configuration/Buildship model을 discovery-driven으로 정상화**하는 Current 방향을 유지한다. 변경이 불가피하면 기존 방식의 실패 근거, 영향범위, 대체 설계와 회귀를 같은 Finding에 기록한다.

## 남은 Physical 우선순위

Java25 Build/Publication + Fresh VS Code 0/0 → DB3 Physical → Unified CLI Windows/Linux → Batch 5-role/2-worker fault/UNKNOWN/reconcile → One-WAS logging/OpenAPI → Frontend/Browser → Performance → Actual Open Git → Same Source Fresh Replay.

Git/HEAD/전체 Local Working Tree를 작업 차단 Gate로 쓰지 않고 다른 세션 변경을 건드리지 않는다. Codex는 `Codex_*` 및 Codex Evidence만 수정하며 DevGPT/QA 상태를 임의 변경하지 않는다.
