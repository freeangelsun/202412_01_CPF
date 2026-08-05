# CPF V7 상세 개발 Work Package — Batch·Agent·Worker

- Canonical Requirement: 8개
- 실행 Work Package: 37개
- Canonical IDs: `BAT-AGENT`, `BAT-CALL-ASYNC`, `BAT-CALL-SYNC`, `BAT-CORE`, `BAT-EXECUTOR`, `BAT-ITEM`, `BAT-JOB`, `BAT-SHARED`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP — BAT-CORE / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CORE` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Spring Batch를 Job/Step/Repository/ExecutionContext/Restart의 단일 Primary Engine으로 사용하고 자체 중복 실행 Engine을 제거한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER — BAT-CORE / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CORE` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Spring Batch를 Job/Step/Repository/ExecutionContext/Restart의 단일 Primary Engine으로 사용하고 자체 중복 실행 Engine을 제거한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CORE-03-VERIFICATION_EVIDENCE — BAT-CORE / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CORE` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-CORE-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Spring Batch를 Job/Step/Repository/ExecutionContext/Restart의 단일 Primary Engine으로 사용하고 자체 중복 실행 Engine을 제거한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CORE-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-CORE-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-CORE-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-CORE-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CORE-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-CORE-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CORE-04-FAILURE_RECOVERY — BAT-CORE / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CORE` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-CORE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Spring Batch를 Job/Step/Repository/ExecutionContext/Restart의 단일 Primary Engine으로 사용하고 자체 중복 실행 Engine을 제거한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CORE-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-CORE-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-CORE-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-CORE-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CORE-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-CORE-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP — BAT-JOB / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-JOB` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER — BAT-JOB / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-JOB` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-JOB-03-VERIFICATION_EVIDENCE — BAT-JOB / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-JOB` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-JOB-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-JOB-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-JOB-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-JOB-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-JOB-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-JOB-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-JOB-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-JOB-04-FAILURE_RECOVERY — BAT-JOB / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-JOB` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-JOB-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-JOB-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-JOB-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-JOB-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-JOB-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-JOB-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-JOB-05-DATA_MIGRATION — BAT-JOB / DB·Query·Generator·Vendor Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-JOB` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `DATA_MIGRATION` |
| 선행 작업 | `CPF-WP-BAT-JOB-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-JOB-05-DATA_MIGRATION-REQ-01: DB/SQL/Query 영향 여부를 반드시 판정하고 N/A면 실제 호출 경로 근거를 기록한다.
- [ ] CPF-WP-BAT-JOB-05-DATA_MIGRATION-REQ-02: Canonical Model/Query Contract→Generator/Template→Generated SQL/Mapper→Fresh Init/Seed→Migration/Rollback/Reapply 순서로 변경한다.
- [ ] CPF-WP-BAT-JOB-05-DATA_MIGRATION-REQ-03: MariaDB/PostgreSQL/Oracle의 type/default/paging/locking/error/index/FK/transaction 차이를 전수 반영한다.
- [ ] CPF-WP-BAT-JOB-05-DATA_MIGRATION-REQ-04: Repository/Mapper/DTO/Service/API/Frontend/Batch/ADM/BZA/Sample/EDU 소비자를 모두 점검한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-JOB-05-DATA_MIGRATION-PROP-01: Vendor-native SQL을 유지하되 Canonical Metadata에서 생성/검증 가능한 구조를 우선한다.
- [ ] CPF-WP-BAT-JOB-05-DATA_MIGRATION-PROP-02: 파괴적 변경은 expand→migrate→contract와 compatibility window를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`fresh install`, `double seed`, `upgrade`, `migration interruption`, `rollback/forward recovery`, `drift`, `vendor difference`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP — BAT-ITEM / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-ITEM` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

reader/processor/writer, chunk/skip/retry/checkpoint, item idempotency, partition, restart와 대용량 memory bound를 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER — BAT-ITEM / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-ITEM` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

reader/processor/writer, chunk/skip/retry/checkpoint, item idempotency, partition, restart와 대용량 memory bound를 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-ITEM-03-VERIFICATION_EVIDENCE — BAT-ITEM / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-ITEM` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-ITEM-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

reader/processor/writer, chunk/skip/retry/checkpoint, item idempotency, partition, restart와 대용량 memory bound를 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-ITEM-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-ITEM-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-ITEM-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-ITEM-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-ITEM-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-ITEM-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-ITEM-04-FAILURE_RECOVERY — BAT-ITEM / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-ITEM` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-ITEM-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

