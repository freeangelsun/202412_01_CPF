# CPF Developer GPT Next Work Instruction

## Current basis

Use the latest user-local Working Tree/result package as the next execution basis. Do not inherit historical PASS or Git SHA. The current source package was derived from baseline `b5573c0ab545597563846d0fd31e8669e5b7fec6df73393fed70f17b5f0b6850` without `.git`.

## Architecture invariants to preserve

- Business transaction Canonical Header/Context is System-based six; Channel is separate optional policy/security context.
- `cpf-biz-admin` is an Optional Prebuilt Business Administration Domain. Business masters remain owned by their Business Domain.
- external `cpf-biz-channel` is DB-less Pure Spring Boot / CPF Java dependency 0; `cpf-biz-frontend` calls Channel only.
- Direct HTTP never bypasses security/policy/audit/transaction enforcement.
- all canonical optional/user-selectable surfaces obey physical-removal semantics.
- Source quality includes ownership/package/naming/discoverability/maintainability/operability, not only functional execution.

## Next execution order

1. Ingest the user's latest Java25/DB3/Node/Browser/Public-release validation output.
2. Classify PASS/FAIL/SKIP_ENV/NOT_EXECUTED and reproduce every FAIL against that exact Working Tree.
3. Repair by common root cause across Source/Test/Verifier/Generator/Frontend/SQL/Evidence; do not add duplicate V2/V3 layers.
4. Re-run the smallest affected gates, then the complete required runtime closure.
5. Runtime not executed stays `미검증`.
6. Do not physically delete files or perform Git writes without explicit user approval. Use `cpf-docs/deliverables/DELETE_MANIFEST.csv` for stale candidates.
7. Do not automatically create `WORK_RESULT_REVIEW`; create the detailed 1:1 report only when the user explicitly requests it after development/validation/packaging.
