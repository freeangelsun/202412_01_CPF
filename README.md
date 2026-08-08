<div align="center">

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-hero-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-hero-desktop.png" alt="Core Platform Framework - Business Platform for Build, Run and Recover" width="100%">
</picture>

**Core Platform Framework(CPF)**는 온라인·비동기·배치·외부연계·관리자 운영·보안·복구를 하나의 제품 계약으로 연결하는 Business Platform Framework입니다.

[프레임워크 안내](cpf-docs/guides/00_프레임워크안내.pdf) · [개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.pdf) · [배치 개발](cpf-docs/guides/02_배치개발매뉴얼.pdf) · [ADM 연동 개발](cpf-docs/guides/03_ADM개발자매뉴얼.pdf) · [ADM 운영](cpf-docs/guides/04_ADM운영자매뉴얼.pdf) · [플랫폼 운영](cpf-docs/guides/05_플랫폼운영매뉴얼.pdf)  
[BZA](cpf-docs/guides/90_BZA매뉴얼.pdf) · [Gateway](cpf-docs/guides/91_Gateway매뉴얼.pdf) · [아키텍처 설계](cpf-docs/deliverables/아키텍처설계서.pdf) · [기술 사양](cpf-docs/deliverables/기술사양서.pdf) · [기술 표준](cpf-docs/deliverables/기술표준서.pdf) · [DB 표준](cpf-docs/deliverables/데이터베이스표준서.pdf)

</div>

> **문서 기준** · Repository `freeangelsun/202412_01_CPF` · branch `master` · 최신 검토 HEAD `64fd08d963927860e8d023403dfa276931801ee5` (`07_17`) · Canonical Requirement **186개**.  
> **현행화 Source 기준** · 정본이 지정한 `4c4248a12e699c07f9f5fb11fbb33b97ca04077d` (`07_16`). `07_17`은 Core Kernel·Capability Ownership을 재확정한 Governance 변경이며, 문서는 **CURRENT SOURCE / PRODUCT CONTRACT / REFERENCE**를 구분합니다.

---

## CPF가 해결하는 것

CPF의 중심은 특정 라이브러리 묶음이 아니라 **업무 계약과 운영 계약의 일치**입니다. 동일 업무 기능은 Modular Monolith의 Local Facade와 분리 WAS/Microservice의 Remote Facade에서 동일한 요청·검증·오류·권한·idempotency·audit 의미를 유지합니다. transactionId는 Channel에서 시작해 DB, 외부 API, Message, Batch, File, Retry, UNKNOWN_RESULT, Reconcile과 ADM Timeline까지 이어집니다.

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-architecture-overview-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-architecture-overview-desktop.png" alt="CPF System Architecture" width="100%">
</picture>

| 구축 관점 | CPF가 제공하는 기준 |
|---|---|
| **업무 개발** | Generator → API/Application/Domain/Persistence → Local/Remote → Test → 운영 인계 |
| **거래 일관성** | LOCAL · XA/JTA · OUTBOX · INBOX_DEDUP · SAGA · TCC · UNKNOWN_RESULT/Reconcile |
| **데이터·연계** | JDBC/MyBatis/JPA · REST/SOAP · Kafka/JMS/RabbitMQ · File/Attachment · Object Storage |
| **보안** | OAuth2/OIDC/JWT · Session · KMS/HSM · Digital Signature · Masking · Approval · Audit |
| **운영** | ADM Control Plane · Health · Log/Metric/Trace · Batch/Worker · Backup/Restore · DR |
| **확장** | Public API/SPI · Leaf Starter · Capability Profile · Generated Domain · Testkit/EDU |

---

## 07_17 Ownership 기준

CPF는 **“Provider-neutral이면 Core”** 규칙을 사용하지 않습니다. `cpf-core`는 전 제품이 공유하는 최소 Kernel만 소유하고, Paging/Persistence·Header/Web·File·AI·Batch·Gateway·Fixed-Length·Notification·Webhook 같은 전용 계약과 Runtime은 실제 Capability/Owner/Provider/Starter가 소유합니다.

- Fixed-Length 전용 Contract/Engine: `cpf-starters/integration/fixedlength-core`
- Fixed-Length Spring Runtime/AutoConfiguration: `cpf-starters/integration/fixedlength`
- Logging/Observability Runtime: `platform-operations/observability` 계열 Capability
- Dynamic Log Level·Log Policy·Remote Log/Search/Bundle: Platform Operations Capability + ADM Control Plane
- CURRENT SOURCE에 아직 Core 아래 남아 있는 전용 API는 **현행 구현 위치**로 표시하되, PRODUCT CONTRACT의 목표 Owner와 혼동하지 않습니다.

