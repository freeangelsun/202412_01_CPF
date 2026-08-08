# CPF 개발GPT — PROJECT FINAL SOURCE COMPLETION 실행지침

## 0. 이번 요청의 의미

이번 요청은 일부 Finding을 고치는 회차가 아니다.

**이번 개발 결과로 CPF Product Source 개발을 끝낸다.**

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 현재 중앙 Control 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- Product Source 직전 기준 SHA: `3aa1dd12f8a5938d33feb6ed598b3dd2442bf2e2` (`07_05`, `07_06`은 Control/Evidence currentization only)
- 최상위 Requirement 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- Canonical Requirement denominator: **169**
- Legacy Alias: **8, 완료율 중복집계 금지**

작업 시작 즉시 최신 `origin/master`, exact SHA, HEAD, Working Tree를 다시 확인한다.
이 Central Currentization Overlay가 먼저 적용·Push되었다면 그 successor `master` exact SHA를 실제 개발 시작 기준으로 사용한다. Product Source 차이가 없더라도 Evidence와 결과는 실제 시작 SHA를 기록하고 과거 QA evidence를 자동승계하지 않는다.

## 1. 최상위 완료 목표

개발GPT의 완료 목표는 다음 목록 몇 건을 끝내는 것이 아니다.

**169 Canonical Requirement 전체를 기준으로 현재 Repository에서 구현 가능한 CPF Product 미비점을 이번 요청에서 모두 제거하는 것**이다.

입력 자료:
- 이전 개발 Requirement 원장 93
- 이전 중앙 Finding 56
- 이전 개발 자체발견 4
- QA A Final 신규 25
- QA B Final 신규 8
- 중앙 중복정규화 신규 Action **31 = P0 22 / P1 9**
- Mandatory Runtime 13
- 개발 중 신규 self-found defect 전체

`31/31`, `56/56`, `93/93`만 끝내고 종료하면 안 된다.
종료 질문은 반드시:

> 현재 환경에서 내가 구현할 수 있는 CPF Product 미비점이 더 남아 있는가?

남아 있으면 계속 개발한다.

## 2. 매우 중요 — README/매뉴얼/고객 산출물은 이번 개발GPT가 건드리지 않는다

사용자 지시로 Product Development와 Documentation Finalization을 분리한다.

### 절대 수정 금지

- `README.md`
- `cpf-docs/guides/**`
- `cpf-docs/deliverables/**`
- `cpf-docs/assets/manuals/**`
- `cpf-docs/assets/readme/**`
- `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`

위 경로의 PDF/DOCX/MD/이미지/README를 생성·수정·이동·삭제하지 않는다.
Product API/화면/설정 변경으로 문서 영향이 생기면 `DOCUMENT_IMPACT.csv`에 기록만 한다.

### 개발 결과 작성 가능 범위

- Product Source와 직접 결합된 기술 metadata/catalog/schema
- 중앙이 지정한 신규 개발 결과 경로(권장 `cpf-docs/work/v9i/dev-final/**`)
- `PROJECT_DOCUMENT_ALIGNMENT_REQUEST.csv`
- `DOCUMENT_IMPACT.csv`

### 중앙 정본 수정 금지

개발GPT는 다음을 임의 수정하지 않는다.

- `cpf-docs/governance/**`
- `cpf-docs/work/v9i/final-control/**`
- `cpf-docs/work/v9i/qa/final-a/**`
- `cpf-docs/work/v9i/qa/final-b/**`
- 중앙 Architecture/Specification 제품 계약 정본

정본 문구가 모호하거나 Source/Acceptance와 충돌하면 Product Requirement를 약화하거나 자기 해석으로 고치지 말고
`PROJECT_DOCUMENT_ALIGNMENT_REQUEST.csv`에 document path, section/requirement id, ambiguity, related source/consumer/test,
developer interpretation, recommended canonical text, impact, severity, basis SHA를 기록한다.

중앙 관리자가 프로젝트 정본을 현행화한다.
매뉴얼/README는 별도 Documentation Finalization 작업이 처리한다.

## 2-A. 프로젝트 정본 Owner

프로젝트 목표, Canonical Requirement, Governance, Architecture/Specification 제품 계약, Module Ownership,
Final Control, QA Merge와 정본 간 충돌 판정의 Owner는 **중앙 관리자**다.

