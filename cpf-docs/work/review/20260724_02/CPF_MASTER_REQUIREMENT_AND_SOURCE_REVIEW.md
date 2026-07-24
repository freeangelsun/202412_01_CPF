# CPF Master Requirement & Source Review — 2026-07-24

## 1. Baseline

- Repository: freeangelsun/202412_01_CPF
- Branch: master
- Commit: `22b1874e67547372b51a4bcd21f47aea6fcb5c25`
- 검수 성격: Codex 대규모 WIP Push 후 요구사항↔Source/SQL/Consumer/문서 역추적
- 이 Review는 Overlay가 실제 Runtime 검증을 대신하지 않는다.

## 2. 총평

22b1874는 완제품 Commit이 아니라 올바른 방향의 리팩터링과 신구 구조가 동시에 존재하는 전환 WIP다. Central Vendor Pack, Metadata Generator, Core Fixed-Length, batDB 물리 분리, cmnDB minimal sample 등은 살려야 할 기반이다. 반면 generated DB bundle 불일치, module-local Vendor fallback, MBR DDL/Mapper 충돌, ADM cross-owner DB access, ADM Approval 미구현, BZA Approval skeleton, CenterCutRunner 미구현, migration/evidence 정합성 문제는 P0다.

과거 133 Requirement가 126으로 감소한 원인은 42 ID 제거 + 35 ID 신규 추가였다. 8개는 명시적 alias/split으로 Mapping할 수 있지만 34개는 제품 의미가 여전히 독립적이므로 복구했다. Overlay Canonical Catalog는 162개이며 향후 Mapping 없이 Count를 줄이지 않는다.

Overlay는 중앙 Vendor Pack fail-fast 전환을 실제 사용 경로까지 노출하기 위해 `scripts/runtime-start-services.ps1`도 보강한다. 로컬 통합검증 Harness는 `CPF_DB_VENDOR`와 `CPF_DB_RESOURCE_ROOT`를 Java child process에 전달하지만, 이는 Runtime 검증 성공 근거가 아니며 실제 실행은 다음 Codex가 수행해야 한다.

## 3. 상태 집계

이 집계는 `22b1874e67547372b51a4bcd21f47aea6fcb5c25` Source 정적/구조 검수 기준이다. Overlay 이후 Runtime을 실행하지 않았으므로 `완료`로 승격한 항목은 없다.

- 미검증: 12
- 미구현: 6
- 부분 구현: 127
- 실패: 10
- 재확인 필요: 7

## 4. P0 구조 문제

### 4.1 DB 정본

22b 기준 split DDL은 107 Table인데 generated/central MariaDB install은 115로 불일치했다. 구 MBR/BZA Table이 installer에 남아 새 DDL의 오류를 가리고 있었다. Overlay는 ADM/BZA의 실제 누락 요구를 반영해 정적 split/generated Table 수를 123으로 맞췄지만 실제 MariaDB Fresh Install 전이므로 `미검증`이다.

### 4.2 Vendor SQL 이중화

Central `cpf-tools/db/vendor/<vendor>`와 module `src/main/resources/sql/vendor`, `mybatis/vendor`가 공존하고 resolver/catalog fallback이 이를 소비했다. 이 구조는 중앙 연결 실패를 숨긴다. Overlay는 Production fallback을 제거하고 cleanup allowlist를 제공한다. 삭제 후 깨지는 Consumer는 중앙 Pack으로 교정해야 하며 삭제 파일 복구는 금지한다.

### 4.3 MBR/ACC Reference Domain

MBR split은 `mbr_sample_item` 하나지만 기존 Member/Auth Mapper는 `mbr_member`, login history, refresh token 등에 의존한다. Installer의 구 Table이 이를 가렸다. ACC도 `acc_account`/change log 업무 모델에서 공통 minimal sample로 완전히 전환되지 않았다. Generator와 Reference Domain의 논리 Template을 하나로 닫아야 한다.

### 4.4 ADM Boundary/Approval

ADM이 batDB/refDB 등 Owner DB를 직접 접근하는 구조는 분리 WAS/MSA를 깨뜨린다. ADM Approval도 실제 table/engine/API/UI가 없었다. Overlay는 Directory Profile과 Approval DDL/API Enum baseline만 제공한다. 최종 구현은 승인 후 Owner Command API를 호출하고 result/unknown/recovery까지 추적해야 한다.

### 4.5 BZA 조직/결재

기존 bza_organization/employee/approval_*가 있었지만 기업 결재 요구에는 부족했다. 특히 단일 role_code, 단일 조직, 사람별 approval line, 실제로 사용되지 않는 decision_rule/delegation은 부서 합의/ALL/ANY/N_OF_M/위임/조직개편 재현을 충족하지 못한다. Overlay는 Position/JobTitle/Assignment/multi-role/Policy/Participant/Delegation 구조와 DB Check Constraint를 추가하고, 기존 direct-line INSERT는 새 Target column과 정렬하며 구현되지 않은 ANY/N_OF_M을 ALL로 오판하지 않도록 fail-closed 처리했다. 그러나 이것은 정책 기반 Engine이 아니므로 Engine/API/UI는 Codex가 실제 구현·검증해야 한다.

