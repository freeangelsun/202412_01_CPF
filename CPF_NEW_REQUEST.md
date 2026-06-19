# CPF 기본 구현체 통합 개발 요청서

## 0. 작업 목적

CPF(CoreFlow Platform Framework)를 단순 샘플 프로젝트가 아니라 운영 가능한 범용 업무 프레임워크 수준으로 보강한다.

이번 작업의 목적은 리포트 작성이나 미구현 목록 정리가 아니다.
기존 소스와 README 표준을 확인한 뒤, 부족하거나 품질이 낮은 구현은 실제로 다시 개발하여 완료 수준으로 끌어올리는 것이 목적이다.

문서는 작업 주체가 아니다.
문서는 실제 소스, SQL, UI, Swagger, EDU, 검증 결과에 맞춰 현행화하는 후속 산출물이다.

이번 작업의 핵심 목표는 다음이다.

```text
1. README와 기존 소스 구조를 먼저 확인한다.
2. 기존 CPF 패키지/모듈/테이블/계층 표준을 파악한다.
3. 각 요청사항별 현재 구현 상태를 먼저 재검토한다.
4. 구현이 없으면 새로 개발한다.
5. 구현이 있어도 sample, in-memory, hardcoding, 장문 한 줄, SQL 미연결, UI 미연결 등 품질이 낮으면 다시 만든다.
6. BIZADM/EXS의 sample 중심 구조를 운영 가능한 기본 구현체로 전환한다.
7. MBR/ADM은 이미 구현된 부분을 반복하지 말고 부족하거나 품질 낮은 부분을 보강/재작성한다.
8. PFW에 Spring Batch 공통 이용 API/공통 컴포넌트를 제공한다.
9. BAT 또는 배치 주제영역은 PFW 배치 공통 API를 이용해 Spring Batch 실행 구현체를 만든다.
10. ADM은 배치 운영 관제 REST/API/UI를 제공한다.
11. EDU는 PFW 배치 공통 API를 사용하는 개발자 참조 샘플을 제공한다.
12. PFW/CMN 공통 표준을 ADM/BIZADM/MBR/EXS/BAT/EDU에 적용한다.
13. 구현하지 않은 기능을 완료로 기록하지 않는다.
14. 모든 요청사항별 구현 파일, SQL, UI, Swagger, EDU, 검증 결과, 미검증 사유를 CPF_STABILIZATION_REPORT.html에 기록한다.
```

Git commit, push, branch 생성은 하지 않는다.

`CPF_REQUEST.md`는 요청 확인용으로만 읽고 절대 수정하지 않는다.

---

## 0-A. 개발 완료 우선 원칙

이번 작업은 “못 한 항목을 정리하는 작업”이 아니다.
가능한 범위에서 실제 개발 완료를 목표로 한다.

Codex는 각 요청사항을 아래 순서로 처리한다.

```text
1. 현재 구현이 있는지 확인한다.
2. 현재 구현이 CPF 표준에 맞는지 평가한다.
3. 현재 구현이 충분하면 재사용하고 증적을 남긴다.
4. 현재 구현이 없으면 새로 구현한다.
5. 현재 구현이 있어도 품질이 낮으면 리팩터링 또는 재작성한다.
6. API/Service/Repository 또는 Mapper/SQL/UI/Swagger/EDU까지 연결한다.
7. 가능한 검증 명령을 실행한다.
8. 검증 결과를 CPF_STABILIZATION_REPORT.html에 기록한다.
9. 완료 기준을 만족한 항목만 완료로 기록한다.
```

아래 방식은 금지한다.

```text
- 구현하지 않고 리포트에만 미구현으로 기록
- 검증하지 않고 미검증으로만 기록
- 문서만 수정하고 구현 완료로 기록
- sample/in-memory/hardcoding을 남겨둔 채 일부 구현으로 회피
- Controller만 만들고 Service/Repository/SQL/UI 없이 완료 보고
- API만 만들고 ADM UI/API wrapper 없이 완료 보고
- SQL만 만들고 Java에서 사용하지 않음
- 패키지/테이블/클래스 설계가 어렵다는 이유로 구현을 중단
- 실행 환경이 없다는 이유로 정적 검증까지 생략
```

`미구현`, `미검증`, `재확인 필요`는 면책 상태가 아니다.
해당 상태를 사용하려면 아래를 반드시 기록한다.

```text
- 왜 구현하지 못했는지
- 어떤 파일/기능/설계가 막혔는지
- 어떤 대안까지 검토했는지
- 현재 소스 기준 어디까지 구현했는지
- 다음 작업에서 무엇을 해야 하는지
- 완료로 인정받기 위해 어떤 검증이 필요한지
```

---

## 0-B. 저품질 구현 재작성 원칙

현재 구현이 존재하더라도 아래에 해당하면 완료로 인정하지 않는다.
반드시 다시 만들거나 표준에 맞게 리팩터링한다.

```text
- sample 패키지에 운영 기본 기능이 있음
- sample 이름만 바꾸고 내부 구조가 그대로임
- List.of(Map.of), ConcurrentHashMap, CopyOnWriteArrayList 등으로 운영 데이터를 처리함
- Java/JS/XML/HTML/SQL 파일이 raw 기준 장문 한 줄임
- package/import/class/method가 한 줄에 붙어 있음
- Controller에서 비즈니스 로직이나 SQL을 직접 처리함
- Service/Repository/Mapper/DTO 계층이 불명확함
- SQL/Flyway 또는 설치 SQL이 없음
- Java에서 사용하는 테이블이 SQL에 없음
- 권한/감사/마스킹/다운로드 사유가 빠짐
- transactionGlobalId 추적이 안 됨
- Swagger/EDU/UI가 실제 구현과 맞지 않음
- 리포트가 실제 소스와 다름
```

위 항목이 발견되면 `일부 구현`으로 끝내지 말고, 가능한 범위에서 완료 수준으로 재작성한다.
정말로 재작성하지 못한 경우에만 차단 사유와 다음 조치를 기록한다.

