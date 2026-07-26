# CPF Developer Guide

## 1. 목적
이 문서는 Core Platform Framework(CPF)를 사용해 업무 서비스를 개발할 때 지켜야 할 제품 표준을 설명한다. 샘플 코드의 편의보다 Public API/SPI 경계, Local/Remote topology 동등성, 복구 가능성, 운영 추적성, Generator 재현성을 우선한다.

## 2. Module Ownership
- `cpf-core`: topology-independent API/SPI, 실행 식별, 오류, HTTP, Paging, Secret/Retention/Tenant 계약.
- `cpf-common`: 고객 업무 공통과 공용 구현. 특정 Admin/Batch Runtime 소유 기능을 넣지 않는다.
- `cpf-admin`: 플랫폼 운영 API/UI. 업무 Owner DB를 직접 조회하지 않는다.
- `cpf-biz-admin`: 고객 업무 관리자/조직/권한/결재/첨부/알림.
- `cpf-batch`: Batch/Worker/Scheduler/Center-Cut/Retention 대량 실행 Runtime.
- Generated Domain: Generator로 생성한다. DomainName과 3자리 SystemCode를 분리한다.

외부 Module은 `com.cpf.core.common.*` 내부 구현을 직접 import하지 않는다. 확장에는 `com.cpf.core.api.*`와 `com.cpf.core.spi.*`를 사용한다.

## 3. 거래 식별과 실행 유형
온라인/공유/배치는 각각 O/S/B 실행유형으로 구분한다. 거래 식별자는 CPF 표준 생성기를 사용하고 Browser/Remote/Batch가 서로 임의 규격을 만들지 않는다. `transactionGlobalId`, segment, serverInstanceId를 로그와 운영조회에서 연결할 수 있어야 한다.

## 4. Paging
대량 목록 Public Contract는 `CpfPageRequest`와 `CpfPage<T>`를 사용한다.
- page는 0-base.
- 기본 pageSize 20, 최대 200.
- DB에서 LIMIT/OFFSET 또는 keyset/cursor를 적용한다.
- 전체 조회 후 Java `subList`로 잘라 Paging 완료 처리하지 않는다.
- 운영 목록은 stable ordering을 가져야 한다.

예시:
```java
CpfPageRequest page = CpfPageRequest.of(0, 50);
long total = repository.count(filter);
List<Member> rows = repository.findPage(filter, page.offset(), page.size());
return CpfPage.of(rows, page, total);
```

## 5. 동시성·멱등성
관리/업무 원장의 변경은 다음 중 필요한 계약을 명시한다.
- `expectedVersion`: lost update 방지 CAS.
- `operationId`/idempotency key: timeout 재시도 중복 방지.
- 동일 operationId + 동일 canonical request는 최초 의미를 재사용한다.
- 동일 operationId + 다른 payload는 conflict로 거부한다.
- 결과 불명 거래는 일반 FAILED로 축약하지 않고 `UNKNOWN_RESULT`와 reconciliation 경로를 남긴다.

## 6. Local/Remote Service Call
동일 JVM과 분리 WAS는 동일 Public Contract/Header/Error semantics를 사용한다. Remote retry는 오류 분류와 멱등성을 확인한 뒤 수행한다. 비멱등 POST command를 transport 예외라는 이유만으로 무조건 재실행하지 않는다.

검증 최소 세트:
- 정상, validation, 4xx, 5xx
- timeout, target-down, circuit-open
- commit-then-timeout
- one-instance-down failover
- reconciliation store 장애

## 7. 오류 처리
업무 NotFound, Validation, Conflict, Unauthorized/Forbidden, Infrastructure failure를 구분한다. DB 장애나 Owner API 계약 오류를 빈 list/NotFound로 바꾸지 않는다. 사용자 메시지와 내부 상세는 분리하고 SQL/host/secret이 외부 응답에 노출되지 않게 한다.

## 8. Secret
Secret은 값이 아니라 Reference를 계약으로 전달한다. `CpfSecretValue`는 가능한 짧은 scope에서 사용하고 `close()`로 char buffer를 지운다. `toString()`이나 로그에 원문을 넣지 않는다. ENV Provider는 bootstrap 용도이며 상용 환경에서는 Vault/KMS/HSM adapter를 SPI로 연결한다.

## 9. Tenant
Tenant 기능은 기본 OFF다. 활성화 시 Resolver가 tenantId를 결정하지 못하면 fail-closed한다. `CpfTenantContext`는 요청 종료 시 반드시 clear되어야 한다. ThreadLocal 존재만으로 DB 격리가 완료된 것이 아니며 Repository predicate/schema/connection isolation은 별도 검증한다.

## 10. Retention
데이터 Owner가 정책을 정의하고 대량 실행은 `CpfRetentionOperations`/Handler를 통해 수행한다. `LEGAL_HOLD`는 삭제보다 우선한다. destructive ARCHIVE/PURGE는 cutoff와 운영 kill-switch가 모두 있어야 한다. Preview/Dry-run과 실제 실행 결과를 구분한다.

## 11. Audit
관리자 변경은 verified actor, reason, before/after, target identity, operation id를 남긴다. Audit payload는 canonical JSON과 recursive masking을 사용한다. Hash chain이 적용된 영역은 DB 행 변조뿐 아니라 tail 삭제까지 검증한다.

## 12. Health
Liveness는 프로세스 생존, Readiness는 해당 인스턴스가 신규 요청을 받을 준비 여부다. Readiness에서 모든 원격 인스턴스를 fan-out하지 않는다. 원격 Owner 상태는 diagnostics로 제공하고, 전체 cluster/service 상태는 Service Registry에서 종합한다.

## 13. DB 변경
DDL/DML/Index/FK/Migration/Seed 변경은 canonical vendor source와 lifecycle bundle을 함께 변경한다.
- MariaDB: implemented.
- MySQL/PostgreSQL/Oracle/SQL Server: 구현되지 않은 상태면 fail-closed.
- 다른 Vendor SQL을 문자열 치환해 지원 완료 처리하지 않는다.
- Migration과 rollback은 데이터 손실 가능성이 있으면 precondition으로 중단한다.

## 14. Generator 영향
공통 계약, Header, DB 규칙, Public API가 바뀌면 Generator template을 같은 작업에서 검토한다. Generated Domain은 고객 수정 영역을 덮어쓰지 않아야 하며 module/package/systemCode/schema/route 충돌을 생성 전에 차단한다.

## 15. 주석/JavaDoc
Public API/SPI, Controller, Service, 복구/동시성 로직은 한글 JavaDoc 또는 의도 주석을 남긴다. “무엇을 한다”보다 “왜 이 경계가 필요한가, 어떤 실패를 막는가”를 설명한다.

## 16. 개발 중 저비용 Gate
매 변경마다 가능한 범위에서 실행한다.
- Java compile/static syntax
- Frontend typecheck/SFC parse
- JSON/schema parse
- internal import search
- secret scan
- SQL canonical/bundle parity
- migration/source checksum parity
- repository hygiene

## 17. 완료 판정
Source가 존재하거나 Unit Test 하나가 통과했다고 완료가 아니다. Requirement → Source/API/SQL/Test/Runtime/Evidence와 Implementation → Requirement/Owner/Consumer/Operations 양방향으로 확인한다. 직접 실행하지 않은 Runtime은 `미검증`이다.
