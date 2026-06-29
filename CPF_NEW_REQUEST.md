# CPF_REWORK_REQUEST_20260629

## 1. 작업 목적

이번 작업은 신규 기능 확장이 아니라 ChatGPT 검수에서 확인된 남은 불일치와 테스트 누락을 닫는 작업이다.

Codex 완료 보고가 아니라 실제 push 파일 기준으로 확인된 문제를 수정한다. 이번 범위는 좁게 유지한다. 범위를 넓혀 ADM runtime, MariaDB 신규 DB, 센터컷, 파일 공통 기능까지 동시에 진행하지 않는다.

## 2. 이번 범위

이번 작업 범위는 아래 4개로 한정한다.

1. `CpfTrustedProxyPolicy` invalid IP 검증 보강
2. `CpfHeaderMutatorTest`에 user/operator 변경 차단 테스트 보강
3. `XyzQueryEducationMapperSliceTest` 실제 파일 추가 또는 미구현 사유 명확화
4. `CPF_STABILIZATION_REPORT.html`, `specs/기능_구현_매트릭스.html`, `specs/표준_헤더_가이드.html` 정합성 보정

## 3. 이번 제외 범위

아래는 이번 작업 범위가 아니다.

* Git commit, push, branch 생성
* `CPF_NEW_REQUEST.md` 수정
* 별도 수정파일 목록 산출물 생성
* MariaDB 신규 DB 전체 설치 실실행
* ADM runtime smoke 신규 구현
* OpenAPI runtime smoke 신규 구현
* ADM 실제 브라우저 클릭 자동화 신규 구현
* Redis/Kafka/MQ 실 broker 검증
* 센터컷 신규 구현
* 온디맨드 배치 신규 구현
* CMN 파일 업로드/Excel/PDF/파일 생성 공통 기능 신규 구현

제외 항목은 `CPF_STABILIZATION_REPORT.html`에 `추후 이슈`로 분리한다. 단순히 “추후 확인”이라고 쓰지 말고, 왜 이번 범위에서 제외했는지와 다음에 어떤 조건이면 닫을 수 있는지 적는다.

---

# 4. 현재 상태 판정

## 4.1 Trusted Proxy

`CpfHeaderExtractor`가 `CpfTrustedProxyPolicy`를 호출하는 것은 확인되었다.
그러나 `CpfTrustedProxyPolicy`가 `not-an-ip`, `bad-host-name`, `invalid-real-ip`, `999.999.999.999` 같은 값까지 실제 IP 형식으로 검증해 무시하는지는 불명확하다.

현재 테스트는 invalid forwarded value를 무시해야 한다는 기대를 갖고 있으나, 구현은 `null/blank/unknown` 중심으로만 거르는 것으로 보여 구현과 테스트 기대값이 충돌할 수 있다.

현재 상태값: `재확인 필요 / 실패 가능성`

## 4.2 Header Mutator

`CpfHeaderMutator` 구현에는 `X-User-Id`, `X-Operator-Id`가 restricted header로 들어간 것으로 확인되었다.
그러나 `CpfHeaderMutatorTest`에서 아래 테스트는 확인되지 않았다.

* `X-User-Id` 직접 변경 차단
* `x-user-id` 소문자 우회 차단
* `X-Operator-Id` 직접 변경 차단
* `x-operator-id` 소문자 우회 차단
* 변경 실패 시 기존 `TransactionContext` 보존

현재 상태값: `부분 구현 / 테스트 미구현`

## 4.3 EDU Mapper Slice Test

`XyzQueryEducationMapper.xml`과 fixture SQL은 존재한다.
그러나 GitHub push 기준 `XyzQueryEducationMapperSliceTest.java` 파일은 확인되지 않았다. 현재 확인된 `XyzQueryEducationRepositoryTest`는 Mockito mock 기반이다.

즉 “Mapper slice test 추가” 보고는 실제 파일과 불일치한다.

현재 상태값: `실패`

