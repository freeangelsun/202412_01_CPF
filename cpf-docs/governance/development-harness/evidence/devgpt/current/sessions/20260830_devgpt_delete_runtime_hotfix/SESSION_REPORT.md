# CPF DevGPT Delete/Runtime Hotfix Session Report

- sessionKey: `20260830_devgpt_delete_runtime_hotfix`
- role: `DEVGPT`
- Source Identity: `1a39531bcd1f0b1c82bbc6f330ab7b8256fc9132f62676ed1c8887ae42040839` / 8,451 files
- Scope: Delete Manifest generated-garbage semantics correction + Java host capability-first runtime correction + local maximum-runtime delivery completion
- Overall: `SOURCE_FIXED / VERIFICATION_PENDING`

## WP-H02
- Root Cause: Delivery Delete Manifest가 실행 때마다 바뀌는 Gradle/Python generated artifact를 immutable SHA로 취급해 정상 재생성 후 `DELETE SHA MISMATCH`를 발생시켰다. 또한 package diff 경로 처리 중 `.editorconfig/.gitattributes/.github/.gitignore`의 선행 dot이 유실되어 잘못된 삭제 경로 5건이 생성됐다.
- Source: `standards/CPF_HARNESS_CURRENT_ONLY_AND_GARBAGE_STANDARD.md`, `standards/CPF_FINAL_DELIVERY_AND_HANDOVER_STANDARD.md`, Current Delivery Delete Manifest, regression test.
- Consumer: 최종 Overlay 적용/가비지 삭제 명령, 다음 세션 delivery package.
- Fix: Canonical/legacy Source는 FILE_SHA256, mutable build/cache는 generated ownership이 확인된 4개 `GENERATED_ROOT` exact allowlist로 분리. Mutable generated root에는 stale byte SHA를 강제하지 않고 containment/protected-path/symlink/leaf guard를 적용한다.
- Regression: `HOTFIX_TARGETED_REGRESSION.log` 23 PASS, `HARNESS_STRENGTH_HOTFIX.log` PASS, `CLEAN_SOURCE_HOTFIX.log` PASS.
- Status: developer=완료, verification=미검증, overall=미검증.

## WP-B01
- Root Cause: Harness CR-22는 capability-first였지만 Required Full Runtime과 FullLocal Java 선택부 일부가 JDK major 25 문자열/`jdk-25*` 설치폴더에 의존했다. JDK 26+ 등 Java 25 target 호환 host를 불필요하게 거절할 수 있었다.
- Source: `run-cpf-required-full-runtime-validation.ps1`, `run-cpf-local-full-validation.ps1`, runtime/toolchain tests.
- Consumer: Root Build/Test/Publication/SBOM 및 모든 FullLocal Java stage.
- Fix: `java+javac`를 탐색하고 실제 `javac --release 25` 컴파일 후 생성 class를 해당 `java`로 실행하는 capability probe로 선택한다. `CPF_JAVA_HOME`을 우선 지원하고 기존 `CPF_JAVA25_HOME`은 호환 입력으로 유지한다.
- Regression: `HOTFIX_TARGETED_REGRESSION.log` 23 PASS, `TOOLCHAIN_HOTFIX.log` PASS.
- Status: Source contract fixed. 실제 사용자 Windows Java/Gradle physical replay 전에는 verification 완료로 승격하지 않는다.

## WP-FIN01
- Root Cause: 이전 최종 전달에서 최고강도 로컬 Runtime 한 줄 명령을 사용자에게 누락했다.
- Source/Consumer: `current/CPF_REQUIRED_FULL_RUNTIME_REQUEST.md` → `run-cpf-required-full-runtime-validation.ps1` → `run-cpf-local-full-validation.ps1`.
- Fix: Current Source canonical 명령을 `LOCAL_MAX_RUNTIME_COMMAND.txt`에 그대로 고정. PRIMARY → Fresh VS Code 0/0 → FRESH_REPLAY를 동일 Source Identity로 실행하며 FullLocal은 DB Runtime/Runtime Closure/Browser를 포함하고 wrapper는 Performance Load 및 destructive isolated DB rollback을 활성화한다.
- Evidence: `LOCAL_MAX_RUNTIME_COMMAND.txt`, Source Identity, targeted regression/toolchain/clean-source logs.
- Status: command delivery fixed; 실제 Windows physical Runtime/Independent Reviewer/QA는 여전히 mandatory pending.

## 검증 결과
- Targeted regression: 23 PASS / 0 FAIL.
- Toolchain Contract: PASS.
- Harness Strength: PASS canonical=218 tracking=394 execution=16 controls=33.
- Clean Source: PASS garbage=0 emptyDirs=0.
- Currentizer/Harness Self/Authority: PASS; Registry 410.
- Product Source Identity: `1a39531bcd1f0b1c82bbc6f330ab7b8256fc9132f62676ed1c8887ae42040839` / 8,451 files.

## 미완료 Acceptance
- Fresh Windows VS Code/Buildship `Error=0 / Warning=0`.
- Same Source maximum Full Runtime PRIMARY + VS Code 0/0 + FRESH_REPLAY.
- Open Git actual Fresh Release/Public Consumer.
- Codex/Claude Independent Review + QA Final Acceptance.
