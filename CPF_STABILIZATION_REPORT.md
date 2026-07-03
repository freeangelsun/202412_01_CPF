# CPF Stabilization Report

## 1. 기준 정보

- 작업 시작 branch: `master`
- 작업 시작 HEAD SHA: `7a7f0530ba37292df39ebf50a00baf4312ab120b`
- 작업 시작 origin/master SHA: `7a7f0530ba37292df39ebf50a00baf4312ab120b`
- 작업 시작 마지막 commit: `7a7f053 20260702_05`
- 작업 시작 status: `CPF_NEW_REQUEST.md`, `CPF_STABILIZATION_REPORT.md`가 수정 상태였습니다.
- `CPF_NEW_REQUEST.md` 변경 상태/사유: 사용자가 작업 전 신규 요청서를 갱신한 원본 파일입니다. 이번 작업에서는 기능 검수 대상에서 제외하고 편집하지 않았습니다.
- 별도 변경 파일 목록 산출물: 생성하지 않았습니다.
- Git commit/push/branch 생성: 수행하지 않았습니다.

## 2. 수행 작업

- XYZ EDU CRUD를 메모리 저장소 샘플에서 `cmn_edu_query_item` DB Mapper 기반 샘플로 전환했습니다.
- `/xyz/edu/items` 표준 경로와 기존 `/xyz/edu/crud-items` 호환 경로를 함께 제공했습니다.
- 등록, 수정, 상태 변경, 논리 삭제, 검색, 상태 필터, sort whitelist, limit 검증 흐름을 Controller-Service-Repository-Mapper-Test로 연결했습니다.
- `xyz_edu_query_fixture.sql`을 canonical fixture로 정리하고, `XyzQueryEducationMapperSliceTest`에 CRUD DB slice 검증을 추가했습니다.
- `XyzServiceCallEducationController`의 `CpfWebClient` outbound/header propagation 샘플을 feature evidence에 등록했습니다.
- `scripts/smoke-adm-runtime.ps1`에 `-BuildBeforeRun` 옵션을 추가해 ADM runtime smoke 전에 `:adm:bootJar`를 빌드하도록 보강했습니다.
- ADM permission runtime smoke에 `ADM_VIEWER`의 `GET /adm/api/center-cut/jobs` 허용 검증을 추가했습니다.
- CMN에 `cpf.cmn.message.fixedlength` 패키지를 추가하고 byte 길이 기준 fixed-length parser/formatter skeleton, masking, round-trip 테스트를 작성했습니다.
- `scripts/check-feature-evidence.ps1`에 XYZ CRUD, ADM runtime, CMN fixed-length 증거를 추가했습니다.
- `specs/기능_구현_매트릭스.html`과 이 리포트를 현재 검증 결과 기준으로 갱신했습니다.

## 3. EDU/XYZ 실전 샘플

- CRUD: 완료. `XyzCrudEducationController`, `XyzCrudEducationService`, `XyzQueryEducationRepository`, `XyzQueryEducationMapper.xml`이 실제 DB 경로로 연결됩니다.
- paging: 완료. 기존 offset/keyset paging 샘플 유지 및 전체 테스트 통과.
- keyset: 완료. `findKeysetPageItems`와 service test 유지.
- search/sort: 완료. `criteria.sortCode` whitelist와 MyBatis `<choose>` 기반 정렬 유지. `${...}` 정렬 치환은 사용하지 않습니다.
- validation: 완료. `XyzCrudEducationRequest`, `XyzCrudEducationStatusRequest`, Controller parameter에 Bean Validation을 적용했습니다.
- mapper/repository: 완료. insert/update/status/logical delete Mapper를 추가했습니다.
- SQL/fixture: 완료. `xyz_edu_query_fixture.sql`에 CRUD 테스트 범위 `91000-91999` 정리를 추가했습니다.
- test: 완료. `:xyz:test`, DB slice, 전체 `test` 통과.
- facade/outbound/header propagation: 완료. `CpfWebClient` 샘플을 evidence gate에 등록했습니다.

## 4. ADM 검증 보강

- browser click 환경 확인: 미검증. `node --version`, `npx --version` 모두 PATH에서 찾지 못했습니다.
- browser click 실행: 미검증. Node/npx 부재로 `scripts/smoke-adm-ui.ps1 -BrowserClick`는 실행하지 않았습니다.
- permission runtime: 완료. `scripts/smoke-adm-permission-runtime.ps1 -BuildBeforeRun` 통과.
- bootJar 최신성 보장: 완료. `build/runtime-smoke/adm-runtime-smoke-result.json`에 `buildBeforeRun.requested=true`, `exitCode=0`, `launchMode=BOOT_JAR`가 기록되었습니다.
- V15 API permission migration helper: 미검증. `ADM_DB_MIGRATION_PASSWORD`가 제공되지 않아 `apply-v15-adm-api-permission-management.ps1`는 skip되었습니다.

