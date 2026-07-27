# CPF Codex / ChatGPT Continuity State

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 작업 시작 HEAD: `e725ed3f1bc203e28ff6f06c62a69583358d3b6a` (`20260726_05`)
- 작업 시작 `origin/master`: `e725ed3f1bc203e28ff6f06c62a69583358d3b6a`
- Ahead/Behind: `0/0`
- 작업 PC/환경: HOME Windows, PowerShell/pwsh, JDK 25, Local MariaDB
- 현재 요청: `cpf-docs/work/current/CPF_CODEX_1ST_FULL_VALIDATION_AND_REPAIR_REQUEST_20260726.md`
- 현재 단계: `부분 구현` — MariaDB/BAT 기준선과 Core/CMN/REF/MBR/ACC/Gateway/ADM/BZA Test를 확보했고 나머지 Module·Query Pack 검증 진행
- 기록 시각: `2026-07-27 Asia/Seoul`

이 파일은 회사 PC, 집 PC, Codex, GPT/ChatGPT 사이의 현재 작업 인수인계 정본이다. 과거 세션 History보다 최신 Git, 실제 실행 결과와 이 파일을 우선한다.

## 완료한 작업

- `git fetch origin` 후 HEAD, `origin/master`, Branch, Ahead/Behind와 Worktree를 확인했다.
- 시작 시 Dirty 항목은 사용자가 제공한 현재 요청서 1개가 untracked인 상태뿐이었으며 보호했다.
- 최상위 요구사항, Current/Next Request, 통합 검증 계획, 기존 Handover/Decision Log, 289건 결함 감사와 관련 정본을 다시 읽었다.
- `gradlew projects`를 실제 실행해 성공을 확인했다.
- 첫 Compile 오류인 `CpfPage.of(...)` 누락과 ADM Page 생성자의 인자 순서 오류를 수정했다.
- 다음 Compile 오류인 `CmnWeekendCalendar`의 최신 `shiftBusinessDay` 계약 미구현을 수정했다.
- `settings.gradle`의 Composite Build가 Git 비추적 `cpf-tools/build/*`를 참조하던 문제를 추적 대상 `cpf-platform-bom`, `cpf-gradle-plugin` 경로로 보정했다.
- 사용자 후속 Build Tool Ownership 결정에 따라 추적 가능한 예외를 명시한
  `cpf-tools/build/gradle-plugin`, `cpf-tools/build/platform-bom`으로 최종 이동하고
  Composite Build 경로도 함께 복원한다. 과거 비추적 원인은 위치가 아니라 광범위한
  `build/` ignore 규칙이었으며, 중첩 `.gradle`/`build`/`bin` 산출물은 계속 제외한다.
- MariaDB 실제 연결과 CPF Schema 부재를 확인했다. 다른 Application Schema 1개는 보호했다.
- 공식 Reset Tool에 Profile 기반 CPF Service Account allowlist와 별도 확인 문자열을 추가하고, 부분 설치로 생성된 CPF Schema·Service Account만 정리했다.
- MariaDB 공식 설치 경로를 반복 실행하여 DDL Engine/Collation, `TIME`, `ALTER TABLE ADD COLUMN`, 한 줄 DDL Parser 오류를 순서대로 수정했다.
- 수동 `CREATE TABLE` 없이 공식 `initialize-cpf-database.ps1 -All -SeedMode product -RequireRun` 경로로 8개 DB, 총 152개 Table의 Provision → Empty Install → Product Seed와 Runtime Account Probe를 완료했다.
- 설치 결과 Table 수는 core 38, common 2, admin 29, bizAdmin 27, batch 43, reference 3, member 8, account 2이다. EXS 고정 Schema는 생성되지 않았다.
- Canonical MariaDB DDL을 기준으로 `database-schema-manifest.json`을 152개 Table로 재생성했다.
- 재부팅으로 손실된 `CpfDomainConventionPlugin.groovy`를 기준 HEAD 내용과 대조해 복구했다. 그 외 추적 파일의 0-byte/충돌 마커는 없었다.
- 손상된 Microsoft Store `pwsh` 대신 Microsoft 공식 PowerShell 7.6.3 portable ZIP과 `hashes.sha256`을 내려받아 SHA-256 일치를 확인하고, Git 비추적 `build/tools`에서 사용했다. 이후 PC 재부팅으로 Store PowerShell 7.6.4가 정상화됐고 최종 Build Artifact cleanup에서 portable 복사본은 재생성 가능 산출물로 제거했다.
- Local MariaDB의 재부팅 후 TLS negotiation 오류를 확인하고 DB Profile에 `sslMode`를 명시했다. Local Profile은 `disabled`, Production Template은 `verify-full`이며 Installer가 fail-closed로 반영한다.
- Canonical Schema Manifest를 Table/Column 순서/선언 Index/FK까지 대조하도록 Installer를 강화했다.
- Stale MariaDB Verify SQL을 현재 Platform Schema/Baseline/Product Seed 계약의 20개 fail-closed check로 재작성하고 Installer가 실제로 실행·판정하도록 연결했다.
- ADM Runtime transactionId 2개 컬럼을 34자리 정본으로 보정하고 Historical V55/V56과 충돌하지 않는 신규 V57 Migration/Rollback을 추가했다.
- 정확한 CPF Schema/Service Account allowlist를 공식 Reset Tool로 Dry-run 후 적용하고, 다른 Application Schema 1개를 보호했다.
- 수정된 Canonical DDL로 공식 Fresh Install을 다시 실행해 152개 Table, 선언 Index 332개, FK 115개, Product Seed, Runtime Account Probe, Canonical Verify 20건을 모두 통과했다.
- DB Profile 공통 Secret 해석의 StrictMode optional-property 오류와 Profile Gate의 폐기된 Generator wrapper 경로 판정을 수정하고, Local `sslMode=disabled`·Production `verify-full`·Schema Manifest Drift를 포함한 Profile Gate를 통과했다.
- TLS/Profile 보정 후 공식 Reset Tool을 실제 MariaDB에 다시 Dry-run하여 CPF 8개 Schema, 정확한 Service Account 16개만 대상으로 잡고 다른 Application Schema 1개를 보호함을 확인했다. 데이터는 변경하지 않았다.
- 중앙 Vendor Pack 선택기가 이미 삭제된 compatibility manifest를 요구하던 오류를 제거하고, MariaDB 중앙 Pack 선택을 실행해 lifecycle 누락 0, Runtime Resource 91개, Domain Template 7개를 확인했다.
- 실행 Tool과 현행 Installation/Canonical Path Guide의 폐기된 `cpf-tools/db/source` 참조를 중앙 `cpf-tools/db/vendor/<vendor>` 정본으로 보정했다. Historical Review의 과거 경로 기록과 부재 회귀 Gate는 유지했다.
- ADM Frontend에서 `.gitignore`가 `features/logs` Source까지 숨기던 문제를 보정하고 누락 `LogsPage.vue`를 기존 API/State 계약으로 복구했다. ADM test 4건/typecheck/build/lint와 BZA test 10건/typecheck/build/lint를 모두 통과했다.
- BZA Permission Canonical Manifest 8개 Group과 MariaDB Seed/Bundle 4종의 Projection을 비교해 missing 0, extra 0과 `BZA_ADMIN menu_code=ALL`을 확인했다.
- 중복 표준 실행 ID `OADMOP0051`, REF Shared API의 잘못된 Online Annotation, README의 34자리 `X-Transaction-Id` 명세를 보정하고 Transaction ID Standard Gate를 통과했다.
- 중앙 경로 전환 뒤 실패하던 Feature Baseline, Security Seed, Service Registry Source/SQL Smoke를 현행 Topology/Generator/Optional Seed 계약으로 보정하여 모두 통과했다.
- SQL Standard의 blanket audit quartet을 Canonical lifecycle policy Metadata로 교체하고, 의미 없는 Audit Column 추가 없이 29개 Table의 실제 누락 Comment를 보강했다. V58 Delta는 기존 V57 Comment를 제외한 Column 299개/Table 19개로 확정했다.
- 실제 V57 설치 DB에서 V58 Upgrade → Rollback → Re-upgrade를 수행해 Comment `0/0 → 299/19 → 0/0 → 299/19`, 동일 Column/Index/FK 정의 Hash와 `FOREIGN_KEY_CHECKS=1`을 확인했다.
- Re-upgrade DB에 공식 Installer를 재실행해 152 Table/332 Index/115 FK Manifest와 Canonical Verify 20건을 통과했다.
- 최종 Canonical Source로 공식 Allowlist Reset을 적용한 뒤 Provision → Empty Install → Product Seed → Runtime Account Probe → Verify를 다시 실행했다. CPF 8개 DB/152 Table/332 Index/115 FK와 Verify 20건이 모두 통과했고 다른 Application Schema 1개는 보호했다.
- Fresh Install 직후 V58 Canonical Comment Delta 299개 Column/19개 Table이 모두 존재하고 `FOREIGN_KEY_CHECKS=1`임을 별도 Verify-only Smoke로 확인했다.
- DB Tool/Installation/Central Vendor Pack Guide의 과거 123 Table·CMN 1 Table 표기를 실제 Canonical
  152 Table/332 Index/115 FK, CMN Calendar+Sample 구조와 V58 delta lifecycle 검증 결과로 보정했다.
