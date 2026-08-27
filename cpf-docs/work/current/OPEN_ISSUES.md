# CPF Open Issues — 2026-08-27 — C 개발/QA 관리_1_8

현재 DevGPT 환경에서 재현 가능한 Python/Static/Contract/NXT3 FAIL은 보정 후 **0건**이다. 다만 아래 Physical Acceptance가 실제 실행되지 않았으므로 제품 전체 QA 완료가 아니다.

## BE-01 — Java25 / Gradle9.1 / VSCode Fresh Sync

- 상태: `BLOCKED_EXTERNAL / 미검증`
- 현재 DevGPT 환경: Linux, Java21, Gradle9.1 distribution cache 없음, 외부망 차단, Windows VSCode 없음.
- 현재 Source: `b41abc892e6652ef8461ece1b3daa1057acfbf153185fe8e67ea70a6e20de4af` / 8,340 product files.
- 필수: Java25 Root clean build/test/publication/SBOM + Generated Domain build + Fresh VSCode Gradle/JDT reload.
- 성공: Gradle/Test FAIL 0, VSCode Problems **Error 0 / Warning 0**, warning suppression/disable false green 0.

## BE-02 — DB3 / Batch / One-WAS / Logging / Browser / Performance

- 상태: `BLOCKED_EXTERNAL / 미검증`
- 사용자 이전 Runtime은 PASS129/FAIL18/SKIP_ENV1/NOT_EXECUTED7이었고 execution Source가 현재 Source와 다르다.
- DB 정적/계약 보정은 228 passed / 2 env skipped, Oracle sequence projection 보정까지 완료했지만 DB3 physical 재실행은 필요하다.
- Batch 5-role Shell 20/Profile 15는 정적 PASS이나 Worker×2 kill/takeover/fencing/UNKNOWN/reconcile는 실제 환경 재실행 필요.
- One-WAS, real File↔DB↔Timeline logging, Runtime OpenAPI, Browser, Performance load/soak는 현재 보정 Source에서 미실행.
- 성공: `FAIL=0 / mandatory SKIP_ENV=0 / mandatory NOT_EXECUTED=0 / unresolved UNKNOWN=0 / Source drift=0 / Managed drift=0 / ExitCode=0`.

## BE-03 — Actual Open Git Fresh Binary Release / Golden Path

- 상태: `미검증`
- Contract/Projection/Default-Deny 회귀는 53 passed로 PASS.
- 필수: Java25 Fresh framework publication, Maven-folder repository, JAR/POM/sources/javadoc/checksum/SBOM, fresh generated domains, EDU/Backoffice, fresh clone-equivalent bootstrap/build/test/start/health/operation/stop/reset/rerun.

## BE-04 — Fresh Replay / Final Evidence

- 상태: `미검증`
- 강화된 Required Full Runtime Runner는 VSCode 0/0, load/soak, Actual Open Git Fresh Release, mandatory stage completeness, managed path diff, Fresh Replay를 fail-closed로 요구하도록 보정했다.
- 첫 Full Runtime PASS와 **동일 Source**의 Fresh Replay가 모두 PASS해야 전체 완료 후보가 된다.

위 항목 중 하나라도 남으면 전체 QA 완료가 아니다.
