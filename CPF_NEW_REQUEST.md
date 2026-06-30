CPF_REWORK_REQUEST_20260630_04 — 상태값 자동 검출 강화 및 MariaDB 전체 설치 검증 준비 요청

## 1. 작업 목적

이번 작업의 목적은 직전 작업에서 대부분 정리된 반복 오류 방지 체계를 마무리하고, 다음 큰 단계인 MariaDB 신규 빈 DB 전체 설치 검증으로 넘어가기 위한 준비 상태를 만드는 것이다.

이번 작업은 신규 기능 개발이 아니다.

핵심 목적은 아래 3가지다.

1. `specs/기능_구현_매트릭스.html`의 상태값이 허용된 6개만 사용되는지 자동 검출한다.
2. `CPF_STABILIZATION_REPORT.html`과 기능 매트릭스의 주요 검증 상태가 불일치하지 않도록 점검 기준을 강화한다.
3. 다음 단계인 MariaDB 전체 설치 검증을 위해 SQL/Flyway/all_install/smoke 대상 파일 목록과 검증 절차를 리포트에 준비한다.

## 2. 이번 범위

이번 범위는 아래로 한정한다.

### 2.1 상태값 자동 검출 강화

대상:

* `scripts/check-html-docs.ps1`
* 필요 시 `scripts/check-feature-evidence.ps1`
* `specs/기능_구현_매트릭스.html`
* `CPF_STABILIZATION_REPORT.html`

요구사항:

* 기능 매트릭스 상태값은 아래 6개만 허용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

* 아래 표현은 상태값으로 사용하면 실패 처리한다.

```text
실환경 필요
완료 후보
확인 필요
진행중
보류
대기
작업중
성공
검토중
```

* 단, `실환경 필요` 같은 표현은 설명 문장에서는 허용할 수 있다.
* 상태값 칸 또는 상태값 marker에서만 금지해야 한다.
* 단순 전체 문자열 검색으로 설명 문장까지 무조건 실패 처리하지 말고, 기능 매트릭스의 상태값 위치를 기준으로 검출한다.
* HTML 구조가 복잡해서 상태값 칸 검출이 어렵다면, 기능 매트릭스에 `data-status="미검증"` 같은 명확한 marker를 추가하고 그 값을 검사한다.
* 이 경우 화면 표시 텍스트와 `data-status` 값이 서로 다르면 실패 처리한다.

권장 방식:

```html
<td class="status" data-status="미검증">미검증</td>
```

검출 기준:

* `data-status` 값은 허용된 6개 중 하나여야 한다.
* 상태 칸 표시 텍스트도 허용된 6개 중 하나여야 한다.
* `data-status`와 표시 텍스트가 다르면 실패한다.
* `data-status`가 없는 상태 칸이 있으면 실패한다.

완료 불인정:

* 기능 매트릭스에 금지 상태값이 남아 있음
* 설명 문장과 상태값을 구분하지 못해 정상 문장을 실패 처리
* 상태값 자동 검출을 구현했다고 보고했지만 실제 스크립트에서 허용 상태값 목록이 없음
* 기능 매트릭스는 정리했지만 스크립트가 향후 위반을 잡지 못함

---

### 2.2 리포트와 기능 매트릭스 주요 상태 불일치 검출

대상:

* `scripts/check-html-docs.ps1`
* 필요 시 별도 스크립트 추가 가능: `scripts/check-feature-status.ps1`
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`

요구사항:

아래 주요 검증 항목은 리포트와 기능 매트릭스가 같은 상태를 말해야 한다.

```text
EDU Mapper DB slice
MariaDB 전체 신규 설치
ADM runtime
OpenAPI runtime
ADM browser click
표준 헤더 E2E
Redis/Kafka/MQ broker
qualityGate
check-html-docs
check-feature-evidence
check-utf8
```

권장 방식:

* 기능 매트릭스에는 각 검증 항목에 `data-check-id`와 `data-status`를 둔다.
* 안정화 리포트에도 같은 `data-check-id`와 `data-status`를 둔다.
* 스크립트는 같은 `data-check-id`끼리 status가 같은지 비교한다.

예:

```html
<tr data-check-id="edu-mapper-db-slice">
  <td>EDU Mapper DB slice</td>
  <td class="status" data-status="재확인 필요">재확인 필요</td>
