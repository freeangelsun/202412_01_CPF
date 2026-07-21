# CPF Developer Guide

## 1. Audience

이 문서는 CPF 기반 Backend·Frontend·Integration 개발자를 위한 표준 개발 가이드입니다.

## 2. Development Environment

### Required

- JDK 25
- Git
- Gradle Wrapper
- MariaDB 10.6 이상
- Node.js LTS와 npm
- IntelliJ IDEA 또는 VS Code
- Docker Desktop 또는 호환 Container Runtime — 선택

### Verify

```bash
java -version
./gradlew --version
node --version
npm --version
```

Windows:

```powershell
java -version
.\gradlew.bat --version
node --version
npm --version
```

## 3. Build the Repository

```bash
./gradlew clean build
```

빠른 Backend 검증:

```bash
./gradlew test
```

정적 품질 검증:

```bash
./gradlew qualityGate
```

특정 Module:

```bash
./gradlew :cpf-account:clean :cpf-account:test
```

## 4. Package and Feature Structure

```text
com.cpf.account.transfer/
├─ api/
│  ├─ AccountTransferFacade.java
│  ├─ TransferRequest.java
│  └─ TransferResult.java
├─ application/
│  └─ AccountTransferService.java
├─ domain/
│  ├─ Transfer.java
│  ├─ TransferStatus.java
│  └─ TransferPolicy.java
├─ port/
│  ├─ AccountBalancePort.java
│  └─ TransferNotificationPort.java
├─ adapter/
│  ├─ persistence/
│  ├─ remote/
│  └─ messaging/
├─ repository/
├─ validation/
├─ config/
└─ internal/
```

### Naming

- 읽을 수 있는 DomainName을 Package와 Module에 사용
- 3자리 SystemCode는 내부 식별자
- Interface에 무조건 `I` 접두사를 붙이지 않음
- 구현 Class명은 역할을 드러냄
- DTO와 Domain Entity를 분리
- `CommonUtil`, `BaseService`, `Helper` 같은 모호한 이름을 피함

## 5. Implementing a Use Case

### Step 1. Define the Contract

```java
package com.cpf.account.transfer.api;

public interface AccountTransferFacade {

    TransferResult transfer(TransferRequest request);
}
```

Public API에는 입력, 출력, 오류, 권한, 멱등성과 호환성 계약을 JavaDoc으로 작성합니다.

### Step 2. Validate Input

- 형식 Validation
- 필수값
- 범위
- 상호 조건
- 업무 상태
- 권한

형식 Validation과 업무 Validation을 분리합니다.

### Step 3. Implement Application Flow

Application Service는 업무 흐름을 조정합니다.

- Domain policy 호출
- Repository와 Port 조합
- Transaction 경계
- Idempotency
- Audit
- Event 발행

Controller에 업무 로직을 작성하지 않습니다.

### Step 4. Implement Ports and Adapters

외부 DB, REST, Broker와 File 의존성은 Port를 통해 격리합니다.

```java
public interface TransferNotificationPort {

    void notifyTransferCompleted(TransferCompletedEvent event);
}
```

Adapter는 Timeout, Retry, Circuit Breaker, Mapping과 기술 오류 변환을 담당합니다.

## 6. Local and Remote Calls

업무 Contract는 Local과 Remote가 공유합니다.

```java
CpfCallResult<TransferResult> result =
    transferClient.call(
        TransferRequest.of(...),
        CpfServiceCallOptions.builder()
            .timeout(Duration.ofSeconds(3))
            .idempotencyKey(idempotencyKey)
            .build()
    );
```

### Rules

- 내부 호출은 Gateway를 재경유하지 않음
- Retry는 멱등 요청 또는 조회 가능한 거래에만 적용
- 전체 Timeout budget을 하위 호출에 분배
- ThreadLocal만 의존하지 않고 async context 전파
- 오류를 임의 RuntimeException으로 감싸지 않음

## 7. Standard Headers

Application code는 표준 Context API를 통해 Header를 사용합니다.

```java
CpfTransactionContext context = CpfTransactionContext.current();

String transactionId = context.transactionId();
String traceId = context.traceId();
String segmentId = context.segmentId();
```

업무 코드가 Servlet Header를 직접 읽거나 변경하지 않습니다.

## 8. Error Handling

표준 오류는 다음을 포함합니다.

- 오류 코드
- 메시지 Key
- 메시지 Parameter
- HTTP 또는 transport status
- 오류 유형
- retry 가능 여부
- transactionId
- 상세 원인 식별자

예시:

```json
{
  "code": "ACC-TRANSFER-40901",
  "message": "출금 가능 잔액이 부족합니다.",
  "transactionId": "OACC-TR-0001",
  "retryable": false
}
```

### Do Not

- Stack trace를 Client에 노출
- 개인정보를 오류 메시지에 포함
- `500` 하나로 모든 오류 표현
- 외부기관 오류를 그대로 노출
- 성공 응답 안에 오류를 숨김

## 9. Transactions and Idempotency

### DB Transaction

