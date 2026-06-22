# CPF 다음 스텝 요건 목록

## 0. 현재 검수 결과 요약

이번 작업으로 ADM 배치 관제 API, worker heartbeat 조회, lock 조회/해제, ghost 후보 조회/조치, V9 migration, 배치 관련 문서/매트릭스/리포트가 일부 보강되었다.

다만 아래 항목은 아직 완료가 아니다.

```text
- BAT 독립 실행 모듈 없음
- ADM 앱 실제 기동 OpenAPI smoke 미검증
- ADM 브라우저 실제 클릭 검증 미수행
- 장시간 실행 Batch heartbeat listener 미완성
- ghost 자동 감지 스케줄러/알림 미완성
- 실제 JobRepository 기반 장시간/실패/재실행 E2E 검증 부족
- 센터컷/온디맨드/스케일아웃 구조는 아직 후속 요건
- 온라인/배치 로그 정책 + 거래 메타 자동 등록은 아직 후속 요건
```

현재 판정은 `부분 구현 / 운영 검증 미완료 포함`으로 둔다.

---

# 1. 다음 요청 1순위: OpenAPI 안전 기동/종료 + ADM 브라우저 클릭 검증

## 목표

ADM 배치 관제 API와 UI가 실제 애플리케이션 기동 상태에서 동작하는지 검증한다.

## 완료 범위

```text
- 앱 안전 기동/종료 자동화
- health check
- OpenAPI smoke
- ADM 배치 API smoke
- ADM UI 정적 smoke
- 가능하면 브라우저 클릭 자동화
- 브라우저 클릭 불가 시 구현 상태와 실클릭 미검증 상태 분리 기록
```

## 필수 구현/보강

```text
- runLocalServices 또는 동등한 앱 기동 스크립트 안정화
- PID 추적
- 기동 timeout 처리
- health endpoint 확인
- smoke 실행 후 정상 종료/cleanup
- 기동 실패 시 로그 파일 위치와 실패 사유 기록
- OpenAPI smoke가 앱 미기동 상태에서 성공으로 기록되지 않도록 방지
```

## 필수 테스트

```text
- ADM 앱 기동 성공
- health check 성공
- OpenAPI 문서 접근 성공
- ADM 배치 API endpoint 접근 성공
- 앱 종료/cleanup 성공
- 기동 실패 시 미검증 기록 확인
- 브라우저 클릭 가능 시 Job 목록/상세/Step/worker/ghost 화면 클릭 검증
```

## 완료 불인정

```text
- 앱을 기동하지 않았는데 OpenAPI 성공으로 기록
- 브라우저 클릭을 하지 않았는데 ADM UI 검증 완료로 기록
- 정적 marker 확인만으로 운영자 화면 검증 완료 처리
- PID cleanup 없이 앱 프로세스를 남김
```

---

# 2. 다음 요청 2순위: Batch Listener Heartbeat / 처리율 갱신 / Ghost 자동 감지

## 목표

현재 조회/조치 중심의 heartbeat/ghost 기능을 실제 장시간 배치 실행 중 자동 갱신/감지 구조로 보강한다.

## 완료 범위

```text
- Job/Step listener 기반 heartbeat 갱신
- 장시간 실행 중 worker 상태 갱신
- 처리율/TPS/진행률 갱신
- ghost 자동 감지 scheduler
- ghost candidate 자동 등록
- ADM ghost 알림 또는 운영 로그 연동
```

## 필수 구현

```text
- BatchJobExecutionListener
- BatchStepExecutionListener
- heartbeat update interval 기준
- processed count 갱신
- success/fail count 갱신
- avg elapsed ms / max elapsed ms 갱신
- RUNNING 상태 + heartbeat timeout 감지
- ghost candidate 자동 등록
- ghost 자동 감지 이력 저장
- ADM에서 자동 감지 결과 조회
```

## 필수 테스트

```text
- 장시간 실행 Job에서 heartbeat 주기 갱신
- Step 실행 중 처리량 갱신
- worker heartbeat timeout 감지
- ghost candidate 자동 생성
- ghost 조치 사유 누락 차단
- ghost 조치 감사 로그 저장
```

## 완료 불인정

```text
- heartbeat 조회 API만 있고 실행 중 갱신 없음
- ghost 후보 수동 조회만 있고 자동 감지 없음
- 처리율/진행률 갱신 없음
- 장시간 Job 테스트 없음
```

---

# 3. 다음 요청 3순위: BAT 독립 실행 모듈 신설

## 목표

BAT를 PFW/ADM 내부 기능이 아닌 독립 실행 가능한 Spring Batch 실행 구현체로 만든다.

## 원칙

