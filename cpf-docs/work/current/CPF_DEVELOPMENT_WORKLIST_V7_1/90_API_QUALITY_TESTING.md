# CPF V7 상세 개발 Work Package — API·Quality Gates·Testing

- Canonical Requirement: 14개
- 실행 Work Package: 49개
- Canonical IDs: `API-ASYNC`, `API-CONTRACT`, `API-FILE`, `API-PAGING`, `RULE-ARCH`, `RULE-QUALITY`, `RULE-SEC`, `TEST-BROKER`, `TEST-BROWSER`, `TEST-CONTRACT`, `TEST-EVIDENCE`, `TEST-FAULT`, `TEST-RUNTIME`, `TEST-UNIT`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP — API-CONTRACT / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-CONTRACT` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER — API-CONTRACT / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-CONTRACT` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-CONTRACT-03-VERIFICATION_EVIDENCE — API-CONTRACT / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-CONTRACT` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP`, `CPF-WP-API-CONTRACT-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-CONTRACT-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-API-CONTRACT-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-API-CONTRACT-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-API-CONTRACT-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-CONTRACT-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-API-CONTRACT-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-CONTRACT-04-FAILURE_RECOVERY — API-CONTRACT / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-CONTRACT` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-CONTRACT-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-API-CONTRACT-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-API-CONTRACT-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-API-CONTRACT-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-CONTRACT-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-API-CONTRACT-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-CONTRACT-05-OPERATIONS_SECURITY — API-CONTRACT / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-CONTRACT` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-CONTRACT-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-API-CONTRACT-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-API-CONTRACT-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-API-CONTRACT-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-CONTRACT-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-API-CONTRACT-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-CONTRACT-06-GENERATION_COMPATIBILITY — API-CONTRACT / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-CONTRACT` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-API-CONTRACT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-CONTRACT-06-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-API-CONTRACT-06-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-API-CONTRACT-06-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-API-CONTRACT-06-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-CONTRACT-06-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-API-CONTRACT-06-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP — API-PAGING / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-PAGING` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

offset page, slice, keyset/signed cursor, sort/filter allowlist, stable ordering, max size와 count 비용 정책을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER — API-PAGING / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-PAGING` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

