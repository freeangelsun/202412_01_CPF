CPF_M4_REQUEST_20260701_06 — BAT Standalone / Heartbeat / Ghost / Center-Cut 1차 개발 요청

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

이번 작업의 목적은 M3/M3-2에서 ADM 권한 관리 1차 기능과 권한 runtime 안정화가 완료된 상태를 바탕으로, 다음 핵심 개발 마일스톤인 **BAT standalone / heartbeat / ghost / center-cut 1차 기능**을 실제 구현하는 것이다.

이번 작업은 검증만 반복하는 작업이 아니다. BAT 모듈이 독립 실행 가능한 batch worker/application으로 동작하기 위한 1차 운영 구조를 만든다.

이번 작업의 중심은 아래다.

```text
1. BAT 독립 bootJar / runtime 기동 가능성 확인
2. BAT worker instance 등록/조회
3. heartbeat 기록/갱신
4. heartbeat timeout 기반 ghost candidate 감지
5. job/step 실행 이력과 worker 연결
6. operator action 기반 stop/retry/force request 모델 정리
7. ADM에서 BAT worker/heartbeat/ghost 상태를 조회할 수 있는 API 연결
8. center-cut 기본 모수/item/result 구조 1차 설계 또는 구현
```

깊은 장애 E2E, 전체 MariaDB 신규 설치, 브라우저 클릭, 표준 헤더 Runtime E2E는 이번 작업의 중심이 아니다. 다만 이번 기능 개발에 필요한 최소 test/smoke/qualityGate는 수행한다.

## 2. 필수 제한

아래는 반드시 지킨다.

```text
Git commit 금지
Git push 금지
branch 생성 금지
민감정보 원문 기록 금지
실행하지 않은 검증을 완료로 기록 금지
별도 변경파일 목록 산출물 생성 금지
신규 빈 MariaDB full install을 하지 않았는데 완료 기록 금지
BAT runtime을 기동하지 않았는데 BAT runtime 완료 기록 금지
```

그 외에는 이번 목적 달성을 위해 필요한 소스, 테스트, SQL, 스크립트, 리포트, 기능 매트릭스의 최소 수정은 Codex가 판단해서 수행한다.

`CPF_NEW_REQUEST.md`가 수정 상태여도 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.

`CPF_STABILIZATION_REPORT.md`는 내부 검수용 리포트다. Markdown/plain text 중심으로 내용만 충실하게 작성한다. 문서 디자인/포맷에 공들이지 않는다. 단, 상태값 과장, 실행/미검증 분리, evidence 경로, 실패 사유, 민감정보 원문 기록 금지, 기능 매트릭스와의 내용상 정합성은 반드시 지킨다.

## 3. M3-2 후속 보안 보강

M3-2에서 `apply-v15-adm-api-permission-management.ps1`에 기본 DB 비밀번호 fallback이 확인되었다.

이번 작업 시작 시 또는 마무리 시 아래를 같이 보강한다.

```text
1. apply-v15-adm-api-permission-management.ps1의 Password 기본값 제거
2. DB 비밀번호는 환경변수 또는 명시 파라미터로만 받기
3. 값이 없으면 V15 JDBC smoke는 SKIPPED 또는 FAILED로 기록
4. 리포트/result JSON에 비밀번호 원문 기록 금지
```

이건 M4 개발을 막는 큰 작업이 아니라 작은 보안 정리다. 이 보강 때문에 BAT 개발 범위를 지연시키지 않는다.

## 4. 작업 시작 전 기준 기록

작업 시작 전에 아래 명령을 실행하고 `CPF_STABILIZATION_REPORT.md`에 기록한다.

```powershell
git branch --show-current
git remote -v
git rev-parse HEAD
git rev-parse origin/master
git status --short
git log -1 --oneline
```

기록할 내용:

```text
작업 시작 branch
작업 시작 HEAD SHA
작업 시작 origin/master SHA
작업 시작 status
이번 작업에서 의도적으로 수정한 파일
기존 수정 상태로 남아 있던 파일
사용자 요청 원본 파일 여부
```

## 5. BAT 현황 파악

먼저 BAT와 PFW batch 공통 구조를 실제 파일 기준으로 확인한다.

확인 대상 후보:

