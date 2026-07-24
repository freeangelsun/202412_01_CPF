# CPF Codex Continuity State

## 1. 현재 기준선

- 갱신 시각: `2026-07-24T15:28:06+09:00`
- Repository: `https://github.com/freeangelsun/202412_01_CPF.git`
- Branch: `master`
- 기준 HEAD: `73f9b5fb63d791ffd77a172e1e66c3f44ddfdfe5`
- 기준 `origin/master`: `73f9b5fb63d791ffd77a172e1e66c3f44ddfdfe5`
- Ahead/Behind: `0/0`
- 시작 Worktree: `완료` — clean
- Commit/Push/Branch 생성: `미구현` — 사용자 승인 전 수행하지 않음

## 2. 작업 환경

- PC: `BOOK-JDJDKA9AHA`
- OS: `Microsoft Windows 10.0.26200`, x64
- PowerShell: `pwsh 7.6.4`
- Java: `Temurin OpenJDK 25.0.3`
- Node.js: `v24.11.0`
- npm: `11.6.1`
- MariaDB client: `12.3.2`
- Docker CLI: `미구현` — 설치 명령을 찾지 못함
- DB host/port/credential: 기록하지 않음

## 3. 현재 작업 단계

- 단계: 전체 DB 소비자 교차분석·Schema 최소화·중앙 Multi-Vendor Pack 구현
- 상태: `부분 구현`
- 우선순위: 불필요 Table·Index·Seed·Query 제거, MBR/ACC/BZA 정책 충돌 동시 전환 후 MariaDB 재설치

## 4. 완료한 작업

- `완료` — `git fetch origin` 실행
- `완료` — HEAD, `origin/master`, branch, ahead/behind와 시작 Worktree 확인
- `완료` — `git log -5`, `git diff --check` 실행
- `완료` — OS/JDK/Node/npm/PowerShell/MariaDB client 환경 확인
- `완료` — 최상위 정본 6종 전체 확인
- `완료` — `cpf-docs/work/review/20260722_03/*` 전체 확인
- `완료` — Architecture/Developer/API/Security/Installation/Migration/Deployment/Operator/Recovery/Generator/EDU 관련 Guide 확인
- `완료` — Stale Evidence, Generated Matrix, 조기 DOCX, 중복 README와 Release Notes 삭제를 되돌리지 않는 정책 확인
- `완료` — Continuity State와 Decision Log 저장 체계 생성
- `완료` — 현재 split SQL의 Schema/Table inventory 생성
- `완료` — Flyway V1~V37 manifest와 실제 SHA-256 대조
- `완료` — Flyway 파일별 Git 이력과 WIP rename Commit 확인
- `완료` — 추가 Steering의 DB preflight, Multi-Vendor, Generator와 Minimal Transaction Reference Schema 원칙을 Decision Log에 반영
- `완료` — `cmnDB` split DDL을 `cmn_sample_item` 한 개 Table로 표준화
- `완료` — CMN Sample datasource를 명시적 opt-in과 DB-less 기본값으로 변경
- `완료` — CMN Sample CRUD, 검색, offset/cursor slice, duplicate, optimistic lock와 rollback Service/API 구현
- `완료` — 기존 Common 채번·알림·업무 로그 Source와 Test 제거
- `완료` — 변경된 `cpf-common`과 `cpf-reference` Java compile 실행
- `완료` — 범용 Fixed-Length Engine을 DB 비의존 `cpf-core` API/SPI/기본 구현으로 이동하고 기존 CMN 중복 구현 제거
- `완료` — DB 없는 member test가 운영 정책을 약화하지 않고 fail-closed로 기동되도록 테스트 전용 startup DB access opt-out 구현
- `완료` — `ref_sample_item` Minimal Transaction Reference Schema 첫 인스턴스와 REF 소유 datasource/transaction/Mapper 경계 구현
- `완료` — `CpfDatabaseVendor`, Vendor URL/Driver 검증, 5개 Driver runtime packaging 계약 구현
- `완료` — MyBatis SQL을 `mybatis/vendor/{vendor}/mapper/{module}` resource pack으로 선택하는 fail-closed resolver 구현
- `완료` — 기존 Core/Common/MBR/ACC/REF Mapper XML 10개를 MariaDB pack으로 이동
- `완료` — Repository SQL 파일 선택용 `sql/vendor/{vendor}/{module}/{statement}.sql` catalog 구현
- `완료` — Vendor lifecycle/resource manifest와 선택 Shell 초안 구현
- `완료` — 비파괴 SQL bundle builder와 exact allowlist/reset dry-run 기본 Shell 구현
- `완료` — Provision/Empty Install/Product Seed/Optional Seed/Test Seed/Verify bundle을 비파괴 경로로 재생성하고 현행 SQL 정적 표준 Gate 통과
- `완료` — `cpf_schema_installation`에 9개 CPF Schema의 MariaDB 제품 Version/Baseline/Product Seed 상태 계약 추가
- `완료` — 공식 `initialize-cpf-database.ps1` 실행 경로 초안 구현; Source SQL 외 임시 DDL 없이 Resource Pack만 실행
- `완료` — Module-local 5 Vendor SQL 확산 금지와 `cpf-tools/db/vendor/<vendor>` 중앙 물리 소유권을 DEC-015로 확정
- `완료` — PC 재부팅 후 `git fetch origin`, HEAD/origin/master/ahead-behind, Dirty WIP와 MariaDB Service를 재확인
- `완료` — 실제 로컬 MariaDB 12.3.2에서 공식 Resource Pack으로 9개 Schema Provision, Empty Install, Product Seed와 Verify Baseline 확보
- `완료` — 실제 DB에서 정본 127 Table, 304 Constraint, 438개 명시 Index 누락 없음 확인; 실제 Index 459개에는 FK 자동 생성 Index 포함
- `완료` — 9개 Runtime Application 계정 실제 로그인과 DDL 권한 0개 확인
- `완료` — 기본 관리자 Credential을 Product Seed에 넣지 않는 보안 정책에 맞춰 잘못된 `adm_operator` 필수 Seed 검사를 `adm_role`로 보정
- `완료` — Generator가 예시 Domain 고정 목록이 아닌 Metadata + 중앙 Vendor Domain Template으로 확장되는 결정을 DEC-016으로 확정
- `완료` — 실제 설치된 127개 Table을 Java/MyBatis/Repository/Installer/Framework 동적 Consumer와 정본 요구에 교차 대조
- `완료` — 소비자 0개이며 활성 원장과 중복인 `cpf_file_exchange_log`, `adm_operation_log`, `bza_user_role`과 EXS 구 중복 원장 6개를 현행 split DDL에서 제거
- `완료` — MariaDB Spring Batch의 잘못된 `BATCH_*_SEQ` Table 3개를 Spring Batch 5.2.4 MariaDB 정본 `CREATE SEQUENCE` 3개로 보정
- `완료` — 명확한 left-prefix 중복 Index 3개를 현행 split DDL에서 제거
- `완료` — `cpf-tools/db/vendor/<vendor>` 중앙 Pack, 선택 Vendor 전용 격리 Overlay, fail-closed manifest/selector와 parity gate 구현
- `완료` — 중앙 Pack parity 311쌍, MariaDB executable selection, 5 Vendor × 7 generated-domain template 정적 검증

