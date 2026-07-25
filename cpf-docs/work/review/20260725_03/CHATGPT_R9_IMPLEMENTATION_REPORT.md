# CPF R9 ChatGPT 구현·검수 리포트

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 기준 Commit: `f1d85cf087e2a16038b21f6c53ac29204d164124` (`20250725_03`, 실제 2026-07-25)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 사용자 정책: 개발단계에서는 잘못된 Legacy/호환 잔재를 보존하지 않는다. 대안 Runtime과 migration 경로를 구현한 후 Consumer/Test/Config/Doc까지 옮기고 물리 삭제한다.
- 검증 정책: 구현을 더 누적한 뒤 DB/Gradle/npm/Runtime/Browser/Generator/Multi-instance/Evidence를 한 번에 Full Verification 한다. 이 문서의 실행 미수행 항목은 완료로 판정하지 않는다.

## 1~23 전수검수와 R9 조치

| # | 확인 내용 | 발견한 미흡/위험 | R9 실제 조치 | 현재 판정 |
|---|---|---|---|---|
| 1 | 문서 정본성 | R8 이후 Source 변경과 Current Request/Review 상태가 다시 어긋날 수 있음 | Final Target을 우선 정본으로 유지하고 R9 Requirement 162개 review/implementation/handover를 공식 work 위치에 작성 | 부분 구현 |
| 2 | 제품 정의 | 일부 기능은 계약/DDL만 있고 실제 Consumer가 약함 | Incident/Maintenance/Break-glass/BZA Approval lifecycle/Lineage hook/Self-healing orchestrator 등 실제 Consumer를 추가 | 부분 구현 |
| 3 | 지원 Topology | Center-Cut Generated Domain 분리 WAS generic 호출이 약함 | BAT `BatRemoteCenterCutHandler` + transport SPI를 ServiceCallEngine 위에 추가. 내부 호출은 Gateway 재경유 금지 | 부분 구현 |
| 4 | Module/SystemCode | 고정 EXS residue, ADM의 업무 Domain 침범, Legacy package 잔존 위험 | EXS Generated Domain 정책 유지. ADM Member 직접 CRUD 제거 유지. Core Batch Runtime 삭제 Gate 강화 | 부분 구현 |
| 5 | Architecture/Dependency | `cpf-core/common/batch`에 Runtime이 남아 BAT Ownership을 침범 | Batch 계약만 `core.api.batch`, Center-Cut 계약 `core.api/spi.centercut`; Runtime/Test는 BAT로 이동 후 Core legacy 물리 삭제 | 부분 구현 |
| 6 | cpf-core | Runtime과 계약 혼재, multitenant/lineage/self-healing/product capability 공통 경계 부족 | Core를 작은 API/SPI/primitive 중심으로 재정비. Tenant/Lineage/SelfHealing/Capability/DB routing primitive 추가 | 부분 구현 |
| 7 | cpf-common/cmnDB | `CMN-CALENDAR`, `CMN-TEMPLATE` Source 공백 | DB 테이블 추정 없이 DB-less Calendar/Template API/SPI와 fail-closed renderer/test 추가 | 부분 구현 |
| 8 | DB/SQL/Migration | MariaDB canonical source가 구/신 경로로 분할, V6 고정 EXS/BIZADM residue, V29 BZA baseline stale | old source를 vendor source에 missing-only merge 후 삭제. V6 pre-GA 정본 재작성/rename, V29 현재 BZA baseline 재생성, V42~V44 추가 | 미검증 |
| 9 | Fixed/File/External/Messaging | 이번 R9 직접 변경은 적으나 Lineage Consumer 공백 확인 | ServiceCall 실제 lineage hook부터 연결. File/Broker/Batch lineage hook은 잔여 Gap으로 유지 | 부분 구현 |
| 10 | 업무 채번 | R8 BZA 선택형 Sequence sample은 존재하나 Runtime 검증 미수행 | 기본 OFF 정책 유지. Full Verify 대상 | 부분 구현 |
| 11 | Gateway/ServiceCall | Registry 상태 변경용 command boundary와 drained instance 선택 방지 부족 | `CpfServiceRegistryControlPort`/Facade 추가, health-aware selector가 inactive/down/drained fallback을 선택하지 않도록 보강 | 부분 구현 |
| 12 | Batch/Center-Cut/Agent | Core Runtime 중복, generic remote handler 부재 | BAT Runtime을 유일 Owner로 정리, `BatBatchJobLogPath` 누락 교정, remote Center-Cut adapter 추가 | 부분 구현 |
| 13 | ADM/BZA | Incident/Maintenance/Break-glass 및 BZA lifecycle 일부 공백 | ADM control plane + scoped Break-glass, BZA withdraw/cancel/resubmit/expire 구현 | 부분 구현 |
| 14 | Frontend | 기능 디렉터리는 늘었지만 coarse residue/raw JSON/prompt 중심 UX와 운영 화면 공백 | ADM Dashboard/Topology/Recovery/Incident/Maintenance/Worker/Capacity, BZA Org Tree/Approval Board/Submission lifecycle UI, CPF local responsive design system 추가 | 부분 구현 |
| 15 | API | Owner command/lifecycle API 부족 | ADM control plane/Break-glass API, BZA approval lifecycle API, Center-Cut remote transport boundary 추가 | 부분 구현 |
| 16 | Security/Audit | Break-glass를 무제한 bypass로 만들 위험, BZA lifecycle 감사 연속성 필요 | scope/TTL/reason/close/post-review 세션을 fail-closed로 구현. 자동 위험조치 bypass는 의도적으로 미연결. resubmit은 새 Snapshot/새 idempotency document | 부분 구현 |
| 17 | Generator/DevEx | EXS Generated Domain lifecycle Runtime 검증은 아직 미수행 | fixed EXS 정책 재발 금지 유지. R9가 old V6 EXS DDL까지 제거. Final Generator lifecycle 검증 대상 | 미검증 |
| 18 | Reliability/Observability | Self-healing은 Guard만, Lineage는 Recorder만 있어 실제 Consumer 부족 | Self-healing Orchestrator/Owner Action Port/Event Sink와 ServiceCall Lineage SUCCESS/FAILED/UNKNOWN hook 추가 | 부분 구현 |
| 19 | 설치/배포/호환성 | pre-GA migration source 자체가 잘못된 구간 존재 | 잘못된 V6/V29를 보존하지 않고 current canonical 기준 재작성. 실제 fresh/upgrade/rollback은 최종 Full Verify로 미검증 | 미검증 |
| 20 | Repository/문서 | Root compose 중복, R7/R8 coarse UI/console residue | Root compose 삭제 Gate, BZA console/coarse page와 ADM coarse panel 삭제, old DB source 삭제 Gate | 부분 구현 |
| 21 | Evidence/완료판정 | 구현량은 늘었으나 실제 사용자 환경 Evidence는 아직 없음 | R8 Full Verify Runner/Evidence 보존 방식을 유지하고 R9까지 누적 후 한 번에 실행 | 미검증 |
| 22 | Requirement Catalog | 일부 과거 실패/미구현이 문서에서 상태만 바뀔 위험 | Canonical ID 162개를 정확히 1회 유지하고 R9 개발 내용을 개별 행에 재판정. 완료 상태는 부여하지 않음 | 부분 구현 |
| 23 | 최종 제품화 Gate | 아직 DB/Build/Browser/Multi-instance가 실행되지 않음 | R9 Static Gate를 강화하고 최종 `verify-full-product.ps1 -RequireAll` 실행 전 완료 금지 | 미검증 |

