# CPF R9 전체 Requirement / Gap Review

- 기준 master: `f1d85cf087e2a16038b21f6c53ac29204d164124` (`20250725_03`, 2026-07-25)
- 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 방식: R8의 162개 Requirement를 누락 없이 재사용하고 R9 실제 구현 변경을 요구사항별로 재판정
- 주의: Runtime/DB/Browser/다중인스턴스 Full Verification은 사용자 요청에 따라 최종 누적 작업 후 한 번에 수행한다. 실행하지 않은 항목을 완료로 올리지 않는다.

## R9에서 실제로 확인한 문제와 조치

1. **Core Batch/Center-Cut 소유권**: R8이 BAT 대체 Runtime을 만들고도 Core legacy Runtime을 남겼다. R9 APPLY에서 계약은 `core.api/spi`, Runtime은 BAT로 완전 분리하고 Core Runtime/AutoConfiguration/Runtime test를 물리 제거한다.
2. **ADM 제품 UX**: 메뉴는 분리됐지만 Dashboard/Topology/Incident/Maintenance/Recovery/Worker/Capacity 화면이 빠졌고 Service Registry는 raw JSON 중심이었다. 7개 전용 기능과 responsive CPF Design System을 추가한다.
3. **BZA 제품 UX**: 조직은 단순 CRUD Table, 결재 Inbox는 `prompt()` 기반이었다. 조직 Tree/상세, 결재 board/dialog/step UI, Dashboard를 강화한다.
4. **Frontend garbage**: R7 coarse panel과 BZA `console.ts`가 master에 남아 있었다. R9 APPLY가 강제 제거한다.
5. **ADM owner command**: Maintenance가 없었다. Core Service Registry Control Port를 추가하여 ADM이 cpfDB를 직접 수정하지 않고 DRAIN/DISABLE/RESUME을 요청한다.
6. **Incident lifecycle**: 명시 Requirement는 있었지만 Source가 없었다. admDB lifecycle + optimistic version + Audit + API/UI를 추가한다.
7. **CMN-CALENDAR/TEMPLATE**: Source가 없었다. cmnDB에 추정 테이블을 추가하지 않고 DB-less Extension API/SPI로 구현한다.
8. **PROD-MULTITENANT**: Source 0건이었다. opt-in Tenant Context/Resolver/Strict Access Policy 기반을 추가하되 실제 격리 wiring 전까지 부분 구현이다.
9. **DB-MULTI**: 기존 단일 DataSource resolver만 존재했다. Read/Write intent + routing datasource primitive를 추가한다.
10. **DATA-LINEAGE**: Source가 없었다. 표준 Lineage record/recorder 계약과 bounded implementation을 추가한다.
11. **OPS-SELF**: 자동 복구 무한루프 방지 기반이 없었다. max action/window, cooldown, consecutive failure limit Guard를 추가한다.
12. **Repository Root**: Root compose는 `deploy/local`과 SHA 동일 중복이었다. 사용자 로컬 삭제 + R9 Gate로 재발 방지한다.

## 아직 반드시 남는 상용화 Gap

### P0 — 다음 구현에서 계속 닫아야 함
- ADM Approval: scoped Break-glass TTL/종료/사후검토 기반은 R9에서 추가. 실제 위험조치별 scope 소비, escalation, Owner Command 완전 매핑은 잔여.
- BZA Approval: withdraw/cancel/resubmit/expiry는 R9 구현. escalation 정책, 조직개편·휴직·퇴직·위임 경계 자동시험, 상세 Timeline은 잔여.
- Center-Cut: R9에서 ServiceCallEngine 기반 Generated Domain generic remote Handler/Transport를 추가. 실제 HTTP/메시징 transport 구현과 다중 인스턴스 Runner/Unknown 복구 Evidence는 잔여.
- Multi-tenant: HTTP/JWT/Header tenant resolver, async context propagation, tenant별 DB/row/schema isolation, audit/masking, Generator 표준.
- Self-healing: R9에서 allowlist/approval/cooldown/window/failure-cutoff Orchestrator와 Owner Action/Event Port까지 추가. 실제 Health event consumer와 ADM 승인/감사 Adapter Runtime 연결은 잔여.
- Data Lineage: R9에서 ServiceCallEngine 성공/실패/UNKNOWN consumer hook을 추가. Batch/File/Broker hook, 영속/조회/보존 정책과 ADM 화면은 잔여.
- DB Multi/Perf: Primary/Replica topology, lag gate, read-after-write, failover, explain/index baseline, slow-query regression gate.
- DB Migration: V6 고정 EXS/BIZADM residue 제거·rename, V29 현재 BZA baseline 재생성까지 R9 Source 교정. 실제 fresh/upgrade/rollback 실행 전이므로 `미검증`.

