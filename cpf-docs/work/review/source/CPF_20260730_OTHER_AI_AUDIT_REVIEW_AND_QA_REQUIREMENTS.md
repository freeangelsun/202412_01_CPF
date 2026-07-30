# CPF 타 AI 추가 감사 결과 검토 보고서

- 작성일: 2026-07-30
- 검토 기준 저장소: `freeangelsun/202412_01_CPF`
- 검토 기준 SHA: `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e`
- 입력 문서: `붙여넣은 마크다운(1)(2).md`
- 문서 목적: 타 AI가 제출한 발굴 목록을 실제 결함, 공통 개발 요건, 과장·오판으로 재분류하고 다음 QA 요청에 누락 없이 반영
- 제품 검수 제외: 모든 README, `cpf-docs/guides/**`, `cpf-tools/README.md`, README 전용 asset/overlay

## 1. 최종 결론

입력 보고서는 41개 명목 범주(번호 31 중복으로 실제 heading 42개)와 240개 파일 사례를 포함한 **발굴 목록**입니다. 240개가 곧 240개 확정 결함을 뜻하지 않습니다.

- 기존 누적 공통 QA 요건: **36개**
- 이번 검토에서 신규 추가하는 공통 요건: **8개**
- 다음 QA 요청에 유지할 누적 공통 요건: **44개**
- 특정 파일은 발견 사례일 뿐이며, 모든 개발 패턴 요건은 저장소 전체 Source/Script/Template/Generated Source/Runtime/Artifact 경로에 적용
- 정적 API 패턴만으로 결함을 확정하지 않고 입력 신뢰 경계, 실행 경로, 상태 전이, 자원 상한, 보안 정책과 Test를 기준으로 판정

### 우선 처리

- SEC-BOOTSTRAP-001: BZA/ADM 특권 초기 관리자 bootstrap 경계
- OPS-DEPLOY-001: selective rollback, STOP/Drain 결과 확인, lock owner/fencing
- SEC-ARTIFACT-001: runtime artifact signer/key/manifest/anti-rollback
- OPS-PROBE-001: timeout 실제 적용과 typed readiness/functional smoke
- SRC-RESOURCE-001: unbounded memory/disk/process output/full scan
- SRC-SQL-001: 동적 SQL 값·식별자·정렬식 결합 통제

## 2. 판정 원칙

- 사용 자체가 정상인 API: `ObjectMapper`, `StringBuilder`, `SecureRandom`, `Cipher`, `setAccessible`, `Runtime.getRuntime`, `Files.*`, `WebClient`는 호출 문맥 없이 결함으로 세지 않는다.
- 실제 결함 기준: 외부 입력 결합, 권한 상승, 경로 탈출, 무제한 자원 사용, 상태 전이 불일치, 실패 은닉, 운영 profile fallback, 증빙 없는 PASS.
- 공통 요건 기준: 발견 파일만 고치는 것이 아니라 동일·유사·변형 패턴을 전수 수정하고 자동 Gate와 negative fixture로 재유입을 차단한다.
- 미실행 처리: Compile 또는 정적 검색만으로 PASS 금지. Runtime/Failure/Timeout/Concurrency/Security scenario 미실행은 `미검증`이다.
- 문서 범위: README/Guide와 QA 상태 문서의 TODO/FIXME는 Product Source 결함 집계에서 제외한다.

## 3. 입력 보고서 범주별 검토 결과

