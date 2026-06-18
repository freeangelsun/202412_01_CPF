# CPF 안정화 작업 리포트

작성 시각: 2026-06-18 11:38:28 +09:00

## 1. 작업 개요

CPF 프레임워크의 기반 안정화 요청에 따라 배치 관리, Spring Batch 표준 저장소, ADM 배치 관제 API/UI, SQL/Flyway/문서/EDU 샘플 정합성을 보강했습니다. 커밋은 하지 않았습니다.

## 2. 사용자 요청사항 원문 요약 및 수행 체크리스트

| 번호 | 요청사항 | 수행 상태 | 증빙/결과 | 비고 |
|---|---|---|---|---|
| 1 | PFW는 프레임워크 핵심 코어로 보호 영역, CMN은 업무 공통 기능/API 영역으로 역할 분리 | 일부 | 문서와 SQL 가이드에 `BATCH_*`, `pfw_batch_*`, `pfw_notification_*` 책임 반영 | 전체 모듈 의존성 자동 분석은 미수행 |
| 2 | 문서, 소스, SQL, Swagger, ADM 화면, EDU 샘플 정합성 유지 | 일부 | ADM Batch API/UI, SQL, 가이드, EDU 샘플 동시 갱신 | 앱 기동 Swagger JSON 검증은 미검증 |
| 3 | README는 짧게 유지하고 상세 내용은 가이드 문서로 연결 | 완료 | `README.md`를 진입점 중심으로 정리 |  |
| 4 | 기능 변경 시 README/개발가이드/관리자가이드/SQL가이드/EDU샘플 함께 현행화 | 완료 | README, 개발/관리자/구성/SQL 가이드, XYZ EDU Batch 갱신 |  |
| 5 | 신규 주석/설명/SQL COMMENT는 한글 작성 | 완료 | 신규 SQL COMMENT와 코드 설명 한글 작성 | Spring Batch 표준 영문 식별자는 유지 |
| 6 | UTF-8 설정만이 아니라 실제 mojibake/한글 깨짐 전수 제거 | 일부 | `check-utf8.ps1 -CheckMojibake` 통과 | 요청 파일은 외부 입력으로 제외, 레거시 광범위 의미 검수는 별도 |
| 7 | 한 줄로 몰린 Gradle/YAML/SQL/MD/PS1/Java 포맷 복구 | 미검증 | 별도 전수 포맷 복구 검사는 하지 않음 | 이번 변경 파일은 컴파일/품질 gate 통과 |
| 8 | MariaDB 실 DB 기준 00_all_install_and_smoke.sql 실행 및 검증 | 완료 | 동일 SQL 2회 실행 성공, smoke row count 확인 | 비밀번호는 기록하지 않음 |
| 9 | DB 비밀번호는 소스/문서/리포트에 기록하지 않고 로컬 실행에만 사용 | 완료 | 리포트/변경 파일에 비밀번호 미기록 | 명령 실행 시 로컬 환경변수로만 사용 |
| 10 | Swagger가 PFW에만 갇히지 않고 ACC/MBR/ADM/XYZ 실행 모듈에서 노출되는 구조 검증 | 미검증 | 앱 기동 및 `/v3/api-docs` 미실행 | 코드 어노테이션은 갱신 |
| 11 | /v3/api-docs, /swagger-ui.html, /swagger-ui/index.html 경로 검증 또는 미검증 사유 기록 | 미검증 | 서버 기동 검증 미수행 | 다음 작업 필요 |
| 12 | AutoConfiguration.imports 정상 형식 확인 및 자동 설정 로딩 구조 점검 | 미검증 | 이번 범위에서 파일 점검 미수행 |  |
| 13 | OpenAPI 그룹 하드코딩 문제 분석 및 property 기반 확장 구조 검토 | 미검증 | 이번 범위에서 구조 점검 미수행 |  |
| 14 | ADM 온라인 거래 조회/오류 조회/배치 조회/알림 조회의 표준 검색조건과 필터 보강 | 일부 | 배치 관계/수행대상/시뮬레이션 조회 추가 | 온라인/오류/알림 전체 검색 UX는 미완성 |
| 15 | 거래 ID, 글로벌 ID, UUID, Trace ID, Correlation ID, Request ID 기준 조회 제공 | 일부 | 기존 거래 로그 smoke에서 거래/trace/header 확인 | 전체 ADM 검색 조건 추가 구현은 미완성 |
| 16 | 헤더값, 고객번호/회원번호, 파라미터 특정 문구 포함 거래 검색 기능 검토/보강 | 일부 | 기존 ADM/SQL에 회원/고객/헤더 샘플 확인 | 파라미터 전문 검색 고도화는 미완성 |
| 17 | 개인정보/민감정보 검색 시 마스킹/권한 통제/저장 정책 검토 | 일부 | 기존 마스킹 정책과 문서 유지 | 다운로드/원문 권한 전수 검증은 미완료 |
| 18 | ADM 모든 목록/로그/이력 화면에서 권한에 따른 Excel/CSV/로그 다운로드 기능 검토/보강 | 일부 | 기존 로그 상세 다운로드는 확인, 전체 화면 공통 다운로드는 미구현 | 다음 작업 우선 |
| 19 | 다운로드 권한, 마스킹, 다운로드 사유, 다운로드 감사 로그 저장 정책 검토/보강 | 일부 | 권한 seed와 문서 방향 유지 | 다운로드 감사 테이블/API 전수 구현은 미완성 |
| 20 | 배치 관리/관제/스케줄/영업일/선행 Job/트리거 Job/실행 로그/재수행/중지/관계도 기능은 PFW 공통 기능으로 설계 | 완료 | `BATCH_*`, `pfw_batch_schedule`, `pfw_batch_job_relation`, `pfw_batch_execution_target` 반영 | 관계도 시각화는 다음 단계 |
| 21 | ADM은 PFW 배치 기능을 관리하는 운영 콘솔 역할로 설계 | 완료 | ADM Batch Controller/Service/UI에 조회 API와 버튼 추가 |  |
| 22 | ACC/MBR/PAY 등 주제영역은 실제 업무 배치 Job만 구현하고 PFW/ADM 공통 기능 사용 | 일부 | 개발 가이드에 방향 반영 | 실제 ACC/MBR 배치 샘플 추가는 미수행 |
| 23 | Spring Batch 표준 BATCH_* 테이블과 PFW 자체 배치 테이블 관계 검증 | 완료 | MariaDB에서 `BATCH_*`와 `pfw_batch_*` 설치 확인 | MariaDB는 lower_case_table_names 영향으로 소문자로 표시 |
| 24 | JobRepository 실 DB 운영 검증 | 일부 | 표준 테이블 설치와 채번 테이블 seed 확인 | 앱에서 실제 JobRepository 실행은 미검증 |
| 25 | 영업일만 수행, 수행 가능 시간, 수행 일자 시뮬레이션 기능 검토/보강 | 완료 | 스케줄 컬럼, `/simulation` API, ADM 버튼 추가 | cron 실제 다음 실행 계산은 단순 후보일 기준 |
| 26 | 선행 Job/후행 Job/트리거 Job 설정 및 관계 구조도 검토/보강 | 일부 | `pfw_batch_job_relation`, 조회 API, ADM 버튼 추가 | 그래프 UI 시각화는 미구현 |
| 27 | 수행 대기 인스턴스/수행 대상 인스턴스 관리 기능 검토/보강 | 완료 | `pfw_batch_execution_target`, 조회 API, smoke 확인 | 등록/배정 UI는 다음 단계 |
| 28 | 배치 실행 로그, Step 로그, 운영자 조작 로그 DB 저장 정책 정리 | 완료 | 기존 `pfw_batch_execution`, `pfw_batch_step_execution`, `pfw_batch_operation_log` 문서화 |  |
| 29 | 알림/노티 기능을 PFW 공통 기능 + ADM 관리 화면/API로 설계 | 일부 | `pfw_notification_rule`, `pfw_notification_delivery_log` 추가 | ADM 알림 관리 화면/API는 미구현 |
| 30 | 오류/배치 실패/지연/외부 API 오류/DB 오류 등 알림 대상 조건을 ADM에서 설정 가능하도록 설계 | 일부 | 배치 실패/보안 이벤트 seed 추가 | ADM 설정 화면/API는 다음 단계 |
| 31 | JWT/OAuth/Spring Security 책임을 PFW/CMN/ADM/주제영역으로 분리 | 일부 | 기존 문서와 구조 유지 | 이번 변경에서 보안 모듈 전수 재설계는 미수행 |
| 32 | ADM은 관리자 인증만 담당하고 비즈니스 인증은 각 주제영역에서 확장 | 일부 | 관리자 가이드 방향 유지 | 코드 전수 검증은 미수행 |
| 33 | CMN은 JWT/OAuth 공통 유틸/API를 제공하되 로그인 정책과 권한 판단은 소유하지 않음 | 일부 | 프레임워크 구성 가이드 방향 유지 | 코드 전수 검증은 미수행 |
| 34 | 테스트 disabled 상태 점검 및 가능한 범위에서 qualityGate 신뢰도 회복 | 일부 | `qualityGate` 성공, test는 일부 모듈 SKIPPED 확인 | skipped 테스트 활성화는 미수행 |
| 35 | Redis/Kafka/RabbitMQ 실 broker 테스트는 이번 범위에서 제외 | 완료 | 실 broker 테스트 미수행으로 명시 | 요청 범위 제외 |
| 36 | broker 미사용 환경에서 no-op/in-memory/DB fallback 구조 점검 | 일부 | DB fallback 캐시 이벤트와 배치/알림 DB 기준 유지 | broker 장애 시나리오 자동 테스트는 미수행 |
| 37 | ADM MFA/OTP는 이번 작업에서 무리하게 완성하지 않고 상태와 다음 단계 문서화 | 완료 | 관리자 가이드에 보안 운영 상태 유지 | TOTP QR/복구코드는 다음 단계 |
| 38 | VS Code Problems 기준 null/static warning 정리 | 미검증 | IDE Problems 직접 조회 불가 | 컴파일과 qualityGate는 통과 |
| 39 | 프로젝트 루트에 CPF_STABILIZATION_REPORT.md 생성 | 완료 | 이 파일 생성 |  |
| 40 | 프로젝트 루트에 CPF_STABILIZATION_CHANGED_FILES.txt 생성 | 완료 | 별도 파일 생성 예정 |  |
| 41 | 실행하지 않은 검증을 성공으로 보고하지 않음 | 완료 | 미검증 항목 별도 표기 |  |
| 42 | 실패한 명령과 미검증 항목을 숨기지 않음 | 완료 | 실패/미검증 섹션 작성 |  |
| 43 | 커밋하지 않음 | 완료 | git commit 미수행 |  |

