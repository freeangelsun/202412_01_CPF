# CPF 배치 개발자 매뉴얼 작성 작업 지침

## 실무 배치 개발자 관점 보완 규칙 (2026-08-16)

- 배치 Framework 내부 구현보다 **신규 업무 Job을 작성·테스트·실행하는 개발자**의 작업 순서를 우선한다.
- `Job 유형 선택 → Job/Step 표준 구조 → Tasklet/Chunk → Reader/Processor/Writer → Parameter → Transaction/Checkpoint → Retry/Skip/Restart/Reprocess → Partition/Worker → Scheduler → Center-Cut → Test/운영 인계` 흐름으로 구성한다.
- 각 기능군에는 `용도`, `사용 Annotation/API`, `주요 옵션`, `선택 기준`, `재시작/Transaction 영향`, `최소 예제`를 제공한다. 내부 Runtime 객체 목록이나 운영용 Artifact 정보는 실제 개발 시점에 필요한 절로 후순위 배치한다.
- Batch 실행 명령은 개발 Shell과 운영 Command를 혼합하지 않는다. 로컬 Runtime 기동/빌드/Test/검증은 개발 명령으로, Job 실행/Stop/Restart/Reprocess는 실제 ADM/운영 경로로 분리한다.
- Worker/Agent/Center-Cut은 단순 Component 설명이 아니라 **업무 증가 시 Worker/Agent 확장, 실행 제어와 처리 분리, 실패/재처리/대사** 같은 사용자 관점의 효과가 이해되게 시각화한다.

## 1. 문서 목적

본 지침은 `cpf-docs/guides/02_배치개발매뉴얼.md`를 작성·개정·검수하기 위한 기준을 정의한다.

CPF 배치 개발자 매뉴얼은 Spring Batch 개념을 설명하는 교재만을 목적으로 하지 않는다.

CPF를 처음 접한 Java 개발자가 Framework Source를 역분석하거나 Framework 담당자에게 별도 설명을 요청하지 않고 다음 업무를 수행할 수 있어야 한다.

1. CPF의 배치 실행 구조를 이해한다.
2. 배치 Job 유형을 선택한다.
3. 신규 Job을 생성한다.
4. Job과 Step을 구성한다.
5. Tasklet 또는 Chunk 방식을 선택한다.
6. Reader·Processor·Writer를 구현한다.
7. JobParameter를 정의한다.
8. Spring Batch Metadata와 CPF 관리 Metadata의 관계를 이해한다.
9. Transaction·Chunk·Commit 단위를 결정한다.
10. 실패 시 재시작 가능한 Job을 설계한다.
11. Checkpoint와 Restart 지점을 설계한다.
12. Stop·Restart·Abandon의 차이를 이해하고 적용한다.
13. 대용량 배치를 Partition 처리한다.
14. Local Partition과 Remote Worker 방식을 구분한다.
15. Runner·Worker·Agent의 역할을 이해한다.
16. Job 등록과 Version을 관리한다.
17. Scheduler에 Job을 등록한다.
18. Schedule 변경과 Misfire를 처리한다.
19. Center-Cut 유형의 대량 처리 Job을 개발한다.
20. Dry Run과 건수 Preview를 제공한다.
21. Lease·Claim·Fencing을 이용하는 분산 실행 구조가 있다면 이를 정확하게 적용한다.
22. 실행 요청·승인·실행·진행·중지 등 Job Lifecycle을 이해한다.
23. 실패 Job을 Restart 또는 Reprocess한다.
24. 처리 결과를 Reconcile한다.
25. 응답 유실이나 실행 결과 불명 상태에서 `UNKNOWN_RESULT`를 판단한다.
26. Batch Artifact 또는 Job Pack을 생성한다.
27. 배포 담당자에게 배치 실행 요건을 인계한다.
28. ADM에서 Job 상태·Step 상태·실패·재처리 결과를 확인한다.
29. 정상·오류·중단·재시작·동시 실행·장애 Test를 수행한다.

---

# 2. 완료 판단 기준

다음 질문을 배치 개발자가 Source 분석 없이 매뉴얼에서 찾을 수 있어야 한다.

> 신규 배치는 어디에 만드는가?

> Tasklet과 Chunk 중 무엇을 선택해야 하는가?

> Reader·Processor·Writer는 어떤 CPF API를 사용하는가?

> JobParameter는 어디서 정의하고 어떻게 전달하는가?

> 같은 Job을 같은 Parameter로 다시 실행할 수 있는가?

> JobInstance와 JobExecution은 어떻게 달라지는가?

> 몇 건마다 Commit되는가?

> 한 Chunk 중 일부 Writer가 실패하면 어디까지 Rollback되는가?

> 재실행하면 처음부터 다시 처리하는가?

> 어디부터 Restart되는가?

> 처리한 레코드를 중복 처리하지 않는가?

> DB Commit 후 외부 시스템 호출 결과를 받지 못하면 어떻게 되는가?

> Stop과 Kill은 무엇이 다른가?

> Restart와 Reprocess는 무엇이 다른가?

> Abandon하면 다시 실행할 수 있는가?

> Partition 수는 어떻게 결정하는가?

> Worker 하나가 죽으면 해당 Partition은 어떻게 되는가?

> 두 Instance가 같은 Partition을 동시에 처리하지 않는가?

> Scheduler가 실행 시각을 놓치면 어떻게 되는가?

> Center-Cut에서 대상 건수와 실제 처리 건수가 다르면 무엇을 확인하는가?

> 실행 전에 몇 건이 처리될지 Preview할 수 있는가?

> 실패 Job은 ADM에서 어디서 확인하는가?

> 운영자가 재실행해도 되는 Job인지 어떻게 판단하는가?

이 질문의 중요한 부분을 문서가 답하지 못한다면 해당 범위는 완료로 판단하지 않는다.

---

# 3. 사실성 원칙

다음 항목은 실제 CPF Repository를 확인하여 작성한다.

- Spring Batch Version
- Job
- Step
- Tasklet
- ItemReader
- ItemProcessor
- ItemWriter
- Listener
- JobParameter
- ExecutionContext
- JobRepository
- JobLauncher
- JobOperator
- Batch Metadata Table
- CPF Batch 관리 Table
- Runner
- Worker
- Agent
- Scheduler
- Job Registry
- Partition
- Lease
- Claim
- Fencing
- Center-Cut
- Artifact
- Job Pack
- Command
- API
- Property
- Environment Variable
- SQL
- Migration
- ADM Route
- Permission
- Batch Status
- Exit Status
- Error Code
- Test

Source에 존재하지 않는 배치 기능을 일반 Spring Batch 기능이라는 이유만으로 CPF 기능처럼 작성하지 않는다.

---

# 4. 문서 전체 구조

배치 개발자 매뉴얼은 다음 6개 탐색 계층을 가진다.

## 4.1 Quick Finder

개발자가 하려는 배치 작업을 기준으로 필요한 기능을 찾는다.

## 4.2 Batch Feature Summary

CPF에서 사용할 수 있는 Job 유형·API·Command·Annotation·명령·실행 유형을 한눈에 파악한다.

## 4.3 Tutorial

신규 배치 Job 하나를 처음부터 끝까지 구현한다.

## 4.4 기능별 Guide

Job, Step, Chunk, Partition, Scheduler 등 각 기능의 상세 개발법을 설명한다.

## 4.5 Cookbook

“대량 데이터를 병렬 처리하고 싶다”와 같은 문제 중심으로 설명한다.

## 4.6 Reference

API·Class·Property·JobParameter·Command·상태·Error Code를 빠르게 검색한다.

---

# 5. 문서 앞부분 필수 Summary

배치 개발자가 문서를 처음 열었을 때 다음 Summary를 먼저 볼 수 있도록 한다.

