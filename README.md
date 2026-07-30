<div align="center">

<img src="cpf-docs/assets/brand/cpf-hero.svg" alt="Core Platform Framework 소개" width="100%" />

# Core Platform Framework

**업무 애플리케이션의 개발부터 실행, 운영, 확장, 검증까지 하나의 구조로 연결합니다.**

Topology-independent Contract · Modular Monolith · Microservices · Operational Control · Generator-driven Domain

[개발자 안내](cpf-docs/guides/CPF_DEVELOPER_GUIDE.md)
· [Public API와 Generated Domain](cpf-docs/guides/CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md)
· [운영자 안내](cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md)
· [Generator](cpf-docs/guides/CPF_GENERATOR_TOOL_GUIDE.md)
· [Database Tool](cpf-docs/guides/CPF_DATABASE_TOOL_GUIDE.md)
· [Tool Reference](cpf-docs/guides/CPF_TOOL_REFERENCE.md)

</div>

---

## CPF를 한눈에 보기

CPF는 단순 공통 Library가 아니라 업무 Domain의 개발, 실행, 연계, 운영과 변경 관리를 일관된 Contract로 연결하는 **Core Platform Framework**입니다.

동일 JVM과 분리 Runtime에서 같은 업무 Public Contract를 유지하고, 업무 필요에 따라 Gateway, Batch, Messaging, File, External Integration과 운영 기능을 선택하여 조합합니다. 기능의 존재만을 완료로 간주하지 않고 Source, API, SQL, Test, Runtime과 Evidence가 같은 기준을 가리키도록 관리합니다.

<img src="cpf-docs/assets/architecture/cpf-product-overview.svg" alt="CPF Product Architecture" width="100%" />

### 핵심 가치

| Architecture | Reliable Execution | Operations | Extension & Delivery |
| --- | --- | --- | --- |
| Modular Monolith와 Microservices를 같은 Module 원칙으로 구성합니다. | Timeout, Retry, Circuit, Idempotency와 결과 불명 상태를 실행 흐름에 포함합니다. | ADM에서 Service, Instance, Transaction, Batch와 운영 명령을 추적합니다. | Generator로 Domain, DB, Test, Config와 Delivery Artifact를 함께 생성합니다. |
| 동일 JVM은 Local Invocation, 분리 Runtime은 Remote Invocation으로 연결합니다. | Async, External Integration과 Batch의 재처리·대사·보상 경계를 구분합니다. | 권한, 사유, 승인, 감사와 마스킹을 위험 조치에 연결합니다. | Oracle, PostgreSQL, MariaDB Vendor Pack과 Migration Lifecycle을 관리합니다. |
| Public API, SPI, Internal 구현과 Owner DB 경계를 분리합니다. | Multi-instance 환경에서 Lease, Claim, Fencing과 Checkpoint를 사용합니다. | Log, Trace, Timeline, Audit과 Evidence를 실행 식별자로 연결합니다. | Build, Static Gate, Runtime Test, Browser Test와 Evidence를 추적합니다. |

---

## Architecture Model

CPF Architecture는 **Framework Foundation**, **Application / Domain Plane**, **Optional Runtime & Edge**, **Tooling & Delivery**로 구분합니다.

<img src="cpf-docs/assets/architecture/cpf-module-ownership.svg" alt="CPF Module Ownership" width="100%" />

### 1. Framework Foundation

#### `cpf-core`

기술 기반과 topology-independent Public API·SPI의 Owner입니다.

- Standard Header, Transaction Identity, Error와 Result Contract
- Service Call, Runtime, Logging, Tracing, Messaging과 Integration 기반
- Public API / SPI / Internal Boundary
- 공통 기술 규약과 Auto Configuration

#### `cpf-common`

여러 Application이 필요에 따라 재사용하는 업무 공통 Module입니다.

- 업무 공통 Policy, Data Utility, Cache와 Metadata 지원
- Paging, Validation, Transform, Attachment 등 반복 업무 지원
- `cpf-core`를 기반으로 구성되며 중앙 호출 경유 계층이 아닙니다.
- Application 또는 Runtime은 필요한 Capability만 명시적으로 사용합니다.

