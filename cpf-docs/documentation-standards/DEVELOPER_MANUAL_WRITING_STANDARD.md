# CPF 개발자 매뉴얼 작성 작업 지침

## 1. 문서 목적

본 지침은 `cpf-docs/guides/01_개발자매뉴얼.md`를 작성·검수하기 위한 작업 기준을 정의한다.

개발자 매뉴얼의 목적은 CPF의 구조와 기능을 설명하는 데 그치지 않는다.

CPF를 처음 접한 Java 개발자가 Framework Source를 역분석하거나 Framework 담당자에게 별도 설명을 요청하지 않고 다음 업무를 수행할 수 있어야 한다.

1. 개발환경을 구성한다.
2. CPF Repository와 Module 구조를 이해한다.
3. 신규 업무영역을 생성한다.
4. REST API를 개발한다.
5. Command와 Query를 개발한다.
6. Domain과 Persistence를 구현한다.
7. CPF가 제공하는 Public API·SPI·Annotation·공통 기능을 찾아 사용한다.
8. Transaction Boundary를 올바르게 설정한다.
9. 동시성과 중복 요청을 처리한다.
10. Local·Remote 호출을 구현한다.
11. Kafka·Outbox·Inbox를 이용한 비동기 처리를 구현한다.
12. File·Attachment·외부 시스템 연계를 구현한다.
13. Permission·Data Scope·Masking·Reason·Approval·Audit를 적용한다.
14. DB Migration을 작성하고 Upgrade·Rollback 영향을 판단한다.
15. 정상·오류·경계·동시성·Timeout·부분 실패 Test를 작성한다.
16. 응답 유실과 `UNKNOWN_RESULT`를 판단한다.
17. Retry·Reprocess·Reconcile·Compensation을 적용한다.
18. ADM과 Log·Metric·Trace에서 처리 결과를 확인한다.
19. 개발 결과를 배포·운영 담당자에게 인계한다.

---

# 2. 작성 완료 판단 기준

문서 분량, 표 개수, Source 링크 개수, API 목록 개수만으로 작성 완료를 판단하지 않는다.

다음 질문에 개발자가 문서만으로 답할 수 있어야 한다.

> 무엇을 사용해야 하는가?

> 어느 Module과 Package에 작성해야 하는가?

> 어떤 API·Command·Query·Annotation을 호출해야 하는가?

> 실제 입력값과 기본값은 무엇인가?

> Transaction은 어디에서 시작되고 종료되는가?

> 여러 Repository 또는 Command가 연결되면 Transaction은 어떻게 되는가?

> Remote 호출이나 Kafka를 Transaction과 같이 처리하면 어떻게 되는가?

> 동시 요청과 중복 요청은 어떻게 막는가?

> Timeout이 발생하면 실패인가, 결과 불명인가?

> Retry해도 되는가?

> 이미 상대 시스템에서 처리됐다면 어떻게 확인하고 복구하는가?

> 어떤 Permission이 필요한가?

> 어떤 Audit가 남는가?

> 어떤 Test를 작성해야 하는가?

> 실패하면 Log·DB·ADM에서 무엇을 확인하는가?

> 정상화됐다는 것을 무엇으로 판단하는가?

이 질문 중 중요한 항목에 문서만으로 답할 수 없다면 해당 기능의 문서화 상태는 `부분 구현`, `미검증` 또는 `재확인 필요`로 판단한다.

---

# 3. 사실성 원칙

문서에 기술하는 다음 항목은 실제 Repository에서 확인한다.

- Module
- Package
- Class
- Interface
- Method
- Annotation
- Command
- Query
- REST Endpoint
- Request / Response
- Error Code
- Permission
- Property
- Environment Variable
- SQL
- Migration
- Table
- Column
- Index
- Kafka Topic
- Event
- Script
- Gradle/Maven Task
- Shell 명령
- ADM Route
- Frontend Component
- Test
- 상태값

Source에 없는 이름을 설명을 위해 임의 생성하지 않는다.

샘플이 필요한 경우 다음과 같이 명확히 구분한다.

`실제 CPF API`

`교육용 업무 코드`

교육용 업무 코드를 CPF 자체 Public API처럼 표현하지 않는다.

---

# 4. Source 조사 순서

개발자 매뉴얼 작성 전에 다음 순서로 실제 구현을 조사한다.

## 4.1 Architecture 조사

- Root Module
- Submodule
- Module Dependency
- API Layer
- Application Layer
- Domain Layer
- Persistence Layer
- Infrastructure Layer
- Framework Core
- ADM Backend
- ADM Frontend
- Batch
- Gateway
- BZA
- 공통 Library

## 4.2 개발자 사용 Surface 조사

다음 항목을 전수 조사한다.

- Public Java API
- Public Interface
- SPI
- Annotation
- Command
- Query
- Controller Endpoint
- Client API
- Generator
- Gradle Task
- Script
- Migration 명령
- Test 명령
- 개발 관련 Property

## 4.3 Runtime 동작 조사

각 주요 기능이 실제 실행될 때 다음을 추적한다.

`Controller → Application → Command/Query → Transaction → Domain → Repository → DB`

필요한 경우:

`→ Outbox → Kafka`

`→ Remote Adapter → Remote Service`

`→ Audit`

까지 연결한다.

## 4.4 실패 및 복구 조사

각 중요 기능에서 다음 상황을 조사한다.

- Validation 실패
- Permission 실패
- 데이터 없음
- Version 충돌
- Duplicate Request
- DB 오류
- Lock Timeout
- Remote Timeout
- Connection Reset
- Response Loss
- Kafka Publish 실패
- Consumer 실패
- 부분 적용
- Instance 종료
- `UNKNOWN_RESULT`
- Retry
- Reprocess
- Reconcile
- Compensation
- Rollback

---

# 5. 문서 전체 사용 구조

개발자 매뉴얼은 다음 5개 사용 계층을 가진다.

## 5.1 Quick Finder

개발자가 하려는 업무를 기준으로 필요한 CPF 기능을 찾는다.

## 5.2 Summary

CPF에서 사용할 수 있는 API·Command·Query·명령·기능을 빠르게 파악한다.

## 5.3 Tutorial

처음 CPF를 접한 개발자가 하나의 업무를 처음부터 끝까지 구현한다.

## 5.4 기능별 Guide / Cookbook

업무 중 필요한 기능을 상황별로 찾아 적용한다.

## 5.5 Reference

정확한 Class·Method·Command·Property·명령·Error Code 등을 검색한다.

---

# 6. 문서 앞부분 필수 Summary

개발자가 전체 문서를 읽기 전에 CPF의 개발 기능을 파악할 수 있도록 다음 Summary를 제공한다.

1. 개발 작업 Quick Finder
2. CPF 개발 기능 Map
3. Public API Summary
4. Command Summary
5. Query Summary
6. Annotation Summary
7. SPI Summary
8. 개발 명령어 Summary
9. Transaction Summary
10. Concurrency Summary
11. Idempotency Summary
12. Local / Remote Summary
13. Kafka / Async Summary
14. Security Summary
15. Persistence / Paging Summary
16. Error / Exception Summary
17. `UNKNOWN_RESULT` / Recovery Summary
18. Test / Fault Injection Summary
19. Source Navigation Map
20. ADM 확인 Map

