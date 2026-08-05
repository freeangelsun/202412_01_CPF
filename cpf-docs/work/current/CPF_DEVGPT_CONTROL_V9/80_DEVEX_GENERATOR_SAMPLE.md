# CPF V8 상세 개발 Work Package — Developer Experience·Generator·Sample

- Canonical Requirement: 9개
- 실행 Work Package: 33개
- Canonical IDs: `DEVEX-CODEGEN`, `DEVEX-COMMENT`, `DEVEX-QUICK`, `ONBOARD-DOMAIN`, `SAMPLE-ACC`, `SAMPLE-BIZADM`, `SAMPLE-EDU`, `SAMPLE-MBR`, `SAMPLE-REF`
- 각 Work Package는 독립 개발 단위이며, 동일 호출 경로를 숫자 때문에 중간 절단하지 않는다.
- 구현 제안은 비강제이고 CPF 정본·표준·기존 Architecture가 우선한다.

## CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP — DEVEX-QUICK / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-QUICK` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

신규 개발자가 표준 환경에서 설치→생성→빌드→실행→테스트→디버깅을 문서대로 재현할 수 있는 quick start를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER — DEVEX-QUICK / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-QUICK` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

신규 개발자가 표준 환경에서 설치→생성→빌드→실행→테스트→디버깅을 문서대로 재현할 수 있는 quick start를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-QUICK-03-VERIFICATION_EVIDENCE — DEVEX-QUICK / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-QUICK` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP`, `CPF-WP-DEVEX-QUICK-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

신규 개발자가 표준 환경에서 설치→생성→빌드→실행→테스트→디버깅을 문서대로 재현할 수 있는 quick start를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-QUICK-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-DEVEX-QUICK-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-DEVEX-QUICK-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-DEVEX-QUICK-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-QUICK-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-DEVEX-QUICK-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-QUICK-04-GENERATION_COMPATIBILITY — DEVEX-QUICK / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-QUICK` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-DEVEX-QUICK-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OPENFEATURE`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