> 현재 공식 Application과 일부 Batch Runtime은 `cpf-common`을 사용합니다. 그러나 Architecture상 모든 호출이 `cpf-common`을 경유하는 구조로 취급하지 않습니다.

### 2. Application / Domain Plane

`cpf-admin`, `cpf-biz-admin`, Generated Business Domain은 모두 CPF Foundation을 사용하는 독립 Application입니다. 각 Application은 자신의 API, Application Service, Domain Logic, Adapter와 Data Ownership을 가집니다.

| Application / Domain | System Code | 역할 |
| --- | --- | --- |
| `cpf-admin` | `ADM` | Platform Operations, Observability, Security Control, Approval과 Audit |
| `cpf-biz-admin` | `BZA` | 업무 관리자, User·Role·Permission, 조직·직원, 결재, 알림과 업무 운영 |
| `cpf-member` | `MBR` | Generator Golden Reference Instance로 관리되는 생성형 업무 Domain |
| `cpf-<domain>` | 3자리 System Code | Generator로 생성하는 독립 업무 Domain |

ADM과 BZA는 일반 고객 Domain과 목적은 다르지만 Runtime Architecture에서는 같은 Application / Domain Plane에 위치합니다. ADM은 다른 Owner의 DB를 직접 수정하지 않고 Public Operations Contract를 통해 조회와 제어를 수행합니다.

### 3. Optional Runtime & Edge

#### `cpf-gateway`

공통 진입 제어가 필요한 환경에서 선택하는 Edge Adapter입니다.

- Service Registry 기반 Server Group, Route와 Environment Binding
- HTTP/HTTPS, gRPC, TCP와 Same-JVM Local Target 선택
- Round Robin, Weight, Rendezvous Hash, Priority Failover와 Least Load
- Active/Passive Health, Hysteresis, Drain, Maintenance와 Fail-closed 안전 상한
- Connection Test, Versioned Apply, Instance ACK와 Configuration Drift 추적
- Transaction Timeline과 Retry·Failover Attempt 추적
- Authentication Bridge, Standard Header, Rate, Timeout과 Failure Isolation
- 업무 Domain 직접 진입과 함께 선택 가능

Gateway는 필수 진입점이 아닙니다. 각 업무 Domain 또는 Channel이 자체 Endpoint와 등록 정책을 소유하면 Gateway 없이 직접 호출할 수 있습니다. 또한 내부 Domain 간 호출을 불필요하게 Gateway로 재경유시키지 않습니다.

#### `cpf-batch`

업무 Domain이 제공한 Batch Contract를 실행하는 독립 Runtime 제품 영역입니다.

- Versioned Job Definition, Typed Parameter, Dependency와 승인·배포 이력
- Control Server와 Runtime Query/Command
- Scheduler, Calendar와 Trigger
- Worker와 Restartable File Watch
- Approved Shell Artifact, Hash/Signature와 Process Tree Control
- Center-Cut Runner와 Host Agent
- Runtime Common, Contract와 Testkit

Batch는 `cpf-common`의 하위 기능이 아니며 Scheduler, Worker, Center-Cut와 Remote Agent 책임을 별도 Runtime으로 구성합니다.

### 4. Tooling & Delivery

`cpf-tools`는 Generator, Database Lifecycle, Runtime Assembly, Quality Gate, Artifact Supply와 Evidence 도구를 소유합니다.

- Domain Generator와 Repository Federation
- Official DB Vendor Pack 생성·동기화·검증
- Local / Offline / Remote Artifact Federation
- Runtime Start, Stop, Status와 Diagnostics
- Architecture, Security, SQL, Frontend, Evidence Gate
- Release, Migration, Upgrade, Rollback과 DR 지원

---

## Runtime과 Deployment Topology

CPF는 특정 Deployment 방식에 고정되지 않습니다. 직접 진입, 선택 Gateway, Same JVM, Separate Runtime과 Hybrid Deployment를 같은 Contract 기준으로 조합합니다.