- PowerShell Tool/Gate 재부팅 무결성을 보정하고 AST 152/152, Script 참조 86개/누락 0,
  Contract self-test, R12/R13, Repository Hygiene, Runtime Config/Profile, Layer Taxonomy 1,355
  type, R11 Runtime Entry Point, Core Boundary, Document Link, MariaDB Metadata 59건을 통과했다.
- JVM crash/replay 비추적 산출물 4개를 Repository 내부 정확한 경로로 확인 후 삭제했다. 추적
  Source의 0-byte 파일과 충돌 마커는 0건이다.
- Generator Metadata Schema와 중앙 Contract를 14개 공통 Column/22개 최소 거래 기능으로
  일치시키고, 임의 `payment/PAY`, `insurance/INS`를 각각 31개 파일로 생성했다. 5 Vendor
  Template render, JSON Schema, Federation 2건, normalized parity, ACC 삭제 보호→생성→51개
  검증→제거→재생성 hash parity를 모두 통과했으며 임시 Sandbox는 삭제했다.
- BAT Worker/Scheduler의 30개 Runtime statement를 Canonical Metadata/Template에서 5 Vendor
  150개 SQL로 생성하고 해당 Java 5개 Consumer의 inline SQL을 제거했다. Query Pack Gate와
  Worker/Scheduler `compileJava`를 통과했다.
- `CpfPage` 공개 API의 `items/hasPrevious/of` 계약과 중앙 Vendor Pack filesystem
  `CpfSqlResourceResolver`를 복구하고, Candidate PostgreSQL URL/Driver 계약용 Test Runtime
  Driver를 분리했다. `:cpf-core:test` 222건(1 skip)을 통과했다.
- CMN Base Hierarchy 적용 시 중복되던 `requireText`를 Canonical Base Helper로 통합했다.
  Calendar Override Test가 특별 영업 토요일을 첫 영업일로 저장하고도 월요일을 기대하던
  모순을 `offset=1 토요일`, `offset=2 월요일` 계약으로 강화했으며 `:cpf-common:test`
  16건을 통과했다.
- REF Center-Cut/Data Transform/Foundation API의 Canonical·Legacy 경로와 OpenAPI Tag를
  Identity Contract에 맞게 보정했으며 `:cpf-reference:test`를 통과했다.
- BAT 배포 Inventory를 `CONTROL_SERVER/SCHEDULER/WORKER/CENTER_CUT_RUNNER/HOST_AGENT`
  5개 독립 Role과 각 Artifact/Port/Identity 계약으로 고정했다. Runtime Config Gate와
  5개 Role 각각의 `checkDeployEnv/checkDeployInventory`를 실제 통과했다.
- 삭제된 Legacy BAT 단일 Application을 요구하던 R8/R9 Gate를 5개 Standalone Runtime
  정본으로 보정하고 R8/R9/R10 및 Source Documentation/OpenAPI Gate를 모두 통과했다.
- BAT Contract/Runtime Common/Control Server/Scheduler/Worker/Center-Cut Runner/Host
  Agent/Testkit 전체 Test를 실행해 `BUILD SUCCESSFUL`을 확인했다.
- BAT Control Server 14개 Consumer/103개 inline SQL을 Canonical Query Contract와
  Template 우선으로 이관했다. Contract는 총 158개 Statement이며 5 Vendor 790개 Runtime
  SQL Resource를 Generator로 생성했고, Java inline SQL 0건/Generated Drift 0건/Control
  Server Compile을 통과했다.
- 공식 DB Tool로 8개 Module Service Account를 Local Profile 정본에 재Provision하고
  기존 152개 Table의 Schema Drift 없음, 332 Index/115 FK Manifest, Product Seed,
  Runtime Account Probe와 Canonical Verify 20건을 다시 통과했다.
- MariaDB Runtime Account로 BAT 중앙 Query Pack 158개 전 문장을 서버 측 `PREPARE`하여
  문법·Table/Column·권한 해석을 실제 통과했다.
- CMN `test/library` 모드에서도 DB 전용 MyBatis·Cache Bean이 강제 생성되던 구성을
  `cpf.common.runtime-mode=product` 경계로 일치시켰다. Product 기본값과 필수
  `cmnDataSource` fail-closed 정책은 유지했다.
- 공개 `com.cpf.core.api.execution` Annotation을 거래 Header Interceptor가 인식하지 못하던
  결함을 보정하고 Core 회귀 테스트를 추가했다.
- TYPE 단위 표준 실행 Annotation을 실행 Catalog Scanner가 모든 Method에 중복 등록하던
  결함을 클래스 경로 1건과 Method 선언별 수집으로 분리하고 회귀 테스트를 추가했다.
- 다른 Module의 `@EnableScheduling` 때문에 `cpf.logging.db-fallback.enabled=false`가
  무시되던 Recovery Worker를 보정했다.
- `cpf-member`의 Remote Proxy 공개 `CpfHttpClient` 계약, 비제품 CMN DB 경계, WebMvc
  Header Interceptor Test 구성을 보정하고 17개 Test를 모두 통과했다.
- `cpf-account:test`와 `cpf-gateway:test`를 실행해 모두 통과했다.
- 최신 DEC-033 경계를 실제 ADM Source에 적용했다. ADM의 MBR 전용
  Controller/Service/DTO/Remote Adapter, MBR readiness, MBR/BAT/REF 외부 DataSource
  설정, Frontend 회원 메뉴/Route/API Method와 고정 권한 매핑을 제거했다. ADM Source와
  Config의 `mbrOwner`, `/adm/api/members`, 외부 생성형 Domain DataSource 참조는 정적
  검색 0건이며, 공통 거래 로그의 표준 개인정보 필드/마스킹은 Framework 계약으로
  유지했다.
- ADM Test가 이미 제거된 BAT 전용 Datasource/Repository와 과거 내부 보안 API를 직접
  요구하던 문제를 Owner Port·공개 API 기준으로 보정했다. BZA Test도 최신 Audit,
  PermissionRequest, Password Service 계약에 맞췄다.
- BAT JobInstance 파일 경로에는 이미 `serverInstanceId`가 포함됐지만 ADM 목록/상세
  조회와 화면이 과거 단일 인스턴스 경로를 사용하던 결함을 보정했다. 목록은 표준
  `businessDate/jobName/serverInstanceId/fileName` 구조 전체를 검증하며 상세 API와
  화면은 대상 `serverInstanceId`를 명시적으로 전달한다.
