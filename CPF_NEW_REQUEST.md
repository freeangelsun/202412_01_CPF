# CPF 차기 대형 작업 요청서

## 1. 작업명

**CPF 운영 로그 표준·BAT JobInstance별 일자 로그·Reliability 실검증·ADM/OpenAPI/EDU 정합성 완성 대형 마일스톤**

## 2. 작업 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 시작 commit: `1d5f2280f72719959dfd98dd78acde6299b0da74`
- 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검색 보조: `CPF_FINAL_TARGET_REQUIREMENTS_01.md` ~ `CPF_FINAL_TARGET_REQUIREMENTS_05.md`
- 운영·완료 기준: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 결과·상태 기준:
  - `CPF_STABILIZATION_REPORT.md`
  - `CPF_GAP_MATRIX.md`
  - `CPF_EVIDENCE_INDEX.md`
- 기능·샘플 기준:
  - `specs/기능_구현_매트릭스.html`
  - `specs/sample-coverage-matrix.md`

작업 시작 시 최신 `master`가 위 commit 이후 변경되었으면 최신 commit을 다시 확인하고 그 기준으로 전수 검수한다. Codex의 이전 보고 문구나 기존 상태값을 그대로 신뢰하지 말고 정본, 최신 source/test/SQL/script/config/evidence를 직접 대조한다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 정본이며 `_01`~`_05`는 검색 보조다. 현재 구현 수준에 맞춰 목표를 축소하거나 미완료 항목을 삭제하지 않는다.

## 3. 이번 요청의 배경과 최신 검수 사실

직전 작업에서 다음 개발 진전은 확인됐다.

- PFW 멱등성 엔진과 상태 전이
- Broker publisher/consumer worker와 deterministic adapter
- LOCAL 파일전송 엔진과 checksum/중복/이력/UNKNOWN 연결
- XYZ/BAT reliability EDU의 실제 PFW capability 호출
- ADM Reliability 조회·DLQ replay·UNKNOWN 수동확정 API/UI/권한/감사
- Flyway V23 및 ADM seed
- LNG 생성 모듈의 test/bootJar
- 요청서 보호 방식 보강
- Java 21 기준 qualityGate와 JUnit 실행 주장 및 일부 evidence

그러나 최신 `master` 검수에서 다음 문제가 확인됐다.

1. `.gitignore`의 모든 로그 제외 규칙을 주석 처리해 runtime 로그 약 100만 줄이 Git에 추적됐다.
2. `logs/{module}`와 `{module}/logs/{module}`가 함께 존재해 로그 root가 중복된다.
3. BAT 로그는 `cpf-bat-batch.log`, `cpf-bat-transaction.log` 두 파일에 여러 일자가 섞여 있고 JobInstance별·일자별 파일이 아니다.
4. BAT batch 로그의 `transactionGlobalId`, `runId`, `rerunId`가 비어 있는 사례가 확인됐다.
5. BAT 온라인 실행 요청과 실제 JobInstance/JobExecution/Step 실행의 추적 연결이 부족하다.
6. `NO_TRANSACTION.0.log`가 대량 생성되어 정상 시스템 로그와 거래 context 누락이 구분되지 않는다.
7. 원본 runtime 로그와 검증 evidence 로그가 같은 `.log` 확장자로 섞여 Git 관리 기준이 불명확하다.
8. LOCAL FileTransfer DOWNLOAD 대상 경로 제한과 임시 파일 cleanup 보강이 필요하다.
9. PFW/CMN/EXS/ADM/BIZADM에 범용 `EducationSample`이 남아 최신 EDU ownership 원칙과 충돌한다.
10. MariaDB V22/V23/all_install 실제 실행은 비밀번호 환경변수 미제공으로 `미검증`이다.
11. ADM Reliability DB runtime·브라우저 클릭·권한/감사 실검증이 남아 있다.
12. 전체 Controller OpenAPI coverage와 고유 `operationId` 검증이 미완이다.
13. LNG 생성기는 Java 21 하드코딩과 candidate-only 통합 방식이 남아 있다.
14. Java 25는 Gradle 8.11.1 실행 비호환으로 실패했다.
15. real broker, 외부 SFTP/FTP/SCP/SSH, 다중 인스턴스 runtime은 여전히 미검증이다.