<img src="cpf-docs/assets/architecture/cpf-runtime-topology.svg" alt="CPF Runtime and Deployment Topology" width="100%" />

### Direct Entry와 Optional Gateway

```text
Direct Entry
Client / Channel ───────────────→ Business Domain

Optional Gateway
Client / Channel → cpf-gateway → Business Domain
```

업무 Module 자체 또는 Channel이 Routing, 인증, Endpoint 등록을 소유하면 Direct Entry를 사용합니다. 여러 Channel의 공통 진입 정책, Header 정규화와 장애 격리가 필요하면 `cpf-gateway`를 선택합니다.

### Local과 Remote Invocation

```text
Same JVM
Domain A → Local Adapter → Domain B Public Contract

Separate Runtime
Domain A → Remote Adapter → Domain B Public API
```

호출 방식이 달라도 업무 Public Contract, Header, Error, Transaction Identity와 Trace 기준은 가능한 한 동일하게 유지합니다.

### Runtime Packaging

- Embedded BootJar
- External WAS / WAR
- Modular Monolith
- Microservices
- Local Development Assembly
- Hybrid Deployment Cell

`cpf-local-runtime`과 `cpf-local-batch-runtime`은 제품 Module과 개발용 Runtime 조립체를 분리하여 로컬 통합 실행을 지원합니다.

---

## Module Catalog

### Official Product Modules

| Module | 분류 | 주요 책임 |
| --- | --- | --- |
| `cpf-core` | Foundation | 기술 공통 Contract, Runtime 기반, Public API·SPI |
| `cpf-common` | Reusable Common | 여러 Application에서 선택적으로 사용하는 업무 공통 Capability |
| `cpf-admin` | Official Application | 운영·관제·보안·감사·통제 Application |
| `cpf-biz-admin` | Official Application | 업무 관리자와 업무 운영 Application |
| `cpf-gateway` | Optional Edge | 외부 진입, Routing, Header와 인증 연계 |
| `cpf-reference` | Reference / EDU | 제품 API 사용 예제와 교육 Reference |

### Batch Product Modules

| Gradle Project | Artifact 역할 |
| --- | --- |
| `:cpf-batch:contract` | Batch Public Contract |
| `:cpf-batch:runtime-common` | Batch Runtime 공통 기반 |
| `:cpf-batch:control-server` | Control Plane API와 Runtime State |
| `:cpf-batch:scheduler` | Trigger, Calendar, Lease와 Scheduling |
| `:cpf-batch:worker` | Spring Batch Job, Step, Chunk와 Tasklet 실행 |
| `:cpf-batch:center-cut-runner` | Partition, Claim, Lease와 대량 분산 처리 |
| `:cpf-batch:host-agent` | Remote Host Process와 Artifact 실행 |
| `:cpf-batch:testkit` | Contract와 Runtime Scenario Fixture |

### Runtime Assembly와 Build Tooling

| Project | 위치 | 역할 |
| --- | --- | --- |
| `cpf-local-runtime` | `cpf-tools/runtime/cpf-local-runtime` | 온라인 Application 로컬 통합 Runtime |
| `cpf-local-batch-runtime` | `cpf-tools/runtime/cpf-local-batch-runtime` | Batch 로컬 통합 Runtime |
| Platform BOM | `cpf-tools/build/platform-bom` | Published Dependency BOM |
| Convention Plugin | `cpf-tools/build/gradle-plugin` | Module과 Generated Domain Build Convention |
| Local Domain Federation | `local-domains/*` | 독립 Domain Repository의 개발용 Composite Build Mount |

---

## Transaction과 Service Call

CPF의 온라인 실행 흐름은 Transaction Identity와 Standard Header를 중심으로 연결됩니다.

```text
Inbound Request
→ Header Validation / Context
→ Transaction Identity
→ Application Service
→ Local or Remote Invocation
→ Result / Error Mapping
→ Log / Trace / Audit
```

