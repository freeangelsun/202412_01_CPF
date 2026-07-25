# CPF R9 이후 전체 잔여 Gap

기준: master `f1d85cf` + R9 overlay. 아래는 R9 Source 반영 후에도 상용 제품 완료를 위해 남는 항목이다. 같은 Gap을 다른 이름으로 중복 집계하지 않는다.

## P0 — 구현을 계속 닫아야 하는 제품 핵심

### Architecture / Ownership
1. Core Batch physical cleanup을 실제 프로젝트에 적용한 뒤 전체 compile로 잔여 import/bean/test가 0인지 확인.
2. Core의 다른 `common/*` 중 Admin/BAT/업무 구현 성격 class를 API/SPI/Internal 관점에서 추가 분류. 특히 ServiceCall/Database/File/Broker/Logging의 Public surface를 최소화.
3. ADM→Owner Module 직접 DB 접근 잔존 전수검색: BAT/REF/Generated Domain/BZA DB 직접 `JdbcTemplate` 사용이 남으면 Port로 교체.
4. Generated Domain이 Core/Common의 내부 구현 class를 직접 import하지 않는지 Architecture Gate 강화.

### ADM Control Plane
5. Break-glass session을 실제 위험조치 Owner Command에 scope별로 소비하도록 연결. 모든 위험조치에 blanket bypass 금지.
6. Break-glass 종료 후 post-review 미처리 알림, TTL 만료 자동 정리, MFA/dual-control 연계.
7. Incident 자동 생성: SLO/Alert/Unknown/Batch Ghost/DLQ 임계 이벤트와 연결.
8. Incident Postmortem/Runbook/배포·설정변경 correlation.
9. Maintenance drain grace, in-flight request count, long-running batch/center-cut coordination, 종료 조건.
10. Topology를 Service→Endpoint→Instance뿐 아니라 Service dependency edge/call health까지 graph로 표시.
11. Capacity/SLO 장기 metric store, percentile, trend/forecast, alert threshold.
12. Recovery Center에서 UNKNOWN/DLQ/Saga/Center-Cut 재조정의 owner/approval/audit 결과를 하나의 work queue로 통합.
13. Agent/Worker lease/fencing/ghost 상태와 수동 drain/reclaim command UI.

### BZA / 조직 / 결재
14. Approval escalation 정책: reminder, timeout, substitute/reassign, policy-based escalation.
15. 조직개편/휴직/퇴직/부재 중 approval snapshot과 delegation edge case 자동 처리.
16. Approval policy version rollback/compare/diff UI.
17. 조직도 대규모 데이터 virtualized tree/search/breadcrumb.
18. 사용자 다중 Role 충돌/deny precedence/effective permission 설명 UI.
19. Role/Permission matrix bulk edit + impact preview + approval/audit 연계.
20. 업무 관리자 Dashboard를 approval SLA, org change, expired delegation, security/session anomaly 중심으로 확장.

### Center-Cut / Batch
21. R9 `BatRemoteCenterCutHandler`의 실제 HTTP 또는 messaging transport 기본 구현.
22. Generated Domain generator가 remote Center-Cut handler/service endpoint scaffold를 생성하도록 Template 연결.
23. Center-Cut 결과불명 전용 상태/재조정 정책을 `RETRY_REQUESTED` 임시 표현보다 명시 모델로 고도화.
24. multi-instance claim/lease/fencing/stop/reprocess 실제 경쟁 테스트.
25. Scheduler misfire/catch-up/calendar/holiday policy와 Agent failover evidence.
26. Batch parameter snapshot/secret masking/재실행 diff 조회 UI.

### Multi-tenant / Product capability
27. HTTP Header/JWT tenant resolver 및 위조/불일치 fail-closed.
28. Async/Executor/Batch/Message context tenant propagation과 context leak 방지.
29. Row/Schema/Database 격리 전략별 DataSource/SQL enforcement.
30. Tenant별 audit/masking/retention/rate-limit/secret/config 경계.
31. Generator에서 tenant-aware Domain option과 migration/rollback 생성.
32. ADM tenant lifecycle/health/quota/maintenance 화면.
33. Capability Registry에 실제 package assembly/license provider/expiry/grace/offline license 정책 연결.

### Data Lineage / Reliability
34. Lineage Batch/File/Broker/External adapter consumer hook.
35. Lineage persistent store, retention, masking, query API, ADM graph/search UI.
36. Self-healing Health/Alert event source → Orchestrator 연결.
37. Owner별 안전 action adapter와 approval/audit mapping.
38. repeated-failure 자동중단 후 Incident 자동 생성 및 operator handoff.
39. Retry storm/bulkhead/backpressure adaptive control의 runtime fault evidence.