이번 작업은 위 문제를 문서로만 정리하는 작업이 아니다. **로그 구조와 Reliability 기능을 실제 소스·설정·SQL·runtime·EDU·OpenAPI·evidence까지 연결해 운영 가능한 수준으로 전진시키는 개발 마일스톤**이다.

## 4. 작업 원칙

- 작업 범위에 필요한 source/test/SQL/Flyway/script/config/Markdown 수정은 직접 수행한다.
- 단순 검수만 반복하지 말고 반드시 실제 구현 진도를 포함한다.
- 기존 기능을 삭제하거나 목표를 낮춰 완료처럼 만들지 않는다.
- source만 존재하거나 unit test만 통과한 기능은 전체 완료가 아니다.
- 실행하지 못한 검증은 `미검증`, 실패한 검증은 `실패`, 근거가 불충분하면 `재확인 필요`로 기록한다.
- 상태값은 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.
- MariaDB 소프트웨어 설치·삭제·업그레이드를 수행하지 않는다.
- 사용자의 기설치 MariaDB가 현재 셸에서 접근 가능할 때만 신규 빈 검증 schema를 사용한다.
- 비밀번호·토큰·Authorization·개인정보·민감한 내부값을 로그나 evidence에 원문으로 남기지 않는다.
- 신규 HTML 작성은 지양하고 기존 matrix 정합성 갱신에 필요한 범위만 수정한다.
- PDF는 생성하지 않는다.
- Git commit, push, branch 생성은 수행하지 않는다.

# 5. 필수 완료 범위

## 5.1 accidental runtime 로그 Git 추적 정리와 evidence 로그 정책 확립

### 5.1.1 `.gitignore` 복구

다음 runtime 로그 제외 정책을 복구한다.

```gitignore
logs/
**/logs/
*.log
*.log.*
```

다만 검증을 위해 Git에 남겨야 하는 정제 evidence 로그는 별도 예외로 관리한다.

권장 기준:

```gitignore
!specs/evidence/
!specs/evidence/**/
!specs/evidence/**/*.sanitized.log
```

일반 `.log` 전체를 허용하지 않는다. `specs/evidence/**` 아래에서도 민감정보 제거와 규격 검사를 통과한 `*.sanitized.log`만 추적한다.

### 5.1.2 현재 추적된 runtime 로그 정리

최신 commit에 포함된 다음 종류의 runtime 로그를 repository 추적 대상에서 제거한다.

- `logs/**`
- `*/logs/**`
- 거래별 raw log
- `NO_TRANSACTION` raw log
- application/error/transaction/batch raw log
- 과거 일자 runtime log

작업 PC의 원본 로그를 무조건 삭제하지 말고 Git 추적만 해제할 수 있는 방식으로 처리한다. force push, rebase, filter-branch, history rewrite는 수행하지 않는다. 현재 branch의 정상 후속 변경으로 정리한다.

정리 후 다음을 검증한다.

- `git ls-files`에 runtime raw log가 남지 않음
- `specs/evidence/**/*.sanitized.log`만 허용
- runtime 실행 후 새 raw log가 `git status`에 나타나지 않음
- evidence 정제본은 `git status`에 나타나고 추적 가능함
- `git diff --check` 통과

### 5.1.3 evidence 로그 규격

`*.sanitized.log`는 최소 다음 메타데이터를 갖는다.

```text
CPF_EVIDENCE_VERSION
EVIDENCE_ID
STATUS
EXECUTED_AT
START_COMMIT
BRANCH
COMMAND
EXIT_CODE
JAVA_VERSION
GRADLE_VERSION
PROFILE
SANITIZED
SECRET_SCAN
TESTS
FAILURES
ERRORS
SKIPPED
LOG_SHA256
```

상태별 규칙:

- `완료`: 실행 명령과 `EXIT_CODE=0`이 있어야 함
- `실패`: 실패 원인과 분류가 있어야 함
- `미검증`: 미실행 사유와 재현 명령이 있어야 함
- `SKIPPED`가 있으면 해당 검증 범위를 명확히 분리
- 로그 hash는 최종 정제본 기준으로 기록
- 절대 개인 경로는 상대경로나 마스킹값으로 치환

### 5.1.4 evidence Git 추적 검증

기존 `check-evidence-path-existence`를 다음 수준으로 보강한다.

- 로컬 파일 존재
- `git ls-files` 추적 여부
- 현재 commit tree 포함 여부
- report/gap/index/matrix에서 참조한 path와 실제 path 일치
- stale evidence 여부
- 동일 evidence를 다른 실행처럼 중복 참조하는지 검사

