# CPF Starter Architecture·Lifecycle 정책

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 중앙 정책 현행화 기준 Branch/SHA: `master` / `a570b366ef85b23863e41173c991025c072a2427` (`07_12`)
- 적용 Root: `cpf-starters`
- Root 분류: `FIXED_PRODUCT_CONTAINER`
- Architecture 방향: **Lightweight Core + Explicit Opt-in Starter**
- 상태 판정: 이 정책 파일에 고정하지 않으며 current exact-SHA Source, Final QA와 Runtime Evidence에서 `development_status` / `verification_status`를 별도 판정한다.

## 1. 제품 원칙

`cpf-core`는 topology-independent Public API/SPI, 표준 식별자·오류·보안 문맥과 최소 실행 계약만 소유한다.
Spring Boot Web, Security, Session, Kafka, Redis, Caffeine, OpenTelemetry Exporter, OpenAPI UI, MyBatis와 같은 선택 Runtime은 Core에 강제로 포함하지 않는다.

선택 Runtime은 승인된 Starter 또는 실제 Owner Module로 분리한다.
Generated Domain과 실행 Product는 필요한 Capability만 명시적으로 선택하고, 선택하지 않은 Starter의 JAR·Bean·설정·SQL·전이 Dependency를 포함하지 않는다.

```text
Generated Domain / Product Runtime
  ├─ 일반 Boot Runtime은 cpf-starter-foundation-base 선택 가능
  └─ 필요한 Leaf Starter만 명시적 선택
       ├─ cpf-core Public API/SPI
       ├─ 승인된 Public Contract
       └─ 외부 기술 Library

cpf-core
  └─ 선택 Runtime을 역으로 참조하거나 강제로 전이하지 않음
```

## 2. Starter 정의

CPF Starter는 특정 기술 Runtime을 CPF 공개 계약에 연결하고 Spring Boot AutoConfiguration으로 조립하는 독립 Library JAR이다.
Starter 자체는 독립 서버가 아니며, 이를 선택한 ADM·BZA·Gateway·Batch Runtime·Generated Domain·Reference Runtime의 JAR/WAR 내부 Library로 포함된다.

Starter는 다음 조건을 모두 만족해야 한다.

1. 둘 이상의 Product Runtime에서 재사용되거나 Architecture상 공식 선택 기술로 승인됐다.
2. 업무·관리·Batch·Gateway 고유 정책이 아니라 기술 Adapter와 Runtime 조립 책임이다.
3. 선택하지 않은 애플리케이션에 Dependency·Bean·기동 조건을 강제하지 않는다.
4. CPF Public API/SPI 또는 명시된 공개 계약을 실제 구현한다.
5. AutoConfiguration, Properties, Adapter, 오류·복구 의미와 Test를 함께 제공한다.
6. 독립 JAR, Publication, BOM, SBOM, Version, Upgrade·Rollback 정책을 가진다.
7. 실제 Product Consumer와 Optional 제거 Compile·Runtime Evidence가 있다.

## 3. 의존 방향

허용 기본 방향:

```text
Product / Generated Domain → cpf-starter-* → cpf-core Public API/SPI
                                          → 외부 기술 Library
```

`cpf-common` 참조는 고객 업무 공통 전체가 기술 Starter에 전이되지 않도록 예외 승인을 요구한다.

금지 방향:

```text
cpf-starter-* ─X─> cpf-admin / cpf-biz-admin / cpf-gateway
cpf-starter-* ─X─> cpf-batch 실행 제품 / cpf-reference / Generated Domain
cpf-core      ─X─> 선택 Starter 또는 선택 OSS Runtime
```

## 3.1 Core와 Base Starter의 관계

`cpf-core`는 Starter가 아니라 모든 기술 Adapter가 공유하는 독립 초경량 계약 Artifact다.
Spring Boot를 사용하지 않는 Consumer와 고객 SPI 구현도 Core만 소비할 수 있어야 한다.

`cpf-starter-foundation-base`는 다음 QA에서 확정할 Spring Boot 최소 조립 Starter 후보다.
일반 Boot Domain의 편의 진입점으로 사용할 수 있지만 Core를 대체하지 않는다.

