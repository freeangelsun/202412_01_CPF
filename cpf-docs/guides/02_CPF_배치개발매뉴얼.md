# CPF 배치 개발 매뉴얼 — 정기·대량 처리의 개발·실행·대사

> **주 독자**: 배치 개발자, 배치 운영자, 플랫폼 운영 담당자
> **완료 결과**: Job·Step·Worker·Scheduler·Center-Cut을 개발하고 승인·실행·중지·재시작·재처리·대사한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `54bcc10887a83b933685bff462c0b0d7df824923`

<!-- CPF-TOC:START -->
## 전체 목차

- [1. 제품 경계](#1-제품-경계)
- [2. 역할](#2-역할)
- [3. 시작 전 점검](#3-시작-전-점검)
- [4. Job 설계서](#4-job-설계서)
- [5. Tasklet과 Chunk 선택](#5-tasklet과-chunk-선택)
  - [5.1 Tasklet](#51-tasklet)
  - [5.2 Chunk](#52-chunk)
- [6. Reader·Processor·Writer](#6-readerprocessorwriter)
  - [Reader](#reader)
  - [Processor](#processor)
  - [Writer](#writer)
- [7. JobParameter와 재시작](#7-jobparameter와-재시작)
- [8. Stop·Restart·Abandon](#8-stoprestartabandon)
  - [Stop](#stop)
  - [Restart](#restart)
  - [Abandon](#abandon)
- [9. Partition과 Worker](#9-partition과-worker)
  - [9.1 분할 계약](#91-분할-계약)
  - [9.2 Claim·Lease·Fencing](#92-claimleasefencing)
  - [9.3 Worker 장애](#93-worker-장애)
- [10. Center-Cut](#10-center-cut)
- [11. Scheduler](#11-scheduler)
  - [등록](#등록)
  - [Misfire](#misfire)
- [12. 등록·승인·실행 흐름](#12-등록승인실행-흐름)
- [13. Dry Run과 Preview](#13-dry-run과-preview)
- [14. Artifact와 Job Pack](#14-artifact와-job-pack)
- [15. 오류·결과 미확정·부분 실패](#15-오류결과-미확정부분-실패)
  - [오류 분류](#오류-분류)
  - [결과 미확정](#결과-미확정)
- [16. 보안·권한·감사](#16-보안권한감사)
- [17. Log·Metric·Trace](#17-logmetrictrace)
  - [구조화 Log](#구조화-log)
  - [Metric](#metric)
  - [Trace](#trace)
- [18. ADM 확인](#18-adm-확인)
- [19. Test Matrix](#19-test-matrix)
- [20. 운영 완료 판정](#20-운영-완료-판정)
- [21. 배치 EDU 실행 절차](#21-배치-edu-실행-절차)
  - [EDU-BAT-01 — Chunk 재시작](#edu-bat-01-chunk-재시작)
  - [EDU-BAT-02 — Partition Worker 소유권 상실](#edu-bat-02-partition-worker-소유권-상실)
  - [EDU-BAT-03 — Scheduler Misfire와 승인](#edu-bat-03-scheduler-misfire와-승인)
  - [21.1 배치 EDU 30개 전수표](#211-배치-edu-30개-전수표)
- [22. Batch 장애 Runbook](#22-batch-장애-runbook)
- [23. 종단간 예제: 월말 수수료 정산 Chunk Job](#23-종단간-예제-월말-수수료-정산-chunk-job)
  - [23.1 이 기능으로 만드는 업무 결과](#231-이-기능으로-만드는-업무-결과)
  - [23.2 선택 기준과 사용하지 말아야 할 경우](#232-선택-기준과-사용하지-말아야-할-경우)
  - [23.3 주 사용자와 권한](#233-주-사용자와-권한)
  - [23.4 시작 전에 결정할 값](#234-시작-전에-결정할-값)
  - [23.5 작업 후 만들어지는 결과물](#235-작업-후-만들어지는-결과물)
  - [23.6 단계별 절차](#236-단계별-절차)
  - [23.7 입력값·기본값·허용 범위](#237-입력값기본값허용-범위)
  - [23.8 정상 결과와 완료 판정](#238-정상-결과와-완료-판정)
  - [23.9 중복·동시성·시간초과·응답 유실·부분 실패](#239-중복동시성시간초과응답-유실부분-실패)
  - [23.10 재시도·재시작·재처리·대사·보상·되돌리기](#2310-재시도재시작재처리대사보상되돌리기)
  - [23.11 로그·지표·추적·감사](#2311-로그지표추적감사)
  - [23.12 교육 예제](#2312-교육-예제)
  - [23.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2313-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [23.14 운영 인계](#2314-운영-인계)
- [24. Tasklet Job](#24-tasklet-job)
  - [24.1 이 기능으로 만드는 업무 결과](#241-이-기능으로-만드는-업무-결과)
  - [24.2 선택 기준과 사용하지 말아야 할 경우](#242-선택-기준과-사용하지-말아야-할-경우)
  - [24.3 주 사용자와 권한](#243-주-사용자와-권한)
  - [24.4 시작 전에 결정할 값](#244-시작-전에-결정할-값)
  - [24.5 작업 후 만들어지는 결과물](#245-작업-후-만들어지는-결과물)
  - [24.6 단계별 절차](#246-단계별-절차)
  - [24.7 입력값·기본값·허용 범위](#247-입력값기본값허용-범위)
  - [24.8 정상 결과와 완료 판정](#248-정상-결과와-완료-판정)
  - [24.9 중복·동시성·시간초과·응답 유실·부분 실패](#249-중복동시성시간초과응답-유실부분-실패)
  - [24.10 재시도·재시작·재처리·대사·보상·되돌리기](#2410-재시도재시작재처리대사보상되돌리기)
  - [24.11 로그·지표·추적·감사](#2411-로그지표추적감사)
  - [24.12 교육 예제](#2412-교육-예제)
  - [24.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2413-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [24.14 운영 인계](#2414-운영-인계)
- [25. 파일 입출력 Batch](#25-파일-입출력-batch)
  - [25.1 이 기능으로 만드는 업무 결과](#251-이-기능으로-만드는-업무-결과)
  - [25.2 선택 기준과 사용하지 말아야 할 경우](#252-선택-기준과-사용하지-말아야-할-경우)
  - [25.3 주 사용자와 권한](#253-주-사용자와-권한)
  - [25.4 시작 전에 결정할 값](#254-시작-전에-결정할-값)
  - [25.5 작업 후 만들어지는 결과물](#255-작업-후-만들어지는-결과물)
  - [25.6 단계별 절차](#256-단계별-절차)
  - [25.7 입력값·기본값·허용 범위](#257-입력값기본값허용-범위)
  - [25.8 정상 결과와 완료 판정](#258-정상-결과와-완료-판정)
  - [25.9 중복·동시성·시간초과·응답 유실·부분 실패](#259-중복동시성시간초과응답-유실부분-실패)
  - [25.10 재시도·재시작·재처리·대사·보상·되돌리기](#2510-재시도재시작재처리대사보상되돌리기)
  - [25.11 로그·지표·추적·감사](#2511-로그지표추적감사)
  - [25.12 교육 예제](#2512-교육-예제)
  - [25.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2513-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [25.14 운영 인계](#2514-운영-인계)
- [26. Partition·Remote Worker](#26-partitionremote-worker)
  - [26.1 이 기능으로 만드는 업무 결과](#261-이-기능으로-만드는-업무-결과)
  - [26.2 선택 기준과 사용하지 말아야 할 경우](#262-선택-기준과-사용하지-말아야-할-경우)
  - [26.3 주 사용자와 권한](#263-주-사용자와-권한)
  - [26.4 시작 전에 결정할 값](#264-시작-전에-결정할-값)
  - [26.5 작업 후 만들어지는 결과물](#265-작업-후-만들어지는-결과물)
  - [26.6 단계별 절차](#266-단계별-절차)
  - [26.7 입력값·기본값·허용 범위](#267-입력값기본값허용-범위)
  - [26.8 정상 결과와 완료 판정](#268-정상-결과와-완료-판정)
  - [26.9 중복·동시성·시간초과·응답 유실·부분 실패](#269-중복동시성시간초과응답-유실부분-실패)
  - [26.10 재시도·재시작·재처리·대사·보상·되돌리기](#2610-재시도재시작재처리대사보상되돌리기)
  - [26.11 로그·지표·추적·감사](#2611-로그지표추적감사)
  - [26.12 교육 예제](#2612-교육-예제)
  - [26.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2613-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [26.14 운영 인계](#2614-운영-인계)
- [27. Scheduler·Misfire](#27-schedulermisfire)
  - [27.1 이 기능으로 만드는 업무 결과](#271-이-기능으로-만드는-업무-결과)
  - [27.2 선택 기준과 사용하지 말아야 할 경우](#272-선택-기준과-사용하지-말아야-할-경우)
  - [27.3 주 사용자와 권한](#273-주-사용자와-권한)
  - [27.4 시작 전에 결정할 값](#274-시작-전에-결정할-값)
  - [27.5 작업 후 만들어지는 결과물](#275-작업-후-만들어지는-결과물)
  - [27.6 단계별 절차](#276-단계별-절차)
  - [27.7 입력값·기본값·허용 범위](#277-입력값기본값허용-범위)
  - [27.8 정상 결과와 완료 판정](#278-정상-결과와-완료-판정)
  - [27.9 중복·동시성·시간초과·응답 유실·부분 실패](#279-중복동시성시간초과응답-유실부분-실패)
  - [27.10 재시도·재시작·재처리·대사·보상·되돌리기](#2710-재시도재시작재처리대사보상되돌리기)
  - [27.11 로그·지표·추적·감사](#2711-로그지표추적감사)
  - [27.12 교육 예제](#2712-교육-예제)
  - [27.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2713-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [27.14 운영 인계](#2714-운영-인계)
- [28. Center-Cut](#28-center-cut)
  - [28.1 이 기능으로 만드는 업무 결과](#281-이-기능으로-만드는-업무-결과)
  - [28.2 선택 기준과 사용하지 말아야 할 경우](#282-선택-기준과-사용하지-말아야-할-경우)
  - [28.3 주 사용자와 권한](#283-주-사용자와-권한)
  - [28.4 시작 전에 결정할 값](#284-시작-전에-결정할-값)
  - [28.5 작업 후 만들어지는 결과물](#285-작업-후-만들어지는-결과물)
  - [28.6 단계별 절차](#286-단계별-절차)
  - [28.7 입력값·기본값·허용 범위](#287-입력값기본값허용-범위)
  - [28.8 정상 결과와 완료 판정](#288-정상-결과와-완료-판정)
  - [28.9 중복·동시성·시간초과·응답 유실·부분 실패](#289-중복동시성시간초과응답-유실부분-실패)
  - [28.10 재시도·재시작·재처리·대사·보상·되돌리기](#2810-재시도재시작재처리대사보상되돌리기)
  - [28.11 로그·지표·추적·감사](#2811-로그지표추적감사)
  - [28.12 교육 예제](#2812-교육-예제)
  - [28.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2813-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [28.14 운영 인계](#2814-운영-인계)
- [29. Job Pack·Runner·Worker·Agent](#29-job-packrunnerworkeragent)
  - [29.1 이 기능으로 만드는 업무 결과](#291-이-기능으로-만드는-업무-결과)
  - [29.2 선택 기준과 사용하지 말아야 할 경우](#292-선택-기준과-사용하지-말아야-할-경우)
  - [29.3 주 사용자와 권한](#293-주-사용자와-권한)
  - [29.4 시작 전에 결정할 값](#294-시작-전에-결정할-값)
  - [29.5 작업 후 만들어지는 결과물](#295-작업-후-만들어지는-결과물)
  - [29.6 단계별 절차](#296-단계별-절차)
  - [29.7 입력값·기본값·허용 범위](#297-입력값기본값허용-범위)
  - [29.8 정상 결과와 완료 판정](#298-정상-결과와-완료-판정)
  - [29.9 중복·동시성·시간초과·응답 유실·부분 실패](#299-중복동시성시간초과응답-유실부분-실패)
  - [29.10 재시도·재시작·재처리·대사·보상·되돌리기](#2910-재시도재시작재처리대사보상되돌리기)
  - [29.11 로그·지표·추적·감사](#2911-로그지표추적감사)
  - [29.12 교육 예제](#2912-교육-예제)
  - [29.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분](#2913-조직-고유-업무로-바꿀-부분과-cpf가-유지하는-부분)
  - [29.14 운영 인계](#2914-운영-인계)
- [30. Job 설계서 표준 양식](#30-job-설계서-표준-양식)
- [31. 실행 상태와 허용 행동](#31-실행-상태와-허용-행동)
- [32. Batch 운영 한 줄 검증 명령](#32-batch-운영-한-줄-검증-명령)
- [33. Batch Job 정의서 완성 예시](#33-batch-job-정의서-완성-예시)
- [34. Worker·Lease·Fencing 실행 계약](#34-workerleasefencing-실행-계약)
- [35. Scheduler Misfire 결정표](#35-scheduler-misfire-결정표)
- [36. Center-Cut 승인·실행·대사 양식](#36-center-cut-승인실행대사-양식)
- [37. Batch 장애 주입 시나리오](#37-batch-장애-주입-시나리오)

<!-- CPF-TOC:END -->

## 1. 제품 경계

CPF Batch는 Spring Batch를 Primary Engine으로 사용한다. 업무 상태와 합계는 업무 Owner가, 실행 원장·Checkpoint·Lease·Fencing은 Batch Owner가 소유한다.

기준 모듈:

```text
cpf-batch:contract
cpf-batch:runtime-common
cpf-batch:execution-runtime
cpf-batch:control-server
cpf-batch:scheduler
cpf-batch:worker
cpf-batch:center-cut-runner
cpf-batch:host-agent
cpf-batch:testkit
```

ADM은 Batch 내부 DB를 직접 수정하지 않고 `cpf-batch:contract`와 Owner Port를 사용한다.

## 2. 역할

| 역할 | 책임 |
|---|---|
| 배치 개발자 | Job·Step·Reader·Processor·Writer·Parameter·대사 기준 |
| 배치 운영자 | 등록·승인·실행·중지·재시작·재처리·대사 |
| 업무 Owner | 대상 선정, 업무 상태, 금액·건수 합계, 보상 |
| Batch Control | 실행 요청·승인·상태·제어 명령 |
| Scheduler | 일정·Misfire·동기화 |
| Execution Runtime | Spring Batch Job 실행과 Metadata |
| Worker | 분할 작업 Claim·Lease·Fencing·실행 |
| Host Agent | Process·Artifact 전달과 Host 상태 |
| ADM | 조회·조치 요청·승인·감사 UI |

## 3. 시작 전 점검

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; git -C $repo rev-parse HEAD; & (Join-Path $repo 'gradlew.bat') :cpf-batch:contract:test; & (Join-Path $repo 'gradlew.bat') :cpf-batch:execution-runtime:test; & (Join-Path $repo 'gradlew.bat') :cpf-batch:control-server:test; & (Join-Path $repo 'gradlew.bat') :cpf-batch:scheduler:test; & (Join-Path $repo 'gradlew.bat') :cpf-batch:worker:test
```


## 4. Job 설계서

| 항목 | 작성 내용 |
|---|---|
| Job ID·Version | 변경 불가능한 식별자와 Version |
| 업무 목적 | 처리할 업무 결과 |
| Owner | 업무 상태·대사 책임 모듈 |
| Parameter | 이름·Type·필수·기본값·재시작 동일성 |
| Step | 순서·조건·Rollback 경계 |
| Reader | 대상 Query·정렬·Paging·Snapshot |
| Processor | 검증·변환·Skip·Filter |
| Writer | Commit 단위·멱등성·Batch Write |
| Chunk | 크기·Memory·Lock·Commit 영향 |
| Partition | Key·분할 기준·중복·빈 Partition |
| Checkpoint | 재시작 위치와 업무 상태의 관계 |
| 대사 | 입력·처리·성공·실패·제외·금액 합계 |
| Retry | 오류 분류·횟수·Backoff |
| 보상 | 외부 효과·부분 성공 처리 |
| 운영 | Log·Metric·Trace·ADM·Alert |

## 5. Tasklet과 Chunk 선택

### 5.1 Tasklet

선택 상황:

- 단일 파일 이동
- Metadata 정리
- 외부 시스템 단일 조치
- 짧고 명확한 단계

필수:

- 반복 실행 멱등성
- 완료 여부 저장
- Timeout
- 외부 결과 미확정 처리
- 중지 신호 확인

### 5.2 Chunk

선택 상황:

- 대량 Row 처리
- Reader·Processor·Writer 분리
- Checkpoint 재시작
- Skip·Retry 정책 필요

Chunk 크기는 처리량만으로 정하지 않는다.

```text
DB Lock 시간
Memory
외부 호출 수
Rollback 범위
재시작 중복
업무 합계 대사
```

## 6. Reader·Processor·Writer

### Reader

- 정렬 Key가 결정적이어야 한다.
- Offset Paging보다 Keyset·Cursor의 일관성을 검토한다.
- 실행 중 대상 변경을 허용할지 Snapshot 기준을 정한다.
- 동일 Row 중복 읽기와 누락을 Test한다.

### Processor

- 업무 규칙은 Domain 또는 Owner Service를 사용한다.
- `null` Filter와 오류 Skip을 구분한다.
- 입력과 출력의 식별자를 유지한다.
- 개인정보를 Log에 출력하지 않는다.

### Writer

- 업무 원장과 실행 Metadata의 Transaction 경계를 구분한다.
- 재시작 시 동일 항목의 Replay가 안전해야 한다.
- 외부 효과는 Outbox·Attempt Ledger를 사용한다.
- 부분 Batch 실패 시 성공 Row와 실패 Row를 구분한다.

## 7. JobParameter와 재시작

Parameter는 다음으로 분류한다.

| 분류 | 예 | 재시작 규칙 |
|---|---|---|
| 식별 Parameter | 영업일·기관·업무구분 | 동일 Instance 판정에 포함 |
| 조정 Parameter | Chunk Size·Worker Count | Version·승인 없이 임의 변경 금지 |
| 비밀값 | Credential | Parameter 원문 저장 금지, Secret 참조 |
| 추적 Parameter | 요청자·승인 ID | Audit과 연결 |
| Dry Run | `true`·`false` | 실제 실행과 Instance를 구분 |

재시작 전 확인:

1. 이전 JobExecution 상태
2. 마지막 성공 Step
3. Checkpoint
4. 업무 원장 반영
5. 외부 효과
6. Parameter 동일성
7. 재실행 대상 범위
8. 대사 기준

## 8. Stop·Restart·Abandon

### Stop

- 중지 요청을 기록한다.
- 현재 Step이 중지 신호를 확인한다.
- 열린 Resource를 정리한다.
- 마지막 Checkpoint를 저장한다.
- 중지 완료와 Process 종료를 구분한다.

### Restart

- 같은 JobInstance와 허용 Parameter를 사용한다.
- 이미 성공한 Step을 다시 실행할지 정책을 확인한다.
- Writer의 Replay 안전성을 검증한다.
- 외부 효과는 Attempt Ledger와 상대 상태를 대사한다.

### Abandon

Abandon은 재시작을 막는 운영 판정이다. 다음 없이 사용하지 않는다.

- 사유
- 승인
- 영향 대상
- 업무 원장 상태
- 대체 처리
- Audit
- 되돌릴 수 없는 영향

## 9. Partition과 Worker

### 9.1 분할 계약

- Partition Key
- 대상 범위
- 중복 금지 규칙
- 빈 Partition 처리
- 총 Partition 수
- 재분할 허용 조건
- 업무 합계 대사

### 9.2 Claim·Lease·Fencing

1. Worker가 Partition을 Claim한다.
2. Lease 만료 시 다른 Worker가 인수할 수 있다.
3. 이전 Worker는 Fencing Token이 달라지면 쓰기를 중단한다.
4. Heartbeat 지연과 Process Kill을 시험한다.
5. 완료·실패·결과 미확정을 대상별로 기록한다.

### 9.3 Worker 장애

| 장애 | 판정 | 조치 |
|---|---|---|
| Process 종료 전 Commit 없음 | 미처리 | Lease 만료 후 재할당 |
| DB Commit 후 ACK 유실 | 결과 미확정 | 업무 원장·Checkpoint 대사 |
| 일부 Item 성공 | 부분 성공 | 성공 Item 유지, 실패 Item 재처리 |
| Lease 상실 후 늦은 응답 | Stale Worker | Fencing으로 쓰기 거부 |
| 반복 Poison Data | 재시도 불가 | 격리·사유·승인 후 처리 |

## 10. Center-Cut

Center-Cut은 중앙에서 대상을 분할해 Runner·Worker에 전달하는 대량 처리 방식이다.

정의해야 할 값:

- 대상 Query와 기준 시점
- Partition Key
- Chunk Size
- Commit 간격
- 동시 Worker
- Lease·Heartbeat
- Fencing
- Retry
- 결과 집계
- 대사 허용 오차
- 중지·재시작
- Artifact Version


## 11. Scheduler

### 등록

- Job ID·Version
- Cron·Timezone
- Calendar
- Misfire
- 중복 실행 정책
- Parameter Template
- 유효 기간
- 승인
- Owner

### Misfire

| 정책 | 사용 시점 | 위험 |
|---|---|---|
| 누락 확인 후 1회 | 누락 실행을 바로 보완 | 업무일·중복 확인 |
| 다음 일정부터 | 과거 실행 불필요 | 누락 데이터 별도 처리 |
| 누락 횟수만큼 | 각 회차 의미가 독립 | 폭주·중복 |
| 운영 승인 | 금액·외부 효과가 큼 | 승인 지연 |

db-scheduler 기반 구성과 Schedule 동기화 Source가 확인된 경우에도 실제 Lock·다중 인스턴스·Timezone·Misfire 시험이 없으면 실행 검증이다.

## 12. 등록·승인·실행 흐름

1. 개발자가 Job Pack과 Version을 등록한다.
2. 운영자가 Parameter·대상·Preview를 확인한다.
3. 위험 실행이면 승인 요청을 생성한다.
4. 승인자는 범위·건수·예상 금액·Artifact Hash를 확인한다.
5. Control Server가 실행 Operation을 생성한다.
6. Execution Runtime 또는 Worker가 실행한다.
7. ADM에서 상태·진행·오류·대사를 조회한다.
8. 결과가 미확정이면 신규 실행보다 Reconcile을 우선한다.
9. 완료 후 업무 합계·Audit·Artifact를 인계한다.

## 13. Dry Run과 Preview

Dry Run은 실제 Writer·외부 효과를 실행하지 않는다.

표시할 값:

- 대상 건수
- Partition 수
- 예상 Chunk
- 예상 금액·용량
- 제외 건수
- 권한 범위
- Job·Artifact Version
- DB Vendor
- 예상 실행 시간은 근거가 있을 때만

Dry Run 결과와 실제 실행 사이에 대상이 바뀔 수 있으므로 기준 시점·Query Hash·승인 유효시간을 기록한다.

## 14. Artifact와 Job Pack

Job Pack에 포함:

- Job Definition
- 실행 Artifact
- Version
- Parameter Schema
- DB Migration
- Config
- Checksum
- SBOM
- Rollback·Forward Recovery
- 운영 설명
- Test 결과

Control Server가 Artifact 내용을 임의로 바꾸지 않는다. 실행 노드는 승인된 Hash를 확인한다.

## 15. 오류·결과 미확정·부분 실패

### 오류 분류

```text
INPUT_INVALID
PERMISSION_DENIED
VERSION_CONFLICT
RETRYABLE_RESOURCE
NON_RETRYABLE_BUSINESS
TIMEOUT
UNKNOWN_RESULT
PARTIAL_SUCCESS
```

실제 상태·오류 Code는 Source 계약을 사용한다.

### 결과 미확정

- DB Commit 여부를 모름
- 메시지 발행 후 ACK 유실
- 외부 파일 전송 후 응답 유실
- Worker Process 종료 시점 불명

조치:

1. Transaction·Operation ID로 원장을 조회한다.
2. Checkpoint와 업무 상태를 비교한다.
3. 외부 시스템·메시지·파일 상태를 확인한다.
4. 성공 대상은 유지한다.
5. 실패 대상만 재처리한다.
6. 판정 사유와 담당자를 Audit에 남긴다.

## 16. 보안·권한·감사

| 조치 | Permission | 추가 통제 |
|---|---|---|
| 조회 | Batch View | Data Scope·Masking |
| 실행 요청 | Batch Execute | Reason·Parameter Validation |
| 중지 | Batch Control | Expected Version |
| 재시작 | Batch Restart | 대사 결과·승인 |
| 재처리 | Batch Reprocess | 대상 Preview·Idempotency |
| Abandon | Batch Admin | 승인·사유·대체 처리 |
| Scheduler 변경 | Batch Schedule Admin | Version·Approval·Audit |

실제 Permission 문자열은 Backend·Frontend Source에서 확인한다.

## 17. Log·Metric·Trace

### 구조화 Log

- Job·Step·Execution ID
- Partition·Worker
- Operation·Transaction ID
- Artifact Version
- 처리·성공·실패·제외 건수
- 오류 분류
- 민감정보 가림

### Metric

- 실행 시간
- 처리량
- Queue·Backlog
- Retry
- Skip
- 실패율
- Lease 만료
- Worker Heartbeat
- 대사 불일치

### Trace

외부 호출과 메시지·파일 연계는 Trace Context를 유지한다. 대량 Item 전체를 무제한 Span으로 만들지 않고 Sampling·Cardinality를 통제한다.

## 18. ADM 확인

ADM은 Batch를 새로 구현하는 화면이 아니다. 다음을 조회·조치한다.

- Job·Version
- Schedule
- Execution·Step
- Parameter
- Progress
- Worker·Lease
- 오류
- 결과 미확정
- 재시작·재처리
- 대사
- Approval·Audit

자세한 권한별 절차는 [03 CPF ADM 매뉴얼](03_CPF_ADM매뉴얼.md)을 사용한다.

## 19. Test Matrix

| Test | 필수 시나리오 |
|---|---|
| Job Unit | Parameter·상태·전이 |
| Step | Reader·Processor·Writer |
| Restart | Chunk 중간 종료 후 재시작 |
| Duplicate | 동일 Parameter 동시 실행 |
| Multi-instance | Claim·Lease·Fencing |
| Process Kill | Commit 전·후 종료 |
| DB Loss | Connection Lost·Timeout |
| Broker Loss | 발행·소비·ACK 유실 |
| File Loss | Partial Transfer·Checksum |
| Scheduler | Misfire·Timezone·중복 |
| Approval | 만료·권한·Version 충돌 |
| Reconcile | 결과 미확정·부분 성공 |
| Vendor | MariaDB·PostgreSQL·Oracle |

## 20. 운영 완료 판정

- Job·Artifact Version과 Hash가 확정됐다.
- 입력·처리·성공·실패·제외·금액 합계가 맞는다.
- 업무 원장과 Spring Batch Metadata가 일치한다.
- 결과 미확정 대상이 0이거나 승인된 미결 목록에 있다.
- Worker Lease와 실행 Process가 종료 상태다.
- Log·Metric·Trace·Audit가 같은 식별자로 연결된다.
- 재시작·재처리·보상 내역이 기록됐다.
- 실행 명령·환경·Exit Code·Job/Step 상태·업무 대사 결과를 기록했다.

## 21. 배치 EDU 실행 절차

### EDU-BAT-01 — Chunk 재시작

1. Job·Step·Chunk Size·Commit Interval을 확인한다.
2. 중복되지 않는 JobParameter를 준비한다.
3. 전체 건수·금액·Checksum Preview를 기록한다.
4. 중간 Chunk에서 Process Kill을 주입한다.
5. Spring Batch Metadata와 업무 Checkpoint를 비교한다.
6. 같은 Job Instance를 Restart한다.
7. 이미 Commit된 Item이 중복 처리되지 않는지 확인한다.
8. 최종 Read·Write·Skip·Rollback·업무 합계를 대사한다.

### EDU-BAT-02 — Partition Worker 소유권 상실

1. Partition Key와 예상 건수를 준비한다.
2. Worker 2개 이상에서 Claim·Lease를 확인한다.
3. 한 Worker의 Heartbeat를 중단한다.
4. Lease 만료와 새 Fencing Token 발급을 확인한다.
5. 이전 Worker의 늦은 Write가 거부되는지 확인한다.
6. 실패 Partition만 재처리하고 전체 합계를 대사한다.

### EDU-BAT-03 — Scheduler Misfire와 승인

1. Schedule·Timezone·Calendar·Misfire 정책을 등록한다.
2. Scheduler 중단으로 Misfire를 재현한다.
3. 바로 실행·건너뛰기·한 번만 보정 중 선택 정책을 확인한다.
4. 위험 Job은 승인 ID·Version·만료를 확인한다.
5. 중복 Job Instance가 생성되지 않는지 확인한다.

### 21.1 배치 EDU 30개 전수표

실행 전 `cpf.reference.features.batch.enabled=true`와 실제 `CpfBatchOperationsPort` Local/Remote 연결을 확인한다. 정책 조회 API의 성공을 Job 실행 성공으로 대체하지 않는다.

| 교육 ID | 확인할 기능 | 활성 조건 | 실행 안내 | 완료 판정 |
|---|---|---|---|---|
| `EDU-BAT-01` | 업무일 마감 Tasklet | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-02` | 대량 등급 계산 Chunk | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-03` | CSV 입출력 Batch | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-04` | 범위 Partition 처리 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-05` | 관리 노드·Worker·Lease·Fencing | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-06` | Center-Cut Preview·Approval·Execution | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-07` | 영업일 Scheduler | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-08` | Job Pack Version·Artifact 배포 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-09` | Stop·Restart·실패 건 Reprocess | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-10` | 실행 요청 응답 유실·Reconciliation | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-11` | 조건 분기·다단계 Job Flow | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-12` | Retry·Skip·금지 예외 분류 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-13` | Writer Commit 장애와 Checkpoint Restart | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-14` | JobParameter 식별·중복 실행·새 Instance | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-15` | Late-arriving Data·Backfill·재산출 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-16` | Watermark 기반 증분 수집·Restart | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-17` | 암호화·압축·Checksum 파일 산출 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-18` | 수신 파일 Header·Detail·Trailer 대사 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-19` | 다중 Input 병합·다중 Output 분기 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-20` | Scheduler Misfire·Catch-up·Skip | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-21` | 중복 실행 차단·동시 실행 허용 범위 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-22` | Holiday Calendar·영업일 순번 JobParameter | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-23` | Stop·Abandon·Restart 의미 분리 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-24` | Remote Worker 유실·재할당·중복 결과 차단 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-25` | Partition Skew 감지·재분할 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-26` | Center-Cut 결과 대사·차이 보정·재실행 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-27` | Job Pack Checksum·Compatibility·Rollback | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-28` | Host Agent 연결 끊김·Command ACK 유실 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-29` | Dry Run·Count Preview·Sample 확인 | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-BAT-30` | 대용량 처리 Capacity·Backpressure | `cpf.reference.features.batch.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |

각 항목은 Job/Step/Execution Metadata, Checkpoint, 대상 건수·금액, Audit와 ADM 화면을 함께 확인한다. Worker·Agent·Scheduler·Center-Cut 항목은 단일 JVM 성공으로 완료 처리하지 않고 다중 Process·응답 유실·Lease 상실을 포함해 시험한다.

## 22. Batch 장애 Runbook

| 장애 | 최초 확인 | 정상화 | 종료 판정 |
|---|---|---|---|
| Job 중복 실행 | JobParameter·Instance·Lease | 신규 실행 중지·Owner 판정 | 실행 Owner 1개 |
| Step 장기 정체 | Read/Process/Write 지표·DB Lock | 원인 제거·Stop/Restart | 진행률 증가·합계 일치 |
| Worker 이탈 | Heartbeat·Lease·Fencing | 재할당·과거 Write 차단 | Partition Owner 일치 |
| Commit 후 응답 유실 | Metadata·업무 원장·Attempt | Reconcile | 실제 결과 확정 |
| 일부 Partition 실패 | Target별 결과 | 실패 대상만 재처리 | 성공 대상 중복 없음 |
| Misfire 폭주 | Schedule·Last Fire·Policy | 신규 Trigger 차단·정책 적용 | 예정 실행 수 일치 |
| Artifact 불일치 | Job Pack Hash·Worker Version | Traffic/Claim 차단·재배포 | Version·Checksum 일치 |
## 23. 종단간 예제: 월말 수수료 정산 Chunk Job

### 23.1 이 기능으로 만드는 업무 결과

기준일의 계좌를 분할 조회해 수수료를 계산하고 원장에 반영하며, 중단 후 마지막 Commit 지점부터 재시작하고 업무 합계를 대사한다.

### 23.2 선택 기준과 사용하지 말아야 할 경우

- 수만 건 이상을 일정 단위로 읽고 변환·저장할 때 사용한다.
- 하나의 외부 명령만 실행하거나 전체를 한 Transaction으로 처리해야 하면 Tasklet을 검토한다.

### 23.3 주 사용자와 권한

배치 개발자는 Job/Step을 작성하고, 운영자는 실행·중지·재시작 Permission을 가지며, ABANDON·대량 재처리는 승인자가 결정한다.

### 23.4 시작 전에 결정할 값

Job 이름·Version, JobParameter, 기준일, Reader 정렬 Key, Chunk/Commit 크기, Retry/Skip, Checkpoint, 동시 실행, 대사 건수·금액·Hash를 정한다.

### 23.5 작업 후 만들어지는 결과물

Job Definition, Reader/Processor/Writer, Metadata, Migration, Job Pack Manifest, Scheduler, Test, ADM 운영 절차.

### 23.6 단계별 절차

1. `businessDate`, `runType`, `jobVersion`, `reprocessScope`를 식별 Parameter로 정의한다.
2. Reader Query에 안정된 정렬 Key와 재시작 조건을 넣는다.
3. Processor는 수수료 규칙과 Validation만 수행한다.
4. Writer는 한 Chunk의 업무 원장·상세·Audit를 같은 Transaction으로 저장한다.
5. Chunk 완료 후 ExecutionContext에 마지막 Key·건수·금액·Hash를 기록한다.
6. 동일 Parameter 동시 실행을 차단한다.
7. Dry Run으로 대상 건수·표본·예상 금액을 출력한다.
8. 승인된 Job Pack을 배포하고 Scheduler 또는 수동 실행한다.
9. Process Kill 후 같은 Job Instance를 Restart한다.
10. 메타데이터와 업무 원장을 건수·금액·Hash로 대사한다.
11. 실패 대상만 Reprocess하고 전체 완료 상태를 확정한다.

### 23.7 입력값·기본값·허용 범위

| 입력 | 기본값 | 범위 | 의미 |
|---|---|---|---|
| `businessDate` | 없음 | 유효 영업일 | 업무 기준일 |
| `runType` | `NORMAL` | NORMAL/REPROCESS/DRY_RUN | 실행 목적 |
| `chunkSize` | Job 정의값 | 용량 시험 범위 | Commit 단위 |
| `reprocessScope` | 없음 | 승인된 실패 ID/범위 | 성공 대상 제외 |
| `requestedBy` | 실행 주체 | 인증 사용자 | Audit |

### 23.8 정상 결과와 완료 판정

Job/Step가 COMPLETED이고 Read·Process·Write·Skip·Rollback Count가 설계와 일치한다. 업무 원장의 건수·금액·Hash가 Dry Run 및 대사 SQL과 일치한다.

### 23.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 실행, Reader 결과 변동, Writer Commit 실패, Process Kill, DB Deadlock, Skip 한도 초과, 응답 유실, 일부 Partition 실패를 구분한다.

### 23.10 재시도·재시작·재처리·대사·보상·되돌리기

같은 Job Instance를 Checkpoint에서 Restart한다. 데이터가 바뀌었으면 Snapshot/기준시각을 검증한다. 성공 범위는 유지하고 실패 범위만 Reprocess한다. 의미를 보장할 수 없으면 ABANDON 승인 후 새 Parameter로 실행한다.

### 23.11 로그·지표·추적·감사

Job/Step Execution ID, JobParameter Hash, 마지막 Key, Chunk Count, 건수·금액·Hash, Retry/Skip, Lock/Lease, Worker ID, Trace, Audit를 기록한다.

### 23.12 교육 예제

`EDU-BAT-02`, `EDU-BAT-09`, `EDU-BAT-13`, `EDU-BAT-16`을 연결해 실행한다.

### 23.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

수수료 계산·대상 Query·업무 Table은 조직 영역이다. Spring Batch Metadata, Checkpoint, Lease/Fencing, Job Pack·ADM 계약은 CPF가 유지한다.

### 23.14 운영 인계

Job Definition, Parameter, Schedule, Capacity, 대사 SQL, Restart/Reprocess/ABANDON 기준, Permission, Alert, Job Pack Checksum을 인계한다.


## 24. Tasklet Job

### 24.1 이 기능으로 만드는 업무 결과

마감 상태 전환·파일 이동·단일 집계처럼 한 Step의 명확한 작업을 수행한다.

### 24.2 선택 기준과 사용하지 말아야 할 경우

작업 전체가 하나의 의미 단위이고 부분 Commit이 불필요할 때 선택한다. 대량 건별 처리는 Chunk를 사용한다.

### 24.3 주 사용자와 권한

배치 개발자·운영자·승인자 역할을 분리하며 실행·중지·재처리·ABANDON·배포 Permission을 구분한다.

### 24.4 시작 전에 결정할 값

Tasklet 입력, Transaction 경계, 재실행 안전성, 완료 Marker, Timeout을 정한다.

### 24.5 작업 후 만들어지는 결과물

Tasklet Job Definition, Metadata, Config, Test, ADM 화면·Runbook.

### 24.6 단계별 절차

Tasklet은 실행 전 기존 완료 Marker와 업무 상태를 확인한다. 외부 부수 효과 전 Attempt를 기록하고, 완료 후 Marker·Audit를 남긴다.

### 24.7 입력값·기본값·허용 범위

입력은 Job Definition의 Parameter Schema를 따르고 필수·기본값·형식·식별 여부를 명시한다.

### 24.8 정상 결과와 완료 판정

완료 Marker와 업무 상태가 일치하고 같은 Parameter 재실행이 중복 효과를 만들지 않는다.

### 24.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 실행, Lock 경쟁, Timeout, Process 종료, 응답 유실, 부분 대상 실패를 각 상태로 분리한다.

### 24.10 재시도·재시작·재처리·대사·보상·되돌리기

Process Kill·응답 유실 시 Marker와 외부 상태를 조회한다. 결과가 확정되지 않으면 UNKNOWN_RESULT로 두고 Reconcile한다.

### 24.11 로그·지표·추적·감사

Job/Step/Operation/Partition/Worker ID와 건수·금액·Hash·Retry·Lease·Audit를 기록한다.

### 24.12 교육 예제

`EDU-BAT-01·11`를 실행하고 정상·Process Kill·응답 유실·부분 실패를 재현한다.

### 24.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 계산과 대상 선정은 조직 영역이다. Batch Metadata·Lease·Job Pack·운영 계약은 CPF가 유지한다.

### 24.14 운영 인계

Parameter, Schedule, Capacity, Permission, 대사·재시작·재처리·Rollback 기준을 인계한다.


## 25. 파일 입출력 Batch

### 25.1 이 기능으로 만드는 업무 결과

대용량 CSV·고정길이·암호화·압축 파일을 읽고 쓰며 Header/Trailer·건수·금액·검사합을 대사한다.

### 25.2 선택 기준과 사용하지 말아야 할 경우

업무 교환이 파일 단위이고 재시작 위치·완료 파일 규칙이 필요할 때 사용한다.

### 25.3 주 사용자와 권한

배치 개발자·운영자·승인자 역할을 분리하며 실행·중지·재처리·ABANDON·배포 Permission을 구분한다.

### 25.4 시작 전에 결정할 값

Encoding, Record Layout, Header/Trailer, Delimiter, Compression, Encryption, Checksum, Reject File을 정한다.

### 25.5 작업 후 만들어지는 결과물

파일 입출력 Batch Definition, Metadata, Config, Test, ADM 화면·Runbook.

### 25.6 단계별 절차

수신 파일을 임시 이름으로 안정화한 뒤 검사한다. Reader는 Record 번호를 Checkpoint로 저장하고 Writer는 임시 산출물을 생성한 뒤 Trailer·Hash 확인 후 원자 Rename한다.

### 25.7 입력값·기본값·허용 범위

입력은 Job Definition의 Parameter Schema를 따르고 필수·기본값·형식·식별 여부를 명시한다.

### 25.8 정상 결과와 완료 판정

입력·출력 건수와 금액, Trailer, SHA-256이 일치하고 Reject가 승인 범위 안에 있다.

### 25.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 실행, Lock 경쟁, Timeout, Process 종료, 응답 유실, 부분 대상 실패를 각 상태로 분리한다.

### 25.10 재시도·재시작·재처리·대사·보상·되돌리기

중간 파일과 완료 파일을 구분한다. 재시작은 마지막 Commit 이후 Record부터 수행하고, 이미 확정한 산출물은 재생성하지 않는다.

### 25.11 로그·지표·추적·감사

Job/Step/Operation/Partition/Worker ID와 건수·금액·Hash·Retry·Lease·Audit를 기록한다.

### 25.12 교육 예제

`EDU-BAT-03·17·18·19`를 실행하고 정상·Process Kill·응답 유실·부분 실패를 재현한다.

### 25.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 계산과 대상 선정은 조직 영역이다. Batch Metadata·Lease·Job Pack·운영 계약은 CPF가 유지한다.

### 25.14 운영 인계

Parameter, Schedule, Capacity, Permission, 대사·재시작·재처리·Rollback 기준을 인계한다.


## 26. Partition·Remote Worker

### 26.1 이 기능으로 만드는 업무 결과

대량 대상을 겹치지 않는 Partition으로 나누고 여러 Worker가 Lease·Fencing으로 처리한다.

### 26.2 선택 기준과 사용하지 말아야 할 경우

단일 Node 처리 시간이 목표 시간을 넘고 대상 분할 Key가 안정적일 때 사용한다.

### 26.3 주 사용자와 권한

배치 개발자·운영자·승인자 역할을 분리하며 실행·중지·재처리·ABANDON·배포 Permission을 구분한다.

### 26.4 시작 전에 결정할 값

Partition Key, 범위, Grid Size, Worker Pool, Lease TTL, Heartbeat, Fencing, Skew 기준을 정한다.

### 26.5 작업 후 만들어지는 결과물

Partition·Remote Worker Definition, Metadata, Config, Test, ADM 화면·Runbook.

### 26.6 단계별 절차

Manager가 대상 Snapshot과 Partition Manifest를 만든다. Worker는 Claim 후 Fencing Token을 포함해 Write하고 Heartbeat한다. 만료 Worker 결과는 현재 Token과 비교해 거부한다.

### 26.7 입력값·기본값·허용 범위

입력은 Job Definition의 Parameter Schema를 따르고 필수·기본값·형식·식별 여부를 명시한다.

### 26.8 정상 결과와 완료 판정

모든 대상이 정확히 한 Partition에 속하고 중복·누락이 없으며 Worker별 결과 합계가 전체와 같다.

### 26.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 실행, Lock 경쟁, Timeout, Process 종료, 응답 유실, 부분 대상 실패를 각 상태로 분리한다.

### 26.10 재시도·재시작·재처리·대사·보상·되돌리기

Worker 유실은 Lease 만료 후 미완료 범위만 재할당한다. Stale Writer는 Fencing으로 차단하고 성공 Partition은 유지한다.

### 26.11 로그·지표·추적·감사

Job/Step/Operation/Partition/Worker ID와 건수·금액·Hash·Retry·Lease·Audit를 기록한다.

### 26.12 교육 예제

`EDU-BAT-04·05·24·25`를 실행하고 정상·Process Kill·응답 유실·부분 실패를 재현한다.

### 26.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 계산과 대상 선정은 조직 영역이다. Batch Metadata·Lease·Job Pack·운영 계약은 CPF가 유지한다.

### 26.14 운영 인계

Parameter, Schedule, Capacity, Permission, 대사·재시작·재처리·Rollback 기준을 인계한다.


## 27. Scheduler·Misfire

### 27.1 이 기능으로 만드는 업무 결과

영업일·시간대·Calendar에 따라 Job을 한 번만 발화하고 누락·중복·지연 실행을 정책대로 처리한다.

### 27.2 선택 기준과 사용하지 말아야 할 경우

정기 실행이 필요할 때 사용한다. 단발 운영 실행은 승인된 수동 실행을 사용한다.

### 27.3 주 사용자와 권한

배치 개발자·운영자·승인자 역할을 분리하며 실행·중지·재처리·ABANDON·배포 Permission을 구분한다.

### 27.4 시작 전에 결정할 값

Cron/Calendar, Timezone, Misfire, 주 실행 Node, 동시 실행, Holiday, Catch-up 한도를 정한다.

### 27.5 작업 후 만들어지는 결과물

Scheduler·Misfire Definition, Metadata, Config, Test, ADM 화면·Runbook.

### 27.6 단계별 절차

Schedule을 초안·검증·승인·게시한다. Trigger Claim과 실행 요청을 별도 ID로 기록하고 실제 Job Execution 연결을 확인한다.

### 27.7 입력값·기본값·허용 범위

입력은 Job Definition의 Parameter Schema를 따르고 필수·기본값·형식·식별 여부를 명시한다.

### 27.8 정상 결과와 완료 판정

예정 Trigger와 실제 Job Execution이 1:1로 연결되고 Timezone·영업일이 맞는다.

### 27.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 실행, Lock 경쟁, Timeout, Process 종료, 응답 유실, 부분 대상 실패를 각 상태로 분리한다.

### 27.10 재시도·재시작·재처리·대사·보상·되돌리기

Misfire는 SKIP·RUN_ONCE·CATCH_UP 중 정책을 적용한다. 응답 유실 시 Trigger와 Job Instance를 조회하고 새 실행을 만들지 않는다.

### 27.11 로그·지표·추적·감사

Job/Step/Operation/Partition/Worker ID와 건수·금액·Hash·Retry·Lease·Audit를 기록한다.

### 27.12 교육 예제

`EDU-BAT-07·20·22`를 실행하고 정상·Process Kill·응답 유실·부분 실패를 재현한다.

### 27.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 계산과 대상 선정은 조직 영역이다. Batch Metadata·Lease·Job Pack·운영 계약은 CPF가 유지한다.

### 27.14 운영 인계

Parameter, Schedule, Capacity, Permission, 대사·재시작·재처리·Rollback 기준을 인계한다.


## 28. Center-Cut

### 28.1 이 기능으로 만드는 업무 결과

다수 계정·계약에 같은 업무 규칙을 대량 적용하기 전에 대상·건수·금액을 Preview하고 승인 후 실행한다.

### 28.2 선택 기준과 사용하지 말아야 할 경우

업무 영향이 크고 대상 확정·승인·대사가 필요한 대량 변경에 사용한다.

### 28.3 주 사용자와 권한

배치 개발자·운영자·승인자 역할을 분리하며 실행·중지·재처리·ABANDON·배포 Permission을 구분한다.

### 28.4 시작 전에 결정할 값

Cut ID, 기준일, 대상 Query, Partition, Chunk, Commit, Exclusion, Approval, 대사·보정 기준을 정한다.

### 28.5 작업 후 만들어지는 결과물

Center-Cut Definition, Metadata, Config, Test, ADM 화면·Runbook.

### 28.6 단계별 절차

Snapshot과 대상 Manifest를 만들고 Preview·Sample을 승인받는다. 실행 중 대상 Version을 확인하고 결과를 성공·실패·미확정으로 분리한다.

### 28.7 입력값·기본값·허용 범위

입력은 Job Definition의 Parameter Schema를 따르고 필수·기본값·형식·식별 여부를 명시한다.

### 28.8 정상 결과와 완료 판정

승인한 Snapshot과 실제 처리 대상·건수·금액이 일치하고 차이는 보정 작업으로 연결된다.

### 28.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 실행, Lock 경쟁, Timeout, Process 종료, 응답 유실, 부분 대상 실패를 각 상태로 분리한다.

### 28.10 재시도·재시작·재처리·대사·보상·되돌리기

일부 실패는 성공 대상을 유지하고 실패·미확정만 재처리한다. 대상 Snapshot이 달라졌으면 새 승인 없이 재실행하지 않는다.

### 28.11 로그·지표·추적·감사

Job/Step/Operation/Partition/Worker ID와 건수·금액·Hash·Retry·Lease·Audit를 기록한다.

### 28.12 교육 예제

`EDU-BAT-06·26·29`를 실행하고 정상·Process Kill·응답 유실·부분 실패를 재현한다.

### 28.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 계산과 대상 선정은 조직 영역이다. Batch Metadata·Lease·Job Pack·운영 계약은 CPF가 유지한다.

### 28.14 운영 인계

Parameter, Schedule, Capacity, Permission, 대사·재시작·재처리·Rollback 기준을 인계한다.


## 29. Job Pack·Runner·Worker·Agent

### 29.1 이 기능으로 만드는 업무 결과

승인된 Job Artifact와 Manifest를 Runner·Worker·Agent에 배포하고 Version·Checksum·ACK/NACK를 대사한다.

### 29.2 선택 기준과 사용하지 말아야 할 경우

분리 Process·Host에서 Batch를 실행하거나 Job Version을 통제할 때 사용한다.

### 29.3 주 사용자와 권한

배치 개발자·운영자·승인자 역할을 분리하며 실행·중지·재처리·ABANDON·배포 Permission을 구분한다.

### 29.4 시작 전에 결정할 값

Artifact URI, SHA-256, CPF 호환 범위, Java/Profile, Secret 참조, 실행 명령 Allowlist, LKG를 정한다.

### 29.5 작업 후 만들어지는 결과물

Job Pack·Runner·Worker·Agent Definition, Metadata, Config, Test, ADM 화면·Runbook.

### 29.6 단계별 절차

Job Pack을 생성·서명·검증하고 Agent별 Preview 후 배포한다. Runner가 Operation을 만들고 Worker가 Lease를 획득해 실행한다.

### 29.7 입력값·기본값·허용 범위

입력은 Job Definition의 Parameter Schema를 따르고 필수·기본값·형식·식별 여부를 명시한다.

### 29.8 정상 결과와 완료 판정

모든 대상 Agent의 Version·Checksum이 승인본과 일치하고 허용 명령만 실행된다.

### 29.9 중복·동시성·시간초과·응답 유실·부분 실패

중복 실행, Lock 경쟁, Timeout, Process 종료, 응답 유실, 부분 대상 실패를 각 상태로 분리한다.

### 29.10 재시도·재시작·재처리·대사·보상·되돌리기

ACK 유실은 Agent 상태를 조회한다. 일부 적용은 성공 Agent를 반복하지 않고 실패 대상만 재적용하거나 LKG로 Rollback한다.

### 29.11 로그·지표·추적·감사

Job/Step/Operation/Partition/Worker ID와 건수·금액·Hash·Retry·Lease·Audit를 기록한다.

### 29.12 교육 예제

`EDU-BAT-08·27·28`를 실행하고 정상·Process Kill·응답 유실·부분 실패를 재현한다.

### 29.13 조직 고유 업무로 바꿀 부분과 CPF가 유지하는 부분

업무 계산과 대상 선정은 조직 영역이다. Batch Metadata·Lease·Job Pack·운영 계약은 CPF가 유지한다.

### 29.14 운영 인계

Parameter, Schedule, Capacity, Permission, 대사·재시작·재처리·Rollback 기준을 인계한다.


## 30. Job 설계서 표준 양식

| 항목 | 작성 내용 | 검수 질문 |
|---|---|---|
| Job ID·Version | 고유 ID와 호환 범위 | 진행 중 실행의 Version 의미가 유지되는가 |
| 업무 목적 | 처리 대상과 결과 | 업무 Owner가 결과를 판정할 수 있는가 |
| Parameter | 이름·Type·필수·식별 | 같은 Job Instance를 구분하는가 |
| Step 흐름 | 순서·분기·중단 | 실패 Step 이후 재시작 의미가 명확한가 |
| Reader | Query·정렬·Snapshot | 재시작 중 중복·누락이 없는가 |
| Processor | 규칙·Validation | 부수 효과가 없는가 |
| Writer | Table·Commit | 멱등·Version·Lock이 있는가 |
| Retry·Skip | 예외 분류·한도 | 업무 오류를 Skip하지 않는가 |
| Partition | Key·범위·Grid | 겹침·누락·Skew를 검출하는가 |
| Capacity | 건수·시간·자원 | 목표 시간과 제한값이 있는가 |
| 대사 | 건수·금액·Hash | 메타데이터와 업무 원장을 비교하는가 |
| 운영 | ADM·Alert·Runbook | 중지·재시작·재처리 책임이 있는가 |

## 31. 실행 상태와 허용 행동

| 상태 | 의미 | 허용 행동 | 금지 행동 |
|---|---|---|---|
| STARTING | 실행 준비 | 조회·취소 요청 | 같은 Instance 재실행 |
| STARTED | 처리 중 | 진행 조회·정상 Stop | 강제 DB 상태 변경 |
| STOPPING | Checkpoint 종료 중 | 대기·진행 조회 | 새 Worker 강제 투입 |
| STOPPED | 재시작 가능 중지 | Restart | 새 Parameter로 중복 실행 |
| FAILED | 실패 확정 | 원인 수정·Restart/Reprocess | 원인 없이 반복 실행 |
| UNKNOWN_RESULT | 실제 결과 미확정 | Reconcile | 성공·실패 임의 확정 |
| COMPLETED | 업무·메타데이터 완료 | 대사·보존 | 같은 Instance 재실행 |
| ABANDONED | 승인된 재시작 금지 | 새 Instance 설계 | Restart |

## 32. Batch 운영 한 줄 검증 명령

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'; git -C $repo status --short; & (Join-Path $repo 'gradlew.bat') :cpf-batch:test; if($LASTEXITCODE -ne 0){throw 'Batch Test 실패'}
```

## 33. Batch Job 정의서 완성 예시

| 항목 | 예시 | 완료 판정 |
|---|---|---|
| Job ID/Version | `PAY_MONTHLY_FEE` / `2026.08.01` | 게시 Version과 Artifact Manifest 일치 |
| Parameter | `businessDate`, `partitionCount`, `runType` | 식별 Parameter와 비식별 Parameter 구분 |
| Reader | 상태=READY, 기준일 이하, PK Cursor | 정렬·재시작 위치 결정적 |
| Processor | 수수료 계산·Validation | 같은 입력은 같은 결과 |
| Writer | 업무 결과 Upsert + 처리 이력 | Chunk Transaction과 Idempotency 보장 |
| Chunk/Commit | 500 / 500 | 성능·Rollback 범위 시험 |
| Retry | 일시 DB/Network 3회 | 업무 오류는 Retry 제외 |
| Skip | 형식 오류를 오류 원장에 기록 | Skip 한도와 최종 대사 포함 |
| Checkpoint | 마지막 PK·건수·금액·Hash | Restart 후 중복·누락 0 |
| Concurrency | 같은 businessDate 단일 실행 | Job Key·Lease·Fencing |
| Stop/Restart | 현재 Chunk Commit 후 Stop | 새 JobInstance 생성 금지 |
| Reconcile | 대상/처리/성공/실패/미확정 건수·금액·Hash | 업무 원장과 Batch Metadata 일치 |
| ADM | batch-executions·recovery·audit | Operation·실행·대사 연결 |

## 34. Worker·Lease·Fencing 실행 계약

1. Control Server가 Partition을 생성하고 고유 Partition Key와 입력 범위를 저장한다.
2. Worker는 Claim 시 Owner ID, Lease Expiry, Fencing Token을 받는다.
3. Heartbeat는 같은 Token으로 Lease를 연장한다.
4. Lease가 만료돼 새 Worker가 Claim하면 Token을 증가시킨다.
5. 이전 Worker의 늦은 Write·ACK는 낮은 Token이므로 거부한다.
6. Process Kill 뒤 마지막 Commit·Checkpoint·Target 결과를 대사한다.
7. 재할당은 미완료 Partition만 대상으로 하고 성공 Partition은 반복하지 않는다.
8. 모든 Partition의 건수·금액·Hash와 Job 합계가 맞을 때 Job을 완료한다.

## 35. Scheduler Misfire 결정표

| 상황 | SKIP | RUN_ONCE | CATCH_UP | 판정 |
|---|---|---|---|---|
| 업무일 마감이 이미 수동 완료 | 선택 | 금지 | 금지 | 중복 마감 차단 |
| 한 번 누락된 일일 집계 | 선택 가능 | 권장 | 정책에 따라 | 기준일 Parameter 고정 |
| 여러 기간의 독립 집계 | 금지 | 부분 | 권장 | 기간별 JobInstance 분리 |
| 외부기관 전송 | 정책에 따라 | 승인 후 | 대체로 금지 | 기관 결과 대사 선행 |
| 대량 Center-Cut | 금지 | Preview·승인 후 | 금지 | 대상 Snapshot 재생성 |

## 36. Center-Cut 승인·실행·대사 양식

| 단계 | 입력 | 저장 상태 | 정상 결과 | 실패 시 |
|---|---|---|---|---|
| Definition | 대상 Query·Partition Key·처리기·대사식 | DRAFT | Version 생성 | 정의 수정 |
| Validate | SQL Plan·권한·용량·금지 조건 | VALIDATED | 오류 0 | 검증 오류 수정 |
| Preview | 기준시각·대상수·금액·Hash·표본 | PREVIEWED | Snapshot 고정 | 대상 변경 시 재Preview |
| Approval | Requester·Approver·Reason·Snapshot Hash | APPROVED | 승인 유효 | 만료/변경 시 재승인 |
| Execute | Execution ID·Worker Pool·Lease | RUNNING | 진행률·Target 결과 | Stop/UNKNOWN_RESULT |
| Reconcile | 대상·성공·실패·미확정 합계 | RECONCILING | 차이 0 | 실패 Target만 재처리 |
| Close | 최종 합계·Audit·Artifact | COMPLETED | 업무 원장 일치 | 운영 확정/보상 |

## 37. Batch 장애 주입 시나리오

| 장애 | 주입 시점 | 관찰 | 허용 행동 | 완료 기준 |
|---|---|---|---|---|
| Process Kill | Reader 후/Commit 전/Commit 후 | Metadata·Checkpoint·업무 원장 | Restart | 중복·누락 0 |
| DB Timeout | Read/Write/Metadata | Transaction Rollback·Retry | 일시 오류만 Retry | 업무 오류와 분리 |
| Worker Lease Lost | 처리 중 | Fencing Token·늦은 Write | 재할당 | Stale Write 0 |
| ACK Loss | Control↔Worker | Target 상태·Worker 상태 | Reconcile | 같은 Partition 중복 실행 없음 |
| Disk Full | File/Temp/Log | Partial File·Checkpoint | 공간 확보·Resume/Restart | 손상 Artifact 격리 |
| Misfire | Scheduler 중단 | 누락 Trigger·기준일 | 정책별 Skip/RunOnce/CatchUp | 중복 JobInstance 0 |
| Partial Result | 일부 Partition 실패 | Target별 상태 | 실패만 Reprocess | 합계 차이 0 |
| UNKNOWN_RESULT | 외부 Write 후 응답 유실 | Attempt·상대 원장 | Reconcile | 확정 전 재전송 0 |
