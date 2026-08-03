# CPF 배치 개발 매뉴얼


## 문서 기준과 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 기준 Commit: `e134c1f275c306c0e9ab4a044d9140ac4b3ca620`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 사용자 지시 적용: 요구사항에 정의되고 Source에 연결된 기능은 사용 가능한 제품 기능으로 설명한다.
- 이 문서 작업에서 직접 수행한 Runtime·DB·Browser·다중 인스턴스 검증: `미검증`
- 문서와 Source의 경로·식별자 정합성 검토: `완료`

> Runtime 미검증은 기능 절차를 생략하는 이유가 아니다. 다만 실행 결과를 직접 확인하지 않은 항목은 배포 승인 시 해당 환경의 Evidence로 다시 확인한다.


## 1. 배치 제품 구성

| Module | 역할 |
|---|---|
| `cpf-batch/contract` | Job·Execution·Control Public Contract |
| `cpf-batch/runtime-common` | 공통 실행 상태·식별자 |
| `cpf-batch/execution-runtime` | Spring Batch 실행 기반 |
| `cpf-batch/control-server` | 등록·실행·제어 API |
| `cpf-batch/scheduler` | Trigger·Misfire·실행 요청 |
| `cpf-batch/worker` | Job 실행, Messaging/Notification Worker |
| `cpf-batch/center-cut-runner` | 대량 대상 분할 실행 |
| `cpf-batch/host-agent` | Host Process 제어·상태 보고 |
| `cpf-batch/testkit` | Contract/Fault Test 지원 |

공개 선택은 `batch-service` Profile이다. Scheduler 구현은 `cpf-batch`가 소유하며 별도 공개 Quartz Starter를 선택하지 않는다.

## 2. Job 설계

1. Job 목적과 업무 원장을 정의한다.
2. JobParameter를 재현 가능한 값으로 구성한다.
3. Tasklet 또는 Chunk를 선택한다.
4. Reader/Processor/Writer와 Transaction 경계를 정한다.
5. Checkpoint와 Restart Key를 정의한다.
6. 실패·Skip·Retry·UNKNOWN 정책을 정한다.
7. ADM 조회·제어·대사 기준을 정한다.

## 3. Tasklet 선택

단일 외부 호출, 파일 이동, 집계 마감처럼 작업 단위가 명확할 때 사용한다. Tasklet은 재실행 가능성, 완료 Marker, 외부 부작용 멱등성을 제공해야 한다.

## 4. Chunk 선택

Reader는 안정적 정렬과 Restart Cursor를 제공하고, Processor는 순수 변환을 우선하며, Writer는 Chunk 단위 Transaction과 영향 건수를 검증한다. Commit Interval은 Lock 시간, 재처리 비용과 메모리를 함께 고려한다.

## 5. JobParameter·Instance

업무일자, 대상 범위, Version, 요청 ID를 식별 Parameter로 사용한다. 실행 시각처럼 매번 바뀌는 값을 JobInstance 식별에 무조건 포함하지 않는다. 동일 업무일자 재처리는 재처리 사유와 새로운 Operation ID로 구분한다.

## 6. Stop·Restart·Abandon

- Stop: 현재 안전 지점에서 중지 요청
- Restart: 실패/중지 Execution의 Checkpoint에서 재개
- Abandon: 더 이상 Restart하지 않을 Execution을 운영자가 사유와 승인으로 종결
- Reprocess: 업무 대상 일부 또는 전체를 새 실행으로 처리

Abandon은 데이터 정상화를 의미하지 않는다. 대사와 후속 보상 완료 여부를 별도로 확인한다.

## 7. Partition·Remote Worker

1. Partition Key와 범위 중복 방지 규칙을 정의한다.
2. Master가 Partition Manifest를 생성한다.
3. Worker가 Lease와 Fencing Token으로 Claim한다.
4. Heartbeat와 Progress를 보고한다.
5. Worker 종료 시 Lease 만료 후 다른 Worker가 Reclaim한다.
6. 이전 Worker의 늦은 Commit은 Fencing으로 거부한다.

