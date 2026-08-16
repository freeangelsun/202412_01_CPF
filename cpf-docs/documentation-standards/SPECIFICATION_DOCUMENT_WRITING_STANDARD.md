# 스펙 기술문서 작성 가이드

## 1. 문서 목적

스펙 기술문서는 단순히 기능을 소개하는 문서가 아니다.

문서를 처음 보는 개발자가 별도의 구두 설명 없이도 다음을 판단할 수 있어야 한다.

- 이 기능이 무엇인지
- 왜 존재하는지
- 언제 사용하는지
- 어떤 Module과 Component가 담당하는지
- 어떤 API·Annotation·Configuration·Command·SQL을 사용하는지
- 입력하면 내부에서 어떤 순서로 동작하는지
- 정상 상황에서는 어떤 결과가 나오는지
- 잘못 사용하면 어떤 오류가 발생하는지
- 다른 기능과 어떻게 연결되는지
- Transaction·Async·Batch·Cache·Security 등이 어떻게 영향을 주는지
- 어떤 설정을 변경할 수 있는지
- 어떤 값을 변경하면 어떤 영향이 생기는지
- 확장 가능한 부분과 변경하면 안 되는 부분이 무엇인지
- 실제 적용 예제가 무엇인지
- 어떻게 Test하고 확인하는지
- Version 변경 시 무엇이 달라졌는지

따라서 스펙 문서는 **개념 설명 + 사용 계약 + 동작 규칙 + 참조 정보 + 예제 + 검증 방법**을 하나의 체계로 제공해야 한다.

---

# 2. 기본 작성 원칙

## 2.1 Source와 일치해야 한다

문서 내용은 실제 Source를 기준으로 작성한다.

확인 대상:

- Public Class
- Interface
- SPI
- Annotation
- Method
- DTO
- Enum
- Exception
- Configuration Property
- Spring Bean
- Filter / Interceptor
- Scheduler
- Batch
- SQL
- Table
- REST API
- CLI / Script
- Frontend 호출
- Sample
- Test

문서에 존재하지만 Source에 없는 기능을 작성하지 않는다.

Source에는 있지만 문서에 빠져 있는 공개 사용 기능도 확인한다.

---

## 2.2 기능 목록 나열로 끝내지 않는다

잘못된 예:

| 기능설명        |                |
| ----------- | -------------- |
| Transaction | Transaction 지원 |
| Retry       | Retry 지원       |
| Cache       | Cache 지원       |

이 정도만으로는 실제 개발에 사용할 수 없다.

다음 수준까지 작성해야 한다.

| 항목작성 내용 |                                 |
| ------- | ------------------------------- |
| 기능      | Transaction                     |
| 사용 목적   | 하나의 업무 처리 단위를 원자적으로 처리          |
| 사용 API  | `@Transactional`                |
| 적용 위치   | Service Layer                   |
| 기본 동작   | RuntimeException 발생 시 Rollback  |
| 주요 옵션   | propagation, isolation, timeout |
| 연계 기능   | Retry, Event, Outbox            |
| 주의사항    | 동일 Class 내부 호출 시 Proxy 적용 여부 확인 |
| 예제      | 실제 Service 코드                   |
| 오류 상황   | TransactionRequiredException 등  |
| 확인 방법   | Test 및 Log 확인                   |

---

# 3. 문서 전체 권장 구조

스펙 문서는 다음 순서로 구성한다.

1. 문서 개요
2. 기능 한눈에 보기
3. Architecture 위치
4. 용어 및 핵심 개념
5. 기능 목록
6. Public API Summary
7. Configuration Summary
8. 동작 방식
9. 상세 기능 Specification
10. API / Interface Specification
11. Annotation Specification
12. Configuration Specification
13. Command / Script Specification
14. Data / DB Specification
15. Transaction Specification
16. Async / Event Specification
17. Error Handling Specification
18. Security 관련 Specification
19. 확장 Point
20. Integration Specification
21. Runtime 동작
22. 주요 Sequence
23. 사용 예제
24. 잘못된 사용 예제
25. Test 및 검증 방법
26. 제한사항
27. 호환성
28. 변경 이력
29. 관련 문서
30. Reference Index

기능 특성상 해당하지 않는 장은 생략할 수 있지만, **단순히 내용이 없다는 이유로 누락해서는 안 된다.**

---

# 4. 문서 개요

문서 첫 부분에서는 독자가 문서의 범위를 즉시 이해할 수 있도록 한다.

## 필수 정보

| 항목내용       |                                       |
| ---------- | ------------------------------------- |
| 문서명        | 기능 또는 Component 이름                    |
| 대상 Module  | 실제 Module                             |
| 대상 Package | 주요 Package                            |
| 주요 기능      | 기능 요약                                 |
| 대상 독자      | Framework 사용자 / 확장 개발자 / Maintainer 등 |
| 선수 지식      | 필요한 사전 개념                             |
| 관련 기능      | 연계 Component                          |
| 기준 Version | 문서가 설명하는 Version                      |
| 기준 Source  | 가능하면 Tag 또는 Commit                    |
| 관련 문서      | Architecture / Developer Guide 등      |

---

