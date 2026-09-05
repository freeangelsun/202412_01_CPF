# Claude Collaboration Handoff (Current)

Codex 와 같은 Working Tree 에서 병행 작업한다. 서로의 변경을 되돌리지 않는다. 각 항목은 **증상 근거 /
직접 Root Cause / 수정 Source / 되돌릴 때 재발할 증상** 을 남긴다. Codex 기록은
`evidence/codex/current/CODEX_COLLABORATION_HANDOFF.md` 다.

---

## L-01. Public Runtime Lifecycle CLI — 전체 / 논리 Group / 개별 Target (WP-CLI01, Harness §38)

**사용자 Steering:** "CPF Public Runtime CLI / Group Lifecycle / Harness Canonicalization FINAL STEERING".
Source 수정에 그치지 않고 Harness Rule → Validator → Negative Mutation → Registry 까지 닫았다.

**Canonical authority:** `cpf-tools/runtime/cpf-runtime-target-catalog.json` 에
`runtimeGroups`(Group authority), `startOrderContract`, Runtime 별 `architectureRole` / `runtimeGroups` /
`dependsOn` / `publicLifecycle` / `buildSurface`, `dynamicRuntimes.primaryModule` /
`runtimeGroupsByModule` 를 추가했다. Group 은 metadata selector 로만 정의하고 대상 이름 목록을 두지 않는다.

**Source:**
- `cpf-tools/runtime/cli/java/CpfRuntimeTargets.java` — `Group` record, `groups()`, `select()`,
  `isGroup()`, `startOrder()`(dependsOn 위상정렬 + 순환 fail-closed), `moduleGroups()`,
  `moduleCapabilities(root)`(catalog 파생). Domain 대표 module 의 Target 이름은 Domain 이름 자체다.
- `cpf-tools/runtime/bootstrap/CpfBootstrap.java` — Group/전체 lifecycle, `runtimeGroupStatus`(pid 단독
  판정 금지, DEGRADED), `runtimeTargets`(GROUPS/RUNTIMES 표), `unknownSelector`(선택지 안내),
  capability 미지원 시 `UNSUPPORTED`, stop 은 기동 역순, group log 는 Target prefix.
- `cpf-tools/runtime/cli/java/CpfCli.java` — `cpf targets`, `cpf start|stop|restart|status|health|log
  <target|group>` 위치 인자. `stop`/`status` 는 selector 유무로 Workspace 명령과 갈린다.
  help 예제는 catalog 에서 만든다.

**되돌리면 재발할 증상:** 사용자가 Batch 구성요소 이름을 전부 외워야 하고, Generated Domain 을 만들 때마다
launcher/CLI 를 고쳐야 한다.

---

## L-02. Fresh Consumer Runtime Finding (CRF-45 ~ CRF-48, CRF-51)

원장: `current/CONSUMER_RUNTIME_FINDING_LEDGER.csv`. Finding 하나에 Targeted Test 하나, Negative Mutation
하나를 둔다. 44건을 하나로 축약하지 않는다.

- **CRF-45** `cpf bootstrap --run` 이 `:online:bootRun` 으로 Domain 을 띄워 상태 파일 pid 가 Gradle
  wrapper 였다 → `cpf stop` 이 실제 Runtime 을 못 멈춘다. `startRuntimes`/`buildDomainRuntimeJar` 로 수정.
- **CRF-46** 최초 운영자가 `password_change_required_yn='Y'` 로 생성되어 첫 로그인 흐름이 끊긴다.
  vendor 3종 `auth-bootstrap-operator.sql` 수정.
- **CRF-47** 공개 배포본 Linux 의 `bin/cpf-*.sh` 전부가 **배포되지 않는** `bin/cpf.sh` 로 위임해
  "No such file or directory" 로 즉시 실패했다. 실물(`cpf-release/open-git`)에서 재현 확인. 모든 wrapper 를
  canonical `bin/cpf` 위임 thin wrapper 로 교체하고 `cpf.sh` 의 자체 명령 해석을 제거했다.