```text
PFW:
- 배치 공통 API/표준/메타 제공

BAT:
- 독립 실행 가능한 Spring Boot batch worker
- PFW 배치 공통 API 사용
- Spring Batch Job/Step 실행 구현
- worker heartbeat, lock, retry, recovery 처리

ADM:
- BAT 실행 상태와 결과 관제

업무 모듈:
- BAT가 직접 DB를 수정하지 않고 Handler/Service/Facade 호출
```

## 필수 구현

```text
- settings.gradle include 'bat'
- bat/build.gradle
- bat/src/main/java/cpf/bat/BatApplication.java 또는 BatchWorkerApplication
- executable jar 구동 가능
- profile: bat-local, bat-worker, scheduler-off 등
- actuator/health endpoint
- worker heartbeat
- JobLauncher 실행
- JobRepository 표준 사용
- PFW CpfBatchLauncher 또는 공통 API 사용
- 독립 JobRepository 임의 생성 금지
```

## 구동 기준

```text
- java -jar bat/build/libs/cpf-bat.jar --spring.profiles.active=bat-local 형태로 구동 가능
- 별도 batch worker 서버에 독립 배포 가능
- 필요 시 WAS/컨테이너 환경에도 배포 가능
- 다중 worker 구동 시 DB lock/claim/heartbeat로 중복 실행 방지
```

## 필수 테스트

```text
- :bat:test 성공
- bat executable jar 생성 확인
- BAT health check
- Job 정상 실행
- Job 실패 실행
- 중복 실행 차단
- worker heartbeat 저장
- ADM에서 BAT worker 상태 조회
```

## 완료 불인정

```text
- BAT가 library 수준이고 독립 구동 불가
- PFW/ADM 내부에 배치 실행 로직이 흡수됨
- settings.gradle/build.gradle 연결 없음
- 독립 JobRepository 임의 생성
- :bat:test 없음
```

---

# 4. 다음 요청 4순위: 온라인/배치 로그 정책 + 거래 메타 자동 등록

## 목표

파일 로그와 DB 로그의 기본 정책을 yml로 관리하고, ADM은 장애/이슈 대응용 override로 사용한다. Controller/API 거래 메타는 자동 등록하고 ADM 로그 메뉴와 연결한다.

## 원칙

```text
- 파일 로그와 DB 로그는 모두 yml 기본 정책을 가진다.
- 파일 로그는 기본적으로 항상 남긴다.
- DB 로그는 전체/모듈/거래/Batch Job/Step 단위로 저장 여부를 제어한다.
- ADM 설정은 상시 원천이 아니라 기간/사유/감사 이력이 있는 override다.
```

## 우선순위

```text
1. ADM 활성 override
2. DB 운영 정책
3. application.yml 기본값
4. CPF 기본값
```

## 필수 구현

```text
- pfw_transaction_meta
- pfw_log_policy
- pfw_log_policy_override
- pfw_log_policy_audit
- Controller/API @CpfTransaction 또는 동등한 메타
- ApplicationReadyEvent 이후 RequestMapping 스캔
- 비동기/증분 upsert
- 사라진 거래 inactive 처리
- ADM 거래 상세에서 거래 로그/오류 로그/감사 로그 자동 연결
```

## 필수 테스트

```text
- yml 기본 정책 로딩
- ADM override 적용
- override 만료 후 기본 정책 복귀
- 거래 메타 자동 등록
- 거래ID/거래 논리명 누락 차단
- DB 로그 ON/OFF 실제 저장 여부 확인
- 변경 사유 누락 차단
- 감사 이력 저장
```

---

# 5. 다음 요청 5순위: 온디맨드 배치 구현

## 목표

운영자/API/업무 요청에 의해 즉시 실행되는 배치 구조를 만든다.

## 필수 구현

```text
- 온디맨드 실행 요청 API
- 요청자/요청 사유 필수
- Job parameter validation
- 중복 요청 방지
- 실행 대기/실행 중/성공/실패 상태
- JobExecutionId 연결
- ADM 실행 결과 조회
- 실패 시 재실행
```

## EDU 위치

```text
bat/src/main/java/cpf/bat/edu/ondemand
xyz/src/main/java/cpf/xyz/edu/batch/ondemand
```

## 필수 테스트

```text
- 정상 요청
- 요청 사유 누락 차단
- 중복 요청 차단
- 실행 성공
- 실행 실패
- 재실행
- ADM 조회
```

---

# 6. 다음 요청 6순위: 센터컷 기본 구현체 + 업무별 확장 저장소 구조

## 목표

센터컷은 BAT 기본 구현체로 제공하되, 대량 item/result 테이블을 PFW에 고정으로 강제하지 않는다. 개발자는 필요 시 업무별 커스텀 모수/item/result 테이블을 만들고 표준 adapter로 연동할 수 있어야 한다.

## 책임 기준

