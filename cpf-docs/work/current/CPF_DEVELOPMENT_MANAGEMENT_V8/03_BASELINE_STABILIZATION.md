# CPF 현재 Baseline P0/P1 안정화 작업 V8

> 대상 Diff: `f97655c1299936a1101bc3ec10239265ec3b502e..faedf43a7baffdad456bf40f8e46d622db9cfc76`  
> QA가 수정한 제품 Source는 개발 GPT가 다시 재현·검토·보완한다.  
> 아래 28개는 Canonical 작업과 별개로 먼저 닫아야 할 현재 baseline 위험이다.

## STAB-001 — OpenAPI 삭제 API 타입 불일치

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

OpenAPI 원본→Generator→Generated Client→Frontend Consumer→Controller 계약을 정합화한다.

### 대상 경로

- `cpf-admin/frontend/openapi/**`
- `cpf-admin/frontend/src/generated/**`
- `cpf-admin/frontend/src/app/**`

### 상세 작업

- [ ] STAB-001-TASK-01: requestBody 전파 오류 재현
- [ ] STAB-001-TASK-02: clean regeneration
- [ ] STAB-001-TASK-03: strict typecheck
- [ ] STAB-001-TASK-04: 실제 삭제 Consumer/Controller contract
- [ ] STAB-001-TASK-05: 무관 operation negative fixture
- [ ] STAB-001-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-001-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-001-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-001-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-002 — Notification Rule raw URL 우회

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

알림 규칙 조회·생성·수정·위험조치를 Generated Client와 표준 오류/권한 경로로 통일한다.

### 대상 경로

- `cpf-admin/frontend/src/app/**`
- `cpf-admin/frontend/src/generated/**`
- `cpf-admin/src/**`

### 상세 작업

- [ ] STAB-002-TASK-01: raw URL inventory
- [ ] STAB-002-TASK-02: Generated client parity
- [ ] STAB-002-TASK-03: 권한/중복/timeout workflow
- [ ] STAB-002-TASK-04: RFC 9457 mapping
- [ ] STAB-002-TASK-05: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-002-TASK-06: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-002-TASK-07: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-002-TASK-08: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-003 — Generated Source 정본·Drift

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

OpenAPI/Generator 입력과 생성 산출물의 결정적 재생성, orphan model/operation 제거와 user-owned 영역을 검증한다.

### 대상 경로

- `cpf-admin/frontend/openapi/**`
- `cpf-admin/frontend/src/generated/**`
- `generator metadata`

### 상세 작업

- [ ] STAB-003-TASK-01: input/version manifest
- [ ] STAB-003-TASK-02: remove→regenerate
- [ ] STAB-003-TASK-03: normalized diff
- [ ] STAB-003-TASK-04: orphan reference scan
- [ ] STAB-003-TASK-05: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-003-TASK-06: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-003-TASK-07: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-003-TASK-08: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-004 — SensitiveDataMasker 우회·경계

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Bearer/JSON/KV/Header/Exception/Nested 값과 Unicode·길이 경계에서 원문 누출을 차단한다.

### 대상 경로

- `cpf-core/**/SensitiveDataMasker*`
- `logging/audit/download consumers`

### 상세 작업

- [ ] STAB-004-TASK-01: negative corpus
- [ ] STAB-004-TASK-02: property/boundary
- [ ] STAB-004-TASK-03: double masking
- [ ] STAB-004-TASK-04: performance/resource bound
- [ ] STAB-004-TASK-05: actual consumer
- [ ] STAB-004-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-004-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-004-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-004-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-005 — File Log Writer 안전성

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

권한·rotation·retention·disk failure·concurrency·crash 시 유실과 업무 오염 정책을 완성한다.

### 대상 경로

- `cpf-core/**/CpfFileLogWriter*`
- `file log tests`

### 상세 작업

- [ ] STAB-005-TASK-01: POSIX/Windows
- [ ] STAB-005-TASK-02: rotation race
- [ ] STAB-005-TASK-03: disk full
- [ ] STAB-005-TASK-04: crash/restart
- [ ] STAB-005-TASK-05: spool/alert/dedup
- [ ] STAB-005-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-005-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-005-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-005-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-006 — Reconciliation Worker 다중 인스턴스

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Claim/Lease/Fencing, Probe, Circuit, Manual Review와 scheduler/resource lifecycle을 검증한다.