offset page, slice, keyset/signed cursor, sort/filter allowlist, stable ordering, max size와 count 비용 정책을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-PAGING-03-VERIFICATION_EVIDENCE — API-PAGING / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-PAGING` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-API-PAGING-01-CONTRACT_OWNERSHIP`, `CPF-WP-API-PAGING-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

offset page, slice, keyset/signed cursor, sort/filter allowlist, stable ordering, max size와 count 비용 정책을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-PAGING-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-API-PAGING-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-API-PAGING-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-API-PAGING-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-PAGING-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-API-PAGING-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP — API-ASYNC / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-ASYNC` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER — API-ASYNC / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-ASYNC` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-ASYNC-03-VERIFICATION_EVIDENCE — API-ASYNC / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-ASYNC` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP`, `CPF-WP-API-ASYNC-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-ASYNC-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-API-ASYNC-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-API-ASYNC-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-API-ASYNC-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-ASYNC-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-API-ASYNC-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-ASYNC-04-FAILURE_RECOVERY — API-ASYNC / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-ASYNC` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-ASYNC-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-API-ASYNC-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-API-ASYNC-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-API-ASYNC-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-ASYNC-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-API-ASYNC-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-ASYNC-05-OPERATIONS_SECURITY — API-ASYNC / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-ASYNC` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-API-ASYNC-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CISA-SBD`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-ASYNC-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-API-ASYNC-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-API-ASYNC-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-API-ASYNC-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-ASYNC-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-API-ASYNC-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP — API-FILE / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-FILE` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-10-CRYPTO-PRIVACY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER — API-FILE / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-FILE` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-10-CRYPTO-PRIVACY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-FILE-03-VERIFICATION_EVIDENCE — API-FILE / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-FILE` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP`, `CPF-WP-API-FILE-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-10-CRYPTO-PRIVACY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-FILE-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-API-FILE-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-API-FILE-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-API-FILE-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-FILE-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-API-FILE-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-FILE-04-FAILURE_RECOVERY — API-FILE / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-FILE` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-FILE-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-API-FILE-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-API-FILE-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-API-FILE-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-FILE-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-API-FILE-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-API-FILE-05-OPERATIONS_SECURITY — API-FILE / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `API-FILE` |
| Canonical Owner | `cpf-core API contract + endpoint owner` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-API-FILE-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-API-FILE-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-API-FILE-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-API-FILE-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-API-FILE-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-API-FILE-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-API-FILE-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-ARCH-01-GATE_ENGINE — RULE-ARCH / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `RULE-ARCH` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Module/package/dependency/owner/internal API/DB access/dual primary/generated drift 위반을 자동 Architecture Gate로 차단한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-ARCH-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-RULE-ARCH-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-RULE-ARCH-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-ARCH-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-ARCH-02-NEGATIVE_FIXTURES — RULE-ARCH / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `RULE-ARCH` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-RULE-ARCH-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Module/package/dependency/owner/internal API/DB access/dual primary/generated drift 위반을 자동 Architecture Gate로 차단한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-ARCH-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-RULE-ARCH-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-RULE-ARCH-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-ARCH-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-ARCH-03-INTEGRATION_ENFORCEMENT — RULE-ARCH / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `RULE-ARCH` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-RULE-ARCH-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Module/package/dependency/owner/internal API/DB access/dual primary/generated drift 위반을 자동 Architecture Gate로 차단한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-ARCH-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-RULE-ARCH-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-RULE-ARCH-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-ARCH-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-SEC-01-GATE_ENGINE — RULE-SEC / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `RULE-SEC` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-12-OBSERVABILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

secret/credential/URL/TLS/security header/unsafe API/path/query/log/evidence pattern과 dependency vulnerability를 자동 Gate로 차단한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-SEC-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-RULE-SEC-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-RULE-SEC-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-SEC-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-SEC-02-NEGATIVE_FIXTURES — RULE-SEC / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `RULE-SEC` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-RULE-SEC-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-12-OBSERVABILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

secret/credential/URL/TLS/security header/unsafe API/path/query/log/evidence pattern과 dependency vulnerability를 자동 Gate로 차단한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-SEC-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-RULE-SEC-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-RULE-SEC-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-SEC-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-SEC-03-INTEGRATION_ENFORCEMENT — RULE-SEC / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `RULE-SEC` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-RULE-SEC-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-12-OBSERVABILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

secret/credential/URL/TLS/security header/unsafe API/path/query/log/evidence pattern과 dependency vulnerability를 자동 Gate로 차단한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-SEC-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-RULE-SEC-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-RULE-SEC-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-SEC-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-QUALITY-01-GATE_ENGINE — RULE-QUALITY / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `RULE-QUALITY` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

compile/static analysis/duplication/dead code/dependency lock/license/SBOM/test coverage/marker-only 구현을 자동 Gate로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-QUALITY-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-RULE-QUALITY-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-RULE-QUALITY-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-QUALITY-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-QUALITY-02-NEGATIVE_FIXTURES — RULE-QUALITY / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `RULE-QUALITY` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-RULE-QUALITY-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

compile/static analysis/duplication/dead code/dependency lock/license/SBOM/test coverage/marker-only 구현을 자동 Gate로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-QUALITY-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-RULE-QUALITY-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-RULE-QUALITY-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-QUALITY-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-RULE-QUALITY-03-INTEGRATION_ENFORCEMENT — RULE-QUALITY / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `RULE-QUALITY` |
| Canonical Owner | `cpf-tools quality gates` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-RULE-QUALITY-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

compile/static analysis/duplication/dead code/dependency lock/license/SBOM/test coverage/marker-only 구현을 자동 Gate로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-RULE-QUALITY-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-RULE-QUALITY-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-RULE-QUALITY-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-RULE-QUALITY-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-UNIT-01-GATE_ENGINE — TEST-UNIT / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-UNIT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순수 로직·validation·state transition·error mapping·serialization·security utility를 deterministic unit test로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-UNIT-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-TEST-UNIT-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-TEST-UNIT-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-UNIT-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-UNIT-02-NEGATIVE_FIXTURES — TEST-UNIT / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-UNIT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-TEST-UNIT-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순수 로직·validation·state transition·error mapping·serialization·security utility를 deterministic unit test로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-UNIT-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-TEST-UNIT-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-TEST-UNIT-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-UNIT-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-UNIT-03-INTEGRATION_ENFORCEMENT — TEST-UNIT / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-UNIT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-TEST-UNIT-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순수 로직·validation·state transition·error mapping·serialization·security utility를 deterministic unit test로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-UNIT-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-TEST-UNIT-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-TEST-UNIT-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-UNIT-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-CONTRACT-01-GATE_ENGINE — TEST-CONTRACT / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-CONTRACT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Public API/SPI, Local/Remote, OpenAPI, message schema, DB query, generated client와 published artifact compatibility를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-CONTRACT-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-TEST-CONTRACT-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-TEST-CONTRACT-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-CONTRACT-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-CONTRACT-02-NEGATIVE_FIXTURES — TEST-CONTRACT / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-CONTRACT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-TEST-CONTRACT-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Public API/SPI, Local/Remote, OpenAPI, message schema, DB query, generated client와 published artifact compatibility를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-CONTRACT-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-TEST-CONTRACT-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-TEST-CONTRACT-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-CONTRACT-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-CONTRACT-03-INTEGRATION_ENFORCEMENT — TEST-CONTRACT / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-CONTRACT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-TEST-CONTRACT-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Public API/SPI, Local/Remote, OpenAPI, message schema, DB query, generated client와 published artifact compatibility를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-CONTRACT-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-TEST-CONTRACT-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-TEST-CONTRACT-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-CONTRACT-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-RUNTIME-01-GATE_ENGINE — TEST-RUNTIME / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-RUNTIME` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

