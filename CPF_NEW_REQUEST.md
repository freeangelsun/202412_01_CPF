# CPF 정본 문서화 + 저품질 구현 재작성 + Sample 제거 + 검수 증적 보존 요청서

## 0. 작업 목적

CPF(CoreFlow Platform Framework)를 운영 가능한 범용 업무 프레임워크 수준으로 정리한다.

이번 작업은 단순 문서 보강이나 리포트 작성이 아니다.
현재 구현과 문서가 CPF 공식 프레임워크 기준에 맞는지 다시 확인하고, 부족하거나 품질이 낮은 부분은 실제로 수정/재작성해야 한다.

특히 README와 specs 가이드 문서는 너무 빈약하면 안 된다.
문서는 사람이 프로젝트를 이해하고, 개발자가 기능을 만들고, 운영자가 ADM을 사용하고, 검수자가 실제 구현 여부를 확인할 수 있는 **정본 문서**여야 한다.

---

## 1. 최우선 원칙

### 1.1 개발 완료 우선

이번 작업의 목적은 “못 한 항목을 리포트에 적는 것”이 아니다.
가능한 범위에서 실제 구현과 문서를 완료 수준으로 끌어올린다.

```text
1. 현재 구현을 확인한다.
2. 구현이 없으면 새로 만든다.
3. 구현이 있어도 품질이 낮으면 다시 만든다.
4. sample, hardcoding, in-memory 구조는 운영 기본 구현체에서 제거한다.
5. README와 가이드는 실제 소스/SQL/UI/API/EDU 기준으로 다시 작성한다.
6. 검수자가 나중에 대조할 수 있도록 모든 변경 증적을 CPF_STABILIZATION_REPORT.html에 남긴다.
```

### 1.2 미구현/미검증은 면책이 아니다

`미구현`, `미검증`, `재확인 필요`는 작업 회피용 상태가 아니다.

정말 완료하지 못한 경우에만 아래를 기록한다.

```text
- 왜 못 했는지
- 어디까지 확인했는지
- 어떤 파일/기능이 막혔는지
- 어떤 대안까지 검토했는지
- 다음에 무엇을 해야 하는지
- 완료하려면 어떤 검증이 필요한지
```

---

## 2. Git 및 파일 생성 금지 기준

```text
- Git commit 하지 않는다.
- Git push 하지 않는다.
- branch 생성하지 않는다.
- CPF_REQUEST.md 수정하지 않는다.
- CPF_NEW_REQUEST.md가 있으면 사용자 요청 파일로 보고 수정하지 않는다.
- CPF_STABILIZATION_CHANGED_FILES.txt 새로 만들지 않는다.
- 별도 변경파일 목록 txt/md/html을 새로 만들지 않는다.
- 변경 파일 목록과 증적은 CPF_STABILIZATION_REPORT.html 안에만 기록한다.
```

---

## 3. 전체 현황 재점검

작업 시작 전에 반드시 현재 상태를 확인한다.

### 확인 대상

```text
README.md
settings.gradle
build.gradle
pfw
cmn
adm
bizadm
mbr
exs
xyz/edu
specs
specs/sql
scripts
CPF_STABILIZATION_REPORT.html
```

### 확인 명령

```powershell
Get-Content README.md
Get-Content settings.gradle

Get-ChildItem -Recurse pfw/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse cmn/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse adm/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse bizadm/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse mbr/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse exs/src/main/java -Directory | Select-Object FullName
Get-ChildItem -Recurse xyz/src/main/java -Directory | Select-Object FullName

Get-ChildItem -Recurse -Directory | Where-Object { $_.Name -match 'sample|Sample' } | Select-Object FullName
Select-String -Path **/*.java,**/*.js,**/*.xml,**/*.sql,**/*.html,**/*.md -Pattern 'sample','Sample','List.of\(Map.of','ConcurrentHashMap','CopyOnWriteArrayList'
```

### 리포트 기록

`CPF_STABILIZATION_REPORT.html`에 아래를 기록한다.

```text
- 현재 sample 폴더 목록
- 현재 hardcoding 사용 위치
- 현재 README/specs 문서 품질 문제
- 현재 PFW/CMN 책임 경계 문제
- 현재 BIZADM/EXS 운영 구현 상태
- 현재 PFW batch/EDU batch 상태
- 재사용할 구현
- 재작성할 구현
- 삭제할 sample 폴더
```

