# CPF_REQUEST_005: BAT 실행 중 Heartbeat / 처리율 갱신 / Ghost 자동 감지 완성 요청

## 0. 이번 작업 범위 고정

이번 작업은 BAT 독립 실행 모듈이 생성된 상태를 전제로, 실제 장시간 Batch 실행 중 worker heartbeat, 처리율, 진행률, ghost candidate 자동 감지를 구현하고 검증하는 작업이다.

이번 작업에서 수행할 범위는 아래로 고정한다.

```text
선택 범위:
- BAT 실행 중 worker heartbeat 주기 갱신
- Job/Step listener 기반 실행 상태 갱신
- 처리 건수, 성공 건수, 실패 건수, 진행률, 처리율 갱신
- 장시간 실행 smoke Job 추가 또는 기존 Job 보강
- heartbeat timeout 기준 ghost candidate 자동 감지
- ghost candidate 자동 등록 또는 상태 갱신
- ADM에서 자동 감지된 ghost candidate 조회 가능
- ghost 조치 사유/조치자/감사 로그 기준 유지
- 기존 pfw_batch_worker / pfw_batch_execution / pfw_batch_step_execution / pfw_batch_ghost_event 활용
- README / 배치 개발 가이드 / 운영 매뉴얼 / 기능 구현 매트릭스 / CPF_STABILIZATION_REPORT.html 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.
필요하면 `CPF_STABILIZATION_REPORT.html`의 다음 보강 후보로만 기록한다.

```text
이번 실행 제외:
- 온디맨드 배치 전체 구현
- 센터컷 기본 구현체 전체 구현
- 센터컷 모수/item/result 테이블 구현
- 업무별 커스텀 센터컷 adapter 구현
- ADM 브라우저 실제 클릭 자동화
- 온라인/배치 로그 정책 + 거래 메타 자동 등록
- Redis/Kafka/MQ 실 broker 장애 시나리오 검증
- BAT EDU / XYZ EDU 대규모 보강
- 전체 MariaDB 설치 SQL 재실행
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 상위 아키텍처 전제

이번 작업은 BAT가 실제 실행 중 상태를 주기적으로 남기고, ADM이 이를 관제할 수 있게 하는 작업이다.

아래 책임 경계를 지킨다.

```text
PFW:
- Batch 공통 API, 공통 repository, 공통 status/error/metadata 기준 제공
- transactionGlobalId, JobExecutionId, StepExecutionId 연결 기준 제공
- heartbeat/ghost 판정에 필요한 공통 service/repository 기준 제공
- 실제 장시간 Job 실행 runtime 자체를 소유하지 않음

BAT:
- 독립 실행 가능한 Spring Boot batch worker
- 실제 Job/Step 실행 주체
- 실행 중 heartbeat 갱신 주체
- listener를 통해 Job/Step 상태와 처리율을 CPF 운영 메타에 반영
- 향후 온디맨드/센터컷 실행 기반

ADM:
- 실행 로직을 직접 소유하지 않음
- worker 상태, heartbeat, ghost candidate, lock, 실행 이력, step 이력 관제
- ghost 조치, lock 조치, 재실행 가능 여부 확인
```

센터컷 전제도 훼손하지 않는다.

```text
센터컷 전제:
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않는다.
- PFW는 표준 인터페이스, 상태, 오류, 로그, 감사, transactionGlobalId 기준만 제공한다.
- BAT는 향후 기본 센터컷 모수/item/result 테이블과 기본 구현체를 제공한다.
- 업무 주제영역은 커스텀 모수/item/result 테이블을 추가하고 adapter로 BAT와 연동할 수 있어야 한다.
```

이번 작업에서 센터컷을 구현하지 않는다.
단, 향후 센터컷 확장을 방해하는 구조 변경은 하지 않는다.

---

## 2. 먼저 현재 상태 판정

작업 시작 전 실제 소스/SQL/문서 기준으로 현재 상태를 먼저 판정한다.