</tr>
```

완료 불인정:

* 리포트는 `완료`, 기능 매트릭스는 `미검증`
* 기능 매트릭스는 `재확인 필요`, 리포트는 `완료`
* check script가 불일치를 잡지 못함
* Codex 로컬 실행 성공을 ChatGPT 검증 완료로 표현

주의:

* Codex 로컬 MariaDB Mapper slice 성공은 기록 가능하다.
* 다만 ChatGPT 직접 실행 또는 재현 가능한 원격 evidence가 없으면 `완료`가 아니라 `재확인 필요`로 둔다.
* MariaDB Mapper fixture 성공을 MariaDB 전체 설치 성공으로 확대하지 않는다.

---

### 2.3 MariaDB 전체 설치 검증 준비

이번 작업에서 MariaDB 전체 설치를 실제 수행하지 않는다.
대신 다음 작업에서 바로 검증할 수 있도록 대상 파일과 절차를 정리한다.

대상 후보를 실제 repo에서 확인해서 `CPF_STABILIZATION_REPORT.html`에 기록한다.

확인 대상 예시:

```text
specs/sql/00_all_install.sql
specs/sql/00_all_install_and_smoke.sql
Flyway migration 파일
module별 split SQL
smoke SQL
fixture SQL
scripts/check-sql-standard.ps1
DB 표준 가이드
```

요구사항:

* 실제 존재하는 SQL/Flyway/smoke 파일 목록을 확인한다.
* 존재하지 않는 파일은 있다고 쓰지 않는다.
* 파일 목록은 `CPF_STABILIZATION_REPORT.html`에 “다음 MariaDB 전체 설치 검증 대상”으로 기록한다.
* 아직 실행하지 않은 것은 `미검증`으로 기록한다.
* 다음 단계에서 실행할 검증 명령 후보를 정리한다.
* MariaDB 신규 빈 DB 실실행은 이번 범위에서 제외한다.

완료 불인정:

* 실제 파일을 확인하지 않고 추정 목록 작성
* MariaDB 전체 설치를 실행하지 않았는데 완료로 기록
* Mapper fixture 성공을 전체 설치 성공으로 기록
* Flyway/all_install/smoke SQL 정합성을 확인하지 않고 성공 기록

## 3. 제외 범위

이번 작업에서는 아래를 하지 않는다.

* Git commit
* Git push
* branch 생성
* 신규 기능 개발
* MariaDB 신규 빈 DB 실제 설치
* ADM runtime 기동
* OpenAPI runtime 접근
* 브라우저 클릭 검증
* Redis/Kafka/MQ 실 broker 검증
* 문서 포맷 체계 전환
* Markdown/AsciiDoc/YAML 전환
* 대규모 문서 정본화
* 별도 변경파일 목록 산출물 생성

`CPF_STABILIZATION_CHANGED_FILES.txt` 같은 산출물은 만들지 않는다.

## 4. 작업 시작 전 기준 기록

작업 시작 전에 아래 명령을 실행하고, 결과를 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```powershell
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

주의:

* 이 명령은 상태 확인용이다.
* commit/push/branch 생성은 하지 않는다.
* 로컬 작업트리 변경이 있으면 파일별로 구분해서 기록한다.
* `CPF_NEW_REQUEST.md`가 사용자 갱신분으로 수정 상태라면 작업 대상에서 제외하고 리포트에 별도 기록한다.

## 5. 수정 대상 파일

우선 수정 대상은 아래다.

