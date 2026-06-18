# CPF 3차 완성도 보강 요청서

## 문서 체계 단순화, HTML 가이드 정본화, Swagger/ADM 운영 검증

현재 열려 있는 로컬 프로젝트 소스 기준으로 작업하세요.

이번 작업은 신규 기능을 무리하게 늘리는 작업이 아닙니다.
CPF 프레임워크의 문서 체계, 소스 포맷, Swagger, ADM 운영 검증, 남은 미검증 항목을 정리하여 프레임워크 완성도를 높이는 작업입니다.

이전 작업에서 qualityGate, MariaDB smoke, EDU Batch 실실행, ADM API smoke가 성공한 것으로 보고되었지만, 문서가 여전히 복잡하고 specs 폴더에 불필요한 md 파일이 남아 있으며, 일부 기능은 브라우저 기준 검증이 부족합니다.

---

# 1. 이번 작업 핵심 목표

```text
1. README.md를 제외한 specs 문서는 HTML 중심으로 정리
2. specs 루트의 불필요한 md 문서를 정본 HTML 가이드에 통합 후 삭제
3. 기능 구현 매트릭스를 md가 아니라 HTML 문서로 전환
4. specs/index.html을 브라우저에서 바로 보는 문서 포털로 정리
5. 문서 수를 줄이고, 중복/파편 문서를 제거
6. 핵심 Java/HTML/SQL/PowerShell/Gradle 포맷 정상화
7. /swagger-ui.html HTTP 500 수정
8. /v3/api-docs, /swagger-ui/index.html, /swagger-ui.html 실제 기동 검증
9. ADM 브라우저 클릭 검증 수행
10. 알림/다운로드/동적 로그레벨 운영 기능 보강
11. Batch scheduler 자동 수행 시나리오 검증
12. Redis/Kafka 실 broker 연동 가능 여부 확인 또는 미검증 사유 명확화
13. 결과 산출물은 리포트 1개만 생성
```

---

# 2. 산출물 기준 변경

이번 작업부터 결과 산출물은 아래 하나만 새로 작성 또는 갱신하세요.

```text
CPF_STABILIZATION_REPORT.html
```

아래 파일은 새로 만들지 마세요.

```text
CPF_STABILIZATION_CHANGED_FILES.txt
```

기존에 `CPF_STABILIZATION_CHANGED_FILES.txt`가 남아 있다면 다음 중 하나로 처리하세요.

```text
1. 삭제 가능하면 삭제
2. 삭제가 어렵다면 더 이상 정본 산출물로 사용하지 않음
3. README.md 또는 specs/index.html에서 링크하지 않음
4. 리포트 안에 변경 요약 섹션으로 통합
```

`CPF_STABILIZATION_REPORT.html`에는 아래 내용을 포함하세요.

```text
1. 작업 요약
2. 완료 항목
3. 일부 구현 항목
4. 실패 항목
5. 미검증 항목
6. 실제 실행 명령과 결과
7. Swagger 검증 결과
8. ADM 브라우저 검증 결과
9. 문서 정리 결과
10. 삭제/통합한 md 문서 목록
11. 남은 리스크
12. 다음 작업 후보
```

---

# 3. 문서 체계 단순화 원칙

문서가 너무 많아지면 프레임워크 검수와 유지보수가 어려워집니다.
이번 작업에서는 문서 수를 줄이고, 브라우저에서 바로 볼 수 있는 HTML 가이드 중심으로 정리하세요.

## 3.1 유지할 정본 문서

아래 문서를 정본으로 유지하세요.