### P1 — 제품화/검증
- 다른 DB Vendor(PostgreSQL/Oracle/MSSQL 등)의 실제 platform DDL/migration parity. 미지원 Vendor는 명시 fail-closed 유지.
- SLO/Capacity 장기 Metrics, percentile/forecast, alert routing.
- Incident 자동 생성(Alert 연계), Postmortem/Runbook link, 변경/배포 상관관계.
- Backup/Restore/DR 실검증, Upgrade/Rollback/Compatibility Matrix.
- Security: MFA/OIDC/JWT/API Key/mTLS/Secret rotation/Download-unmask dual control 실제 환경 검증.
- EDU/OpenAPI/JavaDoc/Generator create→run→remove→regenerate parity.
- ADM/BZA Browser E2E, keyboard/accessibility, responsive viewport, CSP/secure-header 확인.
- Full Gradle, npm verify, MariaDB all-install, Runtime E2E, multi-instance/fault/broker test와 Evidence 보존.

## 상태 집계

- 미검증: **13**
- 부분 구현: **149**

## 162개 Requirement 전수표

| Requirement | R8 이전 상태 | R9 확인/개발 | R9 상태 | 근거/잔여 |
|---|---|---|---|---|
| `ADM-APPROVAL` | 미구현 | R8 승인 Engine에 R9 scoped Break-glass TTL/종료/사후검토 API/UI/DB를 추가. 실제 위험조치별 scope 소비와 escalation은 잔여. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `BZA-ORG` | 부분 구현 | 직급/직책/다중소속/조직책임/다중 Role API·UI 연결. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `CPF-ROLE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-OPSDB` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-LOGFAIL` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CPF-SCHED` | 부분 구현 | Core legacy runtime 제거 정책 강화. BAT owner runtime은 유지하며 Full build/runtime verification 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `CMN-CODE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-MSG` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CMN-CALENDAR` | 재확인 필요 | DB-less Calendar API/기본 Weekend 정책/고객 Calendar resolver와 단위테스트 추가. 기관 holiday provider/runtime evidence는 추가 필요. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `CMN-TEMPLATE` | 재확인 필요 | Versioned Template Provider SPI, 변수 allowlist/fail-closed renderer/service와 단위테스트 추가. 고객 저장소/Channel adapter runtime evidence는 추가 필요. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `ADM-SERVICE` | 부분 구현 | Service Registry raw JSON 화면을 Instance/Health/Routing/Circuit product UI로 개선하고 maintenance command port 추가. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `ADM-LOG` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ADM-INCIDENT` | 부분 구현 | adm_incident lifecycle DB/Service/API/UI 추가. OPEN→ACKNOWLEDGED→MITIGATED→RESOLVED/CLOSED 전이와 optimistic version 적용. Alert 자동연결/사후분석은 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `ADM-UX` | 부분 구현 | ADM Dashboard/Topology/Recovery/Incident/Maintenance/Worker/Capacity 전용 lazy feature와 CPF responsive design system 추가. Browser E2E 전까지 완료 금지. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `SEC-APP` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `OPS-SELF` | 재확인 필요 | bounded guard에 allowlist/approval-required/Owner Action Port/Event Sink Orchestrator까지 추가. 실제 Health event source와 ADM 승인/감사 Adapter 연결은 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `OPS-TOPOLOGY` | 부분 구현 | Service Registry 실데이터 기반 topology UI 추가. service dependency graph/실시간 stream은 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `OPS-MAINT` | 부분 구현 | Service Registry Control Port와 DRAIN/DISABLE/RESUME, ADM command audit/UI 추가. Connection drain grace/long-running request coordination은 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `DB-SQL` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-PERF` | 재확인 필요 | R9 schema-manifest 기반 index baseline Gate와 최종 Runtime EXPLAIN/slow-query evidence 요구를 추가. 실제 DB plan/slow-query 실행 전까지 부분 구현. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-MULTI` | 재확인 필요 | Read/Write intent context와 Spring routing datasource primitive 추가. 실제 Primary/Replica datasource 구성·failover·lag/read-after-write runtime evidence는 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `DATA-LINEAGE` | 재확인 필요 | Lineage API/bounded recorder에 ServiceCallEngine SUCCESS/FAILED/UNKNOWN 실제 hook을 연결. Batch/File/Broker hook 및 영속 backend/ADM 조회는 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `DATA-RETENTION` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `API-LIMIT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DEVEX-COMMENT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `RULE-ARCH` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `RULE-SEC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `RULE-QUALITY` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `PROD-EDITION` | 재확인 필요 | Edition 문자열 분기를 Runtime에 퍼뜨리지 않는 Capability enum/Registry/LicenseProvider SPI를 추가. 실제 상용 License Provider/Package 조합은 잔여. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `PROD-MULTITENANT` | 미구현 | Opt-in tenant model/resolver/access policy/context 기반 추가. HTTP/JWT resolution, async propagation, DB isolation, migration, ADM tenant ops는 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `PROD-PLUGIN` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `PROD-PACKAGE` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `REQ-GAP` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-CALL-SYNC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-CALL-ASYNC` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `BAT-SHARED` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `SEC-APPROVAL` | 부분 구현 | R9 BZA 철회/취소/재상신/만료와 ADM scoped Break-glass를 추가. escalation 및 break-glass 실제 Owner Command scope 소비는 잔여. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `BZA-APPROVAL` | 부분 구현 | 기존 정책/Target/Snapshot Engine에 R9 withdraw/cancel/resubmit/expire API와 상신 Lifecycle UI, 원본 결재 linkage를 추가. escalation/경계 시나리오 E2E는 잔여. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `ARCH-MISSION` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ARCH-MSA` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `ARCH-BOUNDARY` | 실패 | Core Batch Runtime 물리삭제/계약 API·SPI 이동을 APPLY에서 강제하고 Service Registry command owner port 추가. Full compile로 잔존 consumer 확인 필요. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
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
| `CMN-EXTENSION` | 부분 구현 | Calendar/Template를 DB-less customer extension으로 추가하여 cmnDB 최소 1-table 정책을 보존. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `CMN-SAMPLE-DB` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DB-OWNERSHIP` | 실패 | ADM batDB/refDB/mbrDB 직접 소유 침범을 제거·축소하고 BAT/REF Owner Port·Extension 경계 추가. | 부분 구현 | Static boundary gate + 사용자 Local/Remote runtime Evidence 필요 |
| `DB-INSTALL` | 실패 | MariaDB canonical source를 vendor/mariadb/source 정본 경로로 이동하고 R8 sync gate에 통합. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `DB-MIGRATION` | 실패 | R9에서 pre-GA V6/V29 정본을 현재 Generated EXS/BZA 정책으로 교정. Fresh install/upgrade/rollback 실제 실행이 아직 없어 미검증으로 전환. | 미검증 | Source 교정 완료; Full DB lifecycle Evidence 필요 |
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
| `CENTER-CORE` | 부분 구현 | Center-Cut 계약은 Core api/spi로 이동하고 실행 Service/Runner/remote adapter는 BAT 소유로 정리. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `CENTER-RUNNER` | 미구현 | BAT registry/runner/stop/rate/last-run에 R9 ServiceCallEngine 기반 Generated Domain remote handler/transport 경계 추가. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
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
| `ADM-BATCH` | 실패 | Worker 전용 UI와 BAT owner 경계를 강화. Full runtime/multi-instance 검증 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
| `ADM-CENTER` | 실패 | R8 owner port를 유지하고 Core legacy CenterCut runtime 물리삭제. Generated/REF remote routing은 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
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
| `OPS-CAPACITY` | 미검증 | call-history/instance 기반 Capacity/SLO 기본 UI 추가. 장기 metric store, percentile, forecast/alert evidence는 잔여. | 부분 구현 | R9 Source/UI/Static Gate 추가; 사용자 환경 Full Verify 필요 |
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
| `REL-MIG` | 실패 | R9 pre-GA canonical repair로 V6 고정 EXS/BIZADM DDL 제거·rename, V29를 현재 BZA canonical baseline으로 재생성하고 V42~V44를 추가. 실제 upgrade/rollback 실행 전. | 미검증 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `REL-COMPAT` | 미검증 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 미검증 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `DOC-GOV` | 실패 | R8 162 requirement matrix/implementation/handover를 정식 work/review 위치에 생성. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `DOC-PRODUCT` | 부분 구현 | 최신 Final Target과 2026-07-24 전수검수 판정을 재대조. 이번 R8에서 직접 구현 변경 없음. | 부분 구현 | Source/구조 정적 검수; Runtime/Evidence 재검증 필요 |
| `REQ-GOV` | 부분 구현 | 162 canonical IDs를 alias 제외 정확히 1회 재검수 matrix로 생성. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `REQ-REVIEW` | 부분 구현 | R8 1~23 검수·변경 보고서 생성. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
| `REQ-CODEX` | 부분 구현 | 회사/집/Codex 교차 인수인계 문서와 verification 명령 고정. | 부분 구현 | R8 Source/SQL/UI 변경 포함; 사용자 환경 Full Verify 필요 |