```text
비 Spring/계약 Consumer → cpf-core

Spring Boot Domain → cpf-starter-foundation-base → cpf-core
                  → 필요한 Leaf Starter만 추가
```

Base Starter에는 Web·DB·Messaging·Cache·Session·OpenAPI·Exporter·Batch를 포함하지 않는다.
Base가 `cpf-common`을 강제 전이해서도 안 된다.

## 4. Starter가 소유하는 것

- 기술 Provider Adapter
- Spring Boot AutoConfiguration과 조건부 Bean
- Configuration Properties와 fail-closed validation
- CPF Public Contract와 외부 Library 간 변환
- 표준 오류·timeout·retry·idempotency·recovery 의미
- Security·Masking·Audit·Metric·Readiness 연결
- Unit·Contract·Integration·Optional-removal Test
- AutoConfiguration imports, metadata, JavaDoc
- 독립 Publication과 Consumer-facing dependency metadata

## 5. Starter가 소유하지 않는 것

- ADM/BZA/업무 Domain 고유 인증·권한·Route·메뉴·승인
- Batch Job·Step·Scheduler·Worker의 제품 실행 책임
- Gateway Route 원장·Control Plane
- 고객사별 Secret Provider·기관별 전문 Adapter의 사설 구현
- Domain SQL·Migration·Entity·업무 Transaction
- Generator·DB 설치·검증 Script
- Consumer가 하나뿐인 Owner 전용 내부 구현
- Interface·Marker·AutoConfiguration 이름만 있고 Provider·Consumer·실패 처리가 없는 추상화

## 6. 현재 공식 Container와 구성

`cpf-starters`는 2026-07-31 Commit `1536a0d59004ebade7dcb29383cbe2e758547f8e`에서 추가됐다. 현재 Gradle Project는 다음 7개다.

| Starter | 현재 역할 | 현재 판정 |
|---|---|---|
| `cpf-starter-security` | Spring Security·Spring Session JDBC 기반 Browser/BFF Session | 유지하되 범용 기술과 ADM/BZA 경로 정책 분리 필요 |
| `cpf-starter-messaging-kafka` | `CpfBrokerClient`·Bridge의 Kafka Adapter | 유지, Producer/Consumer·ACK/DLQ·결과불명 Closure 보완 |
| `cpf-starter-cache` | Caffeine 선택 Provider와 Common Cache Runtime 활성화 | 유지 후보, `cpf-common` Runtime AutoConfiguration 이관 필요 |
| `cpf-starter-platform-operations-observability` | Micrometer Observation·OTel 연결 | 유지, Exporter·Trace·Masking·Backpressure Closure 보완 |
| `cpf-starter-resilience` | CircuitBreaker 기반 실행 Adapter | 유지, 정책·timeout budget·retry 의미 보완 |
| `cpf-starter-featureflag` | OpenFeature Client 연결 | 실제 Provider·Consumer·Audit 확보 전 부분 구현 |
| `cpf-starter-security-secret` | 승인된 `CpfSecretProvider` Registry | Provider·Rotation·Readiness·Consumer 검증 전 부분 구현 |

이 목록은 현재 기준선이며 최종 Starter Catalog가 아니다. 다음 Core 경량화·Starter 세분화 QA가 전체 Framework 기능을 다시 분류하여 분리·통합·이관·제거를 결정한다.

## 7. Core 경량화 기준

Core에는 다음만 남기는 것을 기본으로 한다.

- 표준 식별자, 요청·응답·오류 모델
- Public API/SPI와 topology-independent 계약
- 구현 기술을 노출하지 않는 보안·거래·Broker·Cache·Observability 계약
- 최소 Java/Jakarta API와 실행 기술에 독립적인 Utility

다음은 전수 검토 후 Starter 또는 Owner Module로 이관할 후보이다.

- MyBatis·JDBC Runtime 자동 구성
- AspectJ 기반 선택 Runtime
- Servlet/Web MVC Filter·Interceptor Runtime
- RestClient/WebClient 구현
- Spring Security·Session
- OpenAPI UI·Scalar Runtime
- OTel SDK·Exporter
- Cache Provider·Redis 연결
- Kafka/AMQP Provider
- Feature Flag·Secret Provider Registry
- 선택형 Idempotency·Outbox/Inbox JDBC Adapter

