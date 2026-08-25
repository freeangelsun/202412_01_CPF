# TEST AND EVIDENCE — Developer GPT — 2026-08-25

## 1. 기준 Source

- Baseline ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260825_121103.zip`
- Baseline ZIP SHA-256: `d2e89aba1841a4387a473610db905415f8565fcf09d06a56a8afa3a1b33a3a48`
- Baseline Product Source Identity: `7c7b806d4284a5a655731cff60b3cce214cdcec9f73ce489b9f3f96bf9bac809`
- Current Product Source SHA-256: `c79be31a71c15c02665d56e29c0f51244c91ab3894183775ce311cde3dbf40df`
- Current Product Source files: `8439`
- Current Product Source bytes: `49,558,021`
- Git exact SHA: Source ZIP에 `.git`이 없으므로 `UNVERIFIED_SOURCE_ZIP_HAS_NO_DOT_GIT`

과거 PASS Evidence를 현재 Source의 PASS로 승계하지 않았다. 아래 결과는 이번 Current Source에서 실제 재실행한 결과만 기록한다.

## 2. 최종 정적·계약·Substitute 실행 결과

| 검증 | 실제 결과 | 판정 |
|---|---:|---|
| Canonical verifier | 24/24 | PASS |
| NXT3 stage-equivalent commands | 23/23 | PASS |
| Windows path | max relative 199 / >200 0 | PASS |
| Testing tools | 385 passed / 22 environment skipped / 2 subtests passed | FAIL 0 |
| Verification tests | 73 passed / 3 subtests passed | FAIL 0 |
| DB verification | 86 passed | FAIL 0 |
| DB tests | 125 passed / 2 environment skipped | FAIL 0 |
| Generator verification | 46 passed / 10 environment skipped / 6 subtests passed | FAIL 0 |
| Runtime tool contracts | 76 passed / 2 environment skipped / 7 subtests passed | FAIL 0 |
| Open Git | 17 passed | FAIL 0 |
| Public Release | 22 passed | FAIL 0 |
| Release tools | 14 passed | FAIL 0 |
| Security | 8 passed | FAIL 0 |
| OpenAPI | 21 passed | FAIL 0 |
| Audit runtime contract | 1 passed | FAIL 0 |
| Supply Chain | 3 passed | FAIL 0 |
| Docker lifecycle harness contracts | 6 passed | FAIL 0 |
| Frontend Full Compile | ADM 469 TS / Backoffice 18 TS | PASS |
| Frontend Workflow Runtime Harness | 14 checks | PASS |
| Frontend API Runtime Harness | 2 surfaces / System6 protection | PASS |
| Frontend Golden Path | live-source hashed | PASS |
| Frontend Node+Chromium substitute | ADM + Backoffice boundary | PASS |
| Java21 substitute | compile + real unit runner 2 + runtime harness / class major 65 | PASS |

Environment skip은 성공으로 합산하지 않았다. 현재 실행된 Source/Static/Contract/Substitute 검증에서는 관측된 FAIL이 0건이다.

## 3. 이번 결함 수렴에서 닫은 주요 Root Cause

- Codex Preflight PowerShell parser/current-path drift
- NXT3 config/comment/retention/layout/garbage/hygiene drift
- Context standalone compile source-set 누락
- Backoffice Approval CAS 계약 Test/Consumer drift
- V138/V139 DB3 current-edge/logical DB/checksum/MBW manifest drift
- BAT/CEC `CENTER_CUT_RUNNER` canonical role 및 역사 migration 검증 drift
- Batch 2-worker prerequisite/diagnostic/ledger harness drift
- Generator lifecycle v5/restore/signature/current generated output drift
- Frontend sandbox/OpenAPI/runtime-path 및 Approval/Maintenance consumer drift
- Supply-chain `center-cut-runtime` artifact catalog 누락
- Open Git canonical bin template/dispatcher 누락
- Network policy classifier 중복 및 zero-footprint dependency closure
- Windows 200자 초과 Codex Evidence 경로 alias currentization
- Docker Runtime Harness의 prerequisite auto-start/readiness/test-owned cleanup
- Customer Shared Library Generator: create/attach/sync/verify + opt-in Domain dependency + 한글 주석/도움말
- Java21 substitute harness가 현재 Retention/Approval Controller 계약을 따라가지 못하던 compile drift

## 4. 실제 실행 명령 대표

`PYTHONDONTWRITEBYTECODE=1 python3 cpf-tools/verification/tools/run-cpf-canonical-verifiers.py --root .`

`PYTHONDONTWRITEBYTECODE=1 python3 -m pytest -q -p no:cacheprovider cpf-tools/verification/tests`

`PYTHONDONTWRITEBYTECODE=1 python3 -m pytest -q cpf-tools/db/verification/tests`

`PYTHONDONTWRITEBYTECODE=1 python3 -m pytest -q cpf-tools/db/tests`

`PYTHONDONTWRITEBYTECODE=1 python3 -m pytest -q cpf-tools/generator/verification/tests`

`PYTHONDONTWRITEBYTECODE=1 python3 -m pytest -q cpf-tools/runtime/tools/tests`

`python3 cpf-tools/verification/frontend-full-compile/run-frontend-full-compile.py --root .`

`python3 cpf-tools/verification/frontend-api-runtime/run-frontend-api-runtime-harness.py --root .`

`python3 cpf-tools/verification/frontend-workflow-runtime/run-frontend-workflow-runtime-harness.py --root .`

`python3 cpf-tools/verification/tools/verify-cpf-frontend-substitute-validation.py --repository-root .`

`python3 cpf-tools/verification/tools/verify-cpf-java21-substitute-validation.py --repository-root .`

## 5. 현재 환경 때문에 실제 수행하지 못한 필수 검증

현재 Assistant 실행환경에는 Java 25, `pwsh`, Docker daemon, Gradle 9.1.0 distribution이 없다. Gradle 9.1.0 외부 배포본 다운로드도 실행환경 네트워크에서 확보되지 않았다.

따라서 아래는 **PASS가 아니라 미검증**이다.

- Java25 Root `clean build --continue --stacktrace`
- Java25 전체 `compileJava / compileTestJava / test / publication`
- Oracle/PostgreSQL/MariaDB Fresh → Current → Seed → Runtime E2E → Upgrade → Rollback/Reapply → Recovery
- Kafka 실제 2-worker 분산 → Worker Kill → Lease/Fencing → UNKNOWN → Reconcile → Recovery
- QA39 Toxiproxy/실 Messaging 장애주입 Runtime
- Local One-WAS 기동 및 File Log/DB Log/Trace/Timeline 상관관계
- 실제 ADM/Backoffice running backend에 대한 Browser E2E
- Performance Live
- Public binary live repository resolution
- 최종 Full Runtime 전체 재실행 및 Fresh Replay

## 6. 마지막 실제 사용자 로컬 Full Runtime

현재 Source를 적용하기 전 마지막 실제 로컬 실행 결과는 `PASS=110 / FAIL=34 / SKIP_ENV=2 / NOT_EXECUTED=7`이었다. 이번 개발은 그 실패 Root Cause를 Source/Harness/정본에서 보정했지만, 새 Source의 동일 Full Runtime 결과가 아직 없으므로 해당 수치를 0으로 변경하지 않는다.

QA 진입 조건은 새 Source에서 `FAIL=0 / SKIP_ENV=0 / NOT_EXECUTED=0 / UNVERIFIED=0` 및 필수 Java25/DB3/Docker/Browser/Fresh Replay 성공이다.

## 7. Evidence

- `cpf-docs/work/evidence/developer-gpt/current/CPF_20260825_STATIC_CLOSURE.json`
- `cpf-docs/work/evidence/developer-gpt/current/final-static/FINAL_STATIC_REGRESSION.json`
- `cpf-docs/work/evidence/developer-gpt/current/final-static/canonical-verifiers.log`
- `cpf-docs/work/evidence/developer-gpt/current/final-static/windows-path.log`
- `cpf-docs/work/evidence/developer-gpt/current/final-static/java21-substitute.json`
- `cpf-docs/work/evidence/developer-gpt/current/final-static/frontend-substitute.json`

Generated at: `2026-08-25T17:36:54+09:00`


## 8. 최종 Current Source 재검증 및 Overlay 규칙

- 최종 재검증 시각: `2026-08-25T17:48:16+09:00`
- Canonical verifier: `24/24 PASS`
- Windows Path: `PASS`, 프로젝트 상대경로+파일명 최대 `199`, 200자 초과 `0`
- Testing-tools 최종 분할 전수: `385 PASS / 22 environment SKIP / 2 subtests PASS / FAIL 0`
- Verification: `73 PASS / 3 subtests PASS / FAIL 0`
- DB: `86 + 125 PASS / 2 environment SKIP / FAIL 0`
- Generator: `46 PASS / 10 environment SKIP / 6 subtests PASS / FAIL 0`
- Runtime tool contracts: `76 PASS / 2 environment SKIP / 7 subtests PASS / FAIL 0`
- OpenAPI/Security/Release/Public/OpenGit/Supply/Docker Harness: 실행 범위 `FAIL 0`
- Java21 substitute currentization regression: `1 PASS`, 실제 compile/runtime harness `PASS`
- NXT3 `verify-all`의 정적 Stage는 실패가 없었으나 `db3_live`, `multi_instance_reconcile`, `process_kill_recovery`는 현재 환경에 `pwsh/Docker`가 없어 `UNVERIFIED rc=127`이다. 이를 PASS로 승격하지 않는다.
- 결과 ZIP은 **Baseline 대비 ADD/MODIFY 파일만 포함**한다. unchanged 파일은 `0`건 포함한다. 삭제 대상 220건은 파일 payload가 아니라 `DELETE_MANIFEST.csv`로만 전달한다.

- Modified-only Overlay Fresh Replay: `PASS`, current/replay `10,462 / 10,462`, missing `0`, extra `0`, hash diff `0`, unchanged ZIP payload `0`.

Generated at: `2026-08-25T17:48:16+09:00`


## 9. 패키징 직전 재검증

- Current Product Source SHA-256: `c79be31a71c15c02665d56e29c0f51244c91ab3894183775ce311cde3dbf40df`
- Canonical verifier runner: 22개 연속 PASS 후 장시간 제한으로 중단, 잔여 `repository-garbage`와 `toolchain-contract`을 독립 재실행하여 둘 다 PASS. 따라서 registry 24개 모두 현재 Source에서 PASS 확인.
- `repository-garbage`: PASS, decisionCount 946 / deleteCount 220 / ephemeralCacheCount 0.
- `toolchain-contract --mutation-self-test`: PASS, Node `>=22.18.0 <25`, npm `10.9.2`, mutation PASS.
- Overlay ZIP은 Baseline 대비 실제 ADD/MODIFY만 포함하며 unchanged entry가 발견되면 패키징 자체를 FAIL한다.

Generated at: `2026-08-25T17:53:08+09:00`
