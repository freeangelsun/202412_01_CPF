# CPF 개발 완료서 — C 개발/QA 관리_1

## 1. 기준 Source와 최종 판정

- Baseline Local Working Tree ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260821_151542(1).zip`
- Baseline SHA-256: `324f5d8f33bd59925fcfe4cfcb24772a543cfbf9acbafebe0f6b4b88841a8583`
- Final Source Identity SHA-256: `4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886`
- Final Source-scope files: `8,173`
- Canonical Requirements: `205`
- Source-side Work Packages: `13/13 CLOSED (100%)`
- Development Final Gate: `PASS`
- Exact-baseline Fresh Replay: `PASS`, 동일 Source Identity 재현
- Overall product QA: `NOT COMPLETE` — `EA-01 BLOCKED_EXTERNAL / 미검증`이 남아 있음

## 2. Work Package별 완료 내용

### WP-01 — Root Gradle / Backoffice Domain Classification — CLOSED
MBR/EXS Generated Customer Domain과 MBW prebuilt Backoffice를 분리했다. Root private-source build에서 MBW가 `:cpf-generated-cpf-backoffice`로 잘못 평가되던 분류 경로를 제거하고 classification verifier/mutation을 보강했다. 과거 `cpf-backoffice/build.gradle:16` 실패의 Source-side Root Cause는 닫혔다. Java25 Root Gradle 실실행은 EA-01에 남긴다.

### WP-02 — Runtime Central Registry / Compile Closure — CLOSED
`CpfRuntimeControlPlaneRepository` 중복 canonical method block을 제거하고 Runtime lifecycle/CAS authority를 중앙 Registry로 단일화했다. Batch exact-version contract와 Retention/UNKNOWN/reconcile 검증 자산을 현재 Architecture에 맞췄다. duplicate signature 0, Central Registry/Retention 및 exact-version contract PASS.

### WP-03 — Generator Operation Owner / Generated Compile — CLOSED
`@CpfOnlineTransaction.operationId`와 typed `CpfDomainOperation<I,O>` adapter를 canonical operation owner로 연결했다. CREATE/DETAIL/SEARCH/SLICE/UPDATE/DELETE operation, dependency selector, typed client/service import와 Generated Test를 currentize했다. `MBR_SAMPLE_TX_DETAIL` Fresh Scratch 선택과 Generated Java compile이 PASS한다.

### WP-04 — Starter Optional Capability Zero-Footprint — CLOSED
Persistence/HTTP/Resilience를 명시 선택형으로 분리하고 hidden transitive edge를 제거했다. `framework:web`이 Common 전체를 참조하며 JDBC를 끌던 Error Resolution 계약을 topology-independent `cpf-core` API/SPI로 이동하고 Common은 Provider로 유지했다. minimal / persistence-only / http-only / resilience-only transitive graph와 hidden-edge mutation이 모두 PASS했다.

### WP-05 — Generated Domain Canonical Directory / Java Package IA — CLOSED
Generator Root Owner를 `Domain Project → Runtime Module → Java Base Package → Business Feature → Technical Role` 구조로 currentize했다. `businessFeatures`를 capability와 분리하고 미지정 scaffold는 `sample`을 사용한다. `<domain>.online.<domain>.*`, `<domain>.<domain>.*`, 중복 directory를 제거했다. MBR/EXS clean regenerate, Scratch A~E, multi-feature, batch/dependency/external client, regenerate 2회 idempotency, IA mutation, Generated Java 65 source javac가 PASS했다. Legacy generated 47개는 GENERATED_OWNED 증명 후 Exact Delete Manifest 후보로 관리했다.

### WP-06 — Runtime Instance Identity — CLOSED
`localhost`, `127.0.0.1`, `::1`, `unknown`, `local`, `dev`, `test`, `prod`, blank를 fail-close하도록 currentize하고 독립 executable harness로 검증했다.

### WP-07 — Runtime / Contract Verifier Currentization — CLOSED
Same-JVM Context, System6/Transaction, central CAS, Java substitute, Alias/fixture, generator lifecycle verifier를 현재 Architecture/CLI와 일치시켰다. 제품 계약을 stale fixture에 맞춰 약화하지 않았다.

### WP-08 — Backoffice OpenAPI → Generated Client → Actual Consumer — CLOSED
Backend OpenAPI `96` = BFF route `96` = generated descriptors/functions `96`으로 currentize했다. hardcoded 8-operation client를 제거하고 operationId/method/path/function body와 실제 UI consumer를 semantic verifier로 비교한다. function/route/method-path corruption mutation은 모두 FAIL한다. Full Node/browser runtime은 EA-01.

### WP-09 — Public Release / Fresh Adoption — CLOSED
Public staging READY 전에 mandatory MBR/EXS domain catalog와 physical project를 강제하고 artifact/catalog parity 및 False READY를 보정했다. Release/Public regression과 Fresh Public staging source contract PASS. Reachable remote repository의 Public Binary isolated consumer는 EA-01.

### WP-10 — cpf-common Ownership / Developer Adoption — CLOSED
`cpf-common` Product Owner와 Starter runtime/autoconfiguration composition 경계를 currentize하고 stale `cpf-starters/common` owner/adoption reference를 제거했다. Common DX/Crosscut/Public Javadoc/Top100 canonical gates PASS.

### WP-11 — Source Hygiene / Delete Lifecycle — CLOSED
Baseline의 `.class` 6개, old Generated IA, stale lock mirror와 Delete/Garbage evidence를 exact file 단위로 정리했다. broad directory delete를 금지하고 Delete Manifest/Garbage Decision을 정합화했다. clean-source/repository-garbage/NXT3 layout 및 `.class` mutation PASS. 사용자 Working Tree 삭제는 수행하지 않았다.

### WP-12 — Toolchain / Developer DX Contract — CLOSED
Node `>=22.18 <25`, npm `10.9.2`, Docker Node `22.18.0`을 canonical contract로 맞췄다. PowerShell compatible entrypoint와 pwsh7-only internal boundary를 분리하고 active incompatible API 사용을 제거/currentize했다. Toolchain gate/mutation PASS. 실제 Windows PowerShell/VS Code UI acceptance는 EA-01.

### WP-13 — Canonical Final Gate / Evidence / Fresh Replay — CLOSED
Canonical high-risk child gate와 timeout/fail-close를 통합하고 Evidence semantics를 direct execution + actual artifact SHA + current Source Identity 검증으로 강화했다. Exact baseline → Overlay → replay-only Delete Manifest 적용 후 Source Identity를 재계산해 동일 SHA-256을 재현했다. Canonical `24/24 PASS`, Evidence `13/13 PASS`, Development Final Gate `PASS`, Fresh Replay `PASS`.

## 3. 최종 검증 요약

- Canonical static registry: `24/24 PASS`
- Evidence semantics: `13 verified rows / 13 direct execution documents PASS`
- Development Final Gate: `PASS`
- Fresh Replay: `PASS`
- DB: `157 passed / 0 failed`
- Generator: `37 passed / 10 environment skips / 0 failed` + `6 subtests PASS`
- Release/Public: `31 passed / 0 failed`
- Runtime + Security + Supply-chain: `76 passed / 2 environment skips / 0 failed` + `7 subtests PASS`
- Verification/OpenAPI: `77 passed / 0 failed`
- Testing-tools: `381 passed / 22 environment skips / 0 failed` + `2 subtests PASS`
- Docker-development-test: `6 passed / 0 failed`
- Aggregate Python: `765 passed / 34 environment skips / 0 failed` + `15 subtests PASS`
- Generated Java: MBR `32` + EXS `33` = `65` source javac PASS

환경 skip은 Runtime PASS로 승격하지 않았다.

## 4. Delete lifecycle

- Delete Manifest total: `689`
- `HISTORICAL_ALREADY_ABSENT`: `600`
- `PENDING_USER_EXECUTION`: `89`
- Pending 89건: development `approved=true`, `precondition=SATISFIED`, `user_approved=false`
- Developer GPT가 사용자 Working Tree에서 실제 삭제한 파일: `0`
- 실제 삭제는 `apply_delete_manifest.ps1`에 명시적 `-UserApprovalRef`를 제공한 사용자 실행으로만 수행한다.

## 5. 남은 External Acceptance

`EA-01`은 필수이며 실제 실행 전 전체 제품 QA를 완료로 표현하지 않는다.

1. Java25 Root Gradle full build/test/publication + Generated Domains/Backoffice.
2. Reachable artifact repository 기반 Public Binary isolated consumer.
3. Oracle/PostgreSQL/MariaDB live install → migration → seed → runtime query → upgrade → rollback 및 mixed-vendor binding.
4. Same-host multi-process/Multi-WAS, collision, process kill, lease expiry, restart/reconcile, UNKNOWN recovery.
5. ADM/Backoffice Chromium/Firefox/WebKit 실제 Browser E2E.
6. Windows PowerShell/VS Code Fresh Workspace 실제 import/index/UI 검증.

현재 assistant 환경은 Java `21.0.11`, Node `22.16.0`이며 Gradle 9.1.0 wrapper 다운로드가 `services.gradle.org` DNS 제약으로 실행되지 않았다. 이 미실행 항목을 PASS로 기록하지 않았다.

## 6. 최종 상태

- Development-environment implementable scope: **100% / 13 of 13 CLOSED**
- Development Final Gate: **PASS**
- Exact-baseline Fresh Replay: **PASS**
- Source Identity: **`4572fd3659d076f230cbe2aa0284a5835a4f914e1f0a0cb4823b20c53b724886` / 8,173 files**
- Overall product QA: **NOT COMPLETE — EA-01 BLOCKED_EXTERNAL**
- Git write: **NONE**
- User Working Tree delete: **NONE**
