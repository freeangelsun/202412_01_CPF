CPF_M3_2_REQUEST_20260701_05 — ADM 권한 기능 최소 Runtime 안정화 및 M4 진입 준비 요청

## 0. CPF 최종 목표

CPF(CoreFlow Platform Framework)의 최종 목표는 금융권을 포함한 범용 업무 시스템에서 실제 프로젝트에 재사용 가능한 운영형 공통 프레임워크를 완성하는 것이다.

최종 상태는 단순 소스 존재나 문서 작성이 아니라 아래가 실제 구현, 테스트, runtime 검증, 운영 문서까지 연결된 상태다.

1. 표준 헤더 수신, 검증, 거래 context 생성, 로그 저장, ADM 조회, outbound 자동 전파까지 E2E로 검증된다.
2. PFW, CMN, ADM, BIZADM, MBR, EXS, BAT, EDU/XYZ 모듈이 각 역할에 맞게 구현된다.
3. ADM은 거래 로그, 오류 로그, 감사 로그, 권한, 메뉴, 버튼, API 권한, 배치, 캐시, 정책, 보안 상태를 조회하고 조치할 수 있는 운영 콘솔이 된다.
4. BAT는 독립 실행 가능한 worker/application으로 job, step, lock, heartbeat, ghost 감지, 재실행, 중지, 운영자 조치, 감사 로그를 지원한다.
5. EDU/XYZ는 개발자가 복사해 신규 기능을 만들 수 있는 실전형 표준 예제가 된다.
6. DB, Flyway, split SQL, all_install, smoke SQL은 정합성을 갖고 신규 빈 MariaDB 기준으로 최종 검증된다.
7. 실행하지 않은 검증, 미구현, 미검증, 실패 항목은 완료로 포장하지 않고 리포트와 기능 매트릭스에 정확히 남긴다.

## 1. 이번 작업 목적

이번 작업의 목적은 M3에서 구현한 ADM 권한 관리 기능을 **운영 위험 구간 중심으로 짧게 안정화**하고, 다음 M4 BAT 개발로 넘어갈 수 있는 상태를 만드는 것이다.

M3에서 역할/메뉴/버튼/API 권한 관리 Controller, Service, DTO, SQL/Flyway, 정적 UI, API 권한 필터, 감사 사유 기반 변경 로그 흐름이 1차 구현되었다. 조회 API runtime smoke도 통과했다.

하지만 아래는 아직 미검증이다.

```text id="tv5fzk"
1. 권한 쓰기 API runtime smoke
2. 권한 변경 감사 로그 실제 적재
3. API 권한 필터의 403/200 차단/허용 runtime 검증
4. V15/Flyway 직접 적용 검증
5. 브라우저 실제 클릭
```

이번 작업은 이 중 **1~3번만 최소 범위로 닫는 것**을 우선한다.
V15 직접 적용은 CLI/환경이 가능하면 확인하고, 불가능하면 미검증 사유를 기록한다.
브라우저 실제 클릭은 환경 부재 상태라면 반복하지 말고 미검증 유지한다.

이번 작업은 검증만 반복하는 작업이 아니라, **방금 개발한 권한 기능이 운영 콘솔로서 위험하지 않은지 최소 안전 확인을 하는 작업**이다.

## 2. 필수 제한

아래는 반드시 지킨다.

```text id="5md044"
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증을 완료로 기록 금지
별도 변경파일 목록 산출물 생성 금지
브라우저 클릭을 수행하지 않았는데 browser click 완료 기록 금지
신규 빈 MariaDB 전체 설치를 하지 않았는데 DB full install 완료 기록 금지
```

그 외에는 이번 목적 달성을 위해 필요한 소스, 테스트, SQL, 스크립트, 리포트, 기능 매트릭스의 최소 수정은 Codex가 판단해서 수행한다.

`CPF_NEW_REQUEST.md`가 수정 상태여도 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.

`CPF_STABILIZATION_REPORT.html`은 내부 검수용 리포트다. 현재 단계에서는 Markdown/plain text 등 작성하기 쉬운 방식으로 작성해도 된다. HTML 포맷, 디자인, CSS, 문장 품질은 완료 기준으로 보지 않는다. 단, 상태값 과장, 실행/미검증 분리, evidence 경로, 실패 사유, 민감정보 원문 기록 금지, 기능 매트릭스와의 내용상 정합성은 반드시 지킨다.

## 3. 작업/검증 요구사항

### 3.1 작업 시작 전 기준 기록

작업 시작 전에 아래 명령을 실행하고 리포트에 기록한다.

