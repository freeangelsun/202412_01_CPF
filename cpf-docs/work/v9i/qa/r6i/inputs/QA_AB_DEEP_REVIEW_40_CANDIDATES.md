# CPF QA A+B R6S12 통합 심층 검수 보고서

## ADM·EDU 집중 / Source·Architecture·Security·Runtime·Contract·Frontend·Release 교차검수

---

## 0. 문서 목적과 사용 방법

이 문서는 CPF R6S12 개발 결과를 **QA A(1) 영역 + QA B(2) 영역을 한 번 더 독립적으로 교차 검수하여 취합한 단일 QA 결과문서**다.

사용자 지시에 따라 이 결과는 여러 파일로 분산하지 않는다.
**통합 QA 세션에는 이 파일 하나만 전달**한다.

통합 QA는 이 보고서를 자체 QA1/A 결과와 대조해:

1. 기존 R5I 29 Finding과 신규 결함을 Crosswalk
2. 동일 Root Cause는 병합하되 세부 결함을 잃지 않음
3. 최종 재개발 Requirement 작성
4. 개발GPT → 자체검수 → Codex → QA 재검수 순환 지침 작성
5. 최종 원장/Repository 반영

을 별도로 수행한다.

본 QA 세션은 Repository Merge/Commit/Push/Delete를 수행하지 않았다.

---

# 1. QA 역할

## QA A / QA1

Core Source·Architecture·Security·Runtime Core:

- Module Ownership
- Public API/SPI/Internal
- Java/Spring Wiring
- Controller → Service → Owner → SPI → Provider → DB
- Approval/Security/Audit
- Idempotency/CAS/Unique/Concurrency
- DB3/Migration/Rollback
- Broker/Multi-instance/Split-WAS/Process Kill
- Backend Runtime

## QA B / QA2

Contract·Frontend·Operations·Release:

- Backend ↔ OpenAPI
- Generated Client
- ADM/BZA actual frontend consumer
- CRUD/상태/오류/권한/감사
- Accessibility/Responsive/Browser
- Starter/Profile/AutoConfiguration
- Generator/Generated Domain/Sample/EDU
- BOM/Publication/Artifact Catalog
- QA38/QA39/Verification Gate
- Evidence/Manifest/SHA/Fresh Clone/Release

## 이번 회차

사용자가 A영역까지 추가 검수하도록 지시했으므로:

- QA B 기존 심층검수 결과 재사용
- QA A 핵심 Source를 별도 심층검수
- ADM을 A+B 경계 기준으로 집중검수
- EDU 135 및 EDU-ADM 17개를 집중검수

하였다.

---

# 2. 정확한 검수 기준선

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- R5I Product QA basis: `e7cc9ada86c871214a20862779f2433bc46fea1b`
- R5I QA/docs baseline: `28f823a18eca859cebdbceb382029f595cdf490c`
- 현재 검수 exact SHA: **`77db10ad9aff44ee422795080fb2e96b364c9d65`**
- Commit message: `08_01`
- `28f823a... -> 77db10ad...`: 1 commit
- changed repository files: **118**
- current commit combined statuses: **0**
- current commit workflow runs: **0**

현재 R6S12 Package는 여전히:

- `basisSha=28f823a...`
- `resultCommitSha=null`
- `status=IMPLEMENTATION_PACKAGE_COMPLETE_RUNTIME_UNVERIFIED`

이다.

즉 Push된 current product SHA와 R6S12 Evidence가 재결속되지 않았다.

---

# 3. 검수 방식과 사실성 경계

## 3.1 실제 수행

- GitHub connector로 current exact SHA Source 직접 조회
- current commit diff 118 files 확인
- Approval Service/Repository/Controller/Owner SPI/Owner Adapters/DB Schema/Test 직접 리뷰
- ADM Route/Menu/Generated Route Contract/Permission/CRUD/API Consumer Source 리뷰
- ADM realtime SSE Source 리뷰
- BZA permission/build/release 경계 리뷰
- OpenAPI/generated consumer/release scripts 리뷰
- EDU canonical requirements 및 135 catalog 리뷰
- EDU registry/contributor/shared engine/repository/test framework 리뷰
- **EDU-ADM-01 \~ EDU-ADM-17 Handler 17개 전부 Source 본문 직접 리뷰**
- 대표 resource contract와 shared tests/doubles 리뷰
- R5I 29 및 FDEV-001\~025 재판정

## 3.2 실행하지 않았고 PASS라고 주장하지 않는 것

이 QA 세션의 로컬 환경은 GitHub checkout/runtime에 사용할 네트워크가 없으며 current SHA GitHub Actions run도 없다.

따라서 다음은 **NOT EXECUTED / 미검증**이다.

- Java 25
- Gradle 9.1 full clean build/test/publication
- ADM `npm ci && npm run verify`
- BZA `npm ci && npm run verify`
- Playwright actual authenticated browser
- Oracle live lifecycle
- PostgreSQL live lifecycle
- MariaDB live lifecycle
- DB3 actual concurrency
- Broker
- 2+ process/multi-instance
- Split-WAS
- Process Kill
- current SHA QA37/QA38/QA39/REV004 full runtime
- Codex independent review

Source 검토로 이들을 PASS 처리하지 않는다.

---

# 4. 최종 판정

## 전체

# **미통과**

## 신규/잔존 QA A+B Candidate Finding

- 총 **40건**
- P0 **31**
- P1 **8**
- P2 **1**

이 Finding들은 통합 QA가 기존 R5I Finding과 병합·Crosswalk하기 위한 후보다.

R6S12를 “거의 완료, Runtime만 남음”으로 판정할 수 없다.

이유는 Runtime 미검증 외에도 **현재 Source 자체의 실제 결함**이 확인됐기 때문이다.

---

# 5. 핵심 결론

## 5.1 ADM

ADM은 빈 화면/Mock만 있는 수준이 아니다.

실제:

- 63개 Route Registry
- Permission/Operator CRUD
- Message/Code/Config/Response/Notification CRUD
- Approval Engine
- Batch/Gateway Control
- SSE realtime
- Audit/Recovery/Incident
- Generated Operation Contract

등 상당한 제품 Source가 있다.

그러나 현재 exact SHA에서:

- Route 63 / sidebar menu 59
- 4개 route sidebar 누락
- 12개 route generated operation contract stale
- Integration Closure permission이 actual session에 미연결
- generic Workbench가 permission/Strict JSON 우회
- Approval action permission 미완성
- OpenAPI/generated stale
- actual Browser Release 미실행

이므로 “ADM 메뉴 전체가 상용 완료”로 판정할 수 없다.

## 5.2 EDU

사용자가 질문한 “130여개 넘지?”에 대한 정확한 답:

# **현재 canonical 요구 예제는 정확히 135개다.**

분포:

- DEV/온라인·공통·외부: 45
- BAT: 30
- ADM: 17
- BZA: 14
- Gateway: 14
- OPS 설치·운영·복구: 15
- 총: **135**

Registry도:

- reference-core 60 = DEV45 + OPS15
- reference-operations 17
- reference-backoffice 14
- reference-gateway 14
- reference-batch 30
- 합계 135

를 강제한다.

하지만 **135개 파일 수/Handler/Test 수가 맞다는 사실과 135개가 요구 의미대로 개발됐다는 것은 다르다.**

이번 QA에서는 특히 EDU-ADM 17개 Handler를 전부 Source로 읽었고:

- 17/17 Catalog role와 Handler role 불일치
- 여러 ID readOnly semantic 불일치
- 5종 Test가 mostly shared common test wrappers
- deterministic Test Consumer를 사용
- 다수 Handler가 시나리오 고유 업무 대신 template/common state-machine에 의존

함을 확인했다.

따라서 EDU 135는 **구조 수량은 맞지만 의미·실 runtime 완료는 미통과**다.

---

# 6. QA A — Approval/Security/Concurrency/Runtime Core 상세 리뷰

## 6.1 Approval 실제 호출 경로

현재 실제 Source 경로:

```
AdmApprovalController
→ AdmApprovalService
→ AdmApprovalRepository
→ DB approval request/participant/execution/history
→ AdmApprovalOwnerCommandPort resolver
→ Owner Adapter
→ 실제 Owner Port
```

Data Quality Correction:

```
ADM request
→ canonical active policy
→ participant snapshot
→ approval decision
→ APPROVED
→ reserveExecution()
→ APPROVED -> EXECUTING
→ adm_approval_execution RUNNING
→ reserved immutable envelope
→ DataQualityCorrectionApprovalOwnerCommandAdapter
→ HMAC proof
→ CpfDataQualityCorrectionPort.correctApproved()
→ provider CAS mutation
→ finishExecutionAndRequest()
```

이 경로는 Interface/DTO 수준이 아니라 실 구현이다.

### 긍정

- request idempotency key
- DB unique conflict convergence
- decision idempotency
- request optimistic version
- execution reservation
- snapshot integrity
- external owner call을 DB long transaction 밖에서 수행
- UNKNOWN 분류
- observation-only reconcile
- sanitized public detail

이 구현은 유지해야 한다.

---

## 6.2 Request/Decision DB Concurrency

PostgreSQL canonical schema 확인:

- `adm_approval_request.REQUEST_KEY` UNIQUE
- participant `(APPROVAL_REQUEST_ID,STEP_NO,OPERATOR_ID)` UNIQUE
- participant `IDEMPOTENCY_KEY` UNIQUE
- execution PK = approval request
- `COMMAND_REQUEST_ID` UNIQUE
- Request `VERSION_NO`
- Request status CAS

Source도:

```
insert
→ DataIntegrityViolationException
→ existing row read
→ fingerprint/equivalence verification
→ same result convergence
```

방향을 갖는다.

### 판정

**설계/Source는 substantive.**

그러나 Oracle/PostgreSQL/MariaDB actual race를 실행하지 않았으므로 Runtime PASS는 아니다.

---

## 6.3 P0 — Process Kill 후 RUNNING 고착

현재:

```
reserveExecution()
  request APPROVED -> EXECUTING
  execution RUNNING

Owner 호출
finishExecutionAndRequest()
```

JVM kill 위치:

```
reservation 완료
↓
[PROCESS KILL]
↓
Owner 호출 전 또는 Owner 호출 후
```

이면 Java catch/finalization이 실행되지 않는다.

재기동 후:

- `execute()`는 existing execution이 있으면 detail 반환
- `reconcile()`은 request/execution이 둘 다 UNKNOWN일 때만 실행
- stale RUNNING을 UNKNOWN으로 바꾸는 sweeper/lease/deadline recovery가 확인되지 않음

따라서:

```
EXECUTING/RUNNING
```

영구 고착 위험이 있다.

### 반드시 필요한 Test

1. instance A reserve 직후 kill
2. instance B restart
3. stale RUNNING detect
4. Owner side-effect 없으면 FAILED/NOT\_APPLIED
5. Owner side-effect 있으면 RECOVERED
6. 애매하면 UNKNOWN 유지
7. mutation 재실행 금지

---

## 6.4 P0 — Owner Registry fuzzy authorization

`BatchRuntimeApprovalOwnerCommandAdapter`:

- owner contains `"batch"`
- action contains `"RETRY"`, `"STOP"`, `"RUN"` 등

`CenterCutApprovalOwnerCommandAdapter`:

- action 문자열 contains 조합

즉 canonical 4D exact tuple이 아니다.

Negative Test도:

- self approval
- target mismatch
- snapshot mismatch
- owner UNKNOWN

위주이며 near-match string rejection matrix가 없다.

### 예시 필요한 Negative Corpus

