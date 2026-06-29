# CPF_REWORK_REQUEST: 검수 결과 기반 보정 작업 요청

## 공통 원칙

이번 요청은 Codex에게 전수검수를 맡기는 요청이 아니다.
검수자가 확인한 결함을 기준으로 수정 작업만 수행한다.

공통 기준:

* 요청 파일은 확인용으로만 읽고 작업 대상으로 수정하지 않는다.
* Git commit, push, branch 생성은 하지 않는다.
* 별도 수정파일 목록 산출물은 만들지 않는다.
* 작업 결과는 `CPF_STABILIZATION_REPORT.html`에 기록한다.
* 실행하지 않은 Gradle, MariaDB, OpenAPI, 브라우저 클릭, Redis/Kafka/MQ 실 broker 검증은 성공으로 기록하지 않는다.
* 미구현, 부분 구현, 미검증, 실패, 재확인 필요를 숨기지 않는다.
* 모든 신규/수정 Java, SQL, script에는 한글 주석을 작성한다.
* 주석은 단순 동작 설명이 아니라 왜 필요한지, 운영 의미, 개발자 주의점을 설명한다.

---

# REQUEST_001: 리포트/기능 매트릭스 상태값 정합성 보정

## 목적

`CPF_STABILIZATION_REPORT.html`과 `specs/기능_구현_매트릭스.html`의 상태값을 실제 구현/검증 상태와 맞춘다.

## 현재 문제

* 최근 완료 보고에서는 ADM runtime smoke, 로그 정책 runtime smoke, OpenAPI runtime smoke, 실제 브라우저 클릭 검증이 미검증이라고 했으나, GitHub 리포트에는 일부 runtime smoke가 성공으로 기록되어 있다.
* 기능 매트릭스는 EDU 조회 샘플에 대해 MyBatis Mapper/SQL이 다음 보강 필요처럼 적고 있으나, 실제 `XyzQueryEducationMapper.xml`은 존재한다.
* 기능 매트릭스의 “완료” 상태가 실제 runtime/브라우저/DB 검증까지 포함한 완료인지 불명확하다.

## 수정 대상

* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`
* `README.md`, 필요한 경우만

## 수정 기준

상태값은 아래 기준으로 정리한다.

* 완료: 소스, SQL, 문서, 테스트, smoke 또는 runtime 검증 증거가 모두 있음
* 부분 구현: 핵심 구조는 있으나 운영 검증, 브라우저, DB, 외부 연동, E2E 일부가 남음
* 미구현: 실체 없음
* 미검증: 구현은 있으나 실행하지 않음
* 실패: 실행했고 실패함
* 재확인 필요: 로컬/브랜치/환경 차이로 추가 확인 필요

## 반드시 반영할 판정

* ADM 브라우저 실제 클릭 검증은 미검증으로 기록한다.
* 실제 MariaDB 신규 DB 전체 설치는 미검증으로 기록한다.
* OpenAPI runtime smoke를 실행하지 않았다면 미검증으로 기록한다.
* ADM runtime smoke를 실행하지 않았다면 미검증으로 기록한다.
* 로그 정책 runtime smoke를 실행하지 않았다면 미검증으로 기록한다.
* EDU는 “MyBatis Mapper/XML 일부 구현, Mapper slice test/fixture 미구현 또는 미검증”으로 기록한다.
* 정적 smoke와 runtime smoke, 브라우저 클릭 검증을 분리한다.

## 완료 기준

* 리포트와 기능 매트릭스의 상태값이 실제 구현/검증과 일치한다.
* 실행하지 않은 검증이 성공으로 남아 있지 않다.
* Codex 완료 보고와 실제 파일 상태가 다른 항목은 정정되어 있다.

## 완료 불인정 기준

* 미실행 검증을 성공으로 기록
* 정적 marker 검사를 브라우저 클릭 검증으로 기록
* Mapper/XML 존재와 Mapper slice test 완료를 혼동
* MariaDB 실실행 없이 신규 DB 설치 성공으로 기록

---

# REQUEST_002: SQL/Flyway/all_install 정합성 보정

## 목적

EDU 조회 샘플에서 사용하는 `cmn_edu_query_item`이 Flyway, split SQL, all_install, all_install_and_smoke에 일관되게 반영되도록 수정한다.

## 현재 확인된 문제

* `V14__edu_query_sample.sql`에는 `cmn_edu_query_item` 생성 및 seed가 있다.
* `00_all_install.sql`과 `00_all_install_and_smoke.sql`에는 `DROP TABLE IF EXISTS cmnDB.cmn_edu_query_item`은 있으나, `CREATE TABLE IF NOT EXISTS cmn_edu_query_item`과 seed가 확인되지 않는다.
* 이 상태면 신규 DB 전체 설치 후 EDU Mapper가 참조하는 테이블이 없을 수 있다.

## 수정 대상

* `specs/sql/20_cmn_schema.sql`
* `specs/sql/migration/flyway/V14__edu_query_sample.sql`
* `specs/sql/00_all_install.sql`
* `specs/sql/00_all_install_and_smoke.sql`
* `scripts/check-sql-standard.ps1`
* `specs/SQL_가이드.html`
* `CPF_STABILIZATION_REPORT.html`
* `specs/기능_구현_매트릭스.html`

## 구현 기준

* `cmn_edu_query_item` 생성 SQL은 split SQL, Flyway, all_install에 모두 있어야 한다.
* seed 데이터도 Flyway와 all_install 기준이 일관되어야 한다.
* seed는 idempotent해야 한다.
* `00_all_install_and_smoke.sql`은 cleanup 위험을 명확히 안내해야 한다.
* `check-sql-standard.ps1`는 split SQL에는 있는데 all_install에는 없는 테이블을 잡을 수 있어야 한다.

## 검증 명령

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-feature-evidence.ps1
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
```