## 5. 진행 중인 작업

- `부분 구현` — Source·SQL·Test·Config·Frontend와 문서 주장 대조
- `부분 구현` — Flyway V6/V29 checksum 불일치 원인과 보존/복구 경계 감사
- `부분 구현` — DB bootstrap/install/reset/provision/seed/verify 책임 분리 구현
- `부분 구현` — `batDB`와 `bat_*` Physical Ownership 구현
- `완료` — Fixed-Length Engine의 Core API/SPI Ownership 구현
- `부분 구현` — 공식 Module 전체 Multi-Vendor SQL resource pack 선택과 논리 Schema parity 구현
- `부분 구현` — Module-local WIP SQL의 Consumer를 확인한 뒤 중앙 Vendor Pack 외부 resource/generated overlay로 전환
- `부분 구현` — Generator 5 Vendor resource pack 및 lifecycle 구현
- `부분 구현` — Java inline Vendor SQL을 Repository resource로 외부화
- `부분 구현` — 삭제 산출물을 참조하는 Quality Gate와 UTF-8/Shell 회귀 감사
- `부분 구현` — MBR/ACC를 동일 `*_sample_item` Minimal Transaction Reference Schema로 전환
- `부분 구현` — 정본에서 금지한 BZA 고객·상품·주문 원장과 연결 API/UI/Test/Seed 제거
- `부분 구현` — Schema 최소화 후 공식 MariaDB allowlist Reset → Fresh Install → Seed → Verify 재검증

## 6. 아직 시작하지 않은 작업

