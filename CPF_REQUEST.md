# CPF 재작업 요청서

## 필수 운영 기능 개발, 문서 정본화, 품질 Gate 정상화, 빈 폴더 정리

현재 열려 있는 로컬 프로젝트 소스 기준으로 작업하세요.

이번 작업은 단순 문서 정리나 보강이 아닙니다.
CPF 프레임워크가 실제 운영 가능한 금융권 공통 프레임워크가 되기 위해 필요한 필수 기능을 개발하고, 직전 작업에서 남은 문서/품질/구조 문제를 정리하는 작업입니다.

---

# 1. 이번 작업 핵심 목표

```text
1. README.md 외 specs 정본 문서는 브라우저에서 바로 볼 수 있는 실제 HTML 문서로 정리
2. 검수용 리포트는 md 또는 html 중 더 적합한 형식으로 1개만 작성
3. 변경파일 목록 txt는 새로 만들거나 갱신하지 않음
4. specs 아래 불필요한 md 문서 제거 또는 HTML 정본 문서로 통합
5. 빈 폴더만 남은 디렉터리는 필요 여부 판단 후 삭제
6. qualityGate 타임아웃 원인 해결
7. ADM 알림 기능을 Controller 직접 처리 방식이 아닌 Service/DTO 구조로 개선
8. ADM 다운로드 공통 기능 개발
9. 배치 scheduler 자동 수행 기능 개발
10. OpenAPI/Swagger 검증 자동화
11. ADM UI/DOM/API smoke 검증 추가
12. 성공하지 않은 항목은 리포트에 성공으로 쓰지 않음
```

---

# 2. 산출물 기준

## 2.1 검수용 리포트

검수용 리포트는 아래 둘 중 하나만 유지하세요.

```text
CPF_STABILIZATION_REPORT.md
또는
CPF_STABILIZATION_REPORT.html
```

리포트 파일 형식은 작업자가 판단하세요.

```text
- HTML이 브라우저 확인에 더 적합하면 html 사용
- Markdown이 검수/검색/수정에 더 적합하면 md 사용
- 단, 둘 다 동시에 최신 산출물로 유지하지 말 것
```

리포트는 최종 제품 문서가 아니라 검수용 임시 산출물입니다.
나중에 개발 완료 후 삭제될 수 있으므로, 이 파일에 한해서는 md/html 형식 중 더 실용적인 방식을 선택해도 됩니다.

## 2.2 만들지 말아야 할 산출물

아래 파일은 새로 만들거나 갱신하지 마세요.

```text
CPF_STABILIZATION_CHANGED_FILES.txt
```

기존 파일이 남아 있으면 아래 기준으로 처리하세요.

```text
1. 더 이상 사용하지 않는 검수 산출물이면 삭제
2. 삭제가 어렵다면 README 또는 specs/index.html에서 링크하지 않음
3. 변경 파일 목록은 별도 파일로 만들지 말고 리포트 안에 요약 섹션으로 포함
```

---

# 3. 문서 정본화 기준

## 3.1 README.md

`README.md`는 루트 진입점으로 유지합니다.
단, 너무 길게 만들지 말고 아래 내용 중심으로 간단히 정리하세요.

```text
1. CPF 프로젝트 개요
2. 모듈 구성
3. 실행/빌드 기본 명령
4. specs/index.html 문서 포털 링크
5. 주요 가이드 링크
6. 현재 검수 리포트 링크
```

## 3.2 specs 정본 문서

아래 문서는 실제 HTML 문서로 유지하세요.

```text
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
```

위 파일들은 단순히 확장자만 `.html`이면 안 됩니다.
반드시 실제 HTML 구조를 갖춰야 합니다.

```text
<!doctype html>
<html lang="ko">
<head>
<body>
<main>
<section>
<table>
```

## 3.3 HTML 문서 금지 패턴

`specs/*.html` 파일 안에 아래 형태가 남아 있으면 실패로 처리하세요.

```text
# 제목
## 제목
| 컬럼 | 컬럼 |
[링크](파일.md)
아래 md 문서
deprecated md 문서
```

즉, Markdown 문서를 확장자만 html로 바꾸는 방식은 허용하지 않습니다.

---

# 4. specs md 문서 정리

README.md를 제외하고, specs 아래 설명 문서는 가급적 HTML로 관리합니다.

## 4.1 정리 대상

