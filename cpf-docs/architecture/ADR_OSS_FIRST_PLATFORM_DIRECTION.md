# ADR — CPF OSS-first Platform Direction

- 상태: `APPROVED FOR QA32 DEVELOPMENT`
- 기준일: 2026-07-30
- 적용 기준 Source: `3249581e8f01dcb546bc6601c31aee525f564d21`
- 법률 고지: 기술·라이선스 운영 결정이며 최종 판매·배포 전 정확한 버전과 배포 형태를 법무가 확인해야 한다.

## 1. 결정 원칙

범용 기술 엔진과 UI Widget을 CPF가 중복 개발하지 않는다. 검증된 permissive OSS를 Primary Path로 사용하고 CPF는 다음만 소유한다.

- 배포 독립 Public API/SPI
- 정책과 Safety Ceiling
- 승인·권한·사유
- Version·Checksum·Fencing
- Audit·Ledger·Timeline
- Unknown Result·Reconciliation·Recovery
- Control Plane과 Product-specific UX
- Evidence·Release Governance

## 2. 승인된 Primary Stack

| 영역 | 결정 |
|---|---|
| UI Widget | Element Plus |
| 고급 Data Table | TanStack Table |
| Frontend Router | Vue Router |
| Client State | Pinia |
| Server State | TanStack Vue Query |
| Form Validation | Zod + Element Plus Form |
| ADM/BZA API Client | Orval |
| Browser Security | BFF + Spring Security + Spring Session JDBC |
| Gateway | Spring Cloud Gateway Server Web MVC + Embedded Tomcat |
| Messaging | Apache Kafka |
| Unit Messaging | CPF In-memory Test Adapter |
| Kafka Integration | Testcontainers Kafka |
| Resilience | Spring Cloud CircuitBreaker + Resilience4j |
| DB Migration | Flyway OSS Core |
| Observability | Micrometer Observation + OpenTelemetry OTLP |
| Local Cache | Caffeine |
| License/SBOM | CycloneDX + ORT + Syft |
| Vulnerability | Grype |
| Browser E2E | Playwright |

## 3. 제한 또는 선택 범위

- Spring Batch: CPF 배치 전체의 Primary Execution Engine. 모든 Job·Step·Tasklet·Chunk·Reader/Processor/Writer·JobRepository·ExecutionContext·Checkpoint·Restart·Stop·Abandon·Flow·병렬 Step·Local/Remote Partitioning·Remote Chunking·Remote Step·Center-Cut·Worker 분산 실행을 소유
- db-scheduler: 기본 persistent Trigger
- Quartz: 고급 Calendar/JTA 요구가 구체적으로 입증될 때만
- Valkey: 분산 Cache 선택 Provider, Server binary는 제품에 번들하지 않음
- OpenFeature: Feature Flag 범위가 실제로 존재할 때만 CPF Provider와 함께
- SecretProvider SPI: 외부 고객 관리 Secret Service와 연결
- Flowable OSS: 동적 사람 중심 Workflow의 필요성이 ADR threshold를 통과할 때만

## 4. 명시적 제외

- PrimeVue 최신 및 PrimeVue 유료 자산
- `cpf-gateway-webflux.jar`
- WebFlux/Netty Gateway
- Envoy Data Plane
- Kafka와 병행되는 Artemis/RabbitMQ Product Primary
- Redis Server 기본 번들
- HashiCorp Vault Server 기본 번들
- Flyway Teams/Enterprise
- Flowable 상용 제품
- Unknown/NOASSERTION 또는 승인되지 않은 Copyleft/Source-available dependency

## 5. Gateway 결정

현재는 SCG Server Web MVC 하나만 개발한다.