---

## 0-C. 완료 기준

각 기능은 아래 연결을 만족해야 완료로 인정한다.

```text
- 실제 Java 소스 존재
- API 존재
- Service 존재
- Repository 또는 Mapper 존재
- DTO 존재
- SQL/Flyway 또는 설치 SQL 반영
- 권한 검사 존재
- 감사 로그 존재
- transactionGlobalId 추적 가능
- UI/API wrapper 존재
- Swagger annotation 존재
- EDU 참조 또는 샘플 반영
- 검증 명령 실행 결과 존재
- CPF_STABILIZATION_REPORT.html에 증적 기록
```

기능 성격상 일부 항목이 불필요하면, 왜 불필요한지 리포트에 명확히 기록한다.

---

## 1. 최우선 표준 확인

### 요청사항

작업 시작 전에 README, settings.gradle, 기존 소스 구조, SQL 구조, 품질 스크립트를 확인한다.
확인 결과를 바탕으로 구현해야 하며, 요청서의 예시 단어를 그대로 패키지명/클래스명/테이블명으로 만들지 않는다.

### 금지사항

```text
- 요청서의 단어를 그대로 패키지명으로 만드는 것
- 기존 CPF 패키지 규칙을 확인하지 않고 새 패키지를 만드는 것
- 업무 도메인 계층 없이 기능명만 바로 패키지로 만드는 것
- sample 패키지 이름만 바꾸고 내부 구조는 그대로 두는 것
- 기존 소스 표준과 충돌하는 테이블명/클래스명을 임의 생성하는 것
- 이미 구현된 기능을 확인하지 않고 중복 구현하는 것
- BAT를 만들면서 PFW 배치 공통 API를 사용하지 않는 것
- BAT가 Spring Batch JobRepository를 독립적으로 임의 생성하는 것
- EDU 배치 샘플이 PFW 공통 API를 사용하지 않고 직접 JobLauncher/JobRepository를 제각각 다루는 것
```

### 필수 확인 대상

```text
README.md
settings.gradle
build.gradle
pfw 패키지 구조
cmn 패키지 구조
adm 패키지 구조
mbr 패키지 구조
bizadm 패키지 구조
exs 패키지 구조
xyz/edu 구조
specs/sql 구조
scripts 품질 검사 구조
기존 Controller/Service/Mapper/Repository 작성 방식
기존 static UI 작성 방식
기존 Swagger annotation 작성 방식
기존 Spring Batch 설정/JobRepository/배치 메타 구조
```

### 검증 명령

```powershell
Get-Content README.md
Get-Content settings.gradle

Get-ChildItem -Recurse pfw/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse cmn/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse adm/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse mbr/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse bizadm/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse exs/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse xyz/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse specs/sql -Include *.sql | Select-Object FullName
Get-ChildItem -Recurse adm/src/main/resources/static -Include *.js,*.html,*.vue | Select-Object FullName

Select-String -Path **/*.java,**/*.sql,**/*.yml,**/*.properties -Pattern 'Spring Batch','JobRepository','JobLauncher','BATCH_','pfw_batch','batch','scheduler'
```

### 예상 정상 결과

`CPF_STABILIZATION_REPORT.html`에 아래 항목을 기록한다.

```text
항목
확인 파일/경로
확인한 표준
현재 구현 상태
현재 품질 문제
이번 작업 적용 방향
재확인 필요 여부
```

---

## 2. PFW 배치 프레임워크 공통 API 구현

### 요청사항

PFW는 CPF 배치 프레임워크의 기반이므로, BAT/ADM/EDU/업무 모듈이 공통으로 사용할 수 있는 배치 공통 API와 컴포넌트를 제공해야 한다.

여기서 PFW 배치 공통 API는 외부 REST API가 아니라 프레임워크 내부 Java API/Interface/Service/AutoConfiguration/공통 컴포넌트를 의미한다.

### PFW가 제공해야 하는 기능

```text
- Spring Batch JobRepository 공통 설정
- Spring Batch JobLauncher 공통 사용 기준
- Batch TransactionManager/DataSource 연결 기준
- Job/Step 실행 표준 파라미터 모델
- 배치 실행 요청 모델
- 배치 실행 결과 모델
- 배치 상태 표준 코드
- 배치 오류 표준 코드
- 배치 transactionGlobalId 생성/전파
- moduleId/wasId/serverInstanceId 기록 표준
- 배치 lock 공통 API
- 중복 실행 방지 공통 API
- 재실행 가능 여부 판단 API
- 배치 실행 전/후 이벤트 API
- 실패/지연/미수행 알림 이벤트 API
- 스케줄 실행 대상 판단 API
- 수동 실행 공통 API
- 배치 실행 이력 저장 연계 API
- Step 실행 이력 저장 연계 API
- Spring Batch JobExecutionId/StepExecutionId와 CPF 운영 이력 연결 API
- Redis/Kafka/MQ 미설정 시 mock/fallback 이벤트 전파 API
```

### PFW 책임 경계

PFW에 넣으면 안 되는 것:

```text
- MBR 휴면 처리 같은 업무 로직
- EXS token refresh 실제 업무 처리
- 특정 업무 테이블 직접 처리
- ADM 화면 로직
- BIZADM/EXS 전용 Controller
```

PFW에 넣어야 하는 것:

```text
- 배치 공통 인터페이스
- 공통 설정
- 공통 이벤트
- 공통 상태/오류/응답
- 공통 실행 이력 연계 기준
- 공통 lock/중복 실행 방지 기준
- JobRepository/JobLauncher 사용 표준
```

### 저품질 재작성 기준

아래에 해당하면 다시 만든다.

```text
- PFW가 아니라 BAT/ADM/업무 모듈마다 JobRepository를 직접 구성함
- 업무 로직이 PFW에 들어감
- PFW 배치 API가 문서에만 있고 실제 Java API가 없음
- transactionGlobalId/JobExecutionId 연결 기준이 없음
- lock/중복 실행 방지가 없는 배치 공통 API
```

