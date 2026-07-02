# CPF_M10_1_M9_REQUEST — MariaDB full install 실실행 / 표준 헤더 Runtime E2E 완성 요청

## 0. CPF 최종 목표

이번 작업은 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 최상위 목표 기준서로 따른다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 이번 작업에서 전체를 구현하라는 범위 문서가 아니다.
이번 요청서는 그 기준서 아래에서 **신규 MariaDB full install 실실행**과 **표준 헤더 Runtime E2E 완성**을 목표로 한다.

이번 작업에서 특히 지킬 기준은 아래와 같다.

```text
1. 실행하지 않은 검증은 완료로 기록하지 않는다.
2. 기존 개발 DB 확인과 신규 빈 MariaDB 설치 검증을 구분한다.
3. Source-Level 테스트와 Runtime E2E를 구분한다.
4. 표준 헤더 Runtime E2E는 실제 API 호출, 로그 저장, ADM 조회, outbound 전파까지 확인해야 완료로 본다.
5. 민감 헤더 원문은 결과 JSON, 로그, DB, 리포트에 기록하지 않는다.
6. 모든 기능은 향후 확장성/변경 가능성을 고려한다.
```

---

## 1. 이번 작업 목적

이번 작업 목적은 두 가지다.

```text
1. `scripts/smoke-mariadb-full-install.ps1`을 실제 MariaDB 접속 정보로 실행해 신규 DB 설치 검증을 완료한다.
2. `scripts/smoke-standard-header-e2e.ps1`를 실제 runtime E2E 검증 수준으로 확장하고 실행한다.
```

이번 작업은 신규 업무 기능 개발이 아니다.
목표는 CPF 공통 기반 검증을 실제 실행으로 닫는 것이다.

---

## 2. 필수 제한

아래는 반드시 지킨다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증을 완료로 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
HTML 문서 신규 작성 금지
HTML 문서 직접 수정 지양
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
```

`CPF_NEW_REQUEST.md`는 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.

---

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래를 확인하고 `CPF_STABILIZATION_REPORT.md`에 기록한다.

```powershell
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

safe.directory 문제로 일부 git 명령이 실패하면 `.git` 파일 확인 기준으로 보조 기록하고, 실패 사유를 리포트에 남긴다.

---

## 4. MariaDB full install 실실행

### 4.1 목적

신규 빈 MariaDB 환경에서 CPF 설치 SQL이 실제로 실행되는지 확인한다.

검증 대상:

```text
specs/sql/00_all_install_and_smoke.sql
specs/sql/99_smoke_check.sql
specs/sql/50_framework_seed_data.sql
specs/sql/35_bat_schema.sql
specs/sql/migration/flyway/V17__batch_center_cut_ownership_repair.sql
```

### 4.2 실행 방식

기존 스크립트를 사용한다.

```powershell
$env:CPF_DB_HOST = "localhost"
$env:CPF_DB_PORT = "3306"
$env:CPF_DB_ROOT_USERNAME = "root"
$env:CPF_DB_ROOT_PASSWORD = "<로컬 환경변수로만 주입>"
$env:CPF_MARIADB_CLI = "C:\Program Files\MariaDB 12.3\bin\mariadb.exe"

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-mariadb-full-install.ps1 -RequireRun
```

주의:

```text
1. 비밀번호를 소스, 리포트, JSON, 로그에 원문 기록하지 않는다.
2. 비밀번호는 환경변수 또는 PowerShell parameter로만 주입한다.
3. `CPF_DB_ROOT_PASSWORD`가 없으면 미검증으로 기록한다.
4. H2/mock DB 성공을 MariaDB full install 성공으로 기록하지 않는다.
5. 기존 개발 DB 일부 확인을 신규 빈 MariaDB full install로 기록하지 않는다.
```

### 4.3 필수 확인

아래를 결과 JSON과 리포트에 기록한다.

```text
1. MariaDB CLI 경로
2. 실행 SQL 파일
3. 00_all_install_and_smoke.sql 실행 결과
4. 99_smoke_check.sql 실행 결과
5. 50_framework_seed_data.sql 반복 실행 결과
6. bat_center_cut_job 생성 여부
7. bat_center_cut_parameter 생성 여부
8. bat_center_cut_item 생성 여부
9. bat_center_cut_result 생성 여부
10. legacy pfw_center_cut_* 테이블 미존재 여부
11. CPF_BAT_CENTER_CUT_JOB seed 존재 여부
12. CPF_BAT_CENTER_CUT_JOB parameter seed 존재 여부
13. FK/index/unique 오류 여부
14. 반복 실행 시 idempotent 가능 범위
```

### 4.4 실패 시 처리

실패하면 실패로 기록하고 아래를 남긴다.

```text
1. 실패 SQL 파일
2. 실패 단계
3. 실패 메시지
4. 민감정보 제거 여부
5. 재실행 조건
```

실패를 미검증으로 바꾸지 않는다.

---

## 5. 표준 헤더 Runtime E2E 완성

### 5.1 목표