아래 항목을 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```text
현재 상태 판정 항목:
- bat 모듈 존재 여부
- BAT health API 존재 여부
- BAT 정상/실패 smoke Job 존재 여부
- smoke-bat-runtime.ps1 존재 여부
- pfw_batch_worker 테이블 구조
- pfw_batch_execution 테이블 구조
- pfw_batch_step_execution 테이블 구조
- pfw_batch_ghost_event 테이블 구조
- 기존 worker heartbeat 갱신 방식
- 기존 ghost candidate 조회/조치 방식
- ADM ghost candidate 조회 API 존재 여부
- ADM ghost 조치 API 존재 여부
- 기존 배치 개발 가이드와 운영 매뉴얼의 heartbeat/ghost 설명 상태
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

Codex 완료 메시지나 과거 리포트만 믿지 말고 실제 파일 기준으로 확인한다.

---

## 3. BAT listener / heartbeat 구현 기준

BAT는 Job/Step 실행 중 주기적으로 worker heartbeat를 갱신해야 한다.

필수 구현 기준:

```text
- JobExecutionListener 또는 동등한 listener
- StepExecutionListener 또는 동등한 listener
- heartbeat interval 설정
- heartbeat timeout 설정
- workerId 기록
- serverInstanceId 기록
- JobExecutionId 기록
- StepExecutionId 기록
- 마지막 heartbeat 시각 갱신
- 현재 실행 중인 jobId / executionId / stepId 연결
- 실행 중 상태 RUNNING 갱신
- 정상 종료 시 COMPLETED 갱신
- 실패 종료 시 FAILED 갱신
```

설정은 yml 기본값으로 제공한다.

```text
예시 설정:
cpf.batch.worker.heartbeat-interval-seconds
cpf.batch.worker.heartbeat-timeout-seconds
cpf.batch.worker.server-instance-id
cpf.batch.worker.worker-id
cpf.batch.ghost-detection.enabled
cpf.batch.ghost-detection.interval-seconds
```

설정명은 기존 프로젝트 컨벤션에 맞춰 조정할 수 있다.
단, 기본값과 override 가능 여부를 문서에 명확히 적는다.

---

## 4. 처리율 / 진행률 갱신 기준

장시간 Job 실행 중 ADM에서 진행 상황을 볼 수 있어야 한다.

필수 갱신 항목:

```text
- totalCount
- processedCount
- successCount
- failureCount
- skipCount
- retryCount
- progressRate
- tps 또는 처리율
- avgElapsedMs
- maxElapsedMs
- currentStepName
- currentStatus
- lastHeartbeatAt
```

기존 테이블에 컬럼이 없으면 아래 기준으로 처리한다.

```text
우선순위:
1. 기존 pfw_batch_execution / pfw_batch_step_execution 컬럼 활용
2. 기존 컬럼으로 부족하면 최소 SQL 변경
3. SQL 변경 시 Flyway migration, all_install, smoke SQL, SQL 가이드, 기능 매트릭스, 리포트 반영
```

SQL 변경이 없으면 “기존 테이블 활용, SQL 변경 없음”으로 리포트에 기록한다.

---

## 5. 장시간 실행 smoke Job 기준

즉시 끝나는 Job만으로는 heartbeat 갱신을 검증할 수 없다.
이번 작업에서는 heartbeat가 여러 번 갱신될 수 있는 장시간 smoke Job을 추가하거나 기존 smoke Job을 보강한다.

필수 기준:

```text
- 최소 10초 이상 실행되는 smoke Job 또는 Step
- 실행 중 heartbeat가 2회 이상 갱신되는지 확인
- 실행 중 step 상태가 RUNNING으로 조회되는지 확인
- 완료 후 상태가 COMPLETED로 변경되는지 확인
- 실패 케이스에서 상태가 FAILED로 변경되는지 확인
```

테스트 시간이 너무 길어지지 않도록 기본 smoke는 짧게 유지하되, heartbeat 검증에 필요한 최소 시간은 확보한다.

---

## 6. Ghost 자동 감지 기준

기존 ghost 후보 조회/조치가 수동 중심이었다면, 이번 작업에서는 timeout 기준으로 ghost candidate를 자동 감지해야 한다.

필수 구현 기준:

```text
- RUNNING 상태 execution 조회
- 마지막 heartbeat 시각과 현재 시각 비교
- heartbeat timeout 초과 시 ghost candidate 판정
- ghost candidate 이력 저장 또는 상태 갱신
- pfw_batch_ghost_event 또는 동등 테이블에 감지 이력 기록
- 중복 ghost event 생성 방지
- ADM에서 자동 감지된 ghost candidate 조회 가능
- 운영자 조치 전에는 임의로 lock을 해제하지 않음
```

ghost 자동 감지는 실행 주체를 명확히 한다.

```text
허용 구조:
- PFW 공통 GhostDetectionService 제공
- ADM 또는 BAT가 scheduler로 해당 service를 호출
- 기본값은 운영 관제 관점에서 ADM scheduler 또는 별도 운영 scheduler가 실행해도 됨
- BAT worker가 자기 heartbeat를 남기고, 감지 service는 공통 메타를 기준으로 판단
```

어느 모듈이 scheduler를 갖는지 리포트와 문서에 명확히 기록한다.
ADM이 ghost 감지를 수행하더라도 Job/Step 실행 로직을 소유하면 안 된다.

---

## 7. ADM 관제 연동 기준

ADM에서는 heartbeat와 ghost 자동 감지 결과를 조회할 수 있어야 한다.

필수 확인:

```text
- worker 상태 조회 API에서 lastHeartbeatAt 확인
- worker 상태 조회 API에서 현재 실행 중 Job/Step 확인
- batch execution 조회에서 progressRate 또는 처리 건수 확인
- batch step execution 조회에서 처리 건수/상태 확인
- ghost candidate 조회 API에서 자동 감지된 후보 확인
- ghost 조치 API에서 조치 사유 필수
- ghost 조치 감사 로그 저장
```

기존 ADM API를 확장할 경우 Controller/Service/DTO/Mapper/Swagger/테스트를 함께 맞춘다.

정적 UI marker만으로 ADM 관제 완료 처리하지 않는다.
브라우저 실제 클릭 검증은 이번 범위가 아니므로 미검증 또는 다음 보강 후보로 남긴다.

---

## 8. SQL / Flyway / all_install 기준

이번 작업은 가능하면 기존 테이블을 사용한다.

기존 사용 우선 대상:

```text
- pfw_batch_worker
- pfw_batch_execution
- pfw_batch_step_execution
- pfw_batch_ghost_event
- pfw_batch_lock
- BATCH_JOB_EXECUTION
- BATCH_STEP_EXECUTION
```

신규 컬럼 또는 신규 테이블이 필요한 경우에만 SQL을 변경한다.

SQL 변경 시 필수:

```text
- Flyway migration 추가
- all_install SQL 반영
- smoke SQL 반영
- table comment / column comment
- 공통 감사 컬럼
- FK/index
- README / SQL 가이드 / 기능 매트릭스 / 리포트 반영
```

센터컷 대량 모수/item/result 테이블은 이번 작업에서 만들지 않는다.
PFW에 센터컷 대량 테이블을 고정 추가하지 않는다.

---

## 9. 테스트 기준

필수 테스트를 추가 또는 보강한다.

```text
BAT 테스트:
- listener bean 로딩
- heartbeat interval 설정 로딩
- heartbeat 갱신 service 호출
- 장시간 smoke Job 실행 중 heartbeat 2회 이상 갱신
- 정상 Job 완료 시 COMPLETED 기록
- 실패 Job 실패 시 FAILED 기록
- StepExecutionId와 CPF step 메타 연결 유지

