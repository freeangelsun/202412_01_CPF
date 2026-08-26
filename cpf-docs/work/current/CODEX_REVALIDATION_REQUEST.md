# CPF Codex Revalidation Request — 2026-08-26

## Exact Source

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_000733.zip`
- Baseline SHA-256: `17778ece0bd2b816f55b0a3140bfb004399bfb9801768e21f28a3fcb300bca16`
- Developer Product Source SHA-256: `3154fbdb54eb32a191df4abf394099550d346338f7bdd6a77a4246329114dd4d`
- Canonical Requirements: 209
- Developer Inventory: 47 items / 45 Source-Static CLOSED / 2 BLOCKED_EXTERNAL
- Developer Final Gate: PASS (Canonical 25/25 + Clean Source + Evidence Semantics 45/45)

과거 Evidence/PASS/Source SHA를 승계하지 말고 위 Source Identity에서 독립 실행한다. Codex-owned status/evidence만 수정하고 DeveloperGPT/QA-owned 컬럼은 수정하지 않는다.

## P0 — Batch Remote Kafka 제거 독립 재검수

사용자 직접 Steering이 과거 Codex 파생 Provider-neutral Remote Transport 요구를 supersede한다.

1. 일반 Batch/Worker/Scheduler/Center-Cut은 Kafka를 사용하지 않아야 한다.
2. `cpf-batch/remote-kafka`, `REMOTE_PARTITION`, `REMOTE_CHUNK`, `REMOTE_STEP`, Remote Worker/Config/Channel/API/SPI/DTO/진단/관리, `BAT_REMOTE_MESSAGE_LEDGER` Current Schema/Query, Remote Test/Publication/Harness의 active Consumer가 0인지 Repository 전체에서 재검수한다.
3. 이름 검색으로 끝내지 말고 Consumer → Bean Wiring → Runtime Config → DB Repo/Query → Gradle/Publication → Harness 호출경로를 확인한다.
4. 일반 Batch DB work-item/claim/lease/fencing, Worker/Scheduler/Center-Cut, official Domain Invocation은 보존되어야 한다.
5. 새 Remote Transport/Broker를 만들지 않는다.
6. 공용 CPF Messaging Kafka가 있으면 별도 Messaging Owner/Consumer로만 평가한다. Batch 때문에 존치한다고 해석하지 않는다.
7. V87/R87 historical bytes는 immutable이어야 하고 V140/R140 3 Vendor removal/recovery를 확인한다.
8. `verify-cpf-batch-no-remote-kafka.py`를 mutation하여 Remote dead surface 하나를 임시 재주입했을 때 FAIL하는지 확인한다.

## P0 — Java25 / Generated / Root Build

Java25 + Gradle9.1에서 Root clean build/test/publication을 실행한다. Scratch Generated Domain과 MBR/EXS build를 실제 수행하고 canonical IA/Consumer를 확인한다. Build/Test FAIL을 waiver/expected 변경으로 숨기지 않는다.

## P0 — Full Runtime

PowerShell7 + Docker 환경에서 공식 Full Runtime을 실행한다. 특히 Batch는 Kafka 없이 Control Plane/Scheduler/Worker×2/Center-Cut/Agent + MBR을 기동해 Drain/Resume, Center-Cut DB claim/lease/fencing, MBR Domain Invocation, Worker kill, lease expiry, UNKNOWN, explicit reconcile, fencing takeover를 실제 검증한다.

DB3 Fresh→Upgrade→Rollback/Reapply, ADM/Backoffice Browser, Fresh Runtime Replay도 필수다.

`SOURCE_FIXED / VERIFICATION_PENDING / BLOCKED_EXTERNAL`은 CLOSED가 아니다. 필수 `SKIP / NOT_EXECUTED / UNKNOWN`을 PASS로 기록하지 않는다.
