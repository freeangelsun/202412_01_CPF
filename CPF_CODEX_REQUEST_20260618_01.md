현재 저장소는 `CPF CoreFlow Platform Framework`입니다.

GitHub 저장소:
`https://github.com/freeangelsun/202412_01_CPF`

이번 작업의 목적은 신규 업무 기능을 무작정 추가하는 것이 아니라, 현재까지 작업된 CPF 프레임워크를 기준으로 **기반 안정화, 현재상황 리뷰, ADM 운영 조회/검색 기능 보강, 배치관리 고도화, 알림/노티 설계, 보안 책임 분리, 관리자 화면 권한별 다운로드 기능, 문서/SQL/Swagger/EDU 샘플 정합성 확보, MariaDB 실검증, 최종 리포트 작성**을 수행하는 것입니다.

다른 AI 모델과 사람이 작업 결과를 별도로 검증할 예정입니다.
따라서 아래 원칙을 반드시 지키세요.

```text
1. 실행하지 않은 검증을 성공했다고 보고하지 마세요.
2. 실패한 명령을 숨기지 마세요.
3. 추측을 사실처럼 말하지 마세요.
4. 일부만 수정하고 전체 완료처럼 보고하지 마세요.
5. 검증하지 못한 항목은 반드시 `미검증`이라고 표시하세요.
6. 커밋하지 마세요.
7. 작업 완료 후 프로젝트 루트에 `CPF_STABILIZATION_REPORT.md` 파일을 반드시 생성하세요.
8. 전체 변경 파일 목록은 프로젝트 루트에 `CPF_STABILIZATION_CHANGED_FILES.txt`로 생성하세요.
9. `CPF_STABILIZATION_REPORT.md`에는 사용자가 요청한 전체 요구사항 목록과 각 요구사항별 수행 여부를 반드시 포함하세요.
10. `CPF_STABILIZATION_REPORT.md`가 없거나 요구사항 체크리스트가 없으면 작업 완료로 보지 않습니다.
```

---

# 0. 작업 전 상태 확인

먼저 현재 상태를 확인하세요.

```powershell
git status
git log --oneline -5
```

`CPF_STABILIZATION_REPORT.md`에 아래 내용을 기록하세요.

```text
현재 브랜치:
마지막 커밋:
작업 시작 전 변경 파일 여부:
작업 시작 일시:
```

커밋은 하지 마세요.

---

# 1. PFW / CMN / ADM / 주제영역 책임 기준

`pfw`는 CPF 프레임워크의 핵심 코어입니다.
업무 개발자가 편의상 임의로 수정하는 영역이 아니라, 프레임워크 패치/업데이트/버전 릴리즈 기준으로 관리되는 보호 영역입니다.

PFW는 아래 성격의 기능을 담당합니다.

```text
- 표준 거래 ID
- 글로벌 ID / Trace ID / Correlation ID / Request ID 표준
- 표준 요청/응답 구조
- 공통 예외 처리
- 공통 로그 AOP
- 인증/인가 필터 흐름
- SecurityContext 표준
- OpenAPI 공통 헤더/응답/오류 스키마
- Spring Batch JobRepository 연동 표준
- 배치 메타/스케줄/영업일/선행 Job/트리거 Job/로그 표준
- 알림 이벤트/룰/발송 이력 표준
- 온라인 거래 로그/오류 로그/추적 로그 표준
- 공통 감사 로그 이벤트
- framework auto-configuration
```

`cmn`은 업무 주제영역에서 재사용하는 공통 라이브러리, 공통 서비스, 공통 API 영역입니다.

```text
- 공통 코드 조회 API
- 공통 메시지 조회 API
- 공통 파일 업로드/다운로드
- 공통 암복호화 유틸
- 날짜/금액/문자열 유틸
- JWT/OAuth 보조 유틸
- 외부 API 호출 공통 클라이언트
- 공통 캐시 서비스
- 공통 알림 발송 adapter
- 공통 validation helper
```

`adm`은 PFW/CMN 기능을 관리하는 운영 콘솔입니다.

```text
- 관리자 로그인
- 관리자 JWT/Refresh Token
- 관리자 MFA/OTP
- 관리자 메뉴 권한
- 관리자 계정 잠금
- 관리자 로그인 이력
- 배치 관리 화면/API
- 알림/노티 관리 화면/API
- 온라인 거래 조회 화면/API
- 오류 로그 조회 화면/API
- 거래 추적 화면/API
- 엑셀/CSV/로그 다운로드 화면/API
- 로그/감사/운영 관제 화면/API
```

의존 방향은 아래를 지키세요.

```text
PFW → CMN/ADM/ACC/MBR/PAY 등 업무 모듈 의존 금지
CMN → PFW는 필요 시 가능
ADM/ACC/MBR/PAY 등 → PFW, CMN 사용 가능
CMN → 특정 업무 모듈 의존 금지
```

---

# 2. 항상 지켜야 할 작업 기준

```text
1. 문서, 소스, SQL, Swagger, ADM 화면, EDU 샘플은 반드시 서로 일치해야 합니다.
2. README는 짧은 진입점으로 유지하고, 상세 내용은 specs 하위 가이드 문서로 연결하세요.
3. 기능을 추가하거나 변경하면 README, 개발가이드, 관리자 가이드, SQL 가이드, EDU 샘플을 함께 현행화하세요.
4. 모든 신규 주석, 설명, SQL COMMENT는 한글로 작성하세요.
5. 운영 코드 주석은 기능 단위로 충분히 작성하세요.
6. EDU 샘플은 학습용이므로 라인 단위에 가깝게 자세히 주석을 작성하세요.
7. 최종 리포트에는 수행 작업, 검증 결과, 남은 리스크, 보류 항목, 다음 보강 후보를 짧고 명확하게 정리하세요.
8. 숨겨진 실패가 없도록 실행/실패/미검증 상태를 구분하세요.
9. 코드만 고치고 문서를 누락하지 마세요.
10. SQL만 고치고 Java/ADM 화면/API/Swagger를 누락하지 마세요.
```

---

# 3. 작업 리포트 파일 생성

작업 완료 후 프로젝트 루트에 아래 파일을 반드시 생성하세요.

```text
./CPF_STABILIZATION_REPORT.md
```

전체 변경 파일 목록은 별도 파일로 생성하세요.

```text
./CPF_STABILIZATION_CHANGED_FILES.txt
```

`CPF_STABILIZATION_CHANGED_FILES.txt`는 아래 명령 기준으로 작성하세요.

```powershell
git diff --name-only
git diff --stat
```

---

# 4. 리포트에 포함할 사용자 요청사항 체크리스트

`CPF_STABILIZATION_REPORT.md`에는 아래 체크리스트를 반드시 포함하세요.