- Artifact: `cpf-gateway.jar`
- Runtime: Embedded Tomcat
- Packaging: executable BootJar
- `bootWar`: disabled
- WebFlux Artifact: 생성 금지
- Scale strategy: stateless horizontal scale-out
- Future WebFlux/Envoy 검토 조건: 실제 부하 시험에서 장기 연결·Streaming·자원 비용 목표를 Web MVC scale-out으로 충족하지 못했을 때 별도 ADR

CPF Route/Policy Model은 SCG Type을 Public Contract로 노출하지 않지만, 미래 가능성만을 위해 과도한 추상화 Framework를 만들지는 않는다.

## 6. Messaging 결정

Kafka가 유일한 Product Messaging Primary다.

- Unit Test: deterministic In-memory Adapter
- Integration: Testcontainers Kafka 필수
- Delivery Contract: at-least-once + consumer idempotency
- Exactly-once는 DB side effect를 포함한 별도 증명이 없으면 제품 보장으로 선언하지 않음
- AMQP는 기본 의존성과 Primary Path에서 제거

## 7. Browser 결정

ADM/BZA Browser는 Server-side Session만 사용한다.

- Secure + HttpOnly + SameSite Cookie
- CSRF
- Session fixation 방지
- Force logout
- Role/Permission 변경 재검증
- ADM/BZA namespace 분리
- Browser readable token persistence 금지

## 8. OSS Migration 완료 규칙

Dependency 추가가 아니라 Consumer 이관과 Legacy 제거가 완료 기준이다.

`Inventory → License → ADR → Adapter → Parity → Failure/Recovery → Consumer Migration → Legacy Removal → Artifact/SBOM → exact-SHA Evidence`

## 9. 공식 참고 URL

- Element Plus: https://github.com/element-plus/element-plus
- TanStack Table: https://github.com/TanStack/table
- Vue Router: https://github.com/vuejs/router
- Pinia: https://github.com/vuejs/pinia
- TanStack Query: https://github.com/TanStack/query
- Zod: https://github.com/colinhacks/zod
- Orval: https://orval.dev/
- Spring Cloud Gateway: https://docs.spring.io/spring-cloud-gateway/reference/
- Spring Security: https://docs.spring.io/spring-security/reference/
- Spring Session: https://docs.spring.io/spring-session/reference/
- Apache Kafka: https://github.com/apache/kafka
- Testcontainers Kafka: https://java.testcontainers.org/modules/kafka/
- Resilience4j: https://github.com/resilience4j/resilience4j
- Spring Batch: https://github.com/spring-projects/spring-batch
- db-scheduler: https://github.com/kagkarlsson/db-scheduler
- Flyway: https://github.com/flyway/flyway
- Micrometer: https://github.com/micrometer-metrics/micrometer
- OpenTelemetry Java: https://github.com/open-telemetry/opentelemetry-java
- Caffeine: https://github.com/ben-manes/caffeine
- Valkey: https://github.com/valkey-io/valkey
- OpenFeature: https://openfeature.dev/
- ORT: https://github.com/oss-review-toolkit/ort
- CycloneDX Gradle: https://github.com/CycloneDX/cyclonedx-gradle-plugin
- Syft: https://github.com/anchore/syft
- Grype: https://github.com/anchore/grype
- Playwright: https://github.com/microsoft/playwright


## QA32 Primary Engine 정정 결정 (2026-07-31)

기존 `ADOPT_SCOPED` 해석을 폐기한다. Spring Batch는 CPF 배치의 단일 Primary Execution Engine이다. CPF는 배치 정의·버전·승인·권한·배포·Topology·Artifact/Agent/File/Shell 보안·감사·업무 식별자 연결·Fencing·`UNKNOWN_RESULT` 대사 등 Control Plane만 확장한다. CPF 원장은 Spring Batch `JobInstanceId`, `JobExecutionId`, `StepExecutionId`를 연결하며 Job/Step 상태·Checkpoint·재시작을 중복 소유하지 않는다. Scheduler는 Trigger만 소유하고 실제 실행은 `JobOperator`로 연결한다.
