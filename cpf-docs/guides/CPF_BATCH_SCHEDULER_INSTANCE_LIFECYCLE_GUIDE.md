# CPF Batch Scheduler와 실행 생명주기 가이드

## 1. 목적

이 문서는 Schedule, Trigger, CPF Execution, Spring Batch JobInstance와 Runtime Instance를 명확히 구분하고 다중 인스턴스 Scheduler와 Worker가 안전하게 실행하는 방법을 정의한다.

## 2. 핵심 개념

| 개념 | 의미 |
|---|---|
| Job Definition | 무엇을 실행하는가 |
| Schedule | 언제 실행하는가 |
| Trigger | 특정 예정 시각이 실제 Dispatch됐다는 기록 |
| CPF Execution | 이번 업무 실행 요청 |
| Spring Batch JobInstance | Spring Batch 식별 Parameter 기반 인스턴스 |
| Runtime Instance | Scheduler/Worker/Agent Process |
| Attempt | 실행 또는 호출 시도 |
| Checkpoint | 재시작 위치 |

## 3. Schedule

Schedule 필드:

- scheduleId
- jobId
- definitionVersion 정책
- cronExpression
- timezone
- calendarId
- businessDayOnly
- holidayPolicy
- availableWindow
- misfirePolicy
- enabled
- validFrom / validTo
- nextFireAt
- version

## 4. Due-time 생성

CPF는 자정에 하루치 Execution을 일괄 선생성하지 않는다.

```text
Scheduler Poll
→ nextFireAt <= now 조회
→ Calendar/Window/Misfire
→ Trigger 기록
→ CPF Execution 생성
→ nextFireAt 갱신
```

이 방식은 운영 중 Schedule 변경, 재기동과 다중 인스턴스에 유리하다.

## 5. 최초 `nextFireAt`

신규 Schedule 등록 시:

1. Cron과 Timezone 검증
2. 현재 시각 이후 첫 예정 시각 계산
3. Calendar/유효기간 반영
4. `nextFireAt` 저장
5. Preview 제공

`NULL`을 모호한 즉시 실행 의미로 사용하지 않는다.

## 6. Leader Lease

Scheduler는 공유 Store의 Leader Lease를 사용한다.

- ownerId
- leaseUntil
- fencingToken
- version

갱신 실패 시 신규 Trigger 생성을 중단한다.

## 7. Trigger 중복 방지

Unique Key:

```text
(scheduleId, scheduledFireAt)
```

Trigger Insert와 Execution 생성은 Transaction 또는 Outbox로 연결한다.

## 8. Fencing

Leader가 바뀌면 Fencing Token이 증가한다. 이전 Leader의 늦은 Update는 거부한다.

```sql
UPDATE bat_schedule
SET next_fire_at = ?, fencing_token = ?
WHERE schedule_id = ?
  AND fencing_token = ?
```

## 9. Calendar

Schedule은 `CmnBusinessCalendar`를 사용한다.

지원 정책:

- 휴일 Skip
- 이전 영업일
- 다음 영업일
- 최근 영업일
- 특정 기관 Calendar
- 임시 영업일
- Calendar Version

Schedule 생성 시 사용한 Calendar 판단 근거를 Trigger에 기록한다.

## 10. Timezone과 DST

- Schedule별 `ZoneId`
- 존재하지 않는 Local Time
- 중복되는 Local Time
- UTC 저장
- 운영 화면 Local 표시
- DST 정책 Preview
- Test

## 11. Misfire

Scheduler 중단 중 놓친 예정 시각을 정책에 따라 처리한다.

### SKIP

누락 Trigger를 만들지 않고 다음 시각 계산.

### FIRE_NOW

복구 시점에 한 번 실행.

### CATCH_UP

누락 시각별 실행을 생성하되 최대 개수와 기간 상한 적용.

### NEXT

과거 시각은 버리고 다음 미래 시각부터.

### COMPENSATE

정의된 보상 Job 또는 운영 승인으로 연결.

