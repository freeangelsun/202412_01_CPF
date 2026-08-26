# CPF Open Issues — 2026-08-26

Source/Static/Contract 범위에서 현재 재현 가능한 미해결 Source FAIL은 0건이다. 아래 2건은 제품 완료에 필수인 **실환경 Acceptance 미검증**이며 `BLOCKED_EXTERNAL`이다.

## BE-01 — Java25 / Gradle 9.1 / VSCode Fresh Sync

- 상태: `BLOCKED_EXTERNAL`
- 현재 환경: Java21; Gradle 9.1 distribution cache 없음; Windows VSCode 없음.
- 재실행: Java25 환경에서 Root clean build/test/publication 및 Generated Domain build 후 VSCode Java/Gradle project clean reload.
- 성공 기준: mandatory Gradle Task/test FAIL 0, VSCode Problems Error 0.

## BE-02 — DB3 / Kafka-free Batch Full Runtime / Browser / Fresh Runtime Replay

- 상태: `BLOCKED_EXTERNAL`
- 현재 환경: PowerShell7/Docker/Oracle/PostgreSQL/MariaDB/Browser 실행환경 없음.
- 재실행: `cpf-tools/verification/tools/run-cpf-required-full-runtime-validation.ps1`
- 성공 기준: DB3 Fresh→Upgrade→Rollback/Reapply, 일반 Batch/Worker/Scheduler/Center-Cut Kafka-free lifecycle, 5 Batch Runtime, 2 Worker Drain/Resume + process kill, lease/fencing, Center-Cut MBR Domain Invocation, UNKNOWN_RESULT + explicit reconcile, Browser E2E, Fresh Runtime Replay가 모두 실제 PASS.
- `FAIL / SKIP / NOT_EXECUTED / UNKNOWN / UNVERIFIED` 필수 항목이 하나라도 남으면 전체 FAIL이다.

**Kafka 기반 Batch Remote Execution 또는 Provider-neutral 대체 Remote Transport를 재도입하지 않는다.**
