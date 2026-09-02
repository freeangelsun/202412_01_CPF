## 2026-09-02 Claude 변경 근거 — Codex 교차 원복 방지 (이 절을 먼저 읽으라)

아래는 Claude 세션이 물리 실행 로그 근거로 반영한 변경이다. 되돌리려면 각 항목의 '증상 근거'가
왜 더는 발생하지 않는지 먼저 제시하라. 판단이 갈리면 여기에 반박 근거를 적고 진행하라.

## 2026-09-02 Claude 세션 — 변경 근거 (Codex 교차 원복 방지용)

원칙: 아래 5건은 모두 **물리 실행 로그에 남은 실패**를 원인까지 좁혀 고친 것이다.
되돌리려면 "그 실패가 왜 더는 발생하지 않는지"를 먼저 제시해야 한다.
증상만 보고 되돌리면 RUN36에서 확인된 기동 실패가 그대로 재발한다.

---

### C-1. `AdmFileJobService` — `@Qualifier("admTransactionManager")` 추가
- **증상 근거**: RUN36 `[148] LOCAL_ONE_WAS_START` FAIL.
  `Parameter 4 of constructor in AdmFileJobService required a single bean, but 3 were found:
   MBW_TRANSACTION_MANAGER / admTransactionManager / cpfCommonTransactionManager`
- **원인**: 1-WAS는 ADM+Backoffice+Common을 함께 합성한다. `PlatformTransactionManager` 후보가
  3개인데 `@Primary`가 없고 파라미터명 `transactionManager`와 같은 Bean도 없다. Spring이 해소 불가.
- **왜 이 값인가**: 같은 패키지 `AdmFileJobRepository`가 `@Qualifier("admJdbcTemplate")`를 쓴다.
  `admJdbcTemplate`은 `admDataSource` 기반이므로 짝이 되는 TX Manager는 `admTransactionManager`다.
  `cpfCommonTransactionManager`로 바꾸면 **다른 DataSource에 커밋**하게 되어 무증상으로 깨진다.
- **되돌리면**: 1-WAS 기동이 즉시 실패한다(모듈 단독 실행에서는 후보가 1개라 통과하므로 오판 주의).
- **재확인**: `pytest cpf-tools/verification/tests/test_cpf_infrastructure_injection_resolvable.py`

### C-2. `AdmNotificationOutboxService` — `@Qualifier("cpfCommonTransactionManager")` 추가
- **증상 근거**: RUN34 `[148]` FAIL, 동일 메시지(Parameter 2).
- **왜 C-1과 값이 다른가**: 이 클래스는 생성자에서 `@Qualifier("cpfJdbcTemplate")`를 받는다.
  `cpfJdbcTemplate`은 `cpfCommonDataSource` 기반이므로 `cpfCommonTransactionManager`가 정답이다.
  **C-1과 값이 다른 것은 의도된 것이다. 통일하지 마라.**

### C-3. `JdbcCpfGatewayRegistryAdapter` — probe 쿼리 2곳의 `response_timeout_ms` 출처 변경
- **증상 근거**: RUN36 `[142] GATEWAY_BATCH_RUNTIME` FAIL.
  `java.sql.SQLSyntaxErrorException: Unknown column 'g.response_timeout_ms' in 'SELECT'`
- **원인**: 별칭 `g`는 `GW_SERVER_GROUP`인데, 정본 스키마상 `response_timeout_ms`는 `GW_BINDING`
  소유다(`cpf-tools/db/generated/current/*/cpf-platform-schema.sql` 확인). `GW_HEALTH_POLICY`
  같은 대체 테이블은 존재하지 않는다.
- **왜 상관 서브쿼리인가**: `GW_BINDING`에는 `endpoint_code`가 없어 `GW_SERVER_GROUP`과 이을 수
  있는 정본 키가 `service_id`+`environment_code`뿐이다. JOIN하면 binding 수만큼 행이 증식해
  probe 후보가 중복된다. 그래서 `MAX(...)` 상관 스칼라 서브쿼리를 썼다.
