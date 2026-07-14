# CPF 다음 작업 요청서

## 1. 작업명

**CPF 운영 로그·DB 거래 추적·다중 인스턴스·ADM 통합조회·저장소 정리 최종 보강 대형 마일스톤**

---

## 2. 작업 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 최신 commit: `255dd6ee752e6c8434eb4bd47c2524dca71cd196`
- 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검색 보조: `CPF_FINAL_TARGET_REQUIREMENTS_01.md` ~ `CPF_FINAL_TARGET_REQUIREMENTS_05.md`
- 운영·완료 기준: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 결과·상태 파일:
  - `CPF_STABILIZATION_REPORT.md`
  - `CPF_GAP_MATRIX.md`
  - `CPF_EVIDENCE_INDEX.md`
- 기능·샘플 기준:
  - `specs/기능_구현_매트릭스.html`
  - `specs/sample-coverage-matrix.md`

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 유일한 정본이다. `_01`~`_05`는 대용량 정본 검색을 보조하는 생성 파일이며 직접 목표를 축소하거나 별도 정본처럼 수정하지 않는다. 정본 변경 시에는 분할 파일 전체를 재생성하고 병합 byte/hash 일치 여부를 검증한다.

작업 시작 전에 최신 `master`를 다시 확인한다. 최신 commit이 위 기준 commit에서 바뀌었으면 변경분을 먼저 전수 검수하고 최신 commit을 시작 기준으로 기록한다.

---

## 3. 이번 요청의 배경과 직접 확인된 문제

최신 `master`에는 PFW 공통 파일 로그 writer, 공통 Logback 설정, BAT JobInstance 로그, ADM BAT 로그 조회, FileTransfer 보안 보강, V24 SQL/Flyway, EDU·테스트·qualityGate가 추가됐다. 그러나 다음 문제와 미검증 항목이 남아 있다.

1. 현재 `cpf.logging.file.base-path`는 `CPF_LOG_ROOT` 미설정 시 `./logs`로 fallback한다. 실행 위치에 따라 `<repo>/logs`, `<repo>/acc/logs`, `<repo>/mbr/logs`처럼 중복 root가 재생성될 수 있다.
2. 현재 공통 Logback은 주로 application/error 파일을 만들고, PFW `CpfFileLogWriter`는 `moduleCode + logType + instanceId + date` 기준 파일을 만든다. 사용자가 요구한 온라인 거래 파일 표준인 `일자/거래ID_일자.log` 구조는 완성되지 않았다.
3. 파일 로그는 `transactionGlobalId`별 파일이 아니라 거래 종류를 나타내는 `transactionId`별 일자 파일이어야 한다. `transactionGlobalId`는 파일 내부 각 레코드의 전체 거래 추적 키다.
4. MBR이 ACC를 호출할 때 MBR 거래 파일만 열어도 대상 거래, 선택 인스턴스, 시도별 성공·실패, timeout, retry, failover, 최종 결과와 실패 구간을 판단할 수 있어야 한다. ACC 내부 상세는 ACC 자신의 거래 파일에 남고, DB/ADM에서 동일 `transactionGlobalId`로 전체 흐름을 통합 조회해야 한다.
5. DB 거래 로그는 PFW 소유 capability이며 업무 트랜잭션과 분리돼야 한다. 업무 rollback, 예외, timeout이 발생해도 거래 로그와 segment/timeline이 함께 rollback되거나 유실되면 안 된다.
6. DB 로그 저장 자체가 실패할 때의 durable fallback, 복구 worker, 중복 방지, health/alert가 실제 구현·검증됐는지 확인되지 않았다.
7. ADM에는 `transactionGlobalId` 조회 구조가 있으나 MBR → ACC → EXS 전체 segment tree, selectedInstanceId, retry/failover, 오류 구간, rollback 결과가 최신 DB에서 실제 조회되는지 미검증이다.
8. PFW/CMN은 독립 실행 주체가 아니다. 기술 진단 로그는 실제 실행 주체 아래에 귀속되어야 하며, 거래 관련 PFW/CMN 로그는 해당 거래 파일에도 포함돼야 한다.
9. 여러 서버·여러 인스턴스가 기동될 때 동일 active 파일 동시 쓰기와 rollover 충돌을 방지하는 instance 경로 분리가 필요하다.
10. BAT는 일반 실행 로그와 JobInstance 로그를 분리하고, 같은 JobInstance restart는 같은 논리 파일에 이어지며 다른 JobInstance는 섞이지 않아야 한다. 다중 서버에서는 writer ownership 또는 동등한 안전장치가 필요하다.
11. 테스트를 수행했다면 실제 신규 raw 로그가 로컬에 생성되어야 한다. raw 로그는 최종 Git 추적 대상이 아니어도 되지만, cleanup 전에 신규 생성 파일 manifest, 크기, 수정 시각, 행 수, hash, 필수 필드, 민감정보 검사 결과를 정제 evidence로 남겨야 한다.
12. `CPF_NEW_REQUEST.md`를 수정하지 않았다는 완료 보고와 달리 최신 commit에서 요청서가 대폭 수정됐다. 요청서 보호 gate가 실제 변경을 차단하지 못했다.
13. 첫 번째 push에 실제 작업 파일이 다 포함되지 않아 두 번째 push가 필요했다. 작업 종료 시 변경·미추적 파일 전수 목록과 사용자 commit 대상 manifest가 반드시 필요하다.
14. raw 로그 추적은 정리됐지만 로컬에 남은 `*/logs`, `build/**/logs`, `tmp/**/logs`, 중복 설정, 낡은 가이드·스크립트·evidence·빈 디렉터리·orphan 파일은 다시 전수 정리해야 한다.
15. MariaDB V22·V23·V24, all_install, Flyway, DB rollback 로그 보존, ADM runtime은 미검증이다.
16. Java 25는 현재 Gradle/Spring Boot 조합에서 실패 상태다.
17. 기존 검수에서 지적된 idempotency expired PROCESSING 재시작, broker retry/backoff/DLQ/replay, ADM의 PFW 테이블 직접 JDBC 접근, EDU ownership, 생성기 apply/integration, 외부 protocol·다중 인스턴스 검증도 잔존 여부를 재확인해야 한다.

