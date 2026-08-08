# QA Rework Request

## Basis

- Development basis: `f6d7080c5a14b7dd7595093f9497470169e18d80` (`master`)
- Developer direct review: Central 36/36, Canonical 169/169, Previous 56/56, Special Review 1000/1000, EDU 135/135.
- Unified Developer ledger: `REQUIREMENT_STATUS.csv` = 1,409 rows.
- This file does **not** change QA-owned status. It requests successor-exact-SHA QA re-execution after user-controlled apply/commit/push.

## Developer rework completion condition

All currently developer-remediable source/consumer/test/harness gaps found in this cycle were implemented. No known developer-remediable FAIL remains in the Developer-owned matrices. Remaining non-PASS items are runtime-only or successor-exact-SHA verification.

## QA must independently rerun

1. Exact successor HEAD provenance and package/hash integrity.
2. Java 25 + Gradle 9.1 full configuration/build/test/publication.
3. Oracle/PostgreSQL/MariaDB live install/upgrade/rollback/reapply and runtime queries.
4. Authenticated ADM/BZA 3-browser flows and generated-client closure.
5. Real multi-instance/process/broker/DR/authorized release-target qualification.
6. Central 36, Canonical 169, Previous 56, Special 1000, EDU 135 and Runtime 13 without inheriting Developer PASS.

Until QA reruns on the successor checkout: `UNVERIFIED / RELEASE_BLOCKED`.
