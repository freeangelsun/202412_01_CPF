# CPF R8 Canonical Requirement Review — 2026-07-25

- Baseline: `512e5f2c7f32ba21ef6be570b2efa3dbcbd7a482`
- Canonical count: **162** (Legacy Alias 8개 제외)
- 원칙: 이번 환경에서 실제 Gradle/DB/Browser 실행을 하지 않은 항목은 완료로 승격하지 않는다.

| Requirement | R8 이전 | R8 확인/개발 | R8 후 | 검증 |
|---|---|---|---|---|
| `ADM-APPROVAL` | 미구현 | ADM policy/participant snapshot/ALL·ANY·N_OF_M/idempotency/owner-command execution engine/API/UI 추가. Break-glass는 미완료. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `BZA-ORG` | 부분 구현 | 직급/직책/다중소속/조직책임/다중 Role API·UI 연결. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `CPF-ROLE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-OPSDB` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-LOGFAIL` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-SCHED` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-CODE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-MSG` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-CALENDAR` | 재확인 필요 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 재확인 필요 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-TEMPLATE` | 재확인 필요 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 재확인 필요 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-SERVICE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-LOG` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-INCIDENT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-UX` | 부분 구현 | 24개 ADM 메뉴를 각각 독립 lazy feature directory로 분리하고 operators 회귀 복구, MEMBER legacy 메뉴 제거. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `SEC-APP` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-SELF` | 재확인 필요 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 재확인 필요 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-TOPOLOGY` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-MAINT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-SQL` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-PERF` | 재확인 필요 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 재확인 필요 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-MULTI` | 재확인 필요 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 재확인 필요 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DATA-LINEAGE` | 재확인 필요 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 재확인 필요 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DATA-RETENTION` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `API-LIMIT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DEVEX-COMMENT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `RULE-ARCH` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `RULE-SEC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `RULE-QUALITY` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `PROD-EDITION` | 재확인 필요 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 재확인 필요 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `PROD-MULTITENANT` | 미구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `PROD-PLUGIN` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `PROD-PACKAGE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `REQ-GAP` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-CALL-SYNC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-CALL-ASYNC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-SHARED` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-APPROVAL` | 부분 구현 | ADM/BZA 승인 엔진에 self-approval 방지·decision idempotency·snapshot 기본 통제 추가; break-glass/expiry는 잔여. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `BZA-APPROVAL` | 부분 구현 | versioned policy, dynamic target, delegation, participant snapshot, simulation, ALL·ANY·N_OF_M, optimistic lock 구현. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `ARCH-MISSION` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ARCH-MSA` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ARCH-BOUNDARY` | 실패 | ADM→BAT Batch/Center-Cut 직접 DB 접근과 ADM→MBR 업무 CRUD를 제거하고 Owner Port로 교정. BAT Runtime 소비를 cpf-batch로 이동. | 부분 구현 | R8 Source boundary gate 준비; Core legacy Batch compatibility class 물리 제거와 전체 compile 필요 |
| `ARCH-LAYER` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CORE-API` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CORE-SPI` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CORE-CONFIG` | 부분 구현 | Core Batch/Center-Cut legacy auto-configuration을 명시 opt-in(default OFF)으로 변경. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `CORE-TESTKIT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-CALL` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-REGISTRY` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-ROUTING` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-HEALTH` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-HEADER` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-CONTEXT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-TXID` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-ERROR` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-VALID` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-IDEMP` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-STATE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-LOCK` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-RESILIENCE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-DEADLINE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-LOGDB` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-FILELOG` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-TRACE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-MASK` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CORE-FIXED` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CORE-FILE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CORE-MESSAGE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-EXTENSION` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-SAMPLE-DB` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-OWNERSHIP` | 실패 | ADM batDB/refDB/mbrDB 직접 소유 침범을 제거·축소하고 BAT/REF Owner Port·Extension 경계 추가. | 부분 구현 | Static boundary gate + 사용자 Local/Remote runtime Evidence 필요 |
| `DB-INSTALL` | 실패 | MariaDB canonical source를 vendor/mariadb/source 정본 경로로 이동하고 R8 sync gate에 통합. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `DB-MIGRATION` | 실패 | V40 Saga/V41 ADM ownership migration과 V39~V41 checksum ledger를 보강했으나 historical V6/V29 integrity 원인은 임의 수정하지 않음. | 실패 | 신규 migration checksum 정적 일치 PASS; historical migration 원인/실행 Evidence 재확인 필요 |
| `DB-ROLLBACK` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-BACKUP` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-MULTI-VENDOR` | 부분 구현 | Vendor source ownership 계약은 동일화했으나 non-MariaDB platform pack은 여전히 명시 not-implemented. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `GWY-ENTRY` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `GWY-ROUTING` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `GWY-TRUST` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `GWY-RESILIENCE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EXS-INST` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EXS-REST` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EXS-FIXED` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EXS-SEC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EXS-FILE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EXS-UNKNOWN` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EXS-RECON` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EVENT-CORE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EVENT-OUTBOX` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EVENT-BROKER` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `EVENT-DLQ` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SAGA-CORE` | 부분 구현 | durable Saga definition/state runtime 추가. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `SAGA-COMP` | 미구현 | 정방향 실패 시 완료 Step 역순 compensation, durable step 상태/attempt, compensation 실패 보존 구현. JDBC attempt counter는 RUNNING/COMPENSATING 진입 때만 증가하도록 보정. | 부분 구현 | Pure Saga `javac` PASS; Spring/JDBC integration/fault Evidence 필요 |
| `SAGA-MANUAL` | 미구현 | 운영자/사유 필수 수동 compensation 재시도·수동 확정·감사 구현. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `BAT-CORE` | 부분 구현 | Batch 실행 Runtime(launcher/heartbeat/ghost/lock/log/listener)을 cpf-batch owner로 이전. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `BAT-JOB` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-ITEM` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-EXECUTOR` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-AGENT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-CORE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-RUNNER` | 미구현 | BAT CenterCut registry/runner/stop/rate/last-run operational runtime 추가. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `CENTER-PARAM` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-CLAIM` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-RATE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-REPROCESS` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-UNKNOWN` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-OPS` | 부분 구현 | BAT internal center-cut operations API와 ADM owner-port 연동 추가. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `ADM-AUTH` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-RBAC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-AUDIT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-TX` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-TIMELINE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-BATCH` | 실패 | ADM 직접 batDB mutation 제거, CpfBatchOperationsPort + Local/Remote owner contract로 전환. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `ADM-CENTER` | 실패 | ADM direct batDB/refDB 조회를 제거하고 BAT Owner Port + ADM Remote Adapter + REF-owned Extension SPI로 전환. | 부분 구현 | 동일 JVM/분리 WAS 표준 BAT 경계 Source 구현; REF generic remote extension routing/runtime Evidence 필요 |
| `ADM-AGENT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-EXS` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-RECOVERY` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BZA-BUSINESS` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BZA-SEQUENCE-SAMPLE` | 미구현 | 기본 OFF 선택형 업무 채번 sample(source/optional SQL/UI) 구현. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `SEC-AUTHN` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-AUTHZ` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-SECRET` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-CERT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-PRIVACY` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-DOWNLOAD` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-AUDIT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-METRIC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-SLO` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-ALERT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-INCIDENT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-RUNBOOK` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-CONFIG` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-DRIFT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-CAPACITY` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-DR` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DEVEX-QUICK` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DEVEX-CODEGEN` | 부분 구현 | R8 APPLY가 fixed EXS를 제거하고 Golden Generator로 external/EXS를 생성·검증. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `ONBOARD-DOMAIN` | 부분 구현 | EXS를 예외가 아닌 동일 Generated Domain lifecycle로 검증. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `SAMPLE-ACC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SAMPLE-MBR` | 실패 | ADM의 legacy mbr_member 직접 CRUD 제거; MBR Golden Reference 정책과 충돌 완화. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `SAMPLE-REF` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SAMPLE-BIZADM` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SAMPLE-EDU` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `API-CONTRACT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `API-PAGING` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `API-ASYNC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `API-FILE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `TEST-UNIT` | 부분 구현 | Saga compensation/manual recovery unit test 추가. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `TEST-CONTRACT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `TEST-RUNTIME` | 미검증 | 통합 verification runner 추가했으나 실제 사용자 환경 실행 전이므로 미검증 유지. | 미검증 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `TEST-BROWSER` | 미검증 | ADM/BZA browser smoke를 통합 runner option으로 연결했으나 실행 전. | 미검증 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `TEST-BROKER` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `TEST-FAULT` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `TEST-EVIDENCE` | 실패 | Full verifier가 각 명령 출력, 시작·종료, 결과, Profile을 수집하고 기존 sanitizer로 commit SHA 포함 Evidence를 자동 저장하도록 보강. | 미검증 | Runner Source/정적 검수 완료; 사용자 환경 `-RequireAll` 실제 Evidence 필요 |
| `REL-BUILD` | 미검증 | Gradle clean test assemble + ADM/BZA npm test/build + post-run hygiene를 단일 verification runner에 연결. | 부분 구현 | Runner 준비 완료; 사용자 Java25/Node 환경 실제 실행 필요 |
| `REL-DEPLOY` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `REL-MIG` | 실패 | V40/V41 신규 migration/checksum 추가. Historical V6/V29 문제는 별도 재확인 필요. | 재확인 필요 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `REL-COMPAT` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DOC-GOV` | 실패 | R8 162 requirement matrix/implementation/handover를 정식 work/review 위치에 생성. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `DOC-PRODUCT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `REQ-GOV` | 부분 구현 | 162 canonical IDs를 alias 제외 정확히 1회 재검수 matrix로 생성. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `REQ-REVIEW` | 부분 구현 | R8 1~23 검수·변경 보고서 생성. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `REQ-CODEX` | 부분 구현 | 회사/집/Codex 교차 인수인계 문서와 verification 명령 고정. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