```text
scripts/check-html-docs.ps1
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

필요한 경우에만 아래 파일을 수정한다.

```text
scripts/check-feature-evidence.ps1
scripts/check-feature-status.ps1
```

신규 스크립트를 추가할 경우:

* 목적은 기능 매트릭스/리포트 상태 정합성 검출에 한정한다.
* README나 문서에 필요 이상으로 새 항목을 늘리지 않는다.
* `CPF_STABILIZATION_REPORT.html`에 신규 스크립트 추가 이유를 기록한다.

## 6. 구현 상세 기준

### 6.1 허용 상태값 목록을 코드에 명시

스크립트에는 허용 상태값 목록이 명확히 있어야 한다.

예:

```powershell
$AllowedStatuses = @(
  "완료",
  "부분 구현",
  "미구현",
  "미검증",
  "실패",
  "재확인 필요"
)
```

금지 상태값도 명시한다.

```powershell
$ForbiddenStatusValues = @(
  "실환경 필요",
  "완료 후보",
  "확인 필요",
  "진행중",
  "보류",
  "대기",
  "작업중",
  "성공",
  "검토중"
)
```

단, 금지어가 설명 문장에 나온다고 무조건 실패 처리하면 안 된다.
상태값 칸 또는 `data-status` 기준으로 검출한다.

---

### 6.2 HTML marker 기준 정리

`specs/기능_구현_매트릭스.html`와 `CPF_STABILIZATION_REPORT.html`의 주요 상태 행에는 가능하면 아래 marker를 추가한다.

```html
<tr data-check-id="mariadb-full-install">
  <td>MariaDB 전체 신규 설치</td>
  <td class="status" data-status="미검증">미검증</td>
  <td>이번 범위에서 신규 빈 DB 설치를 실행하지 않았음.</td>
</tr>
```

검증 대상 주요 `data-check-id`는 아래를 권장한다.

```text
edu-mapper-db-slice
mariadb-full-install
adm-runtime
openapi-runtime
adm-browser-click
standard-header-e2e
redis-kafka-mq-broker
quality-gate
check-html-docs
check-feature-evidence
check-utf8
```

---

### 6.3 리포트와 매트릭스 상태 비교

스크립트는 가능한 경우 아래를 비교한다.

* 기능 매트릭스의 `data-check-id`
* 안정화 리포트의 `data-check-id`
* 양쪽의 `data-status`

비교 결과:

* 양쪽에 같은 check id가 있고 status가 다르면 실패
* 기능 매트릭스에 있는데 리포트에 없으면 경고 또는 실패
* 리포트에 있는데 기능 매트릭스에 없으면 경고 또는 실패
* 이번 범위에서는 주요 검증 항목만 실패 처리하고, 보조 항목은 경고로 둬도 된다.

단, 실패/경고 기준은 리포트에 명확히 적는다.

## 7. 검증 명령

작업 후 아래 명령을 실행한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
.\gradlew.bat :xyz:test --offline --tests cpf.xyz.edu.repository.XyzQueryEducationMapperSliceTest --rerun-tasks
.\gradlew.bat :xyz:test --offline
.\gradlew.bat qualityGate --offline
```

이번 작업은 상태값/문서/스크립트 중심이므로 전체 `test --offline`은 가능하면 실행한다.

```powershell
.\gradlew.bat test --offline
```

단, 시간이 오래 걸리거나 환경상 실행하지 못하면 `미검증`으로 기록한다.
실행하지 않은 명령을 성공으로 기록하지 않는다.

## 8. 삭제 파일 확인

아래 파일이 없는지 확인한다.

```powershell
Test-Path .\CPF_STABILIZATION_CHANGED_FILES.txt
Test-Path .\xyz\src\test\resources\fixture\xyz_edu_query_mapper_fixture.sql
```

기대 결과:

```text
False
False
```

아래 파일은 있어야 한다.

```powershell
Test-Path .\xyz\src\test\resources\sql\xyz_edu_query_fixture.sql
```

기대 결과:

```text
True
```

## 9. `CPF_STABILIZATION_REPORT.html` 기록 기준

리포트에는 아래를 반드시 기록한다.

### 9.1 기준 정보

```text
작업 시작 branch
작업 시작 HEAD SHA
작업 시작 origin/master SHA
작업 종료 HEAD SHA
작업 종료 status --short
사용자 push 후 ChatGPT가 확인해야 하는 항목
```

### 9.2 변경 파일

파일별로 아래를 기록한다.

```text
파일명
변경 목적
추가한 검증 기준
삭제한 legacy 기준
```

### 9.3 상태값 검증 결과

아래를 기록한다.

```text
허용 상태값 목록
금지 상태값 목록
기능 매트릭스 상태값 검출 결과
리포트/매트릭스 주요 상태 비교 결과
```

