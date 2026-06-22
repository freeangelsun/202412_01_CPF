# CPF_REQUEST_001: ADM 배치 관제 / Spring Batch E2E / Ghost Recovery 완성 요청

## 0. 이번 작업 범위 고정

이번 작업은 아래 선택 범위만 완료한다.

```text
선택 범위:
- ADM 배치 관제
- Spring Batch E2E 실행 검증
- BAT 실행 구현체 필요 여부 판단 및 필요 시 최소 BAT 모듈 생성
- Batch Worker heartbeat
- Ghost batch 감지/조치
- 배치 중복 실행 방지
- 배치 수동 실행/중지/재실행
- 배치 로그/실행 이력/Step 이력 ADM 조회
```

이번 작업에서 아래 항목은 구현하지 않는다.
영향이 있으면 `CPF_STABILIZATION_REPORT.html`의 다음 보강 후보로만 기록한다.

```text
이번 실행 제외:
- 온라인/배치 로그 정책 + 거래 메타 자동 등록 전체 구현
- 거래별 로그레벨/DB 로그 저장 override 전체 구현
- 온디맨드 배치 전체 구현
- 센터컷/CenterCutWorker/대상 주입/미터링 전체 구현
- BAT EDU / XYZ EDU 대규모 보강
- 배치 개발 가이드 전체 정본화
- 트랜잭션 가이드 전체 정본화
- Redis/Kafka/MQ 실 broker 검증
```

단, 이번 선택 범위인 ADM 배치 관제와 Spring Batch E2E를 완료하는 데 필요한 최소 SQL, API, Service, DTO, Mapper, ADM UI/API wrapper, Swagger, EDU, README/specs 문서, 리포트는 반드시 갱신한다.

요청 파일은 읽기 전용으로 둔다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 정확히 기록한다.

---

## 1. BAT 모듈 처리 기준

현재 repository에 `bat` 모듈이 없으면 먼저 아래 기준으로 판단한다.

```text
판단 기준:
- PFW는 배치 공통 API를 제공하는 영역이다.
- ADM은 배치 운영 관제 영역이다.
- BAT는 PFW 배치 공통 API를 사용하는 Spring Batch 실행 구현체다.
- Spring Batch E2E 실행 구현체가 기존 모듈에 없거나 책임 경계가 불명확하면 BAT 모듈을 신규 생성한다.
```

BAT 모듈 생성이 필요하면 최소 범위로만 생성한다.

```text
BAT 신규 생성 시 필수:
- settings.gradle include 'bat' 반영
- bat/build.gradle 구성
- bat/src/main/java/cpf/bat/BatApplication.java 또는 실행 구성
- bat/src/main/java/cpf/bat/job
- bat/src/main/java/cpf/bat/step
- bat/src/main/java/cpf/bat/operation
- bat/src/test/java 테스트
- PFW CpfBatchLauncher 또는 기존 PFW 배치 공통 API 사용
- 독립 JobRepository 임의 생성 금지
```

BAT 모듈을 생성하지 않는 경우에는 그 사유를 리포트에 기록한다.

```text
BAT 모듈 미생성 허용 조건:
- 기존 PFW/ADM/XYZ 구조만으로 Spring Batch E2E 실행 구현체가 명확히 존재함
- 책임 경계가 문서에 명확히 설명됨
- :bat:test를 검증 명령에 포함하지 않음
```

BAT 모듈을 생성한 경우에만 `.\gradlew.bat :bat:test --offline`를 실행한다.
BAT 모듈을 생성하지 않았는데 `:bat:test` 실패를 검증 실패로 만들지 않는다.

---

## 2. 작업 목표

운영자가 ADM에서 배치 Job, 실행 이력, Step 이력, 스케줄, 수행 대상, worker 상태, ghost 상태, 실패, 중지, 재실행, 로그를 확인하고 조치할 수 있게 만든다.

완료는 코드 존재가 아니라 아래 완료 판정표를 통과해야 인정한다.

```text
완료 필수 요소:
- Java package/source
- Controller/API
- Service
- Repository/Mapper
- DTO/Validation
- SQL/Flyway/all_install/smoke
- DB 테이블 COMMENT와 공통 감사 컬럼
- ADM UI 또는 API wrapper
- Swagger Tag/Operation/Schema
- 최소 EDU 샘플 또는 개발 가이드 연결
- 정상/오류/권한/DB 테스트
- 실행 명령과 실제 결과
- README/specs/기능_구현_매트릭스/CPF_STABILIZATION_REPORT.html 반영
```

