# CPF_STABILIZATION_REPORT.md

## 검수 기준

- branch: `master`
- SHA: `42c0fda82e0f3061e839f69cad25bbfa9df2aa0f`
- 정본 domain: 133개
- architecture inventory: 1,118개 파일
- 동일 runtime 환경 재실행: 미수행

## 총평

최신 commit에는 진전이 있으나 정본 133개 도메인을 source·runtime과
양방향 대조해 완료한 근거는 없다.

보수적 작업 시작 판정:

- 재확인 필요: 77
- 부분 구현: 30
- 미구현: 19
- 미검증: 5
- 실패: 2

## 직접 확인된 중대 결함

- ACC generator lifecycle 미수행
- Batch Agent/Worker 독립 runtime 미확인
- ADM/BZA 대형 단일 JS
- 전체 계층 gate 부족
- SQL canonical source·DB portability 부족
- 실제 file/DB log parity 부족
- PFW extension model 전수 검증 부족
- external integration failure/recovery 부족
- EDU 존재 중심 완료 판정
- semantic garbage·evidence freshness 부족

## 도메인 판정
- `ARCH-MISSION` CPF 최종 목표/상용 솔루션 원칙: **재확인 필요** (source 0, test 0, SQL 0)
- `ARCH-MSA` MSA-first 및 Modular Monolith 호환: **재확인 필요** (source 80, test 20, SQL 0)
- `ARCH-BOUNDARY` 주제영역 경계/Bounded Context: **재확인 필요** (source 76, test 15, SQL 2)
- `ARCH-LAYER` 계층/패키지/의존성 규칙: **재확인 필요** (source 64, test 16, SQL 1)
- `FACADE-LOCAL` Local Facade 표준: **재확인 필요** (source 19, test 7, SQL 0)
- `FACADE-REMOTE` Remote Facade Proxy/Port-Adapter: **재확인 필요** (source 114, test 19, SQL 1)
- `PFW-CALL` CpfWebClient/CpfRestClient Service Call Engine: **재확인 필요** (source 42, test 8, SQL 0)
- `PFW-REGISTRY` Service/Endpoint/Instance Registry: **재확인 필요** (source 17, test 1, SQL 4)
- `PFW-ROUTING` LB mode/direct instance/discovery routing: **재확인 필요** (source 10, test 2, SQL 0)
- `PFW-HEALTH` Liveness/Readiness/Dependency Health: **재확인 필요** (source 10, test 2, SQL 0)
- `PFW-RESILIENCE` Timeout/Retry/Circuit/Bulkhead/Backpressure: **재확인 필요** (source 3, test 2, SQL 1)
- `PFW-DEADLINE` Request Deadline/Timeout Budget: **미구현** (source 0, test 0, SQL 0)
- `PFW-HEADER` 표준/확장 헤더: **재확인 필요** (source 22, test 8, SQL 1)
- `PFW-CONTEXT` TransactionContext/MDC/Thread Context: **재확인 필요** (source 5, test 1, SQL 0)
- `PFW-TXID` transactionGlobalId/segment/timeline: **재확인 필요** (source 18, test 2, SQL 3)
- `PFW-ROLE` transactionRole/direction/source-target: **재확인 필요** (source 17, test 6, SQL 0)
- `PFW-OPSDB` PFW 운영 DB 공유/장애모드: **재확인 필요** (source 26, test 9, SQL 4)
- `PFW-LOGDB` DB 로그/Segment 로그: **부분 구현** (source 51, test 14, SQL 0)
- `PFW-FILELOG` cpf-{moduleCode}-{logType}.log 파일 로그: **미검증** (source 52, test 15, SQL 0)
- `PFW-LOGFAIL` 로그 실패/fail-open/local spool: **부분 구현** (source 52, test 14, SQL 2)
- `PFW-TRACE` Trace Boost/동적 로그 레벨: **재확인 필요** (source 13, test 1, SQL 2)
- `PFW-MASK` 마스킹/민감정보 보호: **재확인 필요** (source 5, test 2, SQL 0)
- `PFW-ERROR` 오류/예외/응답 표준: **재확인 필요** (source 49, test 1, SQL 0)
- `PFW-VALID` Validation Framework: **재확인 필요** (source 11, test 5, SQL 0)
- `PFW-IDEMP` Idempotency 표준: **재확인 필요** (source 14, test 5, SQL 0)
- `PFW-STATE` 상태 전이 State Machine: **재확인 필요** (source 8, test 1, SQL 0)
- `PFW-LOCK` Optimistic/Distributed Lock: **재확인 필요** (source 6, test 1, SQL 0)
- `PFW-SCHED` Scheduler 표준: **부분 구현** (source 3, test 0, SQL 2)
- `CMN-CODE` 공통코드/참조데이터: **재확인 필요** (source 26, test 3, SQL 1)
- `CMN-MSG` 공통 메시지/다국어/오류 메시지: **재확인 필요** (source 34, test 1, SQL 0)
- `CMN-ID` 채번/분산 ID: **재확인 필요** (source 4, test 3, SQL 0)
- `CMN-FILE` 파일/다운로드/업로드/Object Storage: **재확인 필요** (source 54, test 14, SQL 1)
- `CMN-FIXED` 고정길이 전문 parser/formatter/layout: **재확인 필요** (source 24, test 2, SQL 1)
- `CMN-CALENDAR` 영업일/휴일/기관 캘린더: **부분 구현** (source 1, test 0, SQL 0)
- `CMN-TEMPLATE` 템플릿/알림: **부분 구현** (source 12, test 0, SQL 1)
- `ADM-AUTH` ADM 인증/세션/계정 생명주기: **재확인 필요** (source 23, test 6, SQL 2)
- `ADM-RBAC` RBAC/ABAC/권한 메타: **재확인 필요** (source 17, test 3, SQL 2)
- `ADM-AUDIT` 감사/보안 이벤트/부인방지: **재확인 필요** (source 7, test 1, SQL 0)
- `ADM-TX` 거래 그룹 목록/상세: **재확인 필요** (source 129, test 34, SQL 10)
- `ADM-TIMELINE` 통합 Timeline: **재확인 필요** (source 14, test 1, SQL 3)
- `ADM-SERVICE` Service Instance/Routing/Health 관제: **재확인 필요** (source 136, test 42, SQL 5)
- `ADM-LOG` 로그 정책/Trace Boost 화면: **재확인 필요** (source 43, test 6, SQL 2)
- `ADM-BATCH` Batch/Worker/Heartbeat/Ghost 관제: **부분 구현** (source 57, test 10, SQL 8)
- `ADM-CENTER` Center-Cut 관제: **재확인 필요** (source 25, test 11, SQL 3)
- `ADM-EXS` 대외연계 관제: **재확인 필요** (source 10, test 3, SQL 0)
- `ADM-COMP` Compensation/Manual Recovery 관제: **부분 구현** (source 6, test 0, SQL 2)
- `ADM-INCIDENT` Incident/Alert/Runbook: **미구현** (source 0, test 0, SQL 0)
- `ADM-UX` ADM UX/검색/다운로드/대시보드: **미구현** (source 7, test 1, SQL 1)
- `BAT-CORE` BAT standalone worker/application: **미구현** (source 14, test 1, SQL 2)
- `BAT-JOB` Job/Step/Parameter/Dependency: **재확인 필요** (source 25, test 8, SQL 1)
- `BAT-ITEM` Batch item claim/retry/skip/rerun: **재확인 필요** (source 6, test 4, SQL 1)
- `BAT-CALL-SYNC` BAT → 주제영역 동기 호출: **재확인 필요** (source 43, test 9, SQL 0)
- `BAT-CALL-ASYNC` BAT → Event/Outbox 비동기 호출: **재확인 필요** (source 11, test 1, SQL 0)
- `BAT-SHARED` BAT → SHARED/온라인 Facade 재사용: **재확인 필요** (source 13, test 4, SQL 0)
- `CENTER-CORE` Center-Cut 기본 구현: **재확인 필요** (source 25, test 11, SQL 3)
- `CENTER-ADV` Center-Cut 고급 패턴: **재확인 필요** (source 26, test 11, SQL 1)
- `EXS-INST` 기관/Endpoint/Profile: **부분 구현** (source 5, test 0, SQL 0)
- `EXS-REST` EXS REST 송수신: **재확인 필요** (source 12, test 5, SQL 0)
- `EXS-FIXED` EXS 고정길이 전문 송수신: **재확인 필요** (source 23, test 2, SQL 1)
- `EXS-SEC` EXS OAuth/JWT/mTLS/인증서: **부분 구현** (source 12, test 0, SQL 0)
- `EXS-UNKNOWN` Unknown Result 처리: **부분 구현** (source 35, test 2, SQL 0)
- `EXS-RECON` 대외 Reconciliation: **부분 구현** (source 5, test 3, SQL 0)
- `EXS-FILE` SFTP/File Transfer 연계 후보: **부분 구현** (source 30, test 6, SQL 0)
- `EVENT-CORE` Event/Message Envelope: **재확인 필요** (source 46, test 1, SQL 0)
- `EVENT-OUTBOX` Outbox/Inbox: **재확인 필요** (source 3, test 1, SQL 0)
- `EVENT-BROKER` Kafka/MQ/Redis real broker: **미검증** (source 30, test 4, SQL 1)
- `EVENT-DLQ` DLQ/Replay/Poison Message: **부분 구현** (source 4, test 0, SQL 1)
- `SAGA-CORE` Saga/분산 거래 표준: **부분 구현** (source 0, test 0, SQL 0)
- `SAGA-COMP` Compensation: **부분 구현** (source 0, test 0, SQL 0)
- `SAGA-MANUAL` Manual Recovery/Adjustment: **부분 구현** (source 6, test 0, SQL 2)
- `SEC-AUTHN` 인증/AuthN: **재확인 필요** (source 18, test 5, SQL 2)
- `SEC-AUTHZ` 권한/AuthZ/RBAC/ABAC: **재확인 필요** (source 18, test 3, SQL 2)
- `SEC-SECRET` Secret/Vault/Key Rotation: **부분 구현** (source 1, test 0, SQL 0)
- `SEC-CERT` Certificate/mTLS 관리: **부분 구현** (source 1, test 0, SQL 0)
- `SEC-PRIVACY` 개인정보/Privacy/Data Classification: **재확인 필요** (source 5, test 2, SQL 0)
- `SEC-DOWNLOAD` Download Governance: **재확인 필요** (source 7, test 1, SQL 1)
- `SEC-APP` Application Security 기본 통제: **재확인 필요** (source 23, test 3, SQL 1)
- `SEC-APPROVAL` Approval/Dual Control/Break-glass: **미구현** (source 0, test 0, SQL 0)
- `OPS-METRIC` Metrics/Observability: **재확인 필요** (source 3, test 2, SQL 0)
- `OPS-SLO` SLI/SLO/Error Budget: **미구현** (source 4, test 2, SQL 0)
- `OPS-ALERT` Alert Rule Engine: **미구현** (source 0, test 0, SQL 0)
- `OPS-INCIDENT` Incident Management: **미구현** (source 0, test 0, SQL 0)
- `OPS-RUNBOOK` Runbook/자동 진단: **미구현** (source 0, test 0, SQL 0)
- `OPS-SELF` Self-healing: **미구현** (source 6, test 0, SQL 2)
- `OPS-TOPOLOGY` Topology/Dependency Map/Service Catalog: **미구현** (source 0, test 0, SQL 0)
- `OPS-MAINT` Maintenance Mode/Drain: **미구현** (source 0, test 0, SQL 0)
- `OPS-CONFIG` Config/Policy/Runtime Override: **재확인 필요** (source 82, test 14, SQL 4)
- `OPS-DRIFT` Config Drift/Policy Versioning: **부분 구현** (source 1, test 0, SQL 0)
- `OPS-CAPACITY` Performance/Capacity/Resource Governance: **재확인 필요** (source 0, test 2, SQL 0)
- `OPS-DR` DR/Backup/Restore/Archive: **부분 구현** (source 11, test 5, SQL 1)
- `REL-BUILD` Build/Artifact/Provenance: **재확인 필요** (source 9, test 4, SQL 0)
- `REL-DEPLOY` Deployment/Rollback/Feature Flag: **부분 구현** (source 1, test 1, SQL 0)
- `REL-MIG` DB Migration/Flyway/Expand-Contract: **부분 구현** (source 1, test 3, SQL 35)
- `REL-COMPAT` API/Event/전문 호환성: **부분 구현** (source 1, test 0, SQL 0)
- `DB-SQL` SQL 표준/MyBatis/Index: **재확인 필요** (source 15, test 4, SQL 51)
- `DB-INSTALL` MariaDB 신규 빈 DB Full Install: **재확인 필요** (source 7, test 2, SQL 4)
- `DB-PERF` DB 성능/Partition/Retention: **미구현** (source 0, test 0, SQL 0)
- `DB-MULTI` Multi Datasource/Read Replica: **부분 구현** (source 7, test 2, SQL 0)
- `DATA-LINEAGE` Data Lineage/Quality/Reconciliation: **재확인 필요** (source 5, test 3, SQL 1)
- `DATA-RETENTION` Retention/Purge/Legal Hold/Archive: **재확인 필요** (source 11, test 5, SQL 1)
- `API-CONTRACT` API Contract/OpenAPI/Developer Portal: **재확인 필요** (source 147, test 11, SQL 0)
- `API-GATEWAY` Gateway/L4/WAF/Service Mesh 연계: **재확인 필요** (source 9, test 4, SQL 0)
- `API-LIMIT` Rate Limit/Quota/Abuse Detection: **미구현** (source 0, test 0, SQL 0)
- `API-PAGING` Pagination/Search/Sort/Download Guard: **재확인 필요** (source 12, test 2, SQL 1)
- `SAMPLE-ACC` ACC 실전형 샘플: **재확인 필요** (source 26, test 6, SQL 0)
- `SAMPLE-MBR` MBR 실전형 샘플: **재확인 필요** (source 20, test 9, SQL 0)
- `SAMPLE-BIZADM` BIZADM 업무관리 샘플: **재확인 필요** (source 19, test 5, SQL 0)
- `SAMPLE-EDU` EDU 교육 샘플: **재확인 필요** (source 101, test 61, SQL 4)
- `SAMPLE-XYZ` XYZ 신규 주제영역 샘플: **재확인 필요** (source 77, test 36, SQL 0)
- `ONBOARD-DOMAIN` 신규 주제영역 온보딩: **실패** (source 1, test 1, SQL 0)
- `DEVEX-QUICK` Developer Quickstart/Local Dev: **재확인 필요** (source 9, test 4, SQL 0)
- `DEVEX-CODEGEN` Scaffold/Code Generation Governance: **실패** (source 1, test 1, SQL 0)
- `DEVEX-COMMENT` 한글 주석/설정 주석 표준: **미구현** (source 0, test 0, SQL 0)
- `RULE-ARCH` Architecture Rule Check: **재확인 필요** (source 0, test 0, SQL 0)
- `RULE-SEC` Security/Secret/URL Scan: **재확인 필요** (source 26, test 3, SQL 1)
- `RULE-QUALITY` Static Analysis/Dependency/License: **재확인 필요** (source 0, test 0, SQL 1)
- `TEST-UNIT` Unit/Slice/Integration Test: **재확인 필요** (source 1, test 158, SQL 1)
- `TEST-CONTRACT` Contract/Compatibility Test: **재확인 필요** (source 2, test 158, SQL 1)
- `TEST-RUNTIME` Runtime Smoke: **재확인 필요** (source 30, test 6, SQL 4)
- `TEST-BROWSER` Browser Click E2E: **미검증** (source 2, test 0, SQL 0)
- `TEST-BROKER` Real Broker/Multi-instance Test: **미검증** (source 30, test 4, SQL 1)
- `TEST-FAULT` Fault Injection/Chaos Smoke: **미검증** (source 7, test 1, SQL 0)
- `TEST-EVIDENCE` Evidence Index/Consistency Gate: **재확인 필요** (source 0, test 1, SQL 0)
- `DOC-GOV` 문서 최소화/정본화 후순위: **부분 구현** (source 1, test 0, SQL 51)
- `DOC-PRODUCT` 제품화 문서/설치/운영/개발자 문서: **부분 구현** (source 19, test 7, SQL 7)
- `PROD-EDITION` 상용화/Edition/License 후보: **미구현** (source 0, test 0, SQL 0)
- `PROD-MULTITENANT` Multi-tenant/Multi-customer 후보: **미구현** (source 0, test 0, SQL 0)
- `PROD-PLUGIN` Plugin/Adapter/Marketplace 후보: **미구현** (source 13, test 10, SQL 0)
- `PROD-PACKAGE` 산업군별 패키지 후보: **미구현** (source 2, test 0, SQL 0)
- `REQ-GOV` 요건 ID/우선순위/추적성: **부분 구현** (source 0, test 0, SQL 0)
- `REQ-REVIEW` ChatGPT 검수/완료 불인정 기준: **부분 구현** (source 1, test 0, SQL 0)
- `REQ-CODEX` Codex 요청서 작성 기준: **부분 구현** (source 0, test 0, SQL 0)
- `REQ-GAP` Future Requirement Gap Intake: **부분 구현** (source 0, test 0, SQL 0)