- ADM Frontend production build와 ADM 53개 Backend Test, BZA 26개 Backend Test를
  모두 통과했다.
- Repository 전체 `test`를 직렬 실행해 172 suites/416 tests, 실패 0, 오류 0,
  skip 3으로 통과했다. `cpf-batch/src/**`는 Gradle에서도 Source/Test가 없는
  Aggregator로 처리됐으며 보호 영역을 수정하지 않았다.
- Gradle Problems Report의 유일한 항목이 `processResources` 실행 시
  `Task.project`를 참조하는 Gradle 10 deprecation 경고임을 확인했다. Project
  Metadata를 configuration 시점 값과 task input으로 고정해 경고를 제거했고
  `:cpf-reference:processResources --rerun-tasks`를 재실행해 경고 없이 통과했다.
- PC 재부팅 후 `git fetch origin`을 다시 수행했다. HEAD와 `origin/master`는
  `e725ed3f1bc203e28ff6f06c62a69583358d3b6a`, Ahead/Behind `0/0`이며 Dirty WIP
  429건과 `cpf-batch/src/**` 삭제 WIP를 그대로 보호했다. Portable pwsh와 Local
  MariaDB TCP 3306 연결도 정상이다.
- 재부팅 후 `verifyCpfFinalSourceGates`를 실행해 Version/Standalone Architecture와
  BAT 5개 실행 Artifact Gate를 통과했다. 이 실행에서 추가로 드러난
  `verifyVersionConsistency`의 실행 시점 `Task.project` 경고 3건도 configuration
  시점의 Version/File Task Input으로 고정해 제거했다.
- `checkSqlCanonical`을 재실행해 152개 Canonical Table과 5 Vendor Pack 정적
  동기화 Gate를 통과했다.
- Java 25 Gate는 16개 Module, 11개 BootJar, class major 69를 통과했다. ADM
  Frontend 4 tests와 BZA Frontend 10 tests를 포함한 lint/typecheck/build Gate도
  모두 통과했다.
- 2026-07-27 추가 재부팅 후 `git fetch origin`과 정본 재확인을 다시 수행했다.
  HEAD/`origin/master`는 계속 `e725ed3f1bc203e28ff6f06c62a69583358d3b6a`,
  Ahead/Behind `0/0`, Dirty WIP 429건이며 reset/clean/revert 없이 모두 보호했다.
  `cpf-batch/src/**` 제외 범위와 최종 정상 완료 후 Commit/Push 승인도 다시 확인했다.
- 추가 재부팅 뒤 공식 DB 설치 경로의 첫 실행은 Process 범위
  `CPF_DB_ROOT_PASSWORD`가 소실되어 SQL 실행 전 fail-closed 됐다. 사용자가 제공한
  Secret을 현재 Process에만 주입해 원문을 출력·저장하지 않고 재실행했으며,
  8개 DB/152 Table/332 Index/115 FK, Product Seed, Runtime Account Probe와
  Canonical Verify 20건을 다시 통과했다.
- 실제 MBR Runtime 최초 기동에서 공식 Installer의 모듈별 최소권한 계정과 Runtime
  Harness 환경변수가 연결되지 않아 `cpf_app` 인증이 실패하는 결함을 찾았다.
  `runtime-start-services.ps1`가 DB Profile의 Runtime credential/URL/Driver를
  Process 범위로만 해석·주입하고 결과에는 Profile 경로만 남기도록 보정했다.
- 인증 보정 뒤 같은 MBR 기동에서 component-scan `@ConditionalOnBean` 평가 순서 때문에
  Product Calendar JDBC Store가 누락되는 실제 Bean 결함을 검출했다. Product mode
  자체 조건과 필수 `cmnDataSource` constructor fail-fast는 유지하고 취약한 조건만
  제거했다. Common/Member Test 재통과 후 최신 MBR bootJar를 실제 MariaDB에 연결해
  `/v3/api-docs` HTTP 200과 Process 생존을 확인하고 공식 PID State로 종료했다.

## 진행 중인 작업

- BAT 외 Platform Module의 inline JDBC SQL과 중앙 Vendor Pack 상태를 전수 감사해,
  실제 Vendor 차이가 Java Source에 남아 있는지와 Repository/MyBatis Pack 완결 범위를
  보정하고 있다.
- 전체 Module Test/Assemble과 MariaDB Multi-instance Runtime 검증을 이어간다.

## 아직 시작하지 않은 작업

- V58 외 전체 Historical Migration/Upgrade/Rollback/Re-apply/Drift 실제 검증
- MariaDB Runtime Query와 다중 인스턴스 Runtime 검증
- ADM/BZA 실제 Browser/API Runtime 검증
- 전체 `clean test assemble`과 최종 Quality/Release/Hygiene Gate
- 최종 Current/Handover/Review/Evidence 동기화
- 사용자 승인에 따른 최종 Commit과 `master` Push

## 변경 중인 주요 파일/모듈

- `cpf-core/src/main/java/com/cpf/core/api/page/CpfPage.java`
- `cpf-common/src/main/java/com/cpf/common/calendar/CmnWeekendCalendar.java`
- `cpf-batch` Legacy와 `cpf-batch/*` Standalone Runtime
- `cpf-tools/generator`, `cpf-tools/db/vendor`, `cpf-tools/scripts`
- `cpf-docs/work/state`, 이후 Current/Handover/Review/Evidence
- `cpf-core` 실행 Catalog/Header 검증 및 Recovery Worker
- `cpf-common` Product DB Bean 활성화 경계
- `cpf-member` Test Runtime 구성과 공개 HTTP Client 계약
- `cpf-admin` 다중 인스턴스 BAT 로그 조회 API/Service/Frontend 및 생성형 Domain 독립 경계
- `cpf-biz-admin` 최신 Authorization/Attachment 상태 계약 Test

## 실제 실행한 검증