```
owner = "not-batch-but-contains-batch"
action = "UNRELATED_RETRY_EXPORT"
action = "STOP_AUDIT_ONLY"
action = "RUN_UNAPPROVED"
CENTER_CUT_REPROCESS_DEBUG
```

모두 reject해야 한다.

---

## 6.5 Capability/HMAC

좋아진 점:

- HmacSHA256
- 32-byte key
- payload hash
- nonce
- approvedAt
- constant-time proof compare
- forged/tampered Test

그러나:

### 문제 A

`ApprovedCorrection`은 public record.

모든 Provider가 HMAC verifier를 사용하도록 SPI가 구조적으로 강제하지 않는다.

### 문제 B

HMAC `verify()`:

- approvedAt expiry 확인 없음
- nonce consumption 없음
- token replay ledger 없음

즉 cryptographic token 자체는 one-shot이 아니다.

현재 InMemory Provider는 상태/version CAS가 재실행을 대부분 막지만, 이는 Capability single-use 자체와 다르다.

---

## 6.6 Secret

`AdmIntegrationClosureProperties`:

- `approvalProofKeyBase64`
- `crypto.activeKeyBase64`

를 String property로 받는다.

prod profile은 feature disabled default라 기본 안전성은 좋아졌으나, feature를 켜면 raw environment/property key 입력 구조가 허용된다.

CPF 상용 보안 기준에서는 prod/stg에:

```
SecretRef
→ CpfSecretProvider/KMS
```

를 강제하고 raw secret property를 금지하는 편이 안전하다.

---

## 6.7 Policy Lifecycle

소스:

- duplicate policyCode/version insert 거부
- new version insert-only
- history table 생성
- reason/hash/operator 기록

긍정.

하지만 DB 자체:

- policy UPDATE를 물리적으로 금지하지 않음
- active effective range overlap 저장 방지 없음

따라서 여러 enabled policy가 같은 action/time에 존재하면:

```
findActivePolicy()
→ size > 1
→ DataIntegrityViolationException
```

으로 Approval action 전체가 fail-closed outage가 될 수 있다.

Fail-closed인 건 맞지만 **잘못된 정책 저장 자체를 막아야 한다.**

---

## 6.8 DB Finalization 장애

Owner side-effect 성공 후 DB가 장애나면:

```
finishExecutionAndRequest()
→ fail
→ markExecutionUnknown()
→ 같은 DB에 다시 write
```

이다.

DB outage가 계속되면 UNKNOWN write도 실패한다.

Process kill이면 catch 자체가 없다.

따라서 “catch가 있으므로 UNKNOWN 안전”이라고 판정하지 않는다.

필요:

- durable command id
- owner idempotency
- startup sweeper
- owner observation
- reconciliation
- stale RUNNING handling

---

## 6.9 BAT Remote Default

`BatApprovalOwnerCommandPort`에:

- default base URL `http://127.0.0.1:8180`
- default caller instance `adm-local-01`

이 있다.

승인된 운영 위험조치를 분리 WAS에서 실행하는 production path라면 missing config는 localhost fallback보다 **fail fast**가 맞다.

---

# 7. ADM 집중 검수 — Route 63 / Sidebar 59

현재 exact Source 기준:

- Route Registry: **63**
- Sidebar canonical menus: **59**
- 정상 sidebar 누락: **4**

누락:

1. featureFlags
2. openApiOperations
3. resiliencePolicies
4. integrationClosure

## 7.1 63 Route 전수 Contract 표

| #Routeroutes.ts expectedgeneratedSidebarContractQA Note |                      |    |    |    |      |                             |
| ------------------------------------------------------- | -------------------- | -- | -- | -- | ---- | --------------------------- |
| 1                                                       | dashboard            | 9  | 9  | 포함 | 일치   | registry/generated 기준 일치    |
| 2                                                       | topology             | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 3                                                       | capacity             | 6  | 6  | 포함 | 일치   | registry/generated 기준 일치    |
| 4                                                       | logs                 | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 5                                                       | transactionGroups    | 9  | 4  | 포함 | FAIL | expected 대비 5 operation 누락  |
| 6                                                       | transactions         | 5  | 4  | 포함 | FAIL | expected 대비 1 operation 누락  |
| 7                                                       | remoteLogs           | 9  | 5  | 포함 | FAIL | expected 대비 4 operation 누락  |
| 8                                                       | auditLogs            | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 9                                                       | logLevel             | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 10                                                      | logPolicies          | 13 | 6  | 포함 | FAIL | expected 대비 7 operation 누락  |
| 11                                                      | standardExecutions   | 2  | 2  | 포함 | 일치   | registry/generated 기준 일치    |
| 12                                                      | channelPolicy        | 6  | 6  | 포함 | 일치   | registry/generated 기준 일치    |
| 13                                                      | serviceRegistry      | 15 | 11 | 포함 | FAIL | expected 대비 4 operation 누락  |
| 14                                                      | runtimeControl       | 16 | 16 | 포함 | 일치   | registry/generated 기준 일치    |
| 15                                                      | maintenance          | 2  | 2  | 포함 | 일치   | registry/generated 기준 일치    |
| 16                                                      | cache                | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 17                                                      | configs              | 6  | 5  | 포함 | FAIL | expected 대비 1 operation 누락  |
| 18                                                      | responseCodes        | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 19                                                      | businessCalendar     | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 20                                                      | recoveryCenter       | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 21                                                      | incidents            | 22 | 12 | 포함 | FAIL | expected 대비 10 operation 누락 |
| 22                                                      | reliability          | 8  | 7  | 포함 | FAIL | expected 대비 1 operation 누락  |
| 23                                                      | notifications        | 11 | 9  | 포함 | FAIL | expected 대비 2 operation 누락  |
| 24                                                      | batch                | 12 | 6  | 포함 | FAIL | expected 대비 6 operation 누락  |
| 25                                                      | batch-overview       | 7  | 7  | 포함 | 일치   | registry/generated 기준 일치    |
| 26                                                      | batch-runtime        | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 27                                                      | batch-instances      | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 28                                                      | batch-scheduler      | 6  | 6  | 포함 | 일치   | registry/generated 기준 일치    |
| 29                                                      | batch-worker-pools   | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 30                                                      | batch-center-cut     | 9  | 9  | 포함 | 일치   | registry/generated 기준 일치    |
| 31                                                      | batch-agents         | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 32                                                      | batch-job-packs      | 8  | 8  | 포함 | 일치   | registry/generated 기준 일치    |
| 33                                                      | batch-executions     | 7  | 7  | 포함 | 일치   | registry/generated 기준 일치    |
| 34                                                      | batch-deployment     | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 35                                                      | batch-recovery       | 6  | 6  | 포함 | 일치   | registry/generated 기준 일치    |
| 36                                                      | batch-leases         | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 37                                                      | batch-alerts         | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 38                                                      | batch-audit          | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 39                                                      | workers              | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 40                                                      | downloads            | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 41                                                      | file-jobs            | 10 | 10 | 포함 | 일치   | registry/generated 기준 일치    |
| 42                                                      | messages             | 6  | 6  | 포함 | 일치   | registry/generated 기준 일치    |
| 43                                                      | codes                | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 44                                                      | gateway-dashboard    | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 45                                                      | gateway-servers      | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 46                                                      | gateway-groups       | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 47                                                      | gateway-routes       | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 48                                                      | gateway-security     | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 49                                                      | gateway-health       | 7  | 7  | 포함 | 일치   | registry/generated 기준 일치    |
| 50                                                      | gateway-transactions | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 51                                                      | gateway-log-policies | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 52                                                      | gateway-apply-status | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 53                                                      | permissions          | 26 | 15 | 포함 | FAIL | expected 대비 11 operation 누락 |
| 54                                                      | password             | 5  | 5  | 포함 | 일치   | registry/generated 기준 일치    |
| 55                                                      | security             | 6  | 6  | 포함 | 일치   | registry/generated 기준 일치    |
| 56                                                      | operators            | 12 | 9  | 포함 | FAIL | expected 대비 3 operation 누락  |
| 57                                                      | secrets              | 3  | 3  | 포함 | 일치   | registry/generated 기준 일치    |
| 58                                                      | approvals            | 12 | 12 | 포함 | 일치   | registry/generated 기준 일치    |
| 59                                                      | breakGlass           | 4  | 4  | 포함 | 일치   | registry/generated 기준 일치    |
| 60                                                      | featureFlags         | 7  | 7  | 누락 | 일치   | 정상 sidebar 탐색 경로 없음         |
| 61                                                      | openApiOperations    | 2  | 2  | 누락 | 일치   | 정상 sidebar 탐색 경로 없음         |
| 62                                                      | resiliencePolicies   | 5  | 5  | 누락 | 일치   | 정상 sidebar 탐색 경로 없음         |
| 63                                                      | integrationClosure   | 8  | 8  | 누락 | 일치   | 정상 sidebar 탐색 경로 없음         |

## 7.2 Generated Contract mismatch 12개

정확한 mismatch route:

1. transactionGroups 9 → 4
2. transactions 5 → 4
3. remoteLogs 9 → 5
4. logPolicies 13 → 6
5. serviceRegistry 15 → 11
6. configs 6 → 5
7. incidents 22 → 12
8. reliability 8 → 7
9. notifications 11 → 9
10. batch 12 → 6
11. permissions 26 → 15
12. operators 12 → 9

현재 `write-route-operation-contract.mjs`는 explicit expectedOperationIds를 generated contract에 포함하도록 되어 있으므로 이 차이는 단순 설계 선택이 아니라 **tracked generated artifact stale**로 본다.

---

# 8. ADM 메뉴 기능 — CRUD/운영/Realtime

## 8.1 Permissions

실제 UI:

- 조회
- 메뉴 권한 저장
- 버튼 권한 저장
- API 권한 저장
- 역할 등록/수정
- 메뉴 등록/수정
- 버튼 등록/수정
- API permission 등록/수정

실제 API 호출 Source 존재.

따라서 “폼만 있는 흉내”는 아니다.

그러나:

- expected operations 26 / generated contract 15
- browser actual role matrix current SHA 미실행
- broad generic facade 소비
- action permission 전체 정합성 current release 미증명

**판정: 실구현 존재 / contract+runtime 미통과**

## 8.2 Operators/Sessions/Security

실제 Source 확인:

- operator list
- create
- activate/status
- raw contact permission path
- password change/reset
- unlock
- sessions list
- revoke
- expired cleanup
- MFA register/verify

실행 결과 유실 시 create operator operationId를 재조회하는 보완도 존재.

그러나:

- generated route contract 12 expected / 9 generated
- browser actual current SHA 없음

**판정: 실구현 존재 / generated/runtime 미통과**

## 8.3 Messages/Codes/Configs/Response/Notifications

`referenceMethods.ts`에서 generated Orval operation을 실제 호출하는 경로가 확인된다.

긍정:

- Message CRUD
- Message test/render
- Code CRUD
- Config CRUD
- Response Code CRUD
- Notification template/history/retry/cancel 등

하지만:

- configs 6/5 generated mismatch
- notifications 11/9 mismatch
- current npm verify/browser evidence 없음

## 8.4 Gateway Realtime

`GatewayOperationsPage.vue`:

- credentialed EventSource
- gateway operations stream
- message handler
- status update
- disconnect/fallback

실 Source가 존재한다.

즉 realtime UI가 단순 문구는 아니다.

그러나 actual WAS/browser/SSE current SHA evidence가 없으므로 Runtime PASS는 아니다.

## 8.5 Approval

Route contract 12/12이고 Backend 엔진도 substantive다.