# 5. 기능 한눈에 보기

문서 초반에 반드시 Summary를 제공한다.

독자가 전체 문서를 읽기 전에 사용할 기능을 빠르게 찾을 수 있어야 한다.

## 5.1 기능 Summary 표

| 기능목적주요 API설정관련 기능상세 |                |                  |                 |             |     |
| ------------------- | -------------- | ---------------- | --------------- | ----------- | --- |
| Transaction         | Transaction 처리 | `@Transactional` | `transaction.*` | Retry       | §8  |
| Retry               | 재시도 처리         | `RetryTemplate`  | `retry.*`       | Transaction | §9  |
| Event               | Event 발행       | `EventPublisher` | `event.*`       | Async       | §10 |

---

# 6. 사용 가능한 API Summary

API를 문서 여러 곳에 흩어놓지 않는다.

먼저 전체 목록을 제공하고 상세 설명으로 이동할 수 있게 한다.

## 권장 표

| 종류이름Package용도주요 입력반환상세 |                      |                  |                |               |        |      |
| ---------------------- | -------------------- | ---------------- | -------------- | ------------- | ------ | ---- |
| Interface              | `TransactionManager` | `...transaction` | Transaction 제어 | Context       | Result | §8.2 |
| Annotation             | `@Transactional`     | `...annotation`  | Transaction 선언 | propagation 등 | -      | §8.3 |
| Class                  | `RetryTemplate`      | `...retry`       | Retry 실행       | Callback      | Result | §9   |
| Enum                   | `Propagation`        | `...transaction` | 전파 속성          | -             | -      | §8.4 |

API가 많으면 다음과 같이 종류별로 나눈다.

- Interfaces
- Classes
- Annotations
- Enums
- Exceptions
- DTOs
- SPI
- Utility
- Commands

---

# 7. API 하나를 설명하는 표준 형식

각 Public API는 동일한 형식으로 설명한다.

## 7.1 기본 정보

### `TransactionManager`

**Package**

```
com.example.transaction

```

**역할**

Transaction을 시작하고 Commit 또는 Rollback을 제어한다.

### 사용 위치

- Service
- Framework 내부 Transaction 처리
- Custom Integration Component

### 사용하면 안 되는 위치

- Controller에서 직접 Transaction을 제어해야 할 특별한 이유가 없는 경우
- 단순 DTO
- Domain 객체

---

## 7.2 Method Specification

| Method설명ParameterReturnException |                |         |                    |                      |
| -------------------------------- | -------------- | ------- | ------------------ | -------------------- |
| `begin()`                        | Transaction 시작 | 없음      | TransactionContext | TransactionException |
| `commit(ctx)`                    | Commit         | Context | void               | CommitException      |
| `rollback(ctx)`                  | Rollback       | Context | void               | RollbackException    |

---

## 7.3 Parameter 상세

| ParameterType필수기본값허용값설명 |           |   |         |        |                 |
| ----------------------- | --------- | - | ------- | ------ | --------------- |
| timeout                 | int       | N | 30      | 1\~300 | Timeout 초       |
| isolation               | Isolation | N | DEFAULT | Enum   | Isolation Level |

단순히 Type만 적지 않는다.

반드시 다음을 확인한다.

- nullable 여부
- 기본값
- 범위
- 허용 값
- 단위
- 자동 변환 여부
- 검증 규칙

---

# 8. Annotation Specification

Annotation이 존재하면 별도 Summary를 작성한다.

| Annotation적용 대상목적주요 속성 |                |                |                        |
| ---------------------- | -------------- | -------------- | ---------------------- |
| `@Transactional`       | Method / Class | Transaction 적용 | propagation, isolation |
| `@Retryable`           | Method         | Retry 적용       | attempts, delay        |

각 Annotation마다 다음을 설명한다.

### 적용 위치

```
@Service
public class OrderService {

    @Transactional
    public void createOrder() {
    }
}

```

### 속성

| 속성Type기본값설명 |             |          |                |
| ----------- | ----------- | -------- | -------------- |
| propagation | Propagation | REQUIRED | Transaction 전파 |
| isolation   | Isolation   | DEFAULT  | Isolation      |
| timeout     | int         | -1       | Timeout        |
| readOnly    | boolean     | false    | Read Only 여부   |

### 적용 우선순위

Class와 Method에 동시에 선언한 경우 어느 설정이 우선되는지 명확하게 적는다.

---

# 9. Configuration Specification

설정값은 가장 검색이 많이 발생하는 영역이므로 별도의 Reference 표를 제공한다.

## 9.1 Configuration Summary

| PropertyTypeDefaultRequired설명 |         |      |   |                    |
| ----------------------------- | ------- | ---- | - | ------------------ |
| `cpf.transaction.enabled`     | boolean | true | N | Transaction 기능 활성화 |
| `cpf.transaction.timeout`     | int     | 30   | N | 기본 Timeout         |
| `cpf.retry.max-attempts`      | int     | 3    | N | 최대 실행 횟수           |

---

## 9.2 설정 상세

### `cpf.transaction.timeout`

**Type**

```
integer

```

**Default**

```
30

```

**Unit**

```
seconds

```

**허용 범위**

