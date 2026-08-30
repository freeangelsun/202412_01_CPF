# Claude Session 연속 실행 보고 — Harness Final Gate / Open Git 14/14

- **sessionKey**: `20260830_claude_ide_cli_db_closure`
- **Source Identity (현재)**: `250fd06f6cb050a8d7106cc0a07e481dd9590c7a34a6fadb47ee45c90801ba36` / 8469 files
- **환경**: Windows 10.0.26200.9168 / JDK 25.0.3 Temurin / Gradle 9.1.0 / Python 3.13 /
  Docker 29.7.2 / openssl 3.5.4 / cryptography 50.0.1

---

## WP-H02 — Harness Final Gate (신규 Harness 적용 후)

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-H02 (`registry handover alias WP-R03.15`) |
| 원 Requirement | `run_all_gates.py` Mandatory Harness Gate PASS |
| 최초 상태 | `DEVELOPMENT_HARNESS_FINAL_GATE=FAIL failed=['validators/validate_harness_authority.py', 'tests/test_negative_fixtures.py:AUTH_B']`, `HARNESS_AUTHORITY_GATE=FAIL ERRORS=2072` |
| Root Cause 1 | `HARNESS_GARBAGE` 2072건 — Open Git 릴리즈가 `evidence/platform/current/generated/python/open-git-release-venv/` 에 Python venv(38M, `.pyc` 1950)를 생성 |
| Root Cause 2 | `DEPRECATED_ACTIVE_REFERENCE` 4건 — `documentation-harness` 의 4개 JSON 이 삭제된 legacy 문서 `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` 를 active 참조 |
| Root Cause 3 | `CURRENT_SOURCE_IDENTITY_ACTUAL_DRIFT` — Harness 수정 후 identity 미갱신 |
| Root Cause 4 | `mutation_handover_registry_alias_loss` — `CPF_DEVELOPMENT_HANDOVER.md` 에 registry alias 가 `mentioned=0` 이라 `HANDOVER_REGISTRY_CONSISTENCY` 검사가 무력화되어 negative mutation 을 잡지 못함 |
| 변경 Source | `documentation-harness/{architecture-visual-semantics,harness,source-currentization,SOURCE_BASELINE}.json`, `current/CPF_DEVELOPMENT_HANDOVER.md` |
| Consumer/호출경로 | `validate_harness_authority.py` 의 `scan_roots=[cpf-tools, cpf-docs/development, cpf-docs/governance/documentation-harness]`. Provenance/migration ledger 는 계약상 예외이므로 `CANONICAL_MIGRATION_MAP.csv` / `DELETE_MANIFEST.csv` 의 이력 기록은 보존 |
| 이전 근거 | `CANONICAL_MIGRATION_MAP.csv:24` 가 legacy → `development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md` 를 `MAPPED / BYTE_OR_SEMANTIC_CURRENTIZATION / PASS / PRODUCT_CONTRACT_SEMANTIC_ANCHOR` 로 규정. 대상 문서(160KB)에 `### 2.1 Canonical Owner Map`, `## 12. Backoffice — MBW Business Domain`, `## 13. Backoffice Web — Channel/BFF Reference`, `## 17. ADM — Platform Control Plane` 이 모두 존재함을 확인하고 7건 이전 |
| 실행 명령 | `validators/validate_harness_authority.py`, `validators/currentize_source_identity.py`, `tests/test_negative_fixtures.py`, `validators/run_all_gates.py` |
| Exit Code | 0 |
| Test 결과 | `HARNESS_AUTHORITY_GATE=PASS CANONICAL=218 TRACE=218 BRIDGE=46 WORK=410 TEST=829 CONTROLS=33` / `NEGATIVE_FIXTURES_FINAL=33/33 PASS` / `DEVELOPMENT_HARNESS_FINAL_GATE=PASS failed=[]` |
| Regression | JSON 34개 파싱 유효, harness garbage 0, `SESSION_MERGE_PROTOCOL=PASS sessions=4 merged=4 pending=0 conflicts=0` |
| Evidence | `HARNESS_FINAL_GATE_PASS.log` |
| 상태 | **PASS** (현재 Source Identity 기준) |
| 재실행 조건 | Harness 정본/validator 변경 시 |

---

## WP-RL02 — Open Git Fresh Release 14/14

