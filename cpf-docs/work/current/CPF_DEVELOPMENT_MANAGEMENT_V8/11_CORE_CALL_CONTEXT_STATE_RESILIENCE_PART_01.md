# CPF V8 상세 개발 Work Package — Core Call·Context·State·Lock·Resilience

- Canonical Requirement: 16개
- 실행 Work Package: 74개
- Canonical IDs: `CPF-CALL`, `CPF-CONTEXT`, `CPF-DEADLINE`, `CPF-ERROR`, `CPF-HEADER`, `CPF-HEALTH`, `CPF-IDEMP`, `CPF-LOCK`, `CPF-REGISTRY`, `CPF-RESILIENCE`, `CPF-ROLE`, `CPF-ROUTING`, `CPF-SCHED`, `CPF-STATE`, `CPF-TXID`, `CPF-VALID`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## 분할 파일 정보

- 원본 영역 Index: `11_CORE_CALL_CONTEXT_STATE_RESILIENCE.md`
- Part: 1/2
- Work Package: 52개

## CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP — CPF-CALL / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CALL` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

동일 JVM과 분리 WAS 호출에 동일한 Header·권한·timeout budget·오류·추적·idempotency를 적용하고 내부 호출의 Gateway 재경유를 금지한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER — CPF-CALL / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CALL` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

동일 JVM과 분리 WAS 호출에 동일한 Header·권한·timeout budget·오류·추적·idempotency를 적용하고 내부 호출의 Gateway 재경유를 금지한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CALL-03-VERIFICATION_EVIDENCE — CPF-CALL / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CALL` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-CALL-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