---

## 4. README 및 가이드 문서 정본화

### 4.1 요청사항

README와 specs 문서는 단순 요약이 아니라 CPF 공식 정본 문서여야 한다.

현재 문서가 파일명에 맞는 역할을 하지 못하거나, 실제 개발 내용 전체를 설명하지 못하면 전면 재작성한다.

문서에는 실제 구현 파일, API, SQL, UI, Swagger, EDU, 검증 명령이 서로 연결되어 있어야 한다.

---

### 4.2 README.md 작성 기준

README는 루트 진입점이다.
짧아도 되지만 빈약하면 안 된다.

반드시 포함한다.

```text
1. CPF 개요
2. CPF가 금융권 전용이 아니라 금융권 수준 기준을 포함한 범용 업무 프레임워크라는 설명
3. 전체 모듈 구조
   - pfw
   - cmn
   - adm
   - bizadm
   - mbr
   - exs
   - bat 또는 배치 주제영역
   - xyz/edu
4. 각 모듈 책임
5. PFW/CMN 책임 경계
6. ADM/BIZADM/MBR/EXS/BAT/EDU 책임 경계
7. DB 구성과 소유권
8. pfwDB/cmnDB/admDB/bizadmDB/mbrDB/exsDB 역할
9. Spring Batch BATCH_*와 CPF pfw_batch_* 관계
10. transactionGlobalId 표준
11. 인증/인가/token realm 기준
12. 권한/감사/마스킹/다운로드 기준
13. Redis/Kafka/MQ mock/fallback 기준
14. 실행 방법
15. qualityGate 실행 방법
16. smoke 검증 방법
17. 주요 문서 링크
18. 현재 미검증 항목과 주의사항
```

완료 불인정:

```text
- README가 몇 줄짜리 요약 수준
- 모듈 책임이 불명확함
- 실제 소스/SQL/검증과 다름
- 문서 링크만 있고 설명이 없음
```

---

### 4.3 specs HTML 문서 공통 기준

`specs/*.html`은 실제 HTML이어야 한다.

필수 구조:

```html
<!doctype html>
<html lang="ko">
<head>
  <meta charset="utf-8">
  <title>문서 제목</title>
</head>
<body>
  <main>
    <section>
      <h1>문서 제목</h1>
    </section>
  </main>
</body>
</html>
```

실패 기준:

```text
- 첫 줄이 # 로 시작
- Markdown 표만 있음
- Markdown 링크 목록만 있음
- .html 확장자인데 실제 HTML 구조가 없음
- <main>, <section>, <table> 구조 없음
- 내용이 몇 줄짜리 요약 수준
- 파일명과 내용이 맞지 않음
```

---

### 4.4 specs/index.html 작성 기준

문서 포털 역할을 해야 한다.

포함 내용:

```text
1. CPF 개요
2. 문서 사용 순서
3. 개발자용 문서 흐름
4. 운영자용 문서 흐름
5. 검수자용 문서 흐름
6. 모듈 책임 요약
7. PFW/CMN/ADM/BIZADM/MBR/EXS/BAT/EDU 관계
8. 표준 요약
9. 현재 구현 상태 요약
10. 미검증/주의 항목 요약
11. 상세 문서 링크
```

---

### 4.5 프레임워크 구성 가이드 작성 기준

`specs/프레임워크_구성_가이드.html`은 CPF 아키텍처 정본이어야 한다.

포함 내용:

```text
1. 전체 아키텍처
2. 모듈 책임 경계
3. PFW와 CMN 책임 경계
4. ADM/BIZADM 분리 기준
5. MBR 기본 구현체 기준
6. EXS 대외연계 기본 구현체 기준
7. BAT 배치 주제영역 기준
8. Spring Batch 구조
9. PFW 배치 공통 API
10. BAT 실행 구현체
11. ADM 배치 관제
12. DB 소유권
13. pfwDB/cmnDB/admDB/bizadmDB/mbrDB/exsDB 관계
14. transactionGlobalId 전파 구조
15. 인증/인가/token realm 구조
16. 권한/감사/마스킹/다운로드 공통 구조
17. Redis/Kafka/MQ mock/fallback 구조
18. profile/local/dev/stg/prod 기준
19. qualityGate와 smoke 분리 기준
20. 운영 확장 기준
```