```
1 ~ 300

```

**예제**

```
cpf:
  transaction:
    timeout: 60

```

**영향**

Transaction 기본 Timeout을 변경한다.

**적용 시점**

Application Startup

또는

Runtime Refresh

등 실제 적용 시점을 작성한다.

---

# 10. 설정값에는 영향도를 반드시 설명한다

설정 문서에서 특히 중요하다.

잘못된 설명:

> timeout 값을 설정한다.

올바른 설명:

> Transaction의 최대 실행 시간을 지정한다. 지정 시간을 초과하면 Transaction 처리 흐름에서 timeout 판정이 발생하며 이후 처리 결과는 사용하는 Transaction Provider의 동작에 영향을 받는다.

특히 다음을 작성한다.

- 값을 증가했을 때 영향
- 값을 감소했을 때 영향
- `0`
- 음수
- null
- 미설정
- 최대값 초과
- 잘못된 문자열
- Runtime 변경 가능 여부

---

# 11. Command / Script Specification

Framework가 제공하는 명령어와 Script는 반드시 Summary를 만든다.

## Command Summary

| Command목적실행 위치주요 Option결과 |          |              |             |              |
| ------------------------- | -------- | ------------ | ----------- | ------------ |
| `cpf generate`            | 코드 생성    | Project Root | `--module`  | Source 생성    |
| `cpf validate`            | 설정 확인    | Project Root | `--profile` | 검증 결과        |
| `cpf migrate`             | DB 변경 적용 | Runtime      | `--version` | Migration 수행 |

---

## Command 상세

```
cpf generate order --module order-service

```

### Syntax

```
cpf generate <domain> [options]

```

### Arguments

| Argument필수설명 |   |               |
| ------------ | - | ------------- |
| domain       | Y | 생성할 Domain 이름 |

### Options

| Option필수Default설명 |   |       |           |
| ----------------- | - | ----- | --------- |
| `--module`        | Y | -     | 대상 Module |
| `--package`       | N | 자동 계산 | Package   |

### 성공 결과

무엇이 생성되는지 exact path까지 작성한다.

### 실패 조건

- Domain 이름 오류
- 기존 Source 충돌
- Package 규칙 오류
- Module 미존재

---

# 12. 기능 간 연계관계

기술문서에서 매우 중요하다.

각 기능을 독립적으로만 설명하지 말고 다른 기능과의 관계를 명시한다.

예:

```
Controller
   ↓
Service
   ↓
Transaction
   ↓
Retry
   ↓
Repository
   ↓
Database

```

그리고 다음을 설명한다.

- 호출 순서
- Proxy 적용 순서
- Transaction 시작 시점
- Retry 시작 시점
- Exception 전달 방향
- Commit 시점
- Event 발행 시점

---

# 13. 특히 Transaction과 다른 기능의 관계를 별도로 설명한다

예를 들어 다음 조합을 설명한다.

| 조합확인 내용                    |                               |
| -------------------------- | ----------------------------- |
| Transaction + Retry        | Retry마다 새 Transaction인지       |
| Transaction + Async        | Thread 변경 후 Transaction 유지 여부 |
| Transaction + Event        | Event 발행 시점                   |
| Transaction + Batch        | Chunk 단위 Transaction 여부       |
| Transaction + Cache        | Commit 전 Cache 변경 여부          |
| Transaction + External API | 외부 호출 실패 처리                   |
| Transaction + Scheduler    | 실행 단위 Transaction 범위          |

단순 기능 설명보다 **조합 동작 설명이 훨씬 중요하다.**

---

# 14. 내부 동작 설명

사용법뿐 아니라 내부의 핵심 동작도 설명한다.

단, Source 전체를 그대로 해설할 필요는 없다.

다음 수준의 흐름을 제공한다.

```
요청
 ↓
Interceptor
 ↓
Context 생성
 ↓
Transaction 시작
 ↓
Target Method 호출
 ↓
성공 여부 판단
 ├─ 성공 → Commit
 └─ 실패 → Rollback
 ↓
Context 정리

```

---

# 15. Sequence Diagram

복잡한 기능에는 Sequence를 제공한다.

예:

```
Caller
  |
  | execute()
  v
Interceptor
  |
  | begin()
  v
TransactionManager
  |
  | invoke()
  v
Service
  |
  | save()
  v
Repository

```

Sequence에서는 특히 다음을 표시한다.

- 시작
- 내부 호출
- 외부 호출
- 성공
- Exception
- Retry
- Commit
- Rollback
- Callback
- 종료

---

# 16. 상태 변화가 있는 기능

State가 존재하는 경우 반드시 State Transition을 작성한다.

예:

```
CREATED
   ↓
RUNNING
   ├── SUCCESS
   ├── FAILED
   └── UNKNOWN

```

## 상태 표

| 현재 상태Event다음 상태조건 |                |         |          |
| ----------------- | -------------- | ------- | -------- |
| CREATED           | start          | RUNNING | 실행 가능    |
| RUNNING           | complete       | SUCCESS | 정상 완료    |
| RUNNING           | error          | FAILED  | 실패       |
| RUNNING           | result unknown | UNKNOWN | 결과 확인 불가 |