동일 JVM과 분리 WAS 호출에 동일한 Header·권한·timeout budget·오류·추적·idempotency를 적용하고 내부 호출의 Gateway 재경유를 금지한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CALL-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-CALL-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-CALL-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-CALL-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CALL-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-CALL-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CALL-04-FAILURE_RECOVERY — CPF-CALL / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CALL` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-CALL-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

동일 JVM과 분리 WAS 호출에 동일한 Header·권한·timeout budget·오류·추적·idempotency를 적용하고 내부 호출의 Gateway 재경유를 금지한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CALL-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-CALL-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-CALL-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-CALL-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CALL-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-CALL-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP — CPF-REGISTRY / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-REGISTRY` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER — CPF-REGISTRY / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-REGISTRY` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-REGISTRY-03-VERIFICATION_EVIDENCE — CPF-REGISTRY / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-REGISTRY` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-REGISTRY-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-REGISTRY-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-REGISTRY-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-REGISTRY-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-REGISTRY-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-REGISTRY-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-REGISTRY-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-REGISTRY-04-FAILURE_RECOVERY — CPF-REGISTRY / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-REGISTRY` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-REGISTRY-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-REGISTRY-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-REGISTRY-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-REGISTRY-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-REGISTRY-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-REGISTRY-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-REGISTRY-05-OPERATIONS_SECURITY — CPF-REGISTRY / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-REGISTRY` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-REGISTRY-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-CPF-REGISTRY-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-CPF-REGISTRY-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-CPF-REGISTRY-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-REGISTRY-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-CPF-REGISTRY-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-REGISTRY-06-GENERATION_COMPATIBILITY — CPF-REGISTRY / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-REGISTRY` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-CPF-REGISTRY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-REGISTRY-06-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-CPF-REGISTRY-06-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-CPF-REGISTRY-06-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-CPF-REGISTRY-06-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-REGISTRY-06-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-CPF-REGISTRY-06-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP — CPF-ROUTING / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROUTING` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER — CPF-ROUTING / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROUTING` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROUTING-03-VERIFICATION_EVIDENCE — CPF-ROUTING / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROUTING` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-ROUTING-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROUTING-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-ROUTING-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-ROUTING-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-ROUTING-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROUTING-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-ROUTING-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROUTING-04-FAILURE_RECOVERY — CPF-ROUTING / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROUTING` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROUTING-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-ROUTING-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-ROUTING-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-ROUTING-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROUTING-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-ROUTING-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROUTING-05-OPERATIONS_SECURITY — CPF-ROUTING / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROUTING` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROUTING-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-CPF-ROUTING-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-CPF-ROUTING-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-CPF-ROUTING-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROUTING-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-CPF-ROUTING-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROUTING-06-GENERATION_COMPATIBILITY — CPF-ROUTING / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROUTING` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-CPF-ROUTING-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROUTING-06-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-CPF-ROUTING-06-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-CPF-ROUTING-06-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-CPF-ROUTING-06-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROUTING-06-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-CPF-ROUTING-06-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP — CPF-HEALTH / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEALTH` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER — CPF-HEALTH / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEALTH` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEALTH-03-VERIFICATION_EVIDENCE — CPF-HEALTH / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEALTH` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-HEALTH-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEALTH-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-HEALTH-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-HEALTH-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-HEALTH-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEALTH-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-HEALTH-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEALTH-04-OPERATIONS_SECURITY — CPF-HEALTH / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEALTH` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEALTH-04-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-CPF-HEALTH-04-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-CPF-HEALTH-04-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-CPF-HEALTH-04-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEALTH-04-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-CPF-HEALTH-04-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEALTH-05-DATA_MIGRATION — CPF-HEALTH / DB·Query·Generator·Vendor Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEALTH` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `DATA_MIGRATION` |
| 선행 작업 | `CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEALTH-05-DATA_MIGRATION-REQ-01: DB/SQL/Query 영향 여부를 반드시 판정하고 N/A면 실제 호출 경로 근거를 기록한다.
- [ ] CPF-WP-CPF-HEALTH-05-DATA_MIGRATION-REQ-02: Canonical Model/Query Contract→Generator/Template→Generated SQL/Mapper→Fresh Init/Seed→Migration/Rollback/Reapply 순서로 변경한다.
- [ ] CPF-WP-CPF-HEALTH-05-DATA_MIGRATION-REQ-03: MariaDB/PostgreSQL/Oracle의 type/default/paging/locking/error/index/FK/transaction 차이를 전수 반영한다.
- [ ] CPF-WP-CPF-HEALTH-05-DATA_MIGRATION-REQ-04: Repository/Mapper/DTO/Service/API/Frontend/Batch/ADM/BZA/Sample/EDU 소비자를 모두 점검한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEALTH-05-DATA_MIGRATION-PROP-01: Vendor-native SQL을 유지하되 Canonical Metadata에서 생성/검증 가능한 구조를 우선한다.
- [ ] CPF-WP-CPF-HEALTH-05-DATA_MIGRATION-PROP-02: 파괴적 변경은 expand→migrate→contract와 compatibility window를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`fresh install`, `double seed`, `upgrade`, `migration interruption`, `rollback/forward recovery`, `drift`, `vendor difference`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEALTH-06-GENERATION_COMPATIBILITY — CPF-HEALTH / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEALTH` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-CPF-HEALTH-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-12-OBSERVABILITY`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEALTH-06-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-CPF-HEALTH-06-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-CPF-HEALTH-06-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-CPF-HEALTH-06-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEALTH-06-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-CPF-HEALTH-06-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP — CPF-HEADER / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEADER` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-10-CRYPTO-PRIVACY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

표준/확장 Header의 이름·형식·신뢰경계·생성자·전파·masking·최대크기·호환성을 정본화하고 spoofing을 차단한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER — CPF-HEADER / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEADER` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-10-CRYPTO-PRIVACY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

표준/확장 Header의 이름·형식·신뢰경계·생성자·전파·masking·최대크기·호환성을 정본화하고 spoofing을 차단한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-HEADER-03-VERIFICATION_EVIDENCE — CPF-HEADER / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-HEADER` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-HEADER-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-HEADER-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-10-CRYPTO-PRIVACY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

