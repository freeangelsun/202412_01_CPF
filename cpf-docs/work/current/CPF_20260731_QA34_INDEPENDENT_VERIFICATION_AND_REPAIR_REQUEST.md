# CPF QA34 독립 검증·수정 요청서

## 1. 목적

QA33에서 추가된 실제 Source를 최신 Git exact SHA에서 독립 검증하고, Static Marker Gate가 놓친
Build Tooling, Frontend exact-SHA, Security, DB, Runtime/Recovery 결함을 수정한다.

이 요청서는 이전 대화 없이 독립 수행할 수 있어야 한다.

## 2. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 시작 기준 Commit: `da491b3f5210e36efc63a7a627ad07c9481fac63`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 관련 독립 Review:
  `cpf-docs/work/review/CPF_20260731_QA33_INDEPENDENT_POST_PUSH_SOURCE_REVIEW.md`
- QA33 Canonical Counts:
  - Defect: 113
  - Remediation Requirement: 138
  - Mandatory Scenario: 414
  - Result Row: 552
- README와 `cpf-docs/guides/**` 내용 개편은 별도 작업으로 유지한다.
- 사용자 승인 없이 Commit, Push, Branch, Tag, PR, Release를 생성하지 않는다.

## 3. 시작 Gate

1. 최신 `origin/master`를 Fetch한다.
2. `HEAD == origin/master == da491b3f...`인지 기록한다.
3. Working Tree 변경을 임의 reset/restore/clean/stash하지 않는다.
4. 별도의 fresh clone 또는 clean worktree를 만든다.
5. `GRADLE_USER_HOME`, Maven Local Repository, npm cache를 빈 격리 경로로 설정한다.
6. 과거 `com.cpf.domain-conventions`, `com.cpf.build:cpf-gradle-plugin`, `com.cpf:cpf-bom` Artifact가
   로컬 Cache에 남아 결함을 가리지 못하게 한다.
7. 시작 Commit, OS, Java, Gradle, Node, npm, Browser, DB, Kafka 버전을 Evidence에 기록한다.

## 4. P0 수정 우선순위

### QA34-REQ-001 — Build Plugin Canonical Contract 단일화

다음 중 하나를 Architecture 정본으로 결정하고 Source·Consumer·Verifier를 모두 이관한다.

- 기존 계약 유지:
  - Plugin ID `com.cpf.domain-conventions`
  - Implementation `com.cpf.build:cpf-gradle-plugin`
- 새 계약으로 전환:
  - Plugin ID `com.cpf.platform-conventions`
  - Implementation Group/Artifact를 새 좌표로 통일

필수 범위:

- `settings.gradle`
- `cpf-tools/build/gradle-plugin/**`
- `cpf-member/build.gradle`
- Generator Template과 Golden Reference
- Local Artifact Federation Script
- Plugin Marker/POM
- Test
- Architecture/Decision/Current/Handover

완료 조건:

- Plugin Included Build를 Plugin Management에서 실제 해석
- 빈 Gradle User Home의 fresh clone에서 `gradlew help` 성공
- Generated Domain이 Canonical Plugin을 실제 적용
- 과거 Plugin Artifact가 없어도 성공
- Legacy Plugin ID가 남으면 명시적 Compatibility 정책과 제거 시점 존재

### QA34-REQ-002 — Platform BOM Canonical Coordinate 단일화

`cpf-platform-bom`과 `cpf-bom` 중 하나를 Canonical Artifact로 확정한다.

필수 확인:

- BOM Publication POM
- Root publish task
- Local/Staging/Internal repository
- `verify-local-artifact-propagation.ps1`
- Generated Domain dependency management
- Public Starter/Batch Contract constraints
- Version lock와 Upgrade compatibility

### QA34-REQ-003 — Java 25 Fresh Clone Build Closure

빈 Cache 환경에서 최소 다음을 실행한다.

```powershell
.\gradlew.bat help --no-daemon
.\gradlew.bat projects --no-daemon
.\gradlew.bat aggregateQualityBuild --no-daemon --max-workers=1
.\gradlew.bat publishCpfStagingPlatformArtifacts --no-daemon --max-workers=1
```

Included Build의 `check`, Plugin functional Test, BOM POM, Plugin Marker, Generated Domain Test/bootJar/bootWar를
실제 Java 25로 검증한다.

### QA34-REQ-004 — Post-Push exact-SHA Evidence

최종 수정 Commit이 Push된 뒤 그 exact SHA에서 다시 실행한다.