단순히 Dependency 위치만 옮기지 않는다. Public Contract, 실제 Consumer, Config, Test, 실패 처리, Generator와 Guide를 함께 이관하고 이전 Primary를 제거한다.

## 8. Domain의 Starter 선택

Generated Domain은 최소 Profile로 생성하고 필요한 Capability만 명시적으로 선택한다.

```text
securityMode = NONE | SESSION_JDBC | RESOURCE_SERVER
messaging = NONE | KAFKA | RABBITMQ(공식 승인 시)
cache = NONE | CAFFEINE | REDIS
observability = ENABLED | DISABLED_WITH_REASON
resilience = ENABLED | DISABLED
featureFlag = NONE | OPENFEATURE
secretProvider = NONE | CUSTOMER_PLUGIN_REFERENCE
webRuntime = NONE | WEBMVC
persistence = NONE | MYBATIS | 승인된 다른 Provider
```

실제 enum과 기본값은 다음 QA에서 확정한다. 선택 결과는 `build.gradle`, Config, Test, Domain Manifest, 설치·운영 Guide에 원자적으로 반영돼야 한다.

## 8.1 개별 Starter·Profile·Bundle·BOM 선택 계층

Domain과 Product가 모든 Starter를 반드시 한 줄씩 직접 등록해야 하는 것은 아니다.
다만 편의성을 이유로 불필요한 Runtime을 끌고 오는 Mega Starter를 만들면 Core 경량화 목표가 무너진다.
CPF는 다음 네 계층을 구분한다.

| 계층 | 목적 | Runtime Dependency 활성화 | 기본 정책 |
|---|---|---:|---|
| Leaf Starter | 단일 기술 Capability 구현 | 예 | 구현과 검증의 정본 |
| Capability Profile | 사용 사례별 Leaf Starter 선택 집합 | Generator/Build가 명시적으로 확장 | 권장 기본 편의 계층 |
| Aggregate Starter | 하나의 Dependency로 승인된 Leaf Starter 묶음을 전이 | 예 | 필요성이 입증된 경우만 선택 제공 |
| Platform BOM | Version·Compatibility 정렬 | 아니오 | 모든 Artifact 버전 정본 |

### Leaf Starter

예:

```text
cpf-starter-webmvc
cpf-starter-profile-web-api
cpf-starter-security-resource-server
cpf-starter-messaging-kafka
cpf-starter-cache-redis
```

Leaf Starter는 독립 JAR·POM·Test·Publication·Evidence 단위이며 최종 구현 책임의 정본이다.

### Capability Profile

Profile은 Generator 또는 Build Convention이 여러 Leaf Starter를 선택하는 선언이다.
Profile 자체가 Runtime 구현을 소유하지 않으며, 생성 결과에는 해석된 Leaf Starter 목록을 명시적으로 남긴다.

```text
profile = DOMAIN_WEB_API
resolvedStarters =
  cpf-starter-webmvc
  cpf-starter-profile-web-api
```

권장 원칙:

- Generator 입력에서 Profile 한 개 또는 복수의 조합 가능한 Profile을 선택할 수 있다.
- 생성된 `build.gradle`, Domain Manifest, Config, Test와 Guide에는 최종 Leaf Starter 목록을 기록한다.
- Profile 이름만 남기고 실제 Dependency를 숨기지 않는다.
- Profile 적용 후 Leaf Starter 추가·제외는 승인된 Override 규칙으로만 수행한다.
- Profile 변경으로 기존 Domain의 Dependency가 묵시적으로 바뀌지 않도록 Profile Version/Resolved Lock을 저장한다.

### Aggregate Starter

Aggregate Starter는 외부 Consumer가 하나의 Dependency로 안정된 조합을 사용해야 할 때만 검토한다.

```gradle
implementation "com.cpf.starter:cpf-starter-profile-domain-web-api:<version>"
```

Aggregate Starter가 승인되면 다음을 준수한다.

