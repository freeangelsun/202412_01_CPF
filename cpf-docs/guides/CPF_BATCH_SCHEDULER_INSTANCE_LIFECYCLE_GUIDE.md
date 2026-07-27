# CPF Batch Scheduler · Execution · Spring Batch Instance Lifecycle 가이드

## 1. 결론

현재 CPF BAT는 **매일 0시에 당일 수행 대상 Job Instance를 전부 미리 생성하는 방식이 아니다.**

업무 스케줄 정본은 DB의 `bat_schedule`이며 Scheduler가 주기적으로 due schedule을 확인한다.
실제 예정시각이 도래하면 CPF의 `bat_execution`을 생성하고, Worker가 해당 실행을 Claim한 뒤 Spring Batch `JobLauncher`를 호출할 때
Spring Batch의 `JobInstance/JobExecution`이 생성된다.

즉 세 단계가 서로 다르다.

```text
bat_schedule            = 언제 수행할 것인가에 대한 장기 스케줄 정의
bat_execution           = 이번에 실제 수행해야 하는 CPF 실행 인스턴스
Spring Batch JobInstance = Worker가 Spring Batch Job을 실제 launch하면서 만드는 Spring Batch 메타 인스턴스
```

## 2. 설정은 어디에 있는가

### 2.1 업무 스케줄 — DB 정본

`bat_schedule`의 주요 컬럼:

- `schedule_id`
- `job_id`
- `cron_expression`
- `calendar_id`
- `business_day_only_yn`
- `holiday_policy`
- `available_start_time`
- `available_end_time`
- `run_date_pattern`
- `timezone`
- `enabled_yn`
- `last_fire_at`
- `next_fire_at`

따라서 "매일 02:00", "영업일만", "휴일이면 다음 영업일" 같은 **업무 수행 정책은 Property가 아니라 DB 운영 데이터**다.
ADM/Control Plane에서 변경·승인·감사 가능한 구조로 발전시키는 것이 목표다.

### 2.2 Scheduler Polling — Property

Scheduler가 DB를 얼마나 자주 확인할지는 Runtime Property다.

```properties
cpf.batch.scheduler.dispatch-ms=1000
```

Source 기본값은 1초다.
이 값은 Job의 실행시간이 아니라 **Scheduler polling interval**이다.

## 3. 실행 흐름

```text
1. Scheduler Leader Lease/Fencing 확보
        ↓
2. bat_schedule에서 next_fire_at <= now 인 활성 스케줄 조회
        ↓
3. available window / 영업일 Calendar / holiday policy 적용
        ↓
4. bat_schedule_trigger에 (schedule_id, scheduled_fire_at) 기록
   - PK/Unique + fencing으로 중복 Trigger 방지
        ↓
5. bat_execution READY 생성
   - job_id
   - schedule_id
   - business_date
   - requested_by=SCHEDULER
        ↓
6. cron_expression으로 다음 next_fire_at 계산/갱신
        ↓
7. Worker가 bat_execution을 Lease/Claim
        ↓
8. Job Pack에서 실제 Job resolve
        ↓
9. Spring Batch JobLauncher.run(job, parameters)
        ↓
10. Spring Batch JobInstance / JobExecution 생성
        ↓
11. Spring Batch ID를 CPF bat_execution에 연결
        ↓
12. COMPLETED / FAILED / STOPPED / UNKNOWN_RESULT 정리
```

## 4. 왜 일 초기화 선생성 방식을 사용하지 않는가

현재 방식의 장점:

- 운영자가 당일 스케줄을 중지/변경해도 미리 생성된 대량 실행 인스턴스를 정리할 필요가 적다.
- Calendar/휴일 정책을 Trigger 시점에 최신 상태로 적용할 수 있다.
- Scheduler 다중 Instance에서도 Lease/Fencing과 Trigger Unique Key로 중복 생성 제어가 가능하다.
- 수천/수만 Job의 일별 실행 Row를 자정에 한꺼번에 생성하지 않는다.
- 재기동 시 `next_fire_at`과 Trigger Evidence로 이어서 판단할 수 있다.

따라서 기본 제품 구조는 **Due-time materialization**을 유지하는 것이 적절하다.

## 5. `next_fire_at IS NULL`의 의미

