# Integration Request S06-INT-NOTIFICATION-INCIDENT-CANONICAL-DB-PARITY

- Parent development request: `CPF-V9-INITIAL-FULL-REVIEW-20260805-2020-REV-007-DEVGPT-V9-S06`
- Baseline SHA: `af12a0c8851a2e8d20e9e42964d8dacc0266af03`
- Target Integration Owner: `DEVGPT-V9-S04`
- Priority: `P0`
- Exact paths:
  - `cpf-tools/db/vendor/oracle/source/10_cpf_schema.sql`
  - `cpf-tools/db/vendor/postgresql/source/10_cpf_schema.sql`
  - `cpf-tools/db/vendor/mariadb/source/10_cpf_schema.sql`
- Related IDs: `GAP-NOTIFICATION;GAP-INCIDENT-MANAGEMENT`
- Current status: `PENDING_INTEGRATION_OWNER_IMPLEMENTATION`
- Baseline reproduction: `evidence/notification-incident/BASELINE_GATE.log` (`Exit Code 1`)
- Proposed patch verification: `evidence/notification-incident/PROPOSED_PATCH_GATE.log` (`Exit Code 0`)
- Proposed diffs:
  - `evidence/notification-incident/oracle_10_cpf_schema.patch`
  - `evidence/notification-incident/postgresql_10_cpf_schema.patch`
  - `evidence/notification-incident/mariadb_10_cpf_schema.patch`

## Defect

The exact baseline contains V92 Flyway migrations, R92 rollbacks and matching checksums for Notification/Incident lifecycle, but all three canonical fresh-install schemas omit the V92 tables, constraints and indexes. A fresh install therefore differs from an upgraded database and cannot provide the runtime tables required by the existing backend and frontend consumers.

## Required implementation and acceptance

Apply each vendor-specific V92 DDL to its canonical `source/10_cpf_schema.sql` without changing the existing migration or rollback order. Then run:

```text
python cpf-tools/scripts/verify-cpf-notification-incident-lifecycle.py --root .
```

Acceptance requires Exit Code 0, all three canonical schemas containing the Notification/Incident tables, constraints and indexes, V92 checksum validity, R92 rollback coverage, and existing backend/frontend consumer closure.

## Completion rule

The target owner must implement the three canonical schema changes, rerun the lifecycle gate at the integrated exact SHA, and return the implemented SHA plus evidence. This request document and the proposed patch are not completion evidence.
