# CPF Configuration Setup / Binding / Invocation Matrix

> Currentization review baseline: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`; 실제 실행 시 최신 Git HEAD와 Runtime Evidence를 사용한다.
> Canonical catalog: `cpf-tools/generator/config/application-starters.yml`  
> module count: **64**  
> 목적: 64 Module의 `configPrefix` 존재 여부가 아니라 실제 Setup/Binding/Invocation 완전성 검수

## 1. 실행 규칙

각 행은 실제 개발 시 다음 상세 열을 채워 검증한다.

```text
Artifact
→ Config Prefix
→ Config Owner
→ Typed Properties / Schema
→ Scope
→ Required / Default / Constraint
→ Secret Policy
→ Mutability
→ Native Provider Dependency
→ Binding Cardinality
→ AutoConfiguration
→ Public Operations / Typed Client
→ Actual Consumer
→ Health / Diagnostics
→ ADM
→ Generator / EDU
→ Negative / Runtime / Fault Evidence
```

아래 Cardinality는 **Audit Target**이다.
실제 Source/Provider 의미를 검토해 확정하되 모든 Capability에 exactly-one default를 일괄 강제하지 않는다.

## 2. 64 Module Matrix

| # | Artifact | Config Prefix | Visibility | Setup Family | Binding/Cardinality Target | Minimum Closure |
|---:|---|---|---|---|---|---|
| 1 | `cpf-starter` | `cpf.starter` | public | Base | `SINGLE_BASE` | system/env/instance/context/async executor/config catalog |
| 2 | `cpf-starter-cache-caffeine` | `cpf.data.cache.caffeine` | public | Cache | `SINGLE_PROVIDER + NAMED_CACHE_POLICY` | native connection bridge, namespace/TTL/invalidation/fail policy |
| 3 | `cpf-cache-spring-data-redis` | `cpf.data.cache.redis-common` | internal | Cache | `SINGLE_PROVIDER + NAMED_CACHE_POLICY` | native connection bridge, namespace/TTL/invalidation/fail policy |
| 4 | `cpf-starter-cache-redis` | `cpf.data.cache.redis` | public | Cache | `SINGLE_PROVIDER + NAMED_CACHE_POLICY` | native connection bridge, namespace/TTL/invalidation/fail policy |
| 5 | `cpf-starter-cache-valkey` | `cpf.data.cache.valkey` | public | Cache | `SINGLE_PROVIDER + NAMED_CACHE_POLICY` | native connection bridge, namespace/TTL/invalidation/fail policy |
| 6 | `cpf-starter-data-jdbc` | `cpf.data.persistence.jdbc` | public | Persistence | `SINGLE_PROVIDER + NAMED_DATASOURCE` | logical DB role/native datasource/vendor/pool/tx |
| 7 | `cpf-starter-data-mybatis` | `cpf.data.persistence.mybatis` | public | Persistence | `SINGLE_PROVIDER + NAMED_DATASOURCE` | logical DB role/native datasource/vendor/pool/tx |
| 8 | `cpf-starter-data-jpa` | `cpf.data.persistence.jpa` | public | Persistence | `SINGLE_PROVIDER + NAMED_DATASOURCE` | logical DB role/native datasource/vendor/pool/tx |
| 9 | `cpf-starter-file-archive` | `cpf.file.archive` | internal | File/Object | `NAMED_RESOURCE_MULTI` | named site/storage/bucket/path/secret/reconcile |
| 10 | `cpf-starter-file-attachment` | `cpf.file.attachment` | internal | File/Object | `NAMED_RESOURCE_MULTI` | named site/storage/bucket/path/secret/reconcile |
| 11 | `cpf-starter-file-sftp` | `cpf.file.sftp` | internal | File/Object | `NAMED_RESOURCE_MULTI` | named site/storage/bucket/path/secret/reconcile |
| 12 | `cpf-starter-file-tabular-poi` | `cpf.file.tabular.poi` | internal | File/Object | `NAMED_RESOURCE_MULTI` | named site/storage/bucket/path/secret/reconcile |
| 13 | `cpf-starter-integration-ai` | `cpf.integration.ai` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 14 | `cpf-starter-integration-fixed-length` | `cpf.integration.fixed-length` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 15 | `cpf-starter-integration-http` | `cpf.integration.http` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 16 | `cpf-starter-integration-iso8583` | `cpf.integration.iso8583` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 17 | `cpf-starter-integration-resilience` | `cpf.integration.resilience` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 18 | `cpf-starter-integration-soap` | `cpf.integration.soap` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 19 | `cpf-starter-integration-tcp` | `cpf.integration.tcp` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 20 | `cpf-starter-integration-webhook` | `cpf.integration.webhook` | internal | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 21 | `cpf-starter-messaging-ibm-mq` | `cpf.messaging.ibm-mq` | public | Messaging | `SINGLE_PROVIDER + NAMED_BINDING_MULTI` | native broker + destination/group/schema/retry/DLQ |
| 22 | `cpf-starter-messaging-jms` | `cpf.messaging.jms` | public | Messaging | `SINGLE_PROVIDER + NAMED_BINDING_MULTI` | native broker + destination/group/schema/retry/DLQ |
| 23 | `cpf-starter-messaging-kafka` | `cpf.messaging.kafka` | public | Messaging | `SINGLE_PROVIDER + NAMED_BINDING_MULTI` | native broker + destination/group/schema/retry/DLQ |
| 24 | `cpf-starter-messaging-rabbitmq` | `cpf.messaging.rabbitmq` | public | Messaging | `SINGLE_PROVIDER + NAMED_BINDING_MULTI` | native broker + destination/group/schema/retry/DLQ |
| 25 | `cpf-starter-messaging-reliability-jdbc` | `cpf.messaging.reliability.jdbc` | internal | Messaging | `SINGLE_PROVIDER + NAMED_BINDING_MULTI` | native broker + destination/group/schema/retry/DLQ |
| 26 | `cpf-starter-notification-dispatch` | `cpf.notification.dispatch` | internal | Notification | `NAMED_CHANNEL_MULTI` | provider/sender/template/secret/rate/fallback |
| 27 | `cpf-starter-notification-email` | `cpf.notification.email` | internal | Notification | `NAMED_CHANNEL_MULTI` | provider/sender/template/secret/rate/fallback |
| 28 | `cpf-starter-notification-sms` | `cpf.notification.sms` | internal | Notification | `NAMED_CHANNEL_MULTI` | provider/sender/template/secret/rate/fallback |
| 29 | `cpf-starter-platform-operations-channel-registry-jdbc` | `cpf.platform-operations.channel-registry.jdbc` | internal | Platform Operations | `SINGLETON_OR_NAMED_SINK` | health/config/observability/control/drift |
| 30 | `cpf-starter-platform-operations-feature-flag-openfeature` | `cpf.platform-operations.feature-flag.openfeature` | internal | Platform Operations | `SINGLETON_OR_NAMED_SINK` | health/config/observability/control/drift |
| 31 | `cpf-starter-platform-operations-observability-otlp` | `cpf.platform-operations.observability.otlp` | internal | Platform Operations | `SINGLETON_OR_NAMED_SINK` | health/config/observability/control/drift |
| 32 | `cpf-starter-platform-operations-runtime-control` | `cpf.platform-operations.runtime-control` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 33 | `cpf-starter-batch` | `cpf.profile.batch-service` | public | Application Profile | `SINGLE_PROFILE` | profile composition; selected/unselected zero-footprint |
| 34 | `cpf-starter-bff` | `cpf.profile.browser-bff` | public | Application Profile | `SINGLE_PROFILE` | profile composition; selected/unselected zero-footprint |
| 35 | `cpf-starter-event` | `cpf.profile.event-service` | public | Application Profile | `SINGLE_PROFILE` | profile composition; selected/unselected zero-footprint |
| 36 | `cpf-starter-secure-api` | `cpf.profile.secure-api` | public | Application Profile | `SINGLE_PROFILE` | profile composition; selected/unselected zero-footprint |
| 37 | `cpf-starter-web-api` | `cpf.profile.web-api` | public | Application Profile | `SINGLE_PROFILE` | profile composition; selected/unselected zero-footprint |
| 38 | `cpf-starter-security-audit-jdbc` | `cpf.security.audit.jdbc` | internal | Security | `SINGLE_SECURITY_MODE / INTERNAL` | identity/issuer/session/secret/cert/trust |
| 39 | `cpf-starter-oidc` | `cpf.security.oidc` | public | Security | `SINGLE_SECURITY_MODE / INTERNAL` | identity/issuer/session/secret/cert/trust |
| 40 | `cpf-starter-security-resource-server` | `cpf.security.resource-server` | internal | Security | `SINGLE_SECURITY_MODE / INTERNAL` | identity/issuer/session/secret/cert/trust |
| 41 | `cpf-starter-security-secret` | `cpf.security.secret` | internal | Security | `SINGLE_SECURITY_MODE / INTERNAL` | identity/issuer/session/secret/cert/trust |
| 42 | `cpf-starter-security-service-identity` | `cpf.security.service-identity` | internal | Security | `SINGLE_SECURITY_MODE / INTERNAL` | identity/issuer/session/secret/cert/trust |
| 43 | `cpf-starter-session-jdbc` | `cpf.security.session.jdbc` | public | Session | `SINGLE_PROVIDER` | store/native connection/expiry/multi-instance |
| 44 | `cpf-starter-lock-valkey` | `cpf.data.lock.valkey` | public | Distributed Lock | `SINGLE_PROVIDER + NAMED_LOCK_POLICY` | native connection/lease/fencing/recovery |
| 45 | `cpf-starter-messaging-schema` | `cpf.messaging.schema` | internal | Messaging | `SINGLE_PROVIDER + NAMED_BINDING_MULTI` | native broker + destination/group/schema/retry/DLQ |
| 46 | `cpf-starter-graphql` | `cpf.integration.graphql` | public | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 47 | `cpf-starter-realtime` | `cpf.integration.realtime` | public | Integration | `NAMED_CLIENT_MULTI / INTERNAL_PROVIDER` | named external client/channel; endpoint/security/timeout/recovery |
| 48 | `cpf-starter-object-storage-s3` | `cpf.file.object-storage.s3` | public | File/Object | `NAMED_RESOURCE_MULTI` | named site/storage/bucket/path/secret/reconcile |
| 49 | `cpf-starter-session-valkey` | `cpf.security.session.valkey` | public | Session | `SINGLE_PROVIDER` | store/native connection/expiry/multi-instance |
| 50 | `cpf-starter-platform-operations-health` | `cpf.platform-operations.health` | internal | Platform Operations | `SINGLETON_OR_NAMED_SINK` | health/config/observability/control/drift |
| 51 | `cpf-starter-platform-operations-runtime-health-jdbc` | `cpf.platform-operations.runtime-health.jdbc` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 52 | `cpf-base-runtime` | `cpf.base` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 53 | `cpf-starter-common` | `cpf.common` | public | Common Product Service | `SINGLE_PRODUCT_SERVICE` | cpfDB/cache/refresh/locale/calendar; no technical dumping |
| 54 | `cpf-web-runtime` | `cpf.web` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 55 | `cpf-data-runtime` | `cpf.internal.data` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 56 | `cpf-data-persistence-runtime` | `cpf.internal.data.persistence` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 57 | `cpf-messaging-runtime` | `cpf.internal.messaging` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 58 | `cpf-integration-runtime` | `cpf.internal.integration` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 59 | `cpf-file-runtime` | `cpf.internal.file` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 60 | `cpf-notification-runtime` | `cpf.internal.notification` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 61 | `cpf-security-runtime` | `cpf.internal.security` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 62 | `cpf-platform-operations-runtime` | `cpf.internal.platform-operations` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 63 | `cpf-platform-operations-observability-runtime` | `cpf.internal.platform-operations.observability` | internal | Internal Runtime | `INTERNAL_NO_PUBLIC_BINDING` | Public owner config를 구현; 직접 Customer binding 금지 |
| 64 | `cpf-data-transaction-jta` | `cpf.data.transaction.jta` | internal | Transaction | `SINGLE_PROVIDER` | tx manager/JTA owner, timeout/isolation/recovery |

## 3. Cross-Capability Setup Families

### Domain Call
Canonical catalog의 Integration HTTP 구현만으로 끝내지 않는다.
`MBR/EXS/ACC logical domain → local/remote binding → registry/instances → routing → transport`를 별도 Public contract로 검증한다.

### External Integration
HTTP/TCP/SOAP/Fixed/ISO8583/MQ/SFTP/Object/Notification 등은 external named binding을 지원한다.
동일 transport의 여러 기관이 동시에 존재하는 실제 금융권 구성을 검증한다.

### Native Provider Bridge
각 CPF prefix가 native Spring/OSS 설정을 대체하는지 보완하는지 명확히 한다.
중복 설정 namespace를 만들지 않는다.

## 4. Mandatory Gates

- 64/64 Config Catalog metadata.
- selected capability mandatory config success.
- selected + missing required config/native bean fail.
- unselected capability zero config side effect/bean/thread/listener.
- duplicate prefix/binding/default fail.
- explicit-only binding no implicit default.
- secret masked.
- refresh/restart semantics.
- Generator/EDU actual setup.
- exact-SHA evidence.


## 5. Whole-CPF Configuration Quality Columns — 추가

64 Module Matrix와 별도로 각 실제 config family에 다음 열을 검증한다.

`LocalDefault | LocalPort | ProfileFiles | KoreanComment | JavaDoc |
IDE Metadata | SourceOverridePolicy | RuntimeMutability | ProdSafe |
HardcodedValueAudit | NativeConfigBridge | ActualConsumer`

- loopback host: `127.0.0.1` — local/test only.
- 신규 Generated Domain port: stable allocation, 권장 신규 범위 `18080~18999`.
- external institution: real endpoint default 금지; selected simulator만 loopback.
- test: ephemeral/Testcontainers 허용.
- production: local/example/sample fallback 금지.

기존 Canonical Port를 기계적으로 변경하지 않는다.


## 6. Call / Result Configuration Columns

각 Boundary Capability의 실행표에 다음 열을 추가한다.

`BoundaryType | ResultType | OutcomeSet | GenericTypeSafety | Timeout |
Retry | Idempotency | Reconcile | RemoteInLocalTxPolicy |
AutoLog | TxLineage | KoreanJavaDoc | GeneratorSample | RuntimeEvidence`

`CpfResult<T>`가 필요한 Boundary인데 raw DTO/Map만 반환하면 FAIL.
