# CPF Public API와 Generated Domain 가이드

## 1. 목적

이 문서는 업무 개발자가 CPF 내부 구현에 종속되지 않고 Public API와 SPI만으로 신규 업무영역을 개발하는 방법을 설명한다. Generated Domain은 예외 규격이 아니라 실제 고객 업무영역의 표준 시작점이다.

## 2. 공개 경계

```text
업무 코드
 ├─ com.cpf.core.api.*
 ├─ com.cpf.core.spi.*
 └─ com.cpf.common.api.* 또는 고객 공통 공개 계약

CPF 내부
 ├─ com.cpf.core.internal.*
 └─ 구현 Adapter와 AutoConfiguration
```

Generated Source에서 내부 Package Import를 생성하지 않는다.

## 3. Generated Domain 구조

```text
cpf-payment/
├─ build.gradle
├─ deploy/
│  ├─ database/
│  ├─ runtime/
│  └─ manifests/
├─ src/main/java/com/cpf/payment/
│  ├─ api/
│  ├─ application/
│  ├─ domain/
│  ├─ adapter/
│  └─ config/
├─ src/main/resources/
│  ├─ application.yml
│  ├─ db/
│  └─ mybatis/
└─ src/test/
```

### 계층 책임

| 계층 | 책임 |
|---|---|
| API | HTTP, DTO, Validation, OpenAPI |
| Application | Use Case, Transaction, Port 조정 |
| Domain | 업무 규칙, 상태 전이, 정책 |
| Adapter | DB, Remote, Messaging, File |
| Config | Bean, Profile, Capability 연결 |

## 4. Golden Reference

`cpf-member`는 Generator Golden Reference Instance다. 다음을 확인하는 기준으로 사용한다.

- 생성 결과의 계층 구조
- Public API 사용
- DB Vendor Artifact
- Paging과 Validation
- OpenAPI와 JavaDoc
- Test 구조
- Runtime Profile
- Generator 소유 영역

특정 MBR 업무 규칙을 모든 Domain에 강제하지 않는다.

## 5. 실행 Annotation

온라인 거래는 Public Annotation을 사용한다.

```java
@CpfOnlineTransaction(
    executionId = "PAY-PAYMENT-CREATE",
    visibility = "PUBLIC",
    gatewayAllowed = true
)
@PostMapping("/payments")
public PaymentResponse create(@Valid @RequestBody PaymentRequest request) {
    return paymentApplication.create(request);
}
```

내부 전용 Endpoint:

```java
@CpfOnlineTransaction(
    executionId = "PAY-INTERNAL-SETTLEMENT",
    visibility = "INTERNAL",
    gatewayAllowed = false
)
```

실행 ID는 Domain 내에서 고유해야 하며 Generator가 충돌을 검사한다.

## 6. 표준 Header와 Context

Generated Controller는 Core Filter와 AutoConfiguration을 통해 다음을 사용한다.

- `transactionId`
- `segmentId`
- Channel
- Locale
- Tenant
- 인증 Principal
- Deadline
- Idempotency Key

업무 코드는 Header Literal을 직접 파싱하지 않고 Public Context API를 사용한다.

## 7. Local/Remote Facade

업무 계약은 Java Interface와 Typed DTO로 정의한다.

```java
public interface MemberQuery {
    MemberResult find(MemberQueryRequest request);
}
```

### Local Adapter

```java
@Component
class LocalMemberQueryAdapter implements MemberQuery {
    private final MemberApplicationService target;

    @Override
    public MemberResult find(MemberQueryRequest request) {
        return target.find(request);
    }
}
```

### Remote Adapter

```java
@Component
class RemoteMemberQueryAdapter implements MemberQuery {
    private final CpfHttpClient httpClient;

    @Override
    public MemberResult find(MemberQueryRequest request) {
        return httpClient.post("/members/query", request, MemberResult.class);
    }
}
```

Caller는 배포 Profile에 따라 Adapter가 선택되며 계약은 바뀌지 않는다.

## 8. Service Registry

독립 Runtime은 다음 Metadata를 등록한다.

- `serviceId`
- `systemCode`
- `moduleId`
- `instanceId`
- Host와 Port
- Protocol
- Health Endpoint
- Zone과 Cell
- Version
- Capability
- Internal/Public Endpoint
- Drain/Maintenance 상태

Registry가 없거나 대상이 없을 때 Remote 호출은 명확한 Target Down 오류를 반환한다.

## 9. HTTP 호출

`CpfHttpClient`는 다음을 제공한다.

- 표준 Header 전달
- Timeout Budget
- Retry 조건
- Circuit Breaker
- Instance 선택
- 오류 응답 변환
- Trace
- 멱등성
- 결과 불명 분류

비멱등 Command는 Transport 오류만으로 자동 재실행하지 않는다.

## 10. Paging

Generated Search API는 `CpfPageRequest`와 `CpfPage<T>`를 사용한다.

```java
public CpfPage<PaymentSummary> search(
        PaymentSearchCondition condition,
        CpfPageRequest page
) {
    long total = repository.count(condition);
    List<PaymentSummary> rows = repository.search(condition, page);
    return CpfPages.page(rows, page, total);
}
```

대용량 Timeline은 `CpfCursorPage<T>`를 사용한다.

## 11. Database

Generated Domain은 자신의 DB를 소유한다.

```text
DomainName = payment
SystemCode = PAY
Schema     = payDB 또는 고객 Profile의 physical schema
TablePrefix= pay_
```

업무 Domain은 다른 Domain Table을 FK로 직접 참조하지 않는다. 참조는 Public API, Event 또는 복제된 Read Model을 사용한다.