## R9에서 새로 실제 구현한 주요 기능

### Core/BAT Ownership 정리
- `cpf-core/common/batch` Runtime을 제품 정본에서 제거한다.
- topology-independent Batch contract는 `com.cpf.core.api.batch`로 이동한다.
- Center-Cut model은 `com.cpf.core.api.centercut`, 확장 계약은 `com.cpf.core.spi.centercut`으로 이동한다.
- Launcher/Heartbeat/Ghost/Lock/FileLog/OperationRepository/RuntimeListener/CenterCutService는 `cpf-batch`만 소유한다.
- Core의 Batch/Center-Cut AutoConfiguration은 삭제하고 AutoConfiguration imports에서도 제거한다.
- 기존 Core Runtime unit test도 BAT test ownership으로 이동한다.

### ADM Control Plane / UX
- Incident lifecycle: `OPEN → ACKNOWLEDGED → MITIGATED → RESOLVED/CLOSED`, optimistic version.
- Maintenance: Registry Owner Port를 통한 `DRAIN / DISABLE / RESUME`.
- Break-glass: scope, TTL, 사유, 종료, 사후검토. 기본 자동 bypass 금지.
- Dashboard/Topology/Recovery Center/Incident/Maintenance/Worker/Capacity/Break-glass 전용 lazy feature.
- 서비스 레지스트리 raw JSON 중심 화면을 상태 card/table/control 중심 UI로 개편.
- 로컬 SVG icon + CPF Design System; 외부 runtime CDN/font/CSS 의존 금지.

