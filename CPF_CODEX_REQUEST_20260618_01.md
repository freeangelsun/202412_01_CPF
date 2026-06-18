# CPF 2차 안정화 요청서

## mojibake 전수 제거, 포맷 정상화, qualityGate 복구, Spring Batch 실실행 검증

현재 열려 있는 로컬 프로젝트 소스 기준으로 작업하세요.

이번 작업의 목적은 기능을 무리하게 더 추가하는 것이 아니라, CPF 프레임워크의 품질 기준선을 정상화하는 것입니다.

현재 상태는 기능 골격과 문서 매트릭스는 일부 정리되었지만, `checkMojibake` 실패로 `qualityGate`가 실패하고 있으며, 핵심 Java/문서/SQL 파일의 포맷이 아직 좋지 않고, Spring Batch EDU Job의 실제 실행 및 `BATCH_*` 테이블 적재 검증이 완료되지 않았습니다.

이번 작업에서는 아래 항목을 최우선으로 처리하세요.

---

# 1. 최우선 목표

```text
1. mojibake / 깨진 한글 전수 제거
2. checkMojibake 통과
3. qualityGate 성공 상태 복구
4. 한 줄로 몰린 핵심 Java/JS/SQL/HTML/MD 문서 포맷 정상화
5. 기존 한글 가이드 문서 포맷 개선
6. 기능_구현_매트릭스.md를 사람이 읽고 검수 가능한 형태로 재정리
7. EDU Batch Job 실제 실행 검증
8. Spring Batch BATCH_* 테이블 실제 적재 검증
9. pfw_batch_execution과 Spring Batch JobExecution 연계 검증
10. ADM Swagger 최소 기동 검증
11. ADM 배치 API 최소 smoke 검증
12. 모든 결과를 숨기지 않고 리포트에 기록
```

이번 작업에서는 새로운 대형 기능을 추가하기보다, 이미 드러난 실패 항목과 미검증 항목을 실제로 해결하는 데 집중하세요.

---

# 2. 절대 기준

```text
1. 리포트에 적힌 내용을 그대로 믿고 완료 처리하지 마세요.
2. 실제 소스, SQL, 문서, Swagger, 테스트를 직접 확인하세요.
3. 실행하지 않은 검증은 성공으로 쓰지 말고 미검증으로 표시하세요.
4. 일부만 처리한 기능은 완료가 아니라 일부로 표시하세요.
5. qualityGate가 실패하면 이번 작업은 완료가 아닙니다.
6. checkMojibake가 실패하면 이번 작업은 완료가 아닙니다.
7. Java/문서가 한 줄로 몰려 있으면 포맷 정상화 완료가 아닙니다.
8. BATCH_* 테이블에 실제 row가 생성되지 않으면 Spring Batch 실행 검증 완료가 아닙니다.
9. 가이드 문서에 적은 기능은 실제 소스/API/SQL/Swagger/EDU 샘플과 맞아야 합니다.
10. 실패와 미검증을 숨기지 말고 리포트에 명확히 남기세요.
```

---

# 3. 산출물

작업 완료 후 루트에 아래 파일을 갱신하세요.

```text
CPF_STABILIZATION_REPORT.md
CPF_STABILIZATION_CHANGED_FILES.txt
```

`CPF_STABILIZATION_REPORT.md`에는 아래를 반드시 포함하세요.

```text
1. 이번 작업 전체 체크리스트
2. 완료 / 일부 / 실패 / 미검증 상태
3. 실제 수정한 파일 목록
4. 실제 확인한 파일 목록
5. 실행한 명령어와 결과
6. 실패한 명령어와 원인
7. 미검증 항목과 사유
8. 남은 리스크
9. 다음 작업 후보
10. 기능_구현_매트릭스와 실제 구현 대조 결과
```

---

# 4. mojibake / 깨진 한글 전수 제거

현재 `qualityGate` 실패의 핵심 원인은 강화된 `checkMojibake`에서 실제 깨진 한글이 잡히는 것입니다.
이번 작업의 1순위는 이 문제를 완전히 정리하는 것입니다.

## 4.1 수행 작업

