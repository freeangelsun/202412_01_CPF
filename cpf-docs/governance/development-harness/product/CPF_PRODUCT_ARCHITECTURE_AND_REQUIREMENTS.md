> 이 문서는 과거 독립 개발 정본을 그대로 존치시키는 문서가 아니라 **Development Harness 내부 Product Contract**다.
> 개발·검수·QA는 반드시 상위 `CPF_DEVELOPMENT_HARNESS.md`와 이 계약을 함께 적용하며, 이 문서 단독으로 완료 판정을 내릴 수 없다.

# CPF Development Harness — 제품 Architecture·요구사항 계약

> Product: **Core Platform Framework (CPF)**
> Document Role: **개발·QA·Architecture 최상위 Current Target**
> Revision: **2026-08-20 Current-Only**
> Canonical Requirement Count: **218개**
> History / Legacy Alias: **Current Catalog에 포함하지 않음**

## 1. 문서 목적과 정본 원칙

CPF는 금융권을 포함한 엔터프라이즈 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화할 수 있는 Business Platform Framework다. 이 문서는 현재 Source를 설명하는 문서가 아니라 **Source가 도달해야 하는 최종 제품 Target**을 정의한다.

다음 원칙은 이 문서 전체에 적용한다.

1. **정본을 Source에 맞춰 약화하지 않는다.** Source가 Target과 다르면 Source Gap이다.
2. 같은 의미의 Requirement를 여러 절에 다른 표현으로 반복하지 않는다. 상세 설명은 해당 Owner 절에 한 번만 둔다.
3. 과거 Steering, Amendment, 세션 상태, Checkpoint, 중간 SHA, 이전 Architecture 설명은 Current Target에 남기지 않는다.
4. Public API·SPI·Internal, Consumer, 정상·오류·부분실패·UNKNOWN·복구, DB3, Generator, Frontend, Runtime, Evidence가 연결되지 않으면 구현 완료가 아니다.
5. “Interface/DTO/Sample/Test 파일이 존재한다”는 사실만으로 완료 처리하지 않는다.
6. 기술 선택은 OSS/Spring의 검증된 의미를 우선하며 CPF는 보안·운영·일관성·개발 편의에서 실질 가치가 있을 때만 추상화한다.
7. Optional Capability는 선택하지 않은 Application에 dependency/bean/thread/config/sql/endpoint/background side effect가 없어야 한다.
8. 제품 기능 변경은 Generator, Generated Domain, Sample/EDU, OpenAPI, Frontend Client, Config, DB, Test, Evidence까지 영향도를 함께 닫는다.

## 2. CPF 제품 Architecture와 Ownership

### 2.1 Canonical Owner Map

| Owner | 역할 | 소유해야 하는 것 | 소유하면 안 되는 것 |
|---|---|---|---|
| `cpf-core` | topology-independent 최소 Kernel | transaction/context/error/security 의미, 안정 Public Contract/SPI/Value, 순수 semantics | Spring Runtime, Admin/Batch/Gateway 전용 계약, Optional Provider, Servlet/Actuator/JDBC/JPA/MyBatis 구현, God Utils |
| `cpf-common` | 고객 업무 공통 | 공통 코드/메시지/영업일/템플릿 등 고객 업무 공통 Contract·Service·정책 | 기술 Kernel, Gateway/Batch/Admin Runtime, 특정 고객 Domain master DB 직접 소유 |
| `cpf-admin` | 플랫폼 운영·관리 Control Plane | Runtime/Health/Trace/Log/Config/Incident/Recovery/Deployment/Batch/Gateway/Security 운영 조회·제어 | 고객 업무 Transaction/업무 master data |
| `cpf-backoffice` | Optional Prebuilt Business Domain | MBW 업무관리, 조직/권한/결재/업무 설정 등 고객 업무 관리자 기능 | 다른 Business Domain DB 직접 접근, 플랫폼 Control Plane 기능 |
| `cpf-backoffice-web` | Channel/BFF Reference | Browser session/CSRF, Frontend SPA, Public HTTP Contract 소비 | DB, CPF Internal Java API, Business Domain Java project dependency |
| `cpf-batch` | Batch Runtime | Job/Step/Worker/Scheduler/Center-Cut/Agent/재시작/분산 실행 | 고객 업무 Domain master 기능, Admin UI |
| `cpf-gateway` | Edge/Gateway | 외부 진입, trust normalization, routing, rate/resilience, route lifecycle | 업무 Domain 로직, 타 Domain DB |
| `cpf-starters/*` | Runtime 조립·Capability·Provider | Spring Boot AutoConfiguration, Public Starter/Profile/Provider, owner runtime adapter | 업무 Domain 자체 로직, Kernel 의미의 중복 소유 |
| `cpf-tools/*` | Generator/DB/Build/Verification/Release tooling | deterministic generator, DB renderer/installer, quality gate, public staging, bootstrap | Runtime 업무 기능의 숨은 Owner |
| `cpf-<domain>` | Generated Business Domain | 업무 Feature, operation, business service/repository/client/model | CPF Internal Starter, 다른 Domain DB 직접 접근, vendor DB source folder |

### 2.2 Dependency Direction

기본 방향은 다음과 같다.

```text
Generated Business Domain / cpf-backoffice
        ↓ Public Starter / Public API / Public Client
cpf-common (업무 공통을 사용할 때)
        ↓
Capability / Provider Runtime (cpf-starters/*)
        ↓
cpf-core Kernel Contract
```

`cpf-admin`, `cpf-batch`, `cpf-gateway`는 각각 자신의 Owner 기능을 소유하면서 필요한 `cpf-core` 범용 계약과 Public Capability를 소비한다. 어떤 Module도 다른 Owner의 `internal` package를 직접 참조하지 않는다.

금지:

- `cpf-core -> cpf-starters/*`, `cpf-admin`, `cpf-batch`, `cpf-gateway`, Generated Domain 의존
- Generated Domain -> Internal Leaf/Provider 구현 직접 의존
- Backoffice -> Member/Account 등 타 Domain Repository/DB 직접 접근
- 동일 JVM인데 자기 자신/다른 Domain을 HTTP로 우회 호출하는 self-HTTP
- Owner 전용 DTO/Port/API를 “interface라서” `cpf-core`에 적치
- 업무 공통을 `cpf-core` Utility나 Starter 내부 구현으로 숨김

### 2.3 Public API / SPI / Internal

- **Public API**: 고객 개발자가 Application 코드에서 직접 사용해도 호환성을 보장하는 계약이다.
- **SPI**: 고객/기관/Provider가 구현할 확장 계약이며 lifecycle, version compatibility, failure semantics를 가진다.
- **Internal**: Framework 내부 구현이다. Public BOM, JavaDoc 사용 가이드, Generated Domain dependency에 노출하지 않는다.
- Public Artifact에서 Internal transitive dependency가 필요한 경우 Runtime 내부 구성으로는 허용하지만 고객 Compile Surface에 Internal type을 노출하지 않는다.
- Native Spring/OSS API escape hatch를 제공한다. CPF Wrapper 사용이 OSS 직접 사용보다 불편하거나 기능을 제한하면 Wrapper 설계를 재검토한다.

## 3. 지원 Topology와 Domain Invocation

CPF는 동일 업무 계약으로 다음 배치를 지원한다.

1. Modular Monolith / Same JVM
2. 분리 WAS
3. MSA / 독립 Process
4. 다중 Instance / Multi-WAS

업무 Source가 배치 방식에 따라 `if local`, `if remote`로 분기하지 않는다. 공식 Domain Invocation 계층이 route를 결정한다.

### 3.1 Same JVM

- Logical transaction context는 `CpfContext` 계열 공식 Public API로 전달한다.
- 자기 Domain/다른 Domain을 HTTP로 재진입하지 않는다.
- Security, Operation Policy, timeout/deadline, idempotency, trace 의미는 Remote와 동일하다.

### 3.2 Remote

- Service/Domain Registry와 Routing이 endpoint를 선택한다.
- Canonical System6를 HTTP/메시지 경계에 serialize하고 수신측이 검증한다.
- timeout, retry, circuit breaker, deadline budget, UNKNOWN/reconcile을 공식 호출 계층에서 처리한다.
- endpoint/zone/version/weight/health/maintenance/draining/lease/TTL을 Registry Metadata와 연결한다.

### 3.3 Topology Parity 완료조건

Local과 Remote는 최소 다음이 동등해야 한다.

- operationId와 permission/policy
- Canonical System6
- 표준 성공/오류/UNKNOWN 결과
- idempotency와 retry semantics
- trace/log/audit
- deadline/timeout
- failure injection과 recovery

## 4. Canonical Transaction Context — System6

### 4.1 Canonical 6개

업무 Domain Online Transaction에는 Controller 실행 전에 다음 논리 Context가 정확히 존재해야 한다.

| Header / Context | 의미 | 변경 규칙 |
|---|---|---|
| `X-Transaction-Id` | 전체 거래 ID | 최초 생성 후 불변 |
| `X-Original-System-Code` | 해당 Transaction을 최초 기동한 신뢰 System | 최초 확정 후 불변 |
| `X-System-Code` | 현재 요청을 실제 처리할 System | Hop마다 Target System으로 Framework가 확정 |
| `X-Caller-System-Code` | 바로 직전 호출 System | Hop마다 Framework가 확정 |
| `X-Target-System-Code` | 현재 호출 대상 System | Hop마다 Framework가 확정 |
| `X-Target-Operation-Id` | 대상 Canonical Operation ID | 호출할 Operation마다 Framework가 확정 |

System과 Channel은 별도 개념이다. `originalChannel/currentChannel/callerChannel/targetChannel`을 System Header의 alias로 쓰거나 `MBR -> MEMBER` 같은 mapping을 만들지 않는다.

### 4.2 신뢰 경계와 생성

- Browser/Untrusted Client는 Protected CPF Header를 작성하지 않는다. 보내더라도 제거/무시하고 trusted identity/route metadata로 재구성한다.
- Protected Header를 생성하는 Trusted BFF/Gateway/Channel Application은 Registry/Runtime Configuration에서 **등록된 logical SystemCode**를 가져야 한다. Channel 이름을 SystemCode로 문자열 변환하거나 Header 값만으로 System identity를 신뢰하지 않는다.
- 최초 Trusted Entry는 authenticated logical caller system identity와 target route를 기준으로 Transaction을 기동한다. Browser가 직접 시작한 사용자 요청이라도 Browser 자체를 System으로 등록하지 않는다. BFF가 소속된 logical System이 거래를 최초 기동하면 그 System이 `X-Original-System-Code`가 된다. 외부 Partner System이 인증된 호출 주체이면 해당 Partner System identity를 사용한다.
- Remote outbound는 Framework가 **6개 전체**를 대상 Hop 기준으로 구성해 전송한다.
- Receiver는 `X-System-Code == 자기 SystemCode`, `X-Target-System-Code == 자기 SystemCode`, `X-Target-Operation-Id == 실제 Handler operationId`를 Controller invocation 전에 검증한다.
- 필수값 누락, 형식 오류, Original 변경, Target 불일치, Handler operation mismatch가 있으면 업무 Controller를 실행하지 않는다.

### 4.3 Hop 예시

A가 B의 `MBR-MEMBER-GET`을 호출할 때:

```text
TransactionId      = 기존 값 유지
OriginalSystemCode = 최초 기동 System 유지
SystemCode         = B
CallerSystemCode   = A
TargetSystemCode   = B
TargetOperationId  = MBR-MEMBER-GET
```

B가 C를 다시 호출하면 Transaction/Original은 유지하고 System/Caller/Target/Operation만 새 Hop에 맞게 갱신한다.

Retry는 동일 Hop의 retry attempt이므로 Canonical 6을 임의 재생성하지 않는다. attempt/segment/execution 정보는 별도 Context로 관리한다.

### 4.4 적용 경계

Canonical System6 업무 Transaction 강제 대상:

- Generated Business Domain Online Controller
- `cpf-backoffice`의 실제 업무 Transaction
- 공식 Domain Invocation으로 연결된 업무 호출

강제 제외:

- `cpf-admin` 플랫폼 관리 API
- `cpf-gateway` 운영/관리 API
- `cpf-batch` Control Plane의 Runtime·Job Pack·Orchestration 관리 API
- 일반 Health/Actuator endpoint

이들 관리 API도 일반 Security/Validation/Trace/Audit/Error Handling은 사용하지만 업무 Operation Policy와 System6를 억지로 적용하지 않는다.

## 5. Operation Identity / Registry / Policy

### 5.1 단일 Operation ID

다음 값은 하나의 안정 ID다.

```text
@CpfOnlineTransaction.operationId
= OpenAPI operationId
= X-Target-Operation-Id
= Domain Client target operation
= Operation Registry key
= ADM 거래관리 key
= Log/Trace operationId
```

업무 개발자가 Annotation에 입력하는 필수 Metadata는 기본적으로:

```java
@CpfOnlineTransaction(
    operationId = "MBR-MEMBER-GET",
    name = "회원 조회",
    description = "회원 식별자로 회원을 조회한다"
)
```

`enabled`, caller allowlist, 운영 override 같은 Runtime Policy를 Source Annotation에 박지 않는다.

`operationId`는 **안정적인 실행 정의/Handler 계약 ID**이고 `executionId`는 개별 실행 건 ID다. 두 값을 합치거나 매 실행마다 operationId를 새로 생성하지 않는다. Retry/Async/Batch 실행 식별자는 execution/attempt/segment 계층에서 관리하고 Canonical Operation Registry key는 안정적으로 유지한다.

### 5.2 Ownership 분리

**Source/Framework Catalog 소유:**

- operationId/name/description
- system/domain/application
- handler class/method
- HTTP method/path
- OpenAPI mapping
- discovery state
- deployment/build metadata

**ADM Policy 소유:**

- enabled
- allowedCallerSystems
- 별도 Channel Policy가 필요한 경우 allowedChannels/ingress condition
- override/version/effective time/reason/approval/audit

YAML은 신규 Operation 최초 등록 시 Policy Seed만 제공한다. Registry에 이미 존재하는 운영 Policy를 Source 재배포가 덮어쓰지 않는다.

### 5.3 `ALL` 의미

`allowedCallerSystems = ALL`은 평가 시점에 **등록되고 ACTIVE이며 신뢰된 Caller System 전체**를 의미한다.

다음은 `ALL`이어도 허용하지 않는다.

- 미등록 System
- disabled/inactive System
- spoofed identity
- target/operation mismatch
- 선행 authentication/authorization 실패

Channel의 `ALL`이 필요한 경우 System Policy와 별도 필드/정책으로 관리한다.

### 5.4 Discovery Lifecycle

Discovery와 Policy enabled를 분리한다.

권장 상태:

- `DISCOVERED`
- `NOT_DISCOVERED`
- `INACTIVE_CANDIDATE`
- `RETIRED` — 승인된 제거 절차 완료

Source에서 한 번 안 보였다고 자동 삭제/비활성화하지 않는다. Multi-WAS mixed deployment 중 일부 Instance에만 존재하지 않을 수 있으므로 active deployment/instance 전체의 discovery evidence, grace period, 승인된 retirement 절차를 사용한다.

Policy Store 장애 시 유효 LKG와 `maxStale` 범위에서만 평가할 수 있으며 LKG 없음/만료는 fail-close한다.

## 6. Runtime Instance Identity

`instanceId`는 SystemCode와 다른 축이며 Canonical Header에 포함하지 않는다.

결정 순서:

1. `cpf.runtime.instance-id`
2. `CPF_RUNTIME_INSTANCE_ID`
3. 실제 Runtime Hostname

기동 시 한 번 확정하고 Process 생명주기 동안 불변이다.

금지 fallback:

- `local`, `dev`, `test`, `prod`
- `localhost`, `127.0.0.1`, `::1`
- `unknown`, 빈 값
- Domain명/SystemCode
- Framework가 임의 생성한 `MBR01`, `role-local-01`

Hostname 확보가 실패하거나 금지값이면 READY로 올라가지 않는다.

동일 Host에서 동일 System의 Process를 2개 이상 실행할 경우 explicit instanceId가 필수다. Registry에서 active `{systemCode, instanceId}` 중복이 검출되면 READY 금지다.

최소 자동 관측 필드:

```text
systemCode
instanceId
hostName
hostIp
application
buildVersion/buildSha
transactionId
operationId
traceId
status/errorCode
```

File Log, DB Log, Error/Failure, Trace, ADM Timeline, Runtime/Health Registry, Retry/Recovery, Batch/외부연계에서 개발자가 수동 입력하지 않아도 연결되어야 한다.

## 7. Starter / Capability / Provider Architecture

### 7.1 핵심 원칙

- `cpf-core`는 Starter를 모른다.
- `cpf-starter`는 가벼운 기본 진입점이며 DB/Broker/Object Storage를 무조건 켜지 않는다.
- `cpf-starter-common`은 `cpf-common` 업무 공통 기능을 사용하는 Public Entry다.
- Public Profile은 대표 use-case 조합이며 한 Deployable에 **exactly-one Top-level Profile**을 기본으로 한다.
- Provider는 명시 선택한다. 충돌하면 fail-fast한다.
- Internal shared leaf는 Public BOM에서 개발자 직접 선택 대상으로 노출하지 않는다.

### 7.2 Public Artifact Naming

기본 예:

```text
cpf-starter
cpf-starter-common
cpf-starter-web-api
cpf-starter-secure-api
cpf-starter-bff
cpf-starter-event
cpf-starter-batch
cpf-starter-data-jdbc
cpf-starter-data-mybatis
cpf-starter-data-jpa
cpf-starter-cache-caffeine
cpf-starter-cache-redis
cpf-starter-cache-valkey
cpf-starter-lock-valkey
cpf-starter-session-jdbc
cpf-starter-session-valkey
cpf-starter-messaging-kafka
cpf-starter-messaging-rabbitmq
cpf-starter-messaging-jms
cpf-starter-messaging-ibm-mq
cpf-starter-object-storage-s3
cpf-starter-graphql
cpf-starter-realtime
cpf-starter-oidc
```

실제 Catalog에 없는 Artifact 이름을 문서만 추가하면 완료가 아니다. Catalog/BOM/settings/publication/generator/profile/consumer/test가 하나의 Canonical Catalog를 따라야 한다.

### 7.3 Core Admission

어떤 type을 `cpf-core`에 넣으려면 모두 충족해야 한다.

1. 대부분의 CPF 기능에 공통으로 필요
2. 특정 Admin/Batch/Gateway/File/AI/Provider 전용이 아님
3. Optional Capability가 없어도 필요
4. Runtime/Topology/Provider와 독립
5. 기술 교체 후에도 의미가 유지
6. CPF 고유 Contract/Semantics/Value임

단순 JDK/Spring Wrapper, 편의 Utility, Provider-neutral이라는 이유만으로 Core에 넣지 않는다.

## 8. `cpf-common` — 고객 업무 공통 Owner

`cpf-common`은 기술 공통이 아니라 **고객 업무 공통**을 소유한다. `cpf-starter-common`은 이를 Application에 조립하는 Runtime Entry다.

대표 기능:

- 공통 코드/참조 데이터
- 업무 메시지/다국어 메시지
- 영업일/기관 Calendar
- 공통 Template
- 고객 공통 Validation/Masking/Audit 정책 확장

경계:

