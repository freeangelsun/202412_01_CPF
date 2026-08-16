# ADR — CPF OSS-first Platform Direction

- 상태: `APPROVED PRODUCT DIRECTION`
- 최초 결정일: `2026-07-30`
- 상세 현행화: `2026-07-31`
- 현행화 검토 기준: `c1f273f1ea4fafac6fd5d23bd837adfc38a04497`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 법률 고지: 기술·라이선스 운영 결정이며 실제 판매·배포 전 exact version과 distribution 형태를 법무·보안이 다시 확인한다.

## 1. Context

CPF가 범용 기술 Engine과 UI Widget을 자체 구현하면 다음 문제가 반복된다.

- 검증되지 않은 동시성·보안·프로토콜
- 생태계 표준과의 호환성 저하
- 유지보수·CVE·License 대응 비용
- Sample 또는 Marker만 있는 가짜 구현
- Legacy와 신규 OSS의 Dual Primary
- 최종 Artifact와 Source dependency의 불일치

CPF는 검증된 permissive OSS를 Primary로 사용하되 CPF 고유 책임을 명확히 소유한다.

## 2. Decision

CPF가 소유한다.

- topology-independent Public API/SPI
- 표준 Header·Context·Error·Idempotency
- 정책과 Safety Ceiling
- 권한·사유·승인
- Version·Checksum·Lease·Fencing
- Audit·Attempt Ledger·Timeline
- Unknown Result·Reconciliation·Recovery
- Control Plane과 Product UX
- Generator·Migration·Evidence·Release Governance

OSS가 소유한다.

- 범용 실행 Engine
- 표준 protocol/client/runtime
- 범용 UI primitive
- 표준 metadata repository와 lifecycle
- 표준 dependency/license/vulnerability 분석

## 3. 승인 Primary Stack

| 영역 | Primary | CPF 책임 |
|---|---|---|
| UI Widget | Element Plus | design token, permission, masking, audit UX |
| Data Table | TanStack Table | column policy, server paging/sort/filter |
| Router | Vue Router | business route ID, permission metadata |
| Client State | Pinia | persistence allow/deny, security classification |
| Server State | TanStack Vue Query | query key, retry/idempotency, error contract |
| Form | Zod + Element Plus Form | server validation source, null/empty, reason |
| API Client | Orval | OpenAPI, headers, CSRF, error mutator |
| Browser Security | Spring Security + Spring Session JDBC BFF | role/data scope, session audit/revocation |
| Gateway | Spring Cloud Gateway Server Web MVC + Tomcat | route/trust/approval/ledger/control plane |
| Messaging | Apache Kafka | envelope, idempotency, DLT, attempt/recovery |
| Unit Messaging | CPF in-memory test adapter | deterministic minimal contract only |
| Resilience | Spring Cloud CircuitBreaker + Resilience4j | retry eligibility, deadline, unknown result |
| Batch | Spring Batch | CPF definition/approval/topology/security/control |
| Scheduler | db-scheduler default | schedule policy, Job start recovery |
| Migration | Flyway OSS Core | Vendor SQL, rollback/restore/evidence |
| Observability | Micrometer Observation + OTel OTLP | CPF IDs, masking, attribute policy |
| Local Cache | Caffeine | key/TTL/invalidation/failure policy |
| Distributed Cache | Valkey-compatible optional provider | provider/consistency contract |
| Feature Flag | OpenFeature + CPF Provider, conditional | definition/approval/environment/audit |
| Secret | CPF SecretProvider SPI | reference/rotation/audit/masking |
| Browser E2E | Playwright | role/workflow/fault/evidence |
| Supply Chain | CycloneDX + ORT + Syft + Grype | allow/deny/exception/release decision |

## 4. 제한·선택 범위

### Spring Batch

Spring Batch는 CPF Batch의 단일 Primary Execution Engine이다.

Spring Batch 소유:

- Job·Step·Tasklet·Chunk
- Reader/Processor/Writer
- JobRepository·ExecutionContext
- Checkpoint·Restart·Stop·Abandon
- Flow·병렬 Step
- Local/Remote Partitioning
- Remote Chunk/Step

CPF 소유:

- immutable definition/version/checksum
- 승인·권한·사유
- topology·artifact·agent
- File/Shell/API/Message adapter security
- idempotency·latest fencing
- Spring Batch ID 연결
- unknown-result reconciliation
- ADM 운영·감사

자체 Job/Step lifecycle, execution repository, checkpoint/restart Engine을 병행하지 않는다.

### Scheduler

db-scheduler가 persistent trigger의 기본이다. Trigger claim과 `JobOperator.start` 사이 결과 불명은 CPF outbox/state/reconciliation으로 보완한다.

