# CPF Codex Continuity State

## Current authoritative snapshot — 2026-08-02 post-push reconciliation

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- confirmed `origin/master`: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- local Working Tree: `재확인 필요` — this review used remote Git state, not the user's local filesystem
- active package: `POST-QA37 integrated remediation / next Codex QA38`
- Source status: `부분 구현`
- exact-SHA final verification: `미검증`
- Oracle/PostgreSQL/MariaDB actual Fresh Lifecycle: `미검증`
- Kafka/JMS/IBM MQ/RabbitMQ/TCP runtime: Kafka focused source tests exist; full provider/runtime matrix `미구현 또는 미검증`
- Browser 3-engine, Toxiproxy, OTel, multi-instance, supply-chain at latest SHA: `미검증`
- `cpf-starters/`: official fixed product root
- permanent DB rule: Canonical/Generator first; each vendor starts from a dedicated CPF QA DB/Schema with CPF Object count 0
- permanent Git rule: no Codex commit/push without explicit user approval

### Supersession notice

The historical QA37 body below records valuable focused PASS and defect history at `1eda8e12...` and a dirty WIP. Multiple user pushes and later documentation/consolidation commits produced `38089a96e3f4c7c2ba05cda549785b47f67cd462`. Therefore those results are inputs for impact analysis, not current exact-SHA completion evidence.

### Next exact work

1. Verify local `HEAD == origin/master`, clean/dirty state and all commits since `1eda8e12...`.
2. Merge the external QA37 defect/execution ledgers into the repository verification history without claiming missing logs as PASS.
3. Finish P0 Core-to-Starter source migration and real Consumer conversion.
4. Implement MQ/JMS/IBM MQ/RabbitMQ/TCP provider requirements.
5. Complete Generator Profile/Aggregate/BOM/Domain lock.
6. Fix official Fresh DB lifecycle orchestration before starting any DB.
7. Run one vendor at a time from object count 0, then Java/Frontend/Runtime/Fault/Browser/Supply-chain.
8. Update Matrix/Evidence and final exact-SHA state.

---

## Historical QA37 state retained for traceability

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 HEAD / `origin/master`: `1eda8e12fe123281748a4388938c62f11819da1e`
- 작업 환경: 집 PC Windows / PowerShell, Java 25.0.3, Gradle 9.1.0, Docker 29.6.2
- Active work package: `QA37`
- 현재 작업 단계: `01_STATIC Source Closure 보완` — ADM/BZA/Gateway/QA30/QA31/QA32/Enterprise 정적 Gate 통과, Source Closure의 Windows·WIP 계약과 Starter 제품 편입 잔여 보완 중
- Source overall status: `부분 구현`
- Repository full verification: `미검증`
- DB/Runtime/3DB/Kafka/Browser/Multi-instance/Supply-chain: `미검증`
- Commit/Push: `미구현` — 현 QA37 요청서에 따라 Codex가 수행하지 않음
- Worktree: `부분 구현` — QA37 보완 변경이 누적된 Dirty WIP이며 임의 reset/revert/stash/clean 금지

## Current Canonical Pointers

1. `cpf-docs/work/current/CPF_CODEX_FINAL_FULL_VALIDATION_AND_REMEDIATION_REQUEST_20260729.md`
2. `cpf-docs/work/current/CPF_CODEX_1ST_FULL_VALIDATION_AND_REPAIR_REQUEST_20260726.md`
3. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
4. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
5. `cpf-docs/governance/CPF_NO_PARTIAL_IMPLEMENTATION_COMPLETION_STANDARD.md`
6. 외부 실행 원장: `C:\dev\Docker\CPF\output\codex\qa37\execution-ledger.csv`
7. 외부 결함 원장: `C:\dev\Docker\CPF\output\codex\qa37\defect-ledger.csv`

## 완료한 작업

- `완료`: HEAD와 `origin/master`가 기준 SHA와 동일함을 확인하고 Dirty WIP를 보존함.
- `완료`: exact SHA Preflight PASS. Evidence JSON은
  `C:\dev\Docker\CPF\output\codex\qa37\preflight\cpf-codex-preflight-1eda8e12fe12-20260802_011326.json`,
  SHA-256은 `7674bba288f0ee3b9fa79d987771f1ed940ed0ea02baf33f99414e7062`.
