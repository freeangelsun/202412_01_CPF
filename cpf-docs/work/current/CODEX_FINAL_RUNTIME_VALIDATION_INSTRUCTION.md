# Codex 최종 Runtime 검증·보완 지침

## 1. 목적

기준 SHA `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`에 이번 Overlay를 적용한 뒤, 개발 GPT 환경에서 실행할 수 없었던 **실제 Java25/Runtime/DB3/Browser/장애 테스트를 가장 적은 반복 비용으로 검증**한다. 단순 오류 유무가 아니라 기능이 Requirement대로 실제 동작하는지를 판정한다.

## 2. 절대 규칙

- 시작 시 `master` exact SHA와 clean Working Tree를 확인한다.
- Overlay 적용 후 `CPF_DELETE_MANIFEST.csv`만 Root-relative로 삭제한다.
- 미실행/READY/PLANNED를 PASS로 기록하지 않는다.
- FAIL 발견 시 Requirement를 낮추거나 Test를 삭제하지 않는다.
- 같은 Root Cause의 잠복 결함을 먼저 전역 검색하고 Source/Test/Config/SQL/Frontend를 묶어 보정한다.
- 보정 후 **가장 싼 최소 Gate → 해당 Phase → 전체 회귀** 순으로 재실행한다.
- 민감정보/Secret/raw payload를 Evidence에 남기지 않는다.
- 사용자 승인 없이 commit/push/branch/reset/restore/stash/clean을 하지 않는다.
- Codex 결과와 보완 Source는 별도로 기록하고, QA 최종 상태는 임의 변경하지 않는다.

## 3. Credit 효율 순서

### Phase 0 — 가장 싼 검증부터
1. exact SHA / `git status --short` / `git diff --check`.
2. Java Source syntax, Frontend TS/Vue syntax, Product unresolved import 0.
3. NXT3 Layout/Query DB3/Korean/Hygiene/Generator/ADM-BZA static gates.
4. Delete Manifest 보호경로 0 / Package completeness.

여기서 FAIL이면 Java25 전체 Build로 넘어가지 말고 먼저 보정한다.

### Phase 1 — Java25 Root Build/Test/Publication
1. Java 25 확인.
2. Root Gradle configuration (`help`, `projects`).
3. `qualityGate`, `aggregateQualityBuild`, `qa34IntegrationTest`.
4. `publicationGate` 및 Public BOM/Internal visibility 검증.
5. Generated local domains 포함 projects/test.

### Phase 2 — 가장 가치가 큰 Local Developer Journey
1. Local integrated: Gateway OFF, 1 JVM, 1 HTTP Port.
2. member/external 자동 발견.
3. Code/Message/Parameter/Calendar/Template 5개 Public Product Service 실제 호출.
4. LOCAL Domain Call 실제 요청/응답.
5. Fixed-Length 한글 UTF-8 byte roundtrip + masking.
6. Standard Header Tx/Exec/Caller/Target 누락 fail-fast와 외부 최초 진입 생성.

### Phase 3 — CPF 핵심 장애 계약
1. 같은 업무 시나리오를 REMOTE binding으로 전환하고 Business Source diff 0 확인.
2. 응답 경계에서 상대 process kill.
3. Caller 결과가 `UNKNOWN`인지 확인.
4. Tx/Exec/Segment/Attempt lineage 유지.
5. Recovery ledger 및 ADM 검색 가능.
6. Reconcile로 최종 상태 수렴.
7. idempotency/retry로 double business effect 0 확인.

이 Phase가 CPF Architecture의 가장 중요한 Acceptance다.

### Phase 4 — DB3 Live Lifecycle
Oracle/PostgreSQL/MariaDB 각각:
1. Clean install + seed + runtime query.
2. Upgrade.
3. Rollback.
4. Re-upgrade.
5. Inbox multi-consumer `(consumer_identity,message_id)` uniqueness와 lease_version 확인.
6. Published migration checksum 변경 0.

### Phase 5 — ADM/BZA Browser E2E
ADM:
- 운영자/권한/승인/Runtime Control/Self-healing/Reliability/Transaction Journey/Batch/Gateway/Cache/Config 기능을 실제 화면 역할대로 검증.
- 검색·Paging·상세·Empty·Loading·401/403/404/409/429/500/503.
- 위험조치는 reason + explicit confirmation + approval + immutable audit + 결과 lineage.
- Runtime Control risk create/cancel/rollback direct 우회가 428/fail-closed이고 Approval Owner Command만 실행되는지 확인.

BZA:
- 조직/직원/역할/결재/업무 공통 관리가 BZA 역할에 맞는지 확인.
- Platform Runtime/Gateway 등 ADM 전용 운영기능이 BZA에 노출되지 않는지 확인.
- Generated Client와 Backend를 실제 소비하는지 확인.

### Phase 6 — 분산 Provider/Topology/배포
1. Redis + Valkey TTL/invalidation/reconnect/provider conflict/stampede/multi-instance.
2. Messaging duplicate/out-of-order/poison/DLQ/restart/trace/idempotency.
3. Batch scheduler/worker/center-cut, kill/restart/concurrency/split-batch.
4. Gateway OFF/ON 동등성, timeout/retry/circuit/rate/bulkhead/header spoof/SSRF.
5. single-node / split-batch / full-distributed / custom topology, Source 변경 0.
6. Fresh Windows/Linux server install/start/status/stop/upgrade/rollback, SHA mismatch/missing env/occupied port/partial install.

### Phase 7 — 마지막 고비용 검증
1. Security adversarial: actor/header spoof, replay, approval bypass, session, CSRF/CORS, SSRF/DNS rebinding, auth bypass, audit tamper, secret leak.
2. Performance/backpressure: DB/cache/broker/external/log queue saturation, thread/connection pool, batch concurrency, ADM large paging.

## 4. FAIL 시 보완 절차

1. 실패 명령/Exit Code/환경/실제 오류를 결과 문서에 기록.
2. 동일 Root Cause를 Repository 전체 검색.
3. Source + Test + Config + SQL + Generator + Frontend/OpenAPI 영향 범위를 함께 수정.
4. 최소 재현 Gate 재실행.
5. 해당 Phase 전수 재실행.
6. 최종 static + Java25 build regression 재실행.
7. 변경 파일, 보안 영향, Evidence 경로를 결과 문서에 기록.

## 5. 반드시 작성할 결과

`cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md`의 모든 표를 채운다. 특히:
- exact SHA / Java/OS/DB/Browser 환경
- 각 Phase 명령과 exit code
- PASS/FAIL/UNVERIFIED
- 발견 결함의 Root Cause
- Codex 보완 Source/SQL/Config/Frontend
- 보안 수정 내용
- 재실행 결과
- 남은 미검증
- 최종 `git status --short`

실행하지 않은 항목을 PASS로 채우지 않는다.
