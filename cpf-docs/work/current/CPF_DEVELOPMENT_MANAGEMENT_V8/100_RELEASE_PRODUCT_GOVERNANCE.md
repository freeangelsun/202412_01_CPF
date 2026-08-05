# CPF V8 상세 개발 Work Package — Release·Supply Chain·Product·Governance

- Canonical Requirement: 14개
- 실행 Work Package: 52개
- Canonical IDs: `DOC-GOV`, `DOC-PRODUCT`, `PROD-EDITION`, `PROD-MULTITENANT`, `PROD-PACKAGE`, `PROD-PLUGIN`, `REL-BUILD`, `REL-COMPAT`, `REL-DEPLOY`, `REL-MIG`, `REQ-CODEX`, `REQ-GAP`, `REQ-GOV`, `REQ-REVIEW`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP — REL-BUILD / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-BUILD` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

fresh clone과 clean cache에서 Java25/Gradle build, LOCAL_DEV/REMOTE/OFFLINE resolution, lock/POM/BOM/source/javadoc/reproducible artifact를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER — REL-BUILD / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-BUILD` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

fresh clone과 clean cache에서 Java25/Gradle build, LOCAL_DEV/REMOTE/OFFLINE resolution, lock/POM/BOM/source/javadoc/reproducible artifact를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE — REL-BUILD / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-BUILD` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP`, `CPF-WP-REL-BUILD-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

fresh clone과 clean cache에서 Java25/Gradle build, LOCAL_DEV/REMOTE/OFFLINE resolution, lock/POM/BOM/source/javadoc/reproducible artifact를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-REL-BUILD-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-BUILD-04-FAILURE_RECOVERY — REL-BUILD / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-BUILD` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