`qualityGate`가 로컬에는 있지만 Git에 없는 evidence를 완료 근거로 허용하지 않게 한다.

## 5.2 전체 실행 주체 로그 root·파일명·일자 회전 표준

대상 실행 주체는 ACC/MBR/EXS/ADM/BAT/BIZADM/XYZ다. PFW/CMN/EDU는 독립 실행 주체가 아니다.

### 5.2.1 로그 root 단일화

표준 root:

```text
${CPF_LOG_ROOT}/{moduleCode}/
```

기본 local 경로는 `./logs`를 사용할 수 있으나, 다음 이중 생성을 금지한다.

```text
logs/acc/...
acc/logs/acc/...
```

PFW/CMN 로그는 실제 실행 주체 아래에 귀속한다. 예를 들어 BAT 프로세스에서 발생한 PFW 로그는 BAT 로그 root 아래에 기록한다.

### 5.2.2 파일명 표준

일반 실행 로그는 최소 다음 요소를 파일명에 포함한다.

```text
cpf-{moduleCode}-{logType}-{instanceId}.{yyyy-MM-dd}.log
```

허용 `logType` 예:

- application
- transaction
- error
- audit
- security
- worker

예:

```text
cpf-adm-application-adm-local-01.2026-07-13.log
cpf-xyz-transaction-xyz-local-01.2026-07-13.log
```

### 5.2.3 일자 회전·보관·압축

- TimeBasedRollingPolicy 또는 동등한 일자 회전 적용
- timezone 명시
- 보관 일수 설정 가능
- 보관 로그 압축 가능
- active 파일과 archive 파일명 충돌 방지
- 다중 instance 파일 충돌 방지
- 로그 파일 생성 경로 실패 시 명확한 startup 경고 또는 실패 정책
- 모든 환경 `local/dev/stg/prod`에서 동일한 설정 키 사용

실제 두 날짜 파일 생성 검증 또는 clock 주입 기반 회전 테스트를 남긴다. 단순 설정 문자열 존재만으로 완료 처리하지 않는다.

## 5.3 BAT JobInstance별 일자 로그 구현

사용자가 원하는 BAT 배치 로그 단위는 서버 인스턴스별 파일이 아니라 **`businessDate + jobName + jobInstanceId` 단위**다.

### 5.3.1 표준 경로와 파일명

```text
${CPF_LOG_ROOT}/bat/jobs/{businessDate}/{jobName}/
cpf-bat-{jobName}-{jobInstanceId}-{businessDate}.log
```

예:

```text
logs/bat/jobs/20260713/CPF_BAT_SMOKE_JOB/
cpf-bat-CPF_BAT_SMOKE_JOB-42-20260713.log
```

### 5.3.2 분리 기준

- 새로운 JobInstance는 별도 파일 생성
- 같은 JobInstance의 최초 실행·실패·재시작은 동일 파일에 이어서 기록
- 실행 회차는 `jobExecutionId`, `restartAttempt`, `rerunId`로 구분
- JobInstance 시작 시 `businessDate`를 확정
- 자정을 넘어 종료하더라도 시작 businessDate 파일에 유지
- 다른 JobInstance가 한 파일에 섞이면 실패
- 같은 JobInstance 재시작이 다른 파일로 분리되면 실패
- serverInstanceId와 workerInstanceId는 파일 분리 기준이 아니라 각 레코드 필수 필드

### 5.3.3 필수 로그 필드

모든 BAT JobInstance 로그 레코드에 최소 다음을 포함한다.

- timestamp
- timezone
- businessDate
- moduleCode
- logType
- eventType
- jobName
- jobInstanceId
- jobExecutionId
- stepName
- stepExecutionId
- runId
- rerunId
- originalJobExecutionId
- restartAttempt
- status
- startTime
- endTime
- durationMs
- readCount/writeCount/filterCount/skipCount/commitCount/rollbackCount
- transactionGlobalId
- parentTransactionGlobalId
- segmentId/parentSegmentId
- serverInstanceId
- workerInstanceId
- selectedInstanceId 또는 selected worker
- errorCode
- errorMessage 마스킹값

### 5.3.4 transactionGlobalId·segment 연결