| 상태 | 명령/대상 | 실제 결과 |
|---|---|---|
| 완료 | `git fetch origin`, Git baseline 확인 | HEAD와 `origin/master` 일치, Ahead/Behind `0/0` |
| 완료 | `.\gradlew.bat projects` | PASS |
| 실패 | `.\gradlew.bat :cpf-batch:verifyStandaloneArtifacts --no-daemon` | 최초 `cpf-core` Compile에서 `CpfPage.of` 누락 |
| 실패 | `.\gradlew.bat compileJava --no-daemon --stacktrace` | `CpfPage` 보정 후 `CmnWeekendCalendar` 계약 불일치 발견 |
| 실패 | `.\gradlew.bat compileJava --no-daemon` | Core/Common 보정 후 `cpf-batch/src` Legacy가 Aggregator 의존성 없이 Compile되어 100개 이상 오류; 기능 이관 확인 후 구조 수정 중 |
| 완료 | MariaDB 연결·Schema Inventory | CPF Schema 0개, 보호 대상 타 Application Schema 1개 확인 |
| 완료 | `reset-cpf-databases.ps1` Dry-run/Apply | Profile allowlist의 CPF Schema와 CPF Service Account만 정리, 타 Schema 보호 |
| 실패 | 공식 MariaDB Fresh Install 반복 | stale `@localhost` 계정, Engine/Collation, `TIME`, `ALTER COLUMN`, 한 줄 DDL Parser 오류를 실제 실행으로 발견 |
| 완료 | `initialize-cpf-database.ps1 -All -SeedMode product -RequireRun` | 8개 DB/152개 Table Fresh Install, Product Seed, Runtime Account Probe PASS |
| 완료 | `generate-database-schema-manifest.ps1` | Canonical DDL 기준 152개 Table Manifest 생성 |
| 실패 | 기존 설치 DB 대상 강화된 공식 Installer/Verify | 모든 Manifest 대조 PASS 후 `platform.runtime_transaction_id_width`가 ADM의 과거 VARCHAR 폭을 정확히 검출 |
| 완료 | 공식 Reset Dry-run/Apply | CPF 8개 Schema와 정확한 Service Account만 삭제, 타 Application Schema 1개 보호 |
| 완료 | 강화된 `initialize-cpf-database.ps1 -All -SeedMode product -RequireRun` | Fresh Install 152 Table, 선언 Index 332, FK 115, Product Seed, Runtime Probe, Canonical Verify 20건 PASS |
| 완료 | `check-migration-checksums.ps1` | Source/Runtime Migration, Rollback, Checksum parity PASS |
| 완료 | `check-db-vendor-pack-parity.ps1 -Vendor mariadb` | 42개 File Pair PASS |
| 완료 | `check-sql-canonical.ps1` | 152 Table, 5 Vendor Manifest PASS |
| 실패 | `check-database-profile-standard.ps1` 최초 실행 | StrictMode optional `devDefault` 접근과 폐기 Generator wrapper path를 검출 |
| 완료 | `check-database-profile-standard.ps1` 재실행 | 8개 Module Profile, Local/Production TLS, 5 Vendor Domain Template, Schema Drift PASS |
| 완료 | `reset-cpf-databases.ps1 -DropServiceAccounts` Dry-run | CPF 8개 Schema/16개 정확한 Service Account 대상, 타 Application Schema 1개 보호, 데이터 변경 없음 |
| 완료 | `select-db-vendor-resources.ps1 -Vendor mariadb -SkipParityCheck` | lifecycle 누락 0, Runtime Resource 91개, Domain Template 7개, executable=true |
| 완료 | ADM Frontend `npm test/typecheck/build/lint` | 2 files/4 tests 및 전체 Gate PASS, 복구된 LogsPage chunk 포함 |
| 완료 | BZA Frontend `npm test/typecheck/build/lint` | 4 files/10 tests 및 전체 Gate PASS |
| 실패 | Transaction ID Standard 최초 재실행 | `OADMOP0051` 중복, REF Shared ID/Online Annotation 불일치, README 34자리 명세 누락 검출 |
| 완료 | Transaction ID Standard 재실행 | ID/Annotation/README 보정 후 PASS |
| 완료 | Feature Baseline/Security Seed/Service Registry Source Smoke | 현행 중앙 경로·BAT topology·Generator contract·Optional Seed 기준 PASS |
| 실패 | V58 Migration Smoke 최초 Baseline 검사 | 이미 존재하던 Table Comment 6개와 V57 `TRANSACTION_ID` Comment 1개가 신규 Delta로 잘못 포함됨을 적용 전에 검출; DB 변경 없음 |
| 완료 | `smoke-platform-schema-comment-migration.ps1` | V58 Upgrade/Rollback/Re-upgrade `299/19 → 0/0 → 299/19`, 정의 Hash 동일, FK checks 복원 PASS |
| 완료 | Upgraded DB 대상 공식 Installer 재실행 | 152 Table/332 Index/115 FK Manifest, Product Seed, Canonical Verify 20건 PASS |
| 완료 | 최종 공식 Reset Apply 및 Fresh Install | 정확한 CPF 8개 Schema/16개 Service Account만 Reset, 타 Schema 보호 후 8개 DB/152 Table/332 Index/115 FK/Verify 20건 PASS |
| 완료 | V58 Fresh Install Comment Verify-only | Canonical Delta Column 299개/Table 19개와 FK checks=1 PASS |
| 완료 | PowerShell AST/Script Reference | 152 Script AST PASS, 실행 Script 참조 86개/누락 0 |
| 완료 | Contract/R12/R13/Runtime Config/Profile/Taxonomy/R11/Core/Doc Link Gate | 현행 Standalone BAT·Generated EXS 기준 PASS, 분류 Type 1,355건 |
| 완료 | Repository Crash/Source Integrity | JVM crash/replay 산출물 4개 삭제, Source 0-byte/충돌 마커 0건 |
| 완료 | Generator Golden/Federation/ACC lifecycle static smoke | PAY/INS 임의 Domain, 5 Vendor render, Federation/parity, ACC 51개 검증과 재생성 hash PASS |
| 완료 | BAT Worker/Scheduler Runtime Query Pack 1차 | 30 statement × 5 Vendor=150 SQL, inline SQL 0, pack Gate와 두 Runtime compile PASS |
| 실패 | 최초 `:cpf-core:test` | `CpfPage` 공개 accessor, 중앙 Resolver API, PostgreSQL Test Driver 누락 검출 |
| 완료 | `:cpf-core:test --no-daemon --max-workers=1` | 222 tests, 1 skipped, BUILD SUCCESSFUL |
| 실패 | 최초 `:cpf-common:test` | Base `requireText` 충돌과 Calendar Override Test 기대값 모순 검출 |
| 완료 | `:cpf-common:test --no-daemon --max-workers=1` | 16 tests, BUILD SUCCESSFUL |
| 실패 | `:cpf-reference:test :cpf-member:test :cpf-account:test :cpf-gateway:test` | REF 51 tests 중 `ReferenceIdentityContractTest` 1건 실패, 2 skip; 이후 Module은 실행되지 않음 |
| 완료 | `:cpf-reference:test` 재실행 | Canonical/Legacy 경로와 Tag 보정 후 BUILD SUCCESSFUL |
| 완료 | Source Documentation, R8/R9/R10 Gate | OpenAPI 54건과 Standalone BAT cleanup/hardening/product standard PASS |
| 완료 | BAT 8개 Subproject Test | Contract/Runtime Common/5 Runtime/Testkit BUILD SUCCESSFUL |
| 완료 | BAT 5-role Deploy Env/Inventory | CONTROL_SERVER 포함 5개 Role의 local env/inventory 정확 매칭 PASS |
| 완료 | BAT Runtime Query Pack Gate | 158 statements × 5 vendors=790 generated SQL, inline SQL 0, drift/failure 0 |
| 완료 | `:cpf-batch:control-server:compileJava` | 중앙 Query Catalog Consumer 변경 후 BUILD SUCCESSFUL |
| 실패 | MariaDB BAT Query Prepare 최초 실행 | 재부팅 전 Service Account와 현재 Local Profile 암호 불일치 검출, SQL 실행 전 인증 실패 |
| 완료 | 공식 `initialize-cpf-database.ps1 -All -SeedMode product -RequireRun` | 8개 Service Account 재Provision, 152 Table/332 Index/115 FK, Runtime Probe, Verify 20건 PASS |
| 완료 | `smoke-bat-runtime-query-pack-mariadb.ps1` | Runtime Account로 158/158 SQL server-side PREPARE PASS |
| 실패 | 최초 `:cpf-common:test :cpf-member:test` | MBR Context에서 비제품 CMN MyBatis가 `cmnDataSource`를 강제해 6건 실패 |
| 실패 | CMN DB 경계 보정 후 `:cpf-member:test` | TYPE 단위 `SMBRAD0001`을 Method마다 중복 Catalog 등록하는 Scanner 결함 검출 |
| 실패 | Scanner 보정 후 `:cpf-member:test` | 공개 API Annotation을 Header Interceptor가 인식하지 않아 필수 Header 누락 요청이 200으로 통과 |
| 완료 | Core Catalog/Header 회귀 Test와 `:cpf-member:test` | 신규 Core Test 2종과 MBR 17 tests, BUILD SUCCESSFUL |
| 완료 | `:cpf-account:test :cpf-gateway:test` | 두 Module 모두 BUILD SUCCESSFUL |
| 실패 | 최초 `:cpf-admin:test :cpf-biz-admin:test` | ADM 53건 중 과거 단일 인스턴스 BAT 로그 경로 1건, BZA 26건 중 폐기 권한명/Scan 상태 Fixture 2건 실패 |
| 완료 | ADM/BZA 표적 회귀 Test와 ADM Frontend production build | 다중 인스턴스 로그 경로/API/UI, BZA Authorization/Attachment 계약 PASS |
| 완료 | `:cpf-admin:test :cpf-biz-admin:test` 전체 재실행 | ADM 53 tests, BZA 26 tests, BUILD SUCCESSFUL |
| 완료 | `.\gradlew.bat test --continue --no-daemon --max-workers=1` | 172 suites, 416 tests, failures 0, errors 0, skipped 3, BUILD SUCCESSFUL |
| 완료 | `:cpf-reference:processResources --rerun-tasks` | Gradle 10 `Task.project` deprecation 보정 후 Problems 경고 없이 PASS |
| 완료 | 재부팅 후 Git/pwsh/MariaDB Baseline | HEAD/origin 동일, Ahead/Behind 0/0, Dirty 429 보호, portable pwsh 및 TCP 3306 정상 |
| 완료 | `verifyCpfFinalSourceGates` | Version/Standalone Architecture와 BAT 실행 Artifact Gate PASS |
| 완료 | `verifyVersionConsistency --rerun-tasks` | 실행 시점 Project 접근 3건 제거 후 경고 없이 PASS |
| 완료 | `checkSqlCanonical` | 152 Tables, 5 Vendors PASS |
| 완료 | Java 25 + ADM/BZA Frontend Verify | 16 Modules/11 BootJars/major 69, ADM 4 tests, BZA 10 tests, lint/typecheck/build PASS |
| 완료 | 추가 재부팅 후 Git/정본 Baseline 재확인 | HEAD/origin 동일, Ahead/Behind 0/0, Dirty WIP 429 보호, 전체 1차 검수 요청서/Continuity/Decision 재확인 |
| 실패 | 추가 재부팅 후 공식 DB Installer 첫 실행 | Process 범위 Root Secret 환경변수 소실을 SQL 실행 전 fail-closed로 검출, DB 변경 없음 |
| 완료 | 공식 DB Installer Secret 재주입 후 재실행 | 8 DB/152 Table/332 Index/115 FK, Product Seed, Runtime Probe, Canonical Verify 20건 PASS |
| 실패 | MBR Runtime 최초 기동 | Runtime Harness가 설치 Profile의 모듈별 Runtime credential을 전달하지 않아 DB 인증 실패 |
| 실패 | MBR Runtime credential 보정 후 기동 | 인증은 통과했으나 Product Calendar JDBC Store의 취약한 조건부 Bean 평가 순서 결함 검출 |
| 완료 | `:cpf-common:test :cpf-member:test` | Calendar Store Bean 조건 보정 후 BUILD SUCCESSFUL |
| 완료 | MBR 최신 bootJar + Local MariaDB Runtime | Process 생존, 두 DataSource 연결, `/v3/api-docs` HTTP 200, 공식 Harness 종료 PASS |
| 실패 | ADM 최신 bootJar 최초 Runtime | `CpfServiceCaller` Auto-configuration 순서 결함으로 Application Context 기동 실패 |
| 완료 | `:cpf-core:test :cpf-admin:test` | Public Boundary Auto-configuration 순서 보정 후 BUILD SUCCESSFUL |
| 실패 | ADM Runtime Readiness 최초 확인 | Process와 DB Pool은 정상이었으나 인증 Filter가 `/actuator/health/readiness`를 HTTP 401로 차단 |
| 완료 | ADM 최신 bootJar + Local MariaDB Runtime | GET/HEAD 전용 Health Probe 인증 예외와 회귀 Test 보정, Readiness HTTP 200, `admDB=UP`, `cpfDB=UP`, 공식 Harness 종료 PASS |
| 완료 | BZA 최신 bootJar + Local MariaDB Runtime | CPF/BZA DataSource 연결, `/v3/api-docs` HTTP 200, 표준 Header 기반 Login Repository 경로가 예상 401까지 실행되어 SQL 문법/권한 오류 없음, 공식 Harness 종료 PASS |
| 완료 | BZA Runtime Health Probe 보정 | `/actuator/health` 404 원인을 Actuator/management 설정 누락으로 확인해 readiness 계약과 Harness 경로를 보정하고 `:cpf-biz-admin:test` PASS; 재부팅 후 최신 bootJar Fresh Build와 공식 Harness로 `/actuator/health/readiness`, `/actuator/health` HTTP 200 및 Process 생존을 확인하고 기록된 PID만 공식 종료 |
| 실패 | REF 최신 bootJar 최초 Runtime | 기본 비활성인 EDU Batch가 Spring Boot Batch Auto-configuration을 활성화해 CPF/CMN/REF/BAT 4개 DataSource 중 하나를 고르지 못함 |
| 완료 | `:cpf-reference:test`와 REF Local MariaDB Runtime | 기본 Runtime의 Batch Auto-configuration 제외, 명시 EDU 활성화 때만 BAT DataSource 구성, OpenAPI HTTP 200과 표준 Header 기반 실제 REF 조회 SQL HTTP 200, 공식 종료 PASS |
| 실패 | ACC Local MariaDB 실제 조회 최초 실행 | Health는 200이었으나 Generator의 `cpf.datasource`가 Core `CPF_DATASOURCE_URL`과 충돌하여 ACC Mapper가 cpfDB의 `acc_account`를 조회하고 HTTP 500 |
| 완료 | Generator Domain DataSource 격리 | Canonical Generator를 `cpf.<domain>.datasource`로 보정하고 임의 ZQX Domain 생성/Test/bootJar/bootWar 및 공통 CPF URL 동시 주입 격리 Test PASS |
| 완료 | `:cpf-account:test`와 ACC Local MariaDB Runtime | ACC를 `cpf.acc.datasource`로 동기화, HTTP 200 실제 중앙 Mapper 조회와 MariaDB `cpf_acc_app/accDB` 10개 Connection 확인, 공식 종료 PASS |
| 완료 | GWY 최신 bootJar + Local MariaDB Runtime | CPF DataSource 연결과 Route Snapshot 초기 조회 후 `/actuator/health` HTTP 200, 공식 종료 PASS |
| 완료 | Build Support Unit Tooling 이동/물리 정리 | Source/Composite Build를 `cpf-tools/build/{gradle-plugin,platform-bom}`으로 이동하고 `projects`, Plugin `check/validatePlugins`, BOM `check/POM` PASS; 정본 5개 Source hash와 과거 5개 추적 삭제를 대조한 뒤 공식 cleanup으로 Root의 ignored cache/빈 `cpf-gradle-plugin`, `cpf-platform-bom` 디렉터리까지 제거 |
| 재확인 필요 | VS Code Problems 저장 Marker | JDT 저장 Marker 845건 중 hard compile error 0, null-analysis 765건과 재부팅 후 손상된 Workspace cache/stale Gradle Marker가 대부분; 실제 후보 경고는 최종 Build 전 별도 보정/재확인 |
| 실패 | BZA 최신 bootJar 최초 재생성 | VS Code JDT가 최대 8GB로 설정된 상태에서 병렬 Gradle 검증이 겹쳐 Windows pagefile 부족과 JVM native crash 발생; Application/Compile 결함이 아니며 crash/replay 산출물은 정식 cleanup으로 제거 |
| 완료 | 메모리/재부팅 Baseline 재확인 | 재부팅 후 `git fetch`, HEAD/origin 동일, Ahead/Behind 0/0, Dirty WIP 489 보호; 로컬 비추적 VS Code Java LS 상한을 8GB→4GB로 낮추고 Gradle daemon 2개 종료, 이후 Heavy 검증은 `max-workers=1` 직렬 실행으로 확정 |
| 완료 | BZA 재부팅 후 Fresh Build/Readiness | 캐시 정리 뒤 `:cpf-biz-admin:bootJar --no-daemon --max-workers=1` Fresh Build PASS, 공식 Runtime Harness가 MariaDB CPF/BZA DataSource를 사용해 readiness HTTP 200과 processAlive를 판정, PID 5892 공식 종료 PASS |
| 실패 | Checked-in Generated Domain 구조 Parity 최초 감사 | 정본은 같은 Capability의 normalized Controller/Facade/Service/Port/Adapter/Repository/DTO/Test/DB 구조를 요구하고 MBR을 Golden Reference로 명시하지만, 실제 `cpf-member` 37개와 `cpf-account` 38개 Source/Test는 서로 다른 과거 수작업 구조이며 MBR에는 현행 Generator manifest/ownership도 없음; 문서 완료 표기 대신 Generator-first 이관 필요 |
| 완료 | Generated Domain 수명주기 경계 결정 | DEC-033 확정: ADM/Platform과 REF EDU의 특정 MBR/ACC/PAY Source·URL·DB·Readiness 의존을 제거하고, 각 Generated Domain은 동일 Minimal Self Sample Source + DB 1개만 Generator가 소유하며 임시 Cross-domain parity Domain은 검증 후 삭제 |
| 실패 | ADM 생성형 Domain 독립 보정 후 첫 `:cpf-admin:test` | 앞선 짧은 Gradle 확인 실행에서 남은 single-use daemon이 `test-results/test/binary/output.bin`을 잠가 Test Task가 결과 정리 단계에서 실패; 기능/Compile 오류는 아니며 daemon 1개를 정상 종료 |
| 재확인 필요 | ADM 독립 보정 후 `:cpf-admin:test --no-daemon --max-workers=1` 재실행 | BUILD SUCCESSFUL이나 모든 Task가 UP-TO-DATE였으므로 Source/Frontend Compile 연결은 확인했고, Test의 fresh 실행은 최종 `--rerun-tasks`/전체 Test에서 재확인 |