---

# 7. 개발 작업 Quick Finder 작성 기준

이 표는 개발자가 가장 먼저 사용하는 표다.

첫 번째 열은 기술 이름이 아니라 **개발자가 하려는 일**로 작성한다.

| 내가 하려는 일사용할 CPF 기능시작 API/Command관련 명령함께 확인할 것예제상세 |              |         |         |                    |          |   |
| ------------------------------------------------- | ------------ | ------- | ------- | ------------------ | -------- | - |
| 신규 업무 생성                                          | Generator    | 실제 구성요소 | 실제 명령   | Module/Package     | Tutorial | § |
| 상태 변경 API 작성                                      | Command      | 실제 API  | Test 명령 | TX, Version, Audit | Recipe   | § |
| 목록 조회                                             | Paging/Query | 실제 API  | Test 명령 | Max Size, Sort     | Recipe   | § |
| 중복 요청 방지                                          | Idempotency  | 실제 API  | Test 명령 | Key, Payload       | Recipe   | § |
| Event 발행                                          | Outbox/Kafka | 실제 API  | Test 명령 | TX, Retry          | Recipe   | § |

### 필수 작성 규칙

한 행만 보고 다음을 판단할 수 있어야 한다.

- 어떤 CPF 기능을 선택할지
- 어떤 API나 Command부터 볼지
- 관련 Test나 실행 명령은 무엇인지
- Transaction/Security/Idempotency 등 추가 고려사항이 무엇인지
- 상세 설명이 어디에 있는지

---

# 8. CPF 개발 기능 Map 작성 기준

CPF가 개발자에게 제공하는 기능 전체를 영역별로 보여준다.

| 영역기능개발 목적Owner ModulePublic APICommand/QueryAnnotationConfig관련 Test상세 |
| --------------------------------------------------------------------- |

### 영역 예

- API
- Application
- Domain
- Persistence
- Transaction
- Concurrency
- Idempotency
- Integration
- Kafka
- File
- Security
- Audit
- Validation
- Logging
- Observability
- Migration
- Test
- Recovery

각 기능은 `지원함`이라는 표현만 사용하지 않는다.

어떤 구성요소를 개발자가 실제로 호출하거나 구현하는지 표시한다.

---

# 9. Public API Summary 작성 기준

Public API는 Class 이름만 나열하지 않는다.

개발 목적을 첫 번째 열로 둔다.

| 개발 목적API/Class주요 MethodOwner입력반환TX 관계주요 오류실제 Consumer상세 |
| ------------------------------------------------------- |

### 상세 Reference

각 API 상세 설명에는 다음 항목을 포함한다.

| 항목작성 내용         |                |
| --------------- | -------------- |
| Package         | 실제 Package     |
| Class/Interface | 실제 이름          |
| Visibility      | Public 여부      |
| Owner Module    | 소유 Module      |
| Method          | 실제 Signature   |
| Parameter       | 이름·Type·필수 여부  |
| Default         | 존재 시 실제 값      |
| Return          | Type·의미        |
| Exception       | 실제 발생 가능한 오류   |
| Transaction     | TX 필요 여부       |
| Thread Safety   | 확인 가능한 경우      |
| Consumer        | 실제 호출 Source   |
| 사용 제한           | 호출하면 안 되는 상황   |
| Test            | 실제 Test Source |
| 상세 예제           | 전체 사용 예        |

---

# 10. SPI Summary 작성 기준

| 개발 목적SPI구현 주체Owner구현 위치호출 시점실패 영향등록 방법실제 Consumer상세 |
| --------------------------------------------------- |

SPI 상세 설명에는 다음을 작성한다.

- 왜 SPI가 필요한가
- 누가 구현하는가
- Framework가 언제 호출하는가
- 구현체 등록 방식
- 구현체가 여러 개일 때 선택 방식
- Thread/Transaction Context
- Exception을 던졌을 때 동작
- Timeout 여부
- 구현해서는 안 되는 동작
- Test 방법

---

# 11. Annotation Summary 작성 기준

| Annotation목적적용 대상주요 AttributeDefaultTX 영향Security 영향Runtime 처리 주체상세 |
| ------------------------------------------------------------------- |

각 Annotation 상세에는:

- 실제 Package
- Target
- Retention
- Attribute
- Default
- 적용 예
- 잘못 적용한 예
- Runtime 처리 Class
- 다른 Annotation과의 관계
- Transaction 영향
- Security 영향
- Test

를 작성한다.

---

# 12. Command Summary 작성 기준

Command는 가장 중요한 개발 Surface 중 하나로 취급한다.

| 업무 목적CommandOwner호출 주체핵심 입력결과TXIdempotencyExpected VersionPermissionAuditRemoteUNKNOWN\_RESULT 가능상세 |
| --------------------------------------------------------------------------------------------------- |

### Command 상세 필수 항목

1. Command 목적
2. 사용할 상황
3. 사용하면 안 되는 상황
4. Owner Module
5. 호출 가능한 Consumer
6. 실제 Command Class
7. Handler
8. Dispatcher 또는 호출 API
9. 입력 Field
10. Result
11. Validation
12. Transaction Boundary
13. Rollback
14. Expected Version
15. Lock
16. Idempotency
17. Permission
18. Data Scope
19. Reason
20. Approval
21. Audit
22. Local / Remote 차이
23. Timeout
24. Retry
25. `UNKNOWN_RESULT`
26. Reconciliation
27. Test
28. ADM 확인

---

# 13. Command 입력 Field 표

각 Command는 DTO Source를 따로 확인하지 않아도 될 정도로 입력값을 설명한다.

| FieldType필수DefaultValidation의미생성 주체동일 요청 재전송 시 유지민감정보 |
| ----------------------------------------------------- |

추가로 필요한 경우:

- 최대 길이
- 허용 Enum
- Timezone
- Format
- Null 처리
- Blank 처리
- Version 의미
- Key 생성 규칙

을 기록한다.

---

# 14. Query Summary 작성 기준

| 조회 목적QueryOwner입력PagingSortingReadOnly TXData ScopeMasking조회 제한Cache상세 |
| ---------------------------------------------------------------------- |

Query 상세에는 다음을 작성한다.

- 조회 조건
- Paging 필수 여부
- Default Page Size
- Max Page Size
- Sort 가능한 Field
- Filter 조건
- Data Scope
- Masking
- ReadOnly Transaction
- Lock 여부
- 조회 건수 제한
- 대량 데이터 주의사항
- SQL 또는 Repository Source
- Test

---

# 15. 개발 명령어 Summary 작성 기준

명령은 목적별로 그룹화한다.

## 15.1 Build

- 전체 Build
- Module Build
- Clean
- Package
- Dependency 확인
- 실제 Repository에 존재하는 추가 Task

## 15.2 Test

