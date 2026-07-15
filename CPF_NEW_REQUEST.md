# CPF 차기 최종 통합 개발·정리·검증 요청서

## 0. 기준과 작업 금지사항

### 기준 저장소

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 이번 검수 기준 commit: `0d206c683aab840dbab4639f2797dd7fd718cefd`
- Commit message: `20260715_01`

### 정본과 필수 참조

- 최상위 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검색 보조: `CPF_FINAL_TARGET_REQUIREMENTS_01.md`~`05.md`
- 완료 판정: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 현재 결과: `CPF_STABILIZATION_REPORT.md`
- gap: `CPF_GAP_MATRIX.md`
- evidence: `CPF_EVIDENCE_INDEX.md`
- 기능 matrix
- sample coverage matrix
- source/test/sql/Flyway/all_install
- runtime/browser/MariaDB/broker/multi-instance evidence

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 정본이며 `_01`~`_05`는 검색 보조다. 원본 목표를 확인하지 않고 작업 범위를 축소하거나 기존 구현 수준을 최종 목표로 간주하지 않는다.

### 금지

- Git commit 금지
- Git push 금지
- branch 생성 금지
- force push 금지
- 민감정보 원문 기록 금지
- 실행하지 않은 검증을 완료로 기록 금지
- 문서량을 채우기 위한 문서 생성 금지
- 신규 HTML 문서 작성·수정 지양
- PDF 생성 금지
- CoverageCatalog 또는 sampleId만으로 EDU 완료 처리 금지
- source만 존재하는 기능을 완료 처리 금지
- 정적 UI marker만으로 브라우저 완료 처리 금지
- 실제 DB·broker·remote server를 실행하지 않고 runtime 완료 처리 금지

### 허용 상태

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

---

# 1. 이번 GitHub master 직접 검수 결과

## 1.1 검증 사실

최신 master는 `0d206c683aab840dbab4639f2797dd7fd718cefd`다.

해당 commit은 대규모 변경이다.

- additions: 40,644
- deletions: 4,364
- total changes: 45,008
- ADM bootstrap·비밀번호 변경·session 관련 source/test 추가
- PFW broker·file transfer ownership 이동
- Java 25 gate 추가
- DOCX 9개 추가
- README 재작성
- HTML 일부 삭제
- evidence·matrix·report 동기화

`CPF_STABILIZATION_REPORT.md`에는 다음이 기록되어 있다.

- qualityGate 79개 Gradle task
- JUnit 290 tests
- failures 0
- errors 0
- skipped 4
- 7개 bootJar 기동
- Java 25 class major 69
- DOCX 구조·Word 열기
- report/matrix/evidence consistency

그러나 이 수치는 이번 검수에서 직접 명령을 다시 실행한 것이 아니라 repository에 기록된 evidence와 Codex 결과다. 따라서 `Codex 주장 및 저장소 evidence 확인`으로 분류하며, 외부환경 검증은 별도다.

## 1.2 확인된 미완료·미검증

현재 report 자체가 다음을 미검증 또는 부분 구현으로 남기고 있다.

- MariaDB full install 최신 commit 실검증
- DB 권한 분리
- Flyway 실제 적용
- DB 기반 ADM 로그인·화면
- DB 기반 transaction timeline
- DB 기반 scheduler·batch·center-cut
- 실 Redis
- 실 Kafka
- 실 RabbitMQ
- 실 SFTP/FTP/FTPS/SCP/SSH
- 다중 프로세스·공유 스토리지 BAT
- trace boost runtime
- remote deploy runtime
- browser click E2E

따라서 전체 CPF는 최종 완료가 아니다.

## 1.3 저장소 구조상 확인된 gap

### BIZADM이 그대로 존재

현재 repository와 `settings.gradle`에는 다음이 남아 있다.

- `bizadm` module
- `cpf.bizadm` package
- `BizAdmApplication`
- `application-bizadm*.yml`
- `CPF-BIZADM`
- `CPF_BIZADM_*`
- README의 BIZADM 표기

사용자가 확정한 최종 이름은 `BZA`다. 이름 변경은 아직 구현되지 않았다.

### 초기 배포 대상과 다른 업무 모듈이 잔존

현재 settings와 repository에는 다음이 포함되어 있다.

- `acc`
- `exs`
- `bat`
- `bizadm`

사용자가 원하는 최초 기본 구성은 다음이다.

- library: PFW, CMN
- 기본 실행: ADM, BZA, MBR, XYZ
- 선택 실행: BAT
- ACC, EXS 등은 생성기로 필요할 때 생성

현 repository는 이 목표와 다르다.

### 작업 문서가 최상위에 잔존

- `CPF_NEW_REQUEST.md`
- `CPF_CODEX_HANDOFF.md`

최종 제품 저장소에는 작업 요청서·handoff가 정식 진입 문서처럼 남아 있으면 안 된다.

### README가 아직 최종 제품 안내서가 아님

현재 README는 개선됐지만 다음 문제가 있다.

- 최상단에 정본·완료 판정 문서를 바로 제시
- qualityGate·evidence·작업 검증 설명 비중이 큼
- ACC·EXS·BIZADM 중심 architecture
- ADM을 회원 운영까지 담당한다고 설명
- BZA가 아닌 BIZADM 사용
- 최초 배포 target과 불일치
- CPF의 제품 가치보다 개발·검증 절차 비중이 큼

README는 전면 재정리 대상이다.

### ADM ownership 혼합

ADM Controller inventory에 `AdmMemberController`가 존재한다.

ADM은 프레임워크 관리 콘솔이고 회원 업무 운영은 BZA 또는 MBR 업무영역이다. 실제 source와 service가 ADM에서 MBR public port를 조회하는 reference인지, 회원 업무를 직접 소유하는지 전수 확인하고 ownership을 분리해야 한다.

### BZA 기능 부족

