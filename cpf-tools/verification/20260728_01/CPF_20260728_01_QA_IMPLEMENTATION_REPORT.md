# CPF 20260728_01 Enterprise QA Closing Implementation Report

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Baseline SHA: `ca3cf8a12290903cc482b5e092cdb43e6bf8f1eb`
- QA Inventory: 1,214 requirements
- QA Execution Scenarios: 201 scenarios
- Total trace items: 1,415
- 공식 DB Vendor: MariaDB / PostgreSQL / Oracle only

이 Change Set은 QA 항목을 단순 상태 변경하지 않고 공통 Root Cause를 제품 구조로 수정하는 것을 목표로 한다. 실행하지 않은 검증은 PASS로 기록하지 않는다.

## 2. 구현 Change Set

### 2.1 Runtime Control Plane

`cpf-core`에 topology-independent Public API/SPI와 Runtime Agent를 추가했다.

- target selector: environment/service/group/instance/label/zone/cell
- operationId + canonical request hash
- expectedVersion optimistic concurrency
- desired/actual version
- durable delivery + ACK
- lease + fencing token
- retry/backoff
- target immutable snapshot
- group/member + nested group cycle protection
- cancel / rollback request
- drift state
- immutable audit hash chain
- same JVM local owner / separate WAS HTTP Agent parity

ADM에는 운영자 Command API와 Runtime Agent API를 분리했다. Agent credential은 운영자 credential과 분리하고 lease/fencing 불일치 ACK를 거부한다.

### 2.2 Gateway Trust Boundary / Routing

- `com.cpf.core.common.header.*` 직접 import 제거, Public Header API 사용
- URI/Header execution ID mismatch fail-closed
- `validateHeader=false` bypass 제거
- GET/POST/PUT/PATCH/DELETE method route 일치 검증
- credential 존재만으로 인증 성공 처리 금지
- `CpfGatewayAuthenticationPort`로 검증된 Principal 분리
- Authorization/API key downstream forwarding 금지
- outbound header allowlist + CPF internal header regeneration
- 위험 route `auditReasonRequired` 실제 집행
- durable audit adapter 미구성 시 위험 route fail-closed
- target URI SSRF/CRLF/path traversal 방어
- empty route snapshot production startup fail-closed
- instance routing에 health / maintenance / drain / priority / weight / zone/cell 반영

### 2.3 Service Registry Control

Service/Endpoint/Instance에 Command API를 추가하고 Runtime Control Plane과 같은 운영 규약으로 통일했다.

- operationId request fingerprint
- reason / requestedBy mandatory
- rowVersion CAS
- Service / Endpoint / Instance save/delete
- child ownership delete protection
- DRAIN / DISABLE / RESUME 의미 분리
- DRAIN: `drain_yn=Y`, active 유지, 신규 라우팅만 차단
- DISABLE: active=N
- MariaDB 전용 `DATABASE()`, `ON DUPLICATE KEY`, `LIMIT ?`, `CURRENT_TIMESTAMP(3)` Repository SQL 제거
- JDBC metadata table detection
- circuit state update-first portable SQL

### 2.4 Durable Cache Refresh

기존 in-memory retry/drop-oldest 성격을 제거하고 DB event + consumer checkpoint 방식으로 강화했다.

- mutation transaction에서 refresh event DB insert mandatory
- event 저장 실패 시 원 변경과 함께 rollback 가능한 경계
- Runtime instance별 durable checkpoint
- 신규 instance는 full reload 후 event tail checkpoint
- 재기동 instance는 checkpoint 이후 이벤트 replay
- Code / Message / ResponseCode / Config cache 모두 required publish
- 3 Vendor runtime mapper canonical template 제공

### 2.5 BZA Login Atomic Idempotency / Exact Replay

- operationId + request fingerprint
- 동일 operationId / 다른 request payload => conflict
- 최초 SUCCESS 시 access/refresh token 결과 암호화 저장
- response-loss retry는 새 refresh session을 만들지 않고 최초 결과 exact replay
- login state/history/refresh/result가 하나의 transaction boundary에서 정합화
- expired replay는 새 operationId를 요구
- Runtime query contract / canonical template / 3 Vendor query pack 동시 수정

### 2.6 DB Canonical / Migration

Canonical schema를 Runtime Control Plane과 Cache checkpoint, Registry CAS, BZA login result 계약에 맞춰 확장했다.

주요 신규/확장 테이블:

- `cpf_runtime_version`
- `cpf_runtime_instance_group`
- `cpf_runtime_group_member`
- `cpf_runtime_instance_state`
- `cpf_control_operation`
- `cpf_runtime_change`
- `cpf_runtime_delivery`
- `cpf_runtime_change_audit`
- `cpf_cache_refresh_checkpoint`
- `cpf_service`, `cpf_service_endpoint`, `cpf_service_instance` rowVersion/placement/control fields
- `bza_login_operation` request fingerprint + encrypted exact result

Migration/rollback:

- MariaDB: V64/V65 + R64/R65, canonical source/runtime parity
- PostgreSQL: cpfDB V64 + bzaDB V64 + rollback
- Oracle: cpfDB V64 + bzaDB V64 + rollback

Checksum manifest는 과거 파일을 수정해서 맞추지 않는다. Overlay 적용 후 `generate-migration-checksums.ps1 -Apply`로 전체 current chain을 재생성한 뒤 `check-migration-checksums.ps1`로 tamper/drift를 검증한다.

### 2.7 DB Vendor 정책 정리

`CpfDatabaseVendor`와 `CpfSqlResourceResolver`, Canonical policy를 공식 3종으로 정리했다.

- MariaDB
- PostgreSQL
- Oracle

MySQL / MSSQL / SQLServer / H2는 공식 지원 Surface에서 제외한다.

## 3. 추가 Gate / Apply Script

- `cpf-tools/scripts/check-enterprise-qa-closing.ps1`
- `cpf-tools/scripts/cleanup-20260728-enterprise-qa.ps1`
- `cpf-tools/scripts/apply-20260728-enterprise-qa-closing.ps1`
- `cpf-tools/scripts/sync-cmn-cache-runtime-pack.ps1`
- `cpf-tools/scripts/sync-platform-runtime-query-packs.ps1` stale generated SQL cleanup 보강

적용 후 1차 명령:

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\apply-20260728-enterprise-qa-closing.ps1 -Root "C:\dev\projects\jck\202412_01_CPF"
```

## 4. 이 환경에서 수행한 검증

- 최신 master SHA 및 변경 대상 Source를 GitHub 정본에서 직접 확인
- QA Inventory/Scenario 수량 확인
- Overlay relative path 구조 확인
- Java source syntax smoke: container JDK 21 `javac -proc:none` 사용
  - project dependency가 없는 환경이므로 symbol/package error는 발생
  - Java parser 형태의 `illegal start`, `reached end of file while parsing`, `';' expected` 오류는 발견하지 못함
- Service Registry `saveInstance` SQL placeholder/argument 개수 수동 확인
- Canonical JSON parse 및 tableCount=162 확인
- 공식 Vendor policy marker 확인

위 syntax smoke는 Java 25/Gradle build PASS 증적이 아니다.

## 5. 최신 master 적용 후 반드시 직접 실행할 검증

다음은 구현 완료를 주장하기 위한 문서 체크가 아니라 실제 실행 Gate다. 최신 SHA에서 반드시 다시 실행한다.

```powershell
.\gradlew.bat verifyCpfFinalSourceGates --no-daemon
.\gradlew.bat clean test assemble --no-daemon
.\gradlew.bat qualityGate --no-daemon
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-enterprise-qa-closing.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-migration-checksums.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-db-vendor-pack-parity.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-platform-runtime-query-packs.ps1
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

Source gate가 실패하면 aggregate Gate를 약화시키지 말고 아래 순서로 최초 실패를 확정한다.

```powershell
.\gradlew.bat verifyVersionConsistency --no-daemon
.\gradlew.bat verifyBatStandaloneArchitecture --no-daemon
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-legacy-batch-migration.ps1
.\gradlew.bat :cpf-batch:verifyStandaloneArtifacts --no-daemon
```

그 뒤 ADM/BZA/Gateway/BAT/Generated Domain/3 DB Vendor Runtime scenario를 QA Execution Scenario 기준으로 수행하고 Evidence를 현재 Commit 기준으로 저장한다.

## 6. 완료 판정 금지 조건

아래 중 하나라도 남으면 CPF 전체 QA를 `완료`로 판정하지 않는다.

- Java 25 clean test/assemble 실패
- Source/Ownership/Boundary Gate 실패
- MariaDB/PostgreSQL/Oracle install/migration/rollback/reapply/runtime query 미검증 또는 실패
- Runtime Control Plane multi-instance ACK/fencing/drift/rollback 실패
- Gateway target-down/timeout/retry/UNKNOWN_RESULT/failover 실패
- BAT multi-instance/lease/takeover/checkpoint 실패
- Generated Domain create/build/DB/runtime/remove/regenerate 실패
- ADM/BZA permission/session/idempotency/data-safety 실제 Runtime 실패
- Evidence가 현재 Commit과 일치하지 않음
- checksum manifest를 verification 도중 자동 수정하여 green 처리

## 7. 정직한 현재 판정

이 Overlay는 위 Root Cause를 실제 Source/SQL/API에 반영한 **closing implementation change set**이다.

하지만 이 ChatGPT 실행 환경에는 프로젝트 전체 checkout, Java 25, PowerShell, MariaDB/PostgreSQL/Oracle Runtime, Browser, 다중 WAS 환경이 없으므로 1,415개 QA 항목 전체를 현재 SHA에서 실행 검증했다고 기록하지 않는다. Codex는 Overlay 적용 후 최신 master 전체 기준으로 실행 검증하고, 발견되는 결함을 검증 스킵으로 남기지 말고 같은 작업에서 수정해야 한다.