---

# 17. Exception Specification

Exception Summary를 반드시 제공한다.

| Exception발생 조건Caller 처리Retry |         |           |     |
| ---------------------------- | ------- | --------- | --- |
| ValidationException          | 입력값 오류  | 요청 수정     | N   |
| TimeoutException             | Timeout | 정책에 따라 처리 | 조건부 |
| ConfigurationException       | 설정 오류   | 설정 수정     | N   |

각 Exception에 대해 설명한다.

- 발생 위치
- 발생 조건
- 원본 Exception
- Wrapping 여부
- Caller 전달 여부
- Error Code
- Message Format
- Retry 가능 여부

---

# 18. Error Code Specification

Error Code를 사용하는 경우 별도 표를 만든다.

| Code의미발생 조건처리 |                   |             |       |
| ------------- | ----------------- | ----------- | ----- |
| CPF-TRX-001   | Transaction 시작 실패 | Provider 오류 | 원인 확인 |
| CPF-TRX-002   | Commit 실패         | Commit 중 오류 | 결과 확인 |
| CPF-CFG-001   | 설정 오류             | 설정값 검증 실패   | 설정 수정 |

Error Message 예제도 가능하면 제공한다.

---

# 19. DB Specification

DB와 연결되는 기능은 다음을 작성한다.

## Table Summary

| Table목적주요 Key관련 기능  |        |               |       |
| ------------------- | ------ | ------------- | ----- |
| CPF\_JOB            | Job 정보 | JOB\_ID       | Batch |
| CPF\_JOB\_EXECUTION | 실행 이력  | EXECUTION\_ID | Batch |

각 Table마다 다음을 작성한다.

- Table 목적
- PK
- FK
- Unique Key
- Index
- Column
- Nullable
- Default
- 상태값
- 생성 주체
- 변경 주체
- 삭제 정책

---

# 20. API Specification

REST API가 있다면 최소 다음 내용을 제공한다.

| MethodPath목적권한RequestResponse |                      |       |           |       |           |
| ----------------------------- | -------------------- | ----- | --------- | ----- | --------- |
| GET                           | `/api/jobs`          | 목록 조회 | JOB\_READ | Query | JobList   |
| POST                          | `/api/jobs/{id}/run` | 실행    | JOB\_RUN  | Body  | Execution |

상세 Specification에서는 다음을 작성한다.

- Header
- Path Variable
- Query Parameter
- Request Body
- Response Body
- HTTP Status
- Error Code
- Paging
- Sorting
- Validation

---

# 21. DTO Specification

DTO도 필드를 나열하는 수준에서 끝내지 않는다.

| FieldTypeRequiredNullableDefaultValidation설명 |         |   |   |    |        |         |
| -------------------------------------------- | ------- | - | - | -- | ------ | ------- |
| jobId                                        | String  | Y | N | -  | max 50 | Job ID  |
| timeout                                      | Integer | N | Y | 30 | 1\~300 | Timeout |

Enum인 경우 가능한 값을 전부 보여준다.

---

# 22. SPI 및 확장 Point

Framework 사용자가 구현할 수 있는 Interface는 일반 API와 구분한다.

## Extension Summary

| SPI목적구현 필요기본 구현 |          |    |                    |
| --------------- | -------- | -- | ------------------ |
| `IdGenerator`   | ID 생성    | 선택 | UUID               |
| `RetryPolicy`   | Retry 정책 | 선택 | DefaultRetryPolicy |

각 SPI마다 다음을 작성한다.

- 언제 구현하는지
- 반드시 구현해야 하는 Method
- 호출 시점
- Thread Safety 요구
- Bean 등록 방법
- 기본 구현
- 구현 예제
- 하지 말아야 할 구현

---

# 23. 기본 구현과 사용자 구현을 구분한다

다음처럼 명시한다.

```
Public API
 └─ RetryPolicy

Default Implementation
 └─ DefaultRetryPolicy

Custom Extension
 └─ CustomRetryPolicy

```

독자가 Interface만 보고 직접 구현해야 하는 것으로 오해하지 않도록 한다.

---

# 24. Runtime Component 설명

실행 중 존재하는 Component는 Runtime 관점으로 설명한다.

예:

| Component생성 시점ScopeThread종료 |         |           |                |                |
| --------------------------- | ------- | --------- | -------------- | -------------- |
| TransactionManager          | Startup | Singleton | Shared         | Application 종료 |
| RequestContext              | Request | Request   | Request Thread | Request 종료     |

특히 ThreadLocal 등을 사용하는 경우 반드시 명시한다.

---

# 25. 동시성 관련 Specification

동시 실행 가능성이 있는 기능은 다음을 작성한다.

- Thread Safe 여부
- Singleton 사용 가능 여부
- 공유 상태 존재 여부
- Lock 사용 여부
- 중복 실행 처리
- 동일 Key 동시 처리
- Timeout
- Race Condition 관련 규칙

---

# 26. 보안 관련 Specification

해당 기능이 인증·권한·민감정보와 연결되면 설명한다.

