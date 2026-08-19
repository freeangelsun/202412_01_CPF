# CPF_DOCUMENTATION_STANDARD

> Canonical Repository Path: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
>
> 문서 성격: CPF 공식 사용자 산출물 **내용 작성·현행화 최종 정본**
>
> 이 문서는 CPF 공식 사용자 문서가 **누구를 위해, 어떤 목적과 정보구조로, 어느 깊이까지 작성되어야 하는지**를 정의한다.
>
> 폰트, 여백, 셀 간격, Word Style, 페이지 Header/Footer 같은 순수 편집 규칙은 별도의 공식 문서 편집·작성 표준에서 관리한다.
>
> 단, **표·그림·예제를 왜 쓰는지, 어떤 내용을 담아야 하는지, 독자에게 어떤 판단을 가능하게 해야 하는지**는 내용 품질의 일부이므로 본 지침에서 정의한다.
>
> 본 문서는 과거 CPF 문서작성 관련 지침을 통합·현행화한 최종 기준으로 사용한다. 새로운 공식 사용자 문서를 계속 추가하지 않고, 신규 Requirement·Capability·운영기능은 기존 Owner 문서 안에 흡수한다.

---

# 1. 최종 목표 — CPF 전체 기능을 “목적별로 바로 찾고 바로 선택하게” 한다

CPF 공식 산출물의 최종 목표는 책을 만드는 것이 아니다.

Java/Spring 업무개발 경험이 있는 개발자, 운영자, 설계자가 자신의 업무 목적을 가지고 문서를 열었을 때 다음 흐름이 빠르게 성립해야 한다.

> **“내가 이것을 만들거나 운영하려고 한다 → CPF에는 이런 기능이 있다 → 이 기능에서 사용할 API/명령/옵션은 이것들이다 → 일반적으로는 이것을 쓰면 된다 → 내 조건이면 이 옵션을 선택하면 된다 → 최소 사용법은 이렇다 → 더 깊은 설명이 필요한 경우에만 아래 상세를 본다.”**

즉 문서는 순차 학습형 교재가 아니라 **Lookup / Reference 중심의 실무 가이드**다.

독자가 처음부터 끝까지 읽어야만 답을 얻는 문서를 만들지 않는다.

독자가 필요한 절을 찾아 들어왔을 때, **그 절의 첫 화면 또는 첫 구간만 보고도 “기능 존재 여부 / API / 옵션 / 기본 선택 / 내 경우의 선택”까지 상당 부분 결정**할 수 있어야 한다.

상세 설명은 그 다음이다.

---

# 2. “상세한 문서”의 의미를 잘못 해석하지 않는다

CPF에서 “상세하게 작성한다”는 말은 다음 뜻이 아니다.

- 글을 길게 쓴다.
- 교과서처럼 개념부터 설명한다.
- Java/Spring 기본 원리를 다시 가르친다.
- 한 기능에 몇 페이지씩 이론을 쓴다.
- 옵션보다 서술을 늘린다.

CPF 문서에서 “상세”의 의미는 다음과 같다.

- 실제 제공 기능이 빠지지 않는다.
- Public API/Annotation/Method/명령/Property가 빠지지 않는다.
- 주요 옵션이 빠지지 않는다.
- 기본 선택과 조건부 선택이 구분된다.
- 독자가 자신의 경우에 어떤 옵션을 고를지 판단할 수 있다.
- CPF가 자동 처리하는 것과 직접 작성하는 것이 구분된다.
- 대표 실패와 복구가 빠지지 않는다.
- 실제 최소 예가 있다.
- 운영/Trace에서 결과를 어떻게 확인하는지 알 수 있다.
- 금지/오용이 명확하다.

즉:

> **상세 = 많은 문장**
>
> 이 아니라
>
> **상세 = 필요한 선택정보와 실무정보가 빠짐없이 정리된 상태**

다.

---

# 3. 개발 문서는 “Spring 초보 교재”가 아니다

개발 문서의 기본 독자는 다음과 같다.

> **Java/Spring 기반 업무 시스템 개발 경험은 있으나 CPF는 처음 사용하는 실무 개발자**

따라서 다음을 길게 설명하지 않는다.

- Java 문법
- DI/Bean 기초
- REST란 무엇인가
- JPA란 무엇인가
- HTTP 기초
- Transaction의 일반적인 ACID 이론
- SQL 입문
- Spring MVC 입문

이 사람에게 필요한 것은 다음이다.

- CPF에서는 무엇을 쓰는가
- Spring 기본 방식과 CPF 표준 방식의 경계는 어디인가
- 어떤 API가 있는가
- 어떤 옵션이 있는가
- 기본 선택은 무엇인가
- 다른 선택은 언제 필요한가
- CPF가 자동으로 처리하는 것은 무엇인가
- 개발자가 직접 해야 하는 것은 무엇인가
- 실패하면 어떤 의미인가
- 어떻게 확인하는가

예:

나쁜 설명:

> Transaction은 데이터 일관성을 보장하기 위한 기술입니다.

원하는 설명:

> 일반 업무 Service는 REQUIRED를 기본으로 사용한다.  
> 현재 Transaction과 분리해 독립적으로 확정해야 하는 제한적 작업만 REQUIRES_NEW를 검토한다.  
> 조회 전용은 readOnly를 사용한다.  
> 같은 Bean 내부 self-invocation으로 새로운 Transaction 경계를 만들려고 하지 않는다.

---

# 4. 모든 개발 기능은 “빠른 선택”이 먼저다

개발자가 어떤 기능을 찾았을 때 첫 구간에서 다음을 볼 수 있어야 한다.

1. 이 기능에서 무엇을 할 수 있는가
2. 사용할 수 있는 주요 Public API/Annotation/Method
3. 옵션은 무엇인가
4. 기본은 무엇인가
5. 조건이 다를 때 무엇을 선택하는가
6. 간략한 설명
7. 최소 예

그 뒤에만 상세 설명을 둔다.

예:

