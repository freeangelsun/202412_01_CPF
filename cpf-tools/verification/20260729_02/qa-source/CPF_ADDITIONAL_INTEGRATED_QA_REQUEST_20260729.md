# CPF 추가 통합 QA 요구사항

## 기존 패키지 표준 검증·업무 기능별 구조화·ADM/BZA UI 완성도·Repository Hygiene

## 1. 문서 목적

본 요구사항은 기존 CPF 전수 QA 요청서와 Runtime 통합 검증 요청서에 추가한다.

이번 검수의 목적은 단순히 Source, 화면, 메뉴, API, Table 또는 Interface가 존재하는지를 확인하는 것이 아니다.

다음 사항을 CPF Framework 전체에서 검증하고, 결함이 발견되면 실제 Source·SQL·Frontend·Test·Generator·문서까지 함께 수정한다.

1. 기존 CPF 패키지·Module·Generator 표준이 실제로 일관되게 적용됐는지
2. 기존 표준 자체에 구조적 문제가 있는지
3. ACC·MBR·EXS 등 Generated Domain이 동일한 표준 구조를 따르는지
4. 각 업무 Domain 내부가 실제 업무 기능 단위로 적절하게 세분화됐는지
5. ADM·BZA 메뉴와 UI에 실제 기능이 빠짐없이 녹아 있는지
6. 버튼별 권한, 업로드·다운로드, 원격 작업 결과·실패 원인이 구현됐는지
7. Dead Code, Legacy, 중복 구현, 임시 Source, Stale 문서와 Evidence가 제거됐는지
8. 동일한 구조 문제가 다시 생기지 않도록 자동 Architecture·Hygiene Gate가 있는지

본 요구사항은 새로운 패키지 구조를 근거 없이 강제하기 위한 문서가 아니다.

반드시 최신 `master`의 정본 문서, Generator Template, 실제 Source 구조를 먼저 확인하고 판단한다.

---

# 2. 최우선 원칙

## 2.1 정본 우선

다음 순서로 확인한다.

```text
CPF_FINAL_TARGET_REQUIREMENTS
→ Architecture / Package / Module 정본
→ Generator 정본과 Template
→ 현재 작업 요청서
→ MBR 실제 구조
→ ACC 실제 구조
→ EXS 실제 구조
→ 기타 Generated Domain
→ Platform Module 실제 구조
→ Frontend·SQL·Test·Config·Guide·Evidence
```

일반적인 Clean Architecture, Hexagonal Architecture, DDD 예시를 CPF에 그대로 강제하지 않는다.

기존 CPF 표준과 다른 구조를 제안하려면 먼저 다음을 증명한다.

* 기존 표준의 구체적인 문제
* 실제 발생한 변경 영향
* 잘못된 의존성
* Package 비대화
* 업무 기능 혼재
* Generator Drift
* Consumer 결합
* Test 곤란
* 운영·배포 영향
* 대체안의 장단점
* Migration 범위

## 2.2 확인 전 변경 금지

정본과 Generator 구조를 확인하기 전에 다음 작업을 하지 않는다.

* 새로운 공통 Package 체계 일괄 적용
* 기존 Package 대량 이동
* ACC만 별도 구조로 변경
* MBR·ACC·EXS에 서로 다른 기술 구조 적용
* Package명만 보고 Source 일괄 삭제
* Generator 관리영역 직접 수정
* Legacy 제거 명목의 재귀 삭제

## 2.3 기존 표준에 문제가 없는 경우

기존 표준이 다음 조건을 만족하면 유지한다.

* Module Ownership이 명확함
* 기능 간 의존 방향이 올바름
* 업무 기능 간 응집도가 높음
* Public API·SPI·Internal 경계가 명확함
* Generator로 동일 구조가 재현됨
* MBR·ACC·EXS의 normalized 구조가 일치함
* Local·Remote 양쪽에서 동일 Contract가 성립함
* SQL·Test·Guide·Generator가 같은 구조를 따름
* 신규 Domain 추가 시 중앙 Source 수정이 불필요함

