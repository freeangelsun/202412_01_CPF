# CPF QA32 독립 Source 검수 보고서

- Package: `CPF-20260731-QA33-INDEPENDENT-SOURCE-RUNTIME-CLOSURE`
- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 독립 검수 기준 SHA: `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`)
- QA32 개발 Base로 보고된 SHA: `d31bd127aa12bb9368933216642a5a9d25bd0bfd`
- 작성 시각: `2026-07-31T11:12:55+09:00`
- 검수 방식: GitHub exact SHA의 실제 Source·Build 설정·Frontend lock/generated artifact·SQL·Script·Evidence를 개발 보고와 독립 대조
- Git write: 없음. Commit·Push·Branch·Tag·PR을 생성하지 않았다.

## 1. 최종 판정

**QA32 전체 판정: 실패**

현재 상태는 단순히 `Runtime 245건 미검증`이 아니다. 최신 master Source에서 Gradle 설정 실패, 잘못된 Project path, npm lock 불일치, TypeScript compile 오류, generated client 부재, OSS Ownership 이중화, BFF credential 노출, Batch/Kafka/Gateway 상태기계 결함 등이 확인됐다.

정확한 상태:

> **QA32 Source 수정 실패 존재 / Build 시작 전 확정 실패 존재 / Runtime 검증 미완료 / exact-SHA Evidence 부재 / 최종 완료 판정 금지**

## 2. 기준 SHA 정정

`d31bd127...`은 QA32 Source 결과 Commit이 아니라 이전 Amendment 기준이다. 실제 QA32 Source와 결과 문서는 다음 Commit `1536a0d59004ebade7dcb29383cbe2e758547f8e`에 반영됐다. 향후 작업 시작 시 최신 origin/master를 다시 확인하고 이 SHA보다 진행됐으면 전체 finding을 재검증해야 한다.

## 3. 독립 검수 집계

- Defect/Gap: **113건**
- Requirement: **138건**
- Mandatory Scenario: **414건**
- Source Inspection: **115건**
- Evidence Requirement: **28건**

### 결함 분류
- EXACT_SOURCE_RECHECK: 16건
- FALSE_OR_EXCLUDED: 1건
- RUNTIME_UNVERIFIED: 7건
- SOURCE_CONFIRMED: 89건

### 우선순위
- P0: 59건
- P1: 54건

### 초기 상태
- 미검증: 43건
- 실패: 53건
- 재확인 필요: 17건

### 영역별
- Build: 7건
- Supply Chain: 6건
- Evidence: 5건
- Architecture Ownership: 4건
- Frontend: 4건
- Quality Gate: 2건
- Frontend Build: 2건
- Frontend Migration: 2건
- BFF Security: 2건
- CSRF: 2건
- Batch Contract: 2건
- Database: 2건
- Database Rollback: 2건
- Database Migration: 2건
- Baseline: 1건
- Result Matrix: 1건
- Documentation Scope: 1건
- Dependency Ownership: 1건
- Dependency Lock: 1건
- Legacy Removal: 1건
- Frontend State: 1건
- Frontend Error Handling: 1건
- Frontend UX: 1건
- Frontend Routing: 1건
- Session Security: 1건
- Authentication: 1건
- Authorization: 1건
- Session Readiness: 1건
- Security Headers: 1건
- Batch Idempotency: 1건
- Batch Fencing: 1건
- Batch Unknown Result: 1건
- Batch Link: 1건
- Batch Job Identity: 1건
- Batch Memory: 1건
- Batch Integrity: 1건
- Batch Result Semantics: 1건
- Batch Audit Isolation: 1건
- Batch Sensitive Data: 1건
- Batch Approved Definition: 1건
- Batch Remote Chunk: 1건
- Batch Resource Policy: 1건
- Batch Consumer Migration: 1건
- Remote Topology: 1건
- Kafka Multi-instance: 1건
- Kafka Idempotency: 1건
- Kafka Contract: 1건
- Kafka Routing: 1건
- Kafka Delivery: 1건
- Kafka Backpressure: 1건
- Kafka Blocking: 1건
- Scheduler Transaction: 1건
- Scheduler Ownership: 1건
- Scheduler Misfire: 1건
- Scheduler Multi-instance: 1건
- Gateway Retry: 1건
- Gateway Trust Boundary: 1건
- Gateway Completion: 1건
- Gateway Isolation: 1건
- Gateway SSRF: 1건
- Gateway Load Balancing: 1건
- Gateway Unknown Result: 1건
- Gateway Transaction ID: 1건
- Gateway Artifact: 1건
- Database Vendor Parity: 1건
- Deployment: 1건
- Deployment Idempotency: 1건
- Deployment Lock: 1건
- Health Probe: 1건
- Artifact Rollback: 1건
- Artifact Trust: 1건
- Artifact State: 1건
- Artifact Atomicity: 1건
- Artifact Download: 1건
- Bootstrap Secret: 1건
- Bootstrap Recovery: 1건
- Bootstrap Cleanup: 1건
- Archive Memory: 1건
- Archive Atomicity: 1건
- Archive Security: 1건
- Attachment Streaming: 1건
- Batch/Kafka: 1건
- Gateway/Operations: 1건

