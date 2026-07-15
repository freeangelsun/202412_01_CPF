# CPF 최종 통합 구현·전수 검증·문서 정본화 완료 요청서

## 1. 작업 성격

이번 작업은 작은 마일스톤이나 부분 보강 작업이 아니다.

**CPF 정본 목표에 남아 있는 모든 구현·연결·검증·문서·운영·샘플·생성기·정리 작업을 한 번의 작업에서 최대한 완료하여, 이후에는 외부 운영환경 최종 확인과 소수 결함 수정만 남기는 최종 통합 완료 후보 작업**이다.

작업 시간이 오래 걸려도 된다. 작업량이 많다는 이유로 범위를 축소하거나 다음 요청으로 임의 이월하지 않는다. Codex가 접근 가능한 source, test, SQL, script, local runtime, deterministic harness, DOCX, evidence, quality gate 작업은 이번 작업에서 모두 수행한다.

## 2. 저장소와 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 본 요청서 작성 기준 최신 commit: `d16cd7a40062a1e77bd8cd3c6f6f7125cdc0708d`
- 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검색 보조: `CPF_FINAL_TARGET_REQUIREMENTS_01.md` ~ `CPF_FINAL_TARGET_REQUIREMENTS_05.md`
- 운영·완료 판정 기준: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 결과·상태:
  - `CPF_STABILIZATION_REPORT.md`
  - `CPF_GAP_MATRIX.md`
  - `CPF_EVIDENCE_INDEX.md`
- 샘플 기준: `specs/sample-coverage-matrix.md`
- 현재 기능 matrix: `specs/기능_구현_매트릭스.html`
  - 이번 작업에서 기계 검증 가능한 Markdown/JSON과 사용자용 DOCX로 이전하고 HTML은 최종 삭제한다.

작업 시작 전에 최신 `master`를 다시 확인한다. 최신 commit이 위 기준과 다르면 그 이후 commit을 먼저 검수하고 실제 시작 commit을 report, evidence, handoff에 기록한다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 유일한 최상위 목표 정본이다. `_01`~`_05`는 검색을 위한 분할 보조파일이며 목표 축소 근거가 아니다.

## 3. 최종 목표

CPF는 금융권을 포함한 범용 업무 시스템을 구축·운영·감사·확장·검증·상용화할 수 있는 상용 솔루션급 Core Business Platform Framework다.

최종 목표에 반드시 포함한다.

- MSA-first, modular-monolith 호환
- 동일 서비스 다중 인스턴스
- Local Facade / Remote Facade Proxy
- Service Call Engine
- registry, health, instance selection
- selectedInstanceId
- timeout, retry, backoff, failover, circuit breaker
- transactionGlobalId, transactionId, segment, parent-child, timeline
- 표준·확장 header propagation
- 파일 로그와 DB 로그
- 업무 rollback 독립 로그 저장
- durable fallback, recovery, poison, reconciliation
- security, authentication, token, permission, menu/button/API permission
- audit, before/after, masking, download audit
- idempotency
- outbox/inbox
- broker publish/consume, retry, DLQ, actual replay
- unknown result, callback, polling, manual resolve, compensation
- file transfer
- SFTP/FTP/FTPS/SCP/SSH 표준 port와 adapter
- archive/compression
- scheduler, worker, Spring Batch, center-cut
- ADM framework operations
- BIZADM business operations
- SQL, Flyway, all_install, all_install_and_smoke, smoke
- Java 25
- bootJar + embedded Tomcat + `java -jar`
- local/dev/stg/prod
- 기능별 EDU
- Swagger/OpenAPI
- 주제영역 생성기
- evidence 기반 완료 판정
- 상세 사용자·개발자·운영자 DOCX 가이드
- 다음 Codex 세션을 위한 완전한 인수인계 문서

요청서에 이름이 직접 적히지 않았더라도 정본, public port, engine, Controller, SQL, ADM, 생성기, matrix, source에 존재하는 공개 기능은 자동으로 찾아 이번 범위에 포함한다.

## 4. Codex 작업 판단 원칙

본 요청서는 목표, ownership, 완료 기준, 금지사항을 정의한다. 클래스명, package 분할, 테이블 세부 구조, worker 알고리즘, 테스트 harness 등 세부 구현 방식은 절대 고정안이 아니다.

Codex가 더 안전하고 확장 가능하며 정본 목표에 부합하는 대안을 발견하면 적용할 수 있다.

다만 반드시 다음을 지킨다.

- 목표 범위 축소 금지
- 미구현 요구 삭제 금지
- 완료 기준 완화 금지
- 구현 편의를 위한 ownership·계층 위반 금지
- 요청 방식과 다르게 처리한 경우 report와 handoff에 다음을 기록:
  - 변경 이유
  - 선택한 대안
  - 장점
  - 단점과 위험
  - 영향 파일
  - DB migration·API·호환성 영향
  - 남은 gap
  - 후속 검증
- 같은 방법이 반복 실패하면 같은 시도만 반복하지 말고 원인을 분류하고 대안을 적용
- 외부 환경 부재는 source·test·local harness·EDU·OpenAPI·SQL 개발을 미루는 사유가 아님
- 실제 외부환경 실행만 불가능한 경우에 한해 `미검증`
- “시간 부족”, “작업량이 큼”, “다음 요청에서 처리”만으로 미처리 금지
- Codex 환경에서 해결 가능한 실패가 있으면 최종 보고서 작성 전에 수정·재실행