### 4.6 Batch/Center-Cut

batDB 분리는 올바른 방향이지만 Runtime 책임이 cpf-core에 남아 있고 독립 CenterCutRunner를 확인하지 못했다. cpf-batch로 Runtime Ownership을 완결하고 multi-instance/fencing/failure recovery Evidence가 필요하다.

### 4.7 Evidence/Migration

Stale evidence가 재유입됐고 Index와 실제 path가 달랐다. Historical Flyway V6/V29 checksum 불일치도 미해결이다. 둘 다 문서상 성공으로 덮으면 안 된다.

## 5. Garbage 판정

### 삭제 확정

- `cpf-docs/evidence/20260722_01`
- `cpf-core/src/main/resources/sql/vendor`
- `cpf-biz-admin/src/main/resources/sql/vendor`
- `cpf-core/src/main/resources/mybatis/vendor`
- `cpf-common/src/main/resources/mybatis/vendor`
- `cpf-member/src/main/resources/mybatis/vendor`
- `cpf-account/src/main/resources/mybatis/vendor`
- `cpf-reference/src/main/resources/mybatis/vendor`

### 삭제 금지/재확인

- Historical Flyway: 보존
- EXS 7 Table: 현재 Owner/Consumer가 있어 보존 후 요구 재확인
- REF Center-Cut sample target/result: 실제 EDU consumer 존재
- Service Call/Broker/Header/Trace/Core Fixed-Length: Protected Baseline

## 6. 전체 Canonical Requirement 판정

