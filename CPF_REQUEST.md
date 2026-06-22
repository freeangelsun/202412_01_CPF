# CPF 신규 요건 목록: 배치 관제 / 로그 정책 / 거래 메타 / 센터컷 / 문서 정본화

## 0. 요청 작성 기준

앞으로 작업 요청은 “CPF 전체 완성”으로 작성하지 않는다.
기능 묶음 단위로 완료 판정표를 작성하고, 해당 검수표를 통과해야 완료로 인정한다.

기능 완료 기준은 다음과 같다.

```text
- 실제 package와 Java 소스가 있어야 한다.
- Controller/API가 있어야 한다.
- Service가 있어야 한다.
- Repository/Mapper가 있어야 한다.
- DTO/Validation이 있어야 한다.
- SQL/Flyway/all_install 반영이 있어야 한다.
- ADM UI 또는 API wrapper가 있어야 한다.
- Swagger Tag/Operation/Schema가 있어야 한다.
- EDU 샘플 또는 개발 가이드 연결이 있어야 한다.
- 정상/오류/권한/DB 테스트가 있어야 한다.
- 실행 명령과 실제 결과가 CPF_STABILIZATION_REPORT.html에 남아야 한다.
```

실행하지 않은 검증은 성공으로 기록하지 않는다.
일부만 구현한 항목은 완료가 아니라 `부분 구현`, `미검증`, `재확인 필요`로 남긴다.

---

# 1. ADM 배치 관제 / Spring Batch E2E / Ghost Recovery 완성

## 1.1 작업 목표

ADM에서 운영자가 배치 Job, 실행 이력, Step 이력, 스케줄, 수행 대상, 실패, 중지, 재실행, worker 상태, ghost 상태를 확인하고 조치할 수 있게 한다.

이번 작업은 배치 관제 기능 묶음만 닫는다.
다른 기능을 넓게 건드리지 않는다.

## 1.2 현재 보강 필요 상태

현재 확인 기준으로 ADM 배치 관제는 일부 구현 상태다.
정적 UI smoke와 SQL smoke는 기록되어 있지만, 실제 Spring Batch 실행, scheduler 실행, 브라우저 클릭, ghost recovery는 미완료 또는 미검증이다.

## 1.3 필수 DB/메타

아래 테이블 또는 동등한 구조를 확인/보강한다.

```text
Spring Batch 표준:
- BATCH_JOB_INSTANCE
- BATCH_JOB_EXECUTION
- BATCH_STEP_EXECUTION
- BATCH_JOB_EXECUTION_PARAMS
- BATCH_JOB_EXECUTION_CONTEXT
- BATCH_STEP_EXECUTION_CONTEXT

CPF 운영 메타:
- pfw_batch_job
- pfw_batch_schedule
- pfw_batch_execution
- pfw_batch_step
- pfw_batch_lock
- pfw_batch_worker
- pfw_batch_job_relation
- pfw_batch_execution_target
- pfw_batch_ghost_event 또는 동등한 ghost/recovery 이력 테이블
```

Spring Batch `BATCH_*` 테이블은 JobRepository가 관리한다.
CPF 운영 메타는 `pfw_batch_*`로 별도 관리하고 JobExecutionId/StepExecutionId/transactionGlobalId로 연결한다.

## 1.4 필수 API

ADM 배치 API는 최소 아래 기능을 제공해야 한다.

```text
- Job 목록 조회
- Job 상세 조회
- Job 실행 이력 조회
- Step 실행 이력 조회
- 수동 실행
- 중지 요청
- 재실행 요청
- 스케줄 조회/수정
- 영업일 수행 여부 조회
- 수행 가능 시간 조회
- 수행 시뮬레이션
- 선행/트리거 관계 조회
- 수행 대상 조회
- worker 상태 조회
- ghost candidate 조회
- ghost 상태 조치
- lock 상태 조회
- lock 해제 또는 조치 요청
```

## 1.5 필수 Service

```text
- BatchJobQueryService
- BatchExecutionService
- BatchScheduleService
- BatchRelationService
- BatchTargetService
- BatchWorkerHeartbeatService
- BatchGhostRecoveryService
- BatchLockService
- BatchRestartPolicyService
```

