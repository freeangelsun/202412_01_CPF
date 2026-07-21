# CPF Release Notes

## 1.0.0 GA

Core Platform Framework의 최초 상용 제품 기준 Release입니다.

### Platform

- MSA와 Modular Monolith
- 동일 JVM과 Remote 호출의 Typed Contract
- 표준 Gateway
- 표준 Header, 오류, Validation과 거래 식별
- Retry, Timeout, Circuit Breaker와 Bulkhead
- Idempotency, Lock과 상태 전이

### Business Modules

- Member
- Account
- Reference
- External Integration
- Business Common
- Business Administration

### Batch and Massive Processing

- Scheduler
- Agent and Worker
- Registration and heartbeat
- Lease, fencing and takeover
- Checkpoint and restart
- Center-Cut
- 동기·비동기 업무 호출

### Operations

- Service·Endpoint·Instance Registry
- 거래 그룹·상세·Timeline
- 파일 로그와 DB 로그
- Trace Boost
- Batch·Worker 관제
- 재처리·보상·결과 불명 복구
- 승인과 감사

### Security

- AuthN·AuthZ
- RBAC와 관리자 Dual Control
- 개인정보 Masking
- Secret rotation
- mTLS·OAuth2·JWT·API Key
- Security event와 immutable audit
- SBOM·Dependency·License·Secret scan

### Integration

- REST
- 고정길이 전문
- File·Attachment·Compression
- SFTP
- Outbox·Inbox·DLQ
- Saga·Compensation
- Reconciliation

### Data and Deployment

- MariaDB
- PostgreSQL
- Oracle
- SQL Server
- Flyway Migration
- 신규 설치·Upgrade·Rollback
- JAR·WAR·Docker·Kubernetes

### Developer Experience

- 신규 Domain Generator
- ACC lifecycle parity
- OpenAPI
- JavaDoc
- EDU
- Vue 3·TypeScript ADM/BZA
- Markdown documentation set

### Upgrade

초기 GA Release이므로 이전 상용 Version에서의 Upgrade는 없습니다. 개발 중 Legacy Module·Package에서 전환하는 경우 [Migration Guide](MIGRATION_GUIDE.md)를 따릅니다.

### Release Artifacts

Release 배포 시 다음을 제공합니다.

- Binary JAR/WAR
- Source archive
- JavaDoc
- Frontend assets
- DB install and migration
- Deployment templates
- SBOM
- Third-party notices
- Checksums
- Evidence index