| 번호 | 범주 | 판정 | 반영 요건 | 검토 요약 |
|---:|---|---|---|---|
| 1 | private reflection / setAccessible(true) | 부분 확인 | `SRC-REFLECT-001` | 실제 사용은 확인됐지만 DTO annotation/type 경계와 typed exception이 존재. API 사용 자체를 취약점으로 보지 않고 전체 저장소의 reflection/dynamic loading 허용 목록과 테스트로 관리. |
| 2 | raw socket / SSL 네트워크 헬스 체크 | 공통 Gate 보강 | `SRC-EGRESS-001, OPS-PROBE-001` | Socket/SSL 직접 사용 자체가 결함은 아니나 timeout, TLS hostname/certificate, DNS/IP allowlist, cancellation, probe schema를 공통화해야 함. |
| 3 | 외부 프로세스 실행 및 강제 종료 | 확정·기존 요건 강화 | `SRC-EXEC-001, SRC-RESOURCE-001` | ServiceManager/ApprovedShellExecutor 계열은 특권 명령, 출력 제한, process tree, timeout, audit를 저장소 전체 공통 요건으로 적용. Runtime.getRuntime 자원 조회 자체는 결함 아님. |
| 4 | HTTP/URI/WebClient 처리 위험 | 확정·기존 요건 강화 | `SRC-EGRESS-001, SEC-HTTP-001` | Endpoint/URI 생성이 분산되어 있으며 일부 base URL 검증이 부족. 전체 outbound 호출은 표준 client와 endpoint policy를 강제. |
| 5 | Zip / 압축 파일 처리 | 확정·기존 요건 강화 | `SRC-FS-001, SRC-RESOURCE-001` | Zip-slip만이 아니라 symlink, entry/total count, expansion ratio, streaming, atomic extraction, cleanup을 공통 적용. |
| 6 | Unsafe JSON 역직렬화 / ObjectMapper 남발 | 문맥 검토 필요 | `SRC-DESER-001` | ObjectMapper/Map 자체는 결함 아님. 외부 입력·runtime policy·manifest 경로에서 typed DTO/schema/size-depth 제한을 강제. |
| 7 | 포괄적 예외 처리 / catch-all | 확정·기존 요건 강화 | `SRC-ERR-001` | 예외 분류, interrupt 복원, unknown-result 기준, retry 가능성, audit correlation을 전체 Source에 적용. |
| 8 | SQL 동적 문자열 빌드 | 과장 포함·신규 공통 요건 | `SRC-SQL-001` | 확인한 ADM 예시는 placeholder와 PreparedStatement를 사용해 injection 결함이 아님. 값/식별자/ORDER BY/IN 절 동적 조립을 구분하는 전수 Gate 필요. |
| 9 | 파일 입출력 / 임시 파일 처리 | 확정·기존 요건 강화 | `SRC-FS-001, SRC-RESOURCE-001` | 경로 정규화, symlink, 권한, atomic write, cleanup, retention, size/concurrency 제한을 모든 파일 경로에 적용. |
| 10 | 로컬 하드코딩 호스트/HTTP/환경 기본값 | 부분 확인 | `CFG-ENV-001, SRC-EGRESS-001` | local/test/smoke에서는 허용 가능. production/remote profile에서 loopback·평문 HTTP·기본 JDBC가 자동 선택되면 실패하도록 함. |
| 11 | TODO/FIXME/HACK/미완성 문서 | 제품 결함에서 제외 | `SRC-INCOMPLETE-001` | 문서·QA 산출물의 상태 표시는 제품 Source 결함 수에서 제외. 실행 가능한 Product/Generator Source의 reachable stub만 Gate 대상. |
| 12 | 민감 정보/시크릿 키워드 | 과장 포함 | `CFG-ENV-001, SEC-CRYPTO-001` | password/token 문자열 존재만으로 누출 아님. 실제 secret value, persistence, logging, artifact/evidence 포함 여부를 검사. |
| 13 | XML 파싱 보안 | 현재 예시는 안전·회귀 Gate | `SEC-PARSER-001` | CpfXlsxTabularAdapter는 secure processing, DOCTYPE/외부 entity 차단과 zip bomb 제한이 이미 있음. 전체 parser에 동일 정책을 강제. |
| 14 | 암호화/랜덤성/암호 API | 문맥 검토 필요 | `SEC-CRYPTO-001` | 표준 crypto API 사용 자체는 결함 아님. 알고리즘, mode, nonce, key provider, rotation, constant-time 검증으로 판정. |
| 15 | 리소스/클래스 로딩 | 공통 Gate 보강 | `SRC-REFLECT-001, SRC-INCOMPLETE-001` | Class.forName/getResourceAsStream 사용은 정당할 수 있음. 입력 기반 임의 class loading, 누락 fallback, capability mismatch를 차단. |
| 16 | WebClient 기반 테스트/서비스 호출 | 기존 요건 병합 | `SRC-EGRESS-001, OPS-PROBE-001` | 테스트와 운영 호출의 endpoint/timeout/auth/retry 계약을 분리하고 local smoke를 production evidence로 승격 금지. |
| 17 | JDBC URL / DB 커넥션 정보 | 기존 요건 병합 | `CFG-ENV-001` | 운영 profile에서 빈/loopback/default credential 차단, vendor별 URL parser와 allowlist를 적용. |
| 18 | Deprecated API / 레거시 경고 | 단순 존재는 결함 아님 | `SRC-INCOMPLETE-001` | 실제 호출 가능성, replacement, removal date, compatibility adapter 여부를 기준으로 관리. |
| 19 | Executor/Thread 생성 및 스레드풀 | 확정·기존 요건 강화 | `SRC-CONC-001, SRC-RESOURCE-001` | 소유권, shutdown, backpressure, queue bound, cancellation, virtual thread 적합성을 공통 Gate로 적용. |
| 20 | 환경 변수·비밀번호·시크릿/로컬 JDBC | 기존 요건 병합 | `CFG-ENV-001, SEC-CRYPTO-001` | 환경별 provider 정책, 운영 fail-closed, fixture marker와 실 secret 분리. |
| 21 | CORS / HTTP 보안 정책 동적 제어 | 부분 확인·신규 공통 요건 | `SEC-HTTP-001` | 현재 Gateway는 credentials+wildcard 금지와 max-age 범위 검증이 있음. Origin URI/scheme, private host, change approval, rollback/audit까지 공통화. |
| 22 | Spring 프로퍼티 기본값 / 로컬 URL | 기존 요건 병합 | `CFG-ENV-001` | 운영에서 local default를 금지하고 profile/capability 기반 fail-closed 적용. |
| 23 | 임시 파일·임시 디렉터리 | 확정·기존 요건 강화 | `SRC-FS-001, SRC-RESOURCE-001` | OS shared temp 사용, 권한, symlink, abnormal shutdown cleanup, quota를 공통 관리. |
| 24 | 동적 클래스·리소스 로딩 | 기존 요건 강화 | `SRC-REFLECT-001` | 등록된 package/type/resource allowlist, native-image/JPMS test, 외부 입력 class name 금지. |
| 25 | Zip 메타데이터·경로 검증 | 기존 요건 강화 | `SRC-FS-001, SRC-RESOURCE-001` | entry path, absolute/root, count, ratio, total size, duplicate entry, symlink, overwrite 정책. |
| 26 | 동적 SQL 구성 | 신규 공통 요건 | `SRC-SQL-001` | 값은 parameter binding, 동적 identifier/order는 enum/allowlist, vendor SQL catalog를 사용. |
| 27 | 경로/디렉터리 생성 | 기존 요건 강화 | `SRC-FS-001` | canonical root, no-follow link, ownership/permission, TOCTOU negative tests. |
| 28 | 전체 바이트 읽기/메모리 과다 | 확정·신규 공통 요건 | `SRC-RESOURCE-001` | readAllBytes/byte[] API를 size limit만으로 끝내지 않고 streaming, aggregate concurrency budget, disk quota, output bound로 통제. |
| 29 | ADM/BZA bootstrap 초기 관리자 | 확정·신규 P0 | `SEC-BOOTSTRAP-001` | BZA에는 ADM 수준의 운영 승인 gate가 없고 기본 operationId가 loginId 기반. bootstrap은 one-time token/expiry/approval/disable-after-success/audit를 강제. |
| 30 | 호스트 에이전트 서비스 제어 명령 | 확정·기존 요건 강화 | `SRC-EXEC-001, SRC-RESOURCE-001` | catalog는 존재하지만 executable/script trust, output bound, process tree, state reconciliation을 강화. |
| 31A | 배포/런타임 경로 및 헬스 체크 | 부분 확정·신규 요건 | `OPS-DEPLOY-001, OPS-PROBE-001, SRC-EGRESS-001` | 락 미획득 후 release 주장은 사실 아님. 실패 시 전체 인스턴스 rollback, unvalidated agent URL, 약한 readiness 판정은 확인. |
| 31B | 호스트 에이전트 운영 API/로그/DR | 부분 확정 | `OPS-DEPLOY-001, SRC-FS-001, SEC-ARTIFACT-001` | STOP 결과 미확인 rollback과 로그 ZIP 수명주기 부족은 확인. checksum 문자열 서명은 artifact digest와 결합돼 자체로 결함은 아님. |
| 32 | 배치 런타임 / HTTP URI 입력 | 확정·기존 요건 강화 | `SRC-EGRESS-001` | absolute/network-path URI, scheme/authority/query/path canonicalization, SSRF와 redirect를 공통 차단. |
| 33 | 승인 스크립트 실행/임시 파라미터 | 기존 요건 병합 | `SRC-EXEC-001, SRC-FS-001, SEC-SCRIPT-001` | 명령 allowlist와 secret parameter transport/cleanup/permissions를 전체 실행 경로에 적용. |
| 34 | 로그 검색/유지관리 성능 | 신규 공통 자원 요건 | `SRC-RESOURCE-001, SRC-FS-001` | 전체 walk/scan/retention을 요청 thread에서 무제한 수행 금지; index, paging, budget, lock, background lifecycle 필요. |
| 35 | 압축 서비스 메모리/경로 | 확정 | `SRC-RESOURCE-001, SRC-FS-001` | GZIP unbounded readAllBytes와 ZIP entry materialization이 확인됨. |
| 36 | 첨부 저장소 메모리/권한 | 부분 확정 | `SRC-RESOURCE-001, SRC-FS-001` | 경로/심볼릭 링크 검사는 일부 존재하나 byte[] store/read API와 platform permission 차이를 보강. |
| 37 | 로컬 파일 교환/원격 명령 계획 | 기존 요건 병합 | `SRC-FS-001, SRC-EXEC-001` | base root trust, symlink, command plan structured representation과 downstream shell 금지. |
| 38 | Gateway 원장 스풀 파일 | 기존/신규 자원 요건 | `SRC-FS-001, SRC-RESOURCE-001, SRC-ERR-001` | spool quota, streaming, atomic state transition, replay backpressure, corrupted entry quarantine. |
| 39 | Gateway 요청 본문 임시 파일 | 기존/신규 자원 요건 | `SRC-FS-001, SRC-RESOURCE-001` | secure temp root, quota, cleanup ledger, replacement/TOCTOU 방지. |
| 40 | 외부 기관 endpoint runtime 설정 | 기존 요건 강화 | `SRC-EGRESS-001, SEC-HTTP-001` | base URL canonicalization, path/query 금지 정책, host/network range allowlist, approval/dry-run/audit. |
| 41 | 로그 Export 임시 artifact/메모리 | 기존/신규 자원 요건 | `SRC-FS-001, SRC-RESOURCE-001, CFG-ENV-001` | shared temp, concurrent memory budget, artifact TTL/cleanup, field-classification masking evidence. |