```text
1. checkMojibake 실패 파일 목록을 먼저 추출하세요.
2. ACC / ADM / CMN / MBR / PFW / XYZ 전체에서 깨진 한글을 검색하세요.
3. Java, XML, YML, SQL, HTML, MD, JS, Gradle, PowerShell 파일을 모두 확인하세요.
4. 깨진 주석은 정상 한글로 복구하세요.
5. 의미를 알 수 없는 깨진 문장은 해당 기능에 맞는 자연스러운 한글 설명으로 다시 작성하세요.
6. Swagger summary/description에 깨진 문자가 있으면 반드시 수정하세요.
7. SQL COMMENT와 seed 설명에 깨진 문자가 있으면 반드시 수정하세요.
8. EDU 샘플의 깨진 문자는 우선적으로 수정하세요.
9. 수정 후 checkMojibake를 다시 실행하세요.
10. 최종적으로 qualityGate가 성공해야 합니다.
```

## 4.2 검색 대상 패턴

아래 패턴은 반드시 검색하세요.

```text
?꾨젅
?꾩썙
?쒕
?몄
?ㅻ
?꾪
?곸
?좏
?댁
?덉
濡쒓렇
嫄곕옒
二쇱젣
⑤뱢
硫붿씤
ì
ë
í
ê
�
```

## 4.3 완료 기준

```text
1. checkMojibake 성공
2. checkUtf8 성공
3. qualityGate 성공
4. CPF_STABILIZATION_REPORT.md에 수정 파일 목록 기록
5. 남은 깨진 한글이 없다는 최종 검색 결과 기록
```

---

# 5. FPS/Fps/fps 레거시 잔재 재확인

이전 작업에서 FPS 잔재를 정리했다고 보고되었지만, 이번 안정화 작업에서도 다시 확인하세요.

## 5.1 검색 대상

```text
FPS
Fps
fps
X-Fps
X-FPS
x-fps
com.fps
kr.fps
package fps
import fps
source=fps
fpsDB
fps-db
fps_
_fps
@Fps
FpsWebClient
FpsTransaction
FpsWorkflow
FpsBusinessException
```

## 5.2 완료 기준

```text
1. checkLegacyName 성공
2. 검색 결과가 0건이거나 legacy alias 사유가 명확히 문서화됨
3. Java 클래스명, import, Swagger 설명, SQL, 문서, 테스트 데이터에 FPS 잔재가 없음
```

---

# 6. 핵심 소스 포맷 정상화

현재 일부 핵심 Java/문서 파일이 한 줄 또는 몇 줄로 몰려 있어 유지보수성이 낮습니다.
컴파일 가능 여부와 별개로 개발자가 읽고 수정할 수 있는 형태로 정리하세요.

## 6.1 우선 정리 대상

```text
adm/src/main/java/cpf/adm/opr/controller/AdmBatchController.java
adm/src/main/java/cpf/adm/opr/service/AdmBatchOperationService.java
adm/src/main/resources/static/adm/adm.js
adm/src/main/resources/static/adm/index.html

xyz/src/main/java/cpf/xyz/edu/controller/*.java
xyz/src/main/java/cpf/xyz/edu/config/*.java
xyz/src/main/java/cpf/xyz/edu/service/*.java
xyz/src/main/java/cpf/xyz/edu/dto/*.java

pfw/src/main/java/cpf/pfw/**/*.java
cmn/src/main/java/cpf/cmn/**/*.java
adm/src/main/java/cpf/adm/**/*.java

specs/기능_구현_매트릭스.md
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/transaction-header-standard.md
specs/ci-quality-gate.md
specs/definition-of-done.md
specs/module-template.md
specs/operation-security-guide.md

specs/sql/README.md
specs/sql/00_all_install_and_smoke.sql
specs/sql/migration/flyway/*.sql
```

## 6.2 포맷 기준

```text
1. Java는 package / import / class / field / constructor / method 단위로 줄바꿈하세요.
2. 긴 SQL 문자열은 의미 단위로 분리하거나 읽기 쉽게 정리하세요.
3. JS는 함수 단위로 줄바꿈하세요.
4. SQL은 CREATE / ALTER / INSERT / COMMENT 문장 단위로 줄바꿈하세요.
5. Markdown 표는 사람이 읽을 수 있게 줄바꿈하세요.
6. HTML 확장자 문서가 Markdown 형식이면 현재 프로젝트 방식에 맞춰 읽기 쉽게 정리하세요.
7. 포맷 변경 중 로직을 임의로 바꾸지 마세요.
8. 포맷 후 compile/test/qualityGate를 다시 실행하세요.
```

