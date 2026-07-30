# CPF QA31 개발 전 검토

## 1. 기준선

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 사용자 제시 시작 SHA: `e54b60ffd6f1ae7592bf2c80007f51ce680c7e1b`
- 실제 기능 기준 HEAD 확인 SHA: `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e` (`20260730_09`)
- 작업 시작 시 재확인한 최신 원격 HEAD: `9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e` (`20260730_09`)
- 이후 더 최신 Commit은 이 작업환경에서 확인되지 않았다. 적용 직전 사용자 Repository에서 `git fetch origin; git rev-parse origin/master`로 다시 확인해야 한다.
- Working Tree: GitHub 원격 읽기 전용 검토 환경이므로 로컬 Repository Working Tree는 직접 확인할 수 없음. 결과 Overlay 적용 전 사용자 환경에서 `git status --short` 확인이 필수다.
- 수정 제외: 모든 `README*`, `cpf-docs/guides/**`, `cpf-tools/README.md`, `cpf-docs/assets/readme/**`, `cpf-docs/work/overlay/20260730-readme-guides/**`

## 2. 원본 정본 확인

QA31 Package Index, Development Remediation Request, Defect Register 23건, Requirement Matrix 99건, Scenario Matrix 66건, AI Continuity Standard, QA Session Handover Standard, Codex Batch Review Request를 순서대로 확인했다. 원본 ID·Priority·Acceptance·Scenario·Status는 변경하지 않는다. 결과는 별도 Result Matrix와 Unresolved Register에만 기록한다.

ChatGPT 사전 QA 48건은 새 QA ID를 만들지 않고 동일 Root Cause의 QA31 ID에 병기한다. QA31에 없는 추가 위험은 `PREQA-*` 원본 ID를 유지한다.

## 3. Source 재현 결과와 Root Cause 묶음

### RC-01 Evidence와 Final Gate 신뢰성

관련 ID: `QA31-D001~D007`, `PREQA-GATE-001`, `PREQA-GATE-002`, `PREQA-MATRIX-001`, `PREQA-TEST-001`, `PREQA-DB-001/002`, `PREQA-RUNTIME-001/002`, `PREQA-FRONT-001`.

- Active 문서와 Evidence가 최신 SHA를 일관되게 강제하지 않는다.
- Final Gate가 QA31 Result Matrix, Scenario Matrix, Evidence JSON의 SHA·Command·Exit Code·Expected/Actual을 행 단위로 강제하지 않는다.
- Java25, 3개 DB, Redis, Multi-instance, Browser는 이 개발 환경에서 실행할 수 없으므로 완료로 승격할 수 없다.
- 정적 문자열 Anchor 중심 검사는 Consumer 연결과 행동을 보장하지 않는다.

개발 방향: 원본 Matrix 불변 Hash, Result Matrix 행 완전성, exact-SHA Evidence, 미실행 환경 상태, 삭제 대상 Legacy 경로를 fail-closed로 검증하는 QA31 Gate를 추가하고 Final Gate에서 직접 호출한다.

### RC-02 Gateway Ingress·Target·Apply 원자성

관련 ID: `QA31-D003`, `QA31-D008`, `QA31-D009`, `QA31-GWY-001~003/006`.

- `JdbcCpfGatewayRouteProvider`가 `pathPattern`을 Target endpoint에도 사용한다.
- Public Controller가 실제 Ingress Path를 Proxy에 전달하지 않는다.
- `CpfGatewayRouteSnapshot.initialize()`가 ACK 없는 Candidate를 Cold Start 시 활성화한다.
- Synchronizer가 Candidate를 먼저 활성화하고 ACK를 나중에 기록한다.
- `cpf-core/common/gateway/**` 구형 모델이 남아 신규 Public API와 병존한다.

