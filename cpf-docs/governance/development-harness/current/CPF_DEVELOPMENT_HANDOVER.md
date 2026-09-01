# CPF Development Handover — Current

## 1. Current Authority / Source Identity

- 단일 실행 정본: `cpf-docs/governance/development-harness/`
- 입력 Source ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260830_140354.zip`
- 입력 ZIP SHA-256: `c9f7bd68edc9891638280453bd82ab8000efc5b052a556cc67030d76744fb395`
- Current Product Source Identity: `bb67ca18fcaceb9ddfda5c082f2f38e8deb76b5a58f30afc6c4ad04c31400d74` / **8,450 files**
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
- baseline Source Identity: `bb67ca18fcaceb9ddfda5c082f2f38e8deb76b5a58f30afc6c4ad04c31400d74`
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

- Product Source Identity: `bb67ca18fcaceb9ddfda5c082f2f38e8deb76b5a58f30afc6c4ad04c31400d74` / 8,450 files
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

1. Current Source Identity를 재계산해 `bb67ca18fcaceb9ddfda5c082f2f38e8deb76b5a58f30afc6c4ad04c31400d74`와 일치 여부 확인.
2. Session Merge Preflight 실행. 현재 기준 pending/conflict는 0이다.
3. Overlay 적용 후 사용자 Windows에서 Fresh VS Code Problems 0/0부터 확인.
4. Current Source 최대강도 Full Runtime을 실행해 실제 FAIL을 Root Cause WP에 병합.
5. Actual Open Git Release는 모든 선행 Gate 통과 후 수행하며 Git write는 사용자 승인 전 금지.
6. Codex/Claude Independent Review 후 QA Final Acceptance.

H00/H02를 처음부터 재개발하지 않는다. Source Identity drift, Gate regression, 새 Finding이 실제 발생한 영향범위만 재개방한다.

## 8. Claude 세션 20260901 — Runtime Blocker Root Cause 진행 상태

Codex 후속 인계를 위한 현재 상태다. 세부 근거는 `CPF_FULLRUN_RUN26/28` 로그와 각 게이트 실행 결과다.

### 8.1 종결된 Root Cause (Source 반영 완료)

| Runtime 단계 | 확정된 Root Cause | 조치 |
| --- | --- | --- |
| `[12] TESTING_TOOLS` | 계약 테스트가 `validate()`가 읽지 않는 `cpf-docs` 전체를 복사 → 실행 중 Gradle 캐시 잠금으로 실패 | 검증기에 `CONTRACT_SOURCES` 정본 선언, 테스트는 그것만 복사 |
| `[141] BATCH_TWO_WORKER` | Canonical 6 헤더 미전송 / body actor 불일치 / `BAT_JOB` FK 선행행 부재 / `CPF_CENTER_CUT_PARAMETER_KEY` 부재 / PowerShell 배열 언롤링 | harness를 계약에 맞춤. 실행별 임시 키는 자식 환경변수로만 전달 |
| `[142] GATEWAY_BATCH_RUNTIME` | `@ConditionalOnBean` 오용 / DataSource 다중 후보 / Route 부트스트랩 / 생성자 모호성 / 고아 Provider / Query Pack 컬럼 드리프트 | role 명시 바인딩, Provider 조합, V127 기준 Query Pack 정렬 |
| `[148] LOCAL_ONE_WAS_START` | CPF stereotype 클래스 `final` / AutoConfiguration 미등록 / HTTP Provider 미조합 / Spring Boot 4 `WebClient.Builder` 자동설정 중단 / 생성자 모호성 / FeatureFlag Provider 미조합 | 각 계층별 정본 수정 |

### 8.2 Spring Boot 4 마이그레이션 잔여 리스크

Spring Boot 4.1은 Boot 3에서 제공하던 Bean 일부를 더 이상 auto-configure 하지 않는다. CPF가 직접 소유해야 한다.

- `com.fasterxml.jackson.databind.ObjectMapper` — Boot 4는 Jackson 3 `tools.jackson...JsonMapper`를 만든다. `CpfJackson2AutoConfiguration`이 소유하며, 보안 스타터 대체 Bean에 밀리지 않도록 순서를 명시했다.
- `WebClient.Builder` — Boot 4에 `WebClientAutoConfiguration`이 존재하지 않는다(부트 JAR 내 전 모듈 검색으로 확인). HTTP Integration 스타터가 prototype scope로 소유한다.
- 동일 유형이 더 있을 수 있다. `required a bean of type` 실패가 나오면 먼저 Boot 4 자동설정 제거 여부를 확인한다.

### 8.3 신규 정적 게이트 (모두 음성 변이 검증 완료)

런타임에서만 드러나던 결함을 실행 없이 차단한다.

- `cpf-tools/verification/tests/test_cpf_proxied_stereotype_not_final.py` — CPF stereotype 클래스 `final` 금지
- `cpf-tools/verification/tests/test_cpf_autoconfiguration_registration.py` — `@AutoConfiguration` 등록 누락 차단
- `cpf-tools/verification/tests/test_cpf_injection_constructor_unambiguous.py` — 생성자 다중 + `@Autowired` 미지정 차단
- `cpf-tools/verification/tests/test_cpf_source_tree_bytecode_hygiene.py` — 도구 import가 Source Tree에 `.pyc`를 남겨 clean-source를 깨뜨리는 자기오염 차단
- `cpf-tools/db/tests/test_query_template_schema_columns.py` — Query Pack ↔ Canonical Schema 컬럼 드리프트 차단. **템플릿과 vendor runtime Pack 양쪽을 검사한다**

### 8.4 Codex가 알아야 할 작업 제약

- **전체 실행 중 Gradle 병행 금지.** 동시 실행은 Gradle 실행 이력과 산출물을 불일치 상태로 만들어 `clean` 이후에도 `UP-TO-DATE` 오판을 유발한다(RUN16/RUN23에서 실측).
- **전체 실행 중 Source/Managed 파일 편집 금지.** `[162] SOURCE_STATE_AFTER` / `[163] MANAGED_STATE_AFTER`가 전후 SHA-256 동일성을 요구한다.
- **`build/` 디렉터리는 clean-source 게이트 검사 대상이 아니다.** 삭제할 필요가 없으며, 삭제하면 IDE 클래스패스가 깨진다. 정리 후에는 `cpfPrepareIdeClasspath` + `cpfVerifyIdeClasspathReady`로 복구한다.
- **pytest는 `-p no:cacheprovider`로 실행한다.** 저장소에 pytest 설정이 없어 `.pytest_cache`가 루트에 생기고 clean-source가 실패한다.
- Query Pack은 `runtime-template`과 `vendor/<vendor>/runtime` 두 계열이 병존하고 자동 동기화가 없다. 한쪽만 고치면 Runtime은 여전히 구 SQL을 실행한다.

### 8.5 남은 Open Blocker

- `[142]` / `[148]` — 각 계층 수정 반영 후 재검증 필요. `[141]`은 2회 연속 PASS로 안정화.
- `[149]~[155]` — `[148]` 종결 시 연쇄 해소 예정.
- Fresh Windows/Linux Consumer, Fresh Replay, 최종 Open Git Release from post-close Source.
- Git write(commit/push/tag/release publish)는 사용자 승인 전까지 금지.

### 8.6 사용자 판단이 필요한 사항

- `cpf.gateway.allow-empty-routes` 기본값 `false`로 인해 **신규 설치 Gateway는 Route 등록 전까지 기동 불가**다. `GW_BINDING`은 product/sample seed 어디에도 행이 없다(Route는 운영 데이터). 기본값 유지(설치 절차에서 플래그) vs 변경은 보안 자세 결정이라 임의 변경하지 않았다.
- 미등록 `@AutoConfiguration` 4건이 현재 비활성이다: `CpfCacheAutoConfiguration`, `CpfAttachmentRuntimeControlAutoConfiguration`, `CpfTabularAutoConfiguration`, `CpfOtlpTelemetryAutoConfiguration`. 활성화는 Runtime 동작을 바꾸므로 근거 확인 전까지 켜지 않고 게이트의 `KNOWN_INACTIVE`로 기록했다.