- `완료`: QA37 Stage Wrapper가 exact HEAD/Command/Worktree/환경/Log/Artifact hash를 검증하도록 보완하고 focused test 13/13 PASS.
- `완료`: Source Closure/Secret Scanner/Generator BOM/Platform BOM/Composite Build 결함을 보완하고 해당 focused 검증 PASS.
- `완료`: `cpf-core` test compile 및 영향 9개 class 36/36 PASS.
- `완료`: `cpf-batch:execution-runtime` Spring Batch 6 보정, compile PASS, 전체 test 14/14 PASS, QA33 control-plane gate PASS.
- `완료`: REF legacy batch package를 canonical optional batch package로 이동하고 feature-isolation gate PASS.
- `완료`: REF module-local 임시 DDL fixture를 제거하고 Canonical Seed Metadata에 Mapper test data를 등록한 뒤 MariaDB/PostgreSQL/Oracle 공식 Test Seed로 생성함.
- `완료`: REF Center-Cut inline MariaDB SQL을 중앙 `ref` Vendor Runtime Query Pack으로 이관. 13 key × 3 Vendor 정적 parity PASS.
- `완료`: 중앙 Runtime Query Pack에서 누락된 활성 CPF/BZA query 5개를 Canonical contract/template에 복구하고 sync/check PASS.
- `완료`: `cpf-starter-security` Boot 4 BFF Filter/Authentication/Binding/Jackson ownership을 보완하고 전체 26/26 tests PASS.
- `완료`: BZA의 BFF single-flight refresh, stale-session 격리, credential 비저장 계약을 구현하고 focused 4/4 tests PASS.
- `완료`: BZA controller-source OpenAPI 84개 Operation과 26개 Route Operation 계약을 생성·검증하고 transport 재귀 누출을 차단함.
- `완료`: pinned npm 10.9.2 기준 BZA `verify` 전체 PASS. Lock/License/SBOM/OpenAPI/Generator/Consumer/Lint/Typecheck/Vitest 13/13/Production Build/35-file Bundle Manifest가 모두 Exit 0이며 Bundle SHA-256은 `4a4ae80879806b8344a3d5d3e2cd2c5c236379892311383a432a0875721bc72b`.
- `완료`: `cpf-batch:control-server`의 Jackson/Spring 7 API/승인 컨텍스트/배포 canonical hash와 stale test contract를 보완하고 전체 34/34 tests PASS.
- `완료`: `cpf-common`의 Public response API, Redis/MariaDB test ownership, POI close lifecycle을 보완하고 신규 XLSX streaming round-trip을 포함한 전체 29/29 tests PASS.
- `완료`: BAT Runtime Query를 현재 Spring Batch Primary Engine과 Execution Runtime Consumer 기준으로 재정렬. 삭제된 Custom Worker Query 18개와 미사용 Scheduler Insert 1개를 제거하고 Execution Runtime 19개를 중앙 Catalog로 이관하여 최종 210 Statement × 3 Vendor, Generated SQL 630개, Java inline SQL 0건을 확인함.
- `완료`: Canonical Schema 46에 누락됐던 `cpf_batch_approved_launch`, `cpf_batch_execution_control`, `cpf_batch_execution_link`를 추가하고 기존 Epoch와 함께 BAT 제어 4 Table을 정본화함. 3 Vendor BAT Source는 각 56 Table이며 Append-only V95로 Historical Upgrade를 보정함.
- `완료`: Generated Domain DB 계약을 임의 8 Table이 아닌 공통 Sample 1 + Idempotency Ledger 1의 정확히 2 Table로 복구하고 존재하지 않던 Operation Template 참조를 제거함. Golden Path와 `cpf-member` Generator-owned Database parity PASS.
- `완료`: 공식 Rollback Root를 `cpf-tools/db/vendor/<vendor>/rollback`으로 단일화하고 PostgreSQL/Oracle 및 REF WIP의 중복 `migration/rollback` 56개 Resource를 내용 보존 이동한 뒤 빈 중복 Root를 삭제함.
- `완료`: Historical V69/V74의 Empty-string NOT NULL과 Canonical nullable 의미 Drift를 Metadata-first V96 Generator로 보정. 30 Column × 3 Vendor Migration/Rollback과 Checksum을 생성했으며 Oracle R96은 표현 불가능한 과거 상태로 데이터 변조하지 않고 fail-closed함.
- `완료`: QA30가 `.git`/ignored Build/IDE Cache를 Source로 오인하던 결함, 낡은 BAT/Gateway Anchor, Test Sentinel Secret 오탐을 보정하고 현재 WIP 전체 정적 Gate `errorCount=0` PASS.
- `완료`: 폐기된 Core 내부 Gateway Route/Catalog/Authorization 중복 모델과 Test를 제거하고 공개 `com.cpf.core.api.gateway` 계약만 유지함. Release Signer도 정규 Java package path로 이동하고 직접 `javac --release 25` PASS.
- `완료`: Gateway를 SCG MVC `CpfScgPrimaryHandler`/`CpfScgPrimaryRouteConfiguration` 단일 Primary로 정리하고 legacy Controller/ProxyService/Transport를 제거함. Recovery Spool의 OffsetDateTime 직렬화 offset 보존 결함을 고쳐 Gateway 전체 35/35 tests PASS.
- `완료`: QA30/QA31/QA32 Gate를 현재 SCG Primary와 Windows UTF-8/LF canonical hash/current-worktree scope에 맞게 보완. QA30 PASS, QA31 96 checks/0 failures, QA32 32,341 checks/0 failures.
- `완료`: Generator 실행 결과를 Generated Domain 제품 Root가 아닌 `build/reports/create-domain/<module>/create-domain-result.json`으로 중앙화하고 `cpf-member`를 official sync 35건으로 재생성한 뒤 parity/Golden Path/Hygiene PASS.
- `완료`: ADM/BZA의 raw operational JSON `<pre>` 표시를 bounded depth/item 및 sensitive masking을 제공하는 `CpfStructuredData`로 표준화하고 양쪽 component test/typecheck/lint와 Integrated Architecture/UI/Hygiene Gate를 통과함.
- `완료`: Enterprise Closing Static Gate PASS — Ownership, Consumer, Frontend Route, Hygiene, Public Boundary, 3 Vendor migration checksum, MariaDB source/runtime migration·rollback parity 포함.
- `완료`: `cpf-starters`를 Final Artifact Catalog 기반 7개 정식 제품 Root/Artifact로 fixed-root, Local/Staging/Internal publication, Platform BOM, artifact propagation, ownership/taxonomy/public-boundary/inventory에 완전 편입. Kafka Starter test와 관련 Gate PASS.
- `완료`: R10 Product Gate의 stale exact prose와 ignored output recursive scan을 semantic policy + Git source inventory로 보완하여 전체 Gate를 9.6초에 PASS.

