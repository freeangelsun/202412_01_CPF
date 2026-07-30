# CPF 기반 API 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무 개발자, 공개 API 사용자, 공통 규격 검수자
> **목적**: 반복되는 자료구조·검증·시간·금액·식별·페이징 처리를 CPF 표준으로 구현한다.
> **관련 문서**: [개발자 가이드](CPF_DEVELOPER_GUIDE.md) · [공개 API와 생성 업무영역](CPF_PUBLIC_API_AND_GENERATED_DOMAIN_GUIDE.md)

---

## 1. 목적

CPF Foundation API는 업무 개발자가 JDK나 외부 라이브러리를 제각각 조합하면서 반복적으로 만드는 오류를 줄이는 공개 API다. 단순히 이름만 바꾼 Wrapper는 만들지 않으며 다음 가치가 있는 기능만 제공한다.

- Null과 빈 값 의미 통일
- 표준 제한과 안전한 기본값
- 금융 계산과 시간대 오류 방지
- 보안과 민감정보 보호
- 페이징, 헤더와 거래 식별자 통일
- 테스트 가능성
- 생성기와 EDU 재사용

## 2. 패키지

```text
com.cpf.core.api.util
com.cpf.core.api.page
com.cpf.core.api.header
com.cpf.core.api.logging
com.cpf.core.api.validation
com.cpf.core.api.security
```

업무 모듈은 `com.cpf.core.internal` 또는 `com.cpf.core.common` 구현을 직접 사용하지 않는다.

## 3. 문자열

### 3.1 `CpfStrings`

대표 책임:

- Trim 후 빈 문자열을 Null로 정규화
- 필수 문자열 검증
- 최대 길이 제한
- 안전한 축약
- 제어문자 제거
- 비교용 정규화

```java
String customerId = CpfStrings.requireText(request.customerId(), "customerId");
String memo = CpfStrings.nullIfBlank(request.memo());
String summary = CpfStrings.abbreviateForLog(memo, 200);
```

로그 축약은 멀티바이트 문자를 깨뜨리지 않고, 민감정보 마스킹을 대신하지 않는다.

## 4. 숫자와 금액

### 4.1 `CpfNumbers`

- 정수 안전 파싱
- 최소·최대 범위
- Overflow 검출
- Null 기본값 정책

### 4.2 `CpfDecimals`

금액과 비율 계산은 `double` 대신 `BigDecimal`을 사용한다.

```java
BigDecimal amount = CpfDecimals.money(request.amount(), 2);
BigDecimal fee = CpfDecimals.multiply(amount, rate, 2, RoundingMode.HALF_UP);
```

정책:

- Scale과 반올림 방식을 명시
- 통화별 소수 자릿수 적용
- 0으로 나누기 오류 명확화
- 문자열 숫자의 Locale 혼동 방지
- 과도한 Precision 제한

## 5. 날짜와 시간

### 5.1 `CpfDates`, `CpfTimes`, `CpfClock`

- ISO 날짜·시간 파싱
- 업무 기준일 계산
- `ZoneId` 기반 변환
- 테스트 가능한 Clock
- DST 경계 처리
- 시작/종료 구간 검증

```java
LocalDate businessDate = CpfDates.parseIso("2026-07-30");
Instant now = cpfClock.instant();
ZonedDateTime seoul = CpfTimes.atZone(now, ZoneId.of("Asia/Seoul"));
```

서버 기본값 시간대에 의존하지 않는다. 저장은 `Instant` 또는 명확한 Zone 계약을 사용하고 표시 시점에 변환한다.

## 6. 컬렉션

### 6.1 `CpfLists`

- Null-safe Immutable List
- Chunk
- Distinct
- Index
- 빈 목록 정규화
- 최대 크기 제한

### 6.2 `CpfMaps`, `CpfAttributes`

- Null-safe Map
- Key 존재 검증
- Typed 조회
- Case-sensitive 계약
- Attribute 크기와 깊이 제한

외부 입력 Map을 그대로 SQL, 헤더, 로그에 전달하지 않는다.

## 7. ID와 해시

### 7.1 `CpfIds`

업무 ID와 기술 추적 ID를 구분한다.

- `transactionId`: CPF Core 생성기
- `operationId`: 명령 멱등성
- 업무 PK: 해당 업무 소유자 정책
- UUID 사용 여부: 저장소와 정렬 정책에 따라 결정

### 7.2 `CpfHashes`

