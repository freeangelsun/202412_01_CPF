# CPF Session 18 — Test and Evidence

## Basis

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Exact source basis SHA: `4c4248a12e699c07f9f5fb11fbb33b97ca04077d`
- Latest commit observed through GitHub connector: `07_16`
- Local repository Working Tree: **NOT_AVAILABLE**. This session had no local clone; therefore no local `git status` or full Gradle wrapper execution is represented as PASS.
- Overlay root: `CPF_S18_FINAL_OVERLAY`

## Executed low-cost gates

| Gate | Actual result |
| --- | --- |
| `javac --release 21` over Session 18 `cpf-core` changed/new Java sources | **PASS**, exit 0, 23 class files |
| `javac --release 21` over full `cpf-foundation` | **PASS**, exit 0, 20 class files |
| `python cpf-tools/verification/verify_nxt_architecture.py` on Overlay | **PASS**, Core overlay classes classified, forbidden imports 0, external `com.cpf.core.internal` refs 0 |
| `python cpf-tools/verification/verify_nxt_static.py` on Overlay | **PASS**, errors 0, max relative path 169 |
| Core forbidden dependency/reference scan | **PASS**, Web/WebFlux/Servlet/Batch/OTel/Redis/AWS references in changed Core surface 0 |
| Foundation forbidden import scan | **PASS**, Spring/Jakarta/OTel/AWS/Apache imports 0 |
| Starter catalog duplicate check | **PASS**, 53 modules, duplicate `projectPath/artifactId/ownerPath/configPrefix` 0 |
| DB Vendor scope | **PASS**, Session 18 Overlay contains only Oracle/PostgreSQL/MariaDB vendor paths |
| Runtime Health DB parity | **PASS (static)**, each vendor has Source + Install + V112 + R112 + Verify + Runtime Query; Seed=N/A because registry starts empty |
| ADM Health contract path | **PASS (static)**, Controller -> OpenAPI operationId -> generated `cpf-api.ts` -> Pinia action -> Vue route/page |
| Delete safety | **PASS (static)**, new pending paths protected/root-escape violations 0; actual deletion NOT_EXECUTED |
| JSON/CSV parse in Overlay | **PASS** through `verify_nxt_static.py` |

## Environment evidence

```text
Java runtime: OpenJDK 21.0.11
javac: 21.0.11
Gradle CLI: command not found
Node: v22.16.0
npm: 10.9.2
Python: 3.13.5
```

CPF target Java 25/full Gradle verification cannot be substituted with the Java 21 low-cost compile above. Runtime-only and environment-dependent checks are therefore listed in `RUNTIME_ONLY_VERIFICATION.csv` with rerun conditions and pass/fail criteria.

## Source/ownership evidence

- `cpf-foundation` is a separate physical Pure Java module.
- `cpf-core` changed build boundary does not carry Web/WebFlux/Servlet/Batch/OTel compile dependencies.
- `CpfHttpClient` public contract is provider-neutral through `CpfHttpPath` and `CpfTypeRef`; Spring URI/type tokens live in the HTTP starter.
- OTel SDK/exporter wiring belongs to OTLP provider; generic Observability owns fallback telemetry and logging runtime.
- Feature flag provider belongs to `feature-flag-openfeature`.
- Web/Servlet filters/interceptor/advice belong to `web-api` profile.
- SQL/vendor/transaction metadata runtime belongs to `persistence-jdbc`.
- Runtime fault injection and in-memory lock test provider belong to `cpf-testkit`.
- Valkey lock uses the existing `CpfLockManager`/`CpfLockStore` contract and a monotonic fencing source; forced release defaults to an unavailable/fail-closed audit sink.

## Completion interpretation

Developer-remediable source/package gaps found during this session were corrected in the Overlay and the final low-cost gates are green. **This is not a QA PASS and not a claim that NOT_EXECUTED runtime checks passed.** Independent QA must use the post-apply, post-authorized-delete central SHA.