명칭은 기존 구조에 맞춰도 되지만, 역할은 위 기준을 충족해야 한다.

## 1.6 Ghost Recovery 필수 기준

서버 급중단, deploy/restart, worker 강제 종료로 RUNNING 배치가 실제로는 돌지 않는데 실행 중처럼 남는 상황을 처리한다.

필수 구현:

```text
- serverInstanceId 기록
- workerId 기록
- worker heartbeat 기록
- JobExecutionId와 serverInstanceId 연결
- StepExecutionId와 workerId 연결
- 마지막 heartbeat 시각 기록
- heartbeat timeout 기준
- RUNNING but no heartbeat 상태 감지
- ghost candidate 표시
- ADM에서 ghost 상태 확인
- 운영자 조치: TIMEOUT / FAILED / ABANDONED / RETRYABLE
- 조치 사유 필수
- 조치 감사 로그 저장
- lock 해제 기준
- 재실행 가능 여부 판단
- 동일 Job 중복 실행 방지
```

deploy/restart 중 shutdown hook으로 정리 가능한 범위는 정리하되, 강제 종료는 hook이 보장되지 않으므로 heartbeat 기반 recovery를 반드시 둔다.

## 1.7 ADM UI/API wrapper 기준

ADM 화면 또는 API wrapper에서 아래를 확인할 수 있어야 한다.

```text
- 배치 Job 목록
- Job 상세
- 실행 이력
- Step 이력
- 스케줄
- 영업일/수행 가능 시간
- 선행/트리거 관계
- 수행 대상
- 수동 실행 버튼
- 중지 버튼
- 재실행 버튼
- worker 상태
- ghost 경고
- ghost 조치 화면
- 로그 보기
- 실패 사유 보기
```

정적 marker 확인만으로 완료 처리하지 않는다.
브라우저 클릭을 하지 못하면 미검증으로 기록한다.

## 1.8 필수 테스트

```text
- Job 정상 실행
- Step 정상 실행
- Job 실패 처리
- Step 실패 처리
- 중복 실행 차단
- 수동 실행
- 중지 요청
- 실패 Job 재실행
- JobExecutionId 저장 확인
- StepExecutionId 저장 확인
- transactionGlobalId 저장 확인
- worker heartbeat 저장
- heartbeat timeout 감지
- ghost candidate 조회
- ghost 조치
- lock 해제 기준
- ADM 배치 목록 조회
- ADM 배치 상세 조회
- ADM worker 상태 조회
```

실제 scheduler 실행이 어렵다면 테스트에서 scheduler mock 또는 JobLauncherTestUtils 등 가능한 방식으로 검증하고, 실 scheduler는 미검증 사유를 남긴다.

## 1.9 검증 명령

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :bat:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

가능하면 앱 기동 후 실행한다.

```powershell
.\gradlew.bat runLocalServices --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1
```

## 1.10 완료 불인정 기준

```text
- Spring Batch JobRepository 실행 없이 완료 처리
- ADM 정적 smoke만으로 배치 관제 완료 처리
- ghost recovery 없음
- worker heartbeat 없음
- lock 해제 기준 없음
- 재실행 가능 여부 판단 없음
- 브라우저 클릭 없이 UI 완료 처리
- 실행하지 않은 scheduler 검증을 성공으로 기록
```

---

# 2. 온라인/배치 로그 정책 + 거래 메타 자동 등록

## 2.1 작업 목표

파일 로그와 DB 로그의 기본 정책을 yml로 관리하고, ADM은 장애/이슈 대응용 override로 사용한다.
Controller/API 거래 메타를 자동 등록하고, ADM에서 거래별 로그레벨과 DB 로그 저장 여부를 관리한다.

## 2.2 기본 정책

파일 로그와 DB 로그는 모두 yml 기본 정책을 가진다.

```text
파일 로그:
- 기본적으로 항상 남긴다.
- yml 기준으로 기본 로그레벨을 관리한다.
- ADM override는 장애 대응 시 임시 로그레벨 상향에 사용한다.

DB 로그:
- yml 기준 기본 저장 정책을 가진다.
- 전체/모듈/주제영역/거래/Batch Job/Step 단위로 저장 여부를 제어한다.
- 용량 이슈를 고려하여 기본은 ERROR 또는 필요한 영역 중심으로 설정 가능해야 한다.
```

