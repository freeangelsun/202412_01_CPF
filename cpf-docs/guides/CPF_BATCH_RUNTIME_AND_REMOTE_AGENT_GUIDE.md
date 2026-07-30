# CPF Batch Runtime과 원격 Agent 가이드

## 1. 목적

CPF Batch는 업무 Job을 안정적으로 등록·승인·배포·실행·재시작·재처리하기 위한 독립 Runtime 제품이다. Scheduler, Worker, Agent와 Center-Cut의 책임을 분리하면서 동일한 실행 식별, 권한, 감사와 복구 계약을 사용한다.

## 2. 제품 구성

| Project | 책임 |
|---|---|
| `cpf-batch-contract` | Job Pack, Parameter, Execution Public Contract |
| `cpf-batch-runtime-common` | Lease, Fencing, Context, 공통 상태 |
| `cpf-batch-control-server` | 작업정의, 배포, 실행 조회와 명령 |
| `cpf-batch-scheduler` | Schedule, Calendar, Trigger, Misfire |
| `cpf-batch-worker` | Spring Batch Job/Step 실행 |
| `cpf-center-cut-runner` | 대량 Partition·Claim·재처리 |
| `cpf-batch-host-agent` | 원격 Host Artifact와 Process 실행 |
| `cpf-batch-testkit` | 업무 Job Pack과 Fault 검증 |

## 3. Control Plane과 Execution Plane

```text
Control Plane
작업정의 → 검증 → 승인 → 배포 → 실행용 Projection

Execution Plane
Scheduler → Execution → Worker/Agent/Runner → 결과·Checkpoint
```

Control Plane의 DB와 Execution Runtime의 책임을 혼합하지 않는다.

## 4. 작업정의

작업정의는 Version을 가진다.

필수 정보:

- jobId
- definitionVersion
- checksum
- executorType
- executorReference
- parameterSchema
- trigger
- dependency
- retry
- timeout
- misfire
- unknownResult
- compensation
- SLA
- owner
- state

Published Version은 직접 수정하지 않는다. 변경은 Clone 후 새 Version으로 만든다.

## 5. 상태 전이

예:

```text
DRAFT
→ VALIDATED
→ APPROVAL_REQUESTED
→ APPROVED
→ PUBLISHED
→ RETIRED
```

Rollback은 과거 Published Version을 새 배포 대상으로 선택하는 명시 Action이다. Client가 Body의 상태 값을 임의로 바꿀 수 없다.

## 6. 작성자·승인자 분리

- 작성자와 승인자 동일 금지
- Publish, Retire, Rollback 별도 Permission
- Reason 필수
- Policy Version 기록
- 승인 만료
- expectedVersion
- Immutable Audit

## 7. 실행용 Projection

Published Definition은 Scheduler와 Worker가 소비할 실행용 Projection으로 변환된다.

```text
Definition 원장
+ Parameter Schema
+ Trigger
+ Retry/Timeout
+ Dependency
+ Compensation
→ Versioned Runtime Projection
```

Projection 저장과 배포 Event는 Transaction 또는 Outbox로 원자성을 보장한다.

## 8. Scheduler

Scheduler 책임:

- Leader Lease
- Fencing Token
- Due Schedule 조회
- Calendar
- Misfire
- Trigger Unique
- Execution 생성
- nextFireAt 갱신
- Retryable 오류 분리

다중 Scheduler가 같은 예정 시각을 중복 실행으로 만들지 않는다.

## 9. Worker

Worker 흐름:

```text
READY Execution 조회
→ Claim
→ Lease/Fencing
→ Definition Version 고정
→ Parameter 검증
→ Job Pack Resolve
→ Spring Batch 실행
→ Checkpoint
→ 결과 저장
→ Lease 해제
```

실행 중 Definition 변경은 기존 실행 의미를 바꾸지 않는다.

## 10. Parameter Schema

지원 Type:

- STRING
- INTEGER
- DECIMAL
- BOOLEAN
- DATE
- DATETIME
- ENUM
- CODE
- FILE_REFERENCE
- SECRET_REFERENCE

검증:

- required
- min/max
- length
- pattern
- allowed values
- code group
- overrideAllowed
- sensitive
- default

Secret은 Alias/Reference만 허용한다.

## 11. Retry와 Timeout

Retry 정책:

- 최대 횟수
- Backoff
- Jitter
- Retryable Code
- Non-retryable Code
- 전체 Deadline
- Attempt별 Timeout

비멱등 외부 Command는 결과 조회 없이 재시도하지 않는다.

## 12. Misfire

정책 예:

| 정책 | 의미 |
|---|---|
| SKIP | 놓친 실행을 생성하지 않음 |
| FIRE_NOW | 즉시 한 번 실행 |
| CATCH_UP | 허용 범위 내 누락 실행 생성 |
| NEXT | 다음 예정 시각부터 재개 |
| COMPENSATE | 보상 Job 또는 운영 절차 연결 |

