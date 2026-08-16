# CPF Configuration / Setup / Invocation 상세 개발 Requirement

> 목적: CPF의 모든 Capability를 “구현되어 있음”에서 **설치·설정·주입·호출·운영·복구 가능한 제품 기능**으로 완성한다.  
> 신규 Canonical ID를 추가하지 않는다.  
> Owner: 기존 `CORE-CONFIG`, `CPF-CALL/REGISTRY/ROUTING`, `ARCH-STARTER`, `STARTER-DX`, `OPS-CONFIG`, `SEC-SECRET`, `EXS-*`, `DEVEX-*` 등에 병합한다.

## 1. Configuration Product Model

모든 설정은 다음 질문에 답한다.

- 누가 소유하는가?
- 어떤 Scope인가: global/domain/binding/instance?
- 어떤 환경에서 공급되는가?
- default/required/constraint는 무엇인가?
- Secret인가?
- Native Provider 설정과 어떤 관계인가?
- hot refresh 가능한가, restart가 필요한가?
- 어떤 Bean/Binding/Client가 만들어지는가?
- 어느 Consumer가 실제 사용하나?
- 운영자는 어디서 상태/변경/rollback을 보나?

Config class 존재만으로 완료하지 않는다.

## 2. Source Precedence

Canonical precedence:

```text
Framework Safe Default
→ Generator/Profile Default
→ Application Configuration
→ Environment/Deployment Binding
→ Secret Provider Resolution
→ Authorized Runtime Override
→ Allowed Per-call Option
```

동일 key의 competing source는 deterministic하게 resolve한다.
Effective source/version/hash를 diagnostics에 남긴다.

Runtime override:
- authorization/reason/version/audit.
- atomic validate-before-apply.
- apply failure → previous-good snapshot 유지.
- rollback.
- multi-instance propagation/drift detection.
- restart-required는 live apply 금지.

## 3. Domain Call Setup

CPF Domain 예: MBR, EXS, ACC.

Domain identity와 deployment endpoint를 분리한다.

Logical Definition:
```text
MBR depends-on EXS
MBR depends-on ACC
```

Runtime Binding:
```text
EXS
→ LOCAL / REMOTE / supported AUTO policy
→ service id
→ static VIP/DNS and/or dynamic registry
→ instance/endpoint/zone/version/weight
→ health/maintenance/drain/lease
→ routing/failover
```

Business Source는 동일:

```java
@CpfInject
private ExsClient exsClient;

return exsClient.verify(request);
```

Local/Remote switching 때문에 Java Source를 바꾸면 FAIL.

## 4. External Integration Setup

External client/channel은 Domain ID와 별도 Namespace를 사용한다.

```text
credit-agency
bank-host
card-company
```

공통 logical fields:
- id/name/owner/enabled/environment.
- transport/provider.
- endpoint binding.
- auth/secret/certificate reference.
- timeout/retry/circuit/bulkhead/rate.
- mapping/masking/correlation/idempotency.
- health/degraded/drain.
- UNKNOWN/probe/reconcile.

Transport-specific:
- HTTP: base URL/path/TLS/proxy/DNS/pinning.
- TCP: host/port/framing/charset/heartbeat/reconnect.
- Fixed/ISO8583: layout/schema/version/field mapping.
- SOAP: endpoint/service/action/schema.
- MQ/Messaging: destination/group/ack/DLQ/schema/security.
- SFTP: host/port/path/host-key/credential/checksum/ack.
- S3: bucket/region/endpoint/encryption/credential chain/reconcile.
- Notification: channel/provider/sender/template/rate/fallback.

## 5. Named Binding / Cardinality

Binding policy is capability-specific.

- Provider slot and logical resource binding are 별개다.
- Cache provider는 exactly-one일 수 있다.
- External HTTP/TCP/SFTP 기관은 0..N named binding이 자연스럽다.
- default가 위험한 기능은 explicit-only.
- duplicate binding name/default ambiguity는 fail-fast.
- runtime binding lookup은 type-safe accessor/client/factory를 우선한다.

## 6. Native Config Bridge

CPF 설정은 OSS 설정을 재발명하지 않는다.

각 Starter는 다음을 명확히 한다.

```text
CPF semantic prefix
→ required native property prefix / bean
→ secret provider
→ validation/preflight
→ auto configuration
→ public operations
```

예:
- Redis connection → Spring Data Redis/driver.
- Kafka bootstrap/security → Spring Kafka.
- OIDC issuer/client → Spring Security.
- DataSource → Spring Boot DataSource.
- S3 credential → AWS credential-provider chain.

Native API/Bean을 직접 써야 하는 고급 케이스는 공식 Native Escape로 제공한다.

## 7. Config Metadata / Schema

64 Starter Config Matrix의 모든 prefix에:
- property owner
- schema/type
- default
- required
- unit/range/enum/pattern
- secret separated
- scope
- mutability
- runtime override
- masked display
- native dependency
- health impact
- since/deprecated/replacement/alias
를 연결한다.

IDE configuration metadata와 한글 설명을 제공한다.


## 7A. Local Default / Profile / Korean Comment Requirement

- local/test host 기본은 필요 시 `127.0.0.1`.
- dev/stg/prod silent loopback fallback 금지.
- 외부기관은 local simulator/stub를 명시 선택한 경우에만 loopback.
- Generator는 stable local port와 collision preflight를 제공한다.
- 기존 Canonical Port는 호환성 검토 없이 기계적으로 바꾸지 않는다.