## 4. P0 Source 확정 결함

1. **QA33-DF-001 / Baseline** — 현재 master가 1536a0d인데 보고서는 d31bd127/WORKTREE_OVERLAY_UNCOMMITTED를 Head로 기록한다.
   - Source: `cpf-docs/work/review/CPF_20260730_QA32_DEVELOPMENT_COMPLETION_REPORT.md`
   - 필수 조치: 최신 exact SHA와 clean worktree 기준으로 보고서·Evidence·Manifest를 재생성하고 stale Overlay 보고를 superseded 처리한다.
2. **QA33-DF-002 / Evidence** — Push된 exact SHA의 QA32 Completion Evidence 정본이 없다.
   - Source: `cpf-docs/evidence/current/CPF_20260730_QA32_COMPLETION_EVIDENCE.json`
   - 필수 조치: sanitized exact-SHA Completion/Requirement/Scenario Evidence와 JSON Schema를 정본 경로에 추가한다.
3. **QA33-DF-003 / Evidence** — QA32 Integrity가 요구한 Completion Evidence Template이 없다.
   - Source: `cpf-docs/evidence/templates/CPF_QA32_COMPLETION_EVIDENCE_TEMPLATE.json`
   - 필수 조치: Template·Schema·negative fixture·integrity gate를 추가한다.
4. **QA33-DF-004 / Evidence** — Requirement Evidence Template이 없다.
   - Source: `cpf-docs/evidence/templates/CPF_QA32_REQUIREMENT_EVIDENCE_TEMPLATE.json`
   - 필수 조치: Requirement/Scenario Evidence 필수 필드를 통일하고 schema gate를 추가한다.
