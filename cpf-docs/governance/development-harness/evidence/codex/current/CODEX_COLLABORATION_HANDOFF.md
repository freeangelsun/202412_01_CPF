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

**주의**: 작업 중 heredoc이 정규식 `\b`를 백스페이스 문자(0x08)로 바꿔 게이트가 조용히 0건을
반환하는 상태가 된 적이 있다. 게이트 수정 후에는 반드시 negative mutation으로 검출을 확인하라.

---

## 2026-09-03 Claude 세션 — 변경 근거 (Codex 교차 원복 방지용)

이 절의 변경은 모두 **물리 실행 로그에 남은 실패**를 원인까지 좁혀 고친 것이다.
되돌리려면 "그 실패가 왜 더는 발생하지 않는지" 먼저 제시하라.

### C-18. Codex 최신 작업 수용 확인 (되돌리지 않았다)

커밋 `1dcfb10b` 과 그 뒤 미커밋 Codex 작업을 **그대로 유지**했다. 실행 전에 다음을 정적으로
전수 확인했고 전부 실재함을 검증했다.

- `MBR_SAMPLE_TX_CREATE`(POST /api/v1/member/samples)가 생성 manifest에 실재
- `CPF_TRANSACTION_LOG/SEGMENT/LINEAGE`, `OPS_OPERATION_*`, `OPS_SERVICE_CALL_HISTORY`,
  `BAT_CENTER_CUT_ITEM` 의 사용 컬럼 전수 존재
- `MBR_sample_item(sample_key, idempotency_key)` / `MBR_sample_item_idem(operation_code)` 실재
- `cpf.web.internal-peer-identities`, `cpf.operation-policy.seed.allowed-callers`, `CPF_LOG_ROOT` 실재
- `Join-Path` 의 `'\online\...'` 이중 역슬래시와 `"\|ACTIVE\|..."` 정규식은 PowerShell 에서
  실제로 정상 동작함을 실행으로 확인했다(오탐 아님). **고치지 마라.**

### C-19. 검증기 파일락 — live runtime 로그는 공유 모드로 읽어야 한다

- **증상 근거**: `[141]` 이 업무 단정(sampleRows/idempotencyRows/serviceCallSuccess)과
  UNKNOWN→reconcile→fencing takeover 를 **전부 통과한 뒤** lineage 단계에서만
  `Exception calling "ReadLines" ... because it is being used by another process` 로 죽었다.
- **원인**: Windows File Log Owner(`CpfFileLogWriter`)가 rolling 파일 핸들을 연 채 유지하는데
  `[IO.File]::ReadAllText/ReadAllLines/ReadLines` 는 `FileShare.Read` 만 요청한다. **제품 결함이 아니라
  검증기 결함이다.**
- **조치**: `Read-CpfLiveLogText`(FileShare.ReadWrite|Delete)를 도입하고 같은 계열 7개 스크립트를
  일괄 교정했다. `smoke-integrated-log-correlation.ps1` 은 각 읽기를 try/catch 로 감싸므로
  **잠김 예외가 삼켜져 '상관관계 없음'이라는 잘못된 FAIL** 로 보고되던 더 나쁜 형태였다.
- **게이트**: `test_cpf_live_runtime_log_read_sharing.py` (음성 변이 3건 검출 확인)

### C-20. Codex 의 lineage traceId 단정을 완화했다 (제품이 옳다)

- **되돌린 것**: `모든 lineage 행의 traceId == 업무 traceId` 단정.
- **근거**: `CPF_TRANSACTION_LINEAGE.trace_id` 는 스키마상 NULL 허용이고
  `CpfTransactionLineageRecord.fromSegment` 는 traceId 에 null 을 넣는다(원본
  `TransactionSegmentRecord` 에 traceId 필드가 없고 `CPF_TRANSACTION_SEGMENT` 에 trace_id 컬럼이 없다).
  제품의 정본 상관 조회인 `CpfTransactionTimelineQueryFacade` 도 SEGMENT/OUTBOX/DLQ/FILE source 에
  `NULL AS traceId` 를 내보내고 TRACE source 만 값을 준다.
- **대신 한 것**: transactionId 는 그대로 엄격히 보고, traceId 는 **있을 때만** 일치를 요구한다.
  오염된 trace 는 계속 검출된다.
- **반대 의견이 있으면**: SEGMENT source 가 trace_id 를 싣도록 제품 계약이 바뀌었다는 근거를 먼저 제시하라.

### C-21. segment 상관관계 키는 `transaction_segment_id` 다

- **증상 근거**: `[141]` `DB transaction lineage references an orphan segment`.
- **원인**: 검증기가 `CPF_TRANSACTION_SEGMENT.segment_id`(BIGINT AUTO_INCREMENT 대리 PK, 값 `1`)를
  읽었다. 업무 식별자는 `transaction_segment_id`(VARCHAR(120), UNIQUE, `...-SEG-0002-XXXX`)다.
  lineage/파일로그/`CpfTransactionTimelineQueryFacade` 세 소비자가 모두 후자를 쓴다.

### C-22. 마스킹이 CPF 추적 식별자를 훼손하던 제품 결함 (사용자 Steering 반영)

- **증상 근거**: 파일 로그의 `transactionId` 가 `***6762BATS1JCXLU0000001`,
  `traceId` 가 `da3f***6562f69d...` 로 나와 DB 거래와 대조되지 않았다.
- **원인**: `CpfMaskingRuntime.LONG_ACCOUNT_PATTERN` 이 `(?<!\d)\d{10,19}(?!\d)` 라서
  거래ID 앞 17자리 timestamp 와 16진 traceId 안의 숫자열을 계좌번호로 오인했다.
