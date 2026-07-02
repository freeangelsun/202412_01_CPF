# CPF 안정화 작업 리포트

## 1. 기준 정보

| 항목 | 값 |
| --- | --- |
| 요청 원본 | `CPF_NEW_REQUEST.md` |
| 최상위 목표 | `CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 작업 branch | `master` |
| 작업 시작 HEAD | `2d6eaf0329cd85997b13c595e1f7774004cd88d6` |
| 작업 시작 origin/master | `2d6eaf0329cd85997b13c595e1f7774004cd88d6` |
| remote | `https://github.com/freeangelsun/202412_01_CPF.git` |
| 최근 커밋 | `.git/logs/HEAD` 기준 `20260702_01` |
| 작업 시작 status | `CPF_NEW_REQUEST.md`, `CPF_STABILIZATION_REPORT.md` 수정 상태 |
| 주의 | `git log -1 --oneline`은 sandbox 계정과 저장소 소유자 차이로 safe.directory 오류가 발생했습니다. SHA는 `.git` 파일에서 확인했습니다. |
| 요청 파일 처리 | `CPF_NEW_REQUEST.md`는 확인용으로만 사용했고 작업 대상에서 제외했습니다. |

## 2. 수행 작업

- `scripts/smoke-mariadb-full-install.ps1`을 추가했습니다.
  - MariaDB CLI를 PATH, `CPF_MARIADB_CLI`, 일반 설치 경로에서 탐색합니다.
  - `CPF_DB_ROOT_PASSWORD` 또는 `-RootPassword`가 없으면 DB 접속을 시도하지 않고 `미검증`으로 기록합니다.
  - 실행 가능 시 `00_all_install_and_smoke.sql`, `99_smoke_check.sql`, `50_framework_seed_data.sql` 재실행과 `bat_center_cut_*`/legacy table/seed 검사를 수행하도록 구성했습니다.
  - 비밀번호는 `MYSQL_PWD` 환경변수로만 전달하고 결과 JSON/로그에는 원문을 남기지 않도록 했습니다.
- `scripts/smoke-standard-header-e2e.ps1`을 추가했습니다.
  - 표준 헤더, `X-Cpf-Ext-*` 허용/차단 확장 헤더, 민감 헤더 마스킹 시나리오를 JSON 결과로 남깁니다.
  - 앱 미기동 상태에서는 probe를 `미검증`으로 남기고, 전체 상태는 `부분 구현`으로 기록합니다.
  - 민감 헤더 값은 결과 파일에 `****`로 마스킹합니다.
- `README.md` 기본 검증 명령에 신규 smoke 두 개를 추가했습니다.
- `specs/기능_구현_매트릭스.html`의 `mariadb-full-install`, `standard-header-e2e` 상태와 증거를 이번 실행 결과 기준으로 갱신했습니다.
- `scripts/check-feature-evidence.ps1`에 신규 smoke 스크립트, README, 기능 매트릭스, 리포트 연결 증거를 추가했습니다.
- `scripts/build-all-install-sql.ps1`로 `00_all_install.sql`, `00_all_install_and_smoke.sql` 합본을 재생성했습니다.

## 3. 검증 결과