- 온라인 API에서 배치를 실행하면 API `transactionGlobalId`를 batch parent로 전달
- scheduler/standalone 실행이면 JobInstance 생성 시 parent transactionGlobalId를 발급
- JobExecution, StepExecution, chunk/item은 parent-child segment로 연결
- retry/restart/rerun은 원 JobInstance와 originalExecution 관계를 유지
- 파일 로그와 DB 로그의 transactionGlobalId/segment가 일치
- ADM에서 transactionGlobalId, jobName, jobInstanceId, jobExecutionId로 조회 가능

### 5.3.5 필수 검증 시나리오

최소 다음을 실제 테스트한다.

1. 동일 jobName으로 서로 다른 JobInstance 두 개 실행
2. 하나의 JobInstance 실패 후 restart
3. 같은 JobInstance의 여러 JobExecution이 같은 파일에 기록
4. 다른 JobInstance는 다른 파일에 기록
5. 서로 다른 businessDate 실행
6. 자정 경계 또는 주입 clock을 이용한 businessDate 고정 검증
7. 서로 다른 server/worker instance에서 같은 JobInstance 후속 처리
8. 파일명 식별자와 본문 식별자 일치
9. JSON Lines 전체 파싱
10. 민감정보 마스킹
11. ADM 조회 연결
12. jobExecution 단위 file log와 DB log 상호 추적

단순 `cpf-bat-batch.log` 단일 파일은 목표를 충족하지 않는다.

## 5.4 `NO_TRANSACTION` 정책과 거래 context 누락 검출

현재 `NO_TRANSACTION.0.log`를 정상 로그 집합처럼 사용하는 구조를 정리한다.

구분 기준:

- startup/system
- scheduler
- health
- worker
- transaction
- context-missing anomaly

거래 context가 필수인 controller/service/worker/batch 구간에서 transactionGlobalId가 누락되면:

- warning 또는 error
- metric 증가
- 테스트 실패
- qualityGate 탐지
- ADM 진단 대상

으로 처리한다.

startup/health 같은 비거래 로그는 application 또는 system log에 기록하고 `NO_TRANSACTION` 거래 파일에 몰아넣지 않는다.

## 5.5 로그 형식·마스킹·정합성 qualityGate

신규 또는 보강 스크립트로 다음을 검사한다.

- JSON Lines 파싱
- UTF-8/mojibake
- 필수 필드
- 허용 logType/eventType/status
- timestamp/timezone/businessDate 정합성
- 파일명과 본문 moduleCode/logType/instanceId/date 일치
- BAT 파일명과 jobName/jobInstanceId/businessDate 일치
- 서로 다른 JobInstance 혼합 여부
- restart가 원 JobInstance 파일에 이어지는지
- transactionGlobalId 형식
- parent-child segment 연결
- serverInstanceId/workerInstanceId 정합성
- error message·header·body 마스킹
- multiline stack trace 처리 방식
- runtime raw log Git 추적 금지
- sanitized evidence Git 추적 여부
- 동일 파일 무제한 증가 방지
- retention/compression 설정 누락

검증 결과는 신규 non-overwrite evidence 디렉터리에 저장한다.

## 5.6 Reliability capability 실 DB·runtime 보강

직전 구현을 유지하고 실제 운영형 검증을 닫는다.

### 5.6.1 기설치 MariaDB 검증

현재 셸에서 MariaDB 접근 가능 여부를 비파괴 preflight로 확인한다.

접속 가능하면 신규 빈 검증 schema로 다음을 실행한다.

- split SQL
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- Flyway baseline
- V22 reliability persistence
- V23 ADM reliability menu/permission
- V22 → V23 upgrade
- PK/UK/index/default/NOT NULL
- seed
- 재실행 정책
- cleanup

접속정보가 없거나 접근 불가하면 설치하지 말고 환경변수명, 재현 명령, 원인 evidence를 남겨 `미검증` 처리한다.

### 5.6.2 멱등성

실 MariaDB 또는 가능한 통합 환경에서 다음을 검증한다.

- 동일 key+동일 payload replay
- 동일 key+다른 payload 거절
- concurrent duplicate
- PROCESSING timeout/expiry
- SUCCESS response 재응답
- FAILED/UNKNOWN retry
- 재기동 후 상태 유지
- unique constraint 경쟁
- 온라인/BAT/Broker/FileTransfer 실제 연결
- transactionGlobalId/segment/timeline

### 5.6.3 Broker

