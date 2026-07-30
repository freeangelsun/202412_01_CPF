# CPF Health와 Service Registry 가이드

## 1. 목적

CPF Health는 단순 HTTP 200 확인이 아니다. Process 생존, 신규 요청 수신 가능 여부, 서비스 전체 상태와 Routing 가능성을 분리해 판단한다.

## 2. Health 단계

### Liveness

현재 Process가 살아 있고 기본 실행 Loop가 응답하는지 확인한다.

포함:

- JVM/Process
- Event Loop/Thread 기본 상태
- 치명적 내부 오류

제외:

- 원격 서비스 Fan-out
- 전체 DB Query
- 느린 외부 호출

### Readiness

현재 Instance가 신규 요청을 받을 수 있는지 판단한다.

포함:

- 필수 Local DB
- Listener
- Runtime 초기화
- 필수 Secret/Certificate
- Drain/Maintenance
- Critical Queue/Storage

### Diagnostics

원격 Owner, 외부 기관, Broker 등 상세 Dependency를 별도로 진단한다. Diagnostics 실패가 항상 현재 Instance Readiness를 내리는 것은 아니다.

## 3. Registry 모델

Service:

- serviceId
- systemCode
- moduleId
- owner
- version
- protocol
- endpoint
- visibility
- capability

Instance:

- serverInstanceId
- host
- processId
- zone
- cell
- profile
- startedAt
- heartbeatAt
- liveness
- readiness
- drain
- maintenance
- capacity
- metadata

## 4. 등록

```text
Runtime 시작
→ Identity 생성
→ Local Validation
→ Registry 등록
→ Heartbeat
→ Readiness UP
```

중복 Instance ID를 거부한다.

## 5. Heartbeat

- 주기
- TTL
- Jitter
- Batch Update
- Network Timeout
- Stale 판단
- 복구

Heartbeat Store 장애 시 Local Runtime을 불필요하게 종료하지 않되 Routing 안전성을 위해 Registry 상태를 명확히 한다.

## 6. 상태

- UP
- DEGRADED
- DRAINING
- MAINTENANCE
- DOWN
- STALE
- UNKNOWN

상태 전이와 원인을 기록한다.

## 7. Hysteresis

한 번의 실패로 즉시 DOWN/UP을 반복하지 않는다.

- 연속 실패 수
- 연속 성공 수
- 최소 유지시간
- Passive 오류
- Active Probe

## 8. Routing

신규 요청 대상:

- readiness UP
- drain 아님
- maintenance 아님
- circuit 허용
- zone 정책
- capacity

## 9. Drain

```text
DRAIN_REQUESTED
→ 신규 요청 제외
→ In-flight 감소
→ DRAINED
```

최대 대기시간 후 강제 정책을 정의한다.

## 10. Maintenance

- 사유
- 시작/종료
- Owner
- 승인
- 신규 유입 차단
- Health 표시
- 자동 복귀
- Audit

## 11. Service Group

Registry는 Server Group과 연결된다.

- Member
- Weight
- Priority
- Zone
- Health
- Version

## 12. Dependency

Dependency Graph:

- caller
- target
- protocol
- criticality
- timeout
- fallback
- owner

Incident 영향 분석에 사용한다.

## 13. Version

Rolling 중 Version 혼재를 표시한다.

- compatible
- deprecated
- minimum peer version
- protocol/schema version

## 14. Security

Health 응답에 다음을 노출하지 않는다.

- Password
- DB URL Credential
- Secret
- Private IP 정책상 금지 정보
- Stack Trace
- 개인정보

상세 Diagnostics는 운영 권한을 요구한다.

## 15. Metrics

- instance count
- healthy count
- stale count
- readiness duration
- heartbeat lag
- state transition
- drain duration
- registry error
- probe latency

## 16. 운영 절차

### Instance DOWN

1. Registry 시각
2. Liveness
3. Host/Process
4. 최근 Deployment
5. Log
6. Traffic 영향
7. 자동 복구
8. 재기동
9. 복귀
10. Incident

### Readiness DOWN

1. Reason
2. Local Dependency
3. Pool/Listener
4. Secret/Certificate
5. Drain/Maintenance
6. 복구 후 Routing

## 17. Test

- Instance 2개
- 서로 다른 ID
- DB 중단
- Heartbeat 중단
- Network Partition
- Stale
- Hysteresis
- Drain
- Maintenance
- Rolling Version
- Registry Store 장애
- 민감정보

## 18. 체크리스트

- [ ] Liveness와 Readiness를 분리한다.
- [ ] 원격 Fan-out을 Liveness에 넣지 않는다.
- [ ] Instance Identity가 Log/Trace와 같다.
- [ ] Heartbeat TTL과 Stale 정책이 있다.
- [ ] Drain/Maintenance가 Routing에 반영된다.
- [ ] 상태 전이 원인을 기록한다.
- [ ] Health에 민감정보가 없다.
