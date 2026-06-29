# CPF_NEXT_REQUESTS_AFTER_HEADER_IMPLEMENTATION

## 공통 원칙

이번 요청들은 Codex의 “완료 보고”를 그대로 믿지 않고 실제 소스, SQL, 테스트, smoke, 문서, 리포트 기준으로 재검수한다.

공통 기준:

```text
- 요청 파일은 확인용으로만 읽고 작업 대상으로 수정하지 않는다.
- Git commit, push, branch 생성은 하지 않는다.
- 별도 수정파일 목록 산출물은 만들지 않는다.
- 작업 결과는 CPF_STABILIZATION_REPORT.html에 기록한다.
- 실행하지 않은 검증은 성공으로 기록하지 않는다.
- 실패/미구현/미검증은 분리 기록한다.
- 모든 신규/수정 Java, SQL, script에는 class/method/주요 라인·블록 단위 한글 주석을 작성한다.
- 주석은 단순 동작 설명이 아니라 왜 필요한지, 운영 시 의미, 개발자 주의점을 설명한다.
```

검증 명령은 가능한 범위에서 수행한다.

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :bat:test --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
.\gradlew.bat :adm:bootJar --offline
.\gradlew.bat :bat:bootJar --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\smoke-log-policy-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\check-sql-standard.ps1
```

---

# CPF_REQUEST_014_REVIEW: 표준 헤더 구현 검수 및 누락 보강

## 목적

이전 작업에서 추가된 PFW 표준 헤더 구현이 실제 CPF 표준에 맞는지 검수하고, 누락/미검증/문서 불일치 항목을 보강한다.

## 이번 범위

```text
- CpfHeaderNames 검수
- CpfHeaderSpecs 검수
- CpfHeaderExtractor 검수
- CpfInboundHeaderValidator 검수
- CpfHeaderMutator 검수
- CpfHeaderPropagator 검수
- CpfHeaderMasker 검수
- TransactionHeader 확장 검수
- TransactionContext 조회 API 검수
- WebClient/RestClient outbound 자동 전파 검수
- LoggingAspect inbound/resolved/outbound/response headers 저장 검수
- ADM 로그 상세 API/UI 탭 분리 검수
- 표준_헤더_가이드.html과 실제 구현 일치 여부 검수
- CPF_STABILIZATION_REPORT.html에 증거와 미검증 사유 기록
```

## 반드시 재확인할 헤더

아래 헤더는 누락 없이 문서, 상수, 스펙, extractor, validator, masker, propagator, ADM 표시 기준에서 확인한다.

```text
거래/호출:
- X-Transaction-Id
- X-Parent-Transaction-Id
- X-Original-Transaction-Id
- X-Request-Id
- X-External-Request-Id
- X-Correlation-Id
- X-Idempotency-Key
- Idempotency-Key

추적:
- X-Trace-Id
- X-Span-Id
- X-Parent-Span-Id
- traceparent
- tracestate

채널:
- X-Original-Channel-Code
- X-Channel-Code
- X-Channel-Detail-Code

주체:
- X-User-Id
- X-Operator-Id
- X-Customer-No
- X-Member-No
- X-Tenant-Id
- X-Organization-Code
- X-Branch-Code

IP/위치:
- X-Client-IP
- X-Forwarded-For
- Forwarded
- X-Real-IP
- X-Client-Country-Code
- X-Client-Region-Code
- X-Client-Timezone

호출자:
- X-Client-App-Id
- X-Client-Version
- X-Caller-Service
- X-Caller-Instance-Id
- User-Agent