표준/확장 Header의 이름·형식·신뢰경계·생성자·전파·masking·최대크기·호환성을 정본화하고 spoofing을 차단한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-HEADER-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-HEADER-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-HEADER-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-HEADER-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-HEADER-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-HEADER-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP — CPF-CONTEXT / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CONTEXT` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction, trace, segment, caller, principal, environment, channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER — CPF-CONTEXT / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CONTEXT` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction, trace, segment, caller, principal, environment, channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CONTEXT-03-VERIFICATION_EVIDENCE — CPF-CONTEXT / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CONTEXT` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-CONTEXT-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction, trace, segment, caller, principal, environment, channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CONTEXT-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-CONTEXT-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-CONTEXT-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-CONTEXT-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CONTEXT-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-CONTEXT-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CONTEXT-04-FAILURE_RECOVERY — CPF-CONTEXT / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CONTEXT` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction, trace, segment, caller, principal, environment, channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CONTEXT-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-CONTEXT-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-CONTEXT-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-CONTEXT-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CONTEXT-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-CONTEXT-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-CONTEXT-05-OPERATIONS_SECURITY — CPF-CONTEXT / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-CONTEXT` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-CPF-CONTEXT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction, trace, segment, caller, principal, environment, channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-CONTEXT-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-CPF-CONTEXT-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-CPF-CONTEXT-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-CPF-CONTEXT-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-CONTEXT-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-CPF-CONTEXT-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP — CPF-TXID / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-TXID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

34자리 transactionId 단일 정본을 생성·검증·승계하며 Local/Remote/Async/Batch/File/Log/ADM Timeline이 같은 값을 사용한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER — CPF-TXID / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-TXID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

34자리 transactionId 단일 정본을 생성·검증·승계하며 Local/Remote/Async/Batch/File/Log/ADM Timeline이 같은 값을 사용한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-TXID-03-VERIFICATION_EVIDENCE — CPF-TXID / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-TXID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-TXID-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

34자리 transactionId 단일 정본을 생성·검증·승계하며 Local/Remote/Async/Batch/File/Log/ADM Timeline이 같은 값을 사용한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-TXID-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-TXID-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-TXID-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-TXID-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-TXID-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-TXID-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-TXID-04-FAILURE_RECOVERY — CPF-TXID / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-TXID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

34자리 transactionId 단일 정본을 생성·검증·승계하며 Local/Remote/Async/Batch/File/Log/ADM Timeline이 같은 값을 사용한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-TXID-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-TXID-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-TXID-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-TXID-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-TXID-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-TXID-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-TXID-05-OPERATIONS_SECURITY — CPF-TXID / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-TXID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-CPF-TXID-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CISA-SBD`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

34자리 transactionId 단일 정본을 생성·검증·승계하며 Local/Remote/Async/Batch/File/Log/ADM Timeline이 같은 값을 사용한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-TXID-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-CPF-TXID-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-CPF-TXID-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-CPF-TXID-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-TXID-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-CPF-TXID-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP — CPF-ROLE / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROLE` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER — CPF-ROLE / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROLE` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROLE-03-VERIFICATION_EVIDENCE — CPF-ROLE / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROLE` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-ROLE-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROLE-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-ROLE-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-ROLE-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-ROLE-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROLE-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-ROLE-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROLE-04-FAILURE_RECOVERY — CPF-ROLE / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROLE` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OTEL`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROLE-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-ROLE-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-ROLE-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-ROLE-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROLE-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-ROLE-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ROLE-05-OPERATIONS_SECURITY — CPF-ROLE / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ROLE` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-CPF-ROLE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ROLE-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-CPF-ROLE-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-CPF-ROLE-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-CPF-ROLE-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ROLE-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-CPF-ROLE-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP — CPF-ERROR / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ERROR` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

표준 오류 코드·HTTP/Protocol mapping·retryability·unknown-result·field error·operator message를 버전 가능한 계약으로 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER — CPF-ERROR / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ERROR` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

표준 오류 코드·HTTP/Protocol mapping·retryability·unknown-result·field error·operator message를 버전 가능한 계약으로 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ERROR-03-VERIFICATION_EVIDENCE — CPF-ERROR / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ERROR` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-ERROR-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

표준 오류 코드·HTTP/Protocol mapping·retryability·unknown-result·field error·operator message를 버전 가능한 계약으로 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ERROR-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-ERROR-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-ERROR-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-ERROR-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ERROR-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-ERROR-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-ERROR-04-FAILURE_RECOVERY — CPF-ERROR / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-ERROR` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-ERROR-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

