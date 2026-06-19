# CPF 다음 작업 요청서

## 0. 작업 목표

이번 작업은 단순 기능 추가가 아니라, 현재 CPF 안정화 결과의 정합성을 복구하고 CPF의 핵심 운영 프레임워크 기준을 명확히 고정하는 작업이다.

특히 다음 항목을 반드시 반영한다.

1. Codex 완료 메시지, `CPF_STABILIZATION_REPORT.html`, 실제 소스 상태의 정합성 복구
2. `CPF_STABILIZATION_REPORT.html` 하나만 유지
3. 트랜잭션 글로벌 ID 규칙 공식화 및 모든 거래 필수 적용
4. 로그/ADM 화면/DB에 모듈 ID, WAS ID, 서버 인스턴스 식별값 노출
5. `bizadm` 모듈 신설 및 업무/프로젝트 관리자 화면 샘플 구현
6. `exs` 모듈 신설 및 대외 연계 주제영역 골격 구현
7. ADM은 CPF 플랫폼 운영관리자로 유지하고, 업무 관리자 기능을 ADM 내부에 섞지 않음
8. 문서, 기능 구현 매트릭스, SQL, Swagger, smoke, qualityGate 정합성 유지
9. 실제 실행하지 않은 검증은 절대 성공으로 보고하지 않음

Git commit, push, branch 생성은 하지 않는다.

---

## 1. 매우 중요한 검증 보고 원칙

이번 작업 완료 보고에서 가장 중요한 원칙은 다음이다.

```text
Codex는 작업 완료 후 “검증 완료”라고 단정하지 않는다.
실제로 실행한 명령과 그 결과만 검증 성공으로 보고한다.
ChatGPT 또는 사용자가 이후 별도로 검수할 예정이므로, Codex는 미실행 항목을 성공으로 포장하거나 추정 검증 결과를 작성하지 않는다.
로컬에서 실행하지 않은 항목, 앱을 기동하지 않은 항목, DB/브라우저/Swagger를 직접 확인하지 않은 항목은 반드시 미검증으로 남긴다.
```

다음 표현은 금지한다.

```text
검증 완료
문제 없음
성공한 것으로 보임
대체로 정상
실행은 안 했지만 가능
구조상 문제 없어 보임
정상 동작 예상
```

성공으로 적을 수 있는 것은 실제로 명령을 실행했거나 실제 파일/소스/화면/API를 확인한 항목뿐이다.

구분은 반드시 다음처럼 한다.

```text
[성공] 실제 실행해서 성공한 항목
[실패] 실제 실행했으나 실패한 항목
[미검증] 실행하지 못했거나 환경이 없어 확인하지 못한 항목
[정적 확인] Git/파일/소스 내용만 확인한 항목
```

예시:

```text
성공: .\gradlew.bat test --offline 실제 실행 성공
성공: scripts/check-html-docs.ps1 실제 실행 성공
정적 확인: exs_transaction_log DDL 파일 존재 확인
미검증: MariaDB 클라이언트가 없어 실제 SQL 실행 미검증
미검증: 앱을 기동하지 않아 /v3/api-docs HTTP smoke 미검증
미검증: 브라우저 자동화 도구가 없어 실제 클릭 검증 미검증
```

---

## 2. 리포트 산출물 정리

현재 안정화 리포트 관련 산출물이 서로 충돌할 가능성이 있으므로 반드시 정리한다.

유지 대상:

```text
CPF_STABILIZATION_REPORT.html
```

삭제 대상:

```text
CPF_STABILIZATION_REPORT.md
CPF_STABILIZATION_CHANGED_FILES.txt
```

주의:

```text
CPF_STABILIZATION_CHANGED_FILES.txt는 새로 만들거나 갱신하지 않는다.
변경 파일 요약이 필요하면 CPF_STABILIZATION_REPORT.html 내부 섹션에만 포함한다.
```

완료 기준:

```text
root 경로에 CPF_STABILIZATION_REPORT.html 하나만 남아야 한다.
CPF_STABILIZATION_REPORT.md는 없어야 한다.
CPF_STABILIZATION_CHANGED_FILES.txt는 없어야 한다.
```

---

## 3. 검증 결과 정합성 복구

다음 항목은 실제 실행한 것만 성공으로 기록한다.

```text
compile
test --offline
qualityGate --offline
UTF-8/mojibake 검사
legacy name 검사
SQL standard 검사
Java format 검사
HTML docs 검사
ADM UI smoke
OpenAPI HTTP smoke
MariaDB SQL 실제 실행
브라우저 클릭 검증
```

리포트 상태 값은 반드시 다음만 사용한다.

```text
성공
실패
미검증
```

실제 실행하지 못한 검증은 반드시 `미검증`으로 기록한다.

---

## 4. 트랜잭션 글로벌 ID 공식 규격 및 필수 적용

### 4.1 공식 ID 규격

CPF의 모든 거래는 트랜잭션 글로벌 ID를 필수 추적 키로 사용한다.

