# CPF 20260730_04 Remaining Development Request

## 1. 요청 목적

최신 `master`의 Push 반영 후 재검수에서 확인되거나 완료 증적이 없는 개발 항목을 제품 수준으로 폐쇄한다.

이 요청은 단순 검증 요청이 아니다. 실제 결함 또는 Consumer 연결 누락이 확인되면 ChatGPT가 직접 Source·SQL·Frontend·Test·Guide·Evidence를 함께 개발한다.

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 작성 기준 SHA: `8fb30708f4accc189c00c6fbf020ab4b22f6c51f`
- 최우선 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 역할: ChatGPT 개발 / Codex 최종 독립 검수
- 사용자 승인 없는 Commit·Push·Branch 생성 금지

다음 세션 시작 시 `origin/master`가 달라졌으면 새 SHA에서 요구사항과 실제 구현을 다시 대조한다.

---

## 2. 공통 완료 원칙

각 Requirement는 다음을 하나의 완료 단위로 처리한다.

- 올바른 Owner Module과 의존성 방향
- Public API/SPI/Internal 경계
- 실제 Consumer와 Runtime Wiring
- 정상·오류·경계·부분 실패
- 멱등성·동시성·다중 인스턴스
- 재시도·복구·재처리·결과 불명 처리
- SQL·Migration·Install·Upgrade·Rollback
- 보안·권한·감사·마스킹
- 운영 조회·제어·승인
- Test·EDU·OpenAPI·JavaDoc·Guide
- 최신 Exact-SHA Evidence
- Generator와 Generated Domain 영향
- 기존 성공 기능 회귀 방지

Interface·Table·Controller·화면만 존재하고 실제 Consumer가 없으면 완료가 아니다.

---

# 3. P0 — DB Canonical·Install·Migration

## `P0-DB-01` FK 의존성 기반 생성 순서

### 확인 상태

최신 PostgreSQL Clean Install에서 자식 Table이 부모보다 먼저 생성되는 사례가 확인됐다.

### 필수 개발

- `platform-schema.json` 또는 Canonical Source에 FK Dependency Graph를 구성한다.
- Table 생성은 Topological Sort로 정렬한다.
- Cycle이 있으면 조용히 임의 순서로 생성하지 말고 명확히 실패한다.
- Spring Batch 표준 Table도 Dependency Order를 보장한다.
- Source DDL, Empty Install, All Install, Smoke Bundle, Generated Domain DB Artifact를 동일 규칙으로 생성한다.
- 수동 Vendor SQL 재배열로만 처리하지 않는다.

### 필수 회귀 대상

- `cpf_gateway_apply_status` / `cpf_gateway_binding`
- `cpf_gateway_attempt` / `cpf_gateway_transaction`
- `cpf_gateway_binding` / `cpf_gateway_server_group`
- `cpf_runtime_policy_delivery` / `cpf_runtime_policy_event`
- `BATCH_JOB_EXECUTION` / `BATCH_JOB_INSTANCE`

### 완료 Evidence

- 3 Vendor 생성 순서 Gate PASS
- 3 Vendor Clean Install 실제 실행 PASS
- 존재하지 않는 부모 FK를 참조하면 생성 전 Gate FAIL

---

## `P0-DB-02` Oracle 빈 문자열 의미 정합성

### 필수 개발

- Oracle에서 `NOT NULL DEFAULT ''`를 생성하지 않는다.
- Optional Text의 Canonical 의미를 `NULL`, 명시적 Sentinel 또는 별도 Flag 중 하나로 결정한다.
- Java DTO, Repository Row Mapping, API Response, UI 표시를 동일 의미로 맞춘다.
- PostgreSQL/MariaDB와 Oracle의 의미가 달라지지 않게 Vendor 변환 Test를 추가한다.
- 기존 Migration과 신규 Clean Install 모두 수정한다.

### 완료 Evidence

- Oracle DDL 정적 Gate
- Oracle 실제 Install/Insert/Select Test
- 빈 값·NULL·공백 문자열 경계 Test

---

## `P0-DB-03` PK Identity·Sequence 정책 통일