fresh clone과 clean cache에서 Java25/Gradle build, LOCAL_DEV/REMOTE/OFFLINE resolution, lock/POM/BOM/source/javadoc/reproducible artifact를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-BUILD-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-REL-BUILD-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-REL-BUILD-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-REL-BUILD-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-BUILD-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-REL-BUILD-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-BUILD-05-GENERATION_COMPATIBILITY — REL-BUILD / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-BUILD` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-REL-BUILD-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

fresh clone과 clean cache에서 Java25/Gradle build, LOCAL_DEV/REMOTE/OFFLINE resolution, lock/POM/BOM/source/javadoc/reproducible artifact를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-BUILD-05-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-REL-BUILD-05-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-REL-BUILD-05-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-REL-BUILD-05-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-BUILD-05-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-REL-BUILD-05-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP — REL-DEPLOY / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `REL-DEPLOY` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER — REL-DEPLOY / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `REL-DEPLOY` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-DEPLOY-03-VERIFICATION_EVIDENCE — REL-DEPLOY / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `REL-DEPLOY` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP`, `CPF-WP-REL-DEPLOY-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-DEPLOY-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-REL-DEPLOY-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-REL-DEPLOY-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-REL-DEPLOY-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-DEPLOY-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-REL-DEPLOY-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-DEPLOY-04-FAILURE_RECOVERY — REL-DEPLOY / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `REL-DEPLOY` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-DEPLOY-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-REL-DEPLOY-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-REL-DEPLOY-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-REL-DEPLOY-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-DEPLOY-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-REL-DEPLOY-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-DEPLOY-05-OPERATIONS_SECURITY — REL-DEPLOY / 보안·승인·감사·운영

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `REL-DEPLOY` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `OPERATIONS_SECURITY` |
| 선행 작업 | `CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-09-SECURITY`, `GATE-11-OPS-AUDIT`, `GATE-12-OBSERVABILITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-DEPLOY-05-OPERATIONS_SECURITY-REQ-01: AuthN/AuthZ, object/property/function 권한, trust boundary, secure default와 입력/자원 한도를 적용한다.
- [ ] CPF-WP-REL-DEPLOY-05-OPERATIONS_SECURITY-REQ-02: Secret/PII/credential masking, raw access, download/export와 Evidence sanitization을 적용한다.
- [ ] CPF-WP-REL-DEPLOY-05-OPERATIONS_SECURITY-REQ-03: 조회·상태·오류·timeline·control·reason·approval·SoD·expiry·immutable audit를 제공한다.
- [ ] CPF-WP-REL-DEPLOY-05-OPERATIONS_SECURITY-REQ-04: Metric/alert/runbook와 자동복구 allowlist/attempt limit/rollback/escalation을 연결한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-DEPLOY-05-OPERATIONS_SECURITY-PROP-01: Owner Command/Query API를 통해 운영하고 ADM/BZA가 Owner DB를 직접 갱신하지 않게 한다.
- [ ] CPF-WP-REL-DEPLOY-05-OPERATIONS_SECURITY-PROP-02: OWASP ASVS/API Security negative scenario를 관련 Test Kit에 매핑한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unauthenticated`, `unauthorized object/property`, `approval expired`, `self approval`, `secret leak`, `unsafe operation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-DEPLOY-06-GENERATION_COMPATIBILITY — REL-DEPLOY / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P0** |
| Canonical Requirement | `REL-DEPLOY` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-REL-DEPLOY-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-12-OBSERVABILITY`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-DEPLOY-06-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-REL-DEPLOY-06-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-REL-DEPLOY-06-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-REL-DEPLOY-06-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-DEPLOY-06-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-REL-DEPLOY-06-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP — REL-MIG / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-MIG` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER — REL-MIG / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-MIG` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-MIG-03-VERIFICATION_EVIDENCE — REL-MIG / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-MIG` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP`, `CPF-WP-REL-MIG-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-MIG-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-REL-MIG-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-REL-MIG-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-REL-MIG-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-MIG-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-REL-MIG-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-MIG-04-FAILURE_RECOVERY — REL-MIG / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-MIG` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-MIG-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-REL-MIG-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-REL-MIG-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-REL-MIG-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-MIG-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-REL-MIG-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-MIG-05-DATA_MIGRATION — REL-MIG / DB·Query·Generator·Vendor Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-MIG` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `DATA_MIGRATION` |
| 선행 작업 | `CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-MIG-05-DATA_MIGRATION-REQ-01: DB/SQL/Query 영향 여부를 반드시 판정하고 N/A면 실제 호출 경로 근거를 기록한다.
- [ ] CPF-WP-REL-MIG-05-DATA_MIGRATION-REQ-02: Canonical Model/Query Contract→Generator/Template→Generated SQL/Mapper→Fresh Init/Seed→Migration/Rollback/Reapply 순서로 변경한다.
- [ ] CPF-WP-REL-MIG-05-DATA_MIGRATION-REQ-03: MariaDB/PostgreSQL/Oracle의 type/default/paging/locking/error/index/FK/transaction 차이를 전수 반영한다.
- [ ] CPF-WP-REL-MIG-05-DATA_MIGRATION-REQ-04: Repository/Mapper/DTO/Service/API/Frontend/Batch/ADM/BZA/Sample/EDU 소비자를 모두 점검한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-MIG-05-DATA_MIGRATION-PROP-01: Vendor-native SQL을 유지하되 Canonical Metadata에서 생성/검증 가능한 구조를 우선한다.
- [ ] CPF-WP-REL-MIG-05-DATA_MIGRATION-PROP-02: 파괴적 변경은 expand→migrate→contract와 compatibility window를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`fresh install`, `double seed`, `upgrade`, `migration interruption`, `rollback/forward recovery`, `drift`, `vendor difference`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-MIG-06-GENERATION_COMPATIBILITY — REL-MIG / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-MIG` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-REL-MIG-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-04-EVENT-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-ASYNCAPI`, `STD-CLOUDEVENTS`, `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-MIG-06-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-REL-MIG-06-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-REL-MIG-06-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-REL-MIG-06-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-MIG-06-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-REL-MIG-06-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP — REL-COMPAT / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-COMPAT` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER — REL-COMPAT / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-COMPAT` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-COMPAT-03-VERIFICATION_EVIDENCE — REL-COMPAT / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-COMPAT` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP`, `CPF-WP-REL-COMPAT-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-COMPAT-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-REL-COMPAT-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-REL-COMPAT-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-REL-COMPAT-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-COMPAT-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-REL-COMPAT-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-COMPAT-04-FAILURE_RECOVERY — REL-COMPAT / 오류·부분 실패·UNKNOWN·복구

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-COMPAT` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `FAILURE_RECOVERY` |
| 선행 작업 | `CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-COMPAT-04-FAILURE_RECOVERY-REQ-01: 정상, invalid, timeout, cancellation, dependency failure, partial failure와 resource exhaustion을 구현한다.
- [ ] CPF-WP-REL-COMPAT-04-FAILURE_RECOVERY-REQ-02: side-effect 전/후, commit/send/ACK/response loss의 UNKNOWN을 구분하고 durable state에 기록한다.
- [ ] CPF-WP-REL-COMPAT-04-FAILURE_RECOVERY-REQ-03: Retry budget, idempotency, lease/fencing, restart/reclaim, reconcile/compensation/manual resolution을 적용한다.
- [ ] CPF-WP-REL-COMPAT-04-FAILURE_RECOVERY-REQ-04: 다중 JVM/process와 process-kill 이후 중복·stale writer·blind retry를 방지한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-COMPAT-04-FAILURE_RECOVERY-PROP-01: 명시적 상태 전이와 가상 Clock/Failure Injection 가능한 구조를 사용한다.
- [ ] CPF-WP-REL-COMPAT-04-FAILURE_RECOVERY-PROP-02: 공통 CPF resilience/locking/reconciliation 계약을 재사용하고 기능별 복제 Engine을 만들지 않는다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`timeout`, `cancel`, `dependency outage`, `partial failure`, `unknown result`, `duplicate`, `process kill`, `reconcile`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REL-COMPAT-05-GENERATION_COMPATIBILITY — REL-COMPAT / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P1** |
| Canonical Requirement | `REL-COMPAT` |
| Canonical Owner | `cpf-tools release/deploy` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-REL-COMPAT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-GRADLE-JAVA`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA`, `STD-SPRING` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REL-COMPAT-05-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-REL-COMPAT-05-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-REL-COMPAT-05-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-REL-COMPAT-05-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-REL-COMPAT-05-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-REL-COMPAT-05-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DOC-GOV-01-CANONICAL_MODEL — DOC-GOV / 정본·ID·상태 모델

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DOC-GOV` |
| Canonical Owner | `cpf-docs + source owner` |
| Work Package 유형 | `CANONICAL_MODEL` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Final Target, ADR, Requirement Continuity, Current Request, Review, Handover의 역할·정본·폐기·변경 승인 규칙을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-DOC-GOV-01-CANONICAL_MODEL-REQ-01: Canonical Requirement/Scenario ID, owner, acceptance, continuity와 상태 규칙을 단일 정본으로 유지한다.
- [ ] CPF-WP-DOC-GOV-01-CANONICAL_MODEL-REQ-02: Alias/split/supersede/deprecate 관계와 count 변화를 추적한다.
- [ ] CPF-WP-DOC-GOV-01-CANONICAL_MODEL-REQ-03: 169/162 등 count 불일치를 자동 차단한다.