## 2.4 기존 표준에 문제가 있는 경우

문제가 발견되면 조용히 다른 구조로 바꾸지 않는다.

다음 내용을 포함한 Architecture Decision을 먼저 작성한다.

```text
현재 표준
→ 실제 문제
→ 문제 발생 Source와 Consumer
→ 운영·확장·Generator 영향
→ 유지안
→ 최소 개선안
→ 목표 개선안
→ Migration 순서
→ 호환성 정책
→ Test와 Evidence
→ Legacy 제거 계획
```

Architecture Decision만 작성하고 실제 결함을 남겨두는 것은 완료가 아니다.

---

# 3. CPF 전체 Module Ownership 전수 검수

공식 Module과 Generated Domain의 실제 책임을 전수 확인한다.

최소 검수 대상:

* `cpf-core`
* `cpf-common`
* `cpf-admin`
* `cpf-biz-admin`
* `cpf-batch`
* `cpf-gateway`
* 기타 공식 `cpf-*` Module
* MBR 업무 Domain
* ACC 업무 Domain
* EXS 업무 Domain
* Generator로 생성한 임시 검증 Domain
* ADM Frontend
* BZA Frontend
* SQL·Migration·Rollback·Seed
* Test·Config·Script·Guide·Evidence

## 3.1 Ownership 검수 기준

각 기능은 다음을 명확히 가져야 한다.

* Requirement Owner
* Source Owner Module
* DB Owner
* Runtime Owner
* UI Owner
* Public API Owner
* SPI Owner
* 실제 Consumer
* 운영 조회·제어 Owner
* Test Owner
* Generator Owner 여부

다음은 구조 결함으로 판정한다.

* 동일 기능의 Owner가 두 개 이상
* Owner 없는 공통 기능
* 특정 업무 기능이 `cpf-core`에 존재
* Batch Runtime 기능이 `cpf-core`나 ADM에 존재
* ADM이 업무 Domain의 원장 Repository를 직접 소유
* BZA가 온라인 업무 Runtime의 필수 의존성이 됨
* Generated Domain 기능이 Platform Module에 역으로 흡수됨
* 선택형 기능이 필수 Module 의존성으로 고정됨
* 외부 Module이 다른 Module의 Internal Package를 직접 참조
* 순환 의존 또는 역방향 의존
* 동일 기능이 여러 Module에 중복 구현됨

---

# 4. ACC·MBR·EXS Generated Domain 표준

## 4.1 공통 원칙

ACC·MBR·EXS는 이름만 다른 수동 복사 Domain이 되어서는 안 된다.

동일한 Generator Capability를 선택했다면 Metadata와 실제 업무 기능을 제외한 제품 구조가 동일해야 한다.

### 동일해야 하는 구조

* Gradle Module 구성
* Artifact 구성
* Package Root 규칙
* Domain Manifest
* SystemCode 적용 방식
* Config 구조
* Public API 기본 구조
* Local·Remote Facade 구조
* Header·Error·Validation·Paging 표준
* Repository 계약 구조
* DB Resource 구조
* Vendor SQL 디렉터리 역할
* Migration·Rollback·Seed·Verify 구조
* Test 종류와 위치
* OpenAPI
* JavaDoc
* Guide
* Build·Install·Upgrade 방식
* Generator 관리영역 표시 방식

### 달라도 되는 항목

* DomainName
* 3자리 SystemCode
* Package Root
* Artifact ID
* Schema
* Table Prefix
* Port 또는 표시명
* 실제 업무 모델
* 실제 업무 규칙
* 명시적으로 선택된 Capability

## 4.2 Normalized Tree Parity

ACC·MBR·EXS와 신규 임시 생성 Domain을 대상으로 normalized tree를 비교한다.

비교 시 다음 Metadata 차이는 정규화할 수 있다.

* DomainName
* SystemCode
* Package Root
* Artifact ID
* Schema명
* Table Prefix
* 표시명
* Port
* 생성 일시

그 외 구조 차이는 이유를 설명해야 한다.

다음은 실패다.

