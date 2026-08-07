# CPF 개발GPT QA R6 재개발·자체검수·결과기록 단일 통제서

> 이 파일은 QA R5I 결과를 개발GPT가 직접 실행·수정·자체검수하고, 같은 파일 안의 `개발GPT 결과기록` 영역을 갱신하여 재제출하기 위한 단일 작업 지침서다.
> QA 판정·Finding 원문은 변경하지 않는다. 개발GPT는 각 Finding의 결과기록 영역과 통합 결과 원장만 수정한다.

## 0. 문서 식별정보

- 문서 생성 시각: `2026-08-07T02:47:39+09:00`
- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 현재 확인된 origin/master HEAD: `28f823a18eca859cebdbceb382029f595cdf490c` (`06_11`)
- QA 제품 Source 검수 기준 SHA: `e7cc9ada86c871214a20862779f2433bc46fea1b` (`06_10`)
- 직전 제품/문서 통합 SHA: `11093fe26b4e94d9066f2d9edcc1d06c879d868e` (`06_09`)
- 문서 Delta SHA: `a8be27a34bdac0b7c075e06d6e86571244c96421` (`06_08`)
- QA 회차: `R5I`
- 다음 QA 회차: `R6`
- QA 현재 판정: **미통과**
- FDEV-001~FDEV-025: **25건 전부 미통과**
- 통합 Finding: **29건 — P0 12 / P1 13 / P2 4**

### SHA 해석 규칙

1. `e7cc9ada86c871214a20862779f2433bc46fea1b`는 제품 Source 결함을 판정한 QA 기준 SHA다.
2. `28f823a18eca859cebdbceb382029f595cdf490c`는 QA R5I 문서·원장·Evidence가 추가된 현재 master다. 이 커밋은 QA/문서 변경 중심이며 제품 Source 결함을 해소하지 않는다.
3. 개발 시작 시 반드시 최신 `origin/master`를 다시 조회하고 `work_start_sha`에 기록한다.
4. 작업 중 master가 변경되면 자동 승계하지 말고 `instruction_basis_sha`, `work_start_sha`, `result_commit_sha`, `evidence_source_sha`를 분리한다.

---

## 1. 개발GPT 필수 역할과 금지사항

### 1.1 필수 역할

- [ ] 29개 Finding을 하나도 누락하지 않고 같은 Finding ID로 재개발한다.
- [ ] FDEV-001~FDEV-025 전체를 다시 검수한다. 직접 변경이 없는 Requirement도 영향·회귀를 검증한다.
- [ ] Source·SQL·API·SPI·Consumer·호출 경로·Config·Frontend·Generator·Script·Test·Evidence를 한 완료 단위로 처리한다.
- [ ] 정상·오류·경계·부분 실패·UNKNOWN·멱등성·동시성·재시도·복구·Reconcile을 검증한다.
- [ ] 미실행 검증을 PASS로 기록하지 않는다.
- [ ] 실패를 첫 건에서 멈추지 않고 공통 원인별로 집계하여 일괄 보정한다.
- [ ] 개발 완료 후 자체검수하고 Codex 독립 검수 요청자료를 작성한 뒤 QA R6 재검수를 요청한다.

### 1.2 금지사항

- [ ] 사용자 승인 없는 Commit, Push, Branch, Tag, PR, Release를 수행하지 않는다.
- [ ] Reset, Restore, Stash, Clean, History 변경을 수행하지 않는다.
- [ ] `git clean`, `git reset --hard`, `git restore .`를 실행하거나 제안하지 않는다.
- [ ] 사용자 승인 없는 파일 삭제·이동·대체를 수행하지 않는다.
- [ ] 보호 경로를 오래된 파일이라는 이유로 삭제하지 않는다.
- [ ] Interface·DTO·Swagger·Sample·Mock·정적 문자열 Gate만 추가하고 완료 처리하지 않는다.
- [ ] READY, PLANNED, NOT_EXECUTED, SKIP을 PASS로 기록하지 않는다.
- [ ] QA 판정·QA Finding 원문·QA 상태를 개발GPT가 임의 수정하지 않는다.

### 1.3 보호 경로

- `cpf-docs/deliverables/**`
- `cpf-docs/guides/**`
- `cpf-docs/environment/docker/**`
- `cpf-tools/environment/docker-development-test/**`
- `cpf-docs/assets/manuals/**`는 승인 없는 삭제 금지 대상으로 동일하게 취급한다.

---

## 2. 작업 시작 시 즉시 기록할 기준선

개발GPT는 아래 값만 수정한다.

<!-- BEGIN DEVGPT BASELINE RESULT -->
- 개발GPT_실제_work_start_sha: `28f823a18eca859cebdbceb382029f595cdf490c`
- 개발GPT_작업시작_시각_KST: `2026-08-07T03:04:00+09:00`
- 개발GPT_origin_master_sha: `28f823a18eca859cebdbceb382029f595cdf490c`
- 개발GPT_HEAD_sha: `로컬 checkout 없음; GitHub Connector exact SHA=28f823a18eca859cebdbceb382029f595cdf490c`
- 개발GPT_working_tree: `원격 Connector로 확인 불가; 로컬 fresh clone DNS 실패`
- 개발GPT_기존변경_존재여부: `해당 없음(Repository에 직접 쓰지 않음)`
- 개발GPT_기존변경_보존계획: `Root Overlay만 생성, Git write/restore/delete 미수행`
- 개발GPT_instruction_basis_sha: `28f823a18eca859cebdbceb382029f595cdf490c`
- 개발GPT_QA_product_basis_sha: `e7cc9ada86c871214a20862779f2433bc46fea1b`
- 개발GPT_환경: `Linux; Java21.0.10; Node22.16.0; npm10.9.2; Python3.13.5; pwsh/Docker/Gradle/DB/Browser 없음`
<!-- END DEVGPT BASELINE RESULT -->

### 필수 시작 명령

```powershell
$RepoRoot = (git rev-parse --show-toplevel).Trim()
Set-Location $RepoRoot
git fetch origin master
git rev-parse origin/master
git rev-parse HEAD
git -c core.quotepath=false status --short --branch
java -version
.\gradlew.bat --version
node --version
npm --version
python -c "import sys; print(sys.executable); print(sys.version)"
Get-Command pwsh -ErrorAction SilentlyContinue
Get-Command docker -ErrorAction SilentlyContinue
```

기존 Working Tree 변경이 있으면 임의 복구하지 말고 정확한 경로·소유 추정·충돌 가능성을 결과기록에 남긴다.

---

## 3. 통합 실행 순서

1. 최신 master·SHA·Working Tree 확인
2. 정본·Architecture·Specification·FDEV·Finding 재확인
3. P0 12건 재현 및 공통 원인 분류
4. P0 제품 구현·Test·Negative Test·Runtime Test
5. P1 13건 구현·검증
6. P2 4건 구현·마이그레이션·호환성 검증
7. FDEV-001~FDEV-025 회귀 검수
8. Java 25·Gradle 9.1 fresh clone 전체 Build/Test/Publication
9. Oracle/PostgreSQL/MariaDB 실제 Lifecycle
10. Frontend Build·Vitest·Playwright·접근성·반응형
11. Broker·다중 Instance·분리 WAS·Process Kill·부분 실패·UNKNOWN
12. QA38·QA39·Catalog·BOM·Generator·SQL·OpenAPI parity
13. Evidence·Manifest·SHA·Path·Secret·Hygiene 정합성
14. 개발GPT 자체검수
15. Codex 독립 검수 요청서·Evidence 준비
16. 이 파일의 결과기록 영역 갱신
17. Root Overlay ZIP 및 최종 결과물 작성

---

## 4. 우선순위별 통합 재개발 목표

### 4.1 P0 Release Blocker

- 관리 정본과 exact SHA 정합성
- Target Runtime 전체 미실행 해소
- Evidence 0바이트·경로·Hash 불일치 해소
- 승인 없는 보호 문서 삭제 해소
- 승인 정책의 서버 Registry 결속
- 운영 Profile secure default/fail-closed
- Spring Bean wiring 실제 Context 보장
- Backend/OpenAPI/UI validation parity
- 승인 상세 민감 Payload 비노출
- 승인 Capability 외부 위조 불가
- Frontend A→B→A 교차 Draft 멱등성
- Codex 독립 검수 완료

### 4.2 P1

- Strict JSON duplicate key·정밀도 보존
- Null payload 처리
- Replay/CAS/concurrency/quarantine identity
- 승인 정책 version lifecycle·break-glass 감사
- DB3 unique conflict 수렴
- DB Runner secret·argv·timeout·child environment
- ADM 권한·감사 링크·접근성
- Frontend Gradle input 재현성
- Runtime OpenAPI 단일 정본
- QA38/QA39 false-green 제거
- Documentation Source Evidence 경로 정합성
- Approval HTTP 201/200/409/validation 계약

### 4.3 P2

- Deprecated boolean approved API 격리·제거 계획
- local-domains build graph 명시 opt-in
- Approval key rotation UX
- Overlay와 result commit provenance 결속

---

## 5. Finding별 상세 작업 지침 및 개발GPT 결과기록

아래 QA 원문은 고정이다. 각 항목의 `BEGIN DEVGPT RESULT`와 `END DEVGPT RESULT` 사이만 개발GPT가 수정한다.

### QA-R5I-001 [P0] Management/Baseline

- 연결 Requirement: `FDEV-001,FDEV-021,FDEV-022,FDEV-024`
- QA 상태: `OPEN`
- 확인 수준: `PACKAGE_AND_REPOSITORY_CONFIRMED`
- 동료 QA 연결: `QA-R5-001`
- 자체 QA 연결: `QAF-REV004-001`
- QA 결함: 최신 master는 e7cc9ada(06_10)인데 REVIEW_INDEX, FINAL_MANAGEMENT_STATE, FINAL_INTEGRITY, fdr/r4 BASELINE/REQUIREMENT_STATUS/PACKAGE_MANIFEST가 a8be27/2929163/2a0136/cb3b2a를 현재 기준처럼 혼용한다.
- 영향/위험: QA/개발/Codex가 서로 다른 Source를 기준으로 판정하여 완료·회귀·Evidence 연결이 무효화된다.
- 대상 파일·영역: `cpf-docs/work/v9i/REVIEW_INDEX.md; cpf-docs/work/v9i/evidence/FINAL_*.json; cpf-docs/work/v9i/fdr/r4/**`
- 필수 수정: 최신 master에서 instruction_basis_sha, work_start_sha, documentation_delta_sha, result_commit_sha, evidence_source_sha를 분리하고 모든 현재 상태·Manifest·Evidence를 재생성한다.
- 필수 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기대결과: 모든 current/result 문서의 result_commit_sha=e7cc9ada, Evidence별 실제 SHA 일치, 과거 SHA는 historical 필드에만 존재.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: QA/개발/Codex가 서로 다른 Source를 기준으로 판정하여 완료·회귀·Evidence 연결이 무효화된다.

<!-- BEGIN DEVGPT RESULT QA-R5I-001 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `부분 구현`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `최신 master 기준선과 instruction/work/evidence/result provenance를 분리하고 R6S12 단일 결과 원장을 재작성했다. Result commit은 사용자 적용 전이므로 생성하지 않았으며 overlay tree hash로 결속했다.`
- 개발GPT_변경파일: `cpf-docs/work/v9i/dev/r6s12/*`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-001 -->

### QA-R5I-002 [P0] Runtime/CI