신규 개발자가 표준 환경에서 설치→생성→빌드→실행→테스트→디버깅을 문서대로 재현할 수 있는 quick start를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-QUICK-04-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-DEVEX-QUICK-04-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-DEVEX-QUICK-04-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-DEVEX-QUICK-04-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-QUICK-04-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-DEVEX-QUICK-04-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP — DEVEX-CODEGEN / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-CODEGEN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER — DEVEX-CODEGEN / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-CODEGEN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-CODEGEN-03-VERIFICATION_EVIDENCE — DEVEX-CODEGEN / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-CODEGEN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP`, `CPF-WP-DEVEX-CODEGEN-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-CODEGEN-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-DEVEX-CODEGEN-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-DEVEX-CODEGEN-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-DEVEX-CODEGEN-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-CODEGEN-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-DEVEX-CODEGEN-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-CODEGEN-04-DATA_MIGRATION — DEVEX-CODEGEN / DB·Query·Generator·Vendor Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-CODEGEN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `DATA_MIGRATION` |
| 선행 작업 | `CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-CODEGEN-04-DATA_MIGRATION-REQ-01: DB/SQL/Query 영향 여부를 반드시 판정하고 N/A면 실제 호출 경로 근거를 기록한다.
- [ ] CPF-WP-DEVEX-CODEGEN-04-DATA_MIGRATION-REQ-02: Canonical Model/Query Contract→Generator/Template→Generated SQL/Mapper→Fresh Init/Seed→Migration/Rollback/Reapply 순서로 변경한다.
- [ ] CPF-WP-DEVEX-CODEGEN-04-DATA_MIGRATION-REQ-03: MariaDB/PostgreSQL/Oracle의 type/default/paging/locking/error/index/FK/transaction 차이를 전수 반영한다.
- [ ] CPF-WP-DEVEX-CODEGEN-04-DATA_MIGRATION-REQ-04: Repository/Mapper/DTO/Service/API/Frontend/Batch/ADM/BZA/Sample/EDU 소비자를 모두 점검한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-CODEGEN-04-DATA_MIGRATION-PROP-01: Vendor-native SQL을 유지하되 Canonical Metadata에서 생성/검증 가능한 구조를 우선한다.
- [ ] CPF-WP-DEVEX-CODEGEN-04-DATA_MIGRATION-PROP-02: 파괴적 변경은 expand→migrate→contract와 compatibility window를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`fresh install`, `double seed`, `upgrade`, `migration interruption`, `rollback/forward recovery`, `drift`, `vendor difference`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-CODEGEN-05-GENERATION_COMPATIBILITY — DEVEX-CODEGEN / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-CODEGEN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-DEVEX-CODEGEN-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-05-DB-QUERY`, `GATE-14-FRONTEND`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA`, `STD-WCAG` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-CODEGEN-05-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-DEVEX-CODEGEN-05-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-DEVEX-CODEGEN-05-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-DEVEX-CODEGEN-05-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-CODEGEN-05-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-DEVEX-CODEGEN-05-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP — DEVEX-COMMENT / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-COMMENT` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Public API/SPI, 주요 Service/Controller, 복구·동시성·보안 로직에 유지보수 가능한 한글 JavaDoc/주석과 설정 설명을 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER — DEVEX-COMMENT / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-COMMENT` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Public API/SPI, 주요 Service/Controller, 복구·동시성·보안 로직에 유지보수 가능한 한글 JavaDoc/주석과 설정 설명을 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-COMMENT-03-VERIFICATION_EVIDENCE — DEVEX-COMMENT / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-COMMENT` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP`, `CPF-WP-DEVEX-COMMENT-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Public API/SPI, 주요 Service/Controller, 복구·동시성·보안 로직에 유지보수 가능한 한글 JavaDoc/주석과 설정 설명을 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-COMMENT-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-DEVEX-COMMENT-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-DEVEX-COMMENT-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-DEVEX-COMMENT-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-COMMENT-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-DEVEX-COMMENT-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-DEVEX-COMMENT-04-GENERATION_COMPATIBILITY — DEVEX-COMMENT / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `DEVEX-COMMENT` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-DEVEX-COMMENT-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-03-HTTP-API`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-19-DOC-SUPPORT`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-OAS`, `STD-OPENFEATURE`, `STD-OWASP-API`, `STD-RFC9457`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

Public API/SPI, 주요 Service/Controller, 복구·동시성·보안 로직에 유지보수 가능한 한글 JavaDoc/주석과 설정 설명을 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-DEVEX-COMMENT-04-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-DEVEX-COMMENT-04-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-DEVEX-COMMENT-04-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-DEVEX-COMMENT-04-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-DEVEX-COMMENT-04-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-DEVEX-COMMENT-04-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP — ONBOARD-DOMAIN / 계약·Ownership 확정

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `ONBOARD-DOMAIN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `CONTRACT_OWNERSHIP` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DomainName+SystemCode로 신규 업무 Domain을 충돌 검증 후 생성·DB bootstrap·build/runtime·remove/regenerate할 수 있게 한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP-REQ-01: Canonical Owner Module·Package·State Owner와 실제 Consumer를 코드 기준으로 확정한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP-REQ-02: Public API·SPI·Internal, 입력·출력·오류·lifecycle·thread-safety·version 계약을 정의한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP-REQ-03: 역방향/순환 의존, Internal 참조, 선택 OSS type 노출, Dual Primary와 owner 없는 공통기능을 제거 또는 이관 대상으로 등록한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP-REQ-04: Local/Remote/Async/Batch 등 적용 topology에서 계약 동등성과 trust boundary를 정의한다.

### 비강제 구현 제안