현재 BIZADM에는 인증 관련 source가 보이나 다음은 직접 확인되지 않았다.

- 완성된 백오피스 main UI
- 메뉴 관리 UI
- 역할 관리 UI
- 버튼·API 권한 UI
- 직원·조직 UI
- 결재선
- 결재 요청·승인·반려
- audit UI
- download audit UI
- 공통 업무 화면 scaffold
- 상용 수준 디자인

현재 resources에도 static UI directory가 확인되지 않았으므로 BZA 제품 화면은 미구현 또는 재확인 필요다.

### BZA 인증 보안 gap

직접 확인한 `BizAdmAuthService`에는 다음이 존재한다.

- PBKDF2 password matching
- JWT access token
- refresh token hash DB 저장
- login history
- logout revoke

하지만 다음 문제가 있다.

- source에 local default JWT secret 문자열 존재
- HS256 단일 secret 중심
- refresh token rotation 없음
- refresh token reuse detection 없음
- refresh 시 신규 refresh token 미발급
- token family 없음
- device/session 관리 부족
- password change·reset·history의 BZA 구현 확인 필요
- bootstrap·최초 변경 흐름 확인 필요
- MFA·step-up 없음
- browser cookie/BFF 보안 미확인

### ADM 비밀번호 hash

ADM은 직접 확인 결과:

- PBKDF2WithHmacSHA256
- random salt
- work factor 120,000
- password history
- current password 검증
- 변경 후 session 처리 코드

를 가진다.

하지만 단순 SHA-256으로 변경하면 안 된다. 다음 요청에서 보안 기준을 상향한다.

### SQL 정리 필요

현재 `specs/sql`에는 다음이 존재한다.

- `40_business_modules_schema.sql`
- ACC·EXS·BIZADM 관련 schema 포함 가능성
- `archive` directory
- split SQL
- all_install
- Flyway

초기 배포 target 변경 후 SQL·Flyway·all_install·smoke가 함께 정리되어야 한다. 적용된 Flyway migration은 checksum과 기존 설치 호환성을 고려하지 않고 삭제하면 안 된다.

### script 과다 가능성

현재 commit에서 많은 일회성 export·sync·check·smoke script가 추가됐다. 최종 제품에 필요한 script와 작업 증적 생성용 일회성 script를 분리해야 한다.

---

# 2. 최종 목표

이번 요청은 다음을 목표로 한다.

1. CPF를 최초 배포 가능한 상용 framework baseline으로 정리
2. ADM을 프레임워크 관리 콘솔로 완성
3. BIZADM을 BZA로 완전 전환
4. BZA를 범용 업무 백오피스 기반으로 완성
5. 표준 온라인·배치 실행 ID 체계 구현
6. 표준 ID를 로그·ADM·배치·OpenAPI와 자동 연결
7. BZA 결재 capability와 reference scenario 구현
8. XYZ·BAT 범용 EDU 대폭 확대
9. 초기 배포 모듈 슬림화
10. domain generator 완성
11. README를 공식 제품 안내서로 전면 재작성
12. 불필요 source·SQL·script·문서·evidence 정리
13. MariaDB·browser·multi-instance·외부 infrastructure 검증
14. 최종 qualityGate와 self-review

이번 작업은 “기능을 추가한 뒤 문서만 갱신”하는 작업이 아니다. 실제 source·UI·SQL·runtime·browser·evidence가 완성돼야 한다.

---

# 3. 필수 완료 범위 A — BIZADM을 BZA로 완전 전환

## 3.1 명칭 전환

다음을 전수 변경한다.

- module directory: `bizadm` → `bza`
- Gradle project: `:bizadm` → `:bza`
- package: `cpf.bizadm` → `cpf.bza`
- application class: `BizAdmApplication` → `BzaApplication`
- artifact·bootJar
- application name
- registry service name
- log module code
- profile yml
- environment prefix
- port inventory
- deploy inventory
- SQL schema·table·column prefix
- Flyway location
- API path
- OpenAPI tag·operationId
- menu·permission code
- script parameter
- DOCX
- README
- evidence
- generator template
- qualityGate

최종 repository에서 대소문자를 포함해 `bizadm`, `BIZADM`, `BizAdm` 잔존을 검사한다.

정식 외부 배포 이력이 없으므로 불필요한 호환 alias를 남기지 않는다. 기존 DB migration 호환이 필요한 부분만 migration으로 처리한다.

## 3.2 BZA module code

- module code: `BZA`
- owner domain code: `BZA`
- environment prefix: `BZA_`
- JWT issuer·audience 등도 최종 naming과 일치
- 표준 ID의 BZA 공통 기능 DOMAIN은 `BZA`

---

# 4. 필수 완료 범위 B — 비밀번호·로그인·세션 보안

## 4.1 비밀번호 저장

단순 SHA-256, SHA-512 같은 fast hash를 사용하지 않는다.

우선순위:

1. Argon2id
2. FIPS 요구 시 PBKDF2-HMAC-SHA256
3. 프로젝트 환경에서 검증된 password hashing library

PBKDF2를 사용할 경우:

- 충분한 iteration
- unique random salt
- algorithm identifier
- work factor 저장
- hash version 저장
- 재로그인 시 rehash upgrade
- constant-time comparison
- password history
- raw password logging 금지
- hash·salt API 노출 금지
- DB dump·audit에 hash 노출 최소화
- pepper 적용 여부 검토
- pepper는 DB와 분리된 secret provider 사용

현재 ADM PBKDF2 120,000은 검토 후 상향하고 기존 hash를 로그인 시 점진적으로 재해시한다.

ADM과 BZA가 같은 PFW security password port를 사용하도록 ownership을 정리한다. ADM·BZA가 각각 password engine을 복제하지 않는다.

## 4.2 로그인

ADM과 BZA 모두:

- login ID
- password
- 활성 상태
- 잠금 상태
- 실패 횟수
- 실패 시 지연·rate limit
- 최대 실패 후 잠금
- 마지막 로그인
- client IP
- user agent
- transactionGlobalId
- login audit
- generic failure message
- credential stuffing 방어
- brute-force 방어
- session fixation 방지
- CSRF 적용
- secure cookie
- SameSite
- HttpOnly
- HTTPS secure flag
- CORS allowlist
- security header
- open redirect 차단

## 4.3 초기 bootstrap

- bootstrap은 명시적으로 enable된 경우만
- 초기 사용자 ID·비밀번호 source/SQL/README/DOCX 금지
- secret provider 또는 환경변수
- local/dev/stg/prod 정책 분리
- prod 자동 bootstrap 기본 금지
- idempotent
- 최초 로그인 강제 비밀번호 변경
- 초기 비밀번호 만료
- bootstrap 종료 후 secret 폐기 안내
- bootstrap audit
- 비밀번호 변경 전 일반 메뉴 접근 차단

## 4.4 비밀번호 기능

- 사용자 본인 변경
- current password 검증
- new/confirm 일치
- 복잡도
- 최소 길이
- 최대 길이
- username 포함 방지
- 공통·유출 비밀번호 차단 적용 검토
- history 재사용 금지
- 만료
- 최초 변경 강제
- 관리자 reset
- reset 시 임시 비밀번호 원문 저장 금지
- reset 후 강제 변경
- 변경·reset 후 기존 session·refresh token 전부 폐기
- 변경 audit
- 관리자 reset 사유
- 동일 사용자의 동시 변경 경쟁 제어

## 4.5 Access·Refresh Token

BZA와 ADM token 정책을 공통 PFW capability로 정리한다.

필수:

- 짧은 access token TTL
- refresh token rotation
- refresh token family
- 이전 refresh token 재사용 탐지
- reuse 탐지 시 family 전부 revoke
- refresh token hash 저장
- access token 원문 저장 금지
- token 원문 로그 금지
- logout revoke
- password 변경 시 revoke
- 계정 잠금·비활성 시 revoke
- issuer
- audience
- subject
- jti
- issuedAt
- expiresAt
- key ID
- signing key rotation
- HS256 고정 secret 제거 검토
- secret provider
- local default secret 제거
- session/device 목록
- 개별 session 종료
- 전체 session 종료
- idle timeout
- absolute timeout

---

# 5. 필수 완료 범위 C — ADM 프레임워크 관리 콘솔

ADM은 회원·계좌 같은 업무 운영 콘솔이 아니다. CPF framework와 platform runtime을 관리한다.

## 5.1 ADM 메뉴

최소 메뉴:

```text
ADM
├─ 대시보드
├─ 표준 실행 관리
├─ 거래 관제
├─ 서비스·인스턴스
├─ 로그
├─ 배치·Scheduler
├─ Center-cut
├─ Worker·Lock·Lease
├─ 신뢰성
├─ Broker·DLQ·Replay
├─ File Transfer·Archive
├─ 보안·권한
├─ 감사
├─ 설정·로그레벨
├─ Alert
└─ 시스템·OpenAPI·버전
```

## 5.2 상용 UI 디자인

ADM은 단일 긴 HTML이나 개발자 도구처럼 보이지 않게 한다.

필수:

- 공통 design system
- 좌측 navigation
- top header
- environment indicator
- production warning
- breadcrumb
- 사용자 메뉴
- notification
- dashboard card
- chart
- table
- search panel
- filter chip
- status badge
- pagination
- column chooser
- column width·order 저장
- detail drawer 또는 page
- tab
- timeline
- tree
- dependency graph
- loading skeleton
- empty state
- error state
- partial success
- toast
- confirmation modal
- dangerous action modal
- keyboard·focus
- responsive layout
- accessibility 기본
- date/time 표준
- timezone
- duration format
- transaction ID copy
- deep link
- browser back/forward
- route guard
- 권한 기반 메뉴·버튼 표시
- URL 직접 접근 서버 403

화면을 예쁘게만 꾸미지 말고 운영자가 장애를 찾고 조치할 수 있도록 정보 구조를 설계한다.

## 5.3 ADM 대시보드

- 서비스 정상·경고·장애
- instance online·offline·stale
- 거래량
- 성공률
- 오류율
- P95·P99
- timeout
- retry
- failover
- circuit open
- UNKNOWN
- reconciliation
- recovery
- poison
- outbox
- inbox
- DLQ
- scheduler 지연
- 배치 실패
- ghost batch
- stale worker
- center-cut
- file transfer
- log storage
- credential 만료
- 운영자 조치
- download audit

모든 dashboard card와 chart는 상세 목록으로 drill-down한다.

## 5.4 ADM ownership 정리

`AdmMemberController`를 포함한 업무 데이터 기능을 전수 검토한다.

- 프레임워크 운영 기능이면 ADM에 유지
- 회원 업무 운영이면 BZA 또는 MBR ownership으로 이동
- ADM이 MBR DB·repository·mapper를 직접 조회하면 제거
- ADM이 MBR public facade 상태를 관제하는 기능이면 framework 관제 목적을 명시
- 업무용 CRUD·상태 변경은 ADM에서 제거

---

# 6. 필수 완료 범위 D — 표준 온라인·배치 ID

## 6.1 최종 ID 형식

```text
[O|B][DOMAIN 3]-[BUSINESS 3]-[SUB 2]-[SEQ 4]
```

정확히 16자.

정규식:

```regex
^[OB][A-Z]{3}-[A-Z0-9]{3}-[A-Z0-9]{2}-[0-9]{4}$
```

예:

```text
OMBR-MEM-QY-0001
OBZA-USR-QY-0001
OBZA-APR-AP-0001
BMBR-MEM-SY-0001
BACC-SET-CL-0001
```

구간:

- `O`: online
- `B`: batch
- DOMAIN: 주제영역 3자리
- BUSINESS: 업무기능군 3자리
- SUB: 세부 행위 2자리
- SEQ: 0001~9999

