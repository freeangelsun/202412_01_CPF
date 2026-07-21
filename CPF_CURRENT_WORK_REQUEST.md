# CPF_CURRENT_WORK_REQUEST

## 1. 요청 목적

이 문서는 CPF를 **Core Platform Framework**라는 정식 명칭의 상용 Business Platform 품질 Framework로 완성하기 위한 단일 통합 Codex 작업 요청서다.

이번 작업은 기능 몇 개를 나누어 수행하는 요청이 아니다. 중간 Push에 포함된 실제 구현·미완료·잘못된 완료 처리·회귀를 전수 검수하고, 공식 Module·Package·SystemCode·Generator 구조를 먼저 정리한 뒤, 이전 요청의 잔여 Gap과 상용 제품 필수 요건을 한 번에 구현·검증·정리하는 전체 작업이다.

## 2. 기준선과 중간 Push 처리

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 직전 정상 기준 후보: `b32cc8ae5ccb798f3b1e6720c678e3838d01b460`
- Qwen/Cline 중간 Push: `e35b7a0b4f2f7c94fb42f6767dc09c73ca2a3549`
- 중간 Commit 메시지: `CodeCox: complete 133 domain implementation, modernize frontends, add worker runtime, evidence, and smoke tests`
- 중간 Commit은 작업자 주장과 생성 Evidence를 포함한 **미검수 중간 결과**이며, 완료 기준선이 아니다.
- 작업 재개 시 최신 `origin/master`를 확인하고 위 SHA와 다르면 차이를 먼저 보고한다.


Codex는 중간 Commit의 리포트·Matrix·Evidence·Commit 메시지를 사실로 간주하지 않는다. `b32cc8a..e35b7a0` 전체 변경을 Source·Test·SQL·Config·Script·Frontend·Runtime Evidence 단위로 대조한다.

### 필수 판정

- 실제 구현과 검증이 일치하면 보존·보완한다.
- Source 없이 문서만 완료 처리했으면 상태를 하향한다.
- 실행하지 않은 테스트의 성공 표기는 제거한다.
- 생성 inventory·traceability 파일의 크기나 항목 수를 기능 완료 근거로 사용하지 않는다.
- 유효한 구현은 버리지 말고 목표 구조로 이관한다.
- 기존 성공 기능의 삭제·회귀가 발견되면 우선 복구한다.

## 3. 작업자와 검수자 역할

Codex는 구현·테스트·실행 Evidence·사실 기반 작업 결과를 작성한다. 최종 완료 판정은 별도 검수에서 확정한다.

Codex가 상태 문서를 갱신할 때에는:
- 직접 구현·직접 실행·직접 확인한 근거만 기록한다.
- `완료`, `부분 구현`, `미구현`, `미검증`, `실패`, `재확인 필요`만 사용한다.
- 최종 진행률을 임의 계산하지 않는다.
- 작업자 주장과 검수 확정을 명확히 구분한다.

## 4. 최우선 구조 전환

뒤 작업의 재수정을 막기 위해 다음을 가장 먼저 수행한다.

1. 공식 Module·Package·SystemCode Mapping 확정
2. Root project name을 `cpf-core-platform-framework`로 정리
3. Generator template·입력·충돌 검증을 목표 구조에 맞춤
4. 기존 Module·Package를 목표 구조로 migration
5. Build·Config·API·SQL·Migration·Route·Queue·Topic·Log·UI·Script·EDU·Evidence 경로 보정
6. 전체 Consumer·의존성·실행 경로 복구
7. 그 후 기능 Gap 구현과 제품화 검증 수행

