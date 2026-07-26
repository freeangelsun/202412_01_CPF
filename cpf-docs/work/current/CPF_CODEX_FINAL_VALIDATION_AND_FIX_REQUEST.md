# Codex 최종 검증·수정 지침

> 이 문서는 검수 보고만 작성하는 요청이 아니다. 최신 master를 검증하면서 재현되는 결함을 즉시 수정하고, 같은 검증을 다시 실행하여 Evidence까지 남기는 실행 지침이다.

# Codex 요청 — CPF R14 최신 master 통합검증 + 잔존 결함 수정 Closure

## 목표
R14 Overlay를 사용자가 적용/Push한 뒤 **그 최신 master를 기준으로 검증하면서 발견되는 결함을 즉시 수정하고 다시 검증**한다. 단순 검수 보고서 작성이 목적이 아니다. CPF 최종 목표에 필요한 실제 Source/SQL/API/Test/Frontend/Script/Evidence 일치를 만드는 것이 목적이다.

## 시작 절차
1. `git pull --ff-only origin master`
2. `git rev-parse HEAD`를 기록하고 이 SHA를 모든 Evidence의 기준으로 사용.
3. 아래 정본을 먼저 읽고 역할을 확인.
   - `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
   - `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
   - `cpf-docs/work/state/CPF_R14_HANDOVER.md`
   - `cpf-docs/review/CPF_MASTER_FULL_DEFECT_AUDIT_20260726.md`
   - `specs/기능_구현_매트릭스.html`
   - `specs/sample-coverage-matrix.md`
4. 문서 완료표시를 신뢰하지 말고 최신 Source/SQL/API/Test/Config/Frontend/Script를 직접 확인.

## 작업 방식
- QA 289개 ID + 제품 Gap 12개를 최신 Source 기준으로 재판정한다.
- 같은 root cause는 하나의 구조 수정으로 묶고 이름만 바꿔 중복 집계하지 않는다.
- P0/P1 잔존 결함은 검증 중 가능한 범위에서 즉시 수정하고 해당 검증을 재실행한다.
- Interface만 추가하고 실제 Consumer/default implementation/recovery/ops가 없으면 완료 처리하지 않는다.
- 잘못된 Ownership은 호환을 핑계로 유지하지 말고 대체 구현과 Consumer 이관 후 불필요 Legacy를 제거한다.
- 사용자의 명시 승인 없이 commit/push/branch를 만들지 않는다.

## 반드시 보호할 구조
- Generated Domain은 `com.cpf.core.common.*` 직접 import 금지.
- `cpf-common.utils` legacy 복원 금지.
- BAT이 Batch/Center-Cut Runtime Owner.
- ADM이 MBR/BAT Owner DB 직접 조회 금지.
- `UNKNOWN_RESULT` 보존.
- ADM/BZA 권한 fail-closed.
- EXS는 Generator 생성형. 고정 Module/DB/verify dependency로 복원 금지.
- DB 정본은 `cpf-tools/db/vendor/<vendor>`이며 `cpf-tools/db/source` 복원 금지.
- unsupported Vendor를 MariaDB SQL 복사/치환으로 지원 완료 처리 금지.

## 1. Build / Static Gate
Windows 기준:
```powershell
.\gradlew.bat clean test assemble qualityGate --no-daemon
```
추가 확인:
- Public API/SPI/Internal dependency/ArchUnit/jdeps gate
- Generated Domain direct internal import 0건
- Repository hygiene/secret scan
- Runtime/Generator SQL resource path가 canonical vendor source와 일치

## 2. Frontend
ADM/BZA 각각:
```powershell
cd cpf-admin/frontend
npm ci
npm run verify

cd ../../cpf-biz-admin/frontend
npm ci
npm run verify
```
반드시 확인:
- 모든 Route import resolve
- Vue SFC parse 0 error
- 400/401/403/409/500에서 성공 Toast 금지
- logout/401 후 민감 state reset
- Role별 route/menu/button/API fail-closed
- BZA 10개 이상 동시 401에서 refresh single-flight
- 비밀번호 강제변경/만료 계정의 직접 REST 우회 차단
- Approval Inbox는 자신의 available action만 실행 가능
- 위험 조작은 target preview/reason/권한/재확인/감사 확인
- keyboard/accessibility/console error 0 Browser smoke

## 3. MariaDB
실제 clean DB에서:
- Provision
- Fresh Empty Install
- Product Seed
- Verify
- V52 upgrade
- 이후 최신 migration upgrade
- rollback safety/precondition
- re-apply
- migration checksum/parity/drift

특히 확인:
- `cpf-tools/db/vendor/mariadb/source`만 canonical source
- Source → lifecycle bundle byte/semantic parity
- EXS 고정 DB가 Platform install/verify 필수조건이 아님
- 기본 Metadata catalog의 code/message/response/config가 seed와 일치
- 존재하지 않는 Column/Index/FK 참조 0건
- 기존 Schema가 다르면 silent skip이 아니라 drift/migration 오류

