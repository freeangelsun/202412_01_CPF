# CPF_M5_M6_BIG_PROGRESS_REQUEST — EDU/XYZ 실전 샘플 확대 / ADM 검증 보강 / CMN 고정길이 전문 Skeleton 착수

## 0. CPF 최종 목표

이번 작업은 `CPF_FINAL_TARGET_REQUIREMENTS.md`를 최상위 목표 기준서로 따른다.

`CPF_FINAL_TARGET_REQUIREMENTS.md`는 전체 목표 기준서이며, 이번 작업에서 모든 항목을 구현하라는 범위 문서가 아니다. Codex는 이번 요청서 범위를 우선 수행하되, 구현 과정에서 CPF 최종 목표와 충돌하지 않도록 설계한다.

이번 요청은 개발 진도를 빠르게 내기 위해 평소보다 넓게 구성한다.
단, 필수 완료 범위를 먼저 닫고, 보강/착수 범위는 가능 범위에서 진행한다. 완료하지 못한 항목은 완료로 포장하지 말고 `부분 구현`, `미구현`, `미검증`, `재확인 필요`로 기록한다.

---

## 1. 이번 작업 목적

이번 작업 목적은 세 가지다.

```text id="1qqbb2"
1. EDU/XYZ 실전 샘플을 실제 개발자가 복사해 쓸 수 있는 수준으로 확대한다.
2. ADM Center-Cut 관제의 남은 검증을 가능한 범위에서 보강한다.
3. CMN 고정길이 전문 처리의 최소 skeleton을 착수한다.
```

이번 작업의 우선순위는 아래다.

```text id="4kcm5h"
필수 완료:
- EDU/XYZ 실전 샘플 CRUD/paging/search/sort/validation/mapper fixture 보강
- Facade/outbound/header propagation 예제 추가
- 관련 test/smoke 추가 또는 보강

보강 범위:
- ADM Center-Cut browser click 가능 여부 확인
- ADM permission runtime read 권한 smoke 보강
- smoke-adm-runtime 실행 전 bootJar 최신성 보장

착수 범위:
- CMN 고정길이 전문 LayoutSpec/FieldSpec/parser/formatter skeleton
```

---

## 2. 필수 제한

```text id="ca1foo"
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증 완료 기록 금지
별도 변경파일 목록 산출물 생성 금지
문서 포맷 작업만으로 기능 완료 처리 금지
HTML 문서 신규 작성 금지
HTML 문서 직접 수정 지양
현재 마일스톤 목적과 무관한 대규모 리팩터링 금지
```

`CPF_NEW_REQUEST.md`는 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.
단, 이번 작업 시작 시 `CPF_NEW_REQUEST.md`가 최신 commit에서 변경 파일로 잡힌 이유를 `CPF_STABILIZATION_REPORT.md`에 재확인 기록한다.

---

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래를 확인하고 `CPF_STABILIZATION_REPORT.md`에 기록한다.