## 6.2 ID code catalog

DOMAIN·BUSINESS·SUB는 DB·source·ADM에서 동일한 code catalog를 사용한다.

SUB 예:

- QY 조회
- DT 상세
- RG 등록
- MD 수정
- DL 삭제
- AP 승인
- RJ 반려
- CN 취소
- VF 검증
- DW 다운로드
- UL 업로드
- SY 동기화
- CL 마감
- RC 대사
- IM 적재
- OT 추출
- PG 정리
- AR 보관
- RT 재처리
- AG 집계

코드 충돌을 금지한다.

## 6.3 주제영역 ownership

해당 주제영역 source가 다른 주제영역 ID를 소유하면 오류다.

예:

- MBR source → `OMBR-*`, `BMBR-*`
- BZA 공통 source → `OBZA-*`, `BBZA-*`
- XYZ EDU → `OXYZ-*`
- BAT EDU → `BBAT-*`

BZA가 MBR 업무 백오피스 기능을 제공하는 경우:

- owner domain: MBR
- source module: BZA
- 해당 package·menu·ownership manifest에 MBR이 명시적으로 허용돼야 함
- BZA가 임의의 domain ID를 사용할 수 없음

다른 주제영역 호출은 target ID 참조로 처리하며 source annotation ownership과 혼동하지 않는다.

## 6.4 Source annotation

온라인·배치 source에 annotation 또는 동등한 compile-time metadata를 선언한다.

예:

```java
@CpfOnlineTransaction(
    id = "OMBR-MEM-QY-0001",
    name = "회원 목록 조회"
)
```

```java
@CpfBatchJob(
    id = "BMBR-MEM-SY-0001",
    name = "회원 동기화"
)
```

annotation은 자동 inventory와 runtime context에 사용한다.

annotation만 정본으로 삼지 않는다.

```text
annotation
+ Controller mapping
+ Facade/Port
+ OpenAPI operationId
+ Batch Job/Step metadata
+ build manifest
+ ADM catalog
```

을 상호 검증한다.

## 6.5 자동 등록 시점

권장 흐름:

1. 개발자가 annotation에 ID 선언
2. compile·test에서 규격과 ownership 검증
3. build 시 module execution manifest 생성
4. application startup 시 PFW registration port를 통해 ADM catalog에 idempotent upsert
5. runtime 호출 시 log·metric·audit와 연결
6. ADM에서 운영 metadata 보완

runtime 최초 호출만을 등록 수단으로 사용하지 않는다. 호출되지 않은 거래도 build/startup 시 등록돼야 한다.

## 6.6 ADM 표준 실행 관리

메뉴:

- 전체 실행 ID
- 온라인 거래
- 배치
- 미등록·자동 발견
- Source 변경
- 중복·충돌
- 배치 의존관계
- 표준화 현황

목록 필수 컬럼:

- standardExecutionId
- 이름
- O/B
- owner domain
- source module
- business
- sub
- sequence
- 등록 상태
- class·method 또는 Job
- endpoint
- operationId
- version
- commit
- 최초 등록
- 마지막 발견
- 마지막 호출·실행
- 담당자
- binding 상태

기능:

- 다음 sequence 추천
- ID 실시간 미리보기
- 중복 검사
- annotation 예제 복사
- 자동 발견 source 등록
- 기존 ID와 연결
- source binding 수정
- alias
- 폐기
- exception 승인
- 담당자 지정
- source change 확인
- 로그·거래·배치로 이동
- 등록률 통계

## 6.7 미등록 처리

표준 ID가 없더라도 runtime 로그와 실행 이력을 버리지 않는다.

ADM 표시:

- ID: 미등록
- 상태: AUTO_DISCOVERED 또는 MAPPING_REQUIRED
- endpoint·class·Job·Step 표시
- 로그·오류·metric·runtime 정상 표시
- 신규 등록
- 기존 정의 연결
- 예외 승인

신규·변경 source는 ID annotation이 없으면 qualityGate 실패한다. 기존 legacy는 만료일 있는 예외만 허용한다.

---

# 7. 필수 완료 범위 E — 표준 ID와 로그·관제 자동 연계

## 7.1 온라인 로그

구조화 필드:

- standardTransactionId
- transactionGlobalId
- transactionId
- segmentId
- parentSegmentId
- sourceModule
- targetModule
- sourceInstanceId
- selectedInstanceId
- traceId
- spanId
- operationId
- status
- resultCode
- failureStage
- duration
- retryCount
- failoverCount
- circuitState

## 7.2 배치 로그

- standardBatchId
- JobInstanceId
- JobExecutionId
- StepExecutionId
- businessDate
- schedulerId
- workerId
- lockKey
- leaseOwner
- instanceId
- transactionGlobalId
- status
- readCount
- writeCount
- skipCount
- retryCount
- commitCount
- rollbackCount
- checkpoint
- ghostState

## 7.3 연결 범위

표준 ID는 다음에 자동 전파된다.

- file log
- DB log
- segment
- timeline
- metric
- audit
- alert
- scheduler execution
- JobInstance
- JobExecution
- StepExecution
- remote log artifact
- recovery
- poison
- outbox
- inbox
- DLQ
- reconciliation
- file transfer
- download audit

업무 개발자가 로그 문장마다 ID를 수동 입력하지 않는다.

## 7.4 내부 헤더 strict validation

CPF 내부 HTTP 호출은 표준을 지키지 않으면 오류 처리한다.

- standardExecutionId 누락
- 형식 오류
- 미등록 ID
- 폐기 ID
- endpoint·operationId 불일치
- source domain 불일치
- source·target module 불일치
- transactionGlobalId 누락·형식 오류
- protocol version 오류
- 허용되지 않은 확장 header
- token claim·header 불일치