- Spring AutoConfiguration/Provider wiring은 Starter가 소유한다.
- `cpf-core`에 업무 공통을 넣지 않는다.
- 고객사 고유 Domain master는 `cpf-common`이 소유하지 않는다.
- Business Domain이 Common 기능을 사용할 때 Public API/Starter를 통해 소비한다.
- Stateful Common 데이터는 `CMN_*` 등 단일 Owner schema로 관리하고 DB3 migration/install/rollback을 제공한다. 물리 DB 배치는 환경 정책으로 분리 가능하되 Owner/Schema/권한을 잃지 않는다.

## 9. Generated Business Domain Architecture

### 9.1 Golden Path / Canonical Directory·Java Package IA

Generated Business Domain의 물리 Project, Runtime Module, Java Namespace, Business Feature, Technical Role은 서로 다른 의미이며 다음처럼 분리한다.

```text
cpf-<domain>/                         # Domain Project / Gradle Project boundary
  build.gradle                       # Domain aggregate build contract
  settings.gradle                    # Developer-facing module/build registration
  gradle.properties                  # Developer Domain contract (`cpf.domain.*`)
  online/                            # Online Runtime Gradle Module
    src/main/java/
      <domain-package>/              # Java Namespace root
        <Domain>OnlineApplication.java
        base/                        # Generator-owned bootstrap/common contract만 허용
        <business-feature>/          # 업무 Feature
          controller/
          service/
          repository/                # 실제 persistence 선택 시
          client/                    # 실제 dependency/integration 선택 시
          dto/
          model/
          operation/
    src/test/java/<domain-package>/<business-feature>/
  batch/                             # modules.batch=true일 때만
    src/main/java/<domain-package>/
      <Domain>BatchApplication.java
      <business-feature>/
        job/
        step/                        # 실제 필요할 때
  domain/                            # online+batch 등 2개 이상 Runtime이 실제 공유할 때만
```

핵심은 **Domain Project -> Runtime Module -> Java Base Package -> Business Feature -> Technical Role**이다.

- `cpf-<domain>/online`과 `cpf-<domain>/batch`는 물리 Runtime Module이며 제거하지 않는다.
- `src/main/java/<domain-package>`는 Java Base Package이며 제거하지 않는다.
- Runtime Module 이름 `online`/`batch`를 Java package 안에 다시 반복하지 않는다.
- Domain 이름을 Business Feature 이름으로 자동 재사용하지 않는다.
- `cpf.domain.businessFeatures`는 root `gradle.properties`의 명시적 업무 Feature 목록이며 Domain Identity/Capability와 분리한다.
- Business Feature가 아직 지정되지 않은 최초 scaffold는 reserved `sample`을 사용한다. 실제 Feature가 지정되면 해당 Feature를 사용한다.
- `base/`에는 Generator-owned bootstrap, 공통 infrastructure adapter, 공통 추상 contract만 둘 수 있고 실제 업무 Controller/Service/Repository를 몰아넣지 않는다.
- 모든 Feature에 모든 Technical Role을 억지로 만들지 않는다. 선택 Capability/Dependency가 요구하는 Role만 생성한다.

최종 Generated Source에서 다음 구조와 package는 **0건**이어야 한다.

```text
<domain-package>/online/<domain>/...
<domain-package>/<domain>/...
<domain>.online.<domain>.*
<domain>.<domain>.*
```

예를 들어 MEMBER는 `member.customer.controller`, `member.profile.service` 형태가 정상이며 `member.online.member.controller`, `member.member.controller`는 금지한다. EXTERNAL도 `external.integration.client`가 정상이며 `external.online.external.*`, `external.external.*`는 금지한다.

Generator가 이 구조의 Root Owner다. Existing Domain Source를 먼저 수동 이동해 맞추지 않는다. 변경 순서는 **Generator Model/Package·Directory Policy -> Renderer/Template -> Fresh Scratch Generate -> Existing Domain Clean Regenerate -> Diff -> Compile/Test/Runtime -> Legacy Reference 0 -> User-owned 보호 -> Exact Delete Manifest -> Fresh Workspace**다.

Fresh Scratch Acceptance는 최소 다음을 포함한다.

1. Online=true, Batch=false, Domain dependency 없음
2. Online=true, Batch=false, 실제 Domain dependency/selected operation 있음
3. Online=true, Batch=true
4. Business Feature 2개 이상
5. External Client 선택

`create -> setup -> sync -> diff -> regenerate -> diff -> regenerate -> diff` 결과는 idempotent해야 하며 최종 `missing=[]`, `changed=[]`, `staleGeneratedFiles=[]`, `extraUserFiles=[]`, `clean=true`여야 한다. Existing `cpf-member`와 `cpf-external`도 같은 Generator 규칙으로 clean regenerate하고 Fresh Scratch와 동일한 IA rule을 따라야 한다.

Generated 파일 삭제는 ownership을 `GENERATED_OWNED / USER_OWNED / UNKNOWN`으로 분리하며 `USER_OWNED`와 `UNKNOWN`은 자동 삭제하지 않는다. Legacy generated path는 새 구조 compile/runtime, reference 0, replacement 확인 후 Exact Delete Manifest로만 관리한다.

기본 생성 금지:

```text
online/controller
online/service
online/repository
online/domaincall
domain/audit
domain/mapper
domain/policy
vendor/
db/oracle
db/postgresql
db/mariadb
빈 capability package
```

### 9.2 Base / Annotation

표준 확장 계층은:

```text
CpfBase -> DomainBase -> Business Implementation
```

빈 Base를 만들거나 4단 이상 상속을 기본 패턴으로 만들지 않는다. DTO/Entity/Repository는 기술 제약에 따라 composition/interface/meta-annotation을 사용할 수 있다.

공개 Annotation은 CPF 의미가 있을 때 `Cpf` prefix + Spring/OSS의 익숙한 이름을 사용한다.

대표 Canonical Annotation:

```text
@CpfController
@CpfService
@CpfRepository
@CpfBatchJob
@CpfBatchStep
@CpfMessageListener
@CpfClient
@CpfOnlineTransaction
@CpfRetry
@CpfTimeout
@CpfLogging
@CpfAudit
@CpfPermission
@CpfIdempotent
@CpfPerformance
@CpfApprovalRequired
```

메서드명은 특별한 CPF 의미가 없다면 Spring/OSS 이름을 유지한다.

## 10. Domain Setup / Sync / Ownership

### 10.1 Developer Domain Contract

Generated Customer Domain은 고객 개발자가 실제 업무를 개발·빌드·테스트하는 결과물 영역이다. Root의 source-controlled `gradle.properties`가 Gradle, Generator, Runtime이 함께 읽는 **Developer Domain Contract**이며 `cpf.domain.*` key로 logical identity, module, feature, capability, dependency, integration intent를 선언한다.

최소 의미:

```properties
cpf.domain.contractVersion=1
cpf.domain.name=member
cpf.domain.systemCode=MBR
cpf.domain.packageName=member
cpf.domain.tablePrefix=MBR
cpf.domain.preset=standard-enterprise
cpf.domain.online=true
cpf.domain.batch=true
cpf.domain.businessFeatures=customer,profile
cpf.domain.persistence=mybatis
cpf.domain.httpClient=true
cpf.domain.resilience=true
cpf.domain.cache=none
cpf.domain.messaging=none
cpf.domain.sampleTransaction=false
cpf.domain.generationMode=generated
cpf.domain.dependencies=
cpf.domain.externalClients=
```

Generator 입력 전용 descriptor는 명시적 일회성 입력으로만 사용할 수 있고 Generated Root에 복사·보존하지 않는다. `cpf-domain.yaml`, `cpf-generator.lock.json`, 이름을 바꾼 lock/state/manifest, `.cpf/` 숨김 상태, cache/trace/evidence는 Fresh output과 기존 Generated Root, Open Git, Source ZIP/Release Package에 존재해서는 안 된다. Generator는 Developer Contract와 현재 Template에서 expected output을 deterministic하게 재계산하고 검증 실행 중 필요한 transient state만 `cpf-docs/governance/development-harness/evidence/generated/current/domain-generator/verification/**`가 소유한다.

금지:

- raw password/token/certificate private key
- 실제 production secret
- Framework internal artifact 명칭
- 개발자가 선언하지 않은 business dependency 자동 추론

### 10.2 Environment Binding 분리

host/IP/port/database/service/schema/account/secretRef 등 환경값은 logical definition과 분리한다.

권장 local generated binding:

```text
cpf-<domain>/config/cpf-db-profile.local.yaml
```

local binding은 reconstructable해야 하고 raw secret을 저장하지 않는다. Stage/Prod는 배포 시스템/Secret Manager/KMS/Vault 등 외부 Secret Source를 사용한다.

### 10.3 Setup Family

공식 명령 family는 하나의 engine을 사용한다.

```text
cpf domain create
cpf domain setup
cpf domain sync
cpf domain diff
cpf domain remove
```

`create/setup`은 다음을 한 번의 Definition으로 연결한다.

1. Domain identity/module
2. DB Binding
3. Capability/Preset
4. Domain dependency
5. External client
6. Public Starter resolution
7. Build/workspace registration
8. Generated client/config
9. Bootstrap/runtime discovery 준비

### 10.4 Fail-Closed Validation

다음은 생성/변경을 거부한다.

- duplicate domainName/systemCode/package/port
- 잘못된 SystemCode/TablePrefix
- dependency cycle
- 존재하지 않는 target operation을 강제 dependency로 선언
- `persistence=none`인데 DB-required capability 강제
- DB-required persistence인데 binding 없음
- Oracle/PostgreSQL/MariaDB 외 vendor
- raw secret
- Internal Starter dependency

### 10.5 User-owned Source 보호

파일 Ownership은 최소 다음으로 구분한다.

- **Framework-owned template**: Generator가 안전하게 재생성 가능
- **Generated-owned seed**: 변경 여부/hash를 검증하고 수정됐으면 silent overwrite 금지
- **User-owned business source**: 절대 silent overwrite 금지
- **Environment-owned binding/secret**: Source Definition과 분리

`sync/regenerate`는 사용자 수정이 있는 generated-owned 파일을 발견하면 diff와 해결 방법을 보여주고 fail-closed한다.

DB vendor 변경, package rename, systemCode/tablePrefix 변경은 Risky Change다. dry-run/diff 없이 자동 destructive 전환하지 않는다.

## 11. DB3 / Data Ownership / Binding

공식 Vendor는 **Oracle, PostgreSQL, MariaDB**만이다. MySQL, MSSQL, H2를 제품 증적 DB로 사용하지 않는다.

### 11.1 Logical / Physical 분리

Generated Domain 예:

| 구분 | 예 | Owner |
|---|---|---|
| Logical Domain | member / MBR | root `gradle.properties` Developer Domain Contract |
| Logical DB ID | `mbrDB` | Developer Domain Contract에서 결정적 파생 |
| DB Role | `CUSTOMER_BUSINESS_DB` | Developer Domain Contract |
| Table Prefix | `MBR_` | Developer Domain Contract |
| Vendor | PostgreSQL | Environment Binding |
| Host/Port | 환경값 | Environment Binding |
| Database/Service | 환경값 | Environment Binding |
| Schema | 환경값 | Environment Binding |
| Migration Principal | 별도 계정 | Deployment/Secret |
| Runtime Principal | 최소권한 별도 계정 | Deployment/Secret |

Backoffice는 `MBW` / `mbwDB`를 사용한다.

### 11.2 Domain Source와 Vendor SQL 분리

Generated Domain Java Source root에 vendor directory를 만들지 않는다. DB3 DDL/Migration/Seed/Rollback은 Canonical DB Renderer/Installer가 같은 logical model에서 생성·패키징한다.

Released migration은 immutable이다. 이미 배포된 versioned migration 파일명/내용/checksum을 naming cosmetic 목적으로 수정하지 않는다. 정정은 새 migration으로 한다.

### 11.3 Fresh / Upgrade / Rollback

각 Vendor에 대해 최소:

```text
Fresh install
Seed
Upgrade from supported previous version
Rollback 또는 명시된 forward-fix 정책
Index/FK/constraint parity
Runtime query
Permission separation
Backup/restore
```

을 검증한다.

### 11.4 DB Vendor 변경

Domain의 PostgreSQL -> Oracle 같은 변경은 단순 config flip이 아니다.

- logical Domain identity는 유지 가능
- binding과 generated vendor pack은 변경
- 데이터가 존재하면 명시 migration/provision plan 없이 자동 전환 금지
- setup diff에 schema/data/credential/rollback 영향 표시
- Runtime이 active이면 drain/maintenance/approval 절차 요구


### 11.5 CPF Platform DB Physical Naming

CPF Framework 고유 Platform DB의 물리 Naming은 Canonical 고정값을 사용한다. `CPF_*`, `CMN_*`, `ADM_*`, `BAT_*` prefix와 Platform Schema/Object Naming을 Runtime property로 임의 변경하는 범용 기능을 제공하지 않는다. Runtime System Identity와 Data Ownership을 분리한다. 예를 들어 Center-Cut Runner는 `systemCode=CEC`, `channelCode=CEC`, `runtimeRole=CENTER_CUT_RUNNER`이지만 Batch 원장 Consumer이므로 `BAT_*` 데이터를 사용한다. Generated Customer Domain의 물리 Naming은 해당 Generator/Domain Contract가 소유한다.

### 11.6 DB 변경 Runtime Test 강제

DB 변경 Runtime 검증은 단순 연결/기동 Smoke가 아니다. Oracle/PostgreSQL/MariaDB 각각 verifier-owned 격리 DB/User/Schema를 준비하고 기존 검증 객체를 제거한 Fresh 상태에서 Initializer 전체 실행 → Seed → 대상 실제 거래 → DB 결과/History/Trace/Constraint/Index/Query 확인 → Supported Previous Version Upgrade → Rollback/Recovery → Reapply → 동일 거래 재실행/멱등성 → 영향도 회귀 → Cleanup까지 수행한다. `SKIP`, `NOT_EXECUTED`, `UNVERIFIED`, 일부 Vendor만 PASS인 상태는 전체 PASS가 아니다.

### 11.7 Runtime Identity / Batch Executor Type

일반 Batch Runtime의 canonical System/Channel Code는 `BAT/BAT`다. Center-Cut Runner는 `CEC/CEC/CENTER_CUT_RUNNER` 독립 Runtime Instance로 Registry에 등록·Heartbeat하며, Center-Cut 기능명 `CENTER_CUT`과 Runtime Role을 혼동하지 않는다. Batch Job Definition은 실제 Consumer가 존재하는 실행유형만 등록할 수 있어야 하며 최소 `SPRING_BATCH(Java)`, `APPROVED_SHELL`, `FILE_WATCH`, `FILE_PROCESS`, `FILE_TRANSFER`, `CENTER_CUT`, `SERVICE_CALL`, `MESSAGE_TRIGGER`, `PROTOCOL_ADAPTER`를 실제 Owner/Validation과 연결한다. ADM 등록 UI는 실행유형 Select + 유형별 필수 입력/사전검증을 제공하고, FILE_WATCH는 승인된 `PATH_ALIAS`와 상대경로/안정화 조건을 사용하며 CENTER_CUT은 활성 Center-Cut Job을 선택한다. enum/UI만 있고 Runtime Consumer가 없는 실행유형은 False Green으로 실패 처리한다.

## 12. Backoffice — MBW Business Domain

`cpf-backoffice`는 Optional Prebuilt Business Domain이며 일반 Generated Business Domain과 동일한 Online Transaction 계약을 따른다.

- SystemCode: `MBW`
- Logical DB: `mbwDB`
- Feature-First package
- Canonical System6/Operation/Permission/Audit/Trace 적용
- 다른 Business Domain master DB 직접 접근 금지
- 다른 Domain 기능은 공식 Domain Invocation/Public Contract로 호출
- 플랫폼 운영 Control Plane 기능은 ADM에 둔다

Backoffice가 제공할 수 있는 대표 업무관리 기능:

- 업무 관리자 메뉴/권한
- 조직/직원/직급/직책/assignment
- 순차/병렬/role/조직/N_OF_M 승인
- 위임/대결/회수/재상신/만료
- 고객 업무 설정/조회/다운로드
- 선택형 업무 Sequence customization sample

위험 조치는 permission, 사유, 필요 시 approval/SoD, immutable audit, 결과 추적을 제공한다.

## 13. Backoffice Web — Channel/BFF Reference

`cpf-backoffice-web`은 Browser Frontend + Spring Boot BFF reference다.

의존성 경계:

```text
DB dependency                          = 0
CPF Internal Java dependency           = 0
Business Domain Java project dependency= 0
```

허용/권장:

- Published OpenAPI
- Public protocol schema
- Generated HTTP Client
- 일반 Spring Boot/Web/Security/Jackson/HTTP Client

금지:

- `cpf-core` internal API 직접 import
- Backoffice Repository/Service/Model Java dependency
- DTO 수동 복붙을 공식 Contract로 사용
- Browser가 System6 Protected Header 작성

BFF는 Browser session/cookie/CSRF, server-side credential propagation, outbound public client를 소유한다. Frontend의 Backend API 소비는 Generated Client가 기본이며 handwritten duplicate client를 만들지 않는다.

## 14. Gateway / External Integration / Messaging / File

### 14.1 Gateway

Gateway는 외부 trust boundary, route, version, rate limit, resiliency, admission/drain을 소유한다. 업무 Operation 자체를 소유하지 않는다.

Browser/Partner에서 넘어온 Protected Header는 인증된 identity와 route에 따라 normalize한다. 관리 API는 일반 Spring management contract를 사용하고 업무 `@CpfOnlineTransaction`을 강제하지 않는다.

Gateway 경유와 허용된 Direct Public HTTP는 **endpoint 경로 선택의 차이**일 뿐 보안 우회 수단이 아니다. Direct 경로도 동일한 authN/authZ, Caller System/Channel Policy, Audit, Canonical System6/Operation 검증을 적용한다. Gateway 장애를 이유로 Framework가 자동으로 Direct 경로로 전환하는 fallback은 금지하고, 허용 경로와 전환은 명시적 배포/운영 정책으로 관리한다.

### 14.2 External Integration

REST, fixed-length, TCP, file, webhook 등 외부연계는:

- timeout/deadline
- retryability
- idempotency
- partial send/receive
- UNKNOWN
- reconcile
- credential/certificate
- masking/audit

을 제공한다.

### 14.3 Messaging/Event

Kafka/RabbitMQ/JMS/IBM MQ 등 Provider와 Event Schema Contract를 분리한다. Outbox/Inbox, duplicate/out-of-order, DLQ, replay, schema compatibility, idempotency, transaction boundary를 검증한다.

### 14.4 Fixed-Length

Layout Registry가 Layout ID/version/field/group/encoding/byte length를 소유한다. layout이 없거나 version이 맞지 않으면 offset을 임의 추론하지 않는다. 원문 민감필드는 Log/ADM에 노출하지 않는다.

## 15. Batch / Scheduler / Center-Cut

`cpf-batch`가 Batch Runtime Owner다. Generated Domain의 `batch/`는 업무 Job/Step을 소유하고 Public `cpf-starter-batch`를 소비한다.

지원 Golden Path:

- Tasklet
- Chunk
- LOCAL_PARTITION
- Scheduler
- On-Demand
- Center-Cut
- Agent/Worker
- external call
- restart/reprocess