---

### 4.6 개발 가이드 작성 기준

`specs/개발_가이드.html`은 개발자가 이 문서와 EDU 샘플만 보고 신규 업무 기능을 만들 수 있어야 한다.

포함 내용:

```text
1. 신규 업무 모듈 개발 절차
2. 패키지 작성 규칙
3. Controller 작성 규칙
4. Service 작성 규칙
5. Repository/Mapper 작성 규칙
6. DTO/Validation 작성 규칙
7. SQL/Flyway 작성 규칙
8. 표준 응답/오류 사용법
9. transactionGlobalId 사용법
10. Authorization/Bearer token 사용법
11. 권한 검사 사용법
12. 감사 로그 남기는 법
13. 마스킹 적용 방법
14. 다운로드 사유/감사 적용 방법
15. NotificationSender 사용법
16. PFW 배치 공통 API 사용법
17. Spring Batch Job/Step 개발법
18. BAT 공통 실행 구조에 업무 Service/Facade 연결하는 법
19. EXS 수신/송신/재처리 개발법
20. BIZADM 업무 관리자 기능 개발법
21. MBR 회원 기능 개발법
22. EDU 샘플 매핑표
23. 테스트 작성 기준
24. qualityGate 통과 기준
25. 자주 하는 실수와 실패 사례
```

---

### 4.7 관리자 가이드 작성 기준

`specs/관리자_가이드.html`은 운영자가 ADM에서 무엇을 할 수 있는지 실제 화면/API/DB 기준으로 설명해야 한다.

포함 내용:

```text
1. ADM 로그인
2. 운영자 관리
3. 역할/권한 관리
4. 메뉴 권한
5. 버튼 권한
6. API 권한
7. 다운로드 권한
8. 온라인 거래 로그
9. 오류 로그
10. 오류 조치 상태/담당자/메모
11. 배치 Job 목록/상세
12. Spring Batch 실행 이력
13. CPF 배치 운영 메타
14. 수동 실행/재실행/중지
15. 스케줄/영업일/수행 대상
16. 알림 규칙
17. 알림 발송 이력
18. 동적 로그레벨
19. 공통 코드/메시지/설정
20. 보안/MFA/IP allowlist
21. 다운로드 감사
22. 마스킹/마스킹 해제 감사
23. BIZADM 관제
24. MBR 관제
25. EXS 관제
26. 장애 대응 절차
27. 미검증 항목과 운영 주의사항
```

---

### 4.8 SQL 가이드 작성 기준

`specs/SQL_가이드.html`은 설치/운영 DB 기준서여야 한다.

포함 내용:

```text
1. DB 소유권
2. pfwDB 역할
3. cmnDB 역할
4. admDB 역할
5. bizadmDB 역할
6. mbrDB 역할
7. exsDB 역할
8. bat 또는 배치 주제영역 DB 기준
9. Spring Batch BATCH_* 표준 테이블
10. CPF pfw_batch_* 운영 메타
11. BATCH_*와 pfw_batch_* 연결 기준
12. 테이블명 표준
13. 컬럼명 표준
14. 공통 감사 컬럼
15. 한글 COMMENT 기준
16. FK/UK/INDEX 기준
17. seed 기준
18. Flyway baseline 기준
19. migration 작성 기준
20. 00_all_install.sql 기준
21. 00_all_install_and_smoke.sql 기준
22. MariaDB smoke 실행 방법
23. 권한 계정 분리
24. rollback/보정 migration 기준
25. 자주 발생하는 SQL 오류
```

---

### 4.9 기능 구현 매트릭스 작성 기준

`specs/기능_구현_매트릭스.html`은 실제 HTML table 기반 검수 문서여야 한다.

필수 컬럼:

```text
대분류
기능명
구현 상태
주요 소스
API
ADM 화면
DB 테이블
Swagger Tag
EDU 샘플
테스트
검증 방법
예상 정상 결과
실제 검증 결과
미검증 사유
비고/다음 보완
```

상태값은 아래만 사용한다.

```text
완료
일부 구현
미구현
미검증
실패
재확인 필요
```