실패 이력은 수정 전 실제 결과이며 성공으로 덮어쓰지 않는다. 수정 후 동일 Gate를 재실행한다.

## DB / Runtime 현재 상태

- Local MariaDB 설치·연결: `완료`
- CPF 소유 Schema/Table 존재 여부: 8개 DB, 총 152개 Table 생성 `완료`
- 공식 Fresh Install: `완료`
- Product Seed: `완료`
- Canonical Verify Pack 실제 실행: 20건 `완료`
- Schema Manifest 실제 대조: 152 Table/332 Index/115 FK `완료`
- Migration/Upgrade/Rollback/Re-apply: V58 실제 lifecycle은 `완료`; 전체 Historical chain은 `미검증`
- MariaDB Runtime Query: `부분 구현` — BAT 중앙 Query Pack 158/158 실제 PREPARE 완료,
  MBR·ADM·BZA·REF·ACC·GWY 실제 DB 연결/기동/HTTP Probe 완료, REF/ACC 실제
  업무 Query 완료, BAT Control Runtime은 미검증
- MySQL/PostgreSQL/Oracle/SQL Server: 제품 설치 없이 Source/Template/Contract 검증 대상이며 실제 DB Runtime은 `미검증`
- BAT 5개 독립 Runtime/배포 Identity 및 중앙 Query Pack: `완료`
- ADM/BZA Frontend 정적 Test/Typecheck/Build/Lint: `완료`; ADM/BZA 실제 API Runtime은
  `부분 구현`, 실제 Browser Runtime은 `미검증`