- **CRF-48** `cpf-domain-new.*` / `cpf-domain-sync.*` 가 canonical 동사가 아닌 `domain new` 로 위임했다.
- **CRF-51** `cpf_open_git.py build` 가 01단계에서 `cpf-release` 전체를 지운 뒤 02단계에서 remote 전제조건을
  확인한다. remote 미설정 실행 한 번으로 유효한 Release 산출물(open-git, binary-repository)을 잃었다.
  전제조건 확인(`--skip-build` 거절, `canonical_remote`)을 삭제보다 앞으로 옮겼다.

---

## L-03. Harness 정본화

- **§38 Runtime Lifecycle CLI 계약** 신설. 3단계 제어, canonical authority 파생, Group/Target 이름 충돌
  금지, UNSUPPORTED / 부분 실패 노출 / DEGRADED, Windows-Linux parity, Public Source vs Binary-only
  경계, Profile 무차별, 하드코딩 금지, **사용자 Steering 반영 계약**(Rule+Validator+Mutation+Registry).
- **§36.2 (4)** 추가. 하나의 Build Tree 에는 하나의 Gradle project cache 만 둔다. IDE 전용
  `--project-cache-dir` 는 stale-output registry 를 두 벌 만들어 서로의 산출물을 지우며, 내부 개발환경에서
  VS Code `code 964` 재발의 기전이었다.

## L-04. Validator / Negative Mutation

- `cpf-tools/verification/tests/test_cpf_runtime_lifecycle_cli_contract.py` (41 tests). Group authority,
  dependency 무순환, Channel Front 기동 순서, Generated Domain 동적 발견(**실제 컴파일·실행 probe**),
  CLI 이름 분기/목록 복제 금지, README·help parity, Public Build vs Binary Runtime 경계, Registry 관계.
- `cpf-tools/verification/tests/test_cpf_public_consumer_regression_contract.py` — Finding 별 계약.
- Negative Mutation group `RUNTIME_CLI` 21건 + `STRENGTH` 54건 전부 FAIL-closed 확인.

## L-05. Registry

`WP-CLI01`(root_cause_key=CLI_GENERATOR)에 **병합**했다. 신규 WP 를 만들지 않았다.

---

## L-06. Release Asset 보존 / Open Git Fresh 재생성 (WP-RL01, Harness §39)

**사용자 Steering:** "Development Master Release Preservation + Open Git Clean/Fresh Regeneration".
핵심은 **두 축이 독립**이라는 것이다.

- `masterTracked` — Development Master Git 에 보존하는가
- `freshRegenerationRequired` / `releaseInputAuthority` — 다음 Fresh Release 의 입력이 되는가

`masterTracked=true` 가 `releaseInputAuthority=true` 를 뜻하지 않는다. generated/binary 라는 이유만으로
Master 보존을 막지 않고, Master 보존 여부로 공개 여부를 추론하지 않는다.

**Canonical authority:** `cpf-tools/release/open-git/open-git-surface-policy.json` 에
`releaseAssetPolicy`(4분류·4축·독립성 규칙·Clean Workspace·발행 경로·Gate 분리·Acceptance·Current-only)와
`releaseProducedAssets`(엔진이 만들어 내는 자산 9종) 추가.

**전수 분류:** `cpf-tools/release/open-git/report_release_asset_inventory.py` →
`current/RELEASE_ASSET_INVENTORY.csv` 96건 (CANONICAL 87 / TRACKED 6 / TRANSIENT 3).

**같이 고친 것**
- `.gitignore` 의 `*.zip` 일괄 제외를 transient 범위로 축소. Current Verified Generator 배포본까지
  차단하고 있었다(Steering §44).
- README Gate 가 `--target <name>` 형태만 인정해 canonical `cpf start <name>` README 를 거절했다.
  Gate 를 canonical 형식 수용 + catalog 파생으로 교정(대상 이름 배열 제거).
- PowerShell wrapper 의 `-Target → --target` 변환 계약을 값싼 Gate(계약 Test)로 선반영해 긴 Release
  반복을 막았다.