이번 작업은 문서만 다듬는 작업이 아니다. **로그 저장·경로·다중 인스턴스·DB 내구성·ADM 조회·실제 runtime 검증·저장소 정리를 소스, 테스트, SQL, 설정, EDU, OpenAPI, evidence까지 연결하는 개발 마일스톤**이다.

---

## 4. 작업 원칙

- Codex 보고는 주장일 뿐이며 실제 source/test/SQL/config/evidence가 일치해야 한다.
- 상태값은 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.
- 실행하지 않은 검증은 완료로 기록하지 않는다.
- source만 있거나 unit test만 있으면 전체 기능 완료가 아니다.
- 목표를 현재 구현 수준에 맞춰 축소하거나 미완료 항목을 삭제하지 않는다.
- Git commit, push, branch 생성, force push, rebase, history rewrite를 수행하지 않는다.
- 민감정보·비밀번호·token·Authorization·개인정보 원문을 source, log, evidence, report에 남기지 않는다.
- MariaDB 설치·삭제·업그레이드를 수행하지 않는다.
- 신규 HTML 작성은 지양하고 기존 matrix 정합성에 필요한 최소 수정만 한다.
- PDF를 생성하지 않는다.
- 작업 종료 보고 전에 모든 변경·미추적 파일을 전수 확인하고 사용자에게 commit 대상 manifest를 제공한다.
- 이번 `CPF_NEW_REQUEST.md`는 작업 중 수정하지 않는다.
- 이번에 초기화한 `CPF_STABILIZATION_REPORT.md`는 과거 문구를 복사해 채우지 말고 실제 작업 결과로 갱신한다. 과거 상세 이력은 Git history와 evidence index에 남아 있다.

---

# 5. 필수 완료 범위

## 5.1 요청서 보호와 작업 종료 파일 누락 방지

### 5.1.1 요청서 보호

작업 시작 즉시 코드 수정 전에 다음을 수행한다.

- 시작 commit 기록
- `CPF_NEW_REQUEST.md`의 SHA-256 기록
- `git hash-object CPF_NEW_REQUEST.md` 기록
- 시작 commit에 파일이 존재하면 `git show <START_COMMIT>:CPF_NEW_REQUEST.md`와 현재 파일 byte 비교
- 신규 evidence 디렉터리에 baseline copy와 hash 생성
- baseline 파일을 이후 수정·재생성·덮어쓰기 금지
- qualityGate 실행 중 baseline 자동 초기화 금지
- 작업 종료 시 byte-for-byte 비교

다음은 실패 처리한다.

- 요청서 내용 변경
- baseline이 작업 중 재생성됨
- baseline 작성 시각이 실제 작업 시작 이후로 의심됨
- 요청서 변경 후 새 hash를 정상 기준으로 채택함
- 요청서 변경을 report에 숨김

### 5.1.2 작업 파일 누락 방지

작업 종료 시 다음 명령 결과를 정제 evidence와 report에 포함한다.

```text
git status --short
git diff --name-status
git diff --cached --name-status
git ls-files --others --exclude-standard
git diff --check
```

Codex는 commit하지 않으므로 worktree가 비어 있을 필요는 없다. 대신 다음이 충족되어야 한다.

- 모든 intentional 변경 파일이 manifest에 포함됨
- accidental/unrelated 변경이 없음
- untracked 신규 source/test/SQL/script/evidence가 누락되지 않음
- 사용자에게 한 번에 commit할 정확한 파일 목록 제공
- “작업 완료” 이후 새 변경 파일이 추가로 발견되지 않도록 최종 검사를 마지막 단계에서 재실행

---

## 5.2 `CPF_STABILIZATION_REPORT.md` 초기화 후 실제 결과 재작성

현재 제공된 `CPF_STABILIZATION_REPORT.md`는 작업 시작 전 상태를 담는 초기화본이다.

작업 종료 시 다음 기준으로 전체 갱신한다.