- 현재 공식 Runtime Harness로 기동 중인 Process: 없음
- Multi-instance/Browser/Remote Host: `미검증`

DB Secret은 Process 범위에서만 주입하고 Console, Evidence, 문서, Git에 원문을 기록하지 않는다.

## Blocker와 미검증

- ADM의 특정 Generated Domain Source/Config/Frontend 의존 제거는 `완료`다. REF의
  중립 자체 Echo 시뮬레이터 전환과 ADM Product Seed/Gate의 고정 MEMBER 잔재 제거는
  `부분 구현`이며 Generator/Canonical Metadata 결과와 함께 검증한다.
- BAT 중앙 Vendor Runtime Query Pack은 완료됐으며 다른 Platform Module의 inline SQL과
  Pack descriptor `부분 구현/미구현` 상태를 실제 Source 기준으로 전수 보정 중이다.
- Core/Common/Reference/Member/Account/Gateway/ADM/BZA Test는 현재 보정분 기준 모두
  통과했다. 아직 Repository 전체 단일 `test/assemble` Gate와 독립 Runtime E2E가 남아 있다.
- SQL Standard Gate가 검출한 Saga/ADM/BAT/MBR 표준 위반은 Canonical lifecycle/audit policy와 V58 Migration/Rollback으로 보정했고 실제 MariaDB lifecycle까지 통과했다. 전체 Historical Migration chain 검증은 아직 남아 있다.
- 전체 Gradle `test`는 현재 PC에서 통과했다. `assemble`, Quality/Release/Hygiene
  통합 Gate와 실제 Runtime은 별도 검증이 남아 있다.
- 외부 Remote Host, 다른 DB Vendor, Broker/Vault/HSM/Browser Driver는 현재 환경 부재 여부를 확인한 뒤 실제 E2E만 `미검증`으로 남긴다.

## 다음 정확한 작업 순서

1. REF의 특정 생성형 Domain 호출 예제를 자체 중립 Echo 시뮬레이터로 전환하고 ADM/REF
   의존 0 Gate를 고정한다.
2. MBR/ACC를 고정 업무 모델이 아닌 동일 Minimal Sample의 Generator-owned projection으로
   정규화하고, 설치기는 고정 Domain switch 없이 Metadata 등록을 동적으로 소비한다.
3. BAT 외 Platform Module 중앙 Runtime Query Pack/inline SQL을 Vendor-neutral 정책에 맞게
   보정하고 5 Vendor Source/Contract/Parity를 통과시킨다.
4. Canonical Metadata에서 Vendor Pack을 재생성한 뒤 공식 MariaDB Reset/Fresh Install,
   Product Seed, Verify, Migration/Rollback과 실제 Runtime Query를 반복 검증한다.
5. Repository 전체 Module Compile/Test/Frontend/Assemble을 저메모리 직렬 방식으로
   실행하고 Quality/Release/Hygiene Gate 오류를 수정한다.
6. 다른 4개 Vendor는 설치 제품 부재를 전제로 Source/Template/Contract/Parity를 검증하며
   Runtime 완료로 기록하지 않는다.
7. 검증된 레거시·산출물·빈 폴더를 정식 cleanup으로 제거하고 Current/Handover/Review/
   Evidence를 실제 종료 HEAD와 명령 결과에 맞춘다.
8. 실패/미완성 코드가 없는지 확인한 뒤 Commit하고 `master`에 Push한다. 충돌/인증 실패
   시 강제 Push하지 않는다.

## 다시 수행하면 안 되는 작업 / 확정 사항

- Dirty Worktree를 reset/clean/revert하지 않는다.
- 최신 사용자 Steering이 과거 `cpf-batch/src/**` 제외 결정을 명시적으로 대체했다.
  Legacy `cpf-batch/src` 삭제 WIP를 최종화하되 기능을 단순 폐기하지 않고,
  Runtime/Contract는 `contract`, `runtime-common`, `control-server`, `scheduler`,
  `worker`, `center-cut-runner`, `host-agent`, `testkit`으로, 범용 EDU는
  `cpf-reference` Public API/SPI 경계로 이관한 뒤 Source→Owner→Consumer→Test→Runtime
  매핑과 회귀 Gate를 통과시킨다.