공식 규격은 다음으로 고정한다.

```text
yyyyMMddHHmmssSSS + moduleId(3자리) + wasId(7자리) + sequence(7자리 기본 zero padding)
```

기본 총 길이:

```text
17 + 3 + 7 + 7 = 34자리
```

예시:

```text
20260615120000000MBRlocal010000001
```

분해:

```text
20260615120000000 = yyyyMMddHHmmssSSS
MBR               = moduleId, 3자리
local01           = wasId, 7자리
0000001           = 일자별 sequence, 기본 7자리 zero padding
```

공식 필드명/컬럼명은 다음 기준을 우선 사용한다.

```text
transactionGlobalId
moduleId
wasId
serverInstanceId
```

DB 컬럼명은 다음 기준을 사용한다.

```text
transaction_global_id
module_id
was_id
server_instance_id
```

---

### 4.2 현재 생성 로직 보강 대상

현재 생성 로직은 `TransactionIdGenerator` 기준으로 다음 설정을 사용한다.

```yaml
cpf:
  framework:
    module-id: MBR
    was-id: local01
    transaction-id:
      sequence-digits: 7
```

보강해야 할 사항:

```text
1. moduleId는 반드시 3자리로 정규화한다.
2. wasId는 공식 규격상 7자리 고정으로 검증한다.
3. sequence는 기본 7자리 zero padding으로 유지한다.
4. sequence-digits 설정은 유지하되 기본 공식 규격은 7자리로 문서화한다.
5. wasId가 7자리가 아니거나 영문/숫자 외 문자가 있으면 부팅 시점 또는 설정 검증 시 실패 처리한다.
6. transaction ID 전체 포맷 검증은 34자리 표준 기준으로 강화한다.
7. 테스트 코드로 정상/오류 케이스를 반드시 추가한다.
8. Java 파일 물리 포맷이 한 줄로 붙어 있으면 정상 개행으로 수정한다.
```

검증 예시:

```text
정상:
20260615120000000MBRlocal010000001
20260615120000000ADMadmAP010000001
20260615120000000EXSexsAP010000001

오류:
wasId 7자리 미만
wasId 8자리 이상
wasId에 특수문자 포함
moduleId 3자리 정규화 실패
전체 길이 34자리 불일치
sequence 자리수 불일치
```

---

### 4.3 모든 거래에서 헤더 필수

모든 온라인 거래, 대외 거래, 배치 실행, 내부 API 호출은 트랜잭션 글로벌 ID를 필수로 가져야 한다.

표준 헤더명은 기존 CPF 문서/소스에 정의된 값을 우선 사용하되, 없으면 다음 중 하나로 통일한다.

```text
X-Transaction-ID
```

또는 CPF 표준 헤더를 이미 쓰고 있다면:

```text
X-CPF-Transaction-Id
```

중요:

```text
헤더명이 여러 개로 흩어져 있으면 안 된다.
문서, Swagger, 필터, WebClient, 로그, 오류 응답, DB 컬럼 기준을 하나로 맞춘다.
```

정책:

```text
온라인/API 요청에서 트랜잭션 글로벌 ID가 없으면 오류 처리한다.
```

이번 작업에서는 자동 생성보다 필수 검증을 우선한다.

예외적으로 프레임워크 내부에서 최초 ID를 생성해야 하는 경우:

```text
배치 스케줄러 실행
내부 run-once 실행
테스트용 mock 요청
프레임워크 내부 이벤트
```

이 경우에도 생성된 ID는 반드시 동일 규격을 따른다.

---

### 4.4 PFW/CMN 책임 분리

PFW 책임:

```text
1. 요청 진입 필터/인터셉터에서 트랜잭션 글로벌 ID 헤더 확인
2. 없으면 표준 오류 응답 반환
3. 있으면 포맷 검증
4. TransactionContext 저장
5. MDC 저장
6. 응답 헤더에 동일 ID 반환
7. 표준 응답/오류 응답에 포함
8. 온라인 거래 로그에 저장
9. 오류 로그에 저장
10. 감사 로그에 저장
11. 다운로드 감사 로그에 저장
12. 배치 execution context에 저장
13. 대외 거래/송수신 로그와 연계
```

CMN 책임:

```text
1. 트랜잭션 글로벌 ID 생성 유틸 제공
2. 트랜잭션 글로벌 ID 포맷 검증 유틸 제공
3. 표준 헤더명 상수 제공
4. moduleId/wasId/sequence 자리수 검증 유틸 제공
5. 외부 호출 WebClient/adapter에서 현재 TransactionContext의 ID를 outbound header로 자동 전파하는 보조 기능 제공
```

주의:

```text
PFW는 정책을 강제한다.
CMN은 생성/검증/전파 보조 API를 제공한다.
업무 모듈이 임의 포맷으로 ID를 직접 만들면 안 된다.
```

---

### 4.5 대외 EXS에서의 트랜잭션 ID 처리