- 이전 report의 오래된 실행 결과를 그대로 복사하지 않음
- 이번 시작 commit과 종료 worktree 기준으로 작성
- `검증 사실 / 직접 실행 결과 / 미실행 / 실패 / 재확인 필요` 구분
- 기존 evidence와 신규 evidence 구분
- 외부 환경이 없어 미실행한 항목은 명시
- 각 완료 항목에 실제 evidence 경로 연결
- report, gap, evidence index, 기능 matrix, sample coverage 상태 일치
- 현재 작업에서 바뀌지 않은 과거 완료 항목은 “기존 evidence 유지”로 표시
- 최종 overall 상태를 과대 선언하지 않음

---

## 5.3 로그 capability ownership 확정

로그 표준 엔진의 소유자는 PFW다.

PFW가 소유할 범위:

- 로그 root 해석
- 경로·파일명 정책
- JSON Lines 구조화
- masking
- transaction context
- transactionGlobalId/segment/timeline
- selectedInstanceId/serverInstanceId/workerInstanceId
- file writer
- DB transaction log writer
- rollback 독립 저장
- durable fallback/recovery
- retention/compression
- context 누락 감지
- health/metric/alert
- ADM query port/facade
- architecture rule

ACC/MBR/EXS/ADM/BAT/BIZADM/XYZ는 실행 주체 또는 consumer/adapter로서 다음 context만 제공한다.

- runtimeModuleCode
- transactionId
- transactionGlobalId
- segmentId/parentSegmentId
- source/target module·transaction
- selectedInstanceId
- business key
- 결과·오류·실패 구간
- 마스킹 대상 metadata

업무 모듈이 자체 로그 엔진, 자체 경로 정책, 자체 DB 거래 로그 저장 방식을 중복 구현하지 않는다.

---

## 5.4 단일 절대 로그 root와 다중 서버 표준

### 5.4.1 필수 외부 설정

다음 값을 표준화한다.

```text
CPF_LOG_ROOT
CPF_ENV
CPF_MODULE_CODE 또는 spring.application.name
CPF_INSTANCE_ID 또는 cpf.framework.was-id
```

`CPF_INSTANCE_ID`는 Service Registry instanceId, DB `serverInstanceId`, health, ADM 인스턴스 조회에서 동일한 값을 사용한다.

### 5.4.2 local

local에서도 단순 `./logs` fallback에 의존하지 않는다.

Gradle, bootJar, smoke, test runtime script가 repository root의 절대경로를 계산해 다음처럼 주입한다.

```text
CPF_LOG_ROOT=<repository-absolute-path>/logs
CPF_ENV=local
```

어느 디렉터리에서 실행해도 같은 root 아래에만 생성되어야 한다.

### 5.4.3 dev/stg/prod

다음을 필수로 한다.

- `CPF_LOG_ROOT` 외부 절대경로
- `CPF_ENV`
- 고유 `CPF_INSTANCE_ID`
- 쓰기 권한 확인

다음은 기동 실패 또는 health `DOWN`으로 처리한다.

- `CPF_LOG_ROOT` 미설정
- 상대경로
- `.` 또는 `..` 기반 경로
- 쓰기 불가
- instanceId 미설정
- 같은 배포 단위에서 instanceId 중복
- root 이탈·symbolic link 우회

### 5.4.4 표준 디렉터리

일반 실행 주체:

```text
${CPF_LOG_ROOT}/{environment}/{runtimeModuleCode}/{instanceId}/
├─ application/
├─ error/
├─ integration/
├─ audit/
├─ security/
├─ worker/
├─ transactions/
├─ framework/
│  └─ pfw/
├─ common/
│  └─ cmn/
└─ recovery/
```

예:

```text
/data/cpf/logs/prod/mbr/mbr-prod-app01/
```

PFW/CMN은 독립 실행 주체가 아니므로 전역 `logs/pfw`, `logs/cmn`을 기본 runtime 경로로 사용하지 않는다.

- MBR 프로세스에서 발생한 PFW 기술 진단 로그 → `.../mbr/<instance>/framework/pfw`
- MBR 프로세스에서 발생한 CMN 기술 진단 로그 → `.../mbr/<instance>/common/cmn`
- 거래와 직접 관련된 PFW/CMN 이벤트 → 해당 거래ID 일자 파일에도 기록
- build/qualityGate 실행 로그 → runtime root가 아니라 정제 evidence로 관리

여러 인스턴스는 서로 다른 active 파일을 사용해야 한다. 공용 파일을 여러 JVM이 무제어로 동시에 append하거나 rollover하지 않는다.

---

## 5.5 온라인 거래ID별 일자 파일 로그

### 5.5.1 표준 경로

```text
${CPF_LOG_ROOT}/{environment}/{runtimeModuleCode}/{instanceId}/transactions/{businessDate}/
{transactionId}_{businessDate}.log
```

예:

```text
/data/cpf/logs/prod/mbr/mbr-prod-app01/transactions/20260713/
MBR_MEMBER_REGISTER_20260713.log
```

### 5.5.2 분리 기준