- 전체 Test
- Module Test
- Class 단위 Test
- Method 단위 Test
- Integration Test
- 필요한 경우 Browser/Fault Test

## 15.3 Runtime

- Local 실행
- Profile 지정
- 특정 Module 실행
- Debug 실행 방식이 Repository에 정의된 경우 해당 방식

## 15.4 Generator

- 업무영역 생성
- API 생성
- Domain 생성
- 기타 실제 Generator 기능

## 15.5 DB / Migration

- 상태 확인
- Migration 실행
- 검증
- Repair 또는 Rollback 관련 실제 지원 기능

## 15.6 Documentation / Verification

- OpenAPI
- JavaDoc
- Code Generation
- Link/Schema 검증
- 실제 Repository Script

---

# 16. 개발 명령어 Master Table

앞부분 Summary:

| 목적명령실행 위치자주 사용하는 옵션결과상세 |
| ----------------------- |

뒤쪽 Reference:

| 명령목적실행 위치선행 조건SyntaxOptionDefault입력생성/변경 대상정상 결과실패 결과Exit Code재실행Rollback |
| ------------------------------------------------------------------------- |

### 명령 설명 최소 기준

명령만 표시하지 않는다.

예:

`./gradlew test`

만 적는 것으로 끝내지 않는다.

반드시 다음을 설명한다.

- Repository Root에서 실행하는지
- 특정 Module Directory에서 실행하는지
- 어떤 Profile을 사용하는지
- 어떤 Test가 실행되는지
- DB/Kafka가 필요한지
- 성공 시 어떤 결과가 나오는지
- Report가 어디에 생성되는지
- 실패 시 어디를 확인하는지
- 재실행해도 되는지

---

# 17. Generator Command 표

| 생성 대상명령필수 Parameter선택 ParameterDefault생성 파일기존 파일 처리재실행개발자 수정 영역상세 |
| ----------------------------------------------------------------- |

생성 결과는 별도 표로 작성한다.

| 생성 파일역할자동 생성개발자 수정재생성 영향Framework 관리 여부 |
| --------------------------------------- |

Generator 설명에서 반드시 답해야 할 질문:

- 무엇을 생성하는가
- 무엇은 생성하지 않는가
- 이름 규칙은 무엇인가
- 동일 이름 재실행 시 어떻게 되는가
- 기존 Source가 덮어써지는가
- 개발자는 생성 후 어떤 파일을 수정하는가
- Framework가 계속 관리하는 파일은 무엇인가

---

# 18. Transaction Summary 작성 기준

Transaction 설명은 별도 장으로 독립시키고 Command와 연결한다.

| 상황TX 시작 위치PropagationCommit 조건Rollback 조건개발자 조치주의사항 |       |       |           |       |             |                |
| --------------------------------------------------- | ----- | ----- | --------- | ----- | ----------- | -------------- |
| 일반 Command                                          | 실제 위치 | 실제 정책 | 실제 조건     | 실제 조건 | 표준 방식       | -              |
| Query                                               | 실제 위치 | 실제 정책 | 실제 조건     | 실제 조건 | ReadOnly 여부 | -              |
| Nested Command                                      | 실제 정책 | 실제 정책 | 실제 조건     | 실제 영향 | 호출 규칙 확인    | 부분 Commit      |
| Outbox 포함                                           | 실제 위치 | 실제 정책 | DB+Outbox | 실제 조건 | 표준 API      | 직접 Kafka 발행 비교 |
| Remote 포함                                           | 실제 정책 | 실제 정책 | -         | -     | 권장 패턴       | Timeout        |

---

# 19. Transaction 상세 설명 필수 항목

Transaction 장에서는 반드시 다음 질문에 답한다.

### Transaction 시작

- 어디서 시작되는가
- `@Transactional`을 직접 사용하는가
- Framework Interceptor가 관리하는가
- Application Service인가
- Command Handler인가

### Commit

- 정상 Return 시 Commit인가
- 후처리 시점은 언제인가
- Audit와 Outbox가 동일 TX인가

### Rollback

- Runtime Exception
- Checked Exception
- Business Exception
- Validation Exception
- Remote Exception

각 Exception에 대한 실제 정책을 설명한다.

### Nested Call

- Command → Command
- Command → Query
- Command → Repository A/B
- Command → Remote
- Domain → Command

각 관계별 허용 여부와 Transaction 결과를 작성한다.

---

# 20. Transaction 호출 관계표

| FromTo허용TX 관계실패 영향주의권장 대안 |            |       |              |            |                 |        |
| ------------------------- | ---------- | ----- | ------------ | ---------- | --------------- | ------ |
| Controller                | Command    | 실제    | 실제           | 실제         | -               | -      |
| Command                   | Repository | 실제    | 동일/실제        | Rollback   | -               | -      |
| Command                   | Command    | 실제 정책 | 실제           | 실제         | Nested TX       | 실제 대안  |
| Command                   | Remote     | 조건부   | Local과 분리 여부 | Partial 가능 | UNKNOWN\_RESULT | 실제 패턴  |
| Domain                    | Command    | 금지 여부 | -            | -          | Dependency      | Port 등 |

---

# 21. Transaction Propagation 표

실제 CPF에서 사용하는 Propagation만 작성한다.

| PropagationCPF 사용 여부누가 사용할 수 있는가실제 사용 위치Commit 특성실패 영향사용 제한 |
| ----------------------------------------------------------- |

특히 다음을 명확히 한다.

- 개발자가 `REQUIRES_NEW`를 임의 사용할 수 있는가
- Framework 내부 전용인가
- Audit처럼 별도 Transaction을 허용하는 사례가 있는가
- Nested Command에서 어떤 문제가 생길 수 있는가

---

# 22. Transaction + Remote 상태 조합표

외부 시스템 호출이 포함되는 기능은 다음 상태를 반드시 설명한다.

| Local DBRemote 처리응답Local 상태실제 의미Retry다음 행동 |           |          |          |          |             |                        |
| ------------------------------------------ | --------- | -------- | -------- | -------- | ----------- | ---------------------- |
| 미Commit                                    | 실패 확정     | 실패 응답    | Rollback | 실패 확정    | 조건부         | 오류 처리                  |
| Commit                                     | 성공        | 성공 응답    | 완료       | 정상       | 불필요         | 종료                     |
| 미확정                                        | 성공 가능     | Timeout  | 미확정      | 결과 불명 가능 | 즉시 Retry 제한 | Status/Reconcile       |
| Rollback                                   | Remote 성공 | 응답 또는 유실 | 불일치      | 부분 실패    | 단순 Retry 위험 | Compensation/Reconcile |

---

# 23. 동시성 Summary

| 개발 상황사용할 방식관련 APIDB 기능Client 입력충돌 결과Retry |                   |        |          |                 |                  |       |
| ----------------------------------------- | ----------------- | ------ | -------- | --------------- | ---------------- | ----- |
| 화면 수정 경쟁                                  | Optimistic Lock   | 실제 API | Version  | expectedVersion | 실제 Error         | 재조회   |
| 선점 필요                                     | Pessimistic Lock  | 실제 API | Row Lock | -               | Timeout          | 실제 정책 |
| 중복 데이터                                    | Unique Constraint | -      | UNIQUE   | 값               | Constraint Error | 보통 N  |
| 중복 요청                                     | Idempotency       | 실제 API | Ledger 등 | Key             | 기존 결과            | 조건부   |

