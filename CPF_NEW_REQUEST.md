# CPF_REWORK_REQUEST_AFTER_CHATGPT_REVIEW

## 1. 작업 목적

이번 작업은 신규 기능 확장이 아니라 ChatGPT 검수에서 확인된 실제 파일 불일치와 미완성 항목을 수정하는 작업이다.

Codex 완료 보고와 실제 push 파일이 충돌한 항목을 우선 보정한다.
Codex에게 전수 검수를 요청하는 것이 아니다. 아래에 명시된 결함을 수정하고, 수정하지 못한 항목은 정해진 형식으로 미완료/미검증/실패 사유를 `CPF_STABILIZATION_REPORT.html`에 기록한다.

## 2. 이번 범위

이번 범위는 아래 5개로 한정한다.

1. `CpfHeaderExtractor`에 `CpfTrustedProxyPolicy` 실제 연결
2. `CpfHeaderMutator`에서 `X-User-Id`, `X-Operator-Id` 변경 차단
3. EDU Mapper slice test 실제 구현 및 fixture 사용 구조 보강
4. `cmn_edu_query_item`의 split SQL / Flyway / baseline / all_install / all_install_and_smoke 정합성 보정
5. `CPF_STABILIZATION_REPORT.html`, `specs/기능_구현_매트릭스.html`, 관련 가이드의 실제 상태 반영

## 3. 이번 제외 범위

아래는 이번 작업 범위가 아니다.

* Git commit, push, branch 생성
* `CPF_NEW_REQUEST.md` 수정
* 별도 수정파일 목록 산출물 생성
* Redis/Kafka/MQ 실 broker 구현
* ADM 브라우저 클릭 자동화 신규 구현
* ADM runtime/OpenAPI smoke 신규 구현
* 센터컷 신규 구현
* 온디맨드 배치 신규 구현
* MariaDB 신규 DB 전체 설치 실실행

단, 이번 범위에서 제외하는 항목은 `CPF_STABILIZATION_REPORT.html`에 `추후 이슈`로 분리하고, 왜 이번 범위에서 제외했는지와 다음에 필요한 조건을 기록한다.

## 4. 현재 상태 판정

ChatGPT 검수 기준 현재 상태는 아래와 같다.

### 4.1 Trusted Proxy

`CpfTrustedProxyPolicy` 클래스는 존재한다.
그러나 `CpfHeaderExtractor`가 실제로 `CpfTrustedProxyPolicy`를 호출하지 않고, `X-Client-IP`, `X-Real-IP`, `request.getRemoteAddr()` 순서로 client IP를 선택하는 구조로 확인되었다.

따라서 “client IP를 trusted proxy 기준으로만 해석한다”는 완료 보고는 실제 파일과 충돌한다.

현재 상태값: `실패`

### 4.2 User / Operator Mutator

`CpfHeaderMutator`는 `X-User-Id`, `X-Operator-Id`를 변경 차단하지 않고, `builder.userId(value)`, `builder.operatorId(value)`로 변경을 허용하는 구조로 확인되었다.

따라서 “X-User-Id / X-Operator-Id 업무 코드 변경 차단” 완료 보고는 실제 파일과 충돌한다.

현재 상태값: `실패`

### 4.3 EDU Mapper slice test

`XyzQueryEducationMapper.xml`과 fixture SQL은 존재한다.
그러나 실제 MyBatis XML과 DB fixture를 실행하는 `XyzQueryEducationMapperSliceTest` 또는 동등한 slice test 파일은 확인되지 않았다. 현재 확인된 repository test는 Mockito mock 기반이다.

현재 상태값: `부분 구현 / 미검증`

### 4.4 SQL 정합성

`20_cmn_schema.sql`과 Flyway V14에는 `cmn_edu_query_item`이 확인된다.
그러나 `00_all_install.sql`에서는 `DROP TABLE IF EXISTS cmnDB.cmn_edu_query_item`만 확인되고, `CREATE TABLE IF NOT EXISTS cmn_edu_query_item`과 seed가 확인되지 않았다.

`check-sql-standard.ps1`는 이런 누락을 잡아야 하는 구조인데 성공으로 보고되었으므로, 실제 SQL 상태와 실행 보고가 충돌한다.

현재 상태값: `실패 / 재확인 필요`