Controller만 있거나, 문서만 있거나, 하드코딩 응답이면 완료가 아니다.
테스트하지 않은 기능은 완료가 아니다.
실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 3. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서 기준으로 현재 상태를 먼저 판정한다.

아래 항목을 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```text
현재 상태 판정 항목:
- bat 모듈 존재 여부
- 기존 배치 관련 package
- 기존 ADM 배치 Controller/API
- 기존 Service
- 기존 Repository/Mapper
- 기존 DTO
- 기존 SQL 테이블
- 기존 ADM UI/API wrapper
- 기존 Swagger 문서
- 기존 테스트
- 미구현 항목
- 부분 구현 항목
- 미검증 항목
```

상태값은 아래만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

---

## 4. DB / SQL 완료 기준

Spring Batch 표준 테이블과 CPF 운영 메타 테이블의 책임을 분리한다.

Spring Batch `BATCH_*` 테이블은 표준 JobRepository가 관리한다.
CPF 운영 관제 메타는 `pfw_batch_*` 테이블로 별도 관리하고, `JobExecutionId`, `StepExecutionId`, `transactionGlobalId`로 연결한다.

필수 확인/보강 대상:

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
- pfw_batch_step_execution 또는 기존 CPF 표준에 맞는 동등한 Step 운영 메타 테이블
- pfw_batch_lock
- pfw_batch_worker
- pfw_batch_job_relation
- pfw_batch_execution_target
- pfw_batch_ghost_event 또는 동등한 ghost/recovery 이력 테이블
```

필수 기준:

```text
- all_install SQL에 반영
- Flyway 또는 기준 SQL에 반영
- smoke SQL에 반영
- 테이블 COMMENT 존재
- 주요 컬럼 COMMENT 존재
- 공통 감사 컬럼 존재
- FK/index 기준 확인
- app 계정 DDL 차단, migration 계정 DDL 허용 기준 유지
```

`40_business_sample_schema.sql` 파일명 변경 작업은 반복하지 않는다.
다만 README, specs, SQL 가이드, 개발 가이드, Flyway, all_install, 검사 스크립트에 구명칭 `40_business_sample_schema.sql` 참조가 남아 있으면 `40_business_modules_schema.sql` 기준으로 전수 정리한다.

---

## 5. ADM 배치 API 완료 기준

ADM 배치 API는 최소 아래 기능을 제공해야 한다.

```text
필수 API:
- Job 목록 조회
- Job 상세 조회
- Job 실행 이력 조회
- Step 실행 이력 조회
- 수동 실행
- 중지 요청
- 재실행 요청
- 스케줄 조회
- 스케줄 등록/수정
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
- 배치 로그 조회
```

각 API는 아래를 갖춰야 한다.

```text
- Controller
- Request DTO
- Response DTO
- Validation
- Service
- Repository/Mapper
- 권한 체크
- 오류 응답
- Swagger Tag/Operation/Schema
- 테스트
```

수동 실행, 중지 요청, 재실행 요청 API/Service는 이번 작업의 핵심이므로 구현 필수다.
미구현 사유만 기록하고 완료 처리하지 않는다.

---

## 6. Service / Repository / DTO 기준

아래 역할이 실제 코드로 존재해야 한다.
명칭은 기존 프로젝트 컨벤션에 맞춰도 되지만 역할은 충족해야 한다.

```text
필수 Service 역할:
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

필수 Repository/Mapper 역할:

```text
- Job 목록/상세 조회
- Execution 조회
- Step 조회
- Schedule 조회/수정
- Relation 조회
- Target 조회
- Worker heartbeat 저장/조회
- Ghost candidate 조회
- Ghost 조치 이력 저장
- Lock 조회/획득/해제
- 재실행 가능 여부 조회
```

필수 DTO:

```text
- BatchJobSearchRequest
- BatchJobResponse
- BatchExecutionResponse
- BatchStepExecutionResponse
- BatchRunRequest
- BatchStopRequest
- BatchRestartRequest
- BatchScheduleRequest
- BatchWorkerStatusResponse
- BatchGhostCandidateResponse
- BatchGhostActionRequest
- BatchLockResponse
```

기존 DTO 명칭이 있으면 재사용하되, 역할 누락이 없도록 보강한다.

---

## 7. Spring Batch E2E 완료 기준

Spring Batch 실행 흐름이 실제로 검증되어야 한다.

필수 검증:

```text
- JobLauncher를 통한 Job 실행
- JobRepository에 JobExecution 저장
- StepExecution 저장
- CPF pfw_batch_execution과 JobExecutionId 연결
- CPF pfw_batch_step_execution 또는 동등한 Step 운영 메타와 StepExecutionId 연결
- transactionGlobalId 생성/저장
- Job 성공 상태 저장
- Job 실패 상태 저장
- Step 실패 상태 저장
- 실패 Job 재실행 가능 여부 판단
```

Spring Batch 표준 JobRepository를 우회하거나 독립 JobRepository를 임의 생성하지 않는다.

BAT 모듈을 생성했다면 Spring Batch E2E 실행 구현은 BAT에서 수행하고, PFW 공통 API를 사용해야 한다.
BAT 모듈을 생성하지 않았다면 기존 모듈의 실행 구현체가 PFW 공통 API를 어떻게 사용하는지 리포트와 문서에 명확히 기록한다.

---

## 8. Ghost Recovery 완료 기준

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
- 조치자 기록
- 조치 감사 로그 저장
- lock 해제 기준
- 재실행 가능 여부 판단
- 동일 Job 중복 실행 방지
```

deploy/restart 중 shutdown hook으로 정리 가능한 범위는 정리하되, 강제 종료는 hook이 보장되지 않으므로 heartbeat 기반 recovery를 반드시 둔다.

ADM ghost 조치에는 사유가 필수다.
사유 없는 ghost 조치는 차단한다.

---

## 9. 중복 실행 방지 / Lock 기준

동일 Job이 정책상 중복 실행 불가인 경우, 다중 WAS/다중 Worker 환경에서도 중복 실행이 차단되어야 한다.

필수 기준:

```text
- job_id + business_date 또는 job parameter 기준 lock
- lock owner serverInstanceId 기록
- lock 획득 시각 기록
- lock 만료 기준
- 정상 종료 시 lock 해제
- 실패/timeout/ghost 조치 시 lock 해제 기준
- 중복 실행 요청 시 표준 오류 응답
- 중복 실행 차단 테스트
```

메모리 lock만으로 완료 처리하지 않는다.
DB 기반 lock 또는 동등한 영속 lock 기준이 있어야 한다.

---

## 10. ADM UI / API wrapper 기준

ADM 화면 또는 API wrapper에서 아래를 확인할 수 있어야 한다.

```text
필수 화면/기능:
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
- lock 상태
- 로그 보기
- 실패 사유 보기
```

ADM UI는 정적 파일 존재, 화면 marker 존재, API wrapper 존재만으로 완료 처리하지 않는다.

브라우저 클릭 검증을 수행하지 못한 경우, UI 검증 상태는 반드시 `미검증`으로 기록한다.

단, API/Service/Repository/Mapper/DTO/SQL/테스트가 완료된 경우에는 아래처럼 구현 상태와 실클릭 검증 상태를 분리해서 판정한다.

```text
예시 판정:
- 배치 관제 API 구현 상태: 완료
- 배치 관제 Service/Mapper/SQL 구현 상태: 완료
- ADM UI/API wrapper 구현 상태: 완료
- ADM 브라우저 실클릭 검증 상태: 미검증
- 최종 판정: 완료 아님 / UI 실클릭 미검증 포함
```

브라우저 클릭 검증을 하지 않았는데 `ADM UI 완료`, `브라우저 검증 성공`, `운영자 화면 검증 완료`로 기록하지 않는다.

Node/Playwright 또는 브라우저 자동화 환경이 없으면 작업을 중단하지 말고, 가능한 정적 UI smoke와 API wrapper 테스트를 수행한 뒤 브라우저 실클릭은 미검증으로 남긴다.

최종 완료로 기록하려면 아래 중 하나를 만족해야 한다.

```text
1. 실제 브라우저 클릭 자동화 또는 수동 클릭 검증 결과가 CPF_STABILIZATION_REPORT.html에 기록됨
2. 이번 요청 범위에서 브라우저 실클릭 검증이 명시적으로 제외되었고, 최종 판정에서 UI 실클릭 미검증이 분리 기록됨
```

---

## 11. Swagger / OpenAPI 기준

배치 관제 API는 Swagger/OpenAPI에 노출되어야 한다.

필수 기준:

```text
- ADM Batch 관련 Tag
- API별 Operation summary
- Request schema
- Response schema
- 오류 응답 schema
- 권한 필요 여부 설명
```

OpenAPI smoke는 앱 기동이 필요하다.
앱을 기동하지 못했으면 OpenAPI 검증을 성공으로 기록하지 않는다.

앱 기동 검증을 수행할 경우 아래 기준을 따른다.

```text
- 프로세스 PID 추적
- health check
- OpenAPI smoke 실행
- 종료/cleanup
- 기동 실패 시 로그와 실패 사유 기록
```

앱 기동/종료 자동화가 안전하지 않거나 실패하면 OpenAPI/브라우저 검증은 미검증으로 남긴다.
이 경우에도 코드, SQL, 단위/통합 테스트, 정적 smoke, 문서 검증은 계속 진행한다.

Node/Playwright가 없으면 브라우저 클릭 검증은 미검증으로 남기고, 설치 요구로 작업을 중단하지 않는다.

---

## 12. EDU / 문서 최소 반영 기준

이번 작업에서 배치 개발 가이드 전체 정본화는 하지 않는다.
다만 ADM 배치 관제와 Spring Batch E2E, Ghost Recovery에 필요한 최소 문서는 갱신한다.

필수 반영:

```text
- README.md: 배치 관제 검증 상태 요약
- specs/관리자_가이드.html: ADM 배치 관제 사용 방법
- specs/배치_개발_가이드.html: 없으면 신규 생성, 있으면 이번 범위만 보강
- specs/기능_구현_매트릭스.html: 이번 작업 결과 반영
- specs/SQL_가이드.html: 배치 관련 테이블/검증 명령 반영
- CPF_STABILIZATION_REPORT.html: 실제 작업 결과 기록
```

EDU는 최소 아래 중 하나 이상을 제공한다.

```text
- 단순 Job/Step 실행 예제
- 실패 Job 재실행 예제
- worker heartbeat/ghost recovery 흐름 예제
- ADM 배치 관제 호출 예제
```

대규모 BAT/XYZ EDU 전체 보강은 이번 범위에서 제외하고 다음 보강 후보로 기록한다.

---

## 13. 필수 테스트

아래 테스트를 추가 또는 보강한다.

```text
정상 케이스:
- Job 정상 실행
- Step 정상 실행
- JobExecutionId 저장
- StepExecutionId 저장
- transactionGlobalId 저장
- ADM Job 목록 조회
- ADM Job 상세 조회
- ADM 실행 이력 조회
- ADM Step 이력 조회