```java
String sha256 = CpfHashes.sha256Hex(bytes);
boolean matched = CpfHashes.constantTimeEquals(expected, actual);
```

Password 해시는 일반 SHA-256 API를 사용하지 않고 전용 Password Encoder를 사용한다.

## 8. 파일 경로

### 8.1 `CpfFiles`

- 기준 Directory 하위 경로만 허용
- `..`, 절대 경로, Symbolic Link 우회 방지
- 파일명 정규화
- Extension과 MIME 별도 검증
- Atomic Move 지원
- 체크섬

```java
Path target = CpfFiles.safeChild(uploadRoot, serverGeneratedName);
```

사용자 파일명을 저장 경로로 직접 사용하지 않는다.

## 9. 검증

### 9.1 `CpfValidation`

반복되는 전제조건을 표준 오류로 반환한다.

```java
CpfValidation.requireTrue(amount.signum() > 0, "amount", "금액은 0보다 커야 합니다.");
CpfValidation.requireAllowed(currency, Set.of("KRW", "USD", "EUR"), "currency");
```

업무 규칙은 단순 Utility에 숨기지 않고 명명된 업무 정책으로 구현한다.

## 10. 페이지, 슬라이스와 커서

### 10.1 `CpfPageRequest`

```java
CpfPageRequest request = CpfPages.request(page, size);
```

- 0-base
- 기본 크기
- 최대 크기
- Offset Overflow 검증
- Sort 별도 계약

### 10.2 `CpfPage<T>`

전체 건수가 필요한 운영 목록에 사용한다.

```java
CpfPage<Item> result = CpfPages.page(rows, request, total);
```

### 10.3 `CpfSlice<T>`

전체 Count 비용이 큰 목록에 사용한다.

```text
요청 size=100
→ DB에서 101건 조회
→ 앞 100건 반환
→ hasNext=true
```

### 10.4 `CpfCursorPage<T>`

Keyset Pagination에 사용한다.

Cursor에는 다음을 포함한다.

- 정렬 기준 값
- Tie-breaker PK
- Filter Fingerprint
- 만료 시각 또는 버전
- HMAC 서명

단순 Base64는 위변조 방지가 아니다.

## 11. 정렬

`CpfSort`의 필드는 저장소 허용 목록과 연결한다.

```java
Map<String, String> allowed = Map.of(
    "createdAt", "created_at",
    "customerName", "customer_name"
);
```

사용자 입력을 `ORDER BY` 문자열에 직접 연결하지 않는다.

## 12. 헤더

### 12.1 `CpfHeaders`

헤더 Literal을 소스 곳곳에 반복하지 않는다.

표준 분류:

- 거래 식별
- 추적
- 호출 소스/대상
- 채널
- Tenant
- 인증 문맥
- Idempotency
- Deadline
- Content/Locale

헤더는 허용 목록으로 전달하며, Hop-by-hop 헤더와 인증정보를 무조건 복사하지 않는다.

## 13. 거래 식별자

### 13.1 `CpfTransactionIdGenerator`

Canonical 형식:

```text
yyyyMMddHHmmssSSS(17)
+ SystemCode(3)
+ wasId(7)
+ sequence(7)
= 34자리
```

```java
String transactionId = generator.generateOrUse(inboundId);
```

규칙:

- 유효한 Inbound ID는 승계
- 독립 실행 시작은 신규 생성
- 하위 호출은 Global ID 재생성 금지
- Segment로 계층 표현
- 길이와 문자 규칙 검증
- 충돌 방지 Sequence

## 14. 실행 문맥

### 14.1 `CpfTransactionContext`

```java
try (CpfTransactionScope scope = context.open(metadata)) {
    service.execute();
}
```

- 중첩 범위
- 비동기 전달
- 종료 시 정리
- 로그 MDC 연결
- 추적 문맥 연결
- 테스트 격리

## 15. 비밀값

### 15.1 `CpfSecretReference`

```java
CpfSecretReference.of("vault://payment/client-secret");
```

### 15.2 `CpfSecretValue`

- 짧은 범위
- `char[]` 기반 처리
- 사용 후 `close()`
- `toString()` 원문 금지
- 직렬화 금지

공급자는 ENV, Vault, KMS, HSM 어댑터로 확장한다.

## 16. 마스킹

### 16.1 `CpfMasking`

지원 대상:

- 전화번호
- 이메일
- 주민·사업 식별자
- 계좌·카드
- Token과 비밀값
- JSON 중첩 객체
- Collection과 Map
- 예외 문맥