대외기관이 자체 거래 ID를 보내는 경우에도 CPF 내부 트랜잭션 글로벌 ID는 반드시 존재해야 한다.

대외 거래 로그에는 최소 다음 두 종류를 구분 저장한다.

```text
transactionGlobalId     # CPF 내부 글로벌 트랜잭션 ID
externalTransactionId   # 대외기관이 보낸 거래 ID 또는 전문 ID
```

대외 요청 수신 시 처리 기준:

```text
1. 표준 헤더의 transactionGlobalId 확인
2. 없으면 오류 처리
3. 대외기관 자체 거래 ID가 있으면 externalTransactionId로 별도 저장
4. 대외 수신 로그를 업무 처리 전에 선저장
5. 업무 모듈 호출 시 transactionGlobalId를 그대로 전파
6. 외부기관 응답/오류/재처리 로그에도 동일 transactionGlobalId 저장
```

---

### 4.6 배치에서의 트랜잭션 ID 처리

배치 실행도 거래로 본다.

배치 실행 시 다음 값을 반드시 생성/저장한다.

```text
transactionGlobalId
moduleId
wasId
serverInstanceId
batchJobId
batchExecutionId
batchScheduleId
```

ADM 배치 실행 목록/상세 화면에는 다음 항목이 보여야 한다.

```text
트랜잭션 글로벌 ID
모듈 ID
WAS ID
서버 인스턴스 ID
배치 ID
배치명
실행 상태
시작 시각
종료 시각
처리 시간
실패 메시지
```

---

## 5. 로그/ADM 화면/DB에 서버 인스턴스 식별값 노출

운영자는 ADM 화면만 보고도 어떤 서버 인스턴스에서 거래가 처리되었고, 어디서 오류가 발생했는지 확인할 수 있어야 한다.

### 5.1 주요 로그 테이블 보강

다음 테이블 또는 이에 준하는 로그 테이블에 아래 컬럼을 검토/추가한다.

대상:

```text
pfw_transaction_log
pfw_error_log
adm_operation_audit_log
adm_download_audit_log
pfw_batch_execution
pfw_batch_step_execution
pfw_batch_execution_target
exs_transaction_log
exs_message_log
adm_notification_send_log
```

필수 컬럼:

```text
transaction_global_id
module_id
was_id
server_instance_id
```

권장 컬럼:

```text
application_name
host_name
process_id
thread_name
request_uri
http_method
client_ip
user_id
admin_id
```

주의:

```text
이미 유사 컬럼이 있다면 중복 생성하지 말고 표준명으로 정리한다.
SQL, DTO, Mapper, Service, 화면, 문서의 명칭을 일치시킨다.
```

---

### 5.2 ADM 화면 표시 기준

다음 ADM 목록/상세 화면에는 인스턴스 식별값이 보여야 한다.

```text
온라인 거래 로그 목록/상세
오류 로그 목록/상세
배치 실행 목록/상세
배치 step 상세
알림 발송 이력 목록/상세
다운로드 감사 목록/상세
운영 감사 목록/상세
동적 로그레벨 변경 이력
대외 거래 목록/상세
대외 송수신 목록/상세
```

필수 표시 항목:

```text
트랜잭션 글로벌 ID
모듈 ID
WAS ID
서버 인스턴스 ID
처리 서버
요청 URI
처리 상태
오류 코드
오류 메시지
처리 시간
```

목록에서는 최소한 다음은 보여야 한다.

```text
거래시각
트랜잭션 글로벌 ID
모듈
WAS/인스턴스
상태
처리시간
오류 여부
```

상세에서는 전체 값을 보여야 한다.

---

## 6. `bizadm` 모듈 신설

### 6.1 모듈 정의

`bizadm` 모듈을 신설한다.

정의:

```text
bizadm은 CPF 적용 프로젝트에서 업무/프로젝트 관리자 화면을 구현하는 방법을 보여주는 샘플 모듈이다.
adm은 CPF 플랫폼 운영관리자로 유지한다.
bizadm 기능을 adm 내부 Controller/Service/Mapper에 넣지 않는다.
```

`settings.gradle`에 다음 모듈을 추가한다.

```text
bizadm
```

모듈 책임:

```text
1. 프로젝트/업무 관리자 화면 샘플
2. 업무 관리자 사용자 관리 샘플
3. 업무 메뉴 관리 샘플
4. 업무 권한 관리 샘플
5. 버튼 권한 샘플
6. API 권한 샘플
7. 다운로드 권한 샘플
8. 마스킹 정책 샘플
9. 마스킹 해제 권한/사유/감사 샘플
10. 업무 데이터 CRUD 샘플
11. 업무 다운로드 샘플
12. ADM 공통 메뉴/권한/감사 체계 연계 샘플
```

---

### 6.2 ADM과 bizadm의 경계

반드시 지킬 것:

```text
adm = CPF 플랫폼 운영관리자
bizadm = 업무/프로젝트 관리자 화면 샘플
```

