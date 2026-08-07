# CPF Post-R6I 중앙 통합 개발·검수 리뷰

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 현재 Push 확인 SHA: `0427758db041d38eb0f34d88b55bd5366e2d9e47` (`07_01`)
- 개발GPT 작업 기준 SHA: `64049044956924032360fa80be83b5e37c64f828`
- 중앙 검토 단계: **R6I 개발 결과 반영 후 독립 QA 재검수 준비**
- 현재 판정: **개발 구현 완료 주장 접수 / QA 최종 PASS 아님**

## 1. 개발GPT 결과 통합

개발GPT 제출 원장과 현재 master 반영을 대조한 결과, 대규모 Source 변경과 R6I 결과 문서가 실제 Repository에 반영되어 있다.

개발GPT 자체 보고:
- QA Finding 40/40 개발 구현 완료
- FDEV 25/25 개발 구현 완료
- HARDEN 12/12 Source/Gate 구현 완료
- `development_status=완료` 77/77
- `verification_status=완료` 26/77
- `verification_status=미검증` 51/77
- local executable gate 실패 0
- Java25/Gradle9.1, DB3 live, authenticated browser, distributed/process-kill, performance/observability/security/DR, artifact repository, generator live DB3, Codex는 미검증

현재 master `0427758db041d38eb0f34d88b55bd5366e2d9e47`는 직전 `64049044956924032360fa80be83b5e37c64f828` 대비 약 29,660 LOC의 변경이 반영되었고 Approval, ADM/BZA, EDU, DB3 SQL, Release Gate, Supply-chain/Performance/DR 검증 도구 등이 실제 변경되었다.

**중앙 판정:** 구현량과 범위는 충분히 크지만, 개발GPT의 자체 PASS를 QA PASS로 자동 승계하지 않는다. 특히 51개 미검증과 새 Verification Tool 자체의 false-green 가능성을 독립 QA가 반증해야 한다.

## 2. 즉시 확인된 중앙 정합성 이슈

### CENTRAL-POST-001 — Result SHA 재결속 필요
개발 원장의 `result_sha`와 `PACKAGE_MANIFEST.json`은 Overlay 생성 시점의 `PENDING_USER_APPLY_COMMIT` 상태다. 실제 Push 결과는 `0427758db041d38eb0f34d88b55bd5366e2d9e47`다.

QA는 개발 원장을 임의 수정하지 말고 별도 QA Evidence에서:
- Overlay 기준 SHA `64049044956924032360fa80be83b5e37c64f828`
- 실제 적용 결과 SHA `0427758db041d38eb0f34d88b55bd5366e2d9e47`
- 현재 master file hash
- 실행 Evidence의 source SHA
를 다시 결속한다.

### CENTRAL-POST-002 — 개발 원장이 참조하는 Local Evidence 로그의 Repository 실재성
`REQUIREMENT_STATUS.csv`는 다음 14개 Local Evidence 로그를 반복 참조한다.

`adm-consumer-changed.log`, `artifact-consumer-selftest.log`, `behavior-contract.log`, `bza-consumer.log`,
`db3-runner-contract.log`, `edu-adm17-compile.log`, `edu-adm17-selftest.log`, `edu-consumer.log`,
`frontend-contract.log`, `idempotency-runtime.log`, `openapi-lifecycle.log`, `route-contract-repro.log`,
`sql-parity.log`, `supply-chain-selftest.log`.

현재 Push된 `cpf-docs/work/r6i-dev/evidence/`에서 확인되는 파일은 `environment.txt`다.
따라서 Local PASS 주장의 실행 로그가 최종 Package에서 의도적으로 제외된 것인지, Package Manifest/SHA256SUMS와 불일치한 것인지, 재생성 가능한 Evidence인지 QA가 반드시 판정한다.

**로그 파일 부재 자체를 곧바로 제품 결함이라고 확정하지 않는다.** 그러나 QA Evidence provenance 결함이면 Release Blocker다.

### CENTRAL-POST-003 — 51개 Runtime 미검증
미검증은 미구현과 다르지만 상용 완료도 아니다. Java25/Gradle9.1, 실제 DB3, 실 인증 Browser, Multi-instance/Process Kill/Network/Broker/DB outage, Performance, Observability, Security corpus, DR, Artifact repository, Generator lifecycle, Codex를 current SHA에서 실행해야 한다.

### CENTRAL-POST-004 — ADM full-operation closure
개발GPT는 `ADM changed-scope 332 operations / consumed 183 / waiver 0`을 PASS로 기록했지만, `OPEN_ISSUES.md`는 **full clean checkout의 332-operation source closure**를 미검증으로 명시한다.
QA는 changed-scope PASS와 full-product PASS를 혼동하지 않는다.

### CENTRAL-POST-005 — Verification Gate 자체 재검수
이번 개발에서 다수의 Python/PowerShell/Node Verification Tool이 새로 추가·수정됐다.
QA는 Gate가 기대 문자열·자기 생성 Catalog·synthetic double만 검사하지 않는지 mutation, negative case, 실제 Consumer와 Runtime을 이용해 검증한다.

## 3. Architecture 재검토 — ADM과 EDU

