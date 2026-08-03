# CPF ADM 매뉴얼


## 문서 기준과 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 기준 Commit: `e134c1f275c306c0e9ab4a044d9140ac4b3ca620`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 사용자 지시 적용: 요구사항에 정의되고 Source에 연결된 기능은 사용 가능한 제품 기능으로 설명한다.
- 이 문서 작업에서 직접 수행한 Runtime·DB·Browser·다중 인스턴스 검증: `미검증`
- 문서와 Source의 경로·식별자 정합성 검토: `완료`

> Runtime 미검증은 기능 절차를 생략하는 이유가 아니다. 다만 실행 결과를 직접 확인하지 않은 항목은 배포 승인 시 해당 환경의 Evidence로 다시 확인한다.


## 1. ADM의 역할

ADM은 고객이 새로 개발하는 Framework가 아니라 CPF가 제공하는 플랫폼 운영 제품이다. 고객 업무 개발자는 Owner Query/Command Contract를 연결하고, 운영자는 권한에 따라 조회·조치·승인·감사·복구한다.

Backend: `cpf-admin/**`
Frontend Route 정본: `cpf-admin/frontend/src/app/routes.ts`
OpenAPI 정본: `cpf-admin/frontend/openapi/cpf-openapi.json`

## 2. 연동 개발 원칙

1. Owner Module에 Query/Command Contract를 정의한다.
2. ADM은 Same-JVM 또는 Remote Adapter로 Contract를 호출한다.
3. ADM이 Owner DB를 직접 Update하지 않는다.
4. Command에는 Idempotency, Expected Version, Reason, Actor를 전달한다.
5. Timeout 후 결과 불명은 Operation ID로 조회한다.
6. 화면 Route의 `expectedOperationIds`와 OpenAPI Operation ID를 일치시킨다.

## 3. 권한 모델

- 조회자: 목록·상세·상태 조회
- 운영자: 낮은 위험도 Retry/Refresh
- 승인자: HIGH/CRITICAL 변경 승인
- 보안담당자: 인증·Masking·Secret·Audit 검토
- 운영관리자: 정책·Runtime Change·Rollback

Permission 외에 Data Scope, Masking, Reason, Approval, Expected Version을 함께 적용한다.

## 4. 주요 Route

| Route | 메뉴 | 위험도 | 작업 |
|---|---|---|---|
| `/` | 통합 운영 Dashboard | MEDIUM | Health·UNKNOWN·DLQ·Batch 요약 |
| `/topology` | 서비스 토폴로지 | MEDIUM | Service/Instance/Endpoint/Health |
| `/capacity` | Online Runtime Diagnostics | MEDIUM | Runtime·Outbox·Inbox·File 상태 |
| `/logs` | 거래 로그 | MEDIUM | 검색·상세·Export |
| `/transactionGroups` | Online·Batch 통합 Trace | MEDIUM | Transaction/Trace Timeline |
| `/runtimeControl` | Deployment·Promotion·Rollback | HIGH | Preview·Apply·Cancel·Rollback |
| `/recoveryCenter` | 복구 센터 | MEDIUM | UNKNOWN Resolve·DLQ Replay |
| `/incidents` | Error·Unknown Result | HIGH | Incident 생성·전이·정책 |
| `/reliability` | Analysis Center | MEDIUM | Outbox/Inbox/DLQ/Idempotency 분석 |
| `/notifications` | 알림 | MEDIUM | Rule·Delivery·Retry·Cancel·Test |
| `/batch` | Batch / Center-Cut | MEDIUM | Job·Schedule·Execution·Worker |
| `/feature-flags` | Feature Flag | HIGH | 상태·Override·Audit |
| `/resilience-policies` | Resilience Policy | HIGH | Retry/Circuit/Timeout 정책 |

실제 Route 전체 목록과 Feature Flag는 `routes.ts`를 확인한다.

## 5. 화면 공통 사용 절차