아래 파일이 남아 있으면 내용을 정본 HTML 문서에 통합한 뒤 삭제하세요.

```text
specs/**/*.md
specs/sql/README.md
specs/기능_구현_매트릭스.md
specs/transaction-header-standard.md
specs/ci-quality-gate.md
specs/definition-of-done.md
specs/module-template.md
specs/operation-security-guide.md
```

## 4.2 통합 위치

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

---

# 5. 빈 폴더 정리

문서와 파일이 정리되면서 빈 폴더만 남은 곳이 있으면 삭제 여부를 판단하세요.

## 5.1 삭제 가능한 폴더

아래 조건을 모두 만족하면 삭제해도 됩니다.

```text
1. 실제 파일이 하나도 없음
2. .gitkeep, README, package-info.java 같은 유지 목적 파일도 없음
3. 소스/리소스/테스트에서 참조하지 않음
4. 빌드 스크립트에서 참조하지 않음
5. 향후 모듈 템플릿으로 유지할 명확한 사유가 없음
```

## 5.2 삭제하면 안 되는 폴더

아래 폴더는 단순히 비어 보인다고 삭제하지 마세요.

```text
.git
.gradle
.idea
.vscode
build
out
target
generated
src/main/java
src/main/resources
src/test/java
src/test/resources
```

단, 모듈 내부에 실제로 불필요한 빈 하위 패키지나 문서 삭제 후 남은 빈 폴더는 정리해도 됩니다.

## 5.3 확인 명령 예시

PowerShell 기준으로 빈 폴더 후보를 확인하세요.

```powershell
Get-ChildItem -Directory -Recurse |
  Where-Object {
    -not (Get-ChildItem -LiteralPath $_.FullName -Force | Where-Object { $_.Name -notin @('.gitkeep') })
  } |
  Select-Object FullName
```

빈 폴더를 삭제한 경우 리포트에 아래를 기록하세요.

```text
1. 삭제한 폴더 경로
2. 삭제 사유
3. 참조 여부 확인 결과
```

---

# 6. 품질 Gate 정상화

직전 작업에서 개별 검사는 성공했으나 `qualityGate`는 타임아웃으로 보고되었습니다.
이번 작업에서는 반드시 원인을 분리해서 해결하세요.

## 6.1 요구사항

```text
1. qualityGate가 어떤 하위 task에서 지연되는지 확인
2. 외부 서버, DB, bootRun, 브라우저 검증, 장시간 smoke가 qualityGate에 포함되어 있으면 분리
3. qualityGate는 정적 검사, 인코딩 검사, legacy 명칭 검사, compile, unit test 중심으로 구성
4. MariaDB smoke, OpenAPI smoke, ADM UI smoke, scheduler smoke는 별도 task 또는 별도 명령으로 분리
5. 최종적으로 .\gradlew.bat qualityGate --offline 성공
```

---

# 7. Java/문서 포맷 검사 보강

앞으로 같은 포맷 문제가 반복되지 않도록 자동 검사를 추가하세요.

## 7.1 Java 포맷 검사

아래 조건은 실패로 처리하세요.

```text
1. package와 import가 같은 줄에 있음
2. import 여러 개가 한 줄에 붙어 있음
3. annotation, class 선언, method 선언이 한 줄에 과도하게 붙어 있음
4. Java 파일에 비정상적으로 긴 한 줄이 존재함
5. Controller/Service 주요 파일이 물리 개행 기준으로 몇 줄에 몰려 있음
```

우선 검사 대상:

```text
adm/src/main/java/cpf/adm/opr/controller/AdmBatchController.java
adm/src/main/java/cpf/adm/opr/service/AdmBatchOperationService.java
adm/src/main/java/cpf/adm/opr/controller/AdmNotificationController.java
adm/src/main/java/cpf/adm/opr/controller/AdmDynamicLogLevelController.java
```

## 7.2 HTML 문서 검사

아래 조건은 실패로 처리하세요.

```text
1. specs/*.html이 실제 HTML 구조가 아님
2. specs/*.html 안에 Markdown 제목 문법이 남아 있음
3. specs/*.html 안에 Markdown table 문법이 남아 있음
4. specs/*.html 안에 .md 링크가 남아 있음
5. specs 아래 README.md 외 md 파일이 남아 있음
```

---

# 8. ADM 알림 기능 개발