### 확인 대상

- `bat_job_definition_audit.audit_id`
- 신규 Gateway/Runtime Policy/Batch Table의 자동 생성 PK

### 필수 개발

- Canonical PK Generation Strategy를 선언한다.
- MariaDB Auto Increment, PostgreSQL Identity, Oracle Identity/Sequence를 동일 논리 계약으로 생성한다.
- Application이 ID를 생성하는 Table과 DB가 생성하는 Table을 구분한다.
- Migration·Source·Install·Rollback·Repository Insert를 일치시킨다.

### 완료 Evidence

- ID를 생략한 정상 Insert
- 명시적 충돌 ID 오류
- Rollback/Reapply 후 Sequence/Identity 정상성

---

## `P0-DB-04` 3 Vendor Artifact 완전 동기화

### 필수 개발

Canonical 변경 후 다음을 전부 재생성·비교한다.

- Oracle / PostgreSQL / MariaDB Source DDL
- Empty Install / All Install / Smoke
- Flyway Migration / Rollback
- Runtime Query Pack
- Schema Manifest / Checksums
- Generated Domain DB Artifact
- 초기 Metadata·Seed·Verify SQL

Table 수만 같다고 완료 처리하지 않는다. Column, Type, Default, PK, FK, Index, Constraint, Identity, Comment, Logical DB Ownership까지 비교한다.

---

## `P0-DB-05` Existing DB와 Clean DB Lifecycle

### 필수 Scenario

- 기존 MariaDB Upgrade → Verify → Rollback → Reapply
- 별도 Clean MariaDB Install
- PostgreSQL Clean Install 및 Migration Lifecycle
- Oracle Clean Install 및 Migration Lifecycle
- Drift가 있으면 SKIP하지 않고 Drift/Migration 오류로 실패
- 존재하지 않는 Column을 참조하는 Index/FK 사전 검출

실행할 수 없는 Vendor는 `미검증`으로 남기고 성공으로 기록하지 않는다.

---

# 4. P0 — Batch Job Definition Control Plane

## `P0-BAT-01` Published Definition Runtime Projection

### 필수 개발

- `PUBLISHED` Definition을 Scheduler/Worker/Agent가 소비하는 Runtime Projection으로 변환한다.
- Projection에는 `jobId`, `definitionVersion`, `checksum`, `executorType`, `executorReference`, Trigger, Parameter Schema, Dependency, Retry, Timeout, Misfire, Unknown Result, Compensation, SLA를 포함한다.
- Publish와 Runtime Projection 저장 사이의 Transaction/Outbox 경계를 명확히 한다.
- 다중 인스턴스가 동일 Version을 중복 적용해도 멱등이어야 한다.
- Retired/Expired Definition은 신규 실행에 사용되지 않아야 한다.

### 완료 Evidence

ADM에서 작성·검증·승인·Publish한 Definition이 실제 Worker 실행까지 연결되고 실행 이력에 동일 Version/Checksum이 남아야 한다.

---

## `P0-BAT-02` Scheduler·Worker·Agent 실제 Consumer

### 필수 개발

- Scheduler가 Published Projection을 조회해 Trigger를 등록·갱신·해제한다.
- Worker/Agent가 실행 시 고정 Definition Version을 읽는다.
- 실행 중 Definition 변경이 기존 실행 의미를 바꾸지 않는다.
- 동일 JVM과 분리 WAS/Agent 모두 성립한다.
- Agent Pool·Zone·Capacity·Drain·Maintenance·Fencing을 반영한다.
- Target Down, Timeout, Retry, Unknown Result, 재처리 흐름을 구현한다.

---

## `P0-BAT-03` Version 편집과 자식 데이터 보존

### 필수 개발

- Published Version을 직접 수정하지 않고 새 Version으로 Clone한다.
- 상세 조회는 Parameter·Dependency·정책 전체를 반환한다.
- 부분 Payload 저장으로 기존 자식 목록이 조용히 삭제되지 않게 한다.
- Replace, Patch, Delete 의미를 API에서 구분한다.
- Optimistic Lock 충돌을 명확히 반환한다.
- Dependency Cycle, 자기 참조, 존재하지 않는 Job 참조를 차단한다.