```text
README.md
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

`README.md`는 루트 진입점으로만 유지합니다.
상세 가이드는 specs 하위 HTML 문서에서 관리합니다.

## 3.2 md 문서 정리 기준

README.md 외에는 가급적 md 문서를 유지하지 마세요.

현재 specs에 남아 있는 아래 md 문서들은 정본 HTML 가이드에 통합한 뒤 삭제하세요.

```text
specs/transaction-header-standard.md
specs/ci-quality-gate.md
specs/definition-of-done.md
specs/module-template.md
specs/operation-security-guide.md
specs/기능_구현_매트릭스.md
specs/sql/README.md
```

각 문서의 내용은 아래 기준으로 통합하세요.

```text
transaction-header-standard.md
→ specs/프레임워크_구성_가이드.html
→ specs/개발_가이드.html
→ specs/기능_구현_매트릭스.html

ci-quality-gate.md
→ specs/프레임워크_구성_가이드.html
→ specs/개발_가이드.html

definition-of-done.md
→ specs/개발_가이드.html
→ specs/기능_구현_매트릭스.html

module-template.md
→ specs/개발_가이드.html

operation-security-guide.md
→ specs/프레임워크_구성_가이드.html
→ specs/관리자_가이드.html

specs/sql/README.md
→ specs/SQL_가이드.html

specs/기능_구현_매트릭스.md
→ specs/기능_구현_매트릭스.html
```

## 3.3 삭제가 곤란한 경우

정말 삭제가 곤란한 md 문서가 있으면 specs 루트에 두지 말고 아래로 이동하세요.

```text
specs/deprecated/
```

단, deprecated 문서는 정본 문서가 아니며, `specs/index.html`에서는 “보관 문서”로만 표시하세요.
가능하면 deprecated 문서도 HTML로 변환하거나, 정본 가이드에 완전히 통합한 뒤 제거하세요.

---

# 4. specs/index.html 정리

`specs/index.html`은 브라우저에서 바로 볼 수 있는 문서 포털이어야 합니다.

## 4.1 index 구성

`specs/index.html`에는 아래 링크만 중심으로 남기세요.

```text
1. 프레임워크 구성 가이드
2. 개발 가이드
3. 관리자 가이드
4. SQL 가이드
5. 기능 구현 매트릭스
6. 최신 안정화 리포트
```

## 4.2 index에서 제거할 것

```text
1. 불필요한 md 문서 직접 링크
2. 중복 문서 링크
3. deprecated 문서를 정본처럼 보이게 하는 링크
4. 영어명 guide 문서 링크
5. 실제 없는 문서 링크
```

---

# 5. 기능 구현 매트릭스 HTML 전환

`specs/기능_구현_매트릭스.md`는 더 이상 정본으로 쓰지 말고, 아래 파일로 전환하세요.

```text
specs/기능_구현_매트릭스.html
```

## 5.1 필수 컬럼

HTML table 형태로 아래 컬럼을 포함하세요.

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
비고
```

## 5.2 필수 기능군

아래 기능군은 반드시 포함하세요.

```text
1. 표준 헤더/거래 ID
2. 표준 응답/오류
3. 공통 코드
4. 공통 메시지
5. 트랜잭션 처리
6. 트랜잭션 분리
7. 감사 로그
8. 온라인 거래 로그
9. 오류 로그
10. 외부 연동
11. 주제영역 간 호출
12. 파일 업로드/다운로드
13. Excel/CSV/로그 다운로드
14. 다운로드 권한/마스킹/감사 로그
15. 알림/노티
16. 메시징/비동기
17. 보안/JWT/OAuth
18. 관리자 권한
19. 런타임 로그 레벨 변경
20. 배치 Job
21. 배치 스케줄
22. 배치 자동 수행/반복 수행
23. 배치 영업일/수행 가능 시간
24. 배치 선행/후행/Trigger 관계
25. 배치 수행 대상 인스턴스
26. 배치 Execution 목록/상세
27. 배치 Step 상세
28. 배치 처리 건수/수량/결과 통계
29. 배치 실패/지연 알림
30. EDU 샘플
31. Swagger/OpenAPI
```

## 5.3 구현 상태 표기

구현 상태는 과장하지 말고 아래 중 하나로 표시하세요.

