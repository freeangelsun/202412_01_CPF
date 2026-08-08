# CPF Current Open Issues

Execution basis: `9f16468cccae71523f65f0aefcd94322788c4dd0`

Developer-remediable Source/SQL/API/Test/Config/Generator/Reference gaps found in Session 17: **0 remaining** after remediation.

The remaining items are **runtime-only verification**, not PASS:

- **RT-01** Java 25 / Gradle 9.1 fresh full build and test: No full repository clone/dependency cache in execution container. Rerun: `./gradlew clean test`
- **RT-02** XA DB+DB on Oracle/PostgreSQL/MariaDB: Requires two live XA-capable DB resources. Rerun: `run XA harness prepare-kill/recover per vendor`
- **RT-03** XA DB+JMS prepare-kill/recovery: Requires live XA JMS provider + DB. Rerun: `execute DB+JMS harness with process kill`
- **RT-04** Broker ACK loss/publisher/consumer kill/multi-instance: Requires live Kafka/RabbitMQ/JMS/IBM MQ as applicable. Rerun: `run reliability process-kill scenarios`
- **RT-05** Saga/TCC durable process-kill/restart: Requires live DB3 transaction tables. Rerun: `execute TCC/Saga restart/reconcile scenarios`
- **RT-06** Optional JPA DB3/JTA runtime: Requires live DB3 + Java25 dependencies. Rerun: `generate/run jpa domain and JTA variant`
- **RT-07** OIDC live SSO: Requires Keycloak/Entra ID/Okta tenant/client. Rerun: `login/logout/refresh/expiry BFF scenario`
- **RT-08** PKCS#11 KMS/HSM: Requires vendor PKCS#11 provider/token. Rerun: `sign/verify/rotate/revoke/timeout/health`
- **RT-09** SOAP live timeout/UNKNOWN: Requires controlled SOAP endpoint. Rerun: `success/fault/connect/read timeout/unknown`
- **RT-10** PowerShell Generator full execution: PowerShell is not installed in execution container. Rerun: `create jdbc/mybatis/jpa generated domains and build/test/client generation`

No Commit/Push/Delete was performed by Developer GPT.
