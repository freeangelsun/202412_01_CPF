# CPF 품질 보강 요청서 - GitHub 검수 결과 기반

## 0. 작업 목표

이번 작업은 Codex 보고와 GitHub master 실제 상태의 불일치를 바로잡고, CPF 공통 표준이 무너지지 않도록 BIZADM/MBR/EXS/EDU/문서/품질 gate를 보강하는 것이다.

현재 GitHub master 기준으로 다음 문제가 확인되었다.

```text
1. Codex 보고와 GitHub master 상태가 불일치함
2. CPF_STABILIZATION_CHANGED_FILES.txt가 GitHub master에 보임
3. CPF_STABILIZATION_REPORT.md가 GitHub master에 보임
4. CPF_STABILIZATION_REPORT.html이 실제 HTML이 아니라 Markdown 형식임
5. specs/index.html이 실제 HTML이 아니라 Markdown 형식임
6. specs/기능_구현_매트릭스.html이 실제 HTML이 아니라 Markdown 형식임
7. BIZADM/EXS 기본 구현체가 sample 패키지 중심임
8. BIZADM/EXS 주요 Service가 하드코딩 또는 in-memory 중심임
9. BIZADM/MBR refresh token 저장소가 DB 영속화되지 않음
10. EXS token/retry/control 운영 API가 DB 영속화되지 않음
11. EXS 대외 수신 로그 선저장이 실제 DB 트랜잭션으로 구현되지 않음
12. BIZADM/EXS Vue route/view/API wrapper가 부족함
13. EDU 참조 구현이 신규 핵심 기능과 충분히 연결되어 있는지 부족함
14. Java raw 기준 물리 포맷이 장문 한 줄처럼 보이는 파일이 있음
15. FPS/Fps/fps 레거시 명칭 제거가 제외 파일까지 포함한 전체 기준으로 신뢰되지 않음
```

Git commit, push, branch 생성은 하지 않는다.

---

## 1. 먼저 현재 GitHub 반영 상태를 확인할 것

작업 전 다음을 먼저 확인한다.

```text
- 현재 브랜치
- 현재 commit hash
- GitHub master와 로컬 작업본이 같은지
- Codex가 보고한 파일이 실제 GitHub master에 존재하는지
- CPF_STABILIZATION_CHANGED_FILES.txt 존재 여부
- CPF_STABILIZATION_REPORT.md 존재 여부
- CPF_STABILIZATION_REPORT.html 내용이 실제 HTML인지
```

리포트에는 다음 형식으로 기록한다.

```text
[소스 기준]
- 로컬 브랜치:
- 로컬 commit:
- GitHub 기준 브랜치:
- GitHub 기준 commit:
- 로컬/GitHub 일치 여부:
- 불일치 시 원인 추정:
```

---

## 2. 리포트/문서 파일 정리

다음 기준을 반드시 적용한다.

```text
- CPF_STABILIZATION_CHANGED_FILES.txt는 삭제 상태여야 함
- CPF_STABILIZATION_REPORT.md는 삭제 상태여야 함
- 최종 리포트는 CPF_STABILIZATION_REPORT.html 하나만 유지
- CPF_STABILIZATION_REPORT.html은 실제 HTML이어야 함
- specs/*.html 문서는 실제 HTML이어야 함
```

실제 HTML 필수 구조:

```html
<!doctype html>
<html lang="ko">
<head>
</head>
<body>
<main>
<section>
<table>
</table>
</section>
</main>
</body>
</html>
```

금지:

```text
- .html 확장자에 Markdown 내용 작성
- # 제목으로 시작
- Markdown 표
- Markdown 링크 중심 문서
```

`check-html-docs.ps1`는 루트 `CPF_STABILIZATION_REPORT.html`까지 검사해야 한다.

---

## 3. Java 물리 포맷 검사 강화

GitHub raw 기준으로 Java 파일이 1~10줄 장문 형태로 보이지 않도록 검사한다.

검사 기준:

```text
- package와 import가 한 줄에 붙으면 실패
- 여러 import가 한 줄에 붙으면 실패
- class 선언과 method 선언이 한 줄에 붙으면 실패
- Java 파일 전체 줄 수가 비정상적으로 적으면 실패 후보
- 한 줄 길이가 과도하게 긴 경우 실패 후보
```