| 항목 | 내용 |
| --- | --- |
| WP ID | WP-RL02 (`registry handover alias WP-R07.17`) |
| 원 Requirement | `cpf release open-git build --profile binary` Stage 01~14 실제 PASS |
| 최초 상태 | Stage 06/14 FAIL (`PyInstaller is required only on private release build agents`), 5/14 |
| Root Cause 1 | `REQUIRED_GENERATOR_CLASSIFIERS=("windows-x64","linux-x64")` 인데 두 classifier 가 모두 없었다. release driver 는 cross-OS artifact 를 fabricate/substitute 하지 않는 fail-closed 설계다 |
| Root Cause 2 | Stage 10 에서 `verify_cross_platform_cli` 가 **Open Git 트리 전체의 모든 `.java`** 를 금지해 Generated Domain/EDU projection 과 정면 충돌 |

### 계약 판정 (Container artifact 허용 여부)

추측 없이 Source/Contract/Test 에서 확정했다.

| 확인 대상 | 결과 |
| --- | --- |
| Product Contract / standards / contracts 의 native build 환경·glibc·base image 규정 | **0건** |
| release source 의 container/VM/WSL 배제 조건 | **0건** |
| `build-cpf-generator-binary.py` `classifier()` | `platform.system()`/`platform.machine()` 만 검사 |
| `_verify_generator_distribution` | 파일 존재 + zip sha256 + manifest 좌표 + manifest sha256. **바이너리 형식/ELF/아키텍처 미검증** |
| 계약 정본 test `_fake_generator_distribution` | `.zip` 에 raw bytes 를 써도 통과 → 형식 검증 부재 확정 |
| Supply Chain 표준 | Native binary 를 통제 대상으로 언급하나 generator-cli 전용 서명 요구 없음 |
| **신규 표준 CR-22** | *"exact pin 은 Project 가 소유하는 Wrapper/Lock/**Container/Image** 처럼 재현성 산출물에만 허용한다"* → **Container 를 재현성 산출물로 명시 인정** |

**판정: Linux container 로 생성한 `linux-x64` ELF 는 계약상 허용된다.** Stage 조건 완화·classifier
대체·Contract 약화는 수행하지 않았다.

### 실행 내용

1. `linux-x64` — Linux container(`python:3.13-slim`) 에서 fresh build.
   1차 시도는 `On Linux, objdump is required` 로 실패했고 `binutils` 설치로 해결했다. zip 8,223,883 bytes.
2. `windows-x64` — host PyInstaller build. zip 8,530,982 bytes.
3. complete prebuilt matrix 를 Repository 밖 flat directory 에 구성
   (`<HOME>/Downloads/cpf-generator-matrix`, classifier 당 zip/zip.sha256/json 3파일 = 6파일).
4. `--generator-artifacts` 정규 경로로 주입 → **Stage 06 통과**.
5. Stage 10 결함 수정 후 Stage 01 부터 전체 재실행.

### Stage 10 결함 수정

`verify_cross_platform_cli` 는 CLI 검증 함수이며 docstring 이 *"Only the compiled cpf-cli.jar plus
thin OS wrappers are customer-visible"* 로 CLI source 유출 방지를 목적으로 명시한다. 그러나 구현은
Open Git 트리 전체의 `.java` 를 금지하여 다음과 충돌했다.

- `verify_open_git_tree` 의 `required = ["cpf-education", "bin"]` — EDU 를 **필수**로 요구
- Stage 10 이름: *"Open Git Source Projection — Generated Domain / Backoffice / EDU / Developer Command"*

실측 유출물: `cpf-education` 157 / `cpf-external` 33 / `cpf-member` 32 개 `.java`.
정작 보호 대상인 `CpfCli.java` 유출은 **0건**이었다.

계약 정본 test 가 금지 범위를 명시한다.

```python
assert not (staging / "bin/CpfBootstrap.java").exists()
assert "bin/CpfCli.java" not in targets
assert "bin/CpfGeneratorLauncher.java" not in targets
```

따라서 검사를 `bin/` 영역으로 정확화했다. private framework root 유출은 `verify_open_git_tree` 의
`forbidden_roots` 가 별도로 담당한다. open-git contract test **29 passed** 로 회귀 없음을 확인했다.

### 최종 결과