ADM에 둘 것:

```text
관리자 계정
역할
권한
메뉴 권한
버튼 권한
API 권한
다운로드 권한
운영 감사
다운로드 감사
거래 로그
오류 로그
배치 관제
알림 관리
동적 로그레벨
```

bizadm에 둘 것:

```text
업무 관리자 샘플 화면
업무 기준정보 샘플
업무 데이터 CRUD 샘플
업무 다운로드 샘플
업무 메뉴/버튼/API 권한 적용 샘플
업무 마스킹/마스킹 해제 샘플
업무 감사 연계 샘플
```

금지:

```text
bizadm 기능을 adm Controller에 추가 금지
bizadm 기능을 adm Service에 추가 금지
bizadm 업무 테이블을 adm 업무처럼 취급 금지
adm 내부 테이블을 bizadm이 직접 무분별하게 조작 금지
```

---

### 6.3 bizadm API 구조

API prefix:

```text
/api/bizadm/**
```

추천 API:

```text
/api/bizadm/admin-users
/api/bizadm/menus
/api/bizadm/roles
/api/bizadm/permissions
/api/bizadm/customers
/api/bizadm/products
/api/bizadm/orders
/api/bizadm/settings
/api/bizadm/downloads
/api/bizadm/masking/unmask
```

Swagger Tag:

```text
BIZADM-Admin-User
BIZADM-Menu
BIZADM-Role
BIZADM-Permission
BIZADM-Customer
BIZADM-Product
BIZADM-Order
BIZADM-Setting
BIZADM-Download
BIZADM-Masking
```

---

### 6.4 bizadm UI 구조

관리자 화면 UI는 기존 ADM UI와 같은 Vue 관리자 콘솔 체계를 사용한다.

단일 관리자 콘솔 안에 메뉴를 함께 제공하되 파일은 영역별로 분리한다.

예시:

```text
views/adm/**
views/bizadm/**
views/exs/**

api/admApi.*
api/bizadmApi.*
api/exsApi.*

router/admRoutes.*
router/bizadmRoutes.*
router/exsRoutes.*
```

기준:

```text
1. ADM과 동일한 레이아웃, 메뉴, 권한, 버튼 제어 방식을 사용한다.
2. bizadm만 별도 프론트 기술로 만들지 않는다.
3. bizadm route/view/api 파일은 adm과 분리한다.
4. 메뉴는 ADM 메뉴 체계에 등록하되 모듈 구분은 bizadm으로 관리한다.
```

---

### 6.5 메뉴 등록 시 보안/마스킹 속성

메뉴/버튼/API 등록 시 다음 속성을 포함하거나 연계한다.

```text
menuId
moduleId
menuCode
menuName
apiPath
buttonCode
requiredPermission
maskingRequiredYn
unmaskPermissionCode
downloadPermissionCode
downloadReasonRequiredYn
auditRequiredYn
personalInfoYn
sensitiveInfoYn
enabledYn
sortOrder
```

마스킹 처리 기준:

```text
1. 개인정보 또는 민감정보가 있는 목록/상세 화면은 기본 마스킹한다.
2. 마스킹 해제는 별도 권한이 있어야 한다.
3. 마스킹 해제 시 사유 입력을 필수로 한다.
4. 마스킹 해제 성공/실패를 감사 로그에 남긴다.
5. 다운로드 시 기본 마스킹을 적용한다.
6. 마스킹 해제 다운로드는 별도 권한과 사유가 있어야 한다.
7. 다운로드 성공/실패를 adm_download_audit_log에 남긴다.
```

---

### 6.6 bizadm 샘플 테이블

추천 테이블:

```text
bizadm_admin_user
bizadm_menu_sample
bizadm_role_sample
bizadm_permission_sample
bizadm_customer
bizadm_product
bizadm_order
bizadm_project_setting
bizadm_masking_audit_sample
```

단, ADM의 실제 권한/감사 테이블이 이미 있다면 중복 설계하지 말고, bizadm 샘플은 ADM 공통 권한/감사 체계를 사용하는 예제로 구성한다.

모든 테이블은 다음을 지킨다.

```text
lower snake case
한글 COMMENT 필수
created_by
created_at
updated_by
updated_at
transaction_global_id 필요 시 포함
module_id 필요 시 포함
was_id 필요 시 포함
server_instance_id 필요 시 포함
```

---

## 7. `exs` 모듈 신설

### 7.1 모듈 정의

`exs` 모듈을 신설한다.

정의:

```text
exs는 대외 연계 주제영역 모듈이다.
대외기관, 대외 채널, endpoint, 인증, OAuth/JWT 토큰, 대외 거래, 송수신 로그, 기관별 연동 설정/통제, 대외 요청 라우팅을 담당한다.
```

`settings.gradle`에 다음 모듈을 추가한다.

```text
exs
```

모듈 책임:

```text
1. 대외기관 관리
2. 대외 채널 관리
3. 대외 endpoint 관리
4. 대외 인증 프로파일 관리
5. OAuth/JWT 토큰 관리
6. 대외 거래 내역 관리
7. 대외 송수신 내역 관리
8. 기관별 enabled/disabled 통제
9. 기관별 연동 설정 통제
10. 대외 요청 라우팅
11. 대외 수신 로그 선저장
12. 업무 모듈 호출 전후 추적
13. 재처리/실패/차단 상태 관리 골격
```

---

### 7.2 exs API 구조

관리자 API:

```text
/api/exs/institutions
/api/exs/channels
/api/exs/endpoints
/api/exs/auth-profiles
/api/exs/tokens
/api/exs/routes
/api/exs/transactions
/api/exs/messages
/api/exs/control-policies
/api/exs/retries
```

실제 대외 수신 API:

```text
/api/exs/inbound/**
```

실제 대외 송신 또는 테스트 API:

```text
/api/exs/outbound/**
```

관리자 API와 대외 수신 API는 역할을 분리한다.

Swagger Tag:

```text
EXS-Institution
EXS-Channel
EXS-Endpoint
EXS-Auth
EXS-Token
EXS-Route
EXS-Transaction
EXS-Message
EXS-Control
EXS-Retry
EXS-Inbound
EXS-Outbound
```

---

### 7.3 exs 테이블

추천 테이블:

```text
exs_institution
exs_channel
exs_endpoint
exs_auth_profile
exs_token_store
exs_route_rule
exs_transaction_log
exs_message_log
exs_control_policy
exs_retry_log
```

필수 공통 컬럼:

```text
created_by
created_at
updated_by
updated_at
```

대외 거래/송수신 로그 필수 컬럼:

```text
transaction_global_id
external_transaction_id
institution_id
institution_code
channel_code
endpoint_id
route_id
module_id
was_id
server_instance_id
request_at
response_at
elapsed_ms
direction
http_method
request_uri
status
result_code
error_code
error_message
retryable_yn
```

송수신 전문 저장 기준:

```text
원문 전문 전체 저장 여부는 설정으로 분리한다.
기본은 요약/마스킹 저장이다.
민감정보는 기본 마스킹한다.
원문 조회는 별도 권한과 사유가 필요하다.
원문 조회/다운로드는 감사 로그를 남긴다.
```

---

### 7.4 대외 수신 거래 처리 흐름

대외 요청 수신 흐름은 다음을 기준으로 구현한다.

```text
1. 대외 요청 수신
2. 표준 트랜잭션 글로벌 ID 헤더 확인
3. 없거나 포맷이 틀리면 표준 오류 응답
4. 대외기관 식별
5. 기관 enabled/disabled 확인
6. endpoint/route 사용 가능 여부 확인
7. 인증/토큰/서명 검증 골격 수행
8. exs_transaction_log 선저장
9. exs_message_log 수신 로그 저장
10. 내부 표준 요청으로 변환
11. 업무 모듈 호출
12. 업무 응답 수신
13. exs_transaction_log 결과 갱신
14. exs_message_log 응답 로그 저장
15. 대외 응답 반환
```

중요:

```text
대외 수신 로그는 업무 모듈 호출 전에 저장해야 한다.
업무 모듈 호출 실패 시에도 대외 요청 수신 사실과 실패 원인이 남아야 한다.
가능하면 선저장은 별도 트랜잭션(REQUIRES_NEW 등)으로 처리한다.
```

---

### 7.5 OAuth/JWT/토큰 관리

구현 대상:

```text
1. 기관별 인증 방식 관리
2. OAuth client 정보 관리 골격
3. JWT 발급/검증 설정 관리 골격
4. 토큰 저장소 구조
5. 토큰 만료/갱신 상태
6. Mock token provider
7. 실제 외부 인증 서버 부재 시 mock/fallback 동작
```

주의:

```text
실제 secret 값은 소스에 넣지 않는다.
local/mock profile에서만 샘플 값을 사용한다.
prod profile에는 기본 secret을 두지 않는다.
```

---

## 8. ADM 기능 보강

### 8.1 ADM 다운로드

다음 항목을 보강/검증한다.

```text
1. adm_download_audit_log SQL 존재 확인
2. 다운로드 사유 필수
3. 다운로드 권한 검사
4. 마스킹 기본 적용
5. 마스킹 해제 권한 분리
6. 마스킹 해제 사유 필수
7. 다운로드 성공 감사
8. 다운로드 실패 감사
9. 온라인 거래 로그 CSV 다운로드
10. 오류 로그 CSV 다운로드
11. 배치 실행 CSV 다운로드
12. 알림 발송 이력 CSV 다운로드
13. 대외 거래 CSV 다운로드
14. 대외 송수신 CSV 다운로드
15. bizadm 업무 데이터 CSV 다운로드 샘플
```

Controller에서 `JdbcTemplate`을 직접 사용하지 않는다.

구조:

```text
Controller
→ Service
→ DTO
→ Mapper 또는 Repository
```

---