Kafka 기반 Batch Remote Execution은 제품 범위가 아니다. 일반 Batch·Worker·Scheduler·Center-Cut은 Kafka 없이 동작해야 하며, `remote-kafka` 또는 동등한 Batch 전용 Remote Transport를 다시 추가하지 않는다. Messaging Kafka Provider는 Batch와 분리된 별도 Messaging Capability로만 판단한다.

필수 상용 계약:

- Job/Execution/Step identity
- optimistic fencing/CAS
- duplicate execution 방지
- restart checkpoint
- partition/worker lease
- stale worker 차단
- process kill/reassignment
- transaction boundary
- UNKNOWN/reconcile
- pause/resume/drain
- ADM 조회/제어/audit

## 16. Security / Privacy / Audit

최소 범위:

- authentication / OIDC/JWT/SSO where selected
- authorization / permission / role
- operation caller policy
- session fixation/concurrent session/logout propagation
- secret/cert/KMS/HSM boundary
- approval/SoD/break-glass
- masking/privacy/retention
- tamper-evident audit where required

민감정보는 Log/화면/Evidence/Test 산출물에 원문으로 남기지 않는다. Secret Source와 Secret Reference를 구분한다.

## 17. ADM — Platform Control Plane

ADM은 업무 Backoffice와 분리된 플랫폼 운영 Control Plane이다.

공통 IA:

- 운영 현황
- 로그/추적
- 장애/복구
- 설정/정책
- 감사/변경이력

전용 영역:

- Batch/Center-Cut/Agent
- Gateway/Route
- Security/Session/Permission
- Deployment/Runtime/Health
- Integration/Recovery

모든 검색 화면은 paging/filter/detail/state/error/permission을 제공하고 401/403/404/409/429/500/503을 처리한다. 위험 운영조치는 permission + reason + approval where required + audit + result tracking을 제공한다.

## 18. Frontend / OpenAPI / Generated Client

- Backend OpenAPI `operationId`는 Canonical Operation ID와 동일하다.
- Generated Client가 실제 Frontend Consumer에 연결되어야 한다.
- Generated Client 생성만 하고 handwritten client가 계속 Primary이면 완료가 아니다.
- Backend route/operation 1:1 coverage, auth/error/paging/cursor/file/async contract를 검증한다.
- 외부 Runtime CDN/Font/Script dependency를 제품 필수 경로에 두지 않는다.
- 접근성, responsive, keyboard, focus, form error, loading/empty/error state를 포함한다.

## 19. Developer Experience / Configuration

### 19.1 개발자가 반복 plumbing을 쓰지 않게 한다

개발자는 업무 body와 필요한 business metadata에 집중하고 Framework가 다음을 자동화한다.

- System6 생성/검증/전파
- transaction/trace/instance linkage
- validation/error mapping
- permission/operation policy hook
- idempotency/retry/timeout where declared
- masking/audit/logging
- Local/Remote Domain route

### 19.2 Configuration 우선순위

기본 순서:

```text
Framework safe default
→ Application property
→ profile/environment binding
→ operation policy override
→ 허용된 per-call override
```

Override 가능 범위, 권한, schema, metadata, default, secret 여부, dynamic apply 여부를 Configuration Catalog에 둔다.

### 19.3 한글 JavaDoc / Discoverability

Public API/SPI, 주요 annotation/property에 개발자가 IDE에서 바로 이해할 수 있는 한글 JavaDoc/설명을 제공한다. 문서에는 “이 기능을 만들 때 무엇을 선택하면 되는가”가 한눈에 보여야 하며 불필요한 교과서식 설명을 늘리지 않는다.

## 20. CPF Unified Tooling / Local Bootstrap

### 20.1 Unified CLI Architecture

CPF의 공식 Tooling Interface는 **`cpf` 단 하나**다. Open Git 고객, CPF Framework 개발자, Generator/Initializer/Build/Verification/Publication/Release 사용자가 별도 CLI 제품군을 사용하지 않는다. 차이는 별도 구현체가 아니라 동일 `cpf-cli.jar`의 Public/Internal Capability Projection으로 구분한다.

- Canonical Source Owner는 `cpf-tools/runtime/cli/` exactly-one이다. Runtime 제품 Module(`cpf-core`, `cpf-common`, `cpf-admin`, `cpf-batch`)에 CLI 구현을 두지 않는다.
- 실제 공통 구현은 Java `cpf-cli.jar`가 소유하고 Linux `cpf`, Windows `cpf.cmd`/`cpf.ps1`은 Java 실행과 OS 경로/인코딩 처리만 담당하는 Thin Wrapper다. PowerShell-only/Bash-only 구현을 금지한다.
- PUBLIC Command는 최소 `bootstrap`, `domain-new`, `domain-sync`, `build`, `test`, `run`, `stop`, `reset`, `status`다. 외부 고객과 내부 Framework 개발자가 같은 Command Contract/옵션/Exit Code/Error/UTF-8 Log 계약을 사용한다.
- INTERNAL Namespace는 `cpf dev ...`, `cpf verify ...`, `cpf publish ...`, `cpf release ...`처럼 같은 CLI 안에서 Capability로 제공한다. Open Git Binary Profile에는 INTERNAL Capability를 Projection하지 않으며 help 숨김만으로 처리하지 않고 실행 자체가 fail-closed되어야 한다.
- Generator/Initializer/Gradle/DB/Runtime/Verification/Publication/Release Engine은 기존 Canonical Owner를 유지한다. CLI는 Consumer/Orchestrator이며 Engine Source를 복제하지 않는다.
- Generator는 `cpf domain-new`, `cpf domain-sync`가 Canonical Generator Engine을 호출한다. DB/환경 Initializer는 `cpf bootstrap` 및 필요 시 `cpf db ...` 하위 Contract로 기존 DB Engine을 호출하며 Oracle/PostgreSQL/MariaDB와 `cpfDB/mbwDB/mbrDB/exsDB`만 Current Target으로 허용한다.
- Legacy `cmnDB/admDB/batDB/refDB/bzaDB`는 CLI의 생성·초기화·Migration·Seed·Runtime Query Target이 될 수 없다.
- 내부 Framework Golden Path도 가능한 범위에서 `cpf bootstrap → cpf build → cpf test → cpf domain-new/domain-sync → cpf run/status/stop`을 사용한다. 저수준 `.ps1/.sh/.cmd/.py`/Gradle Task/Java Main은 Engine·Debug·Automation 용도로 유지할 수 있으나 공식 Golden Path를 대체하지 않는다.
- 기존 Entrypoint는 `CANONICAL_ENGINE / INTERNAL_ENGINE / CLI_CONSUMER / THIN_WRAPPER / MIGRATE_TO_CLI / DUPLICATE / DEAD`로 Inventory하고, Consumer 0 + Replacement 확인 뒤에만 Delete Manifest로 제거한다.
- Canonical Command Catalog는 `cpf-tools/runtime/cli/contracts/cpf-command-catalog.json`, Artifact Catalog는 `cpf-tools/release/cpf-final-artifact-catalog.json`, Module/Owner Navigation은 `CPF_CANONICAL_PATH_AND_ROLE_MAP.md`를 정본으로 사용한다. 중복 Catalog를 만들지 않는다.
- CLI 또는 연결 Engine의 변경은 Generator, Initializer, DB3, Build, Runtime, Batch, ADM/Backoffice, OpenAPI/Frontend, Sample/EDU, Publication/Open Git, Verification/Fresh Replay 영향도를 필수 검토한다.
- 완료는 JAR/Wrapper/Command 존재가 아니라 Windows/Linux Fresh Lifecycle, Internal Framework Development Lifecycle, Open Git Fresh Lifecycle, UTF-8/경로/오류/UNKNOWN/복구/멱등성/Source Identity parity까지 실제 PASS한 경우에만 가능하다.

### 20.2 Local Bootstrap

Local Bootstrap은 Full Release QA가 아니라 **신규 개발환경을 실사용 가능하게 만드는 제품 기능**이다.

Windows/Linux thin wrapper는 동일한 shared engine을 호출한다.

Prerequisite:

- Git
- Java 25
- Container Runtime
- Node: Frontend 작업이 필요한 경우만

금지:

- silent OS package 설치
- 관리자 권한 자동 escalation
- PATH 영구 변경
- 사용자 동의 없는 data reset

Golden Path:

```text
prerequisite check
→ selected DB container start
→ actual DB health
→ migration
→ seed
→ capability-required middleware
→ Domain discovery
→ build/test
→ runtime start
→ actual health
```

Local Bootstrap의 기본 개발환경은 선택 DB 하나로 시작할 수 있다. 그러나 **DB 관련 Source/Schema/Seed/Runtime Query/Mapper/Repository/Migration/Initializer/Generator 중 하나라도 변경되는 Work Package는 개발 단계부터 Oracle/PostgreSQL/MariaDB 3사 전체를 같은 변경 단위로 구현·검증한다.** Canonical DB Source → Initializer/Fresh Init → 3 Vendor Render/DDL → Migration → Supported Previous Version Upgrade → Rollback 또는 명시적 Recovery → Seed → Runtime Query/Repository/Mapper → Generator/Generated Domain → 실제 Vendor Runtime 거래 → Fresh-vs-Upgrade Schema Parity → Existing Data Preservation → Evidence가 모두 닫히기 전 완료로 판정하지 않는다. 특정 Vendor만 PASS하거나 SQL 파일 존재/정적검증만으로 DB3 PASS 처리하지 않는다.

`stop`은 data/volume을 삭제하지 않는다. `reset`만 명시적으로 data destructive 동작을 할 수 있다.

Domain 추가/삭제 후 다시 실행하면 각 `cpf-<domain>/gradle.properties`의 `cpf.domain.contractVersion` 계약을 재발견해야 하며 hardcoded Domain list를 사용하지 않는다.

장시간 외부 wait에는 timeout과 진행상황을 출력한다.

## 21. Public Developer Workspace / Binary Distribution

### 21.1 Public Git Workspace

Public staging은 빈 디렉터리에서 default-deny로 생성한다.

포함:

- PUBLIC developer source/template/sample
- public config/script
- public docs
- public workspace metadata

금지:

- Private framework source
- internal provider implementation
- governance/QA/evidence
- secret/credential
- accumulated CPF JAR
- local cache/build output

Public release tooling은 staging/validation/`VERIFIED`까지 수행한다. `git add`/index staging/commit/push는 사용자 최종 검토·승인 이전에 수행하지 않는다. `cpf-release/`는 Private CPF master의 Git 반영 대상이 아니며 Open Git 전달용 local-generated staging으로만 사용한다.

### 21.2 Public Binary Repository

Public Binary Repository는 별도 제품 Deliverable이다.

공개 대상:

- BOM
- Public API/SPI
- Public Starter/Profile/Provider
- Generator/Plugin
- 공개 Runtime artifact

검증:

```text
fresh public clone
+ isolated empty Gradle cache
+ no mavenLocal()
+ no Private repository credential/access
→ resolve/build/test/setup/bootstrap PASS
```

repository URL/version은 중앙 설정으로 관리한다.

### 21.3 Open Git Release Packaging

CPF Open Git은 **Open Source Repository가 아니라 고객 개발·실행용 Current Release Distribution Repository**다. Private CPF Repository가 유일한 Framework 개발 정본이며 Open Git은 동일 Current Source Identity에서 매 Release Fresh 생성한 검증 Projection이다.

#### Release Profile과 공개 경계

- 기본 Profile은 **`binary`**다. 별도 옵션이 없으면 반드시 Binary Distribution을 생성한다.
- `binary`에는 CPF Framework 구현 Source Tree, Framework `sources.jar`, `javadoc.jar`, Internal Module/Source/Artifact, Generator Engine 내부 Source, Development/QA Harness, Governance/Requirement 원장, Evidence/Codex/DevGPT 자료, 내부 Release/CI Tooling Source, Secret/Credential/Key, 개인환경/Debug/Temp/Cache, stale/retired Runtime asset을 포함하지 않는다.
- Binary 공개 대상은 Public Framework/Starter Runtime JAR, Public BOM/POM/Maven metadata, Public API/SPI/Annotation/Configuration/OpenAPI/Error/Security/Header/Batch/DB Vendor Contract, Artifact Catalog, Checksum/SBOM, 실행 Config/Script/문서다.
- Framework Source 제공은 명시적 Optional **`source`** Profile에서만 허용한다. Source Profile도 Repository 전체 복사가 아니라 `open-git-public-source-allowlist.json`의 **Default-Deny Canonical Public Source Allowlist**에 명시 승인된 Public API/SPI/Annotation Source만 `framework-source/`에 Root-relative Projection한다.
- `sources.jar`/`javadoc.jar`는 Profile과 무관하게 Open Git Binary Repository에서 **0건**이어야 한다. Source Profile은 Source JAR가 아니라 Allowlist Source Tree만 사용한다.
- Source Profile에서도 Internal implementation, private security implementation, Generator/Template compiler 내부 구현, Build/Release infrastructure, Development/QA Harness, Evidence, Secret/고객 기밀자산은 금지한다. Denylist-only 방식으로 Source 공개 범위를 결정하지 않는다.

#### 고객 개발 Source Tree

- 고객이 직접 개발·수정·확장해야 하는 영역은 Binary JAR 또는 `sources.jar`로 치환하지 않고 **실제 Source Tree**로 제공한다.
- 최소 포함 대상은 Generated Domain(`cpf-member`, `cpf-external` 및 향후 customer domain), Backoffice(MBW) 고객 업무개발 Backend/Frontend, Sample/EDU, 고객 Config, 고객 업무 SQL/Migration, Generated API Client, Test/Resource/Build Script다.
- Generated Domain은 Fresh Generator로 다시 생성 가능해야 하며 `Generate → Build → Test → Runtime → Rerun Idempotency`를 검증한다. Controller/Service/Repository, 3단 Base/Business 구조, Config/Resource, DB SQL/Migration, Test, 필요한 Frontend/API Consumer와 Sample usage를 포함한다.
- Backoffice는 `mbwDB` Owner의 고객 업무개발 Source Tree를 제공하고 CPF Platform 내부 구현 Source와 혼합하지 않는다.
- Sample/EDU는 실제 실행 가능한 Source Tree로 제공하되 retired `cpf-reference/refDB` 구조를 재생성하거나 Current Runtime Target으로 노출하지 않는다.
- 고객 수정 대상 Frontend는 Source, Generated Client, Build/Test 설정을 제공하고 외부 Runtime CDN/Font/Script에 의존하지 않는다.
- **Framework 내부 Source Leakage=0과 Customer Development Source 누락=0을 동시에 만족**해야 한다. 둘 중 하나라도 위반하면 Release FAIL이다.

#### Platform/ADM/Generator/DB 공개 원칙

- `cpf-core`, `cpf-common`, `cpf-admin`, `cpf-batch` 핵심, Public Starter Runtime, Security/Logging/Audit/Transaction/Context/Gateway/DB Platform Runtime 등 CPF 제품 구현은 기본 Binary 제공 영역이다. 고객에게는 Public Contract와 Binary Artifact를 제공한다.
- ADM은 CPF Platform 운영 제품으로 기본 Binary Profile에서 구현 Java Source를 공개하지 않고 필요한 Runtime Binary, Public/OpenAPI Contract, 운영 문서, 고객 Config만 제공한다. Source 제공 계약이 있으면 Source Profile allowlist에서 별도 승인한다.
- Generator는 고객이 사용할 수 있는 `cpf domain-new`/`cpf domain-sync` 기능과 실행 Binary/계약/문서를 제공하되 Generator Engine/Template Compiler/내부 검증·Release Source는 공개하지 않는다.
- Platform DB의 canonical Physical Runtime DB는 `cpfDB`, 고객 업무 DB는 `mbwDB/mbrDB/exsDB`다. Platform Initializer/Migration은 Binary Resource 또는 공식 Bootstrap 경로로 제공할 수 있고 고객 업무 DB SQL/Migration은 해당 Source Tree에 포함한다.
- `cmnDB/admDB/batDB/refDB/bzaDB`는 Open Git Current Runtime의 active DB/Schema/DataSource/Migration/Seed/Upgrade/Rollback/Query Target으로 **0건**이어야 한다. Released immutable provenance가 Private Repository 내부에 존재해도 Open Git Current Runtime으로 Projection하지 않는다.

#### Unified CLI / Bootstrap Golden Path

- CPF 공식 Tooling Interface는 `cpf` 하나다. Open Git 고객과 CPF 내부 개발자는 같은 Java `cpf-cli.jar`, 같은 Command Contract, 같은 Canonical Generator/Initializer/Build/Runtime Engine을 사용한다.
- PUBLIC 명령은 최소 `cpf bootstrap`, `cpf domain-new <domain>`, `cpf domain-sync [domain]`, `cpf build`, `cpf test`, `cpf run`, `cpf status`, `cpf stop`, `cpf reset`이다.
- INTERNAL Namespace는 같은 CLI의 Capability로 `cpf dev ...`, `cpf verify ...`, `cpf publish ...`, `cpf release ...`를 제공한다. Open Git Binary Profile에는 INTERNAL Capability를 Projection하지 않으며 help 숨김이 아니라 실행 자체가 fail-closed되어야 한다.
- Linux `cpf`, Windows `cpf.cmd`/`cpf.ps1`은 Java CLI 실행과 OS 경로 처리만 하는 Thin Wrapper다. PowerShell-only/Shell-only 기능 구현, Windows Bash 강제, Linux PowerShell 강제를 금지한다.
- `cpf-cli.jar`는 공식 Java 25를 fail-closed 확인하며 Wrapper/JAR의 옵션, Exit Code, Error Contract, UTF-8 로그, Version/Source Identity를 일치시킨다.
- `cpf bootstrap`은 Framework 구현 Source 없이 Binary Profile만으로 Current Binary Artifact 연결, Oracle/PostgreSQL/MariaDB 선택, 필요한 DB/Middleware 준비, Generated Domain 자동발견, Config 준비와 Runtime READY까지 수행한다. 수작업 JAR/Source 복사를 요구하지 않는다.
- 파일 존재만으로 완료하지 않고 Fresh Release에서 `bootstrap → domain-new → domain-sync → build → test → run/READY → status → stop → reset → fresh replay`를 Windows/Linux에서 실제 검증한다. Java/version/Docker/DB/Middleware 미준비, 공백/한글/긴 경로, 부분 실패, retry/re-run/idempotency, UNKNOWN을 포함한다.

#### Current-only Fresh Release

- Private Repository Root의 생성 전용 디렉터리는 `cpf-release/`이며 Private Git 및 Product Source Identity에서 제외한다.
- Release 시작 시 이전 `cpf-release/`를 안전하게 전체 재생성하여 **Current Release 한 본만 유지**한다. 이전 Release Binary/JAR/POM/Generated Source/ZIP/Handover/RERUN Evidence/날짜별 Copy/`_OLD`/`_BACKUP`/V1/V2 history를 누적하지 않는다.
- Binary와 Source Profile은 반드시 **동일 Current `sourceIdentitySha256`**에서 각각 Fresh 생성한다. Binary/BOM/POM/Checksum/SBOM/Generated Domain Identity가 서로 다른 Source에서 만들어지면 FAIL이다.
- Canonical Release Generator는 Public Binary Artifact Allowlist, Customer Development Source Allowlist, Public Source Allowlist, Internal Denylist, Secret/Leakage Denylist를 사용하고 Map 생성 전에 duplicate를 검사한다. Public BOM에 Internal Artifact가 노출되면 FAIL이다.
- Open Git Working Repository는 매 Release마다 지정 Remote에서 fresh clone하고 검증된 Projection으로 동기화한다. 과거 Working Copy를 Current Release 입력으로 재사용하지 않는다.

