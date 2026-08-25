# CPF 공식 산출물 Document Profile Catalog

이 파일은 `profiles/*.json`의 사람이 읽는 인덱스다. **실행 정본은 JSON Profile이며 이 Markdown을 편집해 구조를 바꾸지 않는다.**

# README — Core Platform Framework README

- Primary Persona: CPF를 처음 보는 개발자, Architect/기술 의사결정자, 운영 담당자
- Entry: Repository 첫 화면에서 CPF를 평가하거나 처음 실행하려는 상태
- Exit: CPF가 주는 실제 편의와 주요 기능을 이해하고 Bootstrap/Domain 생성/Build/Test의 시작점을 찾으며 상세 문서를 선택할 수 있다.
- Orientation: screen
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. 업무 호출은 배포 위치가 바뀌어도 같은 계약을 유지합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `TOPOLOGY_COMPARE`
- Required Figure Preset: `REQUEST_FLOW`
- H2 순서:
  1. Same JVM에서 호출할 때
  2. Remote로 호출할 때
  3. 업무 호출 코드가 배포 Topology에 묶이지 않는 이유

## H1-2. 거래 Context는 Framework가 시스템 경계를 넘어 이어줍니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `SYSTEM6`
- Required Figure Preset: `REQUEST_FLOW`
- H2 순서:
  1. CPF가 자동으로 관리하는 Canonical System6
  2. 신뢰 경계에서 Header를 다시 구성하는 경우
  3. 업무 Controller에 들어가기 전에 검증하는 값

## H1-3. 거래 하나를 Log·Trace·Timeline에서 같은 식별자로 따라갑니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `OPERATION_TRACE`
- H2 순서:
  1. transactionId가 연결하는 범위
  2. operationId가 가리키는 실행 계약
  3. instanceId가 가리키는 실제 Runtime

## H1-4. DB Commit과 원격 Side Effect의 결과를 같은 실패로 다루지 않습니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `REQUEST_FLOW`
- H2 순서:
  1. Local Transaction
  2. Remote Side Effect
  3. 부분 실패가 발생했을 때

## H1-5. 결과를 확정할 수 없을 때 UNKNOWN으로 남기고 다시 확인합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `STATE_TRANSITION`
- H2 순서:
  1. UNKNOWN으로 남겨야 하는 경우
  2. Reconcile이 확인하는 것
  3. 성공·실패를 임의 판정하지 않는 이유

## H1-6. 같은 요청이 다시 들어와도 중복 처리를 제어합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `STATE_TRANSITION`
- H2 순서:
  1. Durable Idempotency
  2. Retry와 Idempotency의 역할 차이
  3. 중복 Side Effect를 줄이는 방식

## H1-7. 분산 거래는 Saga·TCC·XA를 상황에 맞게 선택합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `DECISION_FLOW`
- H2 순서:
  1. Saga를 선택하는 경우
  2. TCC를 선택하는 경우
  3. XA를 선택하는 경우
  4. 선택 전에 확인할 Transaction 경계

## H1-8. Batch는 실행과 제어를 분리하고 재시작과 재처리를 구분합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `RESTART_RERUN_REPROCESS_RECONCILE`
- Required Figure Preset: `BATCH_TOPOLOGY`, `BATCH_RECOVERY`
- H2 순서:
  1. Control Plane·Scheduler·Worker·Center-Cut·Agent
  2. Restart·Rerun·Reprocess·Reconcile
  3. Worker 장애와 Process Kill 이후

## H1-9. 필요한 기능만 Starter와 Provider로 조합합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `STARTER_SELECTION`
- Required Figure Preset: `CAPABILITY_GALLERY`
- H2 순서:
  1. Public Profile에서 시작
  2. Capability와 Provider를 선택하는 경우
  3. Internal 구현을 업무 코드에서 직접 쓰지 않는 이유

## H1-10. Domain을 만들 때 구조·Starter·DB 연결을 같은 규칙으로 맞춥니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L2`
- Required Table Preset: 없음
- Required Figure Preset: `GENERATOR_FLOW`
- H2 순서:
  1. Generated Domain의 Canonical 구조
  2. Business Feature와 기술 역할을 나누는 방식
  3. 생성 후 Sync·Build·Test

## H1-11. Oracle·PostgreSQL·MariaDB를 하나의 DB Lifecycle로 관리합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `DB_VENDOR`
- Required Figure Preset: `DB3_LIFECYCLE`
- H2 순서:
  1. 공식 DB Vendor
  2. Migration·Seed·Upgrade
  3. Rollback 또는 Recovery
  4. Generator와 Runtime Query까지 함께 맞추는 이유

## H1-12. 외부 연계·Messaging·File도 실패와 재처리까지 같은 운영 관점으로 연결합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `CAPABILITY_GALLERY`
- H2 순서:
  1. External HTTP·Fixed-Length
  2. Kafka·RabbitMQ·JMS·IBM MQ
  3. Attachment·Object Storage
  4. Timeout·Retry·DLQ·Replay·UNKNOWN

## H1-13. Security·승인·Audit를 업무 흐름과 따로 떼어 두지 않습니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `SECURITY_AUDIT_FLOW`
- H2 순서:
  1. Authentication과 Authorization
  2. Permission과 Operation Policy
  3. Approval·Reason·Audit
  4. Secret·Masking 경계

## H1-14. Gateway와 Backoffice는 역할이 다르고 필요한 경우에만 배치합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `GATEWAY_OPTIONALITY`
- H2 순서:
  1. Gateway를 사용하는 경우
  2. 허용된 Direct Public HTTP
  3. CPF Backoffice와 업무 Domain 경계
  4. DB-less Backoffice Web/BFF

## H1-15. 운영자는 거래와 Runtime을 같은 Control Plane에서 확인합니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `OPERATION_TRACE`
- H2 순서:
  1. Health·Log·Trace
  2. Incident·Recovery
  3. Batch·Gateway·Security 운영
  4. 위험 조치의 Permission·Reason·Approval·Audit

## H1-16. Bootstrap에서 Build·Test·Runtime까지 처음 시작 흐름을 줄입니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L2`
- Required Table Preset: 없음
- Required Figure Preset: `LIFECYCLE`
- H2 순서:
  1. Prerequisite 확인
  2. Local Bootstrap
  3. Domain 생성·Sync
  4. Build·Test·Runtime 확인
  5. Stop과 Reset의 차이

## H1-17. 필요한 상세 문서는 역할에 따라 바로 찾습니다
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. 업무 개발
  2. Batch 개발
  3. 운영
  4. Architecture와 Contract
  5. 기술·DB 표준

## H1-18. Community & Evaluation License
- Content Model: `BROCHURE_PANEL`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: 없음
- H2 순서:
  1. 사용 범위 안내

# FRAMEWORK_DEVELOPER_GUIDE — CPF 프레임워크 개발자 가이드

- Primary Persona: CPF 업무 개발자
- Entry: Spring Boot/Java는 알지만 CPF 기능 선택과 구현 방법을 찾아야 하는 상태
- Exit: 업무 기능을 선택하고 실제 Public API/Starter/옵션을 사용해 구현·테스트하고 운영 인계까지 할 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. 개발 환경과 Domain 생성
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `STARTER_SELECTION`, `SOURCE_NAV`
- Required Figure Preset: `GENERATOR_FLOW`
- H2 순서:
  1. 개발 전에 확인할 환경
  2. Public Profile과 Starter 선택
  3. 신규 Domain 생성
  4. Business Feature 구성
  5. Domain Sync
  6. Build와 빠른 검증
  7. 생성 결과 확인
  8. 삭제/Reset이 필요한 경우

## H1-2. CPF Framework 공통 기능
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `OPTION`, `SOURCE_NAV`
- Required Figure Preset: 없음
- H2 순서:
  1. 공통 기능을 언제 사용하는가
  2. 공통 코드
  3. 공통 파라미터
  4. 메시지 / Error Catalog
  5. 영업일 / Calendar
  6. Template
  7. 공통 관리 API
  8. Cache Refresh / 변경 전파
  9. 사용 범위와 Business Domain Master Data의 차이
  10. Test
  11. Source / EDU

## H1-3. CRUD 개발
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `OPTION`, `TEST_SCENARIO`, `SOURCE_NAV`
- Required Figure Preset: 없음
- H2 순서:
  1. 필요한 CRUD 기능 고르기
  2. 단건 조회
  3. 목록·검색·Paging
  4. 등록
  5. 수정
  6. 삭제
  7. 조건 조회
  8. Transaction과 함께 사용
  9. Validation
  10. 오류 처리
  11. Test
  12. Source / EDU

## H1-4. Controller 거래 API
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `OPTION`, `SYSTEM6`
- Required Figure Preset: 없음
- H2 순서:
  1. Controller 유형 선택
  2. @CpfController / @CpfRestController
  3. @CpfOnlineTransaction
  4. operationId·name·description
  5. Request/Response
  6. System6 진입 검증
  7. Validation과 Error Mapping
  8. OpenAPI operationId
  9. Test
  10. Source / EDU

