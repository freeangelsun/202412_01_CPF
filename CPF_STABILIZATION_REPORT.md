# CPF 2차 안정화 작업 리포트

작성일: 2026-06-18  
기준 요청서: `CPF_CODEX_REQUEST_20260618_01.md`  
커밋 여부: 커밋하지 않음

## 요약

- `checkLegacyName`, `checkUtf8`, `checkMojibake`, `checkSqlStandard`, `test`, `qualityGate`를 통과했습니다.
- MariaDB에서 `specs/sql/00_all_install_and_smoke.sql`을 실제 실행했고, `source` 방식과 `utf8mb4` 기준으로 smoke row count를 확인했습니다.
- XYZ EDU Batch `Tasklet`, `Chunk`, `Retry` Job을 실제 실행했고 Spring Batch `BATCH_*` 및 CPF `pfw_batch_execution`, `pfw_batch_step_execution` 적재를 확인했습니다.
- ADM 앱을 실제 기동해 `/v3/api-docs`, `/swagger-ui/index.html`, 배치/로그/알림/동적 로그레벨 API smoke를 확인했습니다.
- `/swagger-ui.html`은 HTTP 500으로 남았습니다. `/swagger-ui/index.html`은 HTTP 200이라 Swagger UI 접근 자체는 가능하지만, canonical path 보강이 필요합니다.

## CPF 2차 안정화 체크리스트