* MBR만 정식 구조이고 ACC는 수동 복사본
* ACC만 별도 예외 Package 구조
* EXS만 Generator와 다른 구조
* 동일 Capability인데 Controller·Service·Repository·SQL·Test 구조가 다름
* 특정 Domain만 수동 Migration이나 Seed를 가짐
* 특정 Domain만 Local 또는 Remote 호출을 지원함
* 신규 Domain 생성 시 중앙 Installer나 Platform Source 수정이 필요함
* Generator가 생성한 결과를 수동 복사·치환해야 Build됨

## 4.3 Generator 우선 수정

Generated Domain 공통 구조에 문제가 있으면 다음 순서로 처리한다.

```text
정본 Template 문제 확인
→ Generator 수정
→ Generator Test 수정
→ 임시 Domain 생성
→ Build·Runtime 검증
→ ACC·MBR·EXS 이관
→ SQL·Test·Guide 동기화
→ 이전 구조 제거
```

ACC 하나만 직접 수정하고 Generator를 수정하지 않으면 완료가 아니다.

Generator만 수정하고 기존 ACC·MBR·EXS를 Drift 상태로 남겨도 완료가 아니다.

---

# 5. Generated Domain 내부 업무 기능별 세분화

## 5.1 기능 경계 도출 원칙

업무 Package는 기술 계층 이름만 보고 나누지 않는다.

먼저 실제 업무 기능을 Source와 DB에서 도출한다.

```text
Requirement
→ 업무 Use Case
→ API
→ Command / Query
→ 업무 규칙
→ 상태 전이
→ Table / Aggregate
→ Event
→ 실제 Consumer
→ 운영 기능
```

ACC의 업무 기능명을 추측해 임의로 Package를 만들지 않는다.

ACC 실제 Source의 다음 항목을 분석해 기능 경계를 확정한다.

* Controller와 Endpoint
* Service Method
* Domain Model
* Repository
* Table
* Event
* Batch Consumer
* External Consumer
* ADM/BZA Consumer
* Test
* Guide
* Generator Capability

## 5.2 기능별 분리 기준

다음 조건 중 여러 개가 해당하면 독립 업무 기능으로 분리한다.

* 독립된 업무 목적
* 독립된 상태 전이
* 독립된 Transaction 경계
* 독립된 권한
* 독립된 API
* 독립된 DB Aggregate
* 독립된 운영·복구 절차
* 독립된 Batch 또는 Event Consumer
* 독립된 감사 정책
* 독립된 변경 주기

반대로 단순 Class 수가 많다는 이유로 Package를 과도하게 쪼개지 않는다.

## 5.3 권장 구조 원칙

정확한 Package 이름은 기존 CPF 정본을 우선한다.

다만 최소한 다음 책임은 혼재되지 않아야 한다.

```text
Generated Domain
└─ Business Feature
   ├─ 외부 계약
   ├─ 업무 처리
   ├─ 업무 모델·상태·정책
   ├─ 저장소 계약
   ├─ 저장소 구현
   ├─ Local 호출 Adapter
   ├─ Remote 호출 Adapter
   ├─ Event·Batch Consumer
   ├─ 운영 조회·제어
   └─ Test
```

다음은 실패 후보로 탐지한다.

* 모든 업무가 하나의 `controller` Package에 집중
* 모든 업무가 하나의 `service` Package에 집중
* 모든 Entity와 DTO가 하나의 `model` Package에 집중
* 하나의 Service가 무관한 여러 업무 기능 처리
* 하나의 Repository가 무관한 여러 Aggregate 처리
* API DTO와 DB Entity를 동일 객체로 사용
* 업무 규칙이 Controller나 Mapper에 존재
* `common`, `util`, `helper`, `manager`에 업무 규칙 은닉
* 기능 간 직접 Repository 참조
* 업무 기능 간 순환 의존
* 하나의 대형 Facade가 모든 업무를 switch문으로 분기
* DomainName이나 URL 문자열로 업무 기능을 추론

## 5.4 Local·Remote Parity

각 업무 기능은 동일한 업무 Contract를 사용해야 한다.

