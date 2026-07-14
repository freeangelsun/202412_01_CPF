# CPF 다음 작업 요청서

## 1. 작업명

**CPF MSA·Reliability 실연결, EDU·OpenAPI 전수 완성, DB·ADM 통합검증 대형 마일스톤**

---

## 2. 작업 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 최신 commit: `fb6b17850aa915c8ff0db034833e2fe1343fc322`
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

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 유일한 정본이다. `_01`~`_05`는 검색 보조 파일이며, 정본을 축소하거나 현재 구현 수준에 맞춰 목표를 낮추는 근거로 사용하지 않는다.

작업 시작 전에 최신 `master`를 다시 확인한다. 최신 commit이 위 기준과 다르면 변경분을 먼저 확인하고 실제 시작 commit을 report와 evidence에 기록한다.

---

## 3. 이번 작업의 목적

직전 작업에서 로그 root, 거래별 파일 로그, DB 거래 로그 `REQUIRES_NEW`, durable fallback/recovery, ADM 복구 API, SQL/Flyway V25, Java 21 test·bootJar·qualityGate 기반이 보강됐다.

그러나 최신 master 직접 검수 결과 다음 문제가 남아 있다.

1. ADM이 PFW 내부 구현체인 `TransactionLogRecoveryWorker`를 직접 의존할 가능성이 있어 PFW query/command port·facade 경계가 불완전하다.
2. transaction log fallback은 추가됐지만 segment/timeline 저장 실패가 durable recovery 대상으로 완전히 연결됐는지 불충분하다.
3. recovery worker에 재시도 대기 파일이 batch quota를 소비하거나 processing 파일이 고착될 수 있는 starvation·stuck 위험이 있다.
4. PFW·CMN·EXS·ADM·BIZADM에 범용 `EducationSample`이 남아 EDU ownership 원칙을 위반한다.
5. sample class와 test 개수는 많지만 실제 PFW port/engine 호출, 정상·오류·timeout·retry·failover·rollback·recovery·다중 인스턴스·로그·DB·ADM 검증 품질이 균일하지 않다.
6. OpenAPI mapping 351개 중 명시적 operationId는 일부에 불과하며, runtime `/v3/api-docs` 검증, DTO schema, 표준 오류 response, 권한·마스킹·감사 설명 전수 검증이 미완료다.
7. MBR → ACC → EXS 실제 HTTP 다중 인스턴스 호출에서 registry, health, selectedInstanceId, retry, failover, circuit breaker, unknown, reconciliation이 통합 검증되지 않았다.
8. dev/stg/prod 로그 root가 특정 OS 절대경로 예시에 종속될 수 있어 cross-platform 외부 주입형 template 정리가 필요하다.
9. MariaDB full install, V25 upgrade, rollback 독립 로그, fallback DB 재적재, ADM runtime/browser는 인증정보 부재로 미검증이다.
10. 실 broker, 외부 파일전송 protocol, 공유 storage 기반 BAT cross-instance same-file writer는 미검증 또는 부분 구현이다.
11. `docker-compose.local.yml`, 루트 `info` 폴더와 의미 불명확한 루트 파일·디렉터리의 유지 필요성을 정식으로 판정하지 않았다.
12. 정본 요구사항에 이번에 확정된 EDU·OpenAPI·로그·reference domain·선택형 Docker harness 기준이 충분히 반영됐는지 재확인이 필요하다.
13. Java 25는 Gradle 8.11.1 Test task 생성 단계에서 실패한다.
14. 직전 qualityGate는 EDU ownership 위반과 “실제 capability 호출 없는 샘플”을 충분히 차단하지 못한다.

이번 작업은 문서 정리나 재검수만 하는 작업이 아니다.  
**MSA·Reliability 핵심 흐름을 실제 구현·연결하고, EDU와 OpenAPI를 그 기능의 검증 수단으로 완성하는 개발 마일스톤**이다.

---

## 4. 구현 판단과 대안 적용 원칙