Evidence 필수 필드:

- `sourceSha`
- `resultSha`
- `sourceDirty=false`
- command
- profile/environment
- startedAt/finishedAt
- exitCode
- stdout/stderr/report/artifact SHA-256
- sanitized
- requirement/scenario/result row
- releaseEligible

`resultSha=null`, `BASE_SHA_PLUS_ROOT_OVERLAY`, 과거 Commit SHA는 Release Evidence로 금지한다.

## 5. Frontend/BFF

### QA34-REQ-005 — 실제 Backend OpenAPI Source

ADM/BZA OpenAPI Snapshot을 수작업 최소 JSON으로 유지하지 않는다.

- exact SHA Backend Runtime 또는 Canonical OpenAPI Export Task에서 생성
- 모든 Public Controller Operation 포함
- Request/Response/Error DTO와 Validation 포함
- Operation ID 중복 0
- Snapshot SHA와 Backend Source SHA 기록
- Generated Client 재생성 후 `git diff --exit-code`

### QA34-REQ-006 — Generated Client exact-SHA fail-closed

- `CPF_SOURCE_SHA` 생략 시 성공 금지 또는 Git HEAD 자동 해석
- `.cpf-openapi-source.json`과 `source-sha.json` Schema 통일
- Producer와 Verifier가 같은 Field를 사용
- stale Snapshot/Generated Client Fixture에서 반드시 실패
- bootstrap custom generator는 Orval 대체 경로로 사용하지 않거나 동등 계약을 증명

### QA34-REQ-007 — clean npm ci와 Lock 검증

ADM/BZA 각각 승인 Registry에서:

```powershell
npm ci
npm run generate:api
git diff --exit-code
npm run lint
npm run typecheck
npm run test
npm run build
```

Lock 검증은 Direct Entry 존재뿐 아니라 integrity, resolved policy, transitive graph, peer dependency,
install script policy, License/SBOM을 확인한다.

### QA34-REQ-008 — Browser E2E 강화

Chromium, Firefox, WebKit에서 다음을 실제 실행한다.

- Router Registry 전체 Route
- 인증 전/후
- 권한 있음/없음
- 직접 Deep Link
- API 401/403/409/429/500/503/timeout
- Loading/Empty/Error/Retry
- Paging/Sort/Search 결과
- 위험 조치 사유/확인/승인
- Keyboard/Focus/A11y
- Mobile overflow
- Session Fixation/Logout/Concurrent Session
- Credential/Session ID Browser 비노출

Test 내부 조건 때문에 assertion 없이 PASS하는 경로를 금지한다.

### QA34-REQ-009 — BFF Security Chain 독립 검증

- 모든 SecurityFilterChain Order와 Matcher 표 작성
- `/adm/**`, `/api/bza/**`, `/bza/**` 인증/권한 결정 Owner 명확화
- `permitAll`이 Controller/Method Security를 우회하지 않는지 검증
- 인증 없는 위험 API 100% 401/403
- CSRF/Origin/Session Fixation/Revocation/Concurrent Session 실제 Browser 검증
- Vault DB 장애와 key rotation/old key migration 검증

## 6. DB

### QA34-REQ-010 — 3 Vendor 실제 실행 Parity

Oracle/PostgreSQL/MariaDB 각각 clean DB와 기존 QA32 Schema에서 실행한다.

- Install
- V83, V86~V91 Upgrade
- 기존 데이터 Backfill
- Index/FK/Check Constraint
- Runtime Query
- Concurrent Claim/Fencing/Lease
- Selective Rollback
- Destructive Rollback 승인
- Drift Detection
- 재실행 실패/안전성
- 실행계획 및 핵심 Index 사용

Token Presence Gate는 보조 Static Gate로 이름을 변경한다.

## 7. Batch/Kafka/Scheduler

### QA34-REQ-011 — Remote Worker Completion/ACK

현재 기본 `DirectChannel` 동기 계약을 보호한다.

- Channel Bean 교체 시 fail-closed 또는 명시적 SPI 계약
- Handler 완료 전 ACK 금지
- Reply publish 실패
- Ledger complete 전/후 process kill
- duplicate delivery
- lease expiry와 owner fencing
- DLT/replay
- Manager restart와 late reply
- Queue capacity/backpressure

### QA34-REQ-012 — Batch Protocol Outbound Security

`PROTOCOL_ADAPTER`에 다음 정책을 추가한다.