| 제품 역할 | 목표 Module | Java Root Package | SystemCode | 비고 |
|---|---|---|---|---|
| 기술 공통 Framework | `cpf-core` | `com.cpf.core` | `PFW` 유지 권장 | 외부 호환 식별자는 우선 유지 |
| Gateway Runtime | `cpf-gateway` | `com.cpf.gateway` | `GWY` | 외부 진입과 정책 집행 |
| 고객 업무 공통 | `cpf-common` | `com.cpf.common` | `CMN` | 기술 공통과 분리 |
| 플랫폼 관리자 | `cpf-admin` | `com.cpf.admin` | `ADM` | 플랫폼 운영·보안·감사 |
| 고객 업무 관리자 | `cpf-biz-admin` | `com.cpf.bizadmin` | `BZA` | 고객 업무 관리 화면/API |
| Batch·Worker·Scheduler·Center-Cut | `cpf-batch` | `com.cpf.batch` | `BAT` | 독립 프로세스 포함 |
| 회원 업무 | `cpf-member` | `com.cpf.member` | `MBR` | 공식 업무 주제영역 |
| 계좌 업무 | `cpf-account` | `com.cpf.account` | `ACC` | Generator lifecycle 검증 기준 |
| 참조 주제영역 | `cpf-reference` | `com.cpf.reference` | `REF` | 기존 XYZ 대체 |
| 대외연계 | `cpf-external` | `com.cpf.external` | `EXS` | 기관·전문·파일·복구 |


### SystemCode 정책

- Java Package와 Module명에는 읽을 수 있는 DomainName을 사용한다.
- 3자리 대문자 SystemCode는 내부 식별자다.
- `cpf-core`의 기존 외부 식별자 `PFW`는 호환성 위험을 줄이기 위해 우선 유지한다.
- 변경이 더 적합하다고 판단하면 영향 분석·migration·rollback·호환 adapter를 제시하고 변경한다.
- `XYZ`는 공식 제품 Module이 아니며 `cpf-reference`/`REF`로 교체한다.
- Generator는 DomainName과 SystemCode를 분리 입력하고 예약 코드·중복 등록·Module·Package·Config·Route·SQL 충돌을 검사한다.

## 5. AI 전용 기능 제거

`cpf-core`와 공식 기본 제품에는 다음 AI 전용 기능을 두지 않는다.

- AI Provider·Model·Prompt·Embedding·Vector Store
- 추론 SDK·LLM Client·AI 전용 configuration
- 사용되지 않는 AI PoC·Sample·Dependency·문서·설정

실제 Repository를 검색하여 AI 전용 미사용·PoC 코드는 제거한다. AI 코드에 섞인 범용 HTTP·Retry·File·Security 기능은 적정 Owner로 분리한다. 향후 AI는 별도 선택 확장 또는 고객 Module로만 제공하며 기본 Module 간 의존성을 만들지 않는다.

## 6. Codex 중단 지점 인수인계

1. 133개 요구 도메인과 약 13,300개 상세 요구의 inventory·traceability 생성기 작업
2. 전체 Java 타입 계층·역할 분류 및 quality gate 연결
3. Generator capability 분리와 ACC 삭제→재생성 lifecycle 검증
4. 다중 BAT Worker 등록·heartbeat·claim·lease·drain·takeover 구현 및 실런타임 검증
5. ADM/BZA Vue 3 SFC·TypeScript·Vite 전환과 Gradle 패키징
6. 중복 module-local SQL 삭제 및 canonical SQL gate
7. Generator의 MariaDB/PostgreSQL/Oracle/SQL Server 선택 지원
8. 최신 빌드·MariaDB 설치·OpenAPI·다중 서비스 Runtime 검증
9. 파일 로그 검증 하네스를 최신 표준 경로와 응답 계약으로 수정하던 중 중단

위 항목은 로그상 작업·성공 주장이 존재하지만, 현재 Commit의 Source·실행 로그·DB 결과·프로세스 결과를 재검증하기 전에는 완료로 인정하지 않는다.


## 7. 전체 작업 범위

### 7.1 Repository·Root·정본 구조

- Root에는 제품 식별·Build·실행에 필요한 최소 파일과 공식 Module만 둔다.
- 작업 요청서·검수·Gap·Evidence·진행 문서는 공식 문서 위치로 통합한다.
- 단순 이동이 아니라 중복 정본화, 링크·Script·CI·Gradle 경로 보정, `.gitignore` 보강까지 수행한다.
- README에는 완성 제품 소개·구조·주요 기능·사용 안내만 남기고 작업 일지·Commit·진척률을 제거한다.
- 최종 목표 문서는 규범·아키텍처·Requirement Catalog 중심으로 축소한다.
- 대형 inventory·traceability는 재생성 가능한 파생 산출물로 분리하고 Root 정본에서 제외한다.

