# R7 Static Validation

기준 master: `6ceea6642c9bd35f7e94dd82d03ec1b441024135`

본 파일은 Runtime Evidence가 아니라 patch 작성 환경에서 수행한 정적 검증 기록이다.

## DB canonical SQL parser cross-check

현행 master의 MariaDB split schema 5개(`10/20/30/35/40`)를 R7 manifest parser와 동일 규칙으로 독립 재분석했다.

- Table: 117
- Secondary Index: 276
- Foreign Key: 96
- Index missing local column: 0
- FK missing local column: 0
- FK missing referenced table: 0
- FK missing referenced column: 0
- FK local/referenced column count mismatch: 0
- Platform EXS object(`exsDB`, `exs_*`): 0

기존 tracked manifest가 FK 0건이었던 원인은 실제 DDL의 FK가 multiline인데 line-by-line parser만 사용했기 때문이다.
R7은 Table body 전체 multiline FK parser와 2차 referenced table/column 검증을 추가한다.

## Frontend

- ADM/BZA TypeScript/Vue script transpile syntax: PASS
- `.vue` top-level template/script/style count scan: PASS
- R7 ADM/BZA source의 `http://`/`https://` remote Runtime asset reference: 0
- BZA old `features/console.ts`: APPLY delete target
- ADM `App.vue`: App Shell 중심으로 축소하고 5개 lazy feature panel로 분리
- BZA: lazy route registry + feature page/component 구조

## Config/Bundle

- `database-source-plan.json`: JSON parse PASS
- `database-vendor-coverage.json`: JSON parse PASS
- `R7_DELETE_MANIFEST.json`: JSON parse PASS

## 직접 실행하지 못함

patch 작성 환경에는 PowerShell/MariaDB/프로젝트 node_modules가 없으므로 다음은 `미검증`.

- R7 APPLY/VERIFY 실제 PowerShell 실행
- `sync-database-artifacts.ps1` 실제 process exit
- Gradle build/test
- npm unit/build
- Browser E2E
- MariaDB install/upgrade/runtime
