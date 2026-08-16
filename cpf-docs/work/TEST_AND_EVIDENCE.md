# CPF Current Test and Evidence

> 입력 baseline provenance: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`  
> 현재 결과 Content SHA-1: `470ce244d05cdd2674385eb743630e2537f2963c`  
> 현재 결과 Content SHA-256: `f049bf01a59cf57bc823ef59656516c867db9cab2aed6262abc26c4d840d2618`  
> identity 정책: Git 조회 없이 제품 Source canonical path/size/SHA-256 목록으로 계산. baseline SHA와 result content identity를 분리한다.  
> 전체 판정: **개발 GPT 재개발/정적·독립 재검수 범위 PASS. Java25/PowerShell7/Docker live/Browser Runtime은 미검증이며 최종 QA 완료가 아니다.**

## 1. 이번 재개발 핵심

- VS Code/Gradle Projects를 실제 logical path `apps / runtime / framework / starters / internal`로 계층화했다. 물리 폴더와 Maven artifact 좌표는 유지하고 모든 `project(':...')` consumer, Catalog, Generator, Publication 검증을 logical path-aware로 currentize했다.
- 일반 개발 진입점을 `cpfHelp`, `cpfBuild`, `cpfTest`, `cpfVerifyFast`, `cpfVerifyFullLocal`, `cpfRunLocal`, `cpfRunBatch` 중심으로 정리했다. Windows canonical shell은 PowerShell 7 `pwsh`이며 Java25/ResourceProfile/LowMemory 정책을 wrapper가 적용한다.
- FullLocal에 기존 로컬 검수뿐 아니라 Codex에서 수행하려던 자동화 가능 고가치 항목을 흡수했다: Transaction/Header, Fixed-Length, Approval/위험조치, Security/OIDC, Cache, Generated Domain/DB3 static, Batch UNKNOWN/Ghost/Fencing, Gateway/Topology, Messaging/Kafka, Process Kill/2-worker crash, Runtime OpenAPI, Frontend generated consumer, Playwright/A11y, Deployment/Performance.
- `FullLocal`은 독립 단계 실패 후에도 계속 수집하고 ZIP을 생성한 뒤, `FullLocal` 또는 `StrictExit`에서 FAIL이 있으면 non-zero로 종료한다.
- Source identity와 managed-state를 Git에서 분리했다. FullLocal 전/후 product source content identity와 managed tree를 모두 비교해 검증 중 Source mutation을 fail-closed 처리한다.
- Cache/Kafka live verifier도 Git을 조회하지 않으며 검증기가 소유한 container/resource만 restart/cleanup한다.
- Performance mixed profile은 선택 workload를 먼저 결정한 후 해당 workload의 환경 토큰만 확장·검증한다.
- Evidence Integrity는 PACKAGE_MANIFEST↔SHA256SUMS↔CHANGE_MANIFEST를 실물 hash/size로 교차검증하고, QA-B3 완료 Finding마다 중복되지 않는 실행 명령과 Finding ID 전용 Evidence를 요구한다.

## 2. 실제 재검수 결과

### Core / NXT3 / Architecture

- NXT3: **22/22 PASS** (clean snapshot 기준).
- Gradle logical tree gate: PASS.
- Starter catalog truth: **64 modules**, public/internal visibility PASS.
- Dependency closure: PASS, undeclared 0, 운영 SCC cycle 0. test-scoped edge는 운영 SCC에서만 제외하고 owner 금지 검사는 유지.
- ADM dependency boundary / Owner boundary / Supply-chain / Zero-footprint: PASS.
- Supply-chain은 artifact ownerPath 실재 여부까지 fail-closed로 확인한다.
- Windows path static projection: 사용자 root `C:\dev\projects\jck\202412_01_CPF` 기준 **max full path 213 / hard budget 240 / failure 0 / PASS**. 상대경로 160 초과 26건은 warning으로 보존.

### Python / DB / Generator / Runtime 계약

- Testing Tools: **366 PASS / 22 SKIP / FAIL 0**.
- DB tests: **86/86 PASS**.
- DB verification: **75/75 PASS**.
- Generator verification: **27 PASS / 10 SKIP / FAIL 0**.
- Runtime + Security + Release + OpenAPI: **108 PASS / 2 SKIP / FAIL 0**.
- Verification tests: **45/45 PASS**.
- Docker-development fixture tests: **6/6 PASS**.
- QA-V41 performance/source-state/evidence/full-local focused regression도 PASS 범위 확인.

### QA-B3 25건 재실행

- 개발 GPT 완료: **22/25**.
- 개발 GPT 미완료: **3/25** (`QA-B3-008`, `QA-B3-010`, `QA-B3-011`).
- 25건은 `cpf-docs/work/evidence/current/qa-b3/QA-B3-xxx.txt` 전용 Evidence를 사용한다.
- 완료 22건은 각각 서로 다른 exact command와 exit code 0으로 재실행했다.
- `QA-B3-008`: result content identity는 확정했으나 post-commit exact Git SHA는 미확정. Git 조회를 수행하지 않았다.
- `QA-B3-010`: Java25/DB3/Process Kill/Browser/Deployment/Performance 실제 FullLocal Runtime 미실행.
- `QA-B3-011`: 실제 사용자 경로 정적 projection은 PASS이나 Windows fresh extract + Java25 Gradle/Runtime Evidence는 미실행.


## 2A. 2026-08-16 사용자 Full Source 재검수 추가 보정

- 입력은 사용자 로컬 적용 후 전체 Source ZIP `CPF_FULL_SOURCE_FOR_NEXT_QA(20260816-025824).zip`이며 Git/GitHub 조회 없이 byte/독립 실행 기준으로 검토했다.
- 이전 QA-B3 전용 Evidence가 `*.log`여서 Repository `*.log` ignore 정책에 의해 전체 Source 전달본에서 25개 증적이 누락되는 전달 결함을 확인했다. 전용 Evidence를 package-safe `QA-B3-xxx.txt`로 전환하고 Evidence Integrity가 비허용 확장자를 fail-closed하도록 보강했다.
- Windows canonical EOL 정책(`*.ps1` CRLF) 때문에 이전 LF 작업본의 result identity가 사용자 실제 Source byte와 달랐던 문제를 확인했다. 현재 result content identity는 실제 사용자 Source byte + 이번 보정 Source 기준으로 다시 계산했다.
- ADM/BZA tracked pre-runtime OpenAPI가 Controller Source보다 stale하여 오류 응답 계약이 200-only로 남은 drift를 확인했다. canonical Controller Source writer 결과로 ADM 321 operations / BZA 96 operations를 currentize하고 generated marker를 재계산했다. ADM/BZA Source validation/lifecycle/generated-client/consumer가 다시 PASS했다.
- FullLocal에 `ADM_CONTROLLER_SOURCE_OPENAPI_CURRENT` / `BZA_CONTROLLER_SOURCE_OPENAPI_CURRENT` fail-closed 단계를 추가하여 동일 drift 재발을 막았다.
- Windows path verifier가 보호 대상 `cpf-docs/deliverables/**`의 날짜형 archival directory까지 일반 Source version-folder 위반으로 판정하던 false FAIL을 수정했다. 보호 경로도 path-length budget은 그대로 검사하며, 날짜형 directory naming 예외만 `cpf-docs/deliverables/**`에 한정한다. 회귀 7/7 및 사용자 root projection max 213/240 PASS.
- 사용자 `CPF_LOCAL_VALIDATION_20260816_124024.zip`을 직접 분석했다. `[01]~[05]`는 PASS였고 `[06] NXT3_22`에서 FullLocal 결과 log directory 소실과 NXT3 generated-cache 오탐이 발생해 본 재개발에서 수정했다. 수정본의 Windows FullLocal 재실행은 아직 필요하다.

### 2B. 2026-08-16 12:40 FullLocal 실패 및 통합로그 보강

- FullLocal 결과 log directory가 `[06] NXT3_22` 도중 사라져 `Add-Content`가 연쇄 실패한 오케스트레이터 결함을 확인했다. 진행 로그/Evidence를 OS TEMP scratch에 기록하고 결과 directory를 stage마다 재보장한 뒤 최종 결과/ZIP으로 복사하도록 변경했다.
- Repository 내부 Python venv가 Garbage/Hygiene의 `__pycache__/.pyc` false failure를 만들던 구조를 외부 local/temp cache로 이동했다.
- `cpf-member/.gradle`, `cpf-external/.gradle`, root `.pytest_cache` 등 실행 생성 cache를 Generated Domain IA로 오인하던 Gate를 보정했다. 동일 cache를 의도적으로 만든 재현 상태에서 Root/Minimal IA, Garbage, Hygiene, NXT3 전체 22/22를 재검수했다.
- Runtime logging 검증을 다음 3개 독립 stage로 추가했다.
  - `LOCAL_FILE_LOG_STANDARD`: 실제 거래 후 structured FileLog 경로/JSON/transactionId 확인.
  - `LOCAL_DB_LOG_POLICY_RUNTIME`: DB log ON/OFF, request/response/error policy, ADM DB/Audit 조회 확인.
  - `LOCAL_INTEGRATED_LOG_CORRELATION`: FileLog↔DB/ADM의 동일 transactionId/traceId, file-log recovery pending/quarantine/terminal-loss=0, local WAS stdout/stderr fatal pattern 부재, password/access-token 원문 미노출 확인.
- 로컬 로그 확인 위치는 structured FileLog `<repo>/logs`, WAS stdout/stderr `<repo>/build/cpf-local-runtime/logs`, FullLocal 최종 Evidence는 `CPF_LOCAL_VALIDATION_<timestamp>.zip` 내부 stage log/evidence다.
- 통합로그 static closure와 verification regression은 PASS지만 실제 Java25/MariaDB/1-WAS runtime correlation은 다음 Windows FullLocal 결과로 최종 판정한다.
- QA-V41 영향도 재검수: focused regression 34/34 PASS, `broker-backpressure`, `batch-reconcile`, `resource-budget` 실제 dry-run 모두 RC=0.

## 3. QA-V41 재개발 반영

- `QA-V41-001`: 선택하지 않은 HTTP workload env token 때문에 broker/batch/resource dry-run이 실패하던 문제 수정. 3 workload dry-run PASS.
- `QA-V41-002`: `baselineSha`와 `resultContentSha1/Sha256` 분리.
- `QA-V41-003`: SHA256SUMS 및 CHANGE_MANIFEST corruption negative case를 fail-closed로 검증.
- `QA-V41-004`: 최종 패키징에서 CHANGE_MANIFEST를 실제 최종 payload bytes로 재생성한다.
- `QA-V41-005`: 개발 전달 문서/Projection baseline provenance를 `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`로 통일. 과거 History/Codex evidence의 과거 SHA는 변조하지 않는다.
- `QA-V41-006`: QA-B3 25건별 exact command와 전용 Evidence로 분리.
- `QA-V41-007`: FullLocal 결과 ZIP 생성 후 strict exit 계약 반영.
- `QA-V41-008`: Overlay payload 44개가 아닌 전체 managed product tree before/after 감시.
- `QA-V41-009`: 이번 작업은 사용자가 제공한 Full Source ZIP을 입력 정본으로 수행했다.

## 4. 미검증 — PASS 처리 금지

다음은 사용자 Windows FullLocal에서 실제 실행해야 한다.

- Java25 Root Gradle configuration/projects/help/build/test/assemble/qualityGate/qa34/publication/SBOM.
- ADM/BZA npm ci, lint/typecheck/unit/build, Orval regeneration, Playwright E2E/A11y.
- Oracle/PostgreSQL/MariaDB install/seed/runtime-query/upgrade/rollback/reapply.
- Redis/Valkey reconnect/invalidation/multi-instance/provider failure.
- Kafka Outbox/Inbox/DLQ/restart durability.
- Batch scheduler/worker/center-cut 2-worker/process-kill/UNKNOWN/Reconcile/double-effect-zero.
- Local 1-WAS/Gateway OFF·ON/split/distributed/multi-instance.
- Security adversarial, deployment/fresh-host, performance/backpressure.
- Windows fresh extract + Java25 전체 lifecycle.

Canonical command:

```powershell
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\verification\tools\run-cpf-local-full-validation.ps1 -ResourceProfile local -OutputRoot "$HOME\Downloads" -FullLocal
```

`-FullLocal`은 strict exit가 자동 적용되어 모든 독립 단계를 끝까지 수집하고 결과 ZIP을 만든 뒤 FAIL이 남으면 non-zero로 종료한다.

## 5. Requirement / 역할 경계

- Canonical Requirement 정본은 `cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv` + Part dataset, 논리 **30,605건**이다.
- `REQUIREMENT_STATUS.csv` 31행은 개발 GPT Projection이며 Canonical QA 완료 원장이 아니다.
- 개발 GPT는 QA/Codex 상태를 완료로 변경하지 않았다.
- Runtime 미실행 항목은 미완료/미검증을 유지한다.

## 6. 삭제 / 보호

- 이번 최종 Overlay 비교 기준 실제 Source 삭제: **0건**.
- 기존 `CPF_DELETE_MANIFEST.csv`/Garbage ledger는 과거 hygiene 결정을 보존하는 관리 자료이며 이번 Overlay 적용 명령에서 자동 삭제하지 않는다.
- 보호 경로 삭제 0건.
- `cpf-tools/build/**`는 제품 Source로 Source identity/managed-state/패키지에 포함한다.
