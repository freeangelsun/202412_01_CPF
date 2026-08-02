
# CPF Core·Base Starter 최종 Architecture 결정

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch/SHA: `master` / `1eda8e12fe123281748a4388938c62f11819da1e`
- 결정 상태: 다음 QA 구현 기준선
- development_status: 부분 구현
- verification_status: 미검증

## 1. 최종 결정

`cpf-core`는 Starter 내부로 흡수하지 않는다.

```text
cpf-core
  topology-independent 초경량 계약 JAR

cpf-starter-base
  CPF Spring Boot Runtime의 최소 조립 Starter 후보

cpf-starter-*
  Web, Security, Messaging, Cache, Persistence 등 선택 기능
```

Spring 생태계의 Core Library와 Boot Starter 계층을 함께 사용하는 방식과 동일한 원칙을 CPF에 적용한다.

## 2. cpf-core 책임

Core에는 다음만 남긴다.

- Public API/SPI
- 표준 값 타입과 식별자
- 오류·결과·실행 문맥 계약
- Broker·Cache·Secret·Idempotency 등의 기술 중립 Port
- Local/Remote·Async 계약
- Version·Compatibility Metadata 계약
- Spring Boot Runtime 없이 컴파일 가능한 최소 구현

Core에서 제거·이관을 검토한다.

- MyBatis Runtime
- AspectJ Runtime 조립
- Servlet·Web MVC Adapter
- OpenAPI UI와 Scalar
- OpenTelemetry SDK·Exporter
- HTTP Client Runtime 조립
- 선택 Validation Provider
- 특정 DB·Broker·Cache Provider

Core는 Starter를 역참조하지 않는다.

## 3. cpf-starter-base 책임

`cpf-starter-base`는 다음 QA에서 구현 여부와 정확한 Artifact 이름을 확정한다.

허용 후보:

- `cpf-core` 전이
- CPF Platform/Component Metadata 검증
- 최소 AutoConfiguration 등록
- 기본 Configuration Properties 검증
- CPF 실행 문맥의 Spring Boot 최소 조립
- Starter Catalog·Provider 충돌 검증 Hook
- 안전한 Lifecycle/Readiness 기반
- 기본 MDC/Context Bridge의 기술 중립 부분

금지:

- Web MVC
- OpenAPI UI
- JDBC·MyBatis·Flyway
- Kafka·RabbitMQ
- Redis·Caffeine
- Spring Session JDBC
- Resource Server
- OTel Exporter
- Batch Runtime
- ADM/BZA·Gateway·Domain 고유 정책
- 모든 기능을 전이하는 Mega Starter

## 4. Consumer별 Dependency 모델

### 일반 CPF Spring Boot Domain

```gradle
implementation platform("com.cpf.platform:cpf-platform-bom:<version>")
implementation "com.cpf.starter:cpf-starter-base:<version>"
```

필요 기능만 추가한다.

```gradle
implementation "com.cpf.starter:cpf-starter-webmvc:<version>"
implementation "com.cpf.starter:cpf-starter-messaging-kafka:<version>"
```

### 계약 전용·비 Spring Consumer·고객 SPI 구현

```gradle
implementation platform("com.cpf.platform:cpf-platform-bom:<version>")
implementation "com.cpf.platform:cpf-core:<version>"
```

Base Starter를 강제하지 않는다.

### Core API를 직접 사용하는 Boot Domain

다음 QA에서 두 정책을 비교해 확정한다.

1. `cpf-starter-base`의 `api` 전이만 사용
2. Dependency 명시성을 위해 `cpf-core`도 직접 선언

어느 방식을 선택하든 다음을 만족해야 한다.

- 실행 Artifact에 Core JAR이 중복 포함되지 않는다.
- Dependency Analysis가 숨은 의존성을 차단한다.
- Generator Manifest에 Base와 해석된 Core Version을 기록한다.
- Base Starter 제거 Compile Test를 제공한다.

## 5. cpf-common 판정

`cpf-common`은 Starter가 아니다.

```text
cpf-common
  고객 업무 공통 Library

cpf-starter-*
  선택 기술 Runtime Adapter
```

Common은 모든 Domain의 필수 Dependency가 아니다.
공통 코드·달력·메시지·업무 검증 등 실제 고객 업무 공통만 소유한다.

Common에서 다음을 제거·이관한다.

- Redis Connection Factory
- Cache Provider Runtime
- 기술 AutoConfiguration
- 고객 업무와 무관한 Scheduling/Infrastructure 조립
- 선택 OSS Runtime의 강제 전이

Common 세분화는 실제 Consumer와 독립 Release 필요성이 입증될 때만 수행한다.

## 6. Generator 기본 Profile

초기 후보:

```text
MINIMAL_CONTRACT_CONSUMER
  → cpf-core

MINIMAL_BOOT_DOMAIN
  → cpf-starter-base

DOMAIN_WEB_API
  → cpf-starter-base
  → cpf-starter-webmvc
  → cpf-starter-openapi-webmvc

DOMAIN_EVENT_KAFKA
  → cpf-starter-base
  → cpf-starter-messaging-kafka
  → cpf-starter-observability
```

Profile은 최종 Leaf Dependency와 Version을 Build·Manifest에 명시적으로 기록한다.

## 7. 완료 조건

- Core Runtime Dependency 전수 분류
- Core 독립 Fresh Clone 소비
- Base Starter 최소성·제거 Compile
- 비 Spring Consumer에서 Boot Runtime 전이 0
- 최소 Boot Domain에서 Web·DB·Messaging 전이 0
- Common 기술 Runtime 제거
- Generator Profile·BOM·POM·Guide 일치
- JAR/WAR 포함·제외 Evidence
- Upgrade·Rollback·Mixed Version 검증