5. **QA33-DF-005 / Evidence** — cpf-docs/evidence/** ignore 규칙과 요구된 일반 .json Evidence 이름이 충돌한다.
   - Source: `.gitignore`
   - 필수 조치: Evidence 파일명/추적 정책을 sanitized 정본으로 통일하고 git check-ignore gate를 추가한다.
6. **QA33-DF-010 / Build** — includeBuild 대상 platform-bom Source가 삭제됐는데 includeBuild가 남아 있다.
   - Source: `settings.gradle;cpf-tools/build/platform-bom/**`
   - 필수 조치: Included Build를 복원하거나 includeBuild를 제거하고 BOM 공급 경로를 단일화한다.
7. **QA33-DF-011 / Build** — includeBuild 대상 gradle-plugin Source가 삭제됐다.
   - Source: `settings.gradle;cpf-tools/build/gradle-plugin/**`
   - 필수 조치: Convention plugin 정본을 복원하거나 표준 plugin 공급 경로로 이관한다.
8. **QA33-DF-012 / Build** — Gateway가 존재하지 않는 :cpf-starters:resilience/:observability를 참조한다.
   - Source: `cpf-gateway/build.gradle;settings.gradle`
   - 필수 조치: 공식 :cpf-starter-resilience/:cpf-starter-platform-operations-observability Project path로 수정한다.
9. **QA33-DF-013 / Build** — ADM이 존재하지 않는 :cpf-starters:security를 참조한다.
   - Source: `cpf-admin/build.gradle;settings.gradle`
   - 필수 조치: 공식 :cpf-starter-security 경로를 사용한다.
10. **QA33-DF-014 / Build** — BZA가 존재하지 않는 :cpf-starters:security를 참조한다.
   - Source: `cpf-biz-admin/build.gradle;settings.gradle`
   - 필수 조치: 공식 :cpf-starter-security 경로를 사용한다.
11. **QA33-DF-017 / Frontend Build** — ADM manifest와 lockfile root dependency가 불일치한다.
   - Source: `cpf-admin/frontend/package.json;cpf-admin/frontend/package-lock.json`
   - 필수 조치: 승인 Registry에서 exact lockfile을 재생성하고 clean npm ci를 통과시킨다.
12. **QA33-DF-018 / Frontend Build** — BZA manifest와 lockfile이 불일치한다.
   - Source: `cpf-biz-admin/frontend/package.json;cpf-biz-admin/frontend/package-lock.json`
   - 필수 조치: BZA exact lockfile을 재생성하고 clean npm ci를 통과시킨다.
13. **QA33-DF-020 / Architecture Ownership** — cpf-core가 Kafka, AMQP, WebFlux, OpenTelemetry SDK/OTLP를 직접 implementation한다.
   - Source: `cpf-core/build.gradle`
   - 필수 조치: Core에는 topology-independent 계약만 남기고 선택 Runtime을 Starter/Adapter Owner로 이관한다.
14. **QA33-DF-021 / Legacy Removal** — Kafka Starter와 Core Kafka/AMQP가 동시에 남아 Dual Primary다.
   - Source: `cpf-core/build.gradle;cpf-starters/messaging-kafka/**`
   - 필수 조치: Kafka를 Product Primary로 단일화하고 AMQP Primary Bean/Consumer/Dependency를 제거한다.
15. **QA33-DF-022 / Architecture Ownership** — cpf-common이 Caffeine/Redis를 직접 소유하면서 Cache Starter도 존재한다.
   - Source: `cpf-common/build.gradle;cpf-starters/cache/**`
   - 필수 조치: Cache 계약과 Local/Distributed Provider를 분리하고 실제 Consumer를 Starter로 이관한다.
16. **QA33-DF-025 / Frontend** — methods.ts가 존재하지 않는 clearAdmAccessToken을 import한다.
   - Source: `cpf-admin/frontend/src/features/core/methods.ts;src/shared/cpfApi.ts`
   - 필수 조치: 삭제된 token API 참조를 제거하고 server session clear 계약으로 통합한다.
17. **QA33-DF-026 / Frontend** — ADM Orval generated client와 source marker가 없다.
   - Source: `cpf-admin/frontend/src/generated/**;scripts/verify-generated-client.mjs`
   - 필수 조치: OpenAPI exact SHA에서 client를 생성하고 generate 후 git diff 0 gate를 적용한다.
18. **QA33-DF-027 / Frontend** — BZA Orval generated client와 marker가 없다.
   - Source: `cpf-biz-admin/frontend/src/generated/**;scripts/verify-generated-client.mjs`
   - 필수 조치: BZA exact-SHA client를 생성하고 Consumer를 이관한다.
19. **QA33-DF-028 / Frontend Migration** — ADM 핵심 API가 직접 fetch/getJson/sendJson을 계속 사용한다.
   - Source: `cpf-admin/frontend/src/**`
   - 필수 조치: 허용된 Orval mutator 외 raw fetch를 제거하고 Query/Mutation Hook으로 이관한다.
20. **QA33-DF-029 / Frontend Migration** — BZA API가 raw fetch wrapper를 계속 사용한다.
   - Source: `cpf-biz-admin/frontend/src/features/auth/session.ts;src/**`
   - 필수 조치: 인증 bootstrap 예외를 명시하고 일반 API를 Orval/TanStack Query로 이관한다.
21. **QA33-DF-034 / BFF Security** — Token을 Session으로 옮긴 뒤 sessionId를 응답 Body에 반환한다.
   - Source: `CpfBffCredentialResponseAdvice`
   - 필수 조치: sessionId를 Body/DOM/log에서 제거하고 Cookie 외 노출을 금지한다.
22. **QA33-DF-035 / BFF Security** — Map이 아니거나 accessToken이 비어 있으면 원 Body를 반환해 credential stripping이 fail-open이다.
   - Source: `CpfBffCredentialResponseAdvice`
   - 필수 조치: Typed BFF response로 고정하고 unknown credential shape는 fail-closed한다.
23. **QA33-DF-036 / Session Security** — Access/Refresh Token 원문을 JDBC Session Attribute에 저장한다.
   - Source: `CpfBffSessionBridgeFilter;SPRING_SESSION_ATTRIBUTES`
   - 필수 조치: Refresh token 암호화/참조화와 access token 최소 수명·회전·DB masking을 구현한다.
24. **QA33-DF-037 / CSRF** — Spring Security 표준 CSRF와 별도 custom filter가 병렬이며 cookie/path가 하드코딩됐다.
   - Source: `CpfBffCsrfFilter;CpfServerSessionSecurityAutoConfiguration`
   - 필수 조치: 표준 CSRF repository/filter chain으로 통합하고 matcher/rotation/origin 정책을 적용한다.
25. **QA33-DF-043 / Batch Idempotency** — idempotency_key가 전역이며 기존 실행 재사용 시 job/definition/approval/plan/parameter 일치를 검증하지 않는다.
   - Source: `JdbcBatchExecutionControlPlaneAdapter.reserve`
   - 필수 조치: scope와 canonical request hash를 저장하고 mismatch replay를 409/fail-closed한다.
26. **QA33-DF-044 / Batch Fencing** — 해당 실행 행의 token만 비교하고 Job/Lease 최신 token을 확인하지 않는다.
   - Source: `JdbcBatchExecutionControlPlaneAdapter.assertCurrent`
   - 필수 조치: 최신 epoch/lease 원장과 모든 side effect 직전·commit 직전 원자 비교한다.
27. **QA33-DF-045 / Batch Unknown Result** — reserve 후 JobOperator.start/bind 실패를 UNKNOWN_RESULT로 기록하거나 대사하지 않는다.
   - Source: `CpfSpringBatchExecutionControl.start`
   - 필수 조치: start 전후 상태기계와 response-loss reconciliation을 구현한다.
28. **QA33-DF-046 / Batch Link** — duplicate 경로가 일부 column만 갱신하고 control update row count를 확인하지 않는다.
   - Source: `JdbcBatchExecutionControlPlaneAdapter.bind`
   - 필수 조치: Vendor별 원자 upsert와 complete field/fence consistency를 보장한다.
29. **QA33-DF-049 / Batch Integrity** — checksum 형식만 확인하고 canonical plan 내용과 일치 여부를 계산하지 않는다.
   - Source: `BatchExecutionPlan`
   - 필수 조치: canonical serialization digest를 승인·로드·실행 시 재계산한다.
30. **QA33-DF-052 / Batch Result Semantics** — STOPPED/RETRYABLE_FAILURE가 예외로 변환되지만 retry/stop 정책이 Step에 연결되지 않았다.
   - Source: `CpfBatchTasklet;CpfBatchJobFactory`
   - 필수 조치: Spring Batch ExitStatus/BatchStatus/fault-tolerant retry/stop semantics를 표준 API로 구현한다.
31. **QA33-DF-053 / Batch Audit Isolation** — Ledger/Fencing 실패가 listener에서 Job/Step 생명주기를 오염시킬 수 있다.
   - Source: `CpfBatchExecutionListener`
   - 필수 조치: 보안 fail-closed와 비오염 관측 기록을 transaction/outbox로 분리한다.
32. **QA33-DF-054 / Batch Sensitive Data** — shell stdout/stderr 원문을 ExecutionContext에 저장한다.
   - Source: `SpringBatchWorkerStepHandler`
   - 필수 조치: bounded sanitized log artifact reference만 저장하고 원문 DB 저장을 금지한다.
33. **QA33-DF-055 / Batch Approved Definition** — 승인 원본 대신 synthetic definition과 zero checksum/default operator/reason을 만든다.
   - Source: `SpringBatchWorkerStepHandler.definition`
   - 필수 조치: immutable approved definition snapshot을 그대로 전달하고 fallback을 제거한다.
34. **QA33-DF-059 / Remote Topology** — Kafka가 없어도 remote channel을 in-memory로 생성해 remote topology가 같은 JVM으로 fallback한다.
   - Source: `CpfBatchExecutionAutoConfiguration`
   - 필수 조치: Product remote topology는 승인 transport가 없으면 fail-closed하고 test adapter를 분리한다.
35. **QA33-DF-060 / Kafka Multi-instance** — 모든 Manager가 동일 reply group을 공유하지만 reply queue는 인스턴스 로컬이다.
   - Source: `CpfBatchKafkaManagerListener`
   - 필수 조치: instance-safe reply routing 또는 shared persistent correlation store를 구현한다.
36. **QA33-DF-061 / Kafka Idempotency** — serialize마다 새 UUID messageId를 생성해 retry dedupe가 불가능하다.
   - Source: `CpfBatchRemoteCodec`
   - 필수 조치: execution/step/chunk 기반 stable message identity와 attempt number를 사용한다.
37. **QA33-DF-062 / Kafka Contract** — schema version, producer/env binding, TTL, size/depth 제한이 없다.
   - Source: `CpfBatchRemoteCodec`
   - 필수 조치: versioned envelope/schema, max bytes/depth, TTL, producer/tenant/environment binding을 검증한다.
38. **QA33-DF-067 / Scheduler Transaction** — Scheduler DB transaction 안에서 JobOperator.start 후 trigger mark/advance를 수행한다.
   - Source: `SchedulerDispatchService.fire`
   - 필수 조치: outbox/command state machine으로 trigger claim과 Job start response를 분리하고 reconcile한다.
39. **QA33-DF-071 / Gateway Retry** — 요청마다 Retry를 생성하고 same ServerRequest를 재사용해 one-shot body replay가 안전하지 않다.
   - Source: `CpfScgPrimaryHandler`
   - 필수 조치: SCG 표준 filter/policy와 replay-safe body hash/buffer/idempotency 조건을 적용한다.
40. **QA33-DF-072 / Gateway Trust Boundary** — Authorization 외 client header 대부분을 trusted context로 사용한다.
   - Source: `CpfScgPrimaryHandler.headers`
   - 필수 조치: inbound allowlist와 trusted proxy/header overwrite 정책을 적용한다.
41. **QA33-DF-073 / Gateway Completion** — Servlet async/stream lifecycle을 감지하지 않고 chain 반환 시 complete한다.
   - Source: `CpfGatewayLedgerCompletionFilter`
   - 필수 조치: AsyncListener/SCG completion hook으로 실제 write 종료·disconnect를 기록한다.
42. **QA33-DF-074 / Gateway Isolation** — Audit/Ledger 저장 실패가 원 요청/응답을 오염시킬 수 있다.
   - Source: `CpfScgPrimaryHandler;CpfGatewayLedgerCompletionFilter`
   - 필수 조치: 필수 보안 판단과 비오염 관측 기록을 outbox/분리 transaction으로 구현한다.
43. **QA33-DF-080 / Database** — idempotency_key가 전역 unique이고 request hash/scope column이 없다.
   - Source: `V82__spring_batch_primary_control_link.sql`
   - 필수 조치: 3개 Vendor에 scope+request hash와 lifecycle index를 추가한다.
44. **QA33-DF-081 / Database Rollback** — Batch control/link/approval table을 즉시 drop해 감사·대사 데이터가 손실된다.
   - Source: `cpf-tools/db/vendor/**/rollback/R82*.sql`
   - 필수 조치: backup/export/retention/compatibility window와 승인된 destructive rollback을 분리한다.
45. **QA33-DF-086 / Deployment** — DRAIN만 성공한 인스턴스도 touched로 분류해 install 전 rollback한다.
   - Source: `DeploymentEngine.rollbackAfterFailure`
   - 필수 조치: stage별 side-effect ledger로 실제 변경된 operation만 역순 보상한다.
46. **QA33-DF-087 / Deployment Idempotency** — 기존 deploymentId 재사용 시 새 request manifest/hash/approval을 비교하지 않는다.
   - Source: `DeploymentEngine.fromExisting`
   - 필수 조치: canonical request hash와 immutable field mismatch를 충돌로 거부한다.
47. **QA33-DF-088 / Deployment Lock** — lock store 장애를 UNKNOWN_RESULT로 끝내지만 reconcile 경로가 없다.
   - Source: `DeploymentEngine.acquire`
   - 필수 조치: lock claim과 adapter side-effect ledger를 대사하는 recovery API를 구현한다.
48. **QA33-DF-090 / Artifact Rollback** — rollback 시 실제 binary digest/signature를 재검증하지 않는다.
   - Source: `ArtifactInstaller.rollback`
   - 필수 조치: rollback 직전 digest/signature/trust/revocation/environment를 재검증한다.
49. **QA33-DF-091 / Artifact Trust** — keyId가 실제 trust store key 선택과 연결되지 않고 단일 public key file을 쓴다.
   - Source: `ArtifactVerifier`
   - 필수 조치: keyId 기반 allowlisted trust store와 validity/revocation/rotation을 구현한다.
50. **QA33-DF-092 / Artifact State** — artifact-state/previous properties가 서명/MAC 없이 로컬 파일로 저장된다.
   - Source: `ArtifactInstaller`
   - 필수 조치: 상태 원장을 tamper-evident store로 만들고 owner/permission을 검사한다.
51. **QA33-DF-098 / Archive Memory** — Archive create API가 entry를 byte[]로 보유하고 content()마다 복사한다.
   - Source: `CpfArchiveEntry;LocalCpfArchiveService`
   - 필수 조치: Path/InputStream supplier 기반 bounded streaming entry 계약으로 교체한다.
52. **QA33-DF-102 / Supply Chain** — Syft가 final artifact가 아니라 dir:.을 스캔한다.
   - Source: `generate-cpf-supply-chain-evidence.ps1`
   - 필수 조치: 각 final JAR/ZIP/container/filesystem artifact를 개별 스캔하고 hash를 연결한다.
53. **QA33-DF-103 / Supply Chain** — ORT analyze만 실행하고 evaluate/report/NOTICE/source obligation을 검증하지 않는다.
   - Source: `generate-cpf-supply-chain-evidence.ps1`
   - 필수 조치: ORT evaluator/report와 NOTICE/source offer policy를 실행한다.

## 5. QA32 보고 대비 판정

| QA32 보고 | 독립 검수 |
|---|---|
| Source 개발 344/344 완료 | Build·Frontend·Security·Batch·Gateway Source 실패가 있어 승계 불가 |
| OSS Migration 23/23 완료 | Core/Common Legacy dependency, raw fetch, Dual Ownership 잔존 |
| Primary Engine 2,863 checks PASS | 파일·Marker·키워드 중심이며 compile/state/multi-instance 의미를 보장하지 않음 |
| Frontend Primary 전환 | lock/generated client 불일치와 raw fetch Primary 유지 |
| Spring Batch Primary 완료 | JobOperator 연결은 있으나 idempotency/fencing/unknown/retry/remote scale-out 결함 존재 |
| SCG MVC 완료 | SCG handler 사용은 있으나 build path와 retry/header/stream lifecycle 결함 존재 |
| bounded streaming 완료 | Extract는 개선됐지만 Archive create는 byte[] 전체 적재 |
| Supply-chain Gate 완료 | final artifact가 아닌 dir scan, ORT analyze-only, artifact correlation 부재 |
| exact-SHA Evidence 예정 | 현재 Push된 SHA의 completion/requirement/runtime Evidence 정본 부재 |

## 6. QA33 요건 작성 방법

1. 최신 master SHA와 개발 Base/Source Commit을 분리했다.
2. QA32 보고서·Result Matrix·Unresolved Register·Gate를 실제 Source와 대조했다.
3. 파일 존재나 문자열 Marker가 아니라 Owner, Consumer, state transition, failure/recovery, multi-instance, final artifact, Evidence 관점으로 판정했다.
4. 같은 Root Cause 파생 현상은 기존 결함에 병합하고 README/Guide 별도 범위는 제품 결함 수에서 제외했다.
5. Source에서 확정할 수 없는 항목은 `EXACT_SOURCE_RECHECK` 또는 `RUNTIME_UNVERIFIED`로 분리했다.
6. 결함 1건당 수정 Requirement 1건을 만들고 저장소 전역 공통 Gate를 추가했다.
7. Requirement마다 `SOURCE_CONTRACT`, `NEGATIVE_BOUNDARY`, `RUNTIME_FAILURE_RECOVERY` 3개 Scenario를 연결했다.
8. 직접 실행하지 않은 Java25·3DB·Kafka·Browser·Supply-chain은 PASS로 기록하지 않았다.
9. Evidence는 exact SHA, 명령, 환경, 시각, exit code, report/artifact hash, sanitization을 필수화했다.

## 7. 완료 처리 금지

다음 중 하나라도 남으면 QA33와 QA32 Closure를 완료로 판정하지 않는다.

- P0/P1 Source 확정 결함 미수정
- Gradle settings/projects/build/test 실패
- npm ci/typecheck/build/Playwright 실패
- Legacy/Dual Primary/raw fetch/skeleton consumer 잔존
- 3DB/Kafka/Gateway/Batch/Agent Runtime 미검증
- final artifact Supply-chain 결과 불일치
- Requirement/Scenario/Evidence coverage 누락
- Evidence source SHA와 현재 master 불일치
- 부분 구현·미구현·미검증·실패·재확인 필요 상태 존재
- 실행하지 않은 검증을 성공으로 기록