| 번호 | 항목 | 상태 | 증빙 | 비고 |
|---|---|---|---|---|
| 1 | checkLegacyName 성공 | 완료 | `scripts/check-legacy-name.ps1`, `qualityGate` 성공 |  |
| 2 | FPS/Fps/fps 잔재 재확인 | 완료 | `checkLegacyName` 성공 | legacy alias 예외 없음 |
| 3 | checkMojibake 성공 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake`, `qualityGate` 성공 |  |
| 4 | mojibake 전수 제거 | 일부 | 광역 검색 결과는 요청서의 의도적 패턴만 남음 | `CPF_CODEX_REQUEST_20260618_01.md`의 검색 패턴은 사용자 입력이라 유지 |
| 5 | checkUtf8 성공 | 완료 | `scripts/check-utf8.ps1`, `qualityGate` 성공 |  |
| 6 | qualityGate 성공 | 완료 | `.\gradlew.bat qualityGate --offline` 성공 | 최종 문서 변경 후 재실행 |
| 7 | 핵심 Java 포맷 정상화 | 완료 | PFW/MBR/ADM/XYZ 핵심 파일 컴파일 및 test 성공 | 콘솔 `Get-Content` 표시는 한글이 깨져 보일 수 있어 `rg`/UTF-8 기준으로 확인 |
| 8 | 핵심 SQL 포맷 정상화 | 완료 | `scripts/check-sql-standard.ps1` 성공 |  |
| 9 | 핵심 한글 가이드 문서 포맷 정상화 | 일부 | `specs/기능_구현_매트릭스.md` 배치/Swagger 검증 상태 갱신 | 전체 가이드 브라우저 렌더링 검증은 미수행 |
| 10 | 기능_구현_매트릭스.md 재정리 | 완료 | 배치 Job, Execution, Step, Swagger 행을 실제 검증 결과와 동기화 |  |
| 11 | 가이드 문서와 실제 소스 대조 | 일부 | Controller/SQL/Swagger/EDU 주요 항목 대조 | 전체 문서 전수 대조는 다음 보강 후보 |
| 12 | EDU Batch Job bean 로딩 | 완료 | XYZ 앱 기동 후 EDU Batch endpoint 호출 성공 |  |
| 13 | EDU Tasklet Job 실제 실행 | 완료 | `XYZ_BATCH_TASKLET` PASS, Spring Batch executionId 1 |  |
| 14 | EDU Chunk Job 실제 실행 | 완료 | `XYZ_BATCH_CHUNK` PASS, read/write 25 |  |
| 15 | EDU Retry Job 실제 실행 | 완료 | `XYZ_BATCH_RETRY` PASS |  |
| 16 | BATCH_JOB_INSTANCE 적재 | 완료 | row count 3 | `build/cpf-stabilization/xyz-batch-smoke-result.json` |
| 17 | BATCH_JOB_EXECUTION 적재 | 완료 | row count 3 |  |
| 18 | BATCH_STEP_EXECUTION 적재 | 완료 | row count 3 |  |
| 19 | pfw_batch_execution 연계 | 완료 | `pfw_batch_execution_STABILIZATION` row count 3 | Spring Batch execution id와 연결 |
| 20 | 배치 처리 건수/수량 조회 | 완료 | Chunk read/write 25, ADM execution/detail API PASS |  |
| 21 | Swagger /v3/api-docs 확인 | 완료 | ADM `/v3/api-docs` HTTP 200, `adm-openapi.json` 저장 |  |
| 22 | EDU Swagger Tag 확인 | 완료 | OpenAPI tag 검사 PASS | `ADM-Batch`, `ADM-Logs`, `ADM-Notification` 확인 |
| 23 | ADM Batch API smoke | 완료 | jobs/schedules/simulation/executions/detail/relations/targets/instances/calendar PASS |  |
| 24 | ADM Online Transaction API smoke | 완료 | `/adm/api/logs`, `/adm/api/logs/{logIdx}` PASS |  |
| 25 | ADM Error API smoke | 완료 | `/adm/api/logs?logType=ERROR` PASS | 오류 row 0건이어도 API 응답 정상 |
| 26 | ADM Notification API smoke | 완료 | `/adm/api/notifications/rules`, `/delivery-logs` PASS | 새 Controller 추가 |
| 27 | Runtime Log Level API smoke | 완료 | rules/register/rollback PASS | 등록 후 삭제 원복 |
| 28 | 테스트 SKIPPED/NO-SOURCE 사유 정리 | 완료 | 루트/ADM/PFW/XYZ test source 없음 | ACC/CMN/MBR 테스트는 실행됨 |
| 29 | 남은 미구현/미검증 항목 정리 | 완료 | 아래 리스크/다음 작업 후보 참고 |  |

## 주요 수정 내용

- ADM Batch가 다중 DataSource 환경에서 Spring Batch repository를 찾도록 `AdmBatchRepositoryConfig`를 추가했습니다.
- XYZ EDU Batch도 PFW DataSource 기반 Spring Batch repository를 쓰도록 `XyzBatchRepositoryConfig`를 추가하고, 실제 Job 실행 결과를 CPF 배치 메타에 기록하게 보강했습니다.
- ADM 알림 조회 API를 추가하고 권한 필터에 `/adm/api/notifications` 읽기 권한을 연결했습니다.
- MBR Controller validation 테스트의 깨진 주석과 잘못된 기대 메시지를 정상 한글 기준으로 정리했습니다.
- PFW 기본 예외/resolver/거래 헤더 검증 파일의 남은 깨진 주석과 기본 메시지를 정상 한글로 정리했습니다.
- `adm`/`xyz` 설정 파일 주석과 `specs/기능_구현_매트릭스.md`의 검증 상태를 현행화했습니다.
- 로컬 MariaDB의 기존 깨진 `MPFW900001` 메시지는 설치 SQL 재실행 전 임시 보정했고, 이후 전체 설치 SQL 재실행으로 정본 기준을 재적재했습니다.

전체 변경 파일은 `CPF_STABILIZATION_CHANGED_FILES.txt`에 별도 기록했습니다.

## 실제 확인한 파일과 산출물

- 요청서: `CPF_CODEX_REQUEST_20260618_01.md`
- SQL: `specs/sql/00_all_install_and_smoke.sql`, `specs/sql/02_create_service_users.sql`, `specs/sql/50_framework_seed_data.sql`, `specs/sql/60_adm_seed_data.sql`
- 매트릭스: `specs/기능_구현_매트릭스.md`
- 핵심 코드: `AdmBatchRepositoryConfig`, `AdmNotificationController`, `AdmDynamicLogLevelBroadcastService`, `XyzBatchRepositoryConfig`, `XyzBatchEducationConfig`, `XyzBatchEducationController`, `CpfGlobalExceptionHandler`, `TransactionHeaderValidationInterceptor`, `MbrControllerValidationTest`
- Smoke 산출물: `build/cpf-stabilization/xyz-batch-smoke-result.json`, `build/cpf-stabilization/adm-smoke-result.json`, `build/cpf-stabilization/adm-openapi.json`

## 실행한 명령어와 결과

| 명령 | 결과 |
|---|---|
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake` | 성공 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1` | 성공 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1` | 성공 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1` | 성공 |
| `rg` 광역 mojibake 패턴 검색 | 소스/SQL/문서 매칭 없음, 요청서의 검색 패턴만 매칭 |
| `.\gradlew.bat clean :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline` | 성공 |
| `.\gradlew.bat :adm:bootJar :xyz:bootJar --offline` | 성공 |
| MariaDB `source specs/sql/00_all_install_and_smoke.sql` | 성공 |
| XYZ 앱 기동 후 `/xyz/edu/batch/tasklet|chunk|retry/run` | 모두 PASS |
| ADM 앱 기동 후 `/v3/api-docs` | HTTP 200 |
| ADM API smoke | 대부분 PASS, `/swagger-ui.html`만 실패 |
| `.\gradlew.bat test --offline --rerun-tasks` | 성공 |
| `.\gradlew.bat qualityGate --offline --rerun-tasks` | 성공 |
| `.\gradlew.bat qualityGate --offline` | 성공 |

## 실패한 명령어와 원인

- `.\gradlew.bat :mbr:test --offline` 초기 실행 실패: `MPFW900001` 응답 메시지가 깨진 DB row를 타고 내려와 기대값과 불일치했습니다. 테스트 기대값과 PFW 기본 메시지를 정리하고, 이후 설치 SQL 재실행으로 해결했습니다.
- PowerShell `Get-Content -Raw | mariadb` 방식의 설치 SQL 실행 실패: PowerShell 파이프가 한글 SQL을 `?`로 변환해 SQL 문법 오류가 발생했습니다. MariaDB 클라이언트의 `source` 명령으로 파일을 직접 읽게 바꿔 성공했습니다.
- ADM `/swagger-ui.html` smoke 실패: HTTP 500입니다. `/v3/api-docs`와 `/swagger-ui/index.html`은 성공하므로 Swagger 자체보다 legacy/canonical UI 경로 처리 문제로 봅니다.

## 미검증 항목과 사유

- 브라우저 실제 클릭 자동화: 이번에는 API smoke까지 수행했고 Playwright/브라우저 클릭 검증은 수행하지 않았습니다.
- Redis/Kafka 실 broker 전파: 로컬 broker를 이번 범위에서 기동하지 않아 DB fallback/API 기준으로만 확인했습니다.
- 실제 운영 scheduler 자동 수행: EDU Job 수동 실행과 ADM schedule simulation은 확인했지만, 장시간 scheduler trigger는 미검증입니다.
- 권한별 화면 비활성/숨김 UX: API 권한 smoke는 일부 확인했지만 권한별 브라우저 화면 전환 검증은 미수행입니다.

## 기능_구현_매트릭스와 실제 구현 대조 결과

- 배치 Job 행: 실제 `Tasklet`, `Chunk`, `Retry` 실행과 `BATCH_*` 적재 완료로 검증 방법을 갱신했습니다.
- 배치 Execution/Step 행: ADM execution 목록/상세 API와 Spring Batch step 적재 확인으로 갱신했습니다.
- Swagger/OpenAPI 행: ADM `/v3/api-docs`와 `/swagger-ui/index.html` 성공, `/swagger-ui.html` 실패를 그대로 반영했습니다.
- 일부 구현으로 남긴 항목은 실제 운영 scheduler, 브라우저 UX, broker 연동, 다운로드 권한 시나리오처럼 이번 범위에서 자동 검증하지 못한 기능입니다.

## 남은 리스크

- `/swagger-ui.html` HTTP 500은 다음 보강에서 우선 수정해야 합니다.
- `CPF_CODEX_REQUEST_20260618_01.md`에는 의도적 mojibake 검색 패턴이 남아 있어 광역 `rg`에는 계속 잡힙니다. 공식 `checkMojibake`는 통과합니다.
- PowerShell `Get-Content`는 한글 UTF-8 파일도 콘솔 문자셋 때문에 깨져 보일 수 있습니다. SQL 실행은 반드시 MariaDB `source` 또는 UTF-8 보존 방식으로 해야 합니다.
- ADM smoke 결과 JSON의 긴 상세 문자열은 PowerShell 출력/JSON 직렬화 경로에서 UTF-8 표시가 깨져 보일 수 있으나, DB 설치 smoke 출력은 한글 정상 값을 확인했습니다.

## 다음 작업 후보

1. `/swagger-ui.html` legacy path 500 수정 및 OpenAPI JSON 품질 gate 자동화.
2. ADM 화면 브라우저 클릭 검증: 권한별 버튼 숨김/비활성 사유, 배치 관계/수행 대상/로그 상세 UX 확인.
3. Redis/Kafka broker 실연동 테스트와 DB fallback 장애 시나리오 검증.
4. Spring Batch scheduler 자동 trigger, 영업일/수행 가능 시간/선행 Job 조건 기반 장시간 시나리오 검증.
5. ADM 다운로드 권한/마스킹/감사 로그를 별도 다운로드 감사 테이블까지 확장.
6. `NO-SOURCE` 모듈인 ADM/PFW/XYZ에 핵심 slice/context 테스트 추가.

## 항상 지켜야 할 기준 확인

| 기준 | 확인 |
|---|---|
| 문서와 소스, SQL, Swagger, EDU 샘플 일치 | 주요 배치/Swagger 행은 현행화, 전체 문서 전수 대조는 다음 후보 |
| README는 짧은 진입점 | 이번 작업에서 README 구조 변경 없음 |
| 기능 변경 시 가이드/SQL/EDU 동기화 | 배치/Swagger 매트릭스 갱신 |
| 신규 주석/설명/SQL COMMENT 한글 | 신규/수정 주석은 한글로 작성 |
| 운영 코드 주석은 기능 단위, EDU는 자세히 | PFW/MBR/Batch 관련 주석 정리 |
| 최종 리포트에 수행/검증/리스크/보류/다음 후보 기록 | 본 문서에 기록 |