표준 헤더 Runtime E2E는 아래 흐름이 실제 runtime에서 연결되어야 완료로 볼 수 있다.

```text
1. 실제 API 호출
2. 표준 헤더 수신
3. 필수 헤더 검증
4. X-Cpf-Ext-* 확장 헤더 수신
5. 차단 확장 헤더 거부
6. TransactionContext 생성
7. 거래 로그 또는 header snapshot 저장
8. outbound 호출
9. mock downstream 서버에서 표준/확장 헤더 수신 확인
10. ADM 또는 로그 조회 API에서 inbound/resolved/outbound/response 확인
11. Authorization, X-Api-Key, token류 원문 미저장 확인
```

### 5.2 스크립트 보강

기존 스크립트를 확장한다.

```text
scripts/smoke-standard-header-e2e.ps1
```

필수 보강:

```text
1. 대상 앱 기동 상태 확인
2. 정상 표준/확장 헤더 요청
3. 차단 확장 헤더 요청
4. 민감 헤더 포함 요청
5. 응답 상태 코드 검증
6. mock downstream 수신 결과 검증
7. ADM 또는 로그 조회 API 호출
8. 결과 JSON에 각 단계별 상태 기록
9. 실패/미검증/부분 구현/완료 구분
10. -RequireRuntime 옵션에서 runtime probe 실패 시 실패 처리
```

### 5.3 mock downstream

표준 헤더 outbound 전파를 검증하려면 mock downstream이 필요하다.

가능한 방식 중 하나를 선택한다.

```text
1. 프로젝트 내 lightweight mock endpoint 추가
2. PowerShell 간이 HTTP listener 사용
3. 별도 Spring test/mock server 사용
4. 기존 smoke용 downstream endpoint 재사용
```

검증 항목:

```text
1. X-Transaction-Id 수신
2. X-Trace-Id 수신
3. X-Original-Channel-Code 수신
4. X-Channel-Code 수신
5. X-Cpf-Ext-1 수신
6. X-Cpf-Ext-Campaign-Id 수신
7. X-Cpf-Ext-Partner-Code 수신
8. Authorization 원문 미기록
9. X-Api-Key 원문 미기록
```

### 5.4 ADM 또는 로그 조회

가능한 경우 ADM API 또는 로그 조회 API로 아래를 확인한다.

```text
1. transactionGlobalId 기준 거래 로그 조회
2. inbound header snapshot 조회
3. resolved header snapshot 조회
4. outbound header snapshot 조회
5. response header snapshot 조회
6. 확장 헤더 조회
7. 민감 헤더 마스킹 여부
```

ADM API가 아직 부족하면 아래 중 하나로 처리한다.

```text
1. 필요한 최소 조회 API를 보강한다.
2. 기존 로그 조회 API를 활용한다.
3. DB 직접 조회 smoke로 임시 검증하되, ADM 조회 미연결은 부분 구현으로 남긴다.
```

### 5.5 완료 기준

아래가 모두 충족되어야 `standard-header-e2e`를 완료로 올릴 수 있다.

```text
1. 앱 runtime 기동
2. 정상 표준/확장 헤더 요청 성공
3. 차단 확장 헤더 요청 실패 확인
4. TransactionContext 생성 확인
5. 거래 로그/header snapshot 저장 확인
6. outbound mock downstream 수신 확인
7. ADM 또는 로그 조회 API에서 header snapshot 확인
8. 민감 헤더 원문 미저장 확인
9. 결과 JSON과 리포트에 증적 기록
```

하나라도 빠지면 `부분 구현` 또는 `미검증`으로 남긴다.

---

## 6. ADM runtime / OpenAPI smoke

표준 헤더 Runtime E2E와 연결되는 범위에서 ADM runtime과 OpenAPI smoke를 재확인한다.