## 4. 이번에 신규 추가하는 공통 QA 요건 8개

### SRC-SQL-001 — 동적 SQL·Query Construction 공통 계약 (P1)

**적용 범위:** 전체 Java/Groovy/Kotlin Source, JDBC/JPA/MyBatis Adapter, Vendor SQL Catalog, Generator SQL Template

**문제 정의:** StringBuilder 사용 여부가 아니라 외부 값이 SQL 값·식별자·정렬식·절 구조에 결합되는지가 핵심이다.

**올바른 구현 방식**
- 모든 값은 PreparedStatement/Named Parameter/MyBatis parameter binding 사용
- 테이블·컬럼·ORDER BY·방향·함수·Vendor 절은 enum/정적 allowlist 또는 검증된 SQL Catalog에서만 선택
- IN 절은 placeholder 개수만 생성하고 값은 개별 binding
- 사용자 입력을 SQL fragment로 받는 API 금지
- 쿼리 timeout, max rows, paging, execution plan/slow query evidence 적용

**자동 Gate 및 Negative Test**
- SQL concatenation/format/interpolation 정적 분석
- identifier allowlist architecture test
- SQL injection payload negative tests
- 3개 DB vendor query compile/runtime tests
- 예외 승인 목록과 owner/만료일 관리

**완료 Evidence:** 동적 SQL 후보 전체 inventory, unsafe 0건, parameter binding evidence, DB별 test result