## 진행 중인 작업

- `완료`: REF QA32-061 typed contributor/metadata scan/bounded process output focused 8/8 PASS 후 전체 1,016 tests 중 실제 실행 1,014건 PASS. DB 환경 조건부 2건은 `미검증` 유지.
- `완료`: `cpf-batch:worker` Spring Batch 단일 Primary Engine 전환과 STDIN JSON stream 보완 후 전체 38/38 tests PASS.
- `완료`: `cpf-batch:runtime-common` direct dependency/Boot 4 `RestClientCustomizer` 보완 후 compile 및 10/10 tests PASS.
- `완료`: BAT 210개 canonical runtime statement를 3 Vendor 630개 Resource로 sync/check하고 inline SQL 0건, Query Contract integrity, Execution Runtime 19/19, Control-server 34/34, Scheduler 전체 test, QA30/QA33를 통과함.
- `완료`: ADM에도 BZA 공통 Orval mutator, license/SBOM, Vitest 경계, bundle manifest, route traversal 보정을 반영하고 전체 frontend verify 및 추가 structured component test/typecheck/lint를 통과함.
- `완료`: `cpf-core`의 QA32 unbounded test stream 3건을 bounded read로 보완하고 focused 10/10 tests PASS.
- `완료`: Center-cut legacy independent runtime/dispatcher를 제거하고 Spring Batch StepHandler/execution-bound claim/fencing/lease/runtime-state 경계로 이관. Center-cut 7/7와 Execution Runtime 15/15 tests PASS.
- `완료`: Spring Batch 6 제거 예정 `JobExplorer`를 `JobRepository` read 계약으로 교체하고 repository-backed reconcile 테스트를 추가함.
- `부분 구현`: QA37 결함 원장 `QA37-CODEX-001` 이후 Source Defect를 root cause 단위로 기록 중.
- `부분 구현`: Source Closure가 의도적 tracked deletion/merged overlay remediation을 오판하던 계약과 Windows `javac` command-line 한계를 보완했으며 focused unittest 25/25 및 EDU135 독립 compile/runtime PASS. `cpf-starters` publication/BOM/Architecture Gate 편입을 보완 중.

