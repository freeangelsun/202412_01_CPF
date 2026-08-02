# CPF Starter 전체 설명과 선택 가이드 초안

> 다음 Core 경량화·Starter 세분화 QA와 고객 가이드 개정의 입력 초안이다.  
> 현재 `cpf-docs/guides/**`를 직접 덮어쓰지 않는다. Starter 구조가 세분화되면 이 문서를 역할별 정식 Guide에 반영하고 갱신한다.

## 1. Starter란

Starter는 특정 기술을 사용하는 데 필요한 CPF Adapter, Spring Boot AutoConfiguration, 기본 정책과 Dependency를 하나의 독립 JAR로 제공한다.

```text
애플리케이션이 필요한 Starter만 의존
→ Starter JAR이 애플리케이션 JAR/WAR에 포함
→ Classpath·Property·Bean 조건을 만족하면 자동 구성
→ 애플리케이션 고유 업무 정책과 연결
```

Starter는 별도 서버로 실행하지 않는다.

## 2. 언제 Starter를 사용하는가

다음 질문에 모두 가깝다면 Starter 후보다.

1. 특정 업무가 아니라 여러 Runtime이 선택할 기술 Adapter인가?
2. 사용하지 않는 Product/Domain에서는 완전히 빠져야 하는가?
3. Provider 또는 배포 방식이 교체될 수 있는가?
4. CPF Public Contract를 구현하는가?
5. AutoConfiguration과 표준 실패·복구 정책이 필요한가?

Owner 제품의 핵심 실행 기능은 Starter가 아니다.

- Batch Job·Scheduler·Worker → `cpf-batch`
- Gateway Route·Control Plane → `cpf-gateway`
- ADM/BZA 권한·승인·메뉴 → 각 Admin Module
- 업무 Entity·Transaction·SQL → Generated Domain
- 생성·설치·Migration Script → `cpf-tools`

## 3. 현재 Starter 사용 기준

| 기능을 사용할 때 | 연결 Starter | 확인할 설정·운영 | 연결하지 않는 경우 |
|---|---|---|---|
| ADM/BZA Browser Login, HttpOnly Session, CSRF, Credential Vault | `cpf-starter-security` | Session JDBC, Cookie, Origin, Encryption Key, Concurrent Session | Stateless API만 있으면 현 Session 구조를 강제하지 않고 세분화 결과 확인 |
| Kafka Event Publish, Broker Bridge, Batch Manager/Worker 메시징 | `cpf-starter-messaging-kafka` | Topic, Key, ACK, Retry, DLQ, Idempotency, Unknown Result | 단순 JVM 내부 호출에는 사용하지 않음 |
| Local Caffeine 또는 Redis Cache, 분산 Lock·Invalidation | `cpf-starter-cache` | Provider, TTL, Payload Limit, Secret, Durable Invalidation | Cache가 필요 없는 Domain에는 추가하지 않음 |
| Trace·Metric·Observation·OTLP Export | `cpf-starter-observability` | Exporter, Sampling, PII Masking, Queue/Backpressure, Readiness | 비활성 이유와 운영 영향이 명시돼야 함 |
| 외부 호출 Circuit Breaker·Timeout·제한된 Retry | `cpf-starter-resilience` | Operation별 Budget, Retryability, Fallback, Metric | Local Transaction 오류를 숨기는 용도로 사용 금지 |
| 동적 Release·Canary·Kill Switch | `cpf-starter-featureflag` | 승인 Provider, Secure Override, Audit, Default, 장애 시 정책 | Provider·운영 책임 없이 Boolean 분기용으로 추가 금지 |
| Runtime Secret Reference 해석·Provider Registry | `cpf-starter-secret` | Provider ID, Rotation, Cache TTL, Failure, Masking, Readiness | 평문 Config를 그대로 사용하는 상태를 완료로 보지 않음 |

## 3.1 하나씩 등록할지 묶음으로 등록할지

세 가지 사용법을 구분한다.

