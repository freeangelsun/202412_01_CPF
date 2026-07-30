# CPF 관측·장애대응·복구 가이드

## 1. 목적

CPF의 관측 기능은 Log를 많이 남기는 것이 목적이 아니다. 운영자가 거래, 인스턴스, 외부 호출, Batch 실행과 운영 조치를 연결하여 원인을 찾고 안전하게 복구하도록 한다.

## 2. 관측 신호

- Structured Log
- Trace
- Metric
- Event
- Audit
- Health
- Registry
- Timeline
- Incident
- Evidence

## 3. 공통 식별자

- environment
- cellId
- systemCode
- moduleId
- serviceId
- serverInstanceId
- transactionId
- traceId
- segmentId
- operationId
- executionId
- jobId
- eventId

모든 저장 방식에서 같은 의미를 사용한다.

## 4. Structured Log

필드:

- timestamp
- level
- logger
- messageCode
- transactionId
- segmentId
- module
- instance
- execution
- status
- duration
- failureCode
- maskedContext

금지:

- Secret
- 전체 Request/Response
- 무제한 Stack Trace
- Credential URL
- 원문 개인정보

## 5. File Log

경로는 Environment/Service/Instance를 구분한다.

Rotation:

- 일자
- 크기
- 압축
- maxHistory
- totalSizeCap
- 디스크 상한
- 삭제 이력

## 6. DB Log

DB Log 장애가 원 업무에 미치는 정책을 정의한다.

- 필수 감사: 안전 차단
- 운영 보조 Log: Local Spool
- 재전송
- 유실 탐지
- Alert

## 7. Trace

Trace는 Segment와 Remote Attempt를 표현한다.

```text
Inbound
├─ Validation
├─ DB
├─ Remote Attempt 1
├─ Remote Attempt 2
└─ Result
```

Sampling과 Trace Boost 정책을 둔다.

## 8. Metric

Golden Signal:

- Traffic
- Error
- Latency
- Saturation

추가:

- Circuit
- Retry
- Unknown
- Queue Lag
- Worker Lease
- Gateway Apply
- DB Pool
- File Scan
- Compensation

## 9. SLO

SLO 구성:

- 대상 서비스
- 측정 지표
- 목표
- Window
- 제외 조건
- Error Budget
- Alert 정책
- Owner

단일 평균만 사용하지 않고 Percentile과 실패율을 사용한다.

## 10. Alert

Alert는 Actionable해야 한다.

- 대상
- 조건
- 지속 시간
- Severity
- Dedup Key
- Suppression
- Owner
- Runbook
- Incident 정책

## 11. Incident

상태:

```text
OPEN
→ ACKNOWLEDGED
→ INVESTIGATING
→ MITIGATED
→ RESOLVED
→ CLOSED
```

필드:

- incidentId
- severity
- service
- impact
- startedAt
- detectedAt
- owner
- timeline
- related transaction
- deployment/config
- rootCause
- action
- resolvedAt

## 12. Runbook

Runbook은 다음을 포함한다.

- 증상
- 확인 Query
- 안전한 진단
- 영향도 판단
- 자동 조치
- 수동 조치
- 금지 조치
- Rollback
- 복구 확인
- Escalation
- Evidence

## 13. 자동 복구

자동 조치는 제한을 갖는다.

- 조건
- 최대 횟수
- Cooldown
- 대상 Allowlist
- 실패 시 중단
- 반복 실패 차단
- Audit
- 운영자 통지

## 14. 결과 불명 복구

```text
Unknown 탐지
→ Downstream 상태 조회
→ 중복 영향 판단
→ 자동 확정 가능 여부
→ 운영자 Case
→ 재처리/보상
→ 최종 상태
```

## 15. Gateway 장애

확인 순서:

1. Route/Binding Version
2. Apply Status
3. Target Health
4. Attempt
5. TLS/Auth
6. Rate Limit
7. Timeout
8. Unknown
9. Drift
10. Rollback

## 16. Batch 장애

1. Definition Version
2. Trigger
3. Execution
4. Worker Lease
5. Spring Batch Metadata
6. Checkpoint
7. Agent
8. Unknown
9. Restart/Reprocess
10. SLA

## 17. DB 장애

- Liveness/Readiness
- Pool
- Lock
- Slow Query
- Replica
- Migration Drift
- Disk
- Backup
- Failover
- Transaction 결과 불명

## 18. Messaging 장애

- Producer
- Outbox
- Broker
- Lag
- Consumer
- Inbox
- Retry
- DLQ
- Poison
- Replay

## 19. Change Correlation

Incident는 최근 변경과 연결한다.

- Deployment
- Config
- Runtime Policy
- DB Migration
- Gateway Binding
- Certificate
- Secret Rotation
- Feature Flag

## 20. Capacity

- CPU
- Memory
- Thread
- Connection
- Queue
- Disk
- Network
- Worker Slot
- Agent Capacity

Trend와 Forecast를 제공한다.

## 21. Maintenance

- 신규 유입 차단
- Drain
- In-flight
- Batch Window
- Queue
- Health
- 복귀
- Audit

## 22. Evidence 수집

- Source/Artifact Version
- 환경
- 시각
- Dashboard Snapshot
- Query
- Trace
- Log
- 변경 이력
- 조치
- 결과
- 민감정보 제거

## 23. 사후 분석

- 영향
- Timeline
- 탐지
- 근본 원인
- 기여 요인
- 대응
- 잘된 점
- 개선
- Owner
- Due Date
- 재발 방지 Test

## 24. Test

- Alert Dedup
- Incident 연결
- Runbook
- Auto Recovery 상한
- Log Store 장애
- Trace Sampling
- Multi-instance
- 결과 불명
- Change Correlation
- DR

## 25. 체크리스트

- [ ] 거래와 실행이 동일 식별자로 연결된다.
- [ ] Log에 민감정보가 없다.
- [ ] Alert에 Owner와 Runbook이 있다.
- [ ] Incident가 변경 이력과 연결된다.
- [ ] 자동 복구에 상한과 감사가 있다.
- [ ] 결과 불명을 대사한다.
- [ ] 조치 후 복구 확인이 있다.
