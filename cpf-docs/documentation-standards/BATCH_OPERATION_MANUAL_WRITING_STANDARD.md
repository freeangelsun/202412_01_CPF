# CPF 배치 운영 매뉴얼 작성 지침

## 1. 문서 목적

배치 운영 매뉴얼은 **배치 프로그램을 개발하는 방법을 설명하는 문서가 아니라, 이미 등록된 배치를 운영자가 조회·실행·중지·재시작·재처리·대사하고 이상 상황을 판단하는 방법을 설명하는 문서**로 작성한다.

운영자는 Java Source나 Spring Batch 내부 구현을 분석하지 않고도 다음 질문에 답할 수 있어야 한다.

- 지금 어떤 배치가 등록되어 있는가?
- 이 배치는 언제, 왜 실행되는가?
- 정상적으로 끝났는가?
- 아직 실행 중인가, 멈춘 것인가?
- 실패했으면 어느 Step에서 왜 실패했는가?
- 다시 실행해도 되는가?
- Restart와 신규 실행의 차이는 무엇인가?
- 실패한 건만 재처리할 수 있는가?
- 동일 업무가 중복 처리될 가능성은 없는가?
- Scheduler가 실행하지 않은 것인가, Batch가 실패한 것인가?
- Runner·Worker·Agent 중 어디에 문제가 있는가?
- Partition 일부만 실패했으면 어떻게 처리하는가?
- 실행 요청 응답을 받지 못했으면 실행 여부를 어떻게 판정하는가?
- `UNKNOWN_RESULT` 상태에서는 무엇을 확인하는가?
- Stop을 눌렀는데 계속 실행 중이면 무엇을 확인하는가?
- Job을 Abandon하면 다시 실행할 수 있는가?
- 재실행 후 정상화되었다는 것을 무엇으로 판단하는가?
- 업무 처리 건수와 DB 결과가 맞는지 어디서 확인하는가?
- Center-Cut 배치의 분할·처리·대사 결과는 어디서 보는가?
- 운영자가 할 수 있는 조치와 개발자에게 넘겨야 하는 조치는 무엇인가?
- 모든 운영 조치가 Audit에 남는가?

이 질문에 문서에서 바로 답을 찾을 수 없다면 배치 운영 매뉴얼로 충분하지 않은 것으로 판단한다.

---

# 2. 배치 개발자 매뉴얼과의 역할 분리

| 구분배치 개발자 매뉴얼배치 운영 매뉴얼   |                |                                |
| ----------------------- | -------------- | ------------------------------ |
| 주 사용자                   | 개발자            | 운영자·배치 관리자                     |
| 중심 질문                   | 어떻게 구현하는가      | 지금 무엇이 발생했고 어떻게 조치하는가          |
| Job/Step                | 구현·설계          | 상태 확인·운영                       |
| Reader/Processor/Writer | 구현 방법          | 처리 건수와 오류 확인                   |
| Transaction             | Commit 설계      | Commit 경계에 따른 재처리 영향           |
| Scheduler               | 연계 구현          | 일정 등록·변경·Misfire 확인            |
| Partition               | 구현             | Partition별 진행·실패·재처리           |
| Retry                   | 코드 정책          | 운영자가 허용된 Retry를 실행하는 방법        |
| Restart                 | Restartable 구현 | Restart 가능 여부 판정 및 실행          |
| UNKNOWN\_RESULT         | 개발 처리 규약       | 실행 여부 확인·대사·Reconcile          |
| ADM                     | 개발·API 연계      | 실제 운영 화면 사용                    |
| 장애 분석                   | Source·Log 중심  | 화면 → 상태 → Log → Metric → 조치 순서 |
| Test                    | 개발 Test        | 운영 시나리오 검증                     |

두 문서는 중복 설명을 최소화하되 **운영자가 개발자 매뉴얼을 읽어야만 작업할 수 있게 만들면 안 된다.**

---

# 3. 문서 첫 부분에 반드시 제공할 빠른 탐색 영역

## 3.1 운영자 질문별 바로가기

문서 초반에 다음과 같은 질문형 탐색표를 둔다.

| 내가 하려는 일확인 위치사용하는 기능관련 절 |                          |                 |    |
| ------------------------ | ------------------------ | --------------- | -- |
| 오늘 실패한 Job 확인            | Batch Execution          | 실행 이력 조회        | §X |
| 특정 Job 강제 실행             | Job Detail               | Manual Execute  | §X |
| 실행 중 Job 중지              | Execution Detail         | Stop            | §X |
| 실패 Job 재시작               | Execution Detail         | Restart         | §X |
| 일부 실패 건 재처리              | Reprocess 화면             | Reprocess       | §X |
| 응답 없는 실행 확인              | Execution/Reconciliation | Reconcile       | §X |
| Scheduler 미실행 확인         | Scheduler History        | Trigger/Misfire | §X |
| Partition 실패 확인          | Partition Detail         | Partition 조회    | §X |
| 처리 건수 대사                 | Execution/Reconciliation | Reconciliation  | §X |
| Worker 상태 확인             | Worker/Agent             | Health·Lease    | §X |
| 실행 승인                    | Approval                 | Approve/Reject  | §X |
| 과거 조치 확인                 | Audit                    | Audit Search    | §X |