```text
Persistence 빠른 선택

일반 CRUD
→ CpfCrudRepository

목록 + Paging
→ CpfPagingAndSortingRepository

복잡한 SQL / Mapper 기반 조회
→ CpfSqlSession

Framework 표준 접근으로 해결하기 어려운 제한적 Native 접근
→ CpfJpaNativeAccess
```

이 정도만 봐도 대부분의 개발자는 자신의 출발점을 선택할 수 있어야 한다.

그 뒤에 각 API의:

- 주요 Method
- 옵션
- Transaction 영향
- 최소 코드
- 주의사항

을 제공한다.

---

# 5. 문서 전체의 정보 구조

CPF 개발 관련 문서의 각 대기능은 가능한 한 다음 구조를 따른다.

## 5.1 첫 구간 — 기능 한눈에 보기

- 이 기능으로 할 수 있는 일
- 주요 API / Annotation / Method / 명령
- 주요 옵션
- 기본 선택
- 조건부 선택
- 제한적/고급 선택
- 금지/비권장

## 5.2 두 번째 구간 — 내 경우 무엇을 쓰는가

독자가 조건을 대입해서 선택하도록 한다.

예:

- 일반 CRUD인가?
- Paging이 필요한가?
- 복합 SQL인가?
- Native가 정말 필요한가?

또는:

- 단순 Batch 단위 작업인가?
- 반복 Record 처리인가?
- 동일 JVM 병렬인가?
- 여러 Worker 분산인가?

## 5.3 세 번째 구간 — 짧은 설명

선택한 API/옵션이 무엇을 하는지 1~3문단 정도로 설명한다.

Spring/Java 이론으로 확장하지 않는다.

## 5.4 네 번째 구간 — 최소 실사용 예

실제 CPF Public API 이름을 사용한다.

개발자가 바로 자신의 코드로 변형할 수 있는 최소 예를 제공한다.

## 5.5 다섯 번째 구간 — 깊은 설명이 필요한 항목만 상세

다음처럼 오해/실패 위험이 큰 항목만 더 깊게 설명한다.

- Transaction self-invocation
- Same JVM vs Remote Domain Call
- UNKNOWN
- Retry/Reconcile
- Batch Restart/Fencing
- Remote Partition/Chunk/Step
- Runtime Identity
- Approval/Audit
- Multi-instance
- DB Migration/Rollback

모든 기능을 같은 깊이로 장황하게 설명하지 않는다.

---

# 6. “목적 → CPF 기능”으로 찾게 한다

CPF 문서는 Framework 내부 기능명만 아는 사람을 위한 것이 아니다.

개발자는 보통 다음과 같은 목적을 가지고 문서를 본다.

- 목록 조회를 만들고 싶다.
- 다른 Domain을 호출하고 싶다.
- 외부 REST API를 호출하고 싶다.
- 고정길이 전문을 보내고 싶다.
- 별도 Transaction으로 처리하고 싶다.
- Cache를 적용하고 싶다.
- 메시지를 발행하고 싶다.
- 중복 요청을 막고 싶다.
- 승인 필요한 기능을 만들고 싶다.
- Batch 대량처리를 만들고 싶다.
- 재시작 가능한 Batch를 만들고 싶다.
- Gateway를 사용해야 하는지 판단하고 싶다.

따라서 문서는 “CPF 기능명 → 설명”만 제공하지 않는다.

가능하면 다음 Navigation을 제공한다.

> **하려는 일 → 사용할 CPF Capability → 주요 API/옵션**

이 구조를 README, Developer Guide, Batch Guide, Gateway Guide, 산출물목록 등 역할에 맞게 사용한다.

---

# 7. CPF 전체 Capability를 먼저 Inventory하고 문서화한다

산출물 작성자가 자기 기억으로 주요 기능 몇 개만 선정해서 문서화하지 않는다.

매 산출물 현행화 시 실제 Source에서 CPF가 Public하게 제공하는 기능을 Inventory한다.

최소 다음 범주를 확인한다.

## 7.1 Application / Domain

- Generated Domain
- online
- optional batch
- shared domain
- System Code
- Application Identity
- Runtime Instance
- Feature-first package

## 7.2 Web / Online Transaction

- Controller
- Request/Response
- Validation
- Online Transaction
- Operation ID
- Context

## 7.3 Transaction

- REQUIRED
- REQUIRES_NEW
- readOnly
- rollback
- timeout
- self-invocation

## 7.4 Domain Invocation

- Same JVM
- Remote Domain
- Gateway
- Client
- Context propagation
- Target Operation

## 7.5 Persistence

- CRUD
- Paging
- SQL/Mapper
- Native Access
- Transaction
- Query options

## 7.6 External Integration

- REST
- Fixed-length
- Webhook
- Timeout
- Retry
- UNKNOWN
- Reconcile

## 7.7 Cache

- get
- put
- evict
- get-or-load
- optional
- TTL
- invalidation
- multi-instance behavior

실제 Public Surface에 존재하는 것만 사용한다.

## 7.8 Messaging

- producer
- consumer/listener
- retry
- duplicate
- idempotency
- trace/context

## 7.9 Security / Control

- Authentication
- Permission
- Approval
- Audit
- Idempotency

## 7.10 Logging / Observability

- File Log
- DB Log
- Trace
- Timeline
- Runtime Registry
- Health
- Log Level

## 7.11 Batch

- Tasklet
- Chunk
- LOCAL_PARTITION
- REMOTE_PARTITION
- REMOTE_CHUNK
- REMOTE_STEP
- Restart
- Fencing
- Center-Cut
- Lineage
- Agent
- On-demand
- File
- Large-volume
- Webhook
- Reconcile

## 7.12 Gateway

- Direct
- L4
- Gateway
- L4 + Gateway
- Route
- Filter
- Security
- Context
- Rate Limit
- Timeout
- Observability

## 7.13 DB

- Oracle
- PostgreSQL
- MariaDB
- Schema
- Migration
- Seed
- Install
- Upgrade
- Rollback
- Generator parity

## 7.14 Generator / EDU

- Generator
- Template
- Generated Domain
- Sample
- EDU
- OpenAPI
- Test

## 7.15 Operations

