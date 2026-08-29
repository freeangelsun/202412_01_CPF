# CPF Development Completion Review — Current

## 판정

- **Source/Static Rework: 완료**
- **Physical Verification / QA Final Completion: 미완료**
- Current Product Source Identity: `b162d358b4f127f7c4e3a816f89dcc8ab5fc2f66423ec7c37cbbefd6414fde9e` / 8,351 files / 43,569,116 bytes
- Input Working Tree ZIP SHA-256: `52d807563afb9268ef176d89dff8b4721e1a1c5b6475fb8ae0f84e269c6d3886`
- Developer Requirement Ledger: 218 rows
- QA24 Closure Inventory: 222 rows (218 Requirement + 4 Final Meta Gate)

## 2026-08-29 Runtime/VS Code 재개방 Root Cause

1. Runtime UTF-8: Source 파일은 UTF-8이어도 PowerShell→Java/Python/child process→capture/log 경계에서 mojibake가 발생했으며 기존 Gate가 실제 출력 손상을 fail-closed 하지 못했다.
2. VS Code/Buildship/JDT: Source가 0개인 dependency-only/assembly-only Java project는 `compileJava=NO-SOURCE`라 Fresh Import에서 canonical `build/classes/java/main`이 물리 생성되지 않지만 JDT project model은 해당 output을 요구해 missing-library marker를 만들었다. 모듈명 하드코딩·fake class·dependency 우회 없이 Root Gradle convention이 **실제 Source가 0개인 Java project만 동적으로 발견**하여 project configuration/Tooling API model 반환 전에 canonical compile output directory를 materialize한다. `cpfVerifyIdeClasspathModel`은 explicit repair task보다 먼저 이를 검증하며 `clean` 이후에는 `cpfPrepareIdeClasspath`가 같은 generated output만 재생성한다.
3. Observability: FAILURE 거래가 response-body-save=false일 때 canonical error envelope까지 제거되어 Java test가 실패했다. 실패 응답은 민감정보 없는 canonical error envelope를 유지하도록 수정했다.
4. Unified CLI/Generator: Windows/Linux launcher 실행 방식, current source-bound CLI JAR, public command catalog, Generator PowerShell child-process UTF-8가 불일치했다.
5. Tool EntryPoint: `cpf-docs/work/evidence/generated/**`를 제품 entrypoint inventory로 오인해 stale/missing이 누적됐다. Evidence/generated는 inventory source에서 제외했다.
6. DB: Current DB4에서 제거된 referenceFixture/refDB 파일을 semantic verifier/admin safety gate가 과거 exact parity로 요구했다. Current 231-table DB4 canonical contract로 현행화했다.
7. Local Runtime: 폐기된 `:cpf-local-runtime:bootJar`를 호출했다. canonical `:runtime:local:bootJar`로 교정했다.
8. Batch: two-worker smoke가 retired `cpf/cpf` 계정을 사용해 MariaDB access denied가 발생했다. canonical `cpf_app` + secret-based runtime password로 교정했다.
9. Gateway/BAT Runtime: 실패 원인과 stderr가 버려지고 깨진 한국어 예외만 노출됐다. failure classification/root cause/stdout/stderr tail을 보존하도록 보강했다.
10. Frontend Toolchain: 계약 위반 시 구체적인 Node/npm actual 값을 남기지 않았다. `node=<actual> npm=<actual> required=...`를 Evidence에 남기도록 보강했다.
11. Managed/Evidence provenance: source-bound CLI JAR 생성이 managed-state snapshot 뒤에 수행되어 self-drift를 만들었다. CLI build를 managed-state snapshot 전에 수행하도록 순서를 교정했다.

## Current Source에서 실제 재검증한 결과

- Final low-cost gate: **모두 PASS** — Current Final, Hygiene, Garbage, Runtime UTF-8, Physical DB4, Unified CLI, Requirement Progress/Projection.
- VS Code/Gradle model + dependency/local-runtime regression: **37 PASS / 0 FAIL**.
- Verification tests: **104 PASS / 0 FAIL**.
- Testing Tools: **400 PASS / 23 SKIP / 0 FAIL**. SKIP은 환경 의존 항목이며 PASS로 승격하지 않았다.
- DB tests: **142 PASS / 2 SKIP / 0 FAIL**; DB verification: **87 PASS / 0 FAIL**.
- Runtime tools: **73 PASS / 2 SKIP / 0 FAIL**.
- Security tools: **8 PASS / 0 FAIL**.
- Release/Open Git/Public contract: **62 PASS / 0 FAIL**.
- Generator: **48 PASS / 10 SKIP / 6 subtests PASS / 0 FAIL**.
- Docker development-test contract: **8 PASS / 0 FAIL**.
- Tool EntryPoint inventory는 VS Code rework 신규 task/test까지 scanner로 재생성하여 **978 entries**, unified tooling inventory **832 entries / duplicate 0 / dead 0**으로 현행화했다.
- Fresh Windows Java25 Gradle Import의 실제 VS Code Problems **Error 0 / Warning 0은 아직 물리 미검증**이며 Source/contract PASS로 대체하지 않는다.

## 실행하지 못한 필수 Physical Acceptance

현재 실행 컨테이너는 Java 21이며 Gradle distribution network 접근이 없어 Java25 Root Build를 실행할 수 없다. `cpf verify all`은 Java25 필수 계약에 따라 exit 69로 정상 fail-closed했다. 이 항목을 PASS로 기록하지 않는다.

또한 Windows Fresh VS Code Import, Docker DB3, Batch process-kill, One-WAS, Browser, Performance, Actual Open Git Fresh Release, Same Source Fresh Replay는 사용자 Windows Java25/Docker/Browser 환경에서 실행해야 한다. 각 Requirement의 상태·Evidence·재실행 조건은 `CPF_QA24_DEVELOPMENT_CLOSURE_INVENTORY.csv` 222행에 개별 기록했다.

## 최종 완료 조건

Full Runtime에서 `FAIL=0`, mandatory `SKIP_ENV=0`, mandatory `NOT_EXECUTED=0`, unresolved `UNKNOWN=0`, VS Code `Error=0 / Warning=0`, mojibake=0, Source drift=0, Managed drift=0, Same Source Fresh Replay PASS가 모두 충족되어야 QA 전체 완료다.