## 4. Generator
서로 다른 두 Domain/SystemCode로:
- dry-run
- create
- build/test
- 동일 입력 재실행 conflict
- package/module/systemCode/route/DB collision
- remove
- re-create
- 생성 Domain internal import gate
- 생성 DB/Config/OpenAPI/Test 정합성

## 5. Health / Multi-instance
ADM/BAT를 각각 2개 인스턴스로 실행한다.
- 각 liveness는 자기 프로세스만 검사
- 각 readiness는 자기 필수 dependency만 검사
- Registry는 전체 instance 상태를 종합
- DB down에서 liveness UP/readiness DOWN
- 한 node down 시 다른 node까지 DOWN으로 오염되지 않음
- serverInstanceId/host/process/profile/checkedAt가 로그/registry와 연결
- failover 후 신규 transaction routing과 기존 UNKNOWN_RESULT 처리 확인

## 6. MBR / 동시성 / 멱등
- 회원번호 동시 생성/다중 인스턴스
- member/status/role optimistic conflict
- 동일 operationId replay
- 동일 idempotency key + 다른 payload 거부
- DB down/query failure가 NotFound/빈 결과로 위장되지 않음
- member/role/history SQL paging
- 개인정보 masking/audit 원문 노출 점검

## 7. Cache / Config convergence
- preload 정상/실패/readiness
- publish event
- listener restart
- retry/backlog
- 2 node convergence
- stale node 식별 및 reconcile
- cache event 저장소 장애/프로세스 restart 시 event 유실 여부
잔존 process-memory cursor/retry queue가 있으면 P0/P1로 수정한다.

## 8. Service Call / Async / Batch / File
local/remote 양쪽에서 같은 계약을 사용해:
- success/validation/4xx/5xx
- timeout/retry
- target-down
- failover
- circuit open + healthy alternate instance
- commit-then-timeout
- UNKNOWN_RESULT durable registration
- reconciliation store down
- Outbox/Inbox/DLQ/reprocess
- Batch Worker lease/fencing/lock/ghost
- File transfer path sandbox, duplicate identity, transfer-success/history-fail

비멱등 Command를 일반 transport retry로 중복 실행하지 않는다.

## 9. Security / Audit
- Secret 원문이 API/Config/Log/Audit/Trace/CSV/Evidence에 없음
- JWT/session password-change/expiry/logout/revoke 정책
- disabled Role/Menu가 권한을 주지 않음
- permission environment/domain/data scope 평가
- audit before/after recursive masking
- audit hash/ledger/legal hold가 구현된 범위는 실제 tamper 검증
- 내부 MBR admin API의 caller identity를 annotation뿐 아니라 Runtime에서 집행

## 10. Release / Supply Chain
아래는 실제 실패 Gate가 되어야 완료다.
- dependency locking/verification
- frontend 포함 표준 SBOM
- License allow/deny/review
- CVE gate
- clean tree/protected release condition
- artifact count/version consistency
- source/material/builder provenance
- signature/attestation
- prod inventory template fallback 금지
- reproducible build 확인

현재 골격 수준이면 `부분 구현`으로 남기고 상용 Release에 필요한 최소 기능을 구현한다.

## 11. Evidence
각 실행 결과에 반드시:
- HEAD SHA
- exact command
- profile/environment
- start/end time
- 관련 QA/Requirement ID
- actual result
- raw log/DB query/browser artifact
- sensitive data scrub 여부
- 현재 SHA에 유효한지

직접 실행하지 않은 항목은 `미검증`이다. 과거 Commit/다른 PC 결과를 현재 PASS로 승계하지 않는다.

## 종료 산출물
- 최신 `CPF_CURRENT_WORK_REQUEST.md`: 실제 남은 일만
- Handover: 최신 SHA, 완료/부분/미검증/실패, Architecture 결정
- QA 289 ID 재판정 표
- Evidence index
- 실제 수정 Source/SQL/Test/Frontend/Script/Guide
- 전체 최종 Gap 요약

“Build PASS” 하나만으로 CPF 완료를 선언하지 않는다. Requirement → Source/API/SQL/Test/Runtime/Evidence와 Implementation → Requirement/Owner/Consumer/운영기능을 양방향으로 검증한다.

## 12. R14 회귀 시나리오 — 반드시 실제로 실행
### 12.1 BZA Audit Chain
- 정상 신규 감사 연속 기록 후 `VALID`.
- Hash 도입 전 legacy row가 있으면 `PARTIAL_LEGACY`, 무조건 VALID로 승격 금지.
- 임의 audit row before/after/record_hash 변조 후 `BROKEN`.
- 마지막 audit row를 삭제하고 `bza_audit_chain_lock.current_hash`를 그대로 둔 뒤 `BROKEN` 및 chain-head mismatch 확인.
- 2개 BZA instance 동시 audit writer에서도 hash 순서/lock 정합성 확인.