| 항목 | 값 |
| --- | --- |
| 최종 상태 | **VERIFIED** / exitCode **0** |
| Stage | **14/14** |
| Source/Package/Binary/Open Git 검증 | 전부 **PASS** |
| generatorDistribution.classifiers | `["windows-x64","linux-x64"]` |
| openGitFileCount / publicStagingFileCount | 281 / 279 |
| binaryFileCount | 1512 |
| sourceJarCount / javadocJarCount | **0 / 0** (binary profile 정책) |
| frameworkSourceProjection.fileCount | **0** (private framework source 유출 0) |
| crossPlatformCli | PASS `bin/lib/cpf-cli.jar` |
| Fresh Workspace 빌드·테스트 | `CPF_OPEN_GIT_WORKSPACE=PASS DOMAIN_COUNT=2 DOMAIN_STATE=SELECTED` |
| Frontend 계약 | operations=337 mutations=154 PASS |
| **Git write** | `gitAddExecuted=false` / `commitExecuted=false` / `pushExecuted=false` |
| 산출 경로 | `cpf-release/open-git`, `cpf-release/binary-repository` |

| 변경 Source | `cpf-tools/release/open-git/cpf_open_git.py` |
| Evidence | `OPEN_GIT_RELEASE_14_14_VERIFIED.log` |
| 상태 | **VERIFIED / READY_FOR_USER_GIT_ACTION** (사용자 검토 전 add/commit/push 금지 준수) |

---

## WP-B02 — VS Code Build Path (실측 대기)

| 항목 | 내용 |
| --- | --- |
| 원 Requirement | Fresh VS Code 전체 Workspace Java Error=0 / Warning=0 |
| 사용자 보고 오류 | `cpf-admin` → `browser-bff`, `cpf-education` → `batch-service`, `cpf-gateway` → `web-api` 의 `build/classes/java/main` missing 6건 |
| Root Cause | source-empty profile project 는 `compileJava=NO-SOURCE` 라 canonical output 이 생성되지 않는데 Build Server 가 class-folder library 로 참조 |
| 정정 사항 | 세션 전반부 판정 *"`.cpf-ide/main` 을 lib 로 참조하는 JDT entry 0건"* 은 당시 JDT 모델이 `.cpf-ide` 기준이었기 때문이며, canonical 전환 후 source-empty project 도 실제로 참조됨이 확인되었다 |
| 조치 | 정비된 Harness conventions 의 `gradle.afterProject` 가 source-empty project 의 **canonical** `build/classes/java/main` 을 materialize한다(비표준 IDE 전용 경로 아님). Gradle 실행으로 7/7 생성 확인 |
| 검증 | `CPF_IDE_CLASSPATH_READY javaProjects=86 compiled=79 sourceEmpty=7 scope=gradle-canonical-compile-output`, `CPF_IDE_CLASSPATH_MODEL=PASS`. 전 workspace JDT class-folder 참조 **누락 0** |
| **재발 관찰** | Open Git Stage 01(이전 생성물 전체 재생성)이 clean 을 수행하면 source-empty output 이 다시 사라진다. Gradle 재실행 또는 VS Code Refresh(Build Server 가 Gradle configuration 을 수행)로 자동 복구된다. 실측 직전 복구를 완료했다 |
| 상태 | **VERIFICATION_PENDING** — 실제 Problems 수치 미실측 |
| 재실행 조건 | `Gradle: Refresh Gradle Project` → `Java: Reload Projects` → Problems 전량 재수집 → `CPF_VSCODE_PROBLEMS_yyyyMMdd_HHmmss.json` 로 Downloads 에 export |

---

## Full Runtime — 진입 차단 유지

`run-cpf-required-full-runtime-validation.ps1:59-60` 이 Fresh VS Code Problems JSON 을 필수 입력으로
요구하고 `verify-cpf-vscode-problems.py` 가 severity 8/4 를 세어 하나라도 있으면 `throw` 한다.
**우회 금지 항목이므로 실행하지 않았다.**

prerequisite 점검은 완료했고 JSON 확보 즉시 실행 가능한 상태다.

| prerequisite | 상태 |
| --- | --- |
| java / python / docker / node / npm / pwsh | 전부 확인 |
| DockerRoot `C:\dev\Docker` | 존재 |
| `C:\dev\Docker\Secrets\cpf-runtime.env` | 존재 |
| Docker 데몬 | 29.7.2 가동 (containers=17) |
| `CPF_VSCODE_PROBLEMS_*.json` | **없음** ← 유일한 차단 요소 |

상태: **NOT_EXECUTED** (Runtime FAIL 아님. 선행 IDE Acceptance 미충족)

---

## 신규 Finding 요약