- 파일 분리 기준은 `transactionId + businessDate + runtime instance`
- `transactionId`는 거래 종류를 나타내는 고정된 업무·서비스 거래 식별자
- 동일 인스턴스에서 같은 날 같은 transactionId로 실행된 여러 건은 같은 파일에 append
- `transactionGlobalId`별 파일 생성 금지
- 모듈 전체 `transaction` 단일 파일만으로 거래별 파일 요구를 완료 처리하지 않음
- transactionId가 다른 거래는 다른 파일
- 일자가 바뀌면 다른 디렉터리·파일
- filename token validation, 길이 제한, path traversal 방지
- active file과 archive file 충돌 방지
- JSON Lines 유지
- size/time rollover가 필요하면 같은 거래ID·일자 기준을 유지하면서 index suffix를 사용할 수 있으나 transactionGlobalId를 filename으로 사용하지 않음

### 5.5.3 필수 레코드

각 레코드 최소 필드:

- timestamp/timezone/businessDate
- environment
- runtimeModuleCode
- instanceId/serverInstanceId
- transactionId
- transactionGlobalId
- segmentId/parentSegmentId
- eventType
- sourceModuleCode/sourceTransactionId
- targetModuleCode/targetTransactionId
- selectedInstanceId
- attemptNo
- retry/failover/circuit 상태
- start/end/durationMs
- status
- resultCode
- errorCode
- failureStage
- request/response/header 마스킹 요약
- traceId/spanId
- host/process/thread는 운영 필요 범위에서만 사용하고 evidence에는 정제

`NO_TRANSACTION` 파일을 정상 거래 저장소로 사용하지 않는다. 거래 context가 필수인 경로에서 transactionId 또는 transactionGlobalId 누락 시 anomaly metric, warning/error, health/ADM 진단, 테스트 실패로 처리한다.

---

## 5.6 상위 거래 파일의 하위 호출 운영 정보

MBR → ACC → EXS 호출을 예로 들면:

- MBR 거래 파일에는 MBR 처리와 ACC 호출의 운영 판단 정보가 남아야 한다.
- ACC 내부 상세 전체를 MBR 파일에 복제하지 않는다.
- ACC 내부 상세는 ACC의 transactionId 일자 파일에 기록한다.
- 두 파일은 동일 transactionGlobalId와 segment 계층으로 연결한다.

상위 거래 파일에서 최소 확인 가능해야 하는 정보:

- target module/transaction
- 호출 방식(Local Facade/Remote Proxy)
- registry 후보와 선택된 instance
- 시도별 instance
- 시도별 시작·종료·소요시간
- 성공/업무 오류/시스템 오류/timeout/unknown
- retry/backoff
- failover
- circuit breaker
- 최종 selectedInstanceId
- 최종 결과
- errorCode/failureStage
- segmentId/parentSegmentId
- reconciliation 필요 여부

필수 시나리오:

1. MBR → ACC 정상
2. ACC 업무 오류
3. ACC 시스템 오류
4. timeout
5. retry 후 성공
6. 첫 instance timeout 후 다른 instance failover 성공
7. 최종 실패
8. unknown result
9. reconciliation 후 성공/실패 확정
10. MBR → ACC → EXS 다단 호출

---

## 5.7 PFW DB 거래 로그의 rollback 독립성·내구성

### 5.7.1 업무 transaction과 분리

DB 거래 로그, segment, timeline 저장은 업무 transaction rollback과 분리한다.

허용 구현 예:

- 별도 Spring bean의 `REQUIRES_NEW`
- 별도 transaction manager
- 독립 durable logging queue/worker

금지:

- 업무 service의 같은 transaction에서 로그 insert
- 같은 클래스 self-invocation으로 transaction proxy 우회
- `finally`에서 같은 transaction으로 저장
- 로그 저장 실패가 원래 업무 오류를 덮어씀
- business rollback과 함께 transaction/segment 로그 rollback

### 5.7.2 단계별 저장

최소 이벤트:

- transaction started
- segment started
- instance selected
- outbound call attempt
- retry/failover
- downstream response
- business commit/rollback
- segment completed/failed/unknown
- final transaction status
- reconciliation/replay

### 5.7.3 DB 로그 저장 실패 fallback

DB 자체 장애·연결 실패 시에도 추적 정보가 최종 유실되지 않도록 PFW durable fallback을 구현·검증한다.

필수:

- 인스턴스별 durable spool/journal 또는 동등한 저장
- 민감정보 masking
- transactionGlobalId + segmentId + eventType + sequence 기반 중복 방지
- 복구 worker
- 재적재 성공 후 안전한 cleanup
- poison record 격리
- retry/backoff
- health/metric/alert
- spool 용량 제한과 디스크 부족 대응
- ADM에서 미적재/복구 상태 조회

fallback을 구현하지 못하면 해당 항목은 `부분 구현`으로 남기고 유실 위험과 대안을 구체적으로 기록한다. 단순 “파일 로그가 있으므로 괜찮음”으로 완료 처리하지 않는다.

### 5.7.4 필수 runtime 검증