1. 검색 기간·System·Status·Transaction ID를 입력한다.
2. 기본값과 최대 조회 범위를 확인한다.
3. 목록 Column에서 상태, Version, Last Updated, Owner를 확인한다.
4. 상세에서 원 요청, Attempt, Error, Audit Timeline을 확인한다.
5. 조치 버튼의 활성 조건과 Risk를 확인한다.
6. Reason, Expected Version, Idempotency Key를 입력한다.
7. Approval이 필요한 경우 승인자에게 요청한다.
8. 결과가 Timeout이면 재클릭하지 않고 Operation ID로 상태를 조회한다.
9. Partial Apply면 Target별 ACK/NACK를 확인한다.
10. Audit에서 요청·승인·결과를 확인한다.

## 6. Runtime Control

### Preview

대상 Group/Instance, 변경 Key/Value, Current Version, Desired Version, 영향을 확인한다. Preview Hash와 Target Snapshot을 보존한다.

### Apply

Reason, Approval, Expected Version과 Effective Time을 입력한다. Target별 ACK/NACK와 Applied Hash를 확인한다.

### Partial Apply

성공 Target을 다시 적용하지 않는다. 실패 Target 원인을 수정해 재시도하거나 LKG/전체 Rollback을 선택한다.

### Rollback

Rollback 대상 Version과 영향 Preview를 확인하고 승인 후 수행한다. Desired/Applied/Observed가 다시 일치해야 완료다.

## 7. Recovery Center

- Transaction Log Recovery 상태 조회
- Poison Retry
- Broker DLQ Replay 요청
- UNKNOWN Result Resolve

UNKNOWN은 근거 없이 성공/실패로 변경하지 않는다. Provider Probe, 업무 원장, 외부 조회, Audit를 확인하고 Resolution Code와 Evidence를 입력한다.

## 8. Incident

Signal을 수집해 Incident를 생성하고 상태를 Open → Investigating → Mitigated → Resolved로 전이한다. 각 전이는 담당자, Reason, 관련 Transaction/Operation, 조치와 Evidence를 기록한다.

## 9. Feature Flag

1. Flag Key와 현재 Provider/Value를 조회한다.
2. 대상 Tenant/User/환경과 기간을 지정한다.
3. Preview에서 영향 대상과 기본값을 확인한다.
4. HIGH 변경은 Approval을 받는다.
5. Apply 후 Evaluation Audit와 Metric을 확인한다.
6. 종료 시 Override를 제거하고 기본 Provider 상태를 확인한다.

## 10. Resilience Policy

Timeout, Retry Count/Backoff, Circuit Threshold, Bulkhead를 변경한다. 비멱등 Operation에 Retry를 추가하지 않는다. 정책 변경 후 실패율, Latency, UNKNOWN 증가 여부를 확인하고 필요 시 Rollback한다.

## 11. Notification 운영

Rule, Delivery Log, Attempt를 조회한다. Retry 전 Provider 상태와 Receipt를 확인한다. 결과 불명 전송을 무조건 재전송하지 않는다. Cancel은 아직 Provider로 전달되지 않은 상태에서만 수행한다.

## 12. Batch 운영

Job/Schedule/Execution/Worker/Lock을 조회한다. Stop, Restart, Abandon, Reprocess의 의미를 구분한다. Abandon 후에도 업무 원장 대사는 별도로 완료해야 한다.

## 13. 감사·Export

Export는 비동기 Job으로 생성하고 다운로드 만료와 권한을 적용한다. 개인정보는 화면과 파일에서 동일하게 Masking한다. Audit에는 Actor, Approver, Reason, Before/After, Expected/Applied Version과 Result를 포함한다.

## 14. 응답 유실 대응

버튼 클릭 후 Timeout이면 같은 조치를 반복하지 않는다. 화면의 Operation ID 또는 요청 Idempotency Key로 상태를 조회한다. 상태가 없을 때만 재요청 정책을 적용한다.

## 15. Browser·Fault Test

```powershell
cd cpf-admin/frontend
npm ci
npm run lint
npm run typecheck
npm test -- --run
npm run build
npx playwright test --project=chromium --project=firefox --project=webkit
```

권한 없음, 오래된 Expected Version, Backend Timeout, Partial Apply, Approval 거절, Session 만료를 포함한다.