장시간 중단 시 무제한 Catch-up을 막는 상한을 둔다.

## 13. Dependency

Dependency는 단순 Job 이름 목록이 아니다.

- 선행 Job
- 같은 업무일자
- 성공 상태
- 허용 지연
- Timeout
- 선택/필수
- 데이터 준비 조건
- 실패 시 정책

Cycle, 자기 참조, 존재하지 않는 Job을 차단한다.

## 14. 결과 불명

원격 Agent나 외부 시스템 호출 후 응답이 유실되면:

```text
RUNNING
→ UNKNOWN_RESULT
→ 상태 조회 / Agent 대사
→ COMPLETED 또는 FAILED
→ 필요 시 REPROCESS / COMPENSATION
```

UNKNOWN을 자동 FAILED로 바꾸지 않는다.

## 15. Compensation

Definition의 Compensation Reference는 실제 Handler 또는 SPI에 연결된다.

- 대상 실행
- 보상 가능 상태
- 보상 Parameter
- 멱등성
- 최대 횟수
- 결과
- 감사

## 16. Restart와 Reprocess

### Restart

동일 CPF Execution과 Spring Batch JobInstance를 이어 수행한다.

- 실패 Step부터
- 동일 업무 의미
- 동일 Parameter
- Checkpoint 사용

### Reprocess

새 CPF Execution을 만든다.

- 새로운 operationId
- 원 실행 Reference
- Parameter Override 정책
- 중복 영향 확인
- 별도 승인 가능

## 17. Center-Cut

대량 처리 흐름:

```text
대상 추출
→ Partition
→ Item Claim
→ Running
→ Handler
→ Success / Failed / Unknown
→ Reprocess
```

Claim에는 Owner, Lease, Fencing과 Attempt를 기록한다.

## 18. Host Agent

Agent는 임의 Shell 문자열을 실행하지 않는다. 승인 Catalog의 Artifact만 실행한다.

Catalog:

- artifactId
- version
- checksum
- signature
- signer
- interpreter
- interpreterVersion
- installRoot
- workingDirectory
- timeout
- allowedArguments
- environment Reference
- health
- rollbackVersion

## 19. Artifact 설치

```text
Artifact Repository
→ TLS
→ Download
→ SHA-256
→ 전자서명
→ 압축 해제
→ releases/<version>
→ 권한 설정
→ current 전환
→ 기동
→ Readiness
→ 실패 시 Rollback
```

## 20. Shell 보안

- 고정 Interpreter 경로
- Version 범위
- PATH Hijacking 방지
- Working Directory 제한
- Argument Allowlist
- Parameter File 권한
- Secret 원문 Command Line 금지
- Process Tree 종료
- stdout/stderr 마스킹
- 최대 출력 크기
- 실행 사용자 제한

## 21. Agent Pool

- zone
- capability
- capacity
- current load
- drain
- maintenance
- heartbeat
- version
- artifact cache
- fencing

Scheduler는 Drain/Maintenance Agent에 신규 실행을 배정하지 않는다.

## 22. Log

공통 식별:

- environment
- cellId
- jobId
- executionId
- workerId
- agentId
- transactionId
- segmentId
- attempt

Log Rotation, 압축, Retention과 마스킹을 적용한다.

## 23. ADM 연계

ADM은 BAT DB를 직접 수정하지 않는다.

```text
ADM
→ BAT Operations Contract
→ Control Server
→ Scheduler/Worker/Agent
```

위험 명령은 Approval Owner Command를 사용한다.

## 24. 장애 시나리오

- Scheduler Leader 종료
- Worker Lease 만료
- Stale Fencing 완료
- Agent Network 단절
- Process 기동 후 응답 유실
- DB Commit 후 Timeout
- 중복 Trigger
- Parameter Secret 누락
- Signature 실패
- Disk Full
- Output 폭주
- Compensation 실패

## 25. 운영 조회

- Definition
- Version
- Projection
- Schedule
- Trigger
- Execution
- Attempt
- Step
- Checkpoint
- Worker
- Agent
- SLA
- Unknown
- Reprocess
- Compensation
- Audit

## 26. Testkit

업무 Job Pack은 Testkit으로 다음을 검증한다.

- 정상
- Validation
- Restart
- Retry
- Timeout
- Checkpoint
- Duplicate
- Unknown
- Compensation
- Multi-worker
- Secret Masking

## 27. 완료 체크리스트

- [ ] Published Definition이 Runtime Projection으로 연결된다.
- [ ] Scheduler/Worker/Agent가 고정 Version을 사용한다.
- [ ] Parameter를 실행 직전에 검증한다.
- [ ] Lease와 Fencing이 있다.
- [ ] Restart와 Reprocess가 구분된다.
- [ ] Unknown Result와 대사가 있다.
- [ ] Agent Artifact Hash와 Signature를 검증한다.
- [ ] 위험 명령에 권한·사유·승인이 있다.
- [ ] 실행 이력과 Audit이 같은 식별자로 연결된다.
