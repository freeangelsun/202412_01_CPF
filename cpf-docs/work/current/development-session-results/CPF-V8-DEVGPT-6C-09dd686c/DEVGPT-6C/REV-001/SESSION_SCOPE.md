# DEVGPT-6C V8 Session Scope

- Campaign: `CPF-V8-DEVGPT-6C-09dd686c`
- Session: `DEVGPT-6C`
- Revision: `REV-001`
- Baseline: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`
- Management canonical: `cpf-docs/work/current/CPF_DEVELOPMENT_MANAGEMENT_V8/`
- Scope chain: `WORK_ITEM_INDEX.csv → markdown_file → ledger_part → CPF Requirement/Scenario logical masters`

## Fixed-point counts

| Entity | Count |
|---|---:|
| Work Item | 14 |
| Canonical Requirement | 2 |
| CPF-FR | 338 |
| Direct canonical CPF-FR | 284 |
| Linked Runtime Capability CPF-FR | 54 |
| CPF-SC | 950 |
| Engineering Gate | 16 |
| Missing / duplicate / unassigned | 0 |

## Work Item exact IDs

- `CPF-WP-OPS-CONFIG-01-CONTRACT_OWNERSHIP`
- `CPF-WP-OPS-CONFIG-02-IMPLEMENTATION_CONSUMER`
- `CPF-WP-OPS-CONFIG-03-VERIFICATION_EVIDENCE`
- `CPF-WP-OPS-CONFIG-04-FAILURE_RECOVERY`
- `CPF-WP-OPS-CONFIG-05-OPERATIONS_SECURITY`
- `CPF-WP-OPS-CONFIG-06-DATA_MIGRATION`
- `CPF-WP-OPS-CONFIG-07-GENERATION_COMPATIBILITY`
- `CPF-WP-OPS-DRIFT-01-CONTRACT_OWNERSHIP`
- `CPF-WP-OPS-DRIFT-02-IMPLEMENTATION_CONSUMER`
- `CPF-WP-OPS-DRIFT-03-VERIFICATION_EVIDENCE`
- `CPF-WP-OPS-DRIFT-04-FAILURE_RECOVERY`
- `CPF-WP-OPS-DRIFT-05-OPERATIONS_SECURITY`
- `CPF-WP-OPS-DRIFT-06-DATA_MIGRATION`
- `CPF-WP-OPS-DRIFT-07-GENERATION_COMPATIBILITY`

## Engineering Gate exact IDs

- `GATE-01-OWNERSHIP`
- `GATE-02-CONSUMER`
- `GATE-05-DB-QUERY`
- `GATE-06-STATE-IDEMP`
- `GATE-08-UNKNOWN-RECOVERY`
- `GATE-09-SECURITY`
- `GATE-10-CRYPTO-PRIVACY`
- `GATE-11-OPS-AUDIT`
- `GATE-12-OBSERVABILITY`
- `GATE-15-GENERATOR`
- `GATE-16-COMPATIBILITY`
- `GATE-17-SUPPLY-CHAIN`
- `GATE-18-TEST-EVIDENCE`
- `GATE-19-DOC-SUPPORT`
- `GATE-20-HYGIENE`
- `GATE-21-TIME`

## Full ID files

- `CPF_FR_SCOPE.csv`: 338 exact Requirement IDs
- `CPF_SC_SCOPE.csv`: 950 exact Scenario IDs
- `CAPABILITY_CONSUMER_COVERAGE.csv`: 54 capability Requirement IDs and actual consumer status
- `FR_DEVELOPMENT_REVIEW.csv`: 338 per-ID implementation and evidence rows
- `SCENARIO_STATUS.csv`: 950 per-ID test and evidence rows

## Ownership check

The user-assigned DEVGPT-6C exclusive scope covers Runtime Control Plane/Agent, runtime/config/policy/distribution/control starter paths,
`cpf-core/**/runtimecontrol/**`, related backend/tests/config/metadata. ADM Frontend/Generated Client, DB Vendor SQL, Root Build,
Security/Crypto contract changes and capability-owner adapters are cross-session requests and were not directly modified outside the assigned paths.