운영자는 목차를 처음부터 읽지 않아도 이 표에서 바로 필요한 절로 이동할 수 있어야 한다.

---

# 4. 운영 기능 Summary

## 4.1 운영자가 사용할 수 있는 기능 전체 목록

반드시 실제 구현을 기준으로 작성한다.

| 기능목적실행 주체필요 권한실행 가능 상태결과 상태승인 필요Audit관련 화면/API |                  |     |               |                  |                        |       |             |              |
| ---------------------------------------------- | ---------------- | --- | ------------- | ---------------- | ---------------------- | ----- | ----------- | ------------ |
| Job 조회                                         | 등록 Job 조회        | 운영자 | 실제 Permission | -                | -                      | 아니오   | 조회 Audit 여부 | 실제 Route     |
| 수동 실행                                          | Job 실행           | 운영자 | 실제 Permission | 실제 조건            | 실제 상태                  | 실제 여부 | 예           | 실제 Route/API |
| Stop                                           | 실행 중지 요청         | 운영자 | 실제 Permission | STARTED 등        | STOPPING/STOPPED 등     | 실제 여부 | 예           | 실제 Route/API |
| Restart                                        | 이전 Execution 재시작 | 운영자 | 실제 Permission | FAILED/STOPPED 등 | 실제 상태                  | 실제 여부 | 예           | 실제 Route/API |
| Abandon                                        | Execution 폐기     | 관리자 | 실제 Permission | 실제 조건            | ABANDONED              | 실제 여부 | 예           | 실제 Route/API |
| Retry                                          | 실패 요청 재시도        | 운영자 | 실제 Permission | 실제 조건            | 실제 결과                  | 실제 여부 | 예           | 실제 Route/API |
| Reprocess                                      | 업무 건 재처리         | 운영자 | 실제 Permission | 실제 조건            | 실제 상태                  | 실제 여부 | 예           | 실제 Route/API |
| Reconcile                                      | 결과 대사            | 운영자 | 실제 Permission | 실제 조건            | MATCH/MISMATCH 등 실제 상태 | 실제 여부 | 예           | 실제 Route/API |

Source에 존재하지 않는 기능이나 상태는 추가하지 않는다.

---

# 5. 전체 권장 목차

## 1. 배치 운영 개요

### 1.1 이 매뉴얼의 대상

### 1.2 배치 운영자의 역할

### 1.3 개발자·운영자·관리자 책임 구분

### 1.4 CPF 배치 구성요소

### 1.5 Batch Engine과 ADM의 관계

### 1.6 Scheduler와 Batch Engine의 관계

### 1.7 Runner·Worker·Agent 역할

### 1.8 Online·Batch 업무 경계

### 1.9 지원되는 배치 실행 형태

### 1.10 현재 구현 범위와 미구현·미검증 범위

---

# 6. 배치 운영 Architecture

운영자가 장애 위치를 이해할 정도로 설명한다.

```
Scheduler
    ↓
Execution Request
    ↓
Batch Control
    ↓
Runner
    ↓
Spring Batch Job
    ↓
Step
    ├─ Tasklet
    ├─ Chunk
    └─ Partition
          ↓
        Worker
          ↓
      Business DB

Metadata DB
Audit
Metric
Log
ADM

```

각 구성요소마다 다음 표를 둔다.

| 구성요소역할정상 여부 확인 위치주요 상태실패 시 영향운영자 조치 |
| ----------------------------------- |

특히 다음을 혼동하지 않도록 설명한다.

- Scheduler 실패
- 실행 요청 실패
- Runner 시작 실패
- Job 시작 실패
- Step 실패
- Worker 실패
- DB 처리 실패
- 결과 응답 유실
- ADM 표시 문제

---

# 7. 배치 용어 사전

운영자가 반드시 알아야 할 용어를 별도 장으로 둔다.

- Job
- JobInstance
- JobExecution
- Step
- StepExecution
- JobParameter
- Tasklet
- Chunk
- Reader
- Processor
- Writer
- Commit Interval
- Checkpoint
- Restart
- Retry
- Reprocess
- Abandon
- Stop
- Partition
- Worker
- Runner
- Agent
- Scheduler
- Trigger
- Misfire
- Lease
- Claim
- Fencing
- Idempotency
- Attempt
- Attempt Ledger
- UNKNOWN\_RESULT
- Reconciliation
- Compensation
- Center-Cut
- Dry Run
- Preview
- Artifact
- Job Pack

각 용어는 한 줄 정의로 끝내지 않고 다음 형식으로 작성한다.

| 항목내용           |              |
| -------------- | ------------ |
| 의미             | 정의           |
| 언제 나타나는가       | 운영 상황        |
| 화면에서 보는 위치     | 실제 화면        |
| 운영자가 할 수 있는 조치 | 실제 허용 기능     |
| 주의사항           | 재처리·중복 등의 영향 |