### 4.5 리포트 / 기능 매트릭스

`CPF_STABILIZATION_REPORT.html`은 HTML 형식은 정상이나, 위 항목들에 대해 실제 파일과 다른 완료 주장이 남아 있다.
`specs/기능_구현_매트릭스.html`에도 EDU 관련 오래된 문구가 남아 있어 실제 구현 상태와 일부 불일치한다.

현재 상태값: `부분 구현 / 재확인 필요`

---

# 5. 구현/수정 대상

## 5.1 `CpfHeaderExtractor` trusted proxy 실제 연결

### 수정 대상

* `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderExtractor.java`
* `pfw/src/main/java/cpf/pfw/common/header/CpfTrustedProxyPolicy.java`
* 관련 test
* `specs/표준_헤더_가이드.html`
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`

### 구현 기준

* `CpfHeaderExtractor`는 client IP 산정 시 반드시 `CpfTrustedProxyPolicy`를 사용한다.
* trusted proxy가 아닌 remoteAddr에서 온 요청은 `X-Client-IP`, `X-Forwarded-For`, `Forwarded`, `X-Real-IP`를 신뢰하지 않는다.
* trusted proxy에서 온 요청일 때만 `X-Forwarded-For`, `Forwarded`, `X-Real-IP`를 정책 순서에 따라 해석한다.
* raw inbound header와 resolved client IP는 개념상 분리한다.
* 주석에는 client가 보낸 IP를 그대로 신뢰하면 안 되는 이유를 한글로 설명한다.

### 테스트 기준

아래 테스트를 추가한다.

* remoteAddr가 trusted proxy가 아닐 때 `X-Client-IP`가 있어도 `remoteAddr`가 resolved client IP가 되는지
* remoteAddr가 trusted proxy일 때 `X-Forwarded-For` 첫 유효 IP를 사용하는지
* `Forwarded` 헤더의 `for=` 값을 해석하는지
* `unknown`, blank, 잘못된 header 값은 무시하는지
* CIDR trusted proxy가 적용되는지
* `CpfHeaderExtractor`가 실제로 `CpfTrustedProxyPolicy`를 통해 client IP를 설정하는지

### 완료 기준

* `CpfHeaderExtractor` 코드에서 `CpfTrustedProxyPolicy` 호출이 확인된다.
* trusted/untrusted proxy 테스트가 존재한다.
* 표준 헤더 가이드와 리포트에 실제 구현 기준이 반영된다.

### 완료 불인정 기준

* `CpfTrustedProxyPolicy` 클래스만 있고 `CpfHeaderExtractor`에 연결되지 않음
* `X-Client-IP`를 계속 우선 신뢰
* 문서만 수정하고 테스트 없음

---

## 5.2 `X-User-Id`, `X-Operator-Id` 변경 차단

### 수정 대상

* `pfw/src/main/java/cpf/pfw/common/header/CpfHeaderMutator.java`
* 관련 test
* `specs/표준_헤더_가이드.html`
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`

### 구현 기준

* `X-User-Id`는 인증 주체이므로 업무 코드에서 임의 변경할 수 없다.
* `X-Operator-Id`는 ADM 운영 조치자이므로 업무 코드에서 임의 변경할 수 없다.
* 두 헤더는 `RESTRICTED_HEADERS` 또는 동등한 차단 정책에 포함한다.
* `X-Customer-No`, `X-Member-No`, `X-Tenant-Id`, `X-Organization-Code`, `X-Branch-Code`, `X-Channel-Detail-Code`는 업무 보정 가능 후보로 둔다.
* `X-Correlation-Id`, `X-Idempotency-Key`는 변경 허용 여부를 명확히 재검토한다. 허용하면 사유를 문서화하고, 금지하면 restricted에 포함한다.
* 변경 차단 실패 시 표준 예외를 발생시킨다.
* 주석에는 인증 주체와 업무 식별자 변경 정책 차이를 설명한다.

### 테스트 기준

아래 테스트를 추가한다.

* `X-User-Id` 변경 시 예외
* `X-Operator-Id` 변경 시 예외
* `X-Customer-No` 변경 허용
* `X-Member-No` 변경 허용
* `X-Tenant-Id`, `X-Organization-Code`, `X-Branch-Code` 변경 허용
* `X-Channel-Detail-Code` 변경 허용
* restricted header 대소문자 차이 처리
* 변경 차단 시 기존 TransactionContext가 훼손되지 않는지