## 아직 시작하지 않은 작업

- `미검증`: 보완 완료 후 exact command hash로 `01_STATIC` 상위 Lifecycle 1회 재실행.
- `미검증`: Java 25 전체 clean build/test 및 Optional Profile.
- `완료`: ADM/BZA Frontend 현재 Worktree 기준 전체 verify 및 추가 structured component 검증.
- `미검증`: DB Static 전체 Gate와 MariaDB → PostgreSQL → Oracle 순차 Lifecycle.
- `미검증`: 기존 MariaDB Drift/Migration/Upgrade와 분리된 신규 DB Clean Install.
- `미검증`: Runtime/Kafka/Multi-instance/Fault Injection/OTel/Browser.
- `미검증`: Supply-chain, Repository Hygiene, Truth/Evidence/Handover 최종 동기화.

## 변경 중인 주요 파일/모듈

- `cpf-tools/scripts/verify-cpf-qa37-source-closure.py`, `invoke-cpf-codex-stage.ps1` 및 focused tests
- `settings.gradle`, root `build.gradle`, `cpf-tools/build/platform-bom/build.gradle`
- `cpf-core` logging/public service-call API 및 영향 tests
- `cpf-reference` build, Batch 6 sample, attachment/file consumer, Center-Cut repository/tests
- `cpf-tools/db/canonical/seed-model.json`
- `cpf-tools/db/metadata/platform-runtime-query-contract.json`
- `cpf-tools/db/runtime-template/{cpf,bza,ref}` 및 3 Vendor generated runtime/source/lifecycle SQL
- `cpf-tools/db/metadata/platform-nullable-empty-string-repair.json`, `sync-platform-nullable-empty-string-repair.ps1`, 3 Vendor V96/R96
- `cpf-tools/db/vendor/{mariadb,postgresql,oracle}/rollback` — 공식 단일 Rollback Root
- `cpf-batch:execution-runtime`, `cpf-batch:runtime-common`, `cpf-batch:worker`
- `cpf-batch:control-server`, `cpf-batch:scheduler`, `cpf-common` 및 영향 tests
- `cpf-tools/scripts/test-cpf-qa32-negative-fixtures.py` — transient cache 제외 및 clean baseline 선검증
- `cpf-starters/security` BFF filter chain/Boot 4 binding/tests
- `cpf-biz-admin/frontend` OpenAPI/route generator/BFF session/test/build pipeline
- `cpf-admin/frontend` BZA와 공통 generator/mutator/license/test/build 보정 — 전체 verify 및 structured component 검증 완료
- `cpf-admin/frontend`, `cpf-biz-admin/frontend`의 `CpfStructuredData.vue`/tests와 raw operational result 화면
- `cpf-gateway` SCG MVC 단일 Primary 및 recovery spool
- `cpf-tools/scripts/verify-cpf-qa37-source-closure.py`, `verify-cpf-qa37-manual-edu-135.py`, `verify-cpf-reference-feature-removal.py`와 회귀 test
- `cpf-starters`, root publication aggregate, Platform BOM, Architecture/Inventory Gate — 정식 제품 Root 전체 편입 보완 중

## 실제 실행한 검증

- `완료`: Preflight exact SHA PASS.
- `실패`: 최초 `01_STATIC` 실행은 legacy REF batch package에서 중단. Log:
  `C:\dev\Docker\CPF\output\codex\qa37\logs\01_STATIC-20260802_021516_458-37020.log`,
  SHA-256 `8394185c5e79ae09c6c9b9962da9f4531b2372d968ba194c608ec4998f47585c`.