- outbox atomic 저장
- claim/lock
- publisher 성공·실패·unknown
- retry/backoff
- inbox 중복 방지
- DLQ 이동
- replay
- 재기동 복구
- 다중 worker 경쟁은 가능하면 실제 검증
- real broker가 없으면 deterministic adapter와 DB 상태전이까지만 완료 후보
- Kafka/MQ runtime은 별도 `미검증`

### 5.6.4 FileTransfer 보안·복구

기존 LOCAL adapter를 다음까지 보강한다.

- DOWNLOAD `localPath` 허용 base path 제한
- normalize 후 base path 이탈 차단
- symbolic link 우회 차단
- 임시 파일 `finally` cleanup
- checksum 실패 cleanup
- atomic rename 실패 처리
- overwrite 정책
- 중복 reservation의 DB 원자성
- 다중 instance 경쟁
- 재기동 복구
- unknown/reconciliation
- 대용량 streaming

SFTP/FTP/SCP/SSH는 실제 구현·runtime 여부를 분리해 기록한다.

## 5.7 ADM Reliability 운영 완성

다음 기능의 source뿐 아니라 가능한 runtime을 검증한다.

- idempotency 목록/상세
- outbox/inbox/DLQ 목록/상세
- DLQ replay
- file transfer history/상세
- unknown/reconciliation 목록/상세
- 수동 성공/실패 확정
- 재조회 요청
- worker 상태
- BAT JobInstance 로그 조회·다운로드 또는 안전한 참조
- transactionGlobalId/jobInstanceId/jobExecutionId 연계 조회

목록·검색·상세 필드:

- 처리 시작/종료 시각
- 상태
- 소요시간
- transactionGlobalId
- segment
- module/instance/worker
- jobName/jobInstanceId/jobExecutionId/stepExecutionId
- 실패 구간
- retry count/next retry
- external key/business key
- selected adapter/instance
- 생성자/처리자
- 처리 사유
- 오류 코드/마스킹 메시지

수동 작업 기준:

- 권한
- 사유 필수
- 변경 전후 상태
- operationId
- 감사 로그
- 중복 처리 방지
- 200/403/409 등 상태 검증

브라우저 실행 가능 시 실제 클릭 smoke를 수행한다. 불가능하면 API runtime, UI static, browser 미검증을 구분한다.

## 5.8 EDU ownership 정리와 실제 샘플 보강

최신 ownership 원칙:

- 온라인·일반·대외연계 EDU: `xyz/.../edu`
- 배치·센터컷 EDU: `bat/.../edu`
- PFW: capability/port/engine/reference adapter + unit/contract test
- CMN: 공통 기능 구현 + unit/contract test
- EXS/ADM/BIZADM/ACC/MBR 등 업무 모듈: 범용 개발자 EDU를 소유하지 않음

PFW/CMN/EXS/ADM/BIZADM 등에 남아 있는 범용 `EducationSample`을 조사해 XYZ/BAT로 이전하거나 성격에 맞게 contract test/reference adapter로 정리한다.

삭제 또는 이전 시 다음을 함께 정합화한다.

- source
- test
- package
- sampleId
- sample coverage
- 기능 matrix
- evidence
- report/gap/index
- architecture ownership gate

XYZ/BAT EDU는 실제 PFW capability를 호출해야 하며 local Map/List로 흉내 내지 않는다.

이번 로그 마일스톤의 신규 EDU:

- XYZ 일반 application/transaction/error 로그 예제
- BAT JobInstance별 일자 로그 예제
- BAT restart/rerun 로그 예제
- BAT transactionGlobalId/segment 연결 예제
- 로그 마스킹 실패 예제
- ADM 로그 조회 예제

## 5.9 Swagger/OpenAPI 전수 coverage

신규 및 기존 모든 REST Controller를 대상으로 자동 검사를 보강한다.

필수:

- `@Tag`
- `@Operation`
- 고유 `operationId`
- request/response schema
- 표준 오류 response
- 필수 header
- transactionGlobalId/header 설명
- 권한·마스킹·감사 설명

검사:

- Controller 대비 OpenAPI 누락
- operationId 누락·중복
- request/response schema 미노출
- 표준 오류 response 누락
- 필수 header 누락
- ADM Reliability/BAT 운영 API 누락

Swagger UI 접속만으로 완료 처리하지 않는다.

## 5.10 신규 주제영역 생성기 보강