외부 요청은 ingress에서 endpoint binding으로 standard ID를 결정하고 CPF context를 생성한다. 외부 client에게 내부 header 전체를 요구하지 않는다.

---

# 8. 필수 완료 범위 F — ADM 원격 로그 관리

구조:

```text
ADM
→ PFW RemoteLogArtifactPort
→ Registry
→ 대상 실행 인스턴스 LogArtifactAdapter
→ allowlisted log root
```

필수 검색:

- environment
- module
- service
- instance
- standardTransactionId
- standardBatchId
- transactionGlobalId
- transactionId
- segmentId
- JobInstanceId
- JobExecutionId
- StepExecutionId
- schedulerId
- time range
- log type
- file name
- active/archive
- size
- compression

목록 컬럼:

- environment
- module
- service
- instance
- online status
- standard ID
- transactionGlobalId 또는 JobInstanceId
- file name
- safe relative path
- size
- modified time
- compressed
- checksum
- active
- retention expiry
- masking policy
- downloadable

상세:

- metadata
- first/last log time
- last N lines
- time range preview
- keyword search
- error highlight
- standard ID highlight
- masking
- streaming
- active log snapshot

다운로드:

- single
- selected ZIP
- transactionGlobalId multi-instance ZIP
- standardBatchId·JobInstanceId worker ZIP
- gzip original
- checksum manifest
- partial success
- failed instance list
- async download job
- expiry
- redownload
- audit

보안:

- absolute path 금지
- traversal
- symlink·junction
- extension allowlist
- size limit
- time limit
- concurrency
- rate limit
- timeout
- mTLS/service token
- secret provider
- reason
- permission
- masking
- Content-Disposition 검증

---

# 9. 필수 완료 범위 G — 배치·Scheduler·Ghost·Dependency

## 9.1 배치 표준 정의

배치 목록과 상세에 다음이 필요하다.

- standardBatchId
- 배치명
- owner domain
- source module
- actual Job name
- source class
- Step structure
- scheduler
- businessDate
- restartable
- rerun
- failed-item retry
- batchType
- lock
- lease
- ghost policy
- max runtime
- SLA
- alert
- 담당자

## 9.2 Scheduler

- scheduler ID
- target standardBatchId
- cron
- timezone
- next run preview
- effective dates
- active
- misfire
- concurrency
- lock timeout
- max runtime
- parameter template
- notification
- pause
- resume
- immediate run
- one-time run
- dry-run
- audit

## 9.3 선행·후행·Trigger

모든 관계는 `standardBatchId`로 설정한다.

금지:

- raw class name
- raw Job Bean name
- source path
- Controller 직접 호출
- 코드 내부 임의 dependency

필드:

- predecessorStandardBatchId
- successorStandardBatchId
- triggerCondition
- businessDatePolicy
- parameterMapping
- timeout
- failurePolicy
- manualOverridePolicy
- version
- effective dates

기능:

- single predecessor
- multi predecessor
- fan-in
- fan-out
- success condition
- completed condition
- failure trigger
- file arrival
- data ready
- manual approval
- cycle detection
- orphan detection
- graph
- 현재 충족 상태
- 참조한 JobExecution
- manual override reason·audit

## 9.4 JobInstance·Execution·Step

목록·상세에 다음을 포함한다.

- JobInstanceId
- JobExecutionId
- StepExecutionId
- standardBatchId
- businessDate
- status
- start/end
- duration
- attempt
- instance
- worker
- transactionGlobalId
- parameter
- progress
- read/write/filter/skip
- commit/rollback
- retry
- checkpoint
- exit code
- exit message
- error
- logs
- lock·lease
- restart point

운영 액션:

- manual run
- pause
- resume
- graceful stop
- forced stop
- restart
- rerun
- step restart
- failed-item retry
- compensation
- stale lock release
- log download

## 9.5 Ghost

판정 신호:

- heartbeat timeout
- lease expiry
- stale lock
- registry offline
- process/port absence
- log inactivity
- DB state unchanged
- worker claim expiry
- scheduler state mismatch

상태:

- 정상
- 지연
- 고스트 의심
- 고스트 확정
- 복구 중
- 복구 완료
- 수동 확인 필요

조치:

- recheck
- health check
- log collection
- confirm ghost
- release lock·lease
- reclaim PROCESSING item
- reconcile JobExecution·StepExecution
- restart from checkpoint
- fail
- compensate
- quarantine
- owner assignment
- audit

단일 timeout만으로 자동 확정하지 않는다.

---

# 10. 필수 완료 범위 H — BZA 범용 백오피스

## 10.1 BZA 역할

BZA는 주제업무 영역 백오피스 기반이다.

BZA가 제공하는 공통 기능:

- 백오피스 사용자
- 직원 profile
- 조직
- 직급·직책
- 역할
- 메뉴
- 화면
- 버튼
- API 권한
- data scope
- 업무 감사
- download audit
- 공통 CRUD scaffold
- approval
- notification
- attachment
- saved search
- dashboard

특정 업무는 generated domain과 연결하며 BZA core가 MBR repository에 직접 의존하지 않는다.

## 10.2 BZA UI

로그인부터 운영까지 완성한다.

- login
- first password change
- dashboard
- navigation
- user menu
- my profile
- password change
- session list
- logout
- user list·detail
- role list·detail
- menu tree editor
- permission matrix
- button·API permission
- employee·department
- approval
- audit
- download audit
- code/message lookup
- notification
- common business page scaffold

ADM과 동일 design system을 공유하되 BZA는 업무 백오피스에 맞는 정보구조를 갖는다.

## 10.3 사용자·직원

- BackofficeUser
- EmployeeProfile
- Department
- Position
- JobTitle
- manager
- employment status
- join/leave date
- contact
- user-account binding
- role
- delegated approver
- absence period
- use/lock
- audit

외부 HR·directory와 연계할 수 있도록 port를 제공한다. 기본 설치는 BZA 자체 DB adapter를 제공한다.

