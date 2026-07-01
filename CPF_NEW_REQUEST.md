CPF_M1_REQUEST_20260701_01 — ADM Runtime / OpenAPI Smoke 실제 실행 요청

## 0. CPF 최종 목표

CPF(CoreFlow Platform Framework)의 최종 목표는 금융권을 포함한 범용 업무 시스템에서 실제 프로젝트에 재사용 가능한 운영형 공통 프레임워크를 완성하는 것이다.

최종 완성 상태는 단순 소스 존재나 문서 작성이 아니다. 아래가 실제 구현, 테스트, runtime 검증, 운영 문서까지 연결되어야 한다.

1. 표준 헤더 수신, 검증, 거래 context 생성, 로그 저장, ADM 조회, outbound 자동 전파까지 E2E로 검증된다.
2. PFW, CMN, ADM, BIZADM, MBR, EXS, BAT, EDU/XYZ 모듈이 각 역할에 맞게 구현된다.
3. ADM은 단순 API 모음이 아니라 거래 로그, 오류 로그, 감사 로그, 권한, 메뉴, 배치, 캐시, 정책, 보안 상태를 확인하고 조치할 수 있는 운영 콘솔이 된다.
4. BAT는 독립 실행 가능한 batch worker/application으로 job, step, lock, heartbeat, ghost 감지, 재실행, 중지, 운영자 조치, 감사 로그까지 지원한다.
5. EDU/XYZ는 개발자가 복사해서 신규 기능을 만들 수 있는 실전형 표준 예제가 된다.
6. DB, Flyway, split SQL, all_install, smoke SQL이 서로 불일치하지 않고 신규 빈 MariaDB 설치 기준으로 검증된다.
7. Gradle test, qualityGate, smoke script, runtime API, OpenAPI, 브라우저 클릭, 표준 헤더 E2E가 실제 실행 결과 기준으로 검증된다.
8. README, 개발가이드, 운영가이드, 표준헤더가이드, DB/SQL가이드, ADM/BAT/EDU 가이드는 최종 단계에서 실제 구현과 일치하도록 정본화된다.
9. 실행하지 않은 검증, 미구현, 미검증, 실패 항목은 완료로 포장하지 않고 `CPF_STABILIZATION_REPORT.html`과 기능 매트릭스에 정확히 남긴다.
10. 최종적으로 “문서상 프레임워크”가 아니라 실제 실행, 운영, 검증 가능한 CPF 120% 품질 상태를 만든다.

## 1. 이번 작업 목적

이번 작업의 목적은 CPF 최종 목표 중 **ADM runtime / OpenAPI 검증 단계에 진입**하는 것이다.

직전 작업에서 표준 헤더 Source-Level 테스트가 추가되었으므로, 이번에는 그 상태를 과장 없이 정리하고 ADM 애플리케이션이 실제로 기동되는지, OpenAPI와 주요 ADM API가 runtime에서 호출 가능한지 확인한다.

이번 작업은 문서 포맷 정리가 아니라, CPF가 정적 소스/문서 수준을 넘어 실제 실행 검증으로 이동하기 위한 M1 마일스톤이다.

이번 작업의 핵심은 다음이다.

1. ADM 모듈 bootJar 또는 bootRun 실제 시도
2. ADM 앱 기동 성공/실패 확인
3. OpenAPI runtime 접근 확인
4. 주요 ADM API smoke 대상 path를 실제 Controller 기준으로 확인
5. 가능한 smoke script 실행
6. 성공/실패/미검증을 분리 기록
7. 실행하지 않은 검증을 완료로 기록하지 않기
8. 다음 단계인 ADM browser click 또는 표준 헤더 Runtime E2E로 넘어갈 수 있는 근거 확보

## 2. 필수 제한

아래는 반드시 지킨다.

* Git commit 금지
* Git push 금지
* branch 생성 금지
* 민감정보 원문 기록 금지
* 실행하지 않은 검증을 완료로 기록 금지
* `CPF_STABILIZATION_CHANGED_FILES.txt` 같은 별도 변경파일 목록 산출물 생성 금지