```markdown
## 사용자 요청사항 원문 요약 및 수행 체크리스트

| 번호 | 요청사항 | 수행 상태 | 증빙/결과 | 비고 |
|---|---|---|---|---|
| 1 | PFW는 프레임워크 핵심 코어로 보호 영역, CMN은 업무 공통 기능/API 영역으로 역할 분리 | 완료/일부/실패/미검증 |  |  |
| 2 | 문서, 소스, SQL, Swagger, ADM 화면, EDU 샘플 정합성 유지 | 완료/일부/실패/미검증 |  |  |
| 3 | README는 짧게 유지하고 상세 내용은 가이드 문서로 연결 | 완료/일부/실패/미검증 |  |  |
| 4 | 기능 변경 시 README/개발가이드/관리자가이드/SQL가이드/EDU샘플 함께 현행화 | 완료/일부/실패/미검증 |  |  |
| 5 | 신규 주석/설명/SQL COMMENT는 한글 작성 | 완료/일부/실패/미검증 |  |  |
| 6 | UTF-8 설정만이 아니라 실제 mojibake/한글 깨짐 전수 제거 | 완료/일부/실패/미검증 |  |  |
| 7 | 한 줄로 몰린 Gradle/YAML/SQL/MD/PS1/Java 포맷 복구 | 완료/일부/실패/미검증 |  |  |
| 8 | MariaDB 실 DB 기준 00_all_install_and_smoke.sql 실행 및 검증 | 완료/일부/실패/미검증 |  |  |
| 9 | DB 비밀번호는 소스/문서/리포트에 기록하지 않고 로컬 실행에만 사용 | 완료/일부/실패/미검증 |  |  |
| 10 | Swagger가 PFW에만 갇히지 않고 ACC/MBR/ADM/XYZ 실행 모듈에서 노출되는 구조 검증 | 완료/일부/실패/미검증 |  |  |
| 11 | /v3/api-docs, /swagger-ui.html, /swagger-ui/index.html 경로 검증 또는 미검증 사유 기록 | 완료/일부/실패/미검증 |  |  |
| 12 | AutoConfiguration.imports 정상 형식 확인 및 자동 설정 로딩 구조 점검 | 완료/일부/실패/미검증 |  |  |
| 13 | OpenAPI 그룹 하드코딩 문제 분석 및 property 기반 확장 구조 검토 | 완료/일부/실패/미검증 |  |  |
| 14 | ADM 온라인 거래 조회/오류 조회/배치 조회/알림 조회의 표준 검색조건과 필터 보강 | 완료/일부/실패/미검증 |  |  |
| 15 | 거래 ID, 글로벌 ID, UUID, Trace ID, Correlation ID, Request ID 기준 조회 제공 | 완료/일부/실패/미검증 |  |  |
| 16 | 헤더값, 고객번호/회원번호, 파라미터 특정 문구 포함 거래 검색 기능 검토/보강 | 완료/일부/실패/미검증 |  |  |
| 17 | 개인정보/민감정보 검색 시 마스킹/권한 통제/저장 정책 검토 | 완료/일부/실패/미검증 |  |  |
| 18 | ADM 모든 목록/로그/이력 화면에서 권한에 따른 Excel/CSV/로그 다운로드 기능 검토/보강 | 완료/일부/실패/미검증 |  |  |
| 19 | 다운로드 권한, 마스킹, 다운로드 사유, 다운로드 감사 로그 저장 정책 검토/보강 | 완료/일부/실패/미검증 |  |  |
| 20 | 배치 관리/관제/스케줄/영업일/선행 Job/트리거 Job/실행 로그/재수행/중지/관계도 기능은 PFW 공통 기능으로 설계 | 완료/일부/실패/미검증 |  |  |
| 21 | ADM은 PFW 배치 기능을 관리하는 운영 콘솔 역할로 설계 | 완료/일부/실패/미검증 |  |  |
| 22 | ACC/MBR/PAY 등 주제영역은 실제 업무 배치 Job만 구현하고 PFW/ADM 공통 기능 사용 | 완료/일부/실패/미검증 |  |  |
| 23 | Spring Batch 표준 BATCH_* 테이블과 PFW 자체 배치 테이블 관계 검증 | 완료/일부/실패/미검증 |  |  |
| 24 | JobRepository 실 DB 운영 검증 | 완료/일부/실패/미검증 |  |  |
| 25 | 영업일만 수행, 수행 가능 시간, 수행 일자 시뮬레이션 기능 검토/보강 | 완료/일부/실패/미검증 |  |  |
| 26 | 선행 Job/후행 Job/트리거 Job 설정 및 관계 구조도 검토/보강 | 완료/일부/실패/미검증 |  |  |
| 27 | 수행 대기 인스턴스/수행 대상 인스턴스 관리 기능 검토/보강 | 완료/일부/실패/미검증 |  |  |
| 28 | 배치 실행 로그, Step 로그, 운영자 조작 로그 DB 저장 정책 정리 | 완료/일부/실패/미검증 |  |  |
| 29 | 알림/노티 기능을 PFW 공통 기능 + ADM 관리 화면/API로 설계 | 완료/일부/실패/미검증 |  |  |
| 30 | 오류/배치 실패/지연/외부 API 오류/DB 오류 등 알림 대상 조건을 ADM에서 설정 가능하도록 설계 | 완료/일부/실패/미검증 |  |  |
| 31 | JWT/OAuth/Spring Security 책임을 PFW/CMN/ADM/주제영역으로 분리 | 완료/일부/실패/미검증 |  |  |
| 32 | ADM은 관리자 인증만 담당하고 비즈니스 인증은 각 주제영역에서 확장 | 완료/일부/실패/미검증 |  |  |
| 33 | CMN은 JWT/OAuth 공통 유틸/API를 제공하되 로그인 정책과 권한 판단은 소유하지 않음 | 완료/일부/실패/미검증 |  |  |
| 34 | 테스트 disabled 상태 점검 및 가능한 범위에서 qualityGate 신뢰도 회복 | 완료/일부/실패/미검증 |  |  |
| 35 | Redis/Kafka/RabbitMQ 실 broker 테스트는 이번 범위에서 제외 | 완료/일부/실패/미검증 |  |  |
| 36 | broker 미사용 환경에서 no-op/in-memory/DB fallback 구조 점검 | 완료/일부/실패/미검증 |  |  |
| 37 | ADM MFA/OTP는 이번 작업에서 무리하게 완성하지 않고 상태와 다음 단계 문서화 | 완료/일부/실패/미검증 |  |  |
| 38 | VS Code Problems 기준 null/static warning 정리 | 완료/일부/실패/미검증 |  |  |
| 39 | 프로젝트 루트에 CPF_STABILIZATION_REPORT.md 생성 | 완료/일부/실패/미검증 |  |  |
| 40 | 프로젝트 루트에 CPF_STABILIZATION_CHANGED_FILES.txt 생성 | 완료/일부/실패/미검증 |  |  |
| 41 | 실행하지 않은 검증을 성공으로 보고하지 않음 | 완료/일부/실패/미검증 |  |  |
| 42 | 실패한 명령과 미검증 항목을 숨기지 않음 | 완료/일부/실패/미검증 |  |  |
| 43 | 커밋하지 않음 | 완료/일부/실패/미검증 |  |  |
```

---

# 5. 현재상황 리뷰 먼저 수행

작업에 들어가기 전에 현재 상태를 먼저 리뷰하고 `CPF_STABILIZATION_REPORT.md`에 기록하세요.

특히 아래 항목을 현재 구현 여부 기준으로 확인하세요.