- **`probeTimeoutMs()` 방어 이유**: 활성 binding이 없으면 서브쿼리가 NULL이다.
  `claimHealthProbe`는 `queryForList` 경로라 `((Number)null).intValue()`로 NPE가 난다.
  (다른 한 곳은 `rs.getInt`라 NULL이 0이 되어 기존 `Math.max(250,...)`로 안전하다.)
- **스키마를 바꾸지 않은 이유**: `GW_SERVER_GROUP`에 컬럼을 추가하는 것은 정본 스키마 + 3벤더 팩 +
  마이그레이션 변경이다. 기존 정본 데이터로 값을 얻을 수 있으므로 스키마를 건드리지 않았다.

### C-4. Platform Runtime 12개 `application.yml` — `cpf.logging` 정본 블록 선언
- **증상 근거**: RUN34 `[142]` GWY 기동 실패.
  `IllegalArgumentException: cpf.logging.files에는 하나 이상의 로그파일 정책이 필요합니다.`
- **원인**: `CpfRuntimeLoggingAutoConfiguration`은 `matchIfMissing=true`로 항상 활성이고
  `CpfApplicationLoggingPolicyValidator`가 빈 files를 거부한다. 그런데 이 계약을 선언한 것은
  Generated Domain 3개뿐이고 Platform Runtime 12개는 전부 누락이었다.
- **처음에 시도했다가 되돌린 방법(중요)**: `CpfApplicationLoggingProperties.files`에 기본값을 주는
  방식을 먼저 만들었다가 **되돌렸다**. `CpfRuntimeLoggingAutoConfigurationTest
  .failsWithActionablePropertyWhenFilesAreMissing`가 "선언 없으면 크게 실패한다"를 의도적으로
  고정하고 있다. 기본값 주입은 그 가드를 약화시키는 우회다.
  → **다시 기본값 방식으로 바꾸지 마라. 그 테스트와 충돌한다.**
- **1-WAS가 그동안 이 실패를 안 겪은 이유**: component-scan Bean이 auto-configuration보다 먼저
  인스턴스화되어 ADM 실패가 먼저 보고됐다. `[148]`을 고치면 드러날 계층이었다.

### C-5. `smoke-bat-two-worker-runtime.ps1` — START 전 Target 재료화 대기 추가
- **증상 근거**: RUN36 `[141]` FAIL.
  `Approved Center-Cut START did not enter RUNNING: state=STARTING`
- **원인**: **제품이 아니라 검증기 결함이다.** `CenterCutExecutionService.nextState()`는
  `target_complete_yn='Y'`일 때만 START를 RUNNING으로 보내고 그 전에는 `STARTING`을 낸다.
  Codex가 추가한 검증기가 대기 없이 START를 호출해 정상 중간 상태를 실패로 읽었다.
- **범위**: Codex의 응답유실 프록시 설계(`delay_http_response_proxy.py`)와 kill/fencing 시나리오는
  **그대로 두었다.** 누락된 대기만 넣었다. 제품 코드는 건드리지 않았다.

### C-6. 신규 정적 게이트 2건 (음성 변이 검증 완료)
- `test_cpf_runtime_logging_policy_declared.py` — C-4 재발 차단
- `test_cpf_infrastructure_injection_resolvable.py` — C-1/C-2 재발 차단
- **주의**: 후자는 1-WAS 합성 모듈 집합을 명시한다. 모듈 구성이 바뀌면 `COMPOSED_MODULES`를 갱신해야
  한다. `@Value("${a:b})"` 안의 괄호 때문에 단순 정규식은 파라미터를 놓친다(실제로 그래서
  `AdmFileJobService`를 한 사이클 놓쳤다). 괄호 균형 파서를 단순화하지 마라.