---

# 24. Idempotency Summary

| 상황KeyPayload기존 상태Server 동작Client 결과개발자 행동 |    |   |     |           |          |                  |
| ----------------------------------------- | -- | - | --- | --------- | -------- | ---------------- |
| 최초 요청                                     | 신규 | A | 없음  | 실행        | 실제 결과    | 정상               |
| 동일 요청                                     | 동일 | A | 완료  | 기존 처리 재사용 | 실제 결과    | 재실행 금지           |
| 처리 중                                      | 동일 | A | 진행  | 실제 정책     | 실제 응답    | 조회/대기            |
| Key 재사용                                   | 동일 | B | 존재  | 거절        | 실제 Error | 새 Key            |
| Timeout 후                                 | 동일 | A | 미확정 | 실제 정책     | 실제 결과    | Status/Reconcile |

Idempotency 장에는 다음을 포함한다.

- Key 생성 주체
- 길이
- Format
- Scope
- TTL
- Payload Hash
- 동일성 판단
- 저장 위치
- 상태
- 동시 요청 Race
- 처리 중 Instance 장애
- Retry
- Reconciliation
- Test

---

# 25. Local / Remote Summary

| 항목Same-JVMRemote개발자가 사용하는 Contract주의사항 |    |         |              |                 |
| -------------------------------------- | -- | ------- | ------------ | --------------- |
| 호출 진입점                                 | 실제 | 실제      | Owner Port 등 | -               |
| Serialization                          | 실제 | 실제      | DTO          | Version         |
| Transaction                            | 실제 | 분리 여부   | -            | 부분 실패           |
| Authentication                         | 실제 | 실제      | -            | Credential      |
| Timeout                                | 실제 | 실제      | Config       | UNKNOWN\_RESULT |
| Retry                                  | 실제 | 실제      | 정책           | Idempotency     |
| Error                                  | 실제 | Mapping | 실제 API       | -               |

---

# 26. Remote API 상세 표

| 목적Owner Port실제 Endpoint/ClientTimeoutRetryIdempotency인증오류 MappingUNKNOWN\_RESULT상세 |
| ---------------------------------------------------------------------------------- |

Remote 호출 상세에는:

- 요청 DTO
- 응답 DTO
- HTTP Method
- URL 구성 방식
- Authentication
- Audience
- Header
- Timeout
- Retry
- Circuit Breaker 등 실제 기능
- 오류 Mapping
- Version
- Idempotency
- Status Query
- `UNKNOWN_RESULT`
- Test

를 연결한다.

---

# 27. Kafka / Async Summary

| 개발 목적기능API/SPITopic/EventDB TX 관계중복 방지실패 처리운영 확인상세 |
| -------------------------------------------------- |

---

# 28. Event Catalog

| Event목적ProducerConsumerTopicKeySchema Version발생 시점TXOrderingRetry/DLQ상세 |
| ----------------------------------------------------------------------- |

Event Payload:

| FieldType필수의미생성 Source민감정보호환성 |
| ----------------------------- |

반드시 다음을 설명한다.

- DB Commit 전/후 어느 시점에 Event가 확정되는가
- Outbox를 사용하는가
- Event 중복이 가능한가
- Consumer가 중복 Event를 받아도 되는가
- Ordering 범위
- Consumer 실패
- Retry
- DLQ
- Reprocessing

---

# 29. Security Summary

| 개발 목적기능API/Annotation입력/조건실패 결과Data ScopeAudit상세 |
| ------------------------------------------------ |

예:

- 현재 사용자 조회
- Permission 확인
- Data Scope 적용
- Masking
- Reason
- Approval
- Audit

---

# 30. Permission Catalog

| Permission기능적용 API적용 Command기본 Role 연계Data ScopeReasonApprovalAuditADM |
| ---------------------------------------------------------------------- |

Permission 이름만 나열하지 않는다.

개발자가 다음을 확인할 수 있어야 한다.

> 어떤 API를 보호하는가?

> 어떤 Command 실행을 막는가?

> 데이터 범위도 달라지는가?

> Reason이나 Approval이 추가로 필요한가?

---

# 31. Error Code Catalog

| Error CodeHTTP발생 Layer발생 조건의미RollbackRetryClient 행동Log Level상세 |
| -------------------------------------------------------------- |

---

# 32. Exception Mapping 표

| 발생 Exception발생 위치Error CodeHTTPTX RollbackLogClient/Consumer 조치 |
| --------------------------------------------------------------- |

Transaction과 Error Handling이 서로 다른 장에 흩어지지 않도록 상호 링크한다.

---

# 33. Paging Summary

| 항목실제 값/정책설정 위치초과/오류 결과 |    |       |            |
| ---------------------- | -- | ----- | ---------- |
| page 시작                | 실제 | 실제 위치 | 실제 오류      |
| default size           | 실제 | 실제 위치 | -          |
| max size               | 실제 | 실제 위치 | 실제 오류      |
| sort 형식                | 실제 | 실제 위치 | Validation |
| total count            | 실제 | 구현    | -          |

API별 Paging:

| Query/APIPagingDefaultMaxSortFilterTotal CountData Scope |
| -------------------------------------------------------- |

---

# 34. Persistence 기능 Summary

| 개발 목적기능/APIOwnerTX 필요LockPagingSQL 위치Migration 영향상세 |
| --------------------------------------------------- |

포함 항목:

- 단건 조회
- 목록 조회
- Paging
- Sorting
- Insert
- Update
- Delete
- Bulk
- Optimistic Lock
- Pessimistic Lock
- Unique Constraint
- Dynamic Query
- DB Vendor 차이

---

# 35. DB Migration Summary

| 작업명령Migration 파일대상 DB사전조건정상 결과실패 영향Rollback |
| ------------------------------------------- |

Migration 설명에는:

- 파일 경로
- Naming
- Version
- 신규 설치
- Upgrade
- 중복 실행
- Drift
- 기존 데이터 영향
- Long Lock 가능성
- Application 배포 순서
- Expand/Contract
- Rollback 가능 여부

를 작성한다.

---

# 36. DB Vendor 비교표

실제 지원 DB만 작성한다.

| 기능Vendor AVendor BVendor CCPF 추상화개발자 주의 |    |    |    |     |     |
| --------------------------------------- | -- | -- | -- | --- | --- |
| Paging                                  | 실제 | 실제 | 실제 | Y/N | ... |
| Sequence                                | 실제 | 실제 | 실제 | ... | ... |
| Lock                                    | 실제 | 실제 | 실제 | ... | ... |
| JSON                                    | 실제 | 실제 | 실제 | ... | ... |

직접 실행하지 않은 DB는 `미검증`으로 표시한다.

---

# 37. File / Attachment Summary