필수 기능 행:

```text
PFW transactionGlobalId
PFW 표준 응답/오류
PFW 공통 설정/메시지/응답코드
PFW 배치 공통 API
PFW/CMN 책임 경계
CMN 업무 공통 기능
ADM 운영자/권한
ADM 로그/오류
ADM 알림
ADM 다운로드 감사
ADM 배치 관제
BIZADM 업무 관리자 기본 구현체
MBR 회원 기본 구현체
EXS 대외연계 기본 구현체
BAT Spring Batch 실행 구현체
EDU 개발 샘플
SQL/Flyway
Swagger/OpenAPI
qualityGate
smoke 검증
```

완료로 표시하려면 실제 소스, SQL, UI/API wrapper, Swagger, EDU, 검증 결과가 있어야 한다.
문서만 있으면 완료가 아니다.

---

## 5. sample 폴더 제거

### 요청사항

운영 기본 구현체에서 `sample` 폴더나 `SampleController`, `SampleService`가 남아 있으면 안 된다.

sample이 남아 있다는 것은 아래 둘 중 하나다.

```text
1. 아직 개발이 끝나지 않았거나
2. sample 구조를 운영 구현체처럼 잘못 사용하고 있거나
```

따라서 `sample` 폴더는 삭제하거나 정식 구조로 이동한다.

### 처리 기준

```text
- pfw/cmn/adm/bizadm/mbr/exs/bat에 sample 패키지 금지
- 운영 API가 sample 패키지에 있으면 정식 도메인 패키지로 이동
- sample Controller/Service 제거
- List.of(Map.of) 기반 sample 데이터 제거
- 실제 DB 기반 Service/Repository 또는 Mapper 구조로 전환
- 교육용 샘플은 xyz/edu 하위로만 둔다
- xyz/edu도 sample 명칭이 아니라 edu 교육 코드로 정리한다
```

### 삭제/이동 대상 확인

```powershell
Get-ChildItem -Recurse -Directory | Where-Object { $_.Name -match 'sample|Sample' } | Select-Object FullName
Get-ChildItem -Recurse -Include *Sample*.java,*sample*.java | Select-Object FullName
Select-String -Path **/*.java -Pattern 'package .*sample','SampleController','SampleService','List.of\(Map.of'
```

### 완료 기준

```text
- 운영 모듈에 sample 폴더가 남아 있지 않음
- 운영 API에 SampleController/SampleService가 남아 있지 않음
- BIZADM/EXS 운영 기능이 sample 패키지가 아니라 정식 구조에 있음
- 교육 목적 코드는 xyz/edu에 있음
- sample 제거로 깨진 import/API/문서가 모두 수정됨
```

### 완료 불인정

```text
- sample 폴더가 남아 있음
- SampleController/SampleService가 남아 있음
- sample 이름만 바꾸고 내부 하드코딩 구조가 그대로임
- List.of(Map.of) 기반 운영 데이터가 남아 있음
```

---

## 6. BIZADM 운영 기본 구현체 재작성

### 요청사항

BIZADM은 sample이 아니라 업무/프로젝트 관리자 기본 구현체여야 한다.

현재 sample 기반이거나 하드코딩 기반이면 정식 DB 기반 구조로 재작성한다.

### 구현 범위

```text
- 프로젝트/업무 관리자 계정 관리
- 프로젝트/업무 관리자 로그인
- 로그아웃
- refresh token hash 저장
- 로그인 성공/실패 이력
- 역할 관리
- 메뉴 권한
- 버튼 권한
- API 권한
- 다운로드 권한
- 마스킹 권한
- 마스킹 해제 권한
- 고객 관리 참조 기능
- 상품 관리 참조 기능
- 주문 관리 참조 기능
- 설정 관리
- 다운로드 사유 필수
- 마스킹 해제 사유 필수
- 운영 감사
- 다운로드 감사
- 마스킹 해제 감사
```

### 구현 기준

```text
- Controller
- Service
- Repository 또는 Mapper
- DTO
- SQL/Flyway 또는 설치 SQL
- 권한 검사
- 감사 로그
- Swagger annotation
- ADM UI/API wrapper
- EDU 또는 개발 가이드 연결
```

### 검증

