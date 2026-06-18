# CPF 안정화 작업 리포트

작성 시각: 2026-06-18 13:30:35 +09:00

## 1. 작업 개요

`CPF_CODEX_REQUEST_20260618_01.md` 갱신본을 기준으로 실제 소스, SQL, 문서, EDU 샘플, 검증 명령을 다시 확인했습니다. 이번 작업에서는 과거 명칭 잔재 제거, 검사 스크립트 강화, 기능 구현 매트릭스 작성, 문서 정본 연결, MariaDB 설치 SQL 재실행 검증을 수행했습니다. 커밋은 하지 않았습니다.

요청 파일 `CPF_CODEX_REQUEST_20260618_01.md`는 사용자가 갱신한 입력 파일로 보고, 작업 내용으로 수정하지 않았습니다.

## 2. CPF 완성도 보강 체크리스트

| 번호 | 항목 | 상태 | 증빙 | 비고 |
|---|---|---|---|---|
| 1 | FPS/Fps/fps 잔재 전수 검색 | 완료 | `rg`, `scripts/check-legacy-name.ps1`, `gradlew checkLegacyName` 실행 | 요청/리포트/검사 스크립트/빌드 산출물 제외 |
| 2 | FPS/Fps/fps 잔재 제거 | 완료 | 핵심 Java 클래스/헤더/설정 명칭을 `Cpf/CPF/cpf`로 변경, 검색 결과 0건 | 예외 스크립트 내부 패턴은 검사 목적상 보유 |
| 3 | legacy alias 잔존 사유 문서화 | 완료 | 본 리포트에 제외 범위 기록 | 운영 호환 alias는 소스에 남기지 않음 |
| 4 | mojibake 전수 검색 | 완료 | 강화된 `check-utf8.ps1 -CheckMojibake` 실행 | 다수 파일 발견 |
| 5 | mojibake 전수 제거 | 실패 | 검사 실패 파일 다수 | CMN 기본 DTO/유틸 일부만 정리 |
| 6 | check-utf8/checkMojibake 강화 | 완료 | 비 ASCII 리터럴 대신 코드포인트/정규식 기반 검사로 보강 | PowerShell 5.1 인코딩 문제도 보정 |
| 7 | Java/JS/SQL/MD/YAML/HTML 포맷 정상화 | 일부 | 손댄 파일 컴파일 통과, `git diff --check` 오류 없음 | 전체 포맷 전수 정리는 미완료 |
| 8 | specs 문서 파편화 통합 | 일부 | 보조 md deprecated 표시, 정본 가이드 연결 | 실제 삭제/완전 통합은 미완료 |
| 9 | 기존 한글 가이드 문서 정본화 | 일부 | README/index/3대 가이드/SQL 가이드 연결 갱신 | 모든 기능 상세 반영은 계속 필요 |
| 10 | 기능 구현 매트릭스 작성 | 완료 | `specs/기능_구현_매트릭스.md` 신규 작성 | 보수적으로 일부/미검증 표기 |
| 11 | deprecated 문서 표시 | 완료 | 보조 md 5개 상단에 deprecated 안내 추가 | 정본은 한글 가이드 |
| 12 | PFW/CMN/ADM/주제영역 책임 정리 | 일부 | 가이드/매트릭스에 반영 | 의존성 자동 분석은 미수행 |
| 13 | 기본 온라인 거래 EDU 샘플 | 일부 | `xyz` EDU/거래 로그 샘플 존재 | 실제 앱 호출 검증 미수행 |
| 14 | 표준 헤더/거래 ID EDU 샘플 | 일부 | `HeaderDTO`, 헤더 표준 문서, 거래 로그 smoke 확인 | Swagger 실제 호출 미검증 |
| 15 | 트랜잭션 분리 EDU 샘플 | 일부 | `XyzAuditSampleService` 확인 | 통합 테스트 미검증 |
| 16 | 주제영역 간 호출 EDU 샘플 | 일부 | ACC -> MBR 샘플 컴파일 통과 | 서비스 동시 기동 미검증 |
| 17 | 외부 연동 호출 EDU 샘플 | 일부 | `CpfWebClient` 계열 컴파일 통과 | timeout/retry 실행 검증 필요 |
| 18 | 입력/세션/요청 컨텍스트 EDU 샘플 | 일부 | PFW context/filter 컴파일 통과 | 실제 요청 검증 미수행 |
| 19 | 공통 코드/메시지 EDU 샘플 | 일부 | ADM/CMN 코드, 메시지 API 컴파일 통과 | 브라우저 검증 미수행 |
| 20 | 표준 예외/오류 EDU 샘플 | 일부 | 예외 클래스 `Cpf*` rename 및 컴파일 통과 | mojibake 다수 남음 |
| 21 | 로그/감사/추적 EDU 샘플 | 일부 | 감사/거래 로그 API 컴파일, smoke row 확인 | 앱 호출 미검증 |
| 22 | 파일/다운로드 EDU 샘플 | 일부 | CMN file exchange/ADM 로그 다운로드 일부 존재 | 권한/감사 다운로드 미완성 |
| 23 | 메시징/알림 EDU 샘플 | 일부 | CMN MQE, PFW notification SQL 존재 | ADM 알림 관리 UI/API 미완성 |
| 24 | 보안/JWT/OAuth EDU 샘플 | 일부 | CMN security 계열 컴파일 | OTP QR/복구코드 미완성 |
| 25 | 배치 EDU 샘플 | 일부 | `XyzBatchEducationController` 확인 | 실제 Job 실행 미검증 |
| 26 | EDU 샘플 Swagger 정리 | 일부 | Controller annotation 다수 확인 | `/v3/api-docs` 미검증 |
| 27 | EDU seed 데이터 보강 | 일부 | MariaDB smoke seed row 확인 | 전체 EDU seed 매트릭스는 미완성 |
| 28 | EDU Batch Job seed 등록 | 완료 | `pfw_batch_job` 3건, schedule 2건 확인 | DB 기준 |
| 29 | EDU Batch Job 실제 실행 | 미검증 | 앱 기동/Job 실행 미수행 | BATCH_* row 0 |
| 30 | BATCH_JOB_INSTANCE 적재 | 실패 | MariaDB smoke 결과 0건 | 테이블은 생성됨 |
| 31 | BATCH_JOB_EXECUTION 적재 | 실패 | MariaDB smoke 결과 0건 | 실제 Job 실행 필요 |
| 32 | BATCH_STEP_EXECUTION 적재 | 실패 | MariaDB smoke 결과 0건 | 실제 Job 실행 필요 |
| 33 | pfw_batch_execution 연계 | 일부 | `pfw_batch_execution` 1건 seed 확인 | Spring Batch execution id 실연계 미검증 |
| 34 | 배치 처리 건수/수량/결과 통계 | 일부 | `pfw_batch_execution`, `pfw_batch_step_execution` count 컬럼 확인 | read/write/filter/rollback 전체 미검증 |
| 35 | ADM 배치 목록 완성 | 일부 | Controller/API/UI 존재, 컴파일 통과 | 브라우저 클릭 미검증 |
| 36 | ADM 배치 상세 완성 | 일부 | execution detail API 존재 | 실제 화면 클릭 미검증 |
| 37 | Execution 목록/상세/Step 상세 | 일부 | `/adm/api/batch/executions` 계열 확인 | 실제 데이터/브라우저 미검증 |
| 38 | 자동 수행/반복 수행 설정 | 일부 | schedule 테이블/API 존재 | scheduler 실기동 미검증 |
| 39 | 수행 시뮬레이션 고도화 | 일부 | `/schedules/{scheduleId}/simulation` 존재 | cron 정밀 계산 고도화 필요 |
| 40 | 선행/후행/Trigger Job 관리 | 일부 | `pfw_batch_job_relation`, API 존재 | 그래프 UI 미구현 |
| 41 | 수행 대상 인스턴스 관리 | 일부 | `pfw_batch_execution_target`, API 존재 | 등록/배정/취소 API 부족 |
| 42 | 온라인 거래 조회/상세 완성 | 일부 | ADM 로그 화면/API 존재 | 브라우저 클릭 미검증 |
| 43 | 온라인 거래 검색조건 고도화 | 일부 | 주요 조건 UI 존재 | 전문/파라미터 검색 고도화 필요 |
| 44 | 온라인 거래 외부호출 수/오류 수/알림 수 표시 | 일부 | 로그/알림 테이블 일부 존재 | 통계 표시 완성 필요 |
| 45 | 오류 로그 조회/상세/조치 상태 | 일부 | 오류 상세 탭 일부 존재 | 조치 상태 workflow 미완성 |
| 46 | 알림 룰/채널/수신자/템플릿 관리 | 일부 | `pfw_notification_*` SQL 존재 | ADM UI/API 미완성 |
| 47 | 알림 발송 이력 조회 | 일부 | `pfw_notification_delivery_log` 1건 seed | ADM 조회 API/UI 필요 |
| 48 | Excel/CSV/로그 다운로드 | 일부 | ADM 로그 상세 다운로드 일부 존재 | 공통 다운로드 미완성 |
| 49 | 다운로드 권한/마스킹/감사 로그 | 일부 | 권한/마스킹 일부 존재 | 다운로드 감사 테이블/API 필요 |
| 50 | ADM 런타임 로그 레벨 변경 | 일부 | 동적 로그레벨 API/화면 존재 | broker 전파 미검증 |
| 51 | Runtime Log Level API/이력/권한 | 일부 | DB polling/API 일부 존재 | 권한별 브라우저 검증 필요 |
| 52 | Swagger 실제 호출 검증 | 미검증 | 앱 기동하지 않음 | `/v3/api-docs` 미호출 |
| 53 | ADM 브라우저 클릭 검증 | 미검증 | 브라우저/Playwright 미실행 | 화면 정적 파일만 확인 |
| 54 | qualityGate 성공 | 실패 | `gradlew qualityGate --offline` 실패 | 원인: `checkMojibake` |
| 55 | 문서/소스/SQL/Swagger/EDU/ADM 정합성 | 일부 | 매트릭스/가이드/SQL 연결 보강 | Swagger/브라우저/전체 mojibake 미완료 |