---

# 8. 배치 상태 모델

이 장은 매우 중요하다.

## 8.1 Job 상태

실제 Source에서 사용하는 상태 전체를 조사한다.

예:

```
REQUESTED
   ↓
STARTING
   ↓
STARTED
   ↓
COMPLETED

```

실제 구현에 존재한다면 실패·중지 경로도 표현한다.

```
STARTED
 ├─→ FAILED
 ├─→ STOPPING → STOPPED
 └─→ UNKNOWN_RESULT

```

상태를 임의 생성하면 안 된다.

## 8.2 상태별 운영 기준표

| 상태의미정상 지속 시간운영자 조치 가능 여부확인해야 할 것다음 상태 |
| ------------------------------------- |

특히 운영자가 가장 궁금해하는 다음을 명확하게 설명한다.

- STARTING이 오래 지속되는 경우
- STARTED가 오래 지속되는 경우
- STOPPING이 끝나지 않는 경우
- FAILED
- STOPPED
- ABANDONED
- UNKNOWN\_RESULT

---

# 9. ADM 로그인과 배치 메뉴 접근

실제 ADM 기준으로 작성한다.

### 9.1 로그인

### 9.2 필요 Role

### 9.3 필요 Permission

### 9.4 Batch 메뉴 위치

### 9.5 메뉴 접근 실패

### 9.6 Permission 부족

### 9.7 Data Scope 적용 여부

각 화면마다 실제 Route를 함께 표기한다.

```
메뉴
운영 관리
 └─ Batch
     ├─ Job
     ├─ Execution
     ├─ Scheduler
     ├─ Worker
     ├─ Reconciliation
     └─ Audit

```

단, 실제 메뉴에 존재하는 항목만 작성한다.

---

# 10. Job 조회

## 10.1 Job 목록 화면

모든 검색 조건을 전수 작성한다.

| 검색 FieldType기본값필수입력 예의미 |
| ----------------------- |

모든 Column도 작성한다.

| Column의미Source운영 판단 |
| ------------------- |

예:

- Job Name
- Version
- Status
- Artifact
- Last Execution
- Scheduler
- Enabled

실제 화면 기준으로 작성한다.

## 10.2 Job 상세 화면

다음 정보를 빠짐없이 설명한다.

- Job 식별자
- Version
- Description
- Parameter
- Step 구성
- Scheduler 연결
- Artifact
- 실행 가능 여부
- Restart 가능 여부
- 동시 실행 정책
- 승인 정책
- 등록 시각
- 수정 시각

---

# 11. Job 실행

## 11.1 Scheduled Execution

## 11.2 Manual Execution

## 11.3 승인 후 실행

## 11.4 Dry Run

## 11.5 Preview

수동 실행 절차는 다음 수준으로 작성한다.

1. 어느 메뉴에 들어간다.
2. 어떤 Job을 검색한다.
3. 어떤 버튼을 누른다.
4. 어떤 Parameter를 입력한다.
5. Parameter 기본값은 무엇인가.
6. 실행 Reason이 필요한가.
7. Approval이 필요한가.
8. 실행 버튼 활성 조건은 무엇인가.
9. 실행 요청 이후 어떤 상태가 보이는가.
10. 정상 실행을 무엇으로 판단하는가.
11. 실행 요청 응답이 없으면 어떻게 해야 하는가.

---

# 12. JobParameter 운영

별도 장으로 다룬다.

| ParameterType필수기본값허용 범위예재시작 시 변경 가능 여부 |
| -------------------------------------- |

다음 문제를 반드시 설명한다.

- 날짜 Parameter
- 기준일
- From/To
- 중복 실행에 영향을 주는 Parameter
- JobInstance 식별에 사용되는 Parameter
- Restart 시 유지되는 값
- 운영자가 수정할 수 없는 값
- 잘못 입력한 경우의 오류

---

# 13. 실행 이력 조회

검색 기준:

- Job Name
- Execution ID
- 상태
- 실행 요청자
- 시작 시각
- 종료 시각
- Scheduler
- Host
- Worker
- 기준일

모든 Column을 설명한다.

특히 운영자가 **Execution ID를 이후 모든 장애 분석의 기준키로 사용할 수 있게** 작성한다.

---

# 14. 실행 상세

Execution Detail에서 다음 정보를 확인할 수 있도록 설명한다.

### 실행 기본정보

### 실행 Parameter

### Job 상태

### 시작·종료 시각

### 소요시간

### Step 상태

### 처리 건수

### Skip 건수

### Retry 건수

### Commit 건수

### Rollback 건수

### Partition

### Worker

### 오류

### Audit

### Log·Trace 연결

---

# 15. Step 운영

Step마다 다음 표를 제공한다.

| Step방식입력출력정상 상태재시작 영향실패 시 운영 기준 |
| ------------------------------- |

운영자는 최소한 다음을 판단할 수 있어야 한다.