## 4.4 리포트 / 기능 매트릭스 / 표준 헤더 가이드

`CPF_STABILIZATION_REPORT.html`은 미검증 항목을 분리한 점은 개선되었다.
그러나 실제 확인되지 않은 `XyzQueryEducationMapperSliceTest`를 추가했다고 기록한 부분이 있어 실제 파일과 충돌한다.

`specs/표준_헤더_가이드.html`에는 trusted proxy 정책의 핵심 구현 클래스인 `CpfTrustedProxyPolicy`와 invalid IP 처리 기준을 명확히 반영해야 한다.

현재 상태값: `부분 구현 / 불일치 있음`

---

# 5. 작업 상세

## 5.1 `CpfTrustedProxyPolicy` invalid IP 검증 보강

### 수정 대상

* `pfw/src/main/java/cpf/pfw/common/header/CpfTrustedProxyPolicy.java`
* `pfw/src/test/java/cpf/pfw/common/header/CpfHeaderStandardCoverageTest.java`
* `specs/표준_헤더_가이드.html`
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`

### 구현 기준

* forwarded 계열 후보 값은 실제 IP 형식일 때만 client IP 후보로 인정한다.
* `unknown`, blank, hostname, invalid IP는 무시한다.
* IPv4는 octet 범위까지 검증한다.
* IPv6를 지원할 경우 bracket, port 제거 후 유효성을 검증한다.
* IPv6를 이번 범위에서 완전 지원하지 못하면 `추후 이슈`로 분리하고, 현재 구현이 어떤 IPv6 형식을 처리하는지 기록한다.
* `X-Real-IP`도 trusted proxy에서 온 요청일 때만 사용하고, 값이 IP 형식일 때만 사용한다.
* trusted proxy가 아닌 remoteAddr에서 온 요청은 `X-Forwarded-For`, `Forwarded`, `X-Real-IP`, `X-Client-IP`를 신뢰하지 않는다.
* 주석에는 client가 보낸 IP를 그대로 신뢰하면 안 되는 이유를 한글로 작성한다.

### 테스트 기준

아래 테스트를 보강하거나 추가한다.

* `not-an-ip` 무시
* `bad-host-name` 무시
* `invalid-real-ip` 무시
* `999.999.999.999` 무시
* blank 값 무시
* `unknown` 값 무시
* trusted proxy + valid `X-Forwarded-For` 사용
* trusted proxy + invalid `X-Forwarded-For` + valid `X-Real-IP` fallback
* untrusted remote + 모든 forwarding header 무시
* CIDR trusted proxy 적용
* `Forwarded: for=` 값 처리

### 완료 기준

* `CpfTrustedProxyPolicy`에서 실제 IP 유효성 검증 로직이 확인된다.
* invalid IP 관련 테스트가 실제 구현과 일치한다.
* `.\gradlew.bat :pfw:test --offline` 성공 결과를 리포트에 기록한다.
* 실행하지 못하면 성공으로 쓰지 않고, 실패/미검증 사유를 요청 형식대로 기록한다.

### 완료 불인정 기준

* 테스트 기대값만 낮춤
* hostname을 client IP로 인정
* invalid `X-Real-IP`를 fallback으로 사용
* `X-Client-IP`를 계속 우선 신뢰
* 실행하지 않은 테스트를 성공으로 기록

---

## 5.2 `CpfHeaderMutatorTest` 보강

### 수정 대상

* `pfw/src/test/java/cpf/pfw/common/header/CpfHeaderMutatorTest.java`
* `CPF_STABILIZATION_REPORT.html`
* `specs/표준_헤더_가이드.html`, 필요한 경우
* `specs/기능_구현_매트릭스.html`, 필요한 경우

### 테스트 기준

아래 테스트를 추가한다.

* `X-User-Id` 변경 시 예외
* `x-user-id` 소문자 변경 시 예외
* `X-Operator-Id` 변경 시 예외
* `x-operator-id` 소문자 변경 시 예외
* 변경 실패 후 기존 `TransactionContext.userId()` 값 보존
* 변경 실패 후 기존 `TransactionContext.operatorId()` 값 보존
* `X-Customer-No` 변경 허용
* `X-Member-No` 변경 허용
* `X-Tenant-Id` 변경 허용
* `X-Organization-Code` 변경 허용
* `X-Branch-Code` 변경 허용
* `X-Channel-Detail-Code` 변경 허용

### 구현 기준

* 구현 코드가 이미 user/operator를 restricted로 막고 있다면 테스트만 보강한다.
* 테스트 보강 중 구현 결함이 발견되면 구현도 같이 수정한다.
* 차단 실패 시 기존 `TransactionContext`가 훼손되지 않아야 한다.
* 주석에는 인증 주체와 업무 식별자 변경 정책 차이를 한글로 설명한다.

### 완료 기준

* 구현 코드가 아니라 테스트에서 user/operator 차단을 직접 증명한다.
* 대소문자 우회 차단이 테스트된다.
* 실패 시 Context 보존이 테스트된다.
* `.\gradlew.bat :pfw:test --offline` 성공 결과를 리포트에 기록한다.

### 완료 불인정 기준

* transaction id/Auth 차단 테스트만 유지
* user/operator 차단 테스트 없음
* 대소문자 우회 테스트 없음
* Context 보존 테스트 없음
* 실행하지 않은 테스트를 성공으로 기록

---

## 5.3 EDU Mapper slice test 실제 파일 추가 또는 명확한 미구현 기록

### 수정 대상

* `xyz/src/test/java/cpf/xyz/edu/repository/XyzQueryEducationMapperSliceTest.java`
* `xyz/src/test/resources/fixture/xyz_edu_query_mapper_fixture.sql`
* `xyz/build.gradle`, 필요한 경우
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`
* `specs/개발_가이드.html`

