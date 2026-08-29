# CPF TEST AND EVIDENCE — Current

## Source Identity

- Input Working Tree ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260829_172338.zip`
- ZIP SHA-256: `52d807563afb9268ef176d89dff8b4721e1a1c5b6475fb8ae0f84e269c6d3886`
- Current Product Source Identity: `b162d358b4f127f7c4e3a816f89dcc8ab5fc2f66423ec7c37cbbefd6414fde9e`
- Source files: 8,351
- Git write: 0

## 실제 실행 결과

| 검증 | Current exact Source 결과 | Evidence |
|---|---|---|
| Current/Hygiene/Garbage/UTF-8/DB4/CLI/Requirement | PASS | `cpf-docs/work/evidence/current/devgpt/VSCODE_REWORK_LOW_COST.log` |
| VS Code classpath/Gradle dependency/local-runtime contract | 37 PASS / 0 FAIL | `.../VSCODE_REWORK_CONTRACT.log` |
| Verification tests | 104 PASS / 0 FAIL | `.../VSCODE_REWORK_VERIFICATION_TESTS.log` |
| Testing Tools split | 400 PASS / 23 SKIP / 0 FAIL | `.../VSCODE_REWORK_TESTING_TOOLS_1..3.log` |
| DB tests | 142 PASS / 2 SKIP / 0 FAIL | `.../VSCODE_REWORK_DB_TESTS.log` |
| DB verification | 87 PASS / 0 FAIL | `.../VSCODE_REWORK_DB_VERIFICATION.log` |
| Runtime tools | 73 PASS / 2 SKIP / 0 FAIL | `.../VSCODE_REWORK_RUNTIME_TESTS.log` |
| Security tools | 8 PASS / 0 FAIL | `.../VSCODE_REWORK_SECURITY_TESTS.log` |
| Release/Open Git/Public contract | 62 PASS / 0 FAIL | `.../VSCODE_REWORK_RELEASE_TESTS.log` |
| Generator | 48 PASS / 10 SKIP / 6 subtests PASS / 0 FAIL | `.../VSCODE_REWORK_GENERATOR_TESTS.log` |
| Docker development-test contract | 8 PASS / 0 FAIL | `.../VSCODE_REWORK_DOCKER_TESTS.log` |
| Tool EntryPoint inventory | 978 entries; tooling 832; duplicate/dead 0 | `.../VSCODE_REWORK_ENTRYPOINT.log` |

SKIP/환경 미실행은 PASS로 승격하지 않았다. Fresh Windows Java25 Gradle Import의 실제 VS Code Problems 0/0은 별도 Physical Acceptance다.

## Runtime 입력 로그에서 확인한 원 결함

사용자 제공 `CPF_FULL_RUNTIME_LAUNCH_20260829_164952.log`은 NXT3/Evidence/Testing/DB/Verification/Gradle/Frontend/Local topology/Generator/DB verifier/Batch/Gateway/One-WAS/Managed State까지 18 FAIL, 9 NOT_EXECUTED를 기록했다. 이번 Source 보정은 이 Root Cause를 current exact Source에서 정적·계약 수준으로 닫았지만, Windows Java25 Physical Runtime은 다시 실행해야 한다.

## 미검증 환경 및 재실행 조건

1. Java 25.0.3 Root Build/Test/Publication/SBOM.
2. Fresh Gradle Import 후 **전체 Domain/Module VS Code Problems Error 0 / Warning 0**.
3. Oracle/PostgreSQL/MariaDB DB3 Physical lifecycle.
4. Windows/Linux Unified CLI actual lifecycle.
5. Batch 5-role + Worker×2 kill/takeover/fencing/UNKNOWN/reconcile.
6. One-WAS transaction/logging/OpenAPI.
7. ADM/Backoffice Frontend/Browser E2E/a11y/error states.
8. Performance live/load/soak.
9. Actual Open Git Fresh Release.
10. Same Source Full Runtime/Fresh Replay.

각 항목은 `CPF_QA24_DEVELOPMENT_CLOSURE_INVENTORY.csv`에서 Requirement별로 추적한다.

## Fresh Overlay Static Replay

입력 Working Tree ZIP을 다시 복제한 baseline에 이번 Overlay를 적용한 뒤 Source Identity exact match와 low-cost/영향 회귀를 재실행하여 PASS했다. 이는 **Overlay 재현성의 static replay**이며, Docker/DB3/One-WAS/Browser/Performance까지 포함한 Physical Fresh Replay를 대체하지 않는다.

## VS Code Buildship/JDT 재개방 — 2026-08-29

사용자 Fresh workspace에서 `cpf-admin`, `cpf-backoffice/online`, `cpf-education`, `cpf-gateway`, `secure-api`, `browser-bff` 등에 `build/classes/java/main` missing required library가 계속 남아 기존 보정이 False Green임을 확인했다. 이전 `prepareCpfIdeClasspathOutput`은 task 실행 시에만 directory를 만들기 때문에 **Fresh Gradle Import 자체에는 효과가 없었다**.

Current 수정은 Codex의 기존 구조를 유지한다. Public Profile은 `assembly-only`를 유지하고 fake Java Source/Class, profile 종류 변경, Consumer dependency 우회를 추가하지 않는다. Root Gradle convention은 Source가 0개인 Java project만 discovery하여 configuration/Buildship Tooling model 시점에 canonical compile output directory를 생성한다. `cpfVerifyIdeClasspathModel`은 explicit repair 전에 이 조건을 fail-closed 검증한다. 신규 task/test로 인한 Tool EntryPoint Catalog도 scanner로 재생성했다.

**Source/contract 개발은 완료했지만 Fresh Windows VS Code Problems Error 0 / Warning 0 실측 전에는 verification 완료가 아니다.**


## QA24 VS Code + Runtime UTF-8 Hotfix — 2026-08-29

- Current Product Source Identity: `b162d358b4f127f7c4e3a816f89dcc8ab5fc2f66423ec7c37cbbefd6414fde9e`
- VS Code 잔여 4건은 `secure-api/browser-bff -> web-api` Buildship classpath가 `build/classes/java/main`처럼 `clean`으로 삭제되는 source-empty project output을 요구하는 원인으로 재개방했다.
- source-empty Java project의 compile output은 Gradle user-home `cpf-ide-classpath/...` generated state로 이동하며, module 이름 하드코딩·fake Java Source/Class·dependency 복제를 추가하지 않는다. `cpfVerifyIdeClasspathModel`은 output이 stable Gradle user-home 경로인지와 실제 directory 존재를 fail-closed 검증한다.
- Runtime 한글 깨짐은 남아 있던 Windows PowerShell 5.1 child 호출을 `pwsh` 7로 제거하고, Java child에 `file/stdout/stderr.encoding=UTF-8`을 강제했다. Required Full Runtime은 추가 `pwsh` native stdout decode boundary 없이 현재 pwsh process에서 FullLocal runner를 호출한다.
- current exact-source static/replay evidence: Current/Hygiene/Garbage/UTF-8 PASS, redirected native process UTF-8 `28/28`, Full Runtime child PowerShell scripts `21`, mojibake Source `0`, affected regression `37 PASS / 1 environment SKIP / 0 FAIL`. Evidence: `cpf-docs/work/evidence/current/devgpt/QA24_VSCODE_UTF8_FINAL_STATIC.log`, `cpf-docs/work/evidence/current/devgpt/QA24_VSCODE_UTF8_FINAL_REPLAY.log`.
- **Fresh Windows Java25 VS Code Error 0 / Warning 0 및 실제 Full Runtime log mojibake 0은 사용자 환경에서 재실행 전까지 미검증이다. 이 둘 중 하나라도 실패하면 같은 WP를 즉시 재개방한다.**


## QA24 VS Code 잔여 4건 + Runtime ErrorRecord UTF-8 재개발 — 2026-08-29

- Exact Product Source Identity: `b162d358b4f127f7c4e3a816f89dcc8ab5fc2f66423ec7c37cbbefd6414fde9e` / 8,351 files.
- VS Code: `secure-api/browser-bff -> web-api` 잔여 4건의 source-empty assembly-only project output을 module 하드코딩/fake class/dependency 우회 없이 Gradle user-home의 clean-safe IDE output으로 이동하고 model gate를 유지했다. 실제 Fresh Windows VS Code Error 0 / Warning 0 전에는 verification 완료로 판정하지 않는다.
- Runtime UTF-8: 사용자 실측 `LOCAL_INTEGRATED_LOG_CORRELATION`에서 `Invoke-RestMethod`의 OS-localized ErrorRecord가 `占쏙옙` 형태로 깨지는 것을 재현 근거로 재개방했다. Canonical Full Runtime이 직접 실행하는 PowerShell child 21개 전부에 Input/OutputEncoding과 `$OutputEncoding` UTF-8 계약을 fail-closed로 검증한다.
- `smoke-integrated-log-correlation.ps1`은 HTTP 실패를 raw localized ErrorRecord로 재throw하지 않고 method/URI/status/exception type 기반 stable failure summary로 변환한다. 원 예외는 inner exception으로 보존하고 secret/raw credential은 출력하지 않는다.
- Exact-source 실행: Current PASS, Hygiene PASS, Garbage PASS, UTF-8 boundary PASS (`ProcessStartInfo=28`, `redirected=28`, `Start-Process=10`, `FullRuntime child=21`, `mojibake source=0`, failures=0), Unified CLI PASS, Requirement Progress PASS, Projection PASS, Fresh overlay replay 직접 영향 pytest `37 PASS / 1 environment SKIP / 0 FAIL` (SKIP: Java 25 required to rebuild current-source `cpf-cli.jar`). Evidence: `cpf-docs/work/evidence/current/devgpt/QA24_VSCODE_UTF8_FINAL_REPLAY.log`.
- 이 결과는 Source/Contract 검증이다. **Fresh Windows Java25 Gradle Import의 실제 VS Code Error 0 / Warning 0과 실제 Full Runtime log mojibake 0은 사용자 환경 재실행 전까지 미검증**이다.