1. 배치 개발 작업 Quick Finder
2. CPF Batch Architecture Map
3. 지원 Batch 유형 Summary
4. Job / Step 유형 Summary
5. Tasklet / Chunk 선택표
6. Reader / Processor / Writer Summary
7. Batch Public API Summary
8. Batch Command Summary
9. Batch Annotation / SPI Summary
10. 배치 개발 명령어 Summary
11. JobParameter Summary
12. Transaction / Chunk / Commit Summary
13. JobInstance / Execution / Metadata Summary
14. Stop / Restart / Abandon Summary
15. Partition / Parallel Processing Summary
16. Runner / Worker / Agent Summary
17. Lease / Claim / Fencing Summary
18. Scheduler / Misfire Summary
19. Center-Cut Summary
20. Dry Run / Preview Summary
21. Failure / Recovery Summary
22. `UNKNOWN_RESULT` Summary
23. ADM 확인 Summary
24. Source Navigation Map

---

# 6. 배치 개발 작업 Quick Finder

가장 먼저 배치 개발자의 목적을 기준으로 구성한다.

| 내가 하려는 일사용할 방식시작할 CPF 기능관련 API/Command관련 명령중요 고려사항상세 |           |                         |            |            |                |   |
| ---------------------------------------------------- | --------- | ----------------------- | ---------- | ---------- | -------------- | - |
| 신규 Job 생성                                            | Job 등록    | 실제 Generator/등록 방식      | 실제 API     | 실제 명령      | Job ID/Version | § |
| 단순 일괄 처리                                             | Tasklet   | 실제 API                  | 실제 Command | Test 명령    | Restart 가능성    | § |
| 대량 데이터 처리                                            | Chunk     | Reader/Processor/Writer | 실제 API     | Test 명령    | Chunk Size/TX  | § |
| 수백만 건 처리                                             | Partition | Partition API           | 실제 Command | 실행 명령      | Worker/Lease   | § |
| 실패 후 이어서 실행                                          | Restart   | Checkpoint              | 실제 API     | Restart 명령 | JobParameter   | § |
| 특정 건만 다시 처리                                          | Reprocess | 실제 재처리 방식               | 실제 Command | 실제 명령      | 중복 처리          | § |
| 예약 실행                                                | Scheduler | Schedule 등록             | 실제 API     | 등록 명령      | Misfire        | § |
| 대상 건수 미리 확인                                          | Preview   | Dry Run/Count           | 실제 API     | 실제 명령      | 실제 실행과 조건 일치   | § |
| 처리 결과 대사                                             | Reconcile | Reconciliation          | 실제 Command | 실제 명령      | 기준 건수          | § |

이 표 한 장만으로 개발자가 먼저 어느 장을 봐야 하는지 판단할 수 있게 한다.

---

# 7. 지원 Batch 유형 Summary

CPF에서 실제 사용하는 Batch 유형을 전수 정리한다.

| 개발 목적Batch 유형처리 모델사용 기준TX 방식RestartPartitionScheduler대표 예상세 |
| ----------------------------------------------------------- |

예를 들어 실제 구현에 존재하는 범위에서:

- Tasklet
- Chunk
- Partition
- Remote Worker
- Center-Cut
- Scheduled Job
- Manual Job
- Recovery/Reprocess Job

등을 구분한다.

---

# 8. Tasklet / Chunk 선택표

초기 개발자가 가장 먼저 판단해야 하는 영역이다.

| 상황TaskletChunk권장 이유주의 |       |       |                 |                 |
| --------------------- | ----- | ----- | --------------- | --------------- |
| 한 번의 SQL 실행           | 적합 여부 | 적합 여부 | 실제 CPF 기준       | Restart 의미      |
| 파일 이동                 | 실제    | 실제    | ...             | 부분 완료           |
| 다수 레코드 변환             | 실제    | 실제    | Chunk 단위 처리     | Commit Size     |
| 수백만 건 처리              | 실제    | 실제    | Partition 추가 검토 | Memory          |
| 외부 API 반복 호출          | 실제 정책 | 실제 정책 | Idempotency 필요  | UNKNOWN\_RESULT |

단순히 “대량이면 Chunk”라고 작성하지 않는다.

다음 질문을 답하도록 한다.

- 데이터 양
- Transaction 단위
- Restart 요구
- Retry 요구
- Item 단위 실패 처리
- Checkpoint
- 외부 연계
- Memory
- 병렬화

---

# 9. Job / Step Summary

| Job목적StepStep 유형ReaderProcessorWriterJobParameterRestartScheduler상세 |
| ------------------------------------------------------------------- |

Job 명칭만 나열하지 않고 Job이 실제로 무엇을 처리하는지 표시한다.

---

# 10. Job 상세 작성 기준

각 Job에는 다음을 반드시 작성한다.

1. Job 목적
2. 업무 Owner
3. 실제 Job Name
4. Job Version
5. 실행 주체
6. 자동/수동 실행 여부
7. Scheduler
8. JobParameter
9. Step 목록
10. Step 실행 순서
11. 분기 조건
12. Transaction
13. 예상 대상 건수
14. Commit 단위
15. Restart 가능 여부
16. Reprocess 가능 여부
17. Stop 가능 여부
18. Abandon 의미
19. 동시 실행 허용 여부
20. 외부 연계
21. Output
22. Audit
23. Log/Metric
24. ADM 확인
25. 실패 및 복구
26. Test

---

# 11. JobParameter Summary

JobParameter는 별도 핵심 Reference로 작성한다.

| JobParameterType필수Default식별 ParameterValidation예제재실행 영향 |
| ------------------------------------------------------- |

각 Parameter에는 다음을 작성한다.

- Parameter 이름
- Type
- Format
- 필수 여부
- Default
- 허용 범위
- 날짜 Timezone
- 식별 Parameter 여부
- JobInstance 생성에 미치는 영향
- 동일 Parameter 재실행 결과
- ADM 입력 위치
- CLI/API 입력 방법
- 오류 결과

---

# 12. JobInstance / JobExecution / StepExecution 설명

Spring Batch Metadata 용어를 개발자가 실제 상황에 연결해서 이해하도록 설명한다.

반드시 다음 관계를 보여준다.

`Job + Identifying JobParameter`

→ `JobInstance`

→ 실행할 때마다 `JobExecution`

→ 각 Step마다 `StepExecution`

별도 표:

| 개념생성 기준재사용 여부주요 상태어디서 확인Restart 관계 |
| ---------------------------------- |

개발자가 다음을 판단할 수 있어야 한다.

> 같은 Job을 같은 Parameter로 또 실행하면 새 Job인가?

> 실패한 Job을 Restart하면 새 JobInstance인가?

> JobExecution은 새로 생성되는가?

---

# 13. Spring Batch Metadata Table Reference

실제 사용하는 Metadata Table을 정리한다.

| Table목적주요 Key개발자가 직접 수정ADM 사용 여부주의 |
| ---------------------------------- |

직접 SQL UPDATE로 Batch Metadata를 수정하는 행위를 허용하는지 여부를 명확하게 작성한다.

---

# 14. Step Summary

| StepJob유형입력출력TXCommitRestartListener다음 Step |
| ------------------------------------------- |

각 Step 상세:

- Step 목적
- Tasklet/Chunk
- Reader
- Processor
- Writer
- Transaction
- Chunk Size
- Skip
- Retry
- Restart
- ExecutionContext
- 상태
- 다음 Step 결정 조건

---

# 15. Reader Summary

| 데이터 SourceReader입력Paging/CursorFetch SizeRestart 지원State 저장TX 관계상세 |
| ------------------------------------------------------------------ |

Reader 상세에는:

- DB/File/API 등 Source
- 조회 Query
- 정렬 기준
- Paging/Cursor
- Fetch Size
- Reader State
- ExecutionContext
- Restart 시 위치
- 데이터가 실행 중 변경될 때 영향
- Duplicate/누락 가능성
- Partition과 함께 사용 시 범위