### Service Call Capability

- Local / Remote Invocation Abstraction
- Service Registry, Server Group과 Instance Selection
- Gateway Binding, Health와 Configuration Apply ACK
- Timeout, Retry, Backoff와 Circuit State
- Multi-instance Failover
- Distributed Trace Context 전달
- Error Translation과 External Message Mapping

### Idempotency와 결과 불명 상태

동일 요청이 반복될 수 있는 흐름에는 Idempotency Key와 처리 이력을 적용합니다. Timeout 이후 상대 처리 결과를 즉시 확정할 수 없으면 단순 실패로 덮지 않고 `UNKNOWN_RESULT` 또는 이에 상응하는 미확정 상태로 보존해 Reconciliation, 상태 확인, 재처리 또는 Compensation으로 연결합니다.

Recovery와 Reprocessing은 별도 중앙 Module이 아닙니다.

- Transaction: Idempotency, Retry, Unknown Result Resolution, Compensation
- Async: Retry, DLQ, Replay, Idempotent Consumer
- External Integration: Timeout, Result Inquiry, Resend와 전문 추적
- Batch: Restart, Checkpoint, Lost Execution Reconciliation
- ADM: Query, Permission, Reason, Approval, Command와 Audit

---

## Async, Messaging과 External Integration

### Async Processing

- Transactional Outbox
- Inbox와 Idempotent Consumer
- DLQ와 Replay
- Retry / Backoff / Poison Message 분리
- 처리 상태와 Correlation Trace

### Messaging과 File

- Kafka와 AMQP 기반 Messaging Adapter
- File Transfer와 Attachment Lifecycle
- Fixed-length Message, 전문 Encoding과 Validation
- Scan, Quarantine와 Download Control
- External Result Mapping과 오류 추적

### External Integration Ownership

HTTP, Messaging, File, 전문과 Trace 같은 기술 기반은 Framework가 제공할 수 있지만 실제 기관·서비스별 업무 연계 책임은 해당 Owner Domain이 가집니다.

```text
연계 Owner Domain
→ Request / Response Mapping
→ Business Validation
→ Timeout / Result Inquiry / Retry Policy
→ Integration History / Audit
→ External System
```


---

## Batch와 Center-Cut

<img src="cpf-docs/assets/architecture/cpf-batch-runtime.svg" alt="CPF Batch Runtime Architecture" width="100%" />

### Control Plane

- JobPack Registry와 Versioned Job Definition
- 공통 `CpfParameterSchema` 기반 Typed Parameter와 Secret Reference
- Runtime Command와 Query
- Scheduler, Calendar와 Trigger
- Instance State와 Deployment Control
- Lost Execution과 Reconciliation

### Execution Plane

- Worker 기반 Spring Batch Job / Step 실행
- Center-Cut Partition, Target, Claim과 Runner
- Host Agent 기반 Remote Process 실행
- File Stability Window, Marker, Checksum, Claim과 Restart Scan
- Approved Script Hash/Signature, Parameter File, Output Masking과 Process Tree 종료
- Checkpoint와 Restartability
- Lease와 Fencing Token 기반 Owner 보호

### Multi-instance Safety

동일 작업을 여러 Instance가 경쟁할 수 있는 환경에서 실행 Owner, Lease, Claim과 Fencing을 구분합니다. 이전 Owner가 Lease를 잃은 뒤 다시 결과를 반영하지 않도록 실행 세대와 소유권 검증을 사용합니다.

ADM은 Batch Runtime 상태를 조회하고 승인된 Command를 전달할 수 있지만 BAT Owner가 관리하는 Runtime DB를 직접 수정하지 않습니다.

상세 내용은 [Batch Runtime과 Remote Agent](cpf-docs/guides/CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md), [Scheduler와 Instance Lifecycle](cpf-docs/guides/CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md)을 확인합니다.

---

## Operations와 Business Administration

### `cpf-admin` — Operations Domain

`cpf-admin`은 Platform Runtime을 운영하고 통제하는 독립 Application입니다.