- **조치**: 경계를 **영숫자 토큰 경계**(`(?<![A-Za-z0-9])...(?![A-Za-z0-9])`)로 좁혔다.
  라벨이 붙은 값(`accountNo=...`)은 key 기반 규칙이, 따옴표/구분자로 둘러싸인 순수 숫자열은
  이 규칙이 그대로 막는다. 같은 값이 파일명·DB·ADM 에 원문으로 남으므로 본문만 가리는 것은
  보호 효과가 없고 계약만 깨뜨린다.
- **처음에 만들었다가 제거한 것(중요)**: 정본 거래ID 전용 예외(`startsCanonicalTransactionId`)를
  먼저 넣었다가 **제거했다.** 토큰 경계 규칙이 그 경우를 모두 포함해 **도달 불가능한 죽은 코드**가
  되었기 때문이다. 다시 넣지 마라.
- **고정 테스트**: `CpfMaskingRuntimeTransactionIdTest`
- **미완(사용자 Steering)**: 마스킹 항목은 코드가 아니라 **ADM 운영자 설정**이 정해야 한다.
  조사 결과 `cpf_masking_policy_*` 테이블은 **어떤 SQL 에도 없고** `cpf.security.masking-policy.mode=jdbc`
  는 **어디에도 설정되어 있지 않다** — 제어면만 있고 동작한 적이 없다. WP-R17.02 로 등록했다.

### C-23. 파일 로그 이벤트 키는 이벤트 종류마다 다르다

- **증상 근거**: `The property 'sourceSystemCode' cannot be found on this object.`
- **원인**: ONLINE_TRANSACTION 이벤트에는 `callerSystemCode` 는 있어도
  `sourceSystemCode`/`messageCode`/`errorCode` 는 없다. StrictMode 에서 없는 속성을 읽으면 던진다.
- **조치**: `ConvertTo-FileLogEvidenceRow` 로 존재하는 키만 읽는다.
- **함께 완화**: 파일 로그의 `segmentId` 는 호출을 받은 **Domain 자신의 span** 이고
  `$segmentIds` 는 Batch 쪽 segment 라 계층이 다르다. 두 저장소를 잇는 정본 키는
  transactionId/traceId 이므로 그것으로 단정하고 segmentId 는 비어있지 않음만 본다.

### C-24. Harness 호출부의 팬텀 파라미터

- `start-cpf-local.ps1` 은 `-ResourceProfile/-Mode/-SkipBuild` 만 선언하는데 Full Runtime 이
  `-RepoRoot`, `-WebOnly` 를 넘기고 있었다. `git log --all -S WebOnly` 결과 그 이름은 **이 호출부에만**
  존재한다. CmdletBinding 없는 param() 은 알 수 없는 이름을 조용히 `$args` 로 흘려보내므로
  "웹 계층만 기동" 의도가 한 번도 적용된 적이 없었다.
- **게이트 신설**: `test_cpf_powershell_callsite_parameters.py` (PowerShell AST 기반, 음성 변이 확인)

### C-25. 한글 깨짐 — Python 도구가 자기 출력 인코딩을 고정하지 않았다

- **증상 근거**: `REQUIREMENT_PROGRESS_GATE` 결과가 `"�̱���": 2` 처럼 나왔다.
- **원인**: Windows 콘솔 기본 코드페이지가 cp949 라 `sys.stdout.encoding == 'cp949'` 였다.
  파일은 UTF-8 이 맞았다. PowerShell 진입점들은 이미 각자 UTF-8 preamble 을 들고 있었는데
  Python 진입점 349개 중 5개만 고정하고 있었다.
- **조치**: 한글을 출력하는 tool 진입점 81개에 UTF-8 preamble 삽입, 테스트는
  `cpf-tools/conftest.py` 가 정본 소유자(pytest 는 `sys.__stdout__` 도 함께 고정해야 한다),
  `run-cpf-pytest.py` 는 자식 환경에 `PYTHONUTF8`/`PYTHONIOENCODING` 전달.
- **게이트**: `test_cpf_python_console_utf8.py` (음성 변이 확인). SyntaxWarning 1건도 함께 제거했다.

### 실행 결과 (2026-09-03)

| 구간 | 결과 |
| --- | --- |
| `[142] GATEWAY_BATCH_RUNTIME` | **PASS** (Runtime OpenAPI smoke 포함) |
| `[141] BATCH_TWO_WORKER_CRASH_UNKNOWN` | **PASS** — UNKNOWN→reconcile→fencing takeover→업무효과/멱등/lineage 전부 |

### C-26. Codex 진행 중 작업 수용 + 실행 증거 인계 (되돌리지 않았다)

Codex 미커밋 변경 6건을 **그대로 유지**했고 컴파일/단위테스트 통과를 확인했다.

- `cpf-batch/worker/build.gradle` `:starters:data:mybatis` 추가
- `CpfHttpDomainRemoteTransport` `.attribute("sourceModuleCode", context.currentSystemCode())`
- `CpfEndpointResolver` operation endpoint instance 없으면 service 단위 instance 로 fallback
- `LoggingAspect` 내구 segmentId/parentSegmentId 를 File Log detail 로 전달
- `CpfFileLogWriter` `CORRELATION_METADATA_KEYS` 마스킹 예외 + `messageCode`/`errorCode` 노출

**마스킹 수정이 겹치지만 충돌이 아니다 — 둘 다 필요하다.**
- Codex(`CpfFileLogWriter` 키 허용목록): 파일 로그의 `instanceId`(`bat-domain-2026...`),
  `standardExecutionId` 처럼 **토큰 경계 규칙으로는 살릴 수 없는** 값까지 보존한다.
  (`bat-domain-20260903093421776` 의 숫자열은 앞이 `-` 라 토큰 경계로도 마스킹된다.)