ADM 설정은 상시 원천이 아니라 임시 override다.

우선순위:

```text
1순위: ADM 활성 override
2순위: DB 운영 정책
3순위: application.yml 기본값
4순위: CPF 기본값
```

## 2.3 필수 DB 구조

```text
pfw_transaction_meta
- transaction_id
- transaction_name
- module_id
- domain_id
- http_method
- api_path
- controller_class
- handler_method
- active_yn
- default_file_log_level
- default_db_log_enabled
- default_db_log_level

pfw_log_policy
- policy_id
- target_type: GLOBAL / MODULE / DOMAIN / TRANSACTION / BATCH_JOB / BATCH_STEP
- target_id
- file_log_level
- db_log_enabled
- db_log_level
- log_payload_yn
- mask_yn
- effective_from
- effective_to
- active_yn

pfw_log_policy_override
- override_id
- target_type
- target_id
- file_log_level
- db_log_enabled
- db_log_level
- reason
- requested_by
- approved_by
- effective_from
- effective_to
- active_yn

pfw_log_policy_audit
- audit_id
- target_type
- target_id
- before_value
- after_value
- changed_by
- reason
- changed_at
```

테이블명은 기존 CPF SQL 표준과 충돌하지 않게 조정해도 되지만, 역할은 유지한다.

## 2.4 거래 메타 자동 등록

Controller/API 단위로 거래ID와 거래 논리명을 필수로 둔다.

권장 annotation:

```java
@CpfTransaction(
    transactionId = "EXS_INBOUND_RECEIVE",
    transactionName = "대외 수신 거래",
    module = "EXS",
    domain = "external-interface",
    defaultFileLogLevel = "INFO",
    defaultDbLogEnabled = true,
    defaultDbLogLevel = "ERROR"
)
```

등록 시점:

```text
1. Spring Context 초기화 완료
2. RequestMappingHandlerMapping 준비 완료
3. ApplicationReadyEvent 이후 거래 메타 스캔
4. Controller/API annotation과 RequestMapping 정보 수집
5. ADM 거래 메타 테이블에 upsert
6. 현재 스캔되지 않은 기존 거래는 inactive 처리
7. ADM 거래 로그/오류 로그/감사 로그 메뉴와 자동 연결
```

스캔은 비동기 또는 증분 upsert로 처리한다.
스캔 실패가 서비스 기동 실패로 이어지면 안 되며, 실패 이력은 PFW 운영 알림 또는 ADM 시스템 로그에 남긴다.

## 2.5 ADM 화면/API 기준

```text
거래 메타 목록:
- 거래ID
- 거래 논리명
- 모듈
- 업무/주제영역
- HTTP method
- API path
- Controller
- 활성 여부
- 현재 파일 로그레벨
- 현재 DB 로그 저장 여부
- 현재 DB 로그레벨

거래 상세:
- 거래 기본 정보
- 연결 Controller/API
- yml 기본 정책
- DB 운영 정책
- ADM override 정책
- 최근 거래 로그
- 최근 오류 로그
- 최근 감사 로그

거래별 로그 정책 설정:
- 파일 로그레벨 임시 변경
- DB 로그 저장 ON/OFF 임시 변경
- DB 로그레벨 임시 변경
- 적용 기간
- 변경 사유
- 변경 이력
```

## 2.6 필수 테스트

```text
- yml 기본 파일 로그 정책 로딩
- yml 기본 DB 로그 정책 로딩
- Controller 거래ID/논리명 스캔
- ADM 거래 메타 upsert
- 사라진 거래 inactive 처리
- 전체 DB 로그 OFF 시 DB 로그 미저장
- 특정 거래 DB 로그 ON 시 해당 거래 DB 로그 저장
- ADM override 적용 시 yml보다 우선 적용
- override 만료 후 기본 정책 복귀
- 변경 사유 누락 시 차단
- 변경 이력/감사 로그 저장
- 거래 상세에서 거래 로그/오류 로그/감사 로그 자동 연결
```

## 2.7 완료 불인정 기준

