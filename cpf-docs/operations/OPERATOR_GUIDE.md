# CPF Operator Guide

## 1. 목적

운영자가 CPF Runtime을 안전하게 관찰·제어하고 승인·감사를 남기는 일상 운영 절차를 정의한다. 장애 복구 상세 절차는 Recovery Guide가 담당한다.

## 2. 일일 점검

- Service/Instance liveness와 readiness
- DB connection, lock, replication과 storage
- Gateway traffic, error와 circuit
- Online latency, timeout, unknown result
- Batch/Scheduler backlog와 misfire
- Agent/Runner/Worker lease와 heartbeat
- Center-Cut queue, TPS와 failure
- Broker lag, DLQ와 poison message
- File/SFTP queue와 disk/spool
- certificate/secret expiry
- audit/approval pending

## 3. 거래 조회

목록과 상세는 다음을 연결한다.

- start/end/status/duration
- transactionGlobalId와 segment
- caller/target/channel
- standard/extension header 주요 검색조건
- failure stage와 standard error
- external correlation
- file/message/batch item
- instance/host/container
- timeline, log와 audit

## 4. 제어 원칙

- Owner Command API 사용
- DB 직접 상태 변경 금지
- expected version과 idempotency
- reason, permission, approval와 audit
- preview와 impact count
- timeout과 result status

## 5. Online Operations

- traffic block/drain
- instance isolate
- trace boost/dynamic log level
- retry/reconciliation request
- maintenance mode
- config override와 expiry

## 6. Batch/Center-Cut

- job/step/item/attempt 조회
- pause/resume/cancel
- failed-only eligibility preview
- speed/concurrency override
- agent drain와 reassignment
- unknown/reconciliation/compensation

## 7. External/File/Event

- endpoint/certificate/circuit
- unknown result
- reconciliation backlog
- SFTP/file checksum/quarantine
- outbox/inbox/DLQ/replay

Replay는 original correlation, idempotency와 permission을 보존한다.

## 8. Security와 Download

- role/session/account lifecycle
- permission change approval
- download reason, scope, expiry와 watermark
- masking level
- security event와 break-glass review

## 9. Capacity와 Retention

- TPS, latency와 error budget
- DB/table/index growth
- log/spool/disk
- queue/worker capacity
- retention, purge와 archive

## 10. 운영 Evidence

모든 위험 조치에 operator, time, reason, approval, target, before/after, result와 incident link를 남긴다.