하지만 action-level UI permission, process kill recovery, owner exact tuple 문제가 있어 **P0 미통과**.

## 8.6 Batch

세부 child route 대부분 generated contract parity가 맞지만 umbrella `batch`는 12/6 stale다.

Approval Owner의 fuzzy action tuple 및 distributed runtime 미검증도 존재한다.

## 8.7 Incident/Recovery/Reliability

Route 자체와 operational APIs는 대규모로 존재하지만:

- incidents 22/12
- reliability 8/7

stale contract가 확인됐다.

실 Browser/partial failure/recovery runtime 없이 완료 처리하지 않는다.

---

# 9. ADM Permission 실제 Session 경로

실제 로그인:

```
/adm/api/auth/me
→ operator
→ menus
→ buttonIds
→ AdmSessionStore
```

Router/menu path는 이 server session을 소비한다.

그러나 Integration Closure는 별도 DOM dataset을 permission source로 사용.

Test 역시:

```
page.addInitScript()
→ documentElement.dataset.admPermissions
```

를 직접 주입한다.

즉:

```
Synthetic Test PASS
!=
Actual Session Permission PASS
```

이다.

---

# 10. ADM OpenAPI / Generated Client / HTTP

## 10.1 Backend

R6 Backend 방향:

- expectedVersion >= 1
- idempotency key 8..128
- reason 8..500
- 201 new
- 200 replay
- 409 conflict
- 422 validation

## 10.2 문제

Tracked OpenAPI가 stale.

Generation script가 tracked OpenAPI를 in-place enrich.

Generated consumer 일부는 manual interface + generic body unknown.

Response status가 upper payload facade에서 없어져 UI가 201/200을 구분하기 어렵다.

## 10.3 Release Acceptance

반드시:

```
Backend validation
==
runtime OpenAPI
==
release OpenAPI
==
Orval model
==
actual consumer types
==
UI validation/error/status
```

이어야 한다.

---

# 11. EDU 정확한 135개 검산

Canonical EDU 요구 문서는 135개를 확정한다.

| Family          |   Count |
| --------------- | ------: |
| DEV / 온라인·공통·외부 |      45 |
| BAT             |      30 |
| ADM             |      17 |
| BZA             |      14 |
| Gateway         |      14 |
| OPS             |      15 |
| **Total**       | **135** |

`EduCapabilityRegistry` contributor 강제:

| Contributor          |   Count |
| -------------------- | ------: |
| reference-core       |      60 |
| reference-operations |      17 |
| reference-backoffice |      14 |
| reference-gateway    |      14 |
| reference-batch      |      30 |
| **Total**            | **135** |

`reference-core 60 = DEV45 + OPS15`.

`ReferenceOperationsCapabilityContributor`는 `EduAdm01Handler`부터 `EduAdm17Handler`까지 17개를 직접 등록한다.

### 수량 판정

**135 수량/Registry 구조는 확인됨.**

### 완료 판정

**135 의미 구현/Target Runtime은 미통과.**

---

# 12. QA37 EDU Gate 리뷰

현재 Gate의 좋은 점:

- exact 135
- family count
- unique ID
- source path
- resource contract
- 5 ID-specific Test file
- TODO/FIXME/placeholder 방지
- product-module independence
- feature isolation
- DB3 file parity
- consumer binding 존재

이는 과거 marker-only EDU보다 훨씬 낫다.

하지만 Gate가 놓치는 것:

1. Catalog requiredRole == Handler requiredRole 비교 없음
2. Catalog readOnly == Handler readOnly 비교 없음
3. Catalog business states/consumer semantics의 executable equivalence 부족
4. 5 Test가 shared abstract tests인지 구별하지 않음
5. Real Product Consumer인지 Test Double인지 구별하지 않음
6. compile self-test가 production JDBC repository/runtime을 완전히 대체하지 못함
7. Browser requiredVerification을 실제 Browser Test로 강제하지 않음
8. “ID/title/문자열 존재”와 “업무 의미 구현” 사이를 판별하지 못함

따라서 현재 Gate PASS가 있더라도:

```
EDU Source Closure
```

이지:

```
135개 상용 EDU Runtime Completion
```

이 아니다.

---

# 13. EDU Test 구조

각 EDU는 5개 test file을 가진다.

그러나 예:

```
EduAdm04UnitTest
extends AbstractManualEduUnitTest
```

이고 실제 테스트 로직은 공통 base에 있다.

공통 Unit은:

- definition non-empty
- role missing reject
- invalid required field reject

등을 본다.

공통 Integration은:

- shared execution engine
- FileEduOperationRepository
- audit/target persistence
- restart reload

를 본다.

공통 Concurrency는 같은 idempotency key로 8 thread를 돌린다.

하지만 TestSupport는:

```
FileEduOperationRepository
+
TestEduBusinessConsumers.registry()
```

를 사용한다.

`TestEduBusinessConsumers`는 8 consumer type 모두 deterministic double이다.

즉:

```
675 test files
!=
675 scenario-specific actual integration tests
```

이다.

---

# 14. EDU Shared Runtime 긍정 요소

`EduExecutionService`는 substantive하다.

- idempotency
- version CAS
- lease/fencing
- target records
- outbox
- retry
- partial result
- UNKNOWN
- reconcile
- compensation
- audit

`JdbcEduOperationRepository`도:

- durable operation
- unique/idempotency
- optimistic update
- targets
- outbox
- audit
- lease

를 갖는다.

따라서 EDU 전체가 marker mock은 아니다.

문제는 **135개의 각 교육 의미가 이 공통 엔진 위에 실제로 구현됐느냐**다.

---

# 15. EDU-ADM 17개 Source 전수 리뷰

아래 17개 Handler는 이번 QA에서 **전부 Source 본문을 직접 확인**했다.

| ID         | Title                               | Catalog Role       | Handler Role                       | Role | Catalog RO | Handler RO | RO   | Consumer      | QA 결과                                                                                                                                |
| ---------- | ----------------------------------- | ------------------ | ---------------------------------- | ---- | ---------- | ---------- | ---- | ------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| EDU-ADM-01 | 기존 ADM 기능 재사용 판단                    | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | True       | False      | FAIL | JDBC\_COMMAND | 기존 ADM 기능 재사용 판단 로직/중복 메뉴 판정이 구체 구현되지 않고 공통 조회·상태머신으로 수렴. Catalog readOnly와 Handler readOnly 불일치.                                    |
| EDU-ADM-02 | 고객 업무 조회 연동                         | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | True       | True       | 일치   | JDBC\_QUERY   | 조회 Consumer는 선언됐으나 조직 Scope/부분 데이터/Stale Version을 개별 업무 규칙으로 구현하지 않고 공통 엔진에 의존.                                                      |
| EDU-ADM-03 | 안전한 운영 조치                           | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | VERSION\_CHECK step은 있으나 입력에 명시적 resource expectedVersion이 없고 허용상태/응답유실 업무 규칙이 개별 구현되지 않음.                                           |
| EDU-ADM-04 | 승인 필요한 위험 조치                        | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | approvalId/policyId 필드와 APPROVAL step만 존재. 자기승인·만료·범위·대상 변경을 실제 승인정책/SoD 로직으로 검증하지 않음.                                               |
| EDU-ADM-05 | 비동기 작업·응답 유실                        | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | externalEffect/OUTBOX failure point는 공통 엔진에 선언되지만 실제 Consumer는 JDBC\_COMMAND. 실 비동기 접수/Polling/응답유실 연동이 예제별로 구현되지 않음.                |
| EDU-ADM-06 | 부분 성공·대상별 복구                        | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | targetCount가 required field가 아닌데 기본 3개 managed-target을 생성. 실제 입력 대상 목록과 성공/실패 대상별 재처리 계약이 없음.                                        |
| EDU-ADM-07 | 고객 전용 화면 추가의 마지막 선택                 | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | Frontend/Backend 계약·Browser 요구가 있으나 Handler는 generic JDBC command 예제. 실제 전용 화면/route/client 구현을 대표하지 못함.                             |
| EDU-ADM-08 | 권한·데이터 범위·Masking·사유 입력 연동          | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | Resource contract는 browser role matrix/IDOR/masking을 요구하지만 Handler는 공통 role/dataScope/nonblank validation 위주. 실제 masking/IDOR 정책 없음. |
| EDU-ADM-09 | Expected Version 충돌 화면·재조회·재적용      | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | True       | FAIL | JDBC\_QUERY   | Catalog는 변경형인데 Handler readOnly=true. requiredFields에 expectedVersion이 없고 409/diff/reload/resubmit/browser 흐름이 실제 구현되지 않음.           |
| EDU-ADM-10 | 대상 일괄 조치·부분 성공·결과 파일                | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | targetIds/expectedVersions를 target plan에 쓰지 않고 없는 partitionCount/gridSize를 읽어 기본 4개 pseudo partition 생성. 대상별 결과 의미 위반.               |
| EDU-ADM-11 | 설정·기능전환·유지보수 창 운영                   | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | maintenanceWindow/configVersion/targets는 존재하지만 창 검증, checksum drift, LKG rollback, restart-required 처리가 예제 고유 로직으로 없음.               |
| EDU-ADM-12 | Incident·Recovery Center 종단간 복구     | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | incidentId/transactionIds를 받지만 Incident workflow/owner 교대/복구 후 재발/증적 검증은 구현되지 않고 pseudo partition target을 사용.                        |
| EDU-ADM-13 | 감사 증적·다운로드·승인 반출                    | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | file/hash 기본 validation은 있으나 async export, 승인 반출, 개인정보 masking, expiry, download audit가 실제 예제 고유 로직으로 닫히지 않음.                        |
| EDU-ADM-14 | Topology·Health·Capacity Drill-down | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | True       | False      | FAIL | JDBC\_COMMAND | Catalog readOnly=true인데 Handler=false, Consumer도 JDBC\_COMMAND. 실제 health freshness/instance churn/metric gap/trace drill-down이 없음.  |
| EDU-ADM-15 | Log·Trace·Transaction 상관 검색         | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | True       | False      | FAIL | JDBC\_COMMAND | Catalog readOnly=true인데 Handler=false, Consumer도 JDBC\_COMMAND. 실제 로그/trace 상관검색·partial warning·masking/download limit이 없음.         |
| EDU-ADM-16 | 알림 Acknowledge·Escalation·교대 인계     | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | externalEffect/OUTBOX을 선언하나 실제 notification consumer/timer/snooze/escalation/ownership audit 고유 구현이 없음.                              |
| EDU-ADM-17 | Browser 세션 만료·재로그인·위험 조치 안전성        | CPF\_ADM\_OPERATOR | CPF\_REFERENCE\_PLATFORM\_OPERATOR | FAIL | False      | False      | 일치   | JDBC\_COMMAND | Session expiry/multi-tab/CSRF/no-auto-replay 요구인데 Browser consumer가 아니라 generic JDBC command handler. 실제 브라우저 재인증 흐름 없음.             |

---

# 16. EDU-ADM 공통 구조 결함

## 16.1 Role

Catalog:

```
CPF_ADM_OPERATOR
```

Handler 17개:

```
CPF_REFERENCE_PLATFORM_OPERATOR
```

Tests는 Handler definition role을 사용하므로 mismatch를 발견하지 못한다.

즉 Catalog/Manual의 role contract를 실제 handler가 따르지 않아도 Test가 green이 될 수 있다.

## 16.2 Pseudo Target Pattern

다수 Handler:

```
partitionCount
gridSize
targetCount
```

가 requiredFields에 없어도 default로 pseudo target을 만든다.

그 결과 실제 요구 입력:

- targetIds
- transactionIds
- serviceId/instanceId
- alertId
- sessionId

를 target-level recovery 모델이 직접 사용하지 않는 경우가 있다.

## 16.3 “필수 필드·권한·범위 검증은 공통 엔진”

여러 Handler의 `validateBusinessInput()`이 사실상:

```
super.validateBusinessInput(command)
```

뿐이다.

따라서 제목상 고유 의미:

- 승인정책
- IDOR
- Masking
- 유지보수 Window
- Incident Workflow
- Log/Trace Correlation
- Session Expiry
- CSRF

가 executable rule이 아니라 문자열 requirement로만 남는다.

---

# 17. EDU-ADM 개별 핵심 결함

## EDU-ADM-01

제목: 기존 ADM 기능 재사용 판단.

요구:

- 기존 ADM 기능으로 해결 가능 여부
- 중복 menu 금지
- 고객 DB 직접 연결 금지
- Browser/Backend contract

실제:

- generic JDBC command
- businessId/approvalId
- 공통 state machine
- reuse decision algorithm 없음
- Catalog readOnly true / Handler false

**FAIL**

## EDU-ADM-02

고객 업무 조회 예제는 JDBC\_QUERY라는 방향은 맞다.

하지만:

- 조직 범위
- partial data
- stale version
- same-JVM/remote 차이

가 고유 구현되지 않았다.

**부분 구현 / 미검증**

## EDU-ADM-03

안전 운영조치인데:

- required input이 businessId/approvalId뿐
- explicit resource version 없음
- allowed state rule 없음

공통 VERSION\_CHECK step만 존재.

**FAIL**

## EDU-ADM-04

승인 예제인데:

- approvalPolicyId를 받음
- APPROVAL step 존재

하지만:

- SoD
- policy binding
- expiry
- target drift
- approval participant

실제 모델이 없다.

**FAIL**

## EDU-ADM-05

비동기/응답유실 예제인데 Consumer가 generic JDBC\_COMMAND.

OUTBOX/failure point는 shared engine이 제공.

실제:

- async accept
- poll
- late response
- duplicate operation

업무 연동이 없다.

**FAIL**

## EDU-ADM-06

부분 성공 예제인데 실제 target list input이 없고 default 3 managed targets.

**FAIL**

## EDU-ADM-07

“고객 전용 화면”인데 Frontend/Browser artifact가 아니라 backend generic handler.

**FAIL**

## EDU-ADM-08

Masking/IDOR/browser role matrix 요구.

실제:

- permission nonblank
- generic role
- generic dataScope

수준.

**P0 FAIL**

## EDU-ADM-09

Expected Version 충돌 화면인데:

- expectedVersion required input 없음
- Handler readOnly=true
- diff/reload/resubmit 없음
- actual browser 없음

**P0 FAIL**

## EDU-ADM-10

Bulk target인데 `targetIds`를 targetKeys에 사용하지 않음.

default pseudo 4 partitions.

**P0 FAIL**

## EDU-ADM-11

Maintenance window/config rollout인데:

- window parse/now compare 없음
- target rollout 없음
- checksum drift 없음
- LKG rollback 없음

**FAIL**

## EDU-ADM-12

Incident/Recovery인데:

- incident lifecycle transition rule 없음
- owner handover 없음
- evidence completeness 없음
- reopen 없음

**FAIL**

## EDU-ADM-13

Evidence/export인데 file/hash validation 일부만 있음.

- async export
- approval release
- PII masking
- expiry
- download audit

없음.

**FAIL**

## EDU-ADM-14

Topology read scenario인데:

- Handler readOnly=false
- JDBC\_COMMAND
- freshness/instance churn/metric gap/clock skew 계산 없음

**FAIL**

## EDU-ADM-15

Log/Trace query인데:

- Handler readOnly=false
- JDBC\_COMMAND
- correlation/search/masking/partial warning 없음

**FAIL**

## EDU-ADM-16

Notification ACK/escalation인데:

- generic JDBC command
- actual notification destination/send
- ACK owner
- snooze timer
- escalation timer

없음.

**FAIL**

## EDU-ADM-17

Browser Session scenario인데:

- browser consumer 없음
- session expiration injection 없음
- re-login 없음
- multi-tab 없음
- CSRF 없음
- no-auto-replay actual browser behavior 없음

**P0 FAIL**

---

# 18. ADM ↔ EDU “연동”의 올바른 해석

CPF EDU 설계는 `cpf-reference`가 `cpf-admin`을 직접 import하지 않도록 한다.

이 자체는 결함이 아니다.

교육 예제는:

- product module independent
- generated domain independent
- refDB owned

이어야 한다.

따라서 ADM↔EDU 연동 완료 기준은:

```
ADM 실제 제품 기능 의미
↕
EDU 독립 reference contract
↕
동일한 권한/상태/오류/복구/감사/Browser 경험
```

이어야 한다.

현재는 role/readOnly/semantic behavior가 다르므로 **행동 계약 정합성에서 미통과**다.

---

# 19. A+B Candidate Finding 40건

| Finding   | Priority | Title                                                                  | Primary Target                                                                                                                       | Required Rework                                                                                                           |
| --------- | -------- | ---------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ | ------------------------------------------------------------------------------------------------------------------------- |
| AB-R6-001 | P0       | Current result SHA와 R6S12 Evidence provenance 미결속                      | cpf-docs/work/v9i/dev/r6s12/\*\*                                                                                                     | current exact SHA에서 전 검증 재실행, manifest/evidence/result SHA 재결속, clean tree 증거 생성.                                         |
| AB-R6-002 | P0       | Current master Push에 Release Workflow 실행 없음                            | .github/workflows/cpf-r6-release-gates.yml                                                                                           | master result SHA가 required checks와 자동 결속되도록 release policy를 정식화.                                                         |
| AB-R6-003 | P0       | Release Runner가 clean exact-SHA qualification을 증명하지 못함                 | cpf-tools/verification/final-dev/run-r6-release-gates.ps1                                                                            | release mode에서 clean tree, Java25/Gradle9.1, ADM/BZA verify, browser, DB3, distributed, QA38/39/REV004를 non-optional로 강제. |
| AB-R6-004 | P0       | Playwright Release Gate 실제 Product wiring 미완성                          | cpf-admin/frontend/playwright.config.ts;.github/workflows/cpf-r6-release-gates.yml                                                   | 실제 backend/frontend/auth session으로 release E2E 실행. synthetic DOM injection 금지.                                            |
| AB-R6-005 | P0       | Integration Closure UI operation permission이 실제 ADM Session과 연결되지 않음   | cpf-admin/frontend/src/features/integration-closure/IntegrationClosurePage.vue;cpf-admin/frontend/e2e/integration-closure-r6.spec.ts | AdmSessionStore/buttonIds를 reactive single source로 사용하고 실제 session role matrix로 browser test.                             |
| AB-R6-006 | P0       | ADM generic RouteOperationWorkbench가 전용 화면의 permission/Strict JSON을 우회 | cpf-admin/frontend/src/components/RouteOperationWorkbench.vue;cpf-admin/frontend/src/App.vue                                         | critical mutation 제거 또는 route/action permission, schema validation, reason/approval/audit를 동일하게 강제.                       |
| AB-R6-007 | P0       | Approval 위험 Operation UI에 action-level permission 미적용                  | cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue                                                                          | route read와 action permission을 분리하고 모든 위험 버튼을 server button/action grant에 결속.                                             |
| AB-R6-008 | P1       | ADM 63 Route 중 4개가 59개 sidebar canonical menu에서 누락                     | cpf-admin/frontend/src/app/routes.ts;cpf-admin/frontend/src/state/createAdmState.ts                                                  | route/menu registry 단일 정본화, 정상 로그인 후 sidebar discoverability browser test.                                                |
| AB-R6-009 | P0       | ADM generated route-operation contract가 12 Route에서 stale               | cpf-admin/frontend/src/app/routes.ts;cpf-admin/frontend/src/generated/adm-route-operation-contract.ts                                | current source OpenAPI로 regenerate 후 63/63 exact set equality 및 git diff --exit-code.                                     |
| AB-R6-010 | P0       | Backend validation과 committed ADM OpenAPI/Generated artifact drift     | cpf-admin/frontend/openapi/cpf-openapi.json;cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs                               | runtime/controller OpenAPI 정본 → generated client 전부 재생성, clean regeneration gate.                                         |
| AB-R6-011 | P1       | Frontend enrich script가 validation/security response 계약의 제2 정본 역할      | cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs                                                                           | controller/runtime schema를 정본으로 하고 frontend script는 검증/annotation 범위로 축소.                                                 |
| AB-R6-012 | P1       | Generated Client가 high-risk 실제 Consumer를 compile-time으로 충분히 구속하지 않음    | cpf-admin/frontend/src/shared/cpfApi.ts;integrationClosureApi.ts;orval-mutator.ts                                                    | high-risk API는 generated request/response/status를 actual consumer에서 직접 사용.                                                |
| AB-R6-013 | P0       | Operation consumer Gate False Green                                    | cpf-admin/frontend/scripts/verify-operation-consumer.mjs;cpf-biz-admin/frontend/scripts/verify-operation-consumer.mjs                | 실제 component callsite/typed generated API 호출을 증명하고 registry-only 소비를 금지.                                                  |
| AB-R6-014 | P0       | R6 Behavior Mutation Gate가 실제 mutation execution이 아닌 tautology         | cpf-tools/verification/final-dev/verify-r6-behavior-contracts.py                                                                     | temp checkout/copy에 real mutation 적용 → 실제 gate 실행 → nonzero exit 확인.                                                      |
| AB-R6-015 | P1       | DB Runner Security Test가 process behavior가 아니라 문자열 검사 중심               | cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1;verify-db3-runner-contract.py                                     | dummy child로 secret inheritance/timeout/tree-kill/stdout redaction 실제 runtime test.                                       |
| AB-R6-016 | P0       | BZA Workbench permission code가 canonical manifest와 불일치                 | cpf-biz-admin/frontend/src/components/RouteOperationWorkbench.vue;cpf-tools/db/metadata/bza-permission-manifest.json                 | canonical actionRules 기반 operation permission으로 교체.                                                                       |
| AB-R6-017 | P0       | BZA Release Integrity 미완성                                              | cpf-biz-admin/build.gradle;cpf-biz-admin/frontend/\*\*                                                                               | ADM/BZA 동일한 generation reproducibility + BZA browser release gate.                                                        |
| AB-R6-018 | P2       | Frontend idempotency storage 정책/표현 불일치                                 | integrationClosureIdempotency.ts/.test.ts                                                                                            | pending retry lifetime/rotation 정책을 명시하고 실제 storage semantics와 UI 문구/Test를 일치.                                            |
| AB-R6-019 | P0       | Approval Owner Registry 4D binding이 exact tuple이 아니라 fuzzy matching    | BatchRuntimeApprovalOwnerCommandAdapter.java;CenterCutApprovalOwnerCommandAdapter.java                                               | canonical exact allowlist/enum table로 ownerModule+ownerCommand+actionType+targetType 완전일치. near-match negative matrix.    |
| AB-R6-020 | P0       | Process Kill 후 Approval EXECUTING/RUNNING 고착 경로                        | AdmApprovalService.java;AdmApprovalRepository.java;AdmApprovalController.java                                                        | stale RUNNING lease/deadline+sweeper/reconcile entry를 구현하고 owner 호출 전/후 kill을 2-instance에서 검증.                            |
| AB-R6-021 | P0       | Public DQ capability 안전성이 Provider 구현 규율에 의존                           | cpf-core/.../CpfDataQualityCorrectionPort.java                                                                                       | proof verification을 framework-owned final wrapper/token consumption boundary로 이동하거나 provider가 우회할 수 없는 계약으로 변경.           |
| AB-R6-022 | P0       | HMAC capability 자체에 expiry/nonce-consumption single-use 없음             | AdmDataQualityApprovalProofService.java                                                                                              | TTL + nonce ledger/single-use token + replay rejection을 DB/cluster-safe하게 구현.                                             |
| AB-R6-023 | P0       | Integration Closure secret가 raw ConfigurationProperties 문자열로 주입 가능     | AdmIntegrationClosureProperties.java;AdmIntegrationClosureConfiguration.java                                                         | prod/stg에서는 SecretProvider/KMS/secret-ref만 허용하고 raw key property fail-closed.                                             |
| AB-R6-024 | P1       | Approval Policy immutability/active overlap이 DB까지 닫히지 않음               | AdmApprovalService.java;AdmApprovalRepository.java;DB schema/V104                                                                    | DB immutable trigger/privilege or update prohibition + effective range overlap constraint/transactional conflict.         |
| AB-R6-025 | P0       | Owner success 후 DB finalization/DB outage의 durable UNKNOWN 보장 부족       | AdmApprovalService.java;AdmApprovalRepository.java                                                                                   | durable command ledger/outbox/fencing + startup sweeper + owner-status reconcile로 side-effect/ledger 분리를 복구.              |
| AB-R6-026 | P1       | BAT Approved Remote Port가 localhost/local instance default로 fail-open  | cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java                                             | prod/stg risky command는 explicit endpoint/instance required, localhost/default identity 금지.                               |
| AB-R6-027 | P1       | InMemory DQ replay는 multi-instance/process-kill idempotency 증거가 아님     | cpf-common/.../InMemoryCpfDataQualityOperations.java                                                                                 | reference/local scope를 명확히 하고 production persistent provider에서 atomic CAS/idempotency/reconcile proof.                    |
| AB-R6-028 | P0       | EDU 135 Catalog ↔ Handler/Scenario requiredRole 불일치                    | cpf-reference/src/main/resources/edu/manual-135-catalog.json;EduAdm01\~17Handler.java                                                | catalog/resource/handler/test role exact equality gate, 고객 매뉴얼 권한 의미를 하나로 정본화.                                            |
| AB-R6-029 | P0       | EDU 135의 5종 Test가 실제 8종 Product Consumer Runtime을 증명하지 않음              | cpf-reference/src/test/java/com/cpf/reference/edu/runtime/\*\*                                                                       | JDBC/Batch/Gateway/HTTP/File/Process/Outbox 실제 adapters로 family별 integration/runtime corpus 추가.                           |
| AB-R6-030 | P0       | EDU-ADM 17이 요구 의미보다 template/common-state-machine 중심                   | cpf-reference/src/main/java/com/cpf/reference/optional/operations/\*\*/EduAdm\*Handler.java                                          | 17 ID 각각 requiredVerification/exceptionScenario를 executable behavior와 assertion으로 구현.                                     |
| AB-R6-031 | P0       | EDU-ADM readOnly 정본 불일치                                                | manual-135-catalog.json;EduAdm01/09/14/15Handler.java                                                                                | catalog↔scenario-contract↔handler exact semantic equality gate.                                                           |
| AB-R6-032 | P0       | EDU-ADM-04 승인 교육 예제가 실제 승인 정책/SoD/만료/범위를 구현하지 않음                       | EduAdm04Handler.java;tests/resource contract                                                                                         | 승인 요청→결정→실행→만료/SoD/변경충돌을 교육용 독립 모델로 실제 구현 및 test.                                                                         |
| AB-R6-033 | P0       | EDU-ADM-08 보안 교육 예제가 masking/IDOR/browser role matrix를 구현하지 않음         | EduAdm08Handler.java;scenario-contract.json                                                                                          | 조직 scope, IDOR, masked/raw permission, reason, audit를 executable example로 구현.                                             |
| AB-R6-034 | P0       | EDU-ADM-09 version conflict/browser flow 의미 불일치                        | EduAdm09Handler.java                                                                                                                 | 실제 expectedVersion/CAS conflict + reload/diff/resubmit/abort consumer/browser test.                                       |
| AB-R6-035 | P0       | EDU-ADM-10 bulk target semantics가 pseudo partition으로 대체                | EduAdm10Handler.java                                                                                                                 | 실 targetIds별 version/result ledger, partial success, failed-only reprocess, downloadable result 구현.                       |
| AB-R6-036 | P1       | EDU-ADM-11 maintenance/LKG rollback 의미가 선언에 그침                         | EduAdm11Handler.java                                                                                                                 | window validation, checksum, partial apply, LKG rollback, restart-required behavior.                                      |
| AB-R6-037 | P0       | EDU-ADM-12\~17 핵심 운영 의미가 generic JDBC state machine으로 대체               | EduAdm12\~17Handler.java                                                                                                             | 각 ID별 실제 model/consumer/negative/recovery/browser or infra test 구현.                                                       |
| AB-R6-038 | P0       | QA37 EDU Source Closure Gate가 semantic drift를 탐지하지 못함                  | cpf-tools/scripts/verify-cpf-qa37-manual-edu-135.py                                                                                  | catalog/resource/handler field-by-field equality + real consumer binding + actual runtime negative mutation gate.         |
| AB-R6-039 | P0       | Current SHA에서 EDU 135 target runtime 증거 없음                             | manual-135-catalog.json;R6S12 evidence                                                                                               | Java25/Gradle9.1 + refDB DB3 + enabled feature variants + 135 runtime + browser/consumer evidence.                        |
| AB-R6-040 | P0       | QA 진입 전 Codex/필수 Target Runtime 미완료                                    | CODEX\_REVIEW\_REQUEST.md;OPEN\_ISSUES.md                                                                                            | exact final result SHA Codex independent review와 필수 runtime을 완료한 뒤 QA 재요청.                                                |