현재 LNG 생성 결과를 기준으로 다음을 보강한다.

- Java 21 하드코딩 제거
- 프로젝트 Java 목표와 생성 모듈 toolchain 일치
- source/SQL/Flyway/yml/test/smoke/OpenAPI/ADM/registry/deploy inventory 연결
- candidate 파일만 생성하는 경우 apply/검증 경계 명확화
- 임시 LNG를 실제 통합 모드 또는 안전한 격리 모드로 생성
- compile/test/bootJar
- ownership scan
- 금지 의존성 0건
- 생성 결과 cleanup
- 로그 설정도 본 요청의 단일 root·파일명·instance·일자 기준을 사용
- 생성 업무 모듈에는 범용 EDU를 복사하지 않음

## 5.11 Java 25 검증

Java 25 목표를 삭제하지 않는다.

- Gradle wrapper와 Spring Boot/plugin 호환성을 공식 지원 범위 기준으로 점검
- 업그레이드가 필요하면 영향 범위를 분석하고 합리적으로 수정
- 전체 compile/test/bootJar/qualityGate
- 실행 주체 대표 bootJar smoke
- LNG 생성 모듈 Java 25
- 실패 시 정확한 원인과 대안 기록

Java 21에서만 성공한 결과로 Java 25 완료 처리하지 않는다.

# 6. 보강 범위

필수 범위 진행 중 재작업 방지에 도움이 되면 함께 보강한다.

- 로그 retention/archive/compression 정책
- ADM에서 로그 보관 정책 조회
- 로그 다운로드 권한·감사
- trace boost와 JobInstance 로그 연결
- jobExecution 단위 로그 파일 다운로드
- batch restart/compensation/unknown result 로그
- multi-instance worker drain
- selectedInstanceId/workerInstanceId 표준
- OpenTelemetry traceId와 transactionGlobalId 연결
- log volume/rate/size metric
- 비정상 context 누락 alert
- stale evidence 및 다른 commit evidence 탐지
- request protection의 시작 commit/blob SHA 검증
- raw log secret scan 보조
- SBOM/dependency/license/vulnerability/secret scan

# 7. 착수 범위

필수 범위가 안정적으로 닫힌 뒤 남는 작업량에서 다음을 착수할 수 있다.

- real Kafka/MQ adapter runtime
- SFTP/FTP/SCP/SSH 검증 harness
- 다중 BAT worker failover
- Service Call unknown/reconciliation 연계
- Remote Facade Proxy 다중 HTTP runtime
- SLI/SLO와 p95/p99 운영 지표
- dynamic config/feature flag/kill switch
- cache/rate-limit/bulkhead/backpressure
- graceful shutdown과 rolling drain
- 성능·부하·soak·spike·복구 검증

착수만 한 항목은 완료로 기록하지 않는다.

# 8. 후순위·제외 범위

- 실제 운영 Redis/Kafka/MQ 접속
- 실제 외부 SFTP/FTP/SCP/SSH 서버가 필요한 검증
- Vault/KMS/HSM 실연동
- 외부 WAS/JNDI 실배포
- Git history rewrite
- force push
- 전체 최종 HTML/PDF 정본화
- CPF 전체 최종 완료 선언

환경이 없는 항목은 삭제하지 않고 `미검증` 또는 `재확인 필요`로 유지한다.

# 9. 필수 테스트·evidence

이번 작업 신규 evidence는 기존 디렉터리를 덮어쓰지 않고 새 순번 디렉터리에 생성한다.

최소 evidence:

- 시작 commit
- 작업 후 diff 목록
- Java/Gradle 버전
- MariaDB preflight
- `.gitignore` 정책 검사
- runtime raw log Git 비추적 검사
- sanitized evidence 추적 검사
- 전체 로그 root 중복 검사
- 파일명 표준 검사
- 일자 rollover 검사
- BAT JobInstance 두 개 분리
- 동일 JobInstance restart 연속 기록
- businessDate 고정
- JSON Lines 파싱
- transactionGlobalId/segment 연결
- file log/DB log 정합성
- `NO_TRANSACTION` 누락 탐지
- 로그 마스킹
- ADM Reliability API runtime
- 권한/감사
- browser click 또는 미검증 사유
- MariaDB V22/V23/all_install/smoke
- Reliability module test
- FileTransfer path security/temp cleanup
- EDU ownership scan
- sample coverage
- OpenAPI coverage
- LNG generation/test/bootJar
- Java 25
- `git diff --check`
- qualityGate

