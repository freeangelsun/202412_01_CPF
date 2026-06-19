# CPF 전체 품질 보강 요청서

## 0. 작업 목표

CPF(CoreFlow Platform Framework)를 단순 샘플 프로젝트가 아니라, 금융권 수준의 운영 기준을 포함한 범용 업무 프레임워크 품질로 보강한다.

이번 작업은 속도보다 품질을 우선한다.
요청사항을 하나도 누락하지 말고, 처리하지 못한 항목은 숨기지 말고 `미구현`, `미검증`, `실패`, `재확인 필요`로 기록한다.

이번 작업의 핵심 목표는 다음과 같다.

```text id="f5k05s"
1. Codex 보고와 GitHub 실제 소스 상태 불일치 제거
2. CPF_STABILIZATION_REPORT.html과 specs/*.html 실제 HTML화
3. Java 물리 포맷 정상화
4. BIZADM/MBR/EXS 기본 구현체 품질 보강
5. sample/in-memory/hardcoding 잔재 제거 또는 명확한 예제 영역으로 분리
6. DB/Mapper/Repository/SQL/Flyway 정합성 보강
7. BIZADM/MBR/EXS UI/route/API wrapper 최소 연결
8. EDU 개발자 참조 구현 보강
9. 공통 인증/인가/권한/마스킹/다운로드/감사/알림/MFA/EXS 대외토큰 기준 정리
10. 요청사항별 검증 방법, 예상 정상 결과, 실제 결과, 증적을 리포트에 남김
```

Git commit, push, branch 생성은 하지 않는다.

`CPF_REQUEST.md`는 요청 확인용으로만 읽고 수정하지 않는다.

---

# 1. 공통 작업 원칙

## 1.1 완료 보고 금지 기준

다음은 절대 `완료`로 기록하지 않는다.

```text id="ot7j8h"
- 실제 소스가 없는데 문서만 작성한 경우
- sample/in-memory/hardcoding인데 기본 구현체처럼 보고한 경우
- DB 실실행을 하지 않았는데 DB smoke 성공이라고 기록한 경우
- 앱을 기동하지 않았는데 OpenAPI HTTP smoke 성공이라고 기록한 경우
- 브라우저 클릭을 하지 않았는데 UI smoke 성공이라고 기록한 경우
- .html 파일이 Markdown 형식인데 HTML 문서 완료라고 기록한 경우
- Java 파일이 raw 기준 장문 한 줄인데 Java format 성공이라고 기록한 경우
- qualityGate 성공 결과와 실제 파일 상태가 충돌하는 경우
- Vue 화면/route/API wrapper가 없는데 화면 완료라고 기록한 경우
- 기능 구현 매트릭스에 과장해서 완료로 표시한 경우
```

## 1.2 상태값 기준

모든 요청사항과 기능 매트릭스 상태는 아래 값만 사용한다.

```text id="m1jtby"
완료
일부 구현
미구현
미검증
실패
재확인 필요
```

## 1.3 리포트 기록 위치

작업 결과와 검증 증적은 반드시 아래 파일 하나에 기록한다.

```text id="en319s"
CPF_STABILIZATION_REPORT.html
```

새로운 검토보고서 파일을 만들지 않는다.

생성 금지 파일:

```text id="u0tx91"
CPF_STABILIZATION_CHANGED_FILES.txt
CPF_STABILIZATION_REPORT.md
CPF_REVIEW_REPORT.html
CPF_REVIEW_REPORT.md
기타 별도 변경파일 목록
```

---

# 2. 리포트 증적 작성 표준

`CPF_STABILIZATION_REPORT.html`은 실제 HTML 문서여야 한다.

필수 구조:

```html id="pm37ky"
<!doctype html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <title>CPF 안정화 작업 리포트</title>
</head>
<body>
<main>
  <section>
    <h1>CPF 안정화 작업 리포트</h1>
  </section>
</main>
</body>
</html>
```

Markdown 금지:

````text id="vv53m3"
# 제목
## 제목
| Markdown table |
``` 코드펜스
````

## 2.1 요청사항별 증적 표 필수

리포트에는 요청사항별로 아래 표를 반드시 작성한다.

```html id="j2kd32"
<table>
  <thead>
    <tr>
      <th>요청 ID</th>
      <th>요청사항</th>
      <th>변경 파일</th>
      <th>검증 방법</th>
      <th>예상 정상 결과</th>
      <th>실제 결과</th>
      <th>증적</th>
      <th>판정</th>
      <th>미검증/실패 사유</th>
      <th>잔여 과제</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>REQ-001</td>
      <td>CPF_STABILIZATION_REPORT.html 실제 HTML화</td>
      <td>CPF_STABILIZATION_REPORT.html</td>
      <td>check-html-docs.ps1 실행</td>
      <td>Markdown 잔재 없음, HTML 구조 존재</td>
      <td>성공/실패 실제 결과 기록</td>
      <td>명령어, 결과 요약, 파일 경로</td>
      <td>완료/실패</td>
      <td>실패 시 사유</td>
      <td>남은 작업</td>
    </tr>
  </tbody>
</table>
```

## 2.2 증적 예시

성공 증적 예시:

```text id="i57yde"
검증 명령:
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1