## 6.3 완료 기준

```text
1. 핵심 Java 파일이 더 이상 4줄/10줄 같은 형태로 몰려 있지 않음
2. 기능_구현_매트릭스.md가 사람이 읽을 수 있는 표 형태로 정리됨
3. 한글 가이드 문서가 검수 기준으로 읽기 쉬운 형태가 됨
4. 포맷 변경 후 컴파일 성공
5. 포맷 변경 후 qualityGate 성공
```

---

# 7. 한글 가이드 문서 보강

한글 가이드 문서는 앞으로 기능 검수 기준으로 사용할 산출물입니다.
가이드 문서만 보고도 어떤 기능이 구현되어 있고, 어느 소스/API/화면/테이블/Swagger/EDU 샘플에서 확인하는지 알 수 있어야 합니다.

## 7.1 유지할 정본 문서

```text
README.md
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/sql/README.md
specs/기능_구현_매트릭스.md
```

영어명 guide 문서를 새 기준으로 만들지 말고, 기존 한글 가이드 문서명을 유지하세요.

## 7.2 기능_구현_매트릭스.md 보강

`specs/기능_구현_매트릭스.md`는 검수 기준 문서가 되어야 합니다.

아래 컬럼을 유지하세요.

```markdown
| 대분류 | 기능명 | 구현 상태 | 주요 소스 | API | ADM 화면 | DB 테이블 | Swagger Tag | EDU 샘플 | 테스트 | 검증 방법 | 비고 |
|---|---|---|---|---|---|---|---|---|---|---|---|
```

아래 기능군을 반드시 포함하고, 구현 상태를 숨기지 마세요.

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

## 7.3 완료 기준

```text
1. 가이드에 적은 API가 실제 Controller에 있음
2. 가이드에 적은 ADM 화면이 실제 HTML/JS에 있음
3. 가이드에 적은 DB 테이블이 실제 SQL/Flyway에 있음
4. 가이드에 적은 EDU 샘플이 실제 xyz EDU 코드에 있음
5. 가이드에 적은 Swagger Tag가 실제 Swagger annotation에 있음
6. 구현되지 않은 기능은 미구현/일부 구현/미검증으로 표시됨
```

---

# 8. Spring Batch EDU Job 실제 실행 검증

현재 CPF 운영 메타 seed는 들어갔지만, Spring Batch 표준 `BATCH_*` 테이블 row가 0건으로 남아 있습니다.
이번 작업에서는 EDU Batch Job을 실제로 실행해서 `BATCH_*` 테이블 적재를 검증하세요.

## 8.1 실행 대상 Job

```text
CPF_EDU_TASKLET_JOB
CPF_EDU_CHUNK_JOB
CPF_EDU_RETRY_JOB
```

가능하면 아래 Job도 검증하세요.

```text
CPF_EDU_BUSINESS_DAY_JOB
CPF_EDU_TRIGGER_JOB
```

## 8.2 검증 항목

```text
1. 앱 기동
2. EDU Batch Job bean 로딩 확인
3. ADM API 또는 테스트를 통한 Job 실행
4. BATCH_JOB_INSTANCE row 생성 확인
5. BATCH_JOB_EXECUTION row 생성 확인
6. BATCH_JOB_EXECUTION_PARAMS row 생성 확인
7. BATCH_STEP_EXECUTION row 생성 확인
8. BATCH_STEP_EXECUTION_CONTEXT row 생성 확인
9. BATCH_JOB_EXECUTION_CONTEXT row 생성 확인
10. pfw_batch_execution과 Spring Batch JobExecution ID 연계 확인
11. ADM Execution 목록에서 실행 이력 조회 확인
12. ADM Execution 상세에서 Step 목록 조회 확인
```

## 8.3 배치 처리 건수/수량 검증

아래 값이 실제 적재되고 조회되는지 확인하세요.

```text
1. 대상 건수
2. 성공 건수
3. 실패 건수
4. Skip 건수
5. readCount
6. writeCount
7. filterCount
8. skipCount
9. commitCount
10. rollbackCount
11. readSkipCount
12. processSkipCount
13. writeSkipCount
14. 처리 시간
15. 실패 Step
16. 오류 메시지
```

## 8.4 완료 기준