현재 due query는 `next_fire_at IS NULL OR next_fire_at <= now`를 조회한다.
`NULL`이면 Scheduler가 현재 시각을 기준으로 최초 Trigger 후보로 판단하고, 처리 후 Cron의 다음 시각으로 `next_fire_at`을 갱신한다.

제품 완결 시에는 신규 Schedule 등록 단계에서 최초 `next_fire_at`을 명시 계산하는 정책과 현재 Lazy 초기화 정책 중 하나를 정본화하고,
중복/과거시각/Misfire 정책을 Test로 고정해야 한다.

## 6. `bat_instance`는 Job Instance가 아니다

BAT Schema의 `bat_instance`는 Scheduler/Worker/Control/Runner 같은 **배치 Runtime 서버 인스턴스 등록·Heartbeat/상태 추적용** 개념이다.
하루치 Job 실행 대상을 미리 만드는 테이블이 아니며 Spring Batch `BATCH_JOB_INSTANCE`와도 역할이 다르다.

따라서 운영 UI/Guide에서는 다음 용어를 혼용하지 않는다.

- Runtime Instance: 실행 서버/프로세스 인스턴스
- CPF Execution: 이번 실행 요청/수행 인스턴스
- Spring Batch JobInstance: Spring Batch 메타데이터의 Job identity

## 7. Spring Batch JobInstance와 CPF Execution의 차이

CPF Worker는 Spring Batch 실행 시 identifying parameter에 다음을 포함한다.

- `cpfExecutionId`
- `cpfFencingToken`
- `businessDate` (존재 시)
- Job Pack이 정의한 identifying parameters

따라서 현재 구조에서는 동일 업무일자의 재실행이라도 새로운 `cpfExecutionId`가 부여되면 Spring Batch 관점에서 새로운 JobInstance가 될 수 있다.

이 부분은 Restart와 Rerun을 명확히 구분해야 한다.

- **Restart**: 동일 업무 실행의 실패 Step부터 이어가는 의미
- **Rerun/Reprocess**: 새로운 CPF Execution을 만들어 다시 수행하는 의미

BAT Legacy parity 작업에서 Spring Batch JobInstance identity, CPF execution identity, checkpoint/restart 정책을 함께 확정한다.

## 8. Multi-instance 안전장치

현재 Scheduler에는 다음 구조가 있다.

- Scheduler leader lease
- monotonic fencing token
- `bat_schedule_trigger(schedule_id, scheduled_fire_at)` PK
- Trigger insert/update 시 fencing 조건
- `next_fire_at` optimistic/concurrent update 방어

따라서 Scheduler가 2개 이상 떠도 동일 Scheduled Fire를 두 번 materialize하지 않는 것이 목표다.
실제 2 Scheduler process kill/takeover/network-delay 검증은 아직 통합검증 대상이다.

## 9. 운영자가 보게 될 개념

ADM/BAT Control Plane에서는 최소 다음을 분리해서 보여줘야 한다.

1. **Job 정의** — 무엇을 실행하는가
2. **Schedule 정의** — 언제 실행하는가
3. **예정 Trigger** — 어떤 예정시각이 dispatch되었는가
4. **CPF Execution** — 이번 업무 실행의 상태
5. **Worker Lease/Fencing** — 누가 실행권을 가지고 있는가
6. **Spring Batch Instance/Execution** — Spring Batch 내부 실행 ID
7. **Recovery/Restart/Reprocess** — 장애 이후 어떤 방식으로 재개하는가

"배치 인스턴스"라는 한 단어로 위 개념을 섞지 않는 것이 중요하다.

## 10. 현재 검증 상태

Source/SQL 구조로 확인된 사항:

- DB schedule 기반 due dispatch
- Property는 polling interval
- due-time `bat_execution` 생성
- Trigger unique/fencing
- Worker `JobLauncher` 시 Spring Batch instance 생성

아직 실제 Runtime으로 완료 판정하지 않는 사항:

- 자정/Timezone/DST 경계
- Misfire/장시간 Scheduler down 후 catch-up
- 2 Scheduler takeover
- 동일 businessDate Restart/Rerun
- Calendar 변경과 이미 due인 Schedule 경쟁
- 대량 Schedule dispatch 성능

이 항목은 BAT Change Set과 Multi-instance/Fault 통합검증에서 실행한다.