```text
bat/src/main/java/**
bat/src/main/resources/**
bat/src/test/java/**
pfw/src/main/java/**/batch/**
pfw/src/main/java/**/scheduler/**
pfw/src/main/java/**/lock/**
pfw/src/main/java/**/heartbeat/**
adm/src/main/java/**/batch/**
adm/src/main/java/**/controller/**Batch**
specs/sql/**
scripts/smoke-bat-runtime.ps1
scripts/check-feature-evidence.ps1
```

확인할 내용:

```text
BAT application main 존재 여부
BAT bootJar 가능 여부
worker instance 모델
job/step execution 모델
lock 모델
heartbeat 모델
ghost candidate 모델
operator action 모델
center-cut 관련 기존 구조
ADM batch 조회 API와 연결 구조
SQL/Flyway/all_install 반영 상태
```

기존 구현이 있으면 새로 만들기보다 보강한다. 중복 테이블, 중복 Controller, 중복 Service를 만들지 않는다.

## 6. BAT standalone worker 요구사항

BAT는 ADM 하위 기능이 아니라 독립 실행 가능한 worker/application이어야 한다.

필수 요구사항 후보:

```text
BAT Spring Boot application main
:bat:bootJar 성공
BAT jar 독립 실행
workerId / instanceId / host / processId / moduleCode 등록
startup/shutdown lifecycle 기록
worker 상태 조회
worker 종료 시 상태 정리
```

구현 기준:

```text
BAT 모듈 안에 실제 runtime entrypoint가 있어야 한다.
ADM은 관제와 요청을 담당하고, BAT가 job/step 실행과 heartbeat를 담당한다.
worker 정보는 ADM에서 조회 가능해야 한다.
```

## 7. Heartbeat 요구사항

필수 기능 후보:

```text
worker heartbeat 주기적 기록
lastHeartbeatAt 갱신
heartbeat interval/timeout 설정
heartbeat 상태 조회
heartbeat 중단 worker 감지 기준
```

구현 기준:

```text
heartbeat는 단순 로그 출력이 아니라 DB 또는 운영 메타 저장소에 기록되어야 한다.
timeout 기준은 설정 가능해야 한다.
ADM에서 worker별 heartbeat 상태를 조회할 수 있어야 한다.
```

## 8. Ghost 감지 요구사항

ghost는 worker가 죽었거나 heartbeat가 끊겼는데 실행 중 상태가 남아 있는 위험 상태다.

필수 기능 후보:

```text
heartbeat timeout 기반 ghost candidate 조회
running job/step과 worker heartbeat 비교
ghost candidate 상태 기록
operator가 ghost candidate를 확인/조치할 수 있는 API
```

구현 기준:

```text
ghost 감지는 실제 운영 장애 대응 기능이다.
단순 문서 설명만으로 완료 처리하지 않는다.
최소한 candidate 산정 로직과 조회 API가 있어야 한다.
```

## 9. Job / Step / Lock 요구사항

필수 기능 후보:

```text
job execution 이력 조회
step execution 이력 조회
job parameter 기록
workerId와 job/step 연결
lock 획득/해제 기록
lock timeout 또는 stale lock 후보 조회
```

구현 기준:

```text
Spring Batch 기본 테이블만 나열하지 말고 CPF 운영 메타와 연결한다.
ADM에서 job/step/worker/lock 상태를 조회할 수 있어야 한다.
```

## 10. Operator action 요구사항

운영자는 배치 상태를 보고 조치할 수 있어야 한다.

필수 기능 후보:

```text
중지 요청
재실행 요청
강제수행 요청
ghost 확인 처리
lock 해제 요청
operator action audit 기록
```

이번 1차 작업에서 모든 조치를 완성하지 못해도 된다. 다만 모델/API/감사 위치는 실제 구현하거나 부분 구현으로 명확히 기록한다.

## 11. Center-Cut 1차 요구사항

센터컷은 PFW 공통 표준과 BAT 기본 구현의 경계를 지켜야 한다.

원칙:

```text
PFW는 센터컷 실행 표준 인터페이스, 상태 코드, transactionGlobalId parent/child 기준, 로그/감사/오류 표준을 제공한다.
BAT는 기본 센터컷 모수/item/result 테이블과 기본 구현체를 제공한다.
업무 주제영역은 필요 시 커스텀 모수/item/result 테이블과 Provider/Repository/Handler adapter로 연동한다.
ADM은 PFW/BAT/업무 adapter를 통해 관제한다.
```