예상 정상 결과:
- CPF_STABILIZATION_REPORT.html에 <!doctype html> 존재
- specs/*.html에 <html lang="ko"> 존재
- Markdown 제목/표/코드펜스 없음
- 불필요 산출물 없음

실제 결과:
- check-html-docs.ps1 성공
- 검사 대상 7개 HTML 문서 통과
- CPF_STABILIZATION_CHANGED_FILES.txt 없음
- CPF_STABILIZATION_REPORT.md 없음

증적:
- 실행 명령
- 성공 메시지 요약
- 검사 대상 파일 목록
```

실패 증적 예시:

```text id="qby5xw"
검증 명령:
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-java-format.ps1

예상 정상 결과:
- 장문 한 줄 Java 파일 없음
- package/import/class/method가 분리됨

실제 결과:
- 실패
- bizadm/src/main/java/.../BizAdmAuthService.java 1개 장문 라인 감지

판정:
실패

잔여 과제:
해당 Java 파일 물리 개행 보정 후 재검증 필요
```

미검증 증적 예시:

```text id="ftev94"
검증 항목:
MariaDB smoke

예상 정상 결과:
- 00_all_install_and_smoke.sql 실행 성공
- BIZADM/MBR/EXS 주요 테이블 생성 확인
- smoke 데이터 조회 성공

실제 결과:
미검증

미검증 사유:
현재 환경에 MariaDB 클라이언트 또는 서버가 없어 실행 불가

판정:
미검증
```

---

# 3. 작업 전 기준 확인

## 요청사항

작업 시작 전 현재 소스 기준을 명확히 기록한다.

## 검증 방법

```powershell id="m7ihd9"
git status --short
git branch --show-current
git rev-parse HEAD
git ls-files CPF_STABILIZATION_CHANGED_FILES.txt CPF_STABILIZATION_REPORT.md CPF_STABILIZATION_REPORT.html
```

가능하면 원격 기준도 확인한다.

```powershell id="ar8l0a"
git fetch origin
git rev-parse origin/master
git log --oneline -1
```

## 예상 정상 결과

```text id="ipscuq"
- 현재 branch 기록
- local HEAD 기록
- origin/master HEAD 기록
- git status 기록
- CPF_STABILIZATION_CHANGED_FILES.txt 없음
- CPF_STABILIZATION_REPORT.md 없음
- CPF_STABILIZATION_REPORT.html만 존재
```

## 리포트 증적

`CPF_STABILIZATION_REPORT.html`에 `소스 기준 확인` 섹션을 만들고 위 결과를 기록한다.

---

# 4. 루트 산출물 정리

## 요청사항

루트에는 최종 안정화 리포트 HTML 하나만 유지한다.

삭제 대상:

```text id="mmpq9y"
CPF_STABILIZATION_CHANGED_FILES.txt
CPF_STABILIZATION_REPORT.md
```

## 검증 방법

```powershell id="q0lxgt"
Test-Path .\CPF_STABILIZATION_CHANGED_FILES.txt
Test-Path .\CPF_STABILIZATION_REPORT.md
git ls-files CPF_STABILIZATION_CHANGED_FILES.txt CPF_STABILIZATION_REPORT.md
```

## 예상 정상 결과

```text id="n0l1os"
- Test-Path 결과 false
- git ls-files 결과 없음
```

## 리포트 증적

파일별 존재 여부와 git 추적 여부를 기록한다.

---

# 5. HTML 문서 전면 보정

## 요청사항

다음 문서는 실제 HTML이어야 한다.

```text id="xi0n2b"
README.md
CPF_STABILIZATION_REPORT.html
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
```

README.md는 Markdown 허용.
나머지 `.html` 문서는 실제 HTML이어야 한다.

## 검증 방법

```powershell id="r8ob1a"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
```

추가 직접 검사:

````powershell id="ykf7d4"
Select-String -Path CPF_STABILIZATION_REPORT.html,specs/*.html -Pattern '^# ','^\|','```'
Select-String -Path CPF_STABILIZATION_REPORT.html,specs/*.html -Pattern '<!doctype html>','<html lang="ko">','<main>','<section>','<table>'
````

## 예상 정상 결과

```text id="xx8o21"
- Markdown 제목 없음
- Markdown 표 없음
- 코드펜스 없음
- <!doctype html> 존재
- <html lang="ko"> 존재
- <main> 존재
- <section> 존재
- 필요한 검수표는 <table>로 작성
- check-html-docs.ps1 성공
```

## 리포트 증적

문서별로 다음을 표로 기록한다.

```text id="pz0rga"
파일명
HTML 구조 검사 결과
Markdown 잔재 검사 결과
table 존재 여부
판정
```

---

# 6. Java 물리 포맷 전면 보정

## 요청사항

모든 Java 파일은 GitHub raw 기준으로도 정상 물리 개행을 가져야 한다.

금지:

```text id="t7x4pk"
package와 import가 한 줄에 붙음
여러 import가 한 줄에 붙음
class 선언과 method 선언이 한 줄에 붙음
annotation/class/method가 한 줄에 과도하게 붙음
Java 파일이 2~10줄짜리 장문 파일로 보임
한 줄 길이가 과도하게 김
```

## 대상

```text id="g0hob7"
pfw/**/*.java
cmn/**/*.java
adm/**/*.java
bizadm/**/*.java
mbr/**/*.java
exs/**/*.java
xyz/**/*.java
acc/**/*.java
```

## 검증 방법

```powershell id="ud5gny"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-java-format.ps1
```

추가 검사:

```powershell id="xgrcos"
Get-ChildItem -Recurse -Filter *.java | Where-Object {
  (Get-Content $_.FullName).Count -lt 20
}
```

## 예상 정상 결과

```text id="o1j6sn"
- 장문 한 줄 Java 파일 없음
- package/import/class/method가 적절히 개행됨
- 신규/수정 Java 파일이 비정상적으로 짧은 줄 수가 아님
- check-java-format.ps1 성공
```

## 리포트 증적

다음 항목을 기록한다.

```text id="v6w9ka"
- 검사 대상 Java 파일 수
- 20줄 미만 Java 파일 목록
- 보정한 Java 파일 목록
- check-java-format.ps1 결과
```

---

# 7. FPS/Fps/fps 레거시 명칭 전수 제거

## 요청사항

FPS/Fps/fps는 CPF 기준 레거시 명칭이다.
파일 경로, Java, YAML, Gradle, SQL, Vue, README, specs, Swagger, 리포트, 스크립트, 테스트 전체에서 제거한다.

## 검증 방법

```powershell id="n89frj"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1
```

추가 수동 검사:

```powershell id="fhmrt9"
Select-String -Path **/* -Pattern 'FPS','Fps','fps' -CaseSensitive
```

## 예상 정상 결과

```text id="qc99uk"
- 실제 모듈명/패키지명/클래스명/설정명에 FPS/Fps/fps 없음
- 과거 명칭 설명이 불가피한 경우에만 예외 처리
- 예외는 리포트에 파일/라인/사유 기록
```

## 리포트 증적

```text id="sq75xu"
- 검사 명령
- 검사 대상
- 발견 건수
- 제거 건수
- 남긴 예외와 사유
```

---

# 8. BIZADM 기본 구현체 보강

## 8.1 패키지 구조 정리

### 요청사항

BIZADM 기본 구현체는 `sample` 중심이면 안 된다.
다음 기능 그룹 기준으로 정리한다.

```text id="ei099g"
cpf.bizadm.auth
cpf.bizadm.operator
cpf.bizadm.role
cpf.bizadm.menu
cpf.bizadm.permission
cpf.bizadm.download
cpf.bizadm.masking
cpf.bizadm.audit
cpf.bizadm.customer
cpf.bizadm.product
cpf.bizadm.order
cpf.bizadm.setting
```

`sample/demo/example`은 교육/예제에만 허용한다.

### 검증 방법

```powershell id="cqa31q"
Get-ChildItem -Recurse bizadm/src/main/java -Directory
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'package .*sample','ConcurrentHashMap','CopyOnWriteArrayList','List.of\(Map.of'
```

### 예상 정상 결과

```text id="o8y85e"
- 기본 구현체 주요 코드는 sample 패키지에 없음
- sample 패키지는 예제 목적으로만 남음
- in-memory 저장소 없음
- 하드코딩 List.of(Map.of) 기본 구현체에 없음
```

### 리포트 증적

기능 그룹별 패키지와 주요 파일을 기록한다.

---

## 8.2 BIZADM 인증/토큰 DB 영속화

### 요청사항

BIZADM 로그인, refresh, logout, 현재 사용자, 로그인 이력은 DB 기반이어야 한다.

필수:

```text id="md17t2"
- ID/PW 로그인
- BCrypt 또는 공통 PasswordEncoder 검증
- Access Token 발급
- Refresh Token hash 저장
- Refresh Token 원문 저장 금지
- 로그인 성공 이력 저장
- 로그인 실패 이력 저장
- 실패 사유 코드 저장
- logout 시 refresh token 폐기
- token에 loginRealm 또는 loginDomain = BIZADM 포함
- transactionGlobalId/moduleId/wasId/serverInstanceId 저장
```

### 검증 방법

```powershell id="jlhzyb"
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'BizAdmAuthRepository','bizadm_admin_user','bizadm_login_history','bizadm_refresh_token'
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'ConcurrentHashMap','CopyOnWriteArrayList','refreshTokensByHash'
Select-String -Path specs/sql/**/*.sql -Pattern 'bizadm_admin_user','bizadm_login_history','bizadm_refresh_token'
```

### 예상 정상 결과

```text id="m9d5ty"
- BIZADM 인증 Service는 DB Repository 사용
- in-memory token 저장소 없음
- refresh token 원문 저장 없음
- 로그인 성공/실패 이력 DB 저장 코드 있음
- SQL/Flyway에 관련 테이블 있음
```

### 리포트 증적

```text id="g3cnd3"
- Controller/Service/Repository 경로
- 관련 DB 테이블
- SQL/Flyway 경로
- in-memory 검사 결과
- 미검증이면 사유
```

---

## 8.3 BIZADM 권한/메뉴/버튼/API/다운로드/마스킹/감사

### 요청사항

BIZADM은 CPF 적용 프로젝트의 업무 관리자 기본 구현체다.
다음 기능을 실제 구현한다.

```text id="hgm09k"
- 프로젝트 운영자 관리
- 역할 관리
- 메뉴 권한
- 버튼 권한
- API 권한
- 다운로드 권한
- 마스킹 권한
- 마스킹 해제 권한
- 다운로드 사유 필수
- 마스킹 해제 사유 필수
- 운영 감사 로그
- 다운로드 감사 로그
```

### 검증 방법

```powershell id="ah7b1g"
Select-String -Path bizadm/src/main/java/**/*.java -Pattern 'role','menu','button','api','download','mask','audit','permission'
Select-String -Path specs/sql/**/*.sql -Pattern 'bizadm_role','bizadm_menu','bizadm_permission','bizadm_download','bizadm_audit'
```

### 예상 정상 결과

```text id="vzg8hm"
- 각 기능별 Controller/Service/Repository 또는 Mapper 존재
- 관련 SQL/Flyway 존재
- 권한 없는 API 차단 기준 존재
- 다운로드/마스킹 해제 사유 필수 검증 존재
- 감사 로그 저장 코드 존재
```

### 리포트 증적

기능별로 `완료/일부 구현/미구현`을 구분하고, 소스/SQL/API를 기록한다.

---

# 9. MBR 기본 구현체 보강

## 9.1 MBR 인증 DB 영속화

### 요청사항

MBR 인증은 in-memory 없이 DB 기반으로 동작해야 한다.

필수:

```text id="jhbni3"
- 회원 로그인
- 회원 로그아웃
- refresh token hash DB 저장
- refresh token 원문 저장 금지
- 로그인 성공 이력 저장
- 로그인 실패 이력 저장
- 로그인 실패 횟수 증가
- 계정 잠금
- 회원 상태별 로그인 제한
- token refresh 이력 저장
- transactionGlobalId/moduleId/wasId/serverInstanceId 저장
```

### 검증 방법

```powershell id="y3464i"
Select-String -Path mbr/src/main/java/**/*.java -Pattern 'ConcurrentHashMap','CopyOnWriteArrayList','seedLocalMember','members =','refreshTokensByHash'
Select-String -Path mbr/src/main/java/**/*.java -Pattern 'MemberMapper','insertRefreshToken','insertMemberLoginHistory','selectRefreshTokenByHash','revokeRefreshTokenByHash'
Select-String -Path mbr/src/main/resources/**/*.xml,mbr/src/main/resources/**/*.yml -Pattern 'insertRefreshToken','insertMemberLoginHistory','selectRefreshTokenByHash'
Select-String -Path specs/sql/**/*.sql -Pattern 'mbr_refresh_token','mbr_login_history','login_fail','lock'
```

### 예상 정상 결과

```text id="kpjj7b"
- MbrAuthService에 in-memory 저장소 없음
- MemberMapper 또는 Repository 기반 DB 처리
- Mapper XML 또는 annotation SQL 존재
- 로그인 이력/refresh token 관련 SQL 존재
- 실패 횟수/잠금 처리 코드 존재
```

### 리포트 증적

```text id="ifqit5"
- MbrAuthService 경로
- Mapper/Repository 경로
- XML/SQL 경로
- in-memory 검사 결과
- 로그인 성공/실패/refresh/logout 검증 상태
```

---

## 9.2 MBR 회원 운영 기능

### 요청사항

회원 기본 구현체는 로그인만으로 끝나면 안 된다.

필수:

```text id="yaeahi"
- 회원 목록
- 회원 상세
- 회원 상태 변경
- 회원 로그인 이력 조회
- 회원 개인정보 기본 마스킹
- 마스킹 해제 권한/사유
- 회원 데이터 다운로드
- 다운로드 사유
- 다운로드 감사
- 회원 변경 감사
```

### 검증 방법

```powershell id="swq25o"
Select-String -Path mbr/src/main/java/**/*.java -Pattern 'member','history','mask','download','audit','status'
Select-String -Path specs/sql/**/*.sql -Pattern 'mbr_member','mbr_login_history','mbr_audit','download'
```

### 예상 정상 결과

```text id="eg2rff"
- 회원 운영 Controller/Service/Mapper 존재
- 마스킹/다운로드/감사 처리 코드 존재
- 관련 SQL 존재
- 기능 매트릭스에 실제 상태 반영
```

### 리포트 증적

기능별 소스/SQL/API/UI 여부를 기록한다.

---

# 10. EXS 대외연계 기본 구현체 보강

## 10.1 EXS 패키지 구조 정리

### 요청사항

EXS 기본 구현체는 `sample` 중심이면 안 된다.

기능 그룹:

```text id="a7y9uu"
cpf.exs.institution
cpf.exs.channel
cpf.exs.endpoint
cpf.exs.authprofile
cpf.exs.token
cpf.exs.route
cpf.exs.transaction
cpf.exs.message
cpf.exs.control
cpf.exs.retry
cpf.exs.operation
cpf.exs.flow
```

### 검증 방법

```powershell id="xo0exn"
Get-ChildItem -Recurse exs/src/main/java -Directory
Select-String -Path exs/src/main/java/**/*.java -Pattern 'package .*sample','ConcurrentHashMap','CopyOnWriteArrayList','List.of\(Map.of'
```

### 예상 정상 결과

```text id="n45o5k"
- 기본 구현체 핵심 코드는 sample에 없음
- sample은 EDU/예제 목적으로만 남음
- in-memory 저장소 없음
```

### 리포트 증적

EXS 기능 그룹별 주요 파일과 sample 잔재 처리 결과를 기록한다.

---

## 10.2 EXS 기관/채널/Endpoint/Auth Profile/Token

### 요청사항

EXS는 대외기관 연계 기본 구현체로 다음을 제공해야 한다.

```text id="sp44gh"
- 대외기관 관리
- 채널 관리
- endpoint 관리
- 인증 프로파일 관리
- 외부기관 token/secret 관리
- token 원문 화면/로그 노출 금지
- token/secret hash 또는 암호화 저장
- token refresh mock
- token event history
- 기관/endpoint 중지 상태 통제
```

### 검증 방법

```powershell id="kion3n"
Select-String -Path exs/src/main/java/**/*.java -Pattern 'institution','channel','endpoint','authProfile','token','secret','refresh'
Select-String -Path specs/sql/**/*.sql -Pattern 'exs_institution','exs_channel','exs_endpoint','exs_auth','exs_token'
```

### 예상 정상 결과

```text id="usvjpm"
- 각 기능별 Controller/Service/Repository 존재
- token 원문 저장/로그 출력 없음
- token hash/마스킹 값 저장
- token event history 저장
- 관련 SQL/Flyway 존재
```

### 리포트 증적

기능별 소스/SQL/API와 token 원문 노출 검사 결과를 기록한다.

---

## 10.3 EXS 수신/송신 선저장, 라우팅, 실패 갱신, 재처리

### 요청사항

EXS는 단순 선저장 API로 끝나면 안 된다.
대외 수신/송신 거래의 운영 흐름을 기본 구현체로 제공한다.

필수:

```text id="z30a32"
- X-Transaction-Id 필수 검증
- transactionGlobalId 포맷 검증
- 수신 거래 선저장
- 송신 거래 선저장
- message log 선저장
- routing rule 조회
- 대상 업무 Service/Facade 호출 구조
- 업무 호출 성공 시 상태 갱신
- 업무 호출 실패 시 실패 상태/오류 메시지 저장
- 재처리 요청 저장
- 재처리 실행 Service
- 재처리 결과 저장
- 재처리 감사 로그
```

### 검증 방법

```powershell id="nlxksh"
Select-String -Path exs/src/main/java/**/*.java -Pattern 'X-Transaction-Id','TransactionIdGenerator.isValid','saveExchangeLog','route','retry','reprocess','status','error'
Select-String -Path specs/sql/**/*.sql -Pattern 'exs_transaction_log','exs_message_log','exs_retry_log','exs_route'
```

### 예상 정상 결과

```text id="fdskjs"
- X-Transaction-Id 누락/오류 차단 코드 존재
- 선저장 transaction/message log 코드 존재
- 라우팅 규칙 조회 코드 존재
- 업무 호출 인터페이스 또는 Facade 구조 존재
- 실패 상태 갱신 코드 존재
- 재처리 실행 코드 존재
```

### 리포트 증적

수신/송신/재처리 흐름을 단계별로 기록한다.

```text id="eq3rzr"
1. 요청 수신
2. transaction ID 검증
3. 거래 로그 선저장
4. 전문 로그 선저장
5. 라우팅 규칙 조회
6. 업무 호출
7. 결과 상태 갱신
8. 실패 저장
9. 재처리 요청
10. 재처리 실행
11. 재처리 결과 저장
```

---

# 11. ADM 운영 기능 보강

## 요청사항

ADM은 CPF 플랫폼 운영관리자다.
다음 기능의 구현 상태를 재검수하고 부족한 부분을 보강한다.

```text id="tlwj9k"
- 플랫폼 운영자 계정
- 플랫폼 역할/권한
- 메뉴/버튼/API/다운로드 권한
- 온라인 거래 로그
- 오류 로그
- 운영 감사
- 다운로드 감사
- 배치 관리
- 알림 관리
- 동적 로그레벨
- OpenAPI/Swagger 연계
```

## 검증 방법

```powershell id="rz961a"
Select-String -Path adm/src/main/java/**/*.java -Pattern 'Controller','Service','Mapper','audit','download','batch','notification','dynamic','loglevel','permission'
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'batch','notification','download','log','permission'
Select-String -Path specs/sql/**/*.sql -Pattern 'adm_'
```

## 예상 정상 결과

```text id="grz0o3"
- 각 ADM 기능별 Controller/Service/Mapper 또는 Repository 존재
- static 화면 또는 UI 연결 존재
- 다운로드/마스킹/감사 처리 존재
- 권한 없는 기능 차단 기준 존재
```

## 리포트 증적

ADM 기능별 소스/API/UI/SQL/상태를 기록한다.

---

# 12. 공통 인증/인가 기반 보강

## 요청사항

개별 Service 안에서 Bearer token을 직접 확인하는 것만으로는 부족하다.
PFW/CMN 기반 공통 인증/인가 구조를 명확히 한다.

필수:

```text id="cmt3y7"
- Authorization: Bearer token 공통 검증 필터 또는 인터셉터
- loginRealm/domain 분리
- ADM/BIZADM/MBR token 영역 분리
- 다른 realm token으로 API 호출 시 차단
- 인증 실패 표준 오류
- 인가 실패 표준 오류
- token event history
- refresh token repository 추상화
```

## 검증 방법

```powershell id="a9cowh"
Select-String -Path **/*.java -Pattern 'OncePerRequestFilter','HandlerInterceptor','SecurityFilterChain','Authorization','Bearer','loginRealm','loginDomain','ROLE','permission'
```

## 예상 정상 결과

```text id="b1j6pi"
- 공통 인증 적용 위치가 명확함
- ADM/BIZADM/MBR 영역 분리 코드 존재
- 보호 API 차단 기준 존재
- 표준 오류 응답 사용
```

## 리포트 증적

공통 필터/인터셉터/Service 검증 방식 중 현재 구조를 명확히 기록한다.
완성되지 않았다면 `일부 구현` 또는 `미구현`으로 기록한다.

---

# 13. transactionGlobalId / 서버 인스턴스 추적 보강

## 요청사항

공식 transactionGlobalId 규격을 전 모듈에 적용한다.

```text id="zzuemw"
yyyyMMddHHmmssSSS + moduleId(3자리) + wasId(7자리) + sequence(7자리)
총 34자리
헤더명: X-Transaction-Id
```

필수 저장 필드:

```text id="nkbih4"
transactionGlobalId
moduleId
wasId
serverInstanceId
hostName
processId
threadName
```

적용 대상:

```text id="x4rd9j"
온라인 거래 로그
오류 로그
운영 감사
다운로드 감사
로그인 이력
토큰 이벤트
회원 변경 이력
대외 거래 로그
대외 송수신 로그
재처리 로그
배치 실행 로그
알림 발송 이력
```

## 검증 방법

```powershell id="nqon61"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-transaction-id-standard.ps1
Select-String -Path **/*.java,**/*.sql -Pattern 'X-Transaction-Id','transactionGlobalId','moduleId','wasId','serverInstanceId'
```

## 예상 정상 결과

```text id="knpm9b"
- X-Transaction-Id 필수 검증 위치 명확
- 누락/포맷 오류 시 표준 오류
- 주요 로그/감사/이력 테이블에 추적 필드 존재
- 주요 Service에서 추적 필드 저장
```

## 리포트 증적

모듈별 적용 여부를 표로 기록한다.

---

# 14. 공통 코드/공통 메시지 보강

## 요청사항

코드성 값과 메시지는 하드코딩을 최소화하고 CMN 공통 코드/메시지 기준을 사용한다.

대상 코드:

```text id="pe71rk"
로그인 영역
로그인 결과
토큰 이벤트 유형
사용자 상태
관리자 상태
회원 상태
역할 상태
메뉴 유형
버튼 유형
권한 유형
다운로드 유형
감사 이벤트 유형
마스킹 대상 유형
대외기관 상태
대외 채널 유형
대외 인증 방식
대외 거래 상태
송수신 방향
재처리 상태
통제 유형
```

대상 메시지:

```text id="mk9uk5"
로그인 실패
계정 잠금
비밀번호 만료
토큰 누락
토큰 오류
토큰 만료
권한 없음
transaction ID 누락
transaction ID 포맷 오류
다운로드 사유 누락
마스킹 해제 사유 누락
대외기관 없음
endpoint 중지
대외 token 만료
재처리 사유 누락
```

## 검증 방법

```powershell id="z1ce8c"
Select-String -Path cmn/src/main/java/**/*.java,**/*.yml,**/*.sql -Pattern 'LOGIN','TOKEN','MASK','DOWNLOAD','AUDIT','EXS','RETRY'
Select-String -Path **/*.java -Pattern '"권한 없음"','"토큰"','"다운로드 사유"','"마스킹"'
```

## 예상 정상 결과

```text id="w1a95f"
- 주요 코드값은 공통 코드/enum/table 기준 사용
- 주요 오류 메시지는 공통 메시지 기준 사용
- 하드코딩 메시지는 최소화
```

## 리포트 증적

공통 코드/메시지 적용 파일과 남은 하드코딩 예외를 기록한다.

---

# 15. 마스킹/다운로드/감사 공통 기준 보강

## 요청사항

ADM/BIZADM/MBR/EXS 전체에 마스킹, 다운로드, 감사 기준을 적용한다.

필수:

```text id="qts1j0"
- 목록/상세 기본 마스킹
- 마스킹 해제 권한
- 마스킹 해제 사유 필수
- 마스킹 해제 감사 로그
- 다운로드 권한
- 다운로드 사유 필수
- 기본 마스킹 다운로드
- 마스킹 해제 다운로드 별도 권한
- 다운로드 성공 감사
- 다운로드 실패 감사
```

## 검증 방법

```powershell id="f7r31p"
Select-String -Path **/*.java,**/*.sql -Pattern 'mask','masking','download','downloadReason','audit','unmask'
```

## 예상 정상 결과

```text id="yl178s"
- ADM/BIZADM/MBR/EXS 각 영역에 마스킹/다운로드/감사 처리 존재
- 사유 누락 시 표준 오류
- 성공/실패 감사 저장
```

## 리포트 증적

영역별 적용 여부를 기록한다.

---

# 16. CMN Notification Sender/Adapter 보강

## 요청사항

알림 발송은 CMN 공통 Sender/Adapter 구조로 제공한다.

필수:

```text id="p5gnk8"
- NotificationSender 또는 MessageSender 인터페이스
- EMAIL/SMS/KAKAO/PUSH/WEBHOOK 채널 추상화
- Provider/Adapter 구조
- Mock Sender
- 템플릿 + 변수 치환
- 발송 요청 이력
- 발송 성공/실패 이력
- 재시도 가능 여부
- 실패 사유 저장
- 업체 교체 시 업무 코드 변경 최소화
```

## 검증 방법

```powershell id="p6dc21"
Select-String -Path cmn/src/main/java/**/*.java,adm/src/main/java/**/*.java,bizadm/src/main/java/**/*.java,mbr/src/main/java/**/*.java,exs/src/main/java/**/*.java -Pattern 'NotificationSender','MessageSender','Mock','EMAIL','SMS','KAKAO','PUSH','WEBHOOK','template'
```

## 예상 정상 결과

```text id="sjkynu"
- CMN 공통 Sender/Adapter 존재
- ADM 알림은 CMN Sender 사용
- Mock Sender로 서버 미설정 시 동작 가능
- 발송 이력 저장 구조 존재
```

## 리포트 증적

채널별 구현 상태와 Mock/fallback 동작 여부를 기록한다.

---

# 17. MFA/TOTP 확장 기반 보강

## 요청사항

ADM/BIZADM/MBR별 MFA 정책 분리가 가능해야 한다.

필수:

```text id="n6p3n7"
- MFA 정책: 필수/선택/비활성
- ID/PW 1차 인증 후 MFA challenge 상태
- MFA 검증 후 token 발급
- TOTP secret 저장
- 복구 코드 구조
- MFA 실패 이력
- MFA 초기화/재등록 감사
- Mock MFA provider
```

## 검증 방법

```powershell id="uoex38"
Select-String -Path **/*.java,**/*.sql -Pattern 'MFA','TOTP','OTP','recovery','challenge','mfa'
```

## 예상 정상 결과

```text id="yffo1c"
- MFA 정책 구조 존재
- TOTP/복구코드 미완이면 일부 구현으로 기록
- Mock MFA 검증 가능 구조 존재
```

## 리포트 증적

ADM/BIZADM/MBR별 MFA 상태를 기록한다.

---

# 18. Redis/Kafka/MQ mock/fallback 보강

## 요청사항

실제 Redis/Kafka/MQ 서버가 없어도 앱이 정상 기동해야 한다.

필수:

```text id="srt1qs"
- disabled/mock profile
- in-memory/mock adapter
- fallback 처리
- 서버 미설정 시 앱 정상 기동
- 실제 서버 연결 설정 분리
- 알림/동적 로그레벨/캐시 전파 mock 검증
```

## 검증 방법

```powershell id="rxcl9q"
Select-String -Path cmn/src/main/java/**/*.java,pfw/src/main/java/**/*.java,adm/src/main/java/**/*.java,**/*.yml -Pattern 'Redis','Kafka','MQ','Mock','Fallback','disabled','profile'
```

## 예상 정상 결과

```text id="yuf771"
- 실서버 설정 없이 mock/fallback 가능
- 실제 서버 설정과 disabled 설정 분리
- 환경 부재로 실연동 제외 시 미검증으로 기록
```

## 리포트 증적

Redis/Kafka/MQ별 mock/fallback 상태를 기록한다.

---

# 19. BIZADM/MBR/EXS UI/route/API wrapper 보강

## 요청사항

API만 있으면 완료가 아니다.
최소한 운영자가 확인할 수 있는 UI route/view/API wrapper를 제공한다.

필수 대상:

```text id="f0o8z5"
BIZADM:
- 로그인
- 현재 사용자
- 로그인 이력
- 운영자
- 역할
- 메뉴/권한
- 다운로드/마스킹/감사

MBR:
- 회원 목록
- 회원 상세
- 로그인 이력
- 상태 변경
- 마스킹/다운로드/감사

EXS:
- 기관/채널/endpoint
- token 목록/refresh/event
- retry 목록/요청/실행 결과
- control policy
- transaction/message log
```

## 검증 방법

```powershell id="g4knqe"
Get-ChildItem -Recurse adm/src/main/resources/static -Include *.js,*.html,*.vue
Select-String -Path adm/src/main/resources/static/**/*.* -Pattern 'bizadm','mbr','exs','token','retry','download','mask','audit'
```

Node가 있으면:

```powershell id="ou32gl"
node --check adm/src/main/resources/static/adm/adm.js
```

## 예상 정상 결과

```text id="y60wgn"
- BIZADM/MBR/EXS 관련 route 또는 메뉴 존재
- API wrapper 또는 fetch 호출 존재
- 권한 없는 버튼 숨김/비활성 기준 존재
- 브라우저 클릭 미수행 시 UI는 미검증으로 기록
```

## 리포트 증적

화면별 route/view/API wrapper/브라우저 검증 여부를 기록한다.

---

# 20. EDU 개발자 참조 구현 보강

## 요청사항

`xyz/edu`는 신규 업무 개발자가 따라 할 수 있는 참조 구현이어야 한다.

필수 EDU 예시:

```text id="urpnx8"
- transactionGlobalId 사용
- 표준 헤더
- 표준 응답/오류
- Authorization Bearer token 사용
- ADM/BIZADM/MBR realm 분리 예시
- 권한 체크
- 공통 코드 조회
- 공통 메시지 사용
- 마스킹 적용
- 다운로드 사유/감사
- 운영 감사
- NotificationSender 사용
- MFA mock challenge
- 외부 API 호출 시 transactionGlobalId 전파
- Redis/Kafka/MQ mock/fallback
- EXS 수신 선저장
- EXS token refresh mock
- EXS retry 요청
```

## 검증 방법

```powershell id="gcrwvu"
Get-ChildItem -Recurse xyz/src/main/java/cpf/xyz/edu -Filter *.java
Select-String -Path xyz/src/main/java/cpf/xyz/edu/**/*.java -Pattern 'BIZADM','MBR','EXS','Bearer','token','retry','MFA','Notification','Masking','Download','Transaction'
```

## 예상 정상 결과

```text id="if6nng"
- 단순 Hello API가 아니라 실제 개발 참조 예시 존재
- 각 예시에 한글 주석 존재
- 개발 가이드와 기능 매트릭스에 EDU 소스 경로 연결
```

## 리포트 증적

EDU 예시별 소스 경로와 참조 목적을 기록한다.

---

# 21. SQL/Flyway/Mapper/Repository 정합성 검증

## 요청사항

Java 코드, Mapper/Repository, SQL/Flyway, 설치 SQL이 서로 맞아야 한다.

## 검증 방법

```powershell id="frk85b"
Get-ChildItem -Recurse specs/sql -Include *.sql
Get-ChildItem -Recurse */src/main/resources -Include *.xml,*.sql,*.yml
Select-String -Path **/*.java -Pattern 'select.*By','insert','update','delete','JdbcTemplate','Mapper'
Select-String -Path specs/sql/**/*.sql -Pattern 'bizadm_','mbr_','exs_','adm_','pfw_','cmn_'
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
```

MariaDB 가능 시:

```powershell id="pk9fa3"
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