> Job 전체가 실패했는지
> 특정 Step만 실패했는지
> 이전 Step은 Commit됐는지
> Restart하면 어디부터 시작되는지

---

# 16. Chunk와 Commit 이해

운영자에게도 반드시 설명해야 한다.

예:

```
Chunk Size = 100

1~100   → COMMIT
101~200 → COMMIT
201~300 → 처리 중 오류

```

이 경우 Restart·Reprocess 시 어떤 범위가 다시 처리될 수 있는지 실제 CPF 구현 기준으로 설명한다.

특히 다음 용어의 차이를 설명한다.

- 읽은 건수
- 처리 건수
- 기록 건수
- Commit 건수
- Skip 건수
- Rollback 건수

---

# 17. Stop

## 17.1 Stop을 사용할 수 있는 상태

## 17.2 Stop 요청 절차

## 17.3 STOPPING

## 17.4 STOPPED

## 17.5 Stop 요청 이후 Commit

## 17.6 Stop Timeout

## 17.7 Worker 응답 없음

## 17.8 Stop 실패

## 17.9 Stop 이후 Restart

중요:

**Stop 버튼을 누르면 현재 처리 중인 업무가 어느 지점에서 멈추는지**를 설명해야 한다.

---

# 18. Restart

Restart를 단순히 "실패한 Job을 다시 실행"이라고 작성해서는 안 된다.

다음 사항을 모두 설명한다.

### Restart 가능한 상태

### Restart 불가능한 상태

### 기존 JobInstance 관계

### Execution ID 관계

### Parameter 유지

### Checkpoint

### 완료 Step 재실행 여부

### 실패 Step 재개 위치

### 중복 처리 위험

### Restart 결과 확인

그리고 반드시:

> Restart와 New Execution의 차이

를 비교표로 제공한다.

| 항목RestartNew Execution |   |   |
| ---------------------- | - | - |
| 기존 Execution 연계        |   |   |
| Parameter              |   |   |
| Checkpoint             |   |   |
| 완료 Step 처리             |   |   |
| 사용 상황                  |   |   |
| 중복 위험                  |   |   |

---

# 19. Abandon

운영자가 가장 위험하게 사용할 수 있는 기능 중 하나로 취급한다.

### 의미

### 사용 가능한 상태

### 사용 목적

### 사용 후 Restart 가능 여부

### 업무 데이터 영향

### Reason

### Approval

### Audit

### 잘못 사용했을 때 대응

실제 구현에서 지원하지 않으면 `미구현`으로 표시한다.

---

# 20. Retry·Restart·Reprocess 비교

이 표는 반드시 문서 앞쪽에도 요약본을 둔다.

| 기능대상기존 실행 관계주 사용 상황중복 가능성운영 주의 |   |   |   |   |   |
| ------------------------------ | - | - | - | - | - |
| Retry                          |   |   |   |   |   |
| Restart                        |   |   |   |   |   |
| Reprocess                      |   |   |   |   |   |
| New Execution                  |   |   |   |   |   |

운영자는 이 세 기능을 혼동해서는 안 된다.

---

# 21. Reprocess

### 재처리 대상 선정

### 실패 건 조회

### 단건 재처리

### 다건 재처리

### 범위 재처리

### Reason

### Approval

### Preview

### 실행

### 결과 확인

### 실패 건 재확인

### 중복 처리 방지

### Audit

가능하다면 실제 업무키를 기준으로 사례를 제공한다.

```
Execution 10481
전체      10,000
성공       9,997
실패           3

실패 Key
A001
A218
A773

→ 실패 3건만 Reprocess
→ 처리 결과 3건 성공
→ Reconciliation 재실행

```

---

# 22. UNKNOWN\_RESULT

배치 운영 매뉴얼에서 반드시 독립 장으로 다룬다.

예:

```
ADM → 실행 요청
        ↓
Batch Runner → 실행
        ↓
응답 전달 중 Network Timeout
        ↓
ADM은 실행 성공/실패를 판단할 수 없음
        ↓
UNKNOWN_RESULT

```

운영 절차:

1. 동일 Job을 즉시 다시 실행하지 않는다.
2. Execution ID·Request ID·Idempotency Key를 확인한다.
3. 실제 Batch Metadata를 조회한다.
4. Runner 상태를 확인한다.
5. 업무 DB 반영 여부를 확인한다.
6. Attempt Ledger가 있다면 조회한다.
7. Reconciliation을 실행한다.
8. 실행 여부를 판정한다.
9. 필요한 경우 Retry·Restart·Reprocess 중 하나를 선택한다.

실제 CPF 구현과 일치시킨다.

---

# 23. Reconciliation

대사는 운영 결과를 판단하는 핵심 기능으로 작성한다.

### 입력 건수

### 성공 건수

### 실패 건수

### DB 반영 건수

### 대상 업무 건수

### Batch Metadata 건수

예:

```
Target       : 100,000
Read         : 100,000
Write        : 99,998
Skip         : 2
Business DB  : 99,998

```

어떤 조건이면 정상이고 어떤 조건이면 조사 대상인지 실제 기준을 명시한다.