## H1-5. Service 호출
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. Service 유형 선택
  2. @CpfService
  3. 동일 Application 내부 호출
  4. Execution/Workflow 사용
  5. 비동기 Operation 사용
  6. Transaction 경계
  7. 오류 전파
  8. Test
  9. Source / EDU

## H1-6. Repository와 Persistence
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `STARTER_SELECTION`, `API`, `DB_VENDOR`
- Required Figure Preset: 없음
- H2 순서:
  1. Persistence 방식 선택
  2. @CpfRepository
  3. JDBC
  4. MyBatis
  5. JPA
  6. Paging / Cursor
  7. Bulk 처리
  8. Optimistic Lock
  9. 분산 Lock
  10. Transaction 연결
  11. DB Vendor 영향
  12. Data Quality / Correction
  13. Test
  14. Source / EDU

## H1-7. 트랜잭션 관리
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `DECISION`, `OPTION`, `STATE`
- Required Figure Preset: `STATE_TRANSITION`
- H2 순서:
  1. 필요한 Transaction 방식 고르기
  2. Local REQUIRED
  3. REQUIRES_NEW
  4. Remote Side Effect가 포함되는 경우
  5. Saga
  6. TCC
  7. XA
  8. UNKNOWN / Reconcile이 필요한 경우
  9. Idempotency와 Retry
  10. 주요 옵션
  11. 실패 시 결과
  12. Test
  13. 잘못된 사용
  14. Source / EDU

## H1-8. 내부 Domain 호출
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `TOPOLOGY_COMPARE`, `API`, `OPTION`, `SYSTEM6`
- Required Figure Preset: `REQUEST_FLOW`
- H2 순서:
  1. Same JVM / Remote 선택은 누가 하는가
  2. CpfDomainClient
  3. Domain Binding / Registry
  4. 호출 옵션
  5. System6 전파
  6. operationId
  7. Timeout / Deadline
  8. Retry / Circuit
  9. UNKNOWN
  10. Self-HTTP 금지
  11. Test
  12. Source / EDU

## H1-9. 외부기관 호출
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `OPTION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. 연계 방식 고르기
  2. @CpfClient
  3. HTTP
  4. Timeout / TimeLimiter
  5. Retry / Resilience
  6. Fixed-Length
  7. TCP·File·Webhook 계약
  8. Credential / Certificate
  9. 부분 송수신
  10. GraphQL
  11. Realtime / SSE
  12. AI Provider 연계
  13. Webhook
  14. UNKNOWN / Reconcile
  15. Idempotency
  16. Masking / Audit
  17. Test
  18. Source / EDU

## H1-10. 오류 처리
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `ERROR`, `API`
- Required Figure Preset: 없음
- H2 순서:
  1. Business / Technical / Validation 오류 선택
  2. CpfResult / Response
  3. CpfBusinessException
  4. CpfSystemException
  5. CpfValidationException
  6. External Service 오류
  7. 오류 Catalog / Message
  8. HTTP Mapping
  9. UNKNOWN과의 차이
  10. Log / Trace 연결
  11. Test
  12. Source / EDU

## H1-11. Validation
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `API`, `TEST_SCENARIO`
- Required Figure Preset: 없음
- H2 순서:
  1. Validation을 적용하는 위치
  2. Request Validation
  3. 업무 Validation
  4. 표준 오류 변환
  5. 메시지
  6. 경계값 Test
  7. 잘못된 Validation
  8. Source / EDU

## H1-12. Idempotency
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `OPTION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Idempotency가 필요한 작업 고르기
  2. @CpfIdempotent
  3. Idempotency Key
  4. Durable Store
  5. 동시 요청
  6. Retry와의 관계
  7. External Side Effect
  8. Conflict
  9. Test
  10. Source / EDU

## H1-13. UNKNOWN과 Reconcile
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STATE`, `RUNBOOK`
- Required Figure Preset: `STATE_TRANSITION`
- H2 순서:
  1. UNKNOWN으로 남겨야 하는 경우
  2. 결과를 임의 확정하면 안 되는 경우
  3. Reconcile 대상 저장
  4. Result Probe
  5. 재조회 / 대사
  6. Compensation 연결
  7. 운영 인계
  8. Test
  9. Source / EDU

## H1-14. Messaging
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `STARTER_SELECTION`, `API`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Provider 고르기
  2. Kafka
  3. RabbitMQ
  4. JMS
  5. IBM MQ
  6. Producer / Consumer
  7. Event Schema
  8. Outbox / Inbox
  9. Duplicate / Out-of-order
  10. Retry / DLQ
  11. Replay
  12. Idempotency
  13. Transaction Boundary
  14. Test
  15. Source / EDU

## H1-15. Notification
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Notification을 사용하는 경우
  2. CpfNotificationOperations
  3. Provider / Channel
  4. Request / Result / Receipt
  5. 실패 / Retry
  6. UNKNOWN / Reconcile
  7. 운영 상태
  8. Test
  9. Source / EDU

## H1-16. Cache
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `STARTER_SELECTION`, `OPTION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Cache 방식 고르기
  2. Caffeine
  3. Redis
  4. Valkey
  5. Get / Put
  6. Evict
  7. getOrLoad
  8. TTL
  9. Invalidation
  10. Multi-instance
  11. Reconnect / Failure
  12. Stampede
  13. Serialization
  14. Provider Escape Hatch
  15. Test
  16. Source / EDU

## H1-17. File 처리
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `STARTER_SELECTION`, `API`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. File 기능 고르기
  2. Attachment Upload
  3. Download / Stream
  4. Inspection
  5. Checksum
  6. 보관 / Retention
  7. Object Storage S3
  8. 대용량 / Bulk
  9. 외부 File Transfer
  10. Archive / 압축
  11. Tabular Read / Write
  12. Partial / UNKNOWN
  13. Security / Masking
  14. Test
  15. Source / EDU

## H1-18. Security
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `STARTER_SELECTION`, `API`, `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. 보안 Profile 고르기
  2. Authentication
  3. OIDC / JWT / SSO
  4. Authorization
  5. @CpfPermission
  6. @CpfPreAuthorize
  7. Role / Permission
  8. Session JDBC / Valkey
  9. Service Identity
  10. Secret / Credential Reference
  11. Crypto / Field Encryption
  12. Digital Signature
  13. Masking / Sensitive Data Access
  14. Key / Certificate Reload
  15. Masking
  16. Test
  17. Source / EDU

## H1-19. Approval과 Audit
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Approval이 필요한 기능
  2. @CpfApprovalRequired
  3. Reason
  4. Separation of Duties
  5. Break-glass
  6. @CpfAudit
  7. Audit Event
  8. 민감정보 제외
  9. Test
  10. Source / EDU

## H1-20. Context / Header / Trace
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SYSTEM6`, `TOPOLOGY_COMPARE`
- Required Figure Preset: `OPERATION_TRACE`
- H2 순서:
  1. CpfContext
  2. Canonical System6
  3. 신뢰 경계
  4. Same JVM 전달
  5. Remote 직렬화
  6. transactionId
  7. operationId
  8. instanceId
  9. Log / Trace linkage
  10. 개발자가 직접 Header를 만들면 안 되는 경우
  11. Test
  12. Source / EDU

## H1-21. Starter와 Capability
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STARTER_SELECTION`, `SOURCE_NAV`
- Required Figure Preset: `CAPABILITY_GALLERY`
- H2 순서:
  1. Public Profile 고르기
  2. 기본 Starter
  3. Data Provider
  4. Cache Provider
  5. Messaging Provider
  6. Integration Provider
  7. File / Object Storage
  8. Security Provider
  9. BFF / Event / Batch Profile
  10. Internal Leaf를 직접 쓰지 않는 이유
  11. Native Spring Escape Hatch
  12. Source Catalog

## H1-22. Platform Operations 연계
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `STATE`, `SOURCE_NAV`
- Required Figure Preset: 없음
- H2 순서:
  1. 업무 코드에서 Platform Operations를 사용하는 범위
  2. State Operations
  3. Feature Flag
  4. Health / Dependency Health
  5. Observability API
  6. OpenAPI Snapshot / Operations
  7. Runtime Control은 업무 코드에서 직접 수행하지 않는 이유
  8. Test
  9. Source

## H1-23. Config와 Profile
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `OPTION`, `DECISION`
- Required Figure Preset: 없음
- H2 순서:
  1. Configuration 우선순위
  2. Framework Safe Default
  3. Application Property
  4. Profile / Environment
  5. Operation Policy
  6. Per-call Override
  7. Dynamic Apply
  8. Secret 분리
  9. 잘못된 Override
  10. Test / 확인

## H1-24. Generator와 Sync
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `STARTER_SELECTION`, `DB_VENDOR`
- Required Figure Preset: `GENERATOR_FLOW`
- H2 순서:
  1. Generator 입력
  2. Canonical Directory / Package
  3. Business Feature
  4. create / setup
  5. sync / diff
  6. DB Binding
  7. Provider 선택
  8. Generated Metadata 비노출
  9. User-owned Source 보호
  10. 재실행 / 멱등
  11. verify
  12. Source

## H1-25. Test
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `TEST_SCENARIO`
- Required Figure Preset: 없음
- H2 순서:
  1. Unit
  2. Contract
  3. Integration
  4. Runtime
  5. Failure / Boundary
  6. Multi-instance
  7. DB3
  8. Generated Domain
  9. OpenAPI Consumer
  10. Evidence
  11. 실패 판정

## H1-26. Batch 연계
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `API`
- Required Figure Preset: 없음
- H2 순서:
  1. On-Demand Batch
  2. 업무 Domain Online에서 Batch 요청
  3. Batch executionId
  4. 중복 실행 방지
  5. 상태 조회
  6. UNKNOWN / Reconcile
  7. Batch Developer Guide로 이동

## H1-27. 개발 완료 후 운영 인계
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`, `TEST_SCENARIO`
- Required Figure Preset: 없음
- H2 순서:
  1. 운영에 넘길 식별자
  2. Config / Secret
  3. Health / Metric / Log / Trace
  4. Error / UNKNOWN
  5. Retry / Reconcile
  6. DB / Migration
  7. Permission / Approval
  8. Runbook
  9. Test Evidence
  10. 인계 Checklist

