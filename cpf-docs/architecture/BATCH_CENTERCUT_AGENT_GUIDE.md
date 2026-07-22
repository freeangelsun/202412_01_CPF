# CPF Batch·Center-Cut·Agent Guide

## 1. 목적

이 문서는 `cpf-batch`가 소유하는 일반 Batch, Scheduler, Agent, Runner, Worker와 Center-Cut의 Runtime·DB·운영 계약을 정의한다.

## 2. 책임 구분

| 구성요소 | 책임 |
|---|---|
| Scheduler | 시간·Calendar·Dependency·Misfire에 따라 실행 요청 생성 |
| Batch Coordinator | Job/Step 상태, Parameter, Checkpoint와 Restart 관리 |
| Agent | 실행 Node 등록, Capability, Lease, Drain과 Health |
| Executor | Java, 승인 Shell, File Watch/Process/Transfer 실행 |
| CenterCutRunner | Target/Item 생성, Claim, 분배, 속도, 집계와 복구 |
| Worker | 할당 Item을 업무 Contract로 실행하고 Attempt/Result 기록 |
| ADM | Query/Command API를 통한 조회·제어·승인·감사 |

Agent와 Runner를 한 Process에 둘 수 있지만 책임과 상태 모델을 합치지 않는다.

## 3. 일반 Batch 모델

### Job

- jobId, jobDefinitionId, version
- requestedBy, requestChannel
- immutable parameter snapshot와 hash
- schedule/calendar/misfire policy snapshot
- status, start/end, deadline
- transactionGlobalId

### Step와 Attempt

- Step dependency
- retry/skip/fail-fast/threshold policy
- checkpoint와 restart position
- attempt number, agent/instance, start/end
- standard error와 failure stage

상태 전이는 명시적 State Machine과 optimistic version을 사용한다. 운영자가 DB를 직접 수정하지 않는다.

## 4. Executor Type

- `JAVA_JOB`
- `SHELL_JOB`
- `FILE_WATCH`
- `FILE_PROCESS`
- `FILE_TRANSFER`
- `CENTER_CUT`
- 승인된 `COMPOSITE` 후보

Shell은 arbitrary command를 허용하지 않는다. Script Catalog, hash, allowed argument, timeout, working directory alias, execution user와 audit를 사용한다.

## 5. Center-Cut Flow

```text
request
→ validate/authorize
→ create job
→ persist immutable parameter and execution policy
→ target generation
→ item/chunk creation
→ claim with lease/fencing
→ worker dispatch
→ business transaction
→ attempt/result/error
→ aggregate
→ ADM timeline/recovery
```

### Parameter

- 원본 Parameter 또는 canonical JSON/LOB
- schemaVersion, contentType, size와 hash
- encrypted storage와 field masking
- 요청 시점 정책 snapshot
- Worker 전달 값과 저장 값의 동일성 검증

### Target와 Item

- 대량 Target는 streaming/chunk/page 생성
- 생성 중단 후 resume 가능
- item마다 상태를 덮어쓰지 않고 attempt를 누적
- parent/child transactionGlobalId 연결

## 6. Claim, Lease와 Fencing

Claim은 다음을 포함한다.

- ownerInstanceId
- leaseUntil
- fencingToken
- attemptNo
- version

Lease 만료만 보고 기존 실행을 무조건 재실행하지 않는다. Fencing Token으로 오래된 Worker의 결과 반영을 차단하고, 실제 처리 결과를 확인한 뒤 takeover한다.

## 7. 속도와 Backpressure

- job-level concurrency
- tenant/domain/operation별 limit
- cluster-global TPS/RPS
- burst와 steady rate
- queue depth와 downstream health 기반 backpressure
- 운영 override는 permission, expiry, reason과 audit

각 Job은 실행 시작 시 policy snapshot을 저장한다.

## 8. 실패·재처리·결과 불명

- 확정 성공: 재처리 금지
- 확정 실패: eligibility policy에 따라 failed-only reprocess
- 결과 불명: 실제 업무/외부 결과 조회 후 결정
- timeout 전송 전/후 구분
- compensation 가능 여부와 승인
- manual adjustment는 원본을 덮어쓰지 않고 별도 기록

새 재처리 Job과 Attempt는 원본 Job/Item과 연결한다.

## 9. Agent Failover

- Primary/Secondary 또는 capability pool
- zone/host/path alias
- drain과 maintenance
- failover/failback
- pre-execution failure와 mid-execution unknown 구분
- shared durable storage 없는 file job blind replay 금지

## 10. ADM 기능

- Job/Step/Item/Attempt 목록과 상세
- transactionGlobalId timeline
- Agent/Runner/Worker health와 lease
- pause/resume/cancel/drain
- reprocess eligibility preview
- speed/policy override
- approval, reason과 audit
- unknown/reconciliation/compensation
- metric, alert와 incident link

## 11. DB와 Migration

Batch Runtime state는 `cpf-batch` Owner Schema/Table에 둔다. Job, Step, Item, Attempt, lease, policy snapshot, agent registry와 audit 관계를 PK/FK/UK/Index로 보호한다. Empty Install, Upgrade, Rollback, Restart와 Purge/Archive를 검증한다.

## 12. Test와 Evidence

- Unit: state, retry, rate, eligibility
- Integration: DB claim, optimistic lock, transaction
- Runtime: Java/Shell/File executor
- Multi-instance: duplicate claim, lease expiry, fencing
- Fault: worker kill, DB/broker/external timeout
- Recovery: unknown, failed-only, compensation
- ADM Browser: 조회와 위험 조치 승인
