# CPF Enterprise QA 중간 잔여 작업

## 우선순위 P0

1. External Institution layout/version/timeout 최신 보강분 compile 및 실제 WebClient 호출 회귀검증
2. Runtime Feature 잔여 실제 Consumer 연결
   - Batch Schedule/Concurrency/Retry/Calendar/Partition/Center-Cut/Agent Policy
   - Feature Flag/Tenant Policy
   - Session/MFA/Allowlist/Approval/Retention/Backup 정책 중 실제 Owner 미연결 항목
   - Observability Exporter/Alert/Metric/Log Sink 정책
3. Runtime Control DB 상태값·Java Enum·API 상태 전수 parity
4. Generator Template의 Agent capability/application/deployment contract 전수 검증
5. Gateway streaming/multipart/range/backpressure 및 actual Principal adapter 검증

## 우선순위 P1

1. Runtime Control ADM UI: Change Center, preview, rollout, ACK, drift, exception, audit, retention
2. BZA login exact idempotent replay와 3 Vendor SQL 최종 회귀
3. Cache durable delivery/restart replay 전체 Consumer 검증
4. Permission canonical manifest/API/menu/button/role parity
5. Service Registry CRUD/Group/operation result 전체 Runtime E2E

## Closing

1. Java 25/Gradle 9.1 전체 Build/Test
2. 3 Vendor install/upgrade/rollback/checksum
3. Frontend build/Browser E2E
4. 다중 인스턴스/부분 실패/response-loss/target-down/clock-skew 검증
5. Secret/Hygiene/Dead Code/Stale Evidence 정리
6. QA Inventory 1,214개 + Scenario 201개 최종 상태 재판정
7. Current Request/Handover/Validation/Evidence 최종 정본화
8. 최종 Root Overlay ZIP 및 SHA-256 생성
