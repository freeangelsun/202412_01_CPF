# ICR-V9-S02-S05-0001

- requester_session: `DEVGPT-V9-S02`
- target_integration_owner: `DEVGPT-V9-S05 ADM/OpenAPI/Frontend`
- priority: `P0/P1`
- baseline_sha: `fc207ac5560da59f352ee0c5f83199177f2987b4`
- exact_path: `cpf-admin/**`, `cpf-admin/frontend/**`, generated OpenAPI client
- related_work_items: all `CENTER-UNKNOWN`, `CENTER-REPROCESS`, `CENTER-OPS` consumer/verification Work Items
- root_cause: backend execution-scope fail-closed contract is not yet wired to ADM; job-scope bulk endpoints are disabled.
- current_behavior: existing ADM consumer may target job-scope or omit execution-scoped approval/error UX.
- expected_behavior: executionId-scoped action, reason, separate approval, idempotency, 409/401/403/429/500/503 UX, audit and E2E.
- contract_change: use `POST /api/v1/batch/center-cut/executions/{executionId}/reprocess-failed` and `/reconcile-unknown`.
- affected_consumers: ADM Center-Cut page, generated API client, OpenAPI, E2E.
- compatibility_migration_impact: job-scope actions must be removed/disabled; read-only compatibility may remain.
- required_tests: generated client parity, permission/reason/approval, 409 stale state, accessibility, responsive and E2E.
- validation_commands: frontend typecheck/test/build + backend consumer regression against latest master.
- success_criteria: no job-scope mutation consumer; execution-scope action succeeds and errors are explicit.
- failure_criteria: blind retry, bulk job mutation, missing approval/audit, false success.
- evidence: S02 `CENTER-UNKNOWN_REVIEW.md`, `CENTER-REPROCESS_REVIEW.md`, backend tests/harness.
- blocking_relation: final integrated verification only; unrelated S02 work continued.
