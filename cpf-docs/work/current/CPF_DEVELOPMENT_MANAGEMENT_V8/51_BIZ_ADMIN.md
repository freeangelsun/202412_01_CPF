# CPF V8 상세 개발 Work Package — BZA Business Administration

- Canonical Requirement: 4개
- 실행 Work Package: 18개
- Canonical IDs: `BZA-APPROVAL`, `BZA-BUSINESS`, `BZA-ORG`, `BZA-SEQUENCE-SAMPLE`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP — BZA-BUSINESS / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-BUSINESS` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER — BZA-BUSINESS / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-BUSINESS` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-BUSINESS-03-VERIFICATION_EVIDENCE — BZA-BUSINESS / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-BUSINESS` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP`, `CPF-WP-BZA-BUSINESS-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-BUSINESS-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BZA-BUSINESS-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BZA-BUSINESS-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BZA-BUSINESS-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-BUSINESS-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BZA-BUSINESS-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-BUSINESS-04-FAILURE_RECOVERY — BZA-BUSINESS / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-BUSINESS` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-BUSINESS-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BZA-BUSINESS-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BZA-BUSINESS-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BZA-BUSINESS-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-BUSINESS-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BZA-BUSINESS-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-BUSINESS-05-OPERATIONS_SECURITY — BZA-BUSINESS / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-BUSINESS` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-BZA-BUSINESS-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-BUSINESS-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-BZA-BUSINESS-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-BZA-BUSINESS-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-BZA-BUSINESS-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-BUSINESS-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-BZA-BUSINESS-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP — BZA-ORG / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-ORG` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER — BZA-ORG / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-ORG` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-ORG-03-VERIFICATION_EVIDENCE — BZA-ORG / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-ORG` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP`, `CPF-WP-BZA-ORG-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-ORG-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BZA-ORG-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BZA-ORG-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BZA-ORG-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-ORG-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BZA-ORG-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-ORG-04-FAILURE_RECOVERY — BZA-ORG / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-ORG` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-ORG-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BZA-ORG-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BZA-ORG-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BZA-ORG-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-ORG-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BZA-ORG-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-ORG-05-OPERATIONS_SECURITY — BZA-ORG / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-ORG` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-BZA-ORG-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-ORG-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-BZA-ORG-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-BZA-ORG-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-BZA-ORG-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-ORG-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-BZA-ORG-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP — BZA-APPROVAL / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-APPROVAL` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER — BZA-APPROVAL / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-APPROVAL` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-APPROVAL-03-VERIFICATION_EVIDENCE — BZA-APPROVAL / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-APPROVAL` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP`, `CPF-WP-BZA-APPROVAL-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-APPROVAL-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-BZA-APPROVAL-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-BZA-APPROVAL-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-BZA-APPROVAL-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-APPROVAL-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-BZA-APPROVAL-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-APPROVAL-04-FAILURE_RECOVERY — BZA-APPROVAL / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-APPROVAL` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-APPROVAL-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-BZA-APPROVAL-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-BZA-APPROVAL-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-BZA-APPROVAL-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-APPROVAL-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-BZA-APPROVAL-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-APPROVAL-05-OPERATIONS_SECURITY — BZA-APPROVAL / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-APPROVAL` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-BZA-APPROVAL-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-06-STATE-IDEMP`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-OWASP-ASVS`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-APPROVAL-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-BZA-APPROVAL-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-BZA-APPROVAL-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-BZA-APPROVAL-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-APPROVAL-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-BZA-APPROVAL-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-SEQUENCE-SAMPLE-01-REFERENCE_RUNTIME — BZA-SEQUENCE-SAMPLE / Reference 실제 Runtime

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-SEQUENCE-SAMPLE` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `REFERENCE_RUNTIME` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

업무 채번을 선택형 Customization Sample로 제공하되 기본 Runtime 의존을 만들지 않고 규칙·시험·승인·audit를 교육한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-01-REFERENCE_RUNTIME-REQ-01: Sample/Reference가 제품 Public API와 Published Artifact를 실제 소비한다.
- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-01-REFERENCE_RUNTIME-REQ-02: 별도 장난감 Contract나 Sample-only Engine을 만들지 않는다.
- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-01-REFERENCE_RUNTIME-REQ-03: 정상·오류·권한·복구·운영 흐름을 실행 가능하게 제공한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-01-REFERENCE_RUNTIME-PROP-01: 작은 Golden scenario와 표준 Test Kit를 재사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal`, `validation`, `authorization`, `failure`, `recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-SEQUENCE-SAMPLE-02-FAILURE_PARITY — BZA-SEQUENCE-SAMPLE / Reference 오류·복구 Parity

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-SEQUENCE-SAMPLE` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `FAILURE_PARITY` |
| 선행 작업 | `CPF-WP-BZA-SEQUENCE-SAMPLE-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

업무 채번을 선택형 Customization Sample로 제공하되 기본 Runtime 의존을 만들지 않고 규칙·시험·승인·audit를 교육한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-02-FAILURE_PARITY-REQ-01: Reference가 제품 Runtime과 동일한 error taxonomy, retry/unknown/recovery와 audit를 사용한다.
- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-02-FAILURE_PARITY-REQ-02: Local/Remote 또는 provider profile 차이를 동일 업무 계약으로 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-02-FAILURE_PARITY-PROP-01: fault profile과 documented operator workflow를 포함한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`local/remote parity`, `dependency failure`, `unknown`, `manual recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-BZA-SEQUENCE-SAMPLE-03-REGENERATION_EVIDENCE — BZA-SEQUENCE-SAMPLE / Sample·Golden 재생성 Evidence

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `BZA-SEQUENCE-SAMPLE` |
| Canonical Owner | `cpf-biz-admin` |
| Work Package 유형 | `REGENERATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-BZA-SEQUENCE-SAMPLE-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-11-OPS-AUDIT`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

업무 채번을 선택형 Customization Sample로 제공하되 기본 Runtime 의존을 만들지 않고 규칙·시험·승인·audit를 교육한다.

### 탐색·변경 범위

- cpf-biz-admin backend/frontend/contracts, organization/approval domain adapters and generated clients
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

business administrator and owning business domains

### 필수 개발 결과

- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-03-REGENERATION_EVIDENCE-REQ-01: Generator 입력에서 Sample/Golden Domain을 재생성하고 normalized parity를 확인한다.
- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-03-REGENERATION_EVIDENCE-REQ-02: 사용자 영역과 교육 문서가 재생성 후 보존된다.

### 비강제 구현 제안

- [ ] CPF-WP-BZA-SEQUENCE-SAMPLE-03-REGENERATION_EVIDENCE-PROP-01: remove/regenerate CI profile과 golden diff를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`remove/regenerate`, `user edit preserve`, `drift`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- server authorization, 3-vendor persistence and browser workflow
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- contract/state/approval concurrency and frontend mock workflow
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