### 12.2 BZA Role History / Permission
- role history가 존재하지만 모든 grant가 만료/회수된 사용자는 legacy `bza_admin_user.role_code` fallback으로 권한을 되살리지 않는다.
- role history가 한 번도 없는 legacy 계정만 active legacy role fallback 가능.
- 동일 `operationId` + 동일 login/role 재요청은 중복 row 없이 idempotent 결과.
- 동일 `operationId`를 다른 login/role payload에 재사용하면 conflict/validation 실패.
- primary role 동시 부여에서 최종 active primary가 하나이며 legacy role_code와 호환 동기화.
- disabled role/menu 및 다른 environment permission은 실효 권한에서 제외.
- domain/dataScope/httpMethod/apiPattern이 아직 통합 평가되지 않으면 완료 처리하지 말고 구현 후 role/API matrix 재실행.

### 12.3 Retention
- `dryRun=true`: archive/delete 0건.
- legal hold: destructive mutation 0건.
- ARCHIVE/PURGE + cutoff 누락: 400 계열 실패.
- `cpf.retention.execute-enabled=false` 기본 상태: 403/fail-closed, 원본 변경 0건.
- kill-switch ON: ARCHIVE는 archive insert와 original delete가 transaction 단위로 일치.
- R54 rollback은 archive row가 있으면 실패하고 데이터 유실이 없어야 함.

### 12.4 Secret / Certificate
- ADM Secret API 응답, Log, Audit, Exception에 raw secret value 0건.
- ENV provider는 metadata/read-reference만 지원하고 rotate는 명확히 unsupported.
- Rotatable provider가 있으면 permission+reason+audit를 확인.
- certificate expiry 도구 Evidence에 private key/secret 0건.
- Vault/KMS/HSM adapter가 실제 제품 요구에 필요한데 없으면 `부분 구현`으로 유지하고 구현 계획/Owner를 명확히 한다.

### 12.5 Backup / Restore / DR
- backup manifest가 `containsSensitiveData=true`, `credentialEmbedded=false`인지 확인.
- backup SHA 변조 시 restore 실패.
- manifest database와 restore target database가 다르면 실패.
- manifest 누락은 `AllowMissingManifest` 명시 없이는 실패.
- isolated single-DB restore는 baseline 또는 `VerifySql`로 검증.
- 모든 logical DB가 복구된 경우만 `RunPlatformVerify` 실행.
- DR Evidence의 startedAt/finishedAt/duration이 실제 실행과 일치.

### 12.6 Paging / Owner Boundary
- BZA admin-user/menu/role/permission/org/employee/position/job-title/assignment/responsibility/user-role `/page` API를 대량 데이터로 검증.
- `versionNo -> expectedVersion` CAS 충돌은 409로 처리하고 lost update 금지.
- ADM member `/page`는 MBR Owner port를 경유하며 MBR SQL에 count + LIMIT/OFFSET 또는 동등 DB paging이 실제 적용.
- ADM의 MBR DB 직접 query 0건.

### 12.7 Tenant
- default disabled에서 기존 요청 회귀 없음.
- enabled + resolver 미구성은 503.
- resolver가 tenant를 결정하지 못하면 400/fail-closed.
- 요청 종료/예외에서 ThreadLocal clear.
- executor/async/thread reuse에서 tenant leakage 0건.
- DB row/schema isolation을 실행하지 않았다면 tenant 기능 전체를 완료 처리하지 않는다.

### 12.8 DB Canonical / Migration
- canonical source에서 8개 bundle을 재생성하고 byte/semantic parity 확인.
- central provision/install/seed/verify copy와 canonical source 6개 bundle byte-identical.
- V53/V54의 canonical source와 lifecycle migration copy byte-identical.
- R53/R54 canonical rollback과 lifecycle rollback byte-identical.
- checksums.sha256의 V53/V54 실제 hash와 일치.
- Metadata catalog의 모든 required group/value/message/response/config가 MariaDB product seed에 존재.
- 고정 `exsDB` CREATE 또는 `FROM/JOIN exsDB.*`가 Platform product install/verify에 없음.

## 13. 검증하면서 수정해야 하는 원칙
이 요청은 report-only QA가 아니다. 위 시나리오 또는 289 QA ID에서 P0/P1 결함을 재현하면 Owner/Consumer/SQL/Test/Frontend/Guide/Evidence를 함께 수정하고 같은 시나리오를 다시 실행한다. 수정하지 못한 항목만 원인과 막힌 조건을 `미구현/미검증/재확인 필요`로 남긴다. 단순 TODO 문서 추가로 Closure 처리하지 않는다.