## 3. 현재상황 리뷰

- 현재 브랜치: `master`
- 마지막 커밋: `19a35aa 중간 생성물 push`
- 최근 커밋 5개:
  - `19a35aa 중간 생성물 push`
  - `de6ecc5 20260616 작업분`
  - `047fd77 Rename project from FPS to CPF`
  - `e7269b5 테스트 작업중`
  - `87acbc3 FPS 공통 DTO 관리 적용`
- 작업 시작 전 변경 파일 여부: `CPF_CODEX_REQUEST_20260618_01.md`가 untracked 상태였습니다.
- 작업 시작 일시: 정확한 시작 시각은 명령 로그로 별도 캡처하지 못했습니다. 보고서 작성 시각은 상단에 기록했습니다.
- 일반 `git status`는 sandbox 사용자와 저장소 소유자가 달라 `dubious ownership` 오류가 발생했으며, 이후 `git -c safe.directory=D:/WORK_CPF/202412_01_CPF ...`로 상태를 확인했습니다.

## 4. PFW / CMN / ADM / 주제영역 책임 정리 결과

- PFW: Spring Batch 표준 저장소, CPF 배치 운영 메타, 영업일/관계/대상/알림 DB 기준을 담당하도록 보강했습니다.
- CMN: 기존 공통 채번/알림/업무 로그 역할은 유지했습니다.
- ADM: PFW 배치 기능을 운영자가 조회하고 실행하는 콘솔 역할로 API/UI를 보강했습니다.
- 주제영역: EDU는 배치 개발 샘플을 제공하고, 실제 업무 모듈은 Job 구현에 집중하는 방향을 문서화했습니다.
- 의존성 위반 여부: 자동 의존성 분석은 미수행입니다.