### 개별 Leaf Starter

기능을 정밀하게 선택할 때 사용한다.

```gradle
implementation platform("com.cpf.platform:cpf-platform-bom:<version>")
implementation "com.cpf.starter:cpf-starter-webmvc"
implementation "com.cpf.starter:cpf-starter-messaging-kafka"
```

### Generator Capability Profile

신규 Domain 생성 시 권장한다.

```text
Profiles = DOMAIN_WEB_API, DOMAIN_EVENT_KAFKA
```

Generator는 Profile을 실제 Leaf Starter Dependency로 풀어서 `build.gradle`과 Domain Manifest에 기록한다.
따라서 개발자는 Profile 한두 개를 선택할 수 있지만, 결과 Artifact에 무엇이 들어가는지는 숨겨지지 않는다.

### Aggregate Starter

기존 외부 프로젝트에서 하나의 Dependency가 필요한 경우에만 선택 제공을 검토한다.

```gradle
implementation "com.cpf.starter:cpf-starter-profile-domain-web-api:<version>"
```

Aggregate Starter는 편의 Artifact이며 구현 정본이 아니다. 고유 Bean·AutoConfiguration을 두지 않고 승인된 Leaf Starter만 전이해야 한다.

### BOM과의 차이

BOM은 Version을 맞출 뿐 Starter를 활성화하지 않는다. BOM만 등록했다고 Web·Kafka·Redis가 들어오는 것은 아니다.

## 3.2 묶음 선택 안전 규칙

- 개별 Leaf 선택은 항상 가능
- Profile 적용 결과를 Manifest에 기록
- Profile Version·Resolved Starter Lock 저장
- Kafka/RabbitMQ, Caffeine/Redis, Session/Resource Server의 기본 동시 선택 금지
- 모든 Starter를 포함하는 Mega Starter 금지
- 미선택 Starter의 Config·Bean·JAR 포함 금지
- Profile 변경은 Upgrade·Rollback 절차와 함께 수행

## 4. Product별 연결

### ADM/BZA

- Browser Session이 필요하면 Security Starter
- Trace·Metric이 필요하면 Observability Starter
- Remote Control 호출에는 Resilience 검토
- Feature Flag는 운영 승인·감사 모델이 완성된 경우만 선택
- Route·Permission·Menu·Approval은 Starter가 아니라 각 Module 소유

### Gateway

- Resilience와 Observability를 주요 선택
- Route·Trust Boundary·Attempt Ledger는 Gateway 소유
- 인증 형태에 따라 Security 세분화 Starter를 선택

### Batch

- Control Server·Scheduler·Worker의 Kafka/Observability는 현재 실제 Consumer
- Worker는 Secret·Resilience를 사용
- Job·Step·Scheduler·Lease·Fencing은 Batch 제품 소유이며 Starter로 이동하지 않음

### Generated Domain

Generator가 필요한 Capability만 선택한다.

```text
minimal domain
+ webmvc (API가 있을 때)
+ persistence-mybatis (MyBatis를 선택할 때)
+ security mode
+ messaging provider
+ cache provider
+ observability
+ resilience
+ feature flag
+ secret reference
```

선택 결과는 Build, Config, Test, Domain Manifest, Guide에 동시에 반영된다. 선택하지 않은 Starter의 Dependency·Config·Bean은 없어야 한다.

### Reference

공식 Starter의 실제 소비·장애·복구 예제를 제공한다. 모든 Starter를 무조건 포함한 Mega Reference 대신 조합별 시나리오를 제공한다.

## 5. 향후 세분화

다음 QA에서 필요성이 입증되면 Security Session JDBC/Resource Server, Cache Caffeine/Redis, Web MVC/OpenAPI, HTTP Client, Persistence MyBatis 등을 세분화할 수 있다.
세분화가 확정되면 이 초안과 개발자·운영자·설치 Guide, Generator 옵션과 Deliverable을 같은 변경 단위로 갱신한다.
