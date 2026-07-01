CPF_M3_REQUEST_20260701_04 — ADM 운영 콘솔 핵심 기능 1차 개발 요청

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

이번 작업의 목적은 선행 공통 게이트 이후 실제 기능 개발로 전환하는 것이다.

직전 작업까지 아래 기반은 확보되었다.

1. 표준 헤더 Source-Level 검증과 Runtime E2E 미검증 분리
2. ADM runtime / OpenAPI smoke 실제 실행
3. ADM browser click 자동화 진입점 구축
4. 실제 browser click은 환경 부재로 미검증 기록

따라서 이번 작업은 더 깊은 E2E 검증을 반복하지 않고, **ADM 운영 콘솔 핵심 기능 1차 구현**을 진행한다.

이번 1차 개발의 중심은 ADM 운영 콘솔의 기본 권한/메뉴/운영자 관리 축이다.

우선 개발 대상:

1. 운영자 관리
2. 역할 관리
3. 메뉴 관리
4. 버튼 권한 관리
5. API 권한 관리
6. 권한 변경 감사 로그
7. ADM runtime smoke 대상 path 반영

가능하면 Codex 판단으로 캐시, 메시지, 코드, 설정 조회/관리 기능의 기초 API도 함께 보강한다. 단, 억지로 범위를 키우기보다 운영자/권한/메뉴/API 권한 축을 실제로 닫는 것을 우선한다.

## 2. 필수 제한

아래는 반드시 지킨다.

* Git commit 금지
* Git push 금지
* branch 생성 금지
* 민감정보 원문 기록 금지
* 실행하지 않은 검증을 완료로 기록 금지
* 별도 변경파일 목록 산출물 생성 금지

그 외에는 이번 목적 달성을 위해 필요한 소스, 테스트, SQL, 스크립트, 리포트, 기능 매트릭스의 최소 수정은 Codex가 판단해서 수행한다.

`CPF_NEW_REQUEST.md`가 수정 상태여도 사용자 요청 원본으로 보고 기능 검수 대상에서 제외한다.

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

```text
작업 시작 branch
작업 시작 HEAD SHA
작업 시작 origin/master SHA
작업 시작 status
이번 작업에서 의도적으로 수정한 파일
기존 수정 상태로 남아 있던 파일
사용자 요청 원본 파일 여부
```

## 4. 현황 파악

먼저 ADM 쪽 기존 구현을 확인한다.

확인 대상 후보:

```text
adm/src/main/java/**
adm/src/main/resources/**
pfw/src/main/java/**
specs/**
sql/**
db/**
scripts/**
```

기존 구현이 있는 경우 새로 만들기보다 보강한다.
중복 Controller, 중복 Service, 중복 DTO, 중복 SQL을 만들지 않는다.

확인할 항목:

```text
운영자 관련 Controller/Service/Repository/DTO/SQL
역할 관련 Controller/Service/Repository/DTO/SQL
메뉴 관련 Controller/Service/Repository/DTO/SQL
버튼 권한 관련 Controller/Service/Repository/DTO/SQL
API 권한 관련 Controller/Service/Repository/DTO/SQL
감사 로그 기록 위치
권한 변경 이력 기록 위치
ADM OpenAPI 노출 여부
기존 smoke script path
기능 매트릭스 현재 상태
```

## 5. ADM 운영자 관리 구현 요구사항

운영자 관리는 ADM 운영 콘솔의 기본 기능이다.

필수 기능 후보:

```text
운영자 목록 조회
운영자 상세 조회
운영자 생성
운영자 수정
운영자 사용/중지 상태 변경
운영자 잠금/잠금 해제
운영자 역할 매핑 조회
운영자 역할 부여/해제
```

필드 기준 후보:

```text
operatorId
operatorName
loginId
email
departmentCode
status
lockedYn
lastLoginAt
createdAt
createdBy
updatedAt
updatedBy
```

구현 기준:

```text
Controller → Service → Repository/Mapper → DTO → SQL 흐름을 갖춘다.
비밀번호 원문은 저장하거나 로그로 남기지 않는다.
운영자 상태 변경, 잠금/해제, 역할 변경은 감사 로그 대상이다.
목록 조회는 검색 조건과 paging을 고려한다.
하드코딩 응답만으로 완료 처리하지 않는다.
```

## 6. 역할 관리 구현 요구사항

필수 기능 후보:

```text
역할 목록 조회
역할 상세 조회
역할 생성
역할 수정
역할 사용/중지
역할별 메뉴 권한 조회
역할별 버튼 권한 조회
역할별 API 권한 조회
역할별 권한 저장
```

필드 기준 후보:

```text
roleId
roleCode
roleName
roleDescription
status
createdAt
createdBy
updatedAt
updatedBy
```

구현 기준:

```text
역할 변경은 감사 로그 대상이다.
역할과 메뉴/버튼/API 권한 매핑은 분리한다.
사용 중인 역할 삭제 또는 중지 정책은 Service에서 검토한다.
권한 저장은 중복/누락/비정상 ID를 검증한다.
```

## 7. 메뉴 관리 구현 요구사항

필수 기능 후보:

```text
메뉴 트리 조회
메뉴 상세 조회
메뉴 생성
메뉴 수정
메뉴 사용/중지
메뉴 정렬 순서 변경
역할별 메뉴 노출 권한 조회
```

필드 기준 후보:

```text
menuId
parentMenuId
menuCode
menuName
menuPath
menuLevel
sortOrder
useYn
createdAt
createdBy
updatedAt
updatedBy
```

구현 기준:

```text
메뉴는 계층 구조를 고려한다.
parent-child 구조가 깨지지 않게 검증한다.
순환 참조가 생기지 않게 한다.
메뉴 변경은 감사 로그 대상이다.
```

## 8. 버튼 권한 관리 구현 요구사항

필수 기능 후보:

```text
버튼 목록 조회
버튼 상세 조회
버튼 생성
버튼 수정
버튼 사용/중지
메뉴별 버튼 목록 조회
역할별 버튼 권한 저장
```

필드 기준 후보:

```text
buttonId
menuId
buttonCode
buttonName
buttonType
sortOrder
useYn
createdAt
createdBy
updatedAt
updatedBy
```

구현 기준:

```text
버튼은 메뉴에 종속된다.
역할별 버튼 권한은 메뉴 권한과 함께 검토한다.
다운로드, 마스킹 해제, 강제 실행 같은 고위험 버튼은 감사 대상이다.
```

## 9. API 권한 관리 구현 요구사항

필수 기능 후보:

```text
API 권한 목록 조회
API 권한 상세 조회
API 권한 등록
API 권한 수정
API 권한 사용/중지
역할별 API 권한 조회
역할별 API 권한 저장
```

필드 기준 후보:

```text
apiPermissionId
apiGroupCode
httpMethod
apiPath
apiName
permissionCode
useYn
createdAt
createdBy
updatedAt
updatedBy
```

구현 기준:

```text
API path는 실제 Controller path와 맞아야 한다.
Controller 확인 없이 추정 path를 기록하지 않는다.
역할별 API 권한은 runtime 보안 interceptor와 연결될 수 있게 구조를 잡는다.
이번 단계에서 interceptor까지 완성하지 못하면 미검증 또는 부분 구현으로 기록한다.
API 권한 변경은 감사 로그 대상이다.
```

## 10. 감사 로그 요구사항

이번 ADM 권한/운영 기능 변경은 감사 로그 위치를 반드시 고려한다.

감사 대상 후보:

```text
운영자 생성/수정/상태 변경/잠금 해제
역할 생성/수정/상태 변경
운영자 역할 부여/해제
메뉴 생성/수정/상태 변경
버튼 권한 변경
API 권한 변경
다운로드 권한 또는 고위험 권한 변경
```

구현 기준:

```text
기존 PFW/ADM 감사 로그 구조가 있으면 재사용한다.
새 테이블이나 새 이벤트가 필요하면 Codex가 판단해 최소 반영한다.
감사 로그에 비밀번호, token, API key 등 민감정보 원문을 남기지 않는다.
감사 로그 저장이 아직 완전하지 않으면 API 기능과 감사 연계 상태를 분리 기록한다.
```

## 11. SQL / Mapper / Flyway 반영 기준

이번 기능에 DB 변경이 필요하면 Codex가 판단해 반영한다.

확인 및 반영 대상 후보:

```text
adm/src/main/resources/**/*.xml
adm/src/main/resources/**/*.sql
sql/**
db/**
migration/**
flyway/**
```

요구사항:

```text
split SQL, Flyway, all_install 계열이 이미 존재하면 정합성을 고려한다.
한쪽에만 추가하고 다른 설치 경로를 방치하지 않는다.
다만 신규 빈 MariaDB 전체 설치 검증은 이번 완료 기준이 아니다.
DB 구조 변경이 있으면 smoke/test에 필요한 fixture 또는 기본 데이터도 고려한다.
테이블/컬럼에는 가능한 한 COMMENT를 둔다.
SQL에는 운영 의미를 설명하는 한글 주석을 가능한 범위에서 작성한다.
```

## 12. API / OpenAPI / Runtime smoke 반영

이번에 추가하거나 보강한 ADM API는 OpenAPI에 노출되어야 한다.

요구사항:

```text
Controller path와 HTTP method를 명확히 한다.
요청/응답 DTO를 둔다.
Validation을 가능한 범위에서 적용한다.
주요 API는 smoke 대상에 포함한다.
scripts/smoke-adm-runtime.ps1 또는 별도 smoke script에 최소 조회 API를 반영한다.
쓰기 API는 위험하면 테스트 profile 또는 fixture 기준으로만 smoke한다.
```

권장 smoke 대상:

```text
운영자 목록 조회
역할 목록 조회
메뉴 트리 조회
버튼 목록 조회
API 권한 목록 조회
권한 감사 로그 목록 조회
```

쓰기 API는 데이터 오염 위험이 있으면 단위/통합 테스트 중심으로 검증하고, runtime smoke에서는 조회 위주로 검증한다.

## 13. 테스트 요구사항

이번 작업은 기능 개발이 중심이지만 최소 품질 게이트는 닫아야 한다.

필수 테스트 후보:

```text
ADM Controller 또는 API 테스트
ADM Service 테스트
Mapper/Repository 테스트
권한 매핑 저장/조회 테스트
메뉴 트리 조회 테스트
API 권한 path 검증 테스트
감사 로그 기록 위치 테스트
```

기준:

```text
mock만으로 전체 완료 처리하지 않는다.
Mapper/XML/SQL이 추가되면 가능한 범위에서 DB fixture 기반 테스트를 둔다.
테스트가 환경 문제로 불가능하면 미검증 사유를 기록한다.
기존 테스트를 깨뜨리지 않는다.
```

## 14. 기본 검증 명령

가능한 범위에서 아래를 실행한다.