```powershell id="u8j13y"
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

확인할 내용:

```text id="hs0wob"
1. 최신 commit SHA
2. 작업 시작 전 modified/untracked 파일
3. CPF_NEW_REQUEST.md 변경 상태와 사유
4. 이전 작업에서 남은 미검증 항목
```

---

## 4. 필수 완료 범위 — EDU/XYZ 실전 샘플 확대

### 4.1 현재 EDU/XYZ 구조 확인

먼저 현재 XYZ/EDU 구조를 실제 파일 기준으로 확인한다.

확인 대상 후보:

```text id="3ufxz1"
xyz/src/main/java/cpf/xyz/edu/controller/*
xyz/src/main/java/cpf/xyz/edu/service/*
xyz/src/main/java/cpf/xyz/edu/repository/*
xyz/src/main/java/cpf/xyz/edu/mapper/*
xyz/src/main/java/cpf/xyz/edu/dto/*
xyz/src/main/resources/mapper/**
xyz/src/test/**
specs/sql/40_business_modules_schema.sql
specs/sql/70_test_data.sql
specs/sql/99_smoke_check.sql
```

확인 기준:

```text id="o3cdu4"
1. CRUD가 실제 DB/Mapper와 연결되어 있는지
2. offset paging이 있는지
3. keyset paging이 있는지
4. 검색/정렬 whitelist가 있는지
5. DTO validation이 있는지
6. Mapper/XML/SQL fixture가 있는지
7. Facade 연동 예제가 있는지
8. outbound/header propagation 예제가 있는지
9. 권한/마스킹/감사 위치가 설명 또는 코드로 드러나는지
```

### 4.2 CRUD / 상태 변경

EDU/XYZ에 실전형 CRUD 예제를 보강한다.

필수 API 후보:

```text id="8981bx"
GET /xyz/edu/items
GET /xyz/edu/items/{itemId}
POST /xyz/edu/items
PUT /xyz/edu/items/{itemId}
PATCH /xyz/edu/items/{itemId}/status
DELETE /xyz/edu/items/{itemId} 또는 logical delete
```

주의:

```text id="9xhlu1"
1. 실제 기존 path 규칙이 있으면 기존 규칙을 우선한다.
2. Controller만 만들고 Service/Repository/Mapper/SQL이 없으면 완료가 아니다.
3. 삭제는 물리 삭제보다 logical delete 또는 상태 변경 예제를 우선한다.
```

### 4.3 Paging / Search / Sort

목록 API는 아래를 포함한다.

```text id="n34ovo"
offset paging
keyset paging
검색 조건
날짜 범위 조건
상태 조건
sort whitelist
default sort
page size 제한
```

완료 기준:

```text id="eqdlqp"
무제한 목록 조회가 없어야 한다.
sort field를 사용자 입력 그대로 SQL에 붙이면 안 된다.
```

### 4.4 Validation

DTO validation을 적용한다.

필수 후보:

```text id="1fp8xd"
필수값
문자 길이
상태 코드 값
날짜 범위
금액/수량 값
page size 상한
sort field whitelist
```

오류는 CPF 공통 오류/validation 응답 구조와 충돌하지 않게 한다.

### 4.5 Mapper / SQL fixture / DB slice

가능하면 MariaDB DB slice test를 실행한다.

후보:

```text id="0vwc56"
XyzQueryEducationMapperSliceTest
xyz_edu_query_fixture.sql
```

기존에 `edu-mapper-db-slice`가 미검증으로 남아 있으므로 이번에 가능한 범위에서 닫는다.

실행 조건:

```text id="rgo7kp"
CPF_XYZ_EDU_MAPPER_DB_USERNAME
CPF_XYZ_EDU_MAPPER_DB_PASSWORD
CPF_XYZ_EDU_MAPPER_DB_USER legacy 호환 여부
```

실행 불가 시:

```text id="nbv8ro"
미검증으로 기록하고 이유를 남긴다.
H2/mock DB 성공을 MariaDB DB slice 완료로 기록하지 않는다.
```

### 4.6 Facade / Outbound / Header Propagation 예제

EDU/XYZ에 아래 예제를 최소 1개 이상 추가한다.

```text id="663t56"
1. 다른 주제영역 Facade 호출 예제
2. CpfRestClient 또는 CpfWebClient outbound 호출 예제
3. 표준 헤더/확장 헤더 propagation 예제
4. outbound 실패 시 timeout/retry/logging 위치 설명
```

주의:

```text id="3hs0uu"
mbr/exs 테이블 직접 JOIN 금지
for-loop 단건 반복 호출 금지
표준 헤더 수동 하드코딩 금지
CpfWebClient/CpfRestClient wrapper 사용
```

---

## 5. 보강 범위 — ADM Center-Cut 검증 보강

### 5.1 Browser click 가능 여부 확인

Node/Playwright가 없으면 계속 밀어붙이지 않는다.

확인 명령 후보:

```powershell id="q7f4fz"
node --version
npx --version
```

가능하면 실행:

```powershell id="smy696"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-ui.ps1 -BrowserClick
```

불가능하면:

```text id="gj1qdu"
Node/Playwright 없음으로 미검증 유지
API smoke와 정적 marker smoke는 완료/부분 완료로 분리
```

### 5.2 ADM permission runtime read 권한 smoke

Center-Cut read 권한이 `BATCH_READ`로 매핑되었으므로, 가능한 범위에서 permission runtime smoke를 보강한다.

확인 후보:

```text id="gc1ymy"
1. ADM_VIEWER 또는 read-only 권한으로 GET /adm/api/center-cut/jobs 허용
2. write 권한이 필요한 API는 이번 범위에서는 없음
3. 권한 실패 시 표준 오류 응답 확인
```

실행하지 못하면 미검증으로 유지한다.

### 5.3 smoke-adm-runtime bootJar 최신성 보장

이전 작업에서 기존 bootJar가 새 Controller를 포함하지 않아 첫 runtime smoke가 실패했다.
이번에 `smoke-adm-runtime.ps1` 또는 요청서 실행 순서에서 bootJar 최신성 보장을 반영한다.

후보:

```text id="802jr1"
1. smoke 실행 전 :adm:bootJar 실행 옵션 추가
2. 스크립트에 -BuildBeforeRun 옵션 추가
3. 최소한 리포트에 bootJar 최신성 확인 절차 기록
```

---

## 6. 착수 범위 — CMN 고정길이 전문 Skeleton

이번 작업에서 CMN 고정길이 전문 전체 구현을 완료하려 하지 않는다.
다만 다음 마일스톤 진입을 위해 skeleton을 착수한다.

### 6.1 패키지 후보

```text id="snf7fs"
cmn/src/main/java/cpf/cmn/message/fixedlength
```

### 6.2 최소 클래스 후보

```text id="k9vtqj"
FixedLengthLayoutSpec
FixedLengthFieldSpec
FixedLengthFieldType
FixedLengthAlignment
FixedLengthParseResult
FixedLengthFormatResult
FixedLengthMessageParser
FixedLengthMessageFormatter
FixedLengthMaskingRule
```

### 6.3 최소 기능

```text id="fw4r25"
1. LayoutSpec에 charset, totalLength, fields 정의
2. FieldSpec에 name, start, length, type, required, padding, alignment, sensitive 여부 정의
3. Parser는 byte length 기준 skeleton
4. Formatter는 padding/alignment 기준 skeleton
5. 민감 field masking 후보 구조
6. parse-format round trip 단위 테스트 최소 1개
```

주의:

```text id="sofwde"
substring index 하드코딩만 있는 구조 금지
char length 기준 파싱으로 고정 금지
parser만 있고 formatter가 없으면 완료 아님
이번 범위에서는 skeleton 또는 부분 구현으로 기록 가능
```

---

## 7. SQL / Flyway / all_install 기준

DB 변경이 있으면 아래를 함께 반영한다.

```text id="7317te"
split SQL
Flyway
00_all_install.sql
00_all_install_and_smoke.sql
99_smoke_check.sql
test fixture
```

한 경로에만 있으면 완료가 아니다.

이번 작업에서 SQL 변경이 없다면 SQL 변경 없음으로 리포트에 기록한다.

---

## 8. 실행 검증 요구사항

가능한 범위에서 아래를 실행한다.

```powershell id="bz2cqg"
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat :cmn:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain

powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake

.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

가능하면 추가 실행:

```powershell id="q87b2v"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-ui.ps1 -BrowserClick
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-permission-runtime.ps1
```

DB slice가 가능하면:

```powershell id="99ps0e"
.\gradlew.bat :xyz:test --tests *XyzQueryEducationMapperSliceTest --offline --no-daemon --console=plain
```

실행하지 못한 검증은 미검증으로 기록한다.

---

## 9. 리포트 / 기능 매트릭스 반영

`CPF_STABILIZATION_REPORT.md`와 기능 매트릭스에 아래를 최소 반영한다.

```text id="ret1ky"
1. EDU/XYZ CRUD/paging/search/sort/validation 구현 여부
2. Mapper/XML/SQL fixture 반영 여부
3. DB slice 실행 여부
4. Facade/outbound/header propagation 예제 구현 여부
5. ADM browser click 가능 여부와 실행 여부
6. ADM permission runtime 실행 여부
7. smoke-adm-runtime bootJar 최신성 보장 여부
8. CMN 고정길이 전문 skeleton 착수 여부
9. 실행한 검증과 미실행 검증의 분리
```

문서 정본화, HTML 디자인, 가이드 장문 보강은 이번 범위가 아니다.

---

## 10. 완료 기준

아래 필수 범위가 충족되어야 이번 작업 완료로 본다.

```text id="936d52"
1. EDU/XYZ CRUD 또는 상태 변경 예제가 Controller-Service-Repository/Mapper-SQL-Test로 연결됨
2. 목록 API에 offset 또는 keyset paging이 있음
3. 검색/정렬 whitelist가 있음
4. DTO validation이 있음
5. Mapper/XML/SQL fixture 또는 repository test가 있음
6. Facade/outbound/header propagation 예제가 최소 1개 있음
7. 관련 Gradle test가 통과함
8. 리포트와 기능 매트릭스 상태값이 실제 파일과 일치함
9. 실행하지 않은 검증은 미검증으로 기록함
10. Git commit / push / branch 생성 없음
```

보강/착수 범위는 완료하지 못해도 전체 실패로 보지 않는다. 단, 완료로 포장하지 않는다.

---

## 11. 완료 불인정 기준

```text id="95b48g"
1. Controller만 있고 Service/Repository/Mapper/SQL이 없음
2. 무제한 목록 조회
3. sort field를 사용자 입력 그대로 SQL에 붙임
4. mbr/exs 테이블 직접 JOIN
5. CpfWebClient/CpfRestClient 없이 표준 헤더 수동 하드코딩
6. H2/mock DB 성공을 MariaDB DB slice 완료로 기록
7. browser click 미실행인데 완료 기록
8. CMN 전문 skeleton만 만들고 전체 전문 처리 완료로 기록
9. 리포트와 기능 매트릭스 상태 불일치
10. 민감정보 원문 기록
11. Git commit / push / branch 생성
12. 별도 변경파일 목록 산출물 생성
```

---

## 12. 완료 보고 양식

```text id="5o0y98"
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- CPF_NEW_REQUEST.md 변경 상태/사유:

2. EDU/XYZ 실전 샘플
- CRUD:
- paging:
- keyset:
- search/sort:
- validation:
- mapper/repository:
- SQL/fixture:
- test:
- facade/outbound/header propagation:

3. ADM 검증 보강
- browser click 환경 확인:
- browser click 실행:
- permission runtime:
- bootJar 최신성 보장:

4. CMN 고정길이 전문 skeleton
- LayoutSpec:
- FieldSpec:
- Parser:
- Formatter:
- Masking:
- Test:

5. 실행 검증
- :xyz:test:
- :cmn:test:
- :adm:test:
- 전체 test:
- DB slice:
- browser click:
- permission runtime:
- check-sql-standard:
- check-feature-evidence:
- check-html-docs:
- check-utf8:
- qualityGate:

6. 상태값 반영
- EDU/XYZ 실전 샘플:
- edu-mapper-db-slice:
- ADM browser click:
- ADM permission runtime:
- CMN fixed-length skeleton:
- CMN fixed-length full parser/formatter:

7. 남은 작업
- EDU/XYZ 권한/마스킹/감사 고도화:
- CMN 전문 처리 완성:
- EXS 전문 송수신:
- Redis/Kafka/MQ:
- 최종 문서 정본화:
```

---

## 13. 다음 마일스톤 분기

이번 작업이 완료되면 다음은 아래 중 하나로 간다.

```text id="dy6az7"
1. CMN 고정길이 전문 parser/formatter 완성
2. EXS 전문 송수신 연계
3. EDU/XYZ 권한/마스킹/감사/outbound 고도화
4. ADM 운영자 조치/2인 승인 기능
```

분기 기준:

```text id="y7l1nl"
1. EDU/XYZ 실전 샘플이 부족하면 EDU/XYZ 보강 먼저 진행
2. EDU/XYZ가 닫히면 CMN 고정길이 전문 완성으로 진행
3. CMN 전문 skeleton이 어느 정도 잡히면 EXS 송수신으로 연결
4. browser click 환경이 계속 없으면 API/OpenAPI smoke까지만 닫고 기능 개발 전진
```
