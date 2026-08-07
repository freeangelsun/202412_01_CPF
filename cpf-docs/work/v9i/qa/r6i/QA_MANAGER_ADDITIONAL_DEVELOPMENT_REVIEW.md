# CPF 통합 QA 관리자 추가 개발 강화·진행상태 리뷰

- 기준 Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 확인 기준 SHA: `77db10ad9aff44ee422795080fb2e96b364c9d65` (`08_01`)
- 작성 시각(KST): `2026-08-07T11:46:00+09:00`
- 성격: **QA A·B 확정 Finding과 별개인 통합 QA 관리자의 추가 개발 강화 판단**

## 1. 현재 프로젝트 진행 상태

### 정량 상태

- R6S12 개발 Commit 변경: `118 files`, `+7,663 / -488`
- 개발GPT 자체 원장: 기존 R5I 29건 중 `완료 25 / 부분 구현 3 / 미완료 1`, 그러나 `29/29 미검증`
- 독립 통합 QA R6I: `AB-R6-001~040` 전부 OPEN
- Severity: `P0 31 / P1 8 / P2 1`
- FDEV 25건: `미완료 18 / 부분 구현 4 / 미검증 3`
- 검증 상태: `실패 18 / 미검증 7`
- ADM: Route 63개, 12개 Route·55 Operation 실제 Component Consumer gap, 4개 메뉴 탐색 경로 누락
- EDU: 구조 수량 135개 충족, 독립 검증 완료 `0/135`

### 관리 판단

현재 CPF는 **Source 구현 폭은 상당하지만 제품화 Qualification은 닫히지 않은 상태**다. 빈 Framework나 문서-only 단계는 아니며 Approval, ADM CRUD, DB3 SQL, EDU 공통 Runtime, Release Script 등 실 구현이 존재한다. 반면 현재 exact SHA에서 상용 완료를 증명할 Runtime·복구·Compatibility·Artifact Evidence가 부족하고, Source 자체 결함도 P0 다수로 남아 있다.

따라서 진행 상태를 단일 완성률로 표현하면 오해가 생긴다.

| 평가 축 | 현재 판단 | 남은 핵심 |
|---|---|---|
| 제품 Source 폭 | 상당한 구현 존재 | Repository 전체 Consumer·경계 회귀 확인 |
| 결함 종결 | 낮음 | 40개 통합 Finding 전체 OPEN |
| 보안·승인·복구 | 핵심 구현 존재, 상용 폐쇄 미완료 | exact tuple, single-use, process-kill, durable reconcile |
| ADM/BZA | 다수 실 CRUD 존재 | 63 Route 실제 Consumer·권한·Browser·generated parity |
| EDU | 135 구조 충족 | 135개 의미 구현·실 Consumer·Runtime Evidence |
| DB·분산 Runtime | SQL/Runner 구조 존재 | DB3 live, multi-instance, broker, kill/recovery |
| Release·Evidence | 문서·스크립트 구조 존재 | current SHA provenance, final artifact qualification, non-optional gates |
| GA 준비도 | Release Blocked | Source closure → Runtime qualification → Artifact qualification → 독립 재검수 |

## 2. 얼마나 남았는가

남은 작업은 단순 40개 파일 수정이 아니라 다음 **4개 완료층**이다.

1. **확정 결함 Source Closure**: AB-R6-001~040의 공통 원인을 제거하고 실제 Consumer·호출 경로·Test를 닫는다.
2. **통합 Runtime Qualification**: Java25/Gradle9.1, DB3, ADM/BZA Browser, Broker, Multi-instance, Process Kill을 같은 result SHA에서 실행한다.
3. **Final Artifact Qualification**: Published BOM/POM/JAR/WAR/static artifact, install/upgrade/rollback, signature/SBOM/license/vulnerability를 최종 산출물 기준으로 검증한다.
4. **독립 검수 종결**: Codex 독립검수 후 QA A·B가 동일 SHA·동일 Artifact로 재검수한다.

한 번의 부분 Patch로 종료할 범위가 아니다. 최소한 **개발 자체검수 1회 + Codex 독립검수 1회 + QA A·B 재검수 1회**가 모두 동일 result SHA에 결속돼야 한다. 각 단계에서 P0가 재개방되면 같은 ID로 다시 개발한다.

## 3. QA Finding 외 추가 개발 강화 범위

아래 항목은 현재 40개 Finding을 임의 확대하는 신규 QA 결함 판정이 아니다. 최상위 정본의 공통 완료 축과 GA Gate를 빠뜨리지 않기 위한 **개발GPT 필수 자기점검 범위**다.

### MGR-HARDEN-001 — Repository 전체 Public API·SPI·Internal·Consumer 재검산
변경된 Approval/DQ 경로만 보지 말고 모든 Module의 외부 Internal 참조, Owner 없는 기능, Consumer 없는 SPI, 역방향·순환 의존, 중복 Primary 구현을 전수 확인한다.

### MGR-HARDEN-002 — 모든 상태변경 Operation의 신뢰성 표준 통일
ADM·BZA·Batch·Gateway·File·Notification·Approval의 모든 Command에 request/idempotency key, expectedVersion, duplicate convergence, response-loss, UNKNOWN, reconcile, audit를 적용 가능한 범위에서 동일 기준으로 검산한다.

### MGR-HARDEN-003 — 전체 DB Lifecycle·Compatibility Chain
V104 단건이 아니라 Empty Install, reinstall, mandatory seed, 모든 지원 Upgrade 시작점, rollback/forward recovery, drift, backup/restore, mixed-version을 Oracle/PostgreSQL/MariaDB에서 검증한다.

