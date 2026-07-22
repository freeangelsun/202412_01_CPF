# CPF 최종 목표 요구사항 정본

## 1. 제품 정의

`Core Platform Framework(CPF)`는 금융권을 포함한 다양한 업무 시스템을 구축·운영·감사·확장·검증·배포·상용화하기 위한 Business Platform 품질의 Framework다.

제품은 단순 Library, Sample, 공통 모듈 집합이 아니다. Source·SQL·API·Test·Guide·Evidence가 일치하고, 장애·복구·보안·운영·배포까지 제품 상태로 제공돼야 한다.

## 2. 지원 Topology

- Modular Monolith
- MSA
- 동일 JVM Local Call
- 분리 WAS Remote Call
- Embedded JAR
- External WAS WAR
- ADM/BZA 독립 Web Server
- Agent/Runner/Worker 독립 Process
- Multi-instance
- Rolling·Canary·Blue-Green

## 3. 공식 Module

| Module | SystemCode | 책임 |
|---|---:|---|
| cpf-core | CPF | 기술 공통 Contract·SPI·Runtime |
| cpf-common | CMN | 고객사 업무 공통 Extension |
| cpf-admin | ADM | 플랫폼 운영 Control Plane |
| cpf-biz-admin | BZA | 고객 업무 Admin |
| cpf-batch | BAT | Batch·Scheduler·Agent·Runner·Worker·Center-Cut |
| cpf-gateway | GWY | 외부 Entry·Routing·Policy |
| cpf-member | MBR | 최소 생성형 Reference |
| cpf-account | ACC | 최소 생성형 Reference |
| cpf-reference | REF | EDU·Reference |
| cpf-external | EXS | 기관별 대외연계 |

모든 공식 Module은 `cpf-` Prefix, Java Package는 `com.cpf.*`를 사용한다.

## 4. Dependency Principle

```text
Business → Common → Core
Gateway → Core
Batch → Core + Business Public Contract
Admin → Operations Contract
BZA → Business Contract
```

- Core 역방향 의존 금지
- 순환 금지
- Domain DB 직접 접근 금지
- Public API·SPI·Internal 경계
- ArchUnit·Build Gate 강제

## 5. cpf-core 목표

### 5.1 Runtime Foundation

- Standard Header
- Transaction·Trace·Segment
- Context Propagation
- Error Standard
- Validation
- Idempotency
- Timeout Budget
- Retry
- Circuit Breaker
- Bulkhead
- Unknown Result
- Reconciliation
- Logging
- Audit Hook
- Masking
- Security Context
- Service Call
- File·Attachment·Compression Contract
- Broker Contract
- Fixed-Length Engine
- Collection·Paging·Cursor
- Configuration
- Public Test Kit

### 5.2 Public API

- `com.cpf.core.api`
- `com.cpf.core.spi`
- `com.cpf.core.internal`

API는 IDE에서 발견 가능하고 최소 입력으로 안전하게 사용 가능해야 한다.

### 5.3 Developer Utility

- CpfDates
- CpfTimes
- CpfStrings
- CpfNumbers
- CpfDecimals
- CpfLists
- CpfMaps
- CpfIds
- CpfFiles
- CpfHashes
- CpfValidation
- CpfPages
- CpfHeaders
- CpfAttributes
- CpfClock

의미 없는 Wrapper와 거대 Util 금지.

## 6. cpf-common 목표

- 고객 Header·User Context 확장
- 고객 Validation
- 고객 Error Mapping
- 고객 Web Client Profile
- 고객 Masking·Audit 정책
- 고객 공통 Facade
- Core SPI 구현
- 고객 공통 DTO·Metadata

기본 `cmnDB`는 최소 Sample Table 1개만 제공한다. 기술 Engine, 업무 채번, 고정길이 전문, 알림·통합 Log를 기본 소유하지 않는다.

## 7. 데이터 목표

### 7.1 Ownership

모든 Table은 하나의 Owner와 실제 Consumer를 가진다.

### 7.2 Schema

권장:

- cpfDB
- cmnDB
- admDB
- bzaDB
- batDB
- mbrDB
- accDB
- refDB
- exsDB

### 7.3 cmnDB

Sample Table 1개로 CRUD·Paging·Cursor·Validation·Transaction을 검증한다.

### 7.4 Business Reference

MBR·ACC·EXS는 최소 검증 Table만 제공한다. 추정성 전체 업무 Schema 금지.

### 7.5 Migration

- Empty Install
- Immutable Flyway
- Upgrade
- Rollback
- Reinstall
- Multi-version Compatibility
- Vendor Support
- Backup·Restore
- Evidence

## 8. Fixed-Length 목표

### Core

- Layout·Field·Group
- Byte Length
- Encoding
- Padding
- Converter
- Parser·Writer
- Validation
- Masking
- Streaming
- Version
- Error Offset

### External

- 기관별 Layout·Mapping
- Endpoint·Auth
- Adapter
- Retry·Unknown·Reconciliation

## 9. File·Messaging 목표

- File Transfer
- Attachment
- Chunk
- Compression
- Checksum
- Path Alias
- SFTP
- File Watch·Process
- Broker Envelope
- Outbox
- Inbox
- DLQ
- Replay
- Correlation
- Idempotency
- Poison Isolation
- Operations

