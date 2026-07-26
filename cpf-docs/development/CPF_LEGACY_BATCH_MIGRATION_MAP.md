# CPF Legacy Batch Migration Map

## 1. 기준과 판정 원칙

- 삭제 기준 Source: 기준 SHA `e725ed3f1bc203e28ff6f06c62a69583358d3b6a`의 `cpf-batch/src/**`
- 삭제 대상: 146개 추적 파일
- 최종 Runtime Owner: `cpf-batch` 아래 5개 독립 Runtime과 3개 Library
- Batch EDU Owner: `cpf-reference`
- 실제 Batch 명령 경계: `cpf-core`의 `CpfBatchOperationsPort`
- Center-Cut 업무 경계: `cpf-core`의 `api.centercut`과 `spi.centercut`

Legacy Class를 이름만 바꿔 복제하지 않는다. 독립 Runtime으로 대체된 기능은 해당
Runtime 구현으로, 업무 Job/Step과 교육 Scenario는 REF로, 공통 호출 계약은 CPF 공개
API/SPI로 이관한다. 단일 Boot Application, Legacy Profile, 내부 DB 직접 접근과
in-process Runtime Bean은 제거 대상이다.

## 2. 전체 파일 분류와 대체 경로

아래 17개 행의 Legacy 파일 수 합계는 146개다.