## 5. CMN 고정길이 전문 skeleton

- LayoutSpec: 완료. `FixedLengthLayoutSpec`에 charset, totalLength, fields와 overlap/boundary 검증을 추가했습니다.
- FieldSpec: 완료. `FixedLengthFieldSpec`에 name, start, length, type, required, padding, alignment, sensitive를 정의했습니다.
- Parser: 부분 구현. byte 배열 기준 parse, 필수값 오류 수집, masking 결과를 제공합니다.
- Formatter: 부분 구현. byte 길이 기준 padding/alignment와 1 byte padding 검증을 제공합니다.
- Masking: 완료. `FixedLengthMaskingRule`이 sensitive field 마스킹을 담당합니다.
- Test: 완료. UTF-8 한글 2글자 6 byte 필드와 숫자 padding round-trip 테스트를 통과했습니다.
- 범위 주의: 이번 작업은 skeleton 착수입니다. 반복부, 전문 사전 DB, 타입별 엄격 변환, 오류코드 매핑은 아직 전체 구현으로 표시하지 않습니다.

## 6. 실행 검증

| check id | 상태 | 결과 |
|---|---|---|
| edu-mapper-db-slice | 완료 | `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, `CPF_XYZ_EDU_MAPPER_DB_USER` 호환 로직 포함. 로컬 MariaDB `cpf_test` 기준 `XyzQueryEducationMapperSliceTest` 통과. |
| mariadb-full-install | 미검증 | `scripts/smoke-mariadb-full-install.ps1` 전체 재설치 검증은 이번 작업에서 실행하지 않았습니다. |
| adm-runtime | 완료 | `scripts/smoke-adm-runtime.ps1 -BuildBeforeRun` 통과. |
| adm-permission-runtime | 완료 | `scripts/smoke-adm-permission-runtime.ps1 -BuildBeforeRun` 통과. viewer write 403, viewer Center-Cut read 200 확인. |
| openapi-runtime | 완료 | ADM runtime smoke 안에서 `scripts/smoke-openapi.ps1`가 통과했습니다. |
| adm-browser-click | 미검증 | Node/npx가 PATH에서 확인되지 않아 브라우저 클릭 검증은 실행하지 않았습니다. |
| standard-header-e2e | 미검증 | `scripts/smoke-standard-header-e2e.ps1`는 이번 작업에서 실행하지 않았습니다. |
| redis-kafka-mq-broker | 미검증 | 외부 broker 실연동은 이번 작업 범위가 아닙니다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` 통과. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` 통과. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` 통과. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` 통과. |

추가 검증:

- `.\gradlew.bat :xyz:test --offline --no-daemon --console=plain`: 통과.
- `.\gradlew.bat :cmn:test --offline --no-daemon --console=plain`: 통과.
- `.\gradlew.bat :adm:test --offline --no-daemon --console=plain`: 통과.
- `.\gradlew.bat test --offline --no-daemon --console=plain`: 통과.
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1`: 통과.
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1`: 통과.
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1`: 통과.
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake`: 통과.
- `.\gradlew.bat qualityGate --offline --no-daemon --console=plain`: 통과.

## 7. 보류와 리스크

- `scripts/smoke-mariadb-full-install.ps1` 전체 설치 검증은 실행하지 않았습니다. 전체 DB 재설치가 필요한 작업이므로 별도 승인/백업 기준으로 진행하는 편이 안전합니다.
- ADM browser click 검증은 Node/npx 미설치로 미검증입니다.
- `apply-v15-adm-api-permission-management.ps1`는 마이그레이션 계정 비밀번호 환경변수 미제공으로 skip되었습니다. ADM runtime smoke 자체는 통과했습니다.
- CMN fixed-length는 skeleton입니다. 전체 전문 엔진 완료로 보고하지 않습니다.
- `CPF_STABILIZATION_REPORT.html`이 남아 있으면 `check-html-docs`가 실패합니다. 이번 기준 리포트는 Markdown인 `CPF_STABILIZATION_REPORT.md`입니다.

## 8. 다음 보강 후보

1. CMN fixed-length 전문 엔진 완성: 반복부, 타입 변환, 전문 사전, 오류코드, ADM 포맷 뷰어 연동.
2. ADM 브라우저 클릭 자동화: Node/Playwright 설치 후 메뉴/버튼/비활성 사유 UX 클릭 검증.
3. MariaDB 전체 설치 smoke: `00_all_install_and_smoke.sql` 재실행, FK/index/seed idempotent/app-migration 권한 검증.
4. 표준 헤더 E2E: `scripts/smoke-standard-header-e2e.ps1` 재실행과 ADM 로그 헤더/데이터 분리 화면 확인.
5. Redis/Kafka/MQ broker 실연동: DB fallback 장애 시나리오와 broker 우선 전파 검증.