## 예상 정상 결과

```text id="vlssn4"
- Java에서 사용하는 테이블이 SQL/Flyway에 존재
- Mapper method와 XML/SQL 일치
- 합본 설치 SQL에 최신 테이블 반영
- MariaDB 실실행 못 하면 미검증으로 기록
```

## 리포트 증적

모듈별 Java ↔ SQL 정합성 검토 결과를 기록한다.

---

# 22. OpenAPI/Swagger 정합성 보강

## 요청사항

API를 추가했으면 OpenAPI/Swagger에 반영되어야 한다.

필수 확인 대상:

```text id="e5265c"
- /v3/api-docs
- /swagger-ui/index.html
- /swagger-ui.html
- ADM-Batch
- ADM-Notification
- ADM-Log
- ADM-Dynamic-Log-Level
- ADM-Download
- BIZADM Auth/Operation
- MBR Auth/Member
- EXS Operation/Flow
- EDU API tag
- 표준 헤더 설명
- 표준 응답 설명
```

## 검증 방법

앱 기동 가능 시:

```powershell id="gdbkv9"
curl http://localhost:8080/v3/api-docs
curl http://localhost:8080/swagger-ui/index.html
curl http://localhost:8080/swagger-ui.html
```

정적 확인:

```powershell id="sizipu"
Select-String -Path **/*.java -Pattern '@Operation','@Tag','@Parameter','X-Transaction-Id','ApiResponse'
```

