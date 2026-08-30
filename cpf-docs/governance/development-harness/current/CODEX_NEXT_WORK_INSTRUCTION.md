# Codex Next Work Instruction — Current

Current Product Source Identity: `1a39531bcd1f0b1c82bbc6f330ab7b8256fc9132f62676ed1c8887ae42040839`.

과거 Codex RERUN Evidence는 Current-only cleanup으로 제거됐다. 처음부터 Source 전수를 반복하지 말고, Current exact Source에서 `VERIFICATION_PENDING / IN_PROGRESS / NOT_EXECUTED` Physical WP를 순서대로 닫는다.

우선순위: Java25 Build/Publication → DB3 Physical → Unified CLI Windows/Linux → Batch fault/UNKNOWN → One-WAS logging → Frontend/Browser → Performance → Actual Open Git → Same Source Fresh Replay.

각 WP는 Source → Consumer → Test/Verifier → Runtime → Evidence → Current Canonical까지 완결한다. Codex_* 및 Codex Evidence만 수정하며 DevGPT/QA 상태를 임의 변경하지 않는다.


## Source 수정 시 필수 Zero-Diagnostic Closure

Source를 한 줄이라도 수정하면 영향 Domain/Module/Consumer를 확정하고 Java 25 + Gradle Fresh import/reload 후 **영향 범위 전체 VS Code Problems Error=0 / Warning=0**을 새 Evidence로 남긴다. 오류/경고가 생기면 현재 WP에서 즉시 Root Cause와 동일 원인 전체를 수정하고 Source/Consumer/Test/Generator/Config/Runtime까지 재검증한다. suppression/waiver/expected 변경/검사 제외로 숨기지 않는다. 추가 Source 수정은 이전 Problems PASS를 무효화한다. 실행 환경 부족은 `BLOCKED_EXTERNAL`/`VERIFICATION_PENDING`이며 PASS/CLOSED가 아니다.

## Test / Runtime 기록

선택한 검증 범위의 강도를 낮추지 않는다. `ROLE_EXECUTION_LEDGER.csv`와 `TEST_EXECUTION_LEDGER.csv`에 수행여부, 실제 명령, 환경, 시작/종료, ExitCode, 관찰 결과, Evidence/SHA, 완료사유 또는 미완료사유를 기록한다. Test/Runtime 미실행을 Source 완료로 덮지 않는다.

## Windows / Linux

Standalone/CLI/운영 Runtime 영향이 있으면 Windows PowerShell과 Linux shell 경로를 모두 구현·검증하고 의미적 parity를 확인한다. 한쪽 미구현/미검증은 완료가 아니다.