### 8.2 ADM 알림

다음 항목을 보강/검증한다.

```text
1. 알림 규칙 목록
2. 알림 규칙 상세
3. 알림 규칙 등록
4. 알림 규칙 수정
5. 알림 규칙 비활성
6. 발송 이력 조회
7. 테스트 발송
8. 감사 사유 필수
9. NotificationSender 인터페이스
10. MockNotificationSender
11. 배치 실패 알림
12. 배치 지연 알림
13. 배치 미수행 알림
14. 온라인 거래 오류 알림
15. 대외 API 오류 알림
16. 시스템 오류 알림
```

실제 Redis/Kafka/MQ 서버가 없으므로 실연동 성공을 완료 기준으로 삼지 않는다.

대신 다음을 완료 기준으로 삼는다.

```text
mock/fallback sender로 동작
실제 서버 미설정 시 앱 정상 기동
알림 이벤트 발생 흐름 검증
발송 이력 저장
실패 이력 저장
```

---

### 8.3 Batch Scheduler

다음 항목을 실제 구현/검증한다.

```text
1. pfw_batch_schedule 기준 enabled 스케줄 조회
2. cron 실행 대상 판단
3. 영업일 판단 골격
4. 수행 가능 시간 판단 골격
5. 중복 실행 방지 lock
6. execution target 생성
7. JobLauncher 실행
8. execution/step 저장
9. 실패 메시지 저장
10. 실패/지연/미수행 알림 이벤트 발행
11. ADM 배치 실행 목록/상세 조회 연계
12. run-once API와 scheduler 자동 실행 구분
13. transactionGlobalId/moduleId/wasId/serverInstanceId 저장
```

배치 화면에는 반드시 인스턴스 식별값이 보여야 한다.

---

## 9. Java 파일 물리 포맷 정리

일부 Java 파일이 raw 기준으로 한 줄에 붙어 보이는 문제가 있을 수 있다.

반드시 확인할 것:

```text
package 선언과 import가 한 줄에 붙어 있지 않아야 한다.
여러 import가 한 줄에 붙어 있지 않아야 한다.
annotation/class/method가 한 줄에 과도하게 붙어 있지 않아야 한다.
모든 Java 파일은 물리 개행 기준으로 정상 포맷이어야 한다.
```

우선 확인 대상:

```text
pfw/src/main/java/cpf/pfw/common/logging/TransactionIdGenerator.java
AdmBatchController
AdmBatchOperationService
AdmNotificationController
AdmDynamicLogLevelController
AdmDownloadController
신규 bizadm 전체 Java 파일
신규 exs 전체 Java 파일
```

`check-java-format.ps1`가 이 문제를 잡도록 보강한다.

---

## 10. 문서 현행화

다음 문서를 반드시 현행화한다.

```text
README.md
specs/index.html
specs/프레임워크_구성_가이드.html
specs/개발_가이드.html
specs/관리자_가이드.html
specs/SQL_가이드.html
specs/기능_구현_매트릭스.html
CPF_STABILIZATION_REPORT.html
```

반영 내용:

```text
1. bizadm 모듈 정의
2. exs 모듈 정의
3. ADM과 bizadm 경계
4. 트랜잭션 글로벌 ID 공식 규격
5. moduleId/wasId/serverInstanceId 로그/화면/DB 기준
6. 대외 거래 로그 선저장 기준
7. 다운로드/마스킹/감사 기준
8. Redis/Kafka/MQ mock/fallback 기준
9. 미검증 항목 정직하게 기록
```

HTML 문서는 실제 HTML이어야 한다.

필수 구조:

```html
<!doctype html>
<html lang="ko">
<head>...</head>
<body>
<main>
<section>...</section>
<table>...</table>
</main>
</body>
</html>
```

확장자만 `.html`이고 내용이 Markdown이면 실패다.

---

## 11. 기능 구현 매트릭스 현행화

`specs/기능_구현_매트릭스.html`은 실제 검수 문서로 유지한다.

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

상태 값은 다음만 사용한다.

```text
완료
일부 구현
미구현
미검증
실패
```

이번 작업 후 최소 다음 항목을 추가/현행화한다.

```text
bizadm 모듈
bizadm 관리자 사용자 샘플
bizadm 메뉴 샘플
bizadm 권한 샘플
bizadm 버튼 권한 샘플
bizadm API 권한 샘플
bizadm 다운로드 샘플
bizadm 마스킹/마스킹 해제 샘플
exs 대외기관 관리
exs 대외채널 관리
exs endpoint 관리
exs OAuth/JWT 토큰 관리
exs 대외거래 로그
exs 대외 송수신 로그
exs 대외 수신 로그 선저장
트랜잭션 글로벌 ID 필수 헤더 검증
WAS ID 7자리 검증
serverInstanceId 로그/화면 표시
```

---

## 12. SQL 기준

SQL은 다음 기준을 지킨다.