## 12. MyBatis와 SQL

Vendor별 Mapper 위치:

```text
mybatis/vendor/mariadb/mapper/payment/
mybatis/vendor/postgresql/mapper/payment/
mybatis/vendor/oracle/mapper/payment/
```

Runtime은 선택된 Vendor 경로만 로드하며 다른 Vendor로 Fallback하지 않는다.

SQL 규칙:

- Bind Parameter 사용
- 정렬 Allowlist
- Paging DB 실행
- 명시 Column
- Vendor 함수 최소화
- Query Owner 명확화
- Slow Query와 Index 검토

## 13. Messaging

Generated Domain은 `CpfBrokerClient`를 사용한다.

```java
brokerClient.enqueue(new CpfBrokerPublishRequest(
    eventId,
    topic,
    schemaVersion,
    payload,
    headers
));
```

업무 Transaction과 Outbox 저장은 하나의 Transaction에서 처리한다.

Consumer는 Inbox와 Idempotency를 사용한다.

## 14. 외부 연계

기관·서비스별 Adapter는 해당 업무 Domain이 소유한다.

```text
업무 Command
→ 요청 Mapping
→ 인증정보 Reference 해석
→ 외부 호출
→ 응답/오류 Mapping
→ 결과 불명 처리
→ 연계 이력
```

Core는 HTTP, 전문, 파일, Trace 기술 계약을 제공하고 업무 의미를 소유하지 않는다.

## 15. 파일

```java
CpfFileRequest fileRequest = new CpfFileRequest(
    endpointId,
    serverGeneratedName,
    contentReference,
    checksum,
    credentialReference
);
CpfFileResult result = fileTransferClient.send(fileRequest);
```

파일 원문이나 Credential을 Log에 넣지 않는다.

## 16. Batch Job Pack

업무 Domain은 Job과 Step을 Job Pack SPI로 제공한다.

```java
@Component
class PaymentSettlementJobPack implements CpfBatchJobPack {
    @Override
    public String jobId() {
        return "PAY-SETTLEMENT";
    }
}
```

BAT Runtime은 Job Pack을 검색하고 Version, Checksum과 Parameter Schema를 검증한 뒤 실행한다.

업무 Job을 `cpf-batch` 제품 Source에 직접 적치하지 않는다.

## 17. Center-Cut

Generated Capability `center-cut`은 다음을 만든다.

- Target Provider
- Item Handler
- Internal Item Endpoint
- DTO와 오류 계약
- Test Fixture
- Registry Metadata

동일 JVM은 SPI를 직접 호출하고, 분리 Runtime은 Internal Endpoint를 호출한다.

## 18. Secret

Generated Config에는 Secret 원문이 아니라 Reference만 둔다.

```yaml
cpf:
  external:
    payment-bank:
      credential-ref: vault://payment/bank-api
```

Runtime이 Provider를 통해 해석한다.

## 19. 외부 공개

Generated Endpoint의 기본값은 외부 공개 거부다. 공개하려면 다음을 선언한다.

- Gateway Binding
- 인증 방식
- 권한
- TLS
- Rate Limit
- Header Policy
- Timeout과 Retry
- 멱등성
- 연결시험
- 작성자·승인자 분리

## 20. 사용자 수정 영역

Generator는 파일을 다음으로 구분한다.

- Generator 소유: 재생성·동기화 가능
- 혼합 영역: Marker 기반 병합
- 사용자 소유: 덮어쓰기 금지

`generator-ownership.json`에 경로와 Checksum을 기록한다.

## 21. Generator 재실행

같은 입력으로 재실행할 때:

1. Manifest 확인
2. 기존 파일 Checksum 비교
3. 변경 없는 Generator 소유 파일 갱신
4. 사용자 수정 파일 충돌 보고
5. 새 파일 생성
6. 삭제 예정 파일 보고
7. Apply 전 Plan 확인

## 22. 신규 Domain 검증

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName "payment" `
  -SystemCode "PAY" `
  -DatabaseVendor "postgresql" `
  -DryRun
```

적용 후:

```powershell
.\gradlew.bat :cpf-payment:clean :cpf-payment:test :cpf-payment:assemble
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-generator-arbitrary-domain-parity.ps1
```

## 23. 충돌 검증

Generator는 다음을 생성 전에 거부한다.

- 중복 SystemCode
- Module/Package 충돌
- Route 충돌
- Port 충돌
- DB/Schema/Table Prefix 충돌
- Execution ID 충돌
- Config Key 충돌
- 예약 코드
- 사용자 파일 덮어쓰기

## 24. 삭제와 정리

Generated Domain 제거는 Manifest 기반으로 수행한다.

- Root `settings.gradle` 등록 제거
- Module Directory 제거
- Runtime Registry 후보 제거
- DB Artifact와 Profile 제거
- Local Domain Federation 제거
- 사용자 DB 데이터는 명시 승인 없이 삭제하지 않음

## 25. 완료 체크리스트

- [ ] Public API/SPI만 사용한다.
- [ ] 내부 Import가 없다.
- [ ] Local/Remote Adapter가 같은 계약을 구현한다.
- [ ] Header, Error, Paging이 표준이다.
- [ ] DB Owner가 독립적이다.
- [ ] 3개 Vendor Artifact가 생성된다.
- [ ] Messaging/File/Batch Capability가 실제 Consumer에 연결된다.
- [ ] 외부 공개 기본 거부가 적용된다.
- [ ] 사용자 수정 영역을 덮어쓰지 않는다.
- [ ] Build, Test, DB, OpenAPI와 Evidence가 일치한다.