### SRC-RESOURCE-001 — Bounded Streaming·Memory·Disk·Process Output 자원 계약 (P1)

**적용 범위:** 파일/첨부/로그/압축/HTTP body/process output/spool/cache/export 및 모든 readAllBytes·byte[] 경로

**문제 정의:** 개별 파일 크기 제한만으로 동시 요청·압축 팽창·전체 scan·누적 메모리·디스크 고갈을 막을 수 없다.

**올바른 구현 방식**
- 대용량 입력/출력은 streaming API 사용
- request/entry/aggregate/concurrency 단위 memory·disk·count budget
- readAllBytes/ByteArrayOutputStream은 승인된 작은 상한 내에서만 허용
- 압축 ratio/count/total size 및 temp/spool quota
- process stdout/stderr bounded drain과 backpressure
- cleanup TTL, abnormal shutdown recovery ledger, disk low-watermark

**자동 Gate 및 Negative Test**
- readAllBytes/byte[]/unbounded walk 정적 inventory
- concurrent load/OOM/disk-full/zip-bomb/process-output flood tests
- JFR/heap/disk/FD evidence
- stale temp/spool cleanup restart test

**완료 Evidence:** peak heap, disk usage, open FD/thread, cleanup result, configured budgets

### SEC-BOOTSTRAP-001 — 특권 초기 관리자 Bootstrap 수명주기 (P0)

**적용 범위:** ADM/BZA 및 향후 모든 운영자·서비스계정·초기 권한 생성 경로

**문제 정의:** 환경변수 enabled만으로 재기동 시 반복 진입하거나 profile 이름만으로 운영 여부를 판단하면 특권 계정 생성 경계가 약하다.

**올바른 구현 방식**
- 운영환경 판정은 profile 문자열이 아닌 명시 environment classification 사용
- production bootstrap은 별도 승인, one-time nonce/token, 짧은 만료시간, 요청자/승인자 분리
- 최초 성공 후 자동 영구 비활성화 또는 bootstrap lease 소진
- operationId는 환경·인스턴스·요청 nonce와 canonical fingerprint로 생성
- 기존 계정/부분 처리 상태는 임의 binding 금지, 명시 reconciliation
- 비밀번호 원문 즉시 폐기, secret provider만 사용, 로그에 식별정보 최소화

**자동 Gate 및 Negative Test**
- prod-like profile alias/empty profile/renamed profile tests
- restart/retry/concurrent bootstrap/response loss tests
- stolen env value/expired token/operation collision negative tests
- bootstrap 성공 후 재실행 차단 test

**완료 Evidence:** approval ID, one-time token metadata, operation fingerprint, created/blocked/reconciled audit

### OPS-DEPLOY-001 — 배포·Rollback 상태기계와 Lock Ownership 계약 (P0)

**적용 범위:** Batch control server, Host Agent, ADM deployment UI/API, artifact installer, runtime control

**문제 정의:** 부분 배포 실패 시 변경되지 않은 인스턴스까지 rollback하거나 STOP/Drain 결과를 확인하지 않으면 상태 불일치와 불필요한 장애를 만든다.

**올바른 구현 방식**
- lock acquire 성공 여부와 owner/fencing token을 저장하고 동일 owner만 release
- 인스턴스별 이전/현재/목표 상태와 수행 단계 journal
- 실제로 side effect가 발생한 인스턴스만 역순 rollback
- STOP/Drain/Install/Start/Readiness 결과 확인 후 다음 단계
- UNKNOWN_RESULT는 자동 반복 금지, reconcile 후 재개
- 부분 rollback 상태와 수동 복구 runbook 제공

**자동 Gate 및 Negative Test**
- lock contention/owner mismatch/process kill/failover tests
- 각 단계 실패 injection과 selective rollback 검증
- STOP 실패 후 installer rollback 차단 test
- idempotent retry/response loss/reconciliation test

**완료 Evidence:** deployment journal, lock owner/token, changed instance set, rollback decision, final actual state