```text
1. ADM 배치관리 화면 존재 여부
2. ADM 배치관리 API 목록
3. ADM 온라인 거래 조회 화면/API 존재 여부
4. ADM 오류 로그 조회 화면/API 존재 여부
5. ADM 알림/노티 조회 화면/API 존재 여부
6. ADM 엑셀/CSV/로그 다운로드 API 존재 여부
7. PFW 표준 거래 로그 테이블 목록
8. PFW 오류 로그/감사 로그 테이블 목록
9. PFW 배치 관련 테이블 목록
10. Spring Batch 표준 BATCH_* 테이블 존재 여부
11. JobRepository 실 DB 사용 여부
12. ADM 화면에서 실제 제공되는 버튼 목록
13. SQL 설치 스크립트에 온라인 거래 로그/오류 로그/배치/알림/다운로드 감사 테이블이 포함되어 있는지
14. Swagger/OpenAPI에 ADM Batch API, 거래조회 API, 오류조회 API, 알림 API, 다운로드 API가 노출되는지
15. EDU 샘플에 배치/거래로그/오류로그/알림/다운로드 예제가 있는지
16. 현재 미구현/미검증/보류 항목
```

리뷰 결과는 “있는 것 / 없는 것 / 일부 있음 / 미검증”으로 구분하세요.

---

# 6. UTF-8 / mojibake / 한글 깨짐 전수 정리

UTF-8 설정만 추가하고 완료 처리하지 마세요.
실제 mojibake 문자열을 전수 검색하고 제거해야 합니다.

검색 패턴 예시:

```text
?꾨젅
?꾩썙
濡쒓렇
嫄곕옒
二쇱젣
⑤뱢
硫붿씤
?좏뵆
?댁뀡
ì
ë
í
ê
�
```

점검 대상:

```text
Java 전체
XML 전체
YAML 전체
SQL COMMENT 전체
Markdown 전체
PowerShell 전체
HTML/JS 화면 문구 전체
Gradle 전체
```

요구사항:

```text
1. 깨진 한글 주석은 의미를 추정할 수 있으면 정상 한글로 복구하세요.
2. 의미를 확정할 수 없으면 간결한 한글 또는 영문 주석으로 대체하세요.
3. 신규 주석/설명/SQL COMMENT는 한글로 작성하세요.
4. check-utf8.ps1의 mojibake 패턴을 강화하세요.
5. 기존 gate가 놓친 패턴을 추가하세요.
6. 검사 결과를 리포트에 기록하세요.
```

검증 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
.\gradlew.bat checkUtf8
.\gradlew.bat checkMojibake
```

---

# 7. 파일 포맷 복구

한 줄로 몰린 Gradle/YAML/SQL/MD/PS1/Java 파일을 복구하세요.

우선 복구 대상:

```text
build.gradle
settings.gradle
pfw/build.gradle
cmn/build.gradle
acc/build.gradle
mbr/build.gradle
adm/build.gradle
xyz/build.gradle

pfw/src/main/resources/application-pfw.yml
cmn/src/main/resources/application-cmn.yml
acc/src/main/resources/application.yml
mbr/src/main/resources/application.yml
mbr/src/main/resources/application-mbr.yml
mbr/src/main/resources/application-mbr-local.yml
adm/src/main/resources/application.yml
xyz/src/main/resources/application.yml

docker-compose.local.yml
scripts/check-utf8.ps1
scripts/check-sql-standard.ps1
specs/**/*.md
specs/sql/**/*.sql
```

검증 명령:

```powershell
.\gradlew.bat clean :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline
```

---

# 8. MariaDB 실 DB 검증 및 비밀번호 처리

사용자는 로컬 PC에 MariaDB를 설치했고, 로컬 테스트용 비밀번호도 Codex에게 제공할 수 있습니다.

비밀번호 처리 원칙:

```text
1. 비밀번호는 로컬 실행에만 사용하세요.
2. 실제 비밀번호를 Git에 올라가는 파일에 기록하지 마세요.
3. application.yml, application-dev.yml, README, specs 문서, SQL, PowerShell, Gradle, 리포트 파일에 실제 비밀번호를 기록하지 마세요.
4. 환경변수 placeholder를 사용하세요.
5. 리포트에는 실제 비밀번호 값을 쓰지 말고 “사용자 제공 로컬 MariaDB 비밀번호로 실 DB smoke 검증 시도”라고만 기록하세요.
```

검증 요구:

```text
1. MariaDB 서비스 상태 확인
2. 00_all_install_and_smoke.sql 실제 실행
3. DB 생성 확인
4. 테이블 생성 확인
5. FK/index 생성 확인
6. seed 데이터 생성 확인
7. 재실행 가능성 검증
8. Spring Batch BATCH_* 테이블 존재 여부 확인
9. PFW 온라인 거래 로그/오류 로그/알림/배치/다운로드 감사 테이블 존재 여부 확인
10. ADM 앱이 DB 접속 가능한지 확인
11. 배치 API가 DB 테이블 기준 정상 조회되는지 확인
12. 온라인 거래 조회/오류 조회/알림 조회/다운로드 API가 DB 테이블 기준 정상 조회되는지 확인
```

필수 검증 명령:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

---

# 9. Swagger / OpenAPI 구조 검증 및 품질 Gate

`pfw`는 Swagger/OpenAPI 공통 설정을 제공하는 프레임워크 모듈입니다.
하지만 실제 API 문서는 각 실행 모듈에서 노출되어야 합니다.

`/v3/api-docs`의 `v3`는 CPF 프로젝트 버전이 아니라 OpenAPI 3.x 명세 엔드포인트입니다.

각 모듈별 기대 경로:

```text
ACC:
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui.html
http://localhost:8080/swagger-ui/index.html

MBR:
http://localhost:8081/v3/api-docs
http://localhost:8081/swagger-ui.html
http://localhost:8081/swagger-ui/index.html

ADM:
http://localhost:8090/v3/api-docs
http://localhost:8090/swagger-ui.html
http://localhost:8090/swagger-ui/index.html