- Service Registry, Server Group, Gateway Binding과 Instance Health
- Gateway Connection Test, Apply ACK, Transaction/Attempt Timeline
- Transaction, Timeline, Log와 Trace 조회
- Batch Job Definition, Center-Cut와 Runtime Control
- Cache, Dynamic Log Level과 Configuration Control
- Secret Metadata와 Security Operation
- 승인, Break-glass, Reason과 Audit
- Data Safety, Download와 Masking
- Observability Dashboard와 Structured Details

위험한 운영 명령은 대상 확인, 인증된 Operator, Permission, 사유, 필요 시 승인과 실행 결과 Audit으로 연결합니다. 운영 기능의 실패가 원 업무 Transaction을 오염시키지 않도록 책임과 Transaction 경계를 분리합니다.

### `cpf-biz-admin` — Business Administration Domain

`cpf-biz-admin`은 업무 운영을 위한 독립 Application입니다.

- User, Role, Permission과 유효기간
- Organization, Employee, Position과 Assignment
- Approval Workflow
- Notification과 Read State
- Attachment와 Scan State
- Audit Hash Chain
- Tenant-aware Business Administration

ADM과 BZA의 Frontend는 기능별 Route, State, API와 Component 책임을 분리하고 외부 Runtime CDN이나 Font에 의존하지 않는 제품 구조를 지향합니다.

---

## Security, Governance와 Observability

<img src="cpf-docs/assets/architecture/cpf-capability-map.svg" alt="CPF Capability Map" width="100%" />

### Security Boundary

- Authentication과 authenticated Operator Context
- Role / Permission 기반 Authorization
- Tenant Context와 Data Boundary
- Secret Reference와 Provider SPI
- Sensitive Data Masking
- Secure Default와 Break-glass Control

### Audit와 Traceability

- Transaction ID와 실행 Segment
- System / Instance / Job / Execution Identity
- File Log와 DB Log
- Distributed Trace와 Timeline
- Operator Command Audit
- Audit Hash Chain과 Tamper Detection

### Retention과 Data Safety

- Retention Policy
- Legal Hold
- Archive와 Purge
- Backup Manifest와 SHA-256
- Restore와 DR Verification
- Download, Attachment Scan과 Quarantine

---

## Database와 Lifecycle Management

CPF의 공식 지원 Database Vendor는 다음 3종입니다.

| Vendor | 식별자 | 지원 범위 |
| --- | --- | --- |
| Oracle | `oracle` | Generated Domain, Platform SQL, Migration과 Vendor Pack |
| PostgreSQL | `postgresql` | Generated Domain, Platform SQL, Migration과 Vendor Pack |
| MariaDB | `mariadb` | Generated Domain, Platform SQL, Migration과 Vendor Pack |

MySQL, MSSQL과 H2는 공식 지원 Vendor로 두지 않습니다.

### Canonical Source와 Vendor Parity

- DB Artifact는 Canonical Source와 Generator를 우선합니다.
- Vendor별 Install, Seed, Migration, Verify와 Rollback 구조를 동일한 원칙으로 관리합니다.
- 존재하지 않는 Column을 참조하는 Index·FK와 SQL 구조 오류를 Runtime 전에 검출합니다.
- 기존 Schema와 정본이 다르면 조용히 Skip하지 않고 Drift 또는 Migration 문제로 처리합니다.
- Runtime Query Pack과 Application Artifact의 Vendor Resource 포함 여부를 검증합니다.

### Lifecycle

```text
Install
→ Verify
→ Migration / Upgrade
→ Compatibility Check
→ Rollback Plan
→ Backup / Restore
→ DR Verification
```

자세한 명령과 Profile은 [Database Tool Guide](cpf-docs/guides/CPF_DATABASE_TOOL_GUIDE.md), [Database Profile and Domain DB Guide](cpf-docs/guides/DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md)를 확인합니다.

---

## Generator-driven Domain Extension

<img src="cpf-docs/assets/architecture/cpf-domain-extension.svg" alt="CPF Generated Domain Extension" width="100%" />