### 완료 기준

* 실제 `CpfHeaderMutator`에서 user/operator 변경이 차단된다.
* 테스트가 존재한다.
* 표준 헤더 가이드에 변경 가능/금지 헤더가 반영된다.

### 완료 불인정 기준

* 보고서에만 차단했다고 기록
* user/operator 변경 코드가 남아 있음
* 테스트 없이 구현만 변경

---

## 5.3 EDU Mapper slice test 실제 구현

### 수정 대상

* `xyz/src/test/java/cpf/xyz/edu/...`
* `xyz/src/test/resources/fixture/xyz_edu_query_mapper_fixture.sql`
* `xyz/build.gradle`, 필요한 경우만
* `specs/개발_가이드.html`
* `specs/기능_구현_매트릭스.html`
* `CPF_STABILIZATION_REPORT.html`

### 구현 기준

* `XyzQueryEducationMapperSliceTest` 또는 동등한 이름의 실제 Mapper slice test를 추가한다.
* MyBatis XML이 실제로 로딩되어 실행되어야 한다.
* fixture SQL을 실행해 `cmn_edu_query_item` 테스트 데이터를 구성한다.
* 안전한 테스트 DB 환경변수가 없으면 skip 처리할 수 있으나, skip은 성공으로 기록하지 않는다.
* skip 조건, 필요한 환경변수, 실행 명령을 문서와 리포트에 명확히 적는다.
* 가능하면 별도 Gradle task로 DB 기반 mapper 검증을 분리한다.

### 테스트 기준

아래를 검증한다.

* 단건 조회
* 목록 조회
* keyword 검색
* status 검색
* offset page
* count
* keyset page
* sort whitelist: ID_ASC
* sort whitelist: NAME_ASC
* sort whitelist: CREATED_DESC
* 잘못된 sort는 repository 단에서 default로 정규화
* limit+1 hasNext 판단
* mbr_member, exs_* 직접 JOIN 없음

### 완료 기준

* 실제 slice test 파일이 존재한다.
* fixture SQL이 slice test에서 사용된다.
* 실행 가능한 환경에서는 MyBatis XML이 실제로 실행된다.
* 환경 부재로 skip되면 `미검증`으로 기록한다.
* repository mock test와 mapper slice test가 구분된다.

### 완료 불인정 기준

* Mockito mock test만 추가
* fixture만 있고 테스트에서 사용 안 함
* skip을 성공으로 기록
* “slice test 구조”라고 쓰고 실제 파일 없음

---

## 5.4 SQL / Flyway / all_install 정합성 보정

### 수정 대상

* `specs/sql/20_cmn_schema.sql`
* `specs/sql/migration/flyway/V14__edu_query_sample.sql`
* `specs/sql/migration/flyway/V1__cpf_baseline_install.sql`
* `specs/sql/00_all_install.sql`
* `specs/sql/00_all_install_and_smoke.sql`
* `scripts/check-sql-standard.ps1`
* `specs/SQL_가이드.html`
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`

### 구현 기준

* `cmn_edu_query_item` 생성 SQL은 split SQL, Flyway, baseline migration, all_install, all_install_and_smoke에 모두 일관되게 존재해야 한다.
* seed 데이터도 필요한 경우 all_install 계열에 일관되게 포함한다.
* seed는 idempotent해야 한다.
* cleanup은 명확히 안내한다.
* `check-sql-standard.ps1`는 split SQL에 있는 테이블이 all_install/baseline에 없으면 실패해야 한다.
* `check-sql-standard.ps1` 실행 결과와 실제 파일 상태가 충돌하지 않아야 한다.

### 검증 명령

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-feature-evidence.ps1
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
```

MariaDB 신규 DB 실실행은 안전한 빈 DB 또는 사용자 명시 승인 후에만 수행한다.

### 완료 기준

* `00_all_install.sql`에서 `CREATE TABLE IF NOT EXISTS cmn_edu_query_item`이 확인된다.
* `00_all_install_and_smoke.sql`에서도 동일 테이블 생성이 확인된다.
* baseline migration에도 누락이 없다.
* `check-sql-standard.ps1`가 성공한다.
* 성공 보고에는 실제 실행 명령과 결과가 `CPF_STABILIZATION_REPORT.html`에 기록된다.

