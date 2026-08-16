<div align="center">

<img src="cpf-docs/assets/readme/cpf-readme-hero.png" alt="Core Platform Framework" width="940" />

# Core Platform Framework

**업무 애플리케이션의 개발·실행·연계·배치·운영을 하나의 구조로 연결하는 Business Platform Framework**

Online API부터 분산 연계, Batch Runtime, Gateway, 운영·관제까지 필요한 기능을 선택해 구성하고, 업무 코드는 Generator로 생성한 업무 Domain에서 개발합니다.

</div>

<br>
<hr>
<br>

## CPF를 처음 보는 경우

- **CPF가 무엇인가** — CPF(Core Platform Framework)는 **Spring Boot 기반 업무 시스템에서 개발·실행·연계·배치·운영을 같은 기준으로 구성하기 위한 Business Platform Framework**입니다. 업무 코드는 CPF 내부가 아니라 업무 Domain에 두고, CPF가 제공하는 Public Starter와 Public API를 필요한 범위만 선택해 사용합니다.
- **어떤 문제를 해결하는가** — 업무마다 반복해서 만드는 Context, Transaction, Security, Logging/Audit, Domain 호출, 외부 연계, Cache, Messaging, Batch, 운영 기능을 공통 계약과 실행 구조로 표준화합니다. 배포 구조가 달라져도 업무 코드가 인프라 세부 구현에 과도하게 묶이지 않도록 하는 것이 핵심입니다.
- **Spring Boot와 어떤 관계인가** — CPF는 Spring Boot를 대체하지 않습니다. **Spring Boot가 애플리케이션 실행 기반이라면 CPF는 그 위에 업무 개발 표준, Starter 조합, Public API, Generator, Runtime·운영 기능을 추가하는 계층**입니다. 일반 Java/Spring 기능은 그대로 활용하되 CPF가 표준을 제공하는 영역은 CPF Public API를 우선 사용합니다.
- **개발자가 실제 쓰는 Golden Path** — 일반 업무는 `@CpfController → @CpfService + @CpfTx → @CpfRepository` 흐름을 기본으로 하고, 같은 Application 내부는 `service.method()`, CPF Domain 간 호출은 `domainClient.execute()`, 외부 연계는 `@CpfClient + @CpfTimeout + @CpfRetry`가 적용된 Typed Client를 사용합니다. Code·Message·Parameter·Calendar, Cache, Messaging, 권한·감사 등도 준비된 공통 API를 선택해 사용합니다.
- **어떤 규모의 시스템에 적합한가** — 단일 Runtime의 일반 Web/API부터 여러 업무 Domain·다중 인스턴스·Gateway·Messaging·Batch가 필요한 구성까지 단계적으로 확장할 수 있습니다. 작은 시스템은 Web/Data/Common 중심으로 시작하고, 필요한 경우에만 Security, Cache, Messaging, Gateway, Batch와 운영 기능을 추가합니다.
- **5분 안에 실행해 보는 방법** — 준비된 개발 환경에서는 Repository Root에서 `pwsh .\cpf-tools\build\tools\cpf-dev.ps1 run-local`로 로컬 Runtime을 실행하고, `status`로 상태를 확인한 뒤 `verify-fast`로 빠른 검증을 수행합니다. 자세한 명령은 [최소 시작 절차](#8-최소-시작-절차)에서 확인합니다.

## CPF 한눈에 보기

- **업무 Domain 생성** — 필요할 때 Generator로 업무 Domain을 만들고 그 안에서 업무 코드를 구현합니다.
- **필요 기능 선택** — Web, Data, Transaction, Security, Cache, Messaging, Integration, Gateway, Batch 중 필요한 기능만 조합합니다.
- **유연한 진입 구조** — Direct, L4/Ingress, Gateway, L4/Ingress+Gateway 구성을 환경에 맞게 선택합니다.
- **거래·연계 오케스트레이션** — Local Transaction, Domain Call, Event, 외부 연계와 Batch 실행을 동일한 실행 문맥으로 연결합니다.
- **통합 운영** — ADM, Batch Runtime, Trace·Log·Audit를 통해 실행 상태와 운영 이력을 추적합니다.

<img src="cpf-docs/assets/readme/cpf-quick-overview.png" alt="CPF 한눈에 보기 - 업무 Domain과 공통 기능, Runtime 구조" width="100%" />

<br>
<hr>
<br>

## 1. CPF 전체 구조

CPF는 **외부 요청 진입 → 업무 Domain → 공통 실행 기반 → Batch·운영 기능 → 데이터·외부 연계**를 하나의 구조로 연결합니다. 업무 코드는 Framework 내부가 아니라 Generator로 생성한 업무 Domain에 두고, 필요한 기능만 선택해 조합합니다.

<img src="cpf-docs/assets/readme/cpf-architecture-overview.png" alt="CPF 전체 Architecture" width="100%" />

**CPF는 외부 요청 진입부터 업무 Domain, 공통 실행 기반, Batch와 운영 기능, 데이터·외부 연계까지 업무 시스템의 실행 구조를 일관된 방식으로 연결합니다.** 업무 기능은 Generator로 생성한 업무 Domain에 구현하고 필요한 기능은 Starter와 Runtime을 선택해 조합합니다.

<br>
<hr>
<br>

## 2. CPF 구성 방식

구성은 **기본 제공 → 필요한 기능 선택 → 필요 시 업무 Domain 생성**의 흐름으로 이해하면 됩니다.

<img src="cpf-docs/assets/readme/cpf-composition-model.png" alt="CPF 기본 제공 기능, 선택 기능, 업무 Domain 생성 구성" width="100%" />

**기본 제공**은 공통 실행 계약·도구·운영 기반입니다. 업무와 배포 요구에 따라 **필요한 기능만 선택**해 Starter, Provider, Gateway, Batch Runtime 등을 추가하고, 필요하면 **Generator로 업무 Domain을 생성**해 실제 업무 코드를 작성합니다.

<br>
<hr>
<br>

## 3. 거래·연계 오케스트레이션

CPF의 오케스트레이션은 단순한 HTTP 호출 묶음이 아닙니다. **Local Transaction, Domain Call, 외부 연계, Event, Batch 후속 처리와 결과 상태를 하나의 실행 문맥으로 연결**합니다.

<img src="cpf-docs/assets/readme/cpf-orchestration.png" alt="CPF 거래 연계 오케스트레이션" width="100%" />

같은 Runtime의 Local 호출은 Local Transaction 경계를 사용할 수 있지만, Remote·Messaging·외부 Side Effect는 동일 원자성으로 가정하지 않습니다. 분산 경계에서는 `SUCCESS / BUSINESS_FAILURE / TECHNICAL_FAILURE / UNKNOWN`을 구분하고 Retry·Reconcile·Compensation을 적용합니다.

<br>
<hr>
<br>

## 4. Gateway와 외부 진입 구성

Gateway는 **필수 진입점이 아니라 필요할 때 선택하는 Runtime**입니다. 시스템 규모와 외부 진입 정책에 따라 Direct, L4/Ingress, Gateway, L4/Ingress+Gateway 구성을 선택할 수 있습니다.

<img src="cpf-docs/assets/readme/cpf-gateway-topology.png" alt="L4, Gateway, L4와 Gateway 조합 구성" width="100%" />

Gateway를 선택하면 Route, 인증·인가 경계, 요청 제한, Target 선택, Health, Canary, Runtime 정책과 감사 기능을 외부 진입 계층에 집중할 수 있습니다. **Domain 간 내부 거래는 Gateway를 필수로 경유하지 않으며 Typed Domain Client를 사용합니다.**

<br>
<hr>
<br>

## 5. Batch Runtime

CPF Batch는 Job/Step 작성만 제공하는 구조가 아니라 **실행 계획·분산 처리·Runtime 제어·대량처리·운영 추적**까지 연결합니다.

<img src="cpf-docs/assets/readme/cpf-batch-runtime.png" alt="CPF Batch Runtime" width="100%" />

- **Scheduler** - 실행 시점과 Job 요청을 관리합니다.
- **Worker** - Job/Step/Partition을 실제 처리합니다.
- **Agent** - 승인된 Runtime 작업의 설치·기동·중지·재시작·상태·Rollback·Drain/Resume를 수행합니다.
- **Center-Cut** - 대량 대상 선정, Preview, 분할 실행, 진행 상태, 실패 대상, 대사를 하나의 흐름으로 관리합니다.

상세 Job 개발은 **배치 개발자 가이드**, 실행·중단·재시작·재처리·대사는 **배치 운영 가이드**에서 다룹니다.

<br>
<hr>
<br>

## 6. 기능 선택

CPF는 모든 기술을 한 프로젝트에 강제하지 않습니다. 개발 목적을 먼저 정하고 필요한 기능군과 Provider를 선택합니다.

<img src="cpf-docs/assets/readme/cpf-starter-selection.png" alt="CPF Starter 선택 가이드" width="100%" />

주요 범위는 Web/API, Persistence, Transaction, Cache/Lock, Messaging, Integration, Security, Observability, Batch와 File/Storage입니다. 정확한 Starter·API·Property 계약은 **Specification / 기술 명세**에서 확인합니다.

<br>
<hr>
<br>

## 7. 개발에서 운영까지

CPF는 업무 정의부터 생성·개발·검증·배포·관측·운영까지 하나의 흐름으로 연결합니다.

<img src="cpf-docs/assets/readme/cpf-development-operation-lifecycle.png" alt="개발에서 운영까지의 CPF Lifecycle" width="100%" />

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

기본 셋업 이후 일상 개발은 Root의 개발 도구에서 시작하면 됩니다.

```powershell
pwsh .\cpf-tools\build\tools\cpf-dev.ps1
```

평소에는 **로컬 실행(1) / 전체 빌드(2) / 빠른 검증(4)** 세 가지를 먼저 익히면 됩니다. 업무 코드 작성법은 **프레임워크 개발자 가이드**, Batch Job은 **배치 개발자 가이드**에서 바로 찾아볼 수 있습니다.

<br>
<hr>
<br>

## 9. Repository 주요 영역

- `cpf-core/` - Topology와 무관한 핵심 계약
- `cpf-starters/` - 선택 기능·Public API·Provider·AutoConfiguration
- `cpf-admin/` - 플랫폼 운영·관리
- `cpf-biz-admin/` - 필요 시 사용하는 업무관리 기능
- `cpf-batch/` - Batch Scheduler·Worker·Agent·Center-Cut Runtime
- `cpf-gateway/` - 필요 시 사용하는 외부 진입 Runtime
- `cpf-tools/` - Generator·DB Lifecycle·Build·Verification
- `cpf-education/` - 실제 사용 패턴과 학습·검증 예제
- `cpf-member/`, `cpf-external/` - Generator로 생성한 업무 Domain 검증 영역

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

공식 사용자 문서는 위 **7종(README 포함)**을 기준으로 합니다. QA·Requirement·Evidence·Governance 자료는 내부 관리 자료이며 사용자 Navigation에는 포함하지 않습니다.

<br>
<hr>
<br>

## License

This project is provided under the **Community & Evaluation License**.

학습, 테스트, 평가 및 비상업적 이용은 자유롭게 허용합니다.
기업의 상용 또는 업무 목적 사용은 별도 협의가 필요합니다.
