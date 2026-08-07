# ARCHITECTURE DECISION REQUIRED

1. **Core persistence ownership** — move MyBatis/JDBC implementation out of `cpf-core` behind topology-independent Core API/SPI.
2. **Transaction lineage source of truth** — decide whether `cpf_transaction_lineage` is authoritative persisted lineage or an optional projection. If authoritative, define writers/reconcile/idempotency and canonical schema ownership; if not, remove dual-primary ambiguity and standardize federated-source query ownership.
3. **EDU dormant Product/Merge handlers** — authorize deletion/migration or convert the 13 non-executable classes into non-runtime documentation/redirect artifacts. QA did not delete them.
4. **Retired BZA API representation** — choose canonical retirement contract so 410 endpoints are not counted as active operation/consumer closure.