## 3. 실제 확인한 주요 파일

- 요청서: `CPF_CODEX_REQUEST_20260618_01.md`
- 루트/품질: `README.md`, `build.gradle`, `scripts/check-utf8.ps1`, `scripts/check-legacy-name.ps1`, `.gitignore`
- 문서: `specs/index.html`, `specs/개발_가이드.html`, `specs/관리자_가이드.html`, `specs/프레임워크_구성_가이드.html`, `specs/sql/README.md`, `specs/기능_구현_매트릭스.md`
- ADM: `AdmBatchController`, `AdmBatchOperationService`, `AdmMemberController`, `AdmPermissionController`, `adm/src/main/resources/static/adm/index.html`, `adm.js`
- EDU: `XyzBatchEducationController`, `XyzEducationController`, `xyz/src/main/java/cpf/xyz/edu/**`
- SQL: `specs/sql/00_all_install_and_smoke.sql`, `00_all_install.sql`, `02_create_service_users.sql`, `migration/flyway/V1__cpf_baseline_install.sql`
- CMN/PFW 기본: `HeaderDTO`, `CpfDTO`, `DataDTO`, `ValidationUtils`, `MaskingUtils`, PFW `Cpf*` exception/http/logging/workflow 클래스

## 4. 실제 수정한 파일 요약

