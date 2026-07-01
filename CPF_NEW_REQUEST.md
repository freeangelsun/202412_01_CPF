CPF_M2_REQUEST_20260701_02 — ADM Browser Click Smoke 가능 여부 확인 및 자동화 검증 요청

## 0. CPF 최종 목표

CPF(CoreFlow Platform Framework)의 최종 목표는 금융권을 포함한 범용 업무 시스템에서 실제 프로젝트에 재사용 가능한 운영형 공통 프레임워크를 완성하는 것이다.

최종 완성 상태는 단순 소스 존재나 문서 작성이 아니다. 아래가 실제 구현, 테스트, runtime 검증, 운영 문서까지 연결되어야 한다.

1. 표준 헤더 수신, 검증, 거래 context 생성, 로그 저장, ADM 조회, outbound 자동 전파까지 E2E로 검증된다.
2. PFW, CMN, ADM, BIZADM, MBR, EXS, BAT, EDU/XYZ 모듈이 각 역할에 맞게 구현된다.
3. ADM은 거래 로그, 오류 로그, 감사 로그, 권한, 메뉴, 버튼, API 권한, 배치, 캐시, 정책, 보안 상태를 확인하고 조치할 수 있는 운영 콘솔이 된다.
4. BAT는 독립 실행 가능한 worker/application으로 job, step, lock, heartbeat, ghost 감지, 재실행, 중지, 운영자 조치, 감사 로그를 지원한다.
5. EDU/XYZ는 개발자가 복사해 신규 기능을 만들 수 있는 실전형 표준 예제가 된다.
6. DB, Flyway, split SQL, all_install, smoke SQL이 정합성을 갖고 신규 빈 MariaDB 기준으로 검증된다.
7. Gradle test, qualityGate, runtime API, OpenAPI, 브라우저 클릭, 표준 헤더 E2E는 실제 실행 결과 기준으로 기록된다.
8. 실행하지 않은 검증, 미구현, 미검증, 실패 항목은 완료로 포장하지 않고 리포트와 기능 매트릭스에 정확히 남긴다.

## 1. 이번 작업 목적

이번 작업의 목적은 M1에서 ADM runtime / OpenAPI smoke가 통과한 상태를 바탕으로, ADM 운영 콘솔이 실제 브라우저에서 접근·이동·조회 가능한지 검증하는 것이다.

단, Codex 환경에서 GUI나 브라우저 자동화가 항상 가능한 것은 아니므로, 이번 작업은 아래 순서로 진행한다.

1. 브라우저 자동화 가능 여부 확인
2. 가능하면 Playwright 또는 Selenium 기반 ADM UI smoke 작성
3. ADM 앱 기동 후 실제 브라우저 자동화로 URL 접속, 메뉴 이동, 화면 검증 수행
4. screenshot, trace, log 등 증거 산출
5. 불가능하면 완료로 포장하지 않고 미검증 사유와 필요한 환경 조건 기록

이번 작업의 핵심은 “ADM UI 검증 완료”라는 주장 자체가 아니라, **Codex 환경에서 재현 가능한 브라우저 클릭 검증 체계를 만들고 실제 가능한 범위까지 실행하는 것**이다.

## 2. 필수 제한

아래는 반드시 지킨다.

* Git commit 금지
* Git push 금지
* branch 생성 금지
* 민감정보 원문 기록 금지
* 실행하지 않은 검증을 완료로 기록 금지
* 별도 변경파일 목록 산출물 생성 금지
* 브라우저 클릭을 하지 않았는데 ADM browser click 완료로 기록 금지

그 외에는 이번 목적 달성을 위해 필요한 소스, 테스트, 스크립트, 설정, 리포트, 기능 매트릭스의 최소 수정은 Codex가 판단해서 수행한다.

## 3. 작업 시작 전 기준 기록

작업 시작 전에 아래 명령을 실행하고 `CPF_STABILIZATION_REPORT.html`에 기록한다.

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
* 이번 작업에서 의도적으로 수정한 파일
* 기존에 수정 상태로 남아 있던 파일
* 사용자 요청 원본 파일 여부

`CPF_NEW_REQUEST.md`가 수정 상태라면 사용자 요청 원본으로 보고 작업 대상에서 제외한다.

## 4. M1 runtime 상태 간단 재확인

브라우저 클릭 검증 전에 ADM runtime이 다시 기동 가능한지 간단히 확인한다.