```powershell
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'package .*sample','SampleController','SampleService','List.of\(Map.of','ConcurrentHashMap','CopyOnWriteArrayList'
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'Login','Refresh','Role','Menu','Permission','Button','Download','Mask','Audit','Customer','Product','Order','Setting'
Select-String -Path specs/sql/**/*.sql -Pattern 'bizadm_'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'bizadm'
```

---

## 7. EXS 대외연계 기본 구현체 재작성

### 요청사항

EXS는 sample이 아니라 대외연계 기본 구현체여야 한다.

sample 기반이거나 하드코딩 기반이면 정식 DB 기반 구조로 재작성한다.

### 구현 범위

```text
- 대외기관 관리
- 대외 채널 관리
- endpoint 관리
- 인증 프로파일 관리
- 외부기관 token/secret 관리
- token 원문 노출 금지
- token/secret 암호화 또는 hash/reference 저장
- token refresh mock
- token event history
- routing rule 관리
- 수신 거래 로그 선저장
- 송신 거래 로그 선저장
- 전문 message log 선저장
- 기관/채널/endpoint 사용 가능 여부 확인
- routing rule 조회
- 업무 Service/Facade 호출 구조
- 성공 상태 갱신
- 실패 상태/오류 메시지 저장
- retryable 여부 판단
- 재처리 요청 저장
- 재처리 실행 Service
- 재처리 결과 저장
- 재처리 감사 로그
- 통제 정책 관리
```

### 수신/송신 처리 흐름

```text
1. X-Transaction-Id 필수 검증
2. transactionGlobalId 포맷 검증
3. 수신/송신 거래 로그 선저장
4. 전문 message log 선저장
5. 기관/채널/endpoint 사용 가능 여부 확인
6. routing rule 조회
7. 대상 업무 Service/Facade 호출
8. 성공 시 SUCCESS 상태 갱신
9. 실패 시 FAIL 상태 및 오류 메시지 저장
10. retryable 여부 판단
11. 재처리 요청 저장
12. 재처리 실행
13. 재처리 결과 저장
14. 재처리 감사 로그 저장
```

### 검증

```powershell
Select-String -Path exs/src/main/java/**/*.java -Pattern 'package .*sample','SampleController','SampleService','List.of\(Map.of','ConcurrentHashMap','CopyOnWriteArrayList'
Select-String -Path exs/src/main/java/**/*.java -Pattern 'Institution','Channel','Endpoint','Auth','Token','Secret','Route','Transaction','Message','Control','Retry','Reprocess','Facade'
Select-String -Path exs/src/main/java/**/*.java -Pattern 'X-Transaction-Id','transactionGlobalId','SUCCESS','FAIL','retryable'
Select-String -Path specs/sql/**/*.sql -Pattern 'exs_'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'exs'
```

---

## 8. PFW/CMN 책임 경계 재정리

### 요청사항

PFW와 CMN의 책임 경계를 다시 확인한다.
프레임워크 표준 기능 또는 `pfwDB` 기준 테이블을 CMN이 원천 관리하고 있으면 CPF 표준에 맞게 수정한다.

### 기준

PFW는 프레임워크 코어다.

PFW가 담당해야 하는 기능:

```text
- transactionGlobalId
- 표준 헤더
- 표준 응답
- 표준 오류
- 공통 예외
- 프레임워크 설정
- 프레임워크 응답코드
- 프레임워크 공통 메시지
- 프레임워크 보안 메타
- 프레임워크 배치 메타
- Spring Batch 공통 API
- JobRepository/JobLauncher 공통 설정
- 배치 lock/스케줄/실행 이력/Step 이력 연계 API
- 프레임워크 감사/추적 기준
```

CMN은 업무 공통 라이브러리다.

CMN 역할:

```text
- 업무 공통 유틸
- 업무 공통 코드 캐시 또는 조회 보조
- 파일 처리
- 암호화/마스킹 보조 유틸
- JWT/OAuth 보조 유틸
- WebClient/외부 호출 어댑터
- Redis/Kafka/MQ mock/fallback adapter
- 업무 공통 Validation
- 업무 공통 Notification Sender
- PFW API를 사용하는 캐시/보조 기능
```

### 수정 기준

