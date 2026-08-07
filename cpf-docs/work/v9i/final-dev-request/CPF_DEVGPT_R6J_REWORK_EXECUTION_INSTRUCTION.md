# CPF 개발GPT R6J 통합 재개발·자체검수 실행지침

## 0. 실행 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- QA A/B 기준 SHA: `3ed676061246c9db3e44f29e254c0393ecca3929`
- 현재 중앙 확인 SHA: `3ed676061246c9db3e44f29e254c0393ecca3929`
- R6I 개발 baseline: `64049044956924032360fa80be83b5e37c64f828`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 중앙 통합: `cpf-docs/work/v9i/qa/r6j/CENTRAL_MERGED_QA_REPORT.md`
- 통합 Findings: `cpf-docs/work/v9i/qa/r6j/CENTRAL_INTEGRATED_FINDINGS.csv`
- 중앙 93행 원장: `cpf-docs/work/v9i/qa/r6j/CENTRAL_REQUIREMENT_STATUS.csv`
- 직접 재개발 34행: `cpf-docs/work/v9i/final-dev-request/DEVELOPMENT_REWORK_REQUIREMENTS_R6J.csv`
- 거래/로그 표준: `cpf-docs/work/v9i/post-r6i/LOGGING_TRANSACTION_QA_STANDARD.md`

**작업 시작 즉시 latest origin/master exact SHA와 Working Tree를 다시 확인한다.**
`3ed676061246c9db3e44f29e254c0393ecca3929`를 최신이라고 가정하지 않는다.

현재 상태는 **RELEASE_BLOCKED**다.
Developer의 과거 `77/77 완료`를 승계하지 않는다.

## 1. 개발GPT 역할

이번 작업은 QA 문구 수정이 아니라 실제 제품 재개발이다.

반드시:
`Requirement → actual Source → Consumer → 호출경로 → 정상/오류/경계 → 부분실패/UNKNOWN → 복구 → Security/Audit → DB/Frontend/Generator → Test → Runtime Gate → Evidence`
까지 닫는다.

Interface/DTO/Sample/Swagger/Test 존재만으로 완료 처리하지 않는다.

### 자율 발견 의무

QA가 준 exact ID만 고치고 멈추지 않는다.
분석·개발·검증 중 추가 문제, 잘못된 Ownership, Dead/Stale/Duplicate Source, Consumer 단절, false-green, Security/Recovery/Logging/DB/Frontend/Generator/Artifact 결함을 발견하면 별도 지시를 기다리지 말고:
1. 동일 Root Cause의 잠복 결함을 Repository 전체에서 검색
2. 필요한 Source/Test/SQL/API/Config/Frontend/Generator/Script 동시 보완
3. 자체 발견 Requirement를 `DEV-R6J-SELF-*`로 기록
4. Evidence/Manifest/원장에 추가
한다.

사용자 승인 필요한 Git 쓰기/삭제/보호경로 변경은 실행하지 않는다.

## 2. QA 결과 중앙 판정

QA A:
- 40 regression 전수
- 10 new
- 21 Source-resolved/runtime-unverified
- 14 FAIL
- 5 PARTIAL
- fully closed 0

QA B Deep:
- 40 regression 전수
- 8 new
- 7 static pass / 41 fail
- ADM 63
- BZA 26
- EDU 135
- Source probe 156
- Evidence 14/14 missing

중앙:
- 기존 40 + unique new 16 = **56 Findings**
- P0 **44** / P1 **11** / P2 **1**
- 중앙 원장 = historical 77 + new 16 = **93**
- 직접 Source/Contract/Gate 재개발 = **34 exact IDs**
- Source 개선으로 보이는 나머지도 Runtime/Evidence 전까지 Close 아님

B의 static PASS 7건은 삭제하지 말고 회귀 보호한다. A의 stricter 판정에 따라 target runtime/evidence는 계속 필요하다.

## 3. 중앙 Architecture 결정 — EDU-ADM

ADM은 CPF가 완제품으로 제공하는 Product다.
도입 개발자가 ADM 본체를 다시 개발하는 구조가 아니다.

EDU는 실제 adopter가 개발해야 하는 Public API/SPI/Extension/Integration 예제만 유지한다.
숫자 17/135 자체를 목표로 하지 않는다.