- [ ] CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP-PROP-01: 기존 정식 Package와 Contract를 우선 재사용하고 새 Module은 실제 Owner가 없을 때만 제안한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP-PROP-02: 복잡한 기술 세부는 Internal로 숨기고 고객 확장 지점만 안정 SPI로 노출한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`contract compatibility`, `unsupported capability`, `invalid configuration`, `internal access negative fixture`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER — ONBOARD-DOMAIN / 기본 구현·실제 Consumer 연결

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `ONBOARD-DOMAIN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `IMPLEMENTATION_CONSUMER` |
| 선행 작업 | `CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DomainName+SystemCode로 신규 업무 Domain을 충돌 검증 후 생성·DB bootstrap·build/runtime·remove/regenerate할 수 있게 한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER-REQ-01: Interface/DTO/Marker가 아닌 안전한 기본 구현·Provider·AutoConfiguration·상태 저장을 제공한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER-REQ-02: Bean/Route/SQL/API/Frontend/Script를 실제 Product Consumer 호출 경로에 연결한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER-REQ-03: 설정 safe default, validation, capability detection, lifecycle start/stop와 resource cleanup을 구현한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER-REQ-04: Consumer 없는 추상화나 Sample-only 구현이면 완료하지 않고 실제 Consumer 연결 또는 통합/폐기 후보로 처리한다.

### 비강제 구현 제안

- [ ] CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER-PROP-01: Leaf Starter와 조건부 AutoConfiguration을 사용해 선택하지 않은 Runtime 의존을 유입하지 않는다.
- [ ] CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER-PROP-02: 제품 Consumer와 Reference Consumer를 분리하되 동일 Public Contract를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal consumer flow`, `missing provider`, `invalid property`, `startup/shutdown`, `resource cleanup`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-ONBOARD-DOMAIN-03-VERIFICATION_EVIDENCE — ONBOARD-DOMAIN / 검증·Evidence·추적성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `ONBOARD-DOMAIN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `VERIFICATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP`, `CPF-WP-ONBOARD-DOMAIN-02-IMPLEMENTATION_CONSUMER` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DomainName+SystemCode로 신규 업무 Domain을 충돌 검증 후 생성·DB bootstrap·build/runtime·remove/regenerate할 수 있게 한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-ONBOARD-DOMAIN-03-VERIFICATION_EVIDENCE-REQ-01: 연결 CPF-FR Requirement와 CPF-SC Scenario를 논리 원장에서 전수 추출해 이 Work Package에 매핑한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-03-VERIFICATION_EVIDENCE-REQ-02: 목표 환경을 먼저 실제 실행하고, 불가능할 때 최대 대체검증과 남은 실제 Runtime 차이를 분리한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-03-VERIFICATION_EVIDENCE-REQ-03: 각 Scenario에 Test Method·Assertion·실제 결과·Evidence를 개별 연결한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-03-VERIFICATION_EVIDENCE-REQ-04: 명령, 환경/version, 시간, exit code, report/artifact hash, sanitization과 baseline/working-tree 상태를 기록한다.

### 비강제 구현 제안

- [ ] CPF-WP-ONBOARD-DOMAIN-03-VERIFICATION_EVIDENCE-PROP-01: 공통 Test Harness는 재사용하되 각 Requirement/Scenario의 Acceptance 근거를 개별 기록한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-03-VERIFICATION_EVIDENCE-PROP-02: 긴 로그는 별도 Evidence로 두고 원장에는 핵심 결과와 경로를 남긴다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`direct validation`, `alternative validation`, `negative fixture`, `regression`, `evidence schema failure`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-ONBOARD-DOMAIN-04-DATA_MIGRATION — ONBOARD-DOMAIN / DB·Query·Generator·Vendor Lifecycle

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `ONBOARD-DOMAIN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `DATA_MIGRATION` |
| 선행 작업 | `CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DomainName+SystemCode로 신규 업무 Domain을 충돌 검증 후 생성·DB bootstrap·build/runtime·remove/regenerate할 수 있게 한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-ONBOARD-DOMAIN-04-DATA_MIGRATION-REQ-01: DB/SQL/Query 영향 여부를 반드시 판정하고 N/A면 실제 호출 경로 근거를 기록한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-04-DATA_MIGRATION-REQ-02: Canonical Model/Query Contract→Generator/Template→Generated SQL/Mapper→Fresh Init/Seed→Migration/Rollback/Reapply 순서로 변경한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-04-DATA_MIGRATION-REQ-03: MariaDB/PostgreSQL/Oracle의 type/default/paging/locking/error/index/FK/transaction 차이를 전수 반영한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-04-DATA_MIGRATION-REQ-04: Repository/Mapper/DTO/Service/API/Frontend/Batch/ADM/BZA/Sample/EDU 소비자를 모두 점검한다.