---

# 20. Finding 상세 근거

## AB-R6-001 [P0] Current result SHA와 R6S12 Evidence provenance 미결속

### 확인 근거

current master는 77db10ad...이나 PACKAGE\_MANIFEST.resultCommitSha=null, evidence source가 작업 시작 SHA 28f823a...에 머문다.

### 주요 대상

`cpf-docs/work/v9i/dev/r6s12/**`

### QA 판정

**미통과**

### 재개발 요구

current exact SHA에서 전 검증 재실행, manifest/evidence/result SHA 재결속, clean tree 증거 생성.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-002 [P0] Current master Push에 Release Workflow 실행 없음

### 확인 근거

workflow는 workflow\_dispatch/pull\_request만 있고 master push trigger가 없다. current SHA status/run 0건.

### 주요 대상

`.github/workflows/cpf-r6-release-gates.yml`

### QA 판정

**미통과**

### 재개발 요구

master result SHA가 required checks와 자동 결속되도록 release policy를 정식화.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-003 [P0] Release Runner가 clean exact-SHA qualification을 증명하지 못함

### 확인 근거

HEAD 비교만 하고 실행 전후 dirty tree/generated diff를 거부하지 않으며 DB3/multiprocess가 optional이다.

### 주요 대상

`cpf-tools/verification/final-dev/run-r6-release-gates.ps1`

### QA 판정

**미통과**

### 재개발 요구

release mode에서 clean tree, Java25/Gradle9.1, ADM/BZA verify, browser, DB3, distributed, QA38/39/REV004를 non-optional로 강제.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-004 [P0] Playwright Release Gate 실제 Product wiring 미완성

### 확인 근거

CPF\_FRONTEND\_URL/auth fixture/server start가 workflow에 없고 Chromium만 설치하지만 config는 3 browser project를 가진다.

### 주요 대상

`cpf-admin/frontend/playwright.config.ts;.github/workflows/cpf-r6-release-gates.yml`

### QA 판정

**미통과**

### 재개발 요구

실제 backend/frontend/auth session으로 release E2E 실행. synthetic DOM injection 금지.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-005 [P0] Integration Closure UI operation permission이 실제 ADM Session과 연결되지 않음

### 확인 근거

/auth/me buttonIds가 존재하지만 page는 documentElement.dataset.admPermissions를 읽고 E2E가 이를 직접 주입한다.

### 주요 대상

`cpf-admin/frontend/src/features/integration-closure/IntegrationClosurePage.vue;cpf-admin/frontend/e2e/integration-closure-r6.spec.ts`

### QA 판정

**미통과**

### 재개발 요구

AdmSessionStore/buttonIds를 reactive single source로 사용하고 실제 session role matrix로 browser test.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-006 [P0] ADM generic RouteOperationWorkbench가 전용 화면의 permission/Strict JSON을 우회

### 확인 근거

모든 expectedOperationIds를 generic mutation UI로 실행하며 operation permission 검사가 없고 JSON.parse를 사용한다.

### 주요 대상

`cpf-admin/frontend/src/components/RouteOperationWorkbench.vue;cpf-admin/frontend/src/App.vue`

### QA 판정

**미통과**

### 재개발 요구

critical mutation 제거 또는 route/action permission, schema validation, reason/approval/audit를 동일하게 강제.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-007 [P0] Approval 위험 Operation UI에 action-level permission 미적용

### 확인 근거

policy save/request/decision/execute/reconcile 등 위험 버튼에 server operation permission이 직접 결속되지 않는다.

### 주요 대상

`cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue`

### QA 판정

**미통과**

### 재개발 요구

route read와 action permission을 분리하고 모든 위험 버튼을 server button/action grant에 결속.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-008 [P1] ADM 63 Route 중 4개가 59개 sidebar canonical menu에서 누락

### 확인 근거

featureFlags/integrationClosure/openApiOperations/resiliencePolicies가 route에는 있으나 createAdmState.menus에는 없다.

### 주요 대상

`cpf-admin/frontend/src/app/routes.ts;cpf-admin/frontend/src/state/createAdmState.ts`

### QA 판정

**미통과**

### 재개발 요구

route/menu registry 단일 정본화, 정상 로그인 후 sidebar discoverability browser test.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-009 [P0] ADM generated route-operation contract가 12 Route에서 stale

### 확인 근거

63 route 중 12개에서 routes.ts expectedOperationIds와 generated contract count가 불일치한다.

### 주요 대상

`cpf-admin/frontend/src/app/routes.ts;cpf-admin/frontend/src/generated/adm-route-operation-contract.ts`

### QA 판정

**미통과**

### 재개발 요구

current source OpenAPI로 regenerate 후 63/63 exact set equality 및 git diff --exit-code.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-010 [P0] Backend validation과 committed ADM OpenAPI/Generated artifact drift

### 확인 근거

R6 backend/enrich의 idempotency/reason/version 제약과 tracked cpf-openapi.json이 일치하지 않는다.

### 주요 대상

`cpf-admin/frontend/openapi/cpf-openapi.json;cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs`

### QA 판정

**미통과**

### 재개발 요구

runtime/controller OpenAPI 정본 → generated client 전부 재생성, clean regeneration gate.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-011 [P1] Frontend enrich script가 validation/security response 계약의 제2 정본 역할

### 확인 근거

route synthesis는 fail-closed로 바뀌었으나 schema 제약을 frontend script가 다시 정의해 backend drift가 재발했다.

### 주요 대상

`cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs`

### QA 판정

**미통과**

### 재개발 요구

controller/runtime schema를 정본으로 하고 frontend script는 검증/annotation 범위로 축소.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-012 [P1] Generated Client가 high-risk 실제 Consumer를 compile-time으로 충분히 구속하지 않음