## 12. Available Window

예정 시각이 허용 창 밖이면:

- Skip
- 창 시작으로 이동
- 다음 날
- 운영 승인

중 하나를 Schedule 정책으로 명시한다.

## 13. Dependency

Execution 생성 또는 시작 전 Dependency를 평가한다.

```text
PAY-CLOSE
depends on PAY-LOAD
sameBusinessDate = true
requiredState = COMPLETED
timeout = 2h
```

Timeout 이후 상태는 FAILED, SKIPPED, WAITING_APPROVAL 중 정책으로 정한다.

## 14. CPF Execution

필드:

- executionId
- jobId
- definitionVersion
- checksum
- scheduleId
- triggerId
- scheduledFireAt
- businessDate
- state
- requestedBy
- operationId
- transactionId
- retryCount
- version

## 15. 실행 상태

```text
READY
→ CLAIMED
→ RUNNING
→ COMPLETED

RUNNING
→ FAILED
→ RESTART_READY
→ RUNNING

RUNNING
→ UNKNOWN_RESULT
→ RECONCILING
→ COMPLETED / FAILED
```

Stop, Cancel, Skip와 Manual Confirm은 별도 Action이다.

## 16. Worker Claim

원자적 Claim:

- 상태 READY
- Lease 만료 또는 미소유
- capacity
- zone
- required capability
- fencing

Worker가 Claim 후 Definition Version과 Parameter를 다시 검증한다.

## 17. Spring Batch 연결

Worker가 `JobLauncher.run()`을 호출할 때 Spring Batch JobInstance/JobExecution이 생성된다.

Identifying Parameter:

- cpfExecutionId
- businessDate
- Job Pack 정의 값

Fencing Token은 실행 보호 정보이며 JobInstance 업무 식별 의미와 구분한다.

## 18. Restart

Restart 조건:

- 동일 CPF Execution
- Restart 가능한 상태
- Job Repository 상태
- Checkpoint
- Parameter 동일
- 동일 Definition Version
- 권한과 사유

## 19. Rerun/Reprocess

새 CPF Execution을 만든다.

- originalExecutionId
- new operationId
- override 검증
- 중복 업무 영향 확인
- 승인
- 별도 Audit

## 20. Lost Execution

Worker Heartbeat와 Lease가 끊기면 즉시 실패로 확정하지 않는다.

```text
Lease 만료
→ Process/Job Repository 조회
→ Agent 상태 조회
→ RUNNING / COMPLETED / UNKNOWN 판단
→ Takeover 또는 운영 확인
```

## 21. SLA

- 예정 시각 대비 시작 지연
- 실행 시간
- 완료 Deadline
- 미실행
- 반복 실패
- Unknown 장기화

Alert는 Incident와 연결한다.

## 22. 대량 Schedule

- Due Query Index
- Page/Batch Poll
- Leader 단일 병목 검토
- Partitioned Scheduling
- Backpressure
- Catch-up 상한
- Metrics

## 23. 운영 화면

분리 화면:

- Job Definition
- Schedule
- Trigger
- Execution
- Worker
- Spring Batch Metadata
- Restart/Reprocess
- Unknown/Reconcile
- SLA

## 24. Test

- Cron 경계
- Timezone
- DST
- Calendar 변경
- 중복 Trigger
- 2 Scheduler
- Leader Kill
- Network Delay
- Stale Fencing
- Misfire
- 대량 Dispatch
- Worker Takeover
- Restart/Rerun

## 25. 체크리스트

- [ ] Schedule과 Execution을 구분한다.
- [ ] 최초 nextFireAt을 계산한다.
- [ ] Trigger Unique가 있다.
- [ ] Leader Lease와 Fencing이 있다.
- [ ] Timezone/DST 정책이 있다.
- [ ] Misfire별 의미가 명확하다.
- [ ] Dependency와 Timeout을 실행계가 적용한다.
- [ ] Restart와 Reprocess가 분리된다.
- [ ] Lost Execution을 대사한다.
