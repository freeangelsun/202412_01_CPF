# CPF Stabilization Report

## 1. 기준 정보

- 요청 원본 파일: `CPF_NEW_REQUEST.md`
- 최종 목표 기준 파일: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 작업 시작 branch: `master`
- 작업 시작 HEAD SHA: `a3f05c17b76138c0cec2b74ee2f8edff466ce531`
- 작업 시작 origin/master SHA: `a3f05c17b76138c0cec2b74ee2f8edff466ce531`
- 작업 종료 HEAD SHA: `a3f05c17b76138c0cec2b74ee2f8edff466ce531`
- git 참고: 기본 git 명령은 safe.directory 소유권 경고가 발생해 `git -c safe.directory=D:/WORK_CPF/202412_01_CPF ...`로 읽기 전용 기준 정보를 확인했습니다.
- 금지 사항 준수: commit, push, branch 생성, 별도 변경파일 목록 산출물 생성 없음.

## 2. 수행 작업

### ADM Center-Cut API

- `AdmCenterCutController`를 추가했습니다.
- 추가 API:
  - `GET /adm/api/center-cut/jobs`
  - `GET /adm/api/center-cut/jobs/{centerCutJobId}`
  - `GET /adm/api/center-cut/jobs/{centerCutJobId}/parameters`
  - `GET /adm/api/center-cut/jobs/{centerCutJobId}/summary`
  - `GET /adm/api/center-cut/jobs/{centerCutJobId}/targets`
  - `GET /adm/api/center-cut/jobs/{centerCutJobId}/results`
  - `GET /adm/api/center-cut/results/{resultId}`
- OpenAPI tag `ADM-CenterCut`를 추가했고 `scripts/smoke-openapi.ps1` 검증 대상에 반영했습니다.
- `@CpfTransaction` 거래 메타 ID `ADM01CTC0010`부터 `ADM01CTC0070`까지 부여했습니다.

### ADM Center-Cut Service

- `AdmCenterCutOperationService`를 추가했습니다.
- 조회 대상:
  - `pfwDB.bat_center_cut_job`
  - `pfwDB.bat_center_cut_parameter`
  - `pfwDB.bat_center_cut_item`
  - `pfwDB.bat_center_cut_result`
  - `xyzDB.xyz_center_cut_sample_target`
  - `xyzDB.xyz_center_cut_sample_result`
- `CPF_XYZ_CENTER_CUT_SAMPLE_JOB`은 업무 DB adapter 기준으로 조회합니다.
- target/result 응답에 `parentTransactionGlobalId`, `childTransactionGlobalId`, `lastErrorMessage`를 포함했습니다.
- `result_payload`, `target_payload`, `item_payload` 원문은 응답하지 않고 `resultPayloadMasked`, `targetPayloadMasked`, 길이 정보만 제공합니다.

### ADM datasource / 권한 / UI

- ADM에서 `xyzDB`를 읽을 수 있도록 `xyzAdmDataSource`, `xyzJdbcTemplate`을 추가했습니다.
- `application.yml`에 `spring.datasource.xyz` local/prod 설정을 추가했습니다.
- prod profile은 `XYZ_DB_URL`, `XYZ_DB_USERNAME`, `XYZ_DB_PASSWORD` 환경변수 주입 구조로만 동작하게 유지했습니다.
- `AdmApiAuthFilter`에 `/adm/api/center-cut`을 `BATCH` 메뉴, `BATCH_READ` 버튼 권한으로 연결했습니다.
- ADM 배치 화면 하위에 Center-Cut 관제 섹션을 추가했습니다.
- UI 제공 항목:
  - Job 목록
  - Job 상세 조회
  - summary 카드
  - target/result 목록
  - 상태 필터
  - 실패 사유
  - parent/child transactionGlobalId
  - payload 마스킹 표시
  - 새로고침 버튼

### Smoke / Evidence

- `scripts/smoke-adm-center-cut-runtime.ps1`를 추가했습니다.
- `scripts/smoke-adm-runtime.ps1`가 ADM runtime 중 전용 Center-Cut smoke도 호출하게 보강했습니다.
- `scripts/smoke-adm-ui.ps1` 정적 marker에 Center-Cut UI/API marker를 추가했습니다.
- `scripts/check-feature-evidence.ps1`에 ADM Center-Cut Controller, Service, Test, UI, smoke evidence를 추가했습니다.
- `specs/기능_구현_매트릭스.html`에 ADM Center-Cut 관제 상태와 검증 상태를 반영했습니다.

## 3. Center-Cut Runtime 결과

- 결과 파일: `build/runtime-smoke/adm-center-cut-runtime-smoke-result.json`
- Job ID: `CPF_XYZ_CENTER_CUT_SAMPLE_JOB`
- summary:
  - totalCount: 4
  - readyCount: 0
  - runningCount: 0
  - successCount: 3
  - failedCount: 1
  - resultTotalCount: 4
  - resultSuccessCount: 3
  - resultFailedCount: 1