- `완료`: Source Closure unit 24/24 및 QA34 포함 27 tests PASS; repository safe secret probe `files=6428`, finding file 0.
- `완료`: Core targeted 36/36 PASS.
- `완료`: Generator QA34 BOM contract 3/3, golden path, composite identity, Platform BOM publication contract PASS.
- `완료`: `:cpf-batch:execution-runtime:test` 14/14 PASS.
- `완료`: `:cpf-batch:runtime-common:test` 10/10 PASS.
- `완료`: REF feature-isolation PASS.
- `완료`: `:cpf-reference:clean :cpf-reference:compileJava` 및 focused 047~049 tests 4/4 PASS; removal 예정 API 경고 제거.
- `실패`: REF 전체 test 1,013건 중 2건 실패, 2건 DB 환경 조건부 skip. 실패 2건은 054/055로 등록해 구현 보완 중이며 skip은 실제 DB 검증 완료로 승계하지 않음.
- `완료`: REF file-transfer checksum focused test 2/2 PASS. 공개 `CpfFileRequest` checksum 계약에 맞춰 fixture 보정.
- `완료`: Platform Runtime Query sync/check 218 statements / 654 generated repository files PASS; REF 13 × 3 Vendor parity PASS.
- `실패`: `check-query-contract-integrity`는 REF가 아니라 BAT orphan/inline SQL 계약 결함에서 중단했으며 046/050~053으로 보완 중.
- `완료`: QA32 negative fixture Python AST 및 `git diff --check` PASS. 실제 negative lifecycle은 QA32 Primary Engine 기준선 결함을 먼저 닫은 뒤 1회 실행.
- `완료`: `:cpf-starter-security:test` 전체 26/26 PASS.
- `완료`: BZA focused BFF recovery tests 4/4 PASS.
- `완료`: BZA `corepack npm run verify` 전체 PASS — OpenAPI 84, Route 26, Consumer 78, eslint 0 findings, typecheck PASS, Vitest 13/13, production bundle 35 files.
- `완료`: Core bounded attachment/ZIP manifest/GZIP verification focused tests 10/10 PASS.
- `완료`: REF 최신 전체 lifecycle PASS — 720 suites, 총 1,016 tests, 실행 1,014, 실패 0, 오류 0, skip 2. Skip은 `ReferenceCenterCutAdapterTest`, `ReferenceQueryEducationMapperSliceTest`의 DB 조건부 항목.
- `완료`: `:cpf-batch:execution-runtime:test :cpf-batch:center-cut-runner:test` 결합 lifecycle PASS — 각각 15/15, 7/7, 실패/오류/skip 0.
- `완료`: BAT Runtime Query sync/check PASS — 210 statements, MariaDB/PostgreSQL/Oracle 3 Vendor, 630 generated files, 4개 BAT owner scope inline SQL 0건. Query Contract integrity와 checker unittest 2/2도 PASS.
- `완료`: `:cpf-batch:control-server:test` 전체 34/34 PASS — SQL Catalog consumer, 승인 컨텍스트 전달, canonical deployment hash, lock-store UNKNOWN_RESULT 포함.
- `완료`: `:cpf-common:test` 전체 29/29 PASS — XLSX streaming round-trip 신규 regression 포함.
- `완료`: `:cpf-batch:scheduler:test` 전체 Gradle Task PASS 및 `:cpf-core:compileJava` PASS (Java 25, `--no-daemon --max-workers=2`).
- `완료`: QA33 Batch Control Plane Gate PASS. Evidence Log:
  `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa33-batch-control-plane-rollback-root-20260802_115227.log`, SHA-256 `947b5d224cb74057fc186f2d226c5afbc59ad29728cdc5f6cc2974607127a052`.
- `완료`: 공식 DB Artifact 전체 Pipeline PASS — Canonical 192 Table, Platform Runtime 218 × 3 Vendor, BAT Runtime 210 × 3 Vendor, V95/V96, Checksum, Schema Drift, Profile, Generated Domain parity. Evidence Log:
  `C:\dev\Docker\CPF\output\codex\qa37\targeted\database-artifact-sync-v96-20260802_120232.log`, SHA-256 `d19b64bf07a8970c09de41a8e124c247237fd5b64c9e369e0dc25226de9e799b`.
- `완료`: QA30 현재 WIP 전체 Static Gate PASS (`errorCount=0`, Canonical 192/Schema 46, 3 Vendor Source 각 192). Evidence JSON/Log:
  `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa30-static-remediated-20260802_120440.json`, SHA-256 `3dd47aa4e2c7816510b45a4590554e052b85efcf97f5331a56ae3a87e4e98e84`.