그 외에는 이번 목적 달성을 위해 필요한 소스, 테스트, 스크립트, 리포트, 기능 매트릭스의 최소 수정은 Codex가 판단해서 수행한다.

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래 명령을 실행하고 결과를 `CPF_STABILIZATION_REPORT.html`에 기록한다.

```powershell
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

기록 시 아래를 구분한다.

* 작업 시작 branch
* 작업 시작 HEAD SHA
* 작업 시작 origin/master SHA
* 작업 시작 status
* 사용자 요청 원본 파일이 수정 상태인지 여부
* 이번 작업 대상 파일인지 여부

`CPF_NEW_REQUEST.md`가 수정 상태라면 사용자 요청 원본으로 보고 작업 대상에서 제외한다.

## 4. 표준 헤더 Source-Level 상태 정리

직전 작업에서 추가된 표준 헤더 테스트 상태를 확인한다.

대상 후보:

```text
pfw/src/test/java/cpf/pfw/common/header/CpfStandardHeaderE2eTest.java
pfw/src/main/java/cpf/pfw/common/header/**
scripts/check-feature-evidence.ps1
CPF_STABILIZATION_REPORT.html
specs/기능_구현_매트릭스.html
```

확인할 내용:

```text
1. CpfStandardHeaderE2eTest.java 존재 여부
2. 테스트가 실제로 검증하는 항목
3. 테스트가 검증하지 않는 항목
4. check-feature-evidence.ps1가 표준 헤더 핵심 파일과 테스트를 확인하는지 여부
5. 기능 매트릭스가 Source-Level 검증과 Runtime E2E를 분리하는지 여부
```

상태 기록 기준:

```text
PFW 표준 헤더 Source-Level 흐름 테스트: 완료 또는 부분 구현
표준 헤더 Runtime E2E: 미검증
ADM 로그 연계 검증: 미검증
실제 outbound HTTP 수신 검증: 미검증
```

주의:

* Source-Level 테스트를 Runtime E2E 완료로 기록하지 않는다.
* ADM runtime, DB 로그 저장, 브라우저 클릭, broker 검증을 이 테스트로 완료 처리하지 않는다.
* 테스트 파일에 포맷/주석 문제가 있더라도 검증을 방해하는 수준이면 Codex가 최소 보정한다. 단순 문서 미려화로 작업을 키우지 않는다.

## 5. ADM bootJar / bootRun 실제 시도

ADM 모듈이 실행 가능한지 먼저 확인한다.

우선 실행:

```powershell
.\gradlew.bat :adm:bootJar --offline --no-daemon --console=plain
```

bootJar 성공 시 jar 파일을 확인한다.

```powershell
Get-ChildItem .\adm\build\libs\
```

가능하면 ADM 앱을 실제 기동한다.

예시:

```powershell
java -jar .\adm\build\libs\<ADM_JAR_NAME>.jar
```

또는 프로젝트 구조상 bootRun이 가능하면 아래도 시도한다.

```powershell
.\gradlew.bat :adm:bootRun --offline --no-daemon --console=plain
```

기록할 내용:

```text
실행 명령
성공/실패
사용 profile
사용 port
실패 시 exception 요약
DB/Redis/환경변수 등 필요한 조건
timeout 여부
```

판정 기준:

* 앱이 실제로 떠서 HTTP 요청을 받을 수 있으면 ADM runtime smoke 진행
* bootJar 실패면 실패로 기록하고 원인 정리
* 앱 기동에 환경 조건이 필요하면 미검증 또는 재확인 필요로 기록
* 앱을 띄우지 못했는데 runtime 완료로 기록하지 않는다

## 6. OpenAPI runtime 접근 확인

ADM 앱이 실제 기동된 경우 OpenAPI 접근을 확인한다.

후보 URL:

```text
http://localhost:8080/v3/api-docs
http://localhost:8080/swagger-ui/index.html
http://localhost:8080/swagger-ui.html
```

PowerShell 예시:

```powershell
Invoke-WebRequest -Uri http://localhost:8080/v3/api-docs -UseBasicParsing
Invoke-WebRequest -Uri http://localhost:8080/swagger-ui/index.html -UseBasicParsing
```

기록할 내용:

```text
URL
HTTP status
응답 일부
성공/실패
401/403/404/500/timeout 여부
인증 필요 여부
```

완료 기준:

* `/v3/api-docs` 또는 Swagger UI가 실제 HTTP 성공 응답을 반환해야 OpenAPI runtime 완료로 기록한다.
* 설정 파일 존재만으로 OpenAPI runtime 성공으로 기록하지 않는다.

## 7. ADM 주요 API smoke 대상 확인

실제 Controller 파일을 열어 API path를 확인한다. 추정으로 작성하지 않는다.

확인 대상 후보:

```text
adm/src/main/java/**/controller/**/*.java
adm/src/main/java/**/controller/*.java
adm/src/main/java/**/web/**/*.java
adm/src/main/java/**/api/**/*.java
```

우선 확인할 기능:

```text
ADM health 또는 actuator
거래 로그 목록
거래 로그 상세
오류 로그 목록
감사 로그 목록
로그 정책 조회
배치 목록
캐시 상태 조회
운영자/권한/메뉴 관련 기본 API
```

리포트 기록 형식:

```text
기능명
Controller 파일
HTTP method
path
필요 인증/헤더
실행 여부
HTTP status
결과
미검증 사유
```

필요하면 smoke script를 최소 수정하거나 보강한다.

대상 후보:

```text
scripts/smoke-adm-runtime.ps1
scripts/smoke-openapi.ps1
scripts/smoke-log-policy-runtime.ps1
```

## 8. ADM runtime smoke 실행

ADM 앱이 기동되고 API path가 확인되면 smoke를 실행한다.

예시:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-log-policy-runtime.ps1
```

