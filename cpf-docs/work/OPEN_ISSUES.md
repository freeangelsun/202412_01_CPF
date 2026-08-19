# CPF OPEN ISSUES

## Current status

No reproducible source/static blocker is intentionally left open in the desired-state development package. Remaining items are **environment-dependent runtime revalidation conditions** and are not recorded as PASS.

## Runtime revalidation required

1. Java 25 Root Gradle full configuration/compile/test/build/publication/SBOM on the user's environment.
2. `cpf-biz-channel` standalone Gradle build/test with network/dependency resolution available.
3. ADM/BZA Frontend clean install/build/test with official Node `>=22.18.0 <25`.
4. Live Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Runtime/Rollback.
5. Redis/Valkey live integration, reconnect and failure behavior.
6. Multi-WAS policy propagation, Runtime Identity uniqueness, subject concurrent bind and retention lease contention.
7. Process-kill/restart/redeploy recovery.
8. ADM + external BZA Channel/Frontend real-browser responsive/accessibility/error-flow E2E.
9. PowerShell final-validation/publish drivers on Windows.
10. Public Git real remote clone/clean-consumer/commit/push. The publish driver must remain fail-closed and may push only after every prerequisite Gate passes.

## Completion rule

Any failure in the above local/runtime validation reopens the same Requirement. It must be repaired by common root cause and revalidated; it must not be relabeled as an environment PASS.