- `완료`: Gateway 전체 35/35 PASS. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\gateway-full-test-pass-candidate-20260802_133017.log`, SHA-256 `88583035ff74dc081145a55412655c4239bbbb73b52a6be92788f318ac33af28`.
- `완료`: QA32 전체 32,341 checks/0 failures. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa32-primary-engines-pass-candidate-20260802_133131.log`, SHA-256 `4ecd17f99002c5d03e00d86c84badbe61e272861d0f7ddfb5095e05101897d1f`.
- `완료`: 최신 QA30 Gate PASS. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa30-completion-pass-candidate-20260802_133410.log`, SHA-256 `3dd47aa4e2c7816510b45a4590554e052b85efcf97f5331a56ae3a87e4e98e84`.
- `완료`: QA31 96 checks/0 failures. Evidence Log `C:\dev\Docker\CPF\output\codex\qa37\targeted\qa31-development-pass-candidate-20260802_133737.log`, SHA-256 `d0a27338c3080761853e90d078c8e92a2f0cc5221373b77d8921f504416b8d1b`; JSON SHA-256 `3f22af278e361ac1b98d8cf8bad3c37199e70aa2e43e28467186abedc2b3d560`.
- `완료`: ADM/BZA `CpfStructuredData` focused tests, typecheck, lint PASS; raw `<pre>` scan 0건. Integrated Architecture/UI/Hygiene와 Enterprise Closing Static Gate Exit 0.
- `완료`: Source Closure 회귀 unittest 25/25 PASS, REF optional contributor removal Gate PASS, EDU135 independent compile/runtime PASS(135 normal/validation/authorization/duplicate/recovery, 24 exhaustive failure, 6 persistence/concurrency).
- `재확인 필요`: BZA build는 Exit 0이나 initial JS chunk 1,070.34 kB(gzip 344.64 kB)에 대한 Vite 500 kB 성능 경고가 있음. 기능 실패나 IDE compile error로 기록하지 않으며 Frontend 성능 단계에서 실제 budget 기준으로 판정한다.

## DB / Runtime 현재 상태

- `재확인 필요`: 현재 로컬 MariaDB의 기존 CPF Schema/데이터 상태는 QA37 DB Stage 시작 직전에 read-only Drift로 확인해야 함.
- `완료`: 이번 Checkpoint까지 기존 로컬 DB에 DDL/DML/Reset/Drop을 실행하지 않음.
- `완료`: Clean Install은 기존 DB와 분리된 신규 Database/Schema로만 검증한다는 원칙 유지.
- `완료`: 3 Vendor Canonical Source/Install/Migration/Rollback/Runtime Query/Checksum/Drift 정적 Pipeline은 통과함.
- `미검증`: MariaDB/PostgreSQL/Oracle 실제 Lifecycle 및 Runtime.
- `재확인 필요`: Docker Engine은 가용하나 현재 실행 중 CPF Container는 0개이고 `cpf-mariadb`는 `Exited(0)`. 로컬 3306/Windows MariaDB Service는 탐지되지 않아 실제 DB Stage는 공식 Docker `mariadb` 최소 Target을 사용해야 함.

## Blocker와 미검증

- `완료`: REF/Center-cut/Core/BAT/ADM/Gateway 및 QA30/QA31/QA32 영향 검증 PASS.
- `완료`: `cpf-starters` 정식 Root의 publication/BOM/Architecture/Inventory 편입과 focused 상위 Gate PASS.
- `부분 구현`: 공식 DB Tool이 `refDB/V93,V94`와 `rollback/refDB/U93,U94`, optional expected schema, REF runtime query, live different-hash conflict를 연결하지 못하는 `QA37-CODEX-124`를 보완 중. 수동 SQL로 우회하거나 DB를 먼저 기동하지 않음.
- `부분 구현`: QA32가 quoted/comma PowerShell argument의 `-ExecutionPolicy`, `Bypass`를 놓쳐 active canonical consumer 8개 파일 70곳이 false-green이 된 `QA37-CODEX-125`를 보완 대기. `AllSigned`와 과거 실행 Evidence는 변경하지 않음.
- `완료`: 대규모 Canonical projection과 V95/V96를 포함한 3 Vendor Source/Lifecycle diff는 전체 Artifact Sync/Drift/Parity Gate PASS.
- `미검증`: Docker DB/WAS/Kafka/Browser 외부 Lifecycle은 아직 시작하지 않음.

## 다음 정확한 작업 순서

1. 공식 optional DB Lifecycle의 migration/rollback discovery, expected schema overlay, REF runtime query와 different-hash conflict Consumer를 Canonical-first로 보완하고 3 Vendor 정적/dry-run test를 통과시킨다.
2. Active Gradle/Generator/DB/Final Verification Consumer에서 ExecutionPolicy Bypass 인자만 제거하고 QA32/QA33 탐지식과 negative fixture를 보강한다.
3. Repository Source Closure를 재실행하여 다음 최초 Root Cause가 있으면 영향 범위만 보완한다.
4. 모든 관련 Source Defect가 targeted PASS가 된 뒤 아래 exact command hash로 `01_STATIC -AllowRerun`을 한 번만 실행한다.
   - Hash: `bd9eac218f4b7e105a5801abdf266536615d98d1589d3afb83d9bae531954482`
   - Command: `git diff --check` → 실패 시 종료 → `python .\cpf-tools\scripts\verify-cpf-qa37-source-closure.py --root .` → exit code 전달
5. `01_STATIC` PASS 후 기존 MariaDB Read-only Drift와 분리 Clean Install/V93/V94/Runtime/Rollback/Reapply를 공식 Docker `mariadb` 최소 Target으로 실행한다.
6. MariaDB PASS 후 canonical plan의 첫 미완료 Stage부터 계속한다. 유효한 exact PASS만 생략한다.

## 다시 수행하면 안 되는 작업 / 확정 사항

- `cpf-starters/`는 기준 SHA와 Product Surface Policy/Architecture/Release Artifact Catalog가 일치하는 정식 선택 Runtime 제품 Root이다. `cpf-tools/`로 이동하거나 미사용처럼 삭제하지 않는다. 7개 하위 모듈 모두 Boot AutoConfiguration 제품이며 Source 빈 폴더는 없음.
- Generator 결과 JSON은 제품/Generated Domain Root에 보관하지 않고 중앙 ignored `build/reports/create-domain/<module>`에서만 관리한다.
- ADM/BZA 운영 객체는 raw JSON `<pre>`로 노출하지 않고 bounded/masked structured renderer를 사용한다.
- Gateway Primary는 SCG MVC Handler/Route Configuration 하나이며 제거한 legacy Controller/ProxyService/Transport를 복원하지 않는다.
- exact SHA Preflight는 유효 PASS이므로 관련 입력이 바뀌지 않는 한 재실행하지 않는다.
- 이미 PASS한 Core focused 36 tests와 execution-runtime 14 tests를 무의미하게 반복하지 않는다.
- 삭제가 확정된 `JobPackDispatcher`/`JdbcWorkerExecutionRepository`를 복원하지 않는다. Spring Batch가 유일한 Primary Execution Engine이다.
- module-local REF DDL fixture와 MariaDB 전용 test fixture pack을 복원하지 않는다.
- Vendor SQL부터 수동 수정하지 않는다. Canonical Schema/Metadata/Template에서 생성한다.
- Historical Migration 본문/checksum을 수정하지 않는다.
- Generated Domain의 정본 DB 구조는 업무 이름과 무관하게 Sample Table 1 + Idempotency Ledger 1의 정확히 2 Table이다. 존재하지 않는 Operation Template이나 고정 Domain 목록을 다시 추가하지 않는다.
- 공식 Rollback Consumer Root는 `cpf-tools/db/vendor/<vendor>/rollback`이다. 삭제한 중복 `migration/rollback` Root를 복원하지 않는다.
- V69/V74 Historical SQL은 변경하지 않는다. Canonical nullable 의미는 Metadata-first Generated V96으로만 보정하며 Oracle R96의 fail-closed 복구 정책을 임의 Sentinel 변환으로 바꾸지 않는다.
- MBR/ACC/REF/PAY 같은 Domain/SystemCode 고정 목록을 Generator/DB Tool에 추가하지 않는다.
- 과거 SHA/다른 Command/다른 환경 PASS를 현재 QA37 성공으로 승계하지 않는다.
- 사용자 데이터가 있는 기존 DB를 reset/drop하지 않는다.
- Codex는 현 QA37 요청서에 따라 commit/push/branch/tag/PR을 수행하지 않는다.