- 업무 RuntimeException rollback 후 DB 거래 로그 유지
- checked exception rollback 정책 확인
- timeout 후 DB 로그 유지
- downstream 성공 후 상위 rollback
- 로그 저장 DB 일시 장애 → spool 생성
- DB 복구 → 재적재
- 재적재 중복 방지
- ADM transactionGlobalId 조회 성공

---

## 5.8 DB transactionGlobalId 전체 timeline과 ADM

DB 로그와 ADM이 전체 종속 거래 흐름의 정본이다.

PFW는 transaction timeline query port/facade를 제공한다.

- 동일 JVM: Local Facade
- MSA: Remote Facade Proxy + PFW Service Call Engine

ADM은 PFW 내부 테이블을 임의 `JdbcTemplate`, Repository, Mapper로 직접 조회하지 않는다. 기존 직접 접근이 있으면 query port/facade로 이전한다.

ADM에서 transactionGlobalId 하나로 다음을 조회해야 한다.

- 최초 진입 거래
- 전체 segment tree
- source/target module·transaction
- selectedInstanceId
- 각 attempt와 retry/failover
- circuit 상태
- 시작/종료/소요시간
- 성공/업무 오류/시스템 오류/timeout/unknown/rollback
- 오류 코드와 실패 구간
- reconciliation/replay
- 표준·확장 header 주요 값
- 마스킹된 request/response 요약
- 관련 파일 로그의 runtimeModule/instance/date/transactionId 경로 정보
- BAT JobInstance/JobExecution 연결

ADM 목록·검색·상세 필드는 운영자가 나중에 전체 수정하지 않도록 처음부터 충분히 포함한다.

필수 검증:

- 실제 MariaDB 데이터
- API runtime
- 권한별 200/403
- 수동 작업 사유·before/after·audit
- browser click
- timeline tree 렌더링
- 대량 segment pagination/limit
- masking
- download audit 또는 안전한 로그 위치 조회

---

## 5.9 BAT 로그 표준과 다중 서버

### 5.9.1 일반 BAT 실행 로그

```text
${CPF_LOG_ROOT}/{environment}/bat/{instanceId}/
├─ application/
├─ error/
├─ worker/
├─ framework/pfw/
└─ common/cmn/
```

### 5.9.2 JobInstance 로그

표준 논리 경로:

```text
${CPF_LOG_ROOT}/{environment}/bat/jobs/{businessDate}/{jobName}/
cpf-bat-{jobName}-{jobInstanceId}-{businessDate}.log
```

규칙:

- 새 JobInstance는 새 파일
- 같은 JobInstance 최초 실행·실패·restart는 같은 논리 파일
- 실행 회차는 jobExecutionId/restartAttempt/rerunId로 구분
- JobInstance 시작 시 businessDate 확정
- 자정 이후 종료해도 시작 businessDate 유지
- serverInstanceId/workerInstanceId는 레코드 필드이며 기본 분리 키가 아님
- 다른 JobInstance 혼합 금지
- JSON Lines
- 파일과 DB execution metadata 상호 추적

### 5.9.3 다중 서버 안전성

여러 BAT 서버가 같은 JobInstance 파일을 무제어 동시 append하면 안 된다.

표준 모드:

- 공유 durable storage
- DB lease 또는 동등한 single-writer ownership
- lease 만료/이관
- restart 시 새 worker가 동일 논리 파일 이어쓰기
- stale writer 차단
- 원자적 append 또는 안전한 journal

공유 storage가 없는 환경의 명시적 degraded 모드가 필요하면:

- instance fragment 파일
- DB/ADM에서 JobInstance 기준 통합 조회
- 물리적으로 같은 파일이 아님을 명확히 표시
- 표준 same-file 완료로 과대 기록 금지

필수 검증:

1. 같은 jobName의 서로 다른 JobInstance 두 개
2. 동일 JobInstance 실패 후 restart
3. 다른 server/worker가 restart
4. businessDate 고정
5. 자정 경계
6. lease 충돌
7. stale writer 차단
8. 파일/DB/ADM 상호 추적
9. 민감정보 masking
10. 오류·rollback·unknown·compensation

---

## 5.10 실제 신규 로그 생성 evidence와 최종 cleanup

raw runtime 로그는 개발·검증 중 로컬에서 생성·보존할 수 있다. 테스트 전에 기존 로그를 정리해 새 생성 여부를 확인할 수 있으나, 테스트 후 evidence를 남기기 전에 전부 삭제하지 않는다.

검증 후 다음 manifest를 정제 evidence로 남긴다.

- 실행 command
- start commit
- profile
- CPF_LOG_ROOT 마스킹값
- 생성 상대경로
- 생성 시각/수정 시각
- 파일 크기
- 행 수
- SHA-256
- JSON Lines 파싱 결과
- 필수 필드 결과
- transactionId/transactionGlobalId 예시 마스킹값
- secret/PII scan
- 테스트 시나리오 연결
- cleanup 전/후 목록

Git 최종 상태:

- 불필요 raw runtime 로그는 추적하지 않음
- 승인된 `specs/evidence/**/*.sanitized.log|json`만 추적
- stale/duplicate/raw evidence 제거
- GitHub에 raw 로그가 없더라도 실제 생성 검증을 sanitized manifest로 확인 가능

로그 분류를 다음으로 남긴다.

- 유지
- 정제 후 유지
- 임시 유지
- 삭제
- 재확인 필요

---

## 5.11 비표준 로그 폴더와 저장소 전수 정리

### 5.11.1 비표준 로그 폴더

다음을 전수 탐색한다.

```text
*/logs/
build/**/logs/
tmp/**/logs/
out/**/logs/
generated/**/logs/
candidate/**/logs/
```

표준 local root 외의 로그 폴더는 참조·실행 경로를 확인한 뒤 삭제한다.

표준 local root:

```text
<repository-root>/logs/{environment}/{runtimeModuleCode}/{instanceId}/...
```

BAT JobInstance 예외:

```text
<repository-root>/logs/{environment}/bat/jobs/...
```

다음은 runtime 표준 경로로 사용하지 않는다.

```text
acc/logs
mbr/logs
adm/logs
bat/logs
xyz/logs
pfw/logs
cmn/logs
```

### 5.11.2 문서·스크립트·가비지

전수 검수 대상:

- 오래되어 현재 기준과 충돌하는 가이드
- 중복 README·설계 문서
- 임시 작업 메모
- abandoned design
- 불필요 HTML/PDF
- backup/temp 파일
- 실패한 생성기 잔존물
- candidate patch
- build/out/tmp
- IDE 개인 파일
- 사용되지 않는 script
- 같은 목적의 중복 smoke/check script
- stale/duplicate evidence
- 빈 디렉터리
- orphan source/test/SQL/Flyway/yml/config
- 삭제된 기능의 잔존 package/config/docs
- 모듈별 중복 logback XML
- 현재 공통 설정과 충돌하는 환경 설정

삭제 전에 다음을 확인한다.

- `git ls-files`
- source import/reference
- Gradle/settings
- script 호출
- SQL/Flyway/all_install
- README/guide
- report/gap/evidence/matrix
- runtime profile
- 생성기 template

파일별 분류:

- 유지
- 통합
- 정제 후 유지
- 삭제
- 재확인 필요

정본 요구사항 파일, 운영 가이드, report/gap/evidence/matrix를 가비지로 오인해 삭제하지 않는다. `_01`~`_05` 분할 파일도 검색 보조로 유지한다.

cleanup 후 compile/test/bootJar/qualityGate와 `git diff --check`를 재실행한다.

---

## 5.12 SQL/Flyway/MariaDB 실검증

현재 셸에서 기설치 MariaDB 접근 가능 여부를 비파괴 preflight로 확인한다.

접속 가능하면 신규 빈 검증 schema에서 실행:

- split SQL
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- Flyway baseline
- V22
- V23
- V24
- 이전 migration 상태 → 최신 upgrade
- 재실행 정책
- PK/UK/index/default/NOT NULL
- transaction/segment/log tables
- BAT linkage
- seed
- cleanup

특히 검증:

- 업무 rollback 후 거래 로그 유지
- segment parent-child
- selectedInstanceId
- retry/failover attempt
- ADM query
- durable fallback recovery metadata
- BAT JobInstance linkage

접속 불가면 MariaDB를 설치하거나 비밀번호를 추측하지 않는다. 필요한 환경변수, preflight 결과, 재현 명령을 정제 evidence에 남기고 `미검증` 처리한다.

---

## 5.13 기존 Reliability gap 재확인과 잔존 시 보완

### Idempotency

- expired PROCESSING이 실제 재시작 가능한지
- cleanup 선행 없이 engine/repository 상태 전이가 일치하는지
- 같은 key/같은 payload replay
- 같은 key/다른 payload
- concurrent unique race
- FAILED/UNKNOWN/EXPIRED retry
- 재기동 영속성
- REST/Broker/FileTransfer/BAT 실제 연결

### Broker

- consumer 실패 즉시 DLQ가 아니라 retry/backoff/최대 시도 정책
- inbox 상태가 replay를 막지 않는지
- DLQ replay가 상태 변경만 하고 실제 재처리하지 않는 문제
- publisher/consumer worker lease
- DB restart
- 다중 worker
- real broker 미검증 구분

### FileTransfer

기존 보강을 회귀 검증:

- allowed base
- path traversal
- symlink escape
- unique temp file
- checksum
- 실패 cleanup
- atomic rename
- overwrite
- duplicate reservation
- restart
- multi-instance

SFTP/FTP/SCP/SSH는 실제 구현·runtime을 분리해 기록한다.

### ADM boundary

- ADM의 PFW table 직접 JDBC/Repository/Mapper 접근 제거
- PFW query/command port/facade 사용
- replay/manual resolve가 단순 status update에 그치지 않음
- request user spoofing 방지
- 권한·사유
- 상태 변경과 audit 원자성
- masking/download audit
- OpenAPI

---

## 5.14 EDU, OpenAPI, 생성기, Java 목표

