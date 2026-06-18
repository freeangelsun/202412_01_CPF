# CPF 프레임워크 완성도 검증 및 미완료 기능 일괄 보강 최종 요청서

현재 열려 있는 로컬 프로젝트 소스 기준으로 작업하세요.

이번 작업은 단순 기능 추가가 아닙니다.
CPF CoreFlow Platform Framework가 개발자와 운영자가 실제로 사용할 수 있는 수준인지 실제 소스, SQL, Swagger, ADM 화면, EDU 샘플, 한글 가이드 문서, 테스트를 세부적으로 대조하고, 미완료 기능을 실제 동작 가능한 수준으로 보강하는 작업입니다.

리포트에 적힌 내용을 그대로 믿고 완료 처리하지 마세요.
반드시 실제 파일을 열어 소스, SQL, 화면, Swagger, 문서, EDU 샘플, 테스트를 직접 확인한 뒤 판단하세요.

실행하지 않은 검증은 성공으로 기록하지 말고 반드시 `미검증`으로 표시하세요.
일부만 구현된 기능은 `완료`가 아니라 `일부`로 표시하세요.
기능을 만들지 않고 문서만 고친 경우 완료로 표시하지 마세요.
문서에만 있고 실제 소스/SQL/Swagger/화면이 맞지 않으면 완료가 아닙니다.

---

# 1. 이번 작업의 최우선 목표

아래 항목을 실제 완성도 기준으로 검토하고 보강하세요.

```text
1. FPS/Fps/fps 레거시 잔재 전수 검색 및 제거
2. 깨진 한글/mojibake 전수 검색 및 제거
3. mojibake 검사 스크립트 강화
4. 한 줄로 몰린 Java/JS/SQL/MD/YAML/HTML 문서 포맷 정상화
5. specs 문서 파편화 정리 및 기존 한글 가이드 문서 정본화
6. README와 상세 한글 가이드 문서 역할 분리
7. PFW/CMN/ADM/주제영역 책임 재정리
8. 한글 가이드 문서를 실제 기능 검수 기준으로 작성
9. 기능별 구현 매트릭스 작성
10. EDU 샘플 전체 재정비
11. 온라인 거래 EDU 샘플 보강
12. 표준 헤더/거래 ID EDU 샘플 보강
13. 트랜잭션 분리 EDU 샘플 보강
14. 주제영역 간 호출 EDU 샘플 보강
15. 외부 연동 호출 EDU 샘플 보강
16. 입력/세션/요청 컨텍스트 표준 처리 EDU 샘플 보강
17. 공통 코드/공통 메시지 EDU 샘플 보강
18. 표준 예외/오류/동적 메시지 EDU 샘플 보강
19. 로그/감사/추적/마스킹 EDU 샘플 보강
20. 파일/Excel/CSV/로그 다운로드 EDU 샘플 보강
21. 메시징/비동기/알림 EDU 샘플 보강
22. 보안/JWT/OAuth/권한 EDU 샘플 보강
23. 배치 EDU 샘플 실사용 수준 보강
24. EDU 샘플 Swagger 정리
25. EDU 샘플 seed 데이터 보강
26. EDU Batch Job 실제 배치 메타 등록
27. EDU Batch Job 실제 실행 검증
28. Spring Batch BATCH_* 테이블 실제 적재 검증
29. pfw_batch_* 운영 메타와 Spring Batch 메타 연결 검증
30. 배치 처리 건수/성공 건수/실패 건수/Skip 건수/Step 결과 로그 보강
31. ADM 배치 관리 화면/API 완성도 보강
32. ADM 배치 상세에서 Execution 목록/상세/Step 상세 조회 보강
33. 자동 수행/반복 수행/스케줄/영업일/수행 가능 시간 관리 보강
34. 선행 Job/후행 Job/Trigger Job 관리 보강
35. 배치 수행 대상 인스턴스 관리 보강
36. 배치 수행 시뮬레이션 고도화
37. ADM 온라인 거래 로그 조회/상세 완성도 보강
38. 온라인 거래 처리 시간/외부호출 수/오류 수/로그 수량/관련 이벤트 수 표시 보강
39. ADM 오류 로그 조회/상세/조치 상태 관리 보강
40. ADM 알림/노티 룰/채널/수신자/템플릿/이력 관리 보강
41. ADM 권한 기반 Excel/CSV/로그 다운로드 보강
42. 다운로드 권한/마스킹/사유/감사 로그 보강
43. ADM 런타임 실시간 로그 레벨 변경 기능 보강
44. Swagger/OpenAPI 실제 노출 검증
45. ADM 브라우저 클릭 검증
46. 테스트 비활성화/SKIPPED/NO-SOURCE 점검
47. qualityGate 신뢰도 회복
48. 문서/소스/SQL/Swagger/EDU/ADM 화면 정합성 검증
```

---

# 2. 완료 판정 기준

아래 조건을 만족해야 `완료`로 표시할 수 있습니다.

```text
1. 실제 소스가 존재한다.
2. 필요한 SQL/seed/설정이 함께 존재한다.
3. Swagger/OpenAPI에 노출된다.
4. README 또는 정본 한글 가이드 문서에 설명되어 있다.
5. EDU 샘플이 필요한 기능은 EDU 샘플이 있다.
6. 테스트 또는 실행 검증 결과가 있다.
7. ADM 기능이면 화면에서 접근 가능한 구조가 있다.
8. 권한/마스킹/감사 로그가 필요한 기능은 해당 정책까지 반영되어 있다.
9. 소스/문서/SQL/Swagger/화면의 명칭과 내용이 서로 일치한다.
10. 깨진 글자, FPS 잔재, 한 줄 포맷 문제가 남아 있지 않다.
11. 기능 가이드 문서에 해당 기능의 구현 위치와 검증 방법이 명확히 기록되어 있다.
```

하나라도 빠지면 `완료`가 아니라 `일부`, `실패`, `미검증` 중 하나로 표시하세요.

---

# 3. 산출물

작업 완료 후 프로젝트 루트에 아래 파일을 생성 또는 갱신하세요.

```text
CPF_STABILIZATION_REPORT.md
CPF_STABILIZATION_CHANGED_FILES.txt
```

`CPF_STABILIZATION_REPORT.md`에는 아래 내용을 반드시 포함하세요.

```text
1. 이번 요청사항 전체 체크리스트
2. 각 항목별 상태: 완료 / 일부 / 실패 / 미검증
3. 실제 확인한 파일 목록
4. 실제 수정한 파일 목록
5. 실제 실행한 검증 명령과 결과
6. 실패한 명령과 실패 원인
7. 미검증 항목과 미검증 사유
8. 남은 리스크
9. 다음 보강 후보
10. 작업자가 판단한 추가 개선 의견
11. 문서/소스/SQL/Swagger/ADM/EDU 정합성 결과
12. 한글 가이드 문서와 실제 구현 대조 결과
```

`CPF_STABILIZATION_CHANGED_FILES.txt`에는 수정한 파일 목록과 변경 요약을 작성하세요.

---

# 4. 한글 가이드 문서를 검수 기준 산출물로 작성

이번 작업에서 가이드 문서는 단순 설명 문서가 아니라, 향후 기능 검수와 구현 확인의 기준 문서가 되어야 합니다.

작업 리포트는 일시 산출물이지만, 가이드 문서는 프로젝트에 남는 공식 산출물입니다.
따라서 모든 프레임워크 기능은 한글 가이드 문서에 명확히 정리되어야 하며, 한글 가이드 문서만 보더라도 어떤 기능이 구현되어 있고 어디에서 확인해야 하는지 알 수 있어야 합니다.