개발 방향: 실제 Ingress Path를 별도 인자로 전달하고 안전한 Rewrite 계약으로 Target Path를 계산한다. Snapshot은 ACK 완료 Public Route만 활성화한다. Synchronizer는 Candidate 검증→Owner ACK→ACK 정본 Refresh 순으로 바꾸고 실패 시 LKG를 유지한다. 구형 모델은 삭제 Manifest와 Architecture Gate로 제거를 강제한다.

### RC-03 Gateway Probe·Routing·Ledger

관련 ID: `QA31-D010~D012`, `QA31-GHL-001~006`, `PREQA-GW-RUN-001~004`.

- NETWORK/TCP/TLS/APPLICATION이 같은 Socket 흐름이다.
- TCP 또는 TLS 성공만으로 Application `UP`을 기록한다.
- Service Call retry/failover의 최종 결과만 Gateway Attempt 한 행으로 기록한다.
- Streaming 응답 전송 전에 Transaction SUCCESS를 확정한다.
- 실제 Route timeout/retry 값이 Service Call 명령과 HTTP 전송에 모두 연결되지 않는다.

개발 방향: 단계별 Probe Executor를 만들고 HTTP(S) Application 요청을 실제 수행한다. Service Call에 Attempt Callback 계약을 추가해 매 시도를 원장에 기록한다. Streaming 성공은 Output 전송 완료 후 확정하고 전송 중 실패는 bytes와 함께 UNKNOWN/FAILED로 기록한다. Route timeout/retry를 Command와 Transport에 전달한다.

### RC-04 Gateway Control Security·Approval·Remote Unknown

관련 ID: `QA31-D013~D015`, `QA31-GSC-001~005`, `PREQA-GW-CTRL-001~003`, `PREQA-GW-UI-002/004`.

- HMAC Canonical에 Content-Type, Body SHA-256, Audience, Key ID가 없다.
- Nonce는 JVM Local Map뿐이며 다른 Gateway Instance replay를 차단하지 못한다.
- ADM 직접 State API가 BLOCKED/RETIRED를 허용한다.
- Remote Adapter의 `block()`에 설정 Timeout이 없다.

개발 방향: Exact Body Hash와 Instance Audience를 HMAC에 포함하고 current/previous Key Rotation을 지원한다. Body를 bounded replay wrapper로 검증한다. 위험 상태 APPROVED/ACTIVE/BLOCKED/RETIRED는 Approval Owner 전용으로 차단한다. ADM Remote 호출은 connect/response/overall timeout과 Typed UNKNOWN/ERROR를 사용한다.

### RC-05 Batch Payload·File·Shell 실행 원장

관련 ID: `QA31-D016~D020`, `QA31-BAT-001~008`, `PREQA-BAT-*`.

- Map/DTO를 `Objects.toString()`으로 외부 Payload에 전달한다.
- File Ready/Claim/Release/Restart Utility가 Dispatcher 수직 흐름에 연결되지 않았다.
- Remote File Scan/Watch/Claim 미지원 Capability가 Publish 전에 명확히 차단되지 않는다.
- Shell 상세 Result가 Execution status/message로 축약된다.
- Secret 전달과 OS Process Tree 격리는 실제 OS 검증이 필요하다.

개발 방향: ObjectMapper Canonical JSON과 Contract Test를 추가한다. FILE_PROCESS를 Ready→Lease Claim→Process→Release 원자 흐름으로 연결한다. Provider Capability를 조회·검증한다. Shell 상세 결과를 Attempt Detail JSON/Column에 원자 저장하도록 Repository와 Vendor SQL을 확장한다. 실제 OS/Trust Store 검증은 미검증으로 남긴다.

### RC-06 ADM Capability·운영 결과 신뢰성

관련 ID: `QA31-D021~D023`, `QA31-ADM-*`, `PREQA-ADM-*`, `PREQA-GW-UI-*`, `PREQA-BAT-UI-*`.

