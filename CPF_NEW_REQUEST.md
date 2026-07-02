# CPF_M10_0_M9_PREP_REQUEST — 신규 MariaDB full install 실검증 / 표준 헤더 Runtime E2E 진입점 구축 요청

## 0. CPF 최종 목표

CPF(CoreFlow Platform Framework)는 금융권을 포함한 범용 업무 시스템에서 실제 프로젝트에 재사용 가능한 운영형 공통 프레임워크다.

이번 작업은 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 최상위 목표 기준서로 따른다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 이번 작업에서 전체를 구현하라는 범위 문서가 아니다.
이번 요청서는 그 기준서 아래에서 신규 MariaDB 설치 검증과 표준 헤더 Runtime E2E 검증 진입점을 닫기 위한 마일스톤 작업이다.

최종 목표 기준은 아래를 포함한다.

```text id="uv7n47"
1. 실행하지 않은 검증을 완료로 기록하지 않는다.
2. Source-Level 테스트와 Runtime E2E를 구분한다.
3. 기존 개발 DB 확인과 신규 빈 MariaDB 설치 검증을 구분한다.
4. 정적 UI marker smoke와 browser click 검증을 구분한다.
5. embedded/mock broker와 실 Redis/Kafka/MQ 검증을 구분한다.
6. 모든 기능은 확장성/변경 가능성을 고려해 설계한다.
7. PFW, CMN, ADM, BAT, EXS, MBR, BIZADM, EDU/XYZ 모듈 책임을 분리한다.
```

---

## 1. 이번 작업 목적

이번 작업 목적은 두 가지다.

```text id="voa7mn"
1. 신규 빈 MariaDB 기준으로 CPF 전체 설치 SQL을 실제 실행 검증한다.
2. 표준 헤더 Runtime E2E 검증을 위한 smoke 진입점을 구축한다.
```

이번 작업은 center-cut/EXS/EDU 기능을 크게 확장하는 작업이 아니다.
M4 보정으로 변경된 `bat_center_cut_*`, V17 migration, all_install, seed, smoke SQL이 신규 DB에서 실제로 동작하는지 확인하는 것이 우선이다.

동시에 표준 헤더 Runtime E2E를 다음 작업에서 완성할 수 있도록 최소한의 실행 스크립트, mock downstream, 검증 결과 포맷을 준비한다.

---

## 2. 필수 제한

아래는 반드시 지킨다.

```text id="yf1o4y"
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증을 완료로 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
HTML 문서 신규 작성 금지
HTML 문서 직접 수정 지양
```

`CPF_NEW_REQUEST.md`는 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 최상위 목표 기준서로 참조하되, 이번 작업에서 전체 항목을 구현하지 않는다.

---

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래를 확인하고 `CPF_STABILIZATION_REPORT.md`에 기록한다.

