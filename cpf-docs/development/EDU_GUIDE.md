# CPF EDU Guide

## 1. 목적

EDU는 제품 기능을 대신하는 Sample이 아니라 개발자가 CPF 공식 API, 구조, 실패 처리와 운영 방식을 실행하며 학습하는 교육 체계다.

## 2. 원칙

- Source/API/SQL이 실제 제품 Contract를 사용
- 정상뿐 아니라 오류·경계·복구 Scenario 포함
- 운영 Product Seed와 EDU/Test Data 분리
- Sample을 Production 구현으로 간주하지 않음
- 실행 명령, 기대 결과와 Cleanup 제공
- 최신 Commit과 환경을 기록

## 3. 필수 학습 Track

1. Header, transactionId와 Context
2. Standard Error와 Validation
3. Local/Remote Facade
4. CRUD, Offset, Slice와 Cursor
5. Idempotency와 Duplicate Request
6. File/Attachment
7. Fixed-Length Core Engine과 Generated Domain Adapter
8. Event, Outbox/Inbox와 DLQ
9. Batch와 Center-Cut
10. Agent/Worker Failure와 Recovery
11. ADM/BZA Permission과 Audit
12. Generator Create/Verify/Remove

## 4. Reference 역할

- `cpf-reference`: 교육·참조 Scenario의 유일한 범용 EDU Source Owner
- `cpf-common`: Sample Table 1개를 통한 DB/Paging/Transaction
- `cpf-batch`: 독립 Runtime/Contract Owner이며 EDU Source는 두지 않음
- Generated Domain: 사용자가 입력한 임의 Domain/SystemCode의 Generator lifecycle 검증 대상

Generated Domain은 REF의 Compile/Runtime 선행 조건이 아니다. REF의 service-call,
transaction, fixed-length, messaging 예제는 특정 MBR/ACC/EXS DTO·서비스 ID·URL을
고정하지 않고 공개 CPF 계약 또는 REF 자체 중립 시뮬레이터를 사용한다.

### Batch와 Center-Cut EDU Ownership

- 범용 EDU와 Batch 업무 Job/Step 교육의 Source Owner는 `cpf-reference`다.
- `cpf-batch`에는 Control Server, Scheduler, Worker, Center-Cut Runner, Host Agent와
  Contract/Runtime Common/Testkit만 둔다.
- REF의 실제 실행·재시도·중지 요청은 `CpfBatchOperationsPort`로 BAT Owner에 위임한다.
- REF Center-Cut Adapter는 CPF의 공개 `api.centercut`과 `spi.centercut` 경계만 사용한다.
- REF가 BAT Runtime/Repository 구현을 import하거나 Scheduler/Worker/lease를 복제하지 않는다.
- Legacy 이관 근거는 [CPF Legacy Batch Migration Map](CPF_LEGACY_BATCH_MIGRATION_MAP.md)에서
  Source, Owner, Consumer, Test와 Runtime까지 추적한다.

## 5. Lab 구성

각 Lab은 다음을 가진다.

```text
requirementId
objective
prerequisite
source location
command
input
expected result
failure scenario
cleanup
evidence
```

## 6. Coverage Matrix

EDU Coverage Matrix는 Script로 재생성하는 파생 산출물이다. Source와 Test가 변경되면 재생성하고 기준 Commit을 포함한다. Matrix 파일 존재만으로 Coverage 완료 처리하지 않는다.

## 7. 완료 조건

- clean environment에서 재현
- DB/Runtime이 필요한 Lab은 실제 실행
- Negative/Recovery 포함
- Secret과 개인정보 없음
- Guide, Source, Test와 결과 일치

## 8. Core Transaction Reference Track

EDU는 문서 예제가 아니라 다음 실행형 Reference를 제공한다.

1. LOCAL commit / rollback
2. XA/JTA DB+DB commit / rollback
3. XA/JTA DB+JMS commit / rollback
4. XA prepare 이후 process kill → restart recovery
5. Business DB + Outbox 동일 Transaction
6. Broker ACK loss / duplicate publish / Publisher kill
7. Inbox/Dedup duplicate suppression
8. Saga A→B→C 성공, C 실패 후 B/A compensation
9. Saga compensation failure/retry/UNKNOWN/Reconcile
10. TCC Try→Confirm
11. TCC Try→Cancel
12. TCC duplicate Confirm/Cancel, empty rollback, hanging
13. External REST timeout→UNKNOWN→Reconcile
14. Domain A→B→C local/remote parity
15. Batch→Domain→Messaging/DB transaction boundary
16. 모든 흐름의 동일 transactionId와 ADM Timeline

각 Lab은 Source, Consumer, Test/Harness, Config/SQL, 실행 명령, 기대 상태, failure injection, cleanup, exact-SHA Evidence를 포함한다. Mock-only로 실제 DB/Broker/XA 의미론을 PASS 처리하지 않는다.