를 작성한다.

---

# 16. Processor Summary

| Processor입력출력Filter 가능Validation외부 호출Retry부작용 여부상세 |
| -------------------------------------------------- |

Processor는 다음을 명확히 한다.

- Processor가 DB를 변경해도 되는가
- 외부 API를 호출해도 되는가
- `null` 반환 의미
- Business Validation 위치
- Retry 시 같은 Item을 다시 처리해도 되는가
- Side Effect를 허용하는가

---

# 17. Writer Summary

| Writer대상Batch SizeTXUpsertIdempotency오류 시 RollbackRestart 영향상세 |
| -------------------------------------------------------------- |

Writer에는 반드시 다음을 설명한다.

- 한 Chunk가 Writer에 어떻게 전달되는가
- 일부 Item만 성공할 수 있는가
- Writer 중간 실패 시 전체 Chunk가 Rollback되는가
- DB Batch Update 여부
- 외부 API Writer인 경우 결과 유실 가능성
- 중복 Writer 방지
- Restart 시 동일 Item 재처리 가능성

---

# 18. Chunk / Transaction / Commit Summary

| 항목실제 CPF 기준설정 위치변경 영향주의 |           |               |           |             |
| ----------------------- | --------- | ------------- | --------- | ----------- |
| Chunk Size              | 실제 값/Job별 | Config/Source | Commit 주기 | Memory/Lock |
| Transaction             | 실제 방식     | Step          | Chunk 단위  | 외부 연계       |
| Retry                   | 실제        | 실제 위치         | 재호출       | Idempotency |
| Skip                    | 실제        | 실제 위치         | 일부 데이터 제외 | 대사          |

반드시 다음 흐름을 설명한다.

`Read N건`

→ `Process N건`

→ `Write N건`

→ `Commit`

그리고 7번째 Item에서 실패했을 때 1\~6번 Item이 어떻게 되는지를 실제 정책으로 설명한다.

---

# 19. Chunk Size 선택 기준

| 처리 특성작은 Chunk큰 Chunk선택 시 고려 |       |          |         |
| --------------------------- | ----- | -------- | ------- |
| DB Commit 빈도                | 증가    | 감소       | DB 부하   |
| Rollback 범위                 | 작음    | 큼        | 재처리 비용  |
| 메모리                         | 작음    | 큼        | Item 크기 |
| Lock 유지                     | 짧음    | 길어질 수 있음 | 동시 업무   |
| 처리 성능                       | 환경 의존 | 환경 의존    | 실제 측정   |

숫자를 근거 없이 권장하지 않는다.

실제 성능 Test 또는 기본 Config가 있으면 근거를 표시한다.

---

# 20. Checkpoint / ExecutionContext 작성 기준

Restart 가능한 Job은 다음을 설명한다.

- 무엇을 Checkpoint로 저장하는가
- 저장 시점
- 저장 위치
- StepExecutionContext
- JobExecutionContext
- Reader State
- Partition State
- Restart 시 어느 위치부터 이어지는가
- 재시작 중 데이터가 변경되면 어떻게 되는가

---

# 21. Restart / Reprocess / Retry 차이

매우 중요한 선택표로 제공한다.

| 기능대상의미같은 JobExecution?데이터 범위사용 조건중복 위험 |                |                    |                    |       |                |       |
| -------------------------------------- | -------------- | ------------------ | ------------------ | ----- | -------------- | ----- |
| Retry                                  | Item/Operation | 같은 처리 재시도          | 실제                 | 작은 범위 | 일시 오류          | 있음    |
| Restart                                | 실패 Job/Step    | Checkpoint부터 이어 실행 | 실제 Spring Batch 의미 | 기존 대상 | Restartable    | 조건    |
| Reprocess                              | 업무 건           | 선택 대상 다시 처리        | 별도 실행 가능           | 지정 대상 | 업무 정책          | 높음    |
| Reconcile                              | 결과 대사          | 상태 차이 확인/보정        | 별도                 | 불일치 건 | 결과 확인          | 낮음/정책 |
| Rerun                                  | 전체 Job         | 전체 재실행             | 실제                 | 전체 대상 | Idempotency 필요 | 높음    |

용어를 혼용하지 않는다.

---

# 22. Stop / Restart / Abandon Summary

| 조치의미현재 Chunk다음 실행운영 위험사용 조건 |                     |            |                    |       |             |
| --------------------------- | ------------------- | ---------- | ------------------ | ----- | ----------- |
| Stop                        | 정상 중지 요청            | 실제 동작      | Restart 가능 여부      | 처리 지연 | 실제 정책       |
| Restart                     | 실패/중지 이어 실행         | Checkpoint | 이어 실행              | 중복 확인 | Restartable |
| Abandon                     | 더 이상 Restart 대상이 아님 | -          | 실제 Spring Batch 동작 | 복구 제한 | 명확한 승인      |

`Abandon`은 단순 “취소”로 설명하지 않는다.

---

# 23. Skip / Retry 정책

실제 CPF에서 사용하는 경우 다음 표를 작성한다.

| 오류 유형Retry최대 횟수SkipSkip LimitRollback최종 상태 |
| ------------------------------------------ |

Retry/Skip 정책을 모든 Exception에 일괄 적용하지 않는다.

다음 범주를 구분한다.

- 데이터 오류
- 일시적 DB 오류
- Deadlock
- Network Timeout
- Business Validation
- Permission
- 외부 API 결과 불명

---

# 24. Batch Error Catalog

| Error Code발생 위치발생 조건Job StatusStep StatusRetryRestartReprocess운영 조치 |
| ------------------------------------------------------------------- |

---

# 25. Batch 상태 Catalog

Spring Batch Status와 CPF 추가 상태가 있다면 명확히 분리한다.

| 상태Source의미진입 조건다음 상태Restart 가능ADM 표시 |
| ------------------------------------ |

예를 들어 실제 존재하는 범위에서:

- STARTING
- STARTED
- STOPPING
- STOPPED
- COMPLETED
- FAILED
- ABANDONED
- UNKNOWN

CPF 추가 상태가 있다면 별도로 표시한다.

---

# 26. 상태 전이표

| 현재 상태이벤트/명령다음 상태처리 중 ItemMetadata 변화Audit운영 조치 |
| ---------------------------------------------- |

---

# 27. Job 등록 / Version 관리

신규 Job 개발자가 운영 등록까지 이해할 수 있게 작성한다.

| 항목내용       |                    |
| ---------- | ------------------ |
| Job ID     | 실제 식별 방식           |
| Job Name   | Naming Rule        |
| Version    | Version 관리 방식      |
| Artifact   | 실제 형태              |
| 등록 위치      | Registry/DB/Config |
| 활성 Version | 선택 방식              |
| 이전 Version | 유지/삭제 정책           |
| Rollback   | 이전 Version 전환 방식   |

---

# 28. Job Version 변경 기준

다음 변경이 Version에 어떤 영향을 주는지 작성한다.

| 변경Version 변경 필요Parameter 호환성Restart 호환성기존 Execution 영향 |       |               |     |     |
| ------------------------------------------------------ | ----- | ------------- | --- | --- |
| SQL 변경                                                 | 실제 정책 | ...           | ... | ... |
| Step 추가                                                | 실제    | ...           | ... | ... |
| Parameter 추가                                           | 실제    | Default 필요 여부 | ... | ... |
| DTO 변경                                                 | 실제    | ...           | ... | ... |

---

# 29. Scheduler Summary

| JobSchedulerScheduleTimezoneParameterMisfire동시 실행ADM 관리상세 |
| --------------------------------------------------------- |

---

# 30. Scheduler 등록 작성 기준

반드시 다음을 포함한다.