후보 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1
```

기준:

```text
1. ADM 앱 기동 후 OpenAPI 접근 성공이면 openapi-runtime 완료 가능
2. 앱 미기동 상태에서 smoke-openapi.ps1 단독 실패는 실패로 기록
3. ADM runtime smoke 내부 OpenAPI와 단독 smoke-openapi 결과를 구분
4. browser click은 이번 범위가 아니며 실행하지 않으면 미검증으로 유지
```

---

## 7. 테스트 / 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat :bat:bootJar --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all-install-sql.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-mariadb-full-install.ps1 -RequireRun
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-standard-header-e2e.ps1 -RequireRuntime

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

실행하지 못한 검증은 미검증으로 기록한다.

---

## 8. 리포트 / 매트릭스 반영

`CPF_STABILIZATION_REPORT.md`와 기능 매트릭스에 아래를 반영한다.

```text
1. MariaDB full install 실제 실행 여부
2. MariaDB 실행 방식
3. 사용 CLI 또는 Docker 방식
4. 실행한 SQL 파일
5. 실패 SQL/단계/원인
6. bat_center_cut_* 테이블 생성 확인
7. CPF_BAT_CENTER_CUT_JOB seed 확인
8. 99_smoke_check.sql 실행 결과
9. 표준 헤더 Runtime E2E 실제 실행 여부
10. mock downstream 수신 결과
11. ADM/log 조회 결과
12. 민감 헤더 원문 미저장 확인
13. 실행하지 않은 검증의 미검증 사유
14. 실패한 검증의 실패 사유
```

상태값은 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

---

## 9. 완료 기준

아래가 충족되어야 이번 작업 완료로 본다.

```text
1. `smoke-mariadb-full-install.ps1 -RequireRun`을 실제 MariaDB 접속 정보로 실행함
2. `00_all_install_and_smoke.sql` 실제 실행 결과를 기록함
3. `99_smoke_check.sql` 실제 실행 결과를 기록함
4. `50_framework_seed_data.sql` 반복 실행 결과를 기록함
5. `bat_center_cut_*` 4개 테이블 생성 확인
6. legacy `pfw_center_cut_*` 테이블 미존재 확인
7. `CPF_BAT_CENTER_CUT_JOB` seed 확인
8. 표준 헤더 Runtime E2E에서 실제 앱 기동 후 요청 수행
9. 허용 확장 헤더 요청 성공 확인
10. 차단 확장 헤더 요청 실패 확인
11. mock downstream에서 outbound 표준/확장 헤더 수신 확인
12. ADM 또는 로그 조회 API에서 header snapshot 확인
13. 민감 헤더 원문 미저장 확인
14. 결과 JSON, 리포트, 기능 매트릭스 상태 일치
15. 실행하지 않은 검증은 완료로 기록하지 않음
16. qualityGate 통과
17. Git commit / push / branch 생성 없음
```

---

## 10. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
1. CPF_DB_ROOT_PASSWORD 없이 MariaDB full install 완료 기록
2. 기존 개발 DB 일부 확인을 신규 MariaDB full install 완료로 기록
3. H2/mock DB 성공을 MariaDB full install 완료로 기록
4. SQL 실행 실패를 미검증으로 바꿔 기록
5. 표준 헤더 Source-Level 테스트를 Runtime E2E 완료로 기록
6. 앱 미기동 상태에서 standard-header-e2e 완료 기록
7. mock downstream 수신 없이 outbound 전파 완료 기록
8. ADM/log 조회 없이 header snapshot 조회 완료 기록
9. 민감 헤더 원문을 result JSON, 로그, DB, 리포트에 기록
10. 실패한 smoke를 숨김
11. 기능 매트릭스와 CPF_STABILIZATION_REPORT.md 상태 불일치
12. Git commit / push / branch 생성
13. 별도 변경파일 목록 산출물 생성
```

---

## 11. 완료 보고 양식

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- 사용자 요청 원본 파일:

2. MariaDB full install
- 실행 방식:
- 사용 CLI/Docker:
- 실행 SQL:
- 결과:
- 실패 시 실패 단계:
- bat_center_cut_* 확인:
- legacy pfw_center_cut_* 확인:
- CPF_BAT_CENTER_CUT_JOB 확인:
- smoke SQL 결과:
- 비밀번호 원문 미기록 여부:

3. 표준 헤더 Runtime E2E
- 앱 기동 방식:
- 대상 URL:
- mock downstream:
- 허용 헤더 검증:
- 차단 헤더 검증:
- outbound 수신 검증:
- ADM/log 조회 검증:
- 민감 헤더 원문 미기록 여부:
- 결과 JSON:

4. ADM/OpenAPI
- ADM runtime:
- OpenAPI runtime:
- 단독 smoke-openapi:
- browser click:

5. 실행 명령 결과
- :pfw:test:
- :bat:test:
- :adm:test:
- 전체 test:
- :bat:bootJar:
- build-all-install:
- check-sql-standard:
- smoke-mariadb-full-install:
- smoke-standard-header-e2e:
- smoke-adm-runtime:
- check-feature-evidence:
- check-html-docs:
- check-utf8:
- qualityGate:

6. 상태값 반영
- mariadb-full-install:
- standard-header-e2e:
- adm-runtime:
- openapi-runtime:
- adm-browser-click:
- redis-kafka-mq-broker:

7. 남은 작업
- center-cut 업무 adapter:
- EDU center-cut 샘플:
- ADM center-cut 관제:
- CMN 고정길이 전문:
- EXS 전문 송수신:
- 브라우저 클릭:
- 실 broker:
```

---

## 12. 다음 마일스톤 분기

이번 작업이 닫히면 다음은 아래 중 하나로 간다.

```text
1. M4-2 — 업무 DB 기반 center-cut adapter + EDU/XYZ center-cut 샘플
2. M5 — EDU/XYZ 실전 샘플 완성
3. M6-1 — CMN 고정길이 전문 처리 공통 모듈
4. M8 — ADM 운영 콘솔 browser click/운영 UX 보강
```

분기 기준:

```text
1. MariaDB full install이 실패하면 DB/SQL 보정 먼저 진행
2. 표준 헤더 Runtime E2E가 실패하면 헤더/log/ADM/outbound 보정 먼저 진행
3. 둘 다 완료되면 center-cut adapter와 EDU 샘플로 진행
```
