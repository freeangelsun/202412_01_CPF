# CPF 안정화 작업 리포트

## 1. 기준 정보

- 사용자 요청 원본 파일: `CPF_NEW_REQUEST.md`
- 최상위 목표 기준서: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 작업 시작 branch: `master`
- 작업 시작 HEAD SHA: `302d5e73b88b78bad62895fbf98573b5bb4f0a0d`
- 작업 시작 origin/master SHA: `302d5e73b88b78bad62895fbf98573b5bb4f0a0d`
- 작업 종료 HEAD SHA: `302d5e73b88b78bad62895fbf98573b5bb4f0a0d`
- 작업 종료 branch 확인: `git -c safe.directory=D:/WORK_CPF/202412_01_CPF branch --show-current`로 `master` 확인
- 작업 종료 origin/master SHA 확인: `git -c safe.directory=D:/WORK_CPF/202412_01_CPF rev-parse origin/master`로 동일 SHA 확인
- 작업 종료 최근 커밋: `302d5e7 20260607_03`
- 참고: 일부 Git 기준 명령은 sandbox 사용자 소유권 차이로 `safe.directory` 보조 옵션을 사용했다.
- 작업 종료 status --short 요약: modified 19개, untracked 28개 항목
- `CPF_NEW_REQUEST.md`: 사용자 요청 원본으로 확인만 했고 작업 대상으로 편집하지 않았다.
- Git commit / push / branch 생성: 수행하지 않음
- 별도 변경파일 목록 산출물: 생성하지 않음
- 민감정보 원문: 리포트에 기록하지 않음

## 2. 작업 요약

이번 작업은 업무 DB 기반 center-cut adapter를 XYZ EDU 샘플로 추가하고, PFW 표준 계약과 BAT center-cut 모수에 연결하는 범위로 수행했다.

- PFW는 `CenterCutTargetProvider`, `CenterCutHandler`, `CpfCenterCutService` 계약만 제공한다.
- BAT는 `bat_center_cut_job`, `bat_center_cut_parameter` 모수와 기본 실행 메타를 유지한다.
- XYZ는 업무 DB `xyzDB`의 `xyz_center_cut_sample_target`, `xyz_center_cut_sample_result`를 소유한다.
- XYZ 업무 adapter는 대상 조회, RUNNING 전이, 성공/실패 result 기록, parent/child transactionGlobalId 보존을 수행한다.
- `smoke-standard-header-e2e.ps1`의 DB password process argument 노출 가능성을 제거했다.

## 3. Center-Cut Adapter

- 업무 모듈: `xyz`
- target table: `xyzDB.xyz_center_cut_sample_target`
- result table: `xyzDB.xyz_center_cut_sample_result`
- provider: `XyzCenterCutTargetRepository`
- handler: `XyzCenterCutHandler`
- service: `XyzCenterCutEducationService`
- API: `POST /xyz/edu/center-cut/run`
- DB test: `XyzCenterCutAdapterTest`
- smoke: `scripts/smoke-center-cut-adapter.ps1`
- fixture: `xyz_center_cut_fixture.sql`
- 처리 결과: 성공 3건, 실패 1건 검증
- parent/child transactionGlobalId: result snapshot에서 부모/자식 ID 모두 존재 확인

## 4. SQL 반영

- split SQL:
  - `specs/sql/01_create_databases.sql`: `xyzDB` 추가
  - `specs/sql/02_create_service_users.sql`: `cpf_xyz_migration`, `cpf_xyz_app` 추가
  - `specs/sql/40_business_modules_schema.sql`: `xyz_center_cut_sample_target`, `xyz_center_cut_sample_result` 추가
  - `specs/sql/50_framework_seed_data.sql`: `CPF_XYZ_CENTER_CUT_SAMPLE_JOB` PFW/BAT 모수 추가
  - `specs/sql/70_test_data.sql`: XYZ center-cut target seed 4건 추가
  - `specs/sql/99_smoke_check.sql`: XYZ center-cut table/seed 조회 추가
- all install:
  - `specs/sql/00_all_install.sql` 재생성
  - `specs/sql/00_all_install_and_smoke.sql` 재생성
- Flyway:
  - `specs/sql/migration/flyway/V18__xyz_center_cut_sample.sql` 추가
  - `specs/sql/migration/flyway/V1__cpf_baseline_install.sql` 재생성
- install generator:
  - `scripts/build-all-install-sql.ps1`에 `xyzDB` cleanup 반영

## 5. 표준 헤더 E2E 보안 보정

- 대상: `scripts/smoke-standard-header-e2e.ps1`
- 제거: MariaDB query 실행 인자 `--password=$DbPassword`
- 유지: `MYSQL_PWD`, `MARIADB_PWD` 환경변수 주입
- self-check: DB password가 process argument에 포함되면 실패 처리
- 민감정보 확인: result/log/report에 원문 DB password를 기록하지 않도록 유지
- 기존 표준 헤더 smoke 증거: `scripts/smoke-standard-header-e2e.ps1`, `build/runtime-smoke/standard-header-e2e-result.json`

## 6. 실행 검증