### 완료 불인정 기준

* drop만 있고 create 없음
* Flyway에만 있고 all_install에 없음
* check script가 실패해야 할 상태인데 성공으로 기록
* MariaDB 실행 없이 신규 DB 설치 성공으로 기록

---

## 5.5 리포트와 기능 매트릭스 정합성 보정

### 수정 대상

* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`
* `README.md`, 필요한 경우만
* `specs/개발_가이드.html`, 필요한 경우만
* `specs/표준_헤더_가이드.html`, 필요한 경우만

### 수정 기준

* 구현됨과 실제 검증됨을 분리한다.
* `CpfTrustedProxyPolicy`는 “클래스 추가”와 “Extractor 실제 연결”을 분리해 기록한다.
* `CpfHeaderMutator`는 user/operator 차단 여부를 실제 파일 기준으로 기록한다.
* EDU는 Mapper/XML/fixture 존재와 DB slice test 실행 여부를 분리한다.
* `check-sql-standard` 성공 여부는 실제 SQL 상태와 맞게 기록한다.
* 실행하지 않은 Gradle/MariaDB/OpenAPI/browser/broker는 미검증으로 둔다.
* 기능 매트릭스의 stale 문구를 제거한다.

### 완료 기준

* 리포트에 실제 파일 상태와 다른 주장이 남아 있지 않다.
* 기능 매트릭스가 현재 구현 상태와 일치한다.
* 완료/부분 구현/미검증/실패/재확인 필요가 과장 없이 구분된다.

### 완료 불인정 기준

* 실제 파일과 다른 완료 주장 유지
* skip된 테스트를 성공으로 기록
* 정적 smoke를 runtime/browser 검증으로 기록
* stale 문구 방치

---

# 6. 미완료 / 미검증 / 실패 기록 기준

이번 요청 항목 중 구현하지 못한 것, 검증하지 못한 것, 실패한 것은 완료로 기록하지 않는다.

단순히 “환경 문제”, “추후 확인”, “로컬 확인 필요”라고만 쓰지 말고, `CPF_STABILIZATION_REPORT.html`에 아래 형식으로 기록한다.

* 대상 기능명
* 대상 파일 또는 명령
* 시도한 작업
* 완료하지 못한 정확한 이유
* 현재 상태값: `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`
* 다음에 필요한 조치
* 완료로 기록하지 않은 이유

합당한 사유로 이번 범위에서 제외하는 항목은 `추후 이슈`로 분리한다.
다만 왜 이번 범위에서 제외했는지, 다음에 어떤 조건이면 닫을 수 있는지 반드시 적는다.

합당하지 않은 미완료는 아래 중 하나로 대응한다.

* 대안 구현
* 우회 검증
* 재검증 명령 추가
* 추가 테스트 작성

불확실한 상태를 완료로 포장하면 해당 작업은 실패로 본다.

---

# 7. `CPF_STABILIZATION_REPORT.html` 기록 기준

리포트에는 아래를 기록한다.

* 수정한 기능
* 수정한 파일
* 실제 변경 내용
* 실행한 검증 명령
* 검증 결과
* 실패한 검증
* 미검증 항목과 사유
* 추후 이슈
* 완료로 인정하지 않은 항목

다음은 성공으로 기록하지 않는다.

* 환경변수 없어서 skip된 EDU DB slice test
* 실행하지 않은 MariaDB 신규 DB 설치
* 실행하지 않은 ADM runtime/OpenAPI
* 실행하지 않은 브라우저 클릭
* 실행하지 않은 broker 실연동

---

# 8. 다음 보강 후보

이번 재작업이 끝난 뒤 다음 순서로 진행한다.

1. 안전한 빈 MariaDB 신규 DB 전체 설치 실검증
2. ADM runtime 기동 및 OpenAPI runtime 접근 검증
3. ADM 주요 API runtime smoke
4. ADM 주요 화면 브라우저 클릭 자동화
5. 표준 헤더 수신 → context → log → ADM → outbound 전파 E2E
6. BAT runtime / batch worker / ghost detection 검증
7. Redis/Kafka/MQ mock/fallback 및 실 broker 전파 분리
8. 센터컷 기본 구현체
9. 최종 기능 매트릭스 재검수