```text
- yml 기본 정책 없이 ADM 설정만 존재
- 거래 정책을 메모리만으로 관리
- DB 원천 테이블 없음
- Controller 거래ID/논리명 자동 등록 없음
- ADM 거래 목록에 API가 보이지 않음
- DB 로그 ON/OFF가 실제 DB 저장 여부에 영향 없음
- 변경 이력/감사 로그 없음
```

---

# 3. 온디맨드 배치 / 센터컷 / 스케일아웃 대응

## 3.1 작업 목표

온디맨드 배치와 센터컷성 배치를 CPF/BAT 기준으로 구현하고, 스케일아웃 시 병렬 처리, 중복 방지, 실패 복구, 미터링, ADM 관제가 가능하게 한다.

센터컷은 과도한 별도 시스템으로 만들지 않는다.
PFW/BAT 공통 실행 표준 + 각 주제영역 Handler/Service/Facade 처리 구조를 기본으로 한다.

## 3.2 온디맨드 배치 기준

온디맨드 배치는 정해진 스케줄이 아니라 사용자 요청, 운영자 요청, API 요청, 이벤트 요청에 의해 실행되는 배치다.

필수 구현:

```text
- 온디맨드 배치 실행 요청 API
- 실행 요청 파라미터 검증
- 요청자/요청 사유 기록
- 실행 대기/실행 중/성공/실패 상태 관리
- 중복 요청 방지
- transactionGlobalId 생성/전파
- JobExecutionId 연결
- ADM에서 실행 요청/결과 조회
- 실패 시 재실행
```

예제 위치:

```text
bat/src/main/java/cpf/bat/edu/ondemand
xyz/src/main/java/cpf/xyz/edu/batch/ondemand
```

## 3.3 센터컷 구조

역할 기준:

```text
PFW:
- 센터컷 표준 요청/상태/결과 모델
- transactionGlobalId parent/child 기준
- 공통 오류/상태 코드
- 공통 로그/감사 정책
- 중복 실행 방지 표준 API

BAT:
- CenterCutExecutor
- CenterCutWorker
- 대상 item claim/lock
- chunk/partition 처리
- 실패 item retry/recovery
- worker heartbeat
- JobExecutionId/StepExecutionId 연결
- 미터링 집계

각 주제영역:
- CenterCutHandler
- 실제 업무 Service/Facade 호출
- item 단위 업무 검증
- item 단위 성공/실패 결과 반환

ADM:
- 실행 요청
- 대상 주입
- 진행률 조회
- worker 상태 조회
- 실패 건 조회
- 재처리 요청
- 처리량/미터링 조회
```

BAT가 업무 DB를 직접 수정하지 않는다.
업무 처리는 각 주제영역 Handler/Service/Facade가 담당한다.

## 3.4 센터컷 대상 주입

대상 목록은 메모리만으로 관리하지 않고 DB에 확정 저장한다.

대상 주입 방식:

```text
- 대상 ID 목록 직접 입력
- CSV/파일 업로드
- 검색 조건 기반 대상 추출
- SQL/Mapper 기반 대상 추출
- 주제영역 대상 조회 Service/Facade 호출
- 실패 건 재처리 대상 자동 추출
```

권장 테이블:

```text
pfw_center_cut_request
- request_id
- job_id
- center_cut_type
- module_id
- domain_id
- requested_by
- request_reason
- total_count
- success_count
- fail_count
- skip_count
- retry_count
- status
- created_at
- started_at
- finished_at

pfw_center_cut_item
- request_id
- item_id
- business_key
- payload_ref 또는 payload_json
- status: READY / CLAIMED / RUNNING / SUCCESS / FAIL / SKIP / RETRY_WAIT / TIMEOUT / CANCELED
- retryable_yn
- retry_count
- worker_id
- error_code
- error_message
- transaction_global_id
- started_at
- finished_at

pfw_center_cut_metering
- request_id
- processed_count
- success_count
- fail_count
- tps
- avg_elapsed_ms
- max_elapsed_ms
- current_partition
- worker_count
- updated_at

pfw_center_cut_retry
- retry_id
- request_id
- item_id
- retry_reason
- requested_by
- status
- created_at
```

