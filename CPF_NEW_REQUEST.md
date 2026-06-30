CPF_REWORK_REQUEST_20260630_03 — 반복 검수 오류 방지 게이트 및 현재 불일치 정리 요청

## 1. 작업 목적

이번 작업의 목적은 단순 기능 수정이 아니라, Codex 완료 보고와 실제 GitHub/master 파일 상태가 반복해서 불일치하는 문제를 줄이기 위한 검증 게이트를 정리하는 것이다.

현재 반복되는 문제는 다음과 같다.

* Codex는 수정했다고 보고하지만 실제 검수 시 대상 파일에 반영되지 않음
* 삭제했다고 보고한 파일이 repo에 남아 있음
* 테스트/evidence/문서/리포트가 서로 다른 경로를 가리킴
* 리포트와 기능 매트릭스 상태값이 서로 다름
* 허용되지 않은 상태값이 기능 매트릭스에 남아 있음
* check script 성공 주장과 실제 repo 상태가 충돌함
* 로컬 실행 성공과 GitHub master 기준 검수 가능 상태가 구분되지 않음

이번 작업은 위 문제를 줄이기 위해 **파일 수정 + 자동 검출 기준 + 완료 보고 양식**을 함께 정리한다.

## 2. 이번 범위

이번 범위는 아래로 한정한다.

1. 현재 남아 있는 불일치 파일 정리
2. 기존 fixture 경로와 신규 fixture 경로 정합성 복구
3. 기능 매트릭스 상태값 6개 제한
4. 리포트와 기능 매트릭스 상태 일치
5. `check-html-docs.ps1` / `check-feature-evidence.ps1`가 반복 오류를 자동 검출하도록 강화
6. Codex 완료 보고 양식 고정
7. 실행하지 못한 검증을 성공으로 포장하지 않도록 리포트 기준 정리

## 3. 제외 범위

이번 작업에서 아래는 하지 않는다.

* Git commit 지시
* Git push 지시
* branch 생성 지시
* 신규 기능 개발
* MariaDB 전체 신규 설치 검증
* ADM runtime / OpenAPI / 브라우저 smoke 구현
* Redis/Kafka/MQ 실 broker 검증
* 문서 포맷 체계 전환
* Markdown/AsciiDoc/YAML 문서 체계 도입
* 별도 변경파일 목록 산출물 생성

특히 `CPF_STABILIZATION_CHANGED_FILES.txt` 같은 별도 변경파일 목록 산출물은 만들지 않는다.

## 4. ChatGPT 검수 기준

ChatGPT의 최종 검수 기준은 사용자가 push한 후 확인 가능한 **GitHub master 최신 커밋의 실제 파일 내용**이다.

따라서 Codex는 완료 보고 시 아래를 구분한다.

* 로컬에서 수정한 것
* 로컬에서 실행한 것
* GitHub master에서 확인 가능한 것
* 사용자가 아직 push하지 않아 ChatGPT가 확인할 수 없는 것
* 실행하지 못한 것
* 미검증으로 남긴 것

로컬에서 성공했더라도 GitHub master에 반영되지 않았거나, 사용자가 push 후 ChatGPT가 확인할 수 없는 상태면 완료로 포장하지 않는다.

## 5. 작업 시작 전 기준 기록

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
* commit / push / branch 생성은 하지 않는다.
* 로컬 HEAD와 `origin/master`가 다르면 그 차이를 리포트에 기록한다.
* 작업 완료 보고에서 “GitHub master에 반영됨”이라고 단정하지 않는다. 사용자가 push한 후 ChatGPT가 확인한다.

## 6. 현재 상태 판정

현재 상태는 다음과 같이 본다.

* `XyzQueryEducationMapperSliceTest` repository 경로 유지: 부분 구현
* MariaDB DB fixture 로컬 실행 주장: ChatGPT 기준 미검증
* fixture 경로 정합성: 재확인 필요
* old fixture 잔존 여부: 재확인 필요
* `CPF_STABILIZATION_CHANGED_FILES.txt` 잔존 여부: 재확인 필요
* `CPF_STABILIZATION_REPORT.html` HTML 구조: 부분 구현
* 기능 매트릭스 상태값 6개 제한: 재확인 필요
* 리포트와 기능 매트릭스 상태 일치: 재확인 필요
* check script가 반복 오류를 자동 검출하는지: 부분 구현 또는 재확인 필요

이번 작업에서는 위 항목을 실제 파일 기준으로 닫는다.

## 7. 수정 대상

### 7.1 `CPF_STABILIZATION_CHANGED_FILES.txt` 정리

대상:

* `CPF_STABILIZATION_CHANGED_FILES.txt`

요구사항:

* repo 루트에 남아 있으면 제거한다.
* 제거하지 못하면 정확한 사유를 `CPF_STABILIZATION_REPORT.html`에 `재확인 필요`로 기록한다.
* 이 파일이 존재하는 상태에서 `check-html-docs.ps1` 성공이라고 기록하지 않는다.

검증 명령:

```powershell
Test-Path .\CPF_STABILIZATION_CHANGED_FILES.txt
```

기대 결과:

```text
False
```

완료 불인정:

* 파일이 남아 있음
* 파일이 남아 있는데 “생성하지 않음”이라고만 기록
* 파일이 남아 있는데 check-html-docs 성공으로 기록

---

### 7.2 EDU Mapper fixture 경로 단일화

대상 후보:

* `xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java`
* `xyz/src/test/resources/sql/xyz_edu_query_fixture.sql`
* `xyz/src/test/resources/fixture/xyz_edu_query_mapper_fixture.sql`
* `scripts/check-feature-evidence.ps1`
* `specs/개발_가이드.html`
* `specs/기능_구현_매트릭스.html`
* `CPF_STABILIZATION_REPORT.html`

요구사항:

* 최종 fixture 기준 경로를 하나로 정한다.
* 권장 기준은 아래 신규 경로다.

```text
xyz/src/test/resources/sql/xyz_edu_query_fixture.sql
```

* 테스트 클래스, evidence script, 개발 가이드, 기능 매트릭스, 안정화 리포트가 모두 같은 fixture 경로를 가리켜야 한다.
* 기존 경로를 계속 쓸 합당한 이유가 없다면 아래 파일은 제거한다.

```text
xyz/src/test/resources/fixture/xyz_edu_query_mapper_fixture.sql
```

검증 명령:

```powershell
Test-Path .\xyz\src\test\resources\sql\xyz_edu_query_fixture.sql
Test-Path .\xyz\src\test\resources\fixture\xyz_edu_query_mapper_fixture.sql
Select-String -Path .\xyz\src\test\java\cpf\xyz\edu\repository\XyzQueryEducationMapperSliceTest.java -Pattern "xyz_edu_query_fixture.sql","xyz_edu_query_mapper_fixture.sql"
Select-String -Path .\scripts\check-feature-evidence.ps1 -Pattern "xyz_edu_query_fixture.sql","xyz_edu_query_mapper_fixture.sql"
Select-String -Path .\specs\개발_가이드.html -Pattern "xyz_edu_query_fixture.sql","xyz_edu_query_mapper_fixture.sql"
Select-String -Path .\specs\기능_구현_매트릭스.html -Pattern "xyz_edu_query_fixture.sql","xyz_edu_query_mapper_fixture.sql"
Select-String -Path .\CPF_STABILIZATION_REPORT.html -Pattern "xyz_edu_query_fixture.sql","xyz_edu_query_mapper_fixture.sql"
```

기대 결과:

* 신규 경로를 선택했다면 `xyz_edu_query_fixture.sql`만 남아야 한다.
* 기존 경로 `xyz_edu_query_mapper_fixture.sql`는 코드/evidence/문서에서 없어야 한다.
* 기존 경로를 유지하기로 했다면 신규 경로 파일을 제거하고 문서/리포트에 그 이유를 기록한다.
* 두 경로가 동시에 evidence로 남으면 실패다.

완료 불인정:

* 테스트는 old path, 문서는 new path
* evidence는 old path, 리포트는 new path
* 두 fixture 파일이 모두 남아 있음
* 제거했다고 보고했지만 실제 파일이 남아 있음

---

### 7.3 DB username 환경변수 기준 정리

대상:

* `xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java`
* `specs/개발_가이드.html`
* `CPF_STABILIZATION_REPORT.html`

요구사항:

* DB username 환경변수 기준을 하나로 정한다.
* 권장 기준은 아래다.

```text
CPF_XYZ_EDU_MAPPER_DB_USERNAME
```

* 기존 호환이 필요하면 아래 legacy 변수는 보조로만 허용한다.

```text
CPF_XYZ_EDU_MAPPER_DB_USER
```

* 우선순위는 `DB_USERNAME` → `DB_USER` 순서로 한다.
* 문서와 테스트 코드가 같은 기준을 설명해야 한다.

검증 명령:

```powershell
Select-String -Path .\xyz\src\test\java\cpf\xyz\edu\repository\XyzQueryEducationMapperSliceTest.java -Pattern "CPF_XYZ_EDU_MAPPER_DB_USERNAME","CPF_XYZ_EDU_MAPPER_DB_USER"
Select-String -Path .\specs\개발_가이드.html -Pattern "CPF_XYZ_EDU_MAPPER_DB_USERNAME","CPF_XYZ_EDU_MAPPER_DB_USER"
Select-String -Path .\CPF_STABILIZATION_REPORT.html -Pattern "CPF_XYZ_EDU_MAPPER_DB_USERNAME","CPF_XYZ_EDU_MAPPER_DB_USER"
```

