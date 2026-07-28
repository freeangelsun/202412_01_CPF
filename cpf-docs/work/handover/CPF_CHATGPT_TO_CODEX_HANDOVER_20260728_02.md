# CPF ChatGPT → Codex Handover — 20260728_02

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 기준 SHA: `ecaddd581a88ede22b63116effd61313744b3fbe`
- 산출물: `CPF_20260728_02_FINAL_COMPLETION_ROOT_OVERLAY.zip`
- ChatGPT는 Commit, Push, Branch를 생성하지 않았다.

## 2. 이번에 실제 보완한 범위

### Runtime Control

- Runtime ACK, delivery, change, drift 상태를 Java Enum과 canonical catalog로 통일
- 결과 객체 생성 시 허용 상태를 fail-fast 검증
- Runtime Control Agent/Repository/Service 상태 문자열 drift 제거
- External Institution endpoint/layout/timeout 변경을 실제 registry Consumer에 연결

### ADM 운영 화면

- `Runtime Change Center` 신규 기능 화면 및 Route 추가
- target preview, change preview/create/search, desired/actual/drift, cancel, rollback, audit chain 검증 제공
- Runtime Group 생성·조회·삭제와 instance member 활성/비활성 관리 제공
- 운영자 ID는 화면 body 입력을 신뢰하지 않고 인증 Context에서 사용
- Oracle/PostgreSQL/MariaDB 메뉴·권한 migration/rollback 추가

### Gateway

- 기존 `byte[]` 전체 적재 제거
- 작은 요청은 메모리, 큰 요청은 권한 제한 임시파일에 spool
- retry/failover마다 새 InputStream을 제공하는 replayable body 구현
- downstream response를 InputStream으로 servlet output에 전달
- multipart content type, Range/If-Range/If-Modified-Since/ETag 등 전달
- query string 유실 방지
- retryable HTTP status, connect/response timeout, 결과 불명 I/O를 표준 transport exception으로 분류
- API client principal 생성자 순서 결함 및 authority 부분문자열 판정 결함 수정

### Batch

- `BATCH_SCHEDULE`, `BATCH_CONCURRENCY`, `BATCH_CALENDAR`, `BATCH_CENTER_CUT`, `BATCH_AGENT_POLICY` 5개 정책을 실제 Scheduler/Worker/Business Calendar/Center-Cut/Host-Agent Consumer에 연결
- enabled만 변경할 때 기본 concurrency 상한 때문에 실패하던 경계 수정
- Host Agent 명령·로그수집 정책과 Consumer/Applier 계약 통일
- 실제 Consumer 없는 `BATCH_PARTITION` 제거
- `logCollectionEnabled` payload 무시 결함 수정

### Generator·DB

- Domain metadata schema 및 central template contract 보강
- create-domain 사전 충돌/계약 검증 보강
- 3개 DB migration source/runtime/checksum parity 확보

## 3. 실행한 검증

- Overlay static validator: PASS
- Runtime API independent compile/behavior: PASS
- Runtime state compile/parity: PASS
- Gateway transport independent compile: PASS
- Gateway spool/reopen/delete behavior: PASS
- Batch 5개 actual-consumer policy behavior/idempotency: PASS
- Batch Scheduler/Worker/Center-Cut/Host-Agent consumer behavior: PASS
- External Institution applier behavior: PASS
- ADM frontend TypeScript syntax: PASS
- Gateway Principal/IP/CERT/quota behavior: PASS
- Targeted secret scan: PASS

원본은 `cpf-tools/verification/20260728_02/evidence`에 있다.

## 4. 미검증이며 다음 작업자가 반드시 실행할 것

- Java 25 / Gradle 9.1 전체 build/test
- 3개 지원 DB 실설치·migration·upgrade·rollback
- ADM Browser 권한별 E2E
- Gateway 실제 대용량·multipart·Range·timeout·retry·target-down
- Runtime Control 다중 인스턴스·부분 실패·재시작·drift·rollback
- Batch 실제 분산 실행
- ACC/MBR/EXS Generator parity
- QA Inventory/Scenario 최신 Commit 재판정

이 항목들은 구현 미완료라고 자동 판정하지 않되, 실제 Evidence 없이는 완료로 승격하지 않는다.
