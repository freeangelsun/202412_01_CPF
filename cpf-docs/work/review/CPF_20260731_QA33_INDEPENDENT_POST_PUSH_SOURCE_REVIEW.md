# CPF QA33 독립 Post-Push Source Review

## 1. 검토 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 독립 검토 기준 Commit: `da491b3f5210e36efc63a7a627ad07c9481fac63`
- Commit Message: `20260731_09`
- 직전 `.gitignore` 정리 Commit: `e263526c1a15390206eeeeadf984a3ceb7145ecf`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA33 기준: 113 Defect, 138 Remediation Requirement, 414 Mandatory Scenario
- 검토 원칙: 보고서의 PASS/완료 문구보다 Git에 Push된 Source, 실제 Consumer, 설정, SQL, Test, Gate 구현과 Evidence Identity를 우선한다.

특정 Overlay ZIP Binary는 이번 독립 검토 환경에 제공되지 않았으므로 사용자가 제시한 ZIP SHA-256
`d0002b1d78ed0798bffd8aa0b8be24011894695e5d9a4be5b7659a7ee91c0ee8` 자체는 재계산하지 못했다.
다만 Push된 최신 `master`와 Commit된 Completion Manifest를 직접 검토했다. Manifest의 `payloadFileCount=310`,
`manifestFileCount=2`, `zipFileCount=312`는 사용자 보고와 일치한다.

## 2. 종합 판정

| 구분 | 독립 판정 | 근거 |
|---|---|---|
| QA33 Source 보강 | 부분 구현 | 실제 Java/TypeScript/SQL/Test/Script가 대량 추가되었으나 P0 Source 계약 불일치가 남아 있다. |
| QA33 Development Gate | 실패 | 자체 Gate는 PASS이나 overlay/marker/token 검증이 실제 Build·Consumer 결함을 놓쳤다. |
| QA33 Requirement 검증 | 미검증 | Commit된 Final Status 자체가 138/138 Requirement의 verification을 `미검증`으로 기록한다. |
| QA33 Result 검증 | 부분 구현 | 552건 중 151건 완료, 401건 미검증이다. |
| QA33 Release | 미검증 | Java 25, 3DB, Kafka, Browser, multi-instance, fault/recovery를 실행하지 않았다. |
| 현행 master의 fresh clone Build | 실패 | Golden Reference Domain의 Plugin ID와 복원된 Included Build Plugin ID가 불일치한다. |
| GA/Release 승인 | 실패 | P0 Build/Frontend/Evidence 결함과 Runtime Evidence 공백이 있다. |

QA33 자체 결과 문서가 Release 미검증을 숨기지는 않았다. 문제는 “남은 개발 3건”이 전체 QA 미해결 범위로
오해될 수 있다는 점이다. 정확한 상태는 다음과 같다.

- 개발 부분 구현 Requirement: 3건
- Verification 미검증 Requirement: 138건
- 개발 부분 구현 Scenario: 9건
- Verification 미검증 Scenario: 263건
- 개발 부분 구현 Result Row: 12건
- Verification 미검증 Result Row: 401건

## 3. 긍정적으로 확인된 사항

1. `cpf-tools/build/**` Source는 최신 Git에 실제 Commit되었고 `.gitignore`도 Source 추적과 산출물 제외를 구분한다.
2. BFF Credential은 AES-256-GCM, random IV, Handle AAD를 사용한다.
3. Product Profile에서 Secure Cookie, explicit HTTPS Origin, 256-bit key, JDBC Session/Vault Schema와 Index를 fail-closed로 확인한다.
4. Artifact State는 HMAC, owner-only permission/ACL, atomic move를 사용한다.
5. Gateway에는 trusted header boundary, request/response size cap, audit/ledger recovery spool이 추가되었다.
6. QA33 보고서는 Java 25, 실제 DB, Kafka, Browser, multi-instance 검증을 실행하지 않았다고 명시했다.
7. Kafka Worker 기본 Wiring은 `DirectChannel`이므로 Inbound Bridge의 `send()`는 기본 구성에서 동기 Handler 완료까지 반환한다.
   초기 검토에서 의심했던 “Channel enqueue 직후 무조건 완료”는 현재 기본 Wiring의 확정 결함으로 판정하지 않는다.
   단, Channel 대체 가능성 및 process-kill 경계는 실제 Runtime에서 검증해야 한다.

