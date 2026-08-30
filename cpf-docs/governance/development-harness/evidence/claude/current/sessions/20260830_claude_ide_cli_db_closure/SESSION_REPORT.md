# Claude Session Report — `20260830_claude_ide_cli_db_closure`

- **sessionKey**: `20260830_claude_ide_cli_db_closure`
- **agent**: claude
- **작성 목적**: 정비 중인 Current Harness 에 결과를 정확히 투영하기 위한 실행 근거. 이 문서는 정본이 아니다.
- **공용 Harness 취급**: 본 세션 후반부터 Common/Feature Rule, `CURRENT_WORK_ITEM_REGISTRY`,
  Requirement/Status 공용 원장, Report Schema, Final Gate 규칙은 수정하지 않았다.
  세션 전반부에 이미 반영한 공용 원장 변경 내역은 §9 에 그대로 공개한다.
- **환경**: Windows 10.0.26200.9168 / JDK 25.0.3 Temurin / Gradle 9.1.0 / Python 3.13 /
  openssl 3.5.4 / cryptography 50.0.1 / redhat.java 1.55.0 / vscjava.vscode-gradle 3.18.0
- **Source Identity (세션 종료 시점)**: `90e4890dd4630ea2683d3b90223a2b8fd9a739f7444adeaeb3ee9d3d61e049a2`
  / sha1 `d8bfabf350525dc9e142517e98220db0a57cbe2e` / 8454 files / 46,158,807 bytes

---

## WP-R02.07 — VS Code Buildship/JDT Build Path

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-R02.07 (`IDE-BUILD-PATH-CYCLE`), 실행 WP-B02 |
| 원 Requirement | Fresh VS Code/Buildship Import 에서 전체 Workspace Java Error=0 / Warning=0 |
| Root Cause | Gradle Build Server 가 in-build project dependency 를 Eclipse project reference 와 **절대경로 class-folder library** 로 이중 등록한다. `java-library` 는 compile-avoidance 로 jar 가 아닌 classes 디렉터리로 해석되므로 JDT 가 그 class folder 의 물리적 존재를 강제한다. 경로가 `build/` 하위라 `clean` 이 지우고, 재생성돼도 JDT 가 외부 절대경로 변경을 자동 인지하지 않는다. workspace 실측 88 project / 540 참조 |
| 변경 Source | `cpf-tools/build/cpf-root-conventions.gradle`, `cpf-tools/verification/tests/test_cpf_vscode_classpath_output_contract.py` |
| Consumer/호출경로 | 86 Gradle Java project / 88 JDT project 전수. `cpfPrepareIdeClasspath` 호출경로 실측: `run-cpf-local-full-validation.ps1` 2곳뿐이며 **Gradle lifecycle 미연결, `.vscode/tasks.json` 부재, Build Server 미인지**. clean 후 실제 복구 주체는 VS Code Gradle Build Server 의 자동 컴파일(`java.configuration.updateBuildConfiguration: automatic`)이며, 오늘 11:05 build output 생성이 제 첫 Gradle 실행(11:20)보다 앞선 것이 그 증거다 |
| 영향범위 | admin·core·common·starters·gateway·backoffice·education·batch·member·external 전 범위 |
| 실행 명령 | `gradlew compileJava compileTestJava --continue --stacktrace` / `gradlew build --continue --stacktrace` / `gradlew clean` / `gradlew cpfPrepareIdeClasspath cpfVerifyIdeClasspathModel --stacktrace` |
| Exit Code | 0 (전 명령) |
| Test/Runtime 결과 | Java25 build 593 tasks BUILD SUCCESSFUL, compiler warning 0. `CPF_IDE_CLASSPATH_MODEL=PASS javaProjects=86 scope=gradle-canonical-compile-output`. JDT class-folder library 540건 중 누락 **0** |
| Regression/Side Effect | Messaging/JMS canonical type 5종 단일 owner, duplicate 0. `.cpf-ide` 재생성 0 (fail-closed 검증). 신규 하드코딩 0 |
| Evidence | `../../../devgpt/current/vscode-ide-classpath-clean-replay/RESULT.md`, `JDT_CLASSPATH_MODEL.json`, `GRADLE_FULL_BUILD_JAVA25.log`, `GRADLE_IDE_OUTPUT_CONTRACT.log` |
| 완료/미완료 사유 | **SOURCE_FIXED / VERIFICATION_PENDING.** 동일 VS Code 세션에서 Gradle Refresh → Java Reload 후 전체 Workspace Problems 실측이 미실행 |
| 재실행 조건 | `Gradle: Refresh Gradle Project` → `Java: Reload Projects` → (필요 시) `Java: Restart Language Server` 후 Problems 전량 재수집. 이어서 clean → 준비 → Refresh/Reload Clean Replay |