**미해결로 남긴 것 (사용자 판단 필요)**
`.gitignore:157` 의 `/cpf-release/` 가 Release 산출물 전체를 제외한다. 그래서 Acceptance A
("Master checkout 만으로 Current Verified Deliverable 확인")가 현재 거짓이다. 실측 용량과 대안을
제시한 뒤 사용자 결정에 따른다. 임의 Threshold 를 정하지 않는다(Steering §20/§46).

---

## L-07. Release Gate 의 대상 파생 (CRF-52, CRF-53)

Gate 가 대상 이름 배열을 들고 있으면 새 Runtime 이 검사 없이 배포되고, 이름이 바뀐 Runtime 때문에
Gate 가 영원히 실패한다. `cpf_open_git.py` 의 두 곳을 catalog 파생으로 바꿨다.

- `verify_public_readme` — 문서화 대상을 `("admin","backoffice","backoffice-web")` 배열이 아니라
  publicationClass=PUBLIC_RUNTIME + http-server 로 파생한다. `--target <name>` 뿐 아니라 canonical
  `cpf <verb> <name>` 형식도 인정한다.
- 필수 실행 표면 검사 — 같은 규칙으로 파생한다.

**owner 디렉터리 존재로 공개 여부를 판정하지 않는다.** ADM/Gateway 는 Source 가 아니라 Binary
Repository 로 배포되므로 그 조건에서는 영원히 검사 대상에서 빠진다.

이 확장이 드러낸 실제 결함:
- **CRF-52** `gateway`(8070/`GWY_SERVER_PORT`)와 `education`(8099/`EDU_SERVER_PORT`)이 배포되면서
  README 에 실행 방법이 없었다. 둘 다 문서화했다.
- **CRF-53** `local-runtime`/`local-batch`(cpf-tools 소유, 공개 배포본에 실리지 않음)가
  `PUBLIC_RUNTIME` 으로 표시되어 있었다. `INTERNAL_LOCAL_RUNTIME` / `publicLifecycle=false` 로 정정.

값싼 Gate 로 먼저 잡도록 계약 Test 에도 넣었다:
`test_every_public_http_runtime_is_documented`, `test_internal_runtime_is_not_published_as_public`,
`test_release_engine_never_carries_a_runtime_name_list`,
`test_powershell_wrappers_translate_the_expected_parameter`.
긴 Release 를 반복해서 같은 것을 알아내지 않는다.

**부수 정리:** 내 pytest 실행이 남긴 `__pycache__`/`.pytest_cache` 127건을 지워 Harness authority
gate 를 142 FAIL → 6 FAIL 로 낮췄다. 남은 6건은 Source Freeze 시 `currentize_source_identity` 로
해소되는 identity/count drift 다. 앞으로 pytest 는 `PYTHONDONTWRITEBYTECODE=1 -p no:cacheprovider` 로 돈다.

---

## L-08. Service Registry Provisioning (CRF-54, WP-ONE01, Harness §40)

**증상:** 공개 배포본에서 `cpf run` 이
`Runtime Agent service가 중앙 Registry에 등록되어 있지 않습니다: EXS` 로 실패한다. 사용자가
`cpf domain-new` 로 만든 Generated Domain 은 **영원히 기동할 수 없었다.**

**직접 Root Cause:** Platform seed(`OPS_SERVICE`)는 ADM/BAT/CEC/EDU/MBW 만 담고 있고, 사용자가 만든
Domain 의 service 를 등록하는 경로가 어디에도 없었다. Backoffice(MBW)는 seed 에 들어 있어서 우연히
통과했을 뿐이다. Fresh Consumer E2E 는 `cpf runtime start` 경로만 돌았기 때문에 이 결함이 드러나지 않았다.

**사용자 결정(정본):** 등록의 **실행 주체는 `cpf bootstrap` 의 Platform DB provisioning lifecycle**,
등록 **규칙의 Owner 는 canonical Service Registry provisioning 계약**이다. Generator/ADM/Runtime 어느
한 곳의 임시 부가기능으로 만들지 않는다.

