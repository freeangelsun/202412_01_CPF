# CPF Requirement Continuity Ledger

> Canonical path: `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
> Synchronized with Final Target revision: `2026-08-02`
> Synchronization review baseline: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
> Final Target reviewed blob: `262077e913db1d83731c0f3b643565859af431c1`

## 1. 목적

이 문서는 Requirement ID가 PC, 세션, AI 계정, 작업차수, Architecture Rename 또는 Owner 이동 때문에 사라지거나 중복 집계되는 것을 방지하는 영속 추적 정본이다.

최상위 제품 의미와 상세 완료 증명은 다음 파일이 소유한다.

`cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`

이 Ledger는 다음만 소유한다.

- Canonical Requirement Count
- Requirement ID 생성·분해·통합·폐기 이력
- Legacy Alias Mapping
- Owner 이동과 의미 연속성
- 신규 Requirement Intake 절차
- QA/작업 패키지 ID와 제품 Requirement ID의 구분

## 2. 현재 Canonical 상태

| 구분 | 수량 | 완료율 집계 |
|---|---:|---|
| Canonical Product Requirement | **169개** | 포함 |
| Legacy Alias | **8개** | 제외 |
| QA33 Remediation Requirement | **138개** | 별도 작업 원장, 제품 Requirement 수에 미포함 |
| QA33 Mandatory Scenario | **414개** | 별도 검증 원장, 제품 Requirement 수에 미포함 |

2026-08-02 신규 정본화 이후 Canonical Product Requirement는 **169개**다.

QA33의 `QA33-REQ-*`, `QA33-DF-*`, `QA33-SC-*`는 특정 Source 결함 수정과 검증을 위한 **작업 패키지 ID**다. 이 ID를 162개 Canonical Product Requirement에 추가하거나 완료율에 합산하지 않는다.

작업 패키지의 각 행은 반드시 하나 이상의 Canonical Product Requirement와 연결해야 한다. 연결이 없으면 `REQ-GAP` 절차로 신규 제품 Requirement 필요성을 먼저 검토한다.

## 3. Count 변화 이력

| 기준 | Canonical Count | 설명 |
|---|---:|---|
| `a63380e6c736fa9c5ae7e425d0e301d21ef3b848` | 133 | 과거 Catalog |
| `22b1874e67547372b51a4bcd21f47aea6fcb5c25` | 126 | 42개 제거·35개 추가, Mapping 부족 |
| Requirement 연속성 보정 | 160 | 의미가 남은 34개 복구, 8개 Alias 분리 |
| 전수검수 신규 정본화 | **162** | `ADM-APPROVAL`, `BZA-ORG` 신규 추가 |
| 2026-07-31 상세 현행화 | **162** | ID 증감 없이 Owner·최소 목표·완료 증명 상세화 |
| 2026-08-02 누락 요구 복구 | **169** | Starter Architecture, Fresh DB, MQ/JMS/IBM MQ/RabbitMQ/TCP 7개 신규 정본화 |

2026-07-31 현행화는 Requirement 추가·삭제가 아니다. 기존 162개 각각에 상세 Owner, 최소 제품 목표와 필수 완료 증명을 부여한 정본 강화다.

## 4. Legacy Alias Mapping

| Legacy ID | 현재 Canonical 추적 대상 | 정책 |
|---|---|---|
| `FACADE-LOCAL` | `ARCH-MSA + CPF-CALL` | 과거 검색 Key만 유지, 중복 집계 금지 |
| `FACADE-REMOTE` | `ARCH-MSA + CPF-CALL` | 과거 검색 Key만 유지, 중복 집계 금지 |
| `CMN-ID` | `CPF-TXID + BZA-SEQUENCE-SAMPLE/업무 Domain` | 기술 ID와 업무 채번을 분리 |
| `CMN-FILE` | `CORE-FILE` | 중복 집계 금지 |
| `CMN-FIXED` | `CORE-FIXED` | 중복 집계 금지 |
| `ADM-COMP` | `ADM-RECOVERY` | 중복 집계 금지 |
| `CENTER-ADV` | `CENTER-RUNNER + CENTER-PARAM + CENTER-CLAIM + CENTER-RATE + CENTER-REPROCESS + CENTER-UNKNOWN + CENTER-OPS` | 분해 관계 |
| `API-GATEWAY` | `GWY-ENTRY + GWY-ROUTING + GWY-TRUST + GWY-RESILIENCE + API-CONTRACT` | 분해 관계 |

## 5. 복구 Requirement 34개

다음 ID는 133→126 축소 중 사라졌으나 제품 의미가 계속 존재해 Canonical Catalog로 복구했다.

- `CPF-ROLE`, `CPF-OPSDB`, `CPF-LOGFAIL`, `CPF-SCHED`
- `CMN-CODE`, `CMN-MSG`, `CMN-CALENDAR`, `CMN-TEMPLATE`
- `ADM-SERVICE`, `ADM-LOG`, `ADM-INCIDENT`, `ADM-UX`
- `SEC-APP`
- `OPS-SELF`, `OPS-TOPOLOGY`, `OPS-MAINT`
- `DB-SQL`, `DB-PERF`, `DB-MULTI`
- `DATA-LINEAGE`, `DATA-RETENTION`
- `API-LIMIT`
- `DEVEX-COMMENT`
- `RULE-ARCH`, `RULE-SEC`, `RULE-QUALITY`
- `PROD-EDITION`, `PROD-MULTITENANT`, `PROD-PLUGIN`, `PROD-PACKAGE`
- `REQ-GAP`
- `BAT-CALL-SYNC`, `BAT-CALL-ASYNC`, `BAT-SHARED`

각 ID의 현재 의미와 완료 증명은 Final Target 상세 Catalog가 정본이다. 이 Ledger에서 별도 축약 정의를 다시 만들어 내용이 갈라지지 않게 한다.

## 6. 신규 정본화 Requirement

| ID | 신규 정본화 사유 |
|---|---|
| `ADM-APPROVAL` | 플랫폼 위험조치 승인 Runtime의 Owner Command 실행, 결과 불명, Break-glass와 Immutable Audit를 독립 추적해야 함 |
| `BZA-ORG` | 조직·직원·직급·직책·유효기간 Assignment와 업무결재 Snapshot을 독립 제품 기능으로 추적해야 함 |


## 6.1 2026-08-02 사용자 요구 복구

| ID | 생성 근거 | 기존 Requirement와의 관계 |
|---|---|---|
| `ARCH-STARTER` | `cpf-starters` 정식 Root 채택, Core 경량화, Leaf/Profile/Aggregate/BOM 계층을 독립 완료 축으로 추적 | `ARCH-BOUNDARY`, `PROD-PACKAGE`, `DEVEX-CODEGEN` 상세화만으로는 Provider 충돌·Profile Lock·Core Footprint 상태를 독립 추적할 수 없어 신규 |
| `DB-FRESH` | Codex/QA 전 DB를 초기 상태에서 Generator-first로 설치·Upgrade·Rollback·Reapply해야 한다는 사용자 영구 원칙 | `DB-INSTALL`, `DB-MIGRATION`, `DB-MULTI-VENDOR`를 실제 초기화 절차로 묶는 독립 검증 Requirement |
| `EVENT-MQ` | Kafka 외 Queue Messaging 공통 의미와 운영 계약 복구 | 기존 Kafka 중심 `EVENT-BROKER`와 Provider 계약 사이의 공통 Queue Capability |
| `EVENT-JMS` | JMS 공통 Runtime 요구 복구 | Provider-neutral Jakarta JMS 계약 |
| `EVENT-IBM-MQ` | IBM MQ Provider 지원 요구 복구 | `EVENT-JMS` 위의 Provider별 연결·보안·복구 |
| `EVENT-AMQP` | RabbitMQ/AMQP 지원 요구 복구 | Kafka/JMS와 다른 confirm/ack/DLX 의미를 독립 추적 |
| `EXS-TCP` | 영속 TCP 전문통신 요구 복구 | `EXS-FIXED`, `EXS-UNKNOWN`, `EXS-RECON`을 실제 Connection Runtime과 연결 |

### 사용자 입력 검색 Alias

다음 표기는 Requirement Count에 포함하지 않지만 검색과 대화 연속성을 위해 유지한다.

| 사용자 입력 | Canonical 추적 |
|---|---|
| `MQ` | `EVENT-MQ` |
| `JMS` | `EVENT-JMS` |
| `IBM MQ` | `EVENT-IBM-MQ` |
| `RabbitMQ` | `EVENT-AMQP` |
| `TPC` | 후속 확인 전 `EXS-TCP`에 연결하며 원문 표기를 보존 |
| `TCP` | `EXS-TCP` |


## 7. Requirement 변경 절차

### 7.1 신규

1. 기존 169개와 의미·Owner·완료 증명을 비교한다.
2. 기존 ID의 상세화로 해결되면 신규 ID를 만들지 않는다.
3. 독립 Owner·Consumer·상태기계·완료 증명이 필요하면 `REQ-GAP` 기록을 생성한다.
4. Final Target에 ID와 상세 Catalog를 먼저 추가한다.
5. 이 Ledger에 생성 근거와 Count 변화를 기록한다.
6. Matrix·Guide·Test·Evidence의 참조를 갱신한다.

### 7.2 분해

- 기존 ID를 삭제하지 않는다.
- `split-into` 관계와 분해 이유를 남긴다.
- 기존 Evidence와 신규 ID의 적용 범위를 Mapping한다.
- 전환 기간에도 완료율을 중복 집계하지 않는다.

### 7.3 통합

- 기존 ID를 `superseded-by`로 남긴다.
- 통합된 Acceptance가 누락되지 않았음을 증명한다.
- 과거 검색·Evidence Key를 유지한다.

### 7.4 폐기

제품 정책에서 요구 자체를 폐기할 때만 허용한다.

필수:

- 폐기 이유
- 사용자·API·DB·배포 영향
- 대체 Requirement
- Migration과 호환성
- 사용자 승인 근거
- 완료율 Count 변경

## 8. QA·개발 패키지와 Canonical Requirement 연결

QA Matrix는 다음 Column 또는 동등한 구조를 가져야 한다.

- `record_id`
- `canonical_requirement_ids`
- `defect_or_gap`
- `owner_module`
- `source_scope`
- `consumer_scope`
- `acceptance`
- `evidence`
- `development_status`
- `verification_status`

`canonical_requirement_ids`가 비어 있으면 다음 중 하나로 처리한다.

- 기존 Requirement 연결 누락: 수정
- Repository Hygiene·요청서 자체 결함: `REQ-GOV`, `REQ-REVIEW`, `REQ-CODEX` 중 연결
- 실제 신규 제품 요구: `REQ-GAP` 검토
- 오판 또는 범위 외: 근거와 함께 기각

## 9. 완료율 계산 규칙

- 분모는 Canonical 169개다.
- Legacy Alias, QA Defect, QA Scenario, OSS Migration Decision ID를 분모에 합산하지 않는다.
- Requirement 하나가 여러 QA 행에 연결돼도 한 번만 집계한다.
- `완료`는 Final Target 공통 완료 축과 해당 Requirement의 필수 완료 증명을 모두 만족할 때만 가능하다.
- 하나의 적용 Scenario라도 `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`면 해당 Requirement는 완료가 아니다.
- 과거 Evidence가 현재 Source·Artifact에 유효하지 않으면 다시 연다.

## 10. 영구 운영 규칙

1. 모든 작업자는 Final Target과 이 Ledger를 함께 읽는다.
2. Final Target Count와 이 Ledger Count가 다르면 작업을 중단하고 정합성을 복구한다.
3. Current Request는 작업 패키지 ID와 Canonical Requirement를 명확히 구분한다.
4. 과거 Review·Evidence의 ID를 소급 변경하지 않는다.
5. 이름만 바꾼 동일 Gap을 신규 Requirement로 중복 등록하지 않는다.
6. Owner 이동은 Source·Consumer·SQL·Test·Guide·Evidence를 함께 이관한다.
7. 사용자 승인 없는 Canonical Requirement 삭제·Count 감소를 금지한다.

<!-- CPF_QA38_CONTINUITY_START -->
## QA38 Recovery·Currentization
- Canonical 169 유지
- RabbitMQ/AMQP·JMS 승인 없는 제외 무효
- IBM MQ/JMS 분리
- TPC Alias→EXS-TCP
- Core→Starter 30개와 Final Matrix로 승계
- 날짜별 문서는 History 흡수 후 exact 삭제
- 사용자 승인 없는 Requirement 제거 금지
<!-- CPF_QA38_CONTINUITY_END -->
