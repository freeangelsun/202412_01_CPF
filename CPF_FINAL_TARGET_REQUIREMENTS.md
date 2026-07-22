# CPF_FINAL_TARGET_REQUIREMENTS

## 1. 문서 목적

이 문서는 Core Platform Framework(CPF)의 최상위 제품 목표·아키텍처 원칙·Module Ownership·완료 판정·Requirement Catalog를 정의하는 정본이다.

이 문서는 자동 생성 inventory, 파일별 traceability, 날짜별 Evidence, 작업 일지, 반복 상세 카드의 저장소가 아니다. 그러한 자료는 `specs/generated`와 공식 Evidence 위치에서 재생성·관리한다.

## 2. 제품 정의

CPF는 금융권을 포함한 다양한 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화할 수 있는 Business Platform 품질의 Framework다.

필수 제품 특성:

- MSA와 modular monolith
- 동일 JVM과 분리 WAS 호출
- 다중 인스턴스·장애 대응·재시도·복구·분산 추적
- 금융권 수준 보안·권한·감사·마스킹
- 멱등성·비동기·재처리·보상·결과 불명 복구
- 외부 시스템·전문·파일·첨부·압축·메시징
- Batch·Scheduler·Agent·Worker·Center-Cut
- 운영 조회·제어·승인·감사
- 신규 주제영역 Generator
- EDU·OpenAPI·JavaDoc·테스트·샘플
- 설치·migration·upgrade·rollback·배포·DR
- Source·SQL·API·Test·Document·Evidence 정합성

## 3. 공식 명칭과 구조

- 정식 명칭: **Core Platform Framework**
- Business Platform: 제품 품질과 지향 수준
- 공식 Module: `cpf-` 접두사
- Java Package: `com.cpf.<domain>`
- SystemCode: 내부 식별용 3자리 대문자
- DomainName과 SystemCode는 서로 다른 개념이다.

| 제품 역할 | 목표 Module | Java Root Package | SystemCode | 비고 |
|---|---|---|---|---|
| 기술 공통 Framework | `cpf-core` | `com.cpf.core` | `CPF` | 시스템 코드·DB 객체·기술 capability의 정본 접두어 |
| Gateway Runtime | `cpf-gateway` | `com.cpf.gateway` | `GWY` | 외부 진입과 정책 집행 |
| 고객 업무 공통 | `cpf-common` | `com.cpf.common` | `CMN` | 기술 공통과 분리 |
| 플랫폼 관리자 | `cpf-admin` | `com.cpf.admin` | `ADM` | 플랫폼 운영·보안·감사 |
| 고객 업무 관리자 | `cpf-biz-admin` | `com.cpf.bizadmin` | `BZA` | 고객 업무 관리 화면/API |
| Batch·Worker·Scheduler·Center-Cut | `cpf-batch` | `com.cpf.batch` | `BAT` | 독립 프로세스 포함 |
| 회원 업무 | `cpf-member` | `com.cpf.member` | `MBR` | 공식 업무 주제영역 |
| 계좌 업무 | `cpf-account` | `com.cpf.account` | `ACC` | Generator lifecycle 검증 기준 |
| 참조 주제영역 | `cpf-reference` | `com.cpf.reference` | `REF` | 교육·참조 구현의 공식 소유 모듈 |
| 대외연계 | `cpf-external` | `com.cpf.external` | `EXS` | 기관·전문·파일·복구 |


## 4. 아키텍처 원칙

### 4.1 Ownership

- 기술 공통: `cpf-core`
- 고객 업무 공통: `cpf-common`
- 플랫폼 운영·보안·감사: `cpf-admin`
- 고객 업무 관리자: `cpf-biz-admin`
- Batch·Worker·Scheduler·Center-Cut: `cpf-batch`
- 업무 기능: 각 업무 Module
- 대외연계: `cpf-external`

### 4.2 의존성

- 업무 Module은 공개 API/SPI를 통해 기술 공통을 사용한다.
- 기술 공통은 업무 Module을 참조하지 않는다.
- 관리자 Module은 업무 구현을 직접 소유하지 않는다.
- 동일 JVM 호출과 Remote 호출은 동일 업무 Contract를 공유한다.
- 실제 Consumer 없는 추상화와 순환·역방향 의존을 금지한다.

### 4.3 API 경계

- Public API: 고객 개발자가 안정적으로 사용하는 계약
- SPI: 고객·업무 Module이 확장하는 계약
- Internal: 제품 내부 구현, 호환성 보장 대상 아님
- 외부 Client는 Gateway를 경유한다.
- 내부 주제영역 호출은 Gateway를 재경유하지 않는다.

### 4.4 상태·실패·복구

모든 상태 기반 기능은 상태 전이, 멱등성, 동시성, timeout, retry, 결과 불명, 재처리, 보상, 운영 조치를 함께 설계한다.

