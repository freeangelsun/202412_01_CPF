# CPF QA38 Starter 편입·이관 독립 리뷰

## 목적
Core를 topology-independent 계약 중심으로 경량화하고 선택 Runtime을 가독성 있는 Starter로 분리한다.

## Core에 남길 것
Public API/SPI, Identifier/Header/Context, Error/Validation 값 계약, Provider-neutral Message/File/TCP/Remote,
Security/Masking/Audit, 순수 Java 최소 구현.

## Starter가 아닌 것
ADM/BZA 권한·메뉴·결재, Batch Job/Step/Worker, Gateway Route/Control Plane,
업무 Entity/SQL/Transaction, 고객기관 전용 Mapping/Secret.

## 30개 판정
| ID | 현재 Owner | 현재 요소 | 판정 | 목표 Artifact | 남길 계약 | Consumer | DB/Generator | 우선 | 개발 | 검증 |
|---|---|---|---|---|---|---|---|---|---|---|
| ST-001 | cpf-core | CpfDataSourceConfig | MOVE | cpf-starter-persistence-jdbc | DB-neutral datasource contract | ADM/BZA/REF/Generated | Canonical DB/3 Vendor | P0 | 부분 구현 | 미검증 |
| ST-002 | cpf-core | CpfMyBatisConfig+dependency/resources | MOVE | cpf-starter-persistence-mybatis | Persistence-neutral SPI | REF/Generated/Product DB | Mapper/Runtime Query/3 Vendor | P0 | 부분 구현 | 미검증 |
| ST-003 | cpf-core | CpfAopConfig+ServiceAccessAspect+AspectJ | MOVE | cpf-starter-aop-service-access | Service access/audit contract | Service/ADM/BZA | Audit metadata | P0 | 부분 구현 | 미검증 |
| ST-004 | cpf-core | CpfOpenApiAutoConfiguration+Springdoc+Scalar | MOVE | cpf-starter-openapi-webmvc | OpenAPI metadata contract | ADM/BZA/REF/Domain | 없음 | P0 | 부분 구현 | 미검증 |
| ST-005 | cpf-core | CpfSecurityAutoConfiguration | SPLIT_MOVE | cpf-starter-security-resource-server | Security principal/permission contract | API Domain/Gateway | Security metadata | P0 | 부분 구현 | 미검증 |
| ST-006 | cpf-starters/security | BFF/JDBC Security Runtime | SPLIT_MOVE | cpf-starter-security-session-jdbc | Generic session contract | ADM/BZA | Session Schema 3 Vendor | P0 | 부분 구현 | 미검증 |
| ST-007 | cpf-core | Broker Worker/Bridge/JDBC Reliability Repository | MOVE | cpf-starter-messaging-reliability-jdbc | Provider-neutral message contract | Kafka/Rabbit/JMS/Batch | Outbox/Inbox/DLQ/Replay 3 Vendor | P0 | 부분 구현 | 미검증 |
| ST-008 | cpf-core | JdbcCpfChannelRegistryAdapter | MOVE | cpf-starter-channel-registry-jdbc | Channel registry SPI | Runtime/Gateway/Agent | Registry schema 3 Vendor | P0 | 부분 구현 | 미검증 |
| ST-009 | cpf-core | Logging+OTel SDK/Exporter Runtime | SPLIT_MOVE | cpf-starter-observability + cpf-starter-observability-otlp | Trace/metric/log contract | 모든 Runtime | Log DB 선택 | P0 | 부분 구현 | 미검증 |
| ST-010 | cpf-core | Remote HTTP Runtime | MOVE | cpf-starter-http-client | Typed remote client contract | ADM/Gateway/Batch/Domain | 없음 | P0 | 부분 구현 | 미검증 |
| ST-011 | cpf-core/common | Validation Runtime Provider | MOVE | cpf-starter-validation | Validation API/values | ADM/BZA/Domain | 없음 | P1 | 부분 구현 | 미검증 |
| ST-012 | cpf-core | Fixed-length Spring Component | SPLIT | cpf-integration-fixedlength-core + cpf-starter-integration-fixedlength | Pure codec contract | TCP/REF/EDU | Layout metadata | P0 | 부분 구현 | 미검증 |
| ST-013 | cpf-core | FileExchange/SFTP Planned Runtime | MOVE_IMPLEMENT | cpf-starter-integration-sftp | File transfer SPI | Batch/Institution/REF | Transfer Ledger | P0 | 부분 구현 | 미검증 |
| ST-014 | cpf-core | Commons Compress/Archive Runtime | MOVE | cpf-starter-file-archive | Archive/file contract | File/Attachment | 없음 | P1 | 부분 구현 | 미검증 |
| ST-015 | cpf-common | Redis/Valkey Connection·Template·Listener | MOVE | cpf-starter-cache-valkey | Business cache abstraction | Common/ADM/BZA/Domain | Cache metadata optional | P0 | 부분 구현 | 미검증 |
| ST-016 | cpf-common | Caffeine Runtime | MOVE | cpf-starter-cache-caffeine | Business cache abstraction | Common/Domain | 없음 | P0 | 부분 구현 | 미검증 |
| ST-017 | cpf-common | POI/XLSX Runtime | MOVE | cpf-starter-tabular-poi | Tabular contract | ADM/BZA/Domain | 없음 | P1 | 부분 구현 | 미검증 |
| ST-018 | cpf-core | Service Identity Runtime | MOVE | cpf-starter-security-service-identity | Identity/mTLS/OIDC contract | Gateway/Batch/Agent | Identity metadata | P0 | 부분 구현 | 미검증 |
| ST-019 | cpf-core | Registry/Health technical client | MOVE | cpf-starter-runtime-registry-client | Registry/health contract | Gateway/Runtime/Agent | Registry metadata | P0 | 부분 구현 | 미검증 |
| ST-020 | cpf-starters/secret | Secret Provider Runtime | EXPAND | cpf-starter-secret + provider plugins | Secret registry contract | 모든 Secret Consumer | Secret reference metadata | P0 | 부분 구현 | 미검증 |
| ST-021 | cpf-starters/featureflag | OpenFeature Runtime | EXPAND | cpf-starter-featureflag + provider plugins | Feature flag contract | ADM/BZA/Domain | Flag/Audit metadata | P1 | 부분 구현 | 미검증 |
| ST-022 | cpf-starters/resilience | CircuitBreaker-only Runtime | EXPAND | cpf-starter-resilience | Deadline/retry/unknown-result contract | Gateway/Batch/Domain | 없음 | P0 | 부분 구현 | 미검증 |
| ST-023 | cpf-starters/messaging-kafka | Kafka Producer/Consumer Runtime | EXPAND_MIGRATE | cpf-starter-messaging-kafka | Provider-neutral message port | Batch/Domain/REF | Reliability Ledger | P0 | 부분 구현 | 미검증 |
| ST-024 | 누락 | RabbitMQ/AMQP | NEW | cpf-starter-messaging-rabbitmq | Provider-neutral MQ+AMQP extension | Domain/Batch/Bridge/REF | Reliability Ledger | P0 | 미구현 | 미검증 |
| ST-025 | 누락 | Jakarta JMS | NEW | cpf-starter-messaging-jms | Provider-neutral MQ+JMS extension | Domain/Batch/REF | Reliability Ledger | P0 | 미구현 | 미검증 |
| ST-026 | 누락 | IBM MQ Provider | NEW_PLUGIN | cpf-starter-messaging-ibm-mq | JMS+IBM MQ extension SPI | Institution Domain | Reliability Ledger | P0 | 미구현 | 미검증 |
| ST-027 | 누락 | TCP Transport Runtime | NEW | cpf-starter-integration-tcp | TCP transport contract | Institution/Batch/REF | Request/Reconcile Ledger optional | P0 | 미구현 | 미검증 |
| ST-028 | 누락/부분 | Notification Email/SMS Worker | NEW_SPLIT | cpf-starter-notification + email + sms-spi | Notification contract | ADM/BZA/Domain | Outbox/Delivery/Result | P0 | 미구현 | 미검증 |
| ST-029 | 선택 기능 | Quartz Scheduler | NEW_OPTIONAL | cpf-starter-scheduler-quartz | Scheduler SPI | 고급 Batch Consumer | Scheduler schema 3 Vendor | P1 | 미구현 | 미검증 |
| ST-030 | cpf-core/common | Public API/SPI·Identifiers·Context·Error·Masking·업무 Common | KEEP | cpf-core / cpf-common | Topology-independent contract and business common | 모든 Consumer | 공통 Metadata만 | P0 | 부분 구현 | 미검증 |