### C-7. `AdmOperationsGovernanceService` / `AdmIncidentLifecycleService` — `@Qualifier("cpfStarterClock")`
- **증상 근거**: RUN37 `[148]` FAIL.
  `Parameter 1 of AdmOperationsGovernanceService required a single bean, but 3 were found:
   cpfCommonClock / cpfBrokerClock / cpfStarterClock`
- **왜 3개가 공존하는가**: 세 공급자 모두 `@ConditionalOnMissingBean(name = ...)` 형태다.
  **이름 기준 조건은 같은 타입의 다른 Bean 을 막지 못한다.** 타입 기준이었다면 하나만 남았을 것이다.
- **왜 `cpfStarterClock`인가**: 형제 ADM 클래스(`BrokerReliabilityApprovalOwnerCommandAdapter`,
  `AdmHealthInstanceRegistry`)가 같은 Clock 을 쓴다.
- **왜 파라미터 이름 대신 `@Qualifier`인가**: 형제들은 파라미터 이름(`Clock cpfStarterClock`)에
  의존한다. 기동 실패 메시지 자체가 `-parameters` 가 없으면 이름 해소가 깨진다고 경고한다.
  이름 의존은 유지하되 신규 수정은 명시적으로 간다. **형제 코드를 이름 의존으로 되돌리지 마라.**
- `AdmIncidentLifecycleService`는 아직 실패하지 않았지만 **다음 차례**였다. 게이트가 먼저 찾아냈다.

### C-8. `runtime-start-services.ps1` — 모듈이 2개 이상일 때 instanceId를 모듈로 한정
- **증상 근거**: RUN36/RUN37 `[142]` BAT.
  `CpfRuntimeFenceException: 살아 있는 동일 instanceId가 다른 Runtime process에서 이미 사용 중입니다.`
- **원인**: 이 launcher 는 `CPF_RUNTIME_INSTANCE_ID` 를 비워 두고 Runtime 이 hostname 으로 확정하게
  둔다. `[142]`는 GWY 와 BAT 를 **한 번에** 띄우므로 둘이 같은 instanceId 로 등록하고 두 번째가
  fence 에 걸린다. `[141]` 이 실패해서 생긴 연쇄가 아니다 — 두 실행 모두 22.9초로 동일했다.
- **제품은 정상**: 오류 메시지가 직접 "같은 Host 의 다중 Process 는 각각 다른 instance-id 를 주라"고
  안내한다. `[141]`은 `-Dcpf.runtime.instance-id=bat-<role>-<runId>` 로 이미 그렇게 한다.
- **왜 `<hostname>-<module>`인가**: 기존 주석의 결정("`<SYSTEM>01` 같은 serial 을 만들지 마라")을
  지키면서 실제 host 정체성을 유지한다. `CpfInstanceIdentity` 는 시스템속성 → `CPF_RUNTIME_INSTANCE_ID`
  → hostname 순이고 금칙어 목록만 검사하므로 이 형태가 유효하다.
- **모듈이 1개일 때는 종전대로 hostname** 을 쓴다. 단독 실행 의미를 바꾸지 않기 위해서다.

### C-9. `smoke-bat-two-worker-runtime.ps1` — claim 실패 시 진단 증거 출력
- **이유**: RUN37 `[141]`은 `Worker-1 did not own a live Center-Cut claim ... state=FAILED|1|0|1|0`
  으로 죽었는데, **Item 이 왜 FAILED 인지가 어디에도 남지 않았다.** 프록시는 `accept` 타임아웃으로
  끝났고(연결이 한 건도 오지 않음) worker/center-cut 로그에도 처리 오류가 없다. 검증 DB 는 일회성이라
  사후 조회도 불가능하다.
- **조치**: 단정 실패 시 `BAT_CENTER_CUT_ITEM`의 state/result_code/result_message, 해당 service 의
  `OPS_SERVICE_ENDPOINT` 행, 프록시 로그를 함께 남긴다. **읽기만 하며 행을 만들지 않는다.**