## H1-28. 잘못된 구현과 금지 패턴
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Self-HTTP
  2. Protected Header 직접 생성
  3. Internal Starter 직접 의존
  4. 다른 Domain DB 직접 접근
  5. Remote 호출을 Local Transaction처럼 가정
  6. UNKNOWN 강제 성공/실패
  7. 무제한 Retry
  8. 민감정보 원문 Log
  9. Source에 없는 CLI/API 사용
  10. Provider 직접 결합

## H1-29. Source / EDU 길찾기
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SOURCE_NAV`
- Required Figure Preset: 없음
- H2 순서:
  1. Root별 찾는 방법
  2. Public API / SPI
  3. Starter Catalog
  4. Generated Domain
  5. EDU Online 20
  6. EDU Batch 15
  7. DB / SQL
  8. OpenAPI
  9. Test / Verification

# BATCH_DEVELOPER_GUIDE — CPF 배치 개발자 가이드

- Primary Persona: Batch Job 개발자, Batch Platform 개발자
- Entry: Batch 유형과 실행/복구 모델을 선택해 구현해야 하는 상태
- Exit: 복구 가능한 Job/Step/Partition을 개발하고 Scheduler/Worker/Center-Cut/Agent 경계를 이해하며 장애 Test와 운영 인계를 수행할 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. CPF Batch 개발 구조
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ROLE`
- Required Figure Preset: `BATCH_TOPOLOGY`
- H2 순서:
  1. Batch Runtime Owner
  2. Generated Domain batch 영역
  3. Control Plane
  4. Scheduler
  5. Worker
  6. Center-Cut
  7. Agent
  8. Local Batch Runtime과 운영 Topology 차이

## H1-2. Batch 기능 선택
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `FUNCTION_SELECTION`, `DECISION`
- Required Figure Preset: 없음
- H2 순서:
  1. Tasklet / Chunk 선택
  2. Local / Remote Partition
  3. Remote Chunk / Remote Step
  4. Scheduler
  5. On-Demand
  6. Center-Cut
  7. Agent / Worker
  8. 외부 호출 Batch
  9. 복구 요구에 따른 선택

## H1-3. Job 개발
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `API`, `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfBatchJob
  2. Job Definition
  3. Parameter
  4. Execution Identity
  5. 정상 종료
  6. 실패
  7. 재시작 가능성
  8. Test
  9. Source / EDU

## H1-4. Step 개발
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfBatchStep
  2. Step Handler
  3. Tasklet
  4. Chunk
  5. Step Transaction
  6. Execution Context
  7. 실패
  8. Test
  9. Source / EDU

## H1-5. Chunk 처리
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `OPTION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Reader / Processor / Writer
  2. Chunk Size
  3. Commit Boundary
  4. Retry / Skip
  5. Checkpoint
  6. 성능
  7. Test
  8. Source / EDU

## H1-6. Partition 처리
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `DECISION`, `API`
- Required Figure Preset: 없음
- H2 순서:
  1. Partition Key
  2. Local Partition
  3. Remote Partition
  4. Worker 분배
  5. Lease
  6. 재할당
  7. 중복 처리 방지
  8. Test
  9. Source / EDU

## H1-7. Parameter와 Execution Context
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. JobParameter
  2. 식별 Parameter
  3. 재시작 시 유지
  4. Execution Context
  5. 민감정보
  6. Version Compatibility
  7. Test

## H1-8. Batch Transaction
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `DECISION`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. Chunk Transaction
  2. REQUIRES_NEW
  3. Step별 Transaction
  4. Remote Side Effect
  5. Commit 이후 실패
  6. UNKNOWN
  7. Compensation / Reconcile
  8. Test

## H1-9. Control Plane
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ROLE`
- Required Figure Preset: `OWNERSHIP_BOUNDARY`
- H2 순서:
  1. Job Definition
  2. Execution Metadata
  3. Policy
  4. Runtime 제어
  5. Reconcile
  6. ADM 연결
  7. 하지 않는 일

## H1-10. Scheduler
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `OPTION`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. Schedule Definition
  2. Trigger
  3. Claim / Lease
  4. Misfire
  5. Business Day
  6. Dispatch
  7. 중복 Trigger 방지
  8. Test

## H1-11. Worker
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `API`, `STATE`
- Required Figure Preset: `STATE_TRANSITION`
- H2 순서:
  1. Worker Identity
  2. Job / Step / Partition 실행
  3. Heartbeat
  4. Lease / Fencing
  5. 처리량
  6. Stale Worker
  7. Process Kill
  8. Test

## H1-12. Center-Cut
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `FUNCTION_SELECTION`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상 선정
  2. Preview
  3. Claim
  4. 분할 실행
  5. Attempt
  6. 실패 대상
  7. Reconcile
  8. 부분 실패
  9. Test

## H1-13. Job Pack과 배포
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `API`, `RUNBOOK`
- Required Figure Preset: `LIFECYCLE`
- H2 순서:
  1. Job Pack이 필요한 경우
  2. Job Pack Manifest / Catalog
  3. Artifact Digest / Signature
  4. Deployment Plan
  5. Cell / Target
  6. Approved Launch
  7. 배포 실패 / Recovery
  8. Rollback
  9. Audit
  10. Test
  11. Source

## H1-14. Agent
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 설치 / Artifact
  2. Start / Stop / Restart
  3. Rollback
  4. Drain / Resume
  5. Command Ledger
  6. 승인 / Audit
  7. 실패
  8. Test

## H1-15. Lease / Fencing / Heartbeat
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STATE`, `TEST_SCENARIO`
- Required Figure Preset: `STATE_TRANSITION`
- H2 순서:
  1. Lease 취득
  2. Heartbeat 갱신
  3. Fencing Token
  4. 만료
  5. Stale Worker 차단
  6. 재할당
  7. 동시성 Test

## H1-16. Restart / Rerun / Reprocess / Reconcile
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RESTART_RERUN_REPROCESS_RECONCILE`, `DECISION`
- Required Figure Preset: `BATCH_RECOVERY`
- H2 순서:
  1. 네 방식 먼저 비교
  2. Restart
  3. Rerun
  4. Reprocess
  5. Reconcile
  6. Checkpoint / 기존 Execution 관계
  7. 중복 Side Effect 위험
  8. 선택 Test

## H1-17. Idempotency
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `FUNCTION_SELECTION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Job 중복 실행
  2. Step / Item 중복
  3. On-Demand 중복 요청
  4. External Side Effect
  5. Key 설계
  6. Conflict
  7. Test

## H1-18. 외부 Side Effect
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `ERROR`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. Timeout
  2. Retryability
  3. Commit Boundary
  4. UNKNOWN
  5. Idempotency
  6. Reconcile
  7. 부분 전송
  8. Test

## H1-19. UNKNOWN
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STATE`, `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. UNKNOWN 발생 조건
  2. Owner
  3. 대상 저장
  4. Probe
  5. Reconcile
  6. 운영 인계
  7. Test

## H1-20. Process Kill과 장애 복구
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`, `TEST_SCENARIO`
- Required Figure Preset: 없음
- H2 순서:
  1. Kill 시점별 영향
  2. Heartbeat / Lease
  3. Checkpoint
  4. 재할당
  5. Fencing
  6. 중복 방지
  7. 정상화
  8. Fault Injection Test

## H1-21. 대량 처리와 성능
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DECISION`, `TEST_SCENARIO`
- Required Figure Preset: 없음
- H2 순서:
  1. Chunk Size
  2. Partition 수
  3. Worker 수
  4. Backpressure
  5. DB Lock
  6. Batch Claim
  7. Progress
  8. Capacity Baseline
  9. 성능 Test