## 10.4 메뉴·권한

- menu tree
- route
- icon
- order
- active
- role assignment
- user override 적용 여부
- screen permission
- button permission
- API permission
- environment
- domain
- data scope
- effective permission
- role compare
- permission simulation
- before/after
- audit

UI 숨김만으로 권한을 처리하지 않고 API server에서 반드시 검사한다.

---

# 11. 필수 완료 범위 I — BZA 결재 capability

## 11.1 결재 기능

- approval template
- approval line
- request
- draft
- submit
- review
- agree
- approve
- reject
- withdraw
- cancel
- resubmit
- delegate
- substitute approver
- parallel
- sequential
- all approve
- any approve
- maker-checker
- same-person prohibition
- due date
- reminder
- escalation
- attachment
- comment
- before/after diff
- optimistic lock
- duplicate action prevention
- audit
- transactionGlobalId
- notification
- statistics

## 11.2 결재선 설정

등록된 BZA 직원과 연결한다.

- employee
- department
- position
- manager
- role
- amount threshold
- risk level
- business domain
- request type
- condition
- effective dates
- absence
- delegation
- fallback approver

## 11.3 상태

예:

- DRAFT
- SUBMITTED
- IN_REVIEW
- APPROVED
- REJECTED
- WITHDRAWN
- CANCELED
- EXPIRED

상태전이는 state machine과 optimistic lock으로 보호한다.

## 11.4 Reference scenario

특정 MBR·ACC 업무에 종속되지 않는 중립 sample 하나를 BZA reference로 제공한다.

예:

```text
기준정보 변경 요청
→ 담당자 검토
→ 팀장 승인
→ 적용
```

BZA core와 reference data를 분리한다.

- reference seed 제거 가능
- BZA core는 reference에 의존하지 않음
- 사용하지 않는 프로젝트는 reference만 제거 가능
- 기능 자체는 configuration으로 disable 가능

BZA에 `edu` package를 만들지 않는다.

---

# 12. 필수 완료 범위 J — 초기 배포 모듈 슬림화

## 12.1 최종 baseline

### library

- PFW
- CMN

### 기본 실행

- ADM
- BZA
- MBR
- XYZ

### 선택 실행

- BAT

### 기본 repository에서 제거 또는 template화

- ACC
- EXS
- 기타 업무 domain

## 12.2 ACC·EXS 제거 전 검수

무조건 삭제하지 않는다.

다음 중요한 구현을 inventory한다.

- Service Call sample
- Remote Facade Proxy
- failover
- external integration
- file transfer
- broker
- fixed-length
- unknown·reconciliation
- tests
- SQL
- OpenAPI
- runtime smoke

소유권에 따라 이동한다.

- 기술 engine·port → PFW
- 공통 helper → CMN
- 온라인 EDU → XYZ
- 배치 EDU → BAT
- 백오피스 공통 → BZA
- 업무 reference → MBR 또는 generator template
- 불필요 sample → 삭제

이동 후 ACC·EXS 의존이 남지 않아야 한다.

## 12.3 BAT

BAT는 source와 bootJar를 유지하되 optional deployment로 관리한다.

- default startup에 포함하지 않을 수 있음
- batch 필요 프로젝트가 선택
- BAT EDU와 기본 batch runtime 제공
- SQL도 optional package와 core metadata 분리

---

# 13. 필수 완료 범위 K — Domain Generator

`create-domain`은 ACC·EXS 제거 후 실제 domain 생성의 정식 수단이다.

입력:

- moduleCode
- domainName
- domainIdCode 3자리
- basePackage
- port
- tablePrefix
- online 여부
- batch 여부
- BZA menu 연계 여부

생성:

- Gradle module
- source
- Controller
- Facade
- Port
- local adapter
- remote proxy
- standard ID annotation
- OpenAPI
- profile yml
- bootJar
- SQL
- Flyway
- smoke
- test
- ADM catalog manifest
- registry
- BZA menu registration candidate
- log
- permission
- ownership gate
- README snippet가 아닌 공식 generator guide 반영

실제 LNG 같은 domain을 생성해:

- compile
- test
- bootJar
- Java 25
- OpenAPI
- startup
- registry
- ADM catalog
- standard ID
- log
- SQL merge
- ownership

를 검증하고 생성 결과를 삭제한다.

---

# 14. 필수 완료 범위 L — XYZ·BAT EDU 확대

## 14.1 필수 XYZ EDU

- JWT create·validate
- OAuth Authorization Server
- Authorization Code + PKCE
- Client Credentials
- Resource Server
- Access Token
- Refresh Token
- Refresh Rotation
- Reuse Detection
- Revocation
- Introspection
- JWKS
- Key Rotation
- OIDC
- BFF
- API Key
- HMAC
- mTLS
- Webhook sender
- Webhook receiver
- async 202 job
- SSE
- WebSocket
- gRPC
- GraphQL
- idempotency
- outbox
- inbox
- DLQ
- replay
- saga·compensation
- CloudEvents format
- AsyncAPI example
- timeout·retry·failover·circuit
- bulkhead
- rate limit
- load shedding
- deadline propagation
- CRUD
- validation
- optimistic lock
- keyset pagination
- ETag
- JSON Patch
- Problem Details
- Bulk API
- import validation
- async export
- field encryption
- tokenization
- retention
- feature flag
- leader election
- request deduplication
- request coalescing
- quota
- maintenance mode
- graceful drain
- cancellation
- API composition
- CQRS read model
- audit reconstruction
- approval client example
- notification port
- template
- one-time download token
- object versioning
- search port
- time-series
- state machine
- localization
- money
- business calendar
- sequence
- reference cache
- structured log
- trace
- metric
- health
- dynamic log level
- file upload
- streaming download
- range request
- checksum
- archive
- path security
- contract test
- fake external server
- OpenAPI client

