# CPF QA B R6J 독립 검수 실행지침

> 이번 회차는 QA B의 영역을 의도적으로 **Architecture/Ownership/ADM·BZA/EDU·Generator/Approval·Security 중심으로 회전 배정**한다.
> QA A가 Runtime/Release/Logging을 Primary로 보더라도 QA B는 거래·로그와 current-SHA Evidence를 독립 교차검수한다.


## 공통 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 현재 알려진 SHA: `0427758db041d38eb0f34d88b55bd5366e2d9e47`
- **작업 시작 시 latest origin/master exact SHA를 다시 확인한다.**
- Developer baseline: `64049044956924032360fa80be83b5e37c64f828`
- Developer claim: 77/77 implementation complete, 26 verified, 51 unverified
- QA는 Developer PASS를 자동 승계하지 않는다.
- 과거 R6I QA 40건도 자동 CLOSE하지 않는다. current SHA actual source/evidence로 재판정한다.
- QA는 제품 Source를 수정하지 않는다. QA 전용 Report/Evidence/Overlay만 작성한다.
- Commit/Push/Branch/Tag/PR/Reset/Restore/Stash/Clean/Delete 금지.
- `git clean`, `git reset --hard`, `git restore .` 금지.

## QA답게 검수하는 강제 원칙

1. Requirement 문구만 읽지 말고 actual Source, SQL, API, Test, Config, Frontend, Script, Generator, Generated Domain, Consumer, 호출경로, Runtime wiring을 확인한다.
2. 요건에 없는 문제도 발견하면 신규 Finding으로 등록한다.
3. Interface/DTO/Swagger/Test 파일 존재만으로 PASS하지 않는다.
4. 실제 Consumer와 성공/실패/경계/UNKNOWN/Recovery를 확인한다.
5. 새 Gate가 자기 자신을 검사하거나 문자열 존재만 확인하는지 공격적으로 반증한다.
6. Runtime 미실행은 `미검증`, 절대 PASS가 아니다.
7. Evidence에는 source SHA, command, cwd, tool version, exit code, stdout/stderr hash, artifact hash를 남긴다.
8. 상대 QA와 의견이 다를 수 있도록 `DISAGREEMENT`를 명시한다.
9. 자신의 Primary 밖에서도 발견한 결함은 버리지 않는다.
10. QA 종료 시 `ADDITIONAL_DEVELOPMENT_REQUIRED`를 exact finding ID로 작성한다.

## 공통 양쪽 독립검수 P0

- current result SHA / Evidence provenance
- Developer ledger의 14개 Local Evidence log 실재성과 재현성
- 51개 verification_status=미검증
- transactionId end-to-end propagation + ADM one-shot timeline
- File/DB logging standard
- EDU-ADM/EDU135 Architecture 적정성
- P0 Finding 전체 회귀


## 1. QA B Primary — Architecture / Ownership

- cpf-core/common/admin/biz-admin/batch/gateway/reference 역할
- Public API/SPI/Internal package 경계
- reverse/cyclic dependency
- Consumer 없는 API/SPI/Starter
- duplicate/dual primary
- canonical catalog → settings/BOM/profile/publication/generator parity
- public BOM internal leaf 노출
- optional provider가 core에 강제 포함되는지

## 2. QA B Primary — ADM / BZA Product

ADM/BZA는 화면 존재가 아니라 실제 Product 완성도를 검수한다.

ADM:
- 63 routes
- route/menu/button/action permission canonical
- generated OpenAPI client actual consumer
- list/search/paging/detail/create/update/delete-or-state
- expectedVersion/CAS/reason/audit
- 401/403/404/409/422/429/500/503
- authenticated Chromium/Firefox/WebKit
- a11y/responsive
- realtime freshness/reconnect/stale/fallback/multi-instance
- full 332-operation clean checkout consumer closure

BZA:
- canonical permission
- 410 retired API waiver가 실제 backend contract와 일치
- actual consumer / generated client / auth/error/browser

## 3. QA B Primary — EDU / Generator Architecture

핵심 질문:
**CPF 도입 개발자가 이 EDU를 실제로 개발해야 하는가?**

- ADM은 CPF 제품이다.
- ADM 본체 기능을 EDU로 복제하지 않는다.
- EDU는 Public API/SPI/Extension/Integration 사용 예제다.

EDU-ADM 17개를:
`KEEP_EDU | MERGE_EDU | PRODUCT_ADM | EXTENSION_SAMPLE | REMOVE_CANDIDATE`
로 분류한다.

EDU 135 전체:
- actual public contract
- intended consumer
- requiredRole/readOnly semantics
- product duplication
- generic handler false-completion
- actual 8 consumer types
를 검토한다.

Generator:
- create→build/runtime→remove→regenerate
- generated domain = cpf-member parity
- DB3/OpenAPI/Frontend/EDU coupling
- stale catalog/path/lock

## 4. QA B Primary — Approval / Security / Recovery

- exact 4D tuple
- case folding/normalization
- action-level permission
- requester/approver SoD
- policy overlap/immutability
- RUNNING lease/sweeper
- process kill→UNKNOWN→observation-only reconcile
- HMAC TTL/nonce/cluster-safe single-use
- proof verification framework-owned boundary
- raw secret property 제거 / SecretRef
- DB finalization outage
- audit append-only/redaction
- XSS/CSRF/SSRF/IDOR/injection/path/archive/process negative corpus

## 5. QA B Primary — Batch / Gateway / Integration

- Batch Worker/Scheduler/Center-Cut 실제 runtime owner
- duplicate/retry/restart/reconcile
- Gateway attempt ledger
- external REST/fixed/file/message lineage
- topology local/remote parity
- request loss/response loss/UNKNOWN
- multi-instance ownership and concurrency

## 6. QA B Cross-check — 거래·로그

`LOGGING_TRANSACTION_QA_STANDARD.md`를 이용하여 QA A와 독립적으로 최소 다음을 검증한다.

- transactionId propagation 실제 Source
- ADM transaction lookup actual API/UI consumer
- File log writer actual implementation/consumer
- DB timeline persistence/index
- Batch/external call linkage
- masking/raw permission
- log loss/backpressure/recovery

QA A의 결과를 보기 전에 1차 독립 판정을 한다.

## 7. QA B Cross-check — Evidence / Runtime

- Developer 14개 evidence/*.log 참조 실재성
- result SHA binding
- 51 unverified 분류
- Release Gate가 missing runtime을 fail-closed하는지

## 8. 신규 Finding

기존 40건 외 문제는 `QA-B-R6J-NEW-*`로 추가한다.
특히 Requirement 자체가 제품 방향과 충돌하면 `ARCHITECTURE_DECISION_REQUIRED`로 올린다.

## 9. 필수 결과물

QA B는 사용자에게 별도 ZIP으로 제공한다.
- `cpf-docs/work/v9i/qa/r6j/b/QA_B_REPORT.md`
- `QA_B_FINDINGS.csv`
- `QA_B_REQUIREMENT_STATUS.csv`
- `QA_B_EDU_ARCH_CLASSIFICATION.csv`
- `QA_B_LOGGING_MATRIX.csv`
- `QA_B_ARCHITECTURE_MATRIX.csv`
- `QA_B_EVIDENCE.md`
- `QA_B_OPINION.md`
- `CROSS_REVIEW_REQUEST.md`
- `CHANGE_MANIFEST.csv`
- `PACKAGE_MANIFEST.json`
- `SHA256SUMS.txt`
- `HANDOVER.md`

최종 보고에는 finding count/severity, verified/unverified, 새 결함, 추가 개발 필요, QA A에 반론/질문할 항목을 포함한다.
