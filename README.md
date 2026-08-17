<div align="center">

<img src="cpf-docs/assets/readme/cpf-readme-hero.png" alt="Core Platform Framework - Spring Boot 기반 업무 개발·실행·연계·배치·운영 Framework" width="940" />

# Core Platform Framework

**Spring Boot 생태계를 기반으로 업무 개발 표준·연계·Batch·Gateway·운영을 일관된 계약으로 연결하는 Business Platform Framework**

검증된 Spring·Open Source 기반 기술은 그대로 활용하고, CPF는 그 위에 업무 개발 Golden Path, Public Starter/API, Generator, 실행 문맥, 운영·복구 계약을 추가합니다.

</div>

<br>
<hr>
<br>

## CPF 핵심 6가지

- **1. CPF가 무엇인가** — CPF(Core Platform Framework)는 **Spring Boot 기반 업무 시스템을 개발·실행·연계·배치·운영하는 방식을 표준화하는 Framework**입니다. 업무 코드는 CPF 내부가 아니라 업무 Domain에 두고, 일반 개발자는 Public Profile·Starter·API를 필요한 범위만 선택해 사용합니다.

- **2. 어떤 문제를 해결하는가** — 프로젝트마다 반복해서 만드는 Context, Transaction, Security, Logging/Audit, Domain 호출, 외부 연계, Cache, Messaging, Batch, 운영 기능을 공통 계약과 실행 구조로 연결합니다. 배포 구조가 달라져도 업무 코드가 특정 인프라 구현이나 호출 방식에 과도하게 묶이지 않도록 하는 것이 핵심입니다.

- **3. Spring Boot와 어떤 관계인가** — CPF는 Spring Boot를 대체하거나 다시 구현하지 않습니다. **Spring Boot가 애플리케이션 실행 기반이라면 CPF는 그 위에 업무 개발 표준, Starter 조합, Public API, Generator, Runtime·운영 계약을 추가하는 계층**입니다. Spring Transaction, Spring Security, Spring Batch, Spring Cloud Gateway, Spring Data, Spring Messaging 계열의 검증된 기반을 활용합니다.

- **4. 개발자가 실제 쓰는 Golden Path** — 일반 업무는 `@CpfController → @CpfService + @CpfTx → @CpfRepository`를 기본으로 합니다. 같은 Application 내부는 `service.method()`, CPF Domain 간 호출은 `domainClient.execute()`, CPF 밖 외부 연계는 `@CpfClient + @CpfTimeLimiter + @CpfRetry`가 적용된 Typed Client를 사용합니다.

- **5. 어떤 규모의 시스템에 적합한가** — 단일 Runtime의 일반 Web/API부터 여러 업무 Domain·다중 인스턴스·Gateway·Messaging·분산 Batch가 필요한 구성까지 단계적으로 확장할 수 있습니다. 작은 구성에 모든 Runtime을 강제하지 않고 필요한 Capability만 추가합니다.