```text
1. MariaDB 기준
2. lower snake case
3. 모든 테이블/컬럼 한글 COMMENT
4. created_by, created_at, updated_by, updated_at 공통 감사 컬럼
5. transaction_global_id 컬럼은 필요한 로그/감사/거래 테이블에 반영
6. module_id, was_id, server_instance_id 컬럼 반영
7. 대외 로그 테이블은 external_transaction_id 별도 보유
8. 다운로드/마스킹/원문 조회 감사 기준 반영
```

추가 또는 보강 대상 테이블:

```text
bizadm_admin_user
bizadm_menu_sample
bizadm_role_sample
bizadm_permission_sample
bizadm_customer
bizadm_product
bizadm_order
bizadm_project_setting
exs_institution
exs_channel
exs_endpoint
exs_auth_profile
exs_token_store
exs_route_rule
exs_transaction_log
exs_message_log
exs_control_policy
exs_retry_log
adm_download_audit_log
adm_operation_audit_log
pfw_transaction_log
pfw_error_log
pfw_batch_execution
pfw_batch_step_execution
pfw_batch_execution_target
adm_notification_send_log
```

기존 테이블이 있으면 중복 생성하지 말고 ALTER 또는 문서 정합성 기준으로 정리한다.

---

## 13. OpenAPI/Swagger 기준

다음이 `/v3/api-docs`와 Swagger UI에 보여야 한다.

```text
ADM-Batch
ADM-Notification
ADM-Log
ADM-Dynamic-Log-Level
ADM-Download
BIZADM-Admin-User
BIZADM-Menu
BIZADM-Role
BIZADM-Permission
BIZADM-Customer
BIZADM-Product
BIZADM-Order
BIZADM-Setting
BIZADM-Download
BIZADM-Masking
EXS-Institution
EXS-Channel
EXS-Endpoint
EXS-Auth
EXS-Token
EXS-Route
EXS-Transaction
EXS-Message
EXS-Control
EXS-Inbound
EXS-Outbound
EDU API tag
```

Swagger 설명에는 다음이 포함되어야 한다.

```text
표준 거래 헤더
트랜잭션 글로벌 ID 필수 여부
오류 응답
표준 응답
권한 오류
마스킹/다운로드 사유
```

---

## 14. Smoke/검증 스크립트

다음 스크립트를 유지/보강한다.

```text
scripts/check-legacy-name.ps1
scripts/check-utf8.ps1
scripts/check-sql-standard.ps1
scripts/check-java-format.ps1
scripts/check-html-docs.ps1
scripts/smoke-openapi.ps1
scripts/smoke-adm-ui.ps1
```

추가 검토:

```text
scripts/check-transaction-id-standard.ps1
```

검사 내용:

```text
1. TransactionIdGenerator 포맷 검증 테스트 실행 여부
2. wasId 7자리 기준 문서/설정 반영 여부
3. SQL에 transaction_global_id/module_id/was_id/server_instance_id 반영 여부
4. OpenAPI에 트랜잭션 헤더 설명 존재 여부
5. Java raw 포맷 깨짐 여부
```

---

## 15. 실행 명령

가능한 범위에서 다음을 실행하고 결과를 리포트에 기록한다.

```powershell
.\gradlew.bat clean compileJava --offline
.\gradlew.bat test --offline
.\gradlew.bat qualityGate --offline
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-legacy-name.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-java-format.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-html-docs.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-openapi.ps1
powershell -NoProfile -ExecutionPolicy Bypass -File scripts/smoke-adm-ui.ps1
```

MariaDB가 실제로 가능한 경우에만 실행한다.

```powershell
mysql -u root -p < specs/sql/00_all_install_and_smoke.sql
```

앱을 실제 기동한 경우에만 OpenAPI HTTP smoke를 성공으로 기록한다.

브라우저 자동화 도구가 없으면 실제 브라우저 클릭 검증은 `미검증`으로 기록한다.

---

## 16. 완료 기준

### 16.1 산출물 기준

```text
CPF_STABILIZATION_REPORT.html 하나만 유지
CPF_STABILIZATION_REPORT.md 없음
CPF_STABILIZATION_CHANGED_FILES.txt 없음
README/specs 현행화
기능 구현 매트릭스 현행화
```

### 16.2 모듈 기준

```text
settings.gradle에 bizadm 포함
settings.gradle에 exs 포함
bizadm 디렉터리/Gradle 구성 존재
exs 디렉터리/Gradle 구성 존재
adm과 bizadm 책임 경계 문서화
```

### 16.3 트랜잭션 글로벌 ID 기준

```text
34자리 공식 규격 문서화
wasId 7자리 검증
모든 요청 헤더 필수 검증
누락 시 표준 오류 처리
PFW TransactionContext/MDC/응답 헤더 연계
CMN 생성/검증/전파 유틸 제공
WebClient outbound header 전파
온라인/대외/배치/감사/다운로드 로그 저장
단위 테스트 존재
```

### 16.4 화면/로그 기준