## 5. 상태값과 완료 판정

허용 상태값:

- `완료`
- `부분 구현`
- `미구현`
- `미검증`
- `실패`
- `재확인 필요`

기능별 완료 묶음:

```text
정본 요구
+ 설계·ownership
+ source 구현
+ 계층 연결
+ SQL/Flyway/all_install
+ unit test
+ contract test
+ integration test
+ XYZ/BAT EDU
+ EDU test
+ REST이면 OpenAPI
+ 필요한 runtime
+ 파일/DB/ADM evidence
+ sample coverage
+ 기능 matrix
+ report/gap/evidence 정합성
```

다음은 완료 근거가 아니다.

- class 존재
- source만 구현
- test 한 개 통과
- mock만 사용
- `CoverageCatalog`
- sampleId만 등록
- Map/List 기반 가짜 EDU
- PFW 내부 샘플 wrapper
- Swagger annotation만 존재
- `/v3/api-docs` 미검증
- DB 기능인데 실제 DB row 없음
- replay 상태만 변경하고 실제 재처리 없음
- 과거 commit의 stale evidence
- skip된 테스트
- Java 25에서 실행했지만 다른 버전 bytecode를 생성한 결과

# 6. 작업 시작 보호와 전수 inventory

## 6.1 요청서 보호

작업 시작 즉시:

- 시작 HEAD
- branch
- `git status --short`
- 요청서 SHA-256
- 요청서 git blob
- 시작 commit의 요청서 blob
- baseline copy
- baseline metadata
- 신규 non-overwrite evidence 디렉터리

를 기록한다.

`CPF_NEW_REQUEST.md`는 작업 중 수정하지 않는다. baseline을 재생성·덮어쓰기하지 않는다.

## 6.2 전수 inventory

코드 수정 전에 다음을 전수 inventory로 만든다.

- 모든 Gradle module
- 모든 package
- 모든 public port/interface
- 모든 engine/facade/adapter
- 모든 Controller와 mapping
- 모든 service/repository/mapper
- 모든 SQL table/index/constraint/sequence
- 모든 Flyway migration
- 모든 `application*.yml`
- 모든 deploy env/inventory
- 모든 script
- 모든 EDU class/test
- 모든 ADM menu/page/API
- 모든 가이드·README·HTML·MD·DOCX
- 모든 evidence directory
- 모든 root file/folder
- 모든 생성기 template
- 모든 runtime app
- 모든 외부 연계 port
- 모든 기술 기능과 업무 reference 기능

각 항목을 기능 capability와 연결한다.

```text
capability
→ owner
→ source
→ configuration
→ SQL
→ API
→ EDU
→ test
→ runtime
→ evidence
→ guide
→ status
```

요청서에 빠진 기능을 inventory에서 발견하면 자동으로 작업 범위에 추가한다.

# 7. 아키텍처·표준 설계 전수 정비

## 7.1 ownership

### PFW

PFW가 소유:

- Service Call Engine
- HTTP client
- timeout/retry/backoff/circuit/failover
- registry/health
- transactionGlobalId/transactionId/segment/timeline/header
- logging, recovery
- 기본 security/audit/masking
- idempotency
- outbox/inbox/broker
- unknown/reconciliation
- file transfer
- archive/compression
- credential/secret provider port
- runtime worker
- architecture rules

### CMN

CMN이 소유:

- 프로젝트 공통 code/message
- 프로젝트 오류·상태 확장
- fixed-length layout/parser/formatter
- validation/converter/helper
- 업무 공통 fixture와 규칙

CMN은 기술 engine을 소유하지 않는다.

### 업무/실행 모듈

- MBR: 대표 진입·복합 거래
- ACC: 내부 업무 reference domain
- EXS: 외부연계 reference domain
- BAT: 배치·center-cut 실행과 EDU
- BIZADM: 업무 운영
- ADM: framework 운영
- XYZ: 일반·온라인 EDU

금지:

- PFW → 업무 주제영역 의존
- CMN → 업무 구현체 의존
- 업무 주제영역 간 Repository/Mapper 접근
- Controller 직접 호출
- URL 직접 조합
- EXS 등 업무 module의 기술 engine 소유
- 신규 module의 ACC/MBR/EXS 의존
- ADM의 PFW 내부 worker/repository/table 직접 접근
- 핵심 거래를 Spring Event만으로 처리

기존 CMN migration candidate를 최종 정리한다. 호환 facade가 필요하면 CMN에는 얇은 compatibility facade만 두고 실행 engine·adapter는 PFW로 이전한다.

## 7.2 계층·package·네이밍 표준

전체 source를 전수 검사하고 정비한다.

- package naming
- class/interface/record/enum naming
- Controller/Service/Facade/Port/Adapter/Repository/Mapper suffix
- DTO request/response/search/page naming
- configuration/properties naming
- exception/error code naming
- event/command/query naming
- test naming
- EDU naming
- operationId naming
- transactionId naming
- moduleCode
- profile
- environment variable
- log category
- SQL object naming
- Flyway filename
- script filename
- evidence id

자동 생성된 의미 없는 설명과 이름을 제거한다.