- Scheduler 종류
- Schedule 표현식
- Timezone
- 활성화 여부
- Job Version
- 기본 Parameter
- 동적 Parameter
- Calendar
- 실행 제외일
- 동시 실행 허용 여부
- Max Runtime이 있다면 해당 값
- Misfire
- Schedule 변경
- Audit
- Permission

---

# 31. Misfire 판단표

| 상황기존 예정 시각현재 시각Misfire 정책실행 여부Parameter 기준주의 |          |       |       |    |    |       |
| -------------------------------------------- | -------- | ----- | ----- | -- | -- | ----- |
| 서버 일시 중단                                     | 01:00    | 01:10 | 실제 정책 | 실제 | 실제 | 중복    |
| 장기간 중단                                       | 여러 회     | 복구    | 실제    | 실제 | 실제 | 누적 실행 |
| 수동 실행 중                                      | 예약 시각 도래 | -     | 실제    | 실제 | -  | 동시 실행 |

단순 “Misfire를 처리한다”로 끝내지 않는다.

---

# 32. Partition Summary

| 상황Partition 필요기준 KeyPartition 수WorkerRestartRebalance상세 |
| ------------------------------------------------------- |

Partition을 단순 성능 옵션으로 설명하지 않는다.

다음 조건을 판단한다.

- 전체 대상 건수
- Query 분할 가능성
- 데이터 편향
- Commit 단위
- Partition 간 데이터 충돌
- Worker Capacity
- Restart 요구
- Ordering
- 외부 시스템 호출량

---

# 33. Partition Key 설계표

| 분할 방식장점단점누락 위험중복 위험적합한 데이터 |     |     |     |     |     |
| -------------------------- | --- | --- | --- | --- | --- |
| ID Range                   | ... | ... | ... | ... | ... |
| Hash                       | ... | ... | ... | ... | ... |
| 날짜                         | ... | ... | ... | ... | ... |
| 업무 Key                     | ... | ... | ... | ... | ... |

실제 CPF 구현에서 지원하는 방식만 CPF 기능으로 작성한다.

---

# 34. Local Partition / Remote Partition 비교

| 항목Local PartitionRemote Worker |                |                    |
| ------------------------------ | -------------- | ------------------ |
| 실행 Process                     | 실제             | 실제                 |
| Worker 위치                      | Same JVM       | 별도 Runtime         |
| 통신 방식                          | 실제             | 실제                 |
| TX                             | Partition별     | Worker별            |
| 장애 영향                          | 실제             | 실제                 |
| Scale-out                      | 실제             | 실제                 |
| Restart                        | 실제             | 실제                 |
| 배포                             | 단일 Artifact/실제 | Worker Artifact/실제 |

---

# 35. Runner / Worker / Agent Summary

| 구성요소역할실행 위치입력상태 저장Lease장애 시 영향상세 |
| -------------------------------- |

각 구성요소의 역할을 섞어서 설명하지 않는다.

---

# 36. Lease / Claim / Fencing 작성 기준

분산 Worker 구조에서 실제 구현이 존재하면 별도 핵심 장으로 작성한다.

## Lease

- Lease 대상
- Lease Owner
- TTL
- Renewal
- Expire

## Claim

- Claim 단위
- Claim 조건
- 동시 Claim 방지
- Claim 해제

## Fencing

- Fencing Token
- 오래된 Worker의 쓰기 방지
- Token 검증 위치
- Lease 만료 후 이전 Worker 처리

---

# 37. Lease 상태 표

| 상태OwnerLease 만료Worker 행동다른 Worker ClaimDB Update 허용 |
| --------------------------------------------------- |

그리고 반드시 다음 장애를 설명한다.

`Worker A Lease 획득`

→ `Worker A 일시 정지`

→ `Lease 만료`

→ `Worker B Claim`

→ `Worker A 복귀`

이때 Worker A가 이전 Token으로 데이터를 변경할 수 있는지 실제 구현 기준으로 설명한다.

---

# 38. Center-Cut 개발 기준

CPF에 Center-Cut 개념이 있다면 일반 Chunk와 별도로 상세하게 작성한다.

Center-Cut 장에서는 최소 다음을 정의한다.

- Center-Cut의 정확한 의미
- 일반 Chunk Job과 차이
- 사용 대상
- 대상 선정 기준
- 기준 시각
- Cut-off 기준
- 대상 Snapshot 여부
- 대상 건수 확정 시점
- Partition
- Chunk
- Commit
- 상태값
- 대사 기준
- 재처리 방식

---

# 39. Center-Cut 처리 흐름

반드시 다음 관계를 설명한다.

`대상 선정`

→ `대상 건수 확정`

→ `Partition`

→ `Claim`

→ `Chunk 처리`

→ `Commit`

→ `완료 건수 집계`

→ `오류 건수 집계`

→ `대사`

→ `Job 완료 판정`

---

# 40. Center-Cut 대사표

| 항목건수기준 Source정상 조건불일치 시 조치 |    |           |           |           |
| -------------------------- | -- | --------- | --------- | --------- |
| 선정 대상                      | 실제 | 대상 Query  | 기준값       | -         |
| 처리 시도                      | 실제 | Execution | 선정 대상과 관계 | 조사        |
| 성공                         | 실제 | 결과 Table  | ...       | -         |
| 실패                         | 실제 | Error     | ...       | Reprocess |
| 제외                         | 실제 | Filter    | ...       | 사유 확인     |
| 최종 대사                      | 실제 | 집계        | 공식 등식     | Reconcile |

센터컷 계열 배치는 **처리 완료가 아니라 대사 완료까지** 개발 설명 범위에 포함한다.

---

# 41. Dry Run / Preview 작성 기준

실제 실행 전 영향 범위를 확인할 기능이 존재하면 상세히 작성한다.

| 기능실제 처리DB 변경외부 호출예상 건수결과 저장실행과 동일 조건 |
| ------------------------------------ |

반드시 다음을 구분한다.

- Dry Run
- Count Preview
- Validation Only
- 실제 실행

Preview 결과와 실제 실행 대상이 달라질 수 있는 조건을 설명한다.

예:

- Preview 후 데이터 변경
- 기준시각 차이
- Transaction Isolation 차이

---

# 42. Batch 실행 API / Command Summary

| 개발/운영 목적API/Command입력권한상태 변경IdempotencyAudit상세 |    |               |    |             |     |   |   |
| ---------------------------------------------- | -- | ------------- | -- | ----------- | --- | - | - |
| Job 실행                                         | 실제 | Job/Parameter | 실제 | CREATED→... | 실제  | Y | § |
| Stop                                           | 실제 | Execution ID  | 실제 | ...         | ... | Y | § |
| Restart                                        | 실제 | Execution ID  | 실제 | ...         | ... | Y | § |
| Abandon                                        | 실제 | Execution ID  | 실제 | ...         | ... | Y | § |
| Reprocess                                      | 실제 | 대상            | 실제 | ...         | ... | Y | § |

---

# 43. Batch 개발 명령어 Summary

다음 명령을 실제 Repository 기준으로 전수 조사한다.

## Build

- Batch Module Build
- Runner Build
- Worker Build
- Job Pack Build

## Test

- Batch Test
- Job Test
- Step Test
- Integration Test
- Partition Test

## Runtime

- Job 실행
- Runner 실행
- Worker 실행
- Agent 실행

## Job 관리

- Job 등록
- Version 등록
- Job 실행
- Stop
- Restart

## Scheduler

- 등록
- 변경
- 비활성
- Validation

## DB

- Batch Metadata Migration
- Batch 업무 Migration

---

# 44. Batch 명령어 Master Table

| 목적명령실행 위치필수 Parameter주요 옵션영향 대상정상 결과실패 결과재실행 |
| -------------------------------------------- |

뒤쪽 상세 Reference:

| 명령Syntax선행조건ParameterDefault환경변수생성/변경 대상정상 출력Exit CodeRollback |
| -------------------------------------------------------------- |

---

# 45. Batch Property Summary

| 기능PropertyENVTypeDefault필수범위ConsumerProfile재기동상세 |
| ------------------------------------------------ |

최소 검토 범위:

- Chunk Size
- Page/Fetch Size
- Thread Count
- Partition Count
- Worker Count
- Timeout
- Retry
- Skip
- Lease TTL
- Claim Timeout
- Poll Interval
- Scheduler
- Metadata DB
- Artifact
- Kafka/Remote 관련 설정

실제 존재하는 Property만 작성한다.

---

# 46. 배치 동시 실행 정책

| 상황실행 허용식별 기준Lock/Lease중복 위험실제 결과 |    |             |    |     |          |
| -------------------------------- | -- | ----------- | -- | --- | -------- |
| 동일 Job+동일 Parameter              | 실제 | JobInstance | 실제 | 실제  | 실제 Error |
| 동일 Job+다른 날짜                     | 실제 | Parameter   | 실제 | ... | ...      |
| 수동+Scheduler 동시                  | 실제 | 실제          | 실제 | ... | ...      |
| 다른 Version                       | 실제 | Version     | 실제 | ... | ...      |

---

# 47. Batch Idempotency 작성 기준

배치에서는 다음 세 수준을 구분한다.

1. Job 실행 Idempotency
2. Chunk/Partition Idempotency
3. Item 처리 Idempotency

별도 표:

| 수준중복 발생 원인방지 Key저장 위치Restart 영향Reprocess 영향 |
| ------------------------------------------- |

---

# 48. 외부 시스템 연계 Batch

외부 API를 반복 호출하는 Batch는 별도 작성 기준을 둔다.

반드시 다음을 설명한다.

- Chunk TX 안에서 호출하는가
- 호출 성공 후 DB Commit 실패
- Remote 처리 성공 후 Response Loss
- Timeout
- Retry
- Remote Idempotency Key
- Rate Limit
- Throttling
- Reconciliation
- Compensation

---

# 49. 외부 호출 결과 조합표

| Remote 처리ResponseLocal Commit상태재처리다음 조치 |         |          |                 |           |                        |
| --------------------------------------- | ------- | -------- | --------------- | --------- | ---------------------- |
| 실패                                      | 수신      | Rollback | 실패 확정           | 조건부       | 실제 정책                  |
| 성공                                      | 수신      | 성공       | 완료              | N         | 종료                     |
| 성공 가능                                   | Timeout | 미확정      | UNKNOWN\_RESULT | 바로 재호출 제한 | Status Query           |
| 성공                                      | 수신      | 실패       | 불일치             | 위험        | Compensation/Reconcile |

---

# 50. UNKNOWN\_RESULT in Batch

Batch의 `UNKNOWN_RESULT`는 Job 전체 상태와 Item 단위 상태를 구분한다.

다음 경우를 조사한다.

- Job Launch 응답 유실
- Worker 결과 응답 유실
- Remote API 응답 유실
- Commit 응답 유실
- Scheduler 요청 결과 불명

표:

| 대상Local에서 아는 상태실제 가능 상태즉시 Retry확인 방법정상화 |
| --------------------------------------- |

---

# 51. 재처리 대상 관리

Reprocess가 존재하면 다음을 명확히 작성한다.

- 누가 재처리 대상을 만든다
- 대상 식별 Key
- 원 Job과 관계
- Error Reason
- 원본 Payload
- 수정 가능한 값
- 승인
- 실행
- 재처리 결과
- 원 실패 건 상태
- Audit

---

# 52. Reconciliation 작성 기준

배치 처리 완료 판단과 대사를 연결한다.

| 대사 항목Source ASource B비교 Key정상 조건불일치 상태조치 |
| ---------------------------------------- |

예:

- 대상 건수 vs 처리 건수
- Local 성공 vs Remote 성공
- Outbox vs Consumer 결과
- 원장 vs 업무 Table

실제 구현되는 대사만 작성한다.

---

# 53. Artifact / Job Pack 작성 기준

배치 Job이 배포 가능한 단위로 Packaging되는 구조라면 다음을 작성한다.

| Artifact포함 내용Version생성 명령Checksum배포 위치Runner 호환 |
| ----------------------------------------------- |

Job Pack에는 실제 구현 기준으로:

- Job Code
- Dependency
- Config
- Migration
- Metadata
- Manifest
- Version
- Checksum

등을 설명한다.

---

# 54. 배치 실행 Lifecycle

개발자가 자신의 Job이 운영에서 어떻게 실행되는지 알아야 한다.

가능한 전체 흐름을 실제 구현 기준으로 연결한다.

`Job 등록`

→ `Version 등록`

→ `Scheduler/수동 실행 요청`

→ `Parameter Validation`

→ `Permission/Approval`

→ `Execution 생성`

→ `Runner`

→ `Step`

→ `Worker`

→ `Progress`

→ `Completion/Failure`

→ `Reconcile`

→ `종료`

---

# 55. 승인과 위험 조치

실행·Stop·Abandon·Reprocess 등 위험 동작에 승인 기능이 존재한다면 다음 표를 작성한다.

| 작업PermissionReasonApprovalExpected VersionAudit실행 조건 |
| ---------------------------------------------------- |

---

# 56. ADM 개발 확인 Map

| 개발 기능ADM 메뉴RoutePermission핵심 Field정상 상태실패 상태가능한 조치 |
| -------------------------------------------------- |

최소 확인 대상:

- Job Definition
- Job Version
- Schedule
- Job Execution
- Step Execution
- Partition
- Worker
- Error
- Retry
- Stop
- Restart
- Reprocess
- Reconciliation
- Artifact

실제 화면이 없는 기능에는 가상 Route를 작성하지 않는다.

---

# 57. Batch Observability 표

| 대상LogMetricTrace주요 식별자ADM정상 판정 |    |    |     |                 |     |     |
| ------------------------------ | -- | -- | --- | --------------- | --- | --- |
| Job                            | 실제 | 실제 | 실제  | JobExecutionId  | 실제  | ... |
| Step                           | 실제 | 실제 | 실제  | StepExecutionId | ... | ... |
| Partition                      | 실제 | 실제 | ... | PartitionId     | ... | ... |
| Worker                         | 실제 | 실제 | ... | WorkerId        | ... | ... |

---

# 58. 처리량 및 진행률

실제 지원하는 경우 다음을 설명한다.

- Read Count
- Write Count
- Filter Count
- Skip Count
- Commit Count
- Rollback Count
- 처리율
- 예상 완료시간 계산 여부

Summary:

| Metric의미SourceADM 표시완료 판정에 사용 |
| ----------------------------- |

---

# 59. Batch Test Matrix

| 유형정상ParameterTXRestartStopConcurrentPartitionWorker FailureTimeoutReprocessReconcile |    |    |    |       |    |    |    |    |    |    |    |
| ------------------------------------------------------------------------------------ | -- | -- | -- | ----- | -- | -- | -- | -- | -- | -- | -- |
| Tasklet                                                                              | 필수 | 필수 | 조건 | 필수 여부 | 조건 | 조건 | -  | -  | 조건 | 조건 | 조건 |
| Chunk                                                                                | 필수 | 필수 | 필수 | 필수    | 조건 | 조건 | 조건 | 조건 | 조건 | 필수 | 조건 |
| Remote Worker                                                                        | 필수 | 필수 | 필수 | 필수    | 필수 | 필수 | 필수 | 필수 | 필수 | 필수 | 필수 |

실행하지 않은 Test는 `미검증`으로 표시한다.

---

# 60. Job Test 작성 기준

최소 다음을 검토한다.