### 처리 기준

둘 중 하나를 반드시 선택한다.

## 선택 A: 실제 slice test 구현

* `XyzQueryEducationMapperSliceTest.java` 파일을 실제로 추가한다.
* MyBatis XML을 로딩한다.
* fixture SQL을 테스트에서 사용한다.
* 안전한 테스트 DB 환경변수가 없으면 skip 가능하되 성공으로 기록하지 않는다.
* skip 조건과 실행 명령을 리포트에 남긴다.
* DB fixture 실행 테스트와 정적 XML 안전성 테스트를 구분한다.
* 가능하면 DB가 없어도 실행 가능한 정적 안전성 테스트를 함께 둔다.

### 최소 검증 항목

* 단건 조회
* 목록 조회
* keyword 검색
* status 검색
* offset page
* count
* keyset page
* `NAME_ASC`
* `CREATED_DESC`
* `ID_ASC`
* 잘못된 sort의 repository default 정규화
* limit+1 hasNext
* `mbr_member` 직접 JOIN 없음
* `exs_*` 직접 JOIN 없음
* fixture SQL 사용 여부

## 선택 B: 이번 범위에서 slice test 미구현으로 명확히 기록

파일을 만들지 못하면 아래 형식으로 리포트에 기록한다.

* 대상 기능명: EDU Mapper slice test
* 대상 파일: `XyzQueryEducationMapperSliceTest.java`
* 시도한 작업
* 만들지 못한 정확한 이유
* 현재 상태값: `미구현` 또는 `미검증`
* 다음에 필요한 조치
* 완료로 기록하지 않은 이유

이 경우 “추가했다”, “구조를 추가했다”, “완료했다”라고 쓰지 않는다.

### 완료 기준

* 실제 slice test 파일이 존재하거나, 미구현 사유가 요청 형식대로 기록된다.
* mock repository test를 mapper slice test로 포장하지 않는다.
* `:xyz:test`에서 실행/skip 여부가 명확히 보인다.
* fixture가 테스트에서 사용되는지 기록된다.