| EDU ID | 중앙 결정 | 처리 |
|---|---|---|
| EDU-ADM-01 | MERGE_EDU | ADM reuse 판단을 generic ADM 복제 대신 Public Extension 선택 가이드/기존 extension EDU로 통합 |
| EDU-ADM-02 | EXTENSION_SAMPLE | 고객 업무 Query/Integration을 공식 Public Query/Extension 계약 예제로 유지 |
| EDU-ADM-03 | EXTENSION_SAMPLE | 고객 확장 Command를 공식 Public Command/Extension 계약 예제로 유지 |
| EDU-ADM-04 | EXTENSION_SAMPLE | 승인 엔진 복제 금지; 고객 확장 위험조치가 ADM Approval Public Integration을 연결하는 예제로 한정 |
| EDU-ADM-05 | MERGE_EDU | 비동기/응답유실은 ADM 전용이 아닌 공통 async/reconcile EDU와 통합 |
| EDU-ADM-06 | MERGE_EDU | 부분성공/복구는 공통 resilience/bulk recovery EDU와 통합 |
| EDU-ADM-07 | EXTENSION_SAMPLE | 공식 ADM Custom Screen/Extension surface가 Public 계약일 때만 유지 |
| EDU-ADM-08 | PRODUCT_ADM | 권한/데이터범위/Masking/사유는 ADM Product 보안 기능에서 검증 |
| EDU-ADM-09 | MERGE_EDU | Optimistic lock 일반 패턴은 공통 concurrency EDU로 통합; ADM UX는 Product 검증 |
| EDU-ADM-10 | PRODUCT_ADM | 운영 Bulk Action/부분실패/결과파일은 ADM Product 기능 |
| EDU-ADM-11 | PRODUCT_ADM | 설정/Feature/Maintenance/LKG rollback은 ADM Product 기능 |
| EDU-ADM-12 | PRODUCT_ADM | Incident/Recovery Center lifecycle은 ADM Product 기능 |
| EDU-ADM-13 | PRODUCT_ADM | 감사증적/승인반출/다운로드는 ADM Product 기능 |
| EDU-ADM-14 | PRODUCT_ADM | Topology/Health/Capacity drill-down은 ADM Product 기능 |
| EDU-ADM-15 | PRODUCT_ADM | Log/Trace/Transaction correlation은 ADM Product 핵심 기능 |
| EDU-ADM-16 | PRODUCT_ADM | Alert ACK/Escalation/Handover는 ADM Product 운영 기능 |
| EDU-ADM-17 | PRODUCT_ADM | Browser session expiry/relogin/risky action safety는 ADM/Security Product 기능 |

### Architecture 적용 규칙

- PRODUCT_ADM: 해당 동작을 ADM Product Source/API/Frontend/Test/Runtime/Manual에서 완성한다. cpf-reference generic mimic을 canonical EDU로 유지하지 않는다.
- EXTENSION_SAMPLE: 실제 공식 Public API/SPI/Extension contract가 존재할 때만 예제로 유지한다. Product engine을 복제하지 않는다.
- MERGE_EDU: ADM 이름의 독립 generic handler를 유지하지 말고 기존 공통 DEV/Resilience/Concurrency 패턴과 통합한다.
- 삭제가 필요한 Source는 `DELETE_MANIFEST.csv`에 exact root-relative path로 제안만 한다. 사용자 승인 전 실제 삭제 금지.
- 다른 EDU 118개도 작업 중 Product duplication/실제 consumer 부재를 발견하면 자체 Architecture Finding으로 추가한다.
- 전체 EDU canonical count는 재분류/통합 완료 후 Catalog에서 재산정한다. 135를 맞추기 위한 dummy ID 증설 금지.

## 4. 직접 재개발 34 exact IDs