| 검증 | 결과 | 비고 |
| --- | --- | --- |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-mariadb-full-install.ps1` | 통과, 결과 `미검증` | MariaDB CLI는 `C:\Program Files\MariaDB 12.3\bin\mariadb.exe`로 감지했지만 `CPF_DB_ROOT_PASSWORD`가 없어 실제 SQL 실행은 하지 않았습니다. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-standard-header-e2e.ps1` | 통과, 결과 `부분 구현` | 대상 앱이 기동되어 있지 않아 HTTP probe는 `미검증`, mock downstream은 `미구현`, ADM lookup은 `미검증`입니다. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all-install-sql.ps1` | 통과 | all install SQL 합본 재생성 완료. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1` | 통과 | SQL 표준 검사 통과. |
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain` | 통과 | PFW 테스트 통과. |
| `.\gradlew.bat :bat:test --offline --no-daemon --console=plain` | 통과 | BAT 테스트 통과. |
| `.\gradlew.bat :adm:test --offline --no-daemon --console=plain` | 통과 | ADM 테스트 통과. |
| `.\gradlew.bat test --offline --no-daemon --console=plain` | 통과 | 전체 Gradle test 통과. |
| `.\gradlew.bat :bat:bootJar --offline --no-daemon --console=plain` | 통과 | BAT bootJar 생성 통과. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake` | 통과 | UTF-8/mojibake 검사 통과. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1` | 통과 | 신규 smoke 증거 포함 후 통과. |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1` | 통과 | 기능 매트릭스와 리포트 check 상태 일치 확인. |
| `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 통과 | 품질 게이트 통과. |

## 4. Check Status

| check id | status | evidence |
| --- | --- | --- |
| edu-mapper-db-slice | 미검증 | 이번 작업 범위에서 `XyzQueryEducationMapperSliceTest` MariaDB slice 검증은 실행하지 않았습니다. 기준 fixture는 `xyz_edu_query_fixture.sql`이며 환경변수는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`을 사용하고 legacy 호환명 `CPF_XYZ_EDU_MAPPER_DB_USER`를 함께 문서화합니다. |
| mariadb-full-install | 미검증 | `scripts/smoke-mariadb-full-install.ps1`, `build/sql-smoke/mariadb-full-install-result.json`. CLI는 감지했지만 비밀번호 환경변수가 없어 실제 DB 접속과 SQL 실행은 미수행입니다. |
| adm-runtime | 미검증 | ADM 앱 기동 runtime smoke는 실행하지 않았습니다. |
| adm-permission-runtime | 미검증 | ADM permission runtime smoke는 실행하지 않았습니다. |
| openapi-runtime | 미검증 | 앱 기동 후 `/v3/api-docs` 검증은 실행하지 않았습니다. |
| adm-browser-click | 미검증 | 브라우저 클릭 검증은 실행하지 않았습니다. |
| standard-header-e2e | 부분 구현 | `scripts/smoke-standard-header-e2e.ps1`, `build/runtime-smoke/standard-header-e2e-result.json`. 진입점과 결과 포맷은 준비했지만 앱 수신/로그/ADM 조회/outbound mock downstream까지는 미검증입니다. |
| redis-kafka-mq-broker | 미검증 | 실제 Redis/Kafka/MQ broker 연동 검증은 실행하지 않았습니다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` 통과. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` 통과. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` 통과. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` 통과. |

## 5. 남은 리스크

- MariaDB full install은 아직 실제 SQL 실행 검증이 아닙니다. `CPF_DB_ROOT_PASSWORD`를 안전한 환경변수로 주입한 뒤 `scripts/smoke-mariadb-full-install.ps1`을 다시 실행해야 합니다.
- Docker는 설치되어 있지만 docker engine 접근 권한이 없어 compose 기반 신규 DB 검증은 수행하지 못했습니다.
- 표준 헤더 Runtime E2E는 smoke 진입점만 준비되었습니다. 실제 앱 기동, downstream mock 연동, ADM 로그 조회까지 이어지는 완전 E2E는 다음 작업에서 수행해야 합니다.
- `git log`는 safe.directory 문제로 실행하지 못했고, 이번 리포트는 `.git` 파일 직접 확인 기준입니다.
- `CPF_NEW_REQUEST.md`는 사용자 갱신 파일로 남아 있습니다. 이번 작업에서는 의도적으로 수정하지 않았습니다.

## 6. 다음 보강 후보

1. `CPF_DB_ROOT_PASSWORD`를 환경변수로 주입해 `scripts/smoke-mariadb-full-install.ps1 -RequireRun`을 실행하고 FK/index/seed idempotent 결과를 확정합니다.
2. ACC 또는 전용 smoke 앱을 기동하고 `scripts/smoke-standard-header-e2e.ps1 -RequireRuntime`으로 실제 표준 헤더 수신/차단 응답을 검증합니다.
3. 표준 헤더 E2E에 mock downstream 서버와 ADM 로그 조회 API 호출을 연결해 `standard-header-e2e`를 `완료`로 승격합니다.
4. Docker engine 권한을 정리한 뒤 `docker-compose.local.yml --profile db` 기반 신규 MariaDB 검증 경로를 추가합니다.
5. ADM runtime, OpenAPI runtime, ADM browser click, Redis/Kafka/MQ broker 검증을 실제 기동 환경에서 분리 실행합니다.
