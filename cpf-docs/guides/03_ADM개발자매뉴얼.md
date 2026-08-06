# CPF ADM 개발자 매뉴얼

> 문서: `CPF ADM 개발자 매뉴얼`
> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 기준 Commit: `6976d2747481b8540b48ddb9ab8f53cfeaa4b888` (`06_02`)
> 기준일: `2026-08-06 Asia/Seoul`

| 항목 | 내용 |
|---|---|
| 주 독자 | 고객 업무 개발자·ADM 연동 개발자 |
| 문서 목적 | 고객 업무 Query·Command·승인·감사·복구 기능을 완성된 ADM 제품에 연결한다. |
| 기능 서술 전제 | CPF 제품 기능은 고객이 사용할 수 있는 상태로 설명한다. 구현 진행률이나 개발 관리 상태는 이 문서의 사용 절차에 섞지 않는다. |
| 사실 우선순위 | 실제 Source·SQL·API·Config·Frontend·Script·Test → 설계·사양 → 본 매뉴얼 |
| 상태 표현 | 업무 상태와 운영 결과는 Source의 상태값을 사용한다. 문서 검토 상태는 `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다. |

## 1. ADM 연동의 목적과 경계

ADM 연동 개발자는 CPF Admin 제품을 새로 만드는 사람이 아니라 고객 업무 Owner의 조회·조치·승인·감사·복구 기능을 기존 ADM에 연결한다.

- ADM Backend는 Owner Query/Command Port 또는 Remote API를 호출한다.
- ADM Frontend는 OpenAPI Generated Client만 사용한다.
- Route Registry는 Route·Menu·Risk·Feature Flag·expectedOperationIds의 정본이다.
- ADM은 Owner DB를 직접 수정하지 않는다.
- 화면 상태는 Owner 상태를 복제한 별도 정본이 아니다.
- Raw URL·중복 Controller·수기 DTO는 Generated 계약과 충돌하므로 만들지 않는다.

## 2. 연동 설계서

업무 기능마다 다음 표를 작성한다.

| 항목 | 질문 |
|---|---|
| 업무 결과 | 운영자가 무엇을 조회·변경·복구하는가? |
| State Owner | 최종 상태를 어느 Module/Service가 소유하는가? |
| Query/Command | 조회와 부작용 Operation을 분리했는가? |
| Same-JVM/Remote | Local Adapter와 Remote Adapter 계약이 같은가? |
| 식별자 | Transaction/Operation/Attempt/Target/Version은 무엇인가? |
| Permission | Menu·Button·API·Data Scope·Masking은 무엇인가? |
| 위험 조치 | Reason·Expected Version·Approval·Idempotency가 필요한가? |
| 실패 | Timeout·응답 유실·부분 적용을 어떻게 표시하는가? |
| 복구 | Retry·Reconcile·Rollback·Compensation은 어느 Owner가 실행하는가? |
| Evidence | Log·Metric·Trace·Audit·Browser Test는 무엇인가? |

### 2.1 업무 Operation 설계 Template

| 항목 | Query 예 | Command 예 |
|---|---|---|
| 업무 목적 | 결제수단 목록/상세 조회 | 결제수단 정지 |
| Owner | PAY Application Query Port | PAY Application Command Port |
| Operation ID | `payAdmFindMethods` | `payAdmSuspendMethod` |
| 입력 | 고객·상태·기간·Page | Method ID·Expected Version·Reason·Idempotency |
| 권한 | Menu/API/Data Scope | Menu/Button/API·승인 정책 |
| 결과 | Page·Masked Field·기준시각 | 상태·Version·Operation·Audit |
| 오류 | Validation·Forbidden·Timeout | Conflict·Forbidden·UNKNOWN·PARTIAL |
| 복구 | 조건/권한/Owner Health 후 재조회 | Operation 조회→Owner 대사→새 요청/보상 |
| 화면 | 검색·Table·상세 Drawer | 위험 Action Dialog·상태 Banner |
| Test | Query/API/Browser | State/Concurrency/Response-loss/Browser |

Operation ID는 Controller Method 이름 장식이 아니라 OpenAPI·Generated Client·Route Registry·Permission·Audit를 연결하는 식별자다.

## 3. Backend Query 연결

1. Owner Query Port를 정의하거나 기존 Port를 사용한다.
2. Local Adapter는 Same-JVM Owner Service를 호출한다.
3. Remote Adapter는 Generated Client와 Service Identity를 사용한다.
4. Query DTO에는 검색 Field·Paging·Sort·Data Scope를 포함한다.
5. ADM Controller는 HTTP 의미와 Permission을 적용하고 Owner 결과를 그대로 해석한다.
6. Timeout은 조회 실패로 표시하고 상태를 추정하지 않는다.
7. 목록·상세·Export를 별도 Operation으로 둔다.

Query 결과에는 기준시각·Source/Owner·Version을 포함해 운영자가 stale 결과를 판정할 수 있게 한다.

### 3.1 고객 업무 Query Adapter 예시

아래는 고객 PAY 업무가 구현할 예시 구조이며 CPF 내부 Class 이름이 아니다.

```java
public interface PayAdmQueryPort {
    PayMethodPage findMethods(PayMethodCriteria criteria, CpfSecurityContext actor);
    PayMethodDetail findMethod(String methodId, CpfSecurityContext actor);
}

