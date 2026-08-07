# CPF 개발GPT R6I 최종 재개발·자체검수·결과갱신 단일 실행지침

> 이 파일 하나를 개발GPT의 실행 정본으로 사용한다. 분석·부분 수정·중간 ZIP에서 멈추지 않고 `AB-R6-001~040`과 연결된 `FDEV-001~025`를 같은 작업 흐름에서 구현·검증·기록한다.

## 0. 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 지침 기준 SHA: `77db10ad9aff44ee422795080fb2e96b364c9d65` (`08_01`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 정본: `cpf-docs/work/v9i/qa/r6i/QA_FINDINGS.csv`
- Requirement 정본: `cpf-docs/work/v9i/qa/r6i/QA_REQUIREMENT_STATUS.csv`
- 통합 관리자 추가 강화 리뷰: `cpf-docs/work/v9i/qa/r6i/QA_MANAGER_ADDITIONAL_DEVELOPMENT_REVIEW.md`
- 추가 강화 원장: `cpf-docs/work/v9i/qa/r6i/QA_MANAGER_HARDENING_REQUIREMENTS.csv`
- 현재 QA 판정: **미통과 / Release Blocked**

작업 시작 시 반드시 최신 `origin/master`, HEAD, Working Tree를 다시 확인한다. `77db10ad9aff44ee422795080fb2e96b364c9d65` 이후 Commit이 있으면 `instruction_basis_sha`, `work_start_sha`, `result_commit_sha`, `evidence_source_sha`를 분리하며 과거 Evidence를 자동 승계하지 않는다.

## 1. 종료 금지

다음은 종료 사유가 아니다: 일부 Finding 수정, 일부 Test 성공, GitHub DNS 실패, Java/Gradle/Node/DB/Browser 부재, 단일 Gate 실패, Checkpoint ZIP, 환경 제약 발견. Runtime이 불가능한 항목만 `미검증`으로 두고 Source·Test·SQL·Config·Frontend·OpenAPI·Generator·Script·Evidence는 계속 완성한다.

## 2. Git·삭제 안전

사용자 승인 없이 Commit, Push, Pull/Merge, Branch, Tag, PR, Release, Reset, Restore, Stash, Clean, History 변경, 파일 삭제·이동을 하지 않는다. `git clean`, `git reset --hard`, `git restore .`를 실행하거나 제안하지 않는다. 삭제가 필요하면 Root 상대경로 파일별 `DELETE_MANIFEST.csv`를 작성하고 실제 삭제하지 않는다. 보호 경로를 변경·삭제하지 않는다.

## 3. 개발 순서

### Wave 0 — 기준선·Evidence·Release Gate
`AB-R6-001~004,013~017,038~040`
- current result SHA와 Evidence/Manifest/clean tree 결속
- master/PR Release workflow, exact-SHA clean qualification
- Java25/Gradle9.1, ADM/BZA, Browser, DB3, distributed Gate non-optional
- 실제 mutation 실행과 actual child-process security test
- Codex 독립검수는 최종 result SHA에서 수행

### Wave 1 — Approval·Security·Concurrency
`AB-R6-019~027`
- ownerModule+ownerCommand+actionType+targetType exact tuple Registry/DB/API
- stale RUNNING lease/deadline/sweeper/reconcile 및 2-instance process-kill
- Provider가 우회할 수 없는 proof 검증 경계
- TTL+cluster-safe nonce consumption single-use
- prod/stg SecretProvider/KMS/SecretRef fail-closed
- policy immutable/active overlap DB3 차단
- owner success 후 DB outage durable UNKNOWN/reconcile
- BAT localhost/default identity 제거
- persistent provider CAS/idempotency/reconcile

### Wave 2 — ADM/BZA/OpenAPI/Browser
`AB-R6-005~018`
- 63 Route·Menu·Button·Action 단일 정본
- 4개 누락 메뉴 정책 확정 및 실제 탐색 경로
- 12 Route 55 Operation 실제 Component Consumer 연결
- generic Workbench의 권한/Strict JSON/승인/사유/감사 우회 제거
- 실제 `/adm/api/auth/me` session buttonIds reactive 사용
- Backend Runtime OpenAPI → generated client → actual consumer 단방향 정본
- stale generated artifact 0, regeneration 후 diff 0
- ADM/BZA actual authenticated Browser E2E
- 메뉴별 CRUD/search/paging/detail/state/CAS/reason/audit/error 401/403/404/409/422/429/500/503
- 실시간 메뉴별 freshness SLA, SSE/WebSocket/bounded polling, reconnect/backoff/stale/fallback/multi-instance