```powershell
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

주의:

```text
실행하지 못한 명령은 성공으로 기록하지 않는다.
timeout은 timeout으로 기록한다.
재실행했다면 최초 실패와 재실행 성공을 분리한다.
문서 포맷 문제만으로 작업을 키우지 않는다.
상태값 과장, evidence 불일치, 실행/미검증 오기는 수정한다.
```

## 15. 리포트 / 기능 매트릭스 반영 기준

`CPF_STABILIZATION_REPORT.html`과 `specs/기능_구현_매트릭스.html`에는 내용 기준으로 정확히 반영한다. 포맷 정본화는 이번 작업의 목표가 아니다.

반드시 기록할 것:

```text
구현한 ADM 기능 목록
Controller/Service/Repository/DTO/SQL 파일
추가/변경 API path
테스트 결과
runtime smoke 결과
완료/부분 구현/미검증/실패 상태
감사 로그 연계 여부
권한 runtime interceptor 연계 여부
남은 미검증
```

상태 기록 기준:

```text
Controller/Service/Repository/DTO/SQL/Test/Smoke가 연결된 기능은 완료 또는 부분 구현
소스만 있고 테스트/SQL/smoke가 없으면 부분 구현
설계나 리포트만 있으면 미구현 또는 미검증
실행하지 않은 runtime 검증은 미검증
```

## 16. 완료 기준

아래를 충족해야 이번 작업을 완료로 볼 수 있다.

```text
1. ADM 운영자/역할/메뉴/버튼/API 권한 중 1차 범위가 실제 구현됨
2. 각 기능은 가능한 범위에서 Controller, Service, Repository/Mapper, DTO, SQL 구조를 가짐
3. 권한/메뉴/API 권한 변경에 대한 감사 로그 위치가 구현 또는 명확히 분리 기록됨
4. 주요 조회 API가 OpenAPI 또는 runtime smoke 대상에 반영됨
5. 단위/통합 테스트가 추가 또는 보강됨
6. :adm:test, :pfw:test, :adm:bootJar, qualityGate 결과가 기록됨
7. 가능한 smoke-openapi, smoke-adm-runtime 결과가 기록됨
8. 리포트와 기능 매트릭스가 실제 구현 상태와 일치함
9. 실행하지 않은 표준 헤더 Runtime E2E, DB 전체 설치, broker 검증은 완료로 기록하지 않음
10. Git commit / push / branch 생성 없음
```

## 17. 완료 불인정 기준

아래 중 하나라도 해당하면 완료로 인정하지 않는다.

```text
Controller만 있고 Service/Repository/DTO/SQL이 없음
하드코딩 응답만 있음
API path를 Controller 확인 없이 추정
권한 변경 기능이 있는데 감사 위치가 없음
비밀번호/token/API key 원문이 로그나 리포트에 남음
테스트를 실행하지 않았는데 성공으로 기록
runtime smoke를 하지 않았는데 완료로 기록
문서/리포트만 고치고 실제 기능 구현이 없음
리포트와 기능 매트릭스 상태 불일치
Git commit / push / branch 생성
별도 변경파일 목록 산출물 생성
```

## 18. 완료 보고 양식

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

2. 구현 요약
- 운영자:
- 역할:
- 메뉴:
- 버튼 권한:
- API 권한:
- 감사 로그:
- 캐시/메시지/코드/설정 추가 보강 여부:

3. 계층별 파일
- Controller:
- Service:
- Repository/Mapper:
- DTO/Validation:
- SQL/Flyway/all_install:
- Test:
- Smoke script:

4. API 목록
- 기능명:
- HTTP method:
- path:
- 인증/권한:
- smoke 여부:

5. 감사/보안
- 감사 로그 대상:
- 민감정보 원문 차단:
- 권한 변경 이력:
- 미구현/미검증:

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
- ADM 운영 API 추가 보강:
- BAT standalone/heartbeat/ghost:
- EDU/XYZ 실전 샘플:
- 표준 헤더 Runtime E2E:
- DB/Flyway/all_install:
```

## 19. 다음 마일스톤 분기

이번 작업이 정상 완료되면 다음은 결과에 따라 분기한다.

### ADM 운영 콘솔 1차 기능이 충분히 구현된 경우

```text
M4 — BAT standalone / heartbeat / ghost / center-cut 개발
```

### ADM 기능 일부가 얕게 구현된 경우

```text
M3-2 — ADM 운영 콘솔 기능 보강
운영자/역할/메뉴/버튼/API 권한 중 부족한 계층과 테스트를 보강
```

### ADM 기능은 닫혔지만 runtime smoke가 부족한 경우

```text
M3-SMOKE — ADM 운영 API smoke 확대
조회 API 중심으로 runtime smoke 보강
```

### DB 구조 변경이 커져서 기능 실행을 막는 경우

```text
M3-DB-REPAIR — ADM 기능 실행을 막는 DB/Flyway 최소 정합성 수정
```