| check id | 상태 | 결과 |
|---|---|---|
| edu-mapper-db-slice | 미검증 | 이번 작업 범위가 center-cut adapter였기 때문에 `XyzQueryEducationMapperSliceTest`는 실행하지 않았다. 관련 fixture는 `xyz_edu_query_fixture.sql`이고 환경변수 기준은 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, `CPF_XYZ_EDU_MAPPER_DB_USER`다. |
| mariadb-full-install | 완료 | `scripts/smoke-mariadb-full-install.ps1 -RequireRun` 통과, `mariadb-full-install-result.json` status 완료 |
| adm-runtime | 미검증 | 이번 작업 범위가 아니므로 ADM runtime smoke는 실행하지 않았다. |
| adm-permission-runtime | 미검증 | 이번 작업 범위가 아니므로 ADM 권한 runtime smoke는 실행하지 않았다. |
| openapi-runtime | 미검증 | 이번 작업 범위가 아니므로 OpenAPI runtime smoke는 실행하지 않았다. |
| adm-browser-click | 미검증 | 이번 작업 범위가 아니므로 브라우저 클릭 검증은 실행하지 않았다. |
| standard-header-e2e | 미검증 | `scripts/smoke-standard-header-e2e.ps1` DB password process argument 제거와 self-check는 반영했다. 다만 이번 작업의 `-RequireRuntime` 재실행은 120초 제한에서 완료되지 않아 성공으로 기록하지 않는다. |
| redis-kafka-mq-broker | 미검증 | 외부 broker 실연동은 이번 작업 범위가 아니므로 실행하지 않았다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` 최종 통과 |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` 최종 통과 |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` 통과 |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` 통과 |

추가 실행 검증:

| 항목 | 상태 | 결과 |
|---|---|---|
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| `.\gradlew.bat :bat:test --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| `.\gradlew.bat :xyz:test --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| `.\gradlew.bat test --offline --no-daemon --console=plain` | 완료 | BUILD SUCCESSFUL |
| XYZ center-cut DB slice test | 완료 | `XyzCenterCutAdapterTest` 실제 MariaDB 실행 통과 |
| `scripts/smoke-center-cut-adapter.ps1 -RequireRun` | 완료 | `center-cut-adapter-result.json` status 완료 |
| `scripts/check-sql-standard.ps1` | 완료 | 통과 |

## 7. MariaDB Full Install Smoke 결과

- 스크립트: `scripts/smoke-mariadb-full-install.ps1`
- 결과 파일: `build/sql-smoke/mariadb-full-install-result.json`
- 상태: 완료
- 실행 SQL:
  - `specs/sql/00_all_install_and_smoke.sql`
  - `specs/sql/99_smoke_check.sql`
  - `specs/sql/50_framework_seed_data.sql`
- 주요 체크:
  - `batCenterCutTableCount = 4`
  - `legacyPfwCenterCutTableCount = 0`
  - `centerCutSeedCount = 1`
  - `centerCutParameterSeedCount = 2`
  - `xyzCenterCutTableCount = 2`
  - `xyzCenterCutSeedCount = 4`

## 8. 상태값 반영

- center-cut adapter: 완료
- EDU/XYZ center-cut sample: 완료
- SQL/Flyway/all_install/smoke 동기화: 완료
- MariaDB full install: 완료
- standard-header-e2e password process argument 보정: 완료
- standard-header-e2e runtime 재검증: 미검증
- ADM center-cut 관제: 미검증
- ADM browser click: 미검증
- Redis/Kafka/MQ broker: 미검증
- EDU mapper DB slice: 미검증
  - 관련 증거: `xyz_edu_query_fixture.sql`
  - 환경변수 기준: `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, `CPF_XYZ_EDU_MAPPER_DB_USER`

## 9. 남은 리스크와 보류 항목

- ADM center-cut 관제 화면/API는 이번 범위가 아니므로 미검증이다.
- ADM runtime/OpenAPI/browser click은 이번 범위가 아니므로 미검증이다.
- standard-header runtime smoke는 보안 보정 후 재실행을 시도했지만 120초 제한에서 완료되지 않아 검증 성공으로 기록하지 않았다.
- Redis/Kafka/MQ 실 broker 연동은 이번 범위가 아니므로 미검증이다.
- `smoke-center-cut-adapter.ps1`는 sandbox에서 Gradle wrapper cache 접근이 막혀 권한 상승 실행으로 검증했다.
- `smoke-mariadb-full-install.ps1`는 기존 MariaDB CLI 호환성 때문에 내부적으로 root password 환경변수와 CLI 인증 처리를 병행한다. 리포트와 result JSON에는 원문을 기록하지 않았다.

## 10. 다음 보강 후보

1. ADM center-cut 관제 API/UI 추가: job 모수, target, result, parent/child transactionGlobalId, 성공/실패 건수 조회.
2. ADM runtime/OpenAPI/browser click smoke로 center-cut 관제 화면까지 검증.
3. EDU/XYZ 실전 샘플 CRUD/paging/validation/facade/outbound 정본화.
4. CMN 고정길이 전문 처리와 EXS 전문 송수신 마일스톤 진행.
5. Redis/Kafka/MQ 실 broker 전파와 DB fallback 장애 시나리오 검증.