완료 불인정:

* 완료 보고에는 `DB_USERNAME` 우선이라고 쓰고 실제 코드는 `DB_USER`만 사용
* 개발 가이드와 테스트 코드가 서로 다른 환경변수를 설명
* 비밀번호 값을 원문으로 리포트에 기록

---

### 7.4 기능 매트릭스 상태값 6개 제한

대상:

* `specs/기능_구현_매트릭스.html`
* 필요 시 `scripts/check-html-docs.ps1` 또는 `scripts/check-feature-evidence.ps1`

허용 상태값은 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

금지 상태값 예시:

```text
실환경 필요
완료 후보
확인 필요
대기
진행중
보류
```

`실환경 필요`라는 표현이 필요하면 상태값이 아니라 설명 문장에만 사용한다.

예:

```text
상태값: 미검증
설명: 실환경 broker가 없어 이번 범위에서는 검증하지 못함.
```

검증 명령:

```powershell
Select-String -Path .\specs\기능_구현_매트릭스.html -Pattern "실환경 필요","완료 후보","확인 필요","진행중","보류"
```

기대 결과:

* 상태값 칸에는 금지 상태값이 없어야 한다.
* 설명 문장으로 `실환경 필요`를 쓰는 경우에도 상태값과 구분되어야 한다.

완료 불인정:

* 기능 매트릭스 상태값에 `실환경 필요` 사용
* 리포트에는 미검증, 매트릭스에는 완료
* 리포트에는 완료, 매트릭스에는 미검증

---

### 7.5 리포트와 기능 매트릭스 상태 일치

대상:

* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`

요구사항:

아래 항목은 리포트와 기능 매트릭스가 같은 상태를 말해야 한다.

* EDU Mapper DB slice
* MariaDB 전체 신규 설치
* ADM runtime
* OpenAPI runtime
* ADM browser click
* 표준 헤더 E2E
* Redis/Kafka/MQ broker
* qualityGate
* check-html-docs
* check-feature-evidence
* check-utf8

원칙:

* Codex 로컬 실행 결과는 “Codex 로컬 실행 주장” 또는 “로컬 실행 확인”으로 기록한다.
* ChatGPT가 직접 실행하지 못한 것은 “ChatGPT 기준 미검증”으로 구분한다.
* MariaDB Mapper fixture 성공을 MariaDB 전체 설치 성공으로 확대하지 않는다.
* check script 성공을 runtime 성공으로 확대하지 않는다.

완료 불인정:

* 리포트와 매트릭스가 다른 상태를 말함
* 로컬 테스트 성공을 전체 설치 성공으로 기록
* runtime 미검증을 완료로 기록
* broker 미검증을 완료로 기록

---

### 7.6 `check-html-docs.ps1` 반복 오류 검출 강화

대상:

* `scripts/check-html-docs.ps1`

요구사항:

이 스크립트는 아래를 실패로 잡아야 한다.

* `CPF_STABILIZATION_CHANGED_FILES.txt` 존재
* `CPF_STABILIZATION_REPORT.md` 존재
* `specs` 하위 `.md` 문서 존재
* `.html` 파일의 첫 non-blank 문자가 `#`
* `.html` 파일에 Markdown fenced code block 잔재 존재
* `.html` 파일에 Markdown table 잔재 존재
* `.html` 파일에 `<html`, `<head`, `<body`, `<main>` 등 기본 구조 누락
* 기능 매트릭스 상태값에 허용되지 않은 상태값 사용
* 가능하면 `실환경 필요`가 상태값으로 쓰인 경우 실패

검증 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
```

완료 불인정:

* 스크립트가 강화됐다고 보고했지만 실제로 old artifact를 통과시킴
* `.html` 파일이 Markdown 형식인데 성공 처리
* 상태값 기준 위반을 통과시킴

---

### 7.7 `check-feature-evidence.ps1` evidence 경로 검증 강화

대상:

* `scripts/check-feature-evidence.ps1`

요구사항:

이 스크립트는 아래를 검출해야 한다.

* EDU Mapper slice test 경로가 repository 기준인지
* fixture SQL 경로가 현재 정본 경로와 일치하는지
* old fixture path가 evidence로 남아 있지 않은지
* Controller / Service / Repository / Mapper / DTO / SQL / Test evidence가 모두 있는지
* 문서 evidence가 실제 파일 경로와 맞는지

검증 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
```

완료 불인정:

* old fixture path를 계속 required file로 검사
* 문서는 new path, script는 old path
* evidence script 성공만으로 DB fixture 실행 성공이라고 기록

