# 20260725 ChatGPT R7 구현/검수 보고서

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- 기준 master: `6ceea6642c9bd35f7e94dd82d03ec1b441024135` (`20260725_01`)
- 상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 본 산출물은 patch ZIP이며 commit/push를 수행하지 않는다.

## 사용자 1~7 요구사항 검토와 R7 처리

| 요구 | master에서 확인한 문제 | R7 정적 구현 | 판정 |
|---|---|---|---|
| 1. DB Vendor 동일 관리 | `cpf-tools/db/source/mariadb`만 별도 존재, 다른 Vendor와 ownership 비대칭 | Platform source를 `cpf-tools/db/vendor/<vendor>/source` 계약으로 통일. MariaDB source 이동, source plan/build/manifest/gate 현행화. 미구현 Vendor는 MariaDB fallback 없이 fail-closed | 부분 구현 |
| 2. Root compose 정리 | Root `docker-compose.local.yml` 잔존 | `deploy/local/docker-compose.local.yml`로 이동하는 APPLY 제공, Root 삭제 | 부분 구현 |
| 3. cpf-tools Guide | Tool 진입점/책임/안전 규칙이 분산 | `cpf-tools/README.md`, `CPF_TOOLS_GUIDE.md` 추가 | 부분 구현 |
| 4. 전체 요청 목록 순회 | 일부 구현 후 중단점이 문서와 실제 구현에 섞임 | Current Request의 EXS/DB 경로 충돌 교정, Continuity/Decision/Report 갱신. 전체 P0/P1 Runtime 완료는 계속 추적 | 부분 구현 |
| 5. ADM/BZA Vue 표준 | ADM `App.vue` 약 71KB, BZA `features/console.ts` 약 22KB 집중 | App Shell + feature package + route/menu registry + 공유 state/API boundary + lazy dynamic import. 외부 CDN/remote asset 0 구조 | 부분 구현(브라우저 미검증) |
| 6. 교차 검수 Report | 작업 결과를 다음 Agent가 재현하기 어려움 | 본 Report + Handover + Continuity/Decision 갱신 | 부분 구현 |
| 7. 종료/가비지/인수인계 | Root compose/log 등 정리 필요 | compose 이동, untracked root logs 조건부 제거, stale BZA console 삭제, `.gitignore` 깨진 주석 교정, repository hygiene 재발 방지 규칙 보강 | 부분 구현 |

`부분 구현`은 실패가 아니라 Runtime/DB/Browser 또는 타 Vendor 구현이 남아 있어 제품 완료로 승격할 수 없다는 의미다.

## R6.1 실패 원인 교정

`sync-database-artifacts.ps1`은 하위 PowerShell script를 같은 process에서 `&`로 호출한 뒤 `$LASTEXITCODE`를 검사했다.
하위 script가 성공해도 내부 native command의 과거 exit code가 남을 수 있어 정상 bundle 생성을 실패로 오판했다.

R7은 각 gate를 별도 `pwsh` process로 실행한다.

1. `build-all-install-sql.ps1`
2. `generate-database-schema-manifest.ps1`
3. `check-database-schema-drift.ps1`
4. `check-database-profile-standard.ps1`

따라서 부모가 확인하는 `$LASTEXITCODE`는 해당 gate process의 실제 종료코드다.

## DB Vendor source ownership

정본 경계:

```text
cpf-tools/db/vendor/<vendor>/
  source/             # 사람이 수정하는 해당 Vendor Platform SQL 정본
  domain-template/    # Generated Domain Golden Vendor template
  provision/
  install/
  seed/
  migration/
  runtime/
  verify/
  rollback/
```

현재 Platform full source 구현은 MariaDB다. MySQL/PostgreSQL/Oracle/SQL Server를 MariaDB SQL 복사로 채워 완료 처리하지 않는다.
각 Vendor는 같은 path/manifest 계약을 갖되 미구현은 명시적 fail-closed 상태로 남긴다.

## EXS Generated Domain

확정:

- 옛 fixed `cpf-external` Source를 수동 복구하지 않는다.
- Platform 기본 install/seed에는 `exsDB`, `45_external_schema.sql`, `57_external_seed_data.sql`을 넣지 않는다.
- 필요하면 Golden Generator로 `DomainName=external`, `SystemCode=EXS`를 생성한다.
- EXS 전용 Generator switch/template을 만들지 않는다.
- 생성된 `cpf-external`은 `domain-manifest.json`과 `generator-ownership.json`으로 Generated Domain임을 증명해야 한다.
- 대외 공통 EDU는 `cpf-reference`, 공통 기술 SPI는 `cpf-core`, 고객 공통 정책은 `cpf-common`이 소유한다.

R7 APPLY는 기본으로 `external/EXS`를 Generator 생성하고 static verify한다.

## ADM/BZA Frontend

### ADM

- 거대 `App.vue`에서 24개 운영 패널을 분리
- `observability / platform / business / batch / access` lazy feature panel
- 기존 API method contract 재사용
- 공유 console state와 menu registry 분리
- App은 로그인/Navigation/Feature loader 중심

### BZA

- `features/console.ts` 삭제
- `app/routes.ts` route registry
- auth/session API boundary
- dashboard/directory/access/approval/support lazy page
- 재사용 CRUD/DataTable/Metric component
- 기존 BZA endpoint contract를 유지

두 Frontend 모두 runtime 외부 URL/CDN/CSS/font/icon을 사용하지 않는 로컬 CSS 구조다.

## 정적 검증

이 patch 산출 환경에서 수행:

- Vue/TypeScript `<script>` transpile syntax: PASS
- Vue top-level template/script/style tag count scan: PASS
- Frontend `http://`/`https://` runtime source scan: 0
- BZA old `features/console.ts`: patch 삭제 대상
- ADM App shell 크기 축소, feature 파일 분리 확인
- EXS/DB/Root 정책 문서 상호 충돌 문자열 재검사
- 현행 117 Table 독립 parser cross-check: Index 276, FK 96, missing local/referenced column/table 0, Platform EXS object 0
- 기존 FK 0건 manifest parser 결함(multiline constraint 미인식)을 보강

PowerShell, Gradle, npm package install, MariaDB Runtime은 이 산출 환경에서 직접 실행할 수 없으므로 성공으로 기록하지 않는다.

## Current Request 전체 잔여 범위

다음은 본 R7에서 제품 완료로 판정하지 않는다.

- P0 DB Fresh Install / partial batDB 복구 / V39 실제 적용
- P0 Central Multi-Vendor Platform pack의 MySQL/PostgreSQL/Oracle/SQL Server 실제 구현·Runtime
- P0 ADM Owner Boundary 및 위험조치 Approval의 전체 Runtime/Browser
- P0 BZA 조직/직원/다중 Role/업무결재 전체 Runtime/Browser
- P0 Batch/Center-Cut multi-instance/lease/fencing/runtime
- P1 Migration/Upgrade/Rollback 실제 실행 Evidence
- P1 Protected Baseline 전체 회귀
- P1 Generator create/build/test/db/remove/regenerate 전 lifecycle
- Security/Operations/Product 전체 Evidence closure

따라서 R7은 “전체 요청 종료”가 아니라 다음 Agent가 거짓 완료 없이 이어갈 수 있는 구조 교정 + 실제 소스 패치다.

## 사용자 PC에서 반드시 실행할 검증

1. R7 APPLY 자체 PASS
2. `sync-database-artifacts.ps1` PASS
3. Generated `external/EXS` manifest/static verify
4. Gradle clean test/assemble
5. ADM/BZA `npm run verify` 또는 test/build
6. Browser E2E
7. partial `batDB` 상태 확인 후 명시 삭제
8. `initialize-cpf-database.ps1 -All -RequireRun`
9. BAT `parent_segment_id`, V39, EXS Platform object 0건 실제 DB 검증
10. Generated EXS build/test/DB bootstrap/runtime/remove-regenerate

실행하지 않은 검증은 `미검증`으로 유지한다.