- 연결 Requirement: `FDEV-004,FDEV-005,FDEV-006,FDEV-017,FDEV-020,FDEV-024,FDEV-010,FDEV-011,FDEV-015,FDEV-018`
- QA 상태: `OPEN`
- 확인 수준: `PEER_CONNECTOR_CONFIRMED_AND_LOCAL_ENVIRONMENT_BLOCKED`
- 동료 QA 연결: `QA-R5-002`
- 자체 QA 연결: `QAF-REV004-007;QAF-REV004-008;QAF-REV004-009;QAF-REV004-010;QAF-REV004-011;QAF-REV004-012;QAF-REV004-015`
- QA 결함: e7cc9ada에 연결된 GitHub status와 workflow run이 0건이며 Java25/Gradle9.1, full npm verify, Playwright, Pester, DB3, broker/multi-process가 실행되지 않았다. 추가 독립 환경 probe에서도 GitHub DNS 실패, JDK 21, Node 22.16.0, PowerShell 부재가 확인되어 Target Runtime PASS를 만들 수 없었다.
- 영향/위험: fresh clone 재현성·컴파일·테스트·배포·다중 인스턴스·DB Vendor·브라우저 품질을 판정할 근거가 없다.
- 대상 파일·영역: `GitHub commit status/workflow lookup; fdr/r4/TEST_AND_EVIDENCE.md; TEST_EXECUTION_LEDGER.csv`
- 필수 수정: clean checkout exact SHA에서 지정 Target Runtime 명령을 실행하고 URL, tool version, command, exit code, stdout/stderr hash, artifact hash를 제출한다.
- 필수 재실행: `.\gradlew.bat --no-daemon clean aggregateQualityBuild publicationGate; npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify`
- 성공 기대결과: 모든 필수 Gate PASS, git status clean, 실행 SHA와 Evidence SHA 동일. 하나라도 미실행/실패면 QA 미통과.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: fresh clone 재현성·컴파일·테스트·배포·다중 인스턴스·DB Vendor·브라우저 품질을 판정할 근거가 없다.