### 대상 경로

- `cpf-core/**/reconciliation/**`

### 상세 작업

- [ ] STAB-006-TASK-01: 2+ process claim
- [ ] STAB-006-TASK-02: lease takeover
- [ ] STAB-006-TASK-03: stale completion
- [ ] STAB-006-TASK-04: kill/restart
- [ ] STAB-006-TASK-05: half-open probe
- [ ] STAB-006-TASK-06: shutdown
- [ ] STAB-006-TASK-07: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-006-TASK-08: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-006-TASK-09: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-006-TASK-10: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-007 — RuntimeApplyGuard 중복 Resilience

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

공통 Resilience 계약을 재사용하고 timeout/cancel/rejection/UNKNOWN/resource leak를 정리한다.

### 대상 경로

- `cpf-starters/platform-operations/runtime-control-client/**`

### 상세 작업

- [ ] STAB-007-TASK-01: duplicate engine analysis
- [ ] STAB-007-TASK-02: interrupt preservation
- [ ] STAB-007-TASK-03: executor leak
- [ ] STAB-007-TASK-04: bulkhead/circuit/retry
- [ ] STAB-007-TASK-05: unknown ledger
- [ ] STAB-007-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-007-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-007-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-007-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-008 — Runtime Rollback·Agent·Reconcile

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Apply→ACK→Observe→Rollback/Reconcile의 CAS/lease/fencing/duplicate/stale ACK와 process kill을 완성한다.

### 대상 경로

- `cpf-starters/platform-operations/runtime-control-client/**`
- `cpf-admin runtime control`

### 상세 작업

- [ ] STAB-008-TASK-01: state transitions
- [ ] STAB-008-TASK-02: canary partial rollout
- [ ] STAB-008-TASK-03: rollback unknown
- [ ] STAB-008-TASK-04: offline/rejoin
- [ ] STAB-008-TASK-05: kill/reclaim
- [ ] STAB-008-TASK-06: audit/approval
- [ ] STAB-008-TASK-07: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-008-TASK-08: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-008-TASK-09: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-008-TASK-10: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-009 — JMS·IBM MQ·RabbitMQ Provider 계약

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Payload/Header/Correlation/Idempotency와 ACK/confirm/transaction/redelivery/error mapping을 실제 Provider까지 검증한다.

### 대상 경로

- `cpf-starters/messaging/jms/**`
- `cpf-starters/messaging/ibm-mq/**`
- `cpf-starters/messaging/rabbitmq/**`

### 상세 작업

- [ ] STAB-009-TASK-01: provider matrix
- [ ] STAB-009-TASK-02: invocation capture
- [ ] STAB-009-TASK-03: connection loss
- [ ] STAB-009-TASK-04: duplicate/redelivery
- [ ] STAB-009-TASK-05: TLS/credential rotation
- [ ] STAB-009-TASK-06: readiness
- [ ] STAB-009-TASK-07: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-009-TASK-08: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-009-TASK-09: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-009-TASK-10: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-010 — TCP 결과불명·부분 I/O

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

부분 read/write, half-open, reconnect, response loss와 전송 후 UNKNOWN durable store/reconcile를 완성한다.

### 대상 경로

- `cpf-starters/integration/tcp/**`

### 상세 작업

- [ ] STAB-010-TASK-01: fault proxy
- [ ] STAB-010-TASK-02: partial frame
- [ ] STAB-010-TASK-03: response loss
- [ ] STAB-010-TASK-04: duplicate correlation
- [ ] STAB-010-TASK-05: restart persistence
- [ ] STAB-010-TASK-06: TLS/allowlist
- [ ] STAB-010-TASK-07: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-010-TASK-08: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-010-TASK-09: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-010-TASK-10: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-011 — Broker Reliability JDBC 부작용·경쟁

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

조회 부작용을 제거하고 claim/version/lease/state transition을 원자화한다.

### 대상 경로

- `cpf-starters/messaging/reliability-jdbc/**`

### 상세 작업