- **미해결로 남긴 것**: 왜 Domain 호출이 프록시(8286)로 가지 않았는지는 아직 모른다. 가설은
  `operationId='ping'` 의 endpoint 해소 경로가 harness 가 고쳐 쓴 `MBR_API` 행과 다르다는 것이다.
  다음 실행의 진단 출력으로 확정한다. **추측으로 먼저 고치지 않았다.**

---

## 반영 상태 (RUN37 기준)

| 항목 | 결과 |
| --- | --- |
| RUN36 | 135 PASS / 3 FAIL, `[161]` VERIFIED, `[162]`/`[163]` PASS |
| RUN37 | 135 PASS / 3 FAIL, `[161]` VERIFIED, `[162]`/`[163]` PASS |
| C-3 확인 | RUN37 `runtime-gwy.out.log` 예외 0건 (RUN36 의 Unknown column 소멸) |
| C-5 확인 | RUN37 `[141]` 62s → 245s, `target materialization` / `START→RUNNING` PASS |
| 남은 FAIL | `[141]` Domain 호출 라우팅, `[142]` BAT fence(C-8 반영), `[148]` Clock(C-7 반영) |


---

# Codex Collaboration Handoff — Active

This is a live coordination record for a stopped or resumed Claude/Codex session. It is
provenance only: it does not promote any work item to `CLOSED` or `PASS`.

## 2026-09-02 continuation — read this section first

- Read-only baseline now observed: `master` / `84106ce0f485dc33b36c036abf3a97d33fb65740`.
  Commits `30159024` and `84106ce0` appeared after the earlier `f9669afc` baseline. Codex made
  no Git write; do not amend, reset, or overwrite those concurrent changes.
- Current uncommitted Codex work is deliberately limited to the Batch response-loss verifier:
  - `cpf-tools/runtime/tools/delay_http_response_proxy.py` (new loopback, one-shot proxy);
  - `cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1`;
  - `cpf-tools/runtime/tools/tests/test_bat_two_worker_runtime_shell_contract.py`.
- Current source identity after that exact verifier source change:
  `contentSha1=f16fa56e9316a322ebe15c9386514ad537d9269b`,
  `contentSha256=e533576d79e19b5771cd1c5e2676e561255e30f2364114aa13702ce3471c4080`.
- Why this is necessary: the prior runtime at
  `C:/Users/fly10/Downloads/CPF_CODEX_BATCH_POSTBUILD_20260902_073518` passed the five Batch
  roles, second Worker, generated Domain, DB claim, drain/resume, and physical Worker kill.
  However it waited until the Center-Cut execution had already completed before killing the
  Worker, leaving `UNKNOWN=0` and `explicit UNKNOWN reconcile + fencing takeover=NOT_TRIGGERED`.
  That evidence is useful but **not CLOSED**.
- The new verifier proxy forwards one real `Batch -> Domain` request unchanged, waits until the
  Domain has produced its HTTP response, then delays only that response. The harness observes
  Worker-1's live DB claim, kills that actual claim owner, restores only the service endpoint
  routing, and fails closed unless lease expiry produces `UNKNOWN_RESULT`, approved
  `reconcile-unknown` runs, Worker-2 completes the work, and its fencing token increases. It
  never inserts/updates Batch Item, Claim, or Execution rows to fabricate a result.
- Static proof for this delta is current: Batch shell contract `10 passed`, Python compile PASS,
  PowerShell parser PASS, and `git diff --check` PASS. The physical rerun has not started yet;
  source changes are now locked until that run finishes.
- VS Code remains `SOURCE_FIXED / VERIFICATION_PENDING`: classpath gates passed, but there is no
  fresh exported `CPF_VSCODE_PROBLEMS_*.json`; do not claim Error=0/Warning=0.

## Historical continuation boundary (superseded baseline; retain for provenance)

