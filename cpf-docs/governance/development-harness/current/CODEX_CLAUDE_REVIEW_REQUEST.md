# Codex / Claude Independent Review Request — Development Harness Current

**Codex와 Claude는 동일 `INDEPENDENT_REVIEWER` 역할이다.** 같은 Acceptance, 같은 권한, 같은 Evidence schema를 사용한다.

**LONG-TURN MODE:** 현재 Work Package를 끝내기 전에 자의로 turn을 종료하거나 다음 진행 여부를 묻지 않는다. 계획·중간보고는 중단점이 아니며 같은 turn에서 작업을 계속한다. 현재 하던 작업을 Source→Consumer→Test/Runtime→Evidence→Current Harness 원장까지 완전히 종결한 뒤 남은 실행 여력으로 고위험 독립검증을 진행한다. Repository 전체를 이유 없이 처음부터 반복하지 않는다. 같은 Root Cause와 Build/DB/Runtime은 묶는다. Git/HEAD/전체 Local Working Tree 상태를 작업 차단 Gate로 쓰지 않고 다른 세션 변경을 건드리지 않는다.

## 필수 순서
1. `CPF_DEVELOPMENT_HARNESS.md`와 Core Policy/Product Contract 읽기.
2. `current/CURRENT_DEVELOPMENT_STATUS.csv`에서 자기 대상 WP 식별.
3. 변경 전 영향도 작성.
4. Source/Consumer/Test/Config/DB/Generator/OpenAPI/Frontend/Runtime 영향 검수·필요 보완.
5. **Source 수정 시 즉시 Fresh VS Code Zero-Diagnostic Gate**: Java25/Gradle Fresh import/reload 후 수정 영향이 닿는 모든 Domain/Module의 Problems JSON을 생성하여 `Error=0 / Warning=0`을 확인한다. 하나라도 발생하면 같은 WP에서 즉시 수정·재검증하며, suppression/waiver/나중 처리 금지. 실행 불가하면 PASS 금지.
6. 최대강도 Test/Runtime. 환경 부족은 BLOCKED_EXTERNAL이며 smoke 대체 금지.
7. `ROLE_EXECUTION_LEDGER.csv`의 `INDEPENDENT_REVIEWER` 해당 행에 실제 수행 여부/상태/내용/완료·미완료 사유/명령/환경/exit/Evidence/SourceIdentity/impact/regression/runtime을 기록.
8. PASS는 모든 근거가 채워졌을 때만.
9. 신규 결함은 Root Cause Work Item으로 등록하고 False Green 금지.
10. QA 영역은 수정하지 않는다.


## Source 수정 시 필수 Zero-Diagnostic Closure

Source를 한 줄이라도 수정하면 영향 Domain/Module/Consumer를 확정하고 Java 25 + Gradle Fresh import/reload 후 **영향 범위 전체 VS Code Problems Error=0 / Warning=0**을 새 Evidence로 남긴다. 오류/경고가 생기면 현재 WP에서 즉시 Root Cause와 동일 원인 전체를 수정하고 Source/Consumer/Test/Generator/Config/Runtime까지 재검증한다. suppression/waiver/expected 변경/검사 제외로 숨기지 않는다. 추가 Source 수정은 이전 Problems PASS를 무효화한다. 실행 환경 부족은 `BLOCKED_EXTERNAL`/`VERIFICATION_PENDING`이며 PASS/CLOSED가 아니다.

## Test / Runtime 기록

선택한 검증 범위의 강도를 낮추지 않는다. `ROLE_EXECUTION_LEDGER.csv`와 `TEST_EXECUTION_LEDGER.csv`에 수행여부, 실제 명령, 환경, 시작/종료, ExitCode, 관찰 결과, Evidence/SHA, 완료사유 또는 미완료사유를 기록한다. Test/Runtime 미실행을 Source 완료로 덮지 않는다.

## Windows / Linux

Standalone/CLI/운영 Runtime 영향이 있으면 Windows PowerShell과 Linux shell 경로를 모두 구현·검증하고 의미적 parity를 확인한다. 한쪽 미구현/미검증은 완료가 아니다.
