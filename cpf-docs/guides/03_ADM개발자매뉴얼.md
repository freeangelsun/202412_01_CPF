# CPF ADM 개발자 매뉴얼

> 기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`
> 기준 Branch: `master`
> 기준 Commit: `ee977cf66c251081df78ea5e9675b81c3dfafa59` (`06_07`)
> 기준일: `2026-08-06 Asia/Seoul`

| 항목 | 내용 |
|---|---|
| 주 독자 | 고객 업무 개발자·ADM 연동 개발자, Frontend 경험이 많지 않은 개발자 |
| 이 문서로 완료할 일 | 업무 Owner의 Query·Command를 Same-JVM·Remote Adapter, OpenAPI, Generated Client, Route, 화면과 Browser Test로 연결한다. |
| 읽는 방식 | 처음 접하는 독자는 앞에서부터 실습 순서로 읽고, 숙련자는 장별 판단표와 참조 경로를 사용한다. |
| 설명 원칙 | 제품 기능은 사용할 수 있는 상태로 설명한다. 실제 Source·SQL·API·Config·Frontend·Script·Test의 이름과 경계를 유지한다. |


![ADM 연동 개발 여정](../assets/manuals/cpf-adm-integration-journey.svg)

## 1. ADM 연동의 경계

ADM 연동 개발자는 ADM 제품을 새로 만드는 사람이 아니다. 고객 업무의 조회·조치·승인·감사·복구 기능을 기존 ADM에 연결한다.

- 최종 상태는 고객 업무 Owner가 소유한다.
- ADM Backend는 Owner Query/Command Port 또는 Remote API를 호출한다.
- Frontend는 Generated Client를 사용한다.
- Route Registry는 Route·Menu·Risk·Feature Flag·Operation ID의 정본이다.
- ADM은 Owner DB를 직접 수정하지 않는다.
- 화면 Toast를 업무 상태 정본으로 사용하지 않는다.

## 2. 예제: PAY 결제수단 운영 화면

운영자는 고객·상태로 결제수단을 검색하고, 상세를 확인한 뒤 정지 요청을 만든다. 응답 유실이면 Operation을 조회하고 Owner 원장과 대사한다.

| 항목 | 예제 |
|---|---|
| Route | `/pay-methods` 고객 확장 Route |
| Menu | `PAY_METHOD` |
| Query Operation | `payAdmFindMethods`, `payAdmFindMethod` |
| Command Operation | `payAdmSuspendMethod`, `payAdmResumeMethod` |
| Permission | Menu·Button·API·Customer Data Scope |
| 위험 입력 | Reason·Expected Version·Idempotency·Approval ID |
| 상태 | ACTIVE·SUSPENDED·UNKNOWN_RESULT |

## 3. Owner Port

```java
public interface PayMethodAdmPort {
    PayMethodPage findMethods(PayMethodCriteria criteria, AdminActor actor);
    PayMethodDetail findMethod(String methodId, AdminActor actor);
    PayOperationResult suspend(SuspendPayMethodCommand command, AdminActor actor);
    PayOperationResult findOperation(String operationId, AdminActor actor);
}
```

Query와 Command를 분리한다. Query 결과에는 기준시각, Owner, Version을 포함한다.

## 4. Same-JVM Adapter

```java
@Component
final class LocalPayMethodAdmAdapter implements PayMethodAdmPort {
    private final PayMethodQueryService query;
    private final PayMethodCommandService command;

    public PayMethodPage findMethods(PayMethodCriteria criteria, AdminActor actor) {
        return query.find(criteria, actor.toSecurityContext());
    }

    public PayOperationResult suspend(SuspendPayMethodCommand request, AdminActor actor) {
        return command.suspend(request, actor.toSecurityContext());
    }
}
```

Local Adapter도 Permission·Version·Idempotency 의미를 우회하지 않는다.

## 5. Remote Adapter

Remote Adapter는 Generated Client를 사용하고 Service Identity, Audience, Timeout, Trace Header, Error Mapping을 추가한다.

```java
@Component
final class RemotePayMethodAdmAdapter implements PayMethodAdmPort {
    private final PayOwnerGeneratedClient client;

    public PayOperationResult suspend(SuspendPayMethodCommand command, AdminActor actor) {
        return client.suspend(
            actor.serviceIdentity(),
            actor.traceHeaders(),
            command,
            Duration.ofSeconds(5));
    }
}
```

Timeout이 Dispatch 전인지 후인지 구분할 수 있어야 한다. Dispatch 뒤 응답 유실은 `UNKNOWN_RESULT`와 Operation 조회 경로를 반환한다.

## 6. Controller

```java
@RestController
@RequestMapping("/adm/api/pay-methods")
final class PayMethodAdmController {