| ID | 우선순위 | 영역 | 제목 |
|---|---|---|---|
| AB-R6-001 | P0 | Management/Quality | Current result SHA와 R6S12 Evidence provenance 미결속 |
| AB-R6-002 | P0 | Runtime/Release | Current master Push에 Release Workflow 실행 없음 |
| AB-R6-010 | P0 | ADM/Frontend/Contract | Backend validation과 committed ADM OpenAPI/Generated artifact drift |
| AB-R6-012 | P1 | ADM/Frontend/Contract | Generated Client가 high-risk 실제 Consumer를 compile-time으로 충분히 구속하지 않음 |
| AB-R6-013 | P0 | Management/Quality | Operation consumer Gate False Green |
| AB-R6-014 | P0 | Management/Quality | R6 Behavior Mutation Gate가 실제 mutation execution이 아닌 tautology |
| AB-R6-020 | P0 | Approval/Security | Process Kill 후 Approval EXECUTING/RUNNING 고착 경로 |
| AB-R6-025 | P0 | Runtime/Release | Owner success 후 DB finalization/DB outage의 durable UNKNOWN 보장 부족 |
| AB-R6-028 | P0 | EDU/Sample | EDU 135 Catalog ↔ Handler/Scenario requiredRole 불일치 |
| AB-R6-029 | P0 | EDU/Sample | EDU 135의 5종 Test가 실제 8종 Product Consumer Runtime을 증명하지 않음 |
| AB-R6-030 | P0 | EDU/Sample | EDU-ADM 17이 요구 의미보다 template/common-state-machine 중심 |
| AB-R6-032 | P0 | EDU/Sample | EDU-ADM-04 승인 교육 예제가 실제 승인 정책/SoD/만료/범위를 구현하지 않음 |
| AB-R6-033 | P0 | EDU/Sample | EDU-ADM-08 보안 교육 예제가 masking/IDOR/browser role matrix를 구현하지 않음 |
| AB-R6-034 | P0 | EDU/Sample | EDU-ADM-09 version conflict/browser flow 의미 불일치 |
| AB-R6-035 | P0 | EDU/Sample | EDU-ADM-10 bulk target semantics가 pseudo partition으로 대체 |
| AB-R6-036 | P1 | EDU/Sample | EDU-ADM-11 maintenance/LKG rollback 의미가 선언에 그침 |
| AB-R6-037 | P0 | EDU/Sample | EDU-ADM-12\~17 핵심 운영 의미가 generic JDBC state machine으로 대체 |
| AB-R6-038 | P0 | EDU/Sample | QA37 EDU Source Closure Gate가 semantic drift를 탐지하지 못함 |
| R6J-CENTRAL-NEW-001 | P0 | Architecture/EDU | ADM Product와 EDU-ADM generic duplicate implementation의 Architecture 충돌 |
| R6J-CENTRAL-NEW-002 | P0 | Runtime/Release | Release Workflow ADM frontend URL preflight 변수명 불일치 |
| R6J-CENTRAL-NEW-003 | P0 | Verification Tool/Frontend | Frontend contract gate가 risky-operation permission bypass mutation을 탐지하지 못함 |
| R6J-CENTRAL-NEW-004 | P0 | Verification Tool/Observability | Observability qualification이 self-attested boolean proof로 false-green 가능 |
| R6J-CENTRAL-NEW-005 | P0 | Management/Requirement | 07_02 거래·파일·DB Logging 신규 정본이 R6I 77행 개발원장에 미반영 |
| R6J-CENTRAL-NEW-006 | P0 | ADM/Transaction Timeline | ADM transactionId one-shot view가 Message/DLQ/Batch/File/Trace/Audit까지 통합하지 못함 |
| R6J-CENTRAL-NEW-007 | P0 | DB/Transaction Logging | Transaction DB schema/query가 trace/span/request/idempotency/tenant/batch/message/file 식별자를 충분히 연결하지 못함 |
| R6J-CENTRAL-NEW-008 | P0 | EDU/Security | EDU runtime authorization이 caller-provided actor/roles/data-scope header를 신뢰 |
| R6J-CENTRAL-NEW-009 | P0 | EDU/Process Security | PROCESS EDU consumer가 parent environment 상속 및 full payload temp JSON 기록 |
| R6J-CENTRAL-NEW-010 | P1 | OpenAPI/Frontend | Approval backend 422와 committed OpenAPI/generated client/frontend error taxonomy drift |
| R6J-CENTRAL-NEW-011 | P0 | BZA/Frontend/Consumer | Retired HTTP 410 Approval GET가 route metadata/workbench real consumer로 계산 |
| R6J-CENTRAL-NEW-012 | P0 | Approval/Recovery | UNKNOWN-producing Approval Owner 다수가 observation reconcile 구현 부재 |
| R6J-CENTRAL-NEW-013 | P0 | ADM/Recovery | RecoveryCenter가 canonical Reliability mutation의 permission/CAS 계약을 약화한 중복 Consumer |
| R6J-CENTRAL-NEW-014 | P0 | Core/Logging | CPF-LOGFAIL durable spool/retry/dedup/loss recovery owner 부재 |
| R6J-CENTRAL-NEW-015 | P1 | ADM/Frontend/Security | HIGH/CRITICAL ADM 전용 화면 action-level permission projection 불균일 |
| R6J-CENTRAL-NEW-016 | P1 | BZA/Frontend/Security | BZA Approval 계열 mutation button action-level permission projection 누락 |