## 기존 7개 보완
Security, Kafka, Cache, Observability, Resilience, Feature Flag, Secret를 세분화하고
Consumer·Failure·Security·Operations·Publication·Optional-removal을 닫는다.

## 신규·복구
| Artifact | Capability | 유입 사유 | 우선 | 최종 범위 |
|---|---|---|---|---|
| cpf-starter-base | Base Boot Runtime | 신규 | P0 | Core+최소 Boot 조립만; Web/DB/Broker/Cache/Security UI/Batch 전이 금지 |
| cpf-starter-persistence-jdbc | JDBC/DataSource | Core 이관 | P0 | Datasource 조립·health·transaction boundary |
| cpf-starter-persistence-mybatis | MyBatis | Core/Common 이관 | P0 | Mapper·Query Catalog·3 Vendor Runtime |
| cpf-starter-aop-service-access | Service Access AOP | Core 이관 | P0 | Audit/trace 계약과 AspectJ Runtime |
| cpf-starter-openapi-webmvc | OpenAPI WebMVC | Core 이관 | P0 | Springdoc·Scalar 선택 Runtime |
| cpf-starter-security-resource-server | Resource Server | Security 분리 | P0 | JWT/OIDC stateless API |
| cpf-starter-security-session-jdbc | BFF Session JDBC | Security 분리 | P0 | ADM/BZA Route 정책은 Product Owner 유지 |
| cpf-starter-security-service-identity | Service Identity | Core 분리 | P0 | mTLS/OIDC/rotation |
| cpf-starter-messaging-reliability-jdbc | Outbox/Inbox/DLQ/Replay | Core 이관 | P0 | 3 Vendor reliability ledger |
| cpf-starter-messaging-kafka | Kafka | 기존 보완 | P0 | Publish/Consume/Rebalance/DLT/Operations |
| cpf-starter-messaging-rabbitmq | RabbitMQ/AMQP | 누락 복구 | P0 | Exchange/Queue/Binding/Confirm/ACK/NACK/DLX/Quorum |
| cpf-starter-messaging-jms | Jakarta JMS | 누락 복구 | P0 | Queue/Topic/Durable/Selector/Transaction/Redelivery |
| cpf-starter-messaging-ibm-mq | IBM MQ | 누락 복구 | P0 | JMS 기반 Queue Manager/Channel/TLS/CCDT/Reason Code |
| cpf-starter-channel-registry-jdbc | Channel Registry JDBC | Core 이관 | P0 | Registry SPI의 JDBC Provider |
| cpf-starter-cache-caffeine | Local Cache | Common 이관 | P0 | Caffeine-only profile |
| cpf-starter-cache-valkey | Distributed Cache | Common 이관 | P0 | Valkey/Redis-compatible provider; Caffeine 강제 금지 |
| cpf-starter-observability | Observation | 기존 보완 | P0 | Trace/Metric/Log 공통 조립 |
| cpf-starter-observability-otlp | OTLP Exporter | Core/기존 분리 | P0 | Collector 장애 격리 |
| cpf-starter-resilience | Resilience | 기존 보완 | P0 | Timeout/Retry/CB/Bulkhead/Rate/Unknown Result |
| cpf-starter-http-client | Typed HTTP Client | Core 이관 | P0 | Identity/trace/deadline/resilience |
| cpf-integration-fixedlength-core | Fixed-length Pure Java | Core 분리 | P0 | Spring 없는 Codec |
| cpf-starter-integration-fixedlength | Fixed-length Boot | Core 분리 | P0 | Layout/encoding/error properties |
| cpf-starter-integration-tcp | Persistent TCP | 누락 복구 | P0 | Connection/Framing/Heartbeat/Reconnect/TLS/Unknown Result |
| cpf-starter-integration-iso8583 | ISO8583 | 누락 복구 | P1 | Bitmap/Field/MAC/PIN Extension |
| cpf-starter-integration-sftp | SFTP | Planned-only 보완 | P0 | 실제 transfer/resume/checksum/reconcile |
| cpf-starter-notification | Notification Worker | 누락 보완 | P0 | Outbox/Worker/Retry/DLQ/Preference/Audit |
| cpf-starter-notification-email | Email | 누락 보완 | P0 | SMTP/provider/template/attachment/bounce |
| cpf-notification-sms-spi | SMS SPI | 누락 보완 | P0 | Provider plugin/rate/receipt/unknown result |
| cpf-starter-tabular-poi | Tabular/XLSX | Common 이관 | P1 | Streaming/limit/formula policy |
| cpf-starter-file-archive | Archive | Core 이관 | P1 | Zip-slip/bomb/path/resource limits |
| cpf-starter-validation | Validation Runtime | Core/Common 이관 | P0 | API와 Provider 분리 |
| cpf-starter-featureflag | Feature Flag | 기존 보완 | P1 | Provider lifecycle/audit/secure override |
| cpf-starter-secret | Secret | 기존 보완 | P0 | Provider catalog/rotation/revocation/health |
| cpf-starter-scheduler-quartz | Quartz | 선택 신규 | P1 | db-scheduler 기본을 대체하지 않는 고급 선택 Adapter |

## 그룹
Leaf/Base/Profile/Aggregate/BOM을 분리한다. Aggregate는 전이 Dependency만 제공한다.
Generator는 `resolvedStarters`와 Profile/Starter Version Lock을 생성한다.
Named Multi-provider는 허용하고 모호한 Default는 fail-closed한다.

## 완료 Gate
Source/POM, actual Consumer, Generator/DB/Config/Test/Artifact, Optional-removal,
정상·오류·Fault·Multi-instance·Unknown, Security·Audit·Operations, Legacy 제거, exact-SHA Evidence.