| 작업API저장소DB TX 관계최대 크기허용 형식PermissionAudit실패 복구 |
| ---------------------------------------------- |

부분 실패 조합:

| FileDB결과 상태발생 가능한 문제복구 |     |           |        |                      |
| ---------------------- | --- | --------- | ------ | -------------------- |
| 성공                     | 성공  | 완료        | -      | -                    |
| 성공                     | 실패  | Orphan 가능 | 불필요 파일 | Cleanup/Compensation |
| 실패                     | 미수행 | 실패        | 저장 없음  | Retry 조건             |

---

# 38. Config / Property Summary

| 기능PropertyENVTypeDefault필수범위ConsumerProfile재기동Secret상세 |
| ------------------------------------------------------ |

Property 상세에는:

- Key
- ENV 변환명
- Type
- Default
- Required
- 범위
- Consumer Class
- Profile
- Secret 여부
- 동적 반영 여부
- 재기동 여부
- 잘못된 값
- Startup 실패 여부
- 정상 확인 방법
- Rollback

을 작성한다.

---

# 39. Source Navigation Map

Directory Tree를 단순 나열하지 않는다.

개발자가 하려는 일을 기준으로 Source를 연결한다.

| 하려는 일Source 위치역할수정 가능관련 Test관련 Config관련 SQL |       |                     |      |         |           |          |
| ------------------------------------------- | ----- | ------------------- | ---- | ------- | --------- | -------- |
| API 추가                                      | 실제 경로 | Controller          | Y    | 실제 Test | 실제 Config | -        |
| Transaction 확인                              | 실제 경로 | Interceptor/Manager | 보통 N | 실제 Test | 실제 Config | -        |
| Paging 사용                                   | 실제 경로 | Public API          | N    | 실제 Test | -         | 실제 Query |

---

# 40. Module Ownership 표

| 기능Owner ModulePublic ContractInternal 구현ConsumerDB 소유외부 Dependency |
| ------------------------------------------------------------------ |

개발자가 임의로 같은 기능을 자기 Module에 재구현하지 않도록 Owner를 분명하게 작성한다.

---

# 41. Dependency Matrix

| FromTo허용호출 방법금지 이유사용해야 할 대안 |
| --------------------------- |

예:

- API → Application
- Application → Domain
- Domain → Persistence Port
- Domain → Infrastructure
- Consumer Module → Owner Internal
- Owner → Consumer

실제 Architecture에 따라 작성한다.

---

# 42. Tutorial 작성 기준

Tutorial은 코드 몇 개를 보여주는 예제가 아니다.

하나의 업무를 요구사항부터 배포 인계까지 끝낸다.

예제 업무는 너무 단순한 Hello World를 사용하지 않는다.

최소 다음 요소를 포함하는 업무를 선택한다.

- Create 또는 Update
- Validation
- DB
- Transaction
- Permission
- Audit
- Expected Version
- Idempotency
- Event 또는 Remote 중 하나 이상
- 정상 Test
- 오류 Test
- Fault Test
- 복구
- ADM 확인

---

# 43. Tutorial 시작 시 요구사항 표

| 요구사항 ID업무 요구사항CPF 기능구현 위치Test |
| ----------------------------- |

예:

- 연락처 형식 검증
- 수정 권한
- Version 충돌
- 동일 요청 중복 방지
- Audit
- Event 발행

각 요구사항이 Source와 Test에 연결되어야 한다.

---

# 44. Tutorial 파일 목록 표

코드를 작성하기 전에 생성·수정할 파일 전체를 먼저 보여준다.

| 순서파일역할생성 방식개발자 수정관련 Test |
| ------------------------ |

이를 통해 개발자는 최종 구조를 먼저 이해한다.

---

# 45. Tutorial 각 단계 작성 방식

각 단계에는 다음 형식을 적용한다.

## 목적

이번 단계에서 무엇을 만드는지.

## 선행 조건

앞 단계에서 무엇이 완료되어야 하는지.

## 파일 위치

Repository Root 기준 실제 경로.

## 전체 코드

핵심 업무 Source는 생략된 조각이 아닌 따라 작성 가능한 수준으로 제시한다.

## 코드 설명

CPF Framework에 중요한 부분을 설명한다.

## 실행

실제 명령.

## 정상 결과

- Console
- HTTP Response
- DB
- Log
- Event

중 해당되는 것을 작성한다.

## 실패 예

개발자가 흔히 만드는 오류를 최소 한 가지 제시한다.

## Test

실제 Test 코드 또는 작성 위치와 검증 내용을 제공한다.

## 다음 단계

현재 구현이 다음 구성요소와 어떻게 연결되는지 설명한다.

---

# 46. API 개발 장 최소 내용

각 REST API 개발 설명에는 다음을 포함한다.

- URI
- Method
- Controller
- Request
- Response
- Validation
- Error Response
- Authentication
- Permission
- Data Scope
- Idempotency
- Expected Version
- Command/Query Mapping
- Transaction
- OpenAPI
- Test
- 실제 호출 예
- 실패 응답 예

---

# 47. Request / Response Field 표

| FieldType필수DefaultValidationFormat설명Masking |
| ------------------------------------------- |

API 응답에는:

- 정상
- Validation 실패
- Permission 실패
- Conflict
- Not Found
- Timeout
- `UNKNOWN_RESULT` 표현이 API에 존재하는 경우 해당 응답

을 실제 구현 기준으로 설명한다.

---

# 48. 상태 전이 문서

상태를 가지는 기능은 반드시 상태 Catalog와 Transition을 작성한다.

## 상태 Catalog

| 상태의미진입 조건다음 가능 상태개발자 조치ADM 조치 |
| ----------------------------- |

## 상태 Transition

| 현재 상태Event/Command다음 상태DB 변경EventAudit실패 시 |
| ------------------------------------------ |

Source에 없는 상태 이름을 만들지 않는다.

---

# 49. UNKNOWN\_RESULT 작성 기준

`UNKNOWN_RESULT`는 일반 실패와 분리한다.

반드시 다음을 작성한다.

- 정의
- 발생 조건
- 실패와 차이
- Client가 받은 응답
- Local 상태
- Remote 상태 가능성
- Idempotency 관계
- 즉시 Retry 위험
- Status Query
- Reconciliation
- Reprocess
- Compensation
- Audit
- ADM 확인
- 정상화 판정

---

# 50. UNKNOWN\_RESULT 판단표

| 상황성공 확정실패 확정결과 불명Retry다음 행동 |    |    |      |             |                  |
| --------------------------- | -- | -- | ---- | ----------- | ---------------- |
| 명확한 Business 거절             | N  | Y  | N    | 실제 정책       | 오류 처리            |
| 요청 전송 전 실패                  | 실제 | 실제 | 실제   | 조건부         | 실제 정책            |
| 요청 후 Timeout                | N  | N  | Y    | 바로 Retry 제한 | Status/Reconcile |
| 응답 전 Connection Reset       | N  | N  | Y 가능 | 조건부         | Reconcile        |

실제 Transport 구현에 따라 의미를 확인하여 작성한다.

---

