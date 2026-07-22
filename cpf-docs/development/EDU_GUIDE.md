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

1. Header, transactionGlobalId와 Context
2. Standard Error와 Validation
3. Local/Remote Facade
4. CRUD, Offset, Slice와 Cursor
5. Idempotency와 Duplicate Request
6. File/Attachment
7. Fixed-Length Core Engine과 EXS Adapter
8. Event, Outbox/Inbox와 DLQ
9. Batch와 Center-Cut
10. Agent/Worker Failure와 Recovery
11. ADM/BZA Permission과 Audit
12. Generator Create/Verify/Remove

## 4. Reference 역할

- `cpf-member`: 최소 업무 API와 Local/Remote 호출
- `cpf-account`: Generator Lifecycle와 거래 경계
- `cpf-reference`: 교육·참조 Scenario
- `cpf-external`: 기관별 Adapter와 결과 불명
- `cpf-common`: Sample Table 1개를 통한 DB/Paging/Transaction

ACC/MBR는 검증에 필요한 최소 구조를 유지하며 전체 금융 원장을 추정 구현하지 않는다.

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