보안:
- Authorization
- X-Api-Key
- X-Request-Signature
- X-Request-Timestamp
- X-Nonce
```

## 검수 기준

```text
- X-User-Id와 X-Customer-No 의미가 섞이지 않아야 한다.
- X-Member-No와 X-Customer-No가 분리되어야 한다.
- X-Operator-Id는 ADM 운영 조치자 기준이어야 한다.
- X-Original-Channel-Code와 X-Channel-Code가 분리되어야 한다.
- X-Client-IP는 client 입력값을 그대로 신뢰하지 않고 trusted proxy/gateway/LB/WAF 기준을 문서화해야 한다.
- X-Client-Country-Code는 ISO 3166-1 alpha-2 기준이어야 한다.
- Authorization, X-Api-Key, token류는 원문 로그 저장 금지여야 한다.
- 개발자가 outbound 호출마다 header.add(...)를 반복하지 않아야 한다.
- CpfWebClient/CpfRestClient 또는 wrapper/interceptor 사용 시 표준 헤더가 자동 전파되어야 한다.
```

## 테스트/스모크 대상

```text
- 필수 헤더 정상 요청 통과
- 필수 헤더 누락 시 표준 오류
- X-Transaction-Id 형식 오류 시 표준 오류
- context 조회 API로 transactionId/traceId/channel/user/customer/member/clientIp 확인
- mutator로 customerNo/memberNo 보정 가능
- transactionId/originalTransactionId/traceId/originalChannelCode 임의 변경 제한
- outbound 호출 시 하위 mock API가 표준 헤더를 실제로 수신하는지 확인
- Authorization/X-Api-Key 원문이 로그 detail/ADM에 남지 않는지 확인
- ADM 로그 상세에서 수신 헤더/해석 헤더/전파 헤더/응답 헤더가 분리되는지 확인
```

## 완료 기준

```text
- 표준 헤더 문서와 구현이 일치한다.
- 누락 헤더가 있으면 보강되거나 미구현으로 리포트에 남는다.
- outbound 자동 전파가 테스트로 증명된다.
- 민감 헤더 원문 미저장이 테스트 또는 smoke로 증명된다.
- ADM 헤더 탭 분리가 API 또는 UI smoke로 증명된다.
- 미검증 항목은 미검증으로 분리 기록된다.
```

## 완료 불인정 기준

```text
- 문서에만 있고 구현이 없음
- 헤더 상수만 있고 validator/extractor/propagator가 없음
- 개발자가 header.add(...)를 직접 작성해야 함
- outbound 호출 수신 측 검증이 없음
- 민감 헤더 원문이 로그에 남음
- ADM에서 헤더와 데이터부가 분리되지 않음
- 누락 헤더를 검수하지 않고 완료 처리
```

---

# CPF_REQUEST_015_LOCAL_DEV_MANUAL: 로컬 개발환경 구성 / 빌드 / 실행 / 배포 / 테스트 매뉴얼

## 목적

사용자가 직접 로컬 PC에서 CPF를 빌드하고, ADM/BAT를 실행하고, MariaDB를 구성하고, smoke/runtime 테스트까지 수행할 수 있도록 `specs/로컬_개발환경_구성_가이드.html` 문서를 작성한다.

## 이번 범위

```text
- 로컬 개발환경 사전 준비물 정리
- JDK 버전 확인
- Gradle wrapper 사용법
- MariaDB 설치/접속/계정 구성
- 신규 DB 생성 절차
- all_install SQL 실행 절차
- Flyway migration 실행 또는 대체 절차
- ADM 애플리케이션 빌드
- ADM 애플리케이션 실행
- BAT 애플리케이션 빌드
- BAT 애플리케이션 실행
- embedded Tomcat 기준 실행 방식
- 외부 WAS 배포 가능/불가/보류 기준 명시
- Swagger/OpenAPI 확인 방법
- ADM 브라우저 접속 방법
- smoke script 실행 방법
- 테스트 명령 실행 방법
- 로그 확인 방법
- 자주 나는 오류와 조치 방법
```

## 문서 구성 권장

```text
1. 문서 목적
2. 로컬 환경 전제
3. 필수 설치 도구
   - JDK
   - Git
   - MariaDB
   - PowerShell
   - IDE
4. 소스 내려받기
5. Gradle offline build 기준
6. DB 구성
   - root/migration/app 계정 구분
   - DB 생성
   - 권한 부여
   - all_install 실행