# 51. Recovery Action 선택표

| 현재 상황RetryRestartReprocessReconcileCompensationRollback |      |    |    |    |    |                   |
| ------------------------------------------------------- | ---- | -- | -- | -- | -- | ----------------- |
| 일시 오류                                                   | 조건   | -  | -  | 조건 | -  | -                 |
| UNKNOWN\_RESULT                                         | 제한   | -  | 조건 | 우선 | 조건 | 이미 Remote 성공 시 불가 |
| Consumer 실패                                             | 실제   | 실제 | 실제 | 조건 | -  | 실제                |
| Migration 실패                                            | N/실제 | 조건 | -  | -  | -  | 실제 정책             |

각 Cell에 단순 Y/N 대신 조건을 작성한다.

---

# 52. Test Matrix

개발 기능별 필요한 Test를 한눈에 보여준다.

| 기능정상ValidationPermissionTX RollbackConcurrentDuplicateTimeoutPartial FailureRecoveryMulti-instance |
| -------------------------------------------------------------------------------------------------- |

Test가 필요 없으면 `해당 없음`으로 이유를 설명한다.

실행하지 않은 Test는 `미검증`으로 표시한다.

---

# 53. Test Reference

| Test 종류실제 위치목적실행 명령필요 Infra성공 기준결과 위치 |
| ------------------------------------- |

각 핵심 기능에는 최소 다음 중 해당 Test를 작성한다.

- Unit
- Integration
- API
- Repository
- Security
- Transaction
- Concurrency
- Idempotency
- Remote
- Kafka
- Recovery

---

# 54. Fault Injection Catalog

| 장애재현 방법대상 기능예상 상태예상 오류RetryReconcile정상화 기준 |
| ------------------------------------------ |

포함 검토 대상:

- DB Connection 실패
- Deadlock
- Lock Timeout
- Remote Connect 실패
- Remote Read Timeout
- Response Loss
- Kafka Broker 장애
- Consumer 실패
- Instance 종료
- Disk/Resource 관련 개발 영향이 있는 경우 해당 시나리오

---

# 55. 장애 설명 공통 구조

각 장애는 다음 순서로 작성한다.

1. 장애 목적
2. 선행 조건
3. 장애 발생 방법
4. 요청 실행
5. 예상 응답
6. DB 상태
7. Transaction 결과
8. Log
9. Metric
10. Trace
11. Event
12. ADM 상태
13. Retry 가능 여부
14. Reprocess
15. Reconcile
16. Compensation
17. 정상화 방법
18. 정상화 확인 기준

---

# 56. 증상 → 원인 → 조치 Quick Table

| 개발자가 보는 증상먼저 확인가능한 원인하지 말아야 할 행동개발자 조치상세 |                 |              |             |           |   |
| ---------------------------------------- | --------------- | ------------ | ----------- | --------- | - |
| API Timeout                              | Trace/Remote 상태 | Remote 지연    | 무조건 새 요청    | Status 확인 | § |
| Version Conflict                         | Version         | 동시 수정        | 무한 Retry    | 최신 조회     | § |
| Event 미발행                                | Outbox          | Publisher 실패 | 업무 DB 수동 수정 | Outbox 확인 | § |

---

# 57. ADM 확인표

| 개발 기능ADM 화면RoutePermission핵심 Field정상 상태실패 상태가능한 조치 |
| -------------------------------------------------- |

개발자가 자신의 코드가 운영에서 어떻게 보이는지 연결할 수 있어야 한다.

다음 항목을 확인한다.

- Command 상태
- Batch 또는 비동기 상태
- Event
- Audit
- Error
- Retry
- Reprocess
- Reconcile

실제 화면이 없는 기능에 가상 화면을 만들지 않는다.

---

# 58. Log / Metric / Trace 표

| 상황Log/EventMetricTrace핵심 식별자민감정보 제한확인 위치 |
| ---------------------------------------- |

개발자가 최소 다음 식별자를 연결할 수 있는지 확인한다.

- Request ID
- Trace ID
- Command ID
- Idempotency Key
- Event ID
- Job ID
- Attempt ID

실제 구현되는 식별자만 작성한다.

---

# 59. Cookbook 작성 기준

Cookbook은 기술 이름이 아니라 문제 중심 제목으로 작성한다.

좋은 예:

- 신규 CRUD 만들기
- Paging 목록 API 만들기
- 동시에 수정되는 데이터 보호하기
- 동일 요청 두 번 처리되지 않게 하기
- DB 변경 후 Event 발행하기
- 다른 CPF Module 호출하기
- Remote Timeout 처리하기
- Permission 추가하기
- Audit 남기기
- 실패 Event 재처리하기
- `UNKNOWN_RESULT` 정상화하기

각 Recipe는 다음 구성으로 작성한다.

1. 문제
2. 선택할 CPF 기능
3. 적용 조건
4. 사용하면 안 되는 경우
5. 필요한 파일
6. Public API
7. 전체 코드
8. Config
9. SQL
10. Test
11. 실패 사례
12. 복구
13. ADM 확인

---

# 60. Reference와 Guide 분리 원칙

Guide에는 개발자가 이해하고 적용하는 데 필요한 설명을 작성한다.

Reference에는 정확한 값을 빠르게 검색하도록 작성한다.

예:

### Guide

> 언제 Optimistic Lock을 사용하는가?

### Reference

> 어떤 Class, Annotation, Error Code를 사용하는가?

같은 정보를 여러 곳에서 수작업으로 다르게 정의하지 않는다.

Summary → Guide → Reference가 같은 Source 조사 결과를 공유하도록 작성한다.

---

# 61. 각 기능 장의 공통 작성 Template

모든 주요 기능은 해당되는 범위에서 다음 순서를 사용한다.

1. 기능 목적
2. 개발자가 이 기능을 찾는 상황
3. 사용하지 말아야 하는 경우
4. Owner Module
5. 실제 Consumer
6. Public API
7. SPI
8. Internal API
9. Command / Query
10. Annotation
11. Source 위치
12. Config / Property
13. SQL / Migration
14. 선행 조건
15. 입력값
16. Default
17. 전체 사용 코드
18. 실행 명령
19. 정상 처리 Sequence
20. 정상 결과
21. 상태 변화
22. Transaction
23. 동시성
24. Idempotency
25. Timeout
26. Error
27. 부분 실패
28. Retry
29. Restart
30. Reprocess
31. `UNKNOWN_RESULT`
32. Reconciliation
33. Compensation
34. Rollback
35. Permission
36. Data Scope
37. Masking
38. Reason
39. Approval
40. Audit
41. Log
42. Metric
43. Trace
44. Test
45. Fault Injection
46. ADM 확인
47. 운영 인계
48. 제한사항
49. 미검증

해당하지 않는 항목은 억지로 설명을 만들지 않고 `해당 없음 — 이유`를 기록한다.

---

# 62. 각 장 시작 Mini Summary

긴 장은 반드시 시작 부분에 Mini Summary를 둔다.

예: Transaction 장