표준 오류 코드·HTTP/Protocol mapping·retryability·unknown-result·field error·operator message를 버전 가능한 계약으로 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-ERROR-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-ERROR-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-ERROR-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-ERROR-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-ERROR-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-ERROR-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP — CPF-VALID / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-VALID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

입력·출력·설정·Header·파일·메시지·SQL parameter 검증과 오류 위치, allowlist, 크기·깊이·개수 상한을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER — CPF-VALID / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-VALID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

입력·출력·설정·Header·파일·메시지·SQL parameter 검증과 오류 위치, allowlist, 크기·깊이·개수 상한을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-VALID-03-VERIFICATION_EVIDENCE — CPF-VALID / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-VALID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-VALID-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

입력·출력·설정·Header·파일·메시지·SQL parameter 검증과 오류 위치, allowlist, 크기·깊이·개수 상한을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-VALID-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-VALID-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-VALID-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-VALID-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-VALID-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-VALID-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-VALID-04-DATA_MIGRATION — CPF-VALID / DB·Query·Generator·Vendor Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `CPF-VALID` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `DATA_MIGRATION` |
| 선행 작업 | `CPF-WP-CPF-VALID-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

입력·출력·설정·Header·파일·메시지·SQL parameter 검증과 오류 위치, allowlist, 크기·깊이·개수 상한을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-VALID-04-DATA_MIGRATION-REQ-01: DB/SQL/Query 영향 여부를 반드시 판정하고 N/A면 실제 호출 경로 근거를 기록한다.
- [ ] CPF-WP-CPF-VALID-04-DATA_MIGRATION-REQ-02: Canonical Model/Query Contract→Generator/Template→Generated SQL/Mapper→Fresh Init/Seed→Migration/Rollback/Reapply 순서로 변경한다.
- [ ] CPF-WP-CPF-VALID-04-DATA_MIGRATION-REQ-03: MariaDB/PostgreSQL/Oracle의 type/default/paging/locking/error/index/FK/transaction 차이를 전수 반영한다.
- [ ] CPF-WP-CPF-VALID-04-DATA_MIGRATION-REQ-04: Repository/Mapper/DTO/Service/API/Frontend/Batch/ADM/BZA/Sample/EDU 소비자를 모두 점검한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-VALID-04-DATA_MIGRATION-PROP-01: Vendor-native SQL을 유지하되 Canonical Metadata에서 생성/검증 가능한 구조를 우선한다.
- [ ] CPF-WP-CPF-VALID-04-DATA_MIGRATION-PROP-02: 파괴적 변경은 expand→migrate→contract와 compatibility window를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`fresh install`, `double seed`, `upgrade`, `migration interruption`, `rollback/forward recovery`, `drift`, `vendor difference`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP — CPF-IDEMP / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `CPF-IDEMP` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

canonical request hash, scope, TTL, result replay, conflict semantics와 concurrent race를 포함한 idempotency 원장을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER — CPF-IDEMP / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `CPF-IDEMP` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

canonical request hash, scope, TTL, result replay, conflict semantics와 concurrent race를 포함한 idempotency 원장을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-IDEMP-03-VERIFICATION_EVIDENCE — CPF-IDEMP / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `CPF-IDEMP` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP`, `CPF-WP-CPF-IDEMP-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

canonical request hash, scope, TTL, result replay, conflict semantics와 concurrent race를 포함한 idempotency 원장을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-IDEMP-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-CPF-IDEMP-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-CPF-IDEMP-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-CPF-IDEMP-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-IDEMP-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-CPF-IDEMP-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---

## CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY — CPF-IDEMP / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `CPF-IDEMP` |
| Canonical Owner | `cpf-core / repository architecture` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-CPF-IDEMP-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

canonical request hash, scope, TTL, result replay, conflict semantics와 concurrent race를 포함한 idempotency 원장을 제공한다.

### 탐색·변경 범위

- cpf-core API/SPI/internal plus leaf starters/providers and actual service/batch/gateway consumers
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

Local/Remote facade, registry/routing, async/batch propagation, stateful retry/recovery consumers

### 필수 개발 결과

- [ ] CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-CPF-IDEMP-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- Java 25 runtime, multi-process shared-state, timeout/cancel/fault injection
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- javac contract consumer, virtual clock, barrier and child-process state harness
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