### 검증 방법

```powershell
Select-String -Path pfw/src/main/java/**/*.java -Pattern 'JobRepository','JobLauncher','JobExecution','StepExecution','Batch','JobParameter','Tasklet','lock','retry','rerun','restart','schedule','transactionGlobalId','moduleId','wasId','serverInstanceId'
Select-String -Path pfw/src/main/java/**/*.java -Pattern 'interface','AutoConfiguration','Configuration','Event','Notification','Fallback','Mock'
Select-String -Path specs/sql/**/*.sql -Pattern 'BATCH_','pfw_batch','batch','job','step'
```

### 예상 정상 결과

```text
- PFW에 배치 공통 Java API/Interface/Service/Configuration이 존재
- BAT/ADM/EDU가 사용할 수 있는 공통 컴포넌트가 존재
- PFW가 Spring Batch JobRepository/JobLauncher 사용 표준을 제공
- PFW가 특정 업무 로직을 직접 포함하지 않음
- PFW 배치 공통 API가 문서만 있고 실제 코드가 없으면 완료 아님
```

---

## 3. BAT 배치 주제영역 및 Spring Batch 공통 실행 구현체

### 요청사항

배치는 각 업무 모듈에 제각각 만들지 않는다.
가능하면 배치 주제영역을 두고, Spring Batch 실행 구조를 공통화한다.

모듈명, 패키지명, 디렉터리명은 요청서가 강제하지 않는다.
Codex는 README, settings.gradle, 기존 모듈 구조, Gradle 멀티모듈 규칙을 확인한 뒤 `bat` 주제영역 또는 기존 배치 구조 확장 중 어느 방식이 CPF 표준에 맞는지 결정한다.

### 책임 분리 기준

```text
PFW:
- Spring Batch 공통 API/공통 설정/공통 컴포넌트 제공
- JobRepository/DataSource/TransactionManager 연계 표준
- 배치 transactionGlobalId 표준
- 배치 공통 예외/응답/이벤트
- 배치 운영 메타 표준
- 배치 스케줄/락/관제 공통 인터페이스
- 특정 업무 Job 로직은 넣지 않음

BAT 또는 배치 주제영역:
- PFW 배치 공통 API를 이용한 Spring Batch 실행 구현체
- 공통 Job/Step/Tasklet/Reader/Processor/Writer 구현
- 배치 Job 등록/실행/재실행/중지 구현
- 업무 모듈 Service/Facade 호출
- 공통 배치 예시 Job 구현
- 배치 실행 결과를 PFW/ADM 관제 표준에 맞게 기록

ADM:
- 배치 Job 목록/상세
- 실행 이력/Step 이력/실패 메시지 조회
- 수동 실행/재실행/중지/비활성
- 실패/지연/미수행 알림 관제

업무 모듈:
- 배치에서 호출할 업무 Service/Facade 제공
- 업무 도메인별 처리 로직 제공
- Spring Batch 실행 프레임워크를 중복 구현하지 않음
```

### Spring Batch 테이블 사용 기준

```text
- Spring Batch 표준 메타데이터는 Spring Batch JobRepository가 관리한다.
- JobRepository는 CPF/PFW 배치 공통 설정을 통해 구성한다.
- BAT는 별도 독립 JobRepository를 임의 생성하지 않는다.
- BAT는 PFW 배치 공통 API를 통해 JobRepository/JobLauncher를 사용한다.
- BAT는 Spring Batch 표준 메타테이블과 CPF 운영 관제 메타를 함께 사용한다.
- Spring Batch 표준 테이블은 Job/Step 실행 메타데이터의 원천이다.
- CPF 운영 관제 메타는 ADM 조회/알림/스케줄/락/업무 표시용으로 사용한다.
- 두 저장 구조의 실행 ID/Job ID/transactionGlobalId 연결 기준을 명확히 구현한다.
- Spring Batch 표준 테이블을 CPF 운영 관제 테이블로 대체하지 않는다.
- CPF 운영 관제 테이블을 Spring Batch 표준 테이블로 대체하지 않는다.
```

### 구현해야 하는 기능

```text
- Spring Batch Job/Step 표준 구조
- PFW 배치 공통 API 사용
- CPF/PFW 공통 JobRepository 사용
- Job 등록 기준
- Step 등록 기준
- 배치 파라미터 표준
- 배치 실행 이력 저장
- Step 실행 이력 저장
- 실패 메시지 저장
- 중복 실행 방지
- lock 처리
- 수동 실행 API
- 스케줄 실행 또는 scheduler 연계
- 재실행 가능 여부 판단
- 실패/지연/미수행 알림 이벤트 발생
- ADM 배치 관리 화면 연계
- transactionGlobalId 기록
- moduleId/wasId/serverInstanceId 기록
- 업무 모듈 Service/Facade 호출 표준
```

### 배치 시나리오 예시

아래는 예시이며, 그대로 패키지명/클래스명으로 만들라는 뜻이 아니다.

```text
- 로그 보관/삭제 배치
- 오류 로그 후처리 배치
- 알림 재발송 배치
- EXS 재처리 배치
- EXS token refresh 배치
- MBR 휴면/잠금/상태 갱신 배치
- 다운로드 감사 로그 보관 배치
- 운영 통계 집계 배치
```

### 저품질 재작성 기준

아래에 해당하면 다시 만든다.

```text
- 배치 문서만 있고 실제 Job/Step 코드가 없음
- PFW 배치 공통 API 없이 BAT가 직접 JobRepository/JobLauncher를 다룸
- 업무 모듈마다 제각각 배치 구조를 만듦
- Spring Batch 표준 메타테이블과 CPF 운영 관제 메타 연결 기준이 없음
- 실행 이력/Step 이력/실패 메시지 저장이 없음
- ADM 배치 관제가 없음
```