금지 예:

- `정보 조회`
- `요청 처리`
- `Sample1`
- `TestController`
- `CommonUtil`
- 의미 없는 `Map<String,Object>`
- package와 역할 불일치
- 동일 capability의 상이한 용어 혼용

## 7.3 DB 표준

전체 SQL/Flyway/MyBatis/JPA/JDBC를 전수 검사한다.

필수 표준:

- table prefix와 module ownership
- table/column/index/constraint/sequence 이름
- PK/UK/FK
- NOT NULL/default
- timestamp/timezone
- status/code
- version
- created/updated user/time
- soft delete 사용 기준
- business key
- transactionGlobalId/segmentId
- audit
- masking metadata
- comment
- charset/collation
- bigint/varchar/decimal/date/time 타입
- JSON 사용 기준
- reserved word 회피
- index prefix 길이
- multi-instance claim/lease 컬럼
- retry/nextAttemptAt
- poison/recovery
- idempotency key/hash
- outbox/inbox/DLQ
- download audit
- file transfer checksum/history
- batch JobInstance/Execution correlation

다음을 동기화한다.

- split SQL
- Flyway
- V1 baseline
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- seed
- 생성기 SQL template
- 문서
- smoke

신규 SQL 변경 후 과거 evidence만으로 완료 처리하지 않는다.

# 8. Java 25 단일 기준 전환

CPF의 유일한 Java 기준은 **Java 25 LTS**다.

다른 Java 버전을 지원 대상, fallback, 기본값, 배포 bytecode, 완료 근거, 문서 병기 대상으로 유지하지 않는다.

필수:

- Gradle Wrapper Java 25 공식 지원 버전
- Gradle wrapper jar/scripts/properties 정합성
- `JAVA_HOME` 개인 경로를 저장소에 hardcode하지 않음
- `.vscode/settings.json`의 특정 PC 드라이브/JDK 경로 제거
- Java toolchain 25
- compile release 25
- source/target 25
- bootJar Application class major 69
- 모든 module class major 검사
- generated domain class major 69
- Java 25 compile
- Java 25 test
- Java 25 7개 bootJar
- Java 25 qualityGate
- Java 25 representative runtime
- README/DOCX/report/matrix/evidence 모두 Java 25만 기술
- 구버전 이름을 가진 현재 evidence는 작업 이력 보존이 꼭 필요한 최소 파일만 historical/obsolete로 분류하고 현재 완료 근거에서 제외
- current evidence 파일명에도 Java 25 단일 기준 반영
- qualityGate가 class major 69가 아니면 실패

Lombok, Byte Buddy, agent, Spring Boot, MyBatis, test library, Gradle plugin 등 Java 25 compatibility를 검증한다.

경고를 단순 무시하지 않는다.

- native access
- Unsafe
- illegal reflective access
- deprecated Gradle API
- agent dynamic loading
- plugin incompatibility

현재 안전하게 제거 가능한 경고는 제거한다. 라이브러리 upstream 제약이면 원인, 영향, 대안, 추적 버전을 report와 handoff에 기록한다.

# 9. MSA·Service Call·복합 거래 최종 완성

대표 흐름:

```text
MBR → ACC → EXS
```

필수:

- same JVM Local Facade
- separate process Remote Facade Proxy
- public typed contract
- registry persistence
- health probe
- candidate filtering
- instance selection
- selectedInstanceId
- timeout
- retry
- exponential/fixed backoff 정책
- retryable/non-retryable 분류
- circuit open/half-open/closed
- failover
- attempt별 transaction segment
- caller summary log
- callee own log
- transactionGlobalId
- source/target transactionId
- standard/extension headers
- request/response masking summary
- final status
- unknown
- reconciliation
- graceful shutdown
- rolling restart
- multi-instance competition

필수 runtime:

1. 정상
2. business error
3. validation error
4. system error
5. timeout
6. retry success
7. retry exhausted
8. first instance down → second success
9. health down instance 제외
10. circuit open
11. half-open probe
12. all candidates fail
13. unknown
14. reconciliation success/failure
15. caller rollback after callee success
16. two ACC instances
17. two EXS instances
18. multi-process header propagation
19. file log correlation
20. DB timeline
21. ADM integrated query

# 10. transaction·segment·timeline·로그·recovery

## 10.1 추적 모델

전체 온라인·batch·broker·file transfer에 공통 적용:

- transactionId
- transactionGlobalId
- segmentId
- parentSegmentId
- eventId
- sequence
- attemptNo
- selectedInstanceId
- serverInstanceId
- workerInstanceId
- sourceModule/transaction
- targetModule/transaction
- start/end/duration
- status/result/error/failureStage
- retry/failover/circuit
- traceId/spanId
- business key
- reconciliation key

## 10.2 파일 로그

- 절대 log root
- local은 repository 기반 절대경로 계산
- dev/stg/prod는 외부 절대경로 필수
- OS 특정 path hardcode 금지
- module/instance 분리
- transactionId + businessDate file
- transactionGlobalId는 record key
- JSON Lines
- masking
- gzip/retention/size cap
- late write
- path traversal/symlink 방지
- PFW/CMN 로그는 실행 module 귀속
- logger additivity 중복 여부 실검증
- multi-instance active file 충돌 방지
- disk pressure/health/metric

