# CPF Operator Guide

## 1. Purpose

이 문서는 CPF 운영자가 서비스, 거래, Batch, Worker, 외부연계, 로그와 보안 이벤트를 조회·제어하는 방법을 설명합니다.

## 2. Operational Roles

- Viewer: 조회
- Operator: 제한된 운영 조치
- Approver: 중요 조치 승인
- Security Operator: 보안·인증서·권한 운영
- Auditor: 변경 불가 감사 조회
- Administrator: 설정과 계정 관리

권한은 최소 권한으로 분리하며 중요 조치는 요청자와 승인자를 분리합니다.

## 3. Daily Checks

```text
[ ] Gateway와 주요 서비스 Health
[ ] Instance Registry와 비정상 Instance
[ ] Worker heartbeat와 lease
[ ] 실패·결과 불명 거래
[ ] Batch 지연과 실패
[ ] Outbox backlog와 DLQ
[ ] 외부기관 오류율과 timeout
[ ] DB log 적재와 file log rotation
[ ] 인증서·Secret 만료
[ ] 디스크·DB·Queue 사용량
[ ] 보안 이벤트와 비정상 로그인
```

## 4. Service and Instance Operations

ADM에서 다음을 확인합니다.

- SystemCode
- Service와 Endpoint
- Instance ID
- Host, IP, container
- Version
- Profile
- 시작 시각
- Health
- 마지막 heartbeat
- 차단 상태
- 진행 중 거래

### Instance Drain

1. 신규 Routing 제외
2. 진행 중 거래 확인
3. Worker claim 중지
4. 완료 대기
5. Instance 종료
6. Registry 정리 확인

강제 종료는 승인과 사유를 요구합니다.

## 5. Transaction Operations

검색 조건:

- transactionGlobalId
- traceId
- segmentId
- systemCode
- businessId
- channel
- caller
- user
- 상태
- 시작·종료 시간
- elapsed time
- 실패 구간
- 외부기관
- instance

상세 화면:

- Header
- 요청·응답 Masking
- 호출 Timeline
- Retry attempt
- 오류
- 파일 로그
- DB 로그
- 외부연계 결과
- 재처리·보상 이력
- 운영 조치 Audit

## 6. Trace Boost

특정 거래, 업무, Batch 또는 Instance에 대해 제한 시간 동안 DEBUG 수준 추적을 활성화합니다.

필수 제한:

- 대상 범위
- 시작·종료 시각
- 최대 기간
- 최대 로그 크기
- 개인정보 Masking
- 승인
- 자동 종료
- Audit

전역 장시간 DEBUG를 사용하지 않습니다.

## 7. Batch and Worker Operations

### Monitor

- Job instance
- Run ID
- Step
- Parameter
- 상태
- 처리량
- 성공·실패·Skip
- ETA
- Worker
- Lease
- Checkpoint
- 마지막 오류

### Controls

- Pause
- Resume
- Stop
- Drain
- Retry failed item
- Restart from checkpoint
- Rerun
- Cancel
- Reconcile
- Takeover

실행 중인 Job의 Parameter를 직접 변경하지 않습니다.

## 8. External Integration

- 기관별 성공률
- Endpoint 상태
- Timeout
- Retry
- Circuit state
- 인증서 만료
- Unknown result
- Reconciliation backlog
- File 송수신 상태

결과 불명은 단순 실패와 분리하여 관리합니다.

## 9. Messaging

- Outbox backlog
- Publish latency
- Inbox duplicate
- Consumer lag
- Retry count
- DLQ
- Poison message
- Replay history

Replay는 대상, 기간, 최대 건수와 중복 방지 정책을 지정하고 승인 후 실행합니다.

## 10. Logging

### File Log

- 거래·일자 기준 경로
- Size rotation
- 압축
- 보존기간
- Masking
- disk quota

### DB Log

- 거래 검색
- 장애 시 local spool
- 복구 적재
- poison record 격리
- 보존과 파기

File log와 DB log의 transactionId, 상태와 시간이 일치하는지 점검합니다.

## 11. Alerts

권장 Alert:

- Gateway error rate
- Service unavailable
- p95/p99 latency
- Timeout spike
- Circuit open
- Unknown result 증가
- Worker heartbeat loss
- Lease takeover
- Batch delay
- DLQ 증가
- DB log failure
- Disk usage
- 인증서 만료
- 비정상 관리자 활동

Alert에는 Runbook 링크를 포함합니다.

## 12. Change and Audit

모든 운영 조치는 다음을 기록합니다.

- 요청자
- 승인자
- 대상
- 이전 값
- 변경 값
- 사유
- 시작·종료
- 결과
- transactionId
- 관련 Incident

## 13. End-of-Day Review

- 미완료 결과 불명 거래
- 장시간 실행 Batch
- Retry 임계 초과
- 비정상 Worker
- 미처리 DLQ
- 승인 대기 운영 조치
- 인증서 만료 예정
- Evidence 누락