- `미구현` — MySQL/PostgreSQL/Oracle/SQL Server 제품 전체 Install/Seed/Migration/Verify/Rollback SQL pack
- `부분 구현` — Minimal Transaction Reference Schema Template 및 MBR/ACC/REF/Generator 적용
- `부분 구현` — Generator create→db-init→migration→verify→build→runtime→remove→regenerate lifecycle parity
- `미검증` — `package-lock.json` integrity와 Frontend 전체 검증
- `미검증` — Backend clean build/unit/integration/package
- `완료` — 빈 MariaDB provision/empty install/product seed/verify 및 기존 Baseline 재검증
- `미검증` — 전체 Module startup와 API Runtime
- `미검증` — Multi-instance, Browser, Upgrade, Rollback, Backup/Restore와 Recovery
- `미구현` — 최신 기준 Evidence와 Matrix/Guide 동기화

## 7. 변경 중인 주요 파일·모듈

- `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
- `cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md`
- `specs/sql/01_create_databases.sql`, `02_create_service_users.sql`, `10_cpf_schema.sql`, `20_cmn_schema.sql`, `35_bat_schema.sql`, `50_framework_seed_data.sql`, `55_cmn_seed_data.sql`, `70_test_data.sql`, `99_smoke_check.sql`
- `cpf-common` CMN Sample datasource·Service와 기존 업무 DB 잔재
- `cpf-core` Fixed-Length API/SPI
- `cpf-reference` CMN Sample API·Mapper·Fixture
- `cpf-core` DB Vendor/DataSource/SQL resource selector와 DB-less test policy
- Core/Common/MBR/ACC/REF의 `mybatis/vendor/mariadb` Mapper pack
- Core/ADM/BZA의 WIP `sql/vendor/*` Repository Query pack — 중앙 Pack 전환 전 보존, 신규 확산 중단
- `scripts/build-all-install-sql.ps1`, `initialize-cpf-database.ps1`, `reset-cpf-databases.ps1`, `select-db-vendor-resources.ps1`
- `specs/sql/vendor-resource-manifest.json`
- 중앙 경로 `cpf-tools/db/vendor/<vendor>`와 외부 Runtime resource/generated overlay 연결
- `specs/sql/10_cpf_schema.sql`, `30_adm_schema.sql`, `35_bat_schema.sql`, `40_business_modules_schema.sql`, `45_external_schema.sql`
- MBR/ACC/BZA Source·API·Test·Frontend와 중앙 MariaDB Runtime Query

## 8. 실제 실행한 검증

| 검증 | 상태 | 결과 |
|---|---|---|
| `git fetch origin` | 완료 | 성공 |
| `git status --porcelain=v2 --branch` | 완료 | `master`, upstream `origin/master`, `+0/-0`, 시작 Worktree clean |
| `git rev-parse HEAD` / `origin/master` | 완료 | 모두 `73f9b5fb63d791ffd77a172e1e66c3f44ddfdfe5` |
| `git diff --check` | 완료 | 출력 없음, exit code 0 |
| 환경 Version 확인 | 완료 | 위 작업 환경과 일치 |
| `scripts/check-sql-canonical.ps1` | 실패 | `V6__bizadm_exs_transaction_identity.sql`에서 중단; 별도 전수 대조 결과 V6·V29 manifest 불일치 |
| Flyway SHA-256 전수 대조 | 실패 | 37개 중 V6·V29 불일치, 나머지 35개 일치 |
| `scripts/check-sql-standard.ps1` | 완료 | CMN 단일 `cmn_sample_item`, BAT 소유권과 설치 bundle 정합성 기준으로 수정 후 성공 |
| DB 접속과 Schema 확인 | 부분 구현 | TCP server 응답까지 확인, 인증 실패로 Version/Schema query는 미실행 |
| MariaDB passwordless preflight | 실패 | `127.0.0.1:3306`, `root`, password 없음으로 read-only Version/Schema query 시 `ERROR 1045`; Credential 환경 변수는 모두 미설정 |
| MariaDB local protocol preflight | 실패 | PIPE는 named pipe 없음, SOCKET은 `root@localhost` 인증 거부, 지원하지 않는 MEMORY는 client가 거부 |
| 공식 MariaDB initializer | 실패 | Script/Resource 선택은 실행됐으나 Service password 환경 변수가 없어 SQL 실행 전 fail-closed; 관리자 인증도 별도 preflight에서 거부됨 |
| 재부팅 후 Git/환경 재확인 | 완료 | HEAD=`origin/master`=`73f9b5f...`, ahead/behind `0/0`, 기존 Dirty WIP 보존, MariaDB Service Running |
| 실제 MariaDB Fresh Provision/Install/Seed | 완료 | 최초 Schema 0개에서 공식 `00_provision.sql`, `00_empty_install.sql`, `00_product_seed.sql` 실행 성공 |
| 실제 MariaDB 구조 검증 | 완료 | Schema 9, Table 127/127, Constraint 304/304, 명시 Index 438개 누락 0, 실제 Index 459 |
| 실제 MariaDB Product Seed | 완료 | Schema Baseline 9, CPF Code 42, Response Code 29, ADM Role 5, BAT Job 5, EXS Institution 1 |
| 실제 MariaDB Service User/Grant | 완료 | Application 계정 9개 로그인 성공, DDL 권한 0 |
| 실제 MariaDB Verify SQL | 완료 | `00_verify.sql` 성공, 출력 207행; Optional/Test Seed는 실행하지 않음 |
| 전체 127 Table 실제 Consumer 교차분석 | 완료 | 고신뢰 중복/무소비 9 Table, 잘못된 Sequence Table 3개, 중복 Index 3개 확인 및 split DDL 보정 |
| 중앙 Vendor Pack parity | 완료 | 311쌍 일치, MariaDB Runtime 61개·Domain Template 7개 선택 성공 |
| 비-Maria Vendor executable selection | 완료 | MySQL/PostgreSQL/Oracle/SQL Server 상태 `미구현`을 fail-closed로 거부 |
| `:cpf-common:compileJava :cpf-reference:compileJava` | 완료 | 성공 |
| `:cpf-core:test :cpf-common:test :cpf-member:test :cpf-account:test :cpf-reference:test` | 완료 | 성공; Vendor driver/resource selector와 이동한 MariaDB Mapper pack 포함 |
| Fixed-Length 집중 검증 | 완료 | Core 193/Common 7/Reference 49 tests, 실패 0; 전체 10 Module compile 성공 |
| DB-less member 회귀 검증 | 완료 | Core 199/Member 16 tests, 실패 0; 운영 기본 DB access 유지, test opt-out만 적용 |
| Multi-Vendor 실제 DB Runtime | 미검증 | MariaDB 인증 정보 없음; MySQL/PostgreSQL/Oracle/SQL Server 실행 환경 없음 |
| 전체 Build/Test/Runtime/Browser | 미검증 | 변경 후 전체 검증은 아직 실행하지 않음 |

## 9. DB·Runtime 현재 상태

- 사용자 확인 조건: 회사 PC와 집 PC 모두 CPF Schema/Table이 없는 초기 환경
- 로컬 MariaDB client: `완료` — `12.3.2` 실행 파일 확인
- MariaDB Windows Service: `완료` — `MariaDB`, Running, Automatic
- DB Vendor/Server Version: `완료` — MariaDB `12.3.2`
- DB host/port: `완료` — Local TCP 연결 및 실제 SQL 실행
- DB Credential: `완료` — 사용자 제공 Local Credential을 해당 Process 환경에서만 사용, Source/문서/로그에 값 미기록
- CPF Schema 존재 여부/Schema Version/Migration 상태: `부분 구현` — 9개 Schema와 `1.0.0-SNAPSHOT` Product Seed Baseline 9행 확인; Historical Flyway 실제 적용 이력은 없음
- CPF Empty Install: `완료`
- CPF Product Seed/Verify: `완료`
- CPF Runtime: `미검증`
- Docker 기반 대체 Runtime: `미구현` — Docker CLI 없음
- 현재 split SQL 정적 inventory:
  - Schema 생성: `cpfDB`, `cmnDB`, `admDB`, `batDB`, `refDB`, `exsDB`, `mbrDB`, `bzaDB`, `accDB`
  - `cmnDB`: `cmn_sample_item` 1개로 split DDL 정적 표준화 `완료`
  - Batch/Spring Batch/Center-Cut Table: `batDB` 소유, MariaDB Spring Batch Sequence 보정 `완료`; 실제 재설치·Runtime은 `미검증`
  - 최적화 전 실제 Baseline은 127 Table이며 최적화 후 split DDL은 재생성·실제 재설치 전이므로 최종 수량은 `재확인 필요`

## 10. Blocker와 미검증

- `완료` — 실제 MariaDB 접속과 관리자/Service 계정 권한을 Secret 비기록 방식으로 확인
- `재확인 필요` — V6/V29 manifest hash는 현재 파일뿐 아니라 해당 파일의 Git blob 이력과도 일치하지 않음. 생성 시점의 미Commit 상태 또는 잘못된 manifest 가능성을 더 확인해야 함
- `재확인 필요` — 적용 고객/운영 DB 존재 여부는 Repository만으로 확정할 수 없음. Historical Flyway 파일은 변경 금지
- `완료` — 현행 MariaDB 설치/seed/verify split SQL과 bundle을 `cmnDB` 최소화 및 Batch Physical Ownership 정본에 맞춤
- `완료` — 공식 초기화 실행 Credential 입력 Blocker 해소
- `미검증` — MySQL/PostgreSQL/Oracle/SQL Server 제품 SQL pack과 Runtime은 아직 완료로 판정하지 않음
- `부분 구현` — Core 일부와 ADM/BZA inline SQL 외부화 WIP가 Module-local resource를 만들었으므로 중앙 Vendor Pack으로 안전하게 전환해야 함

## 11. 다음 정확한 작업 순서

1. MBR/ACC를 동일 `*_sample_item` Minimal Transaction Reference Schema와 Source/API/Test로 전환한다.
2. BZA 금지 원장 `customer/product/order`와 전용 masking audit, 연결 API/UI/Test/Seed를 함께 제거한다.
3. Generator 임의 Domain Metadata + 중앙 `domain-template` PAY/INS lifecycle과 고정 목록 부재를 최종 확인한다.
4. bundle과 중앙 MariaDB Pack을 재생성하고 SQL standard/parity/build/test를 실행한다.
5. exact CPF 9 Schema allowlist Reset dry-run을 확인한 뒤 로컬 개발 DB를 Reset하고 공식 Provision → Empty Install → Product Seed → Verify를 재실행한다.
6. 중앙 Pack 우선 MariaDB Runtime Query가 성공한 뒤 Module-local Vendor 복제본·Dead Code·빈 폴더를 정리한다.
7. MySQL/PostgreSQL/Oracle/SQL Server는 정적 Template과 fail-closed 상태를 유지하고 실제 환경 없이 완료로 기록하지 않는다.
8. 전체 clean build/test와 Module Runtime, CRUD/거래/Paging/Batch/ADM/BZA를 검증하고 문서/Evidence를 동기화한다.

## 12. 다시 수행하면 안 되는 작업·확정 사항

- reset/revert/clean/checkout으로 사용자 변경을 폐기하지 않는다.
- Stale Evidence, Generated Matrix, 조기 DOCX, 중복 README와 Release Notes를 복구하지 않는다.
- Repository 전체 `PFW→CPF`, `XYZ→REF` 문자열 치환을 반복하지 않는다.
- Historical Flyway를 checksum 맞춤 목적으로 수정하지 않는다.
- 첫 Empty Install의 선행 조건으로 Reset을 요구하지 않는다.
- `cmnDB`에는 기본 제품 sample table 1개만 둔다.
- `cmn_sequence*`를 Core/Common 기본 제품에 되살리지 않는다.
- DB Vendor 전환을 위해 Java 업무 Source를 수정하거나 Module을 fork하지 않는다.
- Vendor 선택은 Install/Seed/Migration/MyBatis·Repository Query/Verify/Rollback SQL resource pack 전체에 동일하게 적용한다.
- 선택 Shell은 Source를 덮어쓰지 않고 격리된 실행 경로/패키징 resource location을 선택한다.
- Vendor SQL 정본을 개별 Module `src/main/resources`에 5벌씩 복제하지 않는다.
- 중앙 Pack Consumer와 실제 MariaDB Runtime Query가 성공하기 전에 현재 Module-local WIP SQL을 삭제하거나 이동하지 않는다.
- Vendor별 SQL 이름만 바꾼 복제본을 지원 완료로 기록하지 않는다.
- 운영 Application에 Schema/User 생성용 관리자 권한이나 암묵 DDL을 부여하지 않는다.
- MBR/ACC/REF/Generator마다 서로 다른 임의 Sample 원장을 새로 설계하지 않는다.
- MBR/ACC/REF/PAY/INS 등 예시 Domain/SystemCode를 Generator 또는 DB Tool의 고정 지원 목록으로 하드코딩하지 않는다.
- 신규 Generated Domain 추가 때문에 중앙 Tool의 Java Source나 switch/if를 수정하지 않는다.
- Batch Runtime state는 `cpf-batch`와 `batDB`/`bat_*`가 소유한다.
- 범용 Fixed-Length Engine은 `cpf-core`, 기관별 Adapter는 `cpf-external`이 소유한다.
- 실행하지 않은 Build/DB/Runtime/Multi-instance/Browser/Upgrade/Rollback/Recovery를 완료로 기록하지 않는다.
- 사용자 승인 전 Commit, Push 또는 Branch를 생성하지 않는다.
