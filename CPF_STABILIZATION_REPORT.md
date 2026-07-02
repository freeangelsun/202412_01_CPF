# CPF 안정화 작업 리포트

## 1. 기준 정보

- 요청 원본 파일: `CPF_NEW_REQUEST.md`
- 최종 목표 기준: `CPF_FINAL_TARGET_REQUIREMENTS.md`
- 작업 시작 branch: `master`
- 작업 시작 HEAD SHA: `ba250c3a81176b7f0c0aa1f4f0d4366eba566bbe`
- 작업 시작 origin/master SHA: `ba250c3a81176b7f0c0aa1f4f0d4366eba566bbe`
- 작업 시작 마지막 커밋: `ba250c3 20260702_02`
- 작업 종료 HEAD SHA: `ba250c3a81176b7f0c0aa1f4f0d4366eba566bbe`
- Git commit/push/branch 생성: 수행하지 않음
- 민감정보 원문 기록: 수행하지 않음
- 요청서 `CPF_NEW_REQUEST.md`: 작업 대상에서 제외, 사용자 갱신 파일로 보존

일반 `git branch --show-current`, `git rev-parse HEAD` 명령은 safe.directory 소유권 경고로 실패했습니다. 동일 저장소를 대상으로 `git -c safe.directory=D:/WORK_CPF/202412_01_CPF ...` 형식으로 기준 정보를 확인했습니다.

## 2. 수행 작업

1. `scripts/smoke-mariadb-full-install.ps1`를 Windows PowerShell 5.1과 MariaDB 12.3 CLI에서 안정적으로 동작하도록 보강했습니다.
2. MariaDB CLI 실행에 `--ssl=0`, UTF-8 설정, 환경변수 비밀번호 주입, SQL 파일 `SOURCE` 실행 방식을 적용했습니다.
3. `TransactionContext`가 `RequestContextHolder`가 아직 바인딩되지 않은 필터 초기 단계에서도 거래 헤더를 유지하도록 ThreadLocal fallback을 추가했습니다.
4. `CpfHeaderPropagatorTest`에 필터 초기 단계 헤더 유지 및 outbound 확장 헤더 전파 회귀 테스트를 추가했습니다.
5. ACC runtime E2E 전용 API `StandardHeaderE2eController`를 추가해 실제 API 호출에서 mock downstream으로 표준/확장 헤더를 전달하도록 했습니다.
6. `scripts/smoke-standard-header-e2e.ps1`를 실제 runtime E2E 검증으로 확장했습니다.
7. 기능 매트릭스 `specs/기능_구현_매트릭스.html`를 이번 검증 상태와 맞춰 갱신했습니다.

## 3. MariaDB full install

- 상태: 완료
- 실행 방식: MariaDB CLI
- CLI: `C:\Program Files\MariaDB 12.3\bin\mariadb.exe`
- 대상: `localhost:3306`
- 비밀번호 원문 기록 여부: 미기록
- 결과 JSON: `build/sql-smoke/mariadb-full-install-result.json`
- 실행 SQL:
  - `specs/sql/00_all_install_and_smoke.sql`
  - `specs/sql/99_smoke_check.sql`
  - `specs/sql/50_framework_seed_data.sql`

확인 결과:

- `00_all_install_and_smoke.sql`: 완료
- `99_smoke_check.sql`: 완료
- `50_framework_seed_data.sql` 반복 실행: 완료
- `bat_center_cut_*` 테이블 수: 4
- legacy `pfw_center_cut_*` 테이블 수: 0
- `CPF_BAT_CENTER_CUT_JOB` seed 수: 1
- center-cut parameter seed 수: 2

## 4. 표준 헤더 Runtime E2E

- 상태: 완료
- 실행 방식: ACC bootJar를 임시 runtime으로 기동 후 `scripts/smoke-standard-header-e2e.ps1 -RequireRuntime` 실행
- 결과 JSON: `build/runtime-smoke/standard-header-e2e-result.json`
- mock downstream capture: `build/runtime-smoke/standard-header-e2e-downstream.json`

확인 결과:

- 실제 API 호출: 완료
- 필수 표준 헤더 수신: 완료
- 허용 확장 헤더 수신: 완료
- 차단 확장 헤더 거부: 완료
- `TransactionContext` 생성: 완료
- mock downstream outbound 전파: 완료
- DB 로그 detail 조회: 완료
- `inboundHeaders`, `resolvedHeaders`, `outboundHeaders`, `responseHeaders` 저장: 완료
- `Authorization`, `X-Api-Key`, 차단 확장 헤더 원문 미저장: 완료

보강된 버그:

- 기존에는 필터가 가장 먼저 실행될 때 `RequestContextHolder`가 비어 있어 `TransactionHeader`가 request attribute에 저장되지 않았습니다.
- 이 때문에 거래 ID와 trace ID만 MDC로 남고, 채널/회원/고객/확장 헤더는 outbound와 DB detail snapshot에 누락되었습니다.
- `TransactionContext`에 ThreadLocal fallback을 추가해 필터 초기 단계부터 컨트롤러, 로그 aspect, outbound 전파까지 동일한 헤더 컨텍스트를 유지하도록 수정했습니다.

## 5. 검증 상태