오류 케이스:
- Job 실패 처리
- Step 실패 처리
- 잘못된 Job parameter 차단
- 중복 실행 차단
- 중지 불가 상태 중지 요청 차단
- 재실행 불가 상태 재실행 요청 차단
- ghost 조치 사유 누락 차단

운영 케이스:
- 수동 실행
- 중지 요청
- 실패 Job 재실행
- worker heartbeat 저장
- heartbeat timeout 감지
- ghost candidate 조회
- ghost 조치
- lock 해제 기준
- worker 상태 조회

DB 확인:
- BATCH_JOB_EXECUTION 저장 확인
- BATCH_STEP_EXECUTION 저장 확인
- pfw_batch_execution 저장 확인
- pfw_batch_step_execution 또는 동등한 Step 운영 메타 저장 확인
- pfw_batch_worker 저장 확인
- pfw_batch_lock 저장 확인
- ghost 조치 이력 저장 확인
```

실제 scheduler 실행이 어렵다면 테스트에서 scheduler mock 또는 JobLauncherTestUtils 등 가능한 방식으로 검증하고, 실 scheduler는 미검증 사유를 남긴다.

---

## 14. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat test --offline
.\gradlew.bat compileJava --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1

mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

BAT 모듈을 신규 생성한 경우에만 아래 명령도 실행한다.

```powershell
.\gradlew.bat :bat:test --offline
```

BAT 모듈을 생성하지 않은 경우에는 `:bat:test`를 실행하지 말고, BAT 미생성 사유와 대체 검증 명령을 리포트에 기록한다.

가능하면 앱 기동 후 아래도 실행한다.

```powershell
.\gradlew.bat runLocalServices --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1
```

앱 기동 검증은 PID 추적, health check, smoke 실행, 종료/cleanup까지 포함한다.
정상 종료가 보장되지 않으면 OpenAPI/브라우저 검증은 미검증으로 남긴다.

---

## 15. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 이번 선택 범위에 대해 아래를 기록한다.

```text
[ADM 배치 관제 / Spring Batch E2E / Ghost Recovery]