## 4. 확정 또는 재확인 필요 결함

### QA34-DF-001 — P0 Build Plugin ID와 Consumer 계약 불일치 — 실패

복원된 Plugin은 다음 계약이다.

- Plugin ID: `com.cpf.platform-conventions`
- Implementation Group: `com.cpf.gradle`
- Implementation Class: `com.cpf.gradle.CpfPlatformConventionPlugin`

하지만 Generator Golden Reference인 `cpf-member/build.gradle`은 다음 Plugin을 소비한다.

- `com.cpf.domain-conventions`

Fresh clone에서 과거 Local Maven Artifact가 없는 경우 해당 Plugin은 해석되지 않는다.
더구나 Included Build는 `pluginManagement { includeBuild(...) }`가 아니라 settings 하단의 일반
`includeBuild`로 등록되어 있어 Plugin DSL 공급 경로도 명확하지 않다.

**영향**

- `gradlew help`, `projects`, `qualityGate`, `aggregateQualityBuild`가 Project 구성 단계에서 실패할 수 있다.
- 기존 Local Repository에 남은 과거 Plugin이 결함을 가려 PC별 결과가 달라질 수 있다.
- Generated Domain 표준이 새 Convention Plugin과 연결되지 않는다.

### QA34-DF-002 — P0 BOM/Plugin Publication 좌표 불일치 — 실패

새 Build Tooling:

- BOM: `com.cpf:cpf-platform-bom`
- Plugin Implementation: `com.cpf.gradle:<project artifact>`
- Plugin Marker: `com.cpf.platform-conventions.gradle.plugin`

기존 Local Artifact Federation 검증 정본:

- BOM: `com.cpf:cpf-bom`
- Plugin Implementation: `com.cpf.build:cpf-gradle-plugin`
- Plugin Marker: `com.cpf.domain-conventions.gradle.plugin`

현재 두 계약은 동시에 만족할 수 없다. `verify-local-artifact-propagation.ps1`, Generator, Root publication,
소비자 Plugin ID, BOM import를 하나의 Canonical Coordinate로 통일해야 한다.

### QA34-DF-003 — P0 Post-Push exact-SHA Evidence 부재 — 실패

Canonical Development Evidence는 다음 Identity를 가진다.

- `sourceSha=c1f273f1ea4fafac6fd5d23bd837adfc38a04497`
- `resultSha=null`
- `verificationScope=BASE_SHA_PLUS_ROOT_OVERLAY`
- Java `21.0.10`, Java 25 unavailable
- `releaseEligible=false`

현재 Push된 Commit은 `da491b3f...`다. 따라서 이 Evidence는 최신 Git exact SHA 검증 결과가 아니며
Commit 후 Source Drift가 없는지 증명하지 못한다.

### QA34-DF-004 — P0 Frontend exact-SHA 검증이 fail-open — 실패

ADM/BZA OpenAPI Snapshot과 Generated Marker는 모두 개발 시작 SHA `c1f273...`를 보존한다.
`verify-generated-client.mjs`는 `CPF_SOURCE_SHA` 환경변수가 있을 때만 현재 Source SHA를 비교한다.
환경변수가 없으면 오래된 Snapshot과 Generated File끼리 Hash가 일치하는 것만으로 Build가 통과한다.

**필수 수정**

- `CPF_SOURCE_SHA` 생략 금지 또는 Git HEAD 자동 해석
- dirty worktree 처리 정책
- OpenAPI Snapshot 생성 Source/Artifact SHA 기록
- `sourceSha == git rev-parse HEAD`를 항상 fail-closed 검증
- Push 후 exact SHA에서 재생성 및 `git diff --exit-code`

