<!-- CPF-DARK-CONTENT-SURFACE -->

<div align="center">

<img src="cpf-docs/assets/product-docs/hero.png" alt="CPF Core Platform Framework - 업무 개발, Runtime, Batch, DB3, 운영을 하나의 계약으로 연결" width="100%" />

<p align="center"><sub>업무 Domain의 소유권은 유지하고, 개발·실행·Batch·운영에서 반복되는 기술 경계를 CPF Public Contract로 연결합니다.</sub></p>

# Core Platform Framework

**업무 Domain의 Owner는 그대로 유지하고, 반복되는 호출·거래·연계·Batch·운영 경계를 CPF의 Public Contract로 연결합니다.**

CPF는 Spring Boot 기반 업무 시스템에서 Context, Transaction, Security, Logging/Audit, Domain Invocation, 외부 연계, Messaging, Cache, Batch, DB3, 운영 추적을 공통 계약으로 제공합니다. 개발자는 업무 기능과 선택 기준에 집중하고, 배포 구조·Provider·실패 복구·운영 추적의 차이는 CPF 경계에서 일관되게 처리합니다. 동일 JVM에서 시작한 서비스가 분리 WAS·MSA·다중 인스턴스로 확장되어도 Domain 호출 의미와 거래 추적 기준을 다시 정의하지 않고, 장애 시에도 단순 성공/실패가 아니라 실제 다음 조치까지 이어지도록 설계합니다.

</div>

<br><br>

<img src="cpf-docs/assets/product-docs/architecture.png" alt="CPF 전체 Architecture - Entry, Business Domain, Public Starter, Framework, Operations, DB3 Owner 경계" width="100%" />

업무 Source와 데이터의 Owner는 Generated/Prebuilt Domain에 남습니다. CPF는 Owner를 대신하지 않고 **호출 방법, 기술 Capability, 실패 의미, 복구 방법, 운영 식별자**를 공통화합니다. Gateway와 Backoffice는 필요한 경우 선택하는 경계이며 내부 Domain 간 호출을 우회시키지 않습니다.

> **CPF가 표준화하는 것은 업무 자체가 아니라 업무를 둘러싼 반복 기술 경계입니다.** 프로젝트마다 다시 만들던 Context 전달, 오류 분류, Retry/Idempotency, Security/Audit, Batch 복구, DB Migration, 운영 추적을 같은 규칙으로 연결해 업무 Domain은 업무 규칙과 데이터 소유에 집중하게 합니다.

- `cpf-core`는 배포 Topology에 독립적인 핵심 계약을 제공합니다.
- Public Starter는 업무가 필요한 Capability만 선택하게 하고 Internal Leaf 직접 조합을 막습니다.
- `cpf-batch`는 Job 계약뿐 아니라 Scheduler·Worker·Center-Cut·Control Plane의 실행/복구 경계를 제공합니다.
- 운영 Surface는 거래·Operation·Instance·Recovery·Audit 정보를 같은 흐름에서 찾게 합니다.
- Oracle·PostgreSQL·MariaDB는 같은 Canonical DB Lifecycle을 따릅니다.

<br><br>

## 1. 배포 구조가 달라져도 Domain 호출 코드는 다시 만들지 않습니다

<img src="cpf-docs/assets/product-docs/invoke.png" alt="Same JVM과 Remote Domain Invocation 비교 - 같은 Domain Operation 계약과 Context 전파" width="100%" />

Same JVM에서는 self-HTTP 없이 Local binding으로 호출하고, Remote/MSA에서는 Registry·Transport가 실제 endpoint를 선택합니다. 업무 코드는 `if local ... else remote ...` 같은 배포 분기를 만들지 않고 같은 Domain Operation 계약을 사용합니다.

- **System6 Header**는 경계를 넘을 때 CPF가 생성·검증·전파하며 업무 개발자가 Header 조립 코드를 반복하지 않습니다.
- `transactionId`는 거래 전체, `operationId`는 업무 Operation, `instanceId`는 실제 Runtime을 식별합니다.
- Timeout·Trace·Security Context도 호출 경계에서 같은 기준으로 적용됩니다.
- 내부 Domain→Domain 호출은 Gateway를 다시 경유하지 않아 외부 Entry Policy와 내부 업무 호출 책임이 섞이지 않습니다.

<br><br>

## 2. 실패를 하나의 `FAILED`로 뭉개지 않고 다음 행동까지 연결합니다

<img src="cpf-docs/assets/product-docs/tx.png" alt="SUCCESS, BUSINESS FAILURE, TECHNICAL FAILURE, UNKNOWN과 Reconcile 복구 경로" width="100%" />

