# QA-12B R3 Rework and Cross-Review Request

## QA direct patches awaiting review

- QA12B-EVT-001: Reference actual Broker consumer/sample path.
- QA12B-EVT-002: JMS/IBM MQ/Rabbit metadata integrity.

Development GPT must run Java25 Module Tests and inspect compatibility. Codex must independently review the ten files.
QA reruns only after user Push and a new origin/master exact SHA.

## Design/redevelopment still required

- EVENT-MQ per-message priority/expiry/transaction/redelivery/DLQ/provider-neutral contract.
- DB lifecycle product operations and runtime fault coverage.
- Institution adapters and provider-specific operations.
- Durable Saga implementation.


## QA12B-EXS-TCP-001 — shared contract and durable recovery
Implement topology-independent TCP Public API/SPI, institution Adapter consumer, durable UNKNOWN/audit/idempotency state, actual reconcile, heartbeat/half-open/reconnect, OpenAPI/ADM operations and 3-vendor migration. Preserve the QA backpressure and atomic-bound patches.

## QA12B-EVENT-DLQ-003 — Approval, masking and durable audit

Implement approvalId/requester/approver/expiry/immutable command hash in the shared replay contract,
persist it in the 3-Vendor canonical schema, prevent self-approval, mask payload/header by default,
provide an approved original-view path, and record append-only before/after/reason/approval/result.
Add CAS/fencing for concurrent replay and actual Broker/DB fault tests.
