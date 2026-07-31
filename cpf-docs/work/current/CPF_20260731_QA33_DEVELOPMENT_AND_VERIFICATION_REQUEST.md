# CPF QA33 Source 수정·Runtime Closure 요청

- Package ID: `CPF-20260731-QA33-INDEPENDENT-SOURCE-RUNTIME-CLOSURE`
- Review baseline: `1536a0d59004ebade7dcb29383cbe2e758547f8e`

## 현재 상태
QA32는 전체 완료가 아니다. 기존 Runtime 미검증 외에 Build 시작 전 실패와 Source semantic 결함이 확인됐다. 독립 검수 보고서와 5개 Matrix를 정본으로 사용한다.

## Phase 0 — Baseline·Evidence
- latest exact SHA/clean worktree
- stale Overlay 보고 정정
- Evidence template/schema/.gitignore/Manifest 복구
- request integrity 통과

## Phase 1 — Build
- Included Build 복구 또는 정식 제거
- ADM/BZA/Gateway Project path
- Java25 full build/test/publish
- POM/BOM/lock/SBOM graph

## Phase 2 — Frontend·BFF
- ADM/BZA exact lock
- Orval exact-SHA generated client
- raw fetch와 대형 Legacy Store 이관
- Token/Session ID 노출 제거
- standard Session/CSRF/Fixation/permission revocation/force logout

## Phase 3 — OSS Ownership
- Core/Common의 Kafka/AMQP/WebFlux/OTel/Cache/Redis Runtime 분리
- Starter 실제 Product Consumer 이관
- Legacy import/bean/route/dependency/artifact 제거

## Phase 4 — Batch·Kafka·Scheduler
- request hash idempotency
- current fencing/lease
- start/bind response loss reconciliation
- Spring Batch status/retry/stop
- remote stable id/schema/TTL/size/DLT/backpressure
- multi-manager reply correlation
- scheduler outbox/reconciliation

## Phase 5 — Gateway
- SCG standard lifecycle/filter/loadbalancer/resilience
- trusted header allowlist
- replay-safe body/idempotency
- async stream completion
- ledger/audit isolation
- SSRF/TLS/target canonicalization

## Phase 6 — DB·Deployment·Agent·Resource
- 3 Vendor canonical schema/migration/rollback
- side-effect selective rollback
- artifact state tamper protection/key rotation/rollback reverify
- bootstrap ACL/reconciliation
- Archive create streaming/atomic publish
- Attachment consumer 전수 이관

## Phase 7 — Runtime·Supply-chain
- Java25 full, ADM/BZA 3 Browser, 3DB, Kafka, Gateway, Batch/Scheduler/Agent
- final Artifact별 CycloneDX/ORT/Syft/Grype
- Requirement/Scenario 전 행 exact-SHA Evidence

## 결과
QA33 Pre-development Review, Completion Report, Result Matrix, Unresolved Register, requirement/scenario evidence, 변경 Source/SQL/Test/Script, Root-relative Overlay ZIP을 제공한다. `.git`, build, node_modules, cache, raw log, secret, private key를 ZIP에 포함하지 않는다.