상세 Required Action/Acceptance는 `DEVELOPMENT_REWORK_REQUIREMENTS_R6J.csv`를 따른다.

## 5. 개발 Wave 0 — Baseline / Evidence / Traceability

1. latest master exact SHA/working tree
2. 93행 중앙 원장 수량 검산
3. Developer 과거 evidence 14 log의 부재 원인 판정
4. 새 실행 Evidence는 반드시 current work result source hash와 결속
5. `PENDING_USER_APPLY_COMMIT` 과거 필드는 역사자료로 남기고 새 결과원장에서 result SHA binding 절차 명시
6. QA A/B 결과 문서는 수정하지 않는다.
7. 보호경로/삭제 0 원칙 유지

Evidence 최소:
- command
- cwd
- tool/version
- source SHA
- exit code
- stdout/stderr
- output hash
- failure stage
- rerun condition

## 6. 개발 Wave 1 — Release / Gate False-Green

### 6.1 Workflow 변수
`CPF_FRONTEND_URL` vs `CPF_ADM_FRONTEND_URL` 불일치를 단일 canonical 변수로 정리한다.
workflow/preflight/runner/browser config가 동일 값을 사용해야 한다.

### 6.2 Frontend permission verifier
`canAction() => true`, button grant 제거, direct API call 우회 등 semantic mutation을 실제로 죽여야 한다.

### 6.3 Observability verifier
self-attested JSON boolean을 신뢰하지 않는다.
Known traffic/failure를 생성하고 authoritative metric/log/trace/alert/audit store에서 독립 조회한다.

### 6.4 Consumer gate
- retired 410 API를 active consumer로 계산 금지
- stale duplicate consumer 금지
- high-risk mutation은 exact generated operation + action permission + CAS + reason/audit consumer를 요구

### 6.5 Behavior mutation
token deletion만으로 PASS하지 않는다.
semantic-preserving bypass, unconditional true, wrong owner, retry/UNKNOWN skip, stale version bypass mutation을 포함한다.

## 7. 개발 Wave 2 — Transaction / File Log / DB Log P0

이 Wave는 이번 개발의 최우선 Product 기능이다.

### 7.1 Transaction lineage
모든 거래는 canonical transactionId를 유지한다.

필수:
- 거래→거래 nested call
- local→remote
- REST/SOAP/fixed/file/webhook
- Gateway
- async
- Message producer/consumer/retry/DLQ
- Batch/Scheduler/Center-Cut
- process kill/retry/reconcile

동일 transactionId 아래에서:
- segmentId
- parentSegmentId
- attempt
- traceId/spanId
- requestId/idempotencyKey
- tenant/channel/actor
- instance/was/agent/worker
를 연결한다.

외부 Client가 내부 transaction/security/instance header를 임의 주입해 권한이나 추적정보를 위조하지 못하게 trust boundary를 명확히 한다.

### 7.2 ADM transactionId one-shot 조회
ADM 사용자는 **transactionId 하나만 입력해서 전체 거래 흐름을 한 번에 조회**할 수 있어야 한다.

반드시 집계:
- 최초 request/result
- nested local segment
- remote/external attempts
- Message producer/consumer/retry/DLQ
- Batch jobInstance/jobExecution/step/partition/item/worker
- Center-Cut/Scheduler
- File/Remote log
- Trace
- Audit
- error/failureStage
- UNKNOWN/reconcile
- source freshness/partial/stale/missing

UI:
- timeline + tree
- paging/detail
- error highlight
- attempt/retry
- secure deep link
- raw download permission/reason/audit
- 401/403/404/409/422/429/500/503

Generic Workbench로 대체하지 말고 실제 Product Consumer를 연결한다.

### 7.3 DB3 transaction logging
Oracle/PostgreSQL/MariaDB Canonical Source를 동시에 맞춘다.

필수 식별:
`transactionId, traceId, spanId, segmentId, parentSegmentId, attempt, requestId, idempotencyKey, tenant, channel, instance, remoteSystem, operation, batch ids, message ids, file ids`