### QA34-DF-005 — P0 Frontend OpenAPI/Generated Client가 제품 API 계약을 대표하지 못함 — 부분 구현

ADM Snapshot은 인증 조회 2개 Operation만 포함한다.
BZA Snapshot은 인증 관련 4개 Operation만 포함한다.
Response Schema는 대부분 `{}`, Request Body는 `additionalProperties=true`다.

Custom bootstrap generator는 URL, Method, generic body만 생성하며 다음을 제공하지 않는다.

- 실제 DTO Type
- Path/Query Parameter
- Error/Status Contract
- Validation Constraint
- Operation별 Request/Response Type

따라서 “Orval Generated Client 전환 완료”가 아니라 인증 일부 Consumer의 bootstrap 수준이다.

### QA34-DF-006 — P1 Frontend Closure Gate가 overlay mode에서 핵심 검증을 건너뜀 — 실패

`verify-cpf-qa33-frontend-closure.py`는 `--overlay`일 때 다음 검증을 실행하지 않는다.

- package-lock 실제 구조의 추가 검증
- `source-sha.json`과 Git HEAD 비교
- Generated Marker Hash Field 검증
- Playwright 3 Browser Config 검증

실제 QA33 Evidence는 `--overlay` PASS를 Development Gate 근거로 사용한다.
또한 non-overlay Gate는 `source-sha.json`에 `openApiSha256`, `generatedClientSha256`, `sanitized`를 기대하지만
실제 marker writer는 `source-sha.json`에 `sourceSha`만 기록한다. Gate와 Producer Schema가 서로 다르다.

### QA34-DF-007 — P1 Frontend E2E가 미실행일 뿐 아니라 false-pass 가능 — 부분 구현

현재 Route Quality Test에는 다음 공백이 있다.

- DOM에 보이는 Navigation Link만 수집하며 Route Registry 전체를 검증하지 않는다.
- 최대 40개 Route만 검사한다.
- API Failure Test는 실제 Request Injection이 없으면 assertion 없이 PASS한다.
- Unauthorized Test는 특정 data attribute가 0개인지 확인할 뿐, 실제 Restricted Route/API 거부를 확인하지 않는다.
- Search/Paging/Sort는 기능 결과가 아니라 Element 존재 중심이다.

### QA34-DF-008 — P1 “3DB Semantic Parity PASS”가 Token Presence 검사에 머묾 — 부분 구현

`verify-cpf-db-vendor-semantic-parity.py`는 SQL을 실행하지 않고 lower-case Text에서 Table/Column/Index Token이
존재하는지만 검사한다. 다음은 증명하지 못한다.

- Vendor SQL Syntax
- 실제 Constraint/Index 생성
- 기존 데이터 Backfill
- Lock/Transaction 동작
- Upgrade 순서
- Rollback 가능성
- Drift Detection
- 실행계획과 성능

따라서 PASS 명칭은 `Static Token Parity` 수준으로 낮춰야 한다.

### QA34-DF-009 — P0 QA33 Verification은 138건 전체 미검증 — 미검증

Commit된 `CPF_20260731_QA33_FINAL_STATUS.json`은 Requirement Verification을 138건 모두 `미검증`으로 기록한다.
“남은 3건”은 개발 부분 구현 수일 뿐 QA 미해결 수가 아니다.

### QA34-DF-010 — P0 BFF Security Filter Chain의 URL Authorization 공백 — 재확인 필요

`cpfBffSecurityFilterChain`은 `/adm/**`, `/api/bza/**`, `/bza/**`를 Matcher로 잡고
`authorizeHttpRequests(... anyRequest().permitAll())`을 적용한다.

다른 더 높은 우선순위 Chain 또는 모든 Controller의 Method Security가 완전하지 않으면 URL Authorization이
우회될 수 있다. 실제 FilterChain 선택 순서와 인증 전/후 Route 접근을 통합 테스트해야 한다.