- 고유 업무 로직·AutoConfiguration·Bean을 소유하지 않는다.
- 승인된 Leaf Starter Dependency만 선언한다.
- 동일 조합을 Generator Profile과 공유하며 별도 두 번째 정본을 만들지 않는다.
- Consumer는 개별 Leaf Starter 선택 방식도 계속 사용할 수 있다.
- 제거 Compile, Transitive Dependency, JAR/WAR 포함 목록과 Footprint를 검증한다.
- Provider가 상호 배타적인 조합은 포함하지 않는다.
- `all`, `full`, `everything` 형태의 Mega Starter는 금지한다.

### Platform BOM

BOM은 Starter를 활성화하지 않고 Version·Compatibility만 정렬한다.

```gradle
implementation platform("com.cpf.platform:cpf-platform-bom:<version>")
implementation "com.cpf.starter:cpf-starter-messaging-kafka"
```

BOM 등록만으로 Kafka·Redis·Security Runtime이 들어오면 안 된다.

## 8.2 Profile·Bundle 충돌 규칙

다음 Provider는 기본적으로 상호 배타적 선택으로 관리한다.

```text
messaging: NONE | KAFKA | RABBITMQ
cache: NONE | CAFFEINE | REDIS
securityMode: NONE | SESSION_JDBC | RESOURCE_SERVER
persistence: NONE | MYBATIS | 승인된 다른 Provider
```

Profile이나 Aggregate Starter가 상호 배타 Provider를 동시에 전이하면 생성·Build 단계에서 fail-closed 한다.
공존이 필요한 특별한 Bridge/전환 시나리오는 별도 Requirement, Owner, Runtime Test와 운영 절차를 요구한다.

## 8.3 초기 Profile 후보

다음은 확정 Artifact가 아니라 다음 QA에서 Consumer와 Footprint로 검증할 후보이다.

| Profile 후보 | 기본 Leaf Starter 후보 | 제외·주의 |
|---|---|---|
| `DOMAIN_WEB_API` | Web MVC + OpenAPI Web MVC | Security·Persistence 자동 포함 금지 |
| `DOMAIN_SECURE_RESOURCE_API` | Web MVC + Resource Server + Observability | Permission은 Domain Owner |
| `DOMAIN_EVENT_KAFKA` | Kafka + Observability | Outbox/JDBC는 별도 선택 |
| `DOMAIN_CACHE_REDIS` | Redis Cache + Secret Registry | 고객 Secret Provider 구현은 별도 |
| `BROWSER_SESSION_RUNTIME` | Security Session JDBC + Observability | ADM/BZA Route·Permission 포함 금지 |
| `MINIMAL_DOMAIN` | 선택 Runtime 없음 | Core/Common 계약만 허용 |

Batch Worker·Gateway·ADM/BZA의 제품 고유 조합은 범용 Starter Profile로 숨기지 않고 각 Product Build 또는 Product 전용 Convention에서 관리한다.

## 9. Messaging과 RabbitMQ

현재 최상위 정본의 Primary Messaging은 Kafka다. RabbitMQ는 현재 공식 구현이 아니다.
다음 중 하나를 Architecture Decision으로 확정한다.

1. Kafka-only 공식 지원
2. `cpf-starter-messaging-rabbitmq`를 공식 선택 Adapter로 지원
3. Customer Plugin SPI로만 허용

RabbitMQ를 공식 채택하면 Exchange·Queue·Routing Key, Publisher Confirm/Return, ACK/NACK, DLQ, Retry, Poison Message, Ordering, Duplicate, Result Unknown, Reconcile, Multi-instance, TLS·Secret, 운영 재처리와 Runtime Evidence를 Kafka와 동등한 수준으로 제공해야 한다.

## 10. 세분화 후보

세분화는 이름을 먼저 늘리는 작업이 아니다. Consumer·Dependency·배포 조합 검증으로 필요성이 입증된 경우에만 수행한다.

```text
cpf-starter-security
cpf-starter-security-session-jdbc
cpf-starter-security-resource-server

cpf-starter-cache
cpf-starter-data-cache-caffeine
cpf-starter-cache-redis

cpf-starter-webmvc
cpf-starter-profile-web-api
cpf-starter-integration-http-client
cpf-starter-data-persistence-mybatis
```

위 이름은 확정 Artifact가 아니라 다음 QA 검토 후보이다.

## 11. Lifecycle