| 기능실제 CPF 구성요소개발자가 직접 사용같이 봐야 하는 기능상세 |        |      |             |   |
| ------------------------------------ | ------ | ---- | ----------- | - |
| Transaction                          | 실제 API | Y/간접 | Command     | § |
| Rollback                             | 실제 구현  | 간접   | Exception   | § |
| Version                              | 실제 API | Y    | Concurrency | § |
| Outbox                               | 실제 API | Y    | Kafka       | § |

그리고 질문별 이동표를 둔다.

| 궁금한 내용바로 볼 절                 |   |
| ---------------------------- | - |
| Transaction이 어디서 시작되는가       | § |
| Command 안에서 Command 호출 가능 여부 | § |
| `REQUIRES_NEW` 사용 가능 여부      | § |
| DB와 Kafka를 같이 처리하는 법         | § |
| Remote Timeout 뒤 Rollback 여부 | § |

---

# 63. 표 작성 공통 규칙

## 63.1 첫 번째 열

가능하면 기술명보다 개발자의 목적이나 상황을 배치한다.

나쁜 예:

`Class | 설명`

권장:

`하려는 일 | 사용할 Class/API`

## 63.2 판단 정보 우선

개발자가 판단하는 순서대로 배치한다.

예:

`목적 → API → TX → Idempotency → Permission → Retry → 상세`

## 63.3 Y/N 금지

가능하면 조건을 같이 작성한다.

나쁜 예:

`Retry = Y`

권장:

`조건부 — 동일 Idempotency Key가 유지되는 경우`

## 63.4 빈 Cell 금지

다음 상태를 사용한다.

- 해당 없음
- 미구현
- 미검증
- 재확인 필요

## 63.5 식별자 표현

다음은 코드 표기를 사용한다.

- Class
- Method
- Property
- Command
- Query
- Annotation
- Route
- File Path

## 63.6 긴 설명 제한

표에는 판단에 필요한 내용만 작성하고 긴 이유는 상세 절로 연결한다.

## 63.7 상세 링크

Summary 표의 모든 주요 행은 상세 Guide 또는 Reference로 연결한다.

---

# 64. 코드 예제 작성 기준

코드는 단순 문법 예가 아니라 실제 개발자가 시작할 수 있는 형태로 작성한다.

핵심 Tutorial에서는 다음을 명확하게 보여준다.

- Package 선언
- Import
- Class
- Annotation
- Constructor
- Method
- Request/Response
- Repository
- Transaction
- Error
- Test

`...`로 핵심 로직을 생략하여 개발자가 다시 Source를 찾아야 하는 예제는 피한다.

단, Framework 내부 구현 전체를 복사하는 것은 목적이 아니다.

개발자가 직접 작성해야 할 Source와 Framework가 제공하는 API의 경계를 보여준다.

---

# 65. 명령 실행 결과 작성 기준

명령을 제공할 경우 최소 하나의 정상 결과를 설명한다.

예:

- BUILD SUCCESSFUL
- Test count
- 생성된 Artifact 경로
- Migration 적용 Version
- 실행 Port
- Health Status

실제 실행하지 않은 결과를 실행한 것처럼 기술하지 않는다.

---

# 66. 오류 예제 작성 기준

정상 예제만 작성하지 않는다.

주요 기능마다 최소한 해당되는 오류를 작성한다.

- 잘못된 입력
- 존재하지 않는 ID
- Permission 부족
- Version 충돌
- Duplicate Key
- Timeout
- Network Failure
- DB Error
- Event Failure

오류 설명에는:

- 입력
- 응답
- Error Code
- HTTP Status
- Rollback
- Retry 가능 여부
- 개발자 조치

를 연결한다.

---

# 67. 개발 완료 Checklist 작성 기준

Checklist는 단순 Yes/No 목록으로 만들지 않는다.

| 검사항목적용 대상확인 방법Test/명령정상 기준Evidence상태 |
| ------------------------------------ |

상태는 다음 중 하나를 사용한다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

---

# 68. 신규 업무 개발 완료 Checklist

최소 다음을 확인한다.

## Architecture

- 올바른 Module에 작성했는가
- Layer 의존 방향을 지켰는가
- Owner Internal을 직접 호출하지 않았는가

## API

- Request/Response가 정의됐는가
- Validation이 있는가
- Error Mapping이 있는가

## Command / Query

- 상태 변경과 조회가 적절히 분리됐는가
- Command 입력이 정의됐는가
- Query 조회 제한이 있는가

## Transaction

- Boundary가 명확한가
- Rollback 정책을 확인했는가
- Remote 호출을 무분별하게 TX에 포함하지 않았는가

## Concurrency

- Version 또는 Lock이 필요한지 판단했는가

## Idempotency

- 재전송 가능한 요청인지 판단했는가
- 필요한 경우 Key가 적용됐는가

## Security

- Permission
- Data Scope
- Masking
- Reason
- Approval

적용 여부를 확인했는가.

## Audit

- 중요 변경의 Audit가 남는가

## DB

- Migration이 있는가
- Upgrade 영향이 있는가
- Rollback 가능 여부가 정의됐는가

## Async / Remote

- Timeout
- Retry
- Idempotency
- `UNKNOWN_RESULT`

을 검토했는가.

## Test

- 정상
- 오류
- Boundary
- Concurrency
- Duplicate
- Failure
- Recovery

중 해당 Test가 있는가.

## ADM

운영자가 결과와 실패를 확인할 수 있는가.

## 배포 인계

- Config
- Migration
- Permission
- Kafka
- External Endpoint
- Secret
- Monitoring
- Rollback

정보가 정리됐는가.

---

# 69. 개발자 매뉴얼 품질 검수 시나리오

문서가 실제로 개발 가능한지 확인하기 위해 신규 개발자 관점의 시나리오를 사용한다.

예시 과제:

> 고객의 연락처를 변경하는 API를 구현한다.

조건:

- 특정 Permission을 가진 사용자만 실행 가능
- 전화번호 형식 검증
- 동시에 두 명이 수정하면 Version 충돌
- 동일 요청 재전송 시 중복 변경 금지
- 변경 Audit 기록
- 변경 Event 발행
- Kafka 장애 시 업무 DB 정합성 유지
- Timeout 상황 처리
- Test 작성
- ADM에서 처리 결과 확인

개발자는 문서만 보고 다음을 찾을 수 있어야 한다.

1. 어느 Module에 개발하는가
2. 어떤 Generator를 사용하는가
3. 어떤 파일을 만드는가
4. 어떤 Public API를 사용하는가
5. Command는 어떻게 작성하는가
6. Transaction은 어디서 잡는가
7. Version은 어떻게 전달하는가
8. Idempotency Key는 어떻게 사용하는가
9. Permission은 어떻게 적용하는가
10. Outbox는 어떻게 사용하는가
11. 어떤 Test 명령을 실행하는가
12. 장애 상태는 어떻게 확인하는가
13. Kafka 장애 후 어떻게 복구하는가
14. ADM 어디서 확인하는가

중요한 질문에서 Source 역분석이 필요하면 해당 부분을 보강한다.

---