실제 Java25/WAS/DB/Process 환경에서 startup, endpoint, transaction, shutdown, recovery와 resource leak를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-RUNTIME-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-TEST-RUNTIME-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-TEST-RUNTIME-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-RUNTIME-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-RUNTIME-02-NEGATIVE_FIXTURES — TEST-RUNTIME / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-RUNTIME` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-TEST-RUNTIME-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

실제 Java25/WAS/DB/Process 환경에서 startup, endpoint, transaction, shutdown, recovery와 resource leak를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-RUNTIME-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-TEST-RUNTIME-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-TEST-RUNTIME-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-RUNTIME-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-RUNTIME-03-INTEGRATION_ENFORCEMENT — TEST-RUNTIME / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-RUNTIME` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-TEST-RUNTIME-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-13-PERFORMANCE`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

실제 Java25/WAS/DB/Process 환경에서 startup, endpoint, transaction, shutdown, recovery와 resource leak를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-RUNTIME-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-TEST-RUNTIME-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-TEST-RUNTIME-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-RUNTIME-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-BROWSER-01-GATE_ENGINE — TEST-BROWSER / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-BROWSER` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS`, `STD-WCAG` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

ADM/BZA의 Chromium/Firefox/WebKit, route/deep link/session/permission/error/a11y/keyboard/responsive를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-BROWSER-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-TEST-BROWSER-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-TEST-BROWSER-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-BROWSER-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-BROWSER-02-NEGATIVE_FIXTURES — TEST-BROWSER / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-BROWSER` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-TEST-BROWSER-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS`, `STD-WCAG` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

ADM/BZA의 Chromium/Firefox/WebKit, route/deep link/session/permission/error/a11y/keyboard/responsive를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-BROWSER-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-TEST-BROWSER-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-TEST-BROWSER-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-BROWSER-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-BROWSER-03-INTEGRATION_ENFORCEMENT — TEST-BROWSER / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-BROWSER` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-TEST-BROWSER-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS`, `STD-WCAG` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

ADM/BZA의 Chromium/Firefox/WebKit, route/deep link/session/permission/error/a11y/keyboard/responsive를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-BROWSER-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-TEST-BROWSER-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-TEST-BROWSER-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-BROWSER-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-BROKER-01-GATE_ENGINE — TEST-BROKER / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-BROKER` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

실제 Kafka·JMS/IBM MQ·RabbitMQ 지원 Matrix에서 ACK, transaction, duplicate, ordering, redelivery/rebalance, retry/DLQ, broker outage와 consumer crash를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-BROKER-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-TEST-BROKER-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-TEST-BROKER-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-BROKER-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-BROKER-02-NEGATIVE_FIXTURES — TEST-BROKER / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-BROKER` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-TEST-BROKER-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

실제 Kafka·JMS/IBM MQ·RabbitMQ 지원 Matrix에서 ACK, transaction, duplicate, ordering, redelivery/rebalance, retry/DLQ, broker outage와 consumer crash를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-BROKER-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-TEST-BROKER-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-TEST-BROKER-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-BROKER-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-BROKER-03-INTEGRATION_ENFORCEMENT — TEST-BROKER / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-BROKER` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-TEST-BROKER-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

실제 Kafka·JMS/IBM MQ·RabbitMQ 지원 Matrix에서 ACK, transaction, duplicate, ordering, redelivery/rebalance, retry/DLQ, broker outage와 consumer crash를 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-BROKER-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-TEST-BROKER-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-TEST-BROKER-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-BROKER-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-FAULT-01-GATE_ENGINE — TEST-FAULT / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `TEST-FAULT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DB/network/broker/disk/process/time/response loss를 side-effect 전후에 주입해 idempotency·unknown·recovery·compensation을 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-FAULT-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-TEST-FAULT-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-TEST-FAULT-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-FAULT-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-FAULT-02-NEGATIVE_FIXTURES — TEST-FAULT / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `TEST-FAULT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-TEST-FAULT-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DB/network/broker/disk/process/time/response loss를 side-effect 전후에 주입해 idempotency·unknown·recovery·compensation을 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-FAULT-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-TEST-FAULT-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-TEST-FAULT-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-FAULT-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-FAULT-03-INTEGRATION_ENFORCEMENT — TEST-FAULT / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `TEST-FAULT` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-TEST-FAULT-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-NIST-SSDF` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DB/network/broker/disk/process/time/response loss를 side-effect 전후에 주입해 idempotency·unknown·recovery·compensation을 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-FAULT-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-TEST-FAULT-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-TEST-FAULT-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-FAULT-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-EVIDENCE-01-GATE_ENGINE — TEST-EVIDENCE / 품질 Gate Engine

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-EVIDENCE` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `GATE_ENGINE` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

