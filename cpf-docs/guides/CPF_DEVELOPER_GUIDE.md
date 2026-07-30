# CPF 개발자 가이드

## 1. 문서 목적

이 문서는 CPF를 사용해 업무 서비스를 설계·개발·검증하는 전체 절차를 정의한다. 단순한 코드 작성법이 아니라 다음을 일관되게 유지하는 것이 목표다.

- Module과 데이터 소유권
- Public API, SPI와 내부 구현 경계
- 동일 JVM과 분리 WAS의 계약 동등성
- 정상·오류·경계·부분 실패 처리
- 멱등성, 동시성, 결과 불명과 복구
- 보안, 권한, 감사와 운영 추적
- Database, Generator와 배포 산출물 정합성
- Test와 Evidence

## 2. 개발 시작 전 판단

코드를 수정하기 전에 다음 질문에 답한다.

| 질문 | 확인 내용 |
|---|---|
| 어떤 Requirement인가 | 기능명보다 장기 추적 ID와 완료 조건 확인 |
| Owner는 누구인가 | Module, DB, API, 운영 명령의 최종 책임 |
| 공개 계약인가 | Public API, 확장 SPI, 내부 구현 중 하나로 분류 |
| 실제 Consumer는 누구인가 | Controller, Service, Worker, Agent, Generator, UI |
| 배포 구성이 달라도 성립하는가 | Local/Remote, 단일/다중 인스턴스 |
| 실패하면 어떻게 복구하는가 | Retry, Reconcile, Replay, Compensation |
| 운영자가 무엇을 볼 수 있는가 | 상태, 이력, 오류, 원인, 조치 결과 |
| 보안 통제가 필요한가 | 인증, 권한, 사유, 승인, 감사, 마스킹 |
| DB와 Generator 영향은 무엇인가 | Schema, Migration, Vendor Pack, Generated Domain |
| 어떻게 검증하는가 | Unit, Integration, Runtime, Browser, Evidence |

## 3. Module 소유권

### 3.1 `cpf-core`

기술 기반과 topology-independent 계약을 소유한다.

- 표준 Header와 거래 문맥
- 오류와 검증
- Local/Remote 서비스 호출
- Retry, Circuit, Timeout Budget
- 멱등성, 상태 전이, Lock
- 비동기 Envelope와 Outbox/Inbox 계약
- 파일·전문 기술 계약
- Secret Reference와 마스킹
- Logging, Trace, Metrics 기반
- Public Test Kit

특정 업무, ADM 화면, BZA 정책, Batch Scheduler 구현을 넣지 않는다.

### 3.2 `cpf-common`

여러 업무영역이 선택적으로 재사용하는 고객 업무 공통을 소유한다.

- 고객 공통 코드와 메시지
- 영업일 Calendar
- 고객 공통 Validation과 Error Mapping
- 공통 Masking·Audit 정책
- 고객 공통 Facade와 Core SPI 구현

모든 호출이 `cpf-common`을 경유하도록 만들지 않는다.

### 3.3 업무·운영 Module

- `cpf-admin`: 플랫폼 운영과 통제
- `cpf-biz-admin`: 고객 업무 관리자
- Generated Domain: 해당 업무 기능과 원장
- `cpf-batch`: Scheduler, Worker, Agent, Center-Cut
- `cpf-gateway`: 외부 진입과 Routing 정책 집행

### 3.4 금지되는 의존성

```text
cpf-core → cpf-common/admin/batch/domain        금지
ADM → 업무 Owner DB 직접 갱신                  금지
업무 Domain A → 업무 Domain B DB 직접 접근     금지
내부 Domain 호출 → Gateway 재경유              금지
외부 Module → com.cpf.core.internal             금지
```

## 4. 계층 구조

Generated Domain과 공식 Application은 다음 책임을 분리한다.

```text
api
 ├─ controller
 ├─ request / response
 └─ openapi
application
 ├─ service
 ├─ command / query
 └─ port
domain
 ├─ model
 ├─ policy
 └─ state machine
adapter
 ├─ persistence
 ├─ remote
 ├─ messaging
 └─ file
config
test
```

Controller는 입력·권한·응답 변환을 담당하고, 업무 상태 전이는 Application/Domain에서 강제한다. Repository는 저장 기술을 숨기되 업무 규칙을 대신하지 않는다.

## 5. Public API와 SPI

### 5.1 Public API

고객 개발자가 직접 사용하는 안정 계약이다.

- 최소 입력으로 안전한 기본 동작
- 명확한 Null, 오류와 동시성 의미
- 한글 JavaDoc
- 예제와 Test Kit
- 버전 호환 정책

### 5.2 SPI

고객이나 업무 Adapter가 구현하는 확장 계약이다.

- 구현 책임과 호출 시점
- Timeout과 Threading
- 재시도 가능 여부
- 중복 호출과 멱등성
- 오류 분류
- Secret과 민감정보 처리
- 기본 구현 또는 명확한 미구성 오류

