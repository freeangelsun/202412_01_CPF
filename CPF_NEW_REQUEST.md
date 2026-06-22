# CPF_REQUEST_003: BAT 독립 실행 모듈 신설 / PFW-ADM-BAT 책임 경계 고정 / 최소 Spring Batch E2E 검증

## 0. 이번 작업 범위 고정

이번 작업은 BAT 독립 실행 모듈을 신설하고, 최소 Spring Batch E2E가 독립 BAT 런타임에서 동작하는지 검증하는 작업이다.

이번 작업에서 수행할 범위는 아래로 고정한다.

```text
선택 범위:
- bat 모듈 신규 생성
- settings.gradle include 'bat' 반영
- bat/build.gradle 구성
- 독립 실행 가능한 Spring Boot BAT application 생성
- executable jar 생성
- java -jar standalone 구동 가능
- BAT health/actuator 또는 동등한 health endpoint 제공
- PFW Batch 공통 API 사용
- Spring Batch 표준 JobRepository 사용
- 최소 Job/Step 1개 실행 E2E 검증
- Spring Batch JobExecutionId / StepExecutionId 저장 확인
- CPF pfw_batch_execution / pfw_batch_step_execution 운영 메타와 연결
- transactionGlobalId 연결 확인
- ADM은 BAT 실행 상태를 관제하는 책임만 유지
- README / specs / 기능 구현 매트릭스 / CPF_STABILIZATION_REPORT.html 반영
```

이번 작업에서 아래 항목은 구현하지 않는다.
필요하면 `CPF_STABILIZATION_REPORT.html`의 다음 보강 후보로만 기록한다.

```text
이번 실행 제외:
- 온디맨드 배치 전체 구현
- 센터컷 기본 구현체 전체 구현
- 센터컷 모수/item/result 테이블 설계 전체 구현
- 업무별 커스텀 센터컷 테이블 adapter 구현
- Batch listener heartbeat / 처리율 갱신 고도화
- ghost 자동 감지 scheduler / 알림 연동
- ADM 브라우저 실제 클릭 자동화
- 온라인/배치 로그 정책 + 거래 메타 자동 등록
- BAT EDU / XYZ EDU 대규모 보강
- 배치 개발 가이드 / 트랜잭션 가이드 대규모 정본화
```

요청 파일은 작업 대상으로 수정하지 않는다.
Git commit, push, branch 생성 지시는 하지 않는다.
별도 수정파일 목록 산출물은 만들지 않는다.
작업 결과는 `CPF_STABILIZATION_REPORT.html`에만 기록한다.

---

## 1. 상위 아키텍처 전제

이번 작업의 핵심은 BAT를 PFW/ADM 내부 기능이 아니라 독립 실행 가능한 배치 실행 구현체로 분리하는 것이다.

아래 전제를 반드시 지킨다.

```text
PFW:
- 배치 공통 API 제공
- 표준 배치 메타 모델 제공
- transactionGlobalId 기준 제공
- 표준 상태 코드 / 오류 코드 / 로그 / 감사 기준 제공
- Spring Batch 공통 launcher/operation API 제공
- 실제 장시간 Job 실행 책임을 소유하지 않음

BAT:
- 독립 실행 가능한 Spring Boot batch worker/application
- PFW Batch 공통 API 사용
- Spring Batch Job/Step 실행 구현
- JobRepository 표준 사용
- worker heartbeat / lock / retry / recovery의 향후 실행 책임을 갖는 런타임
- executable jar 형태로 standalone 구동 가능
- 필요 시 WAS/컨테이너 환경에도 배포 가능

ADM:
- 배치 실행 로직을 소유하지 않음
- Job/Execution/Step/Worker/Lock/Ghost/Log 상태를 관제
- 수동 실행/중지/재실행 요청을 표준 API를 통해 처리
- BAT 상태를 조회하거나 관제할 수 있는 구조 유지

업무 모듈:
- BAT가 직접 업무 DB를 임의 수정하지 않음
- 업무 처리는 각 주제영역 Handler/Service/Facade를 호출하는 구조를 유지
```

이번 작업 중 PFW/ADM에 BAT 실행 책임을 추가하거나 고착화하지 않는다.

센터컷 관련 전제도 훼손하지 않는다.

```text
센터컷 전제:
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않는다.
- PFW는 표준 인터페이스, 상태, 오류, 로그, 감사, transactionGlobalId 기준만 제공한다.
- BAT는 향후 기본 센터컷 모수/item/result 테이블과 기본 구현체를 제공한다.
- 개발자는 필요 시 업무별 커스텀 모수/item/result 테이블을 추가할 수 있어야 한다.
- 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동할 수 있어야 한다.
```

