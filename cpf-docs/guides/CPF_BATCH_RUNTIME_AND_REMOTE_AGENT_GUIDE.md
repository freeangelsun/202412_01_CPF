# CPF 배치 실행 환경과 원격 에이전트 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 배치 개발자, 배치 운영자, 서버 운영자
> **목적**: 작업정의부터 작업자·원격 에이전트·대량 실행까지 안전하게 설치·운영한다.
> **관련 문서**: [배치 스케줄러와 실행 생명주기](CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md) · [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `cpf-batch` 제품군 |
| 이 문서로 완료하는 일 | 승인된 Job Definition을 Projection으로 배포하고 Scheduler·Worker·Center-Cut·Host Agent가 Lease·Fencing·Attempt Ledger로 안전하게 실행한다. |
| 적용 범위 | Control Server, Runtime Common, Scheduler, Worker, Agent, Center-Cut, Testkit |
| 주요 독자 | Batch 개발자, Batch 운영자, Agent 운영자, 승인자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

CPF 배치는 업무 배치 작업을 안정적으로 등록·승인·배포·실행·재시작·재처리하기 위한 독립 실행 제품이다. 일정관리기, 작업자, 에이전트와 대량 실행의 책임을 분리하면서 동일한 실행 식별, 권한, 감사와 복구 계약을 사용한다.

## 2. 제품 구성

| 모듈 | 책임 |
|---|---|
| `cpf-batch-contract` | 작업 묶음, 매개변수, 실행 공개 계약 |
| `cpf-batch-runtime-common` | 임대, Fencing, 문맥, 공통 상태 |
| `cpf-batch-control-server` | 작업정의, 배포, 실행 조회와 명령 |
| `cpf-batch-scheduler` | 일정, 달력, 트리거, 누락 실행 |
| `cpf-batch-worker` | Spring Batch 작업·단계 실행 |
| `cpf-center-cut-runner` | 대량 분할·점유·재처리 |
| `cpf-batch-host-agent` | 원격 호스트 산출물과 프로세스 실행 |
| `cpf-batch-testkit` | 업무 작업 묶음과 장애 주입 검증 |

## 3. 제어 영역과 실행 영역

```text
Control Plane
작업정의 → 검증 → 승인 → 배포 → 실행용 Projection

Execution Plane
Scheduler → Execution → Worker/Agent/Runner → 결과·Checkpoint
```

제어 영역의 DB와 실행 환경의 책임을 혼합하지 않는다.

## 4. 작업정의

### 작업정의 소유 제어 포트

작업정의 생성·수정·검증·승인요청·승인·게시·폐기는 배치 제어 서버가 소유한다. ADM은 `BatchJobDefinitionControlPort`를 통해 명령하며, 분리 실행 환경에서는 원격 어댑터가 제어 서버 API를 호출한다. 상태 전이, 작성자·승인자 분리, `expectedVersion`과 감사는 제어 서버가 최종 강제한다.


작업정의는 버전을 가진다.

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

게시된 버전은 직접 수정하지 않는다. 변경은 복제한 뒤 새 버전으로 만든다.

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

되돌리기는 과거 게시된 버전을 새 배포 대상으로 선택하는 명시적 동작이다. 클라이언트가 요청 본문의 상태 값을 임의로 바꿀 수 없다.

## 6. 작성자·승인자 분리

- 작성자와 승인자 동일 금지
- 게시, 폐기, 되돌리기 별도 권한
- 사유 필수
- 정책 버전 기록
- 승인 만료
- expectedVersion
- 변경할 수 없는 감사 이력

## 7. 실행용 투영

게시된 작업정의는 일정관리기와 작업자가 소비할 실행용 투영으로 변환된다.

```text
작업정의 원장
+ 매개변수 스키마
+ Trigger
+ Retry/Timeout
+ Dependency
+ Compensation
→ 버전이 부여된 실행 투영
```

투영 저장과 배포 사건은 트랜잭션 또는 송신함으로 원자성을 보장한다.

## 8. 일정관리기

### 실행 투영과 일정 동기화

게시된 작업정의는 일정관리기와 작업자가 빠르게 소비할 수 있는 실행 투영으로 변환한다. 일정관리기는 투영의 일정, 달력, 시간대, 활성 상태와 버전을 읽어 내부 트리거를 생성·갱신·중지한다.

- 오래된 투영 버전으로 현재 일정을 덮어쓰지 않는다.
- 삭제·폐기된 정의의 트리거를 비활성화한다.
- 일부 일정 동기화 실패를 전체 성공으로 기록하지 않는다.
- 재시작 시 투영과 현재 트리거를 다시 대사한다.


일정관리기 책임:

- 선도 임대(Leader Lease)
- 세대 토큰
- 실행 예정 일정 조회
- 달력
- 누락 실행
- 트리거 유일성
- 실행 생성
- nextFireAt 갱신
- 재시도 가능 오류 분리

다중 일정관리기가 같은 예정 시각을 중복 실행으로 만들지 않는다.

## 9. 작업자

### 실행기 등록부와 시도 원장

작업자는 작업 유형과 실행 방식에 맞는 실행기를 등록부에서 선택한다. 지원하지 않는 유형은 임의 셸 실행으로 우회하지 않고 명확한 오류로 거부한다.

각 처리 시도는 별도 원장에 다음을 기록한다.

- executionId와 attemptNo
- 선택된 workerInstanceId와 executorType
- 시작·종료 시각
- 입력 스냅샷 식별자와 산출물 해시
- 성공·실패·결과 불명
- 오류 코드와 재시도 가능 여부
- 다음 재시도 시각

실행 최종 상태와 개별 시도 이력을 분리해 운영자가 재시도 폭증, 작업자 교체와 늦은 완료를 분석할 수 있게 한다.


작업자 흐름:

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

## 10. 매개변수 스키마

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

비밀값은 Alias/참조만 허용한다.

## 11. 재시도와 시간 제한

재시도 정책:

- 최대 횟수
- Backoff
- Jitter
- Retryable Code
- Non-retryable Code
- 전체 Deadline
- 시도별 시간 제한

비멱등 외부 명령은 결과 조회 없이 재시도하지 않는다.

## 12. 누락 실행

정책 예:

| 정책 | 의미 |
|---|---|
| SKIP | 놓친 실행을 생성하지 않음 |
| FIRE_NOW | 즉시 한 번 실행 |
| CATCH_UP | 허용 범위 내 누락 실행 생성 |
| NEXT | 다음 예정 시각부터 재개 |
| COMPENSATE | 보상 작업 또는 운영 절차 연결 |

장시간 중단 시 무제한 Catch-up을 막는 상한을 둔다.

## 13. 의존 대상

의존 대상은 단순 작업 이름 목록이 아니다.

- 선행 작업
- 같은 업무일자
- 성공 상태
- 허용 지연
- 시간 제한
- 선택/필수
- 데이터 준비 조건
- 실패 시 정책

Cycle, 자기 참조, 존재하지 않는 작업을 차단한다.

## 14. 결과 불명

원격 에이전트나 외부 시스템 호출 후 응답이 유실되면:

```text
RUNNING
→ UNKNOWN_RESULT
→ 상태 조회 / Agent 대사
→ COMPLETED 또는 FAILED
→ 필요 시 REPROCESS / COMPENSATION
```

UNKNOWN을 자동 FAILED로 바꾸지 않는다.

## 15. 보상

Definition의 보상 참조는 실제 Handler 또는 SPI에 연결된다.

- 대상 실행
- 보상 가능 상태
- 보상 매개변수
- 멱등성
- 최대 횟수
- 결과
- 감사

## 16. Restart와 Reprocess

### Restart

동일 CPF 실행과 Spring Batch JobInstance를 이어 수행한다.

- 실패 Step부터
- 동일 업무 의미
- 동일 매개변수
- 체크포인트 사용

### Reprocess

새 CPF 실행을 만든다.

- 새로운 operationId
- 원 실행 참조
- 매개변수 Override 정책
- 중복 영향 확인
- 별도 승인 가능

## 17. Center-Cut 대량 실행

대량 처리 흐름:

```text
대상 추출
→ Partition
→ Item Claim
→ Running
→ Handler
→ 성공 / 실패 / 결과 불명
→ Reprocess
```

점유에는 소유자, 임대, Fencing과 시도를 기록한다.

## 18. 호스트 에이전트

에이전트는 임의 Shell 문자열을 실행하지 않는다. 승인 Catalog의 산출물만 실행한다.

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
- environment 참조
- health
- rollbackVersion

## 19. 산출물 설치

승인 파일 실행은 로컬 경로 존재만 확인하지 않는다. 원격 저장소에서 산출물을 받을 때 임시 파일로 내려받고 크기·Hash·서명을 검증한 뒤 승인 디렉터리로 원자 이동한다. 전송 실패, Hash 불일치와 부분 파일은 실행하지 않고 격리한다.


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
- 버전 범위
- PATH Hijacking 방지
- Working Directory 제한
- Argument 허용 목록
- 매개변수 파일 권한
- 비밀값 원문 명령 Line 금지
- 프로세스 Tree 종료
- stdout/stderr 마스킹
- 최대 출력 크기
- 실행 사용자 제한

## 21. 에이전트 Pool

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

일정관리기는 배수·점검 모드 에이전트에 신규 실행을 배정하지 않는다.

## 22. 로그

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

로그 Rotation, 압축, 보존과 마스킹을 적용한다.

## 23. ADM 연계

ADM은 BAT DB를 직접 수정하지 않는다.

```text
ADM
→ BAT Operations Contract
→ Control Server
→ Scheduler/Worker/Agent
```

위험 명령은 승인 소유자 명령을 사용한다.

## 24. 장애 시나리오

- 일정관리기 Leader 종료
- 작업자 임대 만료
- Stale Fencing 완료
- 에이전트 Network 단절
- 프로세스 기동 후 응답 유실
- DB Commit 후 시간 제한
- 중복 트리거
- 매개변수 비밀값 누락
- 서명 실패
- Disk Full
- Output 폭주
- 보상 실패

## 25. 운영 조회

- Definition
- 버전
- 투영
- 일정
- 트리거
- 실행
- 시도
- Step
- 체크포인트
- 작업자
- 에이전트
- SLA
- 결과 불명
- Reprocess
- 보상
- 감사

## 26. 시험 도구 모음

업무 작업 묶음은 Testkit으로 다음을 검증한다.

- 정상
- 검증
- Restart
- 재시도
- 시간 제한
- 체크포인트
- Duplicate
- 결과 불명
- 보상
- Multi-worker
- 비밀값 마스킹

## 27. 완료 체크리스트

- [ ] 게시된 작업정의가 실행 환경 투영으로 연결된다.
- [ ] 일정관리기/작업자/에이전트가 고정 버전을 사용한다.
- [ ] 매개변수를 실행 직전에 검증한다.
- [ ] 임대와 Fencing이 있다.
- [ ] Restart와 Reprocess가 구분된다.
- [ ] 결과 불명과 대사가 있다.
- [ ] 에이전트 산출물 해시와 서명을 검증한다.
- [ ] 위험 명령에 권한·사유·승인이 있다.
- [ ] 실행 이력과 감사가 같은 식별자로 연결된다.

## 부록 A. 원격 에이전트 등록 절차

1. 호스트 식별자, 환경, 영역, 운영체제와 실행 계정을 준비한다.
2. 에이전트 인증서 또는 등록 토큰을 발급한다.
3. 허용 실행 경로, 작업 디렉터리, 자원 상한과 네트워크 정책을 설정한다.
4. 제어 서버에 등록 요청을 보내고 운영자가 호스트 정보를 검토한다.
5. 승인 뒤 상호 TLS 연결과 심박을 확인한다.
6. 서명된 시험 산출물을 배포하고 해시·서명·실행·로그 회수를 검증한다.
7. 배수, 인증서 교체, 업그레이드와 폐기 절차를 시험한다.

## 부록 B. 산출물 실행 안전 기준

- 승인된 저장소·버전·해시·서명만 허용
- 상대 경로·상위 경로 이동·심볼릭 링크 우회 차단
- 명령 인수와 환경변수 허용 목록
- 비밀값은 실행 직전에 참조로 해석하고 명령행에 노출하지 않음
- 표준 출력·오류의 크기 제한과 민감정보 마스킹
- 자식 프로세스 트리 추적과 중단
- CPU·메모리·실행시간·파일 크기 상한
- 실행 계정 최소 권한과 작업 디렉터리 격리

## 부록 C. 교체 배포

`배수 요청 → 신규 점유 중단 → 진행 작업 종료·체크포인트 → 상태 확인 → 프로세스 교체 → 등록·심박 → 시험 작업 → 배수 해제`

진행 작업의 강제 종료가 필요한 경우 작업 유형별 재시작 가능 여부와 외부 효과를 먼저 확인한다.

## 31. 작업정의 API와 상태 흐름

BAT 소유 API의 Root는 `/api/v1/batch/job-definitions`다.

| 목적 | Method와 경로 | 판정 |
|---|---|---|
| 목록·상태 조회 | `GET /`, `GET /{jobId}/versions/{version}` | Version, Checksum, 상태, Row Version |
| 사전 검증 | `POST /validate` | 실행기·Trigger·Parameter·Dependency·복구 정책 검증 |
| Draft 저장 | `POST /drafts` | 인증 Principal과 `requestedBy` 일치 확인 |
| 승인 게시 | `POST /{jobId}/versions/{version}/approved-publish` | 승인 ID, Payload Hash, 요청자·승인자 분리 |
| 일반 상태 전이 | `POST /{jobId}/versions/{version}/transition` | 기대 Row Version과 사유 |

게시된 Version은 불변이다. 변경이 필요하면 새 Definition Version을 만들고 검증·승인·게시를 다시 수행한다.

## 32. 최소 작업정의 예제

```json
{
  "jobId": "PAY.DAILY.RECONCILE",
  "definitionVersion": 1,
  "jobName": "일일 결제 대사",
  "executorType": "SERVICE_CALL",
  "state": "DRAFT",
  "ownerDomain": "PAY",
  "description": "전일 결제와 기관 결과를 대사한다.",
  "trigger": {
    "type": "CRON",
    "expression": "0 30 2 * * *",
    "timezone": "Asia/Seoul",
    "misfirePolicy": "FAIL_CLOSED",
    "enabled": true
  },
  "parameters": [],
  "dependencies": [],
  "resourcePolicy": {
    "agentPool": "DEFAULT",
    "zone": "SEOUL-A",
    "maxConcurrency": 1,
    "timeoutSeconds": 3600,
    "memoryLimitMb": 0,
    "cpuLimitMillicores": 0
  },
  "recoveryPolicy": {
    "maxAttempts": 3,
    "initialBackoffSeconds": 30,
    "multiplier": 2.0,
    "maxBackoffSeconds": 300,
    "skipLimit": 0,
    "restartable": true,
    "unknownResultPolicy": "RECONCILE",
    "compensationReference": ""
  },
  "alertPolicy": {
    "delayThresholdSeconds": 300,
    "slaSeconds": 7200,
    "notifyOnFailure": true,
    "notifyOnMissed": true,
    "providerKeys": ["OPS_DEFAULT"]
  },
  "executorReference": "SERVICE:PAY:reconcileDailyPayments:v1",
  "checksum": "",
  "requestedBy": "batch-author01",
  "reason": "일일 결제 대사 작업 신규 등록",
  "effectiveFrom": "2026-07-31T00:00:00+09:00",
  "effectiveUntil": null,
  "expectedRowVersion": 0
}
```

`SERVICE_CALL`은 `SERVICE:`로 시작하는 Typed Operation Reference가 필요하다. `APPROVED_SHELL`은 `SCRIPT:` Catalog Reference가 필요하다. `FILE_PROCESS`는 `PROCESSOR:<processorId>` Reference와 필수 `sourceAlias(PATH_ALIAS)`, `sourcePath(STRING 또는 FILE_REFERENCE)` 매개변수를 요구한다. 자기 자신 Dependency와 중복 Dependency는 거부한다.

## 33. 승인 게시와 실행 Projection

1. `/validate` 결과가 성공인지 확인한다.
2. Draft를 저장하고 반환된 Checksum과 Row Version을 보관한다.
3. ADM 승인 요청에 Definition Snapshot과 Checksum을 첨부한다.
4. 승인 실행 시 `operationId`, `expectedRowVersion`, `approvalRequestId`, `payloadHash`, 요청자, 승인자와 사유를 전달한다.
5. BAT는 현재 Definition Checksum과 승인 Payload Hash가 다르면 게시를 거부한다.
6. 게시 성공 후 Scheduler Projection 동기화 상태를 확인한다.
7. Scheduler가 Projection의 Trigger·Timezone·Misfire Policy를 사용해 실행을 생성하는지 확인한다.
8. Worker가 등록된 실행기를 선택하고 Attempt 원장을 시작·완료하는지 확인한다.

## 34. Worker 인계와 재시도 Runbook

1. 실행 ID, 현재 Attempt, Worker ID, Lease 만료 시각과 Fencing Token을 조회한다.
2. Worker가 살아 있으나 느린지, Process가 종료됐는지, DB 연결만 끊겼는지 분류한다.
3. Lease가 유효하면 다른 Worker가 같은 실행을 Claim하지 못하게 한다.
4. Lease 만료 뒤 새 Worker가 더 큰 Fencing Token으로 Claim한다.
5. 이전 Worker의 늦은 완료는 Stale Fencing으로 거부한다.
6. 오류가 재시도 가능하고 최대 Attempt와 시간 예산이 남았을 때만 Backoff 뒤 재대기한다.
7. 외부 요청 결과가 불명확하면 `unknownResultPolicy`에 따라 대사·수동 검토·보상·안전 차단을 수행한다.
8. 실행 상태뿐 아니라 Attempt 원장과 업무 결과를 함께 확정한다.

### 34.1 원격 승인 파일

- Catalog에서 승인된 Artifact ID, Hash, 서명과 실행 정책을 조회한다.
- 전송 전후 Hash를 비교하고 임시 경로에서 검증한 뒤 원자적으로 전환한다.
- Command Line에 비밀값을 넣지 않고 Secret Reference를 사용한다.
- Process Tree, 시간 제한, 종료 코드, 표준 출력·오류의 마스킹과 정리 결과를 기록한다.
- 전송·실행 중 연결이 끊기면 중복 실행하지 않고 원격 Process와 결과 Artifact를 먼저 대사한다.

## 35. FILE_PROCESS 업무 처리기

`FILE_PROCESS`는 Worker 내부에 업무 코드를 하드코딩하지 않고 `FileProcessHandler` SPI로 처리기를 등록한다.

```json
{
  "executorType": "FILE_PROCESS",
  "executorReference": "PROCESSOR:reference-csv-import",
  "parameters": [
    {"name": "sourceAlias", "type": "PATH_ALIAS", "required": true},
    {"name": "sourcePath", "type": "STRING", "required": true}
  ]
}
```

Worker는 파일을 Claim한 뒤 처리기에 다음 실행 문맥을 전달한다.

- 실행 ID, Definition Version과 Checksum
- 거래·Segment 식별자
- Fencing Token
- Claim된 정규화 경로, 크기와 SHA-256
- 검증된 매개변수

처리 결과는 `COMPLETED`, `RETRYABLE_FAILURE`, `FAILED`, `UNKNOWN_RESULT` 중 하나다. Claim 소유권 또는 Fencing을 잃은 Worker의 늦은 결과는 Attempt 원장 반영 전에 거부한다. `UNKNOWN_RESULT`는 파일 이동·출력 생성·외부 전송 결과를 먼저 대사한 뒤 재처리 여부를 결정한다.

## 36. 승인 Shell 서명 검증

승인 Shell은 Hash만 일치한다고 실행하지 않는다. 기본 `verificationMode`는 `SIGNATURE`이며 Catalog에 다음을 둔다.

- SHA-256
- Detached Signature
- `signatureKeyId`
- `SHA256/384/512withRSA` 또는 `...withECDSA` Algorithm
- 제품 Trust Store의 공개키 또는 X.509 인증서 Chain

검증기는 Artifact Hash와 Signature를 모두 확인하고, 신뢰되지 않은 Key·허용되지 않은 Algorithm·잘못된 Encoding·약한 MD5/SHA1 인증서 Chain을 거부한다. 서명 저장소나 Trust Store를 읽을 수 없을 때 Hash-only로 자동 하향하지 않고 안전 차단한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Public Contract | `cpf-batch/contract/src/main/java/com/cpf/batch/api/`, `cpf-batch/contract/src/main/java/com/cpf/batch/spi/FileProcessHandler.java` | Job Definition·Control Port·FILE_PROCESS SPI |
| Control Server | `cpf-batch/control-server/src/main/java/com/cpf/batch/control/job/BatchJobDefinitionController.java` | 검증·Draft·승인 게시·상태 전이 |
| Scheduler | `cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/BatchProjectionScheduleSynchronizer.java` | 승인 Projection과 Schedule 동기화 |
| Worker | `BatchRuntimeExecutorRegistry.java`, `BatchFileProcessHandlerRegistry.java`, `JobPackDispatcher.java` | Typed 실행기·Processor 선택과 Dispatch |
| Attempt Ledger | `JdbcWorkerExecutionRepository.java`, Vendor `worker-attempt-*.sql` | 실행 시도 기록·완료·재시도 |
| Remote File/Shell | `ApprovedFileExecutor.java`, `JcaScriptArtifactVerifier.java`와 관련 Test | 승인 Artifact 전송·Hash·Signature·실행 |

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