```text
1. 최소 1개 EDU Job 실제 실행 성공
2. BATCH_JOB_INSTANCE row 1건 이상 생성
3. BATCH_JOB_EXECUTION row 1건 이상 생성
4. BATCH_STEP_EXECUTION row 1건 이상 생성
5. pfw_batch_execution과 Spring Batch JobExecution 연계 확인
6. 처리 건수/수량이 ADM API 또는 SQL로 조회됨
7. 실패/재수행 Job은 실패 상태와 재수행 가능 여부 확인
```

실제 실행하지 못하면 완료로 쓰지 말고 미검증 또는 실패로 기록하세요.

---

# 9. Swagger 최소 기동 검증

이번 작업에서는 전체 브라우저 검증까지 못 하더라도, 최소한 Swagger 기동 검증은 수행하세요.

## 9.1 검증 대상

```text
1. ADM /v3/api-docs
2. ADM /swagger-ui.html
3. ADM /swagger-ui/index.html
4. Batch API 노출
5. Transaction 조회 API 노출
6. Error 조회 API 노출
7. Notification API 노출
8. Download API 노출
9. Runtime Log Level API 노출
10. EDU API 노출
11. 공통 헤더/응답/오류 스키마 노출
```

## 9.2 완료 기준

```text
1. 앱 기동 성공
2. /v3/api-docs HTTP 200 확인
3. Swagger JSON 파일 저장 또는 주요 API 목록 리포트 기록
4. EDU Swagger Tag 확인
5. 깨진 한글/FPS 잔재가 Swagger 설명에 없음
```

---

# 10. ADM 최소 smoke 검증

전체 브라우저 클릭 자동화가 어렵더라도, 최소 API smoke는 수행하세요.

## 10.1 검증 대상 API

```text
1. 배치 Job 목록 조회
2. 배치 스케줄 목록 조회
3. 배치 Execution 목록 조회
4. 배치 Execution 상세 조회
5. 배치 관계 조회
6. 배치 수행 대상 인스턴스 조회
7. 온라인 거래 목록 조회
8. 오류 로그 목록 조회
9. 알림 룰 목록 조회
10. 다운로드 권한 정책 조회 또는 다운로드 API mock 호출
11. Runtime Log Level 목록 조회
12. Runtime Log Level 변경/원복
```

## 10.2 완료 기준

```text
1. 각 API HTTP status 확인
2. 오류 발생 API는 원인 기록
3. 미구현 API는 미구현으로 표시
4. 인증/권한 때문에 실패하면 권한 설정 또는 seed 보강
5. 결과를 CPF_STABILIZATION_REPORT.md에 표로 기록
```

---

# 11. 테스트 / qualityGate 신뢰도 회복

테스트가 비활성화되어 있거나 SKIPPED/NO-SOURCE가 많다면 이유를 정리하고 가능한 범위에서 복구하세요.

## 11.1 검색 대상

```text
enabled = false
tasks.withType(Test)
configureEach { enabled = false }
SKIPPED
NO-SOURCE
```

## 11.2 필수 테스트 또는 check task

```text
1. checkLegacyName
2. checkMojibake
3. EDU Batch Job bean loading test
4. EDU Tasklet Job 실행 test
5. EDU Chunk Job 실행 test
6. Batch JobRepository 적재 test
7. pfw_batch_execution 연계 test
8. 배치 처리 건수/수량/결과 통계 적재 test
9. ADM Batch API slice/context test
10. ADM Runtime Log Level API test
11. Download permission policy test
12. Standard error/message sample test
```

## 11.3 완료 기준

```text
1. compile 성공
2. test 성공
3. checkLegacyName 성공
4. checkMojibake 성공
5. qualityGate 성공
6. SKIPPED/NO-SOURCE 항목은 사유 기록
```

---

# 12. 실행 명령

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
3. Swagger URL 호출
4. EDU Batch Job 실행
5. BATCH_* 테이블 적재 확인
6. pfw_batch_* 테이블 적재 확인
7. 배치 처리 건수/수량/결과 통계 확인
8. ADM API smoke 검증
9. Runtime Log Level API 검증
```

실행하지 못한 항목은 미검증으로 기록하세요.

---

# 13. 최종 리포트 체크리스트

`CPF_STABILIZATION_REPORT.md`에 아래 표를 포함하세요.

```markdown
## CPF 2차 안정화 체크리스트