- Job 전체 성공
- Parameter Validation 실패
- Empty Input
- 1건
- Chunk Boundary 직전/직후
- Reader 실패
- Processor 실패
- Writer 실패
- Commit 실패
- Restart
- Duplicate 실행
- Stop
- 동시 실행

---

# 61. Partition Test

- Partition 1개
- Partition 여러 개
- 빈 Partition
- 데이터 편향
- Worker 부족
- Worker 종료
- Partition Retry
- 중복 Claim
- Lease Expiration
- Fencing
- 최종 합계 대사

---

# 62. Fault Injection Catalog

| 장애발생 위치재현 방법기대 상태MetadataRetryRestartReconcile정상화 기준 |
| ---------------------------------------------------- |

검토 대상:

- Reader DB Connection 단절
- Writer Deadlock
- Commit 중 Connection Loss
- Remote Timeout
- Kafka 장애
- Runner 종료
- Worker 종료
- Scheduler 장애
- Lease 갱신 실패
- Metadata DB 장애

---

# 63. Fault Injection 상세 형식

각 장애마다 다음을 작성한다.

1. 테스트 목적
2. 사전 데이터
3. JobParameter
4. Job 실행
5. 장애 발생 시점
6. Job Status
7. Step Status
8. Read/Write/Commit Count
9. DB 상태
10. Metadata
11. Log
12. Metric
13. Worker/Lease 상태
14. Restart 가능 여부
15. Reprocess 대상
16. Reconcile 결과
17. 정상화 방법
18. 정상화 판정

---

# 64. 개발자가 자주 보는 증상 Quick Table

| 증상먼저 확인가능 원인하면 안 되는 것개발자 조치상세 |                    |                 |                 |               |   |
| ----------------------------- | ------------------ | --------------- | --------------- | ------------- | - |
| Job이 시작되지 않음                  | Parameter/Registry | 동일 Instance     | Parameter 임의 변경 | 상태 확인         | § |
| Job은 FAILED인데 일부 데이터 반영       | Commit Count       | 이전 Chunk Commit | 전체 재실행          | Restart 정책 확인 | § |
| Restart 후 중복                  | Writer/Idempotency | Checkpoint 차이   | 수동 삭제           | Item Key 확인   | § |
| Worker가 처리 중 멈춤               | Lease              | Worker 장애       | 강제 DB 수정        | Lease 상태 확인   | § |
| Scheduler 실행 누락               | Misfire            | Scheduler 중단    | 즉시 중복 수동실행      | Misfire 확인    | § |

---

# 65. 배치 Source Navigation Map

| 하려는 일Source 위치역할수정 가능관련 Test관련 Config관련 SQL |    |            |      |    |     |          |
| ------------------------------------------- | -- | ---------- | ---- | -- | --- | -------- |
| 신규 Job 작성                                   | 실제 | Job Config | Y    | 실제 | 실제  | -        |
| Reader 작성                                   | 실제 | Input      | Y    | 실제 | ... | Query    |
| Runner 이해                                   | 실제 | Runtime    | 보통 N | 실제 | ... | ...      |
| Lease 확인                                    | 실제 | Framework  | N    | 실제 | 실제  | 실제 Table |

---

# 66. 각 Batch 기능 장 공통 Template

주요 기능은 해당되는 범위에서 다음 형식을 사용한다.

1. 기능 목적
2. 사용 상황
3. 사용하지 말아야 하는 상황
4. Owner Module
5. 실제 Consumer
6. Public API
7. SPI
8. Annotation
9. Command
10. Source 위치
11. Config
12. SQL/Migration
13. 선행 조건
14. 입력
15. Default
16. 전체 Source 예제
17. 실행 명령
18. 정상 흐름
19. 정상 결과
20. Job/Step 상태
21. Metadata
22. Transaction
23. Chunk/Commit
24. Checkpoint
25. 동시성
26. Idempotency
27. Timeout
28. Retry
29. Skip
30. Stop
31. Restart
32. Reprocess
33. `UNKNOWN_RESULT`
34. Reconciliation
35. Compensation/Rollback
36. Permission
37. Reason
38. Approval
39. Audit
40. Log
41. Metric
42. Trace
43. Test
44. Fault Injection
45. ADM 확인
46. 배포 인계
47. 제한사항
48. 미검증

---

# 67. Tutorial 작성 기준

Tutorial은 단순 CSV Import 예제로 끝내지 않는다.

다음 요소를 포함하는 실제적인 교육용 Job을 선정한다.

예:

> 기준일자의 대상 고객을 조회하여 정산 데이터를 생성하고 처리 결과 Event를 발행하는 Job

요구사항 예:

- 기준일 JobParameter
- 대상 Preview
- Chunk 처리
- DB 저장
- Version 또는 중복 방지
- Outbox/Event
- 일부 데이터 Validation 실패
- Writer 실패
- Restart
- Reprocess
- Reconcile
- ADM 확인

---

# 68. Tutorial 전체 진행

1. 요구사항 분석
2. Job 유형 선택
3. Tasklet/Chunk 선택
4. 생성 파일 목록 확인
5. Job 생성
6. Step 생성
7. JobParameter 작성
8. Reader 작성
9. Processor 작성
10. Writer 작성
11. Transaction 설정
12. Chunk Size 설정
13. Listener 작성
14. Error 처리
15. Retry/Skip 판단
16. Metadata 확인
17. Local 실행
18. 정상 Test
19. Boundary Test
20. Writer 실패 재현
21. Restart
22. 중복 처리 확인
23. Partition 적용
24. Worker 장애
25. Reprocess
26. Reconciliation
27. Scheduler 등록
28. ADM 확인
29. Artifact 생성
30. 배포 인계

---

# 69. Tutorial 요구사항 Trace 표

| 요구사항구현 SourceCPF 기능TestADM 확인 |
| ----------------------------- |

Tutorial 단계가 서로 끊어지지 않도록 최종적으로 양방향 연결한다.

---

# 70. Batch Cookbook 필수 주제

다음 문제 중심 Recipe를 실제 구현 범위에서 제공한다.

- 신규 Tasklet Job 만들기
- 신규 Chunk Job 만들기
- DB Reader 만들기
- 파일 Reader 만들기
- Processor에서 데이터 변환하기
- DB Writer 만들기
- JobParameter 추가하기
- 날짜 기준 Job 만들기
- Chunk Size 변경하기
- 실패 Job Restart하기
- 일부 오류 데이터 Skip하기
- 일시 오류 Retry하기
- 특정 실패 건만 Reprocess하기
- Partition 적용하기
- Remote Worker로 Scale-out하기
- 동일 Job 중복 실행 막기
- Scheduler 등록하기
- Schedule 변경하기
- Misfire 처리하기
- Dry Run 제공하기
- Center-Cut Job 만들기
- 외부 API 호출 Batch 만들기
- 응답 유실 처리하기
- 결과 Reconciliation하기

---

# 71. Cookbook 공통 형식

각 Recipe는 다음 형식으로 작성한다.

1. 해결하려는 문제
2. 적용 조건
3. 선택할 Batch 방식
4. 선택하지 말아야 할 방식
5. 필요한 CPF 기능
6. Source 위치
7. 전체 코드
8. JobParameter
9. Config
10. SQL
11. 실행 명령
12. 정상 결과
13. 실패 사례
14. Restart
15. Reprocess
16. Reconcile
17. Test
18. ADM 확인

---

# 72. Batch Reference 필수 목록

Reference에는 다음을 전수 정리한다.

## Public API

- Class
- Interface
- Method

## SPI

- Reader/Writer 확장 지점
- Runner/Worker 관련 확장 지점

## Annotation

## Job

## Step

## JobParameter

## Command

- Execute
- Stop
- Restart
- Abandon
- Reprocess
- Reconcile 등 실제 명령

## REST API

## 개발 명령어

## Property

## Environment Variable

## Error Code

## Permission

## 상태

## Metadata Table

## ADM Route