### 비강제 구현 제안

- [ ] CPF-WP-DOC-GOV-01-CANONICAL_MODEL-PROP-01: CSV/JSON schema와 deterministic builder를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`duplicate ID`, `orphan scenario`, `count mismatch`, `invalid continuity`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DOC-GOV-02-AUTOMATION_CONTINUITY — DOC-GOV / Builder·Validator·Continuity 자동화

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DOC-GOV` |
| Canonical Owner | `cpf-docs + source owner` |
| Work Package 유형 | `AUTOMATION_CONTINUITY` |
| 선행 작업 | `CPF-WP-DOC-GOV-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Final Target, ADR, Requirement Continuity, Current Request, Review, Handover의 역할·정본·폐기·변경 승인 규칙을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-DOC-GOV-02-AUTOMATION_CONTINUITY-REQ-01: 모든 Part 조립, header/count/hash, Requirement↔Scenario link와 role status를 검증한다.
- [ ] CPF-WP-DOC-GOV-02-AUTOMATION_CONTINUITY-REQ-02: Coverage 검증을 QA 통과로 오인하지 않게 result meaning을 명시한다.
- [ ] CPF-WP-DOC-GOV-02-AUTOMATION_CONTINUITY-REQ-03: 작업 전후 변경 ID와 Source/Evidence 연결을 보존한다.

### 비강제 구현 제안

- [ ] CPF-WP-DOC-GOV-02-AUTOMATION_CONTINUITY-PROP-01: logical master index와 streaming validator를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`missing part`, `wrong hash`, `orphan link`, `false QA complete`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DOC-GOV-03-EVIDENCE_VALIDATION — DOC-GOV / Evidence 유효성 검증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DOC-GOV` |
| Canonical Owner | `cpf-docs + source owner` |
| Work Package 유형 | `EVIDENCE_VALIDATION` |
| 선행 작업 | `CPF-WP-DOC-GOV-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Final Target, ADR, Requirement Continuity, Current Request, Review, Handover의 역할·정본·폐기·변경 승인 규칙을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-DOC-GOV-03-EVIDENCE_VALIDATION-REQ-01: 현재 baseline/working tree와 Evidence의 Source, command, environment, result, hash를 대조한다.
- [ ] CPF-WP-DOC-GOV-03-EVIDENCE_VALIDATION-REQ-02: 과거 Commit·다른 장비·다른 Artifact·미실행 Test를 현재 성공으로 사용하지 않는다.
- [ ] CPF-WP-DOC-GOV-03-EVIDENCE_VALIDATION-REQ-03: Coverage/traceability PASS와 제품 동작 PASS를 구분한다.

### 비강제 구현 제안

- [ ] CPF-WP-DOC-GOV-03-EVIDENCE_VALIDATION-PROP-01: Evidence schema validator와 stale evidence detector를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`baseline mismatch`, `missing report`, `unsanitized secret`, `stale evidence`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DOC-PRODUCT-01-CANONICAL_MODEL — DOC-PRODUCT / 정본·ID·상태 모델

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DOC-PRODUCT` |
| Canonical Owner | `cpf-docs + source owner` |
| Work Package 유형 | `CANONICAL_MODEL` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-14-FRONTEND`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

개발자·운영자·ADM/BZA·Gateway·Batch·설치·Migration·복구 Guide, OpenAPI, JavaDoc가 실제 Source와 일치하게 한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-DOC-PRODUCT-01-CANONICAL_MODEL-REQ-01: Canonical Requirement/Scenario ID, owner, acceptance, continuity와 상태 규칙을 단일 정본으로 유지한다.
- [ ] CPF-WP-DOC-PRODUCT-01-CANONICAL_MODEL-REQ-02: Alias/split/supersede/deprecate 관계와 count 변화를 추적한다.
- [ ] CPF-WP-DOC-PRODUCT-01-CANONICAL_MODEL-REQ-03: 169/162 등 count 불일치를 자동 차단한다.

### 비강제 구현 제안

