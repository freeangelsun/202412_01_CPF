# CPF Stabilization Report

## 기준 정보

- 작업 기준일: 2026-07-02
- 요청 원본: `CPF_NEW_REQUEST.md`
- 상위 목표 기준서: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 리포트 형식: Markdown
- 작업 제한: commit, push, branch 생성, 별도 변경파일 목록 산출물 생성은 수행하지 않음
- 요청 원본 처리: `CPF_NEW_REQUEST.md`는 작업 대상에서 제외함

## 시작 기준

- branch: `master`
- HEAD SHA: `c23824b318bb29ce47910ea66a528f111f59fc4d`
- origin/master SHA: `c23824b318bb29ce47910ea66a528f111f59fc4d`
- remote: `https://github.com/freeangelsun/202412_01_CPF.git`
- 최근 commit: `c23824b 20260701_06`
- 현재 sandbox에서 일부 git 명령은 safe.directory 소유권 차이로 실패했으며, HEAD/remote 값은 `.git` 파일로 보조 확인함
- 작업 시작 상태에서 `CPF_NEW_REQUEST.md`, `CPF_STABILIZATION_REPORT.md`는 이미 수정 상태였고 `CPF_FINAL_TARGET_REQUIREMENTS.md`는 신규 파일 상태였음

## 수행 작업

1. `CPF_FINAL_TARGET_REQUIREMENTS.md`를 CPF 최상위 목표 기준서로 삼도록 README와 리포트 기준을 정리함.
2. 중간 개발/검수 산출물은 Markdown 원본을 우선한다는 기준을 README와 리포트에 반영함.
3. PFW 표준 헤더에 `X-Cpf-Ext-*` 확장 헤더 정책을 source-level로 추가함.
4. `X-Cpf-Ext-1` ~ `X-Cpf-Ext-5` 예약형과 `X-Cpf-Ext-{Key}` 동적 확장 헤더를 처리하도록 보강함.
5. `X-Cpf-Ext-Authorization`, `X-Cpf-Ext-Token`, `X-Cpf-Ext-Api-Key`, secret 계열 이름은 인증값 우회 저장/전파로 보고 차단함.
6. 헤더 Extractor, Validator, Context, Snapshot, Mutator, Propagator, RestClient 전파 테스트를 보강함.
7. center-cut 저장소 소유권을 PFW 고정에서 BAT 기본 실행 메타로 보정함.
8. `specs/sql/35_bat_schema.sql`와 `V17__batch_center_cut_ownership_repair.sql`을 추가하고 `bat_center_cut_*` 기준으로 seed, smoke, all-install, Flyway baseline을 동기화함.
9. V16 migration은 이력 보존 대상으로 두고 V17에서 이관/정리하도록 결정함.
10. 기능 매트릭스에서 standard-header Source-Level과 Runtime E2E 상태를 분리함.

## 검증 상태

| check id | 상태 | 근거 | 비고 |
| --- | --- | --- | --- |
| edu-mapper-db-slice | 미검증 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql` | 이번 작업에서 별도 DB slice 검증은 실행하지 않음. 환경 변수 기준은 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`이며 레거시 호환명은 `CPF_XYZ_EDU_MAPPER_DB_USER`임 |
| mariadb-full-install | 미검증 | `specs/sql/00_all_install_and_smoke.sql` | 로컬 PATH에서 `mysql`과 `mariadb` CLI를 찾지 못해 실제 MariaDB 설치 SQL 실행은 미검증 |
| adm-runtime | 미검증 | `scripts/smoke-adm-runtime.ps1` | 이번 작업에서는 ADM 앱 기동 runtime smoke를 실행하지 않음 |
| adm-permission-runtime | 미검증 | `scripts/smoke-adm-permission-runtime.ps1` | 이번 작업에서는 권한 runtime smoke를 실행하지 않음 |
| openapi-runtime | 미검증 | `/v3/api-docs` | ADM 앱 기동 후 OpenAPI JSON 검증은 실행하지 않음 |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | 브라우저 클릭 검증은 실행하지 않음 |
| standard-header-e2e | 미검증 | `:pfw:test` | source-level 테스트는 통과했지만 수신, 로그, ADM 조회, outbound 전파까지의 Runtime E2E는 실행하지 않음 |
| redis-kafka-mq-broker | 미검증 | Redis/Kafka/MQ broker | 외부 broker 실연동 테스트는 실행하지 않음 |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline` | 통과 |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 통과 |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | 통과 |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | 통과 |

## 실행한 검증

- `.\gradlew.bat :pfw:test --offline --tests cpf.pfw.common.header.CpfExtensionHeaderPolicyTest --tests cpf.pfw.common.http.CpfRestClientInterceptorTest`: 통과
- `.\gradlew.bat :pfw:test --offline`: 통과
- `.\gradlew.bat test --offline`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/build-all-install-sql.ps1`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1`: 통과
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1`: 통과
- `.\gradlew.bat qualityGate --offline`: 통과
- `mysql --version`: 실패, CLI 없음
- `mariadb --version`: 실패, CLI 없음

## 설계 판단

- PFW는 center-cut 표준 인터페이스, 상태값, 공통 실행 계약을 소유함.
- BAT는 기본 center-cut job/parameter/item/result 저장소와 sample worker 구현을 소유함.
- 업무 모듈은 업무별 target/item/result 저장소나 adapter를 별도로 확장하는 구조로 둠.
- V16 migration은 이미 생성된 이력이므로 수정하지 않고, V17 migration으로 `pfw_center_cut_*` 데이터를 `bat_center_cut_*`로 이관한 뒤 PFW 테이블을 제거함.
- `X-Cpf-Ext-*`는 ADM 등록형 운영 설정이 아니라 개발 표준형 확장 헤더로 처리함.
- 확장 헤더는 기본적으로 마스킹 대상이 아니지만, 인증값/토큰/secret류 이름은 확장 헤더 우회 수단으로 차단함.

## 남은 리스크

- MariaDB 실설치 검증은 CLI가 PATH에 없어 미검증임.
- 표준 헤더 Runtime E2E는 source-level 테스트와 분리되어 아직 미검증임.
- ADM runtime, OpenAPI runtime, 브라우저 클릭 검증은 이번 작업에서 실행하지 않음.
- Redis/Kafka/MQ broker 실연동은 외부 환경이 필요해 미검증임.
- 업무 DB 기반 center-cut adapter와 EDU center-cut 샘플은 다음 마일스톤 대상임.

## 다음 보강 후보

1. MariaDB CLI 경로를 정리한 뒤 `00_all_install_and_smoke.sql` 실실행, FK/index/seed idempotent 검증.
2. 표준 헤더 Runtime E2E smoke 추가: 수신, 검증, 거래 context, 로그 저장, ADM 조회, outbound 전파까지 검증.
3. 업무 DB 기반 center-cut adapter와 XYZ/EDU center-cut 샘플 추가.
4. ADM center-cut 관제 화면/API와 배치 상세 관계 구조도 검증.
5. Redis/Kafka/MQ broker 실연동과 DB fallback 장애 시나리오 검증.