성공 기록에 필요한 내용:

```text
실행 명령
대상 URL
HTTP status
응답 확인 기준
성공 API 목록
실패 API 목록
미검증 API 목록
```

실패 기록에 필요한 내용:

```text
실패 명령
실패 URL
HTTP status 또는 exception
timeout 여부
원인 추정
다음 조치
완료로 기록하지 않은 이유
```

## 9. 기본 테스트 / 품질 게이트

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline
.\gradlew.bat :adm:test --offline
.\gradlew.bat :xyz:test --offline
.\gradlew.bat test --offline --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

주의:

* 실행하지 못한 명령은 성공으로 기록하지 않는다.
* timeout은 timeout으로 기록한다.
* 재실행한 경우 최초 실패와 재실행 결과를 분리한다.
* `check-html-docs.ps1`가 문서 포맷 문제만으로 실패하면 포맷 대수정에 들어가지 말고 실패 사유를 기록한다.
* 상태값 과장, evidence 불일치, 실행/미검증 오기 때문에 실패하면 그 부분은 수정한다.

## 10. 상태값 기준

상태값은 아래 6개만 사용한다.

```text
완료
부분 구현
미구현
미검증
실패
재확인 필요
```

이번 작업 권장 판정:

```text
PFW 표준 헤더 Source-Level 흐름 테스트: 완료 또는 부분 구현
표준 헤더 Runtime E2E: 미검증
ADM runtime smoke: 실제 기동 및 API smoke 성공 시 완료, 일부 성공 시 부분 구현, 기동 실패 시 실패, 환경 미충족 시 미검증 또는 재확인 필요
OpenAPI runtime: 실제 /v3/api-docs 접근 성공 시 완료, 아니면 실패/미검증/재확인 필요
ADM browser click: 미검증
MariaDB 전체 신규 설치: 미검증
Redis/Kafka/MQ broker: 미검증
```

## 11. `CPF_STABILIZATION_REPORT.html` 기록 기준

리포트는 내부 검수용이다. 포맷보다 내용 정확성이 중요하다.

반드시 기록할 내용:

```text
1. 기준 정보
2. 변경 파일
3. 표준 헤더 Source-Level 테스트 상태
4. Source-Level 테스트가 검증한 것
5. Source-Level 테스트가 검증하지 않은 것
6. ADM bootJar/bootRun 시도 결과
7. ADM 앱 기동 성공/실패/미검증 사유
8. OpenAPI 접근 시도 결과
9. ADM API smoke 대상과 실행 결과
10. 실패/미검증 항목
11. 다음 작업 후보
```

