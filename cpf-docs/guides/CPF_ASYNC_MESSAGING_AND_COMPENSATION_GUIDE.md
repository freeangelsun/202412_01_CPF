# CPF 비동기·메시징·보상 처리 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무 개발자, 연계 개발자, 메시징 운영자
> **목적**: 비동기 사건을 중복·순서·부분 실패에 안전하게 처리하고 재생·대사·보상한다.
> **관련 문서**: [개발자 가이드](CPF_DEVELOPER_GUIDE.md) · [관측·장애대응·복구](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)

---


<picture>
  <source media="(max-width: 720px)" srcset="../assets/readme/cpf-execution-mobile.png">
  <img src="../assets/readme/cpf-execution-desktop.png" alt="CPF 실행과 복구의 공통 흐름" width="100%">
</picture>

## 1. 목적

비동기 처리는 단순 스레드 분리나 메시지 중개 시스템 전송이 아니다. 업무 트랜잭션, 중복 전달, 순서, 재시도, 독성 메시지, 결과 불명과 보상을 제품 수준으로 관리한다.

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

## 3. 사건 봉투

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

비밀값과 민감정보는 봉투에 원문으로 넣지 않는다.

## 4. 송신함

업무 원장과 송신함을 같은 트랜잭션에 저장한다.

상태:

- READY
- CLAIMED
- PUBLISHED
- FAILED
- POISON
- CANCELLED

점유는 임대와 Fencing을 사용한다.

## 5. Publisher

- 페이지 조회
- 점유
- 스키마 검증
- 메시지 중개 시스템 전송
- ACK
- 재시도
- Backoff
- Poison 분리
- 지표

Publisher 중단 후 안전하게 재개한다.

## 6. 메시지 중개 시스템

Kafka, AMQP 등은 어댑터로 연결한다. 업무 코드는 메시지 중개 시스템의 Client 라이브러리에 직접 종속되지 않고 CPF 공개 계약을 사용한다.

## 7. 수신함

소비자는 `(consumerId, eventId)` 또는 업무 멱등성 키로 중복을 막는다.

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
- 독성 본문
- 결과 불명

재시도는 최대 횟수, Backoff, Jitter와 Deadline을 갖는다.

## 10. 처리 실패 메시지 보관소(DLQ)

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

## 11. 재생

재생 절차:

1. 원인 수정 확인
2. 대상 Preview
3. 중복 영향 확인
4. 권한
5. 사유
6. 승인
7. 새 operationId
8. 재생
9. 결과 대사
10. 감사

원본 사건 ID를 유지할지 새 재생 ID를 사용할지 계약으로 정한다.

## 12. 독성 메시지

반복 실패하는 본문을 일반 재시도 큐에 무한 재투입하지 않는다.

- 스키마 오류
- 최대 크기
- 지원하지 않는 버전
- 필수값 누락
- 악성 Content
- 업무 불변식 위반

POISON 상태로 격리한다.

## 13. 스키마 버전

호환 정책:

- 새 Optional 필드
- Enum 확장
- 필수 필드 변경
- Type 변경
- 제거
- Upcaster/어댑터

생산자와 소비자 버전 Matrix를 관리한다.

## 14. 결과 불명

메시지 중개 시스템 ACK 유실:

```text
Publish 요청
→ Broker 저장 여부 미확정
→ Outbox UNKNOWN
→ Broker Key/상태 조회
→ 게시된 또는 Retry
```

소비자 외부 호출도 같은 원칙을 적용한다.

## 15. Saga

여러 업무 소유자를 하나의 DB 트랜잭션으로 묶지 않는다.

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
- 감사

## 18. 비동기 명령

명령과 사건을 구분한다.

- 명령: 특정 소비자에게 행동 요청
- 사건: 이미 발생한 사실

명령은 성공/실패/Unknown 결과 계약을 갖는다.

## 19. Backpressure

- 큐 Lag
- 소비자 Capacity
- Pause/Resume
- 호출량 제한
- 배치 Size
- 처리 시간 제한
- 회로 차단기
- 경보

원 업무 트랜잭션이 무제한 큐를 생성하지 않도록 상한을 둔다.

## 20. 관측

지표:

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

시간선은 transactionId와 correlationId로 연결한다.

## 21. 보안

- Topic/큐 ACL
- TLS
- 인증정보 참조
- 본문 암호화 정책
- PII 분류
- 마스킹
- 재생 권한
- DLQ 내려받기 통제
- 최대 본문

## 22. 테스트

- Commit과 송신함 원자성
- Publisher Crash
- ACK 유실
- 중복 전달
- 소비자 Crash
- 메시지 중개 시스템 Down
- 순서
- 스키마 버전
- Poison
- DLQ 재생
- Saga 보상
- 보상 실패
- Multi-consumer Fencing

## 23. 체크리스트

- [ ] 업무 변경과 송신함이 원자적이다.
- [ ] 소비자는 중복 전달에 안전하다.
- [ ] 재시도와 Poison을 구분한다.
- [ ] DLQ 재생에 권한·사유·감사가 있다.
- [ ] 스키마 버전과 호환 정책이 있다.
- [ ] 결과 불명을 자동 성공/실패로 바꾸지 않는다.
- [ ] 보상 Handler가 실제 소비자에 연결된다.
- [ ] Lag와 실패를 운영자가 볼 수 있다.

## 부록 A. 사건 봉투 예

```json
{
  "eventId": "01J...",
  "eventType": "PaymentApproved",
  "schemaVersion": 2,
  "occurredAt": "2026-07-30T08:00:00Z",
  "producerSystemCode": "PAY",
  "aggregateType": "Payment",
  "aggregateId": "PAY-20260730-0001",
  "aggregateVersion": 7,
  "transactionId": "TX-...",
  "segmentId": "SEG-...",
  "operationId": "OP-...",
  "tenantId": "TENANT-A",
  "payload": {}
}
```

봉투에는 비밀값이나 불필요한 개인정보를 넣지 않는다. 민감 필드는 분류와 보존 정책을 가진 별도 참조로 전달한다.

## 부록 B. 재시도 판정표

| 실패 | 자동 재시도 | 조건 |
|---|---|---|
| 중개 시스템 일시 중단 | 허용 | 최대 횟수·지수 지연·무작위 분산 |
| 소비자 시간 초과 | 조건부 | 멱등 처리와 실제 결과 조회 가능 |
| 계약 검증 실패 | 금지 | 격리 후 생산자·스키마 수정 |
| 권한·비밀값 오류 | 금지 | 설정 수정과 승인 후 재생 |
| 업무 상태 충돌 | 금지 | 최신 상태 확인과 운영 판단 |
| 처리 여부 불명 | 금지 | 수신함·업무 원장·대사로 확정 |

## 부록 C. 재생 승인 항목

- 사건 유형과 생산자
- 시작·종료 시각 또는 사건 식별자 범위
- 대상 소비자와 환경
- 중복 처리 보호 확인
- 하위 외부 호출 영향
- 예상 건수와 처리량 제한
- 사유·작성자·승인자
- 중단 조건과 되돌리기 계획
- 실행 결과와 실패 건 목록

## 부록 D. 보상 설계

보상은 DB 롤백이 아니다. 이미 외부에 확정된 업무 효과를 새로운 반대 거래로 해소한다. 보상 명령도 멱등 키, 권한, 상태 전이, 결과 불명과 감사 이력을 가져야 한다.