```powershell id="21uuig"
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

기록할 내용:

```text id="en3eim"
작업 시작 branch
작업 시작 HEAD SHA
작업 시작 origin/master SHA
작업 시작 status
이번 작업에서 의도적으로 수정한 파일
기존 수정 상태로 남아 있던 파일
사용자 요청 원본 파일 여부
```

### 3.2 M3 구현 현황 재확인

먼저 M3에서 구현된 파일을 실제로 확인한다.

대상 후보:

```text id="k0vi7w"
adm/src/main/java/cpf/adm/opr/controller/AdmPermissionController.java
adm/src/main/java/cpf/adm/opr/service/AdmPermissionService.java
adm/src/main/java/cpf/adm/opr/filter/AdmApiAuthFilter.java
adm/src/test/java/cpf/adm/opr/service/AdmPermissionServiceTest.java
specs/sql/30_adm_schema.sql
specs/sql/migration/flyway/V15__adm_api_permission_management.sql
scripts/smoke-adm-runtime.ps1
scripts/check-feature-evidence.ps1
CPF_STABILIZATION_REPORT.html
specs/기능_구현_매트릭스.html
```

확인할 내용:

```text id="dd5n8e"
역할/메뉴/버튼/API 권한 조회 API
역할/메뉴/버튼/API 권한 쓰기 API
권한 변경 감사 사유 requireReason 흐름
recordChange 감사 로그 흐름
AdmApiAuthFilter 차단/허용 구조
runtime smoke에 이미 포함된 조회 API
미검증으로 남은 쓰기/감사/필터 검증 항목
```

### 3.3 권한 쓰기 API 최소 runtime smoke

이번 작업에서 모든 쓰기 API를 다 smoke할 필요는 없다.
데이터 오염을 피하면서 운영 위험이 큰 대표 케이스만 최소로 검증한다.

우선 smoke 후보:

```text id="67kg4g"
1. 역할 생성 또는 수정
2. 메뉴 상태 변경 또는 메뉴 권한 저장
3. 버튼 권한 저장
4. 역할별 API 권한 저장
```

필수 smoke는 아래 2개 이상이면 된다.

```text id="gyh3f9"
역할별 API 권한 저장
역할 또는 메뉴 또는 버튼 중 하나의 쓰기 API
```

검증 기준:

```text id="vb818d"
테스트용 데이터 또는 임시 데이터 사용
요청 전/후 조회로 변경 반영 확인
감사 사유 헤더 또는 요청값이 필요한 경우 포함
실패 시 HTTP status와 응답 메시지 기록
데이터 오염 방지를 위해 cleanup 또는 원복 수행
cleanup 불가 시 테스트용 식별자와 사유 기록
```

권장 방식:

```text id="um7yf6"
기존 smoke-adm-runtime.ps1에 최소 쓰기 smoke 옵션 추가
또는 scripts/smoke-adm-permission-runtime.ps1 신규 추가
```

쓰기 API smoke가 위험하거나 인증/fixture 문제로 불가능하면, 실행하지 말고 아래를 기록한다.

```text id="dqp66y"
불가능한 API
사유
필요한 fixture/계정/권한/DB 조건
완료로 기록하지 않은 이유
```

### 3.4 감사 로그 적재 검증

쓰기 API를 1개 이상 실행한 뒤 감사 로그가 실제 적재되는지 확인한다.

확인 대상 후보:

```text id="js5ia2"
adm_audit_log
ADM 감사 로그 조회 API
ADM observability audit/log API
recordChange 결과 조회 API
```

검증할 내용:

```text id="zs0r5t"
감사 대상 유형
대상 ID
변경 전/후 값 또는 요약
감사 사유
요청자/operator
요청 시각
민감정보 원문 미포함
```

완료 인정 기준:

```text id="0elk75"
쓰기 API 실행 후 감사 로그를 DB 또는 ADM API로 실제 확인해야 한다.
Controller에서 recordChange를 호출하는 소스 확인만으로 감사 로그 runtime 완료 처리하지 않는다.
```

감사 로그 확인이 어려우면 아래처럼 분리 기록한다.

```text id="47slcx"
쓰기 API 성공: 완료 또는 부분 구현
감사 로그 적재 확인: 미검증 또는 실패
사유: 조회 API 없음 / DB CLI 없음 / 검색 조건 불일치 / 권한 문제 / 기타
```

### 3.5 API 권한 필터 403/200 최소 runtime 검증

`AdmApiAuthFilter`가 실제로 권한을 차단/허용하는지 최소 1개 케이스를 검증한다.

검증 후보:

```text id="k7hz2l"
ADM_ADMIN 또는 동등 권한 계정으로 허용 API 호출 → 200 계열 기대
ADM_VIEWER 또는 권한 없는 계정으로 쓰기 API 호출 → 403 기대
권한 없는 API path 호출 → 403 또는 정책상 차단 기대
```

Codex는 repo의 seed data, 테스트 계정, local/test profile, 로그인 smoke 구조를 먼저 확인한다. 운영 비밀번호를 요구하거나 기록하지 않는다.

검증이 가능한 경우:

```text id="a6m303"
로그인 또는 세션 발급
권한 있는 계정 호출
권한 없는 계정 호출
HTTP status 기록
응답 메시지 기록
필터 로그 또는 감사 로그 확인 가능하면 기록
```

검증이 불가능한 경우:

```text id="vpxi8z"
필요 계정 없음
role/permission seed 없음
로그인 구조 미확인
권한 없는 세션 생성 불가
환경 문제
```

위 사유를 기록하고 `API 권한 runtime 차단/허용 검증`은 미검증 또는 재확인 필요로 둔다.

단위 테스트만 추가한 경우에는 `부분 구현`으로 기록하고 runtime 완료로 올리지 않는다.

### 3.6 V15/Flyway 직접 적용 가능 여부 확인

M3에서 MariaDB CLI가 PATH에 없어 V15 직접 적용 검증은 미검증으로 남았다. 이번에는 가능 여부만 다시 확인한다.

확인 명령 후보:

```powershell id="jkzupm"
mysql --version
mariadb --version
Get-Command mysql -ErrorAction SilentlyContinue
Get-Command mariadb -ErrorAction SilentlyContinue
```

가능하면 V15 또는 Flyway 적용 상태를 확인한다.

후보:

```text id="43u2m2"
flyway_schema_history 조회
adm_api_permission 테이블 존재 확인
adm_role_api_permission 테이블 존재 확인
V15 적용 여부 확인
```

불가능하면 미검증으로 유지한다.

주의:

```text id="d8vpnp"
이번 작업은 신규 빈 MariaDB 전체 설치 검증이 아니다.
CLI가 없으면 전체 DB 검증으로 빠지지 말고 미검증 사유만 기록한다.
DB 전체 설치 검증은 구조 안정 후 별도 마일스톤에서 수행한다.
```

### 3.7 브라우저 클릭 상태 유지

브라우저 런타임이 없으면 이번 작업에서 브라우저 클릭을 반복하지 않는다.

다만 기존 `scripts/smoke-adm-ui.ps1 -BrowserClick -RequireBrowserClick` 실행 진입점이 유지되는지 정도는 확인 가능하다.

상태 기준:

```text id="0ii2h3"
실제 Playwright/Selenium/Chrome/Edge 실행 없으면 ADM browser click은 미검증 유지
정적 UI marker smoke는 완료 유지 가능
```

### 3.8 Smoke script / evidence 보강

이번 작업 결과가 재실행 가능하도록 smoke script와 evidence gate를 필요한 만큼 보강한다.

후보 파일:

```text id="3aug3e"
scripts/smoke-adm-runtime.ps1
scripts/smoke-adm-permission-runtime.ps1
scripts/check-feature-evidence.ps1
```

필수 evidence 후보:

```text id="50mddg"
AdmPermissionController.java
AdmPermissionService.java
AdmApiAuthFilter.java
V15__adm_api_permission_management.sql
AdmPermissionServiceTest.java
권한 runtime smoke script
기능 매트릭스의 M3/M3-2 상태
```

result JSON을 남긴다면 권장 경로:

```text id="9uqjrz"
build/runtime-smoke/adm-permission-runtime-result.json
```

result JSON에는 민감정보 원문을 기록하지 않는다.

### 3.9 기본 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell id="cl7gvi"
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain
.\gradlew.bat :adm:bootJar --offline --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-openapi.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

쓰기 권한 smoke script를 추가했다면 함께 실행한다.

예시:

```powershell id="ja9hod"
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-permission-runtime.ps1
```

주의:

```text id="3kflsg"
실행하지 못한 명령은 성공으로 기록하지 않는다.
timeout은 timeout으로 기록한다.
재실행했다면 최초 실패와 재실행 성공을 분리한다.
문서 포맷 문제만으로 작업을 키우지 않는다.
상태값 과장, evidence 불일치, 실행/미검증 오기는 수정한다.
```

## 4. 완료 기준

아래가 충족되어야 이번 작업을 완료로 본다.

```text id="ro8bod"
1. M3 ADM 권한 기능의 현재 구현 상태를 실제 파일 기준으로 재확인
2. 권한 쓰기 API 최소 2개 이상 runtime smoke 시도
3. 역할별 API 권한 저장 API runtime smoke 시도
4. 쓰기 API 후 조회로 변경 반영 확인 또는 실패 사유 기록
5. 감사 로그 적재를 DB 또는 ADM API로 확인하거나, 확인 불가 사유 기록
6. API 권한 필터 403/200 runtime 검증을 시도하거나, 불가 사유 기록
7. V15/Flyway 직접 적용 가능 여부를 확인하고 가능하면 적용/조회 검증
8. smoke script 또는 evidence gate가 이번 검증을 재실행 가능하게 반영
9. :adm:test, :pfw:test, 전체 test, :adm:bootJar, qualityGate 결과 기록
10. smoke-openapi, smoke-adm-runtime, 권한 smoke 결과 기록
11. 리포트와 기능 매트릭스가 실제 상태와 일치
12. 브라우저 클릭, DB full install, broker, 표준 헤더 Runtime E2E는 실행하지 않았다면 미검증 유지
13. Git commit / push / branch 생성 없음
```

전체를 모두 성공시키지 못해도 된다.
단, 시도한 것과 못 한 것을 정확히 분리하고, 완료로 포장하지 않아야 한다.

## 5. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text id="as38dt"
쓰기 API를 실행하지 않았는데 쓰기 runtime smoke 완료 기록
감사 로그 적재 확인 없이 감사 runtime 완료 기록
API 권한 403/200을 확인하지 않고 필터 runtime 완료 기록
소스 호출 흐름만 보고 runtime 완료 기록
DB CLI가 없는데 V15 직접 적용 완료 기록
브라우저 클릭을 수행하지 않았는데 browser click 완료 기록
표준 헤더 Runtime E2E를 실행하지 않았는데 완료 기록
민감정보 원문이 리포트/log/result JSON에 남음
테스트/qualityGate/smoke를 실행하지 않고 성공 기록
리포트와 기능 매트릭스 상태 불일치
Git commit / push / branch 생성
별도 변경파일 목록 산출물 생성
```