개발GPT는 정본 문제를 발견하면 `PROJECT_DOCUMENT_ALIGNMENT_REQUEST.csv`로 보고하고 Final Product Source 개발을 계속한다.
중앙 정본을 직접 고치거나 현재 Source에 맞춰 Requirement를 약화하지 않는다.

## 3. Git/삭제 안전

사용자 승인 없이 Commit/Push/Branch/Tag/PR/Release/Reset/Restore/Stash/Clean/History 변경/파일 삭제/이동 금지.

광범위 명령 금지:
- `git clean`
- `git reset --hard`
- `git restore .`

PRODUCT_ADM/MERGE_EDU 13개의 물리 삭제가 최종적으로 필요해도 직접 삭제하지 않는다.
삭제 없이 runtime concrete handler behavior/registration/duplicate logic을 제거하고, 추가 물리삭제가 필요하면 정확한 Root 상대경로를 `DELETE_MANIFEST.csv`에 기록한다.

## 4. QA A/B 중앙 Merge 결과

| 중앙 ID | 심각도 | QA 원본 | 영역 | 반드시 닫을 항목 |
|---|---|---|---|---|
| `CENTRAL-FINAL-001` | P0 | `QA-A-FINAL-NEW-001` | Governance | Canonical Requirement count stale |
| `CENTRAL-FINAL-002` | P0 | `QA-A-FINAL-NEW-002` | Evidence/Exact SHA | Previous-SHA evidence can be promoted incorrectly |
| `CENTRAL-FINAL-003` | P0 | `QA-A-FINAL-NEW-003` | Approval/Recovery | Approval terminal writes are not fenced |
| `CENTRAL-FINAL-004` | P0 | `QA-A-FINAL-NEW-004` | Approval/Center-Cut | Center-Cut non-terminal states can resolve UNKNOWN as success |
| `CENTRAL-FINAL-005` | P0 | `QA-B-FINAL-NEW-003` | Approval/Batch Runtime | Batch UNKNOWN reconcile uses substring identity |
| `CENTRAL-FINAL-006` | P0 | `QA-A-FINAL-NEW-005` | Transaction/Security | Untrusted caller can inject internal transactionId |
| `CENTRAL-FINAL-007` | P0 | `QA-A-FINAL-NEW-006` | Architecture | cpf-core depends on MyBatis-owned mapper implementation |
| `CENTRAL-FINAL-008` | P0 | `QA-A-FINAL-NEW-007` | Logging/Security | DB summary logging is not fail-closed for masking |
| `CENTRAL-FINAL-009` | P0 | `QA-A-FINAL-NEW-008` | DB3/Canonical | V107 lineage missing from canonical platform-schema |
| `CENTRAL-FINAL-010` | P0 | `QA-A-FINAL-NEW-009` | Verification/DB3 | DB3 verifier can pass without canonical V107 |
| `CENTRAL-FINAL-011` | P1 | `QA-A-FINAL-NEW-010` | Transaction/Logging | Lineage source-of-truth/writer ambiguity |
| `CENTRAL-FINAL-012` | P0 | `QA-A-FINAL-NEW-011` | BZA/OpenAPI | Retired 410 APIs still advertised as active 200 operations |
| `CENTRAL-FINAL-013` | P1 | `QA-A-FINAL-NEW-012` | BZA/OpenAPI | Standard error/security responses missing |
| `CENTRAL-FINAL-014` | P0 | `QA-A-FINAL-NEW-013` | FileLog/Recovery | Spool replay bypasses hardened writer safety |
| `CENTRAL-FINAL-015` | P0 | `QA-A-FINAL-NEW-014` | FileLog/Recovery | Dedup disabled above 8 MiB |
| `CENTRAL-FINAL-016` | P0 | `QA-B-FINAL-NEW-004` | FileLog/Recovery | Spool root/retry lifecycle is not truly durable |
| `CENTRAL-FINAL-017` | P1 | `QA-A-FINAL-NEW-015 + QA-B-FINAL-NEW-005` | Verification/FileLog | FileLog recovery tests/gates are false-green |
| `CENTRAL-FINAL-018` | P0 | `QA-A-FINAL-NEW-016` | Verification/Observability | Observability qualifier accepts synthetic authoritative-looking records |
| `CENTRAL-FINAL-019` | P0 | `QA-A-FINAL-NEW-017` | Verification/Security | Security negative gate has labels without attack semantics |
| `CENTRAL-FINAL-020` | P0 | `QA-A-FINAL-NEW-018` | Verification/Performance | Resource gate ignores observed vs declared limits |
| `CENTRAL-FINAL-021` | P1 | `QA-A-FINAL-NEW-019` | Verification/Batch | Batch semantic gate can pass on booleans |
| `CENTRAL-FINAL-022` | P1 | `QA-A-FINAL-NEW-020` | Verification/Broker | Broker semantic gate can pass on booleans |
| `CENTRAL-FINAL-023` | P0 | `QA-A-FINAL-NEW-021` | Verification/DR | DR gate accepts self-attested RPO/RTO |
| `CENTRAL-FINAL-024` | P0 | `QA-A-FINAL-NEW-022` | DB3/Runtime | DB3 lifecycle default runner does not exist |
| `CENTRAL-FINAL-025` | P1 | `QA-A-FINAL-NEW-023` | ADM/Security | ADM CSP allows unpkg and unsafe-eval |
| `CENTRAL-FINAL-026` | P0 | `QA-A-FINAL-NEW-024` | Release/Runtime | Mandatory Runtime Qualification remains incomplete |
| `CENTRAL-FINAL-027` | P1 | `QA-A-FINAL-NEW-025 + QA-B-FINAL-NEW-002` | EDU/Architecture | 13 PRODUCT_ADM/MERGE_EDU concrete handlers remain |
| `CENTRAL-FINAL-028` | P0 | `QA-B-FINAL-NEW-001` | EDU/Role | Retained EDU-ADM extension role differs from canonical ADM role |
| `CENTRAL-FINAL-029` | P1 | `QA-B-FINAL-NEW-006` | Transaction/Operations | One-shot freshness treats N/A sources as missing |
| `CENTRAL-FINAL-030` | P0 | `QA-B-FINAL-NEW-007` | ADM/Frontend | HIGH/CRITICAL mutations bypass generated client |
| `CENTRAL-FINAL-031` | P1 | `QA-B-FINAL-NEW-008` | BZA/Permission | Approval Simulation lacks explicit action permission/generated client |