## 제품 구성과 책임 경계

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-product-map-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-product-map-desktop.png" alt="CPF Product Map" width="100%">
</picture>

| 영역 | Owner | 핵심 책임 |
|---|---|---|
| 전역 Kernel | `cpf-core` | Transaction/Execution Context, Error/Result/Validation, Security/Tenant/Identity, UNKNOWN/Reconcile/Idempotency/Deadline 등 CPF 전역 Semantics와 최소 Value/Pure Logic |
| 선택 Runtime | `cpf-starters` | data / messaging / integration / file / notification / security / platform-operations Capability |
| 업무 공통 | `cpf-common` | 고객 공통 정책, Core SPI 확장, DB-less 기본 사용 |
| 업무 Domain | Generated Domain / `cpf-member` | API, Application, Domain, Repository, Adapter, Migration, Test |
| 운영 Control Plane | `cpf-admin` | 상태·거래·로그·설정·Batch·Recovery·Approval·Audit |
| 업무 관리 | `cpf-biz-admin` | 조직·사용자·Role·Permission·Data Scope·결재 |
| 외부 진입 | `cpf-gateway` | trust boundary, route, auth, target, resilience, publish/LKG |
| Batch Runtime | `cpf-batch` | Spring Batch, Scheduler, Center-Cut, Agent, Runner, Worker |
| 도구·배포 | `cpf-tools` | Generator, BOM, DB Pack, Migration, Verification, Release |
| 교육·복구 Reference | `cpf-reference` | 실제 Public Contract를 사용하는 실행형 EDU·fault/recovery 예제 |

---

## 같은 업무 계약, 여러 배포 Topology

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-topology-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-topology-desktop.png" alt="CPF Deployment Topology" width="100%">
</picture>

CPF는 Embedded Boot JAR, External WAS WAR, Modular Monolith, 독립 Microservice, 독립 Static Web, Gateway Runtime, Scheduler/Agent/Runner/Worker Process, Multi-instance/Multi-zone, Rolling/Canary/Blue-Green, Backup/Restore와 DR Topology를 제품 계약의 범위로 둡니다. Topology가 달라져도 업무 DTO, validation, transactionId, authN/authZ, timeout budget, error taxonomy, audit와 recovery semantics를 다시 설계하지 않습니다.

---

## 거래는 성공뿐 아니라 실패와 복구까지 설계합니다

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-execution-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-execution-desktop.png" alt="CPF Execution and Transaction Lineage" width="100%">
</picture>

CPF의 거래 모델은 `SUCCESS / FAILED / CONFLICT / UNKNOWN / RECONCILED`를 구분합니다. 하위 시스템이 Commit한 뒤 응답이 유실되면 상위 Transaction Rollback만으로 하위 Side Effect가 사라졌다고 가정하지 않습니다. idempotency record, provider request ID, target result query, business data, event/ledger를 대조한 뒤 Retry/Reprocess/Reconcile/Compensation을 선택합니다.

---

## 운영은 “다시 기동”이 아니라 “업무 정상화”까지

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-operations-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-operations-desktop.png" alt="CPF Operations and Recovery" width="100%">
</picture>

운영 절차의 종료 조건은 프로세스가 UP이 된 시점이 아닙니다. DB/Message/File/External side effect, UNKNOWN_RESULT, duplicate/omission, backlog, security/audit와 business probe를 확인해 **관측 → 상태 분류 → 승인된 조치 → 대사/복구 → 정상화 판정**으로 닫습니다.

---

## 신규 업무가 제품 운영으로 넘어가는 경로

<picture>
  <source media="(max-width: 720px)" srcset="cpf-docs/assets/readme/cpf-domain-journey-mobile.png">
  <img src="cpf-docs/assets/readme/cpf-domain-journey-desktop.png" alt="CPF Domain Journey" width="100%">
</picture>

Generator가 만든 Domain은 코드 Skeleton으로 끝나지 않습니다. API·DB·Transaction·Integration·Security·OpenAPI·JavaDoc·Test·Fault Injection·ADM 확인·배포 인계까지 같은 변경 단위로 관리합니다. Product Contract가 바뀌면 Source, SQL, API, Config, Frontend, Script, Test와 Manual을 함께 현행화합니다.

---

## 07_16에서 확장된 제품 목표

Canonical Requirement는 **186개**이며, `07_16`에서 다음 6개 Capability가 정본에 추가되었습니다.