### DB / Migration
40. R9 canonical source merge를 실제 적용하고 `cpf-tools/db/source` 0 확인.
41. V6/V29 pre-GA repair fresh install 검증.
42. 기존 old checksum DB에서 reset/repair/upgrade 정책을 명시하고 실제 upgrade rehearsal.
43. V42/V43/V44 upgrade/rollback 실행 검증.
44. MariaDB full install/reset/reinstall/migration/rollback/backup/restore.
45. PostgreSQL/Oracle/MSSQL 등 지원 Vendor의 platform DDL/migration parity. 미지원 Vendor는 fail-closed.
46. Primary/Replica 실제 datasource config, lag threshold, read-after-write, replica failover.
47. 실행계획 baseline, slow query regression, index selectivity, large-table pagination/partition 기준.

## P1 — 상용화 품질과 실행 검증

### Security
48. ADM/BZA MFA 실제 Provider.
49. OIDC/JWT/API Key/mTLS provider별 integration/E2E.
50. secret rotation/KMS/Vault SPI와 실제 provider evidence.
51. Download/unmask dual control과 break-glass scope 연계.
52. session fixation/CSRF/CORS/CSP/Secure Header/browser hardening.
53. SBOM/dependency/license/vulnerability Gate와 release evidence.
54. Audit tamper resistance/retention/export/immutable sink.

### API / Integration
55. OpenAPI와 실제 header/envelope/error/paging/cursor/file/async/SSE contract parity.
56. Consumer contract test와 backward compatibility matrix.
57. Fixed-length/file/archive/attachment 대용량·부분실패·resume·checksum 검증.
58. External token/certificate/mTLS/timeouts/retry/idempotency 운영 UI와 generated-domain EDU.
59. Broker Outbox/Inbox/DLQ 실제 broker runtime, duplicate/redelivery/partition/rebalance fault test.

### Frontend UX
60. ADM/BZA 실제 browser E2E.
61. 1280/1024/768/mobile responsive viewport 검증.
62. keyboard-only/focus order/ARIA/contrast/accessibility 검사.
63. loading/skeleton/empty/error/403/409/timeout/unknown UX 전체 화면 일관성.
64. large table virtualization/export/saved filter/detail drawer UX.
65. destructive operation confirmation + reason + approval reference + result audit 표준 컴포넌트.
66. dark/light theme를 공식 지원할지 정책 확정. 지원 시 token 기반으로 두 화면 통일.

### Generator / EDU / Docs
67. arbitrary Domain create→DB→CRUD→build→runtime→remove→regenerate parity.
68. EXS generated lifecycle 실제 검증 및 삭제 후 baseline clean 확인.
69. Golden Domain에 header/security/masking/retry/idempotency/async/file/external/center-cut EDU coverage.
70. OpenAPI/JavaDoc/EDU Matrix를 Source/API와 자동 parity 확인.
71. cpf-tools manual을 install/generator/db/migration/verify/evidence/troubleshooting 관점으로 최종 보강.
72. Developer/Admin/Operator/Deploy guides cross-link와 stale 문서 제거.

### Release / Deployment / DR
73. Java 25/Gradle 9.1 clean test assemble.
74. ADM/BZA npm ci/test/build 및 Gradle frontend integration.
75. JAR/WAR/static artifact deploy smoke.
76. modular monolith same-JVM + separated WAS + MSA deployment smoke.
77. multi-instance failover/load balancing/rolling restart.
78. backup/restore/PITR/DR RTO/RPO rehearsal.
79. Upgrade/Rollback/Compatibility matrix와 supported-version policy.
80. install package, checksum, artifact provenance/signing/reproducibility.

## P2 — 최종 정본화와 제품 마감

81. README를 완성 제품 소개/구조/Quick Start만 남기고 진행 정보 0 확인.
82. Root 작업/검수/로그/zip/temp/build residue 0.
83. Evidence index가 최신 SHA와 실제 run evidence만 참조하는지 stale 검사.
84. Requirement→Source/API/SQL/Test/Runtime/Evidence 양방향 링크 자동 생성.
85. Source→Requirement/Owner/Consumer 역추적 dead code 검사.
86. 최종 기능 Matrix/EDU Coverage/Gap Matrix 상태 일치.
87. 최종 Release Notes/CHANGELOG/migration notes/rollback notes.
88. 최종 DOCX/PDF는 기능·구조 안정화 후 마지막 정본화 단계에서만 생성.

## 최종 Full Verification에서 한 번에 실행할 Gate

1. Repository hygiene / secret / stale / ownership static gate
2. DB canonical sync + manifest + drift + performance baseline
3. Gradle clean test assemble
4. ADM/BZA npm ci/test/build
5. MariaDB empty/full/reset/reinstall/upgrade/rollback
6. Generator arbitrary domain + EXS lifecycle
7. Embedded / separated WAS / MSA runtime smoke
8. Local/Remote call, timeout/retry/failover/UNKNOWN
9. Async/Outbox/Inbox/DLQ/Broker
10. Batch/Scheduler/Agent/Center-Cut multi-instance
11. Security/AuthN/AuthZ/MFA/mTLS/download/break-glass
12. ADM/BZA Browser responsive/accessibility
13. Fault injection / recovery / reconciliation / compensation
14. Backup/Restore/DR
15. Evidence sanitization, SHA linkage, requirement coverage and final status recomputation