Ghost 테스트:
- RUNNING + heartbeat 정상 범위는 ghost 아님
- RUNNING + heartbeat timeout 초과는 ghost candidate
- ghost event 중복 생성 방지
- ghost 조치 사유 누락 차단
- ghost 조치 감사 로그 저장

ADM 테스트:
- worker 상태 조회
- execution 진행률 조회
- step 처리 건수 조회
- ghost candidate 조회
```

가능하면 실제 MariaDB 또는 프로젝트 기존 테스트 DB 기준으로 검증한다.
mock만 수행한 경우에는 mock 기반 테스트라고 리포트에 명확히 기록한다.

---

## 10. Runtime smoke 기준

`scripts/smoke-bat-runtime.ps1`를 보강하거나 별도 옵션을 추가해 heartbeat/ghost를 검증한다.

필수 smoke 항목:

```text
- BAT jar 기동
- health 확인
- 장시간 smoke Job 실행
- 실행 중 heartbeat 2회 이상 조회
- 실행 중 progress 또는 처리 건수 조회
- Job 완료 후 COMPLETED 확인
- 실패 Job 실행 후 FAILED 확인
- ghost timeout 시나리오 가능하면 검증
- cleanup 확인
```

ghost timeout 시나리오를 실제 프로세스 kill로 검증하기 어렵다면 아래 중 하나로 대체한다.

```text
허용 대체:
- 테스트용 stale heartbeat fixture 생성
- RUNNING execution의 lastHeartbeatAt을 timeout 이전 시각으로 조정
- ghost detection service를 실행해 candidate 생성 확인
```

대체 검증을 사용한 경우 실제 kill 기반 검증은 미검증으로 기록한다.

---

## 11. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell
.\gradlew.bat :bat:test --offline
.\gradlew.bat :bat:bootJar --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1

.\gradlew.bat :adm:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

가능하면 ADM runtime smoke도 재실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
```