```text
PROPOSED
→ ARCHITECTURE_APPROVED
→ IMPLEMENTED
→ CONSUMER_CONNECTED
→ RUNTIME_VERIFIED
→ GA
→ DEPRECATED
→ REMOVED
```

각 단계에서 `development_status`와 `verification_status`를 분리한다.
Consumer가 없는 Starter는 GA가 아니며, 제거할 때에는 Consumer 0, BOM·Generator·Guide·Config 참조 0, 대체 경로와 Delete Manifest가 필요하다.

## 12. 필수 Gate

- Root Inventory에 `cpf-starters=FIXED_PRODUCT_CONTAINER`
- Starter를 Generated Domain으로 오인하지 않음
- 역방향·순환·Internal Package 의존 0
- Starter별 독립 JAR·POM·Sources·JavaDoc 생성
- Platform BOM에 모든 GA Starter 제약 등록
- 사용하지 않는 Starter 제거 Compile·Runtime
- AutoConfiguration이 Classpath·Property·Bean 조건을 준수
- 모든 GA Starter의 실제 Product Consumer 또는 승인된 전략적 Reference Consumer
- Generator 선택 결과와 Generated Domain Manifest 일치
- 최종 JAR/WAR 내부 포함 Dependency 검증
- Fresh Clone LOCAL_DEV·REMOTE·OFFLINE Publication 검증
- Guide·Deliverable·OpenAPI·JavaDoc·EDU 정합성

## 13. 문서 갱신 규칙

Starter 세분화 개발은 향후 계속될 수 있다. 다음이 변경되면 같은 작업 범위에서 반드시 갱신한다.

- 이 정책
- `CPF_REPOSITORY_SURFACE_INDEX.md`
- 최상위 목표 정본의 공식 Module·기술 Stack·의존 방향
- Starter 선택 가이드
- Generator 옵션과 Domain Manifest Schema
- Platform BOM·Version Manifest
- 개발자·운영자·설치·업그레이드 Guide
- Reference/EDU와 Deliverable 구성표
- Next QA Requirement·Evidence

날짜별 설명 문서를 계속 추가하지 않고 이 정책과 역할별 정본을 갱신한다.

## 14. 2026-08-02 Target Starter Architecture

### 14.1 현재 추적된 7개 Leaf Starter

- `cpf-starter-security`
- `cpf-starter-messaging-kafka`
- `cpf-starter-cache`
- `cpf-starter-platform-operations-observability`
- `cpf-starter-resilience`
- `cpf-starter-featureflag`
- `cpf-starter-security-secret`

위 7개는 정식 제품 Artifact지만 세분화·Core 이관·실제 Consumer·Runtime Evidence가 모두 끝났다는 의미는 아니다.

### 14.2 P0 분리·신규 대상

| Target Artifact | 주요 Source/Capability | 원칙 |
|---|---|---|
| `cpf-starter-foundation-base` | 최소 Boot bridge, CPF contract import | Web/DB/Broker/Cache/Security UI/Batch를 포함하지 않음 |
| `cpf-starter-aop-service-access` | `CpfAopConfig`, `ServiceAccessAspect` | AspectJ가 Core에 강제 전이되지 않음 |
| `cpf-starter-data-persistence-jdbc` | DataSource/JdbcTemplate, JDBC readiness | Vendor driver는 Consumer/Runtime Profile에서 선택 |
| `cpf-starter-data-persistence-mybatis` | `CpfMyBatisConfig`, mapper resource | JDBC Starter 위에 명시적으로 선택 |
| `cpf-starter-webmvc` | Header/Error/Validation/Servlet bridge | non-Web Runtime에 Servlet 강제 금지 |
| `cpf-starter-profile-web-api` | Springdoc/Scalar/OpenAPI UI | API 계약과 UI Runtime을 분리 |
| `cpf-starter-integration-http-client` | RestClient/WebClient, identity/trace/deadline | Resilience 정책과 중복 Primary 금지 |
| `cpf-starter-messaging-reliability-jdbc` | Broker worker, Outbox/Inbox/DLQ/JDBC ledger | Core에는 Envelope·Port·Result 계약만 유지 |
| `cpf-starter-messaging-jms` | Jakarta JMS 공통 Adapter | Provider-neutral |
| `cpf-starter-messaging-ibm-mq` | IBM MQ Provider | JMS Starter에 의존 |
| `cpf-starter-messaging-rabbitmq` | RabbitMQ/AMQP Provider | Kafka/JMS와 동시 Provider 충돌 정책 필요 |
| `cpf-starter-integration-tcp` | persistent TCP, framing, heartbeat, reconnect | 기관별 Layout/Mapping은 Domain Adapter가 소유 |
| `cpf-starter-platform-operations-channel-registry-jdbc` | `JdbcCpfChannelRegistryAdapter` | Channel 계약은 Core 유지 |
| `cpf-starter-file-attachment` | Attachment AutoConfiguration·storage adapter | 업무 Attachment 정책은 Owner 유지 |
| `cpf-starter-archive` | ZIP/TAR/GZIP Runtime 조립 | 순수 bounded archive 계약은 Core 유지 가능 |
| `cpf-starter-idempotency-jdbc` | JDBC idempotency ledger | 다수 Consumer 확인 후 공통 Adapter화 |
| `cpf-starter-security-session-jdbc` | BFF Session/JDBC Credential Vault | 기존 Security Starter 분리 |
| `cpf-starter-security-resource-server` | OAuth2/JWT/mTLS resource server | Session DB를 강제하지 않음 |
| `cpf-starter-platform-operations-runtime-control-client` | Runtime Control client/projection bridge | Control Server 자체는 Admin/Batch/Gateway Owner 유지 |