### OPS-PROBE-001 — Health·Readiness·Functional Smoke Probe 계약 (P1)

**적용 범위:** Java probe, PowerShell smoke, deployment readiness, gateway/agent connection tests

**문제 정의:** 포트 오픈이나 응답 문자열 포함만으로 정상 판정하면 timeout 미적용, false positive, 인증·DB·업무 준비 미확인을 초래한다.

**올바른 구현 방식**
- connect/read/overall timeout을 실제 client에 적용
- HTTP status, Content-Type, versioned typed response schema 검증
- liveness/readiness/startup/functional smoke를 분리
- TLS/hostname/certificate 및 endpoint allowlist
- probe별 auth/header/transaction context
- retry/backoff/jitter와 deadline, cancellation
- functional smoke는 side effect 확인·cleanup 포함

**자동 Gate 및 Negative Test**
- timeout ignored detector 또는 contract test
- 200+invalid body, 500+UP text, slow body, wrong content-type negative tests
- TLS failure/auth failure/partial dependency tests
- local smoke와 production evidence 분리

**완료 Evidence:** URI policy result, timeout values, status/schema, dependency checks, smoke side effect

### SEC-ARTIFACT-001 — Runtime Artifact Trust·Signature·Anti-Rollback 계약 (P0)

**적용 범위:** Host Agent installer/verifier, offline bundle, deployment manifest, release artifact consumer

**문제 정의:** checksum 검증만으로 key path 신뢰, signer identity, version rollback, manifest binding, TOCTOU를 모두 보장하지 못한다.

**올바른 구현 방식**
- artifact bytes의 digest를 계산하고 서명된 canonical manifest/digest와 일치 확인
- signature가 artifact coordinate/version/hash/size/environment/channel을 함께 bind
- public key/certificate canonical path, owner/permission/symlink, trust anchor 검증
- algorithm/key version/expiry/revocation/rotation
- 검증 후 설치까지 동일 file handle 또는 immutable staging으로 TOCTOU 차단
- 허용 version range와 anti-rollback policy

**자동 Gate 및 Negative Test**
- modified artifact/manifest/hash/signature/key path/symlink tests
- wrong signer/revoked key/expired key/version downgrade tests
- verify 후 file swap TOCTOU test
- offline/remote artifact trust parity

**완료 Evidence:** artifact SHA, signed manifest ID, signer/key ID, trust decision, installed exact bytes

### SEC-HTTP-001 — Inbound HTTP·CORS·TLS·Runtime Policy 안전 계약 (P1)

**적용 범위:** Gateway, ADM/BZA, runtime policy applier, proxy/filter/controller

**문제 정의:** 동적 CORS·header·endpoint 정책은 값 검증과 승인/rollback이 부족하면 브라우저·프록시 경계를 약화한다.

**올바른 구현 방식**
- Origin은 URI parser로 scheme/host/port canonicalization, wildcard와 credential 조합 금지
- 허용 method/header/exposed header 최소화 및 CRLF 차단
- prod에서 HTTPS/HSTS/secure cookie/forwarded header trust 정책
- runtime 변경은 version/fencing/dry-run/diff/approval/audit/rollback
- host pattern, public suffix, localhost/private network 정책
- CSP/CORS/security header parity를 Jar/War/Gateway 경로에서 검증

**자동 Gate 및 Negative Test**
- malformed origin, null origin, wildcard credential, CRLF, mixed case/punycode tests
- runtime policy rollback/fencing/version conflict tests
- browser CORS preflight E2E
- security header scanner

**완료 Evidence:** effective policy version, approved diff, browser response headers, rollback result

### SEC-PARSER-001 — XML·Office·Structured Parser 공통 보안 계약 (P2)

**적용 범위:** SAX/DOM/StAX/POI/XML, YAML/CSV 등 외부 structured document parser

**문제 정의:** 일부 parser는 안전 설정이 있으나 신규 parser가 XXE, entity expansion, macro/formula, zip bomb 제한 없이 추가될 수 있다.

**올바른 구현 방식**
- DOCTYPE/external entity/external schema 차단, secure processing
- 입력 크기/행/열/셀/텍스트/중첩/압축 ratio 제한
- macro/formula/external link 정책
- streaming parser 우선, temp file lifecycle/permission
- parser factory와 policy를 공통 component로 제공

**자동 Gate 및 Negative Test**
- XXE/Billion Laughs/external DTD/zip bomb/macro/formula negative corpus
- parser direct construction architecture gate
- version upgrade regression suite

**완료 Evidence:** parser feature snapshot, malicious corpus results, size/ratio limits

## 5. 기존 공통 요건 보강 내용

