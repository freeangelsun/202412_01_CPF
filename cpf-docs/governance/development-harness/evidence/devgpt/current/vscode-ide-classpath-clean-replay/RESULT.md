# VS Code Buildship/JDT Build Path — Root Cause 확정 및 IDE 전용 Output 계약 제거 (WP-R02.07 / WP-B02)

## 1. Source Identity

| 항목 | 값 |
| --- | --- |
| Before productContentSha256 | `810bb3a6ed2a35e732b7441f880edfb71f9a8bbbd8889e9c4c080681d027964a` |
| After productContentSha256 | `7a676246928631b014b8356149a3ec3f077e8b975458641df209ba66de14605b` |
| After productContentSha1 | `65b2ec18fb73118f5c95f077ed5b0fe6fad0c3f6` |
| fileCount / totalBytes | 8453 / 46145070 |
| identityPolicy | `GIT_INDEPENDENT_CANONICAL_PATH_SIZE_SHA256_LINES` |
| gitSha (read-only 참조) | `b20f3ddf8dcc0e26725e8907245c292530c59167` |

파일 수가 변하지 않은 것은 신규 파일 생성이나 삭제 없이 기존 2개 파일만 수정했다는 뜻이다.

## 2. 환경

| 항목 | 값 |
| --- | --- |
| OS | Microsoft Windows [Version 10.0.26200.9168] |
| JDK | OpenJDK 25.0.3 Temurin-25.0.3+9 LTS |
| Gradle | 9.1.0 (wrapper) |
| VS Code Java Extension | `redhat.java-1.55.0-win32-x64` |
| VS Code Gradle Extension | `vscjava.vscode-gradle-3.18.0` |
| IDE import 방식 | `java.gradle.buildServer.enabled: on` (Gradle Build Server) |

## 3. 최초 Error / Warning (원문 보존)

사용자 제공 VS Code Problems. 최초 19건, Clean 검증 중 26건까지 증가. 전부 `severity: 8` (Error),
Warning 은 0건이었다.

```
Project 'cpf-admin-runtime' is missing required library: 'C:\dev\projects\jck\202412_01_CPF\cpf-starters\messaging\reliability\jdbc\build\classes\java\main'
Project 'secret' is missing required library: 'C:\dev\projects\jck\202412_01_CPF\cpf-core\build\classes\java\main'
The project cannot be built until build path errors are resolved
```

위 오류는 해당 Project 의 JDT Build Path 가 물리적으로 존재하지 않는 컴파일 출력 디렉터리를 필수
Library 로 참조하고 있으며 그 결과 Project 전체가 빌드 불가 상태라는 의미다.

| 시점 | Error | Warning |
| --- | --- | --- |
| 최초 보고 | 19 | 0 |
| Clean 검증 중 (일시 증가) | 26 | 0 |

## 4. Root Cause (확정)

JDT workspace metadata (`.metadata/.plugins/org.eclipse.core.resources/.projects/*/.classpath`) 를
직접 해석하여 확정했다. 추정이 아니라 실측이다.

1. Gradle Build Server 는 in-build project dependency 를 **두 형태로 중복 등록**한다.
   - 정상 project reference: `kind="src" path="/cpf-core"`
   - 절대경로 class-folder library: `kind="lib" path="C:/.../build/classes/java/main"`
2. `java-library` plugin 이 적용된 module 은 Gradle compile-avoidance 에 의해 compile classpath 가
   jar 가 아닌 **classes directory** 로 해석되므로 class-folder library 로 등록된다.
   `java` plugin 만 적용된 application project(`cpf-admin`, `cpf-gateway`, `online` 등)는 jar 로
   해석되어 class-folder library 가 생기지 않는다. workspace 실측치는 88 project / 540 참조다.