## 10. 업무 채번 정책

업무 채번은 CPF Core/Common 기본 기능이 아니다.

BZA는 선택형 Customization Sample과 관리 화면을 제공한다. 실제 Runtime Owner는 고객 공통, 해당 업무 Domain 또는 별도 고객 Service다.

## 11. Gateway 목표

- External Entry
- Authentication Policy
- Routing
- Health
- Load Balance
- Timeout
- Circuit Breaker
- Failover
- Header Trust
- Registry
- Manual Block
- Audit
- Metrics

내부 호출 Gateway 재경유 금지.

## 12. Batch 목표

### General Batch

- Job·Step
- Schedule·Calendar
- Dependency
- Misfire
- Parameter
- Checkpoint
- Restart
- Agent Pool
- Java·Shell·File Executor
- Timeout·Retry·Skip·Rollback

### Center-Cut

- CenterCutRunner
- Job·Item·Attempt
- Immutable Parameter
- Target Generation
- Worker Dispatch
- Global TPS
- Pause·Resume·Cancel
- Failed-only Reprocess
- Unknown Result
- Compensation
- Timeline

### Agent

- Primary·Secondary
- Capability
- Zone
- Lease
- Fencing
- Drain
- Failover
- Failback
- Path Alias
- Duplicate Prevention

## 13. ADM 목표

- Online Transaction
- Async·Unknown
- Batch
- Center-Cut
- Agent·Runner·Worker
- Gateway·Registry
- Log·Trace
- Security
- Approval
- Audit
- Recovery
- Policy

모든 Control은 Domain Command API와 권한·승인·감사를 거친다.

## 14. BZA 목표

- Customer Business Admin
- Business Menu·Permission
- Approval
- Business Search·Download
- Customer Extension
- Optional Sample

업무 원장을 임의 소유하거나 CPF Runtime을 직접 제어하지 않는다.

## 15. Frontend 목표

- Vue 3·TypeScript·Vite
- Feature Folder
- ADM/BZA 분리
- API Contract
- Permission
- Accessibility
- Production Build
- Web Server Deployment
- SPA Deep Link
- CSP
- Cookie/CSRF
- Independent Version·Rollback
- Browser E2E

## 16. API 목표

- Standard Envelope
- Standard Error
- List/Page/Slice/Cursor
- Async
- File
- Streaming
- Multipart
- SSE
- Batch·Center-Cut
- Versioning
- OpenAPI
- JavaDoc
- Compatibility

## 17. Security 목표

- AuthN
- AuthZ
- RBAC
- MFA
- IP Allowlist
- mTLS
- OAuth2/JWT/API Key
- Secret Rotation
- PII Classification
- Masking
- Download Control
- Dual Control
- Immutable Audit
- Secure Coding
- SBOM
- License
- Dependency
- Secret Scan

## 18. Generator 목표

- DomainName/SystemCode 분리
- Reserved·Collision
- Module·Package·Config·SQL·Route·Menu
- Capability
- 4 DB Vendor
- Create·Verify·DB Init·Runtime·Delete·Regenerate
- Manifest
- User-owned File 보호
- Hash/Semantic Parity
- Official API Sample

## 19. Reliability 목표

- Multi-instance
- Idempotency
- Retry
- Timeout
- Circuit Breaker
- Bulkhead
- Backpressure
- Lease
- Fencing
- Checkpoint
- Unknown Result
- Reconciliation
- Compensation
- Durable Recovery
- Partial Failure
- Chaos/Fault Injection

## 20. Observability 목표

- File Log
- DB Log
- Trace
- Metrics
- Audit
- Timeline
- Failure Stage
- Instance/Host/Container
- Agent/Runner/Worker
- Dynamic Log Level
- Trace Boost
- Alert
- Retention
- Masking

## 21. Installation·Deployment 목표

- Safe Install
- Allowlisted Reset
- User Provision
- Upgrade
- Rollback
- Restore
- JAR
- WAR
- Docker
- Kubernetes
- Web Server
- Remote Deploy
- Rolling
- Canary
- Blue-Green
- DR

## 22. Guide·EDU 목표

- Architecture
- Developer
- API
- Security
- Generator
- Installation
- Deployment
- Operator
- Recovery
- Migration
- Batch·Center-Cut
- External·Fixed-Length·File
- Frontend
- JavaDoc
- OpenAPI
- EDU
- Test Kit

Guide는 Source·Property·API·UI·SQL과 일치해야 한다.

## 23. Evidence 목표

- Commit
- Command
- Environment
- Time
- Exit Code
- Raw/Sanitized
- Requirement
- Source/API/SQL/Test
- Secret Review
- Stale Status

## 24. 완료 Gate

다음 모두가 확인돼야 최종 완료다.

- Clean Build
- Full Test
- Empty DB Install
- Reinstall
- Upgrade·Rollback
- All Runtime
- Multi-instance
- Fault·Recovery
- ADM/BZA Browser
- Split Deploy
- Security
- Guide
- Evidence
- Root Hygiene
- No Stale Names
- No Stale Evidence
- No Secret
