# CPF Current Work Request — QA37 Codex Verification Primary

- Baseline exact SHA: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`
- Branch: `master`
- Current package status: **Source Closure Candidate / Codex Verification Ready**
- Overall status: `미검증`
- Canonical scope: QA37 64 + SELF 30 + EDU32 32 + Customer Manual EDU135 135
- EDU ownership: `cpf-reference` single module and central `refDB`
- Generated-domain dependency: prohibited
- Product BZA dependency: prohibited
- Package model: functional packages; Requirement ID packages prohibited
- Optional packs: Batch, REF Operations, REF Backoffice, REF Gateway Simulator
- Batch DB pack: V94/U94 `CPF_REF_BAT_*`; Batch Off must omit it
- Protected stream: README and README-linked Guide/Manual excluded

## Required validation order

1. Exact SHA/clean tree and merged low-cost Source Gate
2. Java 25 single fresh-cache lifecycle
3. Optional Pack removal compile matrix
4. ADM/BZA frontend clean verify once each
5. Oracle/PostgreSQL/MariaDB V93/V94 lifecycle once per vendor, including Batch Off
6. Grouped runtime/fault/multi-instance/browser
7. Supply-chain
8. Exact result SHA evidence and status reconciliation

A failed prerequisite stops later expensive stages. File·Class·Interface·Marker·Matrix existence alone cannot close a requirement.