| # | Finding | 상태 |
| --- | --- | --- |
| 1 | Open Git venv 가 harness evidence 경로에 garbage 2072건 생성 | 제거 완료 |
| 2 | `documentation-harness` 4개 JSON 의 deprecated legacy 참조 | canonical 이전 완료 |
| 3 | HANDOVER alias 미기재로 consistency 검사 무력화 | 현행화 완료 |
| 4 | `verify_cross_platform_cli` 가 Generated Domain/EDU projection 을 차단 | 범위 정확화 완료 |
| 5 | Open Git clean 이 source-empty canonical output 을 제거 | 복구 절차 확인·기록 |

미해결 Mandatory Finding: **0** (VS Code 실측과 Full Runtime 은 미실행 상태이며 Finding 이 아님)


---

## WP-B02 추가 — Clean Replay / lifecycle ownership 실증

| 항목 | 내용 |
| --- | --- |
| 원 Requirement | `clean` → canonical prepare → Refresh/Reload → Error=0/Warning=0 이 반복 재현 가능해야 한다 |
| 실행 1 | `gradlew clean` (88 tasks, exit 0) → `gradlew cpfPrepareIdeClasspath cpfVerifyIdeClasspathModel` (exit 0) |
| 결과 1 | `CPF_IDE_CLASSPATH_READY javaProjects=86 compiled=79 sourceEmpty=7`, `CPF_IDE_CLASSPATH_MODEL=PASS`, source-empty **7/7 EXISTS** |
| 실행 2 (ownership 판정) | `gradlew clean` → 상태 확인(MISSING) → **preparation task 없이** `gradlew projects` → 상태 재확인 |
| 결과 2 | clean 직후 MISSING → **Gradle configuration만으로 EXISTS 복구 확인** |

### Root Cause 판정 (4개 후보 전수)

| 후보 | 판정 | 근거 |
| --- | --- | --- |
| clean 후 IDE preparation task 자동 호출 안 됨 | **해당 없음** | preparation task 자체가 불필요. `gradle.afterProject` configuration-time materialization 이 소유 |
| Buildship Refresh 가 required preparation task 를 소비 안 함 | **해당 없음** | Refresh 는 Gradle configuration 을 수행하므로 자동 복구 |
| source-empty output materialization lifecycle ownership 오류 | **해당 없음** | canonical `build/classes/java/main` 에 configuration 시점 소유 확정 |
| `.classpath` / JDT model stale | **해당** | `.classpath` 최종 갱신 14:10, 이후 재import 없음 |

**결론**: `clean` 이 output 을 지우는 것은 정상이며, 이후 **어떤 Gradle 실행(= Buildship Refresh 포함)이든
configuration 단계에서 결정적으로 재materialize** 한다. `.cpf-ide` 같은 비표준 우회 경로 없이 canonical
경로만으로 lifecycle 이 성립함을 실증했다.

### VS Code UI 조작 제약 (제가 직접 수행 불가한 사유)

`code.cmd` CLI 는 `--diff/--merge/--add/--remove/--goto/--new-window` 등 파일·창 조작만 제공하며
**명령 팔레트 명령 실행 인자를 제공하지 않는다**(`code --help` 확인). 따라서
`Gradle: Refresh Gradle Project` / `Java: Reload Projects` 및 Problems export 는 CLI 로 트리거할 수
없다. jdt_ws 메타데이터 직접 삭제는 VS Code 실행 중 위험하고 전체 재시작을 유발하므로 수행하지 않았다.
`.classpath` 직접 편집은 조작이므로 금지 대상이다.

VS Code 프로세스는 14:07 기동, Java LS 3개 프로세스 가동 중.

---

## Garbage 정리

| 항목 | 정리 전 | 정리 후 |
| --- | --- | --- |
| `__pycache__` | 35 | **0** |
| `*.pyc` | 373 | **2** |
| `.pytest_cache` | 0 | **0** |
| harness 내부 garbage | 0 | **0** |

잔존 2건은 `cpf-docs/work/evidence/generated/python/pycache/pytest-ownership-20260822/` 로,
디렉터리명이 `__pycache__` 가 아닌 날짜 부여 evidence 보존물(untracked, harness 외부)이라 유지했다.

Source Identity 는 정리 전후 `250fd06f6cb050a8d7106cc0a07e481dd9590c7a34a6fadb47ee45c90801ba36` 로
변동 없음(untracked 만 제거).


---

## WP-R00.17 — Python bytecode/cache Repository 전면 정리 및 재발 방지