### 비강제 구현 제안

- [ ] CPF-WP-ONBOARD-DOMAIN-04-DATA_MIGRATION-PROP-01: Vendor-native SQL을 유지하되 Canonical Metadata에서 생성/검증 가능한 구조를 우선한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-04-DATA_MIGRATION-PROP-02: 파괴적 변경은 expand→migrate→contract와 compatibility window를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`fresh install`, `double seed`, `upgrade`, `migration interruption`, `rollback/forward recovery`, `drift`, `vendor difference`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-ONBOARD-DOMAIN-05-GENERATION_COMPATIBILITY — ONBOARD-DOMAIN / 생성·Artifact·호환성

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `ONBOARD-DOMAIN` |
| Canonical Owner | `cpf-tools + public artifacts` |
| Work Package 유형 | `GENERATION_COMPATIBILITY` |
| 선행 작업 | `CPF-WP-ONBOARD-DOMAIN-01-CONTRACT_OWNERSHIP` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-16-COMPATIBILITY`, `GATE-17-SUPPLY-CHAIN`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CYCLONEDX`, `STD-NIST-SSDF`, `STD-SCRUM`, `STD-SLSA` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

DomainName+SystemCode로 신규 업무 Domain을 충돌 검증 후 생성·DB bootstrap·build/runtime·remove/regenerate할 수 있게 한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-ONBOARD-DOMAIN-05-GENERATION_COMPATIBILITY-REQ-01: Generator/OpenAPI/AsyncAPI/DB metadata/template 등 Canonical 입력과 생성 산출물의 ownership을 확정한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-05-GENERATION_COMPATIBILITY-REQ-02: remove→regenerate deterministic parity와 user-owned 영역 보존을 검증한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-05-GENERATION_COMPATIBILITY-REQ-03: API/message/file/DB/config/artifact의 semantic version, compatibility, deprecation, mixed-version와 rollback 영향을 처리한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-05-GENERATION_COMPATIBILITY-REQ-04: Generated Source만 수동 수정하지 않고 원본 입력·Generator·Golden Sample을 함께 수정한다.

### 비강제 구현 제안

- [ ] CPF-WP-ONBOARD-DOMAIN-05-GENERATION_COMPATIBILITY-PROP-01: 입력 version/hash와 generator version을 manifest에 고정하고 normalized diff gate를 사용한다.
- [ ] CPF-WP-ONBOARD-DOMAIN-05-GENERATION_COMPATIBILITY-PROP-02: 최신 표준 버전은 shadow compatibility를 통과한 뒤 CPF 지원 profile로 승격한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`clean regeneration`, `user region preserve`, `mixed version`, `unsupported profile`, `rollback compatibility`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-ACC-01-REFERENCE_RUNTIME — SAMPLE-ACC / Reference 실제 Runtime

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-ACC` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REFERENCE_RUNTIME` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

범용 계정/업무 흐름을 과도한 제품 원장 없이 Local/Remote·validation·transaction·error 사용법을 보여주는 선택형 Sample로 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-ACC-01-REFERENCE_RUNTIME-REQ-01: Sample/Reference가 제품 Public API와 Published Artifact를 실제 소비한다.
- [ ] CPF-WP-SAMPLE-ACC-01-REFERENCE_RUNTIME-REQ-02: 별도 장난감 Contract나 Sample-only Engine을 만들지 않는다.
- [ ] CPF-WP-SAMPLE-ACC-01-REFERENCE_RUNTIME-REQ-03: 정상·오류·권한·복구·운영 흐름을 실행 가능하게 제공한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-ACC-01-REFERENCE_RUNTIME-PROP-01: 작은 Golden scenario와 표준 Test Kit를 재사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal`, `validation`, `authorization`, `failure`, `recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-ACC-02-FAILURE_PARITY — SAMPLE-ACC / Reference 오류·복구 Parity

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-ACC` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `FAILURE_PARITY` |
| 선행 작업 | `CPF-WP-SAMPLE-ACC-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

