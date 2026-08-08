# CPF Current Open Issues

1. Core Transaction Strategy new requirements (`TX-*`) require implementation.
2. JTA/XA and crash recovery require Source, provider adapter, Consumer, Test/Harness and live verification.
3. TCC and Inbox/Dedup require explicit implementation/Reference.
4. All active Starters require Developer Experience revalidation; previous status is not automatically inherited.
5. AI Optional, Security DX/SSO, KMS/HSM/Signature hardening require implementation/revalidation.
6. Prior package relocation old paths must be cleaned by exact allowlist after replacement checks.
7. New canonical requirements must be decomposed into the logical CPF-FR/CPF-SC masters during development; do not pre-mark child rows PASS.
8. After development, strengthened QA A/B must independently deep-audit the new successor exact SHA.
9. Live Java25/Gradle9.1, DB3, browser, broker, XA, multi-instance/process-kill and DR axes remain release-blocking until executed where required.

Overall: `REDEVELOPMENT REQUIRED / UNVERIFIED / RELEASE_BLOCKED`.