| 번호 | 항목 | 상태 | 증빙 | 비고 |
|---|---|---|---|---|
| 1 | checkLegacyName 성공 | 완료/일부/실패/미검증 |  |  |
| 2 | FPS/Fps/fps 잔재 재확인 | 완료/일부/실패/미검증 |  |  |
| 3 | checkMojibake 성공 | 완료/일부/실패/미검증 |  |  |
| 4 | mojibake 전수 제거 | 완료/일부/실패/미검증 |  |  |
| 5 | checkUtf8 성공 | 완료/일부/실패/미검증 |  |  |
| 6 | qualityGate 성공 | 완료/일부/실패/미검증 |  |  |
| 7 | 핵심 Java 포맷 정상화 | 완료/일부/실패/미검증 |  |  |
| 8 | 핵심 SQL 포맷 정상화 | 완료/일부/실패/미검증 |  |  |
| 9 | 핵심 한글 가이드 문서 포맷 정상화 | 완료/일부/실패/미검증 |  |  |
| 10 | 기능_구현_매트릭스.md 재정리 | 완료/일부/실패/미검증 |  |  |
| 11 | 가이드 문서와 실제 소스 대조 | 완료/일부/실패/미검증 |  |  |
| 12 | EDU Batch Job bean 로딩 | 완료/일부/실패/미검증 |  |  |
| 13 | EDU Tasklet Job 실제 실행 | 완료/일부/실패/미검증 |  |  |
| 14 | EDU Chunk Job 실제 실행 | 완료/일부/실패/미검증 |  |  |
| 15 | EDU Retry Job 실제 실행 | 완료/일부/실패/미검증 |  |  |
| 16 | BATCH_JOB_INSTANCE 적재 | 완료/일부/실패/미검증 |  |  |
| 17 | BATCH_JOB_EXECUTION 적재 | 완료/일부/실패/미검증 |  |  |
| 18 | BATCH_STEP_EXECUTION 적재 | 완료/일부/실패/미검증 |  |  |
| 19 | pfw_batch_execution 연계 | 완료/일부/실패/미검증 |  |  |
| 20 | 배치 처리 건수/수량 조회 | 완료/일부/실패/미검증 |  |  |
| 21 | Swagger /v3/api-docs 확인 | 완료/일부/실패/미검증 |  |  |
| 22 | EDU Swagger Tag 확인 | 완료/일부/실패/미검증 |  |  |
| 23 | ADM Batch API smoke | 완료/일부/실패/미검증 |  |  |
| 24 | ADM Online Transaction API smoke | 완료/일부/실패/미검증 |  |  |
| 25 | ADM Error API smoke | 완료/일부/실패/미검증 |  |  |
| 26 | ADM Notification API smoke | 완료/일부/실패/미검증 |  |  |
| 27 | Runtime Log Level API smoke | 완료/일부/실패/미검증 |  |  |
| 28 | 테스트 SKIPPED/NO-SOURCE 사유 정리 | 완료/일부/실패/미검증 |  |  |
| 29 | 남은 미구현/미검증 항목 정리 | 완료/일부/실패/미검증 |  |  |
```

---

# 14. 완료 기준

이번 작업은 아래 조건을 충족해야 완료로 볼 수 있습니다.

```text
1. checkLegacyName 성공
2. checkMojibake 성공
3. checkUtf8 성공
4. qualityGate 성공
5. 핵심 Java/SQL/한글 가이드 문서 포맷 정상화
6. 기능_구현_매트릭스.md가 사람이 읽고 검수 가능한 형태로 재정리
7. 최소 1개 이상 EDU Batch Job 실제 실행
8. BATCH_JOB_INSTANCE / BATCH_JOB_EXECUTION / BATCH_STEP_EXECUTION 실제 row 생성
9. pfw_batch_execution과 Spring Batch JobExecution 연계 확인
10. 배치 처리 건수/수량/결과 통계 조회 확인
11. ADM /v3/api-docs 실제 호출 확인
12. ADM 주요 API smoke 검증 결과 기록
13. 실패/미검증 항목을 숨기지 않고 리포트에 기록
```

이번 작업은 CPF 프레임워크의 품질 기준선을 회복하는 작업입니다.
기능 추가보다 `qualityGate 성공`, `mojibake 제거`, `포맷 정상화`, `Spring Batch 실실행 검증`, `Swagger/API 최소 기동 검증`을 우선하세요.