- [ ] CPF-WP-DOC-PRODUCT-01-CANONICAL_MODEL-PROP-01: CSV/JSON schema와 deterministic builder를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`duplicate ID`, `orphan scenario`, `count mismatch`, `invalid continuity`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DOC-PRODUCT-02-AUTOMATION_CONTINUITY — DOC-PRODUCT / Builder·Validator·Continuity 자동화

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DOC-PRODUCT` |
| Canonical Owner | `cpf-docs + source owner` |
| Work Package 유형 | `AUTOMATION_CONTINUITY` |
| 선행 작업 | `CPF-WP-DOC-PRODUCT-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-14-FRONTEND`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

개발자·운영자·ADM/BZA·Gateway·Batch·설치·Migration·복구 Guide, OpenAPI, JavaDoc가 실제 Source와 일치하게 한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-DOC-PRODUCT-02-AUTOMATION_CONTINUITY-REQ-01: 모든 Part 조립, header/count/hash, Requirement↔Scenario link와 role status를 검증한다.
- [ ] CPF-WP-DOC-PRODUCT-02-AUTOMATION_CONTINUITY-REQ-02: Coverage 검증을 QA 통과로 오인하지 않게 result meaning을 명시한다.
- [ ] CPF-WP-DOC-PRODUCT-02-AUTOMATION_CONTINUITY-REQ-03: 작업 전후 변경 ID와 Source/Evidence 연결을 보존한다.

### 비강제 구현 제안

- [ ] CPF-WP-DOC-PRODUCT-02-AUTOMATION_CONTINUITY-PROP-01: logical master index와 streaming validator를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`missing part`, `wrong hash`, `orphan link`, `false QA complete`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DOC-PRODUCT-03-EVIDENCE_VALIDATION — DOC-PRODUCT / Evidence 유효성 검증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DOC-PRODUCT` |
| Canonical Owner | `cpf-docs + source owner` |
| Work Package 유형 | `EVIDENCE_VALIDATION` |
| 선행 작업 | `CPF-WP-DOC-PRODUCT-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-14-FRONTEND`, `GATE-16-COMPATIBILITY`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF`, `STD-OAS`, `STD-OWASP-API`, `STD-RFC9457`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

개발자·운영자·ADM/BZA·Gateway·Batch·설치·Migration·복구 Guide, OpenAPI, JavaDoc가 실제 Source와 일치하게 한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-DOC-PRODUCT-03-EVIDENCE_VALIDATION-REQ-01: 현재 baseline/working tree와 Evidence의 Source, command, environment, result, hash를 대조한다.
- [ ] CPF-WP-DOC-PRODUCT-03-EVIDENCE_VALIDATION-REQ-02: 과거 Commit·다른 장비·다른 Artifact·미실행 Test를 현재 성공으로 사용하지 않는다.
- [ ] CPF-WP-DOC-PRODUCT-03-EVIDENCE_VALIDATION-REQ-03: Coverage/traceability PASS와 제품 동작 PASS를 구분한다.

### 비강제 구현 제안

- [ ] CPF-WP-DOC-PRODUCT-03-EVIDENCE_VALIDATION-PROP-01: Evidence schema validator와 stale evidence detector를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`baseline mismatch`, `missing report`, `unsanitized secret`, `stale evidence`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-EDITION-01-POLICY_BOUNDARY — PROD-EDITION / 제품 정책·지원 경계

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-EDITION` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `POLICY_BOUNDARY` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Edition/License/Capability packaging을 기술 Runtime과 분리하고 미결정 정책을 GA 완료처럼 노출하지 않는다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-EDITION-01-POLICY_BOUNDARY-REQ-01: 지원/비지원, edition/license/capability와 security/compatibility 경계를 명시한다.
- [ ] CPF-WP-PROD-EDITION-01-POLICY_BOUNDARY-REQ-02: 미결정 정책을 GA 기능 완료처럼 노출하지 않는다.
- [ ] CPF-WP-PROD-EDITION-01-POLICY_BOUNDARY-REQ-03: 기술 Runtime과 상용 정책을 분리한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-EDITION-01-POLICY_BOUNDARY-PROP-01: ADR와 capability manifest로 정책을 표현한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unsupported edition`, `license change`, `capability conflict`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-EDITION-02-PROTOTYPE_EVIDENCE — PROD-EDITION / Capability Prototype·실증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-EDITION` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `PROTOTYPE_EVIDENCE` |
| 선행 작업 | `CPF-WP-PROD-EDITION-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Edition/License/Capability packaging을 기술 Runtime과 분리하고 미결정 정책을 GA 완료처럼 노출하지 않는다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-EDITION-02-PROTOTYPE_EVIDENCE-REQ-01: 정책 후보가 Architecture를 깨지 않는 최소 Prototype과 실제 Consumer를 제공한다.
- [ ] CPF-WP-PROD-EDITION-02-PROTOTYPE_EVIDENCE-REQ-02: 성능·보안·업그레이드·제거 가능성과 지원 비용을 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-EDITION-02-PROTOTYPE_EVIDENCE-PROP-01: 기본 비활성 leaf module/profile로 실증한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`enable`, `disable/remove`, `upgrade`, `security isolation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-EDITION-03-COMPATIBILITY_SECURITY — PROD-EDITION / 정책 호환성·보안

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-EDITION` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `COMPATIBILITY_SECURITY` |
| 선행 작업 | `CPF-WP-PROD-EDITION-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Edition/License/Capability packaging을 기술 Runtime과 분리하고 미결정 정책을 GA 완료처럼 노출하지 않는다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-EDITION-03-COMPATIBILITY_SECURITY-REQ-01: tenant/plugin/edition/package 격리, permission, signature, upgrade/rollback와 mixed-version을 검토한다.
- [ ] CPF-WP-PROD-EDITION-03-COMPATIBILITY_SECURITY-REQ-02: 선택 기능이 기본 Runtime 의존이나 권한 우회를 만들지 않는다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-EDITION-03-COMPATIBILITY_SECURITY-PROP-01: capability-based permission and signed plugin/package metadata를 검토한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`cross-tenant`, `unsigned plugin`, `incompatible package`, `rollback`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-MULTITENANT-01-POLICY_BOUNDARY — PROD-MULTITENANT / 제품 정책·지원 경계

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-MULTITENANT` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `POLICY_BOUNDARY` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

tenant context, data/config/permission/secret/quota/audit isolation과 tenant lifecycle을 선택 기능으로 설계한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-MULTITENANT-01-POLICY_BOUNDARY-REQ-01: 지원/비지원, edition/license/capability와 security/compatibility 경계를 명시한다.
- [ ] CPF-WP-PROD-MULTITENANT-01-POLICY_BOUNDARY-REQ-02: 미결정 정책을 GA 기능 완료처럼 노출하지 않는다.
- [ ] CPF-WP-PROD-MULTITENANT-01-POLICY_BOUNDARY-REQ-03: 기술 Runtime과 상용 정책을 분리한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-MULTITENANT-01-POLICY_BOUNDARY-PROP-01: ADR와 capability manifest로 정책을 표현한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unsupported edition`, `license change`, `capability conflict`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-MULTITENANT-02-PROTOTYPE_EVIDENCE — PROD-MULTITENANT / Capability Prototype·실증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-MULTITENANT` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `PROTOTYPE_EVIDENCE` |
| 선행 작업 | `CPF-WP-PROD-MULTITENANT-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

