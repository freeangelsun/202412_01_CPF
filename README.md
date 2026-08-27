<!-- CPF-DARK-CONTENT-SURFACE -->

<div align="center">

<img src="cpf-docs/assets/product-docs/hero.png" alt="CPF Core Platform Framework - 업무 호출, 거래 상태, Batch, DB3, 운영 계약" width="100%" />

# Core Platform Framework

**업무 개발자는 Public API와 선택 기준에 집중하고, CPF는 Context·실패·복구·추적 경계를 연결합니다.**

CPF는 Spring Boot 기반 업무 시스템에서 반복되는 Domain 호출, Transaction, Security, Integration, Messaging, Batch, DB3, 운영 규칙을 하나의 Business Platform 계약으로 제공합니다.

</div>

<img src="cpf-docs/assets/product-docs/architecture.png" alt="CPF 전체 Architecture - Entry, Business Domain, Framework Capability, Operations, DB3 Owner 경계" width="100%" />

Generated/Prebuilt Domain이 업무 Owner를 유지하고 Public Starter가 기술 Capability 진입점을 제공합니다. Gateway는 외부 Entry의 선택 경계이며 내부 Domain 호출 경로가 아닙니다. Oracle·PostgreSQL·MariaDB는 같은 Canonical DB Lifecycle 의미를 유지합니다.

---

# 1. 배포 구조가 달라져도 같은 Domain Invocation 계약을 사용합니다

<img src="cpf-docs/assets/product-docs/invoke.png" alt="Same JVM과 Remote Domain Invocation 비교" width="100%" />

Same JVM에서는 in-process binding과 `CpfContext`를 사용하고, Remote/MSA에서는 Registry·Transport가 endpoint를 선택합니다. 업무 코드는 배포 방식별 분기 대신 같은 Domain Operation 계약을 사용하며 **내부 Domain 간 호출은 Gateway를 재경유하지 않습니다.**

- Same JVM: self-HTTP 없이 Local binding으로 호출합니다.
- Remote/MSA: System6·Timeout·Trace를 경계에서 자동 적용합니다.
- 거래 추적: `transactionId`·`operationId`·`instanceId`로 거래·Operation·Runtime을 연결합니다.

---

# 2. 성공·실패뿐 아니라 결과가 확정되지 않은 상태까지 관리합니다

<img src="cpf-docs/assets/product-docs/tx.png" alt="SUCCESS, BUSINESS FAILURE, TECHNICAL FAILURE, UNKNOWN과 Reconcile 복구 경로" width="100%" />

Local DB Transaction과 Remote Side Effect를 같은 원자성으로 보지 않습니다. 응답 유실이나 Timeout으로 결과를 확정할 수 없으면 `UNKNOWN`을 일반 실패로 덮지 않고 Idempotency·Probe·Reconcile·Compensation 경로로 실제 결과를 확인합니다.

- `SUCCESS`: 결과가 확정된 상태입니다.
- `BUSINESS_FAILURE`: 업무 규칙상 실패이며 blind retry 대상이 아닙니다.
- `TECHNICAL_FAILURE`: 인프라/통신 실패이며 정책에 따라 Retry 여부를 판단합니다.
- `UNKNOWN`: Side Effect 결과를 추측하지 않고 Reconcile합니다.

---

# 3. Batch는 제어와 실행을 분리해 재시작·재처리 판단을 명확하게 합니다

<img src="cpf-docs/assets/product-docs/batch.png" alt="Batch Control Plane과 Execution Lane" width="100%" />

Control Plane은 Policy·Scheduler·Deployment·Recovery를 담당하고 Worker/Center-Cut은 실행 책임을 가집니다. Lease·Fencing·Heartbeat·Checkpoint를 사용해 Process Kill과 stale Worker 상황에서도 실행 소유권과 복구 지점을 확인할 수 있습니다.

- `Restart`: Checkpoint를 이어서 다시 시작할 때 사용합니다.
- `Rerun`: 동일 Job을 새 실행으로 다시 수행할 때 사용합니다.
- `Reprocess`: 특정 대상/범위를 다시 처리할 때 사용합니다.
- `Reconcile`: 결과가 확정되지 않은 실행의 실제 상태를 확인할 때 사용합니다.

---

# 4. Domain 생성·Starter 선택·DB3 변경을 같은 개발 흐름으로 맞춥니다

업무 Source는 Generated/Prebuilt Domain Owner에 두고 필요한 Public Profile/Starter와 Provider만 선택합니다. Framework 내부 Leaf를 직접 조합하지 않으며 선택하지 않은 Optional Capability가 Bean·Thread·SQL·Endpoint Side Effect를 남기지 않는 것이 기본 계약입니다.

- Domain lifecycle: `create → setup --preview → sync/diff → regenerate/upgrade` 순서로 확인합니다.
- Public Starter: Web/API, Security, Data, Cache, Messaging, Integration, File, Batch 등을 필요한 범위만 선택합니다.
- DB3: Oracle·PostgreSQL·MariaDB를 Canonical Source → Migration → Upgrade → Rollback/Recovery까지 함께 검증합니다.

---

# 5. Gateway·외부 연계도 업무 Owner 경계를 우회하지 않습니다

<img src="cpf-docs/assets/product-docs/gateway.png" alt="Gateway 사용과 미사용 Topology 비교" width="100%" />

