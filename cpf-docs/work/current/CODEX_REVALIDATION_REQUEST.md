# CODEX REVALIDATION REQUEST

Revalidate the latest CPF desired-state source independently. **Do not inherit any prior PASS, historical SHA, or embedded-BZA/Channel-header assumption.**

## Basis

- Baseline input SHA-256: `b5573c0ab545597563846d0fd31e8669e5b7fec6df73393fed70f17b5f0b6850` / 8,440 files / `.git` absent in supplied ZIP.
- Desired state excludes only the root-relative `approved=false` candidates in `cpf-docs/deliverables/DELETE_MANIFEST.csv`; validate protected-delete=0.
- Runtime not executed in the review environment must remain `미검증`.

## Priority rechecks

1. Canonical transaction transport is System-based six: Transaction, Original System, Current/Receiver System, Caller System, Target System, Target Operation. Channel identity is a separate optional policy/security context.
2. Receiver owns `X-System-Code`; external Channel cannot author it as trusted identity. Same-JVM/Remote semantics are equivalent without self-HTTP.
3. `cpf-biz-admin` is an Optional Prebuilt Business Administration Domain, not the external Channel application. It owns only BZA/backoffice state and never duplicates other Business Domain masters or directly accesses their DB.
4. `cpf-biz-channel` is standalone DB-less Pure Spring Boot with CPF Java/BOM/Starter/Internal dependency 0; `cpf-biz-frontend` calls Channel only. Direct HTTP must not bypass authN/authZ/Channel Policy/Audit/transaction contract.
5. Physical-removal Optionality: BZA surfaces and all catalog optional/user-selectable surfaces must not be fixed dependencies of mandatory Build/Install/Publication/Verifier paths.
6. Common runtime: canonical `cpfDB` / Common Management/Catalog path only; no active legacy CMN transaction-manager/runtime-mode/datasource consumer.
7. Runtime identity single provider; invalid hostname/synthetic fallback fails closed.
8. Subject tracking: late binding, trusted conflict behavior, original transaction time search, ADM timeline/lineage consumer and masked audit.
9. Pre-controller Header/security reject creates sanitized durable transaction evidence.
10. ADM Managed Server/Runtime Inventory: central owner, server-side paging, feature consumers, no duplicate server masters.
11. Retention actual executor path: scheduled/manual/pause/resume, lease/chunk/throttle/history; no configuration-only false green.
12. EDU Online20/Batch15: feature-first role packages, actual public Golden Path/consumer/test, nested dummy/static-inner compression 0, obsolete flat entries only in Delete Manifest.
13. ADM/BZA Frontend maintainability: feature ownership, generated/OpenAPI client, no stale embedded BZA path, no native prompt/confirm or raw JSON operational UI.
14. Public Distribution: empty default-deny staging, explicit classification, private/internal/secret leakage 0, clean consumer verification, and commit/push unreachable on any failed gate.
15. Requirement Master index/projection/current evidence all bind to the same current baseline and preserve QA/Codex column ownership.
16. Final verifier Git NUL parsing works in actual `.git` checkout mode and ZIP/fallback mode.
17. Source structure review must include ownership/package/naming/dependency direction/maintainability/operability; functionality-only PASS is insufficient.

## Runtime revalidation

Execute Java25 full Gradle/publication/SBOM, official Node frontend build/test, DB3 live lifecycle, Redis/Valkey, Multi-WAS/process-kill/redeploy and browser E2E when the environment supports them. Mark unexecuted stages `미검증`; do not substitute static PASS.

Do not modify Developer-GPT or QA-owned ledger columns. Record Codex-owned findings/evidence only.