XYZ:
http://localhost:8099/v3/api-docs
http://localhost:8099/swagger-ui.html
http://localhost:8099/swagger-ui/index.html
```

반드시 점검할 항목:

```text
1. pfw에 springdoc 의존성이 있고 실행 모듈로 전파되는지 확인하세요.
2. PfwOpenApiAutoConfiguration이 실제 실행 모듈에서 로딩되는지 확인하세요.
3. META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports 파일을 점검하세요.
4. 자동 설정 클래스가 한 줄에 공백으로 붙어 있으면 안 됩니다. 클래스명은 줄 단위로 분리하세요.
5. 각 실행 모듈이 cpf.pfw를 스캔해서 우연히 동작하는 구조에만 의존하지 않도록 하세요.
6. Spring Boot auto-configuration 방식으로도 동작해야 합니다.
7. application-pfw.yml의 springdoc 설정이 정상 YAML 구조인지 확인하세요.
8. PFW의 공통 표준 거래 헤더가 OpenAPI operation에 중복 없이 추가되는지 확인하세요.
9. /v3/api-docs JSON에 openapi, info, paths가 포함되는지 확인하세요.
10. ADM Batch API가 Swagger에 노출되는지 확인하세요.
11. ADM 온라인 거래 조회 API, 오류 조회 API, 알림 조회 API, 다운로드 API가 Swagger에 노출되는지 확인하세요.
12. 공통 응답/오류 구조가 문서화되는지 확인하세요.
```

---

# 10. ADM 운영 조회 검색조건/필터 기준 보강

ADM의 배치관리, 온라인 거래 로그, 오류 로그, 알림 로그 화면에는 운영자가 장애를 빠르게 찾을 수 있도록 표준 검색조건과 필터를 제공하세요.

검색조건은 화면마다 임의로 만들지 말고, PFW 표준 거래 헤더/거래 로그/배치 메타/알림 이벤트 기준과 일치하게 설계하세요.

## 10.1 공통 검색조건

ADM의 주요 조회 화면에는 가능한 범위에서 아래 공통 검색조건을 제공하세요.

```text
- 조회 기간: 시작일시 ~ 종료일시
- 업무일자 / 기준일자 / 영업일자
- 모듈: PFW / CMN / ADM / ACC / MBR / PAY / LNG / EXS 등
- 주제영역 코드
- 거래 ID / Transaction ID
- 글로벌 ID / Global ID
- Trace ID
- Correlation ID
- Request ID
- Span ID
- UUID
- 이벤트 ID / Event ID
- 요청 ID / Header Request ID
- 사용자 ID / 관리자 ID
- 고객번호 / 회원번호
- 고객번호 해시 / 회원번호 해시
- API URI
- HTTP Method
- HTTP Status
- 처리 상태
- 오류 여부
- 오류 코드
- 오류 메시지 키워드
- 서버 인스턴스 ID
- 클라이언트 IP
- 요청 채널
- User-Agent
- 정렬 조건
- 페이지 사이즈
```

고객번호, 회원번호, IP 등 개인정보 또는 민감정보에 해당할 수 있는 항목은 권한에 따라 마스킹하거나 조회 권한을 제한하세요.

## 10.2 표준 거래 ID 체계 정리

PFW 표준 헤더와 로그 테이블에서 아래 ID 체계를 정리하고 문서화하세요.

```text
- transactionId: CPF 내부 단일 거래 식별자
- globalId: 외부/채널/연계 시스템까지 이어지는 전역 거래 식별자
- traceId: 분산 추적용 trace 식별자
- correlationId: 연관 거래 묶음 식별자
- requestId: 단일 요청 식별자
- spanId: trace 내 세부 처리 구간 식별자
- eventId: 이벤트/알림/오류 발생 식별자
- executionId: 배치 실행 식별자
- jobExecutionId: Spring Batch JobExecution 식별자
- stepExecutionId: Spring Batch StepExecution 식별자
- uuid: 시스템 내부 UUID 기반 식별자
```

요구사항:

```text
1. 각 ID가 어느 헤더/테이블/로그에 저장되는지 정리하세요.
2. ADM에서 각 ID로 조회할 수 있게 하세요.
3. 상세 화면에서 관련 ID를 모두 보여주세요.
4. 특정 ID를 클릭하면 연관 거래/오류/알림/배치 이력으로 이동할 수 있게 설계하세요.
5. PFW 표준 헤더명과 Swagger 문서가 일치해야 합니다.
```

## 10.3 온라인 거래 조회 필터

온라인 거래 로그 조회 화면에는 아래 필터를 제공하세요.

```text
- 전체 거래
- 오류 거래만 보기
- 성공 거래만 보기
- 지연 거래만 보기
- 타임아웃 거래만 보기
- 상태별 조회: SUCCESS / FAIL / ERROR / TIMEOUT / PROCESSING
- 거래 ID로 조회
- 글로벌 ID로 조회
- Trace ID로 조회
- Correlation ID로 조회
- Request ID로 조회
- UUID로 조회
- 고객번호/회원번호로 조회
- 고객번호/회원번호 해시로 조회
- 표준 헤더값으로 조회
- API URI로 조회
- HTTP Status별 조회
- 오류 코드별 조회
- 처리 시간 기준 조회
  - 예: 1초 이상, 3초 이상, 5초 이상