| Legacy Source | 수 | Requirement | 새 Owner | 대체 Source | Consumer | Test | Runtime | 상태 |
|---|---:|---|---|---|---|---|---|---|
| `main/java/.../BatApplication.java` | 1 | 역할별 독립 실행 | BAT Runtime 5종 | `control-server/.../BatchControlServerApplication.java`, `scheduler/.../BatchSchedulerApplication.java`, `worker/.../BatchWorkerApplication.java`, `center-cut-runner/.../CenterCutRunnerApplication.java`, `host-agent/.../BatchHostAgentApplication.java` | Deploy inventory, Runtime start/stop 도구 | `verifyStandaloneArtifacts`, `check-r11-runtime-entrypoints.ps1` | 5개 독립 Boot JAR | 완료 |
| `main/java/.../common/**` | 2 | 중복 Base 제거 | 각 Owner | 별도 대체 없음. REST/Application은 역할별 Component, EDU는 `ReferenceBaseController/Service` 사용 | BAT/REF | Module compile/test | 각 독립 Runtime, REF | 완료 |
| `main/java/.../config/**` | 4 | DataSource, Spring Batch 저장소, Runtime identity | `runtime-common`, 각 Runtime, REF JobDefinition | `BatDataSourceConfiguration`, `RuntimeCommonConfiguration`, 각 `application.yml`, `ReferenceBatchRepositoryConfig` | 5개 Runtime, REF local EDU | `RuntimeIdentityFactoryTest`, `ReferenceBatchRepositoryConfigTest`, `ReferenceBatchEducationConfigTest` | BAT 5종, REF | 완료 |
| `main/java/.../edu/**` | 35 | Batch/Center-Cut 교육 | REF | `reference/batch/**`, `reference/centercut/**`, `ReferenceArchiveEducationSample`, `reference/idempotency/**`, `reference/servicecall/**`, `reference/transaction/**`, `reference/catalog/**` | REF REST API와 공개 CPF API/SPI | `ReferenceBatch*Test`, `ReferenceCenterCutAdapterTest`, 대응 REF Education Test | REF | 완료 |
| `main/java/.../job/**` | 11 | JobDefinition, 실패/heartbeat/Center-Cut 업무 샘플 | REF 업무 Job/Step, BAT Center-Cut Runtime | `ReferenceBatchEducationConfig`, `ReferenceCenterCutHandler`, `ReferenceCenterCutTargetRepository`, `JobPackCatalog`, `CenterCutRuntime` | REF 교육 Controller, BAT Worker/Runner | `ReferenceBatchEducationConfigTest`, `ReferenceCenterCutAdapterTest`, `CenterCutRuntimeLeaseLossTest` | REF, Worker, Center-Cut Runner | 완료 |
| `main/java/.../operation/**` | 10 | 실행/조회/중지/재시도/상태 API | Control Server | `BatchOperationsCompatibilityService`, `BatInternalOperationsController`, `RuntimeControlController`, `BatchControlQueryController` | ADM Remote Adapter와 `CpfBatchOperationsPort` Consumer | `AdmBatchOperationServiceTest`, `AdmBatchOperationsClientConfigurationTest`, REF Controller Test | Control Server | 완료 |
| `main/java/.../retention/**` | 3 | BAT 운영 로그 보존 | Control Server | `control-server/.../retention/BatOperationLogRetentionHandler`, `BatRetentionController`, `BatRetentionOperations` | ADM/BAT 운영 명령 | `AdmBatchJobLogServiceTest` 및 Control Server compile | Control Server | 부분 구현 |
| `main/java/.../runtime/*.java` | 10 | Runtime 등록, heartbeat, ghost, lock, launch, log | `runtime-common`, Worker, Control Server, CPF 공개 로그 계약 | `RuntimeReporter`, `RuntimeStateProvider`, `RuntimeIdentityFactory`, `WorkerRuntime`, `JdbcWorkerLeaseRepository`, `JdbcRuntimeRegistry`, `JobPackDispatcher`, `CpfBatchLogPaths` | BAT 5종, ADM, REF log EDU | `RuntimeIdentityFactoryTest`, `WorkerRuntimeLeaseLossTest`, `JobPackDispatcherFencingTest`, `ReferenceAdmBatchLogQueryEducationSampleTest` | BAT 5종 | 완료 |
| `main/java/.../runtime/centercut/**` | 9 | Claim/lease/dispatch/result/recovery | Center-Cut Runner, Control Server, REF Handler | `CenterCutRuntime`, `CenterCutDispatcher`, `JdbcCenterCutClaimRepository`, `CenterCutLifecycleReconciler`, `CenterCutExecutionService`, REF Center-Cut API/SPI Adapter | Control Server와 REF 업무 Handler | `CenterCutRuntimeLeaseLossTest`, `ReferenceCenterCutAdapterTest`, `AdmCenterCutOperationServiceTest` | Center-Cut Runner, Control Server, REF | 완료 |
| `main/java/.../scheduler/**` | 4 | Schedule, leader election, dispatch | Scheduler | `SchedulerCoordinator`, `SchedulerDispatchService`, `JdbcSchedulerLeaderRepository` | Control Server 등록 Schedule, Worker queue | `SchedulerCoordinatorFencingTest`, `SchedulerAvailableWindowTest` | Scheduler | 완료 |
| `main/java/.../worker/**` | 9 | Worker identity, lease, dispatch, 실행 | Worker | `WorkerRuntime`, `WorkerRegistryReporter`, `JobPackDispatcher`, `JdbcWorkerLeaseRepository`, `JdbcWorkerExecutionRepository` | Scheduler/Control Server, Job Pack | `WorkerRuntimeLeaseLossTest`, `JobPackDispatcherFencingTest` | Worker | 완료 |
| `main/resources/application*.yml` | 7 | 독립 Profile/Port/Instance/Log | BAT Runtime 5종, runtime-common | 각 Runtime `src/main/resources/application.yml`, `application-bat-runtime.yml`, Deploy env/inventory | Runtime Launcher와 Deploy Tool | `check-runtime-config-standard.ps1`, `check-profile-loading.ps1` | BAT 5종 | 완료 |
| `test/java/.../config/**` | 2 | DataSource/Repository 회귀 | runtime-common, REF | 위 Config 대체 Source의 Test | Gradle Test | `RuntimeIdentityFactoryTest`, `ReferenceBatchRepositoryConfigTest` | Test | 완료 |
| `test/java/.../edu/**` | 27 | EDU Source 대응 검증 | REF | REF Education Test 전체와 Generated Coverage Matrix | `check-sample-coverage.ps1` | `cpf-reference:test` | REF Test | 완료 |
| `test/java/.../job/**` | 2 | Job/Center-Cut 업무 검증 | REF, Center-Cut Runner | `ReferenceBatchEducationConfigTest`, `ReferenceCenterCutAdapterTest` | REF Job Pack/Runner | 동일 Test | REF, Center-Cut Runner | 완료 |
| `test/java/.../operation/**` | 3 | Health, 명령 안전성, 실행 상태 | Control Server, ADM Adapter | Control Server health/readiness와 public port 구현, ADM/REF Consumer Test | ADM, REF | `AdmBatchOperationServiceTest`, `AdmBatchOperationsClientConfigurationTest`, `ReferenceBatchEducationControllerTest` | Control Server | 완료 |
| `test/java/.../runtime/**`, `test/java/.../worker/**` | 7 | lease, fencing, lock, restart, worker 회귀 | runtime-common, Scheduler, Worker, Center-Cut Runner | 역할별 Runtime Test | 역할별 Runtime | `SchedulerCoordinatorFencingTest`, `WorkerRuntimeLeaseLossTest`, `JobPackDispatcherFencingTest`, `CenterCutRuntimeLeaseLossTest` | BAT Runtime Test | 완료 |