범용 계정/업무 흐름을 과도한 제품 원장 없이 Local/Remote·validation·transaction·error 사용법을 보여주는 선택형 Sample로 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-ACC-02-FAILURE_PARITY-REQ-01: Reference가 제품 Runtime과 동일한 error taxonomy, retry/unknown/recovery와 audit를 사용한다.
- [ ] CPF-WP-SAMPLE-ACC-02-FAILURE_PARITY-REQ-02: Local/Remote 또는 provider profile 차이를 동일 업무 계약으로 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-ACC-02-FAILURE_PARITY-PROP-01: fault profile과 documented operator workflow를 포함한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`local/remote parity`, `dependency failure`, `unknown`, `manual recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-ACC-03-REGENERATION_EVIDENCE — SAMPLE-ACC / Sample·Golden 재생성 Evidence

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-ACC` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REGENERATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-SAMPLE-ACC-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-06-STATE-IDEMP`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

범용 계정/업무 흐름을 과도한 제품 원장 없이 Local/Remote·validation·transaction·error 사용법을 보여주는 선택형 Sample로 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-ACC-03-REGENERATION_EVIDENCE-REQ-01: Generator 입력에서 Sample/Golden Domain을 재생성하고 normalized parity를 확인한다.
- [ ] CPF-WP-SAMPLE-ACC-03-REGENERATION_EVIDENCE-REQ-02: 사용자 영역과 교육 문서가 재생성 후 보존된다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-ACC-03-REGENERATION_EVIDENCE-PROP-01: remove/regenerate CI profile과 golden diff를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`remove/regenerate`, `user edit preserve`, `drift`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-MBR-01-REFERENCE_RUNTIME — SAMPLE-MBR / Reference 실제 Runtime

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-MBR` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REFERENCE_RUNTIME` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

cpf-member를 Generator output과 동일한 Golden Reference Instance로 유지하고 normalize parity gate를 통과시킨다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-MBR-01-REFERENCE_RUNTIME-REQ-01: Sample/Reference가 제품 Public API와 Published Artifact를 실제 소비한다.
- [ ] CPF-WP-SAMPLE-MBR-01-REFERENCE_RUNTIME-REQ-02: 별도 장난감 Contract나 Sample-only Engine을 만들지 않는다.
- [ ] CPF-WP-SAMPLE-MBR-01-REFERENCE_RUNTIME-REQ-03: 정상·오류·권한·복구·운영 흐름을 실행 가능하게 제공한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-MBR-01-REFERENCE_RUNTIME-PROP-01: 작은 Golden scenario와 표준 Test Kit를 재사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal`, `validation`, `authorization`, `failure`, `recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-MBR-02-FAILURE_PARITY — SAMPLE-MBR / Reference 오류·복구 Parity

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-MBR` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `FAILURE_PARITY` |
| 선행 작업 | `CPF-WP-SAMPLE-MBR-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

cpf-member를 Generator output과 동일한 Golden Reference Instance로 유지하고 normalize parity gate를 통과시킨다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-MBR-02-FAILURE_PARITY-REQ-01: Reference가 제품 Runtime과 동일한 error taxonomy, retry/unknown/recovery와 audit를 사용한다.
- [ ] CPF-WP-SAMPLE-MBR-02-FAILURE_PARITY-REQ-02: Local/Remote 또는 provider profile 차이를 동일 업무 계약으로 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-MBR-02-FAILURE_PARITY-PROP-01: fault profile과 documented operator workflow를 포함한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`local/remote parity`, `dependency failure`, `unknown`, `manual recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-MBR-03-REGENERATION_EVIDENCE — SAMPLE-MBR / Sample·Golden 재생성 Evidence

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-MBR` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REGENERATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-SAMPLE-MBR-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

cpf-member를 Generator output과 동일한 Golden Reference Instance로 유지하고 normalize parity gate를 통과시킨다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-MBR-03-REGENERATION_EVIDENCE-REQ-01: Generator 입력에서 Sample/Golden Domain을 재생성하고 normalized parity를 확인한다.
- [ ] CPF-WP-SAMPLE-MBR-03-REGENERATION_EVIDENCE-REQ-02: 사용자 영역과 교육 문서가 재생성 후 보존된다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-MBR-03-REGENERATION_EVIDENCE-PROP-01: remove/regenerate CI profile과 golden diff를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`remove/regenerate`, `user edit preserve`, `drift`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-REF-01-REFERENCE_RUNTIME — SAMPLE-REF / Reference 실제 Runtime

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-REF` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REFERENCE_RUNTIME` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

cpf-reference에서 제품 Public API의 정상·오류·경계·복구·권한·운영 사용법을 실제 Runtime으로 교육한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-REF-01-REFERENCE_RUNTIME-REQ-01: Sample/Reference가 제품 Public API와 Published Artifact를 실제 소비한다.
- [ ] CPF-WP-SAMPLE-REF-01-REFERENCE_RUNTIME-REQ-02: 별도 장난감 Contract나 Sample-only Engine을 만들지 않는다.
- [ ] CPF-WP-SAMPLE-REF-01-REFERENCE_RUNTIME-REQ-03: 정상·오류·권한·복구·운영 흐름을 실행 가능하게 제공한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-REF-01-REFERENCE_RUNTIME-PROP-01: 작은 Golden scenario와 표준 Test Kit를 재사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal`, `validation`, `authorization`, `failure`, `recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-REF-02-FAILURE_PARITY — SAMPLE-REF / Reference 오류·복구 Parity

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-REF` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `FAILURE_PARITY` |
| 선행 작업 | `CPF-WP-SAMPLE-REF-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

cpf-reference에서 제품 Public API의 정상·오류·경계·복구·권한·운영 사용법을 실제 Runtime으로 교육한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-REF-02-FAILURE_PARITY-REQ-01: Reference가 제품 Runtime과 동일한 error taxonomy, retry/unknown/recovery와 audit를 사용한다.
- [ ] CPF-WP-SAMPLE-REF-02-FAILURE_PARITY-REQ-02: Local/Remote 또는 provider profile 차이를 동일 업무 계약으로 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-REF-02-FAILURE_PARITY-PROP-01: fault profile과 documented operator workflow를 포함한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`local/remote parity`, `dependency failure`, `unknown`, `manual recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-REF-03-REGENERATION_EVIDENCE — SAMPLE-REF / Sample·Golden 재생성 Evidence

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-REF` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REGENERATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-SAMPLE-REF-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

cpf-reference에서 제품 Public API의 정상·오류·경계·복구·권한·운영 사용법을 실제 Runtime으로 교육한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-REF-03-REGENERATION_EVIDENCE-REQ-01: Generator 입력에서 Sample/Golden Domain을 재생성하고 normalized parity를 확인한다.
- [ ] CPF-WP-SAMPLE-REF-03-REGENERATION_EVIDENCE-REQ-02: 사용자 영역과 교육 문서가 재생성 후 보존된다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-REF-03-REGENERATION_EVIDENCE-PROP-01: remove/regenerate CI profile과 golden diff를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`remove/regenerate`, `user edit preserve`, `drift`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-BIZADM-01-REFERENCE_RUNTIME — SAMPLE-BIZADM / Reference 실제 Runtime

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-BIZADM` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REFERENCE_RUNTIME` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

BZA 선택형 업무관리/채번/결재 Customization Sample을 기본 활성화 없이 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-BIZADM-01-REFERENCE_RUNTIME-REQ-01: Sample/Reference가 제품 Public API와 Published Artifact를 실제 소비한다.
- [ ] CPF-WP-SAMPLE-BIZADM-01-REFERENCE_RUNTIME-REQ-02: 별도 장난감 Contract나 Sample-only Engine을 만들지 않는다.
- [ ] CPF-WP-SAMPLE-BIZADM-01-REFERENCE_RUNTIME-REQ-03: 정상·오류·권한·복구·운영 흐름을 실행 가능하게 제공한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-BIZADM-01-REFERENCE_RUNTIME-PROP-01: 작은 Golden scenario와 표준 Test Kit를 재사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal`, `validation`, `authorization`, `failure`, `recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-BIZADM-02-FAILURE_PARITY — SAMPLE-BIZADM / Reference 오류·복구 Parity

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-BIZADM` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `FAILURE_PARITY` |
| 선행 작업 | `CPF-WP-SAMPLE-BIZADM-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

BZA 선택형 업무관리/채번/결재 Customization Sample을 기본 활성화 없이 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-BIZADM-02-FAILURE_PARITY-REQ-01: Reference가 제품 Runtime과 동일한 error taxonomy, retry/unknown/recovery와 audit를 사용한다.
- [ ] CPF-WP-SAMPLE-BIZADM-02-FAILURE_PARITY-REQ-02: Local/Remote 또는 provider profile 차이를 동일 업무 계약으로 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-BIZADM-02-FAILURE_PARITY-PROP-01: fault profile과 documented operator workflow를 포함한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`local/remote parity`, `dependency failure`, `unknown`, `manual recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-BIZADM-03-REGENERATION_EVIDENCE — SAMPLE-BIZADM / Sample·Golden 재생성 Evidence

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-BIZADM` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REGENERATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-SAMPLE-BIZADM-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-NIST-SSDF` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