본 요청서의 목표, ownership, 완료 기준, 금지사항은 반드시 지킨다. 다만 세부 클래스명, package 분해, port 이름, worker 알고리즘, SQL 구조, 테스트 harness 등은 절대 고정안이 아니다.

Codex 검토 결과 요청서에 적힌 방식보다 더 안전하고 확장 가능하며 정본 목표에 부합하는 대안이 있으면 그 방식을 적용할 수 있다.

단, 다음을 반드시 지킨다.

- 목표 범위와 완료 기준 축소 금지
- 미처리 항목 삭제 금지
- 구현 편의를 위해 ownership·MSA 경계·내구성 기준 완화 금지
- 요청 방식과 다르게 처리한 경우:
  - 변경 사유
  - 선택한 대안
  - 장점과 단점
  - 영향 파일
  - migration·호환성 영향
  - 남은 gap
  - 후속 조치
  를 report에 기록한다.
- 처리 불가 항목은 단순히 “불가”라고 끝내지 않는다.
- 실패 원인을 `요구사항 / 설계 / 구현 / 계층 / SQL / 설정 / profile / 테스트 / 환경 / DB / broker / browser / evidence / ownership / Spring Event / OpenAPI / EDU` 중 하나 이상으로 분류한다.
- 같은 접근이 반복 실패하면 같은 방식으로 재시도하지 말고 대안을 제시하고 가능한 범위까지 구현한다.
- 외부 환경 부재는 source·unit·contract·local harness 개발을 미루는 이유가 될 수 없다.
- 실제 실행하지 못한 환경 검증만 `미검증`으로 남긴다.

---

## 5. 상태와 완료 기준

허용 상태값:

- `완료`
- `부분 구현`
- `미구현`
- `미검증`
- `실패`
- `재확인 필요`

기능 하나의 `완료`는 다음 묶음이 모두 확인된 경우만 가능하다.

```text
기능 source
계층·ownership 연결
unit·contract test
실제 XYZ 또는 BAT EDU class
EDU test
필요 SQL/Flyway/all_install
실제 runtime 또는 해당 기능에 필요한 실행 evidence
REST API이면 Swagger/OpenAPI
sample coverage
기능 matrix
sanitized evidence
report/gap/index 정합성
```

다음은 완료가 아니다.

- source만 존재
- test만 통과
- `CoverageCatalog` 또는 sampleId만 존재
- plan/DTO/Map만 생성하는 가짜 EDU
- PFW 내부 `EducationSample`을 XYZ에서 감싸는 wrapper
- 정상 케이스만 존재
- stale 로그 재사용
- runtime이 필요한데 unit test만 존재
- OpenAPI annotation만 있고 `/v3/api-docs` 미검증
- DB 기능인데 실제 DB row·rollback·recovery evidence 없음

---

# 6. 필수 완료 범위

## 6.1 시작 기준, 요청서 보호, 최종 변경 manifest

작업 시작 즉시 다음을 수행한다.

- 시작 commit 기록
- `CPF_NEW_REQUEST.md` SHA-256
- `git hash-object CPF_NEW_REQUEST.md`
- 시작 commit의 blob과 현재 파일 비교
- 신규 non-overwrite evidence 디렉터리에 baseline copy와 hash 저장
- baseline 재생성·덮어쓰기 금지
- 작업 종료 시 byte-for-byte 비교

작업 종료 시 반드시 실행:

```text
git status --short
git diff --name-status
git diff --cached --name-status
git ls-files --others --exclude-standard
git diff --check
```

모든 intentional 변경·삭제·신규 파일을 commit 대상 manifest에 포함한다. accidental/unrelated 변경은 0건이어야 한다.

Codex는 Git commit, push, branch 생성, force push, rebase, history rewrite를 수행하지 않는다.

---

## 6.2 직전 검수 결함 필수 수정

### 6.2.1 ADM → PFW 경계 정리

ADM이 PFW 내부 worker, mapper, repository, table을 직접 호출하지 않도록 정리한다.

필수 구조:

```text
ADM Controller/Service
  → PFW public query/command port 또는 facade
    → PFW recovery engine/worker/repository
```

필수:

- recovery 상태 조회 port
- recovery 수동 실행 command port
- transaction timeline query port
- 권한·감사 사유·before/after 결과
- Local Facade와 Remote Facade Proxy 모두 고려
- ADM에서 PFW 내부 구현체 직접 주입 금지
- ADM에서 PFW table 직접 JDBC/Repository/Mapper 접근 금지
- architecture test/gate 추가

### 6.2.2 transaction·segment·timeline 전체 durable recovery

DB 장애 시 transaction log뿐 아니라 전체 추적 계층이 유실되지 않도록 설계한다.

대상:

- transaction start/final
- segment start/end
- parent-child
- selectedInstanceId
- attempt
- retry/failover/circuit
- downstream response
- business commit/rollback
- timeout/unknown
- reconciliation/replay

transaction, segment, timeline을 각각 별도 journal로 분리할지 하나의 통합 event envelope로 구성할지는 Codex가 더 안전한 대안을 선택할 수 있다.

필수:

- eventId·sequence·version
- 중복 방지
- 순서 보존
- parent segment 선행 관계
- 재적재 idempotency
- 부분 재적재 후 재시도
- poison 격리
- masking
- spool limit
- health/metric
- ADM 상태 조회
- DB 복구 후 전체 tree 재구성 검증

segment 저장 실패를 warning만 남기고 폐기하면 실패다.

### 6.2.3 recovery worker starvation·stuck 방지

다음 결함 가능성을 소스와 테스트로 확인하고 수정한다.

- 아직 retry 시각이 아닌 파일이 batch quota를 소비
- 처리 가능한 뒤쪽 파일 starvation
- claim 후 read/parse 실패 시 `processing` 고착
- 프로세스 종료 중 processing 파일 복구 누락
- 다중 worker 동일 파일 claim
- stale processing lease
- poison 반복 이동
- spool full 시 원 거래 예외 덮어쓰기

필수 테스트:

- mixed ready/not-ready queue
- ready 파일 우선 처리
- oldest eligible ordering
- claim/read 실패 복원
- stale processing reclaim
- concurrent worker claim
- process restart
- max retry → poison
- poison 재시도 승인 절차
- spool capacity/disk pressure
- 민감정보 원문 부재

---

## 6.3 Service Call Engine·MSA 실연결 신규 개발

직전 작업의 로그 기반 위에 실제 MSA 호출 흐름을 닫는다.

대표 흐름:

```text
MBR → ACC → EXS
```

ACC와 EXS는 삭제하지 않고 **최소 reference domain**으로 유지한다.

역할:

- MBR: 진입·복합 거래 caller
- ACC: 내부 업무 service reference
- EXS: 외부연계·하위 호출 reference
- XYZ: 일반·온라인 EDU
- BAT: batch·center-cut EDU
- ADM: framework operation
- BIZADM: business operation

필수 구현·검증:

- same JVM Local Facade
- separate process Remote Facade Proxy
- PFW Service Call Engine
- registry 후보 조회
- health 기반 선택
- instance selection
- selectedInstanceId 기록
- timeout
- retry
- backoff
- circuit breaker open/half-open/close
- failover
- attempt별 segment/timeline
- 표준·확장 header propagation
- transactionGlobalId propagation
- source/target transactionId
- caller file summary
- callee own transaction file
- DB timeline
- ADM 통합 조회
- business error
- system error
- timeout
- retry 후 성공
- retry 소진
- 첫 instance 실패 후 다른 instance 성공
- 최종 실패
- unknown result
- reconciliation 후 확정
- 상위 rollback 후 하위 성공 상태
- graceful shutdown 중 호출
- 다중 인스턴스 동시 호출

URL 직접 조합, Controller 직접 호출, 타 주제영역 Repository/Mapper 직접 접근은 금지한다.

---

## 6.4 Idempotency·Broker·Unknown/Reconciliation 통합 보강

### Idempotency

필수:

- same key/same payload replay
- same key/different payload conflict
- expired PROCESSING restart
- FAILED/UNKNOWN/EXPIRED retry
- concurrent unique race
- process restart
- REST, broker, file transfer, BAT 실제 연결
- transactionGlobalId·audit·ADM 상태

### Broker/Outbox/Inbox/DLQ/Replay

필수:

- outbox atomic write
- publisher claim/lease
- retry/backoff/max attempts
- inbox duplicate prevention
- consumer failure
- DLQ 이동
- replay가 상태만 바꾸지 않고 실제 재처리 수행
- replay idempotency
- poison message
- multi-worker
- process restart
- transactionGlobalId·segment
- ADM 조회·권한·감사
- local deterministic adapter runtime

실 Kafka/MQ가 없으면 production port·adapter, deterministic local harness, contract test까지 구현하고 real broker runtime만 `미검증`으로 남긴다.

### Unknown Result / Reconciliation

필수:

- timeout 후 결과 미확정
- external key
- duplicate callback
- polling reconciliation
- manual resolve
- compensation
- final success/failure
- audit reason
- before/after
- replay와 reconciliation 중복 방지
- ADM list/detail/search

---

## 6.5 EDU 전수 재구성 및 누락 기능 자동 발견

### 6.5.1 ownership

- 일반·온라인·대외연계 EDU: `xyz/.../edu`
- 배치·센터컷 EDU: `bat/.../edu`
- PFW: engine/port/reference adapter + unit·contract test
- CMN: 공통 parser/formatter/converter/helper + unit test
- ACC/MBR/EXS/ADM/BIZADM: 범용 개발자 EDU 미소유

PFW/CMN/EXS/ADM/BIZADM에 있는 기존 `EducationSample`을 전수 조사한다.

각 파일은 다음 중 하나로 분류한다.

- XYZ로 이전
- BAT로 이전
- PFW contract fixture/test helper로 전환
- CMN unit fixture로 전환
- reference adapter로 이름·역할 정리
- 호환성 때문에 임시 유지
- 삭제
- 재확인 필요

호환성 때문에 유지하면 이유, 호출자, 제거 계획, 상태를 기록한다.

### 6.5.2 요청서에 직접 적지 않은 capability 자동 발견

다음을 전수 스캔한다.

- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- PFW/CMN public port·engine·adapter
- 전체 REST Controller
- SQL/Flyway table
- ADM 메뉴·운영 API
- 기능 구현 matrix
- sample coverage
- source/test/evidence
- 생성기 template
- runtime script

요청서에 기능명이 없더라도 개발자가 사용하는 공개 capability인데 EDU가 없거나 부족하면 이번 범위에 포함한다.

### 6.5.3 실제 EDU 기준

EDU는 다음을 만족해야 한다.

- 실제 PFW/CMN public port/engine 호출
- 실제 설정 예시
- 정상·오류·경계·복구 케이스
- transactionGlobalId·segment
- 파일 로그
- DB 상태
- ADM 확인 경로
- REST이면 OpenAPI
- 운영 시 금지 구현 설명

금지:

- Map/List로 가짜 상태 관리
- 성공값 하드코딩
- PFW 기능을 EDU 내부에서 재구현
- PFW `EducationSample` wrapper 호출
- raw WebClient로 임의 URL 직접 호출
- Controller 직접 호출
- 타 업무 module dependency
- sampleId만 추가
- stale evidence 재사용

### 6.5.4 기능별 최소 시나리오

각 capability에서 적용 가능한 시나리오를 구현한다.

```text
정상
입력 오류
업무 오류
시스템 오류
경계값
중복
동시성
timeout
retry
retry 소진
failover
circuit open/half-open
rollback
unknown
reconciliation
재기동
다중 인스턴스
권한 거부
masking
audit
파일 로그
DB 상태
ADM 조회
```

적용하지 않는 시나리오는 N/A 근거를 기록한다.

### 6.5.5 EDU 검증 묶음

기능 하나의 review bundle:

```text
PFW/CMN source
기능 test
XYZ/BAT EDU class
EDU test
actual runtime log
DB row
ADM result
Swagger/OpenAPI
sample coverage
기능 matrix
evidence
report/index/gap
```

---

## 6.6 Swagger/OpenAPI 전수 완성

현재 자동 operationId 생성은 fallback으로만 사용한다. 공개 REST API는 명시적인 안정적 operationId를 가져야 한다.

전체 Controller와 mapping을 전수 점검한다.

필수:

- `@Tag`
- `@Operation`
- 명시적 고유 `operationId`
- request DTO
- response DTO
- paging/search DTO
- request/response schema
- field description
- example
- 표준 오류 response
- 400/401/403/404/409/429/500/503
- transactionGlobalId/header 설명
- 권한
- masking
- audit
- download audit
- timeout/unknown/replay/recovery 설명
- 실제 API 특성에 맞는 required header

공통 header를 모든 API에 무조건 required로 넣지 않는다.

API를 구분한다.

- 업무 거래 API
- 운영 조회 API
- command API
- callback
- download
- health/actuator
- batch operation
- internal API

각 유형별 header policy를 실제 runtime filter와 일치시킨다.

필수 runtime 검증:

- 7개 실행 모듈 기동 가능한 범위
- `/v3/api-docs`
- operationId 중복 0
- 명시 operationId 누락 0 또는 예외 목록 0
- schema 참조 오류 0
- 표준 오류 schema 존재
- required header 정책 일치
- security requirement
- Swagger UI 접근
- OpenAPI JSON 저장
- 이전 operationId 변경 영향 분석

`Map<String,Object>` 응답은 운영 API에서 DTO로 전환한다. 불가한 동적 응답은 schema와 사유를 명시한다.

---

## 6.7 MariaDB 실검증

MariaDB를 설치·삭제·업그레이드하지 않는다. 사용자가 이미 설치한 MariaDB를 사용한다.

작업 시작 시 비파괴 preflight:

- service 상태
- client 버전
- host/port
- credential 환경변수 존재 여부
- target schema 임의 변경 없는 연결 확인

인증정보가 있으면 신규 빈 검증 schema에서 실행한다.

- split SQL
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- Flyway baseline
- V22 → V25 upgrade
- seed
- index/constraint/default/comment
- rollback independent transaction log
- segment/timeline
- fallback journal 생성
- DB 복구 후 재적재
- duplicate event 방지
- poison
- ADM query
- MBR → ACC → EXS timeline
- cleanup

인증정보가 없으면:

- 비밀번호 추측 금지
- passwordless root 반복 시도 금지
- MariaDB 재설치 금지
- credential 원문 기록 금지
- preflight와 재현 명령 기록
- DB 검증만 `미검증`
- DB 없이 가능한 source/test/runtime/file-log/local harness는 끝까지 완료

환경변수 값은 evidence에 기록하지 않고 present/absent만 남긴다.

---

## 6.8 ADM runtime·browser 통합 검증

필수 API/화면:

- transactionGlobalId 통합 조회
- segment tree
- selectedInstanceId
- attempt/retry/failover/circuit
- timeout/unknown/rollback
- recovery pending/processing/poison
- recovery 수동 실행
- broker DLQ/replay
- reconciliation
- BAT JobInstance log
- file log reference
- permission
- audit reason
- before/after
- masking
- pagination/search/sort

필수 검증:

- 200/401/403/404/409
- 권한별 runtime
- request user spoofing 차단
- 감사 사유 필수
- 상태 변경과 audit 원자성
- browser click
- 대량 segment pagination
- download audit
- `/v3/api-docs`

환경 부재 시 controller/service/unit/OpenAPI까지 구현하고 browser만 `미검증`으로 남긴다.

---

## 6.9 BAT 공유 storage·writer ownership 보강

현재 단일 JVM path policy test만으로 완료 처리하지 않는다.

필수:

- JobInstance logical file
- restart same logical file
- businessDate fixed
- jobExecutionId/restartAttempt/rerunId
- shared durable storage
- DB lease 또는 동등한 single writer
- stale writer 차단
- lease takeover
- multi-server restart
- atomic append/journal
- DB/ADM correlation
- degraded fragment mode 명시

실 shared storage가 없으면 local filesystem + DB lease harness 또는 process-level simulation으로 source·contract를 검증하고 실제 shared storage runtime만 `미검증` 처리한다.

---

## 6.10 FileTransfer·Archive·외부 protocol

PFW ownership 유지.

필수:

- allowed base
- path traversal
- symbolic link escape
- unique temp
- checksum
- atomic rename
- overwrite policy
- duplicate reservation
- cleanup
- restart
- multi-instance
- audit
- credential provider
- transactionGlobalId
- ADM status

SFTP/FTP/SCP/SSH는 표준 port와 reference adapter를 구현한다. 실제 서버가 없으면 local test server 또는 stub harness를 사용한다. protocol별 production runtime을 구분해 기록한다.

archive/compression은 ZIP/GZIP 등 실제 지원 범위와 TAR 미지원/부분 구현을 명확히 구분한다.

---

## 6.11 설정·루트 파일·reference domain 정리

### `docker-compose.local.yml`

실제 사용처를 전수 확인한다.

유지 조건:

- local 선택형 MariaDB/Redis/Kafka 검증 harness
- 운영 필수 전제가 아님
- profile 구분
- 고정 credential 원문 없음
- image version 근거
- Docker 미사용 환경 대체 명령
- README에 선택형임을 명시

사용되지 않거나 현재 구조와 충돌하면 삭제할 수 있다. 삭제 시 대체 harness와 영향 분석을 기록한다.

### 루트 `info` 폴더

- 참조 여부 전수 검색
- 유효 내용은 정본/README/SQL guide로 통합
- stale·중복이면 삭제
- 비면 폴더 삭제
- 삭제 근거와 이관 위치 기록

### 기타 루트 파일·폴더

각 항목을 다음으로 분류한다.

- 유지
- 통합
- 정제 후 유지
- 삭제
- 재확인 필요

backup/temp/candidate/generated/build/out/IDE/stale evidence/orphan config/script를 정리한다.

### reference domain

ACC·EXS는 지금 삭제하지 않는다.

- 최소 reference domain으로 슬림화
- 범용 EDU 제거
- PFW engine 복제 제거
- 대표 API/Facade/DB만 유지
- MSA·DB ownership·segment 검증에 사용
- 생성기 결과 비교 기준으로 유지

---

## 6.12 정본 요구사항 보강

이번에 확정된 요구가 정본에 없거나 모호하면 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 보강할 수 있다.

대상 예:

- EDU ownership과 실제 capability 호출
- capability별 시나리오 coverage
- OpenAPI 명시 operationId·표준 오류
- transaction·segment·timeline durable recovery
- ADM → PFW port/facade
- reference domain 역할
- optional Docker local harness
- 로그 root cross-platform 외부 주입
- Codex 대안 적용·사유 보고
- 미처리 원인·대안 기록

정본 수정 규칙:

- 기존 목표 삭제·축소 금지
- 미구현 항목 삭제 금지
- 현재 구현 수준에 맞춘 완화 금지
- 대규모 재서술로 의미 변형 금지
- 변경 전후 diff
- 변경 사유
- 영향 범위
- 신규 요구/모호성 해소/충돌 정리 분류
- `_01`~`_05` 전체 재생성
- 병합 byte/hash 검증
- report/gap/matrix/evidence 정합성

---

## 6.13 Java 25 호환성

Java 25 목표는 삭제하지 않는다.

필수:

- 현재 실패 재현
- Gradle/Spring Boot/plugin/JUnit/bytecode tool 원인 분리
- 공식 지원 조합 확인
- 최소 업그레이드 경로
- dependency compatibility
- compile
- test
- bootJar
- qualityGate
- 대표 runtime
- Java 21 회귀