실행 증적 로그는 `*.sanitized.log` 규격으로 Git 추적 가능하게 만든다. raw runtime 로그는 evidence로 push하지 않는다.

JUnit 결과는 다음 중 하나 이상으로 독립 확인 가능하게 남긴다.

- sanitized test log
- JUnit XML summary
- test count/failure/error/skipped JSON
- 실행 command/exit code/hash

# 10. 상태·문서 정합성

작업 종료 시 다음을 실제 상태로 갱신한다.

- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- `specs/기능_구현_매트릭스.html`
- `specs/sample-coverage-matrix.md`

반드시 구분한다.

- 직접 실행 완료
- 소스 구현 완료 후보
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요
- 기존 evidence 유지
- 이번 신규 evidence
- 외부 환경 부재

특히 다음은 실제 검증 전 완료로 두지 않는다.

- BAT JobInstance별 일자 로그
- 서로 다른 JobInstance 파일 분리
- restart 동일 파일 연결
- BAT transactionGlobalId/segment
- runtime 로그 Git 비추적
- sanitized evidence Git 추적
- MariaDB V22/V23
- ADM browser
- 전체 OpenAPI
- Java 25
- real broker
- 외부 파일전송 protocol
- 다중 인스턴스 runtime

# 11. 작업 금지사항

- Git commit 금지
- Git push 금지
- branch 생성 금지
- force push 금지
- Git history rewrite 금지
- `CPF_NEW_REQUEST.md` 수정 금지
- 민감정보 원문 기록 금지
- raw runtime 로그를 evidence라는 이유로 대량 Git 추적 금지
- 실행하지 않은 검증 완료 기록 금지
- 목표 확인 없이 범위 축소 금지
- source만으로 capability 완료 처리 금지
- CoverageCatalog-only 완료 처리 금지
- PFW capability를 EXS 등 업무 모듈 소유로 이동 금지
- URL 직접 조합, Controller 직접 호출, 타 주제영역 DB/Repository/Mapper 직접 접근 금지
- 핵심 거래를 Spring Event만으로 완료 처리 금지
- 문서량 채우기 금지
- 불필요한 신규 HTML 작성 금지
- PDF 생성 금지
- MariaDB 설치·삭제·업그레이드 금지

# 12. 완료 판단 핵심

이번 마일스톤은 다음 조건이 실제로 충족되어야 완료 후보가 된다.

1. runtime raw 로그가 Git 추적 대상에서 제거되고 이후 실행에도 다시 추적되지 않는다.
2. sanitized evidence 로그만 규격과 secret scan을 통과해 Git 추적된다.
3. 로그 root가 하나로 통일되고 실행 주체별 파일명·일자 회전 기준이 적용된다.
4. BAT 로그가 `businessDate + jobName + jobInstanceId` 단위로 분리된다.
5. 같은 JobInstance의 restart는 같은 파일에 이어지고 다른 JobInstance는 섞이지 않는다.
6. BAT JobInstance/JobExecution/Step에 transactionGlobalId와 segment가 연결된다.
7. `NO_TRANSACTION`을 정상 거래 로그 저장소로 사용하지 않고 context 누락을 탐지한다.
8. 사용자의 기설치 MariaDB에 접근 가능하면 V22/V23/all_install/smoke를 신규 빈 schema에서 실제 검증한다.
9. ADM Reliability와 BAT 로그 운영 조회가 권한·감사·수동처리 기준으로 연결된다.
10. FileTransfer DOWNLOAD 경로 보안과 temp cleanup이 보강된다.
11. EDU ownership이 XYZ/BAT 기준으로 정리되고 실제 sample/test/evidence가 일치한다.
12. 전체 Controller OpenAPI coverage가 자동 검증된다.
13. LNG 생성기와 Java 목표가 프로젝트 기준과 일치한다.
14. report/gap/matrix/evidence가 과대 완료 없이 일치한다.

실행하지 못한 항목은 완료가 아니다. 실패 원인은 요구사항/설계/구현/계층/SQL/설정/profile/테스트/환경/DB/broker/browser/evidence/ownership/Spring Event/OpenAPI/EDU 중 어디에 해당하는지 분류하고, 같은 접근을 반복하지 말고 대안을 제시한다.