| 항목내용  |               |
| ----- | ------------- |
| 인증 필요 | 여부            |
| 권한    | 필요한 Authority |
| 입력 검증 | 적용 방식         |
| 민감 필드 | Log 출력 여부     |
| Audit | 기록되는 Event    |

문서 예제에서도 실제 Password, Token, Secret 값을 사용하지 않는다.

---

# 27. Log Specification

개발자가 문제 상황을 확인할 때 필요한 Log 정보도 제공하면 좋다.

| LoggerLevel발생 상황  |       |                |
| ----------------- | ----- | -------------- |
| `cpf.transaction` | DEBUG | Transaction 상세 |
| `cpf.retry`       | INFO  | Retry 실행       |
| `cpf.config`      | WARN  | 설정 이상          |

Log에 포함되는 주요 Identifier도 적는다.

예:

- requestId
- transactionId
- executionId
- jobId

---

# 28. 실제 사용 예제

예제는 반드시 **완성된 흐름**을 보여준다.

나쁜 예:

```
@Transactional
public void test() {
}

```

좋은 예:

```
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order create(CreateOrderRequest request) {

        Order order = Order.create(
            request.customerId(),
            request.amount()
        );

        return orderRepository.save(order);
    }
}

```

그리고 다음 설명을 붙인다.

1. Service Method 진입
2. Transaction 시작
3. Domain 생성
4. Repository 호출
5. 저장 성공
6. Method 종료
7. Transaction Commit

---

# 29. 정상 예제만 작성하지 않는다

최소 다음 예제를 고려한다.

### 정상 사용

가장 일반적인 사용 방법.

### 잘못된 사용

대표적인 실수.

### 경계 조건

빈 값, 최대값, 최소값 등.

### 오류 상황

실패할 경우의 동작.

### 기능 조합

Transaction + Retry처럼 실제 사용 시 함께 사용하는 사례.

---

# 30. 금지 예제를 적극적으로 제공한다

특히 개발자가 많이 실수하는 기능은 다음 형식이 효과적이다.

## 하지 말아야 할 사용

```
public class WrongService {

    public void execute() {
        this.transactionalMethod();
    }

    @Transactional
    public void transactionalMethod() {
    }
}

```

### 문제가 되는 이유

동일 객체 내부 호출에서는 사용 방식과 Proxy 구조에 따라 기대한 Interceptor가 적용되지 않을 수 있다.

### 권장 방식

올바른 호출 구조를 별도로 제공한다.

---

# 31. 빠른 선택표 제공

비슷한 기능이 여러 개 있으면 선택 기준을 제공한다.

예:

| 상황권장 기능          |             |
| ---------------- | ----------- |
| 단순 DB 처리         | Transaction |
| 일시적 실패 재호출       | Retry       |
| 요청과 분리된 처리       | Async       |
| 반복 대량 처리         | Batch       |
| Application 간 전달 | Messaging   |

---

# 32. 설정 선택표

예:

| 목적설정           |                                |
| -------------- | ------------------------------ |
| Transaction 사용 | `cpf.transaction.enabled=true` |
| Timeout 변경     | `cpf.transaction.timeout`      |
| Retry 횟수 변경    | `cpf.retry.max-attempts`       |
| Retry 간격 변경    | `cpf.retry.delay`              |

이런 Summary가 있으면 개발자가 전체 설정 문서를 일일이 찾지 않아도 된다.

---

# 33. API 선택표

예:

| 하고 싶은 일사용 API            |                            |
| ------------------------ | -------------------------- |
| Transaction 적용           | `@Transactional`           |
| Programmatic Transaction | `TransactionTemplate`      |
| Retry 적용                 | `@Retryable`               |
| Event 발행                 | `EventPublisher.publish()` |
| 현재 Context 조회            | `ContextHolder.current()`  |

이 표는 문서 초반과 Appendix 둘 중 한 곳에는 반드시 넣는 것을 권장한다.

---

# 34. Dependency 설명

사용하기 위해 필요한 Dependency를 명시한다.

```
implementation("...")

```

또는

```
<dependency>
    ...
</dependency>

```

그리고 다음을 설명한다.

- 필수 Dependency
- 선택 Dependency
- Runtime Dependency
- DB Driver
- Starter 사용 여부

---

# 35. Module 관계

기능이 어느 Module에 속하는지 명확하게 표시한다.

예:

```
cpf-core
   ↑
cpf-common
   ↑
application

```

또는 실제 구조에 맞는 Dependency 방향을 제공한다.

API 위치와 구현 위치가 다르면 반드시 구분한다.

---

# 36. Package Map

기능 규모가 크다면 Package Map을 제공한다.

```
transaction
├── api
│   ├── TransactionManager
│   └── TransactionContext
├── spi
│   └── TransactionProvider
├── internal
│   └── DefaultTransactionManager
└── exception
    └── TransactionException

```

각 Package 역할을 간단히 설명한다.

---

# 37. Test와 검증 방법

문서 예제를 사용한 뒤 개발자가 정상 적용 여부를 확인할 수 있어야 한다.

예:

```
./gradlew test

```

또는 특정 Test:

```
./gradlew :module:test --tests "*TransactionTest"

```

확인 결과도 작성한다.

```
BUILD SUCCESSFUL

```

