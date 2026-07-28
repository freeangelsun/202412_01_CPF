# Codex Final Verification & Repair Request — CPF 20260728_01

## Mission

Baseline `ca3cf8a12290903cc482b5e092cdb43e6bf8f1eb`에 CPF 20260728_01 Enterprise QA Overlay를 적용한 최신 master를 기준으로 **검증만 하지 말고, 실패가 발견되면 Root Cause를 수정하여 최종 완료 상태까지 닫는다.**

QA 원본은 1,214 requirements + 201 execution scenarios다. 과거 문서의 완료 표시보다 현재 Git Source/SQL/API/Test/Runtime/Evidence를 우선한다.

## Non-negotiable

- Gate 삭제/완화/skip으로 green 처리 금지
- 실행하지 않은 Test를 PASS 기록 금지
- Sample을 제품 구현으로 판정 금지
- Interface만 존재하고 실제 Consumer가 없으면 완료 금지
- MariaDB만 성공하고 PostgreSQL/Oracle 미검증 상태로 완료 금지
- MySQL/MSSQL/H2 공식 지원 복원 금지
- 민감정보 원문 Evidence 금지
- 검증 중 checksum manifest 자동 수정 후 PASS 처리 금지
- 사용자 승인 없이 commit/push/branch 생성 금지

## 1. Overlay Post Apply

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\apply-20260728-enterprise-qa-closing.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
```

실행 전/후 `git status --short`, `git diff --stat`, `git diff`를 보존한다.

## 2. P0 First Failure

```powershell
.\gradlew.bat verifyVersionConsistency --no-daemon
.\gradlew.bat verifyBatStandaloneArchitecture --no-daemon
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-legacy-batch-migration.ps1
.\gradlew.bat :cpf-batch:verifyStandaloneArtifacts --no-daemon
.\gradlew.bat verifyCpfFinalSourceGates --no-daemon
```

첫 실패부터 Source/Ownership/Artifact 구조를 수정한다.

## 3. Build / Static / Security

```powershell
.\gradlew.bat clean test assemble --no-daemon
.\gradlew.bat qualityGate --no-daemon
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-r11-public-boundary.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-service-call-boundary.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-security-seed-standard.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

## 4. Runtime Control Plane

반드시 2개 이상 Runtime instance를 사용하여 다음을 검증한다.

1. Agent register/heartbeat/lease/fencing
2. single instance change
3. service/group/label/zone/cell target
4. invalid target = fail-closed
5. operationId same payload replay / different payload conflict
6. expectedVersion conflict
7. delivery retry/backoff
8. stale ACK and stale fencing rejection
9. process restart after pending change
10. desired/actual drift
11. group nested cycle rejection
12. cancel and rollback
13. offline instance recovery
14. partial rollout / quorum state
15. audit history and sensitive payload masking

DB outage와 ADM outage가 원 업무 Runtime을 불필요하게 오염시키지 않는지도 확인한다.

## 5. Gateway

- URI/Header execution ID mismatch
- unauthenticated protected route rejection
- invalid token/API key rejection
- raw credential downstream non-forwarding
- auditReasonRequired + durable audit fail-closed
- method mismatch rejection
- unsafe target URI rejection
- priority/weight/zone/cell selection
- maintenance/drain exclusion
- target-down / timeout / 408 / 425 / 429 / 5xx retry
- normal 4xx no retry
- UNKNOWN_RESULT + reconciliation
- route snapshot empty boot fail-closed
- route refresh failure retains last good snapshot
- large body / binary / multipart / streaming / range / backpressure scenarios는 실제 실행하여 구현이 충분하지 않으면 Source를 보완한 뒤 완료 처리한다.

## 6. Cache

- DB mutation + event insert same transaction
- event insert failure rollback
- instance A/B propagation
- consumer restart and checkpoint replay
- prolonged consumer downtime then catch-up
- duplicate event idempotency
- checkpoint update failure
- Code/Message/ResponseCode/Config parity

## 7. BZA Login

- same operationId + same request exact response replay
- same operationId + different password/request hash conflict
- concurrent login retry
- response lost after DB commit then retry
- exactly one refresh session creation
- stored result ciphertext only; plaintext token not persisted
- result expiry behavior
- failed login counter/history atomicity
- refresh rotation/reuse attack regression

## 8. Service Registry

- Service/Endpoint/Instance CRUD
- expectedVersion CAS conflict
- operationId fingerprint conflict
- child ownership delete protection
- DRAIN vs DISABLE vs RESUME semantics
- three Vendor query portability
- health update + circuit update concurrent race
- Gateway Runtime consumer immediately reflects state

## 9. Official DB Lifecycle

MariaDB/PostgreSQL/Oracle each:

1. clean install
2. seed
3. verify
4. V64/V65-equivalent migration application
5. Runtime query
6. rollback
7. reapply
8. schema drift
9. checksum tamper negative test
10. index/FK/check validity

`generate-migration-checksums.ps1 -Apply`는 Overlay 적용 maintenance 단계에서 한 번 수행한다. 실제 검증 중 기존 migration을 변조한 뒤 manifest를 다시 생성해서 PASS시키면 실패다.

## 10. ADM/BZA Frontend

Build/test 후 Browser에서 실제 Route를 확인한다. 특히 Runtime Change/ACK/Drift/Registry 상태를 운영자가 구분할 수 있는지 확인하고, raw PII는 최소기간 보유 후 zeroization 되는지 확인한다. 위험 조치는 권한/사유/승인/감사 결과를 확인한다.

## 11. BAT / Generated Domain

기존 handover P0/P7/P8을 그대로 실행한다. Runtime Control Plane 변경으로 BAT/Generated Domain regression이 생기지 않았는지 local/remote topology 양쪽을 검증한다.

## 12. Evidence Contract

각 Evidence에 최소:

- exact commit SHA
- command
- start/end timestamp
- profile/environment
- related QA IDs/area
- actual output/result
- redaction 여부
- PASS/FAIL reason

과거 Commit Evidence를 현재 PASS로 승계하지 않는다.

## 13. Garbage / Hygiene

먼저:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\cleanup-20260728-enterprise-qa.ps1 -Root "C:\dev\projects\jck\202412_01_CPF" -WhatIf
```

후보를 Source/Requirement/Evidence owner와 대조한 후 실제 삭제한다. `build`, `node_modules`, logs, tmp, patch/bak/orig/rej, stale generated SQL, dead source, stale evidence를 검사하되 제품에 필요한 파일은 기계적으로 삭제하지 않는다.

## Final Exit Criteria

- QA requirement → Source/API/SQL/Test/Runtime/Evidence 추적 가능
- 구현 → Requirement/Owner/Consumer/운영기능 역추적 가능
- 최신 master 전체 Gate PASS
- 3 DB vendor PASS
- Runtime multi-instance PASS
- ADM/BZA/Gateway/BAT/Generated Domain PASS
- Release/install/upgrade/rollback/recovery PASS
- Repository hygiene PASS
- 미구현/부분구현을 남기지 않음
- 환경상 직접 실행하지 못한 항목이 있다면 완료가 아니라 `미검증`으로 정확히 기록하고 재현 명령을 남김