## H1-22. Test
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `TEST_SCENARIO`
- Required Figure Preset: 없음
- H2 순서:
  1. Job Test
  2. Step Test
  3. Partition Test
  4. Scheduler Test
  5. Worker Kill
  6. Lease / Fencing
  7. External UNKNOWN
  8. Restart / Reprocess
  9. DB3
  10. Runtime Evidence

## H1-23. 운영 인계
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Job Definition
  2. Schedule
  3. Parameter
  4. Retry/Skip
  5. Restart 정책
  6. Reprocess 기준
  7. Reconcile
  8. Permission / Approval
  9. Metric / Alert
  10. Runbook

## H1-24. 잘못된 구현과 금지 패턴
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 업무 Job을 Control Plane에 구현
  2. Scheduler가 Job 업무를 수행
  3. Lease 없이 다중 Worker
  4. UNKNOWN 강제 확정
  5. Restart와 Reprocess 혼용
  6. Process Kill 후 중복 처리 방치
  7. 무한 Retry
  8. Audit 없는 위험 조치

## H1-25. Source 길찾기
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SOURCE_NAV`
- Required Figure Preset: 없음
- H2 순서:
  1. cpf-batch/api
  2. runtime / runtime-support
  3. control-plane
  4. scheduler
  5. worker
  6. center-cut
  7. agent
  8. testkit
  9. Generated Domain batch
  10. EDU Batch 15

# OPERATOR_MANUAL — CPF 운영자 매뉴얼

- Primary Persona: Application 운영자, Platform 운영자
- Entry: 장애 신고 또는 운영 변경 요청을 받은 상태
- Exit: 현재 상태를 식별하고 권한 범위의 안전한 조치를 수행한 뒤 정상화/에스컬레이션을 판정할 수 있다.
- Orientation: portrait
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. 운영자가 먼저 확인할 것
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 권한과 접근
  2. 현재 Environment / System
  3. 시간 범위
  4. transactionId / operationId / instanceId 확보
  5. 변경 전 Evidence 확보
  6. 위험 조치 전 확인

## H1-2. 거래 찾기
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 거래 검색 키
  2. 시간 / System / 상태 Filter
  3. Paging / Detail
  4. 검색 결과가 없을 때
  5. 관련 Trace / Log 연결

## H1-3. transactionId로 전체 흐름 추적
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `OPERATION_TRACE`
- H2 순서:
  1. 거래 시작점
  2. Hop
  3. Log
  4. Trace
  5. Timeline
  6. 실패 구간
  7. 관련 operationId / instanceId

## H1-4. operationId로 처리 기능 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. Canonical Operation
  2. Handler
  3. Policy
  4. Caller 허용
  5. Discovery 상태
  6. NOT_DISCOVERED / INACTIVE_CANDIDATE
  7. OpenAPI 연결

## H1-5. instanceId로 처리 Runtime 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. SystemCode와 구분
  2. Host / Application
  3. 중복 Instance
  4. Health
  5. Build SHA
  6. Draining / Maintenance
  7. 재기동 판단

## H1-6. Log 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 검색 기준
  2. Transaction / Operation / Instance correlation
  3. Error Log
  4. 민감정보 Masking
  5. Remote Log / Export
  6. Log 누락

## H1-7. Trace / Timeline 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Trace 시작
  2. Span / Hop
  3. Timeline Event
  4. 외부 호출
  5. Retry Attempt
  6. UNKNOWN
  7. Audit 연결

## H1-8. Health 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Application Health
  2. Dependency Health
  3. Registry Health
  4. DB / Messaging / External
  5. Multi-instance
  6. READY 불가 조건

## H1-9. Service Registry 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. System / Service
  2. Endpoint
  3. Version / Zone / Weight
  4. Health / Maintenance / Draining
  5. TTL / Stale
  6. 중복 instanceId
  7. 정상화

## H1-10. Config / Policy 변경
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`, `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. 현재 Effective Config
  2. 변경 가능 여부
  3. Secret 여부
  4. Approval
  5. 적용
  6. Dynamic / Restart 필요
  7. Rollback
  8. Audit
  9. 정상화

## H1-11. Feature Flag 운영
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. Flag 상태
  2. Scope
  3. 변경 사유
  4. Approval
  5. 적용
  6. Rollback
  7. Audit
  8. 정상화

## H1-12. Dynamic Log Level 운영
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 현재 Level
  2. 변경 대상
  3. 유효 시간
  4. 민감정보 위험
  5. 적용
  6. 원복
  7. Audit

## H1-13. Cache 운영
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Provider / Instance
  2. Health / Metrics
  3. Invalidation
  4. Refresh
  5. Multi-instance 정합성
  6. Failure / Reconnect
  7. 정상화

## H1-14. Messaging 운영
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Broker / Consumer
  2. Lag / Backlog
  3. Retry
  4. DLQ
  5. Replay
  6. Outbox / Inbox
  7. Schema
  8. 정상화

## H1-15. Notification 운영
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Provider 상태
  2. 요청 / Receipt
  3. 실패
  4. Retry
  5. UNKNOWN / Reconcile
  6. 정상화

## H1-16. Security / Session / Permission 운영
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Authentication
  2. Session
  3. Permission
  4. Role
  5. Masking
  6. Secret / Certificate
  7. 위험 변경
  8. Audit
  9. 정상화

## H1-17. Incident Lifecycle
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STATE`, `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Incident 생성
  2. 영향 범위
  3. Owner
  4. 상태 전이
  5. Recovery Action
  6. Evidence
  7. 종료 기준

## H1-18. Integration Closure / Recovery
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. 외부 연계 실패
  2. Result Probe
  3. UNKNOWN
  4. Reconcile
  5. Closure 상태
  6. Audit
  7. 정상화

## H1-19. Runtime Control
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상 Runtime
  2. Start / Stop / Restart
  3. Drain / Resume
  4. 사전 조건
  5. Approval
  6. 실행
  7. Result Tracking
  8. Audit
  9. 정상화

## H1-20. 거래 상태 판단
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STATE`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. SUCCESS
  2. BUSINESS_FAILURE
  3. TECHNICAL_FAILURE
  4. UNKNOWN
  5. 부분 실패
  6. 상태를 바꾸면 안 되는 경우

## H1-21. Business Failure
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. 업무 오류 식별
  2. 재시도 여부
  3. 사용자/업무 조치
  4. 데이터 확인
  5. Escalation

## H1-22. Technical Failure
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. 기술 오류 식별
  2. Dependency
  3. Retryability
  4. Circuit / Timeout
  5. 정상화
  6. Escalation

## H1-23. UNKNOWN
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STATE`, `RUNBOOK`
- Required Figure Preset: `STATE_TRANSITION`
- H2 순서:
  1. UNKNOWN 확인
  2. 대상 Owner
  3. 재호출 금지 여부
  4. Probe / Reconcile
  5. 운영자 임의 확정 금지
  6. 정상화

## H1-24. Retry
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: 없음
- H2 순서:
  1. Retry 가능한 오류
  2. Retry 불가 오류
  3. Attempt
  4. Backoff
  5. Idempotency
  6. 중단 기준
  7. 결과 확인

## H1-25. Reconcile
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상 조회
  2. 사전 확인
  3. 실행
  4. 외부 결과 비교
  5. 상태 확정
  6. 부분 실패
  7. Audit
  8. 정상화

## H1-26. 위험 운영 조치
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 위험 조치 목록
  2. Permission
  3. Reason
  4. Approval
  5. Break-glass
  6. Dry-run / Preview가 있는 경우
  7. 실행 후 결과

## H1-27. 승인 / 사유 / Audit
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Approval 상태
  2. Reason 작성 기준
  3. SoD
  4. Audit 검색
  5. 변경 전후 값
  6. 실패한 승인 / 조치

## H1-28. Gateway 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `HTTP_STATUS`, `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Gateway Health
  2. Route
  3. Backend
  4. Auth
  5. Rate Limit
  6. Timeout
  7. 503 / 504
  8. Direct path를 자동 Fallback으로 쓰지 않는 이유
  9. 정상화

## H1-29. Batch 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`, `RESTART_RERUN_REPROCESS_RECONCILE`
- Required Figure Preset: 없음
- H2 순서:
  1. Execution
  2. Scheduler
  3. Worker
  4. Lease
  5. Center-Cut
  6. UNKNOWN
  7. Restart / Reprocess 선택
  8. Batch 운영 가이드로 이동

## H1-30. 외부기관 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ERROR`, `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Timeout
  2. Connection
  3. 인증서 / Credential
  4. 부분 송수신
  5. Retryability
  6. UNKNOWN
  7. Reconcile
  8. Masking

## H1-31. DB 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Connection
  2. Schema mismatch
  3. Lock
  4. Slow Query
  5. Migration 상태
  6. Failover 정책
  7. 데이터 임의 수정 금지
  8. 정상화

## H1-32. HTTP 상태별 대응
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `HTTP_STATUS`
- Required Figure Preset: 없음
- H2 순서:
  1. 401
  2. 403
  3. 404
  4. 409
  5. 429
  6. 500
  7. 503