- Historical Flyway를 임의 수정하지 않는다.
- MariaDB SQL만 직접 고치지 않는다. Requirement/Data Model → Canonical Metadata/Generator → Vendor 산출물 순서를 지킨다.
- Module별 5개 Vendor SQL 복제와 Module-local fallback을 복구하지 않는다.
- Vendor 선택 때문에 Java 업무 Source를 분기하거나 Git Source Resource를 덮어쓰지 않는다.
- Generator에 MBR/ACC/REF/EXS/PAY/INS 등 고정 Domain/SystemCode 목록을 넣지 않는다.
- Generated Domain의 기본 논리 모델은 단일 Minimal Transaction Golden Template이다.
- EXS를 고정 Platform Module/Schema로 복원하지 않는다.
- Root에 Stale Evidence, 조기 DOCX, 중복 README, Release Notes, 작업용 Apply/Checksum 파일을 복원하지 않는다.
- 실행하지 않은 Build/DB/Runtime/Multi-instance/Browser/Upgrade/Rollback/Recovery를 `완료`로 기록하지 않는다.
- 이번 요청에서는 사용자가 정상 완료 후 Commit/Push를 명시적으로 승인했다. 실패 또는 미완성 상태에서는 Push하지 않는다.


---

# ChatGPT 1차 개발 인계 추가 — 2026-07-27

> **역사 기록 주의:** 이 절은 `fb95e15f...` 기준 1차 Patch 당시 상태를 보존한 History다. Artifact 공급/자동 Sync/Codex 즉시 검증 정책은 아래 `2026-07-27 ChatGPT 2차 개발 Continuity`가 현재 정본이며, 상충 시 아래 최신 절을 따른다.

## 기준과 권한

- 이번 ChatGPT 작업 시작 기준: `fb95e15f90856adcff39040a50b128aa40f5ef43` (`20260727_01`)
- 이전 Continuity의 마지막 `Commit/Push 승인` 문구는 **당시 Codex 작업 세션에 한정된 승인**이다.
- 이번 ChatGPT 작업에는 Commit/Push/Branch 승인이 승계되지 않았다.
- ChatGPT는 GitHub `master`를 수정하지 않고 Root 상대경로 patch bundle만 작성한다.
- QA 통합 요구사항과 사용자 추가 요구는 `CPF_NEXT_INTEGRATED_DEVELOPMENT_REQUEST_20260727.md`에 합산했다.

## 이전 Codex 결과 최신 Source 재분류

### 구현 확인됨 — 무조건 재조사 금지, 변경 영향만 집중 재검증

1. `CpfTargetServiceResolver`의 고정 `/mbr`/port 기반 Target 추정 제거.
2. DB Installer의 Profile/Generated Domain metadata 기반 동적 분류/초기화 경로.
3. ADM의 MBR 전용 Controller/Service/Remote Adapter/UI 제거.
4. REF의 MBR Service Call DTO/Client 제거 및 neutral Echo 대체 구조.
5. BAT Legacy `cpf-batch/src/**` 물리 제거와 standalone BAT 구조.
6. Build Tooling의 `cpf-tools/build/{gradle-plugin,platform-bom}` 이동.
7. ACC/Generator DataSource namespace 격리.

### 미완료/재확인 필요 — 다음 개발 연속 P0

1. MBR/ACC normalized Generator Golden parity.
2. Root `settings.gradle`의 MBR/ACC 고정 include 정책.
3. `CpfSystemCodes.inferFromTypeName` package/type-name 추론의 실제 Consumer와 제거/대체 계약.
4. REF self-contained dependency-0 focused gate.
5. BAT Legacy EDU 기능 parity와 Job Pack Generator 완결성.
6. Gateway Failover/Timeout/UNKNOWN_RESULT/O-S-B/Header/Multi-instance 실제 Fault 검증.
7. ADM/BZA Browser, Multi-instance, Historical Migration 전체 Chain.

## ChatGPT 신규 변경 CHG-20260727-ARTIFACT-001

사용자 요구: CPF를 로컬 또는 배포 환경에서 빌드할 때 Core/Common/BAT public contract/BOM/Convention Plugin이 독립 Generated Domain과 standalone WAS에 동일 version으로 자동 공급되고, 생성된 bootJar/bootWar가 필요한 CPF JAR를 실제 포함해야 한다.

구현 방향:

- shared local Maven repository 기본값 `${user.home}/.cpf/repository`
- `CPF_LOCAL_ARTIFACT_REPOSITORY` / `-PcpfLocalArtifactRepository` override
- [역사 기록/폐기된 정책] 원격 `CPF_ARTIFACT_REPOSITORY_URL` 우선 + local fallback — **20260727_04부터 금지. REMOTE/OFFLINE은 fail-closed**
- Root `build` 성공 시에만 local auto publication (`-PcpfAutoLocalArtifactSync=false` opt-out); 실패 build는 shared local repository publish 금지
- `publishCpfLocalPlatformArtifacts`
- `verifyCpfLocalArtifactPropagation`
- BOM/Convention Plugin/BAT public artifacts local publication
- Generator가 standalone repository 생성 전 local public artifact 자동 publish
- 독립 Generated Domain이 local repository를 자동 resolve
- Generated Domain `bootJar`/`bootWar`에 CPF Core/Common/BAT contract 포함 Gate
- standalone build는 `--refresh-dependencies`로 같은 SNAPSHOT version의 최신 local artifact를 재해석

상태: **Source 구현 / 실행 미검증**.

Codex는 전체 CPF를 다시 검수하지 말고 다음을 먼저 실행한다.

1. `publishCpfLocalPlatformArtifacts`
2. `verifyCpfLocalArtifactPropagation`
3. 임시 Generated Domain 1개 생성/독립 build/package
4. 성공 시 두 번째 Domain parity/repeatability
5. bootJar/bootWar ZIP dependency 확인

## ChatGPT 신규 변경 CHG-20260727-CONTACT-001

사용자 요구:

- 휴대폰 UI 표기는 `연락처(휴대폰)`
- `내부 전화번호`를 별도 필드로 관리
- ADM 운영자와 BZA 직원에 동일 의미 적용

구현:

- ADM `mobileNo`, `officePhoneNo` DTO/Service/UI
- BZA `officePhoneNo` Service/Repository/UI
- 기존 Java record Consumer 호환 생성자 유지
- 선택값 blank → null
- MariaDB canonical source에 `adm_operator_profile.MOBILE_NO`, `adm_operator_profile.OFFICE_PHONE_NO`, `bza_employee.office_phone_no`; 인증 Identity `adm_operator`에는 연락처를 두지 않음
- 신규 `V59__admin_contact_model.sql` 및 rollback
- static parity gate `check-admin-contact-model.ps1`

상태: **Source/SQL 구현 / 실행 미검증**.

추가 QA 보정 `CHG-20260727-BZA-DEFAULT-001`:

- 신규 직원 `employmentStatus` 미입력 기본값 `EMPLOYED`
- Canonical DDL default `EMPLOYED`
- V60 forward/rollback 추가
- 기존 Row 데이터는 변경하지 않음

중요: DB Generated bundle/manifest를 이 환경에서 `sync-database-artifacts.ps1`로 실행 재생성하지 못했다. Codex의 첫 DB 단계에서 canonical source를 기준으로 sync하고 실제 Diff를 검토한 뒤 V59 lifecycle을 실행한다. 실행 전에는 DB 완료로 올리지 않는다.

## ChatGPT 검증 환경 제약

현재 ChatGPT 실행 컨테이너에는 완전한 CPF checkout이 없고 GitHub 직접 clone도 DNS 차단으로 실패했다. Java는 21이며 CPF 기준 Java 25가 아니다. `pwsh`도 없다.

따라서 다음은 실행하지 않았으며 PASS가 아니다.

- Java 25 Gradle compile/test
- included-build publication
- local Maven artifact 실제 생성
- Generated Domain 실제 standalone build
- bootJar/bootWar ZIP 검증
- Frontend npm test/typecheck/build
- PowerShell AST/runtime
- DB artifact sync
- MariaDB V59 Upgrade/Rollback/Reapply/Fresh
- Browser/Multi-instance/Fault

실행하지 않은 결과를 기존 Codex Evidence로 자동 승계하지 않는다.

## 다음 Codex 크레딧 절약형 검증 원칙