---

## `P0-BAT-04` 상태 전이·Maker-Checker·감사

### 필수 개발

- 허용 상태 전이를 Server State Machine으로 강제한다.
- Client Body의 `definitionState` 직접 변경을 신뢰하지 않는다.
- 작성자와 승인자를 분리한다.
- Publish·Retire·Rollback은 별도 권한, 사유, 확인, 감사가 필요하다.
- 감사 Insert 실패를 성공으로 삼키지 않는다.
- Before/After, operatorId, reason, transactionId, traceId를 남긴다.

---

## `P0-BAT-05` Typed Parameter·Secret·Validation Runtime 적용

### 필수 개발

- `CpfParameterSchema`를 UI 검증에만 쓰지 말고 실행 직전 Runtime에서 다시 검증한다.
- Secret은 원문이 아니라 Alias/Reference만 허용한다.
- Required, Allowed Values, Pattern, Min/Max, Length, Code Reference를 적용한다.
- Runtime Override가 금지된 Parameter는 요청에서 변경하지 못하게 한다.
- 민감 Parameter를 로그·DB 일반 컬럼·Evidence에 원문 저장하지 않는다.

---

## `P0-BAT-06` Dependency·Misfire·Unknown·Compensation 폐쇄

### 필수 개발

- Dependency 조건과 Timeout을 실제 Scheduler/Worker가 적용한다.
- Misfire 정책별 Skip/Immediate/Next/Compensate 의미를 명확히 한다.
- Unknown Result를 성공/실패로 추정하지 않고 운영 재확인·재처리 흐름으로 보낸다.
- Compensation Reference가 실제 Handler/SPI Consumer와 연결된다.
- SLA 지연·미실행·실패 알림을 운영 화면과 Notification에 연결한다.

---

# 5. P0 — Gateway Registry·Binding·Runtime

## `P0-GWY-01` ADM Controller와 Adapter Bean Wiring

### 필수 개발

- `AdmGatewayRegistryController` 등 ADM API가 제품 Profile에서 실제 Gateway Adapter Bean과 연결되는지 확인한다.
- 선택 제품 미사용 Profile과 사용 Profile을 명확히 분리한다.
- 사용 Profile에서 Port가 없으면 조용히 Controller가 사라지지 말고 Startup Fail-closed 또는 명확한 Capability 상태를 제공한다.
- 동일 JVM과 분리 Gateway Deployment 모두 지원한다.

---

## `P0-GWY-02` Binding 상태 전이와 승인

### 필수 개발

- DRAFT → VALIDATED → APPROVAL → PUBLISHED → RETIRED 등 상태 전이를 Server에서 강제한다.
- Route/Server Group/Policy/Timeout/Retry/Idempotency 조합을 Publish 전에 검증한다.
- 외부 노출은 기본 DENY로 유지한다.
- 인증·인가·TLS·Rate Limit·Header Policy가 없는 외부 Binding은 Publish하지 않는다.
- Maker-Checker, 권한, 사유, 감사, Optimistic Lock을 적용한다.

---

## `P0-GWY-03` Apply·ACK·Retry·Drift·Reconcile·Rollback

### 필수 개발

- Published Binding을 Gateway Instance로 배포한다.
- 각 Instance의 Expected/Applied Version과 ACK를 저장한다.
- 부분 적용, Instance Down, Timeout, Stale ACK를 구분한다.
- Retry는 멱등이고 Fencing/Version을 적용한다.
- Drift 조회, Reconcile, Rollback, Retire를 운영 Action으로 제공한다.
- 운영 Action 자체 실패가 원 업무 거래를 오염시키지 않게 Transaction을 분리한다.

---

## `P0-GWY-04` 실제 Connection Test 실행

### 필수 개발

