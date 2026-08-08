# CPF Final QA A/B 중앙 Merge 판정

- QA Source basis SHA: `3aa1dd12f8a5938d33feb6ed598b3dd2442bf2e2` (`07_05`)
- Final Control currentization basis SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- QA A package SHA-256: `b4e8929066517bf122ef2ea2d9fd54a7b43f29f5a37e14afa0839700cb0e203b`
- QA B package SHA-256: `a1929d223125cc93182013030bf141856125efc35581fae8b7b7906b00336f95`
- 중앙 판정: **FAIL / REDEVELOPMENT REQUIRED + UNVERIFIED / RELEASE_BLOCKED**

## 1. 중앙 분모

최상위 프로젝트 완료 분모는 **Canonical 169 Requirement**다.
93 개발 원장, 기존 Finding 56, QA A 신규 25, QA B 신규 8, 중앙 정규화 Action 31은 모두 입력·결함 추적 단위이며 프로젝트 완료율 분모가 아니다.

QA A 25 + QA B 8에서 Root Cause 중복 2개 계열을 통합해 **중앙 신규 Action 31(P0 22 / P1 9)**로 정규화했다.
Scope의 상한은 31건이 아니라 Canonical 169 전체 + 기존 56 + 신규 31 + self-found + Runtime 13 + 개발 중 추가발견 전체다.

## 2. 중앙 Source 재확인 결함

QA Merge 당시 current Product Source에서 다음 Root Cause를 중앙이 직접 재확인했다.

- Approval terminal UPDATE의 fencing 조건 미완성
- Batch Runtime UNKNOWN reconcile의 부분문자열 identity matching
- Center-Cut 비종료 상태의 terminal 성공 오판 가능성
- TransactionId trust-boundary가 정식 Channel과 비신뢰 injection을 명확히 구분하지 못하는 문제
- FileLog spool의 tmpdir/dedup/replay durability 문제

위 항목은 current Product Source Finalization에서 반드시 닫는다.

## 3. 중앙 Architecture 결정

1. **Core persistence**: `cpf-core`는 topology-independent API/SPI/기술 계약만 소유하며 MyBatis/JDBC 구현은 downstream Provider/Starter가 소유한다.
2. **Transaction lineage**: `cpf_transaction_lineage`는 normalized operational lineage projection/index다. 기존 Domain/Message/Batch 저장소를 대체하는 dual-primary가 아니다.
3. **TransactionId**: 정식 거래 기동 Channel/System은 CPF 규격 transactionId를 최초 1회 생성할 수 있다. 이후 동일 거래의 모든 참여 시스템은 같은 transactionId를 End-to-End로 승계한다. Retry는 같은 transactionId + attempt 증가다. 비신뢰 주체의 spoof/replay/manipulation만 인증된 Channel/System identity와 trust policy로 차단한다. **모든 inbound transactionId 일괄 재생성은 금지한다.**
4. **EDU-ADM**: PRODUCT_ADM/MERGE_EDU는 runtime Product Handler가 아니다. EXTENSION_SAMPLE만 실행형 Extension Example로 유지한다.
5. **EDU retained role**: 실행형 EDU-ADM 02/03/04/07은 canonical `CPF_ADM_OPERATOR` 계약을 따른다.
6. **Retired BZA API**: compatibility 410은 필요 시 남길 수 있으나 active OpenAPI/generated client/consumer count에서는 제외한다.
7. **HIGH/CRITICAL Frontend**: strict generated typed client gate를 유지하고 raw mutation 우회를 허용하지 않는다.
8. **FileLog spool**: managed durable spool root + autonomous retry/replay/dedup/safety를 제공한다.

## 4. 역할 경계

### 중앙 관리자
README/Guide/고객 PDF·DOCX를 제외한 프로젝트 목표, Governance, Canonical Requirement, Architecture/Specification 제품 계약, Module Ownership, Current Control, QA Merge와 문서 상호 정합성을 관리·현행화한다.

### Product Developer GPT
Product Source/SQL/API/SPI/Test/Config/Frontend/Generator/Runtime Gate와 자기 개발 결과/Evidence를 수정한다.
중앙 정본·Final QA 원본을 임의 변경하지 않고 정본 모호성은 `PROJECT_DOCUMENT_ALIGNMENT_REQUEST.csv`로 보고한다.

### QA A/B
동일 전체 범위를 독립 전수검수하고 상대 판정을 승계하지 않는다.

### Documentation Finalization
다음 고객 문서를 별도 관리한다.
- `README.md`
- `cpf-docs/guides/**`
- `cpf-docs/deliverables/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

## 5. 최종 개발 목표

이번 개발은 subset closure가 아니라 **CPF Product Source Finalization**이다.
부분 구현, 구현 가능한 미구현, P0/P1, false-green verification, Consumer 단절, Ownership 위반을 계획 이월하지 않는다.

## 6. Project Control currentization

이 문서와 `CENTRAL_FINAL_ACTIONS.csv`, `ROLE_BOUNDARY.md`, Final Developer instruction은 중앙 현행화에서 TransactionId와 역할 경계를 동일하게 맞췄다.
과거 QA38/QA39/V7/V9 Session Control과 날짜별 중복 자료는 current canonical이 아니며 exact Delete Manifest로 정리한다.