## 6. 완료 보고 양식

완료 보고는 아래 양식으로 작성한다.

```text id="681ru3"
1. 기준 정보
- 작업 시작 branch:
- 작업 시작 HEAD SHA:
- 작업 시작 origin/master SHA:
- 작업 종료 HEAD SHA:
- 작업 종료 status --short:
- 이번 작업에서 의도적으로 수정한 파일:
- 기존 수정 상태로 남아 있던 파일:
- 사용자 요청 원본 파일:

2. M3 구현 재확인
- Controller:
- Service:
- Filter:
- DTO:
- SQL/Flyway:
- Test:
- Smoke:

3. 권한 쓰기 API runtime smoke
- 실행한 API:
- HTTP method/path:
- 요청 데이터 요약:
- HTTP status:
- 변경 후 조회 결과:
- cleanup/원복 여부:
- 실패/미검증 사유:

4. 감사 로그 검증
- 감사 대상:
- 확인 방식(DB/API):
- 확인 결과:
- 민감정보 원문 미포함 여부:
- 실패/미검증 사유:

5. API 권한 필터 검증
- 허용 케이스:
- 차단 케이스:
- 계정/role 조건:
- HTTP status:
- 실패/미검증 사유:

6. V15/Flyway 확인
- mysql/mariadb CLI 여부:
- V15 적용 확인:
- 테이블 존재 확인:
- 실패/미검증 사유:

7. 실행 명령 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- timeout/재실행 여부:

8. 리포트/매트릭스 상태
- 완료:
- 부분 구현:
- 미구현:
- 미검증:
- 실패:
- 재확인 필요:

9. 완료 불인정 항목 자체 점검
- 실행하지 않은 검증 완료 기록 여부:
- 민감정보 원문 기록 여부:
- Git commit/push/branch 여부:
- 변경파일 목록 산출물 생성 여부:

10. 다음 추천 작업
- M4 BAT standalone / heartbeat / ghost / center-cut 개발
- 또는 ADM 권한 보강이 더 필요한 경우 남은 보강 항목
```

## 7. 다음 마일스톤 분기

### M3-2가 충분히 닫힌 경우

다음은 기능 개발 마일스톤으로 전진한다.

```text id="v0vk7u"
M4 — BAT standalone / heartbeat / ghost / center-cut 개발
```

### 권한 쓰기/API 필터/감사 중 치명 오류가 발견된 경우

치명 오류만 짧게 repair 한다.

```text id="s7fgzs"
M3-REPAIR — ADM 권한 쓰기/API 필터/감사 오류 수정
```

### 환경 문제로 runtime 일부가 미검증인 경우

미검증 사유를 남기고 M4 BAT 개발로 전진한다.
환경 문제로 기능 개발 전체를 멈추지 않는다.

```text id="88y5e5"
M4 — BAT standalone / heartbeat / ghost / center-cut 개발
```