- 외부 연계 거래 여부
- 배치 연계 거래 여부
- 알림 발생 거래 여부
- 특정 파라미터명 포함 거래
- 특정 파라미터값 포함 거래
- 특정 문구가 요청 파라미터/요청 바디/응답 바디/헤더에 포함된 거래
```

목록 컬럼:

```text
- 거래일시
- 거래 ID
- 글로벌 ID
- Trace ID
- Correlation ID
- Request ID
- UUID
- 모듈
- 업무 코드
- API URI
- HTTP Method
- 상태
- HTTP Status
- 오류 코드
- 처리 시간
- 고객번호/회원번호 마스킹 값
- 서버 인스턴스
- 알림 발생 여부
```

상세 화면:

```text
- 요청 헤더
- 응답 헤더
- 표준 거래 헤더
- 요청 파라미터
- 요청 바디 요약
- 응답 바디 요약
- 오류 상세
- 연계 거래 ID
- 관련 배치 Execution ID
- 관련 알림 발생 여부
- 관련 오류 로그
- 관련 감사 로그
```

## 10.4 파라미터/본문 특정 문구 포함 거래 검색

검색 대상:

```text
- Query Parameter
- Path Variable
- Request Header
- Request Body
- Response Header
- Response Body
- Error Message
- External API Request/Response 요약
```

검색조건:

```text
- 파라미터명
- 파라미터값
- 특정 문자열 포함
- 대소문자 구분 여부
- 정확히 일치
- 앞/뒤 포함
- 오류건만 검색
- 특정 API URI 내에서만 검색
- 특정 기간 내에서만 검색
```

주의사항:

```text
1. 개인정보/민감정보 원문을 무분별하게 저장하지 마세요.
2. 비밀번호, 토큰, 주민번호, 계좌번호 전문, 카드번호, 인증번호 등은 저장하지 않거나 마스킹/해시 처리하세요.
3. 검색 가능한 payload 저장 범위는 설정으로 제어하세요.
4. 운영환경에서는 body 전문 저장을 기본 비활성화하거나 제한하세요.
5. 민감정보 마스킹 규칙을 PFW 공통 정책으로 설계하세요.
6. 검색용 요약 필드 또는 safe payload 필드를 별도로 설계하세요.
7. 파라미터 검색은 성능에 영향을 줄 수 있으므로 인덱스/전문검색/보관주기 정책을 검토하세요.
```

필요하면 아래와 같은 테이블 또는 컬럼을 검토하세요.

```text
pfw_transaction_log
pfw_transaction_header_log
pfw_transaction_param_log
pfw_transaction_payload_log
pfw_error_log
```

---

# 11. ADM 권한 기반 Excel/CSV/로그 다운로드 기능

ADM의 모든 목록성 화면, 로그 화면, 이력 화면에는 권한에 따라 Excel/CSV/로그 다운로드 기능을 제공하세요.

## 11.1 다운로드 대상 화면

아래 화면에는 다운로드 기능을 검토/보강하세요.

```text
- 온라인 거래 조회
- 거래 상세 조회
- 오류 로그 조회
- 감사 로그 조회
- 배치 Job 목록
- 배치 스케줄 목록
- 배치 실행 이력
- 배치 Step 실행 상세
- 배치 관계 구조 조회
- 수행 대기 인스턴스 조회
- 알림/노티 룰 목록
- 알림 발송 이력
- 관리자 계정 목록
- 관리자 로그인 이력
- 메뉴/권한 목록
- 공통 코드 목록
- 공통 메시지 목록
- 시스템 설정 목록
```

## 11.2 다운로드 형식

```text
- Excel .xlsx
- CSV
- 필요 시 JSON
- 로그 파일 다운로드 또는 로그 요약 다운로드
```

Excel은 대용량을 고려해 streaming 방식 사용을 검토하세요.
Java에서는 Apache POI SXSSF 또는 동등한 streaming 방식 사용을 검토하세요.

## 11.3 권한 기준

다운로드는 단순 조회 권한과 분리하세요.

```text
- 조회 권한
- 상세 조회 권한
- Excel 다운로드 권한
- CSV 다운로드 권한
- 로그 다운로드 권한
- 마스킹 해제 다운로드 권한
- 대용량 다운로드 권한
- 민감정보 포함 다운로드 권한
```

예:

```text
조회 가능하지만 다운로드 불가
다운로드 가능하지만 개인정보는 마스킹
특정 관리자만 마스킹 해제 다운로드 가능
대용량 다운로드는 별도 권한 필요
```

## 11.4 다운로드 정책

```text
1. 현재 검색조건을 그대로 반영해 다운로드하세요.
2. 검색조건 없이 전체 다운로드는 제한하거나 별도 권한을 요구하세요.
3. 대용량 다운로드는 최대 건수 제한 또는 비동기 다운로드 방식으로 설계하세요.
4. 다운로드 사유 입력을 요구할 수 있게 하세요.
5. 다운로드 파일에는 생성일시, 관리자 ID, 검색조건 요약을 포함할지 검토하세요.
6. 파일명은 화면명, 조회기간, 생성일시 기준으로 생성하세요.
7. 민감정보는 기본 마스킹 처리하세요.
8. 토큰, 비밀번호, 인증번호, 주민번호, 카드번호 등은 다운로드 대상에서 제외하거나 강제 마스킹하세요.
9. 다운로드 API는 권한 체크를 반드시 수행하세요.
10. 다운로드 실패도 감사 로그에 남기세요.
```

## 11.5 다운로드 감사 로그

다운로드 행위는 반드시 DB 감사 로그에 남기세요.

권장 테이블:

```text
adm_download_audit_log
또는
pfw_admin_download_log
```

저장 항목 예시:

```text
- download_id
- admin_id
- admin_name
- menu_id
- screen_id
- download_type: EXCEL / CSV / JSON / LOG
- target_type: TRANSACTION / ERROR / BATCH / NOTIFICATION / ADMIN / CODE / MESSAGE 등
- search_condition_summary
- search_condition_hash
- row_count
- masked_yn
- include_sensitive_yn
- reason
- client_ip
- user_agent
- requested_at
- completed_at
- status: REQUESTED / SUCCESS / FAILED
- failure_reason
- file_name
```

실제 파일 자체를 DB에 저장할지 여부는 신중히 검토하세요.
기본은 “파일 저장 X, 다운로드 이력과 조건만 저장”을 권장합니다.

## 11.6 다운로드 API 예시

```text
GET /adm/api/transactions/export
GET /adm/api/errors/export
GET /adm/api/batch/jobs/export
GET /adm/api/batch/executions/export
GET /adm/api/notifications/export
GET /adm/api/admin-users/export
GET /adm/api/downloads/history
```

## 11.7 리포트 기록

`CPF_STABILIZATION_REPORT.md`에는 아래를 기록하세요.

```text
- ADM 다운로드 기능 적용 화면
- 권한별 다운로드 통제 여부
- Excel/CSV/로그 다운로드 지원 여부
- 마스킹 정책
- 다운로드 사유 입력 여부
- 다운로드 감사 로그 저장 여부
- 대용량 다운로드 정책
- 미구현/보류 항목
```

---

# 12. 배치 조회 필터

ADM 배치관리 화면에는 아래 검색조건을 제공하세요.

```text
- Job ID
- Job명
- Schedule ID
- Execution ID
- Spring Batch JobExecution ID
- Spring Batch StepExecution ID
- 업무일자 / 영업일자
- 실행 상태
  - READY
  - WAITING
  - RUNNING
  - SUCCESS
  - FAILED
  - STOPPED
  - SKIPPED
  - RETRYING
- 오류 건만 보기
- 재수행 대상만 보기
- 장시간 실행 건만 보기
- 미수행 건만 보기
- 지연 실행 건만 보기
- 영업일 조건 적용 여부
- 수행 가능 시간 초과 여부
- 선행 Job 실패 여부
- 트리거 Job 여부
- 실행 서버/인스턴스
- 담당자/운영자
- 알림 발송 여부
- transactionId/globalId/traceId와 연계된 배치 여부
```

목록 컬럼:

```text
- Job ID
- Job명
- Schedule ID
- Execution ID
- Spring Batch JobExecution ID
- 업무일자
- 시작일시
- 종료일시
- 상태
- 처리 시간
- 실행 인스턴스
- 실패 Step
- 오류 코드
- 재수행 가능 여부
- 알림 발송 여부
```

---

# 13. 오류 로그 조회 필터

오류 로그 화면에는 아래 필터를 제공하세요.

```text
- 오류 발생 기간
- 오류 레벨: WARN / ERROR / CRITICAL
- 모듈
- 주제영역
- 거래 ID
- 글로벌 ID
- Trace ID
- Correlation ID
- Request ID
- UUID
- 배치 Execution ID
- Spring Batch JobExecution ID
- 고객번호/회원번호
- 고객번호/회원번호 해시
- 관리자 ID
- 오류 코드
- 예외 클래스명
- 오류 메시지 키워드
- API URI
- HTTP Status
- 알림 발송 여부
- 재처리 여부
- 처리 상태
  - 미처리
  - 확인
  - 조치중
  - 조치완료
  - 보류
```

오류 상세에서는 stack trace 전체를 무조건 노출하지 말고, 운영자 권한에 따라 요약/상세 보기로 분리하세요.

---

# 14. 알림/노티 조회 필터

알림 발송 이력 조회 화면에는 아래 필터를 제공하세요.

```text
- 알림 발생 기간
- 알림 유형
  - 배치 실패
  - 배치 지연
  - 온라인 거래 오류
  - 외부 API 오류
  - DB 오류
  - 인증/권한 오류
- 알림 상태
  - 발송 대기
  - 발송 성공
  - 발송 실패
  - 중복 억제
  - 재시도 대기
- 채널
  - Email
  - Webhook
  - Mattermost
  - Slack
  - SMS