- `CPF_CHANGE_IMPACT_AND_VALIDATION_LEDGER.md`의 Change ID부터 시작한다.
- ChatGPT 변경 파일의 직접/간접 Consumer만 우선 검증한다.
- 이전에 실제 성공했고 이번 Diff와 무관한 BAT 158/158 PREPARE, V58 lifecycle, 단일 Runtime 전체 재기동은 기본적으로 반복하지 않는다.
- focused test에서 공통계약 회귀가 발견될 때만 범위를 확대한다.
- Browser/Multi-instance/Fault는 기능 안정화 후 최종 통합 기준 Commit에서 묶어서 실행한다.
- 실행하지 않은 검증은 `미검증`으로 유지한다.

---

## 2026-07-27 ChatGPT 문서 정리 추가 인수사항 — 기준 `9e4edaef24dce901fdcf722e2e6d8c0cf0a623ba`

### 현재 운영상 중요 보정

- Codex는 즉시 투입하지 않는다. ChatGPT가 몇 차례 더 개발한 뒤 독립 QA/실환경 검증에 투입한다.
- 따라서 현재 Codex Checklist/Handover는 최종본이 아니며, Codex 투입 직전 최신 master 전체 Diff와 누적 Change Ledger를 기준으로 다시 작성해야 한다.
- 과거 PASS는 무조건 유지하지 않는다. 이후 Source/DB/Generator/Build/Gate 변경의 직접·간접 영향권에 들어오면 `재검증 필요`로 다시 연다.
- 반대로 변경과 무관한 고비용 Browser/Multi-instance/Fault/전체 DB 검증을 습관적으로 반복하지 않는다.

### Artifact 공급 정책

Generated Domain/독립 WAS의 CPF Library 공급은 `LOCAL_DEV` / `REMOTE` / `OFFLINE` 3모드로 정리한다.

- `LOCAL_DEV`: 개발 Source 변경 즉시 반영
- `REMOTE`: Jenkins/CI가 승인된 Nexus/Artifactory 등 고정 Version 사용
- `OFFLINE`: Registry 없는 환경에서 manifest/checksum/BOM을 가진 Offline CPF Library Bundle 사용

CI/STG/PROD는 Local Repository fallback을 허용하지 않고 fail-closed한다.
업무 Domain이 CPF JAR을 수동 복사하는 방식을 표준으로 사용하지 않는다.

### Gate/PowerShell/Tool 정리 정책

정본: `cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`

향후 모든 작업자는 다음을 유지한다.

1. Gate/Tool Inventory 작성
2. `DEV_ONLY` / `CI_RELEASE` / `PRODUCT_ADMIN_TOOL` 분류
3. 중복/Legacy/무호출/일회성 Gate는 실제 Caller와 Requirement 대체 여부 확인 후 통합/삭제
4. 개발 대표 Gate `QUICK` / 작업단위 `VERIFY` / 통합 `FULL` 체계화
5. 공식 Tool은 옵션/Default/환경변수/Side Effect/실패/복구/예제까지 문서화
6. 개발/CI Gate는 Runtime 배포물에 포함하지 않음
7. 고객 관리자에게 필요한 설치/Upgrade/Rollback/Generator/Verify Tool만 별도 관리 Tool로 제공 가능

ChatGPT가 안전하게 삭제를 확정하지 못한 Gate는 억지로 보존 완료 처리하지 않고 `삭제 후보/재확인 필요`로 남긴다.
향후 Codex는 최신 master에서 실제 Consumer, CI 참조, Requirement coverage를 확인한 뒤 필요하면 삭제한다.

### 새 세션 필수 확인

새 ChatGPT/Codex 세션은 이전 대화만 믿지 말고 다음을 먼저 읽는다.

- `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- `cpf-docs/work/current/CPF_NEXT_INTEGRATED_DEVELOPMENT_REQUEST_20260727.md`
- `cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md`
- `cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`
- 이 Continuity State

QA 최종 개선요청이 도착하면 기존 준비 Requirement와 병합하고 중복을 제거한 뒤 구현한다.

## 2026-07-27 ChatGPT 2차 개발 Continuity — 시작 SHA `702bf83580b9c4db2dbba6482ece233e00842f1b`

### 현재 상황

- 사용자가 `20260727_03` 문서 Patch까지 master에 Push했다.
- 이후 `CPF 차기 통합 QA 요구사항`을 전달했다.
- QA는 `9e4edaef...` 기준으로 작성됐으므로 최신 `702bf835...`를 시작 기준으로 재산정했다.
- 이번 ChatGPT 작업은 `CHANGE-SET-A — Stack / Artifact / Baseline Safety`에 한정한다.
- Codex는 아직 투입하지 않는다. 이후 몇 차례 ChatGPT 개발을 더 수행한 최신 master에서 Checklist를 다시 생성한다.

### QA 병합 결론

우선순위:

1. Stack/Artifact/Baseline Safety
2. ADM/BZA DB 원자성·PII·Status·V59/V60·SQL Boundary
3. Generated Domain Golden normalization
4. BAT Legacy/EDU parity
5. Gateway Fault/Multi-instance
6. ADM/BZA Browser/Observability/Lifecycle/Release

### 이번 변경의 핵심 정책

- 현재 Java25/Gradle9.1/Boot3.4.13은 `TRANSITION`이며 Commercial GA로 완료 처리 금지.
- Java25/Gradle9 목표를 유지하고 Boot 4.x는 별도 Migration Change Set에서 검증한다.
- Stack Version은 `gradle/cpf-stack.properties`가 정본이다.
- Artifact 공급은 `LOCAL_DEV / REMOTE / OFFLINE` 중 하나만 사용한다.
- REMOTE/OFFLINE은 개발자 Local Repository fallback 금지.
- Local auto-sync 기본값은 false.
- Local public artifact는 `aggregate quality → isolated staging → identity/hash verify → manifest barrier promotion` 이후에만 공개한다.
- Remote publish는 `cpfInternal` 전용 task만 사용하여 Local Repository를 건드리지 않는다.
- Offline은 개별 JAR 복사가 아니라 versioned Maven Bundle을 사용한다.

### Batch Scheduler 인스턴스 정책 확인

현재 BAT는 자정 일괄 Job Instance 선생성 구조가 아니다.

- `bat_schedule`: Cron/Calendar/Window/Timezone/next_fire_at DB 정본
- `cpf.batch.scheduler.dispatch-ms`: DB polling interval Property
- due 시 `bat_schedule_trigger` + CPF `bat_execution READY` 생성
- Worker가 `JobLauncher.run()`할 때 Spring Batch JobInstance/JobExecution 생성

상세 정본:
`cpf-docs/guides/CPF_BATCH_SCHEDULER_INSTANCE_LIFECYCLE_GUIDE.md`

### 검증 재개방

이번 Build/BOM/Plugin/Generator 변경 때문에 다음 과거 PASS는 다시 검증 대상이다.

- 전체 Java compile/test
- Included Build BOM/Plugin
- Generated standalone Domain
- bootJar/bootWar
- Artifact publication/package

BAT 158 SQL/V58 자체는 직접 변경하지 않았으므로 즉시 전체 반복하지 않는다. 최종 aggregate/historical lifecycle에서만 필요한 범위를 다시 확인한다.

### Codex 후속 필수

Codex를 실제 투입할 때는 현재 문서를 그대로 쓰지 말고 **그 시점 최신 master의 누적 ChatGPT Diff**로 Checklist를 재산정한다.
특히 이후 ChatGPT 변경으로 영향받은 기존 PASS를 다시 연다.
ChatGPT 구현 보고를 완료 근거로 사용하지 않는다.

### Git 권한

이전 세션의 Push 승인은 승계되지 않는다. 사용자 명시 승인 없이 Commit/Push/Branch/Tag/Release 금지.

- 상세 QA 입력 보존본: `cpf-docs/work/review/CPF_QA_INPUT_20260727_04.md`