필수 확인:

```powershell
.\gradlew.bat :adm:bootJar --offline --no-daemon --console=plain
```

가능하면 기존 smoke도 짧게 확인한다.

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
```

기록할 내용:

```text
ADM jar 생성 여부
기동 URL
실제 포트
health 결과
OpenAPI 결과
smoke 결과
실패 시 사유
```

주의:

* ADM 앱이 기동되지 않으면 브라우저 클릭 검증으로 넘어가지 않는다.
* M1에서 성공했더라도 이번 실행에서 실패하면 실패로 기록한다.
* 앱 기동 실패를 브라우저 클릭 미검증으로만 덮지 말고, runtime 실패와 browser 미검증을 분리한다.

## 5. 브라우저 자동화 가능 여부 확인

Codex 환경에서 브라우저 자동화가 가능한지 먼저 확인한다.

확인 명령 후보:

```powershell
node -v
npm -v
npx playwright --version
where chrome
where msedge
where chromedriver
where geckodriver
```

PowerShell 환경에 따라 `where`가 실패하면 아래도 확인한다.

```powershell
Get-Command chrome -ErrorAction SilentlyContinue
Get-Command msedge -ErrorAction SilentlyContinue
Get-Command npx -ErrorAction SilentlyContinue
```

확인 결과를 아래처럼 분리 기록한다.

```text
Node.js 사용 가능 여부
npm/npx 사용 가능 여부
Playwright 사용 가능 여부
Chrome/Edge 사용 가능 여부
Selenium driver 사용 가능 여부
브라우저 binary 설치 여부
인터넷/패키지 설치 가능 여부
```

판정 기준:

* Playwright 또는 Selenium을 즉시 실행할 수 있으면 UI smoke를 작성하고 실행한다.
* 설치가 필요하고 설치가 가능한 환경이면 최소 설치 후 실행을 시도한다.
* 설치가 불가능하거나 브라우저 binary가 없으면 ADM browser click은 미검증으로 기록하고 필요한 환경 조건을 남긴다.
* Codex가 GUI 화면을 직접 볼 수 있는지 여부는 단정하지 않는다. headless 브라우저 자동화도 실제 클릭 검증으로 인정 가능하다.

## 6. ADM UI 구조 확인

브라우저 자동화가 가능하거나 스크립트 작성이 가능한 경우, 실제 UI 파일과 라우팅 구조를 확인한다.

확인 대상 후보:

```text
adm/src/main/resources/**
adm/src/main/java/**/controller/**
adm/src/main/java/**/web/**
adm/src/main/java/**/api/**
adm/**/static/**
adm/**/templates/**
adm/**/resources/**
```

확인할 내용:

```text
ADM 기본 접속 URL
로그인 필요 여부
테스트 계정 또는 local/test profile 인증 우회 존재 여부
주요 화면 route/path
거래 로그 화면
오류 로그 화면
감사 로그 화면
배치 화면
캐시/정책 화면
운영자/권한/메뉴 화면
```

주의:

* 실제 파일을 확인하지 않고 selector나 URL을 추정하지 않는다.
* 로그인 정보가 필요하면 운영 비밀번호를 요구하거나 기록하지 않는다.
* 테스트 계정, seed data, local/test profile, 인증 우회 설정이 있는지 먼저 repo에서 확인한다.
* 인증 통과가 불가능하면 그 사유를 기록하고 미검증으로 둔다.

## 7. ADM UI smoke 작성

브라우저 자동화가 가능하면 Playwright 또는 Selenium 기반 smoke를 작성한다.

권장 파일 후보:

```text
scripts/smoke-adm-ui.ps1
e2e/adm/adm-ui-smoke.spec.ts
playwright.config.ts
```

이미 유사 파일이 있으면 새로 만들기보다 기존 구조를 활용한다.
필요한 최소 구성은 Codex가 판단한다.

검증 시나리오 후보:

```text
1. ADM base URL 접속
2. 로그인 필요 시 test/local 계정 또는 우회 방식으로 진입
3. 메인 화면 또는 ADM 식별 텍스트 확인
4. 거래 로그 메뉴 또는 route 이동
5. 거래 로그 목록 화면 확인
6. 오류 로그 화면 이동 및 화면 식별 텍스트 확인
7. 감사 로그 화면 이동 및 화면 식별 텍스트 확인
8. 배치 화면 이동 및 화면 식별 텍스트 확인
9. 캐시/정책 화면 이동 가능 여부 확인
10. screenshot 저장
11. 실패 시 trace 또는 screenshot 저장
```

단, 실제 화면에 존재하지 않는 메뉴를 억지로 만들거나 추정하지 않는다.
현재 구현된 UI 범위 안에서 검증 가능한 화면을 우선한다.

## 8. ADM UI smoke 실행

ADM 앱이 기동되어 있고 브라우저 자동화가 가능하면 smoke를 실행한다.

예시:

```powershell
$env:ADM_BASE_URL="http://localhost:8090"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-ui.ps1
```

Playwright 직접 실행이 필요한 경우:

```powershell
$env:ADM_BASE_URL="http://localhost:8090"
npx playwright test e2e/adm/adm-ui-smoke.spec.ts --trace on --screenshot on
```

실행 결과는 아래를 기록한다.

```text
실행 명령
ADM_BASE_URL
접속 URL
클릭한 메뉴/버튼
확인한 화면 텍스트 또는 selector
성공 화면
실패 화면
screenshot 경로
trace 경로
HTTP/API 오류 여부
timeout 여부
```

완료 인정 기준:

* 실제 ADM 앱이 기동된 상태에서 브라우저 자동화가 URL에 접속해야 한다.
* 실제 메뉴 또는 route 이동이 수행되어야 한다.
* 화면 텍스트, selector, table, button 중 하나 이상을 검증해야 한다.
* screenshot 또는 trace/log가 남아야 한다.

정적 HTML 파일 검사, 문자열 grep, Controller path 확인만으로는 browser click 완료가 아니다.

## 9. 결과 상태 기록

`CPF_STABILIZATION_REPORT.html`과 기능 매트릭스에는 내용 기준으로만 정확히 기록한다. 문서 포맷 정본화는 이번 목표가 아니다.

상태 기록 기준:

```text
ADM browser click smoke:
- 실제 자동화 실행과 클릭/화면 검증 성공 시 완료
- 일부 화면만 성공하면 부분 구현
- 브라우저 도구/환경이 없어 실행하지 못하면 미검증
- ADM 앱 기동 실패 때문에 UI 검증이 불가능하면 runtime 실패와 browser 미검증을 분리
- 스크립트 작성은 했지만 실제 실행하지 못했으면 미검증 또는 재확인 필요
```

아래 항목은 이번 작업에서 완료로 올리지 않는다. 실제로 수행했을 때만 상태를 바꾼다.

```text
표준 헤더 Runtime E2E
DB 로그 저장/ADM 조회 E2E
실제 outbound HTTP 수신
MariaDB 신규 빈 DB 전체 설치
Redis/Kafka/MQ broker 검증
```

## 10. 기본 테스트 / 품질 게이트

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :xyz:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

브라우저 자동화 도입으로 Node/Playwright 관련 파일이 추가되었다면 해당 실행 명령도 기록한다.

```powershell
npx playwright test
```

주의:

* 실행하지 못한 명령은 성공으로 기록하지 않는다.
* timeout은 timeout으로 기록한다.
* 재실행했다면 최초 실패와 재실행 성공을 분리한다.
* 문서 포맷 문제만으로 작업을 키우지 않는다.
* 상태값 과장, evidence 불일치, 실행/미검증 오기는 수정한다.

## 11. 완료 기준

아래 중 가능한 경로를 충족해야 한다.

### 11.1 브라우저 자동화 가능 경로

아래가 모두 충족되면 ADM browser click smoke를 완료로 기록할 수 있다.

```text
1. ADM 앱이 실제 기동됨
2. 브라우저 자동화 도구 사용 가능 확인
3. UI smoke script 작성 또는 기존 script 보강
4. 실제 URL 접속 수행
5. 실제 메뉴/route/버튼 중 하나 이상 클릭 또는 이동 수행
6. 화면 텍스트/selector/table/button 중 하나 이상 검증
7. screenshot 또는 trace/log 경로 기록
8. 리포트와 기능 매트릭스에 결과 반영
9. Gradle/check/qualityGate 결과 기록
10. Git commit / push / branch 생성 없음
```

### 11.2 브라우저 자동화 불가능 경로

브라우저 자동화가 불가능한 환경이면 아래가 충족되어야 한다.

```text
1. Node/npm/Playwright/Chrome/Edge/Selenium 가능 여부를 실제 명령으로 확인
2. 불가능한 정확한 사유 기록
3. 필요한 설치/권한/환경 조건 기록
4. ADM browser click을 완료로 기록하지 않음
5. 가능한 범위에서 UI route/static/controller 구조만 확인
6. 기능 매트릭스에는 미검증 또는 재확인 필요로 기록
7. 다음 작업에 필요한 사용자/환경 조치만 명확히 제시
8. Git commit / push / branch 생성 없음
```

## 12. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
ADM 앱을 기동하지 않았는데 browser click 완료 기록
브라우저 자동화 실행 없이 browser click 완료 기록
정적 HTML/문자열 검사만 하고 browser click 완료 기록
Controller path 확인만 하고 UI 검증 완료 기록
screenshot/trace/log 없이 성공 주장
로그인 실패/401/403/404/500/timeout을 성공 처리
표준 헤더 Runtime E2E를 이번 작업에서 실행하지 않고 완료 기록
DB 로그 저장/ADM 조회 확인 없이 로그 E2E 완료 기록
실제 outbound 수신 확인 없이 outbound 완료 기록
브라우저 도구가 없는데 완료로 기록
실행하지 않은 Gradle/check 명령을 성공으로 기록
민감정보 원문 기록
Git commit / push / branch 생성
별도 변경파일 목록 산출물 생성
```

## 13. 완료 보고 양식

완료 보고는 아래 양식으로 작성한다.

```text
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- 이번 작업에서 의도적으로 수정한 파일:
- 기존 수정 상태로 남아 있던 파일:
- GitHub master 확인 가능 여부:

2. M1 runtime 재확인
- ADM bootJar:
- ADM 기동 URL:
- health:
- OpenAPI:
- 기존 smoke 결과:

3. 브라우저 자동화 환경 확인
- node:
- npm/npx:
- Playwright:
- Chrome/Edge:
- Selenium driver:
- 설치 가능 여부:
- 최종 가능/불가능 판정:

4. ADM UI 구조 확인
- 확인한 UI/resource/controller 파일:
- base URL:
- 로그인 필요 여부:
- 테스트 계정/우회 방식:
- 확인한 화면/route:

5. ADM UI smoke 실행 결과
- 실행 명령:
- 접속 URL:
- 클릭/이동한 메뉴 또는 route:
- 확인한 selector/text/table/button:
- screenshot 경로:
- trace/log 경로:
- 성공 화면:
- 실패 화면:
- timeout/오류:

6. 상태값 반영
- ADM browser click:
- 표준 헤더 Runtime E2E:
- DB log 저장/조회:
- outbound HTTP 수신:
- MariaDB full install:
- broker:

7. Gradle/check 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- timeout/재실행 여부:

8. 완료 불인정 항목 자체 점검
- 실행하지 않은 검증 완료 기록 여부:
- 민감정보 원문 기록 여부:
- Git commit/push/branch 여부:
- 변경파일 목록 산출물 생성 여부:

9. 다음 추천 작업
- 표준 헤더 Runtime E2E
- ADM 운영 API smoke 확대
- MariaDB 신규 빈 DB 설치 검증
- BAT standalone/heartbeat/ghost
```

## 14. 다음 마일스톤 분기

이번 작업 결과에 따라 다음을 결정한다.

### ADM browser click smoke가 성공한 경우

다음 작업은 표준 헤더 Runtime E2E로 진행한다.

```text
M3 — 표준 헤더 Runtime E2E
실제 API 호출 → 표준 헤더 검증 → TransactionContext 생성 → DB 로그 저장 → ADM 조회 → outbound mock 서버 수신 확인
```

### 브라우저 자동화가 환경 문제로 미검증인 경우

다음 작업은 표준 헤더 Runtime E2E 또는 ADM 운영 API smoke 확대 중 가능한 쪽으로 진행한다.
브라우저 환경 문제는 리포트에 남기고, 기능 진도를 멈추지 않는다.

```text
M3 후보 1 — 표준 헤더 Runtime E2E
M3 후보 2 — ADM 운영 API smoke 확대
```

### ADM runtime이 다시 실패한 경우

다음 작업은 ADM runtime 복구로 진행한다.

```text
M2-REPAIR — ADM runtime 재기동 실패 원인 수정
```
