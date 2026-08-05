# Integration Change Request — ICR-V9-S02-S04-0002

- Source request: `DEVREQ-V9-S02-LOCAL-20260805-2140-R005`
- Source session: `DEVGPT-V9-S02`
- Target session/owner: `DEVGPT-V9-S04 / DB·SQL·Vendor Pack Integration Owner`
- Priority: `P0`
- Baseline: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- Status: `S02_BACKEND_COMPLETE_TARGET_INTEGRATION_PENDING`

## Required changes

1. In MariaDB/PostgreSQL/Oracle `scheduler-trigger-find-dispatchable.sql` and `scheduler-trigger-claim.sql`, remove `UNKNOWN` from normal automatic dispatch states. Keep `CREATED` and approved retry states only.
2. Add vendor-parity SQL and catalog/pack entries for explicit scheduler UNKNOWN reconciliation: load by `(schedule_id, scheduled_fire_at)`, CAS `UNKNOWN -> CREATED` using idempotency key and expected attempt count, and approved audit insertion.
3. Change all three vendor `centercut-item-complete.sql` statements to CAS only an actively owned `RUNNING` item; retain S02 row-count fail-closed checks.
4. Integrate the S02 `RemoteMessageReconciliationController` SQL keys and extend canonical/vendor schema metadata and operational queries so `bat_remote_message_ledger.status_cd=UNKNOWN` is documented, queryable and explicitly reconcilable; do not auto reclaim UNKNOWN.
5. Add install/upgrade/rollback/verify parity and SQL catalog duplicate/path tests for all new keys.

## Acceptance

- Three vendors are byte/semantic-parity where syntax permits.
- UNKNOWN is not returned by normal scheduler dispatch queries.
- Explicit retry requires verified requester/approver, reason, idempotency key and expected attempt count.
- Stale Center-Cut completion cannot overwrite a non-RUNNING item.
- Remote message UNKNOWN cannot be reclaimed until an approved reconcile transition.
- S04 pushes the implementation and records target commit/evidence; S02 then reruns source consumer regression on latest master.

## S02 REV-005 fail-closed compatibility guard

S02 now validates `scheduler-trigger-find-dispatchable` and `scheduler-trigger-claim` before dispatch. Any SQL containing the literal `UNKNOWN` is rejected with `SCHEDULER_UNKNOWN_AUTO_DISPATCH_SQL_REJECTED:<key>` before query, claim or external Spring Batch start. S04 integration is still required to restore Scheduler availability and provide explicit approved reconcile.
