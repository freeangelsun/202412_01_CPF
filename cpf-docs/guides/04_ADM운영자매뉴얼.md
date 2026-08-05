# CPF ADM 운영자 매뉴얼

## 문서 기준

| 항목 | 기준 |
|---|---|
| Repository | `https://github.com/freeangelsun/202412_01_CPF` |
| Branch | `master` |
| Source 기준 Commit | `61dcbbe7d81e44a4ba3534ecd0f91f7adfa4e9c5` (`04_09`) |
| 최상위 목표 정본 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 문서 표준 정본 | `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md` |
| 주 독자 | 조회자·운영자·승인자·보안담당자·운영관리자 |
| 문서 사용 결과 | 실제 ADM Route에서 상태를 조회하고 권한에 따라 조치·승인·대사·Rollback을 수행한다. |
| 구현 상태 | `완료` — 사용자가 요청한 산출물 작성 전제 |
| 이 작성 세션의 Runtime 재실행 | 수행하지 않음 |
| 문서 현행화 범위 | Source·Catalog·Route·공식 문서 구조와 절차 정합성 |

> 이 문서는 구현 기능을 사용할 수 있는 상태로 설명한다. 이 작성 세션에서 Runtime을 다시 실행하지 않았다는 사실은 기능 절차를 축소하는 근거가 아니며, 고객 환경 배포 승인 시에는 해당 환경의 실행 기록을 별도로 보존한다.
## 1. 역할과 통제

| 역할 | 허용 범위 |
|---|---|
| 조회자 | 목록·상세·상태·Timeline 조회 |
| 운영자 | 승인 없이 허용된 Refresh·Retry·Stop·Reconcile 요청 |
| 승인자 | HIGH/CRITICAL 조치의 영향·Reason 검토와 승인/반려 |
| 보안담당자 | 인증·MFA·IP·Secret·Masking·Audit 검토 |
| 운영관리자 | Config·Runtime·배포·Rollback·운영자 권한 관리 |

화면 노출 여부와 관계없이 Backend Permission, Data Scope, Reason, Approval, Expected Version을 검증한다.

## 2. 화면 공통 읽기

1. 기간, System, Environment, Status, Transaction/Operation ID를 입력한다.
2. 기본 조회 기간과 최대 건수를 확인한다.
3. 목록의 상태·Version·Owner·Updated Time·Risk를 확인한다.
4. 상세의 요청·Attempt·Error·Target·Audit Timeline을 확인한다.
5. 조치가 필요한 경우 Button 활성 조건과 Required Input을 먼저 확인한다.
6. 화면 Timeout 시 같은 Button을 반복 누르지 않고 Operation ID로 조회한다.
7. 결과가 Partial이면 Target별 ACK/NACK와 Observed Version을 확인한다.

## 3. 실제 Route 카탈로그