이번 1차 구현 후보:

```text
center-cut job definition
center-cut parameter table
center-cut item table
center-cut result table
CenterCutTargetProvider interface
CenterCutHandler interface
기본 sample handler
상태 코드: READY/RUNNING/SUCCESS/FAILED/SKIPPED/RETRY_REQUESTED/STOP_REQUESTED
parent transactionGlobalId / child transactionGlobalId 연결 기준
```

이번 작업에서 센터컷 전체를 완성하지 못하면 `부분 구현`으로 기록한다. 단순 문서만 있으면 완료로 기록하지 않는다.

## 12. ADM batch 관제 연결

ADM은 BAT 상태를 조회하고 조치할 수 있어야 한다.

확인/보강 대상:

```text
ADM batch jobs
ADM batch schedules
ADM batch executions
ADM batch steps
ADM batch workers
ADM batch locks
ADM batch ghost candidates
ADM batch operations
```

요구사항:

```text
기존 ADM batch API가 있으면 BAT runtime 메타와 연결한다.
worker heartbeat와 ghost candidate가 ADM 조회 API에 포함되어야 한다.
operator action이 있으면 감사 로그 위치를 고려한다.
```

## 13. SQL / Flyway / all_install 반영 기준

이번 기능에 DB 변경이 필요하면 Codex가 판단해 반영한다.

확인 및 반영 대상 후보:

```text
specs/sql/**
specs/sql/migration/flyway/**
00_all_install.sql
00_all_install_and_smoke.sql
bat/src/main/resources/**
pfw/src/main/resources/**
adm/src/main/resources/**
```

요구사항:

```text
split SQL, Flyway, all_install 계열이 이미 존재하면 정합성을 고려한다.
한쪽에만 추가하고 다른 설치 경로를 방치하지 않는다.
신규 빈 MariaDB 전체 설치 검증은 이번 완료 기준이 아니지만, SQL 경로 불일치는 만들지 않는다.
테이블/컬럼 COMMENT와 한글 주석을 가능한 범위에서 작성한다.
```

## 14. 테스트 / Smoke 요구사항

이번 작업은 기능 개발이 중심이지만 최소 품질 게이트는 닫아야 한다.

필수 테스트 후보:

```text
BAT application context test
heartbeat service test
ghost candidate 산정 test
lock stale candidate test
center-cut provider/handler test
ADM batch query API test
```

runtime smoke 후보:

```text
scripts/smoke-bat-runtime.ps1
```

smoke에 포함할 후보:

```text
:bat:bootJar
BAT jar 기동
worker 등록 확인
heartbeat 기록 확인
ADM worker 조회
ghost candidate 조회
cleanup
```

환경 문제로 BAT runtime 실행이 어렵다면 미검증 사유를 기록한다. BAT bootJar나 앱 기동을 실행하지 않았는데 runtime 완료로 기록하지 않는다.