## H1-33. Runtime Instance 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Health / Heartbeat
  2. 중복 instanceId
  3. Draining
  4. Process Kill
  5. 재기동
  6. Registry stale
  7. 정상화

## H1-34. 정상화 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 거래 정상화
  2. Dependency 정상화
  3. Backlog / UNKNOWN 잔여
  4. Metric / Alert
  5. Audit
  6. 사용자 영향 종료

## H1-35. Escalation
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 개발자 Escalation
  2. DBA / Infra
  3. Security
  4. 외부기관
  5. 반드시 전달할 식별자와 Evidence

## H1-36. 운영 Runbook 모음
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 거래 실패
  2. UNKNOWN
  3. Gateway
  4. Batch
  5. DB
  6. Security
  7. Config 변경
  8. Runtime 장애

# BATCH_OPERATOR_GUIDE — CPF 배치 운영 가이드

- Primary Persona: Batch 운영자
- Entry: Batch 실행 이상 또는 복구 요청을 받은 상태
- Exit: Restart/Rerun/Reprocess/Reconcile을 구분하고 Worker/Scheduler/Center-Cut/Agent 장애를 안전하게 복구·판정할 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. Batch 운영 Topology
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ROLE`
- Required Figure Preset: `BATCH_TOPOLOGY`
- H2 순서:
  1. Control Plane
  2. Scheduler
  3. Worker
  4. Center-Cut
  5. Agent
  6. DB / Registry
  7. ADM

## H1-2. Execution 상태 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. Execution ID
  2. Job / Step
  3. 상태
  4. 시작 / 종료
  5. 처리 건수
  6. Retry / Skip
  7. Partition
  8. 오류

## H1-3. Control Plane 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Job Definition
  2. Execution Metadata
  3. Policy
  4. Runtime 제어
  5. Reconcile 상태

## H1-4. Scheduler 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Trigger
  2. Schedule
  3. Claim / Lease
  4. Misfire
  5. Leader
  6. Dispatch

## H1-5. Worker 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Worker ID
  2. Heartbeat
  3. Lease
  4. Fencing
  5. Processing
  6. Stale
  7. Draining

## H1-6. Center-Cut 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상
  2. Preview
  3. Claim
  4. Attempt
  5. 부분 실패
  6. Reconcile

## H1-7. Agent 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Host
  2. Artifact
  3. Command Ledger
  4. Start / Stop
  5. Rollback
  6. Drain / Resume

## H1-8. Lease / Fencing / Heartbeat
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STATE`, `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Lease 상태
  2. 만료
  3. Fencing mismatch
  4. Heartbeat 지연
  5. Stale Worker
  6. 재할당

## H1-9. 복구 방법 선택
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RESTART_RERUN_REPROCESS_RECONCILE`, `DECISION`
- Required Figure Preset: `BATCH_RECOVERY`
- H2 순서:
  1. Restart / Rerun / Reprocess / Reconcile 한눈에 비교
  2. 선택 전 공통 확인
  3. 중복 Side Effect 여부
  4. Checkpoint / 기존 Execution
  5. UNKNOWN 여부

## H1-10. Restart
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Restart 가능한 상태
  2. Checkpoint
  3. 완료 Step
  4. 기존 Parameter
  5. 중복 위험
  6. 실행
  7. 정상화

## H1-11. Rerun
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 새 실행이 필요한 경우
  2. Parameter
  3. 기존 결과
  4. 중복 위험
  5. 실행
  6. 정상화

## H1-12. Reprocess
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 재처리 대상 선정
  2. 단건 / 다건
  3. 대상 격리
  4. Side Effect
  5. 승인
  6. 실행
  7. 정상화

## H1-13. Reconcile
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상
  2. 외부/내부 결과 조회
  3. 상태 확정
  4. 실패 대상
  5. 재시도
  6. Audit

## H1-14. Process Kill
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Kill 감지
  2. Heartbeat
  3. Lease 만료
  4. Fencing
  5. 재할당
  6. 중복 방지
  7. 정상화

## H1-15. Worker 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 단일 Worker
  2. 다중 Worker
  3. Stale
  4. Drain
  5. 재할당
  6. 정상화

## H1-16. Scheduler 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Leader / Lease
  2. Trigger 누락
  3. Misfire
  4. 중복 Dispatch
  5. 복구
  6. 정상화

## H1-17. 부분 실패
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 실패 범위
  2. 완료 대상
  3. 실패 대상
  4. 재처리 범위
  5. Idempotency
  6. 정상화

## H1-18. 대량 처리 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 진행률
  2. 처리량
  3. DB / Lock
  4. Partition
  5. Backlog
  6. Throttling
  7. 복구

## H1-19. 외부 Side Effect / UNKNOWN
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. 외부 결과 불명
  2. Retry 금지/허용
  3. Probe
  4. Reconcile
  5. Compensation
  6. 정상화

## H1-20. Drain / Resume
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Drain 전 조건
  2. 신규 작업 차단
  3. 진행 작업
  4. 완료 확인
  5. Resume
  6. Audit

## H1-21. Rollback
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Artifact / 배포 상태
  2. Rollback 조건
  3. DB 영향
  4. 진행 Execution
  5. 실행
  6. 확인
  7. Audit

## H1-22. 안전한 재처리
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상 Snapshot
  2. 중복 검증
  3. 승인
  4. Preview
  5. 실행
  6. 결과 비교
  7. 정상화

## H1-23. 정상화 판정
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Execution
  2. Scheduler
  3. Worker
  4. Lease
  5. Backlog
  6. UNKNOWN
  7. Audit

## H1-24. Batch 장애 Decision Matrix
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: 없음
- H2 순서:
  1. 실패 유형별 첫 확인
  2. 권장 조치
  3. 금지 조치
  4. Escalation

# GATEWAY_GUIDE — CPF Gateway 개발/사용 가이드

- Primary Persona: Gateway 개발자, Platform 운영자, Architect
- Entry: Gateway를 사용할지 결정하거나 Route/Policy를 개발·운영해야 하는 상태
- Exit: Gateway Optionality를 이해하고 Route/Security/Resilience를 구성하며 장애와 Rollback을 판단할 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. Gateway 사용 여부 결정
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `GATEWAY_OPTIONALITY`
- H2 순서:
  1. Gateway를 사용하는 경우
  2. Gateway 없이 허용된 Direct Public HTTP를 사용하는 경우
  3. 자동 Direct Fallback이 아닌 이유
  4. 보안 계약은 동일하게 적용
  5. 선택 Checklist

## H1-2. Gateway Topology
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ROLE`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. External Trust Boundary
  2. Route
  3. Backend Domain
  4. Registry
  5. ADM / Control
  6. Multi-instance

## H1-3. Routing
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. Route Source
  2. Path / Method
  3. Target
  4. Version
  5. Priority
  6. 변경 반영
  7. 검증

## H1-4. Domain Route
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. Domain / operationId
  2. Target System
  3. Registry Metadata
  4. Health / Maintenance / Draining
  5. 잘못된 Target

## H1-5. Canonical Context / Header
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SYSTEM6`
- Required Figure Preset: `REQUEST_FLOW`
- H2 순서:
  1. Untrusted Header 제거
  2. Trusted Identity
  3. System6 구성
  4. Target 검증
  5. Management API 제외

## H1-6. Authentication
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `API`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Client Identity
  2. OIDC/JWT/Service Identity
  3. 실패
  4. Audit
  5. Test

## H1-7. Authorization
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `API`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Permission
  2. Caller Policy
  3. Operation Policy
  4. 403
  5. Audit
  6. Test

## H1-8. Timeout
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `OPTION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Connect / Read / Overall Deadline
  2. Budget
  3. Backend 영향
  4. 504
  5. Test

## H1-9. Retry
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DECISION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Retryable 오류
  2. Non-retryable
  3. Backoff
  4. Idempotency
  5. UNKNOWN
  6. Test

## H1-10. Rate Limit
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `OPTION`, `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. 정책
  2. Key
  3. 429
  4. Multi-instance Counter
  5. 운영 확인
  6. Test

## H1-11. Error Mapping
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `ERROR`
- Required Figure Preset: 없음
- H2 순서:
  1. Backend 오류
  2. Gateway 오류
  3. CPF Result
  4. 민감정보
  5. Trace linkage

## H1-12. HTTP 상태 처리
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `HTTP_STATUS`
- Required Figure Preset: 없음
- H2 순서:
  1. 401
  2. 403
  3. 404
  4. 409
  5. 429
  6. 500
  7. 503

## H1-13. Health
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. Gateway Instance
  2. Route Backend
  3. Registry
  4. Dependency
  5. Draining

## H1-14. HA
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: 없음
- H2 순서:
  1. Multi-instance
  2. Load Balance
  3. Shared State
  4. Failure Domain
  5. Failover

## H1-15. Canary
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상 Version
  2. Weight
  3. Health
  4. 전환
  5. Rollback 기준

## H1-16. Rollback
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 변경 전 Snapshot
  2. Route / Policy
  3. 실행
  4. 검증
  5. Audit