MariaDB 실실행은 안전한 빈 DB 또는 사용자 명시 승인 후에만 수행한다.

## 완료 기준

* `cmn_edu_query_item`이 split SQL, Flyway, all_install, all_install_and_smoke에 모두 존재한다.
* all_install에서 create와 seed가 확인된다.
* check-sql-standard가 누락을 잡을 수 있다.
* MariaDB 실실행을 하지 않았다면 미검증으로 기록한다.

## 완료 불인정 기준

* Flyway에만 있고 all_install에 없음
* drop만 있고 create/seed 없음
* SQL 파일 존재만 보고 신규 DB 설치 성공으로 기록
* cleanup 위험 안내 없이 all_install_and_smoke 실행

---

# REQUEST_003: EDU MyBatis Mapper slice test 및 fixture 보강

## 목적

XYZ EDU 조회 샘플을 mock 기반 unit test가 아니라 실제 MyBatis Mapper/XML/fixture 기반으로 검증 가능한 실전형 샘플로 보강한다.

## 현재 확인된 상태

* `XyzQueryEducationMapper.xml`은 존재한다.
* `cmn_edu_query_item`을 조회한다.
* 단건, 목록, offset page, keyset page, 검색, 정렬 whitelist SQL 구조가 있다.
* 현재 확인된 repository test는 Mockito mock 기반이다.
* 실제 MyBatis XML과 DB fixture를 실행하는 Mapper slice test는 확인되지 않는다.

## 수정 대상

* `xyz/src/test/java/cpf/xyz/edu/...`
* `xyz/src/test/resources/...`
* `xyz/src/main/resources/mybatis/mapper/xyz/edu/XyzQueryEducationMapper.xml`, 필요한 경우만
* `specs/개발_가이드.html`
* `specs/기능_구현_매트릭스.html`
* `CPF_STABILIZATION_REPORT.html`

## 구현 기준

* Mapper slice test를 추가한다.
* 테스트용 fixture SQL을 둔다.
* 단건 조회를 검증한다.
* 목록 조회를 검증한다.
* offset page 조회를 검증한다.
* keyset page 조회를 검증한다.
* 검색 조건을 검증한다.
* 정렬 whitelist를 검증한다.
* limit+1 hasNext 판단을 검증한다.
* 없는 데이터 표준 오류는 Service test에서 검증한다.
* 사용자 입력 정렬 문자열을 SQL에 직접 넣지 않는다.
* `${sort}` 같은 직접 삽입은 금지한다.

## 완료 기준

* MyBatis XML이 실제 테스트에서 실행된다.
* fixture 기반 테스트가 있다.
* mock unit test와 mapper slice test가 구분된다.
* EDU 문서와 기능 매트릭스가 실제 상태에 맞게 갱신된다.

## 완료 불인정 기준

* mock 테스트만 추가
* Mapper XML은 있지만 실제 실행 테스트 없음
* fixture 없이 성공 기록
* 정렬 whitelist 없이 사용자 입력 직접 삽입

---

# REQUEST_004: 표준 헤더 보안/신뢰 정책 보강

## 목적

표준 헤더가 문서/상수/추출/검증/전파에만 머물지 않고, 보안과 감사 기준에 맞게 통제되도록 보강한다.