- 단순 결과 저장 API가 아니라 실제 Direct/E2E 연결시험 Executor를 구현한다.
- DNS, Connect, TLS, Authentication, Authorization, Protocol, Response 단계별 실패를 구분한다.
- 대상 Instance 지정 시험과 Server Group 시험을 지원한다.
- Timeout과 민감정보 마스킹을 적용한다.
- 시험 결과에 testId, bindingId, gatewayInstanceId, instanceId, traceId, operationId, duration, failureStage를 저장한다.

---

## `P0-GWY-05` Transaction·Attempt 원장 실제 연결

### 필수 개발

- IN/GATEWAY/OUT/RESULT 흐름과 Retry/Failover Attempt를 실제 호출 Runtime에서 기록한다.
- 하나의 `transactionId`와 `traceId`로 연계한다.
- 각 Attempt의 Target, Duration, Protocol Status, Failure Code를 남긴다.
- 결과 불명과 최종 성공/실패를 구분한다.
- 운영 Logging 실패가 원 거래를 불필요하게 실패시키지 않되, 감사 필수 조치는 Fail-closed 정책을 따른다.

---

## `P0-GWY-06` ADM 운영 화면 완성

### 필수 개발

- Registry, Server Group, Binding, Approval, Apply Status, Health, Connection Test, Transaction/Attempt를 목적별 화면·Route로 분리한다.
- 여러 메뉴가 동일 단일 화면 별칭으로만 연결되지 않게 한다.
- 검색, Paging, 상세, 상태 Badge, 오류, 권한, 위험 조치 확인, 반응형·접근성을 제공한다.
- Raw JSON Textarea/`<pre>` 중심 화면으로 회귀하지 않는다.
- READ 권한 사용자는 변경 버튼을 볼 수 없고, 위험 Action은 별도 권한을 요구한다.

---

# 6. P0 — Runtime Policy Distribution

## `P0-RTP-01` Metadata Codec 정합성

### 필수 개발

- 다중 Metadata, 줄바꿈, 구분자, Unicode, 빈 값, Escape 문자를 손실 없이 Encode/Decode한다.
- 임의 문자열 Split 정규식에 의존하지 말고 Versioned JSON/Typed DTO 등 안정된 Format을 사용한다.
- Payload Checksum과 Version 호환성을 검증한다.

---

## `P0-RTP-02` Vendor 중립 Row Mapping

### 필수 개발

- `queryForMap()`의 Column Key 대소문자에 의존하지 않는다.
- 명시적 `RowMapper` 또는 Case-insensitive Mapping을 사용한다.
- Oracle 대문자 Label, PostgreSQL/MariaDB 소문자 Label에서 동일하게 동작한다.
- Null/Empty/Sentinel 의미를 Vendor별로 동일하게 복원한다.

---

## `P0-RTP-03` Claim·Lease·Fencing·ACK·Retry

### 필수 개발

- 다중 Consumer가 같은 Event를 중복 적용하지 않게 Claim과 Fencing을 원자적으로 처리한다.
- Lease 만료 후 안전한 재Claim을 지원한다.
- Stale Worker의 ACK를 거부한다.
- Retryable/Non-retryable/Poison Event를 구분한다.
- Error Message는 Sanitizing 후 저장한다.
- Applied/Failed/Ignored 의미와 운영 재처리 Action을 제공한다.

---

## `P0-RTP-04` Transaction·Outbox 경계

### 필수 개발

- 정책 변경과 Durable Event 생성의 원자성을 보장한다.
- Runtime Delivery 실패가 정책 원장 자체를 잃게 하지 않는다.
- 원 업무 Transaction과 운영 배포 Transaction을 적절히 분리한다.
- 이벤트 중복 생성·중복 적용에 멱등성을 적용한다.

---

# 7. P0 — Gate·Evidence·문서 정본

## `P0-GATE-01` Exact-SHA Active Context

### 필수 개발

- Current Request, Handover, Continuity/State, Current Evidence가 최신 Clean Source SHA를 가리키게 한다.
- 과거 Archive/Evidence는 과거 SHA를 유지할 수 있으나 Active 정본과 혼동되지 않게 한다.
- Root `qualityGate`와 최종 완료 Gate가 `check-work-context-sha.ps1`을 실제 실행하게 한다.
- `-RequireCurrentEvidence` 사용 조건을 정하고 최종 Gate에서 강제한다.

