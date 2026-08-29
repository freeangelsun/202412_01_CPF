# CPF Open Issues — 2026-08-27

현재 컨테이너에서 재현 가능한 Source/Static/Contract FAIL은 0건이다. 아래는 제품 최종 완료에 필요한 **실환경 Acceptance 미검증**이다.

## BE-01 — Java25 / Gradle9.1 / VSCode Fresh Sync

- 상태: `미검증`
- 현재 환경: Java21, Gradle9.1 distribution cache 없음, Windows VSCode 없음.
- 재실행: Java25 Root clean build/test/publication/SBOM + Generated Domain build 후 VSCode Java/Gradle clean reload.
- 성공 기준: mandatory Gradle/Test FAIL 0, VSCode Problems **Error 0 / Warning 0**, warning disable/suppression false green 0.

## BE-02 — DB3 / Batch / One-WAS / Logging / Browser / Performance

- 상태: `미검증`
- 필수: Oracle/PostgreSQL/MariaDB Fresh→Upgrade→Rollback/Reapply, Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile, One-WAS, real File↔DB↔Trace↔ADM logging correlation, Runtime OpenAPI, ADM/Backoffice Browser, signed Performance Live, Fresh Replay.
- 성공 기준: `FAIL=0 / mandatory SKIP_ENV=0 / mandatory NOT_EXECUTED=0 / unresolved UNKNOWN=0 / Source drift=0 / Managed drift=0 / ExitCode=0`.

## BE-03 — Open Git Actual Fresh Binary Release / Golden Path

- 상태: `미검증`
- Projection/Default-Deny/Source leak/CLI contract 회귀는 PASS.
- 필수: Java25 Fresh framework publication, Maven-folder repository, JAR/POM/sources/javadoc/checksum/SBOM, fresh generated domains, EDU/Backoffice, fresh remote clone-equivalent workspace에서 bootstrap/build/test/start/health/operation/stop/reset/rerun.

위 미검증 중 하나라도 남으면 전체 QA 완료가 아니다.
