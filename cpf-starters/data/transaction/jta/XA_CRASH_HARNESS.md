# XA prepare-kill-recovery harness

This harness is opt-in and must run only against disposable Oracle/PostgreSQL/MariaDB test resources.

1. Start two XA resources and wrap both in `CpfXaCrashProbeResource` with the same `AtomicInteger` and `expectedPrepared=2`.
2. Run the DB+DB or DB+JMS transaction with `-Dcpf.xa.harness.crash=true`.
3. Assert process exit 73 and marker `PREPARED` exists.
4. Restart with crash flag false and the same Narayana object store/recovery configuration.
5. Run recovery scan and query both business resources by stable transactionId.
6. Assert final decision is consistent, duplicate business side effect count is 0, and ADM/recovery evidence contains the same transactionId.

The test is intentionally not registered in the normal unit-test task because it kills its own JVM.