## 현재 확인된 문제

* `CpfHeaderExtractor`는 `X-Client-IP`, `X-Real-IP`, `request.getRemoteAddr()` 우선순위로 client IP를 잡는다.
* trusted proxy/gateway/LB/WAF 기준의 신뢰 체인 검증은 부족하다.
* `CpfHeaderMutator`는 `X-User-Id`와 `X-Operator-Id` 보정을 허용한다.
* `X-User-Id`, `X-Operator-Id`는 인증/감사 주체이므로 업무 코드가 임의 변경하면 위험하다.

## 수정 대상

* `CpfHeaderExtractor`
* `CpfHeaderMutator`
* `CpfHeaderSpecs`
* `TransactionHeader`
* `TransactionContext`
* `specs/표준_헤더_가이드.html`
* 관련 테스트
* `CPF_STABILIZATION_REPORT.html`

## 구현 기준

### Client IP

* client가 직접 보낸 `X-Client-IP`를 그대로 신뢰하지 않는다.
* trusted proxy 목록을 설정으로 둔다.
* trusted proxy에서 온 요청일 때만 `X-Forwarded-For`, `Forwarded`, `X-Real-IP`를 해석한다.
* trusted proxy가 아니면 `request.getRemoteAddr()`를 우선한다.
* resolved client IP와 raw inbound IP 계열 헤더를 분리한다.
* ADM에서는 raw inbound header와 resolved header를 구분한다.

### User/Operator Mutator

* `X-User-Id`와 `X-Operator-Id`는 기본적으로 업무 코드 임의 보정 금지로 전환한다.
* 필요하면 별도 명시 API와 감사 로그를 통해서만 보정한다.
* `X-Customer-No`, `X-Member-No`, `X-Tenant-Id`, `X-Organization-Code`, `X-Branch-Code`, `X-Channel-Detail-Code`는 업무 보정 가능 후보로 둔다.
* 변경 가능/금지 헤더 목록을 문서와 테스트에 반영한다.

## 테스트 기준

* trusted proxy가 아닌 요청에서 `X-Forwarded-For`를 무시하는 테스트
* trusted proxy 요청에서 `X-Forwarded-For`를 해석하는 테스트
* `X-User-Id` 변경 차단 테스트
* `X-Operator-Id` 변경 차단 테스트
* customer/member/tenant/org/branch 보정 허용 테스트
* Authorization/X-Api-Key/token류 원문 미저장 테스트

## 완료 기준

* trusted proxy 기준이 코드/문서/테스트에 반영된다.
* user/operator 헤더 변경 정책이 명확하다.
* 민감 헤더 원문 미저장이 테스트된다.
* ADM raw/resolved header 구분 기준이 문서화된다.

## 완료 불인정 기준

* client IP 헤더를 그대로 신뢰
* user/operator를 업무 코드가 자유롭게 변경 가능
* 문서만 있고 테스트 없음
* 민감 헤더 원문 로그 저장 가능성 방치

---

# REQUEST_005: 표준 헤더 E2E 전파 검증 보강

## 목적

표준 헤더가 수신 → context → 로그 detail → ADM 응답 → outbound 전파까지 이어지는지 테스트로 증명한다.

## 수정 대상

* PFW header 관련 test
* RestClient/WebClient test
* ADM log detail test
* smoke script, 필요한 경우
* `CPF_STABILIZATION_REPORT.html`

## 구현 기준

* 필수 헤더 누락 시 표준 오류
* `X-Transaction-Id` 형식 오류 시 표준 오류
* 정상 요청 시 context에 주요 헤더가 들어감
* LoggingAspect detail에 inbound/resolved/outbound/response headers 저장
* ADM log detail API가 각 header map을 별도 반환
* WebClient 하위 mock API가 실제 전파 헤더를 수신
* RestClient 하위 mock API가 실제 전파 헤더를 수신
* 민감 헤더는 outbound 전파 여부와 로그 저장 여부를 구분

## 완료 기준

* 하위 mock API 수신 기준으로 header 자동 전파가 검증된다.
* ADM log detail 구조가 테스트된다.
* 민감 헤더 마스킹이 테스트된다.
* 수신/해석/전파/응답 헤더가 분리 검증된다.

## 완료 불인정 기준

* header map 생성 unit test만 있고 실제 수신 측 검증 없음
* ADM 화면/응답 구조 미검증
* 민감 헤더 원문 저장 여부 미검증

---

# REQUEST_006: ADM Runtime/OpenAPI/브라우저 검증 분리 및 실제 검증

## 목적