### 확인 근거

manual TS interface + generic operation facade가 많고 response status/signal/headers 일부가 상위 payload 경계에서 소실된다.

### 주요 대상

`cpf-admin/frontend/src/shared/cpfApi.ts;integrationClosureApi.ts;orval-mutator.ts`

### QA 판정

**미통과**

### 재개발 요구

high-risk API는 generated request/response/status를 actual consumer에서 직접 사용.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-013 [P0] Operation consumer Gate False Green

### 확인 근거

generic workbench가 있으면 route expectedOperationIds를 consumed로 넣어 문자열 등록만으로 real consumer로 간주할 수 있다.

### 주요 대상

`cpf-admin/frontend/scripts/verify-operation-consumer.mjs;cpf-biz-admin/frontend/scripts/verify-operation-consumer.mjs`

### QA 판정

**미통과**

### 재개발 요구

실제 component callsite/typed generated API 호출을 증명하고 registry-only 소비를 금지.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-014 [P0] R6 Behavior Mutation Gate가 실제 mutation execution이 아닌 tautology

### 확인 근거

token을 문자열 replace로 제거한 뒤 token 부재를 PASS로 보는 구조이며 mutated source에 실제 gate를 실행하지 않는다.

### 주요 대상

`cpf-tools/verification/final-dev/verify-r6-behavior-contracts.py`

### QA 판정

**미통과**

### 재개발 요구

temp checkout/copy에 real mutation 적용 → 실제 gate 실행 → nonzero exit 확인.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-015 [P1] DB Runner Security Test가 process behavior가 아니라 문자열 검사 중심

### 확인 근거

PowerShell source는 개선됐지만 Python/Pester가 child env leak/timeout/kill-tree를 실제 child process로 재현하지 않는다.

### 주요 대상

`cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1;verify-db3-runner-contract.py`

### QA 판정

**미통과**

### 재개발 요구

dummy child로 secret inheritance/timeout/tree-kill/stdout redaction 실제 runtime test.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-016 [P0] BZA Workbench permission code가 canonical manifest와 불일치

### 확인 근거

Workbench는 SETTINGS/PERMISSIONS를 검사하지만 canonical은 SETTING/AUTHORIZATION이다.

### 주요 대상

`cpf-biz-admin/frontend/src/components/RouteOperationWorkbench.vue;cpf-tools/db/metadata/bza-permission-manifest.json`

### QA 판정

**미통과**

### 재개발 요구

canonical actionRules 기반 operation permission으로 교체.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-017 [P0] BZA Release Integrity 미완성

### 확인 근거

BZA build inputs가 ADM 수준으로 scripts/openapi/orval/tool versions를 추적하지 않고 R6 release가 BZA Playwright를 실행하지 않는다.

### 주요 대상

`cpf-biz-admin/build.gradle;cpf-biz-admin/frontend/**`

### QA 판정

**미통과**

### 재개발 요구

ADM/BZA 동일한 generation reproducibility + BZA browser release gate.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-018 [P2] Frontend idempotency storage 정책/표현 불일치

### 확인 근거

default localStorage ledger인데 UI/요건은 session 범위로 설명하고 test storage isolation도 혼재한다.

### 주요 대상

`integrationClosureIdempotency.ts/.test.ts`

### QA 판정

**미통과**

### 재개발 요구

pending retry lifetime/rotation 정책을 명시하고 실제 storage semantics와 UI 문구/Test를 일치.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-019 [P0] Approval Owner Registry 4D binding이 exact tuple이 아니라 fuzzy matching

### 확인 근거

BatchRuntime은 owner.contains(batch), action.contains(RETRY/STOP/RUN 등), CenterCut도 contains 조합을 허용한다.

### 주요 대상

`BatchRuntimeApprovalOwnerCommandAdapter.java;CenterCutApprovalOwnerCommandAdapter.java`

### QA 판정

**미통과**

### 재개발 요구

canonical exact allowlist/enum table로 ownerModule+ownerCommand+actionType+targetType 완전일치. near-match negative matrix.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-020 [P0] Process Kill 후 Approval EXECUTING/RUNNING 고착 경로

### 확인 근거

reserveExecution 후 JVM kill이면 catch/finalization이 실행되지 않는다. 재기동 execute는 execution 존재 시 return하고 reconcile은 UNKNOWN만 허용한다.

### 주요 대상

`AdmApprovalService.java;AdmApprovalRepository.java;AdmApprovalController.java`

### QA 판정

**미통과**

### 재개발 요구

stale RUNNING lease/deadline+sweeper/reconcile entry를 구현하고 owner 호출 전/후 kill을 2-instance에서 검증.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-021 [P0] Public DQ capability 안전성이 Provider 구현 규율에 의존

### 확인 근거

ApprovedCorrection은 public constructible record이며 SPI가 모든 provider에게 HMAC verification을 구조적으로 강제하지 않는다.

### 주요 대상

`cpf-core/.../CpfDataQualityCorrectionPort.java`

### QA 판정

**미통과**

### 재개발 요구

proof verification을 framework-owned final wrapper/token consumption boundary로 이동하거나 provider가 우회할 수 없는 계약으로 변경.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-022 [P0] HMAC capability 자체에 expiry/nonce-consumption single-use 없음

### 확인 근거

verify는 HMAC만 재계산하며 approvedAt freshness나 nonce consumption ledger를 검사하지 않는다.

### 주요 대상

`AdmDataQualityApprovalProofService.java`

### QA 판정

**미통과**

### 재개발 요구

TTL + nonce ledger/single-use token + replay rejection을 DB/cluster-safe하게 구현.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-023 [P0] Integration Closure secret가 raw ConfigurationProperties 문자열로 주입 가능

### 확인 근거

approvalProofKeyBase64와 crypto.activeKeyBase64를 property/env raw string으로 직접 보유/사용한다.

### 주요 대상

`AdmIntegrationClosureProperties.java;AdmIntegrationClosureConfiguration.java`

### QA 판정

**미통과**

### 재개발 요구

prod/stg에서는 SecretProvider/KMS/secret-ref만 허용하고 raw key property fail-closed.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-024 [P1] Approval Policy immutability/active overlap이 DB까지 닫히지 않음

### 확인 근거

service/repository는 insert-only이나 DB update는 가능하고, overlapping enabled active policy save를 방지하지 않아 findActivePolicy가 outage를 일으킬 수 있다.

### 주요 대상

`AdmApprovalService.java;AdmApprovalRepository.java;DB schema/V104`

### QA 판정

**미통과**

### 재개발 요구

DB immutable trigger/privilege or update prohibition + effective range overlap constraint/transactional conflict.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-025 [P0] Owner success 후 DB finalization/DB outage의 durable UNKNOWN 보장 부족

### 확인 근거

finalization 실패 catch가 같은 DB에 mark UNKNOWN/history를 다시 쓰므로 DB outage나 process kill이면 그것도 실패할 수 있다.

### 주요 대상

`AdmApprovalService.java;AdmApprovalRepository.java`

### QA 판정

**미통과**

### 재개발 요구

durable command ledger/outbox/fencing + startup sweeper + owner-status reconcile로 side-effect/ledger 분리를 복구.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-026 [P1] BAT Approved Remote Port가 localhost/local instance default로 fail-open

### 확인 근거