---

## `P0-GATE-02` False Green 방지

### 필수 개발

- Runtime Query Gate가 HTTP `DELETE` 등을 SQL로 오인하지 않게 한다.
- Vendor-only Inline SQL을 Statement 단위로 검출한다.
- Gate가 Source를 자동 수정하거나 Migration Checksum을 자기 갱신하지 않게 한다.
- UTF-8 without BOM 정책을 Source/JSON/SQL/Script에 일관되게 적용한다.
- Test 삭제·Skip·Assertion 약화가 Gate PASS로 이어지지 않게 한다.

---

## `P0-GATE-03` Matrix·Ledger·Evidence 의미 검증

### 필수 개발

- `완료` Requirement에는 실제 구조화 Evidence JSON이 있어야 한다.
- Evidence에는 Source SHA, Command, Profile/Environment, Start/Finish, Exit Code, Status, Requirement IDs, Sanitizing 확인을 포함한다.
- 단순 문자열 언급이나 과거 SHA Evidence로 완료 처리하지 않는다.
- 162/816/387/2,715 추적 자료의 중복·누락·상태·Evidence를 행 단위로 검증한다.

---

## `P0-DOC-01` Current 문서 Hygiene

### 필수 개발

- `cpf-docs/work/current`에는 현재 작업 정본만 남긴다.
- 과거 20260729 요청서, Checkpoint, 완료 보고, 중복 Codex 요청은 Archive/Handover로 이동한다.
- 동일 역할 문서를 여러 개 Current에 두지 않는다.
- README에 작업 일지나 진행률을 넣지 않는다.
- 이동·삭제 전 Archive 사본과 참조 링크를 보존한다.

---

# 8. P1 — File·Shell 보안

## `P1-FILE-01` Malware Scanner 실제 연결

- `malwareScanRequired` 설정이 실제 Scanner SPI/구현을 호출하게 한다.
- Scanner 미설치 Profile은 Startup 또는 Upload 시 Fail-closed한다.
- Scan Timeout, Scanner Down, Suspicious, Infected, Unknown을 구분한다.
- 원 파일 격리·삭제·재처리·감사·운영 조회를 제공한다.

## `P1-SHELL-01` Signature Verifier와 승인 Artifact

- Signature Required가 실제 Verifier를 호출한다.
- Hash만 맞는다고 Signature 성공으로 처리하지 않는다.
- Trust Store/Signer/Algorithm/Expiry/Revocation을 검증한다.
- 미지원이면 제품 Capability와 Guide에 명확히 표시하고 실행을 차단한다.

## `P1-SHELL-02` Interpreter Version Pinning

- 실행 전 Interpreter 경로와 실제 Version을 검증한다.
- 승인된 Version 범위를 벗어나면 Fail-closed한다.
- PATH Hijacking, Working Directory, Parameter File 권한을 점검한다.
- Command Line과 로그에 Secret 원문을 넣지 않는다.

---

# 9. P1 — Generator·API·Frontend

## `P1-GEN-01` Golden Template Parity

- 신규 Gateway/Registry/Parameter 계약을 Golden Template과 Manifest에 반영한다.
- 최소 2개의 임의 DomainName/SystemCode로 생성한다.
- 생성 후 Build, DB Artifact, Route, Config, Package, Test를 검증한다.
- 사용자 수정 영역을 덮어쓰지 않는다.

## `P1-GEN-02` 고정 Domain 가정 제거

- MBR/ACC/EXS 이름, DB, Route, Package, Seed를 Script와 Source에서 고정 가정하지 않는다.
- `cpf-member`는 Golden Reference Instance로 사용할 수 있지만 Generator 예외 규격이 되어서는 안 된다.

## `P1-API-01` Typed DTO와 OpenAPI

- Public Controller의 임의 `Map<String,Object>` 계약을 Typed Request/Response DTO로 교체한다.
- Validation, 오류 계약, 권한, Examples, OpenAPI 설명을 제공한다.
- Internal Implementation을 외부 API에 노출하지 않는다.