- **6. 5분 안에 실행해 보는 방법** — 준비된 개발 환경에서는 Repository Root에서 `pwsh .\cpf-tools\build\tools\cpf-dev.ps1 run-local`로 로컬 Runtime을 실행하고, `status`로 상태를 확인한 뒤 `verify-fast`로 빠른 검증을 수행합니다. 자세한 명령은 [최소 시작 절차](#8-최소-시작-절차)에서 확인합니다.

<br>
<hr>
<br>

## CPF 구조 한눈에 보기

<img src="cpf-docs/assets/readme/cpf-quick-overview.png" alt="CPF Quick Overview - 업무 Domain에 업무 코드를 두고 Public Starter와 필요한 Runtime Capability를 선택하여 개발·실행·운영하는 구조" width="100%" />

> **핵심 해석** — 업무 코드는 Framework 내부가 아니라 업무 Domain에 위치합니다. 일반 개발자는 Public Starter/API를 사용하고 Web, Data, Security, Cache, Messaging, Gateway, Batch 같은 Capability를 필요한 만큼만 선택합니다.
>
> **이 구조의 장점** — 작은 Web/API 구성에서 시작해도 같은 개발 기준을 유지한 채 Domain 분리, 다중 인스턴스, Gateway, Messaging, Batch Runtime으로 확장할 수 있습니다. 기능을 많이 가진 Framework이지만 모든 기능을 한 애플리케이션에 강제하지 않습니다.

<br>

### 검증된 기술 기반과 CPF가 더하는 역할

CPF는 실행 엔진과 기반 기능을 새로 만들기 위해 Framework의 범위를 넓히지 않습니다. **Spring과 널리 사용되는 Open Source가 잘하는 영역은 그대로 활용하고, CPF는 업무 시스템에서 반복되는 개발 방식·호출 경계·운영 상태·추적·제어를 일관된 계약으로 연결하는 데 집중합니다.**

| 영역 | 검증된 실행 기반 | CPF가 추가하는 역할 |
|---|---|---|
| Application / Web | Spring Boot, Spring MVC | Public Profile/Starter, Context, 표준 Controller·Service·Repository 흐름 |
| Data / Transaction | Spring Transaction, JDBC, JPA, MyBatis | `@CpfTx`, DataSource/DB 계약, 3개 공식 DB Vendor Lifecycle, 공통 Data Operations |
| Security / Session | Spring Security, OAuth2/OIDC, Spring Session | 권한·승인·감사 계약, Service Identity, 업무 Context 연계 |
| Integration / Gateway | Spring Cloud Gateway, Resilience4j, Spring HTTP 계층 | Domain/External 호출 경계, Route·Policy·Target·Health·Audit 운영 계약 |
| Cache / Messaging | Spring Data Redis, Caffeine, Spring Kafka, AMQP/JMS | Provider 선택, 공통 API, Idempotency·신뢰성·운영 계약 |
| Batch / Scheduling | Spring Batch, Spring Batch Integration, db-scheduler | Control Plane, Worker/Agent, Center-Cut, Restart·Reprocess·Reconcile 운영 흐름 |
| Observability | Spring Boot Actuator, Micrometer, OpenTelemetry/OTLP | Transaction/Execution Context와 Log·Metric·Trace·Audit의 공통 상관관계 |

> **역할 구분** — CPF는 Spring/Java의 일반 기능을 숨기는 폐쇄형 API를 목표로 하지 않습니다. CPF가 표준 계약을 제공하는 영역은 Public API를 우선 사용하고, 일반 Java/Spring 기능은 필요한 범위에서 그대로 활용합니다. Internal Starter와 내부 Router/Executor는 일반 업무 개발 Surface와 분리합니다.
>
> **검증 원칙** — 기반 기술의 성숙도가 CPF 자체의 검증을 대신하지 않습니다. CPF가 추가한 기능은 **Source → Consumer → Test → Runtime → Evidence** 중 실제 수행 범위를 구분해 기록하며, 실행하지 않은 검증은 PASS로 표시하지 않습니다.

<br>

### 구현과 검증을 직접 확인하는 경로

아래 경로는 README의 주요 설명을 실제 Source와 계약에서 바로 확인하기 위한 대표 진입점입니다.

- [기술 Stack 정본](gradle/cpf-stack.properties) — Java / Spring Boot / Spring Cloud / Spring Batch 등 기준 버전
- [Canonical Starter Catalog](cpf-tools/generator/contracts/cpf-starter-catalog.json) — Public / Internal 가시성, Profile, Capability, Provider 계약
- [Gateway Build](cpf-gateway/build.gradle) — Spring Cloud Gateway 기반과 CPF Gateway Runtime 조합
- [Batch Runtime Build](cpf-batch/runtime/build.gradle) — Spring Batch / Spring Batch Integration 기반
- [개발·검증 도구](cpf-tools/build/tools/cpf-dev.ps1) — Build / Test / Verify / Local Runtime 진입점


<br>
<hr>
<br>

## 1. CPF 전체 구조

CPF는 **외부 요청 진입 → 업무 Domain → 공통 실행 기반 → 선택 Runtime → 데이터·외부 시스템**을 하나의 구조로 연결합니다.

<img src="cpf-docs/assets/readme/cpf-architecture-overview.png" alt="CPF 전체 Architecture - 업무 Domain, Public Starter/Profile, Core/Common 계약, Gateway/Batch Runtime, ADM/BZA, Generator, DB·Messaging·외부 시스템의 경계" width="100%" />

> **핵심 해석** — `cpf-core`는 Topology와 무관한 핵심 계약을 소유하고, 업무 코드는 `cpf-<domain>`에 둡니다. 일반 업무 개발 Surface는 Public Profile/Starter/API이며, Internal Module은 Framework 구현 세부사항입니다. Gateway·Batch·ADM/BZA는 각자의 Runtime/운영 책임을 갖고 업무 Domain과 분리됩니다.
>
> **이 구조의 장점** — 업무 코드와 기술 Provider·운영 Runtime의 책임을 분리해 Provider 교체나 배포 Topology 변경이 업무 Source 전체의 재작성으로 번지는 것을 줄입니다. 동시에 Spring 기반의 Native 기능을 사용할 수 있는 여지를 남겨 과도한 Framework 종속을 피합니다.

<br>
<hr>
<br>

## 2. CPF 구성 방식

구성은 **기본 실행 계약 → 필요한 Capability 선택 → 필요 시 업무 Domain 생성**의 흐름으로 이해하면 됩니다.

<img src="cpf-docs/assets/readme/cpf-composition-model.png" alt="CPF Composition Model - 공통 기반 위에 Public Profile, Starter, Provider를 선택하고 Generator로 업무 Domain을 생성하는 구성" width="100%" />

> **핵심 해석** — 개발자는 Framework 내부 모듈을 임의 조합하지 않고 Public Profile/Starter에서 시작합니다. Cache, Messaging, Persistence처럼 구현 선택이 필요한 영역은 Provider를 명시적으로 선택하고, Generator도 같은 Canonical Catalog를 사용합니다.
>
> **이 구조의 장점** — Dependency·AutoConfiguration·Generator·BOM·문서가 같은 Catalog 기준을 공유해 “동작은 하지만 조합이 제각각인 프로젝트”가 늘어나는 것을 줄입니다. Internal Leaf를 Public BOM에 노출하지 않아 사용 Surface도 통제합니다.

<br>
<hr>
<br>

## 3. 거래·연계 오케스트레이션

CPF의 오케스트레이션은 Remote 호출을 하나의 DB Transaction처럼 가장하지 않습니다. **Local Transaction, Domain Call, 외부 연계, Event, Batch 후속 처리의 경계를 명시하고 실행 결과를 추적**합니다.

<img src="cpf-docs/assets/readme/cpf-orchestration.png" alt="CPF 거래·연계 오케스트레이션 - Local Transaction, CPF Domain Call, External Integration, Event, Batch 후속 처리와 SUCCESS·FAILURE·UNKNOWN 결과 경계" width="100%" />

> **기반 기술** — Local Transaction은 Spring Transaction을 사용하고, Remote 호출·Messaging·외부 연계는 각 기술의 표준 실행 모델을 유지합니다. CPF는 이 경계 사이의 상태·추적·재처리 규칙을 연결합니다.
>
> **핵심 해석** — Local DB Transaction은 Spring Transaction 기반으로 처리하고, Remote·Messaging·외부 Side Effect는 동일 원자성으로 가정하지 않습니다. 분산 경계에서는 `SUCCESS / BUSINESS_FAILURE / TECHNICAL_FAILURE / UNKNOWN`을 구분하고 Retry·Idempotency·Reconcile·Compensation을 상황에 맞게 적용합니다.
>
> **이 구조의 장점** — 분산 시스템에서 위험한 “HTTP 호출까지 하나의 Transaction처럼 보이는 착시”를 피하면서도 Transaction ID·Execution Context를 이어 거래 관계를 추적할 수 있습니다. 정상 처리뿐 아니라 결과 미확정과 부분 실패를 운영 가능한 상태로 다룹니다.

<br>
<hr>
<br>

## 4. Gateway와 외부 진입 구성

Gateway는 **필수 진입점이 아니라 필요할 때 선택하는 Runtime**입니다. 시스템 규모와 외부 진입 정책에 따라 Direct, L4/Ingress, Gateway, L4/Ingress+Gateway 구성을 선택할 수 있습니다.

<img src="cpf-docs/assets/readme/cpf-gateway-topology.png" alt="CPF Gateway Topology - Direct, L4/Ingress, Spring Cloud Gateway 기반 CPF Gateway, L4/Ingress와 Gateway 조합의 선택 구조" width="100%" />

> **기반 기술** — Gateway Engine은 Spring Cloud Gateway Server MVC를 사용하고, Resilience4j 등 검증된 연계 기반을 조합합니다. CPF는 Engine을 다시 만드는 대신 Route·Policy·Target·Health·Audit의 운영 계약을 추가합니다.
>
> **핵심 해석** — CPF Gateway는 Spring Cloud Gateway Server MVC 기반 위에서 Route, 인증·인가 경계, 요청 제한, Target/Health, Canary, Runtime Policy와 Audit를 운영 관점으로 연결합니다. CPF Domain 간 내부 호출은 Gateway를 필수 경유하지 않고 Typed Domain Client를 사용합니다.
>
> **이 구조의 장점** — 검증된 Gateway Engine을 재구현하지 않고 CPF의 Route/운영 계약을 추가합니다. 단순 시스템은 Direct/L4로 가볍게 유지하고, 중앙 Route·Policy·Traffic 관리가 필요한 시점에만 Gateway를 도입할 수 있습니다.

<br>
<hr>
<br>

## 5. Batch Runtime

CPF Batch는 **Spring Batch를 대체하는 별도 Batch Engine이 아닙니다.** Job/Step/Chunk/Restart의 기본 실행 모델은 Spring Batch와 Spring Batch Integration을 활용하고, 그 위에 Scheduler·Control Plane·Worker·Agent·Center-Cut 운영 계층을 추가합니다.

<img src="cpf-docs/assets/readme/cpf-batch-runtime.png" alt="CPF Batch Runtime - Spring Batch 기반 Job 실행과 Scheduler, Control Plane, Worker, Agent, Center-Cut을 분리한 Batch 운영 구조" width="100%" />

> **기반 기술** — Job/Step/Chunk/Partition 실행은 Spring Batch와 Spring Batch Integration을 사용하고, Scheduling은 `db-scheduler` 기반을 활용합니다. CPF는 그 위에 실행 제어·Worker/Agent·Center-Cut·결과 상태 관리 계층을 추가합니다.
>
> **핵심 해석** — Scheduler는 `db-scheduler`와 CPF의 실행/Lease 계약을 사용하고, Worker는 Spring Batch Job/Step/Partition을 실행합니다. Agent는 승인된 Runtime 작업의 설치·기동·중지·재시작·상태·Rollback·Drain/Resume를 담당하며, Center-Cut은 대량 대상의 선정·Preview·분할 실행·실패 대상·대사 흐름을 관리합니다.
>
> **이 구조의 장점** — 이미 성숙한 Spring Batch 실행 엔진은 그대로 활용하고, 실제 운영에서 필요한 Runtime 제어와 대량처리 오케스트레이션을 별도 책임으로 분리합니다. `FAILED`와 결과 미확정 `UNKNOWN_RESULT`를 구분하고 Restart·Rerun·Reprocess·Reconcile을 서로 다른 운영 행위로 관리합니다.

<br>

상세 Job 개발은 **배치 개발자 가이드**, 실행·중단·재시작·재처리·대사는 **배치 운영 가이드**에서 다룹니다.

<br>
<hr>
<br>

## 6. 기능 선택

CPF는 모든 기술을 한 프로젝트에 강제하지 않습니다. 개발 목적을 먼저 정하고 Public Capability와 Provider를 선택합니다.

<img src="cpf-docs/assets/readme/cpf-starter-selection.png" alt="CPF Starter Selection - Public Profile과 Capability Group에서 필요한 Starter를 선택하고 Redis·Valkey·Caffeine, JDBC·JPA·MyBatis, Kafka·RabbitMQ 등 Provider를 조합하는 구조" width="100%" />

> **기반 기술** — Spring Boot AutoConfiguration과 표준 Dependency 관리 방식을 유지하면서 CPF가 Profile·Starter·Provider 선택 규칙을 Canonical Catalog로 정리합니다.
>
> **핵심 해석** — Starter는 단순 Dependency 묶음이 아니라 Dependency·설정·AutoConfiguration·Public API·Provider 선택을 하나의 진입점으로 정리합니다. Public Starter와 Internal Leaf의 가시성은 Canonical Catalog에서 관리합니다.
>
> **이 구조의 장점** — 일반 개발자가 저수준 구현 클래스를 모두 학습하지 않아도 필요한 Capability부터 선택할 수 있고, Redis/Valkey, JDBC/JPA/MyBatis, Kafka/RabbitMQ 같은 기술 선택을 업무 코드와 분리할 수 있습니다. 필요한 경우에는 Native Spring/Provider 기능으로 내려갈 수 있는 경계도 유지합니다.

<br>

정확한 Starter·API·Property 계약은 **Specification / 기술 명세**에서 확인합니다.

<br>
<hr>
<br>

## 7. 개발에서 운영까지

CPF는 업무 Domain 생성부터 개발·검증·실행·관측·운영까지 서로 다른 도구가 같은 계약을 바라보도록 구성합니다.

<img src="cpf-docs/assets/readme/cpf-development-operation-lifecycle.png" alt="CPF Development Operation Lifecycle - Generator, 업무 개발, Test/Verification, Runtime, ADM/BZA 운영과 Evidence가 이어지는 흐름" width="100%" />

> **핵심 해석** — Generator가 생성한 Domain은 Public Starter/API를 사용하고, Build/Test/Verification은 같은 Catalog·계약을 검증합니다. 운영 단계에서는 Transaction/Execution Context를 기준으로 Log·Metric·Trace·Audit와 Runtime 상태를 연결합니다.
>
> **이 구조의 장점** — 개발 표준을 문서에만 두지 않고 Generator·Build Gate·Runtime·운영 화면까지 연결해 프로젝트별 편차를 줄입니다. 검증되지 않은 항목을 성공으로 간주하지 않고 Source/Test/Runtime Evidence를 구분해 관리할 수 있습니다.

<br>

### 검증 방식과 확인 가능한 근거

CPF는 **기능이 존재하는 것과 Runtime에서 검증된 것을 구분**합니다. 주요 주장은 가능한 범위에서 Source·실제 Consumer·Test·Runtime 결과·Evidence로 이어지게 하고, 현재 수행 여부는 Evidence에 남깁니다.

| 검증 영역 | 검증에서 확인하는 범위 | 확인 위치 |
|---|---|---|
| Build / Contract | Root 구성, Dependency/Ownership, Starter Catalog, Build/Test Gate | `cpf-tools/build/`, `cpf-tools/verification/` |
| Database | Oracle·PostgreSQL·MariaDB, Install/Migration/Seed/Upgrade/Rollback/Reapply | `cpf-tools/db/verification/` |
| Cache / Messaging / Integration | 연결, Retry, Idempotency, 재기동, 중복·실패·결과 미확정 처리 | Verification / FullLocal Evidence |
| Batch / Gateway | Worker·Process Kill·UNKNOWN/Reconcile, Route·Policy·실패 경계 | Runtime / FullLocal Evidence |
| ADM / BZA | OpenAPI Generated Client, Frontend Build, 오류 상태, E2E·접근성 | Frontend / FullLocal Evidence |
| Evidence Integrity | Source identity, Manifest, SHA-256, 실행 결과, Corruption negative test | `cpf-docs/work/TEST_AND_EVIDENCE.md` |

> **상태 해석** — `IMPLEMENTED`, `STATIC_VERIFIED`, `RUNTIME_VERIFIED`, `VERIFIED_WITH_EVIDENCE`, `PENDING_RUNTIME`를 구분합니다. **실행하지 않은 항목을 PASS로 올리지 않는 것**이 검증 원칙입니다.
>
> **재현 가능한 진입점** — 빠른 검증은 `verify-fast`, 전체 로컬 검증은 [`run-cpf-local-full-validation.ps1`](cpf-tools/verification/tools/run-cpf-local-full-validation.ps1), 현재 수행 결과와 미검증 범위는 [`TEST_AND_EVIDENCE.md`](cpf-docs/work/TEST_AND_EVIDENCE.md)에서 확인할 수 있습니다.

<br>

상세 개발 절차는 **프레임워크 개발자 가이드·배치 개발자 가이드**, 운영 절차는 **운영자 매뉴얼·배치 운영 가이드**에서 확인합니다.

<br>
<hr>
<br>

## 8. 최소 시작 절차

**요구 환경**

- Java: `25`
- Gradle Wrapper: Repository 포함
- Spring Boot: `4.1.0`
- 공식 DB Vendor: Oracle / PostgreSQL / MariaDB

Repository Root에서 아래 세 명령부터 시작하면 됩니다.

```powershell
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 run-local
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 status
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 verify-fast
```

업무 코드 작성법은 **프레임워크 개발자 가이드**, Batch Job은 **배치 개발자 가이드**에서 바로 찾아볼 수 있습니다.

<br>
<hr>
<br>

## 9. Repository 주요 영역

- `cpf-core/` - Topology와 무관한 핵심 계약
- `cpf-starters/` - Public Profile/Starter, Capability, Provider, AutoConfiguration
- `cpf-admin/` - 플랫폼 운영·관리
- `cpf-biz-admin/` - 업무관리 기능
- `cpf-batch/` - Batch Scheduler·Control Plane·Worker·Agent·Center-Cut Runtime
- `cpf-gateway/` - Spring Cloud Gateway 기반 선택형 외부 진입 Runtime
- `cpf-tools/` - Generator·DB Lifecycle·Build·Verification
- `cpf-education/` - 실제 사용 패턴과 학습·검증 예제
- `cpf-member/`, `cpf-external/` - Generator로 생성한 업무 Domain 검증 영역

<br>

**처음 Source를 확인한다면** `gradle/cpf-stack.properties` → `cpf-tools/generator/contracts/cpf-starter-catalog.json` → 사용하려는 Public Starter → `cpf-member`/`cpf-external` Consumer 순서가 가장 빠릅니다. Internal Module의 존재만 보고 일반 업무 개발 API로 판단하지 않는 것이 중요합니다.

<br>
<hr>
<br>

## 10. 공식 문서

| 순서 | 공식 문서 | 사용 목적 |
|:---:|---|---|
| **02** | [프레임워크 개발자 가이드](cpf-docs/guides/02_프레임워크_개발자_가이드.pdf) | Online/API/DB/Transaction/Domain Call/공통 기능 개발 |
| **03** | [배치 개발자 가이드](cpf-docs/guides/03_배치_개발자_가이드.pdf) | Job/Step/Chunk/Partition/Worker/Scheduler 개발 |
| **04** | [운영자 매뉴얼](cpf-docs/guides/04_운영자_매뉴얼.pdf) | ADM·BZA·Runtime·Config·Gateway를 포함한 일반 운영 |
| **05** | [배치 운영 가이드](cpf-docs/guides/05_배치_운영_가이드.pdf) | Job 실행·중지·Restart·Reprocess·Reconcile·Worker 운영 |
| **06** | [Gateway 개발·사용 가이드](cpf-docs/guides/06_Gateway_개발_사용_가이드.pdf) | Gateway Route·Policy·Security·Resilience 개발과 적용 |
| **07** | [Specification / 기술 명세](cpf-docs/guides/07_Specification_기술_명세.pdf) | Public API·SPI·Annotation·Config·State·DB·기술 계약 확인 |

<br>

공식 사용자 문서는 위 **7종(README 포함)**을 기준으로 합니다. QA·Requirement·Evidence·Governance 자료는 내부 관리 자료이며 사용자 Navigation에는 포함하지 않습니다.

<br>
<hr>
<br>

## License

This project is provided under the **Community & Evaluation License**.

학습, 테스트, 평가 및 비상업적 이용은 자유롭게 허용합니다.
기업의 상용 또는 업무 목적 사용은 별도 협의가 필요합니다.