BZA 선택형 업무관리/채번/결재 Customization Sample을 기본 활성화 없이 제공한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-BIZADM-03-REGENERATION_EVIDENCE-REQ-01: Generator 입력에서 Sample/Golden Domain을 재생성하고 normalized parity를 확인한다.
- [ ] CPF-WP-SAMPLE-BIZADM-03-REGENERATION_EVIDENCE-REQ-02: 사용자 영역과 교육 문서가 재생성 후 보존된다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-BIZADM-03-REGENERATION_EVIDENCE-PROP-01: remove/regenerate CI profile과 golden diff를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`remove/regenerate`, `user edit preserve`, `drift`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-EDU-01-REFERENCE_RUNTIME — SAMPLE-EDU / Reference 실제 Runtime

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-EDU` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REFERENCE_RUNTIME` |
| 선행 작업 | 없음 |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE`, `GATE-21-TIME` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

교육 시나리오가 장난감 계약을 만들지 않고 실제 Header/API/DB/Event/Batch/Security 표준을 사용한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-EDU-01-REFERENCE_RUNTIME-REQ-01: Sample/Reference가 제품 Public API와 Published Artifact를 실제 소비한다.
- [ ] CPF-WP-SAMPLE-EDU-01-REFERENCE_RUNTIME-REQ-02: 별도 장난감 Contract나 Sample-only Engine을 만들지 않는다.
- [ ] CPF-WP-SAMPLE-EDU-01-REFERENCE_RUNTIME-REQ-03: 정상·오류·권한·복구·운영 흐름을 실행 가능하게 제공한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-EDU-01-REFERENCE_RUNTIME-PROP-01: 작은 Golden scenario와 표준 Test Kit를 재사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`normal`, `validation`, `authorization`, `failure`, `recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-EDU-02-FAILURE_PARITY — SAMPLE-EDU / Reference 오류·복구 Parity

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-EDU` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `FAILURE_PARITY` |
| 선행 작업 | `CPF-WP-SAMPLE-EDU-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-08-UNKNOWN-RECOVERY`, `GATE-09-SECURITY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