### EDU ownership

- 일반·온라인·대외연계 EDU: `xyz/.../edu`
- 배치·센터컷 EDU: `bat/.../edu`
- PFW/CMN: engine/port/reference adapter + unit/contract test
- ACC/MBR/EXS/ADM/BIZADM: 범용 개발자 EDU 미소유

PFW/CMN/EXS/ADM/BIZADM의 기존 범용 `EducationSample` 잔존을 전수 확인하고 호환성 영향 분석 후 이전·정리한다.

### OpenAPI

모든 REST API:

- `@Tag`
- `@Operation`
- 고유 `operationId`
- request/response schema
- 표준 오류 response
- 필수 header
- transactionGlobalId/header 설명
- 권한·마스킹·감사 설명

소스 coverage뿐 아니라 실제 `/v3/api-docs` JSON을 저장해 schema/header/error/operationId를 검사한다.

### 생성기

`create-domain` 계열:

- 프로젝트 Java 목표 사용
- 특정 업무 모듈 비종속
- source/SQL/Flyway/yml/test/smoke/OpenAPI/ADM/registry/deploy 연결
- candidate-only가 아닌 apply 경계 명확화
- 생성 모듈 compile/test/bootJar
- ownership scan
- 로그 절대 root·instance 표준 포함
- 범용 EDU 복사 금지
- 생성 결과 cleanup

### Java 25

Java 25 목표를 삭제하지 않는다.

- 현재 Gradle 8.11.1/Spring Boot 3.4 조합 실패 원인 재확인
- 공식 지원 조합 기준 업그레이드 계획
- 합리적으로 가능한 범위에서 Gradle/Spring Boot/plugin 호환 보강
- compile/test/bootJar/qualityGate
- 대표 runtime
- 실패 시 정확한 원인·영향·대안

Java 21 성공으로 Java 25 완료 처리하지 않는다.

---

# 6. 보강 범위

필수 범위를 해치지 않고 재작업을 줄일 수 있으면 함께 보강한다.

- 로그 파일 size/time rotation과 archive index 규칙
- retention/total size cap/압축
- log disk 사용량 metric
- spool disk pressure
- central log shipper 연계 port
- traceId와 transactionGlobalId 연결
- ADM file log 경로 안전 조회
- trace boost와 transactionId 파일 연결
- batch compensation/unknown/reconciliation
- worker pause/resume/drain
- graceful shutdown
- rolling deploy 중 writer ownership 이전
- API/event/schema versioning
- evidence stale commit 검출
- dependency/license/vulnerability/secret scan

---

# 7. 착수 범위

필수 완료 범위가 안정적으로 닫힌 뒤 남는 작업량에서 착수한다.

- real Kafka/MQ adapter runtime
- SFTP/FTP/SCP/SSH test server harness
- 다중 WAS MBR → ACC failover 자동화
- 공유 storage 기반 BAT cross-instance restart
- Vault/KMS credential runtime
- OpenTelemetry 연계
- 성능/부하/soak/spike
- 장애복구/DR
- SLI/SLO p95/p99

착수만 한 항목은 완료로 기록하지 않는다.

---

# 8. 후순위·제외 범위

- 실제 운영 Redis/Kafka/MQ 접속
- 실제 운영 SFTP/FTP/SCP/SSH 접속
- 실제 운영 Vault/KMS/HSM
- 운영 서버 강제 설정 변경
- MariaDB 설치·삭제·업그레이드
- force push/history rewrite
- 전체 최종 HTML 정본화
- 최종 PDF 생성
- CPF 전체 최종 완료 선언

환경이 없는 항목은 삭제하지 않고 `미검증` 또는 `재확인 필요`로 유지한다.

---

# 9. 필수 실행 시나리오와 evidence

최소 실행·증적:

1. 시작 commit/request hash 보호
2. 전체 변경·미추적 파일 manifest
3. local repository 절대 로그 root
4. 서로 다른 working directory에서 동일 root 생성
5. 같은 모듈 인스턴스 2개 동시 기동과 파일 분리
6. 일반 application/error/framework/common 로그 생성
7. 거래ID별 일자 파일 생성
8. 같은 transactionId, 다른 transactionGlobalId 두 건이 같은 파일
9. 다른 transactionId가 다른 파일
10. `NO_TRANSACTION` 미생성 및 context 누락 탐지
11. MBR → ACC 정상
12. 업무 오류
13. 시스템 오류
14. timeout
15. retry
16. failover
17. unknown/reconciliation
18. MBR → ACC → EXS segment tree
19. 업무 rollback 후 DB 로그 유지
20. DB 로그 저장 장애 fallback/recovery
21. ADM transactionGlobalId 통합 조회
22. ADM 권한/감사/browser
23. BAT JobInstance 분리
24. BAT restart 동일 논리 파일
25. BAT cross-instance writer ownership
26. FileTransfer 보안 회귀
27. MariaDB full install/Flyway
28. OpenAPI runtime JSON
29. EDU ownership/sample coverage
30. 생성기 compile/test/bootJar
31. Java 25
32. garbage/empty/orphan/stale evidence scan
33. compile/test/bootJar/qualityGate
34. `git diff --check`
35. 최종 사용자 commit 대상 manifest