### Wave 3 — EDU 135 Semantic/Runtime
`AB-R6-028~039`
- exact 135 유지; 숫자만 맞추지 않음
- manual == catalog == scenario-contract == handler == consumer == test 필드별 equality
- requiredRole/readOnly/requiredFields/states/steps/failure/idempotency/version/lease/external/compensation/retry exact parity
- deterministic double은 shared engine Unit에만 사용
- Release Evidence는 real JDBC/Batch/Gateway/HTTP/File/Process/Outbox 사용
- 135 ID별 Unit/Integration/Failure/Recovery/Concurrency를 고유 업무 의미로 구현
- EDU-ADM 17개는 승인/SoD, IDOR/masking, CAS conflict browser, bulk partial result, incident/recovery, topology/trace, notification escalation, session expiry/relogin/CSRF까지 executable example로 구현
- OPS Sandbox는 Sandbox로 표시하고 제품 Runtime PASS Evidence로 사용 금지


### Wave 4 — QA 관리자 추가 상용화 강화
`MGR-HARDEN-001~012`
- Repository 전체 Ownership/Public API·SPI/Internal/Consumer 회귀
- 모든 상태변경 Command reliability 표준화
- DB3 전체 lifecycle·mixed-version·backup/restore
- Final artifact SBOM/license/vulnerability/signature
- Resource/load/soak/backpressure와 Observability/SLI/SLO
- Repository-wide security negative corpus
- DR/power-loss/selective rollback
- LOCAL_DEV/REMOTE/OFFLINE artifact consumer
- Generator create→runtime→remove→regenerate
- 양방향 Traceability·Hygiene·Compatibility/Failure Matrix

Wave 4는 QA Finding을 대체하지 않는다. AB-R6-001~040 구현 중 같은 Source와 Gate에 함께 반영하고, 별도 `HARDENING_STATUS.csv`로 수행·검증 상태를 기록한다.


## 3.1 공통 원인 우선 처리

ID별 국소 Patch 전에 다음 Root Cause를 묶어 제거한다.

1. Canonical Drift — Route/Menu/Permission/OpenAPI/Generated/EDU Catalog 단일 정본
2. Runtime-Provenance Disconnect — Source/result/artifact/evidence SHA 결속
3. Security Capability Boundary — Framework-owned 권한·승인·Secret·proof 강제
4. Recovery Gap — RUNNING/UNKNOWN/process-kill/DB outage durable reconcile
5. Synthetic False-Green — 문자열·double·synthetic response를 actual mutation/runtime으로 대체
6. Template False-Completion — 공통 Wrapper 외 ID별 executable business semantics

각 Root Cause 수정 후 Repository 전체에서 동일 패턴을 검색하고 잠복 결함을 함께 보정한다.

## 4. 각 Finding 완료 절차

`Acceptance 확인 → 기존 Source/Consumer/호출 경로 추적 → 결함 재현 → 제품 구현 → Test/Assertion → Negative/Concurrency/Recovery → 실제 또는 타당한 대체검증 → Evidence → 원장 갱신 → 독립 자체검수`

Interface·DTO·Swagger·Sample·문자열 Gate만으로 완료 처리하지 않는다. 동일 원인의 잠복 결함을 Repository 전체에서 찾아 함께 수정한다.

## 5. 필수 결과 파일 갱신

개발 완료 시 다음 파일을 반드시 생성 또는 갱신해 Overlay에 포함한다. QA 원문/QA 판정 컬럼은 변경하지 않고 `개발GPT_*` 영역과 개발 산출물만 갱신한다.

1. `cpf-docs/work/v9i/dev/r6i/FINDING_STATUS.csv`
   - AB-R6-001~040 각각 `개발GPT_개발상태`, `개발GPT_검증상태`, 변경 Source, Consumer, 호출 경로, Test, Evidence, 미완료 사유
