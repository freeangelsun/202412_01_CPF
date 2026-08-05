# CPF ADM 개발자 매뉴얼

## 문서 기준

| 항목 | 기준 |
|---|---|
| Repository | `https://github.com/freeangelsun/202412_01_CPF` |
| Branch | `master` |
| Source 기준 Commit | `61dcbbe7d81e44a4ba3534ecd0f91f7adfa4e9c5` (`04_09`) |
| 최상위 목표 정본 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 문서 표준 정본 | `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md` |
| 주 독자 | 고객 업무 개발자·ADM 연동 개발자 |
| 문서 사용 결과 | 고객 업무의 조회·조치·승인·비동기 작업·복구 계약을 기존 ADM 제품에 연결한다. |
| 구현 상태 | `완료` — 사용자가 요청한 산출물 작성 전제 |
| 이 작성 세션의 Runtime 재실행 | 수행하지 않음 |
| 문서 현행화 범위 | Source·Catalog·Route·공식 문서 구조와 절차 정합성 |

> 이 문서는 구현 기능을 사용할 수 있는 상태로 설명한다. 이 작성 세션에서 Runtime을 다시 실행하지 않았다는 사실은 기능 절차를 축소하는 근거가 아니며, 고객 환경 배포 승인 시에는 해당 환경의 실행 기록을 별도로 보존한다.
## 1. ADM 연동의 책임 경계

ADM은 `cpf-admin`이 제공하는 운영 제품이다. 고객 업무 개발자는 ADM 자체를 다시 만들지 않고 Owner Module의 Query·Command Contract를 연결한다.

- 조회: Owner가 제공하는 읽기 계약 사용
- 변경: Owner가 제공하는 Command 사용
- DB: ADM에서 Owner DB 직접 갱신 금지
- Local: Same-JVM Adapter
- Remote: OpenAPI/HTTP Adapter + Service Identity
- 결과 불명: Operation ID·Idempotency Key로 조회
- Frontend: Generated Client와 Route Operation Contract 사용

## 2. 연동 설계표

| 항목 | 결정 내용 |
|---|---|
| 업무 기능 | 조회·변경·승인·비동기·복구 |
| Owner | 실제 상태와 DB를 소유하는 Module |
| Query | 검색·상세·상태·Timeline |
| Command | Target·Reason·Expected Version·Idempotency |
| Permission | Menu·Button·API Permission |
| Data Scope | Tenant·조직·업무·소유자 |
| Masking | 목록·상세·Export 수준 |
| Risk | LOW·MEDIUM·HIGH·CRITICAL |
| Approval | 정책·승인자·만료 |
| Audit | Actor·Approver·Before/After·Result |
| Recovery | Timeout·UNKNOWN·Partial Apply·Rollback |

## 3. 조회 기능 연결

1. Owner에 Page/Detail Query Contract를 정의한다.
2. 검색 Field, 기본값, 최대 기간·건수와 정렬을 명시한다.
3. ADM Backend Adapter에서 Permission·Data Scope를 전달한다.
4. Same-JVM과 Remote가 같은 DTO를 반환하도록 Contract Test한다.
5. OpenAPI Operation ID와 Frontend Generated Client를 갱신한다.
6. Route `expectedOperationIds`에 실제 Operation을 등록한다.
7. 권한 없음·Empty·Timeout·Owner Down을 Browser Test한다.

조회 전용 화면에는 변경 Button이나 Approval 절차를 만들지 않는다.

## 4. 조치 기능 연결

Command 필수 항목:

- 대상 Resource/Operation ID
- 목표 상태 또는 Action
- `expectedVersion`
- `idempotencyKey`
- `reason`
- Actor·Tenant·Permission Context
- Approval ID 또는 Policy 결과
- Client Request Time과 Timeout Budget

처리 순서:

1. ADM이 입력과 권한을 검증한다.
2. Owner가 상태·Version·업무 규칙을 다시 검증한다.
3. Owner가 상태 변경과 Audit를 Transaction으로 처리한다.
4. ADM은 Owner Result와 Operation ID를 반환한다.
5. Browser Timeout 시 같은 Button을 다시 누르지 않고 Operation 조회 API를 호출한다.

## 5. 위험 조치와 승인

HIGH/CRITICAL 조치는 Preview와 Apply를 분리한다.