전체 MariaDB 설치 SQL 재실행은 이번 요청 필수 범위가 아니다.
실행하지 않았으면 성공으로 기록하지 말고 범위 제외 또는 미검증으로 기록한다.

---

## 12. 문서 반영 기준

이번 작업에서 문서는 기능 변경과 함께 최소 범위로 현행화한다.

필수 반영:

```text
README.md:
- BAT heartbeat/ghost 자동 감지 요약
- smoke-bat-runtime 검증 명령 갱신

specs/배치_개발_가이드.html:
- listener heartbeat 기준
- 처리율/진행률 갱신 기준
- ghost 자동 감지 기준
- 장시간 Job 작성 시 주의사항
- JobRepository와 CPF 운영 메타 연결 기준

specs/운영_매뉴얼.html:
- worker heartbeat 모니터링
- ghost candidate 자동 감지
- ghost 조치 절차
- lock 해제 주의사항
- 장애 후 재기동 절차

specs/관리자_가이드.html:
- ADM에서 worker/ghost/progress 확인하는 방법

specs/SQL_가이드.html:
- SQL 변경이 있으면 반영
- SQL 변경이 없으면 기존 테이블 활용으로 기록

specs/기능_구현_매트릭스.html:
- BAT heartbeat/ghost 자동 감지 상태 반영

CPF_STABILIZATION_REPORT.html:
- 실제 작업 결과와 검증 결과 기록
```

문서만 수정하고 코드/테스트가 없으면 완료가 아니다.

---

## 13. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text
[BAT 실행 중 Heartbeat / 처리율 갱신 / Ghost 자동 감지]

현재 상태 판정:
- 기존 heartbeat 방식:
- 기존 ghost 조회/조치 방식:
- 기존 worker 테이블 상태:
- 기존 execution/step 메타 상태:
- 기존 smoke-bat-runtime 상태:
- 미구현/미검증 항목:

개발 기능:
- listener:
- heartbeat service:
- progress/metring 갱신:
- ghost detection service:
- scheduler 위치:
- ADM API/DTO/Service/Mapper 변경:
- SQL 변경 여부:
- smoke script 변경:
- 문서 변경:

테스트:
- :bat:test:
- :bat:bootJar:
- smoke-bat-runtime:
- :adm:test:
- 전체 test:
- qualityGate:
- check scripts:
- ADM runtime smoke:

검증 결과:
- heartbeat 2회 이상 갱신:
- 진행률/처리율 갱신:
- 정상 Job 완료:
- 실패 Job 실패 처리:
- ghost candidate 자동 생성:
- ghost 조치 사유 검증:
- 감사 로그 저장:

미검증:
- 미검증 항목:
- 미검증 사유:
- 다음 조치:

아키텍처 영향:
- PFW 책임:
- BAT 책임:
- ADM 책임:
- 센터컷 확장성 영향:

최종 판정:
- 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
```

실행하지 않은 검증은 성공으로 기록하지 않는다.
mock 검증은 mock 검증이라고 적는다.
실제 runtime smoke는 실제 runtime smoke라고 구분한다.

---

## 14. 완료 기준

아래가 모두 충족되어야 완료로 기록한다.

```text
- BAT 실행 중 heartbeat가 주기적으로 갱신된다.
- 장시간 smoke Job에서 heartbeat가 2회 이상 갱신된다.
- JobExecutionId / StepExecutionId / CPF executionId / transactionGlobalId 연결이 유지된다.
- 처리 건수 또는 진행률이 실행 중 갱신된다.
- 정상 Job 완료 시 COMPLETED 상태가 기록된다.
- 실패 Job 실패 시 FAILED 상태가 기록된다.
- heartbeat timeout 기준 ghost candidate가 자동 감지된다.
- ghost event 또는 candidate 이력이 저장된다.
- ADM에서 ghost candidate를 조회할 수 있다.
- ghost 조치 사유 누락이 차단된다.
- ghost 조치 감사 로그가 저장된다.
- :bat:test, :bat:bootJar, smoke-bat-runtime이 통과한다.
- README/specs/기능 매트릭스/리포트가 실제 상태와 일치한다.
- 실행하지 않은 검증을 성공으로 기록하지 않는다.
```

---

## 15. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
- heartbeat 조회 API만 있고 실행 중 주기 갱신 없음
- 즉시 종료 Job만 검증하고 장시간 Job heartbeat 검증 없음
- heartbeat 2회 이상 갱신 증거 없음
- 처리율/진행률 갱신 없음
- ghost 후보 수동 조회만 있고 자동 감지 없음
- ghost event 또는 candidate 이력 없음
- ghost 조치 사유 누락 차단 없음
- ADM에서 자동 감지된 ghost candidate 조회 불가
- JobExecutionId / StepExecutionId / CPF executionId 연결 깨짐
- BAT가 PFW 공통 API를 우회해 자체 실행 구현
- ADM이 Job/Step 실행 로직을 직접 소유
- PFW가 BAT runtime 책임을 소유
- 센터컷 대량 모수/item/result 테이블을 PFW에 고정 추가
- SQL 변경이 있는데 Flyway/all_install/SQL 가이드 반영 없음
- 테스트 없이 완료 기록
- 실행하지 않은 검증을 성공으로 기록
- CPF_STABILIZATION_REPORT.html과 실제 소스 상태 불일치
```

---

## 16. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 아래 순서로 기록한다.

```text
1. ADM 브라우저 실제 클릭 자동화
2. 온디맨드 배치
3. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
4. BAT EDU / XYZ EDU 배치 샘플 보강
5. 온라인/배치 로그 정책 + 거래 메타 자동 등록
6. Redis/Kafka/MQ 실 broker 장애 시나리오 검증
7. 트랜잭션 가이드 정본화
```

센터컷 후속 작업 기준은 아래 방향을 유지한다.

```text
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않음
- BAT는 기본 센터컷 모수/item/result 테이블과 기본 구현체 제공
- 업무 주제영역은 커스텀 모수/item/result 테이블을 추가할 수 있음
- 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동
- ADM은 기본 테이블/커스텀 테이블 여부와 관계없이 공통 관제 제공
```