권장 위치:
```text
cpf-docs/
  architecture/
  specifications/
  development/
  operations/
  releases/
  work/
  evidence/
specs/generated/
cpf-deployment/
cpf-tools/
```

### 7.2 Module·Package Migration

- legacy `pfw`, `cmn`, `adm`, `bza`, `bat`, `mbr`, `acc`, `xyz`, `pfw-gateway-runtime`를 목표 공식 Module로 전환한다.
- Java root `cpf.*`를 `com.cpf.*`로 전환한다.
- 단순 문자열 치환을 금지한다.
- Spring scanning, configuration properties, reflection, serialization type, MyBatis mapper, Flyway, OpenAPI, test fixture, frontend API route, shell script를 모두 검사한다.
- 이전 좌표를 사용하는 외부 Consumer가 있으면 호환 계획을 제공한다.
- 역방향·순환 의존을 제거한다.

### 7.3 Generator 완결

Generator lifecycle은 `create`, `db-init`, `verify`, `delete`를 제공한다.

필수 capability:
- DB 사용 여부와 DB Vendor
- Batch
- External Integration
- UI
- Messaging
- File
- Security/Audit 기본 구성
- Local/Remote Facade

필수 검증:
- DomainName/SystemCode 분리
- 예약 코드와 기존 등록
- Module·Package·Config·Route·SQL 충돌
- 선택하지 않은 capability 잔존 0
- MariaDB/PostgreSQL/Oracle/SQL Server 산출물
- 반복 생성 동일성
- 삭제 안전성, 사용자 파일 보호
- ACC 실제 삭제→생성→build→삭제→재생성 parity
- 신규 주제영역이 MSA와 동일 JVM 모두에서 동작

### 7.4 기술 공통과 호출 구조

- Local Facade와 Remote Port/Adapter의 동일 업무 Contract
- Gateway 외부 진입과 내부 주제영역 호출 분리
- Timeout budget, retry, circuit breaker, bulkhead, backpressure
- Registry·health·routing·direct instance·load balance
- 표준/확장 Header와 trust boundary
- transactionGlobalId·segmentId·role·direction·timeline
- Thread/async context propagation
- 표준 오류·validation·idempotency·state machine·lock
- 실제 Consumer 없는 추상화 제거

### 7.5 Logging·Tracing·Audit

- 파일 로그와 DB 로그가 동일 거래 식별자·상태·시간·오류 정보를 제공
- 표준 경로, 파일명, rotation, retention, size guard
- 로그 저장 실패의 fail-open/fail-close 정책
- local spool과 복구
- Trace Boost와 동적 로그 레벨
- 마스킹·Secret 제거
- ADM 거래 그룹/상세/timeline 조회
- 기존 폐기 경로를 참조하는 smoke script 교정
- 실제 파일 내용과 DB record parity 검증

### 7.6 Batch·Worker·Scheduler·Center-Cut

- `cpf-batch` 독립 실행
- Worker registration·heartbeat·capability·version
- claim·lease·renew·fencing·drain·stop·ghost·takeover
- 동시성 제한과 중복 실행 0
- Job/Step/parameter/dependency
- item retry·skip·rerun·checkpoint·partition
- 동기/비동기 주제영역 호출
- Center-Cut 기본 item/result 저장소는 `cpf-batch` 소유
- 업무별 custom Provider/Repository/Handler adapter
- PFW는 실행 계약·오류·로그·감사 표준만 소유
- 2개 실제 Worker 프로세스와 DB 기반 장애 takeover 검증

### 7.7 대외연계·전문·파일·메시징·복구

- REST·고정길이 전문·파일/SFTP
- 기관·endpoint·profile·credential reference
- OAuth/JWT/mTLS·인증서 rotation
- timeout/retry/idempotency
- Unknown Result의 requery·reconciliation·manual recovery
- Outbox/Inbox·real broker·DLQ·replay·poison message
- Saga·compensation
- 실패 주입과 부분 실패 복구 Evidence

### 7.8 보안·권한·감사·개인정보

- AuthN·AuthZ·RBAC·ABAC
- 관리자 계정·세션·승인·dual control
- 감사·보안 이벤트·부인 방지
- Secret 외부화·rotation
- 개인정보 분류·마스킹·다운로드 통제
- 운영 조치의 권한·승인·감사
- prod guardrail
- dependency/SBOM/license/secret scan