```text
1. pfwDB 테이블의 원천 조회/관리 API는 PFW로 이동한다.
2. 프레임워크 표준 기능 API는 PFW에서 제공한다.
3. CMN은 PFW 표준 API를 대체 구현하지 않는다.
4. CMN이 해당 기능을 필요로 하면 PFW API를 호출하거나 캐시/보조 역할만 수행한다.
5. CMN에 있던 Mapper/Service가 PFW 책임이면 PFW로 이동하거나 PFW 기준으로 재구성한다.
6. 업무 모듈이 CMN을 통해 PFW 표준 기능을 우회 사용하고 있으면 PFW API 사용 구조로 수정한다.
7. EDU 샘플도 CMN 직접 호출이 아니라 PFW 공통 API 사용 예제로 수정한다.
8. README, 개발 가이드, SQL 가이드, 기능 구현 매트릭스도 실제 구조에 맞게 현행화한다.
```

### 검증

```powershell
Select-String -Path pfw/src/main/java/**/*.java -Pattern 'code','message','config','response','security','batch','JobRepository','JobLauncher','transactionGlobalId'
Select-String -Path cmn/src/main/java/**/*.java -Pattern 'pfw_','Pfw','Framework','JobRepository','JobLauncher','transactionGlobalId','responseCode','message','config','security','batch'
Select-String -Path specs/sql/**/*.sql -Pattern 'pfw_','cmn_','BATCH_','batch'
Select-String -Path xyz/src/main/java/**/*.java -Pattern 'Pfw','Cmn','JobRepository','JobLauncher','transactionGlobalId','responseCode','message','config','batch'
```

---

## 9. PFW 배치 공통 API + BAT + EDU 배치 샘플 정리

### 요청사항

PFW는 배치 프레임워크 공통 API를 제공한다.
BAT 또는 배치 주제영역은 PFW 배치 공통 API를 사용해서 Spring Batch 실행 구현체를 제공한다.
EDU는 PFW 배치 공통 API를 사용하는 교육 코드로 구성한다.

### PFW 제공 기능

```text
- Spring Batch JobRepository 공통 설정
- Spring Batch JobLauncher 공통 사용 기준
- Batch TransactionManager/DataSource 연결 기준
- Job/Step 실행 표준 파라미터 모델
- 배치 실행 요청 모델
- 배치 실행 결과 모델
- 배치 상태 표준 코드
- 배치 오류 표준 코드
- 배치 transactionGlobalId 생성/전파
- moduleId/wasId/serverInstanceId 기록 표준
- 배치 lock 공통 API
- 중복 실행 방지 공통 API
- 재실행 가능 여부 판단 API
- 배치 실행 전/후 이벤트 API
- 실패/지연/미수행 알림 이벤트 API
- 스케줄 실행 대상 판단 API
- 수동 실행 공통 API
- 배치 실행 이력 저장 연계 API
- Step 실행 이력 저장 연계 API
- Spring Batch JobExecutionId/StepExecutionId와 CPF 운영 이력 연결 API
```

### BAT 기준

```text
- PFW 배치 공통 API를 사용
- 독립 JobRepository 임의 생성 금지
- Spring Batch 표준 BATCH_* 사용
- CPF pfw_batch_* 운영 메타와 연결
- ADM 배치 관제와 연결
```

### EDU 기준

```text
- PFW 배치 공통 API를 이용한 Job 등록 예시
- PFW 배치 공통 API를 이용한 Step 등록 예시
- PFW 배치 공통 API를 이용한 수동 실행 예시
- transactionGlobalId 전파 예시
- JobExecutionId와 CPF 운영 이력 연결 예시
- 실패 알림 이벤트 예시
- 업무 Service/Facade 호출 예시
```

### Java 포맷 기준

PFW batch Java 파일은 정상 물리 포맷이어야 한다.

```text
- package 단독 라인
- import 각각 단독 라인
- class/record/interface 선언 정상 개행
- field/constructor/method 정상 개행
- 장문 한 줄 Java 금지
- 10줄 미만 Java 파일은 원칙적으로 실패
```

---

## 10. 품질 검사 스크립트 강화

### check-html-docs 강화