tenant context, data/config/permission/secret/quota/audit isolation과 tenant lifecycle을 선택 기능으로 설계한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-MULTITENANT-02-PROTOTYPE_EVIDENCE-REQ-01: 정책 후보가 Architecture를 깨지 않는 최소 Prototype과 실제 Consumer를 제공한다.
- [ ] CPF-WP-PROD-MULTITENANT-02-PROTOTYPE_EVIDENCE-REQ-02: 성능·보안·업그레이드·제거 가능성과 지원 비용을 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-MULTITENANT-02-PROTOTYPE_EVIDENCE-PROP-01: 기본 비활성 leaf module/profile로 실증한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`enable`, `disable/remove`, `upgrade`, `security isolation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-MULTITENANT-03-COMPATIBILITY_SECURITY — PROD-MULTITENANT / 정책 호환성·보안

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-MULTITENANT` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `COMPATIBILITY_SECURITY` |
| 선행 작업 | `CPF-WP-PROD-MULTITENANT-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-10-CRYPTO-PRIVACY`, `GATE-11-OPS-AUDIT`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

tenant context, data/config/permission/secret/quota/audit isolation과 tenant lifecycle을 선택 기능으로 설계한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-MULTITENANT-03-COMPATIBILITY_SECURITY-REQ-01: tenant/plugin/edition/package 격리, permission, signature, upgrade/rollback와 mixed-version을 검토한다.
- [ ] CPF-WP-PROD-MULTITENANT-03-COMPATIBILITY_SECURITY-REQ-02: 선택 기능이 기본 Runtime 의존이나 권한 우회를 만들지 않는다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-MULTITENANT-03-COMPATIBILITY_SECURITY-PROP-01: capability-based permission and signed plugin/package metadata를 검토한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`cross-tenant`, `unsigned plugin`, `incompatible package`, `rollback`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-PLUGIN-01-POLICY_BOUNDARY — PROD-PLUGIN / 제품 정책·지원 경계

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-PLUGIN` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `POLICY_BOUNDARY` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객/기관 Plugin·Adapter의 SPI, package, signature, compatibility, isolation, lifecycle, permission와 marketplace 후보 정책을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-PLUGIN-01-POLICY_BOUNDARY-REQ-01: 지원/비지원, edition/license/capability와 security/compatibility 경계를 명시한다.
- [ ] CPF-WP-PROD-PLUGIN-01-POLICY_BOUNDARY-REQ-02: 미결정 정책을 GA 기능 완료처럼 노출하지 않는다.
- [ ] CPF-WP-PROD-PLUGIN-01-POLICY_BOUNDARY-REQ-03: 기술 Runtime과 상용 정책을 분리한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-PLUGIN-01-POLICY_BOUNDARY-PROP-01: ADR와 capability manifest로 정책을 표현한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unsupported edition`, `license change`, `capability conflict`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-PLUGIN-02-PROTOTYPE_EVIDENCE — PROD-PLUGIN / Capability Prototype·실증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-PLUGIN` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `PROTOTYPE_EVIDENCE` |
| 선행 작업 | `CPF-WP-PROD-PLUGIN-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객/기관 Plugin·Adapter의 SPI, package, signature, compatibility, isolation, lifecycle, permission와 marketplace 후보 정책을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-PLUGIN-02-PROTOTYPE_EVIDENCE-REQ-01: 정책 후보가 Architecture를 깨지 않는 최소 Prototype과 실제 Consumer를 제공한다.
- [ ] CPF-WP-PROD-PLUGIN-02-PROTOTYPE_EVIDENCE-REQ-02: 성능·보안·업그레이드·제거 가능성과 지원 비용을 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-PLUGIN-02-PROTOTYPE_EVIDENCE-PROP-01: 기본 비활성 leaf module/profile로 실증한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`enable`, `disable/remove`, `upgrade`, `security isolation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-PLUGIN-03-COMPATIBILITY_SECURITY — PROD-PLUGIN / 정책 호환성·보안

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-PLUGIN` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `COMPATIBILITY_SECURITY` |
| 선행 작업 | `CPF-WP-PROD-PLUGIN-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

고객/기관 Plugin·Adapter의 SPI, package, signature, compatibility, isolation, lifecycle, permission와 marketplace 후보 정책을 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-PLUGIN-03-COMPATIBILITY_SECURITY-REQ-01: tenant/plugin/edition/package 격리, permission, signature, upgrade/rollback와 mixed-version을 검토한다.
- [ ] CPF-WP-PROD-PLUGIN-03-COMPATIBILITY_SECURITY-REQ-02: 선택 기능이 기본 Runtime 의존이나 권한 우회를 만들지 않는다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-PLUGIN-03-COMPATIBILITY_SECURITY-PROP-01: capability-based permission and signed plugin/package metadata를 검토한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`cross-tenant`, `unsigned plugin`, `incompatible package`, `rollback`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-PACKAGE-01-POLICY_BOUNDARY — PROD-PACKAGE / 제품 정책·지원 경계

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-PACKAGE` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `POLICY_BOUNDARY` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

