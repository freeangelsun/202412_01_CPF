# CPF OPEN ISSUES — Current

Source/Static Rework에서 현재 환경으로 실행 가능한 FAIL은 0이다. 아래는 QA 전체 완료 전 반드시 실제 환경에서 닫아야 하는 Physical Acceptance다.

1. Java25 Root Build/Test/Publication/SBOM.
2. Fresh Windows Java25 Gradle Import + 전체 Domain/Module VS Code Error 0 / Warning 0. 잔여 4건 재개발 Source/contract는 완료했으나 실제 Problems 0/0 실측 전까지 미검증.

3. Oracle/PostgreSQL/MariaDB actual DB3 Full Lifecycle.
4. Windows/Linux Unified CLI actual lifecycle/UTF-8/path/prerequisite negative.
5. Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile.
6. One-WAS transaction + rollback-surviving DB log + File/DB/Segment/Timeline correlation + Runtime OpenAPI.
7. ADM/Backoffice Frontend/Browser E2E/a11y/error-state.
8. Performance live/load/soak.
9. Actual Open Git Fresh Binary/Source Release + public CLI lifecycle + leakage 0.
10. Full Runtime mandatory FAIL/SKIP_ENV/NOT_EXECUTED/UNKNOWN/drift/mojibake 0. Full Runtime child 21개 UTF-8 boundary와 integrated-log localized ErrorRecord 안정화 Source/contract는 완료했으나 실제 Windows Full Runtime log mojibake 0 실측 전까지 미검증.

11. Same Source Fresh Replay + Codex/Claude independent current-source verification.

위 항목은 Source/Contract PASS로 대체하지 않는다.