Local DB Commit과 Remote Side Effect는 같은 원자성이 아닙니다. CPF는 업무 실패, 기술 실패, 결과 미확정을 구분해 **Retry 가능한지, 다시 호출하면 안 되는지, 실제 결과를 확인해야 하는지**를 판단할 근거를 남깁니다.

- `BUSINESS_FAILURE`는 업무 규칙상 거절이므로 blind retry 대상으로 취급하지 않습니다.
- `TECHNICAL_FAILURE`는 Timeout·연결·Provider 장애를 분류하고 정책에 따라 bounded retry 여부를 판단합니다.
- 응답 유실로 실제 Side Effect 결과를 모르면 `UNKNOWN`을 실패로 추정하지 않고 Probe·Reconcile로 확인합니다.
- `@CpfIdempotent`, Inbox/Outbox, Recovery ID, Fencing 같은 계약으로 재호출·Process 재시작·중복 실행을 제어합니다.

분산 거래도 한 방식으로 고정하지 않습니다. Local Transaction, Saga, TCC, XA/JTA를 자원과 업무 특성에 따라 구분하고, 각 전략에서 **보상·확정·복구 Evidence가 남는 것**을 완료 조건으로 봅니다.

<br><br>

## 3. Batch는 “돌았다/안 돌았다”가 아니라 실행 소유권과 복구 지점을 관리합니다

<img src="cpf-docs/assets/product-docs/batch.png" alt="Batch Control Plane과 Scheduler, Worker, Center-Cut 실행 및 복구 구조" width="100%" />

Control Plane은 Policy·Schedule·Deployment·Recovery를 관리하고 Worker/Center-Cut은 실제 실행을 담당합니다. 다중 인스턴스와 Process Kill 상황에서도 Lease·Fencing·Heartbeat·Checkpoint로 **누가 실행을 소유했는지, 어디서 이어야 하는지, 이전 Worker가 더 이상 쓸 수 없는지**를 판단합니다.

- `Restart`: 같은 실행을 Checkpoint부터 이어갑니다.
- `Rerun`: 동일 Job을 새 실행으로 다시 수행합니다.
- `Reprocess`: 특정 대상/범위만 다시 처리합니다.
- `Reconcile`: 완료 여부를 확정할 수 없는 실행의 실제 상태를 확인합니다.
- Scheduler·Worker·Center-Cut·Agent를 분리해 대량 처리와 Host 제어를 한 프로세스 책임으로 몰지 않습니다.

<br><br>

## 4. Domain 생성부터 검증까지 같은 개발 경로를 사용합니다

<img src="cpf-docs/assets/product-docs/development.png" alt="CPF 개발 흐름 - Domain 생성, Starter 선택, Public API 개발, DB3/Generator 동기화, 단계별 검증과 운영 연결" width="100%" />

Generator가 Domain 구조를 만들고, Public Profile/Starter가 Runtime Capability를 선택하며, Public API가 업무 코드의 표준 경계를 제공합니다. 이후 DB3·OpenAPI·Generated Source·Sample·Test가 같은 Canonical 입력을 따라가므로 **프로젝트마다 서로 다른 개발 관례를 다시 정의하는 영역을 줄입니다.**

- Generator: `create → setup --preview → sync/diff → build/test` 흐름으로 생성·변경을 검토합니다.
- Public Starter: 필요한 Capability만 선택하고 Internal Starter/Leaf 직접 dependency를 금지합니다.
- 고객사 공통 Library: `cpf library create/attach/sync/verify`로 별도 작업공간에서 관리하고 필요한 Domain에만 연결합니다.
- 검증: Fast → Targeted → Full Local 순으로 피드백 시간을 줄이되 최종 검증 강도는 낮추지 않습니다.
- Generated-owned Source와 사용자 Source의 경계를 구분해 재생성·Upgrade 시 임의 덮어쓰기를 막습니다.

<br><br>

## 5. 반복 공통 기능을 업무마다 직접 조립하지 않습니다

<img src="cpf-docs/assets/product-docs/capabilities.png" alt="CPF Public Capability Map - Web, Data, Transaction, Security, Cache, Messaging, Integration, File, Observability, Batch, Common, Config" width="100%" />

업무 개발자는 필요한 기능의 Public Contract를 선택하고 Provider 구현과 운영 경계는 CPF가 담당합니다. 일반 Java/Spring 기능은 그대로 사용할 수 있지만, 거래·보안·감사·복구가 필요한 경계는 CPF 표준을 우회하지 않습니다.