현재 알림 API가 일부 추가되었더라도 Controller 직접 SQL 처리 방식이면 완료로 보지 않습니다.
운영 기능으로 유지보수할 수 있도록 Service/DTO 구조로 개선하세요.

## 8.1 개발 대상

```text
AdmNotificationController
AdmNotificationService
AdmNotificationRuleRequest
AdmNotificationRuleResponse
AdmNotificationDeliveryLogResponse
NotificationSender
MockNotificationSender
NotificationSendResult
```

## 8.2 필수 기능

```text
1. 알림 규칙 목록 조회
2. 알림 규칙 상세 조회
3. 알림 규칙 등록
4. 알림 규칙 수정
5. 알림 규칙 비활성
6. 알림 발송 이력 조회
7. 알림 테스트 발송
8. 감사 사유 필수
9. 감사 로그 저장
10. Swagger/OpenAPI 설명 작성
11. ADM 화면 버튼 연결
```

## 8.3 알림 연계 대상

```text
1. 배치 실패
2. 배치 지연
3. 배치 미수행
4. 온라인 거래 오류
5. 외부 API 오류
6. 시스템 오류
```

실제 외부 발송 채널이 없으면 mock sender로 먼저 구현하고, 향후 Redis/Kafka/Email/Mattermost 연동이 가능하도록 인터페이스를 분리하세요.

---

# 9. ADM 다운로드 공통 기능 개발

다운로드는 운영 감사 대상 기능입니다.
단순 파일 내려받기가 아니라 권한, 마스킹, 사유, 감사 로그가 포함된 공통 기능으로 개발하세요.

## 9.1 개발 대상

```text
AdmDownloadController
AdmDownloadService
DownloadRequest
DownloadResult
DownloadPolicy
DownloadAuditLog
SensitiveDataMasker 연동
ADM 화면 다운로드 버튼 연결
```

## 9.2 필수 기능

```text
1. 온라인 거래 목록 CSV 다운로드
2. 오류 로그 목록 CSV 다운로드
3. 배치 실행 목록 CSV 다운로드
4. 알림 발송 이력 CSV 다운로드
5. 다운로드 사유 필수
6. 메뉴/버튼 권한 검사
7. 기본 마스킹 적용
8. 마스킹 해제 권한 분리
9. 다운로드 감사 로그 저장
10. 다운로드 실패 시 실패 감사 로그 저장
```

## 9.3 필요 테이블

필요하면 아래 테이블을 추가하세요.

```text
adm_download_audit_log
```

필수 컬럼:

```text
download_id
admin_id
menu_id
screen_id
download_type
target_type
search_condition_summary
row_count
masked_yn
include_sensitive_yn
reason
client_ip
user_agent
requested_at
completed_at
status
failure_reason
file_name
created_by
created_at
updated_by
updated_at
```

SQL 변경 시 아래 파일을 함께 갱신하세요.

```text
분리 SQL
00_all_install.sql
00_all_install_and_smoke.sql
Flyway migration
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
```

---

# 10. 배치 scheduler 자동 수행 기능 개발

현재 수동 실행과 조회 중심이라면 운영 프레임워크로 부족합니다.
`pfw_batch_schedule`을 기준으로 자동 수행하는 scheduler를 개발하세요.

## 10.1 개발 대상

```text
PfwBatchScheduler
PfwBatchScheduleService
PfwBatchExecutionTargetService
PfwBatchLockService
AdmBatchOperationService 연동
```

## 10.2 필수 기능

```text
1. enabled_yn = 'Y'인 스케줄 조회
2. cron_expression 기준 실행 대상 판단
3. 영업일 전용 여부 판단
4. 수행 가능 시작/종료 시간 판단
5. 중복 실행 방지 lock
6. pfw_batch_execution_target 생성
7. JobLauncher 실행
8. pfw_batch_execution 저장
9. pfw_batch_step_execution 저장
10. 실패 시 오류 메시지 저장
11. 실패/지연 시 알림 이벤트 생성
12. ADM 화면에서 자동 수행 결과 조회
```

## 10.3 검증

```text
1. EDU scheduler job 1개 이상 등록
2. 짧은 주기의 테스트 스케줄로 자동 수행 확인
3. BATCH_* 적재 확인
4. pfw_batch_execution 적재 확인
5. pfw_batch_execution_target 적재 확인
6. ADM execution 목록/상세에서 조회 확인
```