- 수신자/수신 그룹
- 오류 코드
- 거래 ID
- 글로벌 ID
- Trace ID
- Correlation ID
- Request ID
- 배치 Execution ID
- 중복 억제 여부
- 재시도 횟수
```

---

# 15. 검색 프리셋 제공

ADM 화면에는 운영자가 자주 쓰는 검색 프리셋을 제공하세요.

```text
- 최근 1시간 오류 거래
- 오늘 배치 실패 건
- 오늘 미수행 배치
- 장시간 RUNNING 배치
- 알림 발송 실패 건
- 외부 API 타임아웃 거래
- 특정 고객번호 거래 이력
- 특정 거래 ID 전체 추적
- 특정 글로벌 ID 전체 추적
- 특정 Trace ID 전체 추적
- 특정 오류 코드 발생 이력
- 파라미터에 특정 문구가 포함된 거래
```

---

# 16. 인덱스/성능 고려

검색조건으로 자주 사용하는 컬럼에는 SQL 인덱스를 검토하세요.

우선 검토 대상:

```text
- transaction_id
- global_id
- trace_id
- correlation_id
- request_id
- uuid
- event_id
- module_code
- business_date
- customer_no_hash
- member_no_hash
- job_id
- execution_id
- job_execution_id
- step_execution_id
- schedule_id
- status
- error_code
- http_status
- created_at
- started_at
- ended_at
```

고객번호/회원번호는 평문 인덱스보다 마스킹/해시/암호화 정책을 검토하세요.

---

# 17. ADM 배치관리 기능 보강

배치 관리/관제/스케줄/영업일/선행 Job/트리거 Job/실행 로그/재수행/중지/관계도 기능은 주제영역별 개별 구현이 아니라 PFW 프레임워크 공통 기능으로 설계하세요.

ADM은 PFW 배치 기능을 관리하는 운영 콘솔 역할입니다.

주제영역 모듈은 실제 업무 배치 Job만 구현하고, 실행/스케줄/로그/영업일/재수행/관제 기능은 PFW/ADM 공통 기능을 사용합니다.

보강 대상:

```text
- 배치 Job 등록/수정
- 영업일만 수행
- 휴일이면 다음 영업일로 이월
- 휴일이면 이전 영업일로 당김
- 월말/월초 영업일 수행
- 수행 가능 시작/종료 시간
- 수행 금지 시간
- 수행 일자 시뮬레이션
- 선행 Job / 후행 Job / 트리거 Job
- 조건부 트리거
- 의존성 순환 검증
- 수행 대기 인스턴스
- 수행 대상 인스턴스
- 배치 관계 구조도
- 실패 재수행
- 중지
- 버튼 권한/비활성 사유 표시
```

---

# 18. Spring Batch 표준 메타 테이블 검증 및 현행화

Spring Batch 표준 BATCH_* 테이블을 확인하세요.

```text
BATCH_JOB_INSTANCE
BATCH_JOB_EXECUTION
BATCH_JOB_EXECUTION_PARAMS
BATCH_STEP_EXECUTION
BATCH_STEP_EXECUTION_CONTEXT
BATCH_JOB_EXECUTION_CONTEXT
```

요구사항:

```text
1. Spring Batch JobRepository를 실제 DB로 사용할지 결정하세요.
2. 사용할 경우 MariaDB용 Spring Batch 표준 메타 테이블 생성 SQL을 포함하세요.
3. spring.batch.jdbc.initialize-schema=never 유지 시, 수동 설치 SQL에 BATCH_* 테이블을 포함하세요.
4. 운영 프로필에서는 자동 생성보다 migration SQL 기준을 우선하세요.
5. CPF/PFW 자체 배치 테이블과 Spring Batch BATCH_* 테이블의 관계를 문서화하세요.
6. pfw_batch_execution.spring_batch_execution_id가 BATCH_JOB_EXECUTION.JOB_EXECUTION_ID와 어떻게 연결되는지 명확히 하세요.
```

---

# 19. 배치 로그 저장 정책

Spring Batch 표준 메타 테이블은 실행 상태와 카운트, ExecutionContext를 저장합니다.
업무 로그, 운영자 조작 로그, Step 상세 로그는 별도 설계가 필요합니다.

요구사항:

```text
1. 배치 운영 로그는 pfw_batch_operation_log에 저장하세요.
2. 배치 실행 요약은 pfw_batch_execution에 저장하세요.
3. Step 실행 상세는 pfw_batch_step_execution에 저장하세요.
4. Spring Batch 표준 실행 메타는 BATCH_* 테이블에 저장하세요.
5. 애플리케이션 로그는 Logback 파일 로그로 남기되, 운영자가 봐야 하는 주요 이벤트는 DB에도 남기세요.
6. ADM 화면에서 배치 실행 로그와 Step 로그를 조회할 수 있게 하세요.
7. 실패 원인, stack trace 요약, 오류 코드, 재수행 가능 여부를 볼 수 있게 하세요.
8. 너무 긴 로그는 DB에 전체 저장하지 말고 요약/참조/파일 경로 저장 정책을 문서화하세요.
```

---

# 20. 알림 / 노티 관리 기능 설계 및 보강

알림/노티 기능은 프레임워크 공통 기능으로 설계하세요.

역할 분리:

```text
PFW:
- 알림 이벤트 표준
- 알림 룰
- 알림 채널
- 알림 템플릿
- 발송 이력
- 중복 억제 정책
- 재시도 정책

ADM:
- 알림 룰/채널/수신자/템플릿 관리 화면
- 알림 발송 이력 조회
- 알림 조건 관리 API

CMN 또는 EXS:
- 실제 발송 adapter
- Email, Webhook, Mattermost, Slack, SMS 등 외부 발송 연계

업무 모듈:
- 직접 알림을 보내지 않고 표준 오류/이벤트만 발생
```

알림 대상:

```text
- 시스템 오류
- 배치 실패
- 배치 Step 실패
- 배치 지연
- 배치 미수행
- 배치 장시간 실행
- 선행 Job 실패
- 후행 Job 미실행
- 수행 가능 시간 초과
- 수행 대상 인스턴스 없음
- 외부 API 오류
- DB 연결 오류
- 인증/권한 오류
- 중요 거래 실패
- 재시도 초과
- 특정 오류 코드 발생
- 특정 모듈 오류 발생
```

권장 테이블:

```text
pfw_notification_rule
pfw_notification_channel
pfw_notification_receiver
pfw_notification_template
pfw_notification_event
pfw_notification_delivery_log
pfw_notification_suppression
```

---

# 21. JWT / OAuth2 / Spring Security 책임 분리

JWT/OAuth2 인증 구조는 PFW, CMN, ADM, 주제영역의 책임을 분리해서 설계하세요.

PFW:

```text
- Security Filter
- SecurityContext 표준
- 인증/인가 공통 예외
- JWT 검증 필터 구조
- 권한 체크 annotation
- OpenAPI security scheme
- 인증/인가 감사 로그 이벤트
- 공통 인증 실패/인가 실패 응답
```

CMN:

```text
- JWT 생성/검증 유틸
- 토큰 파싱
- TokenClaimsMapper
- OAuth2 client 공통 모듈
- OAuth2 token exchange client
- CryptoUtil
- PasswordEncoder 공통 설정
- TokenCache
- CommonAuthResponse
```

ADM:

```text
- 관리자 로그인
- 관리자 JWT 발급
- 관리자 Refresh Token
- 관리자 MFA/OTP
- 관리자 메뉴 권한
- 관리자 계정 잠금
- 관리자 로그인 이력
- 관리자 비밀번호 정책
```

주제영역:

```text
MBR:
- 회원 로그인
- 회원 JWT
- 회원 Refresh Token
- 휴면/탈퇴/잠금 정책

ACC:
- 계좌 접근 권한
- 계좌 소유자 검증

PAY:
- 결제 승인 권한
- 가맹점/사용자 권한

