# CPF Codex 중간점검·직접보정 지침 — C 개발/QA 관리_1_7 결과 기준

## 0. 목적과 Source 정본

이 지침은 Developer GPT가 2026-08-27까지 수행한 재개발 결과를 Codex가 독립적으로 공격 검수하고, 발견한 결함은 **검수와 동시에 직접 수정**하기 위한 중간 작업지침이다.

- Source 정본: **사용자 Local Working Tree**
- Developer 전달 Source Identity SHA-256: `79264c2975bd0b8504a0e2f8ec375070c08699ebcb512e26323d90d7e39490fb`
- Canonical Product Requirements: `208`
- Developer Closure Inventory: `127/127 development_status=완료`
- Developer verification: `7 완료 / 120 미검증`
- Git: RT-02 local provenance **read-only**만 허용. fetch/checkout/reset/restore/clean/stash/commit/push/branch/tag/PR/release 금지.
- Local Git history에 baseline이 없으면 `UNAVAILABLE`; remote 복구/re-anchor 금지.
- Released V/R Migration byte/checksum/baseline 재-anchor 금지.

Codex는 작업 시작 시 반드시 현재 Local Working Tree Source Identity를 다시 계산한다. 위 SHA-256과 다르면 현재 Working Tree를 새 기준으로 삼고 Developer Evidence를 자동 PASS로 승계하지 않는다.

## 1. 역할 경계

- Codex는 `Codex_*` 컬럼과 Codex Source/Evidence만 수정한다.
- Developer GPT/QA 상태 컬럼을 임의 수정하지 않는다.
- 다만 Codex가 Source를 수정해 기존 검수 영향이 발생하면 자신의 Finding/Evidence에서 영향범위를 재개방해 추적한다.
- QA만 최종 전체 상태를 확정한다.

## 2. 절대 규칙 — 수정과 문서 현행화는 같은 작업

**Codex가 Source를 수정하면 반드시 같은 변경에서 다음을 동시에 처리한다.**

1. Source/API/SPI/Internal owner와 실제 Consumer.
2. Test/Verifier/Gate/Harness.
3. Config/Profile/SQL/DB3/Generator/Generated Domain/OpenAPI/Frontend 영향.
4. 관련 Architecture/Specification/운영·개발 문서.
5. `CPF_CURRENT_WORK_REQUEST.md` 및 필요한 Requirement currentization.
6. Codex Finding/Evidence.
7. stale/duplicate/temp 자료의 `DELETE_MANIFEST.csv` currentization.

Source만 수정하고 문서를 다음 작업으로 미루는 것은 **FAIL**이다. 문서만 고쳐 Source 결함을 숨기는 것도 **FAIL**이다.

## 3. 모든 테스트는 고강도

Static/Unit/Contract/Build/DB/Runtime/Browser/Performance 구분 없이 **모든 테스트는 고강도**다.

- Repository-wide Owner/Dependency/Consumer/Public API·SPI·Internal boundary.
- 정상/오류/경계/null/empty/max-min/invalid/duplicate/stale/unsupported/forbidden.
- timeout/retry/concurrency/race/partial failure/process kill/restart/UNKNOWN/reconcile/idempotency.
- provider down/reconnect, secret/auth/approval/audit/masking.
- DB3 Oracle/PostgreSQL/MariaDB parity, migration/rollback/reapply/recovery.
- Generator/Generated Domain/Fresh regeneration/idempotency.
- Frontend Generated Client/실제 Consumer/Runtime OpenAPI/Browser/a11y/error status.
- Side Effect/regression/cleanup/rerun/Fresh Replay.
- False Green/False Red를 공격적으로 탐지한다.

`Smoke`, `DRY_RUN`, `READY`, `PLANNED`, `NOT_EXECUTED`, compiler warning disable, broad suppression을 PASS로 사용하지 않는다.

## 4. 기존 Codex 원장 Carry-over — 우선순위 유지

현재 `CODEX_FINDING_CLOSURE.csv`를 **읽기 정본**으로 사용한다. Developer GPT는 이 상태를 수정하지 않았다.

### IN_PROGRESS 3 — 최우선 재검증

1. `CX-F-026 / REL-BUILD` — Java25 Root build/frontend compile/test 전 범위.
2. `CX-F-258 / REPOSITORY-HYGIENE` — nested doc owner/Windows Path/current-only/garbage.
3. `CX-F-307 / DB3-RUNTIME` — DB3 table count/Oracle readiness/current physical lifecycle.

### SOURCE_FIXED 10 — 반드시 물리 재검증