---

# 24. Partition Batch 운영

## 24.1 Partition 구조

```
Job
 ├─ Partition 01 → Worker 1
 ├─ Partition 02 → Worker 2
 ├─ Partition 03 → Worker 3
 └─ Partition 04 → Worker 4

```

## 24.2 Partition별 상태

## 24.3 일부 Partition 실패

## 24.4 Worker 장애

## 24.5 재할당

## 24.6 중복 실행 방지

## 24.7 Partition 재처리

## 24.8 전체 결과 판정

특히:

```
P01 COMPLETED
P02 COMPLETED
P03 FAILED
P04 COMPLETED

```

일 때 운영자가 전체 Job을 새로 실행하는 것이 아니라 **실제 CPF가 허용하는 복구 단위가 무엇인지** 명확히 설명한다.

---

# 25. Center-Cut 운영

Center-Cut을 사용하는 경우 별도 장을 둔다.

### Center-Cut 목적

### 대상 데이터

### 대상 선정 조건

### Partition 기준

### Chunk

### Commit

### Worker 배분

### 실행 순서

### 진행률

### 처리 건수

### 실패 건

### 재처리

### 대사

### 완료 기준

운영 화면에서 전체 대상과 처리 건수를 연결해서 볼 수 있어야 한다.

---

# 26. Scheduler 운영

## 26.1 Scheduler 목록

## 26.2 Schedule 등록

## 26.3 Schedule 변경

## 26.4 활성화

## 26.5 비활성화

## 26.6 Cron

## 26.7 Timezone

## 26.8 Misfire

## 26.9 중복 Trigger

## 26.10 실행 이력

## 26.11 Scheduler와 Execution 연결

운영자가 다음 문제를 구분할 수 있어야 한다.

> Scheduler가 실행을 만들지 못한 것인가?

와

> Scheduler는 실행 요청을 만들었는데 Batch가 실패한 것인가?

---

# 27. Misfire

반드시 사례 중심으로 설명한다.

예:

```
예정 실행 : 02:00
Scheduler 장애 : 01:50~02:20
복구       : 02:20

```

실제 CPF 정책에 따라:

- 즉시 실행
- 건너뜀
- 다음 일정 대기
- 관리자 승인

중 무엇인지 기술한다.

---

# 28. Runner 운영

### Runner 역할

### Runner 목록

### 상태

### 마지막 Heartbeat

### 실행 중 Job

### Version

### Host

### Health

### 실행 실패

### 연결 실패

### 재시작 후 Job 영향

---

# 29. Worker 운영

### Worker 목록

### 상태

### 처리 Partition

### Heartbeat

### Lease

### Claim

### Fencing Token

### Worker Loss

### Lease 만료

### 다른 Worker 재할당

### 중복 처리 판단

운영자가 Lease·Fencing을 코드 수준이 아닌 **운영 판단 수준으로 이해할 수 있게** 설명한다.

---

# 30. Agent 운영

Agent가 실제 존재한다면:

- 역할
- 등록
- 상태
- Version
- Heartbeat
- 실행 요청
- 오류
- Upgrade 관계

를 작성한다.

없으면 작성하지 않는다.

---

# 31. Artifact·Job Pack 운영

### Artifact Version

### Job Version

### 배포된 Version

### 현재 실행 Version

### Checksum

### 등록 일시

### 활성 Version

### Rollback 가능 여부

운영자가:

> 어제 정상 실행된 Job과 오늘 실패한 Job의 Artifact Version이 같은가?

를 스스로 확인할 수 있어야 한다.

---

# 32. 승인

위험 조치에 Approval이 존재한다면 별도 장으로 작성한다.

### 승인 대상

### 요청자

### 승인자

### 승인 상태

### 승인 만료

### 승인 후 변경 금지 대상

### 실행

### 반려

### 취소

---

# 33. Reason

다음 조치에는 구현상 요구되는 Reason 여부를 조사한다.

- 수동 실행
- Stop
- Restart
- Abandon
- Reprocess
- Scheduler 변경
- 강제 상태 변경

Reason이 Audit에 어떻게 저장되는지 설명한다.

---

# 34. Expected Version과 동시성

운영 화면에서 Optimistic Lock을 사용한다면 운영자 매뉴얼에서도 설명한다.

예:

```
운영자 A 화면 조회 version=4
운영자 B 상태 변경 → version=5
운영자 A가 version=4로 변경 요청
→ Conflict

```

이때 운영자가:

1. 새로 조회한다.
2. 현재 상태를 확인한다.
3. 기존 변경자를 확인한다.
4. 다시 조치할 필요가 있는지 판단한다.

라는 절차를 알 수 있어야 한다.

---

# 35. 중복 실행

반드시 별도 장으로 둔다.

### 같은 Job 동시 실행

### 같은 Parameter 동시 실행

### 같은 업무 기준일 중복 실행

### Idempotency

### Duplicate Request

### Duplicate Execution

### 중복 데이터 반영 여부

### 운영자가 판정하는 방법