필수:
- transactionId lookup index
- time/segment ordering
- source joins
- append/idempotency/duplicate
- retention/partition/archive/purge
- empty install
- migration
- runtime query
- rollback/forward
- large lookup performance

### 7.4 File Log durable failure recovery
현재 `CpfFileLogWriter`의 `counter + warn + false`만으로 CPF-LOGFAIL을 완료 처리하지 않는다.

구현:
- bounded durable spool
- sequence/checksum
- retry with backoff
- dedup/idempotency
- poison/quarantine
- retransmit
- terminal loss counter
- alert/ADM visibility
- disk-full
- read-only
- permission denied
- lock timeout
- process kill/restart
- shutdown drain

업무 로그와 법적/보안 audit의 fail-open/fail-closed 정책을 구분한다.
민감 Payload는 spool/temp/evidence에도 원문 금지.

## 8. 개발 Wave 3 — Approval / UNKNOWN / Recovery

- 모든 UNKNOWN-producing Owner를 목록화
- BAT/Gateway/Broker/Center-Cut/DataQuality 등 각 Owner에 observation-only reconcile 구현
- execute 재호출로 reconcile 금지
- owner side effect 성공 후 DB finalization outage
- lost response
- process kill
- lease expiry
- multi-instance takeover
- duplicate reconcile
를 검증한다.

RecoveryCenter와 Error/Reliability Workbench의 중복 mutation 계약을 하나의 canonical command DTO/consumer로 통합하거나 동일:
- action permission
- expectedVersion
- reason
- idempotency
- audit
를 강제한다.

## 9. 개발 Wave 4 — ADM/BZA Product Security / Consumer

ADM:
- 63 routes
- full operation closure
- HIGH/CRITICAL action exact permission
- Secrets/FeatureFlags/OpenAPI/Resilience/Operators/FileJobs
- direct URL + API server permission
- CAS/reason/audit

BZA:
- retired 410 Approval GET active metadata에서 제거
- Inbox/Submissions/Policies/Delegations action permission
- generated client actual consumer
- browser 3-engine role matrix

Frontend error taxonomy에 422를 명시하고 backend/runtime OpenAPI/generated client와 맞춘다.

## 10. 개발 Wave 5 — EDU / Reference Security / Architecture

### 10.1 Security context
`X-Cpf-Actor-Id`, `X-Cpf-Roles`, `X-Cpf-Data-Scope`를 caller authority로 신뢰하지 않는다.
Authenticated framework-owned context에서 actor/role/scope를 공급한다.

### 10.2 PROCESS consumer
- `ProcessBuilder.environment().clear()`
- allowlist
- Secret env 금지
- full command payload temp 기록 금지
- strict permission
- 최소 IPC
- deterministic delete
- process kill/crash scrub
- no evidence secret

### 10.3 QA37 재설계
현재 “cpf-reference self-contained 135”를 정답으로 hard-code하는 Gate를 폐기/재설계한다.
새 Gate는 각 유지 EDU에:
- intended adopter
- public contract
- actual consumer
- owner
- normal/error/recovery
- security
를 검증한다.

Product ADM 내부 Package를 EDU가 직접 참조하는 것도 금지한다.
필요하면 Public Extension contract를 Owner module에 정의한다.

## 11. 개발 Wave 6 — OpenAPI / DB3 / Generator / Generated Domain

- 422 포함 runtime OpenAPI parity
- generated client diff 0
- source-of-truth 단일화
- V105/V106 + 새 logging migration DB3 parity
- Generator create→build/runtime→remove→regenerate
- generated domain/sample/manual/EDU catalog parity
- duplicate catalog detection before map conversion
- Public BOM internal leaf exposure 0

## 12. 개발 Wave 7 — Target Runtime / Release Qualification

환경이 있으면 반드시 실행:
1. Java25 + Gradle9.1 clean build/test/publication
2. DB3 live lifecycle
3. ADM/BZA authenticated Chromium/Firefox/WebKit
4. Multi-instance/process kill/network/broker/DB outage
5. Approval UNKNOWN reconcile
6. Transaction/Logging end-to-end
7. Performance/load/soak/backpressure
8. Observability authoritative telemetry
9. Security negative corpus
10. DR backup/restore/RTO/RPO
11. LOCAL_DEV/REMOTE/OFFLINE artifact consumers
12. Generator DB3 lifecycle
13. Codex independent review
14. Release workflow current result SHA