# 70. 문서에서 피해야 할 작성 방식

다음 방식으로 작성하지 않는다.

### 기능 존재만 설명

> CPF는 Transaction을 지원한다.

대신:

> 어떤 Command에서 어느 API 또는 Interceptor에 의해 Transaction이 시작되고 어떤 Exception에서 Rollback되는지 설명한다.

### Class 목록만 나열

> `AService`, `BService`, `CUtil`

대신:

> 개발 목적 → 사용할 API → 입력/출력 → 제약 → 예제 순으로 작성한다.

### Swagger로 대체

OpenAPI가 있어도 다음은 매뉴얼에서 설명한다.

- 업무 목적
- Permission
- Transaction
- Idempotency
- Expected Version
- 실패/복구

### Source 링크로 대체

> 자세한 내용은 Source 참조

를 주요 설명의 대체 수단으로 사용하지 않는다.

### 정상 흐름만 설명

오류와 부분 실패, 복구가 중요한 기능은 정상 흐름만으로 완료 처리하지 않는다.

### 가상 API 생성

설명을 편하게 하기 위해 실제 CPF에 없는 Class, API, Command, Property, Permission을 Framework 기능처럼 작성하지 않는다.

---

# 71. 추적성 기준

주요 기능은 다음 연결이 가능해야 한다.

`업무 목적`

↓

`Public API / Command / Query`

↓

`Source`

↓

`Config`

↓

`SQL / Migration`

↓

`Test`

↓

`ADM / 운영 확인`

가능한 경우 각 Summary와 상세 Guide에서 이 연결을 유지한다.

---

# 72. 개발자 매뉴얼 전체 권장 흐름

## Part 1. 사용 안내

- 목적
- 독자
- 질문별 바로가기

## Part 2. CPF 개발 기능 Quick Reference

- Quick Finder
- 기능 Map
- API
- Command
- Query
- Annotation
- SPI
- 명령
- Transaction
- Security
- Async
- Recovery

## Part 3. Architecture와 개발 구조

- Module
- Layer
- Ownership
- Dependency
- Local/Remote

## Part 4. 개발환경과 명령

- 환경
- Build
- Run
- Test
- Generator
- Migration

## Part 5. 첫 업무 Tutorial

- 요구사항
- 생성
- API
- Command
- Domain
- DB
- Security
- TX
- Concurrency
- Idempotency
- Event
- Test
- Fault
- Recovery
- ADM
- 인계

## Part 6. 업무 개발 Guide

- API
- Application
- Domain
- Persistence

## Part 7. Public API / SPI / Annotation

## Part 8. Command / Query / Transaction

## Part 9. Concurrency / Idempotency

## Part 10. Local / Remote

## Part 11. Kafka / Async

## Part 12. 공통 기능 Catalog

## Part 13. File / Attachment / 외부연계

## Part 14. Security / Audit

## Part 15. DB Lifecycle

## Part 16. Test

## Part 17. Fault Injection / Recovery

## Part 18. ADM 확인

## Part 19. Cookbook

## Part 20. Reference

## Part 21. EDU 종합 실습

## Part 22. 개발 완료 Checklist

---

# 73. 개발자가 처음 문서를 열었을 때 기대되는 인지 흐름

첫 화면에서:

> CPF로 무엇을 개발할 수 있는지 알 수 있어야 한다.

Summary를 보면:

> 어떤 API·Command·Query·명령을 사용할 수 있는지 알 수 있어야 한다.

Tutorial을 보면:

> 신규 업무가 어떤 파일과 순서로 만들어지는지 알 수 있어야 한다.

상세 Guide를 보면:

> Transaction·Security·Kafka·Remote 같은 기능을 어떤 규칙으로 적용할지 판단할 수 있어야 한다.

Cookbook을 보면:

> 현재 해결하려는 실무 문제를 바로 찾아 적용할 수 있어야 한다.

Reference를 보면:

> 정확한 Class·Method·Property·명령·Error Code를 빠르게 확인할 수 있어야 한다.

Fault/Recovery 장을 보면:

> 실패했을 때 재시도할지, 결과를 확인할지, 재처리할지 판단할 수 있어야 한다.

ADM 연결을 보면:

> 자신이 만든 기능을 운영 환경에서 어떻게 확인하는지 알 수 있어야 한다.

---

# 74. 최종 검수 질문

개발자 매뉴얼을 완료 상태로 판단하기 전에 다음 질문을 다시 확인한다.

### 찾을 수 있는가

- CPF에 어떤 기능이 있는지 한눈에 찾을 수 있는가?
- Public API 목록을 찾을 수 있는가?
- Command/Query를 찾을 수 있는가?
- 개발 명령어를 찾을 수 있는가?

### 선택할 수 있는가

- 비슷한 기능 중 무엇을 써야 하는지 판단할 수 있는가?
- Optimistic Lock과 Idempotency의 차이를 판단할 수 있는가?
- Local과 Remote 호출을 선택할 수 있는가?

### 구현할 수 있는가

- 실제 경로가 있는가?
- 필요한 전체 코드가 있는가?
- 입력값과 Default가 있는가?
- Config와 SQL이 있는가?
- 실행 명령이 있는가?

### 실패를 이해할 수 있는가

- 어떤 Error가 발생하는지 알 수 있는가?
- Rollback 여부를 알 수 있는가?
- Timeout이 실패인지 `UNKNOWN_RESULT`인지 판단할 수 있는가?

### 복구할 수 있는가

- Retry 가능 여부를 알 수 있는가?
- Reprocess와 Reconcile의 차이를 알 수 있는가?
- 정상화 기준을 알 수 있는가?

### 검증할 수 있는가

- 어떤 Test를 작성해야 하는가?
- 어떤 명령을 실행하는가?
- 정상 결과가 무엇인가?
- ADM·Log·Metric·Trace에서 어디를 확인하는가?

### 운영에 넘길 수 있는가

- 신규 Config가 정리됐는가?
- Migration이 정리됐는가?
- Permission이 정리됐는가?
- Event/외부연계 변경이 정리됐는가?
- Rollback 조건이 정리됐는가?

이 질문에 중요한 공백이 남아 있다면 해당 부분은 보강 대상으로 처리한다.

---

# 75. 핵심 원칙

CPF 개발자 매뉴얼은 다음 세 가지를 동시에 제공해야 한다.

**첫째, 지도**

> CPF에 무엇이 있고 어디에 있는가.

**둘째, 작업 절차**

> 실제 업무를 어떤 순서로 개발하는가.

**셋째, 판단 기준**

> Transaction, Retry, Idempotency, Security, `UNKNOWN_RESULT`, Recovery 상황에서 무엇을 선택해야 하는가.

따라서 단순한 기능 소개서, API 목록, Source Reference, Tutorial 중 하나만으로 구성하지 않는다.

개발자가 실제 업무 중 던지는 질문을 기준으로:

**찾기 → 선택 → 구현 → 실행 → 실패 확인 → 복구 → 검증 → 운영 인계**

까지 하나의 개발자 매뉴얼 안에서 연결되도록 작성한다.