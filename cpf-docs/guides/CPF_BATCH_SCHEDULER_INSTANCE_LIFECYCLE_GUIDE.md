# CPF 배치 일정관리기와 실행 생명주기 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 배치 설계자, 스케줄 운영자, 재처리 담당자
> **목적**: 시간대·달력·오실행·재시작·재실행·재처리 의미를 일관되게 운영한다.
> **관련 문서**: [배치 실행 환경과 원격 에이전트](CPF_BATCH_RUNTIME_AND_REMOTE_AGENT_GUIDE.md) · [용어와 계약 참조](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `:cpf-batch:scheduler`, 실행 상태 Owner는 Batch Control/Runtime |
| 이 문서로 완료하는 일 | Schedule·Calendar·Timezone·Misfire·Lease·Restart·Rerun·Reprocess 의미를 분리하고 다중 Scheduler에서 중복 Dispatch를 차단한다. |
| 적용 범위 | Schedule Projection, Trigger 계산, Dispatch, Execution 상태, 재실행 정책 |
| 주요 독자 | Scheduler 개발자, Batch 운영자, 업무 Job Owner |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

이 문서는 일정, 트리거, CPF 실행, Spring Batch JobInstance와 실행 환경 인스턴스를 명확히 구분하고 다중 인스턴스 일정관리기와 작업자가 안전하게 실행하는 방법을 정의한다.

## 2. 핵심 개념

| 개념 | 의미 |
|---|---|
| 작업 정의 | 무엇을 실행하는가 |
| 일정 | 언제 실행하는가 |
| 트리거 | 특정 예정 시각이 실제 Dispatch됐다는 기록 |
| CPF 실행 | 이번 업무 실행 요청 |
| Spring Batch JobInstance | Spring 배치 식별 매개변수 기반 인스턴스 |
| 실행 환경 인스턴스 | 일정관리기/작업자/에이전트 프로세스 |
| 시도 | 실행 또는 호출 시도 |
| 체크포인트 | 재시작 위치 |

## 3. 일정

실제 일정의 입력 정본은 게시된 작업정의의 실행 투영이다. 일정관리기는 투영 버전과 현재 트리거 버전을 비교해 생성·수정·비활성화를 수행하고, 주기적으로 전체 대사를 실행한다. 한 정의의 동기화 실패가 다른 정의의 일정 갱신을 롤백시키지 않도록 항목별 결과를 남긴다.


일정 필드:

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

CPF는 자정에 하루치 실행을 일괄 선생성하지 않는다.

```text
Scheduler Poll
→ nextFireAt <= now 조회
→ Calendar/Window/Misfire
→ Trigger 기록
→ CPF Execution 생성
→ nextFireAt 갱신
```

이 방식은 운영 중 일정 변경, 재기동과 다중 인스턴스에 유리하다.

## 5. 최초 `nextFireAt`

신규 일정 등록 시:

1. Cron과 시간대 검증
2. 현재 시각 이후 첫 예정 시각 계산
3. 달력/유효기간 반영
4. `nextFireAt` 저장
5. Preview 제공

`NULL`을 모호한 즉시 실행 의미로 사용하지 않는다.

## 6. 선도 임대(Leader Lease)

일정관리기는 공유 Store의 선도 임대(Leader Lease)를 사용한다.

- ownerId
- leaseUntil
- fencingToken
- version

갱신 실패 시 신규 트리거 생성을 중단한다.

## 7. 트리거 중복 방지

Unique Key:

```text
(scheduleId, scheduledFireAt)
```

트리거 Insert와 실행 생성은 트랜잭션 또는 송신함으로 연결한다.

## 8. Fencing

Leader가 바뀌면 세대 토큰이 증가한다. 이전 Leader의 늦은 Update는 거부한다.

```sql
UPDATE bat_schedule
SET next_fire_at = ?, fencing_token = ?
WHERE schedule_id = ?
  AND fencing_token = ?
```

## 9. 달력

일정은 `CmnBusinessCalendar`를 사용한다.

지원 정책:

- 휴일 Skip
- 이전 영업일
- 다음 영업일
- 최근 영업일
- 특정 기관 달력
- 임시 영업일
- 달력 버전

일정 생성 시 사용한 달력 판단 근거를 트리거에 기록한다.

## 10. 시간대와 DST

- 일정별 `ZoneId`
- 존재하지 않는 로컬 Time
- 중복되는 로컬 Time
- UTC 저장
- 운영 화면 로컬 표시
- DST 정책 Preview
- 테스트

## 11. 누락 실행

일정관리기 중단 중 놓친 예정 시각을 정책에 따라 처리한다.

### SKIP

누락 트리거를 만들지 않고 다음 시각 계산.

### FIRE_NOW

복구 시점에 한 번 실행.

### CATCH_UP

누락 시각별 실행을 생성하되 최대 개수와 기간 상한 적용.

### NEXT

과거 시각은 버리고 다음 미래 시각부터.

### COMPENSATE

정의된 보상 작업 또는 운영 승인으로 연결.

## 12. Available Window

예정 시각이 허용 창 밖이면:

- Skip
- 창 시작으로 이동
- 다음 날
- 운영 승인

중 하나를 일정 정책으로 명시한다.

## 13. 의존 대상

실행 생성 또는 시작 전 의존 대상을 평가한다.

```text
PAY-CLOSE
depends on PAY-LOAD
sameBusinessDate = true
requiredState = COMPLETED
timeout = 2h
```

시간 제한 이후 상태는 FAILED, SKIPPED, WAITING_APPROVAL 중 정책으로 정한다.

## 14. CPF 실행

하나의 CPF 실행에는 여러 시도가 연결될 수 있다. 작업자 인계, 재시도와 재기동을 분석하려면 실행 상태만 보지 않고 시도 번호, 작업자, 실행기 유형, 시작·종료·오류와 다음 재시도 시각을 함께 조회한다. 오래된 시도가 늦게 완료되면 현재 세대 토큰과 상태를 확인해 반영을 거부한다.


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

## 16. 작업자 점유

원자적 점유:

- 상태 READY
- 임대 만료 또는 미소유
- capacity
- zone
- required capability
- fencing

작업자가 점유 후 Definition 버전과 매개변수를 다시 검증한다.

## 17. Spring 배치 연결

작업자가 `JobLauncher.run()`을 호출할 때 Spring Batch JobInstance/JobExecution이 생성된다.

Identifying 매개변수:

- cpfExecutionId
- businessDate
- 작업 묶음 정의 값

세대 토큰은 실행 보호 정보이며 JobInstance 업무 식별 의미와 구분한다.

## 18. Restart

Restart 조건:

- 동일 CPF 실행
- Restart 가능한 상태
- 작업 저장소 상태
- 체크포인트
- 매개변수 동일
- 동일 Definition 버전
- 권한과 사유

## 19. Rerun/Reprocess

새 CPF 실행을 만든다.

- originalExecutionId
- new operationId
- override 검증
- 중복 업무 영향 확인
- 승인
- 별도 감사

## 20. 실행 소유권 상실

작업자 심박과 임대가 끊기면 즉시 실패로 확정하지 않는다.

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
- 결과 불명 장기화

경보는 사고와 연결한다.

## 22. 대량 일정

- Due 조회 Index
- 페이지/배치 Poll
- Leader 단일 병목 검토
- Partitioned Scheduling
- Backpressure
- Catch-up 상한
- 지표

## 23. 운영 화면

분리 화면:

- 작업 정의
- 일정
- 트리거
- 실행
- 작업자
- Spring 배치 메타데이터
- Restart/Reprocess
- 결과 불명·상태 대사
- SLA

## 24. 테스트

- Cron 경계
- 시간대
- DST
- 달력 변경
- 중복 트리거
- 2 일정관리기
- Leader Kill
- Network Delay
- Stale Fencing
- 누락 실행
- 대량 Dispatch
- 작업자 Takeover
- Restart/Rerun

## 25. 체크리스트

- [ ] 일정과 실행을 구분한다.
- [ ] 최초 nextFireAt을 계산한다.
- [ ] 트리거 유일성이 있다.
- [ ] 선도 임대(Leader Lease)와 Fencing이 있다.
- [ ] 시간대/DST 정책이 있다.
- [ ] 누락 실행별 의미가 명확하다.
- [ ] 의존 대상과 시간 제한을 실행계가 적용한다.
- [ ] Restart와 Reprocess가 분리된다.
- [ ] 실행 소유권 상실을 대사한다.

## 부록 A. 시간대와 일광절약시간 예

| 상황 | 정책 선택 예 |
|---|---|
| 시계가 앞으로 이동해 02:30이 존재하지 않음 | 건너뛰기, 다음 유효 시각, 명시적 보정 중 하나 |
| 시계가 뒤로 이동해 01:30이 두 번 존재 | 첫 번째·두 번째·한 번만 실행 중 하나 |
| 서버 시간대와 업무 시간대가 다름 | 일정에 업무 시간대 식별자를 저장 |
| 장시간 중단 뒤 여러 예정 시각 누락 | 모두 따라잡기, 마지막 한 건, 건너뛰기, 운영 승인 |

일정 저장 시 단순 오프셋만 저장하지 않고 지역 시간대 식별자와 정책 버전을 함께 저장한다.

## 부록 B. 재시작·재실행·재처리 선택

| 목적 | 선택 |
|---|---|
| 같은 실행의 실패 단계부터 계속 | 재시작 |
| 같은 매개변수로 새 실행을 처음부터 수행 | 재실행 |
| 원본 자료 중 실패·선택 범위만 다시 처리 | 재처리 |
| 예정 시각을 놓친 실행을 생성 | 오실행 정책 |

운영 화면은 네 동작을 다른 명령과 권한으로 제공하고 예상 영향 범위를 표시한다.

## 부록 C. 동시 실행 정책

- `ALLOW`: 같은 작업의 여러 실행 허용
- `FORBID`: 이전 실행 중이면 새 실행 생성을 거부
- `QUEUE`: 이전 실행 종료 뒤 시작하도록 대기
- `REPLACE`: 이전 실행을 안전 중단한 뒤 새 실행 시작

`REPLACE`는 강제 프로세스 종료가 아니라 취소 요청, 체크포인트, 배수와 중단 확인을 포함한다.

## 34. Schedule 계산 기준

- 모든 Schedule은 Timezone을 명시한다.
- 저장 시 Local Time과 Zone ID를 함께 보존하고 실행 시 Instant로 계산한다.
- DST 중복·누락 시각의 실행 정책을 정의한다.
- Holiday/Business Calendar Version을 Projection에 포함한다.
- 다음 실행 시각 계산은 같은 입력에서 결정적이어야 한다.
- Schedule 변경은 과거 Execution 의미를 바꾸지 않고 새 Version으로 적용한다.

## 35. Misfire 정책

| 정책 | 사용 상황 | 위험 |
|---|---|---|
| 즉시 1회 실행 | 최신 상태만 필요한 Job | 오래된 기간 누락 가능 |
| 누락 횟수만큼 Catch-up | 각 기간 처리가 필수 | 폭주·중복·장시간 실행 |
| 다음 정상 시각부터 | 과거 실행 불필요 | 업무 누락 승인 필요 |
| 운영 승인 후 선택 | 고위험·대량 Job | 대응 지연 |

Misfire가 발생한 이유, 누락된 Trigger 범위, 선택한 정책과 결과를 실행 원장에 남긴다.

## 36. Restart·Rerun·Reprocess 구분

- **Restart**: 같은 Execution과 Checkpoint를 이어간다.
- **Rerun**: 같은 업무 기간을 새 Execution으로 처음부터 실행한다.
- **Reprocess**: 실패·선별 데이터만 새 작업 단위로 처리한다.

운영 화면과 API에서 세 동작을 같은 “재실행” 버튼으로 합치지 않는다. Parameter, Checkpoint, 멱등성, 기존 Side Effect와 결과 대사 방식이 다르다.

## 37. 다중 Scheduler Takeover

1. Due 후보를 읽는다.
2. 조건부 Update 또는 Claim으로 Owner·Lease·Fencing을 획득한다.
3. Claim 성공한 인스턴스만 Execution을 생성한다.
4. Dispatch 전 현재 Fencing과 Projection Version을 다시 확인한다.
5. Lease 갱신 실패 시 신규 Dispatch를 중지한다.
6. 다른 Scheduler가 Takeover하면 과거 Scheduler의 늦은 Insert/완료를 차단한다.
7. 중복 Execution이 생성됐는지 Unique Key와 원장으로 검증한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Scheduler | `cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/` | Projection Sync·Due 검색·Dispatch |
| Runtime SQL | `cpf-tools/db/vendor/*/runtime/bat/repository/scheduler-*.sql` | Due Claim·Execution Insert |
| Execution Owner | `cpf-batch/worker`, `cpf-batch/control-server` | 실행 상태·재시도·운영 명령 |
| Fault Test | `git ls-files "*test*" | Select-String "Misfire|Lease|Fencing|Scheduler"` | 중복·Takeover·시간대 경계 |

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