## 8. Center-Cut

Center-Cut은 대상 선정, Partition, Chunk, Commit, 대사 기준을 하나의 실행 계약으로 관리한다.

- 대상 Preview 건수와 조건 Hash
- 실행 승인 시점의 Snapshot/Version
- Partition별 시작·종료 Key
- 처리/성공/실패/UNKNOWN 건수
- 원장 대사와 차이 목록
- 재처리 대상 Manifest

## 9. Scheduler 등록

Job ID, Version, Calendar, Cron/Interval, Timezone, Misfire 정책, Concurrency 정책, Parameter Template을 등록한다. 변경 시 Preview → Approval → Effective Time → Applied Version을 기록한다.

## 10. Misfire

| 정책 | 사용 상황 | 주의 |
|---|---|---|
| Skip | 과거 실행 가치 없음 | 누락 감사 필요 |
| Fire Once | 최신 1회만 필요 | 중복 대상 방지 |
| Catch Up | 모든 회차 필요 | Backlog·부하 제한 |
| Manual | 위험·대량 작업 | 승인 후 실행 |

## 11. Artifact·Job Pack

Job Pack에는 Job Definition, Parameter Schema, DB Migration, Runtime Config, Checksum, Permission, Runbook과 Test Evidence를 포함한다. Control Server는 Artifact Version과 Checksum이 승인값과 일치할 때만 실행 요청을 수락한다.

## 12. 실행 절차

1. ADM `/batch-overview`에서 Job, Schedule, Worker Health를 확인한다.
2. Dry Run 또는 대상 건수 Preview를 실행한다.
3. Parameter와 Expected Version을 입력한다.
4. 위험도에 따라 Reason과 Approval을 받는다.
5. 실행 후 Execution ID를 기록한다.
6. Step, Partition, Worker, Lock, Throughput을 확인한다.
7. 완료 후 원장·처리 건수·Audit를 대사한다.

## 13. 장애와 UNKNOWN

- DB Commit 전 Worker 종료: Checkpoint 이전 범위 재실행
- Commit 후 상태 보고 유실: Execution/업무 원장 대사 후 확정
- 메시지 발행 ACK 유실: Broker UNKNOWN Reconcile
- Notification Timeout: Receipt 조회 후 재전송 판단
- Partition Partial Failure: 실패 Partition만 새 Operation으로 Reprocess

## 14. 재처리

재처리는 원 실행을 수정하지 않는다. 원 Execution ID, 대상 Manifest, 제외 기준, Reason, Approver, 새 Operation ID를 연결한다. 이미 성공한 대상의 중복 부작용을 멱등성 Key로 차단한다.

## 15. ADM 화면

- `/batch`: Job/Schedule/Execution/Instance/Worker 조회
- `/batch-overview`: Overview, Lock, Worker, Schedule 상태
- `/recoveryCenter`: UNKNOWN·DLQ·로그 복구
- `/incidents`: 장애 신호와 상태 전이
- `/runtimeControl`: Worker/정책 변경·Rollback

## 16. EDU 실습

### Chunk Restart

- 1,000건을 100건 Commit으로 처리한다.
- 550건 부근에서 Worker를 종료한다.
- 재시작 후 Commit 완료 범위는 중복 갱신되지 않아야 한다.
- 최종 원장 건수와 Execution Summary를 대사한다.

### Partition Fencing

- Worker A가 Partition을 Claim한다.
- Heartbeat를 차단해 Lease를 만료시킨다.
- Worker B가 Reclaim한 뒤 A의 늦은 Write가 거부되는지 확인한다.

### Misfire

- Scheduler를 중지해 3회 Misfire를 만든다.
- Skip/Fire Once/Catch Up 정책별 실행 수와 Audit를 비교한다.

## 17. 운영 인계

Job ID/Version, Schedule, Parameter, 대상 기준, Commit/Partition, SLA, Metric/Alert, Stop/Restart/Abandon, UNKNOWN/Reprocess/Reconcile, DB Backup·Rollback과 승인 권한을 전달한다.