## 10.3 DB 로그와 fallback

업무 rollback과 독립:

- `REQUIRES_NEW` 또는 동등한 독립 저장
- transaction/segment/timeline 전체 durable recovery
- DB 장애 시 journal
- atomic temp
- stale temp cleanup
- pending/processing/poison
- claim/lease
- eligible ordering
- future retry file이 quota를 소비하지 않음
- claim 후 read 실패 복원
- stale processing reclaim
- process restart
- concurrent worker
- duplicate event
- partial recovery
- parent-child ordering
- retry/backoff
- spool capacity
- disk pressure
- masking
- DB unique race
- recovery success cleanup
- poison manual retry
- ADM status/command
- 권한·감사 사유

필수 실제 DB 시나리오:

- business rollback 후 log 유지
- segment start/end 유지
- DB down → journal
- DB up → recovery
- duplicate replay
- partial failure
- worker restart
- two workers
- poison
- ADM tree reconstruction

# 11. Reliability 기능 최종 완성

## 11.1 Idempotency

- REST
- Service Call
- Broker
- FileTransfer
- BAT/center-cut

시나리오:

- same key/same payload
- same key/different payload
- concurrent race
- PROCESSING timeout
- restart
- FAILED retry
- UNKNOWN retry
- EXPIRED
- final response replay
- payload hash
- MariaDB unique race
- audit/ADM

## 11.2 Outbox/Inbox/Broker

- business transaction + outbox atomicity
- publisher claim/lease
- stale reclaim
- retry/backoff/max attempts
- broker publish
- consumer
- inbox duplicate
- consumer rollback
- DLQ
- poison
- actual replay
- replay idempotency
- multi-worker
- restart
- transactionGlobalId/segment
- ADM list/detail/replay
- permission/audit

real Kafka/MQ가 없으면 production port·adapter와 local/test-container/deterministic harness까지 완성한다. 설치가 불가능하면 real runtime만 `미검증`.

## 11.3 Unknown/Reconciliation/Compensation

- timeout unknown
- external key
- callback
- duplicate callback
- polling
- manual resolve
- final success/failure
- compensation
- replay와 reconciliation 경쟁
- optimistic lock
- audit reason
- before/after
- ADM
- EDU

# 12. Security·ADM·BIZADM 최종 완성

## 12.1 인증·권한

- login
- token
- expiration/refresh/logout
- password hash
- account lock
- password policy
- password history
- failed attempts
- session/token invalidation
- menu/button/API permission
- server-side enforcement
- module/role/user mapping
- spoofing 방지
- CSRF/CORS 적용 기준
- audit

## 12.2 ADM 초기 접속과 비밀번호 변경

초기 접속을 사용자가 알 수 있도록 적절한 운영자 DOCX 가이드와 README에 안내한다.

단, 보안상 고정 평문 비밀번호를 source, SQL, Git, README, DOCX에 직접 넣지 않는다.

표준 bootstrap 방식:

- 기본 관리자 ID 또는 생성 규칙은 문서화 가능
- 초기 비밀번호는 환경변수/secret/one-time bootstrap으로 주입
- 값은 문서에 기록하지 않고 설정 변수명과 생성·확인 절차만 안내
- 최초 로그인 시 강제 비밀번호 변경
- UI에서 비밀번호 변경 가능
- 현재 비밀번호 확인
- 새 비밀번호 정책 검증
- 확인 입력
- password history
- 변경 audit
- token/session 강제 만료
- 관리자 reset 절차
- 분실 시 운영 복구 절차
- 초기 계정 seed idempotency
- prod에서 기본 계정 자동 활성화 금지 또는 명시적 승인 필요

현재 고정 default password가 존재하면 제거하고 안전한 bootstrap으로 migration한다.

필수 UI/runtime 테스트:

- 초기 관리자 생성
- 로그인
- 최초 변경 강제
- 비밀번호 변경 성공
- 정책 위반
- 기존 비밀번호 오류
- 재사용 금지
- 변경 후 기존 token 무효화
- 권한별 메뉴
- 계정 잠금/해제
- 관리자 reset
- audit 확인

## 12.3 운영 기능

ADM에 다음을 완성:

- transaction timeline
- recovery
- broker/DLQ/replay
- reconciliation
- file transfer
- batch/center-cut
- scheduler/worker
- service registry/health
- instance
- logs
- permissions
- audit
- masking
- download audit
- credential provider status
- configuration
- dynamic log level
- notification
- search/paging/sort/export

BIZADM과 ADM의 책임을 분리한다.

# 13. FileTransfer·Archive·Credential

## 13.1 FileTransfer

- LOCAL
- SFTP
- FTP
- FTPS
- SCP
- SSH
- port/adapter
- credential provider
- host key verification
- passive/active mode 기준
- standard ports
- timeout/retry
- checksum
- allowed root
- path traversal
- symlink escape
- unique temp
- atomic rename
- overwrite policy
- duplicate reservation
- partial cleanup
- restart/resume 가능 범위
- history
- unknown/reconciliation
- audit
- ADM
- EDU

실 서버가 없으면 protocol별 embedded/test server 또는 deterministic harness를 구현한다.

## 13.2 Archive/Compression