```powershell id="jw2gag"
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

기록할 내용:

```text id="kyyn65"
작업 시작 branch
작업 시작 HEAD SHA
작업 시작 origin/master SHA
작업 시작 status --short
최근 commit
사용자 요청 원본 파일 여부
이번 작업에서 의도적으로 수정한 파일
기존 수정 상태로 남아 있던 파일
```

---

## 4. MariaDB full install 실검증

### 4.1 목적

신규 빈 MariaDB 환경에서 CPF 설치 SQL이 실제로 동작하는지 확인한다.

검증 대상:

```text id="h818o6"
specs/sql/00_all_install.sql
specs/sql/00_all_install_and_smoke.sql
specs/sql/99_smoke_check.sql
specs/sql/35_bat_schema.sql
specs/sql/migration/flyway/V17__batch_center_cut_ownership_repair.sql
```

특히 이번 작업에서는 아래를 확인한다.

```text id="ml9nqm"
1. 신규 빈 MariaDB에 DB 생성 가능 여부
2. 전체 테이블 생성 가능 여부
3. seed data 삽입 가능 여부
4. smoke SQL 실행 가능 여부
5. bat_center_cut_* 테이블 생성 여부
6. CPF_BAT_CENTER_CUT_JOB seed 여부
7. pfw_center_cut_* 테이블이 all_install 최종 기준에 남지 않는지
8. FK/index/unique key 오류 여부
9. 중복 실행 시 idempotent 가능 범위
```

### 4.2 MariaDB 실행 방법

먼저 로컬에서 `mysql` 또는 `mariadb` CLI를 찾는다.

```powershell id="dkhfvq"
where.exe mysql
where.exe mariadb
mysql --version
mariadb --version
```

CLI가 PATH에 없으면 아래를 확인한다.

```powershell id="b3ixgf"
1. MariaDB 설치 경로 탐색
2. Docker 사용 가능 여부 확인
3. docker-compose.local.yml 사용 가능 여부 확인
4. 프로젝트 내 scripts에 MariaDB 실행 보조 스크립트가 있는지 확인
```

가능하면 Docker 또는 로컬 MariaDB 중 하나로 신규 검증 DB를 준비한다.

주의:

```text id="aaqwto"
1. 실제 MariaDB 실행이 불가능하면 완료로 기록하지 않는다.
2. CLI가 없거나 Docker가 불가능하면 미검증으로 기록하고 사유를 남긴다.
3. H2나 mock DB 성공을 MariaDB full install 성공으로 기록하지 않는다.
4. 기존 개발 DB 일부 테이블 확인을 신규 빈 MariaDB full install로 기록하지 않는다.
```

### 4.3 검증 스크립트 후보

기존 스크립트를 보강하거나 신규 스크립트를 만든다.

후보 이름:

```text id="yjc99m"
scripts/smoke-mariadb-full-install.ps1
```

스크립트 요구사항:

```text id="6h9wef"
1. MariaDB 접속 정보는 parameter 또는 환경변수로 받는다.
2. 비밀번호를 소스에 하드코딩하지 않는다.
3. 실행 결과를 build/runtime-smoke 또는 build/sql-smoke 아래 JSON/LOG로 저장한다.
4. 실패 시 실패 SQL 파일, 실패 단계, 오류 메시지를 기록한다.
5. 성공 시 실행한 SQL 파일과 주요 검증 결과를 기록한다.
```

환경변수 후보:

```text id="cbnn00"
CPF_DB_HOST
CPF_DB_PORT
CPF_DB_ROOT_USERNAME
CPF_DB_ROOT_PASSWORD
CPF_DB_MIGRATION_USERNAME
CPF_DB_MIGRATION_PASSWORD
```

기본값이 필요하면 username/host/port 정도만 허용하고, password fallback은 두지 않는다.

---

## 5. SQL 정합성 확인

아래 정합성을 재확인한다.

```text id="95p87u"
1. split SQL과 00_all_install.sql 정합성
2. 00_all_install.sql과 00_all_install_and_smoke.sql 정합성
3. Flyway migration과 split SQL의 현재 기준 차이 기록
4. V16/V17 center-cut migration 순서
5. bat_center_cut_* 테이블의 all_install 반영
6. smoke SQL의 bat_center_cut_* 조회 반영
7. seed data의 CPF_BAT_CENTER_CUT_JOB 반영
```

명령 후보:

```powershell id="zyjo2o"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all-install-sql.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
```

---

## 6. 표준 헤더 Runtime E2E smoke 진입점 구축

### 6.1 목적

이번 작업에서 표준 헤더 Runtime E2E를 완전히 닫는 것이 목표는 아니다.
다만 다음 작업에서 실제 Runtime E2E를 실행할 수 있도록 진입점을 만든다.

최종 Runtime E2E 목표는 아래 흐름이다.

```text id="x7rxep"
1. 실제 API 호출
2. 표준 헤더 수신
3. 필수 헤더 검증
4. X-Cpf-Ext-* 확장 헤더 수신
5. TransactionContext 생성
6. 거래 로그/header snapshot 저장
7. outbound 호출
8. mock downstream 서버에서 표준/확장 헤더 수신 확인
9. ADM 또는 로그 조회 API에서 inbound/resolved/outbound/response 확인
10. 민감 헤더 원문 로그 금지 확인
```

### 6.2 이번 작업 범위

이번 작업에서는 아래 중 가능한 범위를 수행한다.

```text id="1n4ajz"
1. 표준 헤더 Runtime E2E smoke 스크립트 초안 추가
2. mock downstream 서버 또는 lightweight endpoint 준비
3. 표준 헤더/확장 헤더 요청 payload 샘플 준비
4. E2E 결과 JSON 포맷 정의
5. 아직 ADM 조회까지 연결하지 못하면 미검증 또는 부분 구현으로 기록
```

후보 스크립트 이름:

```text id="a313yc"
scripts/smoke-standard-header-e2e.ps1
```

결과 파일 후보:

```text id="y9uwg4"
build/runtime-smoke/standard-header-e2e-result.json
```

### 6.3 검증해야 할 헤더

필수 표준 헤더 후보:

```text id="3amcv8"
X-Transaction-Id
X-Request-Type
X-Original-Channel-Code
X-Channel-Code
X-Trace-Id
X-Client-App-Id
X-User-Id
X-Customer-No
X-Member-No
```

확장 헤더 후보:

```text id="gph4ux"
X-Cpf-Ext-1
X-Cpf-Ext-Campaign-Id
X-Cpf-Ext-Partner-Code
```

차단 확인 후보:

```text id="vxr958"
X-Cpf-Ext-Token
X-Cpf-Ext-Api-Key
X-Cpf-Ext-Authorization
```

민감 헤더 후보:

```text id="o2gemy"
Authorization
X-Api-Key
```

주의:

```text id="jkt1ur"
Authorization, X-Api-Key, token류는 원문 로그나 결과 JSON에 기록하지 않는다.
```

### 6.4 상태값 기준

이번 작업에서 실제 앱 기동, downstream mock, ADM 조회까지 성공하지 않으면 `standard-header-e2e`를 완료로 올리지 않는다.

상태 기준:

```text id="x34d0p"
스크립트 초안만 있음: 부분 구현
앱 기동 없이 source-level만 있음: 미검증
앱 기동 + API 호출 + downstream 수신까지 성공: 부분 구현 또는 완료 후보
ADM 조회까지 확인: 완료 가능
민감 헤더 원문 미저장 확인까지 포함: 완료 가능
```

---

## 7. ADM runtime / OpenAPI smoke 재확인 후보

이번 작업의 주목적은 MariaDB full install과 표준 헤더 E2E 진입점이다.
다만 시간이 허용되면 ADM runtime/OpenAPI smoke도 재확인한다.

후보 명령:

```powershell id="wxx5vv"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1
```

주의:

```text id="gq4qje"
1. ADM 앱을 기동하지 않고 smoke-openapi.ps1 단독 실행에 실패하면 실패로 기록한다.
2. ADM runtime smoke 내부 OpenAPI 성공과 단독 smoke-openapi 실패를 구분한다.
3. browser click은 이번 범위가 아니며 실행하지 않으면 미검증으로 유지한다.
```

---

## 8. 테스트 / 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell id="l5ozzo"
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat :bat:bootJar --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\build-all-install-sql.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

가능하면 추가 실행:

```powershell id="dx1wbg"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-mariadb-full-install.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-standard-header-e2e.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
```

실행하지 못한 검증은 미검증으로 기록한다.

---

## 9. 리포트 / 매트릭스 반영

`CPF_STABILIZATION_REPORT.md`와 기능 매트릭스에 아래를 반영한다.

```text id="qbt8np"
1. MariaDB full install 실행 여부
2. MariaDB 실행 방식
3. 사용한 CLI 또는 Docker 방식
4. 실행한 SQL 파일
5. 실패 SQL/단계/원인
6. bat_center_cut_* 테이블 생성 확인
7. CPF_BAT_CENTER_CUT_JOB seed 확인
8. smoke SQL 실행 결과
9. standard-header-e2e smoke 진입점 구축 여부
10. standard-header-e2e 실제 실행 여부
11. ADM runtime/OpenAPI 실행 여부
12. 실행하지 않은 검증의 미검증 사유
13. 실패한 검증의 실패 사유
```

상태값은 아래 6개만 사용한다.

```text id="99krea"
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

