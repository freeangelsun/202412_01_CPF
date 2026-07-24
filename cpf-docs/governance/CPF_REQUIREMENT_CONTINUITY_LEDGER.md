# CPF Requirement Continuity Ledger

## 1. 목적

이 문서는 Requirement ID가 PC, Codex 계정, 세션 또는 Architecture Rename 때문에 사라지는 것을 막는 영속 추적 정본이다.

- 기준 과거 정본: `a63380e6c736fa9c5ae7e425d0e301d21ef3b848` — 133개
- 검수 기준 정본: `22b1874e67547372b51a4bcd21f47aea6fcb5c25` — 126개
- 이번 재조정/신규 정본화 Canonical Count: **162개**
- Legacy Alias: **8개**, 완료율 중복 집계 금지
- 복구 Requirement: **34개**

## 2. Count 변경 원인

126개 정본은 133개 정본에서 42개 ID가 제거되고 35개 신규 ID가 추가되어 만들어졌다. 이 과정에 명시적 Old→New Mapping이 없었던 것이 추적성 결함이다. 이번 보정에서는 실제 의미가 계속 필요한 34개를 Canonical Catalog로 복구하고, Owner 이동/세분화에 해당하는 8개는 Alias로 남긴다.

## 3. Legacy Alias Mapping

| Legacy ID | 현재 추적 대상 | 정책 |
|---|---|---|
| `FACADE-LOCAL` | `ARCH-MSA + CPF-CALL` | Legacy ID 보존, 완료율 중복 집계 금지 |
| `FACADE-REMOTE` | `ARCH-MSA + CPF-CALL` | Legacy ID 보존, 완료율 중복 집계 금지 |
| `CMN-ID` | `CPF-TXID(기술 ID/추적) + BZA-SEQUENCE-SAMPLE/업무 Domain(업무 채번)` | Legacy ID 보존, 완료율 중복 집계 금지 |
| `CMN-FILE` | `CORE-FILE` | Legacy ID 보존, 완료율 중복 집계 금지 |
| `CMN-FIXED` | `CORE-FIXED` | Legacy ID 보존, 완료율 중복 집계 금지 |
| `ADM-COMP` | `ADM-RECOVERY` | Legacy ID 보존, 완료율 중복 집계 금지 |
| `CENTER-ADV` | `CENTER-RUNNER + CENTER-PARAM + CENTER-CLAIM + CENTER-RATE + CENTER-REPROCESS + CENTER-UNKNOWN + CENTER-OPS` | Legacy ID 보존, 완료율 중복 집계 금지 |
| `API-GATEWAY` | `GWY-ENTRY + GWY-ROUTING + GWY-TRUST + GWY-RESILIENCE + API-CONTRACT` | Legacy ID 보존, 완료율 중복 집계 금지 |

## 4. 복구된 Canonical Requirement

- `CPF-ROLE` — transactionRole, direction, source/target를 표준 거래 문맥과 로그/추적에 유지
- `CPF-OPSDB` — 운영 DB 공유/장애모드, 로그 DB 장애 시 Runtime 영향과 복구 정책
- `CPF-LOGFAIL` — DB 로그 실패 시 fail-open/fail-closed 정책, local spool, 재전송과 유실 탐지
- `CPF-SCHED` — 기술 Scheduler 표준과 cpf-batch Scheduler Runtime의 경계
- `CMN-CODE` — 고객 업무 공통 코드/참조데이터 확장 계약
- `CMN-MSG` — 고객 업무 공통 메시지/다국어/오류 메시지 확장 계약
- `CMN-CALENDAR` — 영업일/휴일/기관 캘린더 확장 계약
- `CMN-TEMPLATE` — 고객 업무 템플릿/알림 확장 계약; 기술 메시징 엔진과 분리
- `ADM-SERVICE` — Service Instance/Routing/Health 운영 관제
- `ADM-LOG` — 로그 정책/Trace Boost 운영 화면과 제어
- `ADM-INCIDENT` — Alert/Incident/Runbook 연결과 운영 처리
- `ADM-UX` — 운영 검색, 대시보드, 저장조건, 안전 다운로드 UX
- `SEC-APP` — Application Security 기본 통제, 보안 헤더/입력/출력/취약 구성
- `OPS-SELF` — Self-healing 정책과 자동조치 안전장치
- `OPS-TOPOLOGY` — Topology/Dependency Map/Service Catalog
- `OPS-MAINT` — Maintenance Mode, Drain, 신규 유입 차단과 안전 복귀
- `DB-SQL` — SQL/MyBatis/Index/Query 표준과 정적·실행 검증
- `DB-PERF` — DB 성능, Index, Partition, 대용량 Retention/Purge 성능
- `DB-MULTI` — Multi Datasource/Read Replica/Read-Write routing; Multi-Vendor와 별도 요구
- `DATA-LINEAGE` — 데이터 계보, 품질, Reconciliation 추적
- `DATA-RETENTION` — Retention/Purge/Legal Hold/Archive와 감사
- `API-LIMIT` — Rate Limit/Quota/Abuse Detection
- `DEVEX-COMMENT` — JavaDoc, 공개 API/설정 주석, 한글 운영설명 표준
- `RULE-ARCH` — Architecture Ownership/Dependency 자동 Gate
- `RULE-SEC` — Secret/URL/취약 설정/보안 규칙 Gate
- `RULE-QUALITY` — Static Analysis/Dependency/License/중복/Dead Code 품질 Gate
- `PROD-EDITION` — 상용 Edition/License 정책 후보를 장기 Backlog로 추적
- `PROD-MULTITENANT` — Multi-tenant/Multi-customer 확장 후보를 장기 Backlog로 추적
- `PROD-PLUGIN` — Plugin/Adapter/Marketplace 확장 후보를 장기 Backlog로 추적
- `PROD-PACKAGE` — 산업군별 Package/Distribution 확장 후보를 장기 Backlog로 추적
- `REQ-GAP` — 새 Future Requirement를 기존 ID와 중복 없이 Intake하는 절차
- `BAT-CALL-SYNC` — Batch→업무 Domain 동기 호출과 Local/Remote parity
- `BAT-CALL-ASYNC` — Batch→Event/Outbox 비동기 호출, 재시도/중복/복구
- `BAT-SHARED` — Batch가 SHARED/온라인 Facade를 재사용할 때의 계약과 의존성 경계

## 5. 이번 검수에서 신규 정본화한 Requirement

- `ADM-APPROVAL` — `cpf-admin`이 소유하는 플랫폼 위험조치 승인 Runtime. 일반 보안 승인 통제와 별개로 Owner Command 실행/Unknown/Break-glass까지 추적한다.
- `BZA-ORG` — BZA 조직·직원·직급·직책·다중 Role·유효기간 Assignment와 결재 Snapshot Directory 모델.

이 두 ID는 과거 ID의 Rename이 아니라 `22b1874` 전수검수에서 독립 완료 판정이 필요하다고 새로 발견한 Requirement다.

## 6. 영구 운영 규칙

1. 다음 요청서 작성 전 이 Ledger와 Final Target을 함께 비교한다.
2. ID를 Rename하거나 분해할 때 Mapping을 먼저 추가한 뒤 Catalog를 변경한다.
3. 완료율 계산은 Canonical ID만 사용한다.
4. Legacy Alias가 Source/Evidence 검색 Key로 남아 있어도 중복 완료로 집계하지 않는다.
5. 과거 문서나 Git에서 새 의미가 발견되면 `REQ-GAP` 절차로 누락 여부를 검토한다.