---

# 73. Batch 개발 완료 Checklist

Checklist는 단순 Yes/No 목록이 아니라 증적을 포함한다.

| 검사항목적용 대상확인 방법Test/명령정상 기준Evidence상태 |
| ------------------------------------ |

---

# 74. Job 설계 Checklist

- Job 목적이 한 문장으로 정의되는가
- 재실행 기준이 명확한가
- JobParameter가 정의되어 있는가
- Identifying Parameter가 명확한가
- Tasklet/Chunk 선택 이유가 있는가
- Step 순서가 명확한가
- Transaction Boundary가 명확한가
- Chunk Size 근거가 있는가
- Restart 요구를 확인했는가
- 동시 실행 정책이 있는가
- 처리량 기준이 있는가

---

# 75. Reader Checklist

- 안정적인 정렬 Key가 있는가
- Restart 시 누락/중복 가능성을 검토했는가
- 실행 중 데이터 변경 영향을 검토했는가
- Fetch/Page Size가 정의됐는가
- Partition 범위가 겹치지 않는가

---

# 76. Writer Checklist

- 한 Item이 두 번 처리되어도 문제가 없는가
- DB Constraint가 필요한가
- Chunk Rollback 후 재처리 가능한가
- 외부 API Side Effect가 있는가
- 외부 처리 결과 불명 시 Reconcile 가능한가

---

# 77. Restart Checklist

- Job이 Restartable인가
- 어느 Checkpoint에서 재개되는가
- Reader State가 저장되는가
- 이미 Commit된 Chunk는 재처리되는가
- 실패 Chunk는 어떻게 되는가
- 데이터 변경 시 영향이 있는가
- 중복 방지가 있는가

---

# 78. Partition Checklist

- Partition Key가 안정적인가
- 범위가 겹치지 않는가
- 빈 Partition 허용 여부
- 데이터 편향이 있는가
- Worker 장애 후 재할당 가능한가
- Lease/Claim이 필요한가
- Fencing이 적용되는가
- 최종 건수 대사가 가능한가

---

# 79. Scheduler Checklist

- Schedule이 정의됐는가
- Timezone이 명확한가
- Parameter 생성 규칙이 있는가
- Misfire 정책이 있는가
- 동시 실행 정책이 있는가
- Schedule 변경 시 Audit가 있는가
- 수동 실행과 충돌하지 않는가

---

# 80. 실패·복구 Checklist

- Reader 실패
- Processor 실패
- Writer 실패
- Commit 실패
- DB Deadlock
- Remote Timeout
- Response Loss
- Runner 종료
- Worker 종료
- Scheduler 장애
- Lease 만료

각 경우에:

- 상태
- Retry
- Restart
- Reprocess
- Reconcile
- 정상화

가 정의되어 있는지 확인한다.

---

# 81. 배포 인계 Checklist

배치 개발 완료 후 최소 다음 항목을 전달한다.

- Job Name
- Job Version
- Artifact
- Checksum
- 실행 Runtime
- Runner/Worker 요구사항
- JobParameter
- Default Parameter
- Schedule
- Timezone
- Misfire
- 예상 대상 건수
- 예상 처리 시간
- DB 변경
- Kafka
- 외부 Endpoint
- Credential/Secret
- Thread/Partition 설정
- Chunk Size
- Timeout
- Retry
- Monitoring
- ADM Route
- Stop 기준
- Restart 방법
- Reprocess 방법
- Reconcile 방법
- Rollback

---

# 82. EDU 종합 실습

배치 매뉴얼 마지막에는 실제 업무로 전환하기 전 종합 실습을 둔다.

실습자는 문서만 보고 다음을 수행한다.

1. 개발환경 구성
2. Batch Module Build
3. 신규 Job 생성
4. Parameter 작성
5. Chunk Step 구현
6. Reader 작성
7. Processor 작성
8. Writer 작성
9. Transaction 확인
10. Metadata 확인
11. 정상 실행
12. 실패 재현
13. Restart
14. Duplicate 실행 확인
15. Partition 적용
16. Worker 실행
17. Worker 장애 재현
18. Lease/Claim 확인
19. Reprocess
20. Reconcile
21. Scheduler 등록
22. Misfire 재현
23. ADM 확인
24. Artifact 생성
25. 배포 인계 문서 작성

---

# 83. 개발자가 처음 문서를 볼 때 인지 흐름

첫 화면:

> CPF Batch에서 어떤 유형의 Job을 만들 수 있는가?

Quick Finder:

> 지금 만들려는 업무는 Tasklet인가, Chunk인가, Partition인가?

Summary:

> 어떤 API·Command·Parameter·명령이 있는가?

Tutorial:

> 실제 Job 하나를 어떻게 처음부터 끝까지 만드는가?

Transaction/Restart 장:

> 실패하면 어디부터 다시 실행되는가?

Partition 장:

> 대용량 데이터를 어떻게 나누고 Worker 장애를 어떻게 처리하는가?

Scheduler 장:

> 언제 어떻게 실행되는가?

Recovery 장:

> Restart할지 Reprocess할지 Reconcile할지 어떻게 판단하는가?

Reference:

> 정확한 Class·Property·Command가 무엇인가?

---

# 84. 최종 검수 질문

## 찾을 수 있는가

- 지원 Job 유형을 바로 찾을 수 있는가?
- Tasklet과 Chunk 선택 기준을 찾을 수 있는가?
- Reader/Processor/Writer API를 찾을 수 있는가?
- Batch 명령어를 찾을 수 있는가?

## 만들 수 있는가

- 신규 Job을 생성할 수 있는가?
- Parameter를 추가할 수 있는가?
- Step을 작성할 수 있는가?
- Transaction과 Commit 단위를 설정할 수 있는가?

## 다시 실행할 수 있는가

- Retry, Restart, Rerun, Reprocess 차이를 이해하는가?
- 실패 Job을 안전하게 Restart할 수 있는가?

## 병렬 처리할 수 있는가

- Partition 기준을 선택할 수 있는가?
- Worker 장애 후 상태를 판단할 수 있는가?
- 중복 Partition 실행을 방지할 수 있는가?

## 예약 실행할 수 있는가

- Scheduler에 Job을 등록할 수 있는가?
- Misfire 결과를 판단할 수 있는가?
- 수동 실행과 예약 실행 충돌을 판단할 수 있는가?

## 실패를 판단할 수 있는가

- 이미 Commit된 데이터와 실패한 Chunk를 구분할 수 있는가?
- `UNKNOWN_RESULT`를 실패와 구분할 수 있는가?

## 복구할 수 있는가

- Restart할지 Reprocess할지 판단할 수 있는가?
- Reconciliation이 필요한 상황을 알 수 있는가?
- 정상화 여부를 확인할 수 있는가?

## 운영에 넘길 수 있는가

- Job Version
- Parameter
- Schedule
- Artifact
- Worker
- Config
- Migration
- Monitoring
- Restart/Reprocess/Reconcile

정보를 인계할 수 있는가?

---

# 85. 핵심 작성 원칙

CPF 배치 개발자 매뉴얼은 단순한 Spring Batch 사용법이나 Job 작성 예제가 아니다.

다음 전체 흐름을 하나로 연결한다.

**선택**

`어떤 Batch 유형을 사용할 것인가`

↓

**설계**

`Job → Step → Reader/Processor/Writer → Parameter → TX → Commit`

↓

**대용량 처리**

`Partition → Worker → Lease/Claim/Fencing`

↓

**실행**

`Job 등록 → Version → Scheduler/수동 실행 → Parameter`

↓

**진행**

`Execution → Step → Chunk → Commit → Progress`

↓

**실패**

`Retry → Stop → Restart → UNKNOWN_RESULT`

↓

**복구**

`Reprocess → Reconcile → Compensation`

↓

**검증**