- target/result 조회:
  - targetCount: 1
  - resultCount: 1
  - hasParentTransactionGlobalId: true
  - hasChildTransactionGlobalId: true
  - hasFailureReason: true
  - rawPayloadExposed: false

## 4. 실행 검증

| check id | 상태 | 결과/증거 | 비고 |
|---|---|---|---|
| edu-mapper-db-slice | 미검증 | `XyzQueryEducationMapperSliceTest`, `xyz_edu_query_fixture.sql`, `CPF_XYZ_EDU_MAPPER_DB_USERNAME`, `CPF_XYZ_EDU_MAPPER_DB_USER` | 이번 작업 범위에서 MariaDB mapper slice test는 실행하지 않았습니다. |
| mariadb-full-install | 미검증 | `scripts/smoke-mariadb-full-install.ps1`, `build/sql-smoke/mariadb-full-install-result.json` | 이번 작업에서는 전체 DB 재설치 검증을 실행하지 않았습니다. DB 초기화 영향이 있어 별도 요청 시 수행합니다. |
| adm-runtime | 완료 | `scripts/smoke-adm-runtime.ps1`, `build/runtime-smoke/adm-runtime-smoke-result.json` | ADM health, OpenAPI, 운영 API, 배치 API, Center-Cut API, 정적 UI smoke 통과. |
| adm-permission-runtime | 미검증 | `scripts/smoke-adm-permission-runtime.ps1` | 이번 실행에서는 write/filter permission smoke 옵션을 켜지 않았습니다. |
| openapi-runtime | 완료 | `scripts/smoke-openapi.ps1`, `build/runtime-smoke/adm-runtime-smoke-result.json` | `ADM-CenterCut` tag 포함 확인. |
| adm-browser-click | 미검증 | `scripts/smoke-adm-ui.ps1 -BrowserClick` | Node/Playwright 계열 browser runtime이 PATH에 없어 클릭 검증은 실행하지 않았습니다. |
| standard-header-e2e | 미검증 | `scripts/smoke-standard-header-e2e.ps1`, `build/runtime-smoke/standard-header-e2e-result.json` | 이번 작업 범위에서는 실행하지 않았습니다. |
| redis-kafka-mq-broker | 미검증 | Redis/Kafka/MQ broker | 이번 작업 범위가 아닙니다. |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 리포트 갱신 후 재실행 기준으로 확인합니다. |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` | 리포트와 기능 매트릭스 상태 동기화 확인 대상입니다. |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` | Center-Cut evidence와 기존 필수 evidence marker 확인 대상입니다. |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` | 통과 확인. |

추가 실행 결과:

- `.\gradlew.bat :adm:test --offline --no-daemon --console=plain`: 완료
- `.\gradlew.bat :xyz:test --offline --no-daemon --console=plain`: 완료
- `.\gradlew.bat :adm:bootJar --offline --no-daemon --console=plain`: 완료
- `.\gradlew.bat test --offline --no-daemon --console=plain`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-center-cut-adapter.ps1 -RequireRun`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-ui.ps1`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1`: 완료
- `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake`: 완료

미검증/실패 이력:

- `node --check adm\src\main\resources\static\adm\adm.js`: 실패. `node`가 PATH에 없어 JS 구문 검사는 미검증입니다.
- 첫 번째 `scripts/smoke-adm-runtime.ps1`: 실패. 기존 bootJar가 새 Controller를 포함하지 않아 OpenAPI tag `ADM-CenterCut`가 없었습니다.
- 조치: `.\gradlew.bat :adm:bootJar --offline --no-daemon --console=plain` 실행 후 ADM runtime smoke를 재실행했고 통과했습니다.

## 5. 남은 리스크

- ADM Center-Cut API와 정적 UI는 검증했지만 실제 브라우저 클릭 검증은 미검증입니다.
- 운영자 조치 버튼인 강제 재실행, 중지, lock 해제, 2인 승인 플로우는 이번 범위가 아닙니다.
- `mariadb-full-install`은 이번 작업에서 재실행하지 않았습니다. 전체 DB 재설치 검증은 데이터 초기화 영향이 있으므로 별도 작업으로 분리해야 합니다.
- `adm-permission-runtime` write/filter smoke는 이번 실행에서 옵션을 켜지 않았습니다.
- Node/Playwright가 PATH에 없어 JS 정적 구문 검사와 browser click smoke는 미검증입니다.

## 6. 다음 보강 후보

1. ADM Center-Cut 브라우저 클릭 smoke 추가 및 운영자 UX 세부 검증.
2. Center-Cut 운영 조치 API 추가: 재시도 요청, 중지 요청, force run, lock 해제, 2인 승인.
3. Center-Cut 업무 adapter 다중 업무 확장 구조: XYZ 외 업무 target/result adapter 등록형 조회.
4. `scripts/smoke-adm-runtime.ps1` 실행 전 bootJar 최신성 보장 또는 자동 빌드 옵션 추가.
5. `scripts/smoke-mariadb-full-install.ps1`를 별도 안전 DB/schema로 실행해 전체 설치 idempotent 검증 재수행.
6. ADM permission write/filter runtime smoke를 Center-Cut read 권한까지 포함해 확장.