- ZIP
- GZIP
- TAR 지원 여부
- entry traversal
- zip bomb/size limit
- charset
- duplicate entry
- password/encryption 지원 여부
- checksum
- streaming
- cleanup
- history
- EDU/test

## 13.3 Credential

- environment
- file provider
- encrypted local provider
- Vault/KMS port
- cert/key metadata
- expiration
- rotation
- masking
- audit
- health
- ADM
- secret 원문 금지

# 14. BAT·Scheduler·Worker·Center-cut

- Spring Batch
- JobRepository
- JobInstance/JobExecution/StepExecution
- scheduler
- worker
- pause/resume/stop/restart/rerun
- center-cut target/item/result
- compensation
- unknown/reconciliation
- multi-instance claim/lease
- stale worker
- graceful shutdown
- JobInstance logical log
- businessDate
- shared storage
- degraded fragment mode
- ADM
- permission/audit
- EDU

실 shared storage가 없으면 process-level multi-worker harness와 DB lease를 완성한다.

# 15. 모든 프레임워크 기능 EDU 완성

## 15.1 EDU ownership

- 일반·온라인 EDU: XYZ
- 배치·center-cut EDU: BAT
- PFW: engine/port/reference adapter + unit/contract test
- CMN: parser/formatter/helper + unit test
- ACC/MBR/EXS/ADM/BIZADM: 범용 EDU 없음

## 15.2 EDU 전수 inventory

PFW/CMN의 모든 public capability를 찾아 각각 EDU를 만든다.

최소 대상:

- transaction/header
- segment/timeline
- file log
- DB log/recovery
- service call
- registry/health
- timeout/retry/failover/circuit
- idempotency
- outbox/inbox
- broker/DLQ/replay
- unknown/reconciliation
- file transfer protocols
- archive/compression
- security/auth/permission
- audit/masking/download audit
- code/message/config
- fixed-length
- validation/converter
- scheduler/worker
- batch/center-cut
- runtime control
- credential
- notification
- ADM query
- domain generator usage

## 15.3 EDU 품질

각 EDU는 실제 engine/port를 호출한다.

금지:

- fake Map/List
- hardcoded success
- PFW 기능 재구현
- wrapper-only
- raw URL WebClient
- Controller direct call
- 타 업무 module dependency
- sampleId-only

시나리오:

- normal
- validation error
- business error
- system error
- boundary
- duplicate
- concurrency
- timeout
- retry
- retry exhausted
- failover
- circuit
- rollback
- unknown
- reconciliation
- restart
- multi-instance
- permission denied
- masking
- audit
- file log
- DB
- ADM

각 기능에 적용되지 않는 시나리오는 N/A 이유를 기록한다.

## 15.4 EDU 완료 묶음

```text
capability source
+ engine/port test
+ XYZ/BAT sample
+ sample test
+ runtime smoke
+ OpenAPI
+ SQL
+ file/DB/ADM evidence
+ sample coverage
+ guide
```

전체 EDU 검증 리포트를 별도 DOCX로 생성한다.

# 16. Swagger/OpenAPI 전수 완성

전체 Controller를 전수 점검한다.

필수:

- `@Tag`
- `@Operation`
- 의미 있는 summary
- 상세 description
- 고유 explicit operationId
- request/response DTO
- schema description/example
- pagination/search/sort
- standard error
- 400/401/403/404/409/429/500/503
- header policy
- transactionGlobalId
- permission
- masking
- audit
- download audit
- timeout/unknown/replay/recovery 설명
- security requirement

의미 없는 자동 문구를 제거한다.

- `정보 조회`
- `요청 처리`
- `정보 변경`
- `정보 삭제`

API 역할에 맞게 작성한다.

7개 앱을 기동하고 `/v3/api-docs`를 저장한다.

검증:

- operationId duplicate 0
- explicit missing 0
- schema resolve
- broken `$ref` 0
- required header policy
- error response
- security
- examples
- Swagger UI
- runtime vs source mapping
- current commit evidence

# 17. 주제영역 생성기 최종 완성

`scripts/create-domain.ps1`과 관련 script/template를 최종 완성한다.

생성 입력:

- moduleCode
- moduleName
- basePackage
- port
- tablePrefix
- profile
- applicationName
- instanceId
- DB schema/table
- registry metadata

생성 결과에 반드시 포함:

- Gradle module
- settings.gradle 연결
- source
- application main
- controller
- service
- facade contract
- local facade
- remote proxy
- PFW Service Call Engine 사용
- configuration/properties
- local/dev/stg/prod yml
- log configuration
- absolute log root standard
- deploy env/inventory
- bootJar
- SQL split
- Flyway
- all_install integration
- smoke
- OpenAPI
- ADM registry/permission/menu metadata
- health
- transaction/header/logging
- test
- ownership test
- package/naming test
- generated README section
- cleanup/rollback option

생성기는 기존 ACC/MBR/EXS 기술 engine을 복제하거나 의존하지 않는다.

최종 검증:

1. 완전히 새로운 임의 module 생성
2. 예: LNG 또는 다른 미사용 코드
3. Java 25 compile
4. test
5. bootJar
6. class major 69
7. local runtime
8. `/v3/api-docs`
9. transaction/header/log
10. SQL/Flyway
11. MariaDB 가능 시 install
12. ADM registry
13. service call
14. multi-instance
15. ownership
16. qualityGate
17. 생성 모듈 삭제 후 repository 원복
18. 생성·삭제 과정이 기존 module/source/config/SQL에 영향 없음

