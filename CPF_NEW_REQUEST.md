CPF_REWORK_REQUEST_20260630_02 — EDU Mapper DB fixture 검증 불일치 정리 및 문서/리포트 정합성 복구 요청

## 1. 작업 목적

이번 작업의 목적은 직전 EDU Mapper DB fixture 검증 작업에서 발생한 실제 파일과 완료 보고의 불일치를 정리하는 것이다.

이번 작업은 신규 기능 구현이 아니라 다음 불일치를 닫는 정정 작업이다.

* 기존 fixture 파일이 제거됐다고 보고했으나 실제 repo에 남아 있는 문제
* `CPF_STABILIZATION_CHANGED_FILES.txt`가 생성하지 않았다고 보고했으나 repo에 존재하는 문제
* `CPF_STABILIZATION_REPORT.html`과 specs HTML 문서가 실제 HTML이 아니라 Markdown 형식으로 작성된 문제
* `scripts/check-html-docs.ps1` 성공 주장과 현재 repo 상태가 맞지 않는 문제
* 기능 구현 매트릭스가 EDU Mapper DB fixture 검증 결과와 불일치하는 문제
* 기능 구현 매트릭스에 허용되지 않은 상태값 `실환경 필요`가 남아 있는 문제

## 2. 이번 범위

이번 범위는 아래 파일 정리에 한정한다.

### 2.1 제거 또는 정리 대상

* `xyz/src/test/resources/fixture/xyz_edu_query_mapper_fixture.sql`

  * 기존 fixture 경로 파일이다.
  * 현재 테스트는 `sql/xyz_edu_query_fixture.sql`을 사용하므로 제거한다.
  * 제거하지 않을 합당한 이유가 있으면 리포트에 `재확인 필요`로 기록하고, 중복 fixture를 완료로 포장하지 않는다.

* `CPF_STABILIZATION_CHANGED_FILES.txt`

  * 별도 변경파일 목록 산출물은 만들지 않는다는 CPF 기준과 맞지 않는다.
  * 현재 repo에 남아 있으면 `scripts/check-html-docs.ps1`가 실패해야 한다.
  * 파일을 제거하거나, 제거하지 못한 정확한 이유를 `CPF_STABILIZATION_REPORT.html`에 `재확인 필요`로 기록한다.

### 2.2 수정 대상

* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`
* `specs/개발_가이드.html`
* `scripts/check-html-docs.ps1`
* 필요 시 `scripts/check-feature-evidence.ps1`

## 3. 제외 범위

이번 작업에서는 아래를 하지 않는다.

* Git commit / push / branch 생성 지시
* 신규 기능 구현
* MariaDB 전체 신규 설치 검증
* ADM runtime / OpenAPI / 브라우저 smoke
* 표준 헤더 E2E
* Redis/Kafka/MQ 실 broker 검증
* 문서 체계 전체 재설계
* Markdown/AsciiDoc/YAML 문서 체계 전환

문서 포맷은 현재 HTML 유지 방침을 따른다. 단, `.html` 확장자 파일은 실제 HTML 구조를 가져야 한다.

## 4. 현재 상태 판정

현재 상태는 다음과 같이 본다.

* `XyzQueryEducationMapperSliceTest` repository 경로 유지: 완료
* `CPF_XYZ_EDU_MAPPER_DB_USERNAME` 우선 사용: 완료
* `sql/xyz_edu_query_fixture.sql` 사용: 완료
* 기존 `fixture/xyz_edu_query_mapper_fixture.sql` 제거: 실패
* `CPF_STABILIZATION_CHANGED_FILES.txt` 미존재: 실패
* `CPF_STABILIZATION_REPORT.html` 정상 HTML: 실패
* 기능 매트릭스 EDU Mapper DB slice 상태: 실패 또는 재확인 필요
* Gradle/JUnit/MariaDB 실행 성공: ChatGPT 직접 실행 기준 미검증

## 5. 구현 / 수정 대상

### 5.1 기존 fixture 파일 제거

대상:

* `xyz/src/test/resources/fixture/xyz_edu_query_mapper_fixture.sql`

요구사항:

* 현재 테스트에서 사용하지 않는 기존 fixture 파일을 제거한다.
* `XyzQueryEducationMapperSliceTest`는 반드시 `sql/xyz_edu_query_fixture.sql`만 로딩해야 한다.
* `check-feature-evidence.ps1`도 새 fixture 경로만 확인해야 한다.
* 문서와 리포트에서 기존 fixture 경로를 제거한다.

완료 불인정:

* 기존 fixture 파일이 repo에 남아 있음
* 테스트는 새 경로를 쓰지만 old fixture가 남아 있어 증거 경로가 둘로 갈라짐
* 리포트에는 제거했다고 쓰고 실제 파일은 남아 있음

### 5.2 `CPF_STABILIZATION_CHANGED_FILES.txt` 정리

대상:

* `CPF_STABILIZATION_CHANGED_FILES.txt`

요구사항:

* repo 루트에서 제거한다.
* 제거가 불가능하면 이유를 리포트에 정확히 적고 `재확인 필요`로 둔다.
* `scripts/check-html-docs.ps1`가 이 파일 존재 시 실패하는 기준을 유지한다.

완료 불인정:

* 파일이 남아 있는데 `check-html-docs.ps1` 성공으로 기록
* 파일이 남아 있는데 “별도 변경파일 목록 산출물 없음”이라고 기록
* 과거 파일이라는 이유만 쓰고 현 상태를 정리하지 않음

### 5.3 HTML 문서 형식 복구

대상:

* `CPF_STABILIZATION_REPORT.html`
* `specs/*.html`
* `scripts/check-html-docs.ps1`

요구사항:

* `.html` 확장자 문서는 실제 HTML 구조여야 한다.
* 최소한 아래 구조를 가져야 한다.

```html
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>...</title>
</head>
<body>
  ...
</body>
</html>
```

* Markdown 제목(`# 제목`), Markdown 표, Markdown fenced code block이 `.html` 원문에 남아 있으면 실패해야 한다.
* `scripts/check-html-docs.ps1`는 다음을 검사해야 한다.

  * `<!doctype html>` 또는 `<html` 존재
  * `<head>` 존재
  * `<body>` 존재
  * `<meta charset="UTF-8">` 또는 동등한 charset 선언 존재
  * 문서 첫 non-blank 문자가 `#`이면 실패
  * `CPF_STABILIZATION_CHANGED_FILES.txt` 존재 시 실패
  * `CPF_STABILIZATION_REPORT.md` 존재 시 실패

완료 불인정:

* `.html` 파일 내용이 Markdown 형식
* 스크립트 주석은 HTML 검증이라고 되어 있으나 실제 HTML 구조를 검증하지 않음
* check-html-docs가 현재 repo 상태와 다르게 성공으로 기록됨

### 5.4 기능 구현 매트릭스 현행화

대상:

* `specs/기능_구현_매트릭스.html`

요구사항:

* 상태값은 아래 6개만 사용한다.

  * 완료
  * 부분 구현
  * 미구현
  * 미검증
  * 실패
  * 재확인 필요

* `실환경 필요` 상태값은 제거하고, 필요하면 설명 컬럼에 “실환경 필요”라고 문장으로만 적는다.

* EDU Mapper DB slice 상태는 실제 검증 증거 기준으로 정리한다.

  * Codex 로컬 MariaDB 실행 결과만 있고 ChatGPT 직접 실행 또는 재현 가능한 로그가 없으면 `재확인 필요` 또는 `미검증`으로 둔다.
  * JUnit XML을 repo에 넣으라는 뜻은 아니다.
  * 단, 리포트와 기능 매트릭스가 서로 다르게 말하면 안 된다.

완료 불인정:

* 리포트는 완료, 매트릭스는 미검증으로 불일치
* 상태값에 `실환경 필요` 사용
* 실행하지 않은 ADM/runtime/broker 검증을 완료 처리

### 5.5 안정화 리포트 정정

대상:

* `CPF_STABILIZATION_REPORT.html`

요구사항:

리포트에는 아래를 분리해서 적는다.

* Codex가 실행했다고 주장한 명령
* 실제 repo 파일에서 확인 가능한 것
* ChatGPT가 직접 실행하지 못해 미검증인 것
* 기존 fixture 잔존 여부
* `CPF_STABILIZATION_CHANGED_FILES.txt` 처리 여부
* HTML 문서 구조 검사 결과
* 기능 매트릭스와의 일치 여부
* 남은 미검증 항목

리포트에는 “성공”이라는 단어를 쓸 때 반드시 근거를 적는다.

## 6. 테스트 / 검증 명령

필수 실행 명령:

```powershell
.\gradlew.bat :xyz:test --offline --tests cpf.xyz.edu.repository.XyzQueryEducationMapperSliceTest
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
```

파일 존재 확인:

```powershell
Test-Path .\xyz\src\test\resources\fixture\xyz_edu_query_mapper_fixture.sql
Test-Path .\xyz\src\test\resources\sql\xyz_edu_query_fixture.sql
Test-Path .\CPF_STABILIZATION_CHANGED_FILES.txt
```

기대 결과:

```text
old fixture path: False
new fixture path: True
CPF_STABILIZATION_CHANGED_FILES.txt: False
```

HTML 구조 확인:

```powershell
Get-Content .\CPF_STABILIZATION_REPORT.html -TotalCount 5
Get-Content .\specs\개발_가이드.html -TotalCount 5
Get-Content .\specs\기능_구현_매트릭스.html -TotalCount 5
```

기대 결과:

* 첫 줄이 `#`이면 실패
* `<html` 또는 `<!doctype html>`가 없으면 실패

## 7. `CPF_STABILIZATION_REPORT.html` 기록 기준

아래 형식으로 기록한다.

* 대상 기능명
* 대상 파일 또는 명령
* 시도한 작업
* 실제 파일 확인 결과
* 실행한 명령
* 성공/실패/skip/error 수
* 완료하지 못한 정확한 이유
* 현재 상태값: 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
* 다음에 필요한 조치
* 완료로 기록하지 않은 이유

## 8. 완료 기준

아래가 모두 충족되어야 완료로 본다.

* `xyz/src/test/resources/fixture/xyz_edu_query_mapper_fixture.sql`가 repo에서 제거됨
* `xyz/src/test/resources/sql/xyz_edu_query_fixture.sql`만 evidence로 사용됨
* `CPF_STABILIZATION_CHANGED_FILES.txt`가 repo 루트에 없음
* `CPF_STABILIZATION_REPORT.html`이 실제 HTML 구조임
* `specs/*.html`이 실제 HTML 구조임
* `scripts/check-html-docs.ps1`가 HTML 구조와 Markdown 잔재를 실제로 검출함
* 기능 매트릭스와 리포트가 같은 상태를 말함
* 기능 매트릭스 상태값은 허용된 6개만 사용함
* 실행하지 않은 검증은 성공으로 기록하지 않음

## 9. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

* 기존 fixture 파일 잔존
* `CPF_STABILIZATION_CHANGED_FILES.txt` 잔존
* `.html` 파일이 Markdown 형식
* `check-html-docs.ps1`가 현재 repo 상태와 다르게 성공으로 기록됨
* 기능 매트릭스가 리포트와 불일치
* 상태값에 `실환경 필요` 사용
* JUnit XML 또는 실행 로그 확인 없이 ChatGPT 검증 완료로 기록
* MariaDB 전체 설치 검증을 Mapper fixture 검증과 혼동
* ADM runtime/OpenAPI/browser/broker 미검증을 완료로 기록

## 10. 다음 보강 후보

이번 정정 작업이 끝난 뒤에만 다음 순서로 진행한다.

1. MariaDB 신규 빈 DB 전체 설치 검증
2. split SQL / Flyway / all_install / smoke SQL 정합성 검증
3. ADM runtime / OpenAPI smoke
4. ADM 브라우저 클릭 검증
5. 표준 헤더 inbound → context → log → ADM → outbound E2E 검증
6. 문서 정본화 및 개발가이드 상세화