    @GetMapping
    @CpfPermission("PAY_METHOD_READ")
    PayMethodPage find(PayMethodCriteria criteria, AdminActor actor) { ... }

    @PostMapping("/{methodId}/suspend")
    @CpfPermission("PAY_METHOD_SUSPEND")
    PayOperationResult suspend(
        @PathVariable String methodId,
        @RequestBody SuspendPayMethodRequest request,
        AdminActor actor) { ... }
}
```

Controller는 인증·HTTP·입력 형식을 다루고 업무 상태 전이는 Owner가 판단한다.

## 7. Command 입력 계약

```json
{
  "expectedVersion": 7,
  "reason": "분실 신고 접수에 따른 일시 정지",
  "idempotencyKey": "dfe9356e-25d0-4c43-a19d-3f75c2ea772a",
  "approvalRequestId": 4812
}
```

Button은 다음 조건이 모두 맞을 때 활성화한다.

- 현재 상태가 `ACTIVE`.
- 사용자가 `PAY_METHOD_SUSPEND` Button·API Permission을 가짐.
- 대상이 사용자 Data Scope 안에 있음.
- 최신 Version이 상세 화면과 일치.
- Reason이 유효.
- 정책상 필요한 Approval이 `APPROVED`이고 Snapshot이 일치.
- 이전 Operation이 미종결 상태가 아님.

## 8. OpenAPI·Generated Client

1. Controller에 고유 Operation ID를 선언한다.
2. Request·Response·Error Schema를 생성한다.
3. 중복 Operation ID와 Schema 오류를 검사한다.
4. Generated Client를 재생성한다.
5. Frontend에서 Raw URL 호출을 제거한다.
6. Route Registry의 `expectedOperationIds`와 비교한다.
7. Contract Test와 TypeScript Compile을 실행한다.

`cpf.openapi.webmvc`는 API Docs와 관리 Snapshot을 제공한다. 운영 환경에서 API Docs 노출 여부는 별도 정책으로 결정한다.

## 9. Route Registry

```ts
export interface AdmCapabilityRoute {
  routeId: string;
  path: string;
  menuId: string;
  label: string;
  group: AdmFeatureGroup;
  riskLevel: AdmRouteRiskLevel;
  featureFlag: string;
  expectedOperationIds: readonly string[];
}
```

Route를 추가할 때 다음을 함께 변경한다.

- Route Registry.
- Vue Page와 Component.
- Menu·Button·API Permission.
- OpenAPI Operation.
- Generated Client.
- Route Test.
- Browser Test.
- 04 운영자 매뉴얼.

## 10. Frontend 화면 구조

| 영역 | PAY 예 | 독자가 확인할 것 |
|---|---|---|
| 검색 | 고객 ID·상태·기간 | Default·최대 기간·Validation |
| 목록 | ID·Masked Account·상태·Version·Updated | Sort·Masking·Timezone |
| 상세 | 상태 Timeline·Operation·Audit | 목록 Row를 그대로 신뢰하지 않고 재조회 |
| 조치 | 정지·재개 | Button 조건·Reason·Version·Approval |
| 결과 | Operation Banner | `SUCCEEDED`, `FAILED`, `UNKNOWN_RESULT`, `PARTIAL` 구분 |

Loading, Empty, Validation Error, Forbidden, Owner Timeout을 같은 빈 화면으로 표시하지 않는다.

## 11. Vue 예제

```vue
<script setup lang="ts">
import { computed, ref } from 'vue';
import { payMethodApi } from '../../generated/payMethodApi';

const selected = ref<PayMethodDetail | null>(null);
const reason = ref('');
const loading = ref(false);
const canSuspend = computed(() =>
  selected.value?.status === 'ACTIVE' &&
  selected.value.permissions.includes('PAY_METHOD_SUSPEND') &&
  reason.value.trim().length >= 10 &&
  !loading.value
);