## 4.1 기존 한글 가이드 문서명을 기준으로 유지

현재 specs 폴더에는 한글명 가이드 문서가 존재합니다.
영어 파일명 기준으로 새 문서 체계를 만들지 말고, 기존 한글 문서명을 정본으로 유지하면서 내용을 보강하세요.

정본 문서 기준:

```text
README.md
→ 프로젝트 소개, 실행 방법, 주요 가이드 링크만 유지

specs/index.html
→ specs 문서 인덱스 역할 유지

specs/프레임워크_구성_가이드.html
→ 프레임워크 구조, PFW/CMN/ADM/주제영역 책임, 공통 규약, DB 소유권, 관측성, 로그 레벨 관리 기준

specs/개발_가이드.html
→ 개발자가 업무 기능을 구현할 때 사용하는 개발 가이드, EDU 샘플 사용법, 온라인 거래/배치/외부연동/트랜잭션/코드/메시지/로그/보안/다운로드 샘플 기준

specs/관리자_가이드.html
→ ADM 운영 화면, 배치관리, 거래조회, 오류조회, 알림관리, 다운로드, 런타임 로그 레벨 관리, 권한관리

specs/sql/README.md
→ DB 구조, Flyway, 합본 SQL, BATCH_* 테이블, pfw_batch_* 관계, seed 정책

specs/기능_구현_매트릭스.md
→ 기능별 구현 위치, API, 화면, SQL, Swagger, EDU 샘플, 테스트, 검증 방법 매트릭스

specs/adr/*
→ 의사결정 이력만 보관
```

영어명 `framework-guide.md`, `development-guide.md`, `admin-guide.md`, `feature-implementation-matrix.md`를 새 기준으로 만들지 마세요.
이미 존재하는 한글 가이드 문서명을 기준으로 정리하세요.

## 4.2 HTML 확장자 문서 포맷 확인

현재 한글 가이드 문서가 `.html` 확장자를 사용하고 있어도 내용이 Markdown 형식이면, 다음 중 하나로 일관성 있게 정리하세요.

```text
1. 기존 파일명을 유지하고 Markdown 문법이 깨지지 않도록 정리
2. 또는 실제 HTML 문서로 변환
3. 단, 어떤 방식을 선택했는지 specs/index.html에 명확히 기록
4. 링크가 깨지면 안 됨
5. 기존 한글 문서명을 무시하고 영어 문서명으로 바꾸지 말 것
```

## 4.3 기능 구현 매트릭스 필수 작성

`specs/기능_구현_매트릭스.md`를 생성 또는 갱신하세요.
이 문서는 향후 검수자가 “이 기능이 실제 구현되었는지” 확인할 수 있는 기준 문서입니다.

아래 컬럼을 포함하세요.

```markdown
| 대분류 | 기능명 | 구현 상태 | 주요 소스 | API | ADM 화면 | DB 테이블 | Swagger Tag | EDU 샘플 | 테스트 | 검증 방법 | 비고 |
|---|---|---|---|---|---|---|---|---|---|---|---|
```

반드시 아래 기능군을 모두 포함하세요.

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

구현되지 않은 기능은 문서에서 숨기지 말고 `미구현`, `일부 구현`, `미검증`으로 명확히 표시하세요.

## 4.4 가이드 문서와 소스 정합성

가이드 문서에 설명된 기능은 실제 소스와 맞아야 합니다.

```text
1. 가이드에 적은 API는 실제 Controller에 있어야 합니다.
2. 가이드에 적은 ADM 화면은 실제 HTML/JS에 있어야 합니다.
3. 가이드에 적은 DB 테이블은 실제 SQL/Flyway에 있어야 합니다.
4. 가이드에 적은 EDU 샘플은 실제 xyz EDU 코드에 있어야 합니다.
5. 가이드에 적은 Swagger Tag는 실제 Swagger에 노출되어야 합니다.
6. 가이드에 적은 테스트는 실제 테스트 코드 또는 검증 명령에 있어야 합니다.
```

문서만 있고 구현이 없으면 완료로 표시하지 마세요.

---

# 5. PFW / CMN / ADM / 주제영역 책임 재정리

CPF 프레임워크의 모듈 책임을 실제 소스와 문서 기준으로 다시 점검하세요.

## 5.1 PFW 책임

PFW는 프레임워크 핵심 코어입니다.
업무 개발자가 편의상 직접 수정하는 영역이 아니라, 프레임워크 패치/업데이트/버전 릴리즈 기준으로 관리되는 보호 영역입니다.

PFW가 담당해야 할 기능:

```text
- 표준 거래 ID 생성/전파
- transactionId / globalId / traceId / correlationId / requestId / spanId / uuid 표준
- 표준 요청/응답 구조
- 표준 헤더 처리
- 공통 예외 처리
- 공통 로그 AOP
- 트랜잭션 컨텍스트
- SecurityContext 표준
- 인증/인가 필터 흐름
- OpenAPI 공통 헤더/응답/오류 스키마
- Spring Batch JobRepository 연동 표준
- 배치 메타/스케줄/영업일/수행 가능 시간/선행 Job/트리거 Job/로그 표준
- 배치 처리 건수/수량/결과 통계 표준
- 알림 이벤트/룰/발송 이력 표준
- 온라인 거래 로그/오류 로그/추적 로그 표준
- 런타임 로그 레벨 변경 표준
- 다운로드 감사 로그 표준
- 공통 감사 로그 이벤트
- framework auto-configuration
```

PFW에는 특정 업무 도메인 로직을 넣지 마세요.

## 5.2 CMN 책임

CMN은 업무 주제영역에서 재사용하는 공통 기능/API 영역입니다.

CMN이 담당해야 할 기능:

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

CMN은 특정 관리자/회원/업무별 로그인 정책과 권한 판단을 직접 소유하지 않습니다.

## 5.3 ADM 책임

ADM은 PFW/CMN 기능을 관리하는 운영 콘솔입니다.

ADM이 담당해야 할 기능:

```text
- 관리자 로그인
- 관리자 JWT/Refresh Token
- 관리자 MFA/OTP
- 관리자 메뉴 권한
- 관리자 버튼 권한
- 관리자 다운로드 권한
- 관리자 계정 잠금
- 관리자 로그인 이력
- 배치 관리 화면/API
- 알림/노티 관리 화면/API
- 온라인 거래 조회 화면/API
- 오류 로그 조회 화면/API
- 거래 추적 화면/API
- Excel/CSV/로그 다운로드 화면/API
- 런타임 로그 레벨 변경 화면/API
- 감사/운영 관제 화면/API
```

## 5.4 주제영역 책임

ACC, MBR, PAY, LNG, DAI, EXS, STM, MKT 등 주제영역은 실제 업무 기능을 담당합니다.

주제영역은 PFW/CMN을 사용하되, 프레임워크 공통 기능을 중복 구현하지 않습니다.

---

# 6. FPS/Fps/fps 레거시 잔재 전수 제거

기존 프로젝트명 또는 과거 프레임워크명으로 사용된 `FPS`, `Fps`, `fps` 잔재를 전수 검사하고 제거하세요.

현재 표준 명칭은 `CPF`입니다.
Java 패키지 표준은 `cpf.*`입니다.

## 6.1 검색 대상 패턴

아래 패턴을 전체 로컬 소스에서 검색하세요.

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

## 6.2 점검 범위