```text
Consumer
→ Business Contract
→ Local Facade 또는 Remote Facade
→ 동일 Header·Error·Trace·Validation
→ 동일 업무 결과
```

금지 사항:

* 동일 JVM 호출이 HTTP Loopback 사용
* 내부 호출이 Gateway를 재경유
* Local과 Remote가 서로 다른 DTO·Error 사용
* Remote에만 Retry·Timeout이 있고 Local은 다른 규칙 사용
* URL·Port·Package 이름으로 Domain 판별
* 특정 ACC·MBR 이름을 Platform Source에 하드코딩

---

# 6. Public API·SPI·Internal 경계

각 Module과 Generated Domain에서 다음을 구분한다.

* Public API: 고객과 다른 Module이 안정적으로 사용하는 계약
* SPI: 고객·업무 Domain·Provider가 확장하는 계약
* Internal: 제품 내부 구현

검수 기준:

* Public API에 Internal 구현 타입 노출 금지
* Public API에 특정 DB Vendor 타입 노출 금지
* Public API에 특정 Provider 구현 타입 노출 금지
* 외부 Module의 Internal Package import 금지
* SPI에 실제 기본 구현 또는 명확한 필수 Adapter 존재
* SPI에 실제 Consumer 존재
* Consumer 없는 Interface·SPI 탐지
* Public DTO와 Persistence Entity 분리
* 호환성 대상 API 식별
* Public API와 SPI에 한글 JavaDoc
* 실패 조건·멱등성·동시성 계약 명시
* Local·Remote 구현이 동일 Public Contract 사용

Interface가 존재한다는 이유로 완료 처리하지 않는다.

실제 Consumer와 실행 경로가 없으면 `미구현` 또는 `부분 구현`이다.

---

# 7. ADM·BZA UI와 메뉴 구조 전수 검수

## 7.1 검수 범위

ADM·BZA의 다음 항목을 전수 목록화한다.

* Menu Group
* Menu
* Submenu
* Tab
* Route
* Page
* Modal
* Drawer
* Button
* API
* Permission
* Owner Module
* DB
* Audit
* Test

각 기능은 다음과 연결돼야 한다.

```text
Requirement
→ 업무·운영 목적
→ Menu
→ Route
→ Page
→ Action
→ Permission
→ Backend API
→ Owner
→ DB
→ Audit
→ Browser E2E
```

## 7.2 UI 적정성

단순 화면 존재가 아니라 기능 목적에 적합한 UI인지 확인한다.

* 계층 데이터: Tree 또는 Tree Grid
* 대량 목록: Server Paging 또는 Virtual Grid
* 실행 작업: Progress·Timeline·Target Result
* 정책 변경: Current·Desired·Diff·Preview
* 위험 조작: Reason·Approval·Confirmation
* 감사 이력: Immutable Timeline
* 업로드: Wizard·Preview·Validation·Result
* 다운로드: Job·Progress·Expiry·Audit
* 부분 실패: 전체 집계와 대상별 상세
* 결과 불명: Reconcile 동선
* 복구: Retry·Cancel·Rollback 동선

다음은 완료가 아니다.

* JSON `<pre>` 출력
* 개발자용 Form
* 오류 Toast 한 줄
* 2단계까지만 표시되는 Tree
* 평면 목록을 들여쓰기만 한 화면
* 화면 첫 페이지 데이터만 다운로드
* Backend 기능이 연결되지 않은 Button
* 실제 업무 흐름과 맞지 않는 복사형 CRUD 화면

---

# 8. Tree UI 요구사항

## 8.1 ADM Runtime Tree

최소 계층:

```text
Environment
└─ Platform / Cluster
   └─ Domain / System
      └─ Application Group
         └─ Runtime Role
            └─ Service
               └─ Instance
```

필수 기능:

* 임의 깊이
* 펼치기·접기
* 전체 펼치기·접기
* 검색 결과 경로 자동 확장
* Breadcrumb
* 상태별 필터
* 상위 노드 상태 집계
* Desired·Actual·Version·Drift
* Heartbeat
* Host·Port
* Artifact Version·Commit
* Lease·Fencing Token
* 선택 범위 Preview
* 실제 대상 Snapshot
* 로그·거래·Batch·설정·감사 화면 연결
* Lazy Loading
* Virtual Scroll
* 접근성
* 선택·확장 상태 유지

## 8.2 BZA 조직 Tree

필수 기능:

* 임의 깊이 재귀 렌더링
* 조직 검색과 경로 자동 확장
* Breadcrumb
* 조직 이동
* 순환 구조 차단
* 존재하지 않는 Parent 차단
* 하위 조직 보유 시 삭제 정책
* 직원·조직장·승인 경로 영향 Preview
* expectedVersion·CAS
* 권한·사유·감사
* 대규모 조직 성능

## 8.3 BZA 메뉴·권한 Tree

최소 계층:

```text
Menu Group
└─ Menu
   └─ Submenu
      ├─ Screen Permission
      ├─ Button Permission
      ├─ API Permission
      └─ Data Scope
```

필수 기능:

* Allow·Deny
* 상속
* Deny 우선순위
* Effective Permission
* 사용자 권한 Simulation
* Role 비교
* 변경 전후 Diff
* Frontend Route parity
* Backend Endpoint parity

---

# 9. 버튼별 권한

각 Button·Action은 다음 권한 범위를 검증한다.

```text
Menu Permission
+ Screen Permission
+ Action Permission
+ API Permission
+ HTTP Method
+ Resource Scope
+ Data Scope
+ Environment Scope
+ Domain Scope
+ Organization Scope
```

필수 검수 대상:

* 조회
* 등록
* 수정
* 삭제
* 실행
* 중지
* 일시정지
* 재개
* 취소
* Retry
* Rollback
* Reconcile
* 승인
* 반려
* Break Glass
* Excel 다운로드
* Excel 업로드
* Template 다운로드
* 오류 파일 다운로드
* 로그 다운로드
* Evidence 다운로드
* 개인정보 원문 조회
* Secret·Key·인증서 작업

검증 기준:

* Frontend Button 노출
* 비활성화 사유
* URL 직접 접근
* API 직접 호출
* Backend `403`
* HTTP Method별 권한
* 다른 Domain·조직·환경 접근
* Deny 우선
* 권한 변경 후 Session·Cache 반영
* 위험 작업의 사유·승인·감사
* Frontend와 Backend Permission Code parity

Frontend에서 Button을 숨긴 것만으로 완료 처리하지 않는다.

---

# 10. Excel·CSV 다운로드

필수 기능:

* 현재 검색조건 다운로드
* 선택 행 다운로드
* 전체 결과 다운로드
* 예상 건수·크기 Preview
* Server-side 생성
* 대량 비동기 Job
* Streaming
* 메모리 전체 적재 금지
* 진행 상태
* 성공·실패·취소
* 실패 코드와 이유
* 임시 파일 정리
* 만료 정책
* 동시 다운로드 제한
* 최대 행 수·파일 크기
* XLSX·CSV 정책
* Formula Injection 방지
* 날짜·시간대·금액 포맷
* 다국어 Header
* 개인정보·계좌·Token 마스킹
* Data Scope
* 다운로드 사유
* 민감 다운로드 승인
* 감사 이력
* 파일 Hash
* 동일 요청 멱등성
* 재다운로드 정책

완료 금지:

* 현재 브라우저 표시 행만 CSV 생성
* 첫 Paging 데이터만 다운로드
* 모든 데이터를 JVM Heap에 적재
* 직접 URL 호출 가능
* 마스킹 전 데이터 노출
* 감사 이력 없음
* 실패 원인 없음

---

# 11. Excel·CSV 업로드

표준 흐름:

```text
Template 다운로드
→ 파일 선택
→ 확장자·MIME·크기 검증
→ 위험 콘텐츠 검증
→ Schema 검증
→ Preview
→ 행별 오류
→ Dry-run
→ 처리 방식 선택
→ 권한·사유·승인
→ operationId
→ 비동기 실행
→ 성공·부분 성공·실패
→ 오류 파일
→ Retry·Rollback
→ Audit
```