- Earlier baseline was `master` / `f9669afcfe5b7d41a534d46d2eff0bcdb1ba271c`.
- No Git write was performed by Codex in that phase.
- The following historical section records the original uncommitted source root-cause work that
  is now present in the observed shared history. Do not overwrite it without reconciling the
  Root Cause and regressions:
  - `cpf-tools/build/cpf-root-conventions.gradle`
  - `cpf-tools/verification/tests/test_cpf_gradle_task_group_readability.py`
  - `cpf-tools/verification/tests/test_cpf_developer_shell_contract.py`
  - `cpf-tools/db/generator/generate-official-db-vendor-source.ps1`
  - `cpf-tools/db/tests/test_mariadb_profile_verify_contract.py`
  - generated current DB Provision/Verify projections under `cpf-tools/db/vendor/{mariadb,postgresql,oracle}/`.

## Active WP — dynamic Domain task projection

- Observed failure: root `cpfBuildMember` first failed because `GradleBuild.tasks` received a
  Groovy `GString`; after string normalization it failed again because the nested Gradle build
  made the Domain `settings.gradle` attempt to `includeBuild` its parent root.
- Root Cause: `GradleBuild` is an in-process nested build and Gradle correctly forbids a child
  composite from including that parent build.
- Source correction in progress: every generated/optional Domain build, verify, standalone run,
  deploy, and internal target task now invokes the root Gradle wrapper as an isolated OS process
  with `--project-dir <domain>`, current `-PcpfProductCompositeRoot`, and the selected DB vendor.
  This preserves current-source consumption without stale publication fallback.
- Actual consumer evidence: `cpfBuildMember` completed successfully after the correction:
  root task → isolated Domain process → current-source composite → `:online:build` and
  `:batch:build`; 102 Domain actions, `BUILD SUCCESSFUL`.
- Regression source test added: it rejects a return to `GradleBuild` and requires the isolated
  wrapper/current-source contract for every Domain axis.
- Actual complete-consumer evidence: `cpfBuildAll` passed (596 actionable tasks) and includes
  `cpfInternalBuildBackofficeOnline`, `cpfInternalBuildExternalOnline`, and
  `cpfInternalBuildMemberBatch`; individual `cpfBuildBackoffice` also passed (89 actions).

## Active WP — DB Verify Pack physical-owner projection

- Root Cause: the official DB source generator iterated every enabled application module. The
  shared `cpfDB` owner and its enabled education alias therefore emitted duplicate
  `table_count`, `table_engine_collation`, and `runtime_transaction_id_contract` check names.
  `initialize-cpf-database.ps1` correctly failed closed on that duplicate; this was not a
  runner-only false failure and has not been suppressed.
- Source correction: the generator now resolves exactly one explicit physical owner for every
  enabled logical database and uses that collection for Provision, service-account/Grant, and
  Verify projections. A profile with zero or multiple owners fails during generation.
- Canonical generated outputs were regenerated through the official generator and both vendor
  bundle builders. The affected MariaDB, PostgreSQL, and Oracle Provision/Verify projections are
  intentionally part of the same uncommitted source set.
- Regression: isolated pytest of DB + Gradle contracts passed `48 passed, 3 subtests passed`.
  Direct generated SQL correlation confirmed unique check names: MariaDB 20, PostgreSQL 6,
  Oracle 6; duplicate count is zero for each vendor.
- Physical runtime evidence on this current source identity:
  `contentSha1=7ce6806a000d39ec85eebcb4cc57fdd2774f17da`,
  `contentSha256=0afd0f969b8b62bfc1302a60d1229332ee4a943de56fc1139f5ce496dbd0053c`.
  Verifier-owned Docker MariaDB lifecycle is PASS at
  `C:/Users/fly10/Downloads/CPF_CODEX_DB3_MARIADB_20260831_221126`:
  FreshInstall, pre-current fixture, Upgrade, Rollback/Reapply, and cleanup all passed; the
  installer reported `MariaDB canonical verify=PASS checks=20`.

## VS Code / classpath observation

- User-reported missing class folders for `web-api`, `secure-api`, `browser-bff`, and
  `batch-service` were physically absent/stale JDT references, not missing declared dependency
  edges.
