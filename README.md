<div align="center">

<img src="cpf-docs/assets/product-docs/hero.png" alt="CPF Core Platform Framework" width="960" />

# Core Platform Framework

**업무 호출 · 거래 상태 · Batch · DB3 · 운영을 하나의 계약으로 연결하는 Business Platform Framework**

CPF는 Spring Boot 생태계를 기반으로 업무 개발자가 반복해서 조립하던 호출·Transaction·Security·Integration·Batch·운영 규칙을 Public Starter/API와 실행 계약으로 묶습니다.

</div>

![CPF 전체 Architecture](cpf-docs/assets/product-docs/architecture.png)

> **전체 구조 한눈에 보기**  
> • Generated/Prebuilt Domain, Public Starter, Runtime/Operations와 DB3의 Owner 경계를 한 화면에서 구분합니다.  
> • Gateway는 외부 Entry의 선택 경계이며 **내부 Domain 간 호출 경로가 아닙니다.**  
> • Oracle · PostgreSQL · MariaDB는 Canonical Schema → Migration → Recovery의 같은 Lifecycle 의미를 유지합니다.

---

# 1. 호출·오케스트레이션은 배포 구조가 달라도 같은 업무 계약을 유지합니다

![Same JVM과 Remote Domain Invocation 비교](cpf-docs/assets/product-docs/invoke.png)

> **그림 해석**  
> • Same JVM은 in-process binding과 `CpfContext`를 사용합니다.  
> • Remote/MSA는 Registry·Transport가 endpoint를 선택하고 System6·Timeout·Trace 계약을 경계에서 적용합니다.  
> • **내부 Domain 간 호출은 Gateway를 경유하지 않습니다.** Gateway는 외부 진입 정책이 필요한 경우에만 선택합니다.

업무 코드는 배포 방식별 `if/else`를 작성하는 대신 동일한 Domain Operation을 호출합니다. 이를 통해 단일 JVM에서 시작해 MSA로 분리해도 호출 의미와 오류·추적 계약을 유지합니다.

---

# 2. 성공과 실패 사이의 불확실성까지 거래 상태로 관리합니다

![Transaction UNKNOWN Reconcile 상태 모델](cpf-docs/assets/product-docs/tx.png)

> **그림 해석**  
> • Local DB Transaction과 Remote Side Effect를 같은 원자성으로 보지 않습니다.  
> • 응답 유실·Timeout처럼 결과를 확정할 수 없으면 `UNKNOWN`으로 보존합니다.  
> • 재호출보다 Reconcile로 실제 결과를 확인하고 Idempotency로 중복 Side Effect를 막습니다.

Saga·TCC·XA는 참여 자원과 확정/보상 모델에 맞게 선택하며, 결과가 불명확한 상태에서는 먼저 Reconcile 가능성을 판단합니다.

---

# 3. Batch는 실행·제어·복구 역할을 나눠 운영 실수를 줄입니다

![Batch Control Plane과 Execution Lane](cpf-docs/assets/product-docs/batch.png)

> **그림 해석**  
> • Control Plane은 정책·Scheduler·Deployment·Recovery를 관리합니다.  
> • Worker/Center-Cut은 실행·분할 책임을 분리하고 DB claim·Lease·Fencing으로 소유권을 보호합니다.  
> • Batch 실행 Topology는 `LOCAL · PARALLEL_STEPS · LOCAL_PARTITION`이며 Batch 전용 Kafka/Broker Remote Execution은 제품 범위가 아닙니다.  
> • Heartbeat·Checkpoint를 함께 사용해 Process Kill과 stale Worker를 복구합니다.

Restart, Rerun, Reprocess, Reconcile은 서로 다른 운영 행위입니다. CPF는 실행 상태와 외부 Side Effect를 함께 보고 어떤 복구를 선택해야 하는지 구분합니다.

---

# 4. Domain 생성부터 Starter와 DB까지 같은 규칙으로 맞춥니다

> **구조 핵심**  
> • 업무 Source는 Generated/Prebuilt Domain Owner에 둡니다.  
> • `cpf-core`·`cpf-common`·Public Starter는 기술 계약과 Capability 진입점을 제공합니다.  
> • 공식 DB Vendor는 **Oracle · PostgreSQL · MariaDB**이며 Canonical Source부터 Migration·Rollback까지 같은 의미를 유지합니다.

Framework 내부 모듈을 직접 조합하기보다 Public Profile/Starter에서 시작합니다. 선택하지 않은 Optional Capability가 Bean·Thread·SQL·Endpoint Side Effect를 남기지 않는 것이 기본 계약입니다.

---

# 5. 외부 연계와 Gateway·Backoffice도 업무 Owner 경계를 유지합니다

![Gateway 선택 및 미선택 Topology](cpf-docs/assets/product-docs/gateway.png)

> **그림 해석**  
> • Gateway는 인증·Route·Rate Limit 같은 **외부 Entry Policy**가 필요할 때 선택합니다.  
> • Gateway를 쓰지 않아도 Trusted Entry에서 Owner Domain으로 진입할 수 있습니다.  
> • Backoffice와 외부 Channel도 Owner Domain의 API/호출 계약을 사용하며 원장을 직접 우회하지 않습니다.

외부 HTTP·Messaging·File 연계는 Timeout, Retry, Idempotency, UNKNOWN/Reconcile을 동일한 실패 모델로 연결합니다.

