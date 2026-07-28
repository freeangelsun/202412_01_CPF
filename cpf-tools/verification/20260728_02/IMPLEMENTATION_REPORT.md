# CPF 20260728_02 Final Completion Implementation Report

## 1. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 기준 Commit: `ecaddd581a88ede22b63116effd61313744b3fbe`
- 결과물: `CPF_20260728_02_FINAL_COMPLETION_ROOT_OVERLAY.zip`
- Commit/Push/Branch: 수행하지 않음
- 최종 재검증 시각: `2026-07-28T21:19:43+09:00`

## 2. 이번에 마무리한 구현

### Runtime Control

- Change/Delivery/Drift/ACK 상태를 Java Enum과 canonical catalog로 통일했다.
- 결과 객체 생성 시 허용하지 않는 상태 문자열을 fail-fast 처리한다.
- Agent, Control Plane Repository/Service의 상태 문자열 drift를 제거했다.
- External Institution endpoint/layout/timeout 변경을 실제 registry Consumer에 연결했다.

### ADM Runtime Change Center

- 변경 조회·preview·생성·취소·rollback·desired/actual/drift·audit chain 검증을 제공한다.
- Runtime Group과 Instance Member 생성·활성화·비활성화·삭제를 제공한다.
- 화면의 임의 operator 입력을 신뢰하지 않고 인증 Context의 운영자 식별자를 사용한다.
- Oracle/PostgreSQL/MariaDB 메뉴·권한 migration/rollback/checksum을 추가했다.

### Gateway

- 기존 request/response `byte[]` 전체 적재를 제거했다.
- 작은 요청은 메모리, 큰 요청은 제한 권한 임시파일에 spool한다.
- retry/failover마다 새로운 InputStream을 여는 replayable body를 제공한다.
- downstream response는 `StreamingResponseBody`로 전달한다.
- multipart, query string, Range/If-Range/If-Modified-Since/ETag header를 보존한다.
- timeout·target-down·retryable HTTP·결과 불명 I/O를 표준 transport exception으로 분류한다.
- API Client Principal 생성 순서, authority exact match, IP/CERT/expiry/quota fail-closed를 보완했다.

### Batch

- 실제 Consumer가 존재하는 5개 정책만 제공한다.
  - `BATCH_SCHEDULE`
  - `BATCH_CONCURRENCY`
  - `BATCH_CALENDAR`
  - `BATCH_CENTER_CUT`
  - `BATCH_AGENT_POLICY`
- Scheduler, Worker, Business Calendar gate, Center-Cut Runner, Host-Agent command/log API에 직접 연결했다.
- 동일 version replay는 동일 payload일 때만 허용하고 낮은 version 역전 적용을 차단했다.
- 실제 Consumer가 없는 `BATCH_PARTITION` 노출을 제거했다.
- Agent 정책에서 `logCollectionEnabled`가 무시되던 결함을 수정했다.

### Generator·DB·Repository Hygiene

- metadata schema와 central-domain-template contract를 보강했다.
- 생성 전 Module/Package/SystemCode/DB/Route/runtime-agent 계약 충돌 검증을 보강했다.
- 공식 DB Vendor를 Oracle/PostgreSQL/MariaDB로 제한했다.
- MariaDB source/runtime migration 및 3 Vendor checksum parity를 확보했다.
- 적용 시 `CPF_INTERMEDIATE_*`와 잘못 명명된 중간 Final manifest를 제거한다.

## 3. 직접 실행한 검증

- Overlay static validator: PASS
- Runtime API 독립 compile/behavior: PASS
- Runtime state enum/catalog parity: PASS
- Gateway transport compile 및 spool/reopen/delete behavior: PASS
- Gateway API Client Principal/IP/CERT/quota behavior: PASS
- Batch 5개 실제 Consumer 정책 compile/behavior/idempotency: PASS
- External Institution applier compile/behavior: PASS
- ADM TypeScript syntax: PASS
- Generator JSON syntax: PASS
- DB source/runtime/checksum parity: PASS
- Targeted secret scan: PASS

Evidence는 `cpf-tools/verification/20260728_02/evidence`에 저장했다.

## 4. 완료로 기록하지 않은 검증

현재 실행환경은 Java 21이며 Java 25/Gradle 9.1 전체 Repository 및 실 DB/WAS/Browser 환경이 없다. 따라서 다음은 `미검증`이다.

- Java 25 / Gradle 9.1 전체 `clean test assemble`
- Oracle/PostgreSQL/MariaDB install/migration/upgrade/rollback 실DB
- ADM Browser 권한별 E2E
- Gateway 실제 대용량/multipart/Range/timeout/retry/target-down
- Runtime Control 다중 인스턴스·부분 실패·재시작·drift·rollback
- Batch 실제 분산 실행
- ACC/MBR/EXS Generator 생성·빌드·실행 parity
- QA Inventory 1,214건 및 Scenario 201건 최신 Commit 재판정

이 항목은 구현 누락으로 자동 판정하지 않지만, 실제 Evidence 전에는 완료로 승격하지 않는다.