- **Web / Service / Repository** — `@CpfRestController`, `@CpfService`, `@CpfRepository`로 계층과 거래 경계를 명확히 합니다.
- **Transaction / Reliability** — `@CpfTransactional`, `@CpfIdempotent`, afterCommit/afterRollback, Outbox/Inbox로 Local/Remote 일관성을 구분합니다.
- **Security / Audit** — `@CpfPermission`, `@CpfApprovalRequired`, `@CpfAudit`로 권한·사유·승인·감사를 위험 조치와 연결합니다.
- **Cache / Lock** — TTL·Invalidation·Stampede·Multi-instance refresh·Lease/Fencing까지 장애 시 동작을 포함해 관리합니다.
- **Messaging / Event** — Ack/Nack·Retry·DLQ·Replay·Ordering·Duplicate·Outbox를 동일한 운영 모델로 다룹니다.
- **Integration** — HTTP/TCP/SOAP/Fixed Length/ISO8583/Webhook 등 외부 호출에 Timeout·Retry·Circuit Breaker·UNKNOWN을 연결합니다.
- **File / Object Storage / Notification** — Stream·Checksum·Atomic finalize·Quarantine·SFTP/S3·Email/SMS 결과 추적과 보안 경계를 제공합니다.
- **Common / Config / Observability** — Code·Message·Parameter·Calendar·Validation·Config·Log·Metric·Trace·Health를 공통 계약으로 사용합니다.

<br><br>

## 6. DB 변경은 SQL 한 장이 아니라 세 Vendor의 전체 Lifecycle로 관리합니다

Oracle·PostgreSQL·MariaDB를 공식 DB Vendor로 사용하고, Canonical Source에서 생성된 변경이 **Fresh Init → Migration → Seed → Runtime Query → Upgrade → Rollback/Recovery**까지 이어지는지 함께 검증합니다.

- Vendor별 DDL 문법 차이를 업무 Source에 흩뿌리지 않고 Canonical 정의와 Renderer에서 관리합니다.
- Migration 적용 전 Dry-run Plan과 Hash를 검토하고, 실제 적용 시 중지·백업·Rollback 준비·운영 Audit 정보를 요구합니다.
- 실패 후 `reconcileRequired=true`이면 자동 성공/실패를 추정하지 않고 실제 DB 상태를 확인합니다.
- DB 변경은 Generator/Generated Domain, Repository/Mapper, Index/FK, Sample/Test 영향까지 같은 변경 단위에서 확인합니다.

<br><br>

## 7. Gateway와 Backoffice는 필요한 책임만 갖고 업무 Owner를 우회하지 않습니다

<img src="cpf-docs/assets/product-docs/gateway.png" alt="Gateway 사용과 미사용 Topology, Backoffice와 Owner Domain 경계" width="100%" />

Gateway는 외부 Entry에 인증·Route·Rate Limit 같은 Policy가 필요할 때 선택합니다. Trusted Entry에서는 Gateway 없는 Topology도 가능하며, 어느 경우에도 내부 Domain 호출을 Gateway로 되돌리지 않습니다.

Backoffice도 업무 원장을 복제하거나 직접 소유하지 않고 Owner Domain의 API/호출 계약을 사용합니다. 따라서 Gateway·Backoffice를 제거하거나 교체해도 Core Business Domain의 소유권과 호출 계약이 흔들리지 않습니다.

<br><br>

## 8. 운영 화면에서 로그를 뒤지는 대신 같은 식별 체계로 거래를 따라갑니다

<img src="cpf-docs/assets/product-docs/ops.png" alt="transactionId, operationId, instanceId, recoveryId와 Permission, Approval, Audit가 연결된 운영 Timeline" width="100%" />

`transactionId → operationId → instanceId → recoveryId`를 따라가면 여러 System과 Instance에 걸친 거래를 같은 기준으로 좁힐 수 있습니다. Log·Trace·Metric·Health·Audit가 서로 다른 이름 체계를 쓰지 않도록 Runtime Identity를 연결합니다.

- 위험 조치는 **Permission → Reason → Approval → Execution → Audit** 순서로 추적합니다.
- Retry/Reconcile/재처리 같은 복구 조치는 원 거래와 Recovery Evidence를 연결합니다.
- 민감정보는 Log·화면·Evidence·Test 산출물에서 원문 노출하지 않고 Masking 정책을 적용합니다.
- 운영자가 `UNKNOWN`, stale Worker, Provider outage 같은 상태를 정상 실패와 구분할 수 있게 상태 의미를 보존합니다.

<br><br>