`CX-F-099`, `CX-F-101`, `CX-F-104`, `CX-F-105`, `CX-F-181`, `CX-F-182`, `CX-F-183`, `CX-F-221`, `CX-F-237`, `CX-F-290`.

Source-fixed라는 이유로 CLOSED하지 않는다. 영향 Consumer/Runtime/Evidence까지 확인한다.

### VERIFICATION_PENDING 94

기존 94건은 삭제하거나 일괄 CLOSED하지 않는다. Developer 변경 영향과 겹치는 항목부터 우선 검증한다. 특히 REL-BUILD/PUBLIC-API/BATCH-RUNTIME/DB3/SEC-BOUNDARY/LOGGING/Open Git/Generator/Frontend 항목을 우선한다.

### CLOSED 203

전체 재실행 대상은 아니지만 Developer 변경이 닿은 Owner/Consumer/Verifier라면 **재발 여부를 반드시 공격 검증**한다. 재발 시 이전 Closure의 누락 범위를 Root Cause로 기록한다.

## 5. Developer GPT 변경 중 Codex 필수 공격검증 항목

### 5.1 Compile / VSCode / Dependency

- `CpfAttributes` java.util import 및 base/common logging dependency.
- Comparator/generic inference/lambda shadowing 보정 파일 전체.
- JPA `CpfSearchSpec.Operator` owner 정합.
- CSRF/Header/Message provider/Test import/overload.
- Retention/Notification/SFTP/Archive/MyBatis missing type는 타입 복제로 해결하지 말고 canonical owner + Gradle/JDT classpath를 검증.
- Java25 + Gradle9.1 Fresh Import/JDT에서 **Error 0 / Warning 0**.

### 5.2 Logging — 필수 최우선

실제 거래 1건 이상으로 다음을 전부 확인한다.

- `transactionId`, `traceId`, `correlationId`, `executionId`, `segmentId`.
- `originalSystemCode`, `systemCode`, `callerSystemCode`, `targetSystemCode`, `operationId`, `instanceId`.
- File structured log와 일반 file pattern.
- DB transaction log/segment/timeline.
- ADM `/logs` 및 observability transaction/timeline consumer.
- Header6 + W3C trace + same-JVM Context.
- 오류/재시도/비동기/Batch/Backoffice에서 lineage 단절 0.
- masking/secret/raw session id leakage 0.
- file/db failure isolation과 recovery/reconcile.

Developer가 존재하지 않는 `/api/education/query/headers` Probe를 실제 `/edu/online/member-processing` 거래로 currentize했으므로, Codex는 실제 Runtime endpoint/operationId가 현재 Source와 일치하는지 다시 확인한다.

### 5.3 ADM / Backoffice — 실제 기능

정적 Route 수만 검증하지 않는다.

- 로그인/세션/권한.
- 메뉴/검색/Paging/상세.
- transaction/log/timeline 실제 조회.
- 위험 조치/Approval/사유/Audit.
- Batch runtime control.
- Generated OpenAPI Client 실제 Consumer.
- 401/403/404/409/429/500/503.
- Browser E2E/a11y/responsive.
- Node22.18+ lint/typecheck/test/build.

### 5.4 Generator / Generated Domain

- member/external/Scratch clean regeneration.
- idempotency diff 0.
- Domain 1→2→3 automatic discovery.
- user-owned file protection.
- online-only / online+batch.
- Public Starter direct boundary.
- Oracle/PostgreSQL/MariaDB logical/vendor separation.
- Java25 build/test/runtime.

### 5.5 Batch Standalone Shell / Profile

Developer가 추가한 5 역할 × Windows/Linux run/stop **20 Shell**과 dev/test/prod **15 Profile**을 실제 실행한다.

- Control Plane/Scheduler/Worker/Agent/Center-Cut.
- JVM path/options/profile/instanceId/port/config/log/PID/timeout/exit code.
- duplicate start/stale PID/normal stop/process kill/recovery/rerun.
- prod localhost/default password/test fixture/dev fallback 0.
- role별 effective config.
- Windows/Linux parity.

### 5.6 Kafka-free Batch Maximum Runtime

- 5 roles + Worker1/Worker2.
- DB claim/lease/fencing/concurrent claim.
- process kill/takeover/fencing increment.
- UNKNOWN_RESULT + explicit reconcile.
- blind retry/duplicate execution 0.
- Center-Cut/Gateway/Approval/Audit.
- canonical headers/context/trace/instance/execution attempt.
- Remote Kafka mandatory dependency/container/topic/listener 0.