Test 이름만 적지 말고 무엇을 검증하는 Test인지 설명한다.

---

# 38. Test Scenario 표

| Scenario입력기대 결과   |              |                  |
| ----------------- | ------------ | ---------------- |
| 정상 처리             | 정상 Request   | Commit           |
| Runtime Exception | Exception 발생 | Rollback         |
| Timeout           | 제한시간 초과      | Timeout 처리       |
| 잘못된 설정            | timeout=-1   | Validation 결과 확인 |

---

# 39. 적용 확인 방법

실제 Application에서 적용됐는지 확인하는 방법도 제공한다.

예:

- Startup Log
- Actuator
- API Response
- DB Record
- Test
- Debug Log
- Admin 화면

---

# 40. 제한사항

지원하지 않는 사항은 숨기지 않는다.

예:

```
현재 지원하지 않는 항목

- Nested Transaction의 일부 Provider 조합
- Runtime 중 Transaction Provider 교체
- 특정 DB Driver 조합

```

지원 여부를 모호하게 표현하지 않는다.

---

# 41. 호환성 표

필요하다면 다음과 같이 작성한다.

| 항목지원 범위     |         |
| ----------- | ------- |
| Java        | 17 / 21 |
| Spring Boot | 3.x     |
| PostgreSQL  | 지원      |
| Oracle      | 지원      |
| MySQL       | 지원      |

실제 검증되지 않은 환경은 검증 완료처럼 작성하지 않는다.

---

# 42. Version별 차이

기능이 변경되었다면 Migration 관점의 정보가 필요하다.

| Version변경 내용영향 |                |          |
| -------------- | -------------- | -------- |
| 1.1            | timeout 기본값 변경 | 기존 설정 확인 |
| 1.2            | API 추가         | 영향 없음    |
| 2.0            | 기존 API 제거      | 코드 변경 필요 |

---

# 43. Deprecated API

Deprecated 된 기능은 다음을 제공한다.

| 기존 API상태대체 API제거 예정 |            |              |     |
| ------------------- | ---------- | ------------ | --- |
| `OldManager`        | Deprecated | `NewManager` | 3.0 |

단순히 Deprecated라고 표시하지 않고 **무엇으로 바꿔야 하는지** 알려준다.

---

# 44. Reference Index

문서 마지막에는 검색용 Index를 제공하는 것이 좋다.

## API

- `TransactionManager`
- `TransactionTemplate`
- `RetryTemplate`

## Annotation

- `@Transactional`
- `@Retryable`

## Configuration

- `cpf.transaction.*`
- `cpf.retry.*`

## Exception

- `TransactionException`
- `RetryException`

## Command

- `cpf generate`
- `cpf validate`

---

# 45. 반드시 포함해야 할 핵심 표

스펙 기술문서를 작성할 때 다음 표의 존재 여부를 검토한다.

1. 기능 Summary
2. API Summary
3. Annotation Summary
4. Configuration Summary
5. Command Summary
6. Exception Summary
7. Error Code Summary
8. State Summary
9. DB Table Summary
10. REST API Summary
11. DTO Field Specification
12. Extension Point Summary
13. 기능 간 연계표
14. 기능 선택표
15. Test Scenario
16. Compatibility
17. Version 변경표

모든 문서에 전부 필요한 것은 아니지만 해당 기능이 존재한다면 빠뜨리지 않는다.

---

# 46. 표 작성 원칙

표는 정보를 압축하는 용도로 사용한다.

한 Cell에 긴 설명을 몰아 넣지 않는다.

좋은 구조:

| API목적입력결과오류상세 |
| ------------- |

나쁜 구조:

| API설명 |           |
| ----- | --------- |
| API1  | 긴 설명 수십 줄 |

긴 내용은 상세 Section으로 이동한다.

---

# 47. 제목 규칙

제목만 보고 내용을 예측할 수 있어야 한다.

좋은 예:

```
8. Transaction
8.1 Transaction 개요
8.2 Transaction 적용 방법
8.3 @Transactional
8.4 Propagation
8.5 Rollback 규칙
8.6 Retry와 함께 사용할 때
8.7 예제
8.8 오류 및 주의사항

```

나쁜 예:

```
8. 기타
8.1 설명
8.2 참고

```

---

# 48. 용어 규칙

동일한 개념은 문서 전체에서 같은 이름을 사용한다.

예를 들어

```
Execution ID
executionId
실행 ID
실행 식별자

```

를 문맥마다 무작위로 섞지 않는다.

처음 용어를 정의하고 이후에는 동일한 표현을 사용한다.

---

# 49. 코드 표기 규칙

다음은 코드 형태로 표시한다.

- Class
- Interface
- Method
- Property
- Package
- Annotation
- Command
- Path
- Table
- Column

예:

```
`TransactionManager`
`execute()`
`cpf.transaction.timeout`
`CPF_JOB_EXECUTION`

```

---

# 50. 예제 작성 규칙

예제는 실제 복사하여 사용할 수 있는 수준을 목표로 한다.

예제에 반드시 확인할 항목:

- import
- Bean
- Annotation
- Configuration
- Method
- DTO
- Exception 처리
- 호출 코드