3. JDT 는 `kind="lib"` class folder 의 **물리적 존재**를 강제한다. 이 경로는 `build/` 하위이므로
   `gradlew clean` 이 제거하며, 재생성되어도 JDT 는 project 외부 절대경로의 변경을 자동 인지하지
   않는다. 그래서 build 가 진행되는 동안 marker 가 생성되고 그대로 남는다.
4. `cpf-admin-runtime` 과 `secret` 처럼 오류가 잔존한 Project 는 compile event 가 발생하지 않아
   JDT 가 build path 를 재검증할 trigger 를 얻지 못한 경우다.

### 4.1 왜 발생했는가

이전 개선에서 도입되어 있던 IDE output 계약은 source-empty Java project 의 compile output 을
project-local `.cpf-ide/main` 으로 옮겨 `clean` 에 견디게 하는 방식이었다. 그러나

- JDT class-folder library 가 참조하는 대상은 **dependency 79개의 compile output** 이며
  source-empty project 의 output 은 어떤 project 에서도 class-folder library 로 참조되지 않는다
  (실측 결과 `.cpf-ide/main` 을 lib 로 참조하는 JDT entry 는 0건).
- 즉 그 계약은 실제 오류 경로를 덮지 못하면서, 제품 Source Tree 아래에 IDE 종속
  generated directory 를 남기는 부작용만 발생시켰다.

## 5. Case 판정

| Case | 판정 | 근거 |
| --- | --- | --- |
| CASE A — 실제 Gradle dependency/compile 결함 | 해당 없음 | `compileJava compileTestJava` 300 tasks, `build` 593 tasks 모두 BUILD SUCCESSFUL / warning 0 |
| CASE B — Gradle PASS, JDT 만 FAIL | 해당 | JDT 가 class-folder library 의 물리적 존재를 강제 |
| CASE C — stale VS Code model | 해당 | 오류가 가리킨 디렉터리는 진단 시점에 모두 실제 존재 |

## 6. Canonical Owner / Consumer / 수정 파일

- Canonical Owner: root Gradle convention (`cpf-tools/build/cpf-root-conventions.gradle`)
- Consumer: 86 Gradle Java project / 88 JDT project 전수.
  cpf-admin, cpf-admin/runtime, cpf-core, cpf-common, Starter Common, Messaging,
  Messaging Reliability JDBC, Data, Redis, Integration, Resilience, Webhook, File, Tabular/POI,
  Security, Platform Operations, Runtime Health, Runtime Control, Observability, Gateway,
  Backoffice, Education, Batch, Generated Domain, Member, External.

| 파일 | baseline_sha256 (git HEAD) | final_sha256 |
| --- | --- | --- |
| `cpf-tools/build/cpf-root-conventions.gradle` | `5df0dafeb4eaef9f8ef819f64aa5a09cca11e050ba7923e79c3b3111441d2639` | `d8a6d4457e637bd550a32d3411f59c98e776eb6c40194e493138fd40461af218` |
| `cpf-tools/verification/tests/test_cpf_vscode_classpath_output_contract.py` | `e68321816adec0fc4188b4a6b4fdbffd8c56a426a30c1efe73e1012e55ebb042` | `b0a49c1f98a2ac30ef5219c9f22efb6d339c673a945c05bc577739d6c2c65f00` |

### 6.1 수정 내용

1. **IDE 전용 output directory 계약 전면 제거.** `compileJava.destinationDirectory` 재지정과
   project-local `.cpf-ide/main` materialization 을 삭제했다. 모든 Java project 는 Gradle
   canonical compile output (`build/classes/java/main`) 을 그대로 사용한다.
2. `cpfPrepareIdeClasspath` 는 sourceful Java project 의 `compileJava` 를 실제로 수행하여
   JDT 가 요구하는 class folder 를 정상 산출물로 복구한다. 빈 directory 생성이나 fake output 은
   사용하지 않는다. 대상 판별은 discovery 기반이며 module 이름 하드코딩은 없다.