base-url default [http://127.0.0.1:8180](http://127.0.0.1:8180), callerInstanceId default adm-local-01.

### 주요 대상

`cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java`

### QA 판정

**미통과**

### 재개발 요구

prod/stg risky command는 explicit endpoint/instance required, localhost/default identity 금지.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-027 [P1] InMemory DQ replay는 multi-instance/process-kill idempotency 증거가 아님

### 확인 근거

quarantine state를 먼저 변경한 뒤 process-local replayFingerprints/results에 기록한다.

### 주요 대상

`cpf-common/.../InMemoryCpfDataQualityOperations.java`

### QA 판정

**미통과**

### 재개발 요구

reference/local scope를 명확히 하고 production persistent provider에서 atomic CAS/idempotency/reconcile proof.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-028 [P0] EDU 135 Catalog ↔ Handler/Scenario requiredRole 불일치

### 확인 근거

EDU-ADM 17 Catalog는 CPF\_ADM\_OPERATOR이나 17 Handler가 CPF\_REFERENCE\_PLATFORM\_OPERATOR. DEV/BZA sampled family도 동일 패턴.

### 주요 대상

`cpf-reference/src/main/resources/edu/manual-135-catalog.json;EduAdm01~17Handler.java`

### QA 판정

**미통과**

### 재개발 요구

catalog/resource/handler/test role exact equality gate, 고객 매뉴얼 권한 의미를 하나로 정본화.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-029 [P0] EDU 135의 5종 Test가 실제 8종 Product Consumer Runtime을 증명하지 않음

### 확인 근거

공통 TestSupport가 FileEduOperationRepository + TestEduBusinessConsumers deterministic doubles를 사용한다.

### 주요 대상

`cpf-reference/src/test/java/com/cpf/reference/edu/runtime/**`

### QA 판정

**미통과** / 소스 개선은 인정하지만 Runtime 미검증

### 재개발 요구

JDBC/Batch/Gateway/HTTP/File/Process/Outbox 실제 adapters로 family별 integration/runtime corpus 추가.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-030 [P0] EDU-ADM 17이 요구 의미보다 template/common-state-machine 중심

### 확인 근거

17개 Handler 전수 Source 확인 결과 다수가 exception/verification 문자열과 generic JDBC binding만 갖고 고유 ADM 업무 의미를 구현하지 않는다.

### 주요 대상

`cpf-reference/src/main/java/com/cpf/reference/optional/operations/**/EduAdm*Handler.java`

### QA 판정

**미통과**

### 재개발 요구

17 ID 각각 requiredVerification/exceptionScenario를 executable behavior와 assertion으로 구현.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-031 [P0] EDU-ADM readOnly 정본 불일치

### 확인 근거

EDU-ADM-01/09/14/15에서 Catalog readOnly와 Handler readOnly가 다르다.

### 주요 대상

`manual-135-catalog.json;EduAdm01/09/14/15Handler.java`

### QA 판정

**미통과**

### 재개발 요구

catalog↔scenario-contract↔handler exact semantic equality gate.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-032 [P0] EDU-ADM-04 승인 교육 예제가 실제 승인 정책/SoD/만료/범위를 구현하지 않음

### 확인 근거

필드/step 선언 외에 실제 approval policy binding, self-approval, expiry, target drift behavior가 없다.

### 주요 대상

`EduAdm04Handler.java;tests/resource contract`

### QA 판정

**미통과**

### 재개발 요구

승인 요청→결정→실행→만료/SoD/변경충돌을 교육용 독립 모델로 실제 구현 및 test.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-033 [P0] EDU-ADM-08 보안 교육 예제가 masking/IDOR/browser role matrix를 구현하지 않음

### 확인 근거

요구사항은 강하지만 Handler는 generic role/dataScope/nonblank 검사뿐이다.

### 주요 대상

`EduAdm08Handler.java;scenario-contract.json`

### QA 판정

**미통과**

### 재개발 요구

조직 scope, IDOR, masked/raw permission, reason, audit를 executable example로 구현.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-034 [P0] EDU-ADM-09 version conflict/browser flow 의미 불일치

### 확인 근거

expected version 입력이 없고 Handler는 readOnly query이며 409/diff/reload/resubmit UI가 없다.

### 주요 대상

`EduAdm09Handler.java`

### QA 판정

**미통과**

### 재개발 요구

실제 expectedVersion/CAS conflict + reload/diff/resubmit/abort consumer/browser test.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-035 [P0] EDU-ADM-10 bulk target semantics가 pseudo partition으로 대체

### 확인 근거

targetIds/expectedVersions를 targetKeys가 사용하지 않고 기본 4 pseudo partition을 만든다.

### 주요 대상

`EduAdm10Handler.java`

### QA 판정

**미통과**

### 재개발 요구

실 targetIds별 version/result ledger, partial success, failed-only reprocess, downloadable result 구현.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-036 [P1] EDU-ADM-11 maintenance/LKG rollback 의미가 선언에 그침

### 확인 근거

maintenanceWindow/targets/configVersion을 구체 검증하지 않고 pseudo partition을 사용한다.

### 주요 대상

`EduAdm11Handler.java`

### QA 판정

**미통과**

### 재개발 요구

window validation, checksum, partial apply, LKG rollback, restart-required behavior.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-037 [P0] EDU-ADM-12\~17 핵심 운영 의미가 generic JDBC state machine으로 대체

### 확인 근거

Incident/evidence/topology/correlation/notification/browser-session 각 시나리오의 실제 domain behavior가 Handler에 없다.

### 주요 대상

`EduAdm12~17Handler.java`

### QA 판정

**미통과**

### 재개발 요구

각 ID별 실제 model/consumer/negative/recovery/browser or infra test 구현.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-038 [P0] QA37 EDU Source Closure Gate가 semantic drift를 탐지하지 못함

### 확인 근거

정확히 135/파일/5 test를 검사하지만 role/readOnly/canonical semantic equality를 확인하지 않고 compile path가 JdbcEduOperationRepository 실 runtime을 증명하지 않는다.

### 주요 대상

`cpf-tools/scripts/verify-cpf-qa37-manual-edu-135.py`

### QA 판정

**미통과**

### 재개발 요구

catalog/resource/handler field-by-field equality + real consumer binding + actual runtime negative mutation gate.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-039 [P0] Current SHA에서 EDU 135 target runtime 증거 없음

### 확인 근거

catalog 자체가 verificationStatus=미검증이며 JAVA25\_GRADLE\_AND\_RUNTIME\_PENDING으로 기록한다.

### 주요 대상

`manual-135-catalog.json;R6S12 evidence`

### QA 판정

**미통과**

### 재개발 요구

Java25/Gradle9.1 + refDB DB3 + enabled feature variants + 135 runtime + browser/consumer evidence.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

## AB-R6-040 [P0] QA 진입 전 Codex/필수 Target Runtime 미완료

### 확인 근거

QA-R5I-029 Codex 미실행, Java25/Gradle9.1/DB3/Browser/distributed current SHA runtime 미실행.

### 주요 대상

`CODEX_REVIEW_REQUEST.md;OPEN_ISSUES.md`

### QA 판정

**미통과**

### 재개발 요구

exact final result SHA Codex independent review와 필수 runtime을 완료한 뒤 QA 재요청.

### QA 재검수 성공 기준

- current exact result SHA
- 실제 Source/Consumer 경로 일치
- 해당 Negative/Failure/Concurrency/Recovery test
- 실행 command/exit code
- current SHA Evidence
- stale/generated diff 0
- false-green이 아닌 실제 behavior assertion

### 실패 기준

- 파일/문자열/Test 존재만으로 PASS
- 과거 SHA evidence
- synthetic fixture만 PASS
- 실제 consumer/권한/runtime path 미연결
- NOT\_EXECUTED를 PASS로 기록

---

# 21. 기존 R5I 29건 A+B 재판정

| R5I | A+B 판정    | 사유                                                           |
| --- | --------- | ------------------------------------------------------------ |
| 001 | 미통과       | current SHA provenance 미결속                                   |
| 002 | 미통과       | CI/Release current SHA runtime 없음                            |
| 003 | 미통과       | Evidence source SHA stale                                    |
| 004 | 통과 후보(소스) | 보호 SVG 복원은 확인, final package 재검산 필요                          |
| 005 | 미통과       | Owner exact 4D tuple 아님                                      |
| 006 | 소스 개선/미검증 | secure profile guard 개선, target runtime 없음                   |
| 007 | 소스 개선/미검증 | substantive context tests, target Spring runtime 없음          |
| 008 | 미통과       | OpenAPI tracked artifact drift                               |
| 009 | 소스 개선/미검증 | detail sanitize 개선, browser/permission boundary 잔존           |
| 010 | 미통과       | strict parser 개선이나 generic Workbench 우회                      |
| 011 | 소스 개선/미검증 | null-safe copy 개선                                            |
| 012 | 미통과       | process-local replay, process-kill/distributed 보장 없음         |
| 013 | 미통과       | DB immutability/active policy overlap 미완료                    |
| 014 | 미검증       | DB3 live concurrency 없음                                      |
| 015 | 소스 개선/미검증 | runner source 개선, process runtime test 없음                    |
| 016 | 미통과       | actual session operation permission 미연결                      |
| 017 | 미통과       | ADM generated stale + BZA build parity 미완료                   |
| 018 | 미통과       | frontend enrich second-source / tracked spec drift           |
| 019 | 미통과       | QA38/39 current full run 없음 + false-green mutation           |
| 020 | 미통과       | current SHA full hygiene/evidence 미완료                        |
| 021 | 미통과       | current result SHA 이후 docs/evidence stale                    |
| 022 | 미통과       | resultCommitSha null                                         |
| 023 | 미통과       | release runner qualification 부족                              |
| 024 | 미통과       | Codex/runtime/current provenance 미완료                         |
| 025 | 미통과       | public capability provider-dependent + owner tuple fuzziness |
| 026 | 부분 검증/미통과 | A→B→A source 개선, real browser/session semantics 미완료          |
| 027 | 미통과       | mutation gate tautology                                      |
| 028 | 미통과       | backend status 개선, frontend/OpenAPI end-to-end 미완료           |
| 029 | 미통과       | Codex 독립 검수 미실행                                              |

중요:

- 기존 QA 원문을 QA2/A+B가 임의로 통과로 변경하지 않는다.
- `통과 후보(소스)`도 통합 QA actual runtime/pass 전에는 최종 통과가 아니다.
- R6는 R5I history를 보존하고 additive revision으로 기록해야 한다.

---

# 22. FDEV-001\~025 A+B 재판정

| Requirement | A+B 판정    | 근거                                                                           |
| ----------- | --------- | ---------------------------------------------------------------------------- |
| FDEV-001    | 미통과       | baseline/provenance current SHA 불일치                                          |
| FDEV-002    | 부분 검증     | Spring/DQ source 실질 개선, target runtime 미실행                                   |
| FDEV-003    | 미통과       | Approval owner/security/UI/HTTP/kill recovery 잔존                             |
| FDEV-004    | 미검증/미통과   | Java25/Gradle9.1 current SHA build/CI 없음                                     |
| FDEV-005    | 미검증       | DB3 live 없음                                                                  |
| FDEV-006    | 미검증/미통과   | broker/multiprocess/split-WAS/process-kill 없음; approval RUNNING recovery gap |
| FDEV-007    | 재확인 필요    | full Public API/SPI repo regression 미실행                                      |
| FDEV-008    | 부분 검증     | Controller-Service-Owner-Port source path 존재, runtime 미실행                    |
| FDEV-009    | 부분 검증     | DQ 개선, wider Outbox/Notification runtime 미검증                                 |
| FDEV-010    | 미검증       | Batch full runtime 미실행; owner tuple fuzzy                                    |
| FDEV-011    | 미검증       | Cache full scope runtime 미실행                                                 |
| FDEV-012    | 미통과       | CAS source 개선이나 process-kill/distributed atomicity 미증명                       |
| FDEV-013    | 미통과       | security secret/capability/UI permission 잔존                                  |
| FDEV-014    | 미통과       | ADM 63-route/menu/permission/generated contract 결함                           |
| FDEV-015    | 미통과       | BZA Workbench permission/build/browser 결함                                    |
| FDEV-016    | 미통과       | OpenAPI/generated/current artifact drift                                     |
| FDEV-017    | 미통과       | real browser release path 미완성                                                |
| FDEV-018    | 미통과       | EDU 135 semantic/runtime closure 미달                                          |
| FDEV-019    | 부분 검증/미검증 | DB3 static parity, live vendor lifecycle 없음                                  |
| FDEV-020    | 미통과       | false-green gates + current repo evidence 미완료                                |
| FDEV-021    | 미통과       | current result SHA docs state stale                                          |
| FDEV-022    | 미통과       | result SHA provenance 미결속                                                    |
| FDEV-023    | 미통과       | external runner package가 실제 acceptance를 닫지 못함                                |
| FDEV-024    | 미통과       | Codex/target runtime/current provenance 미완료                                  |
| FDEV-025    | 미통과       | QA38/39 current run 및 mutation semantics 미완료                                 |

---

# 23. 긍정적으로 확인된 구현 — 재개발 시 보존

## Approval

- immutable snapshot hash direction
- canonical active policy check
- participant snapshot
- request/decision idempotency
- request CAS
- execution reservation
- owner result UNKNOWN model
- observation-only reconcile
- public detail sanitize
- HMAC proof
- DQ CAS
- DB unique constraints

## Spring/Profile

- default local profile 제거
- feature default disabled
- ephemeral local/dev restriction
- prod/stg ephemeral rejection
- missing provider fail-closed
- query/correction provider pair
- customer override tests

## Frontend

- actual API consumers 다수 존재
- Permissions/Operators CRUD substantive
- Messages/Codes/Configs/Response/Notification actual calls
- Gateway SSE source
- strict JSON implementation
- A→B→A multi-entry ledger direction
- audit links/error UI

## DB Runner

- connection JSON stdin
- child environment clear
- allowlist
- timeout
- tree kill
- output redaction

## EDU

- exact 135 catalog
- exact contributor count
- 17 ADM handlers physically registered
- common execution engine substantive
- durable File repository
- JDBC repository substantive
- shared idempotency/lease/outbox/audit/reconcile architecture

이 구현은 재개발 과정에서 제거하거나 이전 취약 구조로 되돌리지 않는다.

---

# 24. 통합 QA가 개발GPT에 반드시 요구해야 할 ADM 완료 기준

## 메뉴

- 63 route 모두 canonical menu policy 확정
- 사용자 접근용 메뉴면 sidebar/검색/navigation 노출
- 숨김 route면 숨김 이유와 direct URL authorization 명시
- route/menu/button/action single source

## CRUD

각 CRUD 메뉴별:

1. list/search
2. paging
3. detail
4. create
5. update
6. delete/disable/state transition
7. expectedVersion/CAS
8. reason
9. role/action permission
10. audit
11. 401/403/404/409/422/429/500/503
12. generated API parity
13. browser test

해당 기능에 Delete가 업무적으로 금지되면 soft-delete/status transition로 계약을 명시한다.

## Realtime

- actual SSE/WebSocket/polling
- reconnect
- duplicate event
- stale event
- authorization expiry
- logout/relogin
- backpressure
- network loss
- fallback polling
- multi-instance event consistency

## Generated Contract

63 route:

```
expectedOperationIds
==
generated route contract
==
OpenAPI
==
actual component consumer
```

이어야 한다.

---

# 25. 통합 QA가 개발GPT에 반드시 요구해야 할 EDU 완료 기준

## 수량

- 135 exact
- 추가 Gap 발견 시 같은 작업에서 ID 추가
- 숫자만 맞추는 것 금지

## 각 EDU ID마다

### Contract equality

```
manual requirement
==
manual-135-catalog
==
scenario-contract.json
==
handler definition
==
consumer binding
==
test expected contract
```

필드별 비교:

- requiredRole
- readOnly
- requiredFields
- businessStates
- workflowSteps
- failurePoints
- idempotent
- versioned
- leaseRequired
- externalEffect
- compensation
- rollback
- retries
- consumer type
- configuration key
- argument fields

### Business semantics

예제 제목에 맞는 실제 logic을 구현.

`exceptionScenarios`와 `requiredVerification`을 String list로만 두는 것 금지.

각 항목을:

- executable code
- negative test
- recovery test
- runtime evidence

에 연결.

### Test

5개 파일 수만 확인하지 않는다.

- Unit: ID-specific business rules
- Integration: actual product/reference adapter
- Failure: 모든 declared failure point
- Recovery: exact reconcile/compensation semantics
- Concurrency: actual DB/process/distributed semantics

### Consumer

deterministic doubles는 shared engine unit test에서만 허용.

Release evidence는:

- real JDBC
- real Batch
- real Gateway
- real HTTP
- real File
- real Process
- real Outbox

의 필요한 실행 경로를 사용.

### ADM EDU 17

특히:

- actual ADM concept parity
- actual Browser-required scenarios
- role/masking/IDOR/audit
- approval/SoD
- conflict/reload
- bulk partial result
- incident/recovery
- topology/trace
- notification escalation
- session expiry/relogin/CSRF

를 개별 구현.

---

# 26. 필수 Runtime Qualification

## Git

```
git fetch origin master
git rev-parse origin/master
git rev-parse HEAD
git status --porcelain
```

Start/End clean tree 필수.

## Java

```
.\gradlew.bat --version
.\gradlew.bat --no-daemon --max-workers=1 clean build --stacktrace
.\gradlew.bat --no-daemon --max-workers=1 aggregateQualityBuild publicationGate --stacktrace
```

- Java 25
- Gradle 9.1

## ADM

```
cd cpf-admin/frontend
npm ci
npm run verify
npm run test:e2e
npm run test:a11y
```

실제 backend/frontend/session 사용.

## BZA

```
cd cpf-biz-admin/frontend
npm ci
npm run verify
npm run test:e2e
```

## EDU

- exact 135 source closure
- Java25 compile
- cpf-reference full tests
- all feature variants
- refDB Oracle/PostgreSQL/MariaDB
- actual consumer adapters
- 17 EDU-ADM semantic suite
- process kill/restart
- browser-required EDU cases

## Approval distributed

- 2+ instances
- duplicate approval request
- duplicate decision
- duplicate execute
- kill after reserve/before owner
- kill after owner/before finalization
- DB outage after owner
- restart sweeper
- reconcile
- exact once/single side-effect

## DB3

Oracle/PostgreSQL/MariaDB:

- install
- seed
- migration
- upgrade
- verify
- concurrent unique/CAS
- rollback
- drift
- re-upgrade

## QA Gates

- QA37
- QA38
- QA39
- REV004
- real mutation corpus
- generated diff gate
- package/evidence validation

---

# 27. Evidence Acceptance

모든 PASS:

- exact source SHA
- command
- cwd
- OS
- Java/Gradle/Node/npm/DB/browser versions
- start/end
- exit code
- sanitized stdout/stderr
- raw log SHA256
- artifact SHA256
- expected
- actual
- PASS/FAIL/NOT\_EXECUTED

금지:

- pre-push overlay evidence를 current SHA PASS로 재사용
- static token gate를 runtime PASS로 표시
- test double을 real consumer PASS로 표시
- synthetic browser permission injection을 release PASS로 표시
- resultCommitSha null로 final qualification
- empty/stale/mismatched evidence

---

# 28. 마무리 개발 단계 운영 요구

이번 R6S12처럼:

- Finding 부분 구현
- Requirement 재확인 필요
- Codex 미완료
- result SHA 미생성
- complete checkout 미검증

상태에서 “개발 완료”로 QA에 넘기지 않는다.

마무리 단계 권장 순서:

```
개발GPT Source 완결
→ 사용자 적용/Push로 result SHA 생성
→ 개발GPT current SHA 재검증
→ 미실행 중 현재 환경에서 가능한 것 전부 실행
→ Evidence 재결속
→ 개발GPT 자체검수
→ Codex exact SHA 독립검수/보완
→ QA A/B
→ 통합 QA
```

외부 인프라만 정말 불가능하면:

- 담당자
- 환경
- 권한
- command
- expected
- failure criteria
- required evidence

를 기록하고 미검증으로 남긴다.

---

# 29. 최종 통합 QA 인계

## 현재 결론

**R6S12 / master** **`77db10ad...`** **— QA A+B 미통과**

## P0 우선순위

1. current SHA Evidence 재결속
2. Approval Process Kill RUNNING recovery
3. exact Owner Registry
4. capability provider-enforced single-use/expiry
5. production secret binding
6. ADM real permission source
7. generic Workbench bypass
8. ADM route/menu/generated contract
9. OpenAPI/generated parity
10. Browser real release
11. BZA permission/release
12. false-green mutation/consumer gates
13. EDU role/readOnly contract mismatch
14. EDU-ADM 17 semantic reimplementation
15. EDU real consumer runtime closure
16. Codex/current-SHA qualification

## 통합 QA에 전달할 핵심 문장

> 현재 CPF master `77db10ad9aff44ee422795080fb2e96b364c9d65`는 R5I 대비 실질적인 Source 보완이 있으나, QA A 관점에서 Approval exact tuple·process-kill RUNNING recovery·capability single-use/provider enforcement·secret binding·DB finalization recovery가 닫히지 않았고, QA B 관점에서 ADM 63 route/59 sidebar·12 generated contract drift·actual permission consumer·OpenAPI/generated/browser/release가 닫히지 않았다. EDU는 canonical 135개와 ADM 17개 등록 수량은 정확하지만 EDU-ADM 17 Handler 전수 Source 검토에서 catalog/handler role 불일치, readOnly drift, shared test-double 중심 검증, 다수 scenario의 template/common-state-machine 대체가 확인되어 “135개 요구 의미 완료”로 판정할 수 없다. 따라서 R6S12는 A+B 모두 미통과이며 current exact SHA에서 재개발·실 Runtime·Codex·Evidence 재결속 후 재검수해야 한다.

---

# 30. 주요 직접 검토 Source

## Approval / Core

- `cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java`
- `cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java`
- `cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java`
- `cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmApprovalSnapshotIntegrity.java`
- `cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmDataQualityApprovalProofService.java`
- `cpf-admin/src/main/java/com/cpf/admin/approval/spi/AdmApprovalOwnerCommandPort.java`
- `BatchRuntimeApprovalOwnerCommandAdapter.java`
- `CenterCutApprovalOwnerCommandAdapter.java`
- `DataQualityCorrectionApprovalOwnerCommandAdapter.java`
- `GatewayApprovalOwnerCommandAdapter.java`
- `BrokerReliabilityApprovalOwnerCommandAdapter.java`
- `BatchJobDefinitionApprovalOwnerCommandAdapter.java`
- `BatApprovalOwnerCommandPort.java`
- `CpfDataQualityCorrectionPort.java`
- `InMemoryCpfDataQualityOperations.java`

## Spring/Profile

- `AdmIntegrationClosureConfiguration.java`
- `AdmIntegrationClosureProperties.java`
- `AdmIntegrationClosureProfileGuard.java`
- `AdmIntegrationClosureConfigurationTest.java`
- `AdmIntegrationClosureProfileGuardTest.java`

## ADM Frontend

- `src/app/routes.ts`
- `src/state/createAdmState.ts`
- `src/App.vue`
- `src/stores/admSessionStore.ts`
- `src/stores/admConsoleStore.ts`
- `src/features/permissions/PermissionsPage.vue`
- `src/app/methods/accessMethods.ts`
- `src/app/methods/referenceMethods.ts`
- `src/features/integration-closure/IntegrationClosurePage.vue`
- `src/features/approvals/ApprovalsPage.vue`
- `src/features/gateway-operations/GatewayOperationsPage.vue`
- `src/components/RouteOperationWorkbench.vue`
- `src/generated/adm-route-operation-contract.ts`
- `scripts/write-route-operation-contract.mjs`
- `scripts/verify-operation-consumer.mjs`
- `scripts/enrich-adm-openapi-contract.mjs`
- `openapi/cpf-openapi.json`
- `playwright.config.ts`
- `package.json`

## EDU

- `cpf-docs/work/current/CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md`
- `cpf-reference/src/main/resources/edu/manual-135-catalog.json`
- `EduCapabilityRegistry.java`
- `ReferenceOperationsCapabilityContributor.java`
- `EduExecutionService.java`
- `JdbcEduOperationRepository.java`
- `FileEduOperationRepository.java`
- `EduBusinessConsumerRegistry.java`
- `AbstractEduCapabilityHandler.java`
- `AbstractManualEduTestSupport.java`
- `AbstractManualEduUnitTest.java`
- `AbstractManualEduIntegrationTest.java`
- `AbstractManualEduFailureTest.java`
- `AbstractManualEduConcurrencyTest.java`
- `TestEduBusinessConsumers.java`
- `verify-cpf-qa37-manual-edu-135.py`
- `EduAdm01Handler.java`
- `EduAdm02Handler.java`
- `EduAdm03Handler.java`
- `EduAdm04Handler.java`
- `EduAdm05Handler.java`
- `EduAdm06Handler.java`
- `EduAdm07Handler.java`
- `EduAdm08Handler.java`
- `EduAdm09Handler.java`
- `EduAdm10Handler.java`
- `EduAdm11Handler.java`
- `EduAdm12Handler.java`
- `EduAdm13Handler.java`
- `EduAdm14Handler.java`
- `EduAdm15Handler.java`
- `EduAdm16Handler.java`
- `EduAdm17Handler.java`

## DB / Release / Evidence

- PostgreSQL canonical ADM schema
- R104 Oracle/PostgreSQL/MariaDB approval integrity migration/rollback
- `run-db3-lifecycle.ps1`
- DB runner tests/contract
- `verify-r6-behavior-contracts.py`
- `.github/workflows/cpf-r6-release-gates.yml`
- `run-r6-release-gates.ps1`
- R6S12 `PACKAGE_MANIFEST.json`
- `FINDING_STATUS.csv`
- `REQUIREMENT_STATUS.csv`
- `OPEN_ISSUES.md`
- `EVIDENCE_LEDGER.csv`

---

# 31. Git Safety

본 QA 작업:

- Commit: NO
- Push: NO
- Merge: NO
- Branch: NO
- Tag: NO
- PR: NO
- Release: NO
- Delete: NO
- Reset: NO
- Restore: NO
- Stash: NO
- Clean: NO
- History modification: NO

---

# 32. 최종 판정

# **QA A+B: 미통과**

### ADM

“기능이 없는 빈 UI”는 아니며 실제 CRUD/Realtime/Approval Source가 다수 존재한다.
하지만 63 route 전 범위가 메뉴·generated contract·permission·browser runtime까지 닫히지 않았다.

### EDU

**135개는 정확히 존재한다.**

그러나:

```
135개 ID 존재
≠
135개 요구 의미 완료
≠
135개 실제 Consumer Runtime PASS
```

특히 **EDU-ADM 17개는 이번 QA에서 Handler Source 17개를 전부 읽었고**, 현재 상태로는 17개 전체를 완료 승인할 수 없다.

통합 QA는 본 보고서를 기준으로 **ADM 메뉴/contract/runtime 재개발 + EDU 135 semantic closure 재개발**을 R6 후속 개발 요건에 반드시 포함해야 한다.