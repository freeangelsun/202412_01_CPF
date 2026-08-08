# CPF Test and Evidence — Current

## Basis
- remote `master`: `9f16468cccae71523f65f0aefcd94322788c4dd0`
- request authoring source: `a570b366ef85b23863e41173c991025c072a2427`
- GitHub connector used read-only; no commit/push/delete.
- local full repository clone unavailable because external GitHub DNS/network access from the execution container failed.
- execution JVM: OpenJDK 21; canonical target from `gradle/cpf-stack.properties`: Java 25 / Gradle 9.1.0 / Spring Boot 4.1.0.

## Direct review gates
- Core Hardening direct Source review: **180/180** (`CPF_CORE_HARDENING_AUDIT.csv`)
- Fundamental Baseline Audit: **240/240** (`CPF_FUNDAMENTAL_BASELINE_AUDIT.csv`)
- Persistence detail: **35/35** (`CPF_PERSISTENCE_BASELINE_AUDIT.csv`)
- Consumer review: **180/180 checked** (actual consumer or explicit N/A boundary)
- Test/Harness review: **180/180 checked** (actual test/harness or explicit N/A; execution separated)
- developer-remediable gaps: **21 found / 21 remediated / 0 remaining**
- runtime-only verification: **10** (`RUNTIME_ONLY_VERIFICATION.csv`)

## Executed low-cost validations
1. `javac --release 21` for new/changed provider-neutral Core API + broker contracts: **EXIT 0**, 69 classes.
2. `javac --release 21` for pure durable TCC business/recovery classes: **EXIT 0**.
3. New Public Core API Korean JavaDoc gate: **0 missing**.
4. Starter catalog JSON parse/partition static check: 45 physical modules; 6 public profiles; 7 capability groups; new modules internal.
5. DB3 static parity: Oracle/PostgreSQL/MariaDB all contain inbox hardening, XA recovery, tamper audit, durable TCC in Source/Migration/Install/Verify/Rollback.
6. DB vendor policy scan: product SQL/evidence uses only Oracle, PostgreSQL, MariaDB; an unsupported-vendor rejection Negative Test remains outside product evidence.
7. Stale relocation parent review on successor: MyBatis old `com/cpf/core`/`com/cpf/common` and old `starter/persistence`; messaging direct `.../reliability/*` replaced by `.../reliability/jdbc`; session direct `.../security/*` replaced by `.../security/session`. Current delete target count = 0.
8. Overlay JSON/CSV/package/hash gates are executed again before packaging.

## Not executed / not claimed PASS
The 10 rows in `RUNTIME_ONLY_VERIFICATION.csv` require Java25/Gradle9.1, PowerShell, DB3, broker, IdP, HSM or external SOAP runtime. They remain `미검증`; no READY/PLANNED value is counted as PASS.