3. `cpfVerifyIdeClasspathModel` 은 fail-closed 로 두 가지를 검증한다.
   - 모든 Java project 의 compile output 이 Gradle canonical 경로인가 (`non-canonical-compile-output`)
   - project 아래에 IDE 전용 output directory 가 재생성되지 않았는가 (`ide-only-output-directory-present`)
4. Contract test 를 위 계약에 맞게 현행화하여 회귀를 정적으로 차단한다.

## 7. 하드코딩 검사

| 검사 항목 | 결과 |
| --- | --- |
| 절대경로 (`C:\`, `D:\`, `/Users/`, `/home/`) | 0 |
| 사용자명 / PC명 | 0 |
| IDE 설치경로 / Java 설치경로 | 0 |
| Gradle cache 절대경로 / 고정 Workspace 경로 | 0 |
| 특정 Module build output 절대경로 | 0 |
| `.classpath` 직접 조작 | 0 |
| 신규 하드코딩 합계 | **0** |

경로는 전부 Gradle API(`project.projectDir`, `layout.buildDirectory`, `tasks.named`) 로만 해석한다.

## 8. Garbage 검사 / `.cpf-ide` 처리

`.cpf-ide` 는 삭제부터 하지 않고 생성 주체와 참조를 먼저 특정했다.

- 생성 주체: `cpf-tools/build/cpf-root-conventions.gradle` 의 `afterEvaluate` materialization (제거 완료)
- 참조 주체: 동 convention 의 두 verification task (canonical output 기준으로 재작성 완료)
- 제품 Source / Generator / Template / Generated Domain / Open Git projection / VS Code settings
  에서의 참조: 0건 (전수 검색 확인)

| 항목 | 결과 |
| --- | --- |
| 제품 트리 `.cpf-ide` 잔존 | **0** (86개 제거) |
| Gradle 재실행 후 `.cpf-ide` 재생성 | **0** (`cpfVerifyIdeClasspathModel` fail-closed PASS) |
| `__pycache__` / `.pytest_cache` (제품 트리) | **0** (검증 실행 산출물 제거) |
| `.bak` / `.orig` / `.tmp` / `.old` 신규 | **0** |
| `.gitignore` 의 IDE 전용 예외 항목 | 제거 (HEAD 와 동일) |

`cpf-docs/.../evidence/**` 내부의 `.cpf-ide` 3건과 venv `__pycache__` 는 다른 작업의 evidence
산출물이므로 임의 개입하지 않았다.

## 9. 실행 명령 / 결과

| Gate | 명령 | Exit Code | 결과 |
| --- | --- | --- | --- |
| Java25 Compile | `gradlew compileJava compileTestJava --continue --stacktrace` | 0 | BUILD SUCCESSFUL / 300 tasks / warning 0 |
| Java25 Full Build | `gradlew build --continue --stacktrace` | 0 | BUILD SUCCESSFUL in 3m 54s / 593 tasks / warning 0 |
| IDE Output Contract | `gradlew cpfPrepareIdeClasspath cpfVerifyIdeClasspathModel --stacktrace` | 0 | `CPF_IDE_CLASSPATH_MODEL=PASS javaProjects=86 scope=gradle-canonical-compile-output` |
| Contract Test | `pytest cpf-tools/verification/tests/test_cpf_vscode_classpath_output_contract.py` | 0 | 4 passed |

## 10. JDT Model 실측

`JDT_CLASSPATH_MODEL.json`

| 항목 | 값 |
| --- | --- |
| workspaceProjects | 88 |
| totalClassFolderLibraries | 540 |
| totalMissingClassFolders | **0** |
| projectsWithStaleIdeOnlyOutputReference | 7 |

class folder 누락은 0건이다. 나머지 7건은 수정 이전에 기록된 JDT workspace metadata 의 잔재이며
Fresh Java Language Server Workspace 재생성으로 제거된다. 제품 Source 에는 남아있지 않다.

## 11. Messaging/JMS 회귀

| Type | canonical owner | 중복 |
| --- | --- | --- |
| `CpfMessageBridgeContextSupport` | `cpf-starters/messaging/.../messaging/context/` | 0 |
| `CpfBrokerBridgePort` | `cpf-starters/messaging/.../messaging/api/` | 0 |
| `CpfBrokerBridgeResult` | `cpf-starters/messaging/.../messaging/api/` | 0 |
| `CpfBrokerBridgeHandler` | `cpf-starters/messaging/.../messaging/api/` | 0 |
| `CpfMessageHeaderNames` | `cpf-starters/messaging/.../messaging/context/` | 0 |

과거 marker 의 `CpfMessageBridgeContextSupport cannot be resolved to a type` 은 동일한 build-path
cascade 의 stale 잔여이며 Gradle compile 은 PASS 다. duplicate Type 은 생성하지 않았다.

## 12. Warning 처리

- Java25 전체 build 로그 기준 compiler warning **0건**.
- 제품 Source 에 `.settings/org.eclipse.jdt.core.prefs` 등 warning severity 하향 설정 **0건**.
  JDT 설정은 VS Code 가 workspace metadata 에 자동 생성한 것으로 제품 Source 가 아니다.
- 이번 변경에서 `@SuppressWarnings` 추가 0건, compiler option disable 0건, diagnostics disable 0건.
- marker snapshot 누적본의 `Unnecessary @SuppressWarnings("deprecation")` 은 현재 Source 에 해당
  annotation 이 없어 이미 해소된 stale 항목이다.

## 13. Architecture 보호

Internal module 의 Public 노출, cpf-core 로의 runtime/provider 이동, reverse/circular dependency,
Owner 변경, Public BOM 의 internal leaf 노출, Generated Domain 의 internal dependency, consumer 별
wrapper 증식은 전부 발생하지 않았다. `implementation project(...)` / `api project(...)` 추가도 0건이다.
수정은 root Gradle convention 1개 파일과 그 contract test 1개로 한정된다.

## 14. Open Git / Public Leakage

| 항목 | 결과 |
| --- | --- |
| Open Git Gradle 단계 | BUILD SUCCESSFUL in 7m 10s / 864 tasks |
| Open Git 전체 | Stage 06/14 Generator Distribution 에서 FAIL |
| 실패 사유 | `PyInstaller is required only on private release build agents` (환경 전제조건 미충족) |
| commitExecuted / pushExecuted | false / false |
| IDE-only 파일의 Public 산출물 유출 | **0** |

`.cpf-ide` 계약 자체를 제거했으므로 Public JAR / sources JAR / javadoc JAR / POM / BOM / SBOM /
Generator / Generated Source / Open Git ZIP / Public Sample 어디에도 IDE-only 경로가 들어갈 여지가
없다. Open Git 실패는 이번 수정과 무관한 별도 환경 의존성이며 Gradle 산출 단계는 전부 통과했다.

## 15. 상태

| 구분 | 상태 |
| --- | --- |
| Source | SOURCE_FIXED |
| Java25 Compile / Full Build | PASS |
| IDE Output Contract (canonical / garbage 0) | PASS |
| Fresh VS Code Import Error=0 Warning=0 | VERIFICATION_PENDING |
| Clean Replay | VERIFICATION_PENDING (Fresh Import 이후 수행) |
| Independent Reviewer / QA | NOT_EXECUTED |

**CLOSED 아님.** Fresh Java Language Server Workspace 재생성 후 전체 Workspace Problems 재수집이
완료되어야 승격 가능하다.

## 16. Evidence 파일

- `GRADLE_COMPILE_JAVA25.log`
- `GRADLE_FULL_BUILD_JAVA25.log`
- `GRADLE_IDE_OUTPUT_CONTRACT.log`
- `OPEN_GIT_RELEASE_BINARY.log`
- `JDT_CLASSPATH_MODEL.json`
- `SOURCE_STATE_FINAL.json`
