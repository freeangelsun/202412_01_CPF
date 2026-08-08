# QA Rework / Verification Request

Developer GPT has prepared the Session 18 NXT implementation package and self-review evidence. This file **does not set or modify QA PASS status**.

QA should start only from the post-apply, post-authorized-delete, user-pushed central exact SHA. Re-run QA A, QA B, cross-validation and Framework Fundamentals independently. Use `REQUIREMENT_STATUS.csv`, `TEST_AND_EVIDENCE.md`, `RUNTIME_ONLY_VERIFICATION.csv`, Core/Fundamental audits, source, SQL, frontend, generator and runtime evidence as inputs, not as inherited PASS decisions.

If any source/runtime defect is found, reopen the same exact `NXT-*` ID with reproducible evidence and preserve the original Acceptance Criteria.