각 중앙 Action의 상세 `required_fix`와 `acceptance`는 동봉된 `CENTRAL_FINAL_ACTIONS.csv`를 정본으로 사용한다.

## 5. P0 실행 우선순위 — 그러나 P0만 끝내고 멈추지 않는다

### 5.1 Approval/UNKNOWN/Recovery

반드시 함께 닫는다.

1. Approval reservation의 `FENCE_TOKEN`, lease owner, command identity를 실제 invocation context까지 전달
2. 모든 terminal write:
   - success
   - failed
   - UNKNOWN
   - integrity failure
   - request finalization
   에 current fence predicate 적용
3. stale owner가 late response로 새 실행을 덮지 못함
4. Center-Cut `RUNNING/RETRYING/PENDING`은 절대 success terminal이 아님
5. Batch Runtime reconcile에서 문자열 `contains()` identity matching 제거
6. exact structured identity:
   - commandRequestId/operationId
   - approvalRequestId
   - idempotencyKey/request hash
   - target type/id
   를 equality로 검증
7. process kill/lost response/race/collision/duplicate side effect Test 추가

Acceptance:
- stale fence mutation은 0 row
- wrong log row substring collision은 match 불가
- non-terminal state는 UNKNOWN 유지
- recovery가 mutation 재실행 없이 observation-only로 확정

### 5.2 Transaction/Logging/Security

1. **정식 거래 기동 Channel 또는 최초 기동 System은 CPF 규격 transactionId를 최초 1회 생성할 수 있음**
2. 이후 동일 거래의 Local/Remote/REST/SOAP/Gateway/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile 전체는 같은 transactionId를 승계·보존
3. System hop이나 Retry마다 새 transactionId 생성 금지; 하위 호출/재시도는 segmentId/parentSegmentId/attempt/traceId/spanId 등으로 구분
4. transactionId 신뢰 여부를 Header 존재·형식만으로 결정하지 않고 인증된 Channel/System identity, 호출 경로와 trust policy로 검증
5. 정식 Channel/System transactionId는 검증 후 그대로 수용하고, 비신뢰 Client의 타 거래 ID spoof/replay/manipulation만 차단 또는 신규 거래로 격리
6. 외부 기관 자체 correlation은 필요 시 별도 field로 보존
7. DB transaction summary는 persistence 직전 fail-closed masking
8. `cpf_transaction_lineage`를 canonical normalized operational projection/index로 확정하고 실제 idempotent writer/upsert path 구현
9. lineage writer는 transaction, segment/parent, attempt, remote, message/DLQ, batch, UNKNOWN/reconcile을 연결
10. ADM one-shot freshness는 `NOT_APPLICABLE`과 `MISSING/FAILED/STALE`를 구분