```text
PFW:
- 센터컷 표준 인터페이스
- 상태 코드
- 오류 코드
- 로그/감사 기준
- parent/child transactionGlobalId 기준

BAT:
- 독립 실행 가능한 센터컷 기본 구현체
- 기본 모수/item/result 테이블
- worker, claim/lock, heartbeat, retry, metering, ghost recovery

업무 주제영역:
- 필요 시 자체 모수/item/result 테이블 추가
- CenterCutTargetProvider / Handler / ResultProvider adapter 구현

ADM:
- 기본 테이블이든 커스텀 테이블이든 공통 관제
```

## BAT 기본 테이블

```text
- bat_center_cut_request
- bat_center_cut_item
- bat_center_cut_result
- bat_center_cut_worker
- bat_center_cut_metering
- bat_center_cut_retry
```

## 업무별 확장 예

```text
- mbr_center_cut_item
- mbr_center_cut_result
- exs_center_cut_message_item
- exs_center_cut_retry_result
- bizadm_center_cut_target
- bizadm_center_cut_result
```

## 표준 adapter

```text
- CenterCutTargetProvider
- CenterCutItemStateRepository
- CenterCutResultProvider
- CenterCutMeteringProvider
- CenterCutHandler
```

## 필수 구현

```text
- 대상 주입
- item DB 저장
- worker claim/lock
- heartbeat
- timeout recovery
- 실패 item 재처리
- 미터링
- ADM 진행률/결과/실패/재처리 조회
- 기본 테이블 사용 방식
- 업무별 커스텀 테이블 연동 방식
```

## 완료 불인정

```text
- PFW에 센터컷 대량 item/result 테이블을 고정 강제
- BAT 기본 구현체 없음
- 업무별 커스텀 테이블 연동 불가
- ADM이 커스텀 테이블 사용 시 진행률/결과/실패/재처리 조회 불가
- BAT가 업무 DB를 직접 수정하고 주제영역 Handler/Service를 우회
```

---

# 7. 다음 요청 7순위: BAT EDU / XYZ EDU 배치 샘플 보강

## 목표

배치 개발자와 업무 개발자가 문서와 샘플만 보고 배치/온디맨드/센터컷을 구현할 수 있게 한다.

## BAT EDU

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

## XYZ EDU

```text
xyz/src/main/java/cpf/xyz/edu/batch
xyz/src/main/java/cpf/xyz/edu/batch/ondemand
xyz/src/main/java/cpf/xyz/edu/batch/centercut
xyz/src/main/java/cpf/xyz/edu/batch/transaction
```

## 필수 예제

```text
- 정기 배치
- 온디맨드 배치
- 센터컷 기본 테이블 사용
- 센터컷 업무별 커스텀 테이블 사용
- chunk commit
- tasklet
- retry/skip
- parent/child transactionGlobalId
- 실패 item 재처리
- ADM 관제 연결
```

---

# 8. 다음 요청 8순위: 배치 개발 가이드 / 트랜잭션 가이드 정본화

## 목표

배치와 트랜잭션 기준을 개발자가 실제 구현 가능한 수준으로 문서화한다.

## 배치 개발 가이드 필수 내용

```text
- CPF 배치 아키텍처
- PFW/BAT/ADM 책임 경계
- Spring Batch JobRepository 기준
- BATCH_*와 CPF 운영 메타 관계
- 정기 배치
- 온디맨드 배치
- 센터컷
- chunk/tasklet/partition
- retry/skip/restart
- scheduler
- worker heartbeat
- ghost recovery
- scale-out worker
- ADM 배치 관제
- BAT/XYZ EDU 예제
- 검증 명령
```

## 트랜잭션 가이드 필수 내용

```text
- 온라인 API 트랜잭션
- Service 계층 transaction 기준
- 외부 호출과 DB lock 분리
- 감사 로그 트랜잭션
- EXS 수신/송신 트랜잭션
- 배치 chunk 트랜잭션
- 센터컷 item 트랜잭션
- skip/retry/rollback 기준
- parent/child transactionGlobalId 기준
- 실패 item 재처리 기준
```

---

# 9. 추천 실행 순서

```text
1. OpenAPI 안전 기동/종료 + ADM 브라우저 클릭 검증
2. Batch Listener Heartbeat / 처리율 갱신 / Ghost 자동 감지
3. BAT 독립 실행 모듈 신설
4. 온라인/배치 로그 정책 + 거래 메타 자동 등록
5. 온디맨드 배치 구현
6. 센터컷 기본 구현체 + 업무별 확장 저장소 구조
7. BAT EDU / XYZ EDU 배치 샘플 보강
8. 배치 개발 가이드 / 트랜잭션 가이드 정본화
```

각 요청은 한 번에 하나의 묶음만 수행한다.
선택 범위 밖의 항목은 구현하지 말고 다음 보강 후보로만 기록한다.