reader/processor/writer, chunk/skip/retry/checkpoint, item idempotency, partition, restart와 대용량 memory bound를 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-ITEM-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-ITEM-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-ITEM-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-ITEM-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-ITEM-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-ITEM-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP — BAT-EXECUTOR / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-EXECUTOR` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CISA-SBD`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER — BAT-EXECUTOR / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-EXECUTOR` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CISA-SBD`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-EXECUTOR-03-VERIFICATION_EVIDENCE — BAT-EXECUTOR / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-EXECUTOR` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-EXECUTOR-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CISA-SBD`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-EXECUTOR-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-EXECUTOR-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-EXECUTOR-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-EXECUTOR-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-EXECUTOR-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-EXECUTOR-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-EXECUTOR-04-FAILURE_RECOVERY — BAT-EXECUTOR / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-EXECUTOR` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-09-SECURITY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CISA-SBD`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-EXECUTOR-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-EXECUTOR-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-EXECUTOR-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-EXECUTOR-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-EXECUTOR-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-EXECUTOR-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-EXECUTOR-05-OPERATIONS_SECURITY — BAT-EXECUTOR / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-EXECUTOR` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-BAT-EXECUTOR-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CISA-SBD`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-EXECUTOR-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-BAT-EXECUTOR-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-BAT-EXECUTOR-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-BAT-EXECUTOR-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-EXECUTOR-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-BAT-EXECUTOR-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP — BAT-AGENT / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-AGENT` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER — BAT-AGENT / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-AGENT` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-AGENT-03-VERIFICATION_EVIDENCE — BAT-AGENT / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-AGENT` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-AGENT-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-AGENT-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-AGENT-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-AGENT-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-AGENT-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-AGENT-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-AGENT-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-AGENT-04-FAILURE_RECOVERY — BAT-AGENT / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-AGENT` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-AGENT-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-AGENT-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-AGENT-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-AGENT-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-AGENT-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-AGENT-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-AGENT-05-GENERATION_COMPATIBILITY — BAT-AGENT / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-AGENT` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-BAT-AGENT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-AGENT-05-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-BAT-AGENT-05-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-BAT-AGENT-05-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-BAT-AGENT-05-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-AGENT-05-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-BAT-AGENT-05-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP — BAT-CALL-SYNC / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-SYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 업무 Domain 동기 호출에 Local/Remote parity, Header, deadline, idempotency, retry/unknown-result를 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER — BAT-CALL-SYNC / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-SYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 업무 Domain 동기 호출에 Local/Remote parity, Header, deadline, idempotency, retry/unknown-result를 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-SYNC-03-VERIFICATION_EVIDENCE — BAT-CALL-SYNC / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-SYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-CALL-SYNC-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 업무 Domain 동기 호출에 Local/Remote parity, Header, deadline, idempotency, retry/unknown-result를 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-SYNC-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-CALL-SYNC-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-CALL-SYNC-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-CALL-SYNC-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-SYNC-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-CALL-SYNC-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-SYNC-04-FAILURE_RECOVERY — BAT-CALL-SYNC / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-SYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-CALL-SYNC-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 업무 Domain 동기 호출에 Local/Remote parity, Header, deadline, idempotency, retry/unknown-result를 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-SYNC-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-CALL-SYNC-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-CALL-SYNC-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-CALL-SYNC-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-SYNC-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-CALL-SYNC-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP — BAT-CALL-ASYNC / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-ASYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 Event/Outbox 비동기 호출에 stable message ID, retry/DLT, consumer idempotency와 completion correlation을 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER — BAT-CALL-ASYNC / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-ASYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 Event/Outbox 비동기 호출에 stable message ID, retry/DLT, consumer idempotency와 completion correlation을 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-ASYNC-03-VERIFICATION_EVIDENCE — BAT-CALL-ASYNC / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-ASYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-CALL-ASYNC-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 Event/Outbox 비동기 호출에 stable message ID, retry/DLT, consumer idempotency와 completion correlation을 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-ASYNC-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-ASYNC-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-CALL-ASYNC-04-FAILURE_RECOVERY — BAT-CALL-ASYNC / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-CALL-ASYNC` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-CALL-ASYNC-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch/Worker의 Event/Outbox 비동기 호출에 stable message ID, retry/DLT, consumer idempotency와 completion correlation을 적용한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-CALL-ASYNC-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-CALL-ASYNC-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-CALL-ASYNC-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP — BAT-SHARED / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-SHARED` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER — BAT-SHARED / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-SHARED` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-SHARED-03-VERIFICATION_EVIDENCE — BAT-SHARED / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-SHARED` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP`, `CPF-WP-BAT-SHARED-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-SHARED-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BAT-SHARED-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BAT-SHARED-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BAT-SHARED-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-SHARED-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BAT-SHARED-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-SHARED-04-FAILURE_RECOVERY — BAT-SHARED / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-SHARED` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-SHARED-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BAT-SHARED-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BAT-SHARED-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BAT-SHARED-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-SHARED-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BAT-SHARED-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-SHARED-05-OPERATIONS_SECURITY — BAT-SHARED / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-SHARED` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-SHARED-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-BAT-SHARED-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-BAT-SHARED-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-BAT-SHARED-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-SHARED-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-BAT-SHARED-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BAT-SHARED-06-GENERATION_COMPATIBILITY — BAT-SHARED / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `BAT-SHARED` |
| Canonical Owner | `cpf-batch` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-BAT-SHARED-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다.

### 탐색·변경 범위

- cpf-batch, Spring Batch integration, scheduler/agent/worker starters, business facade/event consumers and ADM
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

job/step/item, agent/runner/worker, business domain, broker and ADM operations

### 필수 개발 결과

- [ ] CPF-WP-BAT-SHARED-06-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-BAT-SHARED-06-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-BAT-SHARED-06-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-BAT-SHARED-06-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-BAT-SHARED-06-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-BAT-SHARED-06-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 Spring Batch with shared DB, two processes, kill/restart/reconcile
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- child-process runner, deterministic repository/state and virtual-clock harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