## 5. ADM 운영 조회/검색조건 보강 결과

- `/adm/api/batch/schedules/{scheduleId}/simulation` 추가.
- `/adm/api/batch/relations` 추가.
- `/adm/api/batch/execution-targets` 추가.
- ADM 배치 화면에 수행 시뮬레이션, 관계 조회, 수행 대상 조회 버튼과 필드를 추가했습니다.
- 온라인 거래/오류/알림의 전체 표준 검색조건 통합은 일부만 반영된 상태입니다.

## 6. ADM 다운로드 기능 보강 결과

- 기존 로그 상세 다운로드 기능은 유지했습니다.
- 모든 ADM 목록/이력 화면의 Excel/CSV/로그 다운로드, 사유 필수, 감사 로그, 원문 보기 권한 분리는 이번 작업에서 완성하지 못했습니다.

## 7. UTF-8 / mojibake 정리 결과

- `scripts/check-utf8.ps1 -CheckMojibake` 통과.
- 외부 입력 파일인 `CPF_CODEX_REQUEST_20260618_01.md`는 품질 gate 대상에서 제외했습니다.
- 신규/변경 SQL과 문서는 UTF-8 기준으로 작성했습니다.

## 8. MariaDB / SQL 검증 결과

- `specs/sql/00_all_install_and_smoke.sql` 실제 MariaDB 실행: 성공.
- 동일 SQL 재실행: 성공.
- smoke 결과 주요 row count:
  - `pfwDB.pfw_batch_job`: 3
  - `pfwDB.pfw_batch_schedule`: 2
  - `pfwDB.pfw_batch_job_relation`: 2
  - `pfwDB.pfw_batch_execution_target`: 1
  - `pfwDB.pfw_notification_rule`: 2
  - `pfwDB.pfw_notification_delivery_log`: 1
  - `admDB.adm_button`: 45