이번 작업에서 센터컷을 구현하지는 않지만, 향후 이 구조를 방해하는 SQL/문서/소스 변경은 하지 않는다.

---

## 2. 먼저 현재 상태 판정

작업 시작 전 실제 GitHub 소스/SQL/문서 기준으로 현재 상태를 먼저 판정한다.

아래 항목을 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```text
현재 상태 판정 항목:
- settings.gradle에 bat 모듈 존재 여부
- 기존 배치 실행 구현체 위치
- 기존 PFW Batch 공통 API 존재 여부
- 기존 CpfBatchLauncher 또는 동등한 launcher 존재 여부
- 기존 CpfBatchExecutionRequest / Result 또는 동등한 DTO 존재 여부
- 기존 Spring Batch JobRepository 설정
- 기존 ADM 배치 관제 API와 BAT 연계 가능성
- 기존 pfw_batch_execution / pfw_batch_step_execution 운영 메타 구조
- 기존 runtime smoke / DB smoke 검증 결과
- BAT 미구현 상태
- BAT 분리 시 영향받는 문서
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

Codex 완료 메시지나 과거 리포트만 믿지 말고, 실제 소스/SQL/문서 기준으로 판정한다.

---

## 3. BAT 모듈 생성 기준

`bat` 모듈은 이번 작업에서 반드시 신규 생성한다.

필수 파일/구조:

```text
settings.gradle
- include 'bat' 추가

bat/build.gradle
- Java 17/21 호환
- Spring Boot 3.4.x 기준
- Spring Batch 의존성
- PFW 모듈 의존성
- 필요한 경우 ADM API client 의존성은 직접 참조하지 말고 책임 경계 검토 후 최소화

bat/src/main/java/cpf/bat
- BatApplication.java 또는 BatchWorkerApplication.java

bat/src/main/java/cpf/bat/job
- 최소 검증용 Job 구성

bat/src/main/java/cpf/bat/step
- 최소 검증용 Step 구성

bat/src/main/java/cpf/bat/operation
- BAT 실행/상태/health 관련 operation