## 9. 기존 시스템도 한 번에 갈아엎지 않고 단계적으로 CPF 경계를 늘릴 수 있습니다

기존 시스템의 모든 기술을 한 번에 교체할 필요는 없습니다. Context/Logging부터 시작해 Transaction·Common·Domain Call·Integration·Cache·Messaging·Security·Batch/Backoffice로 적용 범위를 확장할 수 있습니다.

1. Context + Logging
2. Validation + Transaction
3. Code / Message / Parameter / Calendar
4. Domain Call / External Integration
5. Cache / Messaging / Security
6. Generator / ADM / Backoffice / Batch / Gateway / Advanced Recovery

기존 표준을 연결할 때는 CPF가 제공하는 SPI/Extension을 우선 사용하고, 업체별 구현을 `cpf-core`에 넣지 않습니다. Java Collection·순수 함수·단순 Bean wiring처럼 CPF 관리가 불필요한 영역은 Native API를 그대로 사용합니다.

<br><br>

## 10. 개발 시작·Build·Test·Runtime 진입점을 같은 방식으로 확인합니다

**요구 환경**

- Java 25
- Repository Gradle Wrapper
- 공식 DB Vendor: Oracle / PostgreSQL / MariaDB

Repository Root에서 현재 상태와 전체 검증 경로를 확인합니다.

```powershell
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 status
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 build
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 verify-full
```

일상 개발은 `cpfVerifyFast` → 변경 Capability의 `cpfVerifyTargeted` → 최종 `cpfVerifyFullLocal` 순으로 검증합니다. Targeted 검증은 최종 Full Local 시나리오를 줄이는 기능이 아니며, 실행하지 않은 Test는 PASS로 기록하지 않습니다.

<br><br>

## 11. 필요한 문서로 바로 이동합니다

- **CRUD·Transaction·오류·Domain 호출 개발** — [프레임워크 개발자 가이드 PDF](cpf-docs/guides/02_프레임워크_개발자_가이드.pdf)에서 Public API, 선택 기준, 실패·복구, 최소 예제를 확인합니다.
- **Batch Job/Step·동시성·복구 개발** — [배치 개발자 가이드 PDF](cpf-docs/guides/03_배치_개발자_가이드.pdf)에서 Batch API, 실행 역할, Checkpoint와 UNKNOWN 대응을 확인합니다.
- **거래·Runtime 장애 대응** — [운영자 매뉴얼 PDF](cpf-docs/guides/04_운영자_매뉴얼.pdf)에서 식별자 기반 원인 판단, Retry/Reconcile, 정상화 기준을 확인합니다.
- **Batch 실행·중단·재처리** — [배치 운영 가이드 PDF](cpf-docs/guides/05_배치_운영_가이드.pdf)에서 Restart/Rerun/Reprocess/Reconcile 선택과 안전 조치를 확인합니다.
- **Gateway 사용 여부·Route·정책** — [Gateway 개발/사용 가이드 PDF](cpf-docs/guides/06_Gateway_개발_사용_가이드.pdf)에서 Gateway 경계, Security, Timeout, Rate Limit을 확인합니다.
- **Public Contract·상태·오류** — [Specification 기술 명세 PDF](cpf-docs/guides/07_Specification_기술_명세.pdf)에서 Context, Result, Error, Domain, Batch, DB 계약을 확인합니다.
- **전체 Owner·Topology** — [아키텍처설계서 PDF](cpf-docs/deliverables/아키텍처설계서.pdf)에서 Module Ownership, Same JVM/Remote, Gateway/Batch/DB 경계를 확인합니다.
- **Runtime·Starter·호환성** — [기술사양서 PDF](cpf-docs/deliverables/기술사양서.pdf)에서 Java/Spring/Starter/Provider/Runtime 지원 기준을 확인합니다.
- **Naming·Dependency·개발 규칙** — [기술표준서 PDF](cpf-docs/deliverables/기술표준서.pdf)에서 개발/구조/보안/검증 표준을 확인합니다.
- **DB3 설계·Migration·Rollback** — [데이터베이스표준서 PDF](cpf-docs/deliverables/데이터베이스표준서.pdf)에서 3개 Vendor의 공통 규칙과 Lifecycle을 확인합니다.
- **역할별 문서 선택** — [산출물목록 PDF](cpf-docs/deliverables/산출물목록.pdf)에서 상황별로 먼저 볼 문서를 찾습니다.

DOCX는 편집·보관용 최종 산출물로 함께 제공하지만 일반 사용자 Navigation에는 PDF 링크만 노출합니다.

<br><br>

## 12. Community & Evaluation License

CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.
