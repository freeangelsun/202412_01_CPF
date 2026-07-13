# CPF 차기 대형 작업 요청서

## 1. 작업명

**CPF Core Platform 실구현 전환 및 운영 관제·OpenAPI·Evidence 신뢰성 완성 1차 대형 마일스톤**

## 2. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 commit: `34b63661e22acc00c7cfca6953b781b9b1969c72`
- 정본: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 검색 보조: `CPF_FINAL_TARGET_REQUIREMENTS_01.md` ~ `CPF_FINAL_TARGET_REQUIREMENTS_05.md`
- 운영·검수 기준: `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
- 결과 대조 대상:
  - `CPF_STABILIZATION_REPORT.md`
  - `CPF_GAP_MATRIX.md`
  - `CPF_EVIDENCE_INDEX.md`
  - `specs/기능_구현_매트릭스.html`
  - `specs/sample-coverage-matrix.md`

목표파일을 확인하지 않고 구현 범위를 축소하거나 현재 구현 수준에 맞춰 목표를 낮추지 않는다.

## 3. 작업 목적

현재 CPF는 공통 모델, 계약, EDU 샘플과 일부 runtime 검증은 축적되어 있으나 일부 핵심 capability가 in-memory sample, contract test, DTO 후보, 교육용 시나리오 수준에 머문다.

이번 작업은 다음을 실제 플랫폼 capability로 전진시키는 대형 마일스톤이다.

- 실제 engine
- 표준 port
- reference adapter
- persistence
- SQL/Flyway/all_install
- ADM 운영 API/UI
- Swagger/OpenAPI
- 기능 테스트와 EDU 샘플
- 실행 evidence
- qualityGate
- report/matrix/gap 정합성

외부 실행환경이 없는 항목은 허위 완료하지 않고 `미검증`으로 남긴다.

## 4. 필수 완료 범위

### 4.1 작업 상태와 문서 신뢰성 복구

#### 4.1.1 `CPF_NEW_REQUEST.md` 보호

`CPF_NEW_REQUEST.md`는 ChatGPT가 작성하는 차기 작업 요청 입력 파일이다. Codex는 읽기만 하며 수정하지 않는다.

- 작업 중 수정 금지
- 결과 기록 용도로 사용 금지
- 완료 후 내용 변경 금지
- 변경 감지 check 또는 qualityGate 보호 추가
- 기존 작업 전 변경과 Codex 작업 변경을 구분해 보고

#### 4.1.2 `CPF_GAP_MATRIX.md` 전면 재작성

현재 일부 항목만 기록된 축소 GAP을 전체 CPF 목표 기준으로 재작성한다.

최소 영역:

- Architecture / MSA / Module Ownership
- Service Call / Registry / Health / Failover / Remote Facade Proxy
- transactionGlobalId / Segment / Timeline / Header Propagation
- File Log / DB Log / Trace Boost
- Authentication / Token / Permission / Menu / Button / API
- Masking / Audit / Download Audit
- Credential / Key / Certificate / Secret
- Idempotency
- Broker / Outbox / Inbox / DLQ / Replay
- Unknown Result / Reconciliation / Compensation
- FileTransfer / Archive
- Batch / Scheduler / Worker / Center-cut
- ADM / BIZADM
- CMN Fixed-length / EXS
- SQL / Flyway / Installation
- Profile / Configuration / Deploy / bootJar / JNDI
- Java 25
- Swagger / OpenAPI
- EDU Sample Coverage
- Evidence / QualityGate
- Browser / External Runtime / Multi-instance Runtime

각 항목에는 상태, 최종 목표, 현재 실제 구현, 확인 파일, 누락 기능·테스트·evidence, 필요한 runtime, 다음 조치를 기록한다.

상태값은 아래 6개만 사용한다.

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

#### 4.1.3 `CPF_STABILIZATION_REPORT.md` 전면 갱신

전체 CPF 상태 기준으로 다시 작성하고 다음을 구분한다.

- 기존 검증 완료
- 이번 작업 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요
- 직접 실행 미수행
- 외부 환경 부재

### 4.2 PFW Service Call Engine 실제 구조 완성

기존 EDU 내부 Map/List/nested record 수준을 capability 완료 근거로 사용하지 않는다.

구현·보강 대상:

- ServiceCallEngine
- ServiceRegistryPort
- EndpointRegistryPort
- InstanceRegistryPort
- InstanceHealthPort
- InstanceSelectionPolicy
- ServiceCallHistoryPort
- TimeoutPolicy / RetryPolicy / CircuitPolicy / FailoverPolicy
- Direct Instance Call / Load-balanced Endpoint Call
- selectedInstanceId 처리
- transactionGlobalId/header propagation
- segment/timeline 연계
- 표준 오류·실패 결과
- unknown result 후보

동일 JVM Local Facade와 MSA Remote Facade Proxy가 같은 contract를 사용하도록 하고 `CpfWebClient`/`CpfRestClient`를 연결한다. 업무 코드의 URL 직접 조합, Controller 직접 호출, 타 주제영역 Repository/Mapper/DB 직접 접근을 금지한다.

필요한 registry, health, heartbeat, call history, selectedInstanceId, retry/circuit/failover 결과는 reference persistence adapter를 제공한다. 다중 HTTP runtime을 실행하지 못하면 source/contract/persistence와 runtime 상태를 분리한다.

### 4.3 Service Registry·Health·Heartbeat·Ghost Detection

서비스/endpoint/instance 등록, heartbeat, UP/DOWN/DEGRADED 계산, timeout, ghost candidate, instance 제외·복구, selected instance 기록, ADM 조회를 연결된 흐름으로 구현한다.

운영자 수동 제어는 권한, 사유, 감사, 변경 전후 상태, operation id 또는 transactionGlobalId를 기록한다.

### 4.4 Remote Facade Proxy 실구조 보강

- Facade Contract
- Local Facade Implementation
- Remote Facade Proxy
- Service Call Engine 연결
- 표준 header/transactionGlobalId propagation
- timeout/retry/circuit/failover
- selectedInstanceId 기록
- 표준 오류 변환

ACC, MBR, EXS, XYZ 등 대표 복합 거래에서 실제 연결한다. architecture check로 URL 직접 조립, 직접 WebClient/RestTemplate 생성, 타 Controller/Repository/Mapper/DB 접근을 차단한다.

### 4.5 Broker·Outbox·Inbox·DLQ·Replay 실제 구현

교육용 `InMemoryBrokerStore`만으로 capability 완료 처리하지 않는다.

PFW 표준으로 다음을 구현한다.

- BrokerEnvelope
- BrokerPublisherPort / BrokerConsumerPort
- OutboxRecord / OutboxPort
- InboxRecord / InboxPort
- DlqRecord / DlqPort
- ReplayRequest / ReplayResult / ReplayPort
- BrokerIdempotencyPort
- Retry / Backoff 상태
- Publish Unknown Result
- Reconciliation 상태

reference persistent adapter와 필요한 SQL/Flyway/all_install/smoke를 함께 구현한다.

필수 흐름:

- 업무 DB transaction + outbox atomic 저장
- publisher worker
- publish 성공/실패/unknown
- consumer inbox 중복 방지
- 처리 성공/재시도/DLQ
- 관리자 replay 요청과 결과 기록

실 Kafka/MQ가 없으면 persistence/contract까지만 완료 후보로 하고 runtime은 미검증으로 유지한다.

### 4.6 FileTransfer 실제 adapter·history·unknown 구현

- FileTransferPort
- LocalFileTransferAdapter
- FileTransferHistoryPort
- DuplicatePreventionPort
- FileTransferUnknownResultPort
- FileTransferReconciliationPort
- CredentialReference 연결
- Protocol Plan
- Checksum / Temp File / Atomic Rename / Overwrite Policy / Archive Policy

파일 처리는 streaming 방식으로 구현한다. `Files.readAllBytes`나 전체 byte[] 적재를 대용량 운영 완료 근거로 사용하지 않는다.

중복 방지는 source, destination, file size, checksum, external request id, business key 등을 기준으로 실제 저장소 또는 unique constraint로 차단한다.

LOCAL/SFTP/FTP/SCP/SSH port와 adapter 경계를 명확히 하고 실 서버 미접속 시 runtime은 미검증으로 기록한다.

### 4.7 Archive·Compression 실제 기능 보정

현재 과대 완료 가능성이 있는 `PFW-EDU-ARCHIVE-011`, `012`, `014`, `015`를 실제 기능으로 보강한다.

필수 구현·테스트:

- `Files.walk` 기반 recursive directory archive
- 빈/중첩 디렉터리
- 대용량 streaming archive/extract
- entry count, single entry size, total extracted size limit
- corrupted ZIP/GZIP parser failure
- truncated archive, CRC/checksum mismatch
- zip-slip, `../`, absolute path, Windows drive path, encoded traversal, symlink 후보
- overwrite=false, duplicate entry
- temporary output, atomic completion
- history metadata

TAR는 라이브러리 검토와 실제 구현 없이는 `부분 구현`으로 유지한다.

### 4.8 Runtime Worker·Heartbeat·Ghost 실제 연결

- WorkerControlPort
- WorkerStatusQueryPort
- HeartbeatPort
- GhostDetectorPort
- RuntimeInstancePort
- RuntimeOperationHistoryPort

RUNNING, PAUSED, DRAINING, STOPPING, STOPPED, FAILED, GHOST_CANDIDATE 상태와 pause/resume/drain/stop/heartbeat/timeout/manual exclusion/recovery를 실제 worker 또는 batch worker에 연결한다. 단순 DTO나 in-memory EDU만으로 완료 처리하지 않는다.

### 4.9 Idempotency 표준 capability 구현

HTTP, Broker, Batch, FileTransfer가 공통 사용 가능한 PFW idempotency 표준을 구현한다.

- IdempotencyKey / Scope / Request Hash / Payload Hash
- PROCESSING / SUCCESS / FAILED / UNKNOWN / EXPIRED
- Stored Response / TTL / Created At / Completed At / Retry Allowed
- 동일 key+동일 payload
- 동일 key+다른 payload
- 처리 중 중복 요청
- 성공 응답 replay
- 실패 후 재요청
- unknown 후 reconciliation
- TTL 만료
- 동시 요청 race
- DB unique constraint

### 4.10 Unknown Result·Reconciliation 공통 표준화

Service Call, Broker, FileTransfer, EXS, Batch의 unknown/reconciliation을 PFW 공통 모델로 통합한다.

- UnknownResult / UnknownResultType / UnknownResultStatus
- ReconciliationRequest / ReconciliationResult
- ManualResolution / Escalation
- UNKNOWN / CHECK_PENDING / CHECKING / CONFIRMED_SUCCESS / CONFIRMED_FAILURE / RETRY_PENDING / MANUAL_REVIEW / RESOLVED
- 자동 재조회, 재시도, 수동 성공·실패 확정, 사유·감사·이력
- transactionGlobalId / segment / external key 연결

ADM 목록·상세·수동 처리 API를 제공한다.

### 4.11 ADM PFW Capability 통합 관제

실제 API를 구현한다.

Service Call:
- 서비스/endpoint/instance 목록·상세·health
- 호출 이력 목록·상세
- selectedInstanceId
- retry/circuit/failover 결과

Broker:
- outbox/inbox/DLQ 목록·상세
- replay 요청·결과

FileTransfer:
- 전송 이력 목록·상세
- 중복 차단 결과
- unknown/reconciliation

Runtime:
- worker/instance 목록·상세
- heartbeat/ghost candidate
- pause/resume/drain/stop

Security:
- credential provider 상태
- key/certificate metadata, expiry, rotation
- 민감값 원문 비노출

Archive:
- 작업 이력, 실패 사유, 크기, checksum, corrupted 결과

모든 API는 Controller, application service, PFW port, DTO, 검색/정렬/paging/상세, 권한, 감사, 마스킹, 표준 오류, OpenAPI, 테스트, evidence를 갖는다. 운영 제어 API는 사유를 필수로 받는다.

### 4.12 ADM 운영 UI 연결

기존 ADM UI 구조에 다음 화면을 연결한다.

- 서비스/instance 상태
- Service Call 이력
- Broker DLQ/Replay
- FileTransfer 이력
- Unknown/Reconciliation
- Worker/Heartbeat/Ghost
- Credential 상태
- Archive 이력

목록 검색조건은 시작/종료시간, 상태, transactionGlobalId, segment, module, instance, selectedInstanceId, 실패 구간, 오류 코드, external/business key를 포함한다. 운영 가치가 있는 테이블 컬럼을 누락하지 않는다.

브라우저 클릭을 실행하지 못하면 API/정적 연결과 browser runtime을 분리한다.

### 4.13 전체 REST API Swagger/OpenAPI 완결성 검사

모든 REST API는 다음을 갖는다.

- `@Tag`
- `@Operation`
- 고유 `operationId`
- request/response schema
- 표준 오류 response
- 필수 공통 header
- transactionGlobalId/header 설명
- 권한/마스킹/감사 설명
- 다운로드 API 설명

자동 check:

- Controller method 수와 OpenAPI operation 수
- 누락 Controller method
- operationId 중복
- request/response schema 누락
- 표준 오류 누락
- 필수 header 누락
- 권한/감사/마스킹 설명 누락

필수 evidence:

- `openapi-controller-coverage.sanitized.json`
- `openapi-operation-id-check.sanitized.json`
- `openapi-schema-check.sanitized.json`
- `openapi-standard-error-check.sanitized.json`
- `openapi-header-security-audit-check.sanitized.json`
- `sample-openapi-mapping-check.sanitized.json`

Swagger UI 접근만으로 완료 처리하지 않는다.

### 4.14 Authentication·Permission·Download Audit 전수 점검

로그인, 토큰 발급·갱신·만료, 계정 잠금, 로그인 이력, 역할, 메뉴·버튼·API 권한, 관리자 조치 권한, 다운로드 권한·감사를 전수 점검한다.

다운로드 감사에는 사용자, 조직, 권한, 사유, 조회조건, 대상 기능, 행 수, 파일명, 마스킹 여부, 시작·완료 시간, transactionGlobalId를 포함한다.

전체 API permission coverage gate를 추가한다.

### 4.15 SQL·Flyway·all_install 정합성

이번에 persistence가 추가되는 모든 기능은 다음을 한 묶음으로 반영한다.

- split SQL
- Flyway migration
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- seed data
- repository/mapper test

자동 검증 대상:

- split SQL과 all_install 차이
- Flyway/테이블/index/unique/FK/seed/smoke 누락

가능한 환경에서는 신규 빈 MariaDB 전체 설치를 실행한다. 실행하지 못하면 SQL source 정합성과 runtime 상태를 분리한다.

### 4.16 Batch·Scheduler·Center-cut 보강

일반 batch, chunk, tasklet, 온라인 호출 batch, restart, retry, compensation, unknown result, reconciliation, center-cut을 보강한다.

Center-cut은 기본 모수/item/result, parent/child transactionGlobalId, 중복 방지, 재시작, 부분 실패, 보상, unknown/reconciliation, 병렬 worker, worker 장애, 다중 instance 중복 방지를 포함한다.

PFW는 표준 port와 상태 모델을 소유하고 BAT는 기본 구현과 실행을 소유한다. PFW에 대량 업무 item 테이블을 만들지 않는다.

### 4.17 CMN Ownership 보정

아래 파일과 관련 구조를 점검한다.

- `cmn/src/main/java/cpf/cmn/fle/service/CmnFileExchangeService.java`
- `cmn/src/main/java/cpf/cmn/mqe/service/CmnMessageBridgeService.java`

기술 실행 engine 성격이면 PFW port/adapter로 이동한다. CMN에는 공통 envelope/rule/validation/converter/helper/프로젝트 공통 확장만 남긴다.

### 4.18 Java 25 호환성 문제 해결

현재 Java 25 `:pfw:test` 실패인 `Type T not present` 원인을 분석한다.

- `java -version`, `gradle --version`, toolchain
- Gradle wrapper
- Spring Boot / Spring Framework / springdoc
- annotation processor
- test task 구성
- generic metadata/class loading

가능한 범위에서 compileJava, compileTestJava, test, bootJar, qualityGate를 실행한다. 불가능하면 Gradle/Spring/plugin/library/source/test infrastructure 중 원인을 분류하고 `재확인 필요` 또는 `실패`로 남긴다.

## 5. 보강 범위

- 모든 실행 모듈의 file log + DB log + ADM 조회 연결
- transactionGlobalId/segment/module/instance/selectedInstanceId/jobExecution/worker/external key 로그
- 거래·배치·center-cut·worker·file transfer·broker message 단위 Trace Boost
- credential/key/certificate version, expiry, rotation, revocation, mTLS 후보
- local/dev/stg/prod profile 점검과 `prd` 제거
- 실행 주체 ACC/MBR/EXS/ADM/BAT/BIZADM/XYZ 외 EDU/PFW/CMN 서비스 등록 차단

## 6. 착수 범위

필수 범위를 해치지 않는 범위에서 다음을 착수한다.

- Kafka 실제 adapter 1종
- SFTP test server 또는 test container
- 다중 instance Service Call runtime harness
- 다중 instance idempotency concurrency test
- ADM browser smoke 자동화
- remote deploy harness
- Vault/KMS test adapter

실행하지 못하면 미검증으로 기록한다.

## 7. 후순위·제외 범위

다음은 무리하게 완료 처리하지 않는다.

- 실 운영 Kafka/MQ
- 실 운영 SFTP/FTP/SCP/SSH
- 실 Vault/KMS/HSM
- 외부 WAS/JNDI 실제 운영 검증
- 3대 이상 실제 서버 배포
- 실 운영 remote deploy
- 전체 브라우저 수동 검수
- zero-downtime/blue-green/canary 완결
- TAR 최종 라이브러리 확정
- 최종 정본 PDF

현재 상태와 다음 조치는 GAP에 남긴다.

## 8. EDU 샘플 완료 기준

기능 소스, 기능 테스트, EDU 샘플, EDU 샘플 테스트, Swagger/OpenAPI, OpenAPI 검증, sample coverage matrix, 기능 구현 매트릭스, evidence, report/index/gap을 한 묶음으로 본다.

다음은 완료 근거가 아니다.

- CoverageCatalog 등록만 존재
- sampleId 문자열만 존재
- DTO만 존재
- 상수값만 비교하는 테스트
- in-memory sample만 존재
- 실제 기능 없이 evidence JSON만 생성

## 9. Evidence 및 QualityGate

QualityGate는 최소 다음을 실패시켜야 한다.

- 허용되지 않은 상태값
- 없는/stale/다른 branch·profile·DB evidence
- `runtimeExecuted=false`인데 runtime 완료 기록
- CoverageCatalog-only 완료
- sample/test/OpenAPI 누락
- operationId 중복
- 표준 오류/필수 header 누락
- GAP 필수 영역 누락
- report/matrix/evidence 불일치
- `CPF_NEW_REQUEST.md` 변경
- ownership 위반
- Spring Event 핵심 흐름 사용
- persistence 소스가 있는데 SQL/Flyway/all_install 누락

Evidence metadata:

- commit SHA / branch / 실행 시각
- Java / Gradle version
- profile / DB 종류
- 실행 명령 / exit code
- runtimeExecuted
- sanitized 여부

민감정보 원문을 기록하지 않는다.

## 10. 결과 갱신 대상

작업 결과에 따라 다음을 갱신한다.

- `CPF_STABILIZATION_REPORT.md`
- `CPF_GAP_MATRIX.md`
- `CPF_EVIDENCE_INDEX.md`
- `specs/기능_구현_매트릭스.html`
- `specs/sample-coverage-matrix.md`
- 관련 source/test/sql/config/script/evidence

다음 파일은 수정하지 않는다.

- `CPF_NEW_REQUEST.md`
- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- `CPF_FINAL_TARGET_REQUIREMENTS_01.md` ~ `05.md`
- `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`

정본이나 운영 가이드의 오류는 수정하지 말고 별도 제안으로 기록한다.

## 11. Git 및 작업 제한

- Git commit 금지
- Git push 금지
- branch 생성 금지
- force checkout 금지
- 사용자 변경사항 삭제 금지
- 민감정보 원문 기록 금지
- 실행하지 않은 검증 완료 기록 금지
- contract test를 runtime 완료로 기록 금지
- in-memory EDU를 persistent engine 완료로 기록 금지
- 문서량 채우기식 문서화 금지
- 신규 HTML 작성·수정 지양
- PDF 생성 금지

구현 완결에 필요한 인접 source/test/sql/config/script는 판단해 수정할 수 있으며 이유와 영향 범위를 보고한다.

## 12. 완료 판정

다음이 충족된 경우에만 해당 기능을 `완료`로 기록한다.

- 목표 요구사항 확인
- 실제 source 구현과 계층 연결
- 필요한 SQL/Flyway/all_install
- 기능 테스트와 실행 evidence
- EDU sample/test
- Swagger/OpenAPI와 검증
- sample coverage
- report/matrix/evidence 정합성
- 필요한 runtime 검증

판정 원칙:

- source만 있음 → 부분 구현
- source/test만 있음 → 부분 구현
- 외부 runtime 필요·미실행 → 미검증
- 실행 실패 → 실패
- 확인 불충분 → 재확인 필요

## 13. 최종 보고

최종 보고에는 실제 구현 기능, 기능별 상태, 변경 파일, 테스트 성공/실패, 실행하지 않은 검증, MariaDB, Java 25, OpenAPI coverage, ADM 연결, qualityGate, 남은 GAP, 후속 권고를 포함한다.

Codex 보고와 실제 파일이 다르면 실제 파일을 기준으로 판단한다.