### 5.3 Internal

제품 내부 구현이다. 외부 Module이 Import하지 않는다. Package와 Architecture Gate로 강제한다.

## 6. 표준 거래 문맥

모든 Inbound 흐름은 표준 Header를 해석하고 거래 문맥을 만든다.

```text
요청 수신
→ Header 검증
→ 인증·Tenant 문맥
→ transactionId 생성 또는 승계
→ segment 생성
→ Application Service
→ Local/Remote/Async/Batch 실행
→ Result/Error Mapping
→ Log/Trace/Audit
→ 문맥 정리
```

### 6.1 필수 식별자

- `transactionId`
- `segmentId`
- `parentSegmentId`
- `systemCode`
- `moduleId`
- `serverInstanceId`
- `operationId`
- `traceId`
- 실행 유형과 방향

ThreadLocal 문맥은 요청 종료 시 반드시 제거한다. 비동기 실행은 명시적으로 Context를 전달한다.

## 7. 입력 검증

검증은 세 계층에서 수행한다.

1. 형식 검증: 길이, Pattern, 필수값
2. 계약 검증: Enum, 허용 범위, 상호 배타 조건
3. 업무 검증: 상태, 권한, 참조 대상, 중복

검증 실패는 Infrastructure 오류나 NotFound로 바꾸지 않는다.

```java
public record CreatePaymentRequest(
        @NotBlank String customerId,
        @Positive BigDecimal amount,
        @NotNull String currency
) {}
```

복수 필드 규칙은 명명된 Validator 또는 Domain Policy로 구현한다.

## 8. 오류 계약

최소 오류 분류:

| 분류 | 예 |
|---|---|
| Validation | 입력 형식, 범위, 필수값 |
| Not Found | 식별 대상 없음 |
| Conflict | Version 충돌, 중복 키, 상태 전이 불가 |
| Unauthorized | 인증 없음 또는 만료 |
| Forbidden | 권한 부족 |
| Rate Limited | 호출량 제한 |
| Timeout | 제한 시간 초과 |
| Target Down | 대상 인스턴스 연결 불가 |
| Circuit Open | 장애 보호 차단 |
| Unknown Result | 상대 처리 여부 미확정 |
| Internal | 예상하지 못한 제품 내부 오류 |

외부 응답에는 SQL, Host, Stack Trace, Secret을 노출하지 않는다. 내부 상세는 마스킹된 Log와 Trace에서 확인한다.

## 9. Paging과 검색

대량 목록은 서버 Paging을 사용한다.

```java
CpfPageRequest page = CpfPages.request(0, 50);
long total = repository.count(condition);
List<Item> rows = repository.findPage(condition, page.offset(), page.size());
return CpfPages.page(rows, page, total);
```

원칙:

- Page는 0부터 시작
- 최대 크기 제한
- 안정적인 정렬
- 정렬 필드 Allowlist
- 대용량은 Slice 또는 HMAC Cursor
- Browser 전체 조회 후 잘라내기 금지
- Download는 별도 권한·사유·상한 적용

## 10. 동시성

### 10.1 Optimistic Lock

```text
조회 version=5
→ 수정 요청 expectedVersion=5
→ UPDATE ... WHERE id=? AND version=5
→ 영향 행 0이면 409 Conflict
```

충돌을 자동 덮어쓰지 않는다.

### 10.2 Lease와 Fencing

장시간 또는 다중 인스턴스 작업은 소유권과 세대를 함께 확인한다.

```text
claim owner=A, fencing=17
→ lease 만료
→ owner=B, fencing=18
→ A의 늦은 완료 요청은 fencing 불일치로 거부
```

### 10.3 분산 Lock

DB Row Lock, Lease 또는 제품 Lock SPI 중 책임에 맞는 방식을 사용한다. Lock 획득 실패, 만료, 갱신과 해제 실패를 명확히 처리한다.

## 11. 멱등성과 재시도

Command는 `operationId` 또는 멱등성 키를 받는다.

- 같은 키와 같은 요청: 최초 결과 재사용
- 같은 키와 다른 요청: 충돌
- 처리 중 상태: 중복 실행하지 않고 진행 상태 반환
- 결과 불명: 무조건 재실행하지 않고 대사

재시도는 다음 조건을 모두 확인한다.

- 오류가 재시도 가능
- 요청이 멱등하거나 멱등성 보호가 있음
- Timeout Budget이 남음
- 최대 횟수와 Backoff가 있음
- 독성 요청을 무한 반복하지 않음

## 12. Local/Remote 서비스 호출

업무 Service는 Typed Facade를 사용한다.

```java
public interface AccountQuery {
    AccountResult find(AccountQueryRequest request);
}
```

- Local Adapter: 동일 JVM Bean 호출
- Remote Adapter: Public API 호출
- Caller는 배포 구성을 알지 않음
- Header, 오류, Timeout, Trace 의미 동일

