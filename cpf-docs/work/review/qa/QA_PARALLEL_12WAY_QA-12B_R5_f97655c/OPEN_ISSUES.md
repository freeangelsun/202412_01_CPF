# QA-12B R3 Open Issues

1. Two QA direct patches need Development GPT and Codex independent review.
2. Java25 Gradle and Spring AutoConfiguration remain unexecuted.
3. Real JMS/IBM MQ/RabbitMQ runtime, response loss, restart and recovery remain unverified.
4. EVENT-MQ priority/expiry/transaction/redelivery/DLQ per-message Public contract needs design review; QA did not expand Public API.
5. User apply/commit/push and new exact-SHA QA rerun are pending.
6. Next assigned ID: CPF-FR-004332.


## R4 EXS-TCP remaining
- Public API/SPI and institution Adapter Consumer require shared design.
- Heartbeat, reconnect policy integration, half-open probe, durable UNKNOWN/reconcile, security and ADM operations remain.
- QA patches require Development GPT and Codex cross-review plus Java25 post-push revalidation.

## R5 EVENT-DLQ

- Approval identity, requester/approver separation, self-approval prohibition and immutable command hash require shared contract design.
- Raw payload/header masking before ADM response is not proven.
- Expected version/CAS/fencing for concurrent replay is absent.
- 3-Vendor and actual Broker replay/fault/runtime remain unverified.