#### Git Write Boundary

- **Private CPF Repository/master는 Release Source이지만 Git write target이 아니다.** `cpf release open-git`과 그 하위 Tool은 Private CPF Repository에서 `git add`, `git commit`, `git push`, branch/tag/reset/restore/stash/clean 등 Git write/history 변경을 수행하지 않는다. Private Git은 Repository Root, Current Branch, HEAD SHA, Working Tree 상태와 provenance를 읽기 위한 read-only 조회만 허용한다. Working Tree 변경이 존재해도 이를 임의 Reset/Restore/Clean하지 않으며 Current Local Source Identity를 기준으로 Release를 생성한다.
- `cpf-release/`는 **Open Git 전달 전용 local-generated staging root**다. `/cpf-release/`는 Private master의 `.gitignore` 및 Product Source Identity에서 제외하고, Private master Commit/Push 대상에 포함하지 않는다. Release 정의/정책/Template/Tool의 정본을 `cpf-release/`에 두지 않는다.
- Release Tool의 파일 생성/정리는 정확히 `<CPF_PROJECT_ROOT>/cpf-release/` 내부로 한정한다. 보호 경로와 Product Source를 삭제/덮어쓰지 않으며 이전 Release 제거는 Root-relative Delete Inventory/안전성 확인 후 current-only Fresh regeneration으로 수행한다.
- **Release Tool/CLI/DevGPT/Codex는 Open Git fresh clone에서도 사용자 승인 전 `git add`, index staging, commit, push를 수행하지 않는다.** 검증 단계는 `git status --short`, `git diff --check`, manifest/checksum/leakage 등 read-only 검증까지만 수행한다.
- Release 상태는 `GENERATED → VERIFIED → USER_REVIEWED → GIT_COMMITTED → GIT_PUSHED`를 구분한다. Tool이 자동으로 올릴 수 있는 최종 상태는 `VERIFIED`이며 `USER_REVIEWED/GIT_COMMITTED/GIT_PUSHED`는 사용자 행위 없이 자동 전이하지 않는다.
- **Open Git Commit/Push는 필수 Release Gate가 모두 PASS한 뒤 사용자가 `cpf-release/open-git` 결과를 직접 검토하고 Open Git Repository에 수행한다.** CPF 개발 master Commit/Push와 Open Git Commit/Push는 별도 작업이며 `cpf-release/` 결과물을 Private master에 반영하지 않는다.
- `VERIFIED` 결과에는 Repository Root, Current Branch, HEAD SHA, `git status --short`, Release Root/Profile/Source Identity, 파일·Artifact 수, Manifest/Checksum/SBOM, Static/Build/Test/Runtime/Fresh Replay/Leakage 결과, Delete Manifest 여부와 Open Git Working Tree 상태를 출력한다. 모든 필수 Gate가 PASS한 경우에만 사용자용 Open Git Commit/Push 명령을 제시하며 자동 실행하지 않는다.
- Open Git Remote 대상은 Canonical repository policy와 일치해야 하며 잘못된 remote/권한/네트워크/dirty clone은 fail-closed한다.

#### Fresh Release Physical Acceptance

Binary Profile은 다음 전체 흐름을 실제 수행한다.

`Fresh Framework Build → Public Binary Publication → Open Git Projection → Fresh Clone → cpf bootstrap → Generated Domain Fresh Generation/Sync → DB/Middleware 준비 → Build → Test → Runtime/READY → 실제 거래 → Status → Stop → Reset/Cleanup → Fresh Replay`

Source Profile은 Binary 전체에 더해 Public Source Allowlist 정확성, Private/Internal Source Leakage 0, Public Source Build, Binary/Source Source Identity parity를 검증한다.

최종 Leakage/Completeness Hard Gate는 최소 다음을 강제한다.

- Binary Framework implementation Source=0
- Binary/Source Repository `sources.jar`=0, `javadoc.jar`=0
- Internal Source/Artifact/BOM=0
- Generator Engine/Development·QA Harness/Evidence/Internal Release Tool/Secret/Credential/개인경로=0
- stale Release/retired Runtime DB target/generated garbage=0
- unclassified Surface/Artifact=0
- Customer Development Source 누락=0
- Binary/Source/CLI/Generated Domain Source Identity mismatch=0
- Fresh Build/Test/Runtime/Fresh Replay FAIL/SKIP/NOT_EXECUTED/UNKNOWN=0

## 22. EDU Canonical 35

EDU는 **기능 그룹 수** 기준 정확히 35개다.

### 22.1 Online 20

| # | Canonical 목적 | 권장 Physical Group |
|---:|---|---|
| 1 | 기본 CRUD | `basiccrud` |
| 2 | 조회·검색·Paging | `querypaging` |
| 3 | 코드·메시지·파라미터·영업일 | `common` |
| 4 | Validation·표준 오류 | `validation` |
| 5 | 동일 Application 내부 Service 호출 | `internalservice` |
| 6 | CPF Domain 간 호출 | `domaincall` |
| 7 | 외부 REST API 호출 | `externalrest` |
| 8 | 고정길이 전문 외부 호출 | `fixedlength` |
| 9 | Transaction REQUIRED | `transaction-required` |
| 10 | Transaction REQUIRES_NEW | `transaction-requiresnew` |
| 11 | Transaction 경계 + 외부 Side Effect | `externalsideeffect` |
| 12 | On-Demand Batch 호출 | `ondemandbatch` |
| 13 | Center-Cut 대응 Online | `centercut` |
| 14 | Cache | `cache` |
| 15 | Messaging·비동기 | `messaging` |
| 16 | File Upload/Download·Bulk | `file` |
| 17 | Security·Permission·Audit | `securityaudit` |
| 18 | Idempotency·UNKNOWN·Recovery | `recovery` |
| 19 | 동시성·Optimistic Lock | `concurrency` |
| 20 | Callback·Webhook 비동기 결과 | `webhook` |

### 22.2 Batch 15

| # | Canonical 목적 | Physical Group |
|---:|---|---|
| 1 | 일반 Tasklet | `tasklet` |
| 2 | DB Chunk | `chunk` |
| 3 | CSV·고정길이 File | `flatfile` |
| 4 | 대용량 Partition·Parallel | `partition` |
| 5 | Center-Cut Job | `centercut` |
| 6 | Scheduler·영업일 | `scheduler` |
| 7 | Retry·Skip·Restart | `restart` |
| 8 | 분산 Worker·재할당 | `distributedworker` |
| 9 | Shell·Command | `shellcommand` |
| 10 | Multi-Step·조건 Flow | `conditionalflow` |
| 11 | Chunk Transaction 경계 | `chunktransaction` |
| 12 | REQUIRES_NEW 독립 Transaction | `requiresnew` |
| 13 | Step별 Transaction 분리 | `steptransaction` |
| 14 | 외부 시스템 호출 + Transaction·UNKNOWN | `externalcall` |
| 15 | On-Demand Batch·중복실행 방지 | `ondemand` |

ADM/Backoffice/Gateway/Platform Operations는 EDU package를 만들지 않는다. 제품 Owner의 실제 Test/Guide가 검증을 소유한다. Legacy/Micro/Compatibility/Verification-only sample을 병행하지 않는다.

## 23. Build / Release / Supply Chain

- Java 25가 최종 Runtime/Build 기준이다.
- clean clone/clean cache에서 build/test/publication이 가능해야 한다.
- dependency lock, BOM, POM, source/javadoc, reproducible artifact를 제공한다.
- SBOM/license/vulnerability/secret 검사를 수행한다.
- install/upgrade/rollback/mixed-version compatibility를 검증한다.
- 미실행 검증을 PASS로 기록하지 않는다.

로컬 통합 테스트 명령은 진행상황을 콘솔에 실시간 표시하면서 로그 파일에도 저장하고 종료 시 PASS/FAIL, ExitCode, 시작/종료 시각, 로그 경로를 출력해야 한다.

## 24. 공통 완료 기준

모든 Requirement는 적용 가능한 축을 명시하고 필요한 축이 닫히지 않으면 완료가 아니다.

| 축 | 완료 질문 |
|---|---|
| Owner | 기능 Owner가 하나인가? |
| Public Boundary | Public API/SPI/Internal이 분리됐는가? |
| Consumer | 실제 Product Consumer가 있는가? |
| Call Path | 정상 호출이 실제 Runtime까지 연결되는가? |
| Error | 오류/경계/부분실패가 정의됐는가? |
| UNKNOWN | 결과 불명 상태와 reconcile이 있는가? |
| Idempotency | retry/duplicate/concurrency에서 안전한가? |
| Multi-instance | 2+ instance/process kill에서 안전한가? |
| Security | auth/permission/secret/masking/audit이 닫혔는가? |
| DB | schema/query/index/FK/DB3/install/upgrade/rollback이 닫혔는가? |
| Generator | Template/Generated Domain/Sample에 반영됐는가? |
| Frontend/OpenAPI | 필요 시 generated client와 실제 UI consumer가 연결됐는가? |
| Operations | ADM/health/log/trace/recovery가 가능한가? |
| Test | unit/contract/runtime/fault가 실제 실행됐는가? |
| Evidence | exact source identity, command, exit code, report가 있는가? |

### 24.1 12개 영구 QA 관점

모든 개발·검수에서 최소 다음을 확인한다.

1. 정책 오류
2. 정본 충돌
3. 중복 개발
4. 불편한 개발
5. 기능 오류
6. 운영 오류
7. Ownership 오류
8. False Green
9. 실패 경로 누락
10. 보안/감사/마스킹 누락
11. DB/Generator/Vendor 영향 누락
12. 유지보수성/탐색성/과도한 추상화·분할 문제

### 24.2 완료 금지

다음은 완료가 아니다.

- Interface/DTO/Table/Route/화면만 존재
- Consumer 없음
- Sample만 존재
- 미실행 Test를 PASS로 기록
- READY/PLANNED/NOT_EXECUTED를 PASS로 기록
- Local happy path만 검증
- Internal API 우회로만 동작
- 한 Vendor만 구현하고 DB3 완료라고 기록
- Source가 없는 stale Evidence
- verifier가 대상 0건을 PASS

## 25. Requirement / QA / Evidence Governance

- Requirement ID는 Current Catalog의 영구 Key다.
- 새 요구는 기존 ID 중복 여부를 먼저 확인하고 진짜 독립 Scope일 때만 추가한다.
- 개발 상태와 검증 상태를 분리한다.
- QA만 전체 상태/최종 완료를 확정한다.
- 개발 GPT는 구현·자체검수와 자신의 Source/Evidence를 책임진다.
- Codex는 독립 검수·보완을 수행한다.
- Codex 전용 `CODEX_*` 진행문서/원장/Work Package와 `cpf-docs/governance/development-harness/evidence/independent-reviewer/current/**`는 Developer GPT가 읽기 전용으로 취급하며 상태/PASS/CLOSED/Evidence를 대신 수정하지 않는다. Developer GPT가 먼저 보완한 항목도 Codex가 자기 원장에서 최신 Source를 독립 재검수할 수 있도록 보존한다.
- Developer GPT는 별도 실행원장에 `개발완료 / 정적검증완료 / 런타임검증완료 / Codex검증완료`를 독립 상태축으로 기록하고, 신규/수정 영역은 Codex 재검수 대상과 재현조건을 남긴다.
- 모든 수정/영향 Runtime Test는 초기상태 준비 → 정상 → 오류/경계 → 실패/복구 → 재실행/멱등 → Side Effect → DB/Trace/History → Cleanup까지 완전 시나리오로 수행한다. 단순 Smoke/기동만으로 Runtime PASS 처리하지 않는다.
- 과거 SHA Evidence를 현재 PASS로 승계하지 않는다.
- Source가 정본과 다르면 정본을 낮추지 않고 Source Gap을 등록한다.
- `CPF_REQUIREMENT_MASTER.csv`, Scenario/Execution Sequence, Control Register 같은 대규모 실행 Dataset은 **Canonical Product Requirement Registry에서 파생된 관리 자료**이며 상위 정본이 아니다. Canonical 변경 후 QA/중앙 관리 Pipeline으로 재생성·정합화해야 하고, 재생성 전 stale Dataset을 근거로 Final Target을 되돌리지 않는다.

## 26. Current-only 문서 관리

Repository에 같은 역할의 문서를 날짜/세션/REV/FINAL_FINAL/Checkpoint 이름으로 누적하지 않는다.

Current canonical 역할:

1. `cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` — 이 문서, 최상위 Target
2. `cpf-docs/governance/development-harness/standards/CPF_ARCHITECTURE_PATH_AND_ROLE_STANDARD.md` — 경로/Owner Navigation만 제공
3. `cpf-docs/governance/development-harness/standards/CPF_CANONICAL_SURFACE_STANDARD.md` — 공식 문서 역할/진입점만 제공
4. `cpf-docs/governance/development-harness/current/CPF_CURRENT_WORK_REQUEST.md` — 현재 개발 요청
5. `cpf-docs/governance/development-harness/current/REQUIREMENT_STATUS.csv` — 현재 Requirement 상태
6. `cpf-docs/governance/development-harness/current/TEST_AND_EVIDENCE.md` — 현재 Source 실행 Evidence
7. `cpf-docs/governance/development-harness/current/OPEN_ISSUES.md` — 현재 미해결 문제
8. `cpf-docs/governance/development-harness/current/CPF_DEVELOPMENT_HANDOVER.md` — 현재 세션/PC 인수인계

정책이 이 문서에 흡수되면 별도 Starter/Generated Domain/ADR/Steering/Continuity History를 또 다른 정본으로 유지하지 않는다.

## 27. Current Canonical Requirement Catalog

아래 Catalog만 Current Requirement Count에 포함한다. 과거 Alias/History ID는 포함하지 않는다.

