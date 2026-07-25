# 20260725 R7 Cross-PC / Cross-Agent Handover

## 기준

- Base master: `6ceea6642c9bd35f7e94dd82d03ec1b441024135`
- R7은 patch ZIP이다. ChatGPT는 commit/push하지 않았다.
- EXS 정책: Generated Domain only.

## 적용

1. ZIP을 CPF ProjectRoot 밖 임시 폴더에 해제한다.
2. `APPLY_R7_20260725.ps1 -ProjectRoot <CPF root>`를 실행한다.
3. APPLY가 MariaDB source ownership 이동, Root compose 이동, patch overlay, stale 파일 삭제, DB artifact sync를 수행한다.
4. 기본 동작으로 Golden Generator가 `external/EXS`를 생성하고 static verify한다.
5. 기존 Generated EXS가 있으면 manifest 계약이 맞는 경우만 유지한다.
6. `VERIFY_R7_20260725.ps1`과 `git diff --check`로 정적 상태를 재검증한다.
7. commit/push는 사용자가 결과를 검수한 뒤 직접 한다.

## APPLY가 하지 않는 것

- Git commit/push/branch 생성
- DB drop/reset
- partial batDB 자동 삭제
- 실제 MariaDB `-All` 설치
- Gradle/npm/Browser Runtime 성공 주장

## 다음 Agent 첫 검수

- `cpf-tools/db/source/mariadb`가 제거됐는가
- `cpf-tools/db/vendor/mariadb/source`가 Platform MariaDB canonical source를 소유하는가
- `sync-database-artifacts.ps1`가 stale `$LASTEXITCODE` 오판 없이 PASS하는가
- schema manifest가 새 source root에서 결정적으로 재생성되는가
- Platform default install/seed/schema manifest의 EXS object가 0건인가
- `cpf-external`이 존재한다면 Generated Domain ownership manifest를 가지는가
- `settings.gradle`의 `cpf-external` 등록이 Generated EXS와 함께 생성됐는가
- ADM/BZA가 외부 remote asset 없이 build되는가
- BZA `features/console.ts`가 제거됐는가
- ADM App Shell과 BZA route registry가 lazy chunk를 생성하는가

## DB Runtime 다음 순서

현재 사용자 PC의 `batDB`는 직전 실패로 부분 생성 가능성이 있다.

1. 현재 `batDB` 상태 확인
2. partial `batDB`만 명시적으로 삭제
3. `pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun`
4. 이미 정상 설치된 앞쪽 DB는 schema drift 검사 후 SKIP되는지 확인
5. BAT부터 정상 진행하는지 확인
6. `bat_center_cut_item/result.parent_segment_id`와 V39 실제 검증
7. EXS Platform table/seed 0건 검증

## Frontend Runtime 다음 순서

ADM/BZA 각각:

- lockfile 기반 install
- unit test
- production build
- Browser 로그인/권한/menu/CRUD/approval/error flow
- Browser network remote asset 0건
- lazy chunk 생성 확인

## 금지

- 옛 fixed `cpf-external` Source 수동 복구
- `45_external_schema.sql`/`57_external_seed_data.sql` 복구
- EXS 전용 Generator template/switch 추가
- 타 Vendor를 MariaDB 복사로 완료 처리
- sync gate 실패를 무시하고 DB Runtime 완료 처리
- Browser/DB 실행 없이 `완료` 표기