Remote 오류는 Transport 예외를 그대로 업무에 노출하지 않고 표준 오류로 변환한다.

## 13. 비동기와 메시징

업무 Transaction과 Event 저장은 Outbox를 사용한다.

```text
업무 원장 변경 + Outbox 저장
→ Commit
→ Publisher Claim
→ Broker 전송
→ ACK
→ Consumer Inbox 중복 확인
→ 업무 처리
→ 처리 결과 저장
```

Consumer는 같은 Message가 반복 전달될 수 있음을 전제로 한다.

## 14. 파일과 첨부

업로드 처리 순서:

```text
파일명·경로 정규화
→ 크기·확장자·MIME 검증
→ 임시 격리 저장
→ Checksum
→ 악성 파일 검사
→ CLEAN 또는 QUARANTINED
→ 권한 있는 Download
→ Retention
```

사용자 파일명을 서버 경로로 직접 사용하지 않는다.

## 15. Secret과 민감정보

Secret은 원문이 아닌 Reference로 전달한다.

```java
CpfSecretReference ref = CpfSecretReference.of("vault://payment/api-key");
try (CpfSecretValue value = secretProvider.resolve(ref)) {
    remoteClient.call(value);
}
```

금지:

- DTO `toString()`에 Secret
- Command Line Argument에 Password
- Git Profile에 Credential
- 오류 메시지에 원문
- Evidence에 원문
- Raw JSON 전체 Logging

## 16. 권한과 감사

서버가 인증 Principal을 기준으로 권한을 평가한다. Request Body의 `requestedBy`를 신뢰하지 않는다.

관리 Command 감사 필드:

- actor
- target
- action
- reason
- before / after
- operationId
- transactionId / traceId
- approvalId
- result
- failureCode
- occurredAt

감사 저장 실패 정책은 기능 위험도에 따라 안전 차단 또는 별도 Spool을 적용한다.

## 17. DB 변경 절차

1. Owner와 Canonical Source 확인
2. Schema 변경
3. Migration과 Rollback 작성
4. 3개 Vendor 변환
5. Install/Seed/Verify Bundle 생성
6. Runtime Mapper와 DTO 정합성 확인
7. Generator Template 영향 반영
8. 정적 Drift Gate
9. Fresh Install
10. Upgrade → Verify → Rollback → Reapply
11. Evidence

Generated Bundle만 수동 수정하지 않는다.

## 18. Generator 영향

다음 변경은 Generator 검토가 필수다.

- Package/Module 규칙
- Header와 거래 식별자
- 오류와 Paging
- DB Profile과 Vendor 정책
- Local/Remote 호출
- Messaging/File/Batch Capability
- OpenAPI와 Test
- Runtime Route와 Registry Metadata

기존 Generated Domain의 Generator 소유 영역도 Drift를 확인한다.

## 19. Controller와 OpenAPI

Controller는 다음을 문서화한다.

- 기능 목적
- 필요한 권한
- 입력 필드와 제약
- 정상 응답
- Validation/Conflict/Forbidden/Unknown 오류
- 멱등성 키
- Version
- 대표 예제

`Map<String,Object>`를 Public Contract로 사용하지 않는다.

## 20. Test 기준

### Unit

- 정상
- Null/빈 값/최대·최소
- 상태 전이
- 오류 분류
- 마스킹
- 재시도 판정
- Version 충돌

### Integration

- Repository와 Transaction
- Outbox/Inbox
- Local/Remote 동등성
- DB Vendor Mapping
- 권한과 감사
- Migration

### Runtime/Fault

- Target Down
- Timeout
- Commit 후 응답 유실
- 다중 인스턴스 Takeover
- Lease 만료
- Stale Fencing
- Broker 중단
- DB 장애와 복구

### Browser

- 401/403/409/500
- Loading/Empty/Error
- Double Click
- Stale Response
- Keyboard와 접근성
- 위험 조치 확인과 감사

## 21. 저비용 Gate

```powershell
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
pwsh -File .\cpf-tools\scripts\check-source-documentation-standard.ps1
pwsh -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

## 22. 변경 완료 체크리스트

- [ ] Requirement와 Owner가 명확하다.
- [ ] Public API/SPI/Internal 경계가 맞다.
- [ ] 실제 Consumer가 연결됐다.
- [ ] Local/Remote 계약이 같다.
- [ ] 정상·오류·경계·부분 실패를 처리한다.
- [ ] Retry, Idempotency, Unknown Result가 정의됐다.
- [ ] 다중 인스턴스에 안전하다.
- [ ] 권한·사유·승인·감사가 적용됐다.
- [ ] SQL·Migration·Rollback이 있다.
- [ ] Generator 영향이 반영됐다.
- [ ] OpenAPI·JavaDoc·Guide가 갱신됐다.
- [ ] Test와 Evidence가 Source Commit과 일치한다.
- [ ] Repository에 임시 산출물이 남지 않았다.