1. Preview에서 대상·현재값·예상값·영향·Target Snapshot·Hash를 반환한다.
2. 요청자는 Reason과 유효 시간을 입력한다.
3. 승인자는 Preview Hash와 영향을 확인해 승인/반려한다.
4. 승인된 요청만 Apply한다.
5. Owner가 Expected Version과 Approval 상태를 재검증한다.
6. 결과와 Before/After를 Audit에 기록한다.

승인 만료, Preview 이후 대상 변경, 자기 승인, 이미 처리된 Approval은 거부한다.

## 6. 비동기 작업

Export, 배포, 대량 조치, 연결시험은 비동기 Operation으로 처리한다.

- Create 응답: Operation ID, 상태, 조회 URI
- 상태: REQUESTED·RUNNING·SUCCEEDED·FAILED·UNKNOWN·CANCELLED
- Progress: 대상·완료·실패·UNKNOWN 건수
- Cancel: 아직 취소 가능한 단계에서만 허용
- Artifact: 만료와 Download Permission 적용

## 7. Partial Apply

Target별 ACK/NACK를 응답한다. 성공 Target을 다시 적용하지 않는다. 실패 Target의 Error·Observed Version을 표시하고 다음 중 하나를 선택한다.

- 실패 Target만 재시도
- 실패 Target 격리
- LKG Version 적용
- 전체 Rollback
- Drift 상태로 유지하고 Incident 생성

## 8. UNKNOWN과 Reconciliation

Timeout, Connection Reset, 응답 파싱 실패가 부작용 발생 이후일 수 있으면 UNKNOWN으로 기록한다.

1. Operation ID와 Request Hash를 조회한다.
2. Owner Audit·업무 원장·Provider Tracking을 확인한다.
3. 성공·실패·미확정을 구분한다.
4. Resolution Code, Evidence Reference, Resolver, Time을 기록한다.
5. 성공 확정 전 재실행하지 않는다.
6. 미확정이 지속되면 Incident와 수동 대사 절차로 전환한다.

## 9. Permission·Masking·Reason·Audit

Menu 표시 권한, Button 활성 권한, API 실행 권한을 분리한다. 화면에서 숨겨도 Backend Permission을 반드시 재검증한다.

Data Scope와 Masking은 조회·상세·Export에 동일하게 적용한다. Audit에는 Actor, Approver, Target, Action, Before/After, Reason, Expected/Applied Version, Result, Correlation ID를 포함한다.

## 10. OpenAPI와 Generated Client

1. Backend Controller Operation ID를 고정한다.
2. Request/Response/Error Schema를 생성한다.
3. `cpf-admin/frontend/openapi/cpf-openapi.json`을 갱신한다.
4. Generated Client를 재생성한다.
5. Route Registry의 `expectedOperationIds`와 대조한다.
6. Typecheck·Unit·Build·Browser Test를 실행한다.

Generated Client를 수동 수정하지 않는다.

## 11. Route·Menu·Feature Flag

Route 정본은 `cpf-admin/frontend/src/app/routes.ts`다. Route는 `routeId`, `path`, `menuId`, `label`, `group`, `ownerModule`, `riskLevel`, `featureFlag`, `expectedOperationIds`, `component`를 선언한다.

새 Route 추가는 기존 ADM 기능으로 해결할 수 없는 고객 전용 화면에서만 수행한다. 기존 Route에 Owner Operation을 연결할 수 있으면 새 화면을 만들지 않는다.

## 12. Browser·Fault Test

```powershell
cd cpf-admin/frontend
npm ci
npm run lint
npm run typecheck
npm test -- --run
npm run build
npx playwright test --project=chromium --project=firefox --project=webkit
```

시험 항목:

- Menu·Button·API 권한 없음
- Data Scope 밖 결과 0건
- Masking 수준별 표시
- 오래된 Expected Version
- Approval 거절·만료
- Backend Timeout과 응답 유실
- Partial Apply와 Target별 NACK
- Session 만료·CSRF
- Rollback 후 Desired/Observed 일치

## 13. 운영 인계

Route, Operation ID, Owner Module, Local/Remote Binding, Timeout, Permission·Data Scope·Masking, Risk·Approval, 상태·오류, UNKNOWN·Reconcile, Partial Apply·Rollback, Log·Metric·Trace·Audit와 담당자를 전달한다.