### 7.9 ADM·BZA Frontend

- Vue 3 SFC·TypeScript·Vite 정본
- vendored Vue·수작업 bundle 제거
- 기능별 feature module 분리
- lint·typecheck·unit/component·production build
- Gradle JAR/WAR 재현 패키징
- 실제 API·권한·감사 연결
- 목록·상세·검색·정렬·페이징·다운로드·승인
- 브라우저 E2E가 불가능하면 성공 처리하지 않고 미검증으로 남긴다.

### 7.10 SQL·Migration·설치·Upgrade·Rollback

- SQL Owner를 명확히 하고 중복 module-local DDL 제거
- MariaDB 우선 실검증
- PostgreSQL·Oracle·SQL Server dialect·driver·migration 구조
- 신규 설치 합본과 증분 migration 일치
- 재실행 멱등성
- seed·권한 최소화
- upgrade·rollback·backup·restore
- stale SQL과 미참조 schema 제거
- 설치 결과를 DB 조회로 검증

### 7.11 EDU·OpenAPI·JavaDoc·개발 경험

- 회원·계좌·참조·대외 주제영역을 실제 Consumer가 있는 교육 예제로 유지
- CRUD·validation·검색·정렬·페이징
- Local/Remote 호출
- Batch·file·broker·security
- OpenAPI에 URI와 업무 ID, 오류, Header, 권한
- Public API와 SPI에 JavaDoc
- Generator 결과와 EDU 구조 일치

### 7.12 배포·운영·제품화

- JAR/WAR·Docker·분리 WAS
- multi-instance·rolling upgrade
- migration/rollback
- health/readiness
- metrics·alert·incident·runbook
- backup/DR
- configuration reference
- license·third-party notices·CHANGELOG
- private 개발 Repository와 공개 release Repository 분리 전략
- 공개 Repository는 개발 이력 없이 독립 build 가능

## 8. 요구사항 Catalog

아래 133개 도메인은 삭제하거나 임의로 병합하지 않는다. 상세 반복 카드는 제거하고 공통 완료 묶음과 도메인별 Acceptance Criteria로 관리한다.


### 아키텍처·호출·공통 실행 기반

- `ARCH-MISSION` — CPF 최종 목표/상용 솔루션 원칙
- `ARCH-MSA` — MSA-first 및 Modular Monolith 호환
- `ARCH-BOUNDARY` — 주제영역 경계/Bounded Context
- `ARCH-LAYER` — 계층/패키지/의존성 규칙
- `FACADE-LOCAL` — Local Facade 표준
- `FACADE-REMOTE` — Remote Facade Proxy/Port-Adapter
- `PFW-CALL` — CpfWebClient/CpfRestClient Service Call Engine
- `PFW-REGISTRY` — Service/Endpoint/Instance Registry
- `PFW-ROUTING` — LB mode/direct instance/discovery routing
- `PFW-HEALTH` — Liveness/Readiness/Dependency Health
- `PFW-RESILIENCE` — Timeout/Retry/Circuit/Bulkhead/Backpressure
- `PFW-DEADLINE` — Request Deadline/Timeout Budget
- `PFW-HEADER` — 표준/확장 헤더
- `PFW-CONTEXT` — TransactionContext/MDC/Thread Context
- `PFW-TXID` — transactionGlobalId/segment/timeline
- `PFW-ROLE` — transactionRole/direction/source-target
- `PFW-OPSDB` — PFW 운영 DB 공유/장애모드
- `PFW-LOGDB` — DB 로그/Segment 로그
- `PFW-FILELOG` — cpf-{moduleCode}-{logType}.log 파일 로그
- `PFW-LOGFAIL` — 로그 실패/fail-open/local spool
- `PFW-TRACE` — Trace Boost/동적 로그 레벨
- `PFW-MASK` — 마스킹/민감정보 보호
- `PFW-ERROR` — 오류/예외/응답 표준
- `PFW-VALID` — Validation Framework
- `PFW-IDEMP` — Idempotency 표준
- `PFW-STATE` — 상태 전이 State Machine
- `PFW-LOCK` — Optimistic/Distributed Lock
- `PFW-SCHED` — Scheduler 표준

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
- `SAMPLE-XYZ` — XYZ 신규 주제영역 샘플
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