교육 시나리오가 장난감 계약을 만들지 않고 실제 Header/API/DB/Event/Batch/Security 표준을 사용한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-EDU-02-FAILURE_PARITY-REQ-01: Reference가 제품 Runtime과 동일한 error taxonomy, retry/unknown/recovery와 audit를 사용한다.
- [ ] CPF-WP-SAMPLE-EDU-02-FAILURE_PARITY-REQ-02: Local/Remote 또는 provider profile 차이를 동일 업무 계약으로 검증한다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-EDU-02-FAILURE_PARITY-PROP-01: fault profile과 documented operator workflow를 포함한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`local/remote parity`, `dependency failure`, `unknown`, `manual recovery`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
## CPF-WP-SAMPLE-EDU-03-REGENERATION_EVIDENCE — SAMPLE-EDU / Sample·Golden 재생성 Evidence

| 항목 | 값 |
|---|---|
| 우선순위 | **P2** |
| Canonical Requirement | `SAMPLE-EDU` |
| Canonical Owner | `cpf-reference / generated reference` |
| Work Package 유형 | `REGENERATION_EVIDENCE` |
| 선행 작업 | `CPF-WP-SAMPLE-EDU-01-REFERENCE_RUNTIME` |
| 적용 공통 Gate | `GATE-01-OWNERSHIP`, `GATE-02-CONSUMER`, `GATE-05-DB-QUERY`, `GATE-07-MULTI-INSTANCE`, `GATE-09-SECURITY`, `GATE-13-PERFORMANCE`, `GATE-15-GENERATOR`, `GATE-18-TEST-EVIDENCE`, `GATE-20-HYGIENE` |
| 적용 표준 Profile | `STD-CISA-SBD`, `STD-NIST-SSDF`, `STD-OWASP-ASVS` |
| Baseline | `faedf43a7baffdad456bf40f8e46d622db9cfc76` 후보; 작업 시작 시 최신 origin/master 재확인 |
| 최종 완료 | 개발 GPT 완료 후보이며 Codex·QA 최신 Git 통과 전 최종 완료 아님 |