### 검증 방법

```powershell
Select-String -Path pfw/src/main/java/**/*.java -Pattern 'JobRepository','JobLauncher','JobExecution','StepExecution','Batch','JobParameter','lock','schedule','transactionGlobalId'
Select-String -Path **/*.java -Pattern 'Job','Step','Tasklet','ItemReader','ItemProcessor','ItemWriter','JobLauncher','JobRepository','Spring Batch','Batch'
Select-String -Path **/*.java -Pattern 'transactionGlobalId','moduleId','wasId','serverInstanceId'
Select-String -Path **/*.java -Pattern 'lock','retry','rerun','restart','manual','schedule'
Select-String -Path specs/sql/**/*.sql -Pattern 'BATCH_','batch','job','step','pfw_batch'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'batch','job','step','rerun','manual','JobExecution','StepExecution'
```

### 예상 정상 결과

```text
- PFW에 배치 공통 API가 실제 코드로 존재
- BAT 또는 배치 주제영역은 PFW 배치 공통 API를 사용함
- BAT 또는 배치 주제영역은 PFW 공통 JobRepository 설정을 사용함
- BAT가 별도 독립 JobRepository를 임의 생성하지 않음
- Spring Batch Job/Step 구조가 실제 코드에 존재
- Spring Batch 표준 실행 메타데이터와 CPF 운영 관제 메타가 연결됨
- 업무 모듈별 배치는 공통 배치 구조를 통해 실행/관제 가능
- 배치 실행/Step 이력이 DB에 저장됨
- 중복 실행 방지 lock 기준 존재
- 실패/지연/미수행 알림 이벤트 발생 구조 존재
- ADM에서 배치 조회/상세/수동 실행/재실행 흐름 확인 가능
```

---

## 4. EDU 배치 참조 샘플 보강

### 요청사항

`xyz/edu`는 개발자가 CPF 표준을 따라 신규 업무 기능을 만들 수 있는 참조 구현이어야 한다.
배치 샘플도 PFW 배치 공통 API를 사용하는 방식으로 제공해야 한다.

### EDU에 필요한 배치 샘플

```text
- PFW 배치 공통 API를 이용한 Job 등록 예시
- PFW 배치 공통 API를 이용한 Step 등록 예시
- PFW 배치 공통 API를 이용한 수동 실행 예시
- PFW 배치 공통 API를 이용한 transactionGlobalId 전파 예시
- PFW 배치 공통 API를 이용한 실행 이력 저장 예시
- PFW 배치 공통 API를 이용한 실패 알림 이벤트 예시
- 업무 Service/Facade를 배치 Step에서 호출하는 예시
- Spring Batch JobExecutionId와 CPF 운영 이력을 연결하는 예시
- mock/fallback 환경에서 배치 이벤트를 검증하는 예시
```

### 금지사항

```text
- PFW 배치 공통 API를 우회해서 JobRepository/JobLauncher를 직접 제각각 구성하는 것
- 업무 모듈마다 다른 방식의 배치 실행 샘플을 만드는 것
- 문서 설명만 있고 실제 Java 샘플이 없는 것
- 단순 컨트롤러 문구만 있고 Job/Step 또는 Tasklet/Reader/Processor/Writer 예시가 없는 것
```

### 검증 방법

```powershell
Get-ChildItem -Recurse xyz/src/main/java/cpf/xyz/edu -Filter *.java
Select-String -Path xyz/src/main/java/cpf/xyz/edu/**/*.java -Pattern 'Batch','Job','Step','Tasklet','ItemReader','ItemProcessor','ItemWriter','JobExecution','StepExecution','transactionGlobalId','Notification','Fallback'
Select-String -Path xyz/src/main/java/cpf/xyz/edu/**/*.java -Pattern 'JobRepository','JobLauncher'
```

### 예상 정상 결과

```text
- EDU에 실제 Java 배치 샘플 존재
- EDU 배치 샘플이 PFW 배치 공통 API를 사용
- EDU 배치 샘플이 Job/Step 또는 Tasklet/Reader/Processor/Writer 구조를 보여줌
- EDU 배치 샘플이 transactionGlobalId와 JobExecutionId 연결을 보여줌
- EDU 배치 샘플이 실패/알림/fallback 흐름을 보여줌
- 단순 문서 설명만으로 완료 처리하지 않음
```

---

## 5. BIZADM 기본 구현체 실개발

현재 BIZADM은 sample 중심으로 보인다.
이 상태는 CPF 적용 프로젝트의 업무/프로젝트 관리자 기본 구현체로 인정하지 않는다.

### 요청사항

BIZADM sample API와 sample Service에 있는 업무 관리자 기능을 기존 CPF 표준에 맞는 정식 기본 구현체로 전환한다.

패키지명은 요청서가 강제하지 않는다.
Codex는 기존 패키지 규칙과 업무 도메인 계층을 확인한 뒤 설계한다.

### 구현해야 하는 기능

```text
- 프로젝트/업무 관리자 계정 관리
- 프로젝트/업무 관리자 로그인
- 로그아웃
- refresh token hash 저장
- 로그인 성공/실패 이력
- 역할 관리
- 메뉴 권한
- 버튼 권한
- API 권한
- 다운로드 권한
- 마스킹 권한
- 마스킹 해제 권한
- 고객 관리 참조 기능
- 상품 관리 참조 기능
- 주문 관리 참조 기능
- 설정 관리
- 다운로드 사유 필수
- 마스킹 해제 사유 필수
- 운영 감사
- 다운로드 감사
- 마스킹 해제 감사
```

### 구현 기준

```text
- API
- Service
- Repository 또는 Mapper
- DTO
- SQL/Flyway 또는 설치 SQL
- 권한 검사
- 감사 로그
- Swagger annotation
- ADM UI/API wrapper
- 한글 주석
```

### 저품질 재작성 기준

