# CPF_REWORK_REQUEST_20260629_2

## 1. 작업 목적

이번 작업은 신규 기능 확장이 아니라 ChatGPT 검수에서 확인된 실제 push 파일 불일치를 닫는 작업이다.

가장 중요한 결함은 `CPF_STABILIZATION_REPORT.html`과 `specs/기능_구현_매트릭스.html`에 `XyzQueryEducationMapperSliceTest`가 존재한다고 기록되어 있으나, GitHub push 기준 해당 파일이 확인되지 않는 점이다.

이번 작업은 이 불일치를 먼저 닫고, 표준 헤더 가이드의 trusted proxy 문서 누락을 보정한다.

## 2. 이번 범위

이번 범위는 아래 4개로 한정한다.

1. `XyzQueryEducationMapperSliceTest.java` 실제 파일 추가 또는 리포트에서 미구현으로 정정
2. `:xyz:test`의 EDU slice tests=2/skipped=1 결과와 실제 파일 상태 정합성 보정
3. `specs/기능_구현_매트릭스.html`에서 존재하지 않는 slice test 증거 제거 또는 실제 파일 추가
4. `specs/표준_헤더_가이드.html`에 `CpfTrustedProxyPolicy`와 invalid IP/IPv6 처리 기준 명시

## 3. 이번 제외 범위

아래는 이번 작업 범위가 아니다.

* Git commit, push, branch 생성
* `CPF_NEW_REQUEST.md` 수정
* 별도 수정파일 목록 산출물 생성
* ADM runtime smoke 신규 구현
* OpenAPI runtime smoke 신규 구현
* ADM 브라우저 클릭 자동화 신규 구현
* MariaDB 신규 DB 전체 설치 실실행
* Redis/Kafka/MQ 실 broker 검증
* 센터컷 신규 구현
* CMN 파일 공통 기능 신규 구현

제외 항목은 `CPF_STABILIZATION_REPORT.html`에 `추후 이슈`로 분리하고, 왜 이번 범위에서 제외했는지와 다음에 어떤 조건이면 닫을 수 있는지 적는다.

---

# 4. 현재 상태 판정

## 4.1 EDU Mapper Slice Test

현재 `xyz/src/test/java/cpf/xyz/edu/repository`에는 `XyzQueryEducationRepositoryTest.java`만 확인된다. `XyzQueryEducationMapperSliceTest.java`는 GitHub push 기준 확인되지 않는다.

그런데 `CPF_STABILIZATION_REPORT.html`에는 `XyzQueryEducationMapperSliceTest`를 추가했고 `:xyz:test`에서 Mapper XML 위험 패턴 검사 성공, 실제 DB fixture 실행 1건 skip이라고 기록되어 있다.

현재 상태값: `실패`

## 4.2 기능 매트릭스

`specs/기능_구현_매트릭스.html`의 `XYZ EDU 온라인 조회 샘플` 증거에 `XyzQueryEducationMapperSliceTest`가 들어 있다. 실제 파일이 없으면 기능 매트릭스가 실제 구현 상태와 불일치한다.

현재 상태값: `실패`

## 4.3 표준 헤더 가이드

`specs/표준_헤더_가이드.html`은 HTML 구조는 정상이다. 하지만 구현 기준 클래스 목록 또는 client IP 처리 기준에 `CpfTrustedProxyPolicy`가 명시적으로 들어가 있지 않다. trusted proxy 정책이 핵심 구현이 되었으므로 문서 보정이 필요하다.

현재 상태값: `부분 구현`

---

# 5. 작업 상세

## 5.1 EDU Mapper slice test 처리

둘 중 하나를 반드시 선택한다.

### 선택 A: 실제 파일 추가

아래 파일을 실제로 추가한다.

```text
xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java
```

구현 기준:

* MyBatis XML 정적 안전성 테스트와 실제 DB fixture 실행 테스트를 분리한다.
* DB 없이도 실행 가능한 정적 테스트는 `:xyz:test`에서 항상 실행되게 한다.
* 실제 DB fixture 실행 테스트는 안전한 테스트 DB 환경변수가 있을 때만 실행한다.
* 테스트 DB 환경변수가 없으면 skip 처리하되 성공으로 포장하지 않는다.
* skip 조건과 실행 명령을 리포트에 기록한다.
* `xyz_edu_query_mapper_fixture.sql` 사용 여부를 테스트 코드나 리포트에서 명확히 연결한다.
* Mockito mock 기반 repository test를 slice test로 포장하지 않는다.

필수 테스트:

* Mapper XML에 `mbr_member` 직접 JOIN이 없는지
* Mapper XML에 `exs_` 직접 JOIN이 없는지
* `${sort}` 같은 직접 삽입 위험이 없는지
* sort whitelist `ID_ASC`, `NAME_ASC`, `CREATED_DESC` 기준이 있는지
* fixture SQL 파일이 존재하는지
* DB 환경변수 부재 시 실제 DB 테스트는 skip되는지
* DB 환경변수가 있을 때 fixture SQL을 실행하도록 구성되어 있는지

가능하면 실제 DB fixture 테스트 항목:

* 단건 조회
* 목록 조회
* keyword 검색
* status 검색
* offset page
* count
* keyset page
* `NAME_ASC`
* `CREATED_DESC`
* limit+1 hasNext

### 선택 B: 파일 추가 불가 시 정정

이번 범위에서 파일을 만들지 못하면 아래를 수행한다.

* `CPF_STABILIZATION_REPORT.html`에서 `XyzQueryEducationMapperSliceTest` 추가/성공 표현 제거
* `specs/기능_구현_매트릭스.html`에서 증거 파일로 `XyzQueryEducationMapperSliceTest` 제거
* 상태값을 `미구현` 또는 `미검증`으로 정정
* 왜 만들지 못했는지 정확히 기록
* 다음에 필요한 DB/profile/Gradle task 조건 기록
* “구조 추가”, “테스트 추가”, “완료”라는 표현 금지

## 완료 기준

* 실제 파일이 존재하거나, 리포트/매트릭스에서 미구현으로 정확히 정정되어 있다.
* `:xyz:test` 결과와 실제 파일 상태가 충돌하지 않는다.
* skip된 DB fixture 실행은 성공으로 기록하지 않는다.

## 완료 불인정 기준

* 실제 파일 없이 리포트에 추가했다고 기록
* mock repository test를 mapper slice test로 기록
* `tests=2, skipped=1`이라고 쓰고 실제 테스트 파일이 없음
* fixture SQL만 있고 테스트 연결이 없음

---

## 5.2 표준 헤더 가이드 trusted proxy 문서 보정

### 수정 대상

* `specs/표준_헤더_가이드.html`
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`, 필요한 경우

### 수정 기준

표준 헤더 가이드에 아래를 명시한다.

* 구현 기준 클래스에 `CpfTrustedProxyPolicy` 추가
* `CpfHeaderExtractor`가 `CpfTrustedProxyPolicy`를 통해 resolved client IP를 산정한다는 설명
* trusted proxy가 아닌 요청에서는 `X-Client-IP`, `X-Forwarded-For`, `Forwarded`, `X-Real-IP`를 신뢰하지 않는다는 기준
* trusted proxy에서 온 요청일 때만 forwarding header를 해석한다는 기준
* `unknown`, blank, hostname, invalid IPv4는 무시한다는 기준
* IPv6 지원 범위: bracket IPv6 normalization을 실제 테스트로 보장하는지, 아니면 추후 이슈인지 명확히 기록
* raw inbound header와 resolved client IP를 ADM에서 구분해야 한다는 기준

## 완료 기준

* `specs/표준_헤더_가이드.html`에 `CpfTrustedProxyPolicy`가 명시된다.
* invalid IP/IPv6 처리 기준이 실제 구현과 일치한다.
* 문서가 구현보다 앞서 과장하지 않는다.

## 완료 불인정 기준

* 구현 기준 클래스에서 `CpfTrustedProxyPolicy` 누락
* broken IPv6 테스트가 없는데 완료로 기록
* client가 보낸 `X-Client-IP`를 그대로 신뢰하는 표현 유지
* 문서만 완료이고 테스트/구현과 불일치

---

# 6. 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :xyz:test --offline
.\gradlew.bat :pfw:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-utf8.ps1 -CheckMojibake
```

실행하지 못한 명령은 성공으로 기록하지 않는다.

# 7. 리포트 기록 기준

`CPF_STABILIZATION_REPORT.html`에는 아래를 기록한다.

* 실제 추가/수정한 파일
* `XyzQueryEducationMapperSliceTest.java` 실제 존재 여부
* DB fixture 테스트 실행/skip 여부
* skip 조건과 다음 실행 방법
* 표준 헤더 가이드 trusted proxy 반영 여부
* 실행한 명령과 결과
* 실패/미검증/추후 이슈

# 8. 다음 보강 후보

이번 요청이 닫힌 뒤 다음 순서로 진행한다.

1. 안전한 빈 MariaDB 신규 DB 전체 설치 실검증
2. ADM runtime/OpenAPI smoke
3. ADM 주요 화면 브라우저 클릭 자동화
4. 표준 헤더 수신 → context → log → ADM → outbound E2E
5. EDU Mapper DB fixture 실실행 자동화
6. CMN 파일 공통 기능 및 EDU 파일 처리 샘플
7. BAT runtime / ghost detection / 장애 시나리오
