# CPF Batch·Center-Cut·Agent Guide

## 1. Runtime 구성

```text
Scheduler
→ Job Dispatcher
→ Agent Selector
→ Executor

Center-Cut
→ CenterCutRunner
→ Worker
→ Item Attempt
```

## 2. Executor SPI

- JavaJobExecutor
- ShellJobExecutor
- FileWatchExecutor
- FileProcessExecutor
- FileTransferExecutor
- CenterCutRunner

## 3. Job Policy

- Parameter Schema
- Transaction
- Retry
- Skip
- Rollback
- Timeout
- Checkpoint
- Schedule
- Calendar
- Dependency
- Approval
- Notification

## 4. Center-Cut

### Header

- Job ID
- Handler
- Owner
- Requester
- Approver
- Source
- transactionGlobalId
- idempotencyKey
- Parameter Snapshot
- Policy Snapshot
- Counts
- Status
- Error

### Item

- Key
- State
- Attempt
- Worker
- Lease
- Fencing
- Parent/Child Transaction
- Result
- Error
- Retry
- Compensation

## 5. Speed

- Global TPS
- Per Instance
- Delay
- Worker Count
- Inflight
- Backpressure
- Dynamic Change
- Restart Restore

## 6. Error

- Fail Fast
- Continue
- Retry Stop
- Retry Continue
- Skip
- Threshold
- Pause
- Compensate

## 7. Agent

- Primary
- Secondary
- Pool
- Capability
- Zone
- Slot
- Drain
- Failover
- Failback
- Path Alias

## 8. Recovery

- Lease Expiry
- Fencing
- Checkpoint
- Unknown Result
- Result Query
- Failed-only Reprocess
- Compensation

## 9. ADM

- Job
- Instance
- Step
- Item
- Attempt
- Agent
- Runner
- Worker
- Speed
- Failover
- Approval
- Audit
- Timeline

## 10. Runtime Scenarios

1. Normal
2. Retry
3. Fail Fast
4. Continue
5. Threshold Pause
6. Resume
7. Worker Crash
8. Agent Failover
9. Stale Update Rejection
10. Unknown
11. Failed-only
12. Compensation
13. Restart
14. Global TPS