필수 기능:

* Template Version
* 필수·선택 Column
* 코드값 검증
* 참조 무결성
* 파일 내부 중복
* DB 기존 데이터 중복
* 날짜·숫자·Encoding 검증
* MIME 검증
* 최대 건수·크기
* 영향 Preview
* 전체 원자 처리 또는 부분 처리 정책
* 행별 결과
* 실패 코드·상세 이유
* 오류 행 다운로드
* operationId 중복 방지
* expectedVersion·CAS
* 재시작 후 복구
* 취소·재처리
* 원본 파일 보관·폐기
* 개인정보·Secret 차단
* 감사와 DB 반영 대조

---

# 12. ADM 실시간·원격 제어 결과 가시성

모든 원격 작업에 대해 ADM에서 다음을 확인할 수 있어야 한다.

## 12.1 전체 결과

* operationId
* request hash
* 요청자
* 승인자
* 사유
* 대상 Snapshot
* 요청·시작·종료 시각
* 전체 대상 수
* 성공 수
* 실패 수
* 미응답 수
* Offline 수
* 재시작 필요 수
* 전체 상태
* Retry 상태
* Rollback 상태
* Reconcile 상태
* Trace·Transaction·Correlation ID
* 감사 Event
* 로그 연결

상태:

* 대기
* 승인 대기
* 진행 중
* 성공
* 부분 성공
* 실패
* 취소
* Rollback 진행
* Rollback 성공
* Rollback 실패
* 결과 불명
* 재시작 필요

## 12.2 대상별 결과

* Environment
* Domain
* SystemCode
* Cluster·Cell·Zone
* Application Group
* Runtime Role
* Service
* Instance
* Host·Port
* Artifact Version·Commit
* Delivery
* ACK
* 적용 전 Version
* Desired Version
* Actual Version
* 실패 단계
* 오류 코드
* 마스킹된 실패 사유
* Retry 가능 여부
* Retry 횟수
* 다음 Retry 시각
* Timeout
* Offline
* Drift
* Restart Required
* Rollback 결과
* Reconcile 결과
* Heartbeat
* Lease
* Fencing Token

## 12.3 실패 단계

최소 다음을 구분한다.

```text
대상 선택
→ 입력 검증
→ 권한
→ 승인
→ expectedVersion·CAS
→ Delivery
→ Target Offline
→ 연결
→ Timeout
→ Runtime 적용
→ 적용 후 검증
→ Health Gate
→ ACK 유실
→ Drift
→ Retry 소진
→ Rollback
→ Reconcile
→ 결과 불명
```

Retry 성공 후에도 최초 실패 Attempt를 덮어쓰지 않는다.

Controller·Agent·WAS 재시작 후에도 이력이 조회돼야 한다.

---

# 13. Framework 전체 가비지·Legacy 제거

## 13.1 제거 대상

### Java·Backend

* 미사용 Class·Interface·Enum·DTO
* Consumer 없는 SPI
* 구현 없는 Interface
* 중복 Service·Repository·Adapter
* 이관 후 남은 Legacy Package
* 주석 처리된 대량 코드
* 사용하지 않는 Feature Flag
* 빈 Controller·Service
* 임시 Compatibility Wrapper
* `Tmp`, `Old`, `New`, `Copy`, `Backup`
* 운영 Source의 Mock·Test Fixture
* 사용하지 않는 AutoConfiguration
* Dead Branch
* 중복 상태 Enum·문자열

### Frontend

* 고아 Route
* 메뉴 없는 Page
* Page 없는 Menu
* 사용하지 않는 Component·Store·Composable
* 제거 API를 호출하는 Button
* Debug Panel
* JSON 확인 화면
* 중복 CSS
* 사용하지 않는 Design Token
* 외부 CDN·Font·Script
* Build 산출물
* 임시 ZIP·BAK·LOG

### DB