| Requirement | Owner | 최소 제품 목표 | 필수 완료 증명 |
|---|---|---|---|
| `ARCH-MISSION` | cpf-core / repository architecture | CPF를 샘플이나 공통 라이브러리가 아닌 금융권 포함 엔터프라이즈 업무시스템의 구축·운영·감사·확장·배포를 책임지는 상용 Business Platform Framework로 완성한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-MSA` | cpf-core / repository architecture | 동일 Public Contract로 Modular Monolith, 동일 JVM Local Call, 분리 WAS Remote Call, 독립 Microservice를 지원하며 topology 변경이 업무 계약을 바꾸지 않게 한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-BOUNDARY` | repository architecture + canonical owner modules | 기술 Kernel, 고객 업무 공통, 플랫폼 운영, 업무 Backoffice, Batch, Gateway, Generated Business Domain의 Owner를 단일화한다. `cpf-core`는 topology-independent Kernel만, `cpf-common`은 고객 업무 공통, `cpf-admin`은 플랫폼 운영, `cpf-backoffice`는 MBW 업무관리 Domain, `cpf-batch`는 Batch Runtime, `cpf-gateway`는 Edge/Gateway를 소유하며 역방향·순환 의존과 타 Domain DB 직접 접근을 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-LAYER` | cpf-core / repository architecture | Public API, 확장 SPI, Internal 구현을 Module·Package·Publication·JavaDoc·ArchUnit로 구분하고 외부 Consumer가 Internal Package를 참조하지 못하게 한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-API` | cpf-core / repository architecture | 고객 개발자가 최소 입력으로 안전하게 사용할 수 있는 발견 가능한 Public API를 제공하고 거대 Utils·의미 없는 Wrapper·선택 Runtime type 노출을 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-SPI` | cpf-core / repository architecture | 고객·Generated Domain·기관 Adapter가 구현할 안정된 SPI와 lifecycle, capability, version compatibility, failure contract를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-CONFIG` | cpf-core / repository architecture | safe default→customer property→profile→operation override→per-call override 순서와 허용범위·권한·버전·감사·rollback을 보장한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-TESTKIT` | cpf-core / repository architecture | Public Contract, Header, 오류, idempotency, Local/Remote parity, failure injection을 외부 Consumer가 재사용할 수 있는 Test Kit로 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-CALL` | cpf-core / repository architecture | 동일 JVM과 분리 WAS 호출에 동일한 Header·권한·timeout budget·오류·추적·idempotency를 적용하고 내부 호출의 Gateway 재경유를 금지한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-REGISTRY` | cpf-core / repository architecture | Service·Endpoint·Instance·capability·version·zone·health·maintenance·draining 상태의 등록, lease, TTL, stale 제거와 조회 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ROUTING` | cpf-core / repository architecture | service/instance/zone/version/weight/maintenance 정책에 따른 routing과 failover를 결정적으로 수행하고 승인된 운영 override와 audit를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-HEALTH` | cpf-core / repository architecture | liveness·readiness·startup·dependency·business readiness를 구분하고 service identity·build SHA·schema version까지 검증 가능한 health 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-HEADER` | cpf-core contract + ingress/runtime adapters | 표준/확장 Header의 이름·형식·신뢰경계·생성자·전파·masking·최대크기·호환성을 정본화하고 spoofing을 차단한다. 업무 Online Transaction의 Canonical System6는 `CPF-SYSTEM6` 요구를 따른다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-CONTEXT` | cpf-core context contract + runtime adapters | transaction, trace, segment, caller system, target system, principal, environment, optional channel, deadline, attempt context를 동기·비동기·Batch 전 구간에 보존한다. System과 Channel을 alias/mapping하지 않는다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-TXID` | cpf-core transaction identity contract + trusted ingress runtime | 34자리 transactionId는 최초 신뢰 거래 기동점에서 한 번 생성되고 전체 Local/Remote/REST/Gateway/Message/Async/Retry/Batch/File/UNKNOWN/Reconcile/Log/ADM Timeline에서 불변으로 승계된다. 비신뢰 Browser/Client가 임의 생성·변조한 값을 신뢰하지 않으며 replay와 spoofing을 차단한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ROLE` | cpf-core / repository architecture | transaction role, direction, source/target, caller/receiver 관계를 표준 Context·Log·Audit에 일관되게 기록한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-ERROR` | cpf-core / repository architecture | 표준 오류 코드·HTTP/Protocol mapping·retryability·unknown-result·field error·operator message를 버전 가능한 계약으로 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-VALID` | cpf-core / repository architecture | 입력·출력·설정·Header·파일·메시지·SQL parameter 검증과 오류 위치, allowlist, 크기·깊이·개수 상한을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-IDEMP` | cpf-core / repository architecture | canonical request hash, scope, TTL, result replay, conflict semantics와 concurrent race를 포함한 idempotency 원장을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-STATE` | cpf-core / repository architecture | 승인·비동기·배치·배포·복구 등 장기 거래의 허용 상태전이, 낙관적 잠금, terminal state, reconciliation을 명시한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOCK` | cpf-core / repository architecture | optimistic/distributed lock, lease, fencing token, owner epoch, expiry, takeover와 stale writer 차단을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-RESILIENCE` | cpf-core / repository architecture | timeout, retry, circuit breaker, bulkhead, rate/backpressure, retry storm 방지와 operation별 정책을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-DEADLINE` | cpf-core / repository architecture | 요청 전체 deadline budget을 하위 호출·DB·Broker·파일·process에 분배하고 초과 시 cancel·cleanup·unknown-result 규칙을 적용한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-SCHED` | cpf-core / repository architecture | 기술 Scheduler의 trigger, cluster claim, misfire, calendar, idempotency, pause/resume, 운영 제어 계약을 정의하고 Batch Scheduler와 Owner를 분리한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-OPSDB` | cpf-core / repository architecture | 운영 DB의 공유/분리 topology, schema ownership, 연결 장애 시 fail-open/fail-closed, backpressure, 복구와 readiness를 정의한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOGDB` | platform-operations/logging + persistence provider; core는 공통 transaction/error/context 의미만 | DB Log의 schema·index·retention·masking·비동기 적재·조회 성능·장애 격리와 ADM projection을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-FILELOG` | platform-operations/observability logging capability; core는 공통 context 의미만 | 환경·Domain·Instance·transactionId·execution 단위로 탐색 가능한 구조화 File Log, rotation, retention, secure permission과 수집 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-LOGFAIL` | platform-operations/observability logging capability | 로그 저장 실패가 업무를 오염시키지 않도록 정책별 fail-open/closed, local spool, 재전송, 중복 제거, 유실 탐지와 alert를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-TRACE` | core trace/context contract + platform-operations/observability provider | transactionId와 trace/span/segment/attempt를 연결하고 sampling, trace boost, baggage allowlist, cardinality·민감정보 통제를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CPF-MASK` | core classification/redaction contract + security/masking capability | PII/Secret/Credential 분류, context-aware masking/redaction, raw 조회 승인, logging/evidence/download 정책과 테스트 corpus를 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-FIXED` | integration/fixed-length contract + fixed-length starter/provider | 고정길이 전문 Layout/Field/Group/encoding/byte length/parser/writer/validator/version/streaming과 secure diagnostic engine을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-FILE` | file capability contracts + archive/attachment/transfer/object-storage providers | Path Alias, bounded streaming, checksum, atomic publish, symlink/path traversal 방지, cleanup, cancellation을 포함한 File/Attachment/Archive 기술 계약을 제공한다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `CORE-MESSAGE` | messaging contract capability | versioned broker envelope, correlation, idempotency key, schema, TTL, producer/environment binding, size limit와 serialization allowlist를 제공한다. Messaging이 Optional Capability이므로 전용 Contract를 `cpf-core`에 적치하지 않는다. | ArchUnit/Build graph, Published API 소비 Test, Local·Remote parity, 오류·동시성·fault Runtime Evidence |
| `ARCH-STARTER` | starter architecture + cpf-tools catalog/build | `cpf-core`를 Spring Boot 없는 최소 Kernel로 유지하고 Public Starter·Profile·Capability·Provider·Internal Leaf의 역할을 단일 Catalog로 관리한다. Deployable은 exactly-one Top-level Profile 또는 명시 Capability 조합을 사용하고 Generated Domain은 Public Artifact만 직접 참조한다. Optional Provider는 미선택 시 dependency/bean/thread/config/sql/endpoint side effect가 0이어야 한다. | non-Boot Core consumer, Starter removal compile, Profile resolution lock, Aggregate POM, BOM/publication, actual Consumer, startup/classpath/fault Evidence + OSS 직접 적용 대비 boilerplate/설정 감소와 misuse fail-fast 검증 |
| `CMN-EXTENSION` | cpf-common + cpf-starter-common | 고객 업무 공통 Header extension, 공통 코드/메시지/캘린더/템플릿, 업무 공통 Validation/Masking/Audit 정책을 `cpf-core`의 기술 Kernel 위에서 확장한다. 고객 업무 공통을 기술 Starter 내부 구현으로 숨기거나 `cpf-core`에 적치하지 않는다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-SAMPLE-DB` | cpf-education/testkit | Production Common DB가 아닌 Reference Fixture의 단일 Golden Sample Table로 CRUD/Search/Paging/Validation/duplicate/optimistic lock/commit/rollback을 3 Vendor에서 검증한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-CODE` | cpf-common | 고객 공통 코드·참조데이터의 group/item/version/유효기간/cache/invalidation/조회·관리·audit 계약을 제공하며 Public 소비는 `cpf-starter-common` 또는 공식 API를 경유한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-MSG` | cpf-common | 다국어·오류·업무 메시지의 code, locale, parameter schema, fallback, cache, version과 관리 계약을 제공하며 Framework reserved error taxonomy와 고객 업무 메시지 Catalog를 분리한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-CALENDAR` | cpf-common | 영업일·휴일·기관 calendar, 기준일 계산, override 승인과 Batch/업무 공통 소비 계약을 제공한다. 단순 시스템 시각/Clock은 Foundation/Testkit 책임과 분리한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `CMN-TEMPLATE` | cpf-common | 알림·문서 Template의 version, variable schema, escaping, preview, channel extension, approval과 audit를 제공하고 기술 Notification Provider와 업무 Template 정책을 분리한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-OWNERSHIP` | cpf-tools DB + owning module | 모든 schema/table/view/index/FK/trigger/seed/query에 단일 Owner와 실제 Consumer를 부여하고 Admin/타 Domain의 직접 갱신을 금지한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-INSTALL` | cpf-tools DB + owning module | Schema/User provision, 최소권한, product table/index/constraint, mandatory seed, verify/smoke를 Vendor-native 정본으로 재현 가능하게 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-FRESH` | cpf-tools DB + owning module | Oracle·PostgreSQL·MariaDB 검증을 CPF Object 0건의 전용 초기 Database/Schema에서 시작하고 Canonical/Generator-first Fresh Install→Upgrade→Rollback→Reapply→Cleanup을 자동화한다. | Vendor별 pre-object-count 0, generated metadata/seed, runtime query, drift, rollback/reapply, different-hash, optional pack, post-cleanup exact-SHA Evidence |
| `DB-MIGRATION` | cpf-tools DB + owning module | 불변 version migration, expand-migrate-contract, checksum, drift fail-closed, restart, data transform와 신규설치 최종상태 parity를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-ROLLBACK` | cpf-tools DB + owning module | rollback/forward recovery 가능성 분류, 데이터 보존·backup checkpoint·승인·재적용·부분 실패 복구를 Vendor별로 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-BACKUP` | cpf-tools DB + owning module | schema/data/config/key metadata의 backup, encryption, retention, restore validation, PITR와 DR 연계 절차를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-MULTI-VENDOR` | cpf-tools DB + owning module | MariaDB·PostgreSQL·Oracle의 type/default/index/FK/paging/locking/error semantics와 install→upgrade→rollback→reapply 동등성을 보장한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-SQL` | cpf-tools DB + owning module | Query ID·Owner·parameter/result contract·sort/filter allowlist·Vendor SQL resource·MyBatis/JDBC 규칙과 Java literal SQL 금지를 적용한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-PERF` | cpf-tools DB + owning module | index/plan/statistics/partition/slow query/capacity/purge 성능 기준과 대표 데이터 규모의 regression gate를 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DB-MULTI` | cpf-tools DB + owning module | multi datasource, read replica, read/write routing, transaction consistency, lag, failover/failback와 tenant/domain isolation을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DATA-LINEAGE` | cpf-tools DB + owning module | 입력 source→처리→저장→외부전달의 dataset/field lineage, quality rule, reconciliation, owner와 audit 연결을 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `DATA-RETENTION` | cpf-tools DB + owning module | retention, purge, archive, legal hold, 개인정보 삭제, backup 예외와 실행 증적을 데이터 유형별로 제공한다. | Owner/Consumer 추적, MariaDB·PostgreSQL·Oracle SQL 또는 DB-less 근거, install·upgrade·rollback·runtime Evidence |
| `GWY-ENTRY` | cpf-gateway | 외부 진입점의 TLS, listener, protocol, client identity, request limit, maintenance와 control/data plane 분리를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-ROUTING` | cpf-gateway | Spring Cloud Gateway 기반 route snapshot, service registry, path/query rewrite, load balancing, version/zone/weight routing과 atomic refresh를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-TRUST` | cpf-gateway | trusted proxy와 client header allowlist, internal header overwrite, forwarded chain, principal/context 생성과 SSRF target allowlist를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `GWY-RESILIENCE` | cpf-gateway | connect/send/response/read 단계별 timeout·retry·failover·circuit breaker·streaming completion·client disconnect·unknown-result ledger를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `API-LIMIT` | cpf-gateway + cpf-core contract | client/channel/API/tenant별 rate limit·quota·burst·abuse detection·distributed counter·429/Retry-After·운영 override를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-INST` | root `cpf-external/` generated domain / customer adapter | `external`(EXS)을 공식 Generated Customer Domain 회귀 인스턴스로 사용하되 Generator에 EXS를 하드코딩하지 않는다. 기관별 Adapter는 Metadata/Feature와 고객 확장 Owner로 생성·확장하며 `cpf-external`은 Generated Project Root 역할로만 사용하고 CPF Product Module/Public Artifact로 등록하지 않는다. | external fresh generation, integration-http/resilience actual consumer, Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-REST` | generated domain / customer adapter | 외부 REST 호출의 auth, timeout, retry, idempotency, schema, mapping, audit, mock/test contract를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-FIXED` | generated domain / customer adapter | 기관별 고정길이 Layout/Mapping/endpoint를 CORE-FIXED Engine 위에 versioned Adapter로 구현한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-SEC` | generated domain / customer adapter | 외부연계 mTLS/OAuth/API key/certificate/secret rotation, endpoint allowlist, payload masking과 non-repudiation을 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-FILE` | generated domain / customer adapter | SFTP/파일명/ack-nack/checksum/claim/transfer/reconciliation/retention을 고객 Adapter가 안전하게 소유한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-UNKNOWN` | generated domain / customer adapter | 외부 요청의 전송 전 실패·전송 후 응답 유실·상대 처리 불명 상태를 분류하고 자동 성공·무조건 재시도를 금지한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EXS-RECON` | generated domain / customer adapter | 상대 조회·callback·file ack·수동 확인을 통한 reconciliation, compensation, reprocess, SLA와 운영 UI를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-CORE` | cpf-core contract + owning business adapter | Provider-neutral destination naming, versioned envelope, key/order, producer/consumer contract와 in-memory test adapter를 제공하며 Provider별 의미 차이를 명시한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-OUTBOX` | cpf-core contract + owning business adapter | 업무 데이터 변경과 Outbox INSERT를 동일 Local Transaction으로 묶고 stable event/message ID, claim/lease/fencing, retry, ordering, publish/confirm 상태, cleanup, broker ACK 유실·process kill 후 중복 발행과 UNKNOWN/Reconcile을 제공한다. Outbox는 일반 로그가 아니라 외부 전달이 완료될 때까지 생명주기를 관리하는 durable delivery state다. | 업무 DB+Outbox 동일 commit/rollback, 실제 Broker, publisher kill/restart, ACK loss, duplicate publish, multi-instance claim, timeout·unknown·reconcile Evidence |
| `EVENT-BROKER` | cpf-core contract + Starter Provider owner | Kafka/JMS/IBM MQ/AMQP의 ACK·transaction·redelivery·consumer concurrency·backpressure·schema·TTL·security·observability와 multi-instance correlation을 공통 계약으로 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `EVENT-MQ` | cpf-core contract + Starter Provider owner | Queue 기반 Messaging의 destination, durable delivery, correlation, idempotency, expiry, priority, transaction, redelivery, DLQ와 운영 조회를 Provider-neutral 계약으로 제공한다. | Kafka/JMS/IBM MQ/RabbitMQ provider contract parity, actual broker, duplicate/ordering/outage/recovery/multi-instance Evidence |
| `EVENT-JMS` | cpf-starter-messaging-jms | Jakarta JMS 3.x ConnectionFactory, producer/consumer, transaction/session, selector, durable subscription, redelivery, exception listener와 readiness를 CPF Event 계약에 연결한다. Local JMS transaction과 XA-capable ConnectionFactory/XAResource 경로를 구분하며 XA/JTA 선택 시 CPF Transaction Strategy와 연결한다. | embedded/mock만이 아닌 실제 JMS provider matrix, local/XA transaction, redelivery/connection-loss/recovery Evidence |
| `EVENT-IBM-MQ` | cpf-starter-messaging-ibm-mq | JMS 공통 Starter 위의 Optional Provider로 IBM MQ Queue Manager·Channel·TLS·CCDT/endpoint·connection recovery·reason-code mapping·운영 상태를 제공한다. CPF 기본 Runtime에 IBM MQ 의존성을 강제하지 않으며 고객이 선택할 때만 로드되고 XA-capable JMS 구성이 필요한 경우 TX-XA-JTA 계약과 연결한다. | optional dependency/bean 0-footprint, IBM MQ compatible runtime, TLS/credential rotation, queue manager outage, reconnect, XA/in-doubt/duplicate/reconcile Evidence |
| `EVENT-AMQP` | cpf-starter-messaging-rabbitmq | RabbitMQ/AMQP exchange·queue·binding·publisher confirm·consumer ack/nack·redelivery·DLX·quorum/connection recovery를 CPF Event 계약에 연결한다. | actual RabbitMQ runtime, confirm/ack/nack/DLX, duplicate/order/outage/recovery Evidence |
| `EXS-TCP` | cpf-starter-integration-tcp + generated/customer adapter | 영속 TCP 연결의 framing·encoding·heartbeat·reconnect·backpressure·correlation·TLS·half-open·전송 후 결과 불명과 기관별 전문 Adapter 연결을 제공한다. | loopback/mock 및 실제 fault proxy, disconnect/half-open/timeout/response-loss/duplicate/reconcile/multi-instance Evidence |
| `EVENT-DLQ` | cpf-core contract + owning business adapter | retry topic, DLT, poison isolation, replay approval, payload masking, idempotent reprocess와 운영 추적을 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `SAGA-CORE` | cpf-core contract + owning business adapter | 장기 업무 흐름의 step, state, version, timeout, idempotency, event/call correlation과 durable orchestration/choreography 계약을 제공한다. 동일 transactionId lineage 아래 STARTED/RUNNING/COMPLETED/FAILED/COMPENSATING/COMPENSATED/UNKNOWN/MANUAL_REVIEW 상태를 구분하고 restart/multi-instance에서도 이어서 복구한다. | A→B→C(/D) 실제 Reference, 부분 성공, process kill, timeout, duplicate, compensation/retry/unknown/reconcile/multi-instance Evidence |
| `SAGA-COMP` | cpf-core contract + owning business adapter | 각 step의 compensation eligibility, reverse order, idempotency, partial compensation와 unknown-result 분리를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `SAGA-MANUAL` | cpf-core contract + owning business adapter | 자동 복구 불가 Saga의 승인된 수동 확정·보상·재실행·audit·operator guidance를 제공한다. | 실제 Gateway/Kafka/외부 Mock·다중 인스턴스, timeout·duplicate·disconnect·unknown·recovery Evidence |
| `TX-STRATEGY` | cpf-core contract + selected runtime owner | 업무 특성에 따라 `LOCAL`, `XA_JTA`, `OUTBOX`, `SAGA`, `TCC`를 명시적으로 선택·조합하는 정책, 안전한 Default, 상호배타/호환 규칙과 선택하지 않은 Runtime 0-footprint를 제공한다. 동일 거래에서 전략을 혼합해도 transactionId lineage와 오류/복구 모델이 유지되어야 한다. | strategy selection API/config, fail-fast conflict test, selected-only dependency/bean/config/SQL, 실제 Consumer와 혼합 전략 E2E Evidence |
| `TX-LOCAL` | cpf-core contract + data runtime owner | 단일 Resource의 local transaction을 가장 단순한 기본 개발경험으로 제공하고 commit/rollback, propagation, isolation, timeout, read-only, exception mapping, transactionId/log 연계를 표준화한다. XA Provider가 없어도 기본 업무가 정상 동작해야 한다. | 실제 Domain Consumer, commit/rollback/timeout/exception Test, 3 DB Vendor Runtime 또는 타당한 DB-less 근거 |
| `TX-XA-JTA` | cpf-core transaction contract + Optional JTA Provider owner | JTA/XA를 Optional 상용 Capability로 제공한다. Tomcat에서는 standalone Transaction Manager Adapter, JTA-capable WAS에서는 managed JTA Adapter를 지원하며 DB+DB, DB+JMS의 XAResource enlistment와 2PC prepare/commit/rollback/heuristic/in-doubt 상태를 제공한다. 특정 TM 구현을 `cpf-core`에 강제하지 않는다. | Tomcat-compatible standalone TM Reference, managed-JTA adapter contract, Oracle/PostgreSQL/MariaDB XADataSource, JMS XAConnectionFactory/XAResource, DB+DB·DB+JMS Consumer/Test/Runtime Evidence |
| `TX-XA-RECOVERY` | JTA Provider owner + operations | prepare 이후 process kill, TM/RM restart, commit 중 장애와 in-doubt transaction을 durable recovery log와 resource recovery scan으로 안전하게 해소하고 duplicate recovery·heuristic outcome을 구분한다. ADM에서 권한·사유·감사와 함께 조회/조치한다. | prepare-kill-restart, TM/RM restart, in-doubt scan, heuristic/manual review, multi-instance/fencing, ADM Timeline/Recovery Evidence |
| `TX-INBOX` | messaging reliability owner + business consumer | At-least-once 전달 환경에서 Inbox/Dedup을 공식 계약으로 제공하고 eventId/messageId, consumer identity, idempotency, concurrency, duplicate/partial processing, process kill/restart와 retention을 관리하여 업무 중복 Side Effect를 방지한다. | Outbox→Broker→Inbox 실제 Consumer, duplicate/redelivery/process-kill/multi-instance Test, dedup state/cleanup/reconcile Evidence |
| `TX-TCC` | cpf-core contract + owning business domain | Hold/Reservation형 업무를 위한 Optional `Try/Confirm/Cancel` 계약을 제공한다. Try/Confirm/Cancel idempotency, empty rollback, hanging, duplicate confirm/cancel, timeout, UNKNOWN, recovery를 지원하되 Framework가 업무 보상 의미를 임의 결정하지 않는다. | 잔액/한도/재고 등 Reference Consumer, Try→Confirm/Cancel, duplicate/empty rollback/hanging/process-kill/reconcile Evidence |
| `TX-E2E` | cpf-core + all integration/runtime owners | Domain Call, 외부 REST/SOAP/TCP/File, DB, JMS/Kafka/RabbitMQ, Batch, Outbox/Inbox, Saga/TCC/XA, Retry/UNKNOWN/Reconcile, Logging/Audit/Trace/ADM을 하나의 transaction lineage로 연결한다. 기능별 단독 PASS로 E2E 완료를 대신하지 않는다. | 동일 Reference Transaction의 Source→Consumer→Call Path→failure/recovery→Log/ADM Timeline, local/remote/multi-instance/process-kill Evidence |
| `TX-DX` | cpf-core + Starter owners | 업무 개발자가 transactionId/log/audit/metrics/retry/idempotency/recovery를 매번 수동 조립하지 않도록 typed API, 안전 Default, 최소 Config, Fail-Fast와 세밀한 Override를 제공하고 고급 사용자는 underlying transaction/provider native 기능에 접근할 수 있게 한다. | 실제 업무 Consumer 코드 비교, boilerplate 감소, configuration misuse negative test, native escape/conformance Evidence |
| `TX-EDU` | cpf-education + generator | Local, XA DB+DB, XA DB+JMS, XA crash recovery, Outbox/Inbox, Saga compensation, TCC, 외부 timeout/UNKNOWN/Reconcile, Domain A→B→C, Batch 연계를 실행 가능한 Reference로 제공하고 동일 transactionId와 ADM Timeline을 검증한다. | executable EDU/Reference, Source+Test+Harness, 3 Vendor/actual broker where applicable, failure/process-kill/restart Evidence |
| `STARTER-DX` | all Starter owners + generator/build | Canonical Starter Catalog의 모든 활성 Starter가 OSS 직접 사용보다 편하고 안전한 개발경험을 제공해야 한다. 편의 API, AutoConfiguration, 최소 설정, safe default, Fail-Fast, CPF Error/Security/Audit/Masking/Observability/Transaction 연계, Provider 확장성, Native API Escape Hatch, 미사용 0-footprint와 실제 EDU Consumer를 갖춘다. Wrapper-only/consumer-less Starter는 완료가 아니다. | 활성 Starter 전수 DX matrix, OSS-direct 대비 사용 코드/Config 비교, actual Consumer, boot context, failure/timeout/retry/unknown, selected-only footprint, EDU Evidence |
| `AI-OPTIONAL` | Optional AI Starter/Capability owner | 특정 AI Provider에 종속되지 않는 Optional AI API/SPI를 제공하고 model/provider routing, timeout/retry/circuit breaker/fallback, sensitive data masking, token/usage/cost metering, audit/observability, transactionId, authorization/policy, 위험 작업 approval, provider failure/UNKNOWN을 제공한다. 자체 LLM·Vector DB·거대 Agent Framework를 제품 기본범위로 만들지 않는다. | 최소 2 Provider 또는 1 Provider+customer plugin conformance, sensitive-data negative test, timeout/fallback/unknown, usage/cost/audit, approval, actual Consumer/EDU Evidence |
| `BAT-CORE` | cpf-batch | Spring Batch를 Job/Step/Repository/ExecutionContext/Restart의 단일 Primary Engine으로 사용하고 자체 중복 실행 Engine을 제거한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-JOB` | cpf-batch | immutable approved definition/plan checksum, Job identity, parameter schema, start/stop/restart/abandon/recover/reconcile와 상태 연결을 제공한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-ITEM` | cpf-batch | reader/processor/writer, chunk/skip/retry/checkpoint, item idempotency, partition, restart와 대용량 memory bound를 제공한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-EXECUTOR` | cpf-batch | Java, approved Shell, File Watch/Process/Transfer, Service/API, Message Executor를 Step 안에서 timeout·resource·security 정책과 함께 제공한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-AGENT` | cpf-batch | Agent pool, capability, zone, lease, heartbeat, drain, takeover, artifact/config, process tree와 execution output budget을 제공한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-CALL-SYNC` | cpf-batch | Batch/Worker의 업무 Domain 동기 호출에 Local/Remote parity, Header, deadline, idempotency, retry/unknown-result를 적용한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-CALL-ASYNC` | cpf-batch | Batch/Worker의 Event/Outbox 비동기 호출에 stable message ID, retry/DLT, consumer idempotency와 completion correlation을 적용한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-SHARED` | cpf-batch | Batch가 온라인/공유 Facade를 재사용할 때 Owner, transaction boundary, load isolation, version, topology와 운영 영향도를 정의한다. | Spring Batch Repository/Execution ID, DB claim/lease/fencing, 2개 이상 Instance, process kill·restart·UNKNOWN·reconcile Evidence |
| `BAT-NO-REMOTE-KAFKA` | cpf-batch | CPF 일반 Batch·Worker·Scheduler·Center-Cut은 Kafka 기반 Batch Remote Execution을 사용하지 않는다. `cpf-batch/remote-kafka`, `REMOTE_PARTITION`, `REMOTE_CHUNK`, `REMOTE_STEP`, Remote Worker/Config/API/진단, `BAT_REMOTE_MESSAGE_LEDGER`의 Current Schema·Query·Publication·Harness는 제거하고 새 Remote Transport나 대체 Broker를 만들지 않는다. 공용 CPF Kafka Messaging은 별도 Messaging Owner/Consumer가 있을 때만 독립 Capability로 존재하며 Batch 때문에 존치하지 않는다. Released V87/R87은 immutable history로 보존하고 V140/R140으로 Upgrade/Recovery를 제공한다. | 실제 Consumer/Bean Wiring/Runtime Config/DB Query 역추적, Active Remote Dead Surface 0, 일반 Batch 5 Runtime·Center-Cut Domain Invocation·2+ Worker·Kill·Retry/UNKNOWN/Recovery, DB3 V140/R140, Fresh Replay Evidence |
| `CENTER-CORE` | cpf-batch | Center-Cut의 job/item/attempt/aggregate 상태모델, immutable policy와 Spring Batch/업무 transaction 경계를 제공한다. Target 준비 상태(`CREATED`→`TARGETING`→`TARGET_READY`)와 실행 승인 상태(`STARTING`→`RUNNING`)를 분리하며, requester/approver가 분리된 Control Plane START만 업무 처리를 승인한다. Center-Cut은 DB Work Item/Claim/Lease/Fencing과 공식 Domain Invocation으로 실행하며 Kafka/Broker 기반 Remote Execution을 사용하지 않는다. Batch 전용 Remote Transport나 대체 Broker를 새로 만들지 않는다. | Spring Batch Repository/Execution ID, DB Work Item, 2개 이상 Instance, 승인 전 Claim 0, 승인 후 공식 Domain Invocation, process kill·restart·reconcile Evidence |
| `CENTER-RUNNER` | cpf-batch | CenterCutRunner를 Agent 내장 또는 독립 Process로 배포하고 target generation→DB work item→승인된 START→claim/lease/fencing→Worker→공식 Domain Invocation→result aggregate lifecycle을 제공한다. Runner의 Target 생성 완료만으로 `RUNNING`을 만들지 않는다. | DB 기반 target/work/claim/lease/fencing, 승인 전 Claim 0, 2개 이상 Worker, 실제 업무 Domain Operation, result aggregate·drain·recovery·reconcile Evidence |
| `CENTER-PARAM` | cpf-batch | 대량 작업 parameter snapshot, schema, canonical hash, encryption/masking, version과 replay 재현성을 제공한다. | immutable parameter snapshot→target provider→DB Work Item, restart/replay parity Evidence |
| `CENTER-CLAIM` | cpf-batch | item claim, lease, fencing, chunk assignment, stale worker 차단, duplicate prevention과 restart를 제공한다. | DB claim/lease/fencing, 2개 이상 Worker, process kill·stale owner rejection·restart Evidence |
| `CENTER-RATE` | cpf-batch | global/domain/target TPS·RPS, concurrency, backpressure, adaptive throttle, pause/drain과 multi-instance 일관성을 제공한다. | DB/Worker Control 기반 throttle·pause·drain·takeover, multi-instance Evidence |
| `CENTER-REPROCESS` | cpf-batch | failed-only·selected·range 재처리, approval, idempotency, prior result 보존, compensation와 결과 비교를 제공한다. | DB Work Item/Attempt, 공식 Domain Invocation, duplicate·partial failure·reprocess Evidence |
| `CENTER-UNKNOWN` | cpf-batch | item/attempt 결과 불명을 분류·대사하고 확인 전 무조건 재처리를 금지하며 수동 확정과 audit를 제공한다. | Domain Invocation UNKNOWN→result persistence→reconcile/manual decision, process kill Evidence |
| `CENTER-OPS` | cpf-batch | ADM에서 job/item/attempt/timeline/progress/error/reprocess/pause/cancel/drain을 권한·사유·승인과 함께 제공한다. | DB/Worker Control 기반 운영 명령, transactionId/segment/attempt Timeline, ADM Evidence |
| `ADM-AUTH` | cpf-admin | 운영자 identity, password/MFA/OIDC, JDBC Session, session fixation·concurrency·revocation·force logout과 fail-closed product profile을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-RBAC` | cpf-admin | menu/button/API/command 권한, role mapping, 유효기간, organization context, server-side enforcement와 cache invalidation을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-AUDIT` | cpf-admin | 운영자·대상·before/after masked snapshot·reason·approval·result·transactionId의 immutable/tamper-evident audit를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-TX` | cpf-admin | 온라인/비동기/Batch/외부연계 transaction 검색, 표준 Header, payload masking, segment/attempt linkage와 상세 조회를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-TIMELINE` | cpf-admin | transactionId 기준 Local/Remote/Event/Batch/Gateway/File/Agent timeline을 순서·시각·instance·failure stage로 재구성한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-SERVICE` | cpf-admin | service/endpoint/instance/health/version/zone/routing/maintenance/draining을 조회·제어하되 Owner Command API를 사용한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-LOG` | cpf-admin | File/DB Log 조회, saved search, trace boost, dynamic log level, retention, download guard와 권한·audit를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-BATCH` | cpf-admin | Job definition/execution/step/checkpoint/restart/stop/recover와 승인·사유·Spring Batch ID 연계를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-CENTER` | cpf-admin | Center-Cut job/item/attempt/progress/reprocess/unknown/compensation 운영 기능을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-AGENT` | cpf-admin | Agent/Runner/Worker registry, capability, heartbeat, artifact, process, drain/takeover와 위험조치 승인을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-EXS` | cpf-admin | 외부기관 endpoint, health, credential/certificate status, request/response timeline, unknown/reconciliation을 기술 Owner API로 관제한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-RECOVERY` | cpf-admin | unknown-result, DLQ, Saga, deployment, file/batch 실패의 runbook, 승인된 recover/compensate/reconcile와 결과 추적을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-INCIDENT` | cpf-admin | alert→incident→severity/owner→runbook/action→postmortem/closure 흐름과 관련 transaction/evidence를 연결한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-UX` | cpf-admin | 대량 검색·paging·sort·filter·saved condition·status·empty/error/loading·responsive·keyboard·accessibility·safe download UX를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `ADM-APPROVAL` | cpf-admin | 플랫폼 위험조치의 versioned policy, ALL/ANY/N_OF_M, SoD, expiry, break-glass, immutable command hash와 owner-command execution을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `MBW-BUSINESS` | cpf-backoffice | 고객 업무 관리자 메뉴·권한·업무 조회·등록·변경·download·approval을 업무 Domain Public Contract로 수행한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `MBW-ORG` | cpf-backoffice | 조직 hierarchy, 직원, 사번, 직급/직책, 유효기간 assignment, 겸직/파견/대행, masked profile과 결재 snapshot을 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `MBW-APPROVAL` | cpf-backoffice | 순차/병렬/개인/role/조직/ALL/ANY/N_OF_M/위임/대결/회수/재상신/만료/동시승인과 policy/instance 분리를 제공한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `MBW-SEQUENCE-SAMPLE` | cpf-backoffice | 업무 채번을 선택형 Customization Sample로 제공하되 기본 Runtime 의존을 만들지 않고 규칙·시험·승인·audit를 교육한다. | Server 권한 Test, API/OpenAPI, DB migration, Chromium·Firefox·WebKit E2E, 위험조치 audit Evidence |
| `SEC-AUTHN` | cpf-core security contract + product owner | 사용자·운영자·service의 MFA/OIDC/OAuth2/JWT/API key/mTLS 인증, credential lifecycle, session/token replay 방어를 제공한다. Resource Server뿐 아니라 OIDC/OAuth2 Login 기반 SSO를 Keycloak·Microsoft Entra ID·Okta 등 외부 IdP와 연동하고 user/tenant/role/group/scope/claim을 CPF Security Context로 안전하게 매핑하며 login/logout/session/token 만료·갱신과 Frontend/BFF 연결을 제공한다. SAML2는 필요 시 Optional 확장으로 둔다. | 보안 Negative Corpus, credential/PII leak scan, issuer/audience/expiry/claim mapping, login/logout/session/refresh, IdP failure, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-AUTHZ` | cpf-core security contract + product owner | RBAC/ABAC, least privilege, server-side resource/action authorization, SoD, permission version과 즉시 회수를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-SECRET` | cpf-core security contract + product owner | Secret Provider SPI, 외부 Vault/file/env 및 KMS/HSM Provider integration, key version/rotation/revocation/provider health/failure-timeout, 필요 시 PKCS#11 연계, zeroization와 log/config/ADM/evidence의 key·secret 원문 금지를 제공한다. Local/JCE와 외부 KMS/HSM은 동일 계약을 따르되 Provider 고유 기능을 불필요하게 가두지 않는다. | 보안 Negative Corpus, credential/PII leak scan, KMS/HSM/provider failover·health, key version/rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-CERT` | cpf-core security contract + product owner | certificate/key trust store, issuance/import, expiry alert, rotation, revocation, mTLS identity와 keyId 기반 검증을 제공한다. 기존 Crypto/Secret을 재사용하여 범용 digital signature의 sign/verify, algorithm, keyId/keyVersion, certificate, signature metadata와 audit를 제공하고 Private Key 원문 노출을 금지한다. | 보안 Negative Corpus, credential/PII leak scan, sign/verify negative corpus, key/certificate rotation·revocation, 권한·audit와 침해경계 Evidence |
| `SEC-PRIVACY` | cpf-core security contract + product owner | PII catalog, 목적·최소수집·동의/법적근거, masking, raw access, retention/deletion, export와 audit를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-DOWNLOAD` | cpf-core security contract + product owner | 대량/민감 download의 권한·사유·승인·watermark·row/size limit·expiry·encryption·one-time link와 audit를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-APP` | cpf-core security contract + product owner | injection, SSRF, path traversal, upload/archive bomb, XSS/CSRF, deserialization, process execution, security header와 secure default를 통제한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-APPROVAL` | cpf-core security contract + product owner | 보안 위험행위의 dual control, 자기승인 금지, immutable target hash, expiry, break-glass, 사후 review를 제공한다. | 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `SEC-AUDIT` | cpf-core security contract + product owner | audit append-only/tamper detection, canonical payload, previousHash/currentHash chain, 선택적 digital signature, record 수정·삭제 탐지, concurrency/multi-instance 일관성, clock/identity, retention, search, export와 evidence integrity를 제공한다. Masking 후 canonicalization과 검증 순서를 명확히 하고 Audit 자체가 Secret/PII 원문 저장소가 되지 않게 한다. | hash-chain mutation/delete/reorder/concurrency/multi-instance 검증, signature verification, 보안 Negative Corpus, credential/PII leak scan, rotation/revocation, 권한·audit와 침해경계 Evidence |
| `OPS-METRIC` | cpf-admin control plane + runtime owner | transaction/service/instance/DB/Broker/Batch/Gateway/Agent의 bounded-cardinality metric과 dashboard/export를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-SLO` | cpf-admin control plane + runtime owner | availability, latency, error, freshness, backlog, recovery SLI/SLO와 error budget, burn-rate alert를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-ALERT` | cpf-admin control plane + runtime owner | dedup, grouping, inhibition, severity, routing, escalation, maintenance suppression와 acknowledgement를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-INCIDENT` | cpf-admin control plane + runtime owner | incident lifecycle, commander/owner, communication, timeline, evidence, action item와 problem linkage를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-RUNBOOK` | cpf-admin control plane + runtime owner | 탐지조건·영향·진단·안전조치·rollback·escalation·검증·종결 기준을 실행 가능한 runbook으로 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-SELF` | cpf-admin control plane + runtime owner | 자동진단/자동복구의 allowlist, rate/attempt limit, circuit stop, approval boundary, rollback와 immutable audit를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-TOPOLOGY` | cpf-admin control plane + runtime owner | service/instance/dependency/domain/owner/database/broker/endpoint 관계를 versioned topology/service catalog로 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-MAINT` | cpf-admin control plane + runtime owner | maintenance, admission block, drain/quiesce, in-flight deadline, health/routing 반영, resume와 audit를 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-CONFIG` | cpf-admin control plane + runtime owner | runtime config catalog, schema, encryption, version, staged rollout, approval, dynamic apply, rollback와 drift detection을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-DRIFT` | cpf-admin control plane + runtime owner | Source/Artifact/Config/DB/Route/Permission/Runtime version의 desired-actual drift를 탐지·차단·복구한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-CAPACITY` | cpf-admin control plane + runtime owner | CPU/memory/thread/connection/queue/storage/DB/Broker 용량과 threshold, trend, forecast, load test 기준을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `OPS-DR` | cpf-admin control plane + runtime owner | RTO/RPO, multi-zone/site, backup/restore, failover/failback, data consistency, runbook와 정기 DR drill을 제공한다. | 운영 API/UI, multi-instance 상태, alert/runbook, 장애주입·복구·감사 Evidence |
| `DEVEX-QUICK` | cpf-tools + public artifacts | 신규 개발자가 표준 환경에서 설치→생성→빌드→실행→테스트→디버깅을 문서대로 재현할 수 있는 quick start를 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-CODEGEN` | cpf-tools + public artifacts | OpenAPI/Orval/DB metadata/domain template 등 code generation의 exact input SHA, deterministic output, drift gate와 user-owned 영역 보호를 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-COMMENT` | cpf-tools + public artifacts | Public API/SPI, 주요 Service/Controller, 복구·동시성·보안 로직에 유지보수 가능한 한글 JavaDoc/주석과 설정 설명을 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `DEVEX-LAYER` | Foundation/Starter owners + Domain owners + generator | Controller/Service/Batch 등 주요 실행 계층이 `CPF 공통 → Domain 공통 → 업무 구현` 표준 확장점을 경유하며 Runtime 의존 Base는 Core 밖의 올바른 Owner가 소유한다. DTO/Entity/Repository는 기술 제약에 따라 interface/composition/meta-annotation으로 동등한 확장성을 제공한다. | Generated/handwritten 실제 Consumer 계층, ArchUnit/compile gate, stale base FQCN 0, Native extension Evidence |
| `DEVEX-ANNOTATION` | Foundation/Web/Data/Batch/Messaging/Integration owners | CPF 의미가 있는 meta-annotation과 declarative API로 등록·Context·Validation·Error·Audit·Transaction·Retry 등의 plumbing을 단순화하되 표준 Spring/Jakarta API와 Native Escape를 유지하고 no-op wrapper/annotation pollution을 금지한다. | Annotation consumer matrix, IDE/JavaDoc, boot/AOP/interceptor runtime, misuse negative test, Native API parity Evidence |
| `DEVEX-VALIDATION` | Foundation/Web/Batch/Messaging/Integration + generator | Bean Validation을 기본으로 Body/Query/Path/Header/Method/Batch Parameter/Message/Integration DTO와 CPF 전용 값/교차필드 검증을 일관되게 제공하고 실패를 CPF Error/OpenAPI/Frontend 계약으로 연결한다. | 실제 validation consumer, invalid corpus, header trust 분리, Generator/OpenAPI/Frontend parity Evidence |
| `DEVEX-ERROR` | Core error semantics + endpoint/provider owners | 개발자가 반복 try/catch·ErrorResponse 조립을 하지 않도록 기술중립 Error taxonomy와 endpoint별 mapping을 제공한다. HTTP/Persistence/External/Batch/Message mapping은 각 Owner가 담당하고 secret/stack/provider detail은 외부 응답에 노출하지 않는다. | Web/DB/Integration/Message/Batch error runtime, mapping contract, masking/unknown/retry negative Evidence |
| `DEVEX-LOGGING` | platform-operations/observability + runtime owners | 일반 SLF4J 사용만으로 Context/trace/instance/operation이 구조화 로그에 자동 연결되고 masking, slow/error, integration/message/batch lifecycle을 제공한다. Audit/Performance annotation은 필요한 곳에만 사용하며 전 메서드 log annotation 강제를 금지한다. | 실제 업무/Batch/Message/Integration 로그, MDC leak test, masking, dynamic level, ADM trace lookup Evidence |
| `DEVEX-UTILITY` | base + capability owners | 반복 업무 Utility를 Date/Time/BusinessDate/Money/Decimal/ID/Validation/Text/Collection/JSON/File/Hash/Paging 등 올바른 Owner에 제공하되 OSS 단순 재포장·God Utils·Core utility dump를 금지하고 typed/safe API와 Test를 제공한다. | Utility ownership catalog, duplicate/dead check, boundary/security tests, actual consumer/EDU Evidence |
| `DEVEX-TESTKIT` | cpf-tools/testing/cpf-testkit + capability owners | Context/Security/Tenant/Transaction/BusinessDate/DB/REST/Message/Batch/Logging/Audit/Idempotency/Retry fixture와 assertion을 제공하여 제품 Golden Path를 쉽게 검증하고 실제 Provider runtime test로 연결한다. | Testkit actual consumers, provider runtime bridge, leak/failure/retry/restart assertions, Generated Domain Evidence |
| `ONBOARD-DOMAIN` | cpf-tools generator/setup + public artifacts | DomainName+SystemCode를 기준으로 신규 업무 Domain을 생성하되 identity, DB binding, capability/preset, Domain dependency, external client를 한 번 정의하여 Public Starter, config, workspace, bootstrap/runtime 준비까지 연결한다. duplicate와 위험 변경은 fail-closed하며 user-owned source를 보호한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-ACC` | cpf-education / generated reference | 범용 계정/업무 흐름을 과도한 제품 원장 없이 Local/Remote·validation·transaction·error 사용법을 보여주는 선택형 Sample로 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-MBR` | generator verification + retained generated reference domains | `member`(MBR)와 `external`(EXS)을 동일 Generator/Schema/Template으로 `cpf-member/`, `cpf-external/`에 생성·유지한다. 둘 다 Feature-First Online, Public Starter, CUSTOMER_BUSINESS_DB, sample transaction을 검증하고 member는 `batch=true`, external은 `batch=false` 조합으로 Optional Batch 생성/미생성을 검증한다. | fresh generation→member/external normalized parity→sample DB transaction→Online compile/test/runtime→DB3→Batch capability include/exclude 독립 회귀→hardcoding scan→dry-run/diff/regenerate/idempotency/upgrade/remove/restore→user-owned 보호→최종 Root 보존 Evidence |
| `SAMPLE-REF` | cpf-education / generated reference | cpf-education에서 제품 Public API의 정상·오류·경계·복구·권한·운영 사용법을 실제 Runtime으로 교육한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-BIZADM` | cpf-education / generated reference | Backoffice 선택형 업무관리/채번/결재 Customization Sample을 기본 활성화 없이 제공한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `SAMPLE-EDU` | cpf-education / generated reference | 교육 시나리오가 장난감 계약을 만들지 않고 실제 Header/API/DB/Event/Batch/Security 표준을 사용한다. | fresh clone에서 생성→build→runtime→remove→regenerate, normalized parity와 사용자 코드 보호 Evidence |
| `API-CONTRACT` | cpf-core API contract + endpoint owner | HTTP/Local/Remote API의 header, auth, success/error, version, idempotency, permission, content type, example과 consumer compatibility를 정의한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-PAGING` | cpf-core API contract + endpoint owner | offset page, slice, keyset/signed cursor, sort/filter allowlist, stable ordering, max size와 count 비용 정책을 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-ASYNC` | cpf-core API contract + endpoint owner | 202 acceptance, operationId, status/result polling, callback/event, cancellation, idempotency, expiry와 unknown-result를 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `API-FILE` | cpf-core API contract + endpoint owner | multipart, streaming upload/download, range, checksum, content disposition/type, size/virus policy, cancellation와 secure filename을 제공한다. | OpenAPI/Generated Client/Consumer Contract, 정상·오류·경계·호환성·streaming Runtime Evidence |
| `RULE-ARCH` | cpf-tools quality gates | Module/package/dependency/owner/internal API/DB access/dual primary/generated drift 위반을 자동 Architecture Gate로 차단한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `RULE-SEC` | cpf-tools quality gates | secret/credential/URL/TLS/security header/unsafe API/path/query/log/evidence pattern과 dependency vulnerability를 자동 Gate로 차단한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `RULE-QUALITY` | cpf-tools quality gates | compile/static analysis/duplication/dead code/dependency lock/license/SBOM/test coverage/marker-only 구현을 자동 Gate로 검증한다. | 정상 저장소 PASS와 의도적 위반 Negative Fixture FAIL Evidence |
| `TEST-UNIT` | repository-wide test ownership | 순수 로직·validation·state transition·error mapping·serialization·security utility를 deterministic unit test로 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-CONTRACT` | repository-wide test ownership | Public API/SPI, Local/Remote, OpenAPI, message schema, DB query, generated client와 published artifact compatibility를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-RUNTIME` | repository-wide test ownership | 실제 Java25/WAS/DB/Process 환경에서 startup, endpoint, transaction, shutdown, recovery와 resource leak를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-BROWSER` | repository-wide test ownership | ADM/Backoffice의 Chromium/Firefox/WebKit, route/deep link/session/permission/error/a11y/keyboard/responsive를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-BROKER` | repository-wide test ownership | 실제 Kafka·JMS/IBM MQ·RabbitMQ 지원 Matrix에서 ACK, transaction, duplicate, ordering, redelivery/rebalance, retry/DLQ, broker outage와 consumer crash를 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-FAULT` | repository-wide test ownership | DB/network/broker/disk/process/time/response loss를 side-effect 전후에 주입해 idempotency·unknown·recovery·compensation을 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `TEST-EVIDENCE` | repository-wide test ownership | 모든 검증의 exact SHA, command, environment, time, exit code, report/artifact hash, sanitized 여부를 schema로 검증한다. | exact SHA·명령·환경·exit code·report hash가 있는 직접 실행 Evidence |
| `REL-BUILD` | cpf-tools release/publication | fresh clone과 clean isolated cache에서 Java 25/Gradle build, dependency lock, BOM/POM/reproducible binary artifact를 제공한다. 기본 Binary Distribution에는 Framework Source와 sources/javadoc JAR가 없고 Public consumer 검증은 `mavenLocal()`과 Private Repository 없이 Public Binary Repository만으로 수행한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-DEPLOY` | cpf-tools release/deploy | signed artifact, environment/channel binding, install lock, rolling/canary/blue-green, health/smoke, selective rollback와 deployment reconciliation을 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-MIG` | cpf-tools release/deploy | 제품/DB/config/API/message/file schema의 install/upgrade/downgrade/forward recovery, compatibility window와 migration guide를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `REL-COMPAT` | cpf-tools release/deploy | semantic version, compatibility range, deprecation, consumer matrix, rolling mixed-version, rollback와 unsupported combination fail-closed를 제공한다. | fresh clean build, signed final artifact, install/upgrade/rollback, mixed-version와 final-artifact SBOM Evidence |
| `DOC-GOV` | cpf-docs governance + source owner | Current Target, Current Work Request, Requirement Status, Evidence, 사용자 Guide의 역할을 분리하고 같은 목적의 날짜/세션/REV/Checkpoint/History 복제본을 Repository Current Surface에 남기지 않는다. 현재 Requirement에 흡수된 과거 문서는 Delete Manifest로 제거한다. | Source/API/SQL/Test와 문서의 링크·예제·명령을 현재 exact SHA에서 검증한 Evidence |
| `DOC-PRODUCT` | cpf-docs + source owner | 개발자·운영자·ADM/Backoffice·Gateway·Batch·설치·Migration·복구 Guide, OpenAPI, JavaDoc가 실제 Source와 일치하게 한다. | Source/API/SQL/Test와 문서의 링크·예제·명령을 현재 exact SHA에서 검증한 Evidence |
| `PROD-EDITION` | product governance | Edition/License/Capability packaging을 기술 Runtime과 분리하고 미결정 정책을 GA 완료처럼 노출하지 않는다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-MULTITENANT` | product governance | tenant context, data/config/permission/secret/quota/audit isolation과 tenant lifecycle을 선택 기능으로 설계한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-PLUGIN` | product governance | 고객/기관 Plugin·Adapter의 SPI, package, signature, compatibility, isolation, lifecycle, permission와 marketplace 후보 정책을 제공한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `PROD-PACKAGE` | product governance | 산업군·금융권별 capability package의 dependency, install/upgrade, config, license, compatibility와 support boundary를 제공한다. | ADR, capability boundary, packaging/compatibility/security prototype와 명시적 지원 상태 |
| `REQ-GOV` | requirement governance | Requirement ID, owner, priority, acceptance, status, traceability와 변경 규칙을 단일 Current Canonical Catalog로 관리한다. Current Catalog에 History/Alias 행을 섞지 않고 과거 식별자는 negative/compatibility scan에서만 관리한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-REVIEW` | requirement governance | 각 작업 전후 Requirement→구현과 구현→Requirement를 독립 검수한다. 정본을 현재 Source에 맞춰 약화하지 않으며 Source가 Target과 다르면 Source Gap으로 기록하고 구현·검증한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-CODEX` | requirement governance | Codex/AI 요청서가 이전 대화 없이 실행 가능하도록 baseline, scope, architecture, acceptance, evidence, 금지조건과 output을 포함한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `REQ-GAP` | requirement governance | 새 상용 필수 요구를 기존 ID와 중복 검사해 intake하고 split/supersede/deprecate 관계와 count 변화를 기록한다. | Continuity Ledger, 양방향 Trace Matrix, 독립 Review와 Count/상태 무결성 Gate |
| `FOUNDATION-UTILITY` | pure foundation + foundation convenience starter | Core를 Utility 창고로 사용하지 않는다. `CpfClock/Dates/Decimals/Ids/Json/Lists/Maps/Numbers/Strings/Times/Validation/Values/Files/Hashes/Headers/Pages/Attributes` 등 현재 Core Utility를 전수 분류하여 JDK/Spring 단순 Wrapper는 제거 후보로 전환하고, CPF 고유 정책 가치가 있는 순수 기능만 topology-independent Foundation으로 이동한다. Header/Crypto/File/Paging/TransactionId처럼 Owner가 분명한 기능은 해당 Capability로 이동한다. 업무 개발자는 Application Convenience Starter/Profile을 통해 쉽게 사용하되 Core는 Starter를 참조하지 않는다. | Core Utility class-by-class ownership matrix, Core→Starter 0, simple-wrapper 0, actual consumer, deterministic test, native JDK/OSS escape, relocation duplicate 0 |
| `CACHE-REDIS-PROVIDER` | data/cache provider | 기존 `cache-valkey`를 유지하면서 `cache-redis`를 공식 Optional Provider Starter로 추가한다. Redis/Valkey는 `CpfCache`/invalidation/health/metrics/recovery 의미와 Spring Data Redis protocol runtime을 내부 공통 leaf로 공유하고, `cache=redis` 또는 `cache=valkey`를 Catalog/Profile/Generator에서 명시적으로 선택한다. Redis 연결 정상·장애·재연결, durable invalidation, multi-instance, duplicate/out-of-order/version fence, process-kill/reconcile을 검증하며 미선택 Provider는 0-footprint를 지킨다. | `cache-redis`/`cache-valkey` provider parity, shared-runtime duplicate 0, Catalog/BOM/Metadata/Generator/Profile/Sample/EDU, Redis actual runtime outage/reconnect, Valkey regression, multi-instance/process-kill/reconcile Evidence |
| `SEC-SESSION-DIST` | security/session provider | 기존 JDBC Session을 유지하면서 Multi-instance용 Valkey Distributed Session Provider를 Optional로 제공한다. expiration/renewal/rotation, fixation 방어, concurrent-session control, forced logout/logout propagation, user·tenant index, audit/metrics, provider failure와 0-footprint를 제공한다. | JDBC/Valkey provider parity, 2+ instance login/logout/revoke, provider outage/expiry/rotation test, security negative corpus, optional removal boot evidence |
| `FILE-OBJECT-STORAGE` | file/attachment + object-storage provider | Attachment/Archive/SFTP와 중복 Public API를 만들지 않고 S3-compatible Object Storage를 Provider-neutral하게 제공한다. streaming, multipart, checksum, range, metadata, presigned access, encryption/KMS, tenant isolation, timeout/retry, partial failure, orphan reconcile, retention/lifecycle와 malware-scan hook을 지원한다. | Attachment/Object Storage ownership trace, AWS S3 또는 MinIO reference provider, stream/multipart/failure/reconcile test, security/audit, 0-footprint, actual consumer |
| `EVENT-SCHEMA` | messaging contract governance + generator | Kafka/RabbitMQ/JMS/IBM MQ의 Broker 선택과 독립적인 Event Contract Governance를 제공한다. JSON Schema/Avro/Protobuf version, backward/forward compatibility, breaking-change gate, producer/consumer validation, generated model, schema id/content type와 provider-neutral registry boundary를 제공한다. | compatibility corpus, producer/consumer contract test, breaking-change CI gate, generated model, broker-independent reference, EDU |
| `API-GRAPHQL` | optional web/graphql starter + application service owner | REST/OpenAPI를 기본 API로 유지하면서 Browser/Mobile BFF와 복합 Domain Query를 위한 Optional GraphQL을 제공한다. Resolver는 Service/Application Layer를 재사용하고 Query/Mutation, 필요 시 Subscription, CPF Error/Paging/Cursor/Sort/Search, authN/authZ/field auth/tenant/transactionId/audit/trace, depth/complexity/size/rate-limit, N+1/DataLoader, introspection/GraphiQL prod policy와 Native Spring GraphQL escape를 제공한다. | real BFF consumer, schema/contract test, field-auth negative test, N+1 guard, query limit test, REST service reuse, 0-footprint |
| `API-REALTIME` | web/operations capability | Batch progress, Transaction Timeline, Runtime/Health State와 long-running operation을 위해 Server→Browser 단방향은 SSE를 우선하고 실제 양방향 요구에만 WebSocket을 사용한다. authN/authZ, reconnect/heartbeat, duplicate, slow-consumer/backpressure, rate limit, multi-instance fan-out, graceful shutdown, fallback polling과 typed frontend consumer를 제공한다. | SSE reference consumer, optional WebSocket consumer where justified, reconnect/duplicate/backpressure/multi-instance test, frontend typed consumer, fallback evidence |
| `CPF-SYSTEM6` | cpf-core contract + trusted ingress/runtime adapters | 업무 Domain Online Transaction의 논리 거래 Context를 `X-Transaction-Id`, `X-Original-System-Code`, `X-System-Code`, `X-Caller-System-Code`, `X-Target-System-Code`, `X-Target-Operation-Id` 6개로 고정한다. Browser는 작성하지 못하고 Same-JVM은 in-process Context, Remote 경계는 6개를 serialize/deserialize하며 Receiver는 System/Target/Operation 정합을 Controller 실행 전에 검증한다. | Header/Context unit+contract, Browser spoof negative, Same-JVM/Remote parity, mismatch pre-controller reject, async/retry propagation Evidence |
| `CPF-INSTANCE` | runtime identity + platform operations | `instanceId`는 명시 property/env를 우선하고 없으면 실제 runtime hostname을 bootstrap 시 1회 확정한다. localhost/unknown/Domain명 등 fabricated fallback을 금지한다. 동일 Host에서 동일 System의 다중 Process는 explicit instanceId가 필수이며 active `{systemCode,instanceId}` 충돌 시 READY 금지다. | bootstrap unit, hostname failure, same-host multiprocess collision, registry/readiness, log/trace/ADM identity Evidence |
| `CPF-OPERATION` | online transaction contract + operation registry/policy | `@CpfOnlineTransaction.operationId` = OpenAPI operationId = Target Operation Header = Domain Client = Registry/ADM/Log/Trace의 단일 안정 ID다. Source는 operation 사실/metadata/discovery를, ADM Policy는 enabled/allowedCallerSystems 및 별도 channel policy를 소유한다. Source 미발견은 단일 instance 기준 자동삭제하지 않는다. | annotation/OpenAPI/client parity, policy seed/override, multi-instance discovery, ALL/unknown/disabled negative, LKG/fail-close Evidence |
| `GEN-DOMAIN` | cpf-tools generator + generated business domain contract | Generated Business Domain은 고객 개발자가 실제 업무를 개발하는 결과물 영역이다. `cpf-<domain>/online/<feature>/<technical-role>` Feature-First를 기본으로 하고 `batch=true`일 때만 `batch/<feature>/<role>`을 생성한다. 공유 `domain/`은 실제 2개 이상 Runtime Consumer가 있을 때만 만든다. Public Starter/API와 Developer-Facing Source/Build/설정만 노출하며 Internal Leaf, vendor DB source folder, Generator 입력·lock·state·cache·evidence를 생성하지 않는다. | fresh generate normalized parity, root cleanliness, package/ownership gate, forbidden metadata 0, internal dependency 0, optional batch include/exclude, user source protection Evidence |
| `GEN-SETUP` | cpf-tools domain setup/sync | Domain root의 source-controlled `gradle.properties` `cpf.domain.*` Developer Contract에 logical identity/module/capability/dependency/integration을 선언하고 `domain setup/create/sync/diff`가 workspace, build, Public Starter, config template, generated clients, bootstrap/runtime discovery 준비를 일관되게 처리한다. 명시적 외부 descriptor는 transient 입력일 뿐 결과 Root에 저장하지 않는다. 위험 변경과 exact Generated-owned 삭제는 dry-run/diff 및 명시적 승인으로 fail-closed하고 user-owned source를 silent overwrite하지 않는다. | setup validation corpus, duplicate/port/dependency cycle negative, dry-run/idempotency/sync, stateless regenerate, user-owned protection, public workspace consumer Evidence |
| `GEN-CUSTOM-LIB` | cpf-tools customer shared library generator | 고객사는 `cpf-common`을 수정하지 않고 회사 공통 함수·DTO·검증·공통 서비스를 독립 JAR 작업공간으로 생성할 수 있다. `cpf library create/attach/sync/verify`는 `customer-libraries/<name>`을 만들고 Java 25 build/publication 계약을 제공한다. Customer Library는 모든 Domain에 자동 주입하지 않으며 개발자가 선택한 Domain에만 Generated Gradle composite/dependency를 연결한다. 생성 Source와 CLI 도움말은 한글 설명을 기본으로 하고 `com.cpf.*` namespace 및 CPF Internal dependency를 fail-closed한다. | create/attach/sync/verify, Korean generated guidance, explicit domain consumer, idempotent sync, internal dependency negative, Java25 build/test/publication Evidence |
| `DB-BINDING` | cpf-tools DB lifecycle + generated domain setup | Logical Domain Definition과 environment-specific DB Binding을 분리한다. Generated Domain의 logical DB ID는 기본 `<systemCode lower>DB`이고 Oracle/PostgreSQL/MariaDB 중 Domain별 vendor를 독립 선택한다. host/service/database/schema, migration principal, runtime principal, secret reference는 Binding이 소유하며 raw secret은 Source/Evidence에 금지한다. | DB3 binding matrix, migration/runtime account separation, secret scan, persistence=none/no-binding, required DB missing fail-close, vendor-change dry-run Evidence |
| `MBW-WEB` | cpf-backoffice-web | Backoffice Web은 Frontend SPA + Spring Boot BFF Reference다. DB dependency, CPF Internal Java dependency, Business Domain Java project dependency는 0이어야 한다. Published OpenAPI/protocol schema로 생성된 HTTP Client 사용은 허용·권장하며 Browser session/cookie/CSRF와 server-side credential propagation은 BFF가 소유하고 Browser는 protected CPF Header를 작성하지 않는다. | dependency gate, OpenAPI generated client consumer, session/CSRF/security negative, Browser header spoof negative, Gateway→MBW E2E Evidence |
| `CPF-LOGTX` | cpf-starters logging + transaction log persistence | 업무 Transaction의 성공/rollback/handled 4xx·5xx/exception과 무관하게 거래 DB Log Summary/Segment는 `cpfTransactionManager`의 독립 `REQUIRES_NEW` 경계로 요청·응답·상태·error metadata를 마스킹하여 보존한다. 업무 rollback으로 거래 추적이 함께 rollback되면 FAIL이며 DB log 장애는 durable fallback→recovery/replay로 추적성을 유지한다. | REQUIRES_NEW static gate, success/business rollback/handled error/exception runtime, File↔DB↔Segment↔Timeline transactionId parity, masking/secret, fallback/recovery Evidence |
| `REL-PUBLIC-WORKSPACE` | cpf-tools release/public workspace | Public Git Workspace는 empty-directory default-deny staging으로 PUBLIC 분류된 developer source/config/script/docs만 포함하고 Private framework/internal/provider/governance/QA/evidence/secret과 누적 CPF JAR을 포함하지 않는다. commit/push는 자동화하지 않는다. | unclassified=0, private leakage=0, secret=0, manifest/hash, clean clone build/setup/bootstrap, manual commit boundary Evidence |
| `REL-PUBLIC-BINARY` | publication/BOM/public artifact owners | Public Binary Repository는 Public BOM/API/Starter/Generator 및 공개 Runtime artifact를 버전·호환성·SBOM·서명과 함께 제공한다. Public Workspace는 이를 중앙 repository URL/version으로 소비하고 `mavenLocal()` 또는 Private repository에 의존하지 않는다. | isolated cache consumer build, BOM resolution, publication metadata/signature/SBOM, private repo/mavenLocal negative Evidence |
| `REL-OPEN-GIT-BOOTSTRAP` | cpf-tools release/open-git + runtime/bootstrap | 기본 Binary Open Git Fresh 환경에서 Framework 구현 Source 없이 `cpf bootstrap → domain-new → domain-sync → build → test → run/READY → stop → reset → fresh replay`가 수작업 복사 없이 성공해야 한다. Bootstrap은 Oracle/PostgreSQL/MariaDB 선택, Generated Domain 자동발견, 필요한 DB/Middleware 준비와 Current Binary Artifact 연결을 소유한다. | Fresh binary lifecycle DB3 matrix, domain discovery, middleware readiness, READY, stop/reset/replay, failure/rerun Evidence |
| `TOOL-UNIFIED-CLI` | cpf-tools runtime/cli + canonical tooling engines | CPF 공식 Tooling Interface는 exactly-one Java `cpf` CLI다. PUBLIC `bootstrap/domain-new/domain-sync/build/test/run/stop/reset/status`와 INTERNAL `dev/verify/publish/release` Namespace를 같은 Command Architecture로 제공하고 Generator/Initializer/Gradle/DB/Runtime/Verification/Publication/Release의 기존 Canonical Engine을 호출한다. 외부/내부 차이는 별도 CLI가 아니라 Capability/Profile Projection이며 Open Git은 PUBLIC만 노출한다. | exactly-one owner/source, command/artifact/owner catalog parity, internal capability negative in public jar, generator/bootstrap/build/test/runtime/DB3/publication/release consumers, Windows/Linux fresh lifecycle, internal/open-git golden path, duplicate/dead entrypoint 0 Evidence |
| `REL-CLI-CROSSPLATFORM` | cpf-tools runtime/cli + release/open-git | CPF 공식 CLI는 단일 Java `cpf-cli.jar`를 실제 구현체로 사용하고 Linux `cpf`, Windows `cpf.cmd`/`cpf.ps1`은 Thin Wrapper로만 제공한다. PowerShell-only/Shell-only 구현을 금지하며 동일 command/options/exit/error/log contract, Java25, UTF-8, 경로, prerequisite failure, rerun/idempotency, version/sourceIdentity parity를 보장한다. | Java CLI jar build/run, wrapper thinness, Windows/Linux lifecycle parity, UTF-8/path/negative prerequisite, identity/version, Fresh Open Git Evidence |
| `REL-OPEN-GIT-PROFILE` | cpf-tools release/open-git | Open Git은 고객 개발·실행 배포 채널이며 기본 `binary` Profile은 Framework 구현 Source와 sources/javadoc JAR 0건이다. Optional `source` Profile만 Default-Deny Canonical Public Source Allowlist의 Public API/SPI/Annotation Source를 Projection한다. 모든 Profile에서 Internal/Generator Engine/QA/Evidence/Secret/Internal Release Tool은 금지하고 동일 Current Source Identity에서 Fresh Build/Test/Runtime/Fresh Replay한다. Current Release 한 본만 유지한다. | binary/source profile contract, sources/javadoc=0, allowlist exact projection, internal/private leakage=0, same sourceIdentity, fresh release/replay Evidence |
| `REL-OPEN-GIT-CUSTOMER-SOURCE` | cpf-tools release/open-git + generated/customer source owners | 기본 Binary Profile에서도 고객이 직접 개발하는 Generated Domain, Backoffice, Sample/EDU, 고객 Config/SQL/Migration/Frontend/Generated Client/Test/Build Source Tree를 실제 수정 가능한 형태로 제공한다. Framework 내부 구현 Source Leakage=0과 Customer Development Source 누락=0을 동시에 강제하며 고객 Source를 sources.jar나 Framework Binary로 치환하지 않는다. | customer source allowlist completeness, generated domain/backoffice/frontend/sample build+test+runtime, framework/internal leakage negative, DB owner/sql parity, missing-customer-source mutation Evidence |
| `REL-OPEN-GIT-GIT-BOUNDARY` | cpf-tools release/open-git + unified CLI release namespace | `cpf-release/`는 Open Git 전달 전용 staging이며 Private CPF master의 Git/Source Identity에서 제외한다. `cpf release open-git`과 Tool/CLI/DevGPT/Codex는 Private master와 Open Git fresh clone 모두에서 사용자 승인 전 add/index staging/commit/push/branch/tag/reset/restore/stash/clean을 수행하지 않는다. Tool은 read-only status/diff/provenance와 Release 검증까지만 수행해 `VERIFIED`를 만들고, 사용자가 결과 확인 후 Open Git에서만 직접 commit/push한다. Private Working Tree가 dirty여도 임의 정리하지 않고 Current Source Identity를 기록한다. | private `/cpf-release/` tracked=0, private/Open-Git write-command negative, branch/HEAD/status/sourceIdentity capture, GENERATED→VERIFIED→USER_REVIEWED→GIT_COMMITTED→GIT_PUSHED state separation, wrong remote/dirty clone negative, user Git command generation-only, automatic add/commit/push=0 Evidence |
| `DEVEX-BOOTSTRAP` | cpf-tools local bootstrap | Local Bootstrap은 Windows/Linux thin wrapper가 공유 engine을 호출하는 개발환경 제품 기능이다. Git/Java25/container runtime과 필요한 경우 Node를 확인하고 selected DB start→actual health→migration→seed→capability middleware→domain discovery→build/test→runtime health를 수행한다. silent OS install/admin escalation/PATH mutation을 금지하고 stop과 reset을 분리하며 idempotent re-run/domain add-remove rediscovery를 지원한다. | Windows/Linux contract, prerequisite negative, selected DB lifecycle, timeout/progress, stop data preserve/reset explicit, add/remove rediscovery, idempotent rerun Evidence |
| `DEVEX-WINDOWS-PATH` | cpf-tools verification + repository path governance | Windows 개발환경 호환성을 위해 프로젝트 Root 상대경로와 파일명을 합친 전체 상대경로는 항상 200자를 초과하지 않는다. 장문 Evidence·Generated 경로는 안정적인 짧은 alias를 사용하고 원경로와 새 경로의 추적 Map을 보존한다. 200자 초과는 경고가 아니라 검증 실패이며 신규 Source·Evidence·Generator 출력에도 동일 Gate를 적용한다. | repository-wide relative path <=200, alias map, generated/evidence regression, Windows target-root projection, violation mutation Evidence |
| `DEVEX-DOCKER-LIFECYCLE` | cpf-tools local/full runtime verification | Local/Full Runtime 검증은 필요한 CPF Docker 서비스를 실행 전 스스로 확인하고 내려가 있으면 필요한 서비스만 자동 기동한다. running 상태만으로 성공 처리하지 않고 health와 실제 기능 readiness를 확인하며 Stage 간 서비스 lifecycle을 중앙 조정한다. 검증기가 시작한 컨테이너는 성공·실패 모두 종료 단계에서 정리하고 사용자가 이미 실행 중이던 컨테이너는 보존한다. DB3·Kafka·Redis·Messaging·Fault fixture는 실제 거래·장애·복구 검증에 필요한 lifecycle을 각 검증 흐름이 책임진다. | down-state auto-start, health+functional readiness, existing-container preserve, owned cleanup on failure, DB3/Kafka/fault runtime lifecycle Evidence |
| `EDU-CANONICAL` | cpf-education + capability owners | EDU는 개발 목적 기준 정확히 Online 20개 + Batch 15개 Canonical 기능 그룹만 유지한다. ADM/Backoffice/Gateway/OPS/Legacy/Compatibility/Micro Sample을 EDU에 병행하지 않고 유일 검증자산은 해당 Owner Test로 이동한 뒤 중복 sample을 제거한다. 모든 예제는 실제 Public API/Starter/DB/Header/Recovery 계약을 사용한다. | physical group count 20/15, legacy group 0, owner-test migration, compile/runtime, Public API/internal import gate, catalog/doc parity Evidence |
| `REL-DEV-WORKSPACE-GROUPING` | master canonical module/starter/artifact catalog + Gradle + Unified CLI + Open Git | 외부 개발 Workspace는 내부 Module 구조가 아니라 개발행위와 Public Capability 중심으로 제공한다. Canonical Capability taxonomy, exactly-one module role, 기능 중심 Gradle grouping, `cpf doctor/help/version`, Root build/test parity, Generated/Optional Domain 0/add/remove/recreate를 하나의 master Catalog projection으로 보장한다. 특정 Member/External/Backoffice 존재를 CLI/Gradle/Generator/Initializer/Publication/BOM/OpenAPI/Frontend/Test/Open Git이 하드코딩하지 않는다. | capability owner duplicate 0, internal public exposure 0, root build/test parity, doctor text/json/exit contract, domain mutation stale settings/dependency/task/BOM/publication/DB/OpenAPI/frontend/test/release 0, Open Git fresh projection Evidence |
| `CURRENT-ONLY-LEGACY-ZERO` | repository whole tree + Current Canonical governance | Repository는 현재 CPF Architecture만 해석 가능해야 한다. Retired active module/DB/route/config/seed/artifact/entrypoint와 날짜·세션별 Steering/Handover/Completion/Revalidation/RERUN/checkpoint/history를 Current Canonical에 병합하고 Consumer 0을 확인한 뒤 Delete Manifest로 제거한다. 실제 Upgrade/Recovery Consumer가 없는 legacy migration/provenance도 보존하지 않는다. 보호경로와 Current Evidence는 훼손하지 않는다. | current canonical duplicate 0, retired active module/DB/config 0, stale generator/initializer/Gradle/BOM/publication/OpenAPI/frontend/test/open-git 0, duplicate/dead script 0, dated/checkpoint current artifact 0, protected/current evidence loss 0 |

## 28. 금지되는 Current Target 표현

다음은 Current 제품 경로/identity로 사용하지 않는다.

- `cpf-biz-admin`, `cpf-biz-channel`, `cpf-biz-frontend`를 현재 Product Root로 복원
- `bzaDB`를 현재 Backoffice logical DB로 사용
- `X-Original-Channel`, `X-Current-Channel` 등을 Canonical System6 alias로 사용
- `callerChannel`을 Caller System authorization의 대체값으로 사용
- Generated Domain root vendor DB folder
- Generated Domain root 또는 생성 결과의 `cpf-domain.yaml`, `cpf-generator.lock.json`, 이름만 바꾼 Generator state/lock/manifest와 `.cpf/` 숨김 상태
- Generated Domain의 Internal Starter 직접 dependency
- Browser protected CPF Header authoring
- same-JVM self-HTTP
- `mavenLocal()`을 Public Release 성공 증거로 사용
- hostname 실패 시 `local/unknown/domainName` fabricated instanceId fallback
- Source 미발견 Operation 자동 삭제
- Provider/Capability를 미선택 Application에 강제 활성화

Immutable released DB migration의 과거 파일명처럼 기술적으로 변경하면 upgrade/checksum을 깨뜨리는 식별자는 예외적으로 보존할 수 있으나 Current Architecture identity로 재사용하지 않는다.

## 29. 최종 판단 원칙

CPF의 품질 기준은 “현재 Source와 문서가 서로 맞는다”에서 끝나지 않는다. **현재 Source가 이 Target을 실제로 충족하고, Developer가 쉽게 사용하며, 운영자가 안전하게 통제하고, QA가 실패·복구까지 재현할 수 있어야 한다.**

정본과 Source가 충돌하면 먼저 이 Target의 Architecture/사용성/상용 품질이 합리적인지 검토하고, 합리적이면 Source를 수정한다. 구현 편의를 위해 Target을 Source에 맞추는 것은 금지한다.


## 21.8 Developer Workspace / Capability / Domain Zero Dependency — Current Canonical

CPF의 모든 신규 Requirement/Steering/Architecture 변경은 Source 수정 전에 Current Canonical을 먼저 현행화하고 Requirement/Inventory/Acceptance를 확정한다. 이후 Source → Consumer → Generator/Initializer/DB/Frontend/Config → Test/Runtime → Evidence/Completion Review 순으로 닫는다. Source 구현 결과에 맞춰 Requirement를 사후 축소하거나 별도 날짜형 Steering을 추가하지 않는다.

외부 개발 Workspace는 master의 Canonical Module/Starter/Artifact Catalog를 그대로 Projection한다. Open Git 전용 복제 Catalog를 만들지 않는다. Canonical capability taxonomy는 Web, Persistence, Transaction, Security, Logging/Audit, Messaging, Integration, Cache, Batch, Observability, Config/Common 및 실제 Current Catalog에서 필요한 추가 Public capability를 포함한다. 각 Module은 Public Capability Group Owner, Internal Leaf/Foundation, Tooling, Generated/Customer Domain 중 exactly-one 역할을 가진다. Public BOM에는 Internal Leaf를 노출하지 않는다.

Gradle 개발자 UX는 Build, Test, Domain, Database, Runtime, Verification, Publication, Configuration/Discovery 기능 그룹을 사용한다. `cpf build`와 Root Canonical Gradle Build, `cpf test`와 Root Canonical Gradle Test는 동일한 Build Engine/결과를 사용한다. Open Git Root의 `./gradlew build`, `./gradlew test`, `./gradlew tasks`는 현재 선택 Module/Domain을 자동 계산하고 특정 Generated/Optional Domain 부재로 실패하지 않는다.

Unified Public CLI는 최소 `bootstrap/domain-new/domain-sync/build/test/run/stop/reset/status/help/doctor/version`을 제공한다. `cpf help`, `cpf doctor`, `cpf --version`은 Framework/CLI version, 현재 Domain, Capability, DB Vendor, Java/JDK, Build/Test/Run 기본 사용법과 prerequisite 상태를 제공하며 사람이 읽는 출력과 machine-readable JSON 계약, stable exit code, interactive/non-interactive를 지원한다. Internal command는 Public Release Capability에 물리적으로 노출하지 않는다.

Generated/Optional Domain은 0개, 일부 선택, 다수, Backoffice 선택/미선택, 신규 추가, 삭제, 삭제 후 재생성을 모두 정상 상태로 지원한다. Domain 부재는 `NOT_SELECTED/NOT_PRESENT`이며 필수 Platform Module 부재만 fail-fast한다. Domain mutation 후 settings/dependency/task/BOM/publication/DB/Migration/OpenAPI/Frontend/Test/Open Git artifact의 stale reference는 0이어야 한다. Generator는 Domain Catalog → settings/module registration → dependency/build/test target → publication → Open Git projection을 하나의 계약으로 동기화하고 여러 Gradle 파일을 사용자에게 수작업 등록시키지 않는다.

Open Git Release는 `<CPF_PROJECT_ROOT>/cpf-release/`에 Current Source에서 Fresh 생성하고 Current-only, Framework 구현 Source/sources.jar 기본 미포함, Customer/Generated/Backoffice/Sample/EDU 실제 Source 포함, Public CLI Binary 포함, Internal source/artifact/QA evidence/history/stale artifact 0을 강제한다. Release Tool은 자동 Git write를 수행하지 않고 VERIFIED까지만 만든다.

Physical Acceptance는 Java25 actual, Windows/Linux CLI lifecycle, Actual Open Git Fresh Release, Oracle/PostgreSQL/MariaDB DB3, Generated/Optional Domain mutation, Runtime/Fresh Replay, Source leakage 0, stale reference 0, current Source Identity Evidence까지 실제 PASS해야 한다.

## 21.9 Repository Current-only / Legacy Zero — Current Canonical

Repository 어디를 보더라도 현재 CPF Architecture만 해석 가능해야 한다. Current Runtime DB는 `cpfDB/mbwDB/mbrDB/exsDB`이며 `cpf-reference/refDB`, `BZA/cpf-bza/bzaDB`, 별도 Physical DB로서의 `cmnDB/admDB/batDB`, Retired Module/Route/DataSource/Seed/Config/Artifact/Entrypoint, 날짜·세션별 Canonical/Handover/Completion/Revalidation/RERUN/checkpoint/backup/과거 Release Snapshot은 현재 기능처럼 탐지·실행·생성되어서는 안 된다.

문자열만으로 삭제하지 않는다. Current Canonical에 고유정보를 병합하고 Consumer/reference 0을 확인한 뒤 Root-relative Delete Manifest로 제거한다. 현재 Upgrade/Recovery에 실제 필요한 immutable Migration/Rollback만 유지하며, 단순 과거 provenance/history 자체는 보존 사유가 아니다. 유지해야 하는 compatibility source도 Current Runtime/Schema/Config와 물리적으로 분리하여 current target으로 노출하지 않는다.

최종 Gate는 Current Canonical 중복 0, retired active module/DB/route/config 0, stale Generator/Initializer/Gradle/BOM/Publication/OpenAPI/Frontend/Test/Open Git reference 0, duplicate/dead script 0, 과거 Steering/Handover/Completion 잔재 0, Current Evidence 손실 0이다.