- Claude(`CpfMaskingRuntime` 토큰 경계): 파일 로그뿐 아니라 **DB 요약/감사/Gateway capture 등
  모든 `CpfMaskingRuntime.mask` 소비자**에서 `transactionId`/`traceId` 를 보존한다.
  키 허용목록은 File Log Writer 안에만 있어 다른 소비자를 못 지킨다.
→ 어느 쪽도 지우지 마라. 계층이 다르다.

#### 인계: `[141]` 새 실패의 실행 증거 (RUN 20260903_134952)

Codex 가 새로 추가한 단정
`DB transaction segment does not retain the exact successful BAT→Domain selected-instance/attempt/result`
에서 멈췄다. 저장된 raw projection 은 다음과 같다.

```
expectedDomainInstance = bat-domain-20260903135340725
SEG-0001-5F7C32B2 attempt=1    instance=bat-domain-...  op=BAT_CENTER_CUT_WORK   FAILED  err=IllegalArgumentException
SEG-0001-F8427119 attempt=null instance=null            op=BAT_CENTER_CUT_WORK   RUNNING
SEG-0002-9ED14795 attempt=1    instance=bat-domain-...  op=BAT_CENTER_CUT_WORK   FAILED  err=IllegalArgumentException
SEG-0002-78E1C9E4 attempt=null instance=null            op=MBR_SAMPLE_TX_CREATE  SUCCESS rc=null
SEG-0003-810B5BC2 attempt=1    instance=null            op=BAT_CENTER_CUT_WORK   SUCCESS rc=200
```

판정에 필요한 다섯 속성이 **세 행에 흩어져 있다**.
- 올바른 operation(`MBR_SAMPLE_TX_CREATE`) 행에는 instanceId/attempt/responseCode 가 없다
- responseCode=200 과 attempt 는 상위 `BAT_CENTER_CUT_WORK` 행에 있다
- instanceId 는 FAILED 행에만 있다

**추가로 발견한 것 — 원인 미상의 `IllegalArgumentException` 2건.**
`TransactionSegmentService:132` 의 `scope.fail(ex.getClass().getSimpleName(), ex.getMessage())`
경로다. 즉 실제 예외가 던져졌는데 **Runtime 로그에는 스택이 전혀 없다**(worker-1/2/center-cut 로그
확인). 또한 worker 양쪽에 `CpfTransactionContextAnomalyMonitor - CPF transaction context is missing.
boundary=CpfFileLogWriter.writeIntegration` 가 6건 이상 남는다.

**Claude 가 한 조치(제품 코드는 건드리지 않았다)**: 검증기의 segment projection 에
`failure_message`, `sequence_no`, `transaction_role`, `direction` 을 추가했다. 다음 실행이면
IllegalArgumentException 의 실제 메시지가 evidence 에 남는다. 그 메시지 없이 추측으로 고치지 마라.

**Claude 는 이 lineage 계열을 더 손대지 않는다.** Codex 가 진행 중인 영역이므로 교차 수정을 피한다.
Claude 는 WP-R17.02(운영자 선택 마스킹 영속화 + ADM Route)와 WP-R17.01(Shell 조립성) 으로 이동한다.

### C-27. Codex DB-clock 변경의 authoring 템플릿 누락 (§25.7 위반) — 완결시켰다

- **증상 근거**: `sync-database-artifacts.ps1` 이
  `BAT Runtime Query parameter contract mismatch: vendor=mariadb
   key=scheduler-leader-acquire-update expected=4 actual=6` 로 중단됐다.