```text
- sample Controller/Service에 운영 API가 남아 있음
- List.of(Map.of)로 운영 데이터 반환
- 권한/감사/마스킹/다운로드 사유 없음
- SQL/Flyway 없음
- UI/API wrapper 없음
- Swagger 없음
```

### 검증 방법

```powershell
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'package .*sample','SampleController','SampleService','List.of\(Map.of','ConcurrentHashMap','CopyOnWriteArrayList'
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'Login','Refresh','Role','Menu','Permission','Button','Download','Mask','Audit','Customer','Product','Order','Setting'
Select-String -Path specs/sql/**/*.sql -Pattern 'bizadm_'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'bizadm'
```

### 예상 정상 결과

```text
- BIZADM 운영 기본 기능이 sample Controller/Service에 남아 있지 않음
- sample API가 운영 API처럼 노출되지 않음
- 하드코딩 List.of(Map.of)로 운영 데이터를 반환하지 않음
- DB 기반 Repository/Mapper 또는 기존 CPF 표준 저장 구조 사용
- 권한/감사/마스킹/다운로드 기준 적용
- ADM UI/API wrapper 존재
```

---

## 6. EXS 대외연계 기본 구현체 실개발

현재 EXS는 sample 중심으로 보인다.
이 상태는 대외연계 기본 구현체로 인정하지 않는다.

### 요청사항

EXS sample API와 sample Service에 있는 대외기관, 채널, endpoint, 인증, token, route, 거래, 전문, 통제, 재처리 기능을 CPF 표준에 맞는 대외연계 기본 구현체로 전환한다.

패키지명은 요청서가 강제하지 않는다.
Codex는 기존 CPF 패키지 표준과 대외연계 주제영역의 업무 도메인 구조를 먼저 확인한 뒤 설계한다.

### 구현해야 하는 기능

```text
- 대외기관 관리
- 대외 채널 관리
- endpoint 관리
- 인증 프로파일 관리
- 외부기관 token/secret 관리
- token 원문 노출 금지
- token/secret 암호화 또는 hash/reference 저장
- token refresh mock
- token event history
- routing rule 관리
- 수신 거래 로그 선저장
- 송신 거래 로그 선저장
- 전문 message log 선저장
- 기관/채널/endpoint 사용 가능 여부 확인
- routing rule 조회
- 업무 Service/Facade 호출 구조
- 성공 상태 갱신
- 실패 상태/오류 메시지 저장
- retryable 여부 판단
- 재처리 요청 저장
- 재처리 실행 Service
- 재처리 결과 저장
- 재처리 감사 로그
- 통제 정책 관리
```

### EXS 수신/송신 처리 흐름 필수

```text
1. X-Transaction-Id 필수 검증
2. transactionGlobalId 포맷 검증
3. 수신/송신 거래 로그 선저장
4. 전문 message log 선저장
5. 기관/채널/endpoint 사용 가능 여부 확인
6. routing rule 조회
7. 대상 업무 Service/Facade 호출
8. 성공 시 SUCCESS 상태 갱신
9. 실패 시 FAIL 상태 및 오류 메시지 저장
10. retryable 여부 판단
11. 재처리 요청 저장
12. 재처리 실행
13. 재처리 결과 저장
14. 재처리 감사 로그 저장
```

### 저품질 재작성 기준

```text
- sample Controller/Service에 운영 API가 남아 있음
- List.of(Map.of)로 운영 데이터 반환
- token/secret 원문 노출
- 수신/송신 선저장 없음
- 라우팅/실패 갱신/재처리 실행 없음
- SQL/Flyway 없음
- UI/API wrapper 없음
```

### 검증 방법

```powershell
Select-String -Path exs/src/main/java/**/*.java -Pattern 'package .*sample','SampleController','SampleService','List.of\(Map.of','ConcurrentHashMap','CopyOnWriteArrayList'
Select-String -Path exs/src/main/java/**/*.java -Pattern 'Institution','Channel','Endpoint','Auth','Token','Secret','Route','Transaction','Message','Control','Retry','Reprocess','Facade'
Select-String -Path exs/src/main/java/**/*.java -Pattern 'X-Transaction-Id','transactionGlobalId','SUCCESS','FAIL','retryable'
Select-String -Path specs/sql/**/*.sql -Pattern 'exs_'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'exs'
```

### 예상 정상 결과

```text
- EXS 운영 기본 기능이 sample Controller/Service에 남아 있지 않음
- sample API가 운영 API처럼 노출되지 않음
- 하드코딩 List.of(Map.of)로 운영 데이터를 반환하지 않음
- token/secret 원문 저장/로그 출력 없음
- 수신/송신 거래 및 전문 로그 저장
- 라우팅/실패 갱신/재처리 실행 구조 존재
- DB 기반 Repository/Mapper 또는 기존 CPF 표준 저장 구조 사용
- ADM UI/API wrapper 존재
```

---

## 7. MBR 기본 구현체 보강

### 요청사항

MBR은 일부 구조가 이미 있으므로 처음부터 다시 만들지 않는다.
현재 구현을 확인한 뒤 부족하거나 품질 낮은 부분을 보강/재작성한다.

### 구현해야 하는 기능

```text
- 회원 등록
- 회원 수정
- 회원 상태 변경
- 회원 목록
- 회원 상세
- 회원 로그인
- 로그아웃
- refresh token hash 저장
- 로그인 성공/실패 이력
- 실패 횟수/잠금
- 회원 상태별 로그인 제한
- token refresh 이력
- 회원 개인정보 기본 마스킹
- 마스킹 해제 권한/사유
- 회원 데이터 다운로드
- 다운로드 권한/사유
- 다운로드 감사
- 회원 변경 감사
- ADM 또는 운영 UI/API wrapper
```

### 저품질 재작성 기준

```text
- in-memory 회원/token 저장소 사용
- 로그인 이력/실패 이력 DB 저장 없음
- 회원 운영 API 없음
- 마스킹/다운로드/감사 없음
- Mapper XML/SQL 불일치
- 장문 한 줄 Java/XML
- UI/API wrapper 없음
```