이번 범위에서 안전한 업그레이드가 가능하면 수행한다. 대규모 framework major upgrade로 위험이 크면 source·plan·compatibility matrix를 만들고 상태를 `부분 구현` 또는 `실패`로 정확히 남긴다.

Java 21 성공으로 Java 25 완료 처리하지 않는다.

---

# 7. 보강 범위

필수 범위를 해치지 않으면 함께 보강한다.

- log disk metric
- recovery spool alert
- rate limit
- correlation between traceId and transactionGlobalId
- graceful shutdown
- worker pause/resume/drain
- rolling deploy
- API/event/schema versioning
- stale evidence commit detection
- dependency/license/vulnerability/secret scan
- performance baseline
- large segment tree query limit
- OpenTelemetry port

---

# 8. 착수 범위

필수 완료 후 남는 작업량에서 착수한다.

- real Kafka/MQ adapter runtime
- SFTP/FTP/SCP/SSH test server harness
- shared storage BAT cross-instance runtime
- Vault/KMS runtime
- MBR → ACC → EXS multi-WAS automated failover
- performance/soak/spike
- DR
- SLI/SLO p95/p99

착수만 한 항목은 완료로 기록하지 않는다.

---

# 9. 후순위·제외 범위

- 운영 Redis/Kafka/MQ 강제 접속
- 운영 SFTP/FTP/SCP/SSH 강제 접속
- 운영 Vault/KMS/HSM 강제 접속
- MariaDB 설치·삭제·업그레이드
- 운영 서버 설정 강제 변경
- force push/history rewrite
- 전체 최종 HTML 정본화
- 최종 PDF 생성
- CPF 전체 최종 완료 선언

환경이 없는 항목은 삭제하지 않고 `미검증` 또는 `재확인 필요`로 유지한다.

---

# 10. 필수 실행 시나리오

최소 실행·증적:

1. 시작 commit/request hash 보호
2. changed/untracked manifest
3. Java 21 full test
4. 7개 bootJar
5. qualityGate
6. ADM → PFW port/facade architecture test
7. transaction·segment·timeline fallback
8. recovery worker starvation
9. processing stale reclaim
10. poison
11. spool capacity
12. MBR → ACC Local Facade
13. MBR → ACC Remote Proxy
14. MBR → ACC → EXS
15. registry/health
16. retry
17. failover
18. circuit open/half-open
19. timeout
20. unknown/reconciliation
21. selectedInstanceId
22. caller/callee file log
23. DB timeline
24. ADM integrated query
25. idempotency race/restart
26. broker retry/DLQ/actual replay
27. EDU ownership scan
28. capability-to-EDU coverage
29. EDU failure/boundary/recovery cases
30. `/v3/api-docs`
31. operationId explicit coverage
32. error response schema
33. MariaDB full install/Flyway if credential available
34. rollback independent DB log
35. fallback recovery to DB
36. ADM permission/browser if environment available
37. BAT writer ownership harness
38. FileTransfer protocol contract
39. Docker/info/root cleanup decision
40. canonical requirement diff/hash if changed
41. final log cleanup
42. `git diff --check`

---

# 11. Evidence 기준

신규 non-overwrite evidence 디렉터리를 사용한다.

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

runtime evidence에는 추가:

- log root
- file manifest
- relative path
- created/modified time
- bytes
- line count
- hash
- JSON parse
- required fields
- transactionId/globalId masked example
- DB row count
- ADM response
- cleanup before/after

민감정보 원문 기록 금지.

---

# 12. qualityGate 강화

다음을 실패시켜야 한다.

- 요청서 변경
- baseline 재생성
- 없는 evidence
- stale/different commit evidence
- 비허용 상태값
- SKIPPED 완료 처리
- CoverageCatalog-only 완료
- PFW/CMN/EXS/ADM/BIZADM 범용 EDU
- XYZ/BAT EDU가 실제 PFW port/engine을 호출하지 않음
- fake Map/List sample
- 정상 케이스만 있는 핵심 capability
- 명시 operationId 누락
- duplicate operationId
- 표준 오류 response 누락
- runtime `/v3/api-docs` schema 오류
- ADM의 PFW 내부 worker/table 직접 접근
- segment/timeline fallback 누락
- raw runtime 로그 추적
- 상대 log root
- fixed OS-specific prod path
- orphan/stale/garbage
- report/matrix/evidence 불일치
- 최종 changed/untracked manifest 누락

