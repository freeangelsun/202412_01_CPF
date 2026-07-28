# ADM 실시간 Runtime Control 보완 지시 정본

- 기준: 기존 통합 QA 요청서를 대체하지 않고 Root Cause와 실제 Consumer 기준으로 병합한다.
- 금지: API/Class/Table/화면 존재, DB 저장, 현재 JVM 메모리 변경, Mock 성공만으로 완료 처리하지 않는다.

## 공통 제어 Lifecycle

ADM 조회 → 환경·도메인·시스템·Application Group·Instance 선택 → Desired/Actual/Version/적용 상태 조회 → 영향 Preview → 권한 → 사유 → 승인/Break Glass → expectedVersion/operationId/request hash → 대상별 Delivery → Instance ACK/실제 결과 → 성공/부분 성공/실패/UNKNOWN_RESULT/재시작 필요 → Retry/취소/Rollback/Reconcile → Offline 재접속 동기화 → Drift 탐지·복구 → 변경 전후/결과 불변 감사.

## 독립 Capability

1. 공통코드: 시행시점, Cache Version, ACK, Offline 복귀, 중복/과거 Version 역전 방지, Rollback.
2. 메시지·응답코드: 다국어와 Mapping 무결성, 실제 API 응답 적용, Preview·차단·Rollback, Applied Version/Drift.
3. 설정·Parameter·Feature Flag: Global/환경/도메인/그룹/인스턴스 우선순위, Safe Default, Dynamic/Restart 구분, Schema/보안 검증, TTL 자동 복원.
4. Cache: 상태/Version/갱신시각/항목 수, Full/증분/Key 무효화, Transactional durable event, checkpoint/replay, poison/순서/중복, backlog/drift.
5. 온라인 거래: 거래/그룹/도메인 Enable·Block·Drain·Maintenance, 신규/진행 분리, timeout/retry/circuit/bulkhead/rate limit, trace boost TTL, 결과 불명 reconcile.
6. Service Call·Remote Call: Local/Remote 동일 계약, endpoint/timeout/retry/circuit/load balancing/failover, zone/cell, UNKNOWN_RESULT, 실제 Consumer E2E.
7. Gateway: route/method/header/auth/target/weight/priority/zone/cell, timeout/retry/circuit/rate/payload, drain, canary/wave/rollback, dry run/last-good, multipart/range/streaming.
8. 로그·추적: logger/package/거래/도메인/instance level, sampling/trace boost TTL, masking, Desired/Actual/Drift, instance log 목록/다운로드.
9. Batch Runtime: scheduler/job/worker/center-cut/agent 제어, pause/resume/cancel/kill/reprocess, lock/fencing/ghost, partition reassignment, checkpoint restart, preview/approval/CAS/audit.
10. Security Runtime Policy: masking/password/permission/JWT/certificate/secret reference, downgrade 금지, rotation/grace, fail-closed, version/ACK, 원문 비노출.
11. 외부연계·Messaging·File: endpoint/layout/timeout/retry, broker enable/concurrency/retry/DLQ, file/SFTP/webhook, simulator/fake/testkit. 실제 외부 실행은 별도 Evidence 없으면 미검증.
12. 인스턴스 관리: 환경→Cluster→Domain/System→Group→Instance, 역할/ID/host/port/JVM/version/commit/start/heartbeat/lease/status, rolling version, membership version/CAS/audit.
13. 인스턴스 로그 다운로드: 필터/목록/current/rotation/archive, streaming 단일/ZIP, cancel/timeout/partial, path/symlink/root/secret 차단, masking, temp cleanup, 권한/사유/승인/감사.
14. 관제·알림: Event→Durable Outbox→Worker→Provider, 원 업무 분리, rule/severity/suppression/silence/escalation, preview/test/history/retry, Email/SMS/Webhook/ADM, Simulator와 실제 Provider 판정 분리, 3 DB portable SQL.

## 완료 추적

`CPF_ADM_RUNTIME_CONTROL_SUPPLEMENT_TRACEABILITY.csv`에서 기존 Requirement ID와 병합하며 신규 중복 Requirement 수를 늘리지 않는다. Runtime Evidence가 없는 상태는 완료가 아니다.