상세 목록은 `CPF_STABILIZATION_CHANGED_FILES.txt`에 정리했습니다.

- 과거 `Fps*` Java 클래스명을 `Cpf*`로 rename하고 참조를 일괄 정리했습니다.
- `X-Fps` 계열 헤더/문서/설정 문자열을 CPF 기준으로 정리했습니다.
- `check-utf8.ps1`를 강화하고, `check-legacy-name.ps1`와 Gradle `checkLegacyName` task를 추가했습니다.
- `specs/기능_구현_매트릭스.md`를 신규 작성했습니다.
- 보조 md 문서에 deprecated 안내를 추가하고 README/index에서 정본 문서와 매트릭스로 연결했습니다.
- CMN 기본 DTO/유틸 일부의 깨진 한글 주석/메시지를 정상 한글로 정리했습니다.
- tracked runtime log 산출물 `mbr/logs/mbr/*.log`를 삭제 상태로 정리했습니다. `.gitignore`는 이미 로그를 제외하고 있으므로 커밋 시 추적 제거가 필요합니다.

## 5. 실제 실행한 검증 명령과 결과

| 명령 | 결과 | 메모 |
|---|---|---|
| `.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline` | 성공 | 6개 모듈 compileJava 성공 |
| `powershell -File scripts\check-utf8.ps1` | 성공 | UTF-8 strict decode 통과 |
| `powershell -File scripts\check-utf8.ps1 -CheckMojibake` | 실패 | 강화된 검사에서 다수 Java/XML/YML 파일 검출 |
| `powershell -File scripts\check-sql-standard.ps1` | 성공 | SQL 표준 검사 통과 |
| `powershell -File scripts\check-legacy-name.ps1` | 성공 | 레거시 명칭 검색 통과 |
| `.\gradlew.bat checkLegacyName --offline` | 성공 | Gradle task 연결 확인 |
| `.\gradlew.bat test --offline` | 성공 | 단, ACC/ADM/PFW/XYZ test는 SKIPPED 또는 NO-SOURCE 포함 |
| `.\gradlew.bat qualityGate --offline` | 실패 | `:checkMojibake` 실패 |
| `git diff --check` | 성공 | whitespace error 없음. CRLF/LF 경고만 표시 |
| MariaDB `00_all_install_and_smoke.sql` 1회 실행 | 성공 | smoke row count 출력 |
| MariaDB `00_all_install_and_smoke.sql` 2회 실행 | 성공 | seed 재실행/idempotent 성격 확인 |
| information_schema table/FK/index count | 성공 | `pfwdb=31`, `admdb=15`, `cmndb=4`, `mbrdb=4`, `accdb=1` |
| app 계정 DDL probe | 성공적으로 거부됨 | app 계정 CREATE TABLE 거부 확인 |
| migration/app 권한 조회 | 성공 | app은 DML, migration은 DDL 포함. password hash는 리포트에 기록하지 않음 |

