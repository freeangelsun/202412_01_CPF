# CPF OPEN ISSUES

## Current status

No reproducible source/static blocker is intentionally left open in the development workspace. The remaining items are **runtime revalidation conditions** and therefore are not recorded as PASS.

## Runtime revalidation required

1. Java 25 root Gradle full configuration/compile/test/build/publication/SBOM on the user's environment.
2. Live Oracle/PostgreSQL/MariaDB Fresh/Upgrade/Runtime/Rollback.
3. Redis/Valkey live integration and reconnect/failure behavior.
4. Multi-WAS policy propagation, subject concurrent bind and retention single-executor/lease contention.
5. Process-kill/restart/redeploy recovery.
6. Final real-browser Playwright responsive/accessibility E2E on the latest modified source.

## Completion rule

Any failure in the above local/runtime validation reopens the same Requirement. It must be repaired by common root cause and revalidated; it must not be relabeled as environment PASS.