### 검증 방법

```powershell
Select-String -Path mbr/src/main/java/**/*.java -Pattern 'Member','Login','Refresh','History','Fail','Lock','Status','Mask','Download','Audit'
Select-String -Path mbr/src/main/java/**/*.java -Pattern 'ConcurrentHashMap','CopyOnWriteArrayList','members =','refreshTokensByHash'
Select-String -Path mbr/src/main/resources/**/*.xml -Pattern 'insert','update','select','delete'
Select-String -Path specs/sql/**/*.sql -Pattern 'mbr_'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'mbr'
```

### 예상 정상 결과

```text
- 이미 구현된 인증 구조는 중복 구현하지 않음
- in-memory 회원/token 저장소 없음
- 회원 운영/마스킹/다운로드/감사 코드 존재
- 관련 SQL 존재
- ADM UI/API wrapper 존재
```

---

## 8. ADM 운영 기능 보강

### 요청사항

ADM은 기존 구현이 일부 있으므로 현재 ADM 소스를 확인하고 부족하거나 품질 낮은 부분을 보강/재작성한다.

### 구현해야 하는 기능

```text
- 플랫폼 운영자 관리
- 역할/권한
- 메뉴 권한
- 버튼 권한
- API 권한
- 다운로드 권한
- 온라인 거래 로그
- 오류 로그
- 오류 조치 상태/담당자/메모
- 배치 관리
- 알림 관리
- 동적 로그레벨
- 공통 코드 관리
- 공통 메시지 관리
- 설정 관리
- 보안/MFA 운영
- 운영 감사
- 다운로드 감사
- 마스킹/마스킹 해제 감사
- BIZADM/MBR/EXS/BAT 관제 연결
```

### 저품질 재작성 기준

```text
- Controller에서 SQL/비즈니스 로직 직접 처리
- Service/Repository/Mapper 계층 부족
- 권한 검사 없이 UI만 숨김
- 다운로드 사유/감사 없음
- 마스킹 해제 사유/감사 없음
- 배치 관제가 실제 Batch/Spring Batch와 연결되지 않음
- ADM static JS가 장문 한 줄
- API wrapper 없음
```

### 검증 방법

```powershell
Select-String -Path adm/src/main/java/**/*.java -Pattern 'Operator','Role','Permission','Menu','Button','Api','Download','Log','Error','Batch','Notification','Dynamic','Audit','Mfa','Security','Message','Code','Config'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'operator','role','permission','download','log','error','batch','notification','dynamic','audit','mfa','bizadm','mbr','exs','bat'
Select-String -Path specs/sql/**/*.sql -Pattern 'adm_','pfw_batch','BATCH_','batch'
```

### 예상 정상 결과

```text
- 기존 ADM 기능별 구현 상태가 확인됨
- 부족한 기능은 실제 코드로 보강됨
- API/Service/Repository 또는 Mapper/SQL/UI가 연결됨
- 권한/감사/다운로드/마스킹 기준이 적용됨
```

---

## 9. PFW/CMN 공통 표준 적용

### 요청사항

PFW/CMN 공통 기능을 ADM/BIZADM/MBR/EXS/BAT/EDU에 실제 적용한다.

### 필수 적용 표준

```text
- transactionGlobalId 표준
- X-Transaction-Id 검증
- 표준 응답
- 표준 오류
- 공통 예외
- Authorization Bearer token 검증
- ADM/BIZADM/MBR login realm 분리
- Refresh Token hash 저장
- token event history
- 권한 검사
- 마스킹
- 다운로드 사유
- 다운로드 감사
- 운영 감사
- NotificationSender 또는 MessageSender
- MFA/TOTP 또는 mock MFA 확장 기반
- Redis/Kafka/MQ mock/fallback
- Spring Batch 공통 JobRepository 설정
- 배치 공통 API
- 배치 공통 이벤트/알림/락/스케줄 표준
```

### 저품질 재작성 기준

```text
- 모듈마다 별도 응답/오류/권한/감사 규칙 생성
- token 원문 저장
- realm 분리 없음
- 권한 없는 API가 서버에서 차단되지 않음
- Redis/Kafka/MQ 미설정 시 앱 기동 불가
- NotificationSender 없이 ADM/업무 모듈에서 직접 발송 구현
```

### 검증 방법

```powershell
Select-String -Path pfw/src/main/java/**/*.java,cmn/src/main/java/**/*.java,adm/src/main/java/**/*.java,bizadm/src/main/java/**/*.java,mbr/src/main/java/**/*.java,exs/src/main/java/**/*.java,xyz/src/main/java/**/*.java -Pattern 'Transaction','X-Transaction-Id','Bearer','Authorization','loginRealm','loginDomain','Refresh','Permission','Mask','Download','Audit','Notification','MessageSender','MFA','Mock','Fallback','JobRepository','JobLauncher','Batch'
```

### 예상 정상 결과

```text
- 공통 표준을 각 모듈이 사용
- 모듈별로 별도 하드코딩 표준을 만들지 않음
- 실서버 없는 Redis/Kafka/MQ는 mock/fallback 기준으로 처리
- Spring Batch 공통 설정은 PFW/CPF 공통 기준으로 구성
```

---

## 10. UI/API wrapper 구현

### 요청사항

운영 기능은 API만 있으면 완료가 아니다.
ADM static UI 또는 기존 UI 기준에서 최소한 운영자가 호출 가능한 API wrapper가 있어야 한다.

### 대상

```text
ADM 운영 기능
BIZADM 운영 기능
MBR 회원 운영 기능
EXS 대외 운영 기능
BAT 배치 운영 기능
```

### 저품질 재작성 기준

```text
- API는 있는데 화면/JS wrapper 없음
- wrapper marker만 있고 실제 fetch/API 호출 없음
- 권한 없는 버튼 숨김/비활성 기준 없음
- adm.js가 장문 한 줄
- 브라우저 클릭 검증 없이 UI smoke 성공 기록
```