Evidence는 신규 non-overwrite 디렉터리에 저장한다.

각 evidence 최소 메타:

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

실제 raw 로그가 Git에 없더라도 신규 생성 manifest와 hash로 runtime 생성 사실을 확인 가능하게 한다.

---

# 10. 문서·상태 정합성

작업 종료 시 실제 상태로 갱신:

- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- `specs/기능_구현_매트릭스.html`
- `specs/sample-coverage-matrix.md`
- 관련 README/가이드 최소 범위

qualityGate가 실패시켜야 하는 항목:

- 요청서 변경
- 없는 evidence
- Git에 없는 evidence를 완료 근거로 사용
- stale/different commit evidence
- 비허용 상태값
- SKIPPED 완료 처리
- CoverageCatalog-only 완료
- source/test/EDU/OpenAPI/evidence 묶음 누락
- 상대 로그 root
- 중복 로그 root
- transactionGlobalId filename
- 거래ID 일자 파일 미생성
- context 누락 정상 처리
- 업무 rollback과 DB 로그 동반 rollback
- ADM 직접 PFW DB 접근
- 불필요 raw 로그 추적
- garbage/orphan/empty directory
- 최종 changed/untracked manifest 누락

---

# 11. 결과 보고 기준

최종 보고는 다음 순서로 작성한다.

1. 기준 commit과 요청서 hash
2. 직접 변경한 source/test/SQL/config/script 목록
3. 직접 실행한 명령
4. 성공 결과
5. 실패 결과
6. 미실행·환경 부재
7. 로그 경로 표준 적용 결과
8. 신규 raw 로그 생성·정제·cleanup 결과
9. DB rollback/fallback 결과
10. ADM 전체 timeline 결과
11. BAT 다중 서버 결과
12. 파일·문서·스크립트 cleanup 결과
13. report/gap/evidence/matrix 정합성
14. 남은 gap
15. 사용자 commit 대상 전체 manifest

“완료”라는 문구보다 evidence와 실제 상태가 우선한다.

---

# 12. 작업 금지사항

- Git commit 금지
- Git push 금지
- branch 생성 금지
- force push 금지
- rebase/history rewrite 금지
- `CPF_NEW_REQUEST.md` 수정 금지
- baseline 재초기화 금지
- 민감정보 원문 기록 금지
- 실행하지 않은 검증 완료 처리 금지
- 목표 축소 금지
- 문서량 채우기 금지
- 불필요 신규 HTML 금지
- PDF 생성 금지
- PFW capability를 업무 모듈 소유로 이동 금지
- 타 주제영역 DB/Repository/Mapper 직접 접근 금지
- URL 직접 조합/Controller 직접 호출 금지
- 핵심 거래를 Spring Event만으로 처리 금지
- 불필요한 대량 삭제 금지
- 검수 없이 canonical requirement/evidence 삭제 금지

---

# 13. 완료 판단 핵심

이번 마일스톤은 다음이 실제로 확인되어야 완료 후보가 된다.

1. 요청서가 byte 단위로 보존된다.
2. 최종 변경·미추적 파일 전체가 manifest에 포함돼 두 번째 누락 push가 발생하지 않는다.
3. local/dev/stg/prod 로그 root가 절대경로 정책으로 통일된다.
4. 여러 서버·인스턴스가 서로의 active 파일을 충돌 없이 분리한다.
5. PFW/CMN 로그가 실행 주체에 귀속되고 거래 관련 이벤트는 거래 파일에 연결된다.
6. 온라인 거래 파일이 `transactionId + businessDate` 기준으로 생성된다.
7. transactionGlobalId는 파일명이 아니라 각 레코드와 DB timeline의 전체 추적 키다.
8. 상위 거래 파일에서 하위 호출의 인스턴스·시도·결과·실패 구간을 판단할 수 있다.
9. 업무 rollback에도 PFW DB 거래 로그가 유지된다.
10. DB 로그 저장 장애 시 durable fallback과 복구가 동작한다.
11. ADM에서 transactionGlobalId 하나로 전체 segment tree를 조회한다.
12. BAT JobInstance 로그가 restart·다중 서버 기준을 충족한다.
13. 실제 신규 runtime 로그 생성 evidence가 있고 cleanup 전후가 추적된다.
14. 비표준 로그 폴더, 낡은 문서·스크립트, garbage/orphan/stale evidence가 정리된다.
15. MariaDB/Flyway/OpenAPI/browser/Java 25는 실제 실행 결과 또는 정확한 미검증·실패 상태를 가진다.
16. report/gap/evidence/matrix/sample coverage가 실제 상태와 일치한다.

실행하지 못한 항목은 완료가 아니다. 실패 원인을 요구사항/설계/구현/계층/SQL/설정/profile/테스트/환경/DB/broker/browser/evidence/ownership/Spring Event/OpenAPI/EDU로 분류하고 같은 실패 방식을 반복하지 말고 대안을 제시한다.