Acceptance:
- 공식 Channel 생성 transactionId가 Backend→Remote→Message→Batch/Async까지 동일하게 유지
- Retry는 transactionId 동일 + attempt 증가
- UNKNOWN/Reconcile은 원 transactionId 유지
- untrusted client의 기존 transactionId replay/spoof는 타 lineage에 편입되지 않음
- 모든 inbound transactionId를 일괄 재생성하는 구현이 없음
- malicious raw secret persistence negative Test
- pure local transaction이 FILE/DLQ 없음 때문에 partial로 표시되지 않음
- lineage projection이 실제 writer에 의해 채워지고 중복 write가 idempotent

### 5.3 Core Ownership

`cpf-core`가 persistence-mybatis의 mapper implementation을 import하는 구조를 제거한다.

- core: API/SPI/contract
- downstream persistence provider: mapper/JDBC/MyBatis implementation
- AutoConfiguration/Starter가 구현 결합
- Public BOM/Internal visibility 재검토
- reverse/circular dependency gate

Acceptance:
- core-only compile/publication
- Starter 제거 상태에서도 core source compile graph 정상
- external module의 Internal package 참조 0

### 5.4 DB3 Canonical V107/V108

Canonical-first로 정리한다.

- `platform-schema.json`에 lineage
- Oracle/PostgreSQL/MariaDB
- V107/V108
- rollback
- install
- upgrade
- reapply
- index/FK/constraint
- archive/purge/retention/partition
- runtime query
- permission/default deny
- generator parity
- real DB lifecycle runner

`verify-r6j-transaction-db3.py`는 Vendor SQL token 검사만 해서는 안 된다.

Mutation:
- canonical lineage 제거
- 한 Vendor table/index 제거
- rollback 제거
- writer 제거
- duplicate canonical entry
를 모두 실패시켜야 한다.

### 5.5 FileLog Recovery

기존 `CpfFileLogRecoverySpool` 구현을 부분 보완으로 끝내지 않는다.

필수:
- `java.io.tmpdir` default 제거
- 운영 지속성 있는 configured managed spool root
- startup scan
- bounded autonomous background retry
- lease/concurrency
- backoff
- shutdown drain
- terminal-loss metric/alert
- corruption quarantine
- partial write
- checksum
- masking
- hardened writer safety 재사용
- root/path canonicalization
- NOFOLLOW/symlink defense
- file lock/permission
- >8MiB에서도 동작하는 durable dedup ledger/index
- rotation/restart/concurrent replay

Test:
- hard kill
- restart 후 신규 로그 0건 상태에서도 replay
- disk full
- read-only
- permission denied
- symlink swap
- duplicate replay
- >8MiB
- corrupt spool
- partial record
- concurrent writer

### 5.6 Verification False-Green 제거

이번 QA A는 실제로 다음 fake evidence가 통과하는 것을 재현했다.
이것을 단순 문구 검사 강화로 고치지 않는다.

#### Observability
한 synthetic server가 Probe/Metric/Log/Trace/Alert/Audit를 모두 조작해도 PASS하지 못하도록:
- 독립 authenticated source
- sourceSha
- immutable record ID
- workload-side independently produced evidence
- cross-store correlation
- freshness

#### Security Negative
category label만 있는 request + always-403 server로 PASS 금지.
실제 category-specific payload와 semantic assertion 필요.

#### Resource
declared limit와 observed numeric value 비교 필수.

#### Batch/Broker
boolean proof 금지.
execution/message identity와 state transition, fault/reconnect/backpressure 실제 관측 필수.

#### DR
self-attested RPO/RTO 금지.
독립 harness가 fault/restore/hash/time을 측정.