| 요건 ID | 이번 입력 보고서로 추가할 범위 |
|---|---|
| `SRC-EGRESS-001` | raw Socket/SSL, RestClient/WebClient, URI/URL, DNS/IP/redirect/TLS, absolute·authority URI 차단까지 확대 |
| `SRC-FS-001` | archive/temp/spool/log/export/attachment 전체의 symlink, permission, atomic write, cleanup, quota 확대 |
| `SRC-RESOURCE-001` | 새로 추가. readAllBytes, byte[], process output, full scan, archive expansion, concurrent memory/disk budget 통제 |
| `SRC-EXEC-001` | STOP 결과, process tree, output drain, executable/script trust, command plan 구조화 추가 |
| `SRC-ERR-001` | transport failure를 모두 UNKNOWN으로 축약 금지, error taxonomy/retry/reconcile 정보 보존 |
| `SRC-CONC-001` | ExecutorService/virtual thread/scheduled poll의 owner, shutdown, queue/backpressure, log storm 방지 |
| `CFG-ENV-001` | loopback/http/default port/JDBC를 local/test/smoke profile로 제한하고 prod-like environment fail-closed |
| `SRC-REFLECT-001` | Class.forName/loadClass/getResourceAsStream을 포함한 dynamic loading allowlist로 확대 |
| `SRC-DESER-001` | ObjectMapper 직접 생성 inventory, external input typed DTO/schema, size/depth/unknown property policy |
| `SEC-CRYPTO-001` | artifact signature/key trust, token/hash/nonce/key lifecycle과 known-answer/negative test |
| `SRC-INCOMPLETE-001` | 문서 TODO는 제외하고 production reachable partial implementation/stub/deprecated path만 대상 |
| `BUILD-PROV-002` | runtime installer까지 artifact hash/signature/manifest identity를 연결 |

## 6. 실제 Source 대조 결과

| 검토 항목 | 판정 | 근거 위치 | 결론 |
|---|---|---|---|
| ADM bootstrap 운영 판정 | 부분 결함 | `AdmBootstrapInitializer.java:40-55` | 활성 profile 중 정확히 'prod'일 때만 allow-prod를 요구. production alias/무 profile 환경을 놓칠 수 있음. |
| BZA bootstrap 운영 승인 | 확정 P0 | `BzaBootstrapRunner.java:34-54` | enabled와 필수 값만으로 실행되며 ADM과 같은 운영 승인·one-time gate가 없음. 기본 operationId도 loginId 기반. |
| BZA 기존 계정 operation binding | 재조정 필요 | `BzaAuthRepository.java:60-96` | 기존 login에 operationId가 비어 있으면 바인딩. 복구 목적은 있으나 승인된 reconciliation state와 fingerprint가 필요. |
| Deployment lock release | 보고서 주장 기각 | `DeploymentEngine.java:37-44,68` | lock acquire 실패 시 try/finally 진입 전에 return하므로 미획득 lock release 주장은 사실 아님. |
| Deployment 전체 rollback | 확정 P0/P1 | `DeploymentEngine.java:109-121` | 실패 시 실제 변경 여부와 무관하게 manifest 전체 instance에 rollback 호출. |
| Runtime agent URL 검증 | 확정 P1 | `RuntimeLifecycleService.java:49-69` | DB agent_base_url을 policy 검증 없이 RestClient baseUrl로 사용하고 모든 transport 오류를 동일 UNKNOWN으로 축약. |
| Health probe 계약 | 확정 P1 | `HttpRuntimeHealthProbe.java:6-10` | timeoutSeconds 미사용. HTTP status/schema 대신 body 문자열 UP/ready 포함으로 판정. |
| Artifact signature 방식 | 부분 기각·보강 | `ArtifactVerifier.java:3-4` | artifact bytes SHA-256을 expected와 비교한 뒤 digest 문자열 서명을 검증하므로 '본문과 무관' 주장은 부정확. 다만 key path trust, signed manifest binding, anti-rollback은 부족. |
| CORS 기본 안전장치 | 보고서 과장 | `CpfGatewayRuntimePolicy.java:39-45,66-80,125-133` | credentials=true와 wildcard origin 조합을 이미 차단하고 max-age 범위도 검사. Origin canonicalization·approval/audit는 보강 필요. |
| 동적 SQL 예시 | 보고서 과장 | `AdmRuntimePolicyDistributionAdapter.java:157-218` | 조건절과 placeholder 개수만 동적 생성하고 값은 binding. StringBuilder 자체는 injection 결함 아님. |
| XLSX XML parser | 보고서 과장 | `CpfXlsxTabularAdapter.java:23-60` | secure processing, DOCTYPE/external entity 차단, POI zip bomb 제한이 이미 적용. |
| Runtime prepared 유지 | 의도된 상태 가능 | `CpfRuntimeControlAgent.java:135-180` | side effect 결과가 불명인 경우 PREPARED 유지가 reconciliation 계약에 부합. 별도 stale/retry/backoff 검증은 필요. |
| Archive 메모리/경로 | 확정 P1 | `LocalCpfArchiveService.java:117-165` | ZIP entry를 byte[]로 적재하고 결과 list에도 보관. GZIP은 unbounded readAllBytes. |
| Attachment 메모리 | 확정 P1 | `LocalCpfAttachmentStorageAdapter.java:16-22` | 경로 이탈·일부 symlink 검사는 있으나 store/read API가 byte[] 전체 적재. |
| Agent rollback 순서 | 확정 P0/P1 | `AgentController.java:126-145` | STOP command result의 success를 확인하지 않고 installer.rollback 수행. |
| Agent log archive lifecycle | 확정 P1 | `AgentController.java:160-170` | FileSystemResource 반환 후 archive 삭제/TTL lifecycle이 Controller 계약에 없음. |
| Service path normalization | 보강 필요 | `CpfWebClient.java:23-26,61-76` | normalizePath가 선행 slash만 보장. scheme/authority/network-path/encoded traversal/query/fragment를 명시 검증하지 않음. |