```text
완료
일부 구현
미구현
미검증
실패
```

---

# 6. 한글 가이드 문서 보강

정본 한글 가이드 문서는 실제 기능 검수 기준으로 사용할 수 있어야 합니다.

## 6.1 프레임워크 구성 가이드

`specs/프레임워크_구성_가이드.html`에는 아래 내용을 포함하세요.

```text
1. PFW / CMN / ADM / 주제영역 책임
2. DB 소유권
3. 표준 헤더/거래 ID
4. 표준 응답/오류
5. 로그/감사/추적
6. 배치 프레임워크 구조
7. 알림/노티 구조
8. 런타임 로그 레벨 구조
9. 다운로드 감사 로그 구조
10. 품질 Gate 기준
```

## 6.2 개발 가이드

`specs/개발_가이드.html`에는 아래 내용을 포함하세요.

```text
1. 신규 업무 모듈 개발 방법
2. Controller / Service / Mapper 작성 기준
3. 표준 헤더 사용 방법
4. 표준 응답/오류 사용 방법
5. 트랜잭션 분리 사용 방법
6. 외부 연동 호출 방법
7. 주제영역 간 호출 방법
8. 공통 코드/메시지 사용 방법
9. EDU 샘플 사용 방법
10. Batch Job 작성 방법
11. 테스트 작성 기준
12. Definition of Done
```

## 6.3 관리자 가이드

`specs/관리자_가이드.html`에는 아래 내용을 포함하세요.

```text
1. ADM 메뉴 구조
2. 배치 관리
3. 온라인 거래 조회
4. 오류 로그 조회
5. 알림/노티 관리
6. 다운로드 권한/마스킹/감사 로그
7. 런타임 로그 레벨 관리
8. 관리자 권한
9. 운영 장애 대응 흐름
10. 브라우저 검증 시나리오
```

## 6.4 SQL 가이드

`specs/SQL_가이드.html`을 새로 만들고 아래 내용을 통합하세요.

```text
1. DB 구조
2. Flyway 기준
3. 합본 SQL 기준
4. Spring Batch BATCH_* 테이블
5. pfw_batch_* 테이블
6. ADM 운영 테이블
7. 알림 테이블
8. 다운로드 감사 로그 테이블
9. 런타임 로그 레벨 테이블
10. seed 데이터 기준
11. MariaDB smoke 검증 방법
```

---

# 7. 소스/문서 포맷 정상화

에디터 soft wrap 기준이 아니라 실제 파일 개행 기준으로 정리하세요.
단, 실제 로컬 파일이 이미 정상 포맷이면 불필요하게 다시 변경하지 마세요.

## 7.1 우선 정리 대상

```text
build.gradle
scripts/check-legacy-name.ps1
scripts/check-utf8.ps1

CPF_STABILIZATION_REPORT.html

adm/src/main/java/cpf/adm/opr/controller/AdmBatchController.java
adm/src/main/java/cpf/adm/opr/service/AdmBatchOperationService.java
adm/src/main/java/cpf/adm/opr/controller/AdmNotificationController.java
adm/src/main/java/cpf/adm/config/AdmBatchRepositoryConfig.java

adm/src/main/resources/static/adm/adm.js
adm/src/main/resources/static/adm/index.html

specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html

specs/sql/00_all_install_and_smoke.sql
specs/sql/migration/flyway/*.sql

xyz/src/main/java/cpf/xyz/edu/**/*.java
```

## 7.2 포맷 기준

```text
1. Java는 package, import, class, field, constructor, method 단위로 줄바꿈
2. JavaDoc과 주석은 문장 단위로 줄바꿈
3. PowerShell은 변수, 배열, 루프, 조건문 단위로 줄바꿈
4. Gradle은 plugins, allprojects, subprojects, task 단위로 줄바꿈
5. SQL은 CREATE, ALTER, INSERT, COMMENT 문장 단위로 줄바꿈
6. HTML 문서는 브라우저에서 보기 좋은 제목, 목차, 표, 코드블록 구조로 작성
7. 기능 매트릭스는 사람이 검수할 수 있는 HTML table로 작성
```