## 15. 기본 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell
.\gradlew.bat :bat:test --offline --no-daemon --console=plain
.\gradlew.bat :bat:bootJar --offline --no-daemon --console=plain
.\gradlew.bat :adm:test --offline --no-daemon --console=plain
.\gradlew.bat :pfw:test --offline --no-daemon --console=plain
.\gradlew.bat test --offline --no-daemon --console=plain
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-bat-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-adm-runtime.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-feature-evidence.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\check-utf8.ps1 -CheckMojibake
.\gradlew.bat qualityGate --offline --no-daemon --console=plain
```

주의:

```text
실행하지 못한 명령은 성공으로 기록하지 않는다.
timeout은 timeout으로 기록한다.
재실행했다면 최초 실패와 재실행 성공을 분리한다.
문서 포맷 문제만으로 작업을 키우지 않는다.
상태값 과장, evidence 불일치, 실행/미검증 오기는 수정한다.
```

## 16. 리포트 / 기능 매트릭스 반영 기준

`CPF_STABILIZATION_REPORT.md`와 `specs/기능_구현_매트릭스.html`에는 내용 기준으로 정확히 반영한다.

반드시 기록할 것:

```text
구현한 BAT 기능 목록
Controller/Service/Repository/DTO/SQL 파일
worker/heartbeat/ghost/lock/job/step/center-cut 상태
테스트 결과
runtime smoke 결과
완료/부분 구현/미구현/미검증/실패/재확인 필요 상태
남은 미검증
```

상태 기록 기준:

```text
BAT bootJar와 runtime 기동 성공 시 해당 항목 완료
worker/heartbeat/ghost가 소스와 테스트만 있으면 부분 구현
ADM 조회 API까지 연결되면 부분 구현 또는 완료
실행하지 않은 runtime은 미검증
신규 빈 MariaDB full install은 이번에 직접 하지 않으면 미검증
```

## 17. 완료 기준

아래가 충족되어야 이번 작업을 완료로 볼 수 있다.

```text
1. BAT standalone 구조가 실제 파일 기준으로 확인 또는 보강됨
2. worker registration / heartbeat / ghost candidate 중 1차 범위가 실제 구현됨
3. job/step/lock/operator action 구조가 실제 구현 또는 부분 구현으로 명확히 기록됨
4. center-cut 기본 구조가 실제 구현 또는 설계+부분 구현으로 정리됨
5. ADM batch 관제 API와 BAT 메타 연결 상태가 확인됨
6. 필요한 SQL/Flyway/all_install 반영이 누락 없이 고려됨
7. :bat:test, :bat:bootJar, 관련 test/qualityGate 결과가 기록됨
8. 가능하면 smoke-bat-runtime.ps1 실행 결과가 기록됨
9. 리포트와 기능 매트릭스가 실제 상태와 일치함
10. M3-2 DB 비밀번호 fallback 보강이 반영됨
11. Git commit / push / branch 생성 없음
```

## 18. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
BAT bootJar를 실행하지 않았는데 BAT runtime 완료 기록
worker/heartbeat가 DB나 운영 메타에 남지 않는데 heartbeat 완료 기록
ghost 산정 로직 없이 ghost 감지 완료 기록
ADM 조회 API 연결 없이 ADM 관제 완료 기록
center-cut 문서만 있고 구현이 없는데 완료 기록
SQL이 한쪽에만 있고 Flyway/all_install 정합성을 방치
비밀번호/token/API key 원문이 로그나 리포트에 남음
테스트/qualityGate/smoke를 실행하지 않고 성공 기록
리포트와 기능 매트릭스 상태 불일치
Git commit / push / branch 생성
별도 변경파일 목록 산출물 생성
```

## 19. 완료 보고 양식

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
- 사용자 요청 원본 파일:

2. M3-2 보안 보강
- V15 apply script password fallback 제거 여부:
- 비밀번호 주입 방식:
- 민감정보 원문 기록 방지:

3. BAT 구현 요약
- standalone:
- worker:
- heartbeat:
- ghost:
- job/step:
- lock:
- operator action:
- center-cut:

4. 계층별 파일
- BAT Application:
- Controller/API:
- Service:
- Repository/Mapper:
- DTO:
- SQL/Flyway/all_install:
- Test:
- Smoke script:

5. ADM 관제 연결
- workers:
- heartbeat:
- ghost candidates:
- locks:
- operations:
- API path:
- smoke 여부:

6. 실행 명령 결과
- 명령:
- 결과:
- tests:
- skipped:
- failures:
- errors:
- timeout/재실행 여부:

7. 리포트/매트릭스 상태
- 완료:
- 부분 구현:
- 미구현:
- 미검증:
- 실패:
- 재확인 필요:

8. 남은 작업
- BAT 추가 보강:
- center-cut 추가 보강:
- EDU/XYZ 실전 샘플:
- CMN/EXS/MBR/BIZADM:
- 표준 헤더 Runtime E2E:
- DB/Flyway/all_install:
```

## 20. 다음 마일스톤 분기

### M4가 충분히 구현된 경우

다음은 EDU/XYZ 실전 샘플 완성 또는 CMN/EXS/MBR/BIZADM 기본 구현체 보강으로 진행한다.

```text
M5 후보 1 — EDU/XYZ 실전 샘플 완성
M5 후보 2 — CMN/EXS/MBR/BIZADM 기본 구현체 보강
```

### BAT runtime이 실패한 경우

```text
M4-REPAIR — BAT bootJar/runtime/heartbeat 실패 원인 수정
```

### center-cut이 설계 수준에 머문 경우

```text
M4-2 — center-cut 기본 구현체 보강
```