## 예상 정상 결과

```text id="br6qqk"
- 주요 API에 Swagger annotation 존재
- X-Transaction-Id 설명 존재
- 표준 응답/오류 설명 존재
- 앱 미기동이면 HTTP smoke는 미검증으로 기록
```

## 리포트 증적

정적 annotation 확인과 HTTP 확인을 분리 기록한다.

---

# 23. 기능 구현 매트릭스 실제 검수 문서화

## 요청사항

`specs/기능_구현_매트릭스.html`은 실제 HTML table 기반 검수 문서여야 한다.

필수 컬럼:

```text id="pbrnxu"
대분류
기능명
구현 상태
주요 소스
API
화면
DB/저장 구조
Swagger Tag
EDU 참조
검증 방법
예상 정상 결과
실제 검증 결과
미검증 사유
비고/다음 보완
```

## 검증 방법

```powershell id="wjlbxg"
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
Select-String -Path specs/기능_구현_매트릭스.html -Pattern '<table>','구현 상태','검증 방법','예상 정상 결과','실제 검증 결과','미검증 사유'
```

## 예상 정상 결과

```text id="s4ub2k"
- 실제 HTML table 사용
- 상태값 과장 없음
- in-memory/UI 없음/DB 미검증 항목은 완료 아님
- 각 항목에 검증 방법과 실제 결과 존재
```