`retention` 행은 독립 Control Server 구현과 Consumer는 존재하지만 전용 단위 Test가 없어
`부분 구현`으로 유지한다. Compile 성공만으로 전용 보존 정책 동작을 완료 처리하지 않는다.

### 2.1 요구 기능별 추적

| 요구 기능 | Legacy 책임 | 최종 Owner/경계 | 정적 대체 근거 | 완료 판정 |
|---|---|---|---|---|
| Batch Operations | 단일 JVM Controller/Facade | Control Server + `CpfBatchOperationsPort` | `BatchOperationsCompatibilityService`, `BatInternalOperationsController` | Owner/Consumer Test 필요 |
| Job | Smoke/Failure/Heartbeat/On-demand Job | REF Job/Step Definition + Worker Job Pack | `ReferenceBatchEducationConfig`, `JobPackCatalog`, `JobPackDispatcher` | REF/BAT Test 필요 |
| Execution | Launch/상태/Step 조회 | Control Server Query + Worker Execution Repository | `BatchControlQueryController`, `JdbcWorkerExecutionRepository` | 중앙 Query Pack/Test 필요 |
| Scheduler | 후보 생성과 trigger | Scheduler | `SchedulerCoordinator`, `SchedulerDispatchService`, `JdbcSchedulerLeaderRepository` | fencing/window Test 필요 |
| Worker | 등록/lease/dispatch | Worker | `WorkerRuntime`, `WorkerRegistryReporter`, `JobPackDispatcher` | lease/fencing Test 필요 |
| Lock | in-process lock | BAT lease/fencing + 승인된 공개 명령 | Worker/Scheduler lease repository, `CpfBatchOperationsPort.releaseLock` | stale owner 거부 Test 필요 |
| Ghost | heartbeat/ghost 판정 | Control Server + Runtime Common | `JdbcRuntimeRegistry`, `RuntimeReporter`, 공개 ghost operations | Control Server Test/Runtime 필요 |
| Retention | BAT 운영 로그 삭제 | Control Server retention | `BatRetentionController`, `BatRetentionOperations`, `BatOperationLogRetentionHandler` | 전용 정책 Test 미구현 |
| Center-Cut | registry/remote transport/run | Center-Cut Runner + Control Server + CPF API/SPI | claim/dispatcher/reconcile와 REF Handler/Target Provider | lease-loss/adapter Test 필요 |
| Recovery | restart/retry/unknown result | BAT Owner command + REF 정책 교육 | `CpfBatchOperationsPort`, `ReferenceBatchPolicyEducationSample` | owner Test와 REF Test 필요 |
| EDU | BAT 내부 구현 복제 | REF | Batch Job/Step, 정책, archive/idempotency/service-call/transaction/center-cut REF 자산 | `cpf-reference:test` 필요 |
| ADM 연동 | BAT DB/파일 직접 경로 | ADM 관제 API + 공개 로그 경로 | `CpfBatchLogPaths`, `ReferenceAdmBatchLogQueryEducationSample` | `serverInstanceId` 포함 Test 필요 |
| Audit | 강제 해제/중지/재시도 사유 | ADM 승인·감사 + BAT Owner command | `CpfBatchOperationsPort`의 `requestUser/reason`, ADM Audit | ADM owner-port Test 필요 |
| Test | Legacy 단일 모듈 Test | 8개 BAT Project Test + REF Test | 역할별 Test와 `check-legacy-batch-migration.ps1` | 실제 직렬 실행 전 완료 아님 |