**부가 결정**: 이전 세션이 도입한 project-local `.cpf-ide/main` IDE 전용 output 계약을 전면 제거했다.
실측 결과 `.cpf-ide/main` 을 lib 로 참조하는 JDT entry 는 **0건**이어서 실제 오류 경로를 덮지 못하면서
제품 Source Tree 에 IDE 종속 generated directory 만 남기고 있었다. 86개 디렉터리를 제거했고
`.gitignore` 항목도 원복했다.

---

## WP-R03.05 — Harness Evidence Identity (canonical registry 변조)

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-R03.05 (`HARNESS-EVIDENCE-IDENTITY;RT-155`) |
| 원 Requirement | Source State After PASS 인데 Managed State After 가 FAIL 하는 원인 제거 |
| Root Cause | verification gate 4종이 canonical governance registry `LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl` 를 `--evidence` **기본 출력 대상**으로 사용해, 단독 실행 시 tracked 정본을 pretty JSON 으로 덮어썼다. legacy evidence 144 레코드가 소실되고 `NXT2-REDIS-001` 리포트가 그 자리를 점유한 상태였다 |
| 변경 Source | `verify_annotation_runtime_consumer.py`, `verify_redis_valkey_provider_currentization.py`, `verify_business_framework_crosscut.py`, `run_nxt3_final_all.py`, `test_nxt3_local_build_isolation.py`, `LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl` |
| Consumer/호출경로 | `run_nxt3_final_all.py` 는 child 에 temp dir 을 명시 전달하는 올바른 격리 설계를 이미 갖고 있어 runner 경유는 안전했다. **단독 실행 경로만** 정본을 파괴했다. `validate_development_harness.py` 가 이 registry 를 JSONL 로 파싱한다 |
| 영향범위 | governance `current/` 를 출력 대상으로 쓰는 도구 전수 재검색 → 추가 사례 **0**. Repository 전체 `.jsonl` 1개, 형식 invalid **0** |
| 실행 명령 | `pytest cpf-tools/verification/tests/ cpf-tools/release/open-git/tests/ -q`, `validate_development_harness.py` |
| Exit Code | 0 |
| Test/Runtime 결과 | 격리 test 2 passed(강화 후), verification+open-git **148 passed / 1 skipped**, Harness validator **PASS** (REQUIREMENTS=218 WORK_ITEMS=410 ROLE_ROWS=1230 MIGRATIONS=265), registry records=144 / invalid 0 / authority violation 0 |
| Regression/Side Effect | `git restore/reset/clean` 미사용. 해당 파일 1개만 최소 수정. 덮어쓴 리포트는 `OVERWRITTEN_NXT2_REDIS_001_REPORT.json` 으로 보존 |
| Evidence | `../../../devgpt/current/vscode-ide-classpath-clean-replay/HARNESS_EVIDENCE_IDENTITY_REPAIR.md` |
| 완료/미완료 사유 | **SOURCE_FIXED + Test PASS.** Managed State After 재실행은 Full Runtime 경로에서 수행 |
| 재실행 조건 | verification gate 의 evidence 출력 경로 변경 시 |

**false-green 동반 제거**: `test_nxt3_local_build_isolation` 의 두 상수가 동일 경로라 dict 가 1개로
축약되었고, `--evidence` 를 항상 지정해 실행하여 정본 오염 경로를 전혀 검사하지 않았다.
단독 실행 후 registry sha 불변을 검사하는 assert 를 추가했다.

---