## H1-17. Gateway 장애
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `HTTP_STATUS`, `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 접속 불가
  2. 일부 Route 404
  3. 401/403
  4. 429
  5. 500/503
  6. Timeout
  7. 특정 Instance 오류
  8. 정상화

## H1-18. Gateway 없이 배포
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `TOPOLOGY_COMPARE`
- Required Figure Preset: `GATEWAY_OPTIONALITY`
- H2 순서:
  1. 허용 Direct Path
  2. 동일 AuthN/AuthZ
  3. System6
  4. Audit
  5. 배포/운영 정책
  6. 금지 자동 Fallback

## H1-19. 개발 예
- Content Model: `DEVELOPER_CAPABILITY_CHAPTER`
- Max Code: `L3`
- Required Table Preset: `OPTION`
- Required Figure Preset: 없음
- H2 순서:
  1. Route 정의
  2. Security
  3. Timeout / Retry
  4. Error Mapping
  5. Test

## H1-20. 운영 확인
- Content Model: `OPERATOR_RUNBOOK_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `RUNBOOK`
- Required Figure Preset: 없음
- H2 순서:
  1. 배포 후
  2. Route 변경 후
  3. Policy 변경 후
  4. Instance 추가/제거
  5. 정상화

## H1-21. Source / API 길찾기
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SOURCE_NAV`
- Required Figure Preset: 없음
- H2 순서:
  1. Gateway Runtime
  2. Route
  3. Policy
  4. Registry
  5. Security
  6. Rate Limit
  7. Audit
  8. ADM
  9. Test

# SPECIFICATION — CPF Specification 기술 명세

- Primary Persona: Framework 개발자, 통합 개발자, Architect, 검수자
- Entry: 정확한 Public Contract/상태/옵션/오류 계약을 확인해야 하는 상태
- Exit: 특정 API/Annotation/Config/State의 Owner, 입력, 출력, 실패 계약, Consumer, Source를 정확히 찾을 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. Module / Public Boundary
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Public API
  2. SPI
  3. Internal
  4. Artifact Visibility
  5. Consumer 계약

## H1-2. Base Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. CpfRequest / CpfResponse
  2. CpfResult
  3. Version
  4. Base API

## H1-3. Context Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. CpfContext
  2. Context Snapshot
  3. Propagation
  4. Trust Boundary

## H1-4. Canonical Header Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SYSTEM6`, `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. System6
  2. 생성
  3. 검증
  4. Hop 변경
  5. 적용/제외 경계

## H1-5. Runtime Instance Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. instanceId 결정
  2. 금지값
  3. 중복
  4. 관측 필드

## H1-6. Operation ID Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfOnlineTransaction
  2. OpenAPI operationId
  3. Registry
  4. Policy
  5. Discovery

## H1-7. Result Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Success / Failure / UNKNOWN
  2. Recovery Info
  3. Status

## H1-8. Error Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Business
  2. System
  3. Validation
  4. External
  5. Catalog / Message

## H1-9. CPF Framework Common Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Code
  2. Parameter
  3. Message / Error Catalog
  4. Calendar
  5. Template
  6. Management / Change Propagation

## H1-10. Persistence Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Repository
  2. JDBC / MyBatis / JPA
  3. Paging / Cursor
  4. Lock

## H1-11. Data Quality Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Rule
  2. Decision
  3. Correction Port
  4. Audit / Runtime impact

## H1-12. Transaction Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Local
  2. REQUIRES_NEW
  3. Saga
  4. TCC
  5. XA
  6. Outcome / Recovery

## H1-13. Domain Invocation Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. CpfDomainClient
  2. Binding
  3. Router
  4. Local / Remote
  5. Call Options

## H1-14. External Integration Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfClient
  2. Timeout
  3. Retry
  4. Fixed-Length
  5. Webhook / File
  6. UNKNOWN

## H1-15. AI Integration Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. CpfAiOperations
  2. Provider
  3. Request / Response
  4. Policy / Risk
  5. Usage / Telemetry
  6. UNKNOWN

## H1-16. Messaging Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Producer / Consumer
  2. Schema
  3. Outbox / Inbox
  4. DLQ / Replay
  5. Reliability

## H1-17. Notification Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Operations
  2. Request
  3. Result / Receipt
  4. Provider
  5. Reconciler
  6. Runtime status

## H1-18. Cache Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Cache API
  2. Options
  3. Invalidation
  4. Health / Metrics
  5. Multi-instance

## H1-19. File Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Attachment
  2. Object Storage
  3. Inspection
  4. Checksum
  5. Transfer / UNKNOWN

## H1-20. Archive / Tabular Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Archive
  2. Checksum
  3. Extract
  4. Tabular Read
  5. Tabular Write
  6. Error / Security

## H1-21. Security Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Authentication
  2. Permission
  3. PreAuthorize
  4. Session
  5. Crypto / Encryption
  6. Secret Reference

## H1-22. Approval / Audit Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Approval Required
  2. Reason
  3. Audit Event
  4. Break-glass

## H1-23. Idempotency Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfIdempotent
  2. Key
  3. Conflict
  4. Durability

## H1-24. Reconcile Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. UNKNOWN owner
  2. Probe
  3. Reconciler
  4. State transition

## H1-25. Async / Workflow Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Async Operations
  2. Async State
  3. Workflow
  4. Context propagation

## H1-26. Batch Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Job / Step
  2. Execution
  3. Scheduler
  4. Worker
  5. Center-Cut
  6. Agent
  7. Lease / Fencing

## H1-27. Gateway Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Route
  2. Registry
  3. Entry Policy
  4. Rate Limit
  5. Audit
  6. Health

## H1-28. DB Lifecycle Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Vendor
  2. Schema
  3. Migration
  4. Seed
  5. Upgrade
  6. Rollback / Recovery

## H1-29. Platform State / Feature Flag Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`, `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. State identifiers
  2. State transition
  3. Feature Flag
  4. Audit / Store
  5. Runtime status

## H1-30. Runtime / Topology Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Same JVM
  2. Remote
  3. Multi-instance
  4. Registry
  5. Health

## H1-31. Operations Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. ADM
  2. Health
  3. Log / Trace
  4. Runtime Control
  5. Incident / Recovery

## H1-32. OpenAPI Contract
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. OpenAPI Snapshot
  2. Operations
  3. operationId
  4. Frontend generated client
  5. Route coverage

## H1-33. Public API Reference
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. API index
  2. Annotation index
  3. Configuration index
  4. Command index

## H1-34. State Transition
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STATE`
- Required Figure Preset: 없음
- H2 순서:
  1. 거래
  2. Async
  3. Batch
  4. Gateway/Operations relevant states

## H1-35. Compatibility
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Java / Spring
  2. Artifact
  3. Provider
  4. DB
  5. Mixed Version