### 14.3 Core에 남길 항목

- 표준 Identifier, Header, Context, Error, Validation
- Public API/SPI와 순수 DTO/Enum/immutable model
- topology-independent Local/Remote 계약
- Provider-neutral Message Envelope·Port
- 순수 Java fixed-length/file/archive algorithm 중 외부 Runtime을 강제하지 않는 부분
- 보안·감사·마스킹의 계약과 taxonomy
- Starter/Owner Module이 구현할 확장 SPI

### 14.4 Domain Capability Profile

Generator는 Profile을 다음처럼 해석하고 결과를 Domain Manifest에 고정한다.

```text
profileId
profileVersion
resolvedStarters[]
resolvedStarterVersions{}
providerSelections{}
configTemplates[]
testFixtures[]
```

초기 Profile 후보:

- `MINIMAL_CONTRACT_CONSUMER`
- `MINIMAL_BOOT_DOMAIN`
- `DOMAIN_WEB_API`
- `SECURE_RESOURCE_API`
- `BROWSER_BFF_SESSION`
- `PERSISTENCE_MYBATIS`
- `EVENT_KAFKA`
- `EVENT_JMS_IBM_MQ`
- `EVENT_RABBITMQ`
- `INTEGRATION_TCP`
- `OBSERVABLE_RESILIENT_SERVICE`

### 14.5 Aggregate Starter

대표 Starter 하나가 다른 Starter를 자동 포함하는 방식은 가능하다. 승인된 Aggregate Artifact는 POM 전이 Dependency만 소유하고 Bean·AutoConfiguration·업무 정책을 추가하지 않는다.

예:

```text
cpf-starter-bundle-event-kafka
  → messaging-reliability-jdbc
  → messaging-kafka
  → observability
  → resilience

cpf-starter-bundle-event-jms-ibm-mq
  → messaging-reliability-jdbc
  → messaging-jms
  → messaging-ibm-mq
  → observability
  → resilience
```

Generator Profile을 기본 방식으로 사용하고 Aggregate Starter는 외부 Consumer가 단일 Dependency를 요구하는 안정 조합에만 제공한다.

## 15. 2026-08-08 Starter Developer Experience·신규 Capability 고도화

### 15.1 완료 기준

Starter의 완료 조건은 Dependency/AutoConfiguration/Interface/Sample 존재가 아니다. Canonical Starter Catalog의 활성 Starter **전부**를 실제 개발자 Consumer 기준으로 다시 검증한다.

각 Starter는 최소 다음을 만족한다.