## WP-R07.13 — Windows CLI Launcher (`cpf.cmd`)

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-R07.13 (`REL-CLI-CROSSPLATFORM`) |
| 원 Requirement | 공식 CLI 표면이 Windows/Linux 에서 동일하게 동작 |
| Root Cause | `cpf.cmd` 의 `for /f` 가 명령을 `cmd /c` 로 실행하는데 명령이 따옴표로 시작해 `cmd /c` 따옴표 제거 규칙에 걸려 파싱 실패. Java 25 탐지가 **항상** 실패(`CPF-CLI-JAVA-VERSION`, exit 69)했다. `cpf.ps1` 은 `Join-Path` 로 정상 동작해 결함이 가려져 있었다 |
| 변경 Source | `cpf-tools/runtime/cli/cpf.cmd` (`call` 접두 1줄 + 근거 주석) |
| Consumer/호출경로 | generator verification 전 경로, `test_cpf_unified_cli_contract`, Open Git bootstrap 등 Windows 에서 CLI 를 호출하는 모든 소비자 |
| 영향범위 | pytest **9건** 연쇄 FAIL 의 공통 원인 (generator 8 + CLI cross-platform 1) |
| 실행 명령 | `cmd /d /c cpf-tools\runtime\cli\cpf.cmd version` |
| Exit Code | 0 |
| Test/Runtime 결과 | `CPF_FRAMEWORK_VERSION=1.0.0-SNAPSHOT` / `JAVA_VERSION=25.0.3+9-LTS` 정상 출력. generator suite **58 passed / 6 subtests**, CLI contract **4 passed** |
| Regression/Side Effect | `JAVA_HOME` 트레일링 백슬래시 환경에서도 정상(초기 가설이었으나 실험으로 기각 후 재조사). ASCII/CRLF 유지 |
| Evidence | `../../../devgpt/current/vscode-ide-classpath-clean-replay/PYTEST_FINDING_CLOSURE.md` §3.1 |
| 완료/미완료 사유 | **CLOSED** — Source 수정 + 실제 실행 + 소비자 test PASS |
| 재실행 조건 | `cpf.cmd` 또는 Java 탐지 로직 변경 시 |

동일 WP 에서 `test_verify_cpf_javadoc_coverage` fixture 의 false-green 도 종결했다
(negative test 가 javadoc 누락이 아니라 publication policy 부재로 통과하던 문제, 3 passed).

---

## WP-R07.14 — Unified CLI / Entrypoint Inventory

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-R07.14 (`TOOL-UNIFIED-CLI`) |
| 원 Requirement | canonical entrypoint inventory 가 실제 Repository 표면과 정확히 일치 |
| Root Cause | ① inventory CSV stale (실존 entrypoint 15건 미등록) ② `pytest-basetemp`/`generated/pytest` 에 누적된 `CpfCli.java` 사본 6개가 canonical owner 스캔을 오염 ③ bootstrap jar 내장 `sourceIdentitySha256` 이 현재 Source Identity 와 불일치 |
| 변경 Source | `cpf-tools/runtime/cli/contracts/cpf-tool-entrypoint-inventory.csv`(재생성), `cpf-tools/runtime/cli/lib/cpf-cli.jar`(재빌드) |
| Consumer/호출경로 | `CpfCli.internalBootstrap` 이 inventory 생성기를 호출하는 공식 흐름 |
| 영향범위 | untracked garbage 약 16MB 제거 (`work/evidence/generated/pytest-basetemp` 5.4M, `platform/current/generated/pytest` 11M) |
| 실행 명령 | `build-cpf-tool-entrypoint-inventory.py --root .`, `build-cpf-cli.py --root .` |
| Exit Code | 0 |
| Test/Runtime 결과 | inventory entries=1021 (duplicate 0, dead 0), test 2 passed. CLI contract 4 passed |
| Regression/Side Effect | Repository 내 `CpfCli.java` 1개로 정규화. jar 는 Source Identity 계산에서 제외되어 non-circular |
| Evidence | `PYTEST_FINDING_CLOSURE.md` §2 |
| 완료/미완료 사유 | **CLOSED** |
| 재실행 조건 | entrypoint 추가/삭제 또는 Source 변경 후 jar 재빌드 필요 시 |

---