현재 상태 판정:
- 기존 구현:
- 부분 구현:
- 미구현:
- 미검증:
- BAT 모듈 생성 여부:
- BAT 모듈 생성 또는 미생성 사유:

개발 기능:
- package:
- Controller/API:
- Service:
- Repository/Mapper:
- DTO:
- SQL 테이블:
- ADM UI/API wrapper:
- Swagger:
- EDU:
- 문서:

테스트:
- 테스트 명령:
- 정상 케이스 결과:
- 오류 케이스 결과:
- 운영 케이스 결과:
- DB 확인 결과:
- UI 구현/API wrapper 확인 결과:
- UI 브라우저 실클릭 검증 결과:
- OpenAPI 확인 결과:

미검증:
- 미검증 항목:
- 미검증 사유:
- 다음 조치:

최종 판정:
- 완료 / 부분 구현 / 미검증 / 실패 / 재확인 필요
```

완료하지 않은 항목을 완료로 기록하지 않는다.
실행하지 않은 검증을 성공으로 기록하지 않는다.
구현 완료와 브라우저 실클릭 미검증을 반드시 분리해서 기록한다.
Codex 완료 메시지와 리포트 내용이 서로 다르게 작성되지 않도록 한다.

---

## 16. 완료 기준

이번 작업은 아래가 모두 충족될 때만 완료로 기록한다.

```text
- Spring Batch Job 실행 E2E가 검증되었다.
- JobExecutionId와 StepExecutionId가 CPF 운영 메타와 연결된다.
- ADM에서 Job 목록/상세/실행 이력/Step 이력을 조회할 수 있다.
- ADM에서 수동 실행/중지/재실행 API/Service가 구현되어 있다.
- worker heartbeat가 저장된다.
- ghost candidate를 감지할 수 있다.
- ADM에서 ghost 조치가 가능하다.
- ghost 조치 사유와 감사 로그가 남는다.
- 중복 실행 방지 lock이 동작한다.
- SQL/all_install/smoke에 반영되어 있다.
- Swagger/OpenAPI 문서가 반영되어 있다.
- 최소 EDU 또는 개발 가이드 연결이 있다.
- 정상/오류/운영/DB 테스트가 있다.
- 기능 구현 매트릭스와 리포트가 실제 상태를 반영한다.
- BAT 모듈 생성 여부와 사유가 명확히 기록되어 있다.
- UI 실클릭을 하지 못한 경우 최종 판정에서 UI 실클릭 미검증이 분리 기록되어 있다.
```

---

## 17. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
- Spring Batch JobRepository 실행 없이 완료 처리
- ADM 정적 smoke만으로 배치 관제 완료 처리
- Controller만 있고 Service/Mapper/SQL이 없음
- SQL만 있고 API/Service가 없음
- 문서만 있고 구현이 없음
- 수동 실행/중지/재실행 API/Service가 없음
- ghost recovery 없음
- worker heartbeat 없음
- lock 해제 기준 없음
- 재실행 가능 여부 판단 없음
- 중복 실행 방지 없음
- ADM에서 실행 이력/Step 이력을 조회할 수 없음
- 브라우저 클릭 검증을 수행하지 않았는데 ADM UI 검증 완료로 기록
- 정적 marker 확인만으로 운영자 화면 검증 완료로 기록
- API/Service 구현 완료와 UI 실클릭 미검증을 구분하지 않음
- 앱 미기동인데 OpenAPI 성공으로 기록
- 실행하지 않은 scheduler 검증을 성공으로 기록
- BAT 모듈을 생성했는데 settings.gradle/build.gradle/테스트가 연결되지 않음
- BAT 모듈을 생성하지 않았는데 배치 실행 구현체 책임 경계가 불명확함
- 테스트 없이 완료로 기록
- CPF_STABILIZATION_REPORT.html과 실제 소스 상태가 다름
```

---

## 18. 다음 보강 후보로만 기록할 항목

이번 작업에서 아래는 구현하지 말고, 필요한 경우 다음 보강 후보로만 기록한다.

```text
- 온라인/배치 로그 정책 + 거래 메타 자동 등록
- 거래별 파일 로그/DB 로그 override
- Batch Job/Step별 로그 정책 override
- 온디맨드 배치
- 센터컷 대상 주입/결과/재처리/미터링
- CenterCutWorker scale-out
- BAT EDU 전체 보강
- XYZ EDU 배치 샘플 전체 보강
- 배치 개발 가이드 대규모 정본화
- 트랜잭션 가이드 대규모 정본화
- Redis/Kafka/MQ 실 broker 검증
```