- Current-source remediation command passed:
  `gradlew cpfPrepareIdeClasspath cpfVerifyIdeClasspathReady cpfVerifyIdeClasspathModel` →
  `javaProjects=86`, all canonical outputs present, model gate PASS.
- Fresh VS Code Problems JSON is still required before any `0 Error / 0 Warning` claim. No
  suppression, severity exclusion, or fabricated Problems export was used.

## RUN13 continuation finding (prior-source provenance)

- RUN13 result directory: `C:/Users/fly10/Downloads/CPF_LOCAL_VALIDATION_20260831_204607`.
- It is stopped, not running: `PASS=147`, `FAIL=4`, `SKIP_ENV=2`, `NOT_EXECUTED=7`.
- Prior result source identity: `08b7d1615ff36d81001976ac93ad91743e0a5a29ee42aedfbeba5b2e86e1a471`.
  It is not the current source after the active Gradle changes.
- Single upstream Root Cause observed in stages 125, 140, and 145: MariaDB initialization rejected
  duplicate `check_name` `<verification database>.table_count`. It has a current-source physical
  MariaDB lifecycle PASS above, but the affected Batch/One-WAS consumers still need their own
  reruns. The prior failure blocked DB3 MariaDB,
  Batch runtime DB preparation, and One-WAS runtime DB preparation; the remaining One-WAS logging,
  ADM/Backoffice OpenAPI, Browser, and Performance stages were not executed only as a consequence.

## Next safe sequence

1. Rerun the affected Batch and One-WAS DB-preparation consumers against the current DB projection;
   then run the required File/DB transaction-lineage runtime stages.
2. Run PostgreSQL and Oracle physical runtime matrices separately; never inherit MariaDB PASS.
3. Recalculate current source identity and run fresh VS Code Problems collection after the final
   source change; then run the canonical final runtime/fresh replay.

### C-10. `AdmPlatformVersionController` / `AdmOpenApiController` — `final` 제거
- **증상 근거**: RUN38 `[148]` FAIL.
  `IllegalArgumentException: Cannot subclass final class com.cpf.admin.opr.controller.AdmPlatformVersionController`
- **원인**: 클래스에는 `@RestController` 만 있고 프록시를 유발하는 `@CpfTransactional`/`@CpfPermission`
  (그리고 `AdmOpenApiController` 는 `@PreAuthorize`)이 **메서드에** 붙어 있다. CGLIB 은 final 클래스를
  상속할 수 없다.
- **왜 110개를 다 고치지 않았는가**: 저장소에 `final` Spring Bean 이 110개 있지만 **프록시되지 않으면
  문제가 없다.** 특히 `@Scheduled` 만 가진 11개는 지금 `final` 인 채로 정상 동작한다(스케줄러는 대상
  메서드를 직접 부르며 프록시를 만들지 않는다). 실제 프록시 유발 annotation 을 가진 2건만 고쳤다.
  **`@Scheduled` 클래스에 `final` 제거를 확대 적용하지 마라. 근거 없는 변경이다.**
- **게이트 보완**: `test_cpf_proxied_stereotype_not_final.py` 가 클래스 선언부만 보고 있었다.
  메서드 annotation 까지 보도록 넓히고 `@Scheduled` 는 제외했다. 변이 2건(클래스별) 검출 확인.

### C-11. PowerShell 스크립트 참조 경로 4건 수정
- **증상 근거**: RUN38 `[142]`. 기동은 전부 성공(`status=완료`)했는데 그 다음 줄에서
  `The term '...\cpf-tools
untime	ools\smoke-openapi.ps1' is not recognized`.
- **원인**: `smoke-openapi.ps1` 은 `cpf-tools/verification/openapi` 에만 있다. `runtime/tools` 에는
  **git 이력상 한 번도 존재한 적이 없다**(`git log --all` 확인). 그동안 앞 계층 기동 실패가 먼저
  나서 가려져 있었다. 호출 파라미터(`-Root/-Modules/-ResultDir/-RequireRuntime`)는 정확히 일치한다.