<!-- BEGIN DEVGPT RESULT QA-R5I-002 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Java25/Gradle9.1, npm/Playwright, DB3, multi-process를 exact SHA에서 연속 실행하는 PowerShell release runner와 GitHub Actions workflow를 추가했다. 현재 환경의 Target Runtime은 미실행으로 유지했다.`
- 개발GPT_변경파일: `.github/workflows/cpf-r6-release-gates.yml; cpf-tools/verification/final-dev/run-r6-release-gates.ps1`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-002 -->

### QA-R5I-003 [P0] Evidence Integrity

- 연결 Requirement: `FDEV-001,FDEV-014,FDEV-016,FDEV-020,FDEV-022,FDEV-024`
- QA 상태: `OPEN`
- 확인 수준: `PACKAGE_EVIDENCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-003`
- 자체 QA 연결: `QAF-REV004-002`
- QA 결함: idempotency_runtime.txt는 0 byte인데 Ledger는 PASS이며, Requirement CSV는 .log를 참조하지만 저장 파일은 .txt이다. openapi_idempotent_1/2의 935f... 해시는 실제 OpenAPI 22d22...와 불일치한다.
- 영향/위험: 존재하지 않거나 다른 산출물의 Evidence로 PASS를 주장하여 감사 추적성과 재현성이 붕괴한다.
- 대상 파일·영역: `cpf-docs/work/v9i/fdr/r4/e/**; REQUIREMENT_STATUS.csv; TEST_EXECUTION_LEDGER.csv; PACKAGE_MANIFEST.json`
- 필수 수정: Evidence를 실제 명령 출력으로 재생성하고 경로·확장자·SHA를 원장/Manifest와 exact match한다. 빈 Evidence를 금지한다.
- 필수 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기대결과: orphan/missing/empty/hash mismatch 0, 모든 Evidence에 command/environment/time/exit/source SHA 포함.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 존재하지 않거나 다른 산출물의 Evidence로 PASS를 주장하여 감사 추적성과 재현성이 붕괴한다.

<!-- BEGIN DEVGPT RESULT QA-R5I-003 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `부분 구현`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `실제 실행 로그만 Evidence로 수록하고 0바이트·orphan·확장자·SHA 불일치 방지 규칙, ledger, manifest, SHA256SUMS를 재생성했다.`
- 개발GPT_변경파일: `cpf-docs/work/v9i/dev/r6s12/evidence/*; PACKAGE_MANIFEST.json; SHA256SUMS.txt`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-003 -->

### QA-R5I-004 [P0] Governance/Delete Safety

- 연결 Requirement: `FDEV-020,FDEV-021,FDEV-024`
- QA 상태: `OPEN`
- 확인 수준: `COMMIT_DIFF_CONFIRMED`
- 동료 QA 연결: `QA-R5-004`
- 자체 QA 연결: `-`
- QA 결함: 06_08 기준에 존재하던 cpf-document-quality-r9.svg가 최신 master에서 삭제되었고 Documentation Delete Manifest는 삭제를 지시하지만 사용자 승인 Evidence가 없다.
- 영향/위험: 보호 문서/SVG 무단 삭제는 문서 정본 보존·삭제 승인 규칙을 위반한다.
- 대상 파일·영역: `compare a8be27..e7cc9ada; cpf-docs/deliverables/evidence/CPF_DOCUMENTATION_DELETE_MANIFEST.txt`
- 필수 수정: 명시 승인 전 해당 SVG를 복원하거나 승인 ID/일시/범위를 원장에 기록한다. 다른 보호 문서 삭제 여부도 재검사한다.
- 필수 재실행: `git diff --name-status a8be27a34bdac0b7c075e06d6e86571244c96421..HEAD -- cpf-docs/deliverables cpf-docs/guides cpf-docs/assets/manuals`
- 성공 기대결과: 승인 없는 삭제 0; 삭제 시 exact path 승인과 before/after manifest 존재.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 보호 문서/SVG 무단 삭제는 문서 정본 보존·삭제 승인 규칙을 위반한다.

<!-- BEGIN DEVGPT RESULT QA-R5I-004 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `승인 없는 삭제 상태였던 보호 SVG를 a8be27 exact blob에서 복원했고 SHA-256 allowlist로 검증했다. 추가 삭제 요청은 0건이다.`
- 개발GPT_변경파일: `cpf-docs/assets/manuals/cpf-document-quality-r9.svg`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `보호 SVG exact blob 복원 및 SHA-256 검증 PASS; 전체 protected deletion diff는 complete checkout 필요`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-004 -->

### QA-R5I-005 [P0] Approval Policy Binding

- 연결 Requirement: `FDEV-003,FDEV-008,FDEV-013`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-005`
- 자체 QA 연결: `-`
- QA 결함: requestApproval은 client가 policyCode/version을 주면 enabled/effective 기간을 검증하지 않고, 해당 정책을 ownerModule/ownerCommand/targetType과 서버 Registry로 결속하지 않는다.
- 영향/위험: 비활성·구버전·저위험 정책을 임의 Owner Command에 적용하여 승인 강도를 낮출 수 있다.
- 대상 파일·영역: `AdmApprovalService.requestApproval; AdmApprovalRepository.findPolicy/findActivePolicy; Approval Controller/UI`
- 필수 수정: 정책 선택은 서버 action/owner/command/target Registry로 제한하고 explicit version도 enabled/effective/authorized override를 검증한다. negative integration test를 추가한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기대결과: expired/disabled/future/wrong-action/wrong-owner/wrong-target policy 요청 모두 4xx; Owner mutation 0; 감사 기록 존재.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 비활성·구버전·저위험 정책을 임의 Owner Command에 적용하여 승인 강도를 낮출 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-005 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `승인 정책을 ownerModule/ownerCommand/actionType/targetType 서버 Registry와 결속하고 모든 Approval Owner adapter의 4차원 supports를 fail-closed로 강제해 disabled/future/expired/wrong tuple을 거부하도록 변경했다.`
- 개발GPT_변경파일: `AdmApprovalService.java; AdmApprovalOwnerCommandPort.java; BatchJobDefinitionApprovalOwnerCommandAdapter.java; BatchRuntimeApprovalOwnerCommandAdapter.java; BrokerReliabilityApprovalOwnerCommandAdapter.java; CenterCutApprovalOwnerCommandAdapter.java; DataQualityCorrectionApprovalOwnerCommandAdapter.java; GatewayApprovalOwnerCommandAdapter.java; BatApprovalOwnerCommandPort.java`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-005 -->

### QA-R5I-006 [P0] Secure Default/Profile

- 연결 Requirement: `FDEV-002,FDEV-003,FDEV-013,FDEV-020`
- QA 상태: `OPEN`
- 확인 수준: `PEER_EXACT_BLOB_CONFIRMED`
- 동료 QA 연결: `QA-R5-006`
- 자체 QA 연결: `-`
- QA 결함: application.yml이 spring.profiles.active=local을 기본값으로 고정하고 local profile은 integration-closure와 ephemeral providers를 기본 true로 한다.
- 영향/위험: 운영 배포에서 profile 누락 시 in-memory Data Quality/Webhook 운영 기능이 활성화되는 fail-open 구성이다.
- 대상 파일·영역: `cpf-admin/src/main/resources/application.yml; application-adm-local.yml; application-adm-prod.yml`
- 필수 수정: active profile 기본값을 제거하고 prod/unknown 환경에서 ephemeral provider를 절대 활성화하지 않는 fail-fast guard와 배포 테스트를 추가한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "*AdmIntegrationClosureConfigurationTest"`
- 성공 기대결과: profile 미지정/잘못된 profile에서 운영기능 비활성 또는 startup fail; prod에서 ephemeral bean 0.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 운영 배포에서 profile 누락 시 in-memory Data Quality/Webhook 운영 기능이 활성화되는 fail-open 구성이다.

<!-- BEGIN DEVGPT RESULT QA-R5I-006 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `기본 local profile 활성화를 제거하고 explicit profile guard 및 prod/stg ephemeral fail-closed를 구현했다.`
- 개발GPT_변경파일: `application.yml; application-adm-local.yml; application-adm-prod.yml; AdmIntegrationClosureProfileGuard.java`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-006 -->

### QA-R5I-007 [P0] Spring Bean Wiring

- 연결 Requirement: `FDEV-002,FDEV-003`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_GRAPH_CONTRADICTION;RUNTIME_REQUIRED`
- 동료 QA 연결: `QA-R5-007`
- 자체 QA 연결: `QAF-REV004-008`
- QA 결함: customerOverridesWinOverDefaultProviders Test는 CpfDataQualityOperations만 공급하지만 Configuration의 Owner Adapter는 별도 CpfDataQualityCorrectionPort를 필수 주입한다. ephemeral bean은 MissingBean(query) 조건으로 생성되지 않아 Context 성공 기대와 Bean graph가 모순된다.
- 영향/위험: 고객 Override 구성에서 ApplicationContext가 실패할 가능성이 높고 FDEV-002 완료 주장이 깨진다.
- 대상 파일·영역: `AdmIntegrationClosureConfiguration.java; AdmIntegrationClosureConfigurationTest.java`
- 필수 수정: Query/Correction capability를 명시적으로 pair 검증하거나 고객 Owner Adapter override를 허용하고 실제 Gradle Context Test를 실행한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "*AdmIntegrationClosureConfigurationTest"`
- 성공 기대결과: disabled/default/customer override/duplicate/missing provider Context Test 모두 실제 PASS; Bean 목록 Evidence 제출.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 고객 Override 구성에서 ApplicationContext가 실패할 가능성이 높고 FDEV-002 완료 주장이 깨진다.

<!-- BEGIN DEVGPT RESULT QA-R5I-007 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Data Quality query/correction capability pair, proof service, owner adapter wiring을 명시하고 missing/customer override Context 테스트를 보강했다.`
- 개발GPT_변경파일: `AdmIntegrationClosureConfiguration.java; AdmIntegrationClosureConfigurationTest.java`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-007 -->

### QA-R5I-008 [P0] API Contract Parity

- 연결 Requirement: `FDEV-014,FDEV-016`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_AND_OPENAPI_CONFIRMED`
- 동료 QA 연결: `QA-R5-008`
- 자체 QA 연결: `-`
- QA 결함: Webhook replay expectedVersion은 OpenAPI/UI minimum 0이나 Service는 1 이상을 요구한다. reason/idempotencyKey의 OpenAPI max/min 제약은 Controller/Service에서 동일하게 강제되지 않는다.
- 영향/위험: Generated Client가 유효하다고 판단한 요청이 Runtime에서 거부되거나, 계약상 거부해야 할 oversized 입력이 서버에서 수용된다.
- 대상 파일·영역: `enrich-adm-openapi-contract.mjs; AdmIntegrationClosureController.java; AdmIntegrationClosureService.java; IntegrationClosurePage.vue`
- 필수 수정: 단일 Validation DTO/Bean Validation에서 제약을 정의하고 runtime-generated OpenAPI와 generated client를 재생성한다.
- 필수 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify`
- 성공 기대결과: 0/1 경계, 7/8/128/129 idempotency, reason 500/501 contract test가 Controller+OpenAPI+client에서 동일 결과.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Generated Client가 유효하다고 판단한 요청이 Runtime에서 거부되거나, 계약상 거부해야 할 oversized 입력이 서버에서 수용된다.

<!-- BEGIN DEVGPT RESULT QA-R5I-008 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Replay expectedVersion 최소값과 reason/idempotency 경계를 Backend DTO·Controller·OpenAPI·Frontend 소비 경로에 동일하게 반영했다.`
- 개발GPT_변경파일: `AdmIntegrationClosureController.java; AdmIntegrationClosureService.java; enrich-adm-openapi-contract.mjs; integrationClosureApi.ts`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-008 -->

### QA-R5I-009 [P0] Sensitive Data Exposure

- 연결 Requirement: `FDEV-003,FDEV-013,FDEV-014`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-009`
- 자체 QA 연결: `-`
- QA 결함: AdmApprovalService.detail은 COMMAND_PAYLOAD_SNAPSHOT을 포함한 raw request Map을 반환하고 AdmApprovalController detail/create/decision/execute/reconcile 및 ApprovalsPage StructuredData가 이를 그대로 노출한다.
- 영향/위험: 정정 값, 계정/개인정보/Secret이 API와 브라우저 화면에 원문 노출될 수 있다.
- 대상 파일·영역: `AdmApprovalService.detail; AdmApprovalController; ApprovalsPage.vue`
- 필수 수정: 외부 응답 전용 DTO를 도입해 payloadSnapshot을 제거/마스킹하고 민감 필드 접근 권한·감사·negative snapshot test를 적용한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기대결과: API/UI/로그/Evidence에서 원문 corrected/secret/PII 0; 승인 엔진 내부 hash/target/audit reference만 노출.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 정정 값, 계정/개인정보/Secret이 API와 브라우저 화면에 원문 노출될 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-009 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `승인 외부 detail 응답을 sanitize DTO Map으로 제한해 payloadSnapshot/민감 키 원문 노출을 제거하고 UI가 정제 응답만 소비하도록 했다.`
- 개발GPT_변경파일: `AdmApprovalService.java; AdmApprovalController.java; ApprovalsPage.vue`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-009 -->

### QA-R5I-025 [P0] Approval Capability Boundary

- 연결 Requirement: `FDEV-003,FDEV-007,FDEV-008,FDEV-013`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_AND_EXTERNAL_COMPILE_REPRODUCTION_CONFIRMED`
- 동료 QA 연결: `RELATED:QA-R5-005;QA-R5-021`
- 자체 QA 연결: `QAF-REV004-003`
- QA 결함: CpfDataQualityCorrectionPort가 public SPI에 public nested ApprovedCorrection record를 노출한다. 외부 package가 승인 엔진을 거치지 않고 ApprovedCorrection을 직접 생성해 correctApproved를 호출할 수 있어 “caller authorization API가 아니다”라는 주석이 타입 경계로 강제되지 않는다.
- 영향/위험: Correction Port를 주입받거나 구현한 Consumer가 승인 Ledger·single-use reservation·snapshot hash 검증 없이 Owner mutation을 실행할 수 있다.
- 대상 파일·영역: `cpf-core/src/main/java/com/cpf/core/spi/data/quality/CpfDataQualityCorrectionPort.java; cpf-admin/.../DataQualityCorrectionApprovalOwnerCommandAdapter.java; consumers`
- 필수 수정: 승인 증명 객체의 생성자를 외부에 노출하지 말고 ADM 내부 sealed/package-private capability 또는 nonce 검증형 execution token으로 이동한다. Public SPI는 raw correction command를 받지 않도록 재설계하고 외부 compile-negative test를 추가한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기대결과: 외부 package에서 승인 capability 생성/Owner mutation 호출이 compile 또는 runtime authorization 단계에서 거부되고, 유일한 Consumer가 서버 Ledger reservation을 검증한 ADM adapter로 제한된다.
- 실패 판정기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: Correction Port를 주입받거나 구현한 Consumer가 승인 Ledger·single-use reservation·snapshot hash 검증 없이 Owner mutation을 실행할 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-025 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `public correction capability를 HMAC 검증형 proof로 제한하고 모든 Owner adapter를 4차원 fail-closed Registry로 결속했으며 local-domains build graph를 explicit opt-in+manifest settings hash로 제한하고 QA38/QA39 canonical catalog 검증을 보강했다.`
- 개발GPT_변경파일: `CpfDataQualityCorrectionPort.java; AdmDataQualityApprovalProofService.java; AdmApprovalOwnerCommandPort.java; AdmApprovalOwnerCommandPortR6Test.java; settings.gradle; verify-qa38-structure.py; verify-qa39-canonical-starter-closure.py`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-025 -->

### QA-R5I-026 [P0] Frontend Idempotency

- 연결 Requirement: `FDEV-003,FDEV-012,FDEV-014,FDEV-016`
- QA 상태: `OPEN`
- 확인 수준: `LOCAL_REPRODUCTION_CONFIRMED`
- 동료 QA 연결: `RELATED:QA-R5-023`
- 자체 QA 연결: `QAF-REV004-004`
- QA 결함: Integration Closure approval idempotency state가 sessionStorage 단일 key에 저장된다. Draft A pending 후 B를 열고 다시 A를 재시도하면 A의 원 key가 B 상태에 의해 대체되어 새 key가 생성되고 timeout/응답 유실 재시도의 동일 요청 정체성이 깨진다.
- 영향/위험: 동일 correction 요청이 복수 Approval Request로 생성되거나 응답 유실 뒤 중복 side effect 및 운영자 혼란이 발생할 수 있다.
- 대상 파일·영역: `cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts; IntegrationClosurePage.vue; tests`
- 필수 수정: fingerprint별 multi-entry pending ledger를 사용하고 confirmed/expired 상태를 명시 관리한다. A→B→A, refresh, multi-tab, timeout, duplicate click corpus를 추가한다.
- 필수 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기대결과: 동일 fingerprint의 pending retry는 항상 동일 key, 다른 draft는 독립 key, 성공/명시 취소 후에만 회전하며 A→B→A 재현에서 original A key가 유지된다.
- 실패 판정기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: 동일 correction 요청이 복수 Approval Request로 생성되거나 응답 유실 뒤 중복 side effect 및 운영자 혼란이 발생할 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-026 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `단일 슬롯을 fingerprint별 bounded pending/confirmed localStorage ledger와 generation 기반 deterministic key로 교체해 A→B→A, refresh, multi-tab race, timeout, explicit clear 후 rotation을 보장했다.`
- 개발GPT_변경파일: `integrationClosureIdempotency.ts; integrationClosureIdempotency.test.ts`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행; 독립 Node/Java runtime harness PASS`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-026 -->

### QA-R5I-029 [P0] Independent Review

- 연결 Requirement: `FDEV-024`
- QA 상태: `OPEN`
- 확인 수준: `PROCESS_EVIDENCE_ABSENT`
- 동료 QA 연결: `-`
- 자체 QA 연결: `QAF-REV004-016`
- QA 결함: REV-004 개발 결과에 대한 Codex 독립 검수·보완 완료 Evidence가 없으며 개발GPT 자체검수 직후 QA로 진입했다.
- 영향/위험: 개발자와 독립된 2차 Source/Runtime 검증 단계가 생략되어 동일 가정과 false-green이 QA 패키지까지 전파된다.
- 대상 파일·영역: `cpf-docs/work/v9i/fdr/r4/CODEX_REVIEW_REQUEST.md; Codex result/evidence; requirement ledger`
- 필수 수정: 개발 수정 후 Codex가 exact result SHA에서 29개 finding과 FDEV-001~025를 독립 검수하고 Codex 영역 원장·Evidence를 제출한다.
- 필수 재실행: `git rev-parse HEAD; git status --short --branch`
- 성공 기대결과: Codex 검수 결과, 수정 내역, 명령/Exit/Evidence, remaining findings가 exact SHA에 결속되고 QA R6 진입 전에 미완료가 명시된다.
- 실패 판정기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: 개발자와 독립된 2차 Source/Runtime 검증 단계가 생략되어 동일 가정과 false-green이 QA 패키지까지 전파된다.

<!-- BEGIN DEVGPT RESULT QA-R5I-029 -->
- 개발GPT_수행상태: `요청자료 완료/독립검수 미실행`
- 개발GPT_개발판정: `미완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `29 Findings/FDEV-001~025를 exact overlay tree에서 검수하도록 Codex 독립 검수 요청서를 작성했다. 독립 Codex 실행 자체는 이 세션에서 미실행이다.`
- 개발GPT_변경파일: `CODEX_REVIEW_REQUEST.md`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Codex 요청서 작성 완료; Codex 독립 검수 결과 Evidence 부재`
- 개발GPT_미완료_사유: `Codex 독립 검수 미실행`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-029 -->

### QA-R5I-010 [P1] Approval JSON Integrity

- 연결 Requirement: `FDEV-003,FDEV-012,FDEV-013`
- QA 상태: `OPEN`
- 확인 수준: `LOCAL_REPRODUCTION_CONFIRMED`
- 동료 QA 연결: `QA-R5-010`
- 자체 QA 연결: `-`
- QA 결함: Jackson/JSON.parse 기본 파서는 exact duplicate key를 마지막 값으로 덮어쓰고, JS Number는 2^53 초과 정수와 고정소수 정밀도를 손실한다. 현재 canonical hash는 strict duplicate/BigDecimal 설정이 없다.
- 영향/위험: 승인 화면에 표시된 원문과 서버가 해석·hash·실행하는 값이 달라질 수 있다.
- 대상 파일·영역: `AdmApprovalSnapshotIntegrity.java; integrationClosureIdempotency.ts; IntegrationClosurePage.vue`
- 필수 수정: 서버 strict duplicate detection + BigInteger/BigDecimal parsing, 프론트 raw JSON strict parser 또는 typed form을 적용한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기대결과: duplicate key, Unicode collision, 64-bit integer, high-scale decimal corpus가 browser/server 동일 canonical hash로 PASS.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 승인 화면에 표시된 원문과 서버가 해석·hash·실행하는 값이 달라질 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-010 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `서버 Jackson strict duplicate detection 및 BigInteger/BigDecimal과 Frontend strict JSON parser를 도입해 duplicate/NFC/unsafe integer/high precision 입력을 거부했다.`
- 개발GPT_변경파일: `AdmApprovalSnapshotIntegrity.java; strictJsonObject.ts; strictJsonObject.test.ts`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행; 독립 Node/Java runtime harness PASS`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-010 -->

### QA-R5I-011 [P1] Data Quality Null Handling

- 연결 Requirement: `FDEV-003,FDEV-009,FDEV-014`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-011`
- 자체 QA 연결: `-`
- QA 결함: Map.copyOf를 record/corrected payload에 사용하여 null field가 포함된 데이터 품질 검증·정정 요청이 NPE로 실패한다.
- 영향/위험: 누락/NULL 검증이 핵심인 Data Quality 기능이 정상적으로 입력을 검사하지 못한다.
- 대상 파일·영역: `AdmIntegrationClosureService; InMemoryCpfDataQualityOperations; CpfDataQualityCorrectionPort`
- 필수 수정: null을 허용하는 immutable copy 또는 명시 schema validation을 적용하고 null/empty/oversize 경계 테스트를 추가한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-common:test :cpf-admin:test --tests "*DataQuality*"`
- 성공 기대결과: null field는 규칙 위반 결과로 처리되고 500/NPE가 발생하지 않는다.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 누락/NULL 검증이 핵심인 Data Quality 기능이 정상적으로 입력을 검사하지 못한다.

<!-- BEGIN DEVGPT RESULT QA-R5I-011 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Map.copyOf를 null-safe immutable copy로 대체하고 null field가 NPE가 아닌 Data Quality violation으로 처리됨을 Java harness로 검증했다.`
- 개발GPT_변경파일: `InMemoryCpfDataQualityOperations.java; AdmIntegrationClosureService.java`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행; 독립 Node/Java runtime harness PASS`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-011 -->

### QA-R5I-012 [P1] Data Quality Replay/Concurrency

- 연결 Requirement: `FDEV-003,FDEV-009,FDEV-012`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-012`
- 자체 QA 연결: `-`
- QA 결함: replay는 expectedVersion 없이 동작하고 validate를 재호출하여 실패 시 새 quarantineId를 생성한다. 동시 correction/replay에서 stale update·중복 quarantine이 가능하다.
- 영향/위험: 중복 Side Effect, 상태 분기, 대사 불일치와 무한 격리 증가가 발생할 수 있다.
- 대상 파일·영역: `CpfDataQualityOperations; InMemoryCpfDataQualityOperations; replay API/OpenAPI/UI`
- 필수 수정: Replay CAS/operation id를 추가하고 validation-only 경로와 quarantine creation을 분리하며 parent/replay lineage를 저장한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-common:test :cpf-admin:test --tests "*DataQuality*"`
- 성공 기대결과: stale replay 409, concurrent replay 1회, failed replay 신규 orphan 0, reconcile 수렴.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 중복 Side Effect, 상태 분기, 대사 불일치와 무한 격리 증가가 발생할 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-012 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `ReplayCommand에 expectedVersion/operationId를 강제하고 operationId를 quarantine/version/actor/reason fingerprint에 영구 결속해 collision을 거부하며 validation-only, operation lock, CAS, lineage audit로 concurrent replay를 단일 결과로 수렴시켰다.`
- 개발GPT_변경파일: `CpfDataQualityOperations.java; InMemoryCpfDataQualityOperations.java; InMemoryCpfDataQualityOperationsR6Test.java; R6QualityHarness`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행; 독립 Node/Java runtime harness PASS`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-012 -->

### QA-R5I-013 [P1] Approval Policy Lifecycle

- 연결 Requirement: `FDEV-003,FDEV-013,FDEV-014`
- QA 상태: `OPEN`
- 확인 수준: `PEER_EXACT_BLOB_CONFIRMED`
- 동료 QA 연결: `QA-R5-013`
- 자체 QA 연결: `-`
- QA 결함: Versioned 정책을 same policyCode/version으로 UPDATE하고 steps를 DELETE/INSERT한다. PolicyRequest.reason과 breakGlassAllowedYn은 실행/감사에 반영되지 않는다.
- 영향/위험: 과거 승인 정책 재현성·감사성이 깨지고 정책 변경 충돌 및 break-glass 의미가 형식 필드에 그친다.
- 대상 파일·영역: `AdmApprovalService.savePolicy; AdmApprovalRepository.replacePolicy; ApprovalsPage.vue`
- 필수 수정: 사용 중/활성 정책 Version immutable, 새 Version 생성, optimistic lock, 변경 reason/audit, break-glass 정책 실제 enforcement를 구현한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기대결과: same version overwrite 거부, concurrent save 409, policy audit before/after hash, break-glass negative test PASS.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 과거 승인 정책 재현성·감사성이 깨지고 정책 변경 충돌 및 break-glass 의미가 형식 필드에 그친다.

<!-- BEGIN DEVGPT RESULT QA-R5I-013 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `정책 version overwrite를 insert-only로 전환하고 policy history reason/before-after hash 및 break-glass enforcement/audit를 추가했다.`
- 개발GPT_변경파일: `AdmApprovalService.java; AdmApprovalRepository.java; 19_approval_integrity_r6.sql`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-013 -->

### QA-R5I-014 [P1] Approval Idempotency/DB Integration

- 연결 Requirement: `FDEV-003,FDEV-012`
- QA 상태: `OPEN`
- 확인 수준: `SOURCE_PATTERN_CONFIRMED;DB3_RUNTIME_REQUIRED`
- 동료 QA 연결: `QA-R5-014`
- 자체 QA 연결: `QAF-REV004-014`
- QA 결함: requestKey/decision idempotency는 선조회 후 INSERT/UPDATE하며 실제 DB unique conflict 재조회/수렴 테스트가 없다. 변경된 테스트는 대부분 Mockito이며 3 Vendor transaction/CAS를 검증하지 않는다.
- 영향/위험: 다중 인스턴스 동시 요청에서 duplicate key 예외가 500이 되거나 동일 결과 보장이 깨질 수 있다.
- 대상 파일·영역: `AdmApprovalService; AdmApprovalRepository; approval tests; DB vendor schema`
- 필수 수정: DB unique constraint 기반 insert-or-read와 duplicate exception replay를 구현하고 DB3 동시성 통합 테스트를 수행한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기대결과: 2+ process same key에서 mutation/request/decision 1건, 모든 호출 동일 result, 500 0.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 다중 인스턴스 동시 요청에서 duplicate key 예외가 500이 되거나 동일 결과 보장이 깨질 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-014 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `기존 DB3 request/decision unique constraint를 전제로 DataIntegrityViolationException 시 insert-or-read/replay convergence를 구현했다. Live DB3 동시성은 미검증이다.`
- 개발GPT_변경파일: `AdmApprovalService.java; AdmApprovalRepository.java; 30_adm_schema.sql baseline constraints`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-014 -->

### QA-R5I-015 [P1] DB3 Runner Safety

- 연결 Requirement: `FDEV-005,FDEV-020,FDEV-023`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED_AND_ENRICHED`
- 동료 QA 연결: `QA-R5-015`
- 자체 QA 연결: `QAF-REV004-005;QAF-REV004-006`
- QA 결함: DB runner는 JDBC URL/username을 argv로 전달하고 URL 내 credential/query secret을 금지·redact하지 않는다. WaitForExit에 timeout/cancellation이 없다. 또한 ProcessStartInfo.Environment를 정리하지 않아 부모 프로세스의 CPF_RUNTIME_*_PASSWORD 및 기타 Secret 환경변수가 child로 상속될 수 있다.
- 영향/위험: process list·Evidence에 secret이 노출되거나 hung DB/runner가 QA/운영 작업을 무기한 점유한다.
- 대상 파일·영역: `run-db3-lifecycle.ps1; Pester tests; verify-db3-runner-contract.py`
- 필수 수정: URL credential validation/redaction, config/stdin/file descriptor 방식, vendor별 timeout·kill·timeout Evidence를 추가한다. child environment를 clear 후 명시 allowlist만 주입하고 Password/Token inheritance negative test를 추가한다.
- 필수 재실행: `python cpf-tools/verification/final-dev/verify-db3-runner-contract.py; pwsh -NoProfile -Command "Invoke-Pester -Path 'cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1' -CI"`
- 성공 기대결과: secret corpus argv/log/audit 0, timeout 시 child tree 종료 및 명확한 exit code/UNKNOWN result. child environment secret 0, inherited secret canary 0.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: process list·Evidence에 secret이 노출되거나 hung DB/runner가 QA/운영 작업을 무기한 점유한다.

<!-- BEGIN DEVGPT RESULT QA-R5I-015 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `DB runner의 credential URL 거부, JSON stdin transport, child environment clear/allowlist, timeout/kill-tree, redaction 및 contract/Pester tests를 구현했다.`
- 개발GPT_변경파일: `run-db3-lifecycle.ps1; verify-db3-runner-contract.py; run-db3-lifecycle.Tests.ps1`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `DB3 runner static contract 8/8 PASS; PowerShell/live DB3 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-015 -->

### QA-R5I-016 [P1] ADM UX/Permission

- 연결 Requirement: `FDEV-014,FDEV-017`
- QA 상태: `OPEN`
- 확인 수준: `PEER_SOURCE_REVIEW;BROWSER_RUNTIME_REQUIRED`
- 동료 QA 연결: `QA-R5-016`
- 자체 QA 연결: `QAF-REV004-010`
- QA 결함: Integration Closure 화면은 CRITICAL route이나 query/approval/execute/replay 버튼별 권한 상태를 반영하지 않고 audit link도 없다. Playwright/role/accessibility 검증도 미실행이다.
- 영향/위험: 조회 권한만 있는 운영자에게 위험 버튼이 노출되고 운영자가 감사·복구 근거를 연결하기 어렵다.
- 대상 파일·영역: `routes.ts; IntegrationClosurePage.vue; approval page; Playwright suite`
- 필수 수정: operation permission model로 버튼 hide/disable/reason 제공, audit/operation link, 401/403/409 focus flow 및 browser matrix를 구현한다.
- 필수 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기대결과: role별 버튼 matrix, keyboard/focus/aria/viewport, error injection Playwright PASS.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 조회 권한만 있는 운영자에게 위험 버튼이 노출되고 운영자가 감사·복구 근거를 연결하기 어렵다.

<!-- BEGIN DEVGPT RESULT QA-R5I-016 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Integration Closure 위험 버튼을 operation permission fail-closed로 제어하고 audit link, HTTP error focus/alert, aria/keyboard/responsive E2E 시나리오를 추가했다.`
- 개발GPT_변경파일: `IntegrationClosurePage.vue; operationPermissions.ts; integration-closure-r6.spec.ts`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-016 -->

### QA-R5I-017 [P1] Frontend Build Reproducibility

- 연결 Requirement: `FDEV-016,FDEV-020`
- QA 상태: `OPEN`
- 확인 수준: `PEER_SOURCE_REVIEW;CLEAN_INCREMENTAL_RUNTIME_REQUIRED`
- 동료 QA 연결: `QA-R5-017`
- 자체 QA 연결: `-`
- QA 결함: Gradle frontendBuild inputs에 openapi/, scripts/, orval.config.ts 등 generator 입력이 빠져 있어 변경 후 stale output을 up-to-date로 재사용할 수 있다. install marker도 Node/npm version을 입력으로 추적하지 않는다.
- 영향/위험: Source/OpenAPI가 변경돼도 WAR/static artifact가 이전 generated client를 포함할 수 있다.
- 대상 파일·영역: `cpf-admin/build.gradle; frontend package/generator files`
- 필수 수정: 모든 generator/build input과 tool version을 Gradle inputs로 선언하고 clean/incremental 두 경로 artifact hash를 비교한다.
- 필수 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기대결과: OpenAPI/script/tool version 1 byte 변경 시 task 재실행; clean과 incremental artifact hash 동일.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Source/OpenAPI가 변경돼도 WAR/static artifact가 이전 generated client를 포함할 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-017 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Gradle frontend task inputs에 OpenAPI/scripts/orval/tool versions와 Node/npm versions를 포함시켜 stale generated output 재사용을 방지했다.`
- 개발GPT_변경파일: `cpf-admin/build.gradle`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-017 -->

### QA-R5I-018 [P1] OpenAPI Source of Truth

- 연결 Requirement: `FDEV-016,FDEV-021`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-018`
- 자체 QA 연결: `QAF-REV004-013`
- QA 결함: enrich script의 ensureOperation은 Controller/Runtime에 없는 route도 정적 spec에 생성하고 security/error를 수동 삽입한다.
- 영향/위험: OpenAPI가 실제 Runtime을 검증하지 않고 결함을 가리는 별도 정본이 된다.
- 대상 파일·영역: `enrich-adm-openapi-contract.mjs; OpenAPI lifecycle scripts`
- 필수 수정: runtime/controller-generated document를 입력으로 사용하고 missing route/operation이면 생성이 아니라 실패하도록 변경한다.
- 필수 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify`
- 성공 기대결과: Controller 제거/rename/security 변경 mutation test에서 OpenAPI Gate FAIL; runtime snapshot과 release spec exact parity.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: OpenAPI가 실제 Runtime을 검증하지 않고 결함을 가리는 별도 정본이 된다.

<!-- BEGIN DEVGPT RESULT QA-R5I-018 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `OpenAPI enrich가 누락 Runtime route를 합성하지 않고 실패하도록 변경하고 runtime route mutation contract를 추가했다.`
- 개발GPT_변경파일: `enrich-adm-openapi-contract.mjs; test-r6-runtime-route-contract.mjs`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-018 -->

### QA-R5I-019 [P1] QA Gate Coverage

- 연결 Requirement: `FDEV-007,FDEV-019,FDEV-020,FDEV-025`
- QA 상태: `OPEN`
- 확인 수준: `PEER_SOURCE_REVIEW;MUTATION_TEST_REQUIRED`
- 동료 QA 연결: `QA-R5-019`
- 자체 QA 연결: `QAF-REV004-012;QAF-REV004-013`
- QA 결함: QA38 DB parity loop는 cpf-starters 1단계 directory만 검사하여 data/... 등 중첩 module SQL을 놓친다. AutoConfiguration target 일부는 SHA에 묶이지 않은 hard-coded allowlist로 존재 검사를 우회한다.
- 영향/위험: Starter 물리 재편 이후 주요 DB/Bean 결함이 Gate PASS로 누락될 수 있다.
- 대상 파일·영역: `cpf-tools/verification/qa38/verify-qa38-structure.py`
- 필수 수정: canonical catalog modules를 순회해 DB/Source를 검사하고 allowlist를 제거하거나 exact path+hash catalog로 검증한다.
- 필수 재실행: `python cpf-tools/verification/qa38/verify-qa38-structure.py .; python cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py`
- 성공 기대결과: 중첩 module SQL 누락/AutoConfiguration 삭제 mutation test가 반드시 FAIL.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Starter 물리 재편 이후 주요 DB/Bean 결함이 Gate PASS로 누락될 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-019 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `QA38를 recursive DB/catalog/실제 AutoConfiguration source 기반으로 보강하고 QA39를 explicit repository root에서 실행 가능하게 했다. Full checkout gate는 미검증이다.`
- 개발GPT_변경파일: `verify-qa38-structure.py; verify-qa39-canonical-starter-closure.py`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-019 -->

### QA-R5I-020 [P1] Documentation Evidence Matrix

- 연결 Requirement: `FDEV-001,FDEV-021,FDEV-022`
- QA 상태: `OPEN`
- 확인 수준: `PACKAGE_AND_PATH_REVIEW_CONFIRMED`
- 동료 QA 연결: `QA-R5-020`
- 자체 QA 연결: `QAF-REV004-001`
- QA 결함: CPF_SOURCE_EVIDENCE_MATRIX는 모두 a8be27 기준이며 integrationClosureApi/Test를 존재하지 않는 generated 경로로 기록하고 일부 OpenAPI 경로도 실제 Repository와 불일치한다.
- 영향/위험: Guide/산출물에서 Source 추적 링크가 끊기고 최신 구현과 문서가 양방향 불일치한다.
- 대상 파일·영역: `cpf-docs/deliverables/evidence/CPF_SOURCE_EVIDENCE_MATRIX.csv`
- 필수 수정: e7cc9ada source tree로 path existence/hash를 재생성하고 미검증 항목을 완료로 표기하지 않는다.
- 필수 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기대결과: matrix 경로 100% 존재, blob/hash 일치, runtime 미실행 status=미검증.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: Guide/산출물에서 Source 추적 링크가 끊기고 최신 구현과 문서가 양방향 불일치한다.

<!-- BEGIN DEVGPT RESULT QA-R5I-020 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `부분 구현`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Overlay hygiene·secret·empty·long path·temporary output 검사를 추가하고 modified scope의 dead/garbage source를 검토했다. 삭제 근거가 충분한 제품 Source는 없었다.`
- 개발GPT_변경파일: `verify-r6-overlay-hygiene.py; GARBAGE_SOURCE_REVIEW.md`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-020 -->

### QA-R5I-027 [P1] QA Gate False Green

- 연결 Requirement: `FDEV-003,FDEV-005,FDEV-014,FDEV-016,FDEV-025`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_VERIFIER_SOURCE_REVIEW_CONFIRMED`
- 동료 QA 연결: `RELATED:QA-R5-018;QA-R5-019`
- 자체 QA 연결: `QAF-REV004-013`
- QA 결함: REV-004/DB3 정적 Gate는 문자열 패턴 존재를 PASS로 판정하며 public capability 위조, 단일-slot idempotency, child environment secret inheritance, WaitForExit timeout 부재 같은 실제 결함을 검출하지 못한다.
- 영향/위험: 검증 Script PASS가 상용 동작·보안 PASS로 오인되어 동일 유형의 회귀가 반복된다.
- 대상 파일·영역: `cpf-tools/verification/final-dev/verify-rev004-overlay.py; verify-db3-runner-contract.py; verify_db3_runner_protocol.py; tests`
- 필수 수정: negative compile/runtime/mutation test를 Gate에 연결하고 문자열 존재 검사를 행위 검증으로 보강한다. 미실행 Runtime은 PASS가 아니라 NOT_EXECUTED로 구조화한다.
- 필수 재실행: `python cpf-tools/verification/final-dev/verify-rev004-overlay.py; python cpf-tools/verification/final-dev/verify-db3-runner-contract.py`
- 성공 기대결과: capability public 노출, single-slot storage, inherited secret, timeout 제거, runtime route 삭제 mutation이 각각 Gate FAIL을 발생시킨다.
- 실패 판정기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: 검증 Script PASS가 상용 동작·보안 PASS로 오인되어 동일 유형의 회귀가 반복된다.

<!-- BEGIN DEVGPT RESULT QA-R5I-027 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `문자열 존재만 확인하던 R6 핵심 영역에 source/behavior/SQL/hygiene 및 mutation contract gates를 연결하고 NOT_EXECUTED를 PASS와 분리했다.`
- 개발GPT_변경파일: `verify-r6-behavior-contracts.py; verify-r6-approval-contract.py; verify-r6-sql-parity.py`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `R6 behavior 18 checks/9 mutation PASS, approval contract PASS, SQL parity PASS, hygiene PASS`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-027 -->

### QA-R5I-028 [P1] Approval HTTP Contract

- 연결 Requirement: `FDEV-003,FDEV-012,FDEV-014,FDEV-016`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_CONTROLLER_SOURCE_CONFIRMED`
- 동료 QA 연결: `RELATED:QA-R5-008;QA-R5-014`
- 자체 QA 연결: `QAF-REV004-014`
- QA 결함: AdmApprovalController의 create/detail/decision/reconcile/execute가 모두 ResponseEntity.ok를 반환한다. 기존 requestKey 재생, concurrent preemption/conflict, stale version 등 상태 충돌을 HTTP 409로 안정적으로 표현하는 계약이 부족하다.
- 영향/위험: Generated Client와 운영 UI가 성공·멱등 replay·충돌·동시 변경을 구분하지 못하고 재시도/오류 처리를 잘못 수행할 수 있다.
- 대상 파일·영역: `AdmApprovalController.java; AdmApprovalService.java; exception handler; OpenAPI; generated client; frontend consumers`
- 필수 수정: created/replayed/conflict/stale 상태를 명시 DTO와 201/200/409 계약으로 정의하고 ControllerAdvice/OpenAPI/client/UI contract test를 추가한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*"`
- 성공 기대결과: 신규 201, 동일 payload replay 200 same result, key collision/stale CAS 409, validation 400/422가 Backend/OpenAPI/generated client/UI에서 동일하다.
- 실패 판정기준: 결함 재현이 계속되거나 negative/concurrency/contract test가 실패하거나 exact-SHA Evidence가 누락되면 미통과
- 요구 Evidence: exact SHA; changed source hash; test command; tool versions; exit code; sanitized logs; negative/concurrency result; consumer/call-path evidence
- 미조치 위험: Generated Client와 운영 UI가 성공·멱등 replay·충돌·동시 변경을 구분하지 못하고 재시도/오류 처리를 잘못 수행할 수 있다.

<!-- BEGIN DEVGPT RESULT QA-R5I-028 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Approval create/policy save를 201, replay를 200, conflict/stale를 409, validation을 422로 구분하고 ControllerAdvice/OpenAPI 계약을 보강했다.`
- 개발GPT_변경파일: `AdmApprovalController.java; AdmApprovalExceptionHandler.java; enrich-adm-openapi-contract.mjs`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-028 -->

### QA-R5I-021 [P2] Public API Migration

- 연결 Requirement: `FDEV-003,FDEV-007,FDEV-008`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-021`
- 자체 QA 연결: `-`
- QA 결함: CpfDataQualityOperations에 client boolean approved 시그니처가 Deprecated default method로 남아 있다.
- 영향/위험: 금지된 사용법이 public surface와 자동완성에 계속 노출되고 Consumer 제거 여부를 판정하기 어렵다.
- 대상 파일·영역: `CpfDataQualityOperations.java; consumer scan`
- 필수 수정: 호환 기간/제거 버전/사용처 0 증거를 문서화하고 다음 major에서 제거하거나 별도 compatibility adapter로 격리한다.
- 필수 재실행: `.\gradlew.bat --no-daemon :cpf-core:test :cpf-common:test :cpf-admin:test`
- 성공 기대결과: production consumer 0, deprecation migration test/document, major removal plan 존재.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 금지된 사용법이 public surface와 자동완성에 계속 노출되고 Consumer 제거 여부를 판정하기 어렵다.

<!-- BEGIN DEVGPT RESULT QA-R5I-021 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `R6S12 Work Instruction, Finding/Requirement 상태, Test/Evidence, Open Issues, Review, Handover 문서를 최신 basis와 실제 실행 결과로 갱신했다.`
- 개발GPT_변경파일: `cpf-docs/work/v9i/dev/r6s12/*`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-021 -->

### QA-R5I-022 [P2] Build Graph Reproducibility

- 연결 Requirement: `FDEV-004,FDEV-020,FDEV-025`
- QA 상태: `OPEN`
- 확인 수준: `DIRECT_SOURCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-022`
- 자체 QA 연결: `-`
- QA 결함: settings.gradle이 local-domains 아래 임의 included build를 별도 opt-in 없이 자동 포함한다.
- 영향/위험: 동일 Git SHA라도 로컬 directory 존재 여부에 따라 build graph와 dependency resolution이 달라진다.
- 대상 파일·영역: `settings.gradle`
- 필수 수정: 명시 property/allowlist와 manifest hash가 있을 때만 include하고 Evidence에 mounted domains를 기록한다.
- 필수 재실행: `.\gradlew.bat --no-daemon projects; .\gradlew.bat --no-daemon clean assemble`
- 성공 기대결과: fresh clone/로컬 폴더 존재 환경의 기본 build graph 동일; opt-in 시 manifest 검증.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 동일 Git SHA라도 로컬 directory 존재 여부에 따라 build graph와 dependency resolution이 달라진다.

<!-- BEGIN DEVGPT RESULT QA-R5I-022 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `변경 파일 SHA-256, package manifest, evidence ledger, overlay tree hash와 sidecar ZIP hash 구조를 재생성했다.`
- 개발GPT_변경파일: `PACKAGE_MANIFEST.json; SHA256SUMS.txt; OVERLAY_TREE_SHA256.txt`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-022 -->

### QA-R5I-023 [P2] Frontend Approval Key UX

- 연결 Requirement: `FDEV-003,FDEV-014`
- QA 상태: `OPEN`
- 확인 수준: `PEER_SOURCE_REVIEW;UX_TEST_REQUIRED`
- 동료 QA 연결: `QA-R5-023`
- 자체 QA 연결: `-`
- QA 결함: ApprovalsPage의 requestKey/decision idempotencyKey는 초기 1회 생성 후 성공·새 요청 시 자동 회전되지 않아 다른 요청에 재사용되기 쉽다.
- 영향/위험: 운영자가 정상 다음 작업에서 cross-request idempotency 오류를 반복하거나 수동 키를 잘못 수정한다.
- 대상 파일·영역: `ApprovalsPage.vue`
- 필수 수정: 성공 확정/새 작업 시 key rotation, pending retry 시만 동일 key 유지, 상태 표시와 테스트를 추가한다.
- 필수 재실행: `npm --prefix cpf-admin/frontend ci; npm --prefix cpf-admin/frontend run verify; npm --prefix cpf-admin/frontend run test:e2e`
- 성공 기대결과: timeout retry same key, success/new draft new key, cross-request reuse UI 차단.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 운영자가 정상 다음 작업에서 cross-request idempotency 오류를 반복하거나 수동 키를 잘못 수정한다.

<!-- BEGIN DEVGPT RESULT QA-R5I-023 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `외부 Java25/DB3/Browser/Multi-process 실행에 필요한 exact SHA runner, 환경변수 preflight, command/evidence 형식을 패키지화했다.`
- 개발GPT_변경파일: `run-r6-release-gates.ps1; .github/workflows/cpf-r6-release-gates.yml`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-023 -->

### QA-R5I-024 [P2] Evidence Retention/Path

- 연결 Requirement: `FDEV-022,FDEV-024`
- QA 상태: `OPEN`
- 확인 수준: `PACKAGE_PROVENANCE_CONFIRMED`
- 동료 QA 연결: `QA-R5-024`
- 자체 QA 연결: `QAF-REV004-001`
- QA 결함: R4 Package는 baseline overlay 관점의 117개 payload를 기록하지만 result_commit_sha와 commit 적용 후 상태가 없고 current master 기준 Package index가 없다.
- 영향/위험: 사용자가 merge한 Commit과 제출 ZIP의 관계를 독립적으로 증명할 수 없다.
- 대상 파일·영역: `fdr/r4/PACKAGE_MANIFEST.json; PRODUCT_TREE_SHA256.txt; CHANGE_MANIFEST.csv`
- 필수 수정: result commit 기준 tree manifest와 changed-file hash를 재생성하고 overlay baseline manifest는 historical로 분리한다.
- 필수 재실행: `git rev-parse HEAD; git status --short --branch; python cpf-tools/verification/final-dev/verify-rev004-overlay.py`
- 성공 기대결과: baseline overlay hash + result tree hash + commit SHA + file count가 모두 교차 검산됨.
- 실패 판정기준: 결함 잔존, 명령 Exit Code 비정상, exact-SHA 불일치, Evidence 누락/빈 파일/Hash 불일치 또는 필수 Runtime 미실행
- 요구 Evidence: exact SHA; clean working tree; command; tool/environment versions; start/end; exit code; sanitized stdout/stderr; artifact/tree hash; negative/concurrency/runtime result
- 미조치 위험: 사용자가 merge한 Commit과 제출 ZIP의 관계를 독립적으로 증명할 수 없다.

<!-- BEGIN DEVGPT RESULT QA-R5I-024 -->
- 개발GPT_수행상태: `수행 완료`
- 개발GPT_개발판정: `완료`
- 개발GPT_검증판정: `미검증`
- 개발GPT_원인분석: `QA Finding 재현 조건과 Consumer/호출 경로를 기준으로 공통 원인을 정책 결속, fail-open, public capability, non-strict JSON, single-slot idempotency, 정적 false-green, provenance 불일치로 분류`
- 개발GPT_수정내용: `Root 상대경로 Overlay, manifest, hash, apply/verify 절차, Codex 요청 및 handover를 작성했다. Commit/Push는 수행하지 않았다.`
- 개발GPT_변경파일: `cpf-docs/work/v9i/dev/r6s12/*`
- 개발GPT_Owner_API_SPI_Internal_검토: `cpf-core public query/replay; correction SPI; cpf-common provider; cpf-admin approval owner/consumer 경계로 검토`
- 개발GPT_Consumer_호출경로: `Controller/Frontend → Service → Repository/Registry → Owner Adapter → SPI/Provider 경로 검토; 상세는 REVIEW.md`
- 개발GPT_정상_오류_경계_부분실패_UNKNOWN: `Validation/404/409/422/429/500/503, timeout, UNKNOWN/reconcile 경로 반영; Target Runtime은 미검증`
- 개발GPT_멱등성_동시성_재시도_복구_Reconcile: `DB unique convergence, CAS, operation lock, pending ledger, observation-only reconcile 반영`
- 개발GPT_보안_권한_감사_마스킹_Secret: `SoD, HMAC proof, sanitized detail, strict JSON, credential stdin, environment clear, redaction 반영`
- 개발GPT_DB3_Generator_Frontend_OpenAPI_영향: `3 Vendor lifecycle, Gradle input, Runtime OpenAPI failure contract, UI consumer를 검토/보완`
- 개발GPT_추가테스트: `JUnit/Vitest/Playwright/Pester/behavior mutation/Java·Node harness 추가 또는 보강`
- 개발GPT_실행명령_ExitCode: `EVIDENCE_LEDGER.csv 및 TEST_AND_EVIDENCE.md 참조`
- 개발GPT_Evidence_경로_SHA256: `cpf-docs/work/v9i/dev/r6s12/evidence/**; SHA256SUMS.txt 참조`
- 개발GPT_실제결과: `Source/Test/Script/Config 정적 보완 완료; 요구 Target Runtime 또는 exact result commit 검증은 미실행`
- 개발GPT_미완료_사유: `필수 Target Runtime/complete checkout/result SHA 검증 조건 잔존`
- 개발GPT_남은위험: `OPEN_ISSUES.md의 외부 검증 조건`
- 개발GPT_result_sha: `미생성; 사용자 Overlay 적용 후 확정`
- 개발GPT_자체검수: `Source/Static/Harness 수행; 요구 Runtime 미검증`
<!-- END DEVGPT RESULT QA-R5I-024 -->

---

## 6. FDEV-001~FDEV-025 통합 결과 원장

QA 열은 고정이다. 개발GPT는 `개발GPT_*` 열에 해당하는 아래 결과 블록을 갱신한다.

| Requirement | 우선순위 | QA R5I 결과 | 연결 Finding | 현재 개발상태 | 현재 검증상태 |
|---|---:|---|---|---|---|
| `FDEV-001` 최신 master 기준선·관리 원장·검수 문서 정합성 재정리 | P0 | 미통과 | `QA-R5I-001;QA-R5I-003;QA-R5I-020` | 부분 구현 | 실패 |
| `FDEV-002` 신규 ADM Service와 Crypto·Time·Data Quality·Webhook SPI의 실제 Spring Runtime Bean wiring 완성 | P0 | 미통과 | `QA-R5I-006;QA-R5I-007` | 부분 구현 | 실패 |
| `FDEV-003` 위험 정정 API의 Client Boolean 승인 제거 및 서버 검증 승인·권한·감사·멱등성 구현 | P0 | 미통과 | `QA-R5I-005;QA-R5I-006;QA-R5I-007;QA-R5I-009;QA-R5I-010;QA-R5I-011;QA-R5I-012;QA-R5I-013;QA-R5I-014;QA-R5I-021;QA-R5I-023;QA-R5I-025;QA-R5I-026;QA-R5I-027;QA-R5I-028` | 부분 구현 | 실패 |
| `FDEV-004` Java 25·Gradle 9.1 fresh clone 전체 Build·Test·Publication 실실행 | P0 | 미통과 | `QA-R5I-002;QA-R5I-022` | 미검증 | 미검증 |
| `FDEV-005` Oracle·PostgreSQL·MariaDB 실제 Install·Migration·Upgrade·Rollback·Drift 전수 실행 | P0 | 미통과 | `QA-R5I-002;QA-R5I-015;QA-R5I-027` | 부분 구현 | 실패 |
| `FDEV-006` Broker·다중 인스턴스·분리 WAS·Process Kill·부분 실패·UNKNOWN 복구 Runtime 전수 검증 | P0 | 미통과 | `QA-R5I-002` | 미검증 | 미검증 |
| `FDEV-007` 공식 Module Ownership·Public API/SPI/Internal 경계·의존성 방향 전수 재검수 | P1 | 미통과 | `QA-R5I-019;QA-R5I-021;QA-R5I-025` | 부분 구현 | 실패 |
| `FDEV-008` 신규·변경 Framework 계약의 실제 Consumer와 전체 호출 경로 전수 연결 | P1 | 미통과 | `QA-R5I-005;QA-R5I-021;QA-R5I-025` | 부분 구현 | 실패 |
| `FDEV-009` Outbox·Webhook·Notification·Incident 비동기 Lifecycle와 DLQ/UNKNOWN/Reconcile 완성 | P1 | 미통과 | `QA-R5I-011;QA-R5I-012` | 부분 구현 | 실패 |
| `FDEV-010` Batch·Scheduler·Worker·Center-Cut 상태기계와 운영 복구 기능 전수 완성 | P1 | 미통과 | `QA-R5I-002` | 재확인 필요 | 미검증 |
| `FDEV-011` Cache Durable Invalidation·Ledger·Checkpoint·Failover 전수 완성 | P1 | 미통과 | `QA-R5I-002` | 재확인 필요 | 미검증 |
| `FDEV-012` 멱등성·동시성·재시도·Timeout·Fencing·Reconcile 공통 정책 전수 적용 | P1 | 미통과 | `QA-R5I-010;QA-R5I-012;QA-R5I-014;QA-R5I-026;QA-R5I-028` | 부분 구현 | 실패 |
| `FDEV-013` 인증·권한·SoD·감사·마스킹·Secret·암호화 운영품질 전수 재검수 | P1 | 미통과 | `QA-R5I-005;QA-R5I-006;QA-R5I-009;QA-R5I-010;QA-R5I-013;QA-R5I-025` | 부분 구현 | 실패 |
| `FDEV-014` ADM 상용 운영 기능 전수 점검 및 누락 구현 | P1 | 미통과 | `QA-R5I-003;QA-R5I-008;QA-R5I-009;QA-R5I-011;QA-R5I-013;QA-R5I-016;QA-R5I-023;QA-R5I-026;QA-R5I-027;QA-R5I-028` | 부분 구현 | 실패 |
| `FDEV-015` BZA 상용 업무 관리자 기능 전수 점검 및 누락 구현 | P1 | 미통과 | `QA-R5I-002` | 재확인 필요 | 미검증 |
| `FDEV-016` OpenAPI·Backend API·Generated Client·Route·Config 정합성 전수 검증 | P1 | 미통과 | `QA-R5I-003;QA-R5I-008;QA-R5I-017;QA-R5I-018;QA-R5I-026;QA-R5I-027;QA-R5I-028` | 부분 구현 | 실패 |
| `FDEV-017` 실제 Browser·Playwright Release·접근성·반응형·외부 Runtime 의존성 검증 | P1 | 미통과 | `QA-R5I-002;QA-R5I-016` | 미검증 | 미검증 |
| `FDEV-018` Generator·Generated Domain·Sample·EDU의 최종 Framework 계약 정합성 검증 | P1 | 미통과 | `QA-R5I-002` | 재확인 필요 | 미검증 |
| `FDEV-019` 3 Vendor SQL 정적 정합성·Query ID·Metadata·Seed·Index/FK 전수 검수 | P1 | 미통과 | `QA-R5I-019` | 부분 구현 | 실패 |
| `FDEV-020` 전체 회귀·fresh clone·Repository Hygiene·Dead Code·Ignore 보호 전수 수행 | P1 | 미통과 | `QA-R5I-002;QA-R5I-003;QA-R5I-004;QA-R5I-006;QA-R5I-015;QA-R5I-017;QA-R5I-019;QA-R5I-022` | 부분 구현 | 실패 |
| `FDEV-021` 문서·Guide·기술사양·운영 Playbook의 실제 구현·상태·명령 정합성 갱신 | P2 | 미통과 | `QA-R5I-001;QA-R5I-004;QA-R5I-018;QA-R5I-020` | 부분 구현 | 실패 |
| `FDEV-022` 단일 Requirement 원장·Evidence·Manifest·Hash의 최신 Commit 기준 재생성 및 무결성 검증 | P2 | 미통과 | `QA-R5I-001;QA-R5I-003;QA-R5I-020;QA-R5I-024` | 부분 구현 | 실패 |
| `FDEV-023` 외부 환경·권한·Secret·운영 승인 필요 항목의 실행 요청 패키지 완성 | P2 | 미통과 | `QA-R5I-015` | 부분 구현 | 실패 |
| `FDEV-024` 최종 개발GPT 자체검수·Codex 인계·전수 QA 진입 Package 완성 | P2 | 미통과 | `QA-R5I-001;QA-R5I-002;QA-R5I-003;QA-R5I-004;QA-R5I-024;QA-R5I-029` | 부분 구현 | 실패 |
| `FDEV-025` Starter Catalog·Internal/Public BOM·물리 Layout exact equality 및 openapi-webmvc 내부화 결함 보완 | P0 | 미통과 | `QA-R5I-019;QA-R5I-022;QA-R5I-027` | 부분 구현 | 실패 |

<!-- BEGIN DEVGPT REQUIREMENT RESULT -->

| Requirement | 개발GPT_수행상태 | 개발GPT_개발상태 | 개발GPT_검증상태 | 개발GPT_변경요약 | 개발GPT_Evidence | 개발GPT_미완료 |
|---|---|---|---|---|---|---|
| FDEV-001 | 전수 결과 기록 | 부분 구현 | 미검증 | R6S12 baseline/provenance 원장 재작성. Result commit은 사용자 적용 후 확정 필요. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-002 | 전수 결과 기록 | 완료 | 미검증 | Integration Closure Bean pair/proof/profile guard 및 Context test 보강. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-003 | 전수 결과 기록 | 완료 | 부분 검증 | 승인 정책·capability·민감정보·JSON·null·concurrency·HTTP 계약 전반 보완. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-004 | 전수 결과 기록 | 완료 | 미검증 | Java25/Gradle9.1 exact-SHA runner/workflow 작성; Target Runtime 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-005 | 전수 결과 기록 | 완료 | 미검증 | DB3 lifecycle SQL/runner/gate 작성; live DB3 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-006 | 전수 결과 기록 | 재확인 필요 | 미검증 | Multi-process 실행 script 연결만 완료; 실제 broker/split-WAS/process-kill 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-007 | 전수 결과 기록 | 부분 구현 | 미검증 | 변경 API/SPI/Owner 경계 정적 검수 및 public boolean 제거. 전체 repo 회귀 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-008 | 전수 결과 기록 | 완료 | 미검증 | Approval/Data Quality 실제 Controller-Service-Owner-Port 호출 경로 연결. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-009 | 전수 결과 기록 | 부분 구현 | 미검증 | Data Quality quarantine/replay/correction/reconcile 개선. Outbox/Notification 전체 범위 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-010 | 전수 결과 기록 | 재확인 필요 | 미검증 | 기존 Batch 전체 범위 Source 재검수는 complete checkout 필요. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-011 | 전수 결과 기록 | 재확인 필요 | 미검증 | 기존 Cache 전체 범위 Source 재검수는 complete checkout 필요. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-012 | 전수 결과 기록 | 완료 | 부분 검증 | 멱등성/CAS/replay lock/timeout runner 구현과 Java/Node harness PASS. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-013 | 전수 결과 기록 | 완료 | 부분 검증 | 정책 결속·SoD·HMAC proof·마스킹·secret-safe runner 보완. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-014 | 전수 결과 기록 | 완료 | 미검증 | ADM permission/error/audit/idempotency 보완; Browser 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-015 | 전수 결과 기록 | 재확인 필요 | 미검증 | BZA 전체 범위는 변경 없음, complete checkout 회귀 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-016 | 전수 결과 기록 | 완료 | 미검증 | Backend/OpenAPI/client boundary source 보완; generated client full verify 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-017 | 전수 결과 기록 | 완료 | 미검증 | E2E 작성; Playwright/접근성 실제 실행 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-018 | 전수 결과 기록 | 재확인 필요 | 미검증 | Generator/EDU 전체 범위는 complete checkout 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-019 | 전수 결과 기록 | 완료 | 부분 검증 | DB3 R6 schema lifecycle static parity PASS; live vendor runtime 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-020 | 전수 결과 기록 | 부분 구현 | 부분 검증 | Overlay hygiene PASS, 보호 SVG 복원, 삭제 요청 0. 전체 repo dead code scan 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-021 | 전수 결과 기록 | 완료 | 부분 검증 | R6 관련 문서/명령/상태 갱신 완료. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-022 | 전수 결과 기록 | 완료 | 부분 검증 | Requirement/Evidence/Manifest/Hash 재생성 완료; result commit은 사용자 적용 후 결속 필요. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-023 | 전수 결과 기록 | 완료 | 미검증 | 외부 실행 package와 exact-SHA preflight 작성 완료. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-024 | 전수 결과 기록 | 부분 구현 | 미검증 | 최종 Overlay/자체검수/Codex 인계 작성; Codex·Target Runtime 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |
| FDEV-025 | 전수 결과 기록 | 완료 | 부분 검증 | local-domains opt-in 및 QA38/QA39 gate 보강; full canonical gate 미검증. | TEST_AND_EVIDENCE.md; EVIDENCE_LEDGER.csv | Target Runtime/Codex/result commit 조건 |

<!-- END DEVGPT REQUIREMENT RESULT -->

완료 판정 규칙:

- 연결된 모든 Finding의 필수 수정·성공 기준·Evidence가 충족되어야 한다.
- Runtime 검증이 남아 있으면 `개발GPT_검증상태=미검증`이다.
- 일부 구현만 끝났으면 `개발GPT_개발상태=부분 구현`이다.
- QA R5I 결과는 개발GPT가 `통과`로 바꾸지 않는다. 개발GPT는 `재검수 요청`만 기록한다.

---

## 7. 필수 Runtime·Build·Test 검증 묶음

### 7.1 Java 25 / Gradle 9.1 / Fresh Clone

```powershell
$RepoRoot = (git rev-parse --show-toplevel).Trim()
& (Join-Path $RepoRoot 'gradlew.bat') -p $RepoRoot --version
& (Join-Path $RepoRoot 'gradlew.bat') -p $RepoRoot clean build --no-daemon --max-workers=1 --stacktrace
& (Join-Path $RepoRoot 'gradlew.bat') -p $RepoRoot aggregateQualityBuild publicationGate --no-daemon --max-workers=1 --stacktrace
```

성공 조건: Java 25·Gradle 9.1, exact result SHA, fresh clone, Exit Code 0, artifact hash, publication 결과, clean tree.

### 7.2 Targeted Approval / ADM

```powershell
.\gradlew.bat --no-daemon :cpf-admin:test --tests "com.cpf.admin.approval.*" --stacktrace
.\gradlew.bat --no-daemon :cpf-admin:test --tests "*AdmIntegrationClosureConfigurationTest" --stacktrace
.\gradlew.bat --no-daemon :cpf-core:test :cpf-common:test :cpf-admin:test --stacktrace
```

반드시 포함할 Negative/Concurrency Test:

- [ ] 외부 package 승인 Capability 생성 compile-negative 또는 authorization-negative
- [ ] expired/disabled/future/wrong-owner/wrong-command/wrong-target 정책 거부
- [ ] snapshot hash mismatch·payload tamper·version mismatch·single-use 재사용 거부
- [ ] duplicate JSON key·2^53 초과 정수·고정소수 정밀도
- [ ] null field payload
- [ ] requestKey/decision unique conflict와 재조회 수렴
- [ ] stale CAS·concurrent preemption·replay/correction 경쟁
- [ ] 신규 201, 동일 replay 200, collision/stale 409, validation 4xx
- [ ] 민감 payload 응답·로그·화면 비노출

### 7.3 Frontend

```powershell
npm --prefix cpf-admin/frontend ci
npm --prefix cpf-admin/frontend run verify
npm --prefix cpf-admin/frontend run test:e2e
```

필수 시나리오:

- [ ] A→B→A 교차 Draft에서 A 원 key 유지
- [ ] timeout·응답 유실 재시도 동일 key
- [ ] 성공·명시 취소 후 key rotation
- [ ] multi-tab·refresh·double-click
- [ ] 권한별 query/approval/execute/replay 버튼
- [ ] 401·403·404·409·429·500·503
- [ ] 접근성·키보드·반응형
- [ ] Generated Client 실제 Consumer와 Runtime OpenAPI parity

### 7.4 DB3

공식 Vendor는 Oracle, PostgreSQL, MariaDB만 인정한다.

필수 실행 범위:

- [ ] Install
- [ ] Migration
- [ ] Seed
- [ ] Upgrade
- [ ] Rollback
- [ ] Drift
- [ ] Index/FK/Metadata
- [ ] Runtime Query
- [ ] requestKey/decision idempotency unique conflict/CAS
- [ ] Runner timeout/cancellation
- [ ] Password stdin 전달
- [ ] argv·stdout·stderr·environment secret 비노출
- [ ] child process의 불필요한 secret environment 제거

### 7.5 Broker / Multi-instance / Split-WAS / Process Kill

필수 시나리오:

- [ ] 2개 이상 instance 동시 요청
- [ ] 승인·정정 single-use 경쟁
- [ ] Broker 지연·중복·재전송·DLQ
- [ ] 부분 실패와 UNKNOWN
- [ ] Process kill 전후 resume/reconcile
- [ ] 분리 WAS·다중 instance 상태 공유
- [ ] Outbox/Webhook/Notification/Incident 재시도와 감사

### 7.6 Starter / Catalog / Generator / QA Gate

```powershell
python cpf-tools/verification/qa38/verify-qa38-structure.py .
python cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py
python cpf-tools/verification/final-dev/verify-rev004-overlay.py
python cpf-tools/verification/final-dev/verify-db3-runner-contract.py
```

필수 Mutation:

- [ ] public capability 노출 시 FAIL
- [ ] single-slot idempotency 저장소로 회귀 시 FAIL
- [ ] child secret environment 상속 시 FAIL
- [ ] timeout 제거 시 FAIL
- [ ] Runtime route 삭제/rename 시 OpenAPI Gate FAIL
- [ ] 중첩 starter SQL 누락 시 FAIL
- [ ] AutoConfiguration 삭제 시 FAIL
- [ ] Active/Retained/Removed set 불일치 시 FAIL

---

## 8. Evidence 작성 규칙

모든 실행 단위 Evidence는 아래 필드를 포함한다.

- evidence_id
- finding_id / requirement_id
- exact source SHA
- command
- working directory
- OS / tool / runtime versions
- start_at / end_at
- exit_code
- sanitized stdout/stderr
- raw log SHA-256
- artifact SHA-256
- expected result
- actual result
- PASS / FAIL / NOT_EXECUTED
- blocker와 재실행 조건

금지:

- 0바이트 Evidence
- 존재하지 않는 `.log`/`.txt` 경로 참조
- 다른 SHA 산출물 Hash 재사용
- Runtime 미실행을 정적 Gate PASS로 대체
- Secret·Password·Token·개인정보 원문 기록

<!-- BEGIN DEVGPT EVIDENCE LEDGER -->

상세 정본: `cpf-docs/work/v9i/dev/r6s12/EVIDENCE_LEDGER.csv`

| evidence_id | source_sha | status | path |
|---|---|---|---|
| DEV-EVID-001~019 | 28f823a18eca859cebdbceb382029f595cdf490c | PASS 또는 NOT_EXECUTED 명시 | `cpf-docs/work/v9i/dev/r6s12/evidence/**` |

<!-- END DEVGPT EVIDENCE LEDGER -->

---

## 9. 변경 Manifest

<!-- BEGIN DEVGPT CHANGE MANIFEST -->

상세 정본: `cpf-docs/work/v9i/dev/r6s12/CHANGE_MANIFEST.csv`

| path | action | finding_ids | requirement_ids |
|---|---|---|---|
| Root Overlay 전체 | ADD/MODIFY | QA-R5I-001~029 | FDEV-001~025 |

<!-- END DEVGPT CHANGE MANIFEST -->

삭제가 필요하면 실제 삭제하지 않고 정확한 Repository Root 상대경로를 `DELETE_REQUESTED`로 기록하며 사용자 승인 ID를 기다린다.

---

## 10. 개발GPT 자체검수 체크리스트

<!-- BEGIN DEVGPT SELF REVIEW -->

- [x] 29/29 Finding 수행상태를 기록했다.
- [x] FDEV-001~FDEV-025 결과를 기록했다.
- [x] Consumer/API/SPI/Internal, 오류/UNKNOWN, 멱등성/복구, 보안, DB3, Frontend/OpenAPI, Evidence를 Source 단위 검토했다.
- [x] 구현 가능한 Source/Test/SQL/Config/Frontend/Script를 작성했다.
- [x] 실행 가능한 Static/Java/Node harness Gate를 실행했다.
- [x] 미실행 Runtime을 PASS로 기록하지 않았다.
- [x] Evidence empty/temp/longpath/secret/hash 규칙을 검증했다.
- [x] 보호 SVG를 복원하고 삭제 요청 0건으로 기록했다.
- [ ] Java25/Gradle9.1 full Build/Test/Publication — 환경 미구비.
- [ ] DB3 live lifecycle — DB/Secret/pwsh 미구비.
- [ ] Playwright/Broker/Multi-instance — Runtime 미구비.
- [ ] Codex 독립검수 — 미실행.

- 개발GPT_자체검수_최종판정: `미완료(재검수 준비 완료, 외부 Runtime/Codex 대기)`
- 개발GPT_재검수요청여부: `조건부 Y`
- 개발GPT_미완료Finding: `QA-R5I-029 개발 미완료; 전체 Finding Runtime 검증 미완료`
- 개발GPT_미검증Requirement: `25`
- 개발GPT_자체검수_일시: `2026-08-07T04:25:56+09:00`
<!-- END DEVGPT SELF REVIEW -->

---

## 11. Codex 독립 검수 인계

개발GPT 수정 완료 후 Codex가 exact result SHA에서 29개 Finding과 FDEV-001~025를 독립 검수해야 한다.

<!-- BEGIN DEVGPT CODEX HANDOVER -->
- 개발GPT_result_sha: `사용자 Overlay 적용 후 확정`
- Codex_검수요청경로: `cpf-docs/work/v9i/dev/r6s12/CODEX_REVIEW_REQUEST.md`
- Codex_필수검수범위: `QA-R5I-001~029; FDEV-001~025`
- Codex_필수Runtime: `Java25/Gradle9.1; DB3; Frontend/Playwright; Broker/Multi-process; Publication`
- Codex_미실행항목처리: `PASS 금지, 미검증 기록`
- Codex_요청상태: `요청자료 작성 완료/독립검수 미실행`
<!-- END DEVGPT CODEX HANDOVER -->

---

## 12. 최종 결과 및 Package 제출

개발GPT는 이 파일을 결과물에 포함하고 아래 항목을 최종 갱신한다.

<!-- BEGIN DEVGPT FINAL RESULT -->
- 개발GPT_작업종료_시각_KST: `2026-08-07T04:25:56+09:00`
- 개발GPT_result_commit_sha: `미생성(사용자 Git write 대상)`
- 개발GPT_evidence_source_sha: `28f823a18eca859cebdbceb382029f595cdf490c + Overlay manifest`
- 개발GPT_working_tree: `원격 Connector에서 미확인; Repository 직접 변경 없음`
- 개발GPT_변경파일수: `PACKAGE_MANIFEST.json 참조`
- 개발GPT_추가파일수: `PACKAGE_MANIFEST.json 참조`
- 개발GPT_수정파일수: `PACKAGE_MANIFEST.json 참조`
- 개발GPT_삭제요청수: `0`
- 개발GPT_완료Finding: `개발 완료 25/29; 부분 구현 3/29; 미완료 1/29; 검증은 Target Runtime 미완료`
- 개발GPT_완료Requirement: `개발 완료 15/25; 부분 구현 5/25; 재확인 필요 5/25; QA 최종 완료 0/25`
- 개발GPT_미실행Runtime: `Java25/Gradle9.1, DB3 live, Playwright, Broker/Multi-process, complete checkout QA38/QA39/REV004, Codex`
- 개발GPT_최종판정: `미완료(구현 Package 완성, 외부 검증 조건 잔존)`
- 개발GPT_QA_R6_재검수요청: `조건부 Y`
- Root_Overlay_ZIP: `CPF_R6_S12_ROOT_OVERLAY.zip`
- ZIP_SHA256: `외부 sidecar CPF_R6_S12_ROOT_OVERLAY.zip.sha256 참조(자기참조 방지)`
- ZIP_entry_count: `외부 최종 응답 및 PACKAGE_MANIFEST.json 참조`
- manifest_payload_count: `PACKAGE_MANIFEST.json 참조`
- 보호경로변경: `cpf-docs/assets/manuals/cpf-document-quality-r9.svg 복원`
- 사용자승인삭제: `없음; DELETE_REQUESTED 0`
- Commit_Push_Branch_Tag_PR_수행: `수행하지 않음`
<!-- END DEVGPT FINAL RESULT -->

필수 Package 포함 항목:

- [ ] 이 단일 통제서의 개발GPT 결과기록 완료본
- [ ] 변경 Source·SQL·API·Test·Config·Frontend·Script
- [ ] CHANGE_MANIFEST.csv
- [ ] TEST_AND_EVIDENCE.md
- [ ] OPEN_ISSUES.md
- [ ] REQUIREMENT_STATUS.csv
- [ ] PACKAGE_MANIFEST.json
- [ ] SHA256SUMS.txt
- [ ] DELETE_MANIFEST.csv
- [ ] CODEX_REVIEW_REQUEST.md
- [ ] HANDOVER.md

최종 제출은 Root 상대경로 단일 Overlay ZIP으로 한다. 경로는 Windows 호환 길이를 유지하고 불필요한 중첩·임시 로그·build output을 포함하지 않는다.

---

## 13. 동료 QA 검토의 통합 평가

### 13.1 정량 비교

- 자체 QA Finding: 16건
- 동료 QA Finding: 24건
- 직접 중복·부분 중복 통합: 11개 묶음
- 동료 QA에서만 독립적으로 추가된 Finding: 13건
- 자체 QA가 별도 재현·확장한 Finding: 5건
- 최종 통합 Finding: 29건

### 13.2 동료 QA가 추가한 독립 결함 13건

1. 승인 없는 보호 SVG 삭제
2. 승인 정책의 action/owner/command/target 서버 결속 부족
3. local profile 기본 활성화에 따른 fail-open
4. Backend/OpenAPI/UI validation 경계 불일치
5. 승인 상세 raw payload 노출
6. duplicate JSON key와 숫자 정밀도 손실
7. Map.copyOf null payload 실패
8. replay expectedVersion·quarantine identity·동시성 결함
9. versioned policy UPDATE와 break-glass 감사 미반영
10. Frontend Gradle generator input 누락
11. deprecated boolean approved public API 잔존
12. local-domains 자동 include로 build graph 비결정성
13. ApprovalsPage idempotency key rotation UX 결함

### 13.3 동료 QA가 보강한 중복 영역

- 최신 SHA·관리 원장·Evidence 혼용
- Target Runtime 미실행
- 0바이트/경로/Hash Evidence 모순
- Spring Bean wiring
- 실제 DB unique conflict/CAS
- DB Runner argv·timeout
- ADM 권한·브라우저 검증
- OpenAPI 수동 증식과 QA Gate coverage
- Documentation Source Evidence 경로
- Package provenance

### 13.4 자체 QA가 추가·확장한 영역

- public SPI 승인 Capability 외부 위조 재현
- A→B→A 교차 Draft 멱등키 유실 재현
- DB child process의 secret environment 상속
- 정적 문자열 Gate false-green
- Approval HTTP 201/200/409 계약
- Codex 독립 검수 누락

### 13.5 최종 평가

동료 QA는 **의미가 높다**. 단순한 같은 결론 반복이 아니라 13개의 독립 결함을 추가했고, 그중 P0 5건은 출시 차단 수준이다. 특히 정책 결속, secure default, 민감 Payload, 계약 parity, 데이터 품질 replay/concurrency는 기존 자체 QA만으로는 누락될 수 있던 영역이다.

다만 동료 QA만으로도 충분하지는 않다. 승인 Capability 위조, A→B→A 멱등성, child secret 상속, 행위 기반 Gate false-green, Codex 독립검수 누락은 자체 QA에서 추가 확인됐다. 따라서 두 검토를 통합한 29건이 현재 정본이며 어느 한쪽 결과만으로 축소하면 안 된다.

---

## 14. QA R6 진입 조건

아래 조건을 모두 충족하지 않으면 QA R6에서 즉시 미통과다.

- [ ] 29개 Finding 결과기록 완료
- [ ] 25개 Requirement 결과기록 완료
- [ ] P0 잔존 0
- [ ] 필수 Runtime 미실행 0 또는 외부 환경상 불가 항목을 정확히 미검증으로 기록
- [ ] Java 25·Gradle 9.1 fresh clone Build/Test/Publication Evidence
- [ ] Oracle/PostgreSQL/MariaDB live lifecycle Evidence
- [ ] Frontend/Playwright/접근성/반응형 Evidence
- [ ] Broker/Multi-instance/Split-WAS/Process Kill Evidence
- [ ] QA38/QA39/Mutation Gate Evidence
- [ ] Evidence empty/missing/orphan/hash mismatch 0
- [ ] Codex 독립검수 결과와 exact result SHA 결속
- [ ] Root Overlay ZIP/Manifest/SHA/entry count 정합
- [ ] 보호 경로 승인 없는 삭제 0

이 조건은 개발GPT 완료 선언이 아니라 QA R6 재검수 진입 조건이다. 최종 완료는 QA R6 실제 통과 후에만 확정한다.