### MGR-HARDEN-004 — Final Artifact Supply-chain
Source Directory가 아니라 최종 JAR/WAR/static/offline bundle을 대상으로 CycloneDX, ORT, Syft, Grype, OSS lock, license obligation, THIRD_PARTY_NOTICES, signature/trust/revocation을 결속한다.

### MGR-HARDEN-005 — Resource·Capacity·Performance·Backpressure
메모리·Thread·Connection·Queue·Disk·Temp file·Streaming 크기 제한과 cleanup을 확인하고 ADM 대량조회, Export, Batch, Gateway, EDU에 load/soak/backpressure/failure injection을 추가한다.

### MGR-HARDEN-006 — Observability·SLI/SLO·Audit Integrity
system/domain/instance/transaction/segment/attempt/job/execution 식별자 상관관계, bounded metric cardinality, masking, burn-rate, alert dedup/escalation, append-only/tamper-evident audit를 Runtime으로 검증한다.

### MGR-HARDEN-007 — Repository-wide Security Negative Corpus
XSS, CSRF, SSRF, IDOR, mass assignment, injection, path traversal, archive bomb, unsafe deserialization/process, tenant escape, raw export, session fixation, secret leakage를 Backend·Frontend·File·Gateway·Evidence 경계에서 실행한다.

### MGR-HARDEN-008 — Backup·Restore·DR·Power-loss Recovery
DB·Artifact·Configuration·Batch/Gateway Runtime을 대상으로 backup/restore, DR 전환·복귀, split-brain 방지, deploy 중 power loss, selective rollback과 desired/actual reconcile을 확인한다.

### MGR-HARDEN-009 — Artifact Consumer·Offline/Remote 설치 검증
LOCAL_DEV·REMOTE Registry·OFFLINE Bundle을 각각 fresh consumer project에서 검증하고 REMOTE/OFFLINE 실패 시 개발자 Local Maven fallback을 금지한다.

### MGR-HARDEN-010 — Generator create→runtime→remove→regenerate
Canonical Catalog로 생성한 Domain이 build/runtime/DB3/OpenAPI/ADM·BZA/EDU까지 연결되고 remove/regenerate 후 stale file·manual patch·generated diff가 남지 않는지 검증한다.

### MGR-HARDEN-011 — Traceability·Documentation·Hygiene
Requirement→Source/API/SQL/Test/Runtime/Evidence와 역방향 추적을 만들고, stale Evidence, dead code, dual primary, temporary output, long path, root garbage, 문서와 Runtime drift를 0으로 만든다.

### MGR-HARDEN-012 — Compatibility·Failure Matrix
Local/Remote, 1/2+ instance, rolling mixed version, Oracle/PostgreSQL/MariaDB, Chromium/Firefox/WebKit, network loss, broker duplicate, DB outage, process kill을 조합한 지원 Matrix를 명시하고 실제 지원 조합만 GA로 표기한다.

## 4. 개발GPT 작업 전략

40개 Finding을 ID 순서로 개별 봉합하면 같은 결함이 반복된다. 다음 Root Cause 단위로 처리한다.

1. **Canonical Drift**: Route/Menu/Permission/OpenAPI/Generated Client/EDU Catalog를 단일 정본에서 생성·검증한다.
2. **Runtime-Provenance Disconnect**: Source SHA, result SHA, artifact SHA, evidence SHA를 하나의 실행 원장에 결속한다.
3. **Security Capability Boundary**: 승인·Secret·권한·Capability 검증을 Provider/UI 규율이 아니라 Framework-owned 경계에서 강제한다.
4. **Recovery Gap**: RUNNING/UNKNOWN/side-effect/DB outage/process kill을 durable ledger와 observation/reconcile로 수렴시킨다.
5. **Synthetic False-Green**: 문자열 Gate, deterministic double, synthetic permission/HTTP response를 실제 mutation·consumer·runtime test로 대체한다.
6. **Template False-Completion**: EDU/Workbench의 공통 Wrapper는 기반으로만 사용하고 각 ID·메뉴 고유 의미를 executable behavior로 닫는다.

## 5. 개발GPT가 추가로 갱신할 관리 파일

기존 R6I 필수 결과물에 더해 다음을 포함한다.

- `cpf-docs/work/v9i/dev/r6i/HARDENING_STATUS.csv` — MGR-HARDEN-001~012 개발·검증 상태
- `cpf-docs/work/v9i/dev/r6i/RUNTIME_QUALIFICATION_MATRIX.csv`
- `cpf-docs/work/v9i/dev/r6i/COMPATIBILITY_MATRIX.csv`
- `cpf-docs/work/v9i/dev/r6i/ARTIFACT_SUPPLY_CHAIN_EVIDENCE.md`
- `cpf-docs/work/v9i/dev/r6i/TRACEABILITY_MATRIX.csv`
- `cpf-docs/work/v9i/dev/r6i/REPOSITORY_HYGIENE_REPORT.md`

각 파일은 READY/PLANNED를 PASS로 기록하지 않고 command, environment, exit code, actual result, artifact/log SHA-256을 연결한다.

## 6. 통합 관리 결론

- 기존 Source는 보존·확장할 가치가 있으며 전면 재작성으로 되돌릴 단계는 아니다.
- 하지만 현재는 Release Candidate가 아니라 **대규모 재개발 및 Qualification 단계**다.
- 최우선은 문서/Evidence 재작성보다 P0 Source 결함과 실제 Consumer·복구 경로를 먼저 닫는 것이다.
- 최종 완료 주장은 40개 Finding과 12개 강화항목, FDEV-001~025, Target Runtime, Final Artifact, Codex, QA A·B가 모두 같은 SHA에서 종결될 때만 가능하다.