| 항목 | 내용 |
| --- | --- |
| 원 Requirement | Repository garbage 0 (`.pyc` / `__pycache__` / `.pytest_cache` / generated venv) |
| 판정 정정 | 이전 보고에서 `.pyc` 2건을 *"날짜 붙은 evidence 라 의도적 보존"* 으로 판단한 것은 **오류**였다. 폴더명이 아니라 Owner/Consumer/Evidence Contract 로 판단해야 한다 |
| Canonical 정책 근거 | `CURRENT_GARBAGE_DECISIONS.csv` `GARBAGE-0637` — *"[CLEAN-SOURCE] Regeneratable Python bytecode cache must not ship in product source."* / DELETE / approved=true. `GARBAGE-1385`,`GARBAGE-1670` 은 `cpf-docs/work/evidence/**` stale 정리를 user-approved 로 규정 |
| Authority 판정 | `SOURCE_IDENTITY.json` 이 `cpf-docs/work/**` 를 `mutableGovernanceExcludedFromSource` 로 지정. `cpf-source-state.py:35` 가 `/cpf-docs/work/evidence/generated/` 를 **"retired scratch path: identity compatibility only"** 로 명시. `current-authority-registry.json` 에 `cpf-docs/work` **0건** → Current Authority 아님 |
| Consumer 확인 | `.pyc` 를 canonical Evidence 로 소비하는 도구/원장 **0건** |

### 정리 결과

| 항목 | 전 | 후 |
| --- | --- | --- |
| `.pyc` | 375 | **0** |
| `__pycache__` | 35 | **0** |
| `.pytest_cache` | 0 | **0** |
| generated venv (`pyvenv.cfg`) | 0 | **0** |
| `cpf-docs/work/evidence/generated/python/**` | 존재 | **0** (제거) |
| `legacy-ide-cache-20260822` | 317K, activeRef=none | **0** (제거) |
| harness garbage | 0 | **0** |

`jvm` / `gradle` / `runtime-secrets` / `domain-generator` 는 활성 참조가 있고 일부 tracked 이므로 유지했다.
보호경로(`.editorconfig`, `.gitattributes`, `.gitignore`, `.github`)는 cleanup 대상에서 제외했고 무결성을 확인했다.

### 재발 방지 (§7)

기존 `HARNESS_GARBAGE` 검사는 `package_roots=[development-harness, deliverables/development-harness]`
범위뿐이라 이번에 실제 발생한 `cpf-tools` 373건 / `cpf-docs/work` 2건을 **탐지하지 못했다.**

1. `validate_harness_authority.py` 에 `REPOSITORY_PYTHON_CACHE` 검사를 추가해 Repository 전 범위
   (`.git`/`node_modules` 제외)의 `.pyc`/`.pyo`/`__pycache__`/`.pytest_cache` 를 fail-closed 로 잡는다.
2. negative mutation `mutation_repository_python_cache_reentry` 를 추가했다.
   Harness payload 밖(`cpf-tools/__pycache__`)에 bytecode 를 재유입시키면 반드시 검출된다.
   `NEGATIVE_FIXTURES_FINAL=**34/34** PASS` (기존 33 → 34).
3. gate 실행 자체가 bytecode 를 남기던 자기모순을 제거했다.
   `run_all_gates.py` 의 child 호출 2곳, harness `bin/*.ps1`·`bin/*.sh` 8개 진입점,
   `test_negative_fixtures.py` 의 subprocess 5곳에 `-B` 를 적용했다(비-B 호출 잔여 0).

### canonical 실행 경로 확인

`.pyc` 재발의 마지막 원인은 Source 결함이 아니라 **실행 방법**이었다.
canonical runner `cpf-tools/testing/tools/run-cpf-pytest.py` 는 이미
`env["PYTHONDONTWRITEBYTECODE"]="1"` 로 **subprocess 까지 전파**한다.
직접 `python -m pytest` 를 호출하면 `-B` 가 부모 프로세스에만 적용되어 child 가 bytecode 를 남긴다.

실증: canonical runner 로 `cpf-tools/verification/tests/` 실행 →
**125 passed / 1 skipped, `.pyc`=0, `__pycache__`=0**.

### 검증 결과

| Gate | 결과 |
| --- | --- |
| `validate_harness_authority.py` | **PASS** |
| `run_all_gates.py` | **DEVELOPMENT_HARNESS_FINAL_GATE=PASS failed=[]** |
| gate 실행 후 `__pycache__` | **0** (자기모순 해소 실증) |
| negative fixtures | **34/34 PASS** |
| 영향 회귀 (verification+release) | **196 passed / 1 skipped** |
| canonical runner 회귀 | **125 passed / 1 skipped** |
| Source Identity | `f44988968177ceb4a318d2ee9000a7651ae8747244e88fef1bdb44ee270945e8` / 8469 files |