대상:

```text
- bizadm/**/*.java
- mbr/**/*.java
- exs/**/*.java
- xyz/**/*.java
- adm/**/*.java
- pfw/**/*.java
- cmn/**/*.java
```

`check-java-format.ps1`를 강화하고, 리포트에 장문 한 줄 파일 여부를 기록한다.

---

## 4. 패키지 구조 보강

`bizadm`, `mbr`, `exs`는 CPF 기본 구현체다. 주요 구현 코드를 단순히 `sample` 패키지 아래에 몰아넣지 않는다.

단, `sample`, `demo`, `example` 패키지 자체를 전면 금지하는 것은 아니다. 실제 교육/예제/데모 코드에는 허용한다.

요건:

```text
- 기본 구현체는 업무/기능 그룹 기준 패키지로 분리
- sample 패키지는 교육/예제/데모 용도로만 사용
- 기본 구현체와 샘플 참조 코드를 분리
```

권장 기능 그룹:

```text
BIZADM:
- auth
- operator 또는 adminuser
- role
- menu
- permission
- download
- masking
- audit
- customer
- product
- order
- setting

MBR:
- auth
- member
- login
- history
- masking
- download
- audit

EXS:
- institution
- channel
- endpoint
- authprofile
- token
- route
- transaction
- message
- control
- retry
```

기존 구조와 충돌하면 임의로 대규모 이동하지 말고 `재확인 필요`로 보고한다.

---

## 5. BIZADM 기본 구현체 보강

현재 BIZADM은 sample 패키지와 in-memory/hash 샘플 중심이다. 기본 구현체 수준으로 보강한다.

필수 보강:

```text
- BIZADM auth 기능을 기능 그룹 패키지로 분리
- BIZADM operator/admin user 저장소 DB 영속화
- BIZADM refresh token hash DB 영속화
- BIZADM login history DB 영속화
- BIZADM role/menu/button/API/download 권한 DB 연동
- BIZADM 고객/상품/주문/설정 샘플을 실제 Mapper 기반으로 전환
- 하드코딩 List.of(Map.of(...)) 제거 또는 EDU 샘플로 분리
- 권한 없는 API 차단
- 권한 없는 메뉴/버튼 숨김 또는 비활성 처리
- 다운로드 사유 필수
- 마스킹 해제 사유 필수
- 감사 로그 저장
- Vue route/view/api wrapper 구현
```

완료로 인정하지 않는 경우:

```text
- sample 패키지에 기본 구현체가 남아 있음
- ConcurrentHashMap 기반 인증 저장소
- List.of(Map.of(...)) 하드코딩 업무 데이터
- DB Mapper 없이 완료 처리
- Vue 화면 없이 완료 처리
```

---

## 6. MBR 기본 구현체 보강

현재 MBR Auth는 in-memory token/member/loginHistory 중심이다. DB 영속화 기준으로 보강한다.

필수 보강:

```text
- MBR 회원 인증 저장소 DB 연동
- 회원 비밀번호 hash DB 저장
- MBR refresh token hash DB 영속화
- MBR login history DB 영속화
- 로그인 실패 횟수 DB 반영
- 일정 횟수 실패 시 잠금 처리
- 회원 상태별 로그인 제한 정책 명확화
- 로그아웃 시 refresh token 폐기 DB 반영
- token refresh 이력 저장
- 회원 마스킹/다운로드/감사 연결
- 회원 관리 화면 또는 관리자 조회 화면 보강
```

완료로 인정하지 않는 경우:

```text
- ConcurrentHashMap 기반 회원/토큰 저장소
- 로그인 이력 in-memory
- refresh token DB 영속화 없음
- 잠금/상태 정책이 응답용 샘플 수준
```

---

## 7. EXS 기본 구현체 보강

현재 EXS는 sample 패키지, 하드코딩 조회, in-memory token/retry/control 중심이다. 대외연계 기본 구현체 수준으로 보강한다.

필수 보강:

```text
- EXS 기능 그룹 패키지 분리
- 대외기관 DB 연동
- 채널 DB 연동
- endpoint DB 연동
- auth profile DB 연동
- token store DB 연동
- token event history DB 연동
- route rule DB 연동
- transaction log DB 연동
- message log DB 연동
- control policy DB 연동
- retry log DB 연동
- token 원문 저장 금지
- token/secret 화면 원문 노출 금지
- token refresh mock 결과 DB 저장
- retry 요청/결과 DB 저장
- 대외 수신 시 로그 선저장 DB 트랜잭션 구현
- 업무 모듈 호출 실패 시에도 수신 로그와 실패 원인 저장
```

`TransactionContext.getOrCreateTransactionId()`를 대외 수신 표준 API에서 조용히 사용하지 않는다. 대외 수신 API는 `X-Transaction-Id`를 검증하고, 누락/포맷 오류 시 표준 오류를 반환해야 한다. 내부 mock/EDU 샘플에서만 예외 생성이 가능하다.

---

## 8. 공통 인증/토큰 프레임워크 보강

CPF 공통 기반에 로그인 후 토큰으로 보호 API를 사용하는 흐름을 명확히 녹인다.

필수 요건:

```text
- Access Token 발급
- Refresh Token 발급
- Refresh Token hash 저장
- Refresh Token 폐기
- Token refresh
- Token event 이력
- Authorization: Bearer 검증 공통 필터 또는 인터셉터
- ADM/BIZADM/MBR login realm 분리
- 다른 realm token으로 API 호출 시 차단
- 보호 API token 누락 시 표준 오류
- transactionGlobalId와 token 역할 분리
```

BIZADM/MBR 개별 Service 내부에만 Bearer 검증이 있으면 부족하다. 보호 API 공통 필터/인터셉터 또는 공통 권한 적용 구조를 제시하고 구현한다.

---

## 9. MFA 확장 기반 보강

MFA/TOTP는 ADM에는 선택일 수 있고, BIZADM에는 프로젝트 정책상 필요할 수 있다.

필수 요건:

```text
- ADM/BIZADM/MBR별 MFA 정책 분리
- BIZADM MFA 필수/선택/비활성 설정 가능
- ID/PW 성공 후 MFA challenge 상태 지원
- MFA 검증 완료 후 token 발급
- TOTP 등록/검증 구조
- 복구코드 구조
- MFA 실패 이력
- MFA 초기화/재등록 감사
- Mock MFA provider
```

실제 앱 QR/복구코드가 구현되지 않으면 `완료`로 쓰지 말고 `일부 구현` 또는 `미검증`으로 기록한다.

---

## 10. CMN 알림 발송 공통 Sender/Adapter 보강

알림 발송은 ADM에만 갇히지 않고 CMN 업무 공통 기능으로 제공되어야 한다. 발송 업체가 바뀌어도 업무 코드 변경이 최소화되어야 한다.

필수 요건:

```text
- NotificationSender 또는 MessageSender 공통 인터페이스
- EMAIL/SMS/KAKAO/PUSH/WEBHOOK 채널 추상화
- Provider/Adapter 구조
- Mock Sender 제공
- 템플릿 + 변수 치환 구조
- 발송 요청 이력
- 발송 성공/실패 이력
- 재시도 가능 여부
- 실패 사유 저장
- 실서버 미설정 시 mock/fallback 동작
```

ADM 알림은 이 CMN 공통 Sender 기반을 사용한다. BIZADM/MBR/EXS도 필요 시 동일 기반을 사용할 수 있게 한다.

---

## 11. EDU 참조 구현 보강

모든 주요 공통 기능은 `xyz/edu`에 개발자 참조 구현을 제공한다.

필수 EDU 대상:

```text
- transactionGlobalId 사용
- 표준 헤더 사용
- 표준 응답/오류 사용
- 로그인 후 Authorization Bearer token 사용
- ADM/BIZADM/MBR realm 분리 예시
- 권한 체크
- 공통 코드 조회
- 공통 메시지 사용
- 마스킹 적용
- 다운로드 사유/감사
- 운영 감사
- 알림 Sender 사용
- MFA challenge/mock 검증 흐름
- 외부 API 호출 시 transactionGlobalId 전파
- Redis/Kafka/MQ mock/fallback
- EXS 대외 수신 선저장
- EXS token refresh mock
- EXS retry 요청
```