산업군·금융권별 capability package의 dependency, install/upgrade, config, license, compatibility와 support boundary를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-PACKAGE-01-POLICY_BOUNDARY-REQ-01: 지원/비지원, edition/license/capability와 security/compatibility 경계를 명시한다.
- [ ] CPF-WP-PROD-PACKAGE-01-POLICY_BOUNDARY-REQ-02: 미결정 정책을 GA 기능 완료처럼 노출하지 않는다.
- [ ] CPF-WP-PROD-PACKAGE-01-POLICY_BOUNDARY-REQ-03: 기술 Runtime과 상용 정책을 분리한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-PACKAGE-01-POLICY_BOUNDARY-PROP-01: ADR와 capability manifest로 정책을 표현한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`unsupported edition`, `license change`, `capability conflict`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-PACKAGE-02-PROTOTYPE_EVIDENCE — PROD-PACKAGE / Capability Prototype·실증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-PACKAGE` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `PROTOTYPE_EVIDENCE` |
| 선행 작업 | `CPF-WP-PROD-PACKAGE-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

산업군·금융권별 capability package의 dependency, install/upgrade, config, license, compatibility와 support boundary를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-PACKAGE-02-PROTOTYPE_EVIDENCE-REQ-01: 정책 후보가 Architecture를 깨지 않는 최소 Prototype과 실제 Consumer를 제공한다.
- [ ] CPF-WP-PROD-PACKAGE-02-PROTOTYPE_EVIDENCE-REQ-02: 성능·보안·업그레이드·제거 가능성과 지원 비용을 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-PACKAGE-02-PROTOTYPE_EVIDENCE-PROP-01: 기본 비활성 leaf module/profile로 실증한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`enable`, `disable/remove`, `upgrade`, `security isolation`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-PROD-PACKAGE-03-COMPATIBILITY_SECURITY — PROD-PACKAGE / 정책 호환성·보안

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `PROD-PACKAGE` |
| Canonical Owner | `product governance` |
| Work Package 유형 | `COMPATIBILITY_SECURITY` |
| 선행 작업 | `CPF-WP-PROD-PACKAGE-01-POLICY_BOUNDARY` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-09-SECURITY`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-OWASP-ASVS`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

산업군·금융권별 capability package의 dependency, install/upgrade, config, license, compatibility와 support boundary를 제공한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-PROD-PACKAGE-03-COMPATIBILITY_SECURITY-REQ-01: tenant/plugin/edition/package 격리, permission, signature, upgrade/rollback와 mixed-version을 검토한다.
- [ ] CPF-WP-PROD-PACKAGE-03-COMPATIBILITY_SECURITY-REQ-02: 선택 기능이 기본 Runtime 의존이나 권한 우회를 만들지 않는다.

### 비강제 구현 제안

