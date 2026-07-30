# CPF 공개 API와 생성 업무영역 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무 설계자, 공개 API 개발자, 생성 업무영역 개발자
> **목적**: 공개 API·SPI·내부 구현 경계를 지키며 동일 JVM·분리 WAS 양쪽에서 업무영역을 확장한다.
> **관련 문서**: [구조와 배포 구성](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [업무영역 생성기](CPF_GENERATOR_TOOL_GUIDE.md)

---

## 1. 목적

이 문서는 업무 개발자가 CPF 내부 구현에 종속되지 않고 공개 API와 SPI만으로 신규 업무영역을 개발하는 방법을 설명한다. 생성 업무영역은 예외 규격이 아니라 실제 고객 업무영역의 표준 시작점이다.

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

Generated 소스에서 내부 패키지 Import를 생성하지 않는다.

## 3. 생성 업무영역 구조

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
| API | HTTP, DTO, 검증, OpenAPI |
| 애플리케이션 | Use Case, 트랜잭션, Port 조정 |
| 업무영역 | 업무 규칙, 상태 전이, 정책 |
| 어댑터 | DB, 원격, Messaging, 파일 |
| 설정 | Bean, 프로필, Capability 연결 |

## 4. 기준 구현

`cpf-member`는 생성기 기준 구현 인스턴스다. 다음을 확인하는 기준으로 사용한다.

- 생성 결과의 계층 구조
- 공개 API 사용
- DB 공급자 산출물
- 페이징과 검증
- OpenAPI와 JavaDoc
- 테스트 구조
- 실행 환경 프로필
- 생성기 소유 영역

특정 MBR 업무 규칙을 모든 업무영역에 강제하지 않는다.

## 5. 실행 Annotation

온라인 거래는 공개 Annotation을 사용한다.

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

내부 전용 엔드포인트:

```java
@CpfOnlineTransaction(
    executionId = "PAY-INTERNAL-SETTLEMENT",
    visibility = "INTERNAL",
    gatewayAllowed = false
)
```

실행 ID는 업무영역 내에서 고유해야 하며 생성기가 충돌을 검사한다.

## 6. 표준 헤더와 문맥

Generated 컨트롤러는 Core Filter와 AutoConfiguration을 통해 다음을 사용한다.

- `transactionId`
- `segmentId`
- 채널
- Locale
- Tenant
- 인증 주체(Principal)
- Deadline
- Idempotency Key

업무 코드는 헤더 Literal을 직접 파싱하지 않고 공개 문맥 API를 사용한다.

## 7. 로컬/원격 파사드

업무 계약은 Java Interface와 Typed DTO로 정의한다.

```java
public interface MemberQuery {
    MemberResult find(MemberQueryRequest request);
}
```

### 로컬 어댑터

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

### 원격 어댑터

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

Caller는 배포 프로필에 따라 어댑터가 선택되며 계약은 바뀌지 않는다.

## 8. 서비스 등록부

독립 실행 환경은 다음 메타데이터를 등록한다.

- `serviceId`
- `systemCode`
- `moduleId`
- `instanceId`
- 호스트와 Port
- Protocol
- 상태 점검 엔드포인트
- Zone과 셀
- 버전
- Capability
- 내부 구현/공개 엔드포인트
- 배수·점검 모드 상태

등록부가 없거나 대상이 없을 때 원격 호출은 명확한 대상 중단 오류를 반환한다.

## 9. HTTP 호출

`CpfHttpClient`는 다음을 제공한다.

- 표준 헤더 전달
- 시간 예산
- 재시도 조건
- 회로 차단기
- 인스턴스 선택
- 오류 응답 변환
- 추적
- 멱등성
- 결과 불명 분류

비멱등 명령은 Transport 오류만으로 자동 재실행하지 않는다.

## 10. 페이징

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

대용량 시간선은 `CpfCursorPage<T>`를 사용한다.

## 11. 데이터베이스

생성 업무영역은 자신의 DB를 소유한다.

```text
DomainName = payment
SystemCode = PAY
Schema     = payDB 또는 고객 Profile의 physical schema
TablePrefix= pay_
```

업무영역은 다른 업무영역 Table을 FK로 직접 참조하지 않는다. 참조는 공개 API, 사건 또는 복제된 Read Model을 사용한다.

## 12. MyBatis와 SQL

공급자별 Mapper 위치:

```text
mybatis/vendor/mariadb/mapper/payment/
mybatis/vendor/postgresql/mapper/payment/
mybatis/vendor/oracle/mapper/payment/
```

실행 환경은 선택된 공급자 경로만 로드하며 다른 공급자로 Fallback하지 않는다.

SQL 규칙:

- Bind 매개변수 사용
- 정렬 허용 목록
- 페이징 DB 실행
- 명시 Column
- 공급자 함수 최소화
- 조회 소유자 명확화
- Slow 조회와 Index 검토

## 13. 메시징

생성 업무영역은 `CpfBrokerClient`를 사용한다.

```java
brokerClient.enqueue(new CpfBrokerPublishRequest(
    eventId,
    topic,
    schemaVersion,
    payload,
    headers
));
```

업무 트랜잭션과 송신함 저장은 하나의 트랜잭션에서 처리한다.

소비자는 수신함과 Idempotency를 사용한다.

## 14. 외부 연계

기관·서비스별 어댑터는 해당 업무영역이 소유한다.

```text
업무 Command
→ 요청 Mapping
→ 인증정보 Reference 해석
→ 외부 호출
→ 응답/오류 Mapping
→ 결과 불명 처리
→ 연계 이력
```

Core는 HTTP, 전문, 파일, 추적 기술 계약을 제공하고 업무 의미를 소유하지 않는다.

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

파일 원문이나 인증정보를 로그에 넣지 않는다.

## 16. 배치 작업 묶음

업무영역은 작업과 Step을 작업 묶음 SPI로 제공한다.

```java
@Component
class PaymentSettlementJobPack implements CpfBatchJobPack {
    @Override
    public String jobId() {
        return "PAY-SETTLEMENT";
    }
}
```

BAT 실행 환경은 작업 묶음을 검색하고 버전, 체크섬과 매개변수 스키마를 검증한 뒤 실행한다.

업무 배치 작업을 `cpf-batch` 제품 소스에 직접 적치하지 않는다.

## 17. Center-Cut 대량 실행

Generated Capability `center-cut`은 다음을 만든다.

- 대상 공급자
- Item Handler
- 내부 구현 Item 엔드포인트
- DTO와 오류 계약
- 테스트 Fixture
- 등록부 메타데이터

동일 JVM은 SPI를 직접 호출하고, 분리 실행 환경은 내부 구현 엔드포인트를 호출한다.

## 18. 비밀값

Generated 설정에는 비밀값 원문이 아니라 참조만 둔다.

```yaml
cpf:
  external:
    payment-bank:
      credential-ref: vault://payment/bank-api
```

실행 환경이 공급자를 통해 해석한다.

## 19. 외부 공개

Generated 엔드포인트의 기본값은 외부 공개 거부다. 공개하려면 다음을 선언한다.

- 게이트웨이 바인딩
- 인증 방식
- 권한
- TLS
- 호출량 제한
- 헤더 정책
- 시간 제한과 재시도
- 멱등성
- 연결시험
- 작성자·승인자 분리

## 20. 사용자 수정 영역

생성기는 파일을 다음으로 구분한다.

- 생성기 소유: 재생성·동기화 가능
- 혼합 영역: Marker 기반 병합
- 사용자 소유: 덮어쓰기 금지

`generator-ownership.json`에 경로와 체크섬을 기록한다.

## 21. 생성기 재실행

같은 입력으로 재실행할 때:

1. 명세서 확인
2. 기존 파일 체크섬 비교
3. 변경 없는 생성기 소유 파일 갱신
4. 사용자 수정 파일 충돌 보고
5. 새 파일 생성
6. 삭제 예정 파일 보고
7. 적용 전 Plan 확인

## 22. 신규 업무영역 검증

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

생성기는 다음을 생성 전에 거부한다.

- 중복 SystemCode
- 모듈/패키지 충돌
- 경로 충돌
- Port 충돌
- DB/스키마/Table Prefix 충돌
- 실행 ID 충돌
- 설정 Key 충돌
- 예약 코드
- 사용자 파일 덮어쓰기

## 24. 삭제와 정리

생성 업무영역 제거는 명세서 기반으로 수행한다.

- Root `settings.gradle` 등록 제거
- 모듈 Directory 제거
- 실행 환경 등록부 후보 제거
- DB 산출물과 프로필 제거
- 로컬 업무영역 Federation 제거
- 사용자 DB 데이터는 명시 승인 없이 삭제하지 않음

## 25. 완료 체크리스트

- [ ] 공개 API/SPI만 사용한다.
- [ ] 내부 Import가 없다.
- [ ] 로컬/원격 어댑터가 같은 계약을 구현한다.
- [ ] 헤더, 오류, 페이징이 표준이다.
- [ ] DB 소유자가 독립적이다.
- [ ] 3개 공급자 산출물이 생성된다.
- [ ] Messaging/파일/배치 Capability가 실제 소비자에 연결된다.
- [ ] 외부 공개 기본 거부가 적용된다.
- [ ] 사용자 수정 영역을 덮어쓰지 않는다.
- [ ] Build, 테스트, DB, OpenAPI와 검증 증적이 일치한다.

## 부록 A. 생성 업무영역 생명주기

```text
계획 → 충돌 검사 → 생성 → 빌드 → DB 설치 → 로컬 호출 → 원격 호출
→ 화면·운영 등록 → 패키징 → 배포 → 업그레이드 → 재생성 → 제거·되돌리기
```

각 단계는 명령, 입력, 출력, 실패 복구와 사용자 수정 보호 규칙을 가진다.

## 부록 B. 사용자 수정 영역

- 생성기가 소유하는 구조·설정·기본 계약은 소유 표식을 남긴다.
- 고객 업무 코드, 사용자 작성 정책과 화면 확장 영역은 별도 디렉터리나 확장 포트로 분리한다.
- 재실행은 정본과 실제 파일의 차이를 계획서로 보여주고 승인 없이 사용자 코드를 덮어쓰지 않는다.
- 제거는 다른 모듈 의존, DB 자료, 경로·권한·배치 정의를 검사한다.

## 부록 C. API 호환

공개 요청·응답 필드 추가에는 기본값과 구 소비자 처리 규칙이 필요하다. 필드 제거·의미 변경은 폐기 기간, 새 버전 경로 또는 명시적 호환 어댑터를 사용한다.

## 부록 D. 로컬·원격 동등성 시험

같은 입력을 로컬 어댑터와 원격 어댑터에 전달해 정상 결과, 검증 오류, 권한 오류, 충돌, 시간 초과와 결과 불명의 공개 의미가 같은지 비교한다.