| check id | 상태 | 증거 |
|---|---|---|
| edu-mapper-db-slice | 미검증 | 이번 작업 범위에서 실행하지 않음 |
| mariadb-full-install | 완료 | `scripts/smoke-mariadb-full-install.ps1`, `build/sql-smoke/mariadb-full-install-result.json` |
| adm-runtime | 미검증 | 이번 작업 범위에서 실행하지 않음 |
| adm-permission-runtime | 미검증 | 이번 작업 범위에서 실행하지 않음 |
| openapi-runtime | 미검증 | 이번 작업 범위에서 실행하지 않음 |
| adm-browser-click | 미검증 | 이번 작업 범위에서 실행하지 않음 |
| standard-header-e2e | 완료 | `scripts/smoke-standard-header-e2e.ps1`, `build/runtime-smoke/standard-header-e2e-result.json` |
| redis-kafka-mq-broker | 미검증 | 외부 broker 실연동은 이번 작업 범위가 아님 |
| quality-gate | 완료 | `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` |
| check-html-docs | 완료 | `scripts/check-html-docs.ps1` |
| check-feature-evidence | 완료 | `scripts/check-feature-evidence.ps1` |
| check-utf8 | 완료 | `scripts/check-utf8.ps1 -CheckMojibake` |

## 6. 실행 명령 결과

| 명령 | 결과 | 비고 |
|---|---|---|
| `.\gradlew.bat :pfw:test --offline --no-daemon --console=plain` | 완료 | 표준 헤더/컨텍스트 회귀 테스트 포함 |
| `.\gradlew.bat :acc:test --offline --no-daemon --console=plain` | 완료 | ACC E2E 컨트롤러 컴파일 포함 |
| `.\gradlew.bat test --offline --no-daemon --console=plain` | 완료 | 전체 모듈 테스트 통과 |
| `.\gradlew.bat :acc:bootJar --offline --no-daemon --console=plain` | 완료 | runtime E2E용 jar 생성 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1` | 완료 | SQL 표준 검사 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1` | 완료 | 증적 검사 통과 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1` | 완료 | 리포트/매트릭스 상태 일치 포함 |
| `powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake` | 완료 | UTF-8/mojibake 검사 통과 |
| `scripts/smoke-mariadb-full-install.ps1 -RequireRun` | 완료 | 실제 MariaDB 실행 |
| `scripts/smoke-standard-header-e2e.ps1 -RequireRuntime` | 완료 | 실제 ACC runtime 실행 |
| `.\gradlew.bat qualityGate --offline --no-daemon --console=plain` | 완료 | Gradle 품질 게이트 통과 |

## 7. 남은 리스크

- ADM runtime, OpenAPI runtime, ADM 브라우저 클릭은 이번 작업에서 실행하지 않았으므로 미검증입니다.
- Redis/Kafka/MQ 실제 broker 전파 검증은 외부 broker 환경이 필요해 이번 범위에서 제외했습니다.
- ACC jar runtime은 `/actuator/health`가 노출되지 않아 준비 확인을 실제 거래 API `/acc/tran/success`로 수행했습니다. health/readiness endpoint 정리는 다음 운영성 보강 후보입니다.
- EDU mapper DB slice는 이번 작업에서 실행하지 않았습니다. 관련 fixture는 `xyz_edu_query_fixture.sql`이고, 실행 시 DB 계정 환경변수는 `CPF_XYZ_EDU_MAPPER_DB_USERNAME`을 기준으로 사용합니다. 기존 호환 alias `CPF_XYZ_EDU_MAPPER_DB_USER`는 문서/스크립트 호환성 확인 대상입니다.
- 작업 종료 시 8080 listening 프로세스는 남아 있지 않습니다. ACC runtime 검증 중 생성된 `logs/` 산출물은 runtime 로그 보존 정책 대상이며 Git 변경 목록에는 포함되지 않았습니다.

미실행 검증:

- `scripts/build-all-install-sql.ps1`: 이번 작업에서 SQL 원본을 변경하지 않아 실행하지 않았습니다.
- `.\gradlew.bat :bat:bootJar --offline --no-daemon --console=plain`: 이번 작업의 직접 변경 범위가 ACC/PFW runtime E2E라 실행하지 않았습니다.
- `scripts/smoke-adm-runtime.ps1`, `scripts/smoke-openapi.ps1`: 이번 요청의 필수 완료 조건이 아니어서 실행하지 않았습니다.

## 8. 작업 종료 Git 상태

```text
 M CPF_NEW_REQUEST.md
 M CPF_STABILIZATION_REPORT.md
 M pfw/src/main/java/cpf/pfw/common/logging/TransactionContext.java
 M pfw/src/test/java/cpf/pfw/common/header/CpfHeaderPropagatorTest.java
 M scripts/smoke-mariadb-full-install.ps1
 M scripts/smoke-standard-header-e2e.ps1
 M specs/기능_구현_매트릭스.html
?? acc/src/main/java/cpf/acc/tst/controller/StandardHeaderE2eController.java
```

## 9. 다음 보강 후보

1. ADM runtime/OpenAPI/browser click smoke를 실제 앱 기동 기준으로 재검증.
2. 업무 DB 기반 center-cut adapter와 XYZ EDU center-cut 샘플 추가.
3. Redis/Kafka/MQ 실제 broker 전파 및 DB fallback 장애 시나리오 검증.
4. ACC/ADM/BAT runtime health/readiness/liveness endpoint 노출 기준 정리.
5. 최종 문서 정본 Markdown 세트와 HTML 배포본 생성 규칙 재정리.