```text
- .html 파일 첫 줄이 # 이면 실패
- Markdown table만 있고 <table> 없으면 실패
- <!doctype html> 없으면 실패
- <html lang="ko"> 없으면 실패
- <head> 없음 실패
- <body> 없음 실패
- <main> 없음 실패
- <section> 없음 실패
- 기능 구현 매트릭스에 <table> 없음 실패
- CPF_STABILIZATION_REPORT.html에 요청별 검증 section 없음 실패
```

### check-java-format 강화

```text
- package/import/class/method가 한 줄에 붙으면 실패
- Java 파일 10줄 미만이면 검토 대상
- 장문 한 줄 Java 실패
- annotation/class/method가 한 줄에 과도하게 붙으면 실패
```

### sample/hardcoding 검사 추가

```text
- 운영 모듈 sample 폴더 존재 시 실패
- SampleController/SampleService 존재 시 실패
- List.of(Map.of) 운영 데이터 사용 시 실패
- ConcurrentHashMap/CopyOnWriteArrayList 운영 저장소 사용 시 실패
```

---

## 11. SQL/Flyway/Mapper 정합성

### 요청사항

Java에서 사용하는 테이블/컬럼/Mapper는 SQL/Flyway와 맞아야 한다.

### 필수 기준

```text
- Java에서 사용하는 테이블은 SQL에 존재
- Mapper method와 XML SQL 일치
- 모든 신규 테이블/컬럼 COMMENT 존재
- 모든 신규 CPF 운영 테이블에 공통 감사 컬럼 존재
- Spring Batch 표준 BATCH_*와 CPF pfw_batch_* 목적 분리
- BATCH_*와 pfw_batch_* 연결 기준 존재
```

### 검증