---

# 8. Swagger 경로 수정

현재 `/swagger-ui.html`이 HTTP 500으로 보고되었습니다.
이번 작업에서 반드시 수정하세요.

## 8.1 요구사항

```text
1. /v3/api-docs HTTP 200 확인
2. /swagger-ui/index.html HTTP 200 확인
3. /swagger-ui.html HTTP 200 또는 정상 redirect 확인
4. HTTP 500 원인 분석
5. Swagger UI에서 ADM Batch, Logs, Notification, Runtime Log Level API 확인
6. Swagger UI에서 EDU API 확인
7. Swagger 설명에 깨진 한글 또는 FPS 잔재가 없는지 확인
```

---

# 9. ADM 브라우저 클릭 검증

API smoke만으로 완료 처리하지 마세요.
이번 작업에서는 실제 브라우저 또는 Playwright 등으로 ADM 화면 클릭 검증을 수행하세요.

## 9.1 검증 대상

```text
1. ADM 로그인
2. 배치 메뉴 진입
3. 배치 Job 목록 조회
4. 배치 상세 진입
5. Execution 목록 조회
6. Execution 상세 조회
7. Step 상세 조회
8. 배치 처리 건수/수량/결과 통계 확인
9. 스케줄 시뮬레이션 실행
10. 선행/후행/Trigger 관계 조회
11. 수행 대상 인스턴스 조회
12. 온라인 거래 조회
13. 온라인 거래 상세 조회
14. 오류 로그 조회
15. 오류 로그 상세 조회
16. 알림 룰 목록 조회
17. 알림 발송 이력 조회
18. 동적 로그레벨 목록 조회
19. 동적 로그레벨 변경
20. 동적 로그레벨 원복
21. Excel/CSV/로그 다운로드 버튼 확인
22. 권한 부족 시 버튼 숨김 또는 비활성 사유 표시 확인
```

## 9.2 완료 기준

```text
1. 브라우저 클릭 검증 결과가 CPF_STABILIZATION_REPORT.html에 있음
2. 실패 화면은 원인과 로그를 기록
3. 권한별 UX 미검증이면 미검증으로 표시
4. 화면별 결과표 작성
```

---

# 10. ADM 알림 기능 보강

현재 알림이 조회 API 중심이라면 운영 기능으로 부족합니다.

## 10.1 보강 대상

```text
1. 알림 룰 등록
2. 알림 룰 수정
3. 알림 룰 삭제 또는 비활성
4. 알림 채널 관리
5. 수신자 관리
6. 수신 그룹 관리
7. 알림 템플릿 관리
8. 알림 발송 이력 조회
9. 실패 이력 조회
10. 재시도 정책
11. 중복 억제 정책
12. 배치 실패/지연/미수행과 알림 연계
13. 온라인 거래 오류와 알림 연계
14. 외부 API 오류와 알림 연계
```

---

# 11. 다운로드 기능 보강

다운로드는 단순 파일 내려받기가 아니라 권한, 마스킹, 사유, 감사 로그가 함께 있어야 합니다.

## 11.1 보강 대상

```text
1. 온라인 거래 목록 Excel 다운로드
2. 온라인 거래 목록 CSV 다운로드
3. 온라인 거래 상세 로그 다운로드
4. 오류 로그 Excel/CSV 다운로드
5. 배치 Job 목록 다운로드
6. 배치 Execution 목록 다운로드
7. 배치 Step 상세 다운로드
8. 알림 룰/발송 이력 다운로드
9. 동적 로그레벨 변경 이력 다운로드
10. 다운로드 사유 입력
11. 마스킹 기본 적용
12. 마스킹 해제 권한 분리
13. 대용량 다운로드 제한
14. 다운로드 감사 로그 저장
15. 다운로드 실패 감사 로그 저장
```