@Component
final class PayAdmQueryAdapter {
    private final PayAdmQueryPort owner;

    PayMethodPage find(PayMethodCriteria criteria, CpfSecurityContext actor) {
        criteria.validate();
        return owner.findMethods(criteria, actor);
    }
}
```

Query Adapter는 Owner가 반환한 Data Scope·Masking 결과를 확대하지 않는다. Remote Mode에서는 Timeout과 Error Mapping을 적용하고, 기준시각·Page Cursor·Owner Version을 응답에 포함한다.

### 3.2 Query 화면 계약

- 검색 Field마다 Type·Default·최대 기간·허용 Enum을 표시한다.
- Table Column마다 Sort 가능 여부·Masking·Timezone·단위를 정한다.
- Empty와 Loading, Validation 오류, 권한 없음, Owner Timeout을 서로 다른 상태로 표시한다.
- 상세 Drawer는 목록 Row의 ID로 재조회하고 stale Row를 그대로 확정값처럼 보여주지 않는다.
- Export는 별도 Permission·Reason·행 제한·만료·Download Audit를 사용한다.

## 4. Backend Command 연결

Command 입력에는 대상 ID, 현재 상태, Expected Version, Reason, Idempotency Key, Approval Request가 포함된다.

1. ADM Controller가 인증·Permission·입력 형식을 검증한다.
2. Owner Adapter가 요청자와 승인자 Identity를 전달한다.
3. Owner가 상태·Version·Idempotency·Approval을 판정한다.
4. 결과는 Operation ID와 상태를 반환한다.
5. 응답 유실 시 같은 Idempotency Key 또는 Operation ID로 조회한다.
6. 여러 Target은 Target별 결과와 Aggregate 상태를 반환한다.
7. UNKNOWN/PARTIAL을 일반 Error Toast로 숨기지 않는다.

### 4.1 Command 처리 순서

1. 인증 운영자와 요청 Body의 Actor가 다르지 않은지 확인한다.
2. Menu/Button/API Permission과 Data Scope를 평가한다.
3. Target ID·현재 상태·Expected Version을 검증한다.
4. Reason·Approval Request·Idempotency Key를 검증한다.
5. Owner Command를 Same-JVM Port 또는 Remote Adapter로 호출한다.
6. 결과가 확정되면 상태·Version·Operation ID·Audit ID를 반환한다.
7. Timeout/응답 유실이면 `UNKNOWN_RESULT`를 보존하고 기존 Operation 조회 경로를 제공한다.
8. 여러 Target 결과가 섞이면 Target별 상태와 실패 원인을 반환한다.

### 4.2 Command 응답 예시

```json
{
  "operationId": "OP-PAY-20260806-0001",
  "resourceId": "PM-000123",
  "previousStatus": "ACTIVE",
  "status": "SUSPENDED",
  "resultVersion": 8,
  "replayed": false,
  "auditId": "AUD-000991"
}
```

고객 예시 응답은 필요한 의미를 보여 준다. 실제 DTO 이름과 Field는 Owner Source/OpenAPI를 따른다.

## 5. Same-JVM·Remote Adapter

```java
public interface CustomerOrderAdminPort {
    Page<OrderView> search(OrderQuery query, AdminActor actor);
    OperationResult suspend(SuspendOrderCommand command, AdminActor actor);
}
```

Local과 Remote 구현은 같은 Port를 구현한다. Remote Adapter는 Authentication, Audience, Timeout, Serialization, Trace Header, Error Mapping을 추가하지만 업무 상태 의미를 바꾸지 않는다. Contract Test는 같은 Fixture를 두 Adapter에 실행한다.

## 6. Timeout·Expected Version·Idempotency

- Query Timeout: 조회 실패 Banner와 Retry를 제공한다.
- Command Timeout before dispatch: FAILED로 반환할 수 있다.
- Command Timeout after dispatch: UNKNOWN_RESULT로 표시하고 Operation 조회를 제공한다.
- Expected Version 충돌: 최신 Row를 재조회하고 사용자가 변경 내용을 비교한다.
- 같은 Idempotency Key·같은 Payload: 기존 결과를 반환한다.
- 같은 Key·다른 Payload: Conflict로 차단한다.
- Retry 버튼은 원본 Operation 상태를 조회한 뒤 활성화한다.

## 7. 위험 조치 승인

위험 조치는 `요청 생성 → 승인 결정 → 승인 Snapshot 확인 → 실행 → Audit` 순서다.

- 요청자와 승인자는 달라야 한다.
- Approval에는 대상 Snapshot·Action·Reason·Expiry·Policy Version을 저장한다.
- 실행 시 현재 Target Snapshot이 승인 Snapshot과 같은지 확인한다.
- 만료·반려·이미 실행된 Approval을 재사용하지 않는다.
- DangerousActionDialog는 Reason·Approval ID·Idempotency Key를 수집한다.
- 실행 결과 불명은 Approval 완료와 별개로 UNKNOWN_RESULT 대사를 수행한다.

## 8. OpenAPI·Generated Client

1. Owner/ADM Controller에 Operation ID를 선언한다.
2. OpenAPI Source를 생성하고 중복 Operation ID·Schema 오류를 검사한다.
3. Generated Client를 재생성한다.
4. Frontend는 Generated 함수와 Model만 Import한다.
5. Route Registry의 `expectedOperationIds`와 Generated Inventory를 비교한다.
6. 수기 Raw URL Method를 제거한다.
7. `cpf-admin/frontend/openapi/cpf-openapi.json`, `.cpf-openapi-source.json`, Generated Client Hash를 대사한다.
8. Consumer Contract Script와 Browser E2E를 실행한다.

`/openApiOperations` 화면은 Runtime Inventory·Source Hash·누락/중복을 확인하는 운영 화면이다.

### 8.1 생성·소비 절차

1. Owner Controller/OpenAPI Source에서 Operation ID·Schema·Error를 확정한다.
2. Duplicate Operation ID와 누락 Schema를 검사한다.
3. Generated Client를 재생성한다.
4. Frontend는 Generated Method를 사용하고 Raw URL 문자열을 만들지 않는다.
5. Request/Response Type을 Form/Table State와 연결한다.
6. `routes.ts`의 `expectedOperationIds`에 실제 소비 Operation을 등록한다.
7. Consumer Contract Script와 Browser Test를 실행한다.
8. `/openApiOperations`에서 Runtime Source Hash·Operation 수·누락/중복을 확인한다.

### 8.2 계약 변경 분류

| 변경 | Consumer 조치 | Release 조치 |
|---|---|---|
| Optional Field 추가 | Client 재생성·표시 여부 결정 | 호환 회귀 |
| Required Field 추가 | 모든 Caller 수정 | Version/배포 순서 설계 |
| Enum 추가 | Unknown 처리 확인 | 구 Client 호환 확인 |
| 상태 의미 변경 | 화면·Runbook·Audit 수정 | Migration/교육 필요 |
| Operation ID 변경 | Route·Permission·Client·Test 전부 수정 | 구 Operation 폐기 계획 |
| Error Code 변경 | UI 분류·Retry/Recovery 수정 | 운영 Runbook 갱신 |

## 9. Route·Menu·Risk·Feature Flag

Route 추가 전 기존 62개 Route 중 같은 운영 목적을 가진 화면이 있는지 확인한다. 새 Route가 필요하면 다음을 함께 변경한다.

- `routeId`, `path`, `menuId`, `label`, `group`, `ownerModule`, `riskLevel`, `featureFlag`.
- 실제 Vue Component.
- `expectedOperationIds`.
- Menu/Permission Seed와 Button Permission.
- Route Closure/Generated Client Contract Test.
- 04 운영자 매뉴얼의 화면 카드와 Operation Matrix.

Menu ID를 재사용할 때 Data Scope·Button·API Permission이 충돌하지 않는지 확인한다.

## 10. Frontend Query 화면

조회 화면은 검색 Form, 기준시각, Loading/Empty/Error 상태, Table, Detail, Paging을 분리한다.

- 검색 Field는 DTO와 같은 이름·형식·Default를 사용한다.
- Sort는 whitelist만 선택하게 한다.
- Table에는 상태·Version·Owner·Updated At를 포함한다.
- 상세는 Timeline·Attempt·Target·Audit 등 운영 판정 근거를 제공한다.
- 원문 개인정보는 권한이 있을 때 별도 Operation으로 요청하고 기본 화면에는 Masking한다.
- Retry는 조회 재시도이며 Command 재실행과 구분한다.

### 10.1 화면 구현 Checklist

| 영역 | 필수 구현 |
|---|---|
| Header | 업무 목적·Owner·기준시각·새로고침 |
| Search | Label·Type·Default·Validation·초기화 |
| Table | Column·Sort·Paging·Masking·Empty |
| Detail | 최신 재조회·Version·상태·Timeline·Audit |
| State | Loading·Empty·Validation·Forbidden·Timeout·Unknown |
| Navigation | 관련 Trace·Approval·Incident·Owner 화면 |
| Accessibility | Label·Focus·Keyboard·상태 Message·Table Header |
| Browser Test | 정상·Empty·403·409·Timeout·응답 유실·재조회 |

Query 화면은 “호출 성공”만 확인하지 않고 화면의 건수·기준시각·Owner 원장과 일치하는지 검증한다.

## 11. Frontend Command Form

Command Form에는 현재 상태와 변경 후 상태, Expected Version, Reason, Approval, Idempotency Key, Target Snapshot을 표시한다.

Button 활성 조건:

1. 대상이 선택됐다.
2. 현재 상태에서 Action이 허용된다.
3. 필수 Permission이 있다.
4. Reason과 필수 입력이 유효하다.
5. 최신 Version을 조회했다.
6. 승인 필요 Action은 승인 Ticket이 유효하다.
7. 같은 Operation이 제출 중이 아니다.

응답 후 화면을 무조건 성공 상태로 바꾸지 않고 Owner 재조회 결과를 표시한다.

### 11.1 위험 Action Dialog Field

| Field | 표시/검증 |
|---|---|
| Target Snapshot | ID·현재 상태·Version·Environment·Target 수 |
| Reason | 필수·최대 길이·제어문자 차단 |
| Expected Version | 최신 상세 조회값, stale이면 실행 차단 |
| Approval Request | 정책상 필요할 때 필수·상태·만료 표시 |
| Idempotency Key | Operation 범위에서 유일, Replay 상태 표시 |
| Preview/Diff | Before/After·영향 Target·경고 |
| Confirm | 모든 필수 조건과 권한이 충족될 때 활성 |

실행 후 Dialog를 닫기 전에 Operation ID를 보존한다. Response Loss가 발생하면 같은 Idempotency Key로 상태를 조회하고 새 Key를 무작정 만들지 않는다.

## 12. Partial Apply·UNKNOWN UI

PARTIAL 화면은 Aggregate 상태 하나로 숨기지 않고 Target별 `SUCCEEDED/FAILED/UNKNOWN/NACK/ROLLED_BACK`을 보여 준다. 사용자는 성공 Target을 다시 실행하지 않고 실패·UNKNOWN Target만 선택한다.

UNKNOWN_RESULT Banner에는 Operation ID, 마지막 Attempt, Target, 시작시각, 다음 허용 조치, Owner 대사 링크를 표시한다. `다시 실행`보다 `결과 조회`를 먼저 제공한다.

### 12.1 상태 표시 Matrix

| 상태 | Banner | 허용 Button | 다음 화면 |
|---|---|---|---|
| SUCCEEDED | 성공·Version·완료시각 | 상세·Audit | Owner Detail |
| FAILED | Error Code·실패 단계 | 입력 수정 후 새 요청 | Error/Incident |
| UNKNOWN_RESULT | 결과 불명·Operation ID | 대사 조회 | Recovery/Owner Attempt |
| PARTIAL | 성공/실패 Target 수 | 실패 Target 재적용·Rollback | Target Detail |
| ROLLED_BACK | 이전 Version·Target | 새 Preview | Runtime/Publish |
| PARTIALLY_ROLLED_BACK | 남은 Target·위험 | Target별 대사 | Incident/Approval |

UI는 UNKNOWN을 일반 오류 Toast로 숨기지 않는다. 성공한 Target을 실패 Target과 함께 재실행하지 않도록 Target별 상태를 유지한다.

## 13. Permission·Masking·Reason·Audit

- Route 진입: Menu Permission.
- Button 표시/활성: Button Permission + 현재 상태.
- API 실행: API Permission + Data Scope.
- 원문 조회/Download: 별도 Permission + Reason + Audit.
- 상태 변경: Reason·Expected Version·Operation ID.
- 위험 조치: Approval·요청자/승인자 분리.
- 모든 변경: Before/After·Actor·Reason·Approval·Transaction/Operation Audit.

Frontend 권한은 사용성 제어이며 Backend 권한이 최종 판정이다.

## 14. Critical 화면 구현 패턴

### Center-Cut

`BatchCenterCutPage.vue`는 Job 목록과 Execution 결과를 분리한다. `FAILED` 행에만 `실패 재처리`, `UNKNOWN` 행에만 `UNKNOWN 대사`를 노출한다. 첫 Confirm은 Approval Request를 만들고 두 번째 Confirm이 실행한다.

### Cache

`/cache`는 Provider Summary·Tenant/Namespace/Key·Consumer Checkpoint·Lag를 표시한다. Evict 후 DB Ledger Event와 Checkpoint를 재조회하고 Fast Signal 성공만으로 종료하지 않는다.

### Security MFA

Login DTO는 `operatorId`, `password`, `otpCode`를 사용한다. MFA 대상은 6자리 TOTP를 입력하며 Secret은 RFC 4648 Base32 Reference로 조회하고 화면·Log에 노출하지 않는다.

### Runtime Control

Preview Target과 실제 실행 Target, Desired/Observed, Target Attempt, ACK/NACK, Rollback을 표시한다.

### Approval

정책·요청·Decision·Execution을 분리하고 같은 Approval ID의 재실행을 Idempotency로 통제한다.

## 15. Browser·Fault Test

| 시나리오 | 주입 | 기대 UI |
|---|---|---|
| Query Timeout | Owner 응답 지연 | Error Banner·조건 유지·조회 Retry |
| Command 응답 유실 | Owner Commit 후 연결 종료 | UNKNOWN Banner·Operation 조회 |
| Version 충돌 | 다른 사용자가 먼저 변경 | 최신값 표시·비교·재입력 |
| Permission 변경 | Session 중 Role 회수 | Button 비활성·API 403·안내 |
| Partial Apply | Target 일부 NACK | Target별 상태·Rollback/재처리 |
| Approval 만료 | 승인 후 실행 지연 | 실행 차단·새 요청 안내 |
| Process Kill | 실행 중 Backend 종료 | 기존 Operation 대사·중복 실행 금지 |
| MFA Clock Skew | ±2 Step Code | 로그인 거부·시간 동기화 안내 |

### 15.1 필수 Browser Scenario

1. 권한 있는 조회자가 검색·Paging·상세를 수행한다.
2. Data Scope 밖 Row가 목록·Export·직접 URL에서 차단된다.
3. 운영자가 Command Dialog를 열고 stale Version으로 409를 확인한다.
4. 요청자와 승인자가 같은 경우 승인이 거부된다.
5. Owner 응답을 지연해 Timeout/UNKNOWN Banner와 Operation 조회를 확인한다.
6. Target 3개 중 1개 NACK를 반환해 PARTIAL Table과 실패 Target Action을 확인한다.
7. Response는 성공했지만 Audit 저장이 실패하는 Fault에서 업무 Transaction Rollback을 확인한다.
8. Generated Client Source Hash가 바뀌면 Consumer Contract가 실패하는지 확인한다.
9. Keyboard·Focus·Error Summary·상태 Announcement를 확인한다.
10. Chromium·Firefox·WebKit에서 핵심 Route를 회귀한다.

## 16. 연동 완료 Checklist

1. Owner Query/Command와 실제 Consumer가 연결됐는가?
2. Local/Remote Contract Test가 같은가?
3. OpenAPI Operation ID와 Generated Client가 일치하는가?
4. Route Registry의 expectedOperationIds가 최신인가?
5. 검색 Field·Column·Detail·Button이 실제 Component와 일치하는가?
6. Permission·Data Scope·Masking·Reason·Approval이 Backend에서 검증되는가?
7. Timeout·Version 충돌·응답 유실·Partial Apply UI가 있는가?
8. Operation/Attempt/Audit를 운영자가 찾을 수 있는가?
9. Browser E2E와 Fault Test가 있는가?
10. 04 운영자 매뉴얼의 해당 Route 카드가 갱신됐는가?

## 17. Integration Closure 운영 기능 연결

Integration Closure는 새 ADM 제품을 만드는 작업이 아니라 기존 ADM Store와 Generated Client에 다음 Owner Operation을 연결하는 작업이다.

| Operation | Method·Path | 화면 입력 | 표시 결과 |
|---|---|---|---|
| `admIntegrationTimeHealth` | `GET /adm/api/integration-closure/time/health` | Zone, Max Skew ms | UTC·업무시각·편차·Healthy |
| `admIntegrationDataQualityValidate` | `POST .../data-quality/validate/{recordId}` | Record ID, JSON Record | Accepted·Violation·Quarantine ID |
| `admIntegrationDataQualityCorrect` | `POST .../quarantine/{id}/correct` | Expected Version·Reason·Approved·Corrected JSON | State·Version |
| `admIntegrationDataQualityReplay` | `POST .../quarantine/{id}/replay` | Actor Session·Reason | Decision·Replay 상태 |
| `admIntegrationWebhookDlq` | `GET .../webhooks/dlq` | Limit | Delivery 목록·상태·Attempt |
| `admIntegrationWebhookReplay` | `POST .../webhooks/{id}/replay` | Expected Version·Reason | Delivery 상태·Version |

Frontend는 `integrationClosureMethods.ts`와 Generated API를 사용하며 Feature Page에서 Raw URL을 다시 작성하지 않는다. 위험 조치는 Server Session의 `adm.operatorId`, Reason, Expected Version, 승인 여부를 Owner Controller에 전달한다. Browser Test는 권한 거부, Version Conflict, 503 Time unhealthy, Webhook 429·503, 응답 유실 후 재조회까지 포함한다.