사용자가 바로 실행할 수 있는 단일 PowerShell 진입 명령과 DOCX 가이드를 제공한다.

# 18. MariaDB·설치·실검증

MariaDB 설치·삭제·업그레이드 금지. 사용자 PC의 기설치 MariaDB만 사용한다.

비파괴 preflight:

- service
- client
- version
- host/port
- credential env present
- target schema 미변경

권장 변수:

- `CPF_DB_HOST`
- `CPF_DB_PORT`
- `CPF_DB_USER`
- `CPF_DB_PASSWORD`
- `CPF_DB_ADMIN_DATABASE`

값은 evidence에 기록하지 않는다.

접속 가능 시 신규 검증 schema에서:

- split SQL
- all_install
- all_install_and_smoke
- smoke
- repeat install
- Flyway baseline/latest
- older version → latest
- seed
- ADM bootstrap
- full runtime
- rollback log
- recovery
- broker
- idempotency race
- batch JobRepository
- domain generator SQL
- cleanup

접속 불가 시:

- password guessing 금지
- passwordless root 반복 금지
- 재설치 금지
- source/test/local harness 완료
- 정확한 재현 명령
- `미검증`

# 19. 문서 정본화: DOCX 전환과 HTML 삭제

## 19.1 원칙

기존 가이드가 약하고 분산돼 있으므로 이번 작업에서 사용자용 공식 문서를 **DOCX Word 문서로 전면 정본화**한다.

문서 분량은 제한하지 않는다. 수백 페이지·수만 줄이 되어도 좋다. 단순 분량 채우기는 금지하며 실제 source, SQL, runtime, 운영 절차를 정확히 설명한다.

모든 DOCX:

- 표지
- 문서명
- 버전
- 작성일
- 기준 commit
- 문서 상태
- 변경 이력
- 자동 목차 또는 Word TOC field
- Heading 1/2/3 스타일
- 페이지 번호
- header/footer
- 표/코드블록/주의사항 스타일
- cross-reference
- 용어집
- 색인 또는 기능 찾아보기
- appendix
- 깨진 한글/폰트/표 없음
- opening/rendering 검증
- 내용 누락 검증

## 19.2 최종 DOCX 문서 구성

중복 가이드는 통합해서 다음과 같이 정리한다.

1. `specs/CPF_프레임워크_소개_및_아키텍처.docx`
2. `specs/CPF_개발자_가이드.docx`
3. `specs/CPF_운영자_ADM_가이드.docx`
4. `specs/CPF_설치_DB_SQL_Flyway_가이드.docx`
5. `specs/CPF_배치_센터컷_스케줄러_가이드.docx`
6. `specs/CPF_외부연계_파일전송_전문_가이드.docx`
7. `specs/CPF_EDU_샘플_카탈로그_및_실습가이드.docx`
8. `specs/CPF_기능_구현_검증_매트릭스.docx`
9. `specs/CPF_전체_테스트_검증_리포트.docx`

필요하면 문서를 추가할 수 있지만 중복 문서를 양산하지 않는다.

## 19.3 기계 검증 자료

DOCX는 사용자 문서다. qualityGate와 자동 비교를 위해 다음 machine-readable 파일을 유지 또는 생성한다.

- `specs/기능_구현_매트릭스.md`
- `specs/기능_구현_매트릭스.json`
- `specs/sample-coverage-matrix.md`
- evidence JSON/LOG

DOCX 내용과 Markdown/JSON의 핵심 상태가 불일치하면 qualityGate 실패.

## 19.4 HTML 전면 삭제

DOCX 생성, 내용 이관, 링크 변경, 검증 완료 후 기존 `specs/**/*.html`을 전부 삭제한다.

숨은 HTML, 생성기 HTML template, README link도 전수 검색한다.

삭제 전:

- 유효 내용 DOCX 이관
- script/README/reference 수정
- `check-html-docs.ps1` 제거 또는 DOCX 검사 script로 대체
- qualityGate 갱신
- broken link scan
- DOCX opening/content/TOC 검사

최종 repository의 공식 가이드 HTML은 0개여야 한다.

# 20. README 전면 재작성

`README.md`는 프로젝트의 얼굴이자 프레임워크 브로셔다.

현재보다 훨씬 상세하고 보기 좋게 전면 재작성한다.

필수 목차:

- CPF 소개
- 누구를 위한 제품인지
- 핵심 가치
- 주요 기능 요약
- architecture diagram
- module map
- MSA/modular monolith
- Java 25
- quick start
- prerequisites
- clone/build/test
- local runtime
- MariaDB
- ADM 접속
- 관리자 bootstrap과 비밀번호 변경
- Swagger/OpenAPI
- EDU sample
- domain generator
- profiles
- deployment
- logs
- security
- batch/center-cut
- file transfer
- broker/reliability
- directory layout
- documentation list
- verification/evidence
- troubleshooting
- contribution/development rules
- current verified status
- limitations and external validations

README에는 secret 값이나 고정 초기 비밀번호를 쓰지 않는다.

README의 모든 command, path, link를 실제 검증한다.