| 대상 | 정본 |
| --- | --- |
| 등록 규칙 | `cpf-tools/db/canonical/service-registry-provisioning.json` |
| SQL | `cpf-tools/db/vendor/{vendor}/runtime/cpf/repository/service-registry-*.sql` (3 vendor) |
| 실행 | `CpfBootstrap#reconcileServiceRegistry` (Platform DB 적용 직후) |
| 공개 투영 | `config/service-registry-provisioning.json`, `deploy/local/db/vendor/**` |

**지켜야 할 것**
- Domain 이름/SystemCode 를 계약에도 코드에도 복제하지 않는다. Workspace 발견 Domain 전체에 같은 규칙.
- `service_id` / `owner_module_code` 는 `cpf.domain.systemCode` 를 그대로 쓴다(transform NONE).
  truncation/inference/fallback/문자열 조합 금지.
- `validate → reconcile → fail-closed`. 없으면 등록, 같으면 idempotent, 소유 충돌과 운영자 비활성
  (`use_yn='N'`)은 **덮어쓰지 않고 멈춘다.** 표시용 값(`service_name`/`description`)도 덮어쓰지 않는다.
- **Runtime 자가 등록 금지.** ADM 수동 등록과 `domain-new` 즉시 등록은 Golden Path 가 아니다.
- local/dev/stg/test/prod 가 같은 lifecycle 을 쓴다(`profileInvariant`).

**Validator/Mutation:** `test_cpf_service_registry_provisioning_contract.py` (22) +
negative mutation group `SERVICE_REGISTRY` (19) FAIL-closed.

**부수 보완:** `cpf run` 이 중간 실패하면 이미 뜬 Runtime 이 남는다. 이제
`CPF_RUNTIME=PARTIAL_START failed=<code> started=<n> next=cpf stop` 을 출력한다. 남은 프로세스를
모른 채 다음 기동에서 포트 충돌만 보게 되는 상황을 막는다.

---

## L-09. Release 산출물의 Master 보존 범위 (CRF-55) — 사용자 결정 대기

`.gitignore` 의 `/cpf-release/` 한 줄이 Release 산출물 **전체**를 제외하고 있었다. 그래서 Harness §39
Acceptance A("Master checkout 만으로 Current Verified Deliverable 을 확인할 수 있다")가 거짓이었다.
일괄 제외를 부류별 제외로 바꿨다.

```
/cpf-release/work/        TRANSIENT
/cpf-release/logs/        TRANSIENT
/cpf-release/open-git/    projection 결과 (Canonical Source 에서 재생성)
```

`binary-repository/` 와 `reports/` 는 더 이상 ignore 하지 않는다. 다만 **실측 결과 용량 예외 결정이
필요**하므로 아직 tracking 하지 않았다(아무것도 commit 하지 않았다).

| 실측 | 값 |
| --- | --- |
| binary-repository | 699 MB / 125 files |
| 100MB 초과(GitHub 하드제한) | `cpf-admin-1.0.0.jar` 111.0MB, `cpf-local-runtime-1.0.0-local-web.jar` 111.3MB |
| 50~100MB | 5개 367MB |
| 10MB 이하 소형(POM/checksum/manifest) | 19.8MB |
| 현재 `.git` | 183 MB |

권장안은 소형 metadata + `reports/**`(약 25MB)만 tracking 하고 fat-jar 7종은
`UNTRACKED_RELEASE_RESULT` + `trackingExceptionReason` 으로 남기는 것이다. **승인 전에는 예외를
확정하지 않는다**(Steering §46).

---

## L-10. Dependency Ownership 분석 (CRF-56 / 57 / 58) — Codex 가 LFS Writer 인 동안 수행

**역할 분담:** Codex 가 Git LFS 구현(.gitattributes, verify_release_lfs_contract.py, policy/engine)의
Source Writer 였다. Harness §93/§94 에 따라 나는 Writer 를 피하고 분석·검증을 맡았다. Codex 의 LFS
validator 는 PASS(LFS 9 / runtime 9 / manifest 124)이고 내 asset 계약과 충돌 없음을 먼저 확인했다.