## WP-R10.12 — DB Vendor Lifecycle / refDB Garbage / Common Cache 계약

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-R10.12 (`P0-DB-PHYSICAL-CANONICAL-SET`) |
| 원 Requirement | official vendor 3종 lifecycle readiness 및 CMN durable cache 계약 충족 |
| Root Cause | ① 폐기된 refDB lineage 의 **빈 디렉터리 6개(untracked)** 가 historical pack 검사에서 `.sql` 0개로 잡혀 postgresql/oracle 을 `incomplete lifecycle` 로 오판정 ② `cpf-backup-crypto.py` 의 `cryptography` 의존성이 어떤 requirements 에도 미선언 ③ `test_cmn_code_message_durable_cache` 가 service 자체 `refresh()` 를 요구했으나 현재 정본은 중앙 `CpfCommonCacheRefresher` 소유 |
| 변경 Source | 빈 refDB 디렉터리 6개 제거, `cpf-tools/db/tools/requirements.txt`(신규), `cpf-tools/db/tests/test_cmn_code_message_durable_cache.py` |
| Consumer/호출경로 | `SpringCpfCommonCacheRefresher.SNAPSHOT_CACHES` 가 `codeCache` 를 직접 `clear()` 한다. `JdbcCpfCodeService.refresh()` 는 **호출처 0건**으로 dead code 임을 실측 확인 |
| 영향범위 | mariadb/postgresql/oracle vendor pack, DB3 install/migration/rollback, CMN code/message cache |
| 실행 명령 | `pwsh -File cpf-tools/db/verification/check-official-db-vendor-readiness.ps1 -Root .`, `pip install -r cpf-tools/db/tools/requirements.txt`, 관련 pytest |
| Exit Code | 0 |
| Test/Runtime 결과 | vendor readiness **exit 0** (vendors=3, tables=231, seeds=142). `test_admin_data_safety_gate_contract` 3 passed, `test_cpf_backup_crypto` 5 passed/1 skipped, durable cache + vscode regression **7 passed** |
| Regression/Side Effect | refDB 물리 잔재 **0**, 제품 Source/Contract 내 참조 **0**. governance 원장의 refDB 참조는 폐기 이력 증명이므로 보존 |
| Evidence | `PYTEST_FINDING_CLOSURE.md` §4 |
| 완료/미완료 사유 | **CLOSED** (정적 계약 범위). DB3 물리 Runtime 은 Full Runtime 단계 소관 |
| 재실행 조건 | vendor pack 구조 변경 또는 cache 소유 구조 변경 시 |

**계약 충돌 종결**: `test_cmn_code_message_durable_cache` 는 `refresh()` 존재를,
`test_cpf_vscode_problems` 는 동일 파일의 `void refresh()` 부재를 요구해 **동시 만족 불가**였다.
호출처 실측(0건)으로 dead code 임을 확인해 vscode 회귀 방지 쪽을 정본으로 채택하고,
durable cache test 를 중앙 refresher 구조 검증으로 현행화했다.

---

## WP-RL02 / Open Git — Stage 06 Generator Distribution

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-RL02 (Open Git Actual Fresh Release 14/14) |
| 원 Requirement | `cpf release open-git build --profile binary` 14/14 실제 PASS |
| Root Cause | Stage 조건 결함이 **아님**. `REQUIRED_GENERATOR_CLASSIFIERS = ("windows-x64","linux-x64")` 이고 release driver 는 cross-OS artifact 를 fabricate/substitute 하지 않는 fail-closed 설계다. **현재 Windows native 실행환경만으로는 `linux-x64` 를 생성할 수 없다** |
| 변경 Source | 없음 (Contract 약화·classifier 대체 금지 준수) |
| Consumer/호출경로 | `CpfCli.internalRelease` → `cpf_open_git.py` → `publish_generator_distributions` → Stage 07/09 재검증 |
| 영향범위 | Public JAR/POM/BOM/SBOM/Generator/Open Git ZIP |
| 실행 명령 | `cpf-tools\runtime\cli\cpf.ps1 release open-git build --profile binary` |
| Exit Code | 1 (Stage 06/14) |
| Test/Runtime 결과 | Gradle 단계 **BUILD SUCCESSFUL in 7m 10s / 864 actionable tasks**. Stage **05/14** 통과. `commitExecuted=false, pushExecuted=false` |
| Regression/Side Effect | Public Leakage **0** |
| Evidence | `../../../devgpt/current/vscode-ide-classpath-clean-replay/OPEN_GIT_STAGE06_CONTRACT.md` (입력 계약 12항목 Source/Test 확정 포함) |
| 완료/미완료 사유 | **BLOCKED_EXTERNAL** — linux-x64 native artifact 부재. PyInstaller 설치만으로는 해결되지 않음(prebuilt matrix 완비 시 PyInstaller 자체가 불필요함을 계약 test 주석이 명시) |
| 재실행 조건 | windows-x64 + linux-x64 를 하나의 flat directory 에 모아 `--generator-artifacts <DIR>` 로 주입 후 Stage 01~14 전체 재실행 |

---

## Full Runtime — 진입 차단 상태