- **전수 조사로 같은 형태 4건**: `smoke-openapi.ps1`, `initialize-cpf-database.ps1`,
  `runtime-common.ps1` x2. 모두 정본 상대경로로 교정.
- **게이트 신설**: `test_cpf_powershell_script_references_exist.py`. 변이 검출 확인.

### C-9 보정 — 진단이 진단을 가리지 않도록
- RUN38 에서 C-9 진단 SQL 이 `Unknown column 'i.item_state'` 로 **먼저** 던져 정작 claim 실패 사유를
  또 놓쳤다. 컬럼명을 확인하지 않고 추측한 잘못이다.
- 정본 컬럼(`item_status`, `retry_count`, `last_error_message`, `claim_status`, `runner_id`)으로
  고치고, 진단 SQL 을 try/catch 로 감싸 실패해도 본래 단정 메시지가 남도록 했다.

---

## 게이트를 만들 때의 교훈 (Codex 도 참고)

이번 세션에서 내가 만든 게이트가 **세 번** 결함을 놓쳤다. 모두 "음성 변이 1건 통과"만 확인하고
규칙의 적용 범위를 확인하지 않아서 생긴 일이다.

1. **타입 하드코딩** — 검사 대상 타입을 목록으로 고정해 `Clock` 을 놓쳤다.
2. **주석 오작동** — 내가 단 주석에 "Qualifier" 단어가 들어가 skip 조건이 오작동했다.
3. **선언부만 검사** — 메서드 annotation 이 만드는 프록시를 못 봤다.

게이트를 추가할 때는 "이 규칙이 놓칠 수 있는 형태"를 먼저 나열하고 각각을 변이로 확인한다.

### C-15. mandatory Admin Route Provider 계약 — Codex 변경 2건을 되돌렸다 (근거 필독)

**되돌린 것**: `AdmFeatureFlagController` / `AdmResiliencePolicyController` 의
`@ConditionalOnProperty`(커밋 `84106ce0`).

**되돌린 근거**:
- `CPF_ADM_UI_FUNCTION_REQUIREMENTS.csv`(ADM capability registry, 80행)가 두 Route 를 등록된 ADM
  기능으로 선언한다 — `ADMUI-054 featureFlags`(canonical_ref ADM-APPROVAL, ADM-AUDIT),
  `ADMUI-056 resiliencePolicies`(ADM-APPROVAL, ADM-EXS).
- `adm-route-operation-contract.ts` 는 "Generated from ADM capability registry and canonical runtime
  OpenAPI" 이며 두 Route 의 operation 을 모두 요구한다.
- 즉 mandatory Admin Route 다. Consumer 에 조건을 붙이면 설정으로 Route 가 사라져 **계약이 축소**된다.

**Codex 판단이 옳았던 부분**: 조건 없이 필수 주입하면 Provider 가 꺼진 Runtime 이 기동조차 못 한다는
지적은 정확했다. 다만 해법이 Consumer 쪽이 아니라 Provider/Composition 쪽이었다.

**대신 한 것**: capability owner 의 AutoConfiguration 을 `matchIfMissing = true` 로 바꿔 **모듈을
Composition 에 선언하는 행위 자체를 opt-in** 으로 삼고, 속성은 끄기 위한 수단으로 남겼다.
- `CpfFeatureFlagAutoConfiguration`, `CpfResilienceAutoConfiguration`, `CpfRemoteLogLocalAutoConfiguration`
- 기본 제공으로 바꾸면 그 Bean 들의 무자격 `DataSource`/`PlatformTransactionManager`/`Clock` 주입이
  합성 Runtime 에서 곧바로 다음 실패가 된다. 그래서 canonical role
  (`CpfDataSourceRegistry.require(CPF_PLATFORM_DB)`)과 `@Qualifier` 로 함께 해소했다.