너무 많은 부분을 `...`으로 생략하지 않는다.

---

# 51. Source 이름만 나열하지 않는다

다음은 좋지 않다.

> Transaction 관련 구현은 TransactionManager.java를 참고한다.

스펙 문서는 Source를 읽지 않아도 핵심 계약을 이해할 수 있어야 한다.

Source Link는 **추가 참고 정보**로 사용한다.

---

# 52. 문서 간 역할 분리

스펙 문서는 다른 문서와 역할을 구분한다.

### Architecture 문서

전체 구조와 설계 방향.

### Developer Guide

개발자가 기능을 이용하여 개발하는 방법.

### Specification

각 기능의 정확한 계약과 동작.

### API Reference

Class / Method / Parameter 상세 조회.

### Operator Guide

실행 환경에서 설정·조회·조작하는 방법.

따라서 스펙 문서는 단순 Tutorial도 아니고 단순 API 목록도 아니다.

---

# 53. 스펙 문서에서 가장 중요한 질문

각 기능마다 다음 질문에 모두 답할 수 있어야 한다.

### What

무엇인가?

### Why

왜 필요한가?

### When

언제 사용하는가?

### Where

어느 Module / Layer에서 사용하는가?

### How

어떻게 사용하는가?

### Input

무엇을 넣는가?

### Output

무엇이 반환되는가?

### Internal Behavior

내부에서는 무엇이 일어나는가?

### Failure

실패하면 어떻게 되는가?

### Combination

다른 기능과 같이 사용하면 어떻게 되는가?

### Configuration

무엇을 변경할 수 있는가?

### Extension

사용자가 무엇을 확장할 수 있는가?

### Verification

정상 적용 여부를 어떻게 확인하는가?

---

# 54. 작성 전 Source 조사 절차

문서를 바로 작성하지 않는다.

먼저 다음 순서로 Source를 조사한다.

## 1단계 — Module 확인

기능이 위치한 Module을 확인한다.

## 2단계 — Package 탐색

관련 Package 전체를 확인한다.

## 3단계 — Public API 추출

- public class
- public interface
- public enum
- public annotation
- public method

등을 추출한다.

## 4단계 — 실제 구현 확인

Interface만 보고 문서를 작성하지 않는다.

Default Implementation을 확인한다.

## 5단계 — Consumer 확인

실제로 누가 호출하는지 검색한다.

## 6단계 — Configuration 확인

ConfigurationProperties, YAML, Environment 접근을 확인한다.

## 7단계 — Exception 확인

발생 가능한 Exception과 처리 흐름을 확인한다.

## 8단계 — Test 확인

Test에서 정의된 기대 동작을 확인한다.

## 9단계 — Sample 확인

실제 사용 방식을 확인한다.

## 10단계 — DB / API / Script 확인

기능과 연결된 다른 Artifact를 확인한다.

---

# 55. Source와 Consumer를 함께 조사한다

다음 세 가지만 보고 작성해서는 안 된다.

```
Interface
DTO
Annotation

```

반드시 다음 전체 흐름을 확인한다.

```
Public API
   ↓
Implementation
   ↓
Consumer
   ↓
Runtime
   ↓
Persistence / External Component

```

그래야 실제 동작을 설명할 수 있다.

---

# 56. 문서 검증 방법

작성 후 문장을 읽는 수준으로 끝내지 않는다.

다음과 같이 검증한다.

### API 검산

문서 API와 실제 Public API 비교.

### Configuration 검산

Property 이름, Type, Default 비교.

### Exception 검산

실제 발생 코드와 문서 비교.

### Example 검산

예제 Compile 또는 Test.

### Command 검산

실제 Command 실행.

### Link 검산

내부 Link 및 상대경로 확인.

### Terminology 검산

동일 개념 이름 일치 확인.

---

# 57. 문서 품질 판정 기준

좋은 스펙 문서는 처음 보는 개발자가 다음 질문에 Source 검색 없이 대부분 답할 수 있어야 한다.

- 어떤 기능을 써야 하지?
- 어떤 API를 호출하지?
- 어떤 Annotation을 붙이지?
- 설정 이름이 뭐지?
- 기본값은 뭐지?
- 어떤 값들이 가능하지?
- Transaction하고 같이 사용하면 어떻게 되지?
- 실패하면 어떤 Exception이 나오지?
- Retry 가능한가?
- 어느 Module을 추가해야 하지?
- 구현체를 직접 만들어야 하나?
- 기본 구현이 있나?
- 어떤 DB Table을 쓰지?
- 실제 예제는 어디 있지?
- 어떻게 Test하지?
- 내가 잘못 사용하고 있는 건 아닌가?

답을 찾기 위해 계속 Source를 열어봐야 한다면 스펙 문서가 충분히 작성되지 않은 것이다.

---

# 58. 금지하는 작성 형태

다음 형태의 스펙 문서는 만들지 않는다.

### 1. Class 목록만 나열

```
TransactionManager
TransactionContext
TransactionException

```

### 2. Source 구조 복사

Directory Tree만 작성하고 종료.

### 3. 한 줄 기능 설명

> Retry 기능을 제공한다.

### 4. 설정명만 나열