## 리포트 증적

기능 매트릭스 변경 요약과 주요 상태 변경을 기록한다.

---

# 24. 최종 quality gate 및 개별 검증

## 요청사항

가능한 모든 검증 명령을 실행하고 결과를 리포트에 남긴다.
실행하지 못한 명령은 성공으로 기록하지 않는다.

## 실행 명령

```powershell id="mc0tqu"
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

선택 실행:

```powershell id="rpfa8x"
node --check adm/src/main/resources/static/adm/adm.js
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
curl http://localhost:8080/v3/api-docs
```

## 예상 정상 결과

```text id="ufzr73"
- compileJava BUILD SUCCESSFUL
- test BUILD SUCCESSFUL
- qualityGate BUILD SUCCESSFUL
- check-html-docs 성공
- check-java-format 성공
- check-legacy-name 성공
- check-security-seed-standard 성공
- check-sql-standard 성공
- check-transaction-id-standard 성공
```

## 리포트 증적

명령별로 아래를 기록한다.

```text id="jzqlsj"
명령어
실행 여부
예상 정상 결과
실제 결과
성공/실패/미검증
실패 로그 요약
미검증 사유
```

---

# 25. Codex 최종 보고 형식

작업 완료 후 대화창에는 아래 형식으로만 보고한다.

```text id="cjnp1h"
[소스 기준]
- branch:
- local HEAD:
- origin/master HEAD:
- git status 요약:

[완료]
- 실제 구현 및 검증 완료한 항목

[일부 구현]
- 구조는 있으나 미완성인 항목

[미구현]
- 구현하지 못한 항목

[실패]
- 실행했으나 실패한 검증

[미검증]
- 실행하지 못한 검증과 사유

[재확인 필요]
- 설계 선택이 필요하거나 기존 구조와 충돌한 항목

[검증 명령 결과]
- compileJava:
- test:
- qualityGate:
- check-html-docs:
- check-java-format:
- check-legacy-name:
- check-security-seed-standard:
- check-sql-standard:
- check-transaction-id-standard:
- node check:
- MariaDB smoke:
- OpenAPI smoke:

[리포트]
- CPF_STABILIZATION_REPORT.html에 요청사항별 증적 기록 완료 여부
- 별도 리포트/변경파일 생성 여부
```

---

# 26. ChatGPT 재검수 가능 기준

작업이 끝난 후 ChatGPT가 GitHub 소스 기준으로 다음을 한 단씩 대조할 수 있어야 한다.

```text id="u60eq5"
1. 요청사항별 소스 경로 확인 가능
2. 요청사항별 SQL/Flyway 확인 가능
3. 요청사항별 UI/static 경로 확인 가능
4. 요청사항별 Swagger annotation 확인 가능
5. 요청사항별 EDU 참조 경로 확인 가능
6. 요청사항별 검증 명령과 결과 확인 가능
7. 미검증 사유 확인 가능
8. 완료/일부 구현/미구현/미검증/실패 판단 가능
```

리포트에 소스 경로와 검증 증적이 없으면 완료로 인정하지 않는다.

---

# 27. 최우선 처리 순서

이번 작업은 아래 순서로 처리한다.

```text id="wpi1ze"
1. GitHub/로컬 기준 불일치 확인
2. 루트 불필요 산출물 제거
3. HTML 문서 실제 HTML화
4. Java 물리 포맷 전면 보정
5. check-html-docs/check-java-format 신뢰성 회복
6. BIZADM/MBR/EXS sample/in-memory/hardcoding 제거
7. BIZADM/MBR/EXS DB/SQL/Repository 정합성 보강
8. BIZADM/MBR/EXS UI/route/API wrapper 보강
9. EXS 라우팅/실패 갱신/재처리 실행 흐름 보강
10. 공통 인증/인가/transactionGlobalId/감사/마스킹/다운로드 보강
11. EDU 참조 구현 보강
12. 기능 매트릭스 실제 HTML 검수 문서화
13. 전체 quality gate와 개별 검증 실행
14. CPF_STABILIZATION_REPORT.html에 요청사항별 증적 기록
```

---

# 28. 완료 인정 기준

이번 작업은 아래 조건을 만족해야 완료로 인정한다.

```text id="whk3vu"
- CPF_STABILIZATION_REPORT.html이 실제 HTML
- specs/*.html이 실제 HTML
- CPF_STABILIZATION_CHANGED_FILES.txt 없음
- CPF_STABILIZATION_REPORT.md 없음
- Java raw 기준 장문 한 줄 파일 없음
- check-html-docs 성공
- check-java-format 성공
- BIZADM/MBR/EXS 기본 구현체 주요 코드가 sample에 없음
- in-memory 저장소가 기본 구현체에 없음
- DB Repository/Mapper/SQL/Flyway 정합성 확인
- BIZADM/MBR/EXS 주요 UI route/API wrapper 존재
- EXS 선저장 이후 라우팅/실패 갱신/재처리 실행 흐름 존재
- EDU 참조 구현 보강
- 기능 구현 매트릭스가 실제 HTML table이며 과장 없음
- CPF_STABILIZATION_REPORT.html에 요청사항별 증적 있음
- 미검증 항목은 성공으로 기록하지 않음
```
