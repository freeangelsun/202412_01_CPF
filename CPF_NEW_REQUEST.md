# CPF 차기 대형 작업 요청서

## 1. 작업명

**CPF Reliability Capability 실연결·기설치 MariaDB 실검증·ADM/EDU/OpenAPI 완성 대형 마일스톤**

## 2. 작업 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 commit: `7fb5515acbfea18166dc9eef947fb5371aab191d`
- 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검색 보조: `CPF_FINAL_TARGET_REQUIREMENTS_01.md` ~ `CPF_FINAL_TARGET_REQUIREMENTS_05.md`
- 운영·검수 기준: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 반드시 대조할 현황 파일:
  - `CPF_STABILIZATION_REPORT.md`
  - `CPF_GAP_MATRIX.md`
  - `CPF_EVIDENCE_INDEX.md`
  - `specs/기능_구현_매트릭스.html`
  - `specs/sample-coverage-matrix.md`

작업 시작 전에 위 파일과 이번 작업 관련 source/test/sql/script/config/evidence를 실제로 읽고 대조한다. 기존 보고 문구나 상태값을 그대로 신뢰하거나 이어붙이지 말고 최신 소스와 실행 evidence 기준으로 판단한다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`가 정본이다. 원본 확인이 불안정한 경우 `_01`~`_05`를 검색 보조로 함께 확인하되 목표 범위를 현재 구현 수준에 맞춰 축소하지 않는다.

## 3. 작업 배경과 이번 검수 결과

직전 작업에서 다음 기반은 실제 추가됐다.

- PFW idempotency 공통 모델, port, JDBC reference repository
- PFW broker outbox/inbox/DLQ/replay JDBC repository
- PFW file transfer history/duplicate prevention JDBC repository
- PFW unknown result/reconciliation 공통 모델, port, JDBC repository
- 관련 단위 테스트
- 신규 persistence SQL/Flyway/all_install 반영

그러나 최신 master 검수 결과 다음 gap이 남아 있다.

- 신규 SQL 변경 이후 기설치 MariaDB에서 fresh schema/full SQL/Flyway V22 실제 실행 증적 부재
- JDBC repository가 실제 engine/worker/거래 흐름과 충분히 연결되지 않음
- 신규 경로의 transactionGlobalId/segment/timeline/header propagation 미검증
- 신규 capability용 XYZ/BAT 기능별 EDU 샘플과 EDU 테스트 부족
- 신규 ADM 운영 API/UI, 권한, 감사, 수동처리/replay 흐름 부족
- 전체 REST OpenAPI coverage와 operationId 검증 부족
- `CPF_NEW_REQUEST.md` 보호 방식이 작업 전 baseline을 독립적으로 보장하지 못함
- report/gap/matrix에서 fresh install, EDU, OpenAPI, 신규 tracing 등을 과대 완료 처리한 부분 존재
- Java 25 compile/test/bootJar/qualityGate 미검증

이번 작업은 새 기능을 무작정 넓히기보다 직전 구현을 실제 플랫폼 흐름에 연결하고 실행 evidence까지 닫는 대형 보강 마일스톤이다.

## 4. 기설치 MariaDB 사용 조건 및 Codex 수행 범위

사용자 로컬 개발 PC에는 MariaDB가 이미 설치되어 있다. **이번 요청은 MariaDB 소프트웨어 설치·삭제·버전 업그레이드를 요구하지 않는다.** Codex는 현재 작업 셸에서 접근 가능한 기존 MariaDB 인스턴스를 사용해 CPF SQL/Flyway/통합 검증을 수행한다.

- 기존 repository의 local profile, 환경변수, 실행 스크립트, 비추적 로컬 설정과 문서를 먼저 확인한다.
- 현재 셸에서 접속 가능한지 비파괴 preflight를 먼저 수행한다.
- 접속 가능하면 전용 신규 빈 검증 DB/schema를 생성해 CPF 전체 schema 설치와 Flyway baseline/upgrade를 검증한다. 기존 업무 데이터가 있는 DB/schema는 사용하거나 훼손하지 않는다.
- 접속정보를 소스, 문서, 로그, evidence에 원문으로 기록하지 않는다.
- 비밀번호·토큰·개인 경로 등 민감정보는 커밋하지 않는다. 환경변수 또는 로컬 비추적 설정으로만 주입한다.
- DB 서비스 중지, 계정정보 부재, 권한 부족 등으로 현재 셸에서 접속할 수 없으면 MariaDB 설치나 임의 환경 변경을 시도하지 않는다. 대신 preflight 결과, 필요한 환경변수명, 재현 명령, 실패 원인을 evidence에 남기고 DB runtime 항목은 `미검증` 또는 `재확인 필요`로 유지한다.
- H2, mock, SQL 문자열 파싱만으로 MariaDB 실검증 완료를 주장하지 않는다.
- 작업에 필요한 source/test/SQL/script/config/Markdown 파일은 요청 범위 안에서 별도 확인 없이 생성·수정한다. 단, 신규 HTML은 상태 정합성 유지에 꼭 필요한 경우만 수정하고 PDF는 생성하지 않는다.

## 5. 필수 완료 범위

### 5.1 상태·evidence 신뢰성 우선 복구

직전 검수에서 확인된 과대 완료 상태를 실제 구현과 evidence 기준으로 정정한다.

필수 조치:

- `CPF_STABILIZATION_REPORT.md` 전체 상태 재판정
- `CPF_GAP_MATRIX.md` 관련 행 재판정
- `CPF_EVIDENCE_INDEX.md` 실제 경로와 상태 정합성 복구
- `specs/기능_구현_매트릭스.html` 상태 일치
- `specs/sample-coverage-matrix.md` 실제 sample class/test/evidence 존재 여부와 일치
- 없는 evidence, stale evidence, 변경 이전 SQL evidence를 현재 완료 근거로 사용하지 않기
- 기존 기능의 이전 evidence와 이번 변경의 신규 evidence를 구분

상태값은 다음 6개만 사용한다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

특히 다음은 실제 실행 전까지 완료로 두지 않는다.

- 신규 SQL 반영 후 기설치 MariaDB 기반 CPF full schema install
- Flyway V22 적용
- 신규 reliability 경로 runtime
- 신규 EDU coverage
- 신규 ADM 운영 흐름
- 전체 OpenAPI coverage
- Java 25

### 5.2 기설치 MariaDB를 이용한 신규 빈 schema 설치·Flyway·smoke 실검증

MariaDB 소프트웨어를 새로 설치하지 않는다. 현재 작업 셸에서 접근 가능한 기설치 MariaDB를 사용해 직전 신규 persistence SQL을 포함한 **CPF schema 설치 체인**을 실제 검증한다. 접속 preflight가 실패하면 구현·스크립트 보강까지 수행하고 runtime 상태는 정확히 `미검증`으로 남긴다.

필수 검증:

- 비파괴 접속 preflight
- 신규 빈 검증 DB/schema 준비
- split SQL 기반 CPF schema 설치
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- Flyway baseline 기반 CPF schema 설치 경로
- 별도 검증 schema에서 이전 migration 상태를 구성한 뒤 Flyway `V22__pfw_reliability_persistence.sql` upgrade 경로
- 신규 6개 테이블 생성 확인
- PK/UK/index/NOT NULL/default/상태 컬럼 확인
- 재실행 정책 또는 중복 실행 실패가 의도와 일치하는지 확인
- 검증용 DB/schema cleanup 절차. 기존 사용자 DB/schema 삭제 금지

신규 테이블:

- `pfw_idempotency_record`
- `pfw_broker_outbox`
- `pfw_broker_inbox`
- `pfw_broker_dlq`
- `pfw_file_transfer_history`
- `pfw_unknown_result`

설치 파일 간 불일치가 발견되면 split SQL, Flyway, baseline, all_install, smoke generator를 함께 수정한다.

필수 evidence 예시:

- Java/Gradle/MariaDB 버전
- 실행 명령
- 시작·종료 시각
- 대상 DB 식별자 마스킹값
- 적용 migration 목록
- 테이블/인덱스 확인 결과
- smoke 결과
- 실패 시 오류와 원인

### 5.3 Idempotency 실제 적용 흐름 완성

현재 JDBC repository를 실제 공통 capability로 연결한다.

필수 구조:

- PFW 표준 service/engine
- key/scope/request hash/payload hash 정책
- PROCESSING/SUCCESS/FAILED/UNKNOWN/EXPIRED 상태 전이
- stored response와 재응답
- TTL/만료 정리
- 재시도 허용 정책
- 동시 요청 race와 DB unique constraint 처리
- transactionGlobalId/segment/timeline 연계
- 감사·운영 조회 metadata

대표 적용:

- XYZ 온라인 REST EDU 1개 이상
- BAT job 또는 center-cut EDU 1개 이상
- Broker consumer 또는 publisher 흐름 1개 이상
- FileTransfer 요청 흐름 1개 이상

필수 테스트:

- 동일 key+동일 payload
- 동일 key+다른 payload
- 처리 중 동시 중복
- 성공 응답 replay
- 실패 후 재요청
- unknown 후 reconciliation
- TTL 만료
- unique constraint 경쟁
- 재기동 후 영속 상태 유지

단순 repository CRUD test만으로 완료 처리하지 않는다.

### 5.4 Broker outbox/inbox/DLQ/replay 실제 연결

PFW ownership을 유지하면서 JDBC persistence를 실제 engine/worker 흐름에 연결한다.

필수 흐름:

- 업무 transaction과 outbox atomic 저장
- publisher worker polling/locking
- publish 성공/실패/unknown 상태 전이
- retry/backoff/최대 재시도
- consumer inbox 중복 방지
- 처리 성공/재시도/DLQ 이동
- DLQ 조회와 replay 요청
- replay 결과와 감사 이력
- transactionGlobalId/parent-child segment/header propagation
- selected broker/adapter 정보 또는 실행 대상 식별

실 Kafka/MQ가 준비되지 않은 경우에도 다음은 로컬에서 검증한다.

- persistent outbox/inbox/DLQ engine
- deterministic test adapter 또는 reference adapter
- worker restart/recovery
- 중복/동시성/실패/unknown/replay

실 broker runtime만 `미검증`으로 분리한다. in-memory store만으로 전체 capability 완료 처리하지 않는다.

### 5.5 FileTransfer history·duplicate·unknown 실제 연결

필수 구현·보강:

- streaming transfer
- temp file 후 atomic rename
- checksum
- overwrite policy
- duplicate prevention
- transfer history 상태 전이
- 실패와 unknown result
- reconciliation
- credential reference 연계
- archive policy 연계
- transactionGlobalId/segment/timeline

LOCAL adapter로 정상/실패/중복/재기동/대용량 경로를 실제 검증한다.

SFTP/FTP/SCP/SSH는 port와 adapter 경계를 유지하며 외부 서버 미접속 시 runtime은 `미검증`으로 남긴다. protocol별 실제 구현이 없으면 완료 처리하지 않는다.

### 5.6 Unknown Result·Reconciliation 공통 흐름 연결

Service Call, Broker, FileTransfer, Batch 및 프로젝트별 임의 업무 주제영역에서 공통 사용할 수 있게 PFW 표준 모델과 engine을 연결한다. 특정 `EXS` 명칭이나 현재 프로젝트의 대외 주제영역 구조에 종속시키지 않는다.

필수 상태:

- UNKNOWN
- CHECK_PENDING
- CHECKING
- CONFIRMED_SUCCESS
- CONFIRMED_FAILURE
- RETRY_PENDING
- MANUAL_REVIEW
- RESOLVED

필수 기능:

- 자동 재조회 worker
- 재시도 예약
- 수동 성공/실패 확정
- 처리 사유
- 변경 전후 상태
- operation id
- transactionGlobalId/segment/external key
- 권한과 감사
- 중복 수동처리 방지
- 재기동 후 이어서 처리

### 5.7 신규 reliability ADM 운영 API/UI

ADM에 다음 운영 기능을 연결한다.

- idempotency 목록/상세/상태 조회
- broker outbox/inbox/DLQ 목록/상세
- DLQ replay 요청/결과
- file transfer history/상세/재처리 후보
- unknown result/reconciliation 목록/상세
- 수동 성공/실패 확정 또는 재조회 요청
- worker 상태와 최근 실행 결과

목록·검색·상세 필드에는 운영상 필요한 값을 처음부터 포함한다.

- 처리 시작/종료 시각
- 상태
- 소요시간
- transactionGlobalId
- segment
- module/instance/worker
- 실패 구간
- retry count/next retry
- external key/business key
- selected adapter/instance
- 생성자/처리자
- 처리 사유
- 오류 코드/메시지 마스킹값

수동 작업에는 권한, 사유 필수, 변경 전후 상태, 감사 로그, 중복 실행 방지가 필요하다.

브라우저 검증이 가능하면 실제 클릭 smoke를 수행한다. 불가능하면 API runtime과 UI static 상태를 구분한다.

### 5.8 기능별 EDU 샘플과 테스트

PFW unit/contract test를 EDU 샘플로 대체하지 않는다.

온라인·일반·대외연계 사용 예제는 모두 XYZ 내부 기능별 `edu` package에 둔다. `EXS`는 현재 프로젝트의 업무 주제영역 명칭일 뿐이므로 범용 대외연계 예제나 PFW capability 예제를 `exs.edu` 또는 다른 업무 주제영역에 두지 않는다.

배치·센터컷 예제만 BAT 내부 기능별 `edu` package에 둔다. PFW와 CMN에는 개발자 참고용 EDU package를 두지 않고, 실제 engine/port/reference adapter와 unit/contract test만 둔다. XYZ/BAT EDU는 실제 PFW capability를 호출해야 하며 자체 Map/List/상수 로직으로 기능을 흉내 내거나 재구현하지 않는다.

최소 온라인·일반·대외연계 예제:

- idempotent REST request/response replay
- outbox publish와 상태 조회
- inbox duplicate prevention
- DLQ와 replay
- local file transfer history/duplicate/unknown
- reconciliation 자동/수동 처리
- ADM 조회 대상 데이터 생성 시나리오

최소 배치·센터컷 예제:

- batch idempotency
- persistent worker restart
- retry/backoff
- unknown/reconciliation
- center-cut item 중복 방지 또는 replay

각 예제는 다음을 갖춘다.

- 실제 sample class
- 한글 목적·정상 흐름·실패 흐름 설명
- sampleId
- sourcePath
- testPath
- evidencePath
- REST인 경우 Swagger/OpenAPI
- 정상/실패/경계/동시성 테스트

`CoverageCatalog` 행이나 matrix 행만 있고 실제 class/test가 없으면 완료가 아니다.

### 5.9 Swagger/OpenAPI 전수 보강

이번 신규 ADM/XYZ REST API뿐 아니라 CPF 전체 REST 거래의 coverage를 검사한다.

필수 기준:

- `@Tag`
- `@Operation`
- 고유 `operationId`
- request/response schema
- 표준 오류 response
- 필수 header
- transactionGlobalId/header propagation 설명
- 권한/마스킹/감사 설명

필수 자동 검사:

- Controller 대비 OpenAPI 누락
- operationId 누락
- operationId 중복
- schema 미노출
- 표준 오류 response 누락
- 필수 header 누락

Swagger UI 접근 성공만으로 완료 처리하지 않는다.

### 5.10 transactionGlobalId·segment·timeline·header propagation

신규 reliability 경로에서 실제 검증한다.

대상:

- XYZ REST → idempotency
- 업무 저장 → outbox
- publisher worker
- consumer/inbox
- DLQ/replay
- file transfer
- unknown/reconciliation
- ADM 수동 처리
- BAT worker

검증 내용:

- parent/child segment
- 시작/종료/실패 timeline
- 표준/확장 header
- selectedInstanceId 또는 selected adapter
- module/instance/worker
- file log와 DB log
- ADM 조회 연결

기존 온라인 거래 evidence를 신규 경로 완료 근거로 재사용하지 않는다.

### 5.11 `CPF_NEW_REQUEST.md` 보호 방식 보정

직전 보호 방식은 최초 실행 시 현재 파일 hash를 baseline으로 생성할 수 있어 작업 전 원본 보존 증거로 부족하다.

다음 중 신뢰 가능한 방식으로 보정한다.

- 기준 commit의 git object/blob SHA와 작업 종료 파일 비교
- 사용자가 사전에 고정한 baseline hash만 허용
- baseline 파일이 없으면 자동 생성하지 않고 실패
- baseline 갱신은 별도 명시적 초기화 명령으로만 허용
- qualityGate 실행 과정에서 baseline 생성 금지
- 보호 대상 파일 변경 시 명확히 실패

이번 요청서 자체는 Codex 작업 중 수정하지 않는다. 결과는 report/gap/evidence에 기록한다.

### 5.12 Java 25 LTS 검증

환경에 Java 25가 있으면 다음을 실제 실행한다.

- `java -version`
- `gradle --version`
- compile
- test
- bootJar
- qualityGate
- 대표 runtime smoke

환경에 Java 25가 없거나 Spring/Gradle/plugin 호환 문제가 있으면 임의 우회로 완료 처리하지 않는다. 실패 또는 재확인 필요로 기록하고 원인, 영향, 대안, 필요한 환경을 구체적으로 남긴다.

### 5.13 신규 주제영역 생성기와 업무 주제영역 비종속성 검증

기존 `scripts/create-domain.ps1`, `scripts/smoke-create-domain.ps1`을 먼저 실제로 확인한다. 생성기가 이미 있으므로 이름만 존재하는지, 실제 범용 생성기인지 검수·보강하며 불필요하게 별도 생성기를 중복 작성하지 않는다.

대표 검증은 대출 주제영역 예시인 `LNG`를 임시 검증 대상으로 사용한다. 생성 결과는 검증 후 cleanup 가능해야 하며, 실제 기존 모듈을 훼손하지 않는다.

필수 입력 후보:

- moduleCode
- moduleName
- basePackage
- serverPort
- tablePrefix
- 환경변수 prefix

생성 결과 필수 범위:

- Spring Boot executable bootJar 실행 골격
- Controller/Application/Domain/Adapter/Repository 기본 구조
- `local`, `dev`, `stg`, `prod` profile
- module별 환경변수 prefix
- datasource URL/JNDI 선택 구조
- 기본 logging, transactionGlobalId, security/audit 연결
- SQL/Flyway 위치와 all_install 연동 지점
- Swagger/OpenAPI 기본 설정
- 기본 unit/integration/smoke test
- service registry/ADM/deploy inventory 등록 골격
- 생성 결과 architecture ownership 검사

의존성 기준:

- 신규 `LNG` 등 업무 주제영역은 PFW와 CMN의 공개 contract/capability만 사용한다.
- ACC/MBR/EXS/XYZ/BAT 등 기존 업무·예제 모듈의 내부 class/repository/mapper를 참조하지 않는다.
- ServiceCall/Broker/FileTransfer/Idempotency/Reconciliation/Security 같은 engine을 신규 주제영역 안에 복사·재구현하지 않는다.
- PFW나 CMN이 생성된 업무 주제영역에 역의존하지 않는다.
- 생성기는 특정 `EXS`, `ACC`, `MBR` 명칭을 template 기술 의존성으로 하드코딩하지 않는다.

EDU와 생성기의 관계:

- 생성된 업무 주제영역에는 범용 EDU 예제를 복사하지 않는다.
- 온라인·일반·대외연계 PFW 사용 예제는 XYZ `edu`에서 제공한다.
- 배치·센터컷 PFW 사용 예제는 BAT `edu`에서 제공한다.
- 생성된 LNG 개발자는 XYZ/BAT EDU를 참고하되 실제 코드에서는 XYZ/BAT에 의존하지 않고 PFW/CMN contract만 사용한다.

필수 evidence:

- 생성기 실행 명령과 입력값
- 생성 파일 목록
- compile/test/bootJar/smoke 결과
- architecture ownership scan
- 금지 의존성 0건 또는 발견 gap
- cleanup 결과

단순 디렉터리 생성만 성공한 것으로 완료 처리하지 않는다.

## 6. 보강 범위

필수 범위를 완료하는 과정에서 관련성이 높고 재작업을 줄일 수 있으면 함께 보강한다.

- repository polling lock과 다중 worker 경쟁 제어
- cleanup/retention/archive 정책
- retry jitter와 poison message 방지
- pagination/keyset/search/sort
- masking과 download audit
- worker pause/resume/drain
- service call unknown result 연계
- architecture rule 강화
- stale evidence 탐지
- report/matrix/evidence 상태 불일치 gate
- OpenTelemetry/metrics/transactionGlobalId-traceId 연결의 공통 기반
- 동적 설정, feature flag, kill switch의 감사·다중 인스턴스 전파 구조
- local/Redis cache 표준과 TTL/invalidation/stampede 방지
- rate limit, concurrency limit, bulkhead, backpressure 표준 port
- graceful shutdown, worker drain, rolling deploy 보호
- API/event/schema versioning과 breaking-change gate
- retention/destruction/legal-hold, backup/restore/DR 기준
- SBOM, dependency/license/vulnerability/secret scan 기반

필요한 수정은 작업 범위라는 이유로 과도하게 막지 않는다. 다만 unrelated 대규모 리팩터링은 피하고 변경 이유를 보고한다.

## 7. 착수 범위

필수 범위가 안정적으로 닫힌 뒤 남는 시간과 작업량 내에서 다음을 착수할 수 있다.

- Service Call Engine unknown/reconciliation 통합
- Remote Facade Proxy 실제 다중 HTTP runtime
- Redis/Kafka/MQ real adapter runtime 준비
- SFTP/FTP/SCP/SSH test container 또는 검증 harness
- 다중 instance worker failover runtime
- credential/key/cert provider 외부 시스템 연동
- observability SLI/SLO와 p95/p99 운영 지표
- 성능·부하·soak·spike·장애복구 검증 harness
- dynamic configuration/feature flag/cache/rate-limit capability의 기능별 XYZ/BAT EDU 1차 골격

착수만 한 항목은 완료로 기록하지 않는다.

## 8. 후순위·제외 범위

- 실제 운영 Redis/Kafka/MQ 접속이 필요한 검증
- 실제 외부 SFTP/FTP/SCP/SSH 서버가 필요한 검증
- Vault/KMS/HSM 실연동
- 외부 WAS/JNDI 실배포
- 전체 최종 HTML/PDF 정본화
- 전체 CPF 최종 완료 선언

환경이 제공되지 않은 항목은 삭제하지 말고 `미검증` 또는 `재확인 필요`로 유지한다.

## 9. 실행·evidence 기준

각 완료 또는 부분 구현 항목은 실제 존재하는 evidence 경로를 가져야 한다.

필수 evidence 원칙:

- 실행 명령과 환경
- commit SHA
- Java/Gradle/MariaDB 버전
- 시작·종료 시각
- 성공/실패 결과
- 테스트 개수 또는 주요 시나리오
- 민감정보 마스킹
- SKIPPED 여부
- 실행하지 않은 항목 명시

이번 작업 신규 evidence는 새로운 작업 디렉터리에 생성한다. 기존 evidence를 덮어쓰지 않는다.

qualityGate는 최소한 다음을 실패시켜야 한다.

- 없는 evidence 경로
- stale evidence를 현재 완료 근거로 사용
- 허용되지 않은 상태값
- SKIPPED를 완료로 기록
- 실제 EDU class/test 없이 coverage 완료
- OpenAPI 누락·operationId 중복
- 신규 SQL 변경 후 기설치 MariaDB에서 fresh schema/Flyway 검증을 실행하지 않았는데 완료로 기록
- 신규 runtime propagation 미검증인데 전체 tracing 완료 기록
- baseline 자동 생성 방식의 요청서 보호 성공 처리

## 10. 결과 보고 기준

작업 종료 시 `CPF_STABILIZATION_REPORT.md`를 실제 최종 상태로 전체 갱신한다.

반드시 구분한다.

- 직접 확인·실행 완료
- 소스 구현 완료 후보
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요
- 외부 환경 부재
- 기존 evidence 유지 항목
- 이번 신규 evidence 항목

Codex 보고 자체는 완료 근거가 아니다. 실제 파일과 evidence가 일치해야 한다.

## 11. 작업 금지사항

- Git commit 금지
- Git push 금지
- branch 생성 금지
- `CPF_NEW_REQUEST.md` 수정 금지
- 민감정보 원문 기록 금지
- 실행하지 않은 검증 완료 기록 금지
- 목표파일 확인 없이 범위 축소 금지
- source만으로 capability 완료 처리 금지
- PFW capability를 EXS 등 업무 모듈 소유로 이동 금지
- 업무 코드에서 URL 직접 조합, Controller 직접 호출, 타 주제영역 DB/Repository/Mapper 직접 접근 금지
- 핵심 거래를 Spring Event만으로 완료 처리 금지
- 문서량 채우기식 산출물 생성 금지
- 신규 HTML 작성·수정은 꼭 필요한 상태 정합성 반영 외에는 지양
- PDF 생성 금지

## 12. 완료 판단 핵심

이번 마일스톤의 핵심 완료 조건은 다음이다.

1. 현재 작업 셸에서 기설치 MariaDB 접속이 가능하면 신규 빈 검증 schema에서 SQL/Flyway/all_install이 실제 성공한다. 접속 불가 시에는 소프트웨어 설치를 시도하지 않고 preflight evidence와 재현 명령을 남겨 정확히 `미검증`으로 기록한다.
2. 직전 JDBC repository가 idempotency/broker/file transfer/reconciliation의 실제 engine·worker·거래 흐름에 연결된다.
3. 신규 경로의 transactionGlobalId/segment/timeline/header propagation이 검증된다.
4. 온라인·일반·대외연계 예제는 XYZ `edu`, 배치·센터컷 예제는 BAT `edu`에 기능별로 존재하고 실제 PFW capability를 호출한다.
5. 신규 ADM 운영 API/UI, 권한, 감사, 수동처리 흐름이 연결된다.
6. 신규 REST API와 전체 Controller OpenAPI coverage 검사가 통과한다.
7. 요청서 보호가 작업 전 baseline을 독립적으로 보장한다.
8. report/gap/matrix/evidence가 과대 완료 없이 일치한다.
9. 기존 주제영역에 종속되지 않는 생성기가 LNG 같은 신규 주제영역을 실제 생성하고 compile/test/bootJar/architecture smoke를 통과한다.
10. Java 25는 실제 성공 evidence가 있거나 정확히 `재확인 필요`로 남는다.

위 조건 중 실행이 필요한 항목을 실행하지 못했으면 해당 항목을 완료로 기록하지 않는다.