Quartz는 고급 Calendar/JTA 등 구체적 요구와 별도 ADR가 있을 때만 선택 Adapter로 허용한다.

### Feature Flag

실제 Product Requirement와 Consumer가 있을 때만 OpenFeature + CPF Provider를 도입한다. Dependency만 추가하지 않는다.

### Workflow

Flowable은 동적 사람 중심 Workflow가 현재 ADM/BZA Approval Engine으로 해결되지 않고 별도 ADR threshold를 통과할 때만 검토한다.

## 5. Gateway 결정

현재 CPF Gateway는 SCG Server Web MVC 하나다.

- Artifact: executable `cpf-gateway.jar`
- Runtime: Embedded Tomcat
- `bootWar`: disabled
- WebFlux/Netty Artifact: 금지
- Envoy Data Plane: 현재 범위 제외
- Scale: stateless horizontal scale-out
- 미래 재검토: Web MVC scale-out이 실제 long-connection/streaming/resource 목표를 충족하지 못한 부하 Evidence가 있을 때 별도 ADR

CPF Route Model은 SCG Type을 Public Contract로 노출하지 않는다. 미래 가능성을 이유로 범용 Gateway Engine을 다시 만들지 않는다.

## 6. Messaging 결정

Kafka가 유일한 Product Messaging Primary다.

- Unit: deterministic in-memory adapter
- Integration: actual Kafka/Testcontainers Kafka
- Delivery: at-least-once + consumer idempotency
- stable message/correlation ID
- schema version, TTL, size/depth, environment binding
- retry topic/DLT/poison isolation/replay approval
- manager/worker multi-instance correlation
- exactly-once는 DB side effect까지 증명되지 않으면 Framework 보장 금지
- AMQP/RabbitMQ/Artemis Product Primary 제거

## 7. Browser 결정

ADM/BZA Browser는 server-side session만 사용한다.

- Secure/HttpOnly/SameSite Cookie
- Spring Security session fixation 방어
- standard CSRF
- force logout
- permission/session version 재검증
- ADM/BZA namespace 분리
- Access/Refresh Token과 Session ID의 browser-readable storage/body/URL/log 노출 금지
- JDBC Session credential 최소화·보호
- multi-instance session invalidation

## 8. Frontend 결정

전환 완료는 package dependency 존재가 아니다.

필수:

- package-lock exact 일치
- clean npm ci
- Orval exact-SHA generated client
- raw fetch 허용 경계 축소
- feature Query/Mutation 실제 소비
- legacy hash router/large mixin/manual cache 제거
- server-side authorization
- 3 Browser E2E와 accessibility
- external runtime CDN/font/script 없음

## 9. Migration 완료 규칙

```text
Current inventory
→ exact license/version
→ Owner/ADR
→ Adapter/configuration
→ vertical consumer
→ function/security/performance/failure/recovery parity
→ all consumer migration
→ legacy source/bean/route/dependency/artifact removal
→ POM/BOM/lock/final artifact/SBOM
→ exact-SHA runtime evidence
```

Dual Primary는 완료가 아니다.

## 10. 명시적 제외

- PrimeVue 최신/유료 Asset
- `cpf-gateway-webflux.jar`
- WebFlux/Netty Gateway
- Envoy Data Plane
- Kafka와 병행되는 AMQP Product Primary
- Redis Server 기본 Bundle
- Vault Server 기본 Bundle
- Flyway Teams/Enterprise
- Flowable 유료 배포판
- 승인 없는 Copyleft/Source-available
- Unknown/NOASSERTION component

## 11. Decision ID와 Product Requirement ID

`OSS-MIG-001`~`OSS-MIG-023`은 Migration Decision/작업 추적 ID다. Canonical Product Requirement 162개에 합산하지 않는다.

각 Decision은 Build-vs-Buy Matrix에서 다음에 연결한다.

- Canonical Requirement
- Owner
- Consumer
- Legacy removal
- Runtime evidence
- final artifact/license proof

## 12. Consequences

장점:

- 검증된 표준 Engine 활용
- CPF 고유 정책과 Control Plane 집중
- License/CVE/upgrade 추적 가능
- Consumer/Legacy 완료 판정 명확화

비용:

- OSS Major Upgrade와 compatibility 검증
- Adapter·Public Contract 안정성 관리
- final artifact 공급망 증명
- 실제 Runtime/Failure Evidence 필요

## 13. 완료 금지

- QA32 당시 dependency 추가만으로 이 ADR 완료 선언
- Source에 Legacy/Dual Primary 잔존
- Core/Common이 선택 Runtime을 강제 전파
- Browser token/session ID 노출
- Kafka multi-instance/rebalance 미검증
- SCG streaming/disconnect/SSRF 미검증
- Spring Batch 상태·idempotency·fencing·unknown 미검증
- final artifact supply-chain 미검증