```
cpf.retry.enabled
cpf.retry.count

```

### 5. 성공 예제만 작성

실패·경계·잘못된 사용법이 없음.

### 6. Interface만 설명

실제 기본 구현과 Consumer가 없음.

### 7. Swagger 내용 그대로 복사

API 호출 전후의 실제 의미가 없음.

### 8. Test 이름만 기재

무엇을 검증하는지 설명하지 않음.

### 9. Source Link로 설명 대체

> 자세한 내용은 Source 참고.

### 10. 추측 기반 설명

Source 또는 실행 결과로 확인하지 않은 동작을 사실처럼 작성.

---

# 59. 권장 문서 Template

```
# [기능명] Specification

## 1. Overview
## 2. Quick Reference
## 3. Architecture Position
## 4. Concepts
## 5. Feature Summary
## 6. API Summary
## 7. Configuration Summary

## 8. Functional Specification
### 8.1 기본 동작
### 8.2 처리 순서
### 8.3 상태
### 8.4 오류 처리
### 8.5 경계 조건

## 9. API Specification
### 9.1 Interfaces
### 9.2 Classes
### 9.3 Methods
### 9.4 DTO
### 9.5 Enum

## 10. Annotation Specification

## 11. Configuration Specification

## 12. Integration
### 12.1 Transaction
### 12.2 Retry
### 12.3 Async
### 12.4 Event
### 12.5 Batch
### 12.6 Database

## 13. Extension Points
### 13.1 SPI
### 13.2 Default Implementation
### 13.3 Custom Implementation

## 14. Runtime Behavior

## 15. Error Specification
### 15.1 Exception
### 15.2 Error Code

## 16. Examples
### 16.1 기본 예제
### 16.2 실제 사용 예제
### 16.3 기능 조합 예제
### 16.4 잘못된 사용 예제

## 17. Testing & Verification

## 18. Limitations

## 19. Compatibility

## 20. Version Changes

## 21. Related Documents

## 22. Reference Index

```

---

# 60. 최종 작성 완료 Checklist

## Source 정합성

-  실제 Module을 확인했다.
-  실제 Package를 확인했다.
-  Public API 전체를 확인했다.
-  기본 구현을 확인했다.
-  실제 Consumer를 확인했다.
-  Configuration을 확인했다.
-  Exception을 확인했다.
-  Test를 확인했다.
-  Sample을 확인했다.

## 내용

-  기능 목적이 설명되어 있다.
-  사용 시점이 설명되어 있다.
-  기능 Summary가 있다.
-  API Summary가 있다.
-  설정 Summary가 있다.
-  상세 Parameter 설명이 있다.
-  기본값이 있다.
-  허용 범위가 있다.
-  내부 동작이 설명되어 있다.
-  기능 간 연결 관계가 설명되어 있다.
-  오류 동작이 설명되어 있다.
-  경계 조건이 설명되어 있다.
-  확장 Point가 설명되어 있다.
-  기본 구현이 설명되어 있다.

## 예제

-  최소 하나 이상의 완전한 사용 예제가 있다.
-  정상 예제가 있다.
-  실패 예제가 있다.
-  잘못된 사용 예제가 있다.
-  복합 기능 사용 예제가 있다.

## Reference

-  API를 빠르게 찾을 수 있다.
-  Annotation을 빠르게 찾을 수 있다.
-  설정을 빠르게 찾을 수 있다.
-  Command를 빠르게 찾을 수 있다.
-  Exception을 빠르게 찾을 수 있다.
-  관련 문서로 이동할 수 있다.

## 검증

-  문서 API와 Source API가 일치한다.
-  설정명과 기본값이 실제 Source와 일치한다.
-  Example이 실제 API와 일치한다.
-  명령어가 실제로 존재한다.
-  존재하지 않는 기능을 설명하지 않았다.
-  제한사항을 숨기지 않았다.
-  Version 정보가 정확하다.

---

# 61. 최종 품질 목표

최종 스펙 기술문서는 **“어떤 기능이 있다”를 알려주는 문서가 아니라 “이 기능의 정확한 사용 계약을 찾는 기준 문서”**가 되어야 한다.

좋은 문서는 다음 세 가지 탐색 방식을 모두 지원해야 한다.

### 목적 중심 탐색

> “Transaction을 사용하고 싶다.”

→ 기능 Summary → Transaction Specification

### API 중심 탐색

> “`@Transactional` 옵션이 무엇이지?”

→ API / Annotation Summary → 상세 Specification

### 문제 중심 탐색

> “Retry와 Transaction을 같이 사용했는데 Transaction 범위가 어떻게 되지?”

→ Integration → Transaction + Retry

따라서 문서 전체는 다음 구조를 목표로 한다.

```
빠른 탐색
   ↓
정확한 기능 계약
   ↓
실제 동작
   ↓
사용 방법
   ↓
기능 간 관계
   ↓
오류 / 경계조건
   ↓
실제 예제
   ↓
검증 방법
   ↓
상세 Reference

```

이 구조까지 갖춰져야 처음 문서를 접하는 개발자가 필요한 정보를 스스로 찾아 실제 개발에 적용할 수 있는 스펙 기술문서가 된다.