- ADM
- Runtime
- Health
- Trace
- Failure
- Recovery
- Security
- Gateway
- Batch
- Deployment
- Audit

문서 작업자는 이 Inventory를 기반으로 **각 문서 독자에게 필요한 기능을 빠른 선택 구조로 재구성**한다.

---

# 8. 같은 기능은 문서 역할에 따라 다르게 설명한다

같은 기능을 모든 문서에 복사하지 않는다.

예: 외부 REST 호출

## README

독자가:

> “CPF에 외부 REST 연계와 Timeout/Retry/Trace/Reconcile 기능이 있구나.”

를 이해하는 수준.

## 프레임워크 개발자 가이드

독자가:

> “외부 REST를 호출하려면 어떤 Client를 쓰고, Timeout/Retry 옵션 중 내 경우 무엇을 선택해야 하는지”

를 빠르게 선택하는 수준.

## 운영자 매뉴얼

독자가:

> “외부기관 호출 장애가 났을 때 어떤 Trace/상태를 보고 Timeout/UNKNOWN/Retry를 판단하는지”

를 해결하는 수준.

## Gateway 가이드

독자가:

> “이 호출이 Gateway 대상인지 Direct인지”

를 선택하는 수준.

## Specification

정확한 API/Annotation/Property/Default/Error 계약.

## 아키텍처설계서

External Integration Owner와 Domain/Gateway/Core 경계.

## 기술사양서

Call → Timeout → Retry → UNKNOWN → Reconcile Runtime 흐름.

## 기술표준서

Timeout/Retry/Log/Security/Idempotency에 대한 준수 기준.

---

# 9. 공식 사용자 문서 체계는 더 이상 늘리지 않는다

공식 사용자 Surface는 다음 8개로 고정한다.

1. README
2. 프레임워크 개발자 가이드
3. 배치 개발자 가이드
4. 운영자 매뉴얼
5. 배치 운영 가이드
6. Gateway 개발·사용 가이드
7. Specification 기술 명세
8. 설계·표준 산출물 묶음

8번 Surface 내부에는 다음 문서가 있다.

- 아키텍처설계서
- 기술사양서
- 기술표준서
- 데이터베이스표준서
- 산출물목록

새 Requirement가 들어와도 별도 Guide/Manual을 추가하지 않는다.

기존 Owner 문서의 절을 확장한다.

---

# 10. README 작성 지침

## 10.1 대표 독자

CPF Repository를 처음 접하는 Java/Spring 업무 시스템 개발자.

## 10.2 README를 보는 이유

독자는 아직 다음을 모른다.

- CPF가 무엇인지
- Spring Boot와 어떤 관계인지
- 어떤 문제를 해결하는지
- 전체 시스템이 어떻게 구성되는지
- 어떤 기능이 있는지
- 어디서 개발을 시작하는지
- 어떤 Guide를 다음에 봐야 하는지

README는 이 질문을 빠르게 해결하는 **제품 입구**다.

## 10.3 README 최종 상태

README를 훑어본 개발자가 다음을 말할 수 있어야 한다.

> CPF가 무엇인지 알겠다.  
> 어떤 기능들이 있는지 대략 알겠다.  
> 내 업무 Domain이 어디에 있는지 알겠다.  
> Gateway/Batch/ADM의 역할을 알겠다.  
> 내가 뭘 만들려고 할 때 어디서 시작하면 되는지 알겠다.  
> 다음에 어떤 Guide를 보면 되는지 알겠다.

## 10.4 README 필수 내용

### CPF 정의

짧고 정확하게 정의한다.

### CPF가 해결하는 문제

개발자가 느끼는 반복 작업 중심으로 설명한다.

예:

- Transaction
- Context
- Domain Call
- Persistence
- External Integration
- Security
- Logging/Trace
- Batch
- Runtime Operation
- Generator

기능명을 나열한 뒤 1~2문장으로 실제 해결 문제를 붙인다.

### Spring Boot/OSS와의 관계

CPF가 Spring Boot/OSS를 재구현하는 것이 아니라,
그 위에 업무 시스템 공통 계약과 운영 Capability를 제공한다는 점을 명확히 한다.

### 전체 Architecture

**기존 공식 전체 Architecture 원본을 사용하여 현행화한다.**

새 디자인으로 통째로 바꾸지 않는다.

최소 다음이 보여야 한다.

- Mobile App
- Mobile Web
- Customer Web
- Partner/API
- External Channel
- optional Backoffice Web
- Direct/L4/Gateway
- Member/Account/Settlement/External 등 여러 독립 Domain
- Domain별 DB
- Starter/Core/Common/Admin/Batch
- ADM
- External System

Business Domain은 직렬 체인이 아니라 병렬적인 독립 Owner다.

### Architecture 이미지 아래 텍스트 설명 — AI 대응 필수

README의 그림은 사람뿐 아니라 **AI Assistant, Search Engine, Repository Indexer가 읽는 공식 구조 정보**다.

AI가 이미지를 분석하지 못해도 README 본문만으로 CPF를 크게 잘못 설명하지 않도록,
중요 Architecture 그림 아래에 의미를 텍스트로 남긴다.

최소 다음 내용을 텍스트로 설명한다.

- 여러 Channel이 CPF 업무 시스템으로 진입한다.
- 요청은 조건에 따라 Direct/L4 또는 Gateway를 사용할 수 있다.
- Member, Account, Settlement, External 등 Business Domain은 독립적인 Owner다.
- 각 Business Domain은 자신의 DB를 소유한다.
- Domain 간 호출은 공식 Domain Invocation 경계를 사용한다.
- 타 Domain DB 직접 접근은 정상 경로가 아니다.
- `cpf-core`는 topology-independent 핵심 계약을 소유한다.
- Public Capability는 공식 Starter/API를 통해 사용한다.
- ADM은 플랫폼 운영을 담당한다.
- Batch Runtime은 장시간/대량 업무 실행을 담당한다.
- Backoffice는 선택형 업무관리 기능이며 일반 업무개발의 필수 Golden Path가 아니다.

AI/Search가 찾을 수 있도록 주요 Canonical 기술명도 텍스트로 쓴다.