모든 기존 QA fake evidence 패턴을 regression fixture로 넣어 Mutation이 살아남지 않게 한다.

### 5.7 ADM/BZA Frontend/OpenAPI

#### ADM
- HIGH/CRITICAL mutation은 generated typed client가 canonical
- raw `admMutation`, `admInvokeOperation`로 high-risk 우회 금지
- wrapper가 필요하면 generated operationId를 감싸는 typed adapter이며 gate가 추적 가능해야 함
- `runtimeControl`, `maintenance`, `breakGlass`, `incidents` 포함 Repository 전체 재검색
- CSP에서 external runtime CDN/unpkg, unsafe-eval 제거

#### BZA
- retired 4 approval API는 active OpenAPI/client/consumer count에서 제거
- compatibility backend 410은 유지 가능
- standard error responses: 401/403/404/409/422/429/500/503 where applicable
- Approval Simulation에 explicit SIMULATE permission
- backend direct-call enforcement
- generated typed client

Backend → Runtime OpenAPI → checked-in OpenAPI → Generated Client → Frontend Consumer zero-drift를 검증한다.

### 5.8 EDU/Reference

현재 Architecture decision은 변경하지 않는다.

- PRODUCT_ADM 9
- EXTENSION_SAMPLE 4
- MERGE_EDU 4

#### 02/03/04/07
실행 가능한 4개 Extension Sample의 required role은 canonical `CPF_ADM_OPERATOR`.

#### Product/Merge 13
사용자 삭제 승인 없이 파일을 삭제하지 않는다.
대신:
- handler interface 구현 제거
- Spring Bean/Registry 등록 제거
- executable consumer 제거
- ADM Product logic 복제 제거
- non-runtime reference/redirect metadata로 축소

QA37 `--compile`을 약화하지 않는다.

그리고 QA B가 direct-open하지 못한 나머지 EDU handler 118개도 개발 자체검수에서 clean source 기준으로 전부 검사한다.
대표 샘플링 금지.

## 6. Canonical 169 전체 재검수

31개 Action이 끝난 뒤 `169` Requirement를 ID별로 다시 검사한다.

각 ID마다 적용 가능한:
- Source
- Consumer
- API/SPI
- SQL
- Config
- Frontend
- Generator
- Generated Domain
- Test
- Error/Boundary
- UNKNOWN/Recovery
- Security
- Runtime
- Evidence
를 판정한다.

`93` 원장은 169를 대신할 수 없다.
Canonical 169에서 구현 누락을 발견하면 `DEV-FINAL-SELF-###`로 즉시 추가하고 이번 요청에서 수정한다.

## 7. Runtime Qualification 13축

다음 13축을 최종 Candidate 기준으로 모두 준비한다.

1. Java25 + Gradle9.1 clean build/test/publication/regeneration zero-diff
2. Oracle live lifecycle
3. PostgreSQL live lifecycle
4. MariaDB live lifecycle
5. ADM authenticated Chromium/Firefox/WebKit
6. BZA authenticated Chromium/Firefox/WebKit
7. Approval 2+ instance/process-kill/UNKNOWN reconcile
8. Broker/network/DB-finalization fault injection
9. Performance/resource/backpressure
10. Security negative
11. DR/backup/restore
12. Generator create→runtime→remove→regenerate + full ADM operation closure
13. Transaction/Logging E2E lineage + independent Codex/release qualification

Repository 기존 Matrix가 13축을 다르게 묶는 경우 **기존 canonical 13행을 유지**하되 위 Acceptance가 누락되지 않게 세부 sub-gate로 포함한다.

중요:
- 가능한 환경은 먼저 실제 실행
- 실행 불가를 PASS로 기록 금지
- hard-coded `cd5bacc...` 제거
- Candidate Working Tree 검증은 basis SHA + package hash로 기록
- 사용자 적용/Push 후 successor exact SHA에서 그대로 재실행 가능해야 함
- QA가 재작성할 필요 없이 한 명령/한 Matrix로 재현 가능해야 함

## 8. Error 처리 방식

첫 실패만 고치지 않는다.

1. 모든 저비용 Gate 실행
2. 실패 전체 수집
3. Root Cause별 묶기
4. 동일 원인 Repository-wide 검색
5. Source/Test/Gate/Catalog/Schema 동시 수정
6. 최소 Gate 재실행
7. 전체 Gate 재실행
8. 새 실패 반복

