# OPEN ISSUES — 2026-08-25

현재 Source/Static/Contract/Substitute 범위에서 재현된 미해결 Source FAIL은 0건이다. 아래 항목은 Source 수정 미완료가 아니라 **실환경 필수 검증 미실행**으로 남는다.

## BLOCKED_EXTERNAL-01 — Java25/Gradle Full Build

- 상태: `미검증`
- 이유: Assistant 환경은 Java21만 있고 Gradle 9.1.0 배포본을 확보하지 못함.
- 재실행: 프로젝트 Root의 Windows PowerShell 7 + Java25에서 `.\gradlew.bat clean build --continue --stacktrace`
- 성공 기준: compile/test/publication mandatory Task FAIL 0.

## BLOCKED_EXTERNAL-02 — Full Runtime / DB3 / Docker / Browser

- 상태: `미검증`
- 이유: Assistant 환경에 `pwsh` 및 Docker daemon 없음.
- 재실행: `.\cpf-tools\verification\tools\run-cpf-required-full-runtime-validation.ps1` 공식 명령.
- 성공 기준: Oracle/PostgreSQL/MariaDB lifecycle, multi-instance/process-kill/recovery, Kafka 2-worker, QA39 fault, One-WAS, Runtime OpenAPI, Browser E2E, Performance, Open Git/Fresh consumer 모두 성공하며 `FAIL=0 / SKIP_ENV=0 / NOT_EXECUTED=0 / UNVERIFIED=0`.

이 두 항목이 실제 PASS가 되기 전에는 QA-ready 또는 최종 완료로 판정하지 않는다.