예:

- `X-Transaction-Id`
- `operationId`
- `instanceId`
- `@CpfOnlineTransaction`

단 README를 Specification으로 만들지 않는다.

### 주요 Capability 빠른 보기

“CPF에 무엇이 있나?”를 빠르게 확인할 수 있게 한다.

예:

- Online Transaction
- Domain Invocation
- Persistence
- External Integration
- Cache
- Messaging
- Security/Approval/Audit
- Batch
- Gateway
- Runtime/Trace
- Recovery
- Generator

각 항목에는 간략한 역할과 상세 Guide를 연결한다.

### 목적별 시작

예:

- 일반 업무 API 개발 → Framework Developer Guide
- Batch Job 개발 → Batch Developer Guide
- Gateway 적용 판단 → Gateway Guide
- 운영 장애 확인 → Operator Manual
- 정확한 계약 확인 → Specification

### Golden Path

현재 Source에서 실제 가능한 개발 흐름을 짧게 보여준다.

Generated Domain
→ Feature
→ Starter
→ Online Transaction
→ Test
→ Runtime/Trace

개발 중 기능을 이미 존재하는 Golden Path처럼 쓰지 않는다.

---

# 11. 프레임워크 개발자 가이드 작성 지침

## 11.1 대표 독자

Java/Spring 업무개발 경험은 있지만 CPF로 처음 실제 Business 기능을 만드는 개발자.

## 11.2 이 문서의 목적

“CPF를 공부한다”가 아니라:

> **내가 지금 만들려는 기능에 어떤 CPF 기능/API/옵션을 쓰면 되는가**

를 빠르게 찾는 문서다.

## 11.3 문서 첫 부분 — 목적별 Quick Finder

문서 초반에서 개발 목적별 기능을 바로 찾게 한다.

예:

- REST 업무 API를 만든다
- 목록/Paging을 만든다
- 다른 Domain을 호출한다
- 외부 REST를 호출한다
- Fixed-length 전문을 처리한다
- Transaction을 분리한다
- Cache를 사용한다
- 메시지를 발행/수신한다
- 권한을 건다
- 승인 기능을 적용한다
- 중복 요청을 막는다
- Trace를 확인한다
- 복구/Reconcile이 필요한 업무를 만든다

각 항목은 해당 절로 바로 연결한다.

## 11.4 API / Annotation / Method Summary

개발자가 “무슨 기능이 있는가”를 한눈에 볼 수 있어야 한다.

예:

```text
Online Transaction
- @CpfRestController
- @CpfOnlineTransaction

Transaction
- @CpfTransactional
  - REQUIRED
  - REQUIRES_NEW
  - readOnly

Persistence
- CpfCrudRepository
- CpfPagingAndSortingRepository
- CpfSqlSession
- CpfJpaNativeAccess
```

실제 최신 Source의 Public Surface만 사용한다.

## 11.5 Workspace / Generated Domain

개발자가 알아야 할 만큼만 설명한다.

- Generated Domain의 역할
- online
- optional batch
- shared domain
- Feature-first package
- Generator 관리영역
- 개발자 작성영역
- System Code

Spring Project 구조 일반론을 가르치지 않는다.

## 11.6 Starter / Profile

Starter 목록을 단순 나열하지 않는다.

“하려는 일 → 필요한 Starter/Capability”로 정리한다.

각 Starter에:

- 제공 기능
- 기본/선택
- 함께 필요한 Starter
- 직접 사용 금지 Internal 영역

을 간략하게 제공한다.

## 11.7 Online Transaction

먼저 한눈에 보기.

- Controller API
- Transaction Annotation
- Operation ID
- Validation
- Context

그 다음 최소 실제 기능 예를 제공한다.

한 예제에서:

- Request DTO
- Controller
- Service
- Transaction
- Repository
- Response
- Test

까지 연결한다.

## 11.8 Canonical Context

Header 이론을 길게 쓰지 않는다.

개발자 관점에서 먼저 답한다.

- 내가 Header를 직접 넣는가? → 아니다.
- 최초 Transaction ID는 누가 만든다?
- Same JVM에서는 어떻게 전달된다?
- Remote 호출에서는 어떻게 전달된다?
- Caller/Target은 언제 바뀌는가?
- Original System은 왜 바뀌지 않는가?

정확한 Canonical 이름을 제공한다.

현재 기준:

- `X-Transaction-Id`
- `X-Original-System-Code`
- `X-System-Code`
- `X-Caller-System-Code`
- `X-Target-System-Code`
- `X-Target-Operation-Id`

Channel을 Canonical 6에 섞지 않는다.

깊은 설명이 필요한 Same JVM/Remote 흐름만 그림으로 설명한다.

## 11.9 Transaction

첫 화면에서 바로 선택한다.

```text
일반 업무
→ REQUIRED

조회 전용
→ readOnly

현재 Transaction과 분리해 독립 확정
→ REQUIRES_NEW

같은 Bean 내부에서 새 Transaction 경계 필요
→ self-invocation 사용 금지, Bean 경계 분리
```

그 뒤에 각 옵션을 간략 설명한다.

깊은 설명은:

- self-invocation
- rollback
- timeout
- 외부 호출과 DB Transaction 장기 유지 위험

같이 실수 가능성이 높은 항목만 제공한다.

## 11.10 Domain Invocation

먼저 선택표.

```text
같은 JVM의 다른 Domain
→ 공식 Same-JVM Domain Invocation

별도 WAS/MSA Domain
→ 공식 Remote Domain Invocation

외부기관/외부서비스
→ External Client

자기 자신을 HTTP로 다시 호출
→ 금지
```

반드시 설명:

- Same JVM
- Remote
- Gateway
- External
- Context propagation
- 타 Domain DB 직접 접근 금지
- Internal package 직접 참조 금지
- self HTTP 금지

## 11.11 Persistence

빠른 선택이 핵심이다.

```text
일반 CRUD
→ CpfCrudRepository

목록/Paging
→ CpfPagingAndSortingRepository

복잡한 SQL/Mapper
→ CpfSqlSession

제한적 Native
→ CpfJpaNativeAccess
```