### 5.7 DB3 / RT-02

- Working Tree SHA-256이 authoritative provenance인지 확인.
- Git SHA는 explicit local provenance compatibility일 뿐 required authority가 아님.
- sourceSha=`UNAVAILABLE`이어도 `sourceIdentitySha256`이 current Source와 맞으면 fail-closed lifecycle 가능.
- Released V/R bytes/checksum 재-anchor 0.
- Oracle/PostgreSQL/MariaDB Fresh→Seed→Verify→Runtime→Upgrade→Rollback→Reapply→Fault→Cleanup→Rerun.
- FK/index/query plan/schema drift/partial migration/bad checksum/permission/lock/contention 포함.

### 5.8 Performance Live Trust Boundary

Developer가 수정한 `release_target_trust.py`, broker/batch/resource contract를 반드시 독립 검증한다.

- canonical: exact 64-hex `sourceIdentitySha256`.
- signed deployment attestation의 `sourceIdentitySha256` mismatch/missing/tamper 시 fail-closed.
- artifact digest allowlist/signature 검증 유지.
- 40-hex Git `sourceSha`는 명시적 legacy compatibility에만 허용.
- Full Runtime가 `CPF_EXPECTED_SOURCE_IDENTITY`를 실제 probe에 전달하는지 확인.
- broker backpressure/reconnect/retry-DLQ, Batch kill/reconcile, resource budget을 live product probe로 실행.

### 5.9 Open Git Actual Fresh Release

Projection/test PASS만으로 완료 금지.

- Java25 Fresh framework publication.
- Maven-folder repository, JAR/POM/sources/javadoc/checksum/SBOM.
- allowed Source only: EDU/Generated Domain/Backoffice/Developer commands.
- private framework/internal/governance/evidence leakage 0.
- fresh remote clone-equivalent workspace.
- bootstrap/build/test/start/health/actual operation/stop/reset/rerun.
- Domain add/remove/optional mutation.
- commit/push 자동 실행 0.

### 5.10 Garbage / Current-only

- `.pytest_cache`, `__pycache__`, `.class`, build/temp/generated Runtime output 0.
- `cpf-tools/build/gradle-plugin/bin/**/*.class` 6건은 Delete Manifest 적용 대상.
- superseded current narrative는 replacement/reference가 currentized된 경우에만 제거.
- protected path 삭제 0.
- current Source/Managed State before/after drift 0.

## 6. Codex 작업 순서

1. Current Source Identity + prerequisite 확인.
2. 기존 Codex IN_PROGRESS/SOURCE_FIXED/VERIFICATION_PENDING과 Developer 변경 영향 매핑.
3. 낮은 비용의 모든 Static/Contract Gate를 먼저 전수 실행하여 실패를 공통 Root Cause로 묶는다.
4. Source + Consumer + Test + 문서를 **동시에 수정**한다.
5. Java25 Root Build/Test/Publication.
6. DB3 physical.
7. Batch maximum Runtime.
8. One-WAS + Logging + ADM/Backoffice Runtime OpenAPI.
9. Browser E2E/a11y.
10. Performance Live.
11. Open Git Actual Fresh Release/Golden Path.
12. Full Runtime.
13. 동일 Source Fresh Replay.
14. Codex Evidence/원장 currentize + garbage zero.

첫 오류 하나만 고치고 멈추지 않는다. 같은 Root Cause를 Repository 전체에서 찾고 일괄 수정한다.

## 7. Codex 완료 조건

Codex 중간점검 완료는 다음이 모두 충족돼야 한다.

- Codex가 확인한 모든 Finding이 `CLOSED` 또는 명확한 외부환경 `미검증`이며 미검증을 PASS로 쓰지 않음.
- Java25 Build/Test/Publication PASS.
- VSCode Error 0 / Warning 0.
- DB3 PASS.
- Batch maximum runtime PASS.
- File↔DB↔Trace↔ADM logging PASS.
- ADM/Backoffice Browser PASS.
- Performance Live PASS.
- Actual Open Git Fresh Release PASS.
- `FAIL=0 / mandatory SKIP_ENV=0 / NOT_EXECUTED=0 / unresolved UNKNOWN=0`.
- Source/Managed State PASS.
- Fresh Replay PASS.
- Source 변경과 관련 문서 현행화가 같은 change set에 있음.
- stale/duplicate/temp garbage 0.

Codex가 여기까지 닫지 못하면 정확한 명령/환경/오류/ExitCode/재실행 조건을 Evidence에 남기고 `미검증`으로 유지한다.