Gateway는 인증·Route·Rate Limit 같은 **외부 Entry Policy**가 필요할 때 선택합니다. Gateway가 없는 Trusted Entry Topology도 지원하지만, 어느 경우에도 내부 Domain→Domain 호출을 Gateway로 돌려 보내지 않습니다. Backoffice도 Owner Domain API/호출 계약을 사용하며 업무 원장을 직접 우회하지 않습니다.

외부 HTTP·Messaging·File 연계는 Timeout·Retry·Idempotency·UNKNOWN/Reconcile을 같은 실패 모델로 연결합니다.

---

# 6. 운영자는 거래·Operation·Runtime을 같은 Timeline으로 추적합니다

<img src="cpf-docs/assets/product-docs/ops.png" alt="transactionId, operationId, instanceId, recoveryId, Audit 연결 Timeline" width="100%" />

`transactionId`로 거래 전체를 찾고, `operationId`로 현재 업무 Operation을 좁히며, `instanceId`로 실제 처리 Runtime을 확인합니다. 위험 조치는 Permission → Reason → Approval → Execution → Audit 흐름으로 연결해 누가 왜 무엇을 실행했는지 추적합니다.

민감정보는 Log·화면·Evidence에 원문으로 남기지 않고, 장애·복구 결과도 같은 식별 체계에서 확인합니다.

---

# 7. 반복 공통 기능은 필요한 Capability만 선택해 사용합니다

<img src="cpf-docs/assets/product-docs/capabilities.png" alt="CPF Public Capability 선택 지도" width="100%" />

Web/API · Persistence · Transaction · Security · Cache · Messaging · Integration · File · Observability · Batch · Common · Config를 업무에 필요한 범위만 선택합니다. Provider가 바뀌어도 Context·Security·Trace·Failure 경계를 유지하고, 일반 Java/Spring 기능은 필요한 경우 Native API를 사용하되 CPF 관리 경계를 우회하지 않습니다.

---

# 8. 개발 시작·Build·Test·Runtime 진입점이 정해져 있습니다

**요구 환경**

- Java 25
- Repository Gradle Wrapper
- 공식 DB Vendor: Oracle / PostgreSQL / MariaDB

Repository Root에서 다음 순서로 확인합니다.

```powershell
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 status
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 build
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 verify-full
```

일상 개발은 `cpfVerifyFast` → 변경 Capability의 `cpfVerifyTargeted` → 최종 `cpfVerifyFullLocal` 순으로 검증합니다. 실행하지 않은 Test는 PASS로 기록하지 않습니다.

---

# 9. 지금 하려는 일에 맞는 문서부터 엽니다

| 지금 필요한 일 | 먼저 볼 문서 | 이 문서에서 바로 얻는 답 | 바로 열기 |
|---|---|---|---|
| CRUD·Transaction·오류·호출 개발 | 프레임워크 개발자 가이드 | 실제 Public API, 옵션, 선택 기준, 실패·복구, 최소 예 | [PDF](cpf-docs/guides/02_프레임워크_개발자_가이드.pdf) |
| Batch Job/Step·동시성·복구 개발 | 배치 개발자 가이드 | Batch Public API, 실행/복구 선택, UNKNOWN 대응 | [PDF](cpf-docs/guides/03_배치_개발자_가이드.pdf) |
| 거래·Runtime 장애 대응 | 운영자 매뉴얼 | 식별자 기반 원인 판단, Retry/Reconcile, 정상화 기준 | [PDF](cpf-docs/guides/04_운영자_매뉴얼.pdf) |
| Batch 실행·중단·재처리 | 배치 운영 가이드 | Restart/Rerun/Reprocess/Reconcile 선택과 안전 조치 | [PDF](cpf-docs/guides/05_배치_운영_가이드.pdf) |
| Gateway 사용 여부·Route·정책 | Gateway 개발/사용 가이드 | Gateway 경계, Route, Security, Timeout/Rate Limit | [PDF](cpf-docs/guides/06_Gateway_개발_사용_가이드.pdf) |
| Public Contract·상태·오류 확인 | Specification 기술 명세 | Context/Result/Error/Domain/Batch/DB 계약 | [PDF](cpf-docs/guides/07_Specification_기술_명세.pdf) |
| 전체 Owner·Topology 구조 확인 | 아키텍처설계서 | Module Ownership, Same JVM/Remote, Gateway/Batch/DB 경계 | [PDF](cpf-docs/deliverables/아키텍처설계서.pdf) |
| Runtime·Starter·호환성 확인 | 기술사양서 | Java/Spring/Starter/Provider/Runtime 지원 기준 | [PDF](cpf-docs/deliverables/기술사양서.pdf) |
| Naming·Dependency·개발 규칙 확인 | 기술표준서 | 지켜야 할 개발/구조/보안/검증 표준 | [PDF](cpf-docs/deliverables/기술표준서.pdf) |
| DB3 설계·Migration·Rollback | 데이터베이스표준서 | Oracle/PostgreSQL/MariaDB 공통 규칙과 Lifecycle | [PDF](cpf-docs/deliverables/데이터베이스표준서.pdf) |
| 어떤 문서를 먼저 볼지 판단 | 산출물목록 | 역할·상황별 문서 길찾기 | [PDF](cpf-docs/deliverables/산출물목록.pdf) |

DOCX는 편집·보관용 최종 산출물로 ZIP 안에 함께 제공하지만 일반 사용자용 바로 열기 링크에는 노출하지 않습니다.

---

# 10. Community & Evaluation License

CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.