```powershell
Get-ChildItem -Recurse specs/sql -Include *.sql
Get-ChildItem -Recurse */src/main/resources -Include *.xml,*.sql,*.yml
Select-String -Path **/*.java -Pattern 'JdbcTemplate','Mapper','select','insert','update','delete'
Select-String -Path specs/sql/**/*.sql -Pattern 'adm_','bizadm_','mbr_','exs_','pfw_','cmn_','BATCH_','batch'
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

---

## 12. Swagger/OpenAPI 및 UI/API wrapper

### 요청사항

API만 있으면 완료가 아니다.
운영자가 사용할 수 있는 ADM UI/API wrapper와 Swagger annotation이 있어야 한다.

### 필수 기준

```text
- @Tag
- @Operation
- @Parameter
- 표준 헤더 설명
- 표준 응답/오류 설명
- ADM static UI/API wrapper
- 권한 없는 버튼 숨김/비활성
- 서버 API 권한 차단
```

### 검증

```powershell
Select-String -Path **/*.java -Pattern '@Tag','@Operation','@Parameter','X-Transaction-Id','ApiResponse'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'adm','bizadm','mbr','exs','bat','batch','operator','permission','member','institution','token','retry','download','mask','audit'
```

---

## 13. CPF_STABILIZATION_REPORT.html 작성 기준

리포트는 실제 HTML이어야 한다.
첫 줄이 `#`이면 실패다.

리포트에는 모든 작업 증적이 남아야 한다.

### 필수 기록

```text
1. 작업 기준 branch/commit
2. 요청사항별 기존 상태
3. 요청사항별 문제점
4. 재사용한 파일
5. 보강한 파일
6. 재작성한 파일
7. 삭제한 sample 폴더/파일
8. 신규 생성 파일
9. 변경한 SQL
10. 변경한 UI/API wrapper
11. 변경한 Swagger/EDU
12. 실행한 검증 명령
13. 검증 성공/실패/미검증 결과
14. 미검증 사유
15. 남은 리스크
16. 다음 조치
17. CPF_REQUEST.md/CPF_NEW_REQUEST.md 수정 여부
18. Git commit/push 미수행 여부
```

### 요청별 표 필수 컬럼

```text
요청 ID
요청사항
기존 상태
문제점
수정 방식
변경 파일
검증 명령
예상 정상 결과
실제 결과
판정
미검증/실패 사유
다음 조치
```

---

## 14. 최종 검증 명령

가능한 범위에서 실행한다.

```powershell
.\gradlew.bat compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline

powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-java-format.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-security-seed-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-transaction-id-standard.ps1
```

선택 검증:

```powershell
node --check adm/src/main/resources/static/adm/adm.js
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
curl http://localhost:8080/v3/api-docs
curl http://localhost:8080/swagger-ui/index.html
```

실행하지 못한 항목은 반드시 미검증으로 기록한다.
실행하지 않았는데 성공으로 기록하면 실패다.

---

## 15. 완료 인정 기준

아래를 모두 만족해야 완료로 인정한다.

```text
- README가 CPF 진입 문서 수준으로 충분히 보강됨
- specs/*.html이 실제 HTML 정본 문서임
- 각 가이드 문서가 파일명에 맞는 역할과 내용을 가짐
- 기능 구현 매트릭스가 실제 HTML table 검수 문서임
- CPF_STABILIZATION_REPORT.html이 실제 HTML 리포트임
- 운영 모듈에 sample 폴더가 없음
- SampleController/SampleService가 없음
- List.of(Map.of) 운영 데이터 하드코딩 없음
- BIZADM이 DB 기반 운영 구현체로 정리됨
- EXS가 DB 기반 대외연계 구현체로 정리됨
- PFW/CMN 책임 경계가 맞음
- PFW 배치 공통 API가 존재함
- BAT가 PFW 배치 공통 API를 사용함
- EDU가 PFW 공통 API를 사용하는 교육 코드로 정리됨
- SQL/Flyway/Mapper 정합성이 맞음
- ADM UI/API wrapper가 있음
- Swagger annotation이 있음
- qualityGate와 각 검사 결과가 리포트와 일치함
```

---

## 16. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 기록하지 않는다.

```text
- README가 몇 줄짜리 요약 수준
- .html 파일 첫 줄이 # 으로 시작
- specs HTML이 Markdown 문서
- 기능 구현 매트릭스가 실제 table이 아님
- 리포트가 실제 HTML이 아님
- sample 폴더가 운영 모듈에 남아 있음
- SampleController/SampleService가 남아 있음
- List.of(Map.of) 하드코딩이 남아 있음
- BIZADM/EXS가 sample 기반
- PFW 책임 기능을 CMN이 원천 관리
- EDU가 CMN 또는 sample 구조를 통해 PFW 표준을 우회 사용
- PFW batch Java가 장문 한 줄
- check-html-docs가 Markdown형 HTML을 통과시킴
- check-java-format가 장문 한 줄 Java를 통과시킴
- MariaDB 미실행인데 성공으로 기록
- OpenAPI 미실행인데 성공으로 기록
- 문서에는 완료라고 되어 있는데 실제 소스/SQL/UI/Swagger/EDU가 없음
```

---

## 17. Codex 최종 보고 형식

작업 완료 후 아래 형식으로 보고한다.

```text
[소스 기준]
branch:
local HEAD:
origin/master HEAD:
git status 요약:

[문서 정본화]
README:
index:
프레임워크 구성 가이드:
개발 가이드:
관리자 가이드:
SQL 가이드:
기능 구현 매트릭스:
리포트:

[Sample 제거]
삭제한 sample 폴더:
삭제한 SampleController/Service:
정식 구조로 이동한 기능:
남은 sample이 있다면 사유:

[저품질 재작성]
재작성한 Java:
재작성한 JS:
재작성한 HTML:
재작성한 SQL/XML:

[BIZADM]
구현 파일:
SQL:
UI wrapper:
Swagger:
검증 결과:

[EXS]
구현 파일:
SQL:
UI wrapper:
Swagger:
검증 결과:

[PFW/CMN 책임 경계]
PFW로 이동/재구성한 기능:
CMN에 남긴 보조 기능:
EDU 수정:

[PFW Batch/BAT/EDU]
PFW 배치 공통 API:
BAT 구현체:
EDU 배치 샘플:
Spring Batch 메타 연결:
ADM 관제:

[검증 결과]
compileJava:
test:
qualityGate:
check-html-docs:
check-java-format:
check-legacy-name:
check-security-seed-standard:
check-sql-standard:
check-transaction-id-standard:
node check:
MariaDB smoke:
OpenAPI smoke:

[미검증/실패]
항목:
사유:
다음 조치:

[금지사항 준수]
Git commit/push 여부:
CPF_REQUEST.md 수정 여부:
CPF_NEW_REQUEST.md 수정 여부:
별도 변경파일 생성 여부:
```