## 3. EDU 이관 경계

REF가 제공하는 것은 다음 두 종류다.

1. `ReferenceBatchEducationConfig`의 업무 Job/Step Definition
2. 공개 CPF API/SPI를 소비하는 Controller, Handler, Target Provider와 정책 교육

REF가 소유하지 않는 것은 다음과 같다.

- Scheduler leader election
- Worker lease와 fencing
- Runtime heartbeat/ghost 처리
- BAT DB 운영 Query
- Center-Cut claim/reconcile Runtime
- Host service install/start/stop

실제 실행·재시도·중지·Schedule 조회는 `CpfBatchOperationsPort`로 BAT Owner에 위임한다.
Center-Cut은 `CpfCenterCutTarget`, `CpfCenterCutResult`, `CenterCutTargetProvider`,
`CenterCutHandler` 경계만 사용한다. REF에서 `com.cpf.batch.runtime`,
`com.cpf.batch.control`, `com.cpf.batch.worker` 같은 Runtime 구현을 import하지 않는다.

REF 서비스 호출 EDU도 MBR/ACC/EXS 같은 특정 Generated Domain의 DTO, URL 또는 서비스
ID를 고정하지 않는다. Typed client 예제는 `ReferenceServiceEcho*`와
`REF-EXTERNAL-SIMULATOR`를 사용해 REF 자체에서 닫힌 중립 시나리오로 검증한다.
ADM Batch 로그 링크는 다중 인스턴스 로그 계약의 필수 축인 `serverInstanceId`를 목록과
상세 요청에 모두 전달한다.

## 4. 삭제 완료 조건

- 실제 Worktree에 `cpf-batch/src`가 없다.
- Gradle에는 8개 BAT 하위 Project만 포함한다.
- BAT 하위 Runtime/Library에 `edu` Package나 `*EducationSample.java`가 없다.
- 범용 EDU Coverage는 `cpf-reference`만 스캔한다.
- 현재 Source에 `com.cpf.core.common.batch` 참조가 없다.
- REF Batch/Center-Cut Source가 BAT Runtime 구현 Package를 import하지 않는다.
- REF Service Call EDU가 특정 Generated Domain 구현, DTO, 서비스 ID와 URL을 고정하지 않는다.
- 독립 BAT Test와 `cpf-reference:test`가 통과한다.

자동 검증은 `cpf-tools/scripts/check-legacy-batch-migration.ps1`를 사용한다.

정적 Gate 이후 실행 검증은 메모리 충돌을 피하도록 다음 순서로 직렬 수행한다.

```powershell
.\gradlew.bat :cpf-reference:test --no-daemon --max-workers=1
.\gradlew.bat :cpf-batch:contract:test :cpf-batch:runtime-common:test `
  :cpf-batch:control-server:test :cpf-batch:scheduler:test :cpf-batch:worker:test `
  :cpf-batch:center-cut-runner:test :cpf-batch:host-agent:test :cpf-batch:testkit:test `
  --no-daemon --max-workers=1
.\gradlew.bat test assemble --no-daemon --max-workers=1
```

이 문서의 `완료`는 Source 이관 상태를 뜻한다. 위 명령을 현재 최종 Source에서 다시
실행하기 전에는 Test/Assemble 재검증 완료를 주장하지 않는다.