Generator는 `DomainName`과 3자리 대문자 `SystemCode`를 입력받아 독립 업무 Domain을 생성합니다.

### 생성 범위

- Module과 Package
- API / Application / Domain / Adapter Layer
- Standard Header와 Error Contract 적용
- Config와 Runtime Profile
- Oracle, PostgreSQL, MariaDB DB Artifact
- Install, Seed, Migration, Verify와 Rollback
- Unit, Integration과 Runtime Test
- OpenAPI, JavaDoc, Guide와 Evidence 연결
- Service Registry 등록 후보, Endpoint와 Health Metadata
- Gateway 외부 공개 기본 거부와 승인·연결시험 정책
- CI/CD와 Repository Federation 설정

### Preflight Validation

- Module, Package와 SystemCode 충돌
- Route와 Standard Execution ID 충돌
- DB Name, Schema와 Table Prefix 충돌
- Config Key와 Port 충돌
- 기존 사용자 수정 영역 침범 여부
- Official Vendor Pack과 Golden Template Parity

`cpf-member`는 Generator Golden Reference Instance입니다. ACC와 신규 업무 Domain도 같은 Template와 품질 기준을 사용합니다.

---

## Technology Stack

기술 Stack의 단일 정본은 `gradle/cpf-stack.properties`입니다.

| 영역 | 기준 |
| --- | --- |
| Java | 25 |
| Gradle | 9.1.0 |
| Spring Boot | 4.1.0 |
| Servlet | 6.1 |
| API | Spring MVC / WebFlux / REST Client / OpenAPI |
| Data | JDBC / MyBatis / Flyway |
| Messaging | Kafka / AMQP |
| Observability | OpenTelemetry / Structured Log / Runtime Metrics |
| Frontend | Vue 기반 ADM·BZA Feature Architecture |
| Database | Oracle / PostgreSQL / MariaDB |

Stack version은 README에 개별적으로 중복 관리하지 않고 정본 Property와 Gate를 기준으로 유지합니다.

---

## 빠른 시작

### 준비 조건

- JDK 25
- Git
- Gradle Wrapper
- PowerShell 7 — Generator, DB와 Runtime Tool 사용 시
- Node.js와 npm — ADM·BZA Frontend 개발 시
- 선택한 공식 Database Vendor Runtime

### 전체 Build

Linux / macOS:

```bash
./gradlew clean build
```

Windows:

```powershell
.\gradlew.bat clean build
```

### Local Runtime

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

분산 Batch Runtime은 다음 Script를 사용합니다.

```powershell
pwsh -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
pwsh -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
```

### 신규 Domain Dry Run

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName "payment" `
  -SystemCode "PAY" `
  -DatabaseVendor "mariadb" `
  -DryRun
```

### Frontend 검증

