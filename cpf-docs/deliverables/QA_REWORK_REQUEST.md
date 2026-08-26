# QA 재개발/재검수 요청 준비 — 2026-08-26

Developer GPT가 현재 환경에서 수행 가능한 Source/Static/Contract 개발·검증은 완료했다. Canonical Inventory 47건 중 45건 Source/Static Closure가 완료되었고 2건 필수 실환경 Acceptance가 `BLOCKED_EXTERNAL`이다.

- Baseline ZIP SHA-256: `17778ece0bd2b816f55b0a3140bfb004399bfb9801768e21f28a3fcb300bca16`
- Product Source SHA-256: `3154fbdb54eb32a191df4abf394099550d346338f7bdd6a77a4246329114dd4d`
- Canonical Requirements: 209
- Canonical verifier: 25/25 PASS
- Batch Kafka Remote Execution: 제거 Gate PASS; 일반 Batch/Worker/Scheduler/Center-Cut Kafka-free 경로 보존

QA 최종 재검수는 아래 실환경 Evidence가 모두 실제 PASS한 뒤 요청한다.

- Java25 Root clean build/test/publication + Generated Domain
- Windows VSCode Fresh Gradle Sync / Problems Error 0
- Oracle/PostgreSQL/MariaDB Fresh→Upgrade→Rollback/Reapply
- Kafka-free 5 Batch Runtime + Worker×2 + Center-Cut Domain Invocation + Kill/UNKNOWN/Recovery/Reconcile/Fencing
- ADM/Backoffice Browser E2E
- Fresh Runtime Replay와 Source Identity 일치
- Codex 독립 재검수

필수 `BLOCKED_EXTERNAL / 미검증 / FAIL / SKIP / NOT_EXECUTED / UNKNOWN`이 남으면 전체 완료로 판정하지 않는다.