**LFS 채택은 Dependency 품질 검증을 대체하지 않는다**(Steering §26). 새 분석기
`cpf-tools/release/open-git/report_runtime_dependency_ownership.py` 로 9개 Runtime 실행물 내부를 재고
분석했다. 결과: `current/RUNTIME_DEPENDENCY_OWNERSHIP.json`.

| ID | 판정 | 내용 |
| --- | --- | --- |
| **CRF-56** | **수정함** | compile 전용 `spring-boot-configuration-processor`(146KB)가 production Runtime 6종에 배포. CPF 선언은 전부 `annotationProcessor` 로 옳았고, 외부 `spring-cloud-circuitbreaker-resilience4j:5.0.2` 가 runtime 스코프로 흘린 것. `cpf-starters/integration/resilience/build.gradle` 에서 해당 module 만 exclude. 수정 후 runtimeClasspath 에서 소멸 확인 |
| **CRF-57** | **결함 아님** | Jackson 2(2.21.4)+3(3.1.4) 공존은 `CpfJackson2AutoConfiguration` 이 명시한 의도된 계약. CPF 소스 153파일이 Jackson 2 사용. `REQUIRED_BY_PRODUCT_CONTRACT` 로 종결 |
| **CRF-58** | **보존 결정** | HTTP/3 QUIC native 5플랫폼 11.68MB/Runtime × 6. CPF 는 HTTP/3 미설정이나, LFS 로 크기 제약이 해소됐고 Steering §27 이 크기를 이유로 한 제거를 금지한다. upstream opt-in 기능이므로 근거와 함께 보존 |

**지켜야 할 판정 규칙 (Harness §39.6.1 로 정본화)**
1. `grep` 결과로 삭제하지 않는다. 유입 경로는 `dependencyInsight` 로 특정한다.
2. 판정은 `SUSPECT` 까지. 제거는 Source Owner 결정.
3. **크기를 이유로 필요한 Dependency 를 제거하지 않는다.** LFS 가 있으므로 GitHub 제한은
   Runtime Architecture 의 목표값이 아니다.
4. 근거가 확인된 공존은 결함이 아니다.

**주의:** `report_runtime_dependency_ownership.py` 는 측정 전용이다. Artifact/Source 를 바꾸지 않는다
(계약 `test_tool_measures_and_never_deletes` 가 강제).

**아직 안 된 것:** CRF-56 수정은 Source 단계까지다. 실행물 반영은 다음 Fresh Release 에서 확인해야
한다(기존 실행물은 수정 전 빌드). 분석기를 다시 돌리면 그때 `wrongScopeDev` 가 비어야 한다.

---

## L-11 CRF-59 — 공개 저장소 LFS scope 가 적용되지 않았다 (False Green)

**증상.** Open Git 공개 저장소에서 Runtime 실행물 9종이 전부 LFS 대상이 아니었다.

```
$ git check-attr filter -- binary-repository/com/cpf/runtime/cpf-admin/1.0.0/cpf-admin-1.0.0.jar
binary-repository/com/cpf/runtime/cpf-admin/1.0.0/cpf-admin-1.0.0.jar: filter: unspecified
```

`cpf-admin-1.0.0.jar` 116,214,604 byte(110.8MiB), `cpf-local-runtime-1.0.0-local-web.jar`
116,516,941 byte(111.1MiB). GitHub 파일당 한도 100MiB 초과. **push 하면 거부된다.**
그런데 Release 는 `VERIFIED`, LFS 검증기는 `PASS` 였다.

**직접 원인.** `.gitattributes` 의 LFS 패턴이 `cpf-release/binary-repository/...` 로 고정돼 있고,
공개 저장소로 그대로 복사됐다. 공개 저장소의 Binary Repository 는 checkout 최상위의
`binary-repository/...` 이므로 어떤 패턴도 매칭되지 않는다.

**왜 검증을 통과했나.** 검증기가 `.gitattributes` **문자열**이 정책 template 과 같은지만 봤다.
문자열은 양쪽 다 `cpf-release/binary-repository/...` 라 일치했다. git 이 실제로 filter 를
적용하는지는 묻지 않았다.