```text
Java source
Java test source
Gradle files
YAML files
XML files
SQL files
Markdown files
HTML files
JavaScript files
PowerShell scripts
properties files
Swagger summary/description
README
specs documents
package directory names
class names
method names
DTO names
constant names
header names
database seed data
sample data
```

## 6.3 수정 기준

```text
1. CPF 표준으로 변경 가능한 것은 CPF/Cpf/cpf로 변경하세요.
2. Java package는 cpf.* 기준으로 통일하세요.
3. 클래스명에 FPS/Fps가 남아 있으면 Cpf 또는 현재 기능명 기준으로 변경하세요.
4. FpsWebClient, FpsTransaction, FpsWorkflow, FpsBusinessException 등 PFW 핵심 클래스명도 CPF 기준으로 변경하세요.
5. 헤더명에 X-Fps 같은 과거 명칭이 남아 있으면 CPF 표준 헤더명으로 변경하세요.
6. 문서, SQL COMMENT, seed data, Swagger 설명에도 과거 명칭이 남아 있으면 수정하세요.
7. 테스트 코드와 테스트 데이터도 함께 수정하세요.
8. 외부 호환 alias로 꼭 남기는 경우에는 legacy compatibility 사유를 주석과 리포트에 남기세요.
```

## 6.4 리포트 필수 기록

```text
- FPS/Fps/fps 전수 검색 수행 여부
- 검색한 패턴 목록
- 발견된 파일 목록
- 수정한 파일 목록
- 남긴 legacy alias 목록
- legacy alias를 남긴 사유
- 최종 검색 결과
- 미해결 잔재 여부
```

FPS/Fps/fps 잔재가 남아 있으면 완료로 표시하지 마세요.

---

# 7. mojibake / 깨진 한글 / 인코딩 문제 전수 제거

UTF-8 검사 통과만으로 완료 처리하지 마세요.
이미 깨진 한글은 UTF-8로 저장해도 정상 한글이 아닙니다.

## 7.1 반드시 검색할 패턴

아래 패턴을 전체 소스에서 검색하세요.

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

## 7.2 점검 범위

```text
Java 전체
XML 전체
YAML 전체
SQL COMMENT 전체
Markdown 전체
HTML 전체
JavaScript 전체
PowerShell 전체
Gradle 전체
Swagger summary/description 전체
EDU 샘플 전체
```

## 7.3 요구사항

```text
1. 깨진 한글 주석은 정상 한글로 복구하세요.
2. 의미를 확정할 수 없으면 기능 기준으로 자연스러운 한글 설명을 다시 작성하세요.
3. Swagger summary/description에 깨진 문자가 있으면 반드시 수정하세요.
4. EDU 샘플의 깨진 문자는 학습 품질에 직접 영향을 주므로 우선 수정하세요.
5. SQL COMMENT와 seed 설명도 정상 한글로 수정하세요.
6. check-utf8.ps1의 mojibake 패턴을 강화하세요.
7. 실제 남아 있는 깨진 문자열이 검사에 걸리도록 스크립트를 보강하세요.
8. 검사 스크립트가 통과해도 실제 깨진 문자열이 남아 있으면 완료가 아닙니다.
```

## 7.4 리포트 필수 기록

```text
- 검색한 mojibake 패턴
- 발견된 파일 목록
- 수정한 파일 목록
- 검사 스크립트 보강 내용
- 최종 검사 결과
- 남은 깨진 문자 여부
```

---

# 8. Java / JS / SQL / MD / YAML / HTML 포맷 정상화

컴파일만 되는 상태로 두지 말고, 운영 개발자가 읽고 유지보수할 수 있는 형식으로 정리하세요.

## 8.1 우선 정리 대상

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