# 21. 전수 테스트와 최종 검증 리포트

이번 작업 종료 전 지금까지 개발된 **전체 프레임워크 기능을 기능 하나하나 전수 검증**한다.

## 21.1 테스트 계층

- compile
- unit
- contract
- architecture
- repository/mapper
- DB slice
- integration
- EDU
- OpenAPI source
- OpenAPI runtime
- bootJar
- packaged resource
- local runtime
- multi-process
- multi-instance
- browser
- MariaDB
- broker harness
- file protocol harness
- batch
- generator
- security
- performance baseline
- cleanup
- documentation

## 21.2 기능별 테스트 ledger

각 기능마다:

```text
기능 ID
기능명
정본 요구
owner
source
test classes
test count
EDU
API
SQL
runtime command
expected
actual
file evidence
DB evidence
ADM evidence
status
failure
fix
rerun
remaining risk
```

## 21.3 실패 처리

- 최초 실패 보존
- 원인 분석
- source/config/SQL/test 수정
- focused rerun
- full regression
- 최종 결과
- regression 영향

Codex 환경에서 수정 가능한 실패가 남은 상태로 종료하지 않는다.

## 21.4 최종 리포트

`specs/CPF_전체_테스트_검증_리포트.docx`와 `CPF_STABILIZATION_REPORT.md`를 모두 작성한다.

DOCX 리포트는 사람이 읽는 상세 보고서, Markdown report는 machine-review 중심 상태 보고서다.

# 22. QualityGate 최종 강화

다음을 실패시킨다.

- Java 25가 아닌 toolchain/release/class major
- 개인 PC JDK absolute path
- non-Java25 README/guide/current evidence
- ownership 위반
- cross-domain Repository/Mapper
- Controller direct call
- URL direct composition
- ADM direct PFW internals
- Spring Event 핵심거래 사용
- DB naming violation
- table/column/comment/index mismatch
- split/Flyway/all_install mismatch
- operationId missing/duplicate
- meaningless OpenAPI summary
- runtime OpenAPI schema error
- EDU outside XYZ/BAT
- public capability without EDU
- fake EDU
- EDU without tests
- sample matrix mismatch
- source/test/SQL/OpenAPI/evidence bundle missing
- skip 완료 처리
- stale evidence
- evidence commit mismatch
- status value violation
- DOCX missing
- DOCX TOC/heading/open failure
- DOCX-machine matrix mismatch
- HTML residual
- broken documentation link
- hardcoded default password
- secret
- raw log/evidence
- garbage/orphan/empty directory
- generator failure
- generated module ownership violation
- report/gap/evidence/matrix/handoff mismatch
- changed/untracked manifest omission

# 23. 가비지·불필요 파일·폴더 최종 정리

작업 완료 후 전 repository를 전수 조사한다.

분류:

- 유지
- 통합 후 삭제
- 정제 후 유지
- 삭제
- 재확인 필요

대상:

- HTML
- stale guides
- duplicate Markdown
- old request/report candidate
- backup/copy/tmp
- build/out/generated
- raw logs
- stale evidence
- old Gradle problems report
- IDE personal settings
- `.vscode` 개인 경로
- orphan source/test
- orphan SQL/Flyway
- unused script
- duplicate smoke/check
- empty directory
- obsolete profile
- legacy env variable
- unused Docker file
- generated domain residue
- deleted feature references
- broken links

삭제 전에 참조 검색과 유효 내용 이관을 수행한다.

삭제 후:

- compile
- test
- bootJar
- runtime
- qualityGate
- documentation link
- secret scan
- `git diff --check`

을 다시 실행한다.

# 24. 정본·report·matrix·evidence 최종 정합성

실제 결과에 맞춰 갱신:

- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- `_01`~`_05`
- `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- 기능 matrix MD/JSON/DOCX
- sample coverage
- README
- DOCX guides
- handoff

정본을 수정하면:

- 목표 축소 금지
- 변경 전후 diff
- 변경 사유
- `_01`~`_05` 재생성
- 병합 bytes/hash
- 목차·검색성
- 중복 제거

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 현재 25MB 이상으로 매우 크므로 중복·병합 흔적을 분석한다. 정본 의미를 손상시키지 않고 중복 내용을 구조화·정리할 수 있으면 정리하되, 삭제한 요구사항과 이관 위치를 manifest로 남긴다.

# 25. Codex 인수인계 문서

작업 마지막에 `CPF_CODEX_HANDOFF.md`를 완성한다.

새 계정·새 PC·새 Codex 세션이 과거 대화 없이 즉시 이어갈 수 있어야 한다.

필수:

- repository/branch/HEAD
- final tree
- request hash
- canonical documents
- architecture decisions
- module ownership
- Java 25
- environment
- completed capabilities
- partial/unverified/failed
- source/test/SQL/evidence paths
- commands
- runtime ports
- admin bootstrap
- secret variable names
- known warnings
- failed approaches
- do-not-repeat
- remaining actions
- first 10 commands
- final manifest
- next priorities

민감정보 값은 기록하지 않는다.

# 26. Evidence

신규 non-overwrite evidence 디렉터리.

최소 metadata:

```text
CPF_EVIDENCE_VERSION
EVIDENCE_ID
STATUS
EXECUTED_AT
START_COMMIT
FINAL_WORKTREE_HASH
BRANCH
COMMAND
EXIT_CODE
JAVA_VERSION
GRADLE_VERSION
PROFILE
MODULE
TESTS
FAILURES
ERRORS
SKIPPED
SANITIZED
SECRET_SCAN
LOG_SHA256
```

runtime 추가:

- ports
- pid
- start/end
- request
- response status
- transactionGlobalId
- file path/hash
- DB row count
- ADM result
- OpenAPI path/hash
- cleanup

push 후 최종 master 기준으로 post-push verification을 할 수 있도록 final expected manifest와 commit-binding 정보를 제공한다.

# 27. 실행 순서

Codex는 다음 순서로 진행한다.

1. 최신 master와 요청서 보호
2. 전수 inventory
3. 정본 대비 gap 확정
4. Java 25 단일 기준
5. architecture/naming/DB standard
6. core source 구현
7. SQL/Flyway
8. unit/contract
9. EDU
10. OpenAPI
11. runtime harness
12. MariaDB 가능 시 DB
13. ADM/browser
14. domain generator
15. DOCX/README
16. HTML 삭제
17. garbage cleanup
18. full test
19. qualityGate
20. 실패 수정·재실행
21. gap 재스캔
22. report/matrix/evidence
23. handoff
24. final manifest
25. 최종 자체 감사

# 28. 최종 자체 감사 체크리스트

- [ ] 정본의 모든 capability를 inventory 했는가
- [ ] 요청서에 없던 gap도 처리했는가
- [ ] Java 25 class major 69인가
- [ ] 다른 Java 버전이 현재 기준 문서·설정에 없는가
- [ ] 모든 public capability에 EDU가 있는가
- [ ] 모든 EDU가 실제 engine/port를 호출하는가
- [ ] 모든 EDU test가 실행됐는가
- [ ] 모든 REST API OpenAPI가 runtime 검증됐는가
- [ ] DB/SQL/Flyway가 일치하는가
- [ ] naming/ownership 규칙 위반이 없는가
- [ ] MBR → ACC → EXS runtime이 검증됐는가
- [ ] recovery/idempotency/broker/unknown이 실제 처리되는가
- [ ] ADM 초기 접속과 비밀번호 변경 UI가 검증됐는가
- [ ] domain generator 결과가 즉시 실행 가능한가
- [ ] 생성기가 기존 프로젝트를 훼손하지 않는가
- [ ] DOCX 문서에 목차가 있고 열리는가
- [ ] 기존 HTML이 모두 삭제됐는가
- [ ] README가 최신이고 command/link가 검증됐는가
- [ ] garbage/orphan/empty directory가 없는가
- [ ] skip을 완료 처리하지 않았는가
- [ ] stale evidence가 없는가
- [ ] 전체 기능 테스트 리포트가 있는가
- [ ] 다음 Codex 세션이 handoff만 보고 이어갈 수 있는가
- [ ] report/gap/matrix/evidence/docx/handoff가 일치하는가
- [ ] 변경·신규·삭제 전체 manifest가 있는가

체크하지 못한 항목은 이유, 시도, 대안, 재현 명령을 바로 아래 기록한다.

# 29. 작업 금지

- Codex Git commit 금지
- Codex push 금지
- branch 생성 금지
- force push/rebase/history rewrite 금지
- 요청서 변경 금지
- baseline 재생성 금지
- 민감정보 원문 기록 금지
- 고정 평문 관리자 비밀번호 기록 금지
- MariaDB 설치·삭제·업그레이드 금지
- 실행하지 않은 검증 완료 처리 금지
- 목표 축소 금지
- 기능 삭제로 완료 만들기 금지
- 가짜 EDU 금지
- 문서 분량 채우기 금지
- 신규 HTML 금지
- PDF 생성 금지
- 외부 운영 인프라 강제 접속 금지
- 대량 삭제 전 참조·이관 확인 없이 삭제 금지

# 30. 최종 완료 판단

이번 작업 후 `완료` 후보가 되려면 다음이 실제로 확인돼야 한다.

1. Java 25 단일 기준과 class major 69
2. architecture/ownership/naming/DB 표준
3. 모든 공개 capability source·test
4. 모든 framework capability XYZ/BAT EDU
5. EDU test와 sample coverage
6. Service Call/MSA multi-process
7. transaction/segment/timeline/file/DB/recovery
8. idempotency/broker/DLQ/replay/unknown/reconciliation
9. security/permission/audit/masking/download
10. ADM 초기 로그인·비밀번호 변경 UI
11. BAT/scheduler/worker/center-cut
12. FileTransfer protocol/Archive
13. SQL/Flyway/all_install
14. OpenAPI runtime
15. domain generator 즉시 사용
16. DOCX 정본 가이드와 README
17. HTML 0개
18. garbage/orphan 제거
19. 전체 기능 전수 테스트
20. 상세 검증 DOCX/Markdown report
21. evidence 정합성
22. Codex handoff

외부 환경이 정말 없는 항목만 `미검증`으로 허용한다. 그 외 Codex 환경에서 구현·검증 가능한 항목은 이번 작업에서 끝낸다.

CPF 전체 최종 완료를 과대 선언하지 않는다. 실제 evidence가 충족된 범위만 완료로 기록한다.
