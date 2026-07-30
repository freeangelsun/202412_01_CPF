# ADR — CPF OSS-first Platform Direction

- 상태: `APPROVED FOR QA32 DEVELOPMENT — PRIMARY ENGINE AMENDMENT APPLIED`
- 기준일: 2026-07-31
- 적용 기준 Source: `4f675c7f89998cdbba7202e6c83320a0a4421a1f`
- 변경 근거: `CPF_20260731_QA32_OSS_PRIMARY_ENGINE_STEERING.md`
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

### 1.1 Primary Engine 전환 원칙

`ADOPT_NOW`로 결정한 OSS는 단순 Dependency, Wrapper, Adapter 또는 일부 Sample Consumer만 추가하는 방식으로 완료할 수 없다.

- 해당 OSS가 맡기로 한 범용 기능은 실제 Product Consumer 전체에서 OSS를 실질적인 Primary Engine으로 사용한다.
- CPF가 동일 기능의 자체 엔진·상태기계·실행기·캐시·라우터·프록시·재시도 루프를 병행 정본으로 유지하지 않는다.
- CPF Wrapper는 보안·정책·감사·계약 안정화를 위한 얇은 경계여야 하며 OSS 기능을 다시 구현해서는 안 된다.
- 전환 완료 전의 임시 Dual Path는 기간·Owner·제거 조건이 기록된 Migration 단계에서만 허용한다.
- 완료는 `Consumer 전수 이관 + Legacy 제거 + Runtime/Fault/Recovery 검증 + exact-SHA Evidence`로 판정한다.
- OSS의 모든 기능을 무조건 사용하는 것이 아니라, CPF가 해당 OSS에 위임하기로 결정한 책임 범위에서는 부분 자체 구현 없이 표준 기능을 충분히 사용한다.

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
| Batch Primary Execution Engine | Spring Batch |
| Persistent Scheduler Trigger | db-scheduler |
| DB Migration | Flyway OSS Core |
| Observability | Micrometer Observation + OpenTelemetry OTLP |
| Local Cache | Caffeine |
| License/SBOM | CycloneDX + ORT + Syft |
| Vulnerability | Grype |
| Browser E2E | Playwright |

## 3. 제한 또는 선택 범위

- db-scheduler: 기본 persistent Trigger. 실제 Job 실행과 상태 정본은 Spring Batch에 연결
- Quartz: 고급 Calendar/JTA 요구가 구체적으로 입증될 때만
- Valkey: 분산 Cache 선택 Provider, Server binary는 제품에 번들하지 않음
- OpenFeature: Feature Flag 범위가 실제로 존재할 때만 CPF Provider와 함께
- SecretProvider SPI: 외부 고객 관리 Secret Service와 연결
- Flowable OSS: 동적 사람 중심 Workflow의 필요성이 ADR threshold를 통과할 때만

`ADOPT_SCOPED` 또는 선택형 결정은 OSS를 어설프게 일부 구현하라는 의미가 아니다. OSS에 맡긴 책임 범위는 표준 기능을 Primary Path로 사용하고, 선택하지 않은 제품 책임만 CPF에 남긴다.

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

CPF Route/Policy Model은 SCG Type을 Public Contract로 노출하지 않지만, 미래 가능성만을 위해 과도한 추상화 Framework를 만들지는 않는다. SCG Dependency만 추가하고 기존 Custom Proxy가 실제 요청을 처리하는 상태는 완료가 아니다.

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

## 8. Spring Batch 결정

Spring Batch를 CPF 배치 전체의 실질적인 Primary Execution Engine으로 사용한다.

### 8.1 Spring Batch 소유 범위

- Job·Step 정의와 실행
- Tasklet과 Chunk 처리
- ItemReader·ItemProcessor·ItemWriter
- JobInstance·JobExecution·StepExecution
- JobRepository·ExecutionContext
- Checkpoint·Restart·Stop·Abandon
- Flow·조건 분기·병렬 Step
- Local Partitioning·Remote Partitioning·Remote Chunking·Remote Step
- 센터컷 중앙 실행과 Worker 분산 실행
- 표준 실행 Metadata·처리 통계·Step 결과
- File·DB·API·Shell 작업의 실제 Batch 실행 생명주기

### 8.2 CPF 소유 범위

- Definition 등록·Version·승인
- 권한·사유·감사·운영 UX
- Artifact 배포·Host Agent 보안
- 실행 대상·Topology·환경 정책
- Secret Reference와 File/Shell 보안 정책
- Lease·Claim·Fencing
- 응답 유실 시 `UNKNOWN_RESULT` 판정·대사·복구
- CPF 업무 원장과 Spring Batch 실행 ID 연결

CPF가 유지하는 위 항목은 Control Plane과 안전 정책이다. Spring Batch와 중복되는 Job/Step 실행 엔진, 자체 Restart/Checkpoint, 자체 Partition Dispatcher, 자체 Worker 완료 집계 및 이중 실행 Metadata 정본은 제거한다.

Scheduler는 Trigger만 소유하고 실제 실행은 Spring Batch `JobOperator` 또는 승인된 표준 실행 API로 연결한다. Trigger 성공을 Job 성공으로 기록하지 않는다.

## 9. OSS Migration 완료 규칙

Dependency 추가가 아니라 Consumer 이관과 Legacy 제거가 완료 기준이다.

`Inventory → License → ADR → OSS-native Design → Adapter/Policy Boundary → Parity → Failure/Recovery → Consumer Migration → Legacy Removal → Artifact/SBOM → exact-SHA Evidence`

모든 `ADOPT_NOW` 항목에는 다음 공통 Gate를 적용한다.

1. Product Consumer가 OSS Primary Path를 실제 사용한다.
2. 동일 책임의 자체 구현·Fallback·이중 정본이 제거되거나 명시적 Migration 기간으로 제한된다.
3. OSS 표준 Lifecycle·State·Extension Point를 우회해 자체 엔진을 재구현하지 않는다.
4. 정상·실패·중단·재시작·복구·Scale Scenario가 실제 Runtime에서 통과한다.
5. Dependency Tree, Runtime Artifact, SBOM, License, CVE, exact Source SHA가 서로 일치한다.
6. 일부 화면·일부 Job·샘플 Adapter만 전환한 상태는 `PARTIAL`이다.

## 10. 공식 참고 URL

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
- Spring Batch: https://docs.spring.io/spring-batch/reference/
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