| 항목 | 내용 |
| --- | --- |
| 원 Requirement | 최대강도 Full Runtime Validation |
| 현재 상태 | **NOT_EXECUTED** |
| 차단 사유 | `run-cpf-required-full-runtime-validation.ps1:59-60` 이 Fresh VS Code Problems JSON 을 필수 입력으로 요구하고 `verify-cpf-vscode-problems.py` 가 severity 8/4 를 세어 하나라도 있으면 `throw` 하는 **hard gate**. VS Code 0/0 확정 전에는 Runtime 시작 자체가 불가능 |
| 판정 | Runtime FAIL 이 아니라 **선행 IDE/Buildship Acceptance 미충족** |
| 재실행 조건 | WP-R02.07 의 동일 세션 0/0 + Clean Replay 0/0 → Fresh Problems JSON 생성 → 해당 JSON 으로 Full Runtime 실행 |

---

## HARNESS_FINDING

1. **`ROLE_LEDGER_COVERAGE` 계약** — `validate_development_harness.py` 는
   `(work_item × contract-registry.roles)` 집합의 **정확한 일치**를 요구한다. 계약에 없는 role
   (예: `DEVELOPER`) 로 행을 추가하면 즉시 FAIL 한다. 세션 중 실제로 유발했고 canonical role
   (`DEVGPT`/`INDEPENDENT_REVIEWER`/`QA`) 행 갱신으로 정정했다. Report Schema 정비 시 role 확장
   가능 여부를 명확히 규정할 필요가 있다.
2. **Evidence 파일 선행 존재 요구** — `TEST_EVIDENCE_MISSING` 검사가 ledger 기재 시점에 Evidence
   파일 존재를 요구한다. ledger 를 먼저 쓰면 FAIL 하므로 기재 순서 규칙이 Schema 에 명시되면 좋다.
3. **`CONTROL_CHAR` 검사** — Evidence 문서에 제어문자가 들어가면 FAIL 한다. 세션 중 `\b` 가
   백스페이스(0x08)로 기록되어 유발했고 제거했다.

위 3건은 Harness 구조 자체에 대한 관찰이며 임의 재설계하지 않았다.

---

## 9. 세션 전반부에 반영한 공용 원장 변경 (투영 대상)

동시작업 Steering 수신 **이전**에 아래 공용 원장을 이미 수정했다. 되돌리면 오히려 충돌하므로
그대로 두고 여기에 공개한다. Harness 정비 완료 후 이 목록 기준으로 재투영·정정하면 된다.

| 파일 | 변경 내용 |
| --- | --- |
| `current/CURRENT_WORK_ITEM_REGISTRY.csv` | WP-R02.07 / WP-R03.05 / WP-R07.13 의 `current_observation` 을 확정 Root Cause 로 갱신 |
| `current/ROLE_EXECUTION_LEDGER.csv` | WP-R02.07·WP-R03.05 의 **DEVGPT 행 갱신**(행 추가 없음). 잘못 추가했던 `DEVELOPER` 행은 제거 완료 |
| `current/TEST_EXECUTION_LEDGER.csv` | WP-R02.07 3행, WP-R03.05 2행, WP-R07.13 1행, WP-R10.12 1행, Open Git 1행 추가. 실수로 삭제한 기존 2행은 원본에서 복원 완료 |
| `current/CHANGE_MANIFEST.csv` | 이번 세션 변경 파일 전건 등록 (총 286행) |
| `SOURCE_IDENTITY.json`, `current/SOURCE_IDENTITY.json` | working tree identity 갱신 |
| `OPEN_ISSUES.md` | 신규 Finding 등록 |

`validate_development_harness.py` 는 위 상태에서 **PASS(exit 0)** 이다.

---

## 10. 종합 수치

| 항목 | 값 |
| --- | --- |
| pytest 시작 | 28 failed / 994 passed / 3 skipped |
| pytest 최종 | **1023 passed / 3 skipped / 0 failed** (exit 0, 347s) |
| 종결한 pytest Finding | **28건 전건** |
| 수정 파일 | 17개 (신규 2) |
| `.cpf-ide` 잔존 / 재생성 | 0 / 0 |
| refDB 물리 잔재 | 0 |
| 제거한 garbage | 약 16MB (untracked pytest 산출물) + 빈 디렉터리 12개 |
| 신규 하드코딩 | 0 |
| Gradle compile/build FAIL | 0 |
| Harness validator | PASS |
| Open Git Stage | 5/14 (BLOCKED_EXTERNAL) |
| Public Leakage | 0 |
| VS Code Error/Warning | 미실측 (사용자 Refresh/Reload 대기) |
| Full Runtime | NOT_EXECUTED (VS Code gate 선행) |