- 하나의 업무 Aggregate 변경 범위에 사용
- 외부 호출을 긴 DB Transaction 안에서 수행하지 않음
- 독립 로그 저장은 별도 Transaction 또는 복구 spool 사용

### Idempotency

- 업무적으로 동일한 요청을 식별
- 상태와 응답을 저장
- 동시 요청을 제어
- 완료 응답을 재사용
- 처리 중·실패·결과 불명 상태를 구분
- 만료와 정리 정책 제공

## 10. External Calls

모든 외부 호출에 다음을 지정합니다.

- Endpoint policy
- Authentication
- Timeout
- Retry
- Circuit breaker
- Idempotency
- Request/response masking
- Audit
- Unknown result recovery

Timeout 후 상대 처리 여부를 알 수 없으면 재조회와 Reconciliation을 사용합니다.

## 11. Persistence and Migration

### Rules

- Table Owner는 하나
- Entity와 API DTO 분리
- N+1 방지
- Paging과 정렬 안정성
- 낙관적 또는 비관적 Lock 명시
- Index와 실행계획 검토
- 개인정보 Column과 보존기간 정의
- DDL을 Module Resource에 중복 보관하지 않음

Migration 파일에는 다음을 기록합니다.

- 변경 목적
- 적용 순서
- 데이터 변환
- Lock과 예상 시간
- Rollback 또는 복구 방법
- 지원 DB Vendor 차이

## 12. Logging and Audit

### Application Log

- 구조화 Field
- transactionId, traceId, segmentId
- systemCode, businessId
- elapsed time
- result status
- masked parameters

### Audit

다음은 Application log가 아니라 Audit event입니다.

- 관리자 로그인
- 권한 변경
- 운영 제어
- 재처리
- 다운로드
- 개인정보 조회
- Secret·인증서 변경
- 승인과 반려

원문 Token, Password, 주민번호, 계좌번호 전체와 인증서 Private Key를 기록하지 않습니다.

## 13. Security Development

- 입력값 allowlist
- Output encoding
- Parameter binding
- Path traversal 방지
- SSRF endpoint allowlist
- File type·size·virus 검증
- CSRF, CSP와 secure cookie
- Secret을 Source와 YAML에 저장 금지
- 관리자 기능은 권한·승인·감사 연결
- 운영 Profile에서 Test API 비활성

## 14. OpenAPI and JavaDoc

Public API에는 다음을 기록합니다.

- 기능 설명
- 권한
- Header
- Request/response example
- 오류 코드
- Idempotency
- Paging·sorting
- Deprecated와 replacement
- Since version

OpenAPI는 실제 Runtime Contract와 일치해야 합니다.

## 15. Testing Strategy

### Unit Test

- Domain policy
- Validation
- Mapper
- Error mapping
- State transition

### Integration Test

- Repository
- Transaction
- Migration
- Local Facade
- Remote Adapter
- Security policy

### Runtime Test

- 실제 프로세스
- 실제 DB
- HTTP 또는 메시지
- Timeout·Retry·Target down
- File log·DB log
- 다중 인스턴스
- 결과 불명과 복구

### Browser Test

ADM/BZA의 핵심 흐름:

- 로그인
- 권한
- 검색
- 상세
- 승인
- 다운로드
- 재처리
- 오류 표시
- 접근성

## 16. Frontend Development

```text
frontend/src/
├─ app/
├─ features/
│  ├─ transaction/
│  ├─ batch/
│  ├─ security/
│  └─ account/
├─ shared/
│  ├─ api/
│  ├─ components/
│  ├─ stores/
│  └─ utils/
└─ main.ts
```

필수 검증:

```bash
npm ci
npm run lint
npm run typecheck
npm run test
npm run build
```

API DTO를 화면에서 임의 해석하지 않고 TypeScript contract로 관리합니다.

## 17. Build and Quality Checklist

```text
[ ] Module Ownership이 맞다.
[ ] Public API, SPI, Internal 경계가 맞다.
[ ] 실제 Consumer가 연결됐다.
[ ] Local/Remote 동작이 일치한다.
[ ] 정상·오류·경계·부분 실패를 검증했다.
[ ] Retry·Unknown Result·Recovery를 검토했다.
[ ] 멱등성과 동시성을 검증했다.
[ ] SQL·Migration·Rollback을 반영했다.
[ ] 권한·감사·마스킹을 반영했다.
[ ] OpenAPI·JavaDoc·EDU를 갱신했다.
[ ] Unit·Integration·Runtime 검증을 수행했다.
[ ] 최신 Commit Evidence를 남겼다.
[ ] Dead Code와 임시 파일을 제거했다.
```

## 18. Prohibited Shortcuts

- Source 없이 문서만 완료
- Controller에서 Repository 직접 호출
- 다른 업무 DB 직접 접근
- 공통 Module에 임시 업무 코드 적치
- Sample을 제품 기능으로 대체
- Interface만 만들고 기본 구현 생략
- 실행하지 않은 Test를 성공으로 기록
- 무제한 Retry
- 개인정보 원문 Log
