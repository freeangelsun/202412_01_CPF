# CPF Approval Architecture Guide

## 1. 목적

CPF의 승인 기능은 단순 `status` 변경이나 화면 결재선 기능이 아니다. 위험한 플랫폼 운영과 고객 업무 결재를 각각 독립된 제품 기능으로 제공하며 동시성, 멱등성, 감사, 조직변경, 위임과 분산 Runtime을 고려한다.

## 2. ADM Approval

ADM은 플랫폼 Control Plane이다. 요청자는 위험 Command를 직접 실행하지 않고 Approval Request를 생성한다. 승인 완료 후 Owner Command API가 실제 명령을 수행한다.

`Request → Policy Version → Step → Participant → Decision → Owner Command → Execution Result → Immutable History`

필수 통제: requester/approver separation, ALL/ANY/N_OF_M, TTL, payload hash, reason, optimistic lock, idempotency, break-glass post review, unknown execution result recovery.

ADM은 BAT/REF/EXS 등의 DB를 직접 수정하지 않는다.

## 3. BZA Approval

BZA는 고객 업무 결재를 소유한다. 정책 정의 시 조직/Role/Position 같은 동적 Target을 사용하고 상신 시 실제 참여자로 Resolution하여 Snapshot을 만든다.

예시:

```text
Step 10  요청자 조직 팀장 승인          ORG_MANAGER  ALL
Step 20  정보보호팀 필수 합의            ORGANIZATION ANY
Step 20  준법팀 필수 합의                ORGANIZATION ALL
Step 20  인프라팀 선택 합의              ORGANIZATION N_OF_M(2)
Step 30  최종 업무승인 Role              ROLE        ANY
```

동일 step 번호의 Target들은 병렬 처리할 수 있으며 각 Target의 결정 규칙을 독립 적용한다.

## 4. Directory와 Snapshot

BZA는 `Employee → Assignment → Organization/Position/JobTitle`을 유효기간으로 해석한다. 결재 생성 후 인사발령이 발생해도 기존 Instance의 참여자/조직/직급 Snapshot은 바뀌지 않는다.

ADM은 HR 원장을 소유하지 않고 Operator Profile/Directory Port로 필요한 조직 문맥만 얻는다.

## 5. 상태와 동시성

상태 전이는 명시적 State Machine으로 구현한다. 동일 Decision의 재전송은 idempotency key로 같은 결과를 반환하며, 서로 다른 두 승인자가 동시에 결정해도 step completion은 낙관적 잠금/원자적 갱신으로 한 번만 진행된다.

## 6. API/OpenAPI/JavaDoc

Public API는 요청 생성, 정책 조회/Simulation, Inbox 조회, Approve/Agree/Reject, Delegate, Cancel/Withdraw, History와 Runtime 상태를 제공한다. Controller만 문서화하지 말고 DTO 필드, Enum 의미, 오류코드, 권한, idempotency, transactionGlobalId를 OpenAPI/JavaDoc에 명시한다.

## 7. 필수 Test

정상 순차결재, 병렬결재, ALL/ANY/N_OF_M, 부서합의, 필수/선택 부서, 위임/부재, 자기승인 차단, 중복요청, 동시결정, 정책 Version 변경, 조직개편 후 Snapshot, 만료/취소/반려/재상신, Owner Command 실패/Unknown, Break-glass를 포함한다.
