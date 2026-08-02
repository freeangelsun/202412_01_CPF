# CPF ADM 매뉴얼 — 업무 연동과 권한별 조회·조치·승인·감사

> **주 독자**: 업무 개발자, ADM 연동 개발자, 조회자, 운영자, 승인자, 보안 담당자, 운영 관리자
> **완료 결과**: 업무 기능을 ADM에 연결하고, 권한에 따라 조회·판단·조치·승인·대사·감사를 수행한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `54bcc10887a83b933685bff462c0b0d7df824923`

<!-- CPF-TOC:START -->
## 전체 목차

- [1. ADM의 정체성](#1-adm의-정체성)
- [2. Owner와 경계](#2-owner와-경계)
- [3. 기준 Source 위치](#3-기준-source-위치)
- [4. 역할별 권한 모델](#4-역할별-권한-모델)
- [5. 업무 연동 계약](#5-업무-연동-계약)
  - [5.1 Query 계약](#51-query-계약)
  - [5.2 Command 계약](#52-command-계약)
  - [5.3 Operation 조회](#53-operation-조회)
- [6. Same-JVM과 Remote 연결](#6-same-jvm과-remote-연결)
- [7. ADM 연동 개발 절차](#7-adm-연동-개발-절차)
- [8. 화면 설명 표준](#8-화면-설명-표준)
- [9. 공통 화면 이용 절차](#9-공통-화면-이용-절차)
  - [9.1 목록 조회](#91-목록-조회)
  - [9.2 상세 조회](#92-상세-조회)
  - [9.3 조치 요청](#93-조치-요청)
- [10. 권한·Data Scope·Masking](#10-권한data-scopemasking)
  - [Permission](#permission)
  - [Data Scope](#data-scope)
  - [Masking](#masking)
- [11. Reason·Approval](#11-reasonapproval)
  - [Reason](#reason)
  - [Approval](#approval)
- [12. Expected Version과 동시성](#12-expected-version과-동시성)
- [13. Timeout과 응답 유실](#13-timeout과-응답-유실)
  - [Timeout 전](#timeout-전)
  - [Timeout 후](#timeout-후)
- [14. 부분 적용](#14-부분-적용)
- [15. 온라인 업무 운영](#15-온라인-업무-운영)
- [16. Batch 운영](#16-batch-운영)
- [17. 설정·배포 상태](#17-설정배포-상태)
- [18. Log·Metric·Trace](#18-logmetrictrace)
  - [Log](#log)
  - [Metric](#metric)
  - [Trace](#trace)
- [19. Audit](#19-audit)
- [20. Export](#20-export)
- [21. 실제 ADM Route·화면 59개](#21-실제-adm-route화면-59개)
  - [21.1 화면별 공통 오류·경계 처리](#211-화면별-공통-오류경계-처리)
- [22. Route·Menu 전수 대조 절차](#22-routemenu-전수-대조-절차)
- [23. Browser Test](#23-browser-test)
- [24. 장애와 정상화 Runbook](#24-장애와-정상화-runbook)
  - [ADM 접속 실패](#adm-접속-실패)
  - [Owner 호출 실패](#owner-호출-실패)
  - [화면과 상태 불일치](#화면과-상태-불일치)
- [25. 개발 인계표](#25-개발-인계표)
- [26. 완료 점검표](#26-완료-점검표)
- [27. Table·Form 작성 기준](#27-tableform-작성-기준)
  - [Table](#table)
  - [Form](#form)
- [28. Reconciliation 절차](#28-reconciliation-절차)
- [29. ADM EDU — 권한별 조회와 응답 유실](#29-adm-edu-권한별-조회와-응답-유실)
  - [29.1 ADM EDU 17개 전수표](#291-adm-edu-17개-전수표)
- [30. 종단간 예제: 지급 실패 거래 조회와 재처리](#30-종단간-예제-지급-실패-거래-조회와-재처리)
  - [30.1 업무 결과](#301-업무-결과)
  - [30.2 선택 기준](#302-선택-기준)
  - [30.3 역할과 권한](#303-역할과-권한)
  - [30.4 시작 전에 결정할 값](#304-시작-전에-결정할-값)
  - [30.5 결과물](#305-결과물)
  - [30.6 단계별 절차](#306-단계별-절차)
  - [30.7 입력·기본값·허용 범위](#307-입력기본값허용-범위)
  - [30.8 정상 결과와 완료 판정](#308-정상-결과와-완료-판정)
  - [30.9 오류·동시성·시간초과·응답 유실·부분 실패](#309-오류동시성시간초과응답-유실부분-실패)
  - [30.10 재시도·재처리·대사·보상·되돌리기](#3010-재시도재처리대사보상되돌리기)
  - [30.11 로그·지표·추적·감사](#3011-로그지표추적감사)
  - [30.12 교육 예제](#3012-교육-예제)
  - [30.13 조직 영역과 CPF 유지 영역](#3013-조직-영역과-cpf-유지-영역)
  - [30.14 운영 인계](#3014-운영-인계)
- [31. 업무 Query 연동](#31-업무-query-연동)
  - [31.1 업무 결과](#311-업무-결과)
  - [31.2 선택 기준](#312-선택-기준)
  - [31.3 역할과 권한](#313-역할과-권한)
  - [31.4 시작 전에 결정할 값](#314-시작-전에-결정할-값)
  - [31.5 결과물](#315-결과물)
  - [31.6 단계별 절차](#316-단계별-절차)
  - [31.7 입력·기본값·허용 범위](#317-입력기본값허용-범위)
  - [31.8 정상 결과와 완료 판정](#318-정상-결과와-완료-판정)
  - [31.9 오류·동시성·시간초과·응답 유실·부분 실패](#319-오류동시성시간초과응답-유실부분-실패)
  - [31.10 재시도·재처리·대사·보상·되돌리기](#3110-재시도재처리대사보상되돌리기)
  - [31.11 로그·지표·추적·감사](#3111-로그지표추적감사)
  - [31.12 교육 예제](#3112-교육-예제)
  - [31.13 조직 영역과 CPF 유지 영역](#3113-조직-영역과-cpf-유지-영역)
  - [31.14 운영 인계](#3114-운영-인계)
- [32. 안전 Command 연동](#32-안전-command-연동)
  - [32.1 업무 결과](#321-업무-결과)
  - [32.2 선택 기준](#322-선택-기준)
  - [32.3 역할과 권한](#323-역할과-권한)
  - [32.4 시작 전에 결정할 값](#324-시작-전에-결정할-값)
  - [32.5 결과물](#325-결과물)
  - [32.6 단계별 절차](#326-단계별-절차)
  - [32.7 입력·기본값·허용 범위](#327-입력기본값허용-범위)
  - [32.8 정상 결과와 완료 판정](#328-정상-결과와-완료-판정)
  - [32.9 오류·동시성·시간초과·응답 유실·부분 실패](#329-오류동시성시간초과응답-유실부분-실패)
  - [32.10 재시도·재처리·대사·보상·되돌리기](#3210-재시도재처리대사보상되돌리기)
  - [32.11 로그·지표·추적·감사](#3211-로그지표추적감사)
  - [32.12 교육 예제](#3212-교육-예제)
  - [32.13 조직 영역과 CPF 유지 영역](#3213-조직-영역과-cpf-유지-영역)
  - [32.14 운영 인계](#3214-운영-인계)
- [33. 승인 기반 위험 조치](#33-승인-기반-위험-조치)
  - [33.1 업무 결과](#331-업무-결과)
  - [33.2 선택 기준](#332-선택-기준)
  - [33.3 역할과 권한](#333-역할과-권한)
  - [33.4 시작 전에 결정할 값](#334-시작-전에-결정할-값)
  - [33.5 결과물](#335-결과물)
  - [33.6 단계별 절차](#336-단계별-절차)
  - [33.7 입력·기본값·허용 범위](#337-입력기본값허용-범위)
  - [33.8 정상 결과와 완료 판정](#338-정상-결과와-완료-판정)
  - [33.9 오류·동시성·시간초과·응답 유실·부분 실패](#339-오류동시성시간초과응답-유실부분-실패)
  - [33.10 재시도·재처리·대사·보상·되돌리기](#3310-재시도재처리대사보상되돌리기)
  - [33.11 로그·지표·추적·감사](#3311-로그지표추적감사)
  - [33.12 교육 예제](#3312-교육-예제)
  - [33.13 조직 영역과 CPF 유지 영역](#3313-조직-영역과-cpf-유지-영역)
  - [33.14 운영 인계](#3314-운영-인계)
- [34. 비동기 Operation과 결과 미확정](#34-비동기-operation과-결과-미확정)
  - [34.1 업무 결과](#341-업무-결과)
  - [34.2 선택 기준](#342-선택-기준)
  - [34.3 역할과 권한](#343-역할과-권한)
  - [34.4 시작 전에 결정할 값](#344-시작-전에-결정할-값)
  - [34.5 결과물](#345-결과물)
  - [34.6 단계별 절차](#346-단계별-절차)
  - [34.7 입력·기본값·허용 범위](#347-입력기본값허용-범위)
  - [34.8 정상 결과와 완료 판정](#348-정상-결과와-완료-판정)
  - [34.9 오류·동시성·시간초과·응답 유실·부분 실패](#349-오류동시성시간초과응답-유실부분-실패)
  - [34.10 재시도·재처리·대사·보상·되돌리기](#3410-재시도재처리대사보상되돌리기)
  - [34.11 로그·지표·추적·감사](#3411-로그지표추적감사)
  - [34.12 교육 예제](#3412-교육-예제)
  - [34.13 조직 영역과 CPF 유지 영역](#3413-조직-영역과-cpf-유지-영역)
  - [34.14 운영 인계](#3414-운영-인계)
- [35. 부분 적용·Reconciliation](#35-부분-적용reconciliation)
  - [35.1 업무 결과](#351-업무-결과)
  - [35.2 선택 기준](#352-선택-기준)
  - [35.3 역할과 권한](#353-역할과-권한)
  - [35.4 시작 전에 결정할 값](#354-시작-전에-결정할-값)
  - [35.5 결과물](#355-결과물)
  - [35.6 단계별 절차](#356-단계별-절차)
  - [35.7 입력·기본값·허용 범위](#357-입력기본값허용-범위)
  - [35.8 정상 결과와 완료 판정](#358-정상-결과와-완료-판정)
  - [35.9 오류·동시성·시간초과·응답 유실·부분 실패](#359-오류동시성시간초과응답-유실부분-실패)
  - [35.10 재시도·재처리·대사·보상·되돌리기](#3510-재시도재처리대사보상되돌리기)
  - [35.11 로그·지표·추적·감사](#3511-로그지표추적감사)
  - [35.12 교육 예제](#3512-교육-예제)
  - [35.13 조직 영역과 CPF 유지 영역](#3513-조직-영역과-cpf-유지-영역)
  - [35.14 운영 인계](#3514-운영-인계)
- [36. 역할별 하루 업무 흐름](#36-역할별-하루-업무-흐름)
  - [36.1 조회자](#361-조회자)
  - [36.2 운영자](#362-운영자)
  - [36.3 승인자](#363-승인자)
  - [36.4 보안 담당자](#364-보안-담당자)
- [37. 화면 장에 반드시 기재할 항목](#37-화면-장에-반드시-기재할-항목)
- [38. Browser·Fault Test 시나리오](#38-browserfault-test-시나리오)
- [39. 교대 인계 양식](#39-교대-인계-양식)

<!-- CPF-TOC:END -->

## 1. ADM의 정체성

ADM은 이용 조직이 새로 개발하는 별도 Framework가 아니다. CPF가 제공하는 운영 제품이다.

- 업무 개발자는 자기 업무 Owner의 Query·Command 계약을 ADM에 연결한다.
- 조회자와 운영자는 같은 ADM 화면을 권한에 따라 다르게 이용한다.
- 승인자는 위험 조치를 검토하고 승인·반려한다.
- 보안 담당자는 Permission·Data Scope·Masking·Audit을 점검한다.
- 플랫폼 운영자는 ADM Process·DB·배포·관측을 운영하지만 업무 원장을 직접 수정하지 않는다.

기존의 “ADM 개발자”와 “ADM 운영자” 문서를 분리하면 ADM 자체를 새로 개발하는 것으로 오해할 수 있으므로, 이 문서에서는 연동과 이용을 하나의 흐름으로 설명한다.

## 2. Owner와 경계

| 영역 | Owner |
|---|---|
| ADM Backend·Frontend·메뉴·사용자 경험 | `cpf-admin` |
| 업무 데이터·상태·업무 Command | 해당 업무 Module |
| Batch 실행 상태·제어 | `cpf-batch` |
| Gateway Route·게시·적용 | `cpf-gateway` |
| 조직·사용자·권한·결재 | `cpf-biz-admin` |
| DB·배포·Secret·관측 | 플랫폼 운영 영역 |

ADM은 다른 Owner의 DB를 직접 수정하지 않는다. Same-JVM Port 또는 Remote API로 조회·조치를 요청한다.

## 3. 기준 Source 위치

```text
cpf-admin/build.gradle
cpf-admin/src/main/**
cpf-admin/frontend/**
cpf-batch/contract/**
cpf-gateway/**
cpf-biz-admin/**
```

기준 Source에서 `cpf-admin`은 WAR Plugin, Web MVC, WebFlux, JDBC, Security Starter와 Batch Contract 의존성을 선언한다. 실제 Route·Operation·Permission은 다음 정본을 함께 대조한다.

- Backend Controller·Service·Owner Port
- OpenAPI
- Generated Operation Contract
- Generated Route Operation Contract
- Orval 기반 Generated API Client
- Frontend Router·Menu·Component
- Permission 검사
- Browser Test

문서가 Source보다 앞서 새로운 Route·Button·Permission을 만들지 않는다.

## 4. 역할별 권한 모델

| 역할 | 기본 행동 | 금지 |
|---|---|---|
| 조회자 | 목록·상세·상태·Log·Metric 조회 | 상태 변경 |
| 운영자 | 허용된 재시도·중지·재개·대사 요청 | 승인 우회·DB 직접 수정 |
| 승인자 | 위험 조치 승인·반려 | 자기 요청 승인 |
| 보안 담당자 | 권한·가림·Audit·Export 점검 | 업무 상태 직접 변경 |
| 운영 관리자 | 메뉴·정책·조치 범위 관리 | 업무 Owner 책임 대체 |
| 업무 개발자 | Query·Command·Operation 계약 연결 | ADM 내부 DB에 업무 원장 복제 |
| ADM 확장 개발자 | 기본 제품으로 해결되지 않는 조직별 UI 확장 | Internal API 직접 참조 |

실제 Permission 문자열과 Scope는 Source에서 확인한다.

## 5. 업무 연동 계약

### 5.1 Query 계약

Query는 다음을 제공한다.

- 검색 Field와 Type
- 기본값
- 정렬
- Paging
- 목록 Column
- 상세 Field
- 상태와 Version
- Data Scope
- Masking
- 조회 시각·데이터 기준 시점
- Timeout
- 오류 Mapping

### 5.2 Command 계약

Command는 다음을 제공한다.

- Command ID
- 대상 ID
- `expectedVersion`
- `idempotencyKey`
- Reason
- Approval 필요 여부
- 입력값·범위
- Operation ID
- 운영 상태
- Timeout·결과 미확정
- Reconcile 방법
- Audit

### 5.3 Operation 조회

조치 응답을 받지 못했을 때 신규 Command를 만들지 않고 Operation ID로 조회한다.

```text
REQUESTED
IN_PROGRESS
WAITING_APPROVAL
WAITING_EXTERNAL
UNKNOWN_RESULT
PARTIAL_SUCCESS
FAILED_RETRYABLE
FAILED_FINAL
SUCCEEDED
```

실제 Owner가 다른 상태를 사용하면 Source 계약을 따른다.

## 6. Same-JVM과 Remote 연결

| 방식 | 사용 시점 | 필수 확인 |
|---|---|---|
| Same-JVM | ADM과 Owner가 같은 Process | Bean 경계·Transaction·Internal 직접 참조 금지 |
| Remote | 별도 WAS·서비스 | 인증·Audience·Deadline·오류·Retry·Trace |
| Read Model | 대량 조회·통합 목록 | 갱신 지연·정본 Owner·Drift |
| Async Command | 오래 걸리는 조치 | Operation·Outbox·결과 조회 |

두 방식은 Permission·Reason·Approval·Expected Version·Idempotency·Audit 의미가 같아야 한다.

## 7. ADM 연동 개발 절차

1. 업무 Owner와 상태·Command를 정한다.
2. ADM에서 필요한 Query·Command 목록을 만든다.
3. Public Port 또는 Remote API를 제공한다.
4. OpenAPI와 Operation Contract를 생성·검증한다.
5. Generated Client를 갱신한다.
6. Route·Menu·Component와 계약을 연결한다.
7. Permission·Data Scope·Masking을 Backend와 Frontend에 적용한다.
8. Reason·Approval·Expected Version을 조치 Form에 연결한다.
9. 정상·권한 거부·Version 충돌·Timeout·응답 유실을 시험한다.
10. Browser에서 Query·Command·Operation·Audit을 확인한다.
11. 운영 담당자에게 상태 판정과 정상화 절차를 인계한다.

## 8. 화면 설명 표준

각 화면은 다음 항목을 문서화한다.

| 항목 | 설명 |
|---|---|
| 메뉴 | 사용자가 찾는 위치 |
| Route | Source와 대조할 식별자 |
| Permission | 조회·조치·승인 권한 |
| 검색 Field | 이름·Type·기본값·범위 |
| Column | 표시값·정렬·가림 |
| 상세 Field | 상태·Version·식별자 |
| State | 화면 표시 상태와 Owner Operation 상태의 대응, 전이 가능 조건 |
| Button | 활성 조건·입력·결과 |
| Reason | 위험 조치 사유 |
| Approval | 정책·만료·승인자 |
| Expected Version | 동시 변경 방지 |
| 응답 유실 | Operation 조회 |
| 부분 적용 | 대상별 결과 |
| Retry·Reprocess | 허용 조건 |
| Reconcile | 원장·대상 대사 |
| Rollback·보상 | Owner가 제공하는 절차 |
| Audit | 요청·승인·결과·이전/이후 값 |

## 9. 공통 화면 이용 절차

### 9.1 목록 조회

1. 메뉴에 접근한다.
2. Permission과 Data Scope가 적용됐는지 확인한다.
3. 기준 시각과 검색 기본값을 확인한다.
4. 상태·기간·업무 Key를 입력한다.
5. 목록 Column의 가림 처리를 확인한다.
6. 총 건수와 Paging을 확인한다.
7. 상세로 이동한다.

정상 결과:

- 권한 범위 안의 데이터만 표시
- 검색 조건과 결과 수 일치
- 민감정보 가림
- 조회 Audit 또는 접근 Log 확인

### 9.2 상세 조회

확인 순서:

1. 업무 식별자
2. 운영 상태
3. Version
4. 최근 Operation
5. 외부 효과·Outbox
6. 오류 분류
7. Log·Trace
8. Audit
9. 허용 조치

### 9.3 조치 요청

1. 운영 상태와 Version을 다시 읽는다.
2. Button 활성 조건을 확인한다.
3. 대상·입력·Reason을 입력한다.
4. Preview가 있으면 범위·건수·영향을 확인한다.
5. 승인 필요 여부를 확인한다.
6. Command를 전송한다.
7. Operation ID를 저장한다.
8. 결과가 늦으면 Operation을 조회한다.
9. 업무 원장·대상·Audit에서 결과를 대사한다.

## 10. 권한·Data Scope·Masking

### Permission

- 메뉴 표시
- Route Guard
- Backend Authorization
- Command Permission
- Approval Permission
- Export Permission

모든 계층이 같은 계약을 사용해야 한다. Frontend Button 숨김만으로 권한을 구현하지 않는다.

### Data Scope

예:

- 전체 조직
- 소속 조직
- 하위 조직
- 담당 업무
- 본인 생성
- 승인 대상

Scope는 Query와 Command 모두에 적용한다.

### Masking

- 목록
- 상세
- Export
- Log
- Audit
- 오류 메시지

권한이 높아져도 원문 표시가 필요한 이유와 Audit을 남긴다.

## 11. Reason·Approval

### Reason

다음 조치에는 사유를 요구한다.

- 재처리
- 강제 중지·재개
- 결과 확정
- 설정 변경
- 대량 대상 조치
- Export
- 권한 변경

### Approval

승인 계약:

- 요청자와 승인자 분리
- 정책 Version
- 승인 유효시간
- 대상·입력 Hash
- 승인 후 변경 금지
- 반려 사유
- 만료
- 중복 승인 방지
- Audit

승인 후 대상 상태나 Version이 바뀌면 재승인을 요구하거나 Fail-closed한다.

## 12. Expected Version과 동시성

조치 Form은 상세 조회 시점의 Version을 전송한다.

충돌 시:

1. 변경된 최신 상태를 다시 조회한다.
2. 기존 입력을 자동 재적용하지 않는다.
3. 사용자에게 변경 내용을 표시한다.
4. 다시 조치할지 판단한다.
5. 필요한 경우 새 Reason·Approval을 받는다.

## 13. Timeout과 응답 유실

### Timeout 전

- 전체 Deadline
- ADM→Owner 호출 Timeout
- Retry 여부
- Operation 생성 시점
- Idempotency Key
- Trace

### Timeout 후

- 같은 Command를 새 Key로 반복하지 않는다.
- Operation ID 또는 Idempotency Key로 조회한다.
- Owner 원장과 외부 효과를 대사한다.
- 성공·실패·결과 미확정을 구분한다.
- 결과 미확정이면 Reconcile Command를 사용한다.

## 14. 부분 적용

대량 조치는 전체 성공만 표시하지 않는다.

| 대상 | 결과 | 다음 행동 |
|---|---|---|
| 성공 | 상태 유지 | 재실행 대상에서 제외 |
| 재시도 가능 실패 | 원인 제거 후 재처리 | 같은 Operation과 대상 Key 유지 |
| 최종 실패 | 사유 확인 | 보상·업무 판단 |
| 결과 미확정 | 원장·대상 대사 | 신규 실행 금지 |
| 권한 제외 | 조치하지 않음 | Scope·정책 확인 |

## 15. 온라인 업무 운영

조회 항목:

- Transaction ID
- Operation ID
- API
- 상태
- 응답 시간
- 오류 분류
- Idempotency
- Version
- Trace
- Audit

조치 예:

- 결과 대사 요청
- Retry 가능 Operation 재시도
- 실패 대상 재처리
- 정책이 허용한 상태 정정 Command

DB 직접 수정이나 상태 강제 변경은 허용하지 않는다.

## 16. Batch 운영

ADM에서 다음을 확인한다.

- Job·Version
- Schedule
- Parameter
- JobExecution·StepExecution
- Progress
- Worker·Lease·Fencing
- Retry·Skip
- 결과 미확정
- 업무 합계 대사
- 재시작·재처리·Abandon
- Approval·Audit

상세 절차는 [02 CPF 배치 개발 매뉴얼](02_CPF_배치개발매뉴얼.md)을 함께 사용한다.

## 17. 설정·배포 상태

ADM이 설정·배포 상태를 표시할 때 확인한다.

- 대상 인스턴스
- Desired Version
- Applied Version
- Checksum
- ACK·NACK
- 부분 적용
- Last Known Good
- Drift
- Reconcile
- Rollback 승인

실제 적용 책임은 각 Product Owner와 플랫폼 운영 영역에 있다.

## 18. Log·Metric·Trace

### Log

- 시간
- Service·Instance
- Transaction·Operation
- Error Code
- Masking
- 배포 Version

### Metric

- 요청량
- 오류율
- 지연
- Backlog
- Retry
- 결과 미확정
- Worker·Lease
- Resource

### Trace

- Parent·Child 관계
- Local·Remote
- 메시지·파일
- 외부기관
- Sampling
- 민감정보

ADM은 관측 데이터를 업무 원장과 동일시하지 않는다. 최종 상태는 Owner의 Operation·업무 원장으로 판정한다.

## 19. Audit

Audit에는 다음이 있어야 한다.

- Actor
- Role·Permission
- Data Scope
- Target
- Action
- Reason
- Approval
- Before·After
- Expected·Actual Version
- Operation
- Result
- Time
- Source IP·Client Context
- Masking·Export 여부

Audit 원문에 Password·Token·Secret을 저장하지 않는다.

## 20. Export

1. Export Permission을 확인한다.
2. 검색 조건과 Scope를 고정한다.
3. 예상 건수·민감정보를 표시한다.
4. Reason·Approval을 받는다.
5. 비동기 생성이면 Operation을 조회한다.
6. 파일 Checksum·만료·다운로드 횟수를 기록한다.
7. Audit과 삭제 정책을 확인한다.

## 21. 실제 ADM Route·화면 59개

아래 Route 59개는 최신 Source의 `cpf-admin/frontend/src/generated/adm-route-operation-contract.ts` Key와 대조한 정적 화면 기준이다. 각 Route가 소비하는 Operation 목록은 생성 계약에서 확인했다. 다만
표의 권한은 역할 범주다. 실제 Permission 문자열은 Source Manifest를 따르며, 문서가 임의 문자열을 만들지 않는다. 모든 조치형 화면은 Reason, 필요 시 Approval, Expected Version, Idempotency Key 또는 Operation ID, Target별 결과와 Audit을 공통으로 확인한다.

| 구분 | Route·화면 | 역할·권한 범주 | 검색·기본값 | 주요 Column·상세 | Button·활성 조건 | 완료 판정 |
|---|---|---|---|---|---|---|
| 통합 운영 | `/`<br>**통합 운영 Dashboard** | 조회자·운영자 | 환경·기간·서비스; 기본 기간은 화면 설정 | 수집 시각·긴급 경보·UNKNOWN_RESULT·PARTIAL_SUCCESS·진행 Operation | 관련 상세 이동·담당자 지정; 상태 변경 없음 | 수집 지연 이내, 미조치 경보의 담당자·다음 확인 시각 기록 |
| 통합 운영 | `/topology`<br>**서비스 토폴로지** | 조회자·운영자 | 환경·서비스·인스턴스 | Instance 상태·의존 방향·버전·마지막 Heartbeat | 상세 이동·점검 전환; 격리는 별도 조치 권한 | 필수 연결 정상, 고립·중복 Instance 조치 기록 |
| 통합 운영 | `/capacity`<br>**Online Runtime Diagnostics** | 조회자·플랫폼 운영자 | 서비스·인스턴스·관찰 기간 | CPU·Memory·Thread·Connection Pool·Queue·오류율·지연 | 진단·Drain 요청·용량 조정 요청 | 임계 초과 원인·담당자·조정 또는 Rollback 기준 기록 |
| 공통 설정 | `/logLevel`<br>**동적 로그** | 운영자·승인자 | 환경·서비스·인스턴스·Logger | 현재 Level·적용 Version·만료·최근 변경자 | Preview 후 기간 제한 변경·원복 | 대상별 Version 일치, 만료 후 원래 Level, Audit 존재 |
| 공통 설정 | `/logPolicies`<br>**로그 정책** | 보안 담당자·승인자 | 채널·업무·데이터 등급·정책 Version | Masking Field·Sampling·보존·반출·대상 | 검증·승인·적용·이전 Version 복원 | 원문 노출 0, 대상 Checksum 일치, 수집량 기준 이내 |
| 공통 설정 | `/channelPolicy`<br>**채널 정책** | 운영자·승인자 | 채널·환경·정책 Version | Timeout·Retry·전문 형식·Encoding·사용 여부 | Preview·승인·적용·중지 | 신규 요청은 새 Version, 진행 요청의 기존 기준 보존 |
| 공통 설정 | `/serviceRegistry`<br>**서비스 레지스트리** | 운영자 | 서비스명·Endpoint·Version·환경 | Health·Weight·지원 기능·마지막 등록 | 등록·중지·Weight 변경·Health 재확인 | 호출 가능 대상만 활성, 설정과 실제 Instance 일치 |
| 공통 설정 | `/runtimeControl`<br>**Deployment·Promotion·Rollback** | 운영자·승인자 | 환경·서비스·배포 Version·대상 | Preview·현재/목표 Version·Health·승인·Target 결과 | 실행·취소·LKG 복원 | Target ACK/NACK·Checksum 판정, Drift 0 또는 비정상 Target 격리 |
| 공통 설정 | `/maintenance`<br>**점검·Drain** | 운영자·승인자 | 서비스·인스턴스·시간·Reason | 활성 Session·진행 Operation·신규 유입·Drain 진행률 | 시작·연장·종료·취소 | 신규 유입 차단, 진행 Operation 정책 종료, 정상 유입 재개 |
| 공통 설정 | `/cache`<br>**캐시** | 운영자 | Cache 이름·Key 범위·서비스 | Hit/Miss·항목 수·원본 Version·TTL·최근 Evict | 범위 Evict·Reload·정책 변경 | 원본과 값 일치, 전면 Evict 부하가 허용 기준 이내 |
| 공통 설정 | `/configs`<br>**설정** | 운영자·승인자 | Key·Profile·환경·대상·Version | 현재값·Default·Secret 여부·재기동·Target 적용 상태 | Preview·승인·적용·Reconcile·Rollback | Target Version 일치, Secret 원문 미노출, 재기동 조건 충족 |
| 공통 설정 | `/responseCodes`<br>**응답코드** | 운영자·업무 책임자 | 업무 영역·코드·적용일 | HTTP Mapping·Retry 분류·사용자 메시지·Consumer API | 신규 Version·종료·복원 | 기존 Consumer 해석 유지, 같은 코드 의미의 비호환 변경 없음 |
| 공통 설정 | `/businessCalendar`<br>**영업일·휴일** | 운영자·승인자 | Calendar ID·지역·기준일·기간 | 영업일·순번·대체 휴일·연결 Schedule/Batch | 등록·변경·재계산 | 다음 실행일·업무 기준일이 Preview와 일치 |
| 공통 설정 | `/notifications`<br>**알림** | 조회자·운영자 | 심각도·상태·담당자·기간 | 발생·갱신 시각·영향 서비스·Operation/Incident | 확인·담당 지정·Escalation·종료 | 미확인 긴급 알림 0, 종료 근거와 Trace/Audit 연결 |
| 공통 설정 | `/downloads`<br>**다운로드·반출** | 반출 권한자·승인자 | Operation ID·File ID·생성자·기간 | 상태·크기·Checksum·만료·권한·Masking | 다운로드·만료·재생성 | Checksum 일치, Permission·Reason·시각 Audit 존재 |
| 공통 설정 | `/codes`<br>**공통 코드** | 운영자·업무 책임자 | 그룹·코드·기준일·Version | 명칭·사용 여부·유효기간·상위 코드·Consumer | 등록·변경·종료·복원 | 유효기간 중복 0, Consumer 조회 의미 일치 |
| 온라인·연계 | `/logs`<br>**거래 로그** | 조회자 | 거래 ID·Request ID·Trace ID·기간 | 요청 구간·응답 코드·업무 오류·DB·Masking·수집 시각 | 상세·통합 Trace·Incident 연결 | 같은 Trace로 전 구간 연결, 누락 구간 원인 설명 |
| 온라인·연계 | `/transactionGroups`<br>**Online·Batch 통합 Trace** | 조회자 | 거래 ID·Trace ID·업무 ID | 부모/자식 Span·Batch·Message·File 연결·지연 | 문제 구간의 담당 화면 이동 | 최초 요청부터 최종 결과까지 순서 연결, 중복 구간 식별 |
| 온라인·연계 | `/transactions`<br>**거래 메타** | 조회자·운영자 | 서비스·API·상태·기간·Business Key | 요청/응답 시각·결과 코드·Version·멱등 Key·Operation ID | 상세·결과 미확정 화면 이동 | 업무 원장과 상태·Version·식별자 일치 |
| 온라인·연계 | `/remoteLogs`<br>**원격 로그** | 조회자·반출 권한자 | 서비스·인스턴스·Level·기간 | Collector 상태·원격 위치·마지막 수집·Trace·Masking | 조회·승인된 반출·수집 장애 등록 | 원본과 중앙 수집 기간·Hash 일치, 누락 0 또는 사유 기록 |
| 온라인·연계 | `/standardExecutions`<br>**표준 실행** | 조회자·운영자 | Operation ID·Business Key·멱등 Key·상태 | State·Request Hash·Expected Version·Attempt·Target·Outbox | 허용 State에서 Retry·Reconcile·Cancel | 최종 State와 업무 원장·Target·Audit 일치 |
| 온라인·연계 | `/file-jobs`<br>**대량파일 Job** | 조회자·운영자 | File ID·Job명·Execution ID·기준일 | Checksum·검사·격리·읽기/성공/실패·Checkpoint | Stop·Restart·실패 Row 재처리·결과 File | 입력=성공+실패+제외, Checksum 보존 |
| 온라인·연계 | `/messages`<br>**전문·Protocol Message** | 조회자·운영자 | Message ID·거래 ID·기관·기간 | 방향·전문 Version·Masking·응답 코드·처리 시각·Trace | 상세·거래 Trace; 재전송은 정상화 화면에서 | 상관 Key 일치, 원문 Permission·Masking 준수 |
| 신뢰성·감사 | `/auditLogs`<br>**감사 로그** | 감사 담당자·반출 권한자 | Actor·Action·Target·기간·Trace | Before/After·Reason·Approval ID·Request/Operation·결과 | 조회·승인된 반출 | 업무 변경과 Audit 건수·시각·Actor 일치 |
| 신뢰성·감사 | `/recoveryCenter`<br>**정상화 센터** | 운영자·승인자 | Operation ID·상태·Business Key·장애 유형 | Commit·외부 효과·실패 Target·Outbox·Attempt·허용 조치 | Retry·Reconcile·Compensation·Cancel | 중복 실행 없이 상태 확정, Target·원장·Audit 대사 |
| 신뢰성·감사 | `/incidents`<br>**Error·Unknown Result** | 운영자·승인자 | 상태·서비스·Version·기간·Business Key | UNKNOWN_RESULT·DLQ·정상화 이력·영향 건수·배포 상관 | Incident 생성·DLQ 재처리·결과 확정·정상화 | 영향·원인 기록, 동일 업무 효과 중복 0 |
| 신뢰성·감사 | `/reliability`<br>**Analysis Center** | 조회자·운영 관리자 | 서비스·Version·상태·기간 | 상태 분포·반복 오류·멱등 충돌·Outbox 지연·외부 실패 | Incident·정상화 화면 연결 | 상위 원인·영향 서비스·담당 조치 연결 |
| 배치 | `/batch`<br>**Batch / Center-Cut** | 배치 조회자·운영자·승인자 | 기준일·Job·상태 | 오늘 실행·지연·실패·Worker·Center-Cut·Alert | 세부 화면 이동 | 누락·지연·실패가 담당 화면과 담당자에 연결 |
| 배치 | `/batch-overview`<br>**Batch Overview** | 배치 조회자·운영자·승인자 | 업무일·Job·상태·Job Pack Version | 예정/실제 시작·종료·건수·실패 Step·다음 실행 | Execution·Scheduler·Recovery 이동 | 필수 Job 실행 여부와 지연 사유 설명 |
| 배치 | `/batch-runtime`<br>**Runtime Topology** | 배치 조회자·운영자·승인자 | 역할·Instance·환경 | Control·Scheduler·Worker·Center-Cut·Agent·Primary | Drain·Instance 상세 | 필수 역할 가동, 중복 Primary 0, Queue 처리 |
| 배치 | `/batch-instances`<br>**Runtime Instances** | 배치 조회자·운영자·승인자 | 역할·Instance·Version·상태 | Heartbeat·실행 Job·용량·Version·최근 오류 | Drain·Stop·복귀 확인 | 종료 대상 신규 할당 0, 기존 Job 인계 완료 |
| 배치 | `/batch-scheduler`<br>**Scheduler HA** | 배치 조회자·운영자·승인자 | Schedule ID·Job·Calendar·상태 | 다음 실행·Primary·Misfire·중복 정책·Parameter | Pause·Resume·변경·보충·건너뛰기 | 예정 시각과 실행 요청 일치, 중복 0 |
| 배치 | `/batch-worker-pools`<br>**Worker Pools** | 배치 조회자·운영자·승인자 | Pool·Job·환경 | 가용/사용 Worker·Queue·처리율·Partition 편향·자원 | Scale·Drain·Job Pack 제한 | Queue 감소, 특정 Worker/Partition 편향 해소 |
| 배치 | `/batch-center-cut`<br>**Center-Cut** | 배치 조회자·운영자·승인자 | Center-Cut ID·조건 Version·기준일 | Snapshot·Checksum·건수·금액·표본·Approval·차이 | Preview·Approval·Run·Stop·차이 보정·재실행 | 승인 Snapshot=실제 대상, 대사 차이 0 |
| 배치 | `/batch-agents`<br>**Host Agents** | 배치 조회자·운영자·승인자 | Host·Agent·상태 | Heartbeat·Version·Disk·실행 Command·최근 응답 | Command 결과 조회·Reconnect·Maintenance | 같은 Command ID로 결과 확인, 중복 실행 0 |
| 배치 | `/batch-job-packs`<br>**Job Packs** | 배치 조회자·운영자·승인자 | Pack·Version·상태 | Artifact Checksum·지원 CPF·포함 Job·활성 Version·LKG | 등록·검증·활성·중지·LKG 선택 | Instance가 승인 Checksum 사용, 호환성 확인 |
| 배치 | `/batch-executions`<br>**Executions** | 배치 조회자·운영자·승인자 | Execution ID·Job·업무일·상태 | Parameter·Step·Checkpoint·Read/Write/Skip·Error | Stop·Restart·Abandon·상세 | Commit 범위 중복 0, 건수·금액 대사 |
| 배치 | `/batch-deployment`<br>**Deployment / Rollback** | 배치 조회자·운영자·승인자 | 환경·Pack Version·Target | Preview·Target·ACK/NACK·Checksum·Health·LKG | Deploy·Promote·Stop·Rollback | Target Version·Checksum 일치, 신규 Execution 목표 Version |
| 배치 | `/batch-recovery`<br>**Recovery / Unknown** | 배치 조회자·운영자·승인자 | Execution ID·Job·장애 유형 | 마지막 Commit·Checkpoint·외부 효과·실패 Item·Attempt | Restart·Reconcile·실패 Item 재처리·Abandon | 성공 범위 유지, 실패 범위만 처리, 전체 대사 |
| 배치 | `/batch-leases`<br>**Lease / Fencing** | 배치 조회자·운영자·승인자 | Lease Key·Owner·상태 | 만료·Owner Instance·Token·Heartbeat·대기 Job | 소유권 상실 확인·만료 후 재할당 | 유효 Owner 하나, 과거 Token 결과 차단 |
| 배치 | `/batch-alerts`<br>**Batch Alerts** | 배치 조회자·운영자·승인자 | 심각도·Job·상태·담당자 | 발생·갱신·중복·영향 Execution·Rule | 확인·담당 지정·Escalation·종료 | 미확인 Alert 0, 종료 근거와 Execution ID 기록 |
| 배치 | `/batch-audit`<br>**Audit / Evidence** | 배치 조회자·운영자·승인자 | Execution ID·Job·Actor·기간 | 배포·실행·Stop·Restart·Approval·Reconcile·Export | 조회·승인된 Evidence 반출 | 모든 위험 조치가 Reason·Approval·Before/After 연결 |
| 배치 | `/workers`<br>**Agent / Worker** | 배치 조회자·운영자·승인자 | Worker·Agent·Partition ID | Heartbeat·Job·Lease·Token·처리율·최근 오류 | Drain·Partition 재할당·상태 재확인 | 소유권 이전 후 과거 쓰기 차단, 결과 중복 0 |
| Gateway | `/gateway-dashboard`<br>**Gateway 대시보드** | Gateway 조회자·운영자·보안 담당자·승인자 | 환경·기간·서버 Group·Route | Health·오류율·지연·Circuit·부분 적용·Drift | Server·Route·Transaction·Apply 이동 | 긴급 오류·부분 적용 담당자·정상화 계획 지정 |
| Gateway | `/gateway-servers`<br>**Gateway 연동 서버** | Gateway 조회자·운영자·보안 담당자·승인자 | Server ID·환경·상태 | Endpoint·TLS·DNS·Health·Weight·최근 오류 | 등록·중지·연결 시험·Certificate 교체 | 허용 Endpoint만 활성, TLS·DNS·Health 정상 |
| Gateway | `/gateway-groups`<br>**Gateway 서버 Group** | Gateway 조회자·운영자·보안 담당자·승인자 | Group ID·환경·상태 | Member·Weight·LB 방식·최소 정상 수·Failover | 구성·Weight 변경·비정상 제외·복귀 | 정상 Member만 요청 수신, Weight·Failover 의도 일치 |
| Gateway | `/gateway-routes`<br>**Gateway Route·Routing** | Gateway 조회자·운영자·보안 담당자·승인자 | Route ID·Host·Path·Method·Version | Predicate·Rewrite·Target Group·Priority·Timeout·Approval | Draft·Validate·Approval·Publish·Disable | 중복 Route 0, Probe가 목표 Target/Rewrite와 일치 |
| Gateway | `/gateway-security`<br>**Gateway 보안·제한** | Gateway 조회자·운영자·보안 담당자·승인자 | Route·Client·정책 Version | Audience·Permission·HMAC Key·Body Hash·Nonce·Allowlist·TLS | Validate·Approval·Apply·Key Rotate·Block | 인증 실패 분류, Replay·SSRF·내부망 우회 차단 |
| Gateway | `/gateway-health`<br>**Gateway Health·연결시험** | Gateway 조회자·운영자·보안 담당자·승인자 | Server·Group·Route·환경 | DNS·TCP·TLS·Probe 응답·지연·최근 실패 | Probe·재확인·Incident 연결 | 직접 Target과 Gateway 경유 결과 일치, 실패 구간 식별 |
| Gateway | `/gateway-transactions`<br>**Gateway 거래 조회** | Gateway 조회자·운영자·보안 담당자·승인자 | Request ID·Trace ID·Route ID·상태·기간 | Target·Attempt·Circuit·전체 지연·응답 코드·Masking | 거래·Log·Target Trace 이동 | 같은 Trace로 Gateway와 Target 결과 연결, 원문 노출 0 |
| Gateway | `/gateway-log-policies`<br>**Gateway 로그 정책** | Gateway 조회자·운영자·보안 담당자·승인자 | Route·데이터 등급·정책 Version | Header/Body 수집·Masking·Sampling·보존·반출 | Preview·Approval·Apply·Rollback | 민감정보 원문 0, Instance 정책 Checksum 일치 |
| Gateway | `/gateway-apply-status`<br>**Gateway 적용 상태·이력** | Gateway 조회자·운영자·보안 담당자·승인자 | Bundle Version·Checksum·환경 | Instance ACK/NACK·현재 Version·Drift·LKG·Attempt | 실패 Instance Reconcile·재적용·LKG | 활성 Instance 승인 Version/Checksum 일치 또는 격리 |
| 권한·보안 | `/permissions`<br>**권한** | 보안 담당자·운영 관리자·승인자 | 사용자·Role·Permission·Data Scope·기준일 | 직접/상속·유효기간·직무분리·원문/반출 | 부여·회수·유효기간·Simulation | 실효 권한=승인 내용, 충돌 0, Session 반영 |
| 권한·보안 | `/password`<br>**비밀번호** | 보안 담당자·운영 관리자·승인자 | 계정·상태 | Lock·만료·실패 횟수·MFA·Session·마지막 변경 | 초기화·Lock 해제·Session 종료 | 기존 비밀번호 미노출, 새 인증 후 이전 Session 무효 |
| 권한·보안 | `/security`<br>**보안** | 보안 담당자·운영 관리자·승인자 | Incident·계정·IP·기간 | 인증 실패·차단·Session·Key 사용·원문 조회·위험 조치 | 계정/IP/Session 차단·Key Rotate 요청·Incident 연결 | 위험 접근 차단, 영향 Session·Key·Audit 범위 확인 |
| 권한·보안 | `/operators`<br>**운영자** | 보안 담당자·운영 관리자·승인자 | 운영자·상태·Role·조직 | 개인 계정·실효 권한·최근 Login·Lock·유효기간 | 등록·비활성·Role 변경·Session 종료 | 공유 계정 0, 이동·퇴직 권한/Session 회수 |
| 권한·보안 | `/secrets`<br>**Secret / Key** | 보안 담당자·운영 관리자·승인자 | Secret ID·Certificate ID·Consumer | Version·만료·Active/Standby·최근 Rotate·대상·실패 | 신규 Version·Rotate·Activate·이전 폐기 | 원문 미표시, 신규 사용 확인 후 이전 Version 정책 폐기 |
| 권한·보안 | `/approvals`<br>**위험조치 승인** | 보안 담당자·운영 관리자·승인자 | Approval ID·위험도·요청자·상태·만료 | Diff·Target·Reason·검증·Expected Version·자기 승인 | Approve·Reject·Cancel·Expire | 요청자/승인자 분리, 승인 범위·시간 내 실행 |
| 권한·보안 | `/breakGlass`<br>**Break-glass** | 보안 담당자·운영 관리자·승인자 | Incident ID·요청자·Scope·종료 시각 | 사전 승인·허용 Permission·환경·Session·남은 시간 | Activate·조기 종료·권한/Session 회수·사후 검토 | 종료 시 권한/Session 회수, 모든 조회·조치 별도 Audit |

### 21.1 화면별 공통 오류·경계 처리

- 검색 Timeout: 조회 조건과 기준 시각을 보존하고 Trace ID로 Backend 처리 여부를 확인한다.
- Command 응답 유실: 같은 Button을 다시 누르지 않고 Request ID·Idempotency Key·Operation ID로 기존 작업을 조회한다.
- Version 충돌: 최신 상세를 다시 읽고 변경 차이를 검토한 뒤 새 Expected Version으로 다시 요청한다.
- 부분 적용: 성공 Target은 유지하고 NACK·미응답 Target만 Reconcile하거나 재적용한다.
- Permission 불일치: Frontend Button 숨김만으로 통제하지 않고 Backend 거부와 Audit을 함께 확인한다.
- Masking 불일치: 목록·상세·Export·Log에서 같은 Data Scope와 Masking 정책을 적용한다.


## 22. Route·Menu 전수 대조 절차

```text
Backend Controller
→ OpenAPI Operation
→ Generated Operation Contract
→ Generated Route Contract
→ Frontend Router
→ Menu
→ Component
→ Permission
→ Browser Test
```

전수 대조 결과:

- Controller만 있고 화면이 없으면 제공
- 화면만 있고 Backend가 없으면 제공
- Permission 불일치면 실패
- Generated Client가 Source OpenAPI와 다르면 원본을 수정하고 재생성
- Browser Test에서 Route·Permission·Form·Operation·Audit를 함께 확인

## 23. Browser Test

필수 시나리오:

- 로그인·Session 만료
- 메뉴 Permission
- Data Scope
- Masking
- 검색 기본값
- Paging·정렬
- 상세 상태·Version
- Button 활성 조건
- Reason
- Approval
- Version 충돌
- Timeout
- 응답 유실
- 부분 적용
- Retry·Reprocess
- Reconcile
- Export
- Audit


## 24. 장애와 정상화 Runbook

### ADM 접속 실패

1. Process·Health 확인
2. Session·인증 Provider 확인
3. DB·Owner API 확인
4. 최근 배포·설정 확인
5. Log·Trace 확인
6. Read-only 조회 가능 여부 확인
7. 원인 제거 후 기능 Smoke Test

### Owner 호출 실패

1. 대상 Service·Instance 확인
2. Deadline·Circuit 상태 확인
3. Operation 생성 여부 확인
4. 업무 원장 확인
5. 결과 미확정이면 Reconcile
6. 신규 Command 중복 전송 금지

### 화면과 상태 불일치

1. 조회 기준 시각 확인
2. Read Model 지연 확인
3. Owner 원장 조회
4. Drift Reconcile
5. Generated Client·Contract Version 확인
6. Audit 기록

## 25. 개발 인계표

| 항목 | 내용 |
|---|---|
| 업무 Owner | Module·담당팀 |
| Query | 목록·상세·필드·기본값 |
| Command | 입력·상태·Version |
| Permission | 조회·조치·승인·Export |
| Data Scope | 조직·업무 범위 |
| Masking | 필드·조건 |
| Reason·Approval | 정책·만료 |
| Operation | 상태·조회 |
| Timeout | 전체·하위 |
| Reconcile | 판정 절차 |
| Log·Metric·Trace | 이름·Label |
| Audit | 저장 항목 |
| Test | Contract·Browser·Fault |
| 제공 | 환경·시나리오 |

## 26. 완료 점검표

- [ ] ADM을 업무 제품 자체의 개발 영역으로 설명하지 않았다.
- [ ] 업무 Owner와 ADM Owner가 분리됐다.
- [ ] Route·Menu·Controller·Generated Contract가 일치한다.
- [ ] Permission·Data Scope·Masking이 Frontend·Backend에 함께 적용됐다.
- [ ] Reason·Approval·Expected Version이 위험 조치에 적용됐다.
- [ ] Timeout·응답 유실·부분 적용·결과 미확정 절차가 있다.
- [ ] Retry·Reprocess·Reconcile·보상·Rollback Owner가 명확하다.
- [ ] Log·Metric·Trace·Audit로 결과를 확인할 수 있다.
- [ ] Browser·Fault Test 실행 여부를 사실대로 기록했다.

## 27. Table·Form 작성 기준

ADM 화면 문서는 실제 Component와 Generated Client를 기준으로 다음을 전수 기록한다.

### Table

| 항목 | 기록 내용 |
|---|---|
| 검색 Field | 이름·Type·기본값·필수·Range |
| Column | 필드·정렬·표시 형식·Masking |
| Page | Page Size·Cursor·Total 여부 |
| 상태 표시 | 실제 상태값·Label·색상 의미 |
| Row Action | Permission·활성 조건·Expected Version |
| Empty/Error | 빈 결과·권한 없음·Timeout 표시 |

### Form

| 항목 | 기록 내용 |
|---|---|
| 입력 Field | Type·Default·필수·Validation |
| Reason | 최소 길이·민감정보 금지 |
| Approval | 필요 조건·정책 Version·만료 |
| Target | 단일·다중 대상과 Preview |
| Expected Version | 조회 시점 Version 전달 |
| Submit | Idempotency Key·중복 클릭 방지 |
| 결과 | Operation ID·상태·Target별 결과 |

화면에 존재하지 않는 필드·Button·상태를 예시로 추가하지 않는다.

## 28. Reconciliation 절차

ADM 조회 결과와 Owner 원장이 다를 때 다음 순서로 판정한다.

1. 조회 기준 시각과 Read Model 지연을 확인한다.
2. `operationId`, `businessKey`, `traceId`, `expectedVersion`을 수집한다.
3. Owner Query로 현재 원장 상태를 확인한다.
4. Outbox·Inbox·Batch Metadata·Target 결과를 확인한다.
5. 결과가 확정되면 ADM Read Model을 Reconcile한다.
6. 결과가 확정되지 않으면 신규 Command를 만들지 않고 `UNKNOWN_RESULT`로 유지한다.
7. 운영 확정·보상·Rollback은 Owner의 공개 Command와 승인 절차를 사용한다.
8. 최종 상태·Version·Audit·Trace가 일치할 때 종료한다.

## 29. ADM EDU — 권한별 조회와 응답 유실

1. 조회자·운영자·승인자·보안 담당자 Test 계정을 준비한다.
2. 같은 업무 Operation을 역할별로 조회해 Field·Masking·Button 차이를 확인한다.
3. 운영자가 위험 Command를 요청하고 Reason·Expected Version을 입력한다.
4. 승인자가 Approval을 처리한다.
5. Owner 응답을 지연시켜 Timeout을 재현한다.
6. ADM에서 신규 Command를 만들지 않고 Operation을 조회한다.
7. Owner 원장과 Target 결과를 Reconcile한다.
8. 부분 적용이면 실패 Target만 재처리한다.
9. Audit에서 요청자·승인자·Permission·Reason·Before/After를 확인한다.
10. Browser 새로고침·재로그인 후 상태가 유지되는지 확인한다.

### 29.1 ADM EDU 17개 전수표

교육 기능은 ADM 자체를 새로 개발하는 예제가 아니라 업무 기능을 ADM에 연결하고 권한별로 이용하는 절차다. 실행 전 기능 Catalog의 `sourcePath`, Owner Consumer, Route Operation Contract와 Browser Test를 같은 Commit에서 대조한다.

| 교육 ID | 확인할 기능 | 역할 | 활성 조건 | 실행 안내 | 완료 판정 |
|---|---|---|---|---|---|
| `EDU-ADM-01` | 기존 ADM 기능 재사용 판단 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-02` | 업무 Query 연동 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-03` | 상태 기반 안전 조치 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-04` | Approval이 필요한 위험 조치 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-05` | 비동기 Operation·응답 유실 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-06` | 부분 성공·대상별 정상화 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-07` | 전용 화면 추가의 마지막 선택 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-08` | Permission·Data Scope·Masking·Reason 연동 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-09` | Expected Version 충돌·재조회·재적용 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-10` | 대상 일괄 조치·부분 성공·결과 파일 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-11` | Config·Feature Flag·Maintenance Window 운영 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-12` | Incident·정상화 Center 종단간 처리 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-13` | Audit Evidence·Download·승인 Export | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-14` | Topology·Health·Capacity 상세 이동 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-15` | Log·Trace·Transaction Correlation 검색 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-16` | Notification 확인·Escalation·교대 인계 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |
| `EDU-ADM-17` | Browser Session 만료·재로그인·위험 조치 중복 방지 | `CPF_ADM_OPERATOR` | `cpf.reference.features.operations.enabled` | 공통 EDU 실행 계약과 해당 ID | 정상 응답·상태·로그·감사·복구 확인 |

## 30. 종단간 예제: 지급 실패 거래 조회와 재처리

### 30.1 업무 결과

운영자가 지급 실패 거래를 검색하고 업무 상태·외부 Attempt·승인·감사를 확인한 뒤 허용된 실패 대상만 재처리하고 결과를 대사한다.

### 30.2 선택 기준

Owner 업무가 거래 조회·재처리·수동 확정을 제공할 때 사용한다. ADM이 Owner DB를 직접 변경하거나 임의 상태를 생성하는 방식은 사용하지 않는다.

### 30.3 역할과 권한

조회자는 검색·상세만, 운영자는 안전 조치, 승인자는 위험 조치 승인, 보안 담당자는 원문·반출, 운영관리자는 정책·권한을 담당한다.

### 30.4 시작 전에 결정할 값

업무 Key, 검색 기본 기간, 상태·오류 분류, 재처리 가능 상태, Permission, Data Scope, Masking, Reason, Approval, Expected Version, 결과 대사 기준을 정한다.

### 30.5 결과물

Owner Query/Command API, OpenAPI·Generated Client, Route/Menu/Component, Permission Matrix, Browser/Fault Test, 운영 Runbook.

### 30.6 단계별 절차

1. `/transactions` 또는 업무 연결 화면에서 최근 실패 상태와 업무 Key를 검색한다.
2. 목록의 상태·Version·오류·마지막 Attempt 시각을 확인한다.
3. 상세에서 업무 원장, 외부 Attempt, Outbox/Inbox, 승인, Audit Timeline을 같은 Correlation ID로 조회한다.
4. 재처리 전에 Owner가 제공한 Preview에서 대상 수·현재 Version·이미 성공한 대상을 확인한다.
5. 실패 원인이 일시적이고 재처리 가능 상태인지 확인한다.
6. 사유를 입력하고 필요한 경우 승인 요청을 생성한다.
7. Expected Version과 Idempotency Key를 포함해 Command를 한 번 전송한다.
8. 202 응답이면 Operation ID를 저장하고 진행 상태를 조회한다.
9. 응답 유실이면 같은 버튼을 다시 누르지 않고 Operation·Audit를 검색한다.
10. 대상별 결과에서 성공·실패·미확정을 분리하고 실패·미확정만 Reconcile한다.
11. 최종 업무 상태, 외부 결과, Version, Audit가 일치하면 종료한다.

### 30.7 입력·기본값·허용 범위

| 입력 | 기본값 | 허용 범위 | 비고 |
|---|---|---|---|
| 조회 기간 | 최근 1시간 | 역할별 최대 기간 | 대량 조회는 Export |
| 상태 | 실패·미확정 | 화면 제공 상태 | 업무 상태와 Operation 상태 구분 |
| 사유 | 없음 | 정책 최소 길이 | 개인정보 입력 금지 |
| Expected Version | 상세의 현재 값 | 0 이상 | 오래된 값은 409 |
| Idempotency Key | 화면 생성 | 같은 조치에 재사용 | 다른 요청 본문 재사용 금지 |

### 30.8 정상 결과와 완료 판정

Owner 상태와 ADM 표시가 일치하고, 한 Operation에 대상별 결과·Audit·Trace가 연결된다. 성공 대상을 재실행하지 않고 최종 미확정 수가 0이다.

### 30.9 오류·동시성·시간초과·응답 유실·부분 실패

권한 없음, Data Scope 밖 대상, 409 Version 충돌, Owner Timeout, 202 응답 유실, 일부 대상 성공, 승인 만료, Session 만료를 구분한다.

### 30.10 재시도·재처리·대사·보상·되돌리기

409는 재조회·Preview·재승인을 수행한다. Timeout/응답 유실은 Operation을 조회한다. 부분 성공은 실패·미확정 대상만 Reconcile한다. 잘못된 조치는 Owner의 Compensation/Rollback Command를 사용한다.

### 30.11 로그·지표·추적·감사

Route, Actor, Permission, Data Scope, Business Key, Operation ID, Target ID, Expected/Actual Version, Reason, Approval ID, Trace ID, Result를 기록한다.

### 30.12 교육 예제

`EDU-ADM-03`, `EDU-ADM-04`, `EDU-ADM-05`, `EDU-ADM-06`, `EDU-ADM-09`를 연결해 Browser와 Fault 시나리오를 실행한다.

### 30.13 조직 영역과 CPF 유지 영역

업무 상태·재처리 규칙·대사 기준은 Owner 업무 영역이다. ADM의 권한·사유·승인·Operation·Audit 표시 규칙은 CPF가 유지한다.

### 30.14 운영 인계

메뉴·Route·Permission, 검색 기본값, 상태·버튼 활성 조건, Reason/Approval, Operation 조회, 대사·Rollback 책임자를 인계한다.


## 31. 업무 Query 연동

### 31.1 업무 결과

업무 목록·상세·집계 결과를 권한·범위·가림과 함께 ADM에 표시한다.

### 31.2 선택 기준

조회 전용 기능에는 변경·승인·Rollback 버튼을 만들지 않는다.

### 31.3 역할과 권한

ADM 연동 개발자·운영자·승인자·보안 담당자의 Permission을 분리한다.

### 31.4 시작 전에 결정할 값

검색 필드·기본값·정렬·열·상세·Page Size·Data Scope·Masking을 정한다.

### 31.5 결과물

Owner API·OpenAPI·Generated Client·Route·Component·Permission·Test·Runbook.

### 31.6 단계별 절차

Owner Query API를 OpenAPI에 등록하고 Generated Client를 생성한다. Route Component가 Query를 호출하고 Loading·Empty·Error·Stale 상태를 구분한다.

### 31.7 입력·기본값·허용 범위

실제 화면 Field와 Owner DTO를 사용하며 기본값·허용 범위·Masking을 문서화한다.

### 31.8 정상 결과와 완료 판정

목록·상세·집계가 Owner와 일치하고 권한 밖 데이터가 없다.

### 31.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Validation·409·Timeout·응답 유실·Session 만료·부분 성공을 독립 상태로 표시한다.

### 31.10 재시도·재처리·대사·보상·되돌리기

Timeout은 Query 재시도가 가능하나 Snapshot이 필요한 경우 기준시각을 고정한다. Stale 표시는 Source Version과 조회시각을 보여 준다.

### 31.11 로그·지표·추적·감사

Route·Operation·Business/Target ID·Version·Reason·Approval·Trace·Audit를 기록한다.

### 31.12 교육 예제

`EDU-ADM-02·14·15`를 실행해 정상·오류·Browser 재접속·응답 유실을 검증한다.

### 31.13 조직 영역과 CPF 유지 영역

업무 Query/Command와 상태는 Owner 영역이다. ADM UI·권한·Operation·Audit 표준은 CPF가 유지한다.

### 31.14 운영 인계

Route, Permission, 필드·버튼·상태, Owner Endpoint, Timeout, 대사·Rollback을 전달한다.


## 32. 안전 Command 연동

### 32.1 업무 결과

Cache Refresh·Session 종료·실패건 재처리처럼 제한된 상태 변경을 Owner Command로 실행한다.

### 32.2 선택 기준

업무 상태를 바꾸는 모든 조치는 Owner Command가 있어야 하며 ADM DB 직접 갱신은 금지한다.

### 32.3 역할과 권한

ADM 연동 개발자·운영자·승인자·보안 담당자의 Permission을 분리한다.

### 32.4 시작 전에 결정할 값

Permission, Reason, Expected Version, Idempotency, Preview, Operation 상태를 정한다.

### 32.5 결과물

Owner API·OpenAPI·Generated Client·Route·Component·Permission·Test·Runbook.

### 32.6 단계별 절차

Component가 Preview 후 Command를 한 번 호출하고 200/202를 구분한다. 202는 Operation Polling 또는 Event Stream으로 완료를 확인한다.

### 32.7 입력·기본값·허용 범위

실제 화면 Field와 Owner DTO를 사용하며 기본값·허용 범위·Masking을 문서화한다.

### 32.8 정상 결과와 완료 판정

Owner 결과·화면·Audit가 일치하고 중복 클릭이 새 작업을 만들지 않는다.

### 32.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Validation·409·Timeout·응답 유실·Session 만료·부분 성공을 독립 상태로 표시한다.

### 32.10 재시도·재처리·대사·보상·되돌리기

응답 유실은 Operation 조회, 409는 재조회, 부분 성공은 대상별 Reconcile을 수행한다.

### 32.11 로그·지표·추적·감사

Route·Operation·Business/Target ID·Version·Reason·Approval·Trace·Audit를 기록한다.

### 32.12 교육 예제

`EDU-ADM-03·05·09`를 실행해 정상·오류·Browser 재접속·응답 유실을 검증한다.

### 32.13 조직 영역과 CPF 유지 영역

업무 Query/Command와 상태는 Owner 영역이다. ADM UI·권한·Operation·Audit 표준은 CPF가 유지한다.

### 32.14 운영 인계

Route, Permission, 필드·버튼·상태, Owner Endpoint, Timeout, 대사·Rollback을 전달한다.


## 33. 승인 기반 위험 조치

### 33.1 업무 결과

대량 삭제·설정 게시·강제 재처리·LKG 복구처럼 영향이 큰 조치를 Preview·승인 후 실행한다.

### 33.2 선택 기준

복구가 어렵거나 다수 대상·보안·금액 영향이 있는 조치에 적용한다.

### 33.3 역할과 권한

ADM 연동 개발자·운영자·승인자·보안 담당자의 Permission을 분리한다.

### 33.4 시작 전에 결정할 값

Approval Policy, 대상 Snapshot, Version/Checksum, 만료, 승인자 분리, Rollback을 정한다.

### 33.5 결과물

Owner API·OpenAPI·Generated Client·Route·Component·Permission·Test·Runbook.

### 33.6 단계별 절차

요청자가 Preview와 사유로 승인 요청을 만들고 승인자가 차이·영향·Rollback을 검토한다. 실행자는 승인 ID와 Snapshot Hash로 Command를 실행한다.

### 33.7 입력·기본값·허용 범위

실제 화면 Field와 Owner DTO를 사용하며 기본값·허용 범위·Masking을 문서화한다.

### 33.8 정상 결과와 완료 판정

승인 대상과 실행 대상·Version·Checksum이 같고 승인자와 실행자가 정책에 맞게 분리된다.

### 33.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Validation·409·Timeout·응답 유실·Session 만료·부분 성공을 독립 상태로 표시한다.

### 33.10 재시도·재처리·대사·보상·되돌리기

승인 후 대상 변경은 재승인한다. 승인 만료·부분 적용·실행 응답 유실은 Approval과 Operation을 대사한다.

### 33.11 로그·지표·추적·감사

Route·Operation·Business/Target ID·Version·Reason·Approval·Trace·Audit를 기록한다.

### 33.12 교육 예제

`EDU-ADM-04·10·11·13`를 실행해 정상·오류·Browser 재접속·응답 유실을 검증한다.

### 33.13 조직 영역과 CPF 유지 영역

업무 Query/Command와 상태는 Owner 영역이다. ADM UI·권한·Operation·Audit 표준은 CPF가 유지한다.

### 33.14 운영 인계

Route, Permission, 필드·버튼·상태, Owner Endpoint, Timeout, 대사·Rollback을 전달한다.


## 34. 비동기 Operation과 결과 미확정

### 34.1 업무 결과

장시간 조치를 202 Operation으로 실행하고 진행·대상·취소·결과 대사를 제공한다.

### 34.2 선택 기준

수 초 이상 걸리거나 다수 대상·외부 부수 효과가 있는 조치에 사용한다.

### 34.3 역할과 권한

ADM 연동 개발자·운영자·승인자·보안 담당자의 Permission을 분리한다.

### 34.4 시작 전에 결정할 값

Operation 상태, Progress, Target 결과, Cancel 가능 시점, Timeout, Retention을 정한다.

### 34.5 결과물

Owner API·OpenAPI·Generated Client·Route·Component·Permission·Test·Runbook.

### 34.6 단계별 절차

Command 응답의 Operation ID를 저장하고 상태·대상·Audit를 조회한다. Browser 재접속 후에도 Operation을 다시 찾을 수 있게 한다.

### 34.7 입력·기본값·허용 범위

실제 화면 Field와 Owner DTO를 사용하며 기본값·허용 범위·Masking을 문서화한다.

### 34.8 정상 결과와 완료 판정

Operation과 Owner 작업이 같은 ID를 사용하고 최종 상태·대상 결과가 완결된다.

### 34.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Validation·409·Timeout·응답 유실·Session 만료·부분 성공을 독립 상태로 표시한다.

### 34.10 재시도·재처리·대사·보상·되돌리기

Browser 종료·Session 만료·응답 유실·Worker 유실은 작업 자체를 취소하지 않는다. 상태 조회 후 허용된 Cancel/Reconcile을 수행한다.

### 34.11 로그·지표·추적·감사

Route·Operation·Business/Target ID·Version·Reason·Approval·Trace·Audit를 기록한다.

### 34.12 교육 예제

`EDU-ADM-05·06·17`를 실행해 정상·오류·Browser 재접속·응답 유실을 검증한다.

### 34.13 조직 영역과 CPF 유지 영역

업무 Query/Command와 상태는 Owner 영역이다. ADM UI·권한·Operation·Audit 표준은 CPF가 유지한다.

### 34.14 운영 인계

Route, Permission, 필드·버튼·상태, Owner Endpoint, Timeout, 대사·Rollback을 전달한다.


## 35. 부분 적용·Reconciliation

### 35.1 업무 결과

설정·배포·대량 조치의 대상별 ACK/NACK·Version·Checksum을 비교해 실패·미확정만 정상화한다.

### 35.2 선택 기준

다중 Instance·다중 대상 조치에 적용한다.

### 35.3 역할과 권한

ADM 연동 개발자·운영자·승인자·보안 담당자의 Permission을 분리한다.

### 35.4 시작 전에 결정할 값

목표 Version/Checksum, 대상 Snapshot, 성공 기준, Retry/Exclude/Rollback 정책을 정한다.

### 35.5 결과물

Owner API·OpenAPI·Generated Client·Route·Component·Permission·Test·Runbook.

### 35.6 단계별 절차

적용 시도 후 대상별 결과를 표로 표시하고 Drift를 계산한다. 성공 대상은 고정하고 실패·미확정만 재적용 또는 LKG 복구한다.

### 35.7 입력·기본값·허용 범위

실제 화면 Field와 Owner DTO를 사용하며 기본값·허용 범위·Masking을 문서화한다.

### 35.8 정상 결과와 완료 판정

모든 활성 대상의 Version·Checksum이 목표 또는 승인된 LKG와 일치하고 Drift가 0이다.

### 35.9 오류·동시성·시간초과·응답 유실·부분 실패

권한·Validation·409·Timeout·응답 유실·Session 만료·부분 성공을 독립 상태로 표시한다.

### 35.10 재시도·재처리·대사·보상·되돌리기

연결 끊김은 NACK와 구분한다. 대상이 처리했을 수 있으면 상태 조회 전 재전송하지 않는다.

### 35.11 로그·지표·추적·감사

Route·Operation·Business/Target ID·Version·Reason·Approval·Trace·Audit를 기록한다.

### 35.12 교육 예제

`EDU-ADM-06·10·11·12`를 실행해 정상·오류·Browser 재접속·응답 유실을 검증한다.

### 35.13 조직 영역과 CPF 유지 영역

업무 Query/Command와 상태는 Owner 영역이다. ADM UI·권한·Operation·Audit 표준은 CPF가 유지한다.

### 35.14 운영 인계

Route, Permission, 필드·버튼·상태, Owner Endpoint, Timeout, 대사·Rollback을 전달한다.


## 36. 역할별 하루 업무 흐름

### 36.1 조회자

1. 기본 검색 기간과 Data Scope를 확인한다.
2. 목록 열의 집계 기준시각과 Source Version을 확인한다.
3. 상세에서 업무 상태·오류·Trace를 확인한다.
4. 변경 버튼이 보이면 권한 구성이 잘못된 것이므로 운영관리자에게 보고한다.

### 36.2 운영자

1. 경보에서 업무 영향과 대상 수를 확인한다.
2. 거래·로그·Trace·외부 Attempt를 같은 식별자로 연결한다.
3. Preview에서 현재 Version과 이미 성공한 대상을 확인한다.
4. 안전 조치는 사유와 Idempotency Key로 한 번 실행한다.
5. 202 작업은 Operation 완료까지 확인하고 교대 인계에 남긴다.

### 36.3 승인자

1. 요청자와 실행자의 분리 여부를 확인한다.
2. 대상 Snapshot, Version, Checksum, 영향, Rollback을 검토한다.
3. 승인 유효시간과 실행 환경을 제한한다.
4. 실행 결과와 부분 적용·Rollback Audit를 확인한다.

### 36.4 보안 담당자

1. 원문 조회·반출·Break-glass 요청의 사유와 승인 범위를 검토한다.
2. Session·MFA·IP Allowlist·Credential Rotation 상태를 확인한다.
3. 개인정보·Token·Secret 원문이 Log/Export에 포함되지 않았는지 확인한다.
4. 사고 종료 후 긴급 권한과 Session을 회수한다.

## 37. 화면 장에 반드시 기재할 항목

| 구분 | 필수 내용 | 완료 판정 |
|---|---|---|
| 메뉴 | 상위 메뉴·Route·주 역할 | 직접 URL과 메뉴 접근 권한 일치 |
| 검색 | Field·기본값·최대 범위 | 초기 조회가 과도하지 않음 |
| 목록 | Column·정렬·단위·Masking | Owner 응답과 표시 의미 일치 |
| 상세 | 상태·Version·Source 시각·식별자 | 재처리 판단에 필요한 정보 존재 |
| 버튼 | 이름·Permission·활성 조건 | 조회 전용 화면에 변경 버튼 없음 |
| 입력 | Type·필수·기본·범위 | 잘못된 값의 오류가 명확 |
| 위험 조치 | Reason·Approval·Preview | 승인 대상과 실행 대상 일치 |
| 비동기 | Operation·Progress·Target | 재접속 후 작업 재조회 가능 |
| 실패 | 400/401/403/404/409/429/5xx/Timeout | 다음 행동이 화면에 제시됨 |
| 감사 | 수행자·사유·승인·전후·Trace | 업무 결과와 같은 ID로 조회 |

## 38. Browser·Fault Test 시나리오

| 시나리오 | 기대 결과 |
|---|---|
| 권한 없는 직접 Route 접근 | 403 또는 권한 안내, 데이터 렌더링 없음 |
| Data Scope 밖 ID 직접 입력 | 존재 여부를 노출하지 않는 거부 |
| 검색 중 Session 만료 | 재로그인 후 Query 재실행, Command 자동 재전송 없음 |
| Command 중 네트워크 단절 | Operation 조회 안내, 버튼 중복 실행 차단 |
| 409 Version 충돌 | 최신 상세·차이 표시 후 재Preview |
| 202 작업 중 Browser 종료 | 재접속 후 Operation 검색 가능 |
| 일부 대상 NACK | 성공·실패·미확정 분리, 실패만 재적용 |
| 승인 만료 | 실행 차단·재승인 |
| Export 생성 응답 유실 | Export Operation과 Download Audit 조회 |
| 원문 보기 | 대상·사유·시간 제한·Audit 표시 |

## 39. 교대 인계 양식

```text
Incident/Operation ID:
업무 영향:
운영 상태:
완료 대상:
실패 대상:
미확정 대상:
마지막 확인 시각:
다음 확인 시각:
수행한 조치와 사유:
승인 ID:
Version/Checksum:
Rollback 가능 여부:
관련 Dashboard/Log/Trace/Audit:
다음 담당자와 종료 기준:
```