---

## 10. 완료 기준

아래가 충족되어야 이번 작업 완료로 본다.

```text id="e8c5cq"
1. MariaDB full install 실행 가능 여부를 실제로 확인함
2. 가능하면 신규 빈 MariaDB에 00_all_install_and_smoke.sql을 실제 실행함
3. bat_center_cut_* 테이블 생성 여부를 확인함
4. CPF_BAT_CENTER_CUT_JOB seed 여부를 확인함
5. 99_smoke_check.sql 실행 결과를 기록함
6. MariaDB 실행이 불가능하면 CLI/Docker/환경 사유를 명확히 기록하고 미검증으로 남김
7. 표준 헤더 Runtime E2E smoke 스크립트 또는 실행 진입점을 추가함
8. 표준/확장/차단/민감 헤더 검증 시나리오를 스크립트 또는 리포트에 정의함
9. standard-header Source-Level과 Runtime E2E 상태를 계속 분리함
10. 실행하지 않은 검증을 완료로 기록하지 않음
11. SQL standard, feature evidence, UTF-8, qualityGate 결과를 기록함
12. CPF_FINAL_TARGET_REQUIREMENTS.md 기준과 충돌하는 설계가 없음
13. Git commit / push / branch 생성 없음
```

---

## 11. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text id="7amf9i"
1. 기존 개발 DB 일부 확인을 신규 MariaDB full install 완료로 기록
2. H2/mock DB 성공을 MariaDB full install 완료로 기록
3. mysql/mariadb CLI가 없는데 full install 완료 기록
4. Docker/MariaDB 실행 실패를 숨김
5. 00_all_install.sql만 생성하고 실제 실행하지 않았는데 완료 기록
6. bat_center_cut_* 테이블 생성 확인 없이 SQL 정합성 완료 기록
7. 표준 헤더 Runtime E2E를 실행하지 않았는데 완료 기록
8. Source-Level 테스트를 Runtime E2E로 포장
9. 민감 헤더 원문을 result JSON이나 리포트에 기록
10. 실패한 smoke를 미검증으로 바꿔 기록
11. 기능 매트릭스와 CPF_STABILIZATION_REPORT.md 상태 불일치
12. Git commit / push / branch 생성
13. 별도 변경파일 목록 산출물 생성
```

---

## 12. 완료 보고 양식

```text id="bu1w2h"
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
- CPF_BAT_CENTER_CUT_JOB 확인:
- smoke SQL 결과:

3. 표준 헤더 Runtime E2E 진입점
- 스크립트:
- mock downstream:
- 검증 헤더:
- 차단 헤더:
- 민감 헤더 원문 기록 여부:
- 실제 실행 여부:
- 남은 미검증:

4. SQL 정합성
- build-all-install:
- check-sql-standard:
- split SQL:
- Flyway:
- all_install:
- smoke SQL:

5. 실행 명령 결과
- :pfw:test:
- :bat:test:
- :adm:test:
- 전체 test:
- :bat:bootJar:
- smoke-mariadb-full-install:
- smoke-standard-header-e2e:
- smoke-adm-runtime:
- check-feature-evidence:
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
- 표준 헤더 Runtime E2E 완성:
- ADM header snapshot 조회:
- center-cut 업무 adapter:
- EDU center-cut 샘플:
- ADM center-cut 관제:
- 브라우저 클릭:
- 실 broker:
```

---

## 13. 다음 마일스톤 분기

이번 작업이 닫히면 다음은 아래 중 하나로 간다.

```text id="469s4n"
1. M9 — 표준 헤더 Runtime E2E 완성
2. M4-2 — 업무 DB 기반 center-cut adapter + EDU/XYZ center-cut 샘플
3. M5 — EDU/XYZ 실전 샘플 완성
4. M6-1 — CMN 고정길이 전문 처리 공통 모듈
```

분기 기준:

```text id="2vl2xh"
1. MariaDB full install이 실패하면 DB/SQL 보정 먼저 진행
2. MariaDB full install이 성공하면 표준 헤더 Runtime E2E 완성으로 진행
3. 표준 헤더 Runtime E2E가 닫히면 center-cut 업무 adapter/EDU 샘플로 진행
4. center-cut adapter가 닫히면 EDU/XYZ 실전 샘플 또는 CMN 전문 처리로 진행
```