문서 수정 기준:

* 내용 오류는 수정한다.
* 상태값 과장은 수정한다.
* evidence 경로 오류는 수정한다.
* 실행하지 않은 검증을 완료로 쓴 경우 수정한다.
* 문서 포맷/디자인/문장 품질 정본화는 이번 작업의 목표가 아니다.

## 12. 완료 기준

아래가 충족되어야 이번 작업을 완료로 본다.

```text
1. CpfStandardHeaderE2eTest 존재와 검증 범위가 리포트/매트릭스/evidence에 반영됨
2. Source-Level 테스트와 Runtime E2E가 분리됨
3. ADM bootJar 또는 bootRun을 실제 시도함
4. ADM 앱 기동 성공/실패/미검증 사유가 기록됨
5. OpenAPI runtime 접근을 실제 시도했거나, 시도하지 못한 사유가 기록됨
6. 주요 ADM API smoke 대상이 실제 Controller 기준으로 정리됨
7. 가능한 smoke script를 실제 실행했거나, 실행하지 못한 사유가 기록됨
8. 기본 Gradle/check 명령 결과가 기록됨
9. 리포트와 기능 매트릭스 상태값이 내용 기준으로 일치함
10. 실행하지 않은 browser/DB/broker 검증은 미검증으로 남김
11. Git commit / push / branch 생성 없음
```

## 13. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
ADM 앱을 기동하지 않았는데 ADM runtime 완료 기록
/v3/api-docs 접근 없이 OpenAPI runtime 완료 기록
Controller를 열지 않고 smoke path 작성
API 404/500/timeout을 성공 처리
표준 헤더 Source-Level 테스트를 Runtime E2E 완료로 기록
DB 로그 저장 확인 없이 로그 E2E 완료 기록
브라우저 클릭 없이 ADM browser click 완료 기록
broker 미검증을 완료 기록
리포트와 기능 매트릭스 상태 불일치
실행하지 않은 명령을 성공으로 기록
민감정보 원문을 리포트에 기록
Git commit / push / branch 생성 수행
CPF_STABILIZATION_CHANGED_FILES.txt 생성
```

## 14. 완료 보고 양식

완료 보고는 아래 양식으로 작성한다.

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- GitHub master 확인 가능 여부:

2. 표준 헤더 Source-Level 상태
- 확인한 테스트 파일:
- 검증한 항목:
- 검증하지 않은 항목:
- evidence 반영 여부:
- 기능 매트릭스 상태:

3. ADM runtime 기동
- 실행 명령:
- 결과:
- 포트:
- profile:
- 실패/미검증 사유:

4. OpenAPI runtime
- URL:
- HTTP status:
- 결과:
- 실패/미검증 사유:

5. ADM API smoke
- 확인한 Controller:
- 확인한 path:
- 실행한 smoke script:
- 성공 API:
- 실패 API:
- 미검증 API:

6. 실행 명령 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- timeout/재실행 여부:

7. 남은 미검증
- ADM browser click:
- 표준 헤더 Runtime E2E:
- DB log 저장/조회:
- MariaDB full install:
- Redis/Kafka/MQ broker:

8. 다음 추천 작업
- ADM browser click 가능 여부 확인
- 표준 헤더 Runtime E2E
- BAT ghost/center-cut
```

## 15. 다음 마일스톤 분기

이번 작업 결과에 따라 다음을 결정한다.

### ADM runtime/OpenAPI가 성공한 경우

```text
M2 — ADM browser click 가능 여부 확인 및 Playwright/Selenium smoke 준비
M3 — 표준 헤더 Runtime E2E: 실제 API 호출 → 로그 저장 → ADM 조회 → outbound 확인
```

### ADM runtime/OpenAPI가 실패한 경우

```text
M1-REPAIR — ADM 기동 실패 원인 수정
```

### ADM runtime이 환경 문제로 미검증인 경우

```text
M1-ENV — 필요한 profile, DB, port, 계정, 환경변수 조건 정리
```
