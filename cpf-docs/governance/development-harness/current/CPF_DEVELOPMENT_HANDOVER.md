# CPF Development Handover — Current

## 1. Current Authority / Source Identity

- 단일 실행 정본: `cpf-docs/governance/development-harness/`
- 입력 Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260830_140354.zip`
- 입력 ZIP SHA-256: `c9f7bd68edc9891638280453bd82ab8000efc5b052a556cc67030d76744fb395`
- Current Product Source Identity: `d54e64e446d99e0601bf36205e492e3568bb56fd0d80bff5e54793249394f97f` / **8,450 files**
- Canonical Product Requirement: **218**
- Tracking Work Item: **394**
- Root Cause Execution WP: **16**
- Current Work Item Registry: **410**
- Role Ledger: **1,230**
- Current Test Ledger: **829**
- Migration Semantic Ledger: **265**

Current Registry와 세션 Evidence가 현재 상태의 출발점이다. 별도 TODO/완료원장을 정본으로 만들지 않는다.

## 2. Session Merge Control

- merge protocol: `1`
- baseline Source Identity: `d54e64e446d99e0601bf36205e492e3568bb56fd0d80bff5e54793249394f97f`
- last merged session: `20260830_devgpt_toolchain_release_finalization`
- pending sessions: `NONE`
- conflict sessions: `NONE`
- merged set digest: `a315fa6113998384e2c43ee237cba755df283d304432538a10c52241e30adcff`
- last reviewer: `HARNESS-20260830T083000Z-FINAL-MERGE-REVIEW2`

검증 결과: `SESSION_MERGE_PROTOCOL=PASS sessions=3 merged=3 pending=0 conflicts=0`.

## 3. 이번 DevGPT 개발·보완 Closure

### WP-H00 / WP-H02 (registry handover alias `WP-R03.15`)
- Current Source Identity와 mutable Harness current/evidence projection의 순환을 차단했다.
- Session Manifest/Merge/no-bulk/current prerequisite/Final Self Review를 Machine Validator 및 Negative Mutation으로 강제했다.
- Common Rule `CR-22 Capability-first Toolchain Compatibility`를 추가했다. Host tool의 exact patch/minor 고정을 금지하고 실제 기능 호환을 우선한다. Java는 CPF target Java 25는 유지하되 Host JDK exact 25 고정 대신 실제 `--release 25`/Gradle capability로 판정한다.

### WP-B02 (registry handover alias `WP-R01.21`)
- VS Code 6개 Error의 Root Cause인 source-empty profile(`browser-bff`, `batch-service`, `web-api`) class output 누락을 discovery-driven으로 보완했다.
- `cpfVerifyIdeClasspathReady`가 canonical class folder 존재를 fail-closed 확인한다.
- Current Source static/contract regression은 PASS. **Windows Fresh VS Code/Buildship actual Error=0 Warning=0은 Current Source에서 필수 재실행**이다.

### WP-CLI01 / WP-RL01 (registry handover alias `WP-R07.17`) / WP-RL02
- CLI의 JAVA_HOME/PATH resolution 불일치와 child CLI Source Identity override를 보완했다.
- Release/Open Git 14단계 콘솔을 한글 업무 단계명·진행상태·실패원인·다음조치 중심으로 정리하고 내부 Gradle task 원문은 상세 로그에 보존한다.
- Host tool exact patch/minor version 차이로 불필요하게 중단하지 않고 capability를 먼저 검증한다.

### WP-FE01
- ADM/Backoffice `npm ci`의 `Missing prettier@3.9.6 from lock file` Root Cause를 package/package-lock canonical graph 동기화로 수정했다.
- exact Node/npm patch gate를 제거하고 실제 `npm ci/lint/typecheck/test/build` capability로 판정한다.

### WP-BAT01
- Two-worker runtime의 root/app DB credential ownership을 분리했다.
- Gateway/BAT 실패 결과에서 optional stdout/stderr 접근이 원 Root Cause를 덮는 StrictMode 연쇄 오류를 제거했다.

### Docker Development Test Entry Point
- 누락됐던 `CPF_도커_개발테스트환경_전체설치.ps1`, `CPF_도커_확장연동환경_증분설치.ps1`을 실제 lifecycle로 복구했다.
- Workspace runtime 강제 동기화, Secret/Volume 보존, 실행 중 container fail-closed, Docker/Compose capability, image/tooling 준비, Created/Stopped 검증을 포함한다.

## 4. Current Source 실제 검증 근거

- Product Source Identity: `d54e64e446d99e0601bf36205e492e3568bb56fd0d80bff5e54793249394f97f` / 8,450 files
- Affected regression: **62 PASS / 1 SKIP / 0 FAIL**
- Generator affected regression: **24 PASS / 1 SKIP / 0 FAIL**
- Verification suite: **126 PASS / 0 FAIL** (`47 + 35 + 12 + 32`)
- Docker/UTF-8/Tool EntryPoint: **12 PASS / 0 FAIL**, EntryPoint **1,017**, duplicate/dead migration 0
- Harness Session Merge: **3 merged / pending 0 / conflict 0**
- Product Conformance: **PASS / findings 0 / untracked 0**
- Harness Strength: **PASS — 218 / 394 / 16 / controls 33**
- Harness Authority: **PASS — work 410 / test 829 / controls 33**
- Migration Semantic Closure: **PASS — 265 / delete eligible 246 / protected retain 19**
- Harness Self Acceptance: **PASS — requirements 218 / work 410 / role 1230 / migrations 265**
- Split Dataset: **PASS**
- Negative Mutation: **BASE 17/17 + AUTH_A 5/5 + AUTH_B 5/5 + STRENGTH 6/6 PASS**
- Detailed Review: **410/410 / omitted 0 PASS**

`run_all_gates.py` 단일 프로세스는 실행환경의 장시간 실행 상한 때문에 Negative 단계 전에 종료됐지만, **동일 Gate 구성요소를 동일 Current Source에서 분할 실행하여 전부 PASS**했다. 이를 monolithic PASS로 위조하지 않는다.

## 5. 이전 Source Physical Evidence와 Current Source의 구분

2026-08-30 13:26 실행의 Source Identity `90e4890d...`에서는 Java25 Full Build/Java Test/Publication/SBOM 및 Oracle/PostgreSQL/MariaDB DB3 Physical이 실제 PASS했다. 이 근거는 환경 가용성과 과거 결함 해소 이력의 provenance로 유지한다.

그러나 Current Source `1289304269e6f684cb9c32414efadbcfa179b5f7208bcd42f6ff1d5dff15a87f`와 다르므로 현재 최종 PASS로 자동 승계하지 않는다. 영향범위 Physical Acceptance는 Current Source에서 Fresh Replay한다.

## 6. Current Mandatory Open Acceptance

1. Windows Fresh VS Code/Buildship/JDT 전체 **Error=0 / Warning=0**.
2. Current Source Java25-target Root Build/Test/Publication/SBOM Fresh Replay.
3. Oracle/PostgreSQL/MariaDB DB3 Physical Full Lifecycle Fresh Replay.
4. Windows/Linux Unified CLI/Generator Physical Lifecycle.
5. Batch 5-role + Worker×2 + kill/takeover/fencing/UNKNOWN/reconcile.
6. One-WAS actual transaction + File/DB/Segment/Timeline log correlation + Runtime OpenAPI.
7. ADM/Backoffice Fresh `npm ci → lint → typecheck → test → build` + Browser E2E/a11y/error states.
8. Performance load/soak/backpressure/resource leak.
9. Actual Open Git Fresh Release + Public Consumer + Leakage 0. Push/commit은 사용자 승인 없이 수행하지 않는다.
10. Same Source Required Full Runtime + Fresh Replay.
11. Codex/Claude Independent Review.
12. QA Final Acceptance.

위 중 하나라도 `VERIFICATION_PENDING/BLOCKED_EXTERNAL/NOT_EXECUTED`이면 CPF 전체 완료가 아니다.

## 7. 다음 세션 시작 순서

1. Current Source Identity를 재계산해 `d54e64e446d99e0601bf36205e492e3568bb56fd0d80bff5e54793249394f97f`와 일치 여부 확인.
2. Session Merge Preflight 실행. 현재 기준 pending/conflict는 0이다.
3. Overlay 적용 후 사용자 Windows에서 Fresh VS Code Problems 0/0부터 확인.
4. Current Source 최대강도 Full Runtime을 실행해 실제 FAIL을 Root Cause WP에 병합.
5. Actual Open Git Release는 모든 선행 Gate 통과 후 수행하며 Git write는 사용자 승인 전 금지.
6. Codex/Claude Independent Review 후 QA Final Acceptance.

H00/H02를 처음부터 재개발하지 않는다. Source Identity drift, Gate regression, 새 Finding이 실제 발생한 영향범위만 재개방한다.
