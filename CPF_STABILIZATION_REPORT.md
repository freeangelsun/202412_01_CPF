# CPF M4 안정화 리포트

## 1. 기준 정보

- 작업 요청 원본: `CPF_NEW_REQUEST.md`
- 요청 원본 취급: 기능 검토 대상에서 제외, 읽기 기준으로만 사용
- 작업 시작 branch: `master`
- 작업 시작 HEAD SHA: `99dfc1224c5cb76baea9d5fdac2f6ddb9473917c`
- 작업 시작 origin/master SHA: `99dfc1224c5cb76baea9d5fdac2f6ddb9473917c`
- 작업 시작 status: `CPF_NEW_REQUEST.md`, `CPF_STABILIZATION_REPORT.md` 수정 상태
- git 정보 수집 특이사항: sandbox 사용자와 저장소 소유자가 달라 일부 git 명령은 `safe.directory` 일회성 옵션으로 조회
- 커밋/푸시/브랜치 생성: 수행하지 않음
- 별도 변경 파일 목록 산출물: 생성하지 않음

## 2. 구현 요약

- BAT standalone 경로를 확인하고 `:bat:bootJar`로 jar 생성까지 검증했습니다.
- BAT center-cut 1차 구현을 추가했습니다.
  - PFW 표준 계약: `CpfCenterCutService`, `CenterCutTargetProvider`, `CenterCutHandler`, `CpfCenterCutStatus`, `CpfCenterCutTarget`, `CpfCenterCutResult`
  - BAT sample 구현: `BatCenterCutSampleTargetProvider`, `BatCenterCutSampleHandler`, `BatCenterCutSmokeTasklet`
  - BAT Job: `CPF_BAT_CENTER_CUT_JOB`, `CPF_BAT_CENTER_CUT_STEP`
- BAT runtime smoke에 center-cut Job 실행과 `centerCutRequested=`, `centerCutSuccess=` step log 검증을 추가했습니다.
- PFW SQL에 `pfw_center_cut_job`, `pfw_center_cut_parameter`, `pfw_center_cut_item`, `pfw_center_cut_result`를 추가했습니다.
- Flyway `V16__batch_center_cut_standard.sql`을 추가하고, split SQL, `00_all_install.sql`, `00_all_install_and_smoke.sql`, `V1__cpf_baseline_install.sql`, `99_smoke_check.sql`을 동기화했습니다.
- V15 ADM API 권한 적용 스크립트의 DB password 기본값 fallback을 제거했습니다. 비밀번호가 없으면 DB 접속 없이 `SKIPPED` 결과 JSON만 남깁니다.
- README, 문서 인덱스, SQL 가이드, 배치 개발 가이드, 기능 구현 매트릭스를 현재 구현 기준으로 정리했습니다.

## 3. M3-2 보안 보강

- `scripts/apply-v15-adm-api-permission-management.ps1`에서 `cpf_local_pw` fallback을 제거했습니다.
- 비밀번호 주입 방식은 `ADM_DB_MIGRATION_PASSWORD` 또는 `-Password` 명시 파라미터만 허용합니다.
- 비밀번호가 없을 때 실행 결과: `SKIPPED`
- 결과 파일: `build/runtime-smoke/v15-adm-api-permission-result.json`
- 민감정보 원문 기록 여부: 비밀번호 원문 미기록, JDBC URL password 파라미터 마스킹

## 4. BAT 구현 상태

- standalone: 완료. `BatApplication` bootJar 생성 검증 완료
- worker: 부분 구현. `pfw_batch_worker` heartbeat와 workerId 연결은 runtime smoke로 확인
- heartbeat: 완료. `CPF_BAT_HEARTBEAT_JOB`에서 처리 건수, 진행률, step log 갱신 확인
- ghost: 부분 구현. 후보 감지 서비스와 ADM 조회 API는 존재하지만 자동 scheduler 장애 시나리오는 미검증
- job/step: 완료. Spring Batch execution ID와 CPF execution ID 연결 확인
- lock: 부분 구현. 공통 lock manager는 존재하나 stale lock 장애 시나리오는 미검증
- operator action: 부분 구현. ADM API와 operation log 구조는 존재하나 전체 운영 행위 E2E는 미검증
- center-cut: 부분 구현. PFW 계약, SQL 표준 메타, BAT sample worker, runtime smoke는 완료. 업무 DB adapter와 ADM 상세 관제는 다음 단계

## 5. 주요 파일