- [ ] CPF-WP-PROD-PACKAGE-03-COMPATIBILITY_SECURITY-PROP-01: capability-based permission and signed plugin/package metadata를 검토한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`cross-tenant`, `unsigned plugin`, `incompatible package`, `rollback`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-GOV-01-CANONICAL_MODEL — REQ-GOV / 정본·ID·상태 모델

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-GOV` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `CANONICAL_MODEL` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-12-OBSERVABILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Requirement ID, owner, priority, acceptance, status, continuity, traceability와 변경 승인 규칙을 영속 정본으로 관리한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-GOV-01-CANONICAL_MODEL-REQ-01: Canonical Requirement/Scenario ID, owner, acceptance, continuity와 상태 규칙을 단일 정본으로 유지한다.
- [ ] CPF-WP-REQ-GOV-01-CANONICAL_MODEL-REQ-02: Alias/split/supersede/deprecate 관계와 count 변화를 추적한다.
- [ ] CPF-WP-REQ-GOV-01-CANONICAL_MODEL-REQ-03: 169/162 등 count 불일치를 자동 차단한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-GOV-01-CANONICAL_MODEL-PROP-01: CSV/JSON schema와 deterministic builder를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`duplicate ID`, `orphan scenario`, `count mismatch`, `invalid continuity`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-GOV-02-AUTOMATION_CONTINUITY — REQ-GOV / Builder·Validator·Continuity 자동화

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-GOV` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `AUTOMATION_CONTINUITY` |
| 선행 작업 | `CPF-WP-REQ-GOV-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-12-OBSERVABILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Requirement ID, owner, priority, acceptance, status, continuity, traceability와 변경 승인 규칙을 영속 정본으로 관리한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-GOV-02-AUTOMATION_CONTINUITY-REQ-01: 모든 Part 조립, header/count/hash, Requirement↔Scenario link와 role status를 검증한다.
- [ ] CPF-WP-REQ-GOV-02-AUTOMATION_CONTINUITY-REQ-02: Coverage 검증을 QA 통과로 오인하지 않게 result meaning을 명시한다.
- [ ] CPF-WP-REQ-GOV-02-AUTOMATION_CONTINUITY-REQ-03: 작업 전후 변경 ID와 Source/Evidence 연결을 보존한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-GOV-02-AUTOMATION_CONTINUITY-PROP-01: logical master index와 streaming validator를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`missing part`, `wrong hash`, `orphan link`, `false QA complete`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-GOV-03-EVIDENCE_VALIDATION — REQ-GOV / Evidence 유효성 검증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-GOV` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `EVIDENCE_VALIDATION` |
| 선행 작업 | `CPF-WP-REQ-GOV-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-12-OBSERVABILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OTEL`, `STD-SCRUM`, `STD-SLSA`, `STD-TRACE` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Requirement ID, owner, priority, acceptance, status, continuity, traceability와 변경 승인 규칙을 영속 정본으로 관리한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-GOV-03-EVIDENCE_VALIDATION-REQ-01: 현재 baseline/working tree와 Evidence의 Source, command, environment, result, hash를 대조한다.
- [ ] CPF-WP-REQ-GOV-03-EVIDENCE_VALIDATION-REQ-02: 과거 Commit·다른 장비·다른 Artifact·미실행 Test를 현재 성공으로 사용하지 않는다.
- [ ] CPF-WP-REQ-GOV-03-EVIDENCE_VALIDATION-REQ-03: Coverage/traceability PASS와 제품 동작 PASS를 구분한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-GOV-03-EVIDENCE_VALIDATION-PROP-01: Evidence schema validator와 stale evidence detector를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`baseline mismatch`, `missing report`, `unsanitized secret`, `stale evidence`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-REVIEW-01-CANONICAL_MODEL — REQ-REVIEW / 정본·ID·상태 모델

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-REVIEW` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `CANONICAL_MODEL` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

각 작업 전후 Requirement→구현과 구현→Requirement를 독립 검수하고 완료 보고와 실제 Git 차이를 기록한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-REVIEW-01-CANONICAL_MODEL-REQ-01: Canonical Requirement/Scenario ID, owner, acceptance, continuity와 상태 규칙을 단일 정본으로 유지한다.
- [ ] CPF-WP-REQ-REVIEW-01-CANONICAL_MODEL-REQ-02: Alias/split/supersede/deprecate 관계와 count 변화를 추적한다.
- [ ] CPF-WP-REQ-REVIEW-01-CANONICAL_MODEL-REQ-03: 169/162 등 count 불일치를 자동 차단한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-REVIEW-01-CANONICAL_MODEL-PROP-01: CSV/JSON schema와 deterministic builder를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`duplicate ID`, `orphan scenario`, `count mismatch`, `invalid continuity`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-REVIEW-02-AUTOMATION_CONTINUITY — REQ-REVIEW / Builder·Validator·Continuity 자동화

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-REVIEW` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `AUTOMATION_CONTINUITY` |
| 선행 작업 | `CPF-WP-REQ-REVIEW-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

각 작업 전후 Requirement→구현과 구현→Requirement를 독립 검수하고 완료 보고와 실제 Git 차이를 기록한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-REVIEW-02-AUTOMATION_CONTINUITY-REQ-01: 모든 Part 조립, header/count/hash, Requirement↔Scenario link와 role status를 검증한다.
- [ ] CPF-WP-REQ-REVIEW-02-AUTOMATION_CONTINUITY-REQ-02: Coverage 검증을 QA 통과로 오인하지 않게 result meaning을 명시한다.
- [ ] CPF-WP-REQ-REVIEW-02-AUTOMATION_CONTINUITY-REQ-03: 작업 전후 변경 ID와 Source/Evidence 연결을 보존한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-REVIEW-02-AUTOMATION_CONTINUITY-PROP-01: logical master index와 streaming validator를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`missing part`, `wrong hash`, `orphan link`, `false QA complete`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-REVIEW-03-EVIDENCE_VALIDATION — REQ-REVIEW / Evidence 유효성 검증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-REVIEW` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `EVIDENCE_VALIDATION` |
| 선행 작업 | `CPF-WP-REQ-REVIEW-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

각 작업 전후 Requirement→구현과 구현→Requirement를 독립 검수하고 완료 보고와 실제 Git 차이를 기록한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-REVIEW-03-EVIDENCE_VALIDATION-REQ-01: 현재 baseline/working tree와 Evidence의 Source, command, environment, result, hash를 대조한다.
- [ ] CPF-WP-REQ-REVIEW-03-EVIDENCE_VALIDATION-REQ-02: 과거 Commit·다른 장비·다른 Artifact·미실행 Test를 현재 성공으로 사용하지 않는다.
- [ ] CPF-WP-REQ-REVIEW-03-EVIDENCE_VALIDATION-REQ-03: Coverage/traceability PASS와 제품 동작 PASS를 구분한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-REVIEW-03-EVIDENCE_VALIDATION-PROP-01: Evidence schema validator와 stale evidence detector를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`baseline mismatch`, `missing report`, `unsanitized secret`, `stale evidence`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-CODEX-01-CANONICAL_MODEL — REQ-CODEX / 정본·ID·상태 모델

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-CODEX` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `CANONICAL_MODEL` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Codex/AI 요청서가 이전 대화 없이 실행 가능하도록 baseline, scope, architecture, acceptance, evidence, 금지조건과 output을 포함한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-CODEX-01-CANONICAL_MODEL-REQ-01: Canonical Requirement/Scenario ID, owner, acceptance, continuity와 상태 규칙을 단일 정본으로 유지한다.
- [ ] CPF-WP-REQ-CODEX-01-CANONICAL_MODEL-REQ-02: Alias/split/supersede/deprecate 관계와 count 변화를 추적한다.
- [ ] CPF-WP-REQ-CODEX-01-CANONICAL_MODEL-REQ-03: 169/162 등 count 불일치를 자동 차단한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-CODEX-01-CANONICAL_MODEL-PROP-01: CSV/JSON schema와 deterministic builder를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`duplicate ID`, `orphan scenario`, `count mismatch`, `invalid continuity`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-CODEX-02-AUTOMATION_CONTINUITY — REQ-CODEX / Builder·Validator·Continuity 자동화

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-CODEX` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `AUTOMATION_CONTINUITY` |
| 선행 작업 | `CPF-WP-REQ-CODEX-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Codex/AI 요청서가 이전 대화 없이 실행 가능하도록 baseline, scope, architecture, acceptance, evidence, 금지조건과 output을 포함한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-CODEX-02-AUTOMATION_CONTINUITY-REQ-01: 모든 Part 조립, header/count/hash, Requirement↔Scenario link와 role status를 검증한다.
- [ ] CPF-WP-REQ-CODEX-02-AUTOMATION_CONTINUITY-REQ-02: Coverage 검증을 QA 통과로 오인하지 않게 result meaning을 명시한다.
- [ ] CPF-WP-REQ-CODEX-02-AUTOMATION_CONTINUITY-REQ-03: 작업 전후 변경 ID와 Source/Evidence 연결을 보존한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-CODEX-02-AUTOMATION_CONTINUITY-PROP-01: logical master index와 streaming validator를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`missing part`, `wrong hash`, `orphan link`, `false QA complete`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-CODEX-03-EVIDENCE_VALIDATION — REQ-CODEX / Evidence 유효성 검증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-CODEX` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `EVIDENCE_VALIDATION` |
| 선행 작업 | `CPF-WP-REQ-CODEX-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Codex/AI 요청서가 이전 대화 없이 실행 가능하도록 baseline, scope, architecture, acceptance, evidence, 금지조건과 output을 포함한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-CODEX-03-EVIDENCE_VALIDATION-REQ-01: 현재 baseline/working tree와 Evidence의 Source, command, environment, result, hash를 대조한다.
- [ ] CPF-WP-REQ-CODEX-03-EVIDENCE_VALIDATION-REQ-02: 과거 Commit·다른 장비·다른 Artifact·미실행 Test를 현재 성공으로 사용하지 않는다.
- [ ] CPF-WP-REQ-CODEX-03-EVIDENCE_VALIDATION-REQ-03: Coverage/traceability PASS와 제품 동작 PASS를 구분한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-CODEX-03-EVIDENCE_VALIDATION-PROP-01: Evidence schema validator와 stale evidence detector를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`baseline mismatch`, `missing report`, `unsanitized secret`, `stale evidence`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-GAP-01-CANONICAL_MODEL — REQ-GAP / 정본·ID·상태 모델

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-GAP` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `CANONICAL_MODEL` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

새 상용 필수 요구를 기존 ID와 중복 검사해 intake하고 split/supersede/deprecate 관계와 count 변화를 기록한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-GAP-01-CANONICAL_MODEL-REQ-01: Canonical Requirement/Scenario ID, owner, acceptance, continuity와 상태 규칙을 단일 정본으로 유지한다.
- [ ] CPF-WP-REQ-GAP-01-CANONICAL_MODEL-REQ-02: Alias/split/supersede/deprecate 관계와 count 변화를 추적한다.
- [ ] CPF-WP-REQ-GAP-01-CANONICAL_MODEL-REQ-03: 169/162 등 count 불일치를 자동 차단한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-GAP-01-CANONICAL_MODEL-PROP-01: CSV/JSON schema와 deterministic builder를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`duplicate ID`, `orphan scenario`, `count mismatch`, `invalid continuity`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-GAP-02-AUTOMATION_CONTINUITY — REQ-GAP / Builder·Validator·Continuity 자동화

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-GAP` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `AUTOMATION_CONTINUITY` |
| 선행 작업 | `CPF-WP-REQ-GAP-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

새 상용 필수 요구를 기존 ID와 중복 검사해 intake하고 split/supersede/deprecate 관계와 count 변화를 기록한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-GAP-02-AUTOMATION_CONTINUITY-REQ-01: 모든 Part 조립, header/count/hash, Requirement↔Scenario link와 role status를 검증한다.
- [ ] CPF-WP-REQ-GAP-02-AUTOMATION_CONTINUITY-REQ-02: Coverage 검증을 QA 통과로 오인하지 않게 result meaning을 명시한다.
- [ ] CPF-WP-REQ-GAP-02-AUTOMATION_CONTINUITY-REQ-03: 작업 전후 변경 ID와 Source/Evidence 연결을 보존한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-GAP-02-AUTOMATION_CONTINUITY-PROP-01: logical master index와 streaming validator를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`missing part`, `wrong hash`, `orphan link`, `false QA complete`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-REQ-GAP-03-EVIDENCE_VALIDATION — REQ-GAP / Evidence 유효성 검증

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `REQ-GAP` |
| Canonical Owner | `requirement governance` |
| Work Package 유형 | `EVIDENCE_VALIDATION` |
| 선행 작업 | `CPF-WP-REQ-GAP-01-CANONICAL_MODEL` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

새 상용 필수 요구를 기존 ID와 중복 검사해 intake하고 split/supersede/deprecate 관계와 count 변화를 기록한다.

### 탐색·변경 범위

- root build/wrapper/BOM/publication, deploy/migration, docs, product capability policy and requirement governance
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

fresh-clone user, artifact consumer, deployment operator, product owner and QA

### 필수 개발 결과

- [ ] CPF-WP-REQ-GAP-03-EVIDENCE_VALIDATION-REQ-01: 현재 baseline/working tree와 Evidence의 Source, command, environment, result, hash를 대조한다.
- [ ] CPF-WP-REQ-GAP-03-EVIDENCE_VALIDATION-REQ-02: 과거 Commit·다른 장비·다른 Artifact·미실행 Test를 현재 성공으로 사용하지 않는다.
- [ ] CPF-WP-REQ-GAP-03-EVIDENCE_VALIDATION-REQ-03: Coverage/traceability PASS와 제품 동작 PASS를 구분한다.

### 비강제 구현 제안

- [ ] CPF-WP-REQ-GAP-03-EVIDENCE_VALIDATION-PROP-01: Evidence schema validator와 stale evidence detector를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`baseline mismatch`, `missing report`, `unsanitized secret`, `stale evidence`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clean build, signed artifact/provenance/SBOM, install/upgrade/rollback and ledger validation
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- module/offline build, artifact/POM/BOM/schema/state simulation and link/path validation
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