마스킹은 권한 없는 원문 조회를 정당화하지 않는다. 원문 접근은 별도 권한, 사유와 감사가 필요하다.

## 17. Business 달력

달력 소유자는 `cpf-common`이다.

```java
boolean businessDay = calendar.isBusinessDay("DEFAULT", date);
LocalDate next = calendar.nextBusinessDay("DEFAULT", date, 1);
```

기능:

- 주말
- 공휴일
- 기관별 달력
- 임시 휴일과 영업일
- 유효기간
- 버전
- 캐시와 무효화
- DB-less 주말 기본 모드

ADM은 Override를 관리하고, BAT와 업무영역은 동일 계약을 소비한다.

## 18. 사용 예제: 조회 API

```java
@GetMapping
public CpfPage<MemberResponse> search(
        @Valid MemberSearchRequest request,
        @Valid CpfPageRequest page
) {
    return memberQuery.search(request, page);
}
```

저장소는 Count와 페이지 조회를 분리하거나 성능에 맞는 전략을 사용한다.

## 19. 사용 예제: 안전한 명령

```java
public PaymentResult create(PaymentCommand command) {
    CpfValidation.requireText(command.operationId(), "operationId");
    return idempotency.execute(
        command.operationId(),
        command.canonicalHash(),
        () -> createInternal(command)
    );
}
```

## 20. API 추가 기준

새 Foundation API는 다음을 충족해야 한다.

1. 서로 다른 실제 소비자가 반복 사용한다.
2. JDK Wrapper 이상의 오류 감소 가치가 있다.
3. Null, 예외와 동시성 의미가 명확하다.
4. 공개 API와 JavaDoc이 있다.
5. 기본 구현 또는 SPI 연결이 있다.
6. 단위 테스트와 경계 테스트가 있다.
7. EDU와 생성 업무영역 사용 예제가 있다.
8. 기존 Utility와 책임이 중복되지 않는다.

## 21. 검증 체크리스트

- [ ] 공개 패키지에 위치한다.
- [ ] 내부 구현 Type이 노출되지 않는다.
- [ ] 한글 JavaDoc과 예제가 있다.
- [ ] Null/빈 값/최대·최소를 테스트한다.
- [ ] 스레드-safe 여부가 문서화됐다.
- [ ] 민감정보가 `toString()`에 노출되지 않는다.
- [ ] 생성기와 EDU가 같은 API를 사용한다.
- [ ] OpenAPI DTO와 의미가 일치한다.

## 부록 A. 페이징 기준 정본

CPF의 기본 페이지 크기는 20, 일반 API의 권장 최대 크기는 200이다. 제품 설정으로 허용할 수 있는 절대 상한은 500이며, 이를 초과하는 대량 조회는 커서·슬라이스·내보내기 작업으로 분리한다.

| 항목 | 기준 |
|---|---:|
| 시작 페이지 | 0 |
| 기본 크기 | 20 |
| 일반 권장 최대 | 200 |
| 설정 가능한 절대 상한 | 500 |
| 대량 처리 | HMAC 커서·슬라이스·비동기 내보내기 |

정렬 필드는 허용 목록으로 제한하고 사용자 입력을 SQL 식별자로 직접 연결하지 않는다.

## 부록 B. 금액과 반올림

```java
BigDecimal normalized = CpfDecimals.money(
        input,
        Currency.getInstance("KRW"),
        RoundingMode.HALF_UP
);
```

- 통화별 소수 자릿수를 명시한다.
- 계산 중간값과 최종 표시값의 반올림 시점을 구분한다.
- `double`을 금액 원장에 사용하지 않는다.
- 0으로 나누기, 정밀도 초과와 음수 허용 여부를 오류 계약으로 정의한다.

## 부록 C. 시간 처리

- 저장과 시스템 간 전달은 `Instant` 또는 명시적 오프셋을 사용한다.
- 사용자 입력 시간은 업무 시간대와 일광절약시간의 모호·존재하지 않는 시각을 검증한다.
- 테스트에서는 시스템 시계 대신 `CpfClock`을 주입한다.
- 만료 판정은 서버 시계와 허용 오차를 문서화한다.

## 부록 D. 커서 보안

커서는 정렬 키, 방향, 필터 지문, 만료 시각을 포함하고 서버 비밀키로 인증한다. 사용자가 값을 변경했거나 다른 검색 조건에 재사용하면 거부한다.

```text
version | sortKey | direction | filterHash | expiresAt | signature
```