`DB → Metadata → Log → Metric → ADM`

↓

**인계**

`Artifact → Config → Schedule → 운영 절차`

배치 개발자가 이 흐름을 다른 문서나 Source 분석 없이 이해하고 적용할 수 있게 작성한다.

---

# 86. 가장 중요한 핵심 표

매뉴얼 분량이 많아지더라도 다음 표는 앞부분에서 반드시 빠르게 찾을 수 있도록 한다.

1. **배치 개발 작업 Quick Finder**
   - 지금 하려는 일을 어디서 시작하는가.
2. **Tasklet / Chunk / Partition 선택표**
   - 어떤 처리 모델을 선택하는가.
3. **Job / Step / Reader / Processor / Writer Map**
   - Job이 실제 어떤 구성요소로 이루어지는가.
4. **JobParameter / JobInstance / Execution 관계표**
   - 같은 Job을 다시 실행했을 때 무엇이 달라지는가.
5. **Transaction / Chunk / Commit 관계표**
   - 몇 건이 하나의 Transaction이고 어디까지 Rollback되는가.
6. **Retry / Restart / Rerun / Reprocess / Reconcile 선택표**
   - 실패 이후 무엇을 해야 하는가.
7. **Partition / Worker / Lease / Fencing 관계표**
   - 분산 처리에서 누가 어떤 데이터를 처리하는가.
8. **Scheduler / Misfire 판단표**
   - 예정 실행을 놓쳤을 때 어떤 일이 발생하는가.
9. **UNKNOWN\_RESULT 판단표**
   - 실패인지 결과 불명인지 판단한다.
10. **Center-Cut 대상·처리·대사표**
    - 선정한 대상과 최종 처리 결과가 일치하는지 판단한다.
11. **Fault Injection / Recovery Matrix**
    - 어떤 장애에서 어떤 상태가 되고 어떻게 복구하는가.
12. **Source / API / Command / Property Navigation**
    - 정확히 어디를 수정하고 무엇을 호출하는가.

이 표들은 단순 Reference가 아니라 **배치 개발자의 설계 판단표**로 작성한다.

---

# 87. 최종 목표

CPF 배치 개발자 매뉴얼의 최종 목표는 다음 질문에 답하는 것이다.

> “CPF를 처음 사용하는 개발자에게 기준일자별 대량 데이터 처리 Job을 하나 만들고, Scheduler에 등록하고, 실패 시 Restart 가능하게 만들고, Partition으로 확장하고, Worker 하나를 중간에 종료한 뒤 복구하고, 일부 실패 건을 Reprocess하고, 최종 처리 건수를 Reconcile한 뒤 ADM에서 결과를 확인하라”고 했을 때 매뉴얼만 보고 수행할 수 있는가?

이를 수행할 수 없다면 부족한 부분을 찾아:

- Quick Finder
- Summary
- 상세 Guide
- Tutorial
- Cookbook
- Reference
- Test
- Fault Injection
- Recovery

중 적절한 위치에 보강한다.

배치 매뉴얼은 **Job 생성 방법을 설명하는 문서가 아니라, 배치를 설계하고 실행하고 실패를 판단하고 다시 정상 상태로 가져오는 전 과정을 개발자가 스스로 수행할 수 있게 하는 개발 작업 문서**로 작성한다.

---

# 88. 처음 사용하는 배치 개발자 우선 기준

기본 Batch Runtime과 프로젝트 골격이 준비된 개발자가 신규 Job을 추가하는 상황을 우선한다. 내부 Scheduler/Control Plane 구현보다 다음을 빠르게 찾을 수 있어야 한다.

- 어떤 처리 모델(Tasklet / Chunk / Partition)을 선택하는가.
- Job/Step Annotation을 어디에 붙이는가.
- Parameter가 JobInstance와 재실행에 어떤 영향을 주는가.
- Chunk size와 Transaction/Checkpoint가 어떻게 연결되는가.
- Retry / Skip / Restart / Reprocess / Reconcile 중 무엇을 선택하는가.
- Worker/Agent를 추가하면 무엇이 확장되는가.
- 외부 Side Effect가 UNKNOWN일 때 어떻게 판단하는가.
- 전체 Batch Runtime / 특정 Job Test / 검증 명령을 어디서 실행하는가.

각 장은 `선택 기준 → 공개 API/Annotation → 주요 옵션 → 최소 코드/설정 예 → 실패·재실행 기준` 순서를 우선하며, 배치 내부 구현 설명으로 분량을 채우지 않는다.



---

# 89. Batch TOP 50·실행 계약·튜닝표 현행화 기준 (2026-08-16)

배치 개발자 가이드는 처음 CPF Batch를 사용하는 업무 개발자가 **처리 모델을 고르고, 실행 요청/재실행 의미를 구분하고, 필요한 설정을 찾는 것**을 우선한다.

## 89.1 탐색 계층

문서 앞부분은 `Quick Map → 실행/테스트 명령 → 처리 모델 선택` 순으로 구성하고, 뒤쪽에 **Batch 개발 기능 TOP 50 Quick Reference**를 둔다. TOP 50은 50개 수를 채우는 목록이 아니라 최신 Source에서 확인된 공개 계약과 개발자가 알아야 할 실행 개념을 기능군별로 정리한다.

TOP 50의 각 항목은 필요에 따라 `Golden / Capability / Advanced`로 구분한다.

- `Golden`: 일반 업무 Job 개발자가 우선 익힐 계약
- `Capability`: 해당 기능/Topology를 사용할 때 확인할 계약
- `Advanced`: Worker/Adapter/Control Plane 확장 개발에서 주로 사용하는 계약

Advanced API를 Golden Path와 같은 비중으로 앞쪽에 노출하지 않는다.

## 89.2 처리 모델과 Topology

최소 다음 선택을 구분한다.

`Tasklet / Chunk / LOCAL / PARALLEL_STEPS / LOCAL_PARTITION / REMOTE_PARTITION / REMOTE_CHUNK / REMOTE_STEP / Center-Cut`

각 항목은 단순 정의가 아니라 `적합한 경우 / 분할 또는 Commit 단위 / 실패 후 영향 / 반드시 확인할 조건`을 제공한다.

## 89.3 실행 요청과 제어 계약

실행 요청은 다음 의미를 섞지 않는다.

`run / scheduledRun / retry / restart / rerun / stop / onDemand`

Control Plane 공개 제어 계약은 다음을 별도 Summary로 제공한다.

`start / stop / restart / abandon / reconcile`

일반 업무 개발자에게 Spring Batch `JobLauncher`를 운영 호출 Surface로 안내하지 않는다.

## 89.4 실패 후 용어

`Retry / Restart / Rerun / Reprocess / Reconcile / Abandon`은 각각 독립 기능으로 정의한다. 특히 `UNKNOWN_RESULT`는 실패 확정 상태가 아니므로 Blind Retry나 FAILED 강제 변환을 안내하지 않는다.

## 89.5 JobParameter

`name / type / required / defaultValue / identifying / sensitive / allowedValues / pattern / min / max`를 단순 필드 목록이 아니라 **JobInstance 식별, Restart/Rerun, Validation, Masking**과 연결해 설명한다.

## 89.6 Batch 실행 Property

Source에 `@ConfigurationProperties`로 존재하는 튜닝값은 별도 표로 제공하고 `Property / 기본값 / 허용 범위 / 용도`를 표시한다. 기본값과 범위를 추정하지 않고 최신 Source에서 검증한다.

최소 대상은 Chunk, Partition, Remote poll/timeout/throttle, materialized jobs, executor core/max/queue이다.

## 89.7 운영 인계

개발 장 마지막에는 운영자가 알아야 할 `Job ID / Parameter / 재실행 조건 / 외부 Side Effect / 예상 처리량 / Schedule / ADM 위치 / 권한·승인`을 짧게 정리한다.