### 완료 불인정 기준

* 리포트에만 `XyzQueryEducationMapperSliceTest` 추가라고 기록
* 실제 파일 없음
* Mockito mock test만 존재
* fixture만 있고 테스트에서 사용 안 함
* skip을 성공으로 기록

---

## 5.4 리포트 / 기능 매트릭스 / 표준 헤더 가이드 정합성 보정

### 수정 대상

* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`
* `specs/표준_헤더_가이드.html`
* `specs/개발_가이드.html`, 필요한 경우

### 수정 기준

* 실제 존재하지 않는 파일은 추가했다고 기록하지 않는다.
* `EDU Mapper slice`는 실제 파일 존재, 실행, skip, 미구현을 분리한다.
* `:pfw:test`, `:xyz:test`, `test`, `qualityGate`는 실행한 경우에만 성공으로 기록한다.
* 표준 헤더 가이드의 구현 기준 클래스 또는 client IP 섹션에 `CpfTrustedProxyPolicy`를 명시한다.
* invalid IP 처리 기준을 문서화한다.
* 미검증 항목은 대상 파일/명령/사유/다음 조치/완료 불인정 사유를 포함한다.
* 기능 매트릭스는 완료/부분 구현/미구현/미검증/실패/재확인 필요 상태값을 과장 없이 사용한다.

### 완료 기준

* 리포트와 실제 파일 상태가 일치한다.
* 기능 매트릭스에 EDU Mapper slice test 상태가 정확히 반영된다.
* 표준 헤더 가이드가 trusted proxy 정책과 invalid IP 처리 기준을 반영한다.
* 미검증 항목이 요청 형식대로 기록된다.

### 완료 불인정 기준

* 실제 파일 없는 테스트를 추가했다고 기록
* 미검증을 성공으로 기록
* 표준 헤더 가이드에 trusted proxy 정책 누락
* stale 문구 유지
* “환경 문제”, “추후 확인” 같은 한 줄 사유만 기록

---

# 6. 검증 명령

가능한 범위에서 아래 명령을 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-utf8.ps1 -CheckMojibake
```

실행하지 못한 명령은 성공으로 기록하지 않는다.
실패한 명령은 실패 명령, 실패 원인, 관련 파일, 다음 조치를 리포트에 기록한다.

---

# 7. `CPF_STABILIZATION_REPORT.html` 기록 기준

리포트에는 아래를 기록한다.

* 수정한 파일
* 실제 변경 내용
* 실행한 명령
* 성공/실패/skip 여부
* 실패 시 원인
* 미구현/미검증 사유
* 다음 조치
* 완료로 기록하지 않은 이유
* 추후 이슈

다음은 성공으로 기록하지 않는다.

* 환경변수 없어서 skip된 EDU DB slice test
* 실행하지 않은 MariaDB 신규 DB 설치
* 실행하지 않은 ADM runtime/OpenAPI
* 실행하지 않은 브라우저 클릭
* 실행하지 않은 broker 실연동

---

# 8. 다음 보강 후보

이번 요청이 닫힌 뒤 다음 순서로 진행한다.

1. 안전한 빈 MariaDB 신규 DB 전체 설치 실검증
2. ADM runtime / OpenAPI runtime smoke
3. ADM 주요 화면 브라우저 클릭 자동화
4. 표준 헤더 수신 → context → log → ADM → outbound E2E
5. BAT runtime / batch worker / ghost detection 검증
6. CMN 파일 공통 기능 및 EDU 파일 처리 샘플

   * 파일 업로드
   * Excel/PDF 업로드 검증
   * 파일 생성
   * 다운로드
   * 다운로드 감사
   * 저장소 adapter
   * ADM 파일 처리 이력
   * EDU 교육용 샘플
7. Redis/Kafka/MQ mock/fallback 및 실 broker 전파 분리
8. 센터컷 기본 구현체
9. 최종 기능 매트릭스 재검수