- information_schema 확인:
  - table count: `pfwdb=31`, `admdb=15`, `cmndb=4`, `mbrdb=4`, `accdb=1`
  - FK count: `pfwdb=18`, `admdb=10`, `cmndb=1`, `mbrdb=3`
  - index count: `pfwdb=141`, `admdb=62`, `cmndb=28`, `mbrdb=28`, `accdb=4`
- app/migration 계정 권한 분리: app 계정은 DML, migration 계정은 DDL 포함으로 확인했습니다. 비밀번호/hash 값은 리포트에 기록하지 않습니다.

## 9. Swagger/OpenAPI 검증 결과

- 코드 수준 Swagger 어노테이션은 ADM Batch와 XYZ EDU Batch에 반영했습니다.
- 앱을 기동해서 `/v3/api-docs`, `/swagger-ui.html`, `/swagger-ui/index.html`을 호출하는 검증은 미검증입니다.

## 10. 배치관리 보강 결과

- Spring Batch 표준 `BATCH_*` 테이블을 설치 SQL과 Flyway baseline에 추가했습니다.
- `pfw_batch_schedule`에 `calendar_id`, `business_day_only_yn`, `holiday_policy`, `available_start_time`, `available_end_time`, `run_date_pattern`을 추가했습니다.
- `pfw_batch_job_relation`으로 선행/후행/트리거 관계를 표현했습니다.
- `pfw_batch_execution_target`으로 수행 대상/대기 인스턴스 기준을 추가했습니다.
- ADM API/UI와 EDU 설명 API를 연결했습니다.

## 11. 알림/노티 보강 결과

- `pfw_notification_rule`, `pfw_notification_delivery_log`를 추가했습니다.
- 배치 실패와 보안 이벤트용 seed를 추가했습니다.
- ADM 알림 관리 화면/API는 아직 미구현입니다.

## 12. JWT/OAuth/Security 책임 분리 결과

- 이번 변경은 보안 모듈 구조를 크게 바꾸지 않았습니다.
- 관리자 인증/운영 보안은 ADM, 공통 보안 유틸은 CMN/PFW 기준으로 유지하는 방향을 문서에 남겼습니다.
- TOTP QR/복구코드, OAuth provider 연동은 미구현입니다.

## 13. 테스트 / qualityGate 결과