specs/sql/migration/flyway/*.sql
specs/sql/00_all_install_and_smoke.sql
specs/sql/README.md

README.md
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/기능_구현_매트릭스.md
specs/transaction-header-standard.md
specs/ci-quality-gate.md
specs/definition-of-done.md
specs/module-template.md
specs/operation-security-guide.md
```

## 8.2 정리 기준

```text
1. Java는 package/import/class/method 단위로 정상 줄바꿈하세요.
2. JS는 함수 단위로 정상 줄바꿈하세요.
3. SQL은 CREATE/ALTER/INSERT 문장 단위로 정상 줄바꿈하세요.
4. Markdown은 제목/표/코드블록이 정상 표시되도록 정리하세요.
5. HTML 확장자 문서가 Markdown 형식이면 해당 형식이 깨지지 않도록 정리하세요.
6. YAML은 들여쓰기 기준을 지키세요.
7. 긴 문자열은 의미가 깨지지 않게 정리하세요.
8. 포맷 정리 중 로직을 임의 변경하지 마세요.
9. 포맷 후 compile/test/qualityGate를 다시 실행하세요.
```

포맷이 여전히 한 줄 또는 몇 줄로 몰려 있으면 완료로 표시하지 마세요.

---

# 9. specs 문서 파편화 정리

specs 하위 문서가 파편화되어 있습니다.
기존 한글 가이드 문서 중심으로 정리하세요.

## 9.1 정본 문서 구조

```text
README.md
→ 프로젝트 소개, 실행 방법, 주요 가이드 링크만 유지

specs/index.html
→ specs 문서 인덱스

specs/프레임워크_구성_가이드.html
→ PFW/CMN/ADM/주제영역 책임, 프레임워크 구조, 공통 규약, DB 소유권, 관측성, 로그 레벨 관리

specs/개발_가이드.html
→ 업무 모듈 개발 방법, EDU 샘플 사용법, 배치 Job 추가 방법, Swagger, 테스트, 트랜잭션, 외부연동, 코드/메시지, 로그/감사, 다운로드, 보안

specs/관리자_가이드.html
→ ADM 운영 화면, 배치관리, 거래조회, 오류조회, 알림관리, 다운로드, 런타임 로그 레벨 관리, 권한관리

specs/sql/README.md
→ DB 구조, Flyway, 합본 SQL, BATCH_* 테이블, pfw_batch_* 관계

specs/기능_구현_매트릭스.md
→ 기능별 구현 위치, API, 화면, SQL, Swagger, EDU 샘플, 테스트, 검증 방법

specs/adr/*
→ 의사결정 이력만 보관
```

## 9.2 통합/정리 기준

아래 문서는 정본 문서에 통합하거나 deprecated 처리하세요.

```text
1. 내용이 중복되는 문서
2. 오래된 정책 문서
3. 단일 기능만 설명하는 짧은 문서
4. README와 내용이 겹치는 문서
5. 한글 가이드 정본과 중복되는 보조 문서
6. 현재 구현과 맞지 않는 문서
7. Markdown 내용인데 확장자만 html인 문서의 형식 불일치
```

삭제가 부담되면 문서 상단에 deprecated 문구를 명확히 남기세요.

```text
이 문서는 deprecated 상태입니다.
정본 문서는 specs/개발_가이드.html 또는 specs/관리자_가이드.html을 참조하세요.
```

## 9.3 리포트 필수 기록

```text
- 통합한 문서
- 유지한 문서
- deprecated 처리한 문서
- 삭제하지 않고 남긴 이유
- 각 정본 문서의 역할
- 문서와 실제 소스/SQL/Swagger/화면 정합성 여부
```

---

# 10. EDU 샘플 전체 재정비

EDU는 CPF 프레임워크를 처음 사용하는 개발자가 그대로 보고 따라 할 수 있는 학습용 표준 샘플이어야 합니다.

기존 EDU 샘플을 분석해서 중복/부족/깨진 문구/미검증 샘플을 정리하고, 아래 기능별 샘플을 완성하세요.

각 샘플은 가능한 한 아래 구조를 갖추세요.

```text
- Controller
- Service
- DTO
- Config
- Mapper 또는 Repository
- 필요 SQL seed
- Swagger 노출
- ADM에서 확인하는 방법
- DB에서 확인하는 방법
- 성공 케이스
- 오류 케이스
- 라인 단위에 가까운 한글 주석
- specs/개발_가이드.html 연결
- specs/기능_구현_매트릭스.md 연결
```

---

# 11. 필수 EDU 샘플 목록

아래 샘플은 개발자가 실무 개발 시 반드시 참고할 수 있어야 합니다.

## 11.1 기본 온라인 거래 샘플

```text
1. 단순 조회 API 샘플
2. 단순 등록 API 샘플
3. 수정 API 샘플
4. 삭제 또는 비활성 처리 API 샘플
5. Controller → Service → Mapper 흐름 샘플
6. 요청 DTO / 응답 DTO / validation annotation 샘플
7. 표준 응답 구조 사용 샘플
8. 표준 오류 응답 발생 샘플
9. 성공 거래 로그 확인 샘플
10. 오류 거래 로그 확인 샘플
11. 처리 시간 확인 샘플
12. 관련 로그 건수 확인 샘플
```

## 11.2 표준 거래/헤더/ID 샘플

```text
1. transactionId 생성/전파 샘플
2. globalId 전파 샘플
3. traceId 전파 샘플
4. correlationId 전파 샘플
5. requestId 전파 샘플
6. spanId 사용 샘플
7. UUID 기반 업무 식별자 사용 샘플
8. 표준 헤더 누락 시 처리 샘플
9. 표준 헤더를 Swagger에서 확인하는 샘플
10. ADM에서 ID 기준으로 거래 추적하는 샘플
```

## 11.3 트랜잭션 처리 샘플

```text
1. 단일 트랜잭션 샘플
2. 트랜잭션 분리 샘플
3. REQUIRES_NEW 감사 로그 저장 샘플
4. 메인 거래 실패 후 감사 로그 유지 샘플
5. rollback 샘플
6. readOnly transaction 샘플
7. 중첩 service 호출 시 propagation 샘플
8. 멱등성 보장 샘플
9. 중복 요청 방지 샘플
10. 오류 발생 시 거래 로그와 감사 로그 확인 샘플
```

## 11.4 주제영역 간 호출 샘플

```text
1. XYZ에서 MBR 호출 샘플
2. XYZ에서 ACC 호출 샘플
3. XYZ에서 CMN 공통 API 호출 샘플
4. CPF 표준 WebClient 사용 샘플
5. 표준 헤더 자동 전파 샘플
6. transactionId/globalId/traceId 전파 확인 샘플
7. 호출 실패 시 표준 오류 변환 샘플
8. 타임아웃 처리 샘플
9. 재시도 여부 판단 샘플
10. ADM에서 원거래와 연계거래를 함께 추적하는 샘플
11. 외부/내부 호출 건수 표시 샘플
```

## 11.5 외부 연동 호출 샘플

```text
1. 외부 REST API GET 호출 샘플
2. 외부 REST API POST 호출 샘플
3. connection timeout 설정 샘플
4. read timeout 설정 샘플
5. 외부 API 오류를 표준 외부연계 예외로 변환하는 샘플
6. 외부 요청/응답 요약 로그 샘플
7. 민감정보 마스킹 샘플
8. 외부 연동 실패 알림 샘플
9. 외부 연동 거래를 ADM에서 조회하는 샘플
10. EXS 모듈로 확장해야 하는 기준 설명
11. 외부 호출 수/성공 수/실패 수 확인 샘플
```

## 11.6 입력/세션/요청 컨텍스트 표준 처리 샘플

```text
1. 요청자 ID 처리 샘플
2. 관리자 ID 처리 샘플
3. 고객번호/회원번호 처리 샘플
4. 채널 코드 처리 샘플
5. 클라이언트 IP 처리 샘플
6. User-Agent 처리 샘플
7. 세션 없이 표준 헤더 기반으로 처리하는 샘플
8. 관리자 세션/토큰과 업무 사용자 토큰 분리 샘플
9. SecurityContext 사용 샘플
10. TransactionContext 사용 샘플
```

## 11.7 공통 코드/공통 메시지 샘플

```text
1. 공통 코드 조회 샘플
2. 코드 그룹 조회 샘플
3. 코드 캐시 사용 샘플
4. 코드 미존재 시 표준 오류 샘플
5. 공통 메시지 조회 샘플
6. 메시지 파라미터 치환 샘플
7. indexed message {0}, {1} 사용 샘플
8. 동적 메시지 사용 샘플
9. 내부 로그 메시지와 사용자 메시지 분리 샘플
10. ADM/DB에서 코드와 메시지를 확인하는 샘플
```

## 11.8 표준 예외/오류 샘플

```text
1. validation 오류 샘플
2. business 오류 샘플
3. external 오류 샘플
4. system 오류 샘플
5. response code 기반 오류 샘플
6. dynamic error code 샘플
7. indexed message 오류 샘플
8. 오류 로그 적재 확인 샘플
9. 오류 알림 발생 샘플
10. ADM에서 오류 상세 확인 샘플
```

## 11.9 로그/감사/추적 샘플

```text
1. 온라인 거래 로그 샘플
2. 오류 로그 샘플
3. 감사 로그 샘플
4. 업무 로그 샘플
5. 외부 연동 로그 샘플
6. 동적 로그 레벨 샘플
7. 요청/응답 payload 요약 로그 샘플
8. 민감정보 마스킹 로그 샘플
9. transactionId 기준 추적 샘플
10. globalId/traceId/correlationId 기준 추적 샘플
11. ADM에서 런타임 로그 레벨 변경 후 로그 출력 차이 확인 샘플
```

## 11.10 파일/다운로드 샘플

```text
1. 파일 업로드 샘플
2. 파일 다운로드 샘플
3. Excel 다운로드 샘플
4. CSV 다운로드 샘플
5. 로그 다운로드 샘플
6. 대용량 다운로드 제한 샘플
7. 다운로드 권한 체크 샘플
8. 다운로드 사유 입력 샘플
9. 다운로드 감사 로그 샘플
10. 마스킹/비마스킹 다운로드 권한 분리 샘플
```

## 11.11 메시징/비동기/알림 샘플

```text
1. in-memory 메시징 샘플
2. no-op 메시징 샘플
3. DB fallback 이벤트 샘플
4. Kafka/RabbitMQ 확장 기준 설명
5. 알림 룰 등록 샘플
6. 알림 채널 샘플
7. 알림 수신자 샘플
8. 알림 템플릿 샘플
9. 알림 발송 이력 샘플
10. 중복 억제/재시도 샘플
```

## 11.12 보안/JWT/OAuth 샘플

```text
1. 관리자 인증 샘플
2. 관리자 권한 체크 샘플
3. 메뉴 권한 체크 샘플
4. 버튼 권한 체크 샘플
5. 다운로드 권한 체크 샘플
6. 관리자 토큰과 업무 토큰 분리 샘플
7. JWT claim 파싱 샘플
8. OAuth client 공통 유틸 사용 샘플
9. 인증 실패 응답 샘플
10. 인가 실패 응답 샘플
```

## 11.13 배치 샘플

```text
1. Tasklet Job 샘플
2. Chunk Job 샘플
3. 실패/재수행 Job 샘플
4. 영업일만 수행 Job 샘플
5. 수행 가능 시간 조건 Job 샘플
6. 자동 수행 여부 설정 샘플
7. 반복 수행 스케줄 샘플
8. 선행 Job 샘플
9. 후행 Job 샘플
10. Trigger Job 샘플
11. 수행 대상 인스턴스 샘플
12. 수행 시뮬레이션 샘플
13. BATCH_* 적재 확인 샘플
14. pfw_batch_execution 연계 샘플
15. 배치 실패 알림 샘플
16. ADM 배치 상세 조회 샘플
17. Execution 목록 조회 샘플
18. Execution 상세 조회 샘플
19. Step 상세 조회 샘플
20. 재수행/중지 샘플
21. 처리 대상 건수/성공 건수/실패 건수/Skip 건수 확인 샘플
22. readCount/writeCount/filterCount/skipCount/commitCount/rollbackCount 확인 샘플
23. 배치 실행 결과 통계 조회 샘플
```

---

# 12. EDU 샘플 Swagger 정리

모든 EDU 샘플은 Swagger에서 개발자가 쉽게 찾을 수 있어야 합니다.

## 12.1 요구사항

```text
1. EDU 샘플별 Tag를 명확히 분리하세요.
2. summary는 한글로 명확히 작성하세요.
3. description에는 이 샘플을 언제 쓰는지 작성하세요.
4. 요청 파라미터 설명을 작성하세요.
5. 응답 예시 또는 응답 필드 설명을 작성하세요.
6. 오류 샘플은 발생 가능한 오류 코드와 ADM 확인 방법을 설명하세요.
7. Swagger 설명에 FPS 잔재나 깨진 한글이 없어야 합니다.
8. Swagger Tag 순서를 개발 흐름 기준으로 정리하세요.
```

## 12.2 권장 Swagger Tag

```text
XYZ-EDU 01. 기본 온라인 거래
XYZ-EDU 02. 표준 헤더/거래 ID
XYZ-EDU 03. 표준 응답/오류/메시지
XYZ-EDU 04. 트랜잭션/감사 로그
XYZ-EDU 05. 주제영역 간 호출
XYZ-EDU 06. 외부 연동 호출
XYZ-EDU 07. 공통 코드/메시지
XYZ-EDU 08. 로그/추적/마스킹
XYZ-EDU 09. 파일/다운로드
XYZ-EDU 10. 메시징/알림
XYZ-EDU 11. 보안/JWT/OAuth
XYZ-EDU 12. 배치
XYZ-EDU 13. ADM 운영 확인
XYZ-EDU 14. 런타임 로그 레벨
```

---

# 13. EDU 샘플 seed 및 실증 검증

EDU 샘플은 코드만 있으면 안 됩니다.
필요한 seed 데이터와 ADM 확인 경로까지 제공하세요.

## 13.1 필수 seed

```text
1. EDU 공통 코드
2. EDU 공통 메시지
3. EDU 응답 코드
4. EDU 온라인 거래 샘플 데이터
5. EDU 오류 로그 샘플 데이터
6. EDU 외부 연동 샘플 데이터
7. EDU 알림 룰
8. EDU 알림 채널
9. EDU 알림 수신자
10. EDU 알림 템플릿
11. EDU 다운로드 권한
12. EDU 메뉴/버튼 권한
13. EDU 런타임 로그 레벨 관리 권한
14. EDU 배치 Job
15. EDU 배치 스케줄
16. EDU 배치 선행/후행/트리거 관계
17. EDU 배치 수행 대상 인스턴스
```

## 13.2 EDU Batch Job 등록 대상

```text
CPF_EDU_TASKLET_JOB
CPF_EDU_CHUNK_JOB
CPF_EDU_RETRY_JOB
CPF_EDU_BUSINESS_DAY_JOB
CPF_EDU_TRIGGER_JOB
```

## 13.3 검증 요구

```text
1. SQL 설치 후 pfw_batch_job에 EDU Job 존재
2. pfw_batch_schedule에 EDU Job 스케줄 존재
3. pfw_batch_job_relation에 EDU 관계 존재
4. pfw_batch_execution_target에 수행 대상 존재
5. ADM 배치 목록에서 EDU Job 조회 가능
6. ADM에서 EDU Job 수동 실행 가능
7. 실행 후 BATCH_JOB_INSTANCE 적재 확인
8. 실행 후 BATCH_JOB_EXECUTION 적재 확인
9. 실행 후 BATCH_STEP_EXECUTION 적재 확인
10. 실행 후 pfw_batch_execution 연계 확인
11. 실행 후 처리 대상 건수/성공 건수/실패 건수/Skip 건수 확인
12. 실패/재수행 샘플은 실패 상태와 재수행 가능 여부 확인
```

실제 실행하지 못하면 완료로 쓰지 말고 미검증으로 기록하세요.

---

# 14. ADM 배치 관리 완성도 보강

ADM 배치 화면은 단순 JSON 출력 수준이면 안 됩니다.
운영자가 Job → Schedule → Execution → Step → Log → Alert 흐름을 자연스럽게 볼 수 있어야 합니다.

## 14.1 배치 목록

목록 컬럼:

```text
- Job ID
- Job명
- 주제영역
- 사용 여부
- 자동 수행 여부
- 반복 수행 여부
- 스케줄 존재 여부
- 마지막 실행 상태
- 마지막 실행 일시
- 다음 예정 실행 일시
- 마지막 처리 대상 건수
- 마지막 성공 건수
- 마지막 실패 건수
- 마지막 Skip 건수
- 마지막 처리 시간
- 실패 여부
- 알림 발생 여부
- 담당자
```

검색조건:

```text
- Job ID
- Job명
- 주제영역
- 사용 여부
- 자동 수행 여부
- 반복 수행 여부
- 상태
- 오류건만
- 알림 발생건만
- 담당자
```

## 14.2 배치 상세

배치 상세에는 아래 탭 또는 섹션이 있어야 합니다.

```text
1. 기본 정보
2. 스케줄 정보
3. 자동 수행/반복 수행 설정
4. 영업일/수행 가능 시간
5. 선행/후행/트리거 관계
6. 수행 대상 인스턴스
7. Execution 목록
8. 처리 건수/결과 통계
9. 알림 설정
10. 운영 로그
11. 변경 이력
```

## 14.3 Execution 목록

배치 상세 페이지에서 해당 Job의 Execution 목록을 볼 수 있어야 합니다.

```text
- Execution ID
- Spring Batch JobExecution ID
- 업무일자
- 시작일시
- 종료일시
- 상태
- 처리 시간
- 실행 인스턴스
- 대상 건수
- 성공 건수
- 실패 건수
- Skip 건수
- readCount
- writeCount
- filterCount
- commitCount
- rollbackCount
- 실패 Step
- 오류 코드
- 재수행 가능 여부
- 알림 발송 여부
```

Execution row를 클릭하면 Execution 상세로 이동해야 합니다.

## 14.4 Execution 상세

Execution 상세에는 아래 정보가 있어야 합니다.

```text
1. 실행 기본 정보
2. JobParameters
3. Spring Batch JobExecution 정보
4. StepExecution 목록
5. pfw_batch_execution 정보
6. pfw_batch_step_execution 정보
7. 처리 대상 건수
8. 성공 건수
9. 실패 건수
10. Skip 건수
11. readCount/writeCount/filterCount/skipCount
12. commitCount/rollbackCount
13. 실행 로그
14. 오류 로그
15. 알림 발송 이력
16. 재수행/중지 가능 여부
17. 재수행 버튼
18. 중지 버튼
```

## 14.5 Step 상세

Step 상세에는 아래 정보가 있어야 합니다.

```text
- Step ID
- Step명
- 상태
- readCount
- writeCount
- filterCount
- skipCount
- commitCount
- rollbackCount
- readSkipCount
- processSkipCount
- writeSkipCount
- 시작/종료 일시
- 처리 시간
- 오류 메시지
- ExecutionContext 요약
```

## 14.6 자동 수행/반복 수행

아래 기능이 화면/API/DB/문서에서 일치해야 합니다.

```text
1. 자동 수행 여부
2. 수동 수행 가능 여부
3. 반복 수행 여부
4. Cron 표현식
5. 반복 주기
6. 사용 여부
7. 일시정지 여부
8. 다음 실행 예정일시
9. 마지막 실행 결과
10. 자동 수행 제외 사유
```

## 14.7 수행 시뮬레이션

수행 시뮬레이션은 단순 후보일 표시가 아니라 실제 운영 조건을 반영해야 합니다.

```text
1. Cron 표현식 기반 다음 실행일 계산
2. 영업일만 수행 조건 반영
3. 휴일이면 다음 영업일로 이월
4. 휴일이면 이전 영업일로 당김
5. 월초 영업일 수행
6. 월말 영업일 수행
7. 수행 가능 시작/종료 시간 반영
8. 수행 금지 시간 반영
9. 선행 Job 완료 여부 반영
10. 선행 Job 실패 시 제외 사유 표시
11. 트리거 Job 여부 표시
12. 왜 수행/제외되는지 사유 표시
```

---

# 15. ADM 온라인 거래 로그 완성도 보강

온라인 거래 로그는 단순 목록이면 안 됩니다.
운영자가 장애를 추적할 수 있도록 목록 → 상세 → 관련 오류/알림/외부호출/배치 연결까지 볼 수 있어야 합니다.

## 15.1 검색조건

```text
- 조회 기간
- 업무일자
- 모듈
- 주제영역
- transactionId
- globalId
- traceId
- correlationId
- requestId
- spanId
- uuid
- 고객번호
- 회원번호
- 고객번호 해시
- 회원번호 해시
- API URI
- HTTP Method
- HTTP Status
- 상태
- 오류 여부
- 오류 코드
- 처리 시간 기준
- 외부 연계 여부
- 배치 연계 여부
- 알림 발생 여부
- 헤더값 포함
- 파라미터명 포함
- 파라미터값 포함
- 요청 바디 특정 문구 포함
- 응답 바디 특정 문구 포함
```

## 15.2 목록 컬럼

```text
- 거래일시
- transactionId
- globalId
- traceId
- correlationId
- requestId
- uuid
- 모듈
- API URI
- HTTP Method
- 상태
- HTTP Status
- 오류 코드
- 처리 시간
- 외부 호출 수
- 외부 호출 성공 수
- 외부 호출 실패 수
- 관련 오류 로그 수
- 관련 알림 수
- 관련 감사 로그 수
- 고객번호/회원번호 마스킹 값
- 서버 인스턴스
- 알림 발생 여부
```

## 15.3 상세 화면

```text
1. 표준 거래 헤더
2. 요청 헤더
3. 응답 헤더
4. 요청 파라미터
5. 요청 바디 요약
6. 응답 바디 요약
7. 마스킹 적용 결과
8. 오류 상세
9. 외부 연동 호출 목록
10. 외부 호출 성공/실패 건수
11. 관련 오류 로그
12. 관련 알림 이력
13. 관련 감사 로그
14. 관련 배치 Execution
15. 처리 시간 상세
16. 다운로드 버튼
17. 권한별 원문 보기/마스킹 보기 분리
```

민감정보는 기본 마스킹하세요.

```text
- 비밀번호
- 토큰
- 인증번호
- 주민번호
- 카드번호
- 계좌번호 전문
- 개인식별정보
```

---

# 16. ADM 오류 로그 완성도 보강

오류 로그는 단순 에러 목록이 아니라 조치 상태까지 관리할 수 있어야 합니다.

## 16.1 검색조건

```text
- 오류 발생 기간
- 오류 레벨
- 모듈
- 주제영역
- transactionId
- globalId
- traceId
- correlationId
- requestId
- uuid
- 배치 Execution ID
- Spring Batch JobExecution ID
- 고객번호/회원번호
- 오류 코드
- 예외 클래스명
- 오류 메시지 키워드
- API URI
- HTTP Status
- 알림 발송 여부
- 재처리 여부
- 처리 상태
```

## 16.2 처리 상태

```text
- 미처리
- 확인
- 조치중
- 조치완료
- 보류
```

## 16.3 상세 화면

```text
1. 오류 기본 정보
2. 관련 거래 정보
3. 관련 배치 정보
4. 관련 알림 정보
5. stack trace 요약
6. stack trace 상세
7. 조치 상태 변경
8. 조치 메모
9. 담당자 지정
10. 다운로드
```

stack trace 상세는 권한이 있는 사용자만 볼 수 있게 하세요.

---

# 17. ADM 알림/노티 완성도 보강

알림/노티는 테이블만 있으면 안 됩니다.
ADM에서 룰/채널/수신자/템플릿/이력을 관리할 수 있어야 합니다.

## 17.1 기능

```text
1. 알림 룰 목록 조회
2. 알림 룰 등록/수정/삭제
3. 알림 채널 관리
4. 수신자/수신 그룹 관리
5. 알림 템플릿 관리
6. 발송 이력 조회
7. 발송 실패 이력 조회
8. 중복 억제 정책 관리
9. 재시도 정책 관리
10. 배치 실패/지연/미수행과 알림 연계
11. 온라인 거래 오류와 알림 연계
12. 외부 API 오류와 알림 연계
13. 알림 발송 시뮬레이션
```

## 17.2 알림 대상

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
```

---

# 18. ADM 권한 기반 다운로드 완성

ADM의 모든 목록성 화면, 로그 화면, 이력 화면에는 권한에 따라 Excel/CSV/로그 다운로드 기능을 제공하세요.

## 18.1 대상 화면

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
- 런타임 로그 레벨 변경 이력
```

## 18.2 권한 기준

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

## 18.3 정책

```text
1. 현재 검색조건 그대로 다운로드
2. 검색조건 없는 전체 다운로드 제한
3. 다운로드 사유 입력
4. 기본 마스킹 다운로드
5. 마스킹 해제 다운로드는 별도 권한 필요
6. 대용량 다운로드 최대 건수 제한
7. 다운로드 실패도 감사 로그 저장
8. 토큰/비밀번호/인증번호/주민번호/카드번호는 강제 제외 또는 마스킹
```

## 18.4 다운로드 감사 로그 저장 항목

```text
- download_id
- admin_id
- menu_id
- screen_id
- download_type
- target_type
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
- status
- failure_reason
- file_name
```

---

# 19. ADM 런타임 실시간 로그 레벨 변경 기능

운영 중 장애 분석을 위해 ADM에서 런타임 로그 레벨을 변경할 수 있어야 합니다.

이 기능은 배치와 온라인 거래 모두에 적용되어야 합니다.
서버 재기동 없이 특정 모듈, 패키지, 클래스, logger name 단위로 로그 레벨을 임시 변경할 수 있어야 합니다.

## 19.1 기능 요구사항

```text
1. 현재 로그 레벨 목록 조회
2. logger name 기준 로그 레벨 조회
3. 모듈별 로그 레벨 조회
4. 패키지별 로그 레벨 조회
5. 클래스별 로그 레벨 조회
6. 로그 레벨 변경
7. 로그 레벨 원복
8. TTL 기반 임시 로그 레벨 설정
9. 만료 시 자동 원복
10. 변경 이력 조회
11. 변경 사유 입력
12. 변경자/변경일시/대상/이전레벨/변경레벨 기록
13. 권한 없는 관리자 변경 차단
14. 운영 환경에서 DEBUG/TRACE 장시간 유지 방지
15. 배치 Job 실행 중 로그 레벨 변경 반영
16. 온라인 거래 처리 중 로그 레벨 변경 반영
```

## 19.2 지원 로그 레벨

```text
TRACE
DEBUG
INFO
WARN
ERROR
OFF
```

## 19.3 ADM 화면

ADM에 아래 화면 또는 섹션을 추가하세요.

```text
1. 로그 레벨 관리 목록
2. 현재 logger 목록
3. logger name 검색
4. 모듈/패키지/클래스 검색
5. 로그 레벨 변경 팝업
6. 변경 사유 입력
7. TTL 설정
8. 변경 이력
9. 원복 버튼
10. 권한 부족 시 비활성 처리 및 사유 표시
```

## 19.4 API

예시 API는 아래 형태를 참고하세요. 실제 프로젝트 규칙에 맞게 조정하세요.

```text
GET /adm/api/runtime-log-levels
GET /adm/api/runtime-log-levels/history
POST /adm/api/runtime-log-levels
POST /adm/api/runtime-log-levels/{id}/reset
POST /adm/api/runtime-log-levels/reset-expired
```

## 19.5 DB/감사 로그

로그 레벨 변경 이력을 DB에 남기세요.

필수 항목:

```text
- log_level_change_id
- admin_id
- target_type
- module_code
- logger_name
- package_name
- class_name
- before_level
- after_level
- reason
- ttl_seconds
- expire_at
- changed_at
- reverted_at
- status
- client_ip
- user_agent
```

## 19.6 EDU 샘플

EDU에 아래 샘플을 추가하세요.

```text
1. 현재 로그 레벨 조회 샘플
2. 특정 logger DEBUG 변경 샘플
3. TTL 만료 후 원복 샘플
4. 배치 실행 중 로그 레벨 변경 샘플
5. 온라인 거래 처리 중 로그 레벨 변경 샘플
6. 변경 이력 조회 샘플
7. 권한 부족 오류 샘플
```

## 19.7 한글 가이드 문서

`specs/관리자_가이드.html`와 `specs/개발_가이드.html`에 아래 내용을 포함하세요.

```text
1. 런타임 로그 레벨 변경 기능 목적
2. 운영 사용 시 주의사항
3. 권한 정책
4. TTL 정책
5. 변경 이력 확인 방법
6. 배치 장애 분석 시 사용 방법
7. 온라인 거래 장애 분석 시 사용 방법
8. EDU 샘플 사용 방법
9. 관련 API
10. 관련 DB 테이블
```

---

# 20. Swagger/OpenAPI 실제 검증

설정만 보고 완료 처리하지 말고 앱을 실제로 기동해서 확인하세요.

검증 대상:

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

모든 신규 EDU 샘플 API는 Swagger에서 확인 가능해야 합니다.

---

# 21. ADM 브라우저 클릭 검증

코드만 보고 완료 처리하지 말고 브라우저에서 실제 클릭 검증하세요.

검증 대상:

```text
1. 로그인
2. EDU API Swagger 확인
3. 배치 메뉴 진입
4. EDU Batch Job 조회
5. EDU Batch Job 수동 실행
6. 반복 수행/자동 수행 설정 확인
7. 배치 상세에서 Execution 목록 확인
8. Execution 상세 확인
9. Step 상세 확인
10. 배치 처리 건수/수량/결과 통계 확인
11. 수행 시뮬레이션
12. 선행 Job/트리거 Job 설정
13. 관계 구조도 조회
14. 수행 대상 인스턴스 조회
15. 온라인 거래 조회
16. 온라인 거래 상세 조회
17. 온라인 거래 외부호출 수/오류 수/알림 수 확인
18. 오류 로그 조회
19. 오류 로그 상세/조치 상태 변경
20. 알림 룰 등록/수정
21. 알림 발송 이력 조회
22. Excel 다운로드
23. CSV 다운로드
24. 로그 다운로드
25. 다운로드 권한 통제
26. 런타임 로그 레벨 조회
27. 런타임 로그 레벨 변경
28. 런타임 로그 레벨 원복
29. 버튼 권한/비활성 사유 표시
```

브라우저 검증을 못 하면 완료로 쓰지 말고 미검증으로 기록하세요.

---

# 22. 테스트 / qualityGate 신뢰도 회복

테스트가 비활성화되어 있거나 SKIPPED/NO-SOURCE가 많다면 정리하세요.

## 22.1 검색 대상

```text
enabled = false
tasks.withType(Test)
configureEach { enabled = false }
```

## 22.2 필수 테스트

```text
1. EDU Batch Job bean loading test
2. EDU Tasklet Job 실행 test
3. EDU Chunk Job 실행 test
4. Batch JobRepository 적재 test
5. pfw_batch_execution 연계 test
6. 배치 처리 건수/수량/결과 통계 적재 test
7. ADM Batch API slice/context test
8. ADM Transaction Search API test
9. ADM Error Search API test
10. ADM Notification API test
11. ADM Runtime Log Level API test
12. Download permission policy test
13. Code/message sample test
14. Transaction separation sample test
15. External call sample mock test
16. Standard error/message sample test
17. FPS 잔재 검색 test 또는 check task
18. mojibake 검색 test 또는 check task
```

실 DB가 필요한 테스트는 기본 unit test와 분리하고 명시적으로 실행되게 하세요.

---

# 23. 검증 명령

가능한 범위에서 아래 명령을 실행하고 리포트에 결과를 기록하세요.

```powershell
.\gradlew.bat checkUtf8
.\gradlew.bat checkMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
.\gradlew.bat clean :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline
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
7. 배치 처리 건수/수량/결과 통계 적재 확인
8. ADM 화면 클릭 검증
9. Runtime Log Level API 검증
10. FPS 잔재 전수 검색
11. mojibake 전수 검색
```

실행하지 못한 항목은 미검증으로 기록하세요.

---

# 24. 최종 리포트 필수 체크리스트

`CPF_STABILIZATION_REPORT.md`에는 아래 체크리스트를 포함하세요.

```markdown
## CPF 완성도 보강 체크리스트

| 번호 | 항목 | 상태 | 증빙 | 비고 |
|---|---|---|---|---|
| 1 | FPS/Fps/fps 잔재 전수 검색 | 완료/일부/실패/미검증 |  |  |
| 2 | FPS/Fps/fps 잔재 제거 | 완료/일부/실패/미검증 |  |  |
| 3 | legacy alias 잔존 사유 문서화 | 완료/일부/실패/미검증 |  |  |
| 4 | mojibake 전수 검색 | 완료/일부/실패/미검증 |  |  |
| 5 | mojibake 전수 제거 | 완료/일부/실패/미검증 |  |  |
| 6 | check-utf8/checkMojibake 강화 | 완료/일부/실패/미검증 |  |  |
| 7 | Java/JS/SQL/MD/YAML/HTML 포맷 정상화 | 완료/일부/실패/미검증 |  |  |
| 8 | specs 문서 파편화 통합 | 완료/일부/실패/미검증 |  |  |
| 9 | 기존 한글 가이드 문서 정본화 | 완료/일부/실패/미검증 |  |  |
| 10 | 기능 구현 매트릭스 작성 | 완료/일부/실패/미검증 |  |  |
| 11 | deprecated 문서 표시 | 완료/일부/실패/미검증 |  |  |
| 12 | PFW/CMN/ADM/주제영역 책임 정리 | 완료/일부/실패/미검증 |  |  |
| 13 | 기본 온라인 거래 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 14 | 표준 헤더/거래 ID EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 15 | 트랜잭션 분리 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 16 | 주제영역 간 호출 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 17 | 외부 연동 호출 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 18 | 입력/세션/요청 컨텍스트 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 19 | 공통 코드/메시지 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 20 | 표준 예외/오류 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 21 | 로그/감사/추적 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 22 | 파일/다운로드 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 23 | 메시징/알림 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 24 | 보안/JWT/OAuth EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 25 | 배치 EDU 샘플 | 완료/일부/실패/미검증 |  |  |
| 26 | EDU 샘플 Swagger 정리 | 완료/일부/실패/미검증 |  |  |
| 27 | EDU seed 데이터 보강 | 완료/일부/실패/미검증 |  |  |
| 28 | EDU Batch Job seed 등록 | 완료/일부/실패/미검증 |  |  |
| 29 | EDU Batch Job 실제 실행 | 완료/일부/실패/미검증 |  |  |
| 30 | BATCH_JOB_INSTANCE 적재 | 완료/일부/실패/미검증 |  |  |
| 31 | BATCH_JOB_EXECUTION 적재 | 완료/일부/실패/미검증 |  |  |
| 32 | BATCH_STEP_EXECUTION 적재 | 완료/일부/실패/미검증 |  |  |
| 33 | pfw_batch_execution 연계 | 완료/일부/실패/미검증 |  |  |
| 34 | 배치 처리 건수/수량/결과 통계 | 완료/일부/실패/미검증 |  |  |
| 35 | ADM 배치 목록 완성 | 완료/일부/실패/미검증 |  |  |
| 36 | ADM 배치 상세 완성 | 완료/일부/실패/미검증 |  |  |
| 37 | Execution 목록/상세/Step 상세 | 완료/일부/실패/미검증 |  |  |
| 38 | 자동 수행/반복 수행 설정 | 완료/일부/실패/미검증 |  |  |
| 39 | 수행 시뮬레이션 고도화 | 완료/일부/실패/미검증 |  |  |
| 40 | 선행/후행/Trigger Job 관리 | 완료/일부/실패/미검증 |  |  |
| 41 | 수행 대상 인스턴스 관리 | 완료/일부/실패/미검증 |  |  |
| 42 | 온라인 거래 조회/상세 완성 | 완료/일부/실패/미검증 |  |  |
| 43 | 온라인 거래 검색조건 고도화 | 완료/일부/실패/미검증 |  |  |
| 44 | 온라인 거래 외부호출 수/오류 수/알림 수 표시 | 완료/일부/실패/미검증 |  |  |
| 45 | 오류 로그 조회/상세/조치 상태 | 완료/일부/실패/미검증 |  |  |
| 46 | 알림 룰/채널/수신자/템플릿 관리 | 완료/일부/실패/미검증 |  |  |
| 47 | 알림 발송 이력 조회 | 완료/일부/실패/미검증 |  |  |
| 48 | Excel/CSV/로그 다운로드 | 완료/일부/실패/미검증 |  |  |
| 49 | 다운로드 권한/마스킹/감사 로그 | 완료/일부/실패/미검증 |  |  |
| 50 | ADM 런타임 로그 레벨 변경 | 완료/일부/실패/미검증 |  |  |
| 51 | Runtime Log Level API/이력/권한 | 완료/일부/실패/미검증 |  |  |
| 52 | Swagger 실제 호출 검증 | 완료/일부/실패/미검증 |  |  |
| 53 | ADM 브라우저 클릭 검증 | 완료/일부/실패/미검증 |  |  |
| 54 | qualityGate 성공 | 완료/일부/실패/미검증 |  |  |
| 55 | 문서/소스/SQL/Swagger/EDU/ADM 정합성 | 완료/일부/실패/미검증 |  |  |
```

---

# 25. 완료 기준

이번 작업은 아래 조건을 충족해야 완료로 볼 수 있습니다.

```text
1. FPS/Fps/fps 잔재가 전수 검색되고 제거 또는 사유 문서화되어야 합니다.
2. mojibake/깨진 한글이 전수 검색되고 제거되어야 합니다.
3. 한 줄로 몰린 핵심 소스/SQL/문서가 정상 포맷으로 정리되어야 합니다.
4. specs 문서가 기존 한글 가이드 문서 중심으로 통합되어야 합니다.
5. 기능 구현 매트릭스가 작성되어야 합니다.
6. 한글 가이드 문서가 실제 구현 검수 기준으로 사용할 수 있을 정도로 상세해야 합니다.
7. PFW/CMN/ADM/주제영역 책임이 문서와 소스 구조에서 일치해야 합니다.
8. EDU 샘플이 배치뿐 아니라 온라인 거래, 외부연동, 트랜잭션, 메시지, 코드, 로그, 다운로드, 보안까지 기능별로 정리되어야 합니다.
9. 각 EDU 샘플은 Swagger에서 확인 가능해야 합니다.
10. 각 EDU 샘플은 개발 가이드와 기능 구현 매트릭스에 연결되어야 합니다.
11. EDU Batch Job은 실제 메타에 등록되고 실행 검증되어야 합니다.
12. Spring Batch BATCH_* 테이블 적재가 확인되어야 합니다.
13. 배치 처리 대상 건수, 성공 건수, 실패 건수, Skip 건수, read/write/filter/commit/rollback 수량이 확인되어야 합니다.
14. ADM 배치 상세에서 Execution 목록과 상세, Step 상세를 볼 수 있어야 합니다.
15. 자동 수행/반복 수행/수행 가능 시간/영업일/선행 Job 조건이 배치 관리에 반영되어야 합니다.
16. ADM 온라인 거래 상세에서 표준 헤더, 요청/응답 요약, 오류/알림/외부호출/배치 연계를 볼 수 있어야 합니다.
17. 온라인 거래 목록/상세에서 외부호출 수, 오류 수, 알림 수, 감사 로그 수 등 운영 추적 지표를 볼 수 있어야 합니다.
18. ADM 오류 로그는 조치 상태와 담당자/메모를 관리할 수 있어야 합니다.
19. ADM 다운로드는 권한, 마스킹, 사유, 감사 로그까지 포함해야 합니다.
20. 알림은 룰/채널/수신자/템플릿/발송이력까지 관리 가능해야 합니다.
21. ADM에서 런타임 로그 레벨을 조회/변경/원복하고 이력을 확인할 수 있어야 합니다.
22. Runtime Log Level 기능은 배치와 온라인 거래 장애 분석에 모두 사용할 수 있어야 합니다.
23. Swagger는 실제 URL 호출로 확인되어야 합니다.
24. ADM 화면은 실제 클릭 검증 결과가 있어야 합니다.
25. qualityGate와 필수 검증 명령 결과가 리포트에 있어야 합니다.
26. 실패/미검증 항목은 숨기지 않고 리포트에 남겨야 합니다.
```

이번 작업은 CPF 프레임워크 완성도를 높이는 작업입니다.
개발자가 “이 샘플을 보고 실무 기능을 만들 수 있다”고 판단할 수 있는 수준으로 정리하세요.
운영자가 “ADM 화면에서 배치, 온라인 거래, 오류, 알림, 다운로드, 런타임 로그 레벨을 실제로 추적하고 조치할 수 있다”고 판단할 수 있는 수준으로 보강하세요.