| Route | 메뉴 | 위험도 | 주 기능 | 형태 |
|---|---|---|---|---|
| / | 통합 운영 Dashboard | MEDIUM | Health·UNKNOWN·DLQ·Batch 요약 | 조회 |
| /topology | 서비스 토폴로지 | MEDIUM | Service·Instance·Endpoint·Health | 조회 |
| /capacity | Online Runtime Diagnostics | MEDIUM | Runtime·Outbox·Inbox·File 상태 | 조회 |
| /logs | 거래 로그 | MEDIUM | 검색·상세·Export | 조회/비동기 |
| /transactionGroups | Online·Batch 통합 Trace | MEDIUM | Transaction·Trace Timeline | 조회 |
| /transactions | 온라인 거래 정의 | HIGH | 거래 정의 조회·Scan·비활성 | 조회/변경 |
| /remoteLogs | 원격 로그 | MEDIUM | 검색·Preview·Bundle | 조회/비동기 |
| /auditLogs | 감사 로그 | MEDIUM | 감사·Delivery·Retry | 조회/변경 |
| /logLevel | 동적 로그 | HIGH | Rule 등록·제거 | 변경 |
| /logPolicies | 로그 정책 | MEDIUM | 정책·배포 상태 | 조회/변경 |
| /channelPolicy | 채널 정책 | HIGH | Snapshot·Save·Import/Export | 변경 |
| /serviceRegistry | 서비스 레지스트리 | MEDIUM | Service·Instance·Endpoint 관리 | 조회/변경 |
| /runtimeControl | Deployment·Promotion·Rollback | HIGH | Preview·Apply·Cancel·Rollback | 변경 |
| /maintenance | 점검·Drain | HIGH | 점검·Traffic Drain | 변경 |
| /cache | 캐시 | HIGH | Refresh·Evict·Reconcile | 변경 |
| /configs | 설정 | HIGH | Config 생성·변경·삭제 | 변경 |
| /responseCodes | 응답코드 | MEDIUM | 응답코드 관리 | 조회/변경 |
| /businessCalendar | 영업일·휴일 | MEDIUM | 일자 조회·저장·삭제 | 조회/변경 |
| /recoveryCenter | 복구 센터 | MEDIUM | Log Recovery·Poison Retry·UNKNOWN·DLQ | 복구 |
| /incidents | Error·Unknown Result | HIGH | Incident 생성·전이·정책 | 조회/변경 |
| /reliability | Analysis Center | MEDIUM | Outbox·Inbox·DLQ·Idempotency 분석 | 조회 |
| /notifications | 알림 | MEDIUM | Rule·Delivery·Retry·Cancel·Test | 조회/변경 |
| /batch | Batch / Center-Cut | MEDIUM | Job·Schedule·Execution·Worker | 조회 |
| /batch-overview | Batch Overview | MEDIUM | Overview·Lock·Worker·Schedule | 조회 |
| /batch-runtime | Runtime Topology | HIGH | Runtime Command·상태 | 조회/변경 |
| /batch-scheduler | Scheduler HA | MEDIUM | Simulation·Enable·Disable·Run Once | 조회/변경 |
| /batch-center-cut | Center-Cut | MEDIUM | 대상·결과·Parameter | 조회 |
| /batch-job-packs | Job Packs | MEDIUM | Definition·Validate·Transition | 조회/변경 |
| /batch-executions | Executions | MEDIUM | Execution 상세·Retry·Stop | 조회/변경 |
| /batch-deployment | Deployment / Rollback | HIGH | 배포 Plan·Command 상태 | 변경 |
| /batch-recovery | Recovery / Unknown | MEDIUM | Ghost·UNKNOWN 정상화 | 복구 |
| /batch-leases | Lease / Fencing | MEDIUM | Lock 조회·Release | 조회/변경 |
| /file-jobs | 대량파일 Job | MEDIUM | Upload·Apply·Retry·Rollback·Unknown | 조회/변경 |
| /messages | 전문·Protocol Message | MEDIUM | 전문 정의 관리·Trace | 조회/변경 |
| /gateway-dashboard | Gateway 대시보드 | MEDIUM | 운영 Snapshot·Event·Stream | 조회 |
| /gateway-routes | Gateway 경로·라우팅 | MEDIUM | Binding 저장·상태·삭제 | 조회/변경 |
| /gateway-security | Gateway 보안·제한 | HIGH | 인증·제한 정책 | 변경 |
| /gateway-health | Gateway Health·연결시험 | MEDIUM | Connection Test·재검증 | 조회/변경 |
| /gateway-apply-status | Gateway 적용 상태·이력 | MEDIUM | ACK/NACK·Operation | 조회 |
| /permissions | 권한 | MEDIUM | Menu·Button·API·Role Matrix | 조회/변경 |
| /password | 비밀번호 | HIGH | 정책·변경·Reset·Session Revoke | 변경 |
| /security | 보안 | HIGH | MFA·IP Allowlist | 조회/변경 |
| /operators | 운영자 | HIGH | 운영자·Role·Session | 조회/변경 |
| /secrets | Secret / Key | HIGH | Metadata·Provider·Rotate | 조회/변경 |
| /approvals | 위험조치 승인 | HIGH | Policy·Request·Decision·Execute | 승인 |
| /breakGlass | Break-glass | HIGH | Emergency Session·Review·Close | 승인/변경 |
| /featureFlags | Feature Flag | CRITICAL | Evaluate·Override·Kill Switch | 승인/변경 |
| /resiliencePolicies | Resilience 정책 | CRITICAL | 정책 요청·승인·반려 | 승인/변경 |

## 4. 조회 전용 메뉴 운영

Dashboard, Topology, Capacity, Trace, Reliability, Center-Cut 결과처럼 조회 중심 화면은 다음 순서로 사용한다.

1. 환경·System·기간·상태를 좁힌다.
2. 목록에서 이상 상태와 Last Updated를 확인한다.
3. 상세에서 Owner·Dependency·Attempt·Error와 Correlation ID를 확인한다.
4. 관련 거래 로그·Trace·Audit로 이동한다.
5. 변경이 필요하면 전용 조치 화면으로 이동하고 조회 화면에서 상태를 임의 변경하지 않는다.

Empty는 장애와 동일하지 않다. 권한·Data Scope·기간·검색 조건을 먼저 확인한다.

## 5. Runtime Control

### Preview 입력

Target Group/Instance, Change Key/Value, Effective Time, Expected Current Version, Reason을 입력한다.

### 처리