async function suspend() {
  if (!selected.value) return;
  loading.value = true;
  try {
    const result = await payMethodApi.suspend(selected.value.methodId, {
      expectedVersion: selected.value.version,
      reason: reason.value.trim(),
      idempotencyKey: crypto.randomUUID()
    });
    await reloadOperation(result.operationId);
  } finally {
    loading.value = false;
  }
}
</script>
```

Client가 승인 여부를 Boolean으로 직접 보내 승인하는 방식을 사용하지 않는다.

## 12. `06_07` 통합 운영 정정 승인

![통합 운영 정정 승인](../assets/manuals/cpf-integration-closure-screen.svg)

현재 Route:

```text
/integrationClosure
menuId = INTEGRATION_CLOSURE
riskLevel = CRITICAL
```

연결 Operation:

- `admIntegrationCryptoStatus`
- `admIntegrationTimeHealth`
- `admIntegrationDataQualityValidate`
- `admIntegrationDataQualityCorrectionApprovalRequest`
- `admIntegrationDataQualityCorrectionExecute`
- `admIntegrationDataQualityReplay`
- `admIntegrationWebhookDlq`
- `admIntegrationWebhookReplay`

정정 승인 요청은 `expectedVersion`, `idempotencyKey`, `reason`, `corrected`를 보내며 Server가 Snapshot을 고정한다. 실행은 Approval ID와 Reason만 사용한다. 실행 단계에서 정정 Payload를 다시 받지 않는다.

## 13. Permission·Masking·Export

- Route 진입 Permission.
- Button Action Permission.
- Backend API Permission.
- Row Data Scope.
- Field Masking 또는 원문 조회 Permission.
- Export Permission·Reason·행 제한·만료·Download Audit.

목록에서 Masked Field를 보여 주더라도 상세나 Export에서 원문이 노출되지 않는지 Negative Test한다.

## 14. Timeout·Version·Idempotency UI

| 상황 | 화면 표시 | 허용 행동 |
|---|---|---|
| Query Timeout | 조회 실패 Banner | 같은 조건 Retry |
| Command Dispatch 전 실패 | `FAILED` | 입력·Health 확인 뒤 새 요청 |
| Command 응답 유실 | `UNKNOWN_RESULT` | Operation 조회, Button 비활성 |
| Version 충돌 | 비교 Dialog | 최신 상세 재조회 후 재입력 |
| 같은 Key·같은 Payload | 기존 결과 | 새 Operation 생성 금지 |
| 같은 Key·다른 Payload | Conflict | 새 의도면 새 Key |
| 일부 Target 성공 | `PARTIAL` | 실패 Target만 조치 |

## 15. Browser Test

### 15.1 정상

1. 권한 있는 사용자로 Route 진입.
2. 검색 Default와 Column 확인.
3. 상세 재조회.
4. Reason 입력과 Button 활성화.
5. Command 실행.
6. Operation·Owner 상태·Audit 일치 확인.

### 15.2 Negative

- Menu 권한 없음.
- Button 권한 없음.
- 다른 조직 Data Scope.
- Masking 원문 노출.
- Stale Version.
- Approval 만료·반려.
- Owner Timeout.
- 응답 유실.
- 여러 Target 부분 적용.

### 15.3 예제 명령

```powershell
$env:CPF_FRONTEND_URL='https://adm.example'
$env:CPF_E2E_RELEASE='true'
$env:CPF_E2E_AUTH_STATE='D:\secure\adm-auth.json'
# Repository의 실제 Playwright 명령을 package.json에서 확인해 실행한다.
```

## 16. Contract Parity Test

같은 Fixture를 Local과 Remote Adapter에 실행한다.

```java
@ParameterizedTest
@MethodSource("payMethodAdapters")
void suspend_contract_is_same(PayMethodAdmPort adapter) {
    PayOperationResult result = adapter.suspend(validCommand(), operator());
    assertThat(result.status()).isEqualTo("SUSPENDED");
    assertThat(result.operationId()).isNotBlank();
}
```

Remote Test에는 Serialization·401·403·409·Timeout·응답 유실을 추가한다.

## 17. 운영 인계

| 항목 | 기록 |
|---|---|
| Route·Menu | Path·Menu ID·Group·Risk·Feature Flag |
| Owner | Query/Command Port·Remote Base URL |
| Operation | OpenAPI ID·Generated Client Method |
| 화면 | 검색·Column·상세·Button·활성 조건 |
| Permission | Menu·Button·API·Data Scope·Masking |
| 오류 | Validation·Forbidden·Conflict·Timeout·Unknown·Partial |
| 복구 | Operation 조회·Reconcile·Retry·Rollback |
| Evidence | Contract·Browser·Audit·Trace·Screenshot |

## 18. ADM 연동 자체 검수

1. ADM이 Owner DB를 직접 수정하지 않는가?
2. Query와 Command Port가 분리됐는가?
3. Local·Remote Error 의미가 같은가?
4. Operation ID가 OpenAPI·Client·Route·Audit에 연결되는가?
5. Raw URL과 수기 DTO가 없는가?
6. 화면 상태가 Owner 상태를 복제한 정본이 아닌가?
7. Button 조건에 Permission·Version·Reason·Approval이 있는가?
8. `UNKNOWN_RESULT`를 일반 오류 Toast로 숨기지 않는가?
9. Browser Negative Test가 있는가?
10. 04 운영자 매뉴얼에 실제 사용 절차가 반영됐는가?
