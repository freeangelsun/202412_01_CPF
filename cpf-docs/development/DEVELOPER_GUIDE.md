# CPF Developer Guide

## 1. 개발 시작

- JDK 25
- Node/npm Repository 고정 Version
- PowerShell 7
- MariaDB
- Chrome/Edge

```powershell
pwsh
./gradlew clean test
```

## 2. 표준 Package

```text
com.cpf.account.transfer/
├─ api
├─ application
├─ domain
├─ port
├─ adapter
├─ validation
├─ config
└─ internal
```

## 3. Public API 사용 원칙

- 최소 입력
- Safe Default
- 추가 Option은 필요할 때
- Effective Config 조회
- Typed Error
- Testable Clock·Context

## 4. Utility

```java
CpfDates.format(date, CpfDatePattern.YYYYMMDD);
CpfStrings.trimToNull(value);
CpfLists.requireMaxSize(items, 100);
CpfMaps.getString(attributes, "business.code");
```

Null, Thread Safety, Limits, Error를 JavaDoc에 명시한다.

## 5. Web Client

```java
CpfClientResponse<Result> result =
    client.post(endpoint, request)
        .profile("default")
        .timeout(Duration.ofSeconds(3))
        .idempotencyKey(key)
        .execute(Result.class);
```

Default:

- Finite Timeout
- Header Allowlist
- Trace
- Masking
- Error Mapping
- Size Limit
- Observer
- Mutation Retry Off

## 6. Paging

### Small List

`ListResult<T>`

### Admin

`PageResult<T>`

### Count-free

`SliceResult<T>`

### Large/Changing

`CursorResult<T>`

Query는 Max+1로 Overflow를 확인한다.

## 7. Error

- Standard Code
- Field Error
- Retryable
- Unknown
- Compensation
- User/Operator Message
- Cause ID

## 8. Context

Servlet Header를 직접 읽지 않고 `CpfTransactionContext`를 사용한다.

## 9. Persistence

- Owner Table만 접근
- DTO/Entity 분리
- Stable Sort
- Lock 명시
- Index
- PII
- Retention
- Migration

## 10. Fixed-Length

Core Parser/Writer를 사용한다. 직접 substring 반복 구현 금지.

## 11. External

Institution Adapter는 EXS에서 구현한다.

## 12. Batch Handler

Handler는 Job Runtime·Lease·Retry를 직접 소유하지 않는다. Batch SPI를 구현한다.

## 13. Frontend

- Feature Folder
- Typed API
- Permission
- No Mock in Production
- Production Build

## 14. Test

- Unit
- Integration
- Runtime
- Browser
- Fault
- Recovery
- Contract
- Migration

## 15. JavaDoc

Public API:

- 목적
- 최소 예제
- 고급 예제
- Default
- Property
- Null
- Limit
- Error
- Thread Safety
- Security
- SPI
- Compatibility

## 16. 금지

- CommonUtil
- Arbitrary Map
- Arbitrary SQL
- Direct Domain DB
- Infinite Retry
- Raw Secret
- Raw PII Log
- UI-only Permission
- Sample을 제품 구현으로 간주