### 4.5 다중 인스턴스

DB·Broker·Scheduler·Worker·Cache·Lock·Log는 다중 인스턴스에서 중복·경합·부분 실패에 안전해야 한다. Lease는 fencing token과 takeover 기준을 가져야 한다.

## 5. 보안·감사 원칙

- 최소 권한
- AuthN/AuthZ/RBAC/ABAC
- 관리자 dual control
- Secret 외부화·rotation
- mTLS·인증서 생명주기
- 개인정보 분류·마스킹
- 보안·감사 이벤트
- 다운로드 승인·워터마크·만료
- 민감정보 없는 Evidence
- 공급망·Dependency·SBOM·License 검증

## 6. 데이터·SQL·호환성 원칙

- SQL Owner는 하나다.
- 신규 설치 SQL과 migration은 일치한다.
- MariaDB 우선 실검증, PostgreSQL·Oracle·SQL Server 교체 설계
- schema version·upgrade·rollback·backup·restore
- API·DB·Config·Message·File format 호환성 정책
- breaking change는 migration과 rollback을 제공한다.

## 7. Generator와 확장 모델

Generator는 DomainName·SystemCode·Module·Package·Capability·DB Vendor를 입력받아 표준 구조를 생성한다.

필수 lifecycle:
- create
- db-init
- verify
- delete

필수 특성:
- 충돌 검증
- 선택 capability만 생성
- 반복 실행 동일성
- 안전 삭제
- 사용자 확장 파일 보호
- Local/Remote parity
- build·test·package 가능
- 공식 Module·Package·SystemCode 정합성

## 8. AI 정책

AI Provider·Model·Prompt·Embedding·Vector Store·추론 SDK 등 AI 전용 기능은 `cpf-core`와 기본 제품에 포함하지 않는다.

AI는 향후 별도 선택 확장 또는 고객 Module로 제공할 수 있다. AI PoC에 포함된 범용 기능은 적정 Owner로 분리하고 AI 전용 미사용 코드는 제거한다.

## 9. Repository와 문서 정본

Root에는 제품 식별·Build·실행에 필요한 최소 파일과 공식 Module만 둔다.

문서 역할:
- 이 문서: 최상위 목표·정책·Requirement Catalog
- Architecture/Specification: 상세 구조와 계약
- Guide: 개발·운영·설치
- Work/Review: 요청·검수·진행
- Evidence: 실행 근거
- Generated: inventory·traceability
- Release: CHANGELOG·호환성·migration

README는 제품 소개와 사용 안내만 기록한다.

## 10. 완료 판정

허용 상태:

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

각 요구사항은 적용 가능한 범위에서 다음이 함께 확인되어야 한다.

- 실제 Source와 계층 연결, 실제 Consumer
- Module·Package Ownership과 의존성 방향
- API Contract, 오류·Validation·권한 표준
- SQL·Migration·신규 설치·Upgrade·Rollback
- 정상·오류·경계·부분 실패
- 멱등성·동시성·재시도·결과 불명·복구
- 다중 인스턴스·Lease·Fencing·Takeover
- 보안·감사·마스킹·민감정보 제거
- 운영 조회·제어·승인·통계·알림
- EDU·OpenAPI·JavaDoc
- Unit·Integration·Runtime·Browser 검증
- 기준 Commit과 실행 환경이 명시된 최신 Evidence
- 기존 성공 기능 회귀 검증
- Dead Code·Stale Evidence·중간 산출물 제거

적용하지 않는 항목은 `해당 없음`으로 숨기지 말고, 비적용 이유와 대체 검증을 기록한다.


파일·Class 존재, Swagger 노출, 정적 검색, 일부 테스트, 과거 Evidence, 작업자 보고는 완료 근거가 아니다.

## 11. Requirement Catalog


### 아키텍처·호출·공통 실행 기반