## 7. 과장되거나 그대로 결함으로 넣으면 안 되는 주장

- `DeploymentEngine`가 lock을 획득하지 못해도 finally에서 release한다: acquire 실패 시 try 진입 전에 return하므로 사실 아님.
- artifact digest 문자열에 서명하므로 artifact 본문과 무관하다: bytes digest를 먼저 계산·비교하므로 일반적인 detached digest signature 구조로 볼 수 있음. 문제는 trust anchor/manifest binding/anti-rollback.
- `StringBuilder`로 SQL을 만들면 SQL injection이다: 확인 예시는 placeholder와 binding을 사용. 값·식별자·정렬식 결합 여부로 판정해야 함.
- `CpfXlsxTabularAdapter`가 XML 보안 설정이 부족하다: 확인 파일에는 XXE 및 external DTD/entity 차단과 POI zip bomb limit가 존재.
- Gateway CORS가 wildcard credential을 허용한다: 현재 record constructor가 해당 조합을 차단.
- `setAccessible(true)` 또는 `Runtime.getRuntime()` 존재만으로 취약점이다: 입력 type/권한/노출 데이터/실행 경계를 확인해야 함.
- 테스트용 secret marker가 존재하므로 secret leak이다: marker가 실제 artifact/log/evidence에 남는지와 production config로 승격되는지를 검사해야 함.
- 문서 TODO/FIXME가 존재하므로 Product 구현 결함이다: 문서 범위는 별도이며 실행 가능한 Source 상태와 연결될 때만 Product defect.

## 8. 누적 공통 QA 요건 원장 — 총 44개

| # | ID | 우선순위 | 제목 | 상태 |
|---:|---|:---:|---|---|
| 1 | `BUILD-DEP-001` | P0 | MyBatis Boot 4 단일 버전 정합성 | 기존 강화 |
| 2 | `BUILD-DEP-002` | P0 | Dependency Convergence·BOM·Lock State Gate | 기존 강화 |
| 3 | `BUILD-PROV-001` | P0 | Release Provenance Fail-closed | 기존 강화 |
| 4 | `BUILD-ART-001` | P1 | Artifact Mode 계약 단일화 | 기존 강화 |
| 5 | `BUILD-ART-002` | P1 | Full Offline/Air-gap Repository 정책 | 기존 강화 |
| 6 | `BUILD-PKG-001` | P1 | Jar/War Deployment Matrix·Runtime Smoke | 기존 강화 |
| 7 | `BUILD-FE-001` | P1 | Node/npm Toolchain 고정 | 기존 |
| 8 | `BUILD-FE-002` | P1 | Frontend Dependency·Bundle Evidence 정확성 | 기존 강화 |
| 9 | `BUILD-GEN-001` | P1 | Domain Template Contract | 기존 |
| 10 | `BUILD-GEN-002` | P1 | Structured Standalone Export | 기존 강화 |
| 11 | `BUILD-GEN-003` | P1 | Standalone Federation Boundary Gate | 기존 강화 |
| 12 | `BUILD-ART-003` | P1 | Repository/Publication Policy Drift Gate | 기존 강화 |
| 13 | `BUILD-ART-004` | P1 | Artifact Promotion Failure Injection | 기존 |
| 14 | `BUILD-API-001` | P2 | java-library API Dependency 최소화 | 기존 |
| 15 | `BUILD-REL-001` | P1 | Release Ref 정책 단일화 | 기존 강화 |
| 16 | `BUILD-PROV-002` | P1 | Artifact 무결성 Metadata 의미 명확화 | 기존 강화 |
| 17 | `BUILD-GEN-004` | P1 | 생성 Domain Dependency 재현성 | 기존 |
| 18 | `BUILD-PKG-002` | P1 | 실행 Artifact Dependency 정밀 검증 | 기존 |
| 19 | `BUILD-ART-005` | P1 | 검증 경로 Artifact Mode 일치 | 기존 강화 |
| 20 | `BUILD-PROV-003` | P0 | Source Identity 미확인 시 Artifact 재사용 차단 | 기존 |
| 21 | `BUILD-ART-006` | P1 | Offline Bundle 콘텐츠 Allowlist·Secret Hygiene | 기존 |
| 22 | `BUILD-GOV-001` | P1 | GitHub Governance Tool·Repository Portability | 기존 |
| 23 | `BUILD-OBS-001` | P1 | Build·Generator 검증 실패 진단 보존 | 기존 |
| 24 | `SRC-ABSENCE-001` | P1 | Optional·Nullable 부재 상태 Typed Contract | 기존 |
| 25 | `SRC-ERR-001` | P1 | Exception Taxonomy·Unknown Recovery Contract | 기존 강화 |
| 26 | `SRC-CONC-001` | P1 | Blocking Wait·Polling·Cancellation·Executor Lifecycle | 기존 강화 |
| 27 | `SRC-EXEC-001` | P0 | Privileged Process Execution Security Boundary | 기존 강화 |
| 28 | `SRC-EGRESS-001` | P1 | Outbound Network·HTTP Resilience·Egress 통제 | 기존 강화 |
| 29 | `CFG-ENV-001` | P1 | 환경·Profile·Secret Fixture 경계 | 기존 강화 |
| 30 | `SRC-INCOMPLETE-001` | P1 | Reachable Stub·Placeholder·Unsupported Path 차단 | 기존 강화 |
| 31 | `SEC-SCRIPT-001` | P1 | PowerShell Script 신뢰·실행 경계 | 기존 |
| 32 | `BUILD-PS-001` | P2 | PowerShell Runtime·호환성·HTTP 계약 | 기존 |
| 33 | `SRC-FS-001` | P1 | Runtime File·Directory Security Boundary | 기존 강화 |
| 34 | `SRC-REFLECT-001` | P2 | Reflection·Dynamic Loading·DTO Mapping 경계 | 기존 강화 |
| 35 | `SRC-DESER-001` | P1 | JSON 역직렬화 입력 계약 | 기존 강화 |
| 36 | `SEC-CRYPTO-001` | P1 | Cryptography Algorithm·Key Lifecycle Gate | 기존 강화 |
| 37 | `SRC-SQL-001` | P1 | 동적 SQL·Query Construction 공통 계약 | 신규 |
| 38 | `SRC-RESOURCE-001` | P1 | Bounded Streaming·Memory·Disk·Process Output 자원 계약 | 신규 |
| 39 | `SEC-BOOTSTRAP-001` | P0 | 특권 초기 관리자 Bootstrap 수명주기 | 신규 |
| 40 | `OPS-DEPLOY-001` | P0 | 배포·Rollback 상태기계와 Lock Ownership 계약 | 신규 |
| 41 | `OPS-PROBE-001` | P1 | Health·Readiness·Functional Smoke Probe 계약 | 신규 |
| 42 | `SEC-ARTIFACT-001` | P0 | Runtime Artifact Trust·Signature·Anti-Rollback 계약 | 신규 |
| 43 | `SEC-HTTP-001` | P1 | Inbound HTTP·CORS·TLS·Runtime Policy 안전 계약 | 신규 |
| 44 | `SEC-PARSER-001` | P2 | XML·Office·Structured Parser 공통 보안 계약 | 신규 |