다음 QA 회차에서 `EDU-ADM-01~17`을 무조건 다시 보강하는 것이 목표가 아니다.

원칙:
- ADM은 CPF가 제공하는 플랫폼 관리 Product다.
- 도입 개발자가 ADM 본체를 다시 개발하는 구조가 아니다.
- EDU는 도입 개발자가 실제 사용하는 Public API/SPI/Extension/Integration을 교육해야 한다.
- Product ADM 내부 기능을 EDU에서 복제하면 Ownership이 잘못된 것이다.

QA A/B는 기존 EDU-ADM 17개와 EDU 135개 전체를 다음으로 분류한다.
1. Public Consumer 교육 예제로 유지
2. 여러 EDU를 하나의 Public Pattern으로 통합
3. ADM/BZA/Gateway/Batch Product 검증으로 귀속
4. 공식 Extension Sample로 재정의
5. 정본 변경 후 삭제 후보

현재 Requirement를 QA 전 임의 삭제하지 않는다. QA A/B의 독립 의견과 중앙 판정을 거쳐 최상위 Requirement, Catalog, Generator, Manual, Test, 개발지침을 동시에 정렬한다.

## 4. 거래·로그를 독립 QA 핵심축으로 승격

CPF는 금융권 업무 플랫폼이므로 Logging/Transaction Timeline은 보조 기능이 아니라 운영 필수 기능이다.

QA는 다음을 독립적으로 검증한다.

### 4.1 Transaction lineage
- transactionId 34자리 표준 생성
- 거래가 거래를 호출해도 동일 transactionId 유지
- Local/Remote/Async/Retry/Message/File/Batch/Center-Cut 전체 propagation
- segmentId/parentSegmentId/attempt로 호출 계층 구분
- trace/span과 transaction 연결
- trust boundary에서 위조 internal header 차단
- UNKNOWN/reconcile 후에도 동일 lineage 유지

### 4.2 ADM transactionId 단일 조회
운영자는 **transactionId 하나만 입력해서 전체 호출 흐름을 한 번에 조회**할 수 있어야 한다.

조회 결과는 최소:
- 요청/응답 결과와 elapsed
- 호출 Tree/시간순 Timeline
- 시스템/인스턴스/WAS
- 하위 거래 segment
- 외부 연계 attempt/timeout/result
- Message producer/consumer/retry/DLQ
- Batch job/execution/step/partition/worker
- File/Remote log 연결
- 오류/failure stage/UNKNOWN/reconcile
- partial/stale/missing source 표시
를 제공한다.

### 4.3 File log
- 표준 구조화 필드
- 표준 Path/File naming/encoding
- date/system/instance 분리
- rotation/compression/retention
- bounded async queue/backpressure
- disk full/write failure/process kill/shutdown drain
- fallback spool/replay/duplicate/loss detection
- 파일 권한과 masking
- terminal loss metric/alert

### 4.4 DB log/timeline
- transactionId/segment/attempt/batch execution canonical schema
- transactionId 검색 Index와 대량 조회 성능
- append/duplicate/idempotency
- retention/partition/archive/purge
- DB outage/retry/partial persistence
- Audit append-only/tamper evidence
- File/DB/Trace의 동일 transaction 상호 연결

### 4.5 거래·대외연계·Batch 특화
외부 연계는 remoteSystem/operation/attempt/timeout/error를 남기고, Batch는 jobInstance/jobExecution/step/partition/item/agent/worker와 원 transactionId를 연결한다. Restart/Retry/Skip/Rollback/Commit 후에도 계보를 재구성할 수 있어야 한다.

## 5. 중앙 협업 운영

각 작업자는 단순 결과뿐 아니라 다음 의견을 보고한다.

- `WORKER_OPINION`
- `DISAGREEMENT`
- `ARCHITECTURE_DECISION_REQUIRED`
- `ADDITIONAL_QA_REQUIRED`
- `ADDITIONAL_DEVELOPMENT_REQUIRED`
- `NEXT_ACTION`

QA A/B와 개발GPT의 의견은 자동 정답으로 승계하지 않는다. 중앙 통합자는 최신 Requirement, actual Source, Runtime Evidence와 비교해 판정한다.

QA/개발 지침에 적힌 것만 수행하지 않는다. 분석·검수·개발 중 새 결함, 누락, false-green, dead/stale source, consumer 단절, 보안·복구·로그·운영·DB·Frontend·Generator 문제를 발견하면 영향범위를 확장하여 보고하고, 개발 단계에서는 구현 가능한 부분을 별도 지시 없이 함께 보완한다.

## 6. 다음 단계

1. QA A 독립 검수
2. QA B 독립 검수
3. 중앙에서 두 결과 + 개발GPT 의견 + actual Source 통합
4. 필요한 항목은 A↔B Cross Review 재요청
5. 중앙 최종 재개발 Requirement 생성
6. 다음 개발GPT 지침과 관련 정본문서 동시 갱신
7. 개발 결과 ZIP → 사용자 적용/Push → 새 SHA 재검수

현재는 QA 재검수 전이므로 Release Final PASS로 표현하지 않는다.