---

# 13. 문서·상태 갱신

실제 상태로 갱신:

- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- `specs/기능_구현_매트릭스.html`
- `specs/sample-coverage-matrix.md`
- 관련 README/guide 최소 수정
- 정본 수정 시 `_01`~`_05`

문서량을 채우기 위해 신규 HTML을 만들지 않는다. 기존 HTML 수정도 실제 기능·상태 반영에 필요한 최소 범위로 한다. PDF는 생성하지 않는다.

---

# 14. 결과 보고 형식

최종 report는 다음 순서로 작성한다.

1. 시작 commit·종료 worktree
2. 요청서 보호
3. 직접 변경 source/test/SQL/config/script
4. 직접 실행 명령
5. 성공
6. 실패
7. 미실행·환경 부재
8. 요청서와 다르게 적용한 대안
9. 대안 사유·장단점·영향
10. 미처리 원인 분류
11. 다음 실행 가능한 조치
12. EDU coverage
13. OpenAPI runtime
14. MSA/Reliability runtime
15. MariaDB
16. ADM
17. BAT/FileTransfer/Broker
18. cleanup
19. report/gap/evidence/matrix 정합성
20. 사용자 commit 대상 전체 manifest

Codex 주장과 직접 실행 결과를 구분한다.

---

# 15. 작업 금지사항

- Git commit 금지
- Git push 금지
- branch 생성 금지
- force push 금지
- rebase/history rewrite 금지
- `CPF_NEW_REQUEST.md` 수정 금지
- baseline 재초기화 금지
- 민감정보 원문 기록 금지
- MariaDB 설치·삭제·업그레이드 금지
- 실행하지 않은 검증 완료 처리 금지
- 목표 축소 금지
- 문서량 채우기 금지
- 불필요 신규 HTML 금지
- PDF 생성 금지
- PFW capability를 업무 모듈 소유로 이동 금지
- 타 주제영역 DB/Repository/Mapper 직접 접근 금지
- URL 직접 조합 금지
- Controller 직접 호출 금지
- 핵심 거래를 Spring Event만으로 처리 금지
- 불필요 대량 삭제 금지
- 검수 없이 정본/evidence 삭제 금지

---

# 16. 완료 판단 핵심

이번 마일스톤은 다음이 실제 확인되어야 완료 후보가 된다.

1. ADM이 PFW public port/facade만 사용한다.
2. transaction·segment·timeline 전체가 rollback 독립·durable recovery된다.
3. recovery worker starvation·stuck·concurrency가 해결된다.
4. MBR → ACC → EXS Local/Remote 호출이 실제 연결된다.
5. retry/failover/circuit/unknown/reconciliation이 파일·DB·ADM에 연결된다.
6. idempotency·broker·DLQ replay가 실제 재처리까지 동작한다.
7. 범용 EDU가 XYZ/BAT로 정리된다.
8. 모든 공개 capability에 실사용 EDU와 주요 시나리오가 있다.
9. OpenAPI가 명시 operationId·DTO·표준 오류·권한 설명을 갖고 runtime JSON으로 검증된다.
10. MariaDB 접근 가능 시 full install·V25·rollback·recovery가 실제 검증된다.
11. BAT writer ownership, FileTransfer contract가 검증된다.
12. Docker/info/root cleanup이 근거 있게 결정된다.
13. 필요 시 정본이 목표 축소 없이 보강된다.
14. Java 25 상태가 정확히 검증·기록된다.
15. report/gap/evidence/matrix/sample coverage가 실제 상태와 일치한다.

실행하지 못한 항목은 완료가 아니다.  
불가하거나 실패한 항목은 원인·대안·후속 조치를 남기고 가능한 개발 범위까지 완료한다.
