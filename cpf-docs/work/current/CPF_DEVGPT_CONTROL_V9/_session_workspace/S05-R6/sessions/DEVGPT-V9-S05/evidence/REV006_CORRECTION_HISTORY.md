# REV-006 correction history

- ADM approval execution stale-version finalization defect: replaced by atomic reservation/finalization and UNKNOWN recovery.
- Approval request/decision divergent idempotency replay: strict immutable replay matching added.
- Duplicate ADM approval route and frontend `approvalRequestId` mismatch: canonical route ownership and generated consumer contract enforced.
- Default mock notification sender: product mode now fails closed; mock is test/profile constrained.
- ADM MFA placeholder acceptance: TOTP secret resolution and verification fail closed.
- BZA approval/sequence idempotency and audit boundaries: strict replay and lifecycle assertions added.
- OpenAPI validator schema-version drift: validators aligned with canonical schema version and operation inventory.
- S02 Center-Cut integration: execution-scoped approval, fingerprint validation, requester/approver separation, UNKNOWN/reconcile and generated frontend consumer added.
- Center-Cut product test API drift: obsolete constructor/command API test replaced with canonical `AdmApprovalService` contract test.
- Intermediate harness/snapshot failures were tooling or pre-fix reproductions; they are superseded by the REV-006 Exit Code 0 evidence retained in this package.