- [ ] STAB-011-TASK-01: query side-effect negative
- [ ] STAB-011-TASK-02: CAS conflict
- [ ] STAB-011-TASK-03: concurrent claim
- [ ] STAB-011-TASK-04: retry/DLQ transition
- [ ] STAB-011-TASK-05: 3-vendor SQL
- [ ] STAB-011-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-011-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-011-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-011-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-012 — Vendor Seed 단일 정본

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Canonical seed→Vendor seed/source의 중복·순서·closure·idempotency를 하나의 생성/검증 경로로 정리한다.

### 대상 경로

- `cpf-tools/db/canonical/**`
- `cpf-tools/db/vendor/**`
- `database-source-plan`

### 상세 작업

- [ ] STAB-012-TASK-01: 3-vendor parse
- [ ] STAB-012-TASK-02: dependency closure
- [ ] STAB-012-TASK-03: double run
- [ ] STAB-012-TASK-04: fresh query
- [ ] STAB-012-TASK-05: drift
- [ ] STAB-012-TASK-06: rollback/reapply
- [ ] STAB-012-TASK-07: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-012-TASK-08: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-012-TASK-09: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-012-TASK-10: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-013 — Oracle SQLPlus Secret Transport

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Credential을 process argument/stdout/stderr/temp에 노출하지 않고 승인된 secure channel로 전달한다.

### 대상 경로

- `cpf-tools/scripts/invoke-official-db-vendor-sql.ps1`
- `related tests`

### 상세 작업

- [ ] STAB-013-TASK-01: PowerShell AST
- [ ] STAB-013-TASK-02: argument capture
- [ ] STAB-013-TASK-03: leak corpus
- [ ] STAB-013-TASK-04: special characters
- [ ] STAB-013-TASK-05: timeout/kill cleanup
- [ ] STAB-013-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-013-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-013-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-013-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-014 — QA Ledger Validator False PASS

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Coverage/traceability 결과와 QA 제품 검수 결과를 분리하고 Gate ID/count/orphan/duplicate를 정확히 검산한다.

### 대상 경로

- `cpf-tools/scripts/validate-cpf-full-qa-ledgers.py`
- `tests`
- `logical master index`

### 상세 작업

- [ ] STAB-014-TASK-01: positive/negative fixtures
- [ ] STAB-014-TASK-02: CPF-GATE
- [ ] STAB-014-TASK-03: baseline mismatch
- [ ] STAB-014-TASK-04: orphan
- [ ] STAB-014-TASK-05: duplicate
- [ ] STAB-014-TASK-06: 미검수 집계
- [ ] STAB-014-TASK-07: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-014-TASK-08: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-014-TASK-09: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-014-TASK-10: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-015 — Canonical 169/162 Count 불일치

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Catalog 169개와 Legacy Alias 8개, Continuity Ledger와 Builder/Validator의 수를 일치시킨다.

### 대상 경로

- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- `Continuity Ledger`
- `Requirement builders`

### 상세 작업

- [ ] STAB-015-TASK-01: catalog ID count
- [ ] STAB-015-TASK-02: alias exclusion
- [ ] STAB-015-TASK-03: derived mapping
- [ ] STAB-015-TASK-04: negative fixture
- [ ] STAB-015-TASK-05: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-015-TASK-06: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-015-TASK-07: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-015-TASK-08: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-016 — Java 25 실제 Toolchain·보안 Patch

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

설치 JDK 탐색과 Gradle Toolchain을 실제 실행하고 Java 25 미실행을 Java 21 성공으로 확대하지 않는다.

### 대상 경로

- `gradle/cpf-stack.properties`
- `wrapper/toolchain config`
- `CI/runtime scripts`

### 상세 작업

- [ ] STAB-016-TASK-01: java/javac/JAVA_HOME inventory
- [ ] STAB-016-TASK-02: Java 25 compile/test
- [ ] STAB-016-TASK-03: toolchain provision attempt
- [ ] STAB-016-TASK-04: alternative compile
- [ ] STAB-016-TASK-05: remaining differences
- [ ] STAB-016-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-016-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-016-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-016-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-017 — Gradle 9.x Wrapper·Plugin Shadow Upgrade

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

현재 9.1.0 정본을 유지하면서 최신 호환 9.x를 shadow build로 평가하고 승인 전 자동 업그레이드하지 않는다.

### 대상 경로

- `gradle-wrapper.properties`
- `plugins`
- `build logic`

### 상세 작업