7. ADM 실행
   - bootJar
   - bootRun 또는 java -jar
   - profile 설정
   - health 확인
8. BAT 실행
   - bootJar
   - smoke job 실행
   - health 확인
9. 브라우저 테스트
   - ADM 로그인
   - 거래 메타
   - 로그 상세
   - 헤더 탭 확인
10. OpenAPI 확인
11. smoke 테스트
12. 전체 테스트
13. 로그/리포트 확인
14. 문제 해결
15. 완료 체크리스트
```

## 필수 명령 예시

문서에는 실제 프로젝트 기준으로 동작 가능한 명령을 확인해서 작성한다. 명령이 실제 확인되지 않으면 “예시/미검증”으로 표시한다.

```powershell
java -version
.\gradlew.bat --version
.\gradlew.bat clean test --offline
.\gradlew.bat :adm:bootJar --offline
.\gradlew.bat :bat:bootJar --offline
.\gradlew.bat qualityGate --offline
```

ADM/BAT 실행 명령은 실제 jar 파일명과 profile 기준을 확인해서 작성한다.

```powershell
java -jar adm\build\libs\*.jar
java -jar bat\build\libs\*.jar
```

## 완료 기준

```text
- specs/로컬_개발환경_구성_가이드.html이 실제 HTML로 작성되어 있다.
- README와 specs/index.html에서 해당 문서로 연결된다.
- 사용자가 로컬에서 빌드/DB구성/ADM실행/BAT실행/smoke를 따라 할 수 있다.
- 실제 확인한 명령과 미검증 명령이 구분되어 있다.
- 외부 WAS 배포가 현재 지원되지 않거나 미검증이면 명확히 기록되어 있다.
```

## 완료 불인정 기준

```text
- 일반적인 Spring Boot 설명만 있고 CPF 명령이 없음
- DB 계정/권한/all_install 절차가 없음
- ADM/BAT 실행 방법이 없음
- smoke 실행 방법이 없음
- 실제 확인하지 않은 명령을 성공으로 기록
- Markdown 형식으로 작성하고 HTML로 위장
```

---

# CPF_REQUEST_016_EDU_SQL: EDU 조회 샘플 MyBatis/SQL/슬라이스 테스트 실전화

## 목적

이전 EDU 조회 샘플이 in-memory 기준으로 남아 있으므로, 실제 신규 개발자가 복사해 쓸 수 있도록 MyBatis Mapper, SQL, 테스트까지 갖춘 실전형 EDU 조회 샘플로 보강한다.

## 이번 범위

```text
- EDU 단건 조회 MyBatis Mapper
- EDU 목록 조회 MyBatis Mapper
- EDU offset paging Mapper
- EDU keyset paging Mapper
- 검색 조건 Mapper
- 정렬 조건 whitelist
- SQL seed 또는 테스트 fixture
- Repository/Service/Controller 연결
- 슬라이스 테스트 또는 Mapper 테스트
- 표준 헤더 자동 검증/전파 샘플 유지
- 한글 주석 상세 보강
```

## 필수 구현 패턴

```text
- 단건 조회
- 목록 조회
- offset page 조회
- keyset page 조회
- 검색 조건 조회
- 정렬 조건 조회
- limit + 1 hasNext 판단
- 안정 정렬키 사용
- 대량 조회 size 제한
- 없는 데이터 표준 오류
```

## 금지 기준

```text
- EDU Mapper에서 mbr_member 직접 JOIN
- EDU Mapper에서 exs_* 직접 JOIN
- for-loop 안에서 회원 단건 조회 반복 호출
- outbound 호출 시 표준 헤더 수동 하드코딩
- 정렬 컬럼을 사용자 입력 문자열 그대로 SQL에 삽입
```

## 완료 기준

```text
- EDU 조회 샘플이 in-memory가 아니라 Mapper/SQL 기반이다.
- 테스트가 있다.
- 표준 헤더 자동 검증/전파 샘플이 유지된다.
- 개발자가 복사해 신규 조회 기능을 만들 수 있을 정도로 주석이 충분하다.
```

---

# CPF_REQUEST_017_DB_RUNTIME: MariaDB 신규 DB 전체 설치 SQL 실검증

## 목적

현재 미검증으로 남아 있는 MariaDB 전체 설치 SQL 실행을 실제 신규 DB 기준으로 검증한다.

## 이번 범위

```text
- 신규 DB drop/create
- root 또는 migration 계정 기준 DDL 실행
- app 계정 DDL 차단 확인
- all_install SQL 실행
- Flyway migration 정합성 확인
- seed idempotent 재실행 확인
- ADM 기동 확인
- BAT 기동 확인
- 기본 smoke 실행
- 실패 SQL/원인/조치 기록
```

## 완료 기준

```text
- 신규 DB에서 전체 설치가 성공한다.
- seed 재실행이 안전하다.
- app 계정과 migration/root 계정 권한 분리가 검증된다.
- 실패 시 실패 항목과 미조치 사유가 CPF_STABILIZATION_REPORT.html에 기록된다.
```

## 완료 불인정 기준

```text
- SQL 파일만 존재하고 실제 MariaDB 실행 없음
- 기존 DB에서만 확인
- 실패를 숨기고 성공으로 기록
- app 계정으로 DDL이 가능한데도 완료 처리
```

---

# CPF_REQUEST_018_ADM_RUNTIME: ADM 브라우저 클릭 / OpenAPI JSON Runtime 검증

## 목적

ADM 기능이 API/정적 UI 수준을 넘어 실제 브라우저와 runtime OpenAPI 기준으로 동작하는지 확인한다.

## 이번 범위

```text
- ADM 애플리케이션 기동
- ADM 로그인
- 거래 메타 목록
- 거래 메타 상세
- 거래 로그 목록
- 거래 로그 상세
- 수신 헤더 탭
- 해석 헤더 탭
- 전파 헤더 탭
- 응답 헤더 탭
- 로그 정책 화면
- 캐시 summary 화면
- 배치 관제 화면
- OpenAPI JSON 접근
- 주요 API가 OpenAPI에 노출되는지 확인
```

## 완료 기준

```text
- 실제 브라우저로 메뉴 진입이 검증된다.
- 헤더 탭 분리가 화면에서 확인된다.
- OpenAPI JSON runtime 접근이 검증된다.
- 실패 화면/사유가 리포트에 기록된다.
```

## 완료 불인정 기준

```text
- API 테스트만 하고 브라우저 클릭 성공으로 기록
- 정적 HTML marker만 보고 화면 완료 처리
- OpenAPI 문서 파일만 보고 runtime 성공 처리
```

---

# CPF_REQUEST_019_DB_STANDARD_REVIEW: DB 표준 기반 기존 SQL/테이블 점검

## 목적

DB_표준_가이드 기준으로 기존 SQL/Flyway/all_install/Mapper를 점검하고 보정 후보를 분리한다.

## 이번 범위

```text
- 주제영역별 prefix 점검
- 테이블 COMMENT 점검
- 컬럼 COMMENT 점검
- 공통 감사 컬럼 점검
- PK/FK/INDEX 명명 규칙 점검
- ID/NO/CODE/TYPE/STATUS/YN/AT/BY 컬럼 규칙 점검
- VARCHAR 길이 표준 점검
- JSON/LONGTEXT 사용 기준 점검
- Flyway와 all_install 정합성 점검
- 부적합 항목 리포트 기록
```

## 주의

이번 요청에서는 대량 DDL 변경을 하지 않는다.
점검과 보정 후보 분리가 중심이다.

## 완료 기준

```text
- 표준 적합/부적합 목록이 CPF_STABILIZATION_REPORT.html에 있다.
- 바로 수정할 항목과 보류할 항목이 분리되어 있다.
- SQL 가이드와 DB 표준 가이드가 연결되어 있다.
```
