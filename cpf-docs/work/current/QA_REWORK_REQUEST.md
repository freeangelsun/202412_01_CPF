# QA 재개발/재검수 요청 준비 — 2026-08-27 — C 개발/QA 관리_1_8

현재 Developer Source/Static 보정은 진행됐지만 **QA 최종 재검수 요청 단계가 아니다.** Physical mandatory Acceptance와 Fresh Replay가 남아 있다.

- Current Product Source Identity: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af`
- Canonical Product Requirements: `208`
- Developer Inventory: `164` rows
- NXT3/Static/Contract current reproduced FAIL: `0`
- User previous Full Runtime: `PASS 129 / FAIL 18 / SKIP_ENV 1 / NOT_EXECUTED 7 / RC 1` — execution Source가 달라 현재 PASS로 승계하지 않음.

QA 재검수 전 필수 Evidence:

1. Java25/Gradle9.1 Root Build/Test/Publication/SBOM + Generated Domain.
2. Fresh VSCode Error 0 / Warning 0.
3. Oracle/PostgreSQL/MariaDB physical lifecycle + fault/recovery/cleanup.
4. Batch 5-role/Worker×2 kill/takeover/fencing/UNKNOWN/reconcile.
5. One-WAS + File/DB/Transaction/Timeline logging correlation.
6. Runtime OpenAPI + Frontend + Browser E2E/a11y/error statuses.
7. signed Performance Live + load/soak.
8. Actual Open Git Fresh Binary Release/Golden Path.
9. Source/Managed drift 0.
10. 동일 Source Fresh Replay.
11. Codex continuation 독립검증에서 신규/재발 필수 Finding 0 또는 모두 수정/재검증.

필수 `FAIL / SKIP_ENV / NOT_EXECUTED / UNKNOWN / drift / Evidence mismatch`가 하나라도 남으면 QA 완료로 올리지 않는다.