---

# 6. 운영자는 거래와 Runtime을 같은 식별 체계로 추적합니다

![Operations Trace Timeline](cpf-docs/assets/product-docs/ops.png)

> **그림 해석**  
> • `transactionId`는 거래 전체, `operationId`는 현재 Operation, `instanceId`는 실제 처리 Runtime을 좁혀 줍니다.  
> • Log·Trace·Health·Recovery가 같은 Timeline으로 이어집니다.  
> • 위험 조치는 Permission → Reason → Approval → Execution → Audit 흐름으로 추적합니다.

민감정보는 Log·화면·Evidence에 원문으로 남기지 않고, 운영 실패·복구 결과도 동일한 식별자와 Audit 흐름에서 확인할 수 있어야 합니다.

---

# 7. 반복 공통 기능을 다시 만들지 않고 필요한 Capability를 선택합니다

![CPF Capability Selection](cpf-docs/assets/product-docs/capabilities.png)

• Web/API · Persistence · Transaction · Security · Cache · Messaging · Integration · File · Observability · Batch를 필요한 범위만 선택합니다.  
• Provider가 필요한 영역은 JDBC/MyBatis/JPA, Redis/Valkey/Caffeine, Kafka/RabbitMQ/JMS처럼 구현 선택과 업무 계약을 분리합니다.  
• CPF가 표준화하지 않는 일반 Java/Spring 기능은 Native API를 사용할 수 있지만 Context·Security·Trace·Failure 경계는 우회하지 않습니다.

---

# 8. 시작·생성·Build·Test 흐름이 정해져 있어 바로 개발에 들어갑니다

**요구 환경**

• Java 25  
• Repository Gradle Wrapper  
• 공식 DB: Oracle / PostgreSQL / MariaDB

Repository Root에서 다음 흐름으로 시작합니다.

```powershell
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 status
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 build
pwsh .\cpf-tools\build\tools\cpf-dev.ps1 verify-full
```

Domain 생성/동기화는 `cpf-tools/runtime/cli/cpf.py`의 `domain create/setup/sync/diff/remove` 흐름을 사용합니다. 실행하지 않은 Test는 PASS로 기록하지 않습니다.

---

# 9. 필요한 매뉴얼과 상세 가이드를 역할별로 바로 찾습니다


| 필요한 정보 | 문서 | 바로 열기 |
|---|---|---|
| 업무 개발 · API 선택 · 실패/복구 | 프레임워크 개발자 가이드 | [PDF](cpf-docs/guides/02_프레임워크_개발자_가이드.pdf) · [DOCX](cpf-docs/guides/02_프레임워크_개발자_가이드.docx) |
| Batch Job · 동시성 · 복구 | 배치 개발자 가이드 | [PDF](cpf-docs/guides/03_배치_개발자_가이드.pdf) · [DOCX](cpf-docs/guides/03_배치_개발자_가이드.docx) |
| 거래·Runtime 장애 대응 | 운영자 매뉴얼 | [PDF](cpf-docs/guides/04_운영자_매뉴얼.pdf) · [DOCX](cpf-docs/guides/04_운영자_매뉴얼.docx) |
| Batch 실행·중단·재처리 | 배치 운영 가이드 | [PDF](cpf-docs/guides/05_배치_운영_가이드.pdf) · [DOCX](cpf-docs/guides/05_배치_운영_가이드.docx) |
| Gateway 선택·Route·정책 | Gateway 개발/사용 가이드 | [PDF](cpf-docs/guides/06_Gateway_개발_사용_가이드.pdf) · [DOCX](cpf-docs/guides/06_Gateway_개발_사용_가이드.docx) |
| Public Contract · 상태 · 오류 | Specification 기술 명세 | [PDF](cpf-docs/guides/07_Specification_기술_명세.pdf) · [DOCX](cpf-docs/guides/07_Specification_기술_명세.docx) |
| 전체 구조 · Owner · Topology | 아키텍처설계서 | [PDF](cpf-docs/deliverables/아키텍처설계서.pdf) · [DOCX](cpf-docs/deliverables/아키텍처설계서.docx) |
| Runtime · Starter · 호환성 | 기술사양서 | [PDF](cpf-docs/deliverables/기술사양서.pdf) · [DOCX](cpf-docs/deliverables/기술사양서.docx) |
| Naming · Ownership · 개발 표준 | 기술표준서 | [PDF](cpf-docs/deliverables/기술표준서.pdf) · [DOCX](cpf-docs/deliverables/기술표준서.docx) |
| DB3 · Migration · Rollback | 데이터베이스표준서 | [PDF](cpf-docs/deliverables/데이터베이스표준서.pdf) · [DOCX](cpf-docs/deliverables/데이터베이스표준서.docx) |
| 전체 공식 문서 목록 | 산출물목록 | [PDF](cpf-docs/deliverables/산출물목록.pdf) · [DOCX](cpf-docs/deliverables/산출물목록.docx) |

---

# 10. Community & Evaluation License

CPF는 **Community & Evaluation License** 안내를 기준으로 사용합니다.

---

<sub>Documentation baseline: Harness v2.1.0 · Source: master `054d894b47f4be8323439dc6f9e58b7d8b60fe54` · 2026-08-26</sub>
