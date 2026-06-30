CPF_REWORK_REQUEST_20260630_01 — EDU Mapper DB fixture 실검증 및 남은 미검증 항목 정리 요청

## 1. 작업 목적

이번 작업의 목적은 기존에 경로 정합성만 정리된 `XyzQueryEducationMapperSliceTest`를 실제 안전한 MariaDB 테스트 DB 기준으로 실행 검증하고, `CPF_STABILIZATION_REPORT.html`에 성공/skip/미검증/실패를 과장 없이 분리 기록하는 것이다.

이번 작업은 “파일 존재 확인”이 아니라 “실제 DB fixture 실행 가능성”과 “검증 증거의 재현 가능성”을 닫는 작업이다.

## 2. 이번 범위

이번 범위는 아래로 한정한다.

1. `xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java`

   * 기존 정적 XML 검증 유지
   * 안전한 MariaDB 테스트 DB 환경변수가 있을 때 fixture SQL을 실제 실행
   * 단건, 목록, 검색, 정렬 fallback, offset page/count, keyset page 검증
   * 환경변수가 없을 때는 skip하되, skip을 성공으로 기록하지 말 것

2. `xyz/src/test/resources/sql/xyz_edu_query_fixture.sql`

   * 테스트 DB에서 반복 실행 가능해야 함
   * destructive SQL은 fixture 범위의 테스트 데이터에 한정
   * 실제 업무/개발 DB를 오염시키지 않도록 주석과 조건을 명확히 둘 것

3. `scripts/check-feature-evidence.ps1`

   * 현재 repository 경로 evidence는 유지
   * 단, 이 스크립트가 Gradle/JUnit/DB 실검증을 대체하지 않는다는 점을 리포트에 명확히 기록

4. `CPF_STABILIZATION_REPORT.html`

   * 실행한 명령
   * 실행 조건
   * 성공/실패/skip 건수
   * DB fixture 실행 여부
   * 미검증 사유
   * 다음 조치
     를 분리 기록

5. `specs/기능_구현_매트릭스.html`

   * DB fixture가 실제 실행되기 전까지 XYZ EDU는 `완료`로 올리지 말 것
   * 실제 DB fixture까지 성공했을 때도 MariaDB 전체 설치, runtime, OpenAPI, 브라우저 검증이 남아 있으면 전체 완료로 올리지 말 것

## 3. 제외 범위

이번 작업에서 아래는 제외한다.

* Git commit / push / branch 생성 지시
* 별도 변경파일 목록 산출물 생성
* `CPF_STABILIZATION_CHANGED_FILES.txt` 생성
* `CPF_NEW_REQUEST.md` 수정
* ADM runtime / OpenAPI / 브라우저 smoke 구현
* 표준 헤더 E2E 구현
* Redis/Kafka/MQ 실 broker 검증
* MariaDB 전체 신규 설치 검증

단, 제외 항목은 `CPF_STABILIZATION_REPORT.html`에 `추후 이슈` 또는 `미검증`으로 분리 기록한다.

## 4. 현재 상태 판정

현재 상태는 다음과 같이 본다.

* `XyzQueryEducationMapperSliceTest` 경로 정합성: 완료
* 기존 mapper 경로 테스트 제거: 완료
* evidence script / 개발 가이드 / 기능 매트릭스 경로 정합성: 완료
* Gradle/JUnit 실행 성공: 재확인 필요
* EDU Mapper DB fixture 실실행: 미검증
* MariaDB 신규 DB 전체 설치: 미검증
* ADM runtime/OpenAPI/browser: 미검증
* 표준 헤더 E2E: 미검증

## 5. 구현 / 수정 대상

### 5.1 Mapper slice test

대상 파일:

* `xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java`

요구사항:

* `CPF_XYZ_EDU_MAPPER_SLICE_TEST=true`일 때만 실제 DB 테스트 실행
* DB URL, 사용자, 비밀번호가 모두 없으면 skip
* skip은 성공으로 기록하지 말 것
* fixture SQL 실행 후 아래를 검증

  * 단건 조회
  * 목록 조회
  * keyword 검색
  * status 검색
  * 허용 sort
  * 허용되지 않은 sort fallback
  * offset page
  * count
  * keyset page
* 테스트 대상 테이블/데이터는 fixture 범위로 제한
* 실제 운영/개발 DB에 연결되지 않도록 환경변수명과 주석을 명확히 둘 것

### 5.2 Fixture SQL

대상 파일:

* `xyz/src/test/resources/sql/xyz_edu_query_fixture.sql`

요구사항:

* 반복 실행 가능해야 함
* 테스트 데이터 삭제 범위는 fixture key 범위로 제한
* 테이블 생성, comment, index, 테스트 데이터 insert가 유지되어야 함
* SQL 표준 가이드와 맞지 않는 컬럼/명명/COMMENT가 있으면 수정

### 5.3 리포트

대상 파일:

* `CPF_STABILIZATION_REPORT.html`

요구사항:

아래 형식으로 기록한다.

* 대상 기능명
* 대상 파일 또는 명령
* 시도한 작업
* 실행한 명령
* 실행 환경
* 성공/실패/skip/error 수
* 완료하지 못한 정확한 이유
* 현재 상태값: 완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요
* 다음에 필요한 조치
* 완료로 기록하지 않은 이유

## 6. 문서 반영 대상

* `specs/기능_구현_매트릭스.html`
* `specs/개발_가이드.html`
* `CPF_STABILIZATION_REPORT.html`

문서에는 “파일 존재 확인”과 “실제 DB 실행 검증”을 분리해서 적는다.

## 7. 테스트 / 스모크 대상

필수 실행 명령:

```powershell
.\gradlew.bat :xyz:test --offline --tests cpf.xyz.edu.repository.XyzQueryEducationMapperSliceTest
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
scripts\check-feature-evidence.ps1
scripts\check-html-docs.ps1
scripts\check-utf8.ps1 -CheckMojibake
```

DB fixture 실검증 시에는 안전한 테스트 DB 전용 환경변수를 사용한다.

예시:

```powershell
$env:CPF_XYZ_EDU_MAPPER_SLICE_TEST="true"
$env:CPF_XYZ_EDU_MAPPER_DB_URL="jdbc:mariadb://localhost:3306/cpf_test"
$env:CPF_XYZ_EDU_MAPPER_DB_USERNAME="cpf_test"
$env:CPF_XYZ_EDU_MAPPER_DB_PASSWORD="cpf_test_password"

.\gradlew.bat :xyz:test --offline --tests cpf.xyz.edu.repository.XyzQueryEducationMapperSliceTest
```

주의:

* 위 DB는 반드시 테스트 전용 DB여야 한다.
* 운영/개발 공용 DB에 연결하지 말 것.
* 안전한 테스트 DB가 없으면 실행하지 말고 `미검증`으로 기록할 것.

## 8. 검증 명령 기록 기준

`CPF_STABILIZATION_REPORT.html`에는 명령별로 아래를 기록한다.

* 명령어
* 실행 여부
* 성공/실패
* 실패 시 오류 요약
* skip 수
* DB fixture 실행 여부
* 실행하지 못한 경우 정확한 사유

`check-feature-evidence.ps1` 성공만으로 JUnit/DB 검증 성공이라고 기록하지 말 것.

## 9. 완료 기준

아래가 모두 충족되어야 이번 범위를 완료로 본다.

* repository 경로의 `XyzQueryEducationMapperSliceTest`가 유지됨
* 기존 mapper 경로 테스트가 다시 생기지 않음
* 안전한 MariaDB 테스트 DB에서 fixture SQL이 실제 실행됨
* JUnit 결과에서 DB fixture 테스트가 skip이 아니라 실행 성공으로 확인됨
* `:xyz:test --offline` 성공
* 관련 문서와 리포트가 실제 결과와 일치
* 실패/skip/미검증이 있으면 완료로 기록하지 않고 정확한 상태값으로 기록

## 10. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

* DB fixture 테스트가 skip인데 완료로 기록
* `check-feature-evidence.ps1` 성공만으로 DB 검증 성공 처리
* Gradle 실행 로그 없이 성공이라고 기록
* MariaDB 테스트 DB가 아닌 개발/운영 DB에 fixture 실행
* `CPF_NEW_REQUEST.md` 수정
* `CPF_STABILIZATION_CHANGED_FILES.txt` 생성
* Controller/Service/Repository/Mapper/DTO/SQL 중 하나와 문서 evidence가 불일치
* 미검증 항목을 “환경 문제” 한 줄로만 처리

## 11. 다음 보강 후보

이번 작업 이후 다음 우선순위는 아래다.

1. MariaDB 신규 빈 DB 전체 설치 검증
2. split SQL / Flyway / `00_all_install.sql` / `00_all_install_and_smoke.sql` 정합성 검증
3. ADM runtime / OpenAPI smoke
4. ADM 주요 화면 브라우저 클릭 검증
5. 표준 헤더 inbound → context → log → ADM → outbound E2E
6. 민감 헤더 원문 로그 저장 금지 검증
7. Redis/Kafka/MQ 실 broker 전파 검증
