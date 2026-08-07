# CPF 거래·연동·Batch Logging / Transaction Timeline QA 표준

- 기준 SHA: `0427758db041d38eb0f34d88b55bd5366e2d9e47`
- 적용 대상: Core/Common/Admin/Biz-Admin/Batch/Gateway/Generated Domain/Reference/Starter
- 중요도: **Release 핵심 검수축**

## 1. 목적

운영자가 장애 발생 시 transactionId 하나로 최초 요청부터 모든 하위 호출, 외부 연계, Message, File, Batch 실행과 복구 결과까지 재구성할 수 있어야 한다.

## 2. 필수 식별자

기본 공통:
- transactionId
- traceId / spanId
- segmentId / parentSegmentId / attempt
- systemCode / environment / instanceId / wasId
- requestId / idempotencyKey
- actor / tenant / channel

Batch:
- jobName / jobId
- jobInstanceId / jobExecutionId
- stepExecutionId
- partitionId / itemId
- agentId / workerId

외부 연계:
- remoteSystem
- operation/interfaceId
- remoteRequestId
- attempt
- timeout budget
- result/error/failureStage/unknownResult

## 3. 전파 규칙

- transactionId는 nested transaction에서도 유지한다.
- Retry는 새 transactionId를 만들지 않고 attempt를 증가시킨다.
- Async/Message는 envelope/header로 transaction/trace/segment lineage를 전파한다.
- Batch로 넘길 때 originating transactionId/requestId를 Job Parameter 또는 표준 Metadata에 보존한다.
- 외부 응답 loss/UNKNOWN/reconcile에서도 원 transactionId를 유지한다.
- 신뢰되지 않은 외부 Client의 내부 transaction/instance/security header는 그대로 채택하지 않는다.

## 4. 표준 로그 Record

필수/조건부 필드:
`timestamp`, `level`, `eventType`, `systemCode`, `environment`, `instanceId`, `wasId`,
`transactionId`, `traceId`, `spanId`, `segmentId`, `parentSegmentId`, `attempt`,
`requestId`, `idempotencyKey`, `actor`, `tenant`, `channel`,
`operation`, `endpoint`, `remoteSystem`,
`jobInstanceId`, `jobExecutionId`, `stepExecutionId`, `partitionId`, `itemId`, `agentId`, `workerId`,
`status`, `result`, `errorCode`, `failureStage`, `retryable`, `unknownResult`, `elapsedMs`.

## 5. File Log QA

확인사항:
- 표준 path/file naming
- UTF-8 및 표준 structured format
- system/date/instance 식별 가능
- size/time rotation
- compression
- retention/cleanup
- log directory 권한
- symlink/path traversal 방어
- async bounded queue
- caller-runs/backpressure/fallback
- disk full/write failure
- process kill
- graceful shutdown drain
- local spool/replay
- replay duplicate 제거
- checksum/sequence gap
- terminal loss counter와 alert
- 민감정보 masking/redaction

File Writer API/Status가 존재하는 것만으로 PASS하지 않는다. 실제 writer consumer와 실패 상황에서 파일/metric/alert 결과를 확인한다.

## 6. DB Log / Transaction Timeline QA

- canonical DDL이 Oracle/PostgreSQL/MariaDB에 존재
- transactionId/segment/time/index
- 대량 transactionId lookup
- child segment hierarchy
- append idempotency/duplicate handling
- partial transaction log persistence
- DB outage 후 retry/recovery
- retention/partition/archive/purge
- audit append-only/tamper evidence
- raw payload 최소화
- masking
- 조회 권한/감사

## 7. ADM QA

ADM에서 transactionId 하나로:
1. Transaction Group/Transaction Header
2. Segment Tree
3. Remote Call
4. Message Attempt/DLQ
5. File Log/Remote Log
6. Batch/Center-Cut/Scheduler 실행
7. Instance/Agent/Worker
8. Error/UNKNOWN/Reconcile
9. Audit/Evidence
를 연결해서 볼 수 있어야 한다.

필수 UX:
- exact transactionId 검색
- 기간/시스템/상태 보조 필터
- paging
- detail
- timeline
- tree
- error highlight
- retry/attempt 표시
- partial/stale warning
- 안전한 log download
- raw 민감정보 별도 권한/사유/승인

## 8. Batch QA

- Job 시작/종료/재시작
- Step 시작/종료
- Chunk/commit/rollback
- Retry/Skip
- Partition/Worker
- Agent/Runner
- Center-Cut target
- Scheduler trigger/misfire
- originating transactionId
- executionId
- restart lineage
를 연결한다.

## 9. 외부 연계 QA

REST/SOAP/Fixed Length/File/Webhook/Gateway/Message에 대해:
- destination/interface
- send/receive timestamps
- attempt
- timeout
- circuit breaker/rate limit
- HTTP/transport/business error
- request/response size
- payload digest 또는 masked summary
- UNKNOWN/reconcile
를 검증한다.

## 10. Failure Acceptance

다음이면 FAIL:
- transactionId가 중간 호출에서 끊김
- Retry마다 새 transactionId가 생성되어 원 거래 계보가 사라짐
- Batch와 원 거래 연결 불가
- ADM이 transactionId로 전체 흐름을 재구성하지 못함
- File/DB/Trace의 동일 transaction 연결이 안 됨
- 로그 저장 실패 시 조용히 유실되고 metric/alert가 없음
- async queue가 무제한
- 디스크 Full 시 원 거래 무한 block 또는 무조건 rollback
- 로그에 Secret/Token/PII 원문 존재
- 대량 DB에서 transactionId 조회 Index 없음
- 로그 파일명 충돌로 다중 인스턴스 기록 혼합
- Process kill 후 buffered log 유실 여부를 탐지하지 못함
