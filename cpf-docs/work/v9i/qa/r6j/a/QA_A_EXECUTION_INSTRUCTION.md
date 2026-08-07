# CPF QA A R6J 독립 검수 실행지침

> 이번 회차는 QA A의 영역을 의도적으로 **Runtime/Release/Logging/DB3/Artifact/Observability 중심으로 회전 배정**한다.
> QA B가 주로 Architecture/ADM/EDU/Security를 담당하더라도 QA A는 고위험 영역을 교차검수한다.


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


## 1. QA A Primary — Exact SHA / Release / Evidence

- latest master clean snapshot 기준
- Developer Overlay baseline과 actual result commit 분리
- `REQUIREMENT_STATUS.csv` 77행 수량/상태 검산
- `PENDING_USER_APPLY_COMMIT`을 current result SHA로 자동 치환하지 말고 QA Evidence로 binding
- `SHA256SUMS.txt`, `PACKAGE_MANIFEST.json`, CHANGE_MANIFEST 실제 파일 hash 검증
- `evidence/*.log` 14종 참조 실재성, 누락 원인, 재실행 가능성 검증
- `.github/workflows/cpf-r6-release-gates.yml`이 actual required check로 실행 가능한지 검증
- Release mode에서 Java25/Gradle9.1/DB3/Browser/Multi-process가 optional 우회되지 않는지
- 실패 전체 집계 후에도 Evidence가 남는지
- clean checkout에서 final-clean 검증 순서

## 2. QA A Primary — 거래·로그

`cpf-docs/work/v9i/post-r6i/LOGGING_TRANSACTION_QA_STANDARD.md`를 전부 실행 기준으로 사용한다.

특히:
- 거래→거래 호출 동일 transactionId
- Local/Remote/Async/Retry/Message/File/Batch 전파
- segment/parent/attempt hierarchy
- Batch job/execution/step/partition/worker 원 거래 연결
- 외부 연계 remoteSystem/operation/attempt/timeout/result
- File log queue/rotation/retention/disk full/process kill/loss
- DB timeline schema/index/retention
- ADM에서 transactionId 하나로 전체 Timeline/Tree 조회
- 관련 `/transactionGroups`, `/transactions`, `/remoteLogs`, Batch/Trace 화면 실제 Consumer
- PII/Secret masking/raw permission/download audit

Logging API가 존재한다고 PASS하지 말고 **실제 Writer, DB persistence, ADM 조회 Consumer**까지 추적한다.

## 3. QA A Primary — DB3 / Artifact / DR

- Oracle/PostgreSQL/MariaDB empty install→migration→seed→upgrade→runtime→rollback→forward
- V105/V106와 canonical schema parity
- CAS/idempotency/unique race
- backup/restore/DR RTO/RPO
- LOCAL_DEV/REMOTE/OFFLINE artifact consumer
- remote/offline failure 시 local fallback 금지
- SBOM/ORT/Syft/Grype/license/signature가 동일 final artifact를 가리키는지

## 4. QA A Primary — Observability / Performance

- metrics/logs/traces/transaction timeline correlation
- bounded cardinality
- SLI/SLO/burn-rate
- log writer terminal-loss metric/alert
- queue/backpressure/resource limit
- load/soak/failure injection
- runtime config/desired-actual drift
- audit tamper evidence

## 5. QA A Primary — Verification Tool Adversarial Review

이번 개발에서 새로 추가/수정된:
- `verify-r6-behavior-contracts.py`
- `verify-r6-frontend-contract.py`
- `verify-r6-edu-consumer-runtime-contract.py`
- `verify-r6-sql-parity.py`
- `run-r6-hardening-qualification.py`
- `run-r6-supply-chain-qualification.py`
- `run-r6-artifact-consumer-qualification.py`
- `run-r6-security-negative-qualification.py`
- `run-r6-observability-qualification.py`
- `run-r6-dr-qualification.ps1`
- `run-r6-release-gates.ps1`
를 변조/삭제/오류주입하여 survivor가 남는지 본다.

## 6. QA A Cross-check

QA B Primary 영역 중 최소:
- ADM/BZA critical mutation 10개 이상
- Approval capability/UNKNOWN/recovery
- EDU-ADM 17 Architecture classification
- Generator/Reference boundary
를 독립 확인한다.

## 7. EDU/ADM Architecture 의견

17개 각각에 대해:
`KEEP_EDU | MERGE_EDU | PRODUCT_ADM | EXTENSION_SAMPLE | REMOVE_CANDIDATE`
중 하나와 근거를 작성한다.

135 전체도 category별로 Public Consumer 교육 필요성을 검토한다.
수량 135 유지 자체를 Acceptance로 두지 않는다.

## 8. 신규 Finding

기존 40건 외 발견한 문제는 `QA-A-R6J-NEW-*`로 추가한다.
다른 QA가 다룰 것 같다는 이유로 누락하지 않는다.

## 9. 필수 결과물

QA A는 사용자에게 별도 ZIP으로 제공한다.
- `cpf-docs/work/v9i/qa/r6j/a/QA_A_REPORT.md`
- `QA_A_FINDINGS.csv`
- `QA_A_REQUIREMENT_STATUS.csv`
- `QA_A_EDU_ARCH_CLASSIFICATION.csv`
- `QA_A_LOGGING_MATRIX.csv`
- `QA_A_RUNTIME_MATRIX.csv`
- `QA_A_EVIDENCE.md`
- `QA_A_OPINION.md`
- `CROSS_REVIEW_REQUEST.md`
- `CHANGE_MANIFEST.csv`
- `PACKAGE_MANIFEST.json`
- `SHA256SUMS.txt`
- `HANDOVER.md`

최종 보고에는 finding count/severity, verified/unverified, 새 결함, 추가 개발 필요, QA B에 반론/질문할 항목을 포함한다.
