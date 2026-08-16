# CPF Batch 개발 기능 TOP 50

> 실제 `cpf-batch/api` Public 계약만 기준으로 정리한 교육/개발용 빠른 선택표입니다. Runtime 내부 구현 클래스는 수량을 채우기 위해 넣지 않습니다.

## 먼저 기억할 배치 개발 흐름

```text
@CpfBatchJob / @CpfBatchStep
  → Job·Step·Parameter 정의
  → CpfBatchExecutionRequest
  → BatchStepHandler / FileProcessHandler
  → Ledger + Fencing + UNKNOWN/Reconcile
  → Operations / ADM에서 추적
```

## TOP 50

| No | 용도 | 기능/API | Level | 언제 사용 | Source |
|---:|---|---|---|---|---|
| 1 | 배치 선언 | `@CpfBatchJob` | golden | 배치 Job 클래스를 CPF Job으로 선언 | `cpf-batch/api/src/main/java/com/cpf/batch/api/annotation/CpfBatchJob.java` |
| 2 | 배치 선언 | `@CpfBatchStep` | golden | 배치 Step 클래스를 CPF Step으로 선언 | `cpf-batch/api/src/main/java/com/cpf/batch/api/annotation/CpfBatchStep.java` |
| 3 | Job 정의 | `BatchJobDefinition` | golden | Job 정의와 실행 정책을 표현 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchJobDefinition.java` |
| 4 | Job 정의 | `BatchStepDefinition` | golden | Job의 Step 정의를 표현 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchStepDefinition.java` |
| 5 | 파라미터 | `BatchParameterDefinition` | golden | 배치 입력 파라미터 계약을 정의 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchParameterDefinition.java` |
| 6 | 파라미터 | `BatchParameterDefinition.validate(...)` | golden | 입력값을 정의된 형식·범위로 검증 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchParameterDefinition.java` |
| 7 | 파라미터 | `BatchParameterDefinition.effectiveValue(...)` | capability | 입력값과 기본값을 기준으로 실제 적용값 계산 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchParameterDefinition.java` |
| 8 | 파라미터 | `BatchJobDefinition.requireParameter(...)` | golden | 필수 Job 파라미터를 조회하고 누락을 즉시 실패 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchJobDefinition.java` |
| 9 | 실행 정책 | `BatchExecutionPolicy.defaults()` | capability | 기본 배치 실행 정책 사용 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionPolicy.java` |
| 10 | 실행 정책 | `BatchExecutionTopology` | capability | 실행 Topology 계약을 선택·표현 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionTopology.java` |
| 11 | 실행 요청 | `CpfBatchExecutionRequest.run(...)` | golden | 일반 수동/온라인 Job 실행 요청 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchExecutionRequest.java` |
| 12 | 실행 요청 | `CpfBatchExecutionRequest.scheduledRun(...)` | capability | Scheduler 기반 실행 요청 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchExecutionRequest.java` |
| 13 | 실행 요청 | `CpfBatchExecutionRequest.retry(...)` | golden | 실패 실행의 멱등 Retry 요청 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchExecutionRequest.java` |
| 14 | 실행 요청 | `CpfBatchExecutionRequest.restart(...)` | capability | Checkpoint/상태를 고려한 Restart 요청 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchExecutionRequest.java` |
| 15 | 실행 요청 | `CpfBatchExecutionRequest.stop(...)` | capability | 실행 중지 요청 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchExecutionRequest.java` |
| 16 | 실행 요청 | `CpfBatchExecutionRequest.onDemand(...)` | capability | On-demand 실행 요청 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchExecutionRequest.java` |
| 17 | 실행 결과 | `CpfBatchExecutionResult` | golden | 배치 실행 결과 표준 계약 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchExecutionResult.java` |
| 18 | 실행 결과 | `CpfBatchOutcome` | golden | 성공/실패/UNKNOWN 결과 의미를 표준화 | `cpf-batch/api/src/main/java/com/cpf/batch/api/error/CpfBatchOutcome.java` |
| 19 | Context | `CpfBatchContext` | golden | Job/Step 실행 Context 전달 | `cpf-batch/api/src/main/java/com/cpf/batch/context/CpfBatchContext.java` |
| 20 | Context | `CpfBatchContext.withJobExecution(...)` | capability | Job execution 정보를 포함한 새 Context 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/context/CpfBatchContext.java` |
| 21 | Context | `CpfBatchContextBundle` | capability | Batch/Center-Cut Context를 함께 전달 | `cpf-batch/api/src/main/java/com/cpf/batch/context/CpfBatchContextBundle.java` |
| 22 | Context | `CpfBatchContextBundle.withCenterCut(...)` | capability | Center-Cut Context를 bundle에 결합 | `cpf-batch/api/src/main/java/com/cpf/batch/context/CpfBatchContextBundle.java` |
| 23 | Context | `CpfCenterCutContext` | capability | Center-Cut 실행 단위 Context | `cpf-batch/api/src/main/java/com/cpf/batch/context/CpfCenterCutContext.java` |
| 24 | Step 구현 | `BatchStepHandler` | golden | 업무 Step 처리 SPI | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchStepHandler.java` |
| 25 | Step 구현 | `BatchStepHandler.execute(...)` | golden | 업무 Step 실행 구현 | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchStepHandler.java` |
| 26 | File 처리 | `FileProcessHandler` | capability | File 기반 배치 처리 SPI | `cpf-batch/api/src/main/java/com/cpf/batch/spi/FileProcessHandler.java` |
| 27 | File 처리 | `FileProcessHandler.process(...)` | capability | File process 실행 구현 | `cpf-batch/api/src/main/java/com/cpf/batch/spi/FileProcessHandler.java` |
| 28 | Job 제공 | `BusinessJobProvider` | capability | 업무 Job 정의를 Runtime에 제공 | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BusinessJobProvider.java` |
| 29 | Event | `CpfBatchEventPublisher.publish(...)` | capability | 배치 상태 Event 발행 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchEventPublisher.java` |
| 30 | Event | `CpfBatchEvent.now(...)` | capability | 현재 시점의 표준 배치 Event 생성 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchEvent.java` |
| 31 | 실행 제어 | `BatchExecutionControlPort.start(...)` | golden | 승인된 실행 시작 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionControlPort.java` |
| 32 | 실행 제어 | `BatchExecutionControlPort.stop(...)` | capability | 실행 중지 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionControlPort.java` |
| 33 | 실행 제어 | `BatchExecutionControlPort.restart(...)` | capability | 실행 재시작 | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionControlPort.java` |
| 34 | 실행 제어 | `BatchExecutionControlPort.abandon(...)` | advanced | 운영 판단에 따라 실행 abandon | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionControlPort.java` |
| 35 | 실행 제어 | `BatchExecutionControlPort.reconcile(...)` | golden | UNKNOWN/부분 실패 실행을 reconcile | `cpf-batch/api/src/main/java/com/cpf/batch/api/BatchExecutionControlPort.java` |
| 36 | 신뢰성 | `BatchExecutionLedgerPort.reserve(...)` | capability | 실행 예약을 멱등 Ledger에 기록 | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java` |
| 37 | 신뢰성 | `BatchExecutionLedgerPort.transition(...)` | capability | 실행 상태를 유효한 전이로 갱신 | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java` |
| 38 | 신뢰성 | `BatchExecutionLedgerPort.recordUnknown(...)` | golden | UNKNOWN_RESULT를 실패로 덮지 않고 별도 기록 | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java` |
| 39 | 신뢰성 | `BatchExecutionLedgerPort.findReservation(...)` | capability | 기존 reservation/idempotency 상태 조회 | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchExecutionLedgerPort.java` |
| 40 | 신뢰성 | `BatchFencingPort.assertCurrent(...)` | golden | stale worker/lease의 중복 효과를 fencing | `cpf-batch/api/src/main/java/com/cpf/batch/spi/BatchFencingPort.java` |
| 41 | 신뢰성 | `CpfBatchOwnerUnknownResultException` | golden | Owner 결과 미확정 상태를 명시적으로 전달 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchOwnerUnknownResultException.java` |
| 42 | 운영 조회 | `CpfBatchOperationsPort.findJobs(...)` | capability | Job 목록 조회 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchOperationsPort.java` |
| 43 | 운영 조회 | `CpfBatchOperationsPort.findExecutions(...)` | capability | 실행 이력 조회 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchOperationsPort.java` |
| 44 | 운영 조회 | `CpfBatchOperationsPort.findExecutionDetail(...)` | capability | 실행 상세/추적 정보 조회 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchOperationsPort.java` |
| 45 | 운영 조회 | `CpfBatchOperationsPort.findRecoverySnapshot(...)` | capability | 복구·UNKNOWN 현황 snapshot 조회 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchOperationsPort.java` |
| 46 | 운영 실행 | `CpfBatchOperationsPort.requestRun(...)` | capability | 운영/API 경로에서 Job 실행 요청 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchOperationsPort.java` |
| 47 | 운영 실행 | `CpfBatchOperationsPort.requestRetry(...)` | capability | 운영/API 경로에서 Retry 요청 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfBatchOperationsPort.java` |
| 48 | Center-Cut | `CpfCenterCutOperationsPort.findTargets(...)` | capability | Center-Cut 대상 조회 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfCenterCutOperationsPort.java` |
| 49 | Center-Cut | `CpfCenterCutOperationsPort.findResults(...)` | capability | Center-Cut 결과 조회 | `cpf-batch/api/src/main/java/com/cpf/batch/api/CpfCenterCutOperationsPort.java` |
| 50 | Center-Cut | `CpfCenterCutResult.unknown(...)` | golden | 대상 처리 결과가 확정되지 않았음을 보존 | `cpf-batch/api/src/main/java/com/cpf/batch/api/centercut/CpfCenterCutResult.java` |

## 선택 기준

- **golden**: 대부분의 배치 업무개발자가 먼저 알아야 하는 표준 경로입니다.
- **capability**: 해당 기능을 사용할 때 선택적으로 학습합니다.
- **advanced**: 운영·복구 등 제한된 고급 경로에서 사용합니다.

## 자주 쓰는 개발·검증 Entry Point

```powershell
.\gradlew.bat cpfVerifyFast
.\gradlew.bat cpfVerifyTargeted -PcpfTargetCapabilities=batch
.\gradlew.bat cpfRunBatch
pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\verification\tools\run-cpf-local-full-validation.ps1 -ResourceProfile local -OutputRoot "$HOME\Downloads" -FullLocal
```

일상 개발은 Fast → Batch Targeted 순으로 확인하고, Process Kill·2 Worker·UNKNOWN/Reconcile·Kafka·DB3가 필요한 최종 검증은 FullLocal에서 수행합니다.