- Secret/Path/File Reference Catalog가 고정 `UNAVAILABLE`을 반환한다.
- 일부 Code Catalog가 Controller에 하드코딩되어 Backend와 UI Drift 위험이 있다.
- Log Export는 Local Temp와 JVM Map에 종속된다.
- 운영 화면의 일부 Capability/Permission/Approval/Async 상태는 Backend 결과와 연결이 불충분하다.

개발 방향: Reference Catalog Provider SPI와 안전한 Reference-only 기본 구현을 연결하고 Provider 미설치 시 편집/Publish를 차단한다. Code Catalog는 Owner API에서 조회한다. Log Export는 Durable Artifact Store와 DB Metadata가 없는 환경에서 생성 자체를 차단하며 Local Memory를 완료 상태로 위장하지 않는다. 전체 Browser E2E는 미실행으로 유지한다.

### RC-07 EDU 실행 가능한 Reference

관련 ID: `QA31-EDU-*`.

`cpf-reference`의 기존 Sample은 폭넓지만 QA31 Matrix가 요구하는 Success·Validation·Permission·Timeout·Unknown·Recovery·Local/Remote·DB-less/DB·Sync/Async를 하나의 Scenario Catalog와 자동 Test/Evidence로 추적하지 않는다.

개발 방향: Public API만 사용하는 QA31 Scenario Catalog, 실행 API, deterministic failure injection, Test를 추가한다. 실제 외부 Credential이나 개인정보는 포함하지 않는다. README/Guide는 수정하지 않는다.

## 4. 개발 순서

1. QA31 Integrity/Result/Evidence Gate와 Legacy deletion enforcement
2. Gateway Path Rewrite, Snapshot/ACK/LKG
3. Gateway Probe, Attempt callback, Streaming completion, timeout/retry
4. Gateway HMAC Body/Audience/Key Rotation, Approval boundary, Remote typed timeout
5. Batch Canonical JSON, File claim flow, Shell detail ledger 및 Vendor SQL
6. ADM Provider/Capability와 운영 API·Frontend 연결 보완
7. BZA 직접 영향 회귀 Test
8. `cpf-reference` QA31 Comprehensive Developer Sample
9. 정적 Test/Gate 실행, 실행 불가 환경은 미검증 등록, 결과 문서·Evidence·ZIP 생성

## 5. 회귀 위험

- Public API 시그니처 변경은 Source Compatibility를 보존하는 overload/default method로 제공한다.
- HMAC 변경은 ADM/Gateway를 함께 배포해야 한다. current/previous Key ID로 무중단 Rotation을 지원한다.
- ACK 순서 변경 시 이미 ACTIVE지만 ACK가 없는 잘못된 데이터는 즉시 Default Deny된다. 이는 의도된 fail-closed다.
- Streaming 원장 완료 시점 변경으로 응답 종료 전 Transaction이 RUNNING으로 보이는 시간이 늘어난다.
- Batch Attempt Detail SQL은 Oracle/PostgreSQL/MariaDB Canonical·Migration·Rollback을 모두 동기화해야 한다.

## 6. Post-GA 제외

장기 Trend 분석, 비용 최적화, 다중 Region Active-Active, ML 이상 탐지 등 QA31 원본에서 Post-GA로 분류된 항목은 이번 개발에서 신규 범용 추상화로 확장하지 않는다. 단, 원본 결함의 완료 조건에 필요한 기본 운영·복구·감사 기능은 제외하지 않는다.

## 7. 판정 원칙

Source를 수정했더라도 실행하지 않은 Java25 Full Build, Oracle/PostgreSQL/MariaDB Lifecycle, Redis, Multi-instance, Browser E2E, 실제 OS Shell 격리는 `완료`나 `PASS`로 기록하지 않는다. 이 Overlay의 Head는 Git Commit SHA가 아니라 `WORKTREE-OVERLAY`이며, 사용자가 적용·Push한 후 새 exact SHA에서 Evidence를 다시 생성해야 한다.