### Canonical 제품 목표

교육 시나리오가 장난감 계약을 만들지 않고 실제 Header/API/DB/Event/Batch/Security 표준을 사용한다.

### 탐색·변경 범위

- generator/templates/codegen, golden generated domain, samples, EDU and quick-start artifacts
- 실제 경로가 다르면 Canonical Owner와 Consumer를 따라 기존 정식 경로를 사용한다.
- 새 Class/Package/Library/알고리즘은 비강제 제안이며 정본·표준·기존 Architecture가 우선한다.

### 실제 Consumer

new customer domain, developer, cpf-member and cpf-reference

### 필수 개발 결과

- [ ] CPF-WP-SAMPLE-EDU-03-REGENERATION_EVIDENCE-REQ-01: Generator 입력에서 Sample/Golden Domain을 재생성하고 normalized parity를 확인한다.
- [ ] CPF-WP-SAMPLE-EDU-03-REGENERATION_EVIDENCE-REQ-02: 사용자 영역과 교육 문서가 재생성 후 보존된다.

### 비강제 구현 제안

- [ ] CPF-WP-SAMPLE-EDU-03-REGENERATION_EVIDENCE-PROP-01: remove/regenerate CI profile과 golden diff를 사용한다.

제안을 사용하지 않아도 된다. 다만 동등 이상의 표준 준수, 호환성, 오류·복구·보안·운영, Test와 Evidence를 제공하고 대안 선택 근거를 기록한다.

### 필수 Scenario Class

`remove/regenerate`, `user edit preserve`, `drift`

연결된 실제 CPF-SC Scenario를 전수 매핑한다. 현재 원장에 필요한 Class가 없으면 중복 검사를 거쳐 `SELF_DISCOVERED_SCENARIO`로 등록한다.

### 직접검증 기준

- fresh clone create→build→runtime→remove→regenerate
- 적용 공통 Gate의 직접검증 항목
- 목표 환경에서 먼저 실제 실행하고 명령·환경·exit code·결과를 기록

### 환경 제약 시 대체검증

- template/golden/normalized-tree and offline generated consumer compile
- 적용 공통 Gate의 대체검증 항목
- Source·Test·Harness 구현을 중단하지 않고 실제 환경에서만 남는 차이를 구체화

### 완료 차단 조건

- 실제 Product Consumer와 전체 호출 경로가 확인되지 않음
- 적용 Gate 중 하나라도 미판정 또는 근거 없는 N/A
- 목표 환경 직접검증 미시도와 대체검증 미수행
- 연결 CPF-FR/CPF-SC 미검수 또는 Evidence 없는 판정
- 다른 Owner Internal/DB 직접 참조, Dual Primary, Generated 수동 수정

---