- OSS 직접 사용보다 반복 코드와 설정이 실질적으로 감소한다.
- Typed CPF convenience API 또는 업무 의도 중심 API를 제공한다.
- safe default와 최소 Config를 제공한다.
- 오설정·Provider 충돌·필수 보안 설정 누락은 startup/build 시 Fail-Fast한다.
- 필요 시 per-call/고급 Override가 가능하다.
- underlying OSS Native API/extension point를 고급 사용자가 사용할 수 있는 Escape Hatch가 있다.
- transactionId/context, 표준 Error, Security/Authorization, Masking, Audit, Logging, Observability를 특성에 맞게 자동/최소 설정으로 연결한다.
- 외부 호출/비동기 기능은 Timeout/Retry/Circuit Breaker/Idempotency/UNKNOWN/Reconcile을 일관되게 연결한다.
- 미사용 시 Bean/Dependency/Config/SQL/Migration Side Effect가 0이다.
- 실제 Product/Generated Domain/Reference Consumer와 executable EDU가 있다.
- 정상뿐 아니라 Timeout/Retry/Partial/UNKNOWN/Process Kill/Multi-instance 경계를 검증한다.

`wrapper-only`, `consumer-less`, `metadata-only`, `sample-only`는 완료로 판정하지 않는다.

### 15.2 신규/보강 Capability

- **AI Optional**: Provider-neutral API/SPI, routing, resilience, masking, usage/cost, audit, transactionId, policy/approval, failure/UNKNOWN. 자체 LLM/Vector DB/대형 Agent Platform은 기본범위에서 제외한다.
- **OAuth2/JWT**: 기존 resource-server를 유지하면서 `currentUserId/currentTenantId/currentPrincipal/hasRole/hasScope`, safe claim access, issuer/audience/expiry, role/scope/claim mapping, token propagation과 표준 401/403 개발 경험을 고도화한다.
- **SSO/OIDC**: 기존 Security 구조 위 Optional Capability로 OIDC/OAuth2 Login, Keycloak/Entra ID/Okta, User/Role/Group/Claim mapping, CPF User/Tenant/Authority Context, login/logout/session/token refresh와 Frontend/BFF를 연결한다. SAML2는 Optional이다.
- **KMS/HSM**: 기존 `CpfSecretProvider`/Crypto를 확장하여 KMS/HSM/PKCS#11, key version/rotation/revocation/provider health/timeout/audit를 지원하며 key 원문 노출을 금지한다.
- **Digital Signature**: 기존 Crypto/Secret/Certificate를 재사용하여 sign/verify/keyId/keyVersion/certificate/signature metadata/audit를 범용화한다.
- **Hash/HMAC/AES-GCM**: 기존 Crypto를 재사용하며 중복 Starter를 만들지 않는다.
- **Tamper-evident Audit**: 별도 유행성 Starter 대신 기존 Audit/Logging/Security를 append-only/hash-chain/signature/concurrency/multi-instance/delete detection 수준으로 완성한다.
- **Blockchain/DLT**: 이번 범위에서 신규 Starter를 만들지 않는다.

### 15.3 Transaction Starter/Capability

`LOCAL / XA-JTA / OUTBOX / INBOX-DEDUP / SAGA / TCC`를 서로 대체 관계가 아니라 선택 가능한 Transaction Strategy로 제공한다.

- XA/JTA는 Optional이며 `cpf-core`에 Narayana/Atomikos 등 특정 구현을 강제하지 않는다.
- Tomcat은 standalone JTA Transaction Manager Adapter로, JTA-capable WAS는 managed JTA Adapter로 지원한다.
- Outbox/Saga/UNKNOWN/Reconcile 기존 구현은 유지·고도화한다.
- DB+Kafka/RabbitMQ/Event는 Outbox 우선, 강한 DB+DB/DB+JMS 원자성이 필요할 때 XA/JTA를 선택할 수 있게 한다.
- Hold/Reservation은 TCC를 선택할 수 있게 한다.
- 모든 전략은 transactionId/log/audit/trace/ADM Timeline에 연결한다.

### 15.4 Catalog Admission

신규 AI/JTA/SSO Capability 이름만 먼저 Canonical `modules`에 등록하여 `settings.gradle`을 깨뜨리지 않는다. 개발 사이클에서 실제 폴더·`build.gradle`·AutoConfiguration·Consumer·Test·Profile/Group owner를 만든 뒤 **같은 변경 단위에서** settings/BOM/Catalog/Generator/Publication을 동기화한다.

