# Open Issues

There are **no known Developer-GPT-remediable source/package defects remaining in the Session 18 Overlay after the final low-cost gates**.

Remaining items are verification/authority conditions, not silently promoted to PASS:

1. **User-authorized physical relocation delete** — 67 exact Core source paths are `PENDING_USER_APPROVAL`. Apply does not delete them. Until the delete helper is explicitly authorized/executed, full-repository Core Slimming Gate is expected to detect the legacy originals.
2. **Java 25 / Gradle full build and test** — not executed because this session has Java 21, no Gradle CLI, and no local repository clone.
3. **DB/Valkey/S3/IdP/browser/multi-instance/process-kill runtime suites** — not executed; exact scenarios and pass/fail conditions are in `RUNTIME_ONLY_VERIFICATION.csv`.
4. **Independent QA A/B/Cross/Fundamentals** — Developer GPT did not self-certify QA. QA must run against the post-apply central SHA.

Any failure from those reruns reopens the corresponding exact NXT requirement; it must not be hidden as environment-only if the root cause is Source/Config/Test/SQL/Frontend/Generator code.