각 주요 option은 사용하지 않더라도 commented example로 검색 가능해야 하며,
한글로 역할/기본값/허용값/단위/우선순위/변경방식/보안/운영주의/실패조건을 설명한다.
YAML 주석과 실제 Java default/validation이 다르면 Gate FAIL.

## 7B. Programmatic Override

Source 확장 API:
- Typed Customizer
- Typed Builder/Options
- Provider SPI
- documented Bean override
- Per-call bounded options

금지:
- mutable static config
- raw Map/string lookup Golden Path
- arbitrary production endpoint/secret/security override
- `System.setProperty`를 통한 정책 우회

각 property는 Override Policy를 Config Catalog metadata로 가진다.

## 7C. Repository-wide Config Inventory Gate

전 Repository에서:
`application*`, `@ConfigurationProperties`, `@Value`, env/system property,
URL/IP/port, timeout/retry/TTL/pool/thread/concurrency, CLI/JVM option,
Docker/K8s/CI binding, Generator template/default를 inventory한다.

각 항목을:
`owner | key | profile | default | documented | KoreanComment | secret |
mutability | overridePolicy | consumer | test`
로 매핑한다.

## 7D. Gateway

Gateway의 `application.yml`, `CpfGatewaySafetyProperties`,
Route/Registry/SCG/Control Plane Source를 전수대조한다.

listen/control bind/port, CORS/forwarded header, trusted proxy,
route/registry/routing, timeout/retry/rate/circuit, auth/mTLS/TLS,
request limit, context/idempotency, error mapping, health/drain,
maintenance/canary/version/zone/weight, audit/secret masking을
환경별 profile/한글주석/typed validation/runtime consumer와 맞춘다.

## 8. Setup UX

Normal Developer:
1. Generator/Starter 선택.
2. generated config skeleton 확인.
3. environment/secret 값 공급.
4. `@CpfInject` typed bean/client 사용.
5. IDE JavaDoc로 호출.
6. fail-fast message로 누락 설정 수정.

Advanced Developer:
- Customizer/Strategy/Provider/SPI/Bean override.
- Named binding.
- Programmatic configuration where authorized.
- Native Escape.

Internal package 접근이나 Source fork가 필요하면 DX FAIL.


## 8A. Call Option / Result / Recovery Configuration

Call 설정은 개발자가 raw Map으로 만들지 않는다.

Typed option family:
- `CpfCallOptions`
- `CpfDomainCallOptions`
- `CpfExternalCallOptions`
또는 naming-consistent 동등 계약.

주요 option:
- deadline/timeout
- idempotency key/reference
- retry policy reference
- reconcile policy reference
- route hint(version/zone) — 허용된 범위만
- consistency/remote-in-local-tx policy
- result metadata verbosity
- masking/logging policy reference

금지:
- per-call raw URL/IP override Golden Path
- per-call credential/token
- security/TLS policy bypass
- raw `Map<String,Object>` options

각 option은 한글 JavaDoc, default, allowed range, source override policy를 Config Catalog와 맞춘다.

## 9. Generator

Generator schema가 최소 다음 논리 항목을 표현할 수 있어야 한다.

```yaml
domain:
  name: member
  systemCode: MBR

domainDependencies:
  - EXS
  - ACC

externalClients:
  - id: credit-agency
    capability: http
```

위 YAML은 **개념 예시**이며 실제 canonical key 이름은 기존 Generator schema와 충돌/중복 없이 하나로 확정한다.

금지:
- real IP/URL
- password/token
- private key/certificate content
- environment-specific secret

Generated output:
- required starter/provider.
- typed client/contract skeleton.
- environment binding placeholders.
- config metadata/comments.
- negative validation test.
- sample consumer.

## 10. Operations / ADM

Runtime Config:
- catalog/search/detail.
- effective source/version.
- mutability.
- validation.
- masked display.
- staged apply.
- rollback.
- drift.

Topology:
- domain/service/instance.
- endpoint/zone/version/weight.
- health.
- maintenance/drain.
- routing.

External:
- institution/client/channel.
- protocol/endpoint health.
- cert expiration/status.
- last call/recovery state.
- UNKNOWN/reconcile.

모든 risky command는 permission/reason/approval/audit.

## 11. Test Matrix

반드시:
- 64/64 prefix/metadata.
- config duplicate/orphan/unknown.
- missing mandatory native dependency.
- secret masking.
- local Domain Call.
- remote Domain Call same source.
- 2+ remote instances/load balance/failover/drain.
- version/zone/weight.
- same transport external clients 2+.
- explicit-only missing binding.
- invalid timeout/range/schema.
- refreshable atomic success.
- invalid refresh previous-good preservation.
- restart-required live apply rejection.
- multi-instance drift/reconcile.
- generator logical setup.
- member/external/education actual use.
- ADM generated client/backend/e2e.
- exact-SHA Evidence.

## 12. Commercial Completion

Configuration가 없거나 호출 Source에서 raw URL/IP/bean-name/provider plumbing을 반복하거나,
운영자가 active/effective 설정을 확인할 수 없거나,
Generator/EDU가 실제 사용법을 보여주지 못하면 해당 Capability는 완료가 아니다.