### QA34-DF-011 — P1 Gateway DNS Rebinding/TOCTOU — 재확인 필요

Gateway Target Resolver는 Host를 DNS 조회해 Address를 검사한 뒤 원래 Hostname URI를 HTTP Client에 전달한다.
검사 시점과 접속 시점의 DNS Resolution이 분리되어 있어 DNS Rebinding/Address Change를 고정하지 못한다.
Connection 단계 Address Pinning 또는 신뢰 Registry Network Identity 검증이 필요하다.

### QA34-DF-012 — P1 Agent Artifact Download DNS Rebinding/TOCTOU — 재확인 필요

Artifact Installer도 `InetAddress.getAllByName()` 검사 후 동일 Hostname을 `HttpClient`가 다시 해석한다.
Artifact Signature가 Payload 변조는 막지만 내부 Metadata Endpoint 접근과 Network Policy 우회 가능성은 별개다.

### QA34-DF-013 — P1 Batch Protocol Adapter Outbound Policy 부재 — 부분 구현

`BatchRuntimeExecutorRegistry.executeProtocol()`은 승인 Definition의 URI를 바로 `HttpClient`에 전달한다.
Scheme, TLS, Host Allowlist, DNS/IP Range, Redirect, Response Size, Header Policy가 제품 수준으로 강제되지 않는다.
승인 데이터가 잘못되거나 탈취될 때 SSRF/내부 접근 경계가 없다.

### QA34-DF-014 — P0 Release Runtime 검증 공백 — 미검증

다음은 실행되지 않았다.

- Java 25 전체 Gradle 및 Included Build
- Fresh clone/empty local artifact repository
- ADM/BZA clean `npm ci`, typecheck, unit, production build
- Chromium/Firefox/WebKit
- Oracle/PostgreSQL/MariaDB install/upgrade/migration/rollback/drift
- Kafka Broker와 DLT/replay/backpressure/process-kill
- Gateway/Batch/Scheduler/Deployment/Agent multi-instance와 부분 실패
- Final Artifact SBOM/ORT/Syft/Grype와 Source SHA 연결

### QA34-DF-015 — P1 Convention Plugin Test가 핵심 계약을 검증하지 않음 — 부분 구현

현재 Plugin Test는 Java Toolchain이 25인지 한 건만 확인한다.
다음은 검증하지 않는다.

- Plugin ID/Marker Resolution
- Java `release=25`
- UTF-8와 `-parameters`
- Publication Coordinate
- LOCAL/REMOTE/OFFLINE
- Generated Domain 실제 Consumer
- Fresh Gradle User Home
- Java 25 Compiler 실제 실행

## 5. QA33 보고 대비 독립 결론

QA33 개발자는 실행하지 않은 Runtime 검증을 성공으로 조작하지 않았다는 점은 긍정적이다.
그러나 Static Development Gate가 실제 Build Graph, Coordinate, exact-SHA, Consumer, test false-pass를
충분히 검출하지 못했다.

따라서 독립 판정은 다음과 같다.

- `Requirement 개발 완료 135/138`: **재확인 필요**
- `Development Gate PASS`: **실패**
- `QA33 전체`: **부분 구현**
- `Release`: **미검증**
- `다음 작업`: QA34에서 P0 Source 결함을 먼저 수정하고, exact SHA fresh clone Runtime Evidence를 수행한다.

## 6. 보호할 성공 기능

다음 구현은 제거하지 말고 회귀 검증 대상으로 보호한다.

- `.gitignore`의 `cpf-tools/build` Source 추적과 nested output 제외
- AES-256-GCM Credential Vault와 Handle AAD
- Product Profile Session/Vault Readiness fail-closed
- Trusted Origin/CSRF/Cookie hardening
- Artifact State HMAC/atomic publish/permission
- Gateway trusted header와 body budget
- Audit/Ledger recovery spool
- Batch fencing/idempotency canonical digest 방향
- 3 Vendor Canonical Source 및 Migration/Runtime Query 구조
- 사용자 승인 없는 Commit/Push 금지