### BZA 업무관리 / Approval
- 조직 단순 CRUD 화면을 조직 Tree/상세 구조로 개선.
- Approval Inbox를 상태 board + 상세 dialog + step timeline으로 개선.
- `WITHDRAW`, `CANCEL`, `RESUBMIT`, `EXPIRE` lifecycle 추가.
- 재상신은 기존 approval을 재활성화하지 않고 새 Snapshot/새 idempotency 문서를 만들며 `resubmitted_from_approval_id`로 원본 연결.
- 상신함 UI에서 lifecycle command와 재상신 흐름을 제공.

### 운영형 공통 기능 Gap 보강
- CMN Calendar: DB-less 기본 weekend policy + 고객 Calendar SPI.
- CMN Template: versioned provider + 변수 allowlist + missing-variable fail-closed renderer.
- Multi-tenant: opt-in tenant model/resolver/access-policy/context 기반.
- DB Multi: read/write intent + routing datasource primitive.
- Data Lineage: 표준 record/recorder + ServiceCallEngine 실제 SUCCESS/FAILED/UNKNOWN hook.
- Self-healing: bounded Guard + allowlist + approval-required + Owner Action Port + Event Sink Orchestrator.
- Product Edition: Capability Registry/License Provider SPI로 Runtime edition 문자열 분기 방지.
- DB Performance: schema/index baseline + 최종 runtime EXPLAIN evidence Gate.

## 직접 확인한 Static 결과

- R9 Requirement Catalog: 162개 / 중복 0 유지.
- R9 Java overlay 50개 기본 구조 오류 0.
- 변경된 ADM/BZA TypeScript/Vue script 24개 parser 오류 0.
- Frontend 외부 Runtime URL 0.
- Self-healing/Tenant/Lineage/Capability 순수 Java subset은 로컬 `javac` PASS.
- 외부 runtime URL을 추가하지 않는 구조 유지.
- PowerShell APPLY, Gradle 전체 Build, npm test/build, MariaDB, Browser, Runtime, Multi-instance는 이 실행환경에서 최종 실행하지 않음.

## 완료로 올리지 않는 이유

R9의 목적은 Gap을 실제 Source로 줄이고 잘못된 구조를 정리하는 것이다. 사용자의 요청대로 최종 실행 검증은 구현을 더 누적한 뒤 한 번에 수행한다. 따라서 Source가 추가되었어도 Runtime/Evidence가 없는 Requirement는 `완료`로 기록하지 않는다.