**반대 의견이 있으면**: Consumer 조건부로 되돌리기 전에 ADMUI-054/056 이 registry 에서 제거되었거나
optional 로 표기되었다는 근거를 먼저 제시하라. 그 근거 없이 되돌리면 ADM 제품 기능이 사라진다.

### C-16. Canonical Config Owner — Log Root
- `cpf.logging.root`(`CpfApplicationLoggingProperties`, 기본 `logs`)를 Log Root 정본 소유자로 확정했다.
- `CpfRemoteLogLocalAutoConfiguration` 의 폴백이 `cpf.logging.file.base-path` 였다. 같은 네임스페이스에
  얹혔지만 그 properties 클래스의 필드가 아닌 중복 철자여서, 정본 `cpf.logging.root` 를 선언해도
  연결되지 않았다. 소비자가 정본을 따르도록 고쳤다.
- **미완으로 남긴 것**: `CpfLogPathPolicy` 도 같은 중복 키를 읽는데 그쪽은 **절대경로를 강제**한다.
  정본 기본값은 상대경로 `logs` 라 일괄 치환하면 파일 로그 경로 해소가 깨진다. 의미 충돌을 해소한 뒤
  별도로 은퇴시킨다. **검증 없이 두 키를 합치지 마라.**

### 신규 Validator
- `test_cpf_mandatory_route_provider_contract.py` — mandatory Admin Route 가 요구하는 Port 는
  opt-in Provider 만으로 충족될 수 없다. 음성 변이 확인(remote-log 를 opt-in 으로 되돌리면 검출).
- Harness `CPF_DEVELOPMENT_HARNESS.md` §26 에 판정 순서와 금지 사항을 정본화했다.

### C-17. RCF 전수 종결 — Full Runtime 반복 탐색 중단

사용자 Steering("Full Runtime을 결함 탐색기로 사용하지 마라")에 따라 개별 수정을 멈추고 Root Cause
Family 별 repo-wide 감사를 먼저 수행했다.

- **RCF-1 multi-bean 주입 모호성**: 후보 86건(Clock/TX/DataSource/JdbcTemplate/ObjectMapper 등)을
  상한 추출해 분류했다. `ObjectMapper`는 공급자 1개, batch/gateway는 `@Primary` 보유, 나머지는
  opt-in이라 **실제 결함 0건**이 남았다. 남아 있던 `CpfResilienceAutoConfiguration`의 `@Bean`
  파라미터 11곳을 canonical role(`cpfStarterClock`, `cpfCommonTransactionManager`,
  `CpfDataSourceRegistry.require(CPF_PLATFORM_DB)`)로 자격 지정했다.
- **RCF-3 app-class 전용 등록**: boot app 15개를 감사했다. ADM만 `@EnableConfigurationProperties`가
  `AdmApplication`에만 있어 1-WAS에서 실패했고, `AdmConfigurationPropertiesRegistrar`(component-scan)로
  옮겼다. **batch는 역할별 독립 Context로 각자의 app class를 그대로 쓰므로 해당 없음**이다.
  batch app class의 `@Import(BatDataSourceConfiguration)` 등을 옮기지 마라 — 불필요하다.
- **Validator 확장**: 주입 게이트가 `@Bean` 파라미터를 보지 않아 이번 회귀를 놓쳤다. 확장 후
  20건 과다검출을 4단계로 정밀화했다(opt-in / `@ConditionalOnClass` / `@ConditionalOnBean` /
  **`@ConditionalOnSingleCandidate`** 제외, 1-WAS의 프로그램적 DataSource primary 반영,
  admin 의존 계약이 금지한 optional provider leaf 제외).

**주의**: 작업 중 heredoc이 정규식 ``를 백스페이스 문자(0x08)로 바꿔 게이트가 조용히 0건을
반환하는 상태가 된 적이 있다. 게이트 수정 후에는 반드시 negative mutation으로 검출을 확인하라.