- `ARCH-MISSION` — CPF 최종 목표/상용 솔루션 원칙
- `ARCH-MSA` — MSA-first 및 Modular Monolith 호환
- `ARCH-BOUNDARY` — 주제영역 경계/Bounded Context
- `ARCH-LAYER` — 계층/패키지/의존성 규칙
- `FACADE-LOCAL` — Local Facade 표준
- `FACADE-REMOTE` — Remote Facade Proxy/Port-Adapter
- `CPF-CALL` — CpfWebClient/CpfRestClient Service Call Engine
- `CPF-REGISTRY` — Service/Endpoint/Instance Registry
- `CPF-ROUTING` — LB mode/direct instance/discovery routing
- `CPF-HEALTH` — Liveness/Readiness/Dependency Health
- `CPF-RESILIENCE` — Timeout/Retry/Circuit/Bulkhead/Backpressure
- `CPF-DEADLINE` — Request Deadline/Timeout Budget
- `CPF-HEADER` — 표준/확장 헤더
- `CPF-CONTEXT` — TransactionContext/MDC/Thread Context
- `CPF-TXID` — transactionGlobalId/segment/timeline
- `CPF-ROLE` — transactionRole/direction/source-target
- `CPF-OPSDB` — CPF 운영 DB 공유/장애모드
- `CPF-LOGDB` — DB 로그/Segment 로그
- `CPF-FILELOG` — cpf-{moduleCode}-{logType}.log 파일 로그
- `CPF-LOGFAIL` — 로그 실패/fail-open/local spool
- `CPF-TRACE` — Trace Boost/동적 로그 레벨
- `CPF-MASK` — 마스킹/민감정보 보호
- `CPF-ERROR` — 오류/예외/응답 표준
- `CPF-VALID` — Validation Framework
- `CPF-IDEMP` — Idempotency 표준
- `CPF-STATE` — 상태 전이 State Machine
- `CPF-LOCK` — Optimistic/Distributed Lock
- `CPF-SCHED` — Scheduler 표준

### 업무 공통·파일·전문

- `CMN-CODE` — 공통코드/참조데이터
- `CMN-MSG` — 공통 메시지/다국어/오류 메시지
- `CMN-ID` — 채번/분산 ID
- `CMN-FILE` — 파일/다운로드/업로드/Object Storage
- `CMN-FIXED` — 고정길이 전문 parser/formatter/layout
- `CMN-CALENDAR` — 영업일/휴일/기관 캘린더
- `CMN-TEMPLATE` — 템플릿/알림

### 운영·관리·보안

- `ADM-AUTH` — ADM 인증/세션/계정 생명주기
- `ADM-RBAC` — RBAC/ABAC/권한 메타
- `ADM-AUDIT` — 감사/보안 이벤트/부인방지
- `ADM-TX` — 거래 그룹 목록/상세
- `ADM-TIMELINE` — 통합 Timeline
- `ADM-SERVICE` — Service Instance/Routing/Health 관제
- `ADM-LOG` — 로그 정책/Trace Boost 화면
- `ADM-BATCH` — Batch/Worker/Heartbeat/Ghost 관제
- `ADM-CENTER` — Center-Cut 관제
- `ADM-EXS` — 대외연계 관제
- `ADM-COMP` — Compensation/Manual Recovery 관제
- `ADM-INCIDENT` — Incident/Alert/Runbook
- `ADM-UX` — ADM UX/검색/다운로드/대시보드
- `SEC-AUTHN` — 인증/AuthN
- `SEC-AUTHZ` — 권한/AuthZ/RBAC/ABAC
- `SEC-SECRET` — Secret/Vault/Key Rotation
- `SEC-CERT` — Certificate/mTLS 관리
- `SEC-PRIVACY` — 개인정보/Privacy/Data Classification
- `SEC-DOWNLOAD` — Download Governance
- `SEC-APP` — Application Security 기본 통제
- `SEC-APPROVAL` — Approval/Dual Control/Break-glass
- `OPS-METRIC` — Metrics/Observability
- `OPS-SLO` — SLI/SLO/Error Budget
- `OPS-ALERT` — Alert Rule Engine
- `OPS-INCIDENT` — Incident Management
- `OPS-RUNBOOK` — Runbook/자동 진단
- `OPS-SELF` — Self-healing
- `OPS-TOPOLOGY` — Topology/Dependency Map/Service Catalog
- `OPS-MAINT` — Maintenance Mode/Drain
- `OPS-CONFIG` — Config/Policy/Runtime Override
- `OPS-DRIFT` — Config Drift/Policy Versioning
- `OPS-CAPACITY` — Performance/Capacity/Resource Governance
- `OPS-DR` — DR/Backup/Restore/Archive

### Batch·Worker·Center-Cut

- `BAT-CORE` — BAT standalone worker/application
- `BAT-JOB` — Job/Step/Parameter/Dependency
- `BAT-ITEM` — Batch item claim/retry/skip/rerun
- `BAT-CALL-SYNC` — BAT → 주제영역 동기 호출
- `BAT-CALL-ASYNC` — BAT → Event/Outbox 비동기 호출
- `BAT-SHARED` — BAT → SHARED/온라인 Facade 재사용
- `CENTER-CORE` — Center-Cut 기본 구현
- `CENTER-ADV` — Center-Cut 고급 패턴

### 대외연계·Event·복구