- `.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline`: 성공.
- `.\gradlew.bat test --offline`: 성공. 단, `acc`, `adm`, `pfw`, `xyz` 테스트는 Gradle 설정상 `SKIPPED` 또는 `NO-SOURCE`로 표시되었습니다.
- `.\gradlew.bat qualityGate --offline`: 최종 성공.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-utf8.ps1 -CheckMojibake`: 성공.
- `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-sql-standard.ps1`: 성공.
- `git diff --check`: whitespace 오류 없음. 단, SQL 합본 파일은 Git 경고로 CRLF가 LF로 변환될 수 있다고 표시되었습니다.

## 14. 문서/EDU 샘플 정합성 결과

- `README.md`를 짧은 진입점으로 정리했습니다.
- `specs/index.html`에서 핵심 가이드와 보조 표준 문서를 분리했습니다.
- 개발/관리자/프레임워크 구성/SQL 가이드에 Spring Batch 저장소와 CPF 배치 운영 메타 차이를 반영했습니다.
- `XyzBatchEducationController`에 배치 스케줄 정책 EDU 설명을 추가했습니다.

## 15. 실행한 명령과 결과

- `git status`: 실패. sandbox 사용자와 저장소 소유자 차이로 dubious ownership 발생.
- `git -c safe.directory=... status --short`: 성공.
- `git -c safe.directory=... log --oneline -5`: 성공.
- `rg ... -g "build.gradle"` 이전 wildcard 검색: 실패. Windows wildcard 경로 구문 문제.
- Gradle compile/test/qualityGate: 최종 성공.
- MariaDB `00_all_install_and_smoke.sql`: 2회 성공.
- information_schema FK 조회 1차: 실패. `REFERENTIAL_CONSTRAINTS.TABLE_SCHEMA` 컬럼 오사용.
- information_schema FK 조회 2차: 성공.
- `git diff --check` 1차: 실패. 합본 SQL EOF 빈 줄.
- `git diff --check` 2차: 성공. CRLF/LF 경고만 남음.

## 16. 남은 리스크

- Swagger/OpenAPI JSON은 앱 기동 후 검증하지 않았습니다.
- ADM UI는 브라우저 클릭 검증을 수행하지 않았습니다.
- Spring Batch JobRepository는 테이블 설치까지 검증했지만, 앱에서 실제 Job 실행 후 `BATCH_JOB_EXECUTION` 적재는 미검증입니다.
- 전체 ADM 다운로드 기능과 다운로드 감사 로그는 아직 미완성입니다.
- 알림/노티 ADM 관리 화면/API는 아직 미완성입니다.
- VS Code Problems 직접 확인은 미검증입니다.

## 17. 보류 항목

- Redis/Kafka/RabbitMQ 실 broker 테스트.
- TOTP QR/복구코드까지 포함한 OTP 완성.
- ADM 전체 다운로드 기능.
- OpenAPI JSON 품질 gate.
- 배치 관계 그래프 시각화.

## 18. Codex가 판단한 추가 보강 의견

1. ADM 배치 관제에서 관계 조회 결과를 그래프 형태로 보여주는 UI가 필요합니다.
2. `pfw_batch_execution_target`에 등록/배정/취소 API를 추가하면 운영자가 수행 대기 상태를 더 명확히 관리할 수 있습니다.
3. 다운로드 기능은 공통 download policy interceptor와 `adm_download_audit_log`로 한 번에 묶는 편이 좋습니다.
4. OpenAPI JSON 검증은 앱 기동 자동화와 함께 CI gate로 올리는 것이 좋습니다.
5. `git diff`의 CRLF/LF 경고는 `.gitattributes` 기준과 합본 생성기의 줄바꿈 정책을 한 번 더 맞추는 것이 좋습니다.

## 19. 다음 Codex 작업 추천

1. ADM 전체 목록/로그/이력 다운로드 공통 기능과 감사 로그 구현.
2. ADM 알림/노티 관리 화면/API 구현.
3. Spring Batch 실제 앱 기동 후 Job 실행, `BATCH_*` 적재, ADM 상세 연동 검증.
4. Swagger 앱 기동 검증과 OpenAPI JSON 품질 gate 추가.
5. 배치 관계 그래프 UI와 수행 대상 등록/배정 API 보강.
6. VS Code Problems 기준 null/static warning 전수 정리.

## 20. 추천 커밋 메시지

```text
stabilize cpf batch repository and adm batch operations
```

## 21. 허위 보고 방지 체크

- 실행하지 않은 Swagger 앱 기동 검증은 미검증으로 표시했습니다.
- 실행하지 않은 VS Code Problems 확인은 미검증으로 표시했습니다.
- 실패한 명령을 15번 섹션에 기록했습니다.
- MariaDB 비밀번호와 계정 password hash는 기록하지 않았습니다.
- 커밋하지 않았습니다.