환경이 없으면:
- 필요한 Source/Test/Script는 구현
- exact environment/command/expected evidence/failure criterion 기록
- `미검증`
- PASS 금지

## 13. 회귀 보호 — Source-resolved 항목

다음 유형은 무작정 재작성하지 않는다.
QA A가 `SOURCE_RESOLVED_RUNTIME_UNVERIFIED`, QA B가 static PASS/SOURCE_IMPROVED로 본 항목은:
1. actual Source 재검토
2. 새 변경 영향 확인
3. targeted regression
4. required runtime/evidence
순서로 닫는다.

재개발하면서 해결된 4D owner tuple, nonce, SecretRef, DB policy overlap, GET-only Workbench, sidebar 63, BZA permission manifest 등을 다시 깨뜨리지 않는다.

## 14. 개발GPT 결과 원장

개발GPT는 QA A/B 원본 컬럼을 수정하지 않는다.

새 개발 원장:
- 기존 93 exact IDs
- 자체 발견 `DEV-R6J-SELF-*`
에 대해:
`development_status`, `verification_status`, `개발GPT_수행`, `개발GPT_내용`, `개발GPT_검증`, `개발GPT_Evidence`, `개발GPT_자체검수`
를 기록한다.

Progress는 반드시:
- direct rework x/34
- central findings source-closed x/56
- central requirement reviewed x/93
- EDU-ADM architecture applied x/17
- ADM routes x/63
- BZA routes x/26
- transaction/logging acceptance x/N
- runtime gates x/13
처럼 분모/분자로 보고한다.

## 15. Checkpoint / 세션 한계

중간 ZIP은 Checkpoint이며 완료가 아니다.
세션 한계가 임박하면 Source 변경, 원장, Evidence, HANDOVER, Manifest, SHA를 Root-relative Windows-compatible ZIP으로 보존하고 계속 이어간다.

최종 완료 시 중간 ZIP을 단순 묶지 말고 **현재 전체 최종 변경을 다시 통합한 Root Overlay ZIP 하나**를 생성한다.

## 16. 필수 결과 파일

최소:
- `REVIEW_INDEX.md`
- `REQUIREMENT_STATUS.csv`
- `DEVELOPMENT_SESSION_RESULT.csv`
- `DEVELOPMENT_RESULT.md`
- `CHANGE_MANIFEST.csv`
- `TEST_AND_EVIDENCE.md`
- `OPEN_ISSUES.md`
- `DELETE_MANIFEST.csv`
- `PACKAGE_MANIFEST.json`
- `SHA256SUMS.txt`
- `CODEX_REVIEW_REQUEST.md`
- `HANDOVER.md`
- `SELF_DISCOVERED_FINDINGS.csv`
- `EDU_ARCH_MIGRATION_MATRIX.csv`
- `TRANSACTION_LOGGING_MATRIX.csv`
- `RUNTIME_QUALIFICATION_MATRIX.csv`

Evidence log는 최종 ZIP/Repository에 포함하거나, 외부 보관 정책이면 immutable URI/hash/source SHA를 원장에 기록한다. 존재하지 않는 `evidence/*.log` 경로를 PASS 근거로 남기지 않는다.

## 17. Git / 삭제 안전

GPT는 Commit/Push/Branch/Tag/PR/Release를 하지 않는다.
`git clean`, `git reset --hard`, `git restore .` 금지.

삭제는 사용자 승인 전 수행 금지.
삭제가 필요하면 exact root-relative path를 `DELETE_MANIFEST.csv`에만 기록한다.

보호:
- `cpf-docs/deliverables/**`
- `cpf-docs/guides/**`
- `cpf-docs/environment/docker/**`
- `cpf-tools/environment/docker-development-test/**`
- `cpf-docs/assets/manuals/**`

## 18. 완료 기준

개발GPT가 “완료”라고 할 수 있는 것은 자신의 Source 구현/자체검수 범위다.
CPF 제품 최종 완료는 QA가 결정한다.

개발GPT 자체 완료 조건:
- direct 34 source/contract/gate rework 모두 처리
- 자체 발견 결함 처리
- 실행 가능한 Gate 실패 0
- 실행 불가 runtime은 정확히 미검증
- stale/generated diff 0
- Evidence 실재/Hash 일치
- delete/protected path 정책 준수
- final ZIP CRC/hash/manifest PASS

QA 최종 PASS를 대신 선언하지 않는다.
