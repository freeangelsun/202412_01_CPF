# R6J Transaction / Logging Development Rework

Basis SHA: `3ed676061246c9db3e44f29e254c0393ecca3929`

## P0 Findings
- `R6J-CENTRAL-NEW-006` ADM one-shot multi-source timeline
- `R6J-CENTRAL-NEW-007` DB transaction identifiers/linkage
- `R6J-CENTRAL-NEW-014` FileLog durable failure recovery
- `R6J-CENTRAL-NEW-005` Current canonical logging requirements ledger integration

## Acceptance
- transactionId remains identical across nested tx, remote, async, message, batch, center-cut
- segment/parent/attempt/trace/span/request/idempotency/tenant and execution IDs correlate
- ADM transactionId exact query produces full timeline/tree
- message/DLQ/batch/file/trace/audit links
- partial/stale/missing source state
- DB3 indexed canonical storage and retention lifecycle
- FileLog durable spool/retry/dedup/checksum/quarantine/loss alert
- disk-full/read-only/process-kill/restart evidence
- PII/Secret masking and raw log permission/reason/audit