각 API에 대해:

- 대표 Method
- 옵션
- 언제 사용
- 언제 사용하지 말 것
- 최소 코드
- Transaction 영향

만 우선 제공한다.

복잡한 Native/JPA 내부 동작은 필요한 경우에만 상세.

## 11.12 External Integration

목적별 빠른 선택을 제공한다.

- REST
- Fixed-length
- Webhook
- Timeout
- Retry
- UNKNOWN
- Reconcile

외부 연계에서는 다음은 반드시 더 깊게 설명한다.

- Timeout
- 처리결과 UNKNOWN
- Retry 가능/불가능
- Idempotency
- Reconcile

성공 예제만 넣지 않는다.

## 11.13 Cache

실제 Public API를 빠르게 보여준다.

예:

- get
- put
- evict
- getOrLoad
- optional 처리
- TTL

실제 Source에 존재하는 항목만 쓴다.

그 다음:

- 일반 조회 cache
- cache-aside/get-or-load
- invalidation
- multi-instance

의 선택만 간략하게 설명한다.

## 11.14 Messaging

빠른 API 목록 + 선택 설명.

- publish/send
- listener
- retry
- duplicate
- idempotency
- trace/context

Messaging 이론은 쓰지 않는다.

## 11.15 Security / Approval / Audit

기능별로 빠르게 구분한다.

```text
사용자 인증
→ Authentication

기능 접근통제
→ Permission

위험 업무 사전 승인
→ Approval

행위 및 결과 추적
→ Audit
```

실제 Annotation/API가 있다면 함께 보여준다.

깊은 설명은:

- Backend 강제 통제
- 화면 버튼 숨김과 권한 차이
- 위험 조치 승인
- Audit 결과

정도에 집중한다.

## 11.16 Runtime / Trace

개발자가 “내 요청이 어디서 어떻게 처리됐나”를 빠르게 찾게 한다.

주요 식별자:

- transactionId
- operationId
- systemCode
- instanceId
- hostName
- traceId

각 값이 무엇인지 한 줄 설명.

그 뒤 실제 Trace/ADM에서 확인하는 경로를 제공한다.

## 11.17 Failure / Recovery

개발자가 자주 만나는 실패를 빠르게 찾게 한다.

- Validation
- Business rejection
- Permission
- Conflict
- Timeout
- 429
- 500
- 503
- UNKNOWN
- Retry
- Reconcile

각 실패에:

- 의미
- 재시도 가능 여부
- 개발자 대응
- 운영 확인

을 짧게 제공한다.

## 11.18 Test

기능별 최소 확인 항목을 제공한다.

- 정상
- Validation
- Permission
- Transaction
- Context
- Failure
- Trace

테스트 프레임워크 교재를 쓰지 않는다.

## 11.19 Developer Guide 완료 기준

개발자가 기능 목적을 가지고 Guide를 열었을 때:

1. 기능이 있는지 바로 찾는다.
2. API/옵션을 한눈에 본다.
3. 자신의 경우 어떤 옵션인지 선택한다.
4. 짧은 설명을 읽는다.
5. 최소 예를 보고 개발을 시작한다.
6. 복잡한 경우만 상세 설명을 읽는다.

이 흐름이 안 되면 실패다.

---

# 12. 배치 개발자 가이드 작성 지침

## 12.1 대표 독자

CPF에서 Batch Job을 실제 구현하려는 Java/Spring Batch 경험 개발자.

## 12.2 핵심 목적

“배치 이론을 배우는 것”이 아니라:

> **내 업무에 어떤 Batch 처리모델을 쓰면 되는가**

를 빠르게 결정하는 것.

## 12.3 첫 화면 — 처리모델 Quick Select

```text
단순 단위 작업
→ Tasklet

대량 Record 반복
→ Chunk

동일 Runtime 병렬
→ LOCAL_PARTITION

여러 Worker 분산
→ REMOTE_PARTITION

Item 단위 원격 분산
→ REMOTE_CHUNK

Step 자체 원격 실행
→ REMOTE_STEP
```

그 뒤 비교표에서:

- 적합 업무
- 데이터 규모
- 병렬성
- Worker
- Transaction/Commit
- Restart 단위
- 운영 복잡도

를 비교한다.

## 12.4 각 모델의 설명

각 모델마다:

- 언제 쓰는가
- 대표 API/구성요소
- 주요 옵션
- 최소 예
- 실패 시 의미
- Restart 방식

까지만 먼저 설명한다.

깊은 설명은 필요한 모델에만 한다.

## 12.5 반드시 깊게 설명할 것

- Restart
- Fencing
- Worker death
- Process Kill
- REMOTE_PARTITION vs REMOTE_CHUNK vs REMOTE_STEP
- UNKNOWN
- Reconcile
- Center-Cut
- Lineage

이 항목은 운영 오류 위험이 크므로 그림/비교표를 적극 사용한다.

---

# 13. 운영자 매뉴얼 작성 지침

## 13.1 대표 독자

ADM을 실제로 사용하는 시스템 운영자.

## 13.2 핵심 목적

메뉴 사용법을 공부하는 문서가 아니다.

> **지금 발생한 운영 목적/문제를 해결하기 위해 무엇을 보고 무엇을 해야 하는가**

를 빠르게 찾는 Runbook형 매뉴얼이다.

## 13.3 첫 부분 — 목적별 Quick Finder

예:

- 시스템 정상여부 확인
- 특정 Transaction 추적
- 오류 거래 확인
- Instance 상태 확인
- Log Level 변경
- Gateway 장애 확인
- Deployment 상태 확인
- Permission 확인
- 위험조치/Approval
- Audit 확인
- Recovery/Reconcile

각 목적을 해당 절로 연결한다.

## 13.4 각 운영 기능

먼저:

- 무엇을 보는가
- 정상은 무엇인가
- 이상은 무엇인가
- 어떤 조치를 할 수 있는가

를 짧게 제공한다.

그 뒤 상세 Runbook이 필요한 장애만 자세히 설명한다.

## 13.5 장애 Runbook

다음 구조:

> 증상 → 첫 확인 → 원인 후보 → 허용 조치 → 위험/승인 → 결과 확인

운영 교과서가 아니라 실제 조치에 필요한 정보만 제공한다.

---

# 14. 배치 운영 가이드 작성 지침

## 14.1 대표 독자

Batch Runtime을 운영하는 담당자.

## 14.2 핵심 목적

> **현재 Batch 상태에서 어떤 조치를 선택해야 하는가**

를 빠르게 판단한다.

## 14.3 Quick Finder

- Start
- Stop
- Restart
- Reprocess
- Reconcile
- Worker failure
- Scheduler failure
- UNKNOWN
- Retention
- Center-Cut
- Lineage
- Agent

## 14.4 Restart / Reprocess / Reconcile

반드시 한눈에 비교되게 한다.

각각:

- 목적
- 언제 사용
- 데이터 영향
- 중복 위험
- 권한/승인
- 결과 확인

을 제공한다.

## 14.5 깊은 설명 대상

- Fencing
- stale owner
- Worker death
- Process Kill
- Multi-instance
- UNKNOWN
- Center-Cut resume

---

# 15. Gateway 개발·사용 가이드 작성 지침

## 15.1 대표 독자

Channel과 Domain 연결을 구현하는 개발자.

## 15.2 첫 질문

> Gateway가 필요한가?

## 15.3 첫 화면 Quick Select

```text
단순 Direct 공식 호출
→ Direct

Load Balancing만 필요
→ L4

공통 인증/라우팅/Rate Limit/관측 필요
→ Gateway

L4 + 공통 Gateway 기능
→ L4 + Gateway
```

실제 Architecture/Source 기준으로 조건을 재검증한다.

## 15.4 반드시 제공할 선택 기준

- Security
- Routing
- Context
- Rate Limit
- Observability
- 운영 복잡도
- 장애 지점

## 15.5 Gateway 기능 빠른 목록

- Route
- Filter
- Authentication
- Authorization
- Context
- Rate Limit
- Timeout
- Retry
- Health
- Trace

각 기능은 API/Config/옵션을 빠르게 보여주고 짧게 설명한다.

복잡한 Route/Context/Failure만 상세로 내려간다.

---

# 16. Specification 기술 명세 작성 지침

## 16.1 대표 독자

정확한 CPF Public 계약을 확인하려는 개발자.

## 16.2 핵심 목적

> **Source를 열지 않고 API/Annotation/Property/Header/Error의 정확한 계약을 확인한다.**

## 16.3 검색 중심

사용자가 Class/Annotation/Property/Header 이름으로 바로 찾을 수 있어야 한다.

## 16.4 Public API

각 API에는 가능한 한:

- Canonical Name
- Fully Qualified Name
- Owner Module
- 역할
- 언제 사용
- Signature
- 주요 Method
- Parameter
- Return
- Default
- Failure
- Context 영향
- Same JVM/Remote
- 금지 사용
- 최소 예

를 제공한다.

## 16.5 Annotation

- 적용 대상
- 필수 Attribute
- 옵션 Attribute
- Default
- Runtime 영향
- 잘못된 조합
- Failure

## 16.6 Config

- key
- type
- default
- required
- environment override
- runtime effect
- invalid value behavior

## 16.7 Canonical Header

Header 6개는 정확한 생성/변경/검증 Lifecycle을 정의한다.

## 16.8 Runtime Identity / Operation ID

`instanceId`, `systemCode`, `application`, `hostName`, `operationId`의 차이를 정확히 설명한다.

---

# 17. 아키텍처설계서 작성 지침

## 17.1 대표 독자

기능 Owner와 Dependency를 결정하는 기술 설계자.

## 17.2 핵심 목적

> **이 기능은 어디에 있어야 하며, 누가 소유하고, 무엇을 참조할 수 있는가**

를 빠르게 판단한다.

## 17.3 전체 Architecture

기존 공식 전체 Architecture 원본을 현행화한다.

새 그림으로 통째로 바꾸지 않는다.

## 17.4 Module별 빠른 이해

각 Module마다:

- 역할
- 소유하는 기능
- 소유하지 않는 기능
- Public Surface
- Consumer
- 허용 Dependency
- 금지 Dependency
- 배포 위치

를 한눈에 볼 수 있게 한다.

그 뒤 복잡한 Ownership/Topology만 상세 설명한다.

## 17.5 반드시 다룰 경계

- `cpf-core`
- `cpf-common`
- `cpf-admin`
- Backoffice/BZA 계열
- `cpf-batch`
- Gateway
- Starter/Provider
- Generated Domain

## 17.6 Topology

- Same JVM
- Separate WAS
- MSA
- Multi-instance
- L4
- Gateway

에서 변하는 것과 변하지 않는 계약을 설명한다.

---

# 18. 기술사양서 작성 지침

## 18.1 대표 독자

CPF Runtime의 동작과 복잡한 장애를 이해해야 하는 시니어 개발자.

## 18.2 핵심 목적

기능 목록이 아니라 **실제 처리 흐름**을 확인한다.

## 18.3 Quick Flow

각 주요 Runtime 기능은 먼저 짧은 Flow로 보여준다.

예:

Request
→ Context
→ Validation
→ Security
→ Operation
→ Transaction
→ Service
→ Persistence/Domain/External
→ Response
→ Trace

그 뒤 복잡한 항목만 Sequence/State로 상세 설명한다.

## 18.4 깊게 설명할 대상

- Same JVM vs Remote
- Context lifecycle
- Transaction lifecycle
- Retry/Timeout
- UNKNOWN
- Reconcile
- Multi-instance
- Runtime registration
- Operation discovery
- Batch Fencing

---

# 19. 기술표준서 작성 지침

## 19.1 대표 독자

CPF 기반 업무 코드를 작성하는 개발자.

## 19.2 핵심 목적

> **내 코드가 CPF 표준에 맞는가**

를 빠르게 확인한다.

## 19.3 각 표준의 내용

먼저 짧게:

- 기본
- 허용 옵션
- 금지
- 예외

를 보여준다.