## 3.5 스케일아웃 / 중복 방지

CenterCutWorker는 여러 대가 동시에 떠도 같은 item을 중복 처리하지 않아야 한다.

필수 기준:

```text
- request_id + item_id 기준 중복 처리 방지
- item 상태 기반 claim 처리
- READY item만 worker가 선점
- 선점 시 CLAIMED 상태와 worker_id 기록
- 처리 시작 시 RUNNING 상태 기록
- worker heartbeat 기록
- 일정 시간 heartbeat가 없으면 timeout 처리
- timeout item은 재처리 가능 상태로 전환
- 동일 item의 SUCCESS 이후 재처리 차단
- retryable item만 재처리 허용
```

초기 구현은 Redis/Kafka/MQ 없이 DB 기반 claim/lock으로 처리한다.
실 broker는 향후 확장 가능 구조로 문서화한다.

## 3.6 센터컷 트랜잭션 기준

```text
- 전체 대상을 하나의 트랜잭션으로 묶지 않는다.
- partition 단위로 worker 분산 가능해야 한다.
- chunk 단위 commit을 기본으로 한다.
- item 단위 성공/실패 결과를 저장한다.
- 실패 item은 정책에 따라 skip/retry한다.
- 실패율 threshold 초과 시 Job 중단 가능해야 한다.
- parent transactionGlobalId는 센터컷 요청 단위다.
- child transactionGlobalId는 item 처리 단위다.
- JobExecutionId/StepExecutionId와 연결한다.
```

## 3.7 필수 테스트

```text
- 온디맨드 요청 정상
- 온디맨드 요청 사유 누락 차단
- 온디맨드 중복 요청 차단
- 센터컷 대상 주입 정상
- 단일 worker 정상 처리
- worker 2대 동시 claim 시 item 중복 처리 방지
- 동일 item 중복 처리 차단
- 일부 item 실패
- 실패 item 재처리
- SUCCESS item 재처리 차단
- worker heartbeat 저장
- heartbeat timeout item 복구
- chunk commit 확인
- 진행률/미터링 저장 확인
- ADM에서 결과/진행률/worker 상태 조회
```

실제 다중 worker 실행이 어려우면 테스트 코드에서 worker_id를 다르게 주고 claim 로직을 동시 호출하는 방식으로 mock 검증한다.

## 3.8 완료 불인정 기준

```text
- 센터컷 대상 item을 메모리에서만 관리
- worker 여러 대 실행 시 중복 처리 방지 기준 없음
- DB claim/lock 기준 없음
- worker heartbeat 없음
- 장애 복구 기준 없음
- 실패 item 재처리 없음
- 미터링/진행률 없음
- ADM 관제 없음
- BAT가 업무 DB를 직접 수정
- 각 주제영역 Handler를 호출하지 않음
- 테스트 없이 완료로 기록
```

---

# 4. BAT EDU / XYZ EDU 배치 샘플 보강

## 4.1 작업 목표

배치 개발자가 참고할 수 있는 BAT 전용 EDU와, 업무 개발자가 배치를 요청/연동할 수 있는 XYZ EDU 샘플을 분리해 제공한다.

## 4.2 BAT EDU 패키지

권장 패키지:

```text
bat/src/main/java/cpf/bat/edu/scheduled
bat/src/main/java/cpf/bat/edu/ondemand
bat/src/main/java/cpf/bat/edu/centercut
bat/src/main/java/cpf/bat/edu/chunk
bat/src/main/java/cpf/bat/edu/tasklet
bat/src/main/java/cpf/bat/edu/retry
bat/src/main/java/cpf/bat/edu/transaction
bat/src/main/java/cpf/bat/edu/servicecall
```

샘플 내용:

```text
- PFW CpfBatchLauncher 사용
- Job/Step 구성
- Job parameter
- transactionGlobalId 전파
- JobExecutionId/StepExecutionId 연결
- chunk commit
- tasklet 처리
- item 실패 처리
- skip/retry
- CenterCutWorker claim
- 실패 item 재처리
- 미터링 저장
- ADM 관제 연결
```

## 4.3 XYZ EDU 배치 샘플

권장 패키지:

```text
xyz/src/main/java/cpf/xyz/edu/batch
xyz/src/main/java/cpf/xyz/edu/batch/ondemand
xyz/src/main/java/cpf/xyz/edu/batch/centercut
xyz/src/main/java/cpf/xyz/edu/batch/transaction
```

샘플 내용:

```text
- 온라인 API에서 온디맨드 배치 요청
- 온라인 업무 Service를 센터컷에서 재사용
- CenterCutHandler 구현
- parent/child transactionGlobalId
- 배치 실행 결과 조회
- 실패 건 재처리 요청
- 권한/감사/로그 정책 적용
```

## 4.4 완료 기준

```text
- BAT EDU에 정기/온디맨드/센터컷/chunk/tasklet/retry/transaction 예제가 있다.
- XYZ EDU에 업무 개발자가 배치 요청 또는 CenterCutHandler를 작성하는 예제가 있다.
- 개발 가이드와 배치 개발 가이드에서 EDU 위치를 설명한다.
- 테스트 또는 smoke 호출 예시가 있다.
```

---

# 5. 배치 개발 가이드 / 트랜잭션 가이드 정본화

## 5.1 신규 문서

아래 문서를 신규 또는 정본 문서로 추가한다.

```text
specs/배치_개발_가이드.html
specs/트랜잭션_가이드.html
```

## 5.2 배치 개발 가이드 필수 내용

```text
- CPF 배치 아키텍처
- PFW Batch 공통 API
- BAT 실행 구현체 역할
- Spring Batch JobRepository 기준
- BATCH_*와 pfw_batch_* 관계
- 정기 배치
- 수동 실행
- 온디맨드 배치
- 센터컷성 배치
- Job/Step 설계
- chunk/tasklet/partition
- retry/skip/restart
- scheduler
- JobExecutionId/StepExecutionId 연결
- transactionGlobalId 연결
- worker heartbeat
- ghost recovery
- scale-out worker
- ADM 배치 관제
- BAT EDU 예제
- XYZ EDU 배치 호출 예제
- 테스트 방법
```

## 5.3 트랜잭션 가이드 필수 내용

```text
- 온라인 API 트랜잭션
- Service 계층 transaction 기준
- Controller에서 DB 직접 접근 금지
- validation/권한/사유 검증 우선
- 감사 로그 트랜잭션
- 다운로드/마스킹 해제 트랜잭션
- EXS 수신 트랜잭션
- EXS 송신 트랜잭션
- 외부 호출과 DB lock 분리 기준
- 배치 chunk 트랜잭션
- 센터컷 item 트랜잭션
- skip/retry/rollback 기준
- parent/child transactionGlobalId 기준
- 실패 item 재처리 기준
- 예제 코드
```

## 5.4 문서 완료 기준

```text
- 문서가 단순 요약이 아니라 개발자가 실제 구현할 수 있는 수준이어야 한다.
- 주요 코드 경로, API, DB 테이블, 테스트 명령이 포함되어야 한다.
- EDU 샘플 위치와 연결되어야 한다.
- 기능 구현 매트릭스에 신규 문서 반영 여부가 기록되어야 한다.
```

---

# 6. 검증 명령 공통 기준

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat clean qualityGate --offline
.\gradlew.bat compileJava --offline
.\gradlew.bat test --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-transaction-id-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sample-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
```

가능하면 앱 기동 후 실행한다.

```powershell
.\gradlew.bat runLocalServices --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

실행하지 못한 검증은 성공으로 기록하지 않는다.

---

# 7. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 요청사항별로 아래를 남긴다.

```text
- 실제 확인한 내용
- 개발한 기능
- package 경로
- 생성/수정한 소스 파일
- Controller/API
- Service
- Repository/Mapper
- DTO
- SQL/Flyway/테이블
- ADM UI/API wrapper
- Swagger
- EDU 샘플
- 테스트 명령
- 테스트 결과
- 실패 사유
- 미검증 사유
- 남은 보완 사항
- 최종 판정
```

완료하지 않은 항목을 완료로 기록하지 않는다.
실행하지 않은 검증을 성공으로 기록하지 않는다.
Codex 완료 메시지와 리포트 내용이 다르면 안 된다.