EXS:
- 외부 기관 OAuth client credential
- 외부 API 인증 토큰 관리
```

관리자 토큰과 회원/업무 토큰은 issuer, audience, claim, role 체계를 분리하세요.

---

# 22. 테스트 disabled 상태 점검 및 qualityGate 신뢰도 회복

검색 대상:

```text
enabled = false
tasks.withType(Test)
configureEach { enabled = false }
```

작업:

```text
1. 테스트 비활성화 설정을 전부 찾으세요.
2. 모듈별로 왜 꺼져 있는지 분석하세요.
3. 가능한 테스트는 다시 활성화하세요.
4. DB가 필요한 테스트는 unit/slice test와 integration test로 분리하세요.
5. 실 DB가 필요한 통합 테스트는 기본 빌드에서 강제하지 말고 명시 옵션으로 실행되게 하세요.
6. 기본 qualityGate는 로컬 broker 없이도 최소한 compile, unit/slice test, UTF-8, mojibake, SQL 표준 검사를 수행해야 합니다.
```

검증 명령:

```powershell
.\gradlew.bat clean qualityGate --offline
```

---

# 23. Redis / Kafka / RabbitMQ 실 broker 테스트 제외

현재 로컬에 Redis, Kafka, RabbitMQ 실 서버가 없습니다.
이번 작업에서 실 broker 연동 테스트를 강제로 추가하지 마세요.

이번 범위:

```text
- 설정 분리 확인
- broker 미사용 환경에서 앱이 기동 가능한지 확인
- IN_MEMORY 또는 NO_OP 모드 기본 동작 확인
- DB fallback 이벤트 구조 확인
- 추후 실 broker 테스트 TODO 문서화
```

제외:

```text
- Redis 실 서버 연동 테스트
- Kafka 실 broker 연동 테스트
- RabbitMQ 실 broker 연동 테스트
- Docker로 broker 강제 기동
- Embedded broker 강제 추가
```

---

# 24. ADM MFA/OTP 상태 정리

TOTP QR, 복구코드, MFA 등록/검증/해제 기능은 후순위로 정리하세요.
이번 배치/알림/보안 책임 정리 작업과 섞어서 무리하게 완성하지 마세요.

산출물:

```text
specs/adm/adm-mfa-otp-next-steps.md
```

포함 내용:

```text
- OTP 등록 API
- OTP 검증 API
- OTP 해제 API
- 관리자 로그인 연동
- 복구 코드
- 감사 로그
- 화면 구성
- 테스트 전략
- 미완성 항목
```

---

# 25. VS Code Problems 기준 null/static warning 정리

우선순위:

```text
1. Potential null pointer access
2. Spring override 메서드 @NonNull / @Nullable 누락
3. Objects.requireNonNull 적용 가능 지점
4. unused import
5. unnecessary @Autowired
6. unused field
7. 불필요한 suppress warning
```

원칙:

```text
1. src/main/java를 우선 정리하세요.
2. src/test/java는 2순위로 정리하세요.
3. 전체 null analysis를 끄지 마세요.
4. @SuppressWarnings는 마지막 수단으로만 사용하세요.
5. suppress가 필요하면 사유를 주석으로 남기세요.
6. 동작 로직을 바꾸지 않는 범위에서만 수정하세요.
```

---

# 26. 문서 정합성 및 specs 정리

specs 하위 MD 파일이 많아졌다면 정리하세요.

원칙:

```text
1. README는 짧은 진입점으로 유지
2. 상세 내용은 가이드 문서로 통합
3. 중복 문서는 병합
4. 오래된 문서는 삭제 또는 deprecated 표시
5. 문서 인덱스에서 전체 문서 구조를 안내
6. 관리자 가이드에는 ADM 배치관리, 운영조회, 알림/노티, 다운로드 기능을 반드시 포함
7. SQL 가이드에는 Spring Batch 표준 테이블과 PFW 배치 운영 테이블 관계를 포함
8. 개발 가이드에는 신규 배치 Job 추가 방법을 포함
9. 개발 가이드에는 PFW/CMN/ADM/주제영역 책임 경계를 포함
10. 개발 가이드에는 JWT/OAuth2 책임 분리를 포함
11. EDU 샘플에는 배치 Job, 거래 로그, 오류 로그, 알림, 다운로드 예제를 자세히 포함
12. 문서와 실제 API/SQL/화면이 일치해야 함
```

---

# 27. EDU 샘플 보강

EDU 샘플은 학습용이므로 자세해야 합니다.

보강 대상:

```text
1. 단순 Tasklet Job
2. Chunk Job
3. 실패 재수행 예제
4. 영업일 조건 예제
5. 수행 가능 시간 예제
6. 선행 Job/후행 Job 예제
7. ADM에서 수동 실행하는 방법
8. Swagger에서 확인하는 방법
9. DB 로그/배치 메타 테이블에서 확인하는 방법
10. 온라인 거래 로그 조회 예제
11. 오류 로그 조회 예제
12. 알림 발송 이력 조회 예제
13. Excel/CSV 다운로드 예제
14. 라인 단위에 가까운 한글 주석
```

---

# 28. 최종 검증 명령

가능한 범위에서 아래 명령을 실행하고 결과를 리포트에 기록하세요.

```powershell
git status

.\gradlew.bat checkUtf8

.\gradlew.bat checkMojibake

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1

.\gradlew.bat clean :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline

.\gradlew.bat qualityGate --offline
```

앱 기동, Swagger, MariaDB, ADM 브라우저 클릭 검증을 수행하지 못한 경우에는 반드시 `미검증`으로 기록하세요.

---

# 29. ADM 화면 브라우저 클릭 검증

ADM 화면은 코드만 보고 완료 처리하지 말고 브라우저에서 실제 클릭 검증하세요.

검증 대상:

```text
1. 로그인
2. 배치 메뉴 진입
3. 배치 목록 조회
4. 배치 등록
5. 수동 실행
6. 재수행
7. 중지
8. 영업일 저장
9. 수행 시뮬레이션
10. 선행 Job/트리거 Job 설정
11. 관계 구조도 조회
12. 온라인 거래 조회
13. 오류 로그 조회
14. 알림/노티 설정
15. Excel/CSV 다운로드
16. 로그 다운로드
17. 다운로드 권한 통제
18. 버튼 권한/비활성 사유 표시
```

브라우저 검증을 하지 못하면 `미검증`으로 기록하세요.

---

# 30. 최종 리포트 필수 구조

`CPF_STABILIZATION_REPORT.md`에는 아래 구조를 반드시 포함하세요.

```markdown
# CPF 안정화 및 추가 보강 작업 리포트

## 1. 작업 개요
- 작업 목적:
- 작업 시작 브랜치:
- 작업 시작 기준 커밋:
- 작업 전 git status:
- 작업 수행 일시:

## 2. 사용자 요청사항 원문 요약 및 수행 체크리스트
- 위 요구사항 체크리스트 전체 포함

## 3. 현재상황 리뷰
- 현재 구현된 기능:
- 미구현 기능:
- 일부 구현 기능:
- 미검증 기능:
- 현재 주요 리스크:

## 4. PFW / CMN / ADM / 주제영역 책임 정리 결과
- PFW:
- CMN:
- ADM:
- 주제영역:
- 의존성 위반 여부:

## 5. ADM 운영 조회/검색조건 보강 결과
- 공통 검색조건:
- 거래 ID/globalId/traceId/correlationId/requestId/UUID 조회:
- 고객번호/회원번호 조회:
- 헤더값 조회:
- 파라미터/본문 특정 문구 검색:
- 개인정보 마스킹/권한 통제:
- 인덱스/성능 검토:
- 미구현/보류 항목:

## 6. ADM 다운로드 기능 보강 결과
- 다운로드 적용 화면:
- Excel 지원:
- CSV 지원:
- 로그 다운로드 지원:
- 권한별 통제:
- 마스킹 정책:
- 다운로드 사유:
- 다운로드 감사 로그:
- 대용량 다운로드 정책:
- 미구현/보류 항목:

## 7. UTF-8 / mojibake 정리 결과
- 수행 여부:
- 수정한 파일:
- 남은 mojibake 여부:
- check-utf8.ps1 -CheckMojibake 결과:
- gradlew checkUtf8 결과:
- gradlew checkMojibake 결과:

## 8. MariaDB / SQL 검증 결과
- MariaDB 서비스 확인:
- 00_all_install_and_smoke.sql 실행 여부:
- DB 생성 확인:
- 테이블 생성 확인:
- FK/index 확인:
- seed 확인:
- 재실행 검증:
- Spring Batch BATCH_* 테이블 확인:
- 실패/미검증 사유:

## 9. Swagger/OpenAPI 검증 결과
- PFW 공통 설정:
- AutoConfiguration.imports:
- ACC:
- MBR:
- ADM:
- XYZ:
- /v3/api-docs:
- /swagger-ui.html:
- /swagger-ui/index.html:
- ADM Batch API:
- ADM 거래조회/오류조회/알림/다운로드 API:
- 미검증 사유:

## 10. 배치관리 보강 결과
- PFW 배치 공통 기능:
- ADM 배치 화면/API:
- Spring Batch JobRepository:
- BATCH_* 테이블:
- 영업일 수행 조건:
- 수행 가능 시간:
- 수행 시뮬레이션:
- 선행/후행/트리거 Job:
- 관계 구조도:
- 수행 대기 인스턴스:
- 배치 로그:
- 미구현/보류 항목:

## 11. 알림/노티 보강 결과
- PFW 알림 이벤트/룰:
- ADM 알림 관리 화면/API:
- CMN/EXS 발송 adapter:
- 알림 대상 조건:
- 중복 억제:
- 발송 이력:
- 배치 알림 연계:
- 미구현/보류 항목:

## 12. JWT/OAuth/Security 책임 분리 결과
- PFW:
- CMN:
- ADM:
- MBR/ACC/PAY/EXS:
- 관리자 토큰/업무 토큰 분리:
- 미구현/보류 항목:

## 13. 테스트 / qualityGate 결과
- 테스트 disabled 발견 여부:
- 테스트 disabled 제거 여부:
- 새로 추가한 테스트:
- 복구한 테스트:
- 여전히 비활성화된 테스트:
- 비활성화 유지 사유:
- qualityGate 결과:

## 14. 문서/EDU 샘플 정합성 결과
- README:
- 개발 가이드:
- 관리자 가이드:
- SQL 가이드:
- EDU 샘플:
- Swagger:
- 미검증/보류 항목:

## 15. 실행한 명령과 결과
| 명령 | 결과 | 비고 |
|---|---|---|

## 16. 남은 리스크
1.
2.
3.

## 17. 보류 항목
1.
2.
3.

## 18. Codex가 판단한 추가 보강 의견
1.
2.
3.

## 19. 다음 Codex 작업 추천
1순위:
2순위:
3순위:

## 20. 추천 커밋 메시지
Stabilize CPF framework baseline and ADM operations

## 21. 허위 보고 방지 체크
- 실행하지 않은 명령을 성공으로 기록하지 않았는가:
- 실패한 명령을 누락하지 않았는가:
- 미검증 항목을 미검증으로 표시했는가:
- Swagger 앱 기동 검증 여부를 사실대로 적었는가:
- MariaDB 실접속 여부를 사실대로 적었는가:
- BATCH_* 테이블이 없는데 Spring Batch 표준 테이블 검증 완료라고 쓰지 않았는가:
- 다운로드 권한/감사 로그를 검증하지 않고 완료라고 쓰지 않았는가:
- 사용자 요청사항 체크리스트를 작성했는가:
```

---

# 31. 허위 보고 금지

아래는 반드시 지키세요.

```text
1. 작업하지 않은 것을 완료라고 쓰지 마세요.
2. 실행하지 않은 검증을 성공이라고 쓰지 마세요.
3. 앱을 띄우지 않았으면 화면 검증 완료라고 쓰지 마세요.
4. DB에 접속하지 않았으면 DB smoke 성공이라고 쓰지 마세요.
5. BATCH_* 테이블이 없는데 Spring Batch JobRepository 검증 완료라고 쓰지 마세요.
6. PFW 자체 배치 테이블만 보고 Spring Batch 표준 테이블까지 있다고 쓰지 마세요.
7. 일부 mojibake만 고치고 전수 정리 완료라고 쓰지 마세요.
8. 문서 미현행화 상태를 완료라고 쓰지 마세요.
9. Swagger 설정 파일만 보고 각 모듈 Swagger가 동작한다고 단정하지 마세요.
10. 다운로드 버튼만 만들고 권한/마스킹/감사 로그까지 완료했다고 쓰지 마세요.
11. Redis/Kafka/RabbitMQ 실 broker 연동 성공이라고 보고하지 마세요.
12. 비밀번호를 파일에 기록하지 마세요.
13. 다른 AI 모델이 검증할 예정이므로 실패/미검증/보류를 숨기지 마세요.
```

---

# 32. 이번 작업에서 하지 말 것

```text
- 커밋 금지
- Redis/Kafka/RabbitMQ 실 broker 강제 설치 금지
- Docker broker 강제 기동 금지
- Embedded broker 강제 추가 금지
- TOTP/MFA를 이번 작업에 섞어서 무리하게 완성 금지
- 대규모 모듈 분리 금지
- 대규모 클래스명 rename 금지
- 테스트 실패를 enabled=false로 숨기기 금지
- DB 비밀번호를 Git에 올라가는 파일에 기록 금지
- 검증하지 않은 기능을 완료로 문서화 금지
- PFW에 업무 도메인 로직 추가 금지
- CMN에 특정 업무 도메인 정책 추가 금지
```

---

# 33. 완료 조건

이번 작업은 아래 조건을 충족해야 완료입니다.

```text
1. 요청된 현재상황 리뷰 수행
2. 요청된 코드/문서/설정 수정 수행
3. 프로젝트 루트에 CPF_STABILIZATION_REPORT.md 생성
4. 프로젝트 루트에 CPF_STABILIZATION_CHANGED_FILES.txt 생성
5. CPF_STABILIZATION_REPORT.md에 사용자 요청사항 전체 체크리스트 포함
6. 실행한 검증 명령 결과 기록
7. 실패/미실행/미검증 항목을 숨기지 않고 기록
8. Codex가 판단한 추가 보강 의견 기록
9. 커밋하지 않음
```

`CPF_STABILIZATION_REPORT.md` 파일이 없거나, 사용자 요청사항 체크리스트가 없으면 작업 완료로 보고하지 마세요.