## `P1-UI-01` ADM/BZA 상용 운영 UX

- Raw JSON 입력·출력, 단일 거대 Page, 기능 없는 별칭 Route를 제거한다.
- 검색·Paging·상세·상태·오류·권한·확인·감사·접근성을 기능 특성에 맞게 제공한다.
- BZA Recursive Menu, 조직, 권한, 알림 등 기존 성공 기능을 회귀시키지 않는다.

---

# 10. P1 — Test와 Runtime 검증

## Java/Build

- Root `clean test assemble`
- Module Ownership·Dependency·Public Boundary Gate
- 정상·오류·경계·부분 실패 Unit/Integration Test

## Frontend

- ADM/BZA Typecheck
- Lint
- Vitest
- Production Build
- Browser E2E와 READ/WRITE/DELETE 권한 Negative Test

## Runtime

- MariaDB Existing/Clean
- PostgreSQL
- Oracle
- Redis 장애·복구·다중 인스턴스
- Gateway Local/Remote/Target Down/Timeout/Retry/Failover/Unknown
- Batch Multi-worker/Lease/Fencing/Misfire/Restart/Center-Cut
- File Upload/Scan/Restart/Checksum/Duplicate
- Browser 위험 조치 확인·감사

실행 환경이 없으면 해당 항목은 `미검증`으로 남긴다.

---

# 11. P2 — Release·Repository Hygiene

- Root의 build/log/tmp/zip/bak/patch 등 개발 잔재 제거
- `.gitignore`와 Hygiene Gate 보강
- 설치·Migration·Upgrade·Rollback·장애 복구 Guide 현행화
- Release Package가 외부 CDN/Font/Script에 의존하지 않게 확인
- OSS License와 Third-party Notice 정합성 확인
- Release 후보 SHA에서 Clean Working Tree와 Local/Remote 일치 확인

---

# 12. QA 신규 요건 병합 규칙

사용자가 QA 목록을 전달하면 다음 Matrix를 만든다.

| QA 원문 ID | 요약 | Existing Requirement | 신규 여부 | Owner | 개발/검증 | 완료 조건 |
|---|---|---|---|---|---|---|

- 동일 Root Cause는 기존 Requirement에 연결한다.
- 이름만 다른 중복 Gap을 신규 건수로 집계하지 않는다.
- QA가 발견한 회귀는 P0로 승격할 수 있다.
- QA Scenario만으로 Source 구현을 완료 처리하지 않는다.
- QA 반영 후 최종 고유 Requirement 수를 사용자에게 먼저 보고한다.

---

# 13. 권장 실행 순서

1. 최신 master/정본 확인
2. P0-DB-01~05
3. P0-BAT-01~06
4. P0-GWY-01~06
5. P0-RTP-01~04
6. P0-GATE/DOC
7. P1 File/Shell/Generator/API/UI
8. Static Gate와 Targeted Test
9. 전체 Build와 Frontend
10. 3 Vendor·Redis·다중 인스턴스·Browser Runtime
11. Matrix/Ledger/Evidence 폐쇄
12. Current/Handover/Continuity 갱신
13. 사용자 Commit/Push
14. Codex Review-only 독립 검수

---

# 14. 완료 처리 금지 조건

다음 중 하나라도 남으면 요청 전체를 완료 처리하지 않는다.

- 부분 구현·미구현·TODO·Placeholder
- 실제 Consumer 없는 Interface/Controller/Table/UI
- Clean Install FK 순서 오류
- Vendor 간 Identity/Default/FK 차이
- Batch Definition과 실행 Runtime 미연결
- Gateway Apply/Test/Reconcile 미연결
- 위험 조치 권한·사유·감사 누락
- 보안 설정만 존재하고 Scanner/Verifier 미연결
- Test 삭제·약화·Skip
- 최신 Exact-SHA Evidence 없음
- Matrix/Ledger 미폐쇄
- Dirty Working Tree 또는 Local/Remote 불일치