---

# 12. Batch scheduler 자동 수행 검증

EDU Job 수동 실행은 확인되었으므로, 이번에는 scheduler 자동 수행을 검증하세요.

## 12.1 검증 대상

```text
1. 자동 수행 스케줄 등록
2. 반복 수행 스케줄 등록
3. Cron 기반 다음 실행 시간 계산
4. 실제 scheduler trigger 수행
5. 영업일 조건 반영
6. 수행 가능 시간 조건 반영
7. 선행 Job 완료 조건 반영
8. 선행 Job 실패 시 후행 Job 차단
9. Trigger Job 연계 수행
10. 실행 결과 BATCH_* 및 pfw_batch_* 적재
```

---

# 13. Redis/Kafka 실연동 검증

실 broker 연동 가능성을 검증하거나, 환경 부재 시 명확히 미검증으로 남기세요.

## 13.1 검증 대상

```text
1. Redis 연결
2. Redis 장애 시 fallback
3. Kafka publish
4. Kafka consume
5. Kafka 장애 시 fallback
6. 메시지 중복 처리
7. 메시지 실패 로그
8. 알림/캐시/동적 로그레벨 전파와 연계
```

---

# 14. 테스트 보강

NO-SOURCE 모듈을 계속 방치하지 마세요.

## 14.1 보강 대상

```text
1. PFW 핵심 context test
2. PFW exception/message resolver test
3. PFW transaction header test
4. ADM Batch API slice test
5. ADM Notification API test
6. ADM Runtime Log Level API test
7. ADM Download policy test
8. XYZ EDU Batch execution test
9. XYZ EDU online transaction sample test
10. CMN code/message cache test
```

---

# 15. 실행 명령

가능한 범위에서 아래 명령을 실행하고 결과를 리포트에 기록하세요.

```powershell
.\gradlew.bat checkLegacyName
.\gradlew.bat checkUtf8
.\gradlew.bat checkMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
.\gradlew.bat clean :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
```

가능하면 아래도 수행하세요.

```text
1. MariaDB 00_all_install_and_smoke.sql 실행
2. ADM 앱 기동
3. /v3/api-docs 호출
4. /swagger-ui/index.html 호출
5. /swagger-ui.html 호출
6. ADM 브라우저 클릭 검증
7. Scheduler 자동 수행 검증
8. Redis/Kafka broker 검증
```

---

# 16. 완료 기준

이번 작업은 아래 조건을 충족해야 완료로 볼 수 있습니다.

```text
1. README.md 외 specs 루트의 불필요한 md 문서가 제거 또는 HTML 정본 문서로 통합됨
2. 기능_구현_매트릭스.md가 기능_구현_매트릭스.html로 전환됨
3. specs/sql/README.md 내용이 SQL_가이드.html로 통합됨
4. specs/index.html이 브라우저용 문서 포털로 정리됨
5. CPF_STABILIZATION_CHANGED_FILES.txt를 새로 만들지 않음
6. CPF_STABILIZATION_REPORT.html만 결과 산출물로 작성됨
7. 핵심 소스와 문서 포맷이 정상화됨
8. /swagger-ui.html HTTP 500이 해결됨
9. ADM 브라우저 클릭 검증 결과가 있음
10. 알림 기능이 조회 수준을 넘어 등록/수정/비활성/이력 관리까지 확장됨
11. 다운로드 기능이 권한/마스킹/사유/감사 로그까지 연결됨
12. Batch scheduler 자동 수행 검증 결과가 있음
13. Redis/Kafka 실연동 또는 명확한 미검증 사유가 있음
14. NO-SOURCE 모듈 핵심 테스트가 보강됨
15. qualityGate가 성공함
16. 실패/일부/미검증 항목이 숨겨지지 않음
```