## 14.2 AI EDU 착수

외부 provider 자격정보 없이도 contract와 deterministic adapter를 구현한다.

- LLM provider port
- structured output
- JSON schema validation
- streaming
- tool calling
- masking
- prompt injection defense
- output validation
- timeout
- retry
- circuit
- model fallback
- token usage metric
- embeddings port
- vector store port
- simple RAG
- source citation
- async AI job
- human approval
- ADM observability

실 provider runtime은 자격정보 없으면 미검증으로 남긴다.

## 14.3 BAT EDU

- tasklet
- chunk
- partition
- parallel worker
- checkpoint
- restart
- ghost
- lock
- lease
- stale worker
- file import
- DB export
- bulk API
- rate limit
- reconciliation
- archive
- purge
- statistics
- dependency chain
- conditional step
- cutoff
- business date
- SLA
- expected finish

## 14.4 EDU 완료 묶음

각 sample:

- real PFW/CMN capability
- sampleId
- source
- configuration
- OpenAPI
- normal test
- failure test
- security test
- runtime smoke
- transactionGlobalId
- standard execution ID
- log
- masking
- audit
- ADM 확인
- sample coverage
- DOCX catalog 최소 갱신
- evidence

---

# 15. 필수 완료 범위 M — README 전면 재작성

README는 작업·검수 문서가 아니다.

## 15.1 목적

CPF를 처음 접하는 개발자·아키텍트·운영자에게 다음을 설명한다.

- CPF가 무엇인가
- 어떤 문제를 해결하는가
- 어떤 시스템을 만들 수 있는가
- 어떤 핵심 기능을 제공하는가
- 모듈이 어떻게 나뉘는가
- 어떻게 실행하는가

## 15.2 첫 화면

```text
CPF
Core Business Platform Framework

금융권을 포함한 범용 업무 시스템을
구축·운영·감사·확장·검증할 수 있는
MSA-first Core Business Platform Framework
```

첫 화면에서 바로 보여야 할 내용:

- MSA-first
- modular-monolith compatible
- multi-instance
- Java 25
- transaction trace
- reliability
- security
- batch
- ADM
- BZA
- EDU
- generator

## 15.3 구성

1. CPF 소개
2. 핵심 가치
3. architecture
4. module map
5. online transaction flow
6. Service Call Engine
7. transactionGlobalId·segment·timeline
8. security·permission·audit·masking
9. idempotency·outbox·inbox·DLQ
10. unknown·reconciliation
11. file transfer·archive
12. batch·center-cut
13. ADM
14. BZA
15. XYZ·BAT EDU
16. domain generator
17. quick start
18. OpenAPI
19. official guides
20. verified status summary

## 15.4 제외

- Codex
- request SHA
- handoff
- WIP
- 작업 재개 방법
- milestone
- 임시 evidence 장문
- 중간 gap
- 이번 변경 파일 목록

검증 상태는 짧은 summary만 제공하고 상세는 공식 report로 연결한다.

## 15.5 architecture

ACC·EXS·BIZADM 중심 diagram을 제거한다.

baseline:

```text
Client
→ MBR / generated domains
→ PFW Service Call
→ generated local/remote domain
ADM → framework runtime
BZA → business backoffice
XYZ → online EDU
BAT → optional batch runtime
PFW/CMN → library
```

---

# 16. 필수 완료 범위 N — 저장소 정리

## 16.1 문서

삭제·이관 후보:

- `CPF_NEW_REQUEST.md`
- `CPF_CODEX_HANDOFF.md`
- WIP
- 이전 요청서
- 작업 재개 문서
- 중복 목표 문서
- stale report
- raw temporary log
- 실패 변환 파일
- 임시 HTML

정본과 공식 운영 문서는 유지한다.

새 문서를 대량 생성하지 않는다.

최소 문서:

- README
- 정본
- completion guide
- final report
- gap
- evidence index
- 기능 matrix
- sample coverage
- 공식 DOCX

## 16.2 SQL

전수 inventory:

- split SQL
- Flyway
- all_install
- all_install_and_smoke
- smoke
- archive
- test data
- seed
- removed module SQL
- duplicate SQL

원칙:

- 적용된 Flyway migration 무단 삭제 금지
- 기존 설치 checksum 고려
- baseline module과 optional module 분리
- ACC·EXS·BIZADM SQL 제거 또는 migration
- BZA rename migration
- all_install 재생성
- full install 재실행
- repeat install
- Flyway migrate
- FK/index/comment
- app/migration 권한
- smoke

`archive` SQL이 실제 필요 없으면 제거하고, 보존 이유가 있으면 공식 기준을 명확히 한다.

## 16.3 Script

script별로 분류한다.

- product operation
- install
- start/stop
- test
- smoke
- generator
- qualityGate
- one-time evidence
- obsolete
- duplicate

삭제 후보:

- 호출되지 않는 script
- 일회성 export
- 이전 request 전용
- HTML 전용
- stale path
- 중복 check
- 폐기 module 전용
- 임시 probe

남는 script는:

- 목적
- input
- output
- exit code
- secret handling
- usage
- referenced guide

가 명확해야 한다.

## 16.4 Source·test

- unused class
- dead controller
- placeholder
- duplicated engine
- wrong ownership
- ACC·EXS sample
- BIZADM residue
- empty package
- stale test
- disabled test
- ignored test
- generated temporary source

를 정리한다.

---

# 17. 필수 완료 범위 O — MariaDB·Browser·Runtime 검증

## 17.1 MariaDB

기설치 MariaDB에 접근 가능한 경우 신규 빈 schema로 실행한다.

- full install
- repeat install
- account creation
- permission
- app user DML
- migration user DDL
- forbidden DDL
- FK
- index
- comments
- seed
- smoke
- Flyway migrate
- BZA rename migration
- standard execution catalog
- approval
- transaction log
- batch
- ghost
- remote log audit

