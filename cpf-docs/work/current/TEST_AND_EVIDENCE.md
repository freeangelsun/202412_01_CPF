# CPF Developer GPT Test & Evidence — 2026-08-26

## Source Identity

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260826_000733.zip`
- Baseline ZIP SHA-256: `17778ece0bd2b816f55b0a3140bfb004399bfb9801768e21f28a3fcb300bca16`
- Product Source SHA-256: `3154fbdb54eb32a191df4abf394099550d346338f7bdd6a77a4246329114dd4d`
- Source files: 8,439
- Canonical Requirement: 209

## Execution Result

| 검증 | 실제 결과 | 판정 |
|---|---:|---|
| Canonical verifier | 25/25, failed 0 | PASS |
| Canonical Development Final Gate | Canonical 25/25 + Clean Source + Evidence Semantics 45/45, failed 0 | PASS |
| Fresh Replay | baseline ZIP → Overlay → Delete Manifest, canonical files 10,234 / missing 0 / extra 0 / hash mismatch 0; replay Final Gate RC=0 | PASS |
| Verification pytest | 78 passed | PASS |
| Testing Tools split | 386 passed / 22 env skip / 2 subtests | PASS_WITH_ENV_SKIPS |
| DB pytest | 125 passed / 2 env skip | PASS_WITH_ENV_SKIPS |
| Release + Generator + Supply | 99 passed / 10 env skip / 6 subtests | PASS_WITH_ENV_SKIPS |
| Targeted runtime/source contracts | 96 passed + 7 subtests | PASS |
| Kafka-free Batch Harness/Full Validation contracts | 12 passed | PASS |
| Context Architecture substitute runtime | failures 0 | PASS |
| Windows Path | max relative 199 / failures 0 | PASS |
| Current Final | failures 0 | PASS |
| Requirement projection | 209 developer requirements / 30,605 logical requirements | PASS |
| NXT3 stage-equivalent | 23 stages individually PASS | PASS_STAGE_EQUIVALENT_ONLY |

Environment SKIP은 PASS에 합산하지 않는다. NXT3 monolithic wrapper는 assistant timeout으로 정상 종료코드를 얻지 못했으므로 wrapper 자체 PASS라고 기록하지 않는다.

## Batch Kafka Remote Execution Closure

Repository 전체 Consumer/Bean/Runtime/DB/Publication/Harness를 역추적한 뒤 Remote Kafka 전용 Surface만 제거했다. 일반 Batch/Worker/Scheduler/Center-Cut의 DB work-item/claim/lease/fencing과 Domain Invocation은 보존했다. 새 Remote Transport/Broker는 추가하지 않았다. 공용 Messaging Kafka는 별도 Messaging Owner/Consumer와 분리했다.

V87/R87은 immutable history로 보존하고 V140/R140으로 Oracle/PostgreSQL/MariaDB Current removal/recovery를 제공한다.

Kafka-free runtime harness는 Control Plane + Scheduler + Worker×2 + Center-Cut + Agent + MBR을 대상으로 Drain/Resume, DB claim/lease/fencing, Domain Invocation, process kill, lease expiry, UNKNOWN_RESULT, blind retry 금지, explicit reconcile, fencing 증가를 검증하도록 작성했다. 실환경 실행은 아래 BLOCKED_EXTERNAL 때문에 미검증이다.

## BLOCKED_EXTERNAL

### BE-01 Java25/Gradle/VSCode
- 현재 환경: Java21, Gradle 9.1 distribution cache 없음, Windows VSCode 없음.
- 필요: Java25 clean build/test/publication, Generated Domain build, Fresh VSCode Gradle import/sync, Problems Error 0.
- 미실행 결과를 PASS로 기록하지 않는다.

### BE-02 DB3/Full Runtime/Browser
- 현재 환경: `pwsh` 없음, Docker daemon 없음, DB3/Browser 실행 불가.
- 필요: Oracle/PostgreSQL/MariaDB Fresh→Upgrade→Rollback/Reapply, 5 Batch runtimes, 2 Worker, Center-Cut Domain Invocation, kill/UNKNOWN/recovery/reconcile/fencing, ADM/Backoffice Browser, Fresh Runtime Replay.
- 필수 SKIP/NOT_EXECUTED/UNKNOWN이 있으면 실패다.

## Evidence Files

- `cpf-docs/work/evidence/developer-gpt/current/CPF_DEVELOPER_GPT_EXECUTION_SUMMARY.json`
- `cpf-docs/work/evidence/developer-gpt/current/development-final-gate.log`
- `cpf-docs/work/evidence/developer-gpt/current/evidence-semantics.log`
- `cpf-docs/work/evidence/developer-gpt/current/source-state.json`
- `cpf-docs/work/evidence/developer-gpt/current/current-final.json`
- `cpf-docs/work/evidence/developer-gpt/current/batch-no-remote-kafka.log`
- `cpf-docs/work/evidence/developer-gpt/current/windows-path.log`
- `cpf-docs/work/evidence/developer-gpt/current/requirement-projection.log`
- `cpf-docs/work/evidence/developer-gpt/current/clean-source.log`
- `cpf-docs/work/evidence/developer-gpt/current/executions/EXEC-01.json` ~ `EXEC-09.json`