2. `cpf-docs/work/v9i/dev/r6i/REQUIREMENT_STATUS.csv`
   - FDEV-001~025의 development_status와 verification_status 분리
3. `cpf-docs/work/v9i/dev/r6i/CHANGE_MANIFEST.csv`
   - Root 상대경로, add/modify/delete-request, Finding/Requirement, 영향, hash
4. `cpf-docs/work/v9i/dev/r6i/TEST_AND_EVIDENCE.md`
   - command, cwd, OS/tool version, start/end, exit code, stdout/stderr, log/artifact SHA-256, expected/actual, PASS/FAIL/NOT_EXECUTED
5. `cpf-docs/work/v9i/dev/r6i/OPEN_ISSUES.md`
   - 환경·권한·Secret·외부 의존으로 실제 미실행인 것만 기록. Source 미구현을 환경 이슈로 숨기지 않음
6. `cpf-docs/work/v9i/dev/r6i/PACKAGE_MANIFEST.json`
   - instruction_basis_sha, work_start_sha, result_commit_sha, evidence_source_sha, 파일 수/hash, 보호 경로, delete manifest
7. `cpf-docs/work/v9i/dev/r6i/SHA256SUMS.txt`
8. `cpf-docs/work/v9i/dev/r6i/DELETE_MANIFEST.csv`
9. `cpf-docs/work/v9i/dev/r6i/DEVELOPMENT_SESSION_RESULT.csv`
   - 전체/완료/부분/미완료/미검증 수량, 시작·종료 SHA, 최종 자체판정
10. `cpf-docs/work/v9i/dev/r6i/HANDOVER.md`
11. `cpf-docs/work/v9i/dev/r6i/CODEX_REVIEW_REQUEST.md`
12. Root Overlay ZIP과 ZIP SHA-256
13. `cpf-docs/work/v9i/dev/r6i/HARDENING_STATUS.csv` — MGR-HARDEN-001~012
14. `cpf-docs/work/v9i/dev/r6i/RUNTIME_QUALIFICATION_MATRIX.csv`
15. `cpf-docs/work/v9i/dev/r6i/COMPATIBILITY_MATRIX.csv`
16. `cpf-docs/work/v9i/dev/r6i/ARTIFACT_SUPPLY_CHAIN_EVIDENCE.md`
17. `cpf-docs/work/v9i/dev/r6i/TRACEABILITY_MATRIX.csv`
18. `cpf-docs/work/v9i/dev/r6i/REPOSITORY_HYGIENE_REPORT.md`

## 6. 검증 필수 묶음

- `git diff --check`, secret/hygiene, ownership/dependency, JSON/CSV/OpenAPI/route/catalog duplicate
- Java25 + Gradle9.1 clean build/test/publication
- ADM/BZA `npm ci`, verify, actual authenticated E2E/a11y/responsive
- Oracle/PostgreSQL/MariaDB install→migration→seed→upgrade→runtime→rollback
- 2+ instance race, broker duplicate/DLQ, process kill, reconcile
- EDU 135 exact semantic equality + real consumer runtime
- actual mutation survivor 0
- exact result SHA clean snapshot에서 최종 재실행

실행하지 않은 검증은 PASS가 아니다. READY/PLANNED/NOT_EXECUTED도 PASS가 아니다.

## 7. 개발GPT 자체 완료 조건

- AB-R6-001~040의 Source 구현 상태가 모두 완료
- MGR-HARDEN-001~012가 모두 개발 완료되고 실행 대상은 검증 PASS
- 실행 가능한 필수 Gate는 모두 Exit 0
- 외부 환경상 미실행 항목은 정확한 재실행 조건과 Evidence 요구를 기록
- Consumer 없는 추상화, stale generated, false-green, duplicate catalog 없음
- 보호 경로 무삭제, Delete Manifest 승인 대기
- 원장 수량과 package manifest/hash 일치
- 결과 ZIP은 Windows 경로 길이를 고려한 Root 상대경로 단일 Overlay

부분 구현이나 미실행 Runtime이 남으면 최종 완료라고 쓰지 말고 `미완료` 또는 `미검증`으로 제출한다. 다만 환경 제약을 이유로 구현 가능한 나머지 작업을 중단하지 않는다.