```text
ADM 거래 로그 목록/상세에 transactionGlobalId/moduleId/wasId/serverInstanceId 표시
ADM 오류 로그 목록/상세에 transactionGlobalId/moduleId/wasId/serverInstanceId 표시
ADM 배치 목록/상세에 transactionGlobalId/moduleId/wasId/serverInstanceId 표시
ADM 다운로드 감사 목록/상세에 transactionGlobalId/moduleId/wasId/serverInstanceId 표시
EXS 거래/송수신 목록/상세에 transactionGlobalId/externalTransactionId/moduleId/wasId/serverInstanceId 표시
```

### 16.5 bizadm 기준

```text
업무 관리자 화면 샘플 존재
업무 관리자 사용자/메뉴/권한/버튼/API 권한 샘플 존재
다운로드/마스킹/마스킹 해제/감사 샘플 존재
Vue 관리자 콘솔 route/view/api 분리
Swagger Tag 존재
기능 매트릭스 반영
```

### 16.6 exs 기준

```text
대외기관/채널/endpoint/auth/token/route/transaction/message/control/retry 골격 존재
대외 수신 로그 선저장 구조 존재
transactionGlobalId와 externalTransactionId 분리 저장
OAuth/JWT mock/fallback provider 존재
Swagger Tag 존재
기능 매트릭스 반영
```

---

## 17. 리포트 작성 기준

`CPF_STABILIZATION_REPORT.html`에는 다음 섹션을 포함한다.

```text
1. 작업 요약
2. 실제 변경 파일 요약
3. 검증 결과
4. 성공 항목
5. 실패 항목
6. 미검증 항목
7. 정적 확인 항목
8. 트랜잭션 글로벌 ID 반영 내역
9. 서버 인스턴스 식별값 반영 내역
10. bizadm 반영 내역
11. exs 반영 내역
12. ADM 보강 내역
13. 문서/SQL/Swagger/Smoke 반영 내역
14. 다음 잔여 과제
```

리포트에는 반드시 다음을 구분한다.

```text
실제 실행한 것
소스 정적 확인만 한 것
환경 부재로 미검증인 것
실패한 것
```

다시 강조한다.

```text
실제로 실행하지 않은 항목을 성공으로 적지 않는다.
ChatGPT 또는 사용자가 나중에 검증할 예정이라고 해서, Codex가 검증 완료라고 대신 쓰면 안 된다.
Codex는 본인이 실제 수행한 검증만 성공으로 보고한다.
```

---

## 18. 금지사항

```text
Git commit 금지
Git push 금지
branch 생성 금지
실행하지 않은 검증을 성공으로 기록 금지
ChatGPT가 나중에 검증할 예정인 항목을 Codex가 검증 완료로 기록 금지
앱을 기동하지 않고 OpenAPI HTTP smoke 성공 기록 금지
DB를 실행하지 않고 MariaDB SQL 성공 기록 금지
브라우저 클릭을 하지 않고 브라우저 클릭 검증 성공 기록 금지
정적 파일 확인만 하고 런타임 성공으로 기록 금지
CPF_STABILIZATION_CHANGED_FILES.txt 생성/갱신 금지
CPF_STABILIZATION_REPORT.md 유지 금지
ADM에 bizadm 업무 기능 혼입 금지
업무 모듈에서 임의 트랜잭션 ID 포맷 생성 금지
대외기관 거래 ID와 CPF transactionGlobalId 혼용 금지
wasId 길이 가변을 공식 규격처럼 방치 금지
Java 파일 한 줄 포맷 방치 금지
HTML 확장자에 Markdown 내용 작성 금지
FPS/Fps/fps 레거시 명칭 잔재 방치 금지
한글 깨짐/mojibake 방치 금지
```

---

## 19. 최종 보고 형식

작업 완료 후 다음 형식으로 보고한다.

```text
[성공]
- 실제 실행해서 성공한 항목만 기록
- 실행 명령과 결과를 함께 기록

[실패]
- 실패한 명령, 오류 메시지, 원인, 조치 필요 사항 기록

[미검증]
- 환경 부재 또는 미실행 항목 기록
- 왜 미검증인지 명확히 기록
- ChatGPT 또는 사용자가 나중에 검증할 예정인 항목도 여기 포함

[정적 확인]
- 파일 존재, 소스 구조, 문서 내용 등 실행 없이 확인한 항목 기록

[주요 변경 파일]
- 변경 파일과 변경 목적 요약

[트랜잭션 글로벌 ID 반영]
- 공식 규격
- 필수 헤더 검증
- wasId 7자리 검증
- 로그/DB/화면 반영
- 테스트 결과

[bizadm 반영]
- 모듈/기능/API/UI/SQL/문서/테스트 요약

[exs 반영]
- 모듈/기능/API/UI/SQL/문서/테스트 요약

[잔여 과제]
- 다음 작업으로 남길 항목
```

최종 보고에서도 실제로 실행하지 않은 검증은 절대 성공으로 쓰지 않는다.