**수정.**

| 대상 | 내용 |
| --- | --- |
| `open-git-surface-policy.json` | `gitAttributesPathTemplate` → `binaryRepositoryRelativePathTemplate` (저장소 배치 독립 단일 권위), `regularGitTransportLimitBytes` + 소유 선언, failClosedCodes 2건 |
| `cpf_open_git.py` | `project_lfs_attributes` (투영본 scope 재작성), `master_attribute_prefix`, Fresh Clone 적용 효과 fail-closed, push preflight LFS gate |
| `verify_release_lfs_contract.py` | `resolve_attribute_prefix`, `verify_attribute_effect`(git check-attr), `verify_transport_limit`(git ls-files 기준) |

**실측.** 수정 후 공개 투영본 `attributeEffect={verified:true,artifactCount:9}`,
`transportLimit={limitBytes:104857600,overLimitOnLfs:2,verified:true}`. Master 측은
`attributePrefix=cpf-release/binary-repository` 로 동일 통과.

**되돌리면 재발할 증상.** Release VERIFIED / push REJECTED 조합. 45분짜리 Release 를 마친 뒤
push 단계에서야 거부되고, 검증기는 계속 PASS 를 낸다.

**지켜야 할 규칙 (Harness §39.2 Rule 8-1/8-2/8-3 으로 정본화)**
1. LFS 경로 접두사는 저장소마다 다르다. 한쪽 경로를 template 에 박고 복사하지 마라.
2. `.gitattributes` 문자열 일치는 적용 근거가 아니다. Git Work Tree 에서는 `git check-attr` 로
   확인하고, 확인하지 못한 상태는 SKIP 이 아니라 **실패**다.
3. 외부 전송 한도는 정책이 선언하고 push 전에 검사한다. 검사 대상은
   `git ls-files --cached --others --exclude-standard` 가 보는 실제 전송 대상이다.
4. 한도 초과는 **Dependency 를 지우라는 뜻이 아니라 LFS 로 옮기라는 뜻**이다(§39.6.1과 동일).

## L-12 CRF-60 — 불필요한 deprecation 억제

`CpfScgPrimaryRouteConfigurationTest` 의 `@SuppressWarnings("deprecation")` 제거.
spring-webmvc 7.0.8 의 `ServerRequest.java` 에 `@Deprecated` 가 0건이다. upstream 이 deprecation 을
해제했는데 억제만 남아 JDT 가 `Unnecessary @SuppressWarnings` 경고를 냈다.

## L-13 VS Code — 구버전 language server 좀비

`init.gradle does not exist` 오류의 실체는 Product 결함이 아니었다. redhat.java 확장이
1.55.0 → 1.56.0 으로 올라가면서 **1.55.0 language server(PID 42420)가 종료되지 않고 남았고**,
자기 globalStorage(`redhat.java/1.55.0/config_win`)는 삭제된 상태로 1.56.0 서버와 같은 JDT
workspace 를 물고 있었다. 좀비 프로세스를 종료해야 해소된다. 확장 업데이트 후 Problems 에
없어진 경로가 보이면 **먼저 language server 프로세스 목록을 확인**하라.

---

## 다음 작업자 주의

1. Runtime Group/Target 목록을 CLI Source, launcher, README, help 에 복제하지 마라. catalog 가 정본이다.
2. `bin/cpf-*.{sh,ps1}` 은 위임 전용이다. 자체 명령 해석을 넣으면 OS 사이 의미가 갈라진다.
3. `cpf_open_git.py build` 는 `CPF_OPEN_GIT_REMOTE` 가 필요하다. 정본 값은 Harness 증적에 있다.
4. Fresh Consumer Runtime Evidence 미수집: CRF-45 / CRF-47 / CRF-48 / CRF-51 은
   `SOURCE_FIXED_TARGETED_PENDING` 이다. Consumer E2E 1회로 함께 회수한다.
5. Git Commit/Push/Tag/Release 는 사용자 승인 없이 수행하지 않는다.