- [ ] STAB-017-TASK-01: current wrapper build
- [ ] STAB-017-TASK-02: latest 9.x shadow
- [ ] STAB-017-TASK-03: deprecation report
- [ ] STAB-017-TASK-04: plugin compatibility
- [ ] STAB-017-TASK-05: reproducibility
- [ ] STAB-017-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-017-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-017-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-017-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-018 — Spring Boot 4.1·Cloud 2025.1.2 Matrix

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Boot/Cloud/Framework/Batch/Gateway의 실제 BOM 조합과 unsupported fail-closed를 검증한다.

### 대상 경로

- `gradle/cpf-stack.properties`
- `BOM/platform`
- `starter dependencies`

### 상세 작업

- [ ] STAB-018-TASK-01: dependency graph
- [ ] STAB-018-TASK-02: startup matrix
- [ ] STAB-018-TASK-03: removed/deprecated artifact scan
- [ ] STAB-018-TASK-04: sample consumer
- [ ] STAB-018-TASK-05: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-018-TASK-06: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-018-TASK-07: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-018-TASK-08: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-019 — OpenAPI 3.2.0/3.1.2 지원 Profile

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

최신 3.2.0을 바로 강제하지 않고 CPF Generator/Orval/Backend tooling이 보장하는 지원 Profile과 migration plan을 확정한다.

### 대상 경로

- `OpenAPI source`
- `generator config`
- `client/server tooling`

### 상세 작업

- [ ] STAB-019-TASK-01: 3.1.2 current validation
- [ ] STAB-019-TASK-02: 3.2.0 shadow validation
- [ ] STAB-019-TASK-03: reference cycles
- [ ] STAB-019-TASK-04: Markdown sanitization
- [ ] STAB-019-TASK-05: compatibility
- [ ] STAB-019-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-019-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-019-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-019-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-020 — AsyncAPI 3.1 Event Contract 공백

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Event/Message 계약의 machine-readable source, provider binding, schema compatibility와 generated docs/test를 설계·실증한다.

### 대상 경로

- `event/message contracts`
- `messaging starters`
- `quality tools`

### 상세 작업

- [ ] STAB-020-TASK-01: 3.1 document prototype
- [ ] STAB-020-TASK-02: schema validation
- [ ] STAB-020-TASK-03: provider mapping
- [ ] STAB-020-TASK-04: consumer parity
- [ ] STAB-020-TASK-05: version compatibility
- [ ] STAB-020-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-020-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-020-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-020-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-021 — SLSA 1.2·CycloneDX 1.7 Baseline

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Source/Build provenance와 final-artifact CycloneDX 1.7 BOM/CBOM의 생성·검증 정본을 갱신한다.

### 대상 경로

- `build/release scripts`
- `SBOM/provenance configs`
- `artifact verification`

### 상세 작업

- [ ] STAB-021-TASK-01: SLSA track/level declaration
- [ ] STAB-021-TASK-02: provenance verify
- [ ] STAB-021-TASK-03: CycloneDX 1.7 schema
- [ ] STAB-021-TASK-04: final artifact graph
- [ ] STAB-021-TASK-05: signature
- [ ] STAB-021-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-021-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-021-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-021-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-022 — OpenTelemetry SemConv Stability Profile

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

1.43.0의 stable/mixed/development 그룹을 구분하고 unstable default emission과 cardinality drift를 차단한다.

### 대상 경로

- `observability starters`
- `instrumentation config`
- `metrics/traces/logs`

### 상세 작업

- [ ] STAB-022-TASK-01: semantic convention inventory
- [ ] STAB-022-TASK-02: stability opt-in
- [ ] STAB-022-TASK-03: dual emit migration
- [ ] STAB-022-TASK-04: cardinality budget
- [ ] STAB-022-TASK-05: consumer dashboards
- [ ] STAB-022-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-022-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-022-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-022-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-023 — CI·Commit Status·Authoritative Gate 공백

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

현재 baseline에 authoritative CI 결과가 없거나 불완전한 경우 local/CI/release Gate의 동일성과 fail-closed를 복구한다.

### 대상 경로

- `.github/workflows/**`
- `quality scripts`
- `release gates`

### 상세 작업