---

# 11. OpenAPI/Swagger 검증 자동화

수동 확인으로 끝내지 말고 자동 smoke를 추가하세요.

## 11.1 검증 대상

```text
1. /v3/api-docs HTTP 200
2. /swagger-ui/index.html HTTP 200
3. /swagger-ui.html HTTP 200 또는 정상 redirect
4. ADM-Batch tag 존재
5. ADM-Notification tag 존재
6. ADM-Log tag 존재
7. ADM-Dynamic-Log-Level tag 존재
8. ADM-Download tag 존재
9. EDU API tag 존재
10. 깨진 한글 없음
11. FPS/Fps/fps 잔재 없음
```

---

# 12. ADM UI/DOM/API smoke 추가

브라우저 자동 클릭이 어렵더라도, 대체 가능한 UI/DOM/API smoke를 구현하세요.

## 12.1 최소 검증 대상

```text
1. ADM index.html 로딩
2. 배치 메뉴 존재
3. 로그 메뉴 존재
4. 알림 메뉴 존재
5. 동적 로그레벨 메뉴 존재
6. 다운로드 버튼 존재
7. 주요 API path HTTP smoke
8. 권한 없는 경우 버튼 숨김 또는 비활성 처리 확인
```

가능한 방식 중 하나를 선택하세요.

```text
1. Playwright
2. Selenium
3. MockMvc + HTML selector 검사
4. HTTP smoke + DOM 파싱
```

---

# 13. 기능 구현 매트릭스 갱신

`specs/기능_구현_매트릭스.html`에 이번 작업 결과를 반영하세요.

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
비고
```

구현 상태는 아래 값만 사용하세요.

```text
완료
일부 구현
미구현
미검증
실패
```

과장하지 말고 실제 확인한 내용만 반영하세요.

---

# 14. 실행 명령

가능한 범위에서 아래를 실행하고 리포트에 결과를 기록하세요.

```powershell
.\gradlew.bat checkLegacyName
.\gradlew.bat checkUtf8
.\gradlew.bat checkMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
```

필요 시 별도 smoke로 아래를 수행하세요.

```text
1. MariaDB 00_all_install_and_smoke.sql
2. ADM bootJar
3. XYZ bootJar
4. ADM OpenAPI smoke
5. ADM UI/DOM/API smoke
6. EDU scheduler 자동 실행 smoke
```

---

# 15. 리포트 작성 기준

검수용 리포트에는 아래 내용을 포함하세요.

```text
1. 작업 요약
2. 완료 기능
3. 일부 구현 기능
4. 실패 기능
5. 미검증 기능
6. 실제 실행 명령
7. qualityGate 결과
8. SQL 변경 여부
9. Swagger/OpenAPI 검증 결과
10. ADM UI/DOM/API smoke 결과
11. 삭제 또는 통합한 문서 목록
12. 삭제한 빈 폴더 목록
13. 남은 리스크
14. 다음 개발 후보
```

성공하지 않은 항목은 성공으로 쓰지 마세요.
실제로 실행하지 않은 검증은 반드시 `미검증`으로 표시하세요.

---

# 16. 완료 기준

이번 작업은 아래 조건을 만족해야 완료입니다.

```text
1. README.md 외 specs 아래 불필요한 md 문서가 남아 있지 않음
2. specs 정본 문서가 실제 HTML 구조임
3. HTML 파일 안에 Markdown 문법이 남아 있지 않음
4. 검수용 리포트는 md/html 중 하나만 최신 산출물로 유지됨
5. 변경파일 목록 txt를 새로 만들거나 갱신하지 않음
6. 불필요한 빈 폴더가 정리됨
7. qualityGate가 성공함
8. Java 핵심 파일이 물리 개행 기준 정상 포맷임
9. 알림 기능이 Service/DTO/Sender 구조로 분리됨
10. 알림 테스트 발송 mock adapter가 있음
11. 다운로드 공통 API와 감사 로그가 구현됨
12. 배치 scheduler 자동 수행 기능이 구현됨
13. OpenAPI 자동 검증이 있음
14. ADM UI/DOM/API smoke가 있음
15. SQL, 문서, Swagger, 기능 매트릭스가 서로 맞음
16. 성공하지 않은 항목은 리포트에 성공으로 쓰지 않음
```
