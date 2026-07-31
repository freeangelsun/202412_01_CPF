# CPF Current Work Request — QA33 Source·Runtime Closure

> Canonical current request: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`  
> Repository: `https://github.com/freeangelsun/202412_01_CPF`  
> Branch: `master`  
> Document synchronization review baseline: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`  
> Updated: `2026-07-31T19:07:00+09:00`

## 1. 최우선 정본

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
3. `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`
4. `cpf-docs/architecture/ADR_OSS_FIRST_PLATFORM_DIRECTION.md`
5. `cpf-docs/architecture/CPF_BUILD_VS_BUY_MATRIX.md`
6. `cpf-docs/architecture/CPF_STACK_SUPPORT_AND_MIGRATION_DECISION.md`
7. 이 Current Request
8. QA33 Package Index·Matrix·Handover

## 2. ID 체계

- Canonical Product Requirement: **162개**
- Legacy Alias: 8개, 완료율 제외
- QA33 Remediation Requirement: **138개**
- QA33 Mandatory Scenario: **414개**
- QA33 Defect/Gap: 113개
- QA33 Source Inspection: 115개
- QA33 Evidence Requirement: 28개

QA33 138개는 162개 Product Requirement를 대체하거나 추가하는 것이 아니다. Source 결함 수정과 검증을 위한 작업 원장이다.

## 3. 현재 판정

- Final Target과 관련 활성 정본은 상세 현행화 대상이다.
- QA31 Current Request는 superseded다.
- QA32 Source/Runtime Closure는 완료되지 않았다.
- QA33에서 일부 Source blocker 수정 보고가 있으나 전체 138 Requirement와 414 Scenario의 exact-SHA Runtime Evidence가 없다.
- Java25 full Gradle, ADM/BZA clean npm/3 Browser, 3DB, Kafka, Gateway/Batch/Scheduler/Agent multi-instance, final Artifact Supply-chain은 완료 증명 전까지 `미검증`이다.
- `stackState=TARGET`은 `SUPPORTED_GA`가 아니다.
- 전체 GA 완료 판정 금지.

## 4. 작업 시작

```powershell
git fetch origin master
git rev-parse HEAD
git rev-parse origin/master
git status --porcelain=v1
python cpf-tools/scripts/sync-cpf-final-target-document-references.py --root . --check
python cpf-tools/scripts/verify-cpf-final-target-document-consistency.py --root .
python cpf-tools/scripts/verify-cpf-qa33-request-integrity.py --root .
```

- `HEAD`와 `origin/master`가 다르면 최신 diff를 먼저 검토한다.
- Working Tree 변경을 임의로 reset/restore/clean/stash하지 않는다.
- 기준 SHA는 작업 시작 시 새로 기록한다.

## 5. QA33 읽기 순서

1. `cpf-docs/work/current/CPF_20260731_QA33_PACKAGE_INDEX.md`
2. `cpf-docs/work/current/CPF_20260731_QA33_GPT_DEVELOPMENT_INSTRUCTION.md`
3. `cpf-docs/work/current/CPF_20260731_QA33_DEVELOPMENT_AND_VERIFICATION_REQUEST.md`
4. `cpf-docs/work/review/CPF_20260731_QA32_INDEPENDENT_SOURCE_REVIEW.md`
5. `cpf-docs/quality/CPF_20260731_QA33_DEFECT_REGISTER.csv`
6. `cpf-docs/quality/CPF_20260731_QA33_REQUIREMENT_MATRIX.csv`
7. `cpf-docs/quality/CPF_20260731_QA33_SCENARIO_MATRIX.csv`
8. `cpf-docs/quality/CPF_20260731_QA33_SOURCE_INSPECTION_MATRIX.csv`
9. `cpf-docs/quality/CPF_20260731_QA33_EVIDENCE_MATRIX.csv`
10. `cpf-docs/work/handover/CPF_20260731_QA33_REVIEW_HANDOVER.md`

## 6. 작업 Phase

### Phase 0 — Baseline·정본·Evidence

- 최신 exact SHA와 clean tree
- Final Target/Continuity/Current/QA33 정합성
- stale current 문서 제거
- Evidence template/schema/manifest
- package integrity

### Phase 1 — Build

- settings/includeBuild/project graph
- Java25 full build/test
- Plugin/BOM/lock/POM
- Generated Domain
- LOCAL_DEV/REMOTE/OFFLINE
- final artifact package

### Phase 2 — Frontend·BFF

- ADM/BZA lockfile
- Orval generated client
- actual Query/Mutation consumer
- Legacy raw fetch/store removal
- Session/CSRF/Fixation/Revocation
- credential/session ID non-exposure
- 3 Browser E2E

### Phase 3 — OSS Ownership

- Core/Common 선택 Runtime 분리
- Starter actual consumer
- Legacy/Dual Primary 제거
- published artifact boundary

### Phase 4 — Batch·Kafka·Scheduler

- request hash idempotency
- latest fencing
- start/bind unknown reconciliation
- Spring Batch status/retry/stop
- Kafka stable correlation/DLT/backpressure
- multi-manager response
- Scheduler outbox/reconciliation

### Phase 5 — Gateway

- SCG MVC standard lifecycle
- trusted header
- SSRF/TLS
- replay-safe body
- async/stream completion
- attempt/unknown ledger
- audit isolation
- scale-out

### Phase 6 — DB·Deployment·Agent·Resource

- 3 Vendor lifecycle/parity
- selective rollback
- artifact trust/state/key rotation
- bootstrap reconciliation
- Archive/Attachment streaming/atomicity

### Phase 7 — Runtime·Supply-chain·Evidence

- Java25 full
- ADM/BZA 3 Browser
- 3DB
- Kafka
- Gateway/Batch/Scheduler/Agent multi-instance/fault
- final Artifact CycloneDX/ORT/Syft/Grype
- exact-SHA Requirement/Scenario Evidence

## 7. 완료 조건

- Canonical Product Requirement 영향이 Trace됨
- QA33 138 Requirement 개발·검증 완료
- QA33 414 Scenario 완료
- Defect/Unresolved 0
- Source Inspection/Evidence Matrix 완료
- Legacy/Dual Primary 0
- 실제 실행하지 않은 검증 0
- exact Source/Artifact/Evidence SHA 일치
- independent review 완료
- repository hygiene 통과

## 8. 범위 제외

이번 Current Request의 기능 QA에서 README와 공식 매뉴얼 내용 재작성은 제외한다. 단, Source 변경이 사용자 문서에 영향을 주면 별도 문서 작업 대상으로 기록한다.

## 9. 결과

- Source/SQL/Test/Script
- QA33 Result Matrix
- Completion Report
- Unresolved Register
- Requirement/Scenario Evidence
- latest Handover/Continuity
- Root-relative Overlay ZIP
- exact SHA, file count, delete count, ZIP SHA-256
- Commit/Push 수행 여부

사용자 승인 없이 Commit, Push, Branch, Tag, PR과 Release를 생성하지 않는다.
