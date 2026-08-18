# CPF TEST AND EVIDENCE — Final Development Package

작성 시각: `2026-08-18 10:42:05 +0900`  
기준 Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260817_205301.zip`  
기준 Source ZIP SHA-256: `fb0cb9cc190f79db066e2872fa30b6002eac3140331c2a33f4716f86ba30ed41`  
Git exact SHA: **미검증** — 기준 ZIP에 `.git`이 없으므로 과거 SHA를 현재 Source exact SHA로 승계하지 않음  
제품/사용자 로컬 공식 Java: **Java 25**  
GPT 실행환경 대체 검증: Java 21 / Python / Node 정적·독립 Gate  
개발 판정: **Source/Static 개발 완료**  
전체 QA 판정: **RUNTIME_REVERIFY_REQUIRED**

## 1. 최종 구현 핵심
- 거래 Header/Context를 `Transaction + Original/Current/Caller/Target Channel + Target Operation` 정본으로 currentize.
- 외부 직접 Inbound는 5 Header를 요구하고 `Current Channel`은 Receiver Generated Domain `systemCode`로 자동 확정.
- Generated Domain `systemCode` 값 자체를 Channel Identity로 사용하며 별도 Mapping/중복 Channel 설정 없음.
- Channel Policy 정본을 `operationId + callerChannel`로 수렴하고 기존 LKG/maxStale/fail-close/Audit를 재사용.
- `operationId`, `transactionId`, `executionId`, current/target operation 의미를 분리.
- Operation Catalog/Policy DB3, discovery evidence, ADM 자동 Bootstrap/정책 정본, V121~V127 append migration currentization.
- Transaction Public DX: 실제 Spring completion 기준 `afterCommit/afterRollback/afterCompletion`; `setRollbackOnly`는 고급 Escape Hatch.
- `CpfRestClient` typed GET/POST/PUT/PATCH/DELETE 및 `CpfResult` boundary 결과 연계.
- durable Async Operation: Framework executionId, idempotency, lease/heartbeat/fencing/cancel, encrypted payload, DB3 `OPS_ASYNC_OPERATION`.
- File Context 자동주입, `CpfCache` exactly-one DX, Testkit/File 중복 Public contract 정리.
- EDU를 Flat `OnlineXX/BatchXX Example`에서 실제 개발용 20 Online + 15 Batch 기능 package/역할 class/Test 구조로 재개발.
- ADM Transaction UI의 수동 Scan을 제거하고 Runtime 자동 Bootstrap 정본으로 전환; Generated/compatibility client 소비 정리.

## 2. 최신 Source에서 실행한 PASS
| 검증 | 결과 | 주요 수치 |
|---|---|---|
| Java Source syntax | PASS | 2,733 files / errors 0 |
| Current Final Gate | PASS | EDU online 20 / batch 15 / operation pairs 32 |
| EDU Active Surface | PASS | online 20 / batch 15 / flat 0 / numeric 0 / internal import 0 / catalog 35 |
| Frontend Golden Path | PASS | LIVE_SOURCE_HASHED / failures 0 |
| Frontend consumer closure | PASS | files 626 / imports 852 / operation invocations 6 |
| ADM route interaction | PASS | routes 66 / capabilities 80 / coverage 80 |
| DB3 renderer | PASS | Oracle/PostgreSQL/MariaDB |
| DB3 semantic parity | PASS | canonical tables 229 |
| DB3 static-token parity | PASS | 3 official vendors |
| DB Source Plan derivation | PASS | canonical source-plan tables 225 / vendors 3 |
| DB migration checksum direct verification | PASS | MariaDB 107 / PostgreSQL 25 / Oracle 25 (V125~V127 포함) |
| NXT3 Query DB3 | PASS | scanned 5,701 / SQL 1,741 / failures 0 |
| NXT3 Korean Comment | PASS | scanned 834 / failures 0 |
| NXT3 Layout | PASS | 87 / 87 |
| NXT3 Repository Garbage | PASS | decisions 1,350 / delete manifest 1,350 |
| NXT3 Hygiene | PASS | protected delete 0 / directory delete 0 |
| Generated Domain javac | PASS | member 26 + external 24 / Java21 substitute |
| Generator Gate | PASS | 28 / 28, checked-in generated domain idempotent diff clean |
| Runtime Tool tests | PASS/SKIP_ENV | 65 PASS / 2 SKIP_ENV / 7 subtests PASS |
| Generator tests | PASS/SKIP_ENV | 27 PASS / 10 SKIP_ENV / 6 subtests PASS |
| Testing Tools 전체 분할 실행 | PASS/SKIP_ENV | 378 PASS / 22 SKIP_ENV |
| Java21 Controller substitute harness | PASS | compile/unit/runtime / class major 65 |
| Stale System transaction headers current-source scan | PASS | current Product/Tool contract 0 |
| Flat EDU / Internal EDU import scan | PASS | 0 / 0 |

## 3. Composite Verify-All 주의
`cpf_nxt3_verify_all.py`는 현재 GPT 컨테이너에서 Git metadata가 없고 Gradle wrapper distribution이 캐시되어 있지 않아 전체 결과가 FAIL/UNVERIFIED로 표시된다. 그 실행에서 발견된 **실제 Source 실패 `generator_gate`는 Generated Domain YAML을 Generator 출력과 동기화한 후 `28/28 PASS`로 재검증했다.**

Gradle 관련 rc=125/127, live DB/Redis/Multi-instance/Process-kill rc=127은 이 환경에서 실행할 수 없는 항목이며 PASS로 기록하지 않는다. 실행 가능한 정적/독립 Gate는 위 표처럼 별도로 재실행했다.

## 4. 반드시 사용자 Java25 환경에서 재검증할 항목
- Root Gradle configuration / compile / test / build / publication / SBOM
- Java25 Generated Domain/EDU compile 및 대표 runtime
- 외부 Inbound 5 Header + Receiver Current Channel 자동설정 live 400/403/409/Controller 미실행
- Operation auto-bootstrap, YML 최초 Seed 1회, ADM Policy 보존, `operationId + callerChannel` 실제 정책 적용
- Multi-WAS policyVersion propagation, LKG/maxStale/fail-close, instanceId explicit/hostname
- Oracle/PostgreSQL/MariaDB live install/upgrade/runtime query/rollback
- Async lease/fencing/cancel/process-kill/recovery live
- Docker/Redis/Valkey/Browser E2E

위 항목은 **미검증**이며 정적 PASS를 Runtime PASS로 승계하지 않는다.

## 5. 2026-08-18 Windows Git Working Tree FinalStatic Precheck Hotfix
- 사용자 Java25 로컬에서 `run-cpf-final-local-validation.ps1` 실행 직후 `FINAL_STATIC_GATE`가 `eduOnline=0 / eduBatch=0 / operationPairs=0 / CpfOnlineTransaction definitions=0`으로 실패하여 실제 Build/Test 단계에 진입하지 못했다.
- 원인은 Product Source/EDU 삭제가 아니라 `verify-cpf-current-final.py`의 Git product path 파싱 결함이었다. `git ls-files -co --exclude-standard -z`는 NUL(`\0`) 구분자를 반환하지만 verifier가 `b'\\0'`(문자 backslash+0)으로 분리하여 Git checkout의 전체 파일 목록을 하나의 문자열로 취급했다. ZIP/fallback scan에서는 Git 분기를 타지 않아 사전 검증에서 드러나지 않았다.
- 수정: Git path split을 `b'\x00'`로 currentize하고 verifier 자체 경로가 parsed product path set에 존재하는지 self-check를 추가했다. Repository-wide Python scan에서 동일한 잘못된 NUL split 잔존은 0건이다.
- 수정 후 실제 Git checkout 형태로 Source를 재현해 `verify-cpf-current-final.py`를 다시 실행한 결과 `PASS`, `eduOnline=20`, `eduBatch=15`, `operationPairs=32`, failures 0을 확인했다.
- `operationPairs=32`가 현재 EDU/Operation 재구성 이후 Source의 실제 수치다. 이전 중간 Evidence에 언급된 44는 EDU 전면 재개발 전 수치이므로 현재 Source Evidence로 승계하지 않는다.
- 이 Hotfix는 FinalLocal **사전 Gate 진입 결함만 수정**한다. Java25 Root Gradle/live DB3/Multi-WAS/Docker/Browser 검증은 여전히 사용자 로컬 재실행 결과가 필요하며 PASS로 승계하지 않는다.