---

# 36. 진행률

`50%` 같은 숫자가 무엇을 의미하는지를 설명한다.

예:

```
전체 대상 1,000,000
처리 완료   400,000
진행률          40%

```

그러나 전체 대상이 사전에 확정되지 않는 Job이라면 그 차이를 명확히 적는다.

---

# 37. Log 확인

운영자가 Source 분석 없이 사용할 수 있도록 작성한다.

| 로그확인 목적검색 Key정상 예오류 예 |
| --------------------- |

검색 Key:

- Execution ID
- Job Name
- Step
- Request ID
- Trace ID
- Worker ID
- Partition ID

---

# 38. Metric 확인

실제 Metric만 작성한다.

예를 들어 실제 제공하는 경우:

- 실행 성공/실패 수
- Job Duration
- Processing Rate
- Retry Count
- Worker Count
- Queue Depth
- Lease Failure

각 Metric에 정상 범위를 임의로 만들지 않는다.

---

# 39. Trace 확인

분산 실행을 지원하는 경우:

```
ADM
 → Batch Control
 → Runner
 → Worker
 → DB/External System

```

Trace ID를 기준으로 어디까지 요청이 도달했는지 확인하는 방법을 작성한다.

---

# 40. Audit

운영 조치별 Audit 여부를 전수 정리한다.

| 운영 조치사용자ReasonBeforeAfter실행 ID기록 여부 |
| ----------------------------------- |

---

# 41. 권한

실제 Permission 전수표를 둔다.

| 기능조회 Permission실행 Permission승인 PermissionData Scope |
| --------------------------------------------------- |

Permission 이름은 Source에 존재하는 실제 문자열만 사용한다.

---

# 42. 운영 시나리오

최소 다음 시나리오를 독립 절로 만든다.

### 정상 정기 실행

### 수동 실행

### Job 실패

### Step 실패

### Partition 일부 실패

### Worker Loss

### Stop

### Stop 실패

### Restart

### Reprocess

### Scheduler Misfire

### 실행 중복

### DB Timeout

### DB Deadlock

### 외부 시스템 Timeout

### Network Timeout

### UNKNOWN\_RESULT

### 응답 유실

### ADM Timeout

### 처리 건수 불일치

### Artifact Version 불일치

---

# 43. 상황별 판단표

운영자가 가장 많이 사용할 표다.

| 증상가장 먼저 확인가능한 원인다음 확인가능한 조치 |                   |              |               |                      |
| --------------------------- | ----------------- | ------------ | ------------- | -------------------- |
| Job이 시작되지 않음                | Scheduler History | Trigger 미발생  | Scheduler 상태  | 실제 허용 조치             |
| STARTING 지속                 | Runner            | Runner 응답 없음 | Log           | 실제 허용 조치             |
| STARTED 지속                  | Step 진행           | 장기 처리/정지     | Metric        | 실제 허용 조치             |
| FAILED                      | Step Error        | 업무/DB 오류     | Error Detail  | Restart/Reprocess 여부 |
| STOPPING 지속                 | Worker            | Stop 미응답     | Worker Health | 실제 허용 조치             |
| 건수 불일치                      | Reconciliation    | 일부 실패        | 업무 DB         | Reprocess 여부         |
| 결과 모름                       | Attempt/Metadata  | 응답 유실        | Reconcile     | UNKNOWN\_RESULT 절차   |

---

# 44. 실행하면 안 되는 상황

운영 매뉴얼에는 **하지 말아야 할 행동**도 명시해야 한다.

예:

- UNKNOWN\_RESULT 상태에서 확인 없이 동일 Job 재실행
- RUNNING Job에 중복 Manual Execute
- Restart 가능 여부 확인 없이 New Execution
- 처리 건수 대사 없이 완료 판정
- 실패 원인 확인 없이 반복 Retry
- Worker 장애 상황에서 전체 Job을 무조건 다시 실행

다만 실제 CPF 동작을 조사해 정확한 제약만 작성한다.

---

# 45. 운영 인계 기준

배치가 개발팀에서 운영팀으로 넘어올 때 필요한 정보도 포함한다.

| 항목필수 내용   |   |
| --------- | - |
| Job Name  |   |
| 목적        |   |
| Owner     |   |
| Scheduler |   |
| 실행 시간     |   |
| 예상 처리량    |   |
| 정상 소요시간   |   |
| Parameter |   |
| 재시작 정책    |   |
| 중복 실행 정책  |   |
| 실패 처리     |   |
| Reprocess |   |
| 대사 기준     |   |
| Alert     |   |
| 담당 개발팀    |   |

---

# 46. 일일 운영 절차

매일 운영자가 확인할 사항을 절차로 제공한다.

### 업무 시작 전

- Scheduler 상태
- 전일 미완료 Job
- FAILED
- UNKNOWN\_RESULT
- 장기 실행 Job
- Worker 상태

### 업무 중

- 중요 Batch 진행상태
- 실패 알림
- 처리 지연
- Reconciliation

### 업무 종료 전