| Requirement | 상태 | 검수 메모 |
|---|---|---|
| `ARCH-MISSION` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ARCH-MSA` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ARCH-BOUNDARY` | 실패 | ADM이 BAT/REF 등 Owner DB를 직접 접근하는 구현 확인. Local/Remote Owner Contract로 교정 필요. |
| `ARCH-LAYER` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CORE-API` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CORE-SPI` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CORE-CONFIG` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CORE-TESTKIT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-CALL` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-REGISTRY` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-ROUTING` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-HEALTH` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-HEADER` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-CONTEXT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-TXID` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-ROLE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-ERROR` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-VALID` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-IDEMP` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-STATE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-LOCK` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-RESILIENCE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-DEADLINE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-SCHED` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-OPSDB` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-LOGDB` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-FILELOG` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-LOGFAIL` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-TRACE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CPF-MASK` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CORE-FIXED` | 부분 구현 | Core API/SPI 이동과 EDU 연결 방향은 이번 baseline에서 가장 양호한 구현 중 하나. |
| `CORE-FILE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CORE-MESSAGE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CMN-EXTENSION` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CMN-SAMPLE-DB` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CMN-CODE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CMN-MSG` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CMN-CALENDAR` | 재확인 필요 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CMN-TEMPLATE` | 재확인 필요 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DB-OWNERSHIP` | 실패 | admDB/batDB 등 물리 분리는 진행됐으나 ADM cross-owner DataSource가 Ownership을 침해. |
| `DB-INSTALL` | 실패 | 22b baseline split=107, generated=115 불일치. Overlay는 정적으로 123으로 재생성했으나 MariaDB 실행 전. |
| `DB-MIGRATION` | 실패 | Historical Flyway V6/V29 checksum 불일치 미해결. 임의 수정 금지. |
| `DB-ROLLBACK` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DB-BACKUP` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DB-MULTI-VENDOR` | 부분 구현 | Central Pack 방향은 맞으나 baseline에는 module-local fallback이 공존. Overlay cleanup/fail-fast 후 Runtime 교정 필요. |
| `DB-SQL` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DB-PERF` | 재확인 필요 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DB-MULTI` | 재확인 필요 | 복구 Requirement. DB Vendor 지원과 별개인 Multi Datasource/Read Replica 요구가 사라졌던 것을 복원. |
| `DATA-LINEAGE` | 재확인 필요 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DATA-RETENTION` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `GWY-ENTRY` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `GWY-ROUTING` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `GWY-TRUST` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `GWY-RESILIENCE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `API-LIMIT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EXS-INST` | 부분 구현 | EXS 7개 테이블은 현재 제품 Owner/실제 Consumer가 있어 단순 sample garbage로 삭제하면 안 됨. |
| `EXS-REST` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EXS-FIXED` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EXS-SEC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EXS-FILE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EXS-UNKNOWN` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EXS-RECON` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EVENT-CORE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EVENT-OUTBOX` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EVENT-BROKER` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `EVENT-DLQ` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SAGA-CORE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SAGA-COMP` | 미구현 | 현재 검수에서 명확한 잔여 Gap 확인. |
| `SAGA-MANUAL` | 미구현 | 현재 검수에서 명확한 잔여 Gap 확인. |
| `BAT-CORE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BAT-JOB` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BAT-ITEM` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BAT-EXECUTOR` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BAT-AGENT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BAT-CALL-SYNC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BAT-CALL-ASYNC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BAT-SHARED` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CENTER-CORE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CENTER-RUNNER` | 미구현 | 독립 CenterCutRunner 제품 Runtime을 확인하지 못함. |
| `CENTER-PARAM` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CENTER-CLAIM` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CENTER-RATE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CENTER-REPROCESS` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CENTER-UNKNOWN` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `CENTER-OPS` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-AUTH` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-RBAC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-AUDIT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-TX` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-TIMELINE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-SERVICE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-LOG` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-BATCH` | 실패 | ADM Batch Service가 batDB 직접 UPDATE/DELETE/INSERT. Owner Command API 필요. |
| `ADM-CENTER` | 실패 | ADM Center-Cut 관제도 Owner DB 직접 접근. Query/Command boundary 필요. |
| `ADM-AGENT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-EXS` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-RECOVERY` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-INCIDENT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-UX` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ADM-APPROVAL` | 미구현 | 22b baseline에는 위험조치 Approval Runtime/Table/API/UI가 없었다. Overlay는 DDL/API/SPI baseline만 추가했으며 실제 Engine/Owner Command/Unknown Recovery는 미구현. |
| `BZA-BUSINESS` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `BZA-ORG` | 부분 구현 | 기존 조직/직원 모델은 단일 조직·단일 Role 중심이었다. Overlay가 Position/JobTitle/Assignment/multi-role DDL/SPI를 보강했으나 Service/API/UI/유효기간 Runtime은 미완. |
| `BZA-APPROVAL` | 부분 구현 | 테이블 골격은 있으나 decision_rule/approval_mode/delegation을 실제 Engine이 충분히 사용하지 않음. |
| `BZA-SEQUENCE-SAMPLE` | 미구현 | 현재 검수에서 명확한 잔여 Gap 확인. |
| `SEC-AUTHN` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-AUTHZ` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-SECRET` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-CERT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-PRIVACY` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-DOWNLOAD` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-APP` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-APPROVAL` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SEC-AUDIT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-METRIC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-SLO` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-ALERT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-INCIDENT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-RUNBOOK` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-SELF` | 재확인 필요 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-TOPOLOGY` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-MAINT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-CONFIG` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-DRIFT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-CAPACITY` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `OPS-DR` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DEVEX-QUICK` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DEVEX-CODEGEN` | 부분 구현 | Metadata + central domain-template 방향은 양호하나 full lifecycle Runtime 미검증. |
| `DEVEX-COMMENT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `ONBOARD-DOMAIN` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SAMPLE-ACC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SAMPLE-MBR` | 실패 | mbr_sample_item DDL과 기존 Member/Auth Mapper의 mbr_member 계열 계약 충돌. |
| `SAMPLE-REF` | 부분 구현 | ref_sample_item + Center-Cut EDU consumer가 있어 방향 양호, 최신 Runtime 필요. |
| `SAMPLE-BIZADM` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `SAMPLE-EDU` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `API-CONTRACT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `API-PAGING` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `API-ASYNC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `API-FILE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `RULE-ARCH` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `RULE-SEC` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `RULE-QUALITY` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `TEST-UNIT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `TEST-CONTRACT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `TEST-RUNTIME` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `TEST-BROWSER` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `TEST-BROKER` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `TEST-FAULT` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `TEST-EVIDENCE` | 실패 | Evidence Index와 실제 경로 불일치, stale 20260722_01 재유입. |
| `REL-BUILD` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `REL-DEPLOY` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `REL-MIG` | 실패 | V6/V29 integrity와 upgrade/rollback Runtime 미완. |
| `REL-COMPAT` | 미검증 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `DOC-GOV` | 실패 | Root/Guide/Evidence 상태가 Source와 어긋난 항목 존재. Overlay가 일부 정본화. |
| `DOC-PRODUCT` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `PROD-EDITION` | 재확인 필요 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `PROD-MULTITENANT` | 미구현 | 현재 검수에서 명확한 잔여 Gap 확인. |
| `PROD-PLUGIN` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `PROD-PACKAGE` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `REQ-GOV` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `REQ-REVIEW` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `REQ-CODEX` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |
| `REQ-GAP` | 부분 구현 | 최신 Source/API/SQL/Test/Runtime/Evidence를 Requirement 단위로 다시 닫아야 함. |

## 7. 다음 검수의 완료 조건

다음 Push에서는 단순 변경 파일 수가 아니라 162 Requirement 각각을 Source/API/SQL/Test/Runtime/Evidence/Guide에 재연결한다. 특히 P0가 닫히기 전에는 신규 기능 수를 늘려 완료율을 높이지 않는다. 실제 실행하지 않은 Vendor/DB/Browser/Multi-instance는 미검증으로 유지한다.