## 6. MariaDB 검증 요약

- `pfwDB.BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION` 테이블은 설치됐습니다.
- 하지만 실제 Spring Batch Job 실행을 하지 않았으므로 위 3개 테이블 row count는 모두 0입니다.
- CPF 운영 메타는 seed 기준으로 적재됐습니다: `pfw_batch_job=3`, `pfw_batch_schedule=2`, `pfw_batch_job_relation=2`, `pfw_batch_execution=1`, `pfw_batch_step_execution=1`, `pfw_batch_execution_target=1`.
- FK/index count 확인: `pfwdb FK=18/index=141`, `admdb FK=10/index=62`, `cmndb FK=1/index=28`, `mbrdb FK=3/index=28`, `accdb index=4`.
- 앱 계정 DDL 금지는 실제 MariaDB 오류로 확인했습니다.

## 7. 실패한 명령과 원인

- `scripts/check-utf8.ps1` 1차 보강 후 PowerShell parser 오류가 발생했습니다. 원인은 PowerShell 5.1이 UTF-8 BOM 없는 스크립트의 비 ASCII mojibake 리터럴을 ANSI로 읽은 것입니다. 이후 코드포인트/정규식 기반으로 수정했습니다.
- `scripts/check-utf8.ps1 -CheckMojibake` 최종 실행은 실패했습니다. 원인은 실제 깨진 한글이 ACC/ADM/CMN/MBR/PFW/XYZ 여러 Java/XML/YML 파일에 남아 있기 때문입니다.
- `qualityGate`는 `checkMojibake` 실패 때문에 실패했습니다.

## 8. 미검증 항목과 사유

- Swagger `/v3/api-docs`, `/swagger-ui.html`, `/swagger-ui/index.html`: 앱을 기동하지 않아 미검증입니다.
- ADM 브라우저 클릭 검증: 브라우저/Playwright를 실행하지 않아 미검증입니다.
- Spring Batch 실제 Job 실행과 `BATCH_*` 적재: 앱 기동 및 EDU Job 실행을 하지 않아 미검증/실패 상태입니다.
- VSCode Problems: IDE 내부 Problems 목록을 CLI에서 직접 조회하지 못해 미검증입니다.
- Redis/Kafka/RabbitMQ broker 전파: 외부 broker 실검증은 이번 실행에서 제외했습니다.
- TOTP QR/복구코드: 실제 OTP 앱 호환 등록/검증 UI/API는 미완성입니다.

## 9. 남은 리스크

- mojibake가 전체 소스에 넓게 남아 있어 qualityGate가 실패합니다.
- `CPF_CODEX_REQUEST_20260618_01.md`는 사용자 입력 파일로 변경 상태이며, 작업 변경과 분리해야 합니다.
- 대량 rename으로 git status에는 delete/untracked 쌍이 많이 보입니다. 커밋 전 rename으로 검토해야 합니다.
- `mbr/logs`는 tracked 파일이었으므로 커밋 시 삭제로 반영해 추적을 끊어야 합니다.
- Swagger annotation은 존재하지만 실제 OpenAPI JSON 품질은 확인하지 않았습니다.
- ADM 화면은 파일상 기능이 있으나 실제 운영자 UX 클릭 검증이 필요합니다.