- http/https scheme 제한
- Product Profile HTTPS
- Host/Port allowlist
- DNS/IP/CIDR 검증과 rebinding 방지
- Redirect 금지
- Request/Response size cap
- Header allowlist
- timeout
- credential masking
- result unknown/reconcile

### QA34-REQ-013 — Scheduler Durable Outbox

- multi-scheduler claim
- fencing token
- dispatch response loss
- retry/lease expiry
- duplicate launch
- unknown result reconcile
- process kill
- DB failover
- exact business date/time zone

## 8. Gateway

### QA34-REQ-014 — DNS/SSRF Connection Identity

Registry Hostname 검증과 실제 Socket Connection Address가 동일 정책을 만족하도록 한다.

- DNS rebinding Fixture
- multiple A/AAAA
- mixed private/public response
- IPv4-mapped IPv6
- metadata endpoints
- redirect
- stale registry
- target replacement between attempts
- connection pool 재사용

### QA34-REQ-015 — Retry/Streaming/Unknown Result

- idempotency key 전달 및 충돌
- 5xx body drain/close 후 retry
- streaming single attempt
- client disconnect
- async timeout/error/complete race
- response size cap
- audit/ledger spool DB failure
- multi-instance exact once completion

## 9. Deployment/Agent

### QA34-REQ-016 — Artifact Repository Network Trust

- DNS rebinding 방지
- TLS hostname/certificate
- allowed host/port
- proxy policy
- redirect denial
- digest/signature mismatch
- key rotation/revocation
- release sequence collision
- local path/symlink/hardlink
- disk full/atomic move failure
- Windows ACL/POSIX permission

### QA34-REQ-017 — Deployment Multi-instance State Machine

- Cell lock acquisition response loss
- lock store unavailable
- process kill at DRAIN/INSTALL/START/READINESS/ADMISSION
- Ledger write failure after side effect
- compensation partial/unknown
- reconcile approval and audit
- duplicate request hash collision
- rollback state swap partial failure
- minHealthy under concurrent deployment

## 10. Supply Chain/Artifact

### QA34-REQ-018 — Final Artifact Identity

- Source Commit
- Gradle/Maven Module/POM/JAR/WAR
- BOM
- Plugin Implementation/Marker
- Frontend bundle
- DB Pack
- SBOM
- ORT/Syft/Grype
- License/Notice
- Signature/Checksum
- Promotion Manifest

모든 Artifact를 동일 exact SHA와 Platform Version으로 연결한다.

## 11. Traceability와 Evidence

### QA34-REQ-019 — QA33 138/414/552 재판정

- Requirement 138개 전부 재판정
- Scenario 414개 전부 재판정
- Result Row 552개 전부 재판정
- Source-level 신규 결함을 기존 완료 행에 반영
- Requirement → Source/API/SQL/Test/Runtime/Evidence
- 구현 → Requirement/Owner/Consumer/Operation

### QA34-REQ-020 — Independent Review

최종 Push 후 별도 작업자가 다음을 수행한다.

- fresh clone
- empty cache
- exact SHA
- static gate 재실행
- Java25/Frontend/DB/Kafka/Browser/Agent Runtime
- Evidence Hash 재계산
- Repository Hygiene
- Source와 문서 양방향 추적

## 12. 완료 처리 금지 조건

다음 중 하나라도 존재하면 QA34 전체 완료 금지다.

- Plugin/BOM Coordinate 불일치
- fresh clone `gradlew help` 실패
- 과거 Local Artifact가 있어야 Build 성공
- stale OpenAPI/Generated Client SHA
- `CPF_SOURCE_SHA` 생략 시 stale client PASS
- `resultSha=null`
- overlay mode 결과만 존재
- Requirement verification 미검증
- 실제 DB/Browser/Kafka Runtime 미실행
- Test가 assertion 없이 PASS 가능
- Token/Marker 존재만으로 Runtime 완료 처리
- 민감정보가 Evidence에 존재
- Source·SQL·Test·문서·Evidence Drift
- 사용자 승인 없는 Git Write

## 13. 필수 산출물

- 수정 Source/SQL/Test/Config/Script
- QA34 Defect Register
- QA34 Requirement/Scenario/Result Matrix
- exact-SHA Evidence Index
- Runtime Evidence
- Independent Review
- Current Request/Handover/Continuity
- Root-relative Overlay
- Delete Manifest
- File SHA-256 Manifest
- 적용 전후 Git Status
- Commit/Push 수행 여부