```bash
cd cpf-admin/frontend
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

`cpf-biz-admin/frontend`도 같은 방식으로 검증합니다.

### 대표 Quality Gate

```powershell
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1
```

외부 Runtime, Database, Redis, Messaging 또는 Browser가 필요한 검증은 해당 Profile과 환경을 준비한 뒤 실행하고 Evidence를 함께 보존합니다.

---

## Documentation Map

README는 제품의 시작점입니다. 상세 Contract와 실행 절차는 역할별 정본에서 이어집니다.

| 주제 | 문서 |
| --- | --- |
| Developer Guide | [CPF_DEVELOPER_GUIDE.md](cpf-docs/guides/CPF_DEVELOPER_GUIDE.md) |
| Foundation API | [CPF_FOUNDATION_API_GUIDE.md](cpf-docs/guides/CPF_FOUNDATION_API_GUIDE.md) |
| Public API와 Generated Domain | [CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md](cpf-docs/guides/CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md) |
| Generator | [CPF_GENERATOR_TOOL_GUIDE.md](cpf-docs/guides/CPF_GENERATOR_TOOL_GUIDE.md) |
| ADM Operator | [CPF_ADMIN_OPERATOR_GUIDE.md](cpf-docs/guides/CPF_ADMIN_OPERATOR_GUIDE.md) |
| BZA | [CPF_BIZ_ADMIN_GUIDE.md](cpf-docs/guides/CPF_BIZ_ADMIN_GUIDE.md) |
| ADM / BZA UI Standard | [CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md](cpf-docs/guides/CPF_ADMIN_BZA_UI_STANDARD_GUIDE.md) |
| Batch Runtime | [CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md](cpf-docs/guides/CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md) |
| Batch Scheduler Lifecycle | [CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md](cpf-docs/guides/CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md) |
| Database Tool | [CPF_DATABASE_TOOL_GUIDE.md](cpf-docs/guides/CPF_DATABASE_TOOL_GUIDE.md) |
| Database Profile | [DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md](cpf-docs/guides/DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md) |
| Health와 Registry | [CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md](cpf-docs/guides/CPF_HEALTH_AND_SERVICE_REGISTRY_GUIDE.md) |
| Security, DR와 Retention | [CPF_SECURITY_DR_RETENTION_GUIDE.md](cpf-docs/guides/CPF_SECURITY_DR_RETENTION_GUIDE.md) |
| Artifact Supply와 CI/CD | [CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md](cpf-docs/guides/CPF_ARTIFACT_SUPPLY_AND_CICD_GUIDE.md) |
| Tool Overview | [CPF_TOOLS_GUIDE.md](cpf-docs/guides/CPF_TOOLS_GUIDE.md) |
| Tool Reference | [CPF_TOOL_REFERENCE.md](cpf-docs/guides/CPF_TOOL_REFERENCE.md) |
| EDU Coverage | [CPF_EDU_COVERAGE_GUIDE.md](cpf-docs/guides/CPF_EDU_COVERAGE_GUIDE.md) |

---

## Verification과 Evidence

CPF의 완료 판정은 Source나 Package 존재만으로 이루어지지 않습니다.

```text
Requirement
→ Source / API / SQL / Config
→ Unit / Integration / Runtime / Browser Test
→ Execution Result
→ Sanitized Evidence
```

### 함께 확인하는 범위

- Module Ownership과 Public API·SPI Boundary
- 정상, 오류, 경계와 부분 실패
- Timeout, Retry, Idempotency와 Unknown Result
- Concurrency, Multi-instance, Lease와 Fencing
- Security, Authorization, Masking, Approval과 Audit
- SQL, Vendor Pack, Migration, Upgrade와 Rollback
- Generator와 Generated Domain Parity
- Frontend Route, API, State와 Runtime Integration
- Artifact Packaging과 Deployment Topology
- 현재 Commit과 Evidence의 유효성

직접 실행하지 않은 검증은 성공으로 기록하지 않습니다. 환경이 없어 실행하지 못한 항목은 `미검증`으로 구분하고, 실행 명령, Profile, 기준 Commit과 필요한 환경을 Evidence 계획에 남깁니다.

---

## Contribution Principles

CPF에 기능을 추가하거나 변경할 때는 다음 순서로 판단합니다.

1. 해결하는 Requirement와 Owner Module을 확인합니다.
2. Public API, SPI와 Internal 구현 경계를 구분합니다.
3. 실제 Consumer와 의존성 방향을 확인합니다.
4. Same JVM과 Separate Runtime 양쪽의 Contract를 검토합니다.
5. Multi-instance, Partial Failure와 Recovery 영향을 확인합니다.
6. Security, Audit와 Operations Control 필요 여부를 확인합니다.
7. DB Vendor, Migration, Generator와 Generated Domain 영향을 검토합니다.
8. Source, Test, Guide와 Evidence를 같은 변경 단위로 유지합니다.
9. Repository Hygiene와 기존 성공 기능의 회귀를 확인합니다.

> CPF는 기능 목록이 아니라 일관된 제품 구조를 유지하는 Framework입니다. 새로운 추상화는 실제 Consumer와 기본 구현, 운영·복구 경로와 검증 근거를 함께 가져야 합니다.