ADM 정적 marker 검증과 실제 runtime/OpenAPI/브라우저 클릭 검증을 분리하고, 가능한 범위에서 실제 검증을 수행한다.

## 현재 문제

* 정적 UI marker smoke와 실제 브라우저 클릭 검증이 혼동될 위험이 있다.
* 현재 기능 매트릭스도 ADM 브라우저 실제 클릭 자동화를 미검증으로 기록하고 있다.
* 최근 완료 보고에서는 ADM runtime/OpenAPI도 미검증으로 남겼다.

## 수정 대상

* `scripts/smoke-adm-ui.ps1`
* `scripts/smoke-adm-runtime.ps1`
* `specs/운영_매뉴얼.html`
* `specs/관리자_가이드.html`
* `specs/기능_구현_매트릭스.html`
* `CPF_STABILIZATION_REPORT.html`

## 구현/검증 기준

* `smoke-adm-ui.ps1`는 정적 marker 검사임을 명확히 한다.
* 실제 브라우저 클릭 검증은 별도 script 또는 수동 검증 절차로 분리한다.
* ADM runtime을 실제 기동한다.
* `/adm/api/health` 확인
* OpenAPI JSON 접근 확인
* 거래 로그 목록/상세 API 확인
* 수신/해석/전파/응답 헤더 응답 확인
* 로그 정책 API 확인
* 캐시 summary API 확인
* 배치 관제 API 확인
* 실제 브라우저 클릭을 수행하지 못하면 미검증으로 기록한다.

## 완료 기준

* 정적 smoke, runtime smoke, 브라우저 클릭 검증이 문서/리포트에서 분리된다.
* ADM runtime/OpenAPI 실행 결과가 있으면 성공으로 기록한다.
* 실행하지 않은 항목은 미검증으로 남긴다.

## 완료 불인정 기준

* 정적 marker smoke를 브라우저 클릭 성공으로 기록
* OpenAPI 파일만 보고 runtime 성공 기록
* ADM 기동 없이 API 성공 기록
* 실패 또는 미실행을 숨김

---

# REQUEST_007: MariaDB 신규 DB 전체 설치 실검증

## 목적

신규 빈 MariaDB에서 전체 설치 SQL이 실제 재현 가능한지 확인한다.

## 주의

`00_all_install_and_smoke.sql`은 cleanup을 수행하므로 기존 DB 데이터 삭제 위험이 있다.
반드시 안전한 빈 DB 또는 사용자 명시 승인된 DB에서만 실행한다.

## 범위

* 빈 DB 생성
* root/migration/app 계정 권한 확인
* `00_all_install.sql` 실행
* `00_all_install_and_smoke.sql` 실행 여부는 승인 후 수행
* `cmn_edu_query_item` 생성 확인
* seed 재실행 idempotent 확인
* app 계정 DDL 차단 확인
* ADM 기동 확인
* BAT 기동 확인
* 기본 smoke 실행
* 실패 SQL/라인/원인 기록

## 완료 기준

* 신규 빈 DB에서 전체 설치 성공
* `cmn_edu_query_item` 포함 신규 테이블 생성 확인
* seed 재실행 안전성 확인
* app 계정 DDL 차단 확인
* 실행 로그를 리포트에 기록

## 완료 불인정 기준

* 실제 MariaDB 실행 없이 성공 기록
* 기존 DB에서만 확인
* cleanup 위험 안내 없이 실행
* 실패 SQL 숨김

---

# REQUEST_008: 기능 매트릭스 기반 누락 기능 보강 후보 재정렬

## 목적

지금까지 누적된 기능의 다음 개발 순서를 실제 구현 상태 기준으로 재정렬한다.

## 기준

기능 매트릭스에서 이미 남은 항목으로 잡힌 아래 기능을 우선 재정렬한다.

* ghost 자동 감지 scheduler와 장애 시나리오 테스트
* 로그 정책 다중 인스턴스 broker 전파와 운영 UX 고도화
* ADM 거래/오류/감사 통합 관제 UX 고도화
* ADM 브라우저 실제 클릭 자동화
* ondemand batch와 parameter 검증 UI
* center-cut 기본 구현체와 EDU 샘플

## 산출 기준

* 지금 바로 수정할 항목
* 다음 단계 구현 항목
* 장기 보강 항목
* 실환경 필요 항목
* 미검증 항목

## 완료 기준

* 다음 개발 순서가 기능 매트릭스와 리포트에 일치하게 반영된다.
* 완료/부분 구현/미구현/미검증이 과장 없이 정리된다.
