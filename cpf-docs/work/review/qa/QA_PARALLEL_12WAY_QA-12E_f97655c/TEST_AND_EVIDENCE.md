# QA-12E Test and Evidence — Non-final Checkpoint

- Baseline SHA: `f97655c1299936a1101bc3ec10239265ec3b502e`
- Branch: `master`
- Latest commit message: `04-03`
- Assigned logical range: `10,187–12,733`
- Requirement count: `2,547`
- Actual first ID/order: `CPF-FR-012539` / `05-00012539`
- Actual last ID/order: `CPF-FR-008247` / `06-00008247`
- Individually opened in this checkpoint: `7`
- Pass: `0`
- Fail: `0`
- Environment-difference unverified after review: `7`
- Not individually reviewed: `2,540`

## Commands and results

1. GitHub MCP `list_commits(master)` — success; exact SHA confirmed.
2. Local `git clone --branch master --single-branch ...` — exit `128`; DNS resolution failed.
3. `python build-cpf-full-qa-ledgers.py --help` — exit `0`; builder explicitly identifies itself as coverage/metadata only.
4. Parsed `CPF_EXECUTION_SEQUENCE_PART_001...csv`:
   - logical row 10,187 → `CPF-FR-012539`
   - logical row 12,733 → `CPF-FR-008247`
   - unique requirement IDs in partition → `2,547`
5. GitHub MCP source inspection:
   - `cpf-core/src/main/java/com/cpf/core/service/reliability`
   - visible files are fault-injection classes only.
6. GitHub code searches for `Lease Renewal`, `LeaseRenewal`, `lease`, and Java `Lock`:
   - no matches returned, but result flag was `incomplete_results=true`; not conclusive.

## Rerun condition

Use a clean exact-SHA checkout and run complete repository searches, relevant Gradle tests, three-vendor SQL checks, consumer tracing, and scenario linkage validation. Do not convert these seven rows to pass or fail until that evidence exists.
