# CPF Data Structure / Utility / Transform Guide

## 1. 정본

기술 범용 utility는 `cpf-core/src/main/java/com/cpf/core/api/util`이 정본이다. `cpf-common`에는 고객 업무 공통 규칙만 둔다. 기존 `com.cpf.common.utils`는 실제 Consumer가 0임을 확인한 뒤 `cleanup-r11-obsolete.ps1`로 제거한다.

## 2. 제공 Surface

- `CpfStrings`: null/blank/trim/길이/코드 정규화
- `CpfLists`: immutable/null-safe/distinct/partition/page
- `CpfMaps`: immutable map/index/value/mutable copy
- `CpfValues`: Map/전문/JSON 값의 String/Integer/Long/Decimal/Boolean/Date/Instant 변환
- `CpfJson`: DTO/Map/List ↔ JSON
- `CpfDates`, `CpfTimes`, `CpfClock`: 시간 표준
- `CpfIds`: UUID/temporary technical ID/SystemCode validation
- `CpfHashes`, `CpfFiles`, `CpfDecimals`, `CpfNumbers`, `CpfHeaders`, `CpfValidation`
- `CpfPages`: CPF 표준 Offset Page/Slice 편의

Utility는 JDK wrapper를 늘리는 것이 아니라 반복적인 null/validation/conversion 오류를 줄이는 기능을 우선한다.

## 3. Paging

Offset 방식:

```java
CpfPageRequest pageRequest = CpfPages.request(page, size);
CpfPage<Item> result = CpfPages.page(rows, pageRequest, totalCount);
```

EDU/작은 메모리 목록에서는 `CpfPages.offsetPage`를 사용할 수 있다. DB 대용량 목록은 반드시 SQL LIMIT/OFFSET 또는 Keyset을 사용한다.

Keyset 방식은 `CpfCursor`, `CpfCursorCodec`, `CpfHmacCursorCodec`, `CpfCursorPage`를 사용한다. DB PK 원문을 외부 Cursor로 노출하지 않고 HMAC codec으로 위변조를 검증한다.

## 4. JSON / Map / List

```java
Map<String,Object> map = CpfJson.map(json);
String jsonAgain = CpfJson.write(map);
Integer count = CpfValues.integer(map.get("count"));
BigDecimal amount = CpfValues.decimal(map.get("amount"));
```

JSON parse 실패나 지원하지 않는 Boolean 표현은 조용히 기본값으로 바꾸지 않고 예외를 반환한다.

## 5. 고정길이 전문 변환

`CpfFixedLengthTransforms`는 이미 검증된 `CpfFixedLengthParser/Writer/Layout` 위에 편의 변환만 제공한다.

- 전문 → `Map<String,Object>`
- 반복 group → `List<Map<String,Object>>`
- 전문 → JSON
- Map/JSON → 전문

Layout Metadata 없는 문자열을 길이 추정으로 자르지 않는다.

운영 로그는 `CpfFixedLengthLogDecoder`를 사용하며 `layoutId + version`이 Registry에 존재할 때만 field/group을 표시한다. 반환 값은 parser의 masked field/group만 사용한다.

## 6. Legacy Utils 제거

먼저 Dry Run:

```powershell
pwsh .\cpf-tools\scripts\cleanup-r11-obsolete.ps1 -WhatIf
```

실제 Consumer가 있으면 Script가 삭제 없이 실패한다. Consumer를 `cpf-core.api.util` 또는 명확한 `cpf-common` 업무 helper로 옮긴 뒤 다시 실행한다.
