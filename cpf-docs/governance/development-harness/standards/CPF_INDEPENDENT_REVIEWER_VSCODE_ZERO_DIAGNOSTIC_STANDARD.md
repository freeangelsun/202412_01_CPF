# Codex / Claude Source 수정 시 VS Code Zero-Diagnostic 표준

Codex와 Claude는 동일 `INDEPENDENT_REVIEWER`다. Source를 한 줄이라도 수정하면 수정 전 VS Code PASS 근거는 해당 영향범위에서 무효다.

1. 변경 전 영향 Domain/Module/Consumer를 확정한다.
2. Java 25 + Gradle 환경에서 Fresh import/reload를 수행한다.
3. **변경 영향이 닿는 모든 Domain/Module**의 VS Code Problems JSON을 새로 추출한다.
4. `Error=0 AND Warning=0`만 PASS다.
5. 하나라도 발생하면 현재 WP에서 즉시 Root Cause를 분석하고 동일 원인 전체를 수정한다. 별도 후속 WP로 미루거나 suppression/waiver/expected 변경/검사 제외로 숨기지 않는다.
6. Source를 추가 수정할 때마다 Fresh import와 Problems JSON을 다시 만든다.
7. 실행 환경이 없으면 `BLOCKED_EXTERNAL` 또는 `VERIFICATION_PENDING`; PASS/CLOSED 금지.
8. Role Ledger에는 `source_modified=true`, `vscode_fresh_import=true`, `vscode_scope`, `vscode_problems_json`, `vscode_error_count=0`, `vscode_warning_count=0`, Source Identity와 실제 명령/환경/ExitCode를 기록한다.

이 Gate는 compile/test를 대체하지 않는다. Root Build/Test/Runtime의 추가 Gate다.
