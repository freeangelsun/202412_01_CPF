# CPF Foundation API 개발 가이드

## 1. 목적

업무 Domain이 JDK/Framework 기능을 제각각 포장하지 않고 CPF가 정한 작은 Public API와 자료구조를 사용하도록 한다.
단순히 이름만 바꾼 Wrapper는 추가하지 않으며, null 처리·표준 제한·보안·운영 정합성처럼 반복 오류를 줄이는 기능만 제공한다.

## 2. Public Utility

`com.cpf.core.api.util`

- `CpfStrings`: trim/null/필수 문자열/축약
- `CpfDates`, `CpfTimes`, `CpfClock`: 날짜 파싱, timezone 변환, 테스트 가능한 Clock
- `CpfNumbers`, `CpfDecimals`: 안전 파싱, 범위 제한, 금융 scale/rounding
- `CpfLists`, `CpfMaps`, `CpfAttributes`: null-safe immutable 자료구조, chunk, distinct, index
- `CpfIds`, `CpfHashes`, `CpfFiles`: 기술 ID, SHA-256, 안전한 Child Path
- `CpfValidation`: 반복 입력 검증
- `CpfPages`: 표준 Page/Slice 생성
- `CpfHeaders`: 표준 Header 이름/전달 Map

거대한 `CommonUtils`에 기능을 계속 추가하지 않는다. 새로운 Utility가 필요하면 실제 반복 Consumer가 있는지 확인하고 책임별 `Cpf*` API로 추가한다.

## 3. Page / Slice / Cursor 표준

`com.cpf.core.api.page`

- `CpfPageRequest`: 0-base page, 기본 최대 500건
- `CpfPage`: total count가 필요한 목록
- `CpfSlice`: `size + 1` look-ahead 기반 목록
- `CpfCursor`, `CpfCursorPage`: keyset/cursor 값과 응답
- `CpfCursorCodec`, `CpfHmacCursorCodec`: 외부 Cursor HMAC-SHA256 서명/위변조 검증
- `CpfSort`: Repository allow-list와 함께 사용하는 정렬 계약

```java
CpfPageRequest page = CpfPages.request(0, 50);
CpfPage<Item> result = CpfPages.page(items, page, totalCount);
```

외부에 Cursor를 노출할 때 단순 Base64만 사용하지 않는다. 운영 Secret을 Secret Manager/환경 주입으로 받아 `CpfHmacCursorCodec`으로 서명하고 검증한다.
요청의 sort field를 SQL `ORDER BY` 문자열에 직접 연결하지 않는다.

## 4. transactionId / Header

거래 ID는 `CpfTransactionIdGenerator`를 사용한다.

```java
String transactionId = transactionIdGenerator.generateOrUse(inboundId);
```

Canonical 형식은 34자리다.

```text
yyyyMMddHHmmssSSS(17) + SystemCode(3) + wasId(7) + sequence(7)
```

유효한 inbound ID는 승계하고 독립 Batch/Scheduler/Worker/Agent 시작은 Core Generator가 신규 발급한다.
같은 흐름의 계층은 새 Global ID를 만들지 않고 `transactionSegmentId`와 `parentSegmentId`로 표현한다.

Header literal을 신규 Source에 반복해서 쓰지 말고 `CpfHeaders`/Core Header Engine을 사용한다.

## 5. CMN Business Calendar

영업일/휴일 정책 Owner는 `cpf-common`이다.

```java
if (businessCalendar.isBusinessDay("DEFAULT", businessDate)) {
    // 실행
}
LocalDate next = businessCalendar.nextBusinessDay("DEFAULT", businessDate, 1);
```

ADM은 `/adm/api/business-calendars`에서 Override를 관리하고 BAT/Scheduler/업무 Domain은 `CmnBusinessCalendar`를 소비한다.
BAT 자체 영업일 Table은 만들지 않는다. 정식 MariaDB 제품 구성은 `cmn_business_calendar_day`를 사용한다.
DB-less Library 모드는 주말 기본 조회만 허용하고 변경은 fail-closed한다.

## 6. EDU / Generator

`cpf-reference`의 Foundation API EDU는 위 Public API와 자료구조를 사용한다.
Generated Domain도 같은 계약을 우선 사용하며 Domain별 Utility/Page/Header 규격을 새로 만들지 않는다.
Golden Generator의 Search DTO는 `CpfPageRequest`로 변환하고 keyset 예제는 `CpfSlice`를 사용한다. Generator 템플릿 변경 후 기존 Generated Domain은 `sync-generated-domain-artifacts.ps1 -Scope AllGeneratorOwned`로 drift를 확인한다.

## 7. 완료 조건

Foundation API 변경은 다음을 함께 확인한다.

1. Public API와 한글 JavaDoc
2. Unit Test
3. 실제 EDU/Consumer
4. Generator 영향
5. OpenAPI에 노출되는 계약 정합성
6. Guide 갱신
7. Full Verification 계획 반영
