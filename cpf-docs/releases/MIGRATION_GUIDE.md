# CPF Database·Install·Migration Guide

## 1. 책임 분리

- Install
- Reset
- Provision
- Migration
- Rollback
- Seed
- Verify

한 Script에 Drop·Create User·DDL·Seed를 혼합하지 않는다.

## 2. Empty Install

- Drop 없음
- Schema Allowlist
- Charset/Collation
- DDL
- Index/Constraint
- Seed
- Idempotency
- Verification

## 3. Reset

개발·테스트 전용.

```text
Dry Run default
→ Target Display
→ Exact Schema Allowlist
→ Other Schema Check
→ Explicit Apply
→ Drop CPF Schemas
```

Wildcard 금지.

## 4. Provision

- Migration User
- App User
- Read-only User
- Service별 분리
- Minimum Grant
- Secret 외부 입력
- Evidence Masking

## 5. Schema Ownership

- cpfDB: Core Runtime
- cmnDB: 1 Sample
- admDB: Platform Admin
- bzaDB: Business Admin
- batDB: Batch Runtime
- mbrDB/accDB: Minimal Reference
- refDB: EDU
- exsDB: External

## 6. Flyway

- Applied Migration Immutable
- Checksum
- Gap 없음
- Repeatable 구분
- Baseline
- Expand/Contract
- Multi-version
- Lock·Duration
- Backfill

## 7. Pre-release Re-baseline

아직 정식 출시 전이고 모든 DB를 폐기한다면:

1. 기존 Lineage Inventory
2. 공식 승인
3. Old Migration Archive
4. 단일 New Baseline
5. Checksum Freeze
6. Empty Install
7. Upgrade Simulation

임의 Rename로 Baseline을 바꾸지 않는다.

## 8. Rollback

- Data Loss
- Backup
- Compatibility
- Rows Affected
- Validation
- Forward-fix
- Restore

## 9. Vendor

- MariaDB
- PostgreSQL
- Oracle
- SQL Server

Identity, Sequence, CLOB, Boolean, Timestamp, Index를 검증한다.

## 10. Evidence

- Schema
- Object Count
- Owner
- Grant
- Seed
- Migration History
- Duration
- Reinstall
- Upgrade
- Rollback
- Other Schema Intact