그 뒤 이유가 필요한 항목만 설명한다.

## 19.4 주요 표준 영역

- Package
- Naming
- Feature-first
- Annotation
- Base
- Controller
- Service
- Repository
- Transaction
- Domain Invocation
- External Integration
- Context
- Logging
- Error
- Security
- Approval
- Audit
- Config
- Test
- Generator
- OpenAPI
- Frontend Generated Client
- Dependency
- Ownership

## 19.5 Spring Native 경계

CPF API가 있는 영역에서 왜 CPF 표준을 쓰는지,
Spring Native를 직접 사용해도 되는 영역과 고급 사용 책임을 구분한다.

Spring 사용 자체를 금지하는 문서처럼 쓰지 않는다.

---

# 20. 데이터베이스표준서 작성 지침

## 20.1 대표 독자

CPF 업무 DB를 설계·변경하는 개발자.

## 20.2 핵심 목적

> **DB 변경 하나를 CPF 전체 자산에 안전하게 반영하려면 무엇을 같이 바꿔야 하는가**

를 빠르게 확인한다.

## 20.3 빠른 변경 가이드

예:

```text
Table 추가
→ Canonical Schema
→ Oracle/PostgreSQL/MariaDB
→ Migration
→ Install/Upgrade
→ Rollback
→ Generator
→ Test

Column 추가
→ Null/Default/Compatibility 확인
→ 3 Vendor
→ Migration
→ Rollback
→ Generated Asset
→ Test
```

## 20.4 공식 Vendor

- Oracle
- PostgreSQL
- MariaDB

다른 Vendor를 공식 표준/증적에 섞지 않는다.

## 20.5 깊은 설명 대상

- migration
- rollback
- vendor parity
- lock/concurrency
- data migration
- generator parity

---

# 21. 산출물목록 작성 지침

## 21.1 대표 독자

CPF 문서 중 무엇을 봐야 하는지 모르는 사용자.

## 21.2 핵심 목적

파일 목록이 아니라 **질문 → 문서** Navigation Map이다.

예:

- “일반 업무기능을 개발한다” → Framework Developer Guide
- “Batch 처리모델을 고른다” → Batch Developer Guide
- “운영 장애를 추적한다” → Operator Manual
- “Restart/Reconcile을 판단한다” → Batch Operations Guide
- “Gateway가 필요한지 판단한다” → Gateway Guide
- “정확한 API/Header 계약을 찾는다” → Specification
- “기능 Owner를 결정한다” → Architecture
- “Runtime 내부 흐름을 본다” → Technical Specification
- “코딩 표준을 확인한다” → Technical Standard
- “DB 변경 영향 범위를 본다” → DB Standard

---

# 22. 그림 사용 지침 — 의미 전달 기준

그림은 장식이 아니다.

텍스트보다 관계를 빠르게 이해시키는 경우에만 쓴다.

적합한 대상:

- 전체 Architecture
- Same JVM vs Remote
- Gateway Topology
- Domain 관계
- Transaction boundary
- External Retry/UNKNOWN/Reconcile
- Batch Partition/Remote Processing
- Scheduler/Worker
- Restart/Fencing
- Runtime Identity/Trace

중요 그림은 바로 아래에 **핵심 의미 텍스트**를 둔다.

특히 README는 AI 대응을 위해 필수다.

---

# 23. 표 사용 지침 — 빠른 선택 기준

표는 문장을 담는 장식이 아니라 **비교와 선택을 빠르게 하는 도구**다.

적합:

- API 옵션
- Transaction 옵션
- Persistence API
- Batch 처리모델
- Gateway Topology
- 상태/오류
- Vendor 차이
- 기본/조건부/금지

표를 본 뒤 개발자가 선택을 끝낼 수 있어야 한다.

단순 문서 링크, 한두 문장 설명, 의미 없는 2열 표는 사용하지 않는다.

---

# 24. 예제 사용 지침

핵심 개발 기능에는 최소 예가 필요하다.

좋은 예:

- 실제 CPF Public API
- 실제 Annotation
- 실제 옵션
- 짧은 업무 구조
- 바로 응용 가능

나쁜 예:

- Java/Spring 입문용 Hello World
- 실제 CPF 계약과 무관한 장난감 코드
- 코드만 길고 선택 이유가 없는 예

깊은 예제는 복잡한 항목에만 둔다.

---

# 25. 개발 중 기능 문서화

아직 Source에 없는 개발 중 Requirement를 현재 제공 기능처럼 공식 문서에 쓰지 않는다.

반대로 사용자가 “개발 중이라 현재 Source 미구현을 QA FAIL로 보지 말라”고 한 항목은 현재 문서 QA 결함으로 잘못 판정하지 않는다.

구현이 Source에 들어오면 기존 Owner 문서에 흡수한다.

새 문서를 만들지 않는다.

---

# 26. 기존 좋은 문서/그림 보호

현행화가 “전면 재작성”을 의미하지 않는다.

기존 공식 산출물의:

- 좋은 구조
- 충분한 정보량
- 좋은 그림
- 좋은 표
- 익숙한 Navigation

은 유지한다.

최신 Source/Steering과 충돌하는 내용만 고치고,
부족한 옵션/선택정보/실무설명을 보강한다.

특히 전체 Architecture는 기존 공식 그림을 기준으로 현행화한다.

---

# 27. Source 정합성

문서 작성자는 과거 기억만으로 작성하지 않는다.

관련 Source를 확인한다.

최소:

- Public API
- Annotation
- Starter/Profile
- Config
- Header/Context
- Runtime Identity
- Operation ID
- DB
- Migration
- Generator
- Generated Domain
- EDU
- ADM
- Backoffice/BZA
- Gateway
- Batch
- OpenAPI
- Frontend Generated Client
- Test

Framework 계약이 바뀌면 관련 문서들을 함께 점검한다.

---

# 28. README AI/Search 품질 Gate

README에서 중요한 Architecture 정보는 이미지에만 존재하면 안 된다.

AI가 텍스트만 읽어도 최소 다음을 설명할 수 있어야 한다.

- CPF의 정체성
- Spring Boot/OSS와 관계
- Channel
- Gateway/L4
- Business Domain
- Domain DB Ownership
- Core/Starter
- ADM
- Batch
- Backoffice 선택성
- Domain Invocation
- Canonical Transaction/Operation 개념

Canonical 기술명은 검색 가능한 텍스트로 포함한다.

---

# 29. 개발자 Guide 품질 Gate

개발자가 목적을 가지고 들어왔을 때:

- 어떤 기능이 있는지 찾을 수 있는가
- API/Annotation/Method 목록이 있는가
- 옵션 전체가 보이는가
- 기본 선택이 보이는가
- 자기 조건에 맞는 옵션을 빠르게 고를 수 있는가
- 짧은 설명이 있는가
- 최소 예가 있는가
- 복잡한 부분만 상세 설명되는가

하나라도 안 되면 Guide가 아니다.

---

# 30. 운영 문서 품질 Gate

운영자가 상황을 가지고 들어왔을 때:

- 어디를 보는지
- 정상인지
- 이상인지
- 어떤 조치가 가능한지
- 위험/승인이 있는지
- 결과를 어디서 확인하는지

를 빠르게 찾을 수 있어야 한다.

메뉴 설명만 있는 문서는 실패다.

---

# 31. Architecture/Specification 품질 Gate

설계자는:

- Owner
- Boundary
- Dependency
- Topology

를 빠르게 결정할 수 있어야 한다.

Specification 사용자는:

- API
- Annotation
- Property
- Header
- Status
- Error

의 정확한 계약을 Source를 열지 않고 확인할 수 있어야 한다.

---

# 32. 문서 작성 전 내부 설계 체크

작성자는 목차부터 만들지 않는다.

문서마다 먼저 다음을 정리한다.

1. 대표 독자
2. 독자가 문서를 여는 실제 목적
3. 목적별 질문 목록
4. CPF 기능 Inventory
5. 목적 → Capability Mapping
6. API/Annotation/Method/명령 목록
7. 옵션 목록
8. 기본 선택
9. 조건부 선택
10. 제한적/고급 선택
11. 금지
12. 최소 예
13. 깊은 설명이 필요한 항목
14. 필요한 비교표
15. 필요한 흐름 그림
16. Source 근거

이걸 먼저 잡아야 “교과서형 기능 설명”으로 흐르지 않는다.

---

# 33. 최종 Acceptance Checklist

## 목적성

- 독자가 “무엇을 만들려고” 문서를 보는지가 명확한가?
- 그 목적에서 바로 CPF 기능을 찾을 수 있는가?

## 발견성

- 기능 존재 여부를 빠르게 확인할 수 있는가?
- API/명령/옵션을 한눈에 볼 수 있는가?

## 선택성

- 기본 선택이 명확한가?
- 조건부 옵션이 명확한가?
- “상황에 맞게 선택” 같은 모호한 문장이 없는가?

## 정보량

- 옵션이 빠지지 않았는가?
- 개발자가 실제로 필요한 간략 설명이 있는가?
- 최소 예가 있는가?
- 실패/복구가 필요한 항목은 빠지지 않았는가?

## 깊이

- 모든 기능을 과도하게 장문으로 설명하지 않았는가?
- 오해/장애 위험이 큰 항목만 깊게 설명했는가?

## 표

- 비교/선택이 필요한 곳에 표가 있는가?
- 표를 보고 선택을 끝낼 수 있는가?

## 그림

- 복잡한 관계에 그림이 있는가?
- 기존 좋은 Architecture를 보존했는가?
- 중요 그림 아래 의미 설명이 있는가?

## AI

- README 텍스트만 읽어도 Architecture를 이해할 수 있는가?
- Canonical 이름이 텍스트에 남아 있는가?

## 실무성

- Developer Guide 사용자는 바로 개발을 시작할 수 있는가?
- Batch Guide 사용자는 모델을 선택할 수 있는가?
- Operator는 실제 장애를 판단할 수 있는가?
- Gateway 사용자는 Topology를 고를 수 있는가?
- Specification 사용자는 정확한 계약을 찾을 수 있는가?
- Architecture 사용자는 Owner를 결정할 수 있는가?
- DB Standard 사용자는 변경 영향 범위를 바로 알 수 있는가?

---

# 34. 최종 목표 문장

CPF 공식 사용자 산출물의 최종 목표는 다음과 같다.

> **CPF가 실제로 제공하는 전체 Capability를 누락 없이 확인하고, 각 공식 산출물의 대표 독자가 자신의 업무 목적을 가지고 문서를 열었을 때 필요한 CPF 기능을 즉시 발견하며, 제공되는 Public API·Annotation·Method·명령·옵션을 한눈에 확인하고, 기본 선택과 조건별 다른 선택을 빠르게 판단해 바로 개발·운영·설계할 수 있게 한다.**

> **문서는 순차 학습용 교재가 아니다. Java/Spring 업무 경험이 있는 개발자가 “이 기능을 만들려면 이런 CPF 기능과 옵션이 있구나. 내 경우에는 이 API/옵션을 쓰면 되겠네.”라고 빠르게 판단할 수 있는 실무 Lookup/Reference 가이드여야 한다.**

> **간략한 설명과 최소 실사용 예를 기본으로 하고, Transaction self-invocation, Same JVM/Remote 호출, UNKNOWN/Reconcile, Batch Restart/Fencing, Multi-instance처럼 오해와 운영 위험이 큰 항목만 별도로 깊게 설명한다.**

> **README는 사람뿐 아니라 AI/Search가 Repository를 읽을 때도 CPF 전체 Architecture와 Canonical 용어를 정확하게 이해할 수 있도록, 중요한 그림 아래 핵심 의미를 텍스트로 함께 보존한다.**

> **새 Requirement가 생겨도 공식 사용자 문서를 늘리지 않고 기존 Owner 문서 안에 흡수하여, 사용자가 문서 위치를 다시 학습하지 않아도 되게 한다.**

이 목표를 충족하지 못하면 산출물이 존재하거나 내용이 많이 들어 있다는 이유만으로 완료 처리하지 않는다.