---

## 8. 테스트 / 검증 명령

이번 작업 후 아래 명령을 실행하고 결과를 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```powershell
.\gradlew.bat :xyz:test --offline --tests cpf.xyz.edu.repository.XyzQueryEducationMapperSliceTest --rerun-tasks
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
```

DB fixture 실검증을 수행했다면 아래도 기록한다.

* 사용한 환경변수 이름
* DB URL은 host/db명 수준까지만 기록
* DB password 원문은 기록 금지
* JUnit tests / skipped / failures / errors
* 실행하지 못한 경우 skip 또는 미검증 사유

주의:

* JUnit XML 파일을 repo에 추가하지 않는다.
* 실행 결과를 리포트에 요약 기록한다.
* 실패한 명령이 있으면 성공으로 포장하지 않는다.

## 9. 완료 보고 양식

Codex 완료 보고는 반드시 아래 양식으로 작성한다.

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- 사용자가 push해야 ChatGPT가 확인 가능한 상태인지 여부:

2. 변경 파일
- 파일:
- 변경 목적:
- 포함되어야 하는 문자열:
- 제거되어야 하는 문자열:

3. 삭제 파일 확인
- Test-Path 대상:
- 결과:

4. 실행 명령 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- 실패 또는 skip 사유:

5. 리포트 / 매트릭스 정합성
- 리포트 상태:
- 기능 매트릭스 상태:
- 불일치 여부:

6. 남은 미검증
- 대상:
- 미검증 사유:
- 완료로 기록하지 않은 이유:

7. ChatGPT 검수 참고
- GitHub에서 확인해야 할 주요 파일:
- 주의해야 할 남은 리스크:
```

양식 없이 “완료했습니다”라고만 보고하지 않는다.

## 10. `CPF_STABILIZATION_REPORT.html` 기록 기준

리포트에는 아래를 반드시 구분한다.

* Codex가 로컬에서 실행한 것
* 실제 파일에서 확인 가능한 것
* 사용자가 push 후 ChatGPT가 확인해야 하는 것
* 직접 실행하지 못한 것
* 실패한 것
* 미검증으로 남긴 것

리포트 상태값은 아래만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

## 11. 완료 기준

아래가 모두 충족되어야 완료로 본다.

* `CPF_STABILIZATION_CHANGED_FILES.txt`가 repo에 남지 않음
* fixture SQL evidence 경로가 하나로 통일됨
* old fixture 경로와 new fixture 경로가 동시에 evidence로 남지 않음
* 테스트 코드, evidence script, 개발 가이드, 기능 매트릭스, 안정화 리포트가 같은 fixture 경로를 말함
* DB username 환경변수 기준이 코드/문서/리포트에서 일치함
* 기능 매트릭스 상태값은 허용된 6개만 사용함
* `check-html-docs.ps1`가 HTML 구조, Markdown 잔재, 불필요 산출물, 상태값 위반을 잡음
* `check-feature-evidence.ps1`가 old evidence path를 잡음
* 리포트와 기능 매트릭스가 같은 상태를 말함
* 실행하지 못한 검증은 성공으로 기록하지 않음
* Codex 완료 보고가 기준 SHA, 변경 파일, 삭제 파일, 검증 결과, 남은 미검증을 포함함

## 12. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

* “수정했다”고 보고했으나 대상 파일에 문자열 증거가 없음
* “삭제했다”고 보고했으나 `Test-Path`가 True
* old fixture와 new fixture가 동시에 남아 있음
* 기능 매트릭스 상태값에 `실환경 필요` 사용
* `CPF_STABILIZATION_CHANGED_FILES.txt`가 남아 있음
* check script 성공 주장과 실제 repo 상태가 충돌함
* 리포트와 기능 매트릭스가 서로 다른 상태를 말함
* Codex 로컬 실행 성공을 ChatGPT 검증 완료로 표현
* MariaDB Mapper fixture 성공을 MariaDB 전체 설치 성공으로 확대
* ADM runtime/OpenAPI/browser/broker 미검증을 완료로 기록
* Git commit / push / branch 생성 수행
* 별도 변경파일 목록 산출물 생성

## 13. 다음 보강 후보

이번 반복 오류 방지 게이트와 현재 불일치 정리가 끝난 뒤 다음 순서로 진행한다.

1. MariaDB 신규 빈 DB 전체 설치 검증
2. split SQL / Flyway / all_install / smoke SQL 정합성 검증
3. ADM runtime / OpenAPI smoke
4. ADM 브라우저 클릭 검증
5. 표준 헤더 inbound → context → log → ADM → outbound E2E 검증
6. BAT standalone / lock / heartbeat / ghost 검증
7. 문서 정본화 및 개발가이드 상세화