### 검증 방법

```powershell
Get-ChildItem -Recurse adm/src/main/resources/static -Include *.js,*.html,*.vue
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'adm','bizadm','mbr','exs','bat','batch','operator','permission','member','institution','token','retry','download','mask','audit'
```

### 예상 정상 결과

```text
- 기능별 UI marker 존재
- fetch/API wrapper 존재
- 브라우저 클릭 미수행 시 UI smoke는 미검증으로 기록
- 파일에 marker가 없으면 완료로 기록하지 않음
```

---

## 11. SQL/Flyway/Mapper 정합성

### 요청사항

Java에서 사용하는 테이블/컬럼/Mapper는 SQL/Flyway와 맞아야 한다.

### 필수 기준

```text
- Java에서 사용하는 테이블은 SQL에 존재
- Mapper method와 XML SQL 일치
- 모든 신규 테이블/컬럼 COMMENT 존재
- 모든 신규 CPF 운영 테이블에 공통 감사 컬럼 존재
- Spring Batch 표준 메타테이블과 CPF 운영 관제 메타가 목적별로 분리됨
- SQL 표준 검사 통과
```

### 저품질 재작성 기준

```text
- Java는 있는데 SQL 없음
- SQL은 있는데 Java에서 사용하지 않음
- Mapper method와 XML id 불일치
- 테이블/컬럼 COMMENT 없음
- 감사 컬럼 없음
- sample_schema에 운영 테이블을 계속 몰아넣음
```

### 검증 방법

```powershell
Get-ChildItem -Recurse specs/sql -Include *.sql
Get-ChildItem -Recurse */src/main/resources -Include *.xml,*.sql,*.yml
Select-String -Path **/*.java -Pattern 'JdbcTemplate','Mapper','select','insert','update','delete'
Select-String -Path specs/sql/**/*.sql -Pattern 'adm_','bizadm_','mbr_','exs_','pfw_','cmn_','BATCH_','batch'
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

### 예상 정상 결과

```text
- Java에서 사용하는 테이블이 SQL에 존재
- Mapper method와 XML SQL 일치
- 모든 신규 테이블/컬럼 COMMENT 존재
- 모든 신규 CPF 운영 테이블에 공통 감사 컬럼 존재
- MariaDB 실실행 못 하면 미검증으로 기록
```

---

## 12. 물리 포맷 및 품질 게이트

### 요청사항

Java/JS/XML/HTML/SQL/PS1 파일은 GitHub raw 기준으로도 사람이 읽을 수 있어야 한다.

### 저품질 재작성 기준

```text
- Java 파일이 10줄 미만 장문 파일
- JS 파일이 10줄 미만 장문 파일
- XML/HTML/SQL이 장문 한 줄
- package/import/class/method가 한 줄에 붙음
- Markdown 내용을 .html 확장자로 저장
- 한글 깨짐 또는 mojibake
```

### 검증 방법

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-java-format.ps1

Get-ChildItem -Recurse -Include *.java,*.js,*.xml,*.html,*.sql,*.ps1 | Where-Object {
  (Get-Content $_.FullName).Count -lt 10
} | Select-Object FullName

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1
```

### 예상 정상 결과

```text
- package/import/class/method가 한 줄에 붙지 않음
- adm.js 같은 JS도 장문 한 줄이 아님
- Mapper XML도 장문 한 줄이 아님
- HTML 문서는 실제 HTML 구조
- FPS/Fps/fps 잔재 없음
- 한글 깨짐 없음
```

---

## 13. Swagger/OpenAPI

### 요청사항

추가/보강한 API는 Swagger annotation을 가져야 한다.

### 저품질 재작성 기준

```text
- API는 있는데 @Tag/@Operation 없음
- X-Transaction-Id 설명 없음
- 표준 응답/오류 설명 없음
- 앱 미기동인데 OpenAPI HTTP smoke 성공 기록
```

### 검증 방법

```powershell
Select-String -Path **/*.java -Pattern '@Tag','@Operation','@Parameter','X-Transaction-Id','ApiResponse'
```

앱 기동 가능 시:

```powershell
curl http://localhost:8080/v3/api-docs
curl http://localhost:8080/swagger-ui/index.html
```

### 예상 정상 결과

```text
- 정적 Swagger annotation 존재
- 앱 미기동 시 HTTP smoke는 미검증
- HTTP 200 확인 전에는 성공으로 기록하지 않음
```

---

## 14. 문서/매트릭스/리포트 현행화

### 요청사항

문서는 작업 주체가 아니다.
소스/SQL/UI/Swagger/EDU 변경 결과에 맞춰 현행화한다.

### 대상

```text
README.md
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

### 필수 기준

```text
- HTML 문서는 실제 HTML
- 구현된 기능만 완료로 표시
- 미구현/미검증을 완료로 표시하지 않음
- 요청사항별 증적이 리포트에 있음
- 기능 구현 매트릭스는 실제 구현 상태와 일치
- 별도 변경파일 목록이나 검토보고서를 새로 만들지 않음
```

### 검증 방법

````powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
Select-String -Path CPF_STABILIZATION_REPORT.html,specs/*.html -Pattern '^# ','^\|','```'
````

---

## 15. 최종 검증 명령

가능한 범위에서 실행한다.

```powershell
.\gradlew.bat compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-java-format.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-security-seed-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-transaction-id-standard.ps1
```

선택 검증:

```powershell
node --check adm/src/main/resources/static/adm/adm.js
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
curl http://localhost:8080/v3/api-docs
```

실행하지 못한 항목은 반드시 미검증으로 기록한다.
단, 실행 환경이 없더라도 정적 검증은 반드시 수행한다.

---

## 16. 완료 인정 기준

이번 작업은 아래를 만족해야 완료로 인정한다.