- `EXS-INST` — 기관/Endpoint/Profile
- `EXS-REST` — EXS REST 송수신
- `EXS-FIXED` — EXS 고정길이 전문 송수신
- `EXS-SEC` — EXS OAuth/JWT/mTLS/인증서
- `EXS-UNKNOWN` — Unknown Result 처리
- `EXS-RECON` — 대외 Reconciliation
- `EXS-FILE` — SFTP/File Transfer 연계 후보
- `EVENT-CORE` — Event/Message Envelope
- `EVENT-OUTBOX` — Outbox/Inbox
- `EVENT-BROKER` — Kafka/MQ/Redis real broker
- `EVENT-DLQ` — DLQ/Replay/Poison Message
- `SAGA-CORE` — Saga/분산 거래 표준
- `SAGA-COMP` — Compensation
- `SAGA-MANUAL` — Manual Recovery/Adjustment

### 업무 주제영역·Generator·EDU


### 설치·배포·품질·제품화

- `REL-BUILD` — Build/Artifact/Provenance
- `REL-DEPLOY` — Deployment/Rollback/Feature Flag
- `REL-MIG` — DB Migration/Flyway/Expand-Contract
- `REL-COMPAT` — API/Event/전문 호환성
- `DB-SQL` — SQL 표준/MyBatis/Index
- `DB-INSTALL` — MariaDB 신규 빈 DB Full Install
- `DB-PERF` — DB 성능/Partition/Retention
- `DB-MULTI` — Multi Datasource/Read Replica
- `DATA-LINEAGE` — Data Lineage/Quality/Reconciliation
- `DATA-RETENTION` — Retention/Purge/Legal Hold/Archive
- `API-CONTRACT` — API Contract/OpenAPI/Developer Portal
- `API-GATEWAY` — Gateway/L4/WAF/Service Mesh 연계
- `API-LIMIT` — Rate Limit/Quota/Abuse Detection
- `API-PAGING` — Pagination/Search/Sort/Download Guard
- `SAMPLE-ACC` — ACC 실전형 샘플
- `SAMPLE-MBR` — MBR 실전형 샘플
- `SAMPLE-BIZADM` — BIZADM 업무관리 샘플
- `SAMPLE-EDU` — EDU 교육 샘플
- `SAMPLE-REF` — REF 신규 주제영역 샘플
- `ONBOARD-DOMAIN` — 신규 주제영역 온보딩
- `DEVEX-QUICK` — Developer Quickstart/Local Dev
- `DEVEX-CODEGEN` — Scaffold/Code Generation Governance
- `DEVEX-COMMENT` — 한글 주석/설정 주석 표준
- `RULE-ARCH` — Architecture Rule Check
- `RULE-SEC` — Security/Secret/URL Scan
- `RULE-QUALITY` — Static Analysis/Dependency/License
- `TEST-UNIT` — Unit/Slice/Integration Test
- `TEST-CONTRACT` — Contract/Compatibility Test
- `TEST-RUNTIME` — Runtime Smoke
- `TEST-BROWSER` — Browser Click E2E
- `TEST-BROKER` — Real Broker/Multi-instance Test
- `TEST-FAULT` — Fault Injection/Chaos Smoke
- `TEST-EVIDENCE` — Evidence Index/Consistency Gate
- `DOC-GOV` — 문서 최소화/정본화 후순위
- `DOC-PRODUCT` — 제품화 문서/설치/운영/개발자 문서
- `PROD-EDITION` — 상용화/Edition/License 후보
- `PROD-MULTITENANT` — Multi-tenant/Multi-customer 후보
- `PROD-PLUGIN` — Plugin/Adapter/Marketplace 후보
- `PROD-PACKAGE` — 산업군별 패키지 후보
- `REQ-GOV` — 요건 ID/우선순위/추적성
- `REQ-REVIEW` — ChatGPT 검수/완료 불인정 기준
- `REQ-CODEX` — Codex 요청서 작성 기준
- `REQ-GAP` — Future Requirement Gap Intake

## 12. 상세 명세 분리 원칙

각 Requirement의 상세 API·DB·UI·상태·테스트 명세는 역할별 Specification에 둔다. 공통 완료 조건을 Requirement마다 반복하지 않는다.

자동 생성 자료:
- requirement inventory
- implementation inventory
- traceability
- architecture inventory
- file hash manifest

위 자료는 정본이 아닌 파생 산출물이며, 생성 기준 Commit과 도구 버전을 포함해야 한다.

## 13. 최종 제품화 Gate

GA 판정 전 필수:

- 공식 구조 migration 완료
- 전체 clean build/test
- 주요 DB 설치·migration·rollback
- 다중 인스턴스·Worker·복구
- 실제 Broker·대외 실패·결과 불명
- 보안·감사·마스킹
- ADM/BZA Runtime·Browser
- EDU·OpenAPI·JavaDoc
- 설치·운영·장애 복구 문서
- SBOM·License·Secret scan
- 공개 package 독립 build
- Source·SQL·API·Test·Document·Evidence 일치