* 사용하지 않는 Table·Column·Index
* 잘못된 Column 참조 Index/FK
* 중복 Migration
* 오래된 Seed
* 삭제 메뉴·권한
* 제거 기능 Runtime SQL
* Vendor 수동 복사본
* Rollback 없는 Migration
* Generator와 무관한 SQL
* 테스트 계정·Secret

### 문서·Evidence·Script

* 과거 Current Request
* 오래된 Restart Prompt
* 적용 완료 Overlay·Patch
* 중복 Handover
* Stale Evidence
* 근거 없는 PASS
* Source와 다른 Guide
* 특정 PC 절대경로 Script
* 사용하지 않는 Verification Script
* README 작업 일지
* 동일 역할 중복 정본
* Root 임시 파일

## 13.2 안전한 삭제

다음 순서를 따른다.

```text
후보 탐지
→ Git 참조 검색
→ Reflection 확인
→ Spring AutoConfiguration 확인
→ ServiceLoader·SPI 확인
→ Dynamic Route 확인
→ SQL·Menu·Permission 확인
→ Generator Template 확인
→ 실제 Consumer 확인
→ 대체 구현 확인
→ Consumer·Test 이관
→ 삭제
→ 전체 Build·Runtime 회귀
```

정적 검색 결과가 없다는 이유만으로 즉시 삭제하지 않는다.

반대로 실제 Consumer와 제품 정책을 증명하지 못하면서 “향후 사용 가능”이라는 이유로 보존하지 않는다.

## 13.3 구조 이관 완료조건

```text
목표 구조
→ Generator 수정
→ 실제 구현 이관
→ Consumer 이관
→ Config 이관
→ SQL 이관
→ Frontend·Menu·Permission 이관
→ Test·Guide 이관
→ Legacy 참조 차단
→ Legacy 삭제
→ 전체 회귀
```

새 구조와 Legacy 구조가 동시에 남아 있으면 `부분 구현`이다.

---

# 14. 자동 Architecture·Hygiene Gate

다음 Gate를 구현하거나 기존 Gate에 편입한다.

* 공식 Module·Root 허용 목록
* Module 의존 방향
* 순환 Module·Package
* Internal Package 외부 참조
* Public API의 Vendor·Internal 타입
* Consumer 없는 Interface·SPI 후보
* 대형 Class·Vue 파일
* God Controller·Service
* `common/util/helper/manager` 오남용
* 중복 DTO·Enum·상태 문자열
* Menu·Route·Permission·API parity
* Source·Test Package parity
* Canonical SQL·Vendor SQL Drift
* Generator Template Drift
* ACC·MBR·EXS normalized tree Drift
* 고아 Route·Menu·API
* Build·Log·ZIP·BAK·TMP
* Secret·Password·Private Key
* Stale Evidence SHA
* Repository Root 가비지
* 외부 CDN·Font·Script
* Dead Code 후보

Gate Source가 존재하는 것만으로 완료 처리하지 않는다.

실제 전체 검증 명령이나 CI에서 실행되고 최신 Commit Evidence가 있어야 한다.

---

# 15. Package 표준 문제 판정 기준

기존 표준은 다음 중 하나로 판정한다.

* 유지
* 부분 개선
* 전환 필요
* 재확인 필요

## 15.1 유지

* Ownership 명확
* 기능 응집도 양호
* 의존 방향 정상
* Generator parity 정상
* 신규 Domain 확장 가능
* UI·SQL·Test 구조 일치
* 실제 Consumer 명확

## 15.2 부분 개선

* 기본 구조는 타당하나 일부 기능 Package가 비대함
* Internal 경계 일부 위반
* Generated Domain 일부 Drift
* Frontend 기능 구조 불균형
* 중복 DTO·상태 문자열 존재

## 15.3 전환 필요

* 수평 Package로 모든 업무가 혼재
* 기능별 Transaction·권한·복구 경계 구분 불가
* Module Ownership 반복 위반
* Generator가 Domain별 다른 구조 생성
* 신규 Domain마다 중앙 Source 수정 필요
* Local·Remote Contract 분리
* Legacy와 신규 구조가 장기간 공존
* Package 구조 때문에 Test·배포·운영 분리가 불가능