- BAT Application: `bat/src/main/java/cpf/bat/BatApplication.java`
- BAT Job 설정: `bat/src/main/java/cpf/bat/job/BatSmokeJobConfig.java`
- BAT center-cut: `bat/src/main/java/cpf/bat/centercut`
- BAT runtime smoke: `scripts/smoke-bat-runtime.ps1`
- PFW center-cut 계약: `pfw/src/main/java/cpf/pfw/common/batch/centercut`
- PFW center-cut 테스트: `pfw/src/test/java/cpf/pfw/common/batch/centercut/CpfCenterCutServiceTest.java`
- SQL split: `specs/sql/10_pfw_schema.sql`, `specs/sql/50_framework_seed_data.sql`, `specs/sql/99_smoke_check.sql`
- Flyway: `specs/sql/migration/flyway/V16__batch_center_cut_standard.sql`
- 합본 SQL: `specs/sql/00_all_install.sql`, `specs/sql/00_all_install_and_smoke.sql`
- 문서: `README.md`, `specs/index.html`, `specs/SQL_가이드.html`, `specs/배치_개발_가이드.html`, `specs/기능_구현_매트릭스.html`
- EDU fixture 기준: `xyz_edu_query_fixture.sql`
- EDU Mapper DB 환경 변수 기준: `CPF_XYZ_EDU_MAPPER_DB_USERNAME` 사용을 우선하고, 과거 호환 확인용으로 `CPF_XYZ_EDU_MAPPER_DB_USER` 문자열도 증거에 남깁니다. 비밀번호 값은 기록하지 않습니다.

## 6. 실행 명령 결과

| 명령 | 결과 | 비고 |
| --- | --- | --- |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain` | 완료 | center-cut service 테스트 포함 |
| `.\gradlew.bat :bat:test --offline --no-daemon --console=plain` | 완료 | 최초 1회 API 사용 오류 수정 후 재실행 통과 |
| `.\gradlew.bat :bat:bootJar --offline --no-daemon --console=plain` | 완료 | BAT bootJar 생성 |
| `.\gradlew.bat :adm:test --offline --no-daemon --console=plain` | 완료 | ADM 테스트 통과 |
| `.\gradlew.bat test --offline --no-daemon --console=plain` | 완료 | 전체 테스트 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-bat-runtime.ps1` | 완료 | health, 정상 Job, heartbeat Job, center-cut Job, 실패 Job, cleanup 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1` | 완료 | health, OpenAPI, 운영 API, batch API, transaction meta, 정적 UI 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\apply-v15-adm-api-permission-management.ps1` | 완료 | password 미제공으로 SKIPPED 결과 기록 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake` | 완료 | 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1` | 완료 | 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1` | 실패 | 단독 실행 시 ADM 서버 미기동으로 연결 실패. ADM runtime smoke 내부 OpenAPI 확인은 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1` | 완료 | 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1` | 완료 | 최초 1회 리포트 증거 누락 수정 후 재실행 통과 |
| `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 완료 | `specs/index.html`의 거래 ID 설명 보강 후 재실행 통과 |

## 7. 필수 검증 상태

| check id | 상태 | 근거 |
| --- | --- | --- |
| edu-mapper-db-slice | 미검증 | 이번 M4 작업에서는 실행하지 않음 |
| mariadb-full-install | 미검증 | `00_all_install_and_smoke.sql` 실제 전체 설치는 실행하지 않음 |
| adm-runtime | 완료 | `scripts/smoke-adm-runtime.ps1` 통과 |
| adm-permission-runtime | 미검증 | permission write smoke는 실행하지 않음. V15 apply는 password 미제공 SKIPPED 확인 |
| openapi-runtime | 완료 | ADM runtime smoke 내부 `/v3/api-docs` 확인 통과 |
| adm-browser-click | 미검증 | 브라우저 자동화 runtime 부재로 ADM runtime smoke에서 SKIPPED |
| standard-header-e2e | 완료 | `:pfw:test` 통과 |
| redis-kafka-mq-broker | 미검증 | 외부 broker 연동 테스트 미실행 |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` 통과 |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` 통과 |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` 통과 |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` 통과 |

## 8. 남은 리스크

- `smoke-openapi.ps1` 단독 실행은 서버를 직접 띄우지 않으므로 ADM 서버 미기동 상태에서는 실패합니다. 이번에는 `smoke-adm-runtime.ps1` 내부 OpenAPI 검증으로 통과를 확인했습니다.
- ADM permission write runtime은 비밀번호 fallback 제거 후 별도 migration password와 ADM 비밀번호를 명시해야 완전 검증할 수 있습니다.
- MariaDB 전체 설치 SQL은 이번 작업에서 실행하지 않았습니다. split SQL과 합본 동기화는 수행했지만 실제 빈 DB 설치/seed idempotent는 별도 검증이 필요합니다.
- center-cut은 1차 표준 계약과 BAT sample worker까지 구현했습니다. 업무 DB 기반 adapter, ADM 상세 관제 화면, 실패 재처리 운영 UX는 다음 단계입니다.
- ghost 자동 감지 scheduler와 강제 조치 장애 시나리오는 아직 runtime E2E로 확인하지 않았습니다.

## 9. 다음 보강 후보

1. 업무 DB 기반 center-cut target/item/result repository adapter와 XYZ EDU center-cut 샘플 추가
2. ADM batch 상세에 center-cut 대상, 결과, 실패 사유, 자식 거래 ID 탭 추가
3. ghost 자동 감지 scheduler와 장애 시나리오 smoke 추가
4. ADM permission write runtime을 password 명시 환경에서 재실행해 V15 idempotent와 서버 권한 차단 재검증
5. 빈 MariaDB에서 `specs/sql/00_all_install_and_smoke.sql` 전체 설치와 seed idempotent 검증
