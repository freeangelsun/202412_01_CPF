# CPF 배치 실행 환경과 원격 에이전트 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 배치 개발자, 배치 운영자, 서버 운영자
> **목적**: 작업정의부터 작업자·원격 에이전트·대량 실행까지 안전하게 설치·운영한다.
> **관련 문서**: [배치 스케줄러와 실행 생명주기](CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md) · [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md)

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
→ Success / Failed / Unknown
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
- Unknown
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
- Unknown
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