- 미완료 실행
- 실패 실행
- 재처리 잔여 건
- UNKNOWN\_RESULT
- 대사 불일치

단순 체크리스트가 아니라 각각 **어느 화면에서 무엇을 보고 정상 여부를 판단하는지** 연결한다.

---

# 47. 정기 운영

## 일간

## 주간

## 월간

실제 CPF 운영 정책에 존재하는 항목만 작성한다.

---

# 48. 화면 작성 표준

모든 Batch ADM 화면은 같은 형식으로 작성한다.

## 화면명

**목적**

**Route**

**필요 Permission**

**접근 경로**

**검색 Field**

| FieldType기본값필수설명 |
| ---------------- |

**목록 Column**

| Column의미운영 판단 |
| ------------- |

**Button**

| Button활성 조건입력값결과 상태승인Audit |
| -------------------------- |

**상세 Field**

| Field의미Source |
| ------------- |

**정상 절차**

**실패 절차**

**응답 유실**

**동시 변경**

**복구·재처리·대사**

**Audit 확인**

---

# 49. 작업 절차 작성 표준

모든 운영 절차는 최소 다음 형식으로 통일한다.

### 목적

### 사용 권한

### 선행 조건

### 입력값

### 실행 전 확인

### 실행 절차

### 정상 결과

### 상태 변화

```
BEFORE
 ↓
REQUESTED
 ↓
PROCESSING
 ↓
AFTER

```

### Log

### Metric

### Audit

### 실패 가능한 지점

### Retry 가능 여부

### Restart 가능 여부

### Reprocess 가능 여부

### Reconciliation

### Rollback 또는 Compensation

### 개발팀 이관 조건

---

# 50. 사례 중심 설명

기능 설명만 적지 말고 실제 운영 사례를 제공한다.

## 사례: 새벽 정산 Batch 일부 실패

```
Job        : DAILY_SETTLEMENT
Execution  : 18422
Target     : 120,000
Success    : 119,998
Failed     : 2
Status     : FAILED

```

운영자는 다음 순서로 처리하도록 설명한다.

```
Execution 확인
    ↓
실패 Step 확인
    ↓
실패 업무 Key 확인
    ↓
Commit 범위 확인
    ↓
Restart/Reprocess 선택
    ↓
재처리
    ↓
Reconciliation
    ↓
최종 상태 확인
    ↓
Audit 확인

```

이런 사례를 여러 개 제공해야 한다.

---

# 51. 운영자용 명령어·API Summary

화면만 제공하면 안 된다.

운영 환경에서 공식적으로 사용 가능한 CLI·Script·API가 있다면 전수 정리한다.

| 목적화면CLI/명령API필요 권한 |   |   |   |   |
| ------------------ | - | - | - | - |
| Job 조회             |   |   |   |   |
| 실행 조회              |   |   |   |   |
| 실행                 |   |   |   |   |
| Stop               |   |   |   |   |
| Restart            |   |   |   |   |
| Health             |   |   |   |   |

실제 지원하지 않는 접근 방법은 만들지 않는다.

---

# 52. 오류 메시지 Catalog

실제 코드의 Error Code와 메시지를 수집한다.

| Error Code발생 조건운영 의미운영자 조치개발자 확인 필요 |
| ----------------------------------- |

운영자가 오류 문구를 검색해 바로 대응 절로 이동할 수 있게 한다.

---

# 53. 운영 상태별 빠른 참조표

문서 앞·뒤에서 모두 찾을 수 있게 한다.

| 상태재실행RestartStopReprocessReconcile |
| ---------------------------------- |

실제 CPF 정책을 조사한 후 채운다.

---

# 54. EDU / 운영 실습

배치 운영 매뉴얼에도 실습 과정이 필요하다.

## 실습 1 — Job 조회

## 실습 2 — 정상 수동 실행

## 실습 3 — Parameter 실행

## 실습 4 — 실패 실행 확인

## 실습 5 — Restart

## 실습 6 — Stop

## 실습 7 — 실패 건 Reprocess

## 실습 8 — Partition 일부 실패

## 실습 9 — Scheduler Misfire

## 실습 10 — UNKNOWN\_RESULT

## 실습 11 — Reconciliation

## 실습 12 — Audit 확인

각 실습에는 반드시 다음이 포함된다.

- 선행 조건
- 사용할 Job
- 준비 데이터
- 화면 위치
- 입력값
- 실행 절차
- 예상 상태
- 정상 결과
- 의도적으로 오류를 만드는 방법
- 오류 확인
- 정상화 절차
- ADM 확인
- DB 확인
- Log 확인
- Test 여부
- 실습 이후 원복

---

# 55. 문서 마지막 Quick Reference

운영 중 가장 많이 보는 정보를 2\~4페이지 정도에 압축한다.

## 상태표

## 조치 선택표

## Permission표

## 주요 메뉴

## 검색 Key

## 주요 Error Code

## Restart/Reprocess 판단

## UNKNOWN\_RESULT 절차

## 장애 유형별 첫 확인 위치

예:

```
FAILED
  ↓
Step 확인
  ↓
Commit 여부 확인
  ↓
Restart 가능?
 ├─ YES → Restart
 └─ NO
      ↓
 Reprocess 가능?
 ├─ YES → Reprocess
 └─ NO → 개발 담당 확인

```

---

# 56. 반드시 포함해야 할 핵심 표 목록

배치 운영 매뉴얼에는 최소 다음 표가 필요하다.

1. 운영 기능 Summary
2. 사용자 질문별 바로가기
3. Batch 구성요소 역할표
4. 용어표
5. Job 상태표
6. Step 상태표
7. 상태 전이표
8. Job 목록 Column
9. Job 상세 Field
10. Execution 목록 Column
11. Execution 상세 Field
12. JobParameter
13. Step 실행 결과
14. 처리 건수
15. Retry·Restart·Reprocess 비교
16. Scheduler
17. Misfire 정책
18. Partition
19. Worker
20. Runner
21. Lease·Claim·Fencing
22. Artifact Version
23. Permission
24. Approval
25. Reason
26. Audit
27. Error Code
28. Log 검색 Key
29. Metric
30. 상태별 가능한 조치
31. 상황별 첫 확인 위치
32. 운영 인계 정보
33. 일일 운영 확인 항목
34. 운영 명령어/API Summary
35. 미구현·미검증 항목

---

# 57. 문서에서 특히 피해야 할 설명

다음과 같은 문장만으로 끝내면 안 된다.

> 배치 실행을 관리합니다.

대신:

> `Batch > Execution`에서 Job Name과 실행일을 입력하여 실행 이력을 조회한다. `FAILED` 상태의 Execution을 선택하면 실패 Step과 오류 정보를 확인할 수 있다. Restart 가능 여부는 해당 Execution 상태와 Job의 restartable 설정을 기준으로 결정하며, Restart 버튼의 활성 조건과 재개 위치는 §XX를 따른다.

와 같이 작성한다.

또한:

> 실패하면 재실행합니다.

라고 쓰지 않는다.

반드시 **Restart인지 New Execution인지 Reprocess인지** 구분한다.

---

# 58. 운영 매뉴얼의 핵심 작성 원칙

배치 운영 매뉴얼은 기능 목록 문서가 아니라 다음 흐름으로 작성한다.

```
현재 상태 확인
      ↓
문제 위치 식별
      ↓
실행 결과 확인
      ↓
조치 가능 여부 판단
      ↓
Stop / Restart / Retry / Reprocess / Reconcile
      ↓
업무 데이터 확인
      ↓
정상화 판정
      ↓
Audit 확인

```

그리고 모든 중요한 기능에서 운영자가 최종적으로 다음 네 가지를 판단할 수 있어야 한다.

### 1. 지금 무슨 일이 발생했는가

### 2. 어디까지 처리되었는가

### 3. 내가 어떤 조치를 할 수 있는가

### 4. 조치 후 무엇을 확인해야 정상으로 판단할 수 있는가

이 네 질문에 답하지 못하는 설명은 운영 매뉴얼의 완료 기준으로 보지 않는다.

---

# 59. CPF 문서 체계에서의 배치 운영 매뉴얼 위치

배치 운영은 일반 플랫폼 운영과 성격이 다르므로 **독립적인 실무 범위 자체는 충분히 존재한다.**

특히 다음 기능 때문에 개발자 매뉴얼만으로 대체하기 어렵다.

```
실행
상태 조회
Stop
Restart
Abandon
Retry
Reprocess
UNKNOWN_RESULT
Reconciliation
Scheduler / Misfire
Partition
Runner / Worker / Agent
Lease / Claim / Fencing
Center-Cut
Approval / Reason / Audit

```

다만 현재 CPF 공식 사용자 문서를 제한해서 관리한다면 신규 파일을 하나 더 만드는 대신 **`04_ADM운영자매뉴얼.md`** **안에 「배치 운영」을 독립 대목차 수준으로 구성하고,** **`02_배치개발매뉴얼.md`****에서는 개발 관점만 담당하도록 분리하는 방식**이 문서 수를 늘리지 않으면서 가장 자연스럽다.

플랫폼 설치·프로세스·DB·Kafka·인프라 문제는 `05_플랫폼운영매뉴얼.md`가 담당하고, 배치 업무 실행과 상태·재처리·대사는 ADM 운영 영역이 담당하도록 경계를 잡는다.

즉 권장 책임 구분은 다음과 같다.

```
02 배치개발자매뉴얼
 └─ 어떻게 배치를 만드는가

04 ADM운영자매뉴얼
 └─ 만들어진 배치를 어떻게 운영하는가
     └─ Batch Operations

05 플랫폼운영매뉴얼
 └─ Batch가 실행되는 플랫폼을 어떻게 운영하는가

```

이렇게 구성하면 별도 문서를 추가하지 않으면서도 **배치 운영 매뉴얼 한 권에 해당하는 깊이를** **`04`** **내부 독립 파트로 확보**할 수 있다.