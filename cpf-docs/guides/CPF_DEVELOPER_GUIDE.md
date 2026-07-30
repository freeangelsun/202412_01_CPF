# CPF 개발자 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무 개발자, 모듈 책임자, 코드 검수자
> **목적**: 요구사항을 올바른 모듈과 공개 경계에 구현하고 실패·복구·운영·검증까지 완결한다.
> **관련 문서**: [구조와 배포 구성](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [기반 API](CPF_FOUNDATION_API_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | 기능별 Owner Module |
| 이 문서로 완료하는 일 | Requirement에서 Source·API·SQL·Test·운영·Evidence까지 하나의 완료 단위로 설계·구현·검증한다. |
| 적용 범위 | 모든 CPF 공식 Module과 생성 업무영역의 개발 흐름 |
| 주요 독자 | 업무 개발자, Framework 개발자, Reviewer, Test 개발자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 문서 목적

이 문서는 CPF를 사용해 업무 서비스를 설계·개발·검증하는 전체 절차를 정의한다. 단순한 코드 작성법이 아니라 다음을 일관되게 유지하는 것이 목표다.

- 모듈과 데이터 소유권
- 공개 API, SPI와 내부 구현 경계
- 동일 JVM과 분리 WAS의 계약 동등성
- 정상·오류·경계·부분 실패 처리
- 멱등성, 동시성, 결과 불명과 복구
- 보안, 권한, 감사와 운영 추적
- 데이터베이스, 생성기와 배포 산출물 정합성
- 테스트와 검증 증적

## 2. 개발 시작 전 판단

코드를 수정하기 전에 다음 질문에 답한다.

| 질문 | 확인 내용 |
|---|---|
| 어떤 요구사항인가 | 기능명보다 장기 추적 ID와 완료 조건 확인 |
| 소유자는 누구인가 | 모듈, DB, API, 운영 명령의 최종 책임 |
| 공개 계약인가 | 공개 API, 확장 SPI, 내부 구현 중 하나로 분류 |
| 실제 소비자는 누구인가 | 컨트롤러, 서비스, 작업자, 에이전트, 생성기, 화면 |
| 배포 구성이 달라도 성립하는가 | 로컬/원격, 단일/다중 인스턴스 |
| 실패하면 어떻게 복구하는가 | 재시도, 상태 대사, 재생, 보상 |
| 운영자가 무엇을 볼 수 있는가 | 상태, 이력, 오류, 원인, 조치 결과 |
| 보안 통제가 필요한가 | 인증, 권한, 사유, 승인, 감사, 마스킹 |
| DB와 생성기 영향은 무엇인가 | 스키마, 이관, 공급자 묶음, 생성 업무영역 |
| 어떻게 검증하는가 | 단위, 통합, 실행 환경, 브라우저, 검증 증적 |

## 3. 모듈 소유권

### 3.1 `cpf-core`

기술 기반과 배포 구조에 독립적인 계약을 소유한다.

- 표준 헤더와 거래 문맥
- 오류와 검증
- 로컬/원격 서비스 호출
- 재시도, 회로 차단기, 시간 예산
- 멱등성, 상태 전이, 잠금
- 비동기 봉투와 송신함/수신함 계약
- 파일·전문 기술 계약
- 비밀값 참조와 마스킹
- 로그, 추적, 지표 기반
- 공개 테스트 Kit

특정 업무, ADM 화면, BZA 정책, 배치 일정관리기 구현을 넣지 않는다.

### 3.2 `cpf-common`

여러 업무영역이 선택적으로 재사용하는 고객 업무 공통을 소유한다.

- 고객 공통 코드와 메시지
- 영업일 달력
- 고객 공통 검증과 오류 매핑
- 공통 마스킹·감사 정책
- 고객 공통 파사드와 Core SPI 구현

모든 호출이 `cpf-common`을 경유하도록 만들지 않는다.

### 3.3 업무·운영 모듈

- `cpf-admin`: 플랫폼 운영과 통제
- `cpf-biz-admin`: 고객 업무 관리자
- 생성 업무영역: 해당 업무 기능과 원장
- `cpf-batch`: 일정관리기, 작업자, 에이전트, 대량 실행
- `cpf-gateway`: 외부 진입과 경로 선택 정책 집행

### 3.4 금지되는 의존성

```text
cpf-core → cpf-common/admin/batch/domain        금지
ADM → 업무 Owner DB 직접 갱신                  금지
업무 Domain A → 업무 Domain B DB 직접 접근     금지
내부 Domain 호출 → Gateway 재경유              금지
외부 Module → com.cpf.core.internal             금지
```

## 4. 계층 구조

생성 업무영역과 공식 애플리케이션은 다음 책임을 분리한다.

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

컨트롤러는 입력·권한·응답 변환을 담당하고, 업무 상태 전이는 애플리케이션/업무영역에서 강제한다. 저장소는 저장 기술을 숨기되 업무 규칙을 대신하지 않는다.

## 5. 공개 API와 SPI

### 5.1 공개 API

고객 개발자가 직접 사용하는 안정 계약이다.

- 최소 입력으로 안전한 기본 동작
- 명확한 Null, 오류와 동시성 의미
- 한글 JavaDoc
- 예제와 테스트 Kit
- 버전 호환 정책

### 5.2 SPI

고객이나 업무 어댑터가 구현하는 확장 계약이다.

- 구현 책임과 호출 시점
- 시간 제한과 스레드
- 재시도 가능 여부
- 중복 호출과 멱등성
- 오류 분류
- 비밀값과 민감정보 처리
- 기본 구현 또는 명확한 미구성 오류

### 5.3 내부 구현

제품 내부 구현이다. 외부 모듈이 Import하지 않는다. 패키지와 Architecture Gate로 강제한다.

## 6. 표준 거래 문맥

모든 수신 흐름은 표준 헤더를 해석하고 거래 문맥을 만든다.

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

스레드 지역 문맥은 요청 종료 시 반드시 제거한다. 비동기 실행은 명시적으로 문맥을 전달한다.

## 7. 입력 검증

검증은 세 계층에서 수행한다.

1. 형식 검증: 길이, 형식, 필수값
2. 계약 검증: Enum, 허용 범위, 상호 배타 조건
3. 업무 검증: 상태, 권한, 참조 대상, 중복

검증 실패는 기반 시설 오류나 NotFound로 바꾸지 않는다.

```java
public record CreatePaymentRequest(
        @NotBlank String customerId,
        @Positive BigDecimal amount,
        @NotNull String currency
) {}
```

복수 필드 규칙은 명명된 Validator 또는 업무 정책으로 구현한다.

## 8. 오류 계약

최소 오류 분류:

| 분류 | 예 |
|---|---|
| 검증 | 입력 형식, 범위, 필수값 |
| 대상 없음 | 식별 대상 없음 |
| 충돌 | 버전 충돌, 중복 키, 상태 전이 불가 |
| 인증 필요 | 인증 없음 또는 만료 |
| 권한 없음 | 권한 부족 |
| Rate Limited | 호출량 제한 |
| 시간 제한 | 제한 시간 초과 |
| 대상 중단 | 대상 인스턴스 연결 불가 |
| 회로 차단 | 장애 보호 차단 |
| 결과 불명 | 상대 처리 여부 미확정 |
| 내부 구현 | 예상하지 못한 제품 내부 오류 |

외부 응답에는 SQL, 호스트, 스택 추적, 비밀값을 노출하지 않는다. 내부 상세는 마스킹된 로그와 추적에서 확인한다.

## 9. 페이징과 검색

대량 목록은 서버 페이징을 사용한다.

```java
CpfPageRequest page = CpfPages.request(0, 50);
long total = repository.count(condition);
List<Item> rows = repository.findPage(condition, page.offset(), page.size());
return CpfPages.page(rows, page, total);
```

원칙:

- 페이지는 0부터 시작
- 최대 크기 제한
- 안정적인 정렬
- 정렬 필드 허용 목록
- 대용량은 Slice 또는 HMAC Cursor
- 브라우저 전체 조회 후 잘라내기 금지
- 내려받기는 별도 권한·사유·상한 적용

## 10. 동시성

### 10.1 낙관적 잠금

```text
조회 version=5
→ 수정 요청 expectedVersion=5
→ UPDATE ... WHERE id=? AND version=5
→ 영향 행 0이면 409 Conflict
```

충돌을 자동 덮어쓰지 않는다.

### 10.2 임대와 Fencing

장시간 또는 다중 인스턴스 작업은 소유권과 세대를 함께 확인한다.

```text
claim owner=A, fencing=17
→ lease 만료
→ owner=B, fencing=18
→ A의 늦은 완료 요청은 fencing 불일치로 거부
```

### 10.3 분산 잠금

DB Row 잠금, 임대 또는 제품 잠금 SPI 중 책임에 맞는 방식을 사용한다. 잠금 획득 실패, 만료, 갱신과 해제 실패를 명확히 처리한다.

## 11. 멱등성과 재시도

명령은 `operationId` 또는 멱등성 키를 받는다.

- 같은 키와 같은 요청: 최초 결과 재사용
- 같은 키와 다른 요청: 충돌
- 처리 중 상태: 중복 실행하지 않고 진행 상태 반환
- 결과 불명: 무조건 재실행하지 않고 대사

재시도는 다음 조건을 모두 확인한다.

- 오류가 재시도 가능
- 요청이 멱등하거나 멱등성 보호가 있음
- 시간 예산이 남음
- 최대 횟수와 Backoff가 있음
- 독성 요청을 무한 반복하지 않음

## 12. 로컬/원격 서비스 호출

업무 서비스는 Typed 파사드를 사용한다.

```java
public interface AccountQuery {
    AccountResult find(AccountQueryRequest request);
}
```

- 로컬 어댑터: 동일 JVM Bean 호출
- 원격 어댑터: 공개 API 호출
- Caller는 배포 구성을 알지 않음
- 헤더, 오류, 시간 제한, 추적 의미 동일

원격 오류는 Transport 예외를 그대로 업무에 노출하지 않고 표준 오류로 변환한다.

## 13. 비동기와 메시징

업무 트랜잭션과 사건 저장은 송신함을 사용한다.

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

소비자는 같은 Message가 반복 전달될 수 있음을 전제로 한다.

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

## 15. 비밀값과 민감정보

비밀값은 원문이 아닌 참조로 전달한다.

```java
CpfSecretReference ref = CpfSecretReference.of("vault://payment/api-key");
try (CpfSecretValue value = secretProvider.resolve(ref)) {
    remoteClient.call(value);
}
```

금지:

- DTO `toString()`에 비밀값
- 명령 Line Argument에 Password
- Git 프로필에 인증정보
- 오류 메시지에 원문
- 검증 증적에 원문
- Raw JSON 전체 로그

## 16. 권한과 감사

서버가 인증 주체(Principal)을 기준으로 권한을 평가한다. 요청 본문의 `requestedBy`를 신뢰하지 않는다.

관리 명령 감사 필드:

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

1. 소유자와 Canonical 소스 확인
2. 스키마 변경
3. 이관과 되돌리기 작성
4. 3개 공급자 변환
5. Install/Seed/Verify Bundle 생성
6. 실행 환경 Mapper와 DTO 정합성 확인
7. 생성기 Template 영향 반영
8. 정적 정본 불일치 Gate
9. Fresh Install
10. Upgrade → Verify → 되돌리기 → Reapply
11. 검증 증적

Generated Bundle만 수동 수정하지 않는다.

## 18. 생성기 영향

다음 변경은 생성기 검토가 필수다.

- 패키지/모듈 규칙
- 헤더와 거래 식별자
- 오류와 페이징
- DB 프로필과 공급자 정책
- 로컬/원격 호출
- Messaging/파일/배치 Capability
- OpenAPI와 테스트
- 실행 환경 경로와 등록부 메타데이터

기존 생성 업무영역의 생성기 소유 영역도 정본 불일치를 확인한다.

## 19. 컨트롤러와 OpenAPI

컨트롤러는 다음을 문서화한다.

- 기능 목적
- 필요한 권한
- 입력 필드와 제약
- 정상 응답
- 검증·충돌·권한 없음·결과 불명 오류
- 멱등성 키
- 버전
- 대표 예제

`Map<String,Object>`를 공개 계약으로 사용하지 않는다.

## 20. 테스트 기준

### 단위

- 정상
- Null/빈 값/최대·최소
- 상태 전이
- 오류 분류
- 마스킹
- 재시도 판정
- 버전 충돌

### 통합

- 저장소와 트랜잭션
- 송신함/수신함
- 로컬/원격 동등성
- DB 공급자 매핑
- 권한과 감사
- 이관

### 실행 환경/장애 주입

- 대상 중단
- 시간 제한
- Commit 후 응답 유실
- 다중 인스턴스 Takeover
- 임대 만료
- Stale Fencing
- 메시지 중개 시스템 중단
- DB 장애와 복구

### 브라우저

- 401/403/409/500
- Loading/Empty/오류
- Double Click
- Stale 응답
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

- [ ] 요구사항과 소유자가 명확하다.
- [ ] 공개 API/SPI/내부 구현 경계가 맞다.
- [ ] 실제 소비자가 연결됐다.
- [ ] 로컬/원격 계약이 같다.
- [ ] 정상·오류·경계·부분 실패를 처리한다.
- [ ] 재시도, Idempotency, 결과 불명이 정의됐다.
- [ ] 다중 인스턴스에 안전하다.
- [ ] 권한·사유·승인·감사가 적용됐다.
- [ ] SQL·이관·되돌리기가 있다.
- [ ] 생성기 영향이 반영됐다.
- [ ] OpenAPI·JavaDoc·Guide가 갱신됐다.
- [ ] 테스트와 검증 증적이 소스 Commit과 일치한다.
- [ ] 저장소에 임시 산출물이 남지 않았다.

## 부록 A. 기능 하나를 끝까지 구현하는 예

결제 승인 기능을 예로 들면 다음 산출물을 같은 변경 단위로 다룬다.

1. `PAY` 업무영역의 공개 요청·응답과 오류 계약
2. 입력 검증과 권한 검증
3. 애플리케이션 서비스의 트랜잭션 경계
4. 도메인 상태 전이와 중복 승인 방지
5. 승인 원장과 송신함 저장
6. 외부 승인기관 호출의 시간 제한·멱등성·결과 불명 처리
7. 상태 조회·대사·보상 포트
8. ADM 거래 흐름과 운영 명령
9. 데이터베이스 설치·이관·되돌리기
10. 생성기 템플릿과 교육 예제
11. 단위·통합·실행·장애 테스트와 검증 증적

### 공개 요청 예

```java
public record ApprovePaymentCommand(
        String paymentId,
        BigDecimal amount,
        String currency,
        String operationId,
        long expectedVersion
) {}
```

### 상태 전이 예

```text
CREATED → APPROVING → APPROVED
                  └→ DECLINED
                  └→ UNKNOWN_RESULT → RECONCILING → APPROVED / DECLINED
```

상태 전이는 도메인 정책이 강제하고 컨트롤러나 저장소가 임의로 변경하지 않는다.

## 부록 B. 코드 검토 질문

- 이 클래스가 속한 모듈과 계층을 한 문장으로 설명할 수 있는가?
- 공개 계약이 내부 저장 기술이나 전송 라이브러리를 노출하는가?
- 동일 JVM과 원격 호출에서 같은 입력·오류·추적 의미를 유지하는가?
- 재시도 대상이 비멱등 명령이면 어떤 보호가 있는가?
- 응답 유실 뒤 실제 처리 결과를 어떻게 확인하는가?
- 다른 인스턴스가 같은 대상을 동시에 처리하면 어떻게 되는가?
- 로그·오류·`toString()`에 민감정보가 남는가?
- 운영자가 상태와 실패 원인을 확인하고 안전하게 조치할 수 있는가?
- DB 변경과 생성기 산출물이 함께 갱신됐는가?

## 부록 C. 로컬 문제 해결

| 증상 | 확인 | 조치 |
|---|---|---|
| 로컬 호출은 성공하고 원격 호출은 실패 | 헤더, 직렬화, 시간 제한, 오류 변환 | 계약 테스트와 원격 어댑터 통합 테스트 실행 |
| 같은 명령이 두 번 반영됨 | 멱등 키 저장, 요청 해시, 유일 제약 | 멱등 원장과 충돌 응답 확인 |
| 인스턴스 교체 뒤 늦은 완료가 반영됨 | 임대 만료, 세대 토큰 비교 | 갱신·완료 조건에 세대 토큰 포함 |
| 운영 화면에 거래가 끊겨 보임 | 문맥 전달, 비동기 봉투, 추적 식별자 | 경계마다 `transactionId`·`segmentId` 기록 |
| 오류 응답에 내부 정보 노출 | 예외 변환, 로그와 공개 메시지 분리 | 공개 오류 코드와 마스킹 메시지 사용 |

## 23. 하나의 기능을 완료하는 구현 순서

예를 들어 `결제 결과 조회` 기능을 추가한다면 다음 순서로 진행한다.

1. Requirement ID, 완료 조건, Owner Domain과 실제 Consumer를 기록한다.
2. Local·Remote·Async 중 필요한 호출 구성을 정하고 동일 의미의 공개 계약을 설계한다.
3. Request·Response·Error·Header·Idempotency·Version 계약을 먼저 작성한다.
4. Application Service와 Domain 상태 전이를 구현하고 Infrastructure 예외를 표준 오류로 변환한다.
5. Owner DB의 정본 Schema, Migration, Rollback, 공급자 3종과 Mapper를 함께 변경한다.
6. Controller/OpenAPI와 Local·Remote Adapter를 연결한다.
7. 권한·사유·승인·감사·마스킹과 운영 조회 Key를 적용한다.
8. Generator Golden Template과 `cpf-member`, `cpf-reference` 영향을 검토한다.
9. 정상·검증·권한·충돌·시간 제한·결과 불명·다중 인스턴스 Test를 작성한다.
10. 저비용 Gate를 통과시키고 필요한 Runtime·DB·Browser 검증 증적을 만든다.

## 24. 구현 완료를 금지하는 징후

- Interface만 있고 기본 구현이나 실제 Consumer가 없다.
- Remote Adapter만 있고 같은 JVM 경로가 다른 오류·Header 의미를 사용한다.
- 재시도가 있으나 멱등성·시간 예산·최대 횟수가 없다.
- 결과 불명을 단순 실패로 바꾸거나 무조건 재실행한다.
- Controller가 Request Body의 `requestedBy`를 신뢰한다.
- 운영 화면은 성공 메시지를 표시하지만 Owner 실행 환경의 결과를 확인하지 않는다.
- DB Migration만 있고 Fresh Install·Rollback·Generator 반영이 없다.
- 한 공급자 SQL만 실행하고 나머지는 복사본으로 완료 처리한다.
- Sample Test만 통과하고 부분 실패·다중 인스턴스·권한 Test가 없다.
- 과거 Commit의 Evidence를 현재 변경의 성공 근거로 사용한다.

## 25. Local·Remote 계약 동등성 검토표

| 계약 | Local | Remote | 검증 |
|---|---|---|---|
| 표준 Header | 실행 문맥에서 직접 전달 | HTTP/gRPC Header로 직렬화 | 동일 필수값·검증 오류 |
| 인증·권한 | Principal 문맥 | 서명·Token·mTLS 등 | 행위자와 권한 결과 동일 |
| 시간 예산 | Deadline 전달 | 연결·응답·전체 제한 | 남은 예산 감소 의미 동일 |
| 오류 | 표준 Exception/Result | Transport 오류 매핑 | 외부 계약 코드 동일 |
| 멱등성 | Repository/Service 보호 | Header·Request Key 전달 | 중복·충돌 결과 동일 |
| 추적 | 같은 Trace Context | W3C/표준 Header 전달 | Segment 부모·자식 연결 |
| 결과 불명 | 경계 실패 시 드묾 | 응답 유실·시간 제한 | 대사 계약과 상태 동일 |

## 26. Review 질문

- 이 추상화는 실제로 둘 이상의 Consumer가 필요로 하는가?
- Owner가 아닌 Module이 데이터나 내부 구현을 직접 알고 있지 않은가?
- 정상 경로보다 오류·취소·중복·재시작 경로가 더 위험하지 않은가?
- 다중 인스턴스에서 늦은 완료와 오래된 Version을 차단하는가?
- 운영 기능 실패가 원 거래 Transaction을 오염시키지 않는가?
- 문서의 예제가 실제 Public API와 Generator 결과를 사용하는가?
- 변경을 제거했을 때 남는 Dead Code·Config·SQL·Route·Evidence는 없는가?

## 27. 시도 원장과 확장 SPI 구현 기준

### 서비스 호출 Attempt Observer

Retry·Failover를 구현할 때 최종 Exception만 기록하지 않는다. `CpfServiceCallAttempt`를 사용해 대상, 시도 번호, 장애 전환, 상태·소요시간·실패 코드·결과 불명을 Observer에 전달한다. Observer는 원 호출의 성공·실패 의미를 바꾸지 않지만 기록 유실과 Spool 적체를 운영 가능하게 노출해야 한다.

### FILE_PROCESS Handler

업무 파일 처리기는 `FileProcessHandler` SPI를 구현하고 고정 `processorId()`를 반환한다. Worker가 전달한 Claim 경로·Hash·Definition Checksum·Fencing Token을 신뢰 경계로 사용한다. Handler가 임의 경로를 다시 조합하거나 Claim 전 파일을 처리하지 않는다.

### Gateway 경로 재작성

외부 수신 `pathPattern`과 내부 `targetPath()`를 분리하고 `CpfGatewayPathRewriter`의 검증 규칙을 사용한다. URL Decode 순서가 다른 자체 정규화 코드를 추가해 Traversal 차단을 약화시키지 않는다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Module Map | `settings.gradle` | Owner와 의존 방향 확인 |
| Public API/SPI | `cpf-core/src/main/java/com/cpf/core/api`, `cpf-core/src/main/java/com/cpf/core/spi` | 외부 소비 가능 계약 |
| Generated Reference | `cpf-member`, `cpf-reference` | 생성 표준과 실제 사용 예 |
| Architecture Gate | `check-architecture-ownership.ps1` | Internal·역방향·순환 위반 |
| 문서 Gate | `check-document-links.ps1`, `check-source-documentation-standard.ps1` | Guide·JavaDoc·Link 정합성 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