## H1-36. Contract Acceptance
- Content Model: `SPEC_CONTRACT_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Consumer
  2. Error / Boundary
  3. Runtime
  4. Evidence

# ARCHITECTURE_DESIGN — CPF 아키텍처설계서

- Primary Persona: Architect, Tech Lead, Framework Maintainer
- Entry: Owner/Dependency/Topology/Boundary를 설계하거나 검토하는 상태
- Exit: 기능 배치 위치와 허용 Dependency, Topology 선택, Failure Domain을 문서만으로 판단할 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. CPF Architecture 개요
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. 제품 경계
  2. 주요 Runtime
  3. 업무 Domain
  4. Public Surface

## H1-2. Architecture 원칙
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Owner 단일성
  2. Dependency 방향
  3. Public / Internal
  4. Native Escape Hatch

## H1-3. Module Ownership
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `OWNER_BOUNDARY`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. cpf-core
  2. cpf-common
  3. cpf-admin
  4. cpf-backoffice
  5. cpf-backoffice-web
  6. cpf-batch
  7. cpf-gateway
  8. cpf-starters
  9. cpf-tools
  10. Generated Domain

## H1-4. Dependency 방향
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. 허용 방향
  2. 금지 역방향
  3. Internal 참조 금지
  4. Consumer 방향

## H1-5. 전체 Runtime Topology
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. 외부 진입
  2. 업무 Domain
  3. 공통 Runtime
  4. Batch
  5. Admin
  6. DB / External

## H1-6. Same JVM Topology
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. CpfContext
  2. 내부 호출
  3. Self-HTTP 금지
  4. Remote와 동일한 정책

## H1-7. Remote / MSA Topology
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Registry / Routing
  2. System6
  3. Timeout / Retry
  4. UNKNOWN / Reconcile

## H1-8. Domain Invocation
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Local / Remote Router
  2. Operation
  3. Binding
  4. Failure Semantics

## H1-9. 거래 Context Boundary
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Trusted Entry
  2. System6
  3. Protected Header
  4. Management API 제외

## H1-10. Gateway
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Optionality
  2. Trust Boundary
  3. Routing
  4. Direct 허용 경로
  5. 금지 자동 Fallback

## H1-11. Backoffice
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. cpf-backoffice Owner
  2. 다른 Domain DB 직접 접근 금지
  3. cpf-backoffice-web BFF
  4. Optionality

## H1-12. Batch Architecture
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. api/runtime/runtime-support/testkit
  2. Control Plane
  3. Scheduler
  4. Worker
  5. Center-Cut
  6. Agent

## H1-13. Persistence / DB Ownership
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `OWNER_BOUNDARY`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Business DB
  2. Platform DB
  3. Data Ownership
  4. DB3 Binding

## H1-14. Security Boundary
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. AuthN
  2. AuthZ
  3. Secret
  4. Approval / Audit
  5. Masking

## H1-15. Observability
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. transactionId
  2. operationId
  3. instanceId
  4. Log / Metric / Trace / Timeline

## H1-16. Multi-instance
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. instance identity
  2. registry
  3. lease
  4. shared state
  5. duplicate prevention

## H1-17. HA / Failure Domain
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Gateway
  2. Domain
  3. DB
  4. Messaging
  5. Batch
  6. Admin

## H1-18. Deployment Topology
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Modular Monolith
  2. 분리 WAS
  3. MSA
  4. Multi-WAS
  5. 선택 기준

## H1-19. 금지 Architecture
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: 없음
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. Self-HTTP
  2. Cross-domain DB
  3. Core Pollution
  4. Internal Dependency
  5. Automatic Gateway fallback
  6. Hidden Owner

## H1-20. Topology Decision Matrix
- Content Model: `ARCHITECTURE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DECISION`
- Required Figure Preset: `ARCHITECTURE_TOPOLOGY`
- H2 순서:
  1. 배포 선택 기준
  2. 운영 복잡도
  3. 장애 격리
  4. Latency
  5. 확장
  6. 필수 Runtime

# TECHNICAL_SPECIFICATION — CPF 기술사양서

- Primary Persona: Architect, Framework Maintainer, 인프라/배포 담당자
- Entry: 지원 기술/버전/구성/호환성을 확인해야 하는 상태
- Exit: 환경이 CPF 지원 사양인지, 어떤 Profile/Provider/DB/Build 조건을 만족해야 하는지 판정할 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. 기술 기준과 적용 범위
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. 제품 기준
  2. 지원 범위
  3. 정본
  4. 사양 확인 방법

## H1-2. Runtime Baseline
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Java 25
  2. Process / JVM
  3. Container
  4. OS / Shell 전제

## H1-3. Java
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Version
  2. Compiler
  3. Encoding
  4. Javadoc

## H1-4. Spring / Spring Boot
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Spring Boot
  2. Spring Framework
  3. Spring Cloud
  4. Spring Batch

## H1-5. Build / Artifact
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Gradle
  2. BOM
  3. Artifact Naming
  4. Publication

## H1-6. Starter
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STARTER_SELECTION`
- Required Figure Preset: 없음
- H2 순서:
  1. Base Starter
  2. Capability Group
  3. Public / Internal

## H1-7. Profile
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STARTER_SELECTION`
- Required Figure Preset: 없음
- H2 순서:
  1. web-api
  2. secure-api
  3. bff
  4. event
  5. batch

## H1-8. Provider
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STARTER_SELECTION`
- Required Figure Preset: 없음
- H2 순서:
  1. Data
  2. Cache
  3. Messaging
  4. Integration
  5. File
  6. Security
  7. Lock / Object Storage

## H1-9. Configuration
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Precedence
  2. Schema
  3. Default
  4. Dynamic
  5. Secret

## H1-10. CPF Framework Common
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Code / Parameter / Message
  2. Calendar / Template
  3. Persistence / Cache backing
  4. Management

## H1-11. Transaction
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Local
  2. REQUIRES_NEW
  3. TCC
  4. XA
  5. Recovery

## H1-12. Persistence
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. JDBC
  2. MyBatis
  3. JPA
  4. Cursor / Paging
  5. Lock
  6. Data Quality

## H1-13. Integration
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. HTTP
  2. Resilience
  3. Fixed-Length
  4. GraphQL
  5. Realtime
  6. AI
  7. Webhook

## H1-14. Messaging
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Kafka
  2. RabbitMQ
  3. JMS
  4. IBM MQ
  5. Reliability

## H1-15. Notification
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. API
  2. Provider Runtime
  3. Status / Reconcile
  4. Observability

## H1-16. File
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Attachment
  2. S3
  3. Transfer
  4. Inspection
  5. Archive
  6. Tabular

## H1-17. Security
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. OIDC
  2. Session
  3. Permission
  4. Crypto
  5. Audit
  6. Digital Signature
  7. Masking / Sensitive Data
  8. Secret / Certificate Reload

## H1-18. Observability
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Actuator
  2. Micrometer
  3. OpenTelemetry
  4. CPF correlation

## H1-19. Platform Operations / ADM
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. State / Feature Flag
  2. Runtime Control
  3. Health
  4. Config / Secret
  5. Incident / Recovery
  6. Gateway / Batch control

## H1-20. Batch
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Spring Batch
  2. db-scheduler
  3. Runtime roles
  4. Remote execution

## H1-21. Gateway
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Spring Cloud Gateway
  2. Route
  3. Rate
  4. Health

## H1-22. OpenAPI / Frontend Client
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. OpenAPI operationId
  2. Generated Client
  3. Route Coverage
  4. Auth / Error / Paging / File / Async
  5. Accessibility / Responsive

## H1-23. Database
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DB_VENDOR`
- Required Figure Preset: 없음
- H2 순서:
  1. Oracle
  2. PostgreSQL
  3. MariaDB
  4. Lifecycle

## H1-24. Generator
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Domain create/setup/sync
  2. Catalog
  3. DB renderer
  4. Verification

## H1-25. Generated Domain
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Directory IA
  2. Package IA
  3. Business Feature
  4. Dependency

## H1-26. Performance / Capacity
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Baseline 항목
  2. Latency / Throughput
  3. Batch capacity
  4. DB / Cache / Messaging

## H1-27. Compatibility
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Java / Spring
  2. DB
  3. Provider
  4. Mixed Version

## H1-28. Build / Test
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Build
  2. Unit
  3. Integration
  4. Runtime
  5. DB3
  6. Failure

## H1-29. Release Acceptance
- Content Model: `TECH_SPEC_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `SPEC_CONTRACT`
- Required Figure Preset: 없음
- H2 순서:
  1. Artifact
  2. SBOM / License
  3. Secret / Vulnerability
  4. Fresh Consumer
  5. Evidence

# TECHNICAL_STANDARD — CPF 기술표준서

- Primary Persona: CPF 개발자, Code Reviewer, Framework Maintainer
- Entry: 구현 또는 PR이 CPF 표준을 준수하는지 판단해야 하는 상태
- Exit: 각 규칙을 필수/금지/권장/허용으로 판정하고 올바른/잘못된 구현과 검사 방법을 확인할 수 있다.
- Orientation: portrait
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. 표준 적용 범위
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 대상 Source
  2. 적용 우선순위
  3. 예외 승인

## H1-2. Naming
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Module
  2. Package
  3. Class
  4. Method
  5. Property
  6. operationId

## H1-3. Directory
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Repository Root
  2. Generated Domain
  3. Runtime
  4. Docs / Evidence

## H1-4. Java Package
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Domain Base
  2. Business Feature
  3. Technical Role
  4. Internal

## H1-5. Module Ownership
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Owner
  2. 소유 가능
  3. 소유 금지
  4. Consumer

## H1-6. Dependency
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 허용 Dependency
  2. 금지 Dependency
  3. Internal
  4. Cycle

## H1-7. Public / Internal API
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Public API
  2. SPI
  3. Internal
  4. Compatibility

## H1-8. Controller
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfController
  2. @CpfOnlineTransaction
  3. Validation
  4. System6

## H1-9. Service
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfService
  2. Transaction
  3. Domain Invocation

## H1-10. Repository
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. @CpfRepository
  2. Data Access
  3. Lock
  4. DB Owner
  5. Data Quality
  6. Cursor / Paging
  7. Bulk

## H1-11. Header / Context
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. System6
  2. Trust
  3. Propagation
  4. Management API 제외

## H1-12. Transaction
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Local
  2. Remote Side Effect
  3. UNKNOWN
  4. Saga/TCC/XA

## H1-13. Error
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Business
  2. Technical
  3. Validation
  4. External
  5. Mapping

## H1-14. Retry
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Retryable
  2. Backoff
  3. Idempotency
  4. Deadline
  5. UNKNOWN

## H1-15. Idempotency
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Key
  2. Durability
  3. Conflict
  4. Concurrent request

## H1-16. Security
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Authentication
  2. Authorization
  3. Permission
  4. Secret
  5. Masking
  6. Crypto / Encryption
  7. Digital Signature
  8. Masking / Sensitive Data
  9. Secret / Certificate

## H1-17. Audit
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Reason
  2. Approval
  3. SoD
  4. Break-glass
  5. Tamper Evidence

## H1-18. Logging / Trace
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Correlation
  2. Structured fields
  3. Sensitive data
  4. instanceId
  5. Observability / Timeline

## H1-19. Batch
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Job/Step
  2. Scheduler/Worker
  3. Lease/Fencing
  4. Recovery