- **원인**: 커밋 `1dcfb10b` 이 **생성된 벤더 SQL(4 placeholder, DB clock)과 계약 JSON 은 갱신했는데
  authoring 템플릿(`cpf-tools/db/runtime-template/bat/repository/*.sql.template`)은 옛 형태
  (6 placeholder, client Timestamp)로 남아 있었다.** Harness §25.7("Query Pack은 두 계열을 함께
  갱신한다") 위반이다.
- **더 위험했던 점**: 템플릿이 정본이므로 누구든 DB 아티팩트 sync 를 돌리면 **Codex 의 DB-clock 설계가
  조용히 되돌려진다.** 실제로 첫 sync 에서 `centercut-claim-renew`, `scheduler-leader-heartbeat`,
  `scheduler-leader-is-current` 등이 `UTC_TIMESTAMP(6)` → `CURRENT_TIMESTAMP(6)` 로 회귀했다.
- **조치(Codex 설계 방향으로 완결)**:
  - `sync-bat-runtime-query-pack.ps1` 에 3벤더 매크로 2종 신설
    `@UTC_NOW6@`, `@UTC_NOW6_PLUS_MICROS_PARAM@`
    (mariadb `UTC_TIMESTAMP(6)` / `TIMESTAMPADD(MICROSECOND, ?, UTC_TIMESTAMP(6))`,
     postgresql `(CURRENT_TIMESTAMP(6) AT TIME ZONE 'UTC')` / `+ (? * INTERVAL '1 microsecond')`,
     oracle `SYS_EXTRACT_UTC(SYSTIMESTAMP)` / `+ NUMTODSINTERVAL(? / 1000000, 'SECOND')`)
  - 템플릿 11개를 그 매크로로 정렬
- **검증**: 재-sync 후 `git diff -- cpf-tools/db/vendor/*/runtime` **0건** — 즉 생성물이 Codex 가
  커밋한 벤더 SQL과 **바이트 단위로 동일**하다. 되돌린 것이 아니라 정본 경로를 맞춘 것이다.
- 템플릿에 설명 주석을 넣었다가 제거했다. 렌더러가 주석을 벤더 SQL 로 그대로 내보내 생성물이
  달라지기 때문이다. 근거는 `sync-bat-runtime-query-pack.ps1` 매크로 표 위 주석에 있다.

### C-28. WP-R17.02 마스킹 정책 영속화 — 정본 DDL 신설

- **문제**: `JdbcCpfMaskingPolicyStore` 는 shard/head/version/command 4개 테이블을 요구하면서
  "canonical three-vendor DDL is owned by the DB workstream" 이라고만 적혀 있었고, 그 DDL 이
  **저장소의 어떤 SQL 에도 없었다.** `cpf.security.masking-policy.mode=jdbc` 도 어디에도 없다.
  즉 운영자가 마스킹 정책을 바꿀 수단이 실제로 존재한 적이 없다.
- **조치**: `platform-schema.json` 에 `CPF_MASKING_POLICY_{SHARD,HEAD,VERSION,COMMAND}` 4종을
  CPF 그룹 정렬 위치에 추가(231 → 235). `sync-database-artifacts.ps1` 완주, 3벤더 파리티 PASS.
  운영자 선택 항목은 `value_rules_csv` / `result_value_rules_csv` 로 영속화한다.
- **저장소 SQL 대문자화**: 정본 스키마는 대문자 테이블명이다. 소문자 조회는 identifier case 를
  보존하는 Linux MariaDB 에서 테이블을 찾지 못한다.
- **`getBoolean` 제거**: `mask_bearer_flag` 는 CHAR(1) 'Y'/'N' 이라 `getBoolean` 은 벤더에 따라
  조용히 false 가 된다. `yesNo()` 로 명시 변환한다.
- **게이트**: `cpf-tools/db/tests/test_masking_policy_store_schema_parity.py` (음성 변이 3건 검출).
  단위 테스트 harness 는 `FakeAccess` 메모리 구현이라 SQL/컬럼 어긋남을 잡지 못한다 — 이 게이트가
  그 간극을 닫는다.

### C-29. `[141]` IllegalArgumentException Root Cause 종결 (인계로 끝내지 않고 이어서 닫았다)

C-26 에서 "원인 미상"으로 남겼던 `IllegalArgumentException` 2건을 현재 Source 에서 끝까지 추적해
종결했다. 최종 결과: **`CPF_WORK_UNIT=PASS unit=BATCH_TWO_WORKER`** (Codex 가 추가한 강화 단정 포함).

**원인 1 — 검증기 진단이 정본 컬럼을 몰랐다.**
`failure_message` 는 존재하지 않는 컬럼이고 정본은 `failure_message_masked` 였다.
`ERROR 1054 Unknown column` 으로 한 사이클을 소모했다(과거 `i.item_state` 와 같은 계열, 두 번째).
→ `cpf-tools/db/tests/test_verifier_sql_columns_exist.py` 신설. 검증기 SQL 의 모든 컬럼을
`platform-schema.json` 과 대조한다. 음성 변이 2건 검출 확인. **이제 실행 없이 즉시 잡힌다.**

**원인 2 — Runtime Agent 자기 등록 baseUrl 이 hostname 이었다(핵심).**
`failure_message_masked` 가 실제 메시지를 드러냈다:
`Hostname은 설정된 allowDns 정책 상에 포함되지 않습니다.`
`CpfRuntimeControlAgentAutoConfiguration.resolveRuntimeBaseUrl` 은 `server.address` 가 wildcard 면
`runtime.hostName()` 으로 baseUrl 을 만든다. Codex 의 `CpfEndpointResolver` instance fallback 이
들어오면서 그 instance 로 라우팅되기 시작했고, 정본 네트워크 정책이 hostname 을 거절해
segment 가 `IllegalArgumentException` / `TECHNICAL_FAILURE` 로 남았다. 두 번 실패한 뒤 endpoint
baseUrl 로 우회 성공해서 **성공한 segment 에 selected_instance_id 가 비는** 현상이 나온 것이다.
→ 정책을 느슨하게(`allow-dns=true`) 만들지 않고, 제품이 제공하는 명시 knob
`cpf.runtime.control.agent.runtime-base-url` 로 이 topology 가 실제로 쓰는 IPv4 를 등록하게 했다.
`server.address` 는 건드리지 않아 loopback health probe 도 그대로다.

**원인 3 — instance baseUrl 이 endpoint 보다 우선해 응답유실 프록시를 우회했다.**
원인 2 를 고치자 호출이 곧바로 성공해 kill 대상이 사라졌다(`state=COMPLETED`).
`CpfEndpointResolver` 는 `firstText(instance.baseUrl, endpoint.baseUrl)` 이므로 endpoint 만
프록시로 바꾸면 instance 라우팅이 프록시를 지나지 않는다.
→ 프록시 arm/restore 를 `OPS_SERVICE_ENDPOINT` 와 `OPS_SERVICE_INSTANCE` 양쪽에 적용했다.

**원인 4 — OUTBOUND segment 가 호출자 operation 을 기록했다(제품 결함).**
컬럼 이름이 `target_operation_id` 인데 `TransactionSegmentService.start` 가
`TransactionContext.observedOperationId()`(현재 operation 우선)를 넣어 `BAT_CENTER_CUT_WORK` 가
기록됐다. DB 만 보고는 "이 구간이 `MBR_SAMPLE_TX_CREATE` 를 호출했다" 를 확인할 수 없다.
`CpfDomainClientRouter` 가 원격 호출 직전 `withTargetOperation(operationId)` 로 Context 를 이미
바인딩하므로, **OUTBOUND 에서는 target 을 우선**하도록 소유자에서 고쳤다. INBOUND/LOCAL 은 종전 유지.
→ 고정 테스트 `TransactionSegmentTargetOperationTest` (음성 변이 검출 확인).
이것은 Codex 가 `sourceModuleCode` 로 caller 정체성을 복구한 것과 **같은 계열의 target 쪽 완결**이다.

**최종 실행 증거** (`CPF_WORKUNIT_BATCH_TWO_WORKER_20260903_144952`):
```
explicit UNKNOWN reconcile + fencing takeover before=1 after=2
Generated Domain business effect and retry idempotency sampleRows=1 idempotencyRows=1 serviceCallSuccess=1
Batch→Domain File/DB transaction lineage summaries=1 segments=3 lineage=5 fileRows=1
CPF_WORK_UNIT=PASS unit=BATCH_TWO_WORKER
```

### C-30. One-WAS ADM Session/CSRF → File/DB Log correlation 재검증 (현재 Source에서 PASS)

**검토 범위와 Source:** current `master` `19d536741481d6a239428bc64bbd6c39a133f63f`, clean Working
Tree에서 실행했다. Git-independent Product Source Identity는
`ff370b132093e6ef006ca40e0aef7a42735778e89175c3d7a13b5b365d4ff4d0`이고 실행 전후 값이 동일하다.

**앞선 실패와 Root Cause 분리:** 이전 One-WAS 실행(`8d24e334...`)은 login 이후 최신 XSRF cookie가
Browser에 존재하고 header로도 전송됐음에도 state-changing 요청이 403으로 거절됐다. 이는 현재
Source와 다른 identity이므로 PASS 근거로 승계하지 않는다. 현재 Source는 (1) session-id 회전 owner가
MVC response body 기록 전 configured CSRF repository로 새 cookie를 저장하고, (2) BFF Bridge가 만든
SecurityContext를 Session repository에도 저장하여 `SessionManagementFilter`가 매 요청을 새 인증으로
오인해 session/CSRF를 재회전하지 않게 한다. Product Contract와 Harness에는 같은 경계를 명시했고,
`CpfBffCredentialResponseAdviceTest`, `CpfServerSessionSecurityFilterChainTest`,
`test_cpf_runtime_readiness_ordering_contract.py`가 재발 방지 regression이다.

**정적/Consumer 검증:**

- `:starters:security:session:jdbc:test` — PASS
- readiness + system identity + integrated logging contract pytest 22건 — PASS
- test는 새 XSRF cookie의 response-before-body 발급, stale/missing CSRF 거절 분류, Session
  SecurityContext 영속을 각각 고정한다. skip/xfail/조건완화는 사용하지 않았다.

**실제 Runtime Evidence:**
`C:\Users\fly10\Downloads\CPF_WORKUNIT_ONE_WAS_20260904_125255\work-unit-result.json`

```text
CPF_WORK_UNIT=PASS unit=ONE_WAS totalSeconds=270.1
RUNTIME_DB_PREP / generated EXS·MBR bootstrap / registry seed          PASS
LOCAL_ONE_WAS_START / ADM bootstrap password rotation                  PASS
LOCAL_FILE_LOG_STANDARD / LOCAL_DB_LOG_POLICY_RUNTIME                 PASS
LOCAL_INTEGRATED_LOG_CORRELATION                                      PASS
ADM + MBW Runtime OpenAPI release parity / stop / DB cleanup           PASS
SOURCE_IDENTITY_AFTER ff370b132093e6ef006ca40e0aef7a42735778e89175c3d7a13b5b365d4ff4d0
```

대표 상관 거래는 `transactionId=20260904125554274MBWlogcor10000001`,
`traceId=64f6b784a8b14ab4a3edd5de2703efbf`,
`segmentId=20260904125554274MBWlogcor10000001-SEG-0004-04739D8E`,
`instanceId=cpf-local-5a51ec13e1df`다. File event 2건, DB Summary 1건, ADM transaction log 1건,
ADM Timeline segment 1건이 같은 transaction/trace/segment/MBW target operation(`MBW_AUTH_LOGIN`)으로
대조됐고, File↔DB correlation PASS, raw secret leak 0, fallback/recovery pending/quarantine/terminal loss 0,
fatal runtime marker 0을 확인했다. 이 대표 건은 intentional authentication failure(`401`,
`ECPF990000`, `ResponseStatusException`)이며 root inbound segment라 parent/attempt가 null이다. 따라서
성공·업무 rollback·remote A→B→C·retry/UNKNOWN/reconcile 전체 lineage의 대체 근거로 사용하지 않는다.

**상태 판정:** `WP-ONE01`의 이번 One-WAS substeps(RT-141~145 경로)는 **SOURCE_FIXED + physical
PASS evidence**다. 그러나 `WP-R12.09/CPF-LOGTX`와 전체 CPF는 CLOSED가 아니다. DB3 vendor별
rollback-surviving logging, remote/multi-instance/Batch retry·UNKNOWN/reconcile, Browser, Performance,
Open Git Fresh Release, Fresh Replay와 QA acceptance가 여전히 `VERIFICATION_PENDING/NOT_EXECUTED`다.
다음 작업자는 이 Evidence를 재실행 대상으로 중복 개발하지 말고, 남은 scenario만 current Source에서
이어 닫는다.

### C-31. ADM 필수 Control Plane route 및 One-WAS File↔DB lineage current-source 재종결

**Current Product Source Identity:**
`0a94567d3826a6ef4a7c6994f6784f5a36015c37b4de9f4199e9f731ad4ea2ec`
(`contentSha1=c8179c6255f50caf64f67a20205e29da3a00fda3`). Git `master`/`HEAD`는
`19d536741481d6a239428bc64bbd6c39a133f63f`이며, 이 Identity는 Current Harness/Evidence metadata를
순환 방지를 위해 제외한다. Work-unit 실행 전후 Source Identity는 정확히 동일했다.

**Root Cause와 Source/Consumer 정렬:** ADM은 optional generated domain이 아닌 CPF Platform
Control Plane이다. `AdmIntegrationClosureConfiguration`의 top-level `enabled` switch를 제거했지만,
Consumer인 `AdmIntegrationClosureController`에 남은 같은 switch가 `enabled=false`에서 mandatory route를
다시 제거할 수 있었다. Controller switch, legacy top-level property와 profile YAML key를 제거했고,
configuration/controller/property/profile을 함께 검사하는 `verify_integration_closure_contract.py`와
configuration regression으로 negative mutation을 고정했다. Provider 구성 실패는 fail-closed이며
Controller 제거로 계약을 축소하지 않는다.

**실제 One-WAS runtime:**
`C:\Users\fly10\Downloads\CPF_WORKUNIT_ONE_WAS_20260904_140747\work-unit-result.json`
(`SHA-256=C2AC4639274B793B63CD6D9CFE1AAAC27571034166BE47D2F48D7A052EC00240`)

13/13 stage가 PASS했다: fresh MariaDB schema/seed, generated EXS·MBR bootstrap, registry seed,
One-WAS start, ADM bootstrap rotation, file log, DB policy, integrated correlation, ADM/MBW Runtime OpenAPI,
stop, verifier-owned DB cleanup, Source Identity after check. 총 141.8초였다.

대표 actual business-error 거래는 다음과 같다.

- `transactionId=20260904140924068MBWlogcor10000001`
- `traceId=e1a7e484099d4ec2bae784fd3c1a229d`
- `executionId=EX-e742f674-349f-491d-96e6-75c048491f56`
- `segmentId=20260904140924068MBWlogcor10000001-SEG-0004-97A6493A`, `parentSegmentId=null`,
  `depth=0`, root inbound이므로 `attempt=null`
- `instanceId=cpf-local-2ccc112ba520`, source/target SystemCode=`MBW`,
  operation=`MBW_AUTH_LOGIN`, status=`FAILURE/FAILED`, HTTP `401`, response=`ECPF990000`,
  message=`MCPF990000`, error=`ResponseStatusException`

`file-log-transaction.ndjson` 2행과 DB Summary query result 1행, ADM transaction log 1행, ADM Timeline
segment 1행이 위 transaction/trace/execution/segment를 동일하게 보유한다. File event에는 instance,
parent/depth, operation, status/response/message/error가 있고 DB Summary에는 same transaction/trace,
instance, MBW SystemCode와 status/error가 있다. `db-log-transaction.json`은 raw DB candidate와
case-insensitive canonical aliases를 모두 남긴다. 따라서 API projection transform이 실제 DB row를
버리고 count만 PASS시키는 결함도 재발하지 않는다. raw secret leak=0, recovery pending/quarantined/
terminal loss=0, fatal runtime marker=0도 같은 evidence에 있다.

**재발 방지 검증:**

- `verify-cpf-integrated-logging-closure.py` PASS
- isolated `pytest --basetemp` 11 PASS (`test_integrated_log_correlation_contract`, integrated logging closure,
  live runtime log read sharing)
- targeted integration gates 4 PASS
- `:apps:admin:test` 267 tests PASS
- Java 25 `aggregateQualityBuild publicationGate` 675 tasks, 4m53s PASS:
  `C:\Users\fly10\Downloads\CPF_ROOT_PUBLICATION_20260904_141230.log`

**정확한 상태:** One-WAS scoped mandatory route/logging/OpenAPI path는 physical PASS evidence로
`SOURCE_FIXED`이며 current exact source로 검증됐다. 하지만 **WP-ONE01 parent와 CPF 전체는 CLOSED가
아니다**. DB3 각 vendor rollback-surviving log, normal business transaction, remote A→B→C,
multi-instance, retry/UNKNOWN/reconcile, Browser, Performance, Open Git Fresh Release, Fresh Replay 및 QA
acceptance는 계속 `VERIFICATION_PENDING/NOT_EXECUTED`다. 다음 작업자는 이 closed subpath를 재개발하지
말고, 위 미검증 scenario를 Source → Consumer → Test → Runtime → Evidence로 이어 닫는다.

### C-32. Open Git 기본 Binary Release의 Windows/Linux Generator Matrix Root Cause 종결

**Current Product Source Identity:**
`0235f91d284132edbdd97c58bcd2a1c440bc9c42eff34d1ad4c5572e841deebd`
(`contentSha1=d88cbe165e3cdebaf2a9665ba4ff490c66112944`). Git `master`/`HEAD`는
`19d536741481d6a239428bc64bbd6c39a133f63f`이며 final Release의 Source Identity Before/After가 이 값으로
동일하다.

**실제 Root Cause:** 기본 `cpf release open-git build --profile binary`은 Public
`cpf-generator-cli`에 `windows-x64`와 `linux-x64`를 모두 요구하지만, Windows host에서 Linux artifact를
만드는 canonical consumer path가 없어 Stage 06/14에서 fail-closed됐다. fail-closed 정책은 맞지만 Docker
Linux/amd64가 실제로 준비된 현재 release host에서 official CLI가 matrix를 조립하지 못한 것은 Source/UX
결함이었다.

**Source/Contract 보완:** `cpf_open_git.py`는 Windows에서 검증된 external matrix가 없을 때 fresh
`cpf-release/work/generator-linux-matrix`에 Docker Linux/amd64 native PyInstaller build를 수행한다. Source는
read-only mount, output은 fresh work mount, container workdir는 `/src`로 고정되며 PyInstaller의
`binutils/objdump` 의존성도 명시 설치한다. 기존 publisher checksum/manifest verifier가 생성물을 다시
검증한다. Windows archive classifier 이름 변경, unverified copy, Linux host의 Windows binary 위조는 모두
허용하지 않는다. Product Requirement 21.3, Open Git work package/README, Artifact Catalog, CLI help은 같은
실제 계약으로 currentize했다.

**재발 방지:** Stage 06 isolated pytest 42 PASS. Regression은 Docker command의 read-only source/output
mount, `/src` workdir, `binutils`, pinned PyInstaller를 요구하고 Docker/valid Linux matrix 부재는
fail-closed한다. 기존 Publisher의 two-classifier checksum/manifest negative mutation도 그대로 유지한다.

**Actual Fresh Release Evidence:**
`C:\Users\fly10\Downloads\CPF_OPEN_GIT_BINARY_FINAL_20260904_150609.log`

```text
Framework Binary / Publication     2125 Gradle tasks, BUILD SUCCESSFUL
Stage 06 Generator Matrix          Windows native + Docker Linux/amd64 PASS
Stages 07–09 Public binary policy  PASS; source.jar=0; javadoc.jar=0
Stages 10–12 Projection/Fresh      Fresh clone + isolated workspace PASS
Stages 13–14 Git read-only/Status  PASS; result=VERIFIED
git add/commit/push                false / false / false
CPF_OPEN_GIT_BINARY_FINAL_EXIT=0
```

Final `cpf-generator-cli` archive 두 개의 manifest SHA-256, `.zip.sha256`, actual SHA-256는 일치한다.
`windows-x64`는 MZ executable을 Windows에서 `--help` exit 0으로, `linux-x64`는 ELF executable을 Docker
Linux에서 `--help` exit 0으로 실제 실행했다. 검증용 Windows extraction은 exact Temp path에서 삭제됐고,
final `cpf-release`에는 `binary-repository/`, `open-git/`, `reports/`, `logs/` 한 본만 남는다.

**정확한 상태:** Open Git binary Release/Windows-Linux Generator matrix subpath는 physical PASS evidence로
`SOURCE_FIXED`다. `WP-RL02`와 CPF 전체는 `VERIFICATION_PENDING`이다. 이 evidence는 Source Profile, public
`bootstrap → DB3 runtime transaction → stop/reset → fresh replay`, Browser, Performance, One-WAS 전체
scenario, DB3 vendor별 logging rollback/recovery, QA acceptance의 대체 근거가 아니다. 다음 작업자는 Stage 06을
재개발하지 말고 남은 current-source consumer/runtime scenario만 이어서 닫는다.

### C-33. Open Git Fresh Consumer의 실제 로그인/업무거래 Gate 미완결 발견

**검수 중 read-only로 확인한 Current Product Source Identity:**
`36b2a85b7a73226f84e69f24589fcb1c55cfba8ffc75314d29f47e0e23e580b3`
(`contentSha1=c377889a7e91ee71793a8b3d8ed9aedc8789cb14`, fileCount=8577).

**현재 Source/Consumer 사실:** 공개 Consumer Runtime 계약과 ADM/MBW/Backoffice Web target 및 production
bundle wiring은 추가됐다. Source lock 전의 targeted regression에서는 50 static tests PASS, Backoffice Web의
OpenAPI 96 operations/Vitest 6 PASS, 그리고 bootJar 안 `BOOT-INF/classes/static/mbw/` bundle 5 entries를
확인했다. 동시 currentization이 진행 중이므로 이 결과는 final exact-source Evidence가 아니며 Fresh Consumer
Runtime PASS로 승격하지 않는다.

**Root Cause:** `cpf bootstrap`은 local DB/Domain profile을 준비하지만 MBW 최초 운영자를 만들기 위한
`approval-token-file` + `password-file` + 사전 승인 레코드를 공식 Consumer 경로로 준비하지 않는다.
Backoffice의 현행 bootstrap은 의도적으로 owner-only secret file과 maker/checker 승인 token을 요구한다.
공개 README에도 MBW 최초 운영자 절차가 없다. 따라서 Fresh Clone 사용자는 `backoffice`와
`backoffice-web`을 기동해도 실제 업무 로그인을 완료할 수 없다.

새 `verify_open_git_consumer_runtime.py`도 이 결함을 아직 감춘다. ADM은 SPA/health만 확인하고 login POST를
수행하지 않으며, Backoffice는 가짜 credential POST가 5xx가 아닌지만 확인한다. HttpOnly BFF cookie를 받은
실제 로그인, CSRF token, BFF→MBW Authorization forwarding, 인증된 MBW 업무 API transaction은 검증하지 않는다.
이는 사용자가 요구한 `Fresh Clone → bootstrap → ADM login → MBW/Backoffice login → authenticated business
transaction → status/stop/cleanup` Gate와 불일치한다.

**정확한 상태:** `WP-R07.17` Open Git Consumer Runtime은 `VERIFICATION_PENDING`이다. existing Binary/
Generator Release PASS나 SPA bundle 존재는 이 Root Cause의 PASS evidence가 아니다. 다음 수정은 MBW의
maker/checker/secret-file security boundary를 약화시키지 않는 **local first-operator canonical provisioning
policy**를 Product Contract/Harness에 확정한 뒤, `bootstrap → provision → runtime → real login/CSRF/BFF→MBW
transaction → File/DB lineage → cleanup → Fresh Replay`와 negative mutation까지 연결해야 한다.

### C-34. Codex continuation — VS Code JDT output 보호와 INTERNAL Lifecycle engine 보정

**읽기 시작 기준:** Git `master`, `8ab6ad64e888892c8d43c07592a112157f5dec09`; 이미 존재하던 Claude
working change는 보존했다. Full Runtime/Release 실행 중이 아닌 상태에서만 아래 Source를 보정했다.

**VS Code/JDT Root Cause와 처리:** Java Language Server의 Gradle Build Server 경로는 named-pipe connection
실패 후 official Gradle Eclipse model을 무시하고 88개 project에 `bin/main`, `bin/test`, `bin/default`를
기본 output으로 넣었다. CPF Batch의 `bin/`은 launcher/config Product Source여서 Java workspace clean이
`cpf-batch/agent/bin/main` 및 `runtime-support/bin/main`의 tracked YAML/XML을 실제 삭제했다. 삭제된 Agent
3개 Source와 Claude의 `application-bat-runtime.yml` local/test secret change는 HEAD+직전 diff로 즉시 복구했다.

이 기록의 처음 보정은 `bin/*` Source 손상을 막았지만 JDT와 Gradle output을 함께 쓰는 중간 상태였다.
해당 중간 상태는 C-35에서 **폐기·대체**되었으며 다음 작업자가 `build/classes/java`를 JDT output으로 다시
사용해서는 안 된다.

**실측:** Root/Included Build의 `eclipseClasspath` generated model은 `build/classes/java/main`, `test`,
`default`만 출력했다. Java server restart(창 reload 아님) 뒤 JDT workspace 126 project에서
`bin/main|bin/test|bin/default = 0`, project marker directory `0`을 확인했다. Targeted static regression
`101 passed`와 `cpfVerifyIdeClasspathModel` PASS도 확인했다. 단, Independent Reviewer standard가 요구하는
Fresh exported `CPF_VSCODE_PROBLEMS_*.json`은 아직 없으므로 VS Code Error=0/Warning=0 final evidence나
Full Runtime precondition으로 승격하지 않는다.

**CLI Root Cause/처리 (CRF-49):** `cpf targets`/`cpf status platform` INTERNAL profile이 legacy
`cpf_local_runtime.py`로 위임되어 `invalid choice: targets` exit 2였다. `CpfCli` selector는 profile과
무관하게 `CpfBootstrap`으로 통일하고, Bootstrap root discovery는 Development Master의 runtime target
catalog도 인정하도록 보정했다. Java25 isolated runtime test와 consumer regression을 추가했다. Actual Windows
Java25 `cpf targets`, `cpf status platform`은 exit 0 PASS. Fresh Open Git Consumer lifecycle은 아직 수행하지
않았으므로 `CRF-49`와 `WP-CLI01`은 `SOURCE_FIXED_TARGETED_PENDING`/`진행` 상태다.

**다음 작업자 주의:** 이전 JDT가 남긴 untracked `bin` class/resource copies는 Product Source가 아니며 새
model은 더 만들지 않는다. tracked `cpf-batch/**/bin` Source를 generic delete/clean 대상으로 취급하지 말 것.
Fresh Problems JSON export → verifier PASS → source identity 고정 후에만 Open Git Consumer/Full Runtime을
시작한다. One-WAS logging/DB3/Browser/Performance는 이 기록으로 PASS 처리할 수 없다.

### C-35. Codex continuation — Open Git Release가 적발한 JDT/Gradle output race (CRF-50)

**실제 Release failure:** 2026-09-05 `cpf release open-git build --profile binary`는 Stage 05/14에서 FAIL했다
(16m19s; 886 actionable tasks, commit/push 미실행). ADM test는 93건 `NoClassDefFoundError`, Education test는
59개 source package/class를 못 찾았고, Domain-call MVC test는 `-parameters` 정보가 없는 class 때문에
`IllegalArgumentException`을 냈다. 이어서 IDE repair는 Backoffice Web `static/mbw/index.html` 및 messaging
schema `META-INF/cpf/runtime-capability.properties` duplicate archive entry도 적발했다.

**공통 Root Cause:** JDT가 `build/classes/java`를 Gradle과 공유하면서 Gradle test/processor output을 비동기로
교체·resource copy했다. Source는 존재하고 Gradle의 JavaCompile은 이미 `-parameters`를 사용했으므로 각 오류를
Controller/ADM/Education 기능 결함으로 개별 완화하면 안 됐다.

**Current Source 보정:** official Eclipse model은 Root, 모든 Java subproject, Included Build에서 JDT output을
`build/ide/classes/{main,test,default}`로만 지정한다. pre-isolation JDT가 남긴 class-output resource는
`resources/main`과 `Files.mismatch == -1`인 동일 copy만 archive 직전에 제거한다. class file 또는 byte가 다른
duplicate는 삭제/완화하지 않아 archive가 fail-closed 한다. Harness §26.7, IDE model/readiness gate, static
validator와 shared-output negative mutation이 이 계약을 강제한다. Java Language Server만 재기동했고 VS Code
창 reload는 하지 않았다.

**Targeted Evidence:** `test_cpf_vscode_classpath_output_contract.py` + IDE/resource policy suite **18 passed**;
`cpfVerifyIdeClasspathModel` 및 `cpfVerifyIdeClasspathReady` PASS; Java25
`:apps:backoffice-web:jar :internal:integration:http:test --rerun-tasks` PASS(1m43s),
`:apps:admin:test :apps:education:test --rerun-tasks` PASS(4m44s). Restart 뒤 physical JDT output은
`build/ide/classes`에 생성되고 prior `bin/main|bin/test|bin/default=0`, project marker=0을 확인했다.

**정확한 상태:** CRF-50은 `SOURCE_FIXED_TARGETED_PENDING`이다. 이 targeted PASS는 새로운 exact Source의
Open Git Stage 01–14, Fresh Consumer login/transaction, VS Code Fresh Problems JSON 0/0, DB3/One-WAS logging,
Browser/Performance/Full Runtime PASS가 아니다. 다음 단계는 이 Current Source 그대로 Release를 재실행하고
결과를 Consumer Runtime으로 이어가는 것이다.