단일 오류마다 사용자에게 명령/로그 요청 금지.

## 9. Documentation Impact만 기록

Product 변경이 고객 문서에 미치는 영향은 `DOCUMENT_IMPACT.csv`에 다음만 기록한다.

- product change
- impacted README/manual/deliverable
- required documentation update
- source/API/Config reference
- severity
- successor verification needed

**실제 README/PDF/DOCX/manual은 수정하지 않는다.**

## 10. 완료 조건

개발GPT가 `완료`라고 표현하려면 최소:

- Canonical 169 모두 재판정
- 중앙 신규 Action 31/31 구현/검증 가능한 Gate 완료
- 기존 중앙 Finding 56 재대조
- self-found 4 재대조
- 신규 DEV-FINAL-SELF 전부 해결
- P0/P1 구현 가능한 미해결 0
- 부분 구현 0
- 미구현 0
- Consumer 단절 0
- Architecture ambiguity 0
- False-Green known reproducer 0
- DB3 canonical drift 0
- high-risk raw client bypass 0
- 외부 transactionId spoof 0
- stale Approval owner overwrite 0
- FileLog durability known gap 0
- 실행 가능한 Runtime Gate 실패 0
- Evidence path/hash 불일치 0

외부 환경 때문에 Runtime 일부를 못 돌리더라도 **Source/Test/Script/Gate/Harness는 100% 완성**해야 한다.
그 경우 `Product Source Development Complete / Release Runtime Unverified`처럼 분리해 쓰고 프로젝트 전체 PASS라 하지 않는다.

## 11. 최종 산출물

Root-relative ZIP 하나.

필수:
- `REVIEW_INDEX.md`
- `DEVELOPMENT_RESULT.md`
- `DEVELOPMENT_SESSION_RESULT.csv`
- `CANONICAL_169_STATUS.csv`
- `CENTRAL_FINAL_ACTION_RESULT.csv`
- `PREVIOUS_56_FINDING_RESULT.csv`
- `SELF_DISCOVERED_FINDINGS.csv`
- `SOURCE_CONSUMER_TRACE.csv`
- `APPROVAL_RECOVERY_MATRIX.csv`
- `TRANSACTION_LOGGING_MATRIX.csv`
- `FILELOG_RECOVERY_MATRIX.csv`
- `DB3_MATRIX.csv`
- `ADM_BZA_CONSUMER_MATRIX.csv`
- `EDU_135_MATRIX.csv`
- `RUNTIME_QUALIFICATION_MATRIX.csv`
- `FALSE_GREEN_MUTATION_RESULT.csv`
- `DOCUMENT_IMPACT.csv`
- `CHANGE_MANIFEST.csv`
- `TEST_AND_EVIDENCE.md`
- `OPEN_ISSUES.md`
- `DELETE_MANIFEST.csv`
- `PACKAGE_MANIFEST.json`
- `SHA256SUMS.txt`
- `CODEX_REVIEW_REQUEST.md`
- `HANDOVER.md`
- 실제 변경 Source/SQL/API/Test/Config/Frontend/Generator/Script

금지된 README/Guide/Deliverable 파일이 ZIP에 있으면 Packaging FAIL.

## 12. 최종 자체검수

ZIP 생성 전:
- `git diff --check`
- secret/hygiene
- ownership/dependency
- duplicate catalog
- API/OpenAPI/client
- DB3 canonical parity
- EDU catalog/registry/role
- false-green mutation
- Manifest/hash
- protected documentation paths 변경 0
- deletion 0
- working tree change inventory

ZIP 생성 후:
- CRC/testzip
- internal SHA256SUMS
- file count
- max path
- forbidden documentation path 0
- missing manifest path 0

## 13. 종료 금지 문구

다음 표현으로 작업을 끝내지 않는다.

- “31건은 완료했다”
- “P0는 끝났다”
- “나머지는 QA가 찾으면 된다”
- “Runtime은 환경이 없으므로 개발도 다음 회차”
- “현재 Scope 밖”
- “문서는 다음 개발에서”
- “일부만 구현했고 나머지는 후속”

**이번 요청은 CPF Product Source의 마지막 개발이라고 생각하고, 구현 가능한 미비점을 전부 끝낸다.**