## H1-20. Database
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. DB3
  2. Naming
  3. Migration
  4. Seed
  5. Upgrade
  6. Rollback

## H1-21. Generator
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Canonical Input
  2. Catalog
  3. User-owned Source
  4. Sync

## H1-22. Generated Domain
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Directory
  2. Package
  3. Feature
  4. Base
  5. No duplicate segment

## H1-23. Configuration
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Precedence
  2. Default
  3. Override
  4. Dynamic
  5. Secret

## H1-24. CPF Framework Common 표준
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Code / Parameter / Message
  2. Calendar / Template
  3. Owner / Persistence
  4. Cache / Change propagation

## H1-25. Integration / Messaging / Notification 표준
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Timeout / Deadline
  2. Retry / Idempotency
  3. Schema / DLQ / Replay
  4. AI / Webhook
  5. Notification
  6. UNKNOWN / Reconcile

## H1-26. File / Archive / Object Storage 표준
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Attachment
  2. Inspection / Checksum
  3. Archive
  4. Tabular
  5. S3
  6. Sensitive data

## H1-27. OpenAPI / Frontend 연계 표준
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. operationId
  2. Generated Client
  3. Route Coverage
  4. Auth / Error
  5. Accessibility

## H1-28. Test
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Unit
  2. Contract
  3. Runtime
  4. Failure
  5. DB3
  6. Evidence

## H1-29. Code Review Gate
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Owner
  2. Consumer
  3. Error
  4. Security
  5. DB
  6. Generator
  7. Docs

## H1-30. 금지 패턴
- Content Model: `STANDARD_RULE_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Self-HTTP
  2. Cross-domain DB
  3. Internal direct dependency
  4. Protected Header manual build
  5. UNKNOWN forced result
  6. Unbounded retry
  7. Sensitive log
  8. Stale docs

# DATABASE_STANDARD — CPF 데이터베이스표준서

- Primary Persona: DB 개발자, 업무 개발자, DBA, Framework Maintainer
- Entry: DB 설계/변경/배포/복구를 수행해야 하는 상태
- Exit: Oracle/PostgreSQL/MariaDB를 동일 Lifecycle에서 설계하고 Migration/Upgrade/Rollback 또는 Recovery/Runtime Query/Generator 영향을 함께 판단할 수 있다.
- Orientation: landscape
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. CPF DB 표준
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 적용 범위
  2. Logical / Physical 분리
  3. Data Ownership
  4. DB 변경 완료 단위

## H1-2. Oracle / PostgreSQL / MariaDB
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_VENDOR`
- Required Figure Preset: 없음
- H2 순서:
  1. 공식 Vendor
  2. 지원 원칙
  3. 금지 Evidence Vendor
  4. Vendor 선택

## H1-3. Schema
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Business / Platform Schema
  2. Owner
  3. Privilege
  4. Naming

## H1-4. Naming
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Table
  2. Column
  3. PK/FK/UK/CK/Index
  4. Sequence

## H1-5. Table
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Owner
  2. Audit Column
  3. History
  4. 삭제 정책

## H1-6. Column
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Nullability
  2. Default
  3. Length
  4. Sensitive data

## H1-7. Datatype
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_VENDOR`
- Required Figure Preset: 없음
- H2 순서:
  1. Logical Type
  2. 문자
  3. 숫자
  4. 날짜/시간
  5. Boolean
  6. Large Object
  7. JSON

## H1-8. Primary Key
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Key 설계
  2. Natural / Surrogate
  3. Generator 영향

## H1-9. Foreign Key
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Ownership
  2. Cross-domain 금지
  3. Cascade

## H1-10. Unique
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Business uniqueness
  2. Null
  3. Concurrency

## H1-11. Check Constraint
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 사용 기준
  2. Vendor parity
  3. Migration

## H1-12. Index
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 선택 기준
  2. Composite order
  3. Covering
  4. Over-indexing
  5. Runtime Query

## H1-13. Sequence / Identity
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Vendor 방식
  2. Allocation
  3. Batch

## H1-14. Audit / History
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 변경 이력
  2. Actor / Time
  3. Before / After
  4. Retention

## H1-15. Lock / Concurrency
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Optimistic
  2. Pessimistic
  3. Distributed Lock
  4. Timeout / Deadlock

## H1-16. Batch Claim
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Claim 상태
  2. Lease
  3. Fencing
  4. Index
  5. 재할당

## H1-17. Fresh Initialization
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_LIFECYCLE`
- Required Figure Preset: 없음
- H2 순서:
  1. DB/User/Schema
  2. Initializer
  3. Canonical DDL
  4. Idempotency

## H1-18. Migration
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_LIFECYCLE`
- Required Figure Preset: 없음
- H2 순서:
  1. Version
  2. Forward migration
  3. Failure
  4. History

## H1-19. Seed
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_LIFECYCLE`
- Required Figure Preset: 없음
- H2 순서:
  1. Reference seed
  2. Idempotency
  3. 환경 차이
  4. 운영 데이터 금지

## H1-20. Upgrade
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_LIFECYCLE`
- Required Figure Preset: 없음
- H2 순서:
  1. Supported previous version
  2. Existing data
  3. Mixed version
  4. Parity

## H1-21. Rollback / Recovery
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_LIFECYCLE`
- Required Figure Preset: 없음
- H2 순서:
  1. Rollback 가능 변경
  2. Recovery-only 변경
  3. 데이터 보존
  4. 검증

## H1-22. Runtime Query
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Repository / Mapper
  2. Parameter binding
  3. Index
  4. Timeout

## H1-23. Performance
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Explain / Plan
  2. Index
  3. Batch
  4. Lock
  5. Capacity

## H1-24. Schema Mismatch
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. 탐지
  2. 기동 차단
  3. Migration 상태
  4. 복구

## H1-25. 장애 복구
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Connection
  2. Lock
  3. Partial migration
  4. Seed failure
  5. Upgrade failure

## H1-26. Generator / DB Binding
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `STANDARD_RULE`
- Required Figure Preset: 없음
- H2 순서:
  1. Domain contract
  2. Vendor render
  3. Datasource
  4. Generated Domain
  5. Sync

## H1-27. Vendor 차이
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_VENDOR`
- Required Figure Preset: 없음
- H2 순서:
  1. Datatype
  2. Identity
  3. DDL
  4. Lock
  5. SQL 함수
  6. Migration

## H1-28. Validation / Evidence
- Content Model: `DB_STANDARD_CHAPTER`
- Max Code: `L2`
- Required Table Preset: `DB_LIFECYCLE`
- Required Figure Preset: 없음
- H2 순서:
  1. Fresh
  2. Upgrade
  3. Rollback / Recovery
  4. Runtime transaction
  5. Schema parity
  6. Hash / Evidence

# DELIVERABLE_INDEX — CPF 산출물목록

- Primary Persona: 신규 사용자, Project Manager, 개발자, 운영자
- Entry: 어떤 문서를 먼저 열어야 하는지 모르거나 문서 갱신 영향을 확인해야 하는 상태
- Exit: 30초 안에 올바른 문서를 고르고 Source 변경 시 어떤 공식 문서를 갱신해야 하는지 판단할 수 있다.
- Orientation: portrait
- H1/H2 추가·삭제·개명·순서 변경: 금지

## H1-1. CPF 문서 길찾기
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. 30초 문서 선택표
  2. 처음 보는 사용자
  3. 개발자
  4. Batch 개발자
  5. 운영자
  6. Architect / Reviewer

## H1-2. 처음 CPF를 보는 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. README에서 얻는 답
  2. 다음 문서 선택

## H1-3. 업무 개발을 하는 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. Framework Developer Guide
  2. Specification
  3. Technical Standard
  4. DB Standard

## H1-4. Batch 개발을 하는 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. Batch Developer Guide
  2. Batch Operator Guide
  3. Specification

## H1-5. 운영하는 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. Operator Manual
  2. Batch Operator Guide
  3. Gateway Guide

## H1-6. Architecture를 확인하는 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. Architecture Design
  2. Technical Specification
  3. Specification

## H1-7. Contract가 필요한 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. Specification
  2. Source / Public API

## H1-8. 개발 표준을 확인하는 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. Technical Standard
  2. Developer Guide

## H1-9. DB 표준을 확인하는 경우
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. Database Standard
  2. Technical Specification

## H1-10. 공식 산출물 전체 목록
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. README
  2. Guide 6종
  3. Deliverable 5종
  4. DOCX/PDF 관계

## H1-11. 문서별 갱신 조건
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. API / Annotation
  2. Module / Topology
  3. CLI / Tool
  4. DB
  5. Frontend / OpenAPI
  6. Runtime / Operations

## H1-12. Source 변경과 문서 영향
- Content Model: `DOCUMENT_INDEX_CHAPTER`
- Max Code: `L1`
- Required Table Preset: `DOCUMENT_INDEX`
- Required Figure Preset: 없음
- H2 순서:
  1. 변경 유형
  2. 영향 문서
  3. 재검증 Gate
  4. Harness Change Required 조건