bat/src/main/resources
- application-bat-local.yml
- application-bat-worker.yml 또는 동등한 profile 설정
```

패키지명은 기존 CPF 컨벤션에 맞춰 조정할 수 있다.
다만 BAT가 독립 모듈임은 명확해야 한다.

---

## 4. BAT 독립 구동 기준

BAT는 단순 library가 아니라 독립 실행 가능한 런타임이어야 한다.

필수 구동 기준:

```text
- ./gradlew.bat :bat:bootJar --offline 성공
- bat/build/libs/*.jar 생성
- java -jar bat/build/libs/<bat-jar>.jar --spring.profiles.active=bat-local 구동 가능
- health 또는 actuator endpoint 확인 가능
- 종료/cleanup 가능
```

가능하면 BAT runtime smoke 스크립트를 추가한다.

```text
권장 스크립트:
- scripts/smoke-bat-runtime.ps1
```

`scripts/smoke-bat-runtime.ps1`를 추가하는 경우 아래를 포함한다.

```text
- BAT bootJar 존재 여부 확인
- BAT application 기동
- PID 추적
- health check
- 최소 Job 실행 또는 Job 실행 가능 상태 확인
- 종료/cleanup
- 실패 시 로그 위치와 실패 사유 기록
```

스크립트를 추가하지 못하면 사유를 리포트에 기록한다.

---

## 5. Spring Batch E2E 기준

BAT에서 최소 Job/Step 1개를 실제 실행해야 한다.

필수 검증:

```text
- BAT에서 JobLauncher를 통한 Job 실행
- Spring Batch JobRepository에 JobExecution 저장
- Spring Batch StepExecution 저장
- CPF pfw_batch_execution에 JobExecutionId 연결
- CPF pfw_batch_step_execution 또는 기존 동등 테이블에 StepExecutionId 연결
- transactionGlobalId 생성/연결
- Job 성공 상태 저장
- Step 성공 상태 저장
- 실패 Job 또는 실패 Step의 최소 오류 흐름 확인
```

Spring Batch 표준 JobRepository를 우회하지 않는다.
BAT에서 독립 JobRepository를 임의 생성하지 않는다.
기존 PFW 공통 launcher가 있으면 그것을 사용한다.
기존 PFW 공통 launcher가 부족하면 BAT 전용으로 우회하지 말고 PFW 공통 API를 최소 보강한다.

---

## 6. PFW / ADM 책임 경계 정리

이번 작업에서 책임 경계를 문서와 리포트에 명확히 기록한다.

```text
PFW 책임:
- 공통 launcher
- 공통 execution request/result
- 공통 status/error
- transactionGlobalId
- batch metadata 표준
- lock/worker/ghost 관련 표준 API 기반

BAT 책임:
- Job/Step 실행
- 독립 runtime
- 향후 worker heartbeat/listener/retry/recovery 실행 주체
- 향후 온디맨드/센터컷 실행 주체

ADM 책임:
- 관제
- 수동 실행/중지/재실행 요청
- 실행 이력 조회
- Step 이력 조회
- worker/lock/ghost/log 조회와 조치
```

ADM이 Job/Step 실행 구현체를 직접 소유하도록 만들지 않는다.
PFW가 BAT runtime 책임을 갖도록 만들지 않는다.

---

## 7. SQL / Flyway / all_install 기준

이번 작업에서 신규 테이블 생성은 최소화한다.

우선 기존 테이블을 사용한다.

```text
기존 사용 우선:
- BATCH_JOB_INSTANCE
- BATCH_JOB_EXECUTION
- BATCH_STEP_EXECUTION
- pfw_batch_job
- pfw_batch_execution
- pfw_batch_step_execution
- pfw_batch_worker
- pfw_batch_lock
- pfw_batch_ghost_event
```

BAT 독립 실행에 꼭 필요한 SQL이 추가되는 경우에만 아래 기준을 따른다.

```text
- Flyway migration 추가
- all_install SQL 반영
- smoke SQL 반영
- table comment / column comment
- 공통 감사 컬럼
- FK/index
- README / SQL 가이드 / 기능 매트릭스 / 리포트 반영
```

이번 작업에서 센터컷 대량 모수/item/result 테이블은 만들지 않는다.
특히 PFW에 센터컷 대량 item/result 테이블을 고정으로 추가하지 않는다.

---

## 8. API / Health / Actuator 기준

BAT는 독립 구동 상태를 확인할 수 있어야 한다.

필수 기준:

```text
- BAT health 확인 가능
- datasource 또는 batch runtime 상태 확인 가능
- profile 정보 또는 worker instance 정보 확인 가능
- serverInstanceId 또는 workerId 생성/노출 기준 정리
```

HTTP actuator를 사용할 수 있으면 actuator health를 사용한다.
HTTP endpoint를 열지 않는 구조라면 대체 health 확인 방식을 스크립트와 리포트에 명확히 기록한다.

운영 민감정보나 DB 비밀번호를 health 응답에 노출하지 않는다.

---

## 9. 테스트 기준

필수 테스트를 추가한다.

```text
BAT 단위/통합 테스트:
- BAT application context load
- 최소 Job bean 생성
- 최소 Step bean 생성
- Job 실행 성공
- JobExecutionId 생성 확인
- StepExecutionId 생성 확인
- PFW 공통 launcher 사용 확인
- transactionGlobalId 연결 확인
- 실패 케이스 최소 1개
```

가능하면 아래를 포함한다.

```text
- :bat:test
- :bat:bootJar
- smoke-bat-runtime.ps1
```

기존 전체 검증도 수행한다.

```text
- .\gradlew.bat test --offline
- .\gradlew.bat qualityGate --offline
- scripts/check-utf8.ps1 -CheckMojibake
- scripts/check-html-docs.ps1
```

---

## 10. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell
.\gradlew.bat :bat:test --offline
.\gradlew.bat :bat:bootJar --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

BAT runtime smoke 스크립트를 추가한 경우 아래도 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-bat-runtime.ps1
```

가능하면 기존 ADM runtime smoke도 재실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
```

실행하지 못한 검증은 성공으로 기록하지 않는다.
실패한 명령은 실패 사유와 로그 위치를 리포트에 기록한다.

---

## 11. 문서 반영 기준

이번 작업에서 대규모 배치 개발 가이드 정본화는 하지 않는다.
다만 BAT 독립 모듈 신설에 필요한 최소 문서는 반드시 반영한다.

필수 반영:

```text
README.md:
- BAT 모듈 존재와 구동 방법 요약
- :bat:test / :bat:bootJar / java -jar 또는 smoke-bat-runtime 실행 명령

specs/프레임워크_구성_가이드.html:
- PFW / BAT / ADM 책임 경계 반영
- BAT 독립 실행 모듈 기준 반영

specs/배치_개발_가이드.html:
- BAT 모듈 기본 구조
- 최소 Job/Step 예제 위치
- JobRepository와 CPF 운영 메타 연결 기준

specs/기능_구현_매트릭스.html:
- BAT 독립 실행 모듈 행 추가 또는 갱신
- 상태값은 실제 검증 기준으로 기록

specs/SQL_가이드.html:
- 신규 SQL 변경이 없으면 변경 없음으로 기록
- 신규 SQL 변경이 있으면 Flyway/all_install 기준 기록

CPF_STABILIZATION_REPORT.html:
- 실제 작업 결과 기록
```

문서만 만들고 코드/테스트가 없으면 완료가 아니다.

---

## 12. CPF_STABILIZATION_REPORT.html 기록 기준

리포트에는 아래 형식으로 기록한다.

```text
[BAT 독립 실행 모듈 신설 / PFW-ADM-BAT 책임 경계 고정 / 최소 Spring Batch E2E]

현재 상태 판정:
- 기존 bat 모듈 존재 여부:
- 기존 배치 실행 구현 위치:
- 기존 PFW Batch 공통 API:
- 기존 ADM 관제 API:
- 기존 SQL/JobRepository 상태:
- BAT 미구현 항목:

개발 기능:
- settings.gradle:
- bat/build.gradle:
- BAT application:
- Job/Step:
- PFW 공통 API 사용:
- health/actuator:
- runtime smoke:
- SQL 변경 여부:
- 문서 반영:

테스트:
- :bat:test:
- :bat:bootJar:
- 최소 Job 실행:
- JobExecutionId 저장:
- StepExecutionId 저장:
- CPF 운영 메타 연결:
- transactionGlobalId 연결:
- 전체 test:
- qualityGate:
- smoke-bat-runtime:
- smoke-adm-runtime:

미검증:
- 미검증 항목:
- 미검증 사유:
- 다음 조치:

아키텍처 영향:
- PFW 책임 영향:
- ADM 책임 영향:
- BAT 책임 영향:
- 센터컷 확장성 영향:

최종 판정:
- 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
```

실행하지 않은 검증은 성공으로 기록하지 않는다.
BAT 모듈만 생성하고 독립 실행/테스트가 없으면 완료로 기록하지 않는다.

---

## 13. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 기록한다.

```text
- settings.gradle에 bat 모듈 포함
- bat/build.gradle 존재
- BAT Spring Boot application 존재
- :bat:test 성공
- :bat:bootJar 성공
- executable jar 생성
- java -jar standalone 구동 가능 또는 smoke-bat-runtime 성공
- BAT health 확인 가능
- 최소 Spring Batch Job 실행 성공
- Spring Batch JobExecutionId 저장 확인
- Spring Batch StepExecutionId 저장 확인
- CPF pfw_batch_execution 운영 메타 연결
- CPF pfw_batch_step_execution 또는 동등 메타 연결
- transactionGlobalId 연결 확인
- PFW 공통 Batch API 사용
- PFW/ADM에 실행 책임을 추가하지 않음
- README/specs/기능 매트릭스/리포트 반영
- 실패/미검증 항목을 성공으로 기록하지 않음
```

---

## 14. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
- bat 모듈이 없음
- settings.gradle에 bat include 없음
- bat/build.gradle 없음
- BAT가 단순 library 수준이고 독립 구동 불가
- :bat:test 미실행 또는 실패
- :bat:bootJar 미실행 또는 실패
- executable jar 없음
- Spring Batch JobRepository 실행 없이 완료 처리
- JobExecutionId / StepExecutionId 확인 없음
- CPF 운영 메타와 연결 없음
- transactionGlobalId 연결 없음
- BAT가 PFW 공통 API를 사용하지 않고 자체 우회 구현
- BAT가 독립 JobRepository를 임의 생성
- ADM이 Job/Step 실행 구현체를 직접 소유
- PFW가 BAT runtime 책임까지 소유
- 센터컷 대량 모수/item/result 테이블을 PFW에 고정 추가
- 문서만 있고 코드/테스트 없음
- 실행하지 않은 검증을 성공으로 기록
- CPF_STABILIZATION_REPORT.html과 실제 소스 상태 불일치
```

---

## 15. 다음 보강 후보로만 기록할 항목

이번 작업 이후 다음 보강 후보는 아래 순서로 기록한다.

```text
1. BAT listener heartbeat / 처리율 갱신 / ghost 자동 감지
2. ADM 브라우저 실제 클릭 자동화
3. 온디맨드 배치
4. 센터컷 기본 구현체 + 업무별 커스텀 모수/item/result 테이블 연동
5. 온라인/배치 로그 정책 + 거래 메타 자동 등록
6. BAT EDU / XYZ EDU 배치 샘플 보강
7. 배치 개발 가이드 / 트랜잭션 가이드 정본화
```

센터컷 후속 작업 기준은 아래 방향을 유지한다.

```text
- PFW는 센터컷 대량 모수/item/result 테이블을 직접 소유하지 않음
- BAT는 기본 센터컷 모수/item/result 테이블과 기본 구현체 제공
- 업무 주제영역은 커스텀 모수/item/result 테이블을 추가할 수 있음
- 업무별 테이블은 CenterCutTargetProvider / CenterCutHandler / ResultProvider adapter로 연동
- ADM은 기본 테이블/커스텀 테이블 여부와 관계없이 공통 관제 제공
```
