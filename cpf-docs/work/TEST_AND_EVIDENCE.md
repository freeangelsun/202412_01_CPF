## Current Central Recheck — 07_18 successor

- Repository basis reviewed: `bd9bb2ad3b5cdd2441b1c070c24c1ed9e2b63d52` (`07_18`).
- Previous Session18 development report used older basis SHA `4c4248a...`; PASS is not inherited.
- Core relocation work is retained, but `NXT-ARCH-002` is reopened because owner-specific/optional API/SPI remains in Core and the architecture gate defaulted unknown classes to `KEEP_CORE`.
- Root modules `cpf-foundation/` and `cpf-testkit/` are non-canonical. Physical owners are now fixed as `cpf-starters/foundation/core` and `cpf-tools/testing/cpf-testkit`.
- The revised architecture gate intentionally FAILS until Core Admission, physical relocation, stale-reference, duplicate and garbage closure are complete.
- Runtime NOT_EXECUTED remains NOT_EXECUTED; it is not promoted to PASS.

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

## Unified Context / Header / Mandatory Batch Central Currentization

Basis SHA: `223e76f0da8cdfcd1769477bf9bf4661ae01faca`

Central source inspection confirmed:
- Core `TransactionContext` still imports SLF4J MDC and Spring RequestContextHolder.
- Core `TransactionHeader` mixes transaction/execution/http/channel/identity/client/network/trace/extension.
- Core `common/header` still contains HTTP Header Runtime/Trusted Proxy/Audit/Masking classes.
- Existing Header Policy needs trust/scope/mutation/direction/compatibility semantics.

Therefore prior completion is not inherited.
This package is **Requirement/Architecture currentization only** and does not claim Source PASS.

Developer successor must implement and execute:
- Core Unified Context
- existing Header mapping/policy
- mandatory Web/Gateway/Message/Async/Batch/Center-Cut fan-out
- Batch restart/process-kill/multi-instance
- Generator/EDU/ADM
- old source garbage closure
- permanent Root allowlist gate

Runtime NOT_EXECUTED remains NOT_EXECUTED.

## Final Context Design Freeze Evidence

Basis SHA: `223e76f0da8cdfcd1769477bf9bf4661ae01faca`

설계 검산 입력:
- `API_GUIDE.md` Standard Header
- `CpfHeaderNames` / `CpfHeaderSpecs`
- `TransactionHeader`
- `TransactionContext`
- Core Transaction/Execution/Tenant/Lineage contracts
- BatchExecutionLink / BatControlHeaders
- existing integration/messaging/file/security capabilities
- W3C Trace Context, OpenTelemetry Propagators/Baggage, gRPC Context, Java 25 ScopedValue,
  MicroProfile Context Propagation, Spring Batch restart/ExecutionContext semantics

판정:
- 기존 최소 3-field Transaction Context 설계는 최종 명세로 부족함.
- Final design은 Core Kernel + typed Owner Component 전체 모델로 currentize.
- 이 문서 갱신 자체는 Source PASS가 아님.
- successor SHA에서 field-level implementation + all-boundary consumers + tests + garbage closure가 필요함.