전환 필요 판정 시 전체 재작성보다 단계적 Migration을 우선 검토하되, 잘못된 Legacy를 무기한 유지하지 않는다.

---

# 16. 필수 QA Matrix

다음 Matrix를 산출한다.

## 16.1 Module·Package Matrix

* Module
* Domain
* Feature
* Current Package
* Target Package
* Owner
* Public API
* SPI
* Internal
* Consumer
* DB
* UI
* Test
* Generator Managed 여부
* Drift
* Status
* Required Action

## 16.2 Generated Domain Parity Matrix

* Domain
* SystemCode
* Capability
* Generator Version
* Normalized Tree Hash
* Build
* Runtime
* DB
* Local
* Remote
* Test
* Guide
* Drift
* Status

## 16.3 Menu·UI Matrix

* ADM/BZA
* Menu Group
* Menu
* Route
* Page
* 기능 목적
* 필수 기능
* Button
* Permission
* API
* Owner
* Upload
* Download
* Failure UX
* Audit
* E2E
* Status

## 16.4 Garbage Removal Matrix

* 경로
* 유형
* 현재 역할
* Consumer
* 삭제 후보 근거
* Reflection·Config 확인
* Generator 영향
* 대체 구현
* 삭제 여부
* 회귀 Test
* Evidence

---

# 17. 완료 금지 조건

다음만으로 완료 처리하지 않는다.

* Package Directory 존재
* Class 존재
* Interface 존재
* Controller·Service·Repository 계층 존재
* Generator 명령 성공
* MBR 한 개 Build 성공
* 화면 존재
* 메뉴 존재
* Button 존재
* Frontend Button 숨김
* Tree처럼 보이는 목록
* 다운로드 파일 생성
* Mock 성공
* 일부 Unit Test
* ArchUnit Test 파일 존재
* Dead Code 후보 목록 작성
* Deprecated 표시
* Legacy를 Archive로 이동
* 과거 Evidence
* Build 한 번 성공

완료하려면 최신 Commit에서 다음 연결이 검증돼야 한다.

```text
Requirement
→ 기존 표준 확인
→ 문제 여부 판정
→ 올바른 Owner
→ 업무 기능별 Package
→ Public API·SPI·Internal
→ 실제 Consumer
→ ACC·MBR·EXS Generator parity
→ Local·Remote parity
→ Menu·UI
→ Button Permission
→ Upload·Download
→ 원격 결과·실패 원인
→ DB·Migration·Rollback
→ 정상·오류·경계
→ 부분 실패·결과 불명
→ Multi-instance·Restart
→ Retry·Rollback·Reconcile
→ Test
→ Guide
→ Evidence
→ Legacy·Garbage 제거
```

---

# 18. Codex 수행 지시

1. 최신 `master` SHA를 다시 확인한다.
2. CPF 패키지·Module·Generator 정본을 먼저 찾는다.
3. 정본과 실제 Source 구조의 차이를 작성한다.
4. 기존 표준을 임의로 대체하지 않는다.
5. ACC·MBR·EXS와 임시 Generated Domain의 normalized parity를 확인한다.
6. ACC 실제 업무 기능을 Source·API·DB·Consumer 기준으로 도출한다.
7. 업무 기능별 Package 응집도와 의존성을 검수한다.
8. ADM·BZA Menu·Route·Page·Button·Permission·API를 전수 검수한다.
9. Tree, Upload, Download, 원격 결과·실패 원인 UI를 실제 Browser E2E로 검증한다.
10. Dead Code·Legacy·Stale Artifact를 안전하게 제거한다.
11. 구조 문제를 발견하면 Source·SQL·Frontend·Test·Generator를 함께 수정한다.
12. 발견 결함 수에 제한을 두지 않는다.
13. 실행하지 않은 검증은 `미검증`으로 기록한다.
14. 사용자 승인 없이 Commit, Push, Branch, Tag, Release를 수행하지 않는다.

허용 상태:

* 완료
* 부분 구현
* 미구현
* 미검증
* 실패
* 재확인 필요