EDU는 단순 Hello API가 아니라 개발자가 신규 업무 기능을 만들 때 따라 할 수 있는 수준이어야 한다.

각 EDU 예시는 한글 주석을 충분히 작성하고, 개발 가이드와 기능 구현 매트릭스에 소스 경로를 연결한다.

---

## 12. Java 한글 주석 전체 기준

모든 신규/수정 Java 소스에 한글 주석을 작성한다.

대상:

```text
- 클래스
- public method
- Controller API method
- Service 업무 method
- DTO 주요 필드
- Mapper 주요 쿼리 목적
- 인증/토큰 처리
- 권한 체크
- 마스킹 처리
- 다운로드 감사 처리
- 운영 감사 처리
- transactionGlobalId 처리
- 알림 발송 처리
- MFA 처리
- 대외 수신/송신/재처리 처리
- 배치 실행/실패 처리
```

주석은 “조회한다/저장한다” 같은 단순 반복이 아니라, 왜 필요한지, 어떤 표준을 따르는지, 어떤 이력/감사를 남기는지 설명해야 한다.

리포트에는 신규/수정 Java 파일별 주석 반영 여부를 기록한다.

---

## 13. FPS/Fps/fps 레거시 명칭 전수 제거

기존 프로젝트명 FPS 계열은 CPF 기준 레거시다.

검사 대상:

```text
- 파일 경로
- Java package
- Java class
- method
- field
- comment
- YAML/properties
- Gradle group/name/description
- SQL
- Flyway
- Vue
- README
- specs 문서
- Swagger tag/description
- 리포트
- 스크립트
- 테스트 코드
```

`check-legacy-name.ps1`의 제외 목록을 재검토한다. 리포트와 변경 파일 목록까지 무조건 제외하면 안 된다.

허용 가능한 예외는 “과거 명칭 설명”에 한정하고, 그 경우도 리포트에 명시한다.

---

## 14. 기능 구현 매트릭스 보강

`specs/기능_구현_매트릭스.html`은 실제 HTML이어야 하며, 기능별 상태를 과장 없이 기록한다.

필수 상태 값:

```text
완료
일부 구현
미구현
미검증
실패
```

다음은 `완료` 금지:

```text
- in-memory 저장소
- 하드코딩 데이터
- sample 패키지 중심
- Vue 화면 없음
- DB Mapper 없음
- OpenAPI HTTP 미검증
- 브라우저 클릭 미검증
- 실서버 미검증인데 실연동 성공처럼 표현
```

---

## 15. 검증 및 리포트 기준

Codex는 다음을 구분해서 기록한다.

```text
[성공]
실제로 실행해서 성공한 항목

[실패]
실제로 실행했으나 실패한 항목

[미검증]
실행하지 못했거나 환경이 없어 확인하지 못한 항목

[정적 확인]
파일/소스/문서만 확인한 항목

[일부 구현]
구조는 있으나 DB/UI/검증/운영 수준이 부족한 항목

[요건 미달]
CPF 공통 표준을 만족하지 못한 항목

[재확인 필요]
구조 충돌 또는 설계 선택이 필요한 항목
```

실제 실행하지 않은 검증은 성공으로 기록하지 않는다.

---

## 16. 최종 완료 기준

이번 보완 작업 완료 후보는 다음을 만족해야 한다.

```text
- GitHub master와 로컬 작업본 일치 확인
- 리포트/문서 실제 HTML화
- 불필요한 changed files txt/report md 제거
- BIZADM/MBR/EXS sample 중심 구조 정리
- 인증/토큰 DB 영속화
- EXS 운영 API DB 영속화
- BIZADM/EXS Vue route/view/API wrapper 보강
- EDU 참조 구현 보강
- Java 주석 전체 기준 반영
- FPS 레거시 전수 검사 강화
- qualityGate에 security seed, html report, java raw format 검사 포함
- 기능 구현 매트릭스 과장 제거
```
