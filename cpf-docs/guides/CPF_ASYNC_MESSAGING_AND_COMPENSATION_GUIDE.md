# CPF 비동기·메시징·보상 처리 가이드

## 1. 목적

비동기 처리는 단순 Thread 분리나 Broker 전송이 아니다. 업무 Transaction, 중복 전달, 순서, 재시도, 독성 메시지, 결과 불명과 보상을 제품 수준으로 관리한다.

## 2. 기본 흐름

```text
업무 Transaction
→ Outbox 저장
→ Publisher
→ Broker
→ Consumer
→ Inbox 중복 확인
→ 업무 처리
→ 처리 이력
```

## 3. Event Envelope

필수 필드:

- eventId
- eventType
- schemaVersion
- occurredAt
- producer
- transactionId
- traceId
- correlationId
- causationId
- idempotencyKey
- tenantId
- payload
- metadata

Secret과 민감정보는 Envelope에 원문으로 넣지 않는다.

## 4. Outbox

업무 원장과 Outbox를 같은 Transaction에 저장한다.

상태:

- READY
- CLAIMED
- PUBLISHED
- FAILED
- POISON
- CANCELLED

Claim은 Lease와 Fencing을 사용한다.

## 5. Publisher

- Page 조회
- Claim
- Schema Validation
- Broker 전송
- ACK
- Retry
- Backoff
- Poison 분리
- Metrics

Publisher 중단 후 안전하게 재개한다.

## 6. Broker

지원 Adapter는 Kafka, AMQP 등이다. 업무 코드는 Broker Client Library에 직접 종속되지 않고 CPF Public Contract를 사용한다.

## 7. Inbox

Consumer는 `(consumerId, eventId)` 또는 업무 멱등성 키로 중복을 막는다.

```text
Inbox 없음
→ 처리 시작
→ 업무 변경 + Inbox 완료
→ Commit
```

처리 중 Crash 후 재전달을 고려한다.

## 8. 순서

순서가 필요한 경우 Partition Key를 정의한다.

- 고객
- 계좌
- 주문
- 업무 Key

Global 순서를 요구하지 않는다. 순서 지연과 재처리 정책을 문서화한다.

## 9. 재시도

분류:

- 일시 오류
- 영구 업무 오류
- 계약 오류
- 인증/권한
- 독성 Payload
- 결과 불명

Retry는 최대 횟수, Backoff, Jitter와 Deadline을 갖는다.

## 10. DLQ

DLQ Record:

- originalEvent
- consumer
- failureCode
- sanitizedMessage
- attempts
- firstFailedAt
- lastFailedAt
- transactionId
- replayPolicy

## 11. Replay

Replay 절차:

1. 원인 수정 확인
2. 대상 Preview
3. 중복 영향 확인
4. Permission
5. Reason
6. 승인
7. 새 operationId
8. Replay
9. 결과 대사
10. Audit

원본 Event ID를 유지할지 새 Replay ID를 사용할지 계약으로 정한다.

## 12. 독성 메시지

반복 실패하는 Payload를 일반 Retry Queue에 무한 재투입하지 않는다.

- Schema 오류
- 최대 크기
- 지원하지 않는 Version
- 필수값 누락
- 악성 Content
- 업무 불변식 위반

POISON 상태로 격리한다.

## 13. Schema Version

호환 정책:

- 새 Optional 필드
- Enum 확장
- 필수 필드 변경
- Type 변경
- 제거
- Upcaster/Adapter

Producer와 Consumer Version Matrix를 관리한다.

## 14. 결과 불명

Broker ACK 유실:

```text
Publish 요청
→ Broker 저장 여부 미확정
→ Outbox UNKNOWN
→ Broker Key/상태 조회
→ Published 또는 Retry
```

Consumer 외부 호출도 같은 원칙을 적용한다.

## 15. Saga

여러 업무 Owner를 하나의 DB Transaction으로 묶지 않는다.

```text
Step A 완료
→ Event
→ Step B 완료
→ Event
→ Step C 실패
→ Compensation B
→ Compensation A
```

## 16. 보상

보상은 단순 반대 SQL이 아니다.

필수:

- compensationId
- originalOperation
- 대상 상태
- 가능 조건
- 멱등성
- 순서
- 재시도
- 결과 불명
- 수동 전환
- 감사

이미 외부로 전달된 효과를 고려한다.

## 17. 수동 보상

자동 보상이 위험하거나 불가능하면 운영 Case로 전환한다.

- 영향
- 필요한 증빙
- 담당자
- 기한
- 승인
- 처리 결과
- 고객 통지 여부
- Audit

## 18. 비동기 Command

Command와 Event를 구분한다.

- Command: 특정 Consumer에게 행동 요청
- Event: 이미 발생한 사실

Command는 성공/실패/Unknown 결과 계약을 갖는다.

## 19. Backpressure

- Queue Lag
- Consumer Capacity
- Pause/Resume
- Rate Limit
- Batch Size
- 처리 Timeout
- Circuit
- Alert

원 업무 Transaction이 무제한 Queue를 생성하지 않도록 상한을 둔다.

## 20. 관측

Metrics:

- publish rate
- publish failure
- lag
- retry
- DLQ
- poison
- consumer duration
- duplicate
- replay
- compensation
- unknown

Timeline은 transactionId와 correlationId로 연결한다.

## 21. 보안

- Topic/Queue ACL
- TLS
- Credential Reference
- Payload 암호화 정책
- PII 분류
- Masking
- Replay 권한
- DLQ Download 통제
- 최대 Payload

## 22. Test

- Commit과 Outbox 원자성
- Publisher Crash
- ACK 유실
- 중복 전달
- Consumer Crash
- Broker Down
- 순서
- Schema Version
- Poison
- DLQ Replay
- Saga 보상
- 보상 실패
- Multi-consumer Fencing

## 23. 체크리스트

- [ ] 업무 변경과 Outbox가 원자적이다.
- [ ] Consumer는 중복 전달에 안전하다.
- [ ] Retry와 Poison을 구분한다.
- [ ] DLQ Replay에 권한·사유·감사가 있다.
- [ ] Schema Version과 호환 정책이 있다.
- [ ] 결과 불명을 자동 성공/실패로 바꾸지 않는다.
- [ ] 보상 Handler가 실제 Consumer에 연결된다.
- [ ] Lag와 실패를 운영자가 볼 수 있다.