모든 검증의 exact SHA, command, environment, time, exit code, report/artifact hash, sanitized 여부를 schema로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-EVIDENCE-01-GATE_ENGINE-REQ-01: Architecture/Security/Quality 규칙을 재현 가능한 CLI/Gradle/CI Gate로 구현한다.
- [ ] CPF-WP-TEST-EVIDENCE-01-GATE_ENGINE-REQ-02: 경로·설정·Generated/Published Artifact를 실제 분석하고 단순 문자열 Marker에 의존하지 않는다.
- [ ] CPF-WP-TEST-EVIDENCE-01-GATE_ENGINE-REQ-03: False PASS/False FAIL을 회귀 Test로 통제한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-EVIDENCE-01-GATE_ENGINE-PROP-01: 규칙별 stable ID와 machine-readable result schema를 제공한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`valid repository`, `rule violation`, `tool failure`, `unsupported input`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-EVIDENCE-02-NEGATIVE_FIXTURES — TEST-EVIDENCE / 의도적 위반 Fixture

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-EVIDENCE` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `NEGATIVE_FIXTURES` |
| 선행 작업 | `CPF-WP-TEST-EVIDENCE-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

모든 검증의 exact SHA, command, environment, time, exit code, report/artifact hash, sanitized 여부를 schema로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-EVIDENCE-02-NEGATIVE_FIXTURES-REQ-01: 각 Gate가 잡아야 할 실제 위반을 최소 1개 이상 Fixture로 만들고 실패를 확인한다.
- [ ] CPF-WP-TEST-EVIDENCE-02-NEGATIVE_FIXTURES-REQ-02: Fixture가 제품 Source에 유입되거나 기본 Build를 오염시키지 않게 격리한다.
- [ ] CPF-WP-TEST-EVIDENCE-02-NEGATIVE_FIXTURES-REQ-03: 위반 수정 후 PASS 전환도 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-EVIDENCE-02-NEGATIVE_FIXTURES-PROP-01: 작은 fixture repository/module과 expected finding ID를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`expected fail`, `unexpected pass`, `fixed pass`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-TEST-EVIDENCE-03-INTEGRATION_ENFORCEMENT — TEST-EVIDENCE / CI·Publication·Release 차단 연계

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `TEST-EVIDENCE` |
| Canonical Owner | `repository-wide test ownership` |
| Work Package 유형 | `INTEGRATION_ENFORCEMENT` |
| 선행 작업 | `CPF-WP-TEST-EVIDENCE-01-GATE_ENGINE` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `2b259ea45f3ad1a93bfefd5e7d3bc35f3406bf06` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

모든 검증의 exact SHA, command, environment, time, exit code, report/artifact hash, sanitized 여부를 schema로 검증한다.

### 탐색·변경 범위

- OpenAPI/AsyncAPI contracts, generated clients, quality scripts, test kits, CI fixtures and reports
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

all endpoint/message owners, generated consumers, CI, Codex and QA

### 필수 개발 결과

- [ ] CPF-WP-TEST-EVIDENCE-03-INTEGRATION_ENFORCEMENT-REQ-01: Gate가 local, CI, publication/release에서 동일 기준으로 동작하게 한다.
- [ ] CPF-WP-TEST-EVIDENCE-03-INTEGRATION_ENFORCEMENT-REQ-02: 도구 실패·결과 누락·지원하지 않는 stack은 fail-closed 정책을 적용한다.
- [ ] CPF-WP-TEST-EVIDENCE-03-INTEGRATION_ENFORCEMENT-REQ-03: 예외/waiver는 owner, reason, expiry, scope와 audit를 요구한다.

### 비강제 구현 제안

- [ ] CPF-WP-TEST-EVIDENCE-03-INTEGRATION_ENFORCEMENT-PROP-01: fast local profile과 authoritative release profile을 분리한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`CI failure`, `tool unavailable`, `expired waiver`, `release block`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- schema/client generation, Java/browser/broker/fault matrices and negative fixtures
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- parser/compiler/mock/embedded/state harness with explicit remaining gaps
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