## 9. 공통 완료 묶음

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


## 10. 필수 실행 검증

최소 다음을 최신 Source에서 clean 상태로 실행한다.

1. 전체 Gradle clean test
2. 전체 JAR/WAR packaging
3. frontend npm clean install, lint, typecheck, unit/component, production build
4. MariaDB full install, seed, 재실행, migration, 최소 권한
5. ACC generator lifecycle
6. 4개 DB Vendor generator matrix
7. 2개 BAT Worker 실프로세스
8. Gateway와 주요 서비스 Runtime/OpenAPI
9. Local/Remote service call
10. 파일 로그/DB 로그 parity
11. 대외 timeout·unknown result·reconciliation
12. outbox/inbox/DLQ 또는 미구현 상태 명시
13. ADM/BZA HTTP smoke와 가능한 Browser E2E
14. secret·dependency·license·garbage scan
15. 설치·upgrade·rollback smoke
16. public package 독립 build 검증

실행 환경이 없어 수행하지 못한 검증은 `미검증`이며 대체 Evidence만으로 완료 처리하지 않는다.

## 11. Evidence 기준

Evidence마다 다음을 포함한다.

- 기준 Commit
- 실행 명령
- Profile·OS·JDK·DB·Broker·Node 환경
- 시작·종료 시각
- 관련 Requirement ID
- 종료 코드
- 핵심 결과와 원문 로그 경로
- DB 조회 또는 HTTP 응답
- 민감정보 제거 여부
- 생성 도구 버전과 stale 여부

빈 로그, 정적 검색 결과, 과거 Commit Evidence, 자동 생성 inventory만으로 Runtime 성공을 주장하지 않는다.

## 12. 관리 문서 갱신

작업 마지막에만 다음을 동기화한다.

- `CPF_FINAL_TARGET_REQUIREMENTS.md`
- `CPF_GAP_MATRIX.md`
- `CPF_STABILIZATION_REPORT.md`
- `CPF_EVIDENCE_INDEX.md`
- 기능 구현 Matrix
- EDU Coverage Matrix
- README
- CHANGELOG

상태 문서는 작업 시작 전에 일괄 완료로 바꾸지 않는다.

## 13. 완료 금지 조건

다음 중 하나라도 있으면 전체 완료로 보고하지 않는다.

- 공식 Module·Package·SystemCode migration 미완료
- Generator가 목표 구조를 생성하지 못함
- AI 전용 기능이 `cpf-core` 또는 기본 제품에 잔존
- 133개 도메인을 문서 숫자만으로 완료 처리
- Source 없는 완료 표기
- 실행하지 않은 테스트 성공 표기
- ACC lifecycle 미검증
- BAT 다중 프로세스·lease·takeover 미검증
- 파일/DB 로그 parity 미검증
- 중복 SQL 또는 migration 불일치
- frontend build만 있고 API·권한·패키징 연결 없음
- stale Evidence 또는 민감정보 포함
- Root에 작업·Evidence·임시 문서 산재
- 기존 성공 기능 회귀
- Dead Code·가비지·중간 산출물 잔존

## 14. 작업 결과 보고 형식

```text
1. 기준 Commit과 최종 Commit
2. 요구사항별 상태
3. 변경 Module·Package·SystemCode
4. 구현·수정·삭제 파일
5. 실행 명령과 종료 코드
6. Runtime·DB·Browser 결과
7. 실패·미검증·재확인 필요
8. migration·rollback·호환성
9. 제거한 중복·Dead Code·Stale Evidence
10. 남은 실제 Gap
```

## 15. Git 수행 규칙

- 작업 도중 임의 중간 Push를 하지 않는다.
- 사용자 요청서와 최상위 목표 문서를 보호한다.
- 작업이 모두 끝나고 검증·문서 동기화가 완료된 뒤 한 번의 정리된 Commit/Push를 수행한다.
- 불가피한 중간 Commit은 로컬에만 유지하고 최종 보고에 이유를 기록한다.
- Push 직전 secret scan과 `git status`, `git diff --check`를 확인한다.