## 9. 다음 QA 요청서에 반영하는 방식

- 기존 QA31 원본과 hash는 변경하지 않는다. 다음 요청은 additive addendum 또는 QA32 패키지로 만든다.
- 44개 공통 요건 ID를 변경하지 않고 Requirement Matrix에 등록한다.
- 입력 보고서의 240개 파일 사례는 `discovery evidence`로 보존하되, 각각을 독립 결함 수로 부풀리지 않는다.
- 각 요건에 `적용 언어/모듈/생성 Template/현재 위반/허용 예외/owner/자동 Gate/negative test/runtime scenario/evidence/completion blocker`를 연결한다.
- 정적 Scanner는 우회 표현을 포함한 fixture를 가져야 하며 scanner 미실행 또는 결과 누락은 PASS가 아니다.
- 특정 Source 수정 후 동일 패턴 전체 재검색, 생성기 재생성, artifact unpack 검증, runtime test를 함께 수행한다.
- 승인 예외는 ID, 사유, owner, 만료일, 보완 통제, test를 가져야 하며 영구 suppress를 금지한다.
- README/Guide 제외 범위는 Product defect 집계와 개발 완료 Gate에서 계속 제외한다.

### 권장 Traceability Matrix 열

`requirement_id, priority, common_pattern, applicable_surfaces, discovery_examples, confirmed_defects, false_positive_notes, prohibited_usage, allowed_exception, correct_implementation, static_gate, architecture_gate, negative_tests, runtime_scenarios, evidence_required, owner, completion_blocker, status`

## 10. 검토 한계

- 이번 문서는 입력 보고서 전체를 범주별로 검토하고 신규 Root Cause를 공통 요건으로 정리한 결과다.
- 240개 개별 파일을 모두 한 줄씩 실행 검증한 것은 아니다. 개별 사례는 다음 QA 개발 전 exact SHA 전수 Scanner와 module test로 확정해야 한다.
- GitHub 연결에서 직접 확인한 대표 Source는 기준 SHA의 파일 내용이며, 이후 commit에서는 재검증이 필요하다.
- 보고서에 없는 동일 패턴도 공통 요건의 적용 범위에 포함된다.

## Appendix A. 다음 QA 패키지의 최소 Gate

- Java/Groovy/Kotlin static + architecture tests
- PowerShell/Gradle/Generator scanner
- Generated Source regeneration and standalone build
- Dependency graph, lockfile, artifact unpack, SBOM parity
- Windows/Linux, Local/Remote/Offline, Jar/War matrix
- Security negative corpus: SSRF, command injection, path traversal, symlink, zip bomb, XXE, SQL injection, secret leakage
- Failure injection: timeout, process kill, disk full, lock contention, partial deployment, response loss, unknown result
- Runtime evidence: exact SHA, command, environment, exit code, structured result, artifact hash, logs with masking