1. Target Snapshot과 Preview Hash를 확인한다.
2. HIGH 변경은 Approval을 요청한다.
3. Apply 후 Target별 ACK/NACK·Applied Version·Observed Hash를 확인한다.
4. 일부 실패 시 성공 Target을 다시 적용하지 않는다.
5. 실패 Target만 재시도하거나 LKG/전체 Rollback을 선택한다.
6. Desired·Applied·Observed가 일치하고 Audit가 기록되면 종료한다.

## 6. Recovery Center와 UNKNOWN

1. Transaction/Operation ID로 UNKNOWN을 조회한다.
2. 업무 원장, Outbox/Inbox, Provider Tracking, Owner Audit를 확인한다.
3. Resolution Code와 Evidence Reference를 입력한다.
4. 성공·실패를 근거로 확정하거나 미확정으로 유지한다.
5. 근거 없이 성공/실패로 변경하지 않는다.
6. DLQ Replay 전 Consumer 수정·중복 차단·대상 범위를 확인한다.

## 7. Incident

상태는 Open → Investigating → Mitigated → Resolved 흐름을 사용한다. 각 전이에 담당자, Reason, 관련 거래/Operation, 조치, 남은 위험과 Evidence를 기록한다. Mitigated는 원인 제거와 동일하지 않으므로 재발 방지와 대사를 완료한 뒤 Resolved로 전환한다.

## 8. Batch 운영

- 실행 전: Job Version·Schedule·Worker·Lock·Preview·Approval 확인
- 실행 중: Step·Partition·Throughput·Error·Lease 확인
- Stop: 안전 지점 중지 요청
- Restart: 기존 Checkpoint 재개
- Abandon: Restart 금지 상태로 종결
- Reprocess: 새 Operation으로 대상 재처리
- Ghost/UNKNOWN: 업무 원장·Batch Metadata 대사 후 정상화
- Lease Release: Owner Process가 종료됐고 Fencing 조건을 확인한 경우에만 수행

## 9. Gateway 운영

Server Group·Route·Security·Health·Apply Status 메뉴를 함께 사용한다.

1. Route Draft와 Target Allowlist를 확인한다.
2. 연결시험에서 DNS·TLS·Authentication·Timeout 결과를 확인한다.
3. Approval 후 Publish한다.
4. Instance별 ACK/NACK와 Route Checksum을 확인한다.
5. NACK Instance를 Traffic에서 제외한다.
6. Drift가 남으면 재적용 또는 LKG Rollback한다.

## 10. Feature Flag

1. Flag Key·Provider·기본값·현재 Override를 조회한다.
2. 대상 Tenant/User/환경·시작/종료·Reason을 입력한다.
3. Preview와 Evaluation 결과를 확인한다.
4. CRITICAL 조치는 승인 후 적용한다.
5. 적용 후 Evaluation Audit·Metric·오류율을 확인한다.
6. 기간 종료 또는 이상 발생 시 Override를 제거한다.
7. Kill Switch는 영향 범위와 복구 조건을 명시한다.

## 11. Resilience 정책

Timeout, Retry, Backoff, Circuit, Bulkhead를 변경한다. 비멱등 Operation에는 Retry를 추가하지 않는다. 적용 후 Latency, Failure, UNKNOWN, Queue/Connection Pool 부하를 비교한다. 악화되면 이전 Policy Version으로 되돌린다.

## 12. Config·Cache·Log·Secret

- Config: Expected Version과 재기동 여부 확인
- Cache: Key/Namespace 영향 Preview 후 Evict, Source of Truth 재조회
- Dynamic Log: 대상 Package·Level·만료 시간 지정, 종료 후 원복
- Secret Rotate: 새 Version 배포 → 동시 신뢰 → Consumer Reload/Restart → 연결 확인 → 구 Version 폐기
- Password/MFA/IP: 자기 잠금과 전체 운영자 차단을 방지하는 승인·Break-glass 준비

## 13. Approval과 Break-glass

승인자는 요청자, 대상, Before/After, 영향, Preview Hash, Reason, 만료와 Rollback을 검토한다. 자기 승인과 만료된 요청을 거부한다.

Break-glass는 정상 권한 절차가 장애 대응을 막을 때만 사용한다. 범위·시간·행위 제한을 두고 종료 후 모든 명령과 조회 이력을 리뷰한다.

## 14. Export·다운로드·감사

Export는 비동기 Job으로 생성한다. Download 권한·만료·1회성 Token·Masking·Watermark 정책을 적용한다. Audit에서 Actor, Approver, Reason, Query/Action, File Hash, Download Time을 확인한다.

## 15. 교대 인계

Incident/Operation ID, 현재 상태, 영향 System·Tenant, 마지막 조치, Approval, Target별 ACK/NACK, 남은 UNKNOWN, 다음 점검 시각, 담당자와 Rollback 조건을 전달한다.