| 신규 Requirement | 제품 목표 |
|---|---|
| `FOUNDATION-UTILITY` | Core를 Utility 창고로 만들지 않고 Pure Foundation과 Capability Owner로 재분류 |
| `SEC-SESSION-DIST` | JDBC와 선택형 Valkey Distributed Session Provider, rotation/forced logout/multi-instance |
| `FILE-OBJECT-STORAGE` | Attachment API와 연결되는 S3-compatible Object Storage, multipart/checksum/range/reconcile |
| `EVENT-SCHEMA` | Broker와 독립적인 Event Schema version/compatibility/breaking-change gate |
| `API-GRAPHQL` | Optional GraphQL BFF, Service 재사용, field auth, depth/complexity, DataLoader |
| `API-REALTIME` | SSE 우선 Realtime, reconnect/duplicate/backpressure/multi-instance/fallback polling |

이 Capability는 새 Starter를 무조건 활성화하는 의미가 아닙니다. 선택하지 않은 Application에서 dependency/bean/config/sql/thread/endpoint/background side effect가 0이어야 합니다.

---

## 가장 짧은 시작 경로

**1. 제품 범위를 먼저 판단**  
[00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.pdf)에서 Topology·Module Owner·Starter/Profile·도입 범위를 결정합니다.

**2. 개발자는 실행형 예제로 시작**  
[01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.pdf)의 Generator → 업무 Domain → DB → Transaction → Integration → Security → Test/Fault → ADM 확인 흐름을 따라갑니다.

**3. 배치는 별도 실행·복구 모델로 개발**  
[02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.pdf)에서 Spring Batch Job/Step/Chunk, Metadata/Checkpoint, Partition/Worker, Stop/Restart/Reprocess/Reconcile을 구현합니다.

**4. 운영 인계 전에 Control Plane을 확인**  
[03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.pdf)로 Owner Query/Command를 연결하고, [04 ADM 운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.pdf)과 [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.pdf)에서 운영 절차를 검증합니다.

---

## 공식 사용자 문서

| 문서 | 대상 | 문서만으로 끝내야 하는 일 |
|---|---|---|
| [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.pdf) | 아키텍트·리드 | 제품 범위, Module/Starter, Topology, 도입 순서 결정 |
| [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.pdf) | Backend/Integration 개발자 | Generator부터 API/DB/Transaction/Integration/Security/Test/배포 인계 |
| [02 배치개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.pdf) | Batch 개발자 | Job/Step/Chunk/Partition/Worker 개발과 Restart/Reprocess/Reconcile |
| [03 ADM개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.pdf) | ADM 연동 개발자 | Owner Query/Command → OpenAPI → Generated Client → Vue 화면 연결 |
| [04 ADM운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.pdf) | 플랫폼 운영자 | 실제 Route별 조회·판단·조치·승인·복구·감사 |
| [05 플랫폼운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.pdf) | 플랫폼/SRE/DBA | 설치·Config·DB·Broker·배포·관측·Backup/Restore·Upgrade/Rollback·DR |
| [90 BZA 매뉴얼](cpf-docs/guides/90_BZA매뉴얼.pdf) | 업무 관리자 | 조직·직원·Role·Permission·Data Scope·결재·Session·Audit 운영 |
| [91 Gateway 매뉴얼](cpf-docs/guides/91_Gateway매뉴얼.pdf) | Gateway 운영자 | Route/Target/Security/Publish/LKG/Rollback/Scale-out/Drift 운영 |

설계·인수 문서: [아키텍처설계서](cpf-docs/deliverables/아키텍처설계서.pdf) · [기술사양서](cpf-docs/deliverables/기술사양서.pdf) · [기술표준서](cpf-docs/deliverables/기술표준서.pdf) · [데이터베이스표준서](cpf-docs/deliverables/데이터베이스표준서.pdf) · [산출물목록](cpf-docs/deliverables/산출물목록.pdf)

---

## 문서에서 사실을 읽는 법

- **CURRENT SOURCE** — 정본이 지정한 currentization source `4c4248a12e69` (`07_16`)에서 확인한 실제 Repository 경로·Symbol·Route·Property·SQL·Test. `07_17` Governance 변경은 구현 Symbol이 아니라 Ownership/Product Contract를 갱신합니다.
- **PRODUCT CONTRACT** — CPF 제품이 제공해야 하는 선택·실행·실패·복구 계약. 현재 구현량을 이유로 축소하지 않습니다.
- **REFERENCE** — 사용자가 복사·응용할 수 있는 완성 기준 Source/Config/SQL/Command/Result. 실제 구현 Symbol과 혼동되지 않게 표시합니다.

문서 QA의 페이지 수·표 수·링크 수는 본문 품질의 대체 지표로 사용하지 않습니다. 실제 Source·Config·SQL·명령·출력·Test·Fault Injection·복구 절차가 필요한 기능은 문서 안에서 해당 실행 단위를 직접 제공합니다.