접속 불가면 설치하지 말고 preflight·재현 명령·미검증 상태를 남긴다.

## 17.2 Browser

ADM·BZA 실제 browser E2E:

- bootstrap
- login
- first password change
- logout
- session expiry
- lock
- unlock
- reset
- role
- menu
- button
- API 403
- standard execution catalog
- transaction list·detail
- remote log
- batch
- scheduler
- ghost
- BZA employee
- permission
- approval
- audit
- download audit
- empty
- error
- timeout
- partial success
- console error 0
- dead button 0
- broken link 0
- mojibake 0

## 17.3 Multi-instance

최소 2개 instance:

- registry
- selectedInstanceId
- transaction propagation
- standard ID
- failover
- circuit
- remote log bundle
- batch lease
- worker claim
- ghost false positive
- graceful shutdown

## 17.4 External infrastructure

가능한 환경에서:

- Redis
- Kafka
- RabbitMQ
- SFTP
- FTP
- FTPS
- SCP
- SSH

를 실제 검증한다.

환경 미제공 시 source·contract·deterministic adapter는 검증하되 실 runtime 완료로 올리지 않는다.

---

# 18. OpenAPI

모든 REST API:

- `@Tag`
- `@Operation`
- unique operationId
- request schema
- response schema
- examples
- standard error
- 400
- 401
- 403
- 404
- 409
- 429
- 500
- 503
- standard headers
- standard execution ID
- transactionGlobalId
- permission
- masking
- audit
- idempotency
- async result
- partial success

ADM·BZA·MBR·XYZ와 generated domain runtime `/v3/api-docs`를 검증한다.

---

# 19. QualityGate 보강

다음을 자동 검사한다.

## 19.1 Naming·ownership

- BIZADM residue
- ACC·EXS residue
- forbidden domain dependency
- ADM business ownership violation
- BZA framework ownership violation
- PFW business dependency
- CMN business implementation dependency
- Spring Event misuse

## 19.2 Standard execution ID

- 16-char regex
- O/B position
- domain code
- business code
- sub code
- sequence 0000
- duplicate
- annotation missing
- module/domain mismatch
- BZA allowed domain manifest
- online ID on batch
- batch ID on online
- OpenAPI mapping
- Job mapping
- startup manifest
- runtime log propagation
- legacy exception expiry

## 19.3 Security

- raw SHA password hash 금지
- plaintext password
- default JWT secret
- hardcoded secret
- token logging
- refresh rotation
- password work factor
- weak bootstrap
- missing audit
- missing 403 test

## 19.4 UI

- dead route
- dead button
- missing API mapping
- missing menu permission
- duplicate menu
- broken asset
- console error evidence
- accessibility baseline

## 19.5 Repository cleanup

- obsolete script
- WIP
- handoff
- request docs
- HTML
- empty dir
- build output
- runtime log
- stale evidence
- removed module references
- unused SQL
- all_install mismatch

---

# 20. 문서·Evidence 최소화

이번 작업은 문서 생산량을 늘리지 않는다.

최소 갱신:

- README
- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- 기능 matrix
- sample coverage
- 기존 공식 DOCX 중 실제 내용이 변경된 문서만 갱신

신규 HTML 금지.

추가 DOCX는 기존 공식 DOCX에 흡수할 수 없을 때만 만든다.

raw evidence를 무제한 저장하지 않는다.

evidence는:

- sanitized
- reproducible
- branch/SHA
- timestamp
- environment
- command
- exit code
- result
- secret removed

를 갖는다.

---

# 21. 완료 판정

다음이 모두 충족되어야 최종 완료 후보다.

- BZA rename
- BZA UI·security·approval
- ADM framework console
- standard online/batch ID
- source annotation
- startup catalog registration
- log·metric·audit propagation
- remote log
- scheduler·dependency
- ghost
- initial deployment modules
- ACC·EXS cleanup
- generator
- XYZ·BAT EDU
- README
- SQL cleanup
- script cleanup
- MariaDB
- browser
- multi-instance
- OpenAPI
- qualityGate
- matrix·evidence consistency

외부 infrastructure 미제공 항목은 `미검증`으로 남긴다.

---

# 22. 작업 순서 권고

## Phase 1 — 전수 inventory와 migration plan

- latest master SHA
- module dependency
- BIZADM references
- ACC·EXS assets
- ADM ownership
- SQL
- scripts
- EDU
- UI
- security
- standard ID existing work

삭제 전에 이동·보존 대상을 확정한다.

## Phase 2 — PFW 공통 기반

- password port
- session/token
- standard execution ID
- annotation
- build manifest
- startup registration
- runtime context
- header validation
- remote log port
- approval support port 중 공통 영역

## Phase 3 — ADM

- catalog
- dashboard
- transaction
- service
- log
- batch
- scheduler
- ghost
- reliability
- security
- design

## Phase 4 — BZA

- rename
- auth
- user·employee
- organization
- role·menu·permission
- approval
- UI
- audit

## Phase 5 — module cleanup·generator

- ACC·EXS migration
- default module set
- optional BAT
- generator
- SQL
- deploy
- scripts

## Phase 6 — EDU·README

- XYZ/BAT EDU
- README
- official guide minimal update

## Phase 7 — runtime verification

- test
- bootJar
- Java 25
- MariaDB
- browser
- multi-instance
- OpenAPI
- broker·file server if available
- qualityGate
- self-review

---

# 23. 최종 보고 형식

최종 보고는 과장 없이 다음만 제공한다.

1. 실제 변경 요약
2. 완료
3. 부분 구현
4. 미구현
5. 미검증
6. 실패
7. 재확인 필요
8. 실행한 검증
9. 실행하지 못한 검증
10. 주요 evidence
11. 삭제·이동한 파일
12. 남은 위험
13. 작업트리 상태

Codex 성공 보고만으로 완료 처리하지 않는다.

커밋·push는 하지 않는다.