## 10. 다음 보강 후보

1. 전체 mojibake 전수 정리: 강화된 `checkMojibake`가 통과할 때까지 ACC/ADM/CMN/MBR/PFW/XYZ 파일을 정상 한글로 복구.
2. Spring Batch 실기동 검증: ADM 또는 XYZ 앱을 기동하고 EDU Job을 실행해 `BATCH_JOB_INSTANCE`, `BATCH_JOB_EXECUTION`, `BATCH_STEP_EXECUTION` 적재 확인.
3. Swagger 자동 검증: 앱 기동 후 `/v3/api-docs` JSON을 저장하고 schema/tag/example 품질 gate 추가.
4. ADM 브라우저 검증: 로그인, 회원관리, 권한, 로그 상세 포맷팅, 배치 시뮬레이션/관계/수행대상 버튼 클릭 검증.
5. 배치 운영 고도화: 관계 그래프 UI, 수행 대상 등록/배정/취소 API, scheduler 실제 실행, 실패 재처리 시나리오.
6. 다운로드 공통화: Excel/CSV/로그 다운로드 권한, 사유, 마스킹, 감사 로그를 공통 정책으로 묶기.
7. 알림 운영 완성: 알림 룰/채널/수신자/템플릿 ADM UI/API와 발송 이력 조회.

## 11. 추가 개선 의견

- `qualityGate`가 실패하는 현재 상태가 오히려 정상 신호입니다. 이전처럼 약한 검사로 통과시키는 것보다, 깨진 한글을 모두 제거한 뒤 gate를 다시 녹색으로 만드는 편이 맞습니다.
- `BATCH_*`는 테이블 생성만으로 완료가 아닙니다. 반드시 실제 Job 실행으로 Spring Batch repository row가 생기는지 확인해야 합니다.
- 기능이 많은 ADM은 “화면 있음”과 “운영 가능”을 구분해야 합니다. 버튼 권한, 비활성 사유, 감사 사유, 마스킹, 다운로드 권한까지 클릭 시나리오로 검증해야 합니다.
- 문서 정본은 `README.md`, `specs/index.html`, `specs/프레임워크_구성_가이드.html`, `specs/개발_가이드.html`, `specs/관리자_가이드.html`, `specs/sql/README.md`, `specs/기능_구현_매트릭스.md`로 유지하는 방향이 좋습니다.

## 12. 문서/소스/SQL/Swagger/ADM/EDU 정합성 결과

- 문서와 SQL: `BATCH_*`, `pfw_batch_*`, ADM 회원/권한/배치/로그 기준은 가이드와 SQL 가이드에 연결했습니다.
- 문서와 EDU: `xyz` EDU Batch 샘플과 개발 가이드 배치 매트릭스는 연결되어 있습니다.
- 문서와 ADM: 관리자 가이드는 ADM 주요 메뉴를 설명하지만 브라우저 검증은 아직 없습니다.
- 문서와 Swagger: Controller annotation은 확인했지만 OpenAPI JSON은 미검증입니다.
- 문서와 검사: README/CI 문서에 `qualityGate`, UTF-8, mojibake, legacy name 검사 기준을 연결했습니다.

## 13. 한글 가이드와 실제 구현 대조 결과

- `specs/개발_가이드.html`: EDU 배치, CMN optional DB, 신규 모듈 기준을 실제 소스 경로와 연결했습니다.
- `specs/관리자_가이드.html`: ADM 회원관리, 권한, 통합 로그, 배치, 보안 운영 기준을 현재 테이블/API 기준으로 설명합니다.
- `specs/프레임워크_구성_가이드.html`: PFW/CMN/ADM/업무 DB 소유권과 Spring Batch/Flyway 기준을 설명합니다.
- `specs/기능_구현_매트릭스.md`: 기능별 구현 상태를 보수적으로 표시했습니다. `완료`보다 `일부 구현`이 많은 것이 현재 실제 상태입니다.

## 14. 민감정보 처리

- 사용자가 제공한 DB 비밀번호와 MariaDB password hash는 리포트에 기록하지 않았습니다.
- DB 권한 검증 결과는 권한 범위와 성공/거부 여부만 기록했습니다.

## 15. 커밋 여부

커밋하지 않았습니다.