### 9.4 실행 명령 결과

명령별로 아래를 기록한다.

```text
명령
성공/실패/미실행
실패 원인
skip 수
완료로 기록하지 않은 이유
```

### 9.5 다음 MariaDB 전체 설치 검증 준비

아래를 기록한다.

```text
확인한 SQL/Flyway/all_install/smoke 파일 목록
존재하지 않는 파일 목록
다음 단계에서 실행할 검증 명령 후보
이번 범위에서 실제 MariaDB 전체 설치를 하지 않았다는 점
상태값: 미검증
```

## 10. 완료 기준

아래가 모두 충족되어야 이번 작업을 완료로 본다.

* 기능 매트릭스 상태값은 허용된 6개만 사용한다.
* 상태값 위치에 `data-status` 또는 동등한 marker가 있다.
* `check-html-docs.ps1` 또는 별도 check script가 허용되지 않은 상태값을 검출한다.
* `실환경 필요` 같은 표현은 상태값이 아니라 설명 문장으로만 사용된다.
* `CPF_STABILIZATION_REPORT.html`과 기능 매트릭스의 주요 검증 상태가 일치한다.
* `CPF_STABILIZATION_CHANGED_FILES.txt`가 없다.
* old fixture 파일이 없다.
* new fixture 파일만 evidence로 사용된다.
* check script들이 성공한다.
* 실행하지 않은 Gradle/MariaDB/runtime/browser/broker 검증은 성공으로 기록하지 않는다.
* 다음 MariaDB 전체 설치 검증 대상 파일 목록이 실제 파일 기준으로 리포트에 정리된다.

## 11. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

* 기능 매트릭스 상태값에 허용되지 않은 값이 남아 있음
* 상태값 자동 검출 스크립트에 허용 상태값 목록이 없음
* 설명 문장의 `실환경 필요`까지 무조건 실패 처리해서 문서 작성이 불가능해짐
* 리포트와 기능 매트릭스가 서로 다른 상태를 말함
* `CPF_STABILIZATION_CHANGED_FILES.txt`가 남아 있음
* old fixture 경로가 코드/evidence/문서에 남아 있음
* `check-html-docs.ps1` 성공 주장과 실제 repo 상태가 충돌함
* MariaDB 전체 설치를 하지 않았는데 완료로 기록
* ADM runtime/OpenAPI/browser/broker 미검증을 완료로 기록
* Codex 로컬 실행 성공을 ChatGPT 직접 검증 완료로 표현
* Git commit / push / branch 생성 수행
* 별도 변경파일 목록 산출물 생성

## 12. 완료 보고 양식

완료 보고는 반드시 아래 양식으로 작성한다.

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- GitHub master 확인 가능 여부:

2. 변경 파일
- 파일:
- 변경 목적:
- 추가한 검증:
- 제거한 legacy 기준:

3. 상태값 검증
- 허용 상태값:
- 금지 상태값:
- 기능 매트릭스 검출 결과:
- 리포트/매트릭스 불일치 여부:

4. 삭제 파일 확인
- CPF_STABILIZATION_CHANGED_FILES.txt:
- old EDU fixture:
- new EDU fixture:

5. 실행 명령 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- 미실행 사유:

6. MariaDB 전체 설치 검증 준비
- 확인한 SQL/Flyway/all_install/smoke 파일:
- 존재하지 않는 파일:
- 다음 단계 실행 후보 명령:
- 현재 상태값:

7. 남은 미검증
- 대상:
- 미검증 사유:
- 완료로 기록하지 않은 이유:

8. ChatGPT 검수 참고
- GitHub에서 확인할 주요 파일:
- 주의할 리스크:
```

## 13. 다음 보강 후보

이번 작업이 완료되면 다음 요청은 아래 순서로 진행한다.

1. MariaDB 신규 빈 DB 전체 설치 검증
2. split SQL / Flyway / all_install / smoke SQL 정합성 검증
3. ADM runtime / OpenAPI smoke
4. ADM 브라우저 클릭 검증
5. 표준 헤더 inbound → context → log → ADM → outbound E2E 검증
6. BAT standalone / lock / heartbeat / ghost 검증
7. 문서 정본화 및 개발가이드 상세화