- [ ] STAB-023-TASK-01: workflow inventory
- [ ] STAB-023-TASK-02: commit status mapping
- [ ] STAB-023-TASK-03: tool failure behavior
- [ ] STAB-023-TASK-04: artifact retention
- [ ] STAB-023-TASK-05: negative fixture
- [ ] STAB-023-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-023-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-023-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-023-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-024 — Requirement/Scenario 전체 Owner·Coverage

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

30,558 Requirement와 전체 Scenario의 Canonical mapping, Primary Owner, orphan/duplicate/미배정 0을 통합 검산한다.

### 대상 경로

- `CPF_REQUIREMENT_MASTER parts`
- `Scenario master`
- `builders/validators`

### 상세 작업

- [ ] STAB-024-TASK-01: all parts assembly
- [ ] STAB-024-TASK-02: canonical map
- [ ] STAB-024-TASK-03: owner union/intersection
- [ ] STAB-024-TASK-04: orphan/duplicate
- [ ] STAB-024-TASK-05: count
- [ ] STAB-024-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-024-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-024-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-024-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-025 — Source·Generated·SQL·API Drift Gate

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Canonical 입력과 Generated Source/SQL/Client/Sample의 bidirectional drift를 제품 Gate로 만든다.

### 대상 경로

- `generators/templates`
- `generated outputs`
- `quality scripts`

### 상세 작업

- [ ] STAB-025-TASK-01: clean regeneration
- [ ] STAB-025-TASK-02: different-hash fixture
- [ ] STAB-025-TASK-03: manual edit detection
- [ ] STAB-025-TASK-04: user-area preservation
- [ ] STAB-025-TASK-05: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-025-TASK-06: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-025-TASK-07: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-025-TASK-08: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-026 — Secret·PII·Evidence Sanitization

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

Source/Config/Process/Log/DB/Browser/Evidence 전체에서 원문 Secret/PII와 unsafe sample credential을 제거·차단한다.

### 대상 경로

- `repository-wide`
- `evidence packages`
- `scripts/config/frontend`

### 상세 작업

- [ ] STAB-026-TASK-01: secret corpus
- [ ] STAB-026-TASK-02: process/log capture
- [ ] STAB-026-TASK-03: browser/download
- [ ] STAB-026-TASK-04: evidence schema
- [ ] STAB-026-TASK-05: negative fixture
- [ ] STAB-026-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-026-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-026-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-026-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-027 — Frontend 외부 Runtime Asset 의존

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

ADM/BZA가 외부 CDN/font/script 없이 fresh clone과 offline deployment에서 동작하게 한다.

### 대상 경로

- `cpf-admin/frontend/**`
- `cpf-biz-admin/frontend/**`
- `build/package`

### 상세 작업

- [ ] STAB-027-TASK-01: network-off build/runtime
- [ ] STAB-027-TASK-02: asset inventory
- [ ] STAB-027-TASK-03: CSP
- [ ] STAB-027-TASK-04: browser E2E
- [ ] STAB-027-TASK-05: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-027-TASK-06: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-027-TASK-07: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-027-TASK-08: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
## STAB-028 — Dead Code·Dual Primary·Stale Evidence

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| 작업 유형 | `BASELINE_STABILIZATION` |
| 완료 판정 | 개발·자체검수 완료 후보; Codex·QA 전 최종 완료 아님 |

### 필수 안정화 결과

QA 혼합 Push와 기존 Source의 중복 Primary, consumerless abstraction, generated garbage와 stale evidence를 Manifest로 정리한다.

### 대상 경로

- `repository-wide`
- `cpf-docs/evidence/**`
- `generated/build outputs`

### 상세 작업

- [ ] STAB-028-TASK-01: consumer/reference scan
- [ ] STAB-028-TASK-02: duplicate implementation
- [ ] STAB-028-TASK-03: stale SHA
- [ ] STAB-028-TASK-04: delete manifest
- [ ] STAB-028-TASK-05: fresh clone
- [ ] STAB-028-TASK-06: 실제 Product Consumer와 전체 호출 경로를 추적한다.
- [ ] STAB-028-TASK-07: 관련 Canonical/CPF-FR/CPF-SC와 Evidence를 연결한다.
- [ ] STAB-028-TASK-08: 목표 환경 직접검증과 가능한 대체검증을 수행한다.
- [ ] STAB-028-TASK-09: DB·SQL·Query 영향과 Generator→초기화→3 Vendor→Consumer 연쇄를 판정한다.

---
