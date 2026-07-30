# CPF 비동기·메시징·보상 처리 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 업무 개발자, 연계 개발자, 메시징 운영자
> **목적**: 비동기 사건을 중복·순서·부분 실패에 안전하게 처리하고 재생·대사·보상한다.
> **관련 문서**: [개발자 가이드](CPF_DEVELOPER_GUIDE.md) · [관측·장애대응·복구](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | 공통 계약은 `cpf-core`; 업무 처리와 원장은 각 업무영역 |
| 이 문서로 완료하는 일 | 업무 원장과 Outbox를 원자적으로 저장하고, Inbox·멱등성·Retry·DLQ·Replay·Reconcile·Compensation으로 부분 실패를 복구한다. |
| 적용 범위 | Event Envelope, Outbox/Inbox, Broker Adapter, 독성 메시지, 결과 불명, 보상 |
| 주요 독자 | 업무 개발자, Messaging 운영자, 장애 대응자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

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

명령은 성공·실패·결과 불명 계약을 갖는다.

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

## 28. Outbox 상태 모델

| 상태 | 의미 | 허용 동작 |
|---|---|---|
| `PENDING` | 업무 Transaction과 함께 저장됨 | Publisher Claim |
| `CLAIMED` | 특정 Publisher가 Lease 보유 | 전송·Lease 갱신 |
| `PUBLISHED` | Broker ACK 확인 | 보존·정리 |
| `RETRY_WAIT` | 재시도 가능 실패 | Backoff 뒤 재Claim |
| `DEAD` | 자동 재시도 금지 | 운영 분석·수정·Replay 승인 |
| `UNKNOWN` | Broker 처리 여부 미확정 | Broker/Consumer 대사 |

Publisher가 Broker에 전송한 뒤 ACK를 잃으면 같은 Event ID로 재전송할 수 있어야 하며 Consumer Inbox가 중복 Side Effect를 차단해야 한다.

## 29. Inbox 처리 Transaction

```text
Message 수신
→ Envelope·Schema·권한 검증
→ Inbox 중복 조회
→ 미처리면 Inbox Processing 기록
→ 업무 상태 전이
→ Inbox Processed와 결과 저장
→ Commit
→ Broker ACK
```

Broker ACK와 DB Commit 사이의 실패를 고려한다. Commit 뒤 ACK 유실은 재수신될 수 있으므로 Inbox 결과를 재사용한다. 처리 중 Lease가 만료되면 Fencing 또는 Version으로 늦은 완료를 차단한다.

## 30. Replay 실행서

1. DLQ/Dead Event의 원인, Schema Version, Producer와 Consumer Version을 확인한다.
2. Payload를 수정하지 않고 원본 Hash와 정제된 Preview를 보존한다.
3. 동일 Event ID를 재사용할지 새 Replay ID를 만들지 정책을 확인한다.
4. 대상 Consumer와 업무 Side Effect가 멱등한지 확인한다.
5. Replay 범위, 속도, 동시성, 중단 기준과 승인자를 정한다.
6. 소량 Canary Replay 후 업무 원장과 Inbox 결과를 대사한다.
7. 전체 재생을 진행하고 성공·중복·실패·결과 불명을 분리한다.
8. 원본 DLQ와 Replay 결과의 연결을 Audit와 Evidence에 남긴다.

## 31. 보상 설계

보상은 DB Rollback이 아니다. 이미 외부로 확정된 업무 효과를 반대 의미의 새 업무 Transaction으로 조정한다.

- 원 거래와 보상 거래를 별도 ID로 연결한다.
- 부분 보상, 보상 실패와 재보상을 상태로 표현한다.
- 환불·취소·원복 등 업무 규칙과 권한을 Owner Domain이 강제한다.
- 보상 순서와 외부 기관 응답이 불명일 때 Reconcile을 우선한다.
- 보상 결과가 원 거래 History를 삭제하지 않는다.

## 32. 독성 메시지 판정

재시도 횟수만으로 독성을 판정하지 않는다. Schema 불일치, 필수 Reference 부재, 영구 권한 오류, 업무 상태 충돌과 반복되는 동일 Failure Code를 분류한다. 무한 재시도를 방지하고 운영자가 원인·Payload Metadata·Consumer Version·마지막 오류를 확인할 수 있게 한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| 공통 계약 | `git ls-files cpf-core | Select-String "messag|outbox|inbox|event"` | Envelope·Port·상태 계약 탐색 |
| 업무 Consumer | 각 업무영역 `adapter/messaging`, `application` | 업무 처리와 원장 Owner |
| DB Artifact | `cpf-tools/db/canonical`, `cpf-tools/db/vendor/*`에서 outbox/inbox 검색 | Table·Index·Migration 확인 |
| Test | `git ls-files "*test*" | Select-String "Outbox|Inbox|Replay|Compensation"` | 중복·재시도·보상 시나리오 |

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