```text
- README와 기존 소스 표준을 확인하고 그 기준으로 구현함
- 각 요청사항별 현재 구현을 재검토함
- 미구현 항목은 새로 구현함
- 저품질 구현은 리팩터링 또는 재작성함
- 요청서에 적은 예시 단어를 임의 패키지명으로 만들지 않음
- PFW 배치 공통 API가 실제 코드로 구현됨
- BAT 또는 배치 주제영역이 PFW 배치 공통 API를 사용함
- BAT는 PFW/CPF 공통 JobRepository 설정을 사용함
- Spring Batch 표준 메타테이블과 CPF 운영 관제 메타가 연결됨
- EDU 배치 샘플이 PFW 배치 공통 API를 사용함
- BIZADM sample 운영 API가 정식 기본 구현체로 전환됨
- EXS sample 운영 API가 정식 기본 구현체로 전환됨
- BIZADM/EXS 운영 기능이 하드코딩 List.of(Map.of)로 동작하지 않음
- MBR은 기존 구현을 반복하지 않고 부족 기능을 보강함
- ADM은 기존 구현을 확인한 뒤 부족 기능을 보강함
- 배치 실행/Step 이력/실패 메시지/ADM 관제가 연결됨
- PFW/CMN 공통 표준이 각 모듈에 적용됨
- UI/API wrapper가 존재함
- SQL/Flyway/Mapper 정합성이 맞음
- Swagger/EDU가 실제 소스 기준으로 연결됨
- 문서는 구현 결과에 맞춰 현행화됨
- 리포트에 요청사항별 증적이 있음
- 미검증 항목을 성공으로 기록하지 않음
```

---

## 17. 완료 불인정 기준

아래 중 하나라도 해당하면 완료라고 보고하지 않는다.

```text
- 구현하지 않고 리포트에만 미구현으로 기록
- PFW 배치 공통 API 없이 BAT가 직접 제각각 JobRepository/JobLauncher를 다룸
- EDU 배치 샘플이 PFW 배치 공통 API를 사용하지 않음
- sample 패키지에 운영 기본 기능이 남아 있음
- sample 이름만 바꾸고 내부 하드코딩 구조가 그대로임
- List.of(Map.of), ConcurrentHashMap, CopyOnWriteArrayList로 운영 데이터를 처리함
- Controller만 있고 Service/Repository/Mapper/SQL이 없음
- API만 있고 UI/API wrapper가 없음
- SQL만 있고 Java에서 사용하지 않음
- 문서만 바꾸고 실제 소스 구현이 없음
- 배치 문서만 있고 실제 Job/Step 코드가 없음
- BAT가 PFW/CPF 공통 JobRepository가 아니라 별도 독립 JobRepository를 임의 생성함
- Spring Batch 표준 메타테이블과 CPF 운영 관제 메타의 연결 기준이 없음
- 앱을 기동하지 않았는데 OpenAPI HTTP smoke 성공으로 기록함
- MariaDB를 실행하지 않았는데 DB smoke 성공으로 기록함
- 브라우저 클릭을 하지 않았는데 UI smoke 성공으로 기록함
- 검증하지 않은 항목을 성공으로 기록함
- CPF_REQUEST.md를 수정함
- 금지된 별도 변경파일/검토보고서를 새로 만듦
```

---

## 18. CPF_STABILIZATION_REPORT.html 기록 기준

각 요청사항별로 아래를 반드시 기록한다.

```text
요청 ID
요청사항
현재 구현 상태
현재 품질 판정
재사용/보강/재작성 여부
구현 파일
SQL/Flyway 파일
UI/API wrapper 파일
Swagger/EDU 파일
검증 방법
예상 정상 결과
실제 검증 결과
판정
미구현/미검증/실패 사유
다음 조치
```

판정값은 아래만 사용한다.

```text
완료
일부 구현
미구현
미검증
실패
재확인 필요
```

---

## 19. Codex 최종 보고 형식

작업 완료 후 아래 형식으로 보고한다.

```text
[소스 기준]
branch:
local HEAD:
origin/master HEAD:
git status 요약:

[기존 표준 확인 결과]
README 확인:
settings.gradle 확인:
패키지 규칙 확인:
테이블명 규칙 확인:
기존 계층 구조 확인:
배치 표준 확인:
Spring Batch JobRepository 확인:
PFW 배치 공통 API 확인:
EDU 배치 샘플 확인:
재확인 필요 항목:

[재검토/재작성 결과]
- 기존 구현을 재사용한 항목:
- 기존 구현을 보강한 항목:
- 저품질로 판단하여 재작성한 항목:
- 새로 구현한 항목:

[실제 구현 완료]
- 구현 파일/SQL/UI/검증 증적이 있는 항목만 기록

[일부 구현]
- 구조는 있으나 미완성인 항목
- 남은 작업과 차단 사유 기록

[미구현]
- 구현하지 못한 항목
- 차단 사유와 다음 조치 기록

[미검증]
- 실행하지 못한 검증과 사유

[실패]
- 실행했으나 실패한 검증

[재확인 필요]
- 설계 선택이 필요한 항목

[핵심 증적]
PFW 배치 공통 API:
BAT/Spring Batch 공통화:
PFW/CPF 공통 JobRepository 사용:
Spring Batch 메타와 CPF 운영 메타 연결:
EDU 배치 샘플:
BIZADM sample 전환:
EXS sample 전환:
MBR 보강:
ADM 보강:
PFW/CMN 표준 적용:
UI wrapper:
SQL/Flyway:
Swagger:
문서 현행화:

[검증 명령 결과]
compileJava:
test:
qualityGate:
check-html-docs:
check-java-format:
check-legacy-name:
check-security-seed-standard:
check-sql-standard:
check-transaction-id-standard:
node check:
MariaDB smoke:
OpenAPI HTTP smoke:

[리포트]
CPF_STABILIZATION_REPORT.html에 요청사항별 증적 기록 여부:
금지된 별도 변경파일 생성 여부:
CPF_REQUEST.md 수정 여부:
```